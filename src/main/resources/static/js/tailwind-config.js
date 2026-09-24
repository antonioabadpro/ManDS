/**
 * ManDS ERP - Configuración unificada de Tailwind CSS
 *
 * Centraliza la paleta corporativa 'brand', la fuente principal 'Inter',
 * la activación del modo oscuro mediante clase y el escalado responsivo
 * de la interfaz (Opción A):
 *   - Móviles y Tablets (< 1280px): 100% (16px base)
 *   - Escritorio Full HD (>= 1280px - xl): 105% (~16.8px)
 *   - Pantallas 2K/4K (>= 1536px - 2xl): 110% (17.6px)
 */

window.tailwind = window.tailwind || {};
window.tailwind.config = {
    darkMode: 'class',
    theme: {
        extend: {
            fontFamily: {
                sans: ['Inter', 'sans-serif'],
            },
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
    plugins: [
        function ({ addBase }) {
            addBase({
                'html': {
                    'font-size': '100%',
                    '@media (min-width: 1280px)': {
                        'font-size': '105%',
                    },
                    '@media (min-width: 1536px)': {
                        'font-size': '110%',
                    }
                }
            });
        }
    ]
};
