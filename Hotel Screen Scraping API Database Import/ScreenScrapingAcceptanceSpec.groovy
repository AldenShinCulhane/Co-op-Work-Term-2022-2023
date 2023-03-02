package com.shipyard

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod

import static com.gen.jooq.Tables.SCREEN_SCRAPING_HOTELS
import com.gen.jooq.tables.pojos.ScreenScrapingHotelsEntity
import com.gen.swagger.api.model.ScreenScrapingHotelSearchRequestVM
import com.api.screenscraping.ScreenScrapingAuthenticationResponse
import com.api.screenscraping.ScreenScrapingHotelSearchResponse
import org.jooq.DSLContext
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate


class ScreenScrapingAcceptanceSpec extends AbstractIntegrationSpec {
    @Value('${screenscraping.url}')
    private String screenScrapingHotelSearchURL
    @Value('${screenscraping.authenticationUrl}')
    private String screenScrapingAuthenticationURL
    @SpringBean
    private RestTemplate screenScrapingMockedRestTemplate = Mock()
    @Autowired
    private DSLContext dsl

    def "I want to check for a user input of a hotel name to see if hotels are inserted/updated in DB"(){
        given: "Connection with screenscraping and DB are secure"

        and: "user input provides city and country"
        screenScrapingMockedRestTemplate.postForEntity("${screenScrapingAuthenticationURL}", _ as HttpEntity, ScreenScrapingAuthenticationResponse.class) >> { arguments ->
            ResponseEntity.ok(createScreenScrapingAuthenticationResponseMock())
        }

        def screenScrapingHotelResponseMock = createScreenScrapingHotelSearchResponseMock()
        screenScrapingMockedRestTemplate.exchange("${screenScrapingHotelSearchURL}", _ as HttpMethod, _ as HttpEntity, new ParameterizedTypeReference<List<ScreenScrapingHotelSearchResponse>>() {}) >> { arguments ->
            ResponseEntity.ok(screenScrapingHotelResponseMock)
        }

        when: "Inserting hotels data in the database"
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)

        def screenScrapingHotelsRequest = factory.manufacturePojo(screenScrapingHotelSearchRequestVM.class)
        HttpEntity<ScreenScrapingHotelSearchRequestVM> request = new HttpEntity<>(screenScrapingHotelsRequest, headers)

        def response = getDefaultRestTemplate().postForEntity(
                url("/api/screenscraping/importHotels"), request, Void)

        then: "Export is OK"
        response.getStatusCode() == HttpStatus.OK

        and: "Hotel is inserted"
        List<ScreenScrapingHotelsEntity> hotelSaved = dsl.select(SCREEN_SCRAPING_HOTELS.fields())
                .from(SCREEN_SCRAPING_HOTELS)
                .fetchInto(ScreenScrapingHotelsEntity.class)
        hotelSaved.size() == 1
        hotelSaved.get(0).getHotelname() == screenScrapingHotelResponseMock[0].getHotelname()
    }

    List<ScreenScrapingHotelSearchResponse> createScreenScrapingHotelSearchResponseMock(){
        def singleHotel = factory.manufacturePojo(ScreenScrapingHotelSearchResponse.class)
        singleHotel.setState("MI")
        return List.of(singleHotel)
    }
    ScreenScrapingAuthenticationResponse createScreenScrapingAuthenticationResponseMock(){
        return factory.manufacturePojo(ScreenScrapingAuthenticationResponse.class)
    }
}