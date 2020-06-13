package gameobjects;

import java.io.IOException;

import static gameboards.Constants.BLOCK_SIZE;

class Block extends GameObject  {
    /**
     * initializes Block instance at given coordinates
     * loads "block.png" as its sprite
     * width and height = BLOCK_SIZE
     * @param x vertical coordinate
     * @param y horizontal coordinate
     */
    Block(int x, int y) {
        super(x, y);
        try {
            loadSprite("block.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        width = BLOCK_SIZE;
        height = BLOCK_SIZE;
    }
}
