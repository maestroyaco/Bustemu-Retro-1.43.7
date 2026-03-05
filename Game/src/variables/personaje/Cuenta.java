package variables.personaje;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import servidor.ServidorSocket;
import variables.casa.Cofre;
import variables.montura.Montura;
import variables.montura.Montura.Ubicacion;
import variables.objeto.Objeto;
import estaticos.Constantes;
import variables.personaje.Personaje;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.MainServidor;
import estaticos.Mundo;

public class Cuenta {
	private boolean _muteado, _sinco;
	private final int _id;
	private long _tiempoMuteado, _horaMuteada, _ultReporte, _ultVotoMilis;
	private final String _nombre;
	private String _actualIP = "", _ultimaIP = "", _ultimaConexion = "", _idioma = "es";
	private ServidorSocket _entradaPersonaje;
	private Personaje _tempPerso;
	private final ArrayList<Integer> _idsAmigos = new ArrayList<Integer>(), _idsEnemigos = new ArrayList<Integer>();
	private final Map<Integer, Personaje> _personajes = new TreeMap<Integer, Personaje>();
	private final Map<Byte, ArrayList<Integer>> _idsReportes = new TreeMap<Byte, ArrayList<Integer>>();
	// private final Map<Integer, Objeto> _objetosEnBanco = new TreeMap<Integer, Objeto>();
	private final ConcurrentHashMap<Integer, Montura> _establo = new ConcurrentHashMap<Integer, Montura>();
	private final ArrayList<String> _mensajes = new ArrayList<String>();
	private final Cofre _banco = new Cofre((short) -1, (short) -1, (short) 0, (short) 0, 99999);
	
	public Cuenta(final int id, final String nombre) {
		_id = id;
		_nombre = nombre;
	}
	
	public void addMensaje(String str, boolean salvar) {
		_mensajes.add(str);
		if (salvar) {
			GestorSQL.UPDATE_MENSAJES_CUENTA(_nombre, stringMensajes());
		}
	}
	
	public String stringMensajes() {
		if (_mensajes.isEmpty()) {
			return "";
		}
		StringBuilder str = new StringBuilder();
		for (String s : _mensajes) {
			if (str.length() > 0)
				str.append("&");
			str.append(s);
		}
		return str.toString();
	}
	
	public ArrayList<String> getMensajes() {
		return _mensajes;
	}
	
	public void setUltimoReporte(final long l) {
		_ultReporte = l;
	}
	
	public int getVip() {
		return GestorSQL.GET_VIP(_nombre);
	}
	
	public long getUltimoReporte() {
		return _ultReporte;
	}
	
	public void cargarInfoServerPersonaje(final String banco, final long kamasBanco, final String amigos,
	final String enemigos, final String establo, final String reportes, final String ultimaConexion, String mensajes,
	final String ultimaIP) {
		addKamasBanco(kamasBanco);
		_ultimaConexion = ultimaConexion;
		_ultimaIP = ultimaIP;
		for (final String s : banco.split(Pattern.quote("|"))) {
			try {
				if (s.isEmpty()) {
					continue;
				}
				final Objeto obj = Mundo.getObjeto(Integer.parseInt(s));
				if (obj == null) {
					continue;
				}
				_banco.addObjetoRapido(obj);
			} catch (final Exception e) {}
		}
		for (final String s : amigos.split(";")) {
			try {
				if (s.isEmpty())
					continue;
				_idsAmigos.add(Integer.parseInt(s));
			} catch (final Exception e) {}
		}
		for (final String s : enemigos.split(";")) {
			try {
				if (s.isEmpty())
					continue;
				_idsEnemigos.add(Integer.parseInt(s));
			} catch (final Exception e) {}
		}
		_idsAmigos.trimToSize();
		_idsEnemigos.trimToSize();
		for (final String s : establo.split(";")) {
			try {
				if (s.isEmpty())
					continue;
				final Montura montura = Mundo.getMontura(Integer.parseInt(s));
				if (montura != null) {
					addMonturaEstablo(montura);
				}
			} catch (final Exception e) {}
		}
		byte i = 0;
		for (final String s : reportes.split(Pattern.quote("|"))) {
			final ArrayList<Integer> array = new ArrayList<Integer>();
			for (final String f : s.split(";")) {
				try {
					if (f.isEmpty())
						continue;
					array.add(Integer.parseInt(f));
				} catch (final Exception e) {}
			}
			_idsReportes.put(i, array);
			i++;
		}
		for (String s : mensajes.split("&")) {
			if (s.isEmpty())
				continue;
			_mensajes.add(s);
		}
		try {
			if (_ultimaConexion.isEmpty() || !MainServidor.PARAM_BORRAR_CUENTAS_VIEJAS) {
				// return;
			} else {
				final String[] array = _ultimaConexion.split("~");
				int año = Integer.parseInt(array[0]);
				int mes = Integer.parseInt(array[1]);
				int dia = Integer.parseInt(array[2]);
				int hora = Integer.parseInt(array[3]);
				int minuto = Integer.parseInt(array[4]);
				final long minutos = Constantes.getTiempoFechaX(año, mes, dia, hora, minuto, 60 * 1000);
				if (Mundo.borrarLasCuentas(minutos)) {
					Mundo.CUENTAS_A_BORRAR.add(this);
				}
			}
		} catch (final Exception e) {}
	}
	
