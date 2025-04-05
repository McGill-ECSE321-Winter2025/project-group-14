import React, { useEffect, useState, useContext } from 'react';
import { useParams } from 'react-router-dom';

import GameReview from '../../pages/GameDetails/GameReview';
import Button from '../../components/ui/Button';
import { AuthContext } from '../../AuthContext';

import '../../styles/card-with-user.css';
import '../../styles/layout.css';
import '../../styles/card.css';
import '../../styles/animation.css';
import './GameReviewsTab.css'
import '../../styles/rating.css'

import { usePopup } from '../../components/PopupContext';

const GameReviewsTab = () => {

    const { id } = useParams();

    const [reviews, setReviews] = useState([]);
    const { user } = useContext(AuthContext);
    const [playerId, setPlayerId] = useState();
    const [reloadReviews, setReloadReviews] = useState(true);
    const [userObject, setUserObject] = useState();
    const [review, setReview] = useState("");
    const [rating, setRating] = useState(0);
    const { showPopup } = usePopup();

    useEffect(() => {
        fetch(`http://localhost:8080/users/${user.userId}/player-id`, {
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

    useEffect(() => {
        fetch(`http://localhost:8080/users/${user.userId}`, {
            headers: { 'Content-Type': 'application/json', "User-Id": user.userId }
        })
            .then((response) => response.json())
            .then((data) => setUserObject(data))
            .catch((error) => console.error("Error fetching reviews:", error));
        setReloadReviews(false)
    }, [user]);

    const handleReviewChange = (e) => {
        setReview(e.target.value);
    };

    const handleRatingChange = (ratingValue) => {
        setRating(ratingValue);
    };

    const handleSubmitReview = async (e) => {
        e.preventDefault();
        if (rating === 0) {
            showPopup("Please enter a rating");
        } else {
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
                .catch(error => {
                    console.error('Error:', error);
                    showPopup("The review could not be processed. Try again later.");
                }
                );

            console.log("Review Submitted:", { review, rating, user }, "\nReponse:", response);
            setReloadReviews(true)
            setRating(0)
            setReview("");
        }
    };

    const handleCancelReview = () => {
        setRating(0)
        setReview("");
    };

    return (
        <div>
            <div className='card' style={{ "minWidth": "100%" }}>
                <div className="card-content">
                    <form onSubmit={handleSubmitReview}>

                        <div className="user-section">
                            <div className="avatar">
                                {userObject?.name?.charAt(0).toUpperCase()}
                            </div>
                            <div className="user-details">
                                <h3 className="user-name">{userObject?.name}</h3>
                            </div>
                        </div>
                        <div>
                            <div className="rating">
                                {[1, 2, 3, 4, 5].map((starValue) => (

                                    <span
                                        key={starValue}
                                        className="stars"
                                        onClick={() => handleRatingChange(starValue)}
                                    >
                                        <span className={rating >= starValue ? "filled" : ""}>★</span>
                                    </span>
                                ))}
                            </div>
                        </div>
                        <div className="shaded-section">
                            <textarea
                                value={review}
                                onChange={handleReviewChange}
                                placeholder="Write your review here..."
                                rows="5"
                                required
                                className='textarea'
                                onInput={(e) => {
                                    e.target.style.height = "auto"; // Reset height
                                    e.target.style.height = `${e.target.scrollHeight}px`; // Adjust to content
                                }}
                            />
                        </div>
                        {/* Submit and Cancel Buttons */}
                        <div className="game-review-buttons">
                            <Button type="danger" onClick={handleCancelReview}>Cancel</Button>
                            <Button type="success">Submit review</Button>
                        </div>
                    </form>
                </div>
            </div >

            {
                reviews.map((review, index) => (
                    <GameReview
                        key={index}
                        author={review.author}
                        rating={review.rating}
                        comment={review.comment}
                        datePosted={review.datePosted || "1970-01-01 00:00:00"} // Pass the date here
                    />
                ))
            }
        </div>
    )
}

export default GameReviewsTab;