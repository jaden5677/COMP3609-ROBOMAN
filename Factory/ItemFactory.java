package Factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Entities.Items.AbstractItem;
import Entities.Items.AbstractItem.ItemType;
import Entities.Items.DamageUp;
import Entities.Items.GunType;
import Entities.Items.HealthPacks;
import Entities.Items.HealthPacks.HealthPackType;
import Entities.Items.Key;
import Entities.Items.MovementUp;
import MainGame.Level;

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
            case Key:
                throw new IllegalArgumentException(
                    "Key needs a Level reference; use createKey(x, y, level)");
            default:
                throw new IllegalArgumentException("Unknown item type: " + itemType);
        }
    }

    @Override
    public AbstractItem createRandom(int x, int y) {

        ItemType[] simple = { ItemType.HealthPack, ItemType.DamageUp,
                              ItemType.MovementUp, ItemType.GunType };
        return create(simple[RNG.nextInt(simple.length)], x, y);
    }

    @Override
    public AbstractItem[] createAll(int x, int y) {
        ItemType[] all = ItemType.values();
        List<AbstractItem> built = new ArrayList<>();
        for (int i = 0; i < all.length; i++) {
            if (all[i] == ItemType.Key) continue;
            built.add(create(all[i], x + i * 40, y));
        }
        return built.toArray(new AbstractItem[0]);
    }

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

    public Key createKey(int x, int y, Level level) {
        return new Key(x, y, null, level);
    }
}
