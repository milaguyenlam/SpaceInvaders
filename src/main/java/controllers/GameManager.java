package controllers;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import gameboards.Board;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gameboards.Constants.*;

/***
 * Manages transitions between different panels that the game uses and displays it.
 * Connects to firebase database (firestore) and fetches results data.
 * panels:
 * - Menu - holds buttons (play, results, controls and exit)
 * - Board - panel with the actual game mechanism
 * - Controls - displays game controls and game rules
 * - Results - displays table with all players results
 * - AfterGame - submitting new score
 */
public class GameManager extends JFrame {
    private JPanel panelHolder;
    private JPanel menu;
    private JPanel controls;
    private JPanel results;
    private JButton playButton;
    private JButton resultsButton;
    private JButton controlsButton;
    private JTable resultsTable;
    private JButton backButton;
    private JButton backButton2;
    private JButton exitButton;
    private JPanel afterGame;
    private JTextField playerTextField;
    private JButton submitButton;
    private JButton backButton1;
    private JLabel scoreLabel;
    private JLabel submitErrorLabel;
    private JLabel databaseConnectionLabel;
    private Board gameBoard;
    private Firestore database;

    private int currentScore;
    private boolean connectionInitialized = true;


    /**
     * entry point of the program, initializes GameManager class instance as a JFrame.
     *
     * @param args cli arguments
     */
    public static void main(String[] args) {
        JFrame frame = new GameManager("Space Invaders");
    }

    /**
     * initializes JFrame settings and connects to the database
     * renders main menu first
     * adds Listener classes to buttons (functionality when clicked)
     * JFrame settings:
     * - size 600x450
     * - not resizable
     * - visible
     * - default close operation : JFrame.EXIT_ON_CLOSE
     *
     * @param title JFrame title text
     */
    public GameManager(String title) {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addListenersToUIButtons();
        renderPanelAndDisableRest(menu);
        this.setContentPane(panelHolder);
        this.setSize(BOARD_WIDTH, BOARD_HEIGHT);
        this.setResizable(false);
        this.setVisible(true);
        initializeResultsTable();
        try {
            initializeDatabase();
        } catch (Exception e) {
            databaseConnectionLabel.setText("Couldn't connect to the database!");
            connectionInitialized = false;
        }
    }


    /**
     * renders afterGame panel where user is able to submit his score and alias to the database
     *
     * @param score score player gained
     */
    public void renderAfterGame(int score) {
        String scoreString = Integer.toString(score);
        scoreLabel.setText("your result was " + scoreString + " points.");
        currentScore = score;
        renderPanelAndDisableRest(afterGame);
    }

    /**
     * renders main menu
     */
    public void getBackToMainMenu() {
        renderPanelAndDisableRest(menu);
    }

    /**
     * initialize database connection from ServiceAccountKey.json file and assigns database variable
     *
     * @throws IOException
     */
    private void initializeDatabase() throws IOException {
        InputStream serviceAccount = this.getClass().getClassLoader().getResourceAsStream("ServiceAccountKey.json");
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        FirebaseApp.initializeApp(options);
        database = FirestoreClient.getFirestore();
    }

    /**
     * fetches results from the database, sorts by score (descending order) and adds it to JTable holding these results
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void fetchResultDataAndFillResultTable() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query = database.collection(DB_SCORES_COLLECTION).get();
        QuerySnapshot querySnapshot = query.get();
        List<Score> fetchedScores = new ArrayList<>();
        DefaultTableModel tableModel = (DefaultTableModel) resultsTable.getModel();
        //empties the table
        tableModel.setRowCount(0);
        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            //instantiates Score instance from firestore data and adds to fetchedScores list
            String playerName = document.getString(DB_NAME_ATTRIBUTE);
            int playerScore = document.getDouble(DB_SCORE_ATTRIBUTE).intValue();
            fetchedScores.add(new Score(playerName, playerScore));
        }
        Collections.sort(fetchedScores);
        for (Score score : fetchedScores) {
            //adding records to the table
            tableModel.addRow(new Object[]{score.name, score.score});
        }
    }

    /**
     * initializing Table with 2 columns - name/alias and score
     */
    private void initializeResultsTable() {
        DefaultTableModel tableModel = (DefaultTableModel) resultsTable.getModel();
        tableModel.addColumn(DB_NAME_ATTRIBUTE);
        tableModel.addColumn(DB_SCORE_ATTRIBUTE);
    }

