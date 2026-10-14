package com.costdrive.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserAccount owner;

    @Column(nullable = false, length = 120)
    private String name;
    @Column(length = 80)
    private String brand;
    @Column(length = 120)
    private String model;
    @Column(length = 120)
    private String version;
    private Integer modelYear;
    @Column(length = 32)
    private String vin;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal purchasePrice;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal initialCosts;
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal ipvaRatePercent;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal annualInsurance;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal annualMaintenance;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal consumptionPer100;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal energyUnitPrice;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyKm;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monthlyParking;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monthlyOtherCosts;

    @Column(length = 100)
    private String bodyClass;
    @Column(length = 100)
    private String fuelType;
    @Column(length = 100)
    private String driveType;
    @Column(length = 100)
    private String transmission;
    private Integer doors;
    private Integer seats;
    @Column(precision = 10, scale = 2)
    private BigDecimal lengthMm;
    @Column(precision = 10, scale = 2)
    private BigDecimal widthMm;
    @Column(precision = 10, scale = 2)
    private BigDecimal heightMm;
    @Column(precision = 10, scale = 2)
    private BigDecimal wheelbaseMm;
    @Column(precision = 10, scale = 2)
    private BigDecimal curbWeightKg;
    @Column(precision = 10, scale = 2)
    private BigDecimal batteryKwh;
    @Column(length = 100)
    private String electrificationLevel;
    @Column(length = 80)
    private String specsSource;

    @Column(nullable = false)
    private OffsetDateTime createdAt;
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public Car() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserAccount getOwner() { return owner; }
    public void setOwner(UserAccount owner) { this.owner = owner; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public BigDecimal getInitialCosts() { return initialCosts; }
    public void setInitialCosts(BigDecimal initialCosts) { this.initialCosts = initialCosts; }
    public BigDecimal getIpvaRatePercent() { return ipvaRatePercent; }
    public void setIpvaRatePercent(BigDecimal ipvaRatePercent) { this.ipvaRatePercent = ipvaRatePercent; }
    public BigDecimal getAnnualInsurance() { return annualInsurance; }
    public void setAnnualInsurance(BigDecimal annualInsurance) { this.annualInsurance = annualInsurance; }
    public BigDecimal getAnnualMaintenance() { return annualMaintenance; }
    public void setAnnualMaintenance(BigDecimal annualMaintenance) { this.annualMaintenance = annualMaintenance; }
    public BigDecimal getConsumptionPer100() { return consumptionPer100; }
    public void setConsumptionPer100(BigDecimal consumptionPer100) { this.consumptionPer100 = consumptionPer100; }
    public BigDecimal getEnergyUnitPrice() { return energyUnitPrice; }
    public void setEnergyUnitPrice(BigDecimal energyUnitPrice) { this.energyUnitPrice = energyUnitPrice; }
    public BigDecimal getMonthlyKm() { return monthlyKm; }
    public void setMonthlyKm(BigDecimal monthlyKm) { this.monthlyKm = monthlyKm; }
    public BigDecimal getMonthlyParking() { return monthlyParking; }
    public void setMonthlyParking(BigDecimal monthlyParking) { this.monthlyParking = monthlyParking; }
    public BigDecimal getMonthlyOtherCosts() { return monthlyOtherCosts; }
    public void setMonthlyOtherCosts(BigDecimal monthlyOtherCosts) { this.monthlyOtherCosts = monthlyOtherCosts; }
    public String getBodyClass() { return bodyClass; }
    public void setBodyClass(String bodyClass) { this.bodyClass = bodyClass; }
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public String getDriveType() { return driveType; }
    public void setDriveType(String driveType) { this.driveType = driveType; }
    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    public Integer getDoors() { return doors; }
    public void setDoors(Integer doors) { this.doors = doors; }
    public Integer getSeats() { return seats; }
    public void setSeats(Integer seats) { this.seats = seats; }
    public BigDecimal getLengthMm() { return lengthMm; }
    public void setLengthMm(BigDecimal lengthMm) { this.lengthMm = lengthMm; }
    public BigDecimal getWidthMm() { return widthMm; }
    public void setWidthMm(BigDecimal widthMm) { this.widthMm = widthMm; }
    public BigDecimal getHeightMm() { return heightMm; }
    public void setHeightMm(BigDecimal heightMm) { this.heightMm = heightMm; }
    public BigDecimal getWheelbaseMm() { return wheelbaseMm; }
    public void setWheelbaseMm(BigDecimal wheelbaseMm) { this.wheelbaseMm = wheelbaseMm; }
    public BigDecimal getCurbWeightKg() { return curbWeightKg; }
    public void setCurbWeightKg(BigDecimal curbWeightKg) { this.curbWeightKg = curbWeightKg; }
    public BigDecimal getBatteryKwh() { return batteryKwh; }
    public void setBatteryKwh(BigDecimal batteryKwh) { this.batteryKwh = batteryKwh; }
    public String getElectrificationLevel() { return electrificationLevel; }
    public void setElectrificationLevel(String electrificationLevel) { this.electrificationLevel = electrificationLevel; }
    public String getSpecsSource() { return specsSource; }
    public void setSpecsSource(String specsSource) { this.specsSource = specsSource; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