	public boolean tieneReporte(final byte tipo, final int id) {
		try {
			return _idsReportes.get(tipo).contains(id);
		} catch (final Exception e) {
			return false;
		}
	}
	
	public String listaReportes() {
		final StringBuilder str = new StringBuilder();
		for (byte b = 0; b < 4; b++) {
			if (str.length() > 0) {
				str.append("|");
			}
			final StringBuilder str2 = new StringBuilder();
			try {
				for (final int f : _idsReportes.get(b)) {
					if (str2.length() > 0) {
						str2.append(";");
					}
					str2.append(f);
				}
			} catch (final Exception e) {}
			str.append(str2.toString());
		}
		return str.toString();
	}
	
	public void addIDReporte(final byte tipo, final int id) {
		try {
			if (_idsReportes.get(tipo) == null) {
				_idsReportes.put(tipo, new ArrayList<Integer>());
			}
			if (!_idsReportes.get(tipo).contains(id)) {
				_idsReportes.get(tipo).add(id);
			}
		} catch (final Exception e) {}
	}
	
	public void setUltimaConexion() {
		final Calendar hoy = Calendar.getInstance();
		int año = hoy.get(Calendar.YEAR);
		int dia = hoy.get(Calendar.DAY_OF_MONTH);
		int mes = (hoy.get(Calendar.MONTH) + 1);
		int hora = hoy.get(Calendar.HOUR_OF_DAY);
		int minutos = hoy.get(Calendar.MINUTE);
		int segundos = hoy.get(Calendar.SECOND);
		_ultimaConexion = año + "~" + mes + "~" + dia + "~" + hora + "~" + minutos + "~" + segundos;
	}
	
	public String getUltimaConexion() {
		return _ultimaConexion;
	}
	
	public String getUltimaIP() {
		return _ultimaIP;
	}
	
	public void setUltimaIP(final String ip) {
		_ultimaIP = ip;
	}
	
	public void setActualIP(final String ip) {
		_actualIP = ip;
	}
	
	public String getActualIP() {
		return _actualIP;
	}
	
	public String getRegalo() {
		return GestorSQL.GET_REGALO(_nombre);
	}
	
	public void setRegalo(String regalo) {
		GestorSQL.SET_REGALO(_nombre, regalo);
	}
	
	public void addRegalo(final String regalo) {
		final StringBuilder r = new StringBuilder();
		r.append(getRegalo());
		if (r.length() > 0) {
			r.append(",");
		}
		r.append(regalo);
		setRegalo(r.toString());
	}
	
	public void setIdioma(final String idioma) {
		_idioma = idioma;
	}
	
	public void setPrimeraVez() {
		GestorSQL.SET_PRIMERA_VEZ_CERO(_nombre);
	}
	
	public byte getPrimeraVez() {
		return GestorSQL.GET_PRIMERA_VEZ(_nombre);
	}
	
	public String getContraseña() {
		return GestorSQL.GET_CONTRASEÑA_CUENTA(_nombre);
	}
	
	public String getApodo() {
		return GestorSQL.GET_APODO(_nombre);
	}
	
	public String getPregunta() {
		return GestorSQL.GET_PREGUNTA_SECRETA(_nombre);
	}
	
	public String getRespuesta() {
		return GestorSQL.GET_RESPUESTA_SECRETA(_nombre);
	}
	
	public int getAdmin() {
		return GestorSQL.GET_RANGO(_nombre);
	}
	
