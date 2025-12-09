package com.company.holiday.holiday_service.api.infra;

import com.company.holiday.holiday_service.api.domain.Country;
import com.company.holiday.holiday_service.api.domain.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    void deleteByCountryAndDateBetween(Country country, LocalDate start, LocalDate end);

}
