package TerraIncognita.item;

/**
 * Nguyên liệu — không dùng trực tiếp, chỉ để bán lại cho shop (VD chiến lợi
 * phẩm từ quái, nhặt được trong rương).
 */
public class MaterialItem extends Item {

    public MaterialItem(String id, String name) {
        super(id, name, ItemType.MATERIAL);
        this.stackable = true;
        this.maxStack = 20;
    }

    @Override
    public Item copy() {
        MaterialItem m = new MaterialItem(id, name);
        copyBaseFieldsTo(m);
        return m;
    }
}
