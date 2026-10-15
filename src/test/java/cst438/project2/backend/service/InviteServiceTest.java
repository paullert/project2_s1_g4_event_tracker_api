package cst438.project2.backend.service;

import cst438.project2.backend.model.Invite;
import cst438.project2.backend.repository.InviteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class InviteServiceTest {

    @Mock
    private InviteRepository inviteRepository;

    @InjectMocks
    private InviteService inviteService;

    @Test
    void getAcceptedAttendeeCountReturnsAcceptedInviteCount() {
        when(inviteRepository.countByEventIdAndStatus(1L, "ACCEPTED"))
                .thenReturn(2L);

        long result = inviteService.getAcceptedAttendeeCount(1L);

        assertEquals(2L, result);
        verify(inviteRepository)
                .countByEventIdAndStatus(1L, "ACCEPTED");
    }

    @Test
    void acceptPendingInvitation() {
        Invite invite = org.mockito.Mockito.mock(Invite.class);

        when(inviteRepository.findById(4L))
                .thenReturn(Optional.of(invite));
        when(invite.getUserId()).thenReturn(9L);
        when(invite.getStatus()).thenReturn("PENDING");
        when(inviteRepository.save(invite)).thenReturn(invite);

        Invite result = inviteService.updateInvite(4L, 9L, "ACCEPTED");

        assertEquals(invite, result);
        verify(invite).setStatus("ACCEPTED");
        verify(invite).setRespondedAt(any());
        verify(inviteRepository).save(invite);
    }

    @Test
    void declinePendingInvitation() {
        Invite invite = org.mockito.Mockito.mock(Invite.class);

        when(inviteRepository.findById(4L))
                .thenReturn(Optional.of(invite));
        when(invite.getUserId()).thenReturn(9L);
        when(invite.getStatus()).thenReturn("PENDING");
        when(inviteRepository.save(invite)).thenReturn(invite);

        Invite result = inviteService.updateInvite(4L, 9L, "DECLINED");

        assertEquals(invite, result);
        verify(invite).setStatus("DECLINED");
        verify(invite).setRespondedAt(any());
        verify(inviteRepository).save(invite);
    }

    @Test
    void cannotRespondToAnotherUsersInvitation() {
        Invite invite = org.mockito.Mockito.mock(Invite.class);

        when(inviteRepository.findById(4L))
                .thenReturn(Optional.of(invite));
        when(invite.getUserId()).thenReturn(9L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inviteService.updateInvite(4L, 7L, "ACCEPTED")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(inviteRepository, never()).save(any());
    }

    @Test
    void invalidStatusIsRejected() {
        Invite invite = org.mockito.Mockito.mock(Invite.class);

        when(inviteRepository.findById(4L))
                .thenReturn(Optional.of(invite));
        when(invite.getUserId()).thenReturn(9L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inviteService.updateInvite(4L, 9L, "PENDING")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(inviteRepository, never()).save(any());
    }

    @Test
    void alreadyAnsweredInvitationIsRejected() {
        Invite invite = org.mockito.Mockito.mock(Invite.class);

        when(inviteRepository.findById(4L))
                .thenReturn(Optional.of(invite));
        when(invite.getUserId()).thenReturn(9L);
        when(invite.getStatus()).thenReturn("ACCEPTED");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inviteService.updateInvite(4L, 9L, "DECLINED")
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(inviteRepository, never()).save(any());
    }

    @Test
    void nonexistentInvitationReturnsNotFound() {
        when(inviteRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> inviteService.updateInvite(99L, 9L, "ACCEPTED")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(inviteRepository, never()).save(any());
    }
}