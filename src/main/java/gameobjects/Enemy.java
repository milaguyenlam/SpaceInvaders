package gameobjects;

import java.io.IOException;
import java.util.Random;

import static gameboards.Constants.*;

/**
 * Enemy Game Object
 *
 */
public class Enemy extends MovingObject {

    EnemyShot enemyShot;
    //for proper projection of explosion
    boolean almostDied;
    //for determining if enemy should shoot or not
    private Random rand;

    /**
     * Shot instance getter
     * @return Shot instance linked with (this) Enemy instance
     */
    public EnemyShot getShot() {
        return enemyShot;
    }

    /**
     * initializes Enemy instance at given coordinates
     * loads "enemy.png" as its sprite
     * width = ENEMY_WIDTH
     * height = ENEMY_HEIGHT
     * dx = 1
     * dy = 0
     * links its EnemyShot instance
     * @param x vertical coordinate
     * @param y horizontal coordinate
     */
    Enemy(int x, int y) {
        super(x, y);
        rand = new Random();
        try {
            loadSprite("enemy.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        width=ENEMY_WIDTH;
        height=ENEMY_HEIGHT;
        dx=1;
        almostDied=false;
        enemyShot =new EnemyShot(0, 0);
        enemyShot.die();
    }

    /**
     * randomly determines if should shoot, if so sets its shot active
     */
    void tryToShoot() {
        int random = rand.nextInt(ENEMY_RANDOM_CONSTANT);
        if(random==1 && !this.enemyShot.visible && this.visible){
            this.enemyShot.x=this.x+ENEMY_WIDTH/2;
            this.enemyShot.y=this.y+ENEMY_HEIGHT;
            this.enemyShot.visible=true;
        }
    }

    /**
     * sets Enemy's state
     * @param almostDied boolean flag if almost dying or not
     */
    void setAlmostDied(boolean almostDied) {
        this.almostDied = almostDied;
    }

}