	public long getUltimoSegundosVoto() {
		return GestorSQL.GET_ULTIMO_SEGUNDOS_VOTO(_actualIP, _id);
	}
	
	public int getVotos() {
		return GestorSQL.GET_VOTOS(_nombre);
	}
	
	public void setRango(final int rango) {
		GestorSQL.SET_RANGO(_nombre, rango);
	}
	
	public int tiempoRestanteParaVotar() {
		final long resta = Constantes.getTiempoActualEscala(1000 * 60) - getMinutosUltimoVoto();
		if (resta < 0 || resta >= MainServidor.MINUTOS_VALIDAR_VOTO) {
			return 0;
		} else {
			return (int) (MainServidor.MINUTOS_VALIDAR_VOTO - resta);
		}
	}
	
	private synchronized long getMinutosUltimoVoto() {
		return getUltimoSegundosVoto() / 60;
	}
	
	public synchronized boolean puedeVotar() {
		if (_ultVotoMilis + 3000 > System.currentTimeMillis()) {
			return false;
		}
		return tiempoRestanteParaVotar() == 0;
	}
	
	public synchronized void darOgrinasPorVoto() {
		int votos = getVotos();
		int ogrinasXVotos = Constantes.getOgrinasPorVotos(votos);
		_ultVotoMilis = System.currentTimeMillis();
		GestorSQL.SET_ULTIMO_SEGUNDOS_VOTO(_id, _actualIP, System.currentTimeMillis() / 1000);
		GestorSQL.SET_VOTOS(_id, votos + 1);
		GestorSQL.SET_OGRINAS_CUENTA(GestorSQL.GET_OGRINAS_CUENTA(_id) + ogrinasXVotos, _id);
		GestorSalida.ENVIAR_Im_INFORMACION(_tempPerso, "1THANKS_FOR_VOTE;" + ogrinasXVotos);
	}
	
	public long getTiempoMuteado() {
		return _tiempoMuteado;
	}
	
	public void setTiempoMuteado(final long tiempo) {
		_tiempoMuteado = tiempo;
	}
	
	public long getHoraMuteado() {
		return _horaMuteada;
	}
	
	public void setHoraMuteado(final long hora) {
		_horaMuteada = hora;
	}
	
	public String getIdioma() {
		return _idioma;
	}
	
	public boolean getSinco() {
		return _sinco;
	}
	
	public void actSinco() {
		_sinco = true;
	}
	
	public long getKamasBanco() {
		return _banco.getKamas();
	}
	
	public Cofre getBanco() {
		return _banco;
	}
	
	public void addKamasBanco(final long kamas) {
		if (kamas == 0) {
			return;
		}
		_banco.addKamas(kamas, null);
		// if (_kamas >= MainServidor.LIMITE_DETECTAR_FALLA_KAMAS) {
		// GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_ADMINS(0, "[EMULADOR-ELBUSTEMU]", "La cuenta " + _nombre
		// + " (" + _id
		// + ") posee " + _kamas + " en el banco.");
		// MainServidor.redactarLogServidorln("LA CUENTA " + _nombre + " (" + _id + ") POSSE " +
		// _kamas);
		// if (!ServidorSocket.JUGADORES_REGISTRAR.contains(_nombre)) {
		// ServidorSocket.JUGADORES_REGISTRAR.add(_nombre);
		// }
		// }
	}
	
	public boolean estaMuteado() {
		return _muteado;
	}
	
	public void mutear(final boolean b, final int tiempo) {
		_muteado = b;
		if (tiempo == 0) {
			return;
		} else {
			GestorSalida.ENVIAR_Im_INFORMACION(_tempPerso, "1124;" + tiempo);
		}
		_tiempoMuteado = tiempo * 1000;
		_horaMuteada = System.currentTimeMillis();
	}
	
	public String stringBancoObjetosBD() {
		return _banco.analizarObjetoCofreABD();
	}
	
	public Collection<Objeto> getObjetosBanco() {
		return _banco.getObjetos();
	}
	
	public void addObjetoAlBanco(Objeto obj) {
		_banco.addObjetoRapido(obj);
	}
	
	public void setEntradaPersonaje(final ServidorSocket t) {
		_entradaPersonaje = t;
	}
	
	public ServidorSocket getSocket() {
		return _entradaPersonaje;
	}
	
