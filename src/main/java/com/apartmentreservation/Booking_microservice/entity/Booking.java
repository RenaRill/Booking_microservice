package com.apartmentreservation.Booking_microservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Builder
@Getter
@Setter
@Document(value = "booking")
public class Booking {
    @Id
    private String id;
    private String apartmentId;
    private String userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalPrice;
}
