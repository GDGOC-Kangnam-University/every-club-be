package gdgoc.everyclub.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;

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

    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @SuppressWarnings("unused") // Handled on @SQLDelete
    private LocalDateTime deletedAt;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void update(String name) {
        this.name = name;
    }
}
