package gdgoc.everyclub.user.domain;

import gdgoc.everyclub.user.dto.UserCreateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
public class User {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final UserRole role = UserRole.GUEST;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

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
    public User(String email, String nickname, String profileImageUrl,
                String department, String studentId, String phoneNumber, String bio) {
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.department = department;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.bio = bio;
    }

    public User(UserCreateRequest request) {
        this.email = request.email();
        this.nickname = request.nickname();
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

    /**
     * Returns the display name for the user.
     * Uses nickname if available, otherwise falls back to email prefix.
     */
    public String getName() {
        return nickname != null ? nickname : email.substring(0, email.indexOf('@'));
    }
}
