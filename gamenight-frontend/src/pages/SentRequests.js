import React, { useState, useEffect } from "react";
import axios from "axios";
import "../App.css";
import RequestCard from "../components/RequestCard";
import ActiveRequestCard from "../components/ActiveRequestCard";
import SecondaryNavBar from "../components/SecondaryNavBar";

function SentRequestsPage() {
    const [sentRequests, setSentRequests] = useState([]);
    const [activeRentals, setActiveRentals] = useState([]);
    const [selectedRequest, setSelectedRequest] = useState(null);

    useEffect(() => {
        const mockData = [
            { id: 1, name: "Block Wars", status: "Accepted", itemName: "Chess Set", renterName: "John Doe" },
            { id: 2, name: "Tower Defense 2", status: "Rejected", itemName: "Game Board", renterName: "Jane Smith" },
            { id: 3, name: "Squid Games", status: "Delivered", itemName: "VR Headset", renterName: "Tom Baker" }
        ];
        setSentRequests([mockData[1], mockData[2]]);
        setActiveRentals([mockData[0]]);
    }, []);

    const handleViewDetails = (request) => {
        setSelectedRequest(request);
    };

    return (
        <div>
            <SecondaryNavBar />
            <div className="container">
                <div className="left-column">
                    <h1 className="left-align">Sent Borrowing Requests</h1>
                    <div className="card-list">
                        {sentRequests.length > 0 ? (
                            sentRequests.map((request, index) => (
                                <RequestCard
                                    key={index}
                                    title={request.name}
                                    status={request.status}
                                    onViewDetails={() => handleViewDetails(request)}
                                />
                            ))
                        ) : (
                            <p>No sent requests found.</p>
                        )}
                    </div>
                </div>

                <div className="divider"></div>

                <div className="right-column">
                    {selectedRequest ? (
                        <div className="details-box">
                            <h2>{selectedRequest.name}</h2>
                            <p>Status: {selectedRequest.status}</p>
                            <p>Item: {selectedRequest.itemName}</p>
                            <p>Renter: {selectedRequest.renterName}</p>
                        </div>
                    ) : (
                        <div className="details-box">
                            <p>Select a request to view details.</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default SentRequestsPage;
