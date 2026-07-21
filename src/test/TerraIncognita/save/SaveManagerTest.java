package TerraIncognita.save;

import TerraIncognita.entity.Direction;
import TerraIncognita.entity.Player;
import TerraIncognita.item.Equipment;
import TerraIncognita.item.EquipmentSlot;
import TerraIncognita.item.Item;
import TerraIncognita.item.Key;
import TerraIncognita.item.Potion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class SaveManagerTest {

    private static final String DB_PATH = "resources/saves/test_save.db";
    private SaveManager saveManager;

    @BeforeEach
    void setUp() {
        deleteDbFile();
        saveManager = new SaveManager(DB_PATH);
    }

    @AfterEach
    void tearDown() {
        if (saveManager != null) {
            saveManager.close();
        }
        deleteDbFile();
    }

    private void deleteDbFile() {
        File f = new File(DB_PATH);
        if (!f.exists()) {
            return;
        }
        for (int i = 0; i < 5; i++) {
            if (f.delete()) {
                return;
            }
            System.gc();
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Test
    void save_and_load_player_stats() {
        Player original = new Player();
        original.setMaxHp(120);
        original.takeDamage(20);     // hp = 80
        original.setLevel(5);
        original.setExp(50);
        original.setExpToNextLevel(150);
        original.setGold(200);
        original.setAtk(25);
        original.setDef(15);

        assertTrue(saveManager.saveGame("stats", original));

        Player loaded = new Player();
        assertTrue(saveManager.loadGame("stats", loaded));

        assertEquals(120, loaded.getMaxHp());
        assertEquals(80, loaded.getHp());
        assertEquals(5, loaded.getLevel());
        assertEquals(50, loaded.getExp());
        assertEquals(150, loaded.getExpToNextLevel());
        assertEquals(200, loaded.getGold());
        assertEquals(25, loaded.getAtk());
        assertEquals(15, loaded.getDef());
    }

    @Test
    void save_and_load_inventory() {
        Player original = new Player();
        original.getInventory().addItem(new Potion("p1", "Health Potion", 30));
        original.getInventory().addItem(new Equipment("sword1", "Iron Sword", EquipmentSlot.WEAPON, 5, 0));
        original.getInventory().addItem(new Key("k1", "Door Key", "door_lock"));

        assertTrue(saveManager.saveGame("inv", original));

        Player loaded = new Player();
        assertTrue(saveManager.loadGame("inv", loaded));

        assertEquals(3, loaded.getInventory().getUsedSlots());

        Item potion = loaded.getInventory().findById("p1");
        assertInstanceOf(Potion.class, potion);
        assertEquals(30, ((Potion) potion).getHealAmount());

        Item sword = loaded.getInventory().findById("sword1");
        assertInstanceOf(Equipment.class, sword);
        assertEquals(5, ((Equipment) sword).getAtkBonus());

        Item key = loaded.getInventory().findById("k1");
        assertInstanceOf(Key.class, key);
        assertEquals("door_lock", ((Key) key).getKeyId());
    }

    @Test
    void save_and_load_equipment() {
        Player original = new Player();
        Equipment sword = new Equipment("sword1", "Iron Sword", EquipmentSlot.WEAPON, 5, 0);
        original.getInventory().addItem(sword);
        assertTrue(original.equip(sword));

        assertTrue(saveManager.saveGame("equip", original));

        Player loaded = new Player();
        assertTrue(saveManager.loadGame("equip", loaded));

        Equipment equipped = loaded.getEquippedItems().get(EquipmentSlot.WEAPON);
        assertNotNull(equipped);
        assertEquals("sword1", equipped.getId());
        assertEquals(5, equipped.getAtkBonus());
        assertEquals(15, loaded.getAtk());
        assertFalse(loaded.getInventory().hasItem("sword1"));
    }

    @Test
    void save_and_load_position() {
        Player original = new Player();
        original.setWorldX(100);
        original.setWorldY(200);
        original.setDirection(Direction.UP);

        assertTrue(saveManager.saveGame("pos", original));

        Player loaded = new Player();
        assertTrue(saveManager.loadGame("pos", loaded));

        assertEquals(100, loaded.getWorldX());
        assertEquals(200, loaded.getWorldY());
        assertEquals(Direction.UP, loaded.getDirection());
    }

    @Test
    void hasSaveFile_true_after_save() {
        Player player = new Player();
        assertTrue(saveManager.saveGame("exists", player));

        assertTrue(saveManager.hasSaveFile("exists"));
    }

    @Test
    void hasSaveFile_false_before_save() {
        assertFalse(saveManager.hasSaveFile("never"));
    }

    @Test
    void deleteSave_removes_slot() {
        Player player = new Player();
        assertTrue(saveManager.saveGame("todelete", player));
        assertTrue(saveManager.hasSaveFile("todelete"));

        saveManager.deleteSave("todelete");

        assertFalse(saveManager.hasSaveFile("todelete"));
    }

    @Test
    void load_nonexistent_returns_false() {
        Player player = new Player();

        assertFalse(saveManager.loadGame("nonexistent", player));
    }

    @Test
    void save_multiple_slots() {
        Player p1 = new Player();
        p1.setGold(100);
        assertTrue(saveManager.saveGame("slot1", p1));

        Player p2 = new Player();
        p2.setGold(200);
        assertTrue(saveManager.saveGame("slot2", p2));

        Player p3 = new Player();
        p3.setGold(300);
        assertTrue(saveManager.saveGame("slot3", p3));

        java.util.List<String> slots = saveManager.listSaveSlots();
        assertEquals(3, slots.size());

        Player loaded2 = new Player();
        assertTrue(saveManager.loadGame("slot2", loaded2));
        assertEquals(200, loaded2.getGold());

        Player loaded3 = new Player();
        assertTrue(saveManager.loadGame("slot3", loaded3));
        assertEquals(300, loaded3.getGold());
    }

    @Test
    void save_overwrites_existing() {
        Player first = new Player();
        first.setGold(100);
        assertTrue(saveManager.saveGame("dup", first));

        Player second = new Player();
        second.setGold(500);
        assertTrue(saveManager.saveGame("dup", second));

        assertEquals(1, saveManager.listSaveSlots().size());

        Player loaded = new Player();
        assertTrue(saveManager.loadGame("dup", loaded));
        assertEquals(500, loaded.getGold());
    }
}
