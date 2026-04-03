package gdgoc.everyclub.club.security;

import gdgoc.everyclub.club.domain.ClubAdminRole;
import gdgoc.everyclub.club.repository.ClubAdminRepository;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ClubAdminGuardTest {

    @Mock
    private ClubAdminRepository clubAdminRepository;

    @InjectMocks
    private ClubAdminGuard clubAdminGuard;

    // 헬퍼: Authentication 생성
    private Authentication authAs(Long userId, String role) {
        CustomUserDetails details = new CustomUserDetails(userId, "user@example.com", null, role);
        return new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
    }

    // =====================================================================
    // canManage
    // =====================================================================

    @Test
    @DisplayName("SYSTEM_ADMIN은 레포지토리 조회 없이 canManage가 true를 반환한다")
    void canManage_systemAdmin_returnsTrueWithoutRepositoryQuery() {
        // given
        Authentication auth = authAs(1L, "SYSTEM_ADMIN");
        Long clubId = 10L;

        // when
        boolean result = clubAdminGuard.canManage(auth, clubId);

        // then
        assertThat(result).isTrue();
        verifyNoInteractions(clubAdminRepository);
    }

    @Test
    @DisplayName("해당 동아리 관리자이면 canManage가 true를 반환한다")
    void canManage_clubAdmin_returnsTrue() {
        // given
        Long userId = 1L;
        Long clubId = 10L;
        Authentication auth = authAs(userId, "GUEST");
        given(clubAdminRepository.existsByUserIdAndClubId(userId, clubId)).willReturn(true);

        // when
        boolean result = clubAdminGuard.canManage(auth, clubId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("해당 동아리 관리자가 아니면 canManage가 false를 반환한다")
    void canManage_nonAdmin_returnsFalse() {
        // given
        Long userId = 1L;
        Long clubId = 10L;
        Authentication auth = authAs(userId, "GUEST");
        given(clubAdminRepository.existsByUserIdAndClubId(userId, clubId)).willReturn(false);

        // when
        boolean result = clubAdminGuard.canManage(auth, clubId);

        // then
        assertThat(result).isFalse();
    }

    // =====================================================================
    // canLead
    // =====================================================================

    @Test
    @DisplayName("SYSTEM_ADMIN은 레포지토리 조회 없이 canLead가 true를 반환한다")
    void canLead_systemAdmin_returnsTrueWithoutRepositoryQuery() {
        // given
        Authentication auth = authAs(1L, "SYSTEM_ADMIN");
        Long clubId = 10L;

        // when
        boolean result = clubAdminGuard.canLead(auth, clubId);

        // then
        assertThat(result).isTrue();
        verifyNoInteractions(clubAdminRepository);
    }

    @Test
    @DisplayName("LEAD 역할을 가진 관리자이면 canLead가 true를 반환한다")
    void canLead_lead_returnsTrue() {
        // given
        Long userId = 1L;
        Long clubId = 10L;
        Authentication auth = authAs(userId, "GUEST");
        given(clubAdminRepository.existsByUserIdAndClubIdAndRole(userId, clubId, ClubAdminRole.LEAD))
                .willReturn(true);

        // when
        boolean result = clubAdminGuard.canLead(auth, clubId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("MEMBER 역할의 관리자이면 canLead가 false를 반환한다")
    void canLead_member_returnsFalse() {
        // given
        Long userId = 1L;
        Long clubId = 10L;
        Authentication auth = authAs(userId, "GUEST");
        given(clubAdminRepository.existsByUserIdAndClubIdAndRole(userId, clubId, ClubAdminRole.LEAD))
                .willReturn(false);

        // when
        boolean result = clubAdminGuard.canLead(auth, clubId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("해당 동아리 관리자가 아니면 canLead가 false를 반환한다")
    void canLead_nonAdmin_returnsFalse() {
        // given
        Long userId = 99L;
        Long clubId = 10L;
        Authentication auth = authAs(userId, "GUEST");
        given(clubAdminRepository.existsByUserIdAndClubIdAndRole(userId, clubId, ClubAdminRole.LEAD))
                .willReturn(false);

        // when
        boolean result = clubAdminGuard.canLead(auth, clubId);

        // then
        assertThat(result).isFalse();
    }
}