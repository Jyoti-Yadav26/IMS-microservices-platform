package com.ims.inventory.service.impl;

import com.ims.inventory.dto.StockReservationRequest;
import com.ims.inventory.dto.StockReservationResponse;
import com.ims.inventory.entity.Product;
import com.ims.inventory.entity.ReservationStatus;
import com.ims.inventory.event.InventoryEventPublisher;
import com.ims.inventory.exception.InsufficientStockException;
import com.ims.inventory.repository.ProductRepository;
import com.ims.inventory.repository.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockReservationExecutorTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockReservationRepository reservationRepository;
    @Mock
    private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private StockReservationExecutor executor;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L).sku("SKU-1").name("Widget").price(BigDecimal.TEN)
                .quantity(10).reorderThreshold(5).version(0L)
                .build();
    }

    @Test
    void attemptReservation_decrementsQuantityOnSuccess() {
        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        StockReservationResponse response = executor.attemptReservation(
                new StockReservationRequest("SKU-1", 3, "key-1"));

        assertThat(response.reserved()).isTrue();
        assertThat(response.remainingQuantity()).isEqualTo(7);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(7);
    }

    @Test
    void attemptReservation_throwsAndRecordsFailureWhenStockInsufficient() {
        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> executor.attemptReservation(new StockReservationRequest("SKU-1", 50, "key-2")))
                .isInstanceOf(InsufficientStockException.class);

        verify(productRepository, never()).save(any());
        verify(reservationRepository).save(argThat(r -> r.getStatus() == ReservationStatus.FAILED));
    }

    @Test
    void attemptReservation_publishesLowStockEventWhenThresholdReached() {
        product.setQuantity(6); // 6 - 3 = 3, at or below reorderThreshold(5)
        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        executor.attemptReservation(new StockReservationRequest("SKU-1", 3, "key-3"));

        verify(eventPublisher).publishLowStock(argThat(p -> p.getQuantity() == 3));
    }

    @Test
    void attemptReservation_doesNotPublishLowStockEventWhenAboveThreshold() {
        when(productRepository.findBySku("SKU-1")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        executor.attemptReservation(new StockReservationRequest("SKU-1", 1, "key-4"));

        verify(eventPublisher, never()).publishLowStock(any());
    }
}
