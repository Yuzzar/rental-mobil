# 📚 Panduan Belajar: Membuat Aplikasi Rental Mobil dari Nol

Dokumen ini menjelaskan **urutan lengkap** cara membangun aplikasi Rental Mobil
dengan Spring Boot (backend) + Vite React (frontend), step by step.

---

## 🗺️ Overview Arsitektur

```
┌──────────────────┐         ┌──────────────────────────────────────┐
│   FRONTEND       │  HTTP   │          BACKEND (Spring Boot)       │
│   (Vite React)   │ ◄─────► │                                      │
│                  │  JSON   │  Controller → Service → Repository   │
│  localhost:5173  │         │         ↓          ↓         ↓       │
│                  │         │       DTO      Logic     Database    │
└──────────────────┘         └──────────────────────────────────────┘
                                         │
                                         ▼
                              ┌──────────────────┐
                              │   PostgreSQL DB   │
                              │   rent_car_db     │
                              └──────────────────┘
```

**Alur data:**
1. User klik tombol di Frontend → kirim HTTP request (JSON) ke Backend
2. Backend Controller terima request → validasi → panggil Service
3. Service jalankan business logic → panggil Repository
4. Repository query ke Database → return data
5. Data dikembalikan ke Controller → diubah ke DTO → dikirim sebagai JSON response
6. Frontend terima response → tampilkan di UI

---

## 🔢 URUTAN NGODING (Step by Step)

### ═══════════════════════════════════════
### TAHAP 1: SETUP PROJECT BACKEND
### ═══════════════════════════════════════

#### Step 1.1: Buat Project Spring Boot
1. Buka https://start.spring.io
2. Pilih:
   - **Project:** Maven
   - **Language:** Java
   - **Spring Boot:** 3.2.x
   - **Group:** com.rentcar
   - **Artifact:** backend-rent-car
   - **Java:** 21
3. Tambah Dependencies:
   - Spring Web
   - Spring Data JPA
   - Spring Security
   - Validation
   - PostgreSQL Driver
   - Lombok
   - Spring AOP
4. Download & extract ke folder project

#### Step 1.2: Tambah dependency JWT di `pom.xml`
JWT tidak ada di Spring Initializr, jadi tambah manual:

```xml
<!-- di dalam <dependencies> -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

**Kenapa perlu JWT?** Karena kita pakai REST API (stateless), jadi butuh token
untuk mengenali user di setiap request. Beda dengan web biasa yang pakai session.

#### Step 1.3: Konfigurasi `application.properties`
```properties
# Koneksi ke PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/rent_car_db
spring.datasource.username=postgres
spring.datasource.password=PASSWORD_KAMU

# JPA akan otomatis buat/update tabel berdasarkan Entity
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT secret key (untuk sign token)
app.jwt.secret=SECRET_KEY_YANG_PANJANG_DAN_AMAN
app.jwt.expiration-ms=86400000

# CORS (izinkan frontend akses backend)
app.cors.allowed-origins=http://localhost:5173
```

#### Step 1.4: Buat Database PostgreSQL
```sql
CREATE DATABASE rent_car_db;
```

> 💡 **Tips:** `ddl-auto=update` artinya Hibernate akan otomatis buat tabel
> berdasarkan class Entity kamu. Jadi tidak perlu buat tabel manual!

---

### ═══════════════════════════════════════
### TAHAP 2: ENUM (Buat Dulu yang Paling Sederhana)
### ═══════════════════════════════════════

**Kenapa enum duluan?** Karena Entity akan pakai enum ini, jadi harus ada dulu.

#### Step 2.1: `enums/UserRole.java`
```java
public enum UserRole {
    ADMIN,  // Bisa CRUD semua
    USER    // Hanya bisa rental
}
```

#### Step 2.2: `enums/CarStatus.java`
```java
public enum CarStatus {
    AVAILABLE,    // Bisa dirental
    RENTED,       // Sedang dirental
    MAINTENANCE   // Sedang diperbaiki
}
```

#### Step 2.3: `enums/RentalStatus.java`
```java
public enum RentalStatus {
    PENDING,    // Baru dibuat, belum di-approve
    APPROVED,   // Admin sudah approve
    ACTIVE,     // Sedang berjalan
    COMPLETED,  // Sudah selesai
    CANCELLED,  // Dibatalkan user
    REJECTED    // Ditolak admin
}
```

> 💡 **Kenapa pakai Enum?** Supaya status terbatas dan terkontrol.
> Kalau pakai String biasa, bisa typo ("available" vs "avaiable").

---

### ═══════════════════════════════════════
### TAHAP 3: ENTITY (Definisikan Tabel Database)
### ═══════════════════════════════════════

**Entity = representasi tabel di database dalam bentuk Java class.**
Hibernate akan otomatis buat tabel berdasarkan class ini.

**Urutan buat Entity:**
1. `User` (tidak tergantung entity lain)
2. `Car` (tidak tergantung entity lain)
3. `Rental` (tergantung User & Car → pakai `@ManyToOne`)
4. `ActivityLog` (standalone)

#### Step 3.1: `entity/User.java`
```java
@Entity                    // Tandai ini adalah tabel database
@Table(name = "users")     // Nama tabel di database
@Getter @Setter            // Lombok: auto-generate getter/setter
@NoArgsConstructor         // Lombok: constructor kosong (wajib untuk JPA)
@AllArgsConstructor        // Lombok: constructor semua field
@Builder                   // Lombok: builder pattern
public class User {

