package TerraIncognita.save;

import TerraIncognita.entity.Direction;
import TerraIncognita.entity.Player;
import TerraIncognita.item.BombItem;
import TerraIncognita.item.Equipment;
import TerraIncognita.item.EquipmentSlot;
import TerraIncognita.item.Item;
import TerraIncognita.item.MaterialItem;
import TerraIncognita.item.Potion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

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

        assertTrue(saveManager.saveGame("inv", original));

        Player loaded = new Player();
        assertTrue(saveManager.loadGame("inv", loaded));

        assertEquals(2, loaded.getInventory().getUsedSlots());

        Item potion = loaded.getInventory().findById("p1");
        assertInstanceOf(Potion.class, potion);
        assertEquals(30, ((Potion) potion).getHealAmount());

        Item sword = loaded.getInventory().findById("sword1");
        assertInstanceOf(Equipment.class, sword);
        assertEquals(5, ((Equipment) sword).getAtkBonus());
    }

    @Test
    void save_and_load_potion_variants() {
        Player original = new Player();
        original.getInventory().addItem(Potion.createRegen("regen1", "Regen Potion", 5, 10.0));
        original.getInventory().addItem(Potion.createExp("exp1", "Exp Potion", 20));

        assertTrue(saveManager.saveGame("potions", original));

        Player loaded = new Player();
        assertTrue(saveManager.loadGame("potions", loaded));

        Potion regen = (Potion) loaded.getInventory().findById("regen1");
        assertNotNull(regen);
        assertEquals(Potion.Effect.REGEN, regen.getEffect());
        assertEquals(5, regen.getRegenAmount());
        assertEquals(10.0, regen.getRegenDuration());

        Potion exp = (Potion) loaded.getInventory().findById("exp1");
        assertNotNull(exp);
        assertEquals(Potion.Effect.EXP, exp.getEffect());
        assertEquals(20, exp.getExpAmount());
    }

    @Test
    void save_and_load_material_and_bomb() {
        Player original = new Player();
        original.getInventory().addItem(new MaterialItem("gem1", "Green Gem"));
        original.getInventory().addItem(new BombItem("bomb1", "Bomb"));

        assertTrue(saveManager.saveGame("mats", original));

        Player loaded = new Player();
        assertTrue(saveManager.loadGame("mats", loaded));

        assertInstanceOf(MaterialItem.class, loaded.getInventory().findById("gem1"));
        assertInstanceOf(BombItem.class, loaded.getInventory().findById("bomb1"));
    }

    @Test
    void save_and_load_preserves_buy_and_sell_price() {
        Player original = new Player();
        Potion potion = new Potion("hp_price", "Health Potion", 30);
        potion.setBuyPrice(50);
        potion.setSellPrice(25);
        original.getInventory().addItem(potion);

        assertTrue(saveManager.saveGame("prices", original));

        Player loaded = new Player();
        assertTrue(saveManager.loadGame("prices", loaded));

        Item loadedPotion = loaded.getInventory().findById("hp_price");
        assertEquals(50, loadedPotion.getBuyPrice());
        assertEquals(25, loadedPotion.getSellPrice());
    }

    /**
     * DB tạo bởi bản cũ (trước khi có potion_effect/buy_price/sell_price...) không có
     * các cột này — SaveManager phải tự ALTER TABLE thêm cột khi mở, không được để
     * saveGame/loadGame lỗi SQLException âm thầm (trả false) do thiếu cột.
     */
    @Test
    void opens_legacy_db_missing_new_columns_without_error() throws Exception {
        if (saveManager != null) {
            saveManager.close();
        }
        deleteDbFile();

        Class.forName("org.sqlite.JDBC");
        try (Connection legacyConn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH)) {
            try (Statement stmt = legacyConn.createStatement()) {
                stmt.executeUpdate(
                    "CREATE TABLE saves (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "slot_name TEXT NOT NULL UNIQUE," +
                    "created_at TEXT NOT NULL)");
                stmt.executeUpdate(
                    "CREATE TABLE save_inventory (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "save_id INTEGER NOT NULL," +
                    "slot_index INTEGER NOT NULL," +
                    "item_id TEXT NOT NULL," +
                    "item_type TEXT NOT NULL," +
                    "item_name TEXT NOT NULL," +
                    "stack_count INTEGER DEFAULT 1," +
                    "heal_amount INTEGER," +
                    "atk_bonus INTEGER," +
                    "def_bonus INTEGER," +
                    "equipment_slot TEXT)");
            }
        }

        saveManager = new SaveManager(DB_PATH);
        Player original = new Player();
        original.getInventory().addItem(Potion.createExp("exp_legacy", "Exp Potion", 20));

        assertTrue(saveManager.saveGame("legacy", original));

        Player loaded = new Player();
        assertTrue(saveManager.loadGame("legacy", loaded));
        assertNotNull(loaded.getInventory().findById("exp_legacy"));
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
