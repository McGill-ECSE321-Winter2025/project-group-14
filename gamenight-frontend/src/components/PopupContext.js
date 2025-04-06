// src/components/PopupContext.js
import React, { createContext, useState, useContext, useCallback } from 'react';
import {
    Typography,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Button as MuiButton
} from "@mui/material";

const PopupContext = createContext();

export const usePopup = () => useContext(PopupContext);

export const PopupProvider = ({ children }) => {
    const [popup, setPopup] = useState({
        open: false,
        message: '',
        title: '', 
        type: 'info',
        onConfirm: null,
        confirmText: 'OK',
        cancelText: 'Cancel',
        confirmVariant: 'contained',
        confirmColor: 'primary',
        isProcessing: false
    });

    const showPopup = useCallback((options) => {
        if (typeof options === 'string') {
            // Handle simple info popup call: showPopup("My message")
            setPopup({
                open: true, message: options, title: '', type: 'info', onConfirm: null,
                confirmText: 'OK', cancelText: 'Cancel', confirmVariant: 'contained',
                confirmColor: 'primary', isProcessing: false
            });
        } else {
            const isConfirm = options.type === 'confirm';
            setPopup({
                open: true,
                message: options.message || '',
                title: options.title || (isConfirm ? 'Confirm Action' : ''),
                type: options.type || 'info',
                onConfirm: options.onConfirm || null,
                confirmText: options.confirmText || (isConfirm ? 'Confirm' : 'OK'),
                cancelText: options.cancelText || 'Cancel',
                confirmVariant: options.confirmVariant || 'contained',
                confirmColor: options.confirmButtonVariant === 'danger' ? 'error' : (options.confirmColor || 'primary'),
                isProcessing: false
            });
        }
    }, []);

    const hidePopup = useCallback(() => {
        setPopup(prev => ({ ...prev, open: false, isProcessing: false }));
    }, []);

    const setProcessing = useCallback((processing) => {
        setPopup(prev => ({ ...prev, isProcessing: processing }));
    }, []);

    const handleConfirm = async () => {
        if (popup.type === 'info') {
            hidePopup();
        } else if (popup.type === 'confirm' && popup.onConfirm) {
             setProcessing(true);
             try {
                 await popup.onConfirm(); 
                 hidePopup();
             } catch (error) {
                  console.error("Error during popup confirmation action:", error);
                  setProcessing(false);
             }
        } else {
            hidePopup();
        }
    };

    return (
        <PopupContext.Provider value={{ showPopup, hidePopup, setProcessing }}>
            {children}

            <Dialog
                open={popup.open}
                onClose={(event, reason) => {
                    if (reason === 'backdropClick' && popup.isProcessing) return;
                    if (!popup.isProcessing) hidePopup();
                }}
                sx={{ zIndex: 100000 }} // High z-index if needed
                aria-labelledby="popup-dialog-title"
                aria-describedby="popup-dialog-description"
            >
                {popup.title && <DialogTitle id="popup-dialog-title">{popup.title}</DialogTitle>}
                <DialogContent>
                    <Typography component="div" id="popup-dialog-description">{popup.message}</Typography>
                </DialogContent>
                <DialogActions>
                    {/* Conditionally show Cancel button only for 'confirm' type */}
                    {popup.type === 'confirm' && (
                        <MuiButton
                            onClick={hidePopup}
                            size="small" // Use MUI's small size
                            variant='outlined'
                            disabled={popup.isProcessing}
                        >
                            {popup.cancelText}
                        </MuiButton>
                    )}
                    {/* OK or Confirm button */}
                    <MuiButton
                        onClick={handleConfirm}
                        size="small" // Use MUI's small size
                        variant={popup.confirmVariant}
                        color={popup.confirmColor}
                        disabled={popup.isProcessing}
                    >
                        {popup.isProcessing ? 'Processing...' : popup.confirmText}
                    </MuiButton>
                </DialogActions>
            </Dialog>
        </PopupContext.Provider>
    );
};