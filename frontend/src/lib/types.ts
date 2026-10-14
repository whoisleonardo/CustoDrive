export type Costs = {
  initialDisbursement: number
  monthlyIpva: number
  monthlyInsurance: number
  monthlyMaintenance: number
  monthlyEnergy: number
  monthlyCost: number
  annualRecurringCost: number
}

export type Car = {
  id: number
  name: string
  brand?: string | null
  model?: string | null
  version?: string | null
  modelYear?: number | null
  vin?: string | null
  purchasePrice: number
  initialCosts: number
  ipvaRatePercent: number
  annualInsurance: number
  annualMaintenance: number
  consumptionPer100: number
  energyUnitPrice: number
  monthlyKm: number
  monthlyParking: number
  monthlyOtherCosts: number
  bodyClass?: string | null
  fuelType?: string | null
  driveType?: string | null
  transmission?: string | null
  doors?: number | null
  seats?: number | null
  lengthMm?: number | null
  widthMm?: number | null
  heightMm?: number | null
  wheelbaseMm?: number | null
  curbWeightKg?: number | null
  batteryKwh?: number | null
  electrificationLevel?: string | null
  specsSource?: string | null
  costs: Costs
}

export type CarInput = Omit<Car, 'id' | 'costs'>
export type User = { id: number; name: string; email: string }
export type AuthResponse = { token: string; tokenType: 'Bearer'; user: User }

export type CatalogFuelChoice = {
  label: string
  fuelType: string
  consumptionPer100?: number | null
  unit?: string | null
  sourceValue?: number | null
  sourceUnit?: string | null
}
export type CatalogSource = { name: string; version?: string | null; url?: string | null; updatedAt?: string | null }
export type CatalogVehicle = {
  id: string
  make?: string | null
  model?: string | null
  trim?: string | null
  modelYear?: number | null
  category?: string | null
  engine?: string | null
  powertrain?: string | null
  fuelType?: string | null
  transmission?: string | null
  driveType?: string | null
  doors?: number | null
  seats?: number | null
  lengthMm?: number | null
  widthMm?: number | null
  heightMm?: number | null
  wheelbaseMm?: number | null
  curbWeightKg?: number | null
  batteryKwh?: number | null
  consumptionPer100?: number | null
  consumptionUnit?: string | null
  energyConsumptionMjKm?: number | null
  rangeKm?: number | null
  rangeCycle?: string | null
  fuelChoices: CatalogFuelChoice[]
  source: string
  sourceVersion?: string | null
  sources: CatalogSource[]
}
export type CatalogSearchResponse = { results: CatalogVehicle[]; page: number; pageSize: number; total: number }
export type FuelEconomyOption = { id: string; label: string }

export type FormState = {
  name: string
  brand: string
  model: string
  version: string
  modelYear: string
  vin: string
  purchasePrice: string
  initialCosts: string
  ipvaRatePercent: string
  annualInsurance: string
  annualMaintenance: string
  consumptionPer100: string
  energyUnitPrice: string
  monthlyKm: string
  monthlyParking: string
  monthlyOtherCosts: string
  bodyClass: string
  fuelType: string
  driveType: string
  transmission: string
  doors: string
  seats: string
  lengthMm: string
  widthMm: string
  heightMm: string
  wheelbaseMm: string
  curbWeightKg: string
  batteryKwh: string
  electrificationLevel: string
  specsSource: string
}
