package estaticos

import login.LoginServer
import sincronizador.SincronizadorServer
import variables.Servidor
import java.io.*
import java.util.*
import kotlin.system.exitProcess

object MainMultiservidor {
    const val ES_LOCALHOST = false
    //
//
//
//
    var SONIDO_BIENVENIDA = ""
    var URL_LINK_MP3 = "http://localhost/mp3/"
    private var DIRECTORIO_LOCAL_MP3 = "C://wamp/www/mp3/"
    var ENCRIPTAR_IP = false
    var PERMITIR_MULTICUENTA = true
    var MOSTRAR_RECIBIDOS = false
    var MOSTRAR_ENVIOS = false
    var MOSTRAR_SINCRONIZACION = false
    var MODO_DEBUG = false
    var PARAM_ANTI_DDOS = false
    var PARAM_MOSTRAR_IP = false
    var ACTIVAR_FILA_ESPERA = true
    var ACCESO_VIP = false
    var PUERTO_MULTISERVIDOR = 444
    var PUERTO_SINCRONIZADOR = 19999
    // public static ArrayList<String> IP_SERVIDOR = new ArrayList<String>();// 25.91.217.194 -
// 213.152.29.73
    var VERSION_CLIENTE = "1.29.1"
    var BD_HOST = "localhost"
    var BD_USUARIO = "root"
    var BD_PASS = ""
    var BD_CUENTAS = ""
    var MILISEGUNDOS_SIG_CONEXION = 500
    var SEGUNDOS_INFO_STATUS = 60
    var SEGUNDOS_ESTADISTICAS = 300
    var SEGUNDOS_TRANSACCION_BD = 10
    var SEGUNDOS_ESPERA = 15
    // public static int MAX_CONEXIONES_POR_IP = 8;
    var MAX_CUENTAS_POR_IP: Byte = 8
    var MAX_CONEXION_POR_SEGUNDO: Byte = 10
    private var DATE_ERROR: Calendar? = null
    private var DATE_ESTADISTICA: Calendar? = null
    private var LOG_ERRORES: PrintStream? = null
    private var LOG_ESTADISTICAS: PrintStream? = null
    private var PARAM_MOSTRAR_EXCEPTIONS = false
    private var LIMITE_JUGADORES = 100
    @JvmStatic
    fun main(args: Array<String>) {
        Runtime.getRuntime().addShutdownHook(Thread({ cerrarServer() }))
        println("\tATLANTA LOGIN, PARA ZUFOKIA\n\t\tPor Oxakromax")
        println("\n\nCargando la configuración")
        cargarConfiguracion()
        crearLogEstadisticas()
        if (GestorSQL.iniciarConexion()) {
            println("CONEXION OK")
        } else {
            escribirLog("CONEXION SQL INVALIDA!!")
            exitProcess(1)
            return
        }
        Mundo.crearMultiServer()
        SincronizadorServer()
        LoginServer()
        Consola()
        println("Esperando que los jugadores se conecten")
    }

    private fun crearLogErrores() {
        while (true) {
            try {
                DATE_ERROR = Calendar.getInstance()
                val date = (DATE_ERROR?.get(Calendar.DAY_OF_MONTH).toString() + "-" + (DATE_ERROR?.get(Calendar.MONTH)?.plus(1)) + "-"
                        + DATE_ERROR?.get(Calendar.YEAR))
                LOG_ERRORES = PrintStream(FileOutputStream("Logs_MultiServidor/Log_$date.txt", true))
                LOG_ERRORES!!.println("---------- INICIO DEL MULTISERVIDOR ----------")
                LOG_ERRORES!!.flush()
                System.setErr(LOG_ERRORES)
                break
            } catch (e: IOException) {
                File("Logs_MultiServidor").mkdir()
            } catch (e: Exception) {
                break
            }
        }
    }

