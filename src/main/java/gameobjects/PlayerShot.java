package gameobjects;

import static controllers.Constants.SHOT_HEIGHT;
import static controllers.Constants.SHOT_SPEED;
import static controllers.Constants.SHOT_WIDTH;

public class Shot extends MovingObject {
    Shot(int x, int y) {
        super(x, y);
        loadImage("./src/main/resources/shot.png");
        width= SHOT_WIDTH;
        height= SHOT_HEIGHT;
        dy=-SHOT_SPEED;
    }

    @Override
    public void move() {
        if(y<=0)
            this.die();
        super.move();
    }

}
