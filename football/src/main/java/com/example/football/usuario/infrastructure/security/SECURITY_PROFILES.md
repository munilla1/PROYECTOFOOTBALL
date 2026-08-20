# Configuración de Seguridad por Perfil - CHG-0009

## 📋 Resumen

La configuración de seguridad de Spring se adapta automáticamente según el perfil activo:

- **Perfil `dev`**: Seguridad **completamente desactivada** para desarrollo sin restricciones
- **Perfil de producción** (cualquier perfil que NO sea `dev`): Seguridad **completamente activada** con login personalizado

---

## 🔧 Configuración por Perfil

### 1️⃣ Perfil: `dev` (DESARROLLO)

**Ubicación**: `SecurityConfiguration.java` - Bean `devSecurityFilterChain()`

**Comportamiento:**
```
✅ Todas las rutas PERMITIDAS sin autenticación
✅ NO hay login obligatorio
✅ NO hay basic auth
✅ NO se ejecuta RoleAuthorizationFilter
✅ Acceso a recursos estáticos sin restricción
```

**Rutas accesibles sin autenticación:**
- `/` - raíz
- `/ui/**` - interfaz de usuario
- `/styles/**` - estilos CSS
- `/core/**` - archivos core
- `/domain/**` - archivos domain
- **Cualquier otra ruta sin restricción**

**Uso en desarrollo:**
```bash
# Ejecutar con perfil dev (sin seguridad)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# O configurar en application-dev.properties
spring.profiles.active=dev
```

---

### 2️⃣ Perfil: Producción (CUALQUIER PERFIL QUE NO SEA `dev`)

**Ubicación**: `SecurityConfiguration.java` - Bean `prodSecurityFilterChain()`

**Comportamiento:**
```
🔒 Seguridad COMPLETAMENTE ACTIVADA
🔒 Login personalizado en /ui/login.html
🔒 JWT + Roles para endpoints de API
🔒 RoleAuthorizationFilter ACTIVO
🔒 Basic Auth para herramientas (Postman, Insomnia)
```

**Rutas públicas (sin autenticación):**
- `/ui/login.html` - página de login personalizado
- `/login` - procesamiento de login
- `/styles/**` - estilos CSS
- `/ui/**` - interfaz de usuario
- `/core/**` - archivos core
- `/domain/**` - archivos domain
- `/api/public/**` - endpoints públicos
- `/api/auth/**` - endpoints de autenticación
- `/api/health/**` - health checks

**Rutas protegidas (requieren autenticación):**
- `/api/admin/**` - endpoints de administración (requieren rol ADMIN + JWT válido)
- `/api/user/**` - endpoints de usuario (requieren autenticación)
- Todas las demás rutas no explícitamente permitidas

**Autenticación:**
1. **Form Login** (navegador): Login en `/ui/login.html` → cookie de sesión
2. **JWT** (API): Token en header `Authorization: Bearer {token}` → roles validados
3. **Basic Auth** (herramientas): Para Postman/Insomnia durante desarrollo

**Uso en producción:**
```bash
# Perfil test (para tests de aceptación)
mvn test -Dspring.profiles.active=test

# Perfil producción
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# O configurar en application.properties sin perfiles específicos
# Si no está configurado spring.profiles.active, se usa perfil "default" (no es dev)
```

---

## 🔐 Componentes de Seguridad

### `SecurityConfiguration.java`
- **Responsabilidad**: Definir filtros y rutas protegidas según perfil
- **Beans**: 
  - `devSecurityFilterChain()` - @Profile("dev")
  - `prodSecurityFilterChain()` - @Profile("!dev")

### `RoleAuthorizationFilter.java`
- **Responsabilidad**: Validar roles JWT en endpoints protegidos
- **Activo solo en**: @Profile("!dev") - Producción
- **Inactivo en**: Perfil "dev"
- **Funcionalidad**:
  - Extrae JWT del header `Authorization: Bearer {token}`
  - Valida que el rol sea suficiente para acceder a `/api/admin/**`
  - Responde con 401 si token inválido
  - Responde con 403 si rol insuficiente

### `RequiresRole.java`
- **Responsabilidad**: Anotación para marcar métodos que requieren rol específico
- **Nota**: Actualmente no se usa (RoleAuthorizationFilter valida automáticamente)
- **Uso futuro**: Para validación de roles a nivel de controlador

