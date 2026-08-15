package com.ams.service;

import com.ams.domain.Carrier;
import com.ams.repository.CarrierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarrierService {

    private final CarrierRepository carrierRepository;

    public Carrier registerCarrier(Carrier carrier) {
        if (carrierRepository.existsByCarrierNameIgnoreCase(carrier.getCarrierName())) {
            throw new IllegalArgumentException("Carrier '" + carrier.getCarrierName() + "' is already registered");
        }
        return carrierRepository.save(carrier);
    }

    public Carrier updateCarrier(Long carrierId, Carrier updatedCarrier) {
        Carrier existing = carrierRepository.findById(carrierId)
                .orElseThrow(() -> new IllegalArgumentException("Carrier not found with ID: " + carrierId));

        existing.setCarrierName(updatedCarrier.getCarrierName());
        existing.setDiscount30DaysAdvanceBooking(updatedCarrier.getDiscount30DaysAdvanceBooking());
        existing.setDiscount60DaysAdvanceBooking(updatedCarrier.getDiscount60DaysAdvanceBooking());
        existing.setDiscount90DaysAdvanceBooking(updatedCarrier.getDiscount90DaysAdvanceBooking());
        existing.setBulkBookingDiscount(updatedCarrier.getBulkBookingDiscount());
        existing.setSilverUserDiscount(updatedCarrier.getSilverUserDiscount());
        existing.setGoldUserDiscount(updatedCarrier.getGoldUserDiscount());
        existing.setPlatinumUserDiscount(updatedCarrier.getPlatinumUserDiscount());
        existing.setRefund2DaysBeforeTravelDate(updatedCarrier.getRefund2DaysBeforeTravelDate());
        existing.setRefund10DaysBeforeTravelDate(updatedCarrier.getRefund10DaysBeforeTravelDate());
        existing.setRefund20DaysOrMoreBeforeTravelDate(updatedCarrier.getRefund20DaysOrMoreBeforeTravelDate());

        return carrierRepository.save(existing);
    }

    public List<Carrier> getAllCarriers() {
        return carrierRepository.findAll();
    }

    public Carrier getCarrierById(Long carrierId) {
        return carrierRepository.findById(carrierId)
                .orElseThrow(() -> new IllegalArgumentException("Carrier not found with ID: " + carrierId));
    }
}
