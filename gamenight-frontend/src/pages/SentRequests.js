import React, { useState, useEffect, useContext } from "react";
import axios from "axios";
import "../App.css";
import RequestCard from "../components/RequestCard";
import SecondaryNavBar from "../components/SecondaryNavBar";
import { AuthContext } from "../AuthContext";

function SentRequestsPage() {
    const [sentRequests, setSentRequests] = useState([]);
    const [selectedRequest, setSelectedRequest] = useState(null);

    // Get user ID from context
    const { user } = useContext(AuthContext);
    const userId = user?.userId;

    useEffect(() => {
        if (!userId) {
            console.warn("User ID is not available.");
            return;
        }

        const fetchSentRequests = async () => {
            try {
                console.log("Fetching requests for user ID:", userId);
                const response = await axios.get(`http://localhost:8080/borrowingRequests/${userId}/requests`, {
                    headers: {
                        "User-Id": userId,
                    },
                });
                setSentRequests(response.data);
                console.log("Fetched sent requests:", response.data);
            } catch (error) {
                console.error("Error fetching sent requests:", error);
            }
        };

        fetchSentRequests();
    }, [userId]);

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
                                    title={request.gameName}
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
                            <h2>{selectedRequest.gameName}</h2>
                            <p>Status: {selectedRequest.status}</p>
                            <p>Start Date: {selectedRequest.startTime}</p>
                            <p>End Date: {selectedRequest.endTime}</p>
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
