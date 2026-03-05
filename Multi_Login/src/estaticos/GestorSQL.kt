package estaticos

//import com.mysql.jdbc.PreparedStatement
import variables.Cuenta
import java.io.PrintWriter
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*

//import java.util.Timer;
//import java.util.TimerTask;
object GestorSQL {
    private var bdCuentas: Connection? = null
    private var timerComienzo: Timer? = null
    private var necesitaHacerTransaccion = false
    private fun cerrarResultado(resultado: ResultSet) {
        try {
            resultado.statement.close()
            resultado.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cerrarDeclaracion(declaracion: PreparedStatement) {
        try {
            declaracion.clearParameters()
            declaracion.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun consultaSQL(consultaSQL: String): ResultSet {
        val declaracion = bdCuentas!!.prepareStatement(consultaSQL) as PreparedStatement
        val resultado = declaracion.executeQuery()
        declaracion.queryTimeout = 300
        return resultado
    }

    @Throws(Exception::class)
    private fun consultaSQL(declaracion: PreparedStatement): ResultSet {
        declaracion.execute()
        val resultado = declaracion.executeQuery()
        declaracion.queryTimeout = 300
        return resultado
    }

    @Throws(Exception::class)
    private fun transaccionSQL(consultaSQL: String): PreparedStatement {
        val declaracion = bdCuentas!!.prepareStatement(consultaSQL) as PreparedStatement
        necesitaHacerTransaccion = true
        return declaracion
    }

    private fun TIMER(iniciar: Boolean) {
        if (iniciar) {
            timerComienzo = Timer()
            timerComienzo!!.schedule(object : TimerTask() {
                override fun run() {
                    if (!necesitaHacerTransaccion) {
                        return
                    }
                    comenzarTransacciones()
                    necesitaHacerTransaccion = false
                }
            }, MainMultiservidor.SEGUNDOS_TRANSACCION_BD * 1000.toLong(), MainMultiservidor.SEGUNDOS_TRANSACCION_BD * 1000.toLong())
        } else {
            timerComienzo!!.cancel()
        }
    }

    @Synchronized
    private fun comenzarTransacciones() {
        try {
            if (bdCuentas!!.isClosed) {
                cerrarConexion()
                iniciarConexion()
            }
            bdCuentas!!.commit()
        } catch (e: Exception) {
            println("SQL ERROR:$e")
            e.printStackTrace()
            // comenzarTransacciones();
        }
    }

    @Synchronized
    fun cerrarConexion() {
        try {
            try {
                bdCuentas!!.commit()
            } catch (ignored: Exception) {
            }
            bdCuentas!!.close()
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("Error en la ventana de conexiones SQL:$e")
            e.printStackTrace()
        }
    }
    //Edite por Maestro-Yaco 13/09/2025 para MYSQL en linux VPS -onesv.fun
    fun iniciarConexion(): Boolean {
        try {
            print("Conexión a la base de datos: ")

            // Agregar los parámetros de codificación de caracteres para asegurar la compatibilidad
            val urlParametros = "?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8"

            bdCuentas = DriverManager.getConnection(
                "jdbc:mysql://" + MainMultiservidor.BD_HOST + "/" +
                        MainMultiservidor.BD_CUENTAS + urlParametros,
                MainMultiservidor.BD_USUARIO,
                MainMultiservidor.BD_PASS
            )

            bdCuentas?.autoCommit = false
            if (!bdCuentas?.isValid(1000)!!) {
                MainMultiservidor.escribirLog("SQLError : Conexion a la BDD invalida")
                return false
            }
            necesitaHacerTransaccion = false
            TIMER(true)
            return true
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return false
    }

    // public static void UPDATE_CUENTAS_LOG_CERO() {
// String consultaSQL = "UPDATE `cuentas` SET `logeado`= 0;";
// try {
// final PreparedStatement declaracion = transaccionSQL(consultaSQL);
// declaracion.executeUpdate();
// cerrarDeclaracion(declaracion);
// } catch (final Exception e) {
// System.out.println("ERROR SQL: " + e.toString());
// MainMultiservidor.escribirLog("LINEA SQL: " + consultaSQL);
// e.printStackTrace();
// }
// }
//
// public static int GET_CUENTAS_CONECTADAS_IP(final String ip) {
// int i = 0;
// try {
// String consultaSQL = "SELECT * FROM `cuentas` WHERE `ultimaIP` = '" + ip +
// "' AND `logeado` = '1' ;";
// final ResultSet resultado = consultaSQL(consultaSQL);
// while (resultado.next()) {
// i++;
// }
// cerrarResultado(resultado);
// } catch (final Exception e) {
// MainMultiservidor.escribirLog("ERROR SQL: " + e.toString());
// e.printStackTrace();
// }
// return i;
// }
    fun ACTUALIZAR_DATOS(infos: Array<String>, id: Int, out: PrintWriter?): Boolean {
        val datos = arrayOfNulls<String>(8)
        for (s in 0..7) {
            try {
                var t = infos[s]
                if (t.length > 40) {
                    t = t.substring(0, 40)
                }
                if (s == 0) {
                    if (!t.contains("@")) {
                        GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(out, "Email invalido")
                        return false
                    }
                } else if (t.contains("@")) {
                    GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(out, "Campos con caracteres invalidos")
                    return false
                }
                if (t.isEmpty()) {
                    GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(out, "Campos vacios")
                    return false
                }
                datos[s] = t
            } catch (e: Exception) {
                return false
            }
        }
        val consultaSQL = "UPDATE `cuentas` SET `email`= ?, `apellido`= ?, `nombre`= ?, `cumpleaños`= ?, `pregunta`= ?, `respuesta`= ?, `actualizar`='1' WHERE `id`= ?"
        try {
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, datos[0])
            declaracion.setString(2, datos[1])
            declaracion.setString(3, datos[2])
            declaracion.setString(4, datos[3].toString() + "~" + datos[4] + "~" + datos[5])
            declaracion.setString(5, datos[6])
            declaracion.setString(6, datos[7])
            declaracion.setInt(7, id)
            declaracion.executeUpdate()
            cerrarDeclaracion(declaracion)
            return true
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            MainMultiservidor.escribirLog("LINEA SQL: $consultaSQL")
            e.printStackTrace()
        }
        return false
    }

    fun GET_BANEADO(cuenta: String?): Long {
        var i: Long = 0
        try {
            val consultaSQL = "SELECT `baneado` FROM `cuentas` WHERE `cuenta` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                i = resultado.getLong("baneado")
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return i
    }

    fun GET_IDIOMA(cuenta: String?): String {
        var str = ""
        try {
            val consultaSQL = "SELECT `idioma` FROM `cuentas` WHERE `cuenta` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                str = resultado.getString("idioma")
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return str
    }

    fun SET_BANEADO(cuenta: String?, baneado: Long) {
        val consultaSQL = "UPDATE `cuentas` SET `baneado` = ? WHERE `cuenta` = ? ;"
        try {
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setLong(1, baneado)
            declaracion.setString(2, cuenta)
            declaracion.executeUpdate()
            cerrarDeclaracion(declaracion)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            MainMultiservidor.escribirLog("LINEA SQL: $consultaSQL")
            e.printStackTrace()
        }
    }

    fun GET_ACTUALIZAR(cuenta: String?): Byte {
        var i: Byte = 0
        try {
            val consultaSQL = "SELECT `actualizar` FROM `cuentas` WHERE `cuenta` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                i = resultado.getByte("actualizar")
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return i
    }

    fun GET_RANGO(cuenta: String?): Byte {
        var i: Byte = 0
        try {
            val consultaSQL = "SELECT `rango` FROM `cuentas` WHERE `cuenta` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                i = resultado.getByte("rango")
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return i
    }

    fun GET_ULTIMA_IP(cuenta: String?): String {
        var str = ""
        try {
            val consultaSQL = "SELECT `ultimaIP` FROM `cuentas` WHERE `cuenta` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                str = resultado.getString("ultimaIP")
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return str
    }

    fun GET_PREGUNTA_SECRETA(cuenta: String?): String {
        var str = ""
        try {
            val consultaSQL = "SELECT `pregunta` FROM `cuentas` WHERE `cuenta` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                str = resultado.getString("pregunta")
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return str
    }

    fun GET_RESPUESTA_SECRETA(cuenta: String?): String {
        var str = ""
        try {
            val consultaSQL = "SELECT `respuesta` FROM `cuentas` WHERE `cuenta` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                str = resultado.getString("respuesta")
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return str
    }

    fun GET_APODO(cuenta: String?): String {
        var str = ""
        try {
            val consultaSQL = "SELECT `apodo` FROM `cuentas` WHERE `cuenta` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                str = resultado.getString("apodo")
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return str
    }

    fun GET_APELLIDO(cuenta: String?): String {
        var str = ""
        try {
            val consultaSQL = "SELECT `apellido` FROM `cuentas` WHERE `cuenta` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                str = resultado.getString("apellido")
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return str
    }

    fun GET_APODO_EXISTE(apodo: String?): Boolean {
        var str = false
        try {
            val consultaSQL = "SELECT * FROM `cuentas` WHERE `apodo` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, apodo)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                str = true
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return str
    }

    fun GET_ABONO(cuenta: String?): Long {
        var l: Long = 0
        try {
            val consultaSQL = "SELECT `abono` FROM `cuentas` WHERE `cuenta` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            val resultado = consultaSQL(declaracion)
            while (resultado.next()) {
                l = resultado.getLong("abono")
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return l
    }

    fun GET_ID_CUENTA_NOMBRE(cuenta: String?): Int {
        var i = 0
        try {
            val consultaSQL = "SELECT `id` FROM `cuentas` WHERE `cuenta` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                i = resultado.getInt("id")
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return i
    }

    fun GET_CONTRASEÑA_CUENTA(cuenta: String?): String {
        var str = ""
        try {
            val consultaSQL = "SELECT `pass` FROM `cuentas` WHERE `cuenta` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                str = resultado.getString("pass")
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return str
    }

    fun GET_CONTRASEÑA_SI(cuenta: String?, respuesta: String?, email: String?,
                          cumpleaños: String?): String {
        var str = ""
        try {
            val consultaSQL = "SELECT `pass` FROM `cuentas` WHERE `cuenta` = ? AND `respuestas` = ? AND `email` = ? AND `cumpleaños` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            declaracion.setString(2, respuesta)
            declaracion.setString(3, email)
            declaracion.setString(4, cumpleaños)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                str = resultado.getString("pass")
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return str
    }

    fun CAMBIAR_CONTRASEÑA(cuenta: String?, email: String?, respuesta: String?,
                           nuevaPass: String, id: Int): String {
        var str = ""
        try {
            val consultaSQL = "SELECT `pass` FROM `cuentas` WHERE `cuenta` = ? AND `respuesta` = ? AND `email` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            declaracion.setString(2, respuesta)
            declaracion.setString(3, email)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                try {
                    CAMBIAR_CONTRASEÑA_CUENTA(nuevaPass, id)
                    str = nuevaPass
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return str
    }

    private fun CAMBIAR_CONTRASEÑA_CUENTA(pass: String, cuentaID: Int) {
        val consultaSQL = "UPDATE `cuentas` SET `pass`= ? WHERE `id`= ? ;"
        try {
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, pass)
            declaracion.setInt(2, cuentaID)
            declaracion.executeUpdate()
            cerrarDeclaracion(declaracion)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            MainMultiservidor.escribirLog("LINEA SQL: $consultaSQL")
        }
    }

    fun UPDATE_ULTIMA_IP(ultimaIP: String?, cuentaID: Int) {
        val consultaSQL = "UPDATE `cuentas` SET `ultimaIP`= ? WHERE `id`= ? ;"
        try {
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, ultimaIP)
            declaracion.setInt(2, cuentaID)
            declaracion.executeUpdate()
            cerrarDeclaracion(declaracion)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            MainMultiservidor.escribirLog("LINEA SQL: $consultaSQL")
        }
    }

    fun UPDATE_APODO(apodo: String?, cuentaID: Int) {
        val consultaSQL = "UPDATE `cuentas` SET `apodo`= ? WHERE `id`= ? ;"
        try {
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, apodo)
            declaracion.setInt(2, cuentaID)
            declaracion.executeUpdate()
            cerrarDeclaracion(declaracion)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            MainMultiservidor.escribirLog("LINEA SQL: $consultaSQL")
        }
    }

    fun INSERT_BAN_IP(ip: String?) {
        val consultaSQL = "INSERT INTO `banip` (ip) VALUES (?);"
        try {
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, ip)
            declaracion.executeUpdate()
            cerrarDeclaracion(declaracion)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            MainMultiservidor.escribirLog("LINEA SQL: $consultaSQL")
            e.printStackTrace()
        }
    }

    fun ES_IP_BANEADA(ip: String?): Boolean {
        var b = false
        try {
            val consultaSQL = "SELECT `ip` FROM `banip` WHERE `ip` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, ip)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                b = true
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
        return b
    }

    fun CARGAR_CUENTAS() {
        try {
            val consultaSQL = "SELECT * from cuentas ;"
            val resultado = consultaSQL(consultaSQL)
            while (resultado.next()) {
                val cuenta = Cuenta(resultado.getInt("id"), resultado.getString("cuenta").lowercase(), resultado
                        .getString("apodo"))
                Mundo.addCuenta(cuenta)
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
    }

    fun CARGAR_CUENTA_POR_NOMBRE(cuenta: String?) {
        try {
            val consultaSQL = "SELECT * from cuentas WHERE `cuenta` = ? ;"
            val declaracion = transaccionSQL(consultaSQL)
            declaracion.setString(1, cuenta)
            val resultado = consultaSQL(declaracion)
            if (resultado.first()) {
                val cc = Cuenta(resultado.getInt("id"), resultado.getString("cuenta").lowercase(), resultado
                        .getString("apodo"))
                Mundo.addCuenta(cc)
            }
            cerrarResultado(resultado)
        } catch (e: Exception) {
            MainMultiservidor.escribirLog("ERROR SQL: $e")
            e.printStackTrace()
        }
    }
}