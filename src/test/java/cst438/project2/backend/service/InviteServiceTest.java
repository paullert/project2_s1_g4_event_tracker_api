package cst438.project2.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import cst438.project2.backend.dto.invite.*;
import cst438.project2.backend.model.*;
import cst438.project2.backend.repository.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class InviteServiceTest {
  private static final Long USER_ID = 7L;
  private static final Long EVENT_ID = 20L;
  private static final LocalDateTime INVITED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

  @Mock private InviteRepository inviteRepository;
  @Mock private UserService userService;
  @Mock private UserRepository userRepository;
  @Mock private EventRepository eventRepository;
  @Mock private Authentication authentication;
  @Mock private EventOwnershipService ownership;

  private InviteService inviteService;
  private User user;
  private Event event;

  @BeforeEach
  void setUp() {
    inviteService = new InviteService(inviteRepository, userService, userRepository, eventRepository, ownership);
    user = new User(USER_ID, "test-user", "google-subject", null, null, null);
    event = new Event(EVENT_ID, "Test Event", "Description", INVITED_AT.plusDays(1), USER_ID, null, null);
  }

  @ParameterizedTest
  @EnumSource(value = InviteStatus.class, names = {"PENDING", "ACCEPTED"})
  void managerUpdateCanChangePendingOrAnsweredInvitations(InviteStatus currentStatus) {
    Invite invite = invite(currentStatus, "old message");
    when(inviteRepository.findByUser_UserIdAndEvent_Id(USER_ID, EVENT_ID)).thenReturn(Optional.of(invite));
    when(inviteRepository.save(invite)).thenReturn(invite);
    InviteResponse result = inviteService.updateInvite(authentication, new InviteRequest(USER_ID, EVENT_ID, "ACCEPTED", "new message"));
    assertEquals("ACCEPTED", result.status());
    assertEquals(InviteStatus.ACCEPTED, invite.getStatus());
    assertEquals("new message", invite.getMessage());
    assertNotNull(invite.getRespondedAt());
    verify(inviteRepository).save(invite);
  }

  @Test
  void updateInviteRejectsMissingRequiredFields() {
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> inviteService.updateInvite(authentication, new InviteRequest(null, EVENT_ID, "ACCEPTED", "message")));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(inviteRepository, never()).save(any());
  }

  @Test
  void updateInviteRejectsInvalidStatus() {
    Invite invite = invite(InviteStatus.PENDING, "message");
    when(inviteRepository.findByUser_UserIdAndEvent_Id(USER_ID, EVENT_ID)).thenReturn(Optional.of(invite));
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> inviteService.updateInvite(authentication, new InviteRequest(USER_ID, EVENT_ID, "UNKNOWN", "message")));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(inviteRepository, never()).save(any());
  }

  @Test
  void patchInviteCanUpdateMessageWithoutChangingStatus() {
    Invite invite = invite(InviteStatus.PENDING, "old message");
    when(inviteRepository.findByUser_UserIdAndEvent_Id(USER_ID, EVENT_ID)).thenReturn(Optional.of(invite));
    when(inviteRepository.save(invite)).thenReturn(invite);
    InviteResponse result = inviteService.patchInvite(authentication, new InviteRequest(USER_ID, EVENT_ID, null, "new message"));
    assertEquals("PENDING", result.status());
    assertEquals("new message", invite.getMessage());
    assertEquals(InviteStatus.PENDING, invite.getStatus());
    assertNull(invite.getRespondedAt());
    verify(inviteRepository).save(invite);
  }

  @Test
  void patchInviteCanAcceptPendingInvite() {
    Invite invite = invite(InviteStatus.PENDING, null);
    when(inviteRepository.findByUser_UserIdAndEvent_Id(USER_ID, EVENT_ID)).thenReturn(Optional.of(invite));
    when(inviteRepository.save(invite)).thenReturn(invite);
    inviteService.patchInvite(authentication, new InviteRequest(USER_ID, EVENT_ID, "ACCEPTED", null));
    assertEquals(InviteStatus.ACCEPTED, invite.getStatus());
    assertNotNull(invite.getRespondedAt());
  }

  @Test
  void createInviteDefaultsStatusToPending() {
    when(userService.findUserFromAuthentication(authentication)).thenReturn(user);
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
    when(inviteRepository.save(any(Invite.class))).thenAnswer(invocation -> invocation.getArgument(0));
    InviteResponse result = inviteService.createInvite(authentication,
        new InviteRequest(USER_ID, EVENT_ID, null, "Welcome"));
    assertEquals("PENDING", result.status());
    assertEquals(USER_ID, result.userId());
    verify(inviteRepository).save(any(Invite.class));
  }

  @Test
  void createInviteReturnsNotFoundForMissingUserOrEvent() {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
    ResponseStatusException missingUser = assertThrows(ResponseStatusException.class,
        () -> inviteService.createInvite(authentication, new InviteRequest(USER_ID, EVENT_ID, null, null)));
    assertEquals(HttpStatus.NOT_FOUND, missingUser.getStatusCode());
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.empty());
    ResponseStatusException missingEvent = assertThrows(ResponseStatusException.class,
        () -> inviteService.createInvite(authentication, new InviteRequest(USER_ID, EVENT_ID, null, null)));
    assertEquals(HttpStatus.NOT_FOUND, missingEvent.getStatusCode());
  }

  @Test
  void respondToMyInviteAcceptsPendingInvite() {
    Invite invite = invite(InviteStatus.PENDING, null);
    when(userService.findUserFromAuthentication(authentication)).thenReturn(user);
    when(inviteRepository.findByUser_UserIdAndEvent_Id(USER_ID, EVENT_ID)).thenReturn(Optional.of(invite));
    when(inviteRepository.save(invite)).thenReturn(invite);
    InviteResponse result = inviteService.respondToMyInvite(authentication, EVENT_ID, new MyInviteRequest("DECLINED"));
    assertEquals("DECLINED", result.status());
    assertEquals(InviteStatus.DECLINED, invite.getStatus());
    assertNotNull(invite.getRespondedAt());
    verify(inviteRepository).save(invite);
  }

  @Test
  void statusValidationOnlyAllowsResponses() {
    assertTrue(InviteService.isValidStatus("ACCEPTED"));
    assertTrue(InviteService.isValidStatus("DECLINED"));
    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> InviteService.isValidStatus("PENDING"));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
  }

  @Test
  void createEventInvitesCreatesPendingInvitesForMultipleUsers() {
    // creates a second user for the multiple user test
    User secondUser = new User(
            8L,
            "second-user",
            "second-google-subject",
            null,
            null,
            null
    );

    // confirms that the event exists
    when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));

    // confirms that both users exist
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(userRepository.findById(8L)).thenReturn(Optional.of(secondUser));

    // confirms that neither user already has an invite
    when(inviteRepository.existsByUser_UserIdAndEvent_Id(anyLong(), eq(EVENT_ID)))
            .thenReturn(false);

    // returns the invites that were saved
    when(inviteRepository.saveAll(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

    // creates invites for both users
    List<InviteResponse> result = inviteService.createEventInvites(
            EVENT_ID,
            List.of(USER_ID, 8L)
    );

    // confirms that two invites were created
    assertEquals(2, result.size());

    // confirms that both invites start as pending
    assertEquals("PENDING", result.get(0).status());
    assertEquals("PENDING", result.get(1).status());

    // confirms that the invites were saved
    verify(inviteRepository).saveAll(any());
  }

  private Invite invite(InviteStatus status, String message) {
    return new Invite(new InviteId(USER_ID, EVENT_ID), user, event, status, message, INVITED_AT, null);
  }
}
