package gdgoc.everyclub.club.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagNormalizerTest {

    @Test
    @DisplayName("null 입력은 null을 반환한다")
    void normalize_Null() {
        assertThat(TagNormalizer.normalize(null)).isNull();
    }

    @Test
    @DisplayName("# 문자가 제거된다")
    void normalize_RemovesHash() {
        assertThat(TagNormalizer.normalize("#운동")).isEqualTo("운동");
        assertThat(TagNormalizer.normalize("##코딩")).isEqualTo("코딩");
        assertThat(TagNormalizer.normalize("#운#동")).isEqualTo("운동");
    }

    @Test
    @DisplayName("앞뒤 공백이 제거된다")
    void normalize_Trim() {
        assertThat(TagNormalizer.normalize("  운동  ")).isEqualTo("운동");
        assertThat(TagNormalizer.normalize("\t친목\n")).isEqualTo("친목");
    }

    @Test
    @DisplayName("영문 대문자가 소문자로 변환된다")
    void normalize_Lowercase() {
        assertThat(TagNormalizer.normalize("GDGOC")).isEqualTo("gdgoc");
        assertThat(TagNormalizer.normalize("#GDGoC")).isEqualTo("gdgoc");
    }

    @Test
    @DisplayName("# 제거 + trim + 소문자화가 함께 적용된다")
    void normalize_Combined() {
        assertThat(TagNormalizer.normalize("  #Hello World  ")).isEqualTo("hello world");
    }

    @Test
    @DisplayName("정규화 후 빈 문자열이면 null을 반환한다")
    void normalize_BlankAfterNormalization_ReturnsNull() {
        assertThat(TagNormalizer.normalize("")).isNull();
        assertThat(TagNormalizer.normalize("   ")).isNull();
        assertThat(TagNormalizer.normalize("###")).isNull();
        assertThat(TagNormalizer.normalize("  #  ")).isNull();
    }

    @Test
    @DisplayName("정규화 후 30자를 초과하면 null을 반환한다")
    void normalize_ExceedsMaxLength_ReturnsNull() {
        String longTag = "a".repeat(31);
        assertThat(TagNormalizer.normalize(longTag)).isNull();
    }

    @Test
    @DisplayName("정규화 후 정확히 30자이면 허용된다")
    void normalize_ExactlyMaxLength_Allowed() {
        String exactTag = "a".repeat(30);
        assertThat(TagNormalizer.normalize(exactTag)).isEqualTo(exactTag);
    }

    @Test
    @DisplayName("# 제거 후 30자 이하이면 허용된다")
    void normalize_HashPrefixDoesNotCountTowardLength() {
        // '#' + 30자 → # 제거 후 30자 → 허용
        String tag = "#" + "a".repeat(30);
        assertThat(TagNormalizer.normalize(tag)).isEqualTo("a".repeat(30));
    }

    @Test
    @DisplayName("# 제거 후 31자이면 null을 반환한다")
    void normalize_HashPrefixWithLongBody_ReturnsNull() {
        String tag = "#" + "a".repeat(31);
        assertThat(TagNormalizer.normalize(tag)).isNull();
    }

    @Test
    @DisplayName("일반 특수문자(세미콜론 등)는 제거되지 않는다")
    void normalize_OtherSpecialCharsPreserved() {
        // 세미콜론은 TagNormalizer의 관심 대상이 아님 — 관계형 모델에서는 구분자가 없음
        assertThat(TagNormalizer.normalize("운동;친목")).isEqualTo("운동;친목");
    }
}
