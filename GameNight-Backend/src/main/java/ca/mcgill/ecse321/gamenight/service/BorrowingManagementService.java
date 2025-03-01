package ca.mcgill.ecse321.gamenight.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.gamenight.event.NotificationEvent;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;
import ca.mcgill.ecse321.gamenight.repo.BorrowingRequestRepository;
import jakarta.transaction.Transactional;

@Service
public class BorrowingManagementService {
    
    @Autowired
    private BorrowingRequestRepository borrowingRequestRepository;

    @Autowired 
    private ApplicationEventPublisher eventPublisher;


    @Transactional
    public BorrowingRequest sendBorrowingRequest(BorrowingRequest request){
        request.setStatus(BorrowingRequestStatus.Delivered);
        int ownerId = request.getGameCopy().getOwner().getId();
        int senderId= request.getSender().getId();
        BorrowingRequest savedRequest = borrowingRequestRepository.save(request);
        
        eventPublisher.publishEvent(new NotificationEvent(this, ownerId, 
        "You have received a new borrowing request from" +senderId ));

        return savedRequest;
    }

   // @Transactional
   // public BorrowingRequest respondToBorrowingRequest(BorrowingRequest request){}
   // change status to accept 
   // reina's method sends an email 

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
