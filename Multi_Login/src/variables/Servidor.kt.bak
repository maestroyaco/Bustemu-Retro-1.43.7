package variables

import sincronizador.SincronizadorSocket
import java.util.concurrent.ConcurrentHashMap

class Servidor(val id: Int, var puerto: Int, var estado: Int) {
    private val _ips: MutableMap<String, Int> = ConcurrentHashMap()
    var ip = "127.0.0.1"
    var conector: SincronizadorSocket? = null
    var conectados = 0
    private var _prioridad = 0
    fun setCantidadIp(ip: String, cant: Int) {
        _ips[ip] = cant
    }

    fun getCantidadPorIP(ip: String?): Int? {
        return if (_ips[ip] == null) {
            0
        } else _ips[ip]
    }

    fun setPrioridad(prioridad: Int) {
        _prioridad = prioridad
    }

    val stringParaAH: String
        get() {
            val _puedeLoguear = true
            return "$id;$estado;$_prioridad;1"
        }

    companion object {
        const val SERVIDOR_OFFLINE = 0
        const val SERVIDOR_ONLINE = 1
        const val SERVIDOR_SAVING = 2
    }

}