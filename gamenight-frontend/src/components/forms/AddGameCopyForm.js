import React, { useState, useContext, useEffect } from "react";
import {
  Box,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
} from "@mui/material";
import { AuthContext } from "../../AuthContext";
import CreateGameForm from "./CreateGameForm";
import Button from '../ui/Button';


const AddGameCopyForm = ({ onCancel, onSuccess }) => {
  const { user } = useContext(AuthContext);
  const [description, setDescription] = useState("");
  const [gameId, setGameId] = useState("");
  const [games, setGames] = useState([]);
  const [loadingGames, setLoadingGames] = useState(true);
  const [showCreateGame, setShowCreateGame] = useState(false);

  useEffect(() => {
    const fetchGames = async () => {
      try {
        const response = await fetch("http://localhost:8080/games", {
          headers: { "User-Id": user.userId },
        });
        const data = await response.json();
        setGames(data);
        setLoadingGames(false);
      } catch (error) {
        console.error("Error fetching games:", error);
        setLoadingGames(false);
      }
    };
    fetchGames();
  }, [user.userId]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const response = await fetch("http://localhost:8080/game-copies/", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "User-Id": user.userId
        },
        body: JSON.stringify({
          description,
          gameId: parseInt(gameId),
          ownerId: user.userId
        })
      });

      if (!response.ok) {
        throw new Error("Failed to add game copy");
      }

      const newGameCopy = await response.json();
      onSuccess(newGameCopy);
    } catch (error) {
      console.error("Error adding game copy:", error);
      alert("Failed to add game copy. Please try again.");
    }
  };

  const handleGameCreated = (newGame) => {
    setGames(prev => [...prev, newGame]);
    setGameId(newGame.id.toString());
    setShowCreateGame(false);
  };

  return (
    <>
      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{
          p: 3,
          border: '1px solid #ddd',
          borderRadius: 1,
          mb: 3,
          textAlign: 'center',
          maxWidth: '500px',
          margin: '0 auto'
        }}
      >
        <h2 className="form-title" style={{ textAlign: 'center', marginBottom: '24px' }}>
          Add New Game Copy
        </h2>

        <FormControl fullWidth sx={{ mb: 2 }}>
          <InputLabel id="game-select-label">Game</InputLabel>
          <Select
            labelId="game-select-label"
            value={gameId}
            label="Game"
            onChange={(e) => setGameId(e.target.value)}
            required
            disabled={loadingGames}
          >
            {loadingGames ? (
              <MenuItem value="">Loading games...</MenuItem>
            ) : (
              [
                ...games.map((game) => (
                  <MenuItem key={game.id} value={game.id}>
                    {game.name}
                  </MenuItem>
                )),
                <MenuItem
                  key="create-new"
                  value=""
                  onClick={() => setShowCreateGame(true)}
                  sx={{
                    fontWeight: 'bold',
                    backgroundColor: '#f5f5f5 !important',
                    borderTop: '1px solid #e0e0e0',
                    marginTop: 1,
                    '&.MuiMenuItem-root': {
                        backgroundColor: '#f5f5f5', 
                      },
                      '&.MuiMenuItem-root:hover': {
                        backgroundColor: '#e0e0e0 !important',
                        color: '#1b5e20'
                      },
                      '&.Mui-selected': {
                        backgroundColor: '#f5f5f5' 
                      },
                      '&.Mui-focusVisible': {
                        backgroundColor: '#f5f5f5' 
                      }
                    }}
                >
                  Create a new game
                </MenuItem>
              ]
            )}
          </Select>
        </FormControl>

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
            onClick={onCancel}
            style={{ width: '120px' }}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            style={{ width: '120px' }}
          >
            Add Game Copy
          </Button>
        </Box>
      </Box>

      <CreateGameForm
        open={showCreateGame}
        onClose={() => setShowCreateGame(false)}
        onSuccess={handleGameCreated}
      />
    </>
  );
};

export default AddGameCopyForm;