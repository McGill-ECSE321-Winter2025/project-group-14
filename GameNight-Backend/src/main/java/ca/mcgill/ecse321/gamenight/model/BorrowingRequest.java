package ca.mcgill.ecse321.gamenight.model;

import java.sql.Date;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class BorrowingRequest {

    public enum BorrowingRequestStatus {
        Delivered, Accepted, Rejected
    };

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private Date sendTime;
    private LocalDate startTime;
    private LocalDate endTime;
    private BorrowingRequestStatus status = BorrowingRequestStatus.Delivered;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private Player sender;

    @ManyToOne
    @JoinColumn(name = "gamecopy_id")
    private GameCopy gameCopy;

    public BorrowingRequest() {
    }

    public BorrowingRequest(LocalDate startTime, LocalDate endTime, Player sender, GameCopy gameCopy) {
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

    public LocalDate getStartTime() {
        return startTime;
    }

    public LocalDate getEndTime() {
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

    public void setId(int id) {
        this.id = id;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public void setStartTime(LocalDate startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDate endTime) {
        this.endTime = endTime;
    }

    public void setSender(Player sender) {
        this.sender = sender;
    }

    public void setGameCopy(GameCopy gameCopy) {
        this.gameCopy = gameCopy;
    }
}
