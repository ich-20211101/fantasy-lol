package com.fantasylol.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaguepediaClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${leaguepedia.username}")
    private String username;

    @Value("${leaguepedia.password}")
    private String password;

    private String sessionCookie;

    private static final String BASE_URL = "https://lol.fandom.com/api.php";

    public void login() throws Exception {

        String tokenUrl = BASE_URL + "?action=query&meta=tokens&type=login&format=json";
        ResponseEntity<String> tokenResponse = restTemplate.getForEntity(tokenUrl, String.class);

        List<String> cookies = tokenResponse.getHeaders().get(HttpHeaders.SET_COOKIE);

        if (cookies != null) {
            sessionCookie = String.join(";", cookies.stream()
                    .map(c -> c.split(";")[0])
                    .toList());
        }

        JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
        String loginToken = tokenJson.path("query").path("tokens").path("logintoken").asText();

        log.info("Login token: {}", loginToken);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.COOKIE, sessionCookie);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        formData.add("action", "login");
        formData.add("lgname", username);
        formData.add("lgpassword", password);
        formData.add("lgtoken", loginToken);
        formData.add("format", "json");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(BASE_URL, request, String.class);

        List<String> loginCookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);

        if (loginCookies != null) {
            sessionCookie = String.join(";", loginCookies.stream()
                    .map(c -> c.split(";")[0])
                    .toList());
        }

        log.info("Login response: {}", loginResponse.getBody());

    }

    public JsonNode cargoQuery(String tables, String fields, String where, int limit) throws Exception {
        return cargoQuery(tables, fields, where, null, limit);
    }

    public JsonNode cargoQuery(String tables, String fields, String where, String orderBy, int limit) throws Exception {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("action", "cargoquery")
                .queryParam("tables", tables)
                .queryParam("fields", fields)
                .queryParam("where", where)
                .queryParam("limit", limit)
                .queryParam("format", "json");

        if (orderBy != null && !orderBy.isBlank()) {
            builder.queryParam("order_by", orderBy);
        }

        String url = builder.build().toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, sessionCookie);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        return objectMapper.readTree(response.getBody());

    }

}
