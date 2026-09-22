import java.util.ArrayList;
import java.util.Random;

public class Game {

    public enum Difficulty {
        EASY,
        NORMAL,
        HARD
    }

    public enum GameState {
        SETUP,
        PLAYING,
        GAMEOVER
    }

    public enum GameMode {
        COMPUTER,
        MULTIPLAYER
    }

    public enum WinnerState {
        HUMAN,
        COMPUTER,
        PLAYER1,
        PLAYER2,
        DRAW
    }


    // single player players
    private Player humanPlayer;
    private Player computerPlayer;

    // multiplayer players
    private Player humanPlayer1;
    private Player humanPlayer2;

    // for turns based scenario
    private Player currentPlayer;


    private Difficulty difficulty;
    private GameState gameState;
    private GameMode gameMode;
    private WinnerState winnerState;

    private  Ship lastSunkShip;


    private int rows;
    private int columns;
    private int shotsRemaining;

    private int lastComputerRow;
    private int lastComputerColumn;

    private int playerOneShotsRemaining;
    private int playerTwoShotsRemaining;

    public Game(Difficulty difficulty, GameMode gameMode) {

        this.difficulty = difficulty;
        this.gameMode = gameMode;
        this.gameState = GameState.SETUP;
    }

    public void startGame() {

        setupDifficulty();
        setupPlayers();

        gameState = GameState.SETUP;
    }

    // from the user choices of difficulty you will have different size of boards and amount of shots remaining
    private void setupDifficulty() {

        switch (difficulty) {

            case EASY:
                rows = 10;
                columns = 10;

                shotsRemaining = 60;
                playerOneShotsRemaining = 60;
                playerTwoShotsRemaining = 60;
                break;

            case NORMAL:
                rows = 12;
                columns = 12;

                shotsRemaining = 50;
                playerOneShotsRemaining = 60;
                playerTwoShotsRemaining = 60;
                break;

            case HARD:
                rows = 15;
                columns = 15;

                shotsRemaining = 40;
                playerOneShotsRemaining = 60;
                playerTwoShotsRemaining = 60;
                break;
        }
    }


    // creates the player and boards dpednming on the mdoe the user picks
    private void setupPlayers() {

        switch (gameMode) {

            case COMPUTER:

                humanPlayer = new Player("human", rows, columns);
                computerPlayer = new Player("computer", rows, columns);

                humanPlayer.getBoard().createFleet();
                computerPlayer.getBoard().placeComputerShip();

                currentPlayer = humanPlayer;
                break;


            case MULTIPLAYER:

                humanPlayer1 = new Player("human1", rows, columns);
                humanPlayer2 = new Player("human2", rows, columns);

                humanPlayer1.getBoard().createFleet();
                humanPlayer2.getBoard().createFleet();

                // Player 1 places ships first
                currentPlayer = humanPlayer1;
                break;
        }
    }

    /**
     *
     * @param row the row that the user is attacking
     * @param column the column that the user is attacking
     * @return true if a ship was a hit false otherwise
     */
    public boolean humanAttack(int row, int column) {

        // resets so an old sunk ship wont effect this run
        lastSunkShip = null;

        boolean hit = computerPlayer.getBoard().attackSquare(row, column);

        if (hit) {

            Ship sunkShip = checkSunkShip(computerPlayer.getBoard(), row, column);

            if (sunkShip != null) {
                lastSunkShip = sunkShip;
            }
        }

        // a valid attack will use up one of the player's available shots
        shotsRemaining--;

        // runs a wincheck to see if the user wins or loses when the requriements are met
        checkGameOver();

        return hit;
    }

    private void checkGameOver() {

        // Human destroyed the computer fleet.
        if (computerPlayer.getBoard().allShipsSunk()) {
            gameState = GameState.GAMEOVER;
            winnerState = WinnerState.HUMAN;
            return;
        }

        // Computer destroyed the human fleet.
        if (humanPlayer.getBoard().allShipsSunk()) {
            gameState = GameState.GAMEOVER;
            winnerState = WinnerState.COMPUTER;
            return;
        }

        // Human ran out of available shots.
        if (shotsRemaining <= 0) {
            gameState = GameState.GAMEOVER;
            winnerState = WinnerState.COMPUTER;
        }
    }

