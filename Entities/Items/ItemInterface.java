package Entities.Items;

import Entities.EntityInterface;
import Entities.Player.PlayerInterface;

public interface ItemInterface extends EntityInterface {

    void applyToPlayer(PlayerInterface player);

    AbstractItem.ItemType getItemType();
}
