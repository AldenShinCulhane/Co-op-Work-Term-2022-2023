# Co-op-Work-Term-2022-2023
Main projects from my 2022-2023 8 month full-time co-op work term doing backend Java development as a Junior Developer at Global Solutions Team, 
around 11 months in total including working 3 months part-time.

## Hotel Data API Exports
###### Microservice Level
This Excel file export service was used to interact and send specific requests to the developer API of my company’s hotel provider partner.
The purpose of this program was to then export information received from their API about hotel rooms into an Excel file,
most importantly rates, for the business’ analysis of how to price hotel rooms. This was a program that I used to export 10-15 Excel files
daily and added features including new fields, new requests and new interactions with the hotel provider’s API throughout the co-op term.

**ReportingEndpoint.java:**
API endpoints utilizing Swagger API to access functionality for either an export of hotel data by an input of hotel codes and dates, or by an 
input of coordinates/venue codes and dates. Each endpoint respectively returns a download link to the outputted Excel file that was uploaded
to an Azure storage blob container by the service.

**ReportingService.java:**
The service level of the task called by the endpoint, this is where the majority of the file formatting and data parsing using Java streams is done.
This is also where a request (or multiple) is created to send to the hotel provider's API in a separate class based on the user's specific input, 
including api keys and secrets.

**ReportingAcceptanceSpec.groovy:**
These are two groovy unit tests that are designed to test both of the endpoints in ReportingEndpoint.java with varying inputs. They test the endpoints
with and without api key and secrets to test if local environment variables are used. They also test the endpoints with either coordinates, venue
codes or hotel codes as a mocked user input. This file mocks the API interactions that the service has with the hotel provider and the entire user 
process, it also has a method to mock the hotel provider's API response.


###### Composite Level
This task was programmed to implement the microservice's functionality into our backend team's main composite-service. This allows for the business side,
frontend development team and QA team to access the functionality within this service which was only available to the backend team members.
It also allows for the future implementation of the service into the business' administration portal for ease of use and a clean user interface.

**ReportingEndpoint.java:**
As opposed to the separate endpoints on the microservice level, the composite level has a single endpoint that can accept a valid input for either
of the endpoints on the microservice level. This composite endpoint uses an instance of ReportingMapper.java to send requests to either of the two
service methods with the valid object type.

**ReportingService.java:**
Both of the by coordinates/venue codes and by hotel codes methods that correspond to the endpoints on the microservice level in this class check if
the user has the valid Keycloak user role. It then sends the requests to the microservice through the implemented ReportingApi that contains its functionality.

**ReportingAcceptanceSpec.groovy:**
These 4 groovy unit tests are programmed to test the endpoint twice with hotel codes and twice with coordinates, both with and without the proper
user role. It mocks the endpoint's interaction with the ReportingApi and entire user process, if the proper user role is used then it will also 
recursively check if the request object is valid in the mocked process.

**reporting.api.yaml:**
The yaml file which has a post HTTP interaction to access the endpoint when running Swagger API.

**reporting.model.yaml:**
The objects' data structures used in the endpoint.

## Hotel Screen Scraping API Database Import
For this project I set up interactions with the API of a screen scraping company to get more accurate hotel rates from the largest hotel providers'
websites in real-time. This included setting up an automatic authentication process with an endpoint in their developer Swagger API and storing the
data we received from their hotel information API and this authentication information within Postgres database tables it creates.

**ScreenScrapingEndpoint.java:**
This endpoint utilizes Swagger API to access functionality to call the service level of the task with the user's request of either a list of hotel names, or countries.

**ScreenScrapingService.java:**
This is where the screen scraping API partner's HTTP get method is called with the request from the user to receive hotel information. It then creates and inserts the data
into a Postgres database tale that it creates. There is also a method for fetching a valid authentication token from the screen scraping API's HTTP post method and storing
it in a separate SQL table from the hotel information. It will first check if there is a valid authentication token already stored in the database to limit calls to their 
API. This service uses ScreenScrapingMapper.java to send the proper object types to the client and repository.

**ScreenScrapingClient.java:**
The client class holds the direct API interactions with the screen scraping company's GET method for hotel information and POST method for authentication.

**ScreenScrapingHotelsRepository.java:**
This repository class has methods to create, update and fetch SQL tables and their data for hotel information and authentication responses.

**ScreenScrapingAcceptanceSpec.groovy:**
This class contains a groovy unit test that mocks the 2 interactions with the screen scraping API's methods for hotel information and authentication,
as well as the entire user process. It also mocks the API's responses and tests if hotels are inserted into the postgres database table that is created. 

**api.yaml:**
This yaml file has both the HTTP post method used in ScreenScrapingEndpoint.java and the data structures of its objects used.
