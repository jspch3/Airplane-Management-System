package com.ams.repository;

import com.ams.domain.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Long> {
    Optional<Carrier> findByCarrierNameIgnoreCase(String carrierName);
    boolean existsByCarrierNameIgnoreCase(String carrierName);
}
