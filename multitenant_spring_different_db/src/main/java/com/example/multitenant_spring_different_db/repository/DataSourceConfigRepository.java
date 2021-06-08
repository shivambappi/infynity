package com.example.multitenant_spring_different_db.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.multitenant_spring_different_db.config.DataSourceConfig;

public interface DataSourceConfigRepository extends JpaRepository<DataSourceConfig, Long> {
    DataSourceConfig findByName(String name);
}