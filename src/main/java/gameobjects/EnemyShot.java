package gameobjects;

import java.io.IOException;

import static gameboards.Constants.*;

public class EnemyShot extends MovingObject {

    /**
     * initializes EnemyShot instance at given coordinates
     * loads "enemyShot.png" as its sprite
     * width= ENEMY_SHOT_WIDTH
     * height= ENEMY_SHOT_HEIGHT
     * dx = 0
     * dy = ENEMY_SHOT_SPEED
     * @param x vertical coordinate
     * @param y horizontal coordinate
     */
    EnemyShot(int x, int y) {
        super(x, y);
        try {
            loadSprite("enemyShot.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        width= ENEMY_SHOT_WIDTH;
        height= ENEMY_SHOT_HEIGHT;
        dy= ENEMY_SHOT_SPEED;
    }

    /**
     * moves the shot or dies if reached the ground
     */
    @Override
    public void move() {
        if(y>GROUND-ENEMY_SHOT_HEIGHT)
            this.die();
        super.move();
    }
}
