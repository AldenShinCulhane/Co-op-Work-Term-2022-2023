package com.api.screenscraping;

import lombok.Value;

@Value
public class ScreenScrapingHotelSearchRequest {
  String hotelname;
  String country;
  String city;
  String state;
  String zip;
  String keyword;
}
