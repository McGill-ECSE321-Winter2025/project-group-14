package ca.mcgill.ecse321.gamenight.dto;
import java.sql.Date;

public class BorrowingRequestRequestDto {
    private Date startTime;
    private Date endTime;
    private int senderId;
    private int gameCopyId;

    @SuppressWarnings("unused")
    private BorrowingRequestRequestDto(){
    }
    public BorrowingRequestRequestDto(Date startTime, Date endTime, int senderId, int gameCopyId){
        this.startTime = startTime;
        this.endTime = endTime;
        this.senderId = senderId;
        this.gameCopyId = gameCopyId;
    }

    public Date getStartTime() {
        return startTime;
    }
    public Date getEndTime() {
        return endTime;
    }
    public int getSenderId() {
        return senderId;
    }
    public int getGameCopyId() {
        return gameCopyId;
    }
    
}
