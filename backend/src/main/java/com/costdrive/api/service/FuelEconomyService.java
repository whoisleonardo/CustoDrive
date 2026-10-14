package com.costdrive.api.service;

import com.costdrive.api.dto.CatalogVehicle;
import com.costdrive.api.dto.CatalogVehicle.CatalogFuelChoice;
import com.costdrive.api.dto.CatalogVehicle.CatalogSource;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class FuelEconomyService {
    private static final String BASE = "https://www.fueleconomy.gov/ws/rest/vehicle";
    private static final String DOCS = "https://www.fueleconomy.gov/feg/ws/index.shtml";
    private final RestClient client;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public FuelEconomyService(RestClient.Builder builder) { this.client = builder.baseUrl(BASE).build(); }

    public List<Option> options(int year, String make, String model) {
        String key = "options:" + year + ":" + make.toLowerCase(Locale.ROOT) + ":" + model.toLowerCase(Locale.ROOT);
        JsonNode root = getCached(key, () -> client.get().uri(b -> b.path("/menu/options").queryParam("year", year)
                        .queryParam("make", make).queryParam("model", model).build())
                .accept(MediaType.APPLICATION_JSON).retrieve().body(JsonNode.class));
        JsonNode items = root == null ? null : root.path("menuItem");
        if (items == null || items.isMissingNode() || items.isNull()) return List.of();
        List<Option> result = new ArrayList<>();
        if (items.isObject()) result.add(new Option(items.path("value").asText(), items.path("text").asText()));
        else if (items.isArray()) items.forEach(item -> result.add(new Option(item.path("value").asText(), item.path("text").asText())));
        return result;
    }

    public CatalogVehicle vehicle(String id, String optionLabel) {
        if (id == null || !id.matches("[0-9]{1,8}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identificador EPA inválido.");
        JsonNode n = getCached("vehicle:" + id, () -> client.get().uri("/{id}", id)
                .accept(MediaType.APPLICATION_JSON).retrieve().body(JsonNode.class));
        if (n == null || n.path("id").isMissingNode()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "A EPA não retornou essa versão.");

        String fuel1 = n.path("fuelType1").asText("");
        String fuel2 = n.path("fuelType2").asText("");
        String vehicleType = n.path("atvType").asText("");
        double mpg = positive(n.path("comb08").asText(""));
        double electricKwhPer100Miles = positive(n.path("combE").asText(""));
        List<CatalogFuelChoice> choices = new ArrayList<>();
        if (mpg > 0 && !fuel1.toLowerCase(Locale.ROOT).contains("electric")) {
            choices.add(new CatalogFuelChoice(fuel1 + " (ciclo combinado EPA)", fuel1, 235.215 / mpg,
                    "L/100 km", mpg, "mpg combinado EPA"));
        }
        if (electricKwhPer100Miles > 0) {
            choices.add(new CatalogFuelChoice("Eletricidade (EPA)", "Eletricidade", electricKwhPer100Miles * 0.621371,
                    "kWh/100 km", electricKwhPer100Miles, "kWh/100 mi EPA"));
        }
        boolean electric = electricKwhPer100Miles > 0 && (vehicleType.equalsIgnoreCase("EV") || fuel1.toLowerCase(Locale.ROOT).contains("electric"));
        boolean plugIn = !fuel2.isBlank() && fuel2.toLowerCase(Locale.ROOT).contains("electric") || vehicleType.toLowerCase(Locale.ROOT).contains("plug-in");
        String fuelType = electric ? "Elétrico (EPA)" : plugIn ? "Híbrido plug-in (EPA)" : vehicleType.toLowerCase(Locale.ROOT).contains("hybrid") ? "Híbrido (EPA)" : fuel1;
        CatalogFuelChoice primary = choices.isEmpty() ? null : choices.get(0);
        String model = n.path("model").asText("");
        String trim = optionLabel == null || optionLabel.isBlank() ? n.path("trany").asText("") : optionLabel;
        double miles = positive(n.path("rangeA").asText(""));
        if (miles == 0) miles = positive(n.path("range").asText(""));
        double battery = positive(n.path("battery").asText(""));
        CatalogSource source = new CatalogSource("FuelEconomy.gov / U.S. EPA", "consulta ao vivo", DOCS, n.path("modifiedOn").asText(null));
        return new CatalogVehicle("fueleconomy-" + id, n.path("make").asText(""), model, trim,
                asInt(n.path("year").asText("")), n.path("VClass").asText(null), n.path("eng_dscr").asText(null),
                plugIn ? "Plug-in Hybrid" : electric ? "BEV" : vehicleType, fuelType, n.path("trany").asText(null),
                n.path("drive").asText(null), null, null, null, null, null, null, null, battery > 0 ? battery : null,
                primary == null ? null : primary.consumptionPer100(), primary == null ? null : primary.unit(), null,
                miles > 0 ? miles * 1.60934 : null, miles > 0 ? "EPA" : null, choices,
                "FuelEconomy.gov (EPA, EUA)", "consulta ao vivo", List.of(source));
    }

    private JsonNode getCached(String key, Supplier<JsonNode> supplier) {
        CacheEntry hit = cache.get(key);
        if (hit != null && hit.expiresAt().isAfter(Instant.now())) return hit.value();
        try {
            JsonNode value = supplier.get();
            if (value != null) cache.put(key, new CacheEntry(value, Instant.now().plus(Duration.ofHours(12))));
            if (cache.size() > 1500) {
                cache.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(Instant.now()));
                if (cache.size() > 1500) cache.clear();
            }
            return value;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "FuelEconomy.gov está temporariamente indisponível. Tente novamente mais tarde.", ex);
        }
    }

    private double positive(String s) {
        try { double d=Double.parseDouble(s); return d > 0 ? d : 0; } catch (Exception e) { return 0; }
    }
    private Integer asInt(String s) { try { return Integer.valueOf(s); } catch (Exception e) { return null; } }
    private record CacheEntry(JsonNode value, Instant expiresAt) { }
    public record Option(String id, String label) { }
}
