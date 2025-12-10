package com.company.holiday.holiday_service.api.infra;

import com.company.holiday.holiday_service.api.domain.Country;
import com.company.holiday.holiday_service.api.domain.Holiday;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from Holiday h
        where h.country = :country
          and h.date between :start and :end
    """)
    int deleteInRange(@Param("country") Country country,
                      @Param("start") LocalDate start,
                      @Param("end") LocalDate end);

    Page<Holiday> findByCountry_CodeAndDateBetween(String s, LocalDate start, LocalDate end, Pageable pageable);

    Page<Holiday> findByCountry_CodeAndDateBetweenAndTypesRawContaining(
            String countryCode,
            LocalDate start,
            LocalDate end,
            String typeToken,
            Pageable pageable
    );

}
