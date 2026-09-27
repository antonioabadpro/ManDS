-- ==============================================================================
-- SCRIPT DE DATOS SEMILLA PARA EL SISTEMA ERP DE AUTOESCUELAS (H2 / DESARROLLO)
-- ==============================================================================
-- Este script puebla todas las entidades del dominio con datos consistentes,
-- respetando la jerarquía de herencia JOINED y las restricciones de integridad referencial.
--
-- Credenciales de acceso por defecto (BCrypt):
--   * Administrador : admin / admin123
--   * Profesor 1..5 : profesor1 .. profesor5 / profesor123
--   * Alumno 1..26  : alumno1 .. alumno26 / alumno123
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

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (5, '7890-XYZ', 'Peugeot', '208 PureTech', 'Gris Platino', 62000, '2026-03-12', '2027-03-12', 'MANTENIMIENTO', 'PERMISO_B');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (6, '4321-BCD', 'Volkswagen', 'Golf 2.0 TDI', 'Blanco Puro', 28000, '2026-04-05', '2027-04-05', 'DISPONIBLE', 'PERMISO_B');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (7, '8765-FVK', 'Kawasaki', 'Z650', 'Verde Lime', 8500, '2025-10-18', '2026-10-18', 'INACTIVO', 'PERMISO_A2');

-- Vehículos adicionales en autoescuela sin profesor asignado
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (8, '2468-KLP', 'Peugeot', 'Tweet 125 Pro', 'Gris Grafito', 14500, '2026-03-01', '2027-03-01', 'DISPONIBLE', 'PERMISO_AM');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (9, '1357-NMB', 'Scania', 'Touring HD', 'Blanco Ártico', 195000, '2025-12-10', '2026-12-10', 'DISPONIBLE', 'PERMISO_D');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (10, '9753-RST', 'Schmitz', 'Cargobull Remolque', 'Azul Marino', 42000, '2026-02-20', '2027-02-20', 'DISPONIBLE', 'PERMISO_B_E');

-- Vehículo en mantenimiento con fecha de próxima revisión caducada (alerta de ITV/mantenimiento)
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (11, '8642-TUV', 'Citroën', 'C3 1.2 PureTech', 'Rojo Rubí', 78000, '2025-08-01', CURRENT_DATE - INTERVAL '15' DAY, 'MANTENIMIENTO', 'PERMISO_B');

-- ------------------------------------------------------------------------------
-- 2. USUARIOS BASE (persona)
-- ------------------------------------------------------------------------------
-- 2.1 Administrador (id = 1)
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (1, 'Antonio Abad', 'Hernández Gálvez', '12345678A', '1985-04-12', 'admin', 'admin@autoescuela.es', '$2a$10$umISk5UJjoOGrFmQXaaANernpH1OxhBA/NHx.TpkKBCNfau0s4uUi', '600111222', 'Calle Gran Vía 28, Madrid', 'ACTIVO');

