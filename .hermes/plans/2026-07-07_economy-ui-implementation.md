# Người 3 — Economy & UI Implementation Plan

> **Hermes** = planner + orchestrator + frontend. **OpenCode** = backend implementation.
> Dispatch ONE task to OpenCode at a time, review, then next.

**Goal:** Inventory management, chest opening, merchant trading, HUD, dialog, save/load, event system — full Economy & UI vertical slice for Genshin-style open-world RPG.

**Project:** `C:\Users\ducqu\TerraIncognita` — Java 25, Swing, no build tool (javac direct), no test framework yet.

---

## Architecture

- **Frontend (Hermes):** All `render(Graphics2D)` methods in UI classes + GameEngine state wiring. Hermes writes these files directly.
- **Backend (OpenCode):** Data classes, logic, save/load, events, tests. Dispatched one task at a time via `opencode run`.
- **Interface contract:** Backend exposes getters/setters, frontend calls them in render. No circular dependency.
- **Test framework:** Add JUnit 5 first (OpenCode sets up `pom.xml` or `lib/` jar).

---

## Phase 0: Setup (OpenCode)

### Task 0: Add JUnit 5 + fix critical bugs
**Owner:** OpenCode
**Files:**
- Create: `pom.xml` (or `lib/junit-platform-console-standalone.jar` + build script update)
- Modify: `Chest.java:19-20` — `tileX * 32` → `tileX * Constants.TILE_SIZE`
- Modify: `Player.java:161-177` — `equip()`: check `removeItem` return before applying stats; handle inventory-full when returning old equipment
- Modify: `Potion.java` — override `use(Player player)` → `player.heal(healAmount)`, decrement stackCount, return true
- Modify: `Inventory.java` — add `useItem(int index, Player player)` → calls `item.use(player)`, removes if stack depleted

**Verify:** `javac` compiles, existing code still runs.

---

## Phase 1: Inventory & Items (Backend → Frontend)

### Task 1: Inventory useItem + InventoryTest
**Owner:** OpenCode
**Files:**
- Modify: `Inventory.java` — add `useItem(int index, Player player)`
- Create: `src/test/TerraIncognita/inventory/InventoryTest.java`
- Test: add/stack/remove/full/null, useItem with Potion (heal + decrement), useItem with non-consumable (no-op)

### Task 2: InventoryUI render (Frontend)
**Owner:** Hermes
**Files:**
- Modify: `InventoryUI.java` — implement `render(Graphics2D g, Inventory inv, int px, int py)`: 5×4 grid, slot borders, item icon placeholder (colored square by ItemType), stack count, selected slot highlight
- Add: `toggle()`, `isOpen()`, `moveCursor(Direction)`, `getSelectedIndex()`
- Modify: `GameEngine.renderPlaying` — if INVENTORY state, dim background + call `inventoryUI.render()`
- Modify: `GameEngine.updatePlaying` — I key → toggle INVENTORY state, arrow keys → moveCursor, Enter → useItem

**Verify:** Press I → grid appears, move cursor, use potion → HP increases.

---

## Phase 2: Chest & Loot (Backend → Frontend)

### Task 3: Chest fix + loot table + ChestTest
**Owner:** OpenCode
**Files:**
- Modify: `Chest.java` — fix magic number, add `lootTableId` field, `setLootTable(String)`, `open()` generates Item from loot table → adds to player inventory
- Create: `src/test/TerraIncognita/entity/ChestTest.java`
- Test: locked + has key → opens, locked + no key → fails, already opened → fails, open → item in inventory

### Task 4: Chest open visual (Frontend)
**Owner:** Hermes
**Files:**
- Modify: `Renderer.renderEntities` — draw chest with different color when opened vs closed
- Modify: `GameEngine.updatePlaying` — E key near chest → `chest.open(player)`, show pickup message

**Verify:** Walk to chest, press E → item appears in inventory, chest changes appearance.

---

## Phase 3: Merchant & Shop (Backend → Frontend)

### Task 5: Shop data class + Merchant logic + MerchantTest
**Owner:** OpenCode
**Files:**
- Create: `src/TerraIncognita/economy/Shop.java` — `List<Item> items`, `Map<Item,Integer> prices`, `buyItem(Player, int index)`, `sellItem(Player, int index)`
- Modify: `Merchant.java` — init shop inventory from `items.json`, implement `interact(Player)` → set active shop + signal GameEngine to SHOP state
- Create: `src/test/TerraIncognita/economy/MerchantTest.java`
- Test: buy with enough gold → success, buy without gold → fail, sell → gold increases + item removed, buy when inventory full → fail

