package gdgoc.everyclub.club.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import gdgoc.everyclub.club.dto.ClubCreateRequest;
import gdgoc.everyclub.club.dto.ClubDetailResponse;
import gdgoc.everyclub.club.dto.ClubUpdateRequest;
import gdgoc.everyclub.club.service.ClubService;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ClubController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ActiveProfiles("test")
class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClubService clubService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("동아리 생성 요청 시 200 OK와 생성된 동아리 ID를 반환한다")
    void createClub() throws Exception {
        // given
        ClubCreateRequest request = createCreateRequest("Name", "slug");
        given(clubService.createClub(any(ClubCreateRequest.class))).willReturn(1L);

        // when & then
        mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(1L));
    }

    @Test
    @DisplayName("동아리 생성 요청 시 필수 값이 누락되면 400 Bad Request를 반환한다")
    void createClub_InvalidInput() throws Exception {
        // given: empty name is invalid
        ClubCreateRequest request = createCreateRequest("", "slug");

        // when & then
        mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

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

        given(clubService.getClubs(any(PageRequest.class))).willReturn(new PageImpl<>(List.of(club)));

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

        given(clubService.getPublicClubById(clubId)).willReturn(new ClubDetailResponse(club));

        // when & then
        mockMvc.perform(get("/clubs/{id}", clubId))
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

    private ClubCreateRequest createCreateRequest(String name, String slug) {
        return ClubCreateRequest.builder()
                .name(name)
                .authorId(1L)
                .categoryId(1L)
                .slug(slug)
                .summary("Summary")
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY")
                .hasFee(false)
                .isPublic(true)
                .build();
    }

    private ClubUpdateRequest createUpdateRequest(String name) {
        return ClubUpdateRequest.builder()
                .name(name)
                .summary("Summary")
                .recruitingStatus(RecruitingStatus.OPEN)
                .activityCycle("WEEKLY")
                .hasFee(false)
                .isPublic(true)
                .build();
    }
}
