package com.example.multitenant_spring_different_db.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multitenant_spring_different_db.entity.City;


public interface CityRepository extends JpaRepository<City,Long> {

    Optional<City> findById(Long id);

    City findByName(String name);

    void deleteByName(String name);
}
