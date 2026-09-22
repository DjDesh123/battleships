import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game(Game.Difficulty.EASY, Game.GameMode.COMPUTER);
    }

    @Test
    void gameStartsInSetupState() {

        game.startGame();

        assertEquals(Game.GameState.SETUP, game.getGameState());
    }

    @Test
    void humanAttackDecreasesShots() {

        game.startGame();
        game.startPlaying();

        int shotsBefore = game.getShotsRemaining();

        game.humanAttack(0, 0);

        int shotsAfter = game.getShotsRemaining();

        assertEquals(shotsBefore - 1, shotsAfter);
    }

    @Test
    void humanSinkingEnemyFleetGivesHumanWin() {

        game.startGame();
        game.startPlaying();

        Board computerBoard = game.getComputerBoard();

        for (int row = 0; row < game.getRows(); row++) {
            for (int column = 0; column < game.getColumns(); column++) {

                if (computerBoard.getSquare(row, column).getShip() != null) {
                    game.humanAttack(row, column);
                }
            }
        }

        assertEquals(Game.GameState.GAMEOVER, game.getGameState());

        assertEquals(Game.WinnerState.HUMAN, game.getWinner());
    }

    @Test
    void runningOutOfShotsGivesComputerWin() {

        game.startGame();
        game.startPlaying();

        Board computerBoard = game.getComputerBoard();

        for (int row = 0; row < game.getRows() && game.getShotsRemaining() > 0; row++) {
            for (int column = 0; column < game.getColumns() && game.getShotsRemaining() > 0; column++) {

                if (computerBoard.getSquare(row, column).getShip() == null) {
                    game.humanAttack(row, column);
                }

            }
        }

        assertEquals(0, game.getShotsRemaining());
        assertEquals(Game.GameState.GAMEOVER, game.getGameState());
        assertEquals(Game.WinnerState.COMPUTER, game.getWinner());
    }

    private void placeTestFleet(Board board) {

        board.placeHumanShip(board.getShips().get(0), 0, 0, Board.Direction.HORIZONTAL);
        board.placeHumanShip(board.getShips().get(1), 2, 0, Board.Direction.HORIZONTAL);
        board.placeHumanShip(board.getShips().get(2), 4, 0, Board.Direction.HORIZONTAL);
        board.placeHumanShip(board.getShips().get(3), 6, 0, Board.Direction.HORIZONTAL);
        board.placeHumanShip(board.getShips().get(4), 8, 0, Board.Direction.HORIZONTAL);
        board.placeHumanShip(board.getShips().get(5), 8, 3, Board.Direction.HORIZONTAL);
    }

    @Test
    void computerSinkingHumanFleetGivesComputerWin() {

        game.startGame();
        game.startPlaying();

        Board humanBoard = game.getHumanBoard();

        placeTestFleet(humanBoard);

        int maximumAttacks = game.getRows() * game.getColumns();

        for (int attack = 0; attack < maximumAttacks && game.getGameState() != Game.GameState.GAMEOVER; attack++) {

            game.computerAttack();
        }

        assertEquals(Game.GameState.GAMEOVER, game.getGameState());
        assertEquals(Game.WinnerState.COMPUTER, game.getWinner());
    }

    @Test
    void playerOneSinkingPlayerTwoFleetGivesPlayerOneWin() {

        Game multiplayerGame = new Game(Game.Difficulty.EASY, Game.GameMode.MULTIPLAYER);

        multiplayerGame.startGame();
        multiplayerGame.startPlaying();

        Board playerTwoBoard = multiplayerGame.getPlayerTwo().getBoard();

        placeTestFleet(playerTwoBoard);

        for (int row = 0; row < multiplayerGame.getRows(); row++) {
            for (int column = 0; column < multiplayerGame.getColumns(); column++) {

                if (playerTwoBoard.getSquare(row, column).getShip() != null) {
                    multiplayerGame.multiplayerAttack(true, row, column);
                }

            }
        }

        assertEquals(Game.WinnerState.PLAYER1, multiplayerGame.getWinner());
        assertEquals(Game.GameState.GAMEOVER, multiplayerGame.getGameState());
    }

    @Test
    void playerTwoSinkingPlayerOneFleetGivesPlayerTwoWin() {

        Game multiplayerGame = new Game(
                Game.Difficulty.EASY,
                Game.GameMode.MULTIPLAYER
        );

        multiplayerGame.startGame();
        multiplayerGame.startPlaying();

        Board playerOneBoard =
                multiplayerGame.getPlayerOne().getBoard();

        placeTestFleet(playerOneBoard);

        for (int row = 0; row < multiplayerGame.getRows(); row++) {
            for (int column = 0; column < multiplayerGame.getColumns(); column++) {

                if (playerOneBoard.getSquare(row, column).getShip() != null) {

                    multiplayerGame.multiplayerAttack(false, row,column);
                }
            }
        }

        assertEquals(Game.WinnerState.PLAYER2, multiplayerGame.getWinner());
        assertEquals(Game.GameState.GAMEOVER, multiplayerGame.getGameState());
    }

    @Test
    void multiplayerRunningOutOfShotsGivesDraw() {

        Game multiplayerGame = new Game(Game.Difficulty.EASY, Game.GameMode.MULTIPLAYER);

        multiplayerGame.startGame();
        multiplayerGame.startPlaying();

        Board playerOneBoard = multiplayerGame.getPlayerOne().getBoard();

        Board playerTwoBoard = multiplayerGame.getPlayerTwo().getBoard();

        int playerOneShots = 0;
        int playerTwoShots = 0;

        for (int row = 0; row < multiplayerGame.getRows(); row++) {
            for (int column = 0; column < multiplayerGame.getColumns(); column++) {

                if (playerOneShots < 60 && playerTwoBoard.getSquare(row, column) .getShip() == null) {

                    multiplayerGame.multiplayerAttack(true, row, column);
                    playerOneShots++;
                }

                if (playerTwoShots < 60 && playerOneBoard.getSquare(row, column).getShip() == null) {

                    multiplayerGame.multiplayerAttack(false, row, column);
                    playerTwoShots++;
                }
            }
        }

        assertEquals(Game.GameState.GAMEOVER, multiplayerGame.getGameState());
        assertEquals(Game.WinnerState.DRAW, multiplayerGame.getWinner());
    }

    @Test
    void finalShotSinkingFleetStillGivesHumanWin() {

        game.startGame();
        game.startPlaying();

        Board computerBoard = game.getComputerBoard();

        ArrayList<int[]> shipSquares = new ArrayList<>();
        ArrayList<int[]> waterSquares = new ArrayList<>();

        // Find every ship and water coordinate.
        for (int row = 0; row < game.getRows(); row++) {
            for (int column = 0; column < game.getColumns(); column++) {

                if (computerBoard.getSquare(row, column).getShip() != null) {
                    shipSquares.add(new int[]{row, column});
                } else{
                    waterSquares.add(new int[]{row, column});
                }

            }
        }

        // Work out how many misses are needed so the final
        // ship square is attacked with the final shot.
        int missesNeeded = game.getShotsRemaining() - shipSquares.size();

        for (int i = 0; i < missesNeeded; i++) {
            int[] square = waterSquares.get(i);
            game.humanAttack(square[0], square[1]);
        }

        // Sink everything except one final ship square.
        for (int i = 0; i < shipSquares.size() - 1; i++) {
            int[] square = shipSquares.get(i);
            game.humanAttack(square[0], square[1]);
        }

        assertEquals(1, game.getShotsRemaining());

        // Final shot also sinks the final ship.
        int[] finalSquare = shipSquares.get(shipSquares.size() - 1);

        game.humanAttack(finalSquare[0], finalSquare[1]);

        assertEquals(0, game.getShotsRemaining());
        assertEquals(Game.GameState.GAMEOVER, game.getGameState());
        assertEquals(Game.WinnerState.HUMAN, game.getWinner());
    }

}
