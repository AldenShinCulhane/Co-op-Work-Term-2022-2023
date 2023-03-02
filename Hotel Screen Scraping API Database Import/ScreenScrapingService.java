package com.api.screenscraping;

import com.gen.jooq.tables.records.ScreenScrapingAuthenticationRecord;
import com.gen.swagger.api.model.ScreenScrapingHotelSearchRequestVM;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreenScrapingService {
  @NonNull private final ScreenScrapingHotelsRepository screenScrapingHotelsRepository;
  @NonNull private final ScreenScrapingClient screenScrapingClient;
  @NonNull private final ScreenScrapingMapper screenScrapingMapper;

  public void fetchHotelsInsertInDatabase(ScreenScrapingHotelSearchRequestVM request) {
    log.debug(
        "Fetch data from screen scraping and insert hotels into the SCREEN_SCRAPING_HOTELS table in the database. Request {}",
        request);

    // Fetch hotels with valid authentication
    var hotelsToAdd =
            screenScrapingClient.fetchHotels(
            (screenScrapingMapper.vMToRequest(request)), fetchValidAuthentication().getAccessToken());

    // Add new hotels to database
    hotelsToAdd
        .stream()
        .filter(
            hotel ->
                    screenScrapingHotelsRepository.fetchHotels(hotel.getHotelcode().toString()) == null)
        .forEach(
            hotel ->
                    screenScrapingHotelsRepository.createScreenScrapingHotels(
                            screenScrapingMapper.hotelsResponseToEntity(hotel)));
  }

  private ScreenScrapingAuthenticationRecord fetchValidAuthentication() {
    var authToken = screenScrapingHotelsRepository.fetchAuthentication();
    if (authToken == null || hasExpired(authToken.getExpires())) {
      // Reauthorize
      var newAuthToken =
              screenScrapingMapper.authenticationResponseToRecord(
                      screenScrapingClient.fetchAuthenticationResponse());
      if (authToken == null) {
        // Insert new authentication credentials in database
        screenScrapingHotelsRepository.createScreenScrapingAuthentication(
                screenScrapingMapper.authenticationRecordToEntity(newAuthToken));
      } else {
        // Update authentication credentials in the database
        screenScrapingHotelsRepository.updateScreenScrapingAuthentication(
                screenScrapingMapper.authenticationRecordToEntity(newAuthToken));
      }
      authToken = newAuthToken;
    }
    return authToken;
  }

  private boolean hasExpired(String expiryDate) {
    try {
      // Sample format: "Fri, 14 Oct 2022 13:42:45 GMT"
      var formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
      var date = LocalDateTime.parse(expiryDate, formatter);
      return (date.compareTo(LocalDateTime.now()) < 0);
    } catch (DateTimeParseException e) {
      return true;
    }
  }
}
