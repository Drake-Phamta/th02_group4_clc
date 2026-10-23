# TV4 — Quoc Khanh (@qkhanhbe) — Chức năng Khách hàng (Customer)

> ⚠️ **Việc đầu tiên:** chấp nhận lời mời collaborator tại
> https://github.com/Drake-Phamta/th02_group4_clc/invitations — chưa chấp nhận thì **không push được**.

| Mục | Giá trị |
|---|---|
| Nhánh làm việc | `feature/customer` |
| Package | `com.group4.fruitshop.ui.customer` |
| Tiền tố resource mới | `customer_` |
| Đặc tả phụ trách | **Customer Functions** (xem trái cây còn hàng, tìm kiếm, kiểm tra hạn dùng, đặt hàng, xem lịch sử đơn) · **Expiry**: *chặn khách đặt trái cây đã hết hạn* · **Order Management**: phía khách đặt đơn |

---

## 1. Bắt đầu

```bash
git clone https://github.com/Drake-Phamta/th02_group4_clc.git
cd th02_group4_clc
git checkout feature/customer
```

Mở bằng Android Studio → chờ Gradle sync → **Run ▶**.
Màn Login hiện đang là **stub** (TV2 làm) → bấm **"Demo: log in as customer"** để vào `CustomerHomeActivity` (stub của bạn).
Tài khoản mẫu: `customer / customer123` (Nguyen Van An), `customer2 / customer123`, admin: `admin / admin123`.

---

## 2. File bạn SỞ HỮU (chỉ được sửa/tạo các file này)

**Java** — `app/src/main/java/com/group4/fruitshop/ui/customer/`
| File | Việc |
|---|---|
| `CustomerHomeActivity.java` | **Thay toàn bộ stub** — danh sách trái cây + tìm kiếm |
| `FruitDetailActivity.java` | **Thay toàn bộ stub** — chi tiết + đặt hàng |
| `OrderHistoryActivity.java` | **Thay toàn bộ stub** — lịch sử đơn |
| `CustomerFruitAdapter.java` | Tạo mới — adapter danh sách trái cây |
| `OrderHistoryAdapter.java` | Tạo mới — adapter lịch sử đơn |

**Resource** — `app/src/main/res/`
| File | Việc |
|---|---|
| `layout/activity_customer_home.xml`, `layout/item_customer_fruit.xml` | Tạo mới |
| `layout/activity_fruit_detail.xml` | Tạo mới |
| `layout/activity_order_history.xml`, `layout/item_order_history.xml` | Tạo mới |
| `menu/menu_customer_home.xml` | Tạo mới |
| `values/strings_customer.xml` | Đã có sẵn (rỗng) — **mọi tên string bắt đầu bằng `customer_`** |
| `drawable/customer_*.xml` | Nếu cần drawable riêng, **bắt buộc** tiền tố `customer_` |

🚫 **Mọi file khác chỉ được ĐỌC**: `AndroidManifest.xml` (đã khai báo sẵn 3 Activity của bạn), `build.gradle.kts`, `strings.xml`, `colors.xml`, `themes.xml`, toàn bộ `data/`, `session/`, `util/`, `ui/common/`, file của thành viên khác, `README.md`. Cần thêm gì ở lõi → nhắn nhóm trưởng (@Drake-Phamta).

> Lý do: mỗi người chỉ chạm file của mình ⇒ merge **không bao giờ xung đột**; tên resource có tiền tố ⇒ không bị lỗi *Duplicate resources* khi gộp.

---

## 3. API lõi được dùng (đã có trên `main`, KHÔNG viết SQL riêng)

