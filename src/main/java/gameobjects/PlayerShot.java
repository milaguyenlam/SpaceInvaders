package gameobjects;

import java.io.IOException;

import static gameboards.Constants.PLAYER_SHOT_HEIGHT;
import static gameboards.Constants.PLAYER_SHOT_SPEED;
import static gameboards.Constants.PLAYER_SHOT_WIDTH;

public class PlayerShot extends MovingObject {
    /**
     * initializes PlayerShot instance at given coordinates
     * loads "playerShot.png" as its sprite
     * width= PLAYER_SHOT_WIDTH
     * height= PLAYER_SHOT_HEIGHT
     * dx = 0
     * dy = PLAYER_SHOT_SPEED
     * @param x vertical coordinate
     * @param y horizontal coordinate
     */
    PlayerShot(int x, int y) {
        super(x, y);
        try {
            loadSprite("playerShot.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        width= PLAYER_SHOT_WIDTH;
        height= PLAYER_SHOT_HEIGHT;
        dy=-PLAYER_SHOT_SPEED;
    }

    /**
     * moves the shot or dies if reached the ceiling
     */
    @Override
    public void move() {
        if(y<=0)
            this.die();
        super.move();
    }

}
