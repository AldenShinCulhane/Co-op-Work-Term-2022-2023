package com.api.screenscraping;

import static com.gen.jooq.Tables.SCREEN_SCRAPING_AUTHENTICATION;
import static com.gen.jooq.Tables.SCREEN_SCRAPING_HOTELS;

import com.gen.jooq.tables.pojos.ScreenScrapingAuthenticationEntity;
import com.gen.jooq.tables.pojos.ScreenScrapingHotelsEntity;
import com.gen.jooq.tables.records.ScreenScrapingAuthenticationRecord;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ScreenScrapingHotelsRepository {
  @NotNull private final DSLContext dsl;

  public ScreenScrapingHotelsEntity fetchHotels(String hotelCode) {
    return dsl.selectDistinct(
                    SCREEN_SCRAPING_HOTELS.ID,
                    SCREEN_SCRAPING_HOTELS.HOTELCODE,
                    SCREEN_SCRAPING_HOTELS.HOTELNAME,
                    SCREEN_SCRAPING_HOTELS.ADDRESS,
                    SCREEN_SCRAPING_HOTELS.CITY,
                    SCREEN_SCRAPING_HOTELS.STATE,
                    SCREEN_SCRAPING_HOTELS.ZIP,
                    SCREEN_SCRAPING_HOTELS.COUNTRY,
                    SCREEN_SCRAPING_HOTELS.STARRATING,
                    SCREEN_SCRAPING_HOTELS.LAT,
                    SCREEN_SCRAPING_HOTELS.LNG,
                    SCREEN_SCRAPING_HOTELS.CREATED,
                    SCREEN_SCRAPING_HOTELS.MODIFIED)
        .from(SCREEN_SCRAPING_HOTELS)
        .where(SCREEN_SCRAPING_HOTELS.HOTELCODE.equalIgnoreCase(hotelCode))
        .fetchOneInto(ScreenScrapingHotelsEntity.class);
  }

  public void createScreenScrapingHotels(ScreenScrapingHotelsEntity toCreate) {
    var hotelsRecord = dsl.newRecord(SCREEN_SCRAPING_HOTELS, toCreate);
    hotelsRecord.store();
  }

  public ScreenScrapingAuthenticationRecord fetchAuthentication() {
    return dsl.selectFrom(SCREEN_SCRAPING_AUTHENTICATION).fetchAny();
  }

  public void updateScreenScrapingAuthentication(ScreenScrapingAuthenticationEntity toUpdate) {
    dsl.update(SCREEN_SCRAPING_AUTHENTICATION)
        .set(SCREEN_SCRAPING_AUTHENTICATION.ACCESS_TOKEN, toUpdate.getAccessToken())
        .set(SCREEN_SCRAPING_AUTHENTICATION.TOKEN_TYPE, toUpdate.getTokenType())
        .set(SCREEN_SCRAPING_AUTHENTICATION.EXPIRES_IN, toUpdate.getExpiresIn())
        .set(SCREEN_SCRAPING_AUTHENTICATION.USERNAME, toUpdate.getUsername())
        .set(SCREEN_SCRAPING_AUTHENTICATION.ISSUED, toUpdate.getIssued())
        .set(SCREEN_SCRAPING_AUTHENTICATION.EXPIRES, toUpdate.getExpires())
        .where(SCREEN_SCRAPING_AUTHENTICATION.USERNAME.eq(toUpdate.getUsername()))
        .execute();
  }

  public void createScreenScrapingAuthentication(ScreenScrapingAuthenticationEntity toCreate) {
    var authenticationRecord = dsl.newRecord(SCREEN_SCRAPING_AUTHENTICATION, toCreate);
    authenticationRecord.store();
  }
}
