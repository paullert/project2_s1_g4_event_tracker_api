package cst438.project2.backend.dto.invite;

import java.util.List;

public record EventInviteRequest(
        // holds the users who should receive invites
        List<Long> userIds
) {
}
