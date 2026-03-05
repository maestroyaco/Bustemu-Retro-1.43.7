package variables.personaje;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.swing.Timer;
import servidor.ServidorSocket;
import servidor.ServidorSocket.AccionDeJuego;
import sprites.Preguntador;
import sprites.Exchanger;
import sprites.PreLuchador;
import variables.casa.Casa;
import variables.casa.Cofre;
import variables.encarnacion.Encarnacion;
import variables.gremio.Gremio;
import variables.gremio.MiembroGremio;
import variables.hechizo.Hechizo;
import variables.hechizo.StatHechizo;
import variables.mapa.Celda;
import variables.mapa.Cercado;
import variables.mapa.Mapa;
import variables.mapa.interactivo.ObjetoInteractivo;
import variables.mapa.interactivo.OtroInteractivo;
import variables.mision.Mision;
import variables.mision.MisionModelo;
import variables.mision.MisionObjetivoModelo;
import variables.mob.MobGradoModelo;
import variables.mob.MobModelo;
import variables.montura.Montura;
import variables.montura.Montura.Ubicacion;
import variables.objeto.Objeto;
import variables.objeto.ObjetoModelo;
import variables.objeto.ObjetoModelo.CAPACIDAD_STATS;
import variables.objeto.ObjetoSet;
import variables.oficio.Oficio;
import variables.oficio.StatOficio;
import variables.oficio.Trabajo;
import variables.pelea.Luchador;
import variables.pelea.Pelea;
import variables.personaje.Clase.BoostStat;
import variables.personaje.Especialidad.Don;
import variables.stats.Stats;
import variables.stats.TotalStats;
import variables.zotros.Almanax;
import variables.zotros.Prisma;
import variables.zotros.Tutorial;
import estaticos.Camino;
import estaticos.Condiciones;
import estaticos.Constantes;
import estaticos.Encriptador;
import estaticos.Formulas;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.MainServidor;
import estaticos.Mundo;
import estaticos.Mundo.Duo;

public class Personaje implements PreLuchador, Exchanger, Preguntador {
	// Restricciones
	// public boolean _RApuedeAgredir, _RApuedeDesafiar, _RApuedeIntercambiar, _RApuedeAtacar,
	// _RApuedeChatATodos,
	// _RApuedeMercante, _RApuedeUsarObjetos, _RApuedeInteractuarRecaudador,
	// _RApuedeInteractuarObjetos, _RApuedeHablarNPC,
	// _RApuedeAtacarMobsDungCuandoMutante, _RApuedeMoverTodasDirecciones,
	// _RApuedeAtacarMobsCualquieraCuandoMutante,
	// _RApuedeInteractuarPrisma, _RBpuedeSerAgredido, _RBpuedeSerDesafiado, _RBpuedeHacerIntercambio,
	// _RBpuedeSerAtacado,
	// _RBforzadoCaminar, _RBesFantasma, _RBpuedeSwitchModoCriatura, _RBesTumba;
	// RESTRICCIONES A
	public static final int RA_PUEDE_AGREDIR = 1;
	public static final int RA_PUEDE_DESAFIAR = 2;
	public static final int RA_PUEDE_INTERCAMBIAR = 4;
	public static final int RA_NO_PUEDE_ATACAR = 8;
	public static final int RA_PUEDE_CHAT_A_TODOS = 16;
	public static final int RA_PUEDE_MERCANTE = 32;
	public static final int RA_PUEDE_USAR_OBJETOS = 64;
	public static final int RA_PUEDE_INTERACTUAR_RECAUDADOR = 128;
	public static final int RA_PUEDE_INTERACTUAR_OBJETOS = 256;
	public static final int RA_PUEDE_HABLAR_NPC = 512;
	public static final int RA_NO_PUEDE_ATACAR_MOBS_DUNG_CUANDO_MUTENTE = 4096;
	public static final int RA_NO_PUEDE_MOVER_TODAS_DIRECCIONES = 8192;
	public static final int RA_NO_PUEDE_ATACAR_MOBS_CUALQUIERA_CUANDO_MUTANTE = 16384;
	public static final int RA_PUEDE_INTERACTUAR_PRISMA = 32768;
	// RESTRICCIONES B
	public static final int RB_PUEDE_SER_AGREDIDO = 1;
	public static final int RB_PUEDE_SER_DESAFIADO = 2;
	public static final int RB_PUEDE_HACER_INTERCAMBIO = 4;
	public static final int RB_PUEDE_SER_ATACADO = 8;
	public static final int RB_PUEDE_CORRER = 16;
	public static final int RB_NO_ES_FANTASMA = 32;
	public static final int RB_PUEDE_SWITCH_MODO_CRIATURA = 64;
	public static final int RB_NO_ES_TUMBA = 128;
	//
	private boolean _creandoJuego = true;
	private boolean _esMercante, _mostrarAlas, _mostrarAmigos = true, _ocupado, _sentado, _enLinea, _montando, _ausente,
	_invisible, _olvidandoHechizo, _pescarKuakua, _agresion, _indetectable, _huir = true, _inmovil, _cambiarColor,
	_calabozo, _cargandoMapa, _recienCreado, _deNoche, _deDia;
	private byte _orientacion = 1, _alineacion = Constantes.ALINEACION_NEUTRAL, _sexo, _claseID, _emoteActivado,
	_rostro = 1, _tipoExchange = Constantes.INTERCAMBIO_TIPO_NULO, _resets;
	private int _talla = 100, _ornamento, _gfxID, _titulo, _porcXPMontura;
	private short _mapaSalvada, _celdaSalvada;
	private int _gradoAlineacion = 1, _nivel = 1;
	private int _id, _color1 = -1, _color2 = -1, _color3 = -1, _puntosHechizos, _puntosStats, _energia = 10000, _emotes,
	_deshonor, _PDV, _PDVMax, _ultPDV, _honor, _conversandoCon, _pregunta, _esposoID, _colorNombre = -1;
	private int _restriccionesALocalPlayer = 8200, _restriccionesBCharacter = 8, _puntKoli, _ultimoNivel, _ordenNivel,
	_orden;
	private int _pretendiente;
	private long _kamas, _experiencia, _tiempoAgresion, _experienciaDia, _inicioTuto;
	private long _tiempoDesconexion, _tiempoUltEncarnacion, _tiempoPenalizacionKoliseo, _tiempoUltDesafio;
	// private float _velocidad;
	private String _nombre = "", _forjaEc = "", _ultVictimaPVP = "", _tituloVIP = "", _canales = "*#%!pi$:?^¡@~",
	_tipoInvitacion = "";
	private final Cuenta _cuenta;
	private MiembroGremio _miembroGremio;
	private Map<Integer, Integer> _subStatsBase = new HashMap<Integer, Integer>(),
	_subStatsScroll = new HashMap<Integer, Integer>(), _titulos = new HashMap<Integer, Integer>();
	private final TotalStats _totalStats = new TotalStats(new Stats(), new Stats(), new Stats(), new Stats(), 1);
	private Pelea _pelea, _prePelea;
	private Mapa _mapa;
	private Celda _celda;
	private Grupo _grupo;
	private Montura _montura;
	private Personaje _compañero, _invitandoA, _invitador;
	private Exchanger _exchanger;
	private MisionPVP _misionPvp;
	private Cofre _consultarCofre;
	private Casa _casaDentro, _consultarCasa;
	private final ArrayList<Integer> _ornamentos = new ArrayList<Integer>();
	private final ArrayList<Short> _zaaps = new ArrayList<Short>();
	private final ArrayList<Integer> _cardMobs = new ArrayList<Integer>(), _almanax = new ArrayList<Integer>(),
	_idsOmitidos = new ArrayList<>();
	private final Tienda _tienda = new Tienda();
	private final CopyOnWriteArrayList<Mision> _misiones = new CopyOnWriteArrayList<Mision>();
	private final Map<Integer, Duo<Integer, Integer>> _bonusSetDeClase = new HashMap<Integer, Duo<Integer, Integer>>();
	private final Map<Integer, Objeto> _objetos = new ConcurrentHashMap<Integer, Objeto>();
	private final Map<Integer, SetRapido> _setsRapidos = new ConcurrentHashMap<Integer, SetRapido>();
	private Map<Integer, StatHechizo> _mapStatsHechizos;// solo es para los multiman
	private final Map<Byte, StatOficio> _statsOficios = new HashMap<Byte, StatOficio>();
	private ArrayList<HechizoPersonaje> _hechizos = new ArrayList<HechizoPersonaje>();
	public final ArrayList<Personaje> invocacionControlable = new ArrayList<>();
	private Map<String, ArrayList<Long>> _agredir;
	private Map<String, ArrayList<Long>> _agredido;
	private GrupoKoliseo _koliseo;
	private Encarnacion _encarnacion;
	private Tutorial _tutorial;
	private Clase _clase;
	private byte _medioPagoServicio = 0;
	private final Objeto[] _objPos49 = new Objeto[49];
	private Timer _recuperarVida;
	private StringBuilder _packetsCola = new StringBuilder();
	private boolean _comandoPasarTurno;
	private Oficio _oficioActual = null;
	private Map<Objeto, Boolean> _dropPelea = new HashMap<Objeto, Boolean>();
	private int _unirsePrePeleaAlID;
	
	public Map<Objeto, Boolean> getDropsPelea() {
		return _dropPelea;
	}
	
	public long getTiempoUltDesafio() {
		return _tiempoUltDesafio;
	}
	
	public void setTiempoUltDesafio() {
		_tiempoUltDesafio = System.currentTimeMillis();
	}
	public void addInvocacionPersonaje(Personaje perso){
		perso.setCompañero(this);
		invocacionControlable.add(perso);
	}
	
	public boolean getComandoPasarTurno() {
		return _comandoPasarTurno;
	}
	
	public boolean esDeNoche() {
		return _deNoche;
	}
	
	public boolean esDeDia() {
		return _deDia;
	}
	
	public void setDeNoche() {
		_deNoche = !_deNoche;
		if (_deNoche) {
			_deDia = false;
		}
	}
	
	public void setDeDia() {
		_deDia = !_deDia;
		if (_deDia) {
			_deNoche = false;
		}
	}
	
	public void setComandoPasarTurno(boolean _comandoPasarTurno) {
		this._comandoPasarTurno = _comandoPasarTurno;
	}
	
	public void setMedioPagoServicio(byte medio) {
		_medioPagoServicio = medio;
	}
	
	public void setPenalizarKoliseo() {
		_tiempoPenalizacionKoliseo = System.currentTimeMillis() + (MainServidor.MINUTOS_PENALIZACION_KOLISEO * 60000);
	}
	
	public long getTiempoPenalizacionKoliseo() {
		return _tiempoPenalizacionKoliseo;
	}
	
	public int getMedioPagoServicio() {
		return _medioPagoServicio;
	}
	
	public void setColorNombre(int color) {
		_colorNombre = color;
	}
	
	public int getColorNombre() {
		return _colorNombre;
	}
	
	public boolean getCargandoMapa() {
		return _cargandoMapa;
	}
	
	public void setCargandoMapa(boolean b, ServidorSocket ss) {
		_cargandoMapa = b;
		if (!_cargandoMapa && _packetsCola.length() > 0) {
			try {
				Thread.sleep(MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA);
			} catch (InterruptedException e) {}
			ss.enviarPW(getPacketsCola());
			limpiarPacketsCola();
		}
	}
	
	public String getPacketsCola() {
		return _packetsCola.toString();
	}
	
	public void limpiarPacketsCola() {
		_packetsCola = new StringBuilder();
	}
	
	public void addPacketCola(String packet) {
		if (_packetsCola.length() > 0) {
			_packetsCola.append((char) 0x00);
		}
		_packetsCola.append(packet);
	}
	
	public void actualizarAtacantesDefensores() {}
	
	public int getRestriccionesA() {
		return _restriccionesALocalPlayer;
	}
	
	public int getRestriccionesB() {
		return _restriccionesBCharacter;
	}
	
	public ServidorSocket getServidorSocket() {
		if (_cuenta == null) {
			return null;
		}
		return _cuenta.getSocket();
	}
	
	private void agregarMisionDelDia() {
		int dia = (int) (System.currentTimeMillis() / 86400000);// (24 * 60 * 60 * 1000)
		if (!_almanax.contains(dia)) {
			_almanax.add(dia);
		}
	}
	
	public boolean realizoMisionDelDia() {
		return _almanax.contains((int) (System.currentTimeMillis() / 86400000));
	}
	
	public int cantMisionseAlmanax() {
		return _almanax.size();
	}
	
	public String listaAlmanax() {
		if (_almanax.isEmpty()) {
			return "";
		}
		StringBuilder str = new StringBuilder();
		for (int i : _almanax) {
			if (str.length() > 0)
				str.append(",");
			str.append(i);
		}
		return str.toString();
	}
	
	public String listaCardMobs() {
		if (MainServidor.PARAM_TODOS_MOBS_EN_BESTIARIO) {
			return "ALL";
		}
		final StringBuilder str = new StringBuilder();
		for (final int b : _cardMobs) {
			if (str.length() > 0) {
				str.append(",");
			}
			str.append(b);
		}
		return str.toString();
	}
	
	public void addCardMob(final int id) {
		if (Mundo.getMobModelo(id) == null) {
			return;
		}
		if (!tieneCardMob(id)) {
			_cardMobs.add(id);
			_cardMobs.trimToSize();
			GestorSalida.ENVIAR_Im_INFORMACION(this, "0777;" + id);
		}
	}
	
	public void delCardMob(final int id) {
		_cardMobs.remove((Object) id);
	}
	
	public boolean tieneCardMob(final int id) {
		return MainServidor.PARAM_TODOS_MOBS_EN_BESTIARIO || _cardMobs.contains(id);
	}
	
	public void setPuntKoli(final int i) {
		_puntKoli = i;
	}
	
	public int getPuntoKoli() {
		return _puntKoli;
	}
	
	public CopyOnWriteArrayList<Mision> getMisiones() {
		return _misiones;
	}
	
	public boolean confirmarEtapa(final int idEtapa, boolean preConfirma) {
		for (Mision mision : _misiones) {
			if (mision.getEtapaID() == idEtapa) {
				return mision.confirmarEtapaActual(this, preConfirma);
			}
		}
		return false;
	}
	
	public boolean confirmarObjetivo(final Mision mision, MisionObjetivoModelo obj, final Personaje perso,
	final Map<Integer, Integer> mobs, final boolean preConfirma, int idObjeto) {
		boolean b = obj.confirmar(perso, mobs, preConfirma, idObjeto);
		if (b && !preConfirma) {
			mision.setObjetivoCompletado(obj.getID());// se le convierte en cumplido
			GestorSalida.ENVIAR_Im_INFORMACION(this, "055;" + mision.getIDModelo());
			if (!mision.estaCompletada()) {
				boolean cumplioLosObjetivos = mision.verificaSiCumplioEtapa();
				if (cumplioLosObjetivos) {
					Mundo.getEtapa(mision.getEtapaID()).darRecompensa(this);
					if (mision.verificaFinalizoMision()) {
						GestorSalida.ENVIAR_Im_INFORMACION(this, "056;" + mision.getIDModelo());
					}
				}
			}
		}
		return b;
	}
	
	public void verificarMisionesTipo(int[] tipos, final Map<Integer, Integer> mobs, final boolean preConfirma,
	int idObjeto) {
		for (final Mision mision : _misiones) {
			if (mision.estaCompletada()) {
				continue;
			}
			for (final Entry<Integer, Integer> entry : mision.getObjetivos().entrySet()) {
				if (entry.getValue() == Mision.ESTADO_COMPLETADO) {
					continue;
				}
				MisionObjetivoModelo objMod = Mundo.getMisionObjetivoModelo(entry.getKey());
				boolean paso = false;
				for (int i : tipos) {
					if (objMod.getTipo() == i) {
						paso = true;
						break;
					}
				}
				if (!paso) {
					continue;
				}
				confirmarObjetivo(mision, objMod, this, mobs, preConfirma, idObjeto);
			}
		}
	}
	
	public void addNuevaMision(final MisionModelo misionMod) {
		if (misionMod.getEtapas().isEmpty()) {
			return;
		}
		Mision mision = new Mision(misionMod.getID(), Mision.ESTADO_INCOMPLETO, misionMod.getEtapas().get(0), 0, "");
		_misiones.add(mision);
	}
	
	public boolean tieneEtapa(final int id) {
		for (final Mision mision : _misiones) {
			if (mision.getEtapaID() == id) {
				return true;
			}
		}
		return false;
	}
	
	public boolean tieneMision(final int id) {
		for (final Mision mision : _misiones) {
			if (mision.getIDModelo() == id) {
				return true;
			}
		}
		return false;
	}
	
	public boolean borrarMision(final int id) {
		for (final Mision mision : _misiones) {
			if (mision.getIDModelo() == id) {
				_misiones.remove(mision);
				return true;
			}
		}
		return false;
	}
	
	public int getEstadoMision(final int id) {
		// solo se usa para las condiciones
		for (final Mision mision : _misiones) {
			if (mision.getIDModelo() == id) {
				return mision.getEstadoMision();
			}
		}
		return Mision.ESTADO_NO_TIENE;
	}
	
	public byte getEstadoObjetivo(final int id) {
		// solo se usa para las condiciones
		for (final Mision mision : _misiones) {
			for (final Entry<Integer, Integer> entry : mision.getObjetivos().entrySet()) {
				MisionObjetivoModelo objMod = Mundo.getMisionObjetivoModelo(entry.getKey());
				if (objMod.getID() == id) {
					if (entry.getValue() == Mision.ESTADO_COMPLETADO) {
						return Mision.ESTADO_COMPLETADO;// tiene realizado
					} else {
						return Mision.ESTADO_INCOMPLETO;// tiene sin realizar
					}
				}
			}
		}
		return Mision.ESTADO_NO_TIENE;// no tiene
	}
	
	public String listaMisiones() {
		final StringBuilder str = new StringBuilder();
		int i = 0;
		for (final Mision mision : _misiones) {
			str.append("|" + mision.getIDModelo() + ";" + (mision.getEstadoMision()) + ";" + i++);
		}
		return str.toString();
	}
	
	public String stringMisiones() {
		final StringBuilder str = new StringBuilder();
		for (final Mision mision : _misiones) {
			if (str.length() > 0) {
				str.append("|");
			}
			str.append(mision.getIDModelo() + "~" + mision.getEstadoMision());
			if (!mision.estaCompletada()) {
				str.append("~" + mision.getEtapaID() + "~" + mision.getNivelEtapa());
				boolean paso = false;
				for (final Entry<Integer, Integer> entry : mision.getObjetivos().entrySet()) {
					if (paso) {
						str.append(";");
					} else {
						str.append("~");
					}
					str.append(entry.getKey() + "," + entry.getValue());
					paso = true;
				}
			}
		}
		return str.toString();
	}
	
	public String detalleMision(final int id) {
		final StringBuilder str = new StringBuilder();
		for (final Mision mision : _misiones) {
			if (mision.estaCompletada()) {
				continue;
			}
			if (mision.getIDModelo() == id) {
				final StringBuilder str2 = new StringBuilder();
				for (final Entry<Integer, Integer> entry : mision.getObjetivos().entrySet()) {
					if (str.length() > 0) {
						str.append(";");
					}
					str.append(entry.getKey() + "," + entry.getValue());
				}
				str.append("|");
				for (final int etapa : Mundo.getMision(id).getEtapas()) {
					if (etapa == mision.getEtapaID()) {
						str2.append("|");
						continue;
					}
					if (str2.length() > 0) {
						str2.append(",");
					}
					str2.append(etapa);
				}
				str.append(str2.toString());
				return id + "|" + mision.getEtapaID() + "~" + Mundo.getEtapa(mision.getEtapaID()).getRecompensa().replace("|",
				"*") + "|" + str.toString();
			}
		}
		return "";
	}
	
	public int getUltimoNivel() {
		return _ultimoNivel;
	}
	
	public void setUltimoNivel(int ultimo) {
		_ultimoNivel = ultimo;
	}
	
	public byte getRostro() {
		return _rostro;
	}
	
	public void cambiarRostro(final byte rostro) {
		_rostro = rostro;
	}
	
	public long getExperienciaDia() {
		return _experienciaDia;
	}
	
	public void resetExpDia() {
		_experienciaDia = 0;
	}
	
	public String stringSeguidores() {
		final StringBuilder str = new StringBuilder();
		final String forma = Formulas.getRandomBoolean() ? "," : ":";
		for (byte pos : Constantes.POSICIONES_EQUIPAMIENTO) {
			final Objeto obj = getObjPosicion(pos);
			if (obj == null) {
				continue;
			}
			final String param2 = obj.getParamStatTexto(Constantes.STAT_PERSONAJE_SEGUIDOR, 3);
			if (!param2.isEmpty()) {
				try {
					str.append(forma + Integer.parseInt(param2, 16) + "^" + _talla);
				} catch (Exception e) {}
			}
		}
		return str.toString();
	}
	
	// public String getInterOgrinas(int id) {
	// return _intercambioOgrinas[id];
	// }118.96.114.7/webdav/configSecure.php
	//
	// public void setInterOgrinas(String vendedor, String comprador, String ogrinas, String kamas) {
	// _intercambioOgrinas[0] = vendedor;
	// _intercambioOgrinas[1] = comprador;
	// _intercambioOgrinas[2] = ogrinas;
	// _intercambioOgrinas[3] = kamas;
	// }
	//
	public boolean getCambiarColor() {
		return _cambiarColor;
	}
	
	// public void setCambiarColor(final boolean cambiar) {
	// _cambiarColor = cambiar;
	// }
	public void setColores(int color1, int color2, int color3) {
		if (color1 < -1) {
			color1 = -1;
		} else if (color1 > 16777215) {
			color1 = 16777215;
		}
		if (color1 < -1) {
			color1 = -1;
		} else if (color1 > 16777215) {
			color1 = 16777215;
		}
		if (color2 < -1) {
			color2 = -1;
		} else if (color2 > 16777215) {
			color2 = 16777215;
		}
		if (color3 < -1) {
			color3 = -1;
		} else if (color3 > 16777215) {
			color3 = 16777215;
		}
		_color1 = color1;
		_color2 = color2;
		_color3 = color3;
		GestorSQL.UPDATE_COLORES_PJ(this);
	}
	
	public void setCalabozo(final boolean calabozo) {
		_calabozo = calabozo;
	}
	
	public boolean getCalabozo() {
		return _calabozo;
	}
	
	public void setInmovil(final boolean movil) {
		_inmovil = movil;
	}
	
	public boolean estaInmovil() {
		return _inmovil;
	}
	
	public void setTutorial(final Tutorial tuto) {
		_tutorial = tuto;
		if (tuto != null) {
			_inicioTuto = System.currentTimeMillis();
		}
	}
	
	public long getInicioTutorial() {
		return _inicioTuto;
	}
	
	public Tutorial getTutorial() {
		return _tutorial;
	}
	
	// public boolean getReconectado() {
	// return _reconectado;
	// }
	public void setPescarKuakua(final boolean pescar) {
		_pescarKuakua = pescar;
	}
	
	public boolean getPescarKuakua() {
		return _pescarKuakua;
	}
	
	public void setUltMisionPVP(final String nombre) {
		_ultVictimaPVP = nombre;
	}
	
	public String getUltMisionPVP() {
		return _ultVictimaPVP;
	}
	
	public void setForjaEc(final String forja) {
		_forjaEc = forja;
	}
	
	public String getForjaEc() {
		return _forjaEc;
	}
	
	public boolean getRestriccionA(int param) {
		return (_restriccionesALocalPlayer & param) != param;
	}
	
	public void modificarA(final int restr, final int modif) {
		_restriccionesALocalPlayer = (_restriccionesALocalPlayer | restr) ^ (restr ^ modif);
	}
	
	// 41959 = fantasma, 41959 = tumba
	public String mostrarmeA() {
		StringBuilder packet = new StringBuilder();
		packet.append("RESTRICCIONES A --- " + _nombre + " --- " + _restriccionesALocalPlayer);
		packet.append("\n" + RA_PUEDE_AGREDIR + " PUEDE AGREDIR : " + getRestriccionA(RA_PUEDE_AGREDIR));
		packet.append("\n" + RA_PUEDE_DESAFIAR + " RA_PUEDE_DESAFIAR : " + getRestriccionA(RA_PUEDE_DESAFIAR));
		packet.append("\n" + RA_PUEDE_INTERCAMBIAR + " RA_PUEDE_INTERCAMBIAR : " + getRestriccionA(RA_PUEDE_INTERCAMBIAR));
		packet.append("\n" + RA_NO_PUEDE_ATACAR + " RA_NO_PUEDE_ATACAR : " + getRestriccionA(RA_NO_PUEDE_ATACAR));
		packet.append("\n" + RA_PUEDE_CHAT_A_TODOS + " RA_PUEDE_CHAT_A_TODOS : " + getRestriccionA(RA_PUEDE_CHAT_A_TODOS));
		packet.append("\n" + RA_PUEDE_MERCANTE + " RA_PUEDE_MERCANTE : " + getRestriccionA(RA_PUEDE_MERCANTE));
		packet.append("\n" + RA_PUEDE_USAR_OBJETOS + " RA_PUEDE_USAR_OBJETOS : " + getRestriccionA(RA_PUEDE_USAR_OBJETOS));
		packet.append("\n" + RA_PUEDE_INTERACTUAR_RECAUDADOR + " RA_PUEDE_INTERACTUAR_RECAUDADOR : " + getRestriccionA(
		RA_PUEDE_INTERACTUAR_RECAUDADOR));
		packet.append("\n" + RA_PUEDE_HABLAR_NPC + " RA_PUEDE_HABLAR_NPC : " + getRestriccionA(RA_PUEDE_HABLAR_NPC));
		packet.append("\n" + RA_NO_PUEDE_ATACAR_MOBS_DUNG_CUANDO_MUTENTE + " RA_NO_PUEDE_ATACAR_MOBS_DUNG_CUANDO_MUTENTE : "
		+ getRestriccionA(RA_NO_PUEDE_ATACAR_MOBS_DUNG_CUANDO_MUTENTE));
		packet.append("\n" + RA_NO_PUEDE_MOVER_TODAS_DIRECCIONES + " RA_NO_PUEDE_MOVER_TODAS_DIRECCIONES : "
		+ getRestriccionA(RA_NO_PUEDE_MOVER_TODAS_DIRECCIONES));
		packet.append("\n" + RA_NO_PUEDE_ATACAR_MOBS_CUALQUIERA_CUANDO_MUTANTE
		+ " RA_NO_PUEDE_ATACAR_MOBS_CUALQUIERA_CUANDO_MUTANTE : " + getRestriccionA(
		RA_NO_PUEDE_ATACAR_MOBS_CUALQUIERA_CUANDO_MUTANTE));
		packet.append("\n" + RA_PUEDE_INTERACTUAR_PRISMA + " RA_PUEDE_INTERACTUAR_PRISMA : " + getRestriccionA(
		RA_PUEDE_INTERACTUAR_PRISMA));
		return packet.toString();
	}
	
	public boolean getRestriccionB(int param) {
		return (_restriccionesBCharacter & param) != param;
	}
	
	public void modificarB(final int restr, final int modifComplejo) {
		_restriccionesBCharacter = (_restriccionesBCharacter | restr) ^ (restr ^ modifComplejo);
	}
	
	// 63 = fantasma , 159 = tumba
	public String mostrarmeB() {
		StringBuilder packet = new StringBuilder();
		packet.append("RESTRICCIONES B --- " + _nombre + " --- " + _restriccionesBCharacter);
		packet.append("\n" + RB_PUEDE_SER_AGREDIDO + " PUEDDE SER AGREDIDO : " + getRestriccionB(RB_PUEDE_SER_AGREDIDO));
		packet.append("\n" + RB_PUEDE_SER_DESAFIADO + " PUEDE SER DESAFIADO : " + getRestriccionB(RB_PUEDE_SER_DESAFIADO));
		packet.append("\n" + RB_PUEDE_HACER_INTERCAMBIO + " PUEDE HACER INTERCAMBIO : " + getRestriccionB(
		RB_PUEDE_HACER_INTERCAMBIO));
		packet.append("\n" + RB_PUEDE_SER_ATACADO + " PUEDE SER ATACADO : " + getRestriccionB(RB_PUEDE_SER_ATACADO));
		packet.append("\n" + RB_PUEDE_CORRER + " PUEDE CORRER : " + getRestriccionB(RB_PUEDE_CORRER));
		packet.append("\n" + RB_NO_ES_FANTASMA + " NO ES FANTASMA : " + getRestriccionB(RB_NO_ES_FANTASMA));
		packet.append("\n" + RB_PUEDE_SWITCH_MODO_CRIATURA + " PUEDE SWITCH MODO CRIATURA : " + getRestriccionB(
		RB_PUEDE_SWITCH_MODO_CRIATURA));
		packet.append("\n" + RB_NO_ES_TUMBA + " NO ES TUMBA : " + getRestriccionB(RB_NO_ES_TUMBA));
		return packet.toString();
	}
	
