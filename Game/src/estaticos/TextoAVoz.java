package estaticos;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class TextoAVoz {
	private static final String TEXT_TO_SPEECH_SERVICE = "http://translate.google.com/translate_tts?ie=UTF-8&tl=es-MX&client=tw-ob&q=";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:11.0) Gecko/20100101 Firefox/11.0";
	private static final String LETRAS = "abcdefghijklmnopqrstuvwxyzáéíóúäëïöü";
	private static final String SIGNOS = ",.-¡!¿?=$* ";
	private static final String NUMEROS = "0123456789";
	
	public static String crearMP3(String texto, String idioma) {
		try {
			texto = filtrarTexto(texto).toLowerCase();
			if (contarLetras(texto) > MainServidor.MAX_CARACTERES_SONIDO) {
				return "";
			}
			String nTexto = convertirFile(texto);
			File output = new File(MainServidor.DIRECTORIO_LOCAL_MP3 + nTexto + ".mp3");
			if (!output.exists()) {
				String url = TEXT_TO_SPEECH_SERVICE;
				if (!idioma.isEmpty()) {
					url = url.replace("&tl=es-MX&", "&tl=" + idioma + "&");
				}
				url += URLEncoder.encode(texto, "UTF-8");
				URL cUrl = new URL(url);
				HttpURLConnection connection = (HttpURLConnection) cUrl.openConnection();
				connection.setRequestMethod("GET");
				connection.addRequestProperty("User-Agent", USER_AGENT);
				connection.connect();
				BufferedInputStream bufIn = new BufferedInputStream(connection.getInputStream());
				byte[] buffer = new byte[1024];
				int n;
				ByteArrayOutputStream bufOut = new ByteArrayOutputStream();
				while ((n = bufIn.read(buffer)) > 0) {
					bufOut.write(buffer, 0, n);
				}
				// Done, save data
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(output));
				out.write(bufOut.toByteArray());
				out.flush();
				out.close();
			}
			return nTexto;
		} catch (Exception e) {
			if (MainServidor.ES_LOCALHOST) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static String convertirFile(String texto) {
		texto = texto.replace("?", "#" + (int) '?');
		texto = texto.replace("*", "#" + (int) '*');
		texto = texto.replace(":", "#" + (int) ':');
		return texto;
	}
	
	public static int contarLetras(String texto) {
		int cantidad = 0;
		for (char a : texto.toCharArray()) {
			String b = (a + "").toLowerCase();
			if (LETRAS.contains(b) || NUMEROS.contains(b)) {
				cantidad++;
			}
		}
		return cantidad++;
	}
	
	public static String filtrarTexto(String texto) {
		StringBuilder nuevo = new StringBuilder();
		for (char a : texto.toCharArray()) {
			String b = (a + "").toLowerCase();
			if (LETRAS.contains(b) || SIGNOS.contains(b) || NUMEROS.contains(b)) {
				nuevo.append(a);
			}
		}
		texto = nuevo.toString();
		for (char a : SIGNOS.toCharArray()) {
			while (texto.contains(a + "" + a)) {
				texto = texto.replace(a + "" + a, a + "");
			}
		}
		texto = texto.replace("-", " - ");
		texto = texto.replace(".", ". ");
		texto = texto.replace(",", ", ");
		texto = texto.replace("=", " = ");
		while (texto.contains("  ")) {
			texto = texto.replace("  ", " ");
		}
		return texto;
	}
}
