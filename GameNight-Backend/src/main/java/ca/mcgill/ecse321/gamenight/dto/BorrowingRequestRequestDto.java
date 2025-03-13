package ca.mcgill.ecse321.gamenight.dto;

import java.sql.Date;

import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;

public class BorrowingRequestRequestDto {
    private Date sendTime;
    private Date startTime;
    private Date endTime;
    private String gameName;
    private String senderName;
    private BorrowingRequestStatus status;
    private int senderId;
    private int gameCopyId;
    private int id;
    
    @SuppressWarnings("unused")
    private BorrowingRequestRequestDto(){
    }
    public BorrowingRequestRequestDto(BorrowingRequest request){
        this.sendTime = request.getSendTime();
        this.endTime = request.getEndTime();
        this.startTime = request.getStartTime();
        this.status=request.getStatus();
        this.gameName = request.getGameCopy().getGame().getName();
        this.senderName = request.getSender().getPerson().getName();
        this.senderId = request.getSender().getId();
        this.gameCopyId = request.getGameCopy().getId();
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
    public String getGameName() {
        return gameName;
    }
    public String getSenderName() {
        return senderName;
    }
    public BorrowingRequestStatus getStatus() {
        return status;
    }
    public int getSenderId() {
        return senderId;
    }
    public int getGameCopyId() {
        return gameCopyId;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    public void setStatus(BorrowingRequestStatus status) {
        this.status = status;
    }
    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
    public void setGameCopyId(int gameCopyId) {
        this.gameCopyId = gameCopyId;
    }
    public static BorrowingRequestRequestDto create() {
        return new BorrowingRequestRequestDto();
    }
}
