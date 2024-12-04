package com.apartmentreservation.Booking_microservice.service;

import com.apartmentreservation.Booking_microservice.entity.Booking;
import com.apartmentreservation.Booking_microservice.exception.BookingException;
import com.apartmentreservation.Booking_microservice.repo.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    @Value("${api.gateway.url}")
    private String apiGatewayUrl;
    @Autowired
    private final BookingRepository bookingRepository;
    @Autowired
    private final RestTemplate restTemplate;

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Booking findById(String id) {
        return bookingRepository.findById(id).orElse(null);
    }

    public List<Booking> getBookingsForApartment(String apartmentId) {
        return bookingRepository.findByApartmentId(apartmentId);
    }

    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }

    public Booking update(String id, Booking booking) {
        if (bookingRepository.existsById(id)) {
            booking.setId(id);
            return bookingRepository.save(booking);
        }
        return null;
    }

    public void delete(String id) {
        bookingRepository.deleteById(id);
    }

    public Booking bookApartment(Booking booking) {
        ResponseEntity<Void> userResponse = restTemplate.getForEntity(
                apiGatewayUrl + "/api/user/" + booking.getUserId(),
                Void.class
        );
        if (userResponse.getStatusCode() != HttpStatus.OK) {
            throw new BookingException("Пользователь не найден");
        }

        boolean isApartmentAvailable = isApartmentAvailable(booking.getApartmentId(), booking.getStartDate(), booking.getEndDate());
        if (!isApartmentAvailable) {
            throw new BookingException("Апартаменты недоступны для выбранных дат");
        }

        int pricePerNight = getPricePerNight(booking.getApartmentId());
        if (pricePerNight <= 0) {
            throw new BookingException("Неверная цена");
        }

        int totalPrice = calculateTotalPrice(booking.getStartDate(), booking.getEndDate(), pricePerNight);
        booking.setTotalPrice(totalPrice);

        return bookingRepository.save(booking);
    }

    private boolean isApartmentAvailable(String apartmentId, LocalDate startDate, LocalDate endDate) {
        List<Booking> existingBookings = bookingRepository.findBookingsByApartmentIdAndDates(apartmentId, startDate, endDate);
        return existingBookings.isEmpty();
    }

    private int getPricePerNight(String apartmentId) {
        ResponseEntity<Integer> priceResponse = restTemplate.getForEntity(
                apiGatewayUrl + "/api/apartment/" + apartmentId + "/price",
                Integer.class
        );
        if (priceResponse.getStatusCode() == HttpStatus.OK) {
            return priceResponse.getBody();
        }
        return 0;
    }

    private int calculateTotalPrice(LocalDate startDate, LocalDate endDate, int pricePerNight) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            throw new BookingException("Неверные даты");
        }
        return (int) days * pricePerNight;
    }


}

