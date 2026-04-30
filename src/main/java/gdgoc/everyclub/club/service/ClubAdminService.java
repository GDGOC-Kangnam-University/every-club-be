package gdgoc.everyclub.club.service;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.ClubAdmin;
import gdgoc.everyclub.club.domain.ClubAdminRole;
import gdgoc.everyclub.club.dto.ClubAdminResponse;
import gdgoc.everyclub.club.dto.ClubSummaryResponse;
import gdgoc.everyclub.club.dto.DelegateClubAdminRequest.FormerLeaderAction;
import gdgoc.everyclub.club.repository.ClubAdminRepository;
import gdgoc.everyclub.club.repository.ClubRepository;
import gdgoc.everyclub.common.exception.BusinessErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubAdminService {

    private final ClubAdminRepository clubAdminRepository;
    private final ClubRepository clubRepository;
    private final UserService userService;

    /** 내가 관리하는 동아리 목록 (ClubAdmin 기준, isPublic 무관). */
    public List<ClubSummaryResponse> getManagedClubs(Long userId) {
        List<ClubAdmin> adminRows = clubAdminRepository.findByUserId(userId);
        if (adminRows.isEmpty()) {
            return List.of();
        }

        List<Long> ids = adminRows.stream()
                .map(ca -> ca.getClub().getId())
                .toList();

        Map<Long, Integer> likeCountById = clubRepository.findLikeCountsByIds(ids).stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> ((Long) arr[1]).intValue()
                ));

        return adminRows.stream()
                .map(ca -> new ClubSummaryResponse(ca.getClub(), likeCountById.getOrDefault(ca.getClub().getId(), 0)))
                .toList();
    }

    /** 좋아요한 공개 동아리 목록. */
    public Page<ClubSummaryResponse> getLikedClubs(Long userId, Pageable pageable) {
        Page<Club> page = clubRepository.findLikedClubsByUserId(userId, pageable);
        List<Long> ids = page.map(Club::getId).toList();
        if (ids.isEmpty()) {
            return page.map(club -> new ClubSummaryResponse(club, 0));
        }

        Map<Long, Integer> likeCountById = clubRepository.findLikeCountsByIds(ids).stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> ((Long) arr[1]).intValue()
                ));

        List<ClubSummaryResponse> content = ids.stream()
                .map(id -> {
                    Club club = page.getContent().stream()
                            .filter(c -> c.getId().equals(id))
                            .findFirst()
                            .orElseThrow();
                    return new ClubSummaryResponse(club, likeCountById.getOrDefault(id, 0));
                })
                .toList();

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    /** 동아리 관리자 목록 조회 */
    public List<ClubAdminResponse> getClubAdmins(Long clubId) {
        getClubOrThrow(clubId);
        return clubAdminRepository.findByClubId(clubId).stream()
                .map(ClubAdminResponse::from)
                .toList();
    }

    /** 관리자 추가 — 항상 MEMBER 역할로 추가된다. */
    @Transactional
    public void addClubAdmin(Long clubId, Long targetUserId) {
        if (clubAdminRepository.existsByUserIdAndClubId(targetUserId, clubId)) {
            throw new LogicException(BusinessErrorCode.DUPLICATE_RESOURCE);
        }
        Club club = getClubOrThrow(clubId);
        User targetUser = userService.getUserById(targetUserId);

        clubAdminRepository.save(ClubAdmin.builder()
                .user(targetUser)
                .club(club)
                .role(ClubAdminRole.MEMBER)
                .build());
    }

    @Transactional
    public void removeClubAdmin(Long clubId, Long targetUserId) {
        getClubOrThrow(clubId);
        List<ClubAdmin> admins = clubAdminRepository.findByClubId(clubId);
        if (admins.size() <= 1) {
            throw new LogicException(BusinessErrorCode.LAST_ADMIN_CANNOT_BE_REMOVED);
        }
        ClubAdmin clubAdmin = clubAdminRepository.findByUserIdAndClubId(targetUserId, clubId)
                .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        if (clubAdmin.getRole() == ClubAdminRole.LEAD) {
            long remainingLeads = admins.stream()
                    .filter(a -> a.getRole() == ClubAdminRole.LEAD && !a.getUser().getId().equals(targetUserId))
                    .count();
            if (remainingLeads == 0) {
                throw new LogicException(BusinessErrorCode.LAST_LEAD_CANNOT_BE_REMOVED);
            }
        }

        clubAdminRepository.delete(clubAdmin);
    }

    /**
     * LEAD 위임.
     *
     * <p>targetUserId는 해당 동아리의 MEMBER여야 한다.
     * formerLeaderAction에 따라 기존 LEAD를 MEMBER로 강등하거나 제거한다.
     *
     * @param clubId           동아리 id
     * @param currentLeaderId  현재 LEAD 사용자 id (컨트롤러에서 주입)
     * @param targetUserId     위임 대상 사용자 id (MEMBER여야 함)
     * @param action           기존 LEAD 처우 (DEMOTE: 강등, REMOVE: 제거)
     */
    @Transactional
    public void delegateClub(Long clubId, Long currentLeaderId, Long targetUserId, FormerLeaderAction action) {
        getClubOrThrow(clubId);
        ClubAdmin target = clubAdminRepository.findByUserIdAndClubId(targetUserId, clubId)
                .orElseThrow(() -> new LogicException(BusinessErrorCode.TARGET_NOT_CLUB_ADMIN));

        if (target.getRole() == ClubAdminRole.LEAD) {
            throw new LogicException(BusinessErrorCode.ALREADY_LEAD);
        }

        ClubAdmin currentLeader = clubAdminRepository.findByUserIdAndClubId(currentLeaderId, clubId)
                .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        target.promoteToLead();

        if (action == FormerLeaderAction.REMOVE) {
            clubAdminRepository.delete(currentLeader);
        } else {
            currentLeader.demoteToMember();
        }
    }

    private Club getClubOrThrow(Long clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));
    }
}
