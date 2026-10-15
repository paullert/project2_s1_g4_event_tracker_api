package cst438.project2.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import cst438.project2.backend.dto.InviteResponsePatch;
import cst438.project2.backend.service.InviteService;

public class InviteControllerTest {
    @Test
    void respondToMyInviteRequiresAuthenticationIntegration() {

        InviteService inviteService = mock(InviteService.class);
        InviteController controller = new InviteController(inviteService);

        InviteResponsePatch request =
                new InviteResponsePatch("ACCEPTED");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.respondToMyInvite(4L, request)
        );

        assertEquals(
                HttpStatus.NOT_IMPLEMENTED,
                exception.getStatusCode()
        );

        verifyNoInteractions(inviteService);
    }
}
