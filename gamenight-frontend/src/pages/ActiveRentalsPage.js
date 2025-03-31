import React, { useState, useEffect } from "react";
import axios from "axios";
import "../App.css";
import ActiveRequestCard from "../components/ActiveRequestCard";
import SecondaryNavBar from "../components/SecondaryNavBar";

function ActiveRentalsPage() {
    const [activeRentals, setActiveRentals] = useState([]);
    const [selectedRental, setSelectedRental] = useState(null);

    useEffect(() => {
        const mockData = [
            { id: 1, name: "Chess Set", status: "Active", renterName: "John Doe", startDate: "2025-03-20", endDate: "2025-04-05" },
            { id: 2, name: "Game Board", status: "Active", renterName: "Jane Smith", startDate: "2025-03-22", endDate: "2025-04-12" }
        ];
        setActiveRentals(mockData);
    }, []);

    const handleViewDetails = (rental) => {
        setSelectedRental(rental);
    };

    return (
        <div>
            <SecondaryNavBar />
            <div className="container">
                <div className="left-column">
                    <h1 className="left-align">Active Rentals</h1>
                    <div className="card-list">
                        {activeRentals.length > 0 ? (
                            activeRentals.map((rental, index) => (
                                <ActiveRequestCard
                                    key={index}
                                    title={rental.name}
                                    status={rental.status}
                                    onViewDetails={() => handleViewDetails(rental)}
                                />
                            ))
                        ) : (
                            <p>No active rentals found.</p>
                        )}
                    </div>
                </div>

                <div className="divider"></div>

                <div className="right-column">
                    {selectedRental ? (
                        <div className="details-box">
                            <h2>{selectedRental.name}</h2>
                            <p>Status: {selectedRental.status}</p>
                            <p>Renter: {selectedRental.renterName}</p>
                            <p>Start Date: {selectedRental.startDate}</p>
                            <p>End Date: {selectedRental.endDate}</p>
                        </div>
                    ) : (
                        <div className="details-box">
                            <p>Select a rental to view details.</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default ActiveRentalsPage;
