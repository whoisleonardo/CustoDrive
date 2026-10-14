package com.costdrive.api.dto;

import java.math.BigDecimal;

public record CostBreakdownResponse(
        BigDecimal initialDisbursement,
        BigDecimal monthlyIpva,
        BigDecimal monthlyInsurance,
        BigDecimal monthlyMaintenance,
        BigDecimal monthlyEnergy,
        BigDecimal monthlyCost,
        BigDecimal annualRecurringCost
) {
}
