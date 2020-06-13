package gameobjects;

import gameboards.Board;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import static gameboards.Constants.BLOCK_SIZE;

public class Obstacle {

    private List<Block> blocks;

    /**
     * initializes and Obstacle with is essentially just a list of Block instances
     * @param x vertical coordinate
     * @param y horizontal coordinate
     */
    public Obstacle(int x, int y) {
        blocks =new ArrayList<>();
        for(int i=0; i<3; i++) {
            for (int j = 0; j < 5; j++) {
                blocks.add(new Block(x + BLOCK_SIZE * j, y + BLOCK_SIZE * i));
            }
        }
    }

    /**
     * checks if Obstacle is colliding with other GameObject and if so, kills the Block that was hit
     * @param obj GameObject that the collision is checked upon
     * @return boolean flag is collision was detected
     */
    public void collisionWith(MovingObject obj) {
        for(Block block : blocks) {
            if(block.visible && block.getBoundary().intersects(obj.getBoundary())) {
                block.die();
                obj.die();
            }
        }
    }

    /**
     * calls draw() method on every Block instance thats still active
     * @param g Graphics class instance that is used for rendering
     * @param b Board to render GameObject's sprite on
     */
    public void draw(Graphics g, Board b) {
        for(Block block : blocks) {
            if(block.visible) block.draw(g, b);
        }
    }

}
