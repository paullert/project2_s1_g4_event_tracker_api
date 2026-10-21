package cst438.project2.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cst438.project2.backend.dto.invite.InviteRequest;
import cst438.project2.backend.dto.invite.InviteResponse;
import cst438.project2.backend.dto.invite.MyInviteRequest;
import cst438.project2.backend.service.InviteService;
import cst438.project2.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class InviteControllerTest {

  @Mock private InviteService inviteService;
  @Mock private UserService userService;
  @Mock private Authentication authentication;

  private InviteController controller;
  private Pageable pageable;
  private Page<InviteResponse> page;
  private InviteResponse response;
  private InviteRequest request;

  @BeforeEach
  void setUp() {
    controller = new InviteController(inviteService, userService);
    pageable = PageRequest.of(0, 10);
    page = new PageImpl<>(java.util.List.of());
    response = new InviteResponse(7L, 20L, "Test Event", "Description",
        "2026-01-02 12:00", "PENDING", null);
    request = new InviteRequest(7L, 20L, "PENDING", "Welcome");
  }

  @Test
  void getAllInvitesDelegatesToService() {
    when(inviteService.getInvites(pageable)).thenReturn(page);

    assertEquals(page, controller.getAllInvites(pageable));

    verify(inviteService).getInvites(pageable);
  }

  @Test
  void createInviteDelegatesToService() {
    when(inviteService.createInvite(authentication, request)).thenReturn(response);

    assertEquals(response, controller.createInvite(authentication, request));

    verify(inviteService).createInvite(authentication, request);
  }

  @Test
  void putInviteDelegatesToService() {
    when(inviteService.updateInvite(authentication, request)).thenReturn(response);

    assertEquals(response, controller.putInvite(authentication, request));

    verify(inviteService).updateInvite(authentication, request);
  }

  @Test
  void patchInviteDelegatesToService() {
    when(inviteService.patchInvite(authentication, request)).thenReturn(response);

    assertEquals(response, controller.patchInvite(authentication, request));

    verify(inviteService).patchInvite(authentication, request);
  }

  @Test
  void getUserInvitesDelegatesToService() {
    when(inviteService.getInvitesForUser(7L, pageable)).thenReturn(page);

    assertEquals(page, controller.getUserInvites(7L, pageable));

    verify(inviteService).getInvitesForUser(7L, pageable);
  }

  @Test
  void getEventInvitesDelegatesToService() {
    when(inviteService.getEventInvites(authentication, 20L, pageable)).thenReturn(page);

    assertEquals(page, controller.getEventInvites(authentication, 20L, pageable));

    verify(inviteService).getEventInvites(authentication, 20L, pageable);
  }

  @Test
  void getUserEventInviteDelegatesToService() {
    when(inviteService.getInvite(authentication, 7L, 20L)).thenReturn(response);

    assertEquals(response, controller.getUserEventInvite(authentication, 7L, 20L));

    verify(inviteService).getInvite(authentication, 7L, 20L);
  }

  @Test
  void deleteUserEventInviteIsNotImplemented() {
    assertNull(controller.deleteUserEventInvite(7L, 20L));

    verifyNoInteractions(inviteService);
  }

  @Test
  void getMyInvitesRequiresAuthenticationImplementation() {
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> controller.getMyInvites());

    assertEquals(HttpStatus.NOT_IMPLEMENTED, exception.getStatusCode());
    verifyNoInteractions(inviteService);
  }

  @Test
  void getMyInviteRequiresAuthenticationImplementation() {
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> controller.getMyInvite(20L, null));

    assertEquals(HttpStatus.NOT_IMPLEMENTED, exception.getStatusCode());
    verifyNoInteractions(inviteService);
  }

  @Test
  void respondToMyInviteRequiresAuthenticationImplementation() {
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> controller.respondToMyInvite(20L, new MyInviteRequest("ACCEPTED")));

    assertEquals(HttpStatus.NOT_IMPLEMENTED, exception.getStatusCode());
    verifyNoInteractions(inviteService);
  }
}
