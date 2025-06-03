package com.lhind.shorturi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "url_mapping")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String shortUrl;

    @Column(nullable = false, length = 2048)
    private String longUrl;

    @Column(nullable = false)
    private LocalDateTime expirationTime;

    @Column(nullable = false)
    private Integer clickCount = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String createdBy;

    private String updatedBy;

    public UrlMapping(String shortUrl, String longUrl, LocalDateTime expirationTime) {
        this.shortUrl = shortUrl;
        this.longUrl = longUrl;
        this.expirationTime = expirationTime;
    }
}
