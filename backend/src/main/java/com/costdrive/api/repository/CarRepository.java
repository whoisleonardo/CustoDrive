package com.costdrive.api.repository;

import com.costdrive.api.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findAllByOwnerIdOrderByNameAsc(Long ownerId);
    Optional<Car> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    @Modifying
    @Query(value = "UPDATE cars SET owner_id = :ownerId WHERE owner_id IS NULL", nativeQuery = true)
    int assignUnownedCarsTo(@Param("ownerId") Long ownerId);
}
