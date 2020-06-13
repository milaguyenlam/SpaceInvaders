package gameobjects;

import java.awt.event.KeyEvent;
import java.io.IOException;

import static gameboards.Constants.*;

public class Player extends MovingObject {

    private PlayerShot s;

    /**
     * initializes Player instance at given coordinates
     * loads "player.png" as its sprite
     * width = PLAYER_WIDTH
     * height = PLAYER_HEIGHT
     * dx = 0
     * dy = 0
     * links its PlayerShot instance
     * @param x vertical coordinate
     * @param y horizontal coordinate
     */
    public Player (int x, int y) {
        super(x, y);
        try {
            loadSprite("player.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        width=PLAYER_WIDTH;
        height=PLAYER_HEIGHT;
        s = new PlayerShot(0, 0);
        s.die();
    }

    /**
     * Shot instance getter
     * @return Shot instance linked with (this) Player instance
     */
    public PlayerShot getShot() {
        return s;
    }

    /**
     * makes player live again
     * reloads players sprite and centers his position
     */
    public void revive() {
        try {
            loadSprite("player.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        setDying(false);
        x=BOARD_WIDTH/2;
    }

    /**
     * move with player's shot
     */
    public void shotMove() {
        if(s.isVisible()) {
            s.move();
        }
    }

    /**
     * move player in corresponding direction if any of arrow keys were pressed
     * shoot if space was pressed
     * @param keyCode
     */
    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_LEFT) {
            dx = -PLAYER_SPEED;
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            dx = PLAYER_SPEED;
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            if(!s.visible) {
                s.visible=true;
                s.x=this.x + PLAYER_WIDTH/2;
                s.y=this.y;
            }
        }
    }

    /**
     * stops player from moving if any of arrow keys were released
     * @param keyCode key that was released
     */
    public void keyReleased(int keyCode) {
        if(keyCode==KeyEvent.VK_LEFT) {
            dx=0;
        }
        if(keyCode==KeyEvent.VK_RIGHT) {
            dx=0;
        }
    }

    /**
     * moves player, doesn't let player move outside the left/right walls
     */
    @Override
    public void move() {
        if(x>BOARD_WIDTH-PLAYER_WIDTH)
            x=BOARD_WIDTH-PLAYER_WIDTH;
        else if(x<0)
            x=0;
        else
            super.move();
    }

}
