-- ==============================================================================
-- SCRIPT DE DATOS SEMILLA PARA EL SISTEMA ERP DE AUTOESCUELAS (H2 / DESARROLLO)
-- ==============================================================================
-- Este script puebla todas las entidades del dominio con datos consistentes,
-- respetando la jerarquía de herencia JOINED y las restricciones de integridad referencial.
--
-- Credenciales de acceso por defecto (BCrypt):
--   * Administrador : admin / admin123
--   * Profesor 1    : profesor1 / profesor123
--   * Profesor 2    : profesor2 / profesor123
--   * Alumno 1      : alumno1 / alumno123
--   * Alumno 2      : alumno2 / alumno123
--   * Alumno 3      : alumno3 / alumno123
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. FLOTA DE VEHÍCULOS (vehiculo)
-- ------------------------------------------------------------------------------
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (1, '1234-LMN', 'SEAT', 'Ibiza 1.0 TSI', 'Blanco Nevada', 45000, '2026-01-10', '2027-01-10', 'DISPONIBLE', 'PERMISO_B');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (2, '5678-KPR', 'Renault', 'Clio E-Tech', 'Azul Rayo', 32000, '2025-11-20', '2026-11-20', 'OCUPADO', 'PERMISO_B');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (3, '9012-HJK', 'Yamaha', 'MT-07', 'Negro Midnight', 12000, '2026-02-15', '2027-02-15', 'DISPONIBLE', 'PERMISO_A2');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (4, '3456-FGH', 'Mercedes-Benz', 'Actros 1845', 'Rojo Carmesí', 180000, '2025-09-05', '2026-09-05', 'MANTENIMIENTO', 'PERMISO_C');

-- ------------------------------------------------------------------------------
-- 2. USUARIOS BASE (persona)
-- ------------------------------------------------------------------------------
-- 2.1 Administrador (id = 1)
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (1, 'Antonio Abad', 'Hernández Gálvez', '12345678A', '1985-04-12', 'admin', 'admin@autoescuela.es', '$2a$10$umISk5UJjoOGrFmQXaaANernpH1OxhBA/NHx.TpkKBCNfau0s4uUi', '600111222', 'Calle Gran Vía 28, Madrid', 'ACTIVO');

