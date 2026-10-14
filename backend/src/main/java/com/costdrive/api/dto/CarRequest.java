package com.costdrive.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CarRequest(
        @NotBlank(message = "Informe um nome para o carro") @Size(max = 120, message = "O nome deve ter até 120 caracteres") String name,
        @Size(max = 80) String brand,
        @Size(max = 120) String model,
        @Size(max = 120) String version,
        @Min(value = 1886, message = "Ano do modelo inválido") @Max(value = 2100, message = "Ano do modelo inválido") Integer modelYear,
        @Pattern(regexp = "^$|[A-Za-z0-9]{11,17}$", message = "VIN deve ter de 11 a 17 letras e números") @Size(max = 32) String vin,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 12, fraction = 2) BigDecimal purchasePrice,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 12, fraction = 2) BigDecimal initialCosts,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 4, fraction = 2) BigDecimal ipvaRatePercent,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 12, fraction = 2) BigDecimal annualInsurance,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 12, fraction = 2) BigDecimal annualMaintenance,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 8, fraction = 2) BigDecimal consumptionPer100,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 8, fraction = 2) BigDecimal energyUnitPrice,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 8, fraction = 2) BigDecimal monthlyKm,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 12, fraction = 2) BigDecimal monthlyParking,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 12, fraction = 2) BigDecimal monthlyOtherCosts,
        @Size(max = 100) String bodyClass,
        @Size(max = 100) String fuelType,
        @Size(max = 100) String driveType,
        @Size(max = 100) String transmission,
        @Min(value = 0) @Max(value = 20) Integer doors,
        @Min(value = 0) @Max(value = 100) Integer seats,
        @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 8, fraction = 2) BigDecimal lengthMm,
        @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 8, fraction = 2) BigDecimal widthMm,
        @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 8, fraction = 2) BigDecimal heightMm,
        @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 8, fraction = 2) BigDecimal wheelbaseMm,
        @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 8, fraction = 2) BigDecimal curbWeightKg,
        @DecimalMin(value = "0.00", inclusive = true) @Digits(integer = 8, fraction = 2) BigDecimal batteryKwh,
        @Size(max = 100) String electrificationLevel,
        @Size(max = 80) String specsSource
) {
}
