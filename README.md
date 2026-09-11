# Pasos para que la BD PostgreSQL (Supabase) funcione

A continuación se presentan dos opciones alternativas para configurar el acceso a la base de datos de Supabase.

---

### 🛠️ Opción 1: Configuración mediante variables de entorno

Esta opción te permite gestionar las credenciales de forma segura en el sistema o en un archivo local de entorno.

* **Paso:** Crear las variables de entorno (`.env`) correspondientes.
* **Detalle:** Asegúrate de incluir el usuario y la contraseña de Supabase para poder acceder correctamente a dicha información.

---

### ⚙️ Opción 2: Configuración mediante archivo de lanzamiento

Esta opción es ideal si trabajas con entornos de desarrollo específicos que leen la configuración directamente del editor.

* **Paso:** Copiar el fichero `launch.json`.
* **Ubicación:** Este archivo debe colocarse dentro de la carpeta raíz `.vscode`.
