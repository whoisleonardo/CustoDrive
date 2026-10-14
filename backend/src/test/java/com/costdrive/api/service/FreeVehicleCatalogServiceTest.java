package com.costdrive.api.service;

import com.costdrive.api.dto.CatalogVehicle;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FreeVehicleCatalogServiceTest {
    private final FreeVehicleCatalogService catalog = new FreeVehicleCatalogService(new ObjectMapper());

    @Test
    void findsBrazilianElectricVehicleAndKeepsOfficialConsumptionAndRange() {
        var result = catalog.search("BYD Dolphin Mini", "electric", 2026, 1, 30);
        assertThat(result.total()).isPositive();
        assertThat(result.results()).anySatisfy(vehicle -> {
            assertThat(vehicle.consumptionUnit()).isEqualTo("kWh/100 km");
            assertThat(vehicle.consumptionPer100()).isPositive();
            assertThat(vehicle.rangeKm()).isPositive();
            assertThat(vehicle.sources()).anySatisfy(source -> assertThat(source.name()).contains("Inmetro"));
        });
    }

    @Test
    void flexVehicleOffersGasolineAndEthanolSeparately() {
        var result = catalog.search("Fiat Argo", "combustion", 2026, 1, 100);
        assertThat(result.results()).isNotEmpty();
        CatalogVehicle flex = result.results().stream().filter(v -> v.fuelChoices().size() >= 2).findFirst().orElseThrow();
        assertThat(flex.fuelChoices()).extracting(CatalogVehicle.CatalogFuelChoice::fuelType).contains("Gasolina", "Etanol");
        assertThat(flex.fuelChoices()).allSatisfy(option -> assertThat(option.consumptionPer100()).isPositive());
    }

    @Test
    void plugInKeepsElectricRangeSeparateFromItsFuelChoices() {
        var result = catalog.search("BYD Atto 2", "phev", 2026, 1, 30);
        CatalogVehicle plugIn = result.results().stream().filter(v -> v.powertrain() != null && v.powertrain().contains("Plug-in")).findFirst().orElseThrow();
        assertThat(plugIn.rangeKm()).isPositive();
        assertThat(plugIn.fuelChoices()).extracting(CatalogVehicle.CatalogFuelChoice::fuelType).contains("Gasolina", "Etanol");
        assertThat(plugIn.fuelChoices()).noneMatch(choice -> "Elétrico".equals(choice.fuelType()));
    }

    @Test
    void findsGeelyEx2AcrossLocalSourcesWithoutPretendingTheTrimAndYearAreIdentical() {
        var result = catalog.search("Geely EX2", "all", null, 1, 30);
        assertThat(result.results()).anySatisfy(vehicle -> {
            assertThat(vehicle.source()).contains("Inmetro");
            assertThat(vehicle.trim()).contains("MAX");
            assertThat(vehicle.consumptionPer100()).isPositive();
        });
        assertThat(result.results()).anySatisfy(vehicle -> {
            assertThat(vehicle.source()).contains("Open EV Data");
            assertThat(vehicle.modelYear()).isEqualTo(2025);
            assertThat(vehicle.batteryKwh()).isPositive();
            assertThat(vehicle.lengthMm()).isPositive();
            assertThat(vehicle.rangeKm()).isPositive();
        });
    }

    @Test
    void findsBydKingWithHybridFilterAndNotAsCombustionOnly() {
        assertThat(catalog.search("BYD King", "all", null, 1, 30).total()).isPositive();
        var hybrid = catalog.search("BYD King", "hybrid", 2026, 1, 30);
        assertThat(hybrid.total()).isPositive();
        assertThat(hybrid.results()).allSatisfy(vehicle -> assertThat(vehicle.powertrain()).contains("Plug-in"));
        assertThat(catalog.search("BYD King", "combustion", 2026, 1, 30).total()).isZero();
    }

    @Test
    void rejectsQueriesShorterThanTwoCharacters() {
        assertThrows(ResponseStatusException.class, () -> catalog.search("A", "all", null, 1, 30));
    }
}
