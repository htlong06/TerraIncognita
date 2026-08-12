package TerraIncognita.map;

import TerraIncognita.util.Constants;
import java.awt.image.BufferedImage;
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

/**
 * Loads a TMX map with libtiled and converts it into the simple map model used
 * by the game.
 */
public class TmxMapLoader {

    private static final String WALL_LAYER_NAME = "wall";
    private static final Pattern SOURCE_ATTRIBUTE = Pattern.compile("source=\"([^\"]+)\"");

    private final Path mapPath;
    private final List<VisualLayer> visualLayers = new ArrayList<>();

    public TmxMapLoader(String mapName) {
        this.mapPath = Path.of(Constants.MAPS_PATH, mapName + ".tmx");
    }

    public GameMap load() {
        try {
            visualLayers.clear();

            org.mapeditor.core.Map tiledMap = readMapWithSafeAssetPaths();
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

    private org.mapeditor.core.Map readMapWithSafeAssetPaths() throws Exception {
        Path sourceMap = mapPath.toAbsolutePath().normalize();
        Path tempDir = Files.createTempDirectory("terra-tmx-");
        tempDir.toFile().deleteOnExit();

        Path tempMap = tempDir.resolve(sourceMap.getFileName().toString());
        String mapXml = rewriteSourceAttributes(
                Files.readString(sourceMap, StandardCharsets.UTF_8),
                sourceMap.getParent(),
                tempDir,
                true);
        Files.writeString(tempMap, mapXml, StandardCharsets.UTF_8);
        tempMap.toFile().deleteOnExit();

        return new TMXMapReader().readMap(tempMap.toUri().toURL());
    }

    /*
     * libtiled 1.4.2 resolves raw source attributes as URI paths, so filenames
     * containing spaces fail. The game assets keep their original names; this
     * temporary copy only rewrites source attributes to encoded file URIs.
     */
    private String rewriteSourceAttributes(String xml, Path sourceDir, Path tempDir, boolean copyTilesets)
            throws Exception {
        Matcher matcher = SOURCE_ATTRIBUTE.matcher(xml);
        StringBuffer rewrittenXml = new StringBuffer();

        while (matcher.find()) {
            String source = matcher.group(1);
            Path sourcePath = sourceDir.resolve(source).normalize();
            String safeSource = sourcePath.toUri().toASCIIString();

            if (copyTilesets && source.toLowerCase().endsWith(".tsx")) {
                Path tempTileset = tempDir.resolve(sourcePath.getFileName().toString());
                String tilesetXml = rewriteSourceAttributes(
                        Files.readString(sourcePath, StandardCharsets.UTF_8),
                        sourcePath.getParent(),
                        tempDir,
                        false);
                Files.writeString(tempTileset, tilesetXml, StandardCharsets.UTF_8);
                tempTileset.toFile().deleteOnExit();
                safeSource = tempTileset.toUri().toASCIIString();
            }

            matcher.appendReplacement(
                    rewrittenXml,
                    "source=\"" + Matcher.quoteReplacement(safeSource) + "\"");
        }

        matcher.appendTail(rewrittenXml);
        return rewrittenXml.toString();
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
