package TerraIncognita.save;

import TerraIncognita.entity.Direction;
import TerraIncognita.entity.Player;
import TerraIncognita.item.Equipment;
import TerraIncognita.item.EquipmentSlot;
import TerraIncognita.item.Item;
import TerraIncognita.item.Key;
import TerraIncognita.item.Potion;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Quản lý lưu/tải game bằng SQLite (JDBC).
 * Mỗi slot lưu: player (vị trí, chỉ số, HP, level, exp, gold),
 * inventory (danh sách item), equipped (trang bị đang dùng).
 *
 * Bảng saves là cha; save_player / save_inventory / save_equipped là con,
 * xoá theo CASCADE khi slot bị xoá.
 */
public class SaveManager {

    private final String dbPath;
    private Connection conn;

    public SaveManager(String dbPath) {
        this.dbPath = dbPath;
        initDatabase();
    }

    /**
     * Nạp driver SQLite, mở connection, tạo bảng nếu chưa có.
     */
    private void initDatabase() {
        try {
            File dbFile = new File(dbPath);
            File parent = dbFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("PRAGMA foreign_keys = ON;");
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS saves (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "slot_name TEXT NOT NULL UNIQUE," +
                    "created_at TEXT NOT NULL" +
                    ")");
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS save_player (" +
                    "save_id INTEGER PRIMARY KEY," +
                    "hp INTEGER NOT NULL," +
                    "max_hp INTEGER NOT NULL," +
                    "atk INTEGER NOT NULL," +
                    "def INTEGER NOT NULL," +
                    "level INTEGER NOT NULL," +
                    "exp INTEGER NOT NULL," +
                    "exp_to_next_level INTEGER NOT NULL," +
                    "gold INTEGER NOT NULL," +
                    "world_x REAL NOT NULL," +
                    "world_y REAL NOT NULL," +
                    "direction TEXT NOT NULL," +
                    "FOREIGN KEY (save_id) REFERENCES saves(id) ON DELETE CASCADE" +
                    ")");
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS save_inventory (" +
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
                    "equipment_slot TEXT," +
                    "key_id TEXT," +
                    "FOREIGN KEY (save_id) REFERENCES saves(id) ON DELETE CASCADE" +
                    ")");
                stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS save_equipped (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "save_id INTEGER NOT NULL," +
                    "slot TEXT NOT NULL," +
                    "item_id TEXT NOT NULL," +
                    "item_name TEXT NOT NULL," +
                    "atk_bonus INTEGER NOT NULL," +
                    "def_bonus INTEGER NOT NULL," +
                    "upgrade_level INTEGER DEFAULT 0," +
                    "FOREIGN KEY (save_id) REFERENCES saves(id) ON DELETE CASCADE" +
                    ")");
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Failed to initialize save database: " + dbPath, e);
        }
    }

