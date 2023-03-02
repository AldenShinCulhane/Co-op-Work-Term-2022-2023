package com.product.api.reporting;

import com.config.errors.BadRequestException;
import com.core.service.UploadFileService;
import com.gen.swagger.api.model.*;
import com.product.api.hotels.HotelService;
import com.product.api.hotels.repository.HotelPartnerEnvConfig;
import com.product.api.hotels.repository.HotelSearchCriteria;
import com.product.api.service.VenueService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired, @Lazy}))
public class ReportingService {
  @NonNull private final HotelService hotelService;
  @NonNull private final VenueService venueService;
  @NotNull private final UploadFileService uploadFileService;

  public String writeToExcelByCoordinates(HotelExportByCoordinatesRequestVM hotelExportInput) {
    var outputStream = new ByteArrayOutputStream();
    try (Workbook spreadsheet = new XSSFWorkbook()) {
      // Receives hotel data from database and checkIns for the sheet name
      var hotelExports = hotelExportInput.getHotelExports();
      var customExportAttributesVM = hotelExportInput.getCustomExportAttributes();
      var hotelSearchAttributes =
          HotelSearchAttributes.builder()
              .hotelProviderUserKey(customExportAttributesVM.getHotelProviderUserKey())
              .hotelProviderUserSecret(customExportAttributesVM.getHotelProviderUserSecret())
              .radius(convertMilesToKm(customExportAttributesVM.getRadius()))
              .build();
      for (HotelExportByCoordinatesVM hotelExportVM : hotelExports) {
        var sheetDefinition = fetchHotelDataByCoordinates(hotelExportVM, hotelSearchAttributes);
        createSheetInFile(sheetDefinition, spreadsheet);
      }
      // Returns a servlet response that creates a download link for the Excel file
      spreadsheet.write(outputStream);
    } catch (IOException e) {
      log.error("I/O exception", e);
    }

    var fileContent = outputStream.toByteArray();
    var size = outputStream.size();
    return uploadFileService.uploadToAzureContainer(
        fileContent,
        size,
        LocalDate.now()
            + " "
            + hotelExportInput.getCustomExportAttributes().getDescription()
            + ".xlsx");
  }

  public String writeToExcelByHotelCodes(HotelExportByHotelCodesRequestVM hotelExportInput) {
    var outputStream = new ByteArrayOutputStream();
    try (Workbook spreadsheet = new XSSFWorkbook()) {
      // Receives hotel data from database, extracts checkIns for the sheet name
      var hotelExports = hotelExportInput.getHotelExports();
      var customExportAttributesVM = hotelExportInput.getCustomExportAttributes();
      var hotelSearchAttributes =
          HotelSearchAttributes.builder()
              .hotelProviderUserKey(customExportAttributesVM.getHotelProviderUserKey())
              .hotelProviderUserSecret(customExportAttributesVM.getHotelProviderUserSecret())
              .build();
      for (HotelExportByHotelCodesVM hotelExportVM : hotelExports) {
        var sheetDefinition = fetchHotelDataByHotelCodes(hotelExportVM, hotelSearchAttributes);
        createSheetInFile(sheetDefinition, spreadsheet);
      }
      // Returns a servlet response that creates a download link for the Excel file
      spreadsheet.write(outputStream);
    } catch (IOException e) {
      log.error("I/O exception", e);
    }

    var fileContent = outputStream.toByteArray();
    var size = outputStream.size();
    return uploadFileService.uploadToAzureContainer(
        fileContent,
        size,
        LocalDate.now()
            + " "
            + hotelExportInput.getCustomExportAttributes().getDescription()
            + ".xlsx");
  }

