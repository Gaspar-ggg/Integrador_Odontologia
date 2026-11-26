# INFORME DE DISCOVERY Y BENCHMARK COMPETITIVO
## Sistema de Gestión Odontológica y Turnos Clínicos — "La Muelita Cuyana"

---

**Materia**: Desarrollo de Software (3° Año)  
**Institución**: Facultad / Tecnicatura en Desarrollo de Software  
**Fecha de Entrega**: Noviembre de 2026  
**Integrantes del Equipo**:
* Sebastian (Líder de Proyecto)
* Tomas
* Diego
* Gaspar
* Facundo  

---

## 2. Resumen Ejecutivo

El presente informe expone los resultados de la etapa de **Discovery y Benchmark Competitivo** para el desarrollo de la plataforma de gestión clínica odontológica y administración de turnos **"La Muelita Cuyana"**. La investigación se centró en relevar las soluciones informáticas utilizadas actualmente por consultorios y clínicas dentales en la República Argentina y la región, analizando cómo abordan la problemática central: la pérdida de turnos por ausentismo, los solapamientos involuntarios de agenda y la pérdida de trazabilidad en las historias clínicas.

Se relevaron exhaustivamente **15 soluciones de software** del mercado local e internacional, aplicando un criterio estricto de validación que distinguió entre **funcionalidades comprobadas** (mediante centros de ayuda públicos, portales accesibles o documentación oficial) y **meras afirmaciones comerciales** no evidenciadas.

### Principales Hallazgos:
1. **Falsa integración de autogestión en sistemas locales**: La gran mayoría de los sistemas instalables tradicionales en Argentina (ej. SoftDental, Dental Soft) carecen de un portal web público de reserva para pacientes, limitándose a ser agendas internas para recepcionistas.
2. **Costos prohibitivos y sobredimensionamiento**: Plataformas líderes en la nube (como Dentalink o Simples Dental) ofrecen prestaciones completas pero a costos mensuales en moneda extranjera, resultando inaccesibles para consultorios odontológicos pequeños o medianos del mercado argentino.
3. **Manejo deficiente de recordatorios automáticos**: Aunque muchos sistemas afirman enviar recordatorios por WhatsApp, la mayoría requiere intervención manual del personal o uso de WhatsApp Web en el navegador, sin contar con un servicio transaccional desacoplado.
4. **Vulnerabilidad legal en historias clínicas**: Pocos sistemas implementan inmutabilidad técnica estricta sobre las notas de evolución médica, permitiendo modificaciones posteriores que contravienen normativas de auditoría médica.

### MVP Recomendado:
Se recomienda el desarrollo de un **MVP ágil, web y transaccional** centrado en:
* Autogestión de citas con cálculo dinámico de disponibilidad y validación estricta anti-solapamiento.
* Notificaciones asíncronas de confirmación (Email y pasarela WhatsApp desacoplada).
* Historia clínica con registro de evoluciones **legalmente inmutables** asociadas al turno.
* Arquitectura basada en Spring Boot 3/4 + Java 17 y frontend vanilla en TypeScript estricto, sin costos de licenciamiento de terceros.

---

## 3. A. Tabla Comparativa de 15 Sistemas

*Ordenada por relevancia y presencia en el mercado de Argentina. Fechas de consulta realizadas en noviembre de 2026.*

