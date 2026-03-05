package estaticos

import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.system.exitProcess

internal class Consola : Thread() {
    override fun run() {
        while (CONSOLA_ACTIVADA) {
            try {
                val b = BufferedReader(InputStreamReader(System.`in`))
                var linea = b.readLine()
                var str = ""
                try {
                    str = linea.substring(linea.indexOf(" ") + 1)
                    linea = linea.split(" ".toRegex()).toTypedArray()[0]
                } catch (ignored: Exception) {
                }
                leerComandos(linea, str)
            } catch (e: Exception) {
                println("Error al ingresar texto a la consola")
            }
        }
    }

    companion object {
        private var CONSOLA_ACTIVADA = true
        private fun leerComandos(linea: String, valor: String) {
            try {
                when (linea.toUpperCase()) {
                    "ENVIADOS" -> MainMultiservidor.MOSTRAR_ENVIOS = valor.equals("true", ignoreCase = true)
                    "RECIBIDOS" -> MainMultiservidor.MOSTRAR_RECIBIDOS = valor.equals("true", ignoreCase = true)
                    "DEBUG" -> MainMultiservidor.MODO_DEBUG = valor.equals("true", ignoreCase = true)
                    "DESACTIVAR", "DESACTIVE", "DESACTIVER" -> {
                        CONSOLA_ACTIVADA = false
                        println("CONSOLA DESACTIVADA")
                    }
                    "RELOG", "RECARGAR" -> {
                        MainMultiservidor.cargarConfiguracion()
                        println("Se recargo la config correctamente")
                    }
                    "EXIT", "RESET" -> exitProcess(0)
                    else -> {
                        println("Comando no existe")
                        return
                    }
                }
                println("Comando realizado: $linea -> $valor")
            } catch (e: Exception) {
                System.err.println("Ocurrio un error con el comando $linea")
            }
        }
    }

    init {
        this.isDaemon = true
        priority = 7
        start()
    }
}