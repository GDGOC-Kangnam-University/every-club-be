package gdgoc.everyclub.club.service;

import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.dto.*;
import gdgoc.everyclub.club.repository.CategoryRepository;
import gdgoc.everyclub.club.repository.ClubRepository;
import gdgoc.everyclub.common.exception.BusinessErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {
    private final ClubRepository clubRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Transactional
    public Long createClub(ClubCreateRequest request) {
        if (request == null) {
            throw new NullPointerException("ClubCreateRequest cannot be null");
        }

        if (clubRepository.existsBySlug(request.slug())) {
            throw new LogicException(BusinessErrorCode.DUPLICATE_RESOURCE);
        }

        User author = userService.getUserById(request.authorId());
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        Club club = Club.builder()
                .name(request.name())
                .author(author)
                .category(category)
                .slug(request.slug())
                .summary(request.summary())
                .description(request.description())
                .logoUrl(request.logoUrl())
                .bannerUrl(request.bannerUrl())
                .joinFormUrl(request.joinFormUrl())
                .recruitingStatus(request.recruitingStatus())
                .department(request.department())
                .activityCycle(request.activityCycle())
                .hasFee(request.hasFee())
                .isPublic(request.isPublic())
                .tags(request.tags())
                .build();

        clubRepository.save(club);
        return club.getId();
    }

    public Page<ClubSummaryResponse> getClubsWithLikeCounts(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return clubRepository.findAllPublicWithLikeCounts(pageable);
    }

    public Page<Club> getClubs(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return clubRepository.findAllByIsPublicTrue(pageable);
    }

    public Club getClubById(Long id) {
        return clubRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));
    }

    public ClubDetailResponse getPublicClubById(Long id) {
        return getPublicClubById(id, null);
    }

    public ClubDetailResponse getPublicClubById(Long id, Long userId) {
        Club club = getClubById(id);
        if (!club.isPublic()) {
            throw new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND);
        }
        
        boolean isLiked = false;
        if (userId != null) {
            isLiked = clubRepository.existsLikeByUserIdAndClubId(userId, id);
        }

        int likeCount = clubRepository.countLikesByClubId(id);
        
        return new ClubDetailResponse(club, isLiked, likeCount);
    }

    @Transactional
    public void updateClub(Long id, ClubUpdateRequest request) {
        if (request == null) {
            throw new NullPointerException("ClubUpdateRequest cannot be null");
        }
        Club club = getClubById(id);
        club.update(
                request.name(),
                request.summary(),
                request.description(),
                request.logoUrl(),
                request.bannerUrl(),
                request.joinFormUrl(),
                request.recruitingStatus(),
                request.department(),
                request.activityCycle(),
                request.hasFee(),
                request.isPublic(),
                request.tags()
        );
    }

    @Transactional
    public void deleteClub(Long id) {
        Club club = getClubById(id);
        clubRepository.delete(club);
    }

    /**
     * Validates that the user exists in the system.
     * Temporary authentication check until Spring Security is implemented.
     */
    public boolean validateUserExists(Long userId) {
        return userService.existsById(userId);
    }

    @Transactional
    public boolean toggleLike(Long clubId, Long userId) {
        // Validate club exists first using lightweight exists query
        if (!clubRepository.existsById(clubId)) {
            throw new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND);
        }

        // Validate user exists using lightweight exists query (N+1 fix)
        if (!userService.existsById(userId)) {
            throw new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND);
        }

        // Use atomic insert with ON CONFLICT DO NOTHING for PostgreSQL
        // This handles the race condition by making the check-and-act atomic at the database level
        int inserted = clubRepository.addLikeAtomic(userId, clubId);

        if (inserted > 0) {
            // Like was added
            return true;
        } else {
            // Already liked, so remove it
            clubRepository.removeLikeAtomic(userId, clubId);
            return false;
        }
    }

    public Page<Club> searchClubsByTag(String tag, Pageable pageable) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("Tag cannot be null or blank");
        }
        if (tag.length() > 50) {
            throw new IllegalArgumentException("Tag must be 50 characters or less");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return clubRepository.findByTagsContaining(tag, pageable);
    }
}
