package ca.mcgill.ecse321.gamenight.model;

import java.sql.Date;

public class BorrowingRequest {

    enum BorrowingRequestStatus { Delivered, Acceped, Rejected };

    private Date sendTime;
    private Date startTime;
    private Date endTime;
    private BorrowingRequestStatus status = BorrowingRequestStatus.Delivered;
    private Player sender;
    private GameOwner receiver;
    private GameCopy gameCopy;

    public BorrowingRequest(Date startTime, Date endTime, Player sender, GameOwner receiver, GameCopy gameCopy) {
        this.sendTime = new java.sql.Date(System.currentTimeMillis());
        this.startTime = startTime;
        this.endTime = endTime;
        this.sender = sender;
        this.receiver = receiver;
        this.gameCopy = gameCopy;
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

    public BorrowingRequestStatus setStatus(BorrowingRequestStatus newStatus) {
        status = newStatus;
        return status;
    }

    public Player getSender() {
        return sender;
    }

    public GameOwner getReceiver() {
        return receiver;
    }

    public GameCopy getGameCopy() {
        return gameCopy;
    }
}
