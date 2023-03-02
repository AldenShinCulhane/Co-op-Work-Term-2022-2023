package com.api.screenscraping;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScreenScrapingClient {
  @Value("${screenscraping.username}")
  private String username;

  @Value("${screenscraping.password}")
  private String password;

  @Value("${screenscraping.url}")
  private String url;

  @Value("${screenscraping.authenticationUrl}")
  private String authenticationUrl;

  private final RestTemplate restTemplate;

  public List<ScreenScrapingHotelSearchResponse> fetchHotels(
          ScreenScrapingHotelSearchRequest requestEntity, String authToken) {
    var request = new HttpEntity<>(requestEntity, getHeaders(authToken));
    var result =
        restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            new ParameterizedTypeReference<List<ScreenScrapingHotelSearchResponse>>() {});
    return result.getBody();
  }

  public ScreenScrapingAuthenticationResponse fetchAuthenticationResponse() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
    formParameters.add("grant_type", "password");
    formParameters.add("username", username);
    formParameters.add("password", password);

    var request = new HttpEntity<>(formParameters, headers);
    var result =
        restTemplate.postForEntity(
            authenticationUrl, request, ScreenScrapingAuthenticationResponse.class);

    return result.getBody();
  }

  public HttpHeaders getHeaders(String authToken) {
    var headers = new HttpHeaders();
    headers.setBearerAuth(authToken);
    return headers;
  }
}