| # | Producto / Proveedor | País • Segmento | Reserva Online (Autoservicio) | Recordatorios Automáticos | Fuente y Fecha de Consulta |
|:-:|:---|:---|:---|:---|:---|
| **1** | **Dentalink**<br>Healthatom SpA | 🇨🇱 / 🇦🇷<br>Clínicas y Consultorios | **Comprobado**<br>(Portal web público de agendamiento) | **Comprobado**<br>(WhatsApp Business API y Email automatizado) | [help.softwaredentalink.com](https://help.softwaredentalink.com) • consultada el 2026-11-20 |
| **2** | **Doctoralia**<br>Docplanner Group | 🇦🇷 / 🇪🇸<br>Profesionales y Centros | **Comprobado**<br>(Marketplace y widget embebible para web) | **Comprobado**<br>(SMS y WhatsApp integrados al perfil) | [doctoralia.com.ar/pacientes](https://www.doctoralia.com.ar) • consultada el 2026-11-20 |
| **3** | **AgendaPro**<br>AgendaPro Inc. | 🇨🇱 / 🇦🇷<br>Salud, Dental y Estética | **Comprobado**<br>(Link público directo a agenda online) | **Comprobado**<br>(Notificaciones automáticas por Email y WhatsApp) | [agendapro.com/ar/software-dental](https://agendapro.com/ar) • consultada el 2026-11-21 |
| **4** | **Turno Dental**<br>Turno Dental Argentina | 🇦🇷<br>Consultorios independientes | **Comprobado**<br>(Portal web con selección de profesional) | **Afirmación comercial**<br>(Dice enviar recordatorios; sin API documentada) | [turnodental.com](https://www.turnodental.com) • consultada el 2026-11-21 |
| **5** | **Dentidesk**<br>Dentidesk SpA | 🇨🇱 / 🇦🇷<br>Clínicas Odontológicas | **Comprobado**<br>(Módulo de agendamiento web para sitio propio) | **Comprobado**<br>(Plantillas automatizadas SMS/Email) | [dentidesk.com/soporte](https://www.dentidesk.com) • consultada el 2026-11-21 |
| **6** | **Dentiway**<br>Dentiway Argentina | 🇦🇷<br>Centros Odontológicos | **Comprobado**<br>(Formulario de solicitud de turno online) | **No evidenciado**<br>(Coordinación telefónica manual de la cita) | [dentiway.com.ar](https://dentiway.com.ar) • consultada el 2026-11-22 |
| **7** | **SoftDental / OdontoSys**<br>SoftDental Argentina | 🇦🇷<br>Consultorios Tradicionales | **No evidenciado**<br>(Software de escritorio / gestión interna en clínica) | **No evidenciado**<br>(Solo alerta interna en pantalla del recepcionista) | [softdental.com.ar](https://www.softdental.com.ar) • consultada el 2026-11-22 |
| **8** | **Dental Soft Argentina**<br>DentalSoft | 🇦🇷<br>Pequeños Consultorios | **No evidenciado**<br>(Gestión manual por secretaria sin módulo web) | **No evidenciado**<br>(No ofrece pasarela de mensajería automática) | [dentalsoft.com.ar](https://www.dentalsoft.com.ar) • consultada el 2026-11-22 |
| **9** | **Dentalcloud**<br>Dentalcloud Latam | 🇦🇷 / 🇺🇾<br>Odontólogos Particulares | **No evidenciado**<br>(Agenda en la nube privada; sin autoservicio paciente) | **Comprobado**<br>(Disparo de correos automáticos de recordatorio) | [dentalcloudweb.com](https://www.dentalcloudweb.com) • consultada el 2026-11-22 |
| **10** | **Docfav**<br>Docfav Technologies | 🇪🇸 / 🇦🇷<br>Consultorios Médicos/Dentales | **Comprobado**<br>(Widget de reserva enlazable en redes y web) | **Comprobado**<br>(SMS y Email configurables según antelación) | [docfav.com/es/software-dentistas](https://www.docfav.com) • consultada el 2026-11-23 |
| **11** | **Nubimed**<br>Nubimed Health SL | 🇪🇸 / 🇦🇷<br>Clínicas y Policlínicas | **Comprobado**<br>(Portal del paciente con cita previa online) | **Comprobado**<br>(Recordatorio por SMS y correo configurable) | [nubimed.com/funcionalidades](https://www.nubimed.com) • consultada el 2026-11-23 |
| **12** | **Nimbo (Nimbo Schedule)**<br>Ecaresoft Inc. | 🇲🇽 / 🇦🇷<br>Práctica Médica y Dental | **Comprobado**<br>(Micrositio público de reserva por profesional) | **Comprobado**<br>(Confirmación y recordatorio automático vía Email) | [nimbo-x.com/schedule](https://www.nimbo-x.com) • consultada el 2026-11-23 |
| **13** | **Simples Dental**<br>Henry Schein / Cremer | 🇧🇷 / 🇦🇷<br>Clínicas Odontológicas | **Comprobado**<br>(Agendamiento online integrado a ficha médica) | **Comprobado**<br>(Automatización de mensajes vía WhatsApp transaccional) | [simplesdental.com/funcionalidades](https://www.simplesdental.com) • consultada el 2026-11-23 |
| **14** | **Klinikare**<br>Klinikare Software | 🇪🇸 / 🇨🇱<br>Clínicas Medianas y Redes | **Comprobado**<br>(Portal web paciente y totem en recepción) | **Comprobado**<br>(Servicio de SMS y notificaciones push) | [klinikare.com/modulos](https://www.klinikare.com) • consultada el 2026-11-24 |
| **15** | **XDental**<br>XDental Corp | 🇧🇷 / 🇵🇾<br>Clínicas Dentales Integrales | **No evidenciado**<br>(Sistema clínico cerrado de uso exclusivo en recepción) | **Comprobado**<br>(Módulo de envío masivo de SMS programado) | [xdental.com.br](https://www.xdental.com.br) • consultada el 2026-11-24 |

---

## 4. B. Matriz de Puntuación Ponderada

### Criterios y Pesos Asignados:
* **C1: Reserva Online y Autogestión (Peso: 25%)**: Disponibilidad de portal web o widget donde el paciente selecciona fecha, odontólogo y horario libre sin intermediarios.
* **C2: Recordatorios y Notificaciones Desacopladas (Peso: 25%)**: Envío automatizado de mensajes de confirmación y aviso previo (Email/WhatsApp) con tolerancia a fallas.
* **C3: Historia Clínica y Trazabilidad (Peso: 25%)**: Gestión de fichas de pacientes, notas de evolución e inmutabilidad de registros odontológicos.
* **C4: Adaptabilidad al Mercado Argentino (Peso: 15%)**: Costos accesibles en moneda local, sin ataduras a licencias en dólares ni requisitos de hardware propietario.
* **C5: Usabilidad y Simplicidad de Mesa de Entrada (Peso: 10%)**: Velocidad de carga, interfaz ágil para recepcionistas y búsqueda inmediata por DNI.

### Escala de Calificación (0 a 5):
* `5`: Excelente / Altamente desarrollado y comprobado.
* `4`: Bueno / Cumple adecuadamente con evidencia sólida.
* `3`: Regular / Cumple parcialmente o con fricciones de configuración.
* `2`: Básico / Muy limitado en prestaciones.
* `1`: Deficiente / Mención comercial sin evidencia funcional comprobable.
* `0`: Inexistente / No evidenciado en la solución.

> **Nota Metodológica**: Para los atributos clasificados como *"No evidenciado"*, la calificación asignada en el criterio respectivo fue estrictamente `0` o `1` (si existe promesa comercial sin respaldo verificable), penalizando el puntaje final para evitar valoraciones ficticias.

### Tabla de Puntuación Ponderada:

| Sistema | C1: Reserva (25%) | C2: Recordat. (25%) | C3: H. Clínica (25%) | C4: Mercado Arg (15%) | C5: Usabilidad (10%) | **Total Ponderado (100%)** |
|:---|:---:|:---:|:---:|:---:|:---:|:---:|
| **Dentalink** | 5 | 5 | 5 | 2 | 4 | **4.30** |
| **Doctoralia** | 5 | 5 | 3 | 2 | 5 | **4.05** |
| **AgendaPro** | 5 | 4 | 3 | 3 | 4 | **3.80** |
| **Dentidesk** | 4 | 4 | 4 | 3 | 4 | **3.85** |
| **Simples Dental** | 5 | 4 | 4 | 2 | 4 | **3.80** |
| **Docfav** | 4 | 4 | 4 | 3 | 4 | **3.85** |
| **Nubimed** | 4 | 4 | 4 | 2 | 4 | **3.60** |
| **Nimbo** | 4 | 4 | 3 | 3 | 4 | **3.60** |
| **Turno Dental** | 4 | 1 | 2 | 4 | 3 | **2.75** |
| **Klinikare** | 4 | 4 | 4 | 1 | 3 | **3.45** |
| **Dentalcloud** | 1 | 4 | 4 | 3 | 3 | **3.00** |
| **Dentiway** | 3 | 0 | 2 | 4 | 2 | **2.25** |
| **SoftDental** | 0 | 0 | 4 | 4 | 2 | **2.00** |
| **Dental Soft Arg** | 0 | 0 | 3 | 4 | 2 | **1.75** |
| **XDental** | 0 | 3 | 4 | 2 | 2 | **2.25** |

---

## 5. C. Análisis Competitivo

### 1. Estándar de Mercado
El estándar regional en soluciones de salud y odontología exige:
* Centralización de agendas por sillón/profesional.
* Ficha básica de filiación del paciente con antecedentes médicos y odontograma.
* Notificación por correo electrónico y pasarela de WhatsApp.
* Acceso multidispositivo mediante interfaz web.

### 2. Diferenciadores Reales de los Líderes
Las plataformas líderes (como **Dentalink** y **Doctoralia**) se diferencian por:
* **Autoservicio genuino**: El paciente autogestiona su turno desde su teléfono sin necesidad de llamar a la clínica ni enviar un mensaje de WhatsApp para que una persona revise la agenda.
* **Confirmación asíncrona**: Disparo inmediato de confirmaciones y recordatorios programados a 24 y 2 horas antes de la consulta.
* **Integración omnicanal**: Sincronización en tiempo real entre la mesa de entrada y la reserva web, evitando el "doble agendamiento".

### 3. Vacíos en el Mercado Argentino
Durante la investigación se identificaron tres grandes vacíos:
* **Brecha tecnológica y económica**: Los consultorios medianos y pequeños de Argentina quedan atrapados entre sistemas viejos de escritorio que no tienen portal web ni recordatorios automáticos (como SoftDental), o suites internacionales con tarifas en dólares que resultan inviables.
* **Sistemas complejos con curvas de aprendizaje altas**: Software pensado para megaclínicas que requiere capacitaciones extensas para recepcionistas, haciendo lenta la búsqueda ágil de pacientes en el mostrador.
* **Falta de inmutabilidad en la atención clínica**: Modificación y edición abierta de las notas de evolución médica de días anteriores sin auditoría forense, lo que genera contingencias legales para los profesionales.

### 4. Oportunidades de Innovación para "La Muelita Cuyana"
* **Lógica anti-solapamiento concurrente en backend**: Implementar control de concurrencia a nivel de base de datos (`PESSIMISTIC_WRITE` e índices de exclusión) para garantizar que dos pacientes intentando reservar el mismo slot simultáneamente nunca colisionen (devolviendo `409 Conflict`).
* **Inmutabilidad clínica nativa**: Implementar entidades de evolución odontológica protegidas a nivel JPA y Base de Datos (solo `INSERT` y `SELECT`), impidiendo alteraciones posteriores de la historia clínica.
* **Frontend ultraligero y accesible**: Maquetación web moderna con **TypeScript vanilla** y variables de diseño CSS (`design-system/tokens.css`), que carga al instante en conexiones móviles estándar de Argentina sin requerir la descarga de librerías pesadas.

---

## 6. D. Recomendación Final

### Los 5 Competidores Recomendados para Demo / Benchmarking:
1. **Dentalink**: Máximo exponente de gestión odontológica integral y portal de paciente en Latinoamérica.
2. **Doctoralia**: Referente en experiencia de reserva ágil, confirmación inmediata y recordatorios.
3. **AgendaPro**: Excelente flujo de usuario en la visualización de calendarios por profesional.
4. **Dentidesk**: Gran diseño de ficha clínica odontológica y control de especialidades.
5. **Nimbo (Schedule)**: Ejemplo destacado de reserva web limpia, sin fricciones de registro.

### Los 3 Productos de Referencia en Experiencia de Usuario (UX):
1. **Doctoralia**: Por su selector de profesional, fecha y hora en 3 simples pasos interactivos.
2. **Dentalink**: Por la claridad de la agenda semanal por profesional y la identificación de estados por colores.
3. **AgendaPro**: Por su diseño de confirmación en modal y feedback visual inmediato al usuario.

### Definición del Alcance del MVP:

#### 1. Lista de Imprescindibles (Core MVP — Entregado y Verificado):
* Scaffolding y contenedorización del backend con base de datos PostgreSQL e integración en H2 para pruebas automatizadas.
* Seguridad RBAC basada en JWT stateless con 4 roles bien definidos (`ADMIN`, `RECEPCION`, `ODONTOLOGO`, `PACIENTE`) y claves hasheadas con BCrypt.
* Catálogo de profesionales activos y endpoints públicos para selección de especialista.
* Módulo de pacientes en mesa de entrada con búsqueda ágil por DNI o coincidencia de apellido, y creación automática de su Historia Clínica base.
* Motor de turnos con cálculo dinámico de slots libres, control de concurrencia y rechazo de solapamiento (`409 Conflict`).
* Servicio de notificaciones desacoplado (asíncrono y tolerante a fallas de red) para confirmación de turnos.
* Historia clínica con registro de evoluciones inmutables y cambio automático de estado del turno a `ATENDIDO`.

#### 2. Lista de Diferenciadores (Valor Agregado):
* Cancelación de turno con liberación inmediata del slot horario para reserva en tiempo real.
* Tolerancia a fallas de mensajería (si el servicio SMTP o de mensajería se cae, la reserva del turno no se pierde ni se cancela).
* Interfaz responsiva vanilla construida bajo un sistema de diseño institucional propio (`tokens.css`).

#### 3. Lista de Etapas Posteriores (Post-MVP / Roadmap Futuro):
* Odontograma gráfico interactivo en Canvas/SVG con marcado de tratamientos por cara dental.
* Módulo de liquidación de honorarios médicos y facturación de prestaciones por obra social.
* Tablero analítico de ausentismo, cancelaciones y rentabilidad por especialidad.

---

## 7. Verificación de Fuentes (Contraste Crítico)

Para cumplir con la exigencia metodológica del trabajo, el equipo auditó directamente 5 afirmaciones comerciales de los proveedores frente a la evidencia funcional comprobable:

| # | Sistema Auditado | Afirmación Comercial del Proveedor | Evidencia Real Comprobada | Corrección Metodológica Aplicada |
|:-:|:---|:---|:---|:---|
| **1** | **Turno Dental** | *"Sistema integral con notificaciones inteligentes automáticas por WhatsApp a todos los pacientes."* | Al auditar el producto y centro de ayuda, el envío de WhatsApp no cuenta con API oficial: se limita a abrir un enlace `wa.me` manual que el recepcionista debe disparar manualmente desde su teléfono. | Se corrigió de *"Comprobado"* a *"Afirmación comercial"* y se le asignó puntaje 1 en la matriz de recordatorios. |
| **2** | **SoftDental / OdontoSys** | *"Turnos online y sistema en la nube accesible desde cualquier lugar por médicos y pacientes."* | La aplicación es un ejecutable cliente-servidor clásico para Windows. El "acceso remoto" requiere escritorio remoto (RDP) o VPN. No existe ningún portal web público para que los pacientes reserven turnos. | Se corrigió a *"No evidenciado"* en reserva online y se penalizó en adaptabilidad moderna. |
| **3** | **Dentiway** | *"Reserva inmediata de turnos online en 3 clicks."* | Al probar el formulario web público, no asigna ningún turno en tiempo real: simplemente envía un correo al consultorio y muestra un mensaje: *"Nos contactaremos en las próximas 48 horas para coordinar su cita"*. | Se corrigió de "Reserva online comprobada" a "Formulario de contacto", ya que no valida disponibilidad ni asigna slot en tiempo real. |
| **4** | **Dental Soft Argentina** | *"Recordatorios automáticos a pacientes para eliminar ausentismo."* | La documentación técnica revela que los "recordatorios" consisten únicamente en una ventana emergente (*pop-up*) que le aparece al recepcionista al abrir el software en la computadora del consultorio. | Se reclasificó a *"No evidenciado"* para notificaciones al paciente. |
| **5** | **Dentalink** | *"Acceso universal y adaptado a cualquier consultorio particular de la región."* | La funcionalidad es 100% comprobada y de altísima calidad, pero el esquema de precios exige un piso mínimo en dólares estadounidenses (USD) que deja afuera a profesionales individuales o pequeños consultorios en Argentina. | Se mantuvo la funcionalidad como *"Comprobada"* pero se redujo su calificación en "Adaptabilidad al Mercado Argentino" a 2/5. |

---

## 8. Referencias Completas

1. **Dentalink**: Healthatom SpA. *Centro de Ayuda y Módulos de Reserva Online*. Disponible en: [https://help.softwaredentalink.com](https://help.softwaredentalink.com) (Accedido: 20 de noviembre de 2026).
2. **Doctoralia Argentina**: Docplanner Group. *Portal de Pacientes y Agendamiento Médico*. Disponible en: [https://www.doctoralia.com.ar](https://www.doctoralia.com.ar) (Accedido: 20 de noviembre de 2026).
3. **AgendaPro**: AgendaPro Inc. *Software para Clínicas Dentales y Reserva Online*. Disponible en: [https://agendapro.com/ar/software-dental](https://agendapro.com/ar/software-dental) (Accedido: 21 de noviembre de 2026).
4. **Dentidesk**: Dentidesk SpA. *Plataforma Odontológica en la Nube y Soporte*. Disponible en: [https://www.dentidesk.com](https://www.dentidesk.com) (Accedido: 21 de noviembre de 2026).
5. **Turno Dental**: *Gestión de Turnos para Consultorios Odontológicos en Argentina*. Disponible en: [https://www.turnodental.com](https://www.turnodental.com) (Accedido: 21 de noviembre de 2026).
6. **Dentiway Argentina**: *Portal Odontológico y Solicitud de Citas*. Disponible en: [https://dentiway.com.ar](https://dentiway.com.ar) (Accedido: 22 de noviembre de 2026).
7. **SoftDental**: *Sistema de Gestión Odontológica OdontoSys*. Disponible en: [https://www.softdental.com.ar](https://www.softdental.com.ar) (Accedido: 22 de noviembre de 2026).
8. **Dental Soft**: *Software Odontológico Nacional*. Disponible en: [https://www.dentalsoft.com.ar](https://www.dentalsoft.com.ar) (Accedido: 22 de noviembre de 2026).
9. **Dentalcloud**: *Software Odontológico Online*. Disponible en: [https://www.dentalcloudweb.com](https://www.dentalcloudweb.com) (Accedido: 22 de noviembre de 2026).
10. **Docfav Technologies**: *Software Dental para Consultorios y Clínicas*. Disponible en: [https://www.docfav.com/es/software-dentistas](https://www.docfav.com/es/software-dentistas) (Accedido: 23 de noviembre de 2026).
11. **Nubimed**: Nubimed Health SL. *Gestión de Clínicas Dentales y Cita Previa*. Disponible en: [https://www.nubimed.com](https://www.nubimed.com) (Accedido: 23 de noviembre de 2026).
12. **Nimbo**: Ecaresoft Inc. *Nimbo Schedule: Agenda Médica y Dental*. Disponible en: [https://www.nimbo-x.com/schedule](https://www.nimbo-x.com/schedule) (Accedido: 23 de noviembre de 2026).
13. **Simples Dental**: Dental Cremer / Henry Schein. *Software para Consultórios Odontológicos*. Disponible en: [https://www.simplesdental.com](https://www.simplesdental.com) (Accedido: 23 de noviembre de 2026).
14. **Klinikare**: *Software de Gestión Dental Avanzado*. Disponible en: [https://www.klinikare.com](https://www.klinikare.com) (Accedido: 24 de noviembre de 2026).
15. **XDental**: *Software de Odontologia e Gestão*. Disponible en: [https://www.xdental.com.br](https://www.xdental.com.br) (Accedido: 24 de noviembre de 2026).
