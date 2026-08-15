package com.fantasylol.backend.service;

import com.fantasylol.backend.exception.LeaguepediaApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
    private Instant lastLoginAt;
    private static final Duration SESSION_TTL = Duration.ofMinutes(30);

    private static final String BASE_URL = "https://lol.fandom.com/api.php";

    public void login() throws Exception {

        if (sessionCookie != null && lastLoginAt != null && Duration.between(lastLoginAt, Instant.now()).compareTo(SESSION_TTL) < 0) {
            return;
        }

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

        lastLoginAt = Instant.now();

    }

    public JsonNode cargoQuery(String tables, String fields, String where, int limit) throws Exception {
        return cargoQuery(tables, fields, where, null, null, limit);
    }

    public JsonNode cargoQuery(String tables, String fields, String where, String orderBy, int limit) throws Exception {
        return cargoQuery(tables, fields, where, orderBy, null, limit);
    }

    public JsonNode cargoQuery(String tables, String fields, String where, String orderBy, String groupBy, int limit) throws Exception {
        return cargoQuery(tables, fields, where, orderBy, groupBy, limit, 0);
    }

    public JsonNode cargoQuery(String tables, String fields, String where, String orderBy, int limit, int offset) throws Exception {
        return cargoQuery(tables, fields, where, orderBy, null, limit, offset);
    }

    public JsonNode cargoQuery(String tables, String fields, String where, String orderBy, String groupBy, int limit, int offset) throws Exception {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL)
                .queryParam("action", "cargoquery")
                .queryParam("tables", tables)
                .queryParam("fields", fields)
                .queryParam("where", where)
                .queryParam("limit", limit)
                .queryParam("offset", offset)
                .queryParam("format", "json");

        if (orderBy != null && !orderBy.isBlank()) {
            builder.queryParam("order_by", orderBy);
        }

        if (groupBy != null && !groupBy.isBlank()) {
            builder.queryParam("group_by", groupBy);
        }

        String url = builder.build().toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, sessionCookie);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        JsonNode result = objectMapper.readTree(response.getBody());

        if (result.has("error")) {
            throw new LeaguepediaApiException("Leaguepedia API error: " + result.path("error").path("info").asText());
        }

        return result;

    }

    // 날짜 제한 없이 시즌 전체처럼 큰 범위를 조회할 때 사용 — limit(보통 500) 넘는 결과도 offset으로 끊어서 전부 긁어옴
    public List<JsonNode> cargoQueryAll(String tables, String fields, String where, String orderBy, int pageSize) throws Exception {

        List<JsonNode> all = new ArrayList<>();

        int offset = 0;

        while (true) {
            JsonNode response = cargoQueryWithRetry(tables, fields, where, orderBy, pageSize, offset);
            JsonNode page = response.path("cargoquery");

            if (page.isEmpty()) break;

            page.forEach(all::add);

            if (page.size() < pageSize) break;

            offset += pageSize;

            Thread.sleep(3000);
        }

        return all;

    }

    // rate limit(400) 걸리면 대기 후 최대 3번까지 재시도 — 데이터 많은 시즌 백필할 때 페이지네이션 도중 끊기는 것 방지
    private JsonNode cargoQueryWithRetry(String tables, String fields, String where, String orderBy, int limit, int offset) throws Exception {

        int attempts = 0;
        boolean relogged = false;

        while (true) {
            try {
                return cargoQuery(tables, fields, where, orderBy, limit, offset);
            } catch (HttpClientErrorException | LeaguepediaApiException e) {

                attempts++;

                if (attempts >= 5) {
                    log.error("Leaguepedia API 요청 5회 실패, 포기: {}", e.getMessage());
                    throw e;
                }

                if (!relogged) {
                    relogged = true;
                    log.warn("Leaguepedia API 요청 실패, 세션 만료 의심되어 재로그인 후 재시도: {}", e.getMessage());
                    lastLoginAt = null;
                    login();
                    continue;
                }

                long waitMs = 30000L * attempts;

                log.warn("Leaguepedia API 요청 실패 (시도 {}/5), {}ms 대기 후 재시도: {}", attempts, waitMs, e.getMessage());

                Thread.sleep(waitMs);

            }
        }

    }

}
