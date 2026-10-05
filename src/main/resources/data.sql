-- ==============================================================================
-- SCRIPT DE DATOS SEMILLA PARA EL SISTEMA ERP DE AUTOESCUELAS (H2 / DESARROLLO)
-- ==============================================================================
-- Este script puebla todas las entidades del dominio modelando el flujo operativo
-- real de una autoescuela oficial española (DGT), garantizando integridad referencial,
-- consistencia matemática en saldos y odómetros, y cumplimiento estricto de las reglas
-- de negocio establecidas en AGENTS.md.
--
-- Ordenación estricta de identidades (persona):
--   * Administrador (id = 1)
--   * Profesores    (id = 2 .. 6)
--   * Alumnos       (id = 7 .. 32)
--
-- Agrupación correlativa de flota por tipo de carnet (vehiculo):
--   * Permiso B   : id = 1 .. 7
--   * Permiso A2  : id = 8 .. 10
--   * Permiso C   : id = 11 .. 13
--   * Permiso D   : id = 14 .. 15
--   * Permiso B+E : id = 16 .. 17
--   * Permiso AM  : id = 18 .. 19
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. FLOTA DE VEHÍCULOS (vehiculo)
-- ------------------------------------------------------------------------------
-- Permiso B (Turismos): id = 1 .. 7
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (1, '1234-LMN', 'SEAT', 'Ibiza 1.0 TSI', 'Blanco Nevada', 45080, '2026-01-10', '2027-01-10', 'OCUPADO', 'PERMISO_B');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (2, '5678-KPR', 'Renault', 'Clio E-Tech', 'Azul Rayo', 32050, '2025-11-20', '2026-11-20', 'OCUPADO', 'PERMISO_B');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (3, '7890-XYZ', 'Peugeot', '208 PureTech', 'Gris Platino', 62050, '2026-03-12', '2027-03-12', 'OCUPADO', 'PERMISO_B');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (4, '4321-BCD', 'Volkswagen', 'Golf 2.0 TDI', 'Blanco Puro', 28000, '2026-04-05', '2027-04-05', 'OCUPADO', 'PERMISO_B');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (5, '5522-KZX', 'Ford', 'Focus 1.5 EcoBoost', 'Blanco Glaciar', 15200, '2026-02-10', '2027-02-10', 'DISPONIBLE', 'PERMISO_B');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (6, '9966-PBR', 'Toyota', 'Yaris Hybrid', 'Plata Metalizado', 18400, '2026-01-25', '2027-01-25', 'DISPONIBLE', 'PERMISO_B');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (7, '8642-TUV', 'Citroën', 'C3 1.2 PureTech', 'Rojo Rubí', 78000, '2025-08-01', CURRENT_DATE - INTERVAL '15' DAY, 'MANTENIMIENTO', 'PERMISO_B');

-- Permiso A2 (Motocicletas): id = 8 .. 10
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (8, '9012-HJK', 'Yamaha', 'MT-07', 'Negro Midnight', 12040, '2026-02-15', '2027-02-15', 'DISPONIBLE', 'PERMISO_A2');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (9, '6633-MTR', 'Honda', 'CB500F', 'Rojo Victory', 9300, '2026-03-20', '2027-03-20', 'DISPONIBLE', 'PERMISO_A2');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (10, '8765-FVK', 'Kawasaki', 'Z650', 'Verde Lime', 8500, '2025-10-18', '2026-10-18', 'INACTIVO', 'PERMISO_A2');

-- Permiso C (Camiones pesados): id = 11 .. 13
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (11, '3456-FGH', 'Mercedes-Benz', 'Actros 1845', 'Rojo Carmesí', 180100, '2025-09-05', '2026-09-05', 'MANTENIMIENTO', 'PERMISO_C');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (12, '7744-LPS', 'MAN', 'TGX 18.480', 'Blanco Puro', 142000, '2026-01-15', '2027-01-15', 'DISPONIBLE', 'PERMISO_C');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (13, '1928-KLM', 'Volvo', 'FH 500', 'Azul Océano', 165000, '2026-02-28', '2027-02-28', 'DISPONIBLE', 'PERMISO_C');

-- Permiso D (Autobuses): id = 14 .. 15
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (14, '1357-NMB', 'Scania', 'Touring HD', 'Blanco Ártico', 195000, '2025-12-10', '2026-12-10', 'DISPONIBLE', 'PERMISO_D');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (15, '8855-NWD', 'Iveco', 'Crossway Line', 'Azul Real', 134000, '2026-03-05', '2027-03-05', 'DISPONIBLE', 'PERMISO_D');

-- Permiso B+E (Remolques pesados): id = 16 .. 17
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (16, '9753-RST', 'Schmitz', 'Cargobull Remolque', 'Azul Marino', 42000, '2026-02-20', '2027-02-20', 'DISPONIBLE', 'PERMISO_B_E');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (17, '4582-WXZ', 'Leciñena', 'Remolque B+E', 'Gris Metalizado', 28000, '2026-01-30', '2027-01-30', 'DISPONIBLE', 'PERMISO_B_E');

-- Permiso AM (Ciclomotores): id = 18 .. 19
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (18, '2468-KLP', 'Peugeot', 'Tweet 125 Pro', 'Gris Grafito', 14500, '2026-03-01', '2027-03-01', 'DISPONIBLE', 'PERMISO_AM');

INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, fecha_ultima_revision, fecha_proxima_revision, estado, tipo)
VALUES (19, '1177-QDF', 'Kymco', 'Agility City 50', 'Negro Cosmos', 7200, '2026-02-18', '2027-02-18', 'DISPONIBLE', 'PERMISO_AM');

-- ------------------------------------------------------------------------------
-- 2. USUARIOS BASE (persona)
-- ------------------------------------------------------------------------------
-- 2.1 Administrador (id = 1)
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (1, 'Antonio Abad', 'Hernández Gálvez', '12345678A', '1985-04-12', 'admin', 'admin@autoescuela.es', '$2a$10$umISk5UJjoOGrFmQXaaANernpH1OxhBA/NHx.TpkKBCNfau0s4uUi', '600111222', 'Calle Gran Vía 28, Madrid', 'ACTIVO');

