# Phân công công việc — Nhóm 4 · Fruit Shop Management

Nền tảng chung đã có sẵn trên `main`: CSDL, DAO, SharedPreferences, tiện ích, manifest và màn hình khung. Mỗi thành viên chỉ cần **thay màn hình khung của mình bằng màn hình thật**, trên nhánh riêng.

## 1. Bảng phân công

| TV | Thành viên | Module | Nhánh | File task | Mục đề bài đáp ứng |
|---|---|---|---|---|---|
| TV1 | Phạm Tuấn Anh · @Drake-Phamta (nhóm trưởng) | Nền tảng + Quản lý đơn hàng (Admin) | `main` | [TV1](tasks/TV1_DrakePhamta_core_orders.md) | Data Storage (SQLite 4 bảng), SharedPreferences (`SessionManager`), quy tắc hạn sử dụng, Order Management (Admin cập nhật trạng thái), Version Control |
| TV2 | Vu Hung Anh · @scalliontor | Xác thực + Admin Home | `feature/auth` | [TV2](tasks/TV2_scalliontor_auth.md) | Login 2 vai trò, Login Account Storage (Remember me, tự đăng nhập), Đăng ký, Đăng xuất |
| TV3 | @TrungNGD195 | Admin quản lý trái cây | `feature/admin-fruit` | [TV3](tasks/TV3_TrungNGD195_admin_fruit.md) | Admin Functions (thêm/sửa/xóa/tìm/xem), Expiry Date Management (hiển thị và nhận diện hết hạn) |
| TV4 | Quoc Khanh · @qkhanhbe | Chức năng khách hàng | `feature/customer` | [TV4](tasks/TV4_qkhanhbe_customer.md) | Customer Functions (xem, tìm, kiểm tra hạn, đặt hàng, lịch sử), chặn đặt trái cây hết hạn, Order Management (phía khách) |

## 2. Quy tắc để merge không xung đột ⚠️

1. **Chỉ sửa hoặc tạo các file thuộc phạm vi của mình**, theo danh sách trong file task.
2. **Tuyệt đối không sửa** các file sau. Nếu cần thay đổi, hãy báo nhóm trưởng (TV1):
   - `app/src/main/AndroidManifest.xml` (mọi Activity đã được khai báo sẵn)
   - `build.gradle.kts`, `app/build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`
   - `data/**` (`DatabaseHelper`, `SeedData`, DAO, model), `session/**`, `util/**`, `ui/common/**`
   - `res/values/strings.xml`, `colors.xml`, `themes.xml`
   - `README.md`, `docs/**` ngoài file task của mình
3. **String** chỉ thêm vào file của mình, và **tên phải có tiền tố**:

   | TV | File | Tiền tố tên |
   |---|---|---|
   | TV2 | `res/values/strings_auth.xml` | `auth_` hoặc `admin_home_` |
   | TV3 | `res/values/strings_admin_fruit.xml` | `fruit_` |
   | TV4 | `res/values/strings_customer.xml` | `customer_` |

4. **Layout, drawable, menu mới** phải dùng đúng tên có trong file task; nếu tạo thêm thì đặt tiền tố module (`auth_`, `fruit_`, `customer_`). Trong Android, tên resource là toàn cục: hai người cùng tạo một tên sẽ khiến build lỗi *Duplicate resources* sau khi merge.
5. Mọi Activity **kế thừa `BaseActivity`** và gọi `requireRole(Constants.ROLE_...)` **đầu tiên** trong `onCreate` (trừ Login và Register).
6. Chữ trên giao diện viết bằng **tiếng Anh**, theo đúng thuật ngữ của đề (Pending, Confirmed, Completed, Expired…).
7. Ngày, giá tiền và badge **bắt buộc dùng hàm có sẵn**: `DateUtils` (định dạng ngày, kiểm tra hết hạn), `FormatUtils.formatPrice()`, `Badges.bindExpiry()`, `Badges.bindOrderStatus()`. Không tự viết lại.
8. Dữ liệu chỉ đọc/ghi qua **DAO** (`AccountDao`, `FruitDao`, `CustomerDao`, `OrderDao`). Không viết SQL trong Activity.

## 3. Chạy thử khi các module khác chưa xong

Các màn hình chưa làm hiện là **màn hình khung** (`StubScreen`) có nút điều hướng sẵn. Màn hình Login khung có 2 nút **"Demo: log in as admin"** và **"Demo: log in as customer"**, nên mỗi người có thể chạy app và vào thẳng màn hình của mình để test, không cần chờ người khác.

## 4. Quy trình Git cho thành viên

```bash
# 1. Lấy code (một lần)
git clone https://github.com/Drake-Phamta/th02_group4_clc.git
cd th02_group4_clc

# 2. Chuyển sang nhánh của mình (đã được tạo sẵn)
git checkout feature/auth            # TV2
git checkout feature/admin-fruit     # TV3
git checkout feature/customer        # TV4

# 3. Code + commit nhỏ, thường xuyên
git add .
git commit -m "feat(auth): login screen with remember me"
git push

# 4. Trước khi mở PR: cập nhật main mới nhất vào nhánh của mình
git pull origin main
gradlew.bat assembleDebug testDebugUnitTest   # phải BUILD SUCCESSFUL
git push
```

5. Trên GitHub, mở **Pull Request** từ nhánh của mình vào `main`, ghi rõ đã làm gì và kèm ảnh chụp màn hình.
6. CI **"Android CI"** phải **xanh ✅** (build + test). Nếu đỏ thì sửa trên nhánh rồi push lại.
7. **Nhóm trưởng review và merge.** Thành viên không tự merge vào `main`.

Gợi ý tiền tố commit: `feat(auth)`, `feat(fruit)`, `feat(customer)`, `fix(...)`, `style(...)`.

## 5. Thứ tự merge

`feature/auth` → `feature/admin-fruit` → `feature/customer`

Do các nhánh sửa những file **hoàn toàn tách biệt**, thực tế merge theo thứ tự nào cũng được, ai xong trước thì merge trước. Sau mỗi lần merge, CI trên `main` sẽ chạy lại để xác nhận.

## 6. Definition of Done (checklist trước khi mở PR)

- [ ] Làm đủ mọi mục trong phần **"Tiêu chí nghiệm thu"** của file task
- [ ] Chỉ thay đổi file thuộc phạm vi của mình (kiểm tra bằng `git status` hoặc tab *Files changed* của PR)
- [ ] Tên string, layout, drawable mới có tiền tố module
- [ ] Activity kế thừa `BaseActivity`, gọi `requireRole(...)` đầu `onCreate`
- [ ] Dùng `DateUtils` / `FormatUtils` / `Badges` cho ngày, giá tiền và badge
- [ ] `gradlew.bat assembleDebug testDebugUnitTest` → **BUILD SUCCESSFUL**
- [ ] Đã chạy app thật hoặc máy ảo, test bằng tài khoản demo (`admin/admin123`, `customer/customer123`)
- [ ] Đã `git pull origin main` trước khi mở PR; CI "Android CI" xanh
