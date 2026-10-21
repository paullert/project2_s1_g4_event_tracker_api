package cst438.project2.backend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ApiDatesTest {
  @ParameterizedTest
  @CsvSource({
      "2026-07-01T12:00:00,2026-07-01T12:00:00-07:00,2026-07-01T19:00:00Z",
      "2026-12-01T12:00:00,2026-12-01T12:00:00-08:00,2026-12-01T20:00:00Z"
  })
  void pacificTimestampsKeepTheSameInstantAcrossDaylightSavingTime(
      String stored, String response, String utcRequest) {
    assertEquals(response, ApiDates.format(LocalDateTime.parse(stored)));
    assertEquals(LocalDateTime.parse(stored), ApiDates.parse(utcRequest));
  }
}
