import java.util.ArrayList;

public class Board {
    private Square[][] grid;
    private ArrayList<Ship> ship;
    private int rows;
    private int columns;


    public Board (int rows, int columns){
        this.rows = rows;
        this.columns = columns;

        createGrid(rows,columns);
    }

    public void createGrid(int rows, int columns){
        grid = new Square[rows][columns];

        for(int row = 0; row < rows; row++){
            for(int column = 0; column < columns; columns++){
                grid[row][column] = new Square(row, column);
            }
        }
    }


}
