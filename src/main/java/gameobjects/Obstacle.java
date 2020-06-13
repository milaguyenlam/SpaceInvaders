package gameobjects;

import controllers.Board;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import static controllers.Constants.SQUARE_SIZE;

public class Guard {

    private List<Block> blocks;

    public Guard(int x, int y) {
        blocks =new ArrayList<>();
        for(int i=0; i<3; i++) {
            for (int j = 0; j < 5; j++) {
                blocks.add(new Block(x + SQUARE_SIZE * j, y + SQUARE_SIZE * i));
            }
        }
    }

    public void collisionWith(MovingObject obj) {
        for(Block block : blocks) {
            if(block.visible && block.getBoundary().intersects(obj.getBoundary())) {
                block.die();
                obj.die();
            }
        }
    }

    public void draw(Graphics g, Board b) {
        for(Block block : blocks) {
            if(block.visible) block.draw(g, b);
        }
    }

}
