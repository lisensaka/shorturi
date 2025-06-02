package com.lhind.shorturi.entity.dto.response;

import java.time.LocalDateTime;

public record UrlResponseDto(String url, LocalDateTime expirationDate) {
}
