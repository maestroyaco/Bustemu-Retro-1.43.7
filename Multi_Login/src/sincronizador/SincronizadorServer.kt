package sincronizador

import estaticos.MainMultiservidor
import java.net.ServerSocket

class SincronizadorServer : Runnable {
    override fun run() {
        try {
            while (true) {
                val socket = _serverSocket!!.accept()
                SincronizadorSocket(socket)
            }
        } catch (ignored: Exception) {
        }
    }

    companion object {
        private var _serverSocket: ServerSocket? = null
    }

    init {
        try {
            _serverSocket = ServerSocket(MainMultiservidor.PUERTO_SINCRONIZADOR)
            val _thread = Thread(this)
            _thread.isDaemon = true
            _thread.start()
        } catch (e: Exception) {
            if (MainMultiservidor.MOSTRAR_SINCRONIZACION) {
                println("NO SE PUEDE CREAR EL SINCRONIZADOR SERVER")
            }
            e.printStackTrace()
        }
    }
}