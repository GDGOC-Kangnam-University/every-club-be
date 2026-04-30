package gdgoc.everyclub.user.domain;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.GUEST;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 60)
    private String passwordHash;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @ManyToMany
    @JoinTable(
            name = "club_likes",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "club_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "club_id"})
    )
    private Set<Club> likedClubs = new LinkedHashSet<>();

    private String nickname;

    private String profileImageUrl;

    private String department;

    private String studentId;

    private String phoneNumber;

    @Column(length = 500)
    private String bio;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @SuppressWarnings("unused") // Handled on @SQLDelete
    private LocalDateTime deletedAt;

    @Builder
    public User(String email, String passwordHash, String nickname, String profileImageUrl,
                String department, String studentId, String phoneNumber, String bio) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.department = department;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.bio = bio;
    }

    public User(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }

    public void updateProfile(String nickname, String department, String studentId,
                              String phoneNumber, String bio) {
        this.nickname = nickname;
        this.department = department;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.bio = bio;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void markEmailAsVerified() {
        this.emailVerified = true;
    }

    /**
     * Returns the display name for the user.
     * Uses nickname if available, otherwise falls back to email prefix.
     */
    public String getName() {
        if (nickname != null) {
            return nickname;
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }
}
