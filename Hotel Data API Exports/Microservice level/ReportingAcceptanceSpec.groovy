package com.product.reporting

import com.core.service.UploadFileService
import com.gen.jooq.tables.pojos.VenueEntity
import com.gen.swagger.api.model.*
import com.product.AbstractIntegrationSpec
import com.product.api.hotels.model.HotelProviderAvailabilityResponse
import com.product.util.ElasticsearchTestsData
import com.product.util.Location
import com.product.util.TestDataHelper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import spock.lang.Shared

import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.stream.Collectors

import static org.assertj.core.api.Assertions.assertThat

class ReportingAcceptanceSpec extends AbstractIntegrationSpec{
    @Value('${hotelProvider.url.availability}')
    private String hotelProviderAvailabilityURL
    @Autowired private ElasticsearchTestsData esTestsData
    @Autowired private TestDataHelper testDataHelper
    @SpringBean(name = "hotelRestTemplate")
    private RestTemplate hotelRestTemplate = Mock()
    @SpringBean
    private UploadFileService uploadFileService = Mock()
    @Shared private List<Long> hotelReferences
    @Shared VenueEntity venue

    @Override
    void setupSpecWithInjectedBeans() {
        esTestsData.saveHotels()
        esTestsData.saveHotelProviderHotelsFrom('hotel_provider_116896_no_rooms.json')
        hotelReferences = testDataHelper.saveHotelProducts().collect { it.getId() }
        venue = testDataHelper.createVenues(Location.TORONTO)
    }

    @Override
    void cleanupSpecWithInjectedBeans() {
        esTestsData.deleteAllHotels()
        testDataHelper.deleteAllHotelProducts()
    }

    def "I want to check for a user input of just a venueCode and just longitude/latitude with or without a userKey/userSecret"(){
        given: "Hotels are saved in ES and DB"

        and: "user input provides either a venueCode or lat/long values"
        HttpEntity httpEntity = null
        1 * hotelRestTemplate.postForEntity("${hotelProviderAvailabilityURL}", _ as HttpEntity,  HotelProviderAvailabilityResponse.class) >> { arguments ->
            httpEntity = arguments.get(1) as HttpEntity
            ResponseEntity.ok(createHotelProviderAvailabilityResponseMock(List.of("48930", "49478", "116896"), 'DBL.QN-2'))
        }

        and: "upload report to azure storage is mocked"
        uploadFileService.uploadToAzureContainer(_ as byte[], _ as long, _ as String) >> "dummyLink"

        def hotelExportVM = new HotelExportByCoordinatesVM()
        hotelExportVM.setLatitude(latitude)
        hotelExportVM.setLongitude(longitude)
        hotelExportVM.setCheckIn(LocalDate.now())
        hotelExportVM.setCheckOut(LocalDate.now().plusDays(1))
        hotelExportVM.setVenueCode(venue.getPartnerVenueId())

        def customExportAttributesVM = new CustomExportByCoordinatesAttributesVM()
        customExportAttributesVM.setHotelProviderUserSecret(userSecret)
        customExportAttributesVM.setHotelProviderUserKey(userKey)
        customExportAttributesVM.setRadius(userRadius)

        def hotelExportRequestVM = new HotelExportByCoordinatesRequestVM()
        hotelExportRequestVM.setHotelExports(Arrays.asList(hotelExportVM))
        hotelExportRequestVM.setCustomExportAttributes(customExportAttributesVM)

        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        HttpEntity<HotelExportByCoordinatesRequestVM> request = new HttpEntity<>(hotelExportRequestVM, headers)

        when: "Exporting hotel statistics"
        def response = getDefaultRestTemplate().postForEntity(
                url("/api/hotels/exportHotelsByCoordinatesAsExcel"), request, Resource)

        then: "Export is OK"
        response.getStatusCode() == HttpStatus.OK
        assertThat(httpEntity.getHeaders().get("Api-Key")).isEqualTo(expectedUserKey)

        where:
        latitude     | longitude     | userSecret       | userKey    | userRadius   | expectedUserKey
        null         | null          | "mockSecret"     | "mockKey"  | 25           | ["mockKey"]
        "43.654686"  | "-79.386172"  | null             | null       | 40           | ["b2b-key"]
    }

