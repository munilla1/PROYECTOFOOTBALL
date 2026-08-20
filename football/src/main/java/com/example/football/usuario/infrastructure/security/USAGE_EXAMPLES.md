# Ejemplos de Uso - Seguridad por Perfil

## 🚀 Cómo Ejecutar en Cada Perfil

### 1️⃣ DESARROLLO (SIN SEGURIDAD - Recomendado para desarrollo local)

```bash
# Opción A: Crear application-dev.properties
# Archivo: src/main/resources/application-dev.properties
spring.profiles.active=dev
spring.datasource.url=jdbc:postgresql://localhost:5432/football
spring.datasource.username=postgres
spring.datasource.password=PostgreSQL17

# Opción B: Ejecutar con parámetro
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Opción C: Variable de entorno
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

**Resultado:**
```
✅ Todas las rutas accesibles SIN login
✅ http://localhost:8080/ → Funciona sin token
✅ http://localhost:8080/api/admin/users → Funciona sin token
✅ Sin RoleAuthorizationFilter
✅ Desarrollo sin fricción
```

---

### 2️⃣ PRODUCCIÓN (CON SEGURIDAD - Recomendado para producción)

```bash
# Opción A: Sin perfiles específicos (perfil default = no es dev)
mvn spring-boot:run

# Opción B: Perfil prod explícito
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# Opción C: Variable de entorno
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

**Resultado:**
```
🔒 Login obligatorio en http://localhost:8080/ui/login.html
🔒 http://localhost:8080/ → Redirige a login
🔒 http://localhost:8080/api/admin/users → Requiere JWT token
🔒 RoleAuthorizationFilter ACTIVO
🔒 JWT + roles validados
```

---

### 3️⃣ TESTS (CON SEGURIDAD - Para pruebas de aceptación)

```bash
# Ejecutar con perfil test
mvn test

# O explícitamente:
mvn test -Dspring.profiles.active=test

# Ejecutar test específico
mvn test -Dtest=FootballApplicationTests
mvn test -Dtest=SesionAcceptanceTest
mvn test -Dtest=UsuarioAcceptanceTest
```

**Resultado:**
```
✅ ApplicationContext cargado con seguridad ACTIVA
✅ Tests de aceptación validan:
   - Autenticación
   - JWT tokens
   - Validación de roles
   - Restricción de acceso
✅ Perfil "test" != "dev" → Seguridad activada
```

---

## 🧪 Pruebas Manuales

### Test 1: Desarrollo sin seguridad
```bash
# Terminal 1: Ejecutar con perfil dev
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Terminal 2: Probar acceso sin autenticación
curl http://localhost:8080/api/admin/users
# Respuesta esperada: ✅ 200 OK + datos (SIN login requerido)

curl http://localhost:8080/ui/login.html
# Respuesta esperada: ✅ 200 OK + HTML login (accesible)
```

### Test 2: Producción con seguridad
```bash
# Terminal 1: Ejecutar sin perfil dev (perfil por defecto)
mvn spring-boot:run

# Terminal 2: Probar acceso SIN token
curl http://localhost:8080/api/admin/users
# Respuesta esperada: 🔒 401 Unauthorized o 403 Forbidden

curl http://localhost:8080/
# Respuesta esperada: 🔒 Redirige a login

# Terminal 3: Login y obtener token
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=password" \
  -v
# Respuesta esperada: 🔒 Set-Cookie: JSESSIONID=...

# Terminal 4: Usar token en request
curl http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer {token}"
# Respuesta esperada: ✅ 200 OK + datos (CON token válido)
```

---

## 📝 Archivos de Configuración

### `application.properties` (Producción por defecto)
```properties
spring.application.name=football
spring.datasource.url=jdbc:postgresql://localhost:5432/football
spring.datasource.username=postgres
spring.datasource.password=PostgreSQL17

# Perfil NO definido aquí → se usa "default" (NO es "dev")
# Resultado: SEGURIDAD ACTIVADA
```

### `application-dev.properties` (Desarrollo)
```properties
spring.profiles.active=dev
spring.datasource.url=jdbc:postgresql://localhost:5432/football
spring.datasource.username=postgres
spring.datasource.password=PostgreSQL17
spring.jpa.hibernate.ddl-auto=update

# Perfil = "dev"
# Resultado: SEGURIDAD DESACTIVADA
```

