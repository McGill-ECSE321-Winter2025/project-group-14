package ca.mcgill.ecse321.gamenight.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestRequestDto;
import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestResponseDto;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;
import ca.mcgill.ecse321.gamenight.service.BorrowingManagementService;

@RestController
public class BorrowingManagementController {
        @Autowired
        BorrowingManagementService borrowingManagementService;

        @PostMapping
        public BorrowingRequestResponseDto createBorrowingRequest(
                        @RequestBody BorrowingRequestRequestDto borrowingRequest) {
                return null;
        }

        @PutMapping
        public BorrowingRequestResponseDto respondToBorrowingRequest(@PathVariable int requestId,
                        @PathVariable BorrowingRequestStatus status) {
                return null;
        }

        @PutMapping
        public BorrowingRequestResponseDto updateBorrowingRequestStatus(@PathVariable int requestId,
                        @PathVariable BorrowingRequestStatus status) {
                return null;
        }

        @GetMapping
        public List<BorrowingRequestResponseDto> getDeliveredRequestsForBorrower(@PathVariable int borrowerId) {
                return null;

        }

        @GetMapping
        public List<BorrowingRequestResponseDto> getRejectedRequestsForBorrower(@PathVariable int borrowerId) {
            return null;
        }

        @GetMapping
        public List<BorrowingRequestResponseDto> getAcceptedRequestsForBorrower(@PathVariable int borrowerId) {
            return null;
        }

        @GetMapping
        public List<BorrowingRequestResponseDto> getLendingHistoryForOwner(@PathVariable int ownerId) {
            return null;
        }

        @GetMapping
        public List<BorrowingRequestResponseDto> getGameCopyLendingStatus(@PathVariable int gameCopyId) {
            return null;
        }


}
