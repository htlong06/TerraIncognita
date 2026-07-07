# Sơ đồ lớp — Terra Incognita

## Class Diagram tổng quan

```mermaid
classDiagram
    direction TB

    %% ===== CORE =====
    class Main {
        +main(String[] args)
    }

    class GamePanel {
        -Thread gameThread
        -GameEngine gameEngine
        -InputHandler inputHandler
        -boolean running
        +startGameThread()
        +run()
        +update(double deltaTime)
        #paintComponent(Graphics g)
    }

    class GameEngine {
        -GameState currentState
        -Player player
        -GameMap currentMap
        -Renderer renderer
        -CombatSystem combatSystem
        -EventSystem eventSystem
        -SaveManager saveManager
        +update(double deltaTime)
        +render(Graphics2D g2d)
        +changeState(GameState newState)
    }

    class InputHandler {
        -boolean[] keys
        +keyPressed(KeyEvent e)
        +keyReleased(KeyEvent e)
        +isKeyPressed(int keyCode) boolean
        +isKeyJustPressed(int keyCode) boolean
    }

    Main --> GamePanel
    GamePanel --> GameEngine
    GamePanel --> InputHandler
    GameEngine --> InputHandler

    %% ===== GRAPHICS =====
    class AssetLoader {
        -Map~String, BufferedImage[]~ spriteFrames
        -Map~String, BufferedImage~ tileImages
        +loadAll()
        +getFrames(String name) BufferedImage[]
        +getTile(String name) BufferedImage
    }

    class SpriteSheet {
        -BufferedImage sheet
        -int frameWidth
        -int frameHeight
        +getFrame(int col, int row) BufferedImage
        +getRow(int row, int count) BufferedImage[]
    }

    class Animation {
        -BufferedImage[] frames
        -int currentFrameIndex
        -double frameDuration
        -boolean looping
        +update(double deltaTime)
        +getCurrentFrame() BufferedImage
        +reset()
        +isFinished() boolean
    }

    class Camera {
        -int offsetX
        -int offsetY
        -int screenWidth
        -int screenHeight
        +update(int targetX, int targetY)
        +getOffsetX() int
        +getOffsetY() int
    }

    class Renderer {
        -AssetLoader assetLoader
        -Camera camera
        +renderMap(Graphics2D g2d)
        +renderEntities(Graphics2D g2d)
        +renderHUD(Graphics2D g2d)
    }

    GameEngine --> AssetLoader
    GameEngine --> Renderer
    Renderer --> Camera
    Renderer --> AssetLoader
    AssetLoader --> SpriteSheet

    %% ===== ENTITY HIERARCHY =====
    class Entity {
        <<abstract>>
        #double worldX
        #double worldY
        #int tileX
        #int tileY
        #int hp
        #int maxHp
        #int atk
        #int def
        #Direction direction
        #EntityState state
        #Animation currentAnimation
        #List~StatusEffect~ activeEffects
        +update(double deltaTime)*
        +takeDamage(int damage)
        +heal(int amount)
        #updateAnimation(double deltaTime)
        #updateStatusEffects(double deltaTime)
    }

    class Player {
        -int level
        -int exp
        -int gold
        -Inventory inventory
        -Map~EquipmentSlot, Equipment~ equippedItems
        +move(Direction dir, double dt)
        +addExp(int amount)
        +addGold(int amount)
        +equip(Equipment eq)
        -levelUp()
    }

    class Monster {
        <<abstract>>
        #MonsterAI ai
        #int detectionRange
        #int expReward
        #int goldReward
        +updateAI(Player p, GameMap m, double dt)
    }

    class SlimeMonster
    class SkeletonMonster

    class NPC {
        <<abstract>>
        #String dialogText
        +interact(Player player)*
    }

    class Merchant
    class Blacksmith
    class QuestGiver
    class Chest {
        -boolean opened
        -boolean locked
        +open(Player player) boolean
    }

    Entity <|-- Player
    Entity <|-- Monster
    Entity <|-- NPC
    Entity <|-- Chest
    Monster <|-- SlimeMonster
    Monster <|-- SkeletonMonster
    NPC <|-- Merchant
    NPC <|-- Blacksmith
    NPC <|-- QuestGiver
    Entity --> Animation
    Entity --> Direction
    Entity --> EntityState

    %% ===== MAP =====
    class GameMap {
        -int width
        -int height
        -Tile[][] tiles
        -List~Entity~ entities
        -List~Room~ rooms
        +getTile(int x, int y) Tile
        +setTile(int x, int y, Tile tile)
        +isWalkable(int x, int y) boolean
    }

    class Tile {
        -TileType type
        -boolean revealed
        -boolean visible
        -int linkedId
        +isWalkable() boolean
    }

    class MapGenerator {
        <<interface>>
        +generate(int w, int h, int diff) GameMap
    }

    class RandomMapGenerator {
        -Random random
        -int minRoomSize
        -int maxRoomSize
        +generate(int w, int h, int diff) GameMap
    }

    class FileMapLoader {
        -String filePath
        +generate(int w, int h, int diff) GameMap
    }

    class Room {
        -int x
        -int y
        -int width
        -int height
        -boolean visited
        +getCenterX() int
        +getCenterY() int
        +overlaps(Room other, int margin) boolean
    }

    GameMap *-- Tile
    GameMap *-- Room
    GameMap o-- Entity
    Tile --> TileType
    MapGenerator <|.. RandomMapGenerator
    MapGenerator <|.. FileMapLoader
    GameEngine --> GameMap
    GameEngine --> Player

    %% ===== ITEM =====
    class Item {
        <<abstract>>
        #String id
        #String name
        #ItemType type
        #boolean stackable
        #int stackCount
    }

    class Potion {
        -int healAmount
    }

    class Key {
        -String keyId
        +matches(String lockId) boolean
    }

    class Equipment {
        -EquipmentSlot slot
        -int atkBonus
        -int defBonus
        -int upgradeLevel
        +upgrade()
    }

    Item <|-- Potion
    Item <|-- Key
    Item <|-- Equipment
    Item --> ItemType

    %% ===== INVENTORY =====
    class Inventory {
        -List~Item~ items
        -int maxSlots
        +addItem(Item item) boolean
        +removeItem(Item item) boolean
        +findById(String id) Item
        +isFull() boolean
    }

    Player --> Inventory
    Inventory o-- Item

    %% ===== COMBAT =====
    class CombatSystem {
        -double critChance
        -double critMultiplier
        +attack(Entity attacker, Entity defender)
        +isInMeleeRange(Entity a, Entity b) boolean
    }

    GameEngine --> CombatSystem

    %% ===== AI =====
    class MonsterAI {
        -int spawnTileX
        -int spawnTileY
        +update(Monster m, Player p, GameMap map, double dt)
    }

    Monster --> MonsterAI

    %% ===== EVENT =====
    class EventSystem {
        +checkTileEvent(int x, int y)
        +checkRoomEvent()
    }

    class GameEvent {
        <<interface>>
        +getDescription() String
    }

    class TrapEvent
    class RoomEvent

    GameEvent <|.. TrapEvent
    GameEvent <|.. RoomEvent
    EventSystem --> GameEvent
    GameEngine --> EventSystem

    %% ===== SAVE =====
    class SaveManager {
        -String saveDirectory
        +saveGame(String slot)
        +loadGame(String slot) boolean
        +hasSaveFile(String slot) boolean
    }

    GameEngine --> SaveManager
```

## Quan hệ chính

| Quan hệ | Mô tả |
|---------|-------|
| `Entity ← Player` | Kế thừa — Player là Entity có thêm Level, EXP, Inventory |
| `Entity ← Monster ← Slime/Skeleton` | Kế thừa 2 cấp — đa hình cho nhiều loại quái |
| `Entity ← NPC ← Merchant/Blacksmith/QuestGiver` | Kế thừa — mỗi NPC override interact() |
| `MapGenerator → RandomMapGenerator / FileMapLoader` | Interface — Strategy pattern sinh map |
| `GameEngine → GameState` | State Machine — điều khiển luồng game |
| `Player ◇── Inventory ◇── Item[]` | Composition — Player sở hữu Inventory chứa Item |
| `GameMap ◇── Tile[][]` | Composition — Map chứa lưới Tile |
| `Monster → MonsterAI` | Delegation — AI tách riêng khỏi Monster |
