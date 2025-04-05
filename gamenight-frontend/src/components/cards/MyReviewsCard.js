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

  if (isEditing) {
    return (
      <div className="mygamesreview-card">
        <div className="header-section">
          <h3 className="review-game">{gameName || 'Untitled Game'}</h3>
          <div className="stars-container">
            {[1, 2, 3, 4, 5].map((starValue) => (
              <span
                key={starValue}
                className={`star ${editedRating >= starValue ? "filled" : ""}`}
                onClick={() => onRatingChange(starValue)}
              >
                ★
              </span>
            ))}
          </div>
          <div className="review-date">
            Posted on: {formattedDate}
          </div>
        </div>
        <div className="review-content">
          <textarea
            value={editedComment}
            onChange={onCommentChange}
            rows="5"
            className="review-comment-edit"
            onInput={(e) => {
              e.target.style.height = "auto";
              e.target.style.height = `${e.target.scrollHeight}px`;
            }}
          />
        </div>
        <div className="review-buttons">
          <Button type="success" onClick={onSaveEdit}>
            Save Changes
          </Button>
          <Button type="danger" onClick={onCancelEdit}>
            Cancel
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="mygamesreview-card"> 
      <div className="card-content">
        <div className="header-section">
          <h3 className="review-game">{gameName || 'Untitled Game'}</h3>
          <div className="stars-container">
            {Array.from({ length: 5 }, (_, i) => (
              <span 
                key={i} 
                className={`star ${i < Math.round(rating) ? "filled" : ""}`}
              >
                ★
              </span>
            ))}
          </div>
          <div className="review-date">
            Posted on: {formattedDate}
          </div>
        </div>

        <div className="review-content">
          <div className="review-comment">
            {comment}
          </div>
        </div>
      </div>
      
      <div className="review-buttons" style={{ 
        display: 'flex',
        justifyContent: 'center',
        gap: '1.5rem',
        padding: '1rem'
      }}>
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
      </div>
    </div>
  );
};

export default MyReviewsCard;