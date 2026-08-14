package TerraIncognita.collision;

import TerraIncognita.entity.Player;
import TerraIncognita.map.GameMap;
import TerraIncognita.map.TileType;
import TerraIncognita.util.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollisionManagerTest {

    @Test
    void tileCollisionTreatsNegativePixelPositionsAsOutsideMap() {
        CollisionManager collisionManager = new CollisionManager();
        GameMap map = new GameMap(3, 3, TileType.FLOOR);
        Player player = new Player();

        double barelyInside = -player.getHitbox().x;
        assertFalse(collisionManager.checkTileCollision(player, map, barelyInside, barelyInside),
                "Hitbox whose top-left corner is still at map pixel 0 should be allowed");

        double onePixelPastLeftEdge = barelyInside - 1;
        assertTrue(collisionManager.checkTileCollision(player, map, onePixelPastLeftEdge, barelyInside),
                "Hitbox with negative X must collide with the outside of the map");

        double onePixelPastTopEdge = barelyInside - 1;
        assertTrue(collisionManager.checkTileCollision(player, map, barelyInside, onePixelPastTopEdge),
                "Hitbox with negative Y must collide with the outside of the map");
    }

    @Test
    void resolveMovementStopsAtTopLeftMapBoundary() {
        CollisionManager collisionManager = new CollisionManager();
        GameMap map = new GameMap(3, 3, TileType.FLOOR);
        Player player = new Player();

        player.setWorldX(-player.getHitbox().x);
        player.setWorldY(-player.getHitbox().y);

        double[] resolved = collisionManager.resolveMovement(
                player,
                map,
                -Constants.PLAYER_SPEED,
                -Constants.PLAYER_SPEED);

        assertFalse(resolved[0] < player.getWorldX(), "Player should not move past the left map edge");
        assertFalse(resolved[1] < player.getWorldY(), "Player should not move past the top map edge");
    }
}
