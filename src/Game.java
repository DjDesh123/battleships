public class Game {
    public enum Difficulty{
        EASY,
        NORMAL,
        HARD
    }

    public enum GameState{
        SETUP,
        PLAYING,
        GAMEOVER

    }

    private Board board;
    private Player player;
    private Difficulty difficulty;
    private GameState gameState;
    private int shotsRemaing;



    public Game(Difficulty difficulty){
        this.difficulty = difficulty;
    }

    public void startGame(){
        // assigns board and shots depending on difficulty
        switch(difficulty){
            case EASY:
                board = new Board(5,5);
                shotsRemaing = 60;
                break;
            case NORMAL:
                board = new Board(10,10);
                shotsRemaing = 50;
                break;
            case HARD:
                board = new Board(12,12);
                shotsRemaing = 40;
                break;
        }

        GameState gamestate = GameState.PLAYING;

        // creates the player and computer

        Player humanPlayer = new Player("human");
        Player computerPlayer = new Player("computer");

        // gets their copy of the boards and place their ships
        computerPlayer.getBoard().placeComputerShip();

        // human only creates their fleet for now
        humanPlayer.getBoard().createFleet();


        gamestate = GameState.SETUP;


    }









}
