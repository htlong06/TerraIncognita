package TerraIncognita.event;

import TerraIncognita.collision.CollisionManager;
import TerraIncognita.entity.Direction;
import TerraIncognita.entity.EntityState;
import TerraIncognita.entity.Player;
import TerraIncognita.entity.monster.SwarmCreature;
import TerraIncognita.graphics.AssetLoader;
import TerraIncognita.map.GameMap;
import TerraIncognita.util.Constants;
import TerraIncognita.util.Vec2;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Sự kiện bầy ếch góc map
 */
public class SwarmEvent {

    public enum EventState {
        DORMANT, // chưa kích hoạt — quái chỉ quanh quẩn trong vùng góc map
        ACTIVE, // đã kích hoạt — quái tự do di chuyển khắp map, đang chờ bị tiêu diệt hết
        COMPLETED // đã tiêu diệt hết — sự kiện kết thúc
    }

    private final List<SwarmCreature> creatures;
    private final Rectangle confineZone; // vùng góc map lúc DORMANT (pixel, không phải tile)
    private final Rectangle mapBounds; // toàn bộ map lúc ACTIVE (pixel)
    private final CollisionManager collisionManager;
    private final Random random;
    private final AssetLoader assetLoader; // dùng để init animation cho mỗi creature

    private EventState state;

    public SwarmEvent(GameMap map, AssetLoader assetLoader) {
        this.creatures = new ArrayList<>();
        this.collisionManager = new CollisionManager();
        this.random = new Random();
        this.state = EventState.DORMANT;
        this.assetLoader = assetLoader;

        int ts = Constants.TILE_SIZE;
        this.confineZone = new Rectangle(
                Constants.SWARM_ZONE_TILE_X * ts,
                Constants.SWARM_ZONE_TILE_Y * ts,
                Constants.SWARM_ZONE_TILE_W * ts,
                Constants.SWARM_ZONE_TILE_H * ts);
        this.mapBounds = new Rectangle(0, 0, map.getWidth() * ts, map.getHeight() * ts);

        spawnCreatures(map);
    }

    /**
     * Rải con quái ngẫu nhiên trong confineZone.
     */
    private void spawnCreatures(GameMap map) {
        for (int i = 0; i < Constants.SWARM_COUNT; i++) {
            int tileX = Constants.SWARM_ZONE_TILE_X;
            int tileY = Constants.SWARM_ZONE_TILE_Y;

            for (int attempt = 0; attempt < 30; attempt++) {
                int tx = Constants.SWARM_ZONE_TILE_X + random.nextInt(Constants.SWARM_ZONE_TILE_W);
                int ty = Constants.SWARM_ZONE_TILE_Y + random.nextInt(Constants.SWARM_ZONE_TILE_H);
                if (map.isWalkable(tx, ty)) {
                    tileX = tx;
                    tileY = ty;
                    break;
                }
            }

            SwarmCreature creature = new SwarmCreature(tileX, tileY);
            creature.initAnimations(assetLoader);
            // Vận tốc ban đầu ngẫu nhiên nhỏ để bầy không đứng yên tuyệt đối lúc mới spawn
            double angle = random.nextDouble() * Math.PI * 2;
            creature.setVelocity(Math.cos(angle) * 10, Math.sin(angle) * 10);
            creatures.add(creature);
        }
    }

    public void update(Player player, GameMap map, double deltaTime) {
        if (state == EventState.COMPLETED)
            return;

        if (state == EventState.DORMANT && confineZone.intersects(player.getHitbox())) {
            state = EventState.ACTIVE;
        }

        if (state == EventState.ACTIVE && !anyAlive()) {
            state = EventState.COMPLETED;
            return;
        }

        Rectangle bounds = (state == EventState.ACTIVE) ? mapBounds : confineZone;

        for (SwarmCreature b : creatures) {
            if (!b.isAlive())
                continue;

            // Vừa bị kiếm/cung "giật mình" (không chết) -> khựng lại 1 nhịp
            if (b.isHurt())
                continue;

            flock(b, player, bounds);
            updateBoid(b, player, map, deltaTime);
        }

        resolveOverlaps(map);
    }

