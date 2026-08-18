# Cambios activos

Cada carpeta aquí representa un cambio en curso: una nueva función, una corrección importante
o una decisión técnica con impacto en el producto.

Los cambios activos del proyecto PROYECTOFOOTBALL son:

- CHG-0001 - Integración de estadísticas reales desde API-Football
- CHG-0002 - XP basado en estadísticas reales
- CHG-0003 - Energía y forma basadas en minutos reales
- CHG-0004 - Progresión basada en rendimiento real
- CHG-0005 - Mercado de fichajes basado en estadísticas reales
- CHG-0006 - Jornadas sincronizadas con partidos reales
- CHG‑0007 — Sistema de usuarios persistentes
- CHG‑0008 — Sistema de sesiones del usuario
- CHG‑0009 — Sistema de roles
- CHG‑0010 — Sistema de membresías y cobro con Stripe

Cada uno de estos cambios afecta a uno o varios dominios del videojuego y se gestiona
siguiendo el ciclo SDD.

## Estructura obligatoria por cambio

| Archivo            | Cuándo crear                 | Contenido |
|--------------------|------------------------------|-----------|
| `proposal.md`      | Al inicio                    | Problema, objetivo, alcance y bandera de seguridad |
| `requirements.md`  | Antes de la implementación    | Requisitos con criterios de aceptación medibles |
| `tasks.md`         | Una vez aprobados los requisitos | Lista de verificación rastreable a los requisitos |
| `evidence.md`      | Tras la validación           | Resultados de las pruebas y aprobación |

Cada archivo debe completarse en orden.  
**No se inicia implementación sin `requirements.md` aprobado.**

---

## Puerta de seguridad

Si `proposal.md` marca el cambio como **sensible a la seguridad** (autenticación, permisos,
secretos, exposición de datos o puntos finales públicos), la lista de verificación de seguridad:

