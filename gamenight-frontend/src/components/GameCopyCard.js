import React, { useState, useEffect } from "react";
import { 
  Card, 
  CardContent, 
  CardMedia,
  Typography, 
  Button, 
  Box, 
  IconButton, 
  TextField,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  CircularProgress
} from "@mui/material";
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import "../App.css";

const GameCopyCard = ({ gameCopy, onDelete, onUpdate, isOwner }) => {
  const [editMode, setEditMode] = useState(false);
  const [editedDescription, setEditedDescription] = useState(gameCopy.description);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [imageUrl, setImageUrl] = useState(null);
  const [imageLoading, setImageLoading] = useState(true);
  const [imageError, setImageError] = useState(false);

  useEffect(() => {
    const fetchGameImage = async () => {
      try {
        if (!gameCopy?.game?.id) {
          setImageError(true);
          return;
        }
        
        const response = await fetch(`http://localhost:8080/games/${gameCopy.game.id}/image`);
        if (response.ok) {
          const imageBlob = await response.blob();
          const url = URL.createObjectURL(imageBlob);
          setImageUrl(url);
        } else {
          setImageError(true);
        }
      } catch (error) {
        console.error("Error fetching game image:", error);
        setImageError(true);
      } finally {
        setImageLoading(false);
      }
    };

    fetchGameImage();

    // Clean up the object URL when component unmounts
    return () => {
      if (imageUrl) {
        URL.revokeObjectURL(imageUrl);
      }
    };
  }, [gameCopy.game.id, imageUrl]);

  const handleEditClick = () => {
    setEditMode(true);
  };

  const handleSaveClick = () => {
    onUpdate({ ...gameCopy, description: editedDescription });
    setEditMode(false);
  };

  const handleCancelEdit = () => {
    setEditedDescription(gameCopy.description);
    setEditMode(false);
  };

  const handleDeleteClick = () => {
    setDeleteConfirmOpen(true);
  };

  const handleConfirmDelete = () => {
    onDelete(gameCopy.id);
    setDeleteConfirmOpen(false);
  };

  const handleCancelDelete = () => {
    setDeleteConfirmOpen(false);
  };

  return (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Game Image Section */}
      {imageLoading ? (
        <Box sx={{ 
          height: 140, 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center',
          backgroundColor: '#f5f5f5'
        }}>
          <CircularProgress size={24} />
        </Box>
      ) : (
        <CardMedia
          component="img"
          height="140"
          image={imageError ? '/default-game-image.jpg' : imageUrl}
          alt={gameCopy.game?.name || "Game image"}
          sx={{ 
            objectFit: 'cover',
            backgroundColor: '#f5f5f5'
          }}
        />
      )}

      <CardContent sx={{ flexGrow: 1 }}>
        <Typography variant="h6" gutterBottom>
          {gameCopy.game?.name || "Unknown Game"}
        </Typography>
        
        {editMode ? (
          <TextField
            fullWidth
            multiline
            rows={3}
            value={editedDescription}
            onChange={(e) => setEditedDescription(e.target.value)}
            sx={{ mb: 2 }}
          />
        ) : (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {gameCopy.description}
          </Typography>
        )}
      </CardContent>

      {isOwner && (
        <Box sx={{ p: 2, display: 'flex', justifyContent: 'space-between' }}>
          {editMode ? (
            <>
              <Button size="small" onClick={handleCancelEdit}>
                Cancel
              </Button>
              <Button size="small" color="primary" onClick={handleSaveClick}>
                Save
              </Button>
            </>
          ) : (
            <>
              <IconButton aria-label="edit" onClick={handleEditClick}>
                <EditIcon />
              </IconButton>
              <IconButton aria-label="delete" onClick={handleDeleteClick}>
                <DeleteIcon />
              </IconButton>
            </>
          )}
        </Box>
      )}
      
      <Dialog open={deleteConfirmOpen} onClose={handleCancelDelete}>
        <DialogTitle>Confirm Delete</DialogTitle>
        <DialogContent>
          <Typography>Are you sure you want to delete this game copy?</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancelDelete}>Cancel</Button>
          <Button onClick={handleConfirmDelete} color="error">Delete</Button>
        </DialogActions>
      </Dialog>
    </Card>
  );
};

export default GameCopyCard;