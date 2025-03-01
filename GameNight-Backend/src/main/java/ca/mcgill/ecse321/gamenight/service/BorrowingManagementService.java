package ca.mcgill.ecse321.gamenight.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.GameOwner;
import ca.mcgill.ecse321.gamenight.model.Player;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;
import ca.mcgill.ecse321.gamenight.repo.BorrowingRequestRepository;
import jakarta.transaction.Transactional;

@Service
public class BorrowingManagementService {
    
    @Autowired
    private BorrowingRequestRepository borrowingRequestRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public BorrowingRequest sendBorrowingRequest(BorrowingRequest request){
        if (request == null || request.getGameCopy() == null || request.getGameCopy().getOwner() == null || request.getSender() == null) {
            throw new IllegalArgumentException("Invalid borrowing request or missing game details.");
        }
    
        request.setStatus(BorrowingRequestStatus.Delivered);
        BorrowingRequest savedRequest = borrowingRequestRepository.save(request);

        GameCopy gameCopy = request.getGameCopy();
        GameOwner owner = gameCopy.getOwner();
        Player sender = request.getSender();
       
        emailService.sendBorrowingRequestEmail(
            owner.getPerson().getEmailAddress(),
            sender.getPerson(),
            gameCopy.getGame().getName()
        );
        
        return savedRequest;
    }

   @Transactional
   public BorrowingRequest respondToBorrowingRequest(BorrowingRequest request, BorrowingRequestStatus status){
        if (request == null || request.getGameCopy() == null || request.getGameCopy().getOwner() == null || request.getSender() == null) {
            throw new IllegalArgumentException("Invalid borrowing request or missing game details.");
        }
        if (status.equals(BorrowingRequestStatus.Accepted)){
            updateBorrowingRequestStatus(request,BorrowingRequestStatus.Accepted);
        } else if (status.equals(BorrowingRequestStatus.Rejected)){
            updateBorrowingRequestStatus(request,BorrowingRequestStatus.Rejected);
        }
                return request;
   }
   
    @Transactional
    public BorrowingRequest updateBorrowingRequestStatus(BorrowingRequest request, BorrowingRequestStatus status){
        request.setStatus(status);
        return request;
    }

    public List<BorrowingRequest> findDeliveredBorrowingRequestsForBorrower(int BorrowerId){
    return borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Delivered, BorrowerId);

    }

    public List<BorrowingRequest> findRejectedBorrowingRequestsForBorrower(int BorrowerId){
        return borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Rejected, BorrowerId);
    }

    /*
    We're assuming that borrowing  history of a borrower is the same as accepted request
    So once the request is accepted, then we put it into the previously borrowed tab
    */
    public List<BorrowingRequest> findAcceptedBorrowingRequestsForBorrower(int BorrowerId){
        return borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Accepted, BorrowerId);
    }

    public List<BorrowingRequest> findLendingHistory(int ownerId){
        return borrowingRequestRepository.findAllRequestsByStatusAndGameOwner(BorrowingRequestStatus.Accepted, ownerId);
    }


    public BorrowingRequest findGameCopyLendingStatus(GameCopy gameCopy){
        List <BorrowingRequest> requests = borrowingRequestRepository.findByGameCopy(gameCopy);
        for (BorrowingRequest request: requests){
            if(request.getStatus() == BorrowingRequestStatus.Accepted){
                return request;
            }
        }
        return null;
    }
}
