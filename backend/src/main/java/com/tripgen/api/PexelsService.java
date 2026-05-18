package com.tripgen.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class PexelsService {

    private static final String PEXELS_SEARCH_URL = "https://api.pexels.com/v1/search";
    private static final int PER_PAGE = 15;

    @Value("${pexels.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public PexelsService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<String> fetchImages(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isBlank()) {
            return List.of();
        }

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[PEXELS_SKIP] API key is not configured yet.");
            return List.of();
        }

        int randomPage = new Random().nextInt(5) + 1;
        URI uri = UriComponentsBuilder
                .fromUriString(PEXELS_SEARCH_URL)
                .queryParam("query", searchQuery.trim())
                .queryParam("per_page", PER_PAGE)
                .queryParam("page", randomPage)
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            return extractImageUrls(response.getBody());
        } catch (Exception e) {
            System.out.println("[PEXELS_ERROR] query=" + searchQuery + ", reason=" + e.getMessage());
            return List.of();
        }
    }

    private List<String> extractImageUrls(String rawJson) throws Exception {
        if (rawJson == null || rawJson.isBlank()) {
            return List.of();
        }

        JsonNode root = objectMapper.readTree(rawJson);
        JsonNode photos = root.path("photos");
        Set<String> urls = new LinkedHashSet<>();

        if (photos.isArray()) {
            for (JsonNode photo : photos) {
                JsonNode src = photo.path("src");
                String large = src.path("large").asText("");
                String medium = src.path("medium").asText("");
                String imageUrl = !large.isBlank() ? large : medium;
                if (!imageUrl.isBlank()) {
                    urls.add(imageUrl);
                }
            }
        }

        return new ArrayList<>(urls);
    }
}
