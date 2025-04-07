package ca.mcgill.ecse321.gamenight.dto;
import java.time.LocalDate;

public class BorrowingRequestRequestDto {
    private LocalDate startTime;
    private LocalDate endTime;
    private int senderId;
    private int gameCopyId;
    
    @SuppressWarnings("unused")
    private BorrowingRequestRequestDto(){
    }
    public BorrowingRequestRequestDto(LocalDate startTime, LocalDate endTime, int senderId, int gameCopyId){
        this.startTime = startTime;
        this.endTime = endTime;
        this.senderId = senderId;
        this.gameCopyId = gameCopyId;
    }

    public LocalDate getStartTime() {
        return startTime;
    }
    public LocalDate getEndTime() {
        return endTime;
    }
    public int getSenderId() {
        return senderId;
    }
    public int getGameCopyId() {
        return gameCopyId;
    }
    
}
