package gdgoc.everyclub.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
public class User {

    @Column(nullable = false, unique = true)
    private final UUID publicId = UUID.randomUUID();
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
    private String studentNumber;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    public User(String name, String email, String studentNumber) {
        this.name = name;
        this.email = email;
        this.studentNumber = studentNumber;
    }

    public void update(String name, String studentNumber) {
        this.name = name;
        this.studentNumber = studentNumber;
    }
}