    /**
     * adds new name+score record to the database
     *
     * @param playerName player name/alias
     * @param score      player's gained score
     */
    private void addNewScore(String playerName, int score) {
        Map<String, Object> newDatabaseRecord = new HashMap<>();
        newDatabaseRecord.put(DB_NAME_ATTRIBUTE, playerName);
        newDatabaseRecord.put(DB_SCORE_ATTRIBUTE, score);
        ApiFuture<DocumentReference> result = database.collection(DB_SCORES_COLLECTION).add(newDatabaseRecord);
    }

    /**
     * correct format:
     * - username consists of 6 to 30 characters (ASCII)
     * - only contains alphanumeric characters and underscore
     * - the first character must be an alphabetic character
     *
     * @param playerName player's name/alias
     * @return boolean is players name is in correct format or not
     */
    private boolean playerNameIsCorrect(String playerName) {
        Pattern p = Pattern.compile(USER_NAME_REGEXP);
        Matcher m = p.matcher(playerName);
        return m.matches();
    }

    /**
     * resetting afterGame panel's initial values
     */
    private void resetAfterGame() {
        submitErrorLabel.setText(null);
        playerTextField.setText("player");
    }

    /**
     * renders given panel and disables all the rest
     *
     * @param panel panel to be rendered
     */
    private void renderPanelAndDisableRest(JPanel panel) {
        disableAllPanels();
        panel.setVisible(true);
    }

    /**
     * disables all the panels
     */
    private void disableAllPanels() {
        menu.setVisible(false);
        controls.setVisible(false);
        results.setVisible(false);
        afterGame.setVisible(false);
    }

    /**
     * creates Board class instance and adds it to the panel holder and renders it
     * also removes previous Board instance from the panel holder
     */
    private void startGame() {
        if (gameBoard != null) {
            panelHolder.remove(gameBoard);
        }
        gameBoard = new Board(this);
        panelHolder.add(gameBoard);
        renderPanelAndDisableRest(gameBoard);
    }

    /**
     * adding Listener instances to give all the buttons functionality when clicked
     * exit (in main menu): exit from the program (exitcode: 0)
     * play (in main menu): calls startGame() method
     * results (in main menu): fetches results data is the connection was initialized otherwise displays error message, then renders results panel
     * controls (in main menu): renders controls panel
     * back (in controls): renders menu panel
     * back (in results): renders menu panel
     * back (in after game): resets after game initial values and renders menu panel
     * submit (in after game): checks if given playername is in correct format and then submits the data (name+score) to the database, resets after game panel and renders menu, otherwise only displays error message.
     */
    private void addListenersToUIButtons() {
        exitButton.addActionListener((actionEvent -> System.exit(0)));
        playButton.addActionListener(actionEvent -> startGame());
        resultsButton.addActionListener(actionEvent -> loadResults());
        controlsButton.addActionListener(actionEvent -> renderPanelAndDisableRest(controls));
        backButton.addActionListener(actionEvent -> renderPanelAndDisableRest(menu));
        backButton2.addActionListener(actionEvent -> renderPanelAndDisableRest(menu));
        submitButton.addActionListener(actionEvent -> submitResult());
        backButton1.addActionListener(actionEvent -> {
            resetAfterGame();
            renderPanelAndDisableRest(menu);
        });
    }

    /**
     * fetches results data is the connection was initialized otherwise displays error message, then renders results panel
     */
    private void submitResult() {
        String playerName = playerTextField.getText();
        if (playerNameIsCorrect(playerName)) {
            addNewScore(playerName, currentScore);
            resetAfterGame();
            renderPanelAndDisableRest(menu);
        } else {
            submitErrorLabel.setText("<html>Incorrect player name!<br>" +
                    "- username consists of 6 to 30 characters (ASCII)<br>" +
                    "- only contains alphanumeric characters and underscore<br>" +
                    "- the first character must be an alphabetic character</html>");
        }
    }

