# EncuestaAPP - Sistema de Votación y Recolección de Datos en Tiempo Real

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)
![Material Design](https://img.shields.io/badge/Material%203-757575?style=for-the-badge&logo=material-design&logoColor=white)
![Lottie](https://img.shields.io/badge/Lottie-00D2B4?style=for-the-badge&logo=lottie&logoColor=white)

**EncuestaAPP** es una aplicación Android diseñada para facilitar la participación ciudadana a través de votaciones rápidas y cuestionarios detallados. Utiliza una infraestructura en la nube para procesar y mostrar resultados en tiempo real, proporcionando una experiencia interactiva y moderna.

## 🌟 Características Principales

### 🗳️ Votación en Tiempo Real
- **Registro Instantáneo**: Los votos se sincronizan automáticamente con Firebase Realtime Database.
- **Visualización Dinámica**: Gráficos de progreso que se actualizan al instante conforme otros usuarios participan.
- **Control de Voto Único**: Implementación de lógica local (SharedPreferences) para evitar votos múltiples desde el mismo dispositivo.

### 📋 Cuestionario Demográfico Detallado
- **Perfil del Votante**: Recolección de datos sobre edad, zona de residencia y género.
- **Análisis de Motivación**: Preguntas sobre propuestas favoritas, motivos de elección y nivel de confianza en el candidato.
- **Fuentes de Información**: Seguimiento de cómo se informan los ciudadanos (redes sociales, televisión, etc.).

### 🎨 Interfaz de Usuario Moderna
- **Material Design 3**: Uso de componentes actualizados para una navegación fluida.
- **Animaciones Lottie**: Feedback visual mediante animaciones fluidas al completar acciones con éxito.
- **Diseño Adaptable**: Interfaz optimizada para diferentes tamaños de pantalla y orientaciones.

## 🛠️ Stack Tecnológico

- **Lenguaje**: Kotlin
- **Arquitectura**: Fragment-based Navigation con ViewBinding para un manejo seguro de vistas.
- **Backend**: Firebase Realtime Database para persistencia y sincronización en vivo.
- **UI/UX**:
    - `Material Components` para botones, diálogos y campos de texto.
    - `Lottie` para animaciones interactivas.
    - `ObjectAnimator` para transiciones suaves en las barras de progreso.
- **Gestión de Datos**: Corrutinas de Kotlin para operaciones asíncronas seguras.

## 📁 Estructura del Proyecto

```text
com.example.encuestaapp/
├── WelcomeFragment.kt         # Pantalla de bienvenida e inicio.
├── HomeFragment.kt            # Panel de votación y resultados en vivo.
├── CuestionarioVotoFragment.kt # Formulario detallado post-voto.
├── MainActivity.kt            # Contenedor principal de fragmentos.
└── databinding/               # Generado automáticamente para acceso a vistas.
```

## 🚀 Instalación y Configuración

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/tu-usuario/encuestaAPP.git
   ```

2. **Configurar Firebase**:
   - Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
   - Añade una aplicación Android con el paquete `com.example.encuestaapp`.
   - Descarga `google-services.json` y colócalo en la carpeta `app/`.
   - Habilita **Realtime Database** y configura las reglas de seguridad (lectura/escritura pública para pruebas o autenticada para producción).

3. **Ejecutar**:
   - Abre el proyecto en **Android Studio**.
   - Sincroniza Gradle.
   - Ejecuta en un emulador o dispositivo físico con Android 8.0 (API 26) o superior.

---

Desarrollado como una solución eficiente para el análisis de tendencias electorales y participación ciudadana. 🚀
