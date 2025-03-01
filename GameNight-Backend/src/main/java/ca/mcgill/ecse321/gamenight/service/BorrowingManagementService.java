package ca.mcgill.ecse321.gamenight.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.gamenight.event.NotificationEvent;
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
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private EmailService emailService;

    @Transactional
    public BorrowingRequest sendBorrowingRequest(BorrowingRequest request){
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

   // @Transactional
   // public BorrowingRequest respondToBorrowingRequest(BorrowingRequest request){}

   // @Transactional
   // public BorrowingRequest updateBorrowingRequestStatus(BorrowingRequest request){}

    //public List<BorrowingRequest> findCompletedBorrowingRequestsForBorrower(int BorrowerId){}

    public List<BorrowingRequest> findAcceptedBorrowingRequestsForBorrower(int BorrowerId){
        return borrowingRequestRepository.findAllRequestsByStatusAndSender(BorrowingRequestStatus.Accepted, BorrowerId);
    }

    public List<BorrowingRequest> findLendingHistory(int ownerId){
        return borrowingRequestRepository.findAllRequestsByStatusAndGameOwner(BorrowingRequestStatus.Accepted, ownerId);
    }

    
   // public List<BorrowingRequest> findBorrowingHistory(int borrowerId){}

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