    // allows the computer to attack a random square form the human player board whilst skipping previously attacked squares
    // updates eh game state if the human fleet has been destroyed
    public boolean computerAttack() {

        lastSunkShip = null;

        Random random = new Random();

        Board humanBoard = humanPlayer.getBoard();

        int row;
        int column;

        // keep generating coordinates until an unattacked square is found.
        do {
            row = random.nextInt(humanBoard.getRows());
            column = random.nextInt(humanBoard.getColumns());

        } while (humanBoard.getSquare(row, column).getState() == Square.SquareState.HIT || humanBoard.getSquare(row, column).getState() == Square.SquareState.MISS);

        // stores the position so the ui can display the computer attack later
        lastComputerRow = row;
        lastComputerColumn = column;

        boolean hit = humanBoard.attackSquare(row, column);


        // if the attack is valid check if it has sunk the ship
        if (hit) {
            Ship sunkShip = checkSunkShip(humanBoard, row, column);

            if (sunkShip != null) {
                lastSunkShip = sunkShip;
            }
        }

        checkGameOver();

        return hit;
    }

    /** checks whether the ship occupying the attacked square has been sunk
     *
     * @param board the board that containting the attacked squares
     * @param row the row of the attacked square
     * @param column the column of the attacked square
     * @return the sunkl shif if one exist otherwise null
     */
    private Ship checkSunkShip(Board board, int row, int column) {

        Square square = board.getSquare(row, column);

        Ship ship = square.getShip();

        if (ship != null && ship.isSunk()) {

            return ship;
        }

        return null;
    }

    /** attacks the opposing players board and reduces the attacking players remaining shots
     *
     * @param attackingPlayer true if its player one false if not
     * @param row the row that is being attacked
     * @param column the column that is being attacked
     * @return true if the ship was a hit otherwise false
     */
    public boolean multiplayerAttack(boolean attackingPlayer, int row, int column) {
        Board targetBoard = getMultiplayerTargetBoard(attackingPlayer);

        boolean hit = targetBoard.attackSquare(row, column);

        reduceMultiplayerShots(attackingPlayer);

        checkGameOver(attackingPlayer, targetBoard);

        return hit;
    }


    private void checkGameOver(boolean attackingPlayer, Board targetBoard) {

        // check fleet destruction first
        if (targetBoard.allShipsSunk()) {

            gameState = GameState.GAMEOVER;

            if (attackingPlayer) {
                winnerState = WinnerState.PLAYER1;
            } else {
                winnerState = WinnerState.PLAYER2;
            }

            return;
        }

        //  end through shot limits once both players take their final shot
        if (playerOneShotsRemaining <= 0 && playerTwoShotsRemaining <= 0) {
            gameState = GameState.GAMEOVER;
            winnerState = WinnerState.DRAW;
        }
    }

    private void reduceMultiplayerShots(boolean attackingPlayer) {

        if (attackingPlayer) {
            playerOneShotsRemaining--;
        } else {
            playerTwoShotsRemaining--;
        }
    }

    public boolean switchPlacementPlayer() {

        if (gameMode == GameMode.MULTIPLAYER && currentPlayer == humanPlayer1) {

            currentPlayer = humanPlayer2;
            return true;
        }

        return false;
    }

    public boolean placeHumanShip(Ship ship, int row, int column, Board.Direction direction) {

        return currentPlayer.placeShip(ship, row, column, direction);
    }

    public void removeHumanShip(Ship ship) {

        currentPlayer.getBoard().removeShip(ship);
    }

    public boolean allHumanShipsPlaced() {

        return currentPlayer.getBoard().allShipsPlaced();
    }

    public void startPlaying() {

        gameState = GameState.PLAYING;
    }

    /**
     *
     * getters
     *
     */
    public ArrayList<Ship> getHumanShips() {

        return currentPlayer.getBoard().getShips();
    }

    private Board getMultiplayerTargetBoard(boolean playerOneAttacking) {

        if (playerOneAttacking) {
            return humanPlayer2.getBoard();
        }

        return humanPlayer1.getBoard();
    }

    public Board getHumanBoard() {
        return currentPlayer.getBoard();
    }

    public Board getComputerBoard() {

        return computerPlayer.getBoard();
    }

    public int getRows() {

        return rows;
    }

    public Ship getLastSunkShip() {
        return lastSunkShip;
    }

    public int getColumns() {

        return columns;
    }

    public int getLastComputerRow() {

        return lastComputerRow;
    }

    public int getLastComputerColumn() {

        return lastComputerColumn;
    }

    public GameState getGameState() {

        return gameState;
    }

    public GameMode getGameMode() {

        return gameMode;
    }

    public WinnerState getWinner() {

        return winnerState;
    }

    public Player getPlayerOne() {

        return humanPlayer1;
    }

    public Player getPlayerTwo() {

        return humanPlayer2;
    }

    public int getShotsRemaining() {
        return shotsRemaining;
    }


}