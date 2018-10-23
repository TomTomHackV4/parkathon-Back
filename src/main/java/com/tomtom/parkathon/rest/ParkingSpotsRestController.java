package com.tomtom.parkathon.rest;

import com.tomtom.parkathon.domain.ParkingSpot;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class ParkingSpotsRestController {

    @GetMapping("/parking-spots")
    public List<ParkingSpot> getAvailableParkingSpots() {
        return Arrays.asList(
                new ParkingSpot(52000000L, 13000000L),
                new ParkingSpot(52000000L, 13000000L),
                new ParkingSpot(52000000L, 13000000L));
    }
}
