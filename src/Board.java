import java.util.ArrayList;
import java.util.Random;

public class Board {

    public enum Direction{
        HORIZONTAL,
        VERTICAL
    }

    private int rows;
    private int columns;
    private Square[][] grid;
    private ArrayList<Ship> ships;


    public Board (int rows, int columns){
        this.rows = rows;
        this.columns = columns;

        ships = new ArrayList<>();
        createGrid(rows,columns);
    }

    /**
     *
     * @param rows number of rows in the board
     * @param columns number of columns in the board
     *
     */
    public void createGrid(int rows, int columns){

        // creates a 2d array that can hold square references
        grid = new Square[rows][columns];

        // while looping through the 2d array creates a Square object for that cell and stores the reference in the array
        for(int row = 0; row < rows; row++){
            for(int column = 0; column < columns; column++){
                grid[row][column] = new Square(row, column);
            }
        }
    }

    /**
     * creates the fleet with the subclasses
     */
    public void createFleet(){
        // add the ships to the arrayList
        ships.add(new BattleShip());
        ships.add(new Cruiser());
        ships.add(new Cruiser());
        ships.add(new Destroyer());
        ships.add(new Destroyer());
        ships.add(new Destroyer());

    }

    /**
     * checks whether a ship can be placed at the specified position
     * without going out of bounds or overlapping with another ship or touching another ship
     *
     * @param ship the ship being checked
     * @param row the starting row for the ship
     * @param column the starting column for the ship
     * @param direction the direction which the ship can be placed
     * @return true if its valid false is not
     */
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


    /** this is to actually allow the user to place their ship
     *
     * @param ship the ship being placed
     * @param row the row for the ship being placed
     * @param column the column for the ship being placed
     * @param direction the direction the ship is being placed
     */
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

    /** this is for the computer to randomly place a ship on their board
     *
     * @param ship the ship thats being placed randomly
     */
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

    // this method is to cycle through the ArrayList<Ship> and populate the entire board for the computer side
    private void placeFleetRandomly(){
        for (Ship currentShip : ships){
            placeShipRandomly(currentShip);
        }
    }

    // added this method to have an application layer  and make it look neater in the codebase to access
    public void placeComputerShip(){
        createFleet();
        placeFleetRandomly();
    }

    /**
     * places the human ships if its valid to do so
     *
     * @param ship the ship thats being placed
     * @param row the row that the ship is being placed on
     * @param column the column that the ship is being placed on
     * @param direction the direction the ship is being placed on
     * @return true if the ship being properly placed false if not
     */
    public boolean placeHumanShip(Ship ship, int row, int column, Direction direction){

        // prevents the same ship being placed twice
        if(!ship.getPosition().isEmpty()){
            return false;
        }

        //checks if the ship can be placed or not
        if(checkPlaceShip(ship,row,column,direction)){
            placeShip(ship,row,column,direction);
            return true;
        }

        return false;
    }

    /**
     * allows the user to attack a certain square and see if it was a hit or miss
     *
     * @param row the row that is being attacked
     * @param column the column that is being attacked
     * @return true if the ship was hit false if it was a miss or the square was already attakced
     */
    public boolean attackSquare(int row, int column){
        Square square = grid[row][column];

        //prevent the same squares from being attacked twice
        if(square.getState() == Square.SquareState.HIT || square.getState() == Square.SquareState.MISS){
            return false;
        }

        // if theres a ship present then turn the state to hit and retunr true
        if (square.getShip() != null) {

            square.setState(Square.SquareState.HIT);

            square.getShip().takeHit();

            return true;
        }

        // if not then set the square to miss and return false
        square.setState(Square.SquareState.MISS);

        return false;

    }

    // loops through all ships and checks if its sunk or not
    public boolean allShipsSunk() {

        for (Ship currentShip : ships) {
            if (!currentShip.isSunk()) {
                return false;
            }
        }

        return true;
    }

    public void removeShip(Ship ship){

        if (!ship.getPosition().isEmpty()) {

            // for every square object sotres inside the sips position set to null then to the water state
            for (Square square : ship.getPosition()) {
                square.setShip(null);
                square.setState(Square.SquareState.WATER);
            }

            ship.clearPosition();
        }
    }

    // checks if all ships have been placed
    public boolean allShipsPlaced(){

        for(Ship ship : ships){

            if(ship.getPosition().size() != ship.getLength()){
                return false;
            }
        }

        return true;
    }

    /**
     *
     * Getters for  rows, columns, Square and ships
     *
     */

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public Square getSquare(int row, int column) {
        return grid[row][column];
    }

    public ArrayList<Ship> getShips(){
        return ships;
    }

}
