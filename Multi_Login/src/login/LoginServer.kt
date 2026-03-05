package login

import estaticos.GestorSQL
import estaticos.GestorSalida
import estaticos.MainMultiservidor
import estaticos.Mundo
import variables.Servidor
import java.io.IOException
import java.net.ServerSocket
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import kotlin.system.exitProcess

class LoginServer : Runnable {
    override fun run() {
        try {
            while (true) {
                val socket = _serverSocket!!.accept()
                val ip = socket.inetAddress.hostAddress
                if (MainMultiservidor.PARAM_MOSTRAR_IP) {
                    println("SE ESTA CONECTANDO LA IP $ip")
                }
                if (BLOQUEADO || GestorSQL.ES_IP_BANEADA(ip)) {
                    try {
                        socket.close()
                    } catch (ignored: Exception) {
                    }
                    continue
                }
                if (Tiempos[ip] != null
                        && Tiempos[ip]!! + MainMultiservidor.MILISEGUNDOS_SIG_CONEXION > System.currentTimeMillis()) {
                    try {
                        socket.close()
                    } catch (ignored: Exception) {
                    }
                    continue
                }
                Tiempos[ip] = System.currentTimeMillis()
                if (MainMultiservidor.PARAM_ANTI_DDOS) {
                    _ataques[_j.toInt()]++
                    _alterna += 1
                    if (_alterna == 1) {
                        _primeraIp = ip
                        if (_ban) {
                            _tiempoBan1 = System.currentTimeMillis()
                        } else {
                            _tiempoBan2 = System.currentTimeMillis()
                        }
                        _ban = !_ban
                    } else if (_alterna == 2) {
                        _segundaIp = ip
                        if (_ban) {
                            _tiempoBan1 = System.currentTimeMillis()
                        } else {
                            _tiempoBan2 = System.currentTimeMillis()
                        }
                        _ban = !_ban
                    } else {
                        _terceraIp = ip
                        _alterna = 0
                        if (_ban) {
                            _tiempoBan1 = System.currentTimeMillis()
                        } else {
                            _tiempoBan2 = System.currentTimeMillis()
                        }
                        _ban = !_ban
                    }
                    if (_primeraIp == ip && _segundaIp == ip && _terceraIp == ip && abs(_tiempoBan1 - _tiempoBan2) < 200) {
                        GestorSQL.INSERT_BAN_IP(ip)
                        try {
                            socket.close()
                        } catch (ignored: Exception) {
                        }
                        continue
                    }
                }
                LoginSocket(socket)
            }
        } catch (e: IOException) {
            MainMultiservidor.escribirLog("ERROR EN EL LOGIN SERVER")
            e.printStackTrace()
        } finally {
            try {
                MainMultiservidor.escribirLog("CIERRE DEL LOGIN SERVER")
                if (!_serverSocket!!.isClosed) {
                    _serverSocket!!.close()
                }
            } catch (ignored: IOException) {
            }
        }
    }

    internal class PacketConexion(private val _cliente: LoginSocket, private val _servidor: Servidor) : Thread() {
        override fun run() {
            try {
                if (_servidor.conector == null) {
                    return
                }
                _servidor.conector!!.sendPacket(_cliente.packetConexion)
                sleep(1000)
                if (_cliente.cuenta == null) {
                    return
                }
                GestorSalida.ENVIAR_AxK_TIEMPO_ABONADO_NRO_PJS(_cliente.out, _cliente.cuenta!!)
            } catch (ignored: Exception) {
            }
        }

        init {
            this.isDaemon = true
            start()
        }
    }

    internal class AntiDDos : Thread() {
        override fun run() {
            if (MainMultiservidor.PARAM_ANTI_DDOS) {
                val _minAtaque = 25
                if (!BLOQUEADO && _ataques[0] > _minAtaque && _ataques[1] > _minAtaque && _ataques[2] > _minAtaque) {
                    BLOQUEADO = true
                    System.err.println("EL SERVIDOR ESTA SIENDO ATACADO EN UNOS MINUTOS SE RESTABLECERA A SU ESTADO NORMAL")
                } else if (BLOQUEADO && _ataques[0] < _minAtaque && _ataques[1] < _minAtaque && _ataques[2] < _minAtaque) {
                    BLOQUEADO = false
                    System.err.println("EL SERVIDOR HA SIDO RESTABLECIDO, ATAQUE TERMINADO")
                }
                _j = (_segundosON % 3).toByte()
                _ataques[_j.toInt()] = 0
            }
        }

        init {
            this.isDaemon = true
            start()
        }
    }

