package estaticos;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Pattern;
import variables.mapa.Celda;
import variables.mapa.Mapa;

public class Encriptador {
	private static final char[] HASH = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', // 15
	'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', // 38
	'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', // 61
	'-', '_'};// q = 16, N = 40, - = 63 _ = 64
	private static char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	public static final String ABC_MIN = "abcdefghijklmnopqrstuvwxyz";
	public static final String ABC_MAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String VOCALES = "aeiouAEIOU";
	public static final String CONSONANTES = "bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ";
	public static final String NUMEROS = "0123456789";
	public static final String ESPACIO = " ";
	public static final String GUIONES = "_-";
	
	public static String crearKey(final int limite) {
		final StringBuilder nombre = new StringBuilder();
		while (nombre.length() < limite) {
			nombre.append(HASH[Formulas.getRandomInt(0, HASH.length - 1)]);
		}
		final StringBuilder key = new StringBuilder();
		for (char c : nombre.toString().toCharArray()) {
			key.append(Integer.toHexString(c));
		}
		return key.toString();
	}
	
	public static String palabraAleatorio(final int limite) {
		final StringBuilder nombre = new StringBuilder();
		int i = (int) Math.floor(Math.random() * ABC_MAY.length());
		char temp = ABC_MAY.charAt(i);
		nombre.append(temp);
		char xxx;
		while (nombre.length() < limite) {
			i = (int) Math.floor(Math.random() * ABC_MIN.length());
			xxx = ABC_MIN.charAt(i);
			if (temp == xxx || (VOCALES.contains(temp + "") && VOCALES.contains(xxx + "")) || (CONSONANTES.contains(temp + "")
			&& CONSONANTES.contains(xxx + ""))) {
				continue;
			}
			temp = xxx;
			nombre.append(xxx);
		}
		return nombre.toString();
	}
	
	public static String stringParaGDC(boolean[] permisos, int[] valores) {
		// 16 var layerObjectExternalAutoSize = (_loc6 & 65536) != 0;
		// 15 var layerObjectExternalInteractive = (_loc6 & 32768) != 0;
		// 14 var layerObjectExternal = (_loc6 & 16384) != 0;
		// 13 var active = (_loc6 & 8192) != 0;
		// 12 var lineOfSight = (_loc6 & 4096) != 0;
		// 11 var movement = (_loc6 & 2048) != 0;
		// 10 var groundLevel = (_loc6 & 1024) != 0;
		// 9 var groundSlope = (_loc6 & 512) != 0;
		// 8 var layerGroundNum = (_loc6 & 256) != 0;
		// 7 var layerGroundFlip = (_loc6 & 128) != 0;
		// 6 var layerGroundRot = (_loc6 & 64) != 0;
		// 5 var layerObject1Num = (_loc6 & 32) != 0;
		// 4 var layerObject1Flip = (_loc6 & 16) != 0;
		// 3 var layerObject1Rot = (_loc6 & 8) != 0;
		// 2 var layerObject2Num = (_loc6 & 4) != 0;
		// 1 var layerObject2Flip = (_loc6 & 2) != 0;
		// 0 var layerObject2Interactive = (_loc6 & 1) != 0; << 0
		int i = 0;
		int finalPermiso = 0;
		for (boolean b : permisos) {
			if (b) {
				finalPermiso += (1 << i);
			}
			i++;
		}
		String fP = Integer.toHexString(finalPermiso);
		int[] preData = new int[10];
		preData[0] = ((valores[13] == 1) ? (1) : (0)) << 5;
		preData[0] = preData[0] | (valores[12] == 1 ? (1) : (0));
		preData[0] = preData[0] | (valores[8] & 1536) >> 6;
		preData[0] = preData[0] | (valores[5] & 8192) >> 11;
		preData[0] = preData[0] | (valores[2] & 8192) >> 12;
		preData[1] = (valores[3] & 3) << 4;
		preData[1] = preData[1] | valores[10] & 15;
		preData[2] = (valores[11] & 7) << 3;
		preData[2] = preData[2] | valores[8] >> 6 & 7;
		preData[3] = valores[8] & 63;
		preData[4] = (valores[9] & 15) << 2;
		preData[4] = preData[4] | ((valores[7] == 1) ? (1) : (0)) << 1;
		preData[4] = preData[4] | valores[5] >> 12 & 1;
		preData[5] = valores[5] >> 6 & 63;
		preData[6] = valores[5] & 63;
		preData[7] = (valores[3] & 3) << 4;
		preData[7] = preData[7] | ((valores[4] == 1) ? (1) : (0)) << 3;
		preData[7] = preData[7] | ((valores[1] == 1) ? (1) : (0)) << 2;
		preData[7] = preData[7] | ((valores[0] == 1) ? (1) : (0)) << 1;
		preData[7] = preData[7] | valores[2] >> 12 & 1;
		preData[8] = valores[2] >> 6 & 63;
		preData[9] = valores[2] & 63;
		String fD = "";
		for (int d : preData) {
			fD += Encriptador.getValorHashPorNumero(d);
		}
		return fD + fP;
	}
	
	public static String encriptarIP(final String IP) {
		final String[] split = IP.split(Pattern.quote("."));
		final StringBuilder encriptado = new StringBuilder();
		int cantidad = 0;
		for (int i = 0; i < 50; i++) {
			for (int o = 0; o < 50; o++) {
				if (((i & 15) << 4 | o & 15) == Integer.parseInt(split[cantidad])) {
					final Character A = (char) (i + 48);
					final Character B = (char) (o + 48);
					encriptado.append(A.toString() + B.toString());
					i = 0;
					o = 0;
					cantidad++;
					if (cantidad == 4) {
						return encriptado.toString();
					}
				}
			}
		}
		return "DD";
	}
	
	public static String encriptarPuerto(final int puerto) {
		int P = puerto;
		final StringBuilder numero = new StringBuilder();
		for (int a = 2; a >= 0; a--) {
			numero.append(HASH[(int) (P / Math.pow(64, a))]);
			P = P % (int) Math.pow(64, a);
		}
		return numero.toString();
	}
	
	public static String celdaIDAHash(final short celdaID) {
		return HASH[celdaID / 64] + "" + HASH[celdaID % 64];
	}
	
	public static short hashACeldaID(final String celdaCodigo) {
		final char char1 = celdaCodigo.charAt(0), char2 = celdaCodigo.charAt(1);
		short code1 = 0, code2 = 0, a = 0;
		while (a < HASH.length) {
			if (HASH[a] == char1) {
				code1 = (short) (a * 64);
			}
			if (HASH[a] == char2) {
				code2 = a;
			}
			a++;
		}
		return (short) (code1 + code2);
	}
	
	public static byte getNumeroPorValorHash(final char c) {
		for (byte a = 0; a < HASH.length; a++) {
			if (HASH[a] == c) {
				return a;
			}
		}
		return -1;
	}
	
	public static char getValorHashPorNumero(int c) {
		try {
			if (c >= HASH.length || c < 0) {
				c = 0;
			}
			return HASH[c];
		} catch (Exception e) {
			return 'a';
		}
	}
	
	public static void analizarCeldasDeInicio(String posPelea, ArrayList<Short> listaCeldas) {
		try {
			for (int a = 0; a < posPelea.length(); a += 2) {
				listaCeldas.add((short) ((getNumeroPorValorHash(posPelea.charAt(a)) << 6) + getNumeroPorValorHash(posPelea
				.charAt(a + 1))));
			}
		} catch (Exception e) {}
	}
	
	public static void decompilarMapaData(final Mapa mapa) {
		try {
			boolean activo, lineaDeVista, tieneObjInteractivo;
			byte caminable, level, slope;
			short objInteractivo;
			for (short f = 0; f < mapa.getMapData().length(); f += 10) {
				final StringBuilder celdaData = new StringBuilder(mapa.getMapData().substring(f, f + 10));
				ArrayList<Byte> celdaInfo = new ArrayList<Byte>();
				for (int i = 0; i < celdaData.length(); i++) {
					celdaInfo.add(getNumeroPorValorHash(celdaData.charAt(i)));
				}
				activo = (celdaInfo.get(0) & 32) >> 5 != 0;
				lineaDeVista = (celdaInfo.get(0) & 1) != 0;
				tieneObjInteractivo = (celdaInfo.get(7) & 2) >> 1 != 0;
				caminable = (byte) ((celdaInfo.get(2) & 56) >> 3);// 0 = no, 1 = medio, 4 = si
				level = (byte) (celdaInfo.get(1) & 15);
				slope = (byte) ((celdaInfo.get(4) & 60) >> 2);
				objInteractivo = (short) (((celdaInfo.get(0) & 2) << 12) + ((celdaInfo.get(7) & 1) << 12) + (celdaInfo.get(
				8) << 6) + celdaInfo.get(9));
				short celdaID = (short) (f / 10);
				Celda celda = new Celda(mapa, celdaID, activo, caminable, level, slope, lineaDeVista, tieneObjInteractivo
				? objInteractivo
				: -1);
				mapa.getCeldas().put(celdaID, celda);
				celda.celdaNornmal();
				if (tieneObjInteractivo && objInteractivo != -1) {
					Constantes.getTrabajosPorOI(objInteractivo, mapa.getTrabajos());
				}
			}
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("El mapa ID " + mapa.getID() + " esta errado, con mapData lenght " + mapa
			.getMapData().length());
			e.printStackTrace();
		}
	}
	
	public static String decifrarMapData(String key, String preData) {
		String data = preData;
		try {
			key = prepareKey(key);
			data = decypherData(preData, key, checksum(key) + "");
		} catch (Exception e) {}
		return data;
	}
	
	public static String unprepareData(String s, int currentKey, String[] aKeys) {
		try {
			if (currentKey < 1) {
				return s;
			}
			String _loc3 = aKeys[Integer.parseInt(s.substring(0, 1), 16)];
			if (_loc3 == null) {
				return s;
			}
			String _loc4 = s.substring(1, 2).toUpperCase();
			String _loc5 = decypherData(s.substring(2), _loc3, _loc4);
			if (checksum(_loc5) != (_loc4.charAt(0))) {
				return (s);
			}
			return (_loc5);
		} catch (Exception e) {
			return s;
		}
	}
	
	public static String prepareData(String s, int currentKey, String[] aKeys) {
		if (currentKey < 1) {
			return s;
		}
		if (aKeys[currentKey] == null) {
			return s;
		}
		char _loc3 = HEX_CHARS[currentKey];
		char _loc4 = checksum(s);
		String listo = (_loc3 + "" + _loc4 + "" + cypherData(s, aKeys[currentKey], Integer.parseInt(_loc4 + "", 16) * 2));
		return listo;
	}
	
	private static String cypherData(String d, String k, int c) {
		StringBuilder _loc5 = new StringBuilder();
		int _loc6 = k.length();
		d = preEscape(d);
		for (int _loc7 = 0; _loc7 < d.length(); _loc7++) {
			_loc5.append(d2h((int) (d.charAt(_loc7)) ^ (int) (k.charAt((_loc7 + c) % _loc6))));
		}
		return _loc5.toString();
	}
	
	private static String decypherData(String d, String k, String checksum) throws Exception {
		int c = Integer.parseInt(checksum, 16) * 2;
		String _loc5 = "";
		int _loc6 = k.length();
		int _loc7 = 0;
		int _loc9 = 0;
		for (; _loc9 < d.length(); _loc9 = _loc9 + 2) {
			_loc5 += (char) (Integer.parseInt(d.substring(_loc9, _loc9 + 2), 16) ^ k.codePointAt((_loc7 + c) % _loc6));
			_loc7++;
		}
		_loc5 = unescape(_loc5);
		return (_loc5);
	}
	
	private static String d2h(int d) {
		if (d > 255) {
			d = 255;
		}
		return (HEX_CHARS[(int) Math.floor(d / 16)] + "" + HEX_CHARS[d % 16]);
	}
	
	private static String unescape(String s) {
		try {
			s = URLDecoder.decode(s, "UTF-8");
		} catch (Exception e) {}
		return s;
	}
	
	// oscila del 32 al 127, todos los contenidos de k 95
	private static String escape(String s) {
		try {
			s = URLEncoder.encode(s, "UTF-8");
		} catch (Exception e) {}
		return s;
	}
	
	private static String preEscape(String s) {
		StringBuilder _loc3 = new StringBuilder();
		for (int _loc4 = 0; _loc4 < s.length(); _loc4++) {
			char _loc5 = s.charAt(_loc4);
			int _loc6 = _loc5;
			if (_loc6 < 32 || (_loc6 > 127 || (_loc5 == '%' || _loc5 == '+'))) {
				_loc3.append(escape(_loc5 + ""));
				continue;
			}
			_loc3.append(_loc5);
		}
		return _loc3.toString();
	}
	
	public static String prepareKey(String d) {
		String _loc3 = new String();
		int _loc4 = 0;
		for (; _loc4 < d.length(); _loc4 = _loc4 + 2) {
			_loc3 = _loc3 + (char) (Integer.parseInt(d.substring(_loc4, _loc4 + 2), 16));
		}
		_loc3 = unescape(_loc3);
		return (_loc3);
	}
	
	private static char checksum(String s) {
		int _loc3 = 0;
		int _loc4 = 0;
		for (; _loc4 < s.length(); _loc4++) {
			_loc3 = _loc3 + s.codePointAt(_loc4) % 16;
		}
		return HEX_CHARS[_loc3 % 16];
	}
	
	public static void consultaWeb(String url) throws Exception {
		URL obj = new URL(url);
		URLConnection con = obj.openConnection();
		con.setRequestProperty("Content-type", "charset=Unicode");
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		while ((in.readLine()) != null) {
			Thread.sleep(5);
		}
		in.close();
	}
	
	public static String aUTF(final String entrada) {
		String out = "";
		try {
			out = new String(entrada.getBytes("UTF-8"));
		} catch (final Exception e) {
			System.out.println("Conversion en UTF-8 fallida! : " + e.toString());
		}
		return out;
	}
	
	public static String aUnicode(final String entrada) {
		String out = "";
		try {
			out = new String(entrada.getBytes(), "UTF-8");
		} catch (final Exception e) {
			System.out.println("Conversion en UNICODE fallida! : " + e.toString());
		}
		return out;
	}
}
