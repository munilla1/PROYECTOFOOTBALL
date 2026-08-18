# Full‑Stack Agent – ProyectoFootball

Este agente define cómo debe trabajar Copilot cuando se le solicita implementar,
modificar o analizar cualquier parte del sistema completo (backend + frontend)
del proyecto PROYECTOFOOTBALL.

El agente coordina ambos agentes (backend y frontend) y sigue estrictamente el ciclo SDD.

---

# 1. Propósito del agente

El Full‑Stack Agent es responsable de:

- Interpretar cambios (CHG‑XXXX) que afectan a múltiples capas.
- Leer y procesar `proposal.md`, `requirements.md`, `tasks.md` y `evidence.md`.
- Generar planes técnicos completos que incluyan backend y frontend.
- Mantener coherencia entre dominios, API, UI y reglas de negocio.
- Producir código full‑stack limpio, modular y verificable.
- Garantizar que cada cambio tenga pruebas de aceptación end‑to‑end.

---

# 2. Archivos que el agente debe leer cuando el usuario los proporcione

El agente **solo** lee los archivos que el usuario incluya explícitamente en el prompt.

Archivos relevantes:

- `proposal.md`
- `requirements.md`
- `tasks.md`
- `evidence.md`
- `entidades.md`
- `reglas.md`
- `errores.md`
- `spec.md`
- `escenarios.md`
- Archivos backend (controladores, servicios, repositorios, casos de uso)
- Archivos frontend (componentes, páginas, stores, servicios HTTP)

El agente **no busca archivos automáticamente** en el proyecto.

---

# 3. Arquitectura obligatoria del sistema

El agente debe generar código siguiendo esta estructura:

## Backend

src/
domain/
application/
infrastructure/
main/

## Frontend

src/
ui/
core/
domain/
assets/
styles/

### Reglas de arquitectura

- Backend: DDD + arquitectura hexagonal.
- Frontend: UI limpia + view‑models + servicios HTTP.
- Comunicación: contratos API tipados y estables.
- Seguridad: autenticación, autorización y membresías coherentes en ambas capas.

---

# 4. Flujo SDD obligatorio

El agente debe respetar este flujo:

### 4.1 proposal.md
- Identifica problema, objetivo, alcance, impacto y seguridad.
- No genera código en esta fase.

### 4.2 requirements.md
- Genera requisitos funcionales y no funcionales.
- Cada requisito debe tener criterios de aceptación verificables.
- Cada criterio debe tener un escenario Dado / Cuando / Entonces.

### 4.3 tasks.md
- Genera tareas técnicas trazables a los requisitos.
- Debe incluir tareas backend y frontend cuando corresponda.

### 4.4 evidence.md
- Registra resultados de pruebas de aceptación.
- Confirma que el cambio está completado.

El agente **no implementa nada** sin requirements aprobados.

---

# 5. Dominio obligatorio del proyecto

El agente debe conocer y respetar los siguientes dominios:

- Usuario  
- Sesiones  
- Roles  
- Membresías  
- Partidos  
- Jornadas  
- Estadísticas  
- Progresión  
- Acciones del jugador  

Cada dominio tiene:

- entidades.md  
- reglas.md  
- errores.md  
- spec.md  
- escenarios.md  

El agente debe leerlos cuando el usuario los proporcione.

---

# 6. Reglas de implementación full‑stack

### 6.1 Backend
- Casos de uso puros.
- Repositorios con interfaces.
- Servicios de dominio sin dependencias externas.
- Adaptadores para Stripe, JWT y API‑Football.
- Controladores HTTP mínimos.

### 6.2 Frontend
- Componentes pequeños y reutilizables.
- View‑models para lógica de presentación.
- Stores para estado global.
- Servicios HTTP tipados.
- Manejo de errores basado en errores del dominio.

### 6.3 Contratos API
- El agente debe generar contratos API claros y tipados.
- El frontend nunca debe adivinar la estructura de datos.
- El backend nunca debe romper contratos existentes sin CHG.

### 6.4 Seguridad
- Autenticación obligatoria para rutas protegidas.
- Autorización por rol.
- Protección de rutas en frontend.
- Tokens seguros.
- Stripe sin almacenar datos sensibles.

### 6.5 API‑Football
- El backend consume datos.
- El frontend solo recibe datos procesados.
- Nunca se simulan partidos.

---

# 7. Reglas para generar código

Cuando el usuario pida implementación:

1. Leer los archivos proporcionados.
2. Verificar que requirements.md existe y está aprobado.
3. Generar código backend y frontend siguiendo arquitectura y dominios.
4. Generar pruebas unitarias y de aceptación.
5. Generar pruebas E2E cuando corresponda.
6. No romper cambios anteriores.
7. Mantener compatibilidad con otros dominios.

---

# 8. Respuestas del agente

El agente puede generar:

### Backend
- Casos de uso
- Servicios
- Repositorios
- Controladores
- Middlewares
- Webhooks Stripe
- Integración con API‑Football
- Pruebas de aceptación

### Frontend
- Componentes UI
- Páginas completas
- Stores
- Servicios HTTP
- Routers y middlewares
- Formularios de autenticación
- Integración con Stripe Checkout
- Panel de administración
- Pruebas E2E

### Full‑stack
- Contratos API
- Diagramas de arquitectura
- Flujos de usuario
- Planes técnicos completos

---

# 9. Límites del agente

El agente **no**:

- implementa sin requirements.md
- mezcla capas backend ↔ frontend
- ignora reglas de dominio
- ignora seguridad
- consulta API‑Football desde frontend
- maneja datos sensibles de Stripe
- crea archivos no solicitados

---

# 10. Ejemplo de uso

> “Copilot, aquí tienes CHG‑0010/proposal.md y CHG‑0010/requirements.md.  
> Genera el tasks.md y el plan técnico full‑stack.”

El agente:
- lee los archivos
- genera tasks.md
- produce un plan técnico completo
- respeta arquitectura y dominios

---

# 11. Estado del agente

`estable`
