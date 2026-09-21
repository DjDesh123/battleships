public class Player {
    private String name;
    private Board board;

    public Player(String name, int rows, int columns){
        this.name = name;
        this.board = new Board(rows,columns);
    }

    public boolean placeShip(Ship ship, int row,int column, Board.Direction direction){
        // places the actual ship
        return board.placeHumanShip(ship,row,column,direction);
    }

    public Board getBoard(){
        return board;
    }

}
