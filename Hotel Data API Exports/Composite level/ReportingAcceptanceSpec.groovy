package com.lifestyle.composite

import com.lifestyle.gen.swagger.api.model.HotelExportRequestVM
import com.lifestyle.gen.swagger.api.model.HotelsReportDownloadLinkVM
import com.lifestyle.security.Role
import com.client.HotelExportByCoordinatesRequest
import com.client.HotelExportByHotelCodesRequest
import com.client.HotelsReportDownloadLink
import com.client.ReportingApi
import org.spockframework.spring.SpringBean
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import static org.assertj.core.api.Assertions.assertThat

class ReportingAcceptanceSpec extends AbstractIntegrationSpec {
    @SpringBean ReportingApi reportingApi = Mock()

    def "I want to export hotels by coordinates and check-in/check-out dates with the proper user role"(){
        setCurrentUser(roles: Role.PRODUCT_VIEWER)
        given: "Hotels are saved in ES and DB"
        def hotelExportRequestVM = factory.manufacturePojo(HotelExportRequestVM)
        hotelExportRequestVM.getHotelExports().forEach(hotelExport -> hotelExport.setHotelCodes(null))

        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def request = new HttpEntity<>(hotelExportRequestVM, headers)
        def hotelsReportDownloadLink = factory.manufacturePojo(HotelsReportDownloadLink)

        reportingApi.exportHotelsByCoordinatesAsExcel(_ as HotelExportByCoordinatesRequest) >> {
            arguments ->
                def exportCheck = arguments.get(0) as HotelExportByCoordinatesRequest
                assertThat(hotelExportRequestVM).usingRecursiveComparison().ignoringActualNullFields().isEqualTo(exportCheck)
                return hotelsReportDownloadLink
        }
        when: "Calling the exportHotelsByCoordinatesAsExcel method"
        ResponseEntity<HotelsReportDownloadLinkVM> response = getDefaultRestTemplate().postForEntity("/api/reporting/export", request, HotelsReportDownloadLinkVM)

        then: "Hotels report download url is returned"
        response.getStatusCode() == HttpStatus.OK
        response.getBody().getDownloadLink() == hotelsReportDownloadLink.getDownloadLink()
    }

    def "I want to export hotels by coordinates and check-in/check-out dates without the proper user role"(){
        given: "Hotels are saved in ES and DB"
        def hotelExportRequestVM = factory.manufacturePojo(HotelExportRequestVM)
        hotelExportRequestVM.getHotelExports().forEach(hotelExport -> hotelExport.setHotelCodes(null))

        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        def request = new HttpEntity<>(hotelExportRequestVM, headers)

        when: "Calling the exportHotelsByCoordinatesAsExcel method"
        ResponseEntity<HotelsReportDownloadLinkVM> response = getDefaultRestTemplate().postForEntity("/api/reporting/export", request, HotelsReportDownloadLinkVM)

        then: "Hotels report download url is returned"
        response.getStatusCode() == HttpStatus.FORBIDDEN
        0 * reportingApi._
    }

    def "I want to export hotels by hotel codes and check-in/check-out dates with the proper user role"(){
        setCurrentUser(roles: Role.PRODUCT_VIEWER)
        given: "Hotels are saved in ES and DB"
        def hotelExportRequestVM = factory.manufacturePojo(HotelExportRequestVM)
        hotelExportRequestVM.getHotelExports().forEach(hotelExport -> {
            hotelExport.setHotelCodes([12345, 54321])
            hotelExport.setLatitude(null)
            hotelExport.setLongitude(null)
            hotelExport.setVenueCode(null)
        })
        hotelExportRequestVM.getCustomExportAttributes().setRadius(null)

        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<HotelExportRequestVM> request = new HttpEntity<>(hotelExportRequestVM, headers)
        def hotelsReportDownloadLink = factory.manufacturePojo(HotelsReportDownloadLink)

        reportingApi.exportHotelsByHotelCodesAsExcel(_ as HotelExportByHotelCodesRequest) >> {
            arguments ->
                def exportCheck = arguments.get(0) as HotelExportByHotelCodesRequest
                assertThat(hotelExportRequestVM).usingRecursiveComparison().ignoringActualNullFields().isEqualTo(exportCheck)
                return hotelsReportDownloadLink
        }
        when: "Calling the exportHotelsByCoordinatesAsExcel method"
        ResponseEntity<HotelsReportDownloadLinkVM> response = getDefaultRestTemplate().postForEntity("/api/reporting/export", request, HotelsReportDownloadLinkVM)

        then: "Hotels report download url is returned"
        response.getStatusCode() == HttpStatus.OK
        response.getBody().getDownloadLink() == hotelsReportDownloadLink.getDownloadLink()
    }

    def "I want to export hotels by hotel codes and check-in/check-out dates without the proper user role"(){
        given: "Hotels are saved in ES and DB"
        def hotelExportRequestVM = factory.manufacturePojo(HotelExportRequestVM)
        hotelExportRequestVM.getHotelExports().forEach(hotelExport -> {
            hotelExport.setHotelCodes([12345, 54321])
            hotelExport.setLatitude(null)
            hotelExport.setLongitude(null)
            hotelExport.setVenueCode(null)
        })
        hotelExportRequestVM.getCustomExportAttributes().setRadius(null)

        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity<HotelExportRequestVM> request = new HttpEntity<>(hotelExportRequestVM, headers)

        when: "Calling the exportHotelsByCoordinatesAsExcel method"
        ResponseEntity<HotelsReportDownloadLinkVM> response = getDefaultRestTemplate().postForEntity("/api/reporting/export", request, HotelsReportDownloadLinkVM)

        then: "Hotels report download url is returned"
        response.getStatusCode() == HttpStatus.FORBIDDEN
        0 * reportingApi._
    }
}