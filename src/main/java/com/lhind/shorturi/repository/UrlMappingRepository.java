package com.lhind.shorturi.repository;

import com.lhind.shorturi.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping, String> {
    Optional<UrlMapping> findByShortUrl(String shortUrl);
    Optional<UrlMapping> findByLongUrlAndExpirationTimeAfter(String longUrl, LocalDateTime now);
    void deleteAllByExpirationTimeBefore(LocalDateTime now);

}