-- 2.2 Profesores (id = 2 .. 6)
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (2, 'Laura', 'Sánchez Romero', '23456789B', '1988-09-23', 'profesor1', 'laura.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600222333', 'Avenida de América 15, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (3, 'Manuel', 'Navarro Torres', '34567890C', '1990-11-05', 'profesor2', 'manuel.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600333444', 'Calle Alcalá 120, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (4, 'Carlos', 'Martínez León', '78901234G', '1982-07-14', 'profesor3', 'carlos.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600444555', 'Paseo de la Castellana 200, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (5, 'Elena', 'Gómez Varela', '89012345H', '1992-03-29', 'profesor4', 'elena.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600555666', 'Calle Princesa 45, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (6, 'Javier', 'Blanco Delgado', '21098765N', '1980-05-18', 'profesor5', 'javier.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600666777', 'Avenida de Córdoba 12, Madrid', 'INACTIVO');

-- 2.3 Alumnos (id = 7 .. 32)
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (7, 'Jose', 'López Martínez', '45678901D', '2004-03-15', 'alumno1', 'jose.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '611222333', 'Calle Toledo 88, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (8, 'David', 'Ruiz Gómez', '56789012E', '2003-08-22', 'alumno2', 'david.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '622333444', 'Calle Atocha 14, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (9, 'Carmen', 'Vega Delgado', '67890123F', '2005-01-10', 'alumno3', 'carmen.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '633444555', 'Calle Mayor 5, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (10, 'Alejandro', 'Blanco Gil', '90123456I', '2002-12-05', 'alumno4', 'alejandro.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '644555666', 'Calle Bravo Murillo 30, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (11, 'Lucía', 'Fernández Ramos', '01234567J', '2004-06-18', 'alumno5', 'lucia.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '655666777', 'Avenida Albufera 102, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (12, 'Hugo', 'Medina León', '12345670K', '2001-10-30', 'alumno6', 'hugo.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '666777888', 'Calle O Donnell 40, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (13, 'Daniel', 'Marín Soler', '32109876M', '2003-04-25', 'alumno7', 'daniel.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '677888999', 'Calle Serrano 90, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (14, 'Pablo', 'Iglesias Gil', '10987654O', '1998-09-14', 'alumno8', 'pablo.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '688999000', 'Calle Velázquez 60, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (15, 'Marta', 'Sánchez Romero', '09876543P', '2000-02-11', 'alumno9', 'marta.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '699000111', 'Calle Goya 33, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (16, 'Raúl', 'Castillo Morales', '98765432Q', '1995-11-20', 'alumno10', 'raul.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '612345678', 'Avenida Bruselas 18, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (17, 'Adrián', 'Pascual Sanz', '87654321R', '2004-07-08', 'alumno11', 'adrian.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '623456789', 'Calle Ferraz 52, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (18, 'Mario', 'Torres Gil', '76543210S', '1997-03-17', 'alumno12', 'mario.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '634567890', 'Calle Bailén 12, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (19, 'Claudia', 'Benítez Lara', '65432109T', '2003-11-03', 'alumno13', 'claudia.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '645678901', 'Calle Sagasta 21, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (20, 'Andrea', 'Cabrera Ortiz', '54321098U', '2005-05-14', 'alumno14', 'andrea.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '656789012', 'Calle Santa Engracia 70, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (21, 'Carlos', 'Ibáñez Méndez', '43210987V', '2002-08-19', 'alumno15', 'carlos.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '667890123', 'Calle Guzmán el Bueno 44, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (22, 'Patricia', 'Navarro Ruiz', '32109870W', '2004-01-28', 'alumno16', 'patricia.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '678901234', 'Calle Alberto Aguilera 19, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (23, 'Gonzalo', 'Prieto Moya', '21098701X', '2003-09-07', 'alumno17', 'gonzalo.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '689012345', 'Paseo de las Delicias 35, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (24, 'Nuria', 'Gil Vargas', '10987012Y', '2005-04-02', 'alumno18', 'nuria.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '690123456', 'Calle Embajadores 80, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (25, 'Irene', 'Pacheco Campos', '09870123Z', '2002-06-30', 'alumno19', 'irene.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '601234567', 'Calle Bravo Murillo 115, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (26, 'Fernando', 'Cano Ortiz', '98701234A', '2004-10-12', 'alumno20', 'fernando.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '612345670', 'Calle Arturo Soria 150, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (27, 'Jorge', 'Romero Gil', '87012345B', '2001-08-24', 'alumno21', 'jorge.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '623456701', 'Calle Silvano 22, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (28, 'Sergio', 'Serrano Ortiz', '70123456C', '2003-02-16', 'alumno22', 'sergio.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '634567012', 'Avenida San Luis 88, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (29, 'Celia', 'Morales Sanz', '01234568D', '2004-11-27', 'alumno23', 'celia.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '645670123', 'Calle Costa Rica 10, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (30, 'Roberto', 'Aguilar Díaz', '12345689E', '1999-07-03', 'alumno24', 'roberto.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '656701234', 'Calle Colombia 4, Madrid', 'INACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (31, 'Silvia', 'Montesinos', '23456890F', '2005-09-09', 'alumno25', 'silvia.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '667012345', 'Calle Honduras 15, Madrid', 'ACTIVO');

INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (32, 'Marcos', 'Delgado Peña', '34568901G', '2006-03-21', 'alumno26', 'marcos.alumno@autoescuela.es', '$2a$10$/xpN9C29wP5117wEJm/3UOxAfvgSMyV78Zm2B48o27KEZ4FMnc5Ma', '678123456', 'Calle Monforte de Lemos 33, Madrid', 'ACTIVO');

-- ------------------------------------------------------------------------------
-- 3. ESPECIALIZACIONES DE USUARIO (JOINED)
-- ------------------------------------------------------------------------------
-- 3.1 Administrador (id = 1)
INSERT INTO administrador (id) VALUES (1);

-- 3.2 Profesores (id = 2 .. 6)
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (2, 'MATINAL', '2022-01-15', 1);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (3, 'TARDE',   '2022-03-01', 2);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (4, 'MATINAL', '2022-06-10', 3);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (5, 'TARDE',   '2022-09-15', 4);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (6, 'TARDE',   '2023-01-20', NULL);

-- Permisos asignados a Profesores (profesor_permisos)
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (2, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (2, 'PERMISO_A2');

INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (3, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (3, 'PERMISO_C');

INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (4, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (4, 'PERMISO_B_E');

INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (5, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (5, 'PERMISO_A2');

INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (6, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (6, 'PERMISO_D');

-- 3.3 Alumnos (id = 7 .. 32)
-- Vinculación estricta: un alumno SOLO tiene profesor si dispone de matrícula activa compatible.
INSERT INTO alumno (id, profesor_id) VALUES (7, 2);    -- Jose López (Permiso B, Laura)
INSERT INTO alumno (id, profesor_id) VALUES (8, 2);    -- David Ruiz (Permiso B, Laura)
INSERT INTO alumno (id, profesor_id) VALUES (9, 2);    -- Carmen Vega (Permiso B, Laura)
INSERT INTO alumno (id, profesor_id) VALUES (10, 2);   -- Alejandro Blanco (Permiso B, Laura)
INSERT INTO alumno (id, profesor_id) VALUES (11, 2);   -- Lucía Fernández (Permiso B, Laura)
INSERT INTO alumno (id, profesor_id) VALUES (12, 5);   -- Hugo Medina (Permiso A2, Elena)
INSERT INTO alumno (id, profesor_id) VALUES (13, 2);   -- Daniel Marín (Permiso B, Laura)
INSERT INTO alumno (id, profesor_id) VALUES (14, 3);   -- Pablo Iglesias (Permiso B, conv agotadas, Manuel)
INSERT INTO alumno (id, profesor_id) VALUES (15, NULL);-- Marta Sánchez (Permiso obtenido, matrícula finalizada)
INSERT INTO alumno (id, profesor_id) VALUES (16, 3);   -- Raúl Castillo (Permiso C, Manuel)
INSERT INTO alumno (id, profesor_id) VALUES (17, 5);   -- Adrián Pascual (Permiso A2, Elena)
INSERT INTO alumno (id, profesor_id) VALUES (18, 3);   -- Mario Torres (Permiso C, Manuel)
INSERT INTO alumno (id, profesor_id) VALUES (19, 4);   -- Claudia Benítez (Permiso B_E, Carlos)
INSERT INTO alumno (id, profesor_id) VALUES (20, 4);   -- Andrea Cabrera (Permiso B, Carlos)
INSERT INTO alumno (id, profesor_id) VALUES (21, 4);   -- Carlos Ibáñez (Permiso B, Carlos)
INSERT INTO alumno (id, profesor_id) VALUES (22, 4);   -- Patricia Navarro (Permiso B, Carlos)
INSERT INTO alumno (id, profesor_id) VALUES (23, 4);   -- Gonzalo Prieto (Permiso B, Carlos)
INSERT INTO alumno (id, profesor_id) VALUES (24, 5);   -- Nuria Gil (Permiso B, Elena)
INSERT INTO alumno (id, profesor_id) VALUES (25, 3);   -- Irene Pacheco (Permiso B, Manuel)
INSERT INTO alumno (id, profesor_id) VALUES (26, 3);   -- Fernando Cano (Permiso B, Manuel)
INSERT INTO alumno (id, profesor_id) VALUES (27, 4);   -- Jorge Romero (Permiso B_E, Carlos)
INSERT INTO alumno (id, profesor_id) VALUES (28, NULL);-- Sergio Serrano (Sin matrícula activa actual)
INSERT INTO alumno (id, profesor_id) VALUES (29, 5);   -- Celia Morales (Permiso B, Elena)
INSERT INTO alumno (id, profesor_id) VALUES (30, NULL);-- Roberto Aguilar (Baja lógica INACTIVO)
INSERT INTO alumno (id, profesor_id) VALUES (31, NULL);-- Silvia Montesinos (Activa, pendiente de asignar profesor)
INSERT INTO alumno (id, profesor_id) VALUES (32, NULL);-- Marcos Delgado (Activo, pendiente de asignar profesor)

-- ------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------
-- 4. MATRÍCULAS (matricula) - 27 Matrículas (id = 1 .. 27)
-- ------------------------------------------------------------------------------
-- Tarifas: 250 € estándar (B, A2, B_E, AM) | 450 € pesados (C, D)
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (1, 7, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-07-01', 3, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (2, 8, 'PERMISO_B', 'PRACTICA', 'NUEVA', '2026-07-15', 5, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (3, 9, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-07-20', 5, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (4, 10, 'PERMISO_B', 'PRACTICA', 'NUEVA', '2026-08-01', 4, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (5, 11, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-07-20', 4, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (6, 12, 'PERMISO_A2', 'TEORICO_PRACTICA', 'NUEVA', '2026-08-10', 5, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (7, 13, 'PERMISO_B', 'TEORICO_PRACTICA', 'RENOVACION', '2026-06-15', 6, 1, 3, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (8, 14, 'PERMISO_B', 'PRACTICA', 'RENOVACION', '2026-05-15', 0, 0, 4, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (9, 15, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-05-10', 0, 1, 1, 250.00, FALSE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (10, 16, 'PERMISO_C', 'PRACTICA', 'NUEVA', '2026-07-15', 2, 2, 0, 450.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (11, 17, 'PERMISO_A2', 'TEORICO_PRACTICA', 'NUEVA', '2026-07-01', 2, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (12, 18, 'PERMISO_C', 'PRACTICA', 'NUEVA', '2026-08-20', 4, 2, 0, 450.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (13, 19, 'PERMISO_B_E', 'PRACTICA', 'NUEVA', '2026-07-15', 2, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (14, 20, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-07-01', 2, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (15, 21, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-08-01', 4, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (16, 22, 'PERMISO_B', 'TEORICO_PRACTICA', 'RENOVACION', '2026-04-10', 0, 0, 4, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (17, 23, 'PERMISO_B', 'PRACTICA', 'NUEVA', '2026-08-20', 4, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (18, 24, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-10', 0, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (19, 25, 'PERMISO_B', 'PRACTICA', 'NUEVA', '2026-08-15', 5, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (20, 26, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-07-20', 5, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (21, 27, 'PERMISO_B_E', 'PRACTICA', 'NUEVA', '2026-09-18', 4, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (22, 28, 'PERMISO_B', 'PRACTICA', 'NUEVA', '2026-05-10', 0, 1, 1, 250.00, FALSE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (23, 29, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-08-01', 4, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (24, 30, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-06-20', 0, 2, 0, 250.00, FALSE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (25, 31, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-10-01', 10, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (26, 32, 'PERMISO_AM', 'TEORICO_PRACTICA', 'NUEVA', '2026-10-02', 8, 2, 0, 250.00, TRUE);

INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (27, 27, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-05-15', 0, 2, 0, 250.00, FALSE);

-- ------------------------------------------------------------------------------
-- 5. INCIDENCIAS MECÁNICAS (incidencia_vehiculo) - 8 Incidencias
-- ------------------------------------------------------------------------------
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (1, '2026-09-02 08:30:00', 'Pérdida de presión en circuito neumático de frenos en eje tractor.', 'EN_PROCESO', 11, 3);

INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (2, '2026-09-28 14:00:00', 'Desgaste severo en pastillas y discos delanteros tras revisión preventiva.', 'PENDIENTE', 7, 4);

INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (3, '2026-09-10 11:15:00', 'Sustitución de bombilla halógena de luz diurna delantera derecha.', 'RESUELTA', 1, 2);

INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (4, '2026-09-12 16:45:00', 'Holgura leve en rótula de dirección izquierda detectada durante clase práctica.', 'RESUELTA', 2, 3);

INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (5, '2026-09-15 17:30:00', 'Tensión y engrase de cadena de transmisión tras jornada en pista.', 'RESUELTA', 8, 5);

INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (6, '2026-09-18 10:20:00', 'Calibración de sensor de presión de neumáticos y equilibrado de ruedas.', 'RESUELTA', 3, 4);

INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (7, '2026-09-22 18:00:00', 'Recarga de gas refrigerante del sistema de climatización.', 'RESUELTA', 4, 5);

INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (8, '2025-10-18 12:00:00', 'Deformación estructural de chasis tras caída lateral en circuito cerrado.', 'RESUELTA', 10, 5);
-- ------------------------------------------------------------------------------
-- 6. CLASES PRÁCTICAS (clase_practica) - 243 Clases
-- ------------------------------------------------------------------------------
-- Horarios de turno respetados:
--   * MATINAL (08:00 - 15:00) : Profesora Laura (id = 2) y Profesor Carlos (id = 4)
--   * TARDE   (15:00 - 22:00) : Profesor Manuel (id = 3) y Profesora Elena (id = 5)
-- Odómetros monótonos y consistentes.
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (1, '2026-06-10 08:30:00', 45, 'Calle Goya 33', 41500, 41539, 'Práctica inicial de circulación urbana: manejo de embrague y frenada suave.', 'RECIBIDA', 15, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (2, '2026-06-17 08:30:00', 45, 'Calle Goya 33', 41539, 41567, 'Circulación en glorietas y giros a la izquierda.', 'RECIBIDA', 15, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (3, '2026-06-24 10:00:00', 45, 'Calle Goya 33', 41567, 41600, 'Incorporación a vías de alta capacidad M-30.', 'RECIBIDA', 15, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (4, '2026-07-01 10:00:00', 45, 'Calle Goya 33', 41600, 41636, 'Estacionamiento en línea en calzada inclinada.', 'RECIBIDA', 15, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (5, '2026-07-08 08:30:00', 45, 'Calle Goya 33', 41636, 41674, 'Conducción en vías urbanas con pasos peatonales densos.', 'RECIBIDA', 15, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (6, '2026-07-15 08:30:00', 45, 'Calle Goya 33', 41674, 41712, 'Maniobras de cambio de sentido y parada de emergencia.', 'RECIBIDA', 15, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (7, '2026-07-22 10:00:00', 45, 'Calle Goya 33', 41712, 41743, 'Circulación en túneles urbanos y regulación semafórica.', 'RECIBIDA', 15, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (8, '2026-07-29 10:00:00', 45, 'Calle Goya 33', 41743, 41779, 'Conducción eficiente, anticipación y distancia de seguridad.', 'RECIBIDA', 15, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (9, '2026-08-05 08:30:00', 45, 'Calle Goya 33', 41779, 41814, 'Consolidación de maniobras y examen previo.', 'RECIBIDA', 15, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (10, '2026-08-18 10:00:00', 45, 'Calle Goya 33', 41814, 41843, 'Circulación urbana y glorietas previa a la primera convocatoria.', 'RECIBIDA', 15, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (11, '2026-08-28 08:30:00', 45, 'Calle Goya 33', 41843, 41876, 'Refuerzo tras primer suspenso: atención a ángulos muertos y señalización.', 'RECIBIDA', 15, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (12, '2026-09-10 10:00:00', 45, 'Calle Goya 33', 41876, 41915, 'Última clase de repaso previa al examen práctico definitivo.', 'RECIBIDA', 15, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (13, '2026-07-18 11:30:00', 45, 'Calle Alcalá 45', 41915, 41948, 'Iniciación y control de mandos primarios.', 'RECIBIDA', 8, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (14, '2026-07-25 11:30:00', 45, 'Calle Alcalá 45', 41948, 41983, 'Manejo de caja de cambios y reducción en aproximaciones.', 'RECIBIDA', 8, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (15, '2026-08-01 11:30:00', 45, 'Calle Alcalá 45', 41983, 42020, 'Control de velocidad e incorporación a autovía A-2.', 'RECIBIDA', 8, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (16, '2026-08-08 11:30:00', 45, 'Calle Alcalá 45', 42020, 42054, 'Trazado de curvas interurbanas y distancia de seguridad.', 'RECIBIDA', 8, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (17, '2026-08-15 11:30:00', 45, 'Calle Alcalá 45', 42054, 42084, 'Circulación en glorietas partidas.', 'RECIBIDA', 8, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (18, '2026-08-22 11:30:00', 45, 'Calle Alcalá 45', 42084, 42113, 'Estacionamiento en batería en parking subterráneo.', 'RECIBIDA', 8, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (19, '2026-08-29 11:30:00', 45, 'Calle Alcalá 45', 42113, 42152, 'Conducción con lluvia y adherencia reducida.', 'RECIBIDA', 8, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (20, '2026-09-05 11:30:00', 45, 'Calle Alcalá 45', 42152, 42185, 'Circulación fluida por vías secundarias.', 'RECIBIDA', 8, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (21, '2026-09-12 11:30:00', 45, 'Calle Alcalá 45', 42185, 42224, 'Simulación de trazado de examen DGT.', 'RECIBIDA', 8, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (22, '2026-09-26 10:30:00', 45, 'Calle Alcalá 45', 42224, 42252, 'Perfeccionamiento de maniobras autónomas.', 'RECIBIDA', 8, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (23, CURRENT_DATE + INTERVAL '5' DAY + TIME '10:00:00', 45, 'Calle Alcalá 45', 0, 0, 'Circulación en vías rápidas y glorietas.', 'PENDIENTE', 8, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (24, CURRENT_DATE + INTERVAL '7' DAY + TIME '11:30:00', 45, 'Calle Alcalá 45', 0, 0, 'Estacionamiento en línea y batería en zona céntrica.', 'PENDIENTE', 8, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (25, '2026-08-15 13:00:00', 45, 'Avenida Albufera 102', 42252, 42284, 'Inicio de prácticas tras aprobar examen teórico oficial.', 'RECIBIDA', 11, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (26, '2026-08-22 13:00:00', 45, 'Avenida Albufera 102', 42284, 42322, 'Salidas en pendiente pronunciada sin retroceso.', 'RECIBIDA', 11, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (27, '2026-08-29 13:00:00', 45, 'Avenida Albufera 102', 42322, 42360, 'Circulación en glorietas urbanas de varios carriles.', 'RECIBIDA', 11, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (28, '2026-09-05 13:00:00', 45, 'Avenida Albufera 102', 42360, 42389, 'Entrada y salida de autovía M-40.', 'RECIBIDA', 11, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (29, '2026-09-12 13:00:00', 45, 'Avenida Albufera 102', 42389, 42428, 'Maniobra de marcha atrás y parada técnica.', 'RECIBIDA', 11, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (30, '2026-09-19 11:30:00', 45, 'Avenida Albufera 102', 42428, 42457, 'Anticipación en retenciones de hora punta.', 'RECIBIDA', 11, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (31, '2026-09-26 13:00:00', 45, 'Avenida Albufera 102', 42457, 42491, 'Control de velocidad en zonas pacificadas a 30 km/h.', 'RECIBIDA', 11, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (32, '2026-10-01 11:30:00', 45, 'Avenida Albufera 102', 42491, 42520, 'Consolidación de estacionamiento rápido en línea.', 'RECIBIDA', 11, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (33, CURRENT_DATE + INTERVAL '7' DAY + TIME '08:30:00', 45, 'Avenida Albufera 102', 0, 0, 'Circulación en túneles urbanos y glorietas complejas.', 'PENDIENTE', 11, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (34, CURRENT_DATE + INTERVAL '9' DAY + TIME '11:30:00', 45, 'Avenida Albufera 102', 0, 0, 'Simulacro de prueba práctica de circulación.', 'PENDIENTE', 11, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (35, '2026-08-23 08:30:00', 45, 'Calle Alcalá 45', 42520, 42557, 'Inicio de formación práctica tras superar examen teórico.', 'RECIBIDA', 7, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (36, '2026-08-26 08:30:00', 45, 'Calle Alcalá 45', 42557, 42591, 'Manejo de embrague y cambio en vía urbana.', 'RECIBIDA', 7, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (37, '2026-08-30 08:30:00', 45, 'Calle Alcalá 45', 42591, 42619, 'Circulación por avenidas principales y semáforos en fase ámbar.', 'RECIBIDA', 7, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (38, '2026-09-02 08:30:00', 45, 'Calle Alcalá 45', 42619, 42651, 'Incorporaciones seguras a autovía A-2.', 'RECIBIDA', 7, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (39, '2026-09-06 08:30:00', 45, 'Calle Alcalá 45', 42651, 42687, 'Conducción en vías de servicio y glorietas.', 'RECIBIDA', 7, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (40, '2026-09-09 08:30:00', 45, 'Calle Alcalá 45', 42687, 42724, 'Estacionamiento en batería en pendiente suave.', 'RECIBIDA', 7, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (41, '2026-09-13 08:30:00', 45, 'Calle Alcalá 45', 42724, 42759, 'Control de distancia de seguridad y retrovisores.', 'RECIBIDA', 7, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (42, '2026-09-16 08:30:00', 45, 'Calle Alcalá 45', 42759, 42788, 'Conducción nocturna urbana.', 'RECIBIDA', 7, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (43, '2026-09-20 08:30:00', 45, 'Calle Alcalá 45', 42788, 42816, 'Simulacro de circulación autónoma durante 15 minutos.', 'RECIBIDA', 7, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (44, '2026-09-23 08:30:00', 45, 'Calle Alcalá 45', 42816, 42850, 'Maniobras de estacionamiento en línea entre dos vehículos.', 'RECIBIDA', 7, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (45, '2026-09-27 08:30:00', 45, 'Calle Alcalá 45', 42850, 42880, 'Práctica en tramo habitual de examen DGT Móstoles.', 'RECIBIDA', 7, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (46, '2026-10-02 08:30:00', 45, 'Calle Alcalá 45', 42880, 42915, 'Última clase preparatoria: aptitud consolidada para el examen práctico.', 'RECIBIDA', 7, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (47, '2026-08-24 10:00:00', 45, 'Calle Serrano 90', 42915, 42950, 'Inicio de clases prácticas tras superar la teórica.', 'RECIBIDA', 13, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (48, '2026-08-27 10:00:00', 45, 'Calle Serrano 90', 42950, 42986, 'Coordinación de pedales y marcha suave.', 'RECIBIDA', 13, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (49, '2026-08-31 10:00:00', 45, 'Calle Serrano 90', 42986, 43022, 'Glorietas y pasos peatonales.', 'RECIBIDA', 13, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (50, '2026-09-03 10:00:00', 45, 'Calle Serrano 90', 43022, 43051, 'Conducción fluida en M-30.', 'RECIBIDA', 13, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (51, '2026-09-07 10:00:00', 45, 'Calle Serrano 90', 43051, 43087, 'Estacionamiento en cordón.', 'RECIBIDA', 13, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (52, '2026-09-10 11:30:00', 45, 'Calle Serrano 90', 43087, 43126, 'Observación continua de retrovisores.', 'RECIBIDA', 13, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (53, '2026-09-14 10:00:00', 45, 'Calle Serrano 90', 43126, 43160, 'Manejo en calles estrechas del centro.', 'RECIBIDA', 13, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (54, '2026-09-17 10:00:00', 45, 'Calle Serrano 90', 43160, 43197, 'Anticipación a vehículos detenidos.', 'RECIBIDA', 13, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (55, '2026-09-21 10:00:00', 45, 'Calle Serrano 90', 43197, 43225, 'Control de velocidad en zonas escolares.', 'RECIBIDA', 13, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (56, '2026-09-24 10:00:00', 45, 'Calle Serrano 90', 43225, 43257, 'Estacionamiento marcha atrás con precisión.', 'RECIBIDA', 13, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (57, '2026-09-28 10:00:00', 45, 'Calle Serrano 90', 43257, 43286, 'Simulación de trazado de examen oficial.', 'RECIBIDA', 13, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (58, '2026-10-03 10:00:00', 45, 'Calle Serrano 90', 43286, 43318, 'Clase de consolidación técnica.', 'RECIBIDA', 13, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (59, CURRENT_DATE + INTERVAL '5' DAY + TIME '08:30:00', 45, 'Calle Serrano 90', 0, 0, 'Refuerzo de circulación autónoma.', 'PENDIENTE', 13, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (60, CURRENT_DATE + INTERVAL '8' DAY + TIME '13:00:00', 45, 'Calle Serrano 90', 0, 0, 'Práctica de perfeccionamiento en tráfico denso.', 'PENDIENTE', 13, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (61, '2026-09-08 10:00:00', 45, 'Avenida de América 10', 43318, 43351, 'Inicio de prácticas tras aprobar teórico oficial.', 'RECIBIDA', 9, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (62, '2026-09-11 10:00:00', 45, 'Avenida de América 10', 43351, 43385, 'Circulación fluida por carril bus-VAO y glorietas.', 'RECIBIDA', 9, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (63, '2026-09-15 10:00:00', 45, 'Avenida de América 10', 43385, 43414, 'Control de velocidad en autovía.', 'RECIBIDA', 9, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (64, '2026-09-18 10:00:00', 45, 'Avenida de América 10', 43414, 43452, 'Estacionamiento en línea.', 'RECIBIDA', 9, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (65, '2026-09-22 10:00:00', 45, 'Avenida de América 10', 43452, 43488, 'Manejo de embrague en paradas continuas.', 'RECIBIDA', 9, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (66, '2026-09-25 09:00:00', 45, 'Avenida de América 10', 43488, 43518, 'Conducción en vías secundarias.', 'RECIBIDA', 9, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (67, '2026-09-29 10:00:00', 45, 'Avenida de América 10', 43518, 43550, 'Observación de ángulos muertos.', 'RECIBIDA', 9, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (68, '2026-10-04 10:00:00', 45, 'Avenida de América 10', 43550, 43582, 'Simulacro de circulación autónoma.', 'RECIBIDA', 9, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (69, CURRENT_DATE + INTERVAL '6' DAY + TIME '08:30:00', 45, 'Avenida de América 10', 0, 0, 'Maniobras de estacionamiento y parada reglamentaria.', 'PENDIENTE', 9, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (70, CURRENT_DATE + INTERVAL '8' DAY + TIME '10:00:00', 45, 'Avenida de América 10', 0, 0, 'Circulación en tramos de tráfico intenso.', 'PENDIENTE', 9, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (71, '2026-08-07 10:00:00', 45, 'Plaza de Castilla 3', 43582, 43617, 'Inicio de prácticas de circulación urbana.', 'RECIBIDA', 10, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (72, '2026-08-14 10:00:00', 45, 'Plaza de Castilla 3', 43617, 43652, 'Manejo de caja de cambios e intermitentes en giros complejos.', 'RECIBIDA', 10, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (73, '2026-08-21 10:00:00', 45, 'Plaza de Castilla 3', 43652, 43686, 'Glorietas de acceso múltiple.', 'RECIBIDA', 10, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (74, '2026-08-28 10:00:00', 45, 'Plaza de Castilla 3', 43686, 43723, 'Estacionamiento en espina de pez.', 'RECIBIDA', 10, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (75, '2026-09-04 10:00:00', 45, 'Plaza de Castilla 3', 43723, 43762, 'Conducción en vías interurbanas.', 'RECIBIDA', 10, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (76, '2026-09-11 11:30:00', 45, 'Plaza de Castilla 3', 43762, 43795, 'Circulación por túneles urbanos.', 'RECIBIDA', 10, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (77, '2026-09-18 11:30:00', 45, 'Plaza de Castilla 3', 43795, 43834, 'Anticipación en glorietas saturadas.', 'RECIBIDA', 10, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (78, '2026-09-25 11:30:00', 45, 'Plaza de Castilla 3', 43834, 43863, 'Maniobras de cambio de sentido.', 'RECIBIDA', 10, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (79, CURRENT_DATE + INTERVAL '6' DAY + TIME '13:00:00', 45, 'Plaza de Castilla 3', 0, 0, 'Circulación en túneles urbanos y pasos subterráneos.', 'PENDIENTE', 10, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (80, CURRENT_DATE + INTERVAL '9' DAY + TIME '08:30:00', 45, 'Plaza de Castilla 3', 0, 0, 'Control de adherencia y trazado de curvas.', 'PENDIENTE', 10, 2);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (81, '2026-05-20 15:30:00', 45, 'Calle Velázquez 60', 29000, 29036, 'Iniciación en circuito urbano y manejo de mandos primarios.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (82, '2026-05-27 15:30:00', 45, 'Calle Velázquez 60', 29036, 29067, 'Circulación en glorietas y reducción de marchas.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (83, '2026-06-03 15:30:00', 45, 'Calle Velázquez 60', 29067, 29104, 'Incorporaciones a autovía y mantenimiento de carril.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (84, '2026-06-10 15:30:00', 45, 'Calle Velázquez 60', 29104, 29135, 'Maniobras de estacionamiento en línea.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (85, '2026-06-13 17:00:00', 45, 'Calle Velázquez 60', 29135, 29173, 'Conducción con tráfico moderado en vía rápida.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (86, '2026-06-17 15:30:00', 45, 'Calle Velázquez 60', 29173, 29206, 'Estacionamiento en batería en pendientes.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (87, '2026-06-20 17:00:00', 45, 'Calle Velázquez 60', 29206, 29245, 'Anticipación en intersecciones sin prioridad.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (88, '2026-06-24 15:30:00', 45, 'Calle Velázquez 60', 29245, 29277, 'Circulación en vías estrechas del casco histórico.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (89, '2026-06-26 17:00:00', 45, 'Calle Velázquez 60', 29277, 29307, 'Control de distancia de seguridad en retenciones.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (90, '2026-06-28 17:00:00', 45, 'Calle Velázquez 60', 29307, 29341, 'Simulacro previo a primera convocatoria oficial.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (91, '2026-07-05 15:30:00', 45, 'Calle Velázquez 60', 29341, 29376, 'Refuerzo tras primer suspenso: observación de espejos retrovisores.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (92, '2026-07-12 15:30:00', 45, 'Calle Velázquez 60', 29376, 29410, 'Consolidación de maniobras de estacionamiento.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (93, '2026-07-19 15:30:00', 45, 'Calle Velázquez 60', 29410, 29443, 'Práctica en tramo de examen de circulación.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (94, '2026-08-02 15:30:00', 45, 'Calle Velázquez 60', 29443, 29479, 'Refuerzo tras renovación de matrícula: control de nervios y calma.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (95, '2026-08-09 15:30:00', 45, 'Calle Velázquez 60', 29479, 29510, 'Circulación fluida en glorietas complejas.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (96, '2026-08-16 15:30:00', 45, 'Calle Velázquez 60', 29510, 29548, 'Simulacro completo de examen práctico.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (97, '2026-09-02 15:30:00', 45, 'Calle Velázquez 60', 29548, 29585, 'Refuerzo pedagógico: prioridad de paso a ciclistas y peatones.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (98, '2026-09-09 15:30:00', 45, 'Calle Velázquez 60', 29585, 29617, 'Maniobras de precisión en estacionamiento.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (99, '2026-09-16 15:30:00', 45, 'Calle Velázquez 60', 29617, 29656, 'Última práctica previa a cuarta convocatoria DGT.', 'RECIBIDA', 14, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (100, '2026-08-20 17:00:00', 45, 'Calle Alcalá 120', 29656, 29690, 'Posición en la calzada, observación por retrovisores y cambios de carril.', 'RECIBIDA', 25, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (101, '2026-08-27 17:00:00', 45, 'Calle Alcalá 120', 29690, 29722, 'Aproximación a glorietas y preferencia de paso peatonal.', 'RECIBIDA', 25, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (102, '2026-09-03 17:00:00', 45, 'Calle Alcalá 120', 29722, 29757, 'Manejo de marchas cortas en pendiente ascendente.', 'RECIBIDA', 25, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (103, '2026-09-10 17:00:00', 45, 'Calle Alcalá 120', 29757, 29789, 'Estacionamiento en batería.', 'RECIBIDA', 25, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (104, '2026-09-17 17:00:00', 45, 'Calle Alcalá 120', 29789, 29824, 'Circulación nocturna urbana.', 'RECIBIDA', 25, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (105, '2026-09-24 17:00:00', 45, 'Calle Alcalá 120', 29824, 29854, 'Conducción en autovía M-40.', 'RECIBIDA', 25, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (106, '2026-09-27 16:30:00', 45, 'Calle Alcalá 120', 29854, 29891, 'Anticipación en paradas imprevistas.', 'RECIBIDA', 25, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (107, '2026-10-01 17:00:00', 45, 'Calle Alcalá 120', 29891, 29928, 'Simulación de trazado de examen.', 'RECIBIDA', 25, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (108, CURRENT_DATE + INTERVAL '6' DAY + TIME '15:30:00', 45, 'Calle Alcalá 120', 0, 0, 'Circulación fluida en vías de doble sentido.', 'PENDIENTE', 25, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (109, CURRENT_DATE + INTERVAL '8' DAY + TIME '17:00:00', 45, 'Calle Alcalá 120', 0, 0, 'Perfeccionamiento de maniobras autónomas.', 'PENDIENTE', 25, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (110, '2026-08-18 18:30:00', 45, 'Calle Alcalá 120', 29928, 29967, 'Inicio de prácticas tras superar el examen teórico.', 'RECIBIDA', 26, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (111, '2026-08-25 18:30:00', 45, 'Calle Alcalá 120', 29967, 30001, 'Circulación nocturna e iluminación adecuada.', 'RECIBIDA', 26, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (112, '2026-09-01 18:30:00', 45, 'Calle Alcalá 120', 30001, 30032, 'Simulacro de examen práctico de circulación en casco urbano.', 'RECIBIDA', 26, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (113, '2026-09-08 18:30:00', 45, 'Calle Alcalá 120', 30032, 30069, 'Control de velocidad en zonas pacificadas.', 'RECIBIDA', 26, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (114, '2026-09-15 18:30:00', 45, 'Calle Alcalá 120', 30069, 30104, 'Estacionamiento en batería y en línea.', 'RECIBIDA', 26, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (115, '2026-09-22 18:30:00', 45, 'Calle Alcalá 120', 30104, 30140, 'Conducción por glorietas partidas.', 'RECIBIDA', 26, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (116, '2026-09-28 18:30:00', 45, 'Calle Alcalá 120', 30140, 30172, 'Incorporaciones a vías rápidas con fluidez.', 'RECIBIDA', 26, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (117, '2026-09-30 19:30:00', 45, 'Calle Alcalá 120', 30172, 30210, 'Comportamiento en retenciones y frenado suave.', 'RECIBIDA', 26, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (118, '2026-10-02 18:30:00', 45, 'Calle Alcalá 120', 30210, 30239, 'Trazado de curvas en vías interurbanas.', 'RECIBIDA', 26, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (119, '2026-10-03 17:00:00', 45, 'Calle Alcalá 120', 30239, 30267, 'Maniobra de cambio de sentido en calle estrecha.', 'RECIBIDA', 26, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (120, '2026-10-04 17:00:00', 45, 'Calle Alcalá 120', 30267, 30302, 'Consolidación general previa a citación.', 'RECIBIDA', 26, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (121, CURRENT_DATE + INTERVAL '5' DAY + TIME '17:00:00', 45, 'Calle Alcalá 120', 0, 0, 'Maniobras de estacionamiento en pendiente.', 'PENDIENTE', 26, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (122, CURRENT_DATE + INTERVAL '7' DAY + TIME '18:30:00', 45, 'Calle Alcalá 120', 0, 0, 'Simulación de trazado oficial de examen.', 'PENDIENTE', 26, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (123, '2026-07-21 16:00:00', 90, 'Polígono Industrial Las Mercedes', 138000, 138032, 'Manejo de caja de cambios de camión rígido y freno neumático.', 'RECIBIDA', 16, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (124, '2026-07-28 16:00:00', 90, 'Polígono Industrial Las Mercedes', 138032, 138069, 'Maniobras en muelle de carga y marcha atrás en curva con camión rígido.', 'RECIBIDA', 16, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (125, '2026-08-04 16:00:00', 90, 'Polígono Industrial Las Mercedes', 138069, 138105, 'Circulación por autovía M-50 adaptando distancias de frenado.', 'RECIBIDA', 16, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (126, '2026-08-11 16:00:00', 90, 'Polígono Industrial Las Mercedes', 138105, 138134, 'Aproximación a glorietas con vehículo pesado y radio de giro amplio.', 'RECIBIDA', 16, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (127, '2026-08-18 16:00:00', 90, 'Polígono Industrial Las Mercedes', 138134, 138165, 'Control de gálibo y visibilidad con espejos angulares.', 'RECIBIDA', 16, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (128, '2026-08-25 16:00:00', 90, 'Polígono Industrial Las Mercedes', 138165, 138202, 'Estacionamiento en rampa para carga y descarga.', 'RECIBIDA', 16, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (129, '2026-09-01 16:00:00', 90, 'Polígono Industrial Las Mercedes', 138202, 138241, 'Conducción en polígono industrial con obstáculos y camiones articulados.', 'RECIBIDA', 16, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (130, '2026-09-08 16:00:00', 90, 'Polígono Industrial Las Mercedes', 138241, 138278, 'Uso de retarder e intarder en descensos pronunciados.', 'RECIBIDA', 16, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (131, '2026-09-15 16:00:00', 90, 'Polígono Industrial Las Mercedes', 138278, 138310, 'Simulación de maniobra en L para examen de pista DGT.', 'RECIBIDA', 16, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (132, '2026-09-22 16:00:00', 90, 'Polígono Industrial Las Mercedes', 138310, 138348, 'Circulación urbana en avenidas amplias respetando señalización de masa.', 'RECIBIDA', 16, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (133, '2026-09-29 16:00:00', 90, 'Polígono Industrial Las Mercedes', 138348, 138376, 'Maniobra de aproximación a andén de carga.', 'RECIBIDA', 16, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (134, '2026-10-03 15:30:00', 90, 'Polígono Industrial Las Mercedes', 138376, 138408, 'Último repaso general: aptitud técnica y destreza óptima para examen C.', 'RECIBIDA', 16, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (135, '2026-08-27 18:00:00', 90, 'Polígono Industrial Las Mercedes', 138408, 138436, 'Iniciación al manejo de camión pesado y circuito de frenos.', 'RECIBIDA', 18, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (136, '2026-09-03 18:00:00', 90, 'Polígono Industrial Las Mercedes', 138436, 138473, 'Maniobras en circuito cerrado y control de ángulos muertos.', 'RECIBIDA', 18, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (137, '2026-09-10 18:00:00', 90, 'Polígono Industrial Las Mercedes', 138473, 138503, 'Circulación por autovía M-45.', 'RECIBIDA', 18, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (138, '2026-09-17 18:00:00', 90, 'Polígono Industrial Las Mercedes', 138503, 138539, 'Control de velocidad y marchas reductoras.', 'RECIBIDA', 18, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (139, '2026-09-24 18:00:00', 90, 'Polígono Industrial Las Mercedes', 138539, 138567, 'Maniobra de marcha atrás recta y en curva.', 'RECIBIDA', 18, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (140, '2026-10-01 18:00:00', 90, 'Polígono Industrial Las Mercedes', 138567, 138600, 'Aproximación segura a muelles.', 'RECIBIDA', 18, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (141, '2026-10-04 18:00:00', 90, 'Polígono Industrial Las Mercedes', 138600, 138639, 'Consolidación de maniobras de examen en pista cerrada.', 'RECIBIDA', 18, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (142, CURRENT_DATE + INTERVAL '6' DAY + TIME '18:00:00', 90, 'Polígono Industrial Las Mercedes', 0, 0, 'Circulación en autovía y control de aceleración.', 'PENDIENTE', 18, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (143, CURRENT_DATE + INTERVAL '8' DAY + TIME '18:00:00', 90, 'Polígono Industrial Las Mercedes', 0, 0, 'Simulación de prueba de destreza en pista DGT.', 'PENDIENTE', 18, 3);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (144, '2026-06-25 08:30:00', 45, 'Calle Silvano 22', 58000, 58028, 'Iniciación a circulación en vías rápidas y glorietas para Permiso B.', 'RECIBIDA', 27, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (145, '2026-06-28 08:30:00', 45, 'Calle Silvano 22', 58028, 58064, 'Manejo de caja de cambios y embrague en ciudad.', 'RECIBIDA', 27, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (146, '2026-07-02 08:30:00', 45, 'Calle Silvano 22', 58064, 58094, 'Circulación fluida por M-30.', 'RECIBIDA', 27, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (147, '2026-07-05 08:30:00', 45, 'Calle Silvano 22', 58094, 58129, 'Estacionamiento en línea y batería.', 'RECIBIDA', 27, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (148, '2026-07-08 10:00:00', 45, 'Calle Silvano 22', 58129, 58164, 'Anticipación en cruces peatonales.', 'RECIBIDA', 27, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (149, '2026-07-11 08:30:00', 45, 'Calle Silvano 22', 58164, 58193, 'Control de velocidad en autovía A-1.', 'RECIBIDA', 27, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (150, '2026-07-14 08:30:00', 45, 'Calle Silvano 22', 58193, 58227, 'Circulación nocturna urbana.', 'RECIBIDA', 27, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (151, '2026-07-16 11:00:00', 45, 'Calle Silvano 22', 58227, 58263, 'Simulacro de examen práctico de circulación en casco urbano para Permiso B.', 'RECIBIDA', 27, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (152, '2026-07-18 08:30:00', 45, 'Calle Silvano 22', 58263, 58292, 'Perfeccionamiento de maniobras de precisión.', 'RECIBIDA', 27, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (153, '2026-07-19 10:00:00', 45, 'Calle Silvano 22', 58292, 58331, 'Repaso de trazado habitual de examen.', 'RECIBIDA', 27, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (154, '2026-07-20 08:30:00', 45, 'Calle Silvano 22', 58331, 58370, 'Última clase previa al examen práctico aprobado a la primera.', 'RECIBIDA', 27, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (155, '2026-07-03 10:00:00', 45, 'Calle Alberto Aguilera 19', 58370, 58403, 'Inicio de prácticas tras aprobar examen teórico a la 4ª presentación.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (156, '2026-07-07 10:00:00', 45, 'Calle Alberto Aguilera 19', 58403, 58434, 'Control de embrague y frenado suave.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (157, '2026-07-10 10:00:00', 45, 'Calle Alberto Aguilera 19', 58434, 58468, 'Glorietas urbanas y cambios de carril.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (158, '2026-07-14 10:00:00', 45, 'Calle Alberto Aguilera 19', 58468, 58501, 'Incorporaciones a M-30.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (159, '2026-07-17 10:00:00', 45, 'Calle Alberto Aguilera 19', 58501, 58533, 'Estacionamiento en línea.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (160, '2026-07-21 10:00:00', 45, 'Calle Alberto Aguilera 19', 58533, 58572, 'Conducción en vías secundarias.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (161, '2026-07-24 10:00:00', 45, 'Calle Alberto Aguilera 19', 58572, 58607, 'Observación de retrovisores en cruces.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (162, '2026-07-26 10:00:00', 45, 'Calle Alberto Aguilera 19', 58607, 58644, 'Simulacro de circulación autónoma.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (163, '2026-07-28 10:00:00', 45, 'Calle Alberto Aguilera 19', 58644, 58679, 'Maniobras de marcha atrás en recta y curva.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (164, '2026-07-29 10:00:00', 45, 'Calle Alberto Aguilera 19', 58679, 58712, 'Consolidación previa a primera convocatoria práctica.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (165, '2026-08-05 10:00:00', 45, 'Calle Alberto Aguilera 19', 58712, 58743, 'Refuerzo tras primer suspenso práctico: giros y colocación.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (166, '2026-08-18 10:00:00', 45, 'Calle Alberto Aguilera 19', 58743, 58774, 'Repaso de estacionamiento rápido.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (167, '2026-08-26 10:00:00', 45, 'Calle Alberto Aguilera 19', 58774, 58810, 'Refuerzo tras segunda convocatoria: atención a prioridad de paso.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (168, '2026-09-08 10:00:00', 45, 'Calle Alberto Aguilera 19', 58810, 58838, 'Circulación en tramos complejos de Móstoles.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (169, '2026-09-18 10:00:00', 45, 'Calle Alberto Aguilera 19', 58838, 58877, 'Refuerzo tras tercera convocatoria: serenidad al volante.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (170, '2026-09-28 10:00:00', 45, 'Calle Alberto Aguilera 19', 58877, 58905, 'Repaso definitivo previo a la cuarta prueba práctica oficial.', 'RECIBIDA', 22, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (171, '2026-08-02 11:30:00', 45, 'Paseo de la Castellana 200', 58905, 58943, 'Inicio de prácticas tras aprobar el examen teórico.', 'RECIBIDA', 20, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (172, '2026-08-09 11:30:00', 45, 'Paseo de la Castellana 200', 58943, 58975, 'Circulación por glorietas partidas y regulación semafórica.', 'RECIBIDA', 20, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (173, '2026-08-16 11:30:00', 45, 'Paseo de la Castellana 200', 58975, 59014, 'Conducción eficiente y anticipación en retenciones urbanas.', 'RECIBIDA', 20, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (174, '2026-08-23 11:30:00', 45, 'Paseo de la Castellana 200', 59014, 59050, 'Incorporación a vías de alta capacidad.', 'RECIBIDA', 20, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (175, '2026-08-30 11:30:00', 45, 'Paseo de la Castellana 200', 59050, 59087, 'Estacionamiento en línea y batería en zona céntrica.', 'RECIBIDA', 20, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (176, '2026-09-06 11:30:00', 45, 'Paseo de la Castellana 200', 59087, 59126, 'Conducción nocturna y en túneles.', 'RECIBIDA', 20, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (177, '2026-09-13 11:30:00', 45, 'Paseo de la Castellana 200', 59126, 59155, 'Maniobras de giro y prioridad en intersecciones.', 'RECIBIDA', 20, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (178, '2026-09-20 11:30:00', 45, 'Paseo de la Castellana 200', 59155, 59194, 'Trazado de curvas interurbanas.', 'RECIBIDA', 20, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (179, '2026-09-24 11:30:00', 45, 'Paseo de la Castellana 200', 59194, 59233, 'Control de velocidad en zonas pacificadas.', 'RECIBIDA', 20, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (180, '2026-09-27 11:30:00', 45, 'Paseo de la Castellana 200', 59233, 59261, 'Simulacro de circulación autónoma.', 'RECIBIDA', 20, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (181, '2026-09-29 08:30:00', 45, 'Paseo de la Castellana 200', 59261, 59290, 'Práctica en circuito habitual de examen DGT.', 'RECIBIDA', 20, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (182, '2026-10-03 10:00:00', 45, 'Paseo de la Castellana 200', 59290, 59327, 'Última clase: consolidación y destreza acreditada para examen práctico.', 'RECIBIDA', 20, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (183, '2026-09-06 13:00:00', 45, 'Calle Guzmán el Bueno 44', 59327, 59360, 'Inicio de prácticas tras superar el examen teórico.', 'RECIBIDA', 21, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (184, '2026-09-10 13:00:00', 45, 'Calle Guzmán el Bueno 44', 59360, 59399, 'Manejo de embrague y cambios.', 'RECIBIDA', 21, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (185, '2026-09-13 13:00:00', 45, 'Calle Guzmán el Bueno 44', 59399, 59427, 'Glorietas y preferencia de paso.', 'RECIBIDA', 21, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (186, '2026-09-17 13:00:00', 45, 'Calle Guzmán el Bueno 44', 59427, 59461, 'Estacionamiento en línea.', 'RECIBIDA', 21, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (187, '2026-09-20 13:00:00', 45, 'Calle Guzmán el Bueno 44', 59461, 59493, 'Conducción en autovía A-6.', 'RECIBIDA', 21, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (188, '2026-09-24 13:00:00', 45, 'Calle Guzmán el Bueno 44', 59493, 59529, 'Maniobras de marcha atrás.', 'RECIBIDA', 21, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (189, '2026-09-27 13:00:00', 45, 'Calle Guzmán el Bueno 44', 59529, 59558, 'Anticipación en frenadas.', 'RECIBIDA', 21, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (190, '2026-10-01 13:00:00', 45, 'Calle Guzmán el Bueno 44', 59558, 59596, 'Consolidación general.', 'RECIBIDA', 21, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (191, CURRENT_DATE + INTERVAL '6' DAY + TIME '10:00:00', 45, 'Calle Guzmán el Bueno 44', 0, 0, 'Circulación en tramos urbanos densos.', 'PENDIENTE', 21, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (192, CURRENT_DATE + INTERVAL '8' DAY + TIME '11:30:00', 45, 'Calle Guzmán el Bueno 44', 0, 0, 'Simulación de trazado de examen.', 'PENDIENTE', 21, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (193, '2026-08-27 13:00:00', 45, 'Paseo de las Delicias 35', 59596, 59632, 'Iniciación a mandos y circulación en calzada ancha.', 'RECIBIDA', 23, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (194, '2026-09-03 13:00:00', 45, 'Paseo de las Delicias 35', 59632, 59661, 'Glorietas urbanas y observación de espejos.', 'RECIBIDA', 23, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (195, '2026-09-11 13:00:00', 45, 'Paseo de las Delicias 35', 59661, 59692, 'Estacionamiento en línea.', 'RECIBIDA', 23, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (196, '2026-09-18 13:00:00', 45, 'Paseo de las Delicias 35', 59692, 59725, 'Conducción por M-30.', 'RECIBIDA', 23, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (197, '2026-09-25 13:00:00', 45, 'Paseo de las Delicias 35', 59725, 59759, 'Control de velocidad y frenado suave.', 'RECIBIDA', 23, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (198, '2026-10-02 13:00:00', 45, 'Paseo de las Delicias 35', 59759, 59796, 'Consolidación de maniobras.', 'RECIBIDA', 23, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (199, CURRENT_DATE + INTERVAL '7' DAY + TIME '10:00:00', 45, 'Paseo de las Delicias 35', 0, 0, 'Circulación en glorietas partidas.', 'PENDIENTE', 23, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (200, CURRENT_DATE + INTERVAL '9' DAY + TIME '13:00:00', 45, 'Paseo de las Delicias 35', 0, 0, 'Estacionamiento en batería en pendiente.', 'PENDIENTE', 23, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (201, '2026-07-22 08:30:00', 60, 'Calle Sagasta 21', 59796, 59834, 'Acoplamiento y desacoplamiento de remolque B+E en zona habilitada.', 'RECIBIDA', 19, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (202, '2026-07-29 08:30:00', 60, 'Calle Sagasta 21', 59834, 59870, 'Maniobra de marcha atrás en línea recta y curva con remolque.', 'RECIBIDA', 19, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (203, '2026-08-05 11:30:00', 60, 'Calle Sagasta 21', 59870, 59906, 'Circulación urbana controlando radio de giro y voladizo del remolque.', 'RECIBIDA', 19, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (204, '2026-08-12 08:30:00', 60, 'Calle Sagasta 21', 59906, 59938, 'Estacionamiento seguro de conjunto de vehículos en muelle.', 'RECIBIDA', 19, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (205, '2026-08-19 08:30:00', 60, 'Calle Sagasta 21', 59938, 59966, 'Incorporación a vías rápidas con conjunto de más de 3.500 kg.', 'RECIBIDA', 19, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (206, '2026-08-26 08:30:00', 60, 'Calle Sagasta 21', 59966, 60003, 'Frenado de emergencia y reparto de pesos en remolque.', 'RECIBIDA', 19, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (207, '2026-09-02 11:30:00', 60, 'Calle Sagasta 21', 60003, 60038, 'Maniobra de aproximación y centrado marcha atrás.', 'RECIBIDA', 19, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (208, '2026-09-09 11:30:00', 60, 'Calle Sagasta 21', 60038, 60068, 'Circulación en glorietas manteniendo remolque dentro del carril.', 'RECIBIDA', 19, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (209, '2026-09-16 11:30:00', 60, 'Calle Sagasta 21', 60068, 60099, 'Conducción con viento lateral en autovía.', 'RECIBIDA', 19, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (210, '2026-09-23 11:30:00', 60, 'Calle Sagasta 21', 60099, 60128, 'Simulación de prueba de destreza en circuito cerrado DGT.', 'RECIBIDA', 19, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (211, '2026-09-27 10:00:00', 60, 'Calle Sagasta 21', 60128, 60159, 'Circulación en tráfico denso con conjunto articulado.', 'RECIBIDA', 19, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (212, '2026-10-04 08:30:00', 60, 'Calle Sagasta 21', 60159, 60192, 'Última clase: dominio completo de maniobras con remolque para examen B+E.', 'RECIBIDA', 19, 4);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (213, '2026-08-28 15:30:00', 45, 'Calle Costa Rica 10', 26000, 26034, 'Inicio de prácticas tras superar examen teórico oficial.', 'RECIBIDA', 29, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (214, '2026-09-04 15:30:00', 45, 'Calle Costa Rica 10', 26034, 26071, 'Circulación en vías urbanas y glorietas.', 'RECIBIDA', 29, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (215, '2026-09-11 15:30:00', 45, 'Calle Costa Rica 10', 26071, 26099, 'Manejo de caja de cambios y embrague.', 'RECIBIDA', 29, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (216, '2026-09-18 15:30:00', 45, 'Calle Costa Rica 10', 26099, 26129, 'Estacionamiento en línea.', 'RECIBIDA', 29, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (217, '2026-09-25 15:30:00', 45, 'Calle Costa Rica 10', 26129, 26164, 'Conducción por autovía M-30.', 'RECIBIDA', 29, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (218, '2026-09-28 15:30:00', 45, 'Calle Costa Rica 10', 26164, 26195, 'Anticipación en pasos de peatones.', 'RECIBIDA', 29, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (219, '2026-10-01 15:30:00', 45, 'Calle Costa Rica 10', 26195, 26229, 'Circulación en túneles urbanos.', 'RECIBIDA', 29, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (220, '2026-10-03 15:30:00', 45, 'Calle Costa Rica 10', 26229, 26266, 'Consolidación de maniobras autónomas.', 'RECIBIDA', 29, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (221, CURRENT_DATE + INTERVAL '6' DAY + TIME '17:00:00', 45, 'Calle Costa Rica 10', 0, 0, 'Perfeccionamiento de circulación urbana.', 'PENDIENTE', 29, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (222, CURRENT_DATE + INTERVAL '9' DAY + TIME '18:30:00', 45, 'Calle Costa Rica 10', 0, 0, 'Simulación de trazado de examen DGT.', 'PENDIENTE', 29, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (223, '2026-07-25 17:00:00', 60, 'Centro de Exámenes DGT Móstoles', 10500, 10536, 'Iniciación en pista: equilibrio a baja velocidad y frenada combinada.', 'RECIBIDA', 17, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (224, '2026-08-01 17:00:00', 60, 'Centro de Exámenes DGT Móstoles', 10536, 10573, 'Circuito cerrado de destreza A2: zigzag entre jalones y frenada de emergencia.', 'RECIBIDA', 17, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (225, '2026-08-08 17:00:00', 60, 'Centro de Exámenes DGT Móstoles', 10573, 10602, 'Prueba cronometrada en pista: aceleración, esquiva de obstáculo y frenada en parada.', 'RECIBIDA', 17, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (226, '2026-08-15 17:00:00', 60, 'Centro de Exámenes DGT Móstoles', 10602, 10633, 'Manejo de acelerador y embrague en maniobras lentas.', 'RECIBIDA', 17, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (227, '2026-08-22 17:00:00', 60, 'Centro de Exámenes DGT Móstoles', 10633, 10661, 'Trazado de curvas en circuito cerrado.', 'RECIBIDA', 17, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (228, '2026-08-29 17:00:00', 60, 'Centro de Exámenes DGT Móstoles', 10661, 10692, 'Simulacro de examen en pista superado sin derribar jalones.', 'RECIBIDA', 17, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (229, '2026-09-05 17:00:00', 60, 'Centro de Exámenes DGT Móstoles', 10692, 10721, 'Circulación interurbana en motocicleta: posición de seguridad en carril.', 'RECIBIDA', 17, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (230, '2026-09-12 17:00:00', 60, 'Centro de Exámenes DGT Móstoles', 10721, 10759, 'Conducción con viento y adelantamientos a vehículos pesados.', 'RECIBIDA', 17, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (231, '2026-09-19 17:00:00', 60, 'Centro de Exámenes DGT Móstoles', 10759, 10789, 'Circulación en glorietas y anticipación a ángulos muertos de turismos.', 'RECIBIDA', 17, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (232, '2026-09-23 17:00:00', 60, 'Centro de Exámenes DGT Móstoles', 10789, 10827, 'Práctica intensiva en pista de examen DGT.', 'RECIBIDA', 17, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (233, '2026-10-02 17:00:00', 60, 'Centro de Exámenes DGT Móstoles', 10827, 10860, 'Última clase: destreza y tiempos óptimos consolidados para examen A2.', 'RECIBIDA', 17, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (234, '2026-09-15 18:30:00', 60, 'Centro de Exámenes DGT Móstoles', 10860, 10894, 'Inicio de prácticas A2 tras aprobar examen teórico.', 'RECIBIDA', 12, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (235, '2026-09-18 18:30:00', 60, 'Centro de Exámenes DGT Móstoles', 10894, 10930, 'Equilibrio entre líneas y aceleración en pista.', 'RECIBIDA', 12, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (236, '2026-09-22 18:30:00', 60, 'Centro de Exámenes DGT Móstoles', 10930, 10959, 'Zigzag entre jalones y frenada controlada.', 'RECIBIDA', 12, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (237, '2026-09-25 18:30:00', 60, 'Centro de Exámenes DGT Móstoles', 10959, 10992, 'Prueba de velocidad y esquiva.', 'RECIBIDA', 12, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (238, '2026-09-26 16:00:00', 60, 'Centro de Exámenes DGT Móstoles', 10992, 11022, 'Trazado de curvas y contra-manillar.', 'RECIBIDA', 12, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (239, '2026-09-29 18:30:00', 60, 'Centro de Exámenes DGT Móstoles', 11022, 11057, 'Circulación urbana en tráfico real.', 'RECIBIDA', 12, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (240, '2026-10-01 18:30:00', 60, 'Centro de Exámenes DGT Móstoles', 11057, 11095, 'Entradas y salidas de glorietas en moto.', 'RECIBIDA', 12, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (241, '2026-10-03 18:30:00', 60, 'Centro de Exámenes DGT Móstoles', 11095, 11130, 'Consolidación de maniobras de destreza.', 'RECIBIDA', 12, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (242, CURRENT_DATE + INTERVAL '6' DAY + TIME '16:00:00', 60, 'Centro de Exámenes DGT Móstoles', 0, 0, 'Simulacro de prueba cronometrada de destreza A2.', 'PENDIENTE', 12, 5);

INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, alumno_id, profesor_id)
VALUES (243, CURRENT_DATE + INTERVAL '8' DAY + TIME '16:00:00', 60, 'Centro de Exámenes DGT Móstoles', 0, 0, 'Circulación en tramos de vía rápida interurbana.', 'PENDIENTE', 12, 5);

-- ------------------------------------------------------------------------------
-- 7. SOLICITUDES DE EXAMEN DGT (solicitud_examen) - 43 Solicitudes
-- ------------------------------------------------------------------------------
INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (1, 'ACEPTADA', 'Solicitud teórica aceptada.', 7, 2, 1);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (2, 'ACEPTADA', 'Citación para examen práctico de circulación.', 7, 2, 1);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (3, 'ACEPTADA', 'Solicitud teórica aceptada.', 15, 2, 9);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (4, 'ACEPTADA', 'Primera convocatoria práctica.', 15, 2, 9);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (5, 'ACEPTADA', 'Segunda convocatoria práctica.', 15, 2, 9);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (6, 'ACEPTADA', 'Primera convocatoria práctica.', 14, 3, 8);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (7, 'ACEPTADA', 'Segunda convocatoria práctica.', 14, 3, 8);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (8, 'ACEPTADA', 'Solicitud teórica aceptada.', 9, 2, 3);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (9, 'PENDIENTE', 'Alumno con 10 prácticas completadas y destreza acreditada en vías rápidas.', 8, 2, 2);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (10, 'PENDIENTE', 'Solicitud para examen de destreza en circuito cerrado A2.', 12, 5, 6);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (11, 'PENDIENTE', 'Solicitud teórica Permiso B tras completar test de autoescuela.', 11, 2, 5);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (12, 'PENDIENTE', 'Alumno preparado para el examen práctico oficial de circulación.', 26, 3, 20);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (13, 'RECHAZADA', 'Se aconseja realizar al menos 5 clases prácticas más de circulación en vías rápidas antes de ir a examen.', 10, 2, 4);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (14, 'RECHAZADA', 'Certificado médico psicotécnico caducado; presentar renovación en secretaría.', 18, 3, 12);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (15, 'RECHAZADA', 'Falta afianzar la maniobra de estacionamiento en pendiente antes de ir a examen.', 23, 4, 17);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (16, 'ACEPTADA', 'Solicitud teórica aceptada.', 21, 4, 15);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (17, 'ACEPTADA', 'Citación para examen teórico oficial DGT.', 24, 5, 18);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (18, 'ACEPTADA', 'Solicitud teórica Permiso B aceptada.', 27, 4, 27);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (19, 'ACEPTADA', 'Citación para examen práctico Permiso B.', 27, 4, 27);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (20, 'ACEPTADA', 'Solicitud teórica A2 aceptada.', 12, 5, 6);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (21, 'ACEPTADA', 'Solicitud teórica aceptada.', 11, 2, 5);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (22, 'ACEPTADA', 'Solicitud teórica A2 aceptada.', 17, 5, 11);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (23, 'ACEPTADA', 'Citación para examen práctico en circuito cerrado A2.', 17, 5, 11);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (24, 'ACEPTADA', 'Solicitud teórica aceptada.', 20, 4, 14);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (25, 'ACEPTADA', 'Citación para examen práctico de circulación.', 20, 4, 14);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (26, 'ACEPTADA', 'Solicitud teórica aceptada.', 26, 3, 20);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (27, 'ACEPTADA', 'Solicitud teórica aceptada.', 29, 5, 23);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (28, 'ACEPTADA', 'Citación para examen práctico con camión rígido Permiso C.', 16, 3, 10);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (29, 'ACEPTADA', 'Citación para examen práctico de remolque Permiso B+E.', 19, 4, 13);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (30, 'ACEPTADA', 'Primera convocatoria teórica.', 13, 2, 7);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (31, 'ACEPTADA', 'Segunda convocatoria teórica.', 13, 2, 7);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (32, 'ACEPTADA', 'Tercera convocatoria teórica tras renovación.', 13, 2, 7);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (33, 'ACEPTADA', 'Cuarta convocatoria teórica.', 13, 2, 7);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (34, 'ACEPTADA', 'Tercera convocatoria práctica tras renovación.', 14, 3, 8);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (35, 'ACEPTADA', 'Cuarta convocatoria práctica.', 14, 3, 8);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (36, 'ACEPTADA', 'Primera convocatoria teórica.', 22, 4, 16);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (37, 'ACEPTADA', 'Segunda convocatoria teórica.', 22, 4, 16);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (38, 'ACEPTADA', 'Tercera convocatoria teórica tras renovación.', 22, 4, 16);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (39, 'ACEPTADA', 'Cuarta convocatoria teórica.', 22, 4, 16);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (40, 'ACEPTADA', 'Primera convocatoria práctica.', 22, 4, 16);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (41, 'ACEPTADA', 'Segunda convocatoria práctica.', 22, 4, 16);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (42, 'ACEPTADA', 'Tercera convocatoria práctica tras renovación.', 22, 4, 16);

INSERT INTO solicitud_examen (id, estado, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (43, 'ACEPTADA', 'Cuarta convocatoria práctica.', 22, 4, 16);

-- ------------------------------------------------------------------------------
-- 8. EXÁMENES DGT (examen) - 36 Exámenes
-- ------------------------------------------------------------------------------
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (1, TRUE, '2026-08-20 09:00:00', 30, 'TEORICO', 7, 1);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (2, TRUE, '2026-06-05 10:00:00', 30, 'TEORICO', 15, 3);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (7, TRUE, '2026-09-05 09:00:00', 30, 'TEORICO', 9, 8);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (8, TRUE, '2026-09-12 11:00:00', 30, 'TEORICO', 12, 20);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (14, TRUE, '2026-09-02 10:00:00', 30, 'TEORICO', 21, 16);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (16, TRUE, '2026-06-20 10:00:00', 30, 'TEORICO', 27, 18);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (18, TRUE, '2026-08-10 10:00:00', 30, 'TEORICO', 11, 21);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (19, TRUE, '2026-07-20 10:00:00', 30, 'TEORICO', 17, 22);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (20, TRUE, '2026-07-25 11:00:00', 30, 'TEORICO', 20, 24);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (21, TRUE, '2026-08-15 10:00:00', 30, 'TEORICO', 26, 26);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (22, TRUE, '2026-08-25 10:00:00', 30, 'TEORICO', 29, 27);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (23, FALSE, '2026-07-02 09:00:00', 30, 'TEORICO', 13, 30);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (24, FALSE, '2026-07-16 10:00:00', 30, 'TEORICO', 13, 31);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (25, FALSE, '2026-08-06 09:00:00', 30, 'TEORICO', 13, 32);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (26, TRUE, '2026-08-20 10:00:00', 30, 'TEORICO', 13, 33);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (29, FALSE, '2026-05-08 09:00:00', 30, 'TEORICO', 22, 36);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (30, FALSE, '2026-05-22 10:00:00', 30, 'TEORICO', 22, 37);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (31, FALSE, '2026-06-12 09:00:00', 30, 'TEORICO', 22, 38);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (32, TRUE, '2026-06-26 10:00:00', 30, 'TEORICO', 22, 39);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (6, FALSE, '2026-08-25 09:30:00', 25, 'PRACTICO', 15, 4);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (9, TRUE, '2026-09-15 10:15:00', 35, 'PRACTICO', 15, 5);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (17, TRUE, '2026-07-22 11:00:00', 35, 'PRACTICO', 27, 19);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (4, FALSE, '2026-06-30 16:30:00', 25, 'PRACTICO', 14, 6);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (5, FALSE, '2026-07-25 17:00:00', 25, 'PRACTICO', 14, 7);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (27, FALSE, '2026-08-25 16:30:00', 25, 'PRACTICO', 14, 34);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (28, FALSE, '2026-09-25 17:15:00', 25, 'PRACTICO', 14, 35);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (33, FALSE, '2026-07-31 10:30:00', 25, 'PRACTICO', 22, 40);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (34, FALSE, '2026-08-21 11:00:00', 25, 'PRACTICO', 22, 41);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (35, FALSE, '2026-09-12 10:30:00', 25, 'PRACTICO', 22, 42);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (36, FALSE, '2026-10-02 11:15:00', 25, 'PRACTICO', 22, 43);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (3, NULL, CURRENT_DATE + INTERVAL '10' DAY + TIME '10:00:00', 35, 'PRACTICO', 7, 2);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (10, NULL, CURRENT_DATE + INTERVAL '10' DAY + TIME '16:00:00', 45, 'PRACTICO', 16, 28);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (11, NULL, CURRENT_DATE + INTERVAL '20' DAY + TIME '11:00:00', 40, 'PRACTICO', 19, 29);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (12, NULL, CURRENT_DATE + INTERVAL '20' DAY + TIME '17:00:00', 30, 'PRACTICO', 17, 23);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (13, NULL, CURRENT_DATE + INTERVAL '23' DAY + TIME '12:00:00', 35, 'PRACTICO', 20, 25);

INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (15, NULL, CURRENT_DATE + INTERVAL '12' DAY + TIME '09:30:00', 30, 'TEORICO', 24, 17);

-- ------------------------------------------------------------------------------
-- 9. REAJUSTE DE SECUENCIAS DE IDENTIDAD (H2)
-- ------------------------------------------------------------------------------
-- Permite que las inserciones posteriores desde los formularios de la aplicación
-- no colisionen con los IDs insertados en este script semilla.
ALTER TABLE vehiculo ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE persona ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE incidencia_vehiculo ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE matricula ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE clase_practica ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE solicitud_examen ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE examen ALTER COLUMN id RESTART WITH 1000;
