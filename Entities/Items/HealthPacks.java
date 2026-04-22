package Entities.Items;

import java.awt.Color;

import Entities.Player.PlayerInterface;

/**
 * Restores player HP. Three sizes for varying heal amounts.
 *
 * (Class kept named "HealthPacks" for backwards compatibility; the
 * single-instance alias {@link HealthPack} can be used in new code.)
 */
public class HealthPacks extends AbstractItem {

    public enum HealthPackType {
        SMALL(15),
        MEDIUM(30),
        LARGE(45);

        public final int healAmount;
        HealthPackType(int healAmount) { this.healAmount = healAmount; }
    }

    public final HealthPackType healthPackType;
    public final int healAmount;

    public HealthPacks(int x, int y, String imagePath, HealthPackType healthPackType) {
        super(x, y, imagePath, ItemType.HealthPack);
        this.healthPackType = healthPackType;
        this.healAmount = healthPackType.healAmount;
    }

    public int getHealAmount() { return healAmount; }

    @Override
    protected Color placeholderColor() {
        // Different shades of red/pink so each size is distinguishable
        switch (healthPackType) {
            case SMALL:  return new Color(255, 150, 150);
            case MEDIUM: return new Color(220,  60,  60);
            case LARGE:  return new Color(140,   0,   0);
            default:     return Color.RED;
        }
    }

    @Override
    public void applyToPlayer(PlayerInterface player) {
        if (!isVisible) return;
        player.heal(healAmount);
        isVisible = false;
    }
}
