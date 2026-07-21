package TerraIncognita.entity;

import TerraIncognita.economy.LootTable;
import TerraIncognita.item.Item;
import TerraIncognita.item.Potion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ChestTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player();
    }

    @Test
    void open_commonChest_returnsTrueAndMarksOpened() {
        Chest chest = new Chest(0, 0, "common");

        boolean result = chest.open(player);

        assertTrue(result);
        assertTrue(chest.isOpened());
        assertEquals("common", chest.getRarity());
    }

    @Test
    void open_alreadyOpened_returnsFalse() {
        Chest chest = new Chest(0, 0, "common");
        chest.open(player);

        boolean result = chest.open(player);

        assertFalse(result);
    }

    @Test
    void open_rareChest_returnsTrue() {
        Chest chest = new Chest(0, 0, "rare");

        boolean result = chest.open(player);

        assertTrue(result);
        assertTrue(chest.isOpened());
        assertEquals("rare", chest.getRarity());
    }

    @Test
    void open_mythicChest_returnsTrue() {
        Chest chest = new Chest(0, 0, "mythic");

        boolean result = chest.open(player);

        assertTrue(result);
        assertTrue(chest.isOpened());
        assertEquals("mythic", chest.getRarity());
    }

    @Test
    void open_generatesLoot_addsItemToPlayerInventory() {
        Chest chest = new Chest(0, 0, "common");
        Potion loot = new Potion("loot_hp", "Loot Potion", 30);
        chest.setLootTable(new LootTable(List.of(loot), 1.0, new Random(42)));

        boolean result = chest.open(player);

        assertTrue(result);
        Item found = player.getInventory().findById("loot_hp");
        assertNotNull(found);
        assertEquals("Loot Potion", found.getName());
    }

    @Test
    void open_noLootTable_returnsTrueButNoItem() {
        Chest chest = new Chest(0, 0, "common");

        boolean result = chest.open(player);

        assertTrue(result);
        assertNull(chest.getLastLoot());
        assertEquals(0, player.getInventory().getUsedSlots());
    }

    @Test
    void open_dropChanceZero_returnsTrueButNoLoot() {
        Chest chest = new Chest(0, 0, "rare");
        Potion loot = new Potion("loot_hp", "Loot Potion", 30);
        chest.setLootTable(new LootTable(List.of(loot), 0.0, new Random(42)));

        boolean result = chest.open(player);

        assertTrue(result);
        assertNull(chest.getLastLoot());
        assertFalse(player.getInventory().hasItem("loot_hp"));
    }

    @Test
    void getLastLoot_returnsCorrectItemAfterOpen() {
        Chest chest = new Chest(0, 0, "mythic");
        Potion loot = new Potion("loot_hp", "Loot Potion", 30);
        chest.setLootTable(new LootTable(List.of(loot), 1.0, new Random(42)));

        chest.open(player);

        Item lastLoot = chest.getLastLoot();
        assertNotNull(lastLoot);
        assertEquals("loot_hp", lastLoot.getId());
    }
}
