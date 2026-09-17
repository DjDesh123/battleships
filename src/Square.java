public class Square {

    private enum SquareState{
        WATER,
        HIT,
        MISS
    }

    private int rows;
    private int columns;
    private  SquareState state;
    private Ship ship;


    Square(int rows, int columns){
        this.rows = rows;
        this.columns = columns;
    }

    public Ship getShip(){
        return ship;
    }

    public SquareState getState(){
        return state;
    }

    public void setShip (Ship ship){
        this.ship = ship;

    }

    public void setState(SquareState state){
        this.state = state;

    }




}
