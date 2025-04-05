

package ca.mcgill.ecse321.gamenight.repo;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.Player;

public interface BorrowingRequestRepository extends CrudRepository<BorrowingRequest, Integer> {

    @Query("SELECT r FROM BorrowingRequest r WHERE r.status = ?1 AND r.gameCopy.gameOwner.id = ?2 ORDER BY r.sendTime DESC")
    List<BorrowingRequest> findAllRequestsByStatusAndGameOwner(BorrowingRequestStatus status, int gameOwnerId);

    List<BorrowingRequest> findBySender(Player sender);

    List<BorrowingRequest> findAllByGameCopy_GameOwner_Id(int ownerId);

    @Query("SELECT r FROM BorrowingRequest r WHERE r.status = ?1 AND r.sender.id = ?2 ORDER BY r.sendTime DESC")
    List<BorrowingRequest> findAllRequestsByStatusAndSender(BorrowingRequestStatus status, int senderId);

    List<BorrowingRequest> findByGameCopy(GameCopy gameCopy);

    @Query("SELECT r FROM BorrowingRequest r WHERE r.sender.id = :senderId AND r.status = :status AND :currentDate BETWEEN r.startTime AND r.endTime")
    List<BorrowingRequest> findActiveBorrowingRequestsForBorrower(
    @Param("senderId") int senderId,
    @Param("status") BorrowingRequestStatus status,
    @Param("currentDate") Date currentDate);

    @Query("SELECT r FROM BorrowingRequest r WHERE r.sender.id = ?1 ORDER BY r.sendTime DESC")
    List<BorrowingRequest> findAllBySenderId(int senderId);
}

