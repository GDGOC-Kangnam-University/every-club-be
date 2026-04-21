package gdgoc.everyclub.club.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import gdgoc.everyclub.club.dto.*;
import gdgoc.everyclub.club.service.ClubAdminService;
import gdgoc.everyclub.club.service.ClubService;
import gdgoc.everyclub.common.exception.ValidationErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.security.jwt.JwtProvider;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.support.TestAuthenticationPrincipalConfig;
import gdgoc.everyclub.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import gdgoc.everyclub.security.dto.CustomUserDetails;

import static org.mockito.ArgumentMatchers.*;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ClubController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(TestAuthenticationPrincipalConfig.class)
@ActiveProfiles("test")
class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClubService clubService;

    @MockitoBean
    private ClubAdminService clubAdminService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("모든 동아리 조회 시 200 OK와 동아리 리스트를 반환한다")
    void getClubs() throws Exception {
        // given
        User author = User.builder().email("author@example.com").nickname("Author").build();
        Category category = new Category("Academic");
        Club club = Club.builder()
                .name("Name")
                .author(author)
                .category(category)
                .slug("slug")
                .summary("Summary")
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY")
                .isPublic(true)
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);

        given(clubService.filterClubs(any(ClubFilterRequest.class), any())).willReturn(new PageImpl<>(List.of(new ClubSummaryResponse(club, 0))));

        // when & then
        mockMvc.perform(get("/clubs")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].name").value("Name"))
                .andExpect(jsonPath("$.data.content[0].authorName").value("Author"));
    }

    @Test
    @DisplayName("특정 동아리 조회 시 200 OK와 동아리 정보를 반환한다")
    void getClub() throws Exception {
        // given
        Long clubId = 1L;
        Long userId = 1L;
        CustomUserDetails principal = new CustomUserDetails(userId, "user@example.com", null, "GUEST");
        User author = User.builder().email("author@example.com").nickname("Author").build();
        Category category = new Category("Academic");
        Club club = Club.builder()
                .name("Name")
                .author(author)
                .category(category)
                .slug("slug")
                .summary("Summary")
                .description("Description")
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY")
                .isPublic(true)
                .build();
        ReflectionTestUtils.setField(club, "id", clubId);

        given(clubService.getPublicClubById(clubId, userId)).willReturn(new ClubDetailResponse(club));

        // when & then
        mockMvc.perform(get("/clubs/{id}", clubId)
                        .with(user(principal)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("Name"))
                .andExpect(jsonPath("$.data.description").value("Description"));
    }

    @Test
    @DisplayName("동아리 수정 시 200 OK를 반환한다")
    void updateClub() throws Exception {
        // given
        Long clubId = 1L;
        ClubUpdateRequest request = createUpdateRequest("Updated Name");

        // when & then
        mockMvc.perform(put("/clubs/{id}", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(clubService).updateClub(eq(clubId), any(ClubUpdateRequest.class));
    }

    // ── GET /clubs (name/tag 통합 검색) ──────────────────────────────────────

    @Test
    @DisplayName("GET /clubs?name=으로 이름 검색 시 200 OK를 반환한다")
    void getClubs_WithName() throws Exception {
        // given
        ClubSummaryResponse response = new ClubSummaryResponse(buildClub(), 0);
        given(clubService.filterClubs(any(ClubFilterRequest.class), any()))
                .willReturn(new PageImpl<>(List.of(response)));

        // when & then
        mockMvc.perform(get("/clubs")
                        .param("name", "축구")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray());

        verify(clubService).filterClubs(
                argThat(f -> "축구".equals(f.name()) && f.tag() == null),
                any());
    }

    @Test
    @DisplayName("GET /clubs?tag=으로 태그 검색 시 200 OK를 반환한다")
    void getClubs_WithTag() throws Exception {
        // given
        ClubSummaryResponse response = new ClubSummaryResponse(buildClub(), 0);
        given(clubService.filterClubs(any(ClubFilterRequest.class), any()))
                .willReturn(new PageImpl<>(List.of(response)));

        // when & then
        mockMvc.perform(get("/clubs")
                        .param("tag", "운동")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(clubService).filterClubs(
                argThat(f -> "운동".equals(f.tag()) && f.name() == null),
                any());
    }

    @Test
    @DisplayName("GET /clubs?name=&tag=으로 name+tag AND 검색 시 200 OK를 반환한다")
    void getClubs_WithNameAndTag() throws Exception {
        // given
        given(clubService.filterClubs(any(ClubFilterRequest.class), any()))
                .willReturn(new PageImpl<>(List.of()));

        // when & then
        mockMvc.perform(get("/clubs")
                        .param("name", "축구")
                        .param("tag", "운동")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());

        // then: name과 tag가 모두 filterClubs에 전달됨 (AND 조건)
        verify(clubService).filterClubs(
                argThat(f -> "축구".equals(f.name()) && "운동".equals(f.tag())),
                any());
    }

    @Test
    @DisplayName("GET /clubs?name=&categoryIds=으로 이름 검색 + 필터 조합 시 200 OK를 반환한다")
    void getClubs_WithNameAndFilters() throws Exception {
        // given
        given(clubService.filterClubs(any(ClubFilterRequest.class), any()))
                .willReturn(new PageImpl<>(List.of()));

        // when & then
        mockMvc.perform(get("/clubs")
                        .param("name", "축구")
                        .param("categoryIds", "1")
                        .param("hasFee", "false")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());

        verify(clubService).filterClubs(
                argThat(f -> "축구".equals(f.name())
                        && f.categoryIds() != null && f.categoryIds().contains(1L)
                        && Boolean.FALSE.equals(f.hasFee())),
                any());
    }

    // ── GET /clubs/search ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /clubs/search 파라미터 없으면 400을 반환한다")
    void searchClubs_NoParams_Returns400() throws Exception {
        mockMvc.perform(get("/clubs/search")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("GET /clubs/search?name=&tag=으로 AND 검색 시 filterClubs에 위임된다")
    void searchClubs_WithNameAndTag_DelegatesToFilterClubs() throws Exception {
        // given
        given(clubService.filterClubs(any(ClubFilterRequest.class), any()))
                .willReturn(new PageImpl<>(List.of()));

        // when & then
        mockMvc.perform(get("/clubs/search")
                        .param("name", "축구")
                        .param("tag", "운동")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk());

        verify(clubService).filterClubs(
                argThat(f -> "축구".equals(f.name()) && "운동".equals(f.tag())),
                any());
    }

    @Test
    @DisplayName("GET /clubs/search?tag=으로 태그 검색 시 200 OK와 동아리 리스트를 반환한다")
    void searchClubsByTag() throws Exception {
        // given: /clubs/search는 filterClubs에 위임됨
        String tag = "운동";
        ClubSummaryResponse response = new ClubSummaryResponse(buildClub(), 0);
        given(clubService.filterClubs(any(ClubFilterRequest.class), any()))
                .willReturn(new PageImpl<>(List.of(response)));

        // when & then
        mockMvc.perform(get("/clubs/search")
                        .param("tag", tag)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].name").value("Name"));

        verify(clubService).filterClubs(
                argThat(f -> tag.equals(f.tag()) && f.name() == null),
                any());
    }

    @Test
    @DisplayName("동아리 삭제 시 200 OK를 반환한다")
    void deleteClub() throws Exception {
        // given
        Long clubId = 1L;

        // when & then
        mockMvc.perform(delete("/clubs/{id}", clubId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(clubService).deleteClub(clubId);
    }

    @Test
    @DisplayName("좋아요 토글 시 200 OK와 변경된 좋아요 상태를 반환한다")
    void toggleLike() throws Exception {
        // given
        Long clubId = 1L;
        Long userId = 1L;
        CustomUserDetails principal = new CustomUserDetails(userId, "user@example.com", null, "GUEST");
        given(clubService.toggleLike(any(), any())).willReturn(true);

        // when & then
        mockMvc.perform(post("/clubs/{id}/like", clubId)
                        .with(user(principal)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(true));

        verify(clubService).toggleLike(clubId, userId);
    }

    private Club buildClub() {
        User author = User.builder().email("author@example.com").nickname("Author").build();
        Category category = new Category("Academic");
        Club club = Club.builder()
                .name("Name").author(author).category(category)
                .slug("slug").summary("Summary")
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY").isPublic(true)
                .build();
        ReflectionTestUtils.setField(club, "id", 1L);
        return club;
    }

    private ClubUpdateRequest createUpdateRequest(String name) {
        return ClubUpdateRequest.builder()
                .name(name)
                .summary("Summary")
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY")
                .hasFee(false)
                .isPublic(true)
                .tags(List.of("tag1", "tag2"))
                .build();
    }
}
