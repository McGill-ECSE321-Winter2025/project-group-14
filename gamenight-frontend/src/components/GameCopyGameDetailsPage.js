import React, { useState, useEffect, useContext } from "react";
import { Card, CardContent, Typography, Button, Box } from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import "./GameCopyGameDetailsPage.css";
import { AuthContext } from "../AuthContext";

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
      const response = await fetch("http://localhost:8080/borrowingRequests", {
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
    <Card className="game-copy-card">
      <CardContent>
        <Typography variant="h6" className="owner-text">Owner: {owner}</Typography>
        <Typography variant="body2" className="description-text">{description}</Typography>
        <Box className="button-container">
          {!showDatePicker ? (
            <Button className="borrow-button" onClick={handleBorrowClick}>
              Ask to Borrow
            </Button>
          ) : (
            <Box className="date-picker-container">
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
              <Button className="submit-button" onClick={handleSubmit}>
                Submit Request
              </Button>
            </Box>
          )}
        </Box>
      </CardContent>
    </Card>
  );
};

export default GameCopyCard;