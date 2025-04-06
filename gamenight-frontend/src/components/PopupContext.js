import React, { createContext, useState, useContext } from 'react';

import Button from "./ui/Button";
import {
    Typography,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle
  } from "@mui/material";

  const PopupContext = createContext();

  export const usePopup = () => useContext(PopupContext);
  
  export const PopupProvider = ({ children }) => {
    const [popup, setPopup] = useState({ visible: false, message: '' });
  
    const showPopup = (message) => {
      setPopup({ visible: true, message });
    };
  
    const hidePopup = () => setPopup({ visible: false, message: '' });
  
    return (
      <PopupContext.Provider value={{ showPopup, hidePopup }}>
      {children}

      <Dialog open={popup.visible} onClose={hidePopup} sx={{ zIndex: 100000 }}>
        <DialogContent>
          <Typography>{popup.message}</Typography>
        </DialogContent>
        <DialogActions>
          <Button type="success" onClick={hidePopup}>
            Ok
          </Button>
        </DialogActions>
      </Dialog>
    </PopupContext.Provider>
    );
  };
