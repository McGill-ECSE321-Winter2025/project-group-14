import React, { useState } from "react";
import {
  Button,
  IconButton,
  TextField,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
} from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import "../../styles/card.css"; // ✅ Important: apply your custom styles

const GameCopyCard = ({ gameCopy, onDelete, onUpdate, isOwner }) => {
  const [editMode, setEditMode] = useState(false);
  const [editedDescription, setEditedDescription] = useState(gameCopy.description);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);

  const placeholderImage =
    "https://www.francetvinfo.fr/pictures/KI83JKIWxYVA8ng-cUtYxM6l-z8/1200x1200/2016/08/23/shrek-5.jpg";
  const image = gameCopy.game?.image || placeholderImage;

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
    <div className="game-card fade-in-card">
      <div className="game-card-image">
        <img src={image} alt={gameCopy.game?.name || "Game"} />
      </div>

      <div className="game-card-content">
        <h3>{gameCopy.game?.name || "Unknown Game"}</h3>

        {editMode ? (
          <TextField
            fullWidth
            multiline
            rows={3}
            value={editedDescription}
            onChange={(e) => setEditedDescription(e.target.value)}
            sx={{ mb: 1 }}
          />
        ) : (
          <Typography variant="body2" color="text.secondary">
            {gameCopy.description}
          </Typography>
        )}
      </div>

      {isOwner && (
        <div className="game-card-actions">
          {editMode ? (
            <>
              <Button size="small" onClick={handleCancelEdit}>Cancel</Button>
              <Button size="small" color="primary" onClick={handleSaveClick}>Save</Button>
            </>
          ) : (
            <>
              <IconButton onClick={handleEditClick}>
                <EditIcon />
              </IconButton>
              <IconButton onClick={handleDeleteClick}>
                <DeleteIcon />
              </IconButton>
            </>
          )}
        </div>
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
    </div>
  );
};

export default GameCopyCard;
