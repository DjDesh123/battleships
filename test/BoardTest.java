import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(10, 10);
    }

    @Test
    void boardCreatesWithCorrectSize() {

        assertEquals(10, board.getRows());
        assertEquals(10, board.getColumns());
    }

    @Test
    void validShipCanBePlaced(){
        Ship ship = new BattleShip();

        boolean placed = board.placeHumanShip(ship, 0,0,Board.Direction.HORIZONTAL);

        assertTrue(placed);
    }

    @Test
    void invalidShipPlacement(){
        Ship ship = new BattleShip();

        boolean valid = board.placeHumanShip(ship,10,10, Board.Direction.HORIZONTAL);

        assertFalse(valid);
    }

    @Test
    void noOverlappingShips(){
        Ship ship1 = new BattleShip();
        Ship ship2 = new Destroyer();


        boolean firstShip = board.placeHumanShip(ship1,2,2, Board.Direction.HORIZONTAL);

        boolean secondShip = board.placeHumanShip(ship2,2,2, Board.Direction.HORIZONTAL);

        assertTrue(firstShip);
        assertFalse(secondShip);


    }

    @Test
    void noTouchingShips(){
        Ship ship1 = new Destroyer();
        Ship ship2 = new Destroyer();


        boolean firstShip = board.placeHumanShip(ship1,2,2, Board.Direction.HORIZONTAL);

        boolean secondShip = board.placeHumanShip(ship2,3,2, Board.Direction.HORIZONTAL);

        assertTrue(firstShip);
        assertFalse(secondShip);

    }

    @Test
    void placingSameShipTwice(){
        Ship ship = new Destroyer();


        boolean valid = board.placeHumanShip(ship,2,2, Board.Direction.HORIZONTAL);
        boolean valid2 = board.placeHumanShip(ship,4,5, Board.Direction.HORIZONTAL);

        assertTrue(valid);
        assertFalse(valid2);
    }

    @Test
    void attackMiss(){
        Ship ship = new Destroyer();

        boolean place = board.placeHumanShip(ship,2,2,Board.Direction.HORIZONTAL);

        boolean hit = board.attackSquare(3,2);

        assertFalse(hit);
        assertEquals(Square.SquareState.MISS, board.getSquare(3, 2).getState());

    }

    @Test
    void attackHit(){
        Ship ship = new Destroyer();

        boolean place = board.placeHumanShip(ship,2,2,Board.Direction.HORIZONTAL);

        boolean hit = board.attackSquare(2,2);

        assertTrue(hit);
        assertEquals(Square.SquareState.HIT, board.getSquare(2, 2).getState());

    }

    @Test
    void noAttackingSameSquare(){
        Ship ship = new Destroyer();

        boolean place = board.placeHumanShip(ship,2,2,Board.Direction.HORIZONTAL);

        boolean hit = board.attackSquare(2,2);
        boolean hit2 = board.attackSquare(2,2);

        assertFalse(hit2);

    }

    @Test
    void removingShipClearsSquares() {

        Ship ship = new Destroyer();

        board.placeHumanShip(ship, 2, 2, Board.Direction.HORIZONTAL);

        board.removeShip(ship);

        assertEquals(Square.SquareState.WATER, board.getSquare(2, 2).getState());

        assertEquals(Square.SquareState.WATER, board.getSquare(2, 3).getState());

        assertNull(board.getSquare(2, 2).getShip());
        assertNull(board.getSquare(2, 3).getShip());

        assertTrue(ship.getPosition().isEmpty());
    }

    @Test
    void allShipsPlacedWorks() {

        board.createFleet();

        assertFalse(board.allShipsPlaced());

        board.placeHumanShip(board.getShips().get(0), 0, 0, Board.Direction.HORIZONTAL);
        board.placeHumanShip(board.getShips().get(1), 2, 0, Board.Direction.HORIZONTAL);
        board.placeHumanShip(board.getShips().get(2), 4, 0, Board.Direction.HORIZONTAL);
        board.placeHumanShip(board.getShips().get(3), 6, 0, Board.Direction.HORIZONTAL);
        board.placeHumanShip(board.getShips().get(4), 8, 0, Board.Direction.HORIZONTAL);
        board.placeHumanShip(board.getShips().get(5), 8, 3, Board.Direction.HORIZONTAL);

        assertTrue(board.allShipsPlaced());
    }

    @Test
    void allShipsSunkWorks() {

        board.createFleet();

        assertFalse(board.allShipsSunk());

        for (Ship ship : board.getShips()) {
            for (int hit = 0; hit < ship.getLength(); hit++) {
                ship.takeHit();
            }
        }

        assertTrue(board.allShipsSunk());
    }

}