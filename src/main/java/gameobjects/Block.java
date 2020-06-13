package gameobjects;

import controllers.Commons;

import java.awt.*;
import static controllers.Commons.SQUARE_SIZE;

class Square extends GameObject  {
    Square(int x, int y) {
        super(x, y);
        loadImage("./src/main/resources/block.png");
        width = SQUARE_SIZE;
        height = SQUARE_SIZE;
    }

}
