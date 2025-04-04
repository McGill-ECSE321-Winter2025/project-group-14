import React from "react";
import Modal from "react-modal";
import "../App.css";

Modal.setAppElement("#root");

function RequestDetailsModal({ isOpen, onClose, details }) {
    return (
        <Modal
            isOpen={isOpen}
            onRequestClose={onClose}
            className="modal"
            overlayClassName="overlay"
        >
            <h2>{details.title}</h2>
            <p>Status: {details.status}</p>
            <p>Item: {details.itemName}</p>
            <p>Renter: {details.renterName}</p>
            <button className="modal-close" onClick={onClose}>Close</button>
        </Modal>
    );
}

export default RequestDetailsModal;