  private SheetDefinition fetchHotelDataByCoordinates(
      HotelExportByCoordinatesVM hotelExportVM, HotelSearchAttributes hotelSearchAttributes) {
    var sheetDefinition = new SheetDefinition();
    if (StringUtils.isBlank(hotelExportVM.getLatitude())
        || StringUtils.isBlank(hotelExportVM.getLongitude())) {
      var currentVenue = venueService.getVenueByPartnerVenueId(hotelExportVM.getVenueCode());
      currentVenue.ifPresentOrElse(
          venueEntity -> {
            sheetDefinition.setLatitude(venueEntity.getLatitude().toString());
            sheetDefinition.setLongitude(venueEntity.getLongitude().toString());
          },
          () -> log.debug("Venue not found in database: {}", hotelExportVM.getVenueCode()));
    } else {
      sheetDefinition.setLatitude(hotelExportVM.getLatitude());
      sheetDefinition.setLongitude(hotelExportVM.getLongitude());
    }

    sheetDefinition.setCheckIn(hotelExportVM.getCheckIn().toString());
    sheetDefinition.setCheckOut(hotelExportVM.getCheckOut().toString());
    sheetDefinition.setHotelsVM(
        hotelService.fetchHotels(
            HotelSearchCriteria.builder()
                .latitude(sheetDefinition.getLatitude())
                .longitude(sheetDefinition.getLongitude())
                .range(hotelSearchAttributes.getRadius())
                .checkin(hotelExportVM.getCheckIn())
                .checkout(hotelExportVM.getCheckOut())
                .roomCount(1)
                .adultCount(2)
                .childCount(0)
                .onlyActive(true)
                .limit(1000)
                .offset(0)
                .build(),
            HotelPartnerEnvConfig.builder()
                .apiKey(hotelSearchAttributes.getHotelProviderUserKey())
                .secret(hotelSearchAttributes.getHotelProviderUserSecret())
                .build()));

    return sheetDefinition;
  }

  private SheetDefinition fetchHotelDataByHotelCodes(
      HotelExportByHotelCodesVM hotelExportVM, HotelSearchAttributes hotelSearchAttributes) {
    var sheetDefinition = new SheetDefinition();
    sheetDefinition.setCheckIn(hotelExportVM.getCheckIn().toString());
    sheetDefinition.setCheckOut(hotelExportVM.getCheckOut().toString());
    sheetDefinition.setHotelsVM(
        hotelService.fetchHotels(
            HotelSearchCriteria.builder()
                .externalHotelCodes(hotelExportVM.getHotelCodes())
                .checkin(hotelExportVM.getCheckIn())
                .checkout(hotelExportVM.getCheckOut())
                .contract("GST")
                .roomCount(1)
                .adultCount(2)
                .childCount(0)
                .onlyActive(true)
                .limit(1000)
                .offset(0)
                .build(),
            HotelPartnerEnvConfig.builder()
                .apiKey(hotelSearchAttributes.getHotelProviderUserKey())
                .secret(hotelSearchAttributes.getHotelProviderUserSecret())
                .build()));
    return sheetDefinition;
  }

  private void createSheetInFile(SheetDefinition sheetDefinition, Workbook spreadsheet) {
    // All the hotel data, each inner List<String> represents a row in the spreadsheet
    List<List<String>> toExport = extractHotelDataByRow(sheetDefinition);

    // Sets the "sheet" name shown on the tabs at the bottom of the Excel file
    Sheet currentSheet = generateCurrentSheet(sheetDefinition, spreadsheet);

    // Formats the Excel file's header and column appearance and contents
    formatCurrentSheet(spreadsheet, currentSheet, toExport);
  }

