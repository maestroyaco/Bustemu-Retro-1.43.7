package estaticos

import java.nio.charset.StandardCharsets
import kotlin.math.floor
import kotlin.math.pow

object Encriptador {
    const val NUMEROS = "0123456789"
    private val HASH = charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-',
            '_') // q = 16, N = 40, - = 63
    private const val ABC_MIN = "abcdefghijklmnopqrstuvwxyz"
    private const val ABC_MAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val VOCALES = "aeiouAEIOU"
    private const val CONSONANTES = "bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ"
    private const val GUIONES = "_-"
    fun palabraAleatorio(limite: Int): String {
        val nombre = StringBuilder()
        var i = floor(Math.random() * ABC_MAY.length).toInt()
        var temp = ABC_MAY[i]
        nombre.append(temp)
        var xxx: Char
        while (nombre.length < limite) {
            i = floor(Math.random() * ABC_MIN.length).toInt()
            xxx = ABC_MIN[i]
            if (temp == xxx || VOCALES.contains(temp.toString() + "") && VOCALES.contains(xxx.toString() + "")
                    || CONSONANTES.contains(temp.toString() + "") && CONSONANTES.contains(xxx.toString() + "")) {
                continue
            }
            temp = xxx
            nombre.append(xxx)
        }
        return nombre.toString()
    }

    fun filtro(s: String): String {
        val filtrado = StringBuilder()
        val filtros = charArrayOf('\'', '\"', '\\', '=', '#', '/', '!', '`', '+', '$', '%')
        for (x in s.toCharArray()) {
            var paso = true
            for (f in filtros) {
                if (x == f) {
                    paso = false
                    break
                }
            }
            if (!paso) {
                continue
            }
            filtrado.append(x.toString() + "")
        }
        return filtrado.toString()
    }

    fun encriptarContraseña(key: String, contraseña: String): String {
        val encriptado = StringBuilder("#1")
        for (i in contraseña.indices) {
            val charPass = contraseña[i]
            val charKey = key[i]
            val a = charPass.toInt() / 16
            val b = charPass.toInt() % 16
            val a1 = (a + charKey.toInt()) % HASH.size
            val b1 = (b + charKey.toInt()) % HASH.size
            encriptado.append(HASH[a1])
            encriptado.append(HASH[b1])
        }
        return encriptado.toString()
    }

    fun desencriptarContraseña(contraseña: String, key: String): String {
        var l2: Int
        var l3: Int
        var l4: Int
        var l5: Int
        var l7 = ""
        val abecedario = ABC_MIN + ABC_MAY + GUIONES
        var l1 = 0
        while (l1 <= contraseña.length - 1) {
            l3 = key[l1 / 2].toInt()
            l2 = abecedario.indexOf(contraseña[l1])
            l4 = 64 + l2 - l3
            val l11 = l1 + 1
            l2 = abecedario.indexOf(contraseña[l11])
            l5 = 64 + l2 - l3
            if (l5 < 0) {
                l5 += 64
            }
            l7 += (16 * l4 + l5).toChar()
            l1 += 2
        }
        return l7
    }

    fun encriptarIP(IP: String): String {
        val split = IP.split("\\.".toRegex()).toTypedArray()
        val encriptado = StringBuilder()
        var cantidad = 0
        var i = 0
        while (i < 50) {
            var o = 0
            while (o < 50) {
                if (i and 15 shl 4 or o and 15 == split[cantidad].toInt()) {
                    val A = (i + 48).toChar()
                    val B = (o + 48).toChar()
                    encriptado.append(A.toString() + B)
                    i = 0
                    o = 0
                    cantidad++
                    if (cantidad == 4) {
                        return encriptado.toString()
                    }
                }
                o++
            }
            i++
        }
        return "DD"
    }

    fun encriptarPuerto(puerto: Int): String {
        var P = puerto
        val numero = StringBuilder()
        for (a in 2 downTo 0) {
            numero.append(HASH[(P / 64.0.pow(a.toDouble())).toInt()])
            P %= 64.0.pow(a.toDouble()).toInt()
        }
        return numero.toString()
    }

    fun celdaIDACodigo(celdaID: Int): String {
        val char1 = celdaID / 64
        val char2 = celdaID % 64
        return HASH[char1].toString() + "" + HASH[char2]
    }

    fun celdaCodigoAID(celdaCodigo: String): Short {
        val char1 = celdaCodigo[0]
        val char2 = celdaCodigo[1]
        var code1: Short = 0
        var code2: Short = 0
        var a: Short = 0
        while (a < HASH.size) {
            if (HASH[a.toInt()] == char1) {
                code1 = (a * 64).toShort()
            }
            if (HASH[a.toInt()] == char2) {
                code2 = a
            }
            a++
        }
        return (code1 + code2).toShort()
    }

    fun getNumeroPorValorHash(c: Char): Short {
        for (a in HASH.indices) {
            if (HASH[a] == c) {
                return a.toShort()
            }
        }
        return -1
    }

    fun getValorHashPorNumero(c: Int): Char {
        return HASH[c]
    }

    fun aUTF(entrada: String?): String {
        var out = ""
        try {
            out = String(entrada!!.toByteArray(StandardCharsets.UTF_8))
        } catch (e: Exception) {
            println("Conversion en UTF-8 fallida! : " + e.message)
        }
        return out
    }

    fun aUnicode(entrada: String): String {
        var out = ""
        try {
            out = String(entrada.toByteArray(), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            println("Conversion en UNICODE fallida! : " + e.message)
        }
        return out
    }
}