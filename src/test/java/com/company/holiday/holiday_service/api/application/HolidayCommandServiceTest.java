package com.company.holiday.holiday_service.api.application;

import com.company.holiday.holiday_service.api.application.dto.CountryUpsertCommand;
import com.company.holiday.holiday_service.api.application.dto.HolidayUpsertCommand;
import com.company.holiday.holiday_service.api.application.mapper.HolidayCommandMapper;
import com.company.holiday.holiday_service.api.infra.CountryRepository;
import com.company.holiday.holiday_service.api.presentation.dto.response.HolidayRefreshResponse;
import com.company.holiday.holiday_service.api.presentation.dto.response.HolidaySyncResponse;
import com.company.holiday.holiday_service.clients.nager.NagerClient;
import com.company.holiday.holiday_service.clients.nager.dto.NagerAvailableCountryResponse;
import com.company.holiday.holiday_service.clients.nager.dto.NagerPublicHolidayResponse;
import com.company.holiday.holiday_service.global.error.ErrorCode;
import com.company.holiday.holiday_service.global.error.exception.EntityNotFoundException;
import com.company.holiday.holiday_service.global.error.exception.ExternalApiException;
import com.company.holiday.holiday_service.global.error.exception.InvalidValueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HolidayCommandServiceTest {

    @Mock
    CountryRepository countryRepository;

    @Mock
    NagerClient nagerClient;

    @Mock
    HolidayCommandMapper mapper;

    @Mock
    CountrySyncService countrySyncService;

    @Mock
    HolidaySyncService holidaySyncService;

    HolidayCommandService holidayCommandService;

    @BeforeEach
    void setUp() {
        holidayCommandService = new HolidayCommandService(
                countryRepository,
                nagerClient,
                mapper,
                countrySyncService,
                holidaySyncService
        );
    }

    @DisplayName("가용 국가 조회 → 국가 Upsert → 나라별 최근 5년 공휴일 Upsert 순으로 호출되고, 응답에 국가수/공휴일 수를 담아 리턴한다")
    @Test
    void syncCountriesAndHolidays() {
        // given
        // 1) Nager 국가 응답
        var krResponse = new NagerAvailableCountryResponse("KR", "Korea");
        var usResponse = new NagerAvailableCountryResponse("US", "United States");

        given(nagerClient.getAvailableCountries())
                .willReturn(List.of(krResponse, usResponse));

        // 2) Mapper 변환 (외부 DTO → CountryUpsertCommand)
        var krCommand = new CountryUpsertCommand("KR", "Korea");
        var usCommand = new CountryUpsertCommand("US", "United States");

        given(mapper.toCountryCommand(krResponse)).willReturn(krCommand);
        given(mapper.toCountryCommand(usResponse)).willReturn(usCommand);

        // 3) 공휴일 응답 + HolidayUpsertCommand 변환
        var krHolidayDto = mock(NagerPublicHolidayResponse.class);
        var usHolidayDto = mock(NagerPublicHolidayResponse.class);

        var krHolidayCmd = mock(HolidayUpsertCommand.class);
        var usHolidayCmd = mock(HolidayUpsertCommand.class);

        // 각 국가·연도마다 1개 공휴일이 있다고 가정
        given(nagerClient.getPublicHolidays(anyInt(), eq("KR")))
                .willReturn(List.of(krHolidayDto));
        given(nagerClient.getPublicHolidays(anyInt(), eq("US")))
                .willReturn(List.of(usHolidayDto));

        given(mapper.toHolidayCommand(krHolidayDto)).willReturn(krHolidayCmd);
        given(mapper.toHolidayCommand(usHolidayDto)).willReturn(usHolidayCmd);

        // when
        HolidaySyncResponse response = holidayCommandService.syncCountriesAndHolidays();

        // then
        // 1) 국가 upsert 가 한 번 호출되고, 변환된 커맨드 리스트가 그대로 전달된다.
        verify(countrySyncService).upsertCountries(List.of(krCommand, usCommand));

        // 2) 나라별로 최근 5년 공휴일을 한 번에 upsert한다. 국가 2개 → 2번 호출
        verify(holidaySyncService, times(2))
                .upsertRecentFiveYearsHolidays(anyString(), anyList());
        verify(holidaySyncService).upsertRecentFiveYearsHolidays(eq("KR"), anyList());
        verify(holidaySyncService).upsertRecentFiveYearsHolidays(eq("US"), anyList());

        // 3) 응답 값 검증 (국가 수 = 2, 공휴일 수 = 2개 국가 × 5년 × 연도당 1개)
        assertThat(response.countriesCount()).isEqualTo(2);
        assertThat(response.holidaysCount()).isEqualTo(2 * 5 * 1);
    }

    @DisplayName("가용 국가 조회에서 ExternalApiException이 발생하면 그대로 전파하고 이후 동기화는 수행하지 않는다")
    @Test
    void syncCountriesAndHolidays_throwExternalApiException_CountriesFetch() {
        // given
        given(nagerClient.getAvailableCountries())
                .willThrow(new ExternalApiException(ErrorCode.EXTERNAL_API_ERROR));

        // when & then
        assertThatThrownBy(() -> holidayCommandService.syncCountriesAndHolidays())
                .isInstanceOf(ExternalApiException.class);

        verifyNoInteractions(countrySyncService);
        verifyNoInteractions(holidaySyncService);
        verifyNoInteractions(mapper);
    }

    @DisplayName("(date, localName)이 같은 HolidayUpsertCommand는 하나만 남기고 중복을 제거한다")
    @Test
    void syncCountriesAndHolidays_deduplicateByDateAndLocalName() {
        // given
        LocalDate duplicatedDate = LocalDate.of(2022, 1, 1);

        HolidayUpsertCommand first = createHolidayCommand(
                "KR",
                duplicatedDate,
                "새해",
                "New Year (first)"
        );
        HolidayUpsertCommand duplicate = createHolidayCommand(
                "KR",
                duplicatedDate,
                "새해",
                "New Year (duplicate)"
        );
        HolidayUpsertCommand another = createHolidayCommand(
                "KR",
                LocalDate.of(2022, 3, 1),
                "삼일절",
                "Independence Movement Day"
        );

        List<HolidayUpsertCommand> input = List.of(first, duplicate, another);

        // when
        List<HolidayUpsertCommand> result = holidayCommandService.deduplicateByDateAndLocalName(input);

        // then
        assertThat(result.size()).isEqualTo(2);
    }

    @DisplayName("나라, 연도의 재동기화를 진행하고, 응답에 공휴일 수를 담아 리턴한다.")
    @Test
    void refreshHolidays() {
        // given
        int year = 2023;
        String countryCode = "KR";

        // 1) 국가 코드가 DB에 존재
        given(countryRepository.existsByCode(countryCode))
                .willReturn(true);

        // 2) 외부 Nager 응답 DTO들 (서로 다른 두 개라고 가정)
        NagerPublicHolidayResponse dto1 = mock(NagerPublicHolidayResponse.class);
        NagerPublicHolidayResponse dto2 = mock(NagerPublicHolidayResponse.class);

        given(nagerClient.getPublicHolidays(year, countryCode))
                .willReturn(List.of(dto1, dto2));

        // 3) DTO → Command 매핑
        HolidayUpsertCommand cmd1 = createHolidayCommand(
                countryCode,
                LocalDate.of(2023, 1, 1),
                "새해",
                "New Year"
        );
        HolidayUpsertCommand cmd2 = createHolidayCommand(
                countryCode,
                LocalDate.of(2023, 3, 1),
                "삼일절",
                "Independence Movement Day"
        );

        given(mapper.toHolidayCommand(dto1)).willReturn(cmd1);
        given(mapper.toHolidayCommand(dto2)).willReturn(cmd2);

        // when
        HolidayRefreshResponse response = holidayCommandService.refreshHolidays(year, countryCode);

        // then
        // 1) 국가 존재 여부 체크
        verify(countryRepository).existsByCode(countryCode);

        // 2) 해당 국가/연도에 대해 외부 공휴일 조회
        verify(nagerClient).getPublicHolidays(year, countryCode);

        // 3) DTO → Command 매핑
        verify(mapper).toHolidayCommand(dto1);
        verify(mapper).toHolidayCommand(dto2);

        // 4) 동기화 서비스 호출 (deduplicate 이후에도 두 개가 남아야 한다)
        verify(holidaySyncService).upsertOneYearHolidays(countryCode, List.of(cmd1, cmd2), year);

        // 5) 응답에 "원본 공휴일 개수"가 담긴다 (중복 제거 전 size 기준)
        assertThat(response.holidaysCount()).isEqualTo(2);
    }

    private HolidayUpsertCommand createHolidayCommand(
            String countryCode,
            LocalDate date,
            String localName,
            String name
    ) {
        return new HolidayUpsertCommand(
                countryCode,
                date,
                localName,
                name,
                true,               // global
                false,              // fixed
                null,               // launchYear
                List.of("Public"),  // types
                null                // counties
        );
    }

    @DisplayName("재동기화 시, DB에 없는 국가 코드를 조회했다면 EntityNotFoundException을 던진다.")
    @Test
    void refreshHolidays_NotFoundCountryCode() {
        // given
        int year = 2023;
        String countryCode = "ZZ";

        given(countryRepository.existsByCode(countryCode)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> holidayCommandService.refreshHolidays(year, countryCode))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("재동기화 시, year가 2021~2025 범위를 벗어나면 InvalidValueException 예외가 전파된다.")
    @Test
    void refreshHolidays_InvalidYearInput() {
        // given
        int invalidYear = 2030;
        String countryCode = "KR";

        given(countryRepository.existsByCode(countryCode)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> holidayCommandService.refreshHolidays(invalidYear, countryCode))
                .isInstanceOf(InvalidValueException.class);
    }

    @DisplayName("특정 국가/연도의 공휴일을 삭제하고, 삭제된 건수를 반환한다")
    @Test
    void deleteHolidays() {
        // given
        int year = 2023;
        String countryCode = "KR";

        // HolidaySyncService 가 7건 삭제했다고 가정
        given(holidaySyncService.deleteOneYearHolidays(countryCode, year))
                .willReturn(7);

        // when
        int result = holidayCommandService.deleteHolidays(year, countryCode);

        // then
        // HolidaySyncService 가 올바른 파라미터로 호출되었는지
        verify(holidaySyncService).deleteOneYearHolidays(countryCode, year);

        // 반환값이 삭제 건수와 동일한지
        assertThat(result).isEqualTo(7);
    }

    @DisplayName("삭제 시 HolidaySyncService에서 EntityNotFoundException이 발생하면 그대로 전파된다")
    @Test
    void deleteHolidays_entityNotFound() {
        // given
        int year = 2023;
        String countryCode = "ZZ";

        given(holidaySyncService.deleteOneYearHolidays(countryCode, year))
                .willThrow(new EntityNotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "존재하지 않는 국가 코드입니다. countryCode=" + countryCode
                ));

        // when & then
        assertThatThrownBy(() -> holidayCommandService.deleteHolidays(year, countryCode))
                .isInstanceOf(EntityNotFoundException.class);

        verify(holidaySyncService).deleteOneYearHolidays(countryCode, year);
    }

}
