package TerraIncognita.inventory;

import TerraIncognita.entity.Player;
import TerraIncognita.item.Equipment;
import TerraIncognita.item.EquipmentSlot;
import TerraIncognita.item.Item;
import TerraIncognita.item.ItemType;
import TerraIncognita.item.Potion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory(5);
    }

    @Test
    void addItem_nonStackableEquipment_isAddedAndReturnsTrue() {
        Equipment sword = new Equipment("sword1", "Iron Sword", EquipmentSlot.WEAPON, 5, 0);

        boolean result = inventory.addItem(sword);
        List<Item> items = inventory.getItems();

        assertTrue(result);
        assertEquals(1, items.size());
        assertSame(sword, items.get(0));
        assertEquals("sword1", items.get(0).getId());
    }

    @Test
    void addItem_stackablePotionWithExistingStack_increasesStackCountNoNewSlot() {
        Potion potion = new Potion("hp1", "Health Potion", 30);
        potion.setStackCount(1);
        inventory.addItem(potion);

        Potion another = new Potion("hp1", "Health Potion", 30);
        another.setStackCount(1);
        boolean result = inventory.addItem(another);

        assertTrue(result);
        assertEquals(1, inventory.getUsedSlots());
        Item stacked = inventory.findById("hp1");
        assertNotNull(stacked);
        assertEquals(2, stacked.getStackCount());
    }

    @Test
    void addItem_whenInventoryFull_returnsFalse() {
        Inventory small = new Inventory(1);
        Equipment first = new Equipment("e1", "Equip One", EquipmentSlot.ARMOR, 0, 3);
        Equipment second = new Equipment("e2", "Equip Two", EquipmentSlot.WEAPON, 2, 0);

        assertTrue(small.addItem(first));
        assertTrue(small.isFull());
        assertFalse(small.addItem(second));
        assertEquals(1, small.getUsedSlots());
    }

    @Test
    void removeItem_existingItem_returnsTrueAndRemovesIt() {
        Equipment sword = new Equipment("sword1", "Iron Sword", EquipmentSlot.WEAPON, 5, 0);
        inventory.addItem(sword);

        boolean result = inventory.removeItem(sword);

        assertTrue(result);
        assertFalse(inventory.hasItem("sword1"));
        assertEquals(0, inventory.getUsedSlots());
    }

    @Test
    void removeItem_nonExistingItem_returnsFalse() {
        Equipment sword = new Equipment("sword1", "Iron Sword", EquipmentSlot.WEAPON, 5, 0);
        Equipment other = new Equipment("other", "Other", EquipmentSlot.ARMOR, 0, 1);

        inventory.addItem(sword);
        boolean result = inventory.removeItem(other);

        assertFalse(result);
        assertTrue(inventory.hasItem("sword1"));
    }

    @Test
    void findById_existingId_returnsItem() {
        Equipment sword = new Equipment("sword1", "Iron Sword", EquipmentSlot.WEAPON, 5, 0);
        inventory.addItem(sword);

        Item found = inventory.findById("sword1");

        assertNotNull(found);
        assertSame(sword, found);
        assertEquals("Iron Sword", found.getName());
    }

    @Test
    void findById_nonExistingId_returnsNull() {
        Item found = inventory.findById("does-not-exist");
        assertNull(found);
    }

    @Test
    void hasItem_existingAndNonExisting() {
        inventory.addItem(new Potion("hp1", "Health Potion", 30));

        assertTrue(inventory.hasItem("hp1"));
        assertFalse(inventory.hasItem("nope"));
    }

    @Test
    void isFull_whenUsedSlotsEqualsMax_returnsTrue() {
        Inventory exact = new Inventory(2);
        exact.addItem(new Equipment("a", "A", EquipmentSlot.WEAPON, 1, 0));
        assertFalse(exact.isFull());
        exact.addItem(new Equipment("b", "B", EquipmentSlot.ARMOR, 0, 1));
        assertTrue(exact.isFull());
        assertEquals(2, exact.getUsedSlots());
        assertEquals(2, exact.getMaxSlots());
    }

    @Test
    void useItem_potion_healsPlayerDecrementsStackAndRemovesWhenEmpty() {
        Player player = new Player();
        player.takeDamage(40);
        int hpBefore = player.getHp();

        Potion potion = new Potion("hp1", "Health Potion", 30);
        potion.setStackCount(1);
        inventory.addItem(potion);

        int slotsBefore = inventory.getUsedSlots();
        boolean used = inventory.useItem(0, player);

        assertTrue(used);
        assertEquals(hpBefore + 30, player.getHp());
        assertEquals(0, potion.getStackCount());
        assertEquals(slotsBefore - 1, inventory.getUsedSlots());
        assertFalse(inventory.hasItem("hp1"));
    }

    @Test
    void useItem_nonConsumableEquipment_returnsFalseAndNoHpChange() {
        Player player = new Player();
        player.takeDamage(20);
        int hpBefore = player.getHp();

        Equipment sword = new Equipment("sword1", "Iron Sword", EquipmentSlot.WEAPON, 5, 0);
        inventory.addItem(sword);

        boolean used = inventory.useItem(0, player);

        assertFalse(used);
        assertEquals(hpBefore, player.getHp());
        assertTrue(inventory.hasItem("sword1"));
    }

    @Test
    void useItem_invalidIndex_returnsFalse() {
        Player player = new Player();
        inventory.addItem(new Potion("hp1", "Health Potion", 30));

        assertFalse(inventory.useItem(-1, player));
        assertFalse(inventory.useItem(99, player));
        assertFalse(inventory.useItem(inventory.getUsedSlots(), player));
        assertEquals(1, inventory.getUsedSlots());
    }
}
