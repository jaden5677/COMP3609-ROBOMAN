package Entities.Items;

import Entities.EntityInterface;
import Entities.Player.PlayerInterface;

/**
 * Capabilities every collectible item must expose.
 *
 * Items only ever interact with the player through {@link PlayerInterface},
 * never the concrete Player class, so item code can never reach into
 * enemy- or projectile-only methods.
 */
public interface ItemInterface extends EntityInterface {
    /** Apply this item's effect to the player. Should also mark the item consumed. */
    void applyToPlayer(PlayerInterface player);

    /** Top-level item category (HealthPack, DamageUp, MovementUp, GunType). */
    AbstractItem.ItemType getItemType();
}
