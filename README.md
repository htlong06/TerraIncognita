# Terra Incognita

Trong **Terra Incognita**, người chơi sẽ hóa thân thành một nhà mạo hiểm khám phá thế giới. Xuyên suốt hành trình, bạn sẽ phải vận dụng linh hoạt giữa cận chiến bằng kiếm và bắn cung tầm xa để chiến đấu với quái vật, hoặc đặt bom nhằm dọn dẹp các mục tiêu diện rộng. Trò chơi kết hợp chặt chẽ yếu tố nhập vai thông qua việc thu thập vật phẩm từ rương báu, giao dịch cùng NPC thương nhân, nhận và hoàn thành các quest, cũng như tối ưu hóa việc quản lý túi đồ. Toàn bộ hành trình của bạn đều có thể được lưu/tải tiến trình chơi một cách an toàn để tiếp tục cuộc phiêu lưu qua nhiều phiên khác nhau.

## 🎮 Các tính năng của game

### Di chuyển & điều khiển
- Di chuyển 8 hướng bằng `WASD` hoặc phím mũi tên, tốc độ chuẩn hoá (không bị nhanh hơn khi đi chéo).
- Camera bám theo player, tự giới hạn trong biên bản đồ.

### Chiến đấu
- **2 chế độ vũ khí**, đổi qua lại bằng phím `E`:
  - **Kiếm (SWORD)**: cận chiến, bấm chuột trái là chém ngay; có **combo 3 đòn liên tiếp** — đòn thứ 3 (finisher) gây thêm 30% sát thương; combo bị reset nếu quá 1 giây không đánh tiếp.
  - **Cung (BOW)**: tầm xa, giữ chuột trái để ngắm (giới hạn góc bắn ±60° quanh phương ngang), thả ra để bắn mũi tên bay theo hướng ngắm, tốc độ 400px/s, tầm bắn tối đa 500px.
- Hệ thống sát thương có tỉ lệ **chí mạng** (10%, nhân 1.5 sát thương) và **trượt đòn** (1%).
- **Đặt bom** (phím `B`): nổ diện rộng 3×3 ô, gây 40 sát thương cho mọi mục tiêu trong phạm vi, kèm animation nổ.

### Quái vật & AI
- **Quái thường** (ví dụ Orc): AI state machine `IDLE → CHASE → ATTACK → RETURN_TO_SPAWN`, có tầm phát hiện, cơ chế "dây xích" (leash) quay về vị trí spawn nếu đuổi player quá xa.
- **Swarm Event — đàn ếch**: đàn quái spawn trong 1 vùng góc map (`DORMANT`), di chuyển theo thuật toán boid (tách nhau ra, bắt chước hướng đàn, tụ về tâm đàn). Khi player bước vào vùng kích hoạt → chuyển `ACTIVE`, cả đàn được thả tự do khắp map, né player khi lại gần (hoảng loạn tăng tốc), bị hất văng khi bomb nổ gần; tiêu diệt hết cả đàn → `COMPLETED`.

### Vật phẩm & kinh tế
- **Rương báu** (Chest) với 3 độ hiếm (`common`/`rare`/`mythic`), mỗi độ hiếm gắn 1 `LootTable` riêng (xác suất rơi đồ + item khả dĩ).
- **Túi đồ (Inventory)** giới hạn 20 ô, hỗ trợ vật phẩm hồi máu (Potion — hồi ngay hoặc hồi dần theo thời gian), nguyên liệu (Material), bom mang theo (BombItem).
- **Cửa hàng (Shop)** — mua/bán với NPC thương nhân, có rollback tự động nếu giao dịch thất bại giữa chừng (ví dụ túi đồ đầy).

### Nhiệm vụ (Quest)
- NPC Quest Giver riêng biệt với thương nhân, giao nhiệm vụ theo 2 dạng mục tiêu: **tiêu diệt quái** (`KILL_MONSTER`) hoặc **thu thập vật phẩm** (`COLLECT_ITEM`).
- Theo dõi tiến độ (`QuestProgress`), trạng thái nhiệm vụ đi qua 3 giai đoạn: `ACTIVE → READY_TO_TURN_IN → TURNED_IN`, có thể gắn phần thưởng vật phẩm ngoài vàng/exp.

### Âm thanh
- Nhạc nền loop liên tục (`BackgroundMusic`).
- Hiệu ứng âm thanh (SFX) chồng lấn được — mỗi lần phát mở 1 `Clip` riêng, không cắt ngang tiếng đang phát: tiếng chém kiếm thường/combo, tiếng bắn cung, tiếng nổ bom, và tiếng ếch kêu **theo khoảng cách thực tế** tới con ếch gần nhất (không chỉ 1 lần lúc kích hoạt sự kiện).

### Giao diện & luồng chơi
- Màn hình Menu chính (có phát hiện save cũ để tiếp tục), HUD hiển thị máu/vàng/exp, hộp thoại (Dialog) khi tương tác NPC, giao diện túi đồ và cửa hàng riêng, màn hình tạm dừng (Pause), màn hình tổng kết sau lượt chơi (Run Summary), màn hình thua cuộc (Game Over).

