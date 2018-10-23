package com.tomtom.parkathon.rest;

import java.util.List;

import org.assertj.core.util.Lists;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tomtom.parkathon.domain.ParkingSpot;

@RestController
public class ParkingSpotsRestController {

	@GetMapping("/parking-spots")
	public List<ParkingSpot> getAvailableParkingSpots() {
		return Lists.newArrayList(
				new ParkingSpot(52000000L, 13000000L),
				new ParkingSpot(52000000L, 13000000L),
				new ParkingSpot(52000000L, 13000000L));
	}
}
