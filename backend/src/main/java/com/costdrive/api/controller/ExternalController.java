package com.costdrive.api.controller;

import com.costdrive.api.dto.CatalogVehicle;
import com.costdrive.api.service.FuelEconomyService;
import com.costdrive.api.service.NhtsaVinService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/external")
public class ExternalController {
    private final FuelEconomyService fuelEconomy;
    private final NhtsaVinService nhtsa;
    public ExternalController(FuelEconomyService fuelEconomy, NhtsaVinService nhtsa) {
        this.fuelEconomy = fuelEconomy; this.nhtsa = nhtsa;
    }

    @GetMapping("/fueleconomy/options")
    public List<FuelEconomyService.Option> options(@RequestParam int year, @RequestParam String make, @RequestParam String model) {
        validate(year, make, model);
        return fuelEconomy.options(year, make.trim(), model.trim());
    }

    @GetMapping("/fueleconomy/vehicle/{id}")
    public CatalogVehicle vehicle(@PathVariable String id, @RequestParam(required = false) String label) {
        return fuelEconomy.vehicle(id, label);
    }

    @GetMapping("/nhtsa/vin/{vin}")
    public CatalogVehicle decodeVin(@PathVariable String vin, @RequestParam(required = false) Integer year) {
        if (year != null && (year < 1980 || year > 2100)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ano do modelo inválido.");
        return nhtsa.decode(vin, year);
    }

    private void validate(int year, String make, String model) {
        if (year < 1984 || year > 2100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ano fora do intervalo aceito pela fonte EPA.");
        if (make == null || make.isBlank() || make.length() > 80 || model == null || model.isBlank() || model.length() > 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe marca e modelo para pesquisar na fonte EPA.");
    }
}
