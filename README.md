# Report: Two-Player Icon Matching Game Implementation

## 1. Introduction

This report details the implementation of a **Two-Player Icon Matching Game**. The game is built using Java, with Socket Programming and Multithreading for communication between the client and the server. The graphical user interface (GUI) is developed using **JavaFX**. The players interact with the game by selecting matching icons on a grid, earning points for each successful match. The game ensures synchronization between the server and client, handles potential exceptions, and supports advanced features like account management and game resumption.

## 2. System Architecture

### 2.1. Server-Side Architecture

The server is responsible for managing game states, handling client connections, and ensuring synchronization. The server performs the following tasks:
- **Player Connection Management**: Waits for incoming player connections and automatically matches players to start a game. If no opponent is available, the server informs the player to wait.
- **Game Management**: Tracks the status of each game session, notifying players of game results (win, loss, or tie).
- **Communication Protocol**: The server uses a custom protocol to ensure smooth communication between the clients, including player matching, score synchronization, and game updates.

#### Key Components:
1. **Socket Programming**: The server listens for incoming client connections and establishes communication channels using **sockets**. Each player is handled in a separate thread for non-blocking communication.
2. **Multithreading**: Each game session is managed in a separate thread, allowing multiple games to run concurrently.
3. **Game Board Generation**: The server generates a random game board with pairs of icons, which are then sent to both players at the start of the game.

### 2.2. Client-Side Architecture

The client is responsible for handling game interactions, displaying the game board, and facilitating communication with the server. The client performs the following tasks:
- **Connection**: The client connects to the server and waits for a match with another player. Once matched, players alternate turns to find and connect identical icons.
- **Gameplay**: Players select two icons, and the client checks if the connection is valid (i.e., if the icons can be connected with no more than two right-angled turns). The GUI reflects whether the connection is valid.
- **Synchronization with Server**: The client sends updates (e.g., score, game state) to the server after each turn to maintain synchronization.
- **Notifications**: The client receives notifications about game results (win, loss, tie) from the server.

### 2.3. GUI Implementation

The GUI is developed using **JavaFX** and includes the following components:
- **Game Board**: The board displays randomly distributed pairs of icons. Players can select two identical icons, and if the connection is valid, the icons and connecting lines disappear.
- **Score Display**: The player's current score is displayed on the screen.
- **Game Result**: The game result (win, loss, or tie) is displayed when the game ends.

#### Key Features of the GUI:
- **Grid Layout**: The grid is dynamically sized (e.g., 4x4, 6x8, 8x8) based on the board size selected by the players.
- **Icon Selection**: Players can click on the icons to attempt matching pairs. The GUI provides immediate feedback on whether the selection is valid.
- **Animations**: Successful connections are displayed with animations where the lines between icons are drawn before the icons disappear.

### 2.4. Exception Handling

The game is designed to handle several types of exceptions:
- **Server Crashes**: If the server crashes, players are notified and can attempt to reconnect.
- **Player Disconnections**: If a player disconnects (whether intentional or accidental), the server handles the disconnection gracefully and updates the game state accordingly.
- **Invalid Input**: The client ensures that invalid moves (e.g., selecting unmatched icons) are prevented, and appropriate error messages are displayed.

### 2.5. Advanced Features

Several bonus features were implemented to enhance the gameplay experience:
1. **Account Management & Game History**: Users can register, log in, and view their game history. The server maintains a record of past games, allowing players to track their progress.
2. **Opponent Selection**: Players can view a list of available opponents waiting to be matched. Players can select an opponent from this list to start a new game.
3. **Game Resumption**: If a player disconnects or quits the game, they can reconnect to the server and resume the game from where they left off, provided the same opponent is available.

## 3. Implementation Details

### 3.1. Server Implementation

## Server Introduction

The server in the **Two-Player Icon Matching Game** plays a central role in managing player connections, game synchronization, and communication between clients. Upon receiving a connection request from a client, the server adds the player to a queue after they successfully log in. When another player logs in, the server matches the two players to start a game.

During the game, the server ensures synchronization by handling messages passed between the clients. It receives actions from one player, processes the game logic, and broadcasts the relevant information (such as game state updates, score changes, and game results) to both players involved in the game. This ensures that both players are always in sync with the current game status.

By managing the queue, matching players, and synchronizing game actions, the server acts as the backbone of the game, enabling smooth multiplayer interactions.

## Demo Video

Here is a demo of the **Two-Player Icon Matching Game**:

![Demo Video](./demo.mp4)
