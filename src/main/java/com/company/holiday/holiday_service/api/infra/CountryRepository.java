package com.company.holiday.holiday_service.api.infra;

import com.company.holiday.holiday_service.api.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByCode(String code);

}
