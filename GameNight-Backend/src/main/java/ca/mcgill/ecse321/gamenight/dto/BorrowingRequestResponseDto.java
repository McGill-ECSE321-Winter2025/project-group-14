package ca.mcgill.ecse321.gamenight.dto;

import java.sql.Date;
import java.time.LocalDate;

import ca.mcgill.ecse321.gamenight.model.BorrowingRequest;
import ca.mcgill.ecse321.gamenight.model.BorrowingRequest.BorrowingRequestStatus;

public class BorrowingRequestResponseDto {
    private int id;
    private Date sendTime;
    private LocalDate startTime;
    private LocalDate endTime;
    private String gameName;
    private String senderName;
    private BorrowingRequestStatus status;
    private int gameCopyId;

    @SuppressWarnings("unused")
    private BorrowingRequestResponseDto() {
    }

    public BorrowingRequestResponseDto(BorrowingRequest request) {
        this.id = request.getId();
        this.sendTime = request.getSendTime();
        this.endTime = request.getEndTime();
        this.startTime = request.getStartTime();
        this.status = request.getStatus();
        this.gameName = request.getGameCopy().getGame().getName();
        this.senderName = request.getSender().getPerson().getName();
        this.gameCopyId = request.getGameCopy().getId();
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

    public String getGameName() {
        return gameName;
    }

    public String getSenderName() {
        return senderName;
    }

    public BorrowingRequestStatus getStatus() {
        return status;
    }

    public int getGameCopyId() {
        return gameCopyId;
    }

}
