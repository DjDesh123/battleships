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

    // the gui screen for how to play
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

            refreshFrame();
        });

        howToPlayPanel.add(instructions, BorderLayout.CENTER);
        howToPlayPanel.add(okayButton, BorderLayout.SOUTH);

        frame.add(howToPlayPanel);

        refreshFrame();
    }

    /**
     * creates the difficulty selection screen and adds a button
     * for each available game difficulty.
     */
    private void createDifficultyScreen() {

        difficultyPanel.setLayout(new GridLayout(3, 1));

        for (Game.Difficulty difficulty : Game.Difficulty.values()) {

            JButton button = new JButton(difficulty.toString());

            button.addActionListener(e -> createGameModeScreen(difficulty));

            difficultyPanel.add(button);
        }
    }

    // creates the game mode screen and a button for each game mode
    private void createGameModeScreen(Game.Difficulty difficulty) {

        frame.getContentPane().removeAll();

        gameModePanel = new JPanel();
        gameModePanel.setLayout(new GridLayout(2, 1));

        Game.GameMode[] gameModes = Game.GameMode.values();

        for (Game.GameMode gameMode : gameModes) {

            JButton button = new JButton(gameMode.toString());

            button.addActionListener(e -> {
                // resets ui states for a new game
                showingAttackScreen = false;
                humanHasAttacked = false;
                playerOneTurn = true;
                multiplayerHasAttacked = false;
                selectedDirection = Board.Direction.HORIZONTAL;

                game = new Game(difficulty, gameMode);

                game.startGame();

                createPlacementScreen();
            });

            gameModePanel.add(button);
        }

        frame.add(gameModePanel);

        refreshFrame();
    }

    //creates the placement screen to place their ships before the game starts
    private void createPlacementScreen() {


        frame.getContentPane().removeAll();

        boardPanel = new JPanel(new GridLayout(game.getRows(), game.getColumns()));

        shipPanel = new JPanel(new GridLayout(7, 1));

        createBoardGrid();

        for (Ship ship : game.getHumanShips()) {
            createDraggableShip(ship);
        }

        setupRotationKey();
        setupStartKey();

        frame.setLayout(new BorderLayout());

        frame.add(shipPanel, BorderLayout.WEST);
        frame.add(boardPanel, BorderLayout.CENTER);

        refreshFrame();
    }

    private void createHumanGameScreen() {
        frame.getContentPane().removeAll();

        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(game.getRows(), game.getColumns()));

        createBoardGrid();

        frame.add(boardPanel);

        refreshBoard(game.getHumanBoard(), gridButtons, true);

        refreshFrame();
    }

    /**
     * displays the game over screen with the final result and allows the player to return to the main menu or exit the game.
     *
     * @param message the result message displayed to the player
     */
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

            refreshFrame();
        });

        exitButton.addActionListener(e -> {
            frame.dispose();
        });

        buttonPanel.add(mainMenuButton);
        buttonPanel.add(exitButton);

        gameOverPanel.add(gameOverLabel, BorderLayout.CENTER);

        gameOverPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(gameOverPanel);

        refreshFrame();
    }

    private void createBoardGrid() {

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
    }

    /**
     * refreshes the visual state of each square on the board
     *
     * @param board     the board being displayed
     * @param buttons   the buttons representing the board squares
     * @param showShips whether ship locations should be visible
     */
    private void refreshBoard(Board board, JButton[][] buttons, boolean showShips) {

        for (int row = 0; row < game.getRows(); row++) {
            for (int column = 0; column < game.getColumns(); column++) {
                Square.SquareState state = board.getSquare(row, column).getState();

                updateSquareButton(buttons[row][column], state, showShips);
            }
        }
    }

    // updates the  board button based on which state the square has
    private void updateSquareButton(JButton button, Square.SquareState state, boolean showShips) {
        switch (state) {
            case SHIP:
                if (showShips) {
                    button.setBackground(Color.GRAY);
                    button.setOpaque(true);
                } else {
                    clearButton(button);
                }
                break;
            case HIT:
                button.setBackground(Color.RED);
                button.setOpaque(true);
                break;
            case MISS:
                button.setBackground(Color.BLUE);
                button.setOpaque(true);
                break;
            case WATER:
                clearButton(button);
                break;
        }
    }

    private void clearButton(JButton button) {
        button.setBackground(null);
        button.setOpaque(false);
    }

    // creates a draggable button for a ship while allowing the player to place or remove said ship from the board
    private void createDraggableShip(Ship ship) {

        JButton shipButton = new JButton(ship.getName());

        shipButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {

                // uses right click to remove an already placed ship and uses return to prevent the click continuing into placement logic
                if (setupDeletingClick(ship, e)) {
                    shipButton.setText(ship.getName());
                    return;
                }

                // converts the mouse position to the board position to tell where the ship has been dropped
                Point dropPoint = SwingUtilities.convertPoint(shipButton, e.getPoint(), boardPanel);

                // ignores the drop if it's happened outside the board
                if (!boardPanel.contains(dropPoint)) {
                    return;
                }

                // finds the component underneath the position where the ship button as released
                Component target = boardPanel.getComponentAt(dropPoint);

                if (target instanceof JButton) {

                    JButton squareButton = (JButton) target;

                    // each board button stores its row and column as properties when the grid is created
                    int row = (int) squareButton.getClientProperty("row");
                    int column = (int) squareButton.getClientProperty("column");


                    boolean placed = game.placeHumanShip(ship, row, column, selectedDirection);

                    if (placed) {
                        // redraw the board so the newly placed ship appears.
                        refreshBoard(game.getHumanBoard(), gridButtons, true);
                        shipButton.setText(ship.getName() + " - PLACED");

                    } else {
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
            }
        });
    }

    private void setupStartKey() {

        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "start");
        frame.getRootPane().getActionMap().put("start", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                handleStartGame();
            }
        });
    }

    private void handleStartGame() {

        // Stop the game from starting until every ship is placed.
        if (!game.allHumanShipsPlaced()) {
            JOptionPane.showMessageDialog(frame, "Place all ships first");
            return;
        }

        // handles the multiplayer setup separately
        switch (game.getGameMode()) {
            case MULTIPLAYER:

                // if player one has finished placing then it switches to player two
                if (game.switchPlacementPlayer()) {

                    JOptionPane.showMessageDialog(frame, "Player 2 - place your ships");

                    selectedDirection = Board.Direction.HORIZONTAL;

                    createPlacementScreen();
                    return;
                }

                // both players have placed their ships so start multiplayer
                game.startPlaying();

                playerOneTurn = true;
                multiplayerHasAttacked = false;

                JOptionPane.showMessageDialog(frame, "Player 1 - your turn to attack");

                createMultiplayerAttackScreen();
                setupMultiplayerTurnKey();

                break;

            case COMPUTER:

                // start computer mode and allow the computer to attack first

                game.startPlaying();
                frame.remove(shipPanel);

                game.computerAttack();

                Ship sunkShip = game.getLastSunkShip();

                if (sunkShip != null) {
                    JOptionPane.showMessageDialog(frame, "The computer sunk your " + sunkShip.getName() + "!");
                }

                // update the player's board to show the attack.
                refreshBoard(game.getHumanBoard(), gridButtons, true);

                handleGameOver();

                setupTurnKey();

                refreshFrame();

                break;
        }
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

        handleGameOver();
    }

    private void createAttackScreen() {

        frame.getContentPane().removeAll();

        attackPanel = new JPanel(
                new GridLayout(game.getRows(), game.getColumns())
        );

        attackGridButtons =
                new JButton[game.getRows()][game.getColumns()];

        createAttackGrid();

        frame.add(attackPanel);

        // Restore previous hits and misses.
        refreshBoard(
                game.getComputerBoard(),
                attackGridButtons,
                false
        );

        refreshFrame();
    }

    private void createAttackGrid() {

        for (int row = 0; row < game.getRows(); row++) {
            for (int column = 0; column < game.getColumns(); column++) {

                JButton squareButton = new JButton();

                squareButton.putClientProperty("row", row);
                squareButton.putClientProperty("column", column);

                squareButton.addActionListener(
                        e -> handleHumanAttack(squareButton)
                );

                attackGridButtons[row][column] = squareButton;
                attackPanel.add(squareButton);
            }
        }
    }

    /**
     * handles the human player's attack on the computer board.
     *
     * @param squareButton the board square selected by the player
     */
    private void handleHumanAttack(JButton squareButton) {

        // makes sure that there is only one attack is allowed per turn.
        if (humanHasAttacked) {
            return;
        }

        int row = (int) squareButton.getClientProperty("row");

        int column = (int) squareButton.getClientProperty("column");

        Square.SquareState state = game.getComputerBoard().getSquare(row, column).getState();

        // prevents attacking the same square twice.
        if (state == Square.SquareState.HIT || state == Square.SquareState.MISS) {
            return;
        }

        game.humanAttack(row, column);

        Ship sunkShip = game.getLastSunkShip();

        if (sunkShip != null) {
            JOptionPane.showMessageDialog(frame, "You sunk the " + sunkShip.getName() + "!");
        }

        refreshBoard(game.getComputerBoard(), attackGridButtons, false);

        squareButton.setEnabled(false);
        humanHasAttacked = true;

        handleGameOver();
    }

    private void setupTurnKey() {

        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "turn");
        frame.getRootPane().getActionMap().put("turn", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (game.getGameState() == Game.GameState.GAMEOVER) {
                    return;
                }

                if (!showingAttackScreen) {
                    createAttackScreen();
                    showingAttackScreen = true;
                    return;
                }

                // the player must attack before continuing.
                if (!humanHasAttacked) {
                    return;
                }

                // Return to the human board and let the computer attack.
                createHumanGameScreen();
                createAttackPhase();

                // Stop if the computers attack ended the game.
                if (game.getGameState() == Game.GameState.GAMEOVER) {
                    return;
                }

                refreshBoard(game.getHumanBoard(), gridButtons, true);

                showingAttackScreen = false;
                humanHasAttacked = false;
            }
        });
    }

   // creates the attacking screen for a player
    private void createMultiplayerAttackScreen() {

        frame.getContentPane().removeAll();

        attackPanel = new JPanel(new GridLayout(game.getRows(), game.getColumns()));

        attackGridButtons = new JButton[game.getRows()][game.getColumns()];

        Board targetBoard = getMultiplayerTargetBoard();

        createMultiplayerAttackGrid(targetBoard);

        frame.add(attackPanel);

        // restore previous hits and misses on this board.
        refreshBoard(targetBoard, attackGridButtons, false);

        refreshFrame();
    }

    private Board getMultiplayerTargetBoard() {

        if (playerOneTurn) {
            frame.setTitle("Battleships - Player 1 attacking");
            return game.getPlayerTwo().getBoard();
        }

        frame.setTitle("Battleships - Player 2 attacking");
        return game.getPlayerOne().getBoard();
    }

    // creates the grid for the attacking boards
    private void createMultiplayerAttackGrid(Board targetBoard) {

        for (int row = 0; row < game.getRows(); row++) {
            for (int column = 0; column < game.getColumns(); column++) {

                JButton squareButton = new JButton();

                squareButton.putClientProperty("row", row);
                squareButton.putClientProperty("column", column);

                squareButton.addActionListener(e -> handleMultiplayerAttack(squareButton, targetBoard));

                attackGridButtons[row][column] = squareButton;
                attackPanel.add(squareButton);
            }
        }
    }

    /**
     * handles an attack made during multiplayer.
     *
     * @param squareButton the square selected by the attacking player
     * @param targetBoard the opponent's board
     */
    private void handleMultiplayerAttack(JButton squareButton, Board targetBoard) {

        // Only allow one attack per turn.
        if (multiplayerHasAttacked) {
            return;
        }

        int row = (int) squareButton.getClientProperty("row");

        int column = (int) squareButton.getClientProperty("column");

        Square.SquareState state = targetBoard.getSquare(row, column).getState();

        // Prevent attacking the same square twice.
        if (state == Square.SquareState.HIT || state == Square.SquareState.MISS) {
            return;
        }

        boolean hit = game.multiplayerAttack(playerOneTurn, row, column);

        refreshBoard(targetBoard, attackGridButtons, false);

        squareButton.setEnabled(false);
        multiplayerHasAttacked = true;

        // Display the name of a ship if this attack sank it.
        if (hit) {

            Ship ship = targetBoard.getSquare(row, column).getShip();

            if (ship != null && ship.isSunk()) {
                JOptionPane.showMessageDialog(frame, "You sunk the " + ship.getName() + "!");
            }

        }

        handleGameOver();
    }

    private void setupMultiplayerTurnKey() {

        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "multiplayerTurn");
        frame.getRootPane().getActionMap().put("multiplayerTurn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (game.getGameState() == Game.GameState.GAMEOVER) {
                    return;
                }

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


    private void refreshFrame() {
        frame.revalidate();
        frame.repaint();
    }

    private boolean handleGameOver() {

        if (game.getGameState() != Game.GameState.GAMEOVER) {
            return false;
        }

        switch (game.getWinner()) {

            case HUMAN:
                gameOverScreen("YOU WIN!");
                break;

            case COMPUTER:
                gameOverScreen("YOU LOSE!");
                break;

            case PLAYER1:
                gameOverScreen("PLAYER 1 WINS!");
                break;

            case PLAYER2:
                gameOverScreen("PLAYER 2 WINS!");
                break;

            case DRAW:
                gameOverScreen("DRAW!");
                break;
        }

        return true;
    }
}


