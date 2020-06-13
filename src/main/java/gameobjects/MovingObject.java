package gameobjects;

public class MovingObject extends GameObject {
    int dx;     //velocity OX
    int dy;     //velocity OY

    /**
     * initializes MovingObject at given coordinates
     * @param x vertical coordinate
     * @param y horizontal coordinate
     */
    MovingObject(int x, int y) {
        super(x,y);
    }

    /**
     * moves in direction (saved in dx, dy variables)
     */
    public void move() {
        x+=dx;
        y+=dy;
    }

}
