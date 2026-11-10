# Smart Home Management System

Aplicación de escritorio para gestionar y automatizar un hogar inteligente. Parcial 3 de Programación 2 · Patrones de comportamiento · Java 21, JavaFX y FXML.

- **Repositorio:** _(completar con la URL de GitHub)_
- **Video de presentación:** _(completar con el enlace de YouTube)_
- **Integrantes:** _(completar con nombres y participación de cada uno)_

## Funcionalidades

- Vista en árbol de la casa: habitaciones y dispositivos (luces, termostatos y cerraduras).
- Encender/apagar un dispositivo o una habitación completa con un solo botón.
- Ajustar el brillo de una luz y la temperatura de un termostato.
- Elegir la estrategia de cada luz (Adaptativa, Máximo, Relajante) y de cada termostato (Confort, Ahorro, Nocturno) y aplicar el valor que sugiere.
- Escenas: «Buenas noches», «Modo cine» y «Salir de casa».
- Automatización: al asegurar la puerta se apagan las luces y los termostatos pasan a ahorro; al liberarla de noche se encienden las luces de su habitación.
- Simular y reparar la falla de un dispositivo; un dispositivo con falla rechaza órdenes y configuración.
- Deshacer y rehacer de todas las acciones, incluidas las escenas.
- Agregar habitaciones y dispositivos en tiempo de ejecución.
- Registro de actividad con todo lo que ocurre (órdenes, cambios de estado, reglas, rechazos y alertas).

## Requisitos y ejecución

- JDK 21
- Gradle 8.x (el plugin `org.openjfx.javafxplugin` descarga JavaFX 21 automáticamente)

```bash
git clone <URL-del-repositorio>
cd smart-home
gradle run
```

El proyecto no incluye Gradle Wrapper. Si no tienes Gradle instalado, puedes generarlo una vez con `gradle wrapper` y usar `./gradlew run`.

## Estructura del proyecto

```
src/main/java/com/smarthome/
├── App.java                 Punto de entrada JavaFX (carga el FXML)
├── model/                   Controllable, Device, Light, Thermostat, DoorLock, Room, DeviceType
├── factory/                 DeviceFactory y sus tres creadores concretos
├── state/                   DeviceState, OnState, OffState, FaultState
├── observer/                DeviceObserver, DeviceEvent, ActivityLog
├── strategy/                Estrategias de iluminación y de clima
├── command/                 Command, comandos concretos, MacroCommand, CommandInvoker
├── mediator/                HomeMediator, SmartHomeMediator, HomeEnvironment
├── facade/                  SmartHomeFacade
└── ui/                      MainController
src/main/resources/
├── fxml/main-view.fxml      Interfaz (FXML)
└── css/style.css
```

El controlador de la interfaz solo habla con `SmartHomeFacade`; no conoce comandos, fábricas ni el mediador.

## Patrones implementados

### Patrones de comportamiento (obligatorios)

| Patrón | Ubicación | Problema que resuelve y uso en el sistema |
|---|---|---|
| **Observer** | `observer/`, `model/Device.java` (`addObserver`, `publish`) | Los dispositivos publican un `DeviceEvent` cuando cambian de estado, se configuran o rechazan una orden. Lo escuchan tres observadores: `ActivityLog` (registro), `SmartHomeMediator` (automatización) y la fachada, que avisa a la UI para refrescarse. El dispositivo no sabe quién lo observa. |
| **Strategy** | `strategy/`, `Light.setStrategy`, `Thermostat.setStrategy` | El brillo o la temperatura recomendados dependen de la hora y la temperatura exterior, y la forma de calcularlos cambia según la preferencia. Cada estrategia es un algoritmo intercambiable en tiempo de ejecución desde la UI, y también lo usan el botón «Aplicar valor sugerido» y la automatización. |
| **Command** | `command/` (`Command`, `PowerCommand`, `SetBrightnessCommand`, `SetTemperatureCommand`, `MacroCommand`, `CommandInvoker`) | Cada acción del usuario es un objeto con `execute()` y `undo()`. `CommandInvoker` mantiene las pilas de deshacer/rehacer. `MacroCommand` agrupa varios comandos en una escena. |
| **State** | `state/`, `model/Device.java` (`changeState`) | Un dispositivo reacciona distinto a encender o apagar según esté Encendido, Apagado o Con falla. `FaultState` rechaza órdenes y configuración, y cada transición se publica como evento. |
| **Mediator** | `mediator/SmartHomeMediator.java` | Cerraduras, luces y termostatos no se conocen entre sí. Informan al mediador, que decide qué debe hacer el resto (modo ausente, luces de bienvenida, alertas por falla). Se puede desactivar desde la barra superior. |

### Patrones adicionales (peso de complejidad total: 6)

| Patrón | Categoría | Peso | Ubicación | Justificación |
|---|---|---|---|---|
| **Factory Method** | Creacional | 2 | `factory/DeviceFactory.java` y subclases | Agregar dispositivos en tiempo de ejecución con nombre por defecto y configuración inicial propios de cada tipo. Sumar un nuevo tipo solo exige una nueva fábrica. |
| **Composite** | Estructural | 3 | `model/Controllable.java`, `model/Room.java` | Una habitación contiene dispositivos u otras habitaciones y se controla igual que un dispositivo: apagar «Casa» apaga todo. La UI y los comandos tratan a ambos por la misma interfaz. |
| **Facade** | Estructural | 1 | `facade/SmartHomeFacade.java` | Oculta la construcción del hogar, las fábricas, el invocador, el mediador y el registro detrás de una API pequeña, lo que mantiene la UI desacoplada. |

### Mecanismo auxiliar

`SnapshotCommand` (en `command/`) envuelve cada comando y guarda una foto del estado de la casa antes de ejecutarlo. Al deshacer, restaura lo que el mediador cambió por su cuenta y que el comando no conoce (por ejemplo, las luces que apagó el modo ausente). Es un apoyo del patrón Command y no se cuenta como patrón adicional.

## Flujo de ejemplo

1. El usuario pulsa **Asegurar** en «Puerta principal».
2. La fachada ejecuta un `PowerCommand` a través del `CommandInvoker`.
3. La cerradura cambia de `OffState` a `OnState` (**State**) y publica un evento (**Observer**).
4. `ActivityLog` lo registra y `SmartHomeMediator` aplica el modo ausente (**Mediator**): apaga las luces y cambia los termostatos a la estrategia de ahorro (**Strategy**).
5. La UI se refresca. **Deshacer** revierte la puerta y también lo que hizo el mediador.

## Limitaciones conocidas

- El estado no se guarda al cerrar la aplicación.
- La hora de las estrategias es la del reloj del sistema; la temperatura exterior se ajusta a mano.
- Simular o reparar una falla no se puede deshacer.
- Cambiar la estrategia de una luz o un termostato desde la lista desplegable tampoco entra en el historial de deshacer; sí lo hacen el encendido, el brillo, la temperatura y las escenas.
