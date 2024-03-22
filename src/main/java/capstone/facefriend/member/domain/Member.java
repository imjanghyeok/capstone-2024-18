package capstone.facefriend.member.domain;

import capstone.facefriend.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Getter
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Slf4j
public class Member extends BaseEntity {

    private static final int EMAIL_MASKING_LENGTH = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String imageUrl;

    @Column(unique = true)
    private String phone;

    @Column
    private boolean isVerified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public Member(String email) {
        this.email = email;
        this.role = Role.USER;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isSame(Long id) {
        return this.id.equals(id);
    }

    public String maskEmail() {
        return this.email.charAt(0) + "*".repeat(EMAIL_MASKING_LENGTH) + this.email.substring(EMAIL_MASKING_LENGTH + 1);
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setIsVerified(boolean isVerified) {
        log.info("[ Member ] 현재 isVerified = {}", this.isVerified);
        log.info("[ Member ] 들어온 isVerified = {}", isVerified);
        this.isVerified = isVerified;
    }

    public boolean isVerified() {
        return this.isVerified == true;
    }
}