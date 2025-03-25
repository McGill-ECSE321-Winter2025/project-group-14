import React, { useState } from "react";
import { Card, CardContent, Typography, Button, Box } from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers";
import { AdapterDayjs } from "@mui/x-date-pickers/AdapterDayjs";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import "./GameCopyGameDetailsPage.css";

const GameCopyCard = ({ owner, description }) => {
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [selectedDate, setSelectedDate] = useState(null);

  const handleBorrowClick = () => {
    setShowDatePicker(true);
  };

  const handleDateChange = (date) => {
    setSelectedDate(date);
  };

  const handleSubmit = () => {
    if (selectedDate) {
      console.log(`Borrow request sent for ${selectedDate.format("YYYY-MM-DD")}`);
      alert(`Borrow request submitted for ${selectedDate.format("YYYY-MM-DD")}`);
      setShowDatePicker(false);
      setSelectedDate(null);
    } else {
      alert("Please select a date before submitting.");
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
                <DatePicker label="Select a Date" value={selectedDate} onChange={handleDateChange} />
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

