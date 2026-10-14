package com.costdrive.api.dto;

import com.costdrive.api.model.Car;
import java.math.BigDecimal;

public record CarResponse(
        Long id,
        String name,
        String brand,
        String model,
        String version,
        Integer modelYear,
        String vin,
        BigDecimal purchasePrice,
        BigDecimal initialCosts,
        BigDecimal ipvaRatePercent,
        BigDecimal annualInsurance,
        BigDecimal annualMaintenance,
        BigDecimal consumptionPer100,
        BigDecimal energyUnitPrice,
        BigDecimal monthlyKm,
        BigDecimal monthlyParking,
        BigDecimal monthlyOtherCosts,
        String bodyClass,
        String fuelType,
        String driveType,
        String transmission,
        Integer doors,
        Integer seats,
        BigDecimal lengthMm,
        BigDecimal widthMm,
        BigDecimal heightMm,
        BigDecimal wheelbaseMm,
        BigDecimal curbWeightKg,
        BigDecimal batteryKwh,
        String electrificationLevel,
        String specsSource,
        CostBreakdownResponse costs
) {
    public static CarResponse from(Car car, CostBreakdownResponse costs) {
        return new CarResponse(car.getId(), car.getName(), car.getBrand(), car.getModel(), car.getVersion(), car.getModelYear(), car.getVin(),
                car.getPurchasePrice(), car.getInitialCosts(), car.getIpvaRatePercent(), car.getAnnualInsurance(), car.getAnnualMaintenance(),
                car.getConsumptionPer100(), car.getEnergyUnitPrice(), car.getMonthlyKm(), car.getMonthlyParking(), car.getMonthlyOtherCosts(),
                car.getBodyClass(), car.getFuelType(), car.getDriveType(), car.getTransmission(), car.getDoors(), car.getSeats(), car.getLengthMm(),
                car.getWidthMm(), car.getHeightMm(), car.getWheelbaseMm(), car.getCurbWeightKg(), car.getBatteryKwh(), car.getElectrificationLevel(),
                car.getSpecsSource(), costs);
    }
}
