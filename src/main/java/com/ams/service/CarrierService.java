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

    private void validateCarrierRules(Carrier c) {
        if (c.getCarrierName() == null || c.getCarrierName().trim().length() < 2) {
            throw new IllegalArgumentException("Carrier name must be at least 2 characters long.");
        }

        // Percentage boundaries check (< 90%)
        Double[] percentages = {
            c.getDiscount30DaysAdvanceBooking(), c.getDiscount60DaysAdvanceBooking(), c.getDiscount90DaysAdvanceBooking(),
            c.getBulkBookingDiscount(), c.getSilverUserDiscount(), c.getGoldUserDiscount(), c.getPlatinumUserDiscount(),
            c.getRefund2DaysBeforeTravelDate(), c.getRefund10DaysBeforeTravelDate(), c.getRefund20DaysOrMoreBeforeTravelDate()
        };

        for (Double pct : percentages) {
            if (pct != null && (pct < 0 || pct >= 90.0)) {
                throw new IllegalArgumentException("Discount and refund percentages must be strictly less than 90% (values >= 90% are rejected).");
            }
        }

        // Tiered hierarchy validation: 30-Day < 60-Day < 90-Day
        if (c.getDiscount30DaysAdvanceBooking() >= c.getDiscount60DaysAdvanceBooking() ||
            c.getDiscount60DaysAdvanceBooking() >= c.getDiscount90DaysAdvanceBooking()) {
            throw new IllegalArgumentException("Advance booking discount hierarchy violation: 30-Day Discount < 60-Day Discount < 90-Day Discount is strictly required.");
        }

        // Tiered hierarchy validation: Silver < Gold < Platinum
        if (c.getSilverUserDiscount() >= c.getGoldUserDiscount() ||
            c.getGoldUserDiscount() >= c.getPlatinumUserDiscount()) {
            throw new IllegalArgumentException("Membership discount hierarchy violation: Silver Discount < Gold Discount < Platinum Discount is strictly required.");
        }

        // Tiered hierarchy validation: Refund (<2 days) < Refund (2-10 days) < Refund (>20 days)
        if (c.getRefund2DaysBeforeTravelDate() >= c.getRefund10DaysBeforeTravelDate() ||
            c.getRefund10DaysBeforeTravelDate() >= c.getRefund20DaysOrMoreBeforeTravelDate()) {
            throw new IllegalArgumentException("Cancellation refund hierarchy violation: Refund (<2 days) < Refund (2-10 days) < Refund (>20 days) is strictly required.");
        }
    }

    public Carrier registerCarrier(Carrier carrier) {
        validateCarrierRules(carrier);
        if (carrierRepository.existsByCarrierNameIgnoreCase(carrier.getCarrierName())) {
            throw new IllegalArgumentException("Carrier '" + carrier.getCarrierName() + "' is already registered");
        }
        return carrierRepository.save(carrier);
    }

    public Carrier updateCarrier(Long carrierId, Carrier updatedCarrier) {
        validateCarrierRules(updatedCarrier);
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
