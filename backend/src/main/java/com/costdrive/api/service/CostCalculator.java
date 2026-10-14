package com.costdrive.api.service;

import com.costdrive.api.dto.CostBreakdownResponse;
import com.costdrive.api.model.Car;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class CostCalculator {
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final MathContext EXCEL_LIKE_PRECISION = MathContext.DECIMAL128;

    private CostCalculator() {
    }

    public static CostBreakdownResponse calculate(Car car) {
        BigDecimal initial = money(car.getPurchasePrice().add(car.getInitialCosts(), EXCEL_LIKE_PRECISION));
        BigDecimal monthlyIpvaExact = car.getPurchasePrice().multiply(car.getIpvaRatePercent(), EXCEL_LIKE_PRECISION)
                .divide(ONE_HUNDRED, EXCEL_LIKE_PRECISION).divide(TWELVE, EXCEL_LIKE_PRECISION);
        BigDecimal monthlyInsuranceExact = car.getAnnualInsurance().divide(TWELVE, EXCEL_LIKE_PRECISION);
        BigDecimal monthlyMaintenanceExact = car.getAnnualMaintenance().divide(TWELVE, EXCEL_LIKE_PRECISION);
        BigDecimal monthlyEnergyExact = car.getConsumptionPer100().divide(ONE_HUNDRED, EXCEL_LIKE_PRECISION)
                .multiply(car.getEnergyUnitPrice(), EXCEL_LIKE_PRECISION).multiply(car.getMonthlyKm(), EXCEL_LIKE_PRECISION);
        BigDecimal monthlyExact = monthlyIpvaExact.add(monthlyInsuranceExact, EXCEL_LIKE_PRECISION)
                .add(monthlyMaintenanceExact, EXCEL_LIKE_PRECISION).add(monthlyEnergyExact, EXCEL_LIKE_PRECISION)
                .add(car.getMonthlyParking(), EXCEL_LIKE_PRECISION).add(car.getMonthlyOtherCosts(), EXCEL_LIKE_PRECISION);
        BigDecimal annualExact = monthlyExact.multiply(TWELVE, EXCEL_LIKE_PRECISION);

        return new CostBreakdownResponse(initial, money(monthlyIpvaExact), money(monthlyInsuranceExact), money(monthlyMaintenanceExact),
                money(monthlyEnergyExact), money(monthlyExact), money(annualExact));
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
