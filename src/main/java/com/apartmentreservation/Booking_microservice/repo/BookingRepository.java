package com.apartmentreservation.Booking_microservice.repo;

import com.apartmentreservation.Booking_microservice.entity.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByApartmentId(String apartmentId);
    @Query("{ 'apartmentId' : ?0, 'startDate' : { $lt: ?2 }, 'endDate' : { $gt: ?1 } }")
    List<Booking> findBookingsByApartmentIdAndDates(String apartmentId, LocalDate startDate, LocalDate endDate);

}
