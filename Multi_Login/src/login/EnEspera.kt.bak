package login

import estaticos.GestorSalida
import variables.Cuenta
import java.io.PrintWriter
import java.util.*

internal object EnEspera {
    private val _cuentas = ArrayList<Cuenta>()
    @JvmStatic
    fun enEspera(pendiente: Int, out: PrintWriter?) {
        try {
            GestorSalida.ENVIAR_Af_NUEVA_COLA(out, pendiente + 1, 0, 0, "", -1)
        } catch (e: Exception) {
            GestorSalida.ENVIAR_AlEd_DESCONECTAR_CUENTA_CONECTADA(out)
        }
    }

    @JvmStatic
    fun getIndexOf(cuenta: Cuenta?): Int {
        return _cuentas.indexOf(cuenta)
    }

    @JvmStatic
    fun addEspera(cuenta: Cuenta) {
        if (!_cuentas.contains(cuenta)) {
            _cuentas.add(cuenta)
        }
    }

    @JvmStatic
    fun delEspera(cuenta: Cuenta?) {
        if (cuenta != null) {
            cuenta.pararTimer()
            _cuentas.remove(cuenta)
        }
    }

    @JvmStatic
    fun suTurno(cuenta: Cuenta, _out: PrintWriter?) {
        delEspera(cuenta)
        GestorSalida.ENVIAR_Ad_Ac_AH_AlK_AQ_INFO_CUENTA_Y_SERVER(_out, cuenta.apodo, if (cuenta.rango > 0) 1 else 0,
                cuenta.pregunta)
    }
}