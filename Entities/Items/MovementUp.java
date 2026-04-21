package Entities.Items;

import java.awt.Color;

import Entities.Player.PlayerInterface;

/**
 * Improves the player's mobility. Comes in two flavours:
 *  - JUMP   : raises jump strength
 *  - SPEED  : raises horizontal speed
 *
 * Both flavours share this single class so they can be stored together
 * in any {@code Collection&lt;ItemInterface&gt;}.
 */
public class MovementUp extends AbstractItem {

    public enum BoostType { JUMP, SPEED }

    public final BoostType boostType;
    public final int amount;

    public MovementUp(int x, int y, String imagePath, BoostType boostType, int amount) {
        super(x, y, imagePath, ItemType.MovementUp);
        this.boostType = boostType;
        this.amount = amount;
    }

    @Override
    protected Color placeholderColor() {
        return boostType == BoostType.JUMP
            ? new Color( 80, 200, 255)   // light blue for jump
            : new Color(120, 255, 120);  // light green for speed
    }

    @Override
    public void applyToPlayer(PlayerInterface player) {
        if (!isVisible) return;
        switch (boostType) {
            case JUMP:  player.boostJump(amount);  break;
            case SPEED: player.boostSpeed(amount); break;
        }
        isVisible = false;
    }
}
