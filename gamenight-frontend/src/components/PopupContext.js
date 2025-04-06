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
        title: '',             // Optional title
        type: 'info',          // 'info' or 'confirm'
        onConfirm: null,       // Callback for confirm action
        confirmText: 'OK',
        cancelText: 'Cancel',
        confirmVariant: 'contained', // MUI Button variants: 'contained', 'outlined', 'text'
        confirmColor: 'primary',   // MUI Button colors: 'primary', 'secondary', 'error', 'warning', 'info', 'success'
        isProcessing: false   // Loading state for confirm button
    });

    const showPopup = useCallback((options) => {
        if (typeof options === 'string') {
            setPopup({
                open: true,
                message: options,
                title: '', // No title for simple info
                type: 'info',
                onConfirm: null, // No confirm action
                confirmText: 'OK',
                cancelText: 'Cancel', // Not shown
                confirmVariant: 'contained',
                confirmColor: 'primary',
                isProcessing: false
            });
        } else {
            const isConfirm = options.type === 'confirm';
            setPopup({
                open: true,
                message: options.message || '',
                title: options.title || (isConfirm ? 'Confirm Action' : ''), // Default title for confirm
                type: options.type || 'info',
                onConfirm: options.onConfirm || null,
                confirmText: options.confirmText || (isConfirm ? 'Confirm' : 'OK'), // Default button text
                cancelText: options.cancelText || 'Cancel',
                confirmVariant: options.confirmVariant || 'contained',
                confirmColor: options.confirmButtonVariant === 'danger' ? 'error' : (options.confirmColor || 'primary'),
                isProcessing: false // Always reset processing state when showing
            });
        }
    }, []); // useCallback depends on nothing external here

    const hidePopup = useCallback(() => {
        setPopup(prev => ({ ...prev, open: false, isProcessing: false }));
    }, []);

    const setProcessing = useCallback((processing) => {
        setPopup(prev => ({ ...prev, isProcessing: processing }));
    }, []);

    const handleConfirm = async () => {
        if (popup.type === 'info') {
            hidePopup(); // Simple OK button just closes it
        } else if (popup.type === 'confirm' && popup.onConfirm) {

             setProcessing(true); // Indicate processing starts
             try {
                 await popup.onConfirm();
                 hidePopup();
             } catch (error) {
                  console.error("Error during popup confirmation action:", error);
                  setProcessing(false);
             }
        } else {
            hidePopup(); // Default action if something is misconfigured
        }
    };

    return (
        <PopupContext.Provider value={{ showPopup, hidePopup, setProcessing }}> {/* Expose setProcessing */}
            {children}

            <Dialog open={popup.open} onClose={hidePopup} sx={{ zIndex: 100000 }}> {/* High z-index */}
                {popup.title && <DialogTitle>{popup.title}</DialogTitle>}
                <DialogContent>
                    {/* Render message - could be string or React node */}
                    {typeof popup.message === 'string' ? (
                        <Typography>{popup.message}</Typography>
                    ) : (
                        popup.message
                    )}
                </DialogContent>
                <DialogActions>
                    {/* Conditionally show Cancel button only for confirmation types */}
                    {popup.type === 'confirm' && (
                        <MuiButton
                            onClick={hidePopup}
                            size="small" // SMALLER BUTTON
                            variant='outlined' // Example style for cancel
                            disabled={popup.isProcessing} // Disable if confirm is processing
                        >
                            {popup.cancelText}
                        </MuiButton>
                    )}
                    {/* OK or Confirm button */}
                    <MuiButton
                        onClick={handleConfirm}
                        size="small" // SMALLER BUTTON
                        variant={popup.confirmVariant}
                        color={popup.confirmColor}
                        disabled={popup.isProcessing} // Disable while processing
                    >
                        {popup.isProcessing ? 'Processing...' : popup.confirmText}
                    </MuiButton>
                </DialogActions>
            </Dialog>
        </PopupContext.Provider>
    );
};