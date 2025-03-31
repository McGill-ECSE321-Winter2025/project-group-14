import React, { useState, useEffect, useContext } from "react";
import { DatePicker } from "@mui/x-date-pickers";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import "./GameCopyGameDetailsPage.css";
import { AuthContext } from "../AuthContext";
import Button from "./Button"
import {
  Card,
  Typography,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle
} from "@mui/material";


const GameCopyCard = ({ gameCopyId, owner, description }) => {
  const [startDate, setStartDate] = useState(null);
  const [endDate, setEndDate] = useState(null);
  const { user } = useContext(AuthContext);
  const [playerId, setPlayerId] = useState();
  const [requestBorrowOpen, setBorrowRequestOpen] = useState(false);

  useEffect(() => {
    fetch(`http://localhost:8080/players?person_id=${user.userId}`, {
      headers: { "Content-Type": "application/json", "User-Id": user.userId },
    })
      .then((response) => response.json())
      .then((data) => setPlayerId(data))
      .catch((error) => console.error("Error fetching player for user:", error));
  }, [user]);

  const handleBorrowClick = () => {
    setBorrowRequestOpen(true);
  };

  const handleSubmit = async () => {
    if (startDate && endDate) {
      await fetch("http://localhost:8080/borrowingRequests", {
        method: "POST",
        body: JSON.stringify({
          startTime: startDate.format("YYYY-MM-DD"),
          endTime: endDate.format("YYYY-MM-DD"),
          senderId: playerId,
          gameCopyId: gameCopyId,
        }),
        headers: { "Content-Type": "application/json", "User-Id": user.userId },
      }).catch((error) => console.error("Error:", error));

      console.log(
        `Borrow request sent from ${startDate.format("YYYY-MM-DD")} to ${endDate.format("YYYY-MM-DD")}`
      );
      setBorrowRequestOpen(false);
      setStartDate(null);
      setEndDate(null);
    } else {
      alert("Please select both start and end dates before submitting.");
    }
  };

  const handleCancelBorrowRequest = () => {
    setBorrowRequestOpen(false);
  }

  return (
    <Card>
      <div className="game-copy-card">
        <div className="card-content">
          <div className="user-section">
            <div className="avatar">
              {owner?.charAt(0).toUpperCase()}
            </div>
            <div className="user-details">
              <h3 className="user-name">{owner}</h3>
            </div>
          </div>

          <div className="game-section">
            <div className="info-row">
              <span className="info-label">Details:</span>
              <span className="comment-value">{description}</span>
            </div>
          </div>
          <Button onClick={handleBorrowClick}>
            Ask to Borrow
          </Button>
        </div>
      </div>

      <Dialog open={requestBorrowOpen} onClose={handleCancelBorrowRequest}>
        <DialogTitle>Send a borrowing request to {owner}</DialogTitle>
        <DialogContent>
          <Typography>Select dates for your request.</Typography>
        </DialogContent>
        <DialogActions>
          <div className="date-picker-container">
            <LocalizationProvider dateAdapter={AdapterDayjs}>
              <DatePicker
                label="Start Date"
                value={startDate}
                onChange={(date) => setStartDate(date)}
                disablePast
              />
              <DatePicker
                label="End Date"
                value={endDate}
                onChange={(date) => setEndDate(date)}
                minDate={startDate} // Ensures end date is after start date
                disablePast
              />
            </LocalizationProvider>
            <Button type="success" onClick={handleSubmit}>
              Submit Request
            </Button>
          </div>
        </DialogActions>
      </Dialog>
    </Card>
  );
};

export default GameCopyCard;