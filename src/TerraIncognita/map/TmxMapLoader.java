package TerraIncognita.map;

import TerraIncognita.util.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Load bản đồ từ file .tmx (Tiled Map Editor) — hỗ trợ map "infinite" (dữ
 * liệu layer lưu theo <chunk> rời rạc, toạ độ có thể âm) và nhiều tileset
 * ngoài (.tsx riêng, không nhúng trong .tmx).
 *
 * QUY ƯỚC:
 *  - Va chạm xác định theo LAYER, không theo từng tile: bất kỳ ô nào có GID
 *    khác 0 ở layer tên "wall" (không phân biệt hoa/thường) → không đi qua
 *    được. Mọi layer khác (ground, props, props 2...) chỉ để vẽ trang trí.
 *  - Vị trí bắt đầu player KHÔNG đọc từ file — code Java tự đặt sau khi map
 *    được load xong (theo yêu cầu hiện tại).
 *  - Mỗi <tileset firstgid="X" source="Y.tsx"/> trỏ tới 1 file .tsx cùng thư
 *    mục với file .tmx; bên trong .tsx đó, <image source="..."> trỏ tới ảnh
 *    thật, tính tương đối theo thư mục CHỨA FILE .tsx (không phải .tmx).
 */
public class TmxMapLoader implements MapGenerator {

    // 3 bit cờ lật cao nhất mà Tiled cộng vào GID (ngang/dọc/chéo)
    private static final long FLIP_MASK = 0xE0000000L;
    private static final String WALL_LAYER_NAME = "wall";

    private final String filePath;
    private final List<VisualLayer> visualLayers = new ArrayList<>();

    public TmxMapLoader(String mapName) {
        this.filePath = Constants.MAPS_PATH + mapName + ".tmx";
    }

    /** Ảnh từng layer đã cắt sẵn, theo ĐÚNG thứ tự layer gốc trong .tmx (vẽ theo thứ tự này). */
    public List<VisualLayer> getVisualLayers() {
        return visualLayers;
    }

    public static class VisualLayer {
        public final String name;
        public final BufferedImage[][] images; // [y][x], null = ô trống
        public final int offsetXPx, offsetYPx; // offset riêng của layer (px)

        VisualLayer(String name, int width, int height, int offsetXPx, int offsetYPx) {
            this.name = name;
            this.images = new BufferedImage[height][width];
            this.offsetXPx = offsetXPx;
            this.offsetYPx = offsetYPx;
        }
    }

    private static class TilesetInfo {
        int firstGid, lastGid, tileWidth, tileHeight, columns;
        BufferedImage sheet;
    }

    @Override
    public GameMap generate(int width, int height, int difficulty) {
        try {
            File tmxFile = new File(filePath);
            Path tmxDir = tmxFile.getAbsoluteFile().getParentFile().toPath();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().parse(tmxFile);
            doc.getDocumentElement().normalize();
            Element mapEl = doc.getDocumentElement();

            boolean infinite = "1".equals(mapEl.getAttribute("infinite"));
            int mapAttrWidth = Integer.parseInt(mapEl.getAttribute("width"));
            int mapAttrHeight = Integer.parseInt(mapEl.getAttribute("height"));

            // 1) Parse từng .tsx ngoài để biết ảnh gốc + kích thước/số cột
            List<TilesetInfo> tilesets = new ArrayList<>();
            NodeList tilesetNodes = mapEl.getElementsByTagName("tileset");
            for (int i = 0; i < tilesetNodes.getLength(); i++) {
                Element tsEl = (Element) tilesetNodes.item(i);
                int firstGid = Integer.parseInt(tsEl.getAttribute("firstgid"));
                String source = tsEl.getAttribute("source");
                TilesetInfo info = parseTsx(tmxDir.resolve(source), firstGid);
                if (info != null) tilesets.add(info);
            }
            tilesets.sort(Comparator.comparingInt(t -> t.firstGid));

            System.out.println("[TmxMapLoader] Đã nạp " + tilesets.size() + "/" + tilesetNodes.getLength() + " tileset:");
            for (TilesetInfo ts : tilesets) {
                System.out.println("  - GID " + ts.firstGid + "-" + ts.lastGid
                        + " (" + ts.columns + " cột, tile " + ts.tileWidth + "x" + ts.tileHeight + ")");
            }

            // 2) Tính bounding box thật (map infinite có toạ độ chunk âm)
            NodeList layerNodes = mapEl.getElementsByTagName("layer");
            int[] bounds = computeBounds(layerNodes, infinite, mapAttrWidth, mapAttrHeight);
            int minX = bounds[0], minY = bounds[1], mapWidth = bounds[2], mapHeight = bounds[3];

            GameMap map = new GameMap(mapWidth, mapHeight);
            // GameMap mặc định fill WALL toàn bộ — map kiểu TMX coi mọi ô là
            // FLOOR trừ khi layer "wall" ghi đè lại, nên reset hết về FLOOR trước.
            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < mapWidth; x++) {
                    map.setTile(x, y, new Tile(TileType.FLOOR));
                }
            }

