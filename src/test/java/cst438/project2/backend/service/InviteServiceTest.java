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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

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

  private InviteService inviteService;
  private User user;
  private Event event;

  @BeforeEach
  void setUp() {
    inviteService = new InviteService(inviteRepository, userService, userRepository, eventRepository);
    user = new User(USER_ID, "test-user", "google-subject", null, null, null);
    event = new Event(EVENT_ID, "Test Event", "Description", INVITED_AT.plusDays(1), USER_ID, null, null);
  }

  @Test
  void getAcceptedAttendeeCountUsesAcceptedStatus() {
    when(inviteRepository.countByEvent_IdAndStatus(EVENT_ID, "ACCEPTED")).thenReturn(2L);
    assertEquals(2L, inviteService.getAcceptedAttendeeCount(EVENT_ID));
    verify(inviteRepository).countByEvent_IdAndStatus(EVENT_ID, "ACCEPTED");
  }

  @Test
  void getInvitesForUserMapsRepositoryPage() {
    Invite invite = invite(InviteStatus.PENDING, "bring a friend");
    Pageable pageable = PageRequest.of(1, 2);
    when(inviteRepository.findByUser_UserId(USER_ID, pageable)).thenReturn(new PageImpl<>(List.of(invite), pageable, 3));
    Page<InviteResponse> result = inviteService.getInvitesForUser(USER_ID, pageable);
    assertEquals(1, result.getContent().size());
    assertEquals(USER_ID, result.getContent().get(0).userId());
    assertEquals(EVENT_ID, result.getContent().get(0).eventId());
    verify(inviteRepository).findByUser_UserId(USER_ID, pageable);
  }

  @Test
  void getAllInvitesMapsRepositoryPage() {
    Pageable pageable = PageRequest.of(0, 10);
    when(inviteRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(invite(InviteStatus.PENDING, null)), pageable, 1));
    assertEquals(1, inviteService.getAllInvites(pageable).getTotalElements());
    verify(inviteRepository).findAll(pageable);
  }

  @Test
  void getEventInvitesUsesEventFilter() {
    Pageable pageable = PageRequest.of(0, 10);
    when(inviteRepository.findByEvent_Id(EVENT_ID, pageable)).thenReturn(new PageImpl<>(List.of(invite(InviteStatus.PENDING, null))));
    assertEquals(1, inviteService.getEventInvites(EVENT_ID, pageable).getNumberOfElements());
    verify(inviteRepository).findByEvent_Id(EVENT_ID, pageable);
  }

  @Test
  void invalidPageableIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> inviteService.getInvitesForUser(USER_ID, PageRequest.of(0, 0)));
    verify(inviteRepository, never()).findByUser_UserId(any(), any());
  }

  @Test
  void getInviteReturnsMappedInvite() {
    Invite invite = invite(InviteStatus.PENDING, null);
    when(inviteRepository.findByUser_UserIdAndEvent_Id(USER_ID, EVENT_ID)).thenReturn(Optional.of(invite));
    assertEquals(EVENT_ID, inviteService.getInvite(USER_ID, EVENT_ID).eventId());
  }

  @Test
  void getMissingInviteReturnsNotFound() {
    when(inviteRepository.findByUser_UserIdAndEvent_Id(USER_ID, EVENT_ID)).thenReturn(Optional.empty());
    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> inviteService.getInvite(USER_ID, EVENT_ID));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void updateInviteAcceptsPendingInviteAndUpdatesAllFields() {
    Invite invite = invite(InviteStatus.PENDING, "old message");
    when(inviteRepository.findByUser_UserIdAndEvent_Id(USER_ID, EVENT_ID)).thenReturn(Optional.of(invite));
    when(inviteRepository.save(invite)).thenReturn(invite);
    InviteResponse result = inviteService.updateInvite(new InviteRequest(USER_ID, EVENT_ID, "ACCEPTED", "new message"));
    assertEquals("ACCEPTED", result.status());
    assertEquals(InviteStatus.ACCEPTED, invite.getStatus());
    assertEquals("new message", invite.getMessage());
    assertNotNull(invite.getRespondedAt());
    verify(inviteRepository).save(invite);
  }

  @Test
  void updateInviteRejectsMissingRequiredFields() {
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> inviteService.updateInvite(new InviteRequest(null, EVENT_ID, "ACCEPTED", "message")));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(inviteRepository, never()).save(any());
  }

  @Test
  void updateInviteRejectsAlreadyAnsweredInvite() {
    Invite invite = invite(InviteStatus.ACCEPTED, "message");
    when(inviteRepository.findByUser_UserIdAndEvent_Id(USER_ID, EVENT_ID)).thenReturn(Optional.of(invite));
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> inviteService.updateInvite(new InviteRequest(USER_ID, EVENT_ID, "DECLINED", "message")));
    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    verify(inviteRepository, never()).save(any());
  }

  @Test
  void updateInviteRejectsInvalidStatus() {
    Invite invite = invite(InviteStatus.PENDING, "message");
    when(inviteRepository.findByUser_UserIdAndEvent_Id(USER_ID, EVENT_ID)).thenReturn(Optional.of(invite));
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> inviteService.updateInvite(new InviteRequest(USER_ID, EVENT_ID, "UNKNOWN", "message")));
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    verify(inviteRepository, never()).save(any());
  }

  @Test
  void patchInviteCanUpdateMessageWithoutChangingStatus() {
    Invite invite = invite(InviteStatus.PENDING, "old message");
    when(inviteRepository.findByUser_UserIdAndEvent_Id(USER_ID, EVENT_ID)).thenReturn(Optional.of(invite));
    when(inviteRepository.save(invite)).thenReturn(invite);
    InviteResponse result = inviteService.patchInvite(new InviteRequest(USER_ID, EVENT_ID, null, "new message"));
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
    inviteService.patchInvite(new InviteRequest(USER_ID, EVENT_ID, "ACCEPTED", null));
    assertEquals(InviteStatus.ACCEPTED, invite.getStatus());
    assertNotNull(invite.getRespondedAt());
  }

  @Test
  void createInviteDefaultsStatusToPending() {
    when(userRepository.findByUserId(USER_ID)).thenReturn(user);
    when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
    when(inviteRepository.save(any(Invite.class))).thenAnswer(invocation -> invocation.getArgument(0));
    InviteResponse result = inviteService.createInvite(new InviteRequest(USER_ID, EVENT_ID, null, "Welcome"));
    assertEquals("PENDING", result.status());
    assertEquals(USER_ID, result.userId());
    verify(inviteRepository).save(any(Invite.class));
  }

  @Test
  void createInviteReturnsNotFoundForMissingUserOrEvent() {
    when(userRepository.findByUserId(USER_ID)).thenReturn(null);
    ResponseStatusException missingUser = assertThrows(ResponseStatusException.class,
        () -> inviteService.createInvite(new InviteRequest(USER_ID, EVENT_ID, null, null)));
    assertEquals(HttpStatus.NOT_FOUND, missingUser.getStatusCode());
    when(userRepository.findByUserId(USER_ID)).thenReturn(user);
    when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.empty());
    ResponseStatusException missingEvent = assertThrows(ResponseStatusException.class,
        () -> inviteService.createInvite(new InviteRequest(USER_ID, EVENT_ID, null, null)));
    assertEquals(HttpStatus.NOT_FOUND, missingEvent.getStatusCode());
  }

  @Test
  void authenticatedInviteMethodsUseAuthenticatedUser() {
    Invite invite = invite(InviteStatus.PENDING, null);
    Pageable pageable = PageRequest.of(0, 10);
    when(userService.findUserFromAuthentication(authentication)).thenReturn(user);
    when(inviteRepository.findByUser_UserId(USER_ID, pageable)).thenReturn(new PageImpl<>(List.of(invite)));
    when(inviteRepository.findByUser_UserIdAndEvent_Id(USER_ID, EVENT_ID)).thenReturn(Optional.of(invite));
    assertEquals(1, inviteService.getMyInvites(authentication, pageable).getNumberOfElements());
    assertEquals(EVENT_ID, inviteService.getMyInvite(authentication, EVENT_ID).eventId());
    verify(inviteRepository).findByUser_UserId(USER_ID, pageable);
    verify(inviteRepository).findByUser_UserIdAndEvent_Id(USER_ID, EVENT_ID);
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

  private Invite invite(InviteStatus status, String message) {
    return new Invite(new InviteId(USER_ID, EVENT_ID), user, event, status, message, INVITED_AT, null);
  }
}
