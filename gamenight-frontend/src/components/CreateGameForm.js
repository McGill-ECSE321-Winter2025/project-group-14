import React, { useState, useContext } from "react";
import { 
  Box,
  TextField,
  Modal,
  Backdrop,
  Fade
} from "@mui/material";
import { AuthContext } from "../AuthContext";
import Button from "./Button";

const CreateGameForm = ({ open, onClose, onSuccess }) => {
  const { user } = useContext(AuthContext);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const response = await fetch("http://localhost:8080/games", {
        method: "POST",
        headers: { 
          "Content-Type": "application/json",
          "User-Id": user.userId 
        },
        body: JSON.stringify({
          name,
          description
        })
      });
      
      if (!response.ok) {
        throw new Error("Failed to create game");
      }
      
      const newGame = await response.json();
      onSuccess(newGame);
      onClose();
    } catch (error) {
      console.error("Error creating game:", error);
      alert("Failed to create game. Please try again.");
    } finally {
      setLoading(false);
    }
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
    borderRadius: 2,
    textAlign: 'center' 
  };

  return (
    <Modal
      open={open}
      onClose={onClose}
      closeAfterTransition
      BackdropComponent={Backdrop}
      BackdropProps={{
        timeout: 500,
      }}
    >
      <Fade in={open}>
        <Box sx={modalStyle} component="form" onSubmit={handleSubmit}>
          <h2 className="form-title" style={{ textAlign: 'center' }}>Create New Game</h2>
          
          <TextField
            fullWidth
            label="Game Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            sx={{ mb: 2 }}
            required
          />
          
          <TextField
            fullWidth
            multiline
            rows={4}
            label="Description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            sx={{ mb: 2 }}
            required
          />
          
          <Box sx={{ 
            display: 'flex', 
            justifyContent: 'center', 
            gap: 2,
            width: '100%'
          }}>
            <Button 
              type="secondary" 
              onClick={onClose} 
              disabled={loading}
              style={{ width: '120px' }}
            >
              Cancel
            </Button>
            <Button 
              type="submit"
              disabled={loading}
              style={{ width: '120px' }}
            >
              {loading ? 'Creating...' : 'Create Game'}
            </Button>
          </Box>
        </Box>
      </Fade>
    </Modal>
  );
};

export default CreateGameForm;