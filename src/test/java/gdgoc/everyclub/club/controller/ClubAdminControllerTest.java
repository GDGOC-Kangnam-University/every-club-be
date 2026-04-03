package gdgoc.everyclub.club.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import gdgoc.everyclub.club.dto.ClubUpdateRequest;
import gdgoc.everyclub.club.security.ClubAdminGuard;
import gdgoc.everyclub.club.service.ClubAdminService;
import gdgoc.everyclub.club.service.ClubService;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.security.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClubController.class)
@Import(ClubAdminControllerTest.MethodSecurityConfig.class)
@ActiveProfiles("test")
class ClubAdminControllerTest {

    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ClubService clubService;

    @MockitoBean
    ClubAdminService clubAdminService;

    @MockitoBean(name = "clubAdminGuard")
    ClubAdminGuard clubAdminGuard;

    @MockitoBean
    JwtProvider jwtProvider;

    private CustomUserDetails userDetails() {
        return new CustomUserDetails(1L, "user@example.com", null, "GUEST");
    }

    // =====================================================================
    // LEAD 전용 엔드포인트 — canLead = false → 403
    // =====================================================================

    @Test
    @DisplayName("canLead가 false이면 POST /clubs/{id}/admins 요청 시 403을 반환한다")
    void postAdmins_whenNotLead_returns403() throws Exception {
        // given
        given(clubAdminGuard.canLead(any(), eq(10L))).willReturn(false);

        // when & then
        mockMvc.perform(post("/clubs/10/admins")
                        .with(user(userDetails()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 2}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("canLead가 false이면 DELETE /clubs/{id}/admins/{userId} 요청 시 403을 반환한다")
    void deleteAdmin_whenNotLead_returns403() throws Exception {
        // given
        given(clubAdminGuard.canLead(any(), eq(10L))).willReturn(false);

        // when & then
        mockMvc.perform(delete("/clubs/10/admins/2")
                        .with(user(userDetails()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("canLead가 false이면 POST /clubs/{id}/admins/delegate 요청 시 403을 반환한다")
    void delegateAdmin_whenNotLead_returns403() throws Exception {
        // given
        given(clubAdminGuard.canLead(any(), eq(10L))).willReturn(false);

        // when & then
        mockMvc.perform(post("/clubs/10/admins/delegate")
                        .with(user(userDetails()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetUserId\": 2, \"formerLeaderAction\": \"DEMOTE\"}"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("canLead가 false이면 DELETE /clubs/{id} 요청 시 403을 반환한다")
    void deleteClub_whenNotLead_returns403() throws Exception {
        // given
        given(clubAdminGuard.canLead(any(), eq(10L))).willReturn(false);

        // when & then
        mockMvc.perform(delete("/clubs/10")
                        .with(user(userDetails()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // =====================================================================
    // MANAGE 엔드포인트 — canManage = false → 403
    // =====================================================================

    @Test
    @DisplayName("canManage가 false이면 PUT /clubs/{id} 요청 시 403을 반환한다")
    void updateClub_whenNotAdmin_returns403() throws Exception {
        // given
        given(clubAdminGuard.canManage(any(), eq(10L))).willReturn(false);

        ClubUpdateRequest request = ClubUpdateRequest.builder()
                .name("동아리 이름")
                .summary("한 줄 소개")
                .recruitingStatus(RecruitingStatus.OPEN)
                .hasFee(false)
                .isPublic(true)
                .tags(List.of("태그1"))
                .build();

        // when & then
        mockMvc.perform(put("/clubs/10")
                        .with(user(userDetails()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("canManage가 false이면 GET /clubs/{id}/admins 요청 시 403을 반환한다")
    void getAdmins_whenNotAdmin_returns403() throws Exception {
        // given
        given(clubAdminGuard.canManage(any(), eq(10L))).willReturn(false);

        // when & then
        mockMvc.perform(get("/clubs/10/admins")
                        .with(user(userDetails())))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // =====================================================================
    // 정상 케이스
    // =====================================================================

    @Test
    @DisplayName("canLead가 true이면 POST /clubs/{id}/admins 요청 시 200을 반환한다")
    void postAdmins_whenLead_returns200() throws Exception {
        // given
        given(clubAdminGuard.canLead(any(), eq(10L))).willReturn(true);

        // when & then
        mockMvc.perform(post("/clubs/10/admins")
                        .with(user(userDetails()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 2}"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("canManage가 true이면 PUT /clubs/{id} 요청 시 200을 반환한다")
    void updateClub_whenAdmin_returns200() throws Exception {
        // given
        given(clubAdminGuard.canManage(any(), eq(10L))).willReturn(true);

        ClubUpdateRequest request = ClubUpdateRequest.builder()
                .name("동아리 이름")
                .summary("한 줄 소개")
                .recruitingStatus(RecruitingStatus.OPEN)
                .hasFee(false)
                .isPublic(true)
                .tags(List.of("태그1"))
                .build();

        // when & then
        mockMvc.perform(put("/clubs/10")
                        .with(user(userDetails()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // =====================================================================
    // 미인증
    // =====================================================================

    @Test
    @DisplayName("인증되지 않은 요청은 GET /clubs/{id}/admins에서 401을 반환한다")
    void getAdmins_unauthenticated_returns401() throws Exception {
        // when & then
        mockMvc.perform(get("/clubs/10/admins"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
