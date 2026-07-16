package TerraIncognita.hub;

/**
 * Dữ liệu tổng kết 1 lượt khám phá dungeon.
 * Hiện ra sau khi player hoàn thành hoặc chết.
 */
public class RunSummary {

    private int monstersKilled;
    private int goldEarned;
    private int expEarned;
    private int floorsCleared;
    private int itemsCollected;
    private long timePlayed;
    private boolean survived;

    public RunSummary() {
        this.monstersKilled = 0;
        this.goldEarned = 0;
        this.expEarned = 0;
        this.floorsCleared = 0;
        this.itemsCollected = 0;
        this.timePlayed = 0;
        this.survived = false;
    }

    public void addMonsterKill() { monstersKilled++; }
    public void addGold(int amount) { goldEarned += amount; }
    public void addExp(int amount) { expEarned += amount; }
    public void addFloorCleared() { floorsCleared++; }
    public void addItemCollected() { itemsCollected++; }
    public void addTimePlayed(long ms) { timePlayed += ms; }
    public void setSurvived(boolean survived) { this.survived = survived; }

    /**
     * Tính điểm tổng hợp.
     */
    public int calculateScore() {
        return monstersKilled * 100 + goldEarned * 10 + floorsCleared * 500;
    }

    public int getMonstersKilled() { return monstersKilled; }
    public int getGoldEarned() { return goldEarned; }
    public int getExpEarned() { return expEarned; }
    public int getFloorsCleared() { return floorsCleared; }
    public int getItemsCollected() { return itemsCollected; }
    public long getTimePlayed() { return timePlayed; }
    public boolean isSurvived() { return survived; }
}