---

## 🚀 Casos de Uso

### Desarrollo Local (sin restricciones)
```bash
# Ejecutar con seguridad desactivada
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Resultado: Accede a cualquier ruta sin login
# http://localhost:8080/ → OK
# http://localhost:8080/ui/login.html → OK
# http://localhost:8080/api/admin/users → OK (sin token)
```

### Tests de Aceptación
```bash
# Ejecutar tests con perfil "test" (perfil != "dev", entonces seguridad ACTIVA)
mvn test

# Los tests usan application-test.properties
# spring.profiles.active=test (implícitamente o configurado)
# Seguridad activada: RoleAuthorizationFilter ejecutándose
# Tests validan: autenticación, JWT, roles, etc.
```

### Producción
```bash
# Ejecutar en producción (sin perfil dev)
# Perfil por defecto: "default" → seguridad ACTIVA

# Resultado:
# - Login obligatorio en /ui/login.html
# - JWT para APIs
# - Roles validados en /api/admin/**
# - Basic Auth disponible para herramientas
```

---

## ⚙️ Configuración Relacionada

### `application.properties` (Producción)
```properties
spring.application.name=football
spring.datasource.url=jdbc:postgresql://localhost:5432/football
# Perfil NO definido → default (secur conida ACTIVADA)
```

### `application-dev.properties` (Desarrollo)
```properties
spring.profiles.active=dev
spring.datasource.url=jdbc:postgresql://localhost:5432/football
# Seguridad DESACTIVADA
```

### `application-test.properties` (Tests)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/football_test
spring.jpa.hibernate.ddl-auto=create-drop
spring.test.database.replace=none
# Perfil "test" != "dev" → Seguridad ACTIVADA para tests
```

---

## 📊 Matriz de Seguridad

| Aspecto | Dev | Producción |
|---------|-----|-----------|
| **Seguridad activada** | ❌ No | ✅ Sí |
| **Login obligatorio** | ❌ No | ✅ Sí |
| **Rutas públicas** | Todas | `/api/public/**`, `/api/auth/**`, etc. |
| **RoleAuthorizationFilter** | 🚫 No se crea | ✅ Activo |
| **Basic Auth** | ❌ Desactivado | ✅ Activado |
| **Form Login** | ❌ Desactivado | ✅ Activo en `/ui/login.html` |
| **JWT requerido** | ❌ No | ✅ Sí para `/api/admin/**` |

---

## 🔍 Troubleshooting

### Problema: "No login page in Spring Security"
**Causa**: Perfil no es "dev", seguridad está activa pero no hay handler para `/`
**Solución**: Accede a `/ui/login.html` directamente

### Problema: "Access denied to /api/admin/users"
**Causa**: Token JWT inválido o no incluido en header `Authorization`
**Solución**: 
- Incluir header: `Authorization: Bearer {token}`
- Token debe tener rol "admin"

### Problema: "Seguridad sigue activa en dev"
**Causa**: Perfil no está configurado correctamente
**Solución**: 
- Verificar `spring.profiles.active=dev` en `application-dev.properties`
- O pasar como parámetro: `--spring.profiles.active=dev`

---

## 📝 Notas Importantes

1. ✅ **Arquitectura hexagonal preservada**: Dominio puro, aplicación orquesta, infraestructura adapta
2. ✅ **Login personalizado funcional**: `/ui/login.html` es tu página, no la de Spring
3. ✅ **JWT + Roles funcionando**: RoleAuthorizationFilter valida permisos por rol
4. ✅ **Desarrollo sin fricción**: Perfil `dev` desactiva toda seguridad
5. ✅ **Tests con seguridad**: Tests usan perfil "test" con seguridad activada

---

## 🎯 Resumen Ejecutivo

```
┌─────────────────────────────────────────────────┐
│ SEGURIDAD POR PERFIL - CHG-0009                 │
├─────────────────────────────────────────────────┤
│ Perfil "dev"    → ✅ SIN seguridad              │
│ Otros perfiles  → 🔒 CON seguridad             │
│                                                 │
│ Dev: Acceso libre a todas las rutas            │
│ Prod: Login + JWT + Roles                      │
└─────────────────────────────────────────────────┘
```