	public void setGrupoKoliseo(final GrupoKoliseo koli) {
		_koliseo = koli;
		if (koli != null) {
			GestorSalida.ENVIAR_kCK_CREAR_KOLISEO(this);
			GestorSalida.ENVIAR_kM_TODOS_MIEMBROS_KOLISEO(this, koli);
		}
	}
	
	public GrupoKoliseo getGrupoKoliseo() {
		return _koliseo;
	}
	
	private void refrescarParteSetClase(boolean enviarSiOSi) {
		ArrayList<Integer> tiene = null;
		if (_totalStats.getStatsObjetos().getStatHechizos() != null) {
			tiene = new ArrayList<>();
			for (final String stat : _totalStats.getStatsObjetos().getStatHechizos()) {
				try {
					final String[] val = stat.split("#");
					final int efecto = Integer.parseInt(val[0], 16);
					final int hechizoID = Integer.parseInt(val[1], 16);
					int modif = 1;
					switch (efecto) {
						case Constantes.STAT_HECHIZO_CLASE_DESACTIVA_LINEA_DE_VUELO :
						case Constantes.STAT_HECHIZO_CLASE_DESACTIVA_LINEA_RECTA :
							break;
						default :
							modif = Integer.parseInt(val[3], 16);
							break;
					}
					final String modificacion = efecto + ";" + hechizoID + ";" + modif;
					tiene.add(hechizoID);
					if (!_bonusSetDeClase.containsKey(hechizoID)) {
						_bonusSetDeClase.put(hechizoID, new Duo<Integer, Integer>(efecto, modif));
						if (_enLinea) {
							GestorSalida.ENVIAR_SB_HECHIZO_BOOST_SET_CLASE(this, modificacion);
						}
					}
				} catch (Exception e) {}
			}
		}
		if (!_bonusSetDeClase.isEmpty()) {
			ArrayList<Integer> noTiene = new ArrayList<>();
			for (int hechizoID : _bonusSetDeClase.keySet()) {
				if (tiene == null || !tiene.contains(hechizoID)) {
					noTiene.add(hechizoID);
				}
			}
			for (int hechizoID : noTiene) {
				int efecto = _bonusSetDeClase.get(hechizoID)._primero;
				String modificacion = efecto + ";" + hechizoID;
				_bonusSetDeClase.remove(hechizoID);
				if (_enLinea) {
					GestorSalida.ENVIAR_SB_HECHIZO_BOOST_SET_CLASE(this, modificacion);
				}
			}
		}
	}
	
	public boolean tieneModfiSetClase(int hechizoID) {
		return _bonusSetDeClase.containsKey(hechizoID);
	}
	
	public int getModifSetClase(final int hechizoID, final int efecto) {
		if (_bonusSetDeClase.containsKey(hechizoID) && _bonusSetDeClase.get(hechizoID)._primero == efecto) {
			return _bonusSetDeClase.get(hechizoID)._segundo;
		}
		return 0;
	}
	
	public int getEmotes() {
		return _emotes;
	}
	
	public boolean tieneEmote(final int emote) {
		final int valor = (int) Math.pow(2, emote - 1);
		return (_emotes & valor) != 0;
	}
	
	public boolean addEmote(final byte emote) {
		final int valor = (int) Math.pow(2, emote - 1);
		if ((_emotes & valor) != 0) {
			return false;
		}
		_emotes += valor;
		if (_emotes < 0) {
			_emotes = 0;
		} else if (_emotes > 7667711) {
			_emotes = 7667711;
		}
		return true;
	}
	
	public boolean borrarEmote(final byte emote) {
		final int valor = (int) Math.pow(2, emote - 1);
		if ((_emotes & valor) == 0) {
			return false;
		}
		_emotes -= valor;
		if (_emotes < 0) {
			_emotes = 0;
		} else if (_emotes > 7667711) {
			_emotes = 7667711;
		}
		return true;
	}
	
	public boolean esMercante() {
		return _esMercante;
	}
	
	public void setMercante(final boolean mercante) {
		_esMercante = mercante;
	}
	
	public void setPuntoSalvada(String ptoSalvada) {
		try {
			final String[] infos = ptoSalvada.split(",");
			_mapaSalvada = Short.parseShort(infos[0]);
			_celdaSalvada = Short.parseShort(infos[1]);
			Mundo.getMapa(_mapaSalvada).getCelda(_celdaSalvada).getID();
		} catch (final Exception e) {
			_mapaSalvada = 7411;
			_celdaSalvada = 340;
		}
	}
	
	public Personaje getCompañero() {
		return _compañero;
	}
	
	public void setCompañero(Personaje compañero) {
		_compañero = compañero;
	}
	
	public boolean esMultiman() {
		return _claseID == Constantes.CLASE_MULTIMAN;
	}
	
	private void reiniciarSubStats(Map<Integer, Integer> map) {
		map.clear();
		map.put(Constantes.STAT_MAS_VITALIDAD, 0);
		map.put(Constantes.STAT_MAS_FUERZA, 0);
		map.put(Constantes.STAT_MAS_SABIDURIA, 0);
		map.put(Constantes.STAT_MAS_INTELIGENCIA, 0);
		map.put(Constantes.STAT_MAS_SUERTE, 0);
		map.put(Constantes.STAT_MAS_AGILIDAD, 0);
	}
	
