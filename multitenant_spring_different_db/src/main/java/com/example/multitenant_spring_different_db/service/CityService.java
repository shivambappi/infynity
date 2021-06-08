package com.example.multitenant_spring_different_db.service;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.multitenant_spring_different_db.entity.City;
import com.example.multitenant_spring_different_db.repository.CityRepository;

@Service
public class CityService {

    @Autowired
    private CityRepository cityRepository;

    //@Autowired
    //private TenantDataSource tenantDataSource;

    public void save(City city){
        cityRepository.save(city);
    }

    public List<City> getAll() throws SQLException {
        return cityRepository.findAll();
        /*
        JdbcTemplate jdbcTemplate = new JdbcTemplate(tenantDataSource.getDataSource(TenantContext.getCurrentTenant()));
        String sql = "SELECT * FROM city";
        List<City> cities = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper(City.class));
        return cities;
        */

    }

    public City get(Long id){
        return cityRepository.findById(id).get();
    }

    public City getByName(String name){
        return cityRepository.findByName(name);
    }

    public void delete(String name){
        cityRepository.deleteByName(name);
    }
}
