package TerraIncognita.item;

/**
 * Abstract class vật phẩm cơ sở.
 * Các lớp con: Potion, Key, Equipment, Scroll...
 */
public abstract class Item {

    protected String id;                // ID duy nhất
    protected String name;              // tên hiển thị
    protected String description;       // mô tả
    protected ItemType type;            // loại item
    protected String spriteName;        // tên ảnh đại diện
    protected boolean stackable;        // có xếp chồng được không
    protected int stackCount;           // số lượng hiện tại (nếu stackable)
    protected int maxStack;             // số lượng tối đa trong 1 stack
    protected int buyPrice;             // giá mua từ NPC
    protected int sellPrice;            // giá bán cho NPC

    public Item(String id, String name, ItemType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = "";
        this.spriteName = "";
        this.stackable = false;
        this.stackCount = 1;
        this.maxStack = 1;
        this.buyPrice = 0;
        this.sellPrice = 0;
    }

    /**
     * Sử dụng item (cho item dùng 1 lần như Potion).
     * Mặc định không làm gì — lớp con override nếu cần.
     */
    public boolean use() {
        return false;
    }

    // --- Getter / Setter ---
    public String getId() { return id; }
    public String getName() { return name; }
    public ItemType getType() { return type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSpriteName() { return spriteName; }
    public boolean isStackable() { return stackable; }
    public int getStackCount() { return stackCount; }
    public void setStackCount(int count) { this.stackCount = count; }
    public int getMaxStack() { return maxStack; }
    public int getBuyPrice() { return buyPrice; }
    public void setBuyPrice(int buyPrice) { this.buyPrice = buyPrice; }
    public int getSellPrice() { return sellPrice; }
    public void setSellPrice(int sellPrice) { this.sellPrice = sellPrice; }
}
