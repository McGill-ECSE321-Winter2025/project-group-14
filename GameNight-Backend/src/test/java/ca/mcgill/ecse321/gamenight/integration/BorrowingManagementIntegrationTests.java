package ca.mcgill.ecse321.gamenight.integration;

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

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
public class BorrowingManagementIntegrationTests {
    @Autowired
    private TestRestTemplate client;

    @Test
    @Order(0)
    public void testsendValidBorrowingRequest() {
        
        Date startTime = Date.valueOf("2025-01-05");
        Date endTime = Date.valueOf("2025-01-10");
        int senderId =1;
        int gameCopyId = 5;
        
        BorrowingRequestRequestDto request = new BorrowingRequestRequestDto(startTime, endTime, senderId, gameCopyId);
        ResponseEntity<BorrowingRequestResponseDto> response = 
                client.postForEntity("/borrowingRequests", request, BorrowingRequestResponseDto.class);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        BorrowingRequestResponseDto responseBody = response.getBody();
        assertNotNull(responseBody);
        assertNotNull(responseBody.getSendTime());
    }

}
