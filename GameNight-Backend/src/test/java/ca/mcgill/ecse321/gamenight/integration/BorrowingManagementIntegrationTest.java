/*package ca.mcgill.ecse321.gamenight.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Date;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestRequestDto;
import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestResponseDto;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class BorrowingManagementIntegrationTest {
    @Autowired
    private TestRestTemplate client;

    @Test
    @Order(1)
    public void testSendBorrowingRequest() {
        BorrowingRequestRequestDto request =BorrowingRequestRequestDto.create();
        request.setId(10);
        request.setSenderId(5);
        request.setGameCopyId(1);
        request.setSendTime(Date.valueOf("2025-03-10"));
        request.setStartTime(Date.valueOf("2025-03-12"));
        request.setEndTime(Date.valueOf("2025-03-15"));
        request.setStatus(BorrowingRequestStatus.Delivered);

        ResponseEntity<BorrowingRequestResponseDto> response = 
                client.postForEntity("/request", request, BorrowingRequestResponseDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        BorrowingRequestResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
    }

}
*/