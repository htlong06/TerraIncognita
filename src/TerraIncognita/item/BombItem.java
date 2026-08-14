package TerraIncognita.item;

/**
 * Vật phẩm bom mang theo — mua ở shop, tiêu hao khi đặt bom (phím B).
 * Không override use(): đặt bom xử lý trực tiếp ở GameEngine (phím B), không
 * qua thao tác "dùng" trong túi đồ (Enter).
 */
public class BombItem extends Item {

    public BombItem(String id, String name) {
        super(id, name, ItemType.CONSUMABLE);
        this.stackable = true;
        this.maxStack = 10;
    }

    @Override
    public Item copy() {
        BombItem b = new BombItem(id, name);
        copyBaseFieldsTo(b);
        return b;
    }
}
