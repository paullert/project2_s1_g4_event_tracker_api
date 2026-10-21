package cst438.project2.backend;

import java.time.LocalDateTime;
import cst438.project2.backend.model.UserRole;

public final class TestResources {
  private TestResources() {}

  public static final class Dates {
    private Dates() {}
    public static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 12, 0);
    public static final LocalDateTime[] OFFSET_TIMES = {
        NOW.plusDays(1),
        NOW.plusDays(2),
        NOW.plusDays(3)
    };
  }

  public static final class LocationValues {
    private LocationValues() {

    }
    public static final Long[] LOCATION_IDS = {7L, 13L, 10L};
    public static final Long[] MISSING_LOCATION_IDS = {999L};
    public static final String[] LOCATION_NAMES = {
        "Main Office",
        "Side Office",
        "Test Location"
    };
    public static final String[] ADDRESSES = {
        "10 Main Street",
        "20 Side Street",
        "123 Main Street"
    };
    public static final String[] LOCATION_DESCRIPTIONS = {
        "Primary office",
        "Secondary office",
        "This location definitely exists!"
    };
    public static final LocalDateTime[] UPDATED_ATS = {Dates.NOW};
    public static final String[] UPDATED_LOCATION_NAMES = {
        "Updated Office",
        "Updated Side Office"
    };
    public static final String[] UPDATED_ADDRESSES = {
        "20 Main Street",
        "20 Side Street"
    };
    public static final String[] UPDATED_LOCATION_DESCRIPTIONS = {
        "Updated description",
        "Better side office description"
    };
    public static final String[] PATCH_LOCATION_NAMES = {
        "Partially Updated Side Office",
        "Fully Updated Side Office"
    };
  }

  public static final class UserValues {
    private UserValues() {
    }

    public static final Long[] USER_IDS = {0L, 1L};
    public static final String[] USERNAMES = {"creator", "Test User"};
    public static final String[] GOOGLE_SUBJECTS = {"1234567890", "11111"};
    public static final String[] PHONE_NUMBERS = {"1234567890", "11111"};
    public static final UserRole[] ROLES = {UserRole.ADMIN};
  }

  public static final class EventValues {
    private EventValues() {}

    public static final Long[] EVENT_IDS = {20L, 30L};
    public static final String[] BASE_EVENT_NAMES = {"Test Event", "Big Party"};
    public static final String[] BASE_EVENT_DESCRIPTIONS = {
        "Basic Description",
        "Its very big!"
    };
    public static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 12, 0);
    public static final Long[] CREATOR_IDS = {0L, 1L};
    public static final Long[] EVENT_LOCATION_IDS = {10L, 7L};
    public static final LocalDateTime[] EVENT_START_TIMES = {
        Dates.NOW,
        Dates.OFFSET_TIMES[0]
    };
    public static final String[] EVENT_NAMES = {"Big Event", "Small Party"};
    public static final String[] EVENT_DESCRIPTIONS = {
        "Now this one is big!",
        "Its quite tiny!"
    };

    public static final String[] UPDATE_EVENT_NAMES = {
        "Partially Big Event",
        "Actually Big Event"
    };
    public static final String[] UPDATE_EVENT_DESCRIPTIONS = {
        "Not big yet!",
        "Now we are big!"
    };
    public static final Long[] UPDATED_CREATOR_IDS = {1L, 1L};
    public static final Long[] UPDATED_LOCATION_IDS = {2L, 7L};
  }


  public static final class InviteValues {
    private InviteValues() {}

    public static final Long[] INVITE_IDS = {4L, 99L};
    public static final Long[] USER_IDS = {7L, 9L};
    public static final Long[] EVENT_IDS = {1L};
    public static final String[] MESSAGES = {"Test Message", "Another Test Message"};
    public static final String[] STATUSES = {"PENDING", "ACCEPTED", "DECLINED"};
    public static final String ACCEPTED_STATUS = "ACCEPTED";
    public static final String DECLINED_STATUS = "DECLINED";
    public static final String PENDING_STATUS = "PENDING";
  }

  public static final class ErrorValues {
    private ErrorValues() {}

    public static final String INVALID_REQUEST_STRING = "ERR";
    public static final String[] INVALID_REQUEST_STRINGS = {INVALID_REQUEST_STRING};
    public static final Long INVALID_REQUEST_ID = 999L;
    public static final Long[] INVALID_REQUEST_LONGS = {Long.MIN_VALUE};
    public static final LocalDateTime INVALID_REQUEST_DATE = LocalDateTime.MIN;
    public static final LocalDateTime[] INVALID_REQUEST_DATES = {INVALID_REQUEST_DATE};
    public static final String[] BAD_REQUEST_MESSAGES = {
        constructBadRequestMessage("eventId, eventDescription, eventName, locationId, startsOn"),
        constructBadRequestMessage("locationName, address, locationDescription")
    };

    public static String constructBadRequestMessage(String fields) {
      return "Missing 1 or more required fields: " + fields;
    }

    public static String constructNotFoundMessage(String className, String idSection) {
      return "%s with %s not found".formatted(className, idSection);
    }
  }
}
