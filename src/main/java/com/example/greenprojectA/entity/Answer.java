package com.example.greenprojectA.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "inquiry_id")
    private Inquiry inquiry;

    @Lob
    private String content;

    private LocalDateTime answeredAt;

    @PrePersist
    protected void onCreate() {
        this.answeredAt = LocalDateTime.now();
    }
}
