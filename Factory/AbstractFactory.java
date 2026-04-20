package Factory;
import Entities.EntityInterface;
import Entities.AbstractItem;

public abstract class AbstractFactory implements FactoryInterface {
    @Override
    public EntityInterface createItem(AbstractItem.ItemType itemType, int x, int y) {
        // Implementation to create specific items based on itemType
        return null; // Placeholder
    }

    @Override
    public EntityInterface createRandomItem(int x, int y) {
        // Implementation to create a random item
        return null; // Placeholder
    }

    @Override
    public EntityInterface[] createAllItems(int x, int y) {
        // Implementation to create all items
        return new EntityInterface[0]; // Placeholder
    }
    
}
