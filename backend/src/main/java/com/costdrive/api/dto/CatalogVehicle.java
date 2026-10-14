package com.costdrive.api.dto;

import java.util.List;

public record CatalogVehicle(
        String id,
        String make,
        String model,
        String trim,
        Integer modelYear,
        String category,
        String engine,
        String powertrain,
        String fuelType,
        String transmission,
        String driveType,
        Integer doors,
        Integer seats,
        Double lengthMm,
        Double widthMm,
        Double heightMm,
        Double wheelbaseMm,
        Double curbWeightKg,
        Double batteryKwh,
        Double consumptionPer100,
        String consumptionUnit,
        Double energyConsumptionMjKm,
        Double rangeKm,
        String rangeCycle,
        List<CatalogFuelChoice> fuelChoices,
        String source,
        String sourceVersion,
        List<CatalogSource> sources
) {
    public record CatalogFuelChoice(
            String label,
            String fuelType,
            Double consumptionPer100,
            String unit,
            Double sourceValue,
            String sourceUnit
    ) { }

    public record CatalogSource(String name, String version, String url, String updatedAt) { }
}
