package gdgoc.everyclub.club.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TagListConverterTest {

    private final TagListConverter converter = new TagListConverter();

    @Test
    @DisplayName("리스트를 세미콜론으로 구분된 문자열로 변환한다")
    void convertToDatabaseColumn() {
        // given
        List<String> tags = List.of("운동", "친목", "GDGOC");

        // when
        String result = converter.convertToDatabaseColumn(tags);

        // then
        assertThat(result).isEqualTo(";운동;친목;GDGOC;");
    }

    @Test
    @DisplayName("변환 시 공백을 제거하고 중복을 제거한다")
    void convertToDatabaseColumn_TrimAndUnique() {
        // given
        List<String> tags = List.of(" 운동 ", "친목", "운동");

        // when
        String result = converter.convertToDatabaseColumn(tags);

        // then
        assertThat(result).isEqualTo(";운동;친목;");
    }

    @Test
    @DisplayName("null 리스트를 null 문자열로 변환한다")
    void convertToDatabaseColumn_Null() {
        // when
        String result = converter.convertToDatabaseColumn(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("빈 리스트를 null 문자열로 변환한다")
    void convertToDatabaseColumn_Empty() {
        // when
        String result = converter.convertToDatabaseColumn(List.of());

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("세미콜론으로 구분된 문자열을 리스트로 변환한다")
    void convertToEntityAttribute() {
        // given
        String dbData = ";운동;친목;GDGOC;";

        // when
        List<String> result = converter.convertToEntityAttribute(dbData);

        // then
        assertThat(result).containsExactly("운동", "친목", "GDGOC");
    }

    @Test
    @DisplayName("null 또는 빈 문자열을 빈 리스트로 변환한다")
    void convertToEntityAttribute_Empty() {
        // when
        List<String> result1 = converter.convertToEntityAttribute(null);
        List<String> result2 = converter.convertToEntityAttribute("");

        // then
        assertThat(result1).isEmpty();
        assertThat(result2).isEmpty();
    }
}