    /**
     * checks if given playername is in correct format and then submits the data (name+score) to the database, resets after game panel and renders menu, otherwise only displays error message.
     */
    private void loadResults() {
        if (connectionInitialized) {
            try {
                fetchResultDataAndFillResultTable();
                databaseConnectionLabel.setText("");
                connectionInitialized = true;
            } catch (Exception e) {
                databaseConnectionLabel.setText("Couldn't connect to the database!");
                connectionInitialized = false;
            }
        }
        renderPanelAndDisableRest(results);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelHolder = new JPanel();
        panelHolder.setLayout(new CardLayout(0, 0));
        panelHolder.setBorder(BorderFactory.createTitledBorder(null, "4", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        menu = new JPanel();
        menu.setLayout(new GridLayoutManager(5, 3, new Insets(0, 0, 0, 0), -1, -1));
        panelHolder.add(menu, "Card1");
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$(null, Font.BOLD, 22, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setForeground(new Color(-16739346));
        label1.setText("Space Invaders: Ice edition");
        menu.add(label1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        menu.add(panel1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        playButton = new JButton();
        playButton.setText("play");
        panel1.add(playButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        controlsButton = new JButton();
        controlsButton.setText("controls and rules");
        panel1.add(controlsButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        resultsButton = new JButton();
        resultsButton.setText("results");
        panel1.add(resultsButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        menu.add(spacer1, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        menu.add(spacer2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        menu.add(spacer3, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        exitButton = new JButton();
        exitButton.setText("exit");
        menu.add(exitButton, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        menu.add(spacer4, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        controls = new JPanel();
        controls.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        panelHolder.add(controls, "Card3");
        final JLabel label2 = new JLabel();
        label2.setText("Controls and rules");
        controls.add(label2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        controls.add(spacer5, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(7, 1, new Insets(0, 0, 0, 0), -1, -1));
        controls.add(panel2, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("moving sideways: left and right arrows");
        panel2.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("shooting: spacebar");
        panel2.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("back to main menu: escape");
        panel2.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setForeground(new Color(-16739346));
        label6.setText("rules:");
        panel2.add(label6, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("hitting an enemy: +1 point");
        panel2.add(label7, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("player gets hit: -3 points");
        panel2.add(label8, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("enemy reaches ground: -5 points");
        panel2.add(label9, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer6 = new Spacer();
        controls.add(spacer6, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer7 = new Spacer();
        controls.add(spacer7, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        backButton = new JButton();
        backButton.setText("back");
        controls.add(backButton, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer8 = new Spacer();
        controls.add(spacer8, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        Font label10Font = this.$$$getFont$$$(null, -1, -1, label10.getFont());
        if (label10Font != null) label10.setFont(label10Font);
        label10.setForeground(new Color(-16739346));
        label10.setText("controls:");
        controls.add(label10, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        results = new JPanel();
        results.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        panelHolder.add(results, "Card4");
        final JLabel label11 = new JLabel();
        label11.setText("Results");
        results.add(label11, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer9 = new Spacer();
        results.add(spacer9, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer10 = new Spacer();
        results.add(spacer10, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer11 = new Spacer();
        results.add(spacer11, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer12 = new Spacer();
        results.add(spacer12, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        backButton2 = new JButton();
        backButton2.setText("back");
        results.add(backButton2, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        results.add(scrollPane1, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        resultsTable = new JTable();
        resultsTable.setFillsViewportHeight(false);
        resultsTable.setShowHorizontalLines(false);
        resultsTable.setShowVerticalLines(false);
        scrollPane1.setViewportView(resultsTable);
        databaseConnectionLabel = new JLabel();
        databaseConnectionLabel.setEnabled(true);
        databaseConnectionLabel.setForeground(new Color(-4521964));
        databaseConnectionLabel.setText("");
        results.add(databaseConnectionLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        afterGame = new JPanel();
        afterGame.setLayout(new GridLayoutManager(8, 3, new Insets(0, 0, 0, 0), -1, -1));
        afterGame.setEnabled(true);
        panelHolder.add(afterGame, "Card2");
        scoreLabel = new JLabel();
        scoreLabel.setText("");
        afterGame.add(scoreLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer13 = new Spacer();
        afterGame.add(spacer13, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        afterGame.add(panel3, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        playerTextField = new JTextField();
        playerTextField.setText("player");
        panel3.add(playerTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer14 = new Spacer();
        afterGame.add(spacer14, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer15 = new Spacer();
        afterGame.add(spacer15, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer16 = new Spacer();
        afterGame.add(spacer16, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("your name:");
        afterGame.add(label12, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        submitButton = new JButton();
        submitButton.setText("submit");
        afterGame.add(submitButton, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        backButton1 = new JButton();
        backButton1.setText("back");
        afterGame.add(backButton1, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        submitErrorLabel = new JLabel();
        submitErrorLabel.setForeground(new Color(-4521964));
        submitErrorLabel.setText("");
        afterGame.add(submitErrorLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelHolder;
    }
}
