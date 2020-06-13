package gameobjects;

import gameboards.Board;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class GameObject {
    private Image image;
    int x;
    int y;
    int width;
    int height;
    boolean dying;
    boolean visible;

    /**
     * initializes GameObject at given coordinates
     * visible and not dying
     * @param x vertical coordinate
     * @param y horizontal coordinate
     */
    GameObject(int x, int y) {
        this.x=x;
        this.y=y;
        visible=true;
        dying=false;
    }

    /**
     * loads sprite of the game object
     * @param imageName
     */
    void loadSprite(String imageName) throws IOException {
        URL spriteURL = this.getClass().getClassLoader().getResource(imageName);
        image = ImageIO.read(spriteURL);
    }

    /**
     * loads explosion picture as a game object sprite
     * and sets dying variable to true
     */
    public void explosion() {
        try {
            loadSprite("explosion.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        setDying(true);
    }

    /**
     * creates Rectangle instance based of GameObject's coordinates, width and height
     * @return Rectangle instance representing GameObject's boundaries
     */
    public Rectangle getBoundary() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * checks if GameObject is colliding with other GameObject
     * @param o GameObject that the collision is checked upon
     * @return boolean flag is collision was detected
     */
    public boolean collisionWith(GameObject o) {
        return this.getBoundary().intersects(o.getBoundary());
    }

    /**
     * draws GameObject's sprite
     * @param g Graphics class instance that is used for rendering
     * @param board Board to render GameObject's sprite on
     */
    public void draw(Graphics g, Board board) {
        g.drawImage(image, x, y, width, height, board);
    }

    /**
     * lets gameobject definitively die (not visible)
     */
    public void die() {
        visible=false;
    }

    /**
     * used for indication if GameObject is still active/alive
     * @return boolean flag if active/alive
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * setting GameObject's state of dying
     * @param b true if dying, false otherwise
     */
    void setDying(boolean b) {
        dying=b;
    }

    /**
     * used for indication if GameObject is dying
     * @return boolean flag if dying
     */
    public boolean isDying() {
        return dying;
    }
}
