import React from "react";
import { 
  Card, 
  CardContent, 
  Box, 
  Typography,
  IconButton
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";

const AddGameCopyCard = ({ onClick }) => {
  return (
    <Card 
      onClick={onClick}
      sx={{
        width: 280,
        height: '100%', // Ensure full height to match other cards
        borderRadius: '14px',
        overflow: 'hidden',
        boxShadow: '0 3px 8px rgba(0,0,0,0.1)',
        transition: 'transform 0.2s ease',
        cursor: 'pointer',
        display: 'flex',
        flexDirection: 'column',
        '&:hover': {
          transform: 'translateY(-3px)',
          '& .add-icon-container': {
            backgroundColor: '#e0e0e0',
          },
          '& .add-icon': {
            transform: 'scale(1.1)'
          }
        }
      }}
    >
      <Box 
        className="add-icon-container"
        sx={{
          position: 'relative',
          height: 280, // Fixed height to match image area of GameCopyCard
          backgroundColor: '#f5f5f5',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          transition: 'background-color 0.2s ease'
        }}
      >
        <Box 
          className="add-icon"
          sx={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            width: 80,
            height: 80,
            borderRadius: '50%',
            backgroundColor: 'rgba(0, 0, 0, 0.04)',
            transition: 'all 0.2s ease'
          }}
        >
          <AddIcon 
            sx={{ 
              fontSize: 48,
              color: '#757575',
            }} 
          />
        </Box>
      </Box>

      <CardContent sx={{ 
        p: 2.5,
        flexGrow: 1,
        display: 'flex',
        flexDirection: 'column'
      }}>
        <Typography variant="subtitle1" sx={{ 
          mb: 1.5,
          fontWeight: 600,
          textAlign: 'center',
          fontSize: '1rem',
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis'
        }}>
          Add New Game Copy
        </Typography>

        <Typography variant="body2" sx={{ 
          mb: 1.5,
          color: 'text.secondary',
          textAlign: 'center',
          fontSize: '0.85rem',
          flexGrow: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          Click to add a new game to your collection
        </Typography>

        <Box sx={{ 
          display: 'flex',
          justifyContent: 'center',
          gap: 1.5,
          mt: 1.5,
          visibility: 'hidden',
          minHeight: 42 // Maintain consistent height with action buttons
        }}>
          <IconButton sx={{ visibility: 'hidden' }}>
            <EditIcon />
          </IconButton>
          <IconButton sx={{ visibility: 'hidden' }}>
            <DeleteIcon />
          </IconButton>
        </Box>
      </CardContent>
    </Card>
  );
};

export default AddGameCopyCard;