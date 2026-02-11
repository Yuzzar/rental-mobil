package com.rentcar.backend_rent_car.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RentalRequest {

    @NotNull(message = "Car ID wajib diisi")
    private Long carId;

    @NotNull(message = "Tanggal mulai wajib diisi")
    private LocalDate startDate;

    @NotNull(message = "Tanggal selesai wajib diisi")
    private LocalDate endDate;
}