### Task 6: ShopUI render (Frontend)
**Owner:** Hermes
**Files:**
- Create: `src/TerraIncognita/ui/ShopUI.java` — 2-panel layout: shop items (left) + player items (right), price display, gold display, cursor, buy/sell on Enter
- Modify: `GameEngine` — SHOP state: render shopUI, update handles cursor + buy/sell + ESC to close
- Modify: `Merchant.interact` → store active shop reference for GameEngine

**Verify:** Talk to merchant → shop opens, buy item → gold decreases + item in inventory, sell → gold increases.

---

## Phase 4: HUD & Dialog (Backend → Frontend)

### Task 7: RunSummary + EventSystem + tests
**Owner:** OpenCode
**Files:**
- Modify: `RunSummary.java` — add all fields, setters, getters, `calculateScore()`
- Modify: `EventSystem.java` — `checkTileEvent(GameMap, Player, int x, int y)`: TRAP → damage, CHECKPOINT → SaveManager.save, CHEST → open
- Modify: `TrapEvent.java` — implement constructor + `execute()` + `getDescription()`
- Modify: `GameEvent.java` — add `void execute(Player player, GameMap map)` method
- Create: `src/test/TerraIncognita/event/EventSystemTest.java`
- Test: trap reduces HP, checkpoint saves, expired trap doesn't retrigger

### Task 8: HUD + DialogBox render (Frontend)
**Owner:** Hermes
**Files:**
- Modify: `HUD.java` — implement `render(Graphics2D g, Player player)`: HP bar (red fill on gray bg), level text, exp bar, gold, talent points indicator
- Modify: `DialogBox.java` — implement `show(String)`, `update(InputHandler)`, `render(Graphics2D)`: semi-transparent box, text line, "press to continue" indicator
- Modify: `GameEngine.renderPlaying` — call `HUD.render()` always, call `DialogBox.render()` if DIALOG state
- Modify: `GameEngine.updatePlaying` — E near NPC → DIALOG state + dialogBox.show(npc.getDialogText()), Enter → next line / close

**Verify:** HUD shows HP/level/gold. Talk to NPC → dialog box appears, press Enter → next line → close.

---

## Phase 5: Save & Game Over (Backend → Frontend)

### Task 9: SaveManager + SaveManagerTest
**Owner:** OpenCode
**Files:**
- Modify: `SaveManager.java` — `saveGame(String slot, Player, GameMap)`, `loadGame(String slot, Player, GameMap)`, `hasSaveFile`, `deleteSave`. JSON via StringBuilder (no external lib). Save: player stats + position + inventory + level + gold. Load: restore all.
- Create: `src/test/TerraIncognita/save/SaveManagerTest.java`
- Test: save → load → player stats match (HP, level, gold, inventory contents), hasSaveFile true after save, deleteSave removes file

### Task 10: GameOverScreen render (Frontend)
**Owner:** Hermes
**Files:**
- Modify: `GameOverScreen.java` — implement `render(Graphics2D g, RunSummary)`: "GAME OVER" title, stats (monsters killed, gold, floors, score), "Return to Hub" / "Quit" options
- Modify: `GameEngine` — `updatePlaying`: if `!player.isAlive()` → changeState(GAME_OVER). GAME_OVER state: render game over screen, Enter → MENU
- Modify: `GameEngine.renderGameOver` — call `GameOverScreen.render()`

**Verify:** Player HP reaches 0 → game over screen with stats, Enter → back to menu.

---

## Execution Rules

1. **One OpenCode task at a time.** Dispatch, wait for completion, review, then next.
2. **Hermes writes frontend after backend for that feature is done** (need getters/setters to exist first).
3. **OpenCode command:** `opencode run '<task prompt>' -f <relevant files>` in `~/TerraIncognita`
4. **Review after each task:** compile check (`javac`), read changed files, verify tests pass.
5. **Commit after each phase** (not each task).
6. **Never code without approval** — user reviews plan first.

## Dependency Order

```
Task 0 (setup) → Task 1 (inv backend) → Task 2 (inv frontend)
                → Task 3 (chest backend) → Task 4 (chest frontend)
                → Task 5 (shop backend) → Task 6 (shop frontend)
                → Task 7 (event backend) → Task 8 (HUD/dialog frontend)
                → Task 9 (save backend) → Task 10 (gameover frontend)
```

## Interface Contract (all 3 people agree on these)

```java
// Player — needed by Person 3
Inventory getInventory();
int getHp(), getMaxHp(), getLevel(), getExp(), getExpToNextLevel(), getGold();
void takeDamage(int), heal(int), addGold(int), spendGold(int);
boolean isAlive();

// OpenWorldMap — needed by Person 3 (for EventSystem)
boolean isWalkable(int x, int y);
Tile getTile(int x, int y);
List<Entity> getEntities();
```
