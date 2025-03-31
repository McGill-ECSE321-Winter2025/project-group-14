import React, { useEffect, useState } from "react";
import { GameAPI } from "../GettingAllGamesAPI";
import GameCard from "../components/GameCard";
import "../App.css";

function Home() {
    const [games, setGames] = useState([]);
    const [carouselIndex, setCarouselIndex] = useState(2);
    const [fading, setFading] = useState(false);

    useEffect(() => {
        fetchGames();
    }, []);

    useEffect(() => {
        if (games.length < 10) return;

        const interval = setInterval(() => {
            if (carouselIndex >= 7) {
                setFading(true);
                setTimeout(() => {
                    fetchGames();
                    setCarouselIndex(2);
                    setFading(false);
                }, 600);
            } else {
                setCarouselIndex(prev => prev + 1);
            }
        }, 1500);

        return () => clearInterval(interval);
    }, [carouselIndex, games.length]);

    const fetchGames = async () => {
        const randomGames = await GameAPI.getRandomGames();
        setGames(randomGames);
    };
    useEffect(() => {
        const handleScroll = () => {
            document.querySelectorAll('.fade-in-on-scroll').forEach(section => {
                const rect = section.getBoundingClientRect();
                if (rect.top < window.innerHeight - 100) {
                    section.classList.add('visible');
                }
            });
        };

        window.addEventListener('scroll', handleScroll);
        handleScroll(); // run on mount too

        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    return (
        <div className="marketing-page">

            {/* --- HERO --- */}
            <section className="hero">
                <h1>Level Up Your Board Game Experience</h1>
                <p>Connect. Share. Play. Organize. Your ultimate board game hub.</p>
            </section>
            <h1 className="centered" style={{ marginTop: '2em' }}>Pick your game</h1>
            {/* --- CAROUSEL --- */}
            <div className={`carousel-wrapper ${fading ? "fading-out" : "fading-in"}`}>
                {games.map((game, i) => {
                    const offset = i - carouselIndex;
                    if (offset < -2 || offset > 2) return null;

                    const scale = 1 - Math.abs(offset) * 0.15;
                    const opacity = 1 - Math.abs(offset) * 0.25;
                    const translateX = offset * 250;

                    return (
                        <div
                            onClick={() => window.location.href = '/login'}
                            key={i}
                            className="carousel-item"
                            style={{
                                transform: `translateX(${translateX}px) scale(${scale})`,
                                opacity,
                            }}
                        >
                            <GameCard
                                title={game.name}
                                image={"https://cdn.mos.cms.futurecdn.net/DCNoD5GWBhpHbkybMGt33X-1000-80.jpg"}
                                rating={game.rating}
                            />
                        </div>
                    );
                })}
            </div>
            <section className="features fade-in-on-scroll">
                <h2>Decide</h2>
                <div className="feature-list">
                    <div className="feature-card">
                        <img src="https://img.icons8.com/fluency/96/borrow-book.png" alt="borrow" />
                        <h3>Borrow</h3>
                        <p>Browse available games from the community and borrow what catches your eye.</p>
                    </div>
                    <div className="feature-card">
                        <img src="https://img.icons8.com/?size=256w&id=LmqydxIMVBRX&format=png" alt="lend" />
                        <h3>Lend</h3>
                        <p>Share your board games, help others discover new favorites, and earn community trust.</p>
                    </div>
                    <div className="feature-card">
                        <img src="https://img.icons8.com/fluency/96/conference-call.png" alt="play" />
                        <h3>Play with Others</h3>
                        <p>Meet local players, make new friends, and dive into the vibrant world of board gaming together.</p>
                    </div>
                    <div className="feature-card">
                        <img src="https://img.icons8.com/?size=80&id=uJOoDz71OZQk&format=png" alt="learn" />
                        <h3>Learn</h3>
                        <p>Explore guides, tutorials, and community tips to master your favorite games.</p>
                    </div>
                </div>
            </section>


            {/* --- FEATURES --- */}
            <section className="features fade-in-on-scroll">
                <h2>Play!</h2>
                <div className="feature-list">
                    <div className="feature-card">
                        <img src="https://cdn-icons-png.flaticon.com/512/3430/3430778.png" alt="games" />
                        <h3>Massive Game Library</h3>
                        <p>Browse, review, and access thousands of board games. No limits, pure fun.</p>
                    </div>
                    <div className="feature-card">
                        <img src="https://img.icons8.com/fluency/96/calendar.png" alt="calendar" />
                        <h3>Event Creation</h3>
                        <p>Organize game nights with ease. Choose the game, date, and invite friends.</p>
                    </div>
                    <div className="feature-card">
                        <img src="https://cdn-icons-png.flaticon.com/512/3090/3090423.png" alt="community" />
                        <h3>Connect with Players</h3>
                        <p>Meet local enthusiasts, share collections, and build your board game community.</p>
                    </div>
                    <div className="feature-card">
                        <img src="https://img.icons8.com/fluency/96/trust.png" alt="trust" />
                        <h3>Trustworthy</h3>
                        <p>Verified users and reliable event management. We've got you covered.</p>
                    </div>
                </div>
            </section>

            {/* --- CTA --- */}
            <section className="call-to-action fade-in-on-scroll">
                <h2>Join the Game Night community</h2>
                <p>Whether you're a casual player or a board game collector, your new adventure starts here.</p>
                <button className="btn success" onClick={() => window.location.href = '/signup'}>
                    Get Started
                </button>
            </section>

        </div >
    );
}

export default Home;
