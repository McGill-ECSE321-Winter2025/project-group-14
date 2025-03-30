import React, { useState, useContext } from "react";
import { 
  Box, 
  Button, 
  TextField, 
  Typography, 
  Select, 
  MenuItem, 
  FormControl, 
  InputLabel 
} from "@mui/material";
import { AuthContext } from "../AuthContext";

const AddGameCopyForm = ({ onCancel, onSuccess }) => {
  const { user } = useContext(AuthContext);
  const [description, setDescription] = useState("");
  const [gameId, setGameId] = useState("");
  const [games, setGames] = useState([]);
  const [loadingGames, setLoadingGames] = useState(true);

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
      const response = await fetch("http://localhost:8080/game-copies", {
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

  return (
    <Box 
      component="form" 
      onSubmit={handleSubmit}
      sx={{ 
        p: 3, 
        border: '1px solid #ddd', 
        borderRadius: 1,
        mb: 3 
      }}
    >
      <Typography variant="h6" gutterBottom>
        Add New Game Copy
      </Typography>
      
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
            games.map((game) => (
              <MenuItem key={game.id} value={game.id}>
                {game.name}
              </MenuItem>
            ))
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
      
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
        <Button variant="outlined" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" variant="contained" color="primary">
          Add Game Copy
        </Button>
      </Box>
    </Box>
  );
};

export default AddGameCopyForm;