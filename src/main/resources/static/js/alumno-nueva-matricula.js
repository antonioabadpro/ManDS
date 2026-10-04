/**
 * alumno-nueva-matricula.js
 * Actualización dinámica del desglose de precios al seleccionar un nuevo permiso en el panel del alumno.
 * Conforme con directivas CSP y principio Unobtrusive JS.
 */
document.addEventListener('DOMContentLoaded', () => {
    const selectorCarnet = document.getElementById('selector-tipo-carnet');
    const cuotaAutoescuelaTexto = document.getElementById('cuota-autoescuela-texto');
    const precioTotalTexto = document.getElementById('precio-total-texto');

    if (!selectorCarnet || !cuotaAutoescuelaTexto || !precioTotalTexto) {
        return;
    }

    const actualizarPrecios = () => {
        const valorSeleccionado = selectorCarnet.value;
        const esPesado = valorSeleccionado.startsWith('PERMISO_C') || valorSeleccionado.startsWith('PERMISO_D');

        if (esPesado) {
            cuotaAutoescuelaTexto.textContent = '355,95 €';
            precioTotalTexto.textContent = '450,00 €';
        } else {
            cuotaAutoescuelaTexto.textContent = '155,95 €';
            precioTotalTexto.textContent = '250,00 €';
        }
    };

    selectorCarnet.addEventListener('change', actualizarPrecios);
});
