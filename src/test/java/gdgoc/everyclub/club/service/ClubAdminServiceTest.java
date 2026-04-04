package gdgoc.everyclub.club.service;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.ClubAdmin;
import gdgoc.everyclub.club.domain.ClubAdminRole;
import gdgoc.everyclub.club.domain.RecruitingStatus;
import gdgoc.everyclub.club.dto.DelegateClubAdminRequest.FormerLeaderAction;
import gdgoc.everyclub.club.repository.ClubAdminRepository;
import gdgoc.everyclub.club.repository.ClubRepository;
import gdgoc.everyclub.common.exception.BusinessErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClubAdminServiceTest {

    @Mock
    private ClubAdminRepository clubAdminRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ClubAdminService clubAdminService;

    private User lead;
    private User member;
    private Club club;
    private ClubAdmin leadAdmin;
    private ClubAdmin memberAdmin;

    @BeforeEach
    void setUp() {
        lead = new User("리더", "lead@example.com");
        ReflectionTestUtils.setField(lead, "id", 1L);

        member = new User("멤버", "member@example.com");
        ReflectionTestUtils.setField(member, "id", 2L);

        club = Club.builder()
                .name("테스트동아리")
                .slug("test-club")
                .summary("동아리 소개")
                .category(null)
                .recruitingStatus(RecruitingStatus.OPEN)
                .author(lead)
                .build();
        ReflectionTestUtils.setField(club, "id", 10L);

        leadAdmin = ClubAdmin.builder()
                .user(lead)
                .club(club)
                .role(ClubAdminRole.LEAD)
                .build();
        ReflectionTestUtils.setField(leadAdmin, "id", 1L);

        memberAdmin = ClubAdmin.builder()
                .user(member)
                .club(club)
                .role(ClubAdminRole.MEMBER)
                .build();
        ReflectionTestUtils.setField(memberAdmin, "id", 2L);
    }

    // =====================================================================
    // getClubAdmins
    // =====================================================================

    @Test
    @DisplayName("존재하지 않는 clubId로 관리자 목록 조회 시 RESOURCE_NOT_FOUND 예외가 발생한다")
    void getClubAdmins_clubNotFound_throwsNotFound() {
        // given
        Long clubId = 999L;
        given(clubRepository.findById(clubId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubAdminService.getClubAdmins(clubId))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    // =====================================================================
    // addClubAdmin
    // =====================================================================

    @Test
    @DisplayName("관리자 추가 시 MEMBER 역할로 저장된다")
    void addClubAdmin_success_savedAsMember() {
        // given
        Long clubId = 10L;
        Long targetUserId = 2L;

        given(clubAdminRepository.existsByUserIdAndClubId(targetUserId, clubId)).willReturn(false);
        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(userService.getUserById(targetUserId)).willReturn(member);

        ArgumentCaptor<ClubAdmin> captor = ArgumentCaptor.forClass(ClubAdmin.class);
        given(clubAdminRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

        // when
        clubAdminService.addClubAdmin(clubId, targetUserId);

        // then
        ClubAdmin saved = captor.getValue();
        assertThat(saved.getRole()).isEqualTo(ClubAdminRole.MEMBER);
        assertThat(saved.getUser()).isEqualTo(member);
        assertThat(saved.getClub()).isEqualTo(club);
    }

    @Test
    @DisplayName("이미 관리자인 유저 추가 시 DUPLICATE_RESOURCE 예외가 발생한다")
    void addClubAdmin_alreadyAdmin_throwsDuplicateResource() {
        // given
        Long clubId = 10L;
        Long targetUserId = 2L;

        given(clubAdminRepository.existsByUserIdAndClubId(targetUserId, clubId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> clubAdminService.addClubAdmin(clubId, targetUserId))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(BusinessErrorCode.DUPLICATE_RESOURCE);
    }

    @Test
    @DisplayName("존재하지 않는 clubId로 관리자 추가 시 RESOURCE_NOT_FOUND 예외가 발생한다")
    void addClubAdmin_clubNotFound_throwsNotFound() {
        // given
        Long clubId = 999L;
        Long targetUserId = 2L;

        given(clubAdminRepository.existsByUserIdAndClubId(targetUserId, clubId)).willReturn(false);
        given(clubRepository.findById(clubId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubAdminService.addClubAdmin(clubId, targetUserId))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    // =====================================================================
    // removeClubAdmin
    // =====================================================================

    @Test
    @DisplayName("관리자가 2명 이상일 때 정상적으로 관리자를 제거한다")
    void removeClubAdmin_success() {
        // given
        Long clubId = 10L;
        Long targetUserId = 2L;

        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(clubAdminRepository.findByClubId(clubId)).willReturn(List.of(leadAdmin, memberAdmin));
        given(clubAdminRepository.findByUserIdAndClubId(targetUserId, clubId))
                .willReturn(Optional.of(memberAdmin));

        // when
        clubAdminService.removeClubAdmin(clubId, targetUserId);

        // then
        verify(clubAdminRepository).delete(memberAdmin);
    }

    @Test
    @DisplayName("관리자가 1명뿐일 때 제거 시도 시 LAST_ADMIN_CANNOT_BE_REMOVED 예외가 발생한다")
    void removeClubAdmin_lastAdmin_throwsLastAdminCannotBeRemoved() {
        // given
        Long clubId = 10L;
        Long targetUserId = 1L;

        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(clubAdminRepository.findByClubId(clubId)).willReturn(List.of(leadAdmin));

        // when & then
        assertThatThrownBy(() -> clubAdminService.removeClubAdmin(clubId, targetUserId))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(BusinessErrorCode.LAST_ADMIN_CANNOT_BE_REMOVED);
    }

    @Test
    @DisplayName("LEAD A + MEMBER B 상태에서 LEAD A 자기 삭제 시도 시 LAST_LEAD_CANNOT_BE_REMOVED 예외가 발생한다")
    void removeClubAdmin_lastLead_throwsLastLeadCannotBeRemoved() {
        // given
        Long clubId = 10L;
        Long targetUserId = 1L; // LEAD 본인

        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(clubAdminRepository.findByClubId(clubId)).willReturn(List.of(leadAdmin, memberAdmin));
        given(clubAdminRepository.findByUserIdAndClubId(targetUserId, clubId))
                .willReturn(Optional.of(leadAdmin));

        // when & then
        assertThatThrownBy(() -> clubAdminService.removeClubAdmin(clubId, targetUserId))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(BusinessErrorCode.LAST_LEAD_CANNOT_BE_REMOVED);
    }

    @Test
    @DisplayName("대상이 관리자가 아닐 때 제거 시도 시 RESOURCE_NOT_FOUND 예외가 발생한다")
    void removeClubAdmin_targetNotAdmin_throwsNotFound() {
        // given
        Long clubId = 10L;
        Long targetUserId = 99L;

        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(clubAdminRepository.findByClubId(clubId)).willReturn(List.of(leadAdmin, memberAdmin));
        given(clubAdminRepository.findByUserIdAndClubId(targetUserId, clubId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubAdminService.removeClubAdmin(clubId, targetUserId))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 clubId로 관리자 제거 시 RESOURCE_NOT_FOUND 예외가 발생한다")
    void removeClubAdmin_clubNotFound_throwsNotFound() {
        // given
        Long clubId = 999L;
        Long targetUserId = 2L;
        given(clubRepository.findById(clubId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubAdminService.removeClubAdmin(clubId, targetUserId))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    // =====================================================================
    // delegateClub
    // =====================================================================

    @Test
    @DisplayName("DEMOTE 위임 후 target은 LEAD가 되고 기존 LEAD는 MEMBER로 강등된다")
    void delegateClub_demote_targetBecomesLeadAndFormerLeaderDemoted() {
        // given
        Long clubId = 10L;
        Long currentLeaderId = 1L;
        Long targetUserId = 2L;

        ClubAdmin currentLeaderAdmin = ClubAdmin.builder()
                .user(lead).club(club).role(ClubAdminRole.LEAD).build();
        ClubAdmin targetMemberAdmin = ClubAdmin.builder()
                .user(member).club(club).role(ClubAdminRole.MEMBER).build();

        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(clubAdminRepository.findByUserIdAndClubId(targetUserId, clubId))
                .willReturn(Optional.of(targetMemberAdmin));
        given(clubAdminRepository.findByUserIdAndClubId(currentLeaderId, clubId))
                .willReturn(Optional.of(currentLeaderAdmin));

        // when
        clubAdminService.delegateClub(clubId, currentLeaderId, targetUserId, FormerLeaderAction.DEMOTE);

        // then
        assertThat(targetMemberAdmin.getRole()).isEqualTo(ClubAdminRole.LEAD);
        assertThat(currentLeaderAdmin.getRole()).isEqualTo(ClubAdminRole.MEMBER);
    }

    @Test
    @DisplayName("REMOVE 위임 후 target은 LEAD가 되고 기존 LEAD는 삭제된다")
    void delegateClub_remove_targetBecomesLeadAndFormerLeaderRemoved() {
        // given
        Long clubId = 10L;
        Long currentLeaderId = 1L;
        Long targetUserId = 2L;

        ClubAdmin currentLeaderAdmin = ClubAdmin.builder()
                .user(lead).club(club).role(ClubAdminRole.LEAD).build();
        ClubAdmin targetMemberAdmin = ClubAdmin.builder()
                .user(member).club(club).role(ClubAdminRole.MEMBER).build();

        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(clubAdminRepository.findByUserIdAndClubId(targetUserId, clubId))
                .willReturn(Optional.of(targetMemberAdmin));
        given(clubAdminRepository.findByUserIdAndClubId(currentLeaderId, clubId))
                .willReturn(Optional.of(currentLeaderAdmin));

        ArgumentCaptor<ClubAdmin> deleteCaptor = ArgumentCaptor.forClass(ClubAdmin.class);

        // when
        clubAdminService.delegateClub(clubId, currentLeaderId, targetUserId, FormerLeaderAction.REMOVE);

        // then
        assertThat(targetMemberAdmin.getRole()).isEqualTo(ClubAdminRole.LEAD);
        verify(clubAdminRepository).delete(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue()).isEqualTo(currentLeaderAdmin);
    }

    @Test
    @DisplayName("이미 LEAD인 target에게 위임 시도 시 ALREADY_LEAD 예외가 발생한다")
    void delegateClub_targetAlreadyLead_throwsAlreadyLead() {
        // given
        Long clubId = 10L;
        Long currentLeaderId = 1L;
        Long targetUserId = 2L;

        ClubAdmin alreadyLeadAdmin = ClubAdmin.builder()
                .user(member).club(club).role(ClubAdminRole.LEAD).build();

        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(clubAdminRepository.findByUserIdAndClubId(targetUserId, clubId))
                .willReturn(Optional.of(alreadyLeadAdmin));

        // when & then
        assertThatThrownBy(() -> clubAdminService.delegateClub(clubId, currentLeaderId, targetUserId, FormerLeaderAction.DEMOTE))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(BusinessErrorCode.ALREADY_LEAD);
    }

    @Test
    @DisplayName("해당 동아리 관리자가 아닌 target에게 위임 시도 시 TARGET_NOT_CLUB_ADMIN 예외가 발생한다")
    void delegateClub_targetNotAdmin_throwsTargetNotClubAdmin() {
        // given
        Long clubId = 10L;
        Long currentLeaderId = 1L;
        Long targetUserId = 99L;

        given(clubRepository.findById(clubId)).willReturn(Optional.of(club));
        given(clubAdminRepository.findByUserIdAndClubId(targetUserId, clubId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubAdminService.delegateClub(clubId, currentLeaderId, targetUserId, FormerLeaderAction.DEMOTE))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(BusinessErrorCode.TARGET_NOT_CLUB_ADMIN);
    }

    @Test
    @DisplayName("존재하지 않는 clubId로 위임 시 RESOURCE_NOT_FOUND 예외가 발생한다")
    void delegateClub_clubNotFound_throwsNotFound() {
        // given
        Long clubId = 999L;
        Long currentLeaderId = 1L;
        Long targetUserId = 2L;
        given(clubRepository.findById(clubId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> clubAdminService.delegateClub(clubId, currentLeaderId, targetUserId, FormerLeaderAction.DEMOTE))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }
}