    companion object {
        @JvmField
        val Tiempos: MutableMap<String, Long> = TreeMap()
        private val _clientesEscogerServer = CopyOnWriteArrayList<LoginSocket>()
        private val _clientes = CopyOnWriteArrayList<LoginSocket>()
        private val _IpsClientes: MutableMap<String, ArrayList<LoginSocket>> = ConcurrentHashMap()
        private val _ataques = IntArray(3)
        private var _serverSocket: ServerSocket? = null
        private var _j: Byte = 0
        private var _alterna = 0
        private var _tiempoBan1: Long = 0
        private var _tiempoBan2: Long = 0
        private var _segundosON: Long = 0
        private var _primeraIp = ""
        private var _segundaIp = ""
        private var _terceraIp = ""
        private var _ban = true
        private var BLOQUEADO = false
        @JvmStatic
        fun enviarPacketConexionServidor(servidor: Servidor) {
            for (cliente in _clientes) {
                PacketConexion(cliente, servidor)
            }
        }

        @JvmStatic
        fun refreshServersEstado() {
            for (cliente in _clientesEscogerServer) {
                try {
                    GestorSalida.ENVIAR_AH_ESTADO_SERVIDORES(cliente.out)
                } catch (e: Exception) {
                    _clientesEscogerServer.remove(cliente)
                }
            }
        }

        @JvmStatic
        fun addCliente(cliente: LoginSocket?) {
            if (cliente == null) {
                return
            }
            addIPsClientes(cliente)
            _clientes.add(cliente)
        }

        @JvmStatic
        fun borrarCliente(cliente: LoginSocket?) {
            if (cliente == null) {
                return
            }
            borrarIPsClientes(cliente)
            _clientes.remove(cliente)
        }

        @JvmStatic
        fun borrarEscogerServer(cliente: LoginSocket?) {
            _clientesEscogerServer.remove(cliente)
        }

        @JvmStatic
        fun addEscogerServer(cliente: LoginSocket) {
            if (!_clientesEscogerServer.contains(cliente)) {
                _clientesEscogerServer.add(cliente)
            }
        }

        @JvmStatic
        fun getCantidadIps(ip: String): Int {
            var cant = getIPsClientes(ip)
            for (server in Mundo.Servidores.values) {
                if (server != null) {
                    cant += server.getCantidadPorIP(ip)!!
                }
            }
            return cant
        }

        private fun addIPsClientes(s: LoginSocket) {
            val ip = s.actualIP
            if (ip != null) {
                _IpsClientes.computeIfAbsent(ip) { k: String? -> ArrayList() }
            }
            if (!_IpsClientes[ip]!!.contains(s)) {
                _IpsClientes[ip]!!.add(s)
            }
        }

        private fun borrarIPsClientes(s: LoginSocket) {
            val ip = s.actualIP
            if (_IpsClientes[ip] == null) {
                return
            }
            _IpsClientes[ip]!!.remove(s)
        }

        private fun getIPsClientes(ip: String): Int {
            return if (_IpsClientes[ip] == null) {
                0
            } else _IpsClientes[ip]!!.size
        }

        fun cerrarServidorGeneral() {
            try {
                _serverSocket!!.close()
            } catch (ignored: Exception) {
            }
        }
    }

    init {
        try {
            if (MainMultiservidor.PARAM_ANTI_DDOS) {
                val cuentaRegresiva = Timer()
                cuentaRegresiva.schedule(object : TimerTask() {
                    override fun run() {
                        _segundosON += 1
                        AntiDDos()
                    }
                }, 1000, 1000)
            }
            val autoSelect = Timer()
            autoSelect.schedule(object : TimerTask() {
                override fun run() {
                    GestorSQL.ES_IP_BANEADA("111.222.333.444") // para usar el sql y q no se crashee
                }
            }, 300000, 300000)
            if (MainMultiservidor.SEGUNDOS_INFO_STATUS > 0) {
                val infoStatus = Timer()
                infoStatus.schedule(object : TimerTask() {
                    override fun run() {
                        MainMultiservidor.infoStatus()
                    }
                }, MainMultiservidor.SEGUNDOS_INFO_STATUS * 1000.toLong(), MainMultiservidor.SEGUNDOS_INFO_STATUS * 1000.toLong())
            }
            if (MainMultiservidor.SEGUNDOS_ESTADISTICAS > 0) {
                val estadisticas = Timer()
                estadisticas.schedule(object : TimerTask() {
                    override fun run() {
                        MainMultiservidor.escribirEstadisticas()
                    }
                }, MainMultiservidor.SEGUNDOS_ESTADISTICAS * 1000.toLong(), MainMultiservidor.SEGUNDOS_ESTADISTICAS * 1000.toLong())
            }
            val estadisticas = Timer()
            estadisticas.schedule(object : TimerTask() {
                override fun run() {
                    refreshServersEstado()
                }
            }, 30 * 1000.toLong(), 30 * 1000.toLong())
            _serverSocket = ServerSocket(MainMultiservidor.PUERTO_MULTISERVIDOR)
            val _thread = Thread(this)
            _thread.isDaemon = true
            _thread.start()
            println("\n ----- Multiservidor Abierto Puerto " + MainMultiservidor.PUERTO_MULTISERVIDOR
                    + " ----- \n")
        } catch (e: IOException) {
            e.printStackTrace()
            MainMultiservidor.escribirLog("ERROR AL CREAR EL SERVIDOR GENERAL$e")
            exitProcess(1)
        }
    }
}