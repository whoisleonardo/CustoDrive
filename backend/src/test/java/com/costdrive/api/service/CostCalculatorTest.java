package com.costdrive.api.service;

import com.costdrive.api.dto.CostBreakdownResponse;
import com.costdrive.api.model.Car;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CostCalculatorTest {
    @Test
    void reproduzOsValoresDoExemploDaPlanilha() {
        Car car = new Car();
        car.setPurchasePrice(new BigDecimal("136800"));
        car.setInitialCosts(new BigDecimal("2500"));
        car.setIpvaRatePercent(new BigDecimal("3.5"));
        car.setAnnualInsurance(new BigDecimal("4500"));
        car.setAnnualMaintenance(new BigDecimal("1200"));
        car.setConsumptionPer100(new BigDecimal("15"));
        car.setEnergyUnitPrice(new BigDecimal("0.95"));
        car.setMonthlyKm(new BigDecimal("720"));
        car.setMonthlyParking(new BigDecimal("130"));
        car.setMonthlyOtherCosts(BigDecimal.ZERO);

        CostBreakdownResponse costs = CostCalculator.calculate(car);

        assertThat(costs.initialDisbursement()).isEqualByComparingTo("139300.00");
        assertThat(costs.monthlyIpva()).isEqualByComparingTo("399.00");
        assertThat(costs.monthlyInsurance()).isEqualByComparingTo("375.00");
        assertThat(costs.monthlyMaintenance()).isEqualByComparingTo("100.00");
        assertThat(costs.monthlyEnergy()).isEqualByComparingTo("102.60");
        assertThat(costs.monthlyCost()).isEqualByComparingTo("1106.60");
        assertThat(costs.annualRecurringCost()).isEqualByComparingTo("13279.20");
    }

    @Test
    void somaComPrecisaoInternaAntesDeArredondar() {
        Car car = new Car();
        car.setPurchasePrice(new BigDecimal("100"));
        car.setInitialCosts(BigDecimal.ZERO);
        car.setIpvaRatePercent(new BigDecimal("1"));
        car.setAnnualInsurance(BigDecimal.ONE);
        car.setAnnualMaintenance(BigDecimal.ONE);
        car.setConsumptionPer100(BigDecimal.ZERO);
        car.setEnergyUnitPrice(BigDecimal.ZERO);
        car.setMonthlyKm(BigDecimal.ZERO);
        car.setMonthlyParking(BigDecimal.ZERO);
        car.setMonthlyOtherCosts(BigDecimal.ZERO);

        assertThat(CostCalculator.calculate(car).monthlyCost()).isEqualByComparingTo("0.25");
    }
}
