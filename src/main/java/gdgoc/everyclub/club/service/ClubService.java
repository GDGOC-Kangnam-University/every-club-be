package gdgoc.everyclub.club.service;

import gdgoc.everyclub.club.domain.Category;
import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.Tag;
import gdgoc.everyclub.club.domain.TagNormalizer;
import gdgoc.everyclub.club.dto.*;
import gdgoc.everyclub.club.repository.CategoryRepository;
import gdgoc.everyclub.club.repository.ClubRepository;
import gdgoc.everyclub.club.repository.ClubSpecification;
import gdgoc.everyclub.club.repository.TagRepository;
import gdgoc.everyclub.college.domain.Major;
import gdgoc.everyclub.college.repository.MajorRepository;
import gdgoc.everyclub.common.exception.BusinessErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.common.exception.ValidationErrorCode;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClubService {
    private final ClubRepository clubRepository;
    private final CategoryRepository categoryRepository;
    private final MajorRepository majorRepository;
    private final TagRepository tagRepository;
    private final UserService userService;

    /**
     * 공개 동아리 전체를 페이지 단위로 조회한다 (like count 포함).
     *
     * <p>Specification {@code isPublic()} 하나만 적용해 전체 공개 동아리를 반환한다.
     * 2단계 페치 패턴(ID 조회 → EntityGraph 배치 페치)으로 N+1을 방지한다.
     */
    public Page<ClubSummaryResponse> getClubsWithLikeCounts(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        return fetchWithLikeCounts(Specification.allOf(ClubSpecification.isPublic()), pageable);
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

        Major major = findMajorById(request.majorId());

        club.update(
                request.name(),
                request.summary(),
                request.description(),
                request.logoUrl(),
                request.bannerUrl(),
                request.joinFormUrl(),
                request.recruitingStatus(),
                major,
                request.activityCycle(),
                request.hasFee(),
                request.isPublic()
        );
        resolveAndSetTags(club, request.tags());
    }

    @Transactional
    public void deleteClub(Long id) {
        Club club = getClubById(id);
        clubRepository.delete(club);
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

    /**
     * 동아리 필터 조회.
     *
     * <p>조회 전략:
     * <ul>
     *   <li>이름 단독 검색 → trigram/ILIKE 최적화 경로 ({@link #searchClubsByName})</li>
     *   <li>그 외 (empty 포함) → {@code isPublic()} + 조건 Specification → {@link #fetchWithLikeCounts}</li>
     * </ul>
     * 태그 조건은 {@link ClubSpecification#hasTag(String)} EXISTS 서브쿼리로 Tag/ClubTag 조인 정확 매칭한다.
     */
    public Page<ClubSummaryResponse> filterClubs(ClubFilterRequest filter, Pageable pageable) {
        // 이름 단독 검색 → trigram/ILIKE 최적화 경로
        if (filter.isNameOnly()) {
            return searchClubsByName(filter.name(), pageable);
        }

        Specification<Club> spec = Specification.allOf(
                ClubSpecification.isPublic(),
                ClubSpecification.hasCategories(filter.categoryIds()),
                ClubSpecification.hasCollege(filter.collegeId()),
                ClubSpecification.hasFee(filter.hasFee()),
                ClubSpecification.hasActivity(filter.hasActivity()),
                ClubSpecification.hasTag(filter.tag()),
                ClubSpecification.hasNameLike(filter.name())
        );

        return fetchWithLikeCounts(spec, pageable);
    }

    /**
     * 태그로 동아리를 검색한다 (Tag/ClubTag 조인 정확 매칭).
     *
     * <p>입력 태그는 {@link TagNormalizer}로 정규화되어 정확히 일치하는 Tag를 조인으로 매칭한다.
     * LIKE 기반 TEXT 검색을 제거하고 관계형 조인으로 교체.
     */
    public Page<ClubSummaryResponse> searchClubsByTag(String tag, Pageable pageable) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("Tag cannot be null or blank");
        }
        String normalized = TagNormalizer.normalize(tag);
        if (normalized == null) {
            throw new IllegalArgumentException("Tag cannot be null or blank");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        Specification<Club> spec = Specification.allOf(
                ClubSpecification.isPublic(),
                ClubSpecification.hasTag(normalized)
        );
        return fetchWithLikeCounts(spec, pageable);
    }

    private Major findMajorById(Long majorId) {
        if (majorId == null) {
            return null;
        }

        return majorRepository.findById(majorId)
                .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));
    }

    /**
     * 동아리 이름 검색.
     *
     * <p>검색어 길이에 따라 쿼리 전략을 분기한다.
     * <ul>
     *   <li>공백만 입력 → 거부 (400)</li>
     *   <li>1~3자 → {@code ILIKE '%keyword%'}<br>
     *       trigram은 3글자부터 추출되므로 1~2자는 GIN 인덱스 효과가 없고
     *       3자도 trigram 1개뿐이라 사실상 full scan에 가깝다. 데이터 규모가
     *       커지면 최소 글자 수 정책 도입을 검토해야 한다.</li>
     *   <li>4자 이상 → {@code pg_trgm <%} 연산자 (GIN 인덱스 활용, 유사도 내림차순)<br>
     *       결과가 너무 적을 경우 DB의 {@code pg_trgm.word_similarity_threshold}
     *       (기본값 0.6)를 낮추는 것을 검토한다.</li>
     * </ul>
     *
     * <p>N+1 방지를 위해 native 쿼리로 ID만 조회한 뒤 {@code @EntityGraph}로
     * 엔티티를 배치 페치하는 2단계 패턴을 사용한다. {@code IN (:ids)} 쿼리는
     * 반환 순서를 보장하지 않으므로, {@code Map<Long, Club>}을 경유해
     * 1차 쿼리의 순서(유사도 순)를 명시적으로 복원한다.
     */
    public Page<ClubSummaryResponse> searchClubsByName(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            throw new LogicException(ValidationErrorCode.INVALID_INPUT);
        }

        String trimmed = keyword.strip();
        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        List<Long> ids;
        long total;

        if (trimmed.length() <= 3) {
            ids = clubRepository.findIdsByNameIlike(trimmed, limit, offset);
            total = clubRepository.countByNameIlike(trimmed);
        } else {
            ids = clubRepository.findIdsByNameTrgm(trimmed, limit, offset);
            total = clubRepository.countByNameTrgm(trimmed);
        }

        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 2단계 페치: IN (:ids)는 순서 비보장 → Map 경유로 1차 쿼리 순서 복원
        Map<Long, Club> clubById = clubRepository.findAllByIdInWithGraph(ids)
                .stream()
                .collect(Collectors.toMap(Club::getId, c -> c));

        // 3단계: like count 배치 조회
        Map<Long, Integer> likeCountById = clubRepository.findLikeCountsByIds(ids).stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> ((Long) arr[1]).intValue()
                ));

        List<ClubSummaryResponse> content = ids.stream()
                .map(id -> new ClubSummaryResponse(clubById.get(id), likeCountById.getOrDefault(id, 0)))
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Specification으로 공개 동아리를 페이지 조회하고 like count까지 조합해 반환한다.
     *
     * <p>2단계 + like count 배치 패턴:
     * <ol>
     *   <li>Specification으로 ID + total count 조회</li>
     *   <li>{@code findAllByIdInWithGraph}로 author/category/clubTags EntityGraph 배치 페치</li>
     *   <li>{@code findLikeCountsByIds}로 like count 배치 조회</li>
     * </ol>
     */
    private Page<ClubSummaryResponse> fetchWithLikeCounts(Specification<Club> spec, Pageable pageable) {
        Page<Club> page = clubRepository.findAll(spec, pageable);
        List<Long> ids = page.map(Club::getId).toList();
        if (ids.isEmpty()) {
            return page.map(club -> new ClubSummaryResponse(club, 0));
        }

        Map<Long, Club> clubById = clubRepository.findAllByIdInWithGraph(ids).stream()
                .collect(Collectors.toMap(Club::getId, c -> c));

        Map<Long, Integer> likeCountById = clubRepository.findLikeCountsByIds(ids).stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> ((Long) arr[1]).intValue()
                ));

        List<ClubSummaryResponse> content = ids.stream()
                .map(id -> new ClubSummaryResponse(clubById.get(id), likeCountById.getOrDefault(id, 0)))
                .toList();

        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    /**
     * 태그 이름 목록을 Tag 엔티티로 변환해 Club에 설정한다 (find-or-create).
     *
     * <p>기존 매핑을 전부 지우고 새 매핑을 추가한다.
     * 각 태그 이름은 {@link TagNormalizer#normalize(String)}로 정규화하며,
     * 정규화 후 null인 값(빈 값, 길이 초과 등)은 무시한다.
     * 중복 태그 이름은 DB UNIQUE 제약({@code uk_tag_name})으로 보장하며,
     * 기존 Tag가 없으면 신규 생성한다.
     *
     * @param club       태그를 설정할 Club 엔티티 (managed state 여야 함)
     * @param rawTagNames 입력 태그 이름 목록 (null 허용)
     */
    private void resolveAndSetTags(Club club, List<String> rawTagNames) {
        if (rawTagNames == null || rawTagNames.isEmpty()) {
            throw new LogicException(ValidationErrorCode.INVALID_INPUT);
        }

        List<String> normalizedTagNames = rawTagNames.stream()
                .map(TagNormalizer::normalize)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (normalizedTagNames.isEmpty()) {
            throw new LogicException(ValidationErrorCode.INVALID_INPUT);
        }

        club.clearTags();

        for (String normalized : normalizedTagNames) {
            Tag tag = tagRepository.findByName(normalized)
                    .orElseGet(() -> tagRepository.save(Tag.of(normalized)));
            club.addTag(tag);
        }
    }
}
