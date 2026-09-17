import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class Board {

    public enum Direction{
        HORIZONTAL,
        VERTICAL
    }

    private Square[][] grid;
    private ArrayList<Ship> ship;
    private int rows;
    private int columns;


    public Board (int rows, int columns){
        this.rows = rows;
        this.columns = columns;

        ship = new ArrayList<>();
        createGrid(rows,columns);
    }

    public void createGrid(int rows, int columns){
        grid = new Square[rows][columns];

        for(int row = 0; row < rows; row++){
            for(int column = 0; column < columns; column++){
                grid[row][column] = new Square(row, column);
            }
        }
    }

    public void createFleet(){
        // add the ships to the arrayList
        ship.add(new BattleShip());
        ship.add(new Cruiser());
        ship.add(new Cruiser());
        ship.add(new Destroyer());
        ship.add(new Destroyer());
        ship.add(new Destroyer());

    }

    private boolean checkPlaceShip(Ship ship, int row, int column, Direction direction) {

        int length = ship.getLength();

        // check if the boats go out of bounds
        switch(direction){
            case VERTICAL:
                if (row + length > rows){
                    return false;
                }
                break;
            case HORIZONTAL:
                if (column + length > columns){
                    return false;
                }
                break;
        }

        // checks every square that the ship will occupy
        for (int segment = 0; segment < length; segment++) {

            int checkRow = row;
            int checkColumn = column;

            if (direction == Direction.VERTICAL) {
                checkRow = row + segment;
            }

            if (direction == Direction.HORIZONTAL) {
                checkColumn = column + segment;
            }


            // checks the squares chosen and the surrounding square
            // to prevent ships from overlapping or touching
            for (int neighbourRow = checkRow - 1; neighbourRow <= checkRow + 1; neighbourRow++) {
                for (int neighbourColumn = checkColumn - 1; neighbourColumn <= checkColumn + 1; neighbourColumn++) {

                    // // ignores surrounding positions that are outside the board
                    if (neighbourRow >= 0 && neighbourRow < rows && neighbourColumn >= 0 && neighbourColumn < columns) {

                        // if a ship is found then the placement is invalid
                        if (grid[neighbourRow][neighbourColumn].getShip() != null) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }


    private void placeShip(Ship ship, int row,int column, Direction direction){
        int length = ship.getLength();

        for (int segment = 0; segment < length; segment++) {

            int placeRow = row;
            int placeColumn = column;

            if (direction == Direction.VERTICAL) {
                placeRow = row + segment;
            } else {
                placeColumn = column + segment;
            }

            Square square = grid[placeRow][placeColumn];

            square.setShip(ship);

            square.setState(Square.SquareState.SHIP);

            ship.addPosition(square);
        }
    }


    public void placeShipRandomly(Ship ship) {

        Random random = new Random();

        boolean placed = false;

        while (!placed) {

            int row = random.nextInt(rows);
            int column = random.nextInt(columns);

            Direction[] directions = Direction.values();
            Direction direction = directions[random.nextInt(directions.length)];

            if (checkPlaceShip(ship, row, column, direction)) {

                placeShip(ship, row, column, direction);

                placed = true;
            }
        }
    }

    private void placeFleetRandomly(){
        for (Ship currentShip : ship){
            placeShipRandomly(currentShip);
        }
    }

    public void placeComputerShip(){
        createFleet();
        placeFleetRandomly();
    }

    public boolean placeHumanShip(Ship ship, int row, int column, Direction direction){
        if (checkPlaceShip(ship,row,column,direction)){
            placeShip(ship,row,column,direction);
            return true;
        }
        return false;
    }

    public boolean attackSquare(int row, int column){
        Square square = grid[rows][column];

        //prevent the smae squares from being attacked twice
        if(square.getState() == Square.SquareState.HIT || square.getState() == Square.SquareState.MISS){
            return false;
        }

        if (square.getShip() != null) {

            square.setState(Square.SquareState.HIT);

            square.getShip().takeHit();

            return true;
        }

        square.setState(Square.SquareState.MISS);

        return false;

    }

    public boolean allShipsSunk() {

        for (Ship currentShip : ship) {

            if (!currentShip.isSunk()) {
                return false;
            }
        }

        return true;
    }


    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public Square getSquare(int row, int column) {
        return grid[row][column];
    }

    public Ship getShip(int shipIndex){
        return ship.get(shipIndex);
    }
}
