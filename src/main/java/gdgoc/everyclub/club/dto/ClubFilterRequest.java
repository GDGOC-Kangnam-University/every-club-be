package gdgoc.everyclub.club.dto;

import java.util.List;

/**
 * GET /clubs 필터 파라미터.
 *
 * <p>모든 필드는 선택적(nullable)이다. null 또는 빈 리스트이면 해당 조건은 무시(전체 조회)된다.
 *
 * <ul>
 *   <li>{@code categoryIds} — 동아리 유형 ID 목록 (복수 선택). URL에서 쉼표 구분 또는 반복 파라미터 모두 허용.
 *       예: {@code ?categoryIds=1,2} 또는 {@code ?categoryIds=1&categoryIds=2}</li>
 *   <li>{@code collegeId} — 단과대학 ID. '학과' 유형이 포함된 경우에만 의미 있다.
 *       {@code major}가 null인 동아리(중앙/기타 유형)는 자동으로 결과에서 제외된다.</li>
 *   <li>{@code hasFee} — 회비 유무. null이면 필터 없음.</li>
 *   <li>{@code hasActivity} — 정기 모임 유무.
 *       {@code activityCycle} 컬럼이 null이거나 공백 문자열이면 "없음"(false),
 *       값이 존재하면 "있음"(true)으로 해석한다.</li>
 * </ul>
 */
public record ClubFilterRequest(
        List<Long> categoryIds,
        Long collegeId,
        Boolean hasFee,
        Boolean hasActivity,
        String name,
        String tag
) {
    /** 모든 조건이 비어 있으면 필터 없음으로 판단한다. */
    public boolean isEmpty() {
        return (categoryIds == null || categoryIds.isEmpty())
                && collegeId == null
                && hasFee == null
                && hasActivity == null
                && (name == null || name.isBlank())
                && (tag == null || tag.isBlank());
    }

    /** 이름 검색만 요청된 경우 (다른 필터 없음). trigram 최적화 경로에 사용. */
    public boolean isNameOnly() {
        return (name != null && !name.isBlank())
                && (tag == null || tag.isBlank())
                && (categoryIds == null || categoryIds.isEmpty())
                && collegeId == null
                && hasFee == null
                && hasActivity == null;
    }
}