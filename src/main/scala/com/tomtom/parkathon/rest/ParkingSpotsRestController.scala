package com.tomtom.parkathon.rest

import com.tomtom.parkathon.db.ParkingSpotDatabase
import com.tomtom.parkathon.domain.{Location, ParkingSpot}
import org.springframework.web.bind.annotation._

@RestController
class ParkingSpotsRestController {

  val parkingSpotDatabase: ParkingSpotDatabase = new ParkingSpotDatabase()

  @GetMapping(Array("/parking-spots"))
  def getAvailableParkingSpots(@RequestParam position: String): Seq[ParkingSpot] = {
    val latLong: (Double, Double) = getLatitudeAndLongitude(position)
    parkingSpotDatabase.queryParkingSpots(latLong._1, latLong._2, 1000, 600)
  }

  private def getLatitudeAndLongitude(position: String): (Double, Double) =
    (position.substring(0, position.indexOf(":")).toDouble,
      position.substring(position.indexOf(":") + 1).toDouble)

  @PostMapping(Array("/park-start"))
  def startParking(@RequestBody location: Location): Unit =
    parkingSpotDatabase.deleteParkingSpot(location.latitude, location.longitude)

  @PostMapping(Array("/park-stop"))
  def stopParking(@RequestBody location: Location): Unit =
    parkingSpotDatabase.createParkingSpot(location.latitude, location.longitude)
}
