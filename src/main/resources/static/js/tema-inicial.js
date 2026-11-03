/**
 * tema-inicial.js - Inicialización temprana y síncrona del tema visual (Claro / Oscuro)
 *
 * =====================================================================================
 * 1. PREVENCIÓN DE FOUC (Flash of Unstyled Content):
 *    Si esperáramos a que se descargue todo el HTML y se ejecuten los scripts diferidos
 *    (con 'defer'), la pantalla primero se pintaría en blanco y, fracciones de segundo
 *    después, se volvería oscura al leer la preferencia. Esto causa un parpadeo molesto.
 *
 * 2. PARSER-BLOCKING DELIBERADO:
 *    Este archivo se enlaza en el <head> de cada vista SIN 'defer' ni 'async'. Por tanto,
 *    el navegador detiene temporalmente la lectura del HTML (bloqueo del parser), ejecuta
 *    esta función en menos de 1 milisegundo y, una vez inyectada la clase 'dark' en el
 *    elemento raíz <html> (document.documentElement), continúa renderizando el <body>.
 *    Resultado: cuando los elementos visuales se pintan en pantalla, ya nacen oscuros.
 *
 * 3. PATRÓN IIFE (Immediately Invoked Function Expression):
 *    Se envuelve en '(function () { ... })();' para crear un ámbito local privado. De esta
 *    forma, las variables temporales no contaminan el objeto global 'window'.
 * =====================================================================================
 */
(function () {
    'use strict'; // Activa el modo estricto de JS para evitar errores silenciosos y malas prácticas

    try {
        // Clave idéntica a la utilizada en layout.js para persistir la elección del usuario
        var CLAVE_TEMA = 'mands_theme';

        // 1. Consultar si el usuario guardó previamente una preferencia manual en su navegador
        var temaGuardado = localStorage.getItem(CLAVE_TEMA);

        // 2. Si no hay preferencia guardada, consultar la preferencia del Sistema Operativo (Windows/macOS/Linux)
        // matchMedia evalúa la media query CSS 'prefers-color-scheme: dark'
        var prefiereOscuro = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;

        // 3. Determinar si corresponde activar el tema oscuro:
        //    - O bien el usuario eligió explícitamente 'dark'.
        //    - O bien no eligió nada (primera visita) pero su sistema operativo está en modo oscuro.
        if (temaGuardado === 'dark' || (!temaGuardado && prefiereOscuro)) {
            // Inyecta la clase CSS 'dark' en la etiqueta raíz <html class="dark">
            // Esto le indica a Tailwind CSS que active todas las variantes 'dark:*'
            document.documentElement.classList.add('dark');
        } else {
            // Remueve la clase 'dark' para asegurar modo claro
            document.documentElement.classList.remove('dark');
        }
    } catch (e) {
        // Bloque de seguridad (fallback):
        // En modos de navegación privada estricta o cookies bloqueadas, acceder a 'localStorage'
        // puede lanzar una excepción de seguridad (DOMException). Si ocurre, lo capturamos
        // de forma silenciosa para que la página continúe cargando con normalidad.
    }
})();