### Lưu / tải game
- Lưu toàn bộ tiến trình (vị trí, chỉ số, HP, level, exp, gold, túi đồ, trang bị) vào **SQLite**, hỗ trợ nhiều slot save độc lập, dữ liệu con (inventory, equipped) xoá theo cascade khi xoá slot cha.


## 🛠️ Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt
### Công nghệ / thư viện

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17 |
| Build tool | Apache Maven |
| Đồ hoạ | Java Swing / AWT  |
| Bản đồ | [libtiled](https://github.com/mapeditor/libtiled-java) 1.4.2 — đọc file `.tmx`/`.tsx` từ Tiled Map Editor |
| Lưu trữ | SQLite qua `sqlite-jdbc` 3.46.1.3 |
| XML binding | `jaxb-api` 2.3.1 + `jaxb-runtime` 2.3.3  |
| Kiểm thử | JUnit Jupiter 5.10.0 |
| Âm thanh | `javax.sound.sampled`  |

### Yêu cầu môi trường

- **JDK 17** trở lên
- **Apache Maven** đã cài và có trong `PATH` 
- Hệ điều hành: Windows / Linux / macOS đều chạy được; có sẵn script build riêng cho Windows (`build.bat`) và Unix (`build.sh`) nếu không muốn dùng Maven trực tiếp

### Cài đặt & chạy

**Cách 1 — dùng Maven (khuyến nghị):**

```bash
git clone https://github.com/htlong06/TerraIncognita.git
cd TerraIncognita
mvn compile
mvn exec:java
```

**Cách 2 — dùng script build có sẵn (không cần gõ lệnh Maven):**

```bash
# Windows
build.bat

# Linux / macOS
chmod +x build.sh
./build.sh
```
Script tự biên dịch toàn bộ `src/` bằng `javac` (dùng thư viện trong `lib/`) rồi chạy `TerraIncognita.Main`.

**Chạy test (JUnit):**

```bash
mvn test
# hoặc
./build.sh test      # Linux/macOS
build.bat test        # Windows
```


**Vai trò các module lõi:**

- **`GameEngine`** — lớp trung tâm, giữ tham chiếu tới mọi hệ thống con (player, map, camera, combat, quest, shop, sự kiện...), chạy vòng `update()`/`render()` mỗi frame và chuyển trạng thái `GameState`.
- **`CollisionManager`** — giải va chạm giữa entity (player/quái) với tile tường và với nhau, dùng chung cho cả di chuyển của player lẫn AI quái.
- **`CombatSystem`** — tính toán sát thương (kèm tỉ lệ chí mạng/trượt) khi entity này tấn công entity khác.
- **`MonsterAI`** — state machine 4 trạng thái (`IDLE → CHASE → ATTACK → RETURN_TO_SPAWN`) điều khiển hành vi quái thường (ví dụ `OrcMonster`).
- **`SwarmEvent`** — hệ thống riêng cho bầy đàn ếch, dùng thuật toán **Boid/Flocking**.
- **`SaveManager`** — lưu/tải toàn bộ trạng thái người chơi (vị trí, chỉ số, HP, level, exp, gold, túi đồ, trang bị) vào SQLite, hỗ trợ nhiều slot save.
- **`Constants`** — tập trung toàn bộ magic number của game (tốc độ, sát thương, bán kính, đường dẫn tài nguyên...) để dễ tinh chỉnh cân bằng game mà không phải sửa rải rác trong code.

## 👥 Bảng phân công công việc

| STT | Họ và tên | Chi tiết công việc đảm nhiệm |
| :---: | :--- | :--- |
| **1** | **Hứa Thành Long - 24021551** | -Tạo game thread, dựng GameEngine, vòng lặp game (Game loop).<br>- Hệ thống chiến đấu (kiếm, cung, bom).<br>- Sự kiện bầy ếch.<br>- Hệ thống camera.<br>- Load asset, sprite sheet, animation.<br>- Sound manager.<br>- Collision và hitbox.<br>- Player. <br>- Hệ thống quest(nhận, kiểm soát tiến độ, giao thưởng). <br>- |
| **2** | **Nguyễn Đức Quang - 24021607** | |
| **3** | **Lê Bùi Đức Hạnh - 24022648** ||

## 📚 Tài liệu tham khảo

* **Thuật toán Bầy đàn (Boids / Flocking Algorithm):**

  * **Mã nguồn tham khảo (Processing/Java) từ "The Nature of Code":** 
    * [Dự án gốc NOC_6_09_Flocking](https://github.com/nature-of-code/noc-examples-processing/tree/master/chp06_agents/NOC_6_09_Flocking)
    * Logic mô phỏng thuật toán được tham khảo và chuyển đổi để ứng dụng vào class `SwarmEvent.java` thông qua các file [Boid.pde](https://github.com/nature-of-code/noc-examples-processing/blob/master/chp06_agents/NOC_6_09_Flocking/Boid.pde) và [Flock.pde](https://github.com/nature-of-code/noc-examples-processing/blob/master/chp06_agents/NOC_6_09_Flocking/Flock.pde).
