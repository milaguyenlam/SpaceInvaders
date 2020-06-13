package controllers;

/**
 * Score data structure
 * holds player name and his score
 */
public class Score {
    public String name;
    public int score;

    /**
     *
     * @param name player's name
     * @param score player's gained score
     */
    public Score(String name, int score) {
        this.name = name;
        this.score = score;
    }
}
