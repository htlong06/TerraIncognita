package TerraIncognita.entity;

import TerraIncognita.economy.LootTable;
import TerraIncognita.item.Item;
import TerraIncognita.item.Key;
import TerraIncognita.item.Potion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChestTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player();
    }

    @Test
    void open_unlockedChest_returnsTrueAndMarksOpened() {
        Chest chest = new Chest(0, 0, false);

        boolean result = chest.open(player);

        assertTrue(result);
        assertTrue(chest.isOpened());
        assertFalse(chest.isLocked());
    }

    @Test
    void open_alreadyOpened_returnsFalse() {
        Chest chest = new Chest(0, 0, false);
        chest.open(player);

        boolean result = chest.open(player);

        assertFalse(result);
    }

    @Test
    void open_lockedWithKey_opensConsumesKeyAndReturnsTrue() {
        Chest chest = new Chest(0, 0, true);
        chest.setRequiredKeyId("chest_key");
        Key key = new Key("chest_key", "Chest Key", "chest_key");
        player.getInventory().addItem(key);

        boolean result = chest.open(player);

        assertTrue(result);
        assertTrue(chest.isOpened());
        assertFalse(chest.isLocked());
        assertFalse(player.getInventory().hasItem("chest_key"));
    }

    @Test
    void open_lockedWithoutKey_returnsFalseAndStaysLocked() {
        Chest chest = new Chest(0, 0, true);
        chest.setRequiredKeyId("chest_key");

        boolean result = chest.open(player);

        assertFalse(result);
        assertTrue(chest.isLocked());
        assertFalse(chest.isOpened());
    }

    @Test
    void open_lockedWithWrongKey_returnsFalseAndStaysLocked() {
        Chest chest = new Chest(0, 0, true);
        chest.setRequiredKeyId("chest_key");
        Key wrongKey = new Key("wrong_key", "Wrong Key", "wrong_key");
        player.getInventory().addItem(wrongKey);

        boolean result = chest.open(player);

        assertFalse(result);
        assertTrue(chest.isLocked());
        assertFalse(chest.isOpened());
        assertTrue(player.getInventory().hasItem("wrong_key"));
    }

    @Test
    void open_generatesLoot_addsItemToPlayerInventory() {
        Chest chest = new Chest(0, 0, false);
        Potion loot = new Potion("loot_hp", "Loot Potion", 30);
        chest.setLootTable(new LootTable(List.of(loot), 1.0));

        boolean result = chest.open(player);

        assertTrue(result);
        Item found = player.getInventory().findById("loot_hp");
        assertNotNull(found);
        assertEquals("Loot Potion", found.getName());
    }
}
