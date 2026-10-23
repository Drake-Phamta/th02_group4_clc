# TV3 — @TrungNGD195 — Admin: Quản lý trái cây & Hạn sử dụng

| Mục | Nội dung |
|---|---|
| Nhánh | `feature/admin-fruit` |
| Đặc tả phụ trách | **Admin Functions** (thêm, sửa, xóa, tìm kiếm, xem trái cây: name, category, price, quantity, expiry date, description) + **Expiry Date Management** (hiển thị hạn sử dụng, nhận diện trái cây đã hết hạn) |
| Màn hình | `FruitManagementActivity` (danh sách), `FruitFormActivity` (thêm/sửa) |

---

## 1. Bắt đầu

```bash
git clone https://github.com/Drake-Phamta/th02_group4_clc.git
cd th02_group4_clc
git checkout feature/admin-fruit
```

Mở bằng Android Studio → chờ Gradle sync → **Run ▶**.
Luồng để vào màn hình của bạn: màn Login (đang là stub) → bấm **"Demo: log in as admin"** → Admin home (stub) → **"Fruit management"**.

> App luôn build được: 2 màn của bạn hiện đang là **stub** (màn tạm có vài nút). Nhiệm vụ của bạn là **thay toàn bộ nội dung** 2 file stub bằng màn hình thật.

---

## 2. File bạn sở hữu (CHỈ được sửa/tạo các file này)

| Loại | Đường dẫn (gốc: `app/src/main/`) | Ghi chú |
|---|---|---|
| Java | `java/com/group4/fruitshop/ui/admin/fruit/FruitManagementActivity.java` | **Thay stub** |
| Java | `java/com/group4/fruitshop/ui/admin/fruit/FruitFormActivity.java` | **Thay stub** |
| Java | `java/com/group4/fruitshop/ui/admin/fruit/AdminFruitAdapter.java` | Tạo mới |
| Layout | `res/layout/activity_fruit_management.xml` | Tạo mới |
| Layout | `res/layout/activity_fruit_form.xml` | Tạo mới |
| Layout | `res/layout/item_admin_fruit.xml` | Tạo mới |
| Layout | `res/layout/dialog_fruit_detail.xml` | Tùy chọn |
| Menu | `res/menu/menu_fruit_management.xml` | Tùy chọn |
| Strings | `res/values/strings_admin_fruit.xml` | Đã có sẵn file rỗng. **Mọi tên string phải bắt đầu bằng `fruit_`** |
| Drawable | `res/drawable/fruit_*.xml` | Nếu cần, **tên phải bắt đầu bằng `fruit_`** |

**Mọi file khác là CHỈ ĐỌC**: `AndroidManifest.xml` (đã khai báo sẵn 2 Activity), `build.gradle.kts`, `strings.xml`, `colors.xml`, `themes.xml`, toàn bộ `data/`, `util/`, `session/`, `ui/common/`, màn hình của thành viên khác. Cần thay đổi gì ở lõi → nhắn nhóm trưởng (@Drake-Phamta).

> Lý do quy tắc tiền tố: tên resource trong Android là toàn cục. Hai người cùng đặt `<string name="title">` ở hai file khác nhau thì Git merge không báo xung đột nhưng **build sẽ lỗi "Duplicate resources"**.

---

## 3. API lõi có sẵn (dùng, không viết lại)

### `data.dao.FruitDao` — `new FruitDao(this)`
| Hàm | Trả về |
|---|---|
| `insert(Fruit f)` | `long` id mới (-1 nếu lỗi) |
| `update(Fruit f)` | `int` số dòng cập nhật (1 = OK) — dựa vào `f.getId()` |
| `delete(int id)` | `int` số dòng xóa (1 = OK) |
| `getById(int id)` | `Fruit` hoặc `null` |
| `getAll()` | `List<Fruit>` tất cả, sắp theo tên |
| `search(String keyword)` | Tìm theo **name hoặc category** (không phân biệt hoa thường); `null`/rỗng = tất cả |
| `getExpired()` | Các trái cây có `expiryDate < hôm nay` |
| `getCategories()` | `List<String>` category khác nhau (gợi ý cho form) |
| `countAll()`, `countExpired()` | `int` |

### `data.model.Fruit`
Trường: `id, name, category, price (double), quantity (int), expiryDate (String "yyyy-MM-dd"), description`.
Constructor: `Fruit(name, category, price, quantity, expiryDate, description)` và `Fruit(id, name, ...)`. Getter/setter đầy đủ. Tiện ích: `isExpired()`, `isInStock()`.

