package TerraIncognita.map;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.mapeditor.core.MapLayer;
import org.mapeditor.core.TileLayer;
import org.mapeditor.io.TMXMapReader;

import TerraIncognita.util.Constants;

/**
 * Loads a TMX map with libtiled and converts it into the simple map model used
 * by the game.
 */
public class TmxMapLoader {

    private static final String WALL_LAYER_NAME = "wall";

    private final Path mapPath;
    private final List<VisualLayer> visualLayers = new ArrayList<>();

    public TmxMapLoader(String mapName) {
        this.mapPath = Path.of(Constants.MAPS_PATH, mapName + ".tmx");
    }

    public GameMap load() {
        try {
            visualLayers.clear();

            org.mapeditor.core.Map tiledMap = readMap();
            GameMap gameMap = new GameMap(tiledMap.getWidth(), tiledMap.getHeight(), TileType.FLOOR);

            for (MapLayer layer : tiledMap) {
                if (layer instanceof TileLayer tileLayer) {
                    readTileLayer(tileLayer, gameMap);
                }
            }

            return gameMap;
        } catch (Exception e) {
            System.err.println("[TmxMapLoader] Cannot load map " + mapPath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<VisualLayer> getVisualLayers() {
        return visualLayers;
    }

    private void readTileLayer(TileLayer tileLayer, GameMap gameMap) {
        VisualLayer visualLayer = new VisualLayer(
                tileLayer.getName(),
                gameMap.getWidth(),
                gameMap.getHeight(),
                valueOrZero(tileLayer.getOffsetX()),
                valueOrZero(tileLayer.getOffsetY()));

        boolean blocksMovement = WALL_LAYER_NAME.equalsIgnoreCase(tileLayer.getName());
        int width = Math.min(gameMap.getWidth(), tileLayer.getWidth());
        int height = Math.min(gameMap.getHeight(), tileLayer.getHeight());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                org.mapeditor.core.Tile tiledTile = tileLayer.getTileAt(x, y);
                if (tiledTile == null) {
                    continue;
                }

                visualLayer.images[y][x] = tiledTile.getImage();
                if (blocksMovement) {
                    gameMap.setTile(x, y, TileType.WALL);
                }
            }
        }

        visualLayers.add(visualLayer);
    }

    private org.mapeditor.core.Map readMap() throws Exception {
        return new TMXMapReader().readMap(
                mapPath.toAbsolutePath().normalize().toUri().toURL()
        );
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    public static class VisualLayer {
        public final String name;
        public final BufferedImage[][] images;
        public final int offsetXPx;
        public final int offsetYPx;

        VisualLayer(String name, int width, int height, int offsetXPx, int offsetYPx) {
            this.name = name;
            this.images = new BufferedImage[height][width];
            this.offsetXPx = offsetXPx;
            this.offsetYPx = offsetYPx;
        }
    }
}
