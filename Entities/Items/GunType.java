package Entities.Items;

import java.awt.Color;

import Entities.Player.PlayerInterface;

/**
 * Swaps the player's weapon to a different firing pattern.
 *  - TRIPLE_SHOT : fires three projectiles in a small spread
 *  - CHARGE_SHOT : holding fire charges a heavier shot
 */
public class GunType extends AbstractItem {

    public enum Variant {
        TRIPLE_SHOT(PlayerInterface.GunType.TRIPLE_SHOT),
        CHARGE_SHOT(PlayerInterface.GunType.CHARGE_SHOT);

        public final PlayerInterface.GunType playerGun;
        Variant(PlayerInterface.GunType playerGun) { this.playerGun = playerGun; }
    }

    public final Variant variant;

    public GunType(int x, int y, String imagePath, Variant variant) {
        super(x, y, imagePath, ItemType.GunType);
        this.variant = variant;
    }

    @Override
    protected Color placeholderColor() {
        return variant == Variant.TRIPLE_SHOT
            ? new Color(255, 220,  40)   // bright yellow for triple
            : new Color(180,  60, 220);  // purple for charge
    }

    @Override
    public void applyToPlayer(PlayerInterface player) {
        if (!isVisible) return;
        player.setGunType(variant.playerGun);
        isVisible = false;
    }
}
