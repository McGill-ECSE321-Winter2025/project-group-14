import React, { useState, useContext, useRef } from "react";
import { 
  Box,
  TextField,
  Modal,
  Backdrop,
  Fade,
  Typography,
  FormControl,
} from "@mui/material";
import { AuthContext } from "../../AuthContext";
import Button from "../ui/Button";
import CloudUploadIcon from '@mui/icons-material/CloudUpload';

const CreateGameForm = ({ open, onClose, onSuccess }) => {
  const { user } = useContext(AuthContext);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [imageFile, setImageFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [loading, setLoading] = useState(false);
  const fileInputRef = useRef(null);

  const handleFileButtonClick = (e) => {
    e.preventDefault();
    e.stopPropagation();
    fileInputRef.current.click();
  };

  const handleImageChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      return;
    }
    if (file.size > 2 * 1024 * 1024) {
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

    setLoading(true);

    try {
      const formData = new FormData();
      const gameJson = JSON.stringify({ name, description });
      const gameBlob = new Blob([gameJson], { type: 'application/json' });
      formData.append('game', gameBlob);
      
      if (imageFile) {
        formData.append('imageFile', imageFile);
      }

      const response = await fetch("http://localhost:8080/games", {
        method: "POST",
        headers: {
          "User-Id": user.userId
        },
        body: formData
      });

      if (!response.ok) {
        throw new Error('Failed to create game');
      }

      const newGame = await response.json();
      onSuccess(newGame);
      handleClose();
    } catch (error) {
      console.error("Error creating game:", error);
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
          borderRadius: 2,
          border: '1px solid #ddd'
        }}>
          <Typography variant="h5" gutterBottom textAlign="center" sx={{ mb: 3 }}>
            Create New Game
          </Typography>
          
          <Box component="form" onSubmit={handleSubmit} >
            <FormControl fullWidth sx={{ mb: 2 }}>
              <TextField
                label="Game Name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </FormControl>
            
            <FormControl fullWidth sx={{ mb: 2 }}>
              <TextField
                multiline
                rows={4}
                label="Description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                required
              />
            </FormControl>

            <Box sx={{ mb: 2, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
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
                <Box sx={{ width: '100%', mt: 1 }}>
                  <Box
                    component="img"
                    src={previewUrl}
                    sx={{
                      width: '100%',
                      height: 'auto',
                      maxHeight: 300,
                      display: 'block',
                      objectFit: 'contain',
                      cursor: 'pointer',
                      borderRadius: 1,
                      border: '1px solid rgba(0, 0, 0, 0.12)'
                    }}
                    onClick={removeImage}
                  />
                  <Typography variant="caption" sx={{ display: 'block', textAlign: 'center', mt: 1 }}>
                    Click on the image to remove it
                  </Typography>
                </Box>
              )}
            </Box>

                      <Box sx={{ 
            display: 'flex', 
            justifyContent: 'center', 
            gap: 2, 
            width: '100%',
            mt: 3
          }}>
            <Button
              type="danger"
              onClick={handleClose}
              disabled={loading}
              style={{ width: '120px' }}
            >
              Cancel
            </Button>
            <Button
              type="success"
              disabled={loading}
              style={{ width: '120px' }}
            >
              {loading ? 'Creating...' : 'Create'}
            </Button>
          </Box>

          </Box>
        </Box>
      </Fade>
    </Modal>
  );
};

export default CreateGameForm;