    /**
     * Tổng hợp 5 lực để quyết định hướng đi cho từng con.
     */
    private void flock(SwarmCreature b, Player player, Rectangle bounds) {
        final double W_SEPARATION = 1.5; // Tách bầy
        final double W_ALIGNMENT = 1.0; // Đồng bộ hướng
        final double W_COHESION = 1.0; // Bám theo bầy
        final double W_FLEE = 3.0; // Tránh player
        final double W_CONTAIN = 2.0; // Giữ trong vùng hoạt động

        Vec2 separation = separate(b);
        Vec2 alignment = align(b);
        Vec2 cohesion = cohesion(b);
        Vec2 flee = flee(b, player);
        Vec2 contain = contain(b, bounds);

        separation.mult(W_SEPARATION);
        alignment.mult(W_ALIGNMENT);
        cohesion.mult(W_COHESION);
        flee.mult(W_FLEE);
        contain.mult(W_CONTAIN);

        b.applyForce(separation);
        b.applyForce(alignment);
        b.applyForce(cohesion);
        b.applyForce(flee);
        b.applyForce(contain);
    }

    /**
     * Với mỗi Boid khác trong bán kính desiredSeparation, lấy vector
     * (vị trí mình - vị trí bạn), chuẩn hoá rồi CHIA cho khoảng cách
     * (càng gần thì lực đẩy càng mạnh), cộng dồn rồi lấy trung bình;
     * nếu độ lớn > 0 thì coi đó là "desired velocity", trừ velocity
     * hiện tại và giới hạn bởi maxForce.
     */
    private Vec2 separate(SwarmCreature b) {
        double desiredSeparation = Constants.SWARM_SEPARATION_RADIUS;
        Vec2 steer = new Vec2(0, 0);
        int count = 0;

        for (SwarmCreature other : creatures) {
            if (other == b || !other.isAlive())
                continue;
            double d = distance(b, other);
            if (d > 0 && d < desiredSeparation) {
                Vec2 diff = new Vec2(b.getWorldX() - other.getWorldX(), b.getWorldY() - other.getWorldY());
                diff.normalize();
                diff.div(d);
                steer.add(diff);
                count++;
            }
        }

        if (count > 0) {
            steer.div(count);
        }

        if (steer.mag() > 0) {
            steer.setMag(b.getMaxSpeed());
            steer.sub(b.getVelocity());
            steer.limit(b.getMaxForce());
        }
        return steer;
    }

    /**
     * lấy trung bình vận tốc của các Boid
     * trong bán kính neighborDist, coi đó là "desired velocity" (đưa về độ
     * lớn maxSpeed), rồi steer = desired - velocity, limit maxForce.
     */
    private Vec2 align(SwarmCreature b) {
        double neighborDist = Constants.SWARM_NEIGHBOR_RADIUS;
        Vec2 sum = new Vec2(0, 0);
        int count = 0;

        for (SwarmCreature other : creatures) {
            if (other == b || !other.isAlive())
                continue;
            double d = distance(b, other);
            if (d > 0 && d < neighborDist) {
                sum.add(other.getVelocity());
                count++;
            }
        }

        if (count > 0) {
            sum.div(count);
            sum.setMag(b.getMaxSpeed());
            Vec2 steer = Vec2.sub(sum, b.getVelocity());
            steer.limit(b.getMaxForce());
            return steer;
        }
        return new Vec2(0, 0);
    }

    /**
     * lấy vị trí trung bình ("tâm bầy") của các Boid trong bán kính neighborDist
     * rồi seek() về phía đó.
     */
    private Vec2 cohesion(SwarmCreature b) {
        double neighborDist = Constants.SWARM_NEIGHBOR_RADIUS;
        Vec2 sum = new Vec2(0, 0);
        int count = 0;

        for (SwarmCreature other : creatures) {
            if (other == b || !other.isAlive())
                continue;
            double d = distance(b, other);
            if (d > 0 && d < neighborDist) {
                sum.add(new Vec2(other.getWorldX(), other.getWorldY()));
                count++;
            }
        }

        if (count > 0) {
            sum.div(count);
            return seek(b, sum);
        }
        return new Vec2(0, 0);
    }

    private Vec2 seek(SwarmCreature b, Vec2 target) {
        Vec2 position = new Vec2(b.getWorldX(), b.getWorldY());
        Vec2 desired = Vec2.sub(target, position);
        desired.setMag(b.getMaxSpeed());

        Vec2 steer = Vec2.sub(desired, b.getVelocity());
        steer.limit(b.getMaxForce());
        return steer;
    }

