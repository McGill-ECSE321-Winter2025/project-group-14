package ca.mcgill.ecse321.gamenight.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestRequestDto;
import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestResponseDto;
import ca.mcgill.ecse321.gamenight.service.BorrowingManagementService;

@RestController
public class BorrowingManagementController {
    @Autowired
    BorrowingManagementService borrowingManagementService;

    @PostMapping
    public BorrowingRequestResponseDto createBorrowingRequest(@RequestBody BorrowingRequestRequestDto borrowingRequest){
            return null;  
    }

    @PutMapping
    public BorrowingRequestResponseDto respondToBorrowingRequest(@RequestBody BorrowingRequestRequestDto borrowingRequest){
            return null;  
    }
}
