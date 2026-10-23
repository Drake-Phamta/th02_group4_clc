# TV1 — Phạm Tuấn Anh (@Drake-Phamta) · Nhóm trưởng

**Phạm vi:** Nền tảng dùng chung + module **Quản lý đơn hàng (Admin)** + tích hợp và merge · **Nhánh:** `main`

## 1. Đã bàn giao ✅

### Project & cấu hình
- `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, Gradle wrapper (8.14), `.gitignore`, `.gitattributes`
- `app/src/main/AndroidManifest.xml`: khai báo **tất cả** Activity của mọi module

### Dữ liệu (SQLite, đúng 4 bảng của đề)
- `data/DatabaseHelper.java`: tạo `Account`, `Fruit`, `Customer`, `"Order"`, có ràng buộc CHECK, UNIQUE và khóa ngoại
- `data/SeedData.java`: 3 tài khoản demo, 11 trái cây (có loại hết hạn, sắp hết hạn, hết hàng), 4 đơn hàng
- `data/model/`: `Account`, `Fruit`, `Customer`, `Order`
- `data/dao/`:
  - `AccountDao`: `login`, `registerCustomer`, `isUsernameTaken`
  - `FruitDao`: CRUD, `search`, `searchInStock`, `getExpired`, `countExpired`
  - `CustomerDao`
  - `OrderDao`: `placeOrder` chặn hàng hết hạn và vượt tồn kho, `getByCustomer`, `getAllWithCustomer`, `updateStatus`, `countByStatus`

### SharedPreferences & tiện ích
- `session/SessionManager.java`: phiên đăng nhập, Remember me, `shouldAutoLogin()`
- `util/`: `Constants`, `OrderStatus`, `DateUtils` (quy tắc hết hạn), `FormatUtils`, `PasswordUtils` (SHA-256)
- `ui/common/`: `BaseActivity` (`requireRole`, action bar, toast), `Navigator` (`openHome`, `logout`), `Badges` (badge hạn dùng và trạng thái đơn), `StubScreen` (màn hình khung)

### Module Quản lý đơn hàng (Admin)
- `ui/admin/order/OrderManagementActivity.java`: danh sách mọi đơn kèm tên và SĐT khách, chip lọc **All / Pending / Confirmed / Completed** (có số lượng), tổng tiền, xác nhận trước khi đổi trạng thái
- `ui/admin/order/AdminOrderAdapter.java`: nút **Confirm order** (Pending → Confirmed) và **Mark completed** (Confirmed → Completed)
- `res/layout/activity_order_management.xml`, `res/layout/item_admin_order.xml`, `res/values/strings_admin_order.xml`

### Màn hình khung cho các thành viên
- 8 Activity khung (Login, Register, AdminHome, FruitManagement, FruitForm, CustomerHome, FruitDetail, OrderHistory), giúp app luôn build và điều hướng được trước khi các module được merge
- `res/layout/activity_stub.xml`, `res/layout/item_stub_action.xml`, các file `strings_<module>.xml` rỗng cho từng thành viên

### Chất lượng & cộng tác
- Test (JUnit + Robolectric) — **đang bổ sung**: DAO và nghiệp vụ, `SessionManager`, `DateUtils`, mở thử từng màn hình với đúng vai trò
- `.github/workflows/android-ci.yml`: build và test cho mọi push hoặc PR vào `main`
- `.github/CODEOWNERS`, `.github/pull_request_template.md`
- Tài liệu: `README.md`, `docs/SPEC.md`, `docs/PHAN_CONG.md`, `docs/tasks/*`

## 2. Việc còn lại của nhóm trưởng ⏳

- [ ] Gửi link repo và file task cho từng thành viên; nhắc @qkhanhbe **chấp nhận lời mời collaborator**
- [ ] Review từng PR: chỉ sửa file trong phạm vi, CI xanh, chạy thử đúng tiêu chí nghiệm thu
- [ ] Merge PR vào `main` (gợi ý: `feature/auth` → `feature/admin-fruit` → `feature/customer`)
- [ ] Sau mỗi lần merge: cập nhật cột **Trạng thái** trong `README.md` (⏳ → ✅, kèm link PR)
- [ ] Test tổng thể trên máy ảo: đăng nhập 2 vai trò, Remember me, CRUD trái cây, chặn đặt hàng hết hạn, đặt hàng, cập nhật trạng thái đơn, lịch sử đơn
- [ ] *(Tùy chọn, sau khi đã merge đủ 3 module)* dọn màn hình khung: xóa `ui/common/StubScreen.java`, `res/layout/activity_stub.xml`, `res/layout/item_stub_action.xml` và string `stub_owner` khi không còn nơi nào dùng
