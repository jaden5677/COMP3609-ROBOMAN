package Entities.Player;

import Entities.AbstractEntity;

public abstract class AbstractPlayer extends AbstractEntity implements PlayerInterface {

    protected int health;
    protected int maxHealth;
    protected int lives;
    protected int deathsInLevel;
    protected int currentLevel;

    protected AbstractPlayer(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override public int getHealth()        { return health; }
    @Override public int getMaxHealth()     { return maxHealth; }
    @Override public void setMaxHealth(int max) { this.maxHealth = max; }
    @Override public int getLives()         { return lives; }
    @Override public int getDeathsInLevel() { return deathsInLevel; }
    @Override public int getCurrentLevel()  { return currentLevel; }
}
