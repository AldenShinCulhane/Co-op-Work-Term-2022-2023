package com.api.screenscraping;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ScreenScrapingHotelSearchResponse {
  Integer hotelcode;
  String hotelname;
  String address;
  String city;
  String state;
  String zip;
  String country;
  String starrating;
  BigDecimal lat;
  BigDecimal lng;
}
