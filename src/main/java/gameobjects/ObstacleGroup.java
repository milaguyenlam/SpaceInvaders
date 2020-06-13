package gameobjects;

import controllers.Board;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static controllers.Constants.GUARD_POSX;
import static controllers.Constants.GUARD_POSY;

public class GuardGroup {
    List<Guard> guards;

    public GuardGroup() {
        guards = new ArrayList<>();
        for(int i=0; i<4 ; i++) {
            guards.add(new Guard(GUARD_POSX + i * 125, GUARD_POSY));
        }
    }

    public void draw(Graphics g, Board b) {
        for (Guard guard : guards) {
            guard.draw(g, b);
        }
    }

    public List<Guard> getGuards() {
        return guards;
    }
}
