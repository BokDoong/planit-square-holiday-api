package com.company.holiday.holiday_service.api.application;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// TODO : 다중 서버 환경에서 ShedLock, 외부 스케쥴러 도입 고
@Component
@RequiredArgsConstructor
public class HolidayBatchScheduler {

    private final HolidayCommandService holidayCommandService;

    @Scheduled(cron = "0 0 1 2 1 *", zone = "Asia/Seoul")
    public void syncPreviousAndCurrentYear() {
        holidayCommandService.syncCountriesAndHolidaysForBatch();
    }

}
