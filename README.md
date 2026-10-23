# 🍎 Fruit Shop Management — Nhóm 4 (TH02 · CLC)

Ứng dụng Android **quản lý cửa hàng trái cây**, có 2 vai trò **Admin** và **Customer**. Dữ liệu lưu bằng **SQLite**, thông tin đăng nhập lưu bằng **SharedPreferences**. Toàn bộ mã nguồn được quản lý và cộng tác trên GitHub.

> Đặc tả gốc: [docs/SPEC.md](docs/SPEC.md) · Phân công: [docs/PHAN_CONG.md](docs/PHAN_CONG.md)

---

## 1. Giới thiệu (tóm tắt đề bài)

Project gồm đúng **4 bảng**:

- `Account(id, username, password, role)`
- `Fruit(id, name, category, price, quantity, expiryDate, description)`
- `Customer(id, fullname, phone, email, accountId)`
- `Order(id, customerId, orderDate, totalAmount, status)`

Các chức năng bắt buộc: đăng nhập 2 vai trò, lưu tài khoản đăng nhập bằng SharedPreferences, lưu dữ liệu bằng SQLite/Room, Admin quản lý trái cây, quản lý hạn sử dụng (chặn đặt trái cây hết hạn), chức năng cho khách hàng, quản lý đơn hàng (Pending → Confirmed → Completed) và cộng tác qua GitHub.

## 2. Chức năng ↔ nơi cài đặt

| Yêu cầu trong đề | Cài đặt | Người phụ trách |
|---|---|---|
| **Login** 2 vai trò Admin / Customer, điều hướng theo vai trò | `ui/auth/LoginActivity`, `ui/common/Navigator.openHome()` | TV2 |
| **Login Account Storage** bằng SharedPreferences (phiên đăng nhập, Remember me, tự đăng nhập) | `session/SessionManager` (TV1) + dùng trong `LoginActivity` (TV2) | TV1 + TV2 |
| Đăng ký tài khoản khách hàng (tạo `Account` + `Customer`) | `ui/auth/RegisterActivity`, `AccountDao.registerCustomer()` | TV2 + TV1 |
| **Data Storage**: SQLite, 4 bảng, dữ liệu mẫu | `data/DatabaseHelper`, `data/SeedData`, `data/dao/*` | TV1 |
| **Admin Functions**: thêm, sửa, xóa, tìm kiếm, xem trái cây | `ui/admin/fruit/FruitManagementActivity`, `FruitFormActivity` | TV3 |
| **Expiry Date Management**: hiển thị hạn dùng, nhận diện trái cây hết hạn | `util/DateUtils`, `ui/common/Badges` (TV1) + màn hình Admin (TV3) / Customer (TV4) | TV1 + TV3 + TV4 |
| Chặn khách đặt trái cây hết hạn | `OrderDao.placeOrder()` trả về `EXPIRED` (TV1) + vô hiệu nút đặt hàng (TV4) | TV1 + TV4 |
| **Customer Functions**: xem trái cây còn hàng, tìm kiếm, kiểm tra hạn dùng, đặt hàng, lịch sử đơn | `ui/customer/CustomerHomeActivity`, `FruitDetailActivity`, `OrderHistoryActivity` | TV4 |
| **Order Management**: khách đặt hàng, Admin cập nhật trạng thái | `ui/admin/order/OrderManagementActivity` (TV1), đặt hàng (TV4) | TV1 + TV4 |
| Màn hình chính Admin (thống kê + điều hướng) | `ui/admin/AdminHomeActivity` | TV2 |
| **Version Control & Collaboration** | GitHub: nhánh riêng cho từng thành viên, Pull Request, CI tự động build và test | Cả nhóm |

## 3. Công nghệ

- **Ngôn ngữ**: Java 17 · **IDE**: Android Studio
- **Android**: minSdk 24, compileSdk/targetSdk 35, Android Gradle Plugin 8.8.2, Gradle 8.14
- **UI**: AppCompat, Material Components (theme `Theme.FruitShop`), RecyclerView, ConstraintLayout
- **Dữ liệu**: SQLite qua `SQLiteOpenHelper`; mật khẩu băm **SHA-256**
- **Lưu đăng nhập**: SharedPreferences (`SessionManager`)
- **Test** (`app/src/test`): JUnit 4 + Robolectric. Kiểm tra lược đồ 4 bảng, DAO và quy tắc đặt hàng, SharedPreferences, module đơn hàng, và mở thử mọi màn hình theo đúng vai trò (`ScreenSmokeTest`)
- **CI**: GitHub Actions `Android CI`, tự build và chạy test cho mỗi push hoặc Pull Request vào `main`

