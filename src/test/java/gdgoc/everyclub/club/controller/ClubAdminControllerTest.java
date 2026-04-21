package gdgoc.everyclub.club.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.club.domain.ClubAdminRole;
import gdgoc.everyclub.club.dto.AddClubAdminRequest;
import gdgoc.everyclub.club.dto.ClubAdminResponse;
import gdgoc.everyclub.club.dto.DelegateClubAdminRequest;
import gdgoc.everyclub.club.service.ClubAdminService;
import gdgoc.everyclub.club.service.ClubService;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.security.jwt.JwtProvider;
import gdgoc.everyclub.support.TestAuthenticationPrincipalConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ClubController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(TestAuthenticationPrincipalConfig.class)
@ActiveProfiles("test")
class ClubAdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ClubService clubService;

    @MockitoBean
    ClubAdminService clubAdminService;

    @MockitoBean
    JwtProvider jwtProvider;

    private CustomUserDetails userDetails(Long userId) {
        return new CustomUserDetails(userId, "user@example.com", null, "GUEST");
    }

    // ── GET /clubs/me ──────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /clubs/me - 내가 관리하는 동아리 목록을 반환한다")
    void getManagedClubs_returnsOwnedClubs() throws Exception {
        Long userId = 1L;
        CustomUserDetails principal = userDetails(userId);
        given(clubAdminService.getManagedClubs(userId)).willReturn(List.of());

        mockMvc.perform(get("/clubs/me")
                        .with(user(principal)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(clubAdminService).getManagedClubs(userId);
    }

    // ── GET /clubs/liked ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /clubs/liked - 좋아요한 동아리 목록을 반환한다")
    void getLikedClubs_returnsLikedClubs() throws Exception {
        Long userId = 1L;
        CustomUserDetails principal = userDetails(userId);
        given(clubAdminService.getLikedClubs(eq(userId), any())).willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/clubs/liked")
                        .with(user(principal)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(clubAdminService).getLikedClubs(eq(userId), any());
    }

    // ── GET /clubs/{id}/admins ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /clubs/{id}/admins - 동아리 관리자 목록을 반환한다")
    void getClubAdmins_returnsAdminList() throws Exception {
        Long clubId = 10L;
        ClubAdminResponse admin = new ClubAdminResponse(2L, "관리자", ClubAdminRole.MEMBER, LocalDateTime.now());
        given(clubAdminService.getClubAdmins(clubId)).willReturn(List.of(admin));

        mockMvc.perform(get("/clubs/{id}/admins", clubId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].userId").value(2))
                .andExpect(jsonPath("$.data[0].nickname").value("관리자"))
                .andExpect(jsonPath("$.data[0].role").value("MEMBER"));

        verify(clubAdminService).getClubAdmins(clubId);
    }

    // ── POST /clubs/{id}/admins ────────────────────────────────────────────

    @Test
    @DisplayName("POST /clubs/{id}/admins - 관리자를 추가하고 서비스를 호출한다")
    void addClubAdmin_invokesService() throws Exception {
        Long clubId = 10L;
        AddClubAdminRequest request = new AddClubAdminRequest(2L);

        mockMvc.perform(post("/clubs/{id}/admins", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(clubAdminService).addClubAdmin(clubId, 2L);
    }

    // ── DELETE /clubs/{id}/admins/{userId} ─────────────────────────────────

    @Test
    @DisplayName("DELETE /clubs/{id}/admins/{userId} - 관리자를 제거하고 서비스를 호출한다")
    void removeClubAdmin_invokesService() throws Exception {
        Long clubId = 10L;
        Long targetUserId = 2L;

        mockMvc.perform(delete("/clubs/{id}/admins/{userId}", clubId, targetUserId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(clubAdminService).removeClubAdmin(clubId, targetUserId);
    }

    // ── POST /clubs/{id}/admins/delegate ──────────────────────────────────

    @Test
    @DisplayName("POST /clubs/{id}/admins/delegate - LEAD 위임 요청을 서비스에 전달한다")
    void delegateClub_invokesService() throws Exception {
        Long clubId = 10L;
        Long currentUserId = 1L;
        CustomUserDetails principal = userDetails(currentUserId);
        DelegateClubAdminRequest request = new DelegateClubAdminRequest(
                2L, DelegateClubAdminRequest.FormerLeaderAction.DEMOTE);

        mockMvc.perform(post("/clubs/{id}/admins/delegate", clubId)
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(clubAdminService).delegateClub(
                eq(clubId), eq(currentUserId), eq(2L),
                eq(DelegateClubAdminRequest.FormerLeaderAction.DEMOTE));
    }
}
