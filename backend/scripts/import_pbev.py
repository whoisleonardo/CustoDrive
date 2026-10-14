#!/usr/bin/env python3
"""Extract the public Inmetro PBE Veicular landscape table into a local JSON snapshot.

Usage: python3 import_pbev.py input.pdf output.json
Requires pdfplumber. This is a one-time/update-time importer, not a runtime dependency.
"""
from __future__ import annotations

import json
import re
import sys
import unicodedata
from datetime import date
from pathlib import Path
from typing import Any

import pdfplumber

SOURCE_URL = (
    "https://www.gov.br/inmetro/pt-br/assuntos/regulamentacao/"
    "avaliacao-da-conformidade/programa-brasileiro-de-etiquetagem/"
    "tabelas-de-eficiencia-energetica/veiculos-automotivos-pbe-veicular"
)
YEAR = 2026


def clean(value: str) -> str:
    return re.sub(r"\s+", " ", value).strip()


def normalized(value: str) -> str:
    return "".join(
        c for c in unicodedata.normalize("NFKD", value.casefold())
        if not unicodedata.combining(c)
    )


def cell(words: list[dict[str, Any]], left: float, right: float) -> str:
    selected = sorted(
        (w for w in words if left <= w["x0"] < right),
        key=lambda w: w["x0"],
    )
    return clean(" ".join(w["text"] for w in selected))


def number(value: str) -> float | None:
    value = clean(value).replace(",", ".")
    if not value or value in {"\\", "-", "ND", "N.A.", "#####"}:
        return None
    match = re.search(r"\d+(?:\.\d+)?", value)
    return float(match.group(0)) if match else None


def append_fuel_choice(
    choices: list[dict[str, Any]], label: str, fuel_type: str,
    km_per_litre: float | None,
) -> None:
    if km_per_litre is None or km_per_litre <= 0:
        return
    choices.append({
        "label": label,
        "fuelType": fuel_type,
        "consumptionPer100": round(100 / km_per_litre, 3),
        "unit": "L/100 km",
        "sourceValue": round(km_per_litre, 2),
        "sourceUnit": "km/L (ciclo combinado PBEV)",
    })


def extract(pdf_path: Path) -> list[dict[str, Any]]:
    result: list[dict[str, Any]] = []
    with pdfplumber.open(pdf_path) as pdf:
        for page_number, page in enumerate(pdf.pages, start=1):
            words = page.extract_words(x_tolerance=1, y_tolerance=1)
            anchors = sorted(
                (w for w in words if 210 <= w["x0"] < 242 and 120 < w["top"] < 550
                 and normalized(w["text"]) not in {"categoria", "marca", "modelo"}),
                key=lambda w: w["top"],
            )
            y_rows: list[float] = []
            for anchor in anchors:
                if not y_rows or anchor["top"] - y_rows[-1] > 1.2:
                    y_rows.append(anchor["top"])

            for y in y_rows:
                row = [w for w in words if abs(w["top"] - y) <= 1.15]
                category = cell(row, 170, 210)
                make = cell(row, 210, 242)
                model = cell(row, 242, 279)
                trim = cell(row, 279, 301)
                engine = cell(row, 300, 314)
                propulsion = cell(row, 314, 334)
                transmission = cell(row, 334, 349)
                fuel_code = cell(row, 379, 394).upper()

                if not make or not model or normalized(make) == "marca" or normalized(model) == "modelo":
                    continue
                if len(make) > 40 or len(model) > 70:
                    continue

                ethanol_kml = number(cell(row, 497, 509))
                gasoline_kml = number(cell(row, 531, 547))
                electric_mj_km = number(cell(row, 590, 608))
                electric_range_km = number(cell(row, 608, 628))
                choices: list[dict[str, Any]] = []

                power = normalized(propulsion)
                code = normalized(fuel_code)
                is_electric = "eletrico" in power
                if is_electric:
                    if electric_mj_km and electric_mj_km > 0:
                        choices.append({
                            "label": "Elétrico",
                            "fuelType": "Elétrico",
                            "consumptionPer100": round(electric_mj_km * 100 / 3.6, 3),
                            "unit": "kWh/100 km",
                            "sourceValue": round(electric_mj_km, 3),
                            "sourceUnit": "MJ/km (PBEV)",
                        })
                elif code in {"f", "flex"}:
                    append_fuel_choice(choices, "Gasolina", "Gasolina", gasoline_kml)
                    append_fuel_choice(choices, "Etanol", "Etanol", ethanol_kml)
                elif code in {"e", "e100", "etanol"}:
                    append_fuel_choice(choices, "Etanol", "Etanol", ethanol_kml)
                elif code in {"g", "d", "diesel", "gasolina"}:
                    label = "Diesel" if code in {"d", "diesel"} else "Gasolina"
                    append_fuel_choice(choices, label, label, gasoline_kml)
                else:
                    append_fuel_choice(choices, "Gasolina", "Gasolina", gasoline_kml)
                    append_fuel_choice(choices, "Etanol", "Etanol", ethanol_kml)

                item = {
                    "id": f"pbev-{YEAR}-p{page_number}-{len(result) + 1}",
                    "year": YEAR,
                    "category": category,
                    "make": make,
                    "model": model,
                    "trim": trim,
                    "engine": engine,
                    "powertrain": propulsion,
                    "fuelCode": fuel_code,
                    "fuelChoices": choices,
                    "energyConsumptionMjKm": electric_mj_km,
                    "electricRangeKm": electric_range_km,
                    "transmission": transmission,
                    "source": "Inmetro PBE Veicular 2026",
                    "sourceUrl": SOURCE_URL,
                    "sourcePage": page_number,
                }
                result.append(item)
    return result


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("Uso: import_pbev.py tabela-pbev.pdf saida.json")
    source, destination = map(Path, sys.argv[1:])
    vehicles = extract(source)
    payload = {
        "source": "Inmetro — Programa Brasileiro de Etiquetagem Veicular",
        "referenceYear": YEAR,
        "sourceUrl": SOURCE_URL,
        "sourceUpdatedAt": "2026-08-14",
        "importedAt": date.today().isoformat(),
        "vehicleCount": len(vehicles),
        "vehicles": vehicles,
    }
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(payload, ensure_ascii=False, separators=(",", ":")) + "\n", encoding="utf-8")
    print(f"{len(vehicles)} registros escritos em {destination}")
    print("Amostra:")
    print(json.dumps(vehicles[:3], ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
