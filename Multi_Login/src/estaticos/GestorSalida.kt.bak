package estaticos

import variables.Cuenta
import java.io.PrintWriter
import java.util.*

object GestorSalida {
    fun enviar(out: PrintWriter?, packet: String?) {
        var packet = packet
        if (out != null && packet!!.isNotEmpty() && packet != "" + 0x00.toChar()) {
            packet = Encriptador.aUTF(packet)
            out.print(packet + 0x00.toChar())
            out.flush()
        }
    }

    fun ENVIAR_XML_POLICY_FILE(out: PrintWriter?) {
        val packet = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" //
                + "<cross-domain-policy>" + "<site-control permitted-cross-domain-policies=\"all\" />"
                + "<allow-access-from domain=\"*\" to-ports=\"*\" secure=\"false\" />"
                + "<allow-http-request-headers-from domain=\"*\" headers=\"*\" secure=\"false\"/>" // s
                + "</cross-domain-policy>")
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("POLICY FILE: OUT>>  $packet")
        }
    }

    fun ENVIAR_pong(out: PrintWriter?) {
        val packet = "pong"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("DOFUS PONG: PERSO>>  $packet")
        }
    }

    fun ENVIAR_Bv_SONAR_MP3(_perso: PrintWriter?, str: String) {
        val packet = "Bv$str"
        enviar(_perso, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("SONAR MP3: PERSO>>  $packet")
        }
    }

    fun ENVIAR_ÑV_ACTUALIZAR_URL_LINK_MP3(perso: PrintWriter?) {
        val packet = "ÑV" + MainMultiservidor.URL_LINK_MP3
        enviar(perso, packet)
    }

    fun ENVIAR_qpong(out: PrintWriter?) {
        val packet = "qpong"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("DOFUS QPONG: PERSO>>  $packet")
        }
    }

    fun ENVIAR_HC_CODIGO_LLAVE(out: PrintWriter?): String {
        val alfabeto = "abcdefghijklmnopqrstuvwxyz"
        val rand = Random()
        val codigoLlave = StringBuilder()
        for (i in 0..31) {
            codigoLlave.append(alfabeto[rand.nextInt(alfabeto.length)])
        }
        val packet = "HC$codigoLlave"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("CODIGO LLAVE: OUT>>$packet")
        }
        return codigoLlave.toString()
    }

    fun ENVIAR_HR_RECUPERAR_CUENTA(out: PrintWriter?, str: String) {
        val packet = "HR$str"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("RECUPERAR CUENTA: OUT>>  $packet")
        }
    }

    fun ENVIAR_HN_CAMBIAR_PASSWORD(out: PrintWriter?, str: String) {
        val packet = "HN$str"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("CAMBIAR PASSWORD: OUT>>  $packet")
        }
    }

    fun ENVIAR_HF_CONFIRMAR_NUEVA_PASSWORD(out: PrintWriter?, str: String) {
        val packet = "HF$str"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("CONFIRMA NUEVA PASS: OUT>>  $packet")
        }
    }

    fun ENVIAR_HP_PASS_ENVIADA(out: PrintWriter?, str: String) {
        val packet = "HP$str"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("PASS ENVIADA: OUT>>  $packet")
        }
    }

    fun ENVIAR_AlEk_CUENTA_BANEADA_TIEMPO(out: PrintWriter?, tiempo: Long) {
        var tiempo = tiempo
        tiempo -= System.currentTimeMillis()
        val dia = (tiempo / (1000 * 3600 * 24)).toInt()
        tiempo %= 1000 * 3600 * 24.toLong()
        val horas = (tiempo / (1000 * 3600)).toInt()
        tiempo %= 1000 * 3600.toLong()
        val min = (tiempo / (1000 * 60)).toInt()
        val packet = "AlEk$dia|$horas|$min"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("CUENTA BANEADA TIEMPO: CONEXION>>$packet")
        }
    }

    fun ENVIAR_AlEb_CUENTA_BANEADA_DEFINITIVO(out: PrintWriter?) {
        val packet = "AlEb"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("CUENTA BANEADA DEFINITIVA: CONEXION>>$packet")
        }
    }

    fun ENVIAR_AlEv_ERROR_VERSION_DEL_CLIENTE(out: PrintWriter?) {
        val packet = "AlEv" + MainMultiservidor.VERSION_CLIENTE
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("ERROR VERSION CLIENTE: CONEXION>>$packet")
        }
    }

    fun ENVIAR_AlEw_MUCHOS_JUG_ONLINE(out: PrintWriter?) {
        val packet = "AlEw"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("MAX JUG ONLINE: CONEXION>>  $packet")
        }
    }

    fun ENVIAR_AlEx_NOMBRE_O_PASS_INCORRECTA(out: PrintWriter?) {
        val packet = "AlEx"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("NOMBRE O PASS INCORRECTA: CONEXION>>$packet")
        }
    }

    fun ENVIAR_AlEp_CUENTA_NO_VALIDA(out: PrintWriter?) {
        val packet = "AlEp"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("CUENTA NO VALIDA: CONEXION>>$packet")
        }
    }

    fun ENVIAR_AlEn_CONEXION_NO_TERMINADA(out: PrintWriter?) {
        val packet = "AlEn"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("CONEXION NO TERMINADA: CONEXION>>$packet")
        }
    }

    fun ENVIAR_AlEd_DESCONECTAR_CUENTA_CONECTADA(out: PrintWriter?) {
        val packet = "AlEd"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("MISMA CUENTA CONECTADA: CONEXION>>$packet")
        }
    }

    fun ENVIAR_AlEm_SERVER_MANTENIMIENTO(out: PrintWriter?) {
        val packet = "AlEm"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("SERVER MANTENIMIENTO: CONEXION>>$packet")
        }
    }

    fun ENVIAR_Af_NUEVA_COLA(out: PrintWriter?, posicion: Int, totalAbo: Int,
                             totalNonAbo: Int, suscribirse: String, colaID: Int) {
        val packet = "Af$posicion|$totalAbo|$totalNonAbo|$suscribirse|$colaID"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("MULTIPAQUETES: CONEXION>>$packet")
        }
    }

    fun ENVIAR_Aq_MODIFICA_COLA(out: PrintWriter?, posicion: Int) {
        val packet = "Aq$posicion"
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("MULTIPAQUETES: CONEXION>>$packet")
        }
    }

    fun ENVIAR_Ad_Ac_AH_AlK_AQ_INFO_CUENTA_Y_SERVER(out: PrintWriter?, apodo: String,
                                                    rango: Int, pregunta: String) {
        val packet = StringBuilder()
        packet.append("Ad" + apodo + 0x00.toChar())
        packet.append("Ac" + "??" + 0x00.toChar()) // comunidad
        packet.append("AH" + Mundo.packetParaAH() + 0x00.toChar())
        packet.append("AlK" + rango + 0x00.toChar())
        packet.append("AQ" + pregunta.replace(" ", "+"))
        enviar(out, packet.toString())
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("CONEXION: OUT>>$packet")
        }
    }

    fun ENVIAR_AH_ESTADO_SERVIDORES(out: PrintWriter?) {
        enviar(out, "AH" + Mundo.packetParaAH())
    }

    fun ENVIAR_AxK_TIEMPO_ABONADO_NRO_PJS(out: PrintWriter?, cuenta: Cuenta) {
        val packet = "AxK" + cuenta.abono + cuenta.stringPersonajes
        enviar(out, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("TIEMPO ABON NRO PJS: OUT>>$packet")
        }
    }

    fun ENVIAR_AXK_O_AYK_IP_SERVER(out: PrintWriter?, cuentaID: Int, ipServer: String,
                                   puertoServer: Int) {
        val packet = StringBuilder("A")
        if (MainMultiservidor.ENCRIPTAR_IP) {
            packet.append("XK" + Encriptador.encriptarIP(ipServer) + Encriptador.encriptarPuerto(puertoServer) + cuentaID)
        } else {
            packet.append("YK$ipServer:$puertoServer;$cuentaID")
        }
        enviar(out, packet.toString())
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("CONEXION IP SERVER: OUT>>$packet")
        }
    }

    fun ENVIAR_BN_NADA(out: PrintWriter?) {
        enviar(out, "BN")
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("NADA: PERSO>>  " + "BN")
        }
    }

    fun ENVIAR_HU_ACTUALIZAR_DATOS(out: PrintWriter?) {
        enviar(out, "HU")
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("ACTUALIZAR DATOS: PERSO>>  " + "HU")
        }
    }

    fun ENVIAR_HV_CONFIRMADA_ACTUALIZACION_DATOS(out: PrintWriter?) {
        enviar(out, "Hu")
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("CONFIRMADA ACTUALIZACION DATOS: PERSO>>  " + "Hu")
        }
    }

    fun ENVIAR_M0_MENSAJE_SVR_MUESTRA_DISCONNECT(out: PrintWriter?, id: String, msj: String,
                                                 nombre: String) {
        val packet = "M0$id|$msj|$nombre"
        enviar(out, packet)
        enviar(out, "ATE")
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("MSJ SERVER: PERSO>>  $packet")
        }
    }

    fun ENVIAR_M145_MENSAJE_PANEL_INFORMACION(perso: PrintWriter?, str: String) {
        val packet = "M145|" + str.replace("\\n", "\n").replace("\\t", "\t").replace("\\r", "\r")
        enviar(perso, packet)
        if (MainMultiservidor.MOSTRAR_ENVIOS) {
            println("MSJ SERVER: PERSO>>  $packet")
        }
    }
}