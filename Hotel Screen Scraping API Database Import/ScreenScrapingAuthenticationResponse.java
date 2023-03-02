package com.api.screenscraping;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScreenScrapingAuthenticationResponse {
  @JsonProperty("access_token")
  String accessToken;

  @JsonProperty("token_type")
  String tokenType;

  @JsonProperty("expires_in")
  int expiresIn;

  String userName;

  @JsonProperty(".issued")
  String issued;

  @JsonProperty(".expires")
  String expires;
}
