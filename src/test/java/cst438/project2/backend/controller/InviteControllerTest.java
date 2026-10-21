package cst438.project2.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import cst438.project2.backend.dto.invite.InviteRequest;
import cst438.project2.backend.dto.invite.InviteResponse;
import cst438.project2.backend.security.JwtAuthConverter;
import cst438.project2.backend.security.SecurityConfig;
import cst438.project2.backend.service.InviteService;
import cst438.project2.backend.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InviteController.class)
@Import(SecurityConfig.class)
class InviteControllerTest {
  @Autowired private MockMvc mvc;
  @MockitoBean private InviteService invites;
  @MockitoBean private UserService users;
  @MockitoBean private JwtAuthConverter converter;
  @MockitoBean private JwtDecoder decoder;

  private final InviteResponse response = new InviteResponse(
      7L, 20L, "Study session", "Planning", "2026-11-01 12:00", "PENDING", null, null, null);
  private static final String INVITATION = """
      {"userId":7,"eventId":20,"status":"PENDING","message":"Welcome"}
      """;

  @Test
  void listInvitationsBindsPaginationAndSerializesPage() throws Exception {
    var pageable = PageRequest.of(1, 2);
    when(invites.getInvites(pageable)).thenReturn(new PageImpl<>(List.of(response), pageable, 3));
    mvc.perform(get("/api/v1/invites?page=1&size=2").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].eventId").value(20))
        .andExpect(jsonPath("$.page.totalElements").value(3));
    verify(invites).getInvites(pageable);
  }

  @Test
  void listUserInvitationsBindsUserId() throws Exception {
    when(invites.getInvitesForUser(eq(7L), any())).thenReturn(new PageImpl<>(List.of(response)));
    mvc.perform(get("/api/v1/invites/users/7").with(jwt()))
        .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].userId").value(7));
    verify(invites).getInvitesForUser(eq(7L), any());
  }

  @Test
  void replaceInvitationPassesBodyToService() throws Exception {
    var request = new InviteRequest(7L, 20L, "PENDING", "Welcome");
    when(invites.updateInvite(any(), eq(request))).thenReturn(response);
    mvc.perform(put("/api/v1/invites").with(jwt())
        .contentType(MediaType.APPLICATION_JSON).content(INVITATION))
        .andExpect(status().isOk());
    verify(invites).updateInvite(any(), eq(request));
  }

  @Test
  void patchInvitationPreservesUnspecifiedFieldsAsNull() throws Exception {
    var request = new InviteRequest(7L, 20L, null, "Updated message");
    when(invites.patchInvite(any(), eq(request))).thenReturn(response);
    mvc.perform(patch("/api/v1/invites").with(jwt()).contentType(MediaType.APPLICATION_JSON)
        .content("{\"userId\":7,\"eventId\":20,\"message\":\"Updated message\"}"))
        .andExpect(status().isOk());
    verify(invites).patchInvite(any(), eq(request));
  }

  @Test
  void invitationRoutesRequireAuthentication() throws Exception {
    mvc.perform(post("/api/v1/invites")
        .contentType(MediaType.APPLICATION_JSON).content(INVITATION))
        .andExpect(status().isUnauthorized());
    verifyNoInteractions(invites);
  }

}
