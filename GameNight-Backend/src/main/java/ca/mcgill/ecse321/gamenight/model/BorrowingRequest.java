package ca.mcgill.ecse321.gamenight.model;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class BorrowingRequest {

    public enum BorrowingRequestStatus { Delivered, Acceped, Rejected };

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private Date sendTime;
    private Date startTime;
    private Date endTime;
    private BorrowingRequestStatus status = BorrowingRequestStatus.Delivered;
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private Player sender;
    @ManyToOne
    @JoinColumn(name = "gamecopy_id")
    private GameCopy gameCopy;

    public BorrowingRequest(Date startTime, Date endTime, Player sender, GameCopy gameCopy) {
        this.sendTime = new java.sql.Date(System.currentTimeMillis());
        this.startTime = startTime;
        this.endTime = endTime;
        this.sender = sender;
        this.gameCopy = gameCopy;
    }

    public int getId() {
        return id;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public BorrowingRequestStatus getStatus() {
        return status;
    }

    public BorrowingRequest setStatus(BorrowingRequestStatus newStatus) {
        status = newStatus;
        return this;
    }

    public Player getSender() {
        return sender;
    }

    public GameCopy getGameCopy() {
        return gameCopy;
    }
}
