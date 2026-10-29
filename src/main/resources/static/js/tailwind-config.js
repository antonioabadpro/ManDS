/**
 * tailwind-config.js - Configuración centralizada de Tailwind CSS para ManDS ERP
 *
 * =====================================================================================
 * 1. CONFIGURACIÓN EN TIEMPO DE EJECUCIÓN (JIT CDN):
 *    Al utilizar la versión de Tailwind distribuida por CDN (tailwindcss.com), las reglas
 *    de diseño personalizadas deben inyectarse en el objeto global 'window.tailwind.config'
 *    antes de que Tailwind procese las clases CSS del DOM.
 *
 * 2. ESTRATEGIA DE MODO OSCURO (darkMode: 'class'):
 *    Se configura 'class' para que Tailwind active los estilos oscuros (dark:...) únicamente
 *    cuando la etiqueta raíz <html class="dark"> contiene la clase 'dark'. Esto otorga
 *    control total a nuestra aplicación mediante JavaScript (tema-inicial.js y layout.js).
 *
 * 3. TIPOGRAFÍA Y ESCALADO RESPONSIVO PROPORCIONAL:
 *    - Fuente única corporativa: 'Inter' (prohibiendo fuentes monoespaciadas).
 *    - Plugin base para reescalar la fuente raíz ('html' font-size) en monitores grandes (xl y 2xl),
 *      haciendo que toda la interfaz (botones, textos, tablas) crezca de manera proporcional.
 * =====================================================================================
 */

// Garantiza la existencia del objeto de configuración sin sobrescribir propiedades previas
window.tailwind = window.tailwind || {};

window.tailwind.config = {
    // Modo oscuro controlado por la presencia de la clase CSS 'dark' en el elemento <html>
    darkMode: 'class',

    theme: {
        extend: {
            // Definición de la familia tipográfica principal
            fontFamily: {
                sans: ['Inter', 'sans-serif'],
            },

            // Paleta de colores corporativos 'brand' (Azul/Slate profesional para ERP)
            // Permite usar clases utilitarias como 'bg-brand-600', 'text-brand-500', 'border-brand-200', etc.
            colors: {
                brand: {
                    50: '#eff6ff',
                    100: '#dbeafe',
                    200: '#bfdbfe',
                    300: '#93c5fd',
                    400: '#60a5fa',
                    500: '#3b82f6',
                    600: '#2563eb',
                    700: '#1d4ed8',
                    800: '#1e40af',
                    900: '#1e3a8a',
                    950: '#0f172a',
                }
            }
        }
    },

    // Plugins personalizados de Tailwind:
    // Aquí definimos una función que inyecta reglas CSS directamente en los estilos base ('html')
    plugins: [
        function ({ addBase }) {
            addBase({
                'html': {
                    // Tamaño base estándar en dispositivos móviles y portátiles (< 1280px): 100% = 16px
                    'font-size': '100%',

                    // En pantallas de escritorio Full HD (ancho >= 1280px - breakpoint 'xl'):
                    // Aumenta un 5% el tamaño de la interfaz (~16.8px) para mejorar legibilidad
                    '@media (min-width: 1280px)': {
                        'font-size': '105%',
                    },

                    // En monitores de alta resolución 2K o 4K (ancho >= 1536px - breakpoint '2xl'):
                    // Aumenta un 10% el tamaño de la interfaz (~17.6px) evitando elementos diminutos
                    '@media (min-width: 1536px)': {
                        'font-size': '110%',
                    }
                }
            });
        }
    ]
};
