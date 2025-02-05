package ca.mcgill.ecse321.gamenight.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.GameCopy;
import ca.mcgill.ecse321.gamenight.model.Player;

public interface BorrowingRequestRepository extends CrudRepository<BorrowingRequest, Integer> {

    @Query("SELECT r FROM BorrowingRequest r WHERE r.status = ?1 AND r.gameCopy.gameOwner.id = ?2 ORDER BY r.sendTime DESC")
    List<BorrowingRequest> findAllRequestsByStatusAndGameOwner(String status, int gameOwnerId);

    List<BorrowingRequest> findBySender(Player sender);

    @Query("SELECT r FROM BorrowingRequest r WHERE r.status = ?1 AND r.sender.id = ?2 ORDER BY r.sendTime DESC")
    List<BorrowingRequest> findAllRequestsByStatusAndSender(String status, int senderId);

    List<BorrowingRequest> findByGameCopy(GameCopy gameCopy);
}
