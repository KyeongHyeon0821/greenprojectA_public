package com.example.greenprojectA.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.example.greenprojectA.constant.Role;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "mid", length = 20, unique = true, nullable = false)
    private String mid;  // 로그인 아이디

    @Column(nullable = false)
    private String username;  // 실명

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 13)
    private String tel;

    @Column(length = 255)
    private String address;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "quit_requested_at")
    private LocalDateTime quitRequestedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
