import java.util.ArrayList;

public class Ship {
    private String name;
    private int length;
    private int hits;
    private ArrayList<Square> position;

    public Ship(String name, int length){
        this.name = name;
        this.length = length;
        this.hits =0;
        this.position = new ArrayList<>();
    }

    public String getName(){
        return name;
    }

    public int getLength(){
        return length;
    }

    public int getHits(){
        return hits;
    }

    public ArrayList<Square> getPosition(){
        return position;
    }

    public void addPosition(Square square){
        position.add(square);
    }







}