  private List<List<String>> extractHotelDataByRow(SheetDefinition sheetDefinition) {
    List<List<String>> listOfRowInfo;
    listOfRowInfo =
        sheetDefinition
            .getHotelsVM()
            .getItems()
            .stream()
            .map(
                hotelVM ->
                    hotelVM
                        .getGuestRooms()
                        .stream()
                        .map(
                            roomVM ->
                                roomVM
                                    .getRoomRates()
                                    .stream()
                                    .map(
                                        rate ->
                                            getRoomRateAndHotelInfo(
                                                HotelInfo.builder()
                                                    .hotelVM(hotelVM)
                                                    .guestRoomVM(roomVM)
                                                    .roomRateVM(rate)
                                                    .build(),
                                                sheetDefinition))
                                    .collect(Collectors.toList()))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    return listOfRowInfo;
  }

  private List<String> getRoomRateAndHotelInfo(
      HotelInfo hotelInfo, SheetDefinition sheetDefinition) {
    List<String> currentRow =
        new ArrayList<>(); // Creates a list of strings with each index being each column's info in
    // the spreadsheet
    currentRow.add(hotelInfo.getHotelVM().getHotelName());
    currentRow.add(hotelInfo.getHotelVM().getExternalReferenceId());
    currentRow.add(hotelInfo.getHotelVM().getAwards().get(0).getRating());
    currentRow.add(hotelInfo.getHotelVM().getAddress().getCityName().toUpperCase());
    currentRow.add(hotelInfo.getHotelVM().getAddress().getStateCode());
    currentRow.add(hotelInfo.getHotelVM().getLatitude().toString().replace(".", ","));
    currentRow.add(hotelInfo.getHotelVM().getLongitude().toString().replace(".", ","));
    currentRow.add(hotelInfo.getHotelVM().getAddress().getZipCode());
    currentRow.add(hotelInfo.getHotelVM().getAddress().getAddressLine());
    currentRow.add(hotelInfo.getGuestRoomVM().getRoomCode());
    currentRow.add(hotelInfo.getGuestRoomVM().getRoomType().toUpperCase());
    currentRow.add(hotelInfo.getGuestRoomVM().getRoomName().toUpperCase());
    currentRow.add(
        hotelInfo
            .getRoomRateVM()
            .getInventory()
            .toString()); // .toString() method used to change the returned Integer to a String
    currentRow.add(hotelInfo.getRoomRateVM().getMealPlan());
    currentRow.add(
        hotelInfo
            .getRoomRateVM()
            .getPartnerPrice()
            .toString()); // .toString() method used to change the returned BigDecimal to a String
    currentRow.add(
        hotelInfo.getRoomRateVM().getSellingRate() != null
            ? hotelInfo.getRoomRateVM().getSellingRate().toString()
            : "");
    currentRow.add(hotelInfo.getRoomRateVM().getRateType().toString());
    currentRow.add(hotelInfo.getRoomRateVM().getRateClass());
    if (CollectionUtils.isEmpty(hotelInfo.getRoomRateVM().getFees())) {
      currentRow.add("");
      currentRow.add("");
    } else {
      currentRow.add(sumRateFees(hotelInfo.getRoomRateVM(), FeeFeeVM.IncludedEnum.INCLUDED));
      currentRow.add(sumRateFees(hotelInfo.getRoomRateVM(), FeeFeeVM.IncludedEnum.PAY_LATER));
    }
    currentRow.add(
        hotelInfo.getRoomRateVM().isAllFeesIncluded() != null
            ? hotelInfo.getRoomRateVM().isAllFeesIncluded().toString().toUpperCase()
            : "");
    currentRow.add(
        CollectionUtils.isEmpty(hotelInfo.getRoomRateVM().getCancellationPolicies())
            ? ""
            : hotelInfo.getRoomRateVM().getCancellationPolicies().get(0).getFrom());
    currentRow.add(
        CollectionUtils.isEmpty(hotelInfo.getRoomRateVM().getCancellationPolicies())
            ? ""
            : hotelInfo.getRoomRateVM().getCancellationPolicies().get(0).getAmount().toString());
    currentRow.add(sheetDefinition.getCheckIn());
    currentRow.add(sheetDefinition.getCheckOut());
    currentRow.add(LocalDate.now().toString());

    return currentRow;
  }

  private String sumRateFees(RoomRateVM rate, FeeFeeVM.IncludedEnum included) {
    return rate.getFees()
        .stream()
        .map(FeeVM::getFee)
        .filter(feeFeeVM -> feeFeeVM.getIncluded() == included)
        .map(FeeFeeVM::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .toString();
  }

  private Sheet generateCurrentSheet(SheetDefinition sheetDefinition, Workbook spreadsheet) {
    String definitionName =
        sheetDefinition.getSheetName(); // Parse and format date from check-in input
    int count = 0;

    boolean duplicateNameEncountered; // Flag for if a duplicate check-in date is found
    do {
      duplicateNameEncountered = false;
      String currentSheetName =
          definitionName
              + (count == 0
                  ? ""
                  : " - "
                      + count); // Update the current sheet name with an incremented count to check
      // for duplicates
      for (Sheet currentSheet : spreadsheet) {
        if (currentSheet
            .getSheetName()
            .equals(currentSheetName)) { // If the check-in dates are the same add to the duplicate
          // count
          count++;
          duplicateNameEncountered = true;
        }
      }
    } while (duplicateNameEncountered);
    var sheetName =
        definitionName
            + (count == 0
                ? ""
                : " - " + count); // Generate the sheet name with an incremented count to prevent
    // duplicate sheet names
    return spreadsheet.createSheet(sheetName);
  }

  private void formatCurrentSheet(
      Workbook spreadsheet, Sheet currentSheet, List<List<String>> toExport) {
    // Formatting columns and headers in spreadsheet
    String[] columnHeaders = {
      "Hotel Name",
      "Hotel Code",
      "Rating",
      "City",
      "State",
      "Latitude",
      "Longitude",
      "Zip Code",
      "Address",
      "Room Code",
      "Room Type",
      "Room Name",
      "Allotment",
      "Board Code",
      "Net Rate",
      "Selling Rate",
      "Rate Type",
      "Rate Class",
      "Included Tax",
      "Excluded Tax",
      "Tax All Included",
      "Cancellation From",
      "Cancellation Price",
      "Check-in Date",
      "Check-out Date",
      "Date Pulled"
    };
    Font headerFont = spreadsheet.createFont();
    headerFont.setBold(true);
    headerFont.setColor(IndexedColors.BLACK.index);
    headerFont.setFontHeightInPoints((short) 12);
    CellStyle headerStyle = spreadsheet.createCellStyle();
    headerStyle.setFont(headerFont);
    headerStyle.setFillPattern(FillPatternType.NO_FILL);
    Row headerRow = currentSheet.createRow(0);

    for (int cell = 0; cell < columnHeaders.length; cell++) {
      Cell currentCell = headerRow.createCell(cell);
      currentCell.setCellValue(columnHeaders[cell]);
      currentCell.setCellStyle(headerStyle);
    }

    // Populating columns with row info
    int rowNum = 1;
    for (List<String> rowInfo :
        toExport) { // Goes through each list of rows, extracts their data into columns
      Row row = currentSheet.createRow(rowNum++);
      for (int columnInfo = 0; columnInfo <= columnHeaders.length - 1; columnInfo++) {
        row.createCell(columnInfo)
            .setCellValue(
                rowInfo.get(
                    columnInfo)); // Sets the first->twelfth column's data: Hotel Name->SellingRate
      }
    }

    for (int cell = 0;
        cell < columnHeaders.length;
        cell++) { // Changes the size of the columns accordingly
      currentSheet.autoSizeColumn(cell);
    }
  }

  public Integer convertMilesToKm(Integer km) {
    return ((int) Math.round(km * 1.60934));
  }

  @Data
  @NoArgsConstructor
  private static class SheetDefinition {
    private String checkIn;
    private String checkOut;
    private String latitude;
    private String longitude;
    private HotelsVM hotelsVM;

    private String getSheetName() {
      try {
        DateFormat checkInFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = checkInFormat.parse(this.checkIn);
        DateFormat excelFormat = new SimpleDateFormat("EEE MMM dd");
        return excelFormat.format(date).replaceAll("[.]", "");

      } catch (ParseException e) {
        log.debug("Parse exception", e);
        throw new BadRequestException("Invalid checkIn date.");
      }
    }
  }

  @Builder
  @Getter
  private static class HotelSearchAttributes {
    private String hotelProviderUserKey;
    private String hotelProviderUserSecret;
    private Integer radius;
  }

  @Builder
  @Getter
  private static class HotelInfo {
    private HotelVM hotelVM;
    private GuestRoomVM guestRoomVM;
    private RoomRateVM roomRateVM;
  }
}
