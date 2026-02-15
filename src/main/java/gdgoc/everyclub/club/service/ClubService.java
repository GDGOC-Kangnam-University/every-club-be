package gdgoc.everyclub.club.service;

import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.dto.ClubCreateRequest;
import gdgoc.everyclub.club.dto.ClubDetailResponse;
import gdgoc.everyclub.club.dto.ClubUpdateRequest;
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
                .build();

        clubRepository.save(club);
        return club.getId();
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
        Club club = getClubById(id);
        if (!club.isPublic()) {
            throw new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND);
        }
        return new ClubDetailResponse(club);
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
                request.isPublic()
        );
    }

    @Transactional
    public void deleteClub(Long id) {
        Club club = getClubById(id);
        clubRepository.delete(club);
    }
}
