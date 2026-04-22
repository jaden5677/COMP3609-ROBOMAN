package Factory;

import java.util.Random;

import Entities.Items.AbstractItem;
import Entities.Items.AbstractItem.ItemType;
import Entities.Items.DamageUp;
import Entities.Items.GunType;
import Entities.Items.HealthPacks;
import Entities.Items.HealthPacks.HealthPackType;
import Entities.Items.MovementUp;

/**
 * Builds items from an {@link ItemType} key. Defaults to a sensible
 * mid-tier variant for each category; callers wanting a specific
 * variant can use the dedicated helpers.
 */
public class ItemFactory extends AbstractFactory<AbstractItem, ItemType> {

    private static final Random RNG = new Random();

    @Override
    public AbstractItem create(ItemType itemType, int x, int y) {
        if (itemType == null) {
            throw new IllegalArgumentException("itemType must not be null");
        }
        switch (itemType) {
            case HealthPack: return createHealthPack(x, y, HealthPackType.MEDIUM);
            case DamageUp:   return createDamageUp(x, y, DamageUp.Tier.MEDIUM);
            case MovementUp: return createMovementUp(x, y, MovementUp.BoostType.SPEED, 2);
            case GunType:    return createGunType(x, y, GunType.Variant.TRIPLE_SHOT);
            default:
                throw new IllegalArgumentException("Unknown item type: " + itemType);
        }
    }

    @Override
    public AbstractItem createRandom(int x, int y) {
        ItemType[] types = ItemType.values();
        return create(types[RNG.nextInt(types.length)], x, y);
    }

    @Override
    public AbstractItem[] createAll(int x, int y) {
        ItemType[] types = ItemType.values();
        AbstractItem[] all = new AbstractItem[types.length];
        for (int i = 0; i < types.length; i++) {
            all[i] = create(types[i], x + i * 40, y);
        }
        return all;
    }

    // ---------------- Variant-specific helpers ----------------

    public HealthPacks createHealthPack(int x, int y, HealthPackType type) {
        return new HealthPacks(x, y, null, type);
    }

    public DamageUp createDamageUp(int x, int y, DamageUp.Tier tier) {
        return new DamageUp(x, y, null, tier);
    }

    public MovementUp createMovementUp(int x, int y, MovementUp.BoostType boostType, int amount) {
        return new MovementUp(x, y, null, boostType, amount);
    }

    public GunType createGunType(int x, int y, GunType.Variant variant) {
        return new GunType(x, y, null, variant);
    }
}
