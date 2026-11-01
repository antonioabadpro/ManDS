-- ==============================================================================
-- SCRIPT DE DATOS SEMILLA PARA EL SISTEMA ERP DE AUTOESCUELAS (H2 / DESARROLLO)
-- ==============================================================================
-- Este script puebla todas las entidades del dominio modelando el flujo operativo
-- real de una autoescuela oficial española (DGT), garantizando integridad referencial,
-- consistencia matemática en saldos y odómetros, y cumplimiento estricto de las reglas
-- de negocio establecidas.
--
-- Ordenación estricta de identidades (persona):
--   * Administrador (id = 1)
--   * Profesores    (id = 100 .. 129) [Rango: 100 .. 199]
--   * Alumnos       (id = 200 .. 267) [Rango: 200 .. 299]
--   * Rango libre   (id = 2 .. 99)
--
-- Flota completa de 12 permisos reconocidos por la DGT (mínimo 2 vehículos por permiso):
--   * Permiso B   : id = 1 .. 7
--   * Permiso A2  : id = 8 .. 10
--   * Permiso C   : id = 11 .. 13
--   * Permiso D   : id = 14 .. 15
--   * Permiso B+E : id = 16 .. 17
--   * Permiso AM  : id = 18 .. 19
--   * Permiso A1  : id = 20 .. 21
--   * Permiso A   : id = 22 .. 23
--   * Permiso B1  : id = 24 .. 25
--   * Permiso C+E : id = 26 .. 27
--   * Permiso D+E : id = 28 .. 29
--   * Permiso LVA : id = 30 .. 31
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. FLOTA DE VEHÍCULOS (vehiculo) - 34 Vehículos (Mínimo 2 por carnet, 3 inactivos)
-- ------------------------------------------------------------------------------
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (1, '1234-LMN', 'SEAT', 'Ibiza 1.0 TSI', 'Blanco Nevada', 43880, 95, 2021, 'GASOLINA', 'MANUAL', '2026-01-10', '2027-01-10', 'OCUPADO', 'PERMISO_B');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (2, '2345-LKP', 'Renault', 'Clio 1.0 TCe', 'Gris Platino', 38200, 90, 2021, 'GASOLINA', 'MANUAL', '2026-02-15', '2027-02-15', 'MANTENIMIENTO', 'PERMISO_B');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (3, '3456-JTR', 'Toyota', 'Yaris 1.5 Hybrid', 'Azul Boreal', 30610, 116, 2022, 'HIBRIDO', 'AUTOMATICO', '2026-03-20', '2028-03-20', 'OCUPADO', 'PERMISO_B');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (4, '4567-KLL', 'Peugeot', '208 PureTech', 'Rojo Elixir', 31100, 100, 2022, 'GASOLINA', 'MANUAL', '2026-04-05', '2027-04-05', 'OCUPADO', 'PERMISO_B');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (5, '5678-MNP', 'Volkswagen', 'Polo 1.0 TSI', 'Plata Réflex', 18500, 95, 2023, 'GASOLINA', 'MANUAL', '2026-03-12', '2026-09-20', 'DISPONIBLE', 'PERMISO_B');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (6, '6789-PBC', 'Ford', 'Fiesta 1.0 EcoBoost', 'Negro Agate', 22000, 100, 2022, 'GASOLINA', 'MANUAL', '2026-01-25', '2028-01-25', 'DISPONIBLE', 'PERMISO_B');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (7, '7890-XWZ', 'Citroën', 'C3 PureTech', 'Blanco Banquise', 52300, 83, 2020, 'GASOLINA', 'MANUAL', '2026-06-18', '2027-06-18', 'DISPONIBLE', 'PERMISO_B');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (8, '8901-KTM', 'Yamaha', 'MT-07 (35 kW)', 'Cyan Storm', 14432, 47, 2022, 'GASOLINA', 'MANUAL', '2026-04-10', '2026-10-05', 'DISPONIBLE', 'PERMISO_A2');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (9, '9012-HND', 'Honda', 'CB500F (35 kW)', 'Rojo Grand Prix', 19800, 47, 2021, 'GASOLINA', 'MANUAL', '2026-03-15', '2027-03-15', 'OCUPADO', 'PERMISO_A2');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (10, '0123-KWK', 'Kawasaki', 'Z650 (35 kW)', 'Verde Candy', 24500, 47, 2021, 'GASOLINA', 'MANUAL', '2026-04-20', '2028-04-20', 'DISPONIBLE', 'PERMISO_A2');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (11, '1122-MBX', 'Mercedes-Benz', 'Actros 1845', 'Blanco Ártico', 142000, 449, 2019, 'DIESEL', 'AUTOMATICO', '2026-05-30', '2027-05-30', 'DISPONIBLE', 'PERMISO_C');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (12, '2233-MNX', 'MAN', 'TGX 18.470', 'Azul Tráfico', 138185, 470, 2020, 'DIESEL', 'AUTOMATICO', '2026-03-01', '2026-09-01', 'OCUPADO', 'PERMISO_C');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (13, '3344-SCX', 'Scania', 'R450', 'Blanco Puro', 165000, 450, 2019, 'DIESEL', 'AUTOMATICO', '2026-02-15', '2028-02-15', 'DISPONIBLE', 'PERMISO_C');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (14, '4455-IRZ', 'Irizar', 'i6 / Scania K410', 'Gris Metalizado', 210212, 410, 2018, 'DIESEL', 'AUTOMATICO', '2026-01-20', '2027-01-20', 'OCUPADO', 'PERMISO_D');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (15, '5566-MTR', 'Mercedes-Benz', 'Tourismo', 'Blanco Puro', 185000, 428, 2019, 'DIESEL', 'AUTOMATICO', '2026-04-10', '2028-04-10', 'DISPONIBLE', 'PERMISO_D');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (16, '6677-FRD', 'Ford', 'Ranger 2.0 EcoBlue + Remolque', 'Gris Zinc', 65283, 170, 2021, 'DIESEL', 'MANUAL', '2026-03-10', '2027-03-10', 'OCUPADO', 'PERMISO_B_E');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (17, '7788-THX', 'Toyota', 'Hilux 2.4 D-4D + Remolque', 'Blanco Polar', 72000, 150, 2020, 'DIESEL', 'MANUAL', '2026-02-28', '2028-02-28', 'DISPONIBLE', 'PERMISO_B_E');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (18, '8899-PGZ', 'Piaggio', 'Zip 50 4T', 'Negro Lucido', 8500, 3, 2022, 'GASOLINA', 'AUTOMATICO', '2026-05-15', '2027-05-15', 'OCUPADO', 'PERMISO_AM');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (19, '9900-KAG', 'Kymco', 'Agility 50 City', 'Blanco Puro', 11200, 3, 2021, 'GASOLINA', 'AUTOMATICO', '2026-04-12', '2028-04-12', 'DISPONIBLE', 'PERMISO_AM');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (20, '1011-HBF', 'Honda', 'CB125F', 'Rojo Imperial', 9300, 11, 2022, 'GASOLINA', 'MANUAL', '2026-03-05', '2027-03-05', 'OCUPADO', 'PERMISO_A1');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (21, '1012-YMT', 'Yamaha', 'MT-125', 'Gris Tech Black', 12400, 15, 2021, 'GASOLINA', 'MANUAL', '2026-02-20', '2028-02-20', 'DISPONIBLE', 'PERMISO_A1');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (22, '1013-BMW', 'BMW', 'R 1250 GS', 'Triple Black', 34000, 136, 2021, 'GASOLINA', 'MANUAL', '2026-04-18', '2027-04-18', 'OCUPADO', 'PERMISO_A');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (23, '1014-KWN', 'Kawasaki', 'Ninja 1000SX', 'Verde Esmeralda', 28000, 142, 2022, 'GASOLINA', 'MANUAL', '2026-05-22', '2028-05-22', 'DISPONIBLE', 'PERMISO_A');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (24, '1015-CAM', 'Citroën', 'Ami Electric', 'Azul Ami', 4200, 8, 2023, 'ELECTRICO', 'AUTOMATICO', '2026-06-10', '2027-06-10', 'OCUPADO', 'PERMISO_B1');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (25, '1016-MCR', 'Microlino', '2.0 Pioneer', 'Blanco Santorini', 3100, 17, 2023, 'ELECTRICO', 'AUTOMATICO', '2026-05-18', '2028-05-18', 'DISPONIBLE', 'PERMISO_B1');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (26, '1017-VFH', 'Volvo', 'FH16 750 + Semirremolque', 'Negro Ébano', 195000, 750, 2020, 'DIESEL', 'AUTOMATICO', '2026-01-30', '2027-07-30', 'OCUPADO', 'PERMISO_C_E');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (27, '1018-DXF', 'DAF', 'XF 530 + Semirremolque', 'Naranja Aurora', 215000, 530, 2019, 'DIESEL', 'AUTOMATICO', '2026-02-25', '2028-02-25', 'DISPONIBLE', 'PERMISO_C_E');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (28, '1019-STR', 'Setra', 'S 515 HD + Remolque', 'Plata Diamante', 230000, 456, 2019, 'DIESEL', 'AUTOMATICO', '2026-03-12', '2027-03-12', 'OCUPADO', 'PERMISO_D_E');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (29, '1020-VLV', 'Volvo', '9700 HD + Remolque', 'Azul Marino', 245000, 460, 2018, 'DIESEL', 'AUTOMATICO', '2026-04-08', '2028-04-08', 'DISPONIBLE', 'PERMISO_D_E');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (30, '1021-JDM', 'John Deere', '6120M con Aperos', 'Verde / Amarillo', 5600, 120, 2021, 'DIESEL', 'MANUAL', '2026-05-02', '2027-05-02', 'OCUPADO', 'PERMISO_LVA');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (31, '1022-NHT', 'New Holland', 'T6.160 con Aperos', 'Azul Eléctrico', 4800, 135, 2022, 'DIESEL', 'MANUAL', '2026-04-15', '2028-04-15', 'DISPONIBLE', 'PERMISO_LVA');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (32, '4411-KZX', 'SEAT', 'León 1.5 TSI', 'Rojo Emoción', 89000, 130, 2019, 'GASOLINA', 'MANUAL', '2025-11-10', '2027-11-10', 'INACTIVO', 'PERMISO_B');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (33, '5522-MBK', 'BMW', 'G 310 R', 'Blanco Polar', 22000, 34, 2020, 'GASOLINA', 'MANUAL', '2025-10-15', '2027-10-15', 'INACTIVO', 'PERMISO_A2');
INSERT INTO vehiculo (id, matricula, marca, modelo, color, km, cv, anio, tipo_combustible, caja_cambios, fecha_ultima_revision, fecha_proxima_revision, estado, tipo_permiso)
VALUES (34, '6633-NRT', 'Iveco', 'Eurocargo 120E', 'Blanco Ártico', 210000, 220, 2018, 'DIESEL', 'MANUAL', '2025-09-20', '2028-09-20', 'INACTIVO', 'PERMISO_C');

