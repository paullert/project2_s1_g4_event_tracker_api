package cst438.project2.backend.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/** Existing timestamp-without-time-zone columns represent Los Angeles local time. */
public final class ApiDates {
  public static final ZoneId STORAGE_ZONE = ZoneId.of("America/Los_Angeles");
  private ApiDates() {}

  public static String format(LocalDateTime value) {
    return value == null ? null : value.atZone(STORAGE_ZONE)
        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
  }

  public static LocalDateTime now() {
    return LocalDateTime.now(STORAGE_ZONE);
  }

  public static LocalDateTime parse(String value) {
    if (value == null) return null;
    try {
      return OffsetDateTime.parse(value).atZoneSameInstant(STORAGE_ZONE).toLocalDateTime();
    } catch (java.time.format.DateTimeParseException ex) {
      // Keep accepting legacy local timestamps; new clients should send an explicit offset.
      return LocalDateTime.parse(value);
    }
  }
}
