package TerraIncognita.event;

import TerraIncognita.entity.Player;
import TerraIncognita.map.GameMap;
import TerraIncognita.map.Tile;
import TerraIncognita.map.TileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventSystemTest {
    private EventSystem eventSystem;
    private Player player;
    private GameMap map;

    @BeforeEach
    void setUp() {
        eventSystem = new EventSystem();
        player = new Player();
        map = new GameMap(10, 10);
    }

    @Test
    void trapEvent_reducesPlayerHp() {
        TrapEvent trap = new TrapEvent(20, false);
        int hpBefore = player.getHp();
        trap.execute(player, map);
        assertEquals(hpBefore - 20, player.getHp());
    }

    @Test
    void trapEvent_triggersOnlyOnce() {
        TrapEvent trap = new TrapEvent(20, false);
        int hpBefore = player.getHp();
        trap.execute(player, map);
        trap.execute(player, map);
        assertEquals(hpBefore - 20, player.getHp(), "trap should only damage once");
    }

    @Test
    void eventSystem_checkTileEvent_triggersTrap() {
        map.setTile(5, 5, new Tile(TileType.TRAP));
        TrapEvent trap = new TrapEvent(15, false);
        eventSystem.registerEvent("trap_5_5", trap);

        int hpBefore = player.getHp();
        eventSystem.checkTileEvent(map, player, 5, 5);
        assertEquals(hpBefore - 15, player.getHp());
    }

    @Test
    void eventSystem_checkTileEvent_noTrap_doesNothing() {
        int hpBefore = player.getHp();
        eventSystem.checkTileEvent(map, player, 1, 1);
        assertEquals(hpBefore, player.getHp());
    }
}