    /**
     * Về bản chất là seek() bị đảo hướng (né ra xa thay vì tiến tới), chỉ
     * kích hoạt khi player ở trong bán kính SWARM_PANIC_RADIUS, và tăng
     * cường độ mạnh theo khoảng cách (càng gần phản ứng càng gấp)
     */
    private Vec2 flee(SwarmCreature b, Player player) {
        Vec2 position = new Vec2(b.getWorldX(), b.getWorldY());
        Vec2 target = new Vec2(player.getWorldX(), player.getWorldY());
        double d = position.dist(target);

        if (d >= Constants.SWARM_PANIC_RADIUS) {
            b.setMaxSpeed(Constants.SWARM_BASE_SPEED);
            return new Vec2(0, 0);
        }

        b.setMaxSpeed(Constants.SWARM_PANIC_SPEED);

        Vec2 desired = Vec2.sub(position, target);
        desired.setMag(b.getMaxSpeed());

        Vec2 steer = Vec2.sub(desired, b.getVelocity());
        double strength = (Constants.SWARM_PANIC_RADIUS - d) / Constants.SWARM_PANIC_RADIUS;
        steer.limit(b.getMaxForce() * (1 + strength * 3));
        return steer;
    }

    /**
     * nếu Boid tới gần mép vùng giới hạn (confineZone lúc DORMANT, cả map lúc
     * ACTIVE)
     * tạo lực đẩy ngược vào trong.
     */
    private Vec2 contain(SwarmCreature b, Rectangle bounds) {
        double margin = 32;
        double px = b.getWorldX();
        double py = b.getWorldY();

        double fx = 0, fy = 0;
        if (px < bounds.x + margin)
            fx = 1;
        else if (px > bounds.x + bounds.width - margin)
            fx = -1;
        if (py < bounds.y + margin)
            fy = 1;
        else if (py > bounds.y + bounds.height - margin)
            fy = -1;

        Vec2 steer = new Vec2(fx, fy);
        if (steer.mag() > 0) {
            steer.setMag(b.getMaxForce());
        }
        return steer;
    }

    /**
     * chuyển gia tốc thành vận tốc, vận tốc thành vị trí và giải va chạm
     */
    private void updateBoid(SwarmCreature b, Player player, GameMap map, double deltaTime) {
        Vec2 velocity = b.getVelocity();
        Vec2 acceleration = b.getAcceleration();

        velocity.x += acceleration.x * deltaTime;
        velocity.y += acceleration.y * deltaTime;
        velocity.limit(b.getMaxSpeed());
        b.setVelocity(velocity.x, velocity.y);

        double dx = velocity.x * deltaTime;
        double dy = velocity.y * deltaTime;
        double[] resolved = collisionManager.resolveMovement(b, map, dx, dy);
        b.setWorldX(resolved[0]);
        b.setWorldY(resolved[1]);
        b.updateTilePosition(Constants.TILE_SIZE);

        // Hướng mặt theo velocity
        if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
            if (Math.abs(velocity.x) > 0.5)
                b.setDirection(velocity.x > 0 ? Direction.RIGHT : Direction.LEFT);
        } else {
            if (Math.abs(velocity.y) > 0.5)
                b.setDirection(velocity.y > 0 ? Direction.DOWN : Direction.UP);
        }

        // Cập nhật EntityState dựa trên EventState của SwarmEvent:
        // DORMANT (quanh quẩn chưa bị kích hoạt) → IDLE (ếch đứng yên)
        // ACTIVE (đã bị kích hoạt, truy đuổi) → WALK (ếch nhảy)
        if (b.getState() != EntityState.HURT) {
            b.setState(state == EventState.ACTIVE ? EntityState.WALK : EntityState.IDLE);
        }

