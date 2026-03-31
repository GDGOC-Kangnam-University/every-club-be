package gdgoc.everyclub.club.domain;

import gdgoc.everyclub.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "club_admin",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_club_admin_user_club",
                columnNames = {"user_id", "club_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClubAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClubAdminRole role;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ClubAdmin(User user, Club club, ClubAdminRole role) {
        this.user = user;
        this.club = club;
        this.role = role;
    }

    public void promoteToLead() {
        this.role = ClubAdminRole.LEAD;
    }

    public void demoteToMember() {
        this.role = ClubAdminRole.MEMBER;
    }
}