package com.company.holiday.holiday_service.api.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HolidayBatchSchedulerTest {

    @Mock
    private HolidayCommandService holidayCommandService;

    @InjectMocks
    private HolidayBatchScheduler scheduler;

    @DisplayName("스케줄러가 실행되면 배치 동기화가 호출된다")
    @Test
    void syncPreviousAndCurrentYear_callsCommandService() {
        // when
        scheduler.syncPreviousAndCurrentYear();

        // then
        verify(holidayCommandService, times(1)).syncCountriesAndHolidaysForBatch();
    }

}