	public Personaje(final int id, final String nombre, final byte sexo, final byte claseID, final int color1,
	final int color2, final int color3, final long kamas, final int puntosHechizo, final int capital, final int energia,
	final short nivel, final long exp, final int talla, final int gfxID, final byte alineacion, final int cuenta,
	final Map<Integer, Integer> statsBase, final Map<Integer, Integer> statsScroll, final boolean mostrarAmigos,
	final boolean mostarAlineacion, final String canal, final short mapa, final short celda, final String inventario,
	final int porcPDV, final String hechizos, final String ptoSalvada, final String oficios, final byte porcXPMontura,
	final int montura, final int honor, final int deshonor, final byte gradoAlineacion, final String zaaps,
	final int esposoID, final String tienda, final boolean mercante, final int restriccionesA, final int restriccionesB,
	final int encarnacion, final int emotes, final String titulos, final String tituloVIP, final String ornamentos,
	final String misiones, final String coleccion, final byte resets, final String almanax, final int ultimoNivel,
	final String setsRapidos, final int colorNombre, final String orden) {
		_cuenta = Mundo.getCuenta(cuenta);
		try {
			boolean modificar = false;
			try {
				_mapa = Mundo.getMapa(mapa);
				setCelda(_mapa.getCelda(celda));
			} catch (Exception e) {
				_mapa = Mundo.getMapa((short) 7411);
				setCelda(_mapa.getCelda((short) 311));
			}
			if (_mapa == null || _celda == null) {
				MainServidor.redactarLogServidorln("Mapa o celda invalido del personaje " + _nombre
				+ ", por lo tanto se cierra el server");
				System.exit(1);
				return;
			}
			_id = id;
			_nombre = nombre;
			_colorNombre = colorNombre;
			_sexo = sexo;
			_claseID = claseID;
			_clase = Mundo.getClase(_claseID);
			_color1 = color1;
			_color2 = color2;
			_color3 = color3;
			_subStatsBase.putAll(statsBase);
			_puntosHechizos = puntosHechizo;
			_puntosStats = capital;
			_energia = energia;
			_talla = talla;
			_gfxID = gfxID;
			_restriccionesALocalPlayer = restriccionesA;
			_restriccionesBCharacter = restriccionesB;
			_ultimoNivel = ultimoNivel;
			_canales = canal;
			if (MainServidor.PARAM_REINICIAR_CANALES) {
				_canales = "*#%!pi$:?^¡@~";
				modificar = true;
			}
			addCanal("~");
			_esposoID = esposoID;
			_resets = resets;
			_experiencia = exp;
			_nivel = 1;
			while (_experiencia >= Mundo.getExpPersonaje(_nivel + 1)) {
				_nivel++;
				if (_nivel >= MainServidor.NIVEL_MAX_PERSONAJE) {
					break;
				}
			}
			_alineacion = alineacion;
			if (_alineacion != Constantes.ALINEACION_NEUTRAL) {
				_honor = honor;
				_deshonor = deshonor;
				_gradoAlineacion = gradoAlineacion;
				if (MainServidor.HONOR_FIJO_PARA_TODOS > -1) {
					_honor = MainServidor.HONOR_FIJO_PARA_TODOS;
					refrescarGradoAlineacion();
				}
			}
			if (orden.isEmpty()) {
				_ordenNivel = 0;
				switch (_alineacion) {
					case Constantes.ALINEACION_BONTARIANO :
						_orden = 1;
						break;
					case Constantes.ALINEACION_BRAKMARIANO :
						_orden = 5;
						break;
					case Constantes.ALINEACION_MERCENARIO :
						_orden = 9;
						break;
					default :
						_orden = 0;
						break;
				}
			} else {
				String[] ord = orden.split(",");
				try {
					_orden = Integer.parseInt(ord[0]);
					_ordenNivel = Integer.parseInt(ord[1]);
				} catch (Exception e) {}
			}
			actualizarStatsEspecialidad(Mundo.getEspecialidad(_orden, _ordenNivel));
			if (MainServidor.PARAM_START_EMOTES_COMPLETOS) {
				_emotes = 7667711;
			} else {
				_emotes = emotes;
			}
			if (montura < -1) {
				setMontura(Mundo.getMontura(montura));
			}
			setPorcXPMontura(porcXPMontura);
			_subStatsScroll.putAll(statsScroll);
			_totalStats.getStatsBase().nuevosStatsBase(_subStatsBase, this);
			_totalStats.getStatsBase().acumularStats(_subStatsScroll);
			addKamas(kamas, false, false);
			setPuntoSalvada(ptoSalvada);
			setMisiones(misiones);
			final String[] tArray = titulos.split(Pattern.quote(","));
			for (String t : tArray) {
				if (t.isEmpty()) {
					continue;
				}
				try {
					String[] tt = t.split(Pattern.quote("*"));
					int titulo = Integer.parseInt(tt[0]);
					int color = -1;
					if (tt.length > 1) {
						color = Integer.parseInt(tt[1]);
					}
					_titulos.put(titulo, color);
					if (t.contains("+")) {
						_titulo = titulo;
					}
				} catch (Exception e) {}
			}
			_tituloVIP = tituloVIP;
			for (final String str : ornamentos.split(",")) {
				if (str.isEmpty()) {
					continue;
				}
				try {
					int ornamento = Integer.parseInt(str);
					_ornamentos.add(ornamento);
					if (str.contains("+")) {
						_ornamento = ornamento;
					}
				} catch (final Exception e) {}
			}
			_ornamentos.trimToSize();
			for (final String str : coleccion.split(",")) {
				if (str.isEmpty()) {
					continue;
				}
				try {
					_cardMobs.add(Integer.parseInt(str));
				} catch (final Exception e) {}
			}
			_cardMobs.trimToSize();
			if (MainServidor.PARAM_PERMITIR_DESACTIVAR_ALAS) {
				_mostrarAlas = mostarAlineacion;
			} else {
				_mostrarAlas = _alineacion != Constantes.ALINEACION_NEUTRAL;
			}
			_mostrarAmigos = mostrarAmigos;
			for (final String str : zaaps.split(",")) {
				try {
					_zaaps.add(Short.parseShort(str));
				} catch (final Exception e) {}
			}
			_zaaps.trimToSize();
			for (final String str : almanax.split(",")) {
				try {
					_almanax.add(Integer.parseInt(str));
				} catch (final Exception e) {}
			}
			_almanax.trimToSize();
			for (final String idObjeto : inventario.split(Pattern.quote("|"))) {
				try {
					if (idObjeto.isEmpty()) {
						continue;
					}
					Objeto obj = Mundo.getObjeto(Integer.parseInt(idObjeto));
					if (obj.getDueñoTemp() == 0) {
						obj.setDueñoTemp(_id);
						// se agrega el objeto al array _objPos
						addObjetoConOAKO(obj, false);
					} else {
						modificar = true;
						MainServidor.redactarLogServidorln("El objetoID " + idObjeto + " tiene dueño " + (obj.getDueñoTemp())
						+ " no se puede agregar a " + _nombre + "(" + _id + ")");
					}
				} catch (Exception e) {
					modificar = true;
					MainServidor.redactarLogServidorln("El objetoID " + idObjeto + " pertenece a " + _nombre + "(" + _id + ")"
					+ ", no existe");
				}
			}
			for (final String idObjeto : tienda.split(Pattern.quote("|"))) {
				try {
					if (idObjeto.isEmpty()) {
						continue;
					}
					Objeto obj = Mundo.getObjeto(Integer.parseInt(idObjeto));
					if (obj.getDueñoTemp() == 0) {
						obj.setDueñoTemp(_id);
						if (obj.getPrecio() <= 0) {
							addObjetoConOAKO(obj, false);
						} else {
							_tienda.addObjeto(obj);
						}
					} else {
						modificar = true;
						MainServidor.redactarLogServidorln("La tiendaID " + idObjeto + " tiene dueño " + (obj.getDueñoTemp())
						+ " no se puede agregar a " + _nombre + "(" + _id + ")");
					}
				} catch (Exception e) {
					modificar = true;
					MainServidor.redactarLogServidorln("El objetoID " + idObjeto + " pertenece a " + _nombre + "(" + _id + ")"
					+ ", no existe");
				}
			}
			// boolean mensaje = false;
			// StringBuilder str = new StringBuilder();
			// ArrayList<Objeto> objetos = new ArrayList<>();
			// objetos.addAll(_objetos.values());
			// objetos.addAll(_tienda);
			// for (Objeto o : objetos) {
			// if (o._reseteado) {
			// mensaje = true;
			// if (str.length() > 0)
			// str.append(",");
			// str.append("6962");
			// }
			// }
			// if (mensaje) {
			// _cuenta.addRegalo(str.toString());
			// _cuenta
			// .addMensaje(
			// "1223;Tus objetos magueados se han puesto con los stats base debido a una modificación en
			// la forjamagia muy importante, así evitarémos el over magueo en el servidor ANKALIKE. Te
			// hemos dejado un regalo por cada objeto que te modificamos.",
			// true);
			// }
			if (MainServidor.PARAM_RESET_STATS_PLAYERS) {
				modificar = true;
				resetearStats(false);
			}
			if (_cuenta != null) {
				_esMercante = mercante;
				if (_esMercante) {
					if (!_tienda.estaVacia()) {
						_mapa.addMercante(this);
					} else {
						_esMercante = false;
					}
				}
				// se pone en la creacion para considerar como si recien se hubiera desconectado
				_tiempoDesconexion = System.currentTimeMillis();
				analizarPosHechizos(hechizos);
				if (MainServidor.PARAM_PERMITIR_OFICIOS) {
					_statsOficios.put((byte) 7, new StatOficio((byte) 7, Mundo.getOficio(1), 0));
					if (!oficios.isEmpty()) {
						for (final String data : oficios.split(";")) {
							try {
								final String[] infos = data.split(",");
								aprenderOficio(Mundo.getOficio(Integer.parseInt(infos[0])), Integer.parseInt(infos[1]));
							} catch (final Exception e) {}
						}
					}
				}
				try {
					for (String s : setsRapidos.split(Pattern.quote("*"))) {
						if (s.isEmpty()) {
							continue;
						}
						String[] split = s.split(Pattern.quote("|"));
						int idSet = Integer.parseInt(split[0]);
						String nombreSet = split[1];
						int iconoSet = Integer.parseInt(split[2]);
						String dataSet = split[3];
						addSetRapido(idSet, nombreSet, iconoSet, dataSet);
					}
				} catch (Exception e) {}
				if (_energia > 10000) {
					_energia = 10000;
				} else if (_energia < 0 && !esTumba()) {
					convertirseTumba();
				} else if (_energia == 0 && !esFantasma()) {
					convertirseFantasma();
				}
				if (!MainServidor.PARAM_PERDER_ENERGIA) {
					_energia = 10000;
				}
				actualizarPDV(porcPDV);
				if (modificar) {
					GestorSQL.SALVAR_PERSONAJE(this, false);
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			if (_cuenta == null) {
				MainServidor.redactarLogServidorln("SE DEBE ELIMINAR PERSONAJE " + nombre + " (" + id + ") - CUENTA " + cuenta);
				if (MainServidor.PARAM_ELIMINAR_PERSONAJES_BUG) {
					Mundo.eliminarPersonaje(this, true);
				}
			} else {
				_cuenta.addPersonaje(this);
			}
		}
	}
	
	public static synchronized Personaje crearPersonaje(final String nombre, byte sexo, byte claseID, int color1,
	int color2, int color3, final Cuenta cuenta) {
		try {
			color1 = Math.min(16777215, Math.max(-1, color1));
			color2 = Math.min(16777215, Math.max(-1, color2));
			color3 = Math.min(16777215, Math.max(-1, color3));
			sexo = (sexo != Constantes.SEXO_MASCULINO ? Constantes.SEXO_FEMENINO : Constantes.SEXO_MASCULINO);
			if (Mundo.getClase(claseID) == null) {
				claseID = 1;
			}
			Clase clase = Mundo.getClase(claseID);
			final StringBuilder zaaps = new StringBuilder();
			for (final String zaap : MainServidor.INICIO_ZAAPS.split(",")) {
				try {
					if (zaap.isEmpty()) {
						continue;
					}
					if (Mundo.getCeldaZaapPorMapaID(Short.parseShort(zaap)) == -1) {
						continue;
					}
					if (zaaps.length() > 0) {
						zaaps.append(",");
					}
					zaaps.append(zaap);
				} catch (Exception e) {}
			}
			long kamas = 0;
			final StringBuilder objetos = new StringBuilder();
			final int nivel = MainServidor.INICIO_NIVEL;
			if (!MainServidor.PARAM_SOLO_PRIMERA_VEZ || cuenta.getPrimeraVez() == 1) {
				cuenta.addKamasBanco(MainServidor.KAMAS_BANCO);
				for (final String str : MainServidor.INICIO_OBJETOS.split(";")) {
					try {
						if (str.isEmpty()) {
							continue;
						}
						String[] arg = str.split(",");
						final Objeto obj = Mundo.getObjetoModelo(Integer.parseInt(arg[0])).crearObjeto(Integer.parseInt(arg[1]),
						Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.MAXIMO);
						Mundo.addObjeto(obj, false);
						try {
							if (arg.length > 2) {
								byte pos = Byte.parseByte(arg[2]);
								obj.setPosicion(pos);
							}
						} catch (Exception e) {}
						if (objetos.length() > 0) {
							objetos.append("|");
						}
						objetos.append(obj.getID());
					} catch (final Exception e) {}
				}
				for (final String str : MainServidor.INICIO_SET_ID.split(",")) {
					if (str.isEmpty()) {
						continue;
					}
					final ObjetoSet objSet = Mundo.getObjetoSet(Integer.parseInt(str));
					if (objSet != null) {
						for (final ObjetoModelo OM : objSet.getObjetosModelos()) {
							final Objeto x = OM.crearObjeto(1, Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.MAXIMO);
							Mundo.addObjeto(x, false);
							if (objetos.length() > 0) {
								objetos.append("|");
							}
							objetos.append(x.getID());
						}
					}
				}
				kamas += MainServidor.INICIO_KAMAS;
				cuenta.setPrimeraVez();
			}
			short mapaID = clase.getMapaInicio();
			short celdaID = clase.getCeldaInicio();
			Mapa mapa = Mundo.getMapa(mapaID);
			if (mapa == null) {
				mapaID = 7411;
				celdaID = 340;
			}
			String puntoSalvada = mapaID + "," + celdaID;
			int id = Mundo.sigIDPersonaje();
			int puntosHechizo = (nivel - 1) * MainServidor.PUNTOS_HECHIZO_POR_NIVEL;
			int puntosStats = ((nivel - 1) * MainServidor.PUNTOS_STATS_POR_NIVEL) + MainServidor.INICIO_PUNTOS_STATS;
			int gfxID = clase.getGfxs(sexo);
			int talla = clase.getTallas(sexo);
			long xp = Mundo.getExpPersonaje(nivel);
			int emotes = MainServidor.INICIO_EMOTES;
			final Personaje nuevoPersonaje = new Personaje(id, nombre, sexo, claseID, color1, color2, color3, kamas,
			puntosHechizo, puntosStats, nivel, xp, talla, gfxID, cuenta.getID(), mapaID, celdaID, objetos.toString(),
			puntoSalvada, zaaps.toString(), emotes);
			Mundo.addPersonaje(nuevoPersonaje);
			GestorSQL.SALVAR_PERSONAJE(nuevoPersonaje, true);
			return nuevoPersonaje;
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION crearPersonaje " + e.toString());
			e.printStackTrace();
			return null;
		}
	}
	
	public Personaje(final int id, final String nombre, final byte sexo, final byte claseID, final int color1,
	final int color2, final int color3, final long kamas, final int puntosHechizo, final int capital, final int nivel,
	final long exp, final int talla, final int gfxID, final int cuenta, final short mapa, final short celda,
	final String inventario, final String ptoSalvada, final String zaaps, final int emotes) {
		// personaje recien creado
		_cuenta = Mundo.getCuenta(cuenta);
		_id = id;
		_nombre = nombre;
		_sexo = sexo;
		_claseID = claseID;
		_clase = Mundo.getClase(_claseID);
		_color1 = color1;
		_color2 = color2;
		_color3 = color3;
		_puntosHechizos = puntosHechizo;
		_puntosStats = capital;
		_talla = talla;
		_gfxID = gfxID;
		_experiencia = exp;
		_nivel = 1;
		while (_experiencia >= Mundo.getExpPersonaje(_nivel + 1)) {
			_nivel++;
			if (_nivel >= MainServidor.NIVEL_MAX_PERSONAJE) {
				break;
			}
		}
		reiniciarSubStats(_subStatsBase);
		reiniciarSubStats(_subStatsScroll);
		_totalStats.getStatsBase().nuevosStatsBase(_subStatsBase, this);
		_totalStats.getStatsBase().acumularStats(_subStatsScroll);
		addKamas(kamas, false, false);
		setPuntoSalvada(ptoSalvada);
		if (MainServidor.PARAM_START_EMOTES_COMPLETOS) {
			_emotes = 7667711;
		} else {
			_emotes = emotes;
		}
		if (!MainServidor.PARAM_PERMITIR_DESACTIVAR_ALAS) {
			_mostrarAlas = _alineacion != Constantes.ALINEACION_NEUTRAL;
		}
		for (final String str : zaaps.split(",")) {
			try {
				_zaaps.add(Short.parseShort(str));
			} catch (final Exception e) {}
		}
		_zaaps.trimToSize();
		for (final String idObjeto : inventario.split(Pattern.quote("|"))) {
			try {
				if (idObjeto.isEmpty()) {
					continue;
				}
				Objeto obj = Mundo.getObjeto(Integer.parseInt(idObjeto));
				if (obj.getDueñoTemp() == 0) {
					obj.setDueñoTemp(_id);
					// se agrega el objeto al array _objPos
					addObjetoConOAKO(obj, false);
				} else {
					MainServidor.redactarLogServidorln("El objetoID " + idObjeto + " tiene dueño " + (obj.getDueñoTemp())
					+ " no se puede agregar a " + _nombre + "(" + _id + ")");
				}
			} catch (Exception e) {
				MainServidor.redactarLogServidorln("El objetoID " + idObjeto + " pertenece a " + _nombre + "(" + _id + ")"
				+ ", no existe");
			}
		}
		if (MainServidor.PARAM_RESET_STATS_PLAYERS) {
			resetearStats(false);
		}
		fullPDV();
		if (MainServidor.PARAM_PERMITIR_OFICIOS) {
			_statsOficios.put((byte) 7, new StatOficio((byte) 7, Mundo.getOficio(1), 0));
		}
		fijarHechizosInicio();
		_recienCreado = true;
		_mapa = Mundo.getMapa(mapa);
		setCelda(_mapa.getCelda(celda));
	}
	
	// Doble
	public static Personaje crearClon(final Personaje perso, final int id) {
		boolean mostrarAlas = false;
		int gradoAlineacion = 0;
		if (perso.alasActivadas()) {
			mostrarAlas = true;
			gradoAlineacion = perso.getGradoAlineacion();
		}
		final Personaje clon = new Personaje(id, perso._nombre, perso._sexo, perso._claseID, perso._color1, perso._color2,
		perso._color3, perso._nivel, perso._talla, perso._gfxID, perso._totalStats, perso.getPorcPDV(), perso.getPDVMax(),
		mostrarAlas, gradoAlineacion, perso._alineacion, (perso._montando && perso._montura != null)
		? perso._montura
		: null, perso._objPos49);
		return clon;
	}
	
	// CLON
	public Personaje(final int id, final String nombre, final byte sexo, final byte clase, final int color1,
	final int color2, final int color3, final int nivel, final int talla, final int gfxID, final TotalStats totalStats,
	final float porcPDV, final int pdvMax, final boolean mostarAlineacion, final int gradoAlineacion,
	final byte alineacion, Montura montura, Objeto[] objPos) {
		// crear clon
		_id = id;
		_nombre = nombre;
		_sexo = sexo;
		_claseID = clase;
		_clase = Mundo.getClase(_claseID);
		_color1 = color1;
		_color2 = color2;
		_color3 = color3;
		_nivel = nivel;
		_gradoAlineacion = gradoAlineacion;
		_alineacion = alineacion;
		_talla = talla;
		_gfxID = gfxID;
		_totalStats.getStatsBase().nuevosStats(totalStats.getStatsBase());
		_totalStats.getStatsObjetos().nuevosStats(totalStats.getStatsObjetos());
		if (MainServidor.PARAM_PERMITIR_DESACTIVAR_ALAS) {
			_mostrarAlas = mostarAlineacion;
		} else {
			_mostrarAlas = _alineacion != Constantes.ALINEACION_NEUTRAL;
		}
		actualizarPDV(porcPDV);
		if (montura != null) {
			_montando = true;
			_montura = montura;
		}
		if (objPos != null) {
			for (Objeto obj : objPos) {
				if (obj == null) {
					continue;
				}
				_objPos49[obj.getPosicion()] = obj;
			}
		}
		_cuenta = null;
	}
	
	public static Personaje crearMultiman(final int id, final int nivel, int iniciativa, final MobModelo mobModelo) {
		Personaje multiman = new Personaje(id, nivel, iniciativa, mobModelo);
		return multiman;
	}
	
	public Personaje(final int id, final int nivel, int iniciativa, final MobModelo mobModelo) {
		Stats stats = new Stats();
		stats.fijarStatID(Constantes.STAT_MAS_PA, 6);
		stats.fijarStatID(Constantes.STAT_MAS_PM, 3);
		MobGradoModelo mobGrado = mobModelo.getGradoPorGrado((byte) 1);
		for (Entry<Integer, Integer> entry : mobGrado.getStats().getEntrySet()) {
			int valor = entry.getValue();
			switch (entry.getKey()) {
				case Constantes.STAT_MAS_PA :
					valor -= 6;
					break;
				case Constantes.STAT_MAS_PM :
					valor -= 3;
					break;
				case Constantes.STAT_MAS_INICIATIVA :
					continue;
			}
			stats.addStatID(entry.getKey(), valor * nivel / MainServidor.NIVEL_MAX_PERSONAJE);
		}
		stats.addStatID(Constantes.STAT_MAS_INICIATIVA, iniciativa / 2);
		int PDV = mobGrado.getPDVMAX() * nivel / MainServidor.NIVEL_MAX_PERSONAJE;
		_id = id;
		_nombre = mobModelo.getNombre();
		_claseID = Constantes.CLASE_MULTIMAN;
		_nivel = nivel;
		_gfxID = mobModelo.getGfxID();
		_talla = mobModelo.getTalla();
		_totalStats.getStatsBase().nuevosStats(stats);
		_PDVMax = _PDV = PDV;
		_cuenta = null;
		int i = 1;
		for (Entry<Integer, StatHechizo> entry : mobGrado.getHechizos().entrySet()) {
			StatHechizo st = entry.getValue();
			if (st == null) {
				continue;
			}
			addHechizoPersonaje(Encriptador.getValorHashPorNumero(i), st.getHechizo(), st.getGrado());
			i++;
		}
		_mapStatsHechizos = new HashMap<Integer, StatHechizo>();
		_mapStatsHechizos.putAll(mobGrado.getHechizos());
	}
	
	public void conectarse() {
		if (_cuenta.getSocket() == null) {
			MainServidor.redactarLogServidorln("El personaje " + _nombre + " tiene como entrada personaje NULL");
			return;
		}
		_cuenta.setTempPerso(this);
		setEnLinea(true);
		if (_esMercante) {
			_mapa.removerMercante(_id);
			_esMercante = false;
			GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(_mapa, _id);
		}
		if (_montura != null) {
			GestorSalida.ENVIAR_Re_DETALLES_MONTURA(this, "+", _montura);
		}
		addPuntosPorDesconexion();
		GestorSalida.ENVIAR_ASK_PERSONAJE_SELECCIONADO(this);
		GestorSalida.ENVIAR_Rx_EXP_DONADA_MONTURA(this);
		Especialidad esp = Mundo.getEspecialidad(_orden, _ordenNivel);
		if (esp != null) {
			GestorSalida.ENVIAR_ZS_SET_ESPECIALIDAD_ALINEACION(this, esp.getID());
		}
		if (_miembroGremio != null) {
			GestorSalida.ENVIAR_gS_STATS_GREMIO(this, _miembroGremio);
		}
		GestorSalida.ENVIAR_SL_LISTA_HECHIZOS(this);
		GestorSalida.ENVIAR_eL_LISTA_EMOTES(this, _emotes);
		GestorSalida.ENVIAR_FO_MOSTRAR_CONEXION_AMIGOS(this, _mostrarAmigos);
		enviarMsjAAmigos();
		GestorSalida.ENVIAR_Im_INFORMACION(this, "189");
		if (!_cuenta.getUltimaConexion().isEmpty() && !_cuenta.getUltimaIP().isEmpty()) {
			String u = _cuenta.getUltimaConexion();
			GestorSalida.ENVIAR_Im_INFORMACION(this, "0152;" + u.substring(0, u.lastIndexOf("~")) + "~" + _cuenta
			.getUltimaIP());
		}
		_cuenta.setUltimaIP(_cuenta.getActualIP());
		_cuenta.setUltimaConexion();
		GestorSalida.ENVIAR_Im_INFORMACION(this, "0153;" + _cuenta.getActualIP());
		GestorSalida.ENVIAR_al_ESTADO_ZONA_ALINEACION(this);
		GestorSalida.ENVIAR_AR_RESTRICCIONES_PERSONAJE(this);
		startTimerRegenPDV();
		_casaDentro = Mundo.getCasaDentroPorMapa(_mapa.getID());
		if (_casaDentro != null) {
			GestorSalida.ENVIAR_hL_INFO_CASA(this, _casaDentro.informacionCasa(_id));
		}
		if (!MainServidor.MODO_HEROICO && _energia < 1500) {
			GestorSalida.ENVIAR_M1_MENSAJE_SERVER_SVR_MUESTRA_INSTANTANEO(this, 11, _energia + "", "");
		}
		if (_miembroGremio != null) {
			GestorSalida.ENVIAR_gIG_GREMIO_INFO_GENERAL(this, _miembroGremio.getGremio());
		}
		GestorSalida.ENVIAR_El_LISTA_OBJETOS_COFRE_PRECARGADO(this, getBanco());
		// GestorSQL.UPDATE_CUENTA_LOGUEADO(_cuenta.getID(), (byte) 1);
		if (MainServidor.PARAM_ALIMENTAR_MASCOTAS) {
			comprobarMascotas();
		}
		refrescarStuff(true, true, false);// actualizas los stats y refresca stuff
		enviarBonusSet();
		GestorSalida.ENVIAR_Os_SETS_RAPIDOS(this);
		if (_statsOficios.size() > 1) {
			GestorSalida.ENVIAR_JS_SKILLS_DE_OFICIO(this, _statsOficios.values());
			GestorSalida.ENVIAR_JX_EXPERINENCIA_OFICIO(this, _statsOficios.values());
			GestorSalida.ENVIAR_JO_OFICIO_OPCIONES(this, _statsOficios.values());
			verificarHerramientOficio();
		}
		_creandoJuego = true;
		if (esFantasma()) {
			GestorSalida.ENVIAR_IH_COORDENADAS_UBICACION(this, Constantes.ESTATUAS_FENIX);
		}
	}
	
	public void verificarHerramientOficio() {
		final Objeto obj = getObjPosicion(Constantes.OBJETO_POS_ARMA);
		if (obj != null) {
			Oficio oficio = null;
			for (final StatOficio statOficio : _statsOficios.values()) {
				if (statOficio.getPosicion() == 7) {
					continue;
				}
				if (statOficio.getOficio().esHerramientaValida(obj.getObjModeloID())) {
					oficio = statOficio.getOficio();
					break;
				}
			}
			packetModoInvitarTaller(oficio, true);
		}
	}
	
	private void enviarBonusSet() {
		Map<Integer, Integer> map = new TreeMap<>();
		for (byte pos : Constantes.POSICIONES_EQUIPAMIENTO) {
			final Objeto obj = getObjPosicion(pos);
			if (obj == null) {
				continue;
			}
			int setID = obj.getObjModelo().getSetID();
			if (setID < 1) {
				continue;
			}
			int v = 1;
			if (map.containsKey(setID)) {
				v = map.get(setID) + 1;
			}
			map.put(setID, v);
		}
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			GestorSalida.ENVIAR_OS_BONUS_SET(this, entry.getKey(), entry.getValue());
		}
	}
	
	private void comprobarMascotas() {
		for (final Objeto objeto : _objetos.values()) {
			if (objeto.getObjModelo().getTipo() != Constantes.OBJETO_TIPO_MASCOTA) {
				continue;
			}
			int pdv = objeto.getPDV();
			if (pdv < 1) {
				continue;
			}
			boolean comido = false;
			if (objeto.esDevoradorAlmas()) {
				comido = true;
			} else if (objeto.getDiferenciaTiempo(Constantes.STAT_SE_HA_COMIDO_EL, 60 * 60 * 1000) <= 24) {
				comido = true;
			}
			if (comido) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "025");
			} else {
				if (objeto.getCorpulencia() == Constantes.CORPULENCIA_DELGADO) {
					objeto.horaComer(true, Constantes.CORPULENCIA_DELGADO);
					restarVidaMascota(objeto);
				} else {
					objeto.setCorpulencia(Constantes.CORPULENCIA_DELGADO);
				}
				GestorSalida.ENVIAR_Im_INFORMACION(this, "150");
			}
		}
	}
	
	public void restarVidaMascota(Objeto mascota) {
		if (!MainServidor.PARAM_MASCOTAS_PERDER_VIDA) {
			return;
		}
		if (mascota == null) {
			mascota = getObjPosicion(Constantes.OBJETO_POS_MASCOTA);
		}
		if (mascota == null) {
			return;
		}
		final int pdv = mascota.getPDV();
		if (pdv > 1) {
			mascota.setPDV(pdv - 1);
			GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(this, mascota);
		} else if (pdv == 1) {
			// murio mascota
			mascota.setPDV(0);
			final int fantasma = Mundo.getMascotaModelo(mascota.getObjModeloID()).getFantasma();
			if (Mundo.getObjetoModelo(fantasma) != null) {
				GestorSalida.ENVIAR_OR_ELIMINAR_OBJETO(this, mascota.getID());
				mascota.setPosicion(Constantes.OBJETO_POS_NO_EQUIPADO, this, true);
				mascota.setIDOjbModelo(fantasma);
				GestorSalida.ENVIAR_OAKO_APARECER_OBJETO(this, mascota);
			} else {
				borrarOEliminarConOR(mascota.getID(), true);
			}
			GestorSalida.ENVIAR_Im_INFORMACION(this, "154");
		}
	}
	
	private void addPuntosPorDesconexion() {
		if (_pelea == null && _tiempoDesconexion > -1) {
			int segundos = (int) ((System.currentTimeMillis() - _tiempoDesconexion) / (1000));
			if (!esFantasma() && !esTumba()) {
				int horas = segundos / 3600;
				int energiaAdd = horas * (_casaDentro != null ? 100 : 50);
				energiaAdd = Math.min(energiaAdd, 10000 - _energia);
				if (energiaAdd > 0) {
					addEnergiaConIm(energiaAdd, false);
					GestorSalida.ENVIAR_Im_INFORMACION(this, "092;" + energiaAdd);
				}
			}
		}
		_tiempoDesconexion = -1;
	}
	
	public boolean getRecienCreado() {
		if (MainServidor.MODO_PVP) {
			if (_alineacion == Constantes.ALINEACION_NEUTRAL) {
				return true;
			}
		}
		return _recienCreado;
	}
	
	public Clase getClase() {
		return _clase;
	}
	
	public Map<Integer, Integer> getSubStatsScroll() {
		return _subStatsScroll;
	}
	
	public Map<Integer, Integer> getSubStatsBase() {
		return _subStatsBase;
	}
	
	public void addAgredirA(String nombre) {
		if (_agredir == null) {
			_agredir = new HashMap<>();
		}
		if (_agredir.get(nombre) == null) {
			_agredir.put(nombre, new ArrayList<Long>());
		}
		_agredir.get(nombre).add(System.currentTimeMillis());
	}
	
	public ArrayList<Long> getAgredirA(String nombre) {
		if (_agredir == null) {
			return null;
		}
		return _agredir.get(nombre);
	}
	
	public void addAgredidoPor(String nombre) {
		if (_agredido == null) {
			_agredido = new HashMap<>();
		}
		if (_agredido.get(nombre) == null) {
			_agredido.put(nombre, new ArrayList<Long>());
		}
		_agredido.get(nombre).add(System.currentTimeMillis());
	}
	
	public ArrayList<Long> getAgredidoPor(String nombre) {
		if (_agredido == null) {
			return null;
		}
		return _agredido.get(nombre);
	}
	
	public Oficio getOficioActual() {
		return _oficioActual;
	}
	
	public void packetModoInvitarTaller(Oficio oficio, boolean enviarOT) {
		_oficioActual = oficio;
		if (enviarOT) {
			if (oficio == null) {
				GestorSalida.ENVIAR_OT_OBJETO_HERRAMIENTA(this, -1);
				return;
			} else {
				GestorSalida.ENVIAR_OT_OBJETO_HERRAMIENTA(this, oficio.getID());
			}
		}
		if (_mapa.getTrabajos().isEmpty()) {
			return;
		}
		final StringBuilder mostrar = new StringBuilder();
		if (oficio != null) {
			final String[] trabajos = Constantes.trabajosOficioTaller(oficio.getID()).split(";");
			for (final String skill : trabajos) {
				if (skill.isEmpty()) {
					continue;
				}
				if (!_mapa.getTrabajos().contains(Integer.parseInt(skill))) {
					continue;
				}
				if (mostrar.length() > 0) {
					mostrar.append(";");
				}
				mostrar.append(skill);
			}
		}
		GestorSalida.ENVIAR_EW_OFICIO_MODO_INVITACION(this, oficio != null ? "+" : "-", _id, mostrar.toString());
	}
	
	public void crearJuegoPJ() {
		if (_cuenta.getSocket() == null) {
			return;
		}
		Mapa mapa = _mapa;
		if (_pelea != null) {
			mapa = _pelea.getMapaReal();
		}
		// setCargandoMapa(true, null);
		GestorSalida.ENVIAR_GCK_CREAR_PANTALLA_PJ(this);
		GestorSalida.ENVIAR_As_STATS_DEL_PJ(this);
		// try {
		// Thread.sleep(500);
		// } catch (Exception e) {}
		GestorSalida.ENVIAR_GDM_CAMBIO_DE_MAPA(this, mapa);
		if (_pelea != null) {
			if (_pelea.getFase() != Constantes.PELEA_FASE_FINALIZADO) {
				return;
			} else {
				salirPelea(false, false);
			}
		}
		// solo se agrega si la pelea es null o se sale de la pelea por eso es _mapa
		GestorSalida.ENVIAR_GM_PJ_A_MAPA(_mapa, this);
		_celda.addPersonaje(this, true);		
	   
	}
	private Mapa _mapaGDM;
	
	public void setMapaGDM(Mapa mapa) {
		_mapaGDM = mapa;
	}
	
	public Mapa getMapaGDM() {
		return _mapaGDM;
	}
	
	// private boolean _espiarPJ = false;
	//
	// public void setEspiarPj(boolean b) {
	// _espiarPJ = b;
	// }
	public boolean getCreandoJuego() {
		return _creandoJuego;
	}
	
	public void setCreandoJuego(boolean b) {
		_creandoJuego = b;
	}
	
	private void setMisiones(String misiones) {
		for (final String str : misiones.split(Pattern.quote("|"))) {
			try {
				if (str.isEmpty()) {
					continue;
				}
				final String[] s = str.split("~");
				int idMision = Integer.parseInt(s[0]);
				int estado = Integer.parseInt(s[1]);
				int etapaMision = 0;
				int nivelEtapa = 0;
				String objetivosCumplidos = "";
				try {
					etapaMision = Integer.parseInt(s[2]);
				} catch (Exception e) {}
				try {
					nivelEtapa = Integer.parseInt(s[3]);
				} catch (Exception e) {}
				try {
					objetivosCumplidos = s[4];
				} catch (Exception e) {}
				_misiones.add(new Mision(idMision, estado, etapaMision, nivelEtapa, objetivosCumplidos));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public Cofre getBanco() {
		return _cuenta.getBanco();
	}
	
	public Trabajo getTrabajo() {
		Trabajo trabajo = (Trabajo) getIntercambiandoCon(Trabajo.class);
		return trabajo;
	}
	
	private void interrumpirReceta() {
		Trabajo trabajo = (Trabajo) getIntercambiandoCon(Trabajo.class);
		if (trabajo != null) {
			if (trabajo.esCraft()) {
				trabajo.interrumpirReceta();
				trabajo.limpiarReceta();
			}
		}
	}
	
	public void previosDesconectar() {
		interrumpirReceta();
		if (_pelea != null) {
			if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
				if (_pelea.getTipoPelea() != Constantes.PELEA_TIPO_DESAFIO) {
					_cuenta.addMensaje("Im1192;" + _nombre, false);
				}
			}
		}
	}
	
	public void rechazarGrupo() {
		if (!_tipoInvitacion.equals("grupo")) {
			return;
		}
		Personaje invitandoA, invitador;
		if (_invitador != null) {
			invitador = _invitador;
			invitandoA = this;
		} else if (_invitandoA != null) {
			invitador = this;
			invitandoA = _invitandoA;
		} else {
			GestorSalida.ENVIAR_BN_NADA(this);
			return;
		}
		invitador.setInvitandoA(null, "");
		invitandoA.setInvitador(null, "");
		GestorSalida.ENVIAR_PR_RECHAZAR_INVITACION_GRUPO(invitador);
		GestorSalida.ENVIAR_PR_RECHAZAR_INVITACION_GRUPO(invitandoA);
	}
	
	public void rechazarGremio() {
		if (!_tipoInvitacion.equals("gremio")) {
			return;
		}
		Personaje invitandoA, invitador;
		if (_invitador != null) {
			invitador = _invitador;
			invitandoA = this;
		} else if (_invitandoA != null) {
			invitador = this;
			invitandoA = _invitandoA;
		} else {
			GestorSalida.ENVIAR_BN_NADA(this);
			return;
		}
		invitador.setInvitandoA(null, "");
		invitandoA.setInvitador(null, "");
		GestorSalida.ENVIAR_gJ_GREMIO_UNIR(invitador, "Ec");
		GestorSalida.ENVIAR_gJ_GREMIO_UNIR(invitandoA, "Ec");
	}
	
	public void rechazarKoliseo() {
		if (!_tipoInvitacion.equals("koliseo")) {
			return;
		}
		Personaje invitandoA, invitador;
		if (_invitador != null) {
			invitador = _invitador;
			invitandoA = this;
		} else if (_invitandoA != null) {
			invitador = this;
			invitandoA = _invitandoA;
		} else {
			GestorSalida.ENVIAR_BN_NADA(this);
			return;
		}
		invitador.setInvitandoA(null, "");
		invitandoA.setInvitador(null, "");
		GestorSalida.ENVIAR_kR_RECHAZAR_INVITACION_KOLISEO(invitador);
		GestorSalida.ENVIAR_kR_RECHAZAR_INVITACION_KOLISEO(invitandoA);
	}
	
	public void rechazarDesafio() {
		if (!_tipoInvitacion.equals("desafio")) {
			return;
		}
		Personaje invitandoA, invitador;
		if (_invitador != null) {
			invitador = _invitador;
			invitandoA = this;
		} else if (_invitandoA != null) {
			invitador = this;
			invitandoA = _invitandoA;
		} else {
			GestorSalida.ENVIAR_BN_NADA(this);
			return;
		}
		invitador.setInvitandoA(null, "");
		invitandoA.setInvitador(null, "");
		GestorSalida.ENVIAR_GA902_RECHAZAR_DESAFIO(invitador, invitador.getID(), _id);
		GestorSalida.ENVIAR_GA902_RECHAZAR_DESAFIO(invitandoA, invitador.getID(), _id);
	}
	
	public boolean puedeInvitar() {
		return _tipoInvitacion.isEmpty();
	}
	
	public void desconectar(boolean salvar) {
		try {
			if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
				return;
			}
			if (!_enLinea) {
				return;
			}
			rechazarGrupo();
			rechazarKoliseo();
			rechazarGremio();
			rechazarDesafio();
			cerrarExchange("");
			setEnLinea(false);
			if (_pelea != null) {
				if (_pelea.esEspectador(_id) || _pelea.getTipoPelea() == Constantes.PELEA_TIPO_DESAFIO) {
					// cuando es espectador o desafio
					_pelea.retirarsePelea(_id, 0, true);
				} else if (_pelea.getFase() == Constantes.PELEA_FASE_POSICION || _pelea
				.getFase() == Constantes.PELEA_FASE_COMBATE) {
					_pelea.desconectarLuchador(this);
				}
			} else if (_mapa != null) {
				GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(_mapa, _id);
				if (esMercante()) {
					GestorSalida.ENVIAR_GM_MERCANTE_A_MAPA(_mapa, "+" + stringGMmercante());
				}
			}
			if (_pelea == null) {
				if (_grupo != null) {
					_grupo.dejarGrupo(this, false);
				}
				if (_koliseo != null) {
					_koliseo.dejarGrupo(this);
				}
				Mundo.delKoliseo(_id);
			}
			setCelda(null);
			stopTimerRegenPDV();
			resetearVariables();
			_tiempoDesconexion = System.currentTimeMillis();
		} catch (Exception e) {
			// si ocurre algo
		} finally {
			if (salvar) {
				GestorSQL.SALVAR_PERSONAJE(this, true);
			}
		}
	}
	
	public void cambiarSexo() {
		if (_sexo == Constantes.SEXO_FEMENINO) {
			_sexo = Constantes.SEXO_MASCULINO;
		} else {
			_sexo = Constantes.SEXO_FEMENINO;
		}
	}
	
	public boolean enLinea() {
		return _enLinea;
	}
	
	public void registrar(String packet) {
		if (_cuenta != null) {
			if (ServidorSocket.REGISTROS.get(_cuenta.getNombre()) == null) {
				ServidorSocket.REGISTROS.put(_cuenta.getNombre(), new StringBuilder());
			}
			ServidorSocket.REGISTROS.get(_cuenta.getNombre()).append(System.currentTimeMillis() + ": \t" + packet + "\n");
		}
	}
	
	private void setEnLinea(final boolean linea) {
		if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
			return;
		}
		_enLinea = linea;
		if (_enLinea) {
			Mundo.addOnline(this);
		} else {
			Mundo.removeOnline(this);
		}
	}
	
	public void setGrupo(final Grupo grupo) {
		_grupo = grupo;
	}
	
	public Grupo getGrupoParty() {
		if (esMultiman()) {
			return _compañero.getGrupoParty();
		}
		return _grupo;
	}
	
	public String getPtoSalvada() {
		return _mapaSalvada + "," + _celdaSalvada;
	}
	
	public int getConversandoCon() {
		return _conversandoCon;
	}
	
	public void setConversandoCon(final int conversando) {
		_conversandoCon = conversando;
	}
	
	public int getPreguntaID() {
		return _pregunta;
	}
	
	public void setPreguntaID(final int pregunta) {
		_pregunta = pregunta;
	}
	
	public void dialogoFin() {
		GestorSalida.ENVIAR_DV_FINALIZAR_DIALOGO(this);
		_conversandoCon = 0;
		_pregunta = 0;
	}
	
	public long getKamas() {
		return _kamas;
	}
	
	public void setKamasCero() {
		_kamas = 0;
	}
	
	public void addKamas(final long kamas, final boolean msj, boolean conAk) {
		if (kamas == 0) {
			return;
		}
		_kamas += kamas;
		if (_kamas >= MainServidor.LIMITE_DETECTAR_FALLA_KAMAS) {
			MainServidor.redactarLogServidorln("EL PERSONAJE " + _nombre + " (" + _id + ") CON CUENTA " + getCuentaID()
			+ " POSSE " + _kamas);
			if (!ServidorSocket.JUGADORES_REGISTRAR.contains(_cuenta.getNombre())) {
				ServidorSocket.JUGADORES_REGISTRAR.add(_cuenta.getNombre());
			}
		}
		if (_kamas < 0) {
			_kamas = 0;
		}
		if (_enLinea) {
			if (conAk) {
				GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
			}
			if (msj) {
				if (kamas < 0) {
					GestorSalida.ENVIAR_Im_INFORMACION(this, "046;" + (-kamas));
				} else if (kamas > 0) {
					GestorSalida.ENVIAR_Im_INFORMACION(this, "045;" + kamas);
				}
			}
		}
	}
	
	public synchronized boolean comprarTienda(final Personaje comprador, int cantidad, final Objeto objeto) {
		try {
			if (objeto == null || !_tienda.contains(objeto)) {
				return false;
			}
			if (cantidad < 1) {
				cantidad = 1;
			} else if (cantidad > objeto.getCantidad()) {
				cantidad = objeto.getCantidad();
			}
			if (!Formulas.valorValido(cantidad, objeto.getPrecio())) {
				GestorSalida.ENVIAR_BN_NADA(this, "INTENTO BUG MULTIPLICADOR");
				return false;
			}
			long precio = objeto.getPrecio() * cantidad;
			if (precio <= 0) {
				return false;
			}
			if (comprador.getKamas() < precio) {
				GestorSalida.ENVIAR_Im_INFORMACION(comprador, "1128;" + precio);
				return false;
			}
			comprador.addKamas(-precio, true, false);
			addKamas(precio, false, false);
			int nuevaCantidad = objeto.getCantidad() - cantidad;
			if (nuevaCantidad >= 1) {
				final Objeto nuevoObj = objeto.clonarObjeto(nuevaCantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
				nuevoObj.setPrecio(objeto.getPrecio());
				_tienda.addObjeto(nuevoObj);
				objeto.setCantidad(cantidad);
				// GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(comprador, objeto);
			}
			borrarObjTienda(objeto);
			objeto.setPrecio(0);
			comprador.addObjIdentAInventario(objeto, true);
			return true;
		} catch (final Exception e) {
			return false;
		}
	}
	
	public Cuenta getCuenta() {
		return _cuenta;
	}
	
	public int getPuntosHechizos() {
		return _puntosHechizos;
	}
	
	public Gremio getGremio() {
		if (_miembroGremio == null) {
			return null;
		}
		return _miembroGremio.getGremio();
	}
	
	public void setMiembroGremio(final MiembroGremio gremio) {
		_miembroGremio = gremio;
	}
	
	public Pelea getPelea() {
		return _pelea;
	}
	
	public boolean mostrarAmigos() {
		return _mostrarAmigos;
	}
	
	public boolean alasActivadas() {
		if (_alineacion == Constantes.ALINEACION_NEUTRAL) {
			return false;
		}
		return _mostrarAlas;
	}
	
	private void setRestriccionesA(int[][] r) {
		int restr = 0;
		int modif = 0;
		for (int[] a : r) {
			restr += a[0];
			if (a[1] == 1) {
				modif += a[0];
			}
		}
		modificarA(restr, restr - modif);
	}
	
	private void setRestriccionesB(int[][] r) {
		int restr = 0;
		int modif = 0;
		for (int[] a : r) {
			restr += a[0];
			if (a[1] == 1) {
				modif += a[0];
			}
		}
		modificarB(restr, restr - modif);
	}
	
	public int getEnergia() {
		return _energia;
	}
	
	public void addEnergiaConIm(final int energia, boolean mensaje) {
		if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
			return;
		}
		if (esMultiman()) {
			return;
		}
		if (MainServidor.MODO_HEROICO || MainServidor.MAPAS_MODO_HEROICO.contains(_mapa.getID())
		|| !MainServidor.PARAM_PERDER_ENERGIA) {
			return;
		}
		final int exEnergia = _energia;
		_energia = Math.min(10000, _energia + energia);
		if (energia > 0) {
			if (esFantasma() && exEnergia <= 0 && _energia > 0) {
				deformar();
				_ocupado = false;
				int[][] rA = {{RA_PUEDE_INTERCAMBIAR, 1}, {RA_PUEDE_HABLAR_NPC, 1}, {RA_PUEDE_MERCANTE, 1}, {
				RA_PUEDE_INTERACTUAR_RECAUDADOR, 1}, {RA_PUEDE_INTERACTUAR_PRISMA, 1}, {RA_PUEDE_USAR_OBJETOS, 1}, {
				RA_NO_PUEDE_ATACAR, 0}, {RA_PUEDE_DESAFIAR, 1}, {RA_PUEDE_INTERACTUAR_OBJETOS, 1}, {RA_PUEDE_AGREDIR, 1}};
				setRestriccionesA(rA);
				int[][] rB = {{RB_PUEDE_SER_AGREDIDO, 1}, {RB_PUEDE_SER_DESAFIADO, 1}, {RB_PUEDE_HACER_INTERCAMBIO, 1}, {
				RB_NO_ES_FANTASMA, 1}, {RB_PUEDE_CORRER, 1}, {RB_PUEDE_SER_ATACADO, 0}, {RB_NO_ES_TUMBA, 1}};
				setRestriccionesB(rB);
				refrescarEnMapa();
				GestorSalida.ENVIAR_AR_RESTRICCIONES_PERSONAJE(this);
			}
		} else {
			if (_energia <= 0) {
				convertirseTumba();
				_energia = 0;
			} else if (_energia < 1500) {
				GestorSalida.ENVIAR_M1_MENSAJE_SERVER_SVR_MUESTRA_INSTANTANEO(this, 11, _energia + "", "");
			}
		}
		if (_enLinea && mensaje) {
			if (energia > 0) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "07;" + energia);
			} else if (energia < 0) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "034;" + Math.abs(energia));
			}
		}
		GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
	}
	
	public void convertirseTumba() {
		if (esMultiman()) {
			return;
		}
		if (esTumba()) {
			return;
		}
		if (estaMontando()) {
			subirBajarMontura(false);
		}
		_energia = -1;
		int[][] rA = {{RA_PUEDE_INTERCAMBIAR, 0}, {RA_PUEDE_HABLAR_NPC, 0}, {RA_PUEDE_MERCANTE, 0}, {
		RA_PUEDE_INTERACTUAR_RECAUDADOR, 0}, {RA_PUEDE_INTERACTUAR_PRISMA, 0}, {RA_PUEDE_USAR_OBJETOS, 0}, {
		RA_NO_PUEDE_ATACAR, 1}, {RA_PUEDE_DESAFIAR, 0}, {RA_PUEDE_INTERACTUAR_OBJETOS, 0}, {RA_PUEDE_AGREDIR, 0}};
		setRestriccionesA(rA);
		int[][] rB = {{RB_PUEDE_SER_AGREDIDO, 0}, {RB_PUEDE_SER_DESAFIADO, 0}, {RB_PUEDE_HACER_INTERCAMBIO, 0}, {
		RB_NO_ES_FANTASMA, 1}, {RB_PUEDE_CORRER, 0}, {RB_PUEDE_SER_ATACADO, 0}, {RB_NO_ES_TUMBA, 0}};
		setRestriccionesB(rB);
		_gfxID = _clase.getGfxs(3);
		refrescarEnMapa();
		GestorSalida.ENVIAR_AR_RESTRICCIONES_PERSONAJE(this);
		GestorSalida.ENVIAR_M1_MENSAJE_SERVER_SVR_MUESTRA_INSTANTANEO(this, 12, "", "");
	}
	
	public void convertirseFantasma() {
		if (esMultiman()) {
			return;
		}
		if (esFantasma()) {
			return;
		}
		_energia = 0;
		_gfxID = 8004;
		int[][] rA = {{RA_PUEDE_INTERCAMBIAR, 0}, {RA_PUEDE_HABLAR_NPC, 0}, {RA_PUEDE_MERCANTE, 0}, {
		RA_PUEDE_INTERACTUAR_RECAUDADOR, 0}, {RA_PUEDE_INTERACTUAR_PRISMA, 0}, {RA_PUEDE_USAR_OBJETOS, 0}, {
		RA_NO_PUEDE_ATACAR, 1}, {RA_PUEDE_DESAFIAR, 0}, {RA_PUEDE_INTERACTUAR_OBJETOS, 0}, {RA_PUEDE_AGREDIR, 0}};
		setRestriccionesA(rA);
		int[][] rB = {{RB_PUEDE_SER_AGREDIDO, 0}, {RB_PUEDE_SER_DESAFIADO, 0}, {RB_PUEDE_HACER_INTERCAMBIO, 0}, {
		RB_NO_ES_FANTASMA, 0}, {RB_PUEDE_CORRER, 0}, {RB_PUEDE_SER_ATACADO, 0}, {RB_NO_ES_TUMBA, 1}};
		setRestriccionesB(rB);
		if (MainServidor.MODO_HEROICO || MainServidor.MAPAS_MODO_HEROICO.contains(_mapa.getID())) {
			if (!MainServidor.PARAM_HEROICO_GAME_OVER) {
				revivir(true);
				return;
			}
			if (_enLinea) {
				if (MainServidor.PARAM_MENSAJE_ASESINOS_HEROICO) {
					GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1DIE;" + _nombre);
				}
				GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(_mapa, _id);
			}
			if (_grupo != null) {
				_grupo.dejarGrupo(this, false);
			}
			if (_koliseo != null) {
				_koliseo.dejarGrupo(this);
			}
			Mundo.delKoliseo(_id);
			setCelda(null);
			resetearVariables();
			GestorSalida.ENVIAR_GO_GAME_OVER(this);
			Mundo.eliminarPersonaje(this, false);
			setEnLinea(false);
			GestorSQL.SALVAR_PERSONAJE(this, true);
		} else {// si es fantasma
			refrescarEnMapa();
			GestorSalida.ENVIAR_AR_RESTRICCIONES_PERSONAJE(this);
			String cementerio = _mapa.getSubArea().getCementerio();
			// teleport((short) 10342, (short) 223);
			if (cementerio.isEmpty()) {
				cementerio = "1188,297";
			}
			short mapaID = 1188;
			short celdaID = 297;
			try {
				mapaID = Short.parseShort(cementerio.split(",")[0]);
				celdaID = Short.parseShort(cementerio.split(",")[1]);
			} catch (Exception e) {}
			teleport(mapaID, celdaID);
			GestorSalida.ENVIAR_IH_COORDENADAS_UBICACION(this, Constantes.ESTATUAS_FENIX);
			GestorSalida.ENVIAR_M1_MENSAJE_SERVER_SVR_MUESTRA_INSTANTANEO(this, 15, "", "");
		}
	}
	
	public void revivir(final boolean aparecer) {
		if (esMultiman()) {
			return;
		}
		if (!esFantasma() && !esTumba()) {
			return;
		}
		_energia = MainServidor.MODO_HEROICO ? 10000 : 1000;
		deformar();
		_ocupado = false;
		int[][] rA = {{RA_PUEDE_INTERCAMBIAR, 1}, {RA_PUEDE_HABLAR_NPC, 1}, {RA_PUEDE_MERCANTE, 1}, {
		RA_PUEDE_INTERACTUAR_RECAUDADOR, 1}, {RA_PUEDE_INTERACTUAR_PRISMA, 1}, {RA_PUEDE_USAR_OBJETOS, 1}, {
		RA_NO_PUEDE_ATACAR, 0}, {RA_PUEDE_DESAFIAR, 1}, {RA_PUEDE_INTERACTUAR_OBJETOS, 1}, {RA_PUEDE_AGREDIR, 1}};
		setRestriccionesA(rA);
		int[][] rB = {{RB_PUEDE_SER_AGREDIDO, 1}, {RB_PUEDE_SER_DESAFIADO, 1}, {RB_PUEDE_HACER_INTERCAMBIO, 1}, {
		RB_NO_ES_FANTASMA, 1}, {RB_PUEDE_CORRER, 1}, {RB_PUEDE_SER_ATACADO, 0}, {RB_NO_ES_TUMBA, 1}};
		setRestriccionesB(rB);
		if (aparecer && _pelea == null) {
			refrescarEnMapa();
		}
		if (_enLinea) {
			GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
			GestorSalida.ENVIAR_AR_RESTRICCIONES_PERSONAJE(this);
			GestorSalida.ENVIAR_IH_COORDENADAS_UBICACION(this, "");
			GestorSalida.ENVIAR_Im_INFORMACION(this, "033");
		}
	}
	
	public int getNivelGremio() {
		if (_miembroGremio == null) {
			return 0;
		}
		return _miembroGremio.getGremio().getNivel();
	}
	
	public int getNivel() {
		return _nivel;
	}
	
	public void setNivel(final int nivel) {
		_nivel = nivel;
	}
	
	public long getExperiencia() {
		return _experiencia;
	}
	
	public Celda getCelda() {
		return _celda;
	}
	
	public synchronized void setCelda(final Celda celda) {
		boolean difMapa = celda == null || _celda == null ? true : (celda.getMapa() != _celda.getMapa());
		if (esMultiman()) {
			difMapa = false;
		}
		if (_celda != null) {
			_celda.removerPersonaje(this, difMapa || !_enLinea);
		}
		if (celda != null) {
			_celda = celda;
			_celda.addPersonaje(this, difMapa && _enLinea);
		}
	}
	
	public int getTalla() {
		return _talla;
	}
	
	public void setTalla(final short talla) {
		_talla = talla;
	}
	
	public void setPelea(final Pelea pelea) {
		if (_pelea != null && pelea == null) {
			if (_compañero != null) {
				GestorSalida.ENVIAR_AI_CAMBIAR_ID(this, _id);
				GestorSalida.ENVIAR_SL_LISTA_HECHIZOS(this);
				_compañero.setCompañero(null);
				_compañero = null;
				invocacionControlable.clear();
			}
			else if(!invocacionControlable.isEmpty()){
				GestorSalida.ENVIAR_AI_CAMBIAR_ID(this, _id);
				GestorSalida.ENVIAR_SL_LISTA_HECHIZOS(this);
				invocacionControlable.clear();
			}
			GestorSalida.ENVIAR_ILS_TIEMPO_REGENERAR_VIDA(this, 1000);
		}
		_pelea = pelea;
	}
	
	private void disminuirTurnos() {
		if (_montando && _montura != null) {
			_montura.energiaPerdida(5);
		}
		for (byte pos : Constantes.POSICIONES_EQUIPAMIENTO) {
			try {
				final Objeto obj = getObjPosicion(pos);
				if (obj == null) {
					continue;
				}
				final String param = obj.getParamStatTexto(Constantes.STAT_TURNOS, 3);
				if (param.isEmpty()) {
					continue;
				}
				final int turnos = Integer.parseInt(param, 16);
				if (turnos == 1) {
					borrarOEliminarConOR(obj.getID(), true);
				} else if (turnos > 1) {
					obj.addStatTexto(Constantes.STAT_TURNOS, "0#0#" + Integer.toString(turnos - 1, 16));
					GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(this, obj);
				}
			} catch (final Exception e) {}
		}
		if (MainServidor.PARAM_PERDER_PDV_ARMAS_ETEREAS) {
			Objeto arma = getObjPosicion(Constantes.OBJETO_POS_ARMA);
			if (arma != null && arma.getObjModelo().esEtereo()) {
				if (arma.addDurabilidad(-1)) {
					borrarOEliminarConOR(arma.getID(), true);
					GestorSalida.ENVIAR_Im_INFORMACION(this, "160");
				} else {
					GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(this, arma);
				}
			}
		}
	}
	
	public int getGfxID(boolean rolePlayBuff) {
		if (_encarnacion != null) {
			return _encarnacion.getGfxID();
		}
		int gfx = _totalStats.getStatsObjetos().getStatParaMostrar(Constantes.STAT_CAMBIA_APARIENCIA_2);
		if (rolePlayBuff && gfx != 0) {
			return gfx;
		}
		gfx = _totalStats.getStatsObjetos().getStatParaMostrar(Constantes.STAT_CAMBIA_APARIENCIA);
		if (gfx != 0) {
			return gfx;
		}
		return _gfxID;
	}
	
	public int getGfxIDReal() {
		return _gfxID;
	}
	
	public void setGfxID(final short gfxid) {
		_gfxID = gfxid;
	}
	
	public void deformar() {
		if (_encarnacion != null) {
			_gfxID = _encarnacion.getGfxID();
		} else {
			_gfxID = (_claseID * 10 + _sexo);
		}
	}
	
	public int getID() {
		return _id;
	}
	
	public Mapa getMapa() {
		return _mapa;
	}
	
	public String getNombre() {
		return _nombre;
	}
	
	public boolean estaDisponible(boolean muerto, boolean otros) {
		if (estaOcupado() || getPelea() != null) {
			return false;
		}
		if (otros) {
			if (estaFullOcupado()) {
				return false;
			}
		}
		if (muerto) {
			if (esFantasma() || esTumba()) {
				return false;
			}
		}
		return true;
	}
	
	public boolean estaFullOcupado() {
		if (_conversandoCon != 0 || _exchanger != null || !puedeInvitar() || estaExchange()) {
			return true;
		}
		return false;
	}
	
	private boolean estaOcupado() {
		return _ocupado;
	}
	
	public void setOcupado(final boolean ocupado) {
		_ocupado = ocupado;
	}
	
	public boolean estaSentado() {
		return _sentado;
	}
	
	public byte getSexo() {
		return _sexo;
	}
	
	public byte getClaseID(final boolean original) {
		if (!original && _encarnacion != null) {
			return 20;
		}
		return _claseID;
	}
	
	public void setClaseID(final byte clase) {
		_claseID = clase;
	}
	
	public int getColor1() {
		return _color1;
	}
	
	public int getColor2() {
		return _color2;
	}
	
	public int getColor3() {
		return _color3;
	}
	
	public int getCapital() {
		return _puntosStats;
	}
	
	public void resetearStats(final boolean todo) {
		// ya contiene resfrescarStuff
		if (todo) {
			reiniciarSubStats(_subStatsScroll);
		}
		reiniciarSubStats(_subStatsBase);
		_totalStats.getStatsBase().nuevosStatsBase(_subStatsScroll, this);
		_puntosStats = ((_nivel - 1) * MainServidor.PUNTOS_STATS_POR_NIVEL) + (MainServidor.BONUS_RESET_PUNTOS_STATS
		* _resets) + MainServidor.INICIO_PUNTOS_STATS;
		refrescarStuff(true, true, false);
	}
	
	public boolean cambiarClase(byte clase) {// cambiar raza
		if (clase < 1) {
			clase = 1;
		} else if (clase > 12) {
			clase = 12;
		}
		if (clase == getClaseID(true)) {
			GestorSalida.ENVIAR_BN_NADA(this, "CAMBIAR CLASE - MISMA CLASE");
			return false;
		}
		_claseID = clase;
		_clase = Mundo.getClase(_claseID);
		GestorSalida.ENVIAR_AC_CAMBIAR_CLASE(this, getClaseID(true));
		deformar();
		for (final HechizoPersonaje hp : _hechizos) {
			for (int i = 1; i < hp.getNivel(); i++) {
				_puntosHechizos += i;
			}
		}
		fijarHechizosInicio();
		// _puntosHechizos = (_nivel - 1) + (MainServidor.BONUS_RESET_PUNTOS_HECHIZOS * _resets);
		resetearStats(false);
		refrescarEnMapa();
		GestorSQL.CAMBIAR_SEXO_CLASE(this);
		return true;
	}
	
	public byte getResets() {
		return _resets;
	}
	
	public boolean aumentarReset() {
		if (_resets >= MainServidor.MAX_RESETS) {
			return false;
		}
		_encarnacion = null;
		_resets++;
		int difNivel = _nivel - MainServidor.INICIO_NIVEL;
		_nivel = MainServidor.INICIO_NIVEL;
		_alineacion = Constantes.ALINEACION_NEUTRAL;
		_mostrarAlas = false;
		_honor = 0;
		_deshonor = 0;
		_gradoAlineacion = 1;
		_experiencia = Mundo.getExpPersonaje(_nivel);
		monturaACertificado();
		_porcXPMontura = 0;
		_restriccionesALocalPlayer = 8200;
		_restriccionesBCharacter = 8;
		resetearTodosHechizos();
		_puntosHechizos += MainServidor.BONUS_RESET_PUNTOS_HECHIZOS;
		_puntosHechizos -= difNivel;
		resetearStats(false);
		fullPDV();// FIXME
		refrescarEnMapa();
		return true;
	}
	
	private void monturaACertificado() {
		try {
			if (_montura == null)
				return;
			if (estaMontando()) {
				subirBajarMontura(false);
			}
			final Objeto obj1 = _montura.getObjModCertificado().crearObjeto(1, Constantes.OBJETO_POS_NO_EQUIPADO,
			CAPACIDAD_STATS.RANDOM);
			obj1.fijarStatValor(Constantes.STAT_CONSULTAR_MONTURA, Math.abs(_montura.getID()));
			obj1.addStatTexto(Constantes.STAT_PERTENECE_A, "0#0#0#" + _nombre);
			obj1.addStatTexto(Constantes.STAT_NOMBRE, "0#0#0#" + _montura.getNombre());
			addObjetoConOAKO(obj1, true);
			_montura.setPergamino(obj1.getID());
			GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(this);
			GestorSalida.ENVIAR_Re_DETALLES_MONTURA(this, "-", null);
			GestorSQL.REPLACE_MONTURA(_montura, false);
			_montura = null;
		} catch (Exception e) {}
	}
	
	public void reiniciarCero() {
		if (esMultiman()) {
			return;
		}
		_encarnacion = null;
		revivir(false);
		_ultimoNivel = _nivel;
		_nivel = MainServidor.INICIO_NIVEL;
		_kamas = 0;
		_puntosHechizos = (_nivel - 1) + (MainServidor.BONUS_RESET_PUNTOS_STATS * _resets);
		_alineacion = Constantes.ALINEACION_NEUTRAL;
		_mostrarAlas = false;
		_honor = 0;
		_deshonor = 0;
		_gradoAlineacion = 1;
		_energia = 10000;
		_experiencia = Mundo.getExpPersonaje(_nivel);
		_montura = null;
		_porcXPMontura = 0;
		_talla = _clase.getTallas(_sexo);
		_gfxID = _clase.getGfxs(_sexo);
		final short mapaID = _clase.getMapaInicio();
		final short celdaID = _clase.getCeldaInicio();
		_mapa = Mundo.getMapa(mapaID);
		if (_mapa == null) {
			_mapa = Mundo.getMapa((short) 7411);
		}
		setCelda(_mapa.getCelda(celdaID));
		setPuntoSalvada(mapaID + "," + celdaID);
		_tienda.clear();
		_esMercante = false;
		fullPDV();
		_statsOficios.clear();
		if (MainServidor.PARAM_PERMITIR_OFICIOS) {
			_statsOficios.put((byte) 7, new StatOficio((byte) 7, Mundo.getOficio(1), 0));
		}
		_restriccionesALocalPlayer = 8200;
		_restriccionesBCharacter = 8;
		fijarHechizosInicio();
		resetearStats(true);
		_enLinea = true;
		GestorSalida.ENVIAR_ALK_LISTA_DE_PERSONAJES(this, _cuenta);
		_enLinea = false;
		GestorSQL.SALVAR_PERSONAJE(this, false);
	}
	
	public boolean tieneHechizoID(final int hechizoID) {
		if (_encarnacion != null) {
			return _encarnacion.tieneHechizoID(hechizoID);
		}
		return getHechizoPersonajePorID(hechizoID) != null;
	}
	
	public boolean boostearFullTodosHechizos() {
		if (_encarnacion != null) {
			return false;
		}
		for (HechizoPersonaje h : _hechizos) {
			if (h == null) {
				continue;
			}
			final int antNivel = h.getStatHechizo().getGrado();
			if (antNivel >= 6) {
				continue;
			}
			while (fijarNivelHechizoOAprender(h.getHechizo().getID(), h.getStatHechizo().getGrado() + 1, false)) {}
		}
		GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
		return true;
	}
	public boolean unboostearHechizo(final int hechizoID) {// subir hechizo
		if (_encarnacion != null) {
			return false;
		}
		HechizoPersonaje h = getHechizoPersonajePorID(hechizoID);
		if (h == null) {
			return false;
		}
		final int antNivel = h.getStatHechizo().getGrado();
		if (antNivel <= 1) {
			return false;
		}

		int nivel = antNivel - 1;
		if (!fijarNivelHechizoOAprender(hechizoID, nivel, false)) {
			return false;
		}
		_puntosHechizos += nivel;
		GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
		return true;
	}
	public boolean boostearHechizo(final int hechizoID) {// subir hechizo
		if (_encarnacion != null) {
			return false;
		}
		HechizoPersonaje h = getHechizoPersonajePorID(hechizoID);
		if (h == null) {
			return false;
		}
		final int antNivel = h.getStatHechizo().getGrado();
		if (antNivel >= 6) {
			return false;
		}
		if (_puntosHechizos < antNivel) {
			return false;
		}
		if (!fijarNivelHechizoOAprender(hechizoID, antNivel + 1, false)) {
			return false;
		}
		_puntosHechizos -= antNivel;
		GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
		return true;
	}
	
	public void resetStatsNoparcho(final boolean todo) {
        // ya contiene resfrescarStuff
		reiniciarSubStats(_subStatsBase);
		_totalStats.getStatsBase().nuevosStatsBase(_subStatsScroll, this);
		_puntosStats = ((_nivel - 1) * MainServidor.PUNTOS_STATS_POR_NIVEL) + (MainServidor.BONUS_RESET_PUNTOS_STATS
		* _resets) + MainServidor.INICIO_PUNTOS_STATS+(
				(Mundo.getMasterResetExist(_resets) && Mundo.getMasterReset(_resets).isReinicia())?Mundo.getMasterReset(_resets).getCapital():0);
		refrescarStuff(true, true, false);
    }
	
	public void resetearTodosHechizos() {
		if (_encarnacion != null) {
			return;
		}
		final ArrayList<HechizoPersonaje> hechizos = new ArrayList<HechizoPersonaje>();
		hechizos.addAll(_hechizos);
		for (final HechizoPersonaje hp : hechizos) {
			for (int i = 1; i < hp.getNivel(); i++) {
				_puntosHechizos += i;
			}
			hp.setNivel(1);
			fijarNivelHechizoOAprender(hp.getHechizo().getID(), hp.getStatHechizo().getNivelRequerido() > _nivel ? 0 : 1,
			false);
		}
		GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
		GestorSalida.ENVIAR_SL_LISTA_HECHIZOS(this);
	}
	
	public boolean olvidarHechizo(final int hechizoID, boolean porCompleto, boolean mensaje) {
		if (_encarnacion != null) {
			return false;
		}
		HechizoPersonaje h = getHechizoPersonajePorID(hechizoID);
		if (h == null) {
			return false;
		}
		for (int i = 1; i < h.getNivel(); i++) {
			_puntosHechizos += i;
		}
		fijarNivelHechizoOAprender(hechizoID, porCompleto ? 0 : 1, false);
		if (mensaje) {
			GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
		}
		return true;
	}
	
	private HechizoPersonaje getHechizoPersonajePorID(int hechizoID) {
		HechizoPersonaje h = null;
		for (HechizoPersonaje hp : _hechizos) {
			if (hp.getStatHechizo().getHechizoID() == hechizoID) {
				h = hp;
				break;
			}
		}
		return h;
	}
	
	private HechizoPersonaje getHechizoPersonajePorPos(char pos) {
		HechizoPersonaje h = null;
		for (HechizoPersonaje hp : _hechizos) {
			if (hp.getPosicion() == pos) {
				h = hp;
				break;
			}
		}
		return h;
	}
	
	private void fijarHechizosInicio() {
		if (esMultiman()) {
			return;
		}
		ArrayList<StatHechizo> tempHechizos = new ArrayList<>();
		for (HechizoPersonaje h : _hechizos) {
			if (h.getStatHechizo() == null) {
				continue;
			}
			switch (h.getStatHechizo().getTipo()) {
				case 0 :// normales
				case 4 : // de clase
					continue;
			}
			tempHechizos.add(h.getStatHechizo());
		}
		_hechizos.clear();
		// _mapStatsHechizos.clear();
		for (int nivel = 1; nivel <= _nivel; nivel++) {
			_clase.aprenderHechizo(this, nivel);
		}
		int i = 1;
		for (HechizoPersonaje hp : _hechizos) {
			hp.setPosicion(Encriptador.getValorHashPorNumero(i));
			i++;
		}
		for (StatHechizo sh : tempHechizos) {
			fijarNivelHechizoOAprender(sh.getHechizoID(), sh.getGrado(), false);
		}
		if (_enLinea) {
			GestorSalida.ENVIAR_SL_LISTA_HECHIZOS(this);
		}
	}
	
	public boolean fijarNivelHechizoOAprender(final int hechizoID, final int nivel, final boolean mensaje) {
		if (_encarnacion != null) {
			return false;
		}
		if (nivel > 0) {
			final Hechizo hechizo = Mundo.getHechizo(hechizoID);
			if (hechizo == null) {
				return false;
			}
			final StatHechizo statHechizo = hechizo.getStatsPorNivel(nivel);
			if (statHechizo == null || statHechizo.getNivelRequerido() > _nivel) {
				return false;
			}
			HechizoPersonaje h = getHechizoPersonajePorID(hechizoID);
			if (h == null) {
				addHechizoPersonaje('_', hechizo, nivel);
			} else {
				h.setNivel(nivel);
				// _mapStatsHechizos.put(hechizo.getID(), statHechizo);
			}
		} else {
			HechizoPersonaje h = getHechizoPersonajePorID(hechizoID);
			if (h == null) {
				return false;
			}
			_hechizos.remove(h);
			// _mapStatsHechizos.remove(hechizoID);
		}
		if (_enLinea) {
			GestorSalida.ENVIAR_SUK_NIVEL_HECHIZO(this, hechizoID, nivel);
			if (mensaje) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "03;" + hechizoID);
			}
		}
		return true;
	}
	
	private void analizarPosHechizos(final String str) {
		for (final String s : str.split(";")) {
			try {
				String[] split = s.split(",");
				int id = Integer.parseInt(split[0]);
				int nivel = Integer.parseInt(split[1]);
				char pos = split[2].charAt(0);
				HechizoPersonaje h2 = getHechizoPersonajePorPos(pos);
				if (h2 != null) {
					h2.setPosicion('_');
				}
				addHechizoPersonaje(pos, Mundo.getHechizo(id), nivel);
			} catch (final Exception e) {}
		}
	}
	
	public void setPosHechizo(final int hechizoID, final char pos) {
		if (_encarnacion != null) {
			_encarnacion.setPosHechizo(hechizoID, pos, this);
			return;
		}
		if (pos == 'a') {
			GestorSalida.ENVIAR_BN_NADA(this, "SET POS HECHIZO - POS INVALIDA");
			return;
		}
		HechizoPersonaje h = getHechizoPersonajePorID(hechizoID);
		if (h == null) {
			GestorSalida.ENVIAR_BN_NADA(this, "SET POS HECHIZO - NO TIENE HECHIZO");
			return;
		}
		HechizoPersonaje h2 = getHechizoPersonajePorPos(pos);
		if (h2 != null) {
			h2.setPosicion('_');
		}
		h.setPosicion(pos);
		GestorSalida.ENVIAR_BN_NADA(this);
	}
	
	public StatHechizo getStatsHechizo(final int hechizoID) {
		if (_encarnacion != null) {
			return _encarnacion.getStatsHechizo(hechizoID);
		}
		HechizoPersonaje h = getHechizoPersonajePorID(hechizoID);
		if (h == null) {
			return null;
		}
		return h.getStatHechizo();
	}
	
	private void addHechizoPersonaje(char pos, Hechizo hechizo, int nivel) {
		_hechizos.add(new HechizoPersonaje(pos, hechizo, nivel));
		// _mapStatsHechizos.put(hechizo.getID(), hechizo.getStatsPorNivel(nivel));
	}
	
	public String stringHechizosParaSQL() {
		final StringBuilder str = new StringBuilder();
		for (HechizoPersonaje hp : _hechizos) {
			if (hp.getHechizo() == null) {
				continue;
			}
			if (str.length() > 0) {
				str.append(";");
			}
			str.append(hp.getHechizo().getID() + "," + hp.getNivel() + "," + hp.getPosicion());
		}
		return str.toString();
	}
	
	public String stringListaHechizos() {
		if (_encarnacion != null) {
			return _encarnacion.stringListaHechizos();
		}
		final StringBuilder str = new StringBuilder();
		for (HechizoPersonaje hp : _hechizos) {
			if (hp.getHechizo() == null) {
				continue;
			}
			if (str.length() > 0) {
				str.append(";");
			}
			str.append(hp.getHechizo().getID() + "~" + hp.getNivel() + "~" + hp.getPosicion());
		}
		return str.toString();
	}
	
	public boolean sePuedePonerEncarnacion() {
		return System.currentTimeMillis() - _tiempoUltEncarnacion > 60000;
	}
	
	public String stringParaListaPJsServer() {
		final StringBuilder str = new StringBuilder("|");
		str.append(_id + ";");
		str.append(_nombre + ";");
		str.append(_nivel + ";");
		str.append(getGfxID(false) + ";");
		str.append((_color1 > -1 ? Integer.toHexString(_color1) : -1) + ";");
		str.append((_color2 > -1 ? Integer.toHexString(_color2) : -1) + ";");
		str.append((_color3 > -1 ? Integer.toHexString(_color3) : -1) + ";");
		str.append(getStringAccesorios() + ";");
		str.append((_esMercante ? 1 : 0) + ";");
		str.append(MainServidor.SERVIDOR_ID + ";");
		if (MainServidor.MODO_HEROICO || MainServidor.MAPAS_MODO_HEROICO.contains(_mapa.getID())) {
			str.append((esFantasma() ? 1 : 0) + ";");
		} else {
			str.append("0;");
		}
		str.append(";");
		str.append(MainServidor.NIVEL_MAX_PERSONAJE);
		return str.toString();
	}
	
	public void mostrarAmigosEnLinea(final boolean mostrar) {
		_mostrarAmigos = mostrar;
	}
	
	public void mostrarRates() {
		if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
			GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(this, "<b>Bienvenu sur " + MainServidor.NOMBRE_SERVER
			+ ": \nPrix ressource : " + MainServidor.PRECIO_SISTEMA_RECURSO + "\nKamas par : " + MainServidor.RATE_KAMAS
			+ "   \nDrop par : " + MainServidor.RATE_DROP_NORMAL + "\nXP PVM par : " + MainServidor.RATE_XP_PVM
			+ "   \nXP PVP par : " + MainServidor.RATE_XP_PVP + "\nHonor par : " + MainServidor.RATE_HONOR
			+ "\nXP metier par : " + MainServidor.RATE_XP_OFICIO + " \nRate Elevage par : "
			+ MainServidor.RATE_CRIANZA_MONTURA + "\nTemps pour mettre bas par : " + MainServidor.MINUTOS_GESTACION_MONTURA
			+ " minutos" + " \nLes familiers se nourissant toutes les " + MainServidor.MINUTOS_ALIMENTACION_MASCOTA
			+ " minutos</b>");
		} else {
			GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(this, "<b>BIENVENIDO A " + MainServidor.NOMBRE_SERVER
			+ ": \nPRECIO RECURSO : " + MainServidor.PRECIO_SISTEMA_RECURSO + "\nKAMAS por : " + MainServidor.RATE_KAMAS
			+ "   \nDROP por : " + MainServidor.RATE_DROP_NORMAL + "\nXP PVM por : " + MainServidor.RATE_XP_PVM
			+ "   \nXP PVP por : " + MainServidor.RATE_XP_PVP + "\nHONOR por : " + MainServidor.RATE_HONOR
			+ "\nXP OFICIO por : " + MainServidor.RATE_XP_OFICIO + " \nCRIANZA DE PAVOS por : "
			+ MainServidor.RATE_CRIANZA_MONTURA + "\nTIEMPO PARIR MONTURA por : " + MainServidor.MINUTOS_GESTACION_MONTURA
			+ " minutos" + " \nLAS MASCOTAS SE ALIMENTARAN CADA " + MainServidor.MINUTOS_ALIMENTACION_MASCOTA
			+ " minutos</b>");
		}
	}
	
	public void mostrarTutorial() {
		if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
			if (!MainServidor.TUTORIAL_FR.isEmpty())
				GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(this, MainServidor.TUTORIAL_FR);
		} else {
			if (!MainServidor.TUTORIAL_ES.isEmpty())
				GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(this, MainServidor.TUTORIAL_ES);
		}
	}
	
	public String strRopaDelPJ() {// ropa del personaje, stuff del personaje
		return "Oa" + _id + "|" + getStringAccesorios();
	}
	
	public String stringGMmercante() {
		final StringBuilder str = new StringBuilder();
		str.append(_celda.getID() + ";");
		str.append("1;");
		str.append("0;");
		str.append(_id + ";");
		str.append(_nombre + "^" + _colorNombre + ";");
		str.append("-5" + ",");
		int titulo = getTitulo(false);
		if (titulo > 0) {
			str.append(titulo);
			if (_titulos.containsKey(titulo) && _titulos.get(titulo) > -1) {
				str.append("**" + _titulos.get(titulo));
			}
		}
		str.append(";");
		str.append(getGfxID(false) + "^" + _talla + ";");
		str.append((_color1 == -1 ? "-1" : Integer.toHexString(_color1)) + ";");
		str.append((_color2 == -1 ? "-1" : Integer.toHexString(_color2)) + ";");
		str.append((_color3 == -1 ? "-1" : Integer.toHexString(_color3)) + ";");
		str.append(getStringAccesorios() + ";");
		// str.append(_miembroGremio.getGremio().getNombre() + ";" +
		// _miembroGremio.getGremio().getEmblema() + ";");
		if (_miembroGremio != null && _miembroGremio.getGremio().getCantidadMiembros() >= 10) { 
			str.append(_miembroGremio.getGremio().getNombre() + ";" + _miembroGremio.getGremio().getEmblema() + ";");
		} else {
			str.append(";;");
		}
		str.append("0");
		return str.toString();
	}
	
	public String stringGM() {
		final StringBuilder str = new StringBuilder();
		if (_pelea != null) {
			return "";
		}
		str.append(_celda.getID() + ";");
		str.append(_orientacion + ";");
		str.append(_ornamento + "^" + (MainServidor.PARAM_AURA_VIP ? ((esAbonado() ? 1 : 0)) : "") + ";");
		str.append(_id + ";");
		str.append(_nombre + "^" + _colorNombre + ";");
		str.append(_claseID + ",");
		int titulo = getTitulo(false);
		if (titulo > 0) {
			str.append(titulo);
			if (_titulos.containsKey(titulo) && _titulos.get(titulo) > -1) {
				str.append("**" + _titulos.get(titulo));
			}
		}
		if (!_tituloVIP.isEmpty()) {
			str.append("~" + _tituloVIP);
		}
		str.append(";");
		str.append(getGfxID(true) + "^" + _talla + stringSeguidores() + ";");
		str.append(_sexo + ";");
		str.append(_alineacion + ",");
		str.append(_ordenNivel + ",");
		str.append((alasActivadas() ? _gradoAlineacion : "0") + ",");
		str.append((_id + _nivel) + ",");
		str.append((_deshonor > 0 ? 1 : 0) + ";");
		str.append((_color1 < 0 ? "-1" : Integer.toHexString(_color1)) + ";");
		str.append((_color2 < 0 ? "-1" : Integer.toHexString(_color2)) + ";");
		str.append((_color3 < 0 ? "-1" : Integer.toHexString(_color3)) + ";");
		str.append(getStringAccesorios() + ";");
		if (MainServidor.PARAM_ACTIVAR_AURA) {
			int aura = _totalStats.getStatsObjetos().getStatParaMostrar(Constantes.STAT_AURA);
			str.append(aura != 0 ? aura : ((_nivel / 100)) + ";");
		} else {
			str.append("0;");
		}
		str.append(";");
		str.append(";");
		if (_miembroGremio != null && _miembroGremio.getGremio().getCantidadMiembros() >= 1) { //condición de emblema gremio
			str.append(_miembroGremio.getGremio().getNombre() + ";" + _miembroGremio.getGremio().getEmblema() + ";");
		} else {
			str.append(";;");
		}
		str.append(Integer.toString(_restriccionesBCharacter, 36) + ";");
		str.append((_montando && _montura != null ? _montura.getStringColor(stringColor()) : "") + ";");// 19
		str.append(Math.max(0, _totalStats.getStatsObjetos().getStatParaMostrar(Constantes.STAT_MAS_VELOCIDAD) / 1000f)
		+ ";");
		str.append(_resets + ";");
		return str.toString();
	}
	
	public String getStringAccesorios() {
		final StringBuilder str = new StringBuilder();
		str.append(strObjEnPosParaOa(Constantes.OBJETO_POS_ARMA) + ",");// arma
		str.append(strObjEnPosParaOa(Constantes.OBJETO_POS_SOMBRERO) + ",");// sombrero
		str.append(strObjEnPosParaOa(Constantes.OBJETO_POS_CAPA) + ",");// capa
		str.append(strObjEnPosParaOa(Constantes.OBJETO_POS_MASCOTA) + ",");// mascota
		str.append(strObjEnPosParaOa(Constantes.OBJETO_POS_ESCUDO) + ",");// escudo
		str.append(_rostro + ",");// change face
		try {
			if (getMiembroGremio() != null) {
				final String[] args = getMiembroGremio().getGremio().getEmblema().split(",");
				final String colorEscudo = Integer.toHexString(Integer.parseInt(args[1], 36));
				final int emblemaID = Integer.parseInt(args[2], 36);
				final int colorEmblema = Integer.parseInt(args[3], 36) + 1;
				str.append(colorEscudo + "~" + emblemaID + "~" + colorEmblema);
			}
		} catch (final Exception e) {}
		return str.toString();
	}
	
	public String stringStats() {
		final StringBuilder str = new StringBuilder("As");
		str.append(stringStatsComplemento());
		str.append(getIniciativa() + "|");
		int base = 0, equipo = 0, bendMald = 0, buff = 0, total = 0;
		total = _totalStats.getTotalStatConComplemento(Constantes.STAT_MAS_PROSPECCION);
		// prospeccion
		str.append(total + "|");
		final int[] stats = {111, 128, 118, 125, 124, 123, 119, 126, 117, 182, 112, 142, 165, 138, 178, 225, 226, 220, 115,
		122, 160, 161, 244, 214, 264, 254, 240, 210, 260, 250, 241, 211, 261, 251, 242, 212, 262, 252, 243, 213, 263, 253,
		410, 413, 419, 416, 415, 417, 418};
		for (final int s : stats) {
			base = _totalStats.getStatsBase().getStatParaMostrar(s);
			equipo = _totalStats.getStatsObjetos().getStatParaMostrar(s);
			bendMald = _totalStats.getStatsBendMald().getStatParaMostrar(s);
			buff = _totalStats.getStatsBuff().getStatParaMostrar(s);
			total = _totalStats.getTotalStatParaMostrar(s);
			str.append(base + "," + equipo + "," + bendMald + "," + buff + "," + (total) + "|");
		}
		return str.toString();
	}
	
	public String stringStats2() {
		final StringBuilder str = new StringBuilder("Ak");
		str.append(stringStatsComplemento());
		return str.toString();
	}
	
	private String stringStatsComplemento() {
		final StringBuilder str = new StringBuilder();
		str.append(stringExperiencia(",") + "|");
		str.append(_kamas + "|");
		if (_encarnacion != null) {
			str.append("0|0|");
		} else {
			str.append(_puntosStats + "|" + _puntosHechizos + "|");
		}
		str.append(_alineacion + "~");
		str.append(_alineacion + ",");// fake alineacion, si son diferentes se activa haveFakeAlignment
		str.append(_ordenNivel + ",");// orden alineacion
		str.append(_gradoAlineacion + ",");// nValue
		str.append(_honor + ",");// nHonour
		str.append(_deshonor + ",");// nDisgrace
		str.append((alasActivadas() ? "1" : "0") + "|");// bEnabled
		int PDV = getPDV();
		int PDVMax = getPDVMax();
		if (_pelea != null && _pelea.getLuchadorPorID(_id) != null) {
			final Luchador luchador = _pelea.getLuchadorPorID(_id);
			if (luchador != null) {
				PDV = luchador.getPDVConBuff();
				PDVMax = luchador.getPDVMaxConBuff();
			}
		}
		str.append(PDV + "," + PDVMax + "|");
		str.append(_energia + ",10000|");
		return str.toString();
	}
	
	public String stringExperiencia(final String c) {
		return Mundo.getExpPersonaje(_nivel) + c + _experiencia + c + Mundo.getExpPersonaje(_nivel + 1);
	}
	
	public int emoteActivado() {
		return _emoteActivado;
	}
	
	public void setEmoteActivado(final byte emoteActivado) {
		_emoteActivado = emoteActivado;
	}
	
	public Collection<Objeto> getObjetosTodos() {
		return _objetos.values();
	}
	
	public void actualizarObjEquipStats() {
		_totalStats.getStatsObjetos().clear();
		boolean esEncarnacion = false;
		final ArrayList<Integer> listaSetsEquipados = new ArrayList<Integer>();
		for (byte pos : Constantes.POSICIONES_EQUIPAMIENTO) {
			final Objeto objeto = getObjPosicion(pos);
			if (objeto == null) {
				continue;
			}
			if (objeto.getEncarnacion() != null) {
				esEncarnacion = true;
			}
			_totalStats.getStatsObjetos().acumularStats(objeto.getStats());
			final int setID = objeto.getObjModelo().getSetID();
			if (setID > 0 && !listaSetsEquipados.contains(setID)) {
				listaSetsEquipados.add(setID);
				final ObjetoSet OS = Mundo.getObjetoSet(setID);
				if (OS != null) {
					_totalStats.getStatsObjetos().acumularStats(OS.getBonusStatPorNroObj(getNroObjEquipadosDeSet(setID)));
				}
			}
		} // actualizando
		if (esEncarnacion) {
			_montando = false;
		} else if (_montando && _montura != null) {
			_totalStats.getStatsObjetos().acumularStats(_montura.getStats());
		}
	}
	
	public Encarnacion getEncarnacionN() {
		return _encarnacion;
	}
	
	private int getIniciativa() {
		return Formulas.getIniciativa(getTotalStats(), getPorcPDV());
	}
	
	public TotalStats getTotalStats() {
		return _totalStats;
	}
	
	public TotalStats getTotalStatsPelea() {
		if (_encarnacion != null && _encarnacion.getTotalStats() != null) {
			return _encarnacion.getTotalStats();
		}
		return _totalStats;
	}
	
	public byte getOrientacion() {
		return _orientacion;
	}
	
	public void setOrientacion(final byte orientacion) {
		_orientacion = orientacion;
	}
	
	public int getPodsUsados() {
		int pods = 0;
		for (final Objeto objeto : _objetos.values()) {
			if (objeto == null) {
				continue;
			}
			if (objeto.getObjModelo() == null) {
				MainServidor.redactarLogServidorln("El objeto " + objeto.getID() + ", objModelo " + objeto.getObjModeloID()
				+ " nulo OBJETOS");
				continue;
			}
			pods += Math.abs(objeto.getObjModelo().getPeso() * objeto.getCantidad());
		}
		for (final Objeto objeto : _tienda.getObjetos()) {
			if (objeto == null) {
				continue;
			}
			if (objeto.getObjModelo() == null) {
				MainServidor.redactarLogServidorln("El objeto " + objeto.getID() + ", objModelo " + objeto.getObjModeloID()
				+ " nulo TIENDA");
				continue;
			}
			pods += Math.abs(objeto.getObjModelo().getPeso() * objeto.getCantidad());
		}
		return pods;
	}
	
	public int getPodsMaximos() {
		int pods = _totalStats.getTotalStatParaMostrar(Constantes.STAT_MAS_PODS);
		pods += _totalStats.getStatsBase().getStatParaMostrar(Constantes.STAT_MAS_FUERZA) * 5;
		pods += _totalStats.getStatsObjetos().getStatParaMostrar(Constantes.STAT_MAS_FUERZA) * 5;
		pods += _totalStats.getStatsBendMald().getStatParaMostrar(Constantes.STAT_MAS_FUERZA) * 5;
		for (final StatOficio SO : _statsOficios.values()) {
			if (SO == null) {
				continue;
			}
			if (SO.getPosicion() == 7) {
				continue;
			}
			pods += SO.getNivel() * 5;
			if (SO.getNivel() >= 100) {
				pods += 1000;
			}
		}
		pods *= MainServidor.RATE_PODS;
		if (pods < 1000) {
			pods = 1000;
		}
		return pods;
	}
	
	public int getPDV() {
		return _PDV;
	}
	
	public int getPDVMax() {
		return _PDVMax;
	}
	
	public void addPDV(int pdv) {
		setPDV(_PDV + pdv, false);
	}
	
	public void actualizarPDV(float porcPDV) {
		int oldPDVMAX = _PDVMax;
		if (porcPDV < 1) {
			porcPDV = getPorcPDV();
		} else if (porcPDV > 100) {
			porcPDV = 100;
		}
		actualizarPDVMax();
		int PDV = Math.round(porcPDV * _PDVMax / 100);
		setPDV(PDV, _PDVMax != oldPDVMAX);
	}
	
	private void actualizarPDVMax() {
		if (_encarnacion != null && _encarnacion.getTotalStats() != null) {
			_PDVMax = _encarnacion.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_VITALIDAD);
		} else {
			_PDVMax = _clase.getPDV() + ((_nivel - 1) * 5);
			_PDVMax += getTotalStatsPelea().getTotalStatParaMostrar(Constantes.STAT_MAS_VITALIDAD);
		}
	}
	
	public void setPDV(int pdv, boolean cambioPDVMAX) {
		int oldPDV = _PDV;
		if (pdv > _PDVMax || MainServidor.PARAM_AUTO_RECUPERAR_TODA_VIDA) {
			_PDV = _PDVMax;
		} else if (pdv < 1) {
			_PDV = 1;
		} else {
			_PDV = pdv;
		}
		if (oldPDV == _PDV && !cambioPDVMAX) {
			return;
		}
		actualizarInfoGrupo();
		if (_pelea != null && _pelea.getFase() != Constantes.PELEA_FASE_COMBATE) {
			final Luchador luchador = _pelea.getLuchadorPorID(_id);
			if (luchador != null) {
				luchador.setPDVMAX(getPDVMax(), false);
				luchador.setPDV(getPDV());
				GestorSalida.ENVIAR_GTM_INFO_STATS_TODO_LUCHADORES_A_PERSO(this, _pelea);
			}
		}
	}
	
	public void fullPDV() {
		actualizarPDV(100);
	}
	
	public float getPorcPDV() {
		if (_PDVMax <= 0) {
			return 0;
		}
		float porc = _PDV * 100f / _PDVMax;
		porc = Math.max(0, porc);
		porc = Math.min(100, porc);
		return porc;
	}
	
	private void setDelayTimerRegenPDV(int tiempo) {
		if (_recuperarVida != null) {
			_recuperarVida.setDelay(tiempo);
		}
	}
	
	private void stopTimerRegenPDV() {
		if (_recuperarVida != null) {
			_recuperarVida.stop();
		}
	}
	
	private void startTimerRegenPDV() {
		if (_recuperarVida == null) {
			_recuperarVida = new Timer(1000, new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					if (_pelea != null || _PDV >= _PDVMax) {
						// return;
					} else {
						_PDV++;
					}
				}
			});
		}
		_recuperarVida.restart();
	}
	
	public void setSentado(final boolean sentado) {
		_sentado = sentado;
		if (_sentado) {
			_ultPDV = _PDV;
		}
		final int tiempo = _sentado ? 500 : 1000;
		setDelayTimerRegenPDV(tiempo);
		if (_enLinea) {
			if (!_sentado) {
				GestorSalida.ENVIAR_ILF_CANTIDAD_DE_VIDA(this, _PDV - _ultPDV);
				setPDV(_PDV, true);
			}
			GestorSalida.ENVIAR_ILS_TIEMPO_REGENERAR_VIDA(this, tiempo);
		}
		if (!sentado && (_emoteActivado == Constantes.EMOTE_SENTARSE || _emoteActivado == Constantes.EMOTE_ACOSTARSE)) {
			_emoteActivado = 0;// no hay emote
		}
	}
	
	public byte getAlineacion() {
		return _alineacion;
	}
	
	private void actualizarInfoGrupo() {
		if (_grupo != null) {
			GestorSalida.ENVIAR_PM_ACTUALIZAR_INFO_PJ_GRUPO(_grupo, stringInfoGrupo());
		}
	}
	
	public void mostrarEmoteIcon(final String str) {
		try {
			if (_pelea == null) {
				GestorSalida.ENVIAR_cS_EMOTICON_MAPA(_mapa, _id, Integer.parseInt(str));
			} else {
				GestorSalida.ENVIAR_cS_EMOTE_EN_PELEA(_pelea, 7, _id, Integer.parseInt(str));
			}
		} catch (final Exception e) {}
	}
	
	public void salirPelea(boolean ptoSalvada, boolean borrarMapa) {
		if (esMultiman()) {
			return;
		}
		if (_pelea == null) {
			return;
		}
		if (_pelea.getTipoPelea() != Constantes.PELEA_TIPO_DESAFIO && !_pelea.esEspectador(_id)) {
			disminuirTurnos();
		}
		setPelea(null);
		_ocupado = false;
		if (_energia < 1) {
			return;
		}
		if (ptoSalvada) {
			try {
				if (borrarMapa) {
					GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(_mapa, _id);
				}
				_mapa = Mundo.getMapa(_mapaSalvada);
				if (_nivel > 15 && _mapa.getSubArea().getArea().getSuperArea().getID() == 3) {
					_mapa = Mundo.getMapa((short) 7411);
					setCelda(_mapa.getCelda((short) 340));
				} else {
					setCelda(_mapa.getCelda(_celdaSalvada));
				}
			} catch (final Exception e) {}
		}
		// GestorSalida.ENVIAR_GV_RESETEAR_PANTALLA_JUEGO(this);
	}
	
	public void teleportPtoSalvada() {
		if (esMultiman()) {
			return;
		}
		short mapa = _mapaSalvada;
		short celda = _celdaSalvada;
		if (_nivel > 15 && _mapa.getSubArea().getArea().getSuperArea().getID() == 3) {
			mapa = (short) 7411;
			celda = (short) 340;
		}
		teleport(mapa, celda);
	}
	
	public void addScrollStat(int statID, int cantidad) {
		int anterior = _subStatsScroll.get(statID);
		int valor = anterior + cantidad;
		if (valor > MainServidor.LIMITE_SCROLL) {
			valor = MainServidor.LIMITE_SCROLL;
			cantidad = MainServidor.LIMITE_SCROLL - anterior;
		}
		_subStatsScroll.put(statID, valor);
		_totalStats.getStatsBase().addStatID(statID, cantidad);
	}
	
	public int getStatScroll(int statID) {
		if (_subStatsScroll.get(statID) == null) {
			return 0;
		}
		return _subStatsScroll.get(statID);
	}
	
	private void addStatBase(int statID, int cantidad) {
		int valor = 0;
		if (_subStatsBase.get(statID) != null) {
			valor = _subStatsBase.get(statID) + cantidad;
		}
		_subStatsBase.put(statID, valor);
		_totalStats.getStatsBase().addStatID(statID, cantidad);
	}
	
	public synchronized void boostStat2(final int tipo, int puntosUsar) {
		if (esMultiman()) {
			return;
		}
		if (_puntosStats <= 0) {
			return;
		}
		int statID = 0, usados = 0;
		switch (tipo) {
			case 10 :
				statID = (Constantes.STAT_MAS_FUERZA);
				break;
			case 11 :
				statID = (Constantes.STAT_MAS_VITALIDAD);
				break;
			case 12 :
				statID = (Constantes.STAT_MAS_SABIDURIA);
				break;
			case 13 :
				statID = (Constantes.STAT_MAS_SUERTE);
				break;
			case 14 :
				statID = (Constantes.STAT_MAS_AGILIDAD);
				break;
			case 15 :
				statID = (Constantes.STAT_MAS_INTELIGENCIA);
				break;
		}
		if (puntosUsar > _puntosStats) {
			puntosUsar = _puntosStats;
		}
		int valorStat = 0;
		BoostStat boost;
		boolean mod = false;
		while (true) {
			valorStat = _totalStats.getStatsBase().getStatParaMostrar(statID);
			boost = _clase.getBoostStat(statID, valorStat);
			usados += boost.getCoste();
			if (usados <= puntosUsar) {
				_puntosStats -= boost.getCoste();
				mod = true;
				addStatBase(statID, boost.getPuntos());
			} else {
				break;
			}
		}
		if (statID == Constantes.STAT_MAS_VITALIDAD) {// vitalidad
			actualizarPDV(0);
		}
		if (mod) {
			refrescarStuff(false, true, false);
		}
	}
	
	public void setMapa(final Mapa mapa) {
		_mapa = mapa;
	}
	
	public String stringObjetosABD() {
		final StringBuilder str = new StringBuilder();
		for (final Objeto obj : _objetos.values()) {
			if (str.length() > 0) {
				str.append("|");
			}
			str.append(obj.getID());
		}
		return str.toString();
	}
	
	public boolean estaMuteado() {
		return _cuenta.estaMuteado();
	}
	
	public Objeto getObjIdentInventario(final Objeto objeto, Objeto prohibido) {
		if (objeto.puedeTenerStatsIguales()) {
			for (final Objeto obj : _objetos.values()) {
				if (obj.getPosicion() != Constantes.OBJETO_POS_NO_EQUIPADO) {
					continue;
				}
				if (objeto.getID() == obj.getID()) {
					continue;
				}
				// boolean prohibido = false;
				// for (Objeto o : prohibidos) {
				// if (o.getID() == obj.getID()) {
				// prohibido = true;
				// break;
				// }
				// }
				// if (prohibido) {
				// continue;
				// }
				if (prohibido != null && prohibido.getID() == obj.getID()) {
					continue;
				}
				if (obj.getObjModeloID() == objeto.getObjModeloID() && obj.sonStatsIguales(objeto)) {
					return obj;
				}
			}
		}
		return null;
	}
	
	public boolean addObjIdentAInventario(final Objeto objeto, final boolean eliminar) {
		if (_objetos.containsKey(objeto.getID())) {
			return false;
		}
		// tipo piedra de alma y mascota
		Objeto igual = getObjIdentInventario(objeto, null);
		if (igual != null) {
			igual.setCantidad(igual.getCantidad() + objeto.getCantidad());
			GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(this, igual);
			if (eliminar && objeto.getID() > 0) {
				Mundo.eliminarObjeto(objeto.getID());
			}
			return true;
		}
		addObjetoConOAKO(objeto, true);
		return false;
	}
	
	public void addObjDropPelea(final Objeto objeto, final boolean eliminar) {
		if (_objetos.containsKey(objeto.getID()))
			return;
		// tipo piedra de alma y mascota
		Objeto igual = getObjIdentInventario(objeto, null);
		if (igual != null) {
			igual.setCantidad(igual.getCantidad() + objeto.getCantidad());
			_dropPelea.put(igual, false);
			if (eliminar && objeto.getID() > 0) {
				Mundo.eliminarObjeto(objeto.getID());
			}
			return;
		}
		addObjetoConOAKO(objeto, false);
		_dropPelea.put(objeto, true);
	}
	
	public void addObjetoConOAKO(final Objeto objeto, final boolean enviarOAKO) {
		if (objeto.getID() == 0) {
			Mundo.addObjeto(objeto, false);
		}
		_objetos.put(objeto.getID(), objeto);
		byte pos = objeto.getPosicion();
		if (Constantes.esPosicionObjeto(pos)) {
			if (Constantes.esPosicionEquipamiento(pos)) {
				boolean desequipar = false;
				if (objeto.getObjModelo().getNivel() > _nivel) {
					desequipar = true;
				} else if (!puedeEquiparRepetido(objeto.getObjModelo(), 1)) {
					desequipar = true;
				}
				if (desequipar) {
					pos = Constantes.OBJETO_POS_NO_EQUIPADO;
					objeto.setPosicion(pos);
				}
			}
			cambiarPosObjeto(objeto, Constantes.OBJETO_POS_NO_EQUIPADO, pos, true);
		}
		if (_enLinea && enviarOAKO) {
			GestorSalida.ENVIAR_OAKO_APARECER_OBJETO(this, objeto);
		}
	}
	
	public void borrarOEliminarConOR(final int id, final boolean eliminar) {
		if (borrarObjeto(id) && _enLinea) {
			GestorSalida.ENVIAR_OR_ELIMINAR_OBJETO(this, id);
		}
		if (eliminar) {
			Mundo.eliminarObjeto(id);
		}
	}
	
	private boolean borrarObjeto(final int id) {
		if (_objetos.get(id) != null) {
			byte pos = _objetos.get(id).getPosicion();
			if (pos == Constantes.OBJETO_POS_NO_EQUIPADO || pos >= _objPos49.length) {
				return _objetos.remove(id) != null;
			}
			if (_objPos49[pos].getID() == id) {
				cambiarPosObjeto(null, pos, Constantes.OBJETO_POS_NO_EQUIPADO, true);
			}
		}
		return _objetos.remove(id) != null;
	}
	
	public void cambiarPosObjeto(final Objeto obj, final byte oldPos, final byte newPos, boolean refrescarStuff) {
		if (oldPos == newPos) {
			return;
		}
		if (oldPos != Constantes.OBJETO_POS_NO_EQUIPADO && oldPos < _objPos49.length) {
			if (_objPos49[oldPos] != null) {
				if (obj == null || obj.getID() == _objPos49[oldPos].getID()) {
					_objPos49[oldPos].setPosicion(Constantes.OBJETO_POS_NO_EQUIPADO);
					_objPos49[oldPos] = null;
				}
			}
		}
		if (newPos != Constantes.OBJETO_POS_NO_EQUIPADO && newPos < _objPos49.length) {
			_objPos49[newPos] = obj;
		}
		if (obj != null) {
			obj.setPosicion(newPos);
		}
		if (_enLinea) {
			boolean visual = false;
			if (Constantes.esPosicionVisual(newPos) || Constantes.esPosicionVisual(oldPos)) {
				visual = true;
			}
			if (Constantes.esPosicionEquipamiento(newPos) || Constantes.esPosicionEquipamiento(oldPos)) {
				actualizarObjEquipStats();
				if (refrescarStuff) {
					refrescarStuff(false, true, visual);
				}
			}
		}
	}
	
	public boolean restarCantObjOEliminar(final int idObjeto, int cantidad, final boolean eliminar) {
		try {
			final Objeto obj = _objetos.get(idObjeto);
			if (obj == null) {
				return false;
			}
			if (cantidad > obj.getCantidad()) {
				cantidad = obj.getCantidad();
			}
			if (obj.getCantidad() - cantidad > 0) {
				obj.setCantidad(obj.getCantidad() - cantidad);
				if (_enLinea) {
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(this, obj);
				}
			} else {
				borrarOEliminarConOR(obj.getID(), eliminar);
			}
			return true;
		} catch (final Exception e) {}
		return false;
	}
	
	public boolean tenerYEliminarObjPorModYCant(final int idModelo, int cantidad) {
		final ArrayList<Objeto> listaObjBorrar = new ArrayList<Objeto>();
		for (final Objeto obj : _objetos.values()) {
			if (obj.getObjModeloID() != idModelo) {
				continue;
			}
			if (obj.getCantidad() >= cantidad) {
				final int nuevaCant = obj.getCantidad() - cantidad;
				if (nuevaCant > 0) {
					obj.setCantidad(nuevaCant);
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(this, obj);
				} else {
					listaObjBorrar.add(obj);
				}
				for (final Objeto objBorrar : listaObjBorrar) {
					borrarOEliminarConOR(objBorrar.getID(), true);
				}
				return true;
			} else {
				cantidad -= obj.getCantidad();
				listaObjBorrar.add(obj);
			}
		}
		return false;
	}
	
	public int restarObjPorModYCant(final int idModelo, int cantidad) {
		final ArrayList<Objeto> listaObjBorrar = new ArrayList<Objeto>();
		int eliminados = 0;
		for (final Objeto obj : _objetos.values()) {
			if (obj.getObjModeloID() != idModelo) {
				continue;
			}
			if (cantidad <= 0) {
				break;
			}
			if (obj.getCantidad() - cantidad > 0) {
				eliminados += cantidad;
				obj.setCantidad(obj.getCantidad() - cantidad);
				GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(this, obj);
				break;
			} else {
				cantidad -= obj.getCantidad();
				eliminados += obj.getCantidad();
				listaObjBorrar.add(obj);
			}
		}
		for (final Objeto objBorrar : listaObjBorrar) {
			borrarOEliminarConOR(objBorrar.getID(), true);
		}
		return eliminados;
	}
	
	public int eliminarPorObjModeloRecibidoDesdeMinutos(final int objModeloID, int recibidoMinutos) {
		final ArrayList<Objeto> lista = new ArrayList<Objeto>();
		lista.addAll(_objetos.values());
		int eliminados = 0;
		for (final Objeto obj : lista) {
			if (obj.getObjModeloID() != objModeloID) {
				continue;
			}
			if (recibidoMinutos > 0) {
				if (obj.tieneStatTexto(Constantes.STAT_RECIBIDO_EL)) {
					if (obj.getDiferenciaTiempo(Constantes.STAT_RECIBIDO_EL, 60 * 1000) < recibidoMinutos) {
						continue;
					}
				}
			}
			eliminados += obj.getCantidad();
			borrarOEliminarConOR(obj.getID(), true);
		}
		return eliminados;
	}
	
	public Objeto getObjPosicion(final byte pos) {
		try {
			if (pos > Constantes.OBJETO_POS_NO_EQUIPADO) {
				return _objPos49[pos];
			}
		} catch (final Exception e) {}
		return null;
	}
	
	public Objeto getObjeto(final int id) {
		return _objetos.get(id);
	}
	
	public boolean tieneObjetoID(final int id) {
		return _objetos.containsKey(id);
	}
	
	public boolean tieneObjPorModYCant(final int idModelo, int cantidad) {
		for (final Objeto obj : _objetos.values()) {
			if (obj.getObjModeloID() != idModelo) {
				continue;
			}
			if (obj.getCantidad() >= cantidad) {
				return true;
			} else {
				cantidad -= obj.getCantidad();
			}
		}
		return false;
	}
	
	public String strListaObjetos() {
		final StringBuilder str = new StringBuilder();
		TreeMap<Integer, Objeto> objetos = new TreeMap<>();
		objetos.putAll(_objetos);
		for (final Objeto obj : objetos.values()) {
			if (obj == null) {
				continue;
			}
			str.append(obj.stringObjetoConGuiño());
		}
		return str.toString();
	}
	
	public String getObjetosPersonajePorID(final String separador) {
		final StringBuilder str = new StringBuilder();
		for (final int id : _objetos.keySet()) {
			if (str.length() != 0) {
				str.append(separador);
			}
			str.append(id);
		}
		return str.toString();
	}
	
	public void venderObjetos(String packet) {
		for (String str : packet.split(";")) {
			try {
				final String[] infos = str.split(Pattern.quote("|"));
				int id = Integer.parseInt(infos[0]);
				int cant = Integer.parseInt(infos[1]);
				final Objeto objeto = _objetos.get(id);
				if (objeto.getPosicion() != Constantes.OBJETO_POS_NO_EQUIPADO || objeto.getObjModelo()
				.getTipo() == Constantes.OBJETO_TIPO_OBJETO_DE_BUSQUEDA) {
					GestorSalida.ENVIAR_ESE_ERROR_VENTA(this);
					continue;
				}
				if (objeto.tieneStatTexto(Constantes.STAT_PERTENECE_Y_NO_VENDER)) {
					if (!objeto.getParamStatTexto(Constantes.STAT_PERTENECE_Y_NO_VENDER, 4).equalsIgnoreCase(_nombre)) {
						return;
					}
				}
				if (objeto.getCantidad() < cant) {
					cant = objeto.getCantidad();
				}
				final long kamas = objeto.getObjModelo().getKamas() * cant;
				int ogrinas = 0;
				try {
					ogrinas = objeto.getObjModelo().getOgrinas() * cant;
				} catch (final Exception e) {
					continue;
				}
				if (ogrinas < 0 || kamas < 0) {
					GestorSalida.ENVIAR_ESE_ERROR_VENTA(this);
					continue;
				}
				if (kamas == 0 && ogrinas > 0) {
					if (!MainServidor.PARAM_DEVOLVER_OGRINAS) {
						continue;
					}
					GestorSQL.SET_OGRINAS_CUENTA((int) ((ogrinas * MainServidor.FACTOR_DEVOLVER_OGRINAS) + GestorSQL
					.GET_OGRINAS_CUENTA(_cuenta.getID())), _cuenta.getID());
				} else {
					addKamas(kamas / 10, false, false);
				}
				if (objeto.getCantidad() - cant < 1) {
					borrarOEliminarConOR(id, true);
				} else {
					objeto.setCantidad(objeto.getCantidad() - cant);
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(this, objeto);
				}
				Thread.sleep(150);
			} catch (final Exception e) {
				GestorSalida.ENVIAR_ESE_ERROR_VENTA(this);
			}
		}
		GestorSalida.ENVIAR_ESK_VENDIDO(this);
		GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
		GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(this);
	}
	
	public void addExperiencia(final long experiencia, boolean mensaje) {
		if (esMultiman()) {
			return;
		}
		int exNivel = _nivel;
		int nuevoNivel = _nivel;
		if (_encarnacion != null) {
			exNivel = _encarnacion.getNivel();
			_encarnacion.addExperiencia(experiencia, this);
			nuevoNivel = _encarnacion.getNivel();
		} else {
			_experienciaDia += experiencia;
			_experiencia += experiencia;
			while (_experiencia >= Mundo.getExpPersonaje(_nivel + 1) && _nivel < MainServidor.NIVEL_MAX_PERSONAJE) {
				subirNivel(false);
			}
			nuevoNivel = _nivel;
		}
		if (exNivel < nuevoNivel) {
			fullPDV();
		}
		if (_enLinea) {
			if (mensaje) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "08;" + experiencia);
			}
			if (exNivel < nuevoNivel) {
				GestorSalida.ENVIAR_AN_MENSAJE_NUEVO_NIVEL(this, nuevoNivel);
				GestorSalida.ENVIAR_SL_LISTA_HECHIZOS(this);
			}
			GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
		}
	}
	
	public void subirHastaNivel(int nivel) {
		if (nivel > _nivel) {
			while (_nivel < nivel) {
				subirNivel(true);
			}
			fullPDV();
			if (_enLinea) {
				GestorSalida.ENVIAR_SL_LISTA_HECHIZOS(this);
				GestorSalida.ENVIAR_AN_MENSAJE_NUEVO_NIVEL(this, _nivel);
				GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
			}
		}
	}
	
	private void subirNivel(final boolean expDeNivel) {
		if (esMultiman()) {
			return;
		}
		if (_nivel == MainServidor.NIVEL_MAX_PERSONAJE || _encarnacion != null) {
			return;
		}
		_nivel += 1;
		if (!MainServidor.MODO_PVP && _nivel == MainServidor.NIVEL_MAX_PERSONAJE) {
			GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1CONGRATULATIONS_LVL_MAX;" + _nombre);
		}
		_puntosStats += MainServidor.PUNTOS_STATS_POR_NIVEL;
		_puntosHechizos += MainServidor.PUNTOS_HECHIZO_POR_NIVEL;
		if (_nivel == 100) {
			_totalStats.getStatsBase().addStatID(Constantes.STAT_MAS_PA, 1);
		}
		_clase.aprenderHechizo(this, _nivel);
		if (expDeNivel) {
			_experiencia = Mundo.getExpPersonaje(_nivel);
		}
	}
	
	public void cambiarNivelYAlineacion(int nivel, byte alineacion) {
		if (MainServidor.NIVEL_MAX_ESCOGER_NIVEL <= 1) {
			return;
		}
		if (nivel > MainServidor.NIVEL_MAX_ESCOGER_NIVEL) {
			nivel = MainServidor.NIVEL_MAX_ESCOGER_NIVEL;
		}
		subirHastaNivel(nivel);
		if (MainServidor.MODO_PVP) {
			if ((alineacion == Constantes.ALINEACION_BONTARIANO || alineacion == Constantes.ALINEACION_BRAKMARIANO)
			&& alineacion != _alineacion) {
				cambiarAlineacion(alineacion, false);
			}
			if (_alineacion == Constantes.ALINEACION_NEUTRAL) {
				GestorSalida.ENVIAR_bA_ESCOGER_NIVEL(this);
			}
		}
		_recienCreado = false;
	}
	
	public Map<Byte, StatOficio> getStatsOficios() {
		return _statsOficios;
	}
	
	public StatOficio getStatOficioPorID(final int oficioID) {
		for (final StatOficio SO : _statsOficios.values()) {
			if (SO.getOficio().getID() == oficioID) {
				return SO;
			}
		}
		return null;
	}
	
	public StatOficio getStatOficioPorTrabajo(final int skillID) {
		for (final StatOficio SO : _statsOficios.values()) {
			if (SO.esValidoTrabajo(skillID)) {
				return SO;
			}
		}
		return null;
	}
	
	public int getNivelStatOficio(int oficioID) {
		try {
			StatOficio so = getStatOficioPorID(oficioID);
			if (so != null)
				return so.getNivel();
		} catch (Exception e) {}
		return 0;
	}
	
	public String stringOficios() {
		final StringBuilder str = new StringBuilder();
		for (byte i = 0; i < 6; i++) {
			try {
				_statsOficios.get(i).getNivel();// es para activar el exception
				if (str.length() > 0) {
					str.append(";");
				}
				str.append(_statsOficios.get(i).getOficio().getID() + "," + _statsOficios.get(i).getExp());
			} catch (Exception e) {}
		}
		return str.toString();
	}
	
	public boolean olvidarOficio(final int oficio) {
		try {
			byte id = -1;
			for (Entry<Byte, StatOficio> s : _statsOficios.entrySet()) {
				if (s.getValue().getOficio().getID() == oficio) {
					id = s.getKey();
					break;
				}
			}
			if (id != -1) {
				_statsOficios.remove(id);
				GestorSalida.ENVIAR_JR_OLVIDAR_OFICIO(this, oficio);
			}
			return true;
		} catch (Exception e) {}
		return false;
	}
	
	public boolean puedeAprenderOficio(int oficioID) {
		if (!MainServidor.PARAM_PERMITIR_OFICIOS) {
			return false;
		}
		boolean esMago = Constantes.esOficioMago(oficioID);
		if (esMago) {
			int oficioPrimario = Constantes.getOficioPrimarioDeMago(oficioID);
			if (getNivelStatOficio(oficioPrimario) < 65) {
				if (_enLinea)
					GestorSalida.ENVIAR_Im_INFORMACION(this, "16|" + oficioPrimario);
				return false;
			}
		}
		byte cant = 0, nvl30 = 0;
		for (final StatOficio SO : _statsOficios.values()) {
			if (SO.getPosicion() == 7) {
				continue;
			}
			if (SO.getOficio().getID() == oficioID) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "111");
				return false;
			}
			if (Constantes.esOficioMago(SO.getOficio().getID())) {
				if (esMago) {
					cant++;
					if (SO.getNivel() >= 30)
						nvl30++;
				}
			} else if (!esMago) {
				cant++;
				if (SO.getNivel() >= 30)
					nvl30++;
			}
		}
		if (_statsOficios.size() >= 7 || cant >= 3) {
			if (_enLinea) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "19");
			}
			return false;
		}
		if (nvl30 < cant) {
			if (_enLinea)
				GestorSalida.ENVIAR_Im_INFORMACION(this, "18;30");
			return false;
		}
		return true;
	}
	
	public int aprenderOficio(final Oficio oficio, int exp) {
		if (!MainServidor.PARAM_PERMITIR_OFICIOS) {
			return -1;
		}
		boolean esMago = Constantes.esOficioMago(oficio.getID());
		byte pos = -1, cant = 0;
		for (final StatOficio SO : _statsOficios.values()) {
			if (SO.getPosicion() == 7) {
				continue;
			}
		}
		for (final StatOficio SO : _statsOficios.values()) {
			if (SO.getPosicion() == 7) {
				continue;
			}
			if (SO.getOficio().getID() == oficio.getID()) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "111");
				return -1;
			}
			if (Constantes.esOficioMago(SO.getOficio().getID())) {
				if (esMago) {
					cant++;
				}
			} else if (!esMago) {
				cant++;
			}
		}
		if (cant >= 3) {
			if (_enLinea)
				GestorSalida.ENVIAR_Im_INFORMACION(this, "19");
			return -1;
		}
		for (byte p = 0; p < 6; p++) {
			if (_statsOficios.get(p) == null) {
				pos = p;
				break;
			}
		}
		if (pos == -1) {
			if (_enLinea) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "19");
			}
			return -1;
		}
		final StatOficio statOficio = new StatOficio(pos, oficio, exp);
		_statsOficios.put(pos, statOficio);
		if (_enLinea) {
			GestorSalida.ENVIAR_Im_INFORMACION(this, "02;" + oficio.getID());
			GestorSalida.ENVIAR_JS_SKILL_DE_OFICIO(this, statOficio);
			GestorSalida.ENVIAR_JX_EXPERINENCIA_OFICIO(this, statOficio);
			GestorSalida.ENVIAR_JO_OFICIO_OPCIONES(this, statOficio);
			verificarHerramientOficio();
		}
		return pos;
	}
	
	public boolean tieneDon(int id, int nivel) {
		Especialidad esp = Mundo.getEspecialidad(_orden, _ordenNivel);
		if (esp == null) {
			return false;
		}
		for (Don don : esp.getDones()) {
			if (don.getID() == id && don.getNivel() >= nivel) {
				return true;
			}
		}
		return false;
	}
	
	public boolean tieneObjModeloEquipado(final int id) {
		for (byte pos : Constantes.POSICIONES_EQUIPAMIENTO) {
			final Objeto obj = getObjPosicion(pos);
			if (obj == null) {
				continue;
			}
			if (obj.getObjModeloID() == id) {
				return true;
			}
		}
		return false;
	}
	
	private int cantEquipadoModelo(final int id) {
		int i = 0;
		for (byte pos : Constantes.POSICIONES_EQUIPAMIENTO) {
			final Objeto obj = getObjPosicion(pos);
			if (obj == null) {
				continue;
			}
			if (obj.getObjModeloID() == id) {
				i++;
			}
		}
		return i;
	}
	
	public void setInvitandoA(final Personaje invitando, String tipo) {
		_invitandoA = invitando;
		_tipoInvitacion = tipo;
	}
	
	public Personaje getInvitandoA() {
		return _invitandoA;
	}
	
	public void setInvitador(final Personaje invitando, String tipo) {
		_invitador = invitando;
		_tipoInvitacion = tipo;
	}
	
	public Personaje getInvitador() {
		return _invitador;
	}
	
	public String getTipoInvitacion() {
		return _tipoInvitacion;
	}
	
	public boolean esMaestro() {
		return _grupo != null && _grupo.esLiderGrupo(this) && _grupo.tieneAlumnos();
	}
	
	public String stringInfoGrupo() {
		final StringBuilder str = new StringBuilder();
		str.append(_id + ";");
		str.append(_nombre + ";");
		str.append(getGfxID(false) + ";");
		str.append(_color1 + ";");
		str.append(_color2 + ";");
		str.append(_color3 + ";");
		str.append(getStringAccesorios() + ";");
		str.append(_PDV + "," + _PDVMax + ";");
		str.append(_nivel + ";");
		str.append(getIniciativa() + ";");
		str.append(_totalStats.getTotalStatConComplemento(Constantes.STAT_MAS_PROSPECCION) + ";");
		str.append("1");
		return str.toString();
	}
	
	public int getNroObjEquipadosDeSet(final int setID) {
		int nro = 0;
		for (byte pos : Constantes.POSICIONES_EQUIPAMIENTO) {
			final Objeto obj = getObjPosicion(pos);
			if (obj == null) {
				continue;
			}
			if (obj.getObjModelo().getSetID() == setID) {
				nro++;
			}
		}
		return nro;
	}
	
	public boolean puedeIniciarTrabajo(final int skillID, final ObjetoInteractivo objInterac, final int unicaID,
	final Celda celda) {
		try {
			StatOficio statOficio = getStatOficioPorTrabajo(skillID);
			if (statOficio == null) {
				return false;
			}
			Objeto arma = getObjPosicion(Constantes.OBJETO_POS_ARMA);
			int idObjModelo = arma != null ? arma.getObjModeloID() : -1;
			if (!statOficio.getOficio().esHerramientaValida(idObjModelo)) {
				return false;
			}
			if (idObjModelo != -1) {
				int distHerramienta = 2;
				if (arma.getObjModelo().getStatHechizo() != null) {
					distHerramienta = Math.max(2, arma.getObjModelo().getStatHechizo().getMaxAlc());
				}
				int dist = Camino.distanciaDosCeldas(_mapa, _celda.getID(), celda.getID());
				if (dist == 0 || dist > distHerramienta) {
					return false;
				}
			}
			return statOficio.iniciarTrabajo(skillID, this, objInterac, unicaID, celda);
		} catch (Exception e) {}
		// esta lejos para realizar el trabajo
		return false;
	}
	
	public boolean finalizarTrabajo(final int skillID) {
		StatOficio skill = getStatOficioPorTrabajo(skillID);
		if (skill == null) {
			return false;
		}
		return skill.finalizarTrabajo(this);
	}
	
	public boolean puedeIniciarAccionEnCelda(final AccionDeJuego AJ) {
		try {
			if (AJ == null) {
				return false;
			}
			short celdaID = Short.parseShort(AJ.getPathPacket().split(";")[0]);
			int skillID = Integer.parseInt(AJ.getPathPacket().split(";")[1]);
			Celda celda = _mapa.getCelda(celdaID);
			switch (_orientacion) {
				case 0 :
				case 2 :
				case 4 :
				case 6 :
				case 8 :
					cambiarOrientacionADiagonal((byte) 7);
					break;
			}
			boolean puede = false;
			if (celda.puedeHacerAccion(skillID, _pescarKuakua)) {
				puede = celda.puedeIniciarAccion(this, AJ);
			}
			if (esMaestro()) {
				_grupo.packetSeguirLider(AJ.getPacket());
			}
			return puede;
		} catch (final Exception e) {
			String error = "EXCEPTION iniciarAccionEnCelda AJ.getPacket(): " + AJ.getPathPacket() + " e: " + e.toString();
			GestorSalida.ENVIAR_BN_NADA(this, error);
			MainServidor.redactarLogServidorln(error);
		}
		// si no puede realizar la accion porque no esta cerca al IO
		return false;
	}
	
	public boolean finalizarAccionEnCelda(final AccionDeJuego AJ) {
		try {
			if (AJ != null) {
				short celdaID = Short.parseShort(AJ.getPathPacket().split(";")[0]);
				return _mapa.getCelda(celdaID).finalizarAccion(this, AJ);
			}
		} catch (final Exception e) {
			String error = "EXCEPTION finalizarAccionEnCelda e:" + e.toString();
			GestorSalida.ENVIAR_BN_NADA(this, error);
			MainServidor.redactarLogServidorln(error);
		}
		return false;
	}
	
	public boolean inicioAccionMoverse(final AccionDeJuego AJ) {
		try {
			if (AJ == null) {
				GestorSalida.ENVIAR_BN_NADA(this, "inicioAccionMoverse AJ null");
				return false;
			}
			if (_pelea == null) {
				// no hay pelea
				return inicioMovimiento(AJ);
			} else { // pelea
				Luchador luch = _pelea.getLuchadorPorID(getID());
				if (!luch.puedeJugar()) {
					if (getCompañero() != null) {
						luch = _pelea.getLuchadorPorID(getCompañero().getID());
						if (!luch.puedeJugar()) {
							luch = null;
							for(Personaje perso: invocacionControlable){
								Luchador luchTemp = _pelea.getLuchadorPorID(perso.getID());
								if(luchTemp.puedeJugar()){
									luch = luchTemp;
								}
							}
						}
					}else {
						luch = null;
						for(Personaje perso: invocacionControlable){
							Luchador luchTemp = _pelea.getLuchadorPorID(perso.getID());
							if(luchTemp.puedeJugar()){
								luch = luchTemp;
							}
						}
						
					}		
				}
				if(luch==null){
					return false;
				}
				String moverse = _pelea.intentarMoverse(luch, AJ.getPathPacket(), AJ.getIDUnica(), AJ);
				return (moverse.equals("ok") || moverse.equals("stop"));
			}
		} catch (Exception e) {
			String error = "EXCEPTION inicioAccionMoverse AJ.getPacket(): " + AJ.getPathPacket() + " e: " + e.toString();
			GestorSalida.ENVIAR_BN_NADA(this, error);
			MainServidor.redactarLogServidorln(error);
		}
		return false;
	}
	
	private boolean inicioMovimiento(final AccionDeJuego AJ) {
		int linea = 0;
		try {
			if (esFantasma()) {
				linea = 1;
				// puede moverse normal
			} else if (esTumba() || _inmovil) {
				linea = 23;
				if (esTumba()) {
					linea = 2;
				} else if (_inmovil) {
					linea = 3;
					GestorSalida.ENVIAR_Im_INFORMACION(this, "1ESTAS_INMOVIL");
				}
				linea = 5;
				// setOcupado(false);
				GestorSalida.ENVIAR_GA_DEBUG_ACCIONES(this);
				// borrarAccionJuego(AJ.getIDUnica());
				return false;
			}
			linea = 24;
			if (getPodsUsados() > getPodsMaximos()) {
				linea = 4;
				GestorSalida.ENVIAR_Im_INFORMACION(this, "112");
				GestorSalida.ENVIAR_GA_DEBUG_ACCIONES(this);
				// borrarAccionJuego(AJ.getIDUnica());
				return false;
			}
			linea = 6;
			if (_totalStats.getStatsObjetos().tieneStatID(Constantes.STAT_MOVER_DESAPARECE_BUFF)) {
				linea = 7;
				for (byte pos : Constantes.POSICIONES_EQUIPAMIENTO) {
					try {
						final Objeto obj = getObjPosicion(pos);
						if (obj == null) {
							continue;
						}
						if (obj.getStats().tieneStatID(Constantes.STAT_MOVER_DESAPARECE_BUFF)) {
							borrarOEliminarConOR(obj.getID(), true);
							break;
						}
					} catch (final Exception e) {}
				}
			}
			linea = 8;
			final short celdaIDPersonaje = getCelda().getID();
			linea = 9;
			String path = AJ.getPathPacket();
			linea = 10;
			final AtomicReference<String> pathRef = new AtomicReference<String>(path);
			linea = 11;
			final short ultCelda = Encriptador.hashACeldaID(path.substring(path.length() - 2));
			linea = 12;
			int nroCeldasMov = Camino.nroCeldasAMover(_mapa, null, pathRef, celdaIDPersonaje, ultCelda, this);
			linea = 13;
			if (nroCeldasMov == 0) {
				GestorSalida.ENVIAR_GA_DEBUG_ACCIONES(this);
				// borrarAccionJuego(AJ.getIDUnica());
				return false;
			}
			linea = 14;
			if (nroCeldasMov == -1000) {
				linea = 15;
				path = Encriptador.getValorHashPorNumero(getOrientacion()) + Encriptador.celdaIDAHash(celdaIDPersonaje);
			} else {
				linea = 16;
				if (nroCeldasMov >= 10000) {
					if (nroCeldasMov >= 20000) {
						nroCeldasMov -= 10000;
					}
					nroCeldasMov -= 10000;
				}
				AJ.setCeldas(nroCeldasMov);
				path = pathRef.get();
			}
			linea = 17;
			AJ.setPathReal(path);
			linea = 18;
			if (esMaestro()) {
				linea = 19;
				_grupo.packetSeguirLider(AJ.getPacket());
			}
			linea = 20;
			GestorSalida.ENVIAR_GA_ACCION_JUEGO_AL_MAPA(_mapa, AJ.getIDUnica(), 1, getID() + "", Encriptador
			.getValorHashPorNumero(getOrientacion()) + Encriptador.celdaIDAHash(celdaIDPersonaje) + path);
			linea = 21;
			if (estaSentado()) {
				setSentado(false);
			}
			linea = 22;
			// setOcupado(true);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			String error = "EXCEPTION inicioMovimiento LINEA " + linea + " AJ.getPacket(): " + AJ.getPathPacket() + " e: " + e
			.toString();
			GestorSalida.ENVIAR_BN_NADA(this, error);
			MainServidor.redactarLogServidorln(error);
		}
		return false;
	}
	
	public boolean finAccionMoverse(final AccionDeJuego AJ, String packet) {
		byte bug = 0;
		try {
			final boolean ok = packet.charAt(2) == 'K';
			bug = 1;
			if (MainServidor.PARAM_ANTI_SPEEDHACK && ok) {
				boolean correr = AJ.getCeldas() > 5;
				long debeSer = 0;
				if (_montando) {
					if (correr) {
						debeSer = 130;
					} else {
						debeSer = 280;
					}
				} else {
					if (correr) {
						debeSer = 170;
					} else {
						debeSer = 390;
					}
				}
				long ping = 0;
				try {
					ping = _cuenta.getSocket().getPing();
				} catch (Exception e) {}
				float fVelocidad = (Math.max(0, _totalStats.getTotalStatParaMostrar(Constantes.STAT_MAS_VELOCIDAD) / 1000f));
				debeSer = (long) ((debeSer * AJ.getCeldas()) / (1 + fVelocidad));
				long fue = (System.currentTimeMillis() - AJ.getTiempoInicio()) + ping;
				if (debeSer > fue) {
					MainServidor.redactarLogServidorln("PLAYER " + _nombre + "(" + _id + ") USE SPEEDHACK => MAYBE:" + debeSer
					+ " - WAS:" + fue + " - PING:" + ping + " (" + AJ.getCeldas() + ")");
					// GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(this, "1DONT_USE_SPEEDHACK");
					// GestorSalida.ENVIAR_M0_MENSAJE_BASICOS_SVR(this, "45", "DISCONNECT FOR USE SPEED HACK",
					// "");
					// try {
					// Thread.sleep(3000);
					// _cuenta.getEntradaPersonaje().cerrarSocket(true, "finAccionMoverse()");
					// } catch (Exception e) {}
					// return false;
					try {
						Thread.sleep(debeSer - fue + 1);
					} catch (Exception e) {}
				}
			}
			bug = 2;
			if (_pelea == null) {
				return finMovimiento(AJ, packet);
			} else {
				return _pelea.finalizarMovimiento(this);
			}
		} catch (Exception e) {
			String error = "EXCEPTION finAccionMoverse AJ.getPacket(): " + AJ.getPacket() + " , bug: " + bug + " e:" + e
			.toString();
			GestorSalida.ENVIAR_BN_NADA(this, error);
			MainServidor.redactarLogServidorln(error);
		}
		return false;
	}
	
	private boolean finMovimiento(final AccionDeJuego AJ, String packet) {
		byte bug = 0;
		try {
			short celdaAMover = -1, celdaPacket = -1;
			final String pathReal = AJ.getPathReal();
			final String pathPacket = AJ.getPathPacket();
			_orientacion = (Encriptador.getNumeroPorValorHash(pathReal.charAt(pathReal.length() - 3)));
			bug = 1;
			final boolean ok = packet.charAt(2) == 'K';
			if (ok) {
				bug = 2;
				celdaAMover = Encriptador.hashACeldaID(pathReal.substring(pathReal.length() - 2));
				celdaPacket = Encriptador.hashACeldaID(pathPacket.substring(pathPacket.length() - 2));
			} else {
				bug = 3;
				String[] infos = packet.substring(3).split(Pattern.quote("|"));
				celdaPacket = celdaAMover = Short.parseShort(infos[1]);
				if (_grupo != null && _grupo.esLiderGrupo(this)) {
					_grupo.packetSeguirLider(packet);
				}
			}
			bug = 4;
			return _mapa.jugadorLLegaACelda(this, celdaAMover, celdaPacket, ok);
		} catch (Exception e) {
			String error = "EXCEPTION finMovimiento AJ.getPacket(): " + AJ.getPacket() + " , bug: " + bug + " e:" + e
			.toString();
			GestorSalida.ENVIAR_BN_NADA(this, error);
			MainServidor.redactarLogServidorln(error);
		}
		return false;
	}
	
	public boolean realizarOtroInteractivo(Celda celdaObjetivo, ObjetoInteractivo objInteractivo) {
		boolean b = false;
		if (objInteractivo == null || !objInteractivo.puedeIniciarRecolecta()) {
			return b;
		}
		for (final OtroInteractivo oi : Mundo.OTROS_INTERACTIVOS) {
			if (oi.getGfxID() <= -1 && oi.getMapaID() <= -1 && oi.getCeldaID() <= -1) {
				continue;
			}
			if (oi.getGfxID() > -1 && oi.getGfxID() != objInteractivo.getGfxID()) {
				continue;
			}
			if (oi.getMapaID() > -1 && oi.getMapaID() != _mapa.getID()) {
				continue;
			}
			if (oi.getCeldaID() > -1 && oi.getCeldaID() != celdaObjetivo.getID()) {
				continue;
			}
			if (!Condiciones.validaCondiciones(this, oi.getCondicion())) {
				continue;
			}
			// System.out.println("ENVIO LA ACCION 1");
			objInteractivo.forzarActivarRecarga(oi.getTiempoRecarga());
			oi.getAccion().realizarAccion(this, null, -1, (short) -1);
			return true;
		}
		return b;
	}
	
	public boolean puedeIrKoliseo() {
		if (_tutorial != null || _calabozo || _pelea != null || !_enLinea) {
			return false;
		}
		return true;
	}
	
	public int getPretendiente() {
		return _pretendiente;
	}
	
	public boolean puedeCasarse() {
		if (_mapa.getID() != 2019) {
			return false;
		}
		if (_celda.getID() != 297 && _celda.getID() != 282) {
			return false;
		}
		if (!MainServidor.PARAM_MATRIMONIO_GAY) {
			if (_celda.getID() == 282 && _sexo == Constantes.SEXO_FEMENINO) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "1102");
				return false;
			}
			if (_celda.getID() == 297 && _sexo == Constantes.SEXO_MASCULINO) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "1102");
				return false;
			}
		}
		if (_esposoID > 0) {
			return false;
		}
		return true;
	}
	
	public void preguntaCasarse() {
		if (_mapa.getID() != 2019) {
			return;
		}
		short celda = 0;
		byte sexo = 0;
		if (_celda.getID() == 282) {
			celda = 297;
			sexo = Constantes.SEXO_FEMENINO;
		} else if (_celda.getID() == 297) {
			celda = 282;
			sexo = Constantes.SEXO_MASCULINO;
		} else {
			return;
		}
		Personaje novio = _mapa.getCelda(celda).getPrimerPersonaje();
		if (novio == null || novio.getEsposoID() > 0) {
			return;
		}
		if (!MainServidor.PARAM_MATRIMONIO_GAY && novio.getSexo() != sexo) {
			return;
		}
		GestorSalida.ENVIAR_GA_ACCIONES_MATRIMONIO(_mapa, 617, _id, novio.getID(), -51);
	}
	
	public void confirmarMatrimonio(int proponeID, boolean acepto) {
		Personaje propone = Mundo.getPersonaje(proponeID);
		if (propone == null)
			return;
		if (!acepto) {
			_pretendiente = propone._pretendiente = 0;
			GestorSalida.ENVIAR_GA_ACCIONES_MATRIMONIO(_mapa, 619, _id, proponeID, -51);
		} else {
			if (propone.getPretendiente() == _id) {
				_pretendiente = propone._pretendiente = 0;
				_esposoID = propone.getID();
				propone._esposoID = _id;
				GestorSalida.ENVIAR_GA_ACCIONES_MATRIMONIO(_mapa, 618, _id, proponeID, -51);
			} else {
				_pretendiente = proponeID;
				GestorSalida.ENVIAR_GA_ACCIONES_MATRIMONIO(_mapa, 617, _id, proponeID, -51);
			}
		}
	}
	
	public void divorciar() {
		try {
			if (_enLinea) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "047;" + Mundo.getPersonaje(_esposoID).getNombre());
			}
		} catch (final Exception e) {}
		_esposoID = 0;
	}
	
	public int getEsposoID() {
		return _esposoID;
	}
	
	public void setEsposoID(final int id) {
		_esposoID = id;
	}
	
	public byte getTipoExchange() {
		return _tipoExchange;
	}
	
	public void setTipoExchange(final byte tipo) {
		_tipoExchange = tipo;
	}
	
	public boolean estaExchange() {
		return _tipoExchange != Constantes.INTERCAMBIO_TIPO_NULO;
	}
	
	public void cerrarVentanaExchange(String exito) {
		setExchanger(null);
		setTipoExchange(Constantes.INTERCAMBIO_TIPO_NULO);
		GestorSalida.ENVIAR_EV_CERRAR_VENTANAS(this, exito);
	}
	
	public synchronized void cerrarExchange(String exito) {
		switch (getTipoInvitacion()) {
			case "intercambio" :
				Personaje invitandoA, invitador;
				if (_invitador != null) {
					invitador = _invitador;
					invitandoA = this;
				} else if (_invitandoA != null) {
					invitador = this;
					invitandoA = _invitandoA;
				} else {
					GestorSalida.ENVIAR_BN_NADA(this);
					return;
				}
				invitador.setInvitandoA(null, "");
				invitandoA.setInvitador(null, "");
				invitador.cerrarVentanaExchange("");
				invitandoA.cerrarVentanaExchange("");
				break;
			default :
				if (!estaExchange()) {
					GestorSalida.ENVIAR_BN_NADA(this);
					return;
				}
				switch (_tipoExchange) {
					case Constantes.INTERCAMBIO_TIPO_LIBRO_ARTESANOS :// libro de artesanos
						cerrarVentanaExchange("");
						break;
					case Constantes.INTERCAMBIO_TIPO_TIENDA_NPC :// tienda npc
					case Constantes.INTERCAMBIO_TIPO_MERCANTE :// mercante
					case Constantes.INTERCAMBIO_TIPO_MI_TIENDA :// misma tienda
					case Constantes.INTERCAMBIO_TIPO_MONTURA :// dragopavo
					case Constantes.INTERCAMBIO_TIPO_BOUTIQUE : // boutique
					case Constantes.INTERCAMBIO_TIPO_PERSONAJE :// intercambio
					case Constantes.INTERCAMBIO_TIPO_TALLER : // accion oficio
					case Constantes.INTERCAMBIO_TIPO_COFRE :// cofre o banco
					case Constantes.INTERCAMBIO_TIPO_RECAUDADOR : // recaudador
					case Constantes.INTERCAMBIO_TIPO_MERCADILLO_COMPRAR :
					case Constantes.INTERCAMBIO_TIPO_MERCADILLO_VENDER : // mercadillo
					case Constantes.INTERCAMBIO_TIPO_TALLER_CLIENTE :
					case Constantes.INTERCAMBIO_TIPO_TALLER_ARTESANO :// invitar taller
					case Constantes.INTERCAMBIO_TIPO_CERCADO :// cercado
					case Constantes.INTERCAMBIO_TIPO_TRUEQUE :
					case Constantes.INTERCAMBIO_TIPO_RESUCITAR_MASCOTA :// mascota
					case 9 :// no se q es
						_exchanger.cerrar(this, exito);
						break;
				}
		}
	}
	
	public boolean esAbonado() {
		return (_cuenta != null && _cuenta.esAbonado());
	}
	
	public void teleport(final short nuevoMapaID, final short nuevaCeldaID) {
		if (esMultiman()) {
			return;
		}
		try {
			if (_tutorial != null || _inmovil || _calabozo) {
				if (_calabozo) {
					GestorSalida.ENVIAR_Im_INFORMACION(this, "1YOU_ARE_IN_JAIL");
				}
				if (_tutorial != null) {
					GestorSalida.ENVIAR_Im_INFORMACION(this, "1YOU_ARE_DOING_TUTORIAL");
				}
				if (_inmovil) {
					GestorSalida.ENVIAR_Im_INFORMACION(this, "1DONT_MOVE_TEMP");
				}
				return;
			}
			if (estaExchange()) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "1NO_PUEDES_TELEPORT_POR_EXCHANGE");
				return;
			}
			if (!_huir) {
				if (System.currentTimeMillis() - _tiempoAgresion > 8000) {
					_huir = true;
				} else {
					GestorSalida.ENVIAR_Im_INFORMACION(this, "1NO_PUEDES_HUIR;" + (System.currentTimeMillis() - _tiempoAgresion)
					/ 1000);
					return;
				}
			}
			Mapa nuevoMapa = Mundo.getMapa(nuevoMapaID);
			if (nuevoMapa == null) {
				nuevoMapa = Mundo.getMapa((short) 7411);
			}
			if (nuevoMapa.getCelda(nuevaCeldaID) == null) {
				return;
			}
			if (nuevoMapa.mapaAbonado() && !esAbonado()) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "131");
				return;
			}
			_casaDentro = Mundo.getCasaDentroPorMapa(nuevoMapaID);
			GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(_mapa, _id);
			_mapa = nuevoMapa;
			setCelda(_mapa.getCelda(nuevaCeldaID));
			if (_pregunta > 0) {
				dialogoFin();
			}
			GestorSalida.ENVIAR_GA2_CARGANDO_MAPA(this);
			GestorSalida.ENVIAR_GDM_CAMBIO_DE_MAPA(this, _mapa);
			GestorSalida.ENVIAR_GM_PJ_A_MAPA(_mapa, this);
			rastrearGrupo();
			int[] tt = {MisionObjetivoModelo.DESCUBRIR_MAPA, MisionObjetivoModelo.DESCUBRIR_ZONA};
			verificarMisionesTipo(tt, null, false, 0);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	private void rastrearGrupo() {
		if (_grupo != null && _grupo.getRastrear() != null && _grupo.getRastrear().getID() == _id) {
			for (final Personaje elQueSigue : _grupo.getMiembros()) {
				try {
					if (elQueSigue.getID() == _grupo.getRastrear().getID()) {
						continue;
					}
					if (elQueSigue._enLinea) {
						GestorSalida.ENVIAR_IC_PERSONAJE_BANDERA_COMPAS(elQueSigue, _mapa.getX() + "|" + _mapa.getY());
					}
				} catch (Exception e) {}
			}
		}
	}
	
	public boolean teleportSinTodos(final short nuevoMapaID, final short nuevaCeldaID) {
		Mapa nuevoMapa = Mundo.getMapa(nuevoMapaID);
		if (nuevoMapa == null || esMultiman()) {
			return false;
		}
		// if (_mapa.getID() == nuevoMapaID) {
		// return false;
		// }
		GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(_mapa, _id);
		GestorSalida.ENVIAR_GA2_CARGANDO_MAPA(this);
		GestorSalida.ENVIAR_GDM_CAMBIO_DE_MAPA(this, nuevoMapa);
		return true;
	}
	
	public void setHuir(final boolean huir) {
		_huir = huir;
	}
	
	public boolean getHuir() {
		return _huir;
	}
	
	public long getTiempoAgre() {
		return _tiempoAgresion;
	}
	
	public void setTiempoAgre(final long tiempo) {
		_tiempoAgresion = tiempo;
	}
	
	public void setAgresion(final boolean agre) {
		_agresion = agre;
	}
	
	public boolean getAgresion() {
		return _agresion;
	}
	
	public int getCostoAbrirBanco() {
		return _cuenta.getObjetosBanco().size();
	}
	
	public String getStringVar(final String str) {
		if (str.equalsIgnoreCase("nombre")) {
			return _nombre;
		}
		if (str.equalsIgnoreCase("costoBanco")) {
			return getCostoAbrirBanco() + "";
		}
		return "";
	}
	
	public void addKamasBanco(final long i) {
		_cuenta.addKamasBanco(i);
	}
	
	public long getKamasBanco() {
		return _cuenta.getKamasBanco();
	}
	
	public void addPuntosStats(final int pts) {
		_puntosStats += pts;
	}
	
	public void addPuntosHechizos(final int puntos) {
		_puntosHechizos += puntos;
	}
	
	public void abrirCercado() {
		if (_deshonor >= 5) {
			GestorSalida.ENVIAR_Im_INFORMACION(this, "183");
			return;
		}
		Cercado cercado = _mapa.getCercado();
		if (cercado == null) {
			return;
		}
		_exchanger = cercado;
		_tipoExchange = Constantes.INTERCAMBIO_TIPO_CERCADO;
		GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(this, _tipoExchange, analizarListaMonturas(cercado));
	}
	
	private String analizarListaMonturas(Cercado cercado) {
		final StringBuilder str = new StringBuilder();
		boolean primero = false;
		for (final Montura montura : _cuenta.getEstablo().values()) {
			if (primero) {
				str.append(";");
			}
			str.append(montura.detallesMontura());
			primero = true;
		}
		str.append("~");
		primero = false;
		for (final Montura montura : cercado.getCriando().values()) {
			if (montura.getDueñoID() == _id) {
				if (primero) {
					str.append(";");
				}
				str.append(montura.detallesMontura());
			} else {
				if (cercado.esPublico() || _miembroGremio == null || !_miembroGremio.puede(Constantes.G_OTRAS_MONTURAS)) {
					continue;
				}
				if (primero) {
					str.append(";");
				}
				str.append(montura.detallesMontura());
			}
			primero = true;
		}
		return str.toString();
	}
	
	public void refrescarStuff(boolean actualizar, boolean enviarAs, boolean visual) {
		// solo refresca los items equipados q no deben ser equipados
		if (actualizar) {
			actualizarObjEquipStats();
		}
		Encarnacion encarnacionTemp = _encarnacion;
		float velocidadTemp = Math.max(0, _totalStats.getStatsObjetos().getStatParaMostrar(Constantes.STAT_MAS_VELOCIDAD)
		/ 1000f);
		int aparienciaTemp = _totalStats.getStatsObjetos().getStatParaMostrar(Constantes.STAT_CAMBIA_APARIENCIA_2);
		int tituloTemp = getTitulo(false);
		Encarnacion encarnacion = null;
		do {
			actualizar = false;
			for (byte i : Constantes.POSICIONES_EQUIPAMIENTO) {
				final Objeto objeto = getObjPosicion(i);
				if (objeto == null) {
					continue;
				}
				if (objeto.getEncarnacion() != null) {
					encarnacion = objeto.getEncarnacion();
				}
				ObjetoModelo objMoverMod = objeto.getObjModelo();
				boolean desequipar = false;
				if (objMoverMod.getNivel() > _nivel) {
					desequipar = true;
				} else if (!Condiciones.validaCondiciones(this, objMoverMod.getCondiciones())) {
					desequipar = true;
				} else if (!puedeEquiparRepetido(objMoverMod, 2)) {
					desequipar = true;
				}
				if (desequipar) {
					// si el item no debe ser equipado, se le pone posicio no equipado
					actualizar = true;
					enviarAs |= true;
					if (Constantes.esPosicionVisual(i)) {
						visual |= true;
					}
					objeto.setPosicion(Constantes.OBJETO_POS_NO_EQUIPADO, this, false);
				}
			}
		} while (actualizar);
		float velocidad = Math.max(0, _totalStats.getStatsObjetos().getStatParaMostrar(Constantes.STAT_MAS_VELOCIDAD)
		/ 1000f);
		int apariencia = _totalStats.getStatsObjetos().getStatParaMostrar(Constantes.STAT_CAMBIA_APARIENCIA_2);
		int titulo = getTitulo(false);
		int[][] rB = {{RB_PUEDE_SWITCH_MODO_CRIATURA, apariencia != 0 ? 0 : 1}};
		setRestriccionesB(rB);
		boolean cambioEncarnacion = false;
		if (encarnacion != encarnacionTemp) {
			_encarnacion = encarnacion;
			if (_encarnacion != null) {
				_tiempoUltEncarnacion = System.currentTimeMillis();
			}
			if (_enLinea) {
				GestorSalida.ENVIAR_AC_CAMBIAR_CLASE(this, getClaseID(false));
				GestorSalida.ENVIAR_SL_LISTA_HECHIZOS(this);
				cambioEncarnacion = true;
			}
		}
		if (_encarnacion == null) {
			if (_enLinea) {
				refrescarParteSetClase(true);
			}
		}
		actualizarPDV(0);
		if (enviarAs && _enLinea) {
			GestorSalida.ENVIAR_As_STATS_DEL_PJ(this);
			GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(this);
		}
		if (cambioEncarnacion || velocidadTemp != velocidad || aparienciaTemp != apariencia || tituloTemp != titulo) {
			if (_enLinea) {
				refrescarEnMapa();
			}
		}
		if (visual) {
			cambiarRopaVisual();
		}
	}
	
	public boolean puedeEquiparRepetido(ObjetoModelo objMoverMod, int cant) {
		if ((objMoverMod.getSetID() > 0 || objMoverMod.getTipo() == Constantes.OBJETO_TIPO_DOFUS || objMoverMod
		.getTipo() == Constantes.OBJETO_TIPO_TROFEO) && Mundo.getCreaTuItem(objMoverMod.getID()) == null
		&& cantEquipadoModelo(objMoverMod.getID()) >= cant) {
			return false;
		}
		return true;
	}
	
	public void refrescarEnMapa() {
		if (_pelea == null) {
			GestorSalida.ENVIAR_GM_REFRESCAR_PJ_EN_MAPA(_mapa, this);
		} else if (_pelea.getFase() == Constantes.PELEA_FASE_POSICION) {
			final Luchador luchador = _pelea.getLuchadorPorID(_id);
			if (luchador != null) {
				GestorSalida.ENVIAR_GM_REFRESCAR_LUCHADOR_EN_PELEA(_pelea, luchador);
			}
		}
	}
	
	public void cambiarRopaVisual() {
		if (_pelea != null) {
			GestorSalida.ENVIAR_Oa_CAMBIAR_ROPA_PELEA(_pelea, this);
		} else {
			GestorSalida.ENVIAR_Oa_CAMBIAR_ROPA_MAPA(_mapa, this);
		}
		if (_grupo != null) {
			GestorSalida.ENVIAR_PM_ACTUALIZAR_INFO_PJ_GRUPO(_grupo, stringInfoGrupo());
		}
	}
	
	public String analizarListaAmigos(final int id) {
		final StringBuilder str = new StringBuilder(";");
		str.append("?;");
		str.append(_nombre + ";");
		if (_cuenta.esAmigo(id)) {
			str.append(_nivel + ";");
			str.append(_alineacion + ";");
		} else {
			str.append("?;");
			str.append("-1;");
		}
		str.append(_claseID + ";");
		str.append(_sexo + ";");
		str.append(getGfxID(false));
		return str.toString();
	}
	
	public String analizarListaEnemigos(final int id) {
		final StringBuilder str = new StringBuilder(";");
		str.append("?;");
		str.append(_nombre + ";");
		if (_cuenta.esEnemigo(id)) {
			str.append(_nivel + ";");
			str.append(_alineacion + ";");
		} else {
			str.append("?;");
			str.append("-1;");
		}
		str.append(_claseID + ";");
		str.append(_sexo + ";");
		str.append(getGfxID(false));
		return str.toString();
	}
	
	public boolean estaMontando() {
		return _montando;
	}
	
	public synchronized void subirBajarMontura(boolean obligatorio) {
		if (_montura == null) {
			if (_enLinea) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "1MOUNT_NO_EQUIP");
			}
			return;
		}
		if (!obligatorio) {
			if (_encarnacion != null) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "134|44");
				return;
			}
			if (_pelea != null && (_pelea.getFase() != Constantes.PELEA_FASE_POSICION || _pelea.esEspectador(_id))) {
				return;
			}
			if (!_montando) {// va a montar
				if (_nivel < 60 || esFantasma() || esTumba()) {
					GestorSalida.ENVIAR_Im_INFORMACION(this, "1MOUNT_ERROR_RIDE");
					return;
				}
				if (MainServidor.PARAM_CRIAR_MONTURA) {
					if (_montura.getEnergia() < 10) {
						GestorSalida.ENVIAR_Im_INFORMACION(this, "1113");
						return;
					}
					if (!_montura.esMontable()) {
						GestorSalida.ENVIAR_Im_INFORMACION(this, "1176");
						return;
					}
				}
				if (_casaDentro != null) {
					GestorSalida.ENVIAR_Im_INFORMACION(this, "1117");
					return;
				}
				if (_montura.getDueñoID() != _id) {
					GestorSalida.ENVIAR_BN_NADA(this, "SUBIR BAJAR MONTURA NO DUEÑO " + _montura.getDueñoID());
					return;
				}
			}
			_montura.energiaPerdida(15);
		}
		_montando = !_montando;
		final Objeto mascota = getObjPosicion(Constantes.OBJETO_POS_MASCOTA);
		if (_montando && mascota != null) {
			mascota.setPosicion(Constantes.OBJETO_POS_NO_EQUIPADO, this, false);
		}
		refrescarStuff(true, !obligatorio, false);
		if (!obligatorio) {
			if (_enLinea) {
				refrescarEnMapa();
				GestorSalida.ENVIAR_Re_DETALLES_MONTURA(this, "+", _montura);
				GestorSalida.ENVIAR_Rr_ESTADO_MONTADO(this, _montando ? "+" : "-");
			}
		}
	}
	
	public Montura getMontura() {
		return _montura;
	}
	
	public void setMontura(final Montura montura) {
		_montura = montura;
		if (_montura != null) {
			_montura.setUbicacion(Ubicacion.EQUIPADA);
		}
		if (_enLinea) {
			GestorSalida.ENVIAR_Re_DETALLES_MONTURA(this, _montura != null ? "+" : "-", _montura);
		}
	}
	
	public int getPorcXPMontura() {
		return _porcXPMontura;
	}
	
	public void setPorcXPMontura(final int porcXP) {
		_porcXPMontura = porcXP;
		_porcXPMontura = Math.max(_porcXPMontura, 0);
		_porcXPMontura = Math.min(_porcXPMontura, 90);
	}
	
	public void resetearVariables() {
		_prePelea = null;// es para perco y prismas
		_tipoExchange = Constantes.INTERCAMBIO_TIPO_NULO;
		if (_invitandoA != null) {
			_invitandoA.setInvitador(null, "");
			_invitandoA = null;
		}
		if (_invitador != null) {
			_invitador.setInvitandoA(null, "");
			_invitador = null;
		}
		_tipoInvitacion = "";
		_conversandoCon = 0;
		_pretendiente = 0;
		_pregunta = 0;
		_emoteActivado = 0;
		_exchanger = null;
		_tutorial = null;
		_consultarCasa = null;
		_consultarCofre = null;
		_ocupado = false;
		_sentado = false;
		_ausente = false;// para q no recibas MP de todos
		_invisible = false;// para q solo recibias MP de tus amigos
		_cargandoMapa = false;
		_olvidandoHechizo = false;
		_deDia = false;
		_deNoche = false;
		_idsOmitidos.clear();
		_bonusSetDeClase.clear();
		if (estaMontando()) {
			subirBajarMontura(true);
		}
		addCanal("~");
		limpiarPacketsCola();
	}
	
	public void setPrePelea(Pelea pelea, int idUnirse) {
		_prePelea = pelea;
		_unirsePrePeleaAlID = idUnirse;
	}
	
	public Pelea getPrePelea() {
		return _prePelea;
	}
	
	public int getUnirsePrePeleaAlID() {
		return _unirsePrePeleaAlID;
	}
	
	public void addOmitido(String name) {
		Personaje p = Mundo.getPersonajePorNombre(name);
		if (p == null) {
			return;
		}
		if (!_idsOmitidos.contains(p.getID())) {
			_idsOmitidos.add(p.getID());
		}
	}
	
	public void borrarOmitido(String name) {
		Personaje p = Mundo.getPersonajePorNombre(name);
		if (p == null) {
			return;
		}
		if (_idsOmitidos.contains(p.getID())) {
			_idsOmitidos.remove((Object) p.getID());
		}
	}
	
	public String getCanales() {
		return _canales;
	}
	
	public boolean tieneCanal(String c) {
		switch (c) {
			case "p" :// espectador
			case "F" :// envia
			case "T" :// recibe
			case "@" :// admin
			case "¡" :// vip
			case "¬" :// unknown
			case "~" :// all
				return true;
		}
		return _canales.contains(c);
	}
	
	public void addCanal(final String canal) {
		GestorSalida.ENVIAR_cC_SUSCRIBIR_CANAL(this, '+', canal);
		if (_canales.contains(canal)) {
			return;
		}
		_canales += canal;
	}
	
	public void removerCanal(final String canal) {
		_canales = _canales.replace(canal, "");
		GestorSalida.ENVIAR_cC_SUSCRIBIR_CANAL(this, '-', canal);
	}
	
	public boolean cambiarAlineacion(final byte alineacion, boolean siOsi) {
		if (!siOsi) {
			if (getDeshonor() >= 2) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "183");
				return false;
			}
		}
		_honor = 0;
		_deshonor = 0;
		_alineacion = alineacion;
		_mostrarAlas = alineacion != Constantes.ALINEACION_NEUTRAL;
		if (_alineacion != Constantes.ALINEACION_NEUTRAL) {
			if (MainServidor.HONOR_FIJO_PARA_TODOS > -1) {
				_honor = MainServidor.HONOR_FIJO_PARA_TODOS;
			}
		}
		_ordenNivel = 0;
		switch (_alineacion) {
			case Constantes.ALINEACION_BONTARIANO :
				_orden = 1;
				break;
			case Constantes.ALINEACION_BRAKMARIANO :
				_orden = 5;
				break;
			case Constantes.ALINEACION_MERCENARIO :
				_orden = 9;
				break;
			default :
				_orden = 0;
				break;
		}
		refrescarGradoAlineacion();
		Especialidad esp = Mundo.getEspecialidad(_orden, _ordenNivel);
		if (esp != null) {
			GestorSalida.ENVIAR_ZC_CAMBIAR_ESPECIALIDAD_ALINEACION(this, esp.getID());
		}
		actualizarStatsEspecialidad(esp);
		refrescarStuff(true, true, false);
		refrescarEnMapa();
		return true;
	}
	
	public void addOrdenNivel(int nivel) {
		Especialidad esp = Mundo.getEspecialidad(_orden, _ordenNivel);
		_ordenNivel += nivel;
		if (_ordenNivel > 100) {
			_ordenNivel = 100;
		}
		if (esp != null && esp.getID() != (esp = Mundo.getEspecialidad(_orden, _ordenNivel)).getID()) {
			GestorSalida.ENVIAR_ZC_CAMBIAR_ESPECIALIDAD_ALINEACION(this, esp.getID());
		}
		actualizarStatsEspecialidad(esp);
		GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
	}
	
	public void setOrden(int orden) {
		_orden = orden;
		Especialidad esp = Mundo.getEspecialidad(_orden, _ordenNivel);
		if (esp != null) {
			GestorSalida.ENVIAR_ZC_CAMBIAR_ESPECIALIDAD_ALINEACION(this, esp.getID());
		}
		actualizarStatsEspecialidad(esp);
		GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
	}
	
	public void actualizarStatsEspecialidad(Especialidad esp) {
		_totalStats.getStatsBendMald().clear();
		if (esp == null) {
			return;
		}
		for (Don don : esp.getDones()) {
			_totalStats.getStatsBendMald().acumularStats(don.getStat());
		}
	}
	
	public int getOrden() {
		return _orden;
	}
	
	public int getOrdenNivel() {
		return _ordenNivel;
	}
	
	public int getEspecialidad() {
		Especialidad esp = Mundo.getEspecialidad(_orden, _ordenNivel);
		if (esp != null) {
			return esp.getID();
		}
		return 0;
	}
	
	public int getDeshonor() {
		return _deshonor;
	}
	
	public boolean addDeshonor(int deshonor) {
		if (_alineacion == Constantes.ALINEACION_NEUTRAL || !MainServidor.PARAM_PERMITIR_DESHONOR) {
			return false;
		}
		_deshonor += deshonor;
		if (_deshonor < 0) {
			_deshonor = 0;
		}
		return true;
	}
	
	public int getHonor() {
		return _honor;
	}
	
	public void addHonor(final int honor) {
		if (esMultiman()) {
			return;
		}
		if (honor == 0 || _alineacion == Constantes.ALINEACION_NEUTRAL) {
			return;
		}
		if (honor > 0) {
			GestorSalida.ENVIAR_Im_INFORMACION(this, "074;" + honor);
		} else if (honor < 0) {
			GestorSalida.ENVIAR_Im_INFORMACION(this, "076;" + -honor);
		}
		_honor += honor;
		refrescarGradoAlineacion();
	}
	
	public void refrescarGradoAlineacion() {
		final int nivelAntes = _gradoAlineacion;
		if (_honor < 0) {
			_honor = 0;
		} else if (_honor >= Mundo.getExpAlineacion(MainServidor.NIVEL_MAX_ALINEACION)) {
			_gradoAlineacion = MainServidor.NIVEL_MAX_ALINEACION;
			_honor = Mundo.getExpAlineacion(MainServidor.NIVEL_MAX_ALINEACION);
		}
		for (byte n = 1; n <= MainServidor.NIVEL_MAX_ALINEACION; n++) {
			if (_honor < Mundo.getExpAlineacion(n)) {
				_gradoAlineacion = (byte) (n - 1);
				break;
			}
		}
		if (nivelAntes == _gradoAlineacion) {
			return;
		}
		if (nivelAntes < _gradoAlineacion) {
			GestorSalida.ENVIAR_Im_INFORMACION(this, "082;" + _gradoAlineacion);
		} else if (nivelAntes > _gradoAlineacion) {
			GestorSalida.ENVIAR_Im_INFORMACION(this, "083;" + _gradoAlineacion);
		}
		refrescarStuff(true, true, false);
	}
	
	public int getGradoAlineacion() {
		if (_alineacion == Constantes.ALINEACION_NEUTRAL) {
			return 1;
		}
		return _gradoAlineacion;
	}
	
	public void botonActDesacAlas(final char c) {
		if (_alineacion == Constantes.ALINEACION_NEUTRAL) {
			_mostrarAlas = false;
			return;
		}
		if (!MainServidor.PARAM_PERMITIR_DESACTIVAR_ALAS) {
			_mostrarAlas = _alineacion != Constantes.ALINEACION_NEUTRAL;
			return;
		}
		final int honorPerd = _honor / 20;
		switch (c) {
			case '*' :
				GestorSalida.ENVIAR_GIP_ACT_DES_ALAS_PERDER_HONOR(this, honorPerd);
				return;
			case '+' :
				_mostrarAlas = true;
				GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
				break;
			case '-' :
				_mostrarAlas = false;
				addHonor(-honorPerd);
				GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(this);
				break;
		}
	}
	
	public MiembroGremio getMiembroGremio() {
		return _miembroGremio;
	}
	
	public int getCuentaID() {
		if (_cuenta == null) {
			return -1;
		}
		return _cuenta.getID();
	}
	
	public boolean cumplirMisionAlmanax() {
		if (!MainServidor.PARAM_ALMANAX) {
			GestorSalida.ENVIAR_BN_NADA(this, "ALMANAX NO DISPONIBLE");
			return false;
		}
		// buscar ontoral Zo
		if (realizoMisionDelDia()) {
			GestorSalida.ENVIAR_BN_NADA(this, "YA REALIZO ALMANAX DEL DIA");
			return false;
		}
		Almanax almanax = Mundo.getAlmanaxDelDia();
		if (almanax == null) {
			GestorSalida.ENVIAR_BN_NADA(this, "NO EXISTE ALMANAX DEL DIA");
			return false;
		}
		int id = almanax.getOfrenda()._primero;
		int cant = almanax.getOfrenda()._segundo;
		if (tenerYEliminarObjPorModYCant(id, cant)) {
			GestorSalida.ENVIAR_Im_INFORMACION(this, "022;" + cant + "~" + id);
			GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(this);
			agregarMisionDelDia();
		} else {
			GestorSalida.ENVIAR_Im_INFORMACION(this, "14");
		}
		return true;
	}
	
	public String listaPrismas() {
		final StringBuilder str = new StringBuilder(_mapa.getID());
		final int subAreaID = _mapa.getSubArea().getArea().getSuperArea().getID();
		for (final Prisma prisma : Mundo.getPrismas()) {
			try {
				if (prisma.getAlineacion() != _alineacion) {
					continue;
				}
				if (prisma.getMapa().getSubArea().getArea().getSuperArea().getID() != subAreaID) {
					continue;
				}
				if (prisma.getEstadoPelea() == 0 || prisma.getEstadoPelea() == -2) {
					str.append("|" + prisma.getMapa().getID() + ";*");
				} else {
					int costo = Formulas.calcularCosteZaap(_mapa, prisma.getMapa());
					str.append("|" + prisma.getMapa().getID() + ";" + costo);
				}
			} catch (Exception e) {}
		}
		return str.toString();
	}
	
	public String listaZaap() {
		final StringBuilder str = new StringBuilder();
		if (_zaaps.contains(_mapaSalvada)) {
			str.append(_mapaSalvada);
		}
		final int superAreaID = _mapa.getSubArea().getArea().getSuperArea().getID();
		for (final short i : _zaaps) {
			try {
				if (Mundo.getMapa(i).getSubArea().getArea().getSuperArea().getID() != superAreaID) {
					continue;
				}
				int costo = Formulas.calcularCosteZaap(_mapa, Mundo.getMapa(i));
				str.append("|" + i + ";" + costo);
			} catch (final Exception e) {}
		}
		return str.toString();
	}
	
	public boolean tieneZaap(final short mapaID) {
		return _zaaps.contains(mapaID);
	}
	
	public void abrirMenuZaap() {
		if (_deshonor >= 3) {
			GestorSalida.ENVIAR_Im_INFORMACION(this, "183");
			return;
		}
		if (!tieneZaap(_mapa.getID())) {
			_zaaps.add(_mapa.getID());
			_zaaps.trimToSize();
			GestorSalida.ENVIAR_Im_INFORMACION(this, "024");
		}
		GestorSalida.ENVIAR_WC_MENU_ZAAP(this);
	}
	
	public void abrirMenuZaapi() {
		if (_deshonor >= 3) {
			GestorSalida.ENVIAR_Im_INFORMACION(this, "183");
			return;
		}
		final StringBuilder listaZaapi = new StringBuilder();
		if (_mapa.getSubArea().getArea().getID() != 7 || _alineacion == Constantes.ALINEACION_BRAKMARIANO) {
			// nada
		} else {
			final String[] Zaapis = "6159,4174,8758,4299,4180,8759,4183,2221,4300,4217,4098,8757,4223,8760,2214,4179,4229,4232,8478,4238,4263,4216,4172,4247,4272,4271,4250,4178,4106,4181,4259,4090,4262,4287,4300,4240,4218,4074,4308"
			.split(",");
			int precio = 20;
			if (_alineacion == Constantes.ALINEACION_BONTARIANO) {
				precio = 10;
			}
			for (final String s : Zaapis) {
				listaZaapi.append(s + ";" + precio + "|");
			}
		}
		if (_mapa.getSubArea().getArea().getID() != 11 || _alineacion == Constantes.ALINEACION_BONTARIANO) {
			// nada
		} else {
			final String[] Zaapis = "8756,8755,8493,5304,5311,5277,5317,4612,4618,5112,4639,4637,5116,5332,4579,4588,4549,4562,5334,5295,4646,4629,4601,4551,4607,4930,4622,4620,4615,4595,4627,4623,4604,8754,8753,4630"
			.split(",");
			int precio = 20;
			if (_alineacion == Constantes.ALINEACION_BRAKMARIANO) {
				precio = 10;
			}
			for (final String s : Zaapis) {
				listaZaapi.append(s + ";" + precio + "|");
			}
		}
		GestorSalida.ENVIAR_Wc_LISTA_ZAPPIS(this, _mapa.getID() + "|" + listaZaapi.toString());
	}
	
	public void abrirMenuPrisma() {
		if (_deshonor >= 3) {
			GestorSalida.ENVIAR_Im_INFORMACION(this, "183");
			return;
		}
		GestorSalida.ENVIAR_Wp_MENU_PRISMA(this);
	}
	
	public String stringZaapsParaBD() {
		final StringBuilder str = new StringBuilder();
		for (final short i : _zaaps) {
			if (str.length() > 0) {
				str.append(",");
			}
			str.append(i);
		}
		return str.toString();
	}
	
	public void usarZaap(final short mapaID) {
		try {
			final Mapa mapa = Mundo.getMapa(mapaID);
			if (mapa == null || mapaID == _mapa.getID() || !tieneZaap(_mapa.getID()) || !tieneZaap(mapaID)) {
				GestorSalida.ENVIAR_BN_NADA(this);
				return;
			}
			final short celdaID = Mundo.getCeldaZaapPorMapaID(mapaID);
			if (mapa.getCelda(celdaID) == null || mapa.getSubArea().getArea().getSuperArea().getID() != _mapa.getSubArea()
			.getArea().getSuperArea().getID()) {
				GestorSalida.ENVIAR_WUE_ZAPPI_ERROR(this);
				return;
			}
			if (_alineacion == Constantes.ALINEACION_BRAKMARIANO) {
				if (mapaID == 4263 || _mapa.getSubArea().getAlineacion() == Constantes.ALINEACION_BONTARIANO) {
					GestorSalida.ENVIAR_Im_INFORMACION(this, "1TEAM_DIFFERENT_ALIGNMENT");
					GestorSalida.ENVIAR_WUE_ZAPPI_ERROR(this);
					return;
				}
			}
			if (_alineacion == Constantes.ALINEACION_BONTARIANO) {
				if (mapaID == 5295 || _mapa.getSubArea().getAlineacion() == Constantes.ALINEACION_BRAKMARIANO) {
					GestorSalida.ENVIAR_Im_INFORMACION(this, "1TEAM_DIFFERENT_ALIGNMENT");
					GestorSalida.ENVIAR_WUE_ZAPPI_ERROR(this);
					return;
				}
			}
			final int costo = Formulas.calcularCosteZaap(_mapa, mapa);
			if (_kamas < costo) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "182");
				return;
			}
			addKamas(-costo, false, true);
			teleport(mapaID, celdaID);
			GestorSalida.ENVIAR_WV_CERRAR_ZAAP(this);
		} catch (final Exception e) {}
	}
	
	public void usarZaapi(final short mapaID) {
		try {
			if (_deshonor >= 2) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "183");
				return;
			}
			final Mapa mapa = Mundo.getMapa(mapaID);
			if (mapa == null || mapaID == _mapa.getID() || !Mundo.esZaapi(_mapa.getID(), _alineacion) || !Mundo.esZaapi(
			mapaID, _alineacion)) {
				GestorSalida.ENVIAR_BN_NADA(this);
				return;
			}
			short celdaID = 0;
			for (final Celda celda : mapa.getCeldas().values()) {
				try {
					if (celda.getObjetoInteractivo().getObjIntModelo().getID() != 106) {
						continue;
					}
					celdaID = (short) (celda.getID() + mapa.getAncho());
					break;
				} catch (final Exception e) {}
			}
			if (celdaID == 0) {
				return;
			}
			int costo = 20;
			if (_alineacion == Constantes.ALINEACION_BONTARIANO || _alineacion == Constantes.ALINEACION_BRAKMARIANO) {
				costo = 10;
			}
			if (_kamas < costo) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "182");
				return;
			}
			addKamas(-costo, false, true);
			teleport(mapaID, celdaID);
			GestorSalida.ENVIAR_Wv_CERRAR_ZAPPI(this);
		} catch (final Exception e) {}
	}
	
	public void usarPrisma(final short mapaID) {
		try {
			if (_deshonor >= 1) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "183");
				return;
			}
			final Mapa mapa = Mundo.getMapa(mapaID);
			if (mapa == null || mapaID == _mapa.getID() || _mapa.getPrisma() == null || mapa.getPrisma() == null || _mapa
			.getPrisma().getAlineacion() != _alineacion || mapa.getPrisma().getAlineacion() != _alineacion) {
				GestorSalida.ENVIAR_BN_NADA(this);
				return;
			}
			if (!alasActivadas()) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "1144");
				return;
			}
			short celdaID = mapa.getPrisma().getCelda().getID();
			int costo = Formulas.calcularCosteZaap(_mapa, Mundo.getMapa(mapaID));
			if (_kamas < costo) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "182");
				return;
			}
			addKamas(-costo, false, true);
			teleport(mapaID, celdaID);
			GestorSalida.ENVIAR_Ww_CERRAR_PRISMA(this);
		} catch (final Exception e) {}
	}
	
	public void usarZonas(final short mapaID) {
		try {
			if (Mundo.getMapa(mapaID) == null || Mundo.ZONAS.get(mapaID) == null) {
				return;
			}
			teleport(mapaID, Mundo.ZONAS.get(mapaID));
		} catch (final Exception e) {}
	}
	
	public Objeto getObjModeloNoEquipado(final int idModelo, final int cantidad) {
		for (final Objeto obj : _objetos.values()) {
			if (Constantes.esPosicionEquipamiento(obj.getPosicion()) || obj.getObjModeloID() != idModelo) {
				continue;
			}
			if (obj.getCantidad() >= cantidad) {
				return obj;
			}
		}
		return null;
	}
	
	public void setOlvidandoHechizo(final boolean olvidandoHechizo) {
		_olvidandoHechizo = olvidandoHechizo;
	}
	
	public boolean estaOlvidandoHechizo() {
		return _olvidandoHechizo;
	}
	
	public boolean estaVisiblePara(final Personaje perso) {
		if (_ausente) {
			return false;
		}
		if (_idsOmitidos.contains(perso.getID())) {
			return false;
		}
		if (_cuenta.esEnemigo(perso.getCuentaID())) {
			return false;
		}
		if (_invisible) {
			return _cuenta.esAmigo(perso.getCuentaID());
		}
		return true;
	}
	
	public void enviarMsjAAmigos() {
		String str = getCuenta().getApodo() + " (<b><a href='asfunction:onHref,ShowPlayerPopupMenu," + getNombre() + "'>"
		+ getNombre() + "</a></b>)";
		for (Personaje online : Mundo.getPersonajesEnLinea()) {
			try {
				if (online._cuenta.esAmigo(_id)) {
					GestorSalida.ENVIAR_Im0143_AMIGO_CONECTADO(online, str);
				}
			} catch (Exception e) {}
		}
	}
	
	public boolean estaAusente() {
		return _ausente;
	}
	
	public void setAusente(final boolean ausente) {
		_ausente = ausente;
	}
	
	public boolean esIndetectable() {
		return _indetectable;
	}
	
	public void setIndetectable(final boolean indetectable) {
		_indetectable = indetectable;
	}
	
	public boolean esInvisible() {
		return _invisible;
	}
	
	public void setInvisible(final boolean invisible) {
		_invisible = invisible;
	}
	
	public boolean esFantasma() {
		return !getRestriccionB(RB_NO_ES_FANTASMA);
	}
	
	public boolean esTumba() {
		return !getRestriccionB(RB_NO_ES_TUMBA);
	}
	
	public int getTitulo(boolean real) {
		if (!real) {
			try {
				String titulo = _totalStats.getStatsObjetos().getParamStatTexto(Constantes.STAT_TITULO, 3);
				if (!titulo.isEmpty()) {
					return Integer.parseInt(titulo, 16);
				}
			} catch (Exception e) {}
		}
		return _titulo;
	}
	
	public String listaTitulosParaBD() {
		final StringBuilder str = new StringBuilder();
		for (final int b : _titulos.keySet()) {
			if (str.length() > 0) {
				str.append(",");
			}
			if (b == _titulo) {
				str.append("+");
			}
			str.append(b);
			if (_titulos.get(b) > -1) {
				str.append("*" + _titulos.get(b));
			}
		}
		return str.toString();
	}
	
	public void setTituloVIP(final String titulo) {
		_tituloVIP = titulo;
	}
	
	public String getTituloVIP() {
		return _tituloVIP;
	}
	
	public void addTitulo(final int titulo, int color) {
		if (titulo > 0) {
			_titulos.put(titulo, color);
		}
		_titulo = titulo;
		if (_enLinea) {
			refrescarEnMapa();
		}
	}
	
	public void addOrnamento(final int ornamento) {
		if (ornamento <= 0 || _ornamentos.contains(ornamento)) {
			return;
		}
		_ornamentos.add(ornamento);
		_ornamentos.trimToSize();
	}
	
	public void setOrnamento(final int ornamento) {
		if (ornamento <= 0 || _ornamentos.contains(ornamento)) {
			_ornamento = ornamento;
			refrescarEnMapa();
		} else {
			GestorSalida.ENVIAR_BN_NADA(this);
		}
	}
	
	public String listaOrnamentosParaBD() {
		final StringBuilder str = new StringBuilder();
		for (final int b : _ornamentos) {
			if (str.length() > 0) {
				str.append(",");
			}
			if (b == _ornamento) {
				str.append("+");
			}
			str.append(b);
		}
		return str.toString();
	}
	
	public boolean tieneOrnamento(final int ornamento) {
		return _ornamentos.contains(ornamento);
	}
	
	public boolean tieneTitulo(final int ornamento) {
		return _titulos.containsKey(ornamento);
	}
	
	public int getOrnamento() {
		return _ornamento;
	}
	
	public void setNombre(final String nombre) {
		_nombre = nombre;
		GestorSQL.UPDATE_NOMBRE_PJ(this);
		if (getMiembroGremio() != null) {
			GestorSQL.REPLACE_MIEMBRO_GREMIO(getMiembroGremio());
		}
		try {
			Mundo.getRankingPVP(_id).setNombre(_nombre);
			GestorSQL.REPLACE_RANKING_PVP(Mundo.getRankingPVP(_id));
		} catch (final Exception e) {}
		try {
			Mundo.getRankingKoliseo(_id).setNombre(_nombre);
			GestorSQL.REPLACE_RANKING_KOLISEO(Mundo.getRankingKoliseo(_id));
		} catch (final Exception e) {}
	}
	
	public MisionPVP getMisionPVP() {
		return _misionPvp;
	}
	
	public void setMisionPVP(final MisionPVP mision) {
		_misionPvp = mision;
	}
	
	public String getEsposoListaAmigos() {
		final Personaje esposo = Mundo.getPersonaje(_esposoID);
		final StringBuilder str = new StringBuilder();
		if (esposo != null) {
			str.append(esposo._nombre + "|" + esposo._claseID + esposo._sexo + "|" + esposo._color1 + "|" + esposo._color2
			+ "|" + esposo._color3 + "|");
			if (!esposo._enLinea) {
				str.append("|");
			} else {
				str.append(esposo.stringUbicEsposo() + "|");
			}
		} else {
			str.append("|");
		}
		return str.toString();
	}
	
	public String stringUbicEsposo() {
		return _mapa.getID() + "|" + _nivel + "|" + (_pelea != null ? 1 : 0);
	}
	
	public void seguirEsposo(Personaje esposo, String packet) {
		if (packet.charAt(3) == '+') {
			if (esposo.getMapa().getSubArea().getArea().getSuperArea() != _mapa.getSubArea().getArea().getSuperArea()) {
				if (esposo.getSexo() == Constantes.SEXO_FEMENINO) {
					GestorSalida.ENVIAR_Im_INFORMACION(this, "178");
				} else {
					GestorSalida.ENVIAR_Im_INFORMACION(this, "179");
				}
			}
			GestorSalida.ENVIAR_IC_PERSONAJE_BANDERA_COMPAS(this, esposo.getMapa().getX() + "|" + esposo.getMapa().getY());
		} else {
			GestorSalida.ENVIAR_IC_BORRAR_BANDERA_COMPAS(this);
		}
	}
	
	public void teleportEsposo(final Personaje esposo) {
		if (!estaDisponible(false, true)) {
			if (esposo.getSexo() == Constantes.SEXO_FEMENINO) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "139");
			} else {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "140");
			}
			return;
		}
		if (esFantasma() || esTumba()) {
			if (esposo.getSexo() == Constantes.SEXO_FEMENINO) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "178");
			} else {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "179");
			}
			return;
		}
		final int dist = Camino.distanciaEntreMapas(_mapa, esposo.getMapa());
		if (dist > 10 || esposo.getMapa().esMazmorra()) {
			if (esposo.getSexo() == Constantes.SEXO_FEMENINO) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "181");
			} else {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "180");
			}
			return;
		}
		final short celdaPosicion = Camino.getCeldaIDCercanaLibre(esposo.getCelda(), esposo.getMapa());
		if (celdaPosicion == 0) {
			if (esposo.getSexo() == Constantes.SEXO_FEMENINO) {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "141");
			} else {
				GestorSalida.ENVIAR_Im_INFORMACION(this, "142");
			}
			return;
		}
		teleport(esposo.getMapa().getID(), celdaPosicion);
	}
	
	private void cambiarOrientacionADiagonal(final byte orientacion) {
		switch (_orientacion) {
			case 0 :
			case 2 :
			case 4 :
			case 6 :
				setOrientacion(orientacion);
				GestorSalida.ENVIAR_eD_CAMBIAR_ORIENTACION(_mapa, getID(), orientacion);
				break;
		}
	}
	
	public void addObjetoAlBanco(Objeto obj) {
		_cuenta.addObjetoAlBanco(obj);
	}
	
	public void setConsultarCasa(final Casa casa) {
		_consultarCasa = casa;
	}
	
	public Cofre getConsultarCofre() {
		return _consultarCofre;
	}
	
	public void setConsultarCofre(Cofre cofre) {
		_consultarCofre = cofre;
	}
	
	public Casa getConsultarCasa() {
		return _consultarCasa;
	}
	
	public Casa getAlgunaCasa() {
		if (_consultarCasa != null) {
			return _consultarCasa;
		}
		if (_casaDentro != null) {
			return _casaDentro;
		}
		return null;
	}
	
	public Casa getCasaDentro() {
		return _casaDentro;
	}
	
	public String stringColor() {
		return (_color1 <= -1 ? "" : Integer.toHexString(_color1)) + "," + (_color2 <= -1
		? ""
		: Integer.toHexString(_color2)) + "," + (_color3 <= -1 ? "" : Integer.toHexString(_color3));
	}
	
	private String strObjEnPosParaOa(final byte posicion) {
		final Objeto obj = getObjPosicion(posicion);
		if (obj == null) {
			return "null";
		}
		try {
			if (obj.getParamStatTexto(Constantes.STAT_APARIENCIA_OBJETO, 2) != "") {
				return obj.getParamStatTexto(Constantes.STAT_APARIENCIA_OBJETO, 3);
			}
			if (obj.getObjevivoID() > 0) {
				final Objeto objVivo = Mundo.getObjeto(obj.getObjevivoID());
				if (objVivo != null) {
					return Integer.toHexString(objVivo.getObjModeloID()) + "~" + obj.getObjModelo().getTipo() + "~" + Byte
					.parseByte(objVivo.getParamStatTexto(Constantes.STAT_SKIN_OBJEVIVO, 3), 16);
				} else {
					obj.setIDObjevivo(0);
				}
			}
			if (Mundo.getCreaTuItem(obj.getObjModeloID()) != null) {
				return Integer.toHexString(obj.getObjModeloID()) + "~" + obj.getObjModelo().getTipo() + "~" + (Integer.parseInt(
				obj.getParamStatTexto(Constantes.STAT_CAMBIAR_GFX_OBJETO, 3), 16) + 1);
			}
		} catch (Exception e) {}
		return Integer.toHexString(obj.getObjModeloID());
	}
	
	public void setExchanger(final Exchanger intercambiando) {
		_exchanger = intercambiando;
	}
	
	@SuppressWarnings("rawtypes")
	public Object getIntercambiandoCon(Class clase) {
		if (_exchanger == null) {
			return null;
		}
		if (_exchanger.getClass() == clase) {
			return (clase.cast(_exchanger));
		}
		return null;
	}
	
	public Exchanger getExchanger() {
		return _exchanger;
	}
	
	public String getStringTienda() {
		final StringBuilder str = new StringBuilder();
		for (final Objeto obj : _tienda.getObjetos()) {
			str.append(obj.getID() + "|");
		}
		return str.toString();
	}
	
	public void borrarObjTienda(final Objeto obj) {
		_tienda.borrarObjeto(obj);
	}
	
	public ArrayList<Objeto> getObjetosTienda() {
		return _tienda.getObjetos();
	}
	
	public Tienda getTienda() {
		return _tienda;
	}
	
	public long precioTotalTienda() {
		long precio = 0;
		for (final Objeto obj : _tienda.getObjetos()) {
			precio += obj.getPrecio();
		}
		return precio;
	}
	
	public String getListaExchanger(Personaje perso) {
		final StringBuilder str = new StringBuilder();
		for (final Objeto obj : _tienda.getObjetos()) {
			if (str.length() > 0) {
				str.append("|");
			}
			str.append(obj.getID() + ";" + obj.getCantidad() + ";" + obj.getObjModeloID() + ";" + obj.convertirStatsAString(
			false) + ";" + obj.getPrecio());
		}
		return str.toString();
	}
	
	public String stringGMLuchador() {
		StringBuilder str = new StringBuilder();
		str.append(getClaseID(false) + ";");
		str.append(getGfxID(false) + "^" + getTalla() + ";");
		str.append(getSexo() + ";");
		str.append(getNivel() + ";");
		str.append(getAlineacion() + ",");
		str.append(getOrdenNivel() + ",");
		str.append((alasActivadas() ? getGradoAlineacion() : "0") + ",");
		str.append((getID() + getNivel()) + "," + (getDeshonor() > 0 ? 1 : 0) + ";");
		str.append((getColor1() > -1 ? Integer.toHexString(getColor1()) : -1) + ";");
		str.append((getColor2() > -1 ? Integer.toHexString(getColor2()) : -1) + ";");
		str.append((getColor3() > -1 ? Integer.toHexString(getColor3()) : -1) + ";");
		str.append(getStringAccesorios() + ";");
		return str.toString();
	}
	
	public Map<Integer, StatHechizo> getHechizos() {
		return _mapStatsHechizos;
	}
	
	public void addKamasGanada(long kamas) {}
	
	public void addXPGanada(long exp) {}
	
	public void mostrarGrupo() {
		if (_grupo == null) {
			return;
		}
		Personaje lider = _grupo.getLiderGrupo();
		GestorSalida.ENVIAR_PCK_CREAR_GRUPO(this, lider.getNombre());
		GestorSalida.ENVIAR_PL_LIDER_GRUPO(this, lider.getID());
		GestorSalida.ENVIAR_PM_TODOS_MIEMBROS_GRUPO_A_PERSO(this, _grupo);
	}
	
	@Override
	public void murio() {}
	
	@Override
	public void sobrevivio() {}
	
	public void cambiarNombre(String nombre) {
		setNombre(nombre);
		GestorSalida.ENVIAR_bn_CAMBIAR_NOMBRE_CONFIRMADO(this, nombre);
		refrescarEnMapa();
		GestorSalida.ENVIAR_Im_INFORMACION(this, "1NAME_CHANGED;" + nombre);
	}
	
	public static String nombreValido(String nombre, boolean comando) {
		if (Mundo.getPersonajePorNombre(nombre) != null) {
			return null;
		}
		if (nombre.length() < 1 || nombre.length() > 20) {
			return "";
		}
		if (!comando) {
			StringBuilder nombreFinal = new StringBuilder("");
			final String nLower = nombre.toLowerCase();
			final String abcMin = "abcdefghijklmnopqrstuvwxyz-";
			int cantSimbol = 0;
			char letra_A = ' ', letra_B = ' ';
			boolean primera = true;
			for (final char letra : nLower.toCharArray()) {
				if (primera && letra == '-' || !abcMin.contains(letra + "") || letra == letra_A && letra == letra_B) {
					return "";
				}
				if (primera) {
					nombreFinal.append((letra + "").toUpperCase());
				} else {
					nombreFinal.append((letra + ""));
				}
				primera = false;
				if (abcMin.contains(letra + "") && letra != '-') {
					letra_A = letra_B;
					letra_B = letra;
				} else if (letra == '-') {
					primera = true;
					if (cantSimbol >= 1) {
						return "";
					}
					cantSimbol++;
				}
			}
			if (MainServidor.PARAM_CORREGIR_NOMBRE_JUGADOR) {
				nombre = nombreFinal.toString();
			}
		}
		return nombre;
	}
	
	public void addSetRapido(int id, String nombre, int icono, String data) {
		SetRapido set = new SetRapido(id, nombre, icono, data);
		_setsRapidos.put(set.getID(), set);
	}
	
	public void borrarSetRapido(int id) {
		_setsRapidos.remove(id);
	}
	
	public SetRapido getSetRapido(int id) {
		return _setsRapidos.get(id);
	}
	
	public String getSetsRapidos() {
		StringBuilder str = new StringBuilder();
		for (SetRapido s : _setsRapidos.values()) {
			if (str.length() > 0) {
				str.append("*");
			}
			str.append(s.getString());
		}
		return str.toString();
	}
	
	public void actualizarSetsRapidos(int oldID, int newID, byte oldPos, byte newPos) {
		boolean b = false;
		for (SetRapido set : _setsRapidos.values()) {
			b |= set.actualizarObjetos(oldID, newID, oldPos, newPos);
		}
		if (b) {
			GestorSalida.ENVIAR_Os_SETS_RAPIDOS(this);
		}
	}
	
	public Stats getStatsObjEquipados() {
		return _totalStats.getStatsObjetos();
	}
	
	@Override
	public void addKamas(long k, Personaje perso) {
		addKamas(k, false, true);
	}
	
	@Override
	public synchronized void addObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {}
	
	@Override
	public synchronized void remObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {}
	
	@Override
	public void cerrar(Personaje perso, String exito) {
		perso.cerrarVentanaExchange(exito);
	}
	
	public void botonOK(Personaje perso) {}
	
	@Override
	public String getArgsDialogo(String args) {
		if (args.isEmpty()) {
			return args;
		}
		return ";" + args.replace("[nombre]", getStringVar("nombre")).replace("[costoBanco]", getStringVar("costoBanco"))
		.replace("[lider]", Mundo.LIDER_RANKING).replace("[npcKamas]", MainServidor.KAMAS_RULETA_JALATO + "");
	}
}
