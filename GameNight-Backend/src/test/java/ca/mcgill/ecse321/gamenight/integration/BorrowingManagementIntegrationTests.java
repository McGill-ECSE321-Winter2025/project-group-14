package ca.mcgill.ecse321.gamenight.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestRequestDto;
import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestResponseDto;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.Game;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Person;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.repo.BorrowingRequestRepository;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.repo.GameOwnerRepository;
import ca.mcgill.ecse321.gamenight.repo.GameRepository;
import ca.mcgill.ecse321.gamenight.repo.GameReviewRepository;
import ca.mcgill.ecse321.gamenight.repo.PersonRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;
import ca.mcgill.ecse321.gamenight.service.EmailService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class BorrowingManagementIntegrationTests {
        @Autowired
        private TestRestTemplate client;

        @Autowired
        private GameCopyRepository gameCopyRepository;

        @Autowired
        private GameRepository gameRepository;

        @Autowired
        private GameOwnerRepository gameOwnerRepository;

        @Autowired
        private PersonRepository personRepository;

        @Autowired
        private PlayerRepository playerRepository;

        @Autowired
        private BorrowingRequestRepository borrowingRequestRepository;

        @Autowired
        private GameReviewRepository gameReviewRepository;

        @Mock
        private EmailService emailService;

        private int validSenderId;
        private int validGameCopyId;
        private GameOwner createdGameOwner;
        private int senderUserId;

        private static final String VALID_EMAIL1 = "johnash@gmail.com";
        private static final String VALID_EMAIL2 = "sender@gmail.com";
        private static final String VALID_PASSWORD1 = "1234RE";
        private static final String VALID_NAME1 = "John Nashville";
        private static final String VALID_GAMENAME = "Uno";
        private static final String VALID_DESCRIPTION = "card game";
        private static final String VALID_GAMECOPYDESCRIPTION = "Good condition";
        private static final String VALID_PASSWORD2 = "1234RE";
        private static final String VALID_NAME2 = "jane doe";
        private static final Date START_TIME = Date.valueOf("2025-01-05");
        private static final Date END_TIME = Date.valueOf("2025-01-10");

        private <T> HttpEntity<T> createRequestWithHeaders(T body, int userId) {
                HttpHeaders headers = new HttpHeaders();
                headers.set("User-Id", String.valueOf(userId));
                return new HttpEntity<>(body, headers);
        }

        @BeforeAll
        public void setup() {
                gameReviewRepository.deleteAll();
                gameCopyRepository.deleteAll();
                gameRepository.deleteAll();
                gameOwnerRepository.deleteAll();
                personRepository.deleteAll();
                playerRepository.deleteAll();

                Person ownerPerson = personRepository.save(new Person(VALID_EMAIL1, VALID_PASSWORD1, VALID_NAME1));
                createdGameOwner = gameOwnerRepository.save(new GameOwner(ownerPerson));
                Game game = new Game(VALID_GAMENAME, VALID_DESCRIPTION);
                game = gameRepository.save(game);
                GameCopy gameCopy = new GameCopy(VALID_GAMECOPYDESCRIPTION, game, createdGameOwner);
                gameCopy = gameCopyRepository.save(gameCopy);
                validGameCopyId = gameCopy.getId();
                Person senderPerson = personRepository.save(new Person(VALID_EMAIL2, VALID_PASSWORD2, VALID_NAME2));
                Player sender = new Player(senderPerson);
                sender = playerRepository.save(sender);
                validSenderId = sender.getId();
                senderUserId = sender.getPerson().getId();
        }

        @BeforeEach
        public void cleanData() {
                borrowingRequestRepository.deleteAll();
        }

        @AfterAll
        public void cleanup() {
                borrowingRequestRepository.deleteAll();
                gameCopyRepository.deleteAll();
                gameReviewRepository.deleteAll();
                playerRepository.deleteAll();
                gameOwnerRepository.deleteAll();
                gameRepository.deleteAll();
                personRepository.deleteAll();
        }

        @Test
        @Order(0)
        public void testSendValidBorrowingRequest() {

                BorrowingRequestRequestDto request = new BorrowingRequestRequestDto(START_TIME, END_TIME, validSenderId,
                                validGameCopyId);

                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(request, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> response = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertNotNull(response);
                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                BorrowingRequestResponseDto responseBody = response.getBody();
                assertNotNull(responseBody);
                assertNotNull(responseBody.getSendTime());
        }

        @Test
        @Order(1)
        public void testSendBorrowingRequestInvalidGameCopyTest() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId,
                                99999);

                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);

                ResponseEntity<String> response = client.exchange("/borrowingRequests", HttpMethod.POST, requestEntity,
                                String.class);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @Order(2)
        public void testSendBorrowingRequestInvalidSenderTest() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME, 99999,
                                validGameCopyId);
                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);
                ResponseEntity<String> response = client.exchange("/borrowingRequests", HttpMethod.POST, requestEntity,
                                String.class);
                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @Order(3)
        public void testFindDeliveredRequestsForBorrowerValid() {

                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId,
                                validGameCopyId);
                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);
                ResponseEntity<BorrowingRequestResponseDto> response = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());

                HttpEntity<?> requestEntity2 = createRequestWithHeaders(null, senderUserId);
                String url = String.format("/borrowingRequests/%d/status/delivered", validSenderId);

                ResponseEntity<BorrowingRequestResponseDto[]> getResponse = client.exchange(
                                url,
                                HttpMethod.GET,
                                requestEntity2,
                                BorrowingRequestResponseDto[].class);
                assertEquals(HttpStatus.OK, getResponse.getStatusCode());
                BorrowingRequestResponseDto[] requests = getResponse.getBody();
                assertNotNull(requests);
                assertTrue(requests.length > 0);
        }

        @Test
        @Order(4)
        public void testFindDeliveredRequestsForBorrowerInvalid() {
                int invalidBorrowerId = 9999;

                HttpEntity<?> requestEntity = createRequestWithHeaders(null, senderUserId);
                String url = String.format("/borrowingRequests/%d/status/delivered", invalidBorrowerId);

                ResponseEntity<BorrowingRequestResponseDto[]> getResponse = client.exchange(
                                url,
                                HttpMethod.GET,
                                requestEntity,
                                BorrowingRequestResponseDto[].class);

                assertEquals(HttpStatus.OK, getResponse.getStatusCode());
                BorrowingRequestResponseDto[] requests = getResponse.getBody();
                assertNotNull(requests);
                assertEquals(0, requests.length, "Expected no delivered requests for borrower ID " + invalidBorrowerId);
        }

        @Test
        @Order(5)
        public void testFindRejectedRequestsForBorrowerValid() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId, validGameCopyId);

                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);
                ResponseEntity<BorrowingRequestResponseDto> postResponse = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
                BorrowingRequestResponseDto createdRequest = postResponse.getBody();
                assertNotNull(createdRequest);

                String updateUrl = String.format("/borrowingRequests/%d/status?status=Rejected",
                                createdRequest.getId());
                HttpEntity<?> updateRequest = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> updateResponse = client.exchange(
                                updateUrl,
                                HttpMethod.PUT,
                                updateRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        }

        @Test
        @Order(6)
        public void testFindRejectedRequestsForBorrowerInvalid() {
                int invalidBorrowerId = 77777;

                HttpEntity<?> requestEntity = createRequestWithHeaders(null, senderUserId);
                String url = String.format("/borrowingRequests/%d/status/rejected", invalidBorrowerId);

                ResponseEntity<BorrowingRequestResponseDto[]> response = client.exchange(
                                url,
                                HttpMethod.GET,
                                requestEntity,
                                BorrowingRequestResponseDto[].class);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                BorrowingRequestResponseDto[] requests = response.getBody();
                assertNotNull(requests);
                assertEquals(0, requests.length);
        }

        @Test
        @Order(7)
        public void testFindAcceptedRequestsForBorrowerValid() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId, validGameCopyId);

                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);
                ResponseEntity<BorrowingRequestResponseDto> postResponse = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, postResponse.getStatusCode());
                BorrowingRequestResponseDto createdRequest = postResponse.getBody();
                assertNotNull(createdRequest);

                String updateUrl = String.format("/borrowingRequests/%d/status?status=Accepted",
                                createdRequest.getId());
                HttpEntity<?> updateRequest = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> updateResponse = client.exchange(
                                updateUrl,
                                HttpMethod.PUT,
                                updateRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        }

        @Test
        @Order(8)

        public void testFindAcceptedRequestsForBorrowerInvalid() {
                int invalidBorrowerId = 77777;

                HttpEntity<?> requestEntity = createRequestWithHeaders(null, senderUserId);
                String url = String.format("/borrowingRequests/%d/status/accepted", invalidBorrowerId);

                ResponseEntity<BorrowingRequestResponseDto[]> response = client.exchange(
                                url,
                                HttpMethod.GET,
                                requestEntity,
                                BorrowingRequestResponseDto[].class);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                BorrowingRequestResponseDto[] requests = response.getBody();
                assertNotNull(requests);
                assertEquals(0, requests.length);
        }

        @Test
        @Order(9)
        public void testGetBorrowingRequestByIdValid() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId, validGameCopyId);

                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);
                ResponseEntity<BorrowingRequestResponseDto> createResponse = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
                BorrowingRequestResponseDto created = createResponse.getBody();
                assertNotNull(created);

                HttpEntity<?> getRequestEntity = createRequestWithHeaders(null, senderUserId);
                String url = String.format("/borrowingRequests/%d", created.getId());

                ResponseEntity<BorrowingRequestResponseDto> getResponse = client.exchange(
                                url,
                                HttpMethod.GET,
                                getRequestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, getResponse.getStatusCode());
                BorrowingRequestResponseDto fetched = getResponse.getBody();
                assertNotNull(fetched);
                assertEquals(created.getId(), fetched.getId());
        }

        @Test
        @Order(10)
        public void testGetBorrowingRequestByIdInvalid() {
                HttpEntity<?> requestEntity = createRequestWithHeaders(null, senderUserId);
                String url = String.format("/borrowingRequests/%d", 99999);

                ResponseEntity<String> response = client.exchange(
                                url,
                                HttpMethod.GET,
                                requestEntity,
                                String.class);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @Order(11)
        public void testRespondToBorrowingRequestAccepted() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId, validGameCopyId);

                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);
                ResponseEntity<BorrowingRequestResponseDto> createResponse = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
                BorrowingRequestResponseDto createdRequest = createResponse.getBody();
                assertNotNull(createdRequest);

                String updateUrl = String.format("/borrowingRequests/%d/status?status=Accepted",
                                createdRequest.getId());
                HttpEntity<?> updateRequest = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> response = client.exchange(
                                updateUrl,
                                HttpMethod.PUT,
                                updateRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                BorrowingRequestResponseDto updated = response.getBody();
                assertNotNull(updated);
        }

        @Test
        @Order(12)
        public void testRespondToBorrowingRequestRejected() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId, validGameCopyId);

                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);
                ResponseEntity<BorrowingRequestResponseDto> createResponse = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
                BorrowingRequestResponseDto createdRequest = createResponse.getBody();
                assertNotNull(createdRequest);

                String updateUrl = String.format("/borrowingRequests/%d/status?status=Rejected",
                                createdRequest.getId());
                HttpEntity<?> updateRequest = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> response = client.exchange(
                                updateUrl,
                                HttpMethod.PUT,
                                updateRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                BorrowingRequestResponseDto updated = response.getBody();
                assertNotNull(updated);
        }

        @Test
        @Order(13)
        public void testUpdateBorrowingRequestToAccepted() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId, validGameCopyId);
                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> createResponse = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
                BorrowingRequestResponseDto createdRequest = createResponse.getBody();
                assertNotNull(createdRequest);

                String updateUrlRejected = String.format("/borrowingRequests/%d/status?status=Rejected",
                                createdRequest.getId());
                HttpEntity<?> updateRequest = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> rejectedResponse = client.exchange(
                                updateUrlRejected,
                                HttpMethod.PUT,
                                updateRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, rejectedResponse.getStatusCode());

                String updateUrlAccepted = String.format("/borrowingRequests/%d/status?status=Accepted",
                                createdRequest.getId());
                ResponseEntity<BorrowingRequestResponseDto> acceptedResponse = client.exchange(
                                updateUrlAccepted,
                                HttpMethod.PUT,
                                updateRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, acceptedResponse.getStatusCode());
                BorrowingRequestResponseDto updated = acceptedResponse.getBody();
                assertNotNull(updated);
        }

        @Test
        @Order(14)
        public void testUpdateBorrowingRequestToRejected() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId, validGameCopyId);
                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> createResponse = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
                BorrowingRequestResponseDto createdRequest = createResponse.getBody();
                assertNotNull(createdRequest);

                String updateUrlAccepted = String.format("/borrowingRequests/%d/status?status=Accepted",
                                createdRequest.getId());
                HttpEntity<?> updateRequest = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> acceptedResponse = client.exchange(
                                updateUrlAccepted,
                                HttpMethod.PUT,
                                updateRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, acceptedResponse.getStatusCode());

                String updateUrlRejected = String.format("/borrowingRequests/%d/status?status=Rejected",
                                createdRequest.getId());
                ResponseEntity<BorrowingRequestResponseDto> rejectedResponse = client.exchange(
                                updateUrlRejected,
                                HttpMethod.PUT,
                                updateRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, rejectedResponse.getStatusCode());
                BorrowingRequestResponseDto updated = rejectedResponse.getBody();
                assertNotNull(updated);
        }

        @Test
        @Order(15)
        public void testUpdateBorrowingRequestThatDoesNotExist() {
                String updateUrl = String.format("/borrowingRequests/%d/status?status=Accepted", 99999);
                HttpEntity<?> requestEntity = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<String> response = client.exchange(
                                updateUrl,
                                HttpMethod.PUT,
                                requestEntity,
                                String.class);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @Order(16)
        public void testGetLendingHistoryForOwnerValid() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId, validGameCopyId);
                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> createResponse = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
                BorrowingRequestResponseDto createdRequest = createResponse.getBody();
                assertNotNull(createdRequest);

                String updateUrl = String.format("/borrowingRequests/%d/status?status=Accepted",
                                createdRequest.getId());
                HttpEntity<?> updateRequest = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> updateResponse = client.exchange(
                                updateUrl,
                                HttpMethod.PUT,
                                updateRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

                int ownerId = createdGameOwner.getId();
                String lendingHistoryUrl = String.format("/borrowingRequests/owners/%d/lending-history", ownerId);
                HttpEntity<?> historyRequest = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto[]> historyResponse = client.exchange(
                                lendingHistoryUrl,
                                HttpMethod.GET,
                                historyRequest,
                                BorrowingRequestResponseDto[].class);

                assertEquals(HttpStatus.OK, historyResponse.getStatusCode());
                BorrowingRequestResponseDto[] history = historyResponse.getBody();
                assertNotNull(history, "Response body should not be null");
                assertTrue(history.length >= 0, "Expected at least an empty list");
        }

        @Test
        @Order(17)
        public void testGetLendingHistoryForOwnerInvalid() {
                int invalidOwnerId = 99999;
                String lendingHistoryUrl = String.format("/borrowingRequests/owners/%d/lending-history",
                                invalidOwnerId);
                HttpEntity<?> requestEntity = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto[]> response = client.exchange(
                                lendingHistoryUrl,
                                HttpMethod.GET,
                                requestEntity,
                                BorrowingRequestResponseDto[].class);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                BorrowingRequestResponseDto[] history = response.getBody();
                assertNotNull(history, "Response body should not be null");
                assertEquals(0, history.length, "Expected empty lending history for invalid owner.");
        }

        @Test
        @Order(18)
        public void testGetGameCopyLendingStatusValid() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId, validGameCopyId);
                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> createResponse = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
                BorrowingRequestResponseDto createdRequest = createResponse.getBody();
                assertNotNull(createdRequest);

                String updateUrl = String.format("/borrowingRequests/%d/status?status=Accepted",
                                createdRequest.getId());
                HttpEntity<?> updateRequest = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> updateResponse = client.exchange(
                                updateUrl,
                                HttpMethod.PUT,
                                updateRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

                String lendingStatusUrl = String.format("/borrowingRequests/game-copies/%d/lending-status",
                                validGameCopyId);
                HttpEntity<?> statusRequest = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> statusResponse = client.exchange(
                                lendingStatusUrl,
                                HttpMethod.GET,
                                statusRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
                BorrowingRequestResponseDto statusDto = statusResponse.getBody();
                assertNotNull(statusDto, "Expected a lending status for the game copy.");
                assertEquals(createdRequest.getId(), statusDto.getId());
        }

        @Test
        @Order(19)
        public void testGetGameCopyLendingStatusInvalid() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId, validGameCopyId);
                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> createResponse = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

                String lendingStatusUrl = String.format("/borrowingRequests/gameCopy/%d/lending-status",
                                validGameCopyId);
                HttpEntity<?> requestEntity2 = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<String> response = client.exchange(
                                lendingStatusUrl,
                                HttpMethod.GET,
                                requestEntity2,
                                String.class);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @Order(20)
        public void testGetGameCopyLendingStatusGameCopyNotFound() {
                int nonExistentGameCopyId = 99999;
                String lendingStatusUrl = String.format("/borrowingRequests/game-copies/%d/lending-status",
                                nonExistentGameCopyId);
                HttpEntity<?> requestEntity = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<String> response = client.exchange(
                                lendingStatusUrl,
                                HttpMethod.GET,
                                requestEntity,
                                String.class);

                assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }

        @Test
        @Order(21)
        public void testGetRejectedRequestsForBorrowerMapsCorrectly() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId, validGameCopyId);
                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> createResponse = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
                BorrowingRequestResponseDto createdRequest = createResponse.getBody();
                assertNotNull(createdRequest);

                String updateUrl = String.format("/borrowingRequests/%d/status?status=Rejected",
                                createdRequest.getId());
                HttpEntity<?> updateRequest = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> updateResponse = client.exchange(
                                updateUrl,
                                HttpMethod.PUT,
                                updateRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

                String rejectedUrl = String.format("/borrowingRequests/%d/status/rejected", validSenderId);
                ResponseEntity<BorrowingRequestResponseDto[]> rejectedResponse = client.exchange(
                                rejectedUrl,
                                HttpMethod.GET,
                                updateRequest,
                                BorrowingRequestResponseDto[].class);

                assertEquals(HttpStatus.OK, rejectedResponse.getStatusCode());

                BorrowingRequestResponseDto[] rejectedRequests = rejectedResponse.getBody();
                assertNotNull(rejectedRequests);
                assertTrue(rejectedRequests.length > 0);

                boolean found = false;
                for (BorrowingRequestResponseDto dto : rejectedRequests) {
                        if (dto.getId() == createdRequest.getId()) {
                                found = true;
                                assertEquals(createdRequest.getGameName(), dto.getGameName());
                                assertEquals(createdRequest.getSenderName(), dto.getSenderName());
                                assertTrue(dto.getStatus().toString().contains("Rejected"),
                                                "Status should be Rejected but was: " + dto.getStatus());
                                break;
                        }
                }
                assertTrue(found, "Created request should be found in rejected requests");
        }

        @Test
        @Order(22)
        public void testGetAcceptedRequestsForBorrowerMapsCorrectly() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId, validGameCopyId);
                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> createResponse = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
                BorrowingRequestResponseDto createdRequest = createResponse.getBody();
                assertNotNull(createdRequest);

                String updateUrl = String.format("/borrowingRequests/%d/status?status=Accepted",
                                createdRequest.getId());
                HttpEntity<?> updateRequest = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> updateResponse = client.exchange(
                                updateUrl,
                                HttpMethod.PUT,
                                updateRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

                String acceptedUrl = String.format("/borrowingRequests/%d/status/accepted", validSenderId);
                ResponseEntity<BorrowingRequestResponseDto[]> acceptedResponse = client.exchange(
                                acceptedUrl,
                                HttpMethod.GET,
                                updateRequest,
                                BorrowingRequestResponseDto[].class);

                assertEquals(HttpStatus.OK, acceptedResponse.getStatusCode());

                BorrowingRequestResponseDto[] acceptedRequests = acceptedResponse.getBody();
                assertNotNull(acceptedRequests);
                assertTrue(acceptedRequests.length > 0);

                boolean found = false;
                for (BorrowingRequestResponseDto dto : acceptedRequests) {
                        if (dto.getId() == createdRequest.getId()) {
                                found = true;
                                assertEquals(createdRequest.getGameName(), dto.getGameName());
                                assertEquals(createdRequest.getSenderName(), dto.getSenderName());
                                assertTrue(dto.getStatus().toString().contains("Accepted"),
                                                "Status should be Accepted but was: " + dto.getStatus());
                                break;
                        }
                }
                assertTrue(found, "Created request should be found in accepted requests");
        }

        @Test
        @Order(23)
        public void testHandleBorrowingRequestStatusWithRespondAction() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId, validGameCopyId);
                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> createResponse = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
                BorrowingRequestResponseDto createdRequest = createResponse.getBody();
                assertNotNull(createdRequest);

                String updateUrl = String.format("/borrowingRequests/%d/status?status=Accepted&action=respond",
                                createdRequest.getId());
                HttpEntity<?> updateRequest = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> updateResponse = client.exchange(
                                updateUrl,
                                HttpMethod.PUT,
                                updateRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
                BorrowingRequestResponseDto updatedRequest = updateResponse.getBody();
                assertNotNull(updatedRequest);
                assertTrue(updatedRequest.getStatus().toString().contains("Accepted"),
                                "Status should be Accepted but was: " + updatedRequest.getStatus());

                String acceptedUrl = String.format("/borrowingRequests/%d/status/accepted", validSenderId);
                ResponseEntity<BorrowingRequestResponseDto[]> acceptedResponse = client.exchange(
                                acceptedUrl,
                                HttpMethod.GET,
                                updateRequest,
                                BorrowingRequestResponseDto[].class);

                assertEquals(HttpStatus.OK, acceptedResponse.getStatusCode());
                BorrowingRequestResponseDto[] acceptedRequests = acceptedResponse.getBody();
                assertNotNull(acceptedRequests);

                boolean found = false;
                for (BorrowingRequestResponseDto dto : acceptedRequests) {
                        if (dto.getId() == createdRequest.getId()) {
                                found = true;
                                break;
                        }
                }
                assertTrue(found, "Created request should be found in accepted requests after using respond action");
        }

        @Test
        @Order(24)
        public void testHandleBorrowingRequestStatusWithRespondActionRejected() {
                BorrowingRequestRequestDto requestDto = new BorrowingRequestRequestDto(START_TIME, END_TIME,
                                validSenderId, validGameCopyId);
                HttpEntity<BorrowingRequestRequestDto> requestEntity = createRequestWithHeaders(requestDto,
                                senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> createResponse = client.exchange(
                                "/borrowingRequests",
                                HttpMethod.POST,
                                requestEntity,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
                BorrowingRequestResponseDto createdRequest = createResponse.getBody();
                assertNotNull(createdRequest);

                String updateUrl = String.format("/borrowingRequests/%d/status?status=Rejected&action=respond",
                                createdRequest.getId());
                HttpEntity<?> updateRequest = createRequestWithHeaders(null, senderUserId);

                ResponseEntity<BorrowingRequestResponseDto> updateResponse = client.exchange(
                                updateUrl,
                                HttpMethod.PUT,
                                updateRequest,
                                BorrowingRequestResponseDto.class);

                assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
                BorrowingRequestResponseDto updatedRequest = updateResponse.getBody();
                assertNotNull(updatedRequest);
                assertTrue(updatedRequest.getStatus().toString().contains("Rejected"),
                                "Status should be Rejected but was: " + updatedRequest.getStatus());

                String rejectedUrl = String.format("/borrowingRequests/%d/status/rejected", validSenderId);
                ResponseEntity<BorrowingRequestResponseDto[]> rejectedResponse = client.exchange(
                                rejectedUrl,
                                HttpMethod.GET,
                                updateRequest,
                                BorrowingRequestResponseDto[].class);

                assertEquals(HttpStatus.OK, rejectedResponse.getStatusCode());
                BorrowingRequestResponseDto[] rejectedRequests = rejectedResponse.getBody();
                assertNotNull(rejectedRequests);

                boolean found = false;
                for (BorrowingRequestResponseDto dto : rejectedRequests) {
                        if (dto.getId() == createdRequest.getId()) {
                                found = true;
                                break;
                        }
                }
                assertTrue(found, "Created request should be found in rejected requests after using respond action");
        }

        @Test
        @Order(25)
        public void testGetGameCopyLendingStatus_NoAcceptedBorrowingRequest() {
            Game game = gameRepository.findGameByName(VALID_GAMENAME).orElseGet(() -> 
                gameRepository.save(new Game(VALID_GAMENAME, VALID_DESCRIPTION)));
            
            GameOwner owner = createdGameOwner;
        
            GameCopy gameCopy = new GameCopy();
            gameCopy.setDescription("Test Game Copy");
            gameCopy.setGame(game);
            gameCopy.setGameOwner(owner);
            gameCopy = gameCopyRepository.save(gameCopy);
        
            BorrowingRequest rejectedRequest = new BorrowingRequest();
            rejectedRequest.setGameCopy(gameCopy);
            rejectedRequest.setStatus(BorrowingRequest.BorrowingRequestStatus.Rejected);
            rejectedRequest.setStartTime(START_TIME);
            rejectedRequest.setEndTime(END_TIME);
            rejectedRequest.setSendTime(new Date(System.currentTimeMillis()));
            Player sender = playerRepository.findById(validSenderId).orElseThrow();
            rejectedRequest.setSender(sender);
            borrowingRequestRepository.save(rejectedRequest);
        
            BorrowingRequest pendingRequest = new BorrowingRequest();
            pendingRequest.setGameCopy(gameCopy);
            pendingRequest.setStatus(BorrowingRequest.BorrowingRequestStatus.Delivered);
            pendingRequest.setStartTime(START_TIME);
            pendingRequest.setEndTime(END_TIME);
            pendingRequest.setSendTime(new Date(System.currentTimeMillis()));
            pendingRequest.setSender(sender);
            borrowingRequestRepository.save(pendingRequest);
        
            String url = String.format("/borrowingRequests/game-copies/%d/lending-status", gameCopy.getId());
            HttpEntity<?> requestEntity = createRequestWithHeaders(null, senderUserId);
        
            ResponseEntity<String> response = client.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class);
        
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertTrue(response.getBody().contains("No active borrowing request found for this game copy"),
                    "Unexpected response: " + response.getBody());
        }}