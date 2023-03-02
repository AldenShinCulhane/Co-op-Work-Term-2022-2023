package com.api.screenscraping;

import com.config.GlobalMapperConfig;
import com.gen.jooq.tables.pojos.ScreenScrapingAuthenticationEntity;
import com.gen.jooq.tables.pojos.ScreenScrapingHotelsEntity;
import com.gen.jooq.tables.records.ScreenScrapingAuthenticationRecord;
import com.gen.swagger.api.model.ScreenScrapingHotelSearchRequestVM;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface ScreenScrapingMapper {

  ScreenScrapingHotelsEntity hotelsResponseToEntity(ScreenScrapingHotelSearchResponse response);

  ScreenScrapingHotelSearchRequest vMToRequest(ScreenScrapingHotelSearchRequestVM requestVM);

  @Mapping(target = "username", source = "userName")
  ScreenScrapingAuthenticationRecord authenticationResponseToRecord(
          ScreenScrapingAuthenticationResponse authResponse);

  ScreenScrapingAuthenticationEntity authenticationRecordToEntity(
          ScreenScrapingAuthenticationRecord authRecord);
}
