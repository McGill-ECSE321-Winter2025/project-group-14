import React, { useEffect, useState, useContext } from 'react';
import GameReview from '../../components/ui/GameReview';
import { useParams } from 'react-router-dom';
import { AuthContext } from "../../AuthContext";
import Button from '../../components/ui/Button';
import '../../styles/tabs.css';
import '../../styles/animation.css';
import '../../components/ui/GameReview.css';


const GameReviewsTab = () => {

    const { id } = useParams();

    const [reviews, setReviews] = useState([]);
    const { user } = useContext(AuthContext);
    const [playerId, setPlayerId] = useState();
    const [reloadReviews, setReloadReviews] = useState(true);

    useEffect(() => {
        fetch(`http://localhost:8080/players?person_id=${user.userId}`, {
            headers: { 'Content-Type': 'application/json', "User-Id": user.userId }
        })
            .then((response) => response.json())
            .then((data) => setPlayerId(data))
            .catch((error) => console.error("Error fetching player for user:", error));
    }, [user]);

    useEffect(() => {
        if (reloadReviews) {
            fetch(`http://localhost:8080/games/${id}/reviews`, {
                headers: { 'Content-Type': 'application/json', "User-Id": user.userId }
            })
                .then((response) => response.json())
                .then((data) => setReviews(data))
                .catch((error) => console.error("Error fetching reviews:", error));
            setReloadReviews(false)
        }
    }, [user, id, reloadReviews]);

    const [showReviewForm, setShowReviewForm] = useState(false); // Track if the review form is visible
    const [review, setReview] = useState(""); // Store the review input by the user
    const [rating, setRating] = useState(0); // Store the rating input by the user


    const handleReviewChange = (e) => {
        setReview(e.target.value);
    };

    const handleRatingChange = (ratingValue) => {
        setRating(ratingValue);
    };

    const handleSubmitReview = async (e) => {
        e.preventDefault();

        const response = await fetch('http://localhost:8080/reviews/', {
            method: 'POST',
            body: JSON.stringify({
                reviewId: 0,
                rating: rating,
                comment: review,
                reviewerId: playerId,
                gameId: id,
                author: ""
            }),
            headers: { 'Content-Type': 'application/json', "User-Id": user.userId }
        })
            .catch(error => console.error('Error:', error));;

        console.log("Review Submitted:", { review, rating, user }, "\nReponse:", response);
        setShowReviewForm(false);
        setReloadReviews(true)
        setRating(0)
        setReview("");
    };

    const handleCancelReview = () => {
        setShowReviewForm(false);
        setRating(0)
        setReview("");
    };

    return (
        <div className='container'>
            {/* Add Review Button */}
            {!showReviewForm && (
                <div className="centered">
                    <Button type="success" onClick={() => setShowReviewForm(true)}>Add a review</Button>
                </div>
            )}

            {/* Review Form */}
            {showReviewForm && (
                <div className='review-form-container'>
                    <form onSubmit={handleSubmitReview} className="review-form">
                        {/* Rating Section: Star Rating */}
                        <div className="rating">
                            <label>Rating:</label>
                            <div className="stars">
                                {[1, 2, 3, 4, 5].map((starValue) => (
                                    <span
                                        key={starValue}
                                        className="star"
                                        onClick={() => handleRatingChange(starValue)} // Click handler to update rating
                                    >
                                        {rating >= starValue ? '★' : '☆'}
                                    </span>
                                ))}
                            </div>
                        </div>

                        {/* Review Section */}
                        <div className="review-comment">
                            <label>Review</label>
                            <textarea
                                value={review}
                                onChange={handleReviewChange}
                                placeholder="Write your review here..."
                                rows="5"
                                required
                            />
                        </div>

                        {/* Submit and Cancel Buttons */}
                        <div className="review-buttons">
                            <Button type="success">Submit review</Button>
                            <Button type="danger" onClick={handleCancelReview}>Cancel</Button>
                        </div>
                    </form>
                </div>
            )}

            {reviews.map((review, index) => (
                <GameReview
                    key={index}
                    author={review.author}
                    rating={review.rating}
                    comment={review.comment}
                    datePosted={review.datePosted || "1970-01-01 00:00:00"} // Pass the date here
                />
            ))}
        </div>
    )
}

export default GameReviewsTab;