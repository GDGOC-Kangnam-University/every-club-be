package gdgoc.everyclub.club.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 태그 마스터 엔티티.
 *
 * <p>name은 {@link TagNormalizer}를 통해 정규화(# 제거, trim, 소문자화)된 값만 저장된다.
 * DB UNIQUE 제약({@code uk_tag_name})으로 중복 저장을 방지한다.
 */
@Entity
@Table(name = "tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_tag_name", columnNames = "name"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 정규화된 태그 이름. 소문자, '#' 제거, 앞뒤 공백 제거. 최대 30자. */
    @Column(nullable = false, length = 30)
    private String name;

    public static Tag of(String normalizedName) {
        Tag tag = new Tag();
        tag.name = normalizedName;
        return tag;
    }
}
