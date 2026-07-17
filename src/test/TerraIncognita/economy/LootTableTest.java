package TerraIncognita.economy;

import TerraIncognita.item.Item;
import TerraIncognita.item.Potion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class LootTableTest {

    private Item potion1;
    private Item potion2;

    @BeforeEach
    void setUp() {
        potion1 = new Potion("hp1", "Small Potion", 20);
        potion2 = new Potion("hp2", "Large Potion", 50);
    }

    @Test
    void generateLoot_dropChanceOne_alwaysReturnsItem() {
        LootTable table = new LootTable(List.of(potion1), 1.0, new Random(42));
        for (int i = 0; i < 50; i++) {
            Item loot = table.generateLoot();
            assertNotNull(loot, "dropChance=1.0 should always drop");
        }
    }

    @Test
    void generateLoot_dropChanceZero_neverReturnsItem() {
        LootTable table = new LootTable(List.of(potion1), 0.0, new Random(42));
        for (int i = 0; i < 50; i++) {
            Item loot = table.generateLoot();
            assertNull(loot, "dropChance=0.0 should never drop");
        }
    }

    @Test
    void generateLoot_emptyList_returnsNull() {
        LootTable table = new LootTable(List.of(), 1.0, new Random(42));
        Item loot = table.generateLoot();
        assertNull(loot, "empty possibleItems should return null");
    }

    @Test
    void generateLoot_nullList_returnsNull() {
        LootTable table = new LootTable(null, 1.0, new Random(42));
        Item loot = table.generateLoot();
        assertNull(loot, "null possibleItems should return null, not NPE");
    }

    @Test
    void generateLoot_multipleItems_returnsOneFromList() {
        LootTable table = new LootTable(List.of(potion1, potion2), 1.0, new Random(42));
        Item loot = table.generateLoot();
        assertNotNull(loot);
        assertTrue(loot == potion1 || loot == potion2, "should return one of the possible items");
    }

    @Test
    void generateLoot_deterministicWithSameSeed() {
        LootTable table1 = new LootTable(List.of(potion1, potion2), 1.0, new Random(123));
        LootTable table2 = new LootTable(List.of(potion1, potion2), 1.0, new Random(123));
        Item loot1 = table1.generateLoot();
        Item loot2 = table2.generateLoot();
        assertEquals(loot1, loot2, "same seed should produce same result");
    }

    @Test
    void getDropChance_returnsCorrectValue() {
        LootTable table = new LootTable(List.of(potion1), 0.35);
        assertEquals(0.35, table.getDropChance(), 0.001);
    }

    @Test
    void getPossibleItems_returnsOriginalList() {
        List<Item> items = List.of(potion1, potion2);
        LootTable table = new LootTable(items, 1.0);
        assertEquals(items, table.getPossibleItems());
    }
}
