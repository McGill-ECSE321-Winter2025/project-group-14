import React, { useEffect, useState } from 'react';
import GameReview from '../../components/GameReview';
import { useParams } from 'react-router-dom';

const reviews = [
    {
      author: 'Jane Doe',
      rating: 4,
      comment: 'Great game! Very fun and addictive. I love the graphics and gameplay.',
      datePosted: '2025-03-22T10:00:00Z' // Example ISO 8601 date
    },
    {
      author: 'John Smith',
      rating: 2,
      comment: 'The game was okay, but the controls were hard to master and the levels felt repetitive.',
      datePosted: '2025-03-20T15:30:00Z' // Example ISO 8601 date
    },
  ];

const GameReviewsTab = () => {

    const { id } = useParams();

    // const [reviews, setReviews] = useState([]);

    // useEffect(() => {
    //   fetch(`http://localhost:8080/games/${id}/reviews`) // Adjust URL as needed
    //     .then((response) => response.json())
    //     .then((data) => setReviews(data))
    //     .catch((error) => console.error("Error fetching reviews:", error));
    // }, [id]);

    const [showReviewForm, setShowReviewForm] = useState(false); // Track if the review form is visible
    const [review, setReview] = useState(""); // Store the review input by the user
    const [rating, setRating] = useState(0); // Store the rating input by the user


    const handleReviewChange = (e) => {
        setReview(e.target.value);
    };

    const handleRatingChange = (ratingValue) => {
        setRating(ratingValue);
    };

    const handleSubmitReview = (e) => {
        e.preventDefault();
        // Here, you would send the review data to your backend (API)
        console.log("Review Submitted:", { review, rating });
        setShowReviewForm(false); // Close the form after submitting
    };

    const handleCancelReview = () => {
        setShowReviewForm(false); // Close the review form without submitting
    };

    return (
    <div>
        {/* Add Review Button */}
        {!showReviewForm && (
            <div className='container-center'>
            <button
            className="add-review-btn"
            onClick={() => setShowReviewForm(true)} // Open the review form
            >
            Add a Review
            </button>
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
                <button type="submit" className="submit-review-btn">Submit Review</button>
                <button type="button" className="cancel-review-btn" onClick={handleCancelReview}>Cancel</button>
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
                datePosted={review.datePosted} // Pass the date here
            />
        ))}
    </div>
)
}

export default GameReviewsTab;