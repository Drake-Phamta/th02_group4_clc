# TV2 — Vu Hung Anh (@scalliontor) · Xác thực & Màn hình chính Admin

| | |
|---|---|
| **Nhánh** | `feature/auth` |
| **Package** | `com.group4.fruitshop.ui.auth`, `com.group4.fruitshop.ui.admin` (chỉ `AdminHomeActivity`) |
| **Đặc tả phụ trách** | *Login* (2 vai trò Admin/Customer, chuyển tới chức năng tương ứng) · *Login Account Storage* (SharedPreferences) · Đăng ký khách hàng · Đăng xuất · Trang chủ Admin |
| **Ngôn ngữ UI** | Tiếng Anh (giống đặc tả). Mọi chuỗi đặt trong `strings_auth.xml` |

---

## 1. Bắt đầu (5 phút)

```bash
git clone https://github.com/Drake-Phamta/th02_group4_clc.git
cd th02_group4_clc
git checkout feature/auth
```

1. Mở thư mục bằng **Android Studio** → chờ Gradle Sync xong.
2. Bấm **Run ▶**. Màn Login hiện tại là **stub** có nút *Demo: log in as admin / customer* để đi được hết app. Nhiệm vụ của bạn là **thay toàn bộ** stub đó bằng màn thật.
3. Đọc lướt mục **API lõi** bên dưới và Javadoc trong `data/dao/`, `session/SessionManager.java`, `ui/common/` trước khi code.

---

## 2. File bạn sở hữu (chỉ được sửa/tạo các file này)

| Loại | File | Việc cần làm |
|---|---|---|
| Java | `app/src/main/java/com/group4/fruitshop/ui/auth/LoginActivity.java` | Thay stub |
| Java | `app/src/main/java/com/group4/fruitshop/ui/auth/RegisterActivity.java` | Thay stub |
| Java | `app/src/main/java/com/group4/fruitshop/ui/admin/AdminHomeActivity.java` | Thay stub |
| Layout | `app/src/main/res/layout/activity_login.xml` | Tạo mới |
| Layout | `app/src/main/res/layout/activity_register.xml` | Tạo mới |
| Layout | `app/src/main/res/layout/activity_admin_home.xml` | Tạo mới |
| Menu (tuỳ chọn) | `app/src/main/res/menu/menu_admin_home.xml` | Tạo mới nếu dùng menu Logout |
| Strings | `app/src/main/res/values/strings_auth.xml` | Thêm chuỗi, **tên bắt đầu bằng `auth_` hoặc `admin_home_`** |
| Drawable (tuỳ chọn) | `app/src/main/res/drawable/auth_*.xml` | Tên **bắt buộc** có tiền tố `auth_` |

**Chỉ đọc, KHÔNG sửa:** `AndroidManifest.xml`, mọi file Gradle, `data/`, `session/`, `util/`, `ui/common/`, `values/strings.xml`, `colors.xml`, `themes.xml`, file của thành viên khác, `README.md`. Cần thay đổi gì ở các file này → nhắn nhóm trưởng (@Drake-Phamta).

> Lý do: mỗi người chỉ đụng file của mình ⇒ merge vào `main` **không bao giờ xung đột**. Tên resource trùng nhau (kể cả ở file khác) sẽ làm **build lỗi** sau khi merge, nên tiền tố là bắt buộc.

---

## 3. API lõi được dùng (đã có sẵn trên `main`)

```java
// data.dao.AccountDao  — truyền mật khẩu THÔ, DAO tự băm SHA-256
Account login(String username, String rawPassword);          // sai → null
boolean isUsernameTaken(String username);                    // không phân biệt hoa/thường
int registerCustomer(String username, String rawPassword,
                     String fullname, String phone, String email); // → customerId, hoặc Constants.NO_ID nếu lỗi/trùng

// data.dao.CustomerDao
Customer getByAccountId(int accountId);                      // null nếu không có hồ sơ

// session.SessionManager  (SharedPreferences)
void createSession(Account account, int customerId);         // admin → Constants.NO_ID
void setRememberMe(boolean remember, String username, String password); // false = xoá thông tin đã lưu
boolean isRememberMe();  String getSavedUsername();  String getSavedPassword();
boolean shouldAutoLogin();                                   // isLoggedIn() && isRememberMe()
boolean isLoggedIn();  void logout();  String getUsername();

// ui.common.Navigator
Navigator.openHome(activity);   // Admin → AdminHomeActivity, Customer → CustomerHomeActivity, xoá back stack
Navigator.logout(activity);     // xoá session → LoginActivity, xoá back stack

// ui.common.BaseActivity (mọi Activity phải extends)
protected SessionManager session;
protected boolean requireRole(String role);                  // false → đã tự chuyển về Login, phải return ngay
protected void setupActionBar(@StringRes int title, boolean showBack);
protected void toast(@StringRes int msg);  protected void toast(String msg);

// Thống kê cho Admin home
new FruitDao(this).countAll();   new FruitDao(this).countExpired();
new OrderDao(this).countByStatus(OrderStatus.PENDING);   new OrderDao(this).countAll();

// util.Constants
Constants.ROLE_ADMIN = "Admin";  Constants.ROLE_CUSTOMER = "Customer";  Constants.NO_ID = -1;
```

Tài khoản demo (seed): `admin / admin123` (Admin), `customer / customer123`, `customer2 / customer123` (Customer).

---

## 4. Đặc tả từng màn hình

### 4.1 `LoginActivity` (màn khởi động — LAUNCHER)

