package com.lifestyle.composite.api.reporting;

import com.client.HotelExportByCoordinatesRequest;
import com.client.HotelExportByHotelCodesRequest;
import com.client.HotelsReportDownloadLink;
import com.client.ReportingApi;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportingService {
  @NotNull private final ReportingApi reportingApi;

  @PreAuthorize("hasRole(T(com.lifestyle.security.Role).PRODUCT_VIEWER.getValue())")
  public HotelsReportDownloadLink exportHotelsByHotelCodes(
      HotelExportByHotelCodesRequest hotelExportRequest) {
    return reportingApi.exportHotelsByHotelCodesAsExcel(hotelExportRequest);
  }

  @PreAuthorize("hasRole(T(com.lifestyle.security.Role).PRODUCT_VIEWER.getValue())")
  public HotelsReportDownloadLink exportHotelsByCoordinates(
      HotelExportByCoordinatesRequest hotelExportRequest) {
    return reportingApi.exportHotelsByCoordinatesAsExcel(hotelExportRequest);
  }
}
