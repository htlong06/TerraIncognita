package TerraIncognita.entity.monster;

import TerraIncognita.entity.Direction;
import TerraIncognita.entity.EntityState;
import TerraIncognita.entity.Player;
import TerraIncognita.graphics.AssetLoader;
import TerraIncognita.map.GameMap;
import TerraIncognita.util.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MonsterAITest {

    @Test
    void orcAssetsAreLoaded() {
        AssetLoader loader = new AssetLoader();
        loader.loadAll();

        assertTrue(loader.getFrames("orc_idle").length > 0, "Orc idle sheet phải được load");
        assertTrue(loader.getFrames("orc_walk").length > 0, "Orc walk sheet phải được load");
        assertTrue(loader.getFrames("orc_attack").length > 0, "Orc attack sheet phải được load");
    }

    @Test
    void monsterAiChasesPlayerWhenNearby() {
        Player player = new Player();
        player.setWorldX(5 * Constants.TILE_SIZE);
        player.setWorldY(5 * Constants.TILE_SIZE);
        player.updateTilePosition(Constants.TILE_SIZE);

        OrcMonster monster = new OrcMonster(4, 5);
        monster.setWorldX(4 * Constants.TILE_SIZE);
        monster.setWorldY(5 * Constants.TILE_SIZE);
        monster.updateTilePosition(Constants.TILE_SIZE);

        GameMap map = new GameMap(30, 30);
        monster.updateAI(player, map, 0.016);

        assertTrue(monster.isAggro(), "Quái phải vào trạng thái aggro khi player ở gần");
        assertTrue(monster.getAiState() == MonsterAI.AIState.CHASE || monster.getAiState() == MonsterAI.AIState.ATTACK,
                "Quái phải đuổi theo hoặc tấn công khi phát hiện player");
    }

    @Test
    void monsterAttackAnimationResetsWhenAttackStarts() {
        AssetLoader loader = new AssetLoader();
        loader.loadAll();

        OrcMonster monster = new OrcMonster(1, 1);
        monster.initAnimations(loader);
        monster.setDirection(Direction.RIGHT);
        monster.setState(EntityState.IDLE);
        monster.update(0.016);

        monster.setDirection(Direction.RIGHT);
        monster.setState(EntityState.ATTACK);
        monster.resetAnimationForState(EntityState.ATTACK, monster.getDirection());

        assertNotNull(monster.getCurrentAnimation(), "Animation tấn công của quái phải có sẵn");
        assertEquals(0, monster.getCurrentAnimation().getCurrentFrameIndex(),
                "Animation tấn công phải reset về frame đầu khi bắt đầu tấn công");
    }

    @Test
    void orcMonsterEntersHurtStateAndUsesHurtAnimationWhenDamaged() {
        AssetLoader loader = new AssetLoader();
        loader.loadAll();

        OrcMonster monster = new OrcMonster(1, 1);
        monster.initAnimations(loader);
        monster.setDirection(Direction.RIGHT);
        monster.setState(EntityState.IDLE);

        monster.takeDamage(5);

        assertEquals(EntityState.HURT, monster.getState(), "Orc phải chuyển sang trạng thái HURT khi bị đánh");
        assertNotNull(monster.getCurrentAnimation(), "Orc phải có animation hiện tại sau khi bị đánh");
        assertTrue(monster.getHurtTimer() > 0, "Hurt timer phải được bật khi quái bị đánh");

        monster.update(0.25);

        assertEquals(0.0, monster.getHurtTimer(), "Hurt timer phải về 0 sau thời gian bị đánh");
        assertFalse(monster.isHurt(), "Quái phải hết trạng thái bị khóa sau khi hurt timer kết thúc");
    }

    @Test
    void monsterAttackRangeUsesConfiguredRange() {
        OrcMonster monster = new OrcMonster(3, 3);
        monster.setAttackRange(2);

        assertEquals(2, monster.getAttackRange(), "Tầm đánh của quái phải dùng giá trị cấu hình");
    }
}
