package Factory;
import Entities.AbstractItem;
import Entities.EntityInterface;

public interface FactoryInterface {
    EntityInterface createItem(AbstractItem.ItemType itemType, int x, int y);
    EntityInterface createRandomItem(int x, int y);
    EntityInterface[] createAllItems(int x, int y);
}
