package gameobjects;

import gameboards.Board;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import static gameboards.Constants.*;

public class EnemyGroup {

    private List<Enemy> enemies;
    private int numberOfEnemies;
    private int enemySpeed;

    /**
     * get List<Enemy> of actual enemies inside EnemyWave
     * @return actual enemies inside EnemyWave
     */
    public List<Enemy> getEnemies() {
        return enemies;
    }

    /**
     * get current number of enemies
     * @return current number of enemies
     */
    public int getNumberOfEnemies() {
        return numberOfEnemies;
    }

    /**
     * decrease number of enemies (only variable that represents that)
     */
    public void decreaseNumberOfEnemies() {
        numberOfEnemies--;
    }

    public EnemyGroup() {
        enemies = new ArrayList<>();
        for(int i=0; i<4; i++) {
            for (int j = 0; j < 8; j++) {
                enemies.add(new Enemy(ENEMY_X + 32 * j, ENEMY_Y + 32 * i));
            }
        }
        numberOfEnemies=NUMBER_OF_ENEMIES;
        enemySpeed=1;
    }

    /**
     * draws every enemy and its shot in EnemyWave
     * @param g Graphics class instance that is used for rendering
     * @param board Board to render GameObject's sprite on
     */
    public void draw(Graphics g, Board board) {
        for (Enemy enemy : enemies) {
            if (enemy.visible)
                enemy.draw(g, board);
            if (enemy.enemyShot.visible)
                enemy.enemyShot.draw(g, board);
        }
    }

    /**
     * move with every enemy and its shots
     * accelerate if needed
     * turn in the opposite direction if hit the side (left, right)
     */
    public void move() {
        fixStatus();
        shotMove();
        shooting();
        accelerateIfNeeded();
        turnAroundIfHitTheWall();
    }

    /**
     * determines if EnemyWave reached the ground
     * @return boolean if wave reached the ground or not
     */
    public boolean reachedTheGround() {
        for(Enemy enemy: enemies) {
            if (enemy.visible && enemy.y + enemy.height > OBSTACLE_POSY) {
                return true;
            }
        }
        return false;
    }

    /**
     * helper method for dealing with every enemy's state (especially for explosion rendering)
     */
    private void fixStatus() {
        for(Enemy enemy : enemies) {
            if(enemy.dying) {
                enemy.setAlmostDied(true);
                enemy.setDying(false);
            }
            else if(enemy.almostDied) {
                enemy.die();
                enemy.setAlmostDied(false);
            }
            else if(enemy.visible)
                enemy.move();
        }
    }

    /**
     * moves every enemy's shot
     */
    private void shotMove() {
        for(Enemy enemy: enemies) {
            if(enemy.enemyShot.visible) {
                enemy.enemyShot.move();
            }
        }
    }

    /**
     * tries to shoot for every enemy in the EnemyWave
     */
    private void shooting() {
        for(Enemy enemy: enemies) {
            enemy.tryToShoot();
        }
    }

    /**
     * if number of enemies reaches a certain point, accelerate
     */
    private void accelerateIfNeeded() {
        boolean b=false;

        if(numberOfEnemies==16) {
            enemySpeed = 2;
            b = true;
        }

        if(numberOfEnemies==8) {
            enemySpeed = 3;
            b = true;
        }

        if(b) {
            for (Enemy enemy : enemies) {
                if (enemy.dx > 0) enemy.dx = enemySpeed;
                else enemy.dx = -enemySpeed;
            }
        }
    }

    /**
     * turns to the opposite direction if hit the wall (left, right)
     */
    private void turnAroundIfHitTheWall() {
        for(Enemy enemy: enemies) {
            if(enemy.x>BOARD_WIDTH-ENEMY_WIDTH) {
                for(Enemy enemyReversed : enemies) {
                    enemyReversed.dx = -enemySpeed;
                    enemyReversed.y += 15;
                }
                return;
            }

            if(enemy.x<0) {
                for(Enemy enemyReversed : enemies) {
                    enemyReversed.dx = enemySpeed;
                    enemyReversed.y += 15;
                }
                return;
            }
        }
    }
}
