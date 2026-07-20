package com.ims.inventory.service;

import com.ims.inventory.dto.ProductRequest;
import com.ims.inventory.dto.ProductResponse;
import com.ims.inventory.dto.StockReservationRequest;
import com.ims.inventory.dto.StockReservationResponse;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProduct(String sku);

    List<ProductResponse> getAllProducts();

    StockReservationResponse reserveStock(StockReservationRequest request);

    ProductResponse restock(String sku, int quantity);
}
