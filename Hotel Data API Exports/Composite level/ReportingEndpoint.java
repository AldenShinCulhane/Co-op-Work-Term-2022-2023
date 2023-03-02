package com.lifestyle.composite.api.reporting;

import com.lifestyle.composite.api.reporting.mapping.ReportingMapper;
import com.lifestyle.gen.swagger.api.ReportingApi;
import com.lifestyle.gen.swagger.api.model.HotelExportRequestVM;
import com.lifestyle.gen.swagger.api.model.HotelsReportDownloadLinkVM;
import com.client.HotelsReportDownloadLink;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportingEndpoint implements ReportingApi {
  @NotNull private final ReportingService reportingService;
  @NotNull private final ReportingMapper mapper;

  @Override
  public ResponseEntity<HotelsReportDownloadLinkVM> exportHotelsAsExcel(
      HotelExportRequestVM hotelExport) {
    var downloadLink = new HotelsReportDownloadLink();
    if (hotelExport.getHotelExports().get(0).getHotelCodes()
        == null) { // Export by list of coordinates and check-in dates
      downloadLink =
          reportingService.exportHotelsByCoordinates(mapper.vmToCoordinatesClient(hotelExport));
    } else { // Export by list of hotel codes and check-in dates
      downloadLink =
          reportingService.exportHotelsByHotelCodes(mapper.vmToHotelCodesClient(hotelExport));
    }
    return ResponseEntity.ok(mapper.toVM(downloadLink));
  }
}