-- ------------------------------------------------------------------------------
-- 2. USUARIOS BASE (persona) - 99 Personas (1 Admin, 30 Profesores, 68 Alumnos)
-- ------------------------------------------------------------------------------
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (1, 'Antonio Abad', 'Hernández Gálvez', '09123456T', '1985-04-12', 'admin', 'admin@autoescuela.es', '$2a$10$umISk5UJjoOGrFmQXaaANernpH1OxhBA/NHx.TpkKBCNfau0s4uUi', '600111222', 'Calle Gran Vía 28, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (100, 'Laura', 'Sánchez Romero', '23456789D', '1988-09-23', 'profesor1', 'laura.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600222333', 'Avenida de América 15, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (101, 'Manuel', 'Navarro Torres', '34567890V', '1990-11-05', 'profesor2', 'manuel.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600333444', 'Calle Alcalá 120, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (102, 'Carlos', 'Ruiz Martínez', '78901234X', '1986-07-19', 'profesor3', 'carlos.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600777888', 'Paseo de la Castellana 80, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (103, 'Elena', 'Moreno Díaz', '89012345E', '1992-03-30', 'profesor4', 'elena.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600888999', 'Calle Princesa 45, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (104, 'Javier', 'Navarro Gil', '21098765Z', '1984-12-14', 'profesor5', 'javier.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600999000', 'Calle Fuencarral 60, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (105, 'Benjamín', 'Soto Morales', '21098799W', '1989-05-18', 'profesor6', 'benjamin.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600999111', 'Calle Bravo Murillo 210, Madrid', 'INACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (106, 'Andrés', 'Morales Romero', '31098765M', '1987-08-22', 'profesor7', 'andres.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600999222', 'Avenida de Bruselas 34, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (107, 'Marta', 'Romero Castro', '41098765L', '1991-04-16', 'profesor8', 'marta.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600999333', 'Calle de Atocha 78, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (108, 'Raquel', 'Ortiz Vega', '51098765X', '1993-10-09', 'profesor9', 'raquel.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600999444', 'Calle de O''Donnell 52, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (109, 'Joaquín', 'Castillo Ramos', '61098765R', '1985-02-28', 'profesor10', 'joaquin.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600999555', 'Calle de Serrano 114, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (110, 'Vicente', 'Gil Peña', '71098765S', '1989-11-12', 'profesor11', 'vicente.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600999666', 'Calle de Goya 88, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (111, 'Beatriz', 'Cruz Méndez', '81098765Y', '1988-06-03', 'profesor12', 'beatriz.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600999777', 'Paseo de Santa María de la Cabeza 42, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (112, 'Tomás', 'Vidal Marín', '91098765C', '1983-09-17', 'profesor13', 'tomas.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600999888', 'Calle de Santa Engracia 95, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (113, 'Alberto', 'Cano Flores', '11098765T', '1986-01-25', 'profesor14', 'alberto.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600999999', 'Calle de Raimundo Fernández Villaverde 30, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (114, 'Fernando', 'Méndez Blanco', '31987654J', '1987-04-14', 'profesor15', 'fernando.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000114', 'Calle Velázquez 110, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (115, 'Sonia', 'Iglesias Pastor', '42987654X', '1991-11-20', 'profesor16', 'sonia.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000115', 'Calle Princesa 88, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (116, 'Guillermo', 'Pardo Navarro', '12098765A', '1988-02-14', 'profesor17', 'guillermo.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000116', 'Calle Montera 15, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (117, 'Patricia', 'Herrera Campos', '22098765B', '1990-07-22', 'profesor18', 'patricia.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000117', 'Calle Carretas 8, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (118, 'Mario', 'Vázquez Lozano', '32098765C', '1986-11-09', 'profesor19', 'mario.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000118', 'Calle Mayor 70, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (119, 'Irene', 'Soler Fuentes', '42098765D', '1992-04-18', 'profesor20', 'irene.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000119', 'Plaza de España 4, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (120, 'Roberto', 'Paredes Cano', '52098765E', '1987-10-30', 'profesor21', 'roberto.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000120', 'Avenida de Burgos 22, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (121, 'Silvia', 'Carrasco Rivas', '62098765F', '1991-03-12', 'profesor22', 'silvia.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000121', 'Calle Arenal 18, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (122, 'Álvaro', 'Garrido Peña', '72098765G', '1985-08-25', 'profesor23', 'alvaro.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000122', 'Calle Postas 11, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (123, 'Lucía', 'Esteban Marín', '82098765H', '1989-12-05', 'profesor24', 'lucia.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000123', 'Paseo de Recoletos 15, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (124, 'Daniel', 'Mora Beltrán', '92098765J', '1984-06-17', 'profesor25', 'daniel.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000124', 'Calle Atocha 105, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (125, 'Rosa', 'Calvo Delgado', '13098765K', '1988-09-14', 'profesor26', 'rosa.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000125', 'Calle Huertas 34, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (126, 'Hugo', 'Ibáñez Crespo', '23098765L', '1986-01-29', 'profesor27', 'hugo.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000126', 'Calle Barquillo 30, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (127, 'Marta', 'Benítez Luque', '33098765M', '1993-05-20', 'profesor28', 'martab.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000127', 'Calle Conde Duque 12, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (128, 'Julián', 'Pascual Roldán', '43098765N', '1983-03-10', 'profesor29', 'julian.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000128', 'Calle Princesa 20, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (129, 'Lorena', 'Pastor Cuesta', '53098765P', '1985-11-28', 'profesor30', 'lorena.profesor@autoescuela.es', '$2a$10$dU1qgUT4dh2MvH1vQUePXOpw2syxYe.3DkjMMoHKk1Dm8vU18JenO', '600000129', 'Calle San Bernardo 90, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (200, 'Jose', 'López Martín', '45678901G', '2000-01-15', 'alumno1', 'jose.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '600444555', 'Calle Toledo 10, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (201, 'David', 'Ruiz García', '56789012B', '2001-05-20', 'alumno2', 'david.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '600555666', 'Avenida de la Albufera 45, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (202, 'Carmen', 'Vega Hernández', '67890123B', '1999-11-10', 'alumno3', 'carmen.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '600666777', 'Calle Cartagena 78, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (203, 'Alejandro', 'Blanco Ramos', '78901245K', '2002-03-25', 'alumno4', 'alejandro.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '601111222', 'Calle Sagasta 12, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (204, 'Lucía', 'Fernández Santos', '89012356X', '2001-08-14', 'alumno5', 'lucia.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '601222333', 'Calle Mayor 55, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (205, 'Hugo', 'Medina Cruz', '90123467Z', '2000-12-03', 'alumno6', 'hugo.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '601333444', 'Calle Hermosilla 89, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (206, 'Daniel', 'Marín Gil', '10234578S', '1998-06-18', 'alumno7', 'daniel.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '601444555', 'Calle Bailén 4, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (207, 'María', 'Castillo Ortiz', '12345679S', '2003-02-28', 'alumno8', 'maria.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '601555666', 'Calle Preciados 22, Madrid', 'INACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (208, 'Marta', 'Sánchez Gómez', '23456781R', '1997-09-09', 'alumno9', 'marta.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '601666777', 'Calle Goya 33, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (209, 'Sergio', 'Morales Soto', '34567812P', '2001-04-17', 'alumno10', 'sergio.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '601777888', 'Calle Serrano 102, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (210, 'Irene', 'Ortiz Navarro', '45678123P', '2002-10-31', 'alumno11', 'irene.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '601888999', 'Calle Velázquez 67, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (211, 'Álvaro', 'Delgado Ruiz', '56781234F', '2000-07-22', 'alumno12', 'alvaro.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '601999000', 'Paseo del Prado 18, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (212, 'Sofía', 'Castro Moreno', '67812345L', '2003-01-08', 'alumno13', 'sofia.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '602111222', 'Calle Zurbarán 9, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (213, 'Mario', 'Rubio Torres', '78123456T', '1999-05-15', 'alumno14', 'mario.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '602222333', 'Calle Hortaleza 30, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (214, 'Alba', 'Marín Romero', '81234567Q', '2002-11-20', 'alumno15', 'alba.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '602333444', 'Calle Claudio Coello 41, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (215, 'Javier', 'Sanz Díaz', '91234568P', '2001-09-03', 'alumno16', 'javier.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '602444555', 'Calle Lagasca 75, Madrid', 'INACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (216, 'Paula', 'Iglesias Vega', '13245768E', '2000-08-11', 'alumno17', 'paula.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '602555666', 'Calle Barquillo 14, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (217, 'Marcos', 'Medina Serrano', '24356879V', '1998-12-29', 'alumno18', 'marcos.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '602666777', 'Calle Génova 25, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (218, 'Elena', 'Cortés Ramos', '35467980W', '2003-04-05', 'alumno19', 'elena.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '602777888', 'Calle Argensola 6, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (219, 'Adrián', 'Vega Gil', '46578091D', '2001-06-16', 'alumno20', 'adrian.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '602888999', 'Calle Fernando VI 19, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (220, 'Jorge', 'Romero Blanco', '57689102L', '1999-10-24', 'alumno21', 'jorge.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '602999000', 'Calle Almagro 31, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (221, 'Clara', 'Gil Santos', '68790213L', '2002-02-14', 'alumno22', 'clara.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '603111222', 'Calle Zurbano 50, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (222, 'Gonzalo', 'Serrano Cruz', '79801324H', '2000-03-19', 'alumno23', 'gonzalo.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '603222333', 'Calle Caracas 8, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (223, 'Valeria', 'Blanco Marín', '80912435E', '2003-07-07', 'alumno24', 'valeria.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '603333444', 'Calle Fortuny 21, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (224, 'Raúl', 'Molina Ortiz', '91023546B', '2001-11-02', 'alumno25', 'raul.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '603444555', 'Calle Eduardo Dato 15, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (225, 'Natalia', 'Ramos Delgado', '10923847C', '2002-05-18', 'alumno26', 'natalia.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '603555666', 'Calle Rubén Darío 4, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (226, 'Lucas', 'Navarro Castro', '21834950S', '2000-09-27', 'alumno27', 'lucas.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '603666777', 'Paseo de Eduardo Dato 2, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (227, 'Claudia', 'Vega Rubio', '32745061F', '2003-08-30', 'alumno28', 'claudia.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '603777888', 'Calle Miguel Ángel 11, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (228, 'Ismael', 'Cano Gómez', '51234501T', '2001-03-12', 'alumno29', 'ismael.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '604111222', 'Calle Alberto Aguilera 12, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (229, 'Lorena', 'Gil Moreno', '51234502R', '2002-07-25', 'alumno30', 'lorena.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '604222333', 'Calle San Bernardo 45, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (230, 'Guillermo', 'Díaz Castro', '51234503W', '2000-11-14', 'alumno31', 'guillermo.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '604333444', 'Calle Gaztambide 18, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (231, 'Inés', 'Romero Santos', '51234504A', '2003-09-05', 'alumno32', 'ines.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '604444555', 'Calle Cea Bermúdez 24, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (232, 'Rubén', 'Serrano Ruiz', '51234505G', '2001-05-19', 'alumno33', 'ruben.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '604555666', 'Calle Hilarión Eslava 30, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (233, 'Silvia', 'Molina Torres', '51234506M', '2002-12-08', 'alumno34', 'silvia.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '604666777', 'Calle Donoso Cortés 50, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (234, 'Héctor', 'Ruiz Ortiz', '51234507Y', '2000-04-22', 'alumno35', 'hector.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '604777888', 'Calle Vallehermoso 15, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (235, 'Marcos', 'Ortiz Vega', '51234508F', '1999-08-30', 'alumno36', 'marcoso.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '604888999', 'Calle Galileo 60, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (236, 'Andrea', 'Vidal Blanco', '51234509P', '2001-10-11', 'alumno37', 'andrea.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '604999000', 'Calle Fernando el Católico 40, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (237, 'Samuel', 'Ramos Navarro', '51234510D', '2002-02-17', 'alumno38', 'samuel.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '605111222', 'Calle Guzmán el Bueno 70, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (238, 'Natalia', 'Castro Delgado', '51234511X', '2003-06-21', 'alumno39', 'nataliac.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '605222333', 'Calle Bravo Murillo 90, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (239, 'Víctor', 'Navarro Cruz', '51234512B', '2000-01-29', 'alumno40', 'victor.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '605333444', 'Calle Ríos Rosas 33, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (240, 'Carla', 'Vega Rubio', '51234513N', '2001-09-14', 'alumno41', 'carla.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '605444555', 'Calle José Abascal 12, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (241, 'Jaime', 'Morales Soto', '51234514J', '1998-11-03', 'alumno42', 'jaime.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '605555666', 'Calle Ponzano 25, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (242, 'Miriam', 'Sanz Gil', '51234515Z', '2002-04-16', 'alumno43', 'miriam.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '605666777', 'Calle Alonso Cano 48, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (243, 'Óscar', 'Delgado Marín', '51234516S', '2000-07-09', 'alumno44', 'oscar.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '605777888', 'Calle Santa Engracia 120, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (244, 'Cristina', 'Cruz Medina', '51234517Q', '2001-12-23', 'alumno45', 'cristina.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '605888999', 'Calle Luchana 16, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (245, 'Pablo', 'Benítez Ramos', '51234518V', '1999-03-31', 'alumno46', 'pablo.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '605999000', 'Calle Covarrubias 8, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (246, 'Nuria', 'Rubio Iglesias', '51234519H', '2003-01-19', 'alumno47', 'nuria.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '606111222', 'Calle Alburquerque 5, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (247, 'Emilio', 'Castillo Romero', '51234520L', '2000-08-04', 'alumno48', 'emilio.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '606222333', 'Calle Carranza 22, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (248, 'Rocío', 'Medina López', '51234521C', '2002-05-27', 'alumno49', 'rocio.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '606333444', 'Calle Fuencarral 140, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (249, 'Ángel', 'Flores Sánchez', '51234522K', '2001-11-15', 'alumno50', 'angel.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '606444555', 'Calle San Mateo 10, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (250, 'Celia', 'Gallego Ruiz', '51234523E', '2003-03-02', 'alumno51', 'celia.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '606555666', 'Calle Pelayo 35, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (251, 'Tomás', 'Herrera Gil', '51234524T', '1999-06-18', 'alumno52', 'tomas.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '606666777', 'Calle Hortaleza 80, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (252, 'Marina', 'Lozano Santos', '51234525R', '2002-10-10', 'alumno53', 'marina.alumno@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '606777888', 'Calle Chueca 14, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (253, 'Marcos', 'Prieto Ramos', '51234526A', '2001-04-12', 'alumno54', 'marcos.prieto@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '607111222', 'Calle Serrano 150, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (254, 'Irene', 'Garrido Peña', '51234527B', '2002-08-23', 'alumno55', 'irene.garrido@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '607222333', 'Calle Velázquez 90, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (255, 'Adrián', 'Cabrera Soler', '51234528C', '2000-12-05', 'alumno56', 'adrian.cabrera@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '607333444', 'Calle Claudio Coello 75, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (256, 'Paula', 'Castillo Mora', '51234529D', '2003-02-18', 'alumno57', 'paula.castillo@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '607444555', 'Calle Lagasca 40, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (257, 'Hugo', 'Pardo Rivas', '51234530E', '2001-09-30', 'alumno58', 'hugo.pardo@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '607555666', 'Calle Goya 60, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (258, 'Elena', 'Bermejo Cruz', '51234531F', '2002-06-14', 'alumno59', 'elena.bermejo@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '607666777', 'Calle Hermosilla 30, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (259, 'Diego', 'Santos León', '51234532G', '2000-03-25', 'alumno60', 'diego.santos@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '607777888', 'Calle Ayala 85, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (260, 'Sara', 'Marín Esteban', '51234533H', '2003-11-11', 'alumno61', 'sara.marin@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '607888999', 'Calle Jorge Juan 50, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (261, 'Álvaro', 'Fuentes Moya', '51234534J', '1999-07-08', 'alumno62', 'alvaro.fuentes@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '607999000', 'Calle O Donnell 20, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (262, 'Marta', 'Herrero Solís', '51234535K', '2002-01-27', 'alumno63', 'marta.herrero@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '608111222', 'Calle Narváez 35, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (263, 'Javier', 'Paredes Cano', '51234536L', '2001-10-09', 'alumno64', 'javier.paredes@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '608222333', 'Calle Ibiza 18, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (264, 'Lucía', 'Giménez Vidal', '51234537M', '2003-05-16', 'alumno65', 'lucia.gimenez@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '608333444', 'Calle Menorca 12, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (265, 'Manuel', 'Reyes Cortés', '51234538N', '2000-08-03', 'alumno66', 'manuel.reyes@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '608444555', 'Calle Sainz de Baranda 44, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (266, 'Alba', 'Méndez Gallego', '51234539P', '2002-03-21', 'alumno67', 'alba.mendez@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '608555666', 'Calle Doctor Esquerdo 70, Madrid', 'ACTIVO');
INSERT INTO persona (id, nombre, apellidos, dni, fecha_nacimiento, nombre_usuario, correo, password, telefono, direccion, estado)
VALUES (267, 'Daniel', 'Lara Montero', '51234540Q', '2001-12-19', 'alumno68', 'daniel.lara@autoescuela.es', '$2a$10$1SxY9AYXM.YDmvQHsyCDPe1mZhdgqhe9o5HeLF9cJOk3Bch4ecAjS', '608666777', 'Calle O Donnell 45, Madrid', 'ACTIVO');

-- ------------------------------------------------------------------------------
-- 3. ESPECIALIZACIONES DE USUARIO (administrador, profesor, profesor_permisos, alumno)
-- ------------------------------------------------------------------------------
-- 3.1 Administrador
INSERT INTO administrador (id) VALUES (1);

-- 3.2 Profesores (100..115) y Asignación 1-a-1 de Vehículos
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (100, 'MATINAL', '2022-01-15', 1);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (101, 'MATINAL', '2022-03-01', 12);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (102, 'TARDE', '2021-09-10', 3);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (103, 'TARDE', '2023-02-01', 9);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (104, 'MATINAL', '2020-05-15', 14);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (105, 'MATINAL', '2022-08-01', NULL);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (106, 'MATINAL', '2023-01-10', 16);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (107, 'TARDE', '2023-03-15', 18);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (108, 'MATINAL', '2023-05-02', 20);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (109, 'TARDE', '2022-11-20', 22);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (110, 'MATINAL', '2023-06-01', 24);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (111, 'TARDE', '2022-04-18', 26);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (112, 'MATINAL', '2021-10-05', 28);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (113, 'TARDE', '2023-07-12', 30);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (114, 'MATINAL', '2023-09-01', 2);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (115, 'TARDE', '2023-09-15', 4);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (116, 'MATINAL', '2023-10-01', NULL);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (117, 'TARDE', '2023-10-05', NULL);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (118, 'MATINAL', '2023-10-10', NULL);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (119, 'TARDE', '2023-10-15', NULL);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (120, 'MATINAL', '2023-10-20', NULL);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (121, 'TARDE', '2023-11-01', NULL);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (122, 'MATINAL', '2023-11-05', NULL);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (123, 'TARDE', '2023-11-10', NULL);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (124, 'MATINAL', '2023-11-15', NULL);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (125, 'TARDE', '2023-11-20', NULL);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (126, 'MATINAL', '2023-12-01', NULL);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (127, 'TARDE', '2023-12-05', NULL);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (128, 'MATINAL', '2023-12-10', NULL);
INSERT INTO profesor (id, turno, fecha_contratacion, vehiculo_id) VALUES (129, 'TARDE', '2023-12-15', NULL);

-- Permisos asignados a Profesores (profesor_permisos) - 81 Asignaciones
-- Profesores familia Carnet B (B, B1, B+E): 100, 102, 106, 110, 114, 115
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (100, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (100, 'PERMISO_B1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (100, 'PERMISO_B_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (102, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (102, 'PERMISO_B1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (102, 'PERMISO_B_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (106, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (106, 'PERMISO_B1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (106, 'PERMISO_B_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (110, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (110, 'PERMISO_B1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (110, 'PERMISO_B_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (114, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (114, 'PERMISO_B1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (114, 'PERMISO_B_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (115, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (115, 'PERMISO_B1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (115, 'PERMISO_B_E');

-- Profesores familia Carnet A (AM, A1, A2, A): 103, 107, 108, 109
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (103, 'PERMISO_AM');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (103, 'PERMISO_A1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (103, 'PERMISO_A2');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (103, 'PERMISO_A');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (107, 'PERMISO_AM');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (107, 'PERMISO_A1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (107, 'PERMISO_A2');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (107, 'PERMISO_A');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (108, 'PERMISO_AM');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (108, 'PERMISO_A1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (108, 'PERMISO_A2');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (108, 'PERMISO_A');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (109, 'PERMISO_AM');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (109, 'PERMISO_A1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (109, 'PERMISO_A2');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (109, 'PERMISO_A');

-- Profesores familia Carnet C (C, C+E): 101, 111
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (101, 'PERMISO_C');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (101, 'PERMISO_C_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (111, 'PERMISO_C');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (111, 'PERMISO_C_E');

-- Profesores familia Carnet D (D, D+E): 104, 112
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (104, 'PERMISO_D');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (104, 'PERMISO_D_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (112, 'PERMISO_D');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (112, 'PERMISO_D_E');

-- Profesor Carnet LVA: 113
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (113, 'PERMISO_LVA');

-- Profesor Inactivo: 105
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (105, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (105, 'PERMISO_AM');

-- Profesores sin vehÃ­culo asignado (1 por cada tipo de carnet): 116..127
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (116, 'PERMISO_AM');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (117, 'PERMISO_A1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (118, 'PERMISO_A2');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (119, 'PERMISO_A');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (120, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (121, 'PERMISO_B_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (122, 'PERMISO_B1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (123, 'PERMISO_C');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (124, 'PERMISO_C_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (125, 'PERMISO_D');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (126, 'PERMISO_D_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (127, 'PERMISO_LVA');

-- Profesores con todos los tipos de carnets: 128, 129
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (128, 'PERMISO_AM');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (128, 'PERMISO_A1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (128, 'PERMISO_A2');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (128, 'PERMISO_A');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (128, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (128, 'PERMISO_B_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (128, 'PERMISO_B1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (128, 'PERMISO_C');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (128, 'PERMISO_C_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (128, 'PERMISO_D');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (128, 'PERMISO_D_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (128, 'PERMISO_LVA');

INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (129, 'PERMISO_AM');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (129, 'PERMISO_A1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (129, 'PERMISO_A2');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (129, 'PERMISO_A');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (129, 'PERMISO_B');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (129, 'PERMISO_B_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (129, 'PERMISO_B1');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (129, 'PERMISO_C');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (129, 'PERMISO_C_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (129, 'PERMISO_D');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (129, 'PERMISO_D_E');
INSERT INTO profesor_permisos (profesor_id, tipo_carnet) VALUES (129, 'PERMISO_LVA');

-- 3.3 Alumnos (200..267)
-- Vinculación estricta: un alumno SOLO tiene profesor si dispone de matrícula activa compatible.
-- Alumnos sin profesor asignado (expulsados o graduados con todos los exámenes aprobados)
INSERT INTO alumno (id, profesor_id) VALUES (207, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (208, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (215, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (219, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (220, NULL);

-- Alumnos activos Permiso B (16 alumnos distribuidos en 4 profesores habilitados)
INSERT INTO alumno (id, profesor_id) VALUES (200, 100);
INSERT INTO alumno (id, profesor_id) VALUES (201, 100);
INSERT INTO alumno (id, profesor_id) VALUES (202, 100);
INSERT INTO alumno (id, profesor_id) VALUES (203, 100);
INSERT INTO alumno (id, profesor_id) VALUES (204, 100);
INSERT INTO alumno (id, profesor_id) VALUES (206, 100);
INSERT INTO alumno (id, profesor_id) VALUES (214, 100);
INSERT INTO alumno (id, profesor_id) VALUES (226, 100);

INSERT INTO alumno (id, profesor_id) VALUES (209, 102);
INSERT INTO alumno (id, profesor_id) VALUES (212, 102);
INSERT INTO alumno (id, profesor_id) VALUES (213, 102);
INSERT INTO alumno (id, profesor_id) VALUES (227, 102);

INSERT INTO alumno (id, profesor_id) VALUES (210, 114);
INSERT INTO alumno (id, profesor_id) VALUES (211, NULL);

INSERT INTO alumno (id, profesor_id) VALUES (222, 115);
INSERT INTO alumno (id, profesor_id) VALUES (224, NULL);

-- Alumnos activos otros permisos (mínimo 3 alumnos por carnet con su respectivo profesor)
-- Permiso A2 (Profesor 103)
INSERT INTO alumno (id, profesor_id) VALUES (205, 103);
INSERT INTO alumno (id, profesor_id) VALUES (228, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (229, 103);

-- Permiso AM (Profesor 107)
INSERT INTO alumno (id, profesor_id) VALUES (225, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (230, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (231, 107);

-- Permiso A1 (Profesor 108)
INSERT INTO alumno (id, profesor_id) VALUES (232, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (233, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (234, 108);

-- Permiso A (Profesor 109)
INSERT INTO alumno (id, profesor_id) VALUES (235, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (236, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (237, 109);

-- Permiso B1 (Profesor 110)
INSERT INTO alumno (id, profesor_id) VALUES (238, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (239, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (240, 110);

-- Permiso B+E (Profesor 106)

INSERT INTO alumno (id, profesor_id) VALUES (241, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (242, 106);

-- Permiso C (Profesor 101)
INSERT INTO alumno (id, profesor_id) VALUES (216, 101);
INSERT INTO alumno (id, profesor_id) VALUES (217, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (243, NULL);

-- Permiso C+E (Profesor 111)
INSERT INTO alumno (id, profesor_id) VALUES (244, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (245, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (246, 111);

-- Permiso D (Profesor 104)
INSERT INTO alumno (id, profesor_id) VALUES (218, 104);
INSERT INTO alumno (id, profesor_id) VALUES (221, 104);
INSERT INTO alumno (id, profesor_id) VALUES (223, NULL);

-- Permiso D+E (Profesor 112)
INSERT INTO alumno (id, profesor_id) VALUES (247, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (248, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (249, 112);

-- Permiso LVA (Profesor 113)
INSERT INTO alumno (id, profesor_id) VALUES (250, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (251, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (252, 113);

-- Alumnos en formación Teórico-Práctica sin profesor asignado (pendientes de examen, sin solicitud o con teórico aprobado)
INSERT INTO alumno (id, profesor_id) VALUES (253, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (254, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (255, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (256, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (257, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (258, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (259, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (260, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (261, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (262, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (263, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (264, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (265, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (266, NULL);
INSERT INTO alumno (id, profesor_id) VALUES (267, NULL);

-- ------------------------------------------------------------------------------
-- 4. MATRÍCULAS (matricula) - 69 Matrículas (id = 1 .. 69)
-- ------------------------------------------------------------------------------
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (1, 200, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-06-01', 3, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (2, 201, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-07-01', 3, 1, 1, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (3, 202, 'PERMISO_B', 'PRACTICA', 'NUEVA', '2026-06-15', 3, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (4, 203, 'PERMISO_B', 'PRACTICA', 'NUEVA', '2026-07-10', 4, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (5, 204, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-07-05', 2, 1, 1, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (6, 205, 'PERMISO_A2', 'TEORICO_PRACTICA', 'NUEVA', '2026-08-01', 2, 1, 1, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (7, 206, 'PERMISO_B', 'TEORICO_PRACTICA', 'RENOVACION', '2026-08-01', 2, 0, 2, 500, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (8, 207, 'PERMISO_B', 'PRACTICA', 'RENOVACION', '2026-05-10', 0, 0, 2, 500, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (9, 208, 'PERMISO_B', 'TEORICO_PRACTICA', 'RENOVACION', '2026-05-20', 0, 1, 1, 500, FALSE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (10, 208, 'PERMISO_A2', 'TEORICO_PRACTICA', 'NUEVA', '2026-01-15', 0, 2, 0, 250, FALSE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (11, 209, 'PERMISO_B', 'PRACTICA', 'NUEVA', '2026-07-01', 2, 1, 1, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (12, 210, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-06-15', 0, 0, 2, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (13, 211, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-06-20', 0, 1, 1, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (14, 212, 'PERMISO_B', 'PRACTICA', 'NUEVA', '2026-07-01', 2, 1, 1, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (15, 215, 'PERMISO_B', 'TEORICO_PRACTICA', 'RENOVACION', '2026-04-10', 0, 0, 2, 500, FALSE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (16, 213, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-06-25', 2, 0, 2, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (17, 214, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-07-15', 4, 1, 1, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (18, 216, 'PERMISO_C', 'PRACTICA', 'NUEVA', '2026-08-01', 2, 2, 0, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (19, 217, 'PERMISO_C', 'TEORICO_PRACTICA', 'NUEVA', '2026-08-05', 0, 1, 1, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (20, 218, 'PERMISO_D', 'PRACTICA', 'NUEVA', '2026-08-10', 2, 2, 0, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (21, 253, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-10-05', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (22, 221, 'PERMISO_D', 'PRACTICA', 'NUEVA', '2026-08-01', 2, 2, 0, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (23, 222, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-08-01', 0, 1, 1, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (24, 223, 'PERMISO_D', 'TEORICO_PRACTICA', 'NUEVA', '2026-08-05', 0, 2, 0, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (25, 224, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-10-01', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (26, 225, 'PERMISO_AM', 'TEORICO_PRACTICA', 'NUEVA', '2026-10-02', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (27, 220, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-05-15', 0, 0, 2, 250, FALSE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (28, 226, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-08-15', 2, 0, 2, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (29, 227, 'PERMISO_B', 'PRACTICA', 'NUEVA', '2026-08-20', 2, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (30, 219, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-08-01', 0, 0, 2, 250, FALSE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (31, 228, 'PERMISO_A2', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-01', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (32, 229, 'PERMISO_A2', 'PRACTICA', 'NUEVA', '2026-09-05', 2, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (33, 230, 'PERMISO_AM', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-02', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (34, 231, 'PERMISO_AM', 'PRACTICA', 'NUEVA', '2026-09-06', 2, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (35, 232, 'PERMISO_A1', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-03', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (36, 233, 'PERMISO_A1', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-04', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (37, 234, 'PERMISO_A1', 'PRACTICA', 'NUEVA', '2026-09-07', 2, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (38, 235, 'PERMISO_A', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-01', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (39, 236, 'PERMISO_A', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-02', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (40, 237, 'PERMISO_A', 'PRACTICA', 'NUEVA', '2026-09-08', 2, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (41, 238, 'PERMISO_B1', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-03', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (42, 239, 'PERMISO_B1', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-04', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (43, 240, 'PERMISO_B1', 'PRACTICA', 'NUEVA', '2026-09-09', 2, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (44, 241, 'PERMISO_B_E', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-05', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (45, 242, 'PERMISO_B_E', 'PRACTICA', 'NUEVA', '2026-09-10', 2, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (46, 243, 'PERMISO_C', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-01', 0, 2, 0, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (47, 244, 'PERMISO_C_E', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-02', 0, 2, 0, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (48, 245, 'PERMISO_C_E', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-03', 0, 2, 0, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (49, 246, 'PERMISO_C_E', 'PRACTICA', 'NUEVA', '2026-09-11', 2, 2, 0, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (50, 247, 'PERMISO_D_E', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-04', 0, 2, 0, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (51, 248, 'PERMISO_D_E', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-05', 0, 2, 0, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (52, 249, 'PERMISO_D_E', 'PRACTICA', 'NUEVA', '2026-09-12', 2, 2, 0, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (53, 250, 'PERMISO_LVA', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-01', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (54, 251, 'PERMISO_LVA', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-02', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (55, 252, 'PERMISO_LVA', 'PRACTICA', 'NUEVA', '2026-09-15', 2, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (56, 254, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-10-06', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (57, 255, 'PERMISO_A2', 'TEORICO_PRACTICA', 'NUEVA', '2026-10-07', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (58, 256, 'PERMISO_C', 'TEORICO_PRACTICA', 'NUEVA', '2026-10-08', 0, 2, 0, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (59, 257, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-10-09', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (60, 258, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-10-10', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (61, 259, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-10-11', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (62, 260, 'PERMISO_A2', 'TEORICO_PRACTICA', 'NUEVA', '2026-10-12', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (63, 261, 'PERMISO_D', 'TEORICO_PRACTICA', 'NUEVA', '2026-10-13', 0, 2, 0, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (64, 262, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-10-14', 0, 2, 0, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (65, 263, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-01', 0, 1, 1, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (66, 264, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-02', 0, 1, 1, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (67, 265, 'PERMISO_A2', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-03', 0, 1, 1, 250, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (68, 266, 'PERMISO_C', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-04', 0, 1, 1, 450, TRUE);
INSERT INTO matricula (id, alumno_id, permiso_carnet, modalidad, tipo, fecha_matriculacion, saldo_clases, convocatorias, convocatorias_gastadas, precio, esta_activa)
VALUES (69, 267, 'PERMISO_B', 'TEORICO_PRACTICA', 'NUEVA', '2026-09-05', 0, 1, 1, 250, TRUE);

-- ------------------------------------------------------------------------------
-- 5. INCIDENCIAS MECÁNICAS (incidencia_vehiculo) - 10 Incidencias
-- ------------------------------------------------------------------------------
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (1, '2026-10-15 08:00:00', 'Avería en embrague bimasa y descebe de circuito de combustible.', 'EN_PROCESO', 2, 114);
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (2, '2026-09-28 14:00:00', 'Desgaste severo en pastillas y discos delanteros tras revisión preventiva.', 'PENDIENTE', 7, 102);
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (3, '2026-09-11 11:00:00', 'Sustitución de bombilla halógena de luz diurna delantera derecha.', 'RESUELTA', 1, 100);
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (4, '2026-09-14 16:30:00', 'Holgura leve en rótula de dirección izquierda detectada durante clase práctica.', 'RESUELTA', 2, 100);
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (5, '2026-09-16 17:15:00', 'Tensión y engrase de cadena de transmisión tras jornada en pista.', 'RESUELTA', 8, 103);
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (6, '2026-09-18 10:15:00', 'Calibración de sensor de presión de neumáticos y equilibrado de ruedas.', 'RESUELTA', 3, 102);
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (7, '2026-09-21 18:00:00', 'Recarga de gas refrigerante del sistema de climatización.', 'RESUELTA', 4, 100);
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (8, '2025-10-17 12:30:00', 'Deformación estructural de chasis tras caída lateral en circuito cerrado.', 'RESUELTA', 10, 103);
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (9, '2026-09-08 09:30:00', 'Pinchazo en la rueda delantera derecha.', 'PENDIENTE', 1, 100);
INSERT INTO incidencia_vehiculo (id, fecha_hora, descripcion, estado, vehiculo_id, profesor_id)
VALUES (10, '2026-09-02 08:00:00', 'Pérdida de presión en circuito neumático de frenos en eje tractor.', 'RESUELTA', 11, 101);

-- ------------------------------------------------------------------------------
-- 6. CLASES PRÁCTICAS (clase_practica) - Ajuste canónico de 45 minutos
-- ------------------------------------------------------------------------------
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (1, '2026-06-10 08:45:00', 45, 'Calle Goya 33', 41500, 41523, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 208, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (2, '2026-06-17 08:45:00', 45, 'Calle Goya 33', 41523, 41547, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 208, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (3, '2026-06-24 08:45:00', 45, 'Calle Goya 33', 41547, 41572, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 208, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (4, '2026-07-01 08:45:00', 45, 'Calle Goya 33', 41572, 41598, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 208, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (5, '2026-07-08 08:45:00', 45, 'Calle Goya 33', 41598, 41625, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 208, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (6, '2026-07-15 08:45:00', 45, 'Calle Goya 33', 41625, 41653, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 208, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (7, '2026-07-22 08:45:00', 45, 'Calle Goya 33', 41653, 41682, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 208, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (8, '2026-07-29 08:45:00', 45, 'Calle Goya 33', 41682, 41712, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 208, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (9, '2026-08-19 08:45:00', 45, 'Calle Goya 33', 41712, 41743, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 208, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (10, '2026-08-26 08:45:00', 45, 'Calle Goya 33', 41743, 41775, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 208, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (11, '2026-09-02 08:45:00', 45, 'Calle Goya 33', 41775, 41808, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 208, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (12, '2026-09-09 08:45:00', 45, 'Calle Goya 33', 41808, 41830, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 208, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (13, '2026-09-16 08:45:00', 45, 'Calle Goya 33', 41830, 41853, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 208, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (14, '2026-09-23 08:45:00', 45, 'Calle Goya 33', 41853, 41877, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 208, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (15, '2026-09-21 09:30:00', 45, 'Calle Almagro 31', 65000, 65025, 'Maniobras y enganche de remolque B+E.', 'RECIBIDA', FALSE, FALSE, 220, 106);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (16, '2026-09-23 09:30:00', 45, 'Calle Almagro 31', 65025, 65051, 'Maniobras y enganche de remolque B+E.', 'RECIBIDA', FALSE, FALSE, 220, 106);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (17, '2026-09-28 09:30:00', 45, 'Calle Almagro 31', 65051, 65078, 'Maniobras y enganche de remolque B+E.', 'RECIBIDA', FALSE, FALSE, 220, 106);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (18, '2026-09-30 09:30:00', 45, 'Calle Almagro 31', 65078, 65106, 'Maniobras y enganche de remolque B+E.', 'RECIBIDA', FALSE, FALSE, 220, 106);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (19, '2026-10-05 09:30:00', 45, 'Calle Almagro 31', 65106, 65135, 'Maniobras y enganche de remolque B+E.', 'RECIBIDA', FALSE, FALSE, 220, 106);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (20, '2026-10-07 09:30:00', 45, 'Calle Almagro 31', 65135, 65165, 'Maniobras y enganche de remolque B+E.', 'RECIBIDA', FALSE, FALSE, 220, 106);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (21, '2026-10-09 09:30:00', 45, 'Calle Almagro 31', 65165, 65196, 'Maniobras y enganche de remolque B+E.', 'RECIBIDA', FALSE, FALSE, 220, 106);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (22, '2026-10-14 09:30:00', 45, 'Calle Almagro 31', 65196, 65228, 'Maniobras y enganche de remolque B+E.', 'RECIBIDA', FALSE, FALSE, 220, 106);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (23, '2026-10-16 09:30:00', 45, 'Calle Almagro 31', 65228, 65261, 'Maniobras y enganche de remolque B+E.', 'RECIBIDA', FALSE, FALSE, 220, 106);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (24, '2026-10-21 09:30:00', 45, 'Calle Almagro 31', 65261, 65283, 'Maniobras y enganche de remolque B+E.', 'RECIBIDA', FALSE, FALSE, 220, 106);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (25, '2026-08-24 08:00:00', 45, 'Calle Toledo 10', 41877, 41900, 'Práctica de circulación urbana y glorietas.', 'RECIBIDA', FALSE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (26, '2026-08-27 08:00:00', 45, 'Calle Toledo 10', 41900, 41924, 'Práctica de circulación urbana y glorietas.', 'RECIBIDA', FALSE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (27, '2026-08-31 08:00:00', 45, 'Calle Toledo 10', 41924, 41949, 'Práctica de circulación urbana y glorietas.', 'RECIBIDA', FALSE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (28, '2026-09-03 08:00:00', 45, 'Calle Toledo 10', 41949, 41975, 'Práctica de circulación urbana y glorietas.', 'RECIBIDA', FALSE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (29, '2026-09-07 08:00:00', 45, 'Calle Toledo 10', 41975, 42002, 'Práctica de circulación urbana y glorietas.', 'RECIBIDA', FALSE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (30, '2026-09-10 08:00:00', 45, 'Calle Toledo 10', 42002, 42030, 'Práctica de circulación urbana y glorietas.', 'RECIBIDA', FALSE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (31, '2026-09-14 08:00:00', 45, 'Calle Toledo 10', 42030, 42059, 'Práctica de circulación urbana y glorietas.', 'RECIBIDA', FALSE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (32, '2026-09-17 08:00:00', 45, 'Calle Toledo 10', 42059, 42089, 'Práctica de circulación urbana y glorietas.', 'RECIBIDA', FALSE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (33, '2026-09-21 08:00:00', 45, 'Calle Toledo 10', 42089, 42120, 'Práctica de circulación urbana y glorietas.', 'RECIBIDA', FALSE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (34, '2026-09-28 08:00:00', 45, 'Calle Toledo 10', 42120, 42152, 'Práctica de circulación urbana y glorietas.', 'RECIBIDA', FALSE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (35, '2026-10-01 08:00:00', 45, 'Calle Toledo 10', 42152, 42185, 'Práctica de circulación urbana y glorietas.', 'RECIBIDA', FALSE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (36, '2026-10-05 08:00:00', 45, 'Calle Toledo 10', 42185, 42207, 'Práctica de circulación urbana y glorietas.', 'RECIBIDA', FALSE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (37, CURRENT_DATE + INTERVAL '1' DAY + TIME '08:00:00', 45, 'Calle Toledo 10', 42207, 42230, 'Práctica de perfeccionamiento.', 'PENDIENTE', FALSE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (38, CURRENT_DATE + INTERVAL '3' DAY + TIME '08:00:00', 45, 'Calle Toledo 10', 42230, 42254, 'Práctica de perfeccionamiento.', 'PENDIENTE', FALSE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (39, '2026-08-28 08:00:00', 45, 'Avenida de la Albufera 45', 42254, 42279, 'Circulación en vías interurbanas y adelantamientos.', 'RECIBIDA', FALSE, FALSE, 201, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (40, '2026-09-01 08:00:00', 45, 'Avenida de la Albufera 45', 42279, 42305, 'Circulación en vías interurbanas y adelantamientos.', 'RECIBIDA', FALSE, FALSE, 201, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (41, '2026-09-04 08:00:00', 45, 'Avenida de la Albufera 45', 42305, 42332, 'Circulación en vías interurbanas y adelantamientos.', 'RECIBIDA', FALSE, FALSE, 201, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (42, '2026-09-08 08:00:00', 45, 'Avenida de la Albufera 45', 42332, 42360, 'Circulación en vías interurbanas y adelantamientos.', 'RECIBIDA', FALSE, FALSE, 201, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (43, '2026-09-11 08:00:00', 45, 'Avenida de la Albufera 45', 42360, 42389, 'Circulación en vías interurbanas y adelantamientos.', 'RECIBIDA', FALSE, FALSE, 201, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (44, '2026-09-15 08:00:00', 45, 'Avenida de la Albufera 45', 42389, 42419, 'Circulación en vías interurbanas y adelantamientos.', 'RECIBIDA', FALSE, FALSE, 201, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (45, '2026-09-18 08:00:00', 45, 'Avenida de la Albufera 45', 42419, 42450, 'Circulación en vías interurbanas y adelantamientos.', 'RECIBIDA', FALSE, FALSE, 201, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (46, '2026-09-22 08:00:00', 45, 'Avenida de la Albufera 45', 42450, 42482, 'Circulación en vías interurbanas y adelantamientos.', 'RECIBIDA', FALSE, FALSE, 201, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (47, '2026-09-29 08:00:00', 45, 'Avenida de la Albufera 45', 42482, 42515, 'Circulación en vías interurbanas y adelantamientos.', 'RECIBIDA', FALSE, FALSE, 201, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (48, '2026-10-02 08:00:00', 45, 'Avenida de la Albufera 45', 42515, 42537, 'Circulación en vías interurbanas y adelantamientos.', 'RECIBIDA', FALSE, FALSE, 201, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (49, CURRENT_DATE + INTERVAL '2' DAY + TIME '08:00:00', 45, 'Avenida de la Albufera 45', 42537, 42560, 'Circulación en autovía.', 'PENDIENTE', FALSE, FALSE, 201, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (50, '2026-06-18 09:30:00', 45, 'Calle Cartagena 78', 42560, 42584, 'Manejo de mandos y estacionamiento en línea.', 'RECIBIDA', FALSE, FALSE, 202, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (51, '2026-06-25 09:30:00', 45, 'Calle Cartagena 78', 42584, 42609, 'Manejo de mandos y estacionamiento en línea.', 'RECIBIDA', FALSE, FALSE, 202, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (52, '2026-07-02 09:30:00', 45, 'Calle Cartagena 78', 42609, 42635, 'Manejo de mandos y estacionamiento en línea.', 'RECIBIDA', FALSE, FALSE, 202, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (53, '2026-07-09 09:30:00', 45, 'Calle Cartagena 78', 42635, 42662, 'Manejo de mandos y estacionamiento en línea.', 'RECIBIDA', FALSE, FALSE, 202, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (54, '2026-07-16 09:30:00', 45, 'Calle Cartagena 78', 42662, 42690, 'Manejo de mandos y estacionamiento en línea.', 'RECIBIDA', FALSE, FALSE, 202, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (55, '2026-07-23 09:30:00', 45, 'Calle Cartagena 78', 42690, 42719, 'Manejo de mandos y estacionamiento en línea.', 'RECIBIDA', FALSE, FALSE, 202, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (56, '2026-07-30 09:30:00', 45, 'Calle Cartagena 78', 42719, 42749, 'Manejo de mandos y estacionamiento en línea.', 'RECIBIDA', FALSE, FALSE, 202, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (57, '2026-08-20 09:30:00', 45, 'Calle Cartagena 78', 42749, 42780, 'Manejo de mandos y estacionamiento en línea.', 'RECIBIDA', FALSE, FALSE, 202, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (58, '2026-08-27 09:30:00', 45, 'Calle Cartagena 78', 42780, 42812, 'Manejo de mandos y estacionamiento en línea.', 'RECIBIDA', FALSE, FALSE, 202, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (59, '2026-09-03 09:30:00', 45, 'Calle Cartagena 78', 42812, 42845, 'Manejo de mandos y estacionamiento en línea.', 'RECIBIDA', FALSE, FALSE, 202, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (60, '2026-09-10 09:30:00', 45, 'Calle Cartagena 78', 42845, 42867, 'Manejo de mandos y estacionamiento en línea.', 'RECIBIDA', FALSE, FALSE, 202, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (61, '2026-09-17 09:30:00', 45, 'Calle Cartagena 78', 42867, 42890, 'Manejo de mandos y estacionamiento en línea.', 'RECIBIDA', FALSE, FALSE, 202, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (62, CURRENT_DATE + INTERVAL '1' DAY + TIME '09:30:00', 45, 'Calle Cartagena 78', 42890, 42914, 'Estacionamiento en batería y batería inversa.', 'PENDIENTE', FALSE, FALSE, 202, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (63, '2026-08-24 10:15:00', 45, 'Calle Sagasta 12', 42914, 42939, 'Incorporaciones y salidas de vías de alta ocupación.', 'RECIBIDA', FALSE, FALSE, 203, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (64, '2026-08-31 10:15:00', 45, 'Calle Sagasta 12', 42939, 42965, 'Incorporaciones y salidas de vías de alta ocupación.', 'RECIBIDA', FALSE, FALSE, 203, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (65, '2026-09-07 10:15:00', 45, 'Calle Sagasta 12', 42965, 42992, 'Incorporaciones y salidas de vías de alta ocupación.', 'RECIBIDA', FALSE, FALSE, 203, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (66, '2026-09-14 10:15:00', 45, 'Calle Sagasta 12', 42992, 43020, 'Incorporaciones y salidas de vías de alta ocupación.', 'RECIBIDA', FALSE, FALSE, 203, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (67, '2026-09-21 10:15:00', 45, 'Calle Sagasta 12', 43020, 43049, 'Incorporaciones y salidas de vías de alta ocupación.', 'RECIBIDA', FALSE, FALSE, 203, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (68, '2026-09-28 10:15:00', 45, 'Calle Sagasta 12', 43049, 43079, 'Incorporaciones y salidas de vías de alta ocupación.', 'RECIBIDA', FALSE, FALSE, 203, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (69, CURRENT_DATE + INTERVAL '1' DAY + TIME '10:15:00', 45, 'Calle Sagasta 12', 43079, 43110, 'Recorridos de examen DGT.', 'PENDIENTE', FALSE, FALSE, 203, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (70, '2026-09-01 10:15:00', 45, 'Calle Mayor 55', 43110, 43142, 'Observación en espejos retrovisores y ángulo muerto.', 'RECIBIDA', FALSE, FALSE, 204, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (71, '2026-09-08 10:15:00', 45, 'Calle Mayor 55', 43142, 43175, 'Observación en espejos retrovisores y ángulo muerto.', 'RECIBIDA', FALSE, FALSE, 204, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (72, '2026-09-15 10:15:00', 45, 'Calle Mayor 55', 43175, 43197, 'Observación en espejos retrovisores y ángulo muerto.', 'RECIBIDA', FALSE, FALSE, 204, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (73, '2026-09-22 10:15:00', 45, 'Calle Mayor 55', 43197, 43220, 'Observación en espejos retrovisores y ángulo muerto.', 'RECIBIDA', FALSE, FALSE, 204, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (74, '2026-09-29 10:15:00', 45, 'Calle Mayor 55', 43220, 43244, 'Observación en espejos retrovisores y ángulo muerto.', 'RECIBIDA', FALSE, FALSE, 204, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (75, CURRENT_DATE + INTERVAL '2' DAY + TIME '10:15:00', 45, 'Calle Mayor 55', 43244, 43269, 'Práctica de circulación urbana.', 'PENDIENTE', FALSE, FALSE, 204, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (76, '2026-08-28 10:15:00', 45, 'Calle Bailén 4', 43269, 43295, 'Técnicas de frenada progresiva y detención.', 'RECIBIDA', FALSE, FALSE, 206, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (77, '2026-09-04 10:15:00', 45, 'Calle Bailén 4', 43295, 43322, 'Técnicas de frenada progresiva y detención.', 'RECIBIDA', FALSE, FALSE, 206, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (78, '2026-09-11 10:15:00', 45, 'Calle Bailén 4', 43322, 43350, 'Técnicas de frenada progresiva y detención.', 'RECIBIDA', FALSE, FALSE, 206, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (79, '2026-09-18 10:15:00', 45, 'Calle Bailén 4', 43350, 43379, 'Técnicas de frenada progresiva y detención.', 'RECIBIDA', FALSE, FALSE, 206, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (80, '2026-10-02 10:15:00', 45, 'Calle Bailén 4', 43379, 43409, 'Técnicas de frenada progresiva y detención.', 'RECIBIDA', FALSE, FALSE, 206, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (81, CURRENT_DATE + INTERVAL '3' DAY + TIME '10:15:00', 45, 'Calle Bailén 4', 43409, 43440, 'Circulación en glorietas complejas.', 'PENDIENTE', FALSE, FALSE, 206, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (82, '2026-09-09 11:00:00', 45, 'Calle Claudio Coello 41', 43440, 43472, 'Circulación fluida en tráfico denso.', 'RECIBIDA', FALSE, FALSE, 214, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (83, '2026-09-16 11:00:00', 45, 'Calle Claudio Coello 41', 43472, 43505, 'Circulación fluida en tráfico denso.', 'RECIBIDA', FALSE, FALSE, 214, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (84, '2026-09-23 11:00:00', 45, 'Calle Claudio Coello 41', 43505, 43527, 'Circulación fluida en tráfico denso.', 'RECIBIDA', FALSE, FALSE, 214, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (85, '2026-09-30 11:00:00', 45, 'Calle Claudio Coello 41', 43527, 43550, 'Circulación fluida en tráfico denso.', 'RECIBIDA', FALSE, FALSE, 214, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (86, CURRENT_DATE + INTERVAL '2' DAY + TIME '11:00:00', 45, 'Calle Claudio Coello 41', 43550, 43574, 'Conducción autónoma con GPS.', 'PENDIENTE', FALSE, FALSE, 214, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (87, '2026-08-24 11:45:00', 45, 'Calle Fernando VI 19', 43574, 43599, 'Maniobras básicas y cambios de sentido.', 'RECIBIDA', FALSE, FALSE, 219, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (88, '2026-08-31 11:45:00', 45, 'Calle Fernando VI 19', 43599, 43625, 'Maniobras básicas y cambios de sentido.', 'RECIBIDA', FALSE, FALSE, 219, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (89, '2026-09-07 11:45:00', 45, 'Calle Fernando VI 19', 43625, 43652, 'Maniobras básicas y cambios de sentido.', 'RECIBIDA', FALSE, FALSE, 219, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (90, '2026-09-14 11:45:00', 45, 'Calle Fernando VI 19', 43652, 43680, 'Maniobras básicas y cambios de sentido.', 'RECIBIDA', FALSE, FALSE, 219, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (91, '2026-09-21 11:45:00', 45, 'Calle Fernando VI 19', 43680, 43709, 'Maniobras básicas y cambios de sentido.', 'RECIBIDA', FALSE, FALSE, 219, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (92, '2026-09-28 11:45:00', 45, 'Calle Fernando VI 19', 43709, 43739, 'Maniobras básicas y cambios de sentido.', 'RECIBIDA', FALSE, FALSE, 219, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (93, '2026-10-05 11:45:00', 45, 'Calle Fernando VI 19', 43739, 43770, 'Práctica previa a solicitud de examen.', 'RECIBIDA', FALSE, FALSE, 219, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (94, '2026-09-18 11:45:00', 45, 'Paseo de Eduardo Dato 2', 43770, 43802, 'Circulación por el centro urbano.', 'RECIBIDA', FALSE, FALSE, 226, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (95, '2026-09-25 11:45:00', 45, 'Paseo de Eduardo Dato 2', 43802, 43835, 'Circulación por el centro urbano.', 'RECIBIDA', FALSE, FALSE, 226, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (96, '2026-10-02 11:45:00', 45, 'Paseo de Eduardo Dato 2', 43835, 43857, 'Circulación por el centro urbano.', 'RECIBIDA', FALSE, FALSE, 226, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (97, CURRENT_DATE + INTERVAL '2' DAY + TIME '11:45:00', 45, 'Paseo de Eduardo Dato 2', 43857, 43880, 'Maniobras de parada y estacionamiento.', 'PENDIENTE', FALSE, FALSE, 226, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (98, '2026-06-09 15:00:00', 45, 'Calle Preciados 22', 29400, 29424, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 207, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (99, '2026-06-16 15:00:00', 45, 'Calle Preciados 22', 29424, 29449, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 207, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (100, '2026-06-23 15:00:00', 45, 'Calle Preciados 22', 29449, 29475, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 207, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (101, '2026-07-07 15:00:00', 45, 'Calle Preciados 22', 29475, 29502, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 207, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (102, '2026-07-14 15:00:00', 45, 'Calle Preciados 22', 29502, 29530, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 207, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (103, '2026-07-21 15:00:00', 45, 'Calle Preciados 22', 29530, 29559, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 207, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (104, '2026-08-18 15:00:00', 45, 'Calle Preciados 22', 29559, 29589, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 207, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (105, '2026-09-01 15:00:00', 45, 'Calle Preciados 22', 29589, 29620, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 207, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (106, '2026-09-08 15:00:00', 45, 'Calle Preciados 22', 29620, 29652, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 207, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (107, '2026-09-15 15:00:00', 45, 'Calle Preciados 22', 29652, 29685, 'Clase práctica formativa.', 'RECIBIDA', FALSE, FALSE, 207, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (108, '2026-07-07 15:45:00', 45, 'Calle Serrano 102', 29685, 29707, 'Circulación y glorietas.', 'RECIBIDA', FALSE, FALSE, 209, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (109, '2026-07-14 15:45:00', 45, 'Calle Serrano 102', 29707, 29730, 'Circulación y glorietas.', 'RECIBIDA', FALSE, FALSE, 209, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (110, '2026-07-21 15:45:00', 45, 'Calle Serrano 102', 29730, 29754, 'Circulación y glorietas.', 'RECIBIDA', FALSE, FALSE, 209, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (111, '2026-08-18 15:45:00', 45, 'Calle Serrano 102', 29754, 29779, 'Circulación y glorietas.', 'RECIBIDA', FALSE, FALSE, 209, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (112, '2026-09-01 15:45:00', 45, 'Calle Serrano 102', 29779, 29805, 'Circulación y glorietas.', 'RECIBIDA', FALSE, FALSE, 209, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (113, '2026-09-08 15:45:00', 45, 'Calle Serrano 102', 29805, 29832, 'Circulación y glorietas.', 'RECIBIDA', FALSE, FALSE, 209, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (114, '2026-09-15 15:45:00', 45, 'Calle Serrano 102', 29832, 29860, 'Circulación y glorietas.', 'RECIBIDA', FALSE, FALSE, 209, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (115, '2026-09-22 15:45:00', 45, 'Calle Serrano 102', 29860, 29889, 'Circulación y glorietas.', 'RECIBIDA', FALSE, FALSE, 209, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (116, '2026-09-29 15:45:00', 45, 'Calle Serrano 102', 29889, 29919, 'Circulación y glorietas.', 'RECIBIDA', FALSE, FALSE, 209, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (117, CURRENT_DATE + INTERVAL '1' DAY + TIME '15:45:00', 45, 'Calle Serrano 102', 29919, 29950, 'Recorridos de examen.', 'PENDIENTE', FALSE, FALSE, 209, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (118, '2026-09-16 16:30:00', 45, 'Calle Zurbarán 9', 29950, 29982, 'Circulación fluida.', 'RECIBIDA', FALSE, FALSE, 212, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (119, '2026-09-23 16:30:00', 45, 'Calle Zurbarán 9', 29982, 30015, 'Circulación fluida.', 'RECIBIDA', FALSE, FALSE, 212, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (120, '2026-09-30 16:30:00', 45, 'Calle Zurbarán 9', 30015, 30037, 'Circulación fluida.', 'RECIBIDA', FALSE, FALSE, 212, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (121, CURRENT_DATE + INTERVAL '2' DAY + TIME '16:30:00', 45, 'Calle Zurbarán 9', 30037, 30060, 'Estacionamiento.', 'PENDIENTE', FALSE, FALSE, 212, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (122, '2026-09-17 16:30:00', 45, 'Calle Hortaleza 30', 30060, 30084, 'Vías rápidas.', 'RECIBIDA', FALSE, FALSE, 213, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (123, '2026-09-24 16:30:00', 45, 'Calle Hortaleza 30', 30084, 30109, 'Vías rápidas.', 'RECIBIDA', FALSE, FALSE, 213, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (124, '2026-10-01 16:30:00', 45, 'Calle Hortaleza 30', 30109, 30135, 'Vías rápidas.', 'RECIBIDA', FALSE, FALSE, 213, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (125, CURRENT_DATE + INTERVAL '3' DAY + TIME '16:30:00', 45, 'Calle Hortaleza 30', 30135, 30162, 'Maniobras complejas.', 'PENDIENTE', FALSE, FALSE, 213, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (126, '2026-06-29 17:15:00', 45, 'Calle Lagasca 75', 30162, 30190, 'Práctica de circulación urbana.', 'RECIBIDA', FALSE, FALSE, 215, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (127, '2026-07-06 17:15:00', 45, 'Calle Lagasca 75', 30190, 30219, 'Práctica de circulación urbana.', 'RECIBIDA', FALSE, FALSE, 215, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (128, '2026-07-13 17:15:00', 45, 'Calle Lagasca 75', 30219, 30249, 'Práctica de circulación urbana.', 'RECIBIDA', FALSE, FALSE, 215, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (129, '2026-07-20 17:15:00', 45, 'Calle Lagasca 75', 30249, 30280, 'Práctica de circulación urbana.', 'RECIBIDA', FALSE, FALSE, 215, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (130, '2026-07-27 17:15:00', 45, 'Calle Lagasca 75', 30280, 30312, 'Práctica de circulación urbana.', 'RECIBIDA', FALSE, FALSE, 215, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (131, '2026-08-17 17:15:00', 45, 'Calle Lagasca 75', 30312, 30345, 'Práctica de circulación urbana.', 'RECIBIDA', FALSE, FALSE, 215, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (132, '2026-08-24 17:15:00', 45, 'Calle Lagasca 75', 30345, 30367, 'Práctica de circulación urbana.', 'RECIBIDA', FALSE, FALSE, 215, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (133, '2026-08-31 17:15:00', 45, 'Calle Lagasca 75', 30367, 30390, 'Práctica de circulación urbana.', 'RECIBIDA', FALSE, FALSE, 215, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (134, '2026-09-07 17:15:00', 45, 'Calle Lagasca 75', 30390, 30414, 'Práctica de circulación urbana.', 'RECIBIDA', FALSE, FALSE, 215, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (135, '2026-09-14 17:15:00', 45, 'Calle Lagasca 75', 30414, 30439, 'Práctica de circulación urbana.', 'RECIBIDA', FALSE, FALSE, 215, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (136, '2026-08-21 17:15:00', 45, 'Calle Miguel Ángel 11', 30439, 30465, 'Manejo inicial y mandos.', 'RECIBIDA', FALSE, FALSE, 227, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (137, '2026-08-28 17:15:00', 45, 'Calle Miguel Ángel 11', 30465, 30492, 'Manejo inicial y mandos.', 'RECIBIDA', FALSE, FALSE, 227, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (138, '2026-09-04 17:15:00', 45, 'Calle Miguel Ángel 11', 30492, 30520, 'Manejo inicial y mandos.', 'RECIBIDA', FALSE, FALSE, 227, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (139, '2026-09-11 17:15:00', 45, 'Calle Miguel Ángel 11', 30520, 30549, 'Manejo inicial y mandos.', 'RECIBIDA', FALSE, FALSE, 227, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (140, '2026-09-18 17:15:00', 45, 'Calle Miguel Ángel 11', 30549, 30579, 'Manejo inicial y mandos.', 'RECIBIDA', FALSE, FALSE, 227, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (141, CURRENT_DATE + INTERVAL '1' DAY + TIME '17:15:00', 45, 'Calle Miguel Ángel 11', 30579, 30610, 'Glorietas y giros.', 'PENDIENTE', FALSE, FALSE, 227, 102);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (142, '2026-09-15 09:30:00', 45, 'Calle Barquillo 14', 138000, 138032, 'Manejo de caja de cambios y frenada neumática en camión rígido.', 'RECIBIDA', FALSE, FALSE, 216, 101);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (143, '2026-09-17 09:30:00', 45, 'Calle Barquillo 14', 138032, 138065, 'Manejo de caja de cambios y frenada neumática en camión rígido.', 'RECIBIDA', FALSE, FALSE, 216, 101);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (144, '2026-09-22 09:30:00', 45, 'Calle Barquillo 14', 138065, 138087, 'Manejo de caja de cambios y frenada neumática en camión rígido.', 'RECIBIDA', FALSE, FALSE, 216, 101);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (145, '2026-09-29 09:30:00', 45, 'Calle Barquillo 14', 138087, 138110, 'Manejo de caja de cambios y frenada neumática en camión rígido.', 'RECIBIDA', FALSE, FALSE, 216, 101);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (146, '2026-10-01 09:30:00', 45, 'Calle Barquillo 14', 138110, 138134, 'Manejo de caja de cambios y frenada neumática en camión rígido.', 'RECIBIDA', FALSE, FALSE, 216, 101);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (147, CURRENT_DATE + INTERVAL '1' DAY + TIME '09:30:00', 45, 'Calle Barquillo 14', 138134, 138159, 'Aproximación a muelles de carga y maniobras en pista.', 'PENDIENTE', FALSE, FALSE, 216, 101);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (148, CURRENT_DATE + INTERVAL '3' DAY + TIME '09:30:00', 45, 'Calle Barquillo 14', 138159, 138185, 'Aproximación a muelles de carga y maniobras en pista.', 'PENDIENTE', FALSE, FALSE, 216, 101);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (149, '2026-09-14 15:45:00', 45, 'Calle Hermosilla 89', 19500, 19527, 'Maniobras en circuito cerrado: equilibrio a baja velocidad y esquiva.', 'RECIBIDA', FALSE, FALSE, 205, 103);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (150, '2026-09-16 15:45:00', 45, 'Calle Hermosilla 89', 19527, 19555, 'Maniobras en circuito cerrado: equilibrio a baja velocidad y esquiva.', 'RECIBIDA', FALSE, FALSE, 205, 103);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (151, '2026-09-21 15:45:00', 45, 'Calle Hermosilla 89', 19555, 19584, 'Maniobras en circuito cerrado: equilibrio a baja velocidad y esquiva.', 'RECIBIDA', FALSE, FALSE, 205, 103);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (152, '2026-09-23 15:45:00', 45, 'Calle Hermosilla 89', 19584, 19614, 'Maniobras en circuito cerrado: equilibrio a baja velocidad y esquiva.', 'RECIBIDA', FALSE, FALSE, 205, 103);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (153, '2026-09-28 15:45:00', 45, 'Calle Hermosilla 89', 19614, 19645, 'Maniobras en circuito cerrado: equilibrio a baja velocidad y esquiva.', 'RECIBIDA', FALSE, FALSE, 205, 103);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (154, '2026-09-30 15:45:00', 45, 'Calle Hermosilla 89', 19645, 19677, 'Maniobras en circuito cerrado: equilibrio a baja velocidad y esquiva.', 'RECIBIDA', FALSE, FALSE, 205, 103);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (155, CURRENT_DATE + INTERVAL '1' DAY + TIME '15:45:00', 45, 'Calle Hermosilla 89', 19677, 19710, 'Circulación en vías abiertas y frenada de emergencia.', 'PENDIENTE', FALSE, FALSE, 205, 103);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (156, CURRENT_DATE + INTERVAL '3' DAY + TIME '15:45:00', 45, 'Calle Hermosilla 89', 19710, 19732, 'Circulación en vías abiertas y frenada de emergencia.', 'PENDIENTE', FALSE, FALSE, 205, 103);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (157, '2026-09-15 10:15:00', 45, 'Calle Argensola 6', 210000, 210023, 'Control de voladizos y giros cerrados en autobús interurbano.', 'RECIBIDA', FALSE, FALSE, 218, 104);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (158, '2026-09-18 10:15:00', 45, 'Calle Argensola 6', 210023, 210047, 'Control de voladizos y giros cerrados en autobús interurbano.', 'RECIBIDA', FALSE, FALSE, 218, 104);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (159, '2026-09-22 10:15:00', 45, 'Calle Argensola 6', 210047, 210072, 'Control de voladizos y giros cerrados en autobús interurbano.', 'RECIBIDA', FALSE, FALSE, 218, 104);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (160, '2026-09-25 10:15:00', 45, 'Calle Argensola 6', 210072, 210098, 'Control de voladizos y giros cerrados en autobús interurbano.', 'RECIBIDA', FALSE, FALSE, 218, 104);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (161, '2026-09-29 10:15:00', 45, 'Calle Argensola 6', 210098, 210125, 'Control de voladizos y giros cerrados en autobús interurbano.', 'RECIBIDA', FALSE, FALSE, 218, 104);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (162, '2026-10-02 10:15:00', 45, 'Calle Argensola 6', 210125, 210153, 'Control de voladizos y giros cerrados en autobús interurbano.', 'RECIBIDA', FALSE, FALSE, 218, 104);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (163, CURRENT_DATE + INTERVAL '1' DAY + TIME '10:15:00', 45, 'Calle Argensola 6', 210153, 210182, 'Paradas reglamentarias y aproximación a dársenas.', 'PENDIENTE', FALSE, FALSE, 218, 104);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (164, CURRENT_DATE + INTERVAL '3' DAY + TIME '10:15:00', 45, 'Calle Argensola 6', 210182, 210212, 'Paradas reglamentarias y aproximación a dársenas.', 'PENDIENTE', FALSE, FALSE, 218, 104);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (165, '2026-09-25 08:00:00', 45, 'Calle Toledo 10', 43880, 43911, 'Clase cancelada por indisposición del profesor.', 'CANCELADA', TRUE, FALSE, 200, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (166, '2026-09-25 08:45:00', 45, 'Avenida de la Albufera 45', 43911, 43943, 'Clase cancelada por motivos laborales del alumno.', 'CANCELADA', FALSE, FALSE, 201, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (167, '2026-10-08 11:45:00', 45, 'Calle Fernando VI 19', 43739, 43760, 'PrÃ¡ctica de circulaciÃ³n urbana y glorietas.', 'RECIBIDA', FALSE, FALSE, 219, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (168, '2026-10-15 11:45:00', 45, 'Calle Fernando VI 19', 43760, 43782, 'PrÃ¡ctica de perfeccionamiento y estacionamiento.', 'RECIBIDA', FALSE, FALSE, 219, 100);
INSERT INTO clase_practica (id, fecha_hora, duracion, punto_recogida, km_inicio, km_fin, observaciones, estado_clase, is_cancelada_por_profesor, is_cancelacion_jornada, alumno_id, profesor_id)
VALUES (169, '2026-10-19 11:45:00', 45, 'Calle Fernando VI 19', 43782, 43805, 'Recorridos oficiales previos a examen DGT.', 'RECIBIDA', FALSE, FALSE, 219, 100);

-- ------------------------------------------------------------------------------
-- 7. SOLICITUDES DE EXAMEN DGT (solicitud_examen) - 64 Solicitudes
-- ------------------------------------------------------------------------------
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (1, '2026-08-10 09:00:00', 'ACEPTADA', 'TEORICO', NULL, 200, NULL, 1);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (2, '2026-10-14 10:30:00', 'ACEPTADA', 'PRACTICO', NULL, 200, 100, 1);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (3, '2026-05-25 11:00:00', 'ACEPTADA', 'TEORICO', NULL, 208, NULL, 9);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (4, '2026-08-18 10:00:00', 'ACEPTADA', 'PRACTICO', NULL, 208, 100, 9);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (5, '2026-09-01 12:00:00', 'ACEPTADA', 'PRACTICO', NULL, 208, 100, 9);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (6, '2026-06-20 09:00:00', 'ACEPTADA', 'PRACTICO', NULL, 207, 102, 8);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (7, '2026-07-15 10:00:00', 'ACEPTADA', 'PRACTICO', NULL, 207, 102, 8);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (8, '2026-08-10 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 201, NULL, 2);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (9, '2026-10-18 09:45:00', 'ACEPTADA', 'PRACTICO', NULL, 201, 100, 2);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (10, '2026-10-19 12:00:00', 'PENDIENTE', 'PRACTICO', NULL, 205, 103, 6);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (11, '2026-10-20 16:30:00', 'ACEPTADA', 'PRACTICO', NULL, 204, 100, 5);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (12, '2026-10-15 17:15:00', 'ACEPTADA', 'PRACTICO', NULL, 219, 100, 30);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (13, '2026-09-15 10:00:00', 'RECHAZADA', 'PRACTICO', 'Se aconseja realizar al menos 5 clases prácticas más de circulación en vías rápidas antes de ir a examen.', 203, 100, 4);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (14, '2026-09-18 11:30:00', 'RECHAZADA', 'PRACTICO', 'Certificado médico psicotécnico caducado, presentar renovación en secretaría.', 211, 102, 13);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (15, '2026-09-22 16:00:00', 'RECHAZADA', 'PRACTICO', 'Falta afianzar la maniobra de aproximación a muelle antes de ir a examen.', 216, 101, 18);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (16, '2026-08-20 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 214, NULL, 17);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (17, '2026-10-10 09:00:00', 'ACEPTADA', 'TEORICO', NULL, 217, NULL, 19);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (18, '2026-06-10 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 220, NULL, 27);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (19, '2026-10-14 11:00:00', 'ACEPTADA', 'PRACTICO', NULL, 220, 106, 27);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (20, '2026-08-28 09:00:00', 'ACEPTADA', 'TEORICO', NULL, 205, NULL, 6);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (21, '2026-07-28 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 204, NULL, 5);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (22, '2026-07-08 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 210, NULL, 12);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (23, '2026-10-14 17:00:00', 'ACEPTADA', 'PRACTICO', NULL, 210, 102, 12);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (24, '2026-07-15 11:00:00', 'ACEPTADA', 'TEORICO', NULL, 213, NULL, 16);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (25, '2026-10-14 12:00:00', 'ACEPTADA', 'PRACTICO', NULL, 213, 102, 16);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (26, '2026-08-10 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 219, NULL, 30);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (27, '2026-08-18 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 222, NULL, 23);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (28, '2026-10-14 16:00:00', 'ACEPTADA', 'PRACTICO', NULL, 209, 102, 11);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (29, '2026-10-14 11:00:00', 'ACEPTADA', 'PRACTICO', NULL, 212, 102, 14);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (30, '2026-06-20 09:00:00', 'ACEPTADA', 'TEORICO', NULL, 206, NULL, 7);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (31, '2026-07-05 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 206, NULL, 7);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (32, '2026-07-20 09:00:00', 'ACEPTADA', 'TEORICO', NULL, 206, NULL, 7);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (33, '2026-08-10 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 206, NULL, 7);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (34, '2026-08-18 10:00:00', 'ACEPTADA', 'PRACTICO', NULL, 207, 102, 8);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (35, '2026-09-15 10:00:00', 'ACEPTADA', 'PRACTICO', NULL, 207, 102, 8);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (36, '2026-04-25 09:00:00', 'ACEPTADA', 'TEORICO', NULL, 215, NULL, 15);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (37, '2026-05-12 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 215, NULL, 15);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (38, '2026-06-01 09:00:00', 'ACEPTADA', 'TEORICO', NULL, 215, NULL, 15);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (39, '2026-06-15 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 215, NULL, 15);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (40, '2026-07-20 10:00:00', 'ACEPTADA', 'PRACTICO', NULL, 215, 102, 15);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (41, '2026-08-10 11:00:00', 'ACEPTADA', 'PRACTICO', NULL, 215, 102, 15);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (42, '2026-09-01 10:00:00', 'ACEPTADA', 'PRACTICO', NULL, 215, 102, 15);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (43, '2026-09-20 11:00:00', 'ACEPTADA', 'PRACTICO', NULL, 215, 102, 15);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (44, '2026-08-25 09:00:00', 'ACEPTADA', 'TEORICO', NULL, 226, NULL, 28);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (45, '2026-09-05 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 226, NULL, 28);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (46, '2026-10-18 10:00:00', 'PENDIENTE', 'TEORICO', NULL, 224, NULL, 25);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (47, '2026-10-19 11:30:00', 'PENDIENTE', 'PRACTICO', NULL, 203, 100, 4);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (48, '2026-10-20 09:00:00', 'PENDIENTE', 'TEORICO', NULL, 225, NULL, 26);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (49, '2026-10-21 16:00:00', 'PENDIENTE', 'PRACTICO', NULL, 209, 102, 11);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (50, '2026-10-13 12:00:00', 'RECHAZADA', 'PRACTICO', 'No cumple con las destrezas minimas en maniobras de estacionamiento y circulacion urbana.', 204, 100, 5);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (51, '2026-10-20 09:30:00', 'ACEPTADA', 'TEORICO', NULL, 253, NULL, 21);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (52, '2026-10-20 10:15:00', 'ACEPTADA', 'TEORICO', NULL, 254, NULL, 56);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (53, '2026-10-21 09:00:00', 'ACEPTADA', 'TEORICO', NULL, 255, NULL, 57);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (54, '2026-10-21 11:30:00', 'ACEPTADA', 'TEORICO', NULL, 256, NULL, 58);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (55, '2026-10-22 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 257, NULL, 59);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (56, '2026-09-15 09:00:00', 'ACEPTADA', 'TEORICO', NULL, 263, NULL, 65);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (57, '2026-09-15 09:30:00', 'ACEPTADA', 'TEORICO', NULL, 264, NULL, 66);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (58, '2026-09-16 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 265, NULL, 67);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (59, '2026-10-05 09:00:00', 'ACEPTADA', 'TEORICO', NULL, 266, NULL, 68);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (60, '2026-10-05 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 267, NULL, 69);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (61, '2026-10-23 10:00:00', 'ACEPTADA', 'TEORICO', NULL, 258, NULL, 60);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (62, '2026-10-24 11:00:00', 'ACEPTADA', 'TEORICO', NULL, 259, NULL, 61);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (63, '2026-10-21 09:30:00', 'ACEPTADA', 'PRACTICO', NULL, 206, 100, 7);
INSERT INTO solicitud_examen (id, fecha_hora, estado, tipo, comentario_justificacion, alumno_id, profesor_id, matricula_id)
VALUES (64, '2026-10-22 10:30:00', 'ACEPTADA', 'PRACTICO', NULL, 214, 100, 17);

-- ------------------------------------------------------------------------------
-- 8. EXÁMENES DGT (examen) - 55 Exámenes (Días laborables no festivos)
-- ------------------------------------------------------------------------------
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (1, TRUE, '2026-08-20 08:30:00', 30, 'TEORICO', 200, 1);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (2, TRUE, '2026-06-05 10:00:00', 30, 'TEORICO', 208, 3);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (7, TRUE, '2026-08-20 08:30:00', 30, 'TEORICO', 201, 8);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (8, TRUE, '2026-09-10 08:30:00', 30, 'TEORICO', 205, 20);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (14, TRUE, '2026-09-02 10:00:00', 30, 'TEORICO', 214, 16);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (16, TRUE, '2026-06-18 10:00:00', 30, 'TEORICO', 220, 18);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (18, TRUE, '2026-08-20 08:30:00', 30, 'TEORICO', 204, 21);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (19, TRUE, '2026-07-20 10:00:00', 30, 'TEORICO', 210, 22);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (20, TRUE, '2026-07-23 11:00:00', 30, 'TEORICO', 213, 24);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (21, TRUE, '2026-08-20 08:30:00', 30, 'TEORICO', 219, 26);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (22, TRUE, '2026-08-27 08:30:00', 30, 'TEORICO', 222, 27);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (23, FALSE, '2026-07-02 09:00:00', 30, 'TEORICO', 206, 30);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (24, FALSE, '2026-07-16 10:00:00', 30, 'TEORICO', 206, 31);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (25, FALSE, '2026-07-30 09:00:00', 30, 'TEORICO', 206, 32);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (26, TRUE, '2026-08-20 08:30:00', 30, 'TEORICO', 206, 33);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (29, FALSE, '2026-05-08 09:00:00', 30, 'TEORICO', 215, 36);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (30, FALSE, '2026-05-22 10:00:00', 30, 'TEORICO', 215, 37);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (31, FALSE, '2026-06-12 09:00:00', 30, 'TEORICO', 215, 38);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (32, TRUE, '2026-06-26 10:00:00', 30, 'TEORICO', 215, 39);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (6, FALSE, '2026-08-25 09:30:00', 25, 'PRACTICO', 208, 4);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (9, TRUE, '2026-09-24 08:30:00', 35, 'PRACTICO', 208, 5);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (17, TRUE, '2026-10-29 08:30:00', 35, 'PRACTICO', 220, 19);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (4, FALSE, '2026-06-30 16:30:00', 25, 'PRACTICO', 207, 6);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (5, FALSE, '2026-07-28 17:00:00', 25, 'PRACTICO', 207, 7);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (27, FALSE, '2026-08-25 16:30:00', 25, 'PRACTICO', 207, 34);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (28, FALSE, '2026-09-25 17:15:00', 25, 'PRACTICO', 207, 35);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (33, FALSE, '2026-07-31 10:30:00', 25, 'PRACTICO', 215, 40);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (34, FALSE, '2026-08-21 11:00:00', 25, 'PRACTICO', 215, 41);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (35, FALSE, '2026-09-11 10:30:00', 25, 'PRACTICO', 215, 42);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (36, FALSE, '2026-10-15 08:30:00', 25, 'PRACTICO', 215, 43);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (3, NULL, '2026-10-29 08:30:00', 35, 'PRACTICO', 200, 2);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (10, NULL, '2026-10-29 08:30:00', 45, 'PRACTICO', 209, 28);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (11, NULL, '2026-10-29 08:30:00', 40, 'PRACTICO', 212, 29);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (12, NULL, '2026-10-29 08:30:00', 30, 'PRACTICO', 210, 23);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (13, NULL, '2026-10-29 08:30:00', 35, 'PRACTICO', 213, 25);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (15, NULL, '2026-10-29 08:30:00', 30, 'TEORICO', 217, 17);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (37, FALSE, '2026-09-02 09:00:00', 30, 'TEORICO', 226, 44);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (38, TRUE, '2026-09-16 10:00:00', 30, 'TEORICO', 226, 45);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (39, TRUE, '2026-10-29 08:30:00', 35, 'PRACTICO', 219, 12);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (40, TRUE, '2026-09-24 08:30:00', 30, 'TEORICO', 263, 56);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (41, TRUE, '2026-09-24 08:30:00', 30, 'TEORICO', 264, 57);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (42, TRUE, '2026-09-24 08:30:00', 30, 'TEORICO', 265, 58);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (43, TRUE, '2026-10-15 08:30:00', 30, 'TEORICO', 266, 59);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (44, TRUE, '2026-10-15 08:30:00', 30, 'TEORICO', 267, 60);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (45, NULL, '2026-10-29 08:30:00', 30, 'TEORICO', 253, 51);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (46, NULL, '2026-10-29 08:30:00', 30, 'TEORICO', 254, 52);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (47, NULL, '2026-10-29 08:30:00', 30, 'TEORICO', 255, 53);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (48, NULL, '2026-10-29 08:30:00', 30, 'TEORICO', 256, 54);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (49, NULL, '2026-11-12 08:30:00', 30, 'TEORICO', 257, 55);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (50, NULL, '2026-11-12 08:30:00', 30, 'TEORICO', 258, 61);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (51, NULL, '2026-11-26 08:30:00', 30, 'TEORICO', 259, 62);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (52, NULL, '2026-11-12 08:30:00', 35, 'PRACTICO', 206, 63);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (53, NULL, '2026-11-26 08:30:00', 35, 'PRACTICO', 214, 64);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (54, NULL, '2026-10-29 08:30:00', 35, 'PRACTICO', 201, 9);
INSERT INTO examen (id, es_apto, fecha_hora, duracion, tipo, alumno_id, solicitud_id)
VALUES (55, NULL, '2026-10-29 08:30:00', 35, 'PRACTICO', 204, 11);

-- ------------------------------------------------------------------------------
-- 8.2. CONVOCATORIAS OFICIALES DGT DEL MES (fecha_examen) - 4 Meses
-- ------------------------------------------------------------------------------
INSERT INTO fecha_examen (id, mes, fecha_hora_1, fecha_hora_2)
VALUES (1, '2026-10-01', '2026-10-15 08:30:00', '2026-10-29 08:30:00');
INSERT INTO fecha_examen (id, mes, fecha_hora_1, fecha_hora_2)
VALUES (2, '2026-09-01', '2026-09-10 08:30:00', '2026-09-24 08:30:00');
INSERT INTO fecha_examen (id, mes, fecha_hora_1, fecha_hora_2)
VALUES (3, '2026-11-01', '2026-11-12 08:30:00', '2026-11-26 08:30:00');
INSERT INTO fecha_examen (id, mes, fecha_hora_1, fecha_hora_2)
VALUES (4, '2026-08-01', '2026-08-20 08:30:00', '2026-08-27 08:30:00');

-- ------------------------------------------------------------------------------
-- 8.3. CUPOS DE EXAMEN POR TIPO DE CARNET (cupo_examen) - 32 Registros (8 permisos x 4 meses)
-- ------------------------------------------------------------------------------
-- Octubre 2026 (Cupo práctico de Permiso B dimensionado para cubrir los 9 alumnos examinados el 29/10/2026)
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (1, '2026-10-01', 'PERMISO_B', 5, 10);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (2, '2026-10-01', 'PERMISO_A2', 4, 3);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (3, '2026-10-01', 'PERMISO_A', 3, 3);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (4, '2026-10-01', 'PERMISO_C', 3, 2);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (5, '2026-10-01', 'PERMISO_D', 2, 2);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (6, '2026-10-01', 'PERMISO_B_E', 2, 2);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (7, '2026-10-01', 'PERMISO_AM', 3, 3);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (8, '2026-10-01', 'PERMISO_A1', 3, 3);

-- Septiembre 2026
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (9, '2026-09-01', 'PERMISO_B', 5, 4);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (10, '2026-09-01', 'PERMISO_A2', 4, 3);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (11, '2026-09-01', 'PERMISO_A', 3, 3);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (12, '2026-09-01', 'PERMISO_C', 2, 2);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (13, '2026-09-01', 'PERMISO_D', 2, 2);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (14, '2026-09-01', 'PERMISO_B_E', 2, 2);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (15, '2026-09-01', 'PERMISO_AM', 3, 3);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (16, '2026-09-01', 'PERMISO_A1', 3, 3);

-- Noviembre 2026
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (17, '2026-11-01', 'PERMISO_B', 5, 4);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (18, '2026-11-01', 'PERMISO_A2', 4, 3);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (19, '2026-11-01', 'PERMISO_A', 3, 3);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (20, '2026-11-01', 'PERMISO_C', 2, 2);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (21, '2026-11-01', 'PERMISO_D', 2, 2);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (22, '2026-11-01', 'PERMISO_B_E', 2, 2);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (23, '2026-11-01', 'PERMISO_AM', 3, 3);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (24, '2026-11-01', 'PERMISO_A1', 3, 3);

-- Agosto 2026 (Cupo teórico de Permiso B dimensionado para cubrir los 5 alumnos examinados el 20/08/2026)
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (25, '2026-08-01', 'PERMISO_B', 6, 4);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (26, '2026-08-01', 'PERMISO_A2', 4, 3);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (27, '2026-08-01', 'PERMISO_A', 3, 3);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (28, '2026-08-01', 'PERMISO_C', 2, 2);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (29, '2026-08-01', 'PERMISO_D', 2, 2);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (30, '2026-08-01', 'PERMISO_B_E', 2, 2);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (31, '2026-08-01', 'PERMISO_AM', 3, 3);
INSERT INTO cupo_examen (id, mes, tipo_permiso, cupo_teorico, cupo_practico) VALUES (32, '2026-08-01', 'PERMISO_A1', 3, 3);

-- ------------------------------------------------------------------------------
-- 9. REAJUSTE DE SECUENCIAS DE IDENTIDAD (H2)
-- ------------------------------------------------------------------------------
ALTER TABLE vehiculo ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE persona ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE incidencia_vehiculo ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE matricula ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE clase_practica ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE solicitud_examen ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE examen ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE fecha_examen ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE cupo_examen ALTER COLUMN id RESTART WITH 1000;
