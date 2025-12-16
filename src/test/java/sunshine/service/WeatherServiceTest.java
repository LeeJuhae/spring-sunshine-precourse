package sunshine.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sunshine.weather.dto.ForecastResponse;
import sunshine.weather.model.City;
import sunshine.weather.service.OpenMeteo;
import sunshine.weather.service.WeatherService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class WeatherServiceTest {

    @Mock
    private OpenMeteo openMeteo;
    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        weatherService = new WeatherService(openMeteo);
    }

    @Test
    @DisplayName("존재하는 도시의 날씨 정보를 조회할 수 있다")
    void getWeatherSummaryForValidCity() {
        // given
        ForecastResponse.Current mockWeather = new ForecastResponse.Current(
            20.5, 19.0, 0, 65, 5.7
        );
        when(openMeteo.fetchCurrent(any(City.class))).thenReturn(mockWeather);

        // when
        String result = weatherService.getWeatherSummary("seoul");

        // then
        assertThat(result).contains("Seoul");
        assertThat(result).contains("20.5°C");
        assertThat(result).contains("맑음");
    }

    @Test
    @DisplayName("지원하지 않는 도시명으로 조회시 예외가 발생한다")
    void throwExceptionForInvalidCity() {
        // when & then
        assertThatThrownBy(() -> weatherService.getWeatherSummary("invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("지원하지 않는 도시입니다");
    }
}