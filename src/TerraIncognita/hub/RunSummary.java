package TerraIncognita.hub;

/**
 * Dữ liệu tổng kết 1 lượt khám phá dungeon.
 * Hiện ra sau khi player hoàn thành hoặc chết.
 */
public class RunSummary {

    // TODO: Khai báo các trường
    // - int monstersKilled       — số quái đã giết
    // - int goldEarned           — vàng kiếm được
    // - int expEarned            — EXP kiếm được
    // - int floorsCleared        — số tầng đã qua
    // - int itemsCollected       — số item nhặt được
    // - long timePlayed          — thời gian chơi (ms)
    // - boolean survived         — sống sót hay chết

    public RunSummary() {
        // TODO: Khởi tạo tất cả = 0
    }

    // TODO: Phương thức cập nhật
    // public void addMonsterKill() { monstersKilled++; }
    // public void addGold(int amount) { goldEarned += amount; }
    // public void addExp(int amount) { expEarned += amount; }
    // public void addFloorCleared() { floorsCleared++; }

    // TODO: Getter cho tất cả trường

    /**
     * Tính điểm tổng hợp.
     */
    public int calculateScore() {
        // TODO: Công thức tuỳ chỉnh
        // Ví dụ: monstersKilled * 100 + goldEarned * 10 + floorsCleared * 500
        return 0;
    }
}
