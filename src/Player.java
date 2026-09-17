public class Player {
    private String name;
    private int shotsTaken;
    private Board board;



    public Player(String name, int rows, int columns){
        this.name = name;
        this.shotsTaken = 0;
        this.board = new Board(rows,columns);
    }

    public String getName(String name){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public int getShotTaken(){
        return shotsTaken;
    }

    public Board getBoard(){
        return board;
    }

    public boolean placeShip(int shipIndex, int row,int column, Board.Direction direction){


        // selects the ship
        Ship selectedShip = board.getShip(shipIndex);

        // places the actual ship
        return board.placeHumanShip(selectedShip,row,column,direction);
    }

}
