package com.ims.inventory.repository;

import com.ims.inventory.entity.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    Optional<StockReservation> findByIdempotencyKey(String idempotencyKey);
}
