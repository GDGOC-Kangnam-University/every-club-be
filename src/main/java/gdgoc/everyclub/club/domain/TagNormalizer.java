package gdgoc.everyclub.club.domain;

/**
 * 태그 이름 정규화 유틸리티.
 *
 * <p>정규화 규칙:
 * <ol>
 *   <li>{@code #} 문자 제거 (해시태그 입력 방식 허용)</li>
 *   <li>앞뒤 공백 제거 (trim)</li>
 *   <li>소문자화 (영문 대소문자 무시 매칭)</li>
 *   <li>빈 값 금지 — 정규화 후 빈 문자열이면 {@code null} 반환</li>
 *   <li>길이 제한 — 정규화 후 {@value #MAX_LENGTH}자 초과 시 {@code null} 반환</li>
 * </ol>
 *
 * <p>중복 방지는 {@link Tag} 테이블의 {@code uk_tag_name} UNIQUE 제약으로 보장한다.
 */
public final class TagNormalizer {

    public static final int MAX_LENGTH = 30;

    private TagNormalizer() {}

    /**
     * 원시 태그 입력값을 정규화한다.
     *
     * @param rawTag 사용자 입력 태그 이름 (null 허용)
     * @return 정규화된 태그 이름, 유효하지 않으면 {@code null}
     */
    public static String normalize(String rawTag) {
        if (rawTag == null) return null;
        String normalized = rawTag.replace("#", "").strip().toLowerCase();
        if (normalized.isBlank()) return null;
        if (normalized.length() > MAX_LENGTH) return null;
        return normalized;
    }
}
