# 🛡️ Bustemu-Retro 1.43.7

Un emulador avanzado basado en Java y desarrollado íntegramente en **Español-Latino**, **adaptado y pensado para el cliente 1.43.7**. Este proyecto es el núcleo técnico para la comunidad **Onesv**, optimizado para el rendimiento y la facilidad de modificación.

> **⚠️ Versión del cliente:** Este emulador está **adaptado al cliente 1.43.7**. No está diseñado para otras versiones del cliente Dofus.

---

## 🤖 Mentoría con Inteligencia Artificial

Para acelerar tu curva de aprendizaje, hemos integrado este repositorio con **DeepWiki**. La IA tiene acceso completo al código fuente en español y al diccionario de datos para ayudarte a resolver dudas técnicas al instante.

🚀 **[Consultar a la IA en DeepWiki](https://deepwiki.com/maestroyaco/emu-b)**

---

## ⚙️ Especificaciones Técnicas

| Característica | Detalle |
| :--- | :--- |
| **Nombre del Proyecto** | Bustemu-Retro 1.43.7 |
| **Lenguaje Base** | Java |
| **Versión de Java** | **17 o 21** (LTS) |
| **Build** | **Gradle** (Kotlin DSL) |
| **Versión del Cliente** | **1.43.7** (adaptado) |
| **Idioma del Código** | Español (Spanish) |
| **Documentación DB** | Markdown (docs/database/) |

### Carga de mapas desde memoria

Este emulador **carga los mapas desde la memoria del servidor** (y/o BD), **no desde archivos .swf**. El cliente 1.43.7 recibe los datos del mapa vía paquete **GDM** (nunca **GD**); las entidades se envían con **GM** y se cierra la secuencia con **GDK**. No se usa descarga de .swf en ningún flujo.

---

## 📂 Organización del Repositorio

- `src/`: Lógica central del servidor escrita en Java (Clases, Protocolos, Motor de Batalla).
- `docs/database/`: Diccionario técnico con las tablas MySQL convertidas a Markdown para fácil lectura.

---

## 🌐 Soporte y Comunidad (Onesv)

Formamos parte de la comunidad **Onesv**, apoyando a desarrolladores de **España (ES), Francia (FR) y Portugal (PT)**.

- **🌐 Blog:** [Maestro-Yaco](https://maestro-yaco.blogspot.com/)
- **🌍 Web Oficial:** [onesv.fun](https://onesv.fun/)
- **💬 Discord Oficial:** [Únete al servidor](https://discord.gg/yZnADDUKHx)

---

## 📥 Clonar y compilar

```bash
git clone https://github.com/maestroyaco/Bustemu-Retro-1.43.7.git
cd Bustemu-Retro-1.43.7
```

**Requisitos:** Java 17 o 21.

**Compilar y ejecutar con Gradle:**

```bash
# Windows
.\gradlew.bat build
.\gradlew.bat run

# Linux / macOS / WSL
./gradlew build
./gradlew run
```

**Tests:**

```bash
.\gradlew.bat test   # Windows
./gradlew test       # Linux / macOS
```

**JAR con dependencias (fat JAR):**

```bash
.\gradlew.bat jar
# El JAR se genera en build/libs/
```

Si ya tienes un repositorio local y quieres apuntar a este remoto:

```bash
git remote add origin https://github.com/maestroyaco/Bustemu-Retro-1.43.7.git
```

### Despliegue en VPS (Ubuntu)

- Usa Java 17 o 21 instalado en el servidor.
- Ejecuta con `./gradlew run` o `java -jar build/libs/<nombre>.jar`.
- Si se usa JEP u otras librerías nativas, instala las dependencias del sistema que requieran (p. ej. `libjep` en Linux).

---

*💡 **Nota para nuevos desarrolladores:** Gracias a que el código está en español, la exploración mediante [DeepWiki](https://deepwiki.com/maestroyaco/emu-b) es sumamente intuitiva. Ideal para quienes buscan entender cómo funciona un emulador por dentro sin barreras de idioma. Este proyecto está **adaptado al cliente 1.43.7**.*
