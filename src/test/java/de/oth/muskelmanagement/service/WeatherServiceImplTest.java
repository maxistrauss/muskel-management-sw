package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.WeatherResponseDto;
import de.oth.muskelmanagement.service.impl.WeatherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class WeatherServiceImplTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    private WeatherServiceImpl weatherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webClientBuilder.build()).thenReturn(webClient);
        weatherService = new WeatherServiceImpl(webClientBuilder);
    }

    @Test
    void getCurrentWeather_ShouldReturnMockResponse_WhenNoApiKey() {
        // Given - no API key injected (default null/empty)

        // When
        WeatherResponseDto result = weatherService.getCurrentWeather("Regensburg");

        // Then
        assertNotNull(result);
        assertNotNull(result.getCurrent());
        assertNotNull(result.getHourly());
        assertNotNull(result.getDaily());

        // Verify current
        assertEquals(22.5, result.getCurrent().getTemperature());

        // Verify hourly (mock returns 4 items)
        assertEquals(4, result.getHourly().size());
        assertEquals("14:00", result.getHourly().get(0).getTime());

        // Verify daily (mock returns 5 items)
        assertEquals(5, result.getDaily().size());
        assertEquals("Mon", result.getDaily().get(0).getDate());
    }
}
