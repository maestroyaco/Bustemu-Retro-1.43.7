package sincronizador

import estaticos.Encriptador
import estaticos.MainMultiservidor
import estaticos.Mundo
import login.LoginServer.Companion.enviarPacketConexionServidor
import login.LoginServer.Companion.refreshServersEstado
import variables.Servidor
import java.io.BufferedInputStream
import java.io.PrintWriter
import java.net.Socket
import java.nio.charset.StandardCharsets

class SincronizadorSocket(socket: Socket?) : Runnable {
    private var _socket: Socket? = null
    private var _in: BufferedInputStream? = null
    private var _out: PrintWriter? = null
    private var _servidor: Servidor? = null
    private var _IP: String? = null
    override fun run() {
        try {
            try {
                Thread.sleep(1000)
            } catch (ignored: Exception) {
            }
            var c = -1
            var lenght = -1
            var index = 0
            var bytes = ByteArray(1)
            while (_in!!.read().also { c = it } != -1) {
                if (lenght == -1) {
                    lenght = _in!!.available()
                    bytes = ByteArray(lenght + 1)
                    index = 0
                }
                bytes[index++] = c.toByte()
                if (bytes.size == index) {
                    val tempPacket = String(bytes, StandardCharsets.UTF_8)
                    for (packet in tempPacket.split("[\u0000\n\r]".toRegex()).toTypedArray()) {
                        if (packet.isEmpty()) {
                            continue
                        }
                        analizarPackets(packet)
                    }
                    lenght = -1
                }
            }
        } catch (e: Exception) { // System.out.println("EXCEPTION RUN CONECTOR, IP: " + _IP + " PUERTO: " + _puerto);
        } finally {
            if (_servidor != null) {
                println("<<<--- CERRANDO CONEXION SERVIDOR ID " + _servidor!!.id + ", IP " + _IP + " --->>>")
            }
            desconectar()
        }
    }

    private fun analizarPackets(packet: String) {
        try {
            when (packet[0]) {
                'A' -> try {
                    val str = packet.substring(1).split(";".toRegex()).toTypedArray()
                    val cuentaID = str[0].toInt()
                    val cantPersonajes = str[1].toInt()
                    val cuenta = Mundo.getCuenta(cuentaID)
                    cuenta?.setPersonajes(_servidor!!.id, cantPersonajes)
                } catch (ignored: Exception) {
                }
                'C' -> try {
                    val conectados = packet.substring(1).toInt()
                    if (_servidor != null) {
                        _servidor!!.conectados = conectados
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                'D' -> try {
                    val str = packet.substring(1).split(";".toRegex()).toTypedArray()
                    val servidorID = str[0].toInt()
                    val puerto = str[1].toInt()
                    val prioridad = str[2].toInt()
                    val estado = str[3].toInt()
                    if (str.size > 4) {
                        _IP = str[4]
                    }
                    var servidor = Mundo.Servidores[servidorID]
                    if (servidor == null) {
                        servidor = Servidor(servidorID, puerto, Servidor.SERVIDOR_OFFLINE)
                        Mundo.Servidores[servidorID] = servidor
                    } else {
                        if (servidor.puerto != puerto) {
                            servidor.puerto = puerto
                        }
                    }
                    _servidor = servidor
                    _servidor!!.ip = _IP.toString()
                    _servidor!!.setPrioridad(prioridad)
                    _servidor!!.estado = estado
                    _servidor!!.conector = this
                    refreshServersEstado()
                    enviarPacketConexionServidor(_servidor!!)
                    println("<<<--- INICIANDO CONEXION SERVIDOR ID " + _servidor!!.id + ", IP " + _IP + " --->>>")
                } catch (ignored: Exception) {
                }
                'I' -> try {
                    if (_servidor != null) {
                        val str = packet.substring(1).split(";".toRegex()).toTypedArray()
                        val ip = str[0]
                        val cantidad = str[1].toInt()
                        _servidor!!.setCantidadIp(ip, cantidad)
                    }
                } catch (ignored: Exception) {
                }
                'S' -> try {
                    val estado = packet.substring(1).toInt()
                    if (_servidor != null) {
                        if (estado == Servidor.SERVIDOR_OFFLINE) {
                            desconectar()
                        } else {
                            _servidor!!.estado = estado
                            refreshServersEstado()
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
        } catch (ignored: Exception) {
        }
    }

    fun estaCerrado(): Boolean {
        return _socket == null || _socket!!.isClosed
    }

    private fun desconectar() {
        try {
            if (_socket != null && !_socket!!.isClosed) {
                _socket!!.close()
            }
            if (_servidor != null) {
                _servidor!!.conector = null
                _servidor!!.estado = Servidor.SERVIDOR_OFFLINE
                refreshServersEstado()
            }
            _in?.close()
            _out?.close()
        } catch (ignored: Exception) {
        }
    }

    fun sendPacket(packet: String) {
        var packet = packet
        if (_out != null && packet.isNotEmpty() && packet != "" + 0x00.toChar()) {
            packet = Encriptador.aUTF(packet)
            try {
                println("ENVIAR PACKET SERVIDOR (" + _servidor!!.id + ") >> " + packet)
            } catch (ignored: Exception) {
            }
            _out!!.print(packet + 0x00.toChar())
            _out!!.flush()
        }
    }

    init {
        try {
            _socket = socket
            _IP = _socket!!.inetAddress.hostAddress
            if (MainMultiservidor.MOSTRAR_SINCRONIZACION) {
                println("INTENTO SINCRONIZAR IP: $_IP")
            }
            _in = BufferedInputStream(_socket!!.getInputStream())
            _out = PrintWriter(_socket!!.getOutputStream())
            val _thread = Thread(this)
            _thread.isDaemon = true
            _thread.start()
        } catch (e: Exception) {
            if (MainMultiservidor.MOSTRAR_SINCRONIZACION) {
                println("INTENTO FALLIDO -> $e")
            }
            desconectar()
        }
    }
}