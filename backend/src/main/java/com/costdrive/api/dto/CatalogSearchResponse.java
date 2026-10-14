package com.costdrive.api.dto;

import java.util.List;

public record CatalogSearchResponse(List<CatalogVehicle> results, int page, int pageSize, int total) { }
