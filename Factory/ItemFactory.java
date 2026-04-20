package Factory;

import Entities.AbstractItem;

public class ItemFactory implements FactoryInterface {
    AbstractItem createItem(Item itemType, int x, int y) {
        switch (itemType) {
            case itemType.HEALTH_PACK:
                return createHealthPack(x, y);
            case itemType.DAMAGE_UP:
                // return createDamageUp(x, y);
            case itemType.MOVEMENT_BOOST:
                // return createMovementBoost(x, y);
            case itemType.GUN_TYPE:
                // return createGunType(x, y);
            default:
                throw new IllegalArgumentException("Unknown item type: " + itemType);
        }
    }

    public 

    
}
