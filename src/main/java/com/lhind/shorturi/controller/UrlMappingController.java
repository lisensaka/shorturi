package com.lhind.shorturi.controller;

import com.lhind.shorturi.entity.dto.request.UrlRequestDto;
import com.lhind.shorturi.entity.dto.response.UrlResponseDto;
import com.lhind.shorturi.service.UrlMappingService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

import static com.lhind.shorturi.config.ConstantAppInfo.API_URL_MAPPING;

@RestController
@RequestMapping(API_URL_MAPPING)
@RequiredArgsConstructor
public class UrlMappingController {

    private final UrlMappingService urlMappingService;

    private static final Logger logger = LoggerFactory.getLogger(UrlMappingController.class);


    @PostMapping("/shorten-url")
    public ResponseEntity<UrlResponseDto> saveAndReturnShortenUrl(
            UrlRequestDto urlRequestDto,
            @RequestParam(required = false, defaultValue = "5") int expirationTimeInMinutes,
            Principal principal) throws Exception {
        logger.info("SaveAndReturnShortenUrl Api request: {} begin", urlRequestDto);
        var urlMapping = urlMappingService.saveOriginalUrlAndReturnUniqueShortUrl(urlRequestDto, expirationTimeInMinutes, principal.getName());
        logger.info("SaveAndReturnShortenUrl Api request: {} end", urlRequestDto);
        return ResponseEntity.ok(new UrlResponseDto(urlMapping.getShortUrl(), urlMapping.getExpirationTime()));
    }

    @GetMapping("/all-short-url")
    public ResponseEntity<?> getAllShortenUrl(Principal principal) {
        logger.info("GetAllShortenUrl Api request from user: {} begin", principal.getName());

        var allShortenUrl = urlMappingService.getAllShortenUrl(principal.getName());
        logger.info("GetAllShortenUrl Api request from user: {} end", principal.getName());

        return ResponseEntity.ok(allShortenUrl);
    }

    @GetMapping("/by-short-url/{shortUrl}")
    public ResponseEntity<?> getOriginalUrl(@PathVariable String shortUrl) {
        logger.info("GetOriginalUrl Api request by shortUrl: {} begin", shortUrl);

        var optionalUrlMapping = urlMappingService.findByShortUrl(shortUrl);
        if (optionalUrlMapping.isEmpty()) {
            return ResponseEntity.status(404).body(String.format("Shorten URL :%s  not found", shortUrl));
        }
        var originalUrlMapping = optionalUrlMapping.get();

        if (originalUrlMapping.getExpirationTime().isBefore(LocalDateTime.now())) {
            urlMappingService.deleteUrlMapping(originalUrlMapping);
            return ResponseEntity.status(HttpStatus.GONE).body("URL expired");
        }

        urlMappingService.incrementClickCount(originalUrlMapping);
        logger.info("GetOriginalUrl Api request by shortUrl: {} end", shortUrl);
        return ResponseEntity.ok(new UrlResponseDto(originalUrlMapping.getLongUrl(), originalUrlMapping.getExpirationTime()));
    }

}