	public int getID() {
		return _id;
	}
	
	public String getNombre() {
		return _nombre;
	}
	
	public Personaje getTempPersonaje() {
		return _tempPerso;
	}
	
	public boolean enLinea() {
		return _entradaPersonaje != null || _tempPerso != null;
	}
	
	public void setBaneado(final boolean baneado, final int minutos) {
		if (baneado) {
			long tiempoBaneo = -1;
			if (minutos > 0) {
				tiempoBaneo = System.currentTimeMillis() + (minutos * 60 * 1000);
			}
			GestorSQL.SET_BANEADO(_nombre, tiempoBaneo);
			if (_entradaPersonaje != null) {
				if (tiempoBaneo <= -1) {
					GestorSalida.ENVIAR_AlEb_CUENTA_BANEADA_DEFINITIVO(_entradaPersonaje);
				} else if (tiempoBaneo > System.currentTimeMillis()) {
					GestorSalida.ENVIAR_AlEk_CUENTA_BANEADA_TIEMPO(_entradaPersonaje, tiempoBaneo);
				}
			}
		} else {
			GestorSQL.SET_BANEADO(_nombre, 0);
		}
	}
	
	public boolean esAbonado() {
		return getTiempoAbono() > 0;
	}
	
	public long getTiempoAbono() {
		return Math.max(0, GestorSQL.GET_ABONO(_nombre) - System.currentTimeMillis());
	}
	
	public Personaje crearPersonaje(final String nombre, final byte clase, final byte sexo, final int color1,
	final int color2, final int color3) {
		final Personaje perso = Personaje.crearPersonaje(nombre, sexo, clase, color1, color2, color3, this);
		if (perso == null) {
			return null;
		}
		_personajes.put(perso.getID(), perso);
		return perso;
	}
	
	public void eliminarPersonaje(final int id) {
		if (!_personajes.containsKey(id)) {
			return;
		}
		Personaje perso = _personajes.get(id);
		if (perso == null) {
			return;
		}
		Mundo.eliminarPersonaje(perso, true);
		_personajes.remove(id);
		MainServidor.redactarLogServidorln("Se ha eliminado el personaje " + perso.getNombre() + "(" + perso.getID()
		+ ") de la cuenta " + _nombre + "(" + _id + ")");
	}
	
	public void addPersonaje(final Personaje perso) {
		if (_personajes.containsKey(perso.getID())) {
			MainServidor.redactarLogServidorln("Se esta intentado volver agregar a la cuenta, al personaje " + perso
			.getNombre());
			return;
		}
		_personajes.put(perso.getID(), perso);
	}
	
	public Collection<Personaje> getPersonajes() {
		return _personajes.values();
	}
	
	public Personaje getPersonaje(int id) {
		return _personajes.get(id);
	}
	
	public void setTempPerso(final Personaje perso) {
		_tempPerso = perso;
	}
	
	public synchronized void desconexion() {
		if (_tempPerso != null) {
			_tempPerso.desconectar(true);
		}
		_entradaPersonaje = null;
		_sinco = false;
		if (Mundo.SERVIDOR_ESTADO != Constantes.SERVIDOR_OFFLINE) {
			_tempPerso = null;
			GestorSQL.REPLACE_CUENTA_SERVIDOR(this, GestorSQL.GET_PRIMERA_VEZ(_nombre));
			// GestorSQL.UPDATE_CUENTA_LOGUEADO(_id, (byte) 0);
		}
	}
	
	public String analizarListaAmigosABD() {
		final StringBuilder str = new StringBuilder();
		for (final int i : _idsAmigos) {
			if (!str.toString().isEmpty()) {
				str.append(";");
			}
			str.append(i);
		}
		return str.toString();
	}
	
	public String stringListaEnemigosABD() {
		final StringBuilder str = new StringBuilder();
		for (final int i : _idsEnemigos) {
			if (!str.toString().isEmpty()) {
				str.append(";");
			}
			str.append(i);
		}
		return str.toString();
	}
	
	public String stringListaAmigos() {
		final StringBuilder str = new StringBuilder();
		for (final int i : _idsAmigos) {
			final Cuenta cuenta = Mundo.getCuenta(i);
			if (cuenta == null) {
				continue;
			}
			str.append("|");
			if (MainServidor.PARAM_MOSTRAR_APODO_LISTA_AMIGOS) {
				str.append(cuenta.getApodo());
			} else {
				str.append("EMPTY");
			}
			if (!cuenta.enLinea()) {
				continue;
			}
			final Personaje perso = cuenta.getTempPersonaje();
			if (perso == null) {
				continue;
			}
			str.append(perso.analizarListaAmigos(_id));
		}
		return str.toString();
	}
	
