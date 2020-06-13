package sprites;

import java.awt.event.KeyEvent;
import static game.Commons.*;

public class Player extends MovingObject {

    private Missile m;

    public Player (int x, int y) {
        super(x, y);
        loadImage("./src/main/resources/player.png");
        width=PLAYER_WIDTH;
        height=PLAYER_HEIGHT;
        m = new Missile(0, 0);
        m.die();
    }

    public Missile getM() {
        return m;
    }

    public void revive() {
        loadImage("./src/main/resources/player.png");
        setDying(false);
        x=BOARD_WIDTH/2;
    }

    public void missileMove() {
        if(m.isVisible()) {
            m.move();
        }
    }

    public void keyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_LEFT) {
            dx = -PLAYER_SPEED;
        }
        if (keyCode == KeyEvent.VK_RIGHT) {
            dx = PLAYER_SPEED;
        }
        if (keyCode == KeyEvent.VK_SPACE) {
            if(!m.visible) {
                m.visible=true;
                m.x=this.x + PLAYER_WIDTH/2;
                m.y=this.y;
            }
        }
    }

    public void keyReleased(int keyCode) {
        if(keyCode==KeyEvent.VK_LEFT) {
            dx=0;
        }
        if(keyCode==KeyEvent.VK_RIGHT) {
            dx=0;
        }
    }

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
