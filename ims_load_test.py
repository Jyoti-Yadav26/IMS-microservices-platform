"""
Load / resilience test script for the IMS microservices project.

Requires: pip install requests --break-system-packages
Requires: docker compose up --build   (running locally, gateway on localhost:8080)

Run each test independently — read the code before running so you can
explain exactly what it does and why, in an interview.

    python ims_load_test.py idempotency
    python ims_load_test.py concurrency
    python ims_load_test.py circuit-breaker   (see instructions in that function)
"""

import concurrent.futures
import requests
import sys
import time
import uuid

BASE_URL = "http://localhost:8080"


def create_test_product(sku, quantity):
    """Create a fresh product with a given starting stock quantity."""
    resp = requests.post(
        f"{BASE_URL}/api/inventory/products",
        json={
            "sku": sku,
            "name": "Load Test Widget",
            "description": "created by load test script",
            "price": 9.99,
            "quantity": quantity,
            "reorderThreshold": 1,
        },
        timeout=5,
    )
    print(f"Created product {sku} qty={quantity} -> status {resp.status_code}")
    return resp


def place_order(sku, qty):
    """
    Place an order via the public order API.
    NOTE: OrderRequest has NO idempotencyKey field (checked OrderRequest.java) —
    order-service generates its own key internally per order+sku
    (orderNumber + "-" + sku, see OrderServiceImpl.reserveItem). Every call here
    creates a genuinely NEW order, so this is NOT a way to test idempotency —
    see reserve_stock_direct() below for that. This function is only used by
    test_concurrency, where creating N distinct orders is exactly what we want.
    """
    payload = {
        "customerEmail": "loadtest@example.com",
        "items": [{"sku": sku, "quantity": qty, "unitPrice": 9.99}],
    }
    start = time.time()
    try:
        resp = requests.post(f"{BASE_URL}/api/orders", json=payload, timeout=10)
        elapsed = time.time() - start
        return resp.status_code, elapsed, resp.json() if resp.content else {}
    except requests.exceptions.RequestException as e:
        elapsed = time.time() - start
        return None, elapsed, {"error": str(e)}


def reserve_stock_direct(sku, qty, idempotency_key):
    """
    Calls inventory-service's /api/inventory/reserve endpoint DIRECTLY through
    the gateway, bypassing order-service. This is the only way to control the
    idempotencyKey from outside, and it's the actual field ProductServiceImpl
    checks against the stock_reservations table (see reserveStock() in
    ProductServiceImpl.java).
    """
    payload = {"sku": sku, "quantity": qty, "idempotencyKey": idempotency_key}
    try:
        resp = requests.post(f"{BASE_URL}/api/inventory/reserve", json=payload, timeout=10)
        return resp.status_code, resp.json() if resp.content else {}
    except requests.exceptions.RequestException as e:
        return None, {"error": str(e)}


def test_idempotency(n_requests=10):
    """
    Fires N concurrent reservation requests against inventory-service's
    /reserve endpoint directly, all using the SAME idempotencyKey. Per
    ProductServiceImpl.reserveStock: the first request to actually commit
    writes a row in stock_reservations keyed by idempotencyKey; every other
    request (concurrent or sequential) finds that row and replays its result
    instead of decrementing stock again.

    Expected if idempotency works: stock drops by exactly `order_qty`,
    regardless of n_requests.
    Expected if broken: stock drops by order_qty * n_requests.
    """
    sku = f"IDEMP-{uuid.uuid4().hex[:6]}"
    starting_qty = 100
    order_qty = 5
    key = str(uuid.uuid4())

    create_test_product(sku, starting_qty)

    print(f"\nFiring {n_requests} concurrent /reserve calls, same idempotencyKey={key}")
    with concurrent.futures.ThreadPoolExecutor(max_workers=n_requests) as ex:
        futures = [ex.submit(reserve_stock_direct, sku, order_qty, key) for _ in range(n_requests)]
        results = [f.result() for f in futures]

    statuses = [r[0] for r in results]
    print(f"Status codes: {statuses}")

    check = requests.get(f"{BASE_URL}/api/inventory/products/{sku}", timeout=5)
    remaining = check.json().get("quantity") if check.ok else None
    expected_if_correct = starting_qty - order_qty
    expected_if_broken = starting_qty - (order_qty * n_requests)

    print(f"Remaining stock: {remaining}")
    print(f"Expected if idempotency works: {expected_if_correct}")
    print(f"Expected if idempotency is broken: {expected_if_broken}")


def test_concurrency(n_requests=20, starting_qty=10, order_qty=1):
    """
    Fires N concurrent orders for the SAME sku with LIMITED stock and
    DIFFERENT idempotency keys (this is a genuine race, not a duplicate).
    With optimistic locking working correctly, exactly `starting_qty`
    orders should succeed and the rest should fail with insufficient stock
    — never oversold.
    """
    sku = f"CONC-{uuid.uuid4().hex[:6]}"
    create_test_product(sku, starting_qty)

    print(f"\nFiring {n_requests} concurrent orders against {starting_qty} units of stock")
    with concurrent.futures.ThreadPoolExecutor(max_workers=n_requests) as ex:
        futures = [ex.submit(place_order, sku, order_qty) for _ in range(n_requests)]
        results = [f.result() for f in futures]

    succeeded = sum(1 for _, _, body in results if body.get("status") not in ("REJECTED", "FAILED"))
    rejected = sum(1 for _, _, body in results if body.get("status") == "REJECTED")
    failed = sum(1 for _, _, body in results if body.get("status") == "FAILED")

    print(f"Succeeded: {succeeded} (should be <= {starting_qty})")
    print(f"Rejected (business, insufficient stock): {rejected}")
    print(f"Failed (infra error): {failed}")

    check = requests.get(f"{BASE_URL}/api/inventory/products/{sku}", timeout=5)
    remaining = check.json().get("quantity") if check.ok else None
    print(f"Remaining stock: {remaining} (should be {starting_qty - succeeded * order_qty}, never negative)")


def test_circuit_breaker(n_requests=15):
    """
    MANUAL STEPS REQUIRED before running this:
      1. docker compose up --build   (let everything start)
      2. docker compose stop inventory-service
      3. Then run: python ims_load_test.py circuit-breaker

    This fires repeated orders while inventory-service is down and times
    each response. Early requests should be SLOW (waiting through retry +
    timeout budget before failing). After the circuit breaker trips
    (CLOSED -> OPEN), later requests should fail FAST because the breaker
    short-circuits the call instead of waiting on a dead dependency.

    That latency drop, timestamped and logged, IS your real number —
    e.g. "average response time dropped from 2.1s to 40ms once the circuit
    breaker opened." Check actuator/health on order-service to confirm the
    state transition: http://localhost:8082/actuator/health
    """
    sku = f"CB-{uuid.uuid4().hex[:6]}"
    print("NOTE: this assumes the product/sku already exists or inventory-service")
    print("was up when it was created — if inventory-service is down, product")
    print("creation itself will fail. Create the product BEFORE stopping the service.")

    for i in range(n_requests):
        status, elapsed, body = place_order(sku, 1)
        print(f"Request {i+1}: status={status} elapsed={elapsed:.3f}s body={body}")
        time.sleep(0.2)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)

    test = sys.argv[1]
    if test == "idempotency":
        test_idempotency()
    elif test == "concurrency":
        test_concurrency()
    elif test == "circuit-breaker":
        test_circuit_breaker()
    else:
        print(f"Unknown test: {test}")
        print(__doc__)