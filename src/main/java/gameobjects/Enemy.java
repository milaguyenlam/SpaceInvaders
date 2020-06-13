package sprites;

import java.util.Random;
import static game.Commons.ENEMY_HEIGHT;
import static game.Commons.ENEMY_WIDTH;

public class Enemy extends MovingObject {

    Bomb bomb;
    boolean almostDied;     //for proper projection of explosion
    private Random rand;

    public Bomb getBomb() {
        return bomb;
    }

    Enemy(int x, int y) {
        super(x, y);
        rand = new Random();
        loadImage("./src/main/resources/enemy.png");
        width=ENEMY_WIDTH;
        height=ENEMY_HEIGHT;
        dx=1;
        almostDied=false;
        bomb=new Bomb(0, 0);
        bomb.die();
    }

    void tryToShoot() {
        int random = rand.nextInt()%400;
        if(random==1 && !this.bomb.visible && this.visible){
            this.bomb.x=this.x+ENEMY_WIDTH/2;
            this.bomb.y=this.y+ENEMY_HEIGHT;
            this.bomb.visible=true;
        }
    }

    void setAlmostDied(boolean almostDied) {
        this.almostDied = almostDied;
    }

    @Override
    public void move() {
        super.move();
    }

}