	public String stringListaEnemigos() {
		final StringBuilder str = new StringBuilder();
		for (final int i : _idsEnemigos) {
			final Cuenta cuenta = Mundo.getCuenta(i);
			if (cuenta == null) {
				continue;
			}
			str.append("|" + cuenta.getApodo());
			if (!cuenta.enLinea()) {
				continue;
			}
			final Personaje perso = cuenta.getTempPersonaje();
			if (perso == null) {
				continue;
			}
			str.append(perso.analizarListaAmigos(_id));
		}
		return str.toString();
	}
	
	public void addAmigo(final int id) {
		if (_id == id) {
			GestorSalida.ENVIAR_FA_AGREGAR_AMIGO(_tempPerso, "Ey");
			return;
		}
		if (_idsEnemigos.contains(id)) {
			GestorSalida.ENVIAR_iA_AGREGAR_ENEMIGO(_tempPerso, "Ea");
			return;
		}
		if (!_idsAmigos.contains(id)) {
			_idsAmigos.add(id);
			final Cuenta amigo = Mundo.getCuenta(id);
			if (amigo == null) {
				GestorSalida.ENVIAR_BN_NADA(_tempPerso);
				return;
			}
			GestorSalida.ENVIAR_FA_AGREGAR_AMIGO(_tempPerso, "K" + amigo.getApodo() + amigo.getTempPersonaje()
			.analizarListaAmigos(_id));
		} else {
			GestorSalida.ENVIAR_FA_AGREGAR_AMIGO(_tempPerso, "Ea");
		}
	}
	
	public void addEnemigo(final String packet, final int id) {
		if (_id == id) {
			GestorSalida.ENVIAR_iA_AGREGAR_ENEMIGO(_tempPerso, "Ey");
			return;
		}
		if (_idsAmigos.contains(id)) {
			GestorSalida.ENVIAR_FA_AGREGAR_AMIGO(_tempPerso, "Ea");
			return;
		}
		if (!_idsEnemigos.contains(id)) {
			_idsEnemigos.add(id);
			final Cuenta enemigo = Mundo.getCuenta(id);
			if (enemigo == null) {
				GestorSalida.ENVIAR_BN_NADA(_tempPerso);
				return;
			}
			GestorSalida.ENVIAR_iA_AGREGAR_ENEMIGO(_tempPerso, "K" + enemigo.getApodo() + enemigo.getTempPersonaje()
			.analizarListaEnemigos(_id));
		} else {
			GestorSalida.ENVIAR_iA_AGREGAR_ENEMIGO(_tempPerso, "Ea");
		}
	}
	
	public void borrarAmigo(final int id) {
		try {
			_idsAmigos.remove(_idsAmigos.indexOf(id));
			GestorSalida.ENVIAR_FD_BORRAR_AMIGO(_tempPerso, "K");
		} catch (final Exception e) {}
	}
	
	public void borrarEnemigo(final int id) {
		try {
			_idsEnemigos.remove(_idsEnemigos.indexOf(id));
			GestorSalida.ENVIAR_iD_BORRAR_ENEMIGO(_tempPerso, "K");
		} catch (final Exception e) {}
	}
	
	public boolean esAmigo(final int id) {
		return _idsAmigos.contains(id);
	}
	
	public boolean esEnemigo(final int id) {
		return _idsEnemigos.contains(id);
	}
	
	public String stringIDsEstablo() {
		final StringBuilder str = new StringBuilder();
		for (final Montura DP : _establo.values()) {
			GestorSQL.REPLACE_MONTURA(DP, false);
			if (str.length() > 0) {
				str.append(";");
			}
			str.append(DP.getID());
		}
		return str.toString();
	}
	
	public ConcurrentHashMap<Integer, Montura> getEstablo() {
		return _establo;
	}
	
	public void addMonturaEstablo(Montura montura) {
		_establo.put(montura.getID(), montura);
		montura.setUbicacion(Ubicacion.ESTABLO);
	}
	
	public boolean borrarMonturaEstablo(int id) {
		return _establo.remove(id) != null;
	}
}
