package ca.mcgill.ecse321.gamenight.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestRequestDto;
import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestResponseDto;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.service.BorrowingManagementService;

@RestController
@RequestMapping("/borrowingRequests")
public class BorrowingManagementController {

        @Autowired
        private BorrowingManagementService borrowingManagementService;

        @Autowired
        private GameCopyRepository gameCopyRepository;
        
        @PostMapping("")
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
        
        @PutMapping("/{requestId}/")
        public BorrowingRequestResponseDto respondToBorrowingRequest(@PathVariable int requestId,
                        @RequestParam BorrowingRequestStatus status) {
                                BorrowingRequest request = borrowingManagementService.getBorrowingRequestById(requestId);
                                BorrowingRequest updatedRequest = borrowingManagementService.respondToBorrowingRequest(request, status);
                                return new BorrowingRequestResponseDto(updatedRequest);
        }
        
        @PutMapping("/{requestId}/status")
        public BorrowingRequestResponseDto updateBorrowingRequestStatus(@PathVariable int requestId,
                        @RequestParam BorrowingRequestStatus status) {
                                BorrowingRequest request = borrowingManagementService.getBorrowingRequestById(requestId);
                                BorrowingRequest updatedRequest = borrowingManagementService.updateBorrowingRequestStatus(request,status);
                return new BorrowingRequestResponseDto(updatedRequest);
        }

        @GetMapping("/{borrowerId}/status/delivered")
        public List<BorrowingRequestResponseDto> getDeliveredRequestsForBorrower(@PathVariable int borrowerId) {
                List<BorrowingRequest> requests = borrowingManagementService.findDeliveredBorrowingRequestsForBorrower(borrowerId);
                return requests.stream()
                .map(request -> new BorrowingRequestResponseDto(request))
                .collect(Collectors.toList());
        }
        
        @GetMapping("/{borrowerId}/status/rejected")
        public List<BorrowingRequestResponseDto> getRejectedRequestsForBorrower(@PathVariable int borrowerId) {
                List<BorrowingRequest> requests = borrowingManagementService.findRejectedBorrowingRequestsForBorrower(borrowerId);
                return requests.stream()
                .map(request -> new BorrowingRequestResponseDto(request))
                .collect(Collectors.toList());
        }

        @GetMapping("/{borrowerId}/status/accepted")
        public List<BorrowingRequestResponseDto> getAcceptedRequestsForBorrower(@PathVariable int borrowerId) {
                List<BorrowingRequest> requests = borrowingManagementService.findAcceptedBorrowingRequestsForBorrower(borrowerId);
                return requests.stream()
                .map(request -> new BorrowingRequestResponseDto(request))
                .collect(Collectors.toList());
        }
        
        @GetMapping("/{ownerId}/lending-history")
        public List<BorrowingRequestResponseDto> getLendingHistoryForOwner(@PathVariable int ownerId) {
                List<BorrowingRequest> ownerLendingHistory = borrowingManagementService.findLendingHistory(ownerId);
                return ownerLendingHistory.stream().map(BorrowingRequestResponseDto::new).collect(Collectors.toList());
        }
        
        @GetMapping("/{gameCopyId}/lending-status")
        public BorrowingRequestResponseDto getGameCopyLendingStatus(@PathVariable int gameCopyId) {
                GameCopy gameCopy = gameCopyRepository.findById(gameCopyId).orElseThrow(() -> new IllegalArgumentException("Game copy not found with ID: " + gameCopyId));
                BorrowingRequest request = borrowingManagementService.findGameCopyLendingStatus(gameCopy);
                if (request == null){
                        throw new IllegalArgumentException("No active borrowing request found for Game Copy ID: " + gameCopyId);
                }
        return new BorrowingRequestResponseDto(request);
        }
        }