-- 2.2 Profesores (id = 2, 3)
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (2, 'Laura', 'Sánchez Romero', '23456789B', '1988-09-23', 'profesor1', 'laura.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600222333', 'Avenida de América 15, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (3, 'Manuel', 'Navarro Torres', '34567890C', '1990-11-05', 'profesor2', 'manuel.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600333444', 'Calle Alcalá 120, Madrid', 'ACTIVO');

-- 2.3 Alumnos (id = 4, 5, 6)
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (4, 'Elena', 'Martínez López', '45678901D', '2004-03-15', 'alumno1', 'elena.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600444555', 'Calle Princesa 42, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (5, 'David', 'Ruiz Gómez', '56789012E', '2003-07-28', 'alumno2', 'david.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600555666', 'Paseo de la Castellana 80, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (6, 'Sofía', 'Fernández Castro', '67890123F', '2005-12-02', 'alumno3', 'sofia.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600666777', 'Calle Bravo Murillo 55, Madrid', 'ACTIVO');

-- ------------------------------------------------------------------------------
-- 3. PERFILES ESPECÍFICOS (administrador, profesor, profesor_permisos, alumno)
-- ------------------------------------------------------------------------------
-- 3.1 Administrador
INSERT INTO administrador (id) VALUES (1);

-- 3.2 Profesores y asignación de vehículos
INSERT INTO profesor (id, fecha_contratacion, turno, vehiculo_id)
VALUES (2, '2022-01-15', 'MATINAL', 1);

INSERT INTO profesor (id, fecha_contratacion, turno, vehiculo_id)
VALUES (3, '2023-03-01', 'TARDE', 2);

-- 3.3 Permisos de Carnet autorizados por Profesor
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (2, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (2, 'PERMISO_A2');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (3, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (3, 'PERMISO_C');

-- 3.4 Alumnos y asignación de Profesor
INSERT INTO alumno (id, profesor_id) VALUES (4, 2);
INSERT INTO alumno (id, profesor_id) VALUES (5, 2);
INSERT INTO alumno (id, profesor_id) VALUES (6, 3);

-- ------------------------------------------------------------------------------
-- 4. INCIDENCIAS DE VEHÍCULOS (incidencia_vehiculo)
-- ------------------------------------------------------------------------------
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (1, '2026-08-20 09:30:00', 'Ruido anómalo en pastillas de freno delanteras al frenar en pendiente pronunciada.', 'PENDIENTE', 4, 3);

INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (2, '2026-08-10 16:15:00', 'Sustitución rutinaria de neumático delantero derecho por pinchazo en vía urbana.', 'RESUELTA', 1, 2);

-- ------------------------------------------------------------------------------
-- 5. MATRÍCULAS ACADÉMICAS (matricula)
-- ------------------------------------------------------------------------------
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, num_clases_pendientes_confirmar, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (1, TRUE, 'PERMISO_B', 2, 5, 1, 0, 450.00, '2026-05-10', 'NUEVA', 'TEORICO_PRACTICA', 4);

INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, num_clases_pendientes_confirmar, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (2, TRUE, 'PERMISO_B', 1, 0, 0, 1, 320.00, '2026-04-01', 'RENOVACION', 'PRACTICA', 5);

INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, num_clases_pendientes_confirmar, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (3, TRUE, 'PERMISO_A2', 2, 8, 0, 0, 390.00, '2026-06-15', 'NUEVA', 'INDIVIDUAL', 6);

-- ------------------------------------------------------------------------------
-- 6. CLASES PRÁCTICAS (clase_practica)
-- ------------------------------------------------------------------------------
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (1, '2026-09-10 10:00:00', 45, 'Calle Alcalá 45 (Metro Goya)', 44955, 45000, 'Excelente dominio del embrague y correcta circulación en glorietas.', 'RECIBIDA', 4, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (2, '2026-09-15 11:00:00', 45, 'Calle Alcalá 45 (Metro Goya)', 45000, 45000, 'Práctica programada: maniobras de estacionamiento en línea y batería.', 'PENDIENTE', 4, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (3, '2026-09-08 17:00:00', 45, 'Estación de Atocha', 31950, 31950, 'Cancelada por el alumno con más de 24h de antelación por motivos de fuerza mayor.', 'CANCELADA', 5, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (4, '2026-09-11 16:30:00', 60, 'Pista de prácticas Norte', 11950, 12000, 'Maniobras en circuito cerrado superadas con fluidez (zig-zag y frenada de emergencia).', 'RECIBIDA', 6, 3);

-- ------------------------------------------------------------------------------
-- 7. SOLICITUDES DE EXAMEN (solicitud_examen)
-- ------------------------------------------------------------------------------
INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (1, 'ACEPTADA', 'Alumno preparado tras superar 30 test de examen en plataforma con menos de 2 fallos.', 4, 2, 1);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (2, 'PENDIENTE', 'Solicitud para la próxima convocatoria práctica de la DGT en Móstoles.', 5, 2, 2);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (3, 'RECHAZADA', 'Se aconseja realizar al menos 2 prácticas más para afianzar incorporaciones a autovía.', 6, 3, 3);

-- ------------------------------------------------------------------------------
-- 8. EXÁMENES DGT (examen)
-- ------------------------------------------------------------------------------
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (1, TRUE, '2026-07-20 09:00:00', 30, 'TEORICO', 4, 1);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (2, FALSE, '2026-08-05 10:30:00', 25, 'PRACTICO', 5, NULL);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (3, NULL, '2026-09-25 09:00:00', 35, 'PRACTICO', 4, NULL);

-- ------------------------------------------------------------------------------
-- 9. REAJUSTE DE SECUENCIAS DE IDENTIDAD (H2)
-- ------------------------------------------------------------------------------
-- Permite que las inserciones posteriores desde los formularios de la aplicación
-- no colisionen con los IDs insertados en este script semilla.
ALTER TABLE vehiculo ALTER COLUMN id RESTART WITH 100;
ALTER TABLE persona ALTER COLUMN id RESTART WITH 100;
ALTER TABLE incidencia_vehiculo ALTER COLUMN id RESTART WITH 100;
ALTER TABLE matricula ALTER COLUMN id RESTART WITH 100;
ALTER TABLE clase_practica ALTER COLUMN id RESTART WITH 100;
ALTER TABLE solicitud_examen ALTER COLUMN id RESTART WITH 100;
ALTER TABLE examen ALTER COLUMN id RESTART WITH 100;
