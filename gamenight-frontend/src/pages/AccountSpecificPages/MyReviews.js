import React, { useEffect, useState } from 'react';
import { useAuth } from '../../AuthContext';
import GameReview from '../../pages/GameDetails/GameReview';
import Button from '../../components/ui/Button';
import '../../pages/GameDetails/GameReview.css';
import '../../styles/layout.css';
import '../../styles/card.css';
import '../../styles/animation.css';

const MyReviews = () => {
    const { user } = useAuth();
    const [reviews, setReviews] = useState([]);
    const [playerId, setPlayerId] = useState(null);
    const [editingReviewId, setEditingReviewId] = useState(null);
    const [editedComment, setEditedComment] = useState('');
    const [editedRating, setEditedRating] = useState(0);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (!user?.userId) return;
    
        const fetchData = async () => {
            setIsLoading(true);
            setError(null);
            try {
                // Fetch player ID
                console.log("Fetching data for user ID:", user.userId);
                const playerIdResponse = await fetch(
                    `http://localhost:8080/users/${user.userId}/player-id`,
                    { headers: { 'Content-Type': 'application/json', "User-Id": user.userId } }
                );
                const playerIdData = await playerIdResponse.json();
                console.log("Received player ID:", playerIdData); // Log player ID
                setPlayerId(playerIdData);
    
                // Fetch reviews
                const reviewsResponse = await fetch(
                    `http://localhost:8080/users/${playerIdData}/reviews`,
                    { headers: { 'Content-Type': 'application/json', "User-Id": user.userId } }
                );
                const reviewsData = await reviewsResponse.json();
                // 2. Log the complete reviews data structure
                console.log("Full reviews data structure:", reviewsData);
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
    const handleDeleteReview = async (reviewId) => {
        if (!window.confirm("Are you sure you want to delete this review?")) return
        try {
            const response = await fetch(`http://localhost:8080/reviews/${reviewId}`, {
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
        }
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
            const response = await fetch(`http://localhost:8080/reviews/${editingReviewId}`, {
                method: 'PUT',
                headers: { 
                    'Content-Type': 'application/json', 
                    "User-Id": user.userId 
                },
                body: JSON.stringify({
                    rating: editedRating,
                    comment: editedComment
                })
            });
    
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || "Failed to update review");
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

    if (isLoading) return <p>Loading reviews...</p>;
    if (error) return <p className="error-message">{error}</p>;

    return (
        <div>
            <h2>My Reviews</h2>
            {reviews.length === 0 ? (
                <p>You haven't submitted any reviews yet.</p>
            ) : (
                reviews.map((review) => (
                    <div key={review.reviewId} className="request-card">
                        <div className="card-content">
                            {editingReviewId === review.reviewId ? (
                                <>
                                    <div className="user-section">
                                        <div className="avatar">
                                            {review.author?.charAt(0).toUpperCase()}
                                        </div>
                                        <div className="user-details">
                                            <h3 className="user-name">{review.author}</h3>
                                        </div>
                                    </div>
                                    <div className="rating">
                                        <div className="stars">
                                            {[1, 2, 3, 4, 5].map((starValue) => (
                                                <span
                                                    key={starValue}
                                                    className="star"
                                                    onClick={() => handleRatingChange(starValue)}
                                                >
                                                    <span className={editedRating >= starValue ? "filled" : ""}>★</span>
                                                </span>
                                            ))}
                                        </div>
                                    </div>
                                    <div className="game-section">
                                        <textarea
                                            value={editedComment}
                                            onChange={(e) => setEditedComment(e.target.value)}
                                            rows="5"
                                            className='game-section-textarea'
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
                                    <GameReview
                                        author={review.author}
                                        rating={review.rating}
                                        comment={review.comment}
                                        datePosted={review.datePosted || "1970-01-01 00:00:00"}
                                    />
                                    <div className="review-buttons">
                                        <Button onClick={() => handleEditReview(review)}>
                                            Edit
                                        </Button>
                                        <Button type="danger" onClick={() => handleDeleteReview(review.reviewId)}>
                                            Delete
                                        </Button>
                                    </div>
                                </>
                            )}
                        </div>
                    </div>
                ))
            )}
        </div>
    );
};

export default MyReviews;