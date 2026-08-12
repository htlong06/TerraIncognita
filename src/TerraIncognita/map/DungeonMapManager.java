package TerraIncognita.map;

import TerraIncognita.entity.Player;
import TerraIncognita.util.Constants;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Owns the active dungeon map: load TMX data, place the player, and render the
 * visual tile layers.
 */
public class DungeonMapManager {

    private final GameMap currentMap;
    private final List<TmxMapLoader.VisualLayer> visualLayers;

    public DungeonMapManager(String mapName) {
        TmxMapLoader mapLoader = new TmxMapLoader(mapName);
        this.currentMap = mapLoader.load();
        this.visualLayers = mapLoader.getVisualLayers();
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }

    public boolean isLoaded() {
        return currentMap != null;
    }

    public void placePlayer(Player player) {
        if (currentMap == null) {
            placePlayerAtScreenCenter(player);
            return;
        }

        player.setWorldX(currentMap.getPlayerStartX() * Constants.TILE_SIZE);
        player.setWorldY(currentMap.getPlayerStartY() * Constants.TILE_SIZE);
        player.updateTilePosition(Constants.TILE_SIZE);
        player.setCurrentMap(currentMap);
    }

    public void renderTiles(Graphics2D g2d) {
        if (currentMap == null) {
            return;
        }

        int tileSize = Constants.TILE_SIZE;
        for (TmxMapLoader.VisualLayer layer : visualLayers) {
            renderLayer(g2d, layer, tileSize);
        }
    }

    private void renderLayer(Graphics2D g2d, TmxMapLoader.VisualLayer layer, int tileSize) {
        for (int y = 0; y < currentMap.getHeight(); y++) {
            for (int x = 0; x < currentMap.getWidth(); x++) {
                BufferedImage image = layer.images[y][x];
                if (image == null) {
                    continue;
                }

                int drawX = x * tileSize + layer.offsetXPx;
                int drawY = y * tileSize + layer.offsetYPx;
                g2d.drawImage(image, drawX, drawY, tileSize, tileSize, null);
            }
        }
    }

    private void placePlayerAtScreenCenter(Player player) {
        player.setWorldX(Constants.SCREEN_WIDTH / 2.0 - Constants.TILE_SIZE / 2.0);
        player.setWorldY(Constants.SCREEN_HEIGHT / 2.0 - Constants.TILE_SIZE / 2.0);
    }
}
