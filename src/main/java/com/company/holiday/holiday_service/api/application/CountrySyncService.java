package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.application.dto.CountryUpsertCommand;
import com.company.holiday.holiday_service.api.application.mapper.HolidayDomainMapper;
import com.company.holiday.holiday_service.api.infra.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountrySyncService {

    private final CountryRepository countryRepository;
    private final HolidayDomainMapper domainMapper;

    @Transactional
    public void upsertCountries(List<CountryUpsertCommand> commands) {
        for (CountryUpsertCommand command : commands) {
            countryRepository.findByCode(command.code())
                    .orElseGet(() -> countryRepository.save(domainMapper.toCountry(command)));
        }
    }

}