    @Id                                          // Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto increment
    private Long id;

    @Column(nullable = false, unique = true)     // NOT NULL, UNIQUE
    private String username;

    @Column(nullable = false)
    private String password;                     // Disimpan dalam bentuk HASH

    @Enumerated(EnumType.STRING)                 // Simpan "ADMIN"/"USER" (bukan angka)
    private UserRole role;

    private boolean active = true;               // Untuk soft delete

    @CreationTimestamp                           // Otomatis isi saat INSERT
    private LocalDateTime createdAt;

    @UpdateTimestamp                             // Otomatis isi saat UPDATE
    private LocalDateTime updatedAt;
}
```

**Penjelasan Annotation:**
| Annotation | Fungsi |
|-----------|--------|
| `@Entity` | Bilang ke JPA "ini tabel database" |
| `@Id` | Primary Key |
| `@GeneratedValue(IDENTITY)` | Auto increment |
| `@Column(unique = true)` | Tidak boleh duplikat |
| `@Enumerated(STRING)` | Simpan enum sebagai text, bukan angka |
| `@ManyToOne` | Relasi: banyak rental → 1 user |
| `@CreationTimestamp` | Auto isi waktu saat record dibuat |

#### Step 3.2: `entity/Car.java`
Sama seperti User, tapi dengan field untuk data mobil (brand, model, dailyRate, status, dll).

#### Step 3.3: `entity/Rental.java`
```java
@ManyToOne(fetch = FetchType.LAZY)   // Relasi ke User
@JoinColumn(name = "user_id")       // Foreign Key column
private User user;

@ManyToOne(fetch = FetchType.LAZY)   // Relasi ke Car
@JoinColumn(name = "car_id")
private Car car;
```

> 💡 **`FetchType.LAZY`** = data User/Car tidak diambil sampai dipanggil.
> Ini lebih efisien daripada `EAGER` yang selalu ambil semua data.

#### Step 3.4: `entity/ActivityLog.java`
Tabel terpisah untuk mencatat semua aktivitas (log audit).

---

### ═══════════════════════════════════════
### TAHAP 4: REPOSITORY (Akses Database)
### ═══════════════════════════════════════

**Repository = interface yang menyediakan method untuk query database.**
Spring Data JPA otomatis implementasi method-nya!

#### Step 4.1: `repository/UserRepository.java`
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring auto-implement query berdasarkan nama method!
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

> 💡 **Magic Spring Data JPA:**
> - `findByUsername` → `SELECT * FROM users WHERE username = ?`
> - `existsByEmail` → `SELECT COUNT(*) > 0 FROM users WHERE email = ?`
> - Tidak perlu tulis SQL! Spring baca nama method dan generate query otomatis.

#### Step 4.2: `repository/CarRepository.java` (PENTING: Pessimistic Lock)
```java
/**
 * PESSIMISTIC LOCK: Solusi untuk handle 2 user rental mobil yang sama.
 * 
 * Bagaimana cara kerjanya?
 * 
 * Tanpa lock:
 *   User A: cek mobil available ✓ → buat rental ✓
 *   User B: cek mobil available ✓ → buat rental ✓  ← MASALAH! Double booking!
 * 
 * Dengan lock:
 *   User A: LOCK mobil 🔒 → cek available ✓ → buat rental ✓ → UNLOCK 🔓
 *   User B: coba LOCK... MENUNGGU ⏳ → LOCK 🔒 → cek available ✗ → DITOLAK ❌
 */
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Car c WHERE c.id = :id")
Optional<Car> findByIdWithLock(@Param("id") Long id);
```

#### Step 4.3: `repository/RentalRepository.java` (Overlap Check)
```java
/**
 * Cek apakah ada rental yang bentrok tanggalnya.
 * Overlap terjadi jika: startBaru <= endLama AND endBaru >= startLama
 */
