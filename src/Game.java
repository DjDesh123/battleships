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

    private Player humanPlayer;
    private Player computerPlayer;

    private Difficulty difficulty;
    private GameState gameState;

    private int shotsRemaining;

    public Game(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.gameState = GameState.SETUP;
    }

    public void startGame() {

        switch (difficulty) {
            case EASY:
                shotsRemaining = 60;
                break;

            case NORMAL:
                shotsRemaining = 50;
                break;

            case HARD:
                shotsRemaining = 40;
                break;
        }

        humanPlayer = new Player("human");
        computerPlayer = new Player("computer");

        computerPlayer.getBoard().placeComputerShip();
        humanPlayer.getBoard().createFleet();

        gameState = GameState.SETUP;
    }

    public boolean humanAttack(int row, int column) {

        boolean hit = computerPlayer.getBoard().attackSquare(row, column);
        shotsRemaining--;

        if (computerPlayer.getBoard().allShipsSunk()) {
            gameState = GameState.GAMEOVER;
        }

        return hit;
    }

    public boolean comnputerAttack(){
        Random random = new Random();

        Board humanBoard = humanPlayer.getBoard();


        int row;
        int column;

        do {
            row = random.nextInt(humanBoard.getRows());
            column = random.nextInt(humanBoard.getColumns());

        }while(humanBoard.getSquare(row, column).getState() == Square.SquareState.HIT || humanBoard.getSquare(row, column).getState() == Square.SquareState.MISS);

        // attacks the human player's board
        boolean hit = humanBoard.attackSquare(row, column);

        // checks whether the computer has sunk every human ship
        if (humanBoard.allShipsSunk()) {
            gameState = GameState.GAMEOVER;
        }

        return hit;


    }
}