## 4. Kiến trúc & cấu trúc thư mục

```
app/src/main/java/com/group4/fruitshop/
├── data/
│   ├── DatabaseHelper.java      # SQLiteOpenHelper: tạo 4 bảng, bật khóa ngoại
│   ├── SeedData.java            # dữ liệu mẫu (tài khoản, trái cây, đơn hàng)
│   ├── dao/                     # AccountDao, FruitDao, CustomerDao, OrderDao
│   └── model/                   # Account, Fruit, Customer, Order
├── session/
│   └── SessionManager.java      # SharedPreferences: phiên đăng nhập + Remember me
├── util/                        # Constants, OrderStatus, DateUtils, FormatUtils, PasswordUtils
└── ui/
    ├── common/                  # BaseActivity, Navigator, Badges, StubScreen
    ├── auth/                    # LoginActivity, RegisterActivity            (TV2)
    ├── admin/                   # AdminHomeActivity                          (TV2)
    │   ├── fruit/               # FruitManagementActivity, FruitFormActivity (TV3)
    │   └── order/               # OrderManagementActivity, AdminOrderAdapter (TV1)
    └── customer/                # CustomerHome, FruitDetail, OrderHistory    (TV4)
```

**Luồng màn hình**

```
LoginActivity ──(Admin)──► AdminHomeActivity ──► FruitManagementActivity ──► FruitFormActivity
     │                                       └──► OrderManagementActivity
     ├──(Customer)──► CustomerHomeActivity ──► FruitDetailActivity (đặt hàng)
     │                                   └──► OrderHistoryActivity
     └──► RegisterActivity
```

Mọi màn hình kế thừa `BaseActivity` và gọi `requireRole(...)` đầu tiên trong `onCreate`. Nếu người dùng chưa đăng nhập hoặc sai vai trò, ứng dụng tự quay về màn hình đăng nhập.

## 5. Thiết kế cơ sở dữ liệu

| Bảng | Cột | Ràng buộc |
|---|---|---|
| `Account` | `id` INTEGER PK AUTOINCREMENT, `username` TEXT, `password` TEXT, `role` TEXT | `username` NOT NULL UNIQUE (không phân biệt hoa/thường); `password` lưu **SHA-256**; `role` ∈ {`Admin`, `Customer`} |
| `Fruit` | `id` INTEGER PK, `name` TEXT, `category` TEXT, `price` REAL, `quantity` INTEGER, `expiryDate` TEXT, `description` TEXT | `price ≥ 0`, `quantity ≥ 0`; `expiryDate` dạng `yyyy-MM-dd` |
| `Customer` | `id` INTEGER PK, `fullname` TEXT, `phone` TEXT, `email` TEXT, `accountId` INTEGER | `accountId` UNIQUE, FK → `Account(id)` ON DELETE CASCADE |
| `"Order"` | `id` INTEGER PK, `customerId` INTEGER, `orderDate` TEXT, `totalAmount` REAL, `status` TEXT | FK → `Customer(id)`; `orderDate` dạng `yyyy-MM-dd HH:mm:ss`; `status` ∈ {`Pending`, `Confirmed`, `Completed`}, mặc định `Pending` |

> `Order` là từ khóa SQL nên tên bảng luôn được đặt trong ngoặc kép: `"Order"` (hằng `DatabaseHelper.TABLE_ORDER`).

**Quy tắc nghiệp vụ**

- **Hết hạn** khi `expiryDate < hôm nay`; đúng ngày hết hạn vẫn bán được. **Sắp hết hạn** khi còn ≤ 3 ngày (`Constants.EXPIRING_SOON_DAYS`).
- **Không thể đặt** trái cây đã hết hạn. Việc chặn được làm ở cả tầng dữ liệu (`OrderDao.placeOrder` → `EXPIRED`) lẫn giao diện.
- Khi đặt hàng: `totalAmount = price × quantity`, tồn kho `Fruit.quantity` bị trừ ngay, đơn mới có trạng thái `Pending`. Toàn bộ thao tác chạy trong một transaction.
- Trạng thái đơn chỉ đi tiến: **Pending → Confirmed → Completed**.
- Theo đúng đặc tả 4 bảng, bảng `Order` chỉ lưu tổng tiền, không lưu chi tiết từng mặt hàng.

## 6. Tài khoản demo (tạo sẵn khi cài app)

