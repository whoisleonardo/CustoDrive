package com.costdrive.api.service;

import com.costdrive.api.dto.CatalogVehicle;
import com.costdrive.api.dto.CatalogVehicle.CatalogSource;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NhtsaVinService {
    private static final String BASE = "https://vpic.nhtsa.dot.gov/api/vehicles";
    private static final String DOCS = "https://vpic.nhtsa.dot.gov/api/";
    private final RestClient client;
    private final Map<String, CatalogVehicle> cache = new ConcurrentHashMap<>();

    public NhtsaVinService(RestClient.Builder builder) { client = builder.baseUrl(BASE).build(); }

    public CatalogVehicle decode(String vin, Integer year) {
        if (vin == null || !vin.toUpperCase(Locale.ROOT).matches("[A-HJ-NPR-Z0-9]{17}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um VIN válido com 17 caracteres.");
        String normalizedVin = vin.toUpperCase(Locale.ROOT);
        String cacheKey = normalizedVin + ":" + (year == null ? "" : year);
        CatalogVehicle cached = cache.get(cacheKey);
        if (cached != null) return cached;
        try {
            JsonNode root = client.get().uri(b -> {
                var uri = b.path("/DecodeVinValuesExtended/{vin}").queryParam("format", "json");
                if (year != null) uri.queryParam("modelyear", year);
                return uri.build(normalizedVin);
            }).accept(MediaType.APPLICATION_JSON).retrieve().body(JsonNode.class);
            JsonNode results = root == null ? null : root.path("Results");
            JsonNode row = results != null && results.isArray() && !results.isEmpty() ? results.get(0) : null;
            String make = value(row, "Make");
            String model = value(row, "Model");
            if (make == null && model == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "A NHTSA não encontrou uma identificação para esse VIN.");
            String fuel = value(row, "FuelTypePrimary");
            String electrification = value(row, "ElectrificationLevel");
            String powertrain = electrification != null ? electrification : fuel;
            CatalogSource source = new CatalogSource("NHTSA vPIC — decodificação VIN", "consulta ao vivo", DOCS, null);
            CatalogVehicle result = new CatalogVehicle("nhtsa-" + normalizedVin, make, model, value(row, "Trim"),
                    parseInt(value(row, "ModelYear")), value(row, "BodyClass"), value(row, "EngineModel"), powertrain, fuel,
                    value(row, "TransmissionStyle"), value(row, "DriveType"), parseInt(value(row, "Doors")), parseInt(value(row, "Seats")),
                    inchesToMm(value(row, "VehicleLengthIN")), inchesToMm(value(row, "VehicleWidthIN")), inchesToMm(value(row, "VehicleHeightIN")),
                    inchesToMm(value(row, "WheelBaseIN")), poundsToKg(value(row, "CurbWeightLB")), parseDouble(value(row, "BatteryKWh")),
                    null, null, null, null, null, java.util.List.of(), "NHTSA vPIC (VIN)", "consulta ao vivo", java.util.List.of(source));
            cache.put(cacheKey, result);
            if (cache.size() > 1000) cache.clear();
            return result;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "NHTSA vPIC está temporariamente indisponível. Tente novamente mais tarde.", ex);
        }
    }

    private String value(JsonNode row, String key) {
        if (row == null) return null;
        String value = row.path(key).asText("").trim();
        if (value.isBlank() || value.equalsIgnoreCase("Not Applicable") || value.equalsIgnoreCase("0")) return null;
        return value;
    }
    private Integer parseInt(String s) { try { return s == null ? null : Integer.valueOf(s.replaceAll("[^0-9].*$", "")); } catch (Exception e) { return null; } }
    private Double parseDouble(String s) { try { return s == null ? null : Double.valueOf(s.replace(",", ".").replaceAll("[^0-9.].*$", "")); } catch (Exception e) { return null; } }
    private Double inchesToMm(String s) { Double n=parseDouble(s); return n == null ? null : n * 25.4; }
    private Double poundsToKg(String s) { Double n=parseDouble(s); return n == null ? null : n * 0.453592; }
}
