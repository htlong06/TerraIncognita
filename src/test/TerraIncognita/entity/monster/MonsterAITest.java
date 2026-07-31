package TerraIncognita.entity.monster;

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
}
