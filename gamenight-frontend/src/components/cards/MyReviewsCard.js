import React from 'react';
import '../../styles/card.css';
import '../../styles/layout.css';
import '../../styles/animation.css';
import './MyReviewsCard.css';
import Button from '../ui/Button';
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import IconButton from '@mui/material/IconButton';

const MyReviewsCard = ({ 
  rating, 
  comment, 
  datePosted, 
  gameName,
  onEditClick,
  onDeleteClick,
  isEditing,
  editedComment,
  onCommentChange,
  editedRating,
  onRatingChange,
  onSaveEdit,
  onCancelEdit
}) => {
  const formattedDate = new Date(datePosted).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });

  return (
    <div className="mygamesreview-card">
      <div className="card-content">
        <div className="card-header">
          <div className="header-content">
            <h3 className="review-game">
              <span className="game-title">{gameName || 'Untitled Game'}</span>
            </h3>
            <div className="stars-container">
              {[1, 2, 3, 4, 5].map((starValue) => (
                <span
                  key={starValue}
                  className={`star ${(isEditing ? editedRating : rating) >= starValue ? "filled" : ""} ${isEditing ? "editable" : ""}`}
                  onClick={isEditing ? () => onRatingChange(starValue) : undefined}
                >
                  ★
                </span>
              ))}
            </div>
            <div className="review-date">
              Posted on: {formattedDate}
            </div>
          </div>
          
          <div className="header-actions">
            {isEditing ? (
              <>
                <Button type="success" onClick={onSaveEdit}>
                  Save
                </Button>
                <Button type="danger" onClick={onCancelEdit}>
                  Cancel
                </Button>
              </>
            ) : (
              <>
                <IconButton 
                  aria-label="edit" 
                  onClick={onEditClick}
                  sx={{ 
                    backgroundColor: '#0d90a4', 
                    color: '#ffffff', 
                    '&:hover': { backgroundColor: '#74e1eb' } 
                  }}
                >
                  <EditIcon />
                </IconButton>
                <IconButton 
                  aria-label="delete" 
                  onClick={onDeleteClick}
                  sx={{ 
                    backgroundColor: '#ff6574', 
                    color: '#ffffff', 
                    '&:hover': { backgroundColor: '#fcb559' } 
                  }}
                >
                  <DeleteIcon />
                </IconButton>
              </>
            )}
          </div>
        </div>

        <div className="review-content">
          {isEditing ? (
            <textarea
              value={editedComment}
              onChange={onCommentChange}
              rows="5"
              className="review-comment edit-mode"
              onInput={(e) => {
                e.target.style.height = "auto";
                e.target.style.height = `${e.target.scrollHeight}px`;
              }}
            />
          ) : (
            <div className="review-comment">
              {comment}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MyReviewsCard;