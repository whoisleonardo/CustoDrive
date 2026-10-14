package com.costdrive.api.service;

import com.costdrive.api.dto.CarRequest;
import com.costdrive.api.dto.CarResponse;
import com.costdrive.api.model.Car;
import com.costdrive.api.model.UserAccount;
import com.costdrive.api.repository.CarRepository;
import com.costdrive.api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CarService {
    private final CarRepository repository;
    private final UserRepository users;

    public CarService(CarRepository repository, UserRepository users) {
        this.repository = repository;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<CarResponse> list() {
        UserAccount owner = currentUser();
        return repository.findAllByOwnerIdOrderByNameAsc(owner.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CarResponse get(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public CarResponse create(CarRequest request) {
        Car car = new Car();
        car.setOwner(currentUser());
        apply(car, request);
        OffsetDateTime now = OffsetDateTime.now();
        car.setCreatedAt(now);
        car.setUpdatedAt(now);
        return toResponse(repository.save(car));
    }

    @Transactional
    public CarResponse update(Long id, CarRequest request) {
        Car car = find(id);
        apply(car, request);
        car.setUpdatedAt(OffsetDateTime.now());
        return toResponse(repository.save(car));
    }

    @Transactional
    public void delete(Long id) {
        Car car = find(id);
        repository.delete(car);
    }

    private Car find(Long id) {
        UserAccount owner = currentUser();
        return repository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new EntityNotFoundException("Carro não encontrado"));
    }

    private UserAccount currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return users.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }

    private CarResponse toResponse(Car car) {
        return CarResponse.from(car, CostCalculator.calculate(car));
    }

    private void apply(Car car, CarRequest r) {
        car.setName(r.name().trim());
        car.setBrand(clean(r.brand()));
        car.setModel(clean(r.model()));
        car.setVersion(clean(r.version()));
        car.setModelYear(r.modelYear());
        car.setVin(clean(r.vin()));
        car.setPurchasePrice(r.purchasePrice());
        car.setInitialCosts(r.initialCosts());
        car.setIpvaRatePercent(r.ipvaRatePercent());
        car.setAnnualInsurance(r.annualInsurance());
        car.setAnnualMaintenance(r.annualMaintenance());
        car.setConsumptionPer100(r.consumptionPer100());
        car.setEnergyUnitPrice(r.energyUnitPrice());
        car.setMonthlyKm(r.monthlyKm());
        car.setMonthlyParking(r.monthlyParking());
        car.setMonthlyOtherCosts(r.monthlyOtherCosts());
        car.setBodyClass(clean(r.bodyClass()));
        car.setFuelType(clean(r.fuelType()));
        car.setDriveType(clean(r.driveType()));
        car.setTransmission(clean(r.transmission()));
        car.setDoors(r.doors());
        car.setSeats(r.seats());
        car.setLengthMm(r.lengthMm());
        car.setWidthMm(r.widthMm());
        car.setHeightMm(r.heightMm());
        car.setWheelbaseMm(r.wheelbaseMm());
        car.setCurbWeightKg(r.curbWeightKg());
        car.setBatteryKwh(r.batteryKwh());
        car.setElectrificationLevel(clean(r.electrificationLevel()));
        car.setSpecsSource(clean(r.specsSource()));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
