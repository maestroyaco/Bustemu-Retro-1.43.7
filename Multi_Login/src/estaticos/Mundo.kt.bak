package estaticos

import login.LoginSocket
import variables.Cuenta
import variables.Servidor
import java.util.concurrent.ConcurrentHashMap

object Mundo {
    val Servidores: MutableMap<Int?, Servidor?> = ConcurrentHashMap()
    private val Cuentas: MutableMap<Int, Cuenta> = ConcurrentHashMap()
    fun crearMultiServer() {
        println("Creación del MultiServidor:")
        print("Cargando las cuentas: ")
        GestorSQL.CARGAR_CUENTAS()
        println(Cuentas.size.toString() + " cuentas cargadas")
    }

    @Synchronized
    fun addCuenta(cuenta: Cuenta) {
        Cuentas[cuenta.id] = cuenta
    }

    fun getCuenta(id: Int): Cuenta? {
        return Cuentas[id]
    }

    fun getComunidadIdioma(idioma: String): Byte {
        return when (idioma.lowercase()) {
            "fr", "ch", "be", "lu" -> 0
            "uk", "ie", "gb" -> 1
            "xx" -> 2
            "de", "at", "li" -> 3
            "es", "ad", "ar", "ck", "mx" -> 4
            "ru" -> 5
            "pt", "br" -> 6
            "nl" -> 7
            "it" -> 9
            "jp" -> 10
            else -> 99
        }
    }

    fun packetParaAH(): String {
        val str = StringBuilder()
        for (servidor in Servidores.values) {
            if (str.isNotEmpty()) {
                str.append("|")
            }
            str.append(servidor!!.stringParaAH)
        }
        return str.toString()
    }

    fun enviarPacketsAServidores(cliente: LoginSocket) {
        val packet = cliente.packetConexion
        for (servidor in Servidores.values) {
            try {
                if (servidor!!.conector == null) {
                    continue
                }
                servidor.conector!!.sendPacket(packet)
            } catch (ignored: Exception) {
            }
        }
    }

    fun enviarCantidadIps(IP: String) {
        val packet = "I$IP"
        for (servidor in Servidores.values) {
            try {
                if (servidor!!.conector == null) {
                    continue
                }
                servidor.conector!!.sendPacket(packet)
            } catch (ignored: Exception) {
            }
        }
    }

    fun infoStatus(): String {
        val str = StringBuilder()
        for (server in Servidores.values) {
            try {
                val s = "\n\$server_" + server!!.id
                str.append(s + "_status = " + server.estado + ";")
                str.append(s + "_onlines = " + server.conectados + ";\n")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return str.toString()
    }
}