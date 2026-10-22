package cst438.project2.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cst438.project2.backend.dto.invite.InviteRequest;
import cst438.project2.backend.dto.invite.InviteResponse;
import cst438.project2.backend.dto.invite.MyInviteRequest;
import cst438.project2.backend.service.InviteService;
import cst438.project2.backend.service.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class InviteControllerTest {

  @Mock private InviteService inviteService;
  @Mock private UserService userService;

  private InviteController inviteController;
  private Pageable pageable;
  private Page<InviteResponse> page;
  private InviteResponse response;
  private InviteRequest request;
  private Authentication auth;

  @BeforeEach
  void setUp() {
    inviteController = new InviteController(inviteService, userService);
    pageable = PageRequest.of(0, 10);
    page = new PageImpl<>(List.of());
    response = new InviteResponse(7L, 20L, "Test Event", "Description",
        "2026-01-02 12:00", "PENDING", "message", null, null);
    request = new InviteRequest(7L, 20L, "PENDING", "Welcome");
    auth = Mockito.mock(Authentication.class);
  }

  @Test
  void getAllInvitesDelegatesToService() {
    when(inviteService.getInvites(pageable)).thenReturn(page);

    PagedModel<InviteResponse> result = inviteController.getAllInvites(pageable);

    assertEquals(page.getContent(), result.getContent());
    verify(inviteService).getInvites(pageable);
  }

  @Test
  void createInviteDelegatesToService() {
    when(inviteService.createInvite(auth, request)).thenReturn(response);

    ResponseEntity<InviteResponse> result = inviteController.createInvite(auth, request);

    assertEquals(response, result.getBody());
    verify(inviteService).createInvite(auth, request);
  }

  @Test
  void putInviteDelegatesToService() {
    when(inviteService.updateInvite(auth, request)).thenReturn(response);

    assertEquals(response, inviteController.putInvite(auth, request));

    verify(inviteService).updateInvite(auth, request);
  }

  @Test
  void patchInviteDelegatesToService() {
    when(inviteService.patchInvite(auth, request)).thenReturn(response);

    assertEquals(response, inviteController.patchInvite(auth, request));

    verify(inviteService).patchInvite(auth, request);
  }

  @Test
  void getUserInvitesDelegatesToService() {
    when(inviteService.getInvitesForUser(7L, pageable)).thenReturn(page);

    PagedModel<InviteResponse> result = inviteController.getUserInvites(7L, pageable);

    assertEquals(page.getContent(), result.getContent());
    verify(inviteService).getInvitesForUser(7L, pageable);
  }

  @Test
  void getEventInvitesDelegatesToService() {
    when(inviteService.getEventInvites(auth, 20L, pageable)).thenReturn(page);

    PagedModel<InviteResponse> result = inviteController.getEventInvites(auth, 20L, pageable);

    assertEquals(page.getContent(), result.getContent());
    verify(inviteService).getEventInvites(auth, 20L, pageable);
  }

  @Test
  void getUserEventInviteDelegatesToService() {
    when(inviteService.getInvite(7L, 20L)).thenReturn(response);

    assertEquals(response, inviteController.getUserEventInvite(7L, 20L));

    verify(inviteService).getInvite(7L, 20L);
  }

  @Test
  void deleteUserEventInviteDelegatesToService() {
    inviteController.deleteUserEventInvite(auth, 7L, 20L);

    verify(inviteService).deleteInvite(auth, 7L, 20L);
  }

  @Test
  void getMyInvitesDelegatesToService() {
    when(inviteService.getMyInvites(auth, pageable)).thenReturn(page);

    PagedModel<InviteResponse> result = inviteController.getMyInvites(auth, pageable);

    assertEquals(page.getContent(), result.getContent());
    verify(inviteService).getMyInvites(auth, pageable);
  }

  @Test
  void getMyInviteDelegatesToService() {
    when(inviteService.getMyInvite(auth, 20L)).thenReturn(response);

    assertEquals(response, inviteController.getMyInvite(20L, auth));
    verify(inviteService).getMyInvite(auth, 20L);
  }

  @Test
  void respondToMyInviteDelegatesToService() {
    MyInviteRequest myRequest = new MyInviteRequest("ACCEPTED");
    when(inviteService.respondToMyInvite(auth, 20L, myRequest)).thenReturn(response);

    assertEquals(response, inviteController.respondToMyInvite(20L, myRequest, auth));
    verify(inviteService).respondToMyInvite(auth, 20L, myRequest);
  }
}
