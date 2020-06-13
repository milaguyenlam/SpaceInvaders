package controllers;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import gameboards.Board;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
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
public class GameManager extends JFrame{
    public static final String DB_SCORES_COLLECTION = "scores";
    public static final String DB_SCORE_ATTRIBUTE = "score";
    public static final String DB_NAME_ATTRIBUTE = "name";

    private JPanel panelHolder;
    private JPanel Menu;
    private JPanel Controls;
    private JPanel Results;
    private JButton playButton;
    private JButton resultsButton;
    private JButton controlsButton;
    private JTable resultsTable;
    private JButton backButton;
    private JButton backButton2;
    private JButton exitButton;
    private JPanel AfterGame;
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
    private String userNameRegexp = "^[aA-zZ]\\w{5,29}$";

    /**
     * entry point of the program, initializes GameManager class instance as a JFrame.
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
     * @param title JFrame title text
     */
    public GameManager(String title) {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addListenersToUIButtons();
        renderPanelAndDisableRest(Menu);
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
     * @param score score player gained
     */
    public void renderAfterGame(int score) {
        String scoreString = Integer.toString(score);
        scoreLabel.setText("your result was " + scoreString + " points.");
        currentScore = score;
        renderPanelAndDisableRest(AfterGame);
    }

    /**
     * renders main menu
     */
    public void getBackToMainMenu() {
        renderPanelAndDisableRest(Menu);
    }

    /**
     * initialize database connection from ServiceAccountKey.json file and assigns database variable
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
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void fetchResultDataAndFillResultTable() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> query = database.collection(DB_SCORES_COLLECTION).get();
        QuerySnapshot querySnapshot = query.get();
        ArrayList<Score> fetchedScores = new ArrayList<>();
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
        fetchedScores.sort(new Comparator<Score>() {
            //comparing 2 Score instances
            @Override
            public int compare(Score score1, Score score2) {
                return score2.score - score1.score;
            }
        });
        for (Score score : fetchedScores) {
            //adding records to the table
            tableModel.addRow(new Object[] {score.name, score.score});
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
     * @param playerName player name/alias
     * @param score player's gained score
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
     * @param playerName player's name/alias
     * @return boolean is players name is in correct format or not
     */
    private boolean playerNameIsCorrect(String playerName) {
        Pattern p = Pattern.compile(userNameRegexp);
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
     * @param panel panel to be rendered
     */
    private void renderPanelAndDisableRest(JPanel panel){
        disableAllPanels();
        panel.setVisible(true);
    }

    /**
     * disables all the panels
     */
    private void disableAllPanels() {
        Menu.setVisible(false);
        Controls.setVisible(false);
        Results.setVisible(false);
        AfterGame.setVisible(false);
    }

    /**
     * creates Board class instance and adds it to the panel holder and renders it
     * also removes previous Board instance from the panel holder
     */
    private void startGame() {
        if(gameBoard != null) {
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
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.exit(0);
            }
        });
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                startGame();
            }
        });
        resultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(connectionInitialized) {
                    try {
                        fetchResultDataAndFillResultTable();
                        databaseConnectionLabel.setText("");
                        connectionInitialized = true;
                    } catch (Exception e) {
                        databaseConnectionLabel.setText("Couldn't connect to the database!");
                        connectionInitialized = false;
                    }
                }
                renderPanelAndDisableRest(Results);
            }
        });
        controlsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                renderPanelAndDisableRest(Controls);
            }
        });
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                renderPanelAndDisableRest(Menu);
            }
        });
        backButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                renderPanelAndDisableRest(Menu);
            }
        });
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String playerName = playerTextField.getText();
                if(playerNameIsCorrect(playerName)){
                    addNewScore(playerName, currentScore);
                    resetAfterGame();
                    renderPanelAndDisableRest(Menu);
                }
                else{
                    submitErrorLabel.setText("<html>Incorrect player name!<br>" +
                            "- username consists of 6 to 30 characters (ASCII)<br>" +
                            "- only contains alphanumeric characters and underscore<br>" +
                            "- the first character must be an alphabetic character</html>");
                }
            }
        });
        backButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                resetAfterGame();
                renderPanelAndDisableRest(Menu);
            }
        });
    }

}
