package com.ims.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequest(
        @Email(message = "customerEmail must be a valid email") @NotEmpty String customerEmail,
        @NotEmpty(message = "order must contain at least one item") @Valid List<OrderItemRequest> items
) {
}
