package sprites;

import game.Board;
import javax.swing.*;
import java.awt.*;

public class MovingObject {

    private Image image;
    int x;
    int y;
    int dx;     //velocity OX
    int dy;     //velocity OY
    int width;
    int height;
    boolean dying;
    boolean visible;

    MovingObject(int x, int y) {
        this.x=x;
        this.y=y;
        visible=true;
        dying=false;
    }

    void loadImage(String imageName) {
        ImageIcon ii = new ImageIcon(imageName);
        image = ii.getImage();
    }

    public void explosion() {
        loadImage("./src/main/resources/explosion.png");
        setDying(true);
    }

    public Rectangle getBoundary() {
        return new Rectangle(x, y, width, height);
    }

    public boolean collisionWith(MovingObject o) {
        return this.getBoundary().intersects(o.getBoundary());
    }

    public void draw(Graphics g, Board board) {
        g.drawImage(image, x, y, width, height, board);
    }

    public void die() {
        visible=false;
    }

    public boolean isVisible() {
        return visible;
    }

    void setDying(boolean b) {
        dying=b;
    }

    public boolean isDying() {
        return dying;
    }

    public void move() {
        x+=dx;
        y+=dy;
    }

}