**Giao diện** (`activity_login.xml`, nên bọc `ScrollView`):
- Logo/tên app ("Fruit Shop").
- `TextInputLayout` + `TextInputEditText` **Username**.
- `TextInputLayout` **Password**, `app:endIconMode="password_toggle"`, `inputType="textPassword"`.
- `CheckBox` **Remember me**.
- `MaterialButton` **Login**.
- Text/link **"Don't have an account? Register"** → mở `RegisterActivity`.

**Logic `onCreate`** (theo đúng thứ tự):
1. `super.onCreate(...)`.
2. `if (session.shouldAutoLogin()) { Navigator.openHome(this); return; }` → tự đăng nhập.
3. `else if (session.isLoggedIn()) session.logout();` → phiên cũ không "remember" thì bắt đăng nhập lại.
4. `setContentView(...)`. Nếu `session.isRememberMe()` → điền sẵn `getSavedUsername()` / `getSavedPassword()` và tick checkbox.

**Khi bấm Login:**
1. Kiểm tra rỗng → `til.setError("...")` trên đúng ô (xoá lỗi cũ trước khi kiểm tra lại).
2. `Account acc = accountDao.login(u, p);` → `null` thì báo *"Invalid username or password"*.
3. Nếu là Customer: `Customer c = customerDao.getByAccountId(acc.getId());` → `null` thì báo lỗi hồ sơ, dừng. `customerId = c.getId()`. Admin: `customerId = Constants.NO_ID`.
4. `session.createSession(acc, customerId);`
5. `session.setRememberMe(cbRemember.isChecked(), u, p);`
6. `Navigator.openHome(this);`

### 4.2 `RegisterActivity` (đăng ký tài khoản **Customer**)

**Giao diện:** Full name, Username, Password, Confirm password, Phone (tuỳ chọn), Email (tuỳ chọn), nút **Register**; `setupActionBar(R.string.auth_register_title, true)` để có nút back.

**Validate** (lỗi hiển thị bằng `setError` trên `TextInputLayout`):
| Trường | Quy tắc |
|---|---|
| Full name | bắt buộc |
| Username | ≥ 3 ký tự, **không chứa khoảng trắng**, chưa tồn tại (`isUsernameTaken`) |
| Password | ≥ 6 ký tự |
| Confirm | trùng Password |
| Phone | nếu nhập: khớp `^0\d{9}$` |
| Email | nếu nhập: `Patterns.EMAIL_ADDRESS.matcher(email).matches()` |

**Khi hợp lệ:** `int id = accountDao.registerCustomer(u, p, name, phone, email);`
- `id == Constants.NO_ID` → báo *"Username already exists"* (hoặc lỗi chung).
- Thành công → `toast("Account created, please log in")` → `finish()` quay về Login.

### 4.3 `AdminHomeActivity` (trang chủ Admin)

1. Đầu `onCreate`: `if (!requireRole(Constants.ROLE_ADMIN)) return;`
2. Lời chào: *"Welcome, {session.getUsername()}"*.
3. **3 thẻ thống kê** (`MaterialCardView`): *Total fruits* (`countAll`), *Expired fruits* (`countExpired`, màu `@color/status_expired`), *Pending orders* (`countByStatus(OrderStatus.PENDING)`). **Cập nhật trong `onResume()`** để quay lại là số liệu mới.
4. Nút **Fruit management** → `startActivity(new Intent(this, FruitManagementActivity.class))`.
5. Nút **Order management** → `startActivity(new Intent(this, OrderManagementActivity.class))`.
6. **Logout** (menu trên action bar `menu_admin_home.xml` hoặc nút) → `MaterialAlertDialogBuilder` xác nhận → `Navigator.logout(this)`.

---

## 5. Tiêu chí nghiệm thu (tự test hết trước khi mở PR)

- [ ] `admin / admin123` → vào **Admin home**, thấy 3 số liệu đúng (seed: 11 fruits, 2 expired, 2 pending).
- [ ] `customer / customer123` → vào **CustomerHomeActivity**.
- [ ] Sai mật khẩu / để trống → báo lỗi, không crash.
- [ ] Tick *Remember me* → đăng nhập → **tắt hẳn app** (vuốt khỏi recent) → mở lại → **tự vào trang chủ** đúng vai trò.
- [ ] Không tick → tắt app → mở lại → hiện màn **Login**.
- [ ] Đã tick rồi Logout → màn Login **điền sẵn** username/password, checkbox đang tick.
- [ ] Đăng ký tài khoản mới → đăng nhập bằng tài khoản đó → vào Customer home. Đăng ký trùng username → báo lỗi.
- [ ] Ở Admin home bấm **Back** → thoát app, **không** quay lại màn Login.
- [ ] Từ Admin home mở được Fruit management và Order management; Logout có hộp thoại xác nhận.
- [ ] `gradlew assembleDebug testDebugUnitTest` thành công, **CI trên PR xanh**.

---

## 6. Commit & Pull Request

```bash
git add app/src/main/java/com/group4/fruitshop/ui/auth app/src/main/java/com/group4/fruitshop/ui/admin/AdminHomeActivity.java app/src/main/res
git commit -m "feat(auth): login with Admin/Customer roles and remember me"
git push origin feature/auth
```

- Commit nhỏ, rõ ràng: `feat(auth): ...`, `fix(auth): ...`.
- Trước khi mở PR: `git fetch origin && git merge origin/main` (sẽ không có xung đột nếu chỉ sửa file của mình).
- Mở **Pull Request `feature/auth` → `main`** trên GitHub, điền template, đính kèm ảnh chụp màn hình Login/Register/Admin home.
- `git status` trước khi commit: **không được** có file ngoài danh sách mục 2 (nhất là `.idea/`, `local.properties`, `AndroidManifest.xml`).
