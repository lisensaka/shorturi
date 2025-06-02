package com.lhind.shorturi.service;

import com.lhind.shorturi.entity.UrlMapping;
import com.lhind.shorturi.entity.User;
import com.lhind.shorturi.entity.dto.request.UrlRequestDto;
import com.lhind.shorturi.entity.dto.response.ShortUrlAdminInfoResponseDto;
import com.lhind.shorturi.entity.dto.response.UrlResponseDto;
import com.lhind.shorturi.repository.UrlMappingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UrlMappingService {
    private final UrlMappingRepository urlMappingRepository;

    private static final Logger logger = LoggerFactory.getLogger(UrlMappingService.class);
    private final UserService userService;


    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void deleteExpiredUrls() {
        logger.info("Cron job execution begin");

        urlMappingRepository.deleteAllByExpirationTimeBefore(LocalDateTime.now());

        logger.info("Cron job execution end");
    }

    public UrlMapping saveOriginalUrlAndReturnUniqueShortUrl(UrlRequestDto urlRequestDto, int expirationTimeInMinutes, String loggedUsername) throws Exception {

        LocalDateTime now = LocalDateTime.now();
        try {

            Optional<UrlMapping> optionalUrlMappingByLongUrlAndActive = urlMappingRepository.findByLongUrlAndExpirationTimeAfter(urlRequestDto.url(), now);
            if (optionalUrlMappingByLongUrlAndActive.isPresent()) {
                UrlMapping existingUrlMapping = optionalUrlMappingByLongUrlAndActive.get();
                existingUrlMapping.setExpirationTime(LocalDateTime.now().plusMinutes(expirationTimeInMinutes <= 0 ? 5 : expirationTimeInMinutes));
                existingUrlMapping.setUpdatedBy(loggedUsername);
                return urlMappingRepository.save(existingUrlMapping);
            }

            return saveUrlMapping(urlRequestDto, expirationTimeInMinutes, loggedUsername);

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private UrlMapping saveUrlMapping(UrlRequestDto urlRequestDto, int expirationTimeInMinutes, String loggedUsername) throws Exception {
        try {
            UrlMapping urlMapping = UrlMapping
                    .builder()
                    .longUrl(urlRequestDto.url())
                    .clickCount(0)
                    .shortUrl(getShortUrlUniqueGenerated(urlRequestDto.url()))
                    .expirationTime(LocalDateTime.now().plusMinutes(expirationTimeInMinutes))
                    .createdAt(LocalDateTime.now())
                    .createdBy(loggedUsername)
                    .build();

            urlMappingRepository.save(urlMapping);
            return urlMapping;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            String message = e.getMessage();
            if (e.getMessage().contains("value too long for type character varying(2048)")) {
                message = ("The URL that was sent exceeds the default max length for URLs. ");
            }
            throw new Exception(message);

        }
    }

    private LocalDateTime calculateExpirationTime(int expirationTimeInMinutes) {
        return (expirationTimeInMinutes == 0) ? LocalDateTime.now().plusMinutes(5) : LocalDateTime.now().plusMinutes(expirationTimeInMinutes);
    }

    public String getShortUrlUniqueGenerated(String longUrl) {
        String shortUrl = generateShortUrl(longUrl);
        int attempt = 0;

        while (urlMappingRepository.findByShortUrl(shortUrl).isPresent()) {
            shortUrl = generateShortUrl(longUrl + (++attempt));
        }

        return shortUrl;
    }

    private String generateShortUrl(String originalUrl) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(originalUrl.getBytes(StandardCharsets.UTF_8));

            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

            return encoded.substring(0, 10);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating short URL", e);
        }
    }

    public Optional<UrlMapping> findByShortUrl(String shortUrl) {
        return urlMappingRepository.findByShortUrl(shortUrl);
    }

    void findByLongUrl(String longUrl) {
        Optional<UrlMapping> byLongUrlAndExpirationTimeAfter = urlMappingRepository.findByLongUrlAndExpirationTimeAfter(longUrl, LocalDateTime.now());
    }

    public void deleteUrlMapping(UrlMapping urlMapping) {
        try {
            urlMappingRepository.delete(urlMapping);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void incrementClickCount(UrlMapping urlMapping) {
        urlMapping.setClickCount(urlMapping.getClickCount() + 1);
        urlMappingRepository.save(urlMapping);
    }

    public List<?> getAllShortenUrl(String username) {
        User byUsername = userService.findByUsername(username);
        if (byUsername.getRole().name().equalsIgnoreCase("ADMIN")) {
            return urlMappingRepository.findAll()
                    .stream()
                    .map(UrlMappingService::convertToShortUrlAdminInfoResponseDto)
                    .collect(Collectors.toList());
        } else {
            return urlMappingRepository.findAll()
                    .stream()
                    .map(UrlMappingService::convertToUrlResponseDto)
                    .collect(Collectors.toList());

        }


    }

    private static ShortUrlAdminInfoResponseDto convertToShortUrlAdminInfoResponseDto(UrlMapping urlMapping) {
        return new ShortUrlAdminInfoResponseDto(urlMapping.getShortUrl(), urlMapping.getExpirationTime(), urlMapping.getClickCount());
    }

    private static UrlResponseDto convertToUrlResponseDto(UrlMapping urlMapping) {
        return new UrlResponseDto(urlMapping.getShortUrl(), urlMapping.getExpirationTime());
    }
}