        b.resetAcceleration(); // acceleration.mult(0) — reset mỗi frame, đúng sách gốc
    }

    /**
     * Ép khoảng cách tối thiểu giữa MỌI cặp quái còn sống (nếu 2 con gần
     * nhau hơn SWARM_MIN_SEPARATION_DISTANCE thì đẩy đều mỗi con ra 1 nửa
     * phần chênh lệch, theo đúng hướng nối tâm 2 con). Lặp vài lượt để giải
     * quyết cả các trường hợp chồng lấn dây chuyền (A đè B, B đè C...).
     * Dùng CollisionManager khi đẩy để không đẩy quái lọt vào tường.
     */
    private void resolveOverlaps(GameMap map) {
        double minDist = Constants.SWARM_MIN_SEPARATION_DISTANCE;

        for (int iteration = 0; iteration < 3; iteration++) {
            boolean anyOverlap = false;

            for (int i = 0; i < creatures.size(); i++) {
                SwarmCreature a = creatures.get(i);
                if (!a.isAlive())
                    continue;

                for (int j = i + 1; j < creatures.size(); j++) {
                    SwarmCreature b = creatures.get(j);
                    if (!b.isAlive())
                        continue;

                    double dx = b.getWorldX() - a.getWorldX();
                    double dy = b.getWorldY() - a.getWorldY();
                    double dist = Math.hypot(dx, dy);

                    if (dist >= minDist)
                        continue;
                    anyOverlap = true;

                    double nx, ny;
                    if (dist < 0.001) {
                        double angle = random.nextDouble() * Math.PI * 2;
                        nx = Math.cos(angle);
                        ny = Math.sin(angle);
                        dist = 0.001;
                    } else {
                        nx = dx / dist;
                        ny = dy / dist;
                    }

                    double overlap = minDist - dist;
                    double pushEach = overlap / 2.0;

                    double[] resolvedA = collisionManager.resolveMovement(a, map, -nx * pushEach, -ny * pushEach);
                    a.setWorldX(resolvedA[0]);
                    a.setWorldY(resolvedA[1]);

                    double[] resolvedB = collisionManager.resolveMovement(b, map, nx * pushEach, ny * pushEach);
                    b.setWorldX(resolvedB[0]);
                    b.setWorldY(resolvedB[1]);
                }
            }

            if (!anyOverlap)
                break;
        }

        for (SwarmCreature c : creatures) {
            if (c.isAlive())
                c.updateTilePosition(Constants.TILE_SIZE);
        }
    }

    /**
     * Gọi khi 1 quả bomb nổ (dù trúng quái trong bầy hay không) — mọi con
     * trong bán kính SWARM_EXPLOSION_FLEE_RADIUS quanh tâm nổ bị "hất văng"
     * ra xa NGAY LẬP TỨC (set thẳng vận tốc, không qua applyForce như
     * steering thường, để tạo cảm giác chấn động tức thời của vụ nổ).
     */
    public void notifyExplosion(double explosionCenterX, double explosionCenterY) {
        for (SwarmCreature c : creatures) {
            if (!c.isAlive())
                continue;
            double dx = c.getWorldX() - explosionCenterX;
            double dy = c.getWorldY() - explosionCenterY;
            double dist = Math.hypot(dx, dy);
            if (dist >= Constants.SWARM_EXPLOSION_FLEE_RADIUS)
                continue;

            if (dist < 0.001) {
                double angle = random.nextDouble() * Math.PI * 2;
                dx = Math.cos(angle);
                dy = Math.sin(angle);
                dist = 1;
            }
            double strength = (Constants.SWARM_EXPLOSION_FLEE_RADIUS - dist) / Constants.SWARM_EXPLOSION_FLEE_RADIUS;
            double pushSpeed = Constants.SWARM_EXPLOSION_PUSH_SPEED * strength;
            double nx = dx / dist;
            double ny = dy / dist;
            c.setVelocity(nx * pushSpeed, ny * pushSpeed);
        }
    }

    /**
     * Khoảng cách (pixel) từ player tới con ếch còn sống GẦN NHẤT trong bầy.
     * Dùng cho hiệu ứng âm thanh "lại gần thì nghe tiếng ếch kêu" — không
     * phụ thuộc EventState, vì kể cả lúc DORMANT (chưa kích hoạt) player
     * vẫn có thể nghe tiếng ếch nếu đứng đủ gần vùng confineZone.
     * Trả về Double.MAX_VALUE nếu không còn con nào sống (đã tiêu diệt hết).
     */
    public double distanceToNearestAlive(Player player) {
        double px = player.getWorldX();
        double py = player.getWorldY();
        double minDist = Double.MAX_VALUE;

        for (SwarmCreature c : creatures) {
            if (!c.isAlive())
                continue;
            double dx = c.getWorldX() - px;
            double dy = c.getWorldY() - py;
            double dist = Math.hypot(dx, dy);
            if (dist < minDist) {
                minDist = dist;
            }
        }
        return minDist;
    }

    private boolean anyAlive() {
        for (SwarmCreature c : creatures) {
            if (c.isAlive())
                return true;
        }
        return false;
    }

    private double distance(SwarmCreature a, SwarmCreature b) {
        return Math.hypot(a.getWorldX() - b.getWorldX(), a.getWorldY() - b.getWorldY());
    }

    // --- Getter ---
    public List<SwarmCreature> getCreatures() {
        return creatures;
    }

    public EventState getState() {
        return state;
    }
}
