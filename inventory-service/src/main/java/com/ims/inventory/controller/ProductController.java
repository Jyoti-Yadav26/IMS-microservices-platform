package com.ims.inventory.controller;

import com.ims.inventory.dto.ProductRequest;
import com.ims.inventory.dto.ProductResponse;
import com.ims.inventory.dto.StockReservationRequest;
import com.ims.inventory.dto.StockReservationResponse;
import com.ims.inventory.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/products/{sku}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getProduct(sku));
    }

    @PostMapping("/products/{sku}/restock")
    public ResponseEntity<ProductResponse> restock(@PathVariable String sku, @RequestParam @Min(1) int quantity) {
        return ResponseEntity.ok(productService.restock(sku, quantity));
    }

    /**
     * Called synchronously by order-service to reserve/decrement stock while placing
     * an order. Returns 200 with reserved=false semantics folded into the response body
     * for the "insufficient stock" business case; validation/idempotency issues use
     * the normal HTTP error path via {@link com.ims.inventory.exception.GlobalExceptionHandler}.
     */
    @PostMapping("/reserve")
    public ResponseEntity<StockReservationResponse> reserveStock(@Valid @RequestBody StockReservationRequest request) {
        return ResponseEntity.ok(productService.reserveStock(request));
    }
}