### `application-test.properties` (Tests)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/football_test
spring.datasource.username=postgres
spring.datasource.password=PostgreSQL17
spring.jpa.hibernate.ddl-auto=create-drop
spring.test.database.replace=none

# Perfil = "test" (activado automáticamente por Spring Boot Test)
# Perfil != "dev" → SEGURIDAD ACTIVADA
```

---

## 🔑 Acceso a Endpoints

### En Perfil DEV (sin seguridad)
```
✅ GET  /                           → OK
✅ GET  /ui/**                      → OK
✅ GET  /styles/**                  → OK
✅ GET  /core/**                    → OK
✅ GET  /domain/**                  → OK
✅ GET  /api/admin/users            → OK (sin token)
✅ GET  /api/user/profile           → OK (sin token)
✅ POST /api/auth/login             → OK
✅ GET  /api/public/data            → OK
```

### En Perfil PRODUCCIÓN (con seguridad)
```
✅ GET  /                           → 302 Redirect a /ui/login.html
✅ GET  /ui/login.html              → OK (sin autenticación)
✅ GET  /styles/**                  → OK (sin autenticación)
✅ POST /login                      → OK (procesar login)
🔒 GET  /api/admin/users            → 401/403 (sin token o rol)
🔒 GET  /api/user/profile           → 401/403 (sin autenticación)
✅ GET  /api/public/data            → OK (sin autenticación)
✅ POST /api/auth/login             → OK (obtener JWT)
```

---

## 🎯 Resumen de Flujos

### Flujo DEV (Desarrollo local)
```
Usuario → http://localhost:8080/api/admin/users
  ↓
SecurityConfiguration.devSecurityFilterChain()
  ↓
✅ .anyRequest().permitAll()
  ↓
Acceso PERMITIDO (sin login, sin token)
```

### Flujo PROD (Navegador web)
```
Usuario → http://localhost:8080/
  ↓
SecurityConfiguration.prodSecurityFilterChain()
  ↓
🔒 .anyRequest().authenticated()
  ↓
Redirige a login → http://localhost:8080/ui/login.html
  ↓
Usuario ingresa credenciales → POST /login
  ↓
✅ Login exitoso → Cookie JSESSIONID
  ↓
Acceso PERMITIDO (con sesión autenticada)
```

### Flujo PROD (API con JWT)
```
Cliente → GET /api/admin/users
         Header: Authorization: Bearer {jwt_token}
  ↓
SecurityConfiguration.prodSecurityFilterChain()
  ↓
✅ .authenticated() → validar JWT
  ↓
RoleAuthorizationFilter
  ↓
✅ Extrae rol del token
✅ Verifica que rol sea "admin"
  ↓
Acceso PERMITIDO (JWT + rol válido)
```

---

## 🐛 Troubleshooting

| Problema | Causa | Solución |
|----------|-------|----------|
| "Acceso a /api/admin/users sin login" | Perfil es "dev" | Cambiar a perfil distinto a dev |
| "Login obligatorio en dev" | Perfil no es "dev" | Usar `--spring.profiles.active=dev` |
| "Token inválido en /api/admin/**" | JWT expirado o rol incorrecto | Generar nuevo token con rol "admin" |
| "403 Forbidden en /api/admin/**" | Rol no es "admin" | Verificar rol en JWT token |
| "404 en /ui/login.html" | Archivo no existe | Crear `src/main/resources/templates/ui/login.html` |

---

## ✅ Validación de Configuración

```bash
# Ver qué perfil está activo
curl http://localhost:8080/actuator/env | grep "activeProfiles"

# O en logs
# Buscar: "No active profile set" o "The following profiles are active"
```

---

## 📊 Matriz de Perfiles

| Perfil | `dev` | Otros (`test`, `prod`, `default`) |
|--------|:-----:|:---:|
| **Seguridad** | ❌ Desactivada | ✅ Activada |
| **Acceso libre** | ✅ Todas las rutas | ❌ Solo rutas públicas |
| **Login requerido** | ❌ No | ✅ Sí |
| **JWT requerido** | ❌ No | ✅ Sí (para `/api/admin/**`) |
| **RoleAuthorizationFilter** | 🚫 No se crea | ✅ Activo |
| **Desarrollo** | ✅ Recomendado | ❌ No recomendado |
| **Producción** | ❌ No permitido | ✅ Recomendado |
