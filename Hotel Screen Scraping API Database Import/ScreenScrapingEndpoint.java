package com.api.screenscraping;

import com.config.errors.BadRequestException;
import com.gen.swagger.api.ScreenScrapingApi;
import com.gen.swagger.api.model.ScreenScrapingHotelSearchRequestVM;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ScreenScrapingEndpoint implements ScreenScrapingApi {
  @NonNull private final ScreenScrapingService screenScrapingService;

  @Override
  public ResponseEntity<Void> importHotels(ScreenScrapingHotelSearchRequestVM request) {
    if (StringUtils.isBlank(request.getHotelname()) && StringUtils.isBlank(request.getCountry())) {
      throw new BadRequestException("Either a hotel name or a country should be provided.");
    }
    screenScrapingService.fetchHotelsInsertInDatabase(request);
    return ResponseEntity.ok().build();
  }
}