-- 2.2 Profesores (id = 2, 3, 7, 8)
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (2, 'Laura', 'Sánchez Romero', '23456789B', '1988-09-23', 'profesor1', 'laura.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600222333', 'Avenida de América 15, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (3, 'Manuel', 'Navarro Torres', '34567890C', '1990-11-05', 'profesor2', 'manuel.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600333444', 'Calle Alcalá 120, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (7, 'Carlos', 'Martínez León', '78901234G', '1986-06-18', 'profesor3', 'carlos.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600777888', 'Calle Orense 34, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (8, 'Elena', 'Gómez Varela', '89012345H', '1992-02-14', 'profesor4', 'elena.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600888999', 'Calle O''Donnell 18, Madrid', 'ACTIVO');

-- Profesor 5: nuevo docente contratado, sin alumnos ni vehículo asignado inicialmente
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (12, 'Javier', 'Ortega Morales', '22334455M', '1987-11-30', 'profesor5', 'javier.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600555444', 'Calle Velázquez 60, Madrid', 'ACTIVO');

-- 2.3 Alumnos (26 alumnos en total: id = 4, 5, 6, 9, 10, 11, 13..32)
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (4, 'Jose', 'López Martínez', '45678901D', '2004-03-15', 'alumno1', 'jose.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600444555', 'Calle Princesa 42, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (5, 'David', 'Ruiz Gómez', '56789012E', '2003-07-28', 'alumno2', 'david.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600555666', 'Paseo de la Castellana 80, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (6, 'Sofía', 'Fernández Castro', '67890123F', '2005-12-02', 'alumno3', 'sofia.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600666777', 'Calle Bravo Murillo 55, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (9, 'Lucía', 'Morales Sanz', '90123456J', '2004-10-08', 'alumno4', 'lucia.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600999000', 'Calle Goya 102, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (10, 'Alejandro', 'Blanco Gil', '01234567K', '2003-01-22', 'alumno5', 'alejandro.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600000111', 'Avenida de Bruselas 40, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (11, 'Marta', 'Serrano Peña', '11223344L', '2005-05-19', 'alumno6', 'marta.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600111333', 'Calle Narváez 25, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (13, 'Carmen', 'Vega Delgado', '33445566N', '2004-05-14', 'alumno7', 'carmen.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123001', 'Calle Serrano 10, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (14, 'Pablo', 'Iglesias Rivas', '44556677P', '2003-09-20', 'alumno8', 'pablo.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123002', 'Calle Bravo Murillo 110, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (15, 'Sara', 'Cano Ramos', '55667788Q', '2005-02-11', 'alumno9', 'sara.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123003', 'Avenida de Burgos 4, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (16, 'Daniel', 'Marín Soler', '66778899R', '2002-11-03', 'alumno10', 'daniel.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123004', 'Calle Cea Bermúdez 22, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (17, 'Paula', 'Vidal Cruz', '77889900S', '2004-08-17', 'alumno11', 'paula.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123005', 'Calle Arturo Soria 85, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (18, 'Javier', 'Domínguez Peña', '88990011T', '2001-04-29', 'alumno12', 'javier.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123006', 'Calle Santa Engracia 50, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (19, 'Irene', 'Pacheco Campos', '99001122V', '2005-07-06', 'alumno13', 'irene.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123007', 'Calle López de Hoyos 90, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (20, 'Álvaro', 'Gil Santana', '10111213W', '2003-12-19', 'alumno14', 'alvaro.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123008', 'Calle Hilarión Eslava 15, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (21, 'Claudia', 'Benítez Lara', '21222324X', '2004-02-28', 'alumno15', 'claudia.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123009', 'Calle Alberto Aguilera 30, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (22, 'Marcos', 'Roldán Fuentes', '31323334Y', '2002-06-12', 'alumno16', 'marcos.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123010', 'Paseo de las Delicias 65, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (23, 'Nerea', 'Gallego Soto', '41424344Z', '2005-10-01', 'alumno17', 'nerea.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123011', 'Calle Clara del Rey 18, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (24, 'Gonzalo', 'Prieto Moya', '51525354A', '2000-08-25', 'alumno18', 'gonzalo.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123012', 'Avenida de la Albufera 120, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (25, 'Andrea', 'Cabrera Ortiz', '61626364B', '2003-04-18', 'alumno19', 'andrea.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123013', 'Calle Antonio López 45, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (26, 'Hugo', 'Medina León', '71727374C', '2004-09-09', 'alumno20', 'hugo.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123014', 'Calle Cartagena 70, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (27, 'Raquel', 'Calvo Herrero', '81828384D', '2001-03-31', 'alumno21', 'raquel.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123015', 'Calle Ibiza 35, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (28, 'Adrián', 'Pascual Sanz', '91929394E', '2005-11-23', 'alumno22', 'adrian.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123016', 'Calle Ferraz 40, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (29, 'Marina', 'Flores Rey', '02030405F', '2004-01-14', 'alumno23', 'marina.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123017', 'Calle Menéndez Pelayo 12, Madrid', 'ACTIVO');

-- Casuística: Alumno en estado INACTIVO (expediente suspendido temporalmente por el centro)
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (30, 'Roberto', 'Aguilar Durán', '12131415G', '2002-10-05', 'alumno24', 'roberto.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123018', 'Calle Embajadores 80, Madrid', 'INACTIVO');

-- Casuística: Alumnos recién registrados pendientes de asignación de profesor docente
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (31, 'Silvia', 'Montesinos Ruiz', '23242526H', '2004-06-16', 'alumno25', 'silvia.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123019', 'Calle Príncipe de Vergara 100, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (32, 'Carlos', 'Ibáñez Méndez', '34353637J', '2005-08-22', 'alumno26', 'carlos.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '600123020', 'Calle Ayala 45, Madrid', 'ACTIVO');

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

INSERT INTO profesor (id, fecha_contratacion, turno, vehiculo_id)
VALUES (7, '2021-09-01', 'MATINAL', 5);

INSERT INTO profesor (id, fecha_contratacion, turno, vehiculo_id)
VALUES (8, '2024-01-10', 'TARDE', 6);

-- Profesor 5: sin vehículo asignado (NULL)
INSERT INTO profesor (id, fecha_contratacion, turno, vehiculo_id)
VALUES (12, '2026-09-01', 'MATINAL', NULL);

-- 3.3 Permisos de Carnet autorizados por Profesor
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (2, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (2, 'PERMISO_A2');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (3, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (3, 'PERMISO_C');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (7, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (7, 'PERMISO_B_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (8, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (8, 'PERMISO_A2');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (12, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (12, 'PERMISO_D');

-- 3.4 Alumnos y asignación de Profesor (24 distribuidos en 4 profesores, 2 sin profesor, 0 para profesor 5)
-- Profesor 1 (Laura Sánchez - id: 2): 6 alumnos
INSERT INTO alumno (id, profesor_id) VALUES (4, 2);
INSERT INTO alumno (id, profesor_id) VALUES (5, 2);
INSERT INTO alumno (id, profesor_id) VALUES (9, 2);
INSERT INTO alumno (id, profesor_id) VALUES (13, 2);
INSERT INTO alumno (id, profesor_id) VALUES (14, 2);
INSERT INTO alumno (id, profesor_id) VALUES (15, 2);

-- Profesor 2 (Manuel Navarro - id: 3): 6 alumnos
INSERT INTO alumno (id, profesor_id) VALUES (6, 3);
INSERT INTO alumno (id, profesor_id) VALUES (16, 3);
INSERT INTO alumno (id, profesor_id) VALUES (17, 3);
INSERT INTO alumno (id, profesor_id) VALUES (18, 3);
INSERT INTO alumno (id, profesor_id) VALUES (19, 3);
INSERT INTO alumno (id, profesor_id) VALUES (20, 3);

-- Profesor 3 (Carlos Martínez - id: 7): 6 alumnos
INSERT INTO alumno (id, profesor_id) VALUES (10, 7);
INSERT INTO alumno (id, profesor_id) VALUES (21, 7);
INSERT INTO alumno (id, profesor_id) VALUES (22, 7);
INSERT INTO alumno (id, profesor_id) VALUES (23, 7);
INSERT INTO alumno (id, profesor_id) VALUES (24, 7);
INSERT INTO alumno (id, profesor_id) VALUES (25, 7);

-- Profesor 4 (Elena Gómez - id: 8): 6 alumnos
INSERT INTO alumno (id, profesor_id) VALUES (11, 8);
INSERT INTO alumno (id, profesor_id) VALUES (26, 8);
INSERT INTO alumno (id, profesor_id) VALUES (27, 8);
INSERT INTO alumno (id, profesor_id) VALUES (28, 8);
INSERT INTO alumno (id, profesor_id) VALUES (29, 8);
INSERT INTO alumno (id, profesor_id) VALUES (30, 8);

-- Casuística: Alumnos recién registrados sin profesor asignado
INSERT INTO alumno (id, profesor_id) VALUES (31, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (32, NULL);

-- ------------------------------------------------------------------------------
-- 4. INCIDENCIAS DE VEHÍCULOS (incidencia_vehiculo)
-- ------------------------------------------------------------------------------
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (1, '2026-08-20 09:30:00', 'Ruido anómalo en pastillas de freno delanteras al frenar en pendiente pronunciada.', 'PENDIENTE', 4, 3);

INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (2, '2026-08-10 16:15:00', 'Sustitución rutinaria de neumático delantero derecho por pinchazo en vía urbana.', 'RESUELTA', 1, 2);

INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (3, CURRENT_DATE - INTERVAL '2' DAY + TIME '08:45:00', 'Fallo en alternador: testigo de batería encendido y dificultad en el arranque en frío.', 'PENDIENTE', 5, 7);

INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (4, CURRENT_DATE - INTERVAL '5' DAY + TIME '11:20:00', 'Pérdida de presión en el circuito hidráulico de frenos. Vehículo inmovilizado en taller oficial.', 'EN_PROCESO', 5, 7);

INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (5, CURRENT_DATE - INTERVAL '1' DAY + TIME '15:10:00', 'Desgaste acusado y vibración anómala en tren delantero a más de 80 km/h.', 'EN_PROCESO', 2, 3);

INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (6, CURRENT_DATE - INTERVAL '7' DAY + TIME '17:00:00', 'Sustitución de escobillas limpiaparabrisas y lámpara halógena del faro izquierdo fundida.', 'RESUELTA', 6, 8);

-- Incidencia en vehículo en mantenimiento con ITV vencida (reportada por Profesor 3)
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (7, CURRENT_DATE - INTERVAL '15' DAY + TIME '09:00:00', 'ITV desfavorable por emisión de gases por encima de los límites. Pendiente de puesta a punto en taller.', 'PENDIENTE', 11, 7);

-- Incidencia en ciclomotor Peugeot Tweet (reportada por Profesor 1)
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (8, CURRENT_DATE - INTERVAL '3' DAY + TIME '18:30:00', 'Revisión y tensado de cadena en ciclomotor Peugeot Tweet tras prácticas de maniobras.', 'RESUELTA', 8, 2);

-- ------------------------------------------------------------------------------
-- 5. MATRÍCULAS ACADÉMICAS (matricula)
-- ------------------------------------------------------------------------------
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (1, TRUE, 'PERMISO_B', 2, 5, 0, 450.00, '2026-05-10', 'NUEVA', 'TEORICO_PRACTICA', 4);

INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (2, TRUE, 'PERMISO_B', 1, 0, 1, 320.00, '2026-04-01', 'RENOVACION', 'PRACTICA', 5);

INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (3, TRUE, 'PERMISO_A2', 2, 8, 0, 390.00, '2026-06-15', 'NUEVA', 'INDIVIDUAL', 6);

INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (4, TRUE, 'PERMISO_B', 2, 10, 0, 480.00, '2026-07-01', 'NUEVA', 'TEORICO_PRACTICA', 9);

INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (5, TRUE, 'PERMISO_B', 2, 3, 0, 420.00, '2026-06-20', 'NUEVA', 'PRACTICA', 10);

INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (6, TRUE, 'PERMISO_A2', 1, 6, 1, 350.00, '2026-05-25', 'RENOVACION', 'INDIVIDUAL', 11);

-- Matrículas adicionales cubriendo casuísticas no contempladas previamente
-- Alumno 13 (Profesor 1): Matrícula estándar activa
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (7, TRUE, 'PERMISO_B', 2, 12, 0, 450.00, '2026-08-01', 'NUEVA', 'TEORICO_PRACTICA', 13);

-- Alumno 14 (Profesor 1): Casuística CONVOCATORIAS AGOTADAS (0 restantes, 2 gastadas, saldo 0 - requiere renovación de matrícula)
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (8, TRUE, 'PERMISO_B', 0, 0, 2, 450.00, '2026-03-10', 'NUEVA', 'TEORICO_PRACTICA', 14);

-- Alumno 15 (Profesor 1): Casuística MATRÍCULA INACTIVA / EXPEDIENTE CADUCADO (> 1 año sin actividad en el centro)
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (9, FALSE, 'PERMISO_B', 2, 0, 0, 400.00, '2025-01-15', 'NUEVA', 'TEORICO_PRACTICA', 15);

-- Alumno 16 (Profesor 2): Casuística PERMISO C (Camión rígido profesional)
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (10, TRUE, 'PERMISO_C', 2, 15, 0, 850.00, '2026-07-15', 'NUEVA', 'INDIVIDUAL', 16);

-- Alumno 17 (Profesor 2): Matrícula B modalidad práctica
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (11, TRUE, 'PERMISO_B', 2, 4, 0, 420.00, '2026-08-10', 'NUEVA', 'PRACTICA', 17);

-- Alumno 18 (Profesor 2): Renovación con 1 convocatoria gastada
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (12, TRUE, 'PERMISO_B', 1, 1, 1, 350.00, '2026-06-01', 'RENOVACION', 'PRACTICA', 18);

-- Alumno 21 (Profesor 3): Casuística PERMISO B+E (Conjuntos de vehículos con remolque)
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (13, TRUE, 'PERMISO_B_E', 2, 6, 0, 500.00, '2026-08-20', 'NUEVA', 'INDIVIDUAL', 21);

-- Alumno 22 (Profesor 3): Pack intensivo con alto saldo de clases prácticas
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (14, TRUE, 'PERMISO_B', 2, 20, 0, 600.00, '2026-09-01', 'NUEVA', 'TEORICO_PRACTICA', 22);

-- Alumno 26 (Profesor 4): Casuística PERMISO AM (Ciclomotores)
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (15, TRUE, 'PERMISO_AM', 2, 8, 0, 320.00, '2026-09-05', 'NUEVA', 'TEORICO_PRACTICA', 26);

-- Alumno 27 (Profesor 4): Permiso A2 modalidad individual
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (16, TRUE, 'PERMISO_A2', 2, 5, 0, 420.00, '2026-07-20', 'NUEVA', 'INDIVIDUAL', 27);

-- Alumno 30 (Profesor 4 - Inactivo): Matrícula inactiva
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (17, FALSE, 'PERMISO_B', 2, 2, 0, 450.00, '2026-02-01', 'NUEVA', 'TEORICO_PRACTICA', 30);

-- Alumno 31 (Sin profesor asignado): Alta online recién matriculado con saldo 0
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (18, TRUE, 'PERMISO_B', 2, 0, 0, 450.00, '2026-09-25', 'NUEVA', 'TEORICO_PRACTICA', 31);

-- Alumno 15 (Profesor 1): Renovación activa tras tener el expediente previo inactivo ID 9 (Casuística: Múltiples matrículas por alumno)
INSERT INTO matricula (id, esta_activa, permiso_carnet, convocatorias, saldo_clases, convocatorias_gastadas, precio, fecha_matriculacion, tipo, modalidad, alumno_id)
VALUES (19, TRUE, 'PERMISO_B', 2, 8, 0, 390.00, '2026-09-10', 'RENOVACION', 'PRACTICA', 15);

-- ------------------------------------------------------------------------------
-- 6. CLASES PRÁCTICAS (clase_practica)
-- ------------------------------------------------------------------------------
-- 6.1 Clases asignadas a Profesor 1 (Laura Sánchez - Matinal)
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (1, CURRENT_DATE - INTERVAL '10' DAY + TIME '10:00:00', 45, 'Calle Alcalá 45 (Metro Goya)', 44955, 45000, 'Excelente dominio del embrague y correcta circulación en glorietas.', 'RECIBIDA', 4, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (2, CURRENT_DATE - INTERVAL '2' DAY + TIME '11:00:00', 45, 'Calle Princesa 42', 45000, 45000, 'Cancelada por el alumno con antelación por indisposición médica.', 'CANCELADA', 4, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (3, CURRENT_DATE - INTERVAL '1' DAY + TIME '17:00:00', 45, 'Estación de Atocha', 45000, 45040, 'Práctica en vía rápida e incorporaciones a M-30 completadas satisfactoriamente.', 'RECIBIDA', 4, 2);

-- Clases de hoy para Profesor 1 (una ya recibida por la mañana y dos pendientes para mostrar en dashboard y calendario)
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (4, CURRENT_DATE + TIME '09:00:00', 45, 'Plaza de Castilla', 45040, 45080, 'Circulación urbana fluida, giros a la izquierda y prioridad de paso.', 'RECIBIDA', 9, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (5, CURRENT_DATE + TIME '11:30:00', 45, 'Calle Alcalá 45 (Metro Goya)', 45080, 45080, 'Práctica programada: estacionamiento en línea y batería.', 'PENDIENTE', 9, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (6, CURRENT_DATE + TIME '16:30:00', 45, 'Avenida de América 15', 45080, 45080, 'Maniobras y conducción nocturna en vías con tráfico denso.', 'PENDIENTE', 4, 2);

-- Clases programadas en días posteriores para Profesor 1
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (7, CURRENT_DATE + INTERVAL '1' DAY + TIME '10:00:00', 45, 'Calle Gran Vía 28', 45080, 45080, 'Recorrido oficial de examen DGT zona centro.', 'PENDIENTE', 9, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (8, CURRENT_DATE + INTERVAL '2' DAY + TIME '12:00:00', 45, 'Calle Alcalá 45 (Metro Goya)', 45080, 45080, 'Simulacro de examen práctico con faltas leves y deficientes.', 'PENDIENTE', 4, 2);

-- 6.2 Clases asignadas a Profesor 2 (Manuel Navarro - Tarde)
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (9, CURRENT_DATE - INTERVAL '3' DAY + TIME '16:00:00', 60, 'Pista de prácticas Norte', 31900, 31950, 'Maniobras en circuito cerrado superadas con fluidez (zig-zag y frenada).', 'RECIBIDA', 6, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (10, CURRENT_DATE + TIME '16:00:00', 45, 'Estación de Chamartín', 32000, 32000, 'Conducción eficiente y uso de marchas largas en vía periurbana.', 'PENDIENTE', 5, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (11, CURRENT_DATE + TIME '17:30:00', 45, 'Paseo de la Castellana 80', 32000, 32000, 'Circulación por túneles y glorietas partidas.', 'PENDIENTE', 6, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (12, CURRENT_DATE + INTERVAL '1' DAY + TIME '16:00:00', 45, 'Estación de Chamartín', 32000, 32000, 'Práctica intensiva de adelantamientos en autovía.', 'PENDIENTE', 5, 3);

-- 6.3 Clases asignadas a Profesor 3 (Carlos Martínez - Matinal)
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (13, CURRENT_DATE + TIME '10:00:00', 45, 'Avenida de Bruselas 40', 62000, 62035, 'Comprobaciones previas del vehículo y puesta en marcha.', 'RECIBIDA', 10, 7);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (14, CURRENT_DATE + INTERVAL '1' DAY + TIME '11:00:00', 45, 'Avenida de Bruselas 40', 62035, 62035, 'Práctica de cambios de sentido y detención en rampa.', 'PENDIENTE', 10, 7);

-- 6.4 Clases asignadas a Profesor 4 (Elena Gómez - Tarde)
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (15, CURRENT_DATE - INTERVAL '1' DAY + TIME '17:30:00', 45, 'Pista de Motocicletas Sur', 8450, 8500, 'Equilibrio a baja velocidad y esquiva de obstáculos.', 'RECIBIDA', 11, 8);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (16, CURRENT_DATE + TIME '18:00:00', 45, 'Calle Narváez 25', 8500, 8500, 'Circulación en tráfico abierto con motocicleta de 650cc.', 'PENDIENTE', 11, 8);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (17, CURRENT_DATE + INTERVAL '1' DAY + TIME '17:00:00', 45, 'Calle Narváez 25', 8500, 8500, 'Trazado de curvas e inclinación progresiva.', 'PENDIENTE', 11, 8);

-- 6.5 Clases adicionales y nuevas casuísticas de formación
-- Práctica matinal estándar para nueva alumna (Profesor 1)
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (18, CURRENT_DATE + INTERVAL '1' DAY + TIME '09:00:00', 45, 'Calle Serrano 10', 45080, 45080, 'Iniciación al manejo de marchas y giros en vía urbana.', 'PENDIENTE', 13, 2);

-- Casuística: Práctica de 90 min de Camión rígido en centro logístico (Profesor 2)
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (19, CURRENT_DATE - INTERVAL '1' DAY + TIME '15:30:00', 90, 'Centro Logístico Coslada', 180000, 180060, 'Práctica de maniobras de aproximación a muelle de carga con camión rígido.', 'RECIBIDA', 16, 3);

-- Práctica de tarde en hora punta (Profesor 2)
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (20, CURRENT_DATE + TIME '19:00:00', 45, 'Calle Arturo Soria 85', 32000, 32000, 'Conducción en hora punta y circulación fluida.', 'PENDIENTE', 17, 3);

-- Casuística: Práctica de 60 min de Remolque B+E con maniobra de aproximación y acople (Profesor 3)
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (21, CURRENT_DATE + TIME '11:30:00', 60, 'Pista de maniobras Sur', 62035, 62035, 'Maniobra de marcha atrás en curva y estacionamiento de remolque.', 'PENDIENTE', 21, 7);

-- Casuística: Práctica CANCELADA con antelación por el alumno (Profesor 3)
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (22, CURRENT_DATE - INTERVAL '2' DAY + TIME '12:00:00', 45, 'Paseo de las Delicias 65', 62035, 62035, 'Cancelada por el alumno debido a coincidencia de examen universitario.', 'CANCELADA', 22, 7);

-- Casuística: Práctica de Ciclomotor AM en pista de maniobras (Profesor 4)
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (23, CURRENT_DATE + TIME '16:00:00', 45, 'Pista de Maniobras Ciclomotores', 14500, 14500, 'Zig-zag entre jalones y frenada de emergencia sobre superficie deslizante.', 'PENDIENTE', 26, 8);

-- Práctica de 60 min en motocicleta A2 (Profesor 4)
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (24, CURRENT_DATE + INTERVAL '2' DAY + TIME '18:30:00', 60, 'Calle Ibiza 35', 8500, 8500, 'Circulación urbana en tráfico real con motocicleta.', 'PENDIENTE', 27, 8);

-- ------------------------------------------------------------------------------
-- 7. SOLICITUDES DE EXAMEN (solicitud_examen)
-- ------------------------------------------------------------------------------
INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (1, 'ACEPTADA', 'Alumno preparado tras superar 30 test de examen en plataforma con menos de 2 fallos.', 4, 2, 1);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (2, 'PENDIENTE', 'Solicitud para la próxima convocatoria práctica de la DGT en Móstoles.', 5, 2, 2);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (3, 'RECHAZADA', 'Se aconseja realizar al menos 2 prácticas más para afianzar incorporaciones a autovía.', 6, 3, 3);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (4, 'PENDIENTE', 'Alumno con 15 prácticas completadas y destreza acreditada en estacionamiento y vías rápidas.', 9, 2, 4);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (5, 'ACEPTADA', 'Convocatoria teórica DGT superada. Solicitud tramitada para circuito cerrado A2.', 11, 8, 6);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (6, 'RECHAZADA', 'Falta afianzar la salida en rampa sin retroceso del vehículo antes de ir a examen.', 10, 7, 5);

-- Casuística: Solicitud RECHAZADA por agotar convocatorias iniciales
INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (7, 'RECHAZADA', 'Alumno ha agotado convocatorias de su matrícula inicial. Debe tramitar previamente la renovación del expediente ante la DGT.', 14, 2, 8);

-- Solicitud ACEPTADA para examen de Camión C
INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (8, 'ACEPTADA', 'Superado simulacro de maniobras en circuito cerrado para camión C con cero faltas.', 16, 3, 10);

-- Solicitud PENDIENTE para examen de destreza de Ciclomotor AM
INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (9, 'PENDIENTE', 'Alumno preparado para la prueba de destreza en circuito cerrado AM.', 26, 8, 15);

-- ------------------------------------------------------------------------------
-- 8. EXÁMENES DGT (examen)
-- ------------------------------------------------------------------------------
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (1, TRUE, CURRENT_DATE - INTERVAL '60' DAY + TIME '09:00:00', 30, 'TEORICO', 4, 1);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (2, FALSE, CURRENT_DATE - INTERVAL '40' DAY + TIME '10:30:00', 25, 'PRACTICO', 5, NULL);

-- Examen práctico pendiente de calificar para alumno de Profesor 1 (habilita contador de pendientes en dashboard)
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (3, NULL, CURRENT_DATE + TIME '12:00:00', 35, 'PRACTICO', 4, NULL);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (4, TRUE, CURRENT_DATE - INTERVAL '15' DAY + TIME '09:00:00', 30, 'TEORICO', 9, 4);

-- Examen práctico pendiente de calificar para alumno de Profesor 3
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (5, NULL, CURRENT_DATE + TIME '13:00:00', 40, 'PRACTICO', 10, NULL);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (6, FALSE, CURRENT_DATE - INTERVAL '20' DAY + TIME '11:00:00', 20, 'PRACTICO', 11, NULL);

-- Examen de camión C aceptado y programado con resultado pendiente de calificar
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (7, NULL, CURRENT_DATE + INTERVAL '3' DAY + TIME '08:30:00', 45, 'PRACTICO', 16, 8);

-- Examen previo no apto que provocó el agotamiento de la 2ª convocatoria del alumno 14
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (8, FALSE, CURRENT_DATE - INTERVAL '30' DAY + TIME '11:00:00', 25, 'PRACTICO', 14, NULL);

-- ------------------------------------------------------------------------------
-- 9. REAJUSTE DE SECUENCIAS DE IDENTIDAD (H2)
-- ------------------------------------------------------------------------------
-- Permite que las inserciones posteriores desde los formularios de la aplicación
-- no colisionen con los IDs insertados en este script semilla.
ALTER TABLE vehiculo ALTER COLUMN id RESTART WITH 300;
ALTER TABLE persona ALTER COLUMN id RESTART WITH 300;
ALTER TABLE incidencia_vehiculo ALTER COLUMN id RESTART WITH 300;
ALTER TABLE matricula ALTER COLUMN id RESTART WITH 300;
ALTER TABLE clase_practica ALTER COLUMN id RESTART WITH 300;
ALTER TABLE solicitud_examen ALTER COLUMN id RESTART WITH 300;
ALTER TABLE examen ALTER COLUMN id RESTART WITH 300;
