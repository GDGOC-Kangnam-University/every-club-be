package gdgoc.everyclub.club.domain;

import gdgoc.everyclub.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "club")
@Getter
@Builder
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

    @Column(length = 50)
    private String department;

    @Column(length = 50)
    private String activityCycle;

    @Column(nullable = false)
    @Builder.Default
    private boolean hasFee = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean isPublic = false;

    @Column(nullable = false)
    @Builder.Default
    private int likeCount = 0;

    @Convert(converter = TagListConverter.class)
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

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

    // Legacy constructor for backward compatibility if needed, but we should update usages
    public Club(String name, String summary, User author, Category category, String slug) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        this.name = name;
        this.summary = summary;
        this.author = author;
        this.category = category;
        this.slug = slug;
        this.recruitingStatus = RecruitingStatus.OPEN;
        this.hasFee = false;
        this.isPublic = false;
        this.likeCount = 0;
        this.tags = new ArrayList<>();
    }

    public void update(String name, String summary, String description,
                       String logoUrl, String bannerUrl, String joinFormUrl,
                       RecruitingStatus recruitingStatus, String department,
                       String activityCycle, boolean hasFee, boolean isPublic,
                       List<String> tags) {
        this.name = name;
        this.summary = summary;
        this.description = description;
        this.logoUrl = logoUrl;
        this.bannerUrl = bannerUrl;
        this.joinFormUrl = joinFormUrl;
        this.recruitingStatus = recruitingStatus;
        this.department = department;
        this.activityCycle = activityCycle;
        this.hasFee = hasFee;
        this.isPublic = isPublic;
        this.tags = tags != null ? tags : new ArrayList<>();
    }
}
