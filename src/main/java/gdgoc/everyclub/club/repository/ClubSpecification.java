package gdgoc.everyclub.club.repository;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.domain.ClubTag;
import gdgoc.everyclub.club.domain.TagNormalizer;
import gdgoc.everyclub.college.domain.Major;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * {@link Club} 엔티티에 대한 JPA Specification 팩토리 클래스.
 *
 * <p>각 메서드는 조건이 불필요한 경우(null 또는 빈 값) {@code null}을 반환한다.
 * {@link Specification#and(Specification)}는 null을 무시하므로 안전하게 체이닝할 수 있다.
 */
public class ClubSpecification {

    private ClubSpecification() {}

    /** 공개 동아리만 조회. 모든 필터에 기본으로 적용된다. */
    public static Specification<Club> isPublic() {
        return (root, query, cb) -> cb.isTrue(root.get("isPublic"));
    }

    /**
     * 동아리 유형(카테고리) 필터.
     *
     * @param categoryIds null 또는 빈 리스트이면 null 반환 (조건 무시)
     */
    public static Specification<Club> hasCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return null;
        return (root, query, cb) -> root.get("category").get("id").in(categoryIds);
    }

    /**
     * 단과대학 필터.
     *
     * <p>경로: {@code Club.major(INNER JOIN) → Major.college.id}<br>
     * {@code major}가 null인 동아리(중앙동아리 등)는 INNER JOIN으로 인해 자동 제외된다.
     * 이는 "단과대학 선택" UI가 학과 유형 선택 시에만 활성화되는 요구사항과 일치한다.
     *
     * @param collegeId null이면 null 반환 (조건 무시)
     */
    public static Specification<Club> hasCollege(Long collegeId) {
        if (collegeId == null) return null;
        return (root, query, cb) -> {
            Join<Club, Major> major = root.join("major", JoinType.INNER);
            return cb.equal(major.get("college").get("id"), collegeId);
        };
    }

    /**
     * 회비 유무 필터.
     *
     * @param hasFee null이면 null 반환 (조건 무시)
     */
    public static Specification<Club> hasFee(Boolean hasFee) {
        if (hasFee == null) return null;
        return (root, query, cb) -> cb.equal(root.get("hasFee"), hasFee);
    }

    /**
     * 태그 필터 — Tag/ClubTag 조인 기반 정확 매칭.
     *
     * <p>입력값을 {@link TagNormalizer#normalize(String)}로 정규화한 뒤,
     * {@code club_tag JOIN tag} EXISTS 서브쿼리로 해당 태그를 가진 동아리만 조회한다.
     * LIKE 기반 TEXT 검색을 제거하고 관계형 조인 매칭으로 교체.
     *
     * <p>페이지네이션 count 쿼리에서 JOIN으로 인한 카운트 오염을 방지하기 위해
     * EXISTS 서브쿼리를 사용한다.
     *
     * @param tag null 또는 정규화 후 빈 문자열이면 null 반환 (조건 무시)
     */
    public static Specification<Club> hasTag(String tag) {
        if (tag == null || tag.isBlank()) return null;
        String normalized = TagNormalizer.normalize(tag);
        if (normalized == null) return null;
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<ClubTag> ctRoot = sub.from(ClubTag.class);
            sub.select(ctRoot.get("id"))
               .where(
                   cb.equal(ctRoot.get("club"), root),
                   cb.equal(ctRoot.get("tag").get("name"), normalized)
               );
            return cb.exists(sub);
        };
    }

    /**
     * 이름 LIKE 필터 (대소문자 무시, 부분 일치).
     *
     * <p>trigram 검색과 달리 Specification으로 조합 가능하나 GIN 인덱스를 활용하지 못한다.
     * name + 다른 필터가 함께 쓰일 때만 사용하고, name 단독 검색은 {@code searchClubsByName}을 사용한다.
     *
     * @param name null 또는 빈 문자열이면 null 반환 (조건 무시)
     */
    public static Specification<Club> hasNameLike(String name) {
        if (name == null || name.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.strip().toLowerCase() + "%");
    }

    /**
     * 정기 모임 유무 필터.
     *
     * <p>{@code activityCycle} 컬럼을 기준으로 해석한다.
     * <ul>
     *   <li>{@code true}: activityCycle이 null이 아니고 공백 문자열이 아닌 경우</li>
     *   <li>{@code false}: activityCycle이 null이거나 공백 문자열인 경우</li>
     * </ul>
     * DB에 빈 문자열({@code ""})과 null이 혼재할 수 있으므로 둘 다 "없음"으로 처리한다.
     * 공백 문자열({@code "  "})도 "없음"으로 처리하기 위해 {@code TRIM} 함수를 사용한다.
     *
     * @param hasActivity null이면 null 반환 (조건 무시)
     */
    public static Specification<Club> hasActivity(Boolean hasActivity) {
        if (hasActivity == null) return null;
        if (hasActivity) {
            return (root, query, cb) -> cb.and(
                    cb.isNotNull(root.get("activityCycle")),
                    cb.notEqual(cb.trim(root.get("activityCycle")), "")
            );
        } else {
            return (root, query, cb) -> cb.or(
                    cb.isNull(root.get("activityCycle")),
                    cb.equal(cb.trim(root.get("activityCycle")), "")
            );
        }
    }
}