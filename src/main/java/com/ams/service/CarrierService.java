package com.ams.service;

import com.ams.domain.Carrier;
import com.ams.repository.CarrierRepository;
import com.ams.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarrierService {

    private final CarrierRepository carrierRepository;
    private final FlightRepository flightRepository;

    private void validateCarrierRules(Carrier c) {
        if (c.getCarrierName() == null || c.getCarrierName().trim().length() < 2) {
            throw new IllegalArgumentException("Carrier name must be at least 2 characters long.");
        }

        // Refund Percentages boundaries check: <2d (max 20%), 2-19d (max 40%), >=20d (max 75%)
        if (c.getRefund2DaysBeforeTravelDate() != null && (c.getRefund2DaysBeforeTravelDate() < 0 || c.getRefund2DaysBeforeTravelDate() > 20.0)) {
            throw new IllegalArgumentException("< 2 Days refund percentage cannot exceed 20%.");
        }
        if (c.getRefund10DaysBeforeTravelDate() != null && (c.getRefund10DaysBeforeTravelDate() < 0 || c.getRefund10DaysBeforeTravelDate() > 40.0)) {
            throw new IllegalArgumentException("2 to 19 Days refund percentage cannot exceed 40%.");
        }
        if (c.getRefund20DaysOrMoreBeforeTravelDate() != null && (c.getRefund20DaysOrMoreBeforeTravelDate() < 0 || c.getRefund20DaysOrMoreBeforeTravelDate() > 75.0)) {
            throw new IllegalArgumentException(">= 20 Days refund percentage cannot exceed 75%.");
        }

        // Discount Percentages boundaries check (<= 40%)
        Double[] discountPcts = {
            c.getDiscount30DaysAdvanceBooking(), c.getDiscount60DaysAdvanceBooking(), c.getDiscount90DaysAdvanceBooking(),
            c.getBulkBookingDiscount(), c.getSilverUserDiscount(), c.getGoldUserDiscount(), c.getPlatinumUserDiscount()
        };

        for (Double pct : discountPcts) {
            if (pct != null && (pct < 0 || pct > 40.0)) {
                throw new IllegalArgumentException("Discount percentage cannot exceed 40%.");
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

    public void deleteCarrier(Long carrierId) {
        Carrier carrier = getCarrierById(carrierId);
        if (flightRepository.existsByCarrierId(carrierId)) {
            throw new IllegalArgumentException("We can't delete the carrier, it has active flights.");
        }
        carrierRepository.delete(carrier);
    }
}
