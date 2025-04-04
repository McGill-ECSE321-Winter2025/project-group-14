import React, { useEffect, useState } from 'react';
import { useAuth } from '../../AuthContext';
import MyReviewsCard from '../../components/cards/MyReviewsCard';
import Button from '../../components/ui/Button';
import '../../styles/layout.css';
import '../../styles/card.css';
import '../../styles/animation.css';
import { Modal, Backdrop, Fade, Box } from '@mui/material';

const MyReviews = () => {
    const { user } = useAuth();
    const [reviews, setReviews] = useState([]);
    const [gamesData, setGamesData] = useState({}); 
    const [playerId, setPlayerId] = useState(null);
    const [editingReviewId, setEditingReviewId] = useState(null);
    const [editedComment, setEditedComment] = useState('');
    const [editedRating, setEditedRating] = useState(0);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [deleteModalOpen, setDeleteModalOpen] = useState(false);
    const [reviewToDelete, setReviewToDelete] = useState(null);

    useEffect(() => {
        if (!user?.userId) return;
    
        const fetchData = async () => {
            setIsLoading(true);
            setError(null);
            try {
                const playerIdResponse = await fetch(
                    `http://localhost:8080/users/${user.userId}/player-id`,
                    { headers: { 'Content-Type': 'application/json', "User-Id": user.userId } }
                );
                const playerIdData = await playerIdResponse.json();
                setPlayerId(playerIdData);
    
                const reviewsResponse = await fetch(
                    `http://localhost:8080/users/${playerIdData}/reviews`,
                    { headers: { 'Content-Type': 'application/json', "User-Id": user.userId } }
                );
                const reviewsData = await reviewsResponse.json();
                
                const gameIds = [...new Set(reviewsData.map(review => review.gameId))];
                const gamesPromises = gameIds.map(gameId => 
                    fetch(`http://localhost:8080/games/${gameId}`,
                        { headers: { 'Content-Type': 'application/json', "User-Id": user.userId } })
                        .then(res => res.json())
                );
                
                const gamesResults = await Promise.all(gamesPromises);
                const gamesMap = {};
                gamesResults.forEach(game => {
                    gamesMap[game.id] = game;
                });
                
                setGamesData(gamesMap);
                setReviews(reviewsData);
            } catch (err) {
                console.error("Error fetching data:", err);
                setError("Failed to load reviews. Please try again.");
            } finally {
                setIsLoading(false);
            }
        };
        fetchData();
    }, [user]);

    const refreshReviews = async () => {
        try {
            const response = await fetch(
                `http://localhost:8080/users/${playerId}/reviews`,
                { headers: { 'Content-Type': 'application/json', "User-Id": user.userId } }
            );
            const data = await response.json();
            setReviews(data);
        } catch (error) {
            console.error("Refresh failed:", error);
            throw error; 
        }
    };

    const handleDeleteClick = (reviewId) => {
        setReviewToDelete(reviewId);
        setDeleteModalOpen(true);
    };

    const handleConfirmDelete = async () => {
        if (!reviewToDelete) return;
        
        try {
            const response = await fetch(`http://localhost:8080/reviews/${reviewToDelete}`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json', "User-Id": user.userId }
            });
            
            if (response.status === 204) {
                await refreshReviews();
            } else {
                throw new Error(`Failed to delete review: ${response.status}`);
            }
        } catch (error) {
            console.error("Error deleting review:", error);
            setError(error.message);
        } finally {
            setDeleteModalOpen(false);
            setReviewToDelete(null);
        }
    };

    const handleCancelDelete = () => {
        setDeleteModalOpen(false);
        setReviewToDelete(null);
    };

    const handleEditReview = (review) => {
        setEditingReviewId(review.reviewId);
        setEditedComment(review.comment);
        setEditedRating(review.rating);
    };

    const handleCancelEdit = () => {
        setEditingReviewId(null);
        setEditedComment('');
        setEditedRating(0);
    };

    const handleSaveEdit = async () => {
        if (!editingReviewId) {
            setError("No review selected for editing");
            return;
        }
    
        try {
            const originalReview = reviews.find(review => review.reviewId === editingReviewId);
            
            if (!originalReview) {
                setError("Original review not found");
                return;
            }
    
            const response = await fetch(`http://localhost:8080/reviews/${editingReviewId}`, {
                method: 'PUT',
                headers: { 
                    'Content-Type': 'application/json', 
                    "User-Id": user.userId 
                },
                body: JSON.stringify({
                    reviewId: originalReview.reviewId,
                    rating: editedRating,
                    comment: editedComment,
                    reviewerId: originalReview.reviewerId,
                    gameId: originalReview.gameId,
                    author: originalReview.author,
                    datePosted: originalReview.datePosted
                })
            });
    
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
    
            await refreshReviews();
            setEditingReviewId(null);
        } catch (error) {
            console.error("Error updating review:", error);
            setError(error.message);
        }
    };

    const handleRatingChange = (ratingValue) => {
        setEditedRating(ratingValue);
    };

    const modalStyle = {
        position: 'absolute',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        width: 400,
        bgcolor: 'background.paper',
        boxShadow: 24,
        p: 4,
        borderRadius: 2
    };

    if (isLoading) return <p>Loading reviews...</p>;
    if (error) return <p className="error-message">{error}</p>;

    return (
        <div>
            <h2>My Reviews</h2>
            {reviews.length === 0 ? (
                <p>You haven't submitted any reviews yet.</p>
            ) : (
                reviews.map((review) => (
                    <div key={review.reviewId} className="mygamesreview-card">
                        <div className="card-content">
                            {editingReviewId === review.reviewId ? (
                                <>
                                    <div className="header-section">
                                        <h3 className="review-game">
                                            {gamesData[review.gameId]?.name || 'Loading game...'}
                                        </h3>
                                        <div className="stars-container">
                                            {[1, 2, 3, 4, 5].map((starValue) => (
                                                <span
                                                    key={starValue}
                                                    className={`star ${editedRating >= starValue ? "filled" : ""}`}
                                                    onClick={() => handleRatingChange(starValue)}
                                                >
                                                    ★
                                                </span>
                                            ))}
                                        </div>
                                        <div className="review-date">
                                            Posted on: {new Date(review.datePosted).toLocaleDateString('en-US', {
                                                year: 'numeric',
                                                month: 'long',
                                                day: 'numeric'
                                            })}
                                        </div>
                                    </div>
                                    <div className="review-content">
                                        <textarea
                                            value={editedComment}
                                            onChange={(e) => setEditedComment(e.target.value)}
                                            rows="5"
                                            className="review-comment-edit"
                                            onInput={(e) => {
                                                e.target.style.height = "auto";
                                                e.target.style.height = `${e.target.scrollHeight}px`;
                                            }}
                                        />
                                    </div>
                                    <div className="review-buttons">
                                        <Button type="success" onClick={handleSaveEdit}>
                                            Save Changes
                                        </Button>
                                        <Button type="danger" onClick={handleCancelEdit}>
                                            Cancel
                                        </Button>
                                    </div>
                                </>
                            ) : (
                                <>
                                    <MyReviewsCard
                                    rating={review.rating}
                                    comment={review.comment}
                                    datePosted={review.datePosted || "1970-01-01 00:00:00"}
                                    gameName={gamesData[review.gameId]?.name || 'Loading game...'}
                                    />
                                    <div className="review-buttons">
                                        <Button onClick={() => handleEditReview(review)}>
                                            Edit
                                        </Button>
                                        <Button type="danger" onClick={() => handleDeleteClick(review.reviewId)}>
                                            Delete
                                        </Button>
                                    </div>
                                </>
                            )}
                        </div>
                    </div>
                ))
            )}

            {/* Delete Confirmation Modal */}
            <Modal
                open={deleteModalOpen}
                onClose={handleCancelDelete}
                closeAfterTransition
                BackdropComponent={Backdrop}
                BackdropProps={{ timeout: 500 }}
            >
                <Fade in={deleteModalOpen}>
                    <Box sx={modalStyle}>
                        <h3>Confirm Deletion</h3>
                        <p>Are you sure you want to delete this review?</p>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '20px' }}>
                            <Button onClick={handleCancelDelete} style={{ marginRight: '10px' }}>
                                Cancel
                            </Button>
                            <Button type="danger" onClick={handleConfirmDelete}>
                                Delete
                            </Button>
                        </div>
                    </Box>
                </Fade>
            </Modal>
        </div>
    );
};

export default MyReviews;