            // 3) Đổ dữ liệu từng layer (theo đúng thứ tự xuất hiện trong .tmx)
            for (int i = 0; i < layerNodes.getLength(); i++) {
                Element layerEl = (Element) layerNodes.item(i);
                parseLayer(layerEl, tilesets, map, minX, minY, mapWidth, mapHeight, infinite);
            }

            return map;
        } catch (Exception e) {
            System.err.println("[TmxMapLoader] Lỗi đọc file .tmx: " + filePath + " (" + e.getMessage() + ")");
            e.printStackTrace();
            return null;
        }
    }

    private int[] computeBounds(NodeList layerNodes, boolean infinite, int mapAttrWidth, int mapAttrHeight) {
        if (!infinite) {
            return new int[]{0, 0, mapAttrWidth, mapAttrHeight};
        }
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (int i = 0; i < layerNodes.getLength(); i++) {
            Element layerEl = (Element) layerNodes.item(i);
            Element dataEl = (Element) layerEl.getElementsByTagName("data").item(0);
            NodeList chunks = dataEl.getElementsByTagName("chunk");
            for (int c = 0; c < chunks.getLength(); c++) {
                Element chunkEl = (Element) chunks.item(c);
                int cx = Integer.parseInt(chunkEl.getAttribute("x"));
                int cy = Integer.parseInt(chunkEl.getAttribute("y"));
                int cw = Integer.parseInt(chunkEl.getAttribute("width"));
                int ch = Integer.parseInt(chunkEl.getAttribute("height"));
                minX = Math.min(minX, cx);
                minY = Math.min(minY, cy);
                maxX = Math.max(maxX, cx + cw);
                maxY = Math.max(maxY, cy + ch);
            }
        }
        if (minX == Integer.MAX_VALUE) {
            return new int[]{0, 0, mapAttrWidth, mapAttrHeight};
        }
        return new int[]{minX, minY, maxX - minX, maxY - minY};
    }

    private void parseLayer(Element layerEl, List<TilesetInfo> tilesets, GameMap map,
                             int minX, int minY, int mapWidth, int mapHeight, boolean infinite) {
        String name = layerEl.getAttribute("name");
        int offsetXPx = parseIntOrDefault(layerEl.getAttribute("offsetx"), 0);
        int offsetYPx = parseIntOrDefault(layerEl.getAttribute("offsety"), 0);

        Element dataEl = (Element) layerEl.getElementsByTagName("data").item(0);
        String encoding = dataEl.getAttribute("encoding");
        if (!"csv".equals(encoding)) {
            System.err.println("[TmxMapLoader] Layer '" + name + "' dùng encoding='" + encoding
                    + "' — chỉ hỗ trợ csv (Map Properties > Tile Layer Format = CSV)");
            return;
        }

        VisualLayer visual = new VisualLayer(name, mapWidth, mapHeight, offsetXPx, offsetYPx);
        boolean isWallLayer = WALL_LAYER_NAME.equalsIgnoreCase(name);

        if (infinite) {
            NodeList chunks = dataEl.getElementsByTagName("chunk");
            for (int c = 0; c < chunks.getLength(); c++) {
                Element chunkEl = (Element) chunks.item(c);
                int cx = Integer.parseInt(chunkEl.getAttribute("x"));
                int cy = Integer.parseInt(chunkEl.getAttribute("y"));
                int cw = Integer.parseInt(chunkEl.getAttribute("width"));
                int ch = Integer.parseInt(chunkEl.getAttribute("height"));
                long[] gids = parseCsv(chunkEl.getTextContent());
                placeGids(gids, cw, ch, cx - minX, cy - minY, tilesets, map, visual, isWallLayer);
            }
        } else {
            long[] gids = parseCsv(dataEl.getTextContent());
            placeGids(gids, mapWidth, mapHeight, 0, 0, tilesets, map, visual, isWallLayer);
        }

        visualLayers.add(visual);
    }

    private void placeGids(long[] gids, int blockWidth, int blockHeight, int originX, int originY,
                            List<TilesetInfo> tilesets, GameMap map, VisualLayer visual, boolean isWallLayer) {
        for (int i = 0; i < gids.length && i < blockWidth * blockHeight; i++) {
            long raw = gids[i];
            if (raw == 0) continue;

            int gid = (int) (raw & ~FLIP_MASK);
            int localX = i % blockWidth;
            int localY = i / blockWidth;
            int mapX = originX + localX;
            int mapY = originY + localY;
            if (mapX < 0 || mapX >= map.getWidth() || mapY < 0 || mapY >= map.getHeight()) continue;

            if (isWallLayer) {
                map.setTile(mapX, mapY, new Tile(TileType.WALL));
            }

            BufferedImage sprite = sliceTile(gid, tilesets);
            if (sprite != null) {
                visual.images[mapY][mapX] = sprite;
            }
        }
    }

    private long[] parseCsv(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return new long[0];
        String[] tokens = trimmed.split("[,\\s]+");
        long[] result = new long[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            result[i] = Long.parseLong(tokens[i].trim());
        }
        return result;
    }

    private BufferedImage sliceTile(int gid, List<TilesetInfo> tilesets) {
        for (TilesetInfo ts : tilesets) {
            if (gid >= ts.firstGid && gid <= ts.lastGid) {
                int localId = gid - ts.firstGid;
                int col = localId % ts.columns;
                int row = localId / ts.columns;
                int px = col * ts.tileWidth;
                int py = row * ts.tileHeight;
                if (px + ts.tileWidth > ts.sheet.getWidth() || py + ts.tileHeight > ts.sheet.getHeight()) {
                    System.err.println("[TmxMapLoader] GID=" + gid + " cắt ra ngoài ảnh tileset");
                    return null;
                }
                return ts.sheet.getSubimage(px, py, ts.tileWidth, ts.tileHeight);
            }
        }
        System.err.println("[TmxMapLoader] GID=" + gid + " không khớp tileset nào đã nạp");
        return null;
    }

    private TilesetInfo parseTsx(Path tsxPath, int firstGid) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            Document doc = factory.newDocumentBuilder().parse(tsxPath.toFile());
            doc.getDocumentElement().normalize();
            Element tsEl = doc.getDocumentElement();

            int tileWidth = Integer.parseInt(tsEl.getAttribute("tilewidth"));
            int tileHeight = Integer.parseInt(tsEl.getAttribute("tileheight"));
            int columns = Integer.parseInt(tsEl.getAttribute("columns"));
            int tileCount = Integer.parseInt(tsEl.getAttribute("tilecount"));

            Element imageEl = (Element) tsEl.getElementsByTagName("image").item(0);
            String imageSource = imageEl.getAttribute("source");
            // Đường dẫn ảnh tính tương đối theo thư mục CHỨA FILE .tsx này
            Path imagePath = tsxPath.getParent().resolve(imageSource).normalize();

            BufferedImage sheet = ImageIO.read(imagePath.toFile());
            if (sheet == null) {
                System.err.println("[TmxMapLoader] Không đọc được ảnh tileset: " + imagePath);
                return null;
            }

            TilesetInfo info = new TilesetInfo();
            info.firstGid = firstGid;
            info.lastGid = firstGid + tileCount - 1;
            info.tileWidth = tileWidth;
            info.tileHeight = tileHeight;
            info.columns = columns;
            info.sheet = sheet;
            return info;
        } catch (Exception e) {
            System.err.println("[TmxMapLoader] Lỗi đọc file .tsx: " + tsxPath + " (" + e.getMessage() + ")");
            return null;
        }
    }

    private int parseIntOrDefault(String s, int def) {
        if (s == null || s.isEmpty()) return def;
        try {
            return (int) Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}