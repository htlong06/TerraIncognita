package TerraIncognita.map;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mapeditor.core.MapLayer;
import org.mapeditor.core.TileLayer;
import org.mapeditor.io.TMXMapReader;

import TerraIncognita.util.Constants;

/**
 * Loads a fixed-size TMX map through libtiled.
 *
 * Collision rule: any non-empty tile on the "wall" layer is treated as WALL.
 * Other tile layers are visual only and are rendered in their original order.
 */
public class TmxMapLoader {

    private static final String WALL_LAYER_NAME = "wall";
    private static final Pattern SOURCE_ATTRIBUTE = Pattern.compile("source=\"([^\"]+)\"");

    private final String filePath;
    private final List<VisualLayer> visualLayers = new ArrayList<>();

    public TmxMapLoader(String mapName) {
        this.filePath = Constants.MAPS_PATH + mapName + ".tmx";
    }

    /** Pre-cut tile images for each source layer, in TMX layer order. */
    public List<VisualLayer> getVisualLayers() {
        return visualLayers;
    }

    public static class VisualLayer {
        public final String name;
        public final BufferedImage[][] images; // [y][x], null = empty tile
        public final int offsetXPx, offsetYPx;

        VisualLayer(String name, int width, int height, int offsetXPx, int offsetYPx) {
            this.name = name;
            this.images = new BufferedImage[height][width];
            this.offsetXPx = offsetXPx;
            this.offsetYPx = offsetYPx;
        }
    }

    public GameMap generate(int width, int height, int difficulty) {
        try {
            visualLayers.clear();

            org.mapeditor.core.Map tiledMap = readTiledMap();
            int mapWidth = tiledMap.getWidth();
            int mapHeight = tiledMap.getHeight();

            GameMap gameMap = createEmptyFloorMap(mapWidth, mapHeight);
            for (MapLayer mapLayer : tiledMap) {
                if (mapLayer instanceof TileLayer tileLayer) {
                    parseLayer(tileLayer, gameMap, mapWidth, mapHeight);
                }
            }

            return gameMap;
        } catch (Exception e) {
            System.err.println("[TmxMapLoader] Failed to read TMX map: " + filePath + " (" + e.getMessage() + ")");
            e.printStackTrace();
            return null;
        }
    }

    private org.mapeditor.core.Map readTiledMap() throws Exception {
        Path tmxPath = new File(filePath).toPath().toAbsolutePath().normalize();
        Path tempDir = Files.createTempDirectory("terra-tmx-");
        tempDir.toFile().deleteOnExit();

        Path tempTmxPath = tempDir.resolve(tmxPath.getFileName().toString());
        String normalizedTmx = normalizeSourceAttributes(
                Files.readString(tmxPath, StandardCharsets.UTF_8),
                tmxPath.getParent(),
                tempDir,
                true);
        Files.writeString(tempTmxPath, normalizedTmx, StandardCharsets.UTF_8);
        tempTmxPath.toFile().deleteOnExit();

        return new TMXMapReader().readMap(tempTmxPath.toUri().toURL());
    }

    private String normalizeSourceAttributes(String xml, Path sourceDir, Path tempDir, boolean copyTilesets)
            throws Exception {
        Matcher matcher = SOURCE_ATTRIBUTE.matcher(xml);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            Path sourcePath = sourceDir.resolve(matcher.group(1)).normalize();
            String replacementSource = sourcePath.toUri().toASCIIString();

            if (copyTilesets && matcher.group(1).toLowerCase().endsWith(".tsx")) {
                Path tempTilesetPath = tempDir.resolve(sourcePath.getFileName().toString());
                String normalizedTileset = normalizeSourceAttributes(
                        Files.readString(sourcePath, StandardCharsets.UTF_8),
                        sourcePath.getParent(),
                        tempDir,
                        false);
                Files.writeString(tempTilesetPath, normalizedTileset, StandardCharsets.UTF_8);
                tempTilesetPath.toFile().deleteOnExit();
                replacementSource = tempTilesetPath.toUri().toASCIIString();
            }

            matcher.appendReplacement(result, "source=\"" + Matcher.quoteReplacement(replacementSource) + "\"");
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private GameMap createEmptyFloorMap(int mapWidth, int mapHeight) {
        GameMap map = new GameMap(mapWidth, mapHeight);
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                map.setTile(x, y, new Tile(TileType.FLOOR));
            }
        }
        return map;
    }

    private void parseLayer(TileLayer tileLayer, GameMap gameMap, int mapWidth, int mapHeight) {
        String name = tileLayer.getName();
        boolean isWallLayer = WALL_LAYER_NAME.equalsIgnoreCase(name);
        VisualLayer visual = new VisualLayer(
                name,
                mapWidth,
                mapHeight,
                intOrDefault(tileLayer.getOffsetX(), 0),
                intOrDefault(tileLayer.getOffsetY(), 0));

        int layerWidth = Math.min(mapWidth, tileLayer.getWidth());
        int layerHeight = Math.min(mapHeight, tileLayer.getHeight());
        for (int y = 0; y < layerHeight; y++) {
            for (int x = 0; x < layerWidth; x++) {
                org.mapeditor.core.Tile tiledTile = tileLayer.getTileAt(x, y);
                if (tiledTile == null) {
                    continue;
                }

                if (isWallLayer) {
                    gameMap.setTile(x, y, new Tile(TileType.WALL));
                }

                visual.images[y][x] = tiledTile.getImage();
            }
        }

        visualLayers.add(visual);
    }

    private int intOrDefault(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }
}
