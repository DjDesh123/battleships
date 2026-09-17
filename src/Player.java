public class Player {
    private String name;
    private int shotsTaken;
    private Board board;



    public Player(String name){
        this.name = name;
        this.shotsTaken = 0;
    }

    public String getName(String name){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public void attack(int row,int column){

    }

    public int getShotTaken(){
        return shotsTaken;

    }

    public void increaseShotTaken(){

    }

    public Board getBoard(){
        return board;
    }

}
