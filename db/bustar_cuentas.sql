-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 26-08-2024 a las 22:42:14
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `bustar_cuentas`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `banip`
--

CREATE TABLE `banip` (
  `ip` varchar(15) CHARACTER SET latin1 COLLATE latin1_spanish_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cuentas`
--

CREATE TABLE `cuentas` (
  `id` int(11) NOT NULL,
  `cuenta` varchar(30) NOT NULL,
  `contraseña` varchar(50) NOT NULL,
  `rango` tinyint(2) NOT NULL DEFAULT 0,
  `nombre` varchar(255) NOT NULL,
  `apellido` varchar(255) NOT NULL,
  `pais` varchar(255) NOT NULL,
  `idioma` varchar(255) NOT NULL,
  `ipRegistro` varchar(15) NOT NULL,
  `cumpleaños` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `ultimaIP` varchar(15) NOT NULL,
  `pregunta` varchar(100) NOT NULL DEFAULT 'ES DOFUS',
  `respuesta` varchar(100) NOT NULL DEFAULT 'DOFUS',
  `apodo` varchar(30) NOT NULL,
  `baneado` bigint(30) NOT NULL DEFAULT 0,
  `logeado` tinyint(1) NOT NULL DEFAULT 0,
  `creditos` int(11) NOT NULL DEFAULT 0,
  `ogrinas` int(11) NOT NULL DEFAULT 0,
  `votos` int(11) NOT NULL DEFAULT 0,
  `actualizar` tinyint(1) NOT NULL DEFAULT 1,
  `ultimoVoto` varchar(255) NOT NULL COMMENT 'en segundos',
  `abono` bigint(30) NOT NULL DEFAULT 0 COMMENT 'en milisegundos',
  `fechaCreacion` bigint(30) NOT NULL,
  `ticket` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
-- Volcado de datos para la tabla `cuentas`
--

INSERT INTO `cuentas` (`id`, `cuenta`, `contraseña`, `rango`, `nombre`, `apellido`, `pais`, `idioma`, `ipRegistro`, `cumpleaños`, `email`, `ultimaIP`, `pregunta`, `respuesta`, `apodo`, `baneado`, `logeado`, `creditos`, `ogrinas`, `votos`, `actualizar`, `ultimoVoto`, `abono`, `fechaCreacion`, `ticket`) VALUES
(1, 'beta1', '1', 5, 'aa', 'aaa', 'ES', 'ES', '', '1~1~2011', 'aaa@hotmail.com', '127.0.0.1', 'aa', 'aaa', 'beta1', 0, 0, 166, 86185, 163, 1, '1526149045', 1702293884447, 1445382095692, '');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `cuentas`
--
ALTER TABLE `cuentas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `cuenta` (`cuenta`) USING BTREE;

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `cuentas`
--
ALTER TABLE `cuentas`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