@Query("SELECT COUNT(r) > 0 FROM Rental r WHERE r.car.id = :carId " +
       "AND r.status NOT IN ('CANCELLED', 'REJECTED', 'COMPLETED') " +
       "AND r.startDate <= :endDate AND r.endDate >= :startDate")
boolean existsOverlappingRental(...);
```

#### Step 4.4: `repository/ActivityLogRepository.java`

---

### ═══════════════════════════════════════
### TAHAP 5: DTO (Data Transfer Object)
### ═══════════════════════════════════════

**DTO = object untuk kirim/terima data dari/ke frontend.**
Kenapa tidak pakai Entity langsung? Karena:
1. **Keamanan:** Entity User punya field `password`, tidak boleh dikirim ke frontend
2. **Fleksibilitas:** Response bisa berisi data dari beberapa entity
3. **Validasi:** DTO bisa pakai `@NotBlank`, `@Email` untuk validasi input

**Urutan buat:**
1. `ApiResponse<T>` — wrapper standar untuk semua response
2. `LoginRequest` / `LoginResponse`
3. `RegisterRequest`
4. `CarRequest` / `CarResponse`
5. `UserRequest` / `UserResponse`
6. `RentalRequest` / `RentalResponse`

#### Contoh: `ApiResponse.java`
```java
// Semua API response punya format yang sama:
// { "success": true, "message": "...", "data": { ... } }
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;         // <T> = Generic, bisa isi apa saja
}
```

#### Contoh: `RentalResponse.java`
```java
// Response rental berisi data dari 3 entity: Rental + User + Car
public class RentalResponse {
    private Long id;
    private String userName;         // dari User entity
    private String carBrand;         // dari Car entity
    private String carModel;         // dari Car entity
    private LocalDate startDate;     // dari Rental entity
    private BigDecimal totalCost;    // dari Rental entity
    private String status;           // dari Rental entity
}
```

---

### ═══════════════════════════════════════
### TAHAP 6: EXCEPTION HANDLING
### ═══════════════════════════════════════

**Buat custom exception + global handler SEBELUM service, supaya service bisa melempar exception.**

```java
// 1. Custom Exception
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}

// 2. Global Exception Handler
@RestControllerAdvice  // Tangkap SEMUA exception dari semua controller
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        // Kumpulkan semua validation error
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            errors.put(((FieldError) error).getField(), error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(ApiResponse.error("Validation error"));
    }
}
```

> 💡 **`@RestControllerAdvice`** = satu tempat untuk handle SEMUA error.
> Tanpa ini, kamu harus try-catch di setiap controller method.

---

### ═══════════════════════════════════════
### TAHAP 7: SECURITY (JWT Authentication)
### ═══════════════════════════════════════

**Ini bagian yang paling kompleks. Urutan file yang harus dibuat:**

#### Step 7.1: `security/JwtTokenProvider.java`
```java
// Tugas: Generate token, validasi token, extract username dari token
public String generateToken(Authentication auth) {
    return Jwts.builder()
            .subject(username)          // Simpan username di token
            .issuedAt(new Date())       // Waktu dibuat
            .expiration(expiryDate)     // Waktu expired (24 jam)
            .signWith(secretKey)        // Tanda tangan digital
            .compact();
}
```

#### Step 7.2: `security/CustomUserDetailsService.java`
```java
// Tugas: Load user dari database untuk Spring Security
// Dipanggil otomatis saat login dan saat validasi JWT
@Override
public UserDetails loadUserByUsername(String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(...));
    
    // Convert User entity ke Spring Security UserDetails
    return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
    );
}
```

#### Step 7.3: `security/JwtAuthenticationFilter.java`
```java
// Tugas: Intercept SETIAP request, cek apakah ada JWT token yang valid
// Alur: Request masuk → cek header Authorization → validasi token → set auth

