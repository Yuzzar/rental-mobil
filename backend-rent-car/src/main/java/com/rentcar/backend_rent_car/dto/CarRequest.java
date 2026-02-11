package com.rentcar.backend_rent_car.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarRequest {

    @NotBlank(message = "Brand wajib diisi")
    private String brand;

    @NotBlank(message = "Model wajib diisi")
    private String model;

    @NotNull(message = "Tahun wajib diisi")
    private Integer year;

    @NotBlank(message = "Plat nomor wajib diisi")
    private String licensePlate;

    private String color;

    @NotNull(message = "Harga per hari wajib diisi")
    @Positive(message = "Harga harus lebih dari 0")
    private BigDecimal dailyRate;

    private String status;

    private String imageUrl;
}
