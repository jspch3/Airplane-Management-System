package com.ams.controller;

import com.ams.domain.Carrier;
import com.ams.service.CarrierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carriers")
@RequiredArgsConstructor
public class CarrierController {

    private final CarrierService carrierService;

    // US004: Register Carrier
    @PostMapping
    public ResponseEntity<Carrier> registerCarrier(@Valid @RequestBody Carrier carrier) {
        Carrier savedCarrier = carrierService.registerCarrier(carrier);
        return new ResponseEntity<>(savedCarrier, HttpStatus.CREATED);
    }

    // US008: Update Carrier Details
    @PutMapping("/{carrierId}")
    public ResponseEntity<Carrier> updateCarrierDetails(@PathVariable Long carrierId, @Valid @RequestBody Carrier carrier) {
        Carrier updated = carrierService.updateCarrier(carrierId, carrier);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<Carrier>> getAllCarriers() {
        return ResponseEntity.ok(carrierService.getAllCarriers());
    }

    @GetMapping("/{carrierId}")
    public ResponseEntity<Carrier> getCarrierById(@PathVariable Long carrierId) {
        return ResponseEntity.ok(carrierService.getCarrierById(carrierId));
    }

    @DeleteMapping("/{carrierId}")
    public ResponseEntity<Void> deleteCarrier(@PathVariable Long carrierId) {
        carrierService.deleteCarrier(carrierId);
        return ResponseEntity.noContent().build();
    }
}
