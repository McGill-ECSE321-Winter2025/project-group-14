import React, { useState, useEffect, useContext } from "react";
import { DatePicker } from "@mui/x-date-pickers";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import "./GameCopyGameDetailsPage.css";
import { AuthContext } from "../AuthContext";
import Button from "./Button"

const GameCopyCard = ({ gameCopyId, owner, description }) => {
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [startDate, setStartDate] = useState(null);
  const [endDate, setEndDate] = useState(null);
  const { user } = useContext(AuthContext);
  const [playerId, setPlayerId] = useState();

  useEffect(() => {
    fetch(`http://localhost:8080/players?person_id=${user.userId}`, {
      headers: { "Content-Type": "application/json", "User-Id": user.userId },
    })
      .then((response) => response.json())
      .then((data) => setPlayerId(data))
      .catch((error) => console.error("Error fetching player for user:", error));
  }, [user]);

  const handleBorrowClick = () => {
    setShowDatePicker(true);
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
      alert(
        `Borrow request submitted from ${startDate.format("YYYY-MM-DD")} to ${endDate.format("YYYY-MM-DD")}`
      );
      setShowDatePicker(false);
      setStartDate(null);
      setEndDate(null);
    } else {
      alert("Please select both start and end dates before submitting.");
    }
  };

  return (
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

      <div>
        {!showDatePicker ? (
          <div className="date-picker-container">
            <Button onClick={handleBorrowClick}>
            Ask to Borrow
            </Button>
            </div>
        ) : (
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
        )}
      </div>
      </div>
    </div>
  );
};

export default GameCopyCard;