    /**
     * Lưu game vào slot. Ghi đè nếu slot đã tồn tại.
     * @return true nếu lưu thành công
     */
    public boolean saveGame(String slotName, Player player) {
        try {
            conn.setAutoCommit(false);
            // Xoá slot cũ (CASCADE xoá các bảng con)
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM saves WHERE slot_name = ?")) {
                del.setString(1, slotName);
                del.executeUpdate();
            }
            // Tạo dòng saves mới, lấy id sinh ra
            long saveId;
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO saves (slot_name, created_at) VALUES (?, datetime('now'))",
                    Statement.RETURN_GENERATED_KEYS)) {
                ins.setString(1, slotName);
                ins.executeUpdate();
                try (ResultSet keys = ins.getGeneratedKeys()) {
                    if (!keys.next()) {
                        conn.rollback();
                        return false;
                    }
                    saveId = keys.getLong(1);
                }
            }
            insertPlayer(saveId, player);
            insertInventory(saveId, player);
            insertEquipped(saveId, player);
            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    private void insertPlayer(long saveId, Player player) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO save_player (save_id, hp, max_hp, atk, def, level, exp, " +
                "exp_to_next_level, gold, world_x, world_y, direction) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)")) {
            ps.setLong(1, saveId);
            ps.setInt(2, player.getHp());
            ps.setInt(3, player.getMaxHp());
            ps.setInt(4, player.getAtk());
            ps.setInt(5, player.getDef());
            ps.setInt(6, player.getLevel());
            ps.setInt(7, player.getExp());
            ps.setInt(8, player.getExpToNextLevel());
            ps.setInt(9, player.getGold());
            ps.setDouble(10, player.getWorldX());
            ps.setDouble(11, player.getWorldY());
            ps.setString(12, player.getDirection().name());
            ps.executeUpdate();
        }
    }

    private void insertInventory(long saveId, Player player) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO save_inventory (save_id, slot_index, item_id, item_type, item_name, " +
                "stack_count, heal_amount, atk_bonus, def_bonus, equipment_slot, key_id) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)")) {
            int index = 0;
            for (Item item : player.getInventory().getItems()) {
                ps.setLong(1, saveId);
                ps.setInt(2, index++);
                ps.setString(3, item.getId());
                ps.setString(4, item.getType().name());
                ps.setString(5, item.getName());
                ps.setInt(6, item.getStackCount());
                if (item instanceof Potion) {
                    ps.setInt(7, ((Potion) item).getHealAmount());
                } else {
                    ps.setNull(7, Types.INTEGER);
                }
                if (item instanceof Equipment) {
                    Equipment eq = (Equipment) item;
                    ps.setInt(8, eq.getAtkBonus());
                    ps.setInt(9, eq.getDefBonus());
                    ps.setString(10, eq.getSlot().name());
                } else {
                    ps.setNull(8, Types.INTEGER);
                    ps.setNull(9, Types.INTEGER);
                    ps.setNull(10, Types.VARCHAR);
                }
                if (item instanceof Key) {
                    ps.setString(11, ((Key) item).getKeyId());
                } else {
                    ps.setNull(11, Types.VARCHAR);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void insertEquipped(long saveId, Player player) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO save_equipped (save_id, slot, item_id, item_name, atk_bonus, " +
                "def_bonus, upgrade_level) VALUES (?,?,?,?,?,?,?)")) {
            for (Map.Entry<EquipmentSlot, Equipment> entry : player.getEquippedItems().entrySet()) {
                Equipment eq = entry.getValue();
                ps.setLong(1, saveId);
                ps.setString(2, entry.getKey().name());
                ps.setString(3, eq.getId());
                ps.setString(4, eq.getName());
                ps.setInt(5, eq.getAtkBonus());
                ps.setInt(6, eq.getDefBonus());
                ps.setInt(7, eq.getUpgradeLevel());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Tải game từ slot, khôi phục trạng thái vào player.
     * @return true nếu tải thành công, false nếu slot không tồn tại
     */
    public boolean loadGame(String slotName, Player player) {
        int savedAtk = 0;
        int savedDef = 0;
        try {
            long saveId;
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM saves WHERE slot_name = ?")) {
                ps.setString(1, slotName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    saveId = rs.getLong("id");
                }
            }
            // Khôi phục chỉ số + vị trí (atk/def để cuối cùng, sau khi trang bị)
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM save_player WHERE save_id = ?")) {
                ps.setLong(1, saveId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    savedAtk = rs.getInt("atk");
                    savedDef = rs.getInt("def");
                    player.setMaxHp(rs.getInt("max_hp"));
                    player.setLevel(rs.getInt("level"));
                    player.setExp(rs.getInt("exp"));
                    player.setExpToNextLevel(rs.getInt("exp_to_next_level"));
                    player.setGold(rs.getInt("gold"));
                    player.setWorldX(rs.getDouble("world_x"));
                    player.setWorldY(rs.getDouble("world_y"));
                    player.setDirection(Direction.valueOf(rs.getString("direction")));
                    restoreHp(player, rs.getInt("hp"));
                }
            }
            // Xoá inventory hiện tại rồi nạp lại
            for (Item it : new ArrayList<>(player.getInventory().getItems())) {
                player.getInventory().removeItem(it);
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM save_inventory WHERE save_id = ? ORDER BY slot_index")) {
                ps.setLong(1, saveId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Item item = createItemFromInventory(rs);
                        if (item != null) {
                            player.getInventory().addItem(item);
                        }
                    }
                }
            }
            // Xoá trang bị hiện tại rồi nạp lại qua equip()
            player.getEquippedItems().clear();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM save_equipped WHERE save_id = ?")) {
                ps.setLong(1, saveId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Equipment eq = createEquipmentFromRow(rs);
                        if (eq != null) {
                            player.getInventory().addItem(eq);
                            player.equip(eq);
                        }
                    }
                }
            }
            // atk/def đã bao gồm bonus trang bị → ghi đè sau khi equip
            player.setAtk(savedAtk);
            player.setDef(savedDef);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Đưa HP của player về giá trị mục tiêu dùng heal/takeDamage.
     */
    private void restoreHp(Player player, int targetHp) {
        player.heal(player.getMaxHp());
        int current = player.getHp();
        if (current > targetHp) {
            player.takeDamage(current - targetHp);
        } else if (current < targetHp) {
            player.heal(targetHp - current);
        }
    }

    private Item createItemFromInventory(ResultSet rs) throws SQLException {
        return createItem(
            rs.getString("item_type"),
            rs.getString("item_id"),
            rs.getString("item_name"),
            rs.getInt("stack_count"),
            rs.getInt("heal_amount"),
            rs.getInt("atk_bonus"),
            rs.getInt("def_bonus"),
            rs.getString("equipment_slot"),
            rs.getString("key_id"));
    }

    private Item createItem(String itemType, String itemId, String itemName, int stackCount,
                            int healAmount, int atkBonus, int defBonus, String equipmentSlot, String keyId) {
        switch (itemType) {
            case "POTION":
                Potion potion = new Potion(itemId, itemName, healAmount);
                potion.setStackCount(stackCount);
                return potion;
            case "WEAPON":
            case "ARMOR":
                Equipment eq = new Equipment(itemId, itemName, EquipmentSlot.valueOf(equipmentSlot),
                                             atkBonus, defBonus);
                eq.setStackCount(stackCount);
                return eq;
            case "KEY":
                Key key = new Key(itemId, itemName, keyId);
                key.setStackCount(stackCount);
                return key;
            default:
                return null;
        }
    }

    private Equipment createEquipmentFromRow(ResultSet rs) throws SQLException {
        Equipment eq = new Equipment(
            rs.getString("item_id"),
            rs.getString("item_name"),
            EquipmentSlot.valueOf(rs.getString("slot")),
            rs.getInt("atk_bonus"),
            rs.getInt("def_bonus"));
        return eq;
    }

    /**
     * Kiểm tra có save file tồn tại không.
     */
    public boolean hasSaveFile(String slotName) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM saves WHERE slot_name = ?")) {
            ps.setString(1, slotName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    /**
     * Xoá save file (CASCADE xoá các bảng con).
     */
    public void deleteSave(String slotName) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM saves WHERE slot_name = ?")) {
            ps.setString(1, slotName);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    /**
     * Liệt kê các slot đã lưu (mới nhất trước).
     */
    public List<String> listSaveSlots() {
        List<String> slots = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT slot_name FROM saves ORDER BY created_at DESC")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    slots.add(rs.getString("slot_name"));
                }
            }
        } catch (SQLException ignored) {
        }
        return slots;
    }

    /**
     * Đóng kết nối DB.
     */
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