@Override
protected void doFilterInternal(request, response, filterChain) {
    String token = getTokenFromHeader(request);  // "Bearer xxx" → "xxx"
    
    if (token != null && jwtProvider.validateToken(token)) {
        String username = jwtProvider.getUsernameFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        // Set authentication ke SecurityContext
        // Setelah ini, Spring Security tahu user ini sudah login
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
    
    filterChain.doFilter(request, response); // Lanjut ke controller
}
```

#### Step 7.4: `security/SecurityConfig.java`
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http
        .cors(...)                              // Izinkan frontend akses
        .csrf(csrf -> csrf.disable())           // Disable CSRF (pakai JWT)
        .sessionManagement(session ->
            session.sessionCreationPolicy(STATELESS)) // Tidak pakai session
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()     // Login/Register = publik
            .requestMatchers("/api/admin/**").hasRole("ADMIN") // Admin only
            .anyRequest().authenticated()                      // Sisanya perlu login
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
}
```

**Alur keseluruhan Security:**
```
Request masuk
    ↓
JwtAuthenticationFilter (cek token)
    ↓ token valid?
SecurityConfig (cek role & permission)
    ↓ authorized?
Controller (proses request)
```

---

### ═══════════════════════════════════════
### TAHAP 8: SERVICE (Business Logic)
### ═══════════════════════════════════════

**Service = tempat logika bisnis. Controller TIDAK BOLEH punya logic.**

**Urutan buat:**
1. `ActivityLogService` (paling simple, dipanggil service lain)
2. `AuthService` (login/register)
3. `CarService` (CRUD mobil)
4. `UserService` (CRUD user)
5. `RentalService` (paling kompleks)

#### Contoh Logika di `RentalService.createRental()`:
```java
@Transactional  // Semua dalam 1 transaksi database
public RentalResponse createRental(String username, RentalRequest request) {
    // 1. Ambil data user
    User user = userRepository.findByUsername(username).orElseThrow(...);

    // 2. Validasi tanggal
    if (startDate.isBefore(LocalDate.now())) throw new BadRequestException(...);

    // 3. 🔒 PESSIMISTIC LOCK pada mobil
    //    Thread lain yang akses mobil ini akan MENUNGGU di sini
    Car car = carRepository.findByIdWithLock(request.getCarId()).orElseThrow(...);

    // 4. Cek overlap tanggal → mencegah double booking
    boolean hasOverlap = rentalRepository.existsOverlappingRental(carId, startDate, endDate);
    if (hasOverlap) throw new CarNotAvailableException("Mobil sudah dirental di tanggal tersebut");

    // 5. Hitung total biaya
    long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
    BigDecimal totalCost = car.getDailyRate().multiply(BigDecimal.valueOf(days));

    // 6. Simpan rental dengan status PENDING
    Rental rental = Rental.builder()
            .user(user).car(car)
            .startDate(startDate).endDate(endDate)
            .totalCost(totalCost).status(RentalStatus.PENDING)
            .build();
    rentalRepository.save(rental);

    // 7. Log aktivitas
    activityLogService.log(username, "USER", "CREATE_RENTAL", "Rental", rental.getId(), "...");

    // 8. Convert ke DTO dan return
    return mapToResponse(rental);
}
```

---

### ═══════════════════════════════════════
### TAHAP 9: CONTROLLER (REST Endpoints)
### ═══════════════════════════════════════

**Controller = pintu masuk HTTP request. Tugasnya HANYA:**
1. Terima request
2. Panggil service
3. Return response

```java
@RestController                          // Ini REST controller (return JSON)
@RequestMapping("/api/rentals")
@RequiredArgsConstructor                 // Lombok: auto-inject dependency
public class RentalController {

    private final RentalService rentalService;  // Dependency injection

    @PostMapping                         // POST /api/rentals
    public ResponseEntity<ApiResponse<RentalResponse>> createRental(
            @Valid @RequestBody RentalRequest request,  // @Valid = validasi input
            Authentication authentication) {            // JWT user info
        
        // Controller hanya panggil service, tidak ada logic di sini
        RentalResponse rental = rentalService.createRental(
                authentication.getName(), request);
        
        return ResponseEntity.ok(ApiResponse.success("Rental berhasil", rental));
    }
}
```

**Mapping Annotation:**
| Annotation | HTTP Method | Contoh |
|-----------|-------------|--------|
| `@GetMapping` | GET | Ambil data |
| `@PostMapping` | POST | Buat data baru |
| `@PutMapping` | PUT | Update data |
| `@DeleteMapping` | DELETE | Hapus data |

---

### ═══════════════════════════════════════
### TAHAP 10: DATA SEEDER
### ═══════════════════════════════════════

```java
@Component  // Spring auto-detect
public class DataSeeder implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        // Dijalankan otomatis saat aplikasi start
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123")) // HASH password!
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
        }
    }
}
```

> ⚠️ **JANGAN PERNAH simpan password plain text!** Selalu hash dengan BCrypt.

---

### ═══════════════════════════════════════
### TAHAP 11: TEST BACKEND
### ═══════════════════════════════════════

Jalankan: `.\mvnw.cmd spring-boot:run`

Test pakai Postman/browser:
1. `POST /api/auth/login` → dapatkan JWT token
2. Pakai token di header: `Authorization: Bearer <token>`
3. Test semua endpoint

---

### ═══════════════════════════════════════
### TAHAP 12: SETUP FRONTEND (Vite + React)
### ═══════════════════════════════════════

```bash
npx create-vite@latest frontend-rent-car --template react
cd frontend-rent-car
npm install
npm install react-router-dom axios
```

**Library yang dipakai:**
| Library | Fungsi |
|---------|--------|
| `react-router-dom` | Routing halaman (SPA) |
| `axios` | HTTP client (lebih bagus dari fetch) |

---

### ═══════════════════════════════════════
### TAHAP 13: API CLIENT (Frontend)
### ═══════════════════════════════════════

```javascript
// src/api/api.js
const api = axios.create({ baseURL: 'http://localhost:8080/api' });

// Interceptor: OTOMATIS tambahkan JWT token ke setiap request
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});
```

> 💡 **Interceptor** = middleware yang jalan SEBELUM setiap request dikirim.
> Jadi tidak perlu manual set header di setiap API call.

---

### ═══════════════════════════════════════
### TAHAP 14: AUTH CONTEXT (State Management)
### ═══════════════════════════════════════

```javascript
// src/context/AuthContext.jsx
// Menyimpan state login user secara global (bisa diakses dari semua component)

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);

    const login = (data) => {
        localStorage.setItem('token', data.token);  // Simpan token
        setUser(data);
    };

    const logout = () => {
        localStorage.removeItem('token');            // Hapus token
        setUser(null);
    };

    const isAdmin = () => user?.role === 'ADMIN';

    return (
        <AuthContext.Provider value={{ user, login, logout, isAdmin }}>
            {children}
        </AuthContext.Provider>
    );
}
```

---

### ═══════════════════════════════════════
### TAHAP 15: ROUTING & PROTECTED ROUTES
### ═══════════════════════════════════════

```javascript
// App.jsx — Setup semua route
<Routes>
    {/* Public */}
    <Route path="/login" element={<Login />} />

    {/* User — perlu login */}
    <Route path="/cars" element={
        <ProtectedRoute><CarCatalog /></ProtectedRoute>
    } />

    {/* Admin — perlu login + role ADMIN */}
    <Route path="/admin/cars" element={
        <ProtectedRoute adminOnly><CarManagement /></ProtectedRoute>
    } />
</Routes>
```

---

### ═══════════════════════════════════════
### TAHAP 16: HALAMAN-HALAMAN (Pages)
### ═══════════════════════════════════════

**Urutan buat halaman:**
1. **Login.jsx** (halaman pertama yang dilihat user)
2. **Register.jsx**
3. **Admin: CarManagement.jsx** (CRUD mobil, pakai modal untuk form)
4. **Admin: UserManagement.jsx** (CRUD user)
5. **Admin: RentalManagement.jsx** (approve/reject/complete)
6. **Admin: ActivityLog.jsx** (lihat log)
7. **User: CarCatalog.jsx** (browse + filter + rental)
8. **User: MyRentals.jsx** (riwayat rental)

**Pola CRUD di React:**
```javascript
// State
const [items, setItems] = useState([]);       // Data dari API
const [showModal, setShowModal] = useState(false);  // Toggle form modal
const [form, setForm] = useState({...});      // Form values

// Fetch data saat component mount
useEffect(() => { fetchData(); }, []);

// CRUD functions
const handleCreate = async () => { await api.create(form); fetchData(); };
const handleUpdate = async () => { await api.update(id, form); fetchData(); };
const handleDelete = async (id) => { await api.delete(id); fetchData(); };
```

---

### ═══════════════════════════════════════
### TAHAP 17: CSS STYLING
### ═══════════════════════════════════════

Styling dengan Vanilla CSS. Tips untuk tampilan premium:
- **Dark theme** dengan warna gelap (#0f1117, #1a1d2e)
- **Glassmorphism:** `backdrop-filter: blur(20px)` + background transparan
- **Gradient text:** `-webkit-background-clip: text`
- **Animasi halus:** `transition: all 0.3s ease`
- **Badge berwarna** untuk status (hijau = available, kuning = pending, dll)

---

## 📊 Diagram Relasi Database

```
┌──────────┐       ┌───────────┐       ┌──────────┐
│  users   │       │  rentals  │       │   cars   │
├──────────┤       ├───────────┤       ├──────────┤
│ id (PK)  │◄──────│ user_id   │       │ id (PK)  │
│ username │       │ car_id    │──────►│ brand    │
│ password │       │ start_date│       │ model    │
│ fullName │       │ end_date  │       │ year     │
│ email    │       │ total_cost│       │ plate    │
│ role     │       │ status    │       │ daily_rate│
│ active   │       │           │       │ status   │
└──────────┘       └───────────┘       └──────────┘

┌────────────────┐
│ activity_logs  │
├────────────────┤
│ id (PK)        │
│ username       │
│ role           │
│ action         │
│ entity_name    │
│ entity_id      │
│ details        │
│ timestamp      │
└────────────────┘
```

---

## 🔑 Konsep Penting yang Harus Dipahami

### 1. Clean Architecture (Layered)
```
Controller  →  hanya terima/kirim data
    ↓
Service     →  business logic (validasi, kalkulasi)
    ↓
Repository  →  akses database
    ↓
Entity      →  representasi tabel database
```
**Aturan:** Layer atas HANYA boleh panggil layer bawahnya. Controller tidak boleh langsung akses Repository!

### 2. Dependency Injection
```java
@RequiredArgsConstructor  // Lombok generate constructor
public class CarService {
    private final CarRepository carRepository;  // Spring inject otomatis!
}
```
Spring otomatis cari Bean yang sesuai dan inject ke constructor.

### 3. DTO Pattern
```
Frontend ←→ DTO ←→ Service ←→ Entity ←→ Database
```
DTO melindungi Entity dari exposure langsung ke frontend.

### 4. JWT Flow
```
Login → Server generate token → Client simpan token
    ↓
Setiap request → Client kirim token di header → Server validasi
    ↓
Token expired → Client harus login ulang
```

### 5. Pessimistic Locking
```
Thread A: LOCK row → proses → UNLOCK
Thread B: WAIT... → LOCK row → proses → UNLOCK
```
Mencegah race condition saat 2 user akses data yang sama secara bersamaan.

---

## ✅ Checklist Sebelum Submit

- [ ] Semua CRUD berfungsi (Create, Read, Update, Delete)
- [ ] Login/Register berfungsi
- [ ] Role-based access (Admin vs User) berfungsi
- [ ] Filter mobil berfungsi
- [ ] Rental dengan validasi tanggal berfungsi
- [ ] Status rental berubah sesuai workflow
- [ ] Activity log mencatat semua aksi
- [ ] Concurrent rental ditangani (tidak double booking)
- [ ] Error handling menampilkan pesan yang jelas
- [ ] UI responsive dan rapi
