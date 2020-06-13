package gameobjects;

import gameboards.Board;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static gameboards.Constants.OBSTACLE_POSX;
import static gameboards.Constants.OBSTACLE_POSY;

public class ObstacleGroup {
    List<Obstacle> obstacles;

    /**
     * initializes 4 Obstacle instances, 125 pixels from each other (vertically)
     */
    public ObstacleGroup() {
        obstacles = new ArrayList<>();
        for(int i=0; i<4 ; i++) {
            obstacles.add(new Obstacle(OBSTACLE_POSX + i * 125, OBSTACLE_POSY));
        }
    }

    /**
     * calls draw() method on every Obstacle instance
     * @param g Graphics class instance that is used for rendering
     * @param b Board to render GameObject's sprite on
     */
    public void draw(Graphics g, Board b) {
        for (Obstacle obstacle : obstacles) {
            obstacle.draw(g, b);
        }
    }

    /**
     * get List<Obstacle> of actual obstacles inside ObstacleGroup
     * @return actual obstacles inside ObstacleGroup
     */
    public List<Obstacle> getObstacles() {
        return obstacles;
    }
}