| Vai trò | Username | Password | Ghi chú |
|---|---|---|---|
| Admin | `admin` | `admin123` | |
| Customer | `customer` | `customer123` | Nguyen Van An |
| Customer | `customer2` | `customer123` | Tran Thi Binh |

Dữ liệu mẫu có 11 loại trái cây, trong đó có loại **đã hết hạn**, loại **sắp hết hạn**, loại **hết hàng**, cùng 4 đơn hàng ở đủ các trạng thái. Hạn dùng được tính tương đối theo ngày cài app.

## 7. Cách chạy

**Android Studio:** `File → Open` → chọn thư mục repo → chờ Gradle sync → chọn máy ảo hoặc điện thoại → **Run ▶**.

**Dòng lệnh:**

```bash
git clone https://github.com/Drake-Phamta/th02_group4_clc.git
cd th02_group4_clc
./gradlew assembleDebug          # Windows: gradlew.bat assembleDebug
./gradlew testDebugUnitTest      # chạy unit test
```

File APK nằm tại `app/build/outputs/apk/debug/app-debug.apk`.

## 8. Thành viên & đóng góp

| TV | Thành viên | Nhánh | Phần việc | Chi tiết | Trạng thái |
|---|---|---|---|---|---|
| **TV1** | **Phạm Tuấn Anh** · [@Drake-Phamta](https://github.com/Drake-Phamta) *(nhóm trưởng)* | `main` | **Nền tảng:** khởi tạo project và Gradle, SQLite 4 bảng + dữ liệu mẫu, 4 model, 4 DAO, `SessionManager` (SharedPreferences), `DateUtils` / `FormatUtils` / `PasswordUtils`, `BaseActivity` / `Navigator` / `Badges`, màn hình khung cho mọi module, `AndroidManifest`, CI, tài liệu. **Module Quản lý đơn hàng (Admin):** `OrderManagementActivity`, `AdminOrderAdapter`, lọc theo trạng thái, cập nhật Pending → Confirmed → Completed | [TV1](docs/tasks/TV1_DrakePhamta_core_orders.md) | ✅ Hoàn thành |
| **TV2** | **Vu Hung Anh** · [@scalliontor](https://github.com/scalliontor) | `feature/auth` | Đăng nhập 2 vai trò, Remember me và tự đăng nhập bằng SharedPreferences, Đăng ký khách hàng, Đăng xuất, màn hình chính Admin (thống kê + điều hướng) | [TV2](docs/tasks/TV2_scalliontor_auth.md) | ✅ Hoàn thành ([PR #1](https://github.com/Drake-Phamta/th02_group4_clc/pull/1)) |
| **TV3** | [@TrungNGD195](https://github.com/TrungNGD195) | `feature/admin-fruit` | Admin quản lý trái cây: thêm, sửa, xóa, tìm kiếm, xem chi tiết; quản lý hạn sử dụng (badge hết hạn / sắp hết hạn, lọc trái cây hết hạn) | [TV3](docs/tasks/TV3_TrungNGD195_admin_fruit.md) | ⏳ Đang làm |
| **TV4** | **Quoc Khanh** · [@qkhanhbe](https://github.com/qkhanhbe) | `feature/customer` | Khách hàng: xem và tìm trái cây còn hàng, kiểm tra hạn dùng, đặt hàng (chặn trái cây hết hạn hoặc vượt tồn kho), lịch sử đơn hàng | [TV4](docs/tasks/TV4_qkhanhbe_customer.md) | ✅ Hoàn thành ([PR #3](https://github.com/Drake-Phamta/th02_group4_clc/pull/3)) |

## 9. Quy trình làm việc nhóm

1. Nhóm trưởng dựng nền tảng trên `main`: CSDL, DAO, SharedPreferences, tiện ích, manifest và **màn hình khung** cho mọi module. Nhờ vậy app luôn build được và chạy thử được ngay từ đầu.
2. Mỗi thành viên làm trên **nhánh riêng** và chỉ sửa các file thuộc phạm vi của mình, nên khi merge không xảy ra xung đột.
3. Làm xong thì mở **Pull Request** vào `main`. CI **Android CI** phải xanh (build + test) thì nhóm trưởng mới review và merge.

Quy tắc chi tiết và các lệnh Git cần dùng: **[docs/PHAN_CONG.md](docs/PHAN_CONG.md)**.

> ℹ️ Cột **Trạng thái** ở mục 8 do **nhóm trưởng** cập nhật sau khi merge từng PR. Thành viên **không** tự sửa README để tránh xung đột khi merge.
