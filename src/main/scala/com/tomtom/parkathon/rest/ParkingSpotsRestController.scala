package com.tomtom.parkathon.rest

import java.time.Instant
import java.util

import com.tomtom.parkathon.domain.ParkingSpot
import org.springframework.web.bind.annotation.{GetMapping, RestController}

import scala.collection.JavaConverters._

@RestController
class ParkingSpotsRestController {

  @GetMapping(Array("/parking-spots"))
  def getAvailableParkingSpots: util.List[ParkingSpot] =
    List(
      new ParkingSpot(52.133, 13.231, Instant.now),
      new ParkingSpot(52.132, 13.232, Instant.now),
      new ParkingSpot(52.131, 13.233, Instant.now),
    ).asJava
}
