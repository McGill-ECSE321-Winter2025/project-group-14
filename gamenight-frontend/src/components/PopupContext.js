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
            sx={{ 
                '& .MuiPaper-root': {
                    borderRadius: '12px',
                    minWidth: '350px',
                    maxWidth: '90vw',
                    padding: '16px',
                    boxShadow: '0 4px 20px rgba(0,0,0,0.15)'
                }
            }}
            aria-labelledby="popup-dialog-title"
            aria-describedby="popup-dialog-description"
        >
            {popup.title && (
                <DialogTitle 
                    id="popup-dialog-title"
                    sx={{
                        fontSize: '1.25rem',
                        fontWeight: '600',
                        padding: '0 0 16px 0',
                        color: popup.confirmColor === 'error' ? '#d32f2f' : '#1976d2'
                    }}
                >
                    {popup.title}
                </DialogTitle>
            )}
            <DialogContent sx={{ padding: '8px 0 16px 0' }}>
                <Typography 
                    component="div" 
                    id="popup-dialog-description"
                    sx={{ 
                        fontSize: '1rem',
                        color: 'rgba(0, 0, 0, 0.87)',
                        lineHeight: '1.5'
                    }}
                >
                    {popup.message}
                </Typography>
            </DialogContent>
            <DialogActions sx={{ padding: '0', justifyContent: 'center'}}>
                {/* Conditionally show Cancel button only for 'confirm' type */}
                {popup.type === 'confirm' && (
                    <MuiButton
                        onClick={hidePopup}
                        size="small"
                        variant='outlined'
                        disabled={popup.isProcessing}
                        sx={{
                            textTransform: 'none',
                            borderRadius: '8px',
                            padding: '6px 16px',
                            marginRight: '8px',
                            borderColor: '#e0e0e0',
                            '&:hover': {
                                borderColor: '#bdbdbd'
                            }
                        }}
                    >
                        {popup.cancelText}
                    </MuiButton>
                )}
                {/* OK or Confirm button */}
                <MuiButton
                    onClick={handleConfirm}
                    size="small"
                    variant={popup.confirmVariant}
                    color={popup.confirmColor}
                    disabled={popup.isProcessing}
                    sx={{
                        textTransform: 'none',
                        borderRadius: '8px',
                        padding: '6px 16px',
                        fontWeight: '500',
                        boxShadow: 'none',
                        '&:hover': {
                            boxShadow: 'none',
                            backgroundColor: popup.confirmColor === 'error' ? '#c62828' : 
                                          popup.confirmColor === 'primary' ? '#1565c0' : '#1976d2'
                        }
                    }}
                >
                    {popup.isProcessing ? 'Processing...' : popup.confirmText}
                </MuiButton>
            </DialogActions>
        </Dialog>
        </PopupContext.Provider>
    );
};