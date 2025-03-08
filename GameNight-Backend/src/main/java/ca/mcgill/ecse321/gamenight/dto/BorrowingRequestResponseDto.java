package ca.mcgill.ecse321.gamenight.dto;

import java.sql.Date;

import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;


public class BorrowingRequestResponseDto {
    private Date sendTime;
    private Date startTime;
    private Date endTime;
    private String gameName;
    private String senderName;
    private BorrowingRequestStatus status;
    
    @SuppressWarnings("unused")
    public BorrowingRequestResponseDto(){}

    public BorrowingRequestResponseDto(BorrowingRequest request){

        this.sendTime = request.getSendTime();
        this.endTime = request.getEndTime();
        this.startTime = request.getStartTime();
        this.status=request.getStatus();
        this.gameName = request.getGameCopy().getGame().getName();
        this.senderName = request.getSender().getPerson().getName();
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

}