    private fun crearLogEstadisticas() {
        while (true) {
            try {
                DATE_ESTADISTICA = Calendar.getInstance()
                val date = (DATE_ESTADISTICA?.get(Calendar.DAY_OF_MONTH).toString() + "-"
                        + (DATE_ESTADISTICA?.get(Calendar.MONTH)?.plus(1)) + "-" + DATE_ESTADISTICA?.get(Calendar.YEAR))
                LOG_ESTADISTICAS = PrintStream(FileOutputStream("Estadisticas_Multiservidor/Conectados_" + date
                        + ".txt", true))
                LOG_ESTADISTICAS!!.flush()
                break
            } catch (e: IOException) {
                File("Estadisticas_Multiservidor").mkdir()
            } catch (e: Exception) {
                break
            }
        }
    }

    fun cargarConfiguracion() {
        try {
            val ARCHIVO_CONFIG = "config_MultiServidor.txt"
            val config = BufferedReader(FileReader(ARCHIVO_CONFIG))
            var linea = ""
            val comandos = ArrayList<String>()
            while (config.readLine().also { linea = it } != null) {
                try {
                    if (linea.isEmpty() || linea.contains("#")) {
                        continue
                    }
                    val param = linea.split("=".toRegex()).toTypedArray()[0].trim { it <= ' ' }
                    val valor = linea.split("=".toRegex()).toTypedArray()[1].trim { it <= ' ' }
                    if (comandos.contains(param)) {
                        println("En el $ARCHIVO_CONFIG se repite el comando $param")
                        exitProcess(1)
                        return
                    } else {
                        comandos.add(param)
                    }
                    when (param.toUpperCase()) {
                        "DIRECTORIO_LOCAL_MP3" -> DIRECTORIO_LOCAL_MP3 = valor
                        "URL_LINK_MP3" -> URL_LINK_MP3 = valor
                        "MOSTRAR_ENVIADOS", "ENVIADOS" -> MOSTRAR_ENVIOS = valor.equals("true", ignoreCase = true)
                        "SINCRONIZADOS", "MOSTRAR_SINCRONIZACION" -> MOSTRAR_SINCRONIZACION = valor.equals("true", ignoreCase = true)
                        "MOSTRAR_RECIBIDOS", "RECIBIDOS" -> MOSTRAR_RECIBIDOS = valor.equals("true", ignoreCase = true)
                        "MODO_DEBUG" -> MODO_DEBUG = valor.equals("true", ignoreCase = true)
                        "PARAM_ANTI_DDOS" -> PARAM_ANTI_DDOS = valor.equals("true", ignoreCase = true)
                        "PARAM_MOSTRAR_IP" -> PARAM_MOSTRAR_IP = valor.equals("true", ignoreCase = true)
                        "PARAM_MOSTRAR_EXCEPTIONS" -> PARAM_MOSTRAR_EXCEPTIONS = valor.equals("true", ignoreCase = true)
                        "ACTIVAR_FILA_ESPERA" -> ACTIVAR_FILA_ESPERA = valor.equals("true", ignoreCase = true)
                        "ACCESO_VIP" -> ACCESO_VIP = valor.equals("true", ignoreCase = true)
                        "ENCRIPTAR_IP" -> ENCRIPTAR_IP = valor.equals("true", ignoreCase = true)
                        "VERSION_CLIENTE" -> VERSION_CLIENTE = valor
                        "PUERTO_MULTISERVER", "PUERTO_MULTISERVIDOR" -> PUERTO_MULTISERVIDOR = valor.toInt()
                        "PUERTO_SINCRONIZACION", "PUERTO_SINCRONIZADOR" -> PUERTO_SINCRONIZADOR = valor.toInt()
                        "BD_HOST" -> BD_HOST = valor
                        "BD_USER", "BD_USUARIO" -> BD_USUARIO = valor
                        "BD_PASSWORD", "BD_CONTRASEÑA", "BD_PASS" -> BD_PASS = valor
                        "BD_ACCOUNTS", "BD_COMPTES", "BD_CUENTAS", "BD_LOGIN", "BD_REALM" -> BD_CUENTAS = valor
                        "MAX_CUENTAS_POR_IP" -> MAX_CUENTAS_POR_IP = valor.toByte()
                        "SEGUNDOS_ESPERA", "TIEMPO_ESPERA" -> SEGUNDOS_ESPERA = valor.toInt()
                        "SEGUNDOS_TRANSACCION_BD", "TIEMPO_TRANSACCION_BD" -> SEGUNDOS_TRANSACCION_BD = valor.toInt()
                        "SEGUNDOS_INFO_STATUS", "TIEMPO_INFO_STATUS" -> SEGUNDOS_INFO_STATUS = valor.toInt()
                        "SEGUNDOS_ESTADISTICAS" -> SEGUNDOS_ESTADISTICAS = valor.toInt()
                        "MAX_CONEXION_POR_SEGUNDO", "CONEXION_SEGUNDO" -> MAX_CONEXION_POR_SEGUNDO = (valor.toByte() - 1).toByte()
                        "MILISEGUNDOS_SIG_CONEXION", "TIEMPO_SIG_CONEXION" -> MILISEGUNDOS_SIG_CONEXION = valor.toInt()
                        "ENABLED_MULTIACCOUNT", "PERMITIR_MULTICUENTA" -> PERMITIR_MULTICUENTA = valor.equals("true", ignoreCase = true)
                        "LIMITE_JUGADORES" -> LIMITE_JUGADORES = valor.toShort().toInt()
                        "CONFIG_SERVERS" -> {
                            val s = valor.split(";".toRegex()).toTypedArray()
                            for (sx in s) {
                                try {
                                    val id = sx.split(",".toRegex()).toTypedArray()[0].toInt()
                                    val puerto = sx.split(",".toRegex()).toTypedArray()[1].toInt()
                                    Mundo.Servidores[id] = Servidor(id, puerto, Servidor.SERVIDOR_OFFLINE)
                                } catch (e: Exception) {
                                    println("ERROR EN CONFIG_SERVER $e")
                                }
                            }
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
            config.close()
        } catch (e: Exception) {
//            println(e.toString())
//            println("Ficha de la configuración no existe o ilegible")
        }
    }

    fun escribirLog(str: String) {
        println(str)
        try {
            val temp = Calendar.getInstance()
            if (temp[Calendar.DAY_OF_YEAR] != DATE_ERROR!![Calendar.DAY_OF_YEAR]) {
                crearLogErrores()
            }
            val hora = (temp[Calendar.HOUR_OF_DAY].toString() + ":" + temp[Calendar.MINUTE] + ":"
                    + temp[Calendar.SECOND])
            LOG_ERRORES!!.println("[$hora]  $str")
            LOG_ERRORES!!.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun escribirEstadisticas() {
        try {
            val temp = Calendar.getInstance()
            if (temp[Calendar.DAY_OF_YEAR] != DATE_ESTADISTICA!![Calendar.DAY_OF_YEAR]) {
                crearLogEstadisticas()
            }
            val hora = (temp[Calendar.HOUR_OF_DAY].toString() + ":" + temp[Calendar.MINUTE] + ":"
                    + temp[Calendar.SECOND])
            LOG_ESTADISTICAS!!.printf("%-8s", hora)
            for (server in Mundo.Servidores.values) {
                LOG_ESTADISTICAS!!.printf("\tSERVIDOR_%-4d(%1d):%3d", server!!.id, server.estado, server.conectados)
            }
            LOG_ESTADISTICAS!!.println()
            LOG_ESTADISTICAS!!.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cerrarServer() {
        println("SE ESTA INICIANDO EL CIERRE DEL SERVIDOR ...")
        GestorSQL.cerrarConexion()
        println("EL SERVIDOR SE CERRO EXITOSAMENTE")
    }

    fun infoStatus() {
        try {
            val mod = BufferedWriter(FileWriter("info_status.php"))
            mod.write("<?php " + Mundo.infoStatus())
            mod.write("  echo 's='.\$server_5_status.':o='.\$server_5_onlines; ?>")
            mod.flush()
            mod.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}