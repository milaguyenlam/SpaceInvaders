package controllers;

import gameobjects.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static controllers.Constants.*;

public class Board extends JPanel implements Runnable {
    private GameController gameController;
    private Player player;
    private EnemyGroup enemyGroup;
    private ObstacleGroup guards;

    private Integer score;

    private boolean inGame;
    private Integer lives;

    private InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    private ActionMap actionMap = this.getActionMap();


    public Board(GameController gameController) {
        this.gameController = gameController;
        setFocusable(true);
        setBackground(Color.BLACK);
        initializeBoard();
        registerKeys();
    }

    private void initializeBoard() {
        inGame=true;
        lives=3;
        player=new Player(START_X, START_Y);
        enemyGroup = new EnemyGroup();
        guards = new ObstacleGroup();
        score = 0;
    }

    private void registerKeys() {
        addPlayerKeyBindings(KeyEvent.VK_LEFT, "left");
        addPlayerKeyBindings(KeyEvent.VK_RIGHT, "right");
        addPlayerKeyBindings(KeyEvent.VK_SPACE, "shoot");
        addCustomKeyBindings(KeyEvent.VK_ESCAPE, "back", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                inGame = false;
                gameController.getBackToMainMenu();
            }
        });
    }

    private void addCustomKeyBindings(int keyCode, String id, AbstractAction action) {
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, false), id);
        actionMap.put(id, action);
    }

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

    @Override
    public void addNotify() {
        super.addNotify();
        Thread animator = new Thread(this);
        animator.start();
    }

    @Override
    public void run() {

        long beforeTime, timeDiff, sleep;

        beforeTime = System.currentTimeMillis();

        while(inGame) {
            repaint();
            renderFrame();

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

        guards.draw(g, this);
    }

    public void renderFrame() {
        if(enemyGroup.getNumberOfEnemies()==0) {
            inGame=false;
            gameController.renderAfterGame(score);
        }

        if(player.isDying()) {
            lives--;
            if(lives!=0) player.revive();
            else {
                inGame=false;
                gameController.renderAfterGame(score);
            }
        }

        if(enemyGroup.reachedTheGround()) {
            inGame=false;
            score += ENEMY_REACHES_GROUND_POINTS;
            gameController.renderAfterGame(score);
        }

        player.move();
        player.shotMove();
        enemyGroup.move();
        resolveCollisions();
    }

    private void resolveCollisions() {
        collisionMissileEnemies();
        collisionBombPlayer();
        collisionWithGuards();
    }

    private void collisionMissileEnemies() {
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

    private void collisionBombPlayer() {
        for(Enemy enemy : enemyGroup.getEnemies()) {
            if (enemy.getShot().isVisible() && enemy.getShot().collisionWith(player)) {
                player.explosion();
                enemy.getShot().die();
                score += PLAYER_DIE_POINTS;
            }
        }
    }

    private void collisionWithGuards() {
        for(Obstacle obstacle : guards.getObstacles()) {
            obstacle.collisionWith(player.getShot());
            for (Enemy enemy : enemyGroup.getEnemies()) {
                obstacle.collisionWith(enemy.getShot());
            }
        }
    }
}