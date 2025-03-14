package ca.mcgill.ecse321.gamenight.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestRequestDto;
import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestResponseDto;
import ca.mcgill.ecse321.gamenight.exceptions.BorrowingRequestNotFoundException;
import ca.mcgill.ecse321.gamenight.exceptions.GameCopyNotFoundException;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.service.BorrowingManagementService;

/**
 * REST controller for managing borrowing requests
 * 
 * This controller handles endpoints related to sending, updating, and
 * responding
 * borrowing requests as well as retrieving borrowing requests by status,
 * lending history, and individual requests.
 * 
 */
@RestController
@RequestMapping("/borrowingRequests")
public class BorrowingManagementController {

        @Autowired
        private BorrowingManagementService borrowingManagementService;

        @Autowired
        private GameCopyRepository gameCopyRepository;

        /**
         * Create a new borrowing request.
         * 
         * @param borrowingRequest The borrowing request to create
         * @return The created request
         */
        @PostMapping("")
        @ResponseStatus(HttpStatus.CREATED)
        public BorrowingRequestResponseDto sendBorrowingRequest(
                        @RequestBody BorrowingRequestRequestDto borrowingRequest) {
                BorrowingRequest savedRequest = borrowingManagementService.sendBorrowingRequest(
                                borrowingRequest.getGameCopyId(),
                                borrowingRequest.getSenderId(),
                                borrowingRequest.getStartTime(),
                                borrowingRequest.getEndTime());

                return new BorrowingRequestResponseDto(savedRequest);

        }

        /**
         * Responding to a new borrowing request.
         * 
         * @param requestId The ID of the borrowing request to respond to.
         * @param status    The new status for the borrowing request.
         * @return The updated request
         */
        @PutMapping("/{requestId}/")
        public BorrowingRequestResponseDto respondToBorrowingRequest(@PathVariable int requestId,
                        @RequestParam BorrowingRequestStatus status) {
                BorrowingRequest request = borrowingManagementService.getBorrowingRequestById(requestId);
                BorrowingRequest updatedRequest = borrowingManagementService.respondToBorrowingRequest(request, status);
                return new BorrowingRequestResponseDto(updatedRequest);
        }

        /**
         * Update a new borrowing request.
         * 
         * @param requestId The ID of the borrowing request to update.
         * @param status    The new status for the borrowing request.
         * @return The updated request
         */
        @PutMapping("/{requestId}/status")
        public BorrowingRequestResponseDto updateBorrowingRequestStatus(@PathVariable int requestId,
                        @RequestParam BorrowingRequestStatus status) {
                BorrowingRequest request = borrowingManagementService.getBorrowingRequestById(requestId);
                BorrowingRequest updatedRequest = borrowingManagementService.updateBorrowingRequestStatus(request,
                                status);
                return new BorrowingRequestResponseDto(updatedRequest);
        }

        /**
         * Retrieve all delivered borrowing requests for a given borrower.
         * 
         * @param borrowerId The ID of the borrower.
         * @return A list of delivered borrowing requests.
         */
        @GetMapping("/{borrowerId}/status/delivered")
        public List<BorrowingRequestResponseDto> getDeliveredRequestsForBorrower(@PathVariable int borrowerId) {
                List<BorrowingRequest> requests = borrowingManagementService
                                .findDeliveredBorrowingRequestsForBorrower(borrowerId);
                return requests.stream()
                                .map(request -> new BorrowingRequestResponseDto(request))
                                .collect(Collectors.toList());
        }

        /**
         * Retrieve all rejected borrowing requests for a given borrower.
         * 
         * @param borrowerId The ID of the borrower.
         * @return A list of rejected borrowing requests.
         */
        @GetMapping("/{borrowerId}/status/rejected")
        public List<BorrowingRequestResponseDto> getRejectedRequestsForBorrower(@PathVariable int borrowerId) {
                List<BorrowingRequest> requests = borrowingManagementService
                                .findRejectedBorrowingRequestsForBorrower(borrowerId);
                return requests.stream()
                                .map(request -> new BorrowingRequestResponseDto(request))
                                .collect(Collectors.toList());
        }

        /**
         * Retrieve all accepted borrowing requests for a given borrower.
         * 
         * @param borrowerId The ID of the borrower.
         * @return A list of accepted borrowing requests.
         */

        @GetMapping("/{borrowerId}/status/accepted")
        public List<BorrowingRequestResponseDto> getAcceptedRequestsForBorrower(@PathVariable int borrowerId) {
                List<BorrowingRequest> requests = borrowingManagementService
                                .findAcceptedBorrowingRequestsForBorrower(borrowerId);
                return requests.stream()
                                .map(request -> new BorrowingRequestResponseDto(request))
                                .collect(Collectors.toList());
        }

        /**
         * Retrieve the lending history for a given owner.
         * 
         * @param ownerId The ID of the game owner.
         * @return A list of lending history.
         */
        @GetMapping("/{ownerId}/lending-history")
        public List<BorrowingRequestResponseDto> getLendingHistoryForOwner(@PathVariable int ownerId) {
                List<BorrowingRequest> ownerLendingHistory = borrowingManagementService.findLendingHistory(ownerId);
                return ownerLendingHistory.stream().map(BorrowingRequestResponseDto::new).collect(Collectors.toList());
        }

        /**
         * Retrieve the lending status for a specific game copy.
         * 
         * @param gameCopyId The ID of the game copy.
         * @return borrowing request for the game copy.
         * @throws GameCopyNotFoundException         if the game copy is not found.
         * @throws BorrowingRequestNotFoundException if no accepted borrowing request is
         *                                           found for the game copy.
         */

        @GetMapping("/{gameCopyId}/lending-status")
        public BorrowingRequestResponseDto getGameCopyLendingStatus(@PathVariable int gameCopyId) {
                GameCopy gameCopy = gameCopyRepository.findById(gameCopyId)
                                .orElseThrow(() -> new GameCopyNotFoundException(String.valueOf(gameCopyId)));
                BorrowingRequest request = borrowingManagementService.findGameCopyLendingStatus(gameCopy);
                if (request == null) {
                        throw new BorrowingRequestNotFoundException(gameCopyId);
                }
                return new BorrowingRequestResponseDto(request);
        }

        /**
         * Retrieve a borrowing request by its ID.
         * 
         * @param requestId The ID of the borrowing request.
         * @return borrowing request.
         */
        @GetMapping("/{requestId}")
        public BorrowingRequestResponseDto getBorrowingRequestById(@PathVariable int requestId) {
                BorrowingRequest request = borrowingManagementService.getBorrowingRequestById(requestId);
                return new BorrowingRequestResponseDto(request);
        }

}
