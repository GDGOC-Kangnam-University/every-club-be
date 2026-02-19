package gdgoc.everyclub.user.domain;

import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.user.dto.UserCreateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

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

    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;

    @ManyToMany
    @JoinTable(
            name = "club_likes",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "club_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "club_id"})
    )
    private Set<Club> likedClubs = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @SuppressWarnings("unused") // Handled on @SQLDelete
    private LocalDateTime deletedAt;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public User(UserCreateRequest request) {
        this.name = request.name();
        this.email = request.email();
    }
    public void update(String name) {
        this.name = name;
    }
}