```java
// Trái cây
FruitDao fruitDao = new FruitDao(this);
List<Fruit> list = fruitDao.searchInStock(keyword);  // quantity > 0, khớp name/category; keyword rỗng = tất cả; trái cây HẾT HẠN vẫn trả về nhưng xếp CUỐI
Fruit f = fruitDao.getById(id);                       // null nếu không có
f.isExpired();  f.isInStock();  f.canBeOrdered();     // canBeOrdered = còn hàng && chưa hết hạn
f.getName(); getCategory(); getPrice(); getQuantity(); getExpiryDate() /* yyyy-MM-dd */; getDescription();

// Đơn hàng
OrderDao orderDao = new OrderDao(this);
OrderDao.PlaceOrderResult r = orderDao.placeOrder(session.getCustomerId(), fruitId, quantity);
// Trong 1 transaction: kiểm tra hạn + tồn kho, TRỪ Fruit.quantity, tạo Order(status = Pending, orderDate = now, totalAmount = price × qty)
List<Order> orders = orderDao.getByCustomer(session.getCustomerId());  // mới nhất trước
order.getId(); getOrderDate() /* yyyy-MM-dd HH:mm:ss */; getTotalAmount(); getStatus();

// Phiên đăng nhập / khách hàng
session.getCustomerId();  session.getUsername();         // `session` có sẵn trong BaseActivity
Customer c = new CustomerDao(this).getById(session.getCustomerId());  // c.getFullname()
Navigator.logout(this);                                   // xóa session, về Login, xóa back stack

// Hiển thị
Badges.bindExpiry(tvBadge, f.getExpiryDate());   // "Expired" đỏ / "Expires in N days" cam / "Fresh" xanh
Badges.bindOrderStatus(tvBadge, order.getStatus()); // Pending vàng / Confirmed xanh dương / Completed xanh lá
// TextView badge dùng: style="@style/Widget.FruitShop.Badge"
DateUtils.toDisplayDate("2026-10-23");               // "23/10/2026"
DateUtils.toDisplayDateTime("2026-10-23 14:05:00");  // "23/10/2026 14:05"
DateUtils.daysUntil(f.getExpiryDate());              // 0 = hôm nay, âm = đã hết hạn
FormatUtils.formatPrice(45000);                      // "45,000 VND"
Constants.EXTRA_FRUIT_ID;  Constants.ROLE_CUSTOMER;  Constants.NO_ID;

// BaseActivity (mọi Activity của bạn extends BaseActivity)
requireRole(Constants.ROLE_CUSTOMER);  setupActionBar(R.string.xxx, true);  toast(...);
```

**Map `PlaceOrderResult` → thông báo:**
| Kết quả | Thông báo (gợi ý) |
|---|---|
| `SUCCESS` | "Order placed successfully (Pending)" |
| `INVALID_QUANTITY` | "Quantity must be at least 1" |
| `FRUIT_NOT_FOUND` | "This fruit no longer exists" |
| `EXPIRED` | "This fruit has expired and cannot be ordered" |
| `OUT_OF_STOCK` | "Not enough stock (only N left)" |
| `FAILED` | "Could not place the order, please try again" |

> ℹ️ Theo đặc tả, bảng `Order(id, customerId, orderDate, totalAmount, status)` **không lưu danh sách trái cây** ⇒ lịch sử đơn chỉ hiển thị **#id, ngày, tổng tiền, trạng thái**. Không tự thêm bảng/cột.

---

## 4. Đặc tả màn hình (UI tiếng Anh, chuỗi để trong `strings_customer.xml`)

Mọi Activity: dòng đầu `onCreate` sau `super.onCreate(...)` là
`if (!requireRole(Constants.ROLE_CUSTOMER)) return;` (test CI sẽ kiểm tra admin **không** mở được màn khách).

### 4.1 `CustomerHomeActivity` — Trang chủ khách hàng
- Tiêu đề / dòng chào: `Hello, <fullname>` (lấy từ `CustomerDao.getById(session.getCustomerId())`).
- Ô tìm kiếm (EditText/`SearchView`) **lọc trực tiếp khi gõ** theo tên hoặc loại → `fruitDao.searchInStock(keyword)`.
- (Tùy chọn, nên có) Chip/Checkbox **"Hide expired"** → lọc bỏ `f.isExpired()` trên danh sách.
- `RecyclerView` (card `item_customer_fruit.xml`): tên, loại, giá (`formatPrice`), tồn kho `Stock: N`, `Expiry: dd/MM/yyyy` + badge `Badges.bindExpiry`.
  - Trái cây **hết hạn**: làm mờ item (`alpha 0.5`) + dòng chữ đỏ **"Cannot order"**.
- Danh sách rỗng → hiện `No fruits found`.
- Bấm item → `FruitDetailActivity` với `putExtra(Constants.EXTRA_FRUIT_ID, fruit.getId())`.
- Menu action bar (`menu_customer_home.xml`): **Order history** → `OrderHistoryActivity`; **Logout** → dialog xác nhận → `Navigator.logout(this)`.
- Tải lại danh sách trong `onResume()` (tồn kho thay đổi sau khi đặt hàng).

### 4.2 `FruitDetailActivity` — Chi tiết & Đặt hàng
- Đọc `getIntExtra(Constants.EXTRA_FRUIT_ID, Constants.NO_ID)` → `getById`; nếu `null` → toast + `finish()`.
- Hiển thị **đủ thông tin**: tên, loại, giá, tồn kho, mô tả, **ngày hết hạn** (dd/MM/yyyy) + badge hạn dùng + số ngày còn lại / "Expired".
- Chọn số lượng: nút **− / +** (hoặc ô nhập số), giới hạn `1..quantity`; **Tổng tiền cập nhật trực tiếp** = `price × qty`.
- Nếu `isExpired()` → **disable** nút Order + thông báo đỏ **"This fruit has expired and cannot be ordered"**.
  Nếu hết hàng → disable + "Out of stock".
- Bấm **Order** → dialog xác nhận (tên, SL, tổng tiền) → `orderDao.placeOrder(session.getCustomerId(), id, qty)`:
  - `SUCCESS` → toast "Order placed (Pending)" → mở `OrderHistoryActivity` (hoặc `finish()`).
  - Kết quả khác → thông báo theo bảng mục 3, **tải lại** dữ liệu trái cây.
- Lưu ý: chặn ở UI **và** DAO đều chặn hàng hết hạn (đúng yêu cầu đặc tả).

### 4.3 `OrderHistoryActivity` — Lịch sử đơn hàng
- `orderDao.getByCustomer(session.getCustomerId())` (mới nhất trước).
- Mỗi card (`item_order_history.xml`): `Order #id`, ngày `dd/MM/yyyy HH:mm` (`toDisplayDateTime`), tổng tiền, badge trạng thái `Badges.bindOrderStatus`.
- Dòng tổng kết đầu danh sách: `N orders · Total spent X VND`.
- Rỗng → `You have no orders yet`.
- Nút back trên action bar (`setupActionBar(..., true)`).

---

## 5. Tiêu chí nghiệm thu (tự test trước khi mở PR)

- [ ] Đăng nhập `customer` → thấy danh sách; **không** thấy *Red Grapes* (tồn kho 0).
- [ ] *Cat Chu Mango* và *Watermelon* hiện badge **Expired**, bị làm mờ, **không đặt được** (nút Order bị disable).
- [ ] *Banana*, *Strawberry* hiện badge cam "Expires in N days".
- [ ] Tìm "citrus" → ra Orange, Lemon; tìm "app" → ra Fuji Apple.
- [ ] Đặt **2 Fuji Apple** → tồn kho 50 → **48**; lịch sử có đơn mới **Pending – 130,000 VND**.
- [ ] Nhập số lượng > tồn kho → bị từ chối, có thông báo.
- [ ] Đăng nhập `admin` → *Order management* thấy đơn mới → **Confirm order** → đăng nhập lại `customer` → lịch sử hiện **Confirmed**.
- [ ] Admin **không** mở được màn hình của khách (bị đẩy về Login).
- [ ] Logout → về Login, bấm Back không quay lại được.
- [ ] `gradlew assembleDebug testDebugUnitTest` chạy OK, CI trên PR **xanh**.
- [ ] Chỉ thay đổi các file ở mục 2.

---

## 6. Commit & Pull Request

```bash
git add app/src/main/java/com/group4/fruitshop/ui/customer app/src/main/res
git commit -m "feat(customer): danh sách, tìm kiếm, chi tiết, đặt hàng và lịch sử đơn"
git push origin feature/customer
```
Commit nhỏ theo từng màn (`feat(customer): ...`, `fix(customer): ...`).
Lên GitHub → **New pull request** `feature/customer` → `main` → điền template PR (checklist + ảnh chụp màn hình) → chờ CI xanh → báo nhóm trưởng merge.

Nếu `main` có cập nhật mới: `git fetch origin && git merge origin/main` (sẽ không xung đột vì bạn không sửa file chung).