    def "I want to check for a user input of just hotelCodes with or without a userKey/userSecret"(){
        given: "Hotels are saved in ES and DB"

        and: "user input provides hotel codes and check-in/check-out dates"
        HttpEntity httpEntity = null
        1 * hotelRestTemplate.postForEntity("${hotelProviderAvailabilityURL}", _ as HttpEntity,  HotelProviderAvailabilityResponse.class) >> { arguments ->
            httpEntity = arguments.get(1) as HttpEntity
            ResponseEntity.ok(createHotelProviderAvailabilityResponseMock(List.of("48930", "49478", "116896"), 'DBL.QN-2'))
        }

        and: "upload report to azure storage is mocked"
        uploadFileService.uploadToAzureContainer(_ as byte[], _ as long, _ as String) >> "dummyLink"

        def hotelExportVM = new HotelExportByHotelCodesVM()
        hotelExportVM.setHotelCodes(List.of(48930, 49478, 116896))
        hotelExportVM.setCheckIn(LocalDate.now())
        hotelExportVM.setCheckOut(LocalDate.now().plusDays(1))

        def customExportAttributesVM = new CustomExportByHotelCodesAttributesVM()
        customExportAttributesVM.setHotelProviderUserSecret(userSecret)
        customExportAttributesVM.setHotelProviderUserKey(userKey)

        def hotelExportRequestVM = new HotelExportByHotelCodesRequestVM()
        hotelExportRequestVM.setHotelExports(Arrays.asList(hotelExportVM))
        hotelExportRequestVM.setCustomExportAttributes(customExportAttributesVM)

        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        HttpEntity<HotelExportByHotelCodesRequestVM> request = new HttpEntity<>(hotelExportRequestVM, headers)

        when: "Exporting hotel statistics"
        def response = getDefaultRestTemplate().postForEntity(
                url("/api/hotels/exportHotelsByHotelCodesAsExcel"), request, Resource)

        then: "Export is OK"
        response.getStatusCode() == HttpStatus.OK
        assertThat(httpEntity.getHeaders().get("Api-Key")).isEqualTo(expectedUserKey)

        where:
        userSecret       | userKey       | expectedUserKey
        "mockSecret"     | "mockKey"     | ["mockKey"]
        null             | null          | ["b2b-key"]
    }

    static HotelProviderAvailabilityResponse createHotelProviderAvailabilityResponseMock(List<String> hotelCodes, String roomCode) {
        def hotels = hotelCodes.stream().map(hotelCode -> new HotelProviderAvailabilityResponse.Hotel(Integer.parseInt(hotelCode),
                List.of(new HotelProviderAvailabilityResponse.Room(roomCode, "DOUBLE QUEEN SIZE BED",
                        List.of(new HotelProviderAvailabilityResponse.Rate("20220615|20220616|W|1|265|DBT.ST|ID_B2B_62|BB|B2BXXXX|1~2~0||N@06~A-SIC~21a87~-78015892~N~~~NOR~057BCDEB739C440163577110615503AAUK0000001000100020521a87",
                                "NOR", "RECHECK", BigDecimal.TEN, BigDecimal.TEN.add(BigDecimal.ONE), true, 1, "", "",
                                false, "", "", List.of(new HotelProviderAvailabilityResponse.CancellationPolicy(BigDecimal.TEN, ZonedDateTime.now().toString(), null, null, null, null)), new HotelProviderAvailabilityResponse.Taxes(true, List.of(new HotelProviderAvailabilityResponse.Tax(true, BigDecimal.TEN, "USD", "TAXESANDFEES", null, null, null))), 1, 2, 1, "5")))), null, null, "USD")).collect(Collectors.toList())
        return new HotelProviderAvailabilityResponse(new HotelProviderAvailabilityResponse.Hotels(hotels, 1))
    }
}
