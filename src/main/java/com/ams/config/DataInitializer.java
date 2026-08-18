package com.ams.config;

import com.ams.domain.Carrier;
import com.ams.domain.Flight;
import com.ams.domain.User;
import com.ams.repository.CarrierRepository;
import com.ams.repository.FlightRepository;
import com.ams.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CarrierRepository carrierRepository;
    private final FlightRepository flightRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository,
                           CarrierRepository carrierRepository,
                           FlightRepository flightRepository,
                           JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.carrierRepository = carrierRepository;
        this.flightRepository = flightRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        // 0. Auto Schema Migration for existing databases (adds schedule_date, user_email, user_phone if missing)
        try {
            jdbcTemplate.execute("ALTER TABLE flights ADD COLUMN schedule_date DATE");
            System.out.println("✅ Added missing schedule_date column to flights table.");
        } catch (Exception ignored) {}

        try {
            jdbcTemplate.execute("ALTER TABLE flights ADD COLUMN flight_frequency VARCHAR(50) DEFAULT 'SINGLE_DATE'");
        } catch (Exception ignored) {}

        try {
            jdbcTemplate.execute("UPDATE flights SET flight_frequency = 'SINGLE_DATE' WHERE flight_frequency IS NULL");
        } catch (Exception ignored) {}

        try {
            jdbcTemplate.execute("ALTER TABLE bookings ADD COLUMN user_email VARCHAR(100)");
        } catch (Exception ignored) {}

        try {
            jdbcTemplate.execute("ALTER TABLE bookings ADD COLUMN user_phone VARCHAR(20)");
        } catch (Exception ignored) {}

        try {
            jdbcTemplate.execute("ALTER TABLE bookings ADD COLUMN customer_category VARCHAR(20)");
        } catch (Exception ignored) {}

        // 1. Seed System Admin User if no Admin exists
        if (userRepository.findByUserName("admin").isEmpty()) {
            User admin = User.builder()
                    .userName("admin")
                    .password("admin123")
                    .role("ADMIN")
                    .customerCategory("REGULAR")
                    .phone("9876543210")
                    .emailId("admin@ams.com")
                    .address1("AMS Corporate HQ")
                    .city("Mumbai")
                    .state("Maharashtra")
                    .zipCode("400001")
                    .dob(LocalDate.of(1990, 1, 1))
                    .build();
            userRepository.save(admin);
            System.out.println("✅ Default System Admin created: Username: admin | Password: admin123");
        }

        // 2. Seed Default Carrier if empty
        if (carrierRepository.count() == 0) {
            Carrier indigo = Carrier.builder()
                    .carrierName("Indigo Airlines")
                    .discount30DaysAdvanceBooking(10.0)
                    .discount60DaysAdvanceBooking(15.0)
                    .discount90DaysAdvanceBooking(20.0)
                    .bulkBookingDiscount(12.0)
                    .silverUserDiscount(5.0)
                    .goldUserDiscount(10.0)
                    .platinumUserDiscount(15.0)
                    .refund2DaysBeforeTravelDate(0.0)
                    .refund10DaysBeforeTravelDate(50.0)
                    .refund20DaysOrMoreBeforeTravelDate(80.0)
                    .build();

            Carrier airIndia = Carrier.builder()
                    .carrierName("Air India Express")
                    .discount30DaysAdvanceBooking(8.0)
                    .discount60DaysAdvanceBooking(12.0)
                    .discount90DaysAdvanceBooking(18.0)
                    .bulkBookingDiscount(10.0)
                    .silverUserDiscount(5.0)
                    .goldUserDiscount(10.0)
                    .platinumUserDiscount(15.0)
                    .refund2DaysBeforeTravelDate(0.0)
                    .refund10DaysBeforeTravelDate(45.0)
                    .refund20DaysOrMoreBeforeTravelDate(75.0)
                    .build();

            indigo = carrierRepository.save(indigo);
            airIndia = carrierRepository.save(airIndia);
            System.out.println("✅ Sample Carriers Seeded.");

            // 3. Seed Default Flights
            Flight flight1 = Flight.builder()
                    .carrierId(indigo.getCarrierId())
                    .carrierName(indigo.getCarrierName())
                    .origin("Mumbai (BOM)")
                    .destination("Dubai (DXB)")
                    .scheduleDate(LocalDate.now())
                    .departureTime("10:30 AM")
                    .arrivalTime("01:45 PM")
                    .airFare(28000.0)
                    .economyClassFare(28000.0)
                    .businessClassFare(55000.0)
                    .executiveClassFare(95000.0)
                    .seatCapacityEconomyClass(150)
                    .seatCapacityBusinessClass(30)
                    .seatCapacityExecutiveClass(12)
                    .bookedSeatsEconomyClass(0)
                    .bookedSeatsBusinessClass(0)
                    .bookedSeatsExecutiveClass(0)
                    .build();

            Flight flight2 = Flight.builder()
                    .carrierId(airIndia.getCarrierId())
                    .carrierName(airIndia.getCarrierName())
                    .origin("Delhi (DEL)")
                    .destination("Bengaluru (BLR)")
                    .scheduleDate(LocalDate.now().plusDays(1))
                    .departureTime("06:15 AM")
                    .arrivalTime("09:00 AM")
                    .airFare(6500.0)
                    .economyClassFare(6500.0)
                    .businessClassFare(14000.0)
                    .executiveClassFare(22000.0)
                    .seatCapacityEconomyClass(180)
                    .seatCapacityBusinessClass(24)
                    .seatCapacityExecutiveClass(8)
                    .bookedSeatsEconomyClass(0)
                    .bookedSeatsBusinessClass(0)
                    .bookedSeatsExecutiveClass(0)
                    .build();

            flightRepository.save(flight1);
            flightRepository.save(flight2);
            System.out.println("✅ Sample Flights Seeded.");
        }
    }
}
