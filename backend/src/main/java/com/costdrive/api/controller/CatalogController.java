package com.costdrive.api.controller;

import com.costdrive.api.dto.CatalogSearchResponse;
import com.costdrive.api.service.FreeVehicleCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
    private final FreeVehicleCatalogService catalog;
    public CatalogController(FreeVehicleCatalogService catalog) { this.catalog = catalog; }

    @GetMapping("/search")
    public CatalogSearchResponse search(
            @RequestParam String q,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "30") int pageSize) {
        int safePage = Math.max(1, Math.min(page, 10000));
        int safeSize = Math.max(1, Math.min(pageSize, 100));
        return catalog.search(q, type, year, safePage, safeSize);
    }
}
