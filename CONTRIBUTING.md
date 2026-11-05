# Guía de Contribución

¡Gracias por tu interés en contribuir a este proyecto! Todas las contribuciones, desde reportes de errores hasta nuevas características, son bienvenidas y apreciadas.

Para asegurar un proceso fluido y colaborativo, te pedimos que leas y sigas estas pautas.

## 🐛 Reportar un Problema (Issues)

Si encuentras un error o tienes una sugerencia para una nueva funcionalidad:

1. **Revisa los Issues existentes:** Antes de crear un nuevo ticket, busca en la pestaña de [Issues](../../issues) para asegurarte de que nadie más lo haya reportado ya.
2. **Crea un Issue:** Si no existe, abre un nuevo Issue. 
3. **Sé descriptivo:** Proporciona la mayor cantidad de detalles posible. Si es un error, incluye pasos para reproducirlo, el comportamiento esperado y el comportamiento actual.

## 💻 Entorno de Desarrollo local

Para trabajar en este proyecto en tu máquina local, necesitarás tener instalados:

* **Java JDK 17**
* **Maven**
* (Opcional) Docker, si deseas probar los contenedores localmente.

Para compilar el proyecto y ejecutar las pruebas localmente antes de enviar tu código, utiliza el siguiente comando:

```bash
mvn clean test
```

## 🛠️ Proceso para enviar código (Pull Requests)

Si deseas contribuir con código, sigue este flujo de trabajo:

1. **Haz un Fork del repositorio** a tu propia cuenta de GitHub.
2. **Clona tu fork** en tu máquina local:
   ```bash
   git clone https://github.com/TU-USUARIO/nombre-del-proyecto.git
   ```
3. **Crea una nueva rama** para tu característica o corrección de error. Usa un nombre descriptivo (por ejemplo, `feature/nueva-funcion` o `bugfix/correccion-login`):
   ```bash
   git checkout -b feature/nombre-de-tu-rama
   ```
4. **Haz tus cambios** en el código. Asegúrate de seguir las convenciones de estilo de código del proyecto y de escribir pruebas (tests) para tus nuevas funcionalidades si aplica.
5. **Haz Commit de tus cambios.** Escribe mensajes de commit claros y concisos en tiempo imperativo (ej. "Añade validación de usuario" en lugar de "Añadida la validación"):
   ```bash
   git commit -m "Descripción clara de lo que hace este commit"
   ```
6. **Haz Push de tu rama** a tu fork en GitHub:
   ```bash
   git push origin feature/nombre-de-tu-rama
   ```
7. **Abre un Pull Request (PR)** hacia la rama `main` (o `develop`) del repositorio original.

## ✅ Requisitos para que tu Pull Request sea aceptado

* **CI/CD:** Tu código debe pasar todas las pruebas automáticas (GitHub Actions) que se ejecutan al crear el PR.
* **Revisión:** Un mantenedor del proyecto revisará tu código. Es posible que te dejen comentarios solicitando algunos ajustes antes de aprobarlo.
* **Sin conflictos:** Asegúrate de que tu rama esté actualizada con la rama principal para evitar conflictos de fusión.

¡Gracias de nuevo por ayudar a mejorar este proyecto!