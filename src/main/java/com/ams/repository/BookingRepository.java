package com.ams.repository;

import com.ams.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByUserIdOrderByBookingIdDesc(Long userId);
    List<Booking> findAllByOrderByBookingIdDesc();
    boolean existsByFlightIdAndBookingStatusNot(Long flightId, String bookingStatus);
    boolean existsByFlightId(Long flightId);
}
