import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GameUI {

    private JFrame frame;
    private JPanel boardPanel;
    private JPanel difficultyPanel;
    private JPanel gameModePanel;
    private JPanel shipPanel;
    private JPanel attackPanel;

    private JButton[][] attackGridButtons;
    private JButton[][] gridButtons;

    private Board.Direction selectedDirection = Board.Direction.HORIZONTAL;

    private boolean showingAttackScreen = false;
    private boolean humanHasAttacked = false;

    // Multiplayer turn state
    private boolean playerOneTurn = true;
    private boolean multiplayerHasAttacked = false;

    private Game game;


    public GameUI() {
        frame = new JFrame("Battleships");
        frame.setSize(800, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        howToPlayScreen();
        frame.setVisible(true);
    }


    private void createDifficultyScreen() {

        difficultyPanel.setLayout(new GridLayout(3, 1));

        Game.Difficulty[] difficulties = Game.Difficulty.values();

        for (Game.Difficulty difficulty : difficulties) {

            JButton button = new JButton(difficulty.toString());

            button.addActionListener(e -> createGameModeScreen(difficulty));

            difficultyPanel.add(button);
        }
    }


    private void createGameModeScreen(Game.Difficulty difficulty) {

        frame.getContentPane().removeAll();

        gameModePanel = new JPanel();
        gameModePanel.setLayout(new GridLayout(2, 1));

        Game.GameMode[] gameModes = Game.GameMode.values();

        for (Game.GameMode gameMode : gameModes) {

            JButton button = new JButton(gameMode.toString());

            button.addActionListener(e -> {
                game = new Game(difficulty, gameMode);
                game.startGame();
                createPlacementScreen();
            });

            gameModePanel.add(button);
        }

        frame.add(gameModePanel);

        frame.revalidate();
        frame.repaint();
    }


    private void createPlacementScreen() {

        // Important for multiplayer: completely rebuild the screen
        // when switching from Player 1 placement to Player 2 placement.
        frame.getContentPane().removeAll();

        boardPanel = new JPanel();
        shipPanel = new JPanel();

        boardPanel.setLayout(new GridLayout(game.getRows(), game.getColumns()));

        shipPanel.setLayout(new GridLayout(7, 1));

        gridButtons = new JButton[game.getRows()][game.getColumns()];


        for (int row = 0; row < game.getRows(); row++) {
            for (int column = 0; column < game.getColumns(); column++) {

                JButton squareButton = new JButton();

                squareButton.putClientProperty("row", row);
                squareButton.putClientProperty("column", column);

                gridButtons[row][column] = squareButton;

                boardPanel.add(squareButton);
            }
        }


        for (Ship ship : game.getHumanShips()) {
            createDragableShip(ship);
        }


        setupRotationKey();
        setupStartKey();

        frame.setLayout(new BorderLayout());

        frame.add(shipPanel, BorderLayout.WEST);
        frame.add(boardPanel, BorderLayout.CENTER);

        frame.revalidate();
        frame.repaint();
    }


    private void refreshBoard(Board board, JButton[][] buttons, boolean showShips) {
        for (int row = 0; row < game.getRows(); row++) {
            for (int column = 0; column < game.getColumns(); column++) {
                Square.SquareState state = board.getSquare(row, column).getState();

                switch (state) {

                    case SHIP:

                        if (showShips) {
                            buttons[row][column].setBackground(Color.GRAY);
                            buttons[row][column].setOpaque(true);
                        }else{
                            buttons[row][column].setBackground(null);
                            buttons[row][column].setOpaque(false);
                        }
                        break;

                    case HIT:
                        buttons[row][column].setBackground(Color.RED);
                        buttons[row][column].setOpaque(true);
                        break;


                    case MISS:
                        buttons[row][column].setBackground(Color.BLUE);
                        buttons[row][column].setOpaque(true);
                        break;


                    case WATER:
                        buttons[row][column].setBackground(null);
                        buttons[row][column].setOpaque(false);
                        break;
                }
            }
        }
    }


    private void createDragableShip(Ship ship) {

        JButton shipButton = new JButton(ship.getName());

        shipButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                // Right click removes the ship and should not continue
                // into placement logic afterward.
                if (setupDeletingClick(ship, e)) {
                    shipButton.setText(ship.getName());
                    return;
                }


                Point dropPoint = SwingUtilities.convertPoint(shipButton, e.getPoint(), boardPanel);



                if (!boardPanel.contains(dropPoint)) {
                    return;
                }


                Component target = boardPanel.getComponentAt(dropPoint);


                if (target instanceof JButton) {

                    JButton squareButton = (JButton) target;

                    int row = (int) squareButton.getClientProperty("row");

                    int column = (int) squareButton.getClientProperty("column");


                    boolean placed = game.placeHumanShip(ship, row, column, selectedDirection);


                    if (placed) {
                        refreshBoard(game.getHumanBoard(), gridButtons, true);
                        shipButton.setText(ship.getName() + " - PLACED");

                    }else {

                        JOptionPane.showMessageDialog(frame, "Invalid ship placement");
                    }
                }
            }
        });

        shipPanel.add(shipButton);
    }


    private boolean setupDeletingClick(Ship ship, MouseEvent e) {

        if (SwingUtilities.isRightMouseButton(e)) {

            game.removeHumanShip(ship);

            refreshBoard(game.getHumanBoard(), gridButtons, true);
            return true;
        }

        return false;
    }


    private void setupRotationKey() {

        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("R"), "rotate");


        frame.getRootPane().getActionMap().put("rotate", new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        if (selectedDirection == Board.Direction.HORIZONTAL) {
                            selectedDirection = Board.Direction.VERTICAL;
                        } else {
                            selectedDirection = Board.Direction.HORIZONTAL;
                        }

                        System.out.println(
                                "Direction: " + selectedDirection
                        );
                    }
        });
    }


    private void setupStartKey() {

        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "start");


        frame.getRootPane().getActionMap().put("start", new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        if (!game.allHumanShipsPlaced()) {
                            JOptionPane.showMessageDialog(frame, "Place all ships first");
                            return;
                        }


                        if (game.getGameMode() == Game.GameMode.MULTIPLAYER) {
                            if (game.switchPlacementPlayer()) {

                                JOptionPane.showMessageDialog(frame, "Player 2 - place your ships");

                                selectedDirection = Board.Direction.HORIZONTAL;

                                createPlacementScreen();

                                return;
                            }

                            game.startPlaying();

                            playerOneTurn = true;
                            multiplayerHasAttacked = false;

                            JOptionPane.showMessageDialog(frame, "Player 1 - your turn to attack");

                            createMultiplayerAttackScreen();
                            setupMultiplayerTurnKey();

                            return;
                        }


                        game.startPlaying();

                        frame.remove(shipPanel);

                        // Computer attacks first
                        game.computerAttack();

                        Ship sunkShip = game.getLastSunkShip();

                        if (sunkShip != null) {
                            JOptionPane.showMessageDialog(frame, "The computer sunk your " + sunkShip.getName() + "!");
                        }



                        refreshBoard(game.getHumanBoard(), gridButtons, true);


                        if (game.getGameState() == Game.GameState.GAMEOVER) {
                            gameOverScreen("YOU LOSE!");
                            return;
                        }


                        setupTurnKey();

                        frame.revalidate();
                        frame.repaint();
                    }
        });
    }


    private void createAttackPhase() {

        game.computerAttack();

        int row = game.getLastComputerRow();
        int column = game.getLastComputerColumn();

        JButton attackedSquare = gridButtons[row][column];

        attackedSquare.setOpaque(true);

        Square.SquareState state = game.getHumanBoard().getSquare(row, column).getState();


        if (state == Square.SquareState.HIT) {
            attackedSquare.setBackground(Color.RED);
        } else {
            attackedSquare.setBackground(Color.BLUE);
        }

        attackedSquare.repaint();


        // Check whether the computer just sunk one of the player's ships
        Ship sunkShip = game.getLastSunkShip();

        if (sunkShip != null) {
            JOptionPane.showMessageDialog(frame, "The computer sunk your " + sunkShip.getName() + "!");
        }

        if (game.getGameState() == Game.GameState.GAMEOVER) {
            gameOverScreen("YOU LOSE!");
        }
    }


    private void createAttackScreen() {

        frame.getContentPane().removeAll();

        attackPanel = new JPanel();

        attackPanel.setLayout(new GridLayout(game.getRows(), game.getColumns()));

        attackGridButtons = new JButton[game.getRows()][game.getColumns()];


        for (int row = 0; row < game.getRows(); row++) {
            for (int column = 0; column < game.getColumns(); column++) {

                JButton squareButton = new JButton();

                squareButton.putClientProperty("row", row);
                squareButton.putClientProperty("column", column);

                attackGridButtons[row][column] = squareButton;


                squareButton.addActionListener(e -> {

                    if (humanHasAttacked) {
                        return;
                    }


                    int attackRow = (int) squareButton.getClientProperty("row");

                    int attackColumn = (int) squareButton.getClientProperty("column");


                    Square.SquareState state = game.getComputerBoard().getSquare(attackRow, attackColumn).getState();

                    // Already fired at this square
                    if (state == Square.SquareState.HIT || state == Square.SquareState.MISS) {
                        return;
                    }

                    Ship sunkShip = game.getLastSunkShip();

                    if (sunkShip != null) {
                        JOptionPane.showMessageDialog(frame, "You sunk the " + sunkShip.getName() + "!");
                    }


                    game.humanAttack(attackRow, attackColumn);


                    refreshBoard(game.getComputerBoard(), attackGridButtons, false);


                    squareButton.setEnabled(false);
                    humanHasAttacked = true;


                    if (game.getGameState() == Game.GameState.GAMEOVER) {
                        if (game.getWinner() == Game.WinnerState.HUMAN) {

                            gameOverScreen("YOU WIN!");

                        } else {

                            gameOverScreen("YOU LOSE! OUT OF SHOTS");
                        }
                    }
                });

                attackPanel.add(squareButton);
            }
        }


        frame.add(attackPanel);

        // Restores all previous red / blue attack squares
        // whenever this screen is rebuilt.
        refreshBoard(game.getComputerBoard(), attackGridButtons, false);

        frame.revalidate();
        frame.repaint();
    }


    private void setupTurnKey() {

        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "turn");
        frame.getRootPane().getActionMap().put("turn", new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (game.getGameState()
                                == Game.GameState.GAMEOVER) {
                            return;
                        }


                        if (showingAttackScreen) {

                            if (!humanHasAttacked) {
                                return;
                            }


                            createHumanGameScreen();

                            createAttackPhase();


                            if (game.getGameState()
                                    == Game.GameState.GAMEOVER) {
                                return;
                            }


                            refreshBoard(
                                    game.getHumanBoard(),
                                    gridButtons,
                                    true
                            );

                            showingAttackScreen = false;
                            humanHasAttacked = false;

                        } else {

                            createAttackScreen();
                            showingAttackScreen = true;
                        }
                    }
        });
    }


    private void createHumanGameScreen() {

        frame.getContentPane().removeAll();

        boardPanel = new JPanel();

        boardPanel.setLayout(new GridLayout(game.getRows(), game.getColumns()));

        gridButtons = new JButton[game.getRows()][game.getColumns()];


        for (int row = 0; row < game.getRows(); row++) {
            for (int column = 0; column < game.getColumns(); column++) {

                JButton squareButton = new JButton();

                gridButtons[row][column] = squareButton;

                boardPanel.add(squareButton);
            }
        }


        frame.add(boardPanel);

        refreshBoard(game.getHumanBoard(), gridButtons, true);

        frame.revalidate();
        frame.repaint();
    }

    private void createMultiplayerAttackScreen() {

        frame.getContentPane().removeAll();

        attackPanel = new JPanel();

        attackPanel.setLayout(new GridLayout(game.getRows(), game.getColumns()));

        attackGridButtons = new JButton[game.getRows()][game.getColumns()];

        Board targetBoard;

        if (playerOneTurn) {
            targetBoard = game.getPlayerTwo().getBoard();
            frame.setTitle("Battleships - Player 1 attacking");
        } else {
            targetBoard = game.getPlayerOne().getBoard();
            frame.setTitle("Battleships - Player 2 attacking");
        }


        for (int row = 0; row < game.getRows(); row++) {

            for (int column = 0; column < game.getColumns(); column++) {

                JButton squareButton = new JButton();

                squareButton.putClientProperty("row", row);
                squareButton.putClientProperty("column", column);

                attackGridButtons[row][column] = squareButton;


                squareButton.addActionListener(e -> {

                    if (multiplayerHasAttacked) {
                        return;
                    }


                    int attackRow = (int) squareButton.getClientProperty("row");

                    int attackColumn = (int) squareButton.getClientProperty("column");


                    Square.SquareState state = targetBoard.getSquare(attackRow, attackColumn).getState();


                    if (state == Square.SquareState.HIT || state == Square.SquareState.MISS) {
                        return;
                    }


                    boolean hit = game.multiplayerAttack(playerOneTurn, attackRow, attackColumn);


                    refreshBoard(targetBoard, attackGridButtons, false);


                    squareButton.setEnabled(false);
                    multiplayerHasAttacked = true;


                    if (hit) {

                        Ship ship = targetBoard.getSquare(attackRow, attackColumn).getShip();

                        if (ship != null && ship.isSunk()) {
                            JOptionPane.showMessageDialog(frame, "You sunk the " + ship.getName() + "!");
                        }
                    }


                    if (game.getGameState() == Game.GameState.GAMEOVER) {

                        if (game.getWinner() == Game.WinnerState.PLAYER1) {
                            gameOverScreen("PLAYER 1 WINS!");
                        } else {
                            gameOverScreen("PLAYER 2 WINS!");
                        }

                        return;
                    }
                });

                attackPanel.add(squareButton);
            }
        }


        frame.add(attackPanel);

        // Restore this player's previous attacks.
        refreshBoard(targetBoard, attackGridButtons, false);

        frame.revalidate();
        frame.repaint();
    }


    private void setupMultiplayerTurnKey() {

        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "multiplayerTurn");
        frame.getRootPane().getActionMap().put("multiplayerTurn", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                        if (!multiplayerHasAttacked) {
                            return;
                        }


                        playerOneTurn = !playerOneTurn;
                        multiplayerHasAttacked = false;


                        if (playerOneTurn) {

                            JOptionPane.showMessageDialog(frame, "Player 1 - your turn to attack");

                        } else {

                            JOptionPane.showMessageDialog(frame, "Player 2 - your turn to attack");
                        }

                        createMultiplayerAttackScreen();
                    }
                });
    }


    private void gameOverScreen(String message) {

        frame.getContentPane().removeAll();

        JPanel gameOverPanel = new JPanel();
        gameOverPanel.setLayout(new BorderLayout());

        JLabel gameOverLabel = new JLabel(message, SwingConstants.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

        JButton mainMenuButton = new JButton("Main Menu");
        JButton exitButton = new JButton("Exit Game");


        mainMenuButton.addActionListener(e -> {

            frame.getContentPane().removeAll();

            difficultyPanel = new JPanel();

            frame.add(difficultyPanel);

            createDifficultyScreen();

            frame.revalidate();
            frame.repaint();
        });


        exitButton.addActionListener(e -> {frame.dispose();});


        buttonPanel.add(mainMenuButton);
        buttonPanel.add(exitButton);

        gameOverPanel.add(gameOverLabel, BorderLayout.CENTER);

        gameOverPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(gameOverPanel);

        frame.revalidate();
        frame.repaint();
    }


    private void howToPlayScreen() {

        frame.getContentPane().removeAll();

        JPanel howToPlayPanel = new JPanel();
        howToPlayPanel.setLayout(new BorderLayout());

        JLabel instructions = new JLabel(
                "<html><div style='text-align: center;'>"
                        + "<h1>How To Play</h1>"
                        + "Place all of your ships onto the board.<br><br>"
                        + "Press R to rotate a ship.<br>"
                        + "Right click a placed ship to remove it.<br>"
                        + "Press ENTER when all ships have been placed.<br><br>"
                        + "During battle, click a square to attack.<br>"
                        + "Red = Hit<br>"
                        + "Blue = Miss<br>"
                        + "Grey = Your Ships<br><br>"
                        + "Destroy all enemy ships before losing your own fleet "
                        + "or running out of shots."
                        + "</div></html>",
                SwingConstants.CENTER);

        JButton okayButton = new JButton("OK");

        okayButton.addActionListener(e -> {
            frame.getContentPane().removeAll();

            difficultyPanel = new JPanel();

            frame.add(difficultyPanel);

            createDifficultyScreen();

            frame.revalidate();
            frame.repaint();
        });

        howToPlayPanel.add(instructions, BorderLayout.CENTER);
        howToPlayPanel.add(okayButton, BorderLayout.SOUTH);

        frame.add(howToPlayPanel);

        frame.revalidate();
        frame.repaint();
    }
}
