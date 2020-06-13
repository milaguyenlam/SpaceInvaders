package gameboards;

import controllers.GameManager;
import gameobjects.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static gameboards.Constants.*;

public class Board extends JPanel implements Runnable {
    private GameManager gameManager;
    private Player player;
    private EnemyGroup enemyGroup;
    private ObstacleGroup obstacles;

    private Integer score;

    private boolean inGame;
    private Integer lives;

    private InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    private ActionMap actionMap = this.getActionMap();


    /**
     * initializes all game objects and initial values
     * initializes JPanel settings, registers used keys (for controlling the game)
     * assign GameManager
     * @param gameManager GameManager class instance to interact with
     */
    public Board(GameManager gameManager) {
        this.gameManager = gameManager;
        setFocusable(true);
        setBackground(Color.BLACK);
        initializeBoard();
        registerKeys();
    }

    /**
     * initializes all game objects and initial values
     */
    private void initializeBoard() {
        inGame=true;
        lives=3;
        player=new Player(START_X, START_Y);
        enemyGroup = new EnemyGroup();
        obstacles = new ObstacleGroup();
        score = 0;
    }

    /**
     * registers keys and their actions using Action API
     */
    private void registerKeys() {
        addPlayerKeyBindings(KeyEvent.VK_LEFT, "left");
        addPlayerKeyBindings(KeyEvent.VK_RIGHT, "right");
        addPlayerKeyBindings(KeyEvent.VK_SPACE, "shoot");
        addCustomKeyBindings(KeyEvent.VK_ESCAPE, "back", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                inGame = false;
                gameManager.getBackToMainMenu();
            }
        });
    }

    /**
     * registers a key with custom action (that will be executed whenever clicked)
     * @param keyCode code of to be registered key
     * @param id id of the pair (key+action)
     * @param action action to be done when key pressed
     */
    private void addCustomKeyBindings(int keyCode, String id, AbstractAction action) {
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, false), id);
        actionMap.put(id, action);
    }

    /**
     * registers a key (both pressing and releasing), action is just propagating the keycode to the Player class instance
     * @param keyCode code of to be registered key
     * @param id id of the pair (key+action)
     */
    private void addPlayerKeyBindings(int keyCode, String id) {
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, false), id);
        inputMap.put(KeyStroke.getKeyStroke(keyCode,0, true), id+"-release");
        actionMap.put(id, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                player.keyPressed(keyCode);
            }
        });
        actionMap.put(id + "-release", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                player.keyReleased(keyCode);
            }
        });
    }

    /**
     * starts thread execution - starting the game and calls its parent's (JPanel) addNotify() function
     */
    @Override
    public void addNotify() {
        super.addNotify();
        Thread animator = new Thread(this);
        animator.start();
    }

    /**
     * basic game loop mechanism (refreshes after FRAME_REFRESH_TIME)
     */
    @Override
    public void run() {

        long beforeTime, timeDiff, sleep;

        beforeTime = System.currentTimeMillis();

        while(inGame) {
            repaint();
            refreshFrame();

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = FRAME_REFRESH_TIME - timeDiff;

            if(sleep<0) {
                sleep = 2;
            }
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            beforeTime=System.currentTimeMillis();
        }

    }

    /**
     * rendering all game objects (and their shots if they have any) - calling their draw() method
     * rendering lives and scores indicators
     * @param g Graphics class instance that is used for rendering
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Font font = new Font("Helvetica", Font.PLAIN, 15);
        g.setColor(Color.WHITE);
        g.setFont(font);

        g.drawString("Lives: " + lives.toString(), BOARD_WIDTH - 90, 20);
        g.drawString("Score: " + score.toString(), 28, 20);

        g.setColor(Color.GREEN);
        g.drawLine(0, GROUND, BOARD_WIDTH, GROUND);

        player.draw(g, this);
        if (player.getShot().isVisible())
            player.getShot().draw(g, this);

        enemyGroup.draw(g, this);

        obstacles.draw(g, this);
    }

    /**
     * refreshes frame
     * checks if there game has already ended and if so calls gameManager.renderAfterGame(int score) method with gained score
     * if players state is set to "dying" then decrement lives variable and revive the player
     * if game still continues then moves with every movable game object
     * resolve collisions between game objects
     */
    public void refreshFrame() {
        if(enemyGroup.getNumberOfEnemies()==0) {
            inGame=false;
            gameManager.renderAfterGame(score);
        }

        if(player.isDying()) {
            lives--;
            if(lives!=0) player.revive();
            else {
                inGame=false;
                gameManager.renderAfterGame(score);
            }
        }

        if(enemyGroup.reachedTheGround()) {
            inGame=false;
            score += ENEMY_REACHES_GROUND_POINTS;
            gameManager.renderAfterGame(score);
        }

        player.move();
        player.shotMove();
        enemyGroup.move();
        resolveCollisions();
    }

    /**
     * resolving collisions
     * - player shot and enemies
     * - enemy shots and player
     * - both player/enemy shot and obstacles
     */
    private void resolveCollisions() {
        collisionPlayerShotEnemies();
        collisionEnemyShotPlayer();
        collisionShotsObstacles();
    }

    /**
     * resolves collision between player's shot and enemies
     * if an enemy is hit then kill both the enemy and the shot, increase points (+1)
     */
    private void collisionPlayerShotEnemies() {
        if(player.getShot().isVisible()) {
            for (Enemy enemy : enemyGroup.getEnemies())
                if(enemy.isVisible() && player.getShot().collisionWith(enemy)) {
                    enemy.explosion();
                    enemyGroup.decreaseNumberOfEnemies();
                    player.getShot().die();
                    score += ENEMY_DIE_POINTS;
                }
        }
    }

    /**
     * resolves collision between enemy's shot and the player
     * if the player is hit then kill both the player and the shot, decrease points (-3)
     */
    private void collisionEnemyShotPlayer() {
        for(Enemy enemy : enemyGroup.getEnemies()) {
            if (enemy.getShot().isVisible() && enemy.getShot().collisionWith(player)) {
                player.explosion();
                enemy.getShot().die();
                score += PLAYER_DIE_POINTS;
            }
        }
    }

    /**
     * resolves collision between both enemy/player shots and the obstacles
     * if an obstacle is hit then kill the shot and corresponding obstacle Block instance
     */
    private void collisionShotsObstacles() {
        for(Obstacle obstacle : obstacles.getObstacles()) {
            obstacle.collisionWith(player.getShot());
            for (Enemy enemy : enemyGroup.getEnemies()) {
                obstacle.collisionWith(enemy.getShot());
            }
        }
    }
}