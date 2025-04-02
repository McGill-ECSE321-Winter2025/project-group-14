import React, { useState, useContext, useRef } from "react";
import { 
  Box,
  TextField,
  Modal,
  Backdrop,
  Fade,
  Typography,
  Avatar,
} from "@mui/material";
import { AuthContext } from "../AuthContext";
import Button from "./Button";
import CloudUploadIcon from '@mui/icons-material/CloudUpload';

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
              label="Game Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              margin="normal"
              required
            />
            
            <TextField
              fullWidth
              multiline
              rows={4}
              label="Description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              margin="normal"
              required
            />

            <Box sx={{ my: 2, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
              <input
                type="file"
                accept="image/*"
                onChange={handleImageChange}
                ref={fileInputRef}
                style={{ display: 'none' }}
                id="image-upload"
              />
              
              {!previewUrl && (
                <Button
                  variant="outlined"
                  component="span"
                  startIcon={<CloudUploadIcon />}
                  onClick={handleFileButtonClick}
                  sx={{ width: '100%', textAlign: 'center' }}
                  type="button"
                >
                  Choose Image
                </Button>
              )}
              
              {previewUrl && (
                <Box sx={{ width: '100%', mt: 2 }}>
                  <Avatar
                    src={previewUrl}
                    variant="rounded"
                    sx={{
                      width: '100%',
                      height: 200,
                      mt: 1,
                      objectFit: 'contain',
                      cursor: 'pointer'
                    }}
                    onClick={removeImage}
                  />
                  <Typography variant="caption" sx={{ display: 'block', textAlign: 'center', mt: 1 }}>
                    Click on the image to remove it
                  </Typography>
                </Box>
              )}
            </Box>

            <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mt: 3 }}>
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