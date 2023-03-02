package com.lifestyle.composite.api.reporting.mapping;

import com.lifestyle.config.GlobalMapperConfig;
import com.lifestyle.gen.swagger.api.model.HotelExportRequestVM;
import com.lifestyle.gen.swagger.api.model.HotelsReportDownloadLinkVM;
import com.client.HotelExportByCoordinatesRequest;
import com.client.HotelExportByHotelCodesRequest;
import com.client.HotelsReportDownloadLink;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ReportingMapper {
  public abstract HotelExportByCoordinatesRequest vmToCoordinatesClient(
      HotelExportRequestVM hotelExportRequestVM);

  public abstract HotelExportByHotelCodesRequest vmToHotelCodesClient(
      HotelExportRequestVM hotelExportRequestVM);

  public abstract HotelsReportDownloadLinkVM toVM(
      HotelsReportDownloadLink hotelsReportDownloadLink);
}
