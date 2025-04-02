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

    return () => {
      if (imageUrl) {
        URL.revokeObjectURL(imageUrl);
      }
    };
  }, [gameCopy.game.id, imageUrl]);

  const handleEditClick = () => setEditMode(true);
  const handleSaveClick = () => {
    onUpdate({ ...gameCopy, description: editedDescription });
    setEditMode(false);
  };
  const handleCancelEdit = () => {
    setEditedDescription(gameCopy.description);
    setEditMode(false);
  };
  const handleDeleteClick = () => setDeleteConfirmOpen(true);
  const handleConfirmDelete = () => {
    onDelete(gameCopy.id);
    setDeleteConfirmOpen(false);
  };
  const handleCancelDelete = () => setDeleteConfirmOpen(false);

  return (
    <Card sx={{
      width: 280, // Slightly wider than the tiny version
      borderRadius: '14px',
      overflow: 'hidden',
      boxShadow: '0 3px 8px rgba(0,0,0,0.1)',
      transition: 'transform 0.2s ease',
      '&:hover': {
        transform: 'translateY(-3px)'
      }
    }}>
      {/* Image Section */}
      <Box sx={{
        position: 'relative',
        paddingTop: '60%', // Slightly taller aspect ratio
        backgroundColor: '#f5f5f5'
      }}>
        {imageLoading ? (
          <CircularProgress size={24} sx={{
            position: 'absolute',
            top: '50%',
            left: '50%',
            transform: 'translate(-50%, -50%)'
          }} />
        ) : (
          <CardMedia
            component="img"
            image={imageError ? '/default-game-image.jpg' : imageUrl}
            alt={gameCopy.game?.name || "Game image"}
            sx={{
              position: 'absolute',
              top: 0,
              left: 0,
              width: '100%',
              height: '100%',
              objectFit: 'cover'
            }}
          />
        )}
      </Box>

      {/* Content Section */}
      <CardContent sx={{ p: 2.5 }}>
        <Typography variant="subtitle1" sx={{ 
          mb: 1.5,
          fontWeight: 600,
          textAlign: 'center',
          fontSize: '1rem',
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis'
        }}>
          {gameCopy.game?.name || "Unknown Game"}
        </Typography>

        {editMode ? (
          <TextField
            fullWidth
            multiline
            rows={3}
            value={editedDescription}
            onChange={(e) => setEditedDescription(e.target.value)}
            sx={{ mb: 1.5 }}
            size="small"
          />
        ) : (
          <Typography variant="body2" sx={{ 
            mb: 1.5,
            color: 'text.secondary',
            textAlign: 'center',
            fontSize: '0.85rem',
            height: 60,
            overflow: 'hidden',
            display: '-webkit-box',
            WebkitLineClamp: 3,
            WebkitBoxOrient: 'vertical'
          }}>
            {gameCopy.description}
          </Typography>
        )}

        {/* Actions */}
        {isOwner && (
          <Box sx={{ 
            display: 'flex',
            justifyContent: 'center',
            gap: 1.5,
            mt: 1.5
          }}>
            {editMode ? (
              <>
                <Button 
                  variant="outlined" 
                  onClick={handleCancelEdit}
                  size="small"
                  sx={{ borderRadius: '18px', px: 2 }}
                >
                  Cancel
                </Button>
                <Button 
                  variant="contained" 
                  onClick={handleSaveClick}
                  size="small"
                  sx={{ borderRadius: '18px', px: 2 }}
                >
                  Save
                </Button>
              </>
            ) : (
              <>
                <IconButton 
                  aria-label="edit" 
                  onClick={handleEditClick}
                  sx={{ 
                    backgroundColor: 'primary.main',
                    color: 'white',
                    '&:hover': { backgroundColor: 'primary.dark' }
                  }}
                >
                  <EditIcon />
                </IconButton>
                <IconButton 
                  aria-label="delete" 
                  onClick={handleDeleteClick}
                  sx={{ 
                    backgroundColor: 'error.main',
                    color: 'white',
                    '&:hover': { backgroundColor: 'error.dark' }
                  }}
                >
                  <DeleteIcon />
                </IconButton>
              </>
            )}
          </Box>
        )}
      </CardContent>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteConfirmOpen} onClose={handleCancelDelete}>
        <DialogTitle sx={{ textAlign: 'center' }}>Confirm Delete</DialogTitle>
        <DialogContent sx={{ textAlign: 'center' }}>
          <Typography>Are you sure you want to delete this game copy?</Typography>
        </DialogContent>
        <DialogActions sx={{ justifyContent: 'center', pb: 3, px: 3 }}>
          <Button 
            onClick={handleCancelDelete}
            sx={{ borderRadius: '18px', px: 3 }}
          >
            Cancel
          </Button>
          <Button 
            onClick={handleConfirmDelete} 
            color="error"
            variant="contained"
            sx={{ borderRadius: '18px', px: 3 }}
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Card>
  );
};

export default GameCopyCard;