package gdgoc.everyclub.club.domain;

import gdgoc.everyclub.college.domain.Major;
import gdgoc.everyclub.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "club")
@Getter
@Builder(buildMethodName = "autoBuild")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE club SET deleted_at = NOW() WHERE id = ? AND deleted_at IS NULL")
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 200)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 2048)
    private String logoUrl;

    @Column(length = 2048)
    private String bannerUrl;

    @Column(length = 2048)
    private String joinFormUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RecruitingStatus recruitingStatus = RecruitingStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;

    @Column(length = 50)
    private String activityCycle;

    @Column(nullable = false)
    @Builder.Default
    private boolean hasFee = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean isPublic = false;

    @Builder.Default
    @ManyToMany(mappedBy = "likedClubs")
    private Set<User> likedByUsers = new LinkedHashSet<>();

    /**
     * 태그 매핑 컬렉션. {@link ClubTag}가 소유 측(owning side).
     * 직접 수정하지 말고 {@link #addTag(Tag)}/{@link #clearTags()} 도메인 메서드를 사용할 것.
     */
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ClubTag> clubTags = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User author;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @SuppressWarnings("unused")
    private LocalDateTime deletedAt;

    public static class ClubBuilder {
        /** name null/blank 검증을 빌더 build() 시점에 수행한다. */
        public Club build() {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Name cannot be null or blank");
            }
            return autoBuild();
        }
    }

    /** 태그 이름 목록 반환. 응답 DTO 생성 시 사용. */
    public List<String> getTagNames() {
        return clubTags.stream()
                .map(ct -> ct.getTag().getName())
                .toList();
    }

    /** 기존 태그 매핑을 전부 제거한다. orphanRemoval에 의해 ClubTag 레코드가 DELETE된다. */
    public void clearTags() {
        this.clubTags.clear();
    }

    /** 정규화된 Tag 엔티티를 이 동아리에 매핑한다. */
    public void addTag(Tag tag) {
        this.clubTags.add(ClubTag.of(this, tag));
    }

    public void update(String name, String summary, String description,
                       String logoUrl, String bannerUrl, String joinFormUrl,
                       RecruitingStatus recruitingStatus, Major major,
                       String activityCycle, boolean hasFee, boolean isPublic) {
        this.name = name;
        this.summary = summary;
        this.description = description;
        this.logoUrl = logoUrl;
        this.bannerUrl = bannerUrl;
        this.joinFormUrl = joinFormUrl;
        this.recruitingStatus = recruitingStatus;
        this.major = major;
        this.activityCycle = activityCycle;
        this.hasFee = hasFee;
        this.isPublic = isPublic;
    }
}
