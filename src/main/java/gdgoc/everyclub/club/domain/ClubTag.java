package gdgoc.everyclub.club.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 동아리-태그 N:M 매핑 테이블 (ClubTag).
 *
 * <p>club_id + tag_id 복합 UNIQUE({@code uk_club_tag_club_tag})로 중복 매핑을 방지한다.
 * 태그 기반 역방향 조회 성능을 위해 (tag_id, club_id) 인덱스({@code idx_club_tag_tag_club})를 추가한다.
 */
@Entity
@Table(name = "club_tag",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_club_tag_club_tag",
                columnNames = {"club_id", "tag_id"}),
        indexes = @Index(
                name = "idx_club_tag_tag_club",
                columnList = "tag_id, club_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = {"club", "tag"})
public class ClubTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public static ClubTag of(Club club, Tag tag) {
        ClubTag ct = new ClubTag();
        ct.club = club;
        ct.tag = tag;
        return ct;
    }
}
