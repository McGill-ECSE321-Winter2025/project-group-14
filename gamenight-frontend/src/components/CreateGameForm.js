import React, { useState, useContext, useRef } from "react";
import { 
  Box,
  TextField,
  Modal,
  Backdrop,
  Fade,
  Typography,
  Avatar,
  Stack
} from "@mui/material";
import { AuthContext } from "../AuthContext";
import Button from "./Button";
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import CloseIcon from '@mui/icons-material/Close';

const CreateGameForm = ({ open, onClose, onSuccess }) => {
  const { user } = useContext(AuthContext);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [imageFile, setImageFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [loading, setLoading] = useState(false);
  const fileInputRef = useRef(null);

  // Completely separate click handler for file input
  const handleFileButtonClick = (e) => {
    e.preventDefault(); // Prevent any form submission
    e.stopPropagation(); // Stop event bubbling
    fileInputRef.current.click();
  };

  const handleImageChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validation
    if (!file.type.startsWith('image/')) {
      alert('Please select an image file (JPEG, PNG, etc.)');
      return;
    }
    if (file.size > 2 * 1024 * 1024) {
      alert('Image must be smaller than 2MB');
      return;
    }
    
    setImageFile(file);
    setPreviewUrl(URL.createObjectURL(file));
  };

  const removeImage = () => {
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setImageFile(null);
    setPreviewUrl(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name || !description) {
      alert('Please fill in all required fields');
      return;
    }
    
    setLoading(true);
    
    try {
      const formData = new FormData();
      
      // Create game data as JSON blob
      const gameJson = JSON.stringify({ name, description });
      const gameBlob = new Blob([gameJson], { type: 'application/json' });
      formData.append('game', gameBlob);
      
      // Add image if exists
      if (imageFile) {
        formData.append('imageFile', imageFile);
      }

      const response = await fetch("http://localhost:8080/games", {
        method: "POST",
        headers: {
          "User-Id": user.userId
          // Let browser set Content-Type with boundary
        },
        body: formData
      });

      if (!response.ok) {
        const error = await response.json().catch(() => ({}));
        throw new Error(error.message || 'Failed to create game');
      }

      const newGame = await response.json();
      onSuccess(newGame);
      handleClose();
    } catch (error) {
      console.error("Error creating game:", error);
      alert(error.message || "Failed to create game. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    removeImage();
    setName("");
    setDescription("");
    onClose();
  };

  return (
    <Modal open={open} onClose={handleClose} closeAfterTransition BackdropComponent={Backdrop}>
      <Fade in={open}>
        <Box sx={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          width: 400,
          bgcolor: 'background.paper',
          boxShadow: 24,
          p: 4,
          borderRadius: 2
        }}>
          <Typography variant="h5" gutterBottom textAlign="center">Create New Game</Typography>
          
          <Box component="form" onSubmit={handleSubmit} noValidate>
            <TextField
              fullWidth
              label="Game Name *"
              value={name}
              onChange={(e) => setName(e.target.value)}
              margin="normal"
              required
            />
            
            <TextField
              fullWidth
              multiline
              rows={4}
              label="Description *"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              margin="normal"
              required
            />

            {/* File Upload Section - Now completely separate from form */}
            <Box sx={{ my: 2 }}>
              <input
                type="file"
                accept="image/*"
                onChange={handleImageChange}
                ref={fileInputRef}
                style={{ display: 'none' }}
                id="image-upload"
              />
              
              <Stack direction="row" spacing={2} alignItems="center">
                <Button
                  variant="outlined"
                  component="span"
                  startIcon={<CloudUploadIcon />}
                  onClick={handleFileButtonClick} // Using separate handler
                  sx={{ flex: 1 }}
                  type="button" // Crucial - prevents form submission
                >
                  Choose Image
                </Button>
                
                {previewUrl && (
                  <Button
                    variant="outlined"
                    color="error"
                    startIcon={<CloseIcon />}
                    onClick={removeImage}
                    type="button" // Crucial - prevents form submission
                  >
                    Remove
                  </Button>
                )}
              </Stack>
              
              {previewUrl && (
                <Box sx={{ mt: 2, textAlign: 'center' }}>
                  <Typography variant="caption">Preview:</Typography>
                  <Avatar
                    src={previewUrl}
                    variant="rounded"
                    sx={{
                      width: '100%',
                      height: 200,
                      mt: 1,
                      objectFit: 'contain'
                    }}
                  />
                </Box>
              )}
            </Box>

            <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 3 }}>
              <Button onClick={handleClose} disabled={loading} type="button">
                Cancel
              </Button>
              <Button type="submit" variant="contained" disabled={loading}>
                {loading ? 'Creating...' : 'Create Game'}
              </Button>
            </Box>
          </Box>
        </Box>
      </Fade>
    </Modal>
  );
};

export default CreateGameForm;