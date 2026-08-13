package TerraIncognita.entity.npc;

import TerraIncognita.entity.Player;
import TerraIncognita.item.Potion;
import TerraIncognita.quest.Quest;
import TerraIncognita.quest.QuestLog;
import TerraIncognita.quest.QuestObjectiveType;
import TerraIncognita.util.Constants;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * NPC giao nhiệm vụ (quest board sống). Hiện bảng quest, kiểm tra hoàn
 * thành, trả thưởng.
 *
 * MUỐN THÊM/SỬA NHIỆM VỤ: chỉ cần sửa trong {@link #initQuests()} — không
 * cần đụng vào logic bên dưới (giống cách Merchant.initShop() khai báo
 * hàng bán, hoàn toàn tách biệt khỏi logic mua/bán).
 */
public class QuestGiver extends NPC {

    // Toàn bộ quest mà NPC này có thể giao — khai báo 1 lần, dùng chung cho
    // mọi player (state riêng của từng player nằm ở Player.getQuestLog()).
    private final List<Quest> availableQuests;

    public QuestGiver(int tileX, int tileY) {
        super("Quest Giver", tileX, tileY);
        this.availableQuests = new ArrayList<>();
        initQuests();
    }

    /**
     * ==== NƠI DUY NHẤT CẦN SỬA ĐỂ THÊM/ĐỔI NHIỆM VỤ ====
     * Mỗi Quest cần: id duy nhất, tiêu đề, lời thoại giao việc, lời thoại
     * trả thưởng, loại mục tiêu + targetId + số lượng, và phần thưởng.
     */
    private void initQuests() {
        // Quest 1: giết 3 Orc — thưởng vàng + exp
        availableQuests.add(new Quest(
                "kill_orcs_1",
                "Dọn dẹp lũ Orc",
                "Có vài con Orc đang quấy nhiễu khu vực gần đây.\nHãy tiêu diệt 3 con giúp ta.",
                "Tốt lắm! Khu vực đã an toàn hơn rồi.\nĐây là phần thưởng cho ngươi.",
                QuestObjectiveType.KILL_MONSTER,
                "Orc",      // phải khớp Entity.getName() của OrcMonster
                3,
                50,         // thưởng vàng
                30          // thưởng exp
        ));

        // Quest 2: mang 2 Small Health Potion tới nộp — thưởng vàng + 1 item
        availableQuests.add(new Quest(
                "collect_potions_1",
                "Dược liệu khẩn cấp",
                "Ta đang thiếu thuốc hồi máu dự trữ.\nMang cho ta 2 Bình Máu Nhỏ nhé.",
                "Cảm ơn ngươi rất nhiều!\nĐây là chút quà đáp lễ.",
                QuestObjectiveType.COLLECT_ITEM,
                "small_potion", // phải khớp Item.getId() của Potion tương ứng
                2,
                40,
                20
        ).withRewardItem(new Potion("large_potion_reward", "Large Health Potion", 50)));

        // Muốn thêm quest mới: copy 1 khối availableQuests.add(new Quest(...))
        // ở trên rồi đổi nội dung — không cần sửa gì khác trong file này.
    }

    /**
     * Quest tiếp theo NPC có thể mời player nhận (chưa từng nhận, chưa trả
     * thưởng), theo đúng thứ tự khai báo trong initQuests(). Trả null nếu
     * không còn quest nào để mời.
     */
    public Quest getNextOfferableQuest(Player player) {
        QuestLog log = player.getQuestLog();
        for (Quest q : availableQuests) {
            if (!log.isActive(q.getId()) && !log.isTurnedIn(q.getId())) {
                return q;
            }
        }
        return null;
    }

    /**
     * Quest (thuộc NPC này) mà player đang làm và đã đủ điều kiện trả
     * thưởng. Trả null nếu chưa có quest nào sẵn sàng.
     */
    public Quest getQuestReadyToTurnIn(Player player) {
        QuestLog log = player.getQuestLog();
        for (Quest q : availableQuests) {
            if (log.isActive(q.getId())) {
                boolean ready;
                if (q.getObjectiveType() == QuestObjectiveType.COLLECT_ITEM) {
                    ready = log.checkCollectObjective(q.getId(), player.getInventory());
                } else {
                    ready = log.getProgress(q.getId()).isReadyToTurnIn();
                }
                if (ready) return q;
            }
        }
        return null;
    }

    @Override
    public void interact(Player player) {
        // GameEngine điều khiển toàn bộ luồng dialog (giống Merchant): gọi
        // getQuestReadyToTurnIn()/getNextOfferableQuest() để quyết định hiện
        // gì, rồi tự mở DialogBox + xử lý lựa chọn Nhận/Từ chối.
    }

    /**
     * Vùng tương tác — giống Merchant/NPC mặc định (3x3 tile quanh vị trí).
     */
    @Override
    public Rectangle getInteractionBounds() {
        int ts = Constants.TILE_SIZE;
        int x = (int) Math.round(worldX) - ts;
        int y = (int) Math.round(worldY) - ts;
        int w = ts * 3;
        int h = ts * 3;
        return new Rectangle(x, y, w, h);
    }
}
