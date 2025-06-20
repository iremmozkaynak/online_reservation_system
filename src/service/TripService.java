package service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import models.*;
import repository.*;
 
public class TripService {
    private TripRepository tripRepository;
    
    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }
    public List<Trip> searchTrips(String startPoint, String endPoint, LocalDateTime departureTime) {
        return tripRepository.findTrips(startPoint, endPoint, departureTime);
    }
    public List<Seat> findAvailableSeats(String tripNo) {
        Trip trip = tripRepository.findByTripNo(tripNo);
        if (trip != null) {
            return trip.getSeats().stream()
                .filter(Seat::isAvailable)
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public void addTrip(Trip trip) { //admin
        if (tripRepository.findByTripNo(trip.getTripNo()) != null) {
            throw new IllegalArgumentException("Trip already exists");
        }
        tripRepository.save(trip);
    }

    public void deleteTrip(String tripNo) { //admin
        Trip trip = tripRepository.findByTripNo(tripNo);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found");
        }
        tripRepository.delete(trip);
    }

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public Trip findTripByNo(String tripNo) {
        return tripRepository.findByTripNo(tripNo);
    }

    public void updateTrip(Trip trip) { //admin
        Trip existingTrip = tripRepository.findByTripNo(trip.getTripNo());
        if (existingTrip == null) {
            throw new IllegalArgumentException("Trip not found");
        }
        tripRepository.updateTrip(trip); 
    }

}