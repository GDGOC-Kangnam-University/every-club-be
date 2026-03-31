package gdgoc.everyclub.club.security;

import gdgoc.everyclub.club.domain.ClubAdminRole;
import gdgoc.everyclub.club.repository.ClubAdminRepository;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClubAdminGuard {

    private final ClubAdminRepository clubAdminRepository;

    /**
     * 동아리를 수정할 수 있는지 확인한다.
     * SYSTEM_ADMIN 이거나 해당 동아리의 LEAD/MEMBER이면 true.
     */
    public boolean canManage(Authentication authentication, Long clubId) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if ("SYSTEM_ADMIN".equals(userDetails.getRole())) {
            return true;
        }
        return clubAdminRepository.existsByUserIdAndClubId(userDetails.getUserId(), clubId);
    }

    /**
     * LEAD 전용 작업(삭제, 관리자 추가/제거, 위임)을 수행할 수 있는지 확인한다.
     * SYSTEM_ADMIN 이거나 해당 동아리의 LEAD이면 true.
     */
    public boolean canLead(Authentication authentication, Long clubId) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if ("SYSTEM_ADMIN".equals(userDetails.getRole())) {
            return true;
        }
        return clubAdminRepository.existsByUserIdAndClubIdAndRole(
                userDetails.getUserId(), clubId, ClubAdminRole.LEAD);
    }
}