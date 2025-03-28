package ca.mcgill.ecse321.gamenight.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestRequestDto;
import ca.mcgill.ecse321.gamenight.dto.BorrowingRequestResponseDto;
import ca.mcgill.ecse321.gamenight.exception.ObjectNotFoundException;
import ca.mcgill.ecse321.gamenight.middleware.RequireUser;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.repo.BorrowingRequestRepository;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.service.BorrowingManagementService;

@RestController
@RequestMapping("/borrowingRequests")
public class BorrowingManagementController {

    @Autowired
    private BorrowingManagementService borrowingManagementService;

    @Autowired
    private GameCopyRepository gameCopyRepository;

    @Autowired
    private BorrowingRequestRepository borrowingRequestRepository;

    /**
     * Creates a new borrowing request.
     * 
     * @param borrowingRequest The borrowing request to create.
     * @return The created borrowing request.
     */
    @PostMapping("")
    @RequireUser
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowingRequestResponseDto sendBorrowingRequest(@RequestBody BorrowingRequestRequestDto borrowingRequest) {
        BorrowingRequest savedRequest = borrowingManagementService.sendBorrowingRequest(
                borrowingRequest.getGameCopyId(),
                borrowingRequest.getSenderId(),
                borrowingRequest.getStartTime(),
                borrowingRequest.getEndTime());
        return new BorrowingRequestResponseDto(savedRequest);
    }

    /**
     * Updates or responds to a borrowing request.
     * 
     * @param requestId The ID of the borrowing request to update.
     * @param status    The new status for the borrowing request.
     * @param action    The action to perform: "respond" or "update" (default is
     *                  "update").
     * @return The updated borrowing request.
     */
    @PutMapping("/{requestId}/status")
    @RequireUser
    public BorrowingRequestResponseDto handleBorrowingRequestStatus(
            @PathVariable int requestId,
            @RequestParam BorrowingRequestStatus status,
            @RequestParam(required = false, defaultValue = "update") String action) {

        BorrowingRequest request = borrowingManagementService.getBorrowingRequestById(requestId);
        BorrowingRequest updatedRequest;

        if ("respond".equals(action)) {
            updatedRequest = borrowingManagementService.respondToBorrowingRequest(request, status);
        } else {
            updatedRequest = borrowingManagementService.updateBorrowingRequestStatus(request, status);
        }

        return new BorrowingRequestResponseDto(updatedRequest);
    }

    /**
     * Retrieves all delivered borrowing requests for a given borrower.
     * 
     * @param borrowerId The ID of the borrower.
     * @return A list of delivered borrowing requests.
     */
    @GetMapping("/{borrowerId}/status/delivered")
    @RequireUser
    public List<BorrowingRequestResponseDto> getDeliveredRequestsForBorrower(@PathVariable int borrowerId) {
        return borrowingManagementService.findDeliveredBorrowingRequestsForBorrower(borrowerId)
                .stream().map(BorrowingRequestResponseDto::new).collect(Collectors.toList());
    }

    /**
     * Retrieves all rejected borrowing requests for a given borrower.
     * 
     * @param borrowerId The ID of the borrower.
     * @return A list of rejected borrowing requests.
     */
    @GetMapping("/{borrowerId}/status/rejected")
    @RequireUser
    public List<BorrowingRequestResponseDto> getRejectedRequestsForBorrower(@PathVariable int borrowerId) {
        return borrowingManagementService.findRejectedBorrowingRequestsForBorrower(borrowerId)
                .stream().map(BorrowingRequestResponseDto::new).collect(Collectors.toList());
    }

    /**
     * Retrieves all accepted borrowing requests for a given borrower.
     * 
     * @param borrowerId The ID of the borrower.
     * @return A list of accepted borrowing requests.
     */
    @GetMapping("/{borrowerId}/status/accepted")
    @RequireUser
    public List<BorrowingRequestResponseDto> getAcceptedRequestsForBorrower(@PathVariable int borrowerId) {
        return borrowingManagementService.findAcceptedBorrowingRequestsForBorrower(borrowerId)
                .stream().map(BorrowingRequestResponseDto::new).collect(Collectors.toList());
    }

    /**
     * Retrieves the lending history for a given game owner.
     * 
     * @param ownerId The ID of the game owner.
     * @return A list of borrowing requests related to the owner.
     */
    @GetMapping("/owners/{ownerId}/lending-history")
    @RequireUser
    public ResponseEntity<List<BorrowingRequestResponseDto>> getLendingHistoryForOwner(@PathVariable int ownerId) {
        List<BorrowingRequestResponseDto> responseList = borrowingManagementService.findLendingHistory(ownerId)
                .stream().map(BorrowingRequestResponseDto::new).collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    /**
     * Retrieves the lending status for a specific game copy.
     * 
     * @param gameCopyId The ID of the game copy.
     * @return The borrowing request for the game copy.
     * @throws ObjectNotFoundException If no active borrowing request is found.
     */
    @GetMapping("/game-copies/{gameCopyId}/lending-status")
    @RequireUser
    public ResponseEntity<BorrowingRequestResponseDto> getGameCopyLendingStatus(@PathVariable int gameCopyId) {
        GameCopy gameCopy = gameCopyRepository.findById(gameCopyId)
                .orElseThrow(() -> new ObjectNotFoundException("Game copy not found"));

        return borrowingRequestRepository.findByGameCopy(gameCopy)
                .stream().filter(request -> request.getStatus() == BorrowingRequestStatus.Accepted)
                .findFirst()
                .map(request -> ResponseEntity.ok(new BorrowingRequestResponseDto(request)))
                .orElseThrow(() -> new ObjectNotFoundException("No active borrowing request found for this game copy"));
    }

    /**
     * Retrieves a borrowing request by its ID.
     * 
     * @param requestId The ID of the borrowing request.
     * @return The borrowing request details.
     */
    @GetMapping("/{requestId}")
    @RequireUser
    public BorrowingRequestResponseDto getBorrowingRequestById(@PathVariable int requestId) {
        return new BorrowingRequestResponseDto(borrowingManagementService.getBorrowingRequestById(requestId));
    }

    /**
     * Retrieves all borrowing requests for a given borrower.
     * 
     * @param borrowerId The ID of the borrower.
     * @return A list of all borrowing requests.
     */
    @GetMapping("/{borrowerId}/requests")
    @RequireUser
    public List<BorrowingRequestResponseDto> getAllRequestsForBorrower(
            @PathVariable int borrowerId, 
            @RequestHeader("User-Id") int userId) {
        System.out.println("Fetching borrowing requests for borrower ID: " + borrowerId);
        System.out.println("Received User-Id from header: " + userId);

        if (borrowerId != userId) {
            System.out.println("Mismatch between path variable and header! Borrower ID: " + borrowerId + ", User ID: " + userId);
            throw new IllegalArgumentException("User ID does not match the borrower ID");
        }

        List<BorrowingRequest> requests = borrowingManagementService.findAllBorrowingRequestsForBorrower(borrowerId);
        System.out.println("Number of requests found: " + requests.size());
        return requests.stream().map(BorrowingRequestResponseDto::new).collect(Collectors.toList());
    }



}
