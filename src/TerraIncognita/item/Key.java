package TerraIncognita.item;

/**
 * Chìa khóa — dùng để mở cửa khóa hoặc rương khóa.
 */
public class Key extends Item {

    private String keyId;

    public Key(String id, String name, String keyId) {
        super(id, name, ItemType.KEY);
        this.keyId = keyId;
        this.stackable = false;
    }

    /**
     * Kiểm tra key này có khớp với ổ khóa không.
     */
    public boolean matches(String lockId) {
        return keyId.equals(lockId);
    }

    public String getKeyId() { return keyId; }
}
