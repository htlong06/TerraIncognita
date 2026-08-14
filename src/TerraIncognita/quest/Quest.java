package TerraIncognita.quest;

import TerraIncognita.item.Item;

public class Quest {

    private final String id;
    private final String title;
    private final String offerDialog;
    private final String turnInDialog;
    private final QuestObjectiveType objectiveType;
    private final String targetId;
    private final int targetAmount;
    private final int rewardGold;
    private final int rewardExp;
    private Item rewardItem;

    public Quest(String id, String title, String offerDialog, String turnInDialog,
                 QuestObjectiveType objectiveType, String targetId, int targetAmount,
                 int rewardGold, int rewardExp) {
        this.id = id;
        this.title = title;
        this.offerDialog = offerDialog;
        this.turnInDialog = turnInDialog;
        this.objectiveType = objectiveType;
        this.targetId = targetId;
        this.targetAmount = targetAmount;
        this.rewardGold = rewardGold;
        this.rewardExp = rewardExp;
        this.rewardItem = null;
    }

    public Quest withRewardItem(Item item) {
        this.rewardItem = item;
        return this;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getOfferDialog() { return offerDialog; }
    public String getTurnInDialog() { return turnInDialog; }
    public QuestObjectiveType getObjectiveType() { return objectiveType; }
    public String getTargetId() { return targetId; }
    public int getTargetAmount() { return targetAmount; }
    public int getRewardGold() { return rewardGold; }
    public int getRewardExp() { return rewardExp; }
    public Item getRewardItem() { return rewardItem; }
}
