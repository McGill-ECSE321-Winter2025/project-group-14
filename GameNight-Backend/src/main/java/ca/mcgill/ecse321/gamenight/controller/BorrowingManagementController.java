package ca.mcgill.ecse321.gamenight.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestRequestDto;
import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestResponseDto;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;
import ca.mcgill.ecse321.gamenight.service.BorrowingManagementService;

@RestController
public class BorrowingManagementController {

        @Autowired
        private BorrowingManagementService borrowingManagementService;

        
        @PostMapping("/request")
        public BorrowingRequestResponseDto sendBorrowingRequest(@RequestBody BorrowingRequestRequestDto borrowingRequest) {
                BorrowingRequest savedRequest = borrowingManagementService.sendBorrowingRequest(
                        borrowingRequest.getGameCopyId(),
                        borrowingRequest.getSenderId(),
                        borrowingRequest.getSendTime(),
                        borrowingRequest.getStartTime(),
                        borrowingRequest.getEndTime()
                    );
                    
                    return new BorrowingRequestResponseDto(savedRequest);

        }

        @PutMapping("/{requestId}/respond")
        public BorrowingRequestResponseDto respondToBorrowingRequest(@PathVariable int requestId,
                        @PathVariable BorrowingRequestStatus status) {
                return null;
        }

        @PutMapping("/{requestId}/update")
        public BorrowingRequestResponseDto updateBorrowingRequestStatus(@PathVariable int requestId,
                        @PathVariable BorrowingRequestStatus status) {
                return null;
        }

        @GetMapping("/delivered/{borrowerId}")
        public List<BorrowingRequestResponseDto> getDeliveredRequestsForBorrower(@PathVariable int borrowerId) {
                List<BorrowingRequest> requests = borrowingManagementService.findDeliveredBorrowingRequestsForBorrower(borrowerId);
                return requests.stream()
                .map(request -> new BorrowingRequestResponseDto(request))
                .collect(Collectors.toList());
        }

        @GetMapping("/rejected/{borrowerId}")
        public List<BorrowingRequestResponseDto> getRejectedRequestsForBorrower(@PathVariable int borrowerId) {
                List<BorrowingRequest> requests = borrowingManagementService.findRejectedBorrowingRequestsForBorrower(borrowerId);
                return requests.stream()
                .map(request -> new BorrowingRequestResponseDto(request))
                .collect(Collectors.toList());
        }

        @GetMapping("/accepted/{borrowerId}")
        public List<BorrowingRequestResponseDto> getAcceptedRequestsForBorrower(@PathVariable int borrowerId) {
                List<BorrowingRequest> requests = borrowingManagementService.findAcceptedBorrowingRequestsForBorrower(borrowerId);
                return requests.stream()
                .map(request -> new BorrowingRequestResponseDto(request))
                .collect(Collectors.toList());
        }

       //@GetMapping
       // public List<BorrowingRequestResponseDto> getLendingHistoryForOwner(@PathVariable int ownerId) {
       //     return null;
       // }

       // @GetMapping
       //public List<BorrowingRequestResponseDto> getGameCopyLendingStatus(@PathVariable int gameCopyId) {
        //    return null;
        //}

}
