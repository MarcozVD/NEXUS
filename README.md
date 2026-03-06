# NEXUS - Fintech & Banking App

NEXUS es una aplicación móvil de servicios financieros (Fintech) de vanguardia, diseñada para ofrecer una experiencia de banca digital fluida, segura y moderna. Desarrollada con las últimas tecnologías de Android, la aplicación permite a los usuarios gestionar su patrimonio, realizar transferencias y controlar sus productos financieros desde un solo lugar.

## 🚀 Características Principales

### 🔐 Seguridad y Acceso
- **Autenticación Biométrica**: Acceso rápido y seguro mediante huella dactilar o reconocimiento facial integrando `androidx.biometric`.
- **Registro y Login**: Sistema robusto de gestión de usuarios mediante **Firebase Authentication**.
- **Cifrado de Datos**: Utiliza `androidx.security:security-crypto` para proteger información sensible localmente.

### 💰 Gestión Financiera
- **Pantalla de Inicio Dinámica**: Resumen detallado de saldos, movimientos recientes y accesos directos a funciones clave.
- **Transferencias (Send Money)**: Envío de dinero simplificado entre cuentas o contactos.
- **Ahorros (Savings)**: Herramientas dedicadas para gestionar metas de ahorro y visualizar el progreso.
- **Inversiones (Investments)**: Módulo para explorar y gestionar opciones de inversión y crecimiento de capital.

### 💳 Productos y Servicios
- **Nexus Card**: Gestión completa de tarjetas digitales y físicas, incluyendo visualización de detalles y estados.
- **Billetera Digital (Wallet)**: Control centralizado de fondos y medios de pago.
- **Notificaciones**: Sistema de alertas en tiempo real para transacciones y eventos importantes.

## 🛠️ Stack Tecnológico

- **Lenguaje**: [Kotlin](https://kotlinlang.org/) (100%)
- **Interfaz de Usuario**: [Jetpack Compose](https://developer.android.com/jetpack/compose) con **Material Design 3**.
- **Backend**: [Firebase](https://firebase.google.com/) (Auth, Firestore, Analytics).
- **Arquitectura**: Patrón moderno de navegación con `Navigation Compose`.
- **Versionado Mínimo**: Android 7.0 (API 24).
- **Herramientas de Construcción**: Gradle (Kotlin DSL).

## 📁 Estructura del Proyecto

El código fuente se organiza de la siguiente manera:

- `com.Marcos.nexus`: Paquete principal que contiene la lógica de navegación y las pantallas.
  - `MainActivity.kt`: Punto de entrada que orquestra la navegación (`NavHost`).
  - `*Screen.kt`: Componentes Compose que definen cada vista (Home, Wallet, Savings, etc.).
  - `ui/theme`: Definiciones de colores, tipografía y estilos del sistema de diseño.

## 🏁 Comenzando

### Requisitos Previos
- **Android Studio Jellyfish (o superior)**.
- **JDK 11** configurado.
- Un dispositivo Android o emulador con **API 24+**.

### Instalación
1. Clona el repositorio:
   ```bash
   git clone https://github.com/MarcozVD/NEXUS.git
   ```
2. Abre el proyecto en Android Studio.
3. Asegúrate de tener el archivo `google-services.json` configurado en `app/` (necesario para Firebase).
4. Ejecuta el proyecto en tu dispositivo pulsando "Run".

## 🛡️ Licencia
Este proyecto es privado/propiedad de su desarrollador. Todos los derechos reservados.

---
*Desarrollado con ❤️ por Marcos y el equipo de NEXUS.*
