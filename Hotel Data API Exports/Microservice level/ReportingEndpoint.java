package com.product.api.reporting;

import com.gen.swagger.api.ReportingApi;
import com.gen.swagger.api.model.HotelExportByCoordinatesRequestVM;
import com.gen.swagger.api.model.HotelExportByHotelCodesRequestVM;
import com.gen.swagger.api.model.HotelsReportDownloadLinkVM;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ReportingEndpoint implements ReportingApi {
  @NonNull private final ReportingService reportingService;

  @Override
  public ResponseEntity<HotelsReportDownloadLinkVM> exportHotelsByCoordinatesAsExcel(
      HotelExportByCoordinatesRequestVM hotelExportInput) {
    var excelDownloadUrl = reportingService.writeToExcelByCoordinates(hotelExportInput);
    var excelDownloadObject = new HotelsReportDownloadLinkVM();
    excelDownloadObject.setDownloadLink(excelDownloadUrl);
    return ResponseEntity.ok(excelDownloadObject);
  }

  @Override
  public ResponseEntity<HotelsReportDownloadLinkVM> exportHotelsByHotelCodesAsExcel(
      HotelExportByHotelCodesRequestVM hotelExportInput) {
    var excelDownloadUrl = reportingService.writeToExcelByHotelCodes(hotelExportInput);
    var excelDownloadObject = new HotelsReportDownloadLinkVM();
    excelDownloadObject.setDownloadLink(excelDownloadUrl);
    return ResponseEntity.ok(excelDownloadObject);
  }
}
