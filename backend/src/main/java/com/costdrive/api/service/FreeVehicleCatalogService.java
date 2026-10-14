package com.costdrive.api.service;

import com.costdrive.api.dto.CatalogSearchResponse;
import com.costdrive.api.dto.CatalogVehicle;
import com.costdrive.api.dto.CatalogVehicle.CatalogFuelChoice;
import com.costdrive.api.dto.CatalogVehicle.CatalogSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FreeVehicleCatalogService {
    private static final String PBEV_URL = "https://www.gov.br/inmetro/pt-br/assuntos/regulamentacao/avaliacao-da-conformidade/programa-brasileiro-de-etiquetagem/tabelas-de-eficiencia-energetica/veiculos-automotivos-pbe-veicular";
    private static final String OPEN_EV_URL = "https://github.com/open-ev-data/open-ev-data-dataset";
    private final List<CatalogVehicle> vehicles;

    public FreeVehicleCatalogService(ObjectMapper mapper) {
        try {
            JsonNode pbevRoot = read(mapper, "data/pbev-2026.json");
            JsonNode evRoot = read(mapper, "data/open-ev-data-br-1.24.0.json");
            List<CatalogVehicle> pbev = new ArrayList<>();
            pbevRoot.path("vehicles").forEach(n -> pbev.add(fromPbev(n)));
            List<CatalogVehicle> openEv = new ArrayList<>();
            evRoot.path("vehicles").forEach(n -> openEv.add(fromOpenEv(n, evRoot.path("generated_at").asText("2025-12-30"))));

            Map<String, List<CatalogVehicle>> evByKey = openEv.stream().collect(Collectors.groupingBy(this::key));
            Set<String> consumed = new HashSet<>();
            List<CatalogVehicle> merged = new ArrayList<>();
            for (CatalogVehicle brazil : pbev) {
                List<CatalogVehicle> matches = evByKey.getOrDefault(key(brazil), List.of());
                CatalogVehicle match = matches.stream().filter(e -> !consumed.contains(e.id())).filter(e -> Objects.equals(e.modelYear(), brazil.modelYear())).findFirst().orElse(null);
                if (matches.size() == 1 && match != null) {
                    consumed.add(match.id());
                    merged.add(merge(brazil, match));
                } else {
                    merged.add(brazil);
                }
            }
            openEv.stream().filter(e -> !consumed.contains(e.id())).forEach(merged::add);
            this.vehicles = List.copyOf(merged);
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível carregar os snapshots locais do catálogo de veículos.", ex);
        }
    }

    public CatalogSearchResponse search(String query, String type, Integer year, int page, int pageSize) {
        if (query == null || query.trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe ao menos dois caracteres para pesquisar o catálogo local.");
        }
        String q = normalize(query);
        List<CatalogVehicle> filtered = vehicles.stream()
                .filter(v -> Arrays.stream(q.split(" ")).filter(s -> !s.isBlank())
                        .allMatch(token -> normalize(String.join(" ", safe(v.make()), safe(v.model()), safe(v.trim()), safe(v.category()))).contains(token)))
                .filter(v -> year == null || Objects.equals(v.modelYear(), year))
                .filter(v -> matchesType(v, type))
                .sorted(Comparator.comparing(CatalogVehicle::make, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(CatalogVehicle::model, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(CatalogVehicle::trim, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(CatalogVehicle::source, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
        int from = Math.min((page - 1) * pageSize, filtered.size());
        int to = Math.min(from + pageSize, filtered.size());
        return new CatalogSearchResponse(filtered.subList(from, to), page, pageSize, filtered.size());
    }

    public List<String> sourceStatus() {
        return List.of("Inmetro PBE Veicular 2026 (snapshot local)", "Open EV Data v1.24.0 — mercado BR (snapshot local)", "FuelEconomy.gov / NHTSA vPIC: sob demanda");
    }

    private CatalogVehicle fromPbev(JsonNode n) {
        List<CatalogFuelChoice> choices = new ArrayList<>();
        n.path("fuelChoices").forEach(f -> choices.add(new CatalogFuelChoice(
                text(f, "label"), text(f, "fuelType"), decimal(f, "consumptionPer100"), text(f, "unit"),
                decimal(f, "sourceValue"), text(f, "sourceUnit"))));
        String fuelType = choices.size() > 1 ? "Flex (gasolina/etanol)" : choices.isEmpty() ? text(n, "fuelCode") : choices.get(0).fuelType();
        String powertrain = text(n, "powertrain");
        CatalogSource source = new CatalogSource("Inmetro PBE Veicular", "2026", PBEV_URL, text(n, "sourceUpdatedAt"));
        return new CatalogVehicle(text(n, "id"), text(n, "make"), text(n, "model"), text(n, "trim"), integer(n, "year"),
                text(n, "category"), text(n, "engine"), powertrain, fuelType, text(n, "transmission"), null,
                null, null, null, null, null, null, null, null,
                choices.isEmpty() ? null : choices.get(0).consumptionPer100(), choices.isEmpty() ? null : choices.get(0).unit(),
                decimal(n, "energyConsumptionMjKm"), decimal(n, "electricRangeKm"), decimal(n, "electricRangeKm") == null ? null : "PBEV modo elétrico",
                choices, "Inmetro PBE Veicular 2026", "2026", List.of(source));
    }

    private CatalogVehicle fromOpenEv(JsonNode n, String updated) {
        JsonNode dims = n.path("dimensions");
        JsonNode battery = n.path("battery");
        JsonNode body = n.path("body");
        JsonNode rated = n.path("range").path("rated");
        JsonNode selectedRange = null;
        if (rated.isArray()) {
            for (JsonNode item : rated) if (item.path("cycle").asText().equalsIgnoreCase("wltp")) { selectedRange = item; break; }
            if (selectedRange == null && rated.size() > 0) selectedRange = rated.get(0);
        }
        Double net = number(battery.path("pack_capacity_kwh_net"));
        Double gross = number(battery.path("pack_capacity_kwh_gross"));
        String make = n.path("make").path("name").asText("");
        String model = n.path("model").path("name").asText("");
        String trim = n.path("trim").path("name").asText("");
        String id = n.path("unique_code").asText(make + ":" + model + ":" + n.path("year").asText());
        String rangeCycle = selectedRange == null ? null : selectedRange.path("cycle").asText(null);
        CatalogSource source = new CatalogSource("Open EV Data", "1.24.0", OPEN_EV_URL, updated);
        return new CatalogVehicle("open-ev-" + id, make, model, trim, integer(n, "year"), text(body, "style"), null,
                "BEV — 100% elétrico", "Elétrico", text(n.path("powertrain"), "drivetrain"), text(n.path("powertrain"), "drivetrain"),
                integer(body, "doors"), integer(body, "seats"), number(dims.path("length_mm")), number(dims.path("width_mm")),
                number(dims.path("height_mm")), number(dims.path("wheelbase_mm")), number(n.path("weights").path("curb_weight_kg")),
                net != null ? net : gross, null, null, null, selectedRange == null ? null : number(selectedRange.path("range_km")), rangeCycle,
                List.of(), "Open EV Data — mercado BR", "1.24.0", List.of(source));
    }

    private CatalogVehicle merge(CatalogVehicle p, CatalogVehicle e) {
        List<CatalogSource> sources = new ArrayList<>(p.sources()); sources.addAll(e.sources());
        return new CatalogVehicle(p.id(), p.make(), p.model(), p.trim(), p.modelYear(), p.category() != null ? p.category() : e.category(),
                p.engine(), p.powertrain(), p.fuelType(), p.transmission() != null ? p.transmission() : e.transmission(), e.driveType(),
                e.doors(), e.seats(), e.lengthMm(), e.widthMm(), e.heightMm(), e.wheelbaseMm(), e.curbWeightKg(), e.batteryKwh(),
                p.consumptionPer100(), p.consumptionUnit(), p.energyConsumptionMjKm(), p.rangeKm() != null ? p.rangeKm() : e.rangeKm(),
                p.rangeKm() != null ? p.rangeCycle() : e.rangeCycle(), p.fuelChoices(), "Inmetro PBEV 2026 + Open EV Data 1.24.0",
                "PBEV 2026 / Open EV Data 1.24.0", sources);
    }

    private boolean matchesType(CatalogVehicle v, String type) {
        String t = normalize(type == null ? "all" : type);
        String p = normalize(safe(v.powertrain()) + " " + safe(v.fuelType()));
        if (t.isBlank() || t.equals("all") || t.equals("todos")) return true;
        if (t.equals("electric") || t.equals("eletrico")) return p.contains("eletrico") || p.contains("bev");
        if (t.equals("phev") || t.equals("plug in")) return p.contains("plug in") || p.contains("phev");
        boolean plugIn = p.contains("plug in") || p.contains("phev");
        if (t.equals("hybrid") || t.equals("hibrido")) return p.contains("hibrido") || p.contains("hybrid") || plugIn;
        if (t.equals("combustion") || t.equals("combustao")) return !p.contains("eletrico") && !p.contains("hibrido") && !p.contains("hybrid") && !p.contains("bev") && !plugIn;
        return true;
    }

    private String key(CatalogVehicle v) { return normalize(safe(v.make())) + "|" + normalize(safe(v.model())); }
    private String normalize(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }
    private String safe(String s) { return s == null ? "" : s; }
    private String text(JsonNode n, String key) { String s = n.path(key).asText(""); return s.isBlank() || s.equals("null") ? null : s; }
    private Integer integer(JsonNode n, String key) { JsonNode v=n.path(key); if(v.isInt()) return v.asInt(); try{return Integer.valueOf(v.asText());}catch(Exception e){return null;} }
    private Double decimal(JsonNode n, String key) { return number(n.path(key)); }
    private Double number(JsonNode n) { if(n==null || n.isMissingNode() || n.isNull()) return null; if(n.isNumber()) return n.asDouble(); try{return Double.valueOf(n.asText());}catch(Exception e){return null;} }
    private JsonNode read(ObjectMapper mapper, String resource) throws Exception {
        try (InputStream in = new ClassPathResource(resource).getInputStream()) { return mapper.readTree(in); }
    }
}
