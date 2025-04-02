package ca.mcgill.ecse321.gamenight.service;

import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.gamenight.exception.ObjectNotFoundException;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;
import ca.mcgill.ecse321.gamenight.repo.BorrowingRequestRepository;
import ca.mcgill.ecse321.gamenight.repo.GameCopyRepository;
import ca.mcgill.ecse321.gamenight.repo.PlayerRepository;
import jakarta.transaction.Transactional;

@Service
public class BorrowingManagementService {

    @Autowired
    private BorrowingRequestRepository borrowingRequestRepository;

    @Autowired
    private GameCopyRepository gameCopyRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public BorrowingRequest sendBorrowingRequest(int gameCopyId, int senderId, Date startTime, Date endTime) {
        Optional<GameCopy> gameCopyOpt = gameCopyRepository.findById(gameCopyId);
        if (!gameCopyOpt.isPresent()) {
            throw new ObjectNotFoundException("GameCopy with id " + String.valueOf(gameCopyId) + " not found.");
        }
        GameCopy gameCopy = gameCopyOpt.get();

        Optional<Player> senderOpt = playerRepository.findById(senderId);
        if (!senderOpt.isPresent()) {
            throw new ObjectNotFoundException("Player not found with ID: " + String.valueOf(senderId));
        }
        Player sender = senderOpt.get();
        BorrowingRequest request = new BorrowingRequest();
        request.setGameCopy(gameCopy);
        request.setSender(sender);
        // find current time when sending request
        Date sendTime = new Date(System.currentTimeMillis());
        request.setSendTime(sendTime);

        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setStatus(BorrowingRequestStatus.Delivered);

        BorrowingRequest savedRequest = borrowingRequestRepository.save(request);

        GameOwner owner = gameCopy.getOwner();

        emailService.sendBorrowingRequestEmail(
                owner.getPerson().getEmailAddress(),
                sender.getPerson(),
                gameCopy.getGame().getName());

        return savedRequest;
    }

    @Transactional
    public BorrowingRequest respondToBorrowingRequest(BorrowingRequest request, BorrowingRequestStatus status) {

        GameCopy gameCopy = request.getGameCopy();
        GameOwner owner = gameCopy.getOwner();
        Player sender = request.getSender();
        request.setStatus(status);
        if (status.equals(BorrowingRequestStatus.Accepted)) {
            emailService.sendRequestAcceptedEmail(sender.getPerson().getEmailAddress(), owner.getPerson().getName(),
                    gameCopy.getGame().getName());
        } else if (status.equals(BorrowingRequestStatus.Rejected)) {
            emailService.sendRequestRejectedEmail(sender.getPerson().getEmailAddress(), owner.getPerson().getName(),
                    gameCopy.getGame().getName());
        }

        BorrowingRequest savedRequest = borrowingRequestRepository.save(request);

        return savedRequest;
    }

    @Transactional // done
    public BorrowingRequest updateBorrowingRequestStatus(BorrowingRequest request, BorrowingRequestStatus status) {
        BorrowingRequest existingRequest = borrowingRequestRepository.findById(request.getId())
                .orElseThrow(() -> new ObjectNotFoundException("Borrowing request not found"));
        existingRequest.setStatus(status);
        return borrowingRequestRepository.save(existingRequest);
    }
    
    @Transactional
    public List<BorrowingRequest> findActiveBorrowingRequestsForBorrower(int senderId) {
        Date currentDate = new Date(System.currentTimeMillis());
        return borrowingRequestRepository.findActiveBorrowingRequestsForBorrower(
            senderId, 
            BorrowingRequestStatus.Accepted, 
            currentDate
        );
    }

    public List<BorrowingRequest> findDeliveredBorrowingRequestsForBorrower(int BorrowerId) {
        return borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Delivered,
                BorrowerId);

    }

    public List<BorrowingRequest> findRejectedBorrowingRequestsForBorrower(int BorrowerId) {
        return borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Rejected, BorrowerId);
    }

    /*
     * We're assuming that borrowing history of a borrower is the same as accepted
     * request
     * So once the request is accepted, then we put it into the previously borrowed
     * tab
     */
    public List<BorrowingRequest> findAcceptedBorrowingRequestsForBorrower(int BorrowerId) {
        return borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Accepted, BorrowerId);
    }

    public List<BorrowingRequest> findLendingHistory(int ownerId) {
        return Optional.ofNullable(
                borrowingRequestRepository.findAllRequestsByStatusAndGameOwner(BorrowingRequestStatus.Accepted,
                        ownerId))
                .orElse(Collections.emptyList());
    }

    public BorrowingRequest findGameCopyLendingStatus(GameCopy gameCopy) {
        return borrowingRequestRepository.findByGameCopy(gameCopy)
                .stream()
                .filter(request -> request.getStatus() == BorrowingRequestStatus.Accepted)
                .findFirst()
                .orElse(null);
    }

    public BorrowingRequest getBorrowingRequestById(int requestId) {
        return borrowingRequestRepository.findById(requestId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Borrowing request not found with ID: " + String.valueOf(requestId)));
    }

}
