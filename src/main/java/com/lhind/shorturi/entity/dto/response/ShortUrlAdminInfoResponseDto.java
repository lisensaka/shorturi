package com.lhind.shorturi.entity.dto.response;

import java.time.LocalDateTime;

public record ShortUrlAdminInfoResponseDto(String url, LocalDateTime expirationDate, int clickCount) {
}
