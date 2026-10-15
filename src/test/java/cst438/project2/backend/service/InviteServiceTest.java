package cst438.project2.backend.service;

import cst438.project2.backend.repository.InviteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}