### `util.DateUtils`
| Hàm | Ý nghĩa |
|---|---|
| `today()` | Hôm nay dạng `yyyy-MM-dd` |
| `fromPicker(y, m0, d)` | Giá trị từ `DatePickerDialog` (tháng 0-based) → `yyyy-MM-dd` |
| `toPicker(dbDate)` | `int[]{year, month0, day}` để mở `DatePickerDialog` (ngày lỗi → hôm nay) |
| `isValidDate(s)` | Kiểm tra chuỗi `yyyy-MM-dd` hợp lệ |
| `toDisplayDate(dbDate)` | `2026-10-23` → `23/10/2026` |
| `getExpiryStatus(date)` | `ExpiryStatus.EXPIRED / EXPIRING_SOON / FRESH` |
| `daysUntil(date)` | Số ngày còn lại (âm = đã hết hạn) |

Quy tắc hết hạn (đã thống nhất, **không tự định nghĩa lại**): hết hạn khi `expiryDate < hôm nay`; "sắp hết hạn" khi còn 0–3 ngày (`Constants.EXPIRING_SOON_DAYS`).

### Khác
- `util.FormatUtils.formatPrice(double)` → `"45,000 VND"`.
- `ui.common.Badges.bindExpiry(TextView, expiryDate)` → nhãn màu "Expired" (đỏ) / "Expires in N days" (cam) / "Fresh" (xanh). TextView dùng `style="@style/Widget.FruitShop.Badge"`.
- `util.Constants`: `EXTRA_FRUIT_ID` (int extra cho form sửa), `NO_ID` (-1), `ROLE_ADMIN`.
- `ui.common.BaseActivity` (Activity của bạn **phải extends lớp này**): `requireRole(role)`, `setupActionBar(@StringRes title, showBack)`, `toast(...)`, biến `session`. Mũi tên back trên action bar đã được xử lý sẵn.

---

## 4. Đặc tả màn hình

### 4.1 `FruitManagementActivity` — danh sách trái cây (Admin)
1. Dòng đầu `onCreate` sau `super.onCreate(...)`: `if (!requireRole(Constants.ROLE_ADMIN)) return;` rồi mới `setContentView(...)`. `setupActionBar(R.string.fruit_title, true)`.
2. **Tìm kiếm**: `SearchView` (trên action bar qua menu) hoặc ô `TextInputEditText` ở đầu màn hình; lọc **ngay khi gõ** theo name/category bằng `FruitDao.search(keyword)`.
3. **Lọc hạn sử dụng**: `ChipGroup` (singleSelection, selectionRequired) gồm **All / Expired / Expiring soon**. Lọc trong Java trên kết quả `search(...)` bằng `DateUtils.getExpiryStatus(...)`. Tìm kiếm + bộ lọc phải kết hợp được với nhau.
4. **RecyclerView** (`AdminFruitAdapter` + `item_admin_fruit.xml`, dạng `MaterialCardView`), mỗi thẻ hiển thị:
   - name (đậm), category
   - price `FormatUtils.formatPrice(...)`
   - quantity; nếu `quantity == 0` hiển thị **"Out of stock"**
   - "Expiry: dd/MM/yyyy" (`DateUtils.toDisplayDate`) + badge `Badges.bindExpiry(...)`
   - Trái cây **đã hết hạn được làm nổi bật** (viền đỏ `strokeColor=@color/status_expired` và/hoặc chữ ngày màu đỏ)
5. Dòng tổng kết: **"N fruits · M expired"** (theo danh sách đang hiển thị). Có **trạng thái rỗng** ("No fruits found") khi danh sách trống.
6. **FAB "+"** → mở `FruitFormActivity` (không extra = chế độ thêm).
7. **Bấm 1 thẻ** → dialog chi tiết (`MaterialAlertDialogBuilder`, có thể dùng `dialog_fruit_detail.xml`) hiển thị **ĐẦY ĐỦ** name, category, price, quantity, expiry date + trạng thái, description; các nút:
   - **Edit** → `FruitFormActivity` với `putExtra(Constants.EXTRA_FRUIT_ID, fruit.getId())`
   - **Delete** → dialog xác nhận "Delete <name>?" → `FruitDao.delete(id)` → toast → tải lại danh sách
   - **Close**
8. Tải lại dữ liệu trong `onResume()` (để thấy thay đổi sau khi thêm/sửa).

### 4.2 `FruitFormActivity` — thêm / sửa
1. `requireRole(Constants.ROLE_ADMIN)` như trên.
2. `int id = getIntent().getIntExtra(Constants.EXTRA_FRUIT_ID, Constants.NO_ID);`
   - `NO_ID` → **chế độ thêm**, tiêu đề "Add fruit".
   - Ngược lại → **chế độ sửa**, tiêu đề "Edit fruit", nạp `getById(id)` và điền sẵn form; nếu `null` → toast "Fruit not found" + `finish()`.
3. Các trường (dùng `TextInputLayout` + `TextInputEditText`):
   | Trường | Input | Kiểm tra |
   |---|---|---|
   | Name | text | bắt buộc |
   | Category | `MaterialAutoCompleteTextView` gợi ý từ `getCategories()` | bắt buộc |
   | Price | `numberDecimal` | bắt buộc, số > 0 |
   | Quantity | `number` | bắt buộc, số nguyên ≥ 0 |
   | Expiry date | ô **không gõ được** (`focusable=false`), bấm mở `DatePickerDialog` | bắt buộc; lưu `yyyy-MM-dd` (`DateUtils.fromPicker`), hiển thị `dd/MM/yyyy` |
   | Description | multi-line | không bắt buộc |
4. Nếu ngày chọn đã qua: **vẫn cho lưu** nhưng hiện cảnh báo (helper text màu đỏ) "This date is in the past — the fruit will be marked Expired".
5. Lỗi hiển thị bằng `TextInputLayout.setError(...)` ngay tại trường sai (không chỉ toast).
6. **Save**: hợp lệ → `insert` hoặc `update` → kiểm tra kết quả → toast "Fruit added" / "Fruit updated" → `finish()`. Lỗi DB → toast báo lỗi, giữ nguyên màn hình.

---

## 5. Tiêu chí nghiệm thu (tự kiểm trước khi mở PR)

- [ ] Dữ liệu mẫu: **Cat Chu Mango** và **Watermelon** hiển thị **Expired** (đỏ, thẻ được làm nổi bật); **Banana**, **Strawberry** hiển thị **Expires in N days** (cam); **Red Grapes** hiển thị **Out of stock**.
- [ ] Thêm trái cây mới → xuất hiện trong danh sách với đủ thông tin.
- [ ] Sửa trái cây (đổi giá, số lượng, ngày) → danh sách cập nhật đúng.
- [ ] Xóa có hộp thoại xác nhận → trái cây biến mất.
- [ ] Tìm `citrus` → Orange, Lemon; tìm `app` → Fuji Apple (và các tên/category chứa "app").
- [ ] Chip **Expired** chỉ còn trái cây hết hạn; kết hợp được với ô tìm kiếm.
- [ ] Dialog chi tiết hiển thị đủ 6 thông tin kể cả description.
- [ ] Bỏ trống trường bắt buộc / giá ≤ 0 / số lượng âm → báo lỗi tại trường, không lưu.
- [ ] Tài khoản **Customer** không mở được 2 màn hình này (`requireRole`).
- [ ] Xoay màn hình / quay lại từ form không crash.
- [ ] `gradlew assembleDebug testDebugUnitTest` chạy thành công, **CI trên PR xanh**.
- [ ] `git diff --stat origin/main` chỉ chứa các file ở mục 2.

---

## 6. Commit & Pull Request

```bash
git add app/src/main/java/com/group4/fruitshop/ui/admin/fruit app/src/main/res
git commit -m "feat(admin-fruit): fruit list with search, expiry filter and badges"
git commit -m "feat(admin-fruit): add/edit form with validation and date picker"
git push origin feature/admin-fruit
```

- Commit nhỏ, thường xuyên, prefix `feat(admin-fruit): ...` / `fix(admin-fruit): ...`.
- Trên GitHub: **New pull request** `feature/admin-fruit` → `main`, điền template (mô tả, ảnh chụp màn hình, checklist), gán reviewer **@Drake-Phamta**.
- Trước khi mở PR, đồng bộ main: `git fetch origin && git merge origin/main` (không được có xung đột vì bạn chỉ sửa file của mình).
- **Không sửa README** — nhóm trưởng cập nhật sau khi merge.
