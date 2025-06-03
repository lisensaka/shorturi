package com.lhind.shorturi.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lhind.shorturi.config.security.JwtService;
import com.lhind.shorturi.entity.UrlMapping;
import com.lhind.shorturi.entity.dto.request.UrlRequestDto;
import com.lhind.shorturi.entity.dto.response.ShortUrlAdminInfoResponseDto;
import com.lhind.shorturi.entity.dto.response.UrlResponseDto;
import com.lhind.shorturi.service.UrlMappingService;
import com.lhind.shorturi.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static com.lhind.shorturi.config.ConstantAppInfo.API_URL_MAPPING;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UrlMappingController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class UrlMappingControllerTest {

    @Autowired
    public MockMvc mockMvc;

    @MockBean
    private UrlMappingService urlMappingService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;


    @Test
    void testSaveAndReturnShortenUrl() throws Exception {
//        Given
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        int expirationTimeInMinutes = 10;
        String loggedUsername = "lisen@test.al";

        UrlRequestDto request = new UrlRequestDto("https://example.com?param=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbvvvvvvvvvvvvvvvvvvvvvvvvvvv");

        UrlMapping mockMapping = new UrlMapping();
        mockMapping.setShortUrl("exmp_13");
        mockMapping.setLongUrl(request.url());
        mockMapping.setExpirationTime(now.plusMinutes(expirationTimeInMinutes));

//        When
        when(urlMappingService.saveOriginalUrlAndReturnUniqueShortUrl(request, expirationTimeInMinutes, loggedUsername))
                .thenReturn(mockMapping);

//        Then
        mockMvc.perform(post(API_URL_MAPPING+"/shorten-url")
//                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsaXNlbkB0ZXN0LmFsIiwicm9sZXMiOlt7ImF1dGhvcml0eSI6IlVTRVIifV0sImlhdCI6MTc0ODg4MTIxNywiZXhwIjoxNzQ4OTY3NjE3fQ.vi6WuYQW5Km7fx6tTdTKaouY3mF5m4AJ7iAJj9dehHQ")
                        .param("expirationTimeInMinutes", expirationTimeInMinutes + "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request))
                        .principal(() -> "lisen@test.al"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("exmp_13"))
                .andExpect(jsonPath("$.expirationDate").value(now.plusMinutes(expirationTimeInMinutes).toString()));
    }


    @Test
    void testGetAllShortenUrl_roleAdmin() throws Exception {
//        Given
        List generic = List.of(
                new ShortUrlAdminInfoResponseDto(
                        "exmp_13",
                        LocalDateTime.now().plusMinutes(5),
                        2
                ));

//        When
        when(urlMappingService.getAllShortenUrl("lisen@test.al")).thenReturn(generic);

//        Then
        mockMvc.perform(get(API_URL_MAPPING + "/all-short-url")
                        .principal(() -> "lisen@test.al"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].url").value("exmp_13"))
                .andExpect(jsonPath("$[0].clickCount").value("2"));
    }

    @Test
    void testGetAllShortenUrl_roleUser() throws Exception {
//        Given
        List generic = List.of(
                new UrlResponseDto(
                        "exmp_13",
                        LocalDateTime.now().plusMinutes(5)
                ),
                new UrlResponseDto(
                        "exmp_14",
                        LocalDateTime.now().plusMinutes(5)
                ));

//        When
        when(urlMappingService.getAllShortenUrl("lisen@test.al")).thenReturn(generic);

//        Then
        mockMvc.perform(get(API_URL_MAPPING + "/all-short-url")
                        .principal(() -> "lisen@test.al"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].url").value("exmp_13"))
                .andExpect(jsonPath("$[0].size()").value("2"));
    }

    @Test
    void testGetOriginalUrl_foundAndNotExpired() throws Exception {
        // Given
        UrlMapping mapping = new UrlMapping("exmp_13",
                "https://example.com?param=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbvvvvvvvvvvvvvvvvvvvvvvvvvvv",
                LocalDateTime.now().plusMinutes(10));

        // When
        when(urlMappingService.findByShortUrl("exmp_13")).thenReturn(Optional.of(mapping));

        // Then
        MvcResult result = mockMvc.perform(get(API_URL_MAPPING + "/by-short-url/exmp_13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(mapping.getLongUrl()))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseContent);
        String expirationDateStr = root.path("expirationDate").asText();
        LocalDateTime expirationDate = LocalDateTime.parse(expirationDateStr);

        assertTrue(expirationDate.isAfter(LocalDateTime.now()), "Expected expirationDate to be valid");
    }

    @Test
    void testGetOriginalUrl_notFound() throws Exception {
        when(urlMappingService.findByShortUrl("exmp_13")).thenReturn(Optional.empty());

        mockMvc.perform(get(API_URL_MAPPING + "/by-short-url/exmp_13"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetOriginalUrl_expired() throws Exception {
        UrlMapping expired = new UrlMapping("exmp_13", "https://example.com", LocalDateTime.now().minusMinutes(1));
        when(urlMappingService.findByShortUrl("exmp_13")).thenReturn(Optional.of(expired));

        mockMvc.perform(get(API_URL_MAPPING + "/by-short-url/exmp_13"))
                .andExpect(status().isGone())
                .andExpect(content().string("URL expired"));
    }

}