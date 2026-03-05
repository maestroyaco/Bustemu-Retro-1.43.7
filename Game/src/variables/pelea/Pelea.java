package variables.pelea;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.swing.Timer;
import servidor.ServidorSocket.AccionDeJuego;
import sprites.PreLuchador;
import variables.gremio.Gremio;
import variables.gremio.Recaudador;
import variables.hechizo.Buff;
import variables.hechizo.EfectoHechizo;
import variables.hechizo.EfectoHechizo.TipoDaño;
import variables.hechizo.Hechizo;
import variables.hechizo.HechizoLanzado;
import variables.hechizo.StatHechizo;
import variables.mapa.Celda;
import variables.mapa.Mapa;
import variables.mision.MisionObjetivoModelo;
import variables.mob.GrupoMob;
import variables.mob.MobGrado;
import variables.mob.MobGradoModelo;
import variables.mob.MobModelo;
import variables.mob.AparecerMobs.Aparecer;
import variables.mob.MobModelo.TipoGrupo;
import variables.montura.Montura;
import variables.objeto.Objeto;
import variables.objeto.ObjetoModelo;
import variables.objeto.ObjetoModelo.CAPACIDAD_STATS;
import variables.oficio.StatOficio;
import variables.pelea.Reto.EstReto;
import variables.personaje.Grupo;
import variables.personaje.GrupoKoliseo;
import variables.personaje.MisionPVP;
import variables.personaje.Personaje;
import variables.ranking.RankingKoliseo;
import variables.ranking.RankingPVP;
import variables.zotros.Accion;
import variables.zotros.Almanax;
import variables.zotros.Prisma;
import estaticos.Camino;
import estaticos.Condiciones;
import estaticos.Constantes;
import estaticos.Encriptador;
import estaticos.Formulas;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.Inteligencia;
import estaticos.Inteligencia.EstadoLanzHechizo;
import estaticos.MainServidor;
import estaticos.Mundo;
import estaticos.Mundo.Duo;

public class Pelea {
	public static StringBuilder LOG_COMBATES = new StringBuilder();
	private boolean _tacleado, _cerrado1, _soloGrupo1, _cerrado2, _soloGrupo2, _sinEspectador, _ayuda1, _ayuda2,
	_deshonor;
	private byte _byteColor2, _byteColor1, _cantMuertosReto2x1, _cantUltAfec, _fase, _tipo = -1, _vecesQuePasa,
	_alinPelea = Constantes.ALINEACION_NEUTRAL, _cantCAC, _nroOrdenLuc = -1;
	private short _id, _celdaID1, _celdaID2;
	private int _bonusEstrellas = -1;
	private int _tiempoHechizo, _idLuchInit1, _idLuchInit2;// _idMobReto,
	private int _ultimaInvoID = 0, _idUnicaAccion = -1;
	private long _tiempoPreparacion, _tiempoCombate, _tiempoTurno, _kamasRobadas, _expRobada;
	private String _tempAccion = "", _listadefensores = "";
	private StringBuilder _asesinos = new StringBuilder();
	private Mapa _mapaCopia, _mapaReal;
	private Luchador _luchInit1, _luchInit2, _luchadorDeTurno;
	private GrupoMob _mobGrupo;
	private Timer _rebootTurno;
	private final ConcurrentHashMap<Integer, Luchador> _equipo1 = new ConcurrentHashMap<Integer, Luchador>(),
	_equipo2 = new ConcurrentHashMap<Integer, Luchador>(), _espectadores = new ConcurrentHashMap<Integer, Luchador>();
	private final ArrayList<Celda> _celdasPos1 = new ArrayList<Celda>(), _celdasPos2 = new ArrayList<Celda>();
	private final List<Luchador> _inicioLuchEquipo1 = new ArrayList<>(9), _inicioLuchEquipo2 = new ArrayList<>(9),
	_ordenLuchadores = new ArrayList<>(), _listaMuertos = new ArrayList<>();
	private final List<Inteligencia> _IAs = new ArrayList<>();
	private final Map<Integer, Celda> _posInicialLuch = new TreeMap<Integer, Celda>();
	private CopyOnWriteArrayList<Glifo> _glifos;
	private CopyOnWriteArrayList<Trampa> _trampas;
	private List<Luchador> _capturadores, _domesticadores;
	private ConcurrentHashMap<Byte, Reto> _retos;
	private ArrayList<Objeto> _objetosRobados;
	private ArrayList<Accion> _acciones;
	private ArrayList<Botin> _posiblesBotinPelea;
	private boolean _salvarMobHeroico, _1vs1;
	private int _prospeccionEquipo;
	private int _luchMenorNivelReto, _ultimoElementoReto = Constantes.ELEMENTO_NULO, _ultimoMovedorIDReto;
	private TipoDaño _ultimoTipoDañoReto = TipoDaño.NULL;
	private Timer _timerPelea = new Timer(1, new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			actionListener();
		}
	});
	
	public Pelea(final short id, final Mapa mapa, final PreLuchador pre1, final PreLuchador pre2, short celda1,
	short celda2, final byte tipo, final GrupoMob grupoMob, String posPelea) {
		try {
			pre1.setPelea(this);
			pre2.setPelea(this);
			_fase = Constantes.PELEA_FASE_POSICION;
			_celdaID1 = celda1;
			_celdaID2 = celda2;
			if (_celdaID1 == _celdaID2) {
				short nCelda = -1;
				for (int i = 1; i < 5; i++) {
					for (final short c : Camino.celdasPorDistancia(mapa.getCelda(_celdaID2), mapa, i)) {
						Celda celda = mapa.getCelda(c);
						if (celda.getID() == _celdaID1 || !celda.esCaminable(false)) {
							continue;
						}
						nCelda = celda.getID();
						break;
					}
					if (nCelda != -1) {
						break;
					}
				}
				_celdaID2 = nCelda;
			}
			_tiempoCombate = _tiempoPreparacion = System.currentTimeMillis();
			_tipo = tipo;
			_id = id;
			_mapaCopia = mapa.copiarMapa(posPelea);
			_mapaReal = mapa;
			_alinPelea = _mapaReal.getSubArea().getAlineacion();
			_luchInit1 = new Luchador(this, pre1, false);
			_idLuchInit1 = _luchInit1.getID();
			_equipo1.put(_idLuchInit1, _luchInit1);
			_luchInit2 = new Luchador(this, pre2, false);
			_idLuchInit2 = _luchInit2.getID();
			_equipo2.put(_idLuchInit2, _luchInit2);
			if (_tipo != Constantes.PELEA_TIPO_DESAFIO) {
				startTimerInicioPelea();
				if (_tipo != Constantes.PELEA_TIPO_PVP && _luchInit1.getAlineacion() != Constantes.ALINEACION_NULL && _luchInit2
				.getAlineacion() != Constantes.ALINEACION_NULL && _luchInit1.getAlineacion() != _luchInit2.getAlineacion()) {
					_tipo = Constantes.PELEA_TIPO_PRISMA;
				}
			}
			GestorSalida.ENVIAR_GJK_UNIRSE_PELEA(this, 3, _fase, _tipo == Constantes.PELEA_TIPO_DESAFIO, true, false,
			_tipo == Constantes.PELEA_TIPO_DESAFIO ? 0 : ((MainServidor.SEGUNDOS_INICIO_PELEA * 1000) - 1500), _tipo);
			definirCeldasPos();
			if (pre1.getClass() == Personaje.class) {
				GestorSalida.ENVIAR_ILF_CANTIDAD_DE_VIDA((Personaje) pre1, 0);
				((Personaje) pre1).setCelda(null);
			}
			if (pre2.getClass() == Personaje.class) {
				GestorSalida.ENVIAR_ILF_CANTIDAD_DE_VIDA((Personaje) pre2, 0);
				((Personaje) pre2).setCelda(null);
			}
			GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(_mapaReal, _idLuchInit1);
			GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(_mapaReal, _idLuchInit2);
			_luchInit1.setCeldaPelea(getCeldaRandom(_celdasPos1));
			_luchInit2.setCeldaPelea(getCeldaRandom(_celdasPos2));
			_luchInit1.setEquipoBin((byte) 0);
			_luchInit2.setEquipoBin((byte) 1);
			if (_tipo != Constantes.PELEA_TIPO_PVM_NO_ESPADA) {
				GestorSalida.ENVIAR_Gc_MOSTRAR_ESPADA_EN_MAPA(_mapaReal, mostrarEspada());
				GestorSalida.ENVIAR_Gt_AGREGAR_NOMBRE_ESPADA(_mapaReal, _idLuchInit1, _luchInit1);
				GestorSalida.ENVIAR_Gt_AGREGAR_NOMBRE_ESPADA(_mapaReal, _idLuchInit2, _luchInit2);
			}
			GestorSalida.ENVIAR_GM_LUCHADORES_A_PELEA(this, 7, _mapaCopia);
			if (pre2.getClass() == Prisma.class) {
				final String str = _mapaReal.getID() + "|" + _mapaReal.getX() + "|" + _mapaReal.getY();
				for (final Personaje p : Mundo.getPersonajesEnLinea()) {
					if (p.getAlineacion() != pre2.getAlineacion()) {
						continue;
					}
					GestorSalida.ENVIAR_CA_MENSAJE_ATAQUE_PRISMA(p, str);
				}
			} else if (pre2.getClass() == Recaudador.class) {
				Recaudador recaudador = (Recaudador) pre2;
				recaudador.actualizarAtacantesDefensores();
				final String str = recaudador.mensajeDeAtaque();
				for (final Personaje p : recaudador.getGremio().getMiembros()) {
					if (p.enLinea()) {
						GestorSalida.ENVIAR_gA_MENSAJE_SOBRE_RECAUDADOR(p, str);
					}
				}
			}
			setGrupoMob(grupoMob);
			if (_luchInit1.getPersonaje() != null) {
				cargarMultiman(_luchInit1.getPersonaje());
			}
			if (_luchInit2.getPersonaje() != null) {
				cargarMultiman(_luchInit2.getPersonaje());
			}
			GestorSalida.ENVIAR_fL_LISTA_PELEAS_AL_MAPA(_mapaReal);
		} catch (final Exception e) {
			e.printStackTrace();
			pre1.setPelea(null);
			pre2.setPelea(null);
		}
	}
	
	public Pelea(final short id, final Mapa mapa, final GrupoKoliseo grupo1, final GrupoKoliseo grupo2, String posPelea) {
		_fase = Constantes.PELEA_FASE_POSICION;
		_tiempoCombate = _tiempoPreparacion = System.currentTimeMillis();
		_tipo = Constantes.PELEA_TIPO_KOLISEO;
		_id = id;
		_mapaCopia = mapa.copiarMapa(posPelea);
		_mapaReal = mapa;
		_alinPelea = _mapaReal.getSubArea().getAlineacion();
		boolean b = false;
		for (final Personaje perso : grupo1.getMiembros()) {
			GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(perso.getMapa(), perso.getID());
			perso.setCelda(null);
			if (perso.getMapa().getID() != mapa.getID()) {
				perso.teleportSinTodos(mapa.getID(), (short) 100);
			}
			perso.setPelea(this);
			Luchador l = new Luchador(this, perso, false);
			l.setEquipoBin((byte) 0);
			_equipo1.put(perso.getID(), l);
			if (!b) {
				_luchInit1 = l;
				_idLuchInit1 = _luchInit1.getID();
				b = true;
			}
			GestorSalida.ENVIAR_ILF_CANTIDAD_DE_VIDA(perso, 0);
		}
		b = false;
		for (final Personaje perso : grupo2.getMiembros()) {
			GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(perso.getMapa(), perso.getID());
			perso.setCelda(null);
			if (perso.getMapa().getID() != mapa.getID()) {
				perso.teleportSinTodos(mapa.getID(), (short) 100);
			}
			perso.setPelea(this);
			Luchador l = new Luchador(this, perso, false);
			l.setEquipoBin((byte) 1);
			_equipo2.put(perso.getID(), l);
			if (!b) {
				_luchInit2 = l;
				_idLuchInit2 = _luchInit2.getID();
				b = true;
			}
			GestorSalida.ENVIAR_ILF_CANTIDAD_DE_VIDA(perso, 0);
		}
		try {
			Thread.sleep(1500);
		} catch (final Exception e) {}
		GestorSalida.ENVIAR_GJK_UNIRSE_PELEA(this, 3, 2, false, true, false, (MainServidor.SEGUNDOS_INICIO_PELEA * 1000)
		- 1500, _tipo);
		startTimerInicioPelea();
		final List<Entry<Integer, Luchador>> equipo1 = new ArrayList<Entry<Integer, Luchador>>();
		equipo1.addAll(_equipo1.entrySet());
		final List<Entry<Integer, Luchador>> equipo2 = new ArrayList<Entry<Integer, Luchador>>();
		equipo2.addAll(_equipo2.entrySet());
		definirCeldasPos();
		final StringBuilder gm1 = new StringBuilder("GM");
		final StringBuilder gm2 = new StringBuilder("GM");
		for (final Entry<Integer, Luchador> entry : equipo1) {
			final Luchador lucha = entry.getValue();
			final Celda celdaRandom = getCeldaRandom(_celdasPos1);
			if (celdaRandom == null) {
				_equipo1.remove(lucha.getID());
				continue;
			}
			lucha.setCeldaPelea(celdaRandom);
			gm1.append("|+" + lucha.stringGM(0));
		}
		for (final Entry<Integer, Luchador> entry : equipo2) {
			final Luchador lucha = entry.getValue();
			final Celda celdaRandom = getCeldaRandom(_celdasPos2);
			if (celdaRandom == null) {
				_equipo2.remove(lucha.getID());
				continue;
			}
			lucha.setCeldaPelea(celdaRandom);
			gm2.append("|+" + lucha.stringGM(0));
		}
		for (final Personaje persos : grupo1.getMiembros()) {
			GestorSalida.enviarEnCola(persos, gm1.toString(), true);
			if (MainServidor.PARAM_VER_JUGADORES_KOLISEO) {
				GestorSalida.enviarEnCola(persos, gm2.toString(), true);
			}
		}
		if (MainServidor.MOSTRAR_ENVIOS) {
			System.out.println("GM LUCHADORES TEAM 1: KOLISEO>> " + gm1.toString());
		}
		for (final Personaje persos : grupo2.getMiembros()) {
			GestorSalida.enviarEnCola(persos, gm2.toString(), true);
			if (MainServidor.PARAM_VER_JUGADORES_KOLISEO) {
				GestorSalida.enviarEnCola(persos, gm1.toString(), true);
			}
		}
		GestorSalida.ENVIAR_fL_LISTA_PELEAS_AL_MAPA(_mapaReal);
		if (MainServidor.MOSTRAR_ENVIOS) {
			System.out.println("GM LUCHADORES TEAM 2: KOLISEO>> " + gm2.toString());
		}
	}
	
	public int getPosPelea(int color) {
		switch (color) {
			case 1 :
				return _celdasPos1.size();
			case 2 :
				return _celdasPos2.size();
		}
		return 0;
	}
	
	public int getProspeccionEquipo() {
		return _prospeccionEquipo;
	}
	
	public void setUltimoElementoReto(int t) {
		_ultimoElementoReto = t;
	}
	
	public int getUltimoElementoReto() {
		return _ultimoElementoReto;
	}
	
	public void setUltimoTipoDaño(TipoDaño t) {
		_ultimoTipoDañoReto = t;
	}
	
	private void addDropPelea(DropMob dropM) {
		if (_posiblesBotinPelea == null) {
			_posiblesBotinPelea = new ArrayList<>();
		}
		for (Botin dropP : _posiblesBotinPelea) {
			if (dropM.esIdentico(dropP.getDrop())) {
				dropP.addBotinMaximo(dropM.getMaximo());
				return;
			}
		}
		_posiblesBotinPelea.add(new Botin(dropM));
	}
	
	public short getID() {
		return _id;
	}
	
	public Mapa getMapaCopia() {
		return _mapaCopia;
	}
	
	public Mapa getMapaReal() {
		return _mapaReal;
	}
	
	public List<Luchador> getInicioLuchEquipo1() {
		return _inicioLuchEquipo1;
	}
	
	public List<Luchador> getInicioLuchEquipo2() {
		return _inicioLuchEquipo2;
	}
	
	// private void checkeaInicioPelea() {
	// switch (_estadoPelea) {
	// case Informacion.PELEA_ESTADO_INICIO :
	// case Informacion.PELEA_ESTADO_POSICION :
	// if (getTiempoFaltInicioPelea() <= 0) {
	// try {
	// iniciarPelea();
	// } catch (final Exception e) {
	// e.printStackTrace();
	// acaboPelea((byte) 2);
	// }
	// }
	// break;
	// }
	// }
	public long getTiempoFaltInicioPelea() {
		return Math.max(0, (MainServidor.SEGUNDOS_INICIO_PELEA * 1000) - (System.currentTimeMillis() - _tiempoPreparacion));
	}
	
	public void setDeshonor(boolean d) {
		_deshonor = d;
	}
	
	public void setUltAfec(final byte afec) {
		_cantUltAfec = afec;
	}
	
	public ConcurrentHashMap<Byte, Reto> getRetos() {
		return _retos;
	}
	
	public void setListaDefensores(final String str) {
		_listadefensores = str;
	}
	
	public Map<Integer, Celda> getPosInicial() {
		return _posInicialLuch;
	}
	
	// public void setIDMobReto(final int mob) {
	// _idMobReto = mob;
	// }
	//
	// public int getIDMobReto() {
	// return _idMobReto;
	// }
	private void setGrupoMob(GrupoMob gm) {
		setMobGrupo(gm);
		if (_mobGrupo == null) {
			return;
		}
		_mobGrupo.setPelea(this);
		if (_tipo == Constantes.PELEA_TIPO_PVM) {
			_bonusEstrellas = _mobGrupo.getBonusEstrellas();
		}
	}
	
	private synchronized void cargarMultiman(Personaje perso) {
		if (!MainServidor.PERMITIR_MULTIMAN_TIPO_COMBATE.contains(_tipo)) {
			return;
		}
		if (perso == null || perso.getCompañero() != null) {
			return;
		}
		Objeto obj = perso.getObjPosicion(Constantes.OBJETO_POS_COMPAÑERO);
		if (obj == null) {
			return;
		}
		int mobMultiman = 0;
		try {
			mobMultiman = Integer.parseInt(obj.getParamStatTexto(Constantes.STAT_MAS_COMPAÑERO, 3), 16);
		} catch (Exception e) {}
		MobModelo mobModelo = Mundo.getMobModelo(mobMultiman);
		if (mobModelo == null) {
			return;
		}
		int idMultiman = sigIDLuchadores();
		Personaje multiman = Personaje.crearMultiman(idMultiman, perso.getNivel(), perso.getTotalStatsPelea()
		.getTotalStatParaMostrar(Constantes.STAT_MAS_INICIATIVA), mobModelo);
		multiman.setMapa(_mapaCopia);
		multiman.setCelda(perso.getCelda());
		if (unirsePelea(multiman, perso.getID())) {
			perso.setCompañero(multiman);
			multiman.setCompañero(perso);
		}
	}
	
	public synchronized int sigIDLuchadores() {
		int id = 0;
		for (final Luchador luchador : luchadoresDeEquipo(3)) {
			if (luchador.getID() < id) {
				id = luchador.getID();
			}
		}
		return --id;
	}
	
	private void definirCeldasPos() {
		boolean b = Formulas.getRandomBoolean();
		_byteColor1 = (byte) (b ? 1 : 0);
		_byteColor2 = (byte) (_byteColor1 == 1 ? 0 : 1);
		switch (_mapaReal.getColorCeldasAtacante().toLowerCase()) {
			case "red" :
				_byteColor1 = 0;
				_byteColor2 = 1;
				break;
			case "blue" :
				_byteColor1 = 1;
				_byteColor2 = 2;
				break;
		}
		analizarPosiciones(_byteColor1, _celdasPos1);
		analizarPosiciones(_byteColor2, _celdasPos2);
		GestorSalida.ENVIAR_GP_POSICIONES_PELEA(this, 1, _mapaCopia.strCeldasPelea(), _byteColor1);
		GestorSalida.ENVIAR_GP_POSICIONES_PELEA(this, 2, _mapaCopia.strCeldasPelea(), _byteColor2);
	}
	
	private void actionListener() {
		switch (_fase) {
			case Constantes.PELEA_FASE_INICIO :
			case Constantes.PELEA_FASE_POSICION :
				try {
					iniciarPelea();
				} catch (final Exception e1) {
					acaboPelea((byte) 2);
				}
				break;
			case Constantes.PELEA_FASE_COMBATE :
				preFinTurno(null);
				break;
			case Constantes.PELEA_FASE_FINALIZADO :
				break;
		}
	}
	
	private void startTimerInicioPelea() {
		if (_timerPelea != null) {
			_timerPelea.setRepeats(false);
			_timerPelea.setInitialDelay(MainServidor.SEGUNDOS_INICIO_PELEA * 1000);
			_timerPelea.setDelay(MainServidor.SEGUNDOS_INICIO_PELEA * 1000);
			_timerPelea.restart();
		}
	}
	
	private void startTimerInicioTurno() {
		if (_timerPelea != null) {
			_timerPelea.stop();
			_timerPelea.setRepeats(false);
			_timerPelea.setInitialDelay(MainServidor.SEGUNDOS_TURNO_PELEA * 1000);
			_timerPelea.setDelay(MainServidor.SEGUNDOS_TURNO_PELEA * 1000);
			_timerPelea.restart();
		}
	}
	
	public synchronized Celda puedeUnirsePelea(PreLuchador pre, int idInitAUnir) {
		Personaje perso = null;
		try {
			if (pre.getClass() == Personaje.class) {
				perso = (Personaje) pre;
			}
		} catch (Exception e) {}
		if (_fase > Constantes.PELEA_FASE_POSICION) {
			GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'l');
			return null;
		}
		if (perso != null) {
			if (perso.getCalabozo() || perso.estaInmovil()) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1DONT_MOVE_TEMP");
				return null;
			}
			if (_mobGrupo != null && !Condiciones.validaCondiciones(perso, _mobGrupo.getCondUnirsePelea())) {
				GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'i');
				return null;
			}
		}
		if (_equipo1.containsKey(idInitAUnir)) {
			if (_equipo1.size() >= 8) {
				GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 't');
				return null;
			}
			if (_soloGrupo1) {
				final Grupo grupo = _luchInit1.getPersonaje().getGrupoParty();
				if (grupo != null && !grupo.getIDsPersos().contains(pre.getID())) {
					GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'f');
					return null;
				}
			}
			if (_cerrado1) {
				GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'f');
				return null;
			}
			if (_tipo == Constantes.PELEA_TIPO_PVP || _tipo == Constantes.PELEA_TIPO_PRISMA) {
				if (_luchInit1.getAlineacion() != pre.getAlineacion()) {
					GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'a');
					return null;
				}
				if (perso != null) {
					if (!MainServidor.PARAM_PERMITIR_MULTICUENTA_PELEA_PVP) {
						for (final Luchador luch : _equipo1.values()) {
							try {
								if (luch.getPersonaje() == null || luch.getPersonaje().getCuenta() == null) {
									continue;
								}
								if (luch.getPersonaje().getCuenta().getActualIP().equalsIgnoreCase(perso.getCuenta().getActualIP())) {
									GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'f');
									return null;
								}
							} catch (final Exception e) {}
						}
					}
				}
			} else if (_tipo == Constantes.PELEA_TIPO_RECAUDADOR) {
				if (perso != null) {
					if (perso.getGremio() != null && _luchInit2.getRecaudador().getGremio().getID() == perso.getGremio()
					.getID()) {
						GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'g');
						return null;
					}
					if (!MainServidor.PARAM_PERMITIR_MULTICUENTA_PELEA_RECAUDADOR) {
						for (final Luchador luch : _equipo1.values()) {
							try {
								if (luch.getPersonaje() == null || luch.getPersonaje().getCuenta() == null) {
									continue;
								}
								if (luch.getPersonaje().getCuenta().getActualIP().equalsIgnoreCase(perso.getCuenta().getActualIP())) {
									GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'f');
									return null;
								}
							} catch (final Exception e) {}
						}
					}
				}
			}
		} else if (_equipo2.containsKey(idInitAUnir)) {
			if (_equipo2.size() >= 8) {
				GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 't');
				return null;
			}
			if (_soloGrupo2) {
				final Grupo grupo = _luchInit2.getPersonaje().getGrupoParty();
				if (grupo != null && !grupo.getIDsPersos().contains(pre.getID())) {
					GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'f');
					return null;
				}
			}
			if (_cerrado2) {
				GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'f');
				return null;
			}
			if (_tipo == Constantes.PELEA_TIPO_PVP || _tipo == Constantes.PELEA_TIPO_PRISMA) {
				if (_luchInit2.getAlineacion() != pre.getAlineacion()) {
					GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'a');
					return null;
				}
				if (perso != null) {
					if (!MainServidor.PARAM_PERMITIR_MULTICUENTA_PELEA_PVP) {
						for (final Luchador luch : _equipo2.values()) {
							try {
								if (luch.getPersonaje() == null || luch.getPersonaje().getCuenta() == null) {
									continue;
								}
								if (luch.getPersonaje().getCuenta().getActualIP().equalsIgnoreCase(perso.getCuenta().getActualIP())) {
									GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'f');
									return null;
								}
							} catch (final Exception e) {}
						}
					}
				}
			} else if (_tipo == Constantes.PELEA_TIPO_RECAUDADOR) {
				if (perso != null) {
					if (perso.getGremio() != null && _luchInit2.getRecaudador().getGremio().getID() != perso.getGremio()
					.getID()) {
						GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'g');
						return null;
					}
					if (!MainServidor.PARAM_PERMITIR_MULTICUENTA_PELEA_RECAUDADOR) {
						for (final Luchador luch : _equipo2.values()) {
							try {
								if (luch.getPersonaje() == null || luch.getPersonaje().getCuenta() == null) {
									continue;
								}
								if (luch.getPersonaje().getCuenta().getActualIP().equalsIgnoreCase(perso.getCuenta().getActualIP())) {
									GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'f');
									return null;
								}
							} catch (final Exception e) {}
						}
					}
				}
			}
		}
		Celda celda = null;
		if (_equipo1.containsKey(idInitAUnir)) {
			celda = getCeldaRandom(_celdasPos1);
			if (celda == null) {
				return null;
			}
		} else if (_equipo2.containsKey(idInitAUnir)) {
			celda = getCeldaRandom(_celdasPos2);
			if (celda == null) {
				return null;
			}
		}
		if (perso != null && !perso.esMultiman()) {
			if (perso.getMapa().getID() != _mapaCopia.getID()) {
				switch (_tipo) {
					case Constantes.PELEA_TIPO_PRISMA :
					case Constantes.PELEA_TIPO_RECAUDADOR :
						if (perso.getPrePelea() == null || perso.getPrePelea().getMapaCopia().getID() != _mapaCopia.getID()) {
							perso.setPrePelea(this, idInitAUnir);
							perso.teleportSinTodos(_mapaCopia.getID(), celda.getID());
							return null;
						}
						break;
					default :
						GestorSalida.ENVIAR_GA903_ERROR_PELEA(perso, 'p');
						return null;
				}
			}
		}
		return celda;
	}
	
	public synchronized boolean unirsePelea(final PreLuchador pre, final int idInitAUnir) {
		Celda celda = puedeUnirsePelea(pre, idInitAUnir);
		if (celda == null) {
			return false;
		}
		Personaje perso = null;
		try {
			if (pre.getClass() == Personaje.class) {
				perso = (Personaje) pre;
			}
		} catch (Exception e) {}
		pre.setPelea(this);
		final Luchador luchadorAUnirse = new Luchador(this, pre, false);
		if (perso != null) {
			perso.setPrePelea(null, 0);
			if (perso.esMultiman()) {
				luchadorAUnirse.setIDReal(false);
			} else {
				GestorSalida.ENVIAR_ILF_CANTIDAD_DE_VIDA(perso, 0);
				GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(perso.getMapa(), perso.getID());
				perso.setCelda(null);
			}
			if (_tipo == Constantes.PELEA_TIPO_PVP || _tipo == Constantes.PELEA_TIPO_PRISMA) {
				perso.botonActDesacAlas('+');
			}
		}
		if (_equipo1.containsKey(idInitAUnir)) {
			luchadorAUnirse.setEquipoBin((byte) 0);
			_equipo1.put(pre.getID(), luchadorAUnirse);
		} else if (_equipo2.containsKey(idInitAUnir)) {
			luchadorAUnirse.setEquipoBin((byte) 1);
			_equipo2.put(pre.getID(), luchadorAUnirse);
		}
		luchadorAUnirse.setCeldaPelea(celda);
		GestorSalida.ENVIAR_Gt_AGREGAR_NOMBRE_ESPADA(_mapaReal, (luchadorAUnirse.getEquipoBin() == 0
		? _luchInit1
		: _luchInit2).getID(), luchadorAUnirse);
		seUnioAPelea(pre);
		if (_deshonor && perso != null) {
			if (perso.addDeshonor(1)) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "084;" + perso.getDeshonor());
			}
		}
		if (luchadorAUnirse.getPersonaje() != null) {
			cargarMultiman(luchadorAUnirse.getPersonaje());
		}
		return true;
	}
	
	public void seUnioAPelea(PreLuchador pre) {
		Personaje perso = null;
		try {
			if (pre.getClass() == Personaje.class) {
				perso = (Personaje) pre;
			}
		} catch (Exception e) {}
		if (perso != null && !perso.esMultiman()) {
			if (_tipo == Constantes.PELEA_TIPO_DESAFIO || _fase == Constantes.PELEA_FASE_COMBATE) {
				GestorSalida.ENVIAR_GJK_UNIRSE_PELEA(perso, _fase, true, true, false, 0, _tipo);
			} else {
				GestorSalida.ENVIAR_GJK_UNIRSE_PELEA(perso, _fase, false, true, false, Math.max(getTiempoFaltInicioPelea()
				- 1500, 0), _tipo);
			}
		}
		switch (_fase) {
			case Constantes.PELEA_FASE_POSICION :
				if (perso != null && !perso.esMultiman()) {
					if (_equipo1.containsKey(perso.getID())) {
						GestorSalida.ENVIAR_GP_POSICIONES_PELEA(perso, _mapaCopia.strCeldasPelea(), _byteColor1);
					} else if (_equipo2.containsKey(perso.getID())) {
						GestorSalida.ENVIAR_GP_POSICIONES_PELEA(perso, _mapaCopia.strCeldasPelea(), _byteColor2);
					}
					GestorSalida.ENVIAR_GM_LUCHADORES_A_PERSO(this, _mapaCopia, perso);
				}
				_luchInit2.getPreLuchador().actualizarAtacantesDefensores();
				break;
			case Constantes.PELEA_FASE_COMBATE :
				if (perso != null && !perso.esMultiman()) {
					GestorSalida.ENVIAR_GM_LUCHADORES_A_PERSO(this, _mapaCopia, perso);
					GestorSalida.ENVIAR_GS_EMPEZAR_COMBATE(perso);
					try {
						Thread.sleep(500);
					} catch (final Exception e) {}
					GestorSalida.ENVIAR_GTL_ORDEN_JUGADORES(perso, this);
					GestorSalida.ENVIAR_GTM_INFO_STATS_TODO_LUCHADORES_A_PERSO(perso, this);
				}
				break;
			default :
				return;
		}
		GestorSalida.ENVIAR_GM_JUGADOR_UNIRSE_PELEA(this, 7, getLuchadorPorID(pre.getID()));
	}
	
	public void unirseEspectador(final Personaje perso, final boolean siOsi) {
		if (_fase != Constantes.PELEA_FASE_COMBATE) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "157");
			return;
		}
		if (!siOsi && !perso.esIndetectable()) {
			if (perso.esFantasma() || perso.getPelea() != null) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1116");
				return;
			}
			if (_sinEspectador) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "157");
				return;
			}
		}
		perso.setPelea(this);
		Luchador espectador = new Luchador(this, perso, true);
		_espectadores.put(perso.getID(), espectador);
		if (!siOsi) {
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(this, 7, "036;" + perso.getNombre());
		} else {
			espectador.setEspectadorAdmin(true);
		}
		reconectandoMostrandoInfo(espectador, true);
		// actualizarNumTurnos(perso);
	}
	
	private void enviarRetosPersonaje(Personaje perso) {
		if (_retos == null) {
			return;
		}
		if (_tipo == Constantes.PELEA_TIPO_PVM || _tipo == Constantes.PELEA_TIPO_PVM_NO_ESPADA) {
			for (Reto reto : _retos.values()) {
				GestorSalida.ENVIAR_Gd_RETO_A_PERSONAJE(perso, reto.infoReto());
				// if (reto.getEstado() == EstReto.REALIZADO) {
				// GestorSalida.ENVIAR_GdaK_RETO_REALIZADO(perso, _id);
				// } else if (reto.getEstado() == EstReto.FALLADO) {
				// GestorSalida.ENVIAR_GdaO_RETO_FALLADO(perso, _id);
				// }
			}
		}
	}
	
	public void desconectarLuchador(final Personaje perso) {
		try {
			if (perso == null) {
				return;
			}
			final Luchador luchador = getLuchadorPorID(perso.getID());
			if (luchador == null) {
				return;
			}
			luchador.setDesconectado(true);
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(this, 7, "1182;" + luchador.getNombre() + "~" + luchador
			.getTurnosRestantes());
			if (!perso.esMultiman()) {
				desconectarLuchador(perso.getCompañero());
			}
			if (luchador.puedeJugar()) {
				luchador.setTurnosRestantes((byte) (luchador.getTurnosRestantes() - 1));
				pasarTurno(luchador);
			}
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("Exception desconectarLuchador " + e.toString());
			e.printStackTrace();
		}
	}
	
	public void reconectarLuchador(final Personaje perso) {
		if (perso == null) {
			return;
		}
		final Luchador luchador = getLuchadorPorID(perso.getID());
		if (luchador == null || !luchador.estaDesconectado()) {
			return;
		}
		perso.mostrarGrupo();
		reconectandoMostrandoInfo(luchador, false);
	}
	
	private void reconectandoMostrandoInfo(Luchador luchador, boolean espectador) {
		try {
			Personaje perso = luchador.getPersonaje();
			perso.setCelda(null);
			if (espectador) {
				GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(perso.getMapa(), perso.getID());
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {}
			if (_tipo == Constantes.PELEA_TIPO_DESAFIO || _fase == Constantes.PELEA_FASE_COMBATE) {
				GestorSalida.ENVIAR_GJK_UNIRSE_PELEA(perso, _fase, true, !espectador, espectador, 0, _tipo);
			} else {
				GestorSalida.ENVIAR_GJK_UNIRSE_PELEA(perso, _fase, false, !espectador, espectador, Math.max(
				getTiempoFaltInicioPelea() - 1500, 0), _tipo);
			}
			if (!espectador) {
				GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(this, 7, "1184;" + perso.getNombre());
				luchador.setDesconectado(false);
				if (!perso.esMultiman()) {
					Personaje compañero = perso.getCompañero();
					if (compañero != null) {
						GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(this, 7, "1184;" + compañero.getNombre());
						try {
							getLuchadorPorID(compañero.getID()).setDesconectado(false);
						} catch (Exception e) {}
					}
				}
			}
			switch (_fase) {
				case Constantes.PELEA_FASE_POSICION :
					if (perso != null) {
						if (_equipo1.containsKey(perso.getID())) {
							GestorSalida.ENVIAR_GP_POSICIONES_PELEA(perso, _mapaCopia.strCeldasPelea(), _byteColor1);
						} else if (_equipo2.containsKey(perso.getID())) {
							GestorSalida.ENVIAR_GP_POSICIONES_PELEA(perso, _mapaCopia.strCeldasPelea(), _byteColor2);
						}
						GestorSalida.ENVIAR_GM_LUCHADORES_A_PERSO(this, _mapaCopia, perso);
					}
					break;
				case Constantes.PELEA_FASE_COMBATE :
					final int tiempoRestante = (int) ((MainServidor.SEGUNDOS_TURNO_PELEA * 1000) - (System.currentTimeMillis()
					- _tiempoTurno));
					GestorSalida.ENVIAR_GM_LUCHADORES_A_PERSO(this, _mapaCopia, perso);
					GestorSalida.ENVIAR_GS_EMPEZAR_COMBATE(perso);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {}
					GestorSalida.ENVIAR_GTL_ORDEN_JUGADORES(perso, this);
					GestorSalida.ENVIAR_GTM_INFO_STATS_TODO_LUCHADORES_A_PERSO(perso, this);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {}
					GestorSalida.ENVIAR_GTS_INICIO_TURNO_PELEA(perso, _luchadorDeTurno.getID(), tiempoRestante);
					enviarRetosPersonaje(perso);
					mostrarBuffsDeTodosAPerso(perso);
					mostrarGlifos(luchador);
					mostrarTrampas(luchador);
			}
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("Exception mostrarTodaInfo " + e.toString());
			e.printStackTrace();
		}
	}
	
	public void mostrarGlifos(Luchador luchador) {
		if (_glifos != null) {
			for (Glifo glifo : _glifos) {
				GestorSalida.ENVIAR_GDZ_COLOREAR_ZONA_A_LUCHADOR(luchador, "+", glifo.getCelda().getID(), glifo.getTamaño(),
				glifo.getColor(), glifo.getForma());
			}
		}
	}
	
	public void mostrarTrampas(Luchador luchador) {
		if (_trampas != null) {
			for (Trampa trampa : _trampas) {
				if (!trampa.esInvisiblePara(luchador.getID())) {
					GestorSalida.ENVIAR_GDZ_COLOREAR_ZONA_A_LUCHADOR(luchador, "+", trampa.getCelda().getID(), trampa.getTamaño(),
					trampa.getColor(), ' ');
					boolean[] permisos = new boolean[16];
					int[] valores = new int[16];
					permisos[2] = true;
					permisos[0] = true;
					valores[2] = 25;
					valores[0] = 1;
					GestorSalida.ENVIAR_GDC_ACTUALIZAR_CELDA_A_LUCHADOR(luchador, trampa.getCelda().getID(), Encriptador
					.stringParaGDC(permisos, valores), false);
				}
			}
		}
	}
	
	public CopyOnWriteArrayList<Trampa> getTrampas() {
		return _trampas;
	}
	
	public void addTrampa(Trampa trampa) {
		if (_trampas == null) {
			_trampas = new CopyOnWriteArrayList<>();
		}
		if (!_trampas.contains(trampa))
			_trampas.add(trampa);
	}
	
	public void borrarTrampa(Trampa trampa) {
		if (_trampas == null) {
			return;
		}
		_trampas.remove(trampa);
	}
	
	public void addGlifo(Glifo glifo) {
		if (_glifos == null) {
			_glifos = new CopyOnWriteArrayList<>();
		}
		if (!_glifos.contains(glifo))
			_glifos.add(glifo);
	}
	
	public void borrarGlifo(Glifo glifo) {
		if (_glifos == null) {
			return;
		}
		_glifos.remove(glifo);
	}
	
	private Celda getCeldaRandom(final List<Celda> celdas) {
		if (celdas.isEmpty()) {
			return null;
		}
		final ArrayList<Celda> celdas2 = new ArrayList<Celda>();
		for (Celda c : celdas) {
			if (c == null || c.getPrimerLuchador() != null) {
				continue;
			}
			celdas2.add(c);
		}
		if (celdas2.isEmpty()) {
			return null;
		}
		return celdas2.get(Formulas.getRandomInt(0, celdas2.size() - 1));
	}
	
	private void analizarPosiciones(final byte color, ArrayList<Celda> celdas) {
		if (color == 0) {
			for (short s : _mapaCopia.getPosTeamRojo1()) {
				celdas.add(_mapaCopia.getCelda(s));
			}
		} else if (color == 1) {
			for (short s : _mapaCopia.getPosTeamAzul2()) {
				celdas.add(_mapaCopia.getCelda(s));
			}
		}
	}
	
	public ArrayList<Luchador> luchadoresDeEquipo(int equipos) {
		try {
			final ArrayList<Luchador> luchadores = new ArrayList<Luchador>();
			if (equipos - 4 >= 0) {
				luchadores.addAll(_espectadores.values());
				equipos -= 4;
			}
			if (equipos - 2 >= 0) {
				luchadores.addAll(_equipo2.values());
				equipos -= 2;
			}
			if (equipos - 1 >= 0) {
				luchadores.addAll(_equipo1.values());
			}
			return luchadores;
		} catch (final Exception e) {
			e.printStackTrace();
			return luchadoresDeEquipo(equipos);
		}
	}
	
	public int cantLuchDeEquipo(int equipos) {
		try {
			int luchadores = 0;
			if (equipos - 4 >= 0) {
				luchadores += _espectadores.size();
				equipos -= 4;
			}
			if (equipos - 2 >= 0) {
				luchadores += _equipo2.size();
				equipos -= 2;
			}
			if (equipos - 1 >= 0) {
				luchadores += _equipo1.size();
			}
			return luchadores;
		} catch (final Exception e) {
			return cantLuchDeEquipo(equipos);
		}
	}
	
	public synchronized void cambiarPosMultiman(final Personaje perso, final int idMultiman) {
		if (_fase != Constantes.PELEA_FASE_POSICION) {
			return;
		}
		final Luchador dueño = getLuchadorPorID(perso.getID());
		if (dueño == null || dueño.estaListo()) {
			return;
		}
		if (perso.getCompañero() == null) {
			return;
		}
		final Luchador multiman = getLuchadorPorID(perso.getCompañero().getID());
		if (multiman == null || multiman.getID() != idMultiman) {
			return;
		}
		Celda cMultiman = multiman.getCeldaPelea();
		Celda cDueño = dueño.getCeldaPelea();
		cMultiman.limpiarLuchadores();
		cDueño.limpiarLuchadores();
		dueño.setCeldaPelea(cMultiman);
		multiman.setCeldaPelea(cDueño);
		GestorSalida.ENVIAR_GIC_CAMBIAR_POS_PELEA(this, 3, _mapaCopia, multiman.getID(), cDueño.getID());
		GestorSalida.ENVIAR_GIC_CAMBIAR_POS_PELEA(this, 3, _mapaCopia, dueño.getID(), cMultiman.getID());
	}
	
	public synchronized void cambiarPosicion(final int id, final short celda) {
		if (_fase != Constantes.PELEA_FASE_POSICION) {
			return;
		}
		if (_mapaCopia.getCelda(celda) == null) {
			return;
		}
		final Luchador luchador = getLuchadorPorID(id);
		final int equipo = getParamMiEquipo(id);
		if (luchador == null || _mapaCopia.getCelda(celda).getPrimerLuchador() != null || luchador.estaListo()
		|| (equipo == 1 && !grupoCeldasContiene(_celdasPos1, celda)) || (equipo == 2 && !grupoCeldasContiene(_celdasPos2,
		celda))) {
			return;
		}
		luchador.getCeldaPelea().moverLuchadoresACelda(_mapaCopia.getCelda(celda));
		GestorSalida.ENVIAR_GIC_CAMBIAR_POS_PELEA(this, 3, _mapaCopia, id, celda);
	}
	
	private boolean grupoCeldasContiene(final ArrayList<Celda> celdas, final int celda) {
		for (Celda c : celdas) {
			if (c.getID() == celda) {
				return true;
			}
		}
		return false;
	}
	
	public void verificaTodosListos() {
		if (_tipo == Constantes.PELEA_TIPO_RECAUDADOR || _tipo == Constantes.PELEA_TIPO_PRISMA) {
			return;
		}
		boolean listo = true;
		for (final Luchador luch : _equipo1.values()) {
			if (luch.getPersonaje() == null || luch.getPersonaje().esMultiman()) {
				continue;
			}
			if (!luch.estaListo()) {
				listo = false;
				break;
			}
		}
		if (!listo) {
			return;
		}
		for (final Luchador luch : _equipo2.values()) {
			if (luch.getPersonaje() == null || luch.getPersonaje().esMultiman()) {
				continue;
			}
			if (!luch.estaListo()) {
				listo = false;
				break;
			}
		}
		if (!listo) {
			return;
		}
		iniciarPelea();
	}
	
	private void antesIniciarPelea() {
		try {
			_luchInit2.getPreLuchador().actualizarAtacantesDefensores();
			if (_tipo == Constantes.PELEA_TIPO_PVM_NO_ESPADA) {
				// vacio
			} else if (_tipo == Constantes.PELEA_TIPO_PVP) {
				// milicianos
				if (MainServidor.PARAM_PERMITIR_MILICIANOS_EN_PELEA && _luchInit1
				.getAlineacion() != Constantes.ALINEACION_NEUTRAL && _luchInit2.getAlineacion() == Constantes.ALINEACION_NEUTRAL
				&& _alinPelea == Constantes.ALINEACION_NEUTRAL) {
					final StringBuilder str = new StringBuilder();
					for (final Luchador l : _equipo1.values()) {
						if (l.estaRetirado()) {
							continue;
						}
						if (str.length() > 0) {
							str.append(";");
						}
						str.append(394 + "," + Constantes.getNivelMiliciano(l.getNivel()) + "," + Constantes.getNivelMiliciano(l
						.getNivel()));
					}
					GrupoMob gm = new GrupoMob(_mapaReal, (short) 1, str.toString(), TipoGrupo.SOLO_UNA_PELEA, "");
					for (MobGradoModelo mobG : gm.getMobs()) {
						MobGrado mob = mobG.invocarMob(sigIDLuchadores(), false, null);
						unirsePelea(mob, _idLuchInit2);
					}
				}
			} else if (_mobGrupo != null) {
				_mobGrupo.puedeTimerReaparecer(_mapaReal, _mobGrupo, Aparecer.INICIO_PELEA);
			}
			_inicioLuchEquipo1.addAll(luchadoresDeEquipo(1));
			_inicioLuchEquipo2.addAll(luchadoresDeEquipo(2));
			if (_tipo == Constantes.PELEA_TIPO_PVP) {
				for (Luchador luch : luchadoresDeEquipo(3)) {
					Personaje perso = luch.getPersonaje();
					if (perso == null || perso.esMultiman()) {
						continue;
					}
					if (Mundo.getRankingPVP(perso.getID()) == null) {
						final RankingPVP rank = new RankingPVP(perso.getID(), perso.getNombre(), 0, 0, perso.getAlineacion());
						Mundo.addRankingPVP(rank);
					}
				}
				if (_inicioLuchEquipo1.size() == 1 && _inicioLuchEquipo2.size() == 1) {
					Personaje p1 = _luchInit1.getPersonaje();
					Personaje p2 = _luchInit2.getPersonaje();
					if (p1 != null && p2 != null) {
						p1.addAgredirA(p2.getNombre());
						p2.addAgredidoPor(p1.getNombre());
						_1vs1 = true;
					}
				}
			}
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("Exception antesIniciarPelea - Mapa: " + _mapaCopia.getID() + " PeleaID: "
			+ _id + ", Exception: " + e.toString());
			e.printStackTrace();
		}
	}
	
	public int getIDLuchInit2() {
		if (_luchInit2 == null) {
			return -1;
		}
		return _luchInit2.getID();
	}
	
	private void iniciarPelea() {
		GestorSalida.ENVIAR_Gc_BORRAR_ESPADA_EN_MAPA(_mapaReal, _id);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {}
		antesIniciarPelea();
		acaboPelea((byte) 3);
		if (_fase > Constantes.PELEA_FASE_POSICION) {
			return;
		}
		_tiempoCombate = System.currentTimeMillis();
		_fase = Constantes.PELEA_FASE_COMBATE;
		GestorSalida.ENVIAR_fL_LISTA_PELEAS_AL_MAPA(_mapaReal);
		final StringBuilder gm = new StringBuilder("GM");
		for (final Luchador luchador : luchadoresDeEquipo(3)) {
			if (luchador.getID() < _ultimaInvoID) {
				_ultimaInvoID = luchador.getID();
			}
		}
		if (!MainServidor.PARAM_VER_JUGADORES_KOLISEO && _tipo == Constantes.PELEA_TIPO_KOLISEO) {
			final StringBuilder gm1 = new StringBuilder("GM");
			final StringBuilder gm2 = new StringBuilder("GM");
			for (final Luchador l : _equipo1.values()) {
				if (l != null)
					gm1.append("|+" + l.stringGM(0));
			}
			for (final Luchador l : _equipo2.values()) {
				if (l != null)
					gm2.append("|+" + l.stringGM(0));
			}
			for (final Luchador l : _equipo1.values()) {
				if (l != null)
					GestorSalida.enviarEnCola(l.getPersonaje(), gm2.toString(), true);
			}
			for (final Luchador l : _equipo2.values()) {
				if (l != null)
					GestorSalida.enviarEnCola(l.getPersonaje(), gm1.toString(), true);
			}
		} else if (_tipo != Constantes.PELEA_TIPO_KOLISEO) {
			if (gm.length() > 2) {
				for (final Luchador l : _equipo1.values()) {
					if (l != null) {
						GestorSalida.enviarEnCola(l.getPersonaje(), gm.toString(), true);
					}
				}
				for (final Luchador l : _equipo2.values()) {
					if (l != null) {
						GestorSalida.enviarEnCola(l.getPersonaje(), gm.toString(), true);
					}
				}
			}
		}
		try {
			Thread.sleep(500);
		} catch (final Exception e) {}
		GestorSalida.ENVIAR_GIC_UBICACION_LUCHADORES_INICIAR(this, 7);
		GestorSalida.ENVIAR_GS_EMPEZAR_COMBATE_EQUIPOS(this, 7);
		iniciarOrdenLuchadores();
		try {
			Thread.sleep(500);
		} catch (final Exception e) {}
		GestorSalida.ENVIAR_GTL_ORDEN_JUGADORES(this, 7);
		GestorSalida.ENVIAR_GTM_INFO_STATS_TODO_LUCHADORES_A_TODOS(this, 7, false);
		for (final Luchador luchador : luchadoresDeEquipo(3)) {
			if (luchador == null) {
				continue;
			}
			_posInicialLuch.put(luchador.getID(), luchador.getCeldaPelea());
			final Personaje perso = luchador.getPersonaje();
			if (perso == null) {
				continue;
			}
			if (perso.estaMontando()) {
				GestorSalida.ENVIAR_GA950_ACCION_PELEA_ESTADOS(this, 3, perso.getID(), Constantes.ESTADO_CABALGANDO, true);
			}
		}
		try {
			Thread.sleep(500);
		} catch (final Exception e) {}
		if (_tipo == Constantes.PELEA_TIPO_PVM || _tipo == Constantes.PELEA_TIPO_PVM_NO_ESPADA) {
			_retos = new ConcurrentHashMap<Byte, Reto>();
			final ArrayList<Byte> retosPosibles = new ArrayList<Byte>();
			for (byte retoID = 1; retoID <= 50; retoID++) {
				switch (retoID) {
					case 13 :// no tienen nada
					case 26 :
					case 27 :
						continue;
					default :
						if (Constantes.esRetoPosible1(retoID, this)) {
							retosPosibles.add(retoID);
						}
						break;
				}
			}
			byte retoID = retosPosibles.get(Formulas.getRandomInt(0, retosPosibles.size() - 1));
			_retos.put(retoID, Constantes.getReto(retoID, this));
			if (_mapaReal.esArena() || _mapaReal.esMazmorra()) {
				retoID = retosPosibles.get(Formulas.getRandomInt(0, retosPosibles.size() - 1));
				boolean repetir = true;
				while (repetir) {
					repetir = false;
					retoID = retosPosibles.get(Formulas.getRandomInt(0, retosPosibles.size() - 1));
					for (Entry<Byte, Reto> entry : _retos.entrySet()) {
						if (Constantes.esRetoPosible2(entry.getKey(), retoID) && !repetir) {
							repetir = false;
						} else {
							repetir = true;
						}
					}
				}
				_retos.put(retoID, Constantes.getReto(retoID, this));
			}
			ordenarRetos();
		}
		iniciarTurno();
	}
	
	private void ordenarRetos() {
		if (_retos == null) {
			return;
		}
		for (final Entry<Byte, Reto> entry : _retos.entrySet()) {
			final Reto reto = entry.getValue();
			final byte retoID = entry.getKey();
			EstReto exitoReto = reto.getEstado();
			if (exitoReto != Reto.EstReto.EN_ESPERA) {
				continue;
			}
			int nivel = 10000;
			switch (retoID) {
				case Constantes.RETO_LOS_PEQUEÑOS_ANTES :// los pequeños antes
					for (final Luchador luch : _equipo1.values()) {
						if (luch.getNivel() < nivel) {
							_luchMenorNivelReto = luch.getID();
							nivel = luch.getNivel();
						}
					}
					break;
				case Constantes.RETO_ELEGIDO_VOLUNTARIO :
				case Constantes.RETO_APLAZAMIENTO :
				case Constantes.RETO_ELITISTA :
				case Constantes.RETO_ASESINO_A_SUELDO :
					Luchador mob = null;
					try {
						ArrayList<Luchador> equipo2 = new ArrayList<>();
						for (Luchador luch : _equipo2.values()) {
							if (luch.estaMuerto() || luch.esInvocacion()) {
								continue;
							}
							equipo2.add(luch);
						}
						if (!equipo2.isEmpty()) {
							final int azar = Formulas.getRandomInt(0, equipo2.size() - 1);
							mob = equipo2.get(azar);
						}
					} catch (Exception e) {}
					if (mob != null) {
						GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(this, 5, mob.getID(), mob.getCeldaPelea().getID());
						reto.setMob(mob);
					}
					break;
			}
			GestorSalida.ENVIAR_Gd_RETO_A_LOS_LUCHADORES(this, reto.infoReto());
		}
	}
	
	//
	// private void actualizarNumTurnos(final Personaje perso) {
	// if (!Bustemu.PARAM_MOSTRAR_NRO_TURNOS) {
	// return;
	// }
	// try {
	// int i = 1;
	// final StringBuilder str = new StringBuilder();
	// for (final Luchador luch : _ordenJugadores) {
	// if (luch.estaMuerto()) {
	// continue;
	// }
	// if (i > 1) {
	// str.append(";");
	// }
	// str.append(luch.getID() + ",1" + i);
	// i++;
	// }
	// if (perso == null) {
	// GestorSalida.ENVIAR_GX_EXTRA_CLIP_PELEA(this, 7, "+|" + str.toString() + "|0|1");
	// } else {
	// GestorSalida.ENVIAR_GX_EXTRA_CLIP(perso, "+|" + str.toString() + "|0|1");
	// }
	// } catch (Exception e) {
	// try {
	// Thread.sleep(500);
	// } catch (final Exception e1) {}
	// actualizarNumTurnos(perso);
	// }
	// }
	//
	public boolean hechizoDisponible(final Luchador luchador, final int idHechizo) {
		boolean ver = false;
		if (luchador.getPersonaje().tieneHechizoID(idHechizo)) {
			ver = true;
			for (final HechizoLanzado hl : luchador.getHechizosLanzados()) {
				if (hl.getHechizoID() == idHechizo && hl.getSigLanzamiento() > 0) {
					ver = false;
				}
			}
		}
		return ver;
	}
	
	private void iniciarOrdenLuchadores() {
		int cantLuch = 0;
		final ArrayList<Duo<Integer, Luchador>> iniLuch = new ArrayList<Duo<Integer, Luchador>>();
		for (final Luchador luch : luchadoresDeEquipo(3)) {
			luch.resetPuntos();
			iniLuch.add(new Duo<Integer, Luchador>(Formulas.getIniciativa(luch.getTotalStats(), luch.getPorcPDV()), luch));
			cantLuch++;
		}
		int equipo1 = 0;
		int equipo2 = 0;
		int primero = 0;
		Luchador luchMaxIni = null;
		Luchador ultLuch = null;
		while (_ordenLuchadores.size() < cantLuch) {
			int tempIni = 0;
			for (final Duo<Integer, Luchador> entry : iniLuch) {
				if (_ordenLuchadores.contains(entry._segundo)) {
					continue;
				}
				if (primero == 0 || equipo1 == _equipo1.size() || equipo2 == _equipo2.size() || ultLuch
				.getEquipoBin() != entry._segundo.getEquipoBin()) {
					if (tempIni <= entry._primero) {
						luchMaxIni = entry._segundo;
						tempIni = entry._primero;
					}
				}
			}
			ultLuch = luchMaxIni;
			_ordenLuchadores.add(luchMaxIni);
			if (_equipo1.containsValue(luchMaxIni)) {
				equipo1++;
			} else {
				equipo2++;
			}
			primero++;
		}
	}
	
	public void botonBloquearMasJug(final int id) {
		if (_luchInit1 != null && _idLuchInit1 == id) {
			_cerrado1 = !_cerrado1;
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(_mapaReal, _cerrado1 ? '+' : '-', 'A', id);
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(this, 1, _cerrado1 ? "095" : "096");
		} else if (_luchInit2 != null && _idLuchInit2 == id) {
			_cerrado2 = !_cerrado2;
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(_mapaReal, _cerrado2 ? '+' : '-', 'A', id);
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(this, 2, _cerrado2 ? "095" : "096");
		}
	}
	
	public void botonSoloGrupo(final int idInit) {
		int expulsadoID;
		Luchador luch;
		if (_luchInit1 != null && _idLuchInit1 == idInit) {
			_soloGrupo1 = !_soloGrupo1;
			if (_soloGrupo1) {
				final ArrayList<Integer> lista = new ArrayList<Integer>();
				final ArrayList<Integer> expulsar = new ArrayList<Integer>();
				try {
					lista.addAll(_luchInit1.getPersonaje().getGrupoParty().getIDsPersos());
				} catch (Exception e) {}
				for (final Entry<Integer, Luchador> entry : _equipo1.entrySet()) {
					try {
						luch = entry.getValue();
						expulsadoID = entry.getKey();
						if (!lista.contains(expulsadoID)) {
							expulsar.add(expulsadoID);
							GestorSalida.ENVIAR_GM_BORRAR_LUCHADOR(this, expulsadoID, 3);
							luchadorSalirPelea(luch);
							GestorSalida.ENVIAR_GV_RESETEAR_PANTALLA_JUEGO(luch.getPersonaje());
							luch.getCeldaPelea().removerLuchador(luch);
							GestorSalida.ENVIAR_Gt_BORRAR_NOMBRE_ESPADA(_mapaReal, _idLuchInit1, luch);
						}
					} catch (Exception e) {}
				}
				for (final int ID : expulsar) {
					_equipo1.remove(ID);
				}
			}
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(_mapaReal, _soloGrupo1 ? '+' : '-', 'P', idInit);
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(this, 1, _soloGrupo1 ? "093" : "094");
		} else if (_luchInit2 != null && _idLuchInit2 == idInit) {
			_soloGrupo2 = !_soloGrupo2;
			if (_soloGrupo2) {
				final ArrayList<Integer> lista = new ArrayList<Integer>();
				final ArrayList<Integer> expulsar = new ArrayList<Integer>();
				try {
					lista.addAll(_luchInit2.getPersonaje().getGrupoParty().getIDsPersos());
				} catch (Exception e) {}
				for (final Entry<Integer, Luchador> entry : _equipo2.entrySet()) {
					try {
						luch = entry.getValue();
						expulsadoID = entry.getKey();
						if (!lista.contains(expulsadoID)) {
							expulsar.add(expulsadoID);
							GestorSalida.ENVIAR_GM_BORRAR_LUCHADOR(this, expulsadoID, 3);
							luchadorSalirPelea(luch);
							GestorSalida.ENVIAR_GV_RESETEAR_PANTALLA_JUEGO(luch.getPersonaje());
							luch.getCeldaPelea().removerLuchador(luch);
							GestorSalida.ENVIAR_Gt_BORRAR_NOMBRE_ESPADA(_mapaReal, _idLuchInit2, luch);
						}
					} catch (Exception e) {}
				}
				for (final int ID : expulsar) {
					_equipo2.remove(ID);
				}
			}
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(_mapaReal, _soloGrupo2 ? '+' : '-', 'P', idInit);
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(this, 2, _soloGrupo2 ? "095" : "096");
		}
	}
	
	public void botonBloquearEspect(final int id) {
		if (_luchInit1 != null && _idLuchInit1 == id || _luchInit2 != null && _idLuchInit2 == id) {
			_sinEspectador = !_sinEspectador;
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(_mapaReal, _sinEspectador ? '+' : '-', 'S', id);
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(this, 7, _sinEspectador ? "039" : "040");
		}
		if (_sinEspectador) {
			Map<Integer, Luchador> espectadores = new TreeMap<Integer, Luchador>();
			espectadores.putAll(_espectadores);
			for (final Luchador espectador : espectadores.values()) {
				try {
					if (espectador.esEspectadorAdmin()) {
						continue;
					}
					_espectadores.remove(espectador.getID());
					luchadorSalirPelea(espectador);
					GestorSalida.ENVIAR_GV_RESETEAR_PANTALLA_JUEGO(espectador.getPersonaje());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void botonAyuda(final int id) {
		if (_luchInit1 != null && _idLuchInit1 == id) {
			_ayuda1 = !_ayuda1;
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(_mapaReal, _ayuda1 ? '+' : '-', 'H', id);
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(this, 1, _ayuda1 ? "0103" : "0104");
		} else if (_luchInit2 != null && _idLuchInit2 == id) {
			_ayuda2 = !_ayuda2;
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(_mapaReal, _ayuda2 ? '+' : '-', 'H', id);
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(this, 2, _ayuda2 ? "0103" : "0104");
		}
	}
	
	public byte getFase() {
		return _fase;
	}
	
	public int getTipoPelea() {
		return _tipo;
	}
	
	public List<Luchador> getOrdenLuchadores() {
		return _ordenLuchadores;
	}
	
	public void finAccion(final Personaje perso) {
		final Luchador luchador = getLuchadorPorID(perso.getID());
		finAccion(luchador);
	}
	
	private void finAccion(final Luchador luchador) {
		if (luchador == null || luchador.getPersonaje() == null || !luchador.puedeJugar()) {
			return;
		}
		Personaje perso = luchador.getPersonaje();
		try {
			Thread.sleep(500);
		} catch (Exception e) {}
		GestorSalida.ENVIAR_GAF_FINALIZAR_ACCION(perso, luchador.getID(), _idUnicaAccion);
		if (_idUnicaAccion != -1) {
			_idUnicaAccion = -1;
			return;
		}
		GestorSalida.ENVIAR_GAC_LIMPIAR_ACCION(perso);
		_tempAccion = "";
		// GestorSalida.ENVIAR_GAC_LIMPIAR_ACCION(perso);
		if (!MainServidor.PARAM_JUGAR_RAPIDO)
			GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(perso, "El tempAccion fue limpiado, ahora puedes seguir jugando.");
	}
	
	public boolean esLuchInicioPelea(Luchador luch) {
		return _inicioLuchEquipo1.contains(luch) || _inicioLuchEquipo2.contains(luch);
	}
	
	// este metodo es recibido por el comando jugador .turno
	public synchronized void checkeaPasarTurno() {
		switch (_fase) {
			case Constantes.PELEA_FASE_COMBATE :
				if (_tiempoTurno <= 0) {
					break;
				}
				if ((System.currentTimeMillis() - _tiempoTurno) >= ((MainServidor.SEGUNDOS_TURNO_PELEA * 1000) - 5000)) {
					preFinTurno(null);
				}
				break;
		}
	}
	
	public synchronized void pasarTurnoBoton(final Personaje perso) {
		Luchador luchador = getLuchadorPorID(perso.getID());
		if (luchador == null) {
			return;
		}
		if (!luchador.puedeJugar()) {
			if (perso.getCompañero() == null) {
				return;
			}
			luchador = getLuchadorPorID(perso.getCompañero().getID());
			if (luchador == null || !luchador.puedeJugar()) {
				return;
			}
		}
		if (!_tempAccion.isEmpty()) {
			if (!MainServidor.PARAM_JUGAR_RAPIDO) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1REALIZANDO_TEMP_ACCION;" + _tempAccion);
			} else {
				finAccion(luchador);
			}
			return;
		}
		pasarTurno(luchador);
	}
	
	private void eliminarTimer() {
		if (_timerPelea != null) {
			_timerPelea.stop();
		}
		_timerPelea = null;
	}
	
	// la conmbinacion de GTF , GTM y GTS realiza la disminucion de turnos de los buffs
	private synchronized Object iniciarTurno() { // inicioturno
		// _cantTurnos++;
		acaboPelea((byte) 3);
		if (_fase == Constantes.PELEA_FASE_FINALIZADO) {
			return false;
		}
		startTimerInicioTurno();
		_ultimoTipoDañoReto = TipoDaño.NULL;
		_tiempoHechizo = 0;
		_idUnicaAccion = -1;
		_nroOrdenLuc++;
		_tempAccion = "";
		_cantUltAfec = 0;
		_cantMuertosReto2x1 = 0;
		_cantCAC = 0;
		_luchadorDeTurno = getLuchadorOrden();
		if (_luchadorDeTurno == null) {
			return false;
		}
		// GestorSalida.ENVIAR_GTR_TURNO_LISTO(this, 7, luchador.getID());//hace q retorne luego un
		// GT(sprite)
		// SI ESTA MUERTO SE VA DE FRENTE AL SIGUIENTE TURNO
		_luchadorDeTurno.setDistMinAtq(-1);
		_luchadorDeTurno.setPuedeJugar(true);
		if (_luchadorDeTurno.estaMuerto()) {
			return pasarTurno(_luchadorDeTurno);
		}
		// ACTIVA LOS GLIFOS
		if (_glifos != null) {
			for (final Glifo glifo : _glifos) {
				if (_fase == Constantes.PELEA_FASE_FINALIZADO) {
					return false;
				}
				if (glifo.getLanzador().getID() == _luchadorDeTurno.getID() && glifo.disminuirDuracion() == 0) {
					glifo.desaparecer();
				}
			}
		}
		if (_luchadorDeTurno.getPersonaje() != null) {
			if (_luchadorDeTurno.getPersonaje().getServidorSocket() != null) {
				_luchadorDeTurno.getPersonaje().getServidorSocket().limpiarAcciones(true);
			}
			Personaje compañero = _luchadorDeTurno.getPersonaje().getCompañero();
			if (compañero != null) {
				if (!_luchadorDeTurno.esIDReal()) {
					_luchadorDeTurno.setIDReal(true);
					getLuchadorPorID(compañero.getID()).setIDReal(false);
					GestorSalida.ENVIAR_AI_CAMBIAR_ID(_luchadorDeTurno.getPersonaje(), _luchadorDeTurno.getID());
					GestorSalida.ENVIAR_SL_LISTA_HECHIZOS(_luchadorDeTurno.getPersonaje());
				}
			}
		}
		try {
			Thread.sleep(250);
		} catch (final Exception e) {}
		GestorSalida.ENVIAR_GTS_INICIO_TURNO_PELEA(this, 7, _luchadorDeTurno.getID(), (MainServidor.SEGUNDOS_TURNO_PELEA
		* 1000));
		try {  
		    Thread.sleep(250);  
		} catch (final Exception e) {}  
		GestorSalida.ENVIAR_GTM_INFO_STATS_TODO_LUCHADORES_A_TODOS(this, 7, false);
		try {
			Thread.sleep(250);
		} catch (final Exception e) {}
		_luchadorDeTurno.aplicarBuffInicioTurno(this);
		_luchadorDeTurno.getBonusCastigo().clear();
		_luchadorDeTurno.actualizaHechizoLanzado();
		if (_luchadorDeTurno.estaMuerto()) {
			return false;
		}
		if (_luchadorDeTurno.getCeldaPelea().getGlifos() != null) {
			for (final Glifo glifo : _luchadorDeTurno.getCeldaPelea().getGlifos()) {
				if (_fase == Constantes.PELEA_FASE_FINALIZADO) {
					return false;
				}
				if (!glifo.esInicioTurno()) {
					continue;
				}
				glifo.activarGlifo(_luchadorDeTurno);
				try {
					Thread.sleep(500);
				} catch (final Exception e) {}
			}
		}
		if (_luchadorDeTurno.getPDVConBuff() <= 0) {
			addMuertosReturnFinalizo(_luchadorDeTurno, null);
			return false;
		}
		if (_luchadorDeTurno.estaMuerto()) {
			return false;
		}
		if (_luchadorDeTurno.tieneBuff(140) || _luchadorDeTurno.getComandoPasarTurno()) {
			// efecto de pasar turno
			return pasarTurno(_luchadorDeTurno);
		}
		if (_luchadorDeTurno.estaDesconectado()) {
			_luchadorDeTurno.setTurnosRestantes(_luchadorDeTurno.getTurnosRestantes() - 1);
			if (_luchadorDeTurno.getTurnosRestantes() <= 0) {
				_luchadorDeTurno.setDesconectado(false);
				retirarsePelea(_luchadorDeTurno.getID(), -1, true);
				// luchador.getPersonaje().desconectar();
			} else {
				GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(this, 7, "0162;" + _luchadorDeTurno.getNombre() + "~"
				+ _luchadorDeTurno.getTurnosRestantes());
				pasarTurno(_luchadorDeTurno);
			}
			return false;
		}
		if (MainServidor.MODO_DEBUG) {
			System.out.println("_tempLuchadorPA es " + _luchadorDeTurno.getPARestantes());
			System.out.println("_tempLuchadorPM es " + _luchadorDeTurno.getPMRestantes());
		}
		if (_luchadorDeTurno.getPersonaje() != null) {
			GestorSalida.ENVIAR_GAC_LIMPIAR_ACCION(_luchadorDeTurno.getPersonaje());
			GestorSalida.ENVIAR_As_STATS_DEL_PJ(_luchadorDeTurno.getPersonaje());
		}
		_tiempoTurno = System.currentTimeMillis();
		if ((_tipo == Constantes.PELEA_TIPO_PVM || _tipo == Constantes.PELEA_TIPO_PVM_NO_ESPADA) && _retos != null) {
			for (final Entry<Byte, Reto> entry : _retos.entrySet()) {
				Reto reto = entry.getValue();
				final byte retoID = entry.getKey();
				EstReto exitoReto = reto.getEstado();
				if (exitoReto != Reto.EstReto.EN_ESPERA) {
					continue;
				}
				if (_luchadorDeTurno.esNoIA()) {
					switch (retoID) {
						case Constantes.RETO_ESTATUA :
							_luchadorDeTurno.setIDCeldaInicioTurno(_luchadorDeTurno.getCeldaPelea().getID());
							break;
						case Constantes.RETO_VERSATIL :
							_luchadorDeTurno.getHechizosLanzadosReto().clear();
							break;
						case Constantes.RETO_IMPREVISIBLE :
							final ArrayList<Luchador> mobsVivos = new ArrayList<Luchador>();
							for (final Luchador luch : _inicioLuchEquipo2) {
								if (luch.estaMuerto()) {
									continue;
								}
								mobsVivos.add(luch);
							}
							if (!mobsVivos.isEmpty()) {
								final Luchador mob = mobsVivos.get(Formulas.getRandomInt(0, mobsVivos.size() - 1));
								if (mob != null) {
									reto.setMob(mob);
									GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(this, 5, mob.getID(), mob.getCeldaPelea().getID());
								}
							}
							break;
						case Constantes.RETO_CONTAMINACION :
							if (!_luchadorDeTurno.estaContaminado()) {
								break;
							}
							_luchadorDeTurno.addTurnosParaMorir();
							if (_luchadorDeTurno.getTurnosParaMorir() <= 3) {
								break;
							}
							exitoReto = Reto.EstReto.FALLADO;
							break;
					}
				} else {
					switch (retoID) {
						case Constantes.RETO_BLITZKRIEG :
							if (_luchadorDeTurno.getEquipoBin() == 1) {
								if (reto.getLuchMob() != null && _luchadorDeTurno.getID() == reto.getLuchMob().getID()) {
									exitoReto = Reto.EstReto.FALLADO;
								}
							}
							break;
					}
				}
				reto.setEstado(exitoReto);
			}
		}
		try {
			if (_luchadorDeTurno.getIA() != null) {
				_luchadorDeTurno.getIA().arrancar();
			}
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("Exception en inicioTurno Mapa: " + _mapaCopia.getID() + ", luchador: "
			+ (_luchadorDeTurno == null ? "null" : _luchadorDeTurno.getID()) + e.toString());
			e.printStackTrace();
		}
		return true;
	}
	
	public void mostrarObjetivoReto(byte retoID, Personaje perso) {
		if (_retos == null) {
			return;
		}
		for (Reto reto : _retos.values()) {
			if (reto.getID() != retoID || reto.getLuchMob() == null) {
				continue;
			}
			GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA(perso, reto.getLuchMob().getID(), reto.getLuchMob().getCeldaPelea().getID());
		}
	}
	
	private void mostrarBuffsDeTodosAPerso(final Personaje perso) {
		for (final Luchador luch : luchadoresDeEquipo(3)) {
			for (final Buff buff : luch.getBuffsPelea()) {
				if (!buff.getCondicionBuff().isEmpty()) {
					continue;
				}
				GestorSalida.ENVIAR_GA998_AGREGAR_BUFF(perso, getStrParaGA998(buff.getEfectoID(), luch.getID(), buff
				.getTurnosRestantes(false), buff.getHechizoID(), buff.getArgs()));
			}
		}
	}
	
	public void refrescarBuffsPorMuerte(final Luchador luchador) {
		for (final Luchador luch : luchadoresDeEquipo(3)) {
			if (luch.getID() == luchador.getID()) {
				continue;
			}
			luch.deshechizar(luchador, false);
		}
	}
	
	private int cantLuchIniMuertos(int equipo) {
		int i = 0;
		for (Luchador muerto : _listaMuertos) {
			if (equipo == 1) {
				if (_inicioLuchEquipo1.contains(muerto)) {
					i++;
				}
			} else if (equipo == 2) {
				if (_inicioLuchEquipo2.contains(muerto)) {
					i++;
				}
			}
		}
		return i;
	}
	
	public boolean addMuertosReturnFinalizo(final Luchador victima, Luchador asesino) {
		// agregar a muertos, agregarmuerto
		try {
			if (victima.estaMuerto() || _fase == Constantes.PELEA_FASE_FINALIZADO) {
				return false;
			}
			victima.setEstaMuerto(true);
			victima.setMuertoPor(asesino);
			victima.setPDV(1);
			final int victimaID = victima.getID();
			if (!victima.estaRetirado()) {
				if (!_listaMuertos.contains(victima)) {
					_listaMuertos.add(victima);
				}
			}
			if (_luchadorDeTurno == null) {
				return false;
			}
			for (Luchador l : luchadoresDeEquipo(3)) {
				if (l.getIA() != null) {
					l.getIA().forzarRefrescarMov();
					l.getIA().nullear();
				}
			}
			if ((_tipo == Constantes.PELEA_TIPO_PVM || _tipo == Constantes.PELEA_TIPO_PVM_NO_ESPADA) && _retos != null
			&& (asesino == null || asesino.esNoIA()) && victima.getEquipoBin() == 1) {
				// si la victima no es del equipo, y asesino es un jugador
				for (final Entry<Byte, Reto> entry : _retos.entrySet()) {
					final Reto reto = entry.getValue();
					final byte retoID = entry.getKey();
					EstReto exitoReto = reto.getEstado();
					if (exitoReto != Reto.EstReto.EN_ESPERA) {
						continue;
					}
					final int nivelVict = victima.getNivel();
					switch (retoID) {
						case Constantes.RETO_BLITZKRIEG :
							if (reto.getLuchMob() == victima) {
								reto.setMob(null);
							}
							break;
						case Constantes.RETO_MANOS_LIMPIAS :
							if (victima.esInvocacion()) {
								continue;
							}
							if (_ultimoTipoDañoReto == TipoDaño.NORMAL) {
								exitoReto = Reto.EstReto.FALLADO;
							}
							break;
						case Constantes.RETO_FOCALIZACION :
							if (reto.getLuchMob() != null && reto.getLuchMob().getID() == victimaID) {
								reto.setMob(null);
							}
							break;
						case Constantes.RETO_ELEGIDO_VOLUNTARIO :// elegido voluntario
							if (victima.esInvocacion()) {
								break;
							}
							if (cantLuchIniMuertos(2) > 0) {
								if (reto.getLuchMob() != null && reto.getLuchMob().getID() == victima.getID()) {
									exitoReto = Reto.EstReto.REALIZADO;
								} else {
									exitoReto = Reto.EstReto.FALLADO;
								}
							}
							break;
						case Constantes.RETO_APLAZAMIENTO :// aplazamiento
							if (victima.esInvocacion()) {
								break;
							}
							if (reto.getLuchMob() != null && reto.getLuchMob().getID() == victima.getID()) {
								if (cantLuchIniMuertos(2) == _inicioLuchEquipo2.size()) {
									exitoReto = Reto.EstReto.REALIZADO;
								} else {
									exitoReto = Reto.EstReto.FALLADO;
								}
							}
							break;
						case Constantes.RETO_CRUEL :// cruel
							if (victima.esInvocacion()) {
								continue;
							}
							for (Luchador e : _equipo2.values()) {
								if (e.esInvocacion() || e.estaMuerto()) {
									continue;
								}
								if (e.getNivel() < nivelVict) {
									exitoReto = Reto.EstReto.FALLADO;
									break;
								}
							}
							break;
						case Constantes.RETO_ORDENADO :// ordenado
							if (victima.esInvocacion()) {
								continue;
							}
							for (Luchador e : _equipo2.values()) {
								if (e.esInvocacion() || e.estaMuerto()) {
									continue;
								}
								if (e.getNivel() > nivelVict) {
									exitoReto = Reto.EstReto.FALLADO;
									break;
								}
							}
							break;
						case Constantes.RETO_NI_PIAS_NI_SUMISAS :// ni pias ni sumisas
							if (_luchadorDeTurno.getPersonaje().getSexo() == Constantes.SEXO_MASCULINO) {
								exitoReto = Reto.EstReto.FALLADO;
							}
							break;
						case Constantes.RETO_NI_PIOS_NI_SUMISOS : // ni pios ni sumisos
							if (_luchadorDeTurno.getPersonaje().getSexo() == Constantes.SEXO_FEMENINO) {
								exitoReto = Reto.EstReto.FALLADO;
							}
							break;
						case Constantes.RETO_LOS_PEQUEÑOS_ANTES :// los pequeños antes
							if (_luchadorDeTurno.getID() != _luchMenorNivelReto) {
								exitoReto = Reto.EstReto.FALLADO;
							}
							break;
						case Constantes.RETO_ELITISTA :// elitista
							if (victima.esInvocacion()) {
								break;
							}
							if (reto.getLuchMob() != null && reto.getLuchMob().getID() == victimaID) {
								exitoReto = Reto.EstReto.REALIZADO;
							} else {
								exitoReto = Reto.EstReto.FALLADO;
							}
							break;
						case Constantes.RETO_ASESINO_A_SUELDO :// asesino a sueldo
							if (victima.esInvocacion()) {
								break;
							}
							if (reto.getLuchMob() != null && reto.getLuchMob().getID() == victimaID) {
								Luchador mob = null;
								ArrayList<Luchador> equipo2 = new ArrayList<>();
								for (Luchador luch : _equipo2.values()) {
									if (luch.estaMuerto() || luch.esInvocacion()) {
										continue;
									}
									equipo2.add(luch);
								}
								if (!equipo2.isEmpty()) {
									final int azar = Formulas.getRandomInt(0, equipo2.size() - 1);
									mob = equipo2.get(azar);
								}
								if (mob != null) {
									GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(this, 5, mob.getID(), mob.getCeldaPelea().getID());
									reto.setMob(mob);
								}
							} else {
								exitoReto = Reto.EstReto.FALLADO;
							}
							break;
						case Constantes.RETO_EL_DOS_POR_UNO : // el dos por uno
							_cantMuertosReto2x1++;
							break;
						case Constantes.RETO_REPARTO :// reparto
						case Constantes.RETO_CADA_UNO_CON_SU_MONSTRUO :// cada uno con su monstruo
							if (victima.esInvocacion()) {
								continue;
							}
							_luchadorDeTurno.getMobsAsesinadosReto().add(victimaID);
							break;
					}
					reto.setEstado(exitoReto);
				}
			}
			GestorSalida.ENVIAR_GA103_JUGADOR_MUERTO(this, 7, victimaID);
			_tiempoHechizo += EfectoHechizo.TIEMPO_POR_LUCHADOR_MUERTO;
			if (victima.getTransportando() != null) {
				quitarTransportados(victima);
			} else if (victima.getPortador() != null) {
				quitarTransportados(victima.getPortador());
			}
			victima.getCeldaPelea().removerLuchador(victima);
			final TreeMap<Integer, Luchador> team = new TreeMap<Integer, Luchador>();
			if (victima.getEquipoBin() == 0) {
				team.putAll(_equipo1);
			} else if (victima.getEquipoBin() == 1) {
				team.putAll(_equipo2);
			}
			for (final Luchador luch : team.values()) {
				if (luch.estaMuerto() || luch.estaRetirado() || luch.getInvocador() == null || luch.getInvocador()
				.getID() != victimaID) {
					continue;
				}
				addMuertosReturnFinalizo(luch, asesino);
			}
			if (victima.esInvocacion() && !victima.esEstatico()) {
				victima.getInvocador().addNroInvocaciones(-1);
				if (!_ordenLuchadores.isEmpty()) {
					final int index = _ordenLuchadores.indexOf(victima);
					if (index > -1) {
						if (_nroOrdenLuc >= index && _nroOrdenLuc > 0) {
							_nroOrdenLuc--;
						}
						_ordenLuchadores.remove(victima);
					}
					if (_nroOrdenLuc < 0) {
						return false;
					}
					if (_equipo1.containsKey(victimaID)) {
						_equipo1.remove(victimaID);// expulsa invocacion
					} else if (_equipo2.containsKey(victimaID)) {
						_equipo2.remove(victimaID);
					}
					GestorSalida.ENVIAR_GA_ACCION_PELEA(this, 7, 999, victimaID + "", stringOrdenJugadores());
					try {
						Thread.sleep(500);
					} catch (final Exception e) {}
				}
			}
			if (_glifos != null) {
				for (final Glifo glifo : _glifos) {
					if (glifo.getLanzador().getID() == victimaID) {
						glifo.desaparecer();
					}
				}
			}
			if (_trampas != null) {
				for (final Trampa trampa : _trampas) {
					if (trampa.getLanzador().getID() == victimaID) {
						trampa.activarTrampa(null);
					}
				}
			}
			refrescarBuffsPorMuerte(victima);
			if (victima.getRecaudador() != null || victima.getPrisma() != null) {
				acaboPelea((byte) 2);
				return true;
			} else if (cantLuchIniMuertos(1) == _inicioLuchEquipo1.size() || cantLuchIniMuertos(2) == _inicioLuchEquipo2
			.size()) {
				acaboPelea(cantLuchIniMuertos(1) == _inicioLuchEquipo1.size() ? (byte) 1 : (byte) 2);
				return true;
			}
			comprobarPasarTurnoDespuesMuerte(victima);
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("Exception en addMuertosReturnFinalizo " + e.toString());
			e.printStackTrace();
		}
		return false;
	}
	
	private void comprobarPasarTurnoDespuesMuerte(Luchador victima) {
		if (victima.puedeJugar() && victima.estaMuerto()) {
			preFinTurno(victima);
		}
	}
	
	public void quitarTransportados(Luchador portador) {
		if (portador == null) {
			return;
		}
		// if (nuevaCelda != null && reubicar != null){
		// reubicar.getCeldaPelea().removerLuchador(reubicar);
		// reubicar.setCeldaPelea(nuevaCelda);
		// }
		final Luchador transportado = portador.getTransportando();
		portador.setEstado(Constantes.ESTADO_PORTADOR, 0);
		portador.setTransportando(null);
		if (transportado != null && !transportado.estaMuerto()) {
			transportado.setEstado(Constantes.ESTADO_TRANSPORTADO, 0);
			transportado.setTransportadoPor(null);
		}
		try {
			Thread.sleep(500);
		} catch (final Exception e) {}
	}
	
	public void setTempAccion(final String str) {
		_tempAccion = str;
		if (_tempAccion.equalsIgnoreCase("pasar")) {
			pasarTurno(null);
		}
	}
	
	public int getPMLuchadorTurno() {
		return _luchadorDeTurno.getPMRestantes();
	}
	
	// private void tiempoParaPasarTurno() {
	// if (_fase == Constantes.PELEA_FASE_COMBATE && System.currentTimeMillis() - _ultQping > 500 &&
	// _cantTurnos > 1) {
	// _ultQping = System.currentTimeMillis();
	// checkeaPasarTurno(getLuchadorTurno());
	// }
	// }
	private void preFinTurno(final Luchador victima) {
		if (_vecesQuePasa >= 10) {
			_tempAccion = "";
		}
		// System.out.println("victima preFinTurno " + (victima == null ? victima : victima.getID()));
		if (!_tempAccion.isEmpty() || (System.currentTimeMillis() < (_tiempoTurno + _tiempoHechizo))) {
			if (_rebootTurno == null) {
				_rebootTurno = new Timer(500, new ActionListener() {
					public void actionPerformed(final ActionEvent e) {
						_vecesQuePasa++;
						preFinTurno(victima);
					}
				});
			}
			_rebootTurno.setRepeats(false);
			_rebootTurno.restart();
		} else {
			pasarTurno(victima);
		}
	}
	
	public synchronized String pasarTurno(Luchador pasoTurno) {
		if (_fase == Constantes.PELEA_FASE_FINALIZADO) {
			return "Pelea finalizada";
		}
		if (pasoTurno != null) {
			if (_luchadorDeTurno.getID() != pasoTurno.getID()) {
				return "No es mismo luchador de turno";
			}
		} else {
			if ((System.currentTimeMillis() - _tiempoTurno) < ((MainServidor.SEGUNDOS_TURNO_PELEA * 1000) - 5000)) {
				return "Evitar doble pasar turno";
			}
		}
		if (_rebootTurno != null) {
			_rebootTurno.stop();
		}
		if (!_luchadorDeTurno.puedeJugar()) {
			return "El luchador  " + _luchadorDeTurno.getNombre() + " no puede jugar";
		}
		if (!_luchadorDeTurno.estaMuerto()) {
			// esto es mas q todo para las sangres de lo multimans o sea no afecta a las IAs
			if (_luchadorDeTurno.esMultiman()) {
				if (_luchadorDeTurno.getHechizos() != null) {
					for (StatHechizo sh : _luchadorDeTurno.getHechizos().values()) {
						if (sh == null) {
							continue;
						}
						if (sh.esAutomaticoAlFinalTurno()) {
							intentarLanzarHechizo(_luchadorDeTurno, sh, _luchadorDeTurno.getCeldaPelea(), true);
						}
					}
				}
			}
		}
		_tempAccion = "";
		_vecesQuePasa = 0;
		_tiempoTurno = System.currentTimeMillis();
		_luchadorDeTurno.setPuedeJugar(false);
		_luchadorDeTurno.setUltimoElementoDaño(Constantes.ELEMENTO_NULO);
		try {
			if (!_luchadorDeTurno.estaMuerto()) {
				GestorSalida.ENVIAR_GTF_FIN_DE_TURNO(this, 7, _luchadorDeTurno.getID());
				try {
					Thread.sleep(250);
				} catch (final Exception e) {}
				if ((_tipo == Constantes.PELEA_TIPO_PVM || _tipo == Constantes.PELEA_TIPO_PVM_NO_ESPADA) && _retos != null
				&& _luchadorDeTurno.esNoIA()) {
					for (final Entry<Byte, Reto> entry : _retos.entrySet()) {
						Reto reto = entry.getValue();
						final byte retoID = entry.getKey();
						EstReto exitoReto = reto.getEstado();
						if (exitoReto != Reto.EstReto.EN_ESPERA) {
							continue;
						}
						switch (retoID) {
							case Constantes.RETO_EL_DOS_POR_UNO : // el dos por uno
								if (_cantMuertosReto2x1 > 0 && _cantMuertosReto2x1 != 2) {
									exitoReto = Reto.EstReto.FALLADO;
								}
								break;
							case Constantes.RETO_ZOMBI :
								if (_luchadorDeTurno.getPMUsados() != 0) {
									break;
								}
								exitoReto = Reto.EstReto.FALLADO;
								break;
							case Constantes.RETO_ESTATUA :
								if (_luchadorDeTurno.getIDCeldaInicioTurno() == _luchadorDeTurno.getCeldaPelea().getID()) {
									break;
								}
								exitoReto = Reto.EstReto.FALLADO;
								break;
							case Constantes.RETO_JARDINERO :
								if (!hechizoDisponible(_luchadorDeTurno, 367)) {// zanahowia
									break;
								}
								exitoReto = Reto.EstReto.FALLADO;
								break;
							case Constantes.RETO_NOMADA :
								if (_luchadorDeTurno.getPMRestantes() <= 0) {
									break;
								}
								exitoReto = Reto.EstReto.FALLADO;
								break;
							case Constantes.RETO_SEPULTURERO :
								if (!hechizoDisponible(_luchadorDeTurno, 373)) {// invocacion chaferloko
									break;
								}
								exitoReto = Reto.EstReto.FALLADO;
								break;
							case Constantes.RETO_CASINO_REAL :
								if (!hechizoDisponible(_luchadorDeTurno, 101)) {// ruleta
									break;
								}
								exitoReto = Reto.EstReto.FALLADO;
								break;
							case Constantes.RETO_ARACNOFILO :
								if (!hechizoDisponible(_luchadorDeTurno, 370)) {
									break;
								}
								exitoReto = Reto.EstReto.FALLADO;
								break;
							case Constantes.RETO_ENTOMOLOGO :
								if (!hechizoDisponible(_luchadorDeTurno, 311)) {// escarainvoc
									break;
								}
								exitoReto = Reto.EstReto.FALLADO;
								break;
							case Constantes.RETO_AUDAZ :
								if (Camino.hayAlrededorAmigoOEnemigo(_mapaCopia, _luchadorDeTurno, false, false)) {
									break;
								}
								exitoReto = Reto.EstReto.FALLADO;
								break;
							case Constantes.RETO_PEGAJOSO :
								if (Camino.hayAlrededorAmigoOEnemigo(_mapaCopia, _luchadorDeTurno, true, false)) {
									break;
								}
								exitoReto = Reto.EstReto.FALLADO;
								break;
							case Constantes.RETO_ANACORETA :
								if (!Camino.hayAlrededorAmigoOEnemigo(_mapaCopia, _luchadorDeTurno, true, false)) {
									break;
								}
								exitoReto = Reto.EstReto.FALLADO;
								break;
							case Constantes.RETO_PUSILANIME :
								if (!Camino.hayAlrededorAmigoOEnemigo(_mapaCopia, _luchadorDeTurno, false, false)) {
									break;
								}
								exitoReto = Reto.EstReto.FALLADO;
								break;
							case Constantes.RETO_IMPETUOSO :
								if (_luchadorDeTurno.getPARestantes() <= 0) {
									break;
								}
								exitoReto = Reto.EstReto.FALLADO;
								break;
						}
						reto.setEstado(exitoReto);
					}
				}
				Luchador luchTurno = _luchadorDeTurno;
				EfectoHechizo.buffFinTurno(luchTurno);
				if (!luchTurno.estaMuerto()) {
					if (luchTurno.getCeldaPelea().getGlifos() != null) {
						for (final Glifo glifo : luchTurno.getCeldaPelea().getGlifos()) {
							if (_fase == Constantes.PELEA_FASE_FINALIZADO) {
								return "Se finalizó la pelea en glifos";
							}
							if (luchTurno.estaMuerto()) {
								continue;
							}
							if (glifo.esInicioTurno()) {
								continue;
							}
							glifo.activarGlifo(luchTurno);
							if (luchTurno.getPDVConBuff() <= 0) {
								addMuertosReturnFinalizo(luchTurno, glifo.getLanzador());
							}
						}
					}
				}
				if (!luchTurno.estaMuerto()) {
					// Disminuye los estados y buffs
					luchTurno.disminuirBuffsPelea();
				}
				if (luchTurno.getPersonaje() != null && luchTurno.getPersonaje().enLinea()) {
					GestorSalida.ENVIAR_As_STATS_DEL_PJ(luchTurno.getPersonaje());
				}
				luchTurno.resetPuntos();
				GestorSalida.ENVIAR_GTM_INFO_STATS_TODO_LUCHADORES_A_TODOS(this, 7, false);
				Thread.sleep(250);
			}
			iniciarTurno();
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("Excepcion de fin de turno " + e.toString());
			e.printStackTrace();
			pasarTurno(null);
		}
		return "Return GOOD !!";
	}
	
	public String intentarMoverse(final Luchador movedor, final String path, final int idUnica, final AccionDeJuego AJ) {
		if (movedor == null || !movedor.puedeJugar()) {
			return "no";
		}
		_tacleado = false;
		if (_luchadorDeTurno == null || !_tempAccion.isEmpty() || _fase != Constantes.PELEA_FASE_COMBATE || path
		.isEmpty()) {
			if (movedor.getPersonaje() != null) {
				if (path.isEmpty()) {
					GestorSalida.ENVIAR_Im_INFORMACION(movedor.getPersonaje(), "1102");
				} else if (!_tempAccion.isEmpty()) {
					if (MainServidor.PARAM_JUGAR_RAPIDO) {
						finAccion(movedor);
					} else {
						GestorSalida.ENVIAR_Im_INFORMACION(movedor.getPersonaje(), "1REALIZANDO_TEMP_ACCION;" + _tempAccion);
					}
				}
			}
			return "no";
		}
		// movedor.tieneEstado(55) poner el estado inmovible
		if (_luchadorDeTurno == null || _luchadorDeTurno.getID() != movedor.getID()) {
			if (movedor.getPersonaje() != null) {
				GestorSalida.ENVIAR_Im_INFORMACION(movedor.getPersonaje(), "1NO_ES_TU_TURNO");
			}
			return "no";
		}
		final Personaje persoM = movedor.getPersonaje();
		if (movedor.tieneEstado(Constantes.ESTADO_ARRAIGADO) || movedor.esInvisible(0)) {
			// no es tacleado
		} else {
			// esto es para ser tacleado
			int porcHuida = 100;
			int agiTac = 0;
			boolean paso = false;
			final ArrayList<Integer> tacleadores = new ArrayList<Integer>();
			for (int i = 0; i < 4; i++) {
				final Luchador tacleador = Camino.getEnemigoAlrededor(movedor.getCeldaPelea().getID(), _mapaCopia, tacleadores,
				movedor.getEquipoBin());
				if (tacleador != null) {
					if (!tacleador.esEstatico() && !tacleador.esInvisible(0)) {
						tacleadores.add(tacleador.getID());
						// no puede placar con estado arraigado
						if (!tacleador.tieneEstado(Constantes.ESTADO_ARRAIGADO)) {
							paso = true;
							if (MainServidor.PARAM_FORMULA_TIPO_OFICIAL) {
								agiTac += tacleador.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_AGILIDAD) + (tacleador
								.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_PLACAJE) * 10);
							} else {
								porcHuida = EfectoHechizo.getPorcHuida(movedor, tacleador) * porcHuida / 100;
							}
						}
					}
				} else {
					break;
				}
			}
			if (paso) {
				if (MainServidor.PARAM_FORMULA_TIPO_OFICIAL) {
					int agiMov = movedor.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_AGILIDAD) + (movedor
					.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_HUIDA) * 10);
					agiTac = Math.max(0, agiTac);
					agiMov = Math.max(0, agiMov);
					porcHuida = EfectoHechizo.getPorcHuida2(agiMov, agiTac);
				}
				final int random = Formulas.getRandomInt(1, 100);
				if (MainServidor.PARAM_MOSTRAR_PROBABILIDAD_TACLEO) {
					GestorSalida.ENVIAR_cs_CHAT_MENSAJE_A_PELEA(this, "% FUITE: <b>" + porcHuida + "</b>, RANDOM: <b>" + random
					+ "</b>", Constantes.COLOR_NARANJA);
				}
				if (random > porcHuida) {
					GestorSalida.ENVIAR_GA_ACCION_PELEA(this, 7, 104, movedor.getID() + ";", "");
					// PA
					int pierdePA = Math.max(0, movedor.getPARestantes());
					pierdePA = (int) Math.rint(pierdePA * (100 - porcHuida) / 100f);
					pierdePA = Math.abs(pierdePA);
					// PM
					int pierdePM = Math.max(0, movedor.getPMRestantes());
					if (!MainServidor.PARAM_FORMULA_TIPO_OFICIAL) {
						pierdePM = (int) Math.rint(pierdePM * (100 - porcHuida) / 100f);
						pierdePM = Math.abs(pierdePM);
						pierdePM = Math.max(1, pierdePM);
					}
					pierdePM = movedor.addPMRestantes(-pierdePM);
					if (pierdePM != 0) {
						GestorSalida.ENVIAR_GA_ACCION_PELEA(this, 7, 129, movedor.getID() + "", movedor.getID() + "," + (pierdePM));
					}
					pierdePA = movedor.addPARestantes(-pierdePA);
					if (pierdePA != 0) {
						GestorSalida.ENVIAR_GA_ACCION_PELEA(this, 7, 102, movedor.getID() + "", movedor.getID() + "," + (pierdePA));
					}
					_tacleado = true;
					return "tacleado";
				}
			}
		}
		String moverse = "ok";
		final AtomicReference<String> pathRef = new AtomicReference<String>(path);
		short ultimaCelda = -1;
		try {
			ultimaCelda = Encriptador.hashACeldaID(path.substring(path.length() - 2));
		} catch (Exception e) {}
		short nroCeldasMov = Camino.nroCeldasAMover(_mapaCopia, this, pathRef, movedor.getCeldaPelea().getID(), ultimaCelda,
		null);
		// System.out.println("celdas " + nroCeldasMov);
		if (nroCeldasMov >= 10000) {
			moverse = "stop";
			if (nroCeldasMov >= 20000) {
				// invisible
				GestorSalida.ENVIAR_GA_ACCION_PELEA(this, 7, 151, movedor.getID() + "", "-1");
				GestorSalida.ENVIAR_GAF_FINALIZAR_ACCION(movedor.getPersonaje(), movedor.getID(), _idUnicaAccion);
				nroCeldasMov -= 10000;
			}
			nroCeldasMov -= 10000;
		}
		// System.out.println("nroCeldasMov " + nroCeldasMov);
		if (nroCeldasMov <= 0 || nroCeldasMov > movedor.getPMRestantes() || nroCeldasMov == -1000) {
			GestorSalida.ENVIAR_GAF_FINALIZAR_ACCION(movedor.getPersonaje(), movedor.getID(), _idUnicaAccion);
			return "no";
		}
		if (AJ != null) {
			AJ.setCeldas(nroCeldasMov);
		}
		movedor.addPMRestantes(-nroCeldasMov);
		movedor.addPMUsados(nroCeldasMov);
		if ((_tipo == Constantes.PELEA_TIPO_PVM || _tipo == Constantes.PELEA_TIPO_PVM_NO_ESPADA) && _retos != null
		&& movedor.getPersonaje() != null) {
			for (final Entry<Byte, Reto> entry : _retos.entrySet()) {
				Reto reto = entry.getValue();
				final byte retoID = entry.getKey();
				EstReto exitoReto = reto.getEstado();
				if (exitoReto != Reto.EstReto.EN_ESPERA) {
					continue;
				}
				switch (retoID) {
					case Constantes.RETO_ZOMBI :
						if (movedor.getPMUsados() == 1) {
							break;
						}
						exitoReto = Reto.EstReto.FALLADO;
						break;
				}
				reto.setEstado(exitoReto);
			}
		}
		final String nuevoPath = pathRef.get();
		String ultPathMov = nuevoPath.substring(nuevoPath.length() - 3);
		final short sigCeldaID = Encriptador.hashACeldaID(ultPathMov.substring(1));
		Celda nuevaCelda = _mapaCopia.getCelda(sigCeldaID);
		if (nuevaCelda == null) {
			if (movedor.getPersonaje() != null) {
				GestorSalida.ENVIAR_Im_INFORMACION(movedor.getPersonaje(), "1102");
				GestorSalida.ENVIAR_GAF_FINALIZAR_ACCION(movedor.getPersonaje(), movedor.getID(), _idUnicaAccion);
			}
			return "no";
		}
		movedor.setDireccion(ultPathMov.charAt(0));
		if (persoM != null) {// confirma el inicio de una accion
			GestorSalida.ENVIAR_GAS_INICIO_DE_ACCION(persoM, movedor.getID());
		}
		_idUnicaAccion = idUnica;
		// confirma q se movio
		GestorSalida.ENVIAR_GA_ACCION_PELEA_MOVERSE(this, movedor, 7, _idUnicaAccion, 1, movedor.getID() + "", "a"
		+ Encriptador.celdaIDAHash(movedor.getCeldaPelea().getID()) + nuevoPath);
		final Luchador portador = movedor.getPortador();
		if (portador != null && nuevaCelda != portador.getCeldaPelea()) {
			movedor.getCeldaPelea().removerLuchador(movedor);
			movedor.setCeldaPelea(nuevaCelda);
			quitarTransportados(portador);
			if (movedor.getIA() != null) {
				movedor.getIA().nullear();
			}
		} else {
			movedor.getCeldaPelea().moverLuchadoresACelda(nuevaCelda);
		}
		// final Luchador transportado = movedor.getTransportando();
		// if (transportado != null) {
		// transportado.setCeldaPelea(movedor.getCeldaPelea());
		// }
		_ultimoMovedorIDReto = movedor.getID();
		_tempAccion = "Moverse";
		// GestorSalida.ENVIAR_GA_ACCION_PELEA(this, 7, 129, movedor.getID() + "", movedor.getID() + "," + (-nroCeldasMov));
		if (persoM == null) {
			int factor = nroCeldasMov >= 4 ? 330 : 750;
			try {
				int timer = (200 + (factor * nroCeldasMov));
				Thread.sleep(timer);
			} catch (final Exception e) {}
			// GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(this, 7, movedor.getID(),
			// movedor.getCeldaPelea().getID());
			_tempAccion = "";
			EfectoHechizo.verificaTrampas(movedor);
		} else {
			// test
			// finalizarMovimiento(persoM);
		}
		return moverse;
	}
	
	public boolean finalizarMovimiento(Personaje perso) {
		if (_tacleado) {
			GestorSalida.ENVIAR_BN_NADA(perso, "FIN MOVIMIENTO TACLEADO");
			return false;
		}
		if (_luchadorDeTurno == null || _tempAccion.isEmpty() || _fase != Constantes.PELEA_FASE_COMBATE) {
			GestorSalida.ENVIAR_BN_NADA(perso, "FIN MOVIMIENTO OTROS");
			return false;
		}
		int idLuch = perso.getID();
		if (idLuch != _ultimoMovedorIDReto) {
			if (perso.getCompañero() == null) {
				return false;
			}
			idLuch = perso.getCompañero().getID();
			perso = perso.getCompañero();
			if (idLuch != _ultimoMovedorIDReto) {
				return false;
			}
		}
		Luchador luchador = getLuchadorPorID(idLuch);
		if (luchador == null) {
			GestorSalida.ENVIAR_BN_NADA(perso, "FIN MOVIMIENTO LUCHADOR NULL");
			return false;
		}
		// GestorSalida.ENVIAR_GA_PERDER_PM_PELEA(this, 7, _tempAccion);
		// eso puede ser opcional si borro el GAC
		_idUnicaAccion = -1;
		_tempAccion = "";
		EfectoHechizo.verificaTrampas(luchador);
		GestorSalida.ENVIAR_GAF_FINALIZAR_ACCION(perso, idLuch, -1);
		GestorSalida.ENVIAR_GAs_PARAR_MOVIMIENTO_SPRITE(perso, idLuch);		  
	     
		return true;
	}
	
	public synchronized EstadoLanzHechizo intentarLanzarHechizo(final Luchador lanzador, final StatHechizo SH,
	final Celda celdaObjetivo, final boolean obligaLanzar) {
		if (lanzador == null) {
			return EstadoLanzHechizo.NO_PODER;
		}
		final Personaje perso = lanzador.getPersonaje();
		if (!lanzador.puedeJugar()) {
			if (lanzador.esNoIA()) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1NO_ES_TU_TURNO");
				GestorSalida.ENVIAR_GAC_LIMPIAR_ACCION(perso);
			}
			if (MainServidor.MODO_DEBUG) {
				System.out.println("intentarLanzarHechizo() Hechizo: " + SH.getHechizo().getNombre() + " (" + SH.getHechizoID()
				+ ") Estado: NO PODER");
			}
			return EstadoLanzHechizo.NO_PODER;
		}
		if ((!_tempAccion.isEmpty() && perso != null) || SH == null) {
			if (lanzador.esNoIA()) {
				if (!_tempAccion.isEmpty()) {
					if (!MainServidor.PARAM_JUGAR_RAPIDO) {
						GestorSalida.ENVIAR_Im_INFORMACION(perso, "1REALIZANDO_TEMP_ACCION;" + _tempAccion);
					} else {
						finAccion(lanzador);
						if (MainServidor.MODO_DEBUG) {
							System.out.println("intentarLanzarHechizo() Hechizo: " + SH.getHechizo().getNombre() + " (" + SH
							.getHechizoID() + ") Estado: NO PODER");
						}
						return EstadoLanzHechizo.NO_PODER;
					}
				} else {
					GestorSalida.ENVIAR_Im_INFORMACION(perso, "1169");
				}
				GestorSalida.ENVIAR_GAC_LIMPIAR_ACCION(perso);
			}
			if (MainServidor.MODO_DEBUG) {
				System.out.println("intentarLanzarHechizo() Hechizo: " + SH.getHechizo().getNombre() + " (" + SH.getHechizoID()
				+ ") Estado: NO PODER");
			}
			return EstadoLanzHechizo.NO_PODER;
		}
		_tempAccion = "Hechizo";
		_tiempoHechizo = EfectoHechizo.TIEMPO_POR_LANZAR_HECHIZO;
		EstadoLanzHechizo puede = EstadoLanzHechizo.PODER;
		if (obligaLanzar || (puede = puedeLanzarHechizo(lanzador, SH, celdaObjetivo,
		(short) -1)) == EstadoLanzHechizo.PODER) {
			byte costePA = SH.getCostePA();
			if (perso != null && perso.tieneModfiSetClase(SH.getHechizoID())) {
				costePA -= perso.getModifSetClase(SH.getHechizoID(), 285);
				if (costePA < 0) {
					costePA = 0;
				}
			}
			if (perso != null) {
				GestorSalida.ENVIAR_GAS_INICIO_DE_ACCION(perso, lanzador.getID());// inicia la accion
			}
			lanzador.addPARestantes(-costePA);
			lanzador.addPAUsados(costePA);
			// try {
			// aun cuando el socket se pierde (desconexion del jugador) el thread continua su curso hasta
			// terminar su metodo.
			// Thread.sleep(10000);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			boolean esFC = lanzador.puedeFalloCritico(SH);
			int cantObjetivos = 0;
			GestorSalida.ENVIAR_GA_ACCION_PELEA(this, 7, 102, lanzador.getID() + "", lanzador.getID() + "," + (-costePA));
			if (esFC) {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(this, 7, 302, lanzador.getID() + "", SH.getHechizoID() + "");
			} else {// es golpe normal
				if ((_tipo == Constantes.PELEA_TIPO_PVM || _tipo == Constantes.PELEA_TIPO_PVM_NO_ESPADA) && _retos != null
				&& lanzador.esNoIA()) {
					for (final Entry<Byte, Reto> entry : _retos.entrySet()) {
						Reto reto = entry.getValue();
						final byte retoID = entry.getKey();
						EstReto exitoReto = reto.getEstado();
						if (exitoReto != Reto.EstReto.EN_ESPERA) {
							continue;
						}
						switch (retoID) {
							case Constantes.RETO_AHORRADOR :
								if (lanzador.getHechizosLanzadosReto().contains(SH.getHechizoID())) {
									exitoReto = Reto.EstReto.FALLADO;
								} else {
									lanzador.getHechizosLanzadosReto().add(SH.getHechizoID());
								}
								break;
							case Constantes.RETO_VERSATIL :
								if (lanzador.getHechizosLanzadosReto().contains(SH.getHechizoID())) {
									exitoReto = Reto.EstReto.FALLADO;
								} else {
									lanzador.getHechizosLanzadosReto().add(SH.getHechizoID());
								}
								break;
							case Constantes.RETO_LIMITADO :
								final int hechizoID = SH.getHechizoID();
								if (lanzador.getIDHechizoLanzado() == -1) {
									lanzador.setIDHechizoLanzado(hechizoID);
								} else if (lanzador.getIDHechizoLanzado() != hechizoID) {
									exitoReto = Reto.EstReto.FALLADO;
								}
								break;
						}
						reto.setEstado(exitoReto);
					}
				}
				lanzador.addHechizoLanzado(lanzador, SH, celdaObjetivo.getPrimerLuchador());
				final boolean esGC = lanzador.puedeGolpeCritico(SH);
				ArrayList<EfectoHechizo> efectos = esGC ? SH.getEfectosCriticos() : SH.getEfectosNormales();
				final String hechizoStr = SH.getHechizoID() + "," + celdaObjetivo.getID() + "," + SH.getSpriteID() + "," + SH
				.getGrado() + "," + SH.getSpriteInfos();
				GestorSalida.ENVIAR_GA_ACCION_PELEA(this, 7, 300, lanzador.getID() + "", hechizoStr);
				if (esGC) {
					GestorSalida.ENVIAR_GA_ACCION_PELEA(this, 7, 301, lanzador.getID() + "", hechizoStr);
				}
				// el PA cambiado , hace un setSpellStateOnAllContainers a todos los hechizos, y asi
				// actualiza los estados
				if (costePA > 0 && lanzador.esInvisible(0)) {
					GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(this, 7, lanzador.getID(), lanzador.getCeldaPelea().getID());
				}
				cantObjetivos = Hechizo.aplicaHechizoAPeleaSinGTM(this, lanzador, celdaObjetivo, efectos, TipoDaño.NORMAL,
				esGC);
			}
			// salio del fallo o lanz normal
			if (perso != null) {
				GestorSalida.ENVIAR_GAF_FINALIZAR_ACCION(perso, lanzador.getID(), -1);
			}
			if (cantObjetivos > 0) {
				GestorSalida.ENVIAR_GTM_INFO_STATS_TODO_LUCHADORES_A_TODOS(this, 7, false);
			}
			if (lanzador.getIA() != null) {
				try {
					Thread.sleep(_tiempoHechizo);
				} catch (final Exception e) {}
			}
			if (esFC && (lanzador.getIA() != null || SH.esFinTurnoSiFC())) {
				puede = EstadoLanzHechizo.FALLAR;
				pasarTurno(lanzador);
			}
		} else if (lanzador.esNoIA()) {
			GestorSalida.ENVIAR_GAC_LIMPIAR_ACCION(perso);
		}
		_tempAccion = "";
		if (lanzador.getIA() != null) {
			lanzador.getIA().nullear();
		}
		comprobarPasarTurnoDespuesMuerte(lanzador);
		if (MainServidor.MODO_DEBUG) {
			System.out.println("intentarLanzarHechizo() Hechizo: " + SH.getHechizo().getNombre() + " (" + SH.getHechizoID()
			+ ") Estado: " + puede);
		}
		return puede;
	}
	
	// ataque cuerpo a cuerpo CAC
	public synchronized void intentarCAC(final Personaje perso, final short idCeldaObj) {
		final Luchador lanzador = getLuchadorPorID(perso.getID());
		if (lanzador == null) {
			GestorSalida.ENVIAR_GAC_LIMPIAR_ACCION(perso);
			return;
		}
		if (!lanzador.puedeJugar()) {
			if (lanzador.esNoIA()) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1175");
				GestorSalida.ENVIAR_GAC_LIMPIAR_ACCION(perso);
			}
			return;
		}
		if ((!_tempAccion.isEmpty() && perso != null)) {
			if (lanzador.esNoIA()) {
				if (!_tempAccion.isEmpty()) {
					if (!MainServidor.PARAM_JUGAR_RAPIDO) {
						GestorSalida.ENVIAR_Im_INFORMACION(perso, "1REALIZANDO_TEMP_ACCION;" + _tempAccion);
					} else {
						finAccion(lanzador);
						return;
					}
				}
				GestorSalida.ENVIAR_GAC_LIMPIAR_ACCION(perso);
			}
			return;
		}
		if ((_tipo == Constantes.PELEA_TIPO_PVM || _tipo == Constantes.PELEA_TIPO_PVM_NO_ESPADA) && _retos != null) {// mobs
			for (final Entry<Byte, Reto> entry : _retos.entrySet()) {
				Reto reto = entry.getValue();
				final byte retoID = entry.getKey();
				EstReto exitoReto = reto.getEstado();
				if (exitoReto != Reto.EstReto.EN_ESPERA) {
					continue;
				}
				switch (retoID) {
					case Constantes.RETO_AHORRADOR :
						if (lanzador.getHechizosLanzadosReto().contains(0)) {
							exitoReto = Reto.EstReto.FALLADO;
						} else {
							lanzador.getHechizosLanzadosReto().add(0);
						}
						break;
					case Constantes.RETO_VERSATIL :
						if (lanzador.getHechizosLanzadosReto().contains(0)) {
							exitoReto = Reto.EstReto.FALLADO;
						} else {
							lanzador.getHechizosLanzadosReto().add(0);
						}
						break;
					case Constantes.RETO_MISTICO :
						exitoReto = Reto.EstReto.FALLADO;
						break;
					case Constantes.RETO_LIMITADO :
						final int hechizoID = 0;
						if (lanzador.getIDHechizoLanzado() == -1) {
							lanzador.setIDHechizoLanzado(hechizoID);
						} else if (lanzador.getIDHechizoLanzado() != hechizoID) {
							exitoReto = Reto.EstReto.FALLADO;
						}
						break;
				}
				reto.setEstado(exitoReto);
			}
		}
		StatHechizo SH = Mundo.getHechizo(0).getStatsPorNivel(1);
		ArrayList<EfectoHechizo> eNormales = SH.getEfectosNormales();
		ArrayList<EfectoHechizo> eCriticos = SH.getEfectosCriticos();
		final Objeto arma = perso.getObjPosicion(Constantes.OBJETO_POS_ARMA);
		if (arma != null) {
			SH = arma.getObjModelo().getStatHechizo();
			eNormales = arma.getEfectosNormales();
			eCriticos = arma.getEfectosCriticos();
			int costePA = (int) arma.getObjModelo().getCostePA();
			if (MainServidor.MAX_GOLPES_CAC.get(costePA) != null) {
				int maximo = MainServidor.MAX_GOLPES_CAC.get(costePA);
				if (maximo <= _cantCAC) {
					GestorSalida.ENVIAR_GAC_LIMPIAR_ACCION(perso);
					return;
				}
			}
		}
		Celda celdaObjetivo = _mapaCopia.getCelda(idCeldaObj);
		EstadoLanzHechizo puede = puedeLanzarHechizo(lanzador, SH, celdaObjetivo, (short) -1);
		if (puede != EstadoLanzHechizo.PODER) {
			GestorSalida.ENVIAR_GAC_LIMPIAR_ACCION(perso);
			return;
		}
		if (MainServidor.MAX_CAC_POR_TURNO > 0 && _cantCAC >= MainServidor.MAX_CAC_POR_TURNO) {
			GestorSalida.ENVIAR_GAC_LIMPIAR_ACCION(perso);
			return;
		}
		_cantCAC++;
		if (lanzador.esInvisible(0)) {
			lanzador.hacerseVisible();
		}
		GestorSalida.ENVIAR_GAS_INICIO_DE_ACCION(lanzador.getPersonaje(), lanzador.getID());
		byte costePA = SH.getCostePA();
		lanzador.addPARestantes(-costePA);
		lanzador.addPAUsados(costePA);
		GestorSalida.ENVIAR_GA_ACCION_PELEA(this, 7, 102, perso.getID() + "", perso.getID() + "," + (-costePA));
		final boolean esFC = lanzador.puedeFalloCritico(SH);
		if (esFC) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(this, 7, 305, perso.getID() + "", "");
		} else {
			_tempAccion = "CAC";
			final boolean esGC = lanzador.puedeGolpeCritico(SH);
			GestorSalida.ENVIAR_GA_ACCION_PELEA(this, 7, 303, perso.getID() + "", idCeldaObj + "");
			if (esGC) {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(this, 7, 301, perso.getID() + "", "0");
			}
			Hechizo.aplicaHechizoAPelea(this, lanzador, celdaObjetivo, esGC ? eCriticos : eNormales, TipoDaño.CAC, esGC);
			_tempAccion = "";
		}
		GestorSalida.ENVIAR_GAF_FINALIZAR_ACCION(perso, perso.getID(), -1);
		if (esFC) {
			pasarTurno(lanzador);
		} else {
			comprobarPasarTurnoDespuesMuerte(lanzador);
		}
	}
	
	public EstadoLanzHechizo puedeLanzarHechizo(final Luchador lanzador, final StatHechizo SH,
	final Celda celdaBlancoHechizo, final short celdaDeLanzador) {
		if (_luchadorDeTurno == null) {
			return EstadoLanzHechizo.NO_PODER;
		}
		final Personaje perso = lanzador.getPersonaje();
		if (celdaBlancoHechizo == null) {
			if (perso != null) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1172");
			}
			if (MainServidor.MODO_DEBUG) {
				System.out.println("puedeLanzarHechizo() -> La celda blanco hechizo es nula");
			}
			return EstadoLanzHechizo.NO_PODER;
		}
		if (SH == null) {
			if (perso != null) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1169");
			}
			if (MainServidor.MODO_DEBUG) {
				System.out.println("puedeLanzarHechizo() -> El hechizo es nulo");
			}
			return EstadoLanzHechizo.NO_PODER;
		}
		if (SH.esAutomaticoAlFinalTurno()) {
			if (perso != null) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1IS_AUTOMATIC_END_TURN");
			}
			if (MainServidor.MODO_DEBUG) {
				System.out.println("puedeLanzarHechizo() -> El hechizo es para lanzamiento automatico");
			}
			return EstadoLanzHechizo.NO_PODER;
		}
		Luchador objetivo = celdaBlancoHechizo.getPrimerLuchador();
		EstadoLanzHechizo filtro = filtraHechizoDisponible(lanzador, SH, objetivo == null ? 0 : objetivo.getID());
		if (filtro != EstadoLanzHechizo.PODER) {
			return filtro;
		}
		if (perso == null) {
			if ((SH.esIntercambioPos() || SH.esSoloMover()) && objetivo != null && objetivo.tieneEstado(
			Constantes.ESTADO_ARRAIGADO)) {
				return EstadoLanzHechizo.OBJETIVO_NO_PERMITIDO;
			}
		}
		if (!dentroDelRango(lanzador, SH, celdaDeLanzador, celdaBlancoHechizo.getID())) {
			return EstadoLanzHechizo.NO_TIENE_ALCANCE;
		}
		return EstadoLanzHechizo.PODER;
	}
	
	public boolean dentroDelRango(Luchador lanzador, StatHechizo SH, short celdaIDLanzador, short celdaIDBlanco) {
		if (SH == null) {
			if (MainServidor.MODO_DEBUG) {
				System.out.println("dentroDelRango() -> El hechizo es nulo");
			}
			return false;
		}
		Mapa mapa = _mapaCopia;
		if (mapa.getCelda(celdaIDBlanco) == null) {
			return false;
		}
		Personaje perso = lanzador.getPersonaje();
		if (SH.esTrampa() && mapa.getCelda(celdaIDBlanco).esTrampa()) {
			if (perso != null) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1229");
			}
			return false;
		}
		short tempCeldaIDLanzador;
		if (celdaIDLanzador <= -1) {
			tempCeldaIDLanzador = lanzador.getCeldaPelea().getID();
		} else {
			tempCeldaIDLanzador = celdaIDLanzador;
		}
		if (!Camino.celdasPosibleLanzamiento(SH, lanzador, _mapaCopia, tempCeldaIDLanzador, celdaIDBlanco).contains(mapa
		.getCelda(celdaIDBlanco))) {
			return false;
		}
		// if (SH.esNecesarioCeldaLibre() && mapa.getCelda(celdaIDBlanco).getPrimerLuchador() != null) {
		// if (perso != null) {
		// GestorSalida.ENVIAR_Im_INFORMACION(perso, "1173");
		// }
		// if (Bustemu.MODO_DEBUG) {
		// System.out.println("dentroDelRango() -> El hechizo " + SH.getHechizo().getNombre() +
		// " necesita celda libre");
		// }
		// return false;
		// }
		// boolean modif = false;
		// final int hechizoID = SH.getHechizoID();
		// if (perso != null && perso.tieneModfiSetClase(hechizoID)) {
		// modif = perso.getModifSetClase(hechizoID, 288) == 1;
		// }
		// if (SH.esLanzarLinea() && !modif
		// && !Camino.siCeldasEstanEnMismaLinea(mapa, mapa.getCelda(tempCeldaIDLanzador),
		// mapa.getCelda(celdaIDBlanco))) {
		// if (perso != null) {
		// GestorSalida.ENVIAR_Im_INFORMACION(perso, "1173");
		// }
		// if (Bustemu.MODO_DEBUG) {
		// System.out.println("dentroDelRango() -> El hechizo " + SH.getHechizo().getNombre() +
		// " no esta en Linea");
		// }
		// return false;
		// }
		// modif = false;
		// if (perso != null && perso.tieneModfiSetClase(hechizoID)) {
		// modif = perso.getModifSetClase(hechizoID, 289) == 1;
		// }
		// if (!modif && SH.esLineaVista()
		// && !Camino.lineaDeVistaPelea(mapa, tempCeldaIDLanzador, celdaIDBlanco, lanzador.getID())) {
		// if (perso != null) {
		// GestorSalida.ENVIAR_Im_INFORMACION(perso, "1174");
		// }
		// if (Bustemu.MODO_DEBUG) {
		// System.out.println("dentroDelRango() -> El hechizo " + SH.getHechizo().getNombre() +
		// " tiene linea de vista");
		// }
		// return false;
		// }
		// byte maxAlc = SH.getMaxAlc();
		// final byte minAlc = SH.getMinAlc();
		// modif = false;
		// if (perso != null && perso.tieneModfiSetClase(hechizoID)) {
		// maxAlc += perso.getModifSetClase(hechizoID, 281);
		// modif = perso.getModifSetClase(hechizoID, 282) == 1;
		// }
		// if (modif || SH.esAlcanceModificable()) {
		// maxAlc += lanzador.getTotalStats().getStatParaMostrar(Constantes.STAT_MAS_ALCANCE);
		// }
		// if (maxAlc < minAlc) {
		// maxAlc = minAlc;
		// }
		// final int dist = Camino.distanciaDosCeldas(mapa, tempCeldaIDLanzador, celdaIDBlanco);
		// if (dist < minAlc || dist > maxAlc) {
		// if (perso != null) {
		// GestorSalida.ENVIAR_Im_INFORMACION(perso, "1171;" + minAlc + "~" + maxAlc + "~" + dist);
		// }
		// if (Bustemu.MODO_DEBUG) {
		// System.out.println("dentroDelRango() -> El hechizo " + SH.getHechizo().getNombre() +
		// " esta fuera del alcance");
		// }
		// return false;
		// }
		return true;
	}
	
	public EstadoLanzHechizo filtraHechizoDisponible(final Luchador lanzador, StatHechizo SH, final int idObjetivo) {
		final int hechizoID = SH.getHechizoID();
		Personaje perso = lanzador.getPersonaje();
		if (SH.esNecesarioObjetivo() && idObjetivo == 0) {
			if (perso != null) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1NEED_A_TARGET");
			}
			if (MainServidor.MODO_DEBUG) {
				System.out.println("filtrarHechizo() -> Necesita un objetivo");
			}
			return EstadoLanzHechizo.NO_OBJETIVO;
		}
		for (final int estado : SH.getEstadosProhibido()) {
			if (lanzador.tieneEstado(estado)) {
				if (perso != null) {
					GestorSalida.ENVIAR_Im_INFORMACION(perso, "1IN_FORBIDDEN_STATE;" + estado);
				}
				if (MainServidor.MODO_DEBUG) {
					System.out.println("filtrarHechizo() -> Tiene el estado prohibido " + estado + " para lanzar " + SH
					.getHechizo().getNombre());
				}
				return EstadoLanzHechizo.NO_PODER;
			}
		}
		for (final int estado : SH.getEstadosNecesario()) {
			if (!lanzador.tieneEstado(estado)) {
				if (perso != null) {
					GestorSalida.ENVIAR_Im_INFORMACION(perso, "1NOT_IN_REQUIRED_STATE;" + estado);
				}
				if (MainServidor.MODO_DEBUG) {
					System.out.println("filtrarHechizo() -> No tiene el estado necesario " + estado + " para lanzar " + SH
					.getHechizo().getNombre());
				}
				return EstadoLanzHechizo.NO_PODER;
			}
		}
		byte costePA = SH.getCostePA();
		if (perso != null && perso.tieneModfiSetClase(hechizoID)) {
			costePA -= perso.getModifSetClase(hechizoID, 285);
		}
		int PA = lanzador.getPARestantes();
		if (PA < costePA) {
			if (perso != null) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1170;" + PA + "~" + SH.getCostePA());
			}
			if (MainServidor.MODO_DEBUG) {
				System.out.println("filtrarHechizo() -> No tiene suficientes PA para lanzar " + SH.getHechizo().getNombre());
			}
			return EstadoLanzHechizo.NO_TIENE_PA;
		}
		int sigTurnoLanz = HechizoLanzado.poderSigLanzamiento(lanzador, hechizoID);
		if (sigTurnoLanz > 0) {
			if (perso != null) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1CANT_LAUNCH_BEFORE;" + sigTurnoLanz);
			}
			if (MainServidor.MODO_DEBUG) {
				System.out.println("filtrarHechizo() -> Falta " + sigTurnoLanz + " turnos para lanzar " + SH.getHechizo()
				.getNombre());
			}
			return EstadoLanzHechizo.COOLDOWN;
		}
		byte nroLanzTurno = SH.getMaxLanzPorTurno();
		if (perso != null && perso.tieneModfiSetClase(hechizoID)) {
			nroLanzTurno += perso.getModifSetClase(hechizoID, 290);
		}
		if (nroLanzTurno > 0 && nroLanzTurno - HechizoLanzado.getNroLanzamientos(lanzador, hechizoID) <= 0) {
			if (perso != null) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1CANT_LAUNCH_MORE;" + nroLanzTurno);
			}
			if (MainServidor.MODO_DEBUG) {
				System.out.println("filtrarHechizo() -> El nroLanzTurno es " + nroLanzTurno
				+ ", por lo tanto no se puede lanzar " + SH.getHechizo().getNombre());
			}
			return EstadoLanzHechizo.COOLDOWN;
		}
		if (idObjetivo != 0) {
			byte nroLanzMaxObjetivo = SH.getMaxLanzPorObjetivo();
			if (perso != null && perso.tieneModfiSetClase(hechizoID)) {
				nroLanzMaxObjetivo += perso.getModifSetClase(hechizoID, 291);
			}
			if (nroLanzMaxObjetivo >= 1 && (HechizoLanzado.getNroLanzPorObjetivo(lanzador, idObjetivo,
			hechizoID)) >= nroLanzMaxObjetivo) {
				if (perso != null) {
					GestorSalida.ENVIAR_Im_INFORMACION(perso, "1CANT_ON_THIS_PLAYER");
				}
				if (MainServidor.MODO_DEBUG) {
					System.out.println("filtrarHechizo() -> El nroMaxObjetivo " + nroLanzMaxObjetivo
					+ " por lo tanto no se puede lanzar " + SH.getHechizo().getNombre());
				}
				return EstadoLanzHechizo.OBJETIVO_NO_PERMITIDO;
			}
		}
		return EstadoLanzHechizo.PODER;
	}
	
	private void robarPersonajePerdedor(final Luchador luch) {
		if (!MainServidor.PARAM_JUGADORES_HEROICO_MORIR) {
			return;
		}
		Personaje pjPerdedor = luch.getPersonaje();
		if (pjPerdedor == null) {
			return;
		}
		if (luch.fueSaqueado()) {
			return;
		}
		imprimiAsesinos(luch);
		if (esMapaHeroico()) {
			switch (_tipo) {
				case Constantes.PELEA_TIPO_DESAFIO :
				case Constantes.PELEA_TIPO_KOLISEO :
					break;
				default :
					luch.setSaqueado(true);
					Montura montura = pjPerdedor.getMontura();
					if (montura != null) {
						pjPerdedor.setMontura(null);
						if (montura.getPergamino() <= 0) {
							try {
								final Objeto obj1 = montura.getObjModCertificado().crearObjeto(1, Constantes.OBJETO_POS_NO_EQUIPADO,
								CAPACIDAD_STATS.RANDOM);
								obj1.fijarStatValor(Constantes.STAT_CONSULTAR_MONTURA, Math.abs(montura.getID()));
								obj1.addStatTexto(Constantes.STAT_PERTENECE_A, "0#0#0#" + pjPerdedor.getNombre());
								obj1.addStatTexto(Constantes.STAT_NOMBRE, "0#0#0#" + montura.getNombre());
								pjPerdedor.addObjetoConOAKO(obj1, true);
								montura.setPergamino(obj1.getID());
							} catch (Exception e) {}
						}
					}
					long kamas = pjPerdedor.getKamas();
					_kamasRobadas += kamas;
					_expRobada += pjPerdedor.getExperiencia() / 10;
					pjPerdedor.addKamas(-kamas, false, false);
					final ArrayList<Objeto> objPerder = new ArrayList<Objeto>();
					objPerder.addAll(pjPerdedor.getObjetosTodos());
					for (final Objeto obj : objPerder) {
						if (robarObjPersonaje(obj, pjPerdedor)) {
							pjPerdedor.borrarOEliminarConOR(obj.getID(), false);
						}
					}
					objPerder.clear();
					objPerder.addAll(pjPerdedor.getObjetosTienda());
					for (final Objeto obj : objPerder) {
						if (robarObjPersonaje(obj, pjPerdedor)) {
							pjPerdedor.borrarObjTienda(obj);
						}
					}
					pjPerdedor.convertirseTumba();
					GestorSQL.INSERT_CEMENTERIO(pjPerdedor.getNombre(), pjPerdedor.getNivel(), pjPerdedor.getSexo(), pjPerdedor
					.getClaseID(true), _asesinos.toString(), _mapaReal.getSubArea().getID());
					GestorSQL.SALVAR_PERSONAJE(pjPerdedor, true);
					break;
			}
		}
	}
	
	// private int getPorcFinal(int porcentaje, float coef) {
	// // formulda para drop porcentaje
	// int f = (int) ((1 - (Math.pow(1 - (porcentaje / 100000f), coef))) * 100000);
	// return Math.max(1, f);
	// }
	private boolean robarObjPersonaje(Objeto objeto, Personaje pjPerd) {
		if (!objeto.pasoIntercambiableDesde()) {
			pjPerd.addObjetoAlBanco(objeto);
			return false;
		}
		if (objeto.tieneStatTexto(Constantes.STAT_LIGADO_A_CUENTA)) {
			pjPerd.addObjetoAlBanco(objeto);
			return false;
		}
		if (!MainServidor.PARAM_HEROICO_PIERDE_ITEMS_VIP && objeto.getObjModelo().getOgrinas() > 0) {
			return false;
		}
		if (objeto.getPosicion() >= 20 && objeto.getPosicion() <= 27) {
			return false;
		}
		if (objeto.getObjModelo().getTipo() == Constantes.OBJETO_TIPO_OBJETO_DE_BUSQUEDA) {
			return false;
		}
		objeto.setPosicion(Constantes.OBJETO_POS_NO_EQUIPADO, pjPerd, false);
		addObjetosRobados(objeto);
		return true;
	}
	
	private MisionPVP getMisionPVPPorEquipo(int equipo) {
		if (equipo == 1 && _inicioLuchEquipo1.size() == 1) {
			try {
				final Personaje init = _luchInit1.getPersonaje();
				if (init != null && init.getMisionPVP() != null) {
					String victima = init.getMisionPVP().getNombreVictima();
					for (final Luchador luchador : _inicioLuchEquipo2) {
						Personaje p = luchador.getPersonaje();
						if (p == null) {
							continue;
						}
						if (p.getNombre().equalsIgnoreCase(victima)) {
							return init.getMisionPVP();
						}
						if (p.getMisionPVP() != null) {
							try {
								if (p.getMisionPVP().getNombreVictima().equalsIgnoreCase(init.getNombre())) {
									return p.getMisionPVP();
								}
							} catch (Exception e) {}
						}
					}
				}
			} catch (final Exception e) {}
		}
		if (equipo == 2 && _inicioLuchEquipo2.size() == 1) {
			try {
				final Personaje init = _luchInit2.getPersonaje();
				if (init != null && init.getMisionPVP() != null) {
					String victima = init.getMisionPVP().getNombreVictima();
					for (final Luchador luchador : _inicioLuchEquipo1) {
						Personaje p = luchador.getPersonaje();
						if (p == null) {
							continue;
						}
						if (p.getNombre().equalsIgnoreCase(victima)) {
							return init.getMisionPVP();
						}
						if (p.getMisionPVP() != null) {
							try {
								if (p.getMisionPVP().getNombreVictima().equalsIgnoreCase(init.getNombre())) {
									return p.getMisionPVP();
								}
							} catch (Exception e) {}
						}
					}
				}
			} catch (final Exception e) {}
		}
		return null;
	}
	
	private void recompensaMision(Luchador luchGanador, boolean ganador) {
		MisionPVP mision = getMisionPVPPorEquipo(luchGanador.getParamEquipoAliado());
		if (mision == null) {
			return;
		}
		if (ganador) {
			StringBuilder objetos = new StringBuilder();
			if (mision.esCazaCabezas()) {
				objetos.append(MainServidor.MISION_PVP_OBJETOS);
				int craneo = mision.getCraneo();
				if (craneo != 0) {
					if (objetos.length() > 0) {
						objetos.append(";");
					}
					objetos.append(craneo + "," + 1);
				}
			}
			for (String s : objetos.toString().split(";")) {
				try {
					if (s.isEmpty()) {
						continue;
					}
					int id = Integer.parseInt(s.split(",")[0]);
					int cant = Integer.parseInt(s.split(",")[1]);
					Objeto obj = Mundo.getObjetoModelo(id).crearObjeto(cant, Constantes.OBJETO_POS_NO_EQUIPADO,
					CAPACIDAD_STATS.RANDOM);
					luchGanador.addDropLuchador(obj, true);
				} catch (Exception e) {}
			}
			if (MainServidor.PARAM_GANAR_KAMAS_PVP) {
				final long kamas = mision.getKamasRecompensa();
				luchGanador.addKamasGanadas(kamas);
			}
			if (MainServidor.PARAM_GANAR_EXP_PVP) {
				final long expPorMision = mision.getExpMision();
				luchGanador.addXPGanada(expPorMision);
				GestorSalida.ENVIAR_Im_INFORMACION(luchGanador.getPersonaje(), "08;" + expPorMision);
			}
		}
		if (mision == luchGanador.getPersonaje().getMisionPVP()) {
			luchGanador.getPersonaje().eliminarPorObjModeloRecibidoDesdeMinutos(10085, 0);// pergamino
			luchGanador.getPersonaje().setMisionPVP(null);
		}
	}
	
	private void addObjetosRobados(Objeto obj) {
		if (_objetosRobados == null) {
			_objetosRobados = new ArrayList<Objeto>();
		}
		_objetosRobados.add(obj);
	}
	
	public boolean acaboPelea(final byte equipoMuerto) {
		// equipoMuero = 3, verifica si acabo el combate
		int linea = 0;
		try {
			if (_fase == Constantes.PELEA_FASE_FINALIZADO || Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
				return false;
			}
			boolean equipo1Muerto = true, equipo2Muerto = true;
			linea = 1;
			if (equipoMuerto == 3) {
				for (final Luchador luch : _equipo1.values()) {
					if (luch.esInvocacion()) {
						continue;
					}
					if (!luch.estaMuerto()) {
						equipo1Muerto = false;
						break;
					}
				}
				for (final Luchador luch : _equipo2.values()) {
					if (luch.esInvocacion()) {
						continue;
					}
					if (!luch.estaMuerto()) {
						equipo2Muerto = false;
						break;
					}
				}
			} else {
				equipo1Muerto = equipoMuerto == 1;
				equipo2Muerto = equipoMuerto == 2;
			}
			linea = 2;
			if (equipo1Muerto || equipo2Muerto) {
				linea = 3;
				if (_fase == Constantes.PELEA_FASE_POSICION) {
					antesIniciarPelea();
				}
				linea = 4;
				final String packet = getPanelResultados(equipo1Muerto ? 2 : 1);
				if (equipo1Muerto) {
					_luchInit2.getPreLuchador().sobrevivio();
				} else {
					_luchInit2.getPreLuchador().murio();
				}
				linea = 5;
				mostrarResultados(packet);
				linea = 6;
				if (_salvarMobHeroico) {
					_mapaReal.salvarMapaHeroico();
				}
				linea = 7;
			}
			return equipo1Muerto || equipo2Muerto;
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("Exception acaboPelea - Mapa: " + _mapaCopia.getID() + " PeleaID: " + _id
			+ " Linea: " + linea + ", Exception: " + e.toString());
			e.printStackTrace();
		}
		return false;
	}
	
	public void cancelarPelea() {
		try {
			if (_fase == Constantes.PELEA_FASE_FINALIZADO || Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
				return;
			}
			if (_fase == Constantes.PELEA_FASE_POSICION) {
				antesIniciarPelea();
			}
			final String packet = getPanelResultados(3);
			_luchInit2.getPreLuchador().sobrevivio();
			mostrarResultados(packet);
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("Exception cancelarPelea - Mapa: " + _mapaCopia.getID() + " PeleaID: " + _id
			+ ", Exception: " + e.toString());
			e.printStackTrace();
		}
	}
	
	private void mostrarResultados(String packet) {
		try {
			if (MainServidor.SALVAR_LOGS_TIPO_COMBATE.contains(_tipo)) {
				LOG_COMBATES.append(new Date() + "\t" + _tipo + "\t" + _mapaCopia.getID() + "\t" + packet + "\n");
			}
			Thread.sleep(100 + (300 * _cantUltAfec));
			GestorSalida.ENVIAR_GE_PANEL_RESULTADOS_PELEA(this, 7, packet);
			GestorSalida.ENVIAR_fL_LISTA_PELEAS_AL_MAPA(_mapaReal);
			pararIAs();
			eliminarTimer();
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("Exception mostrarResultados - Mapa: " + _mapaCopia.getID() + " PeleaID: "
			+ _id + ", Exception: " + e.toString());
			e.printStackTrace();
		}
	}
	
	private String getPanelResultados(final int equipoGanador) {
		try {
			// equipoGanador 3 = cancelar la pelea
			if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
				return "";
			}
			if (_fase < Constantes.PELEA_FASE_COMBATE) {
				GestorSalida.ENVIAR_Gc_BORRAR_ESPADA_EN_MAPA(_mapaReal, _id);
			}
			_fase = Constantes.PELEA_FASE_FINALIZADO;
			_mapaReal.borrarPelea(_id);
			final long tiempo = System.currentTimeMillis() - _tiempoCombate;
			final int initID = _idLuchInit1;
			byte tipoX = 0;
			switch (_tipo) {
				case Constantes.PELEA_TIPO_PVP :
				case Constantes.PELEA_TIPO_PRISMA :
				case Constantes.PELEA_TIPO_KOLISEO :
					tipoX = 1;
					break;
				case Constantes.PELEA_TIPO_RECAUDADOR :
					if (equipoGanador == 1) {
						_kamasRobadas += _luchInit2.getRecaudador().getKamas();
						_expRobada += _luchInit2.getRecaudador().getExp();
						if (_objetosRobados == null) {
							_objetosRobados = new ArrayList<Objeto>();
						}
						_objetosRobados.addAll(_luchInit2.getRecaudador().getObjetos());
						_luchInit2.getRecaudador().clearObjetos();
					}
					break;
				case Constantes.PELEA_TIPO_PVM :
					if (_mobGrupo != null) {
						_mobGrupo.setPelea(null);
						if (equipoGanador == 1) {
							_mobGrupo.setBonusEstrellas(MainServidor.INICIO_BONUS_ESTRELLAS_MOBS);
							_mobGrupo.setMuerto(true);
							if (_mobGrupo.esHeroico()) {
								for (int id : _mobGrupo.getObjetosHeroico()) {
									final Objeto obj = Mundo.getObjeto(id);
									if (obj == null) {
										continue;
									}
									addObjetosRobados(obj);
								}
								_kamasRobadas += _mobGrupo.getKamasHeroico();
								_mobGrupo.borrarObjetosHeroico();
								_mobGrupo.setKamasHeroico(0);
								_mapaReal.salvarMapaHeroico();
							}
						}
					}
				case Constantes.PELEA_TIPO_PVM_NO_ESPADA :
					if (_retos != null) {
						if (equipoGanador == 1) {
							for (final Entry<Byte, Reto> entry : _retos.entrySet()) {
								Reto reto = entry.getValue();
								final byte retoID = entry.getKey();
								EstReto exitoReto = reto.getEstado();
								if (exitoReto != Reto.EstReto.EN_ESPERA) {
									continue;
								}
								switch (retoID) {
									case Constantes.RETO_SUPERVIVIENTE : // superviviente
										for (final Luchador luchador : _inicioLuchEquipo1) {
											if (luchador.estaMuerto()) {
												exitoReto = Reto.EstReto.FALLADO;
												break;
											}
										}
										break;
									case Constantes.RETO_REPARTO :// reparto
									case Constantes.RETO_CADA_UNO_CON_SU_MONSTRUO : // cada uno con su mousntro
										for (final Luchador luchador : _inicioLuchEquipo1) {
											if (!luchador.getMobsAsesinadosReto().isEmpty()) {
												exitoReto = Reto.EstReto.FALLADO;
												break;
											}
										}
										break;
								}
								if (exitoReto == Reto.EstReto.EN_ESPERA) {
									exitoReto = Reto.EstReto.REALIZADO;
								}
								reto.setEstado(exitoReto);
							}
						} else if (equipoGanador == 2) {
							for (final Entry<Byte, Reto> entry : _retos.entrySet()) {
								Reto reto = entry.getValue();
								EstReto exitoReto = reto.getEstado();
								if (exitoReto != Reto.EstReto.EN_ESPERA) {
									continue;
								}
								reto.setEstado(Reto.EstReto.FALLADO);
							}
						}
					}
					break;
			}
			for (final Luchador luch : _espectadores.values()) {
				if (luch.estaRetirado()) {
					continue;
				}
				luchadorSalirPelea(luch);
			}
			final StringBuilder packet = new StringBuilder("GE");
			packet.append(tiempo + ";" + _bonusEstrellas + "|" + initID + "|" + tipoX + "|");
			if (equipoGanador == 3) {
				// cancelar la pelea
				final ArrayList<Luchador> cancelados = new ArrayList<Luchador>();
				cancelados.addAll(_equipo1.values());
				cancelados.addAll(_equipo2.values());
				for (final Luchador luch : cancelados) {
					final Personaje pjGanador = luch.getPersonaje();
					if (luch.estaRetirado()) {
						continue;
					}
					// salen todos porq se cancelo
					luchadorSalirPelea(luch);
					if (luch.esInvocacion()) {
						continue;
					}
					if (_tipo != Constantes.PELEA_TIPO_DESAFIO) {
						if (pjGanador != null) {
							pjGanador.setPDV(Math.max(1, luch.getPDVSinBuff()), false);
						}
					}
					if (tipoX == 0) {// PVM -> SIN HONOR
						packet.append("2;" + luch.getID() + ";" + luch.getNombre() + ";" + luch.getNivel() + ";" + (luch
						.estaMuerto() ? "1" : "0") + ";");
						packet.append(luch.xpStringLuch(";") + ";");
						packet.append((luch.getExpGanada() == 0 ? "" : luch.getExpGanada()) + ";");
						packet.append("" + ";");
						packet.append("" + ";");
						packet.append("" + ";");
						packet.append((luch.getKamasGanadas() == 0 ? "" : luch.getKamasGanadas()) + "|");
					} else { // PVP -> CON HONOR
						packet.append("2;" + luch.getID() + ";" + luch.getNombre() + ";" + luch.getNivel());
						packet.append(";" + (luch.estaMuerto() ? 1 : 0) + ";");
						packet.append(stringHonor(luch) + ";");
						packet.append(0 + ";");
						packet.append(luch.getNivelAlineacion() + ";");
						packet.append(luch.getPreLuchador().getDeshonor() + ";");
						packet.append(0 + ";");
						packet.append("" + ";");
						packet.append(luch.getKamasGanadas() + ";");
						packet.append(luch.xpStringLuch(";") + ";");
						packet.append(luch.getExpGanada() + "|");
					}
				}
				return packet.toString();
			}
			// si la pelea no se cancela
			final ArrayList<Luchador> ganadores = new ArrayList<Luchador>();
			final ArrayList<Luchador> perdedores = new ArrayList<Luchador>();
			if (equipoGanador == 1) {
				ganadores.addAll(_equipo1.values());
				perdedores.addAll(_equipo2.values());
			} else if (equipoGanador == 2) {
				ganadores.addAll(_equipo2.values());
				perdedores.addAll(_equipo1.values());
			}
			_mapaReal.aplicarAccionFinPelea(_tipo, ganadores, _acciones);
			for (final Luchador luch : ganadores) {
				if (luch.estaRetirado() || luch.esInvocacion()) {
					continue;
				}
				final Personaje perso = luch.getPersonaje();
				if (perso != null) {
					if (_asesinos.length() > 0) {
						_asesinos.append("~");
					}
					_asesinos.append(perso.getNombre());
				} else if (luch.getMob() != null && !luch.esInvocacion()) {
					if (_asesinos.length() > 0) {
						_asesinos.append("~");
					}
					_asesinos.append(luch.getMob().getIDModelo());
				}
				if (perso == null) {
					continue;
				}
				switch (_tipo) {
					case Constantes.PELEA_TIPO_KOLISEO :
						if (perso != null) {
							RankingKoliseo rank = Mundo.getRankingKoliseo(perso.getID());
							if (rank != null) {
								rank.aumentarVictoria();
							}
							if (perso.getGrupoKoliseo() != null) {
								perso.getGrupoKoliseo().dejarGrupo(perso);
							}
						}
						break;
					case Constantes.PELEA_TIPO_PVP :
						if (perso != null) {
							RankingPVP rank = Mundo.getRankingPVP(perso.getID());
							if (rank != null) {
								rank.aumentarVictoria();
							}
						}
						recompensaMision(luch, true);
						break;
					case Constantes.PELEA_TIPO_PVM :
						if (perso != null) {
							final float balance = Mundo.getBalanceMundo(perso);
							final float bonusExp = Mundo.getBonusAlinExp(perso);
							luch.setBonusAlinExp(balance * bonusExp);
							final float bonusDrop = Mundo.getBonusAlinDrop(perso);
							luch.setBonusAlinDrop(balance * bonusDrop);
							if (MainServidor.PARAM_BESTIARIO) {
								for (final Luchador luchPerdedor : perdedores) {
									if (luchPerdedor.esInvocacion() && luchPerdedor.getMob() == null) {
										continue;
									}
									perso.addCardMob(luchPerdedor.getMob().getIDModelo());
								}
							}
						}
						break;
					case Constantes.PELEA_TIPO_PRISMA :
						GestorSalida.ENVIAR_CP_INFO_DEFENSORES_PRISMA(perso, _listadefensores);
						break;
					case Constantes.PELEA_TIPO_RECAUDADOR :
						GestorSalida.ENVIAR_gITP_INFO_DEFENSORES_RECAUDADOR(perso, _listadefensores);
						break;
				}
				if (_tipo != Constantes.PELEA_TIPO_DESAFIO) {
					if (perso != null) {
						perso.setPDV(Math.max(1, luch.getPDVSinBuff()), false);
					}
				}
			}
			for (final Luchador luch : perdedores) {
				if (luch.estaRetirado() || luch.esInvocacion()) {
					continue;
				}
				robarPersonajePerdedor(luch);
				final Personaje perso = luch.getPersonaje();
				if (perso == null) {
					continue;
				}
				switch (_tipo) {
					case Constantes.PELEA_TIPO_KOLISEO :
						if (perso != null) {
							RankingKoliseo rank = Mundo.getRankingKoliseo(perso.getID());
							if (rank != null) {
								rank.aumentarDerrota();
							}
							if (perso.getGrupoKoliseo() != null) {
								perso.getGrupoKoliseo().dejarGrupo(perso);
							}
						}
						break;
					case Constantes.PELEA_TIPO_PVP :
						if (perso != null) {
							RankingPVP rank = Mundo.getRankingPVP(perso.getID());
							if (rank != null) {
								rank.aumentarDerrota();
							}
						}
						recompensaMision(luch, false);
						break;
					case Constantes.PELEA_TIPO_PRISMA :
						GestorSalida.ENVIAR_CP_INFO_DEFENSORES_PRISMA(perso, _listadefensores);
						break;
					case Constantes.PELEA_TIPO_RECAUDADOR :
						GestorSalida.ENVIAR_gITP_INFO_DEFENSORES_RECAUDADOR(perso, _listadefensores);
						break;
				}
			}
			long minkamas = 0, maxkamas = 0;
			float coefEstrellas = 0, coefRetoDrop = 0, coefRetoXP = 0;
			if (MainServidor.PARAM_PERMITIR_BONUS_ESTRELLAS && _bonusEstrellas > 0) {
				coefEstrellas = _bonusEstrellas / 100f;
			}
			Luchador lucConMaxPP = null;
			ArrayList<Objeto> dropRobado = null;
			boolean mobCapturable = true, monturaSalvaje = false;
			// piedras
			//
			for (final Luchador luchGanador : ganadores) {
				if (luchGanador.esDoble()) {
					continue;
				}
				if (luchGanador.esInvocacion()) {
					if (luchGanador.getMob() != null && luchGanador.getMob().getIDModelo() != 285) {// cofre
						continue;
					}
				}
				int prospeccionLuchador = luchGanador.getTotalStats().getTotalStatConComplemento(
				Constantes.STAT_MAS_PROSPECCION);
				float coefPPLuchador = 1;
				final Personaje pjGanador = luchGanador.getPersonaje();
				if (pjGanador != null) {
					if (_tipo == Constantes.PELEA_TIPO_PVM || _tipo == Constantes.PELEA_TIPO_PVM_NO_ESPADA) {
						final Objeto mascObj = pjGanador.getObjPosicion(Constantes.OBJETO_POS_MASCOTA);
						boolean comio = false;
						if (mascObj != null && mascObj.esDevoradorAlmas()) {
							for (final Entry<Integer, Integer> entry : _mobGrupo.getAlmasMobs().entrySet()) {
								try {
									if (Mundo.getMascotaModelo(mascObj.getObjModeloID()).getComida(entry.getKey()) != null) {
										comio = true;
										mascObj.comerAlma(entry.getKey(), entry.getValue());
									}
								} catch (final Exception e) {}
							}
							if (comio) {
								GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(pjGanador, mascObj);
							}
						}
						int[] tt = {MisionObjetivoModelo.VENCER_AL_MOB, MisionObjetivoModelo.VENCER_MOBS_UN_COMBATE};
						pjGanador.verificarMisionesTipo(tt, _mobGrupo.getAlmasMobs(), false, 0);
					}
					if (pjGanador.realizoMisionDelDia()) {
						Almanax almanax = Mundo.getAlmanaxDelDia();
						if (almanax != null && almanax.getTipo() == Constantes.ALMANAX_BONUS_DROP) {
							coefPPLuchador += almanax.getBonus() / 100f;
						}
					}
					if (pjGanador.alasActivadas() && _alinPelea == pjGanador.getAlineacion()) {
						coefPPLuchador += luchGanador.getBonusAlinDrop() / 100f;
					}
					if (MainServidor.RATE_DROP_ABONADOS > 1) {
						if (pjGanador.esAbonado()) {
							coefPPLuchador += MainServidor.RATE_DROP_ABONADOS / 2f;
						}
					}
				}
				if (MainServidor.PARAM_PERMITIR_BONUS_PELEA_AFECTEN_PROSPECCION) {
					coefPPLuchador += coefEstrellas + coefRetoDrop;
				}
				prospeccionLuchador *= coefPPLuchador;
				luchGanador.setProspeccion(prospeccionLuchador);
				_prospeccionEquipo += prospeccionLuchador;
				// luchGanador.setPorcAdicDrop((int) (prospeccionLuchador * coefDropBonus));
			}
			if (_prospeccionEquipo < 1) {
				_prospeccionEquipo = 1;
			}
			if (equipoGanador == 1) {
				if (_tipo == Constantes.PELEA_TIPO_PVM) {
					for (final Luchador luchPerdedor : perdedores) {
						try {
							if (luchPerdedor.getMob() == null) {
								mobCapturable = false;
								break;
							}
							final MobModelo mobModelo = luchPerdedor.getMob().getMobModelo();
							if (mobModelo.getID() == 171 || mobModelo.getID() == 200 || mobModelo.getID() == 666) {
								monturaSalvaje = true;
							}
							mobCapturable &= mobModelo.esCapturable();
						} catch (final Exception e) {
							mobCapturable = false;
						}
					}
					if ((monturaSalvaje || mobCapturable) && !_mapaReal.esArena()) {
						int maxNivel = 0;
						final StringBuilder piedraStats = new StringBuilder();
						int monturaID = 0;
						for (final Luchador luchGanador : ganadores) {
							if (luchGanador.tieneEstado(Constantes.ESTADO_CAPT_ALMAS)) {
								if (_capturadores == null) {
									_capturadores = new ArrayList<>(8);
								}
								_capturadores.add(luchGanador);
							}
							if (luchGanador.tieneEstado(Constantes.ESTADO_DOMESTICACIÓN)) {
								if (_domesticadores == null) {
									_domesticadores = new ArrayList<>(8);
								}
								_domesticadores.add(luchGanador);
							}
						}
						if (_capturadores == null || _capturadores.isEmpty()) {
							mobCapturable = false;
						}
						if (_domesticadores == null || _domesticadores.isEmpty()) {
							monturaSalvaje = false;
						}
						int objPiedraModID = 7010;
						if (monturaSalvaje || mobCapturable) {
							for (final Luchador luchPerdedor : perdedores) {
								try {
									MobGrado mob = luchPerdedor.getMob();
									if (luchPerdedor.getMob() == null) {
										continue;
									}
									final int m = luchPerdedor.getMob().getIDModelo();
									if (monturaSalvaje) {
										if (m == 171 || m == 200 || m == 666) {
											if (monturaID == 0 || Formulas.getRandomBoolean()) {
												monturaID = m;
											}
										}
									}
									if (mobCapturable) {
										switch (mob.getMobModelo().getTipoMob()) {
											case Constantes.MOB_TIPO_LOS_ARCHIMONSTRUOS :
												if (objPiedraModID == 7010) {
													objPiedraModID = 10418;
												}
												break;
										}
										if (mob.getMobModelo().getID() == 423) {
											objPiedraModID = 9720;
										}
										if (piedraStats.length() > 0) {
											piedraStats.append(",");
										}
										piedraStats.append(Integer.toHexString(Constantes.STAT_INVOCA_MOB) + "#" + Integer.toHexString(
										luchPerdedor.getNivel()) + "#0#" + Integer.toHexString(m));
										if (luchPerdedor.getNivel() > maxNivel) {
											maxNivel = luchPerdedor.getNivel();
										}
									}
								} catch (final Exception e) {}
							}
						}
						if (monturaSalvaje) {
							for (final Luchador luchCapt : _domesticadores) {
								try {
									final Personaje persoCapt = luchCapt.getPersonaje();
									final Objeto redCapt = persoCapt.getObjPosicion(Constantes.OBJETO_POS_ARMA);
									if (redCapt != null && redCapt.getObjModelo().getTipo() == Constantes.OBJETO_TIPO_RED_CAPTURA
									&& persoCapt.getMontura() == null) {
										final int suerteCaptura = luchCapt.getValorPorBuffsID(751) + (MainServidor.RATE_CAPTURA_MONTURA
										* redCapt.getStatValor(Constantes.STAT_DOMESTICAR_MONTURA));
										if (Formulas.getRandomInt(1, 100) <= suerteCaptura) {
											persoCapt.borrarOEliminarConOR(redCapt.getID(), false);
											int color = Constantes.getColorMonturaPorMob(monturaID);
											Montura montura = new Montura(color, luchCapt.getID(), false, true);
											Objeto pergamino = montura.getObjModCertificado().crearObjeto(1,
											Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM);
											if (MainServidor.PARAM_CAPTURAR_MONTURA_COMO_PERGAMINO) {
												pergamino.fijarStatValor(Constantes.STAT_CONSULTAR_MONTURA, Math.abs(montura.getID()));
												pergamino.addStatTexto(Constantes.STAT_PERTENECE_A, "0#0#0#" + luchCapt.getNombre());
												pergamino.addStatTexto(Constantes.STAT_NOMBRE, "0#0#0#" + montura.getNombre());
												montura.setMapaCelda(null, null);
											} else {
												persoCapt.setMontura(montura);
											}
											luchCapt.addDropLuchador(pergamino, MainServidor.PARAM_CAPTURAR_MONTURA_COMO_PERGAMINO);
											break;
										}
									}
								} catch (final Exception e) {}
							}
						}
						if (mobCapturable) {
							for (final Luchador luchCapt : _capturadores) {
								try {// falta agregar al azar
									final Personaje persoCapt = luchCapt.getPersonaje();
									final Objeto piedra = persoCapt.getObjPosicion(Constantes.OBJETO_POS_ARMA);
									if (piedra != null && piedra.getObjModelo().getTipo() == Constantes.OBJETO_TIPO_PIEDRA_DEL_ALMA) {
										int nivelPiedra = Integer.parseInt(piedra.getParamStatTexto(Constantes.STAT_POTENCIA_CAPTURA_ALMA,
										3), 16);
										if (nivelPiedra >= maxNivel) {
											int sPiedra = Integer.parseInt(piedra.getParamStatTexto(Constantes.STAT_POTENCIA_CAPTURA_ALMA, 1),
											16);
											final int suerte = luchCapt.getValorPorBuffsID(Constantes.STAT_BONUS_CAPTURA_ALMA) + sPiedra;
											if (suerte >= Formulas.getRandomInt(1, 100)) {
												persoCapt.borrarOEliminarConOR(piedra.getID(), false);
												luchCapt.addDropLuchador(new Objeto(0, objPiedraModID, 1, Constantes.OBJETO_POS_NO_EQUIPADO,
												piedraStats.toString(), 0, 0), true);
												break;
											}
										}
									}
								} catch (final Exception e) {}
							}
						}
					}
				}
				if ((_tipo == Constantes.PELEA_TIPO_PVM || _tipo == Constantes.PELEA_TIPO_PVM_NO_ESPADA) && _retos != null) {
					for (final Entry<Byte, Reto> entry : _retos.entrySet()) {
						Reto reto = entry.getValue();
						if (reto.getEstado() == Reto.EstReto.REALIZADO) {
							if (MainServidor.PARAM_PERMITIR_BONUS_DROP_RETOS) {
								coefRetoDrop += reto.bonusDrop() / 100f;
							}
							if (MainServidor.PARAM_PERMITIR_BONUS_EXP_RETOS) {
								coefRetoXP += reto.bonusXP() / 100f;
							}
						}
					}
				}
				try {
					int nivelPromMobs = 0;
					if (_tipo == Constantes.PELEA_TIPO_PVM || _tipo == Constantes.PELEA_TIPO_PVM_NO_ESPADA) {
						MobGrado mob;
						int cant = 0;
						for (final Luchador luchPerdedor : perdedores) {
							if (luchPerdedor.esInvocacion() || (mob = luchPerdedor.getMob()) == null) {
								continue;
							}
							if (MainServidor.PARAM_SISTEMA_ORBES) {
								if (MainServidor.MOBS_NO_ORBES.contains(mob.getIDModelo())) {
									continue;
								}
								int cantidad = 0;
								int nivel = mob.getNivel();
								if (nivel >= 1 && nivel <= 50) {
									cantidad += Math.floor(nivel * 0.1);
								} else if (nivel >= 51 && nivel <= 100) {
									cantidad += Math.floor(nivel * 0.13);
								} else if (nivel >= 101 && nivel <= 130) {
									cantidad += Math.floor(nivel * 0.16);
								} else if (nivel >= 131 && nivel <= 150) {
									cantidad += Math.floor(nivel * 0.19);
								} else if (nivel >= 151 && nivel <= 200) {
									cantidad += Math.floor(nivel * 0.21);
								} else {
									cantidad += Math.floor(nivel * 0.25);
								}
								if (MainServidor.MOBS_DOBLE_ORBES.contains(mob.getIDModelo())) {
									cantidad *= 2;
								}
								DropMob drop = new DropMob(MainServidor.ID_ORBE, 0, 99.99f, cantidad, "");
								addDropPelea(drop);
							} else {
								// drops de recursos, objetos, etc normales
								cant++;
								nivelPromMobs += mob.getNivel();
								minkamas += mob.getMobGradoModelo().getMinKamas();
								maxkamas += mob.getMobGradoModelo().getMaxKamas();
								for (final DropMob drop : mob.getMobModelo().getDrops()) {
									if (drop.getProspeccion() == 0 || drop.getProspeccion() <= _prospeccionEquipo) {
										addDropPelea(drop);
									}
								}
							}
						}
						if (cant > 0) {
							nivelPromMobs = nivelPromMobs / cant;
						}
					}
					if (_tipo == Constantes.PELEA_TIPO_PVM) {// drops fijos
						for (final DropMob drop : Mundo.listaDropsFijos()) {
							// armas etereas, materias, dominios
							if (drop.getNivelMin() <= nivelPromMobs && nivelPromMobs <= drop.getNivelMax()) {
								addDropPelea(drop);
							}
						}
					}
				} catch (Exception e) {}
				// hasta aqui acaba todo lo q tiene q ver con ganadores
			}
			final Map<Integer, Luchador> todosConPP = new TreeMap<Integer, Luchador>();
			int prospTemp, tempPP;
			final ArrayList<Luchador> dropeadores = new ArrayList<>();
			final ArrayList<Luchador> ordenLuchMasAMenosPP = new ArrayList<Luchador>();
			for (final Luchador luchGanador : ganadores) {
				prospTemp = luchGanador.getProspeccionLuchador();
				while (todosConPP.containsKey(prospTemp)) {
					prospTemp += 1;
				}
				todosConPP.put(prospTemp, luchGanador);
			}
			while (ordenLuchMasAMenosPP.size() < ganadores.size()) {
				tempPP = -1;
				for (final Entry<Integer, Luchador> entry : todosConPP.entrySet()) {
					if (entry.getKey() > tempPP && !ordenLuchMasAMenosPP.contains(entry.getValue())) {
						lucConMaxPP = entry.getValue();
						tempPP = entry.getKey();
					}
				}
				ordenLuchMasAMenosPP.add(lucConMaxPP);
			}
			if (_objetosRobados != null) {
				for (Objeto obj : _objetosRobados) {
					if (dropRobado == null) {
						dropRobado = new ArrayList<>();
					}
					if (obj.getCantidad() > 1) {
						for (int i = 1; i <= obj.getCantidad(); i++) {
							dropRobado.add(obj.clonarObjeto(1, Constantes.OBJETO_POS_NO_EQUIPADO));
						}
					} else {
						dropRobado.add(obj);
					}
				}
				_objetosRobados.clear();
			}
			// solo para pjs o mobs en heroico
			for (Luchador luch : ordenLuchMasAMenosPP) {
				if (luch.esDoble()) {
					continue;
				}
				if (luch.getPersonaje() != null && luch.getPersonaje().esMultiman()) {
					continue;
				}
				if (luch.esInvocacion()) {
					if (luch.getMob() == null || luch.getMob().getIDModelo() != 285) {// cofre
						continue;
					}
				}
				if (luch.getMob() != null) {
					if (luch.getMob().getIDModelo() == 394) {// caballero
						continue;
					}
				}
				dropeadores.add(luch);
			}
			int ganarHonor = 0, deshonor = 0;
			long xpParaGremio = 0, xpParaMontura = 0;
			int cantGanadores = dropeadores.size();
			StringBuilder strDrops = new StringBuilder();
			// DROPEANDO LOS OBJETOS DE SERVER HEROICO O RECAUDADOR
			// if (MainServidor.MODO_HEROICO ||
			// MainServidor.MAPAS_MODO_HEROICO.contains(_mapaReal.getID())) {
			repartirDropRobado(dropRobado, dropeadores);
			// }
			int tempCantDropeadores = dropeadores.size();
			Recaudador recaudador = _mapaReal.getRecaudador();
			if (equipoGanador == 1) {
				if (_posiblesBotinPelea != null) {
					if (MainServidor.MODO_DEBUG) {
						System.out.println("========== START DROPS PLAYERS ===========");
						System.out.println("PROSPECCION DEL EQUIPO ES " + _prospeccionEquipo);
					}
					for (Botin drop : _posiblesBotinPelea) {
						if (MainServidor.MODO_DEBUG) {
							System.out.println("===========================================");
							System.out.println("Posibilidad Drop (" + drop.getIDObjModelo() + ") " + Mundo.getObjetoModelo(drop
							.getIDObjModelo()).getNombre() + " , MaximoDrop: " + drop.getBotinMaximo() + " , DropFijo: " + drop
							.esDropFijo());
						}
						int maxDrop = drop.getBotinMaximo();
						if (maxDrop <= 0) {
							continue;
						}
						boolean repartido = false;
						if (drop.getProspeccionBotin() == 0) {
							// para q el drop se si o si
							repartido = true;
							if (drop.getPorcentajeBotin() >= 100) {
								// cada jugador dropea la maxima cantidad
								maxDrop *= dropeadores.size();
							}
						}
						int nuevoMaxDrop = maxDrop;
						if (drop.getProspeccionBotin() > 0) {
							nuevoMaxDrop = 0;
							for (int m = 1; m <= maxDrop; m++) {
								float fSuerte = Formulas.getRandomDecimal(3);// 0.001 - 100.000
								// si es drop etero o no
								float fPorc = Formulas.getPorcParaDropAlEquipo(_prospeccionEquipo, coefEstrellas, coefRetoDrop, drop,
								dropeadores.size());
								if (MainServidor.MODO_DEBUG) {
									System.out.println(" -> DropItem: " + drop.getIDObjModelo() + " , %Drop: " + drop.getPorcentajeBotin()
									+ " , %TeamDrop: " + fPorc + " , RandValue: " + fSuerte);
								}
								if (fPorc >= fSuerte) {
									nuevoMaxDrop++;
								}
							}
						}
						if (MainServidor.MODO_DEBUG) {
							System.out.println("Repartiendo drop " + Mundo.getObjetoModelo(drop.getIDObjModelo()).getNombre() + " ("
							+ drop.getIDObjModelo() + ") cantidad " + nuevoMaxDrop);
						}
						int dropsGanados = 0;
						while (dropsGanados < nuevoMaxDrop) {
							if (!drop.getCondicionBotin().isEmpty() || repartido) {
								boolean gano = false;
								boolean pasoCondicion = false;
								for (int j = 1; j <= dropeadores.size(); j++) {
									int k = (tempCantDropeadores + j) % dropeadores.size();
									Luchador posibleDropeador = dropeadores.get(k);
									if (posibleDropeador.getPersonaje() == null || !Condiciones.validaCondiciones(posibleDropeador
									.getPersonaje(), drop.getCondicionBotin())) {
										// si no es personaje y si no cumple la condicion
										if (drop.getProspeccionBotin() == 0 && drop.getPorcentajeBotin() >= 100) {
											dropsGanados++;
										}
										continue;
									}
									pasoCondicion = true;
									float nPorcAzar = Formulas.getRandomDecimal(3);
									float porcDropFinal = Formulas.getPorcDropLuchador(drop.getPorcentajeBotin(), posibleDropeador);
									if (porcDropFinal >= nPorcAzar) {
										posibleDropeador.addDropLuchador(Mundo.getObjetoModelo(drop.getIDObjModelo()).crearObjeto(1,
										Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM), true);
										tempCantDropeadores = k;
										gano = true;
										break;
									}
								}
								if (!pasoCondicion) {
									break;
								}
								if (gano) {
									dropsGanados++;
								}
							} else {// si es etereo o no tiene condicion
								int nPorcAzar = Formulas.getRandomInt(1, _prospeccionEquipo);
								int suma = 0;
								Luchador dropeador = null;
								for (Luchador l : dropeadores) {
									suma += l.getProspeccionLuchador();
									if (suma >= nPorcAzar) {
										dropeador = l;
										break;
									}
								}
								if (dropeador != null) {
									dropeador.addDropLuchador(Mundo.getObjetoModelo(drop.getIDObjModelo()).crearObjeto(1,
									Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM), true);
									dropsGanados++;
								}
							}
						}
						drop.addBotinMaximo(-nuevoMaxDrop);
					}
					if (MainServidor.MODO_DEBUG) {
						System.out.println("========== FINISH DROPS PLAYERS ===========");
					}
				}
				if (_tipo == Constantes.PELEA_TIPO_PVM && recaudador != null) {
					strDrops = new StringBuilder();
					Luchador luchRecau = new Luchador(this, recaudador, true);
					final Gremio gremio = recaudador.getGremio();
					int ppRecau = gremio.getStatRecolecta(Constantes.STAT_MAS_PROSPECCION);
					long expGanada = Formulas.getXPOficial(ganadores, perdedores, luchRecau, coefEstrellas, coefRetoXP,
					equipoGanador == 1);
					long kamasGanadas = Formulas.getKamasGanadas(minkamas, maxkamas, null);
					luchRecau.setKamasGanadas(kamasGanadas);
					luchRecau.setExpGanada(expGanada);
					recaudador.addExp(expGanada);
					recaudador.addKamas(kamasGanadas, null);
					// SI AUN PUEDE DROPEAR
					if (gremio.getStatRecolecta(Constantes.STAT_MAS_PODS) > recaudador.getPodsActuales()) {
						if (_posiblesBotinPelea != null) {
							if (MainServidor.MODO_DEBUG) {
								System.out.println("========== START DROPS RECAUDADOR ===========");
								System.out.println("PROSPECCION RECAUDADOR: " + ppRecau);
							}
							for (Botin drop : _posiblesBotinPelea) {
								int maxDrop = drop.getBotinMaximo();
								if (maxDrop <= 0) {
									continue;
								}
								if (!drop.getCondicionBotin().isEmpty()) {
									continue;
								}
								if (drop.getProspeccionBotin() > 0) {
									for (int m = 1; m <= maxDrop; m++) {
										float fSuerte = Formulas.getRandomDecimal(3);
										float fPorc = Formulas.getPorcParaDropAlEquipo(ppRecau, coefEstrellas, coefRetoDrop, drop, 1);
										if (MainServidor.MODO_DEBUG) {
											System.out.println("DropItem: " + drop.getIDObjModelo() + " , %Drop: " + drop.getPorcentajeBotin()
											+ " , %TeamDrop: " + fPorc + " , RandValue: " + fSuerte);
										}
										if (fPorc >= fSuerte) {
											ObjetoModelo objModelo = Mundo.getObjetoModelo(drop.getIDObjModelo());
											luchRecau.addDropLuchador(objModelo.crearObjeto(1, Constantes.OBJETO_POS_NO_EQUIPADO,
											CAPACIDAD_STATS.RANDOM), true);
										}
									}
								}
							}
							if (MainServidor.MODO_DEBUG) {
								System.out.println("========== FIN DROPS RECAUDADOR ===========");
							}
						}
						if (luchRecau.getObjDropeados() != null) {
							for (Entry<Objeto, Boolean> entry : luchRecau.getObjDropeados().entrySet()) {
								Objeto obj = entry.getKey();
								if (strDrops.length() > 0) {
									strDrops.append(",");
								}
								strDrops.append(obj.getObjModeloID() + "~" + obj.getCantidad());
								if (entry.getValue()) {
									luchRecau.addObjetoAInventario(obj);
								}
							}
						}
					}
					packet.append("5;" + luchRecau.getID() + ";" + luchRecau.getNombre() + ";" + luchRecau.getNivel() + ";"
					+ (luchRecau.estaMuerto() ? "1" : "0") + ";");
					packet.append(luchRecau.xpStringLuch(";") + ";");
					packet.append((luchRecau.getExpGanada() == 0 ? "" : luchRecau.getExpGanada()) + ";");
					packet.append((xpParaGremio == 0 ? "" : xpParaGremio) + ";");
					packet.append((xpParaMontura == 0 ? "" : xpParaMontura) + ";");
					packet.append(strDrops.toString() + ";");
					packet.append((luchRecau.getKamasGanadas() == 0 ? "" : luchRecau.getKamasGanadas()) + "|");
				}
			}
			StringBuilder cuentas_g = null;
			StringBuilder personajes_g = null;
			StringBuilder ips_g = null;
			StringBuilder puntos_g = null;
			StringBuilder cuentas_p = null;
			StringBuilder personajes_p = null;
			StringBuilder ips_p = null;
			StringBuilder puntos_p = null;
			if (MainServidor.PARAM_SALVAR_LOGS_AGRESION_SQL && _tipo == Constantes.PELEA_TIPO_PVP) {
				cuentas_g = new StringBuilder();
				personajes_g = new StringBuilder();
				ips_g = new StringBuilder();
				puntos_g = new StringBuilder();
				cuentas_p = new StringBuilder();
				personajes_p = new StringBuilder();
				ips_p = new StringBuilder();
				puntos_p = new StringBuilder();
			}
			for (final Luchador luchGanador : ganadores) {
				if (luchGanador.esDoble()) {
					continue;
				}
				if (luchGanador.esInvocacion()) {
					if (luchGanador.getMob() != null && luchGanador.getMob().getIDModelo() != 285) {// cofre
						continue;
					}
				}
				strDrops = new StringBuilder();
				final Personaje pjGanador = luchGanador.getPersonaje();
				xpParaGremio = xpParaMontura = ganarHonor = 0;
				if (pjGanador != null && pjGanador.esMultiman()) {
					// multiman no reciven ningun bonus
				} else if (!luchGanador.estaRetirado()) {
					switch (_tipo) {
						case Constantes.PELEA_TIPO_PVP :
						case Constantes.PELEA_TIPO_PRISMA :
							ganarHonor = Formulas.getHonorGanado(ganadores, perdedores, luchGanador, _mobGrupo != null);
							if (_1vs1) {
								int div = 0;
								if (luchGanador.getID() == _luchInit1.getID()) {
									// mientras mas se agrade menos honor se ganar
									try {
										for (long t : luchGanador.getPersonaje().getAgredirA(_luchInit2.getNombre())) {
											if (t + (1000l * 60 * 60 * 24) > System.currentTimeMillis()) {
												div++;
											}
										}
									} catch (Exception e) {}
								} else if (luchGanador.getID() == _luchInit2.getID()) {
									try {
										for (long t : luchGanador.getPersonaje().getAgredidoPor(_luchInit1.getNombre())) {
											if (t + (1000l * 60 * 60 * 24) > System.currentTimeMillis()) {
												div++;
											}
										}
									} catch (Exception e) {}
								}
								if (div < 1) {
									div = 1;
								}
								ganarHonor = ganarHonor / div;
							}
							if (ganarHonor > 0) {
								luchGanador.getPreLuchador().addDeshonor(-1);
								luchGanador.getPreLuchador().addHonor(ganarHonor);
							} else if (ganarHonor < 0) {
								ganarHonor = 0;
							}
							break;
						case Constantes.PELEA_TIPO_PVM :
							if (pjGanador != null) {
								Objeto arma = pjGanador.getObjPosicion(Constantes.OBJETO_POS_ARMA);
								StatOficio oficio = pjGanador.getStatOficioPorID(Constantes.OFICIO_CAZADOR);
								if (arma != null && oficio != null && oficio.getOficio().esHerramientaValida(arma.getObjModeloID())) {
									int nivelOficio = oficio.getNivel();
									for (Luchador mob : perdedores) {
										try {
											if (mob.esInvocacion() || mob.getMob() == null)
												continue;
											int carne = Constantes.getCarnePorMob(mob.getMob().getIDModelo(), nivelOficio);
											if (carne != -1) {
												int cant = Formulas.getRandomInt(1, Math.max(1, luchGanador.getProspeccionLuchador() / 100));
												luchGanador.addDropLuchador(Mundo.getObjetoModelo(carne).crearObjeto(cant,
												Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM), true);
											}
										} catch (Exception e) {}
									}
								}
							}
							// no lleva break porq continua la formula
						case Constantes.PELEA_TIPO_PVM_NO_ESPADA :
							luchGanador.setKamasGanadas(luchGanador.getKamasGanadas() + Formulas.getKamasGanadas(minkamas, maxkamas,
							luchGanador.getPersonaje()));
							if (pjGanador != null) {
								long expGanada = Formulas.getXPOficial(ganadores, perdedores, luchGanador, coefEstrellas, coefRetoXP,
								equipoGanador == 1);
								luchGanador.setExpGanada(luchGanador.getExpGanada() + expGanada);
								if (pjGanador.realizoMisionDelDia()) {
									Almanax almanax = Mundo.getAlmanaxDelDia();
									if (almanax != null) {
										if (almanax.getTipo() == Constantes.ALMANAX_BONUS_KAMAS) {
											luchGanador.setKamasGanadas(luchGanador.getKamasGanadas() + luchGanador.getKamasGanadas()
											* almanax.getBonus() / 100);
										}
										if (almanax.getTipo() == Constantes.ALMANAX_BONUS_EXP_PJ) {
											long expBonus = luchGanador.getExpGanada() * almanax.getBonus() / 100;
											luchGanador.setExpGanada(luchGanador.getExpGanada() + expBonus);
										}
									}
								}
								if (pjGanador.alasActivadas() && _alinPelea == pjGanador.getAlineacion()) {
									long expBonus = (long) (luchGanador.getExpGanada() * luchGanador.getBonusAlinExp() / 100);
									luchGanador.setExpGanada(luchGanador.getExpGanada() + expBonus);
									long kamasBonus = (long) (luchGanador.getKamasGanadas() * luchGanador.getBonusAlinDrop() / 100);
									luchGanador.setKamasGanadas(luchGanador.getKamasGanadas() + kamasBonus);
								}
								if (pjGanador.getMiembroGremio() != null) {
									xpParaGremio = (long) (luchGanador.getExpGanada() * pjGanador.getMiembroGremio().getPorcXpDonada()
									/ 100f);
									luchGanador.setExpGanada(luchGanador.getExpGanada() - xpParaGremio);
									if (xpParaGremio > 0) {
										xpParaGremio = Formulas.getXPDonada(pjGanador.getNivel(), pjGanador.getMiembroGremio().getGremio()
										.getNivel(), xpParaGremio);
										pjGanador.getMiembroGremio().darXpAGremio(xpParaGremio);
									}
								}
								if (pjGanador.getMontura() != null) {
									xpParaMontura = (luchGanador.getExpGanada() * pjGanador.getPorcXPMontura() / 100);
									xpParaMontura = xpParaMontura * pjGanador.getMontura().velocidadAprendizaje() / 100;
									luchGanador.setExpGanada(luchGanador.getExpGanada() - xpParaMontura);
									if (xpParaMontura > 0) {
										xpParaMontura = Formulas.getXPDonada(pjGanador.getNivel(), pjGanador.getMontura().getNivel(),
										xpParaMontura) * MainServidor.RATE_XP_MONTURA;
										pjGanador.getMontura().addExperiencia(xpParaMontura);
									}
									GestorSalida.ENVIAR_Re_DETALLES_MONTURA(pjGanador, "+", pjGanador.getMontura());
								}
							}
							break;
						case Constantes.PELEA_TIPO_RECAUDADOR :
							if (pjGanador != null) {
								if (pjGanador.getMiembroGremio() != null) {
									xpParaGremio = luchGanador.getExpGanada();
									luchGanador.setExpGanada(0);
									if (xpParaGremio > 0) {
										xpParaGremio = Formulas.getXPDonada(pjGanador.getNivel(), pjGanador.getMiembroGremio().getGremio()
										.getNivel(), xpParaGremio);
										pjGanador.getMiembroGremio().darXpAGremio(xpParaGremio);
									}
								}
							}
							break;
						case Constantes.PELEA_TIPO_KOLISEO :
							if (pjGanador != null) {
								luchGanador.setExpGanada(luchGanador.getExpGanada() + Formulas.getXPMision(pjGanador.getNivel())
								/ MainServidor.KOLISEO_DIVISOR_XP);
								if (MainServidor.KOLISEO_PREMIO_KAMAS > 0) {
									luchGanador.setKamasGanadas(luchGanador.getKamasGanadas() + Formulas.getKamasKoliseo(pjGanador
									.getNivel()));
								}
								for (String s : MainServidor.KOLISEO_PREMIO_OBJETOS.split(";")) {
									try {
										int objID = Integer.parseInt(s.split(",")[0]);
										int cant = Integer.parseInt(s.split(",")[1]);
										luchGanador.addDropLuchador(Mundo.getObjetoModelo(objID).crearObjeto(cant,
										Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM), true);
									} catch (Exception e) {}
								}
							}
							break;
						case Constantes.PELEA_TIPO_CACERIA :
							if (pjGanador != null) {
								try {
									String[] str = Mundo.KAMAS_OBJ_CACERIA.split(Pattern.quote("|"));
									luchGanador.setKamasGanadas(luchGanador.getKamasGanadas() + (Integer.parseInt(str[0])
									/ cantGanadores));
									if (str.length > 1) {
										for (final String s : str[1].split(";")) {
											try {
												int objID = Integer.parseInt(s.split(",")[0]);
												int cant = Integer.parseInt(s.split(",")[1]);
												luchGanador.addDropLuchador(Mundo.getObjetoModelo(objID).crearObjeto(cant,
												Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM), true);
											} catch (Exception e) {}
										}
									}
								} catch (Exception e) {}
							}
							break;
					}
					luchGanador.addKamasLuchador();
					// AQUI CONVIERTE LOS OBJ DROPS AL INVENTARIO DE CADA GANADOR
					if (luchGanador.getObjDropeados() != null) {
						for (Entry<Objeto, Boolean> entry : luchGanador.getObjDropeados().entrySet()) {
							Objeto obj = entry.getKey();
							if (strDrops.length() > 0) {
								strDrops.append(",");
							}
							strDrops.append(obj.getObjModeloID() + "~" + obj.getCantidad());
							if (entry.getValue()) {
								luchGanador.addObjetoAInventario(obj);
							}
						}
						Personaje recibidor = pjGanador;
						if (luchGanador.esInvocacion()) {
							recibidor = luchGanador.getInvocador().getPersonaje();
						}
						if (recibidor != null) {
							if (recibidor.enLinea()) {
								StringBuilder oako = new StringBuilder();
								for (Entry<Objeto, Boolean> entry : recibidor.getDropsPelea().entrySet()) {
									if (entry.getValue()) {
										oako.append(entry.getKey().stringObjetoConGuiño());
									} else {
										GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(recibidor, entry.getKey());
									}
								}
								if (oako.length() > 0) {
									GestorSalida.ENVIAR_OAKO_APARECER_MUCHOS_OBJETOS(recibidor, oako.toString());
								}
								GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(recibidor);
							}
							recibidor.getDropsPelea().clear();
						}
					}
					if (pjGanador != null) {
						if (esMapaHeroico()) {
							if (pjGanador.getNivel() < pjGanador.getUltimoNivel()) {
								luchGanador.setExpGanada(luchGanador.getExpGanada() * 2);
							}
						}
						pjGanador.addExperiencia(luchGanador.getExpGanada(), (tipoX == 1));
					}
				}
				packet.append("2;" + luchGanador.getID() + ";" + luchGanador.getNombre() + ";" + luchGanador.getNivel());
				packet.append(";" + (luchGanador.estaMuerto() ? 1 : 0) + ";");
				if (tipoX == 0) {// PVM -> SIN HONOR
					packet.append(luchGanador.xpStringLuch(";") + ";");
					packet.append((luchGanador.getExpGanada() == 0 ? "" : luchGanador.getExpGanada()) + ";");
					packet.append((xpParaGremio == 0 ? "" : xpParaGremio) + ";");
					packet.append((xpParaMontura == 0 ? "" : xpParaMontura) + ";");
					packet.append(strDrops.toString() + ";");
					packet.append((luchGanador.getKamasGanadas() == 0 ? "" : luchGanador.getKamasGanadas()) + "|");
				} else { // PVP -> CON HONOR
					if (MainServidor.PARAM_SALVAR_LOGS_AGRESION_SQL && _tipo == Constantes.PELEA_TIPO_PVP) {
						if (luchGanador.getPersonaje() != null && luchGanador.getPersonaje().getCuenta() != null) {
							if (cuentas_g.length() > 0) {
								cuentas_g.append(",");
								personajes_g.append(",");
								ips_g.append(",");
								puntos_g.append(",");
							}
							cuentas_g.append(luchGanador.getPersonaje().getCuentaID());
							personajes_g.append(luchGanador.getID());
							ips_g.append(luchGanador.getPersonaje().getCuenta().getActualIP());
							puntos_g.append(ganarHonor);
						}
					}
					packet.append(stringHonor(luchGanador) + ";");
					packet.append(ganarHonor + ";");
					packet.append(luchGanador.getNivelAlineacion() + ";");
					packet.append(luchGanador.getPreLuchador().getDeshonor() + ";");
					packet.append(deshonor + ";");
					packet.append(strDrops.toString() + ";");
					packet.append(luchGanador.getKamasGanadas() + ";");
					packet.append(luchGanador.xpStringLuch(";") + ";");
					packet.append(luchGanador.getExpGanada() + "|");
				}
			}
			// -----------
			// PERDODORES
			// -----------
			for (final Luchador luchPerdedor : perdedores) {
				if (luchPerdedor.esDoble()) {
					continue;
				}
				if (luchPerdedor.esInvocacion()) {
					if (luchPerdedor.getMob() != null && luchPerdedor.getMob().getIDModelo() != 285) {// cofre
						continue;
					}
				}
				strDrops = new StringBuilder();
				final Personaje pjPerdedor = luchPerdedor.getPersonaje();
				xpParaGremio = xpParaMontura = ganarHonor = 0;
				if (pjPerdedor != null) {
					if (pjPerdedor.esMultiman()) {
						// multiman no reciven ningun bonus
					} else if (!luchPerdedor.estaRetirado()) {
						switch (_tipo) {
							case Constantes.PELEA_TIPO_PVP :
							case Constantes.PELEA_TIPO_PRISMA :
								ganarHonor = Formulas.getHonorGanado(ganadores, perdedores, luchPerdedor, _mobGrupo != null);
								if (_1vs1) {
									int div = 0;
									if (luchPerdedor.getID() == _luchInit1.getID()) {
										try {
											for (long t : luchPerdedor.getPersonaje().getAgredirA(_luchInit2.getNombre())) {
												if (t + (1000 * 60 * 60 * 24) > System.currentTimeMillis()) {
													div++;
												}
											}
										} catch (Exception e) {}
									} else if (luchPerdedor.getID() == _luchInit2.getID()) {
										try {
											for (long t : luchPerdedor.getPersonaje().getAgredidoPor(_luchInit1.getNombre())) {
												if (t + (1000 * 60 * 60 * 24) > System.currentTimeMillis()) {
													div++;
												}
											}
										} catch (Exception e) {}
									}
									if (div < 1) {
										div = 1;
									}
									ganarHonor = ganarHonor / div;
								}
								if (ganarHonor < 0) {
									luchPerdedor.getPreLuchador().addHonor(ganarHonor);
								}
								break;
							case Constantes.PELEA_TIPO_PVM :
								long expGanada = Formulas.getXPOficial(perdedores, ganadores, luchPerdedor, coefEstrellas, coefRetoXP,
								equipoGanador == 1);
								luchPerdedor.setExpGanada(expGanada);
								break;
						}
						if (pjPerdedor.getMiembroGremio() != null) {
							xpParaGremio = (long) (luchPerdedor.getExpGanada() * pjPerdedor.getMiembroGremio().getPorcXpDonada()
							/ 100f);
							luchPerdedor.setExpGanada(luchPerdedor.getExpGanada() - xpParaGremio);
							if (xpParaGremio > 0) {
								xpParaGremio = Formulas.getXPDonada(pjPerdedor.getNivel(), pjPerdedor.getMiembroGremio().getGremio()
								.getNivel(), xpParaGremio);
								pjPerdedor.getMiembroGremio().darXpAGremio(xpParaGremio);
							}
						}
						if (pjPerdedor.getMontura() != null) {
							xpParaMontura = (long) (luchPerdedor.getExpGanada() * pjPerdedor.getPorcXPMontura() / 100f);
							xpParaMontura = xpParaMontura * pjPerdedor.getMontura().velocidadAprendizaje() / 100;
							luchPerdedor.setExpGanada(luchPerdedor.getExpGanada() - xpParaMontura);
							if (xpParaMontura > 0) {
								xpParaMontura = Formulas.getXPDonada(pjPerdedor.getNivel(), pjPerdedor.getMontura().getNivel(),
								xpParaMontura) * MainServidor.RATE_XP_MONTURA;
								pjPerdedor.getMontura().addExperiencia(xpParaMontura);
							}
							GestorSalida.ENVIAR_Re_DETALLES_MONTURA(pjPerdedor, "+", pjPerdedor.getMontura());
						}
						if (esMapaHeroico()) {
							if (pjPerdedor.getNivel() < pjPerdedor.getUltimoNivel()) {
								luchPerdedor.setExpGanada(luchPerdedor.getExpGanada() * 2);
							}
						}
						pjPerdedor.addExperiencia(luchPerdedor.getExpGanada(), tipoX == 1);
						pjPerdedor.addKamas(luchPerdedor.getKamasGanadas(), false, false);
					}
				}
				packet.append("0;" + luchPerdedor.getID() + ";" + luchPerdedor.getNombre() + ";" + luchPerdedor.getNivel());
				packet.append(";" + (luchPerdedor.estaMuerto() ? 1 : 0) + ";");
				if (tipoX == 0) {// PVM -> SIN HONOR
					packet.append(luchPerdedor.xpStringLuch(";") + ";");
					packet.append((luchPerdedor.getExpGanada() == 0 ? "" : luchPerdedor.getExpGanada()) + ";");
					packet.append((xpParaGremio == 0 ? "" : xpParaGremio) + ";");
					packet.append((xpParaMontura == 0 ? "" : xpParaMontura) + ";");
					packet.append(strDrops.toString() + ";");
					packet.append((luchPerdedor.getKamasGanadas() == 0 ? "" : luchPerdedor.getKamasGanadas()) + "|");
				} else {// PVP -> CON HONOR
					if (MainServidor.PARAM_SALVAR_LOGS_AGRESION_SQL && _tipo == Constantes.PELEA_TIPO_PVP) {
						if (luchPerdedor.getPersonaje() != null && luchPerdedor.getPersonaje().getCuenta() != null) {
							if (cuentas_p.length() > 0) {
								cuentas_p.append(",");
								personajes_p.append(",");
								ips_p.append(",");
								puntos_p.append(",");
							}
							cuentas_p.append(luchPerdedor.getPersonaje().getCuentaID());
							personajes_p.append(luchPerdedor.getID());
							ips_p.append(luchPerdedor.getPersonaje().getCuenta().getActualIP());
							puntos_p.append(ganarHonor);
						}
					}
					packet.append(stringHonor(luchPerdedor) + ";");
					packet.append(ganarHonor + ";");
					packet.append(luchPerdedor.getNivelAlineacion() + ";");
					packet.append(luchPerdedor.getPreLuchador().getDeshonor() + ";");
					packet.append(deshonor + ";");
					packet.append(strDrops.toString());
					packet.append(";" + luchPerdedor.getKamasGanadas() + ";");
					packet.append(luchPerdedor.xpStringLuch(";") + ";");
					packet.append(luchPerdedor.getExpGanada() + "|");
				}
			}
			if (MainServidor.PARAM_SALVAR_LOGS_AGRESION_SQL && _tipo == Constantes.PELEA_TIPO_PVP) {
				GestorSQL.INSERT_LOG_PELEA(cuentas_g.toString(), personajes_g.toString(), ips_g.toString(), puntos_g.toString(),
				cuentas_p.toString(), personajes_p.toString(), ips_p.toString(), puntos_p.toString(), tiempo / 1000, _luchInit1
				.getID(), _luchInit2.getID(), _mapaCopia.getID());
			}
			if (_tipo == Constantes.PELEA_TIPO_PVM) {
				_mobGrupo.puedeTimerReaparecer(_mapaReal, _mobGrupo, Aparecer.FINAL_PELEA);
			}
			if (equipoGanador == 1) {
				if (_tipo == Constantes.PELEA_TIPO_CACERIA) {
					Mundo.NOMBRE_CACERIA = "";
					Mundo.KAMAS_OBJ_CACERIA = "";
					if (MainServidor.SEGUNDOS_REBOOT_SERVER > 0) {
						Mundo.SEG_CUENTA_REGRESIVA = MainServidor.SEGUNDOS_REBOOT_SERVER;
					}
					Mundo.MSJ_CUENTA_REGRESIVA = MainServidor.MENSAJE_TIMER_REBOOT;
					GestorSalida.ENVIAR_bRS_PARAR_CUENTA_REGRESIVA_TODOS();
				}
			}
			for (final Luchador luch : ganadores) {
				if (luch.estaRetirado() || luch.esInvocacion()) {
					continue;
				}
				luchadorSalirPelea(luch);
			}
			for (final Luchador luch : perdedores) {
				if (luch.estaRetirado() || luch.esInvocacion()) {
					continue;
				}
				consecuenciasPerder(luch);
			}
			return packet.toString();
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("Exception PanelResultados - Mapa: " + _mapaCopia.getID() + " PeleaID: " + _id
			+ " e -> " + e.toString());
			e.printStackTrace();
			return "EXCEPTION";
		}
	}
	
	private void repartirDropRobado(ArrayList<Objeto> dropRobado, ArrayList<Luchador> dropeadores) {
		switch (_tipo) {
			case Constantes.PELEA_TIPO_DESAFIO :
			case Constantes.PELEA_TIPO_KOLISEO :
				break;
			default :// diferentes a 0 y 6
				// REPARTIR ITEMS ROBADOS A LOS JUGADORES
				if (dropRobado != null) {
					for (Objeto obj : dropRobado) {
						int nPorcAzar = Formulas.getRandomInt(1, _prospeccionEquipo);
						int suma = 0;
						Luchador dropeador = null;
						for (Luchador l : dropeadores) {
							suma += l.getProspeccionLuchador();
							if (suma >= nPorcAzar) {
								dropeador = l;
								break;
							}
						}
						if (dropeador != null) {
							dropeador.addDropLuchador(obj, true);
						}
					}
				}
				for (Luchador luchGanador : dropeadores) {
					// esto es solo para el grupo de mobs y otros
					float fParte = (luchGanador.getProspeccionLuchador() / (float) _prospeccionEquipo);
					fParte = Math.min(1, Math.max(0, fParte));
					if (_kamasRobadas > 0) {
						luchGanador.addKamasGanadas((long) (_kamasRobadas * fParte));
					}
					if (_expRobada > 0) {
						luchGanador.addXPGanada((long) (_expRobada * fParte));
					}
				}
				break;
		}
	}
	
	private String stringHonor(Luchador luchGanador) {
		switch (luchGanador.getAlineacion()) {
			case Constantes.ALINEACION_BONTARIANO :
			case Constantes.ALINEACION_BRAKMARIANO :
			case Constantes.ALINEACION_MERCENARIO :
				int nivelA = luchGanador.getNivelAlineacion();
				return Mundo.getExpAlineacion(nivelA) + ";" + luchGanador.getPreLuchador().getHonor() + ";" + Mundo
				.getExpAlineacion(nivelA + 1);
			default :
				return "0;0;0";
		}
	}
	
	public void addIA(Inteligencia IA) {
		_IAs.add(IA);
	}
	
	private void pararIAs() {
		for (Inteligencia ia : _IAs) {
			ia.parar();
		}
	}
	
	public void addAccion(final Accion accion) {
		if (_acciones == null) {
			_acciones = new ArrayList<Accion>();
		}
		_acciones.add(accion);
	}
	
	public String getStrAcciones() {
		if (_acciones == null) {
			return "";
		}
		final StringBuilder str = new StringBuilder();
		for (final Accion accion : _acciones) {
			str.append("\nAccion ID: " + accion.getID() + ", Arg: " + accion.getArgs());
		}
		return str.toString();
	}
	
	public byte getParamMiEquipo(final int id) {
		if (_equipo1.containsKey(id)) {
			return 1;
		}
		if (_equipo2.containsKey(id)) {
			return 2;
		}
		if (_espectadores.containsKey(id)) {
			return 4;
		}
		return -1;
	}
	
	public byte getParamEquipoEnemigo(final int id) {
		if (_equipo1.containsKey(id)) {
			return 2;
		}
		if (_equipo2.containsKey(id)) {
			return 1;
		}
		return -1;
	}
	
	// incluye espectadores
	public Luchador getLuchadorPorID(final int id) {
		if (_equipo1.get(id) != null) {
			return _equipo1.get(id);
		}
		if (_equipo2.get(id) != null) {
			return _equipo2.get(id);
		}
		if (_espectadores.get(id) != null) {
			return _espectadores.get(id);
		}
		return null;
	}
	
	public boolean esEspectador(final int id) {
		return _espectadores.containsKey(id);
	}
	
	public Luchador getLuchadorTurno() {
		return _luchadorDeTurno;
	}
	
	private Luchador getLuchadorOrden() {
		try {
			if (_nroOrdenLuc < 0 || _nroOrdenLuc >= _ordenLuchadores.size()) {
				_nroOrdenLuc = 0;
			}
			return _ordenLuchadores.get(_nroOrdenLuc);
		} catch (final Exception e) {
			return null;
		}
	}
	
	private void luchadorSalirPelea(Luchador luch) {
		if (luch == null) {
			return;
		}
		luch.getTotalStats().clearBuffStats();
		Personaje perdedor = luch.getPersonaje();
		if (perdedor == null) {
			return;
		}
		perdedor.salirPelea(false, false);
	}
	
	private boolean esMapaHeroico() {
		return MainServidor.MODO_HEROICO || MainServidor.MAPAS_MODO_HEROICO.contains(_mapaReal.getID());
	}
	
	private void consecuenciasPerder(Luchador luch) {
		if (luch == null) {
			return;
		}
		luch.getTotalStats().clearBuffStats();
		Personaje pjPerdedor = luch.getPersonaje();
		if (pjPerdedor == null) {
			return;
		}
		if (esMapaHeroico()) {
			// switch (_tipo) {
			// // case CentroInfo.PELEA_TIPO_DESAFIO :
			// case Constantes.PELEA_TIPO_KOLISEO :
			// break;
			// default :
			// pjPerdedor.salirPelea(false, false);
			// robarPersonajePerdedor(luch);
			// break;
			// }
			pjPerdedor.salirPelea(false, false);
			robarPersonajePerdedor(luch);
		} else {
			switch (_tipo) {
				case Constantes.PELEA_TIPO_DESAFIO :
					break;
				case Constantes.PELEA_TIPO_PVP :
					pjPerdedor.addEnergiaConIm(-10 * pjPerdedor.getNivel(), true);
					pjPerdedor.setPDV(1, false);
					imprimiAsesinos(luch);
					break;
				case Constantes.PELEA_TIPO_RECAUDADOR :
					pjPerdedor.addEnergiaConIm(-3000, true);
					pjPerdedor.setPDV(1, false);
					break;
				case Constantes.PELEA_TIPO_KOLISEO :
					pjPerdedor.setPDV(1, false);
					imprimiAsesinos(luch);
					break;
				default :
					pjPerdedor.addEnergiaConIm(-10 * pjPerdedor.getNivel(), true);
					pjPerdedor.setPDV(1, false);
					pjPerdedor.restarVidaMascota(null);
					break;
			}
			pjPerdedor.salirPelea(_tipo != Constantes.PELEA_TIPO_DESAFIO, false);
		}
	}
	
	private void imprimiAsesinos(Luchador luch) {
		Personaje pjPerdedor = luch.getPersonaje();
		if (pjPerdedor == null || !luch.getMsjMuerto()) {
			return;
		}
		if (esMapaHeroico()) {
			if (!MainServidor.PARAM_MENSAJE_ASESINOS_HEROICO) {
				return;
			}
		} else {
			switch (_tipo) {
				case Constantes.PELEA_TIPO_KOLISEO :
					if (!MainServidor.PARAM_MENSAJE_ASESINOS_KOLISEO) {
						return;
					}
					break;
				case Constantes.PELEA_TIPO_PVP :
					if (!MainServidor.PARAM_MENSAJE_ASESINOS_PVP) {
						return;
					}
					break;
				default :
					return;
			}
		}
		luch.setMsjMuerto(true);
		if (_asesinos.toString().isEmpty()) {
			GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1776;" + pjPerdedor.getNombre());
		} else {
			GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1777;" + pjPerdedor.getNombre() + "~" + _asesinos.toString());
		}
	}
	
	public synchronized void retirarsePelea(final int idRetirador, final int idExpulsado, boolean obligado) {
		final Luchador luchRetirador = getLuchadorPorID(idRetirador);
		final Luchador luchExpulsado = getLuchadorPorID(idExpulsado);
		if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE || luchRetirador == null) {
			return;
		}
		_cantUltAfec = 1;
		if (!esEspectador(idRetirador)) {
			switch (_fase) {
				case Constantes.PELEA_FASE_COMBATE :// empezo pelea
					Personaje persoRetirador = luchRetirador.getPersonaje();
					if (!obligado) {
						// 5 segundos para q se pueda retirar
						if ((System.currentTimeMillis() - _tiempoCombate) < 5000) {
							GestorSalida.ENVIAR_BN_NADA(persoRetirador, "ESPERAR 5 SEG");
							return;
						}
					}
					if (luchRetirador.estaRetirado()) {
						_espectadores.remove(idRetirador);
						luchadorSalirPelea(luchRetirador);
						GestorSalida.ENVIAR_GV_RESETEAR_PANTALLA_JUEGO(persoRetirador);
						return;
					}
					if (persoRetirador.getCompañero() != null && !persoRetirador.esMultiman()) {
						int idMultiman = persoRetirador.getCompañero().getID();
						retirarsePelea(idMultiman, idMultiman, true);
					}
					if (addMuertosReturnFinalizo(luchRetirador, null)) {
						// si se finalizo la pelea
						return;
					} else {
						luchRetirador.setEstaRetirado(true);
						if (luchRetirador != null) {
							_listaMuertos.remove(luchRetirador);
						}
						consecuenciasPerder(luchRetirador);
						if (_tipo == Constantes.PELEA_TIPO_PVP || _tipo == Constantes.PELEA_TIPO_PRISMA) {
							luchRetirador.getPreLuchador().addHonor(-500);
						}
						// GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(_mapaCopia, idRetirador);
						if (!persoRetirador.esMultiman()) {
							GestorSalida.ENVIAR_GV_RESETEAR_PANTALLA_JUEGO(persoRetirador);
						}
						if (_tipo == Constantes.PELEA_TIPO_KOLISEO) {
							persoRetirador.setPenalizarKoliseo();
						}
					}
					break;
				case Constantes.PELEA_FASE_POSICION :// pelea en estado posicion
					if (_tipo == Constantes.PELEA_TIPO_PVP && !MainServidor.PARAM_EXPULSAR_PREFASE_PVP) {
						GestorSalida.ENVIAR_BN_NADA(luchRetirador.getPersonaje(), "NO SE PUEDE EXPULSAR PREFASE PVP");
						return;
					}
					if (!obligado) {
						if ((System.currentTimeMillis() - _tiempoCombate) < 3000) {
							GestorSalida.ENVIAR_BN_NADA(luchRetirador.getPersonaje(), "ESPERAR MINIMO 3 SEG");
							return;
						}
					}
					boolean puedeExpulsar = false;
					if (idRetirador == _idLuchInit1 || idRetirador == _idLuchInit2) {
						puedeExpulsar = true;
					}
					Luchador luchASalir = luchRetirador;
					if (puedeExpulsar) {
						if (luchExpulsado != null && luchExpulsado.getID() != luchRetirador.getID()) {
							// si puede expulsar, y expulsa a otro jugador
							if (luchExpulsado.getEquipoBin() == luchRetirador.getEquipoBin()) {
								Personaje persoExpulsado = luchExpulsado.getPersonaje();
								if (persoExpulsado != null && persoExpulsado.getCompañero() != null && !persoExpulsado.esMultiman()) {
									int idMultiman = persoExpulsado.getCompañero().getID();
									retirarsePelea(idRetirador, idMultiman, true);
								}
								luchadorSalirPelea(luchExpulsado);
								luchASalir = luchExpulsado;
							} else {
								return;
							}
						} else {
							// si puede expulsar y se expulsa a si mismo
							switch (_tipo) {
								case Constantes.PELEA_TIPO_DESAFIO :
									for (final Luchador luch : luchadoresDeEquipo(3)) {
										luchadorSalirPelea(luch);
										if (luch.getPersonaje() != null && !luch.getPersonaje().esMultiman()) {
											GestorSalida.ENVIAR_GV_RESETEAR_PANTALLA_JUEGO(luch.getPersonaje());
										}
									}
									_fase = Constantes.PELEA_FASE_FINALIZADO;
									_mapaReal.borrarPelea(_id);
									GestorSalida.ENVIAR_Gc_BORRAR_ESPADA_EN_MAPA(_mapaReal, _id);
									GestorSalida.ENVIAR_fC_CANTIDAD_DE_PELEAS(_mapaReal);
									return;
								default :
									Personaje persoExpulsado = luchRetirador.getPersonaje();
									if (persoExpulsado != null && persoExpulsado.getCompañero() != null && !persoExpulsado.esMultiman()) {
										int idMultiman = persoExpulsado.getCompañero().getID();
										retirarsePelea(idRetirador, idMultiman, true);
									}
									consecuenciasPerder(luchRetirador);
									if (_tipo == Constantes.PELEA_TIPO_PVP || _tipo == Constantes.PELEA_TIPO_PRISMA) {
										luchRetirador.getPreLuchador().addHonor(-500);
									}
									break;
							}
						}
					} else {
						if (luchExpulsado != null) {
							GestorSalida.ENVIAR_BN_NADA(luchRetirador.getPersonaje(), "NO HAY LUCH A EXPULSAR");
							return;
						}
						Personaje persoExpulsado = luchRetirador.getPersonaje();
						switch (_tipo) {
							case Constantes.PELEA_TIPO_DESAFIO :
								if (persoExpulsado != null && persoExpulsado.getCompañero() != null && !persoExpulsado.esMultiman()) {
									int idMultiman = persoExpulsado.getCompañero().getID();
									retirarsePelea(idRetirador, idMultiman, true);
								}
								luchadorSalirPelea(luchRetirador);
								break;
							default :
								if (persoExpulsado != null && persoExpulsado.getCompañero() != null && !persoExpulsado.esMultiman()) {
									int idMultiman = persoExpulsado.getCompañero().getID();
									retirarsePelea(idRetirador, idMultiman, true);
								}
								consecuenciasPerder(luchRetirador);
								if (_tipo == Constantes.PELEA_TIPO_PVP || _tipo == Constantes.PELEA_TIPO_PRISMA) {
									luchRetirador.getPreLuchador().addHonor(-500);
								}
								break;
						}
					}
					GestorSalida.ENVIAR_GM_BORRAR_LUCHADOR(this, luchASalir.getID(), 3);
					GestorSalida.ENVIAR_Gt_BORRAR_NOMBRE_ESPADA(_mapaReal, _equipo1.containsKey(luchASalir.getID())
					? _idLuchInit1
					: _idLuchInit2, luchASalir);
					if (_equipo1.containsKey(luchASalir.getID())) {
						luchASalir.getCeldaPelea().removerLuchador(luchASalir);
						_equipo1.remove(luchASalir.getID());// en estado de posiciones
					} else if (_equipo2.containsKey(luchASalir.getID())) {
						luchASalir.getCeldaPelea().removerLuchador(luchASalir);
						_equipo2.remove(luchASalir.getID());
					}
					luchASalir.setEstaMuerto(true);
					if (luchASalir.getPersonaje() != null && !luchASalir.getPersonaje().esMultiman()) {
						GestorSalida.ENVIAR_GV_RESETEAR_PANTALLA_JUEGO(luchASalir.getPersonaje());
						if (_tipo == Constantes.PELEA_TIPO_KOLISEO) {
							luchASalir.getPersonaje().setPenalizarKoliseo();
						}
					}
					if (!acaboPelea((byte) 3) && _tipo == Constantes.PELEA_TIPO_DESAFIO) {
						verificaTodosListos();
					}
					break;
				default :
					System.out.println("ERROR RETIRARSE, estado de combate: " + _fase + " tipo de combate:" + _tipo
					+ " LuchadorExp:" + luchExpulsado + " LuchadorRet:" + luchRetirador + " mapaID: " + _mapaCopia.getID()
					+ " peleaID: " + _id);
					break;
			}
		} else {
			_espectadores.remove(luchRetirador.getID());
			luchadorSalirPelea(luchRetirador);
			GestorSalida.ENVIAR_GV_RESETEAR_PANTALLA_JUEGO(luchRetirador.getPersonaje());
		}
	}
	
	public String stringOrdenJugadores() {
		final StringBuilder packet = new StringBuilder("GTL");
		for (final Luchador luchador : _ordenLuchadores) {
			packet.append("|" + luchador.getID());
		}
		return packet.toString();
	}
	
	public int getSigIDLuchador() {
		return --_ultimaInvoID;
	}
	
	public void addLuchadorEnEquipo(final Luchador luchador, final int equipo) {
		if (equipo == 0) {
			_equipo1.put(luchador.getID(), luchador);
		} else if (equipo == 1) {
			_equipo2.put(luchador.getID(), luchador);
		}
	}
	
	public String strParaListaPelea() {
		if (_fase == Constantes.PELEA_FASE_FINALIZADO) {
			_mapaReal.borrarPelea(_id);
			return "";
		}
		final StringBuilder infos = new StringBuilder();
		infos.append(_id + ";");
		infos.append((_fase <= Constantes.PELEA_FASE_POSICION ? "-1" : _tiempoCombate) + ";");
		int jugEquipo1 = 0, jugEquipo2 = 0;
		for (final Luchador l : _equipo1.values()) {
			if (l == null || l.esInvocacion()) {
				continue;
			}
			jugEquipo1++;
		}
		for (final Luchador l : _equipo2.values()) {
			if (l == null || l.esInvocacion()) {
				continue;
			}
			jugEquipo2++;
		}
		if (jugEquipo1 == 0 || jugEquipo2 == 0) {
			acaboPelea((byte) 3);
			return "";
		}
		infos.append(_luchInit1.getFlag() + ",");
		infos.append(((_tipo == Constantes.PELEA_TIPO_PVP || _tipo == Constantes.PELEA_TIPO_PRISMA)
		? _luchInit1.getAlineacion()
		: 0) + ",");
		infos.append(jugEquipo1 + ";");
		infos.append(_luchInit2.getFlag() + ",");
		infos.append(((_tipo == Constantes.PELEA_TIPO_PVP || _tipo == Constantes.PELEA_TIPO_PRISMA)
		? _luchInit2.getAlineacion()
		: 0) + ",");
		infos.append(jugEquipo2 + ";");
		return infos.toString();
	}
	
	public boolean continuaPelea() {
		boolean equipo1Vivo = false, equipo2Vivo = false;
		for (final Luchador luchador : _equipo1.values()) {
			if (luchador.esInvocacion()) {
				continue;
			}
			if (!luchador.estaMuerto()) {
				equipo1Vivo = true;
				break;
			}
		}
		for (final Luchador luchador : _equipo2.values()) {
			if (luchador.esInvocacion()) {
				continue;
			}
			if (!luchador.estaMuerto()) {
				equipo2Vivo = true;
				break;
			}
		}
		return equipo1Vivo && equipo2Vivo;
	}
	
	public int cuantosQuedanDelEquipo(final int id) {
		int num = 0;
		if (_equipo1.containsKey(id)) {
			for (final Luchador luchador : _equipo1.values()) {
				if (luchador.estaMuerto() || luchador.esInvocacion()) {
					continue;
				}
				num++;
			}
		} else if (_equipo2.containsKey(id)) {
			for (final Luchador luchador : _equipo2.values()) {
				if (luchador.estaMuerto() || luchador.esInvocacion()) {
					continue;
				}
				num++;
			}
		}
		return num;
	}
	
	private String mostrarEspada() {
		// final String packet = "Gc+" + idPelea + ";" + tipoPelea + "|" + id1 + ";" + celda1 + ";" +
		// flag1 + ";" + alin1
		// + "|" + id2 + ";" + celda2 + ";" + flag2 + ";" + alin2;
		StringBuilder p = new StringBuilder("Gc+");
		p.append(_id + ";");
		if (_tipo == Constantes.PELEA_TIPO_CACERIA) {
			p.append(0);
		} else {
			p.append(_tipo);
		}
		p.append("|" + _idLuchInit1 + ";" + _celdaID1 + ";" + _luchInit1.getFlag() + ";");
		if (_tipo == Constantes.PELEA_TIPO_PVP || _tipo == Constantes.PELEA_TIPO_PRISMA) {
			p.append(_luchInit1.getAlineacion());
		} else {
			p.append(Constantes.ALINEACION_NULL);
		}
		p.append("|" + _idLuchInit2 + ";" + _celdaID2 + ";" + _luchInit2.getFlag() + ";");
		if (_tipo == Constantes.PELEA_TIPO_PVP || _tipo == Constantes.PELEA_TIPO_PRISMA) {
			p.append(_luchInit2.getAlineacion());
		} else {
			p.append(Constantes.ALINEACION_NULL);
		}
		return p.toString();
	}
	
	public void infoEspadaPelea(Personaje perso) {
		try {
			if (_fase != Constantes.PELEA_FASE_POSICION || _tipo == Constantes.PELEA_TIPO_PVM_NO_ESPADA)
				return;
			GestorSalida.ENVIAR_Gc_MOSTRAR_ESPADA_A_JUGADOR(perso, mostrarEspada());
			StringBuilder enviar = new StringBuilder();
			for (final Entry<Integer, Luchador> entry : _equipo1.entrySet()) {
				final Luchador luchador = entry.getValue();
				if (!enviar.toString().isEmpty()) {
					enviar.append("|+");
				}
				enviar.append(luchador.getID() + ";" + luchador.getNombre() + ";" + luchador.getNivel());
			}
			GestorSalida.ENVIAR_Gt_AGREGAR_NOMBRE_ESPADA(perso, _idLuchInit1, enviar.toString());
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(perso, _cerrado1 ? '+' : '-', 'A', _idLuchInit1);
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(perso, _ayuda1 ? '+' : '-', 'H', _idLuchInit1);
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(perso, _soloGrupo1 ? '+' : '-', 'P', _idLuchInit1);
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(perso, _sinEspectador ? '+' : '-', 'S', _idLuchInit1);
			enviar = new StringBuilder();
			for (final Entry<Integer, Luchador> entry : _equipo2.entrySet()) {
				final Luchador luchador = entry.getValue();
				if (!enviar.toString().isEmpty()) {
					enviar.append("|+");
				}
				enviar.append(luchador.getID() + ";" + luchador.getNombre() + ";" + luchador.getNivel());
			}
			GestorSalida.ENVIAR_Gt_AGREGAR_NOMBRE_ESPADA(perso, _idLuchInit2, enviar.toString());
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(perso, _cerrado2 ? '+' : '-', 'A', _idLuchInit2);
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(perso, _ayuda2 ? '+' : '-', 'H', _idLuchInit2);
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(perso, _soloGrupo2 ? '+' : '-', 'P', _idLuchInit2);
			GestorSalida.ENVIAR_Go_BOTON_ESPEC_AYUDA(perso, _sinEspectador ? '+' : '-', 'S', _idLuchInit2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<Luchador> getListaMuertos() {
		return _listaMuertos;
	}
	
	public void addBuffLuchadores(final ArrayList<Luchador> objetivos, int efectoID, final int valor,
	final int turnosRestantes, final int hechizoID, String args, final Luchador lanzador, TipoDaño tipo,
	String condicionHechizo) {
		if (objetivos.isEmpty()) {
			return;
		}
		final StringBuilder str = new StringBuilder();
		for (final Luchador luch : objetivos) {
			// aqui no envia el GIE, lo envia abajo
			if (!luch.addBuffConGIE(efectoID, valor, turnosRestantes, hechizoID, args, lanzador, false, tipo,
			condicionHechizo)._primero) {
				continue;
			}
			if (!condicionHechizo.isEmpty()) {
				continue;
			}
			if (str.length() > 0) {
				str.append(",");
			}
			str.append(luch.getID());
		}
		if (str.length() > 0) {
			String gie = getStrParaGIE(efectoID, str.toString(), turnosRestantes, hechizoID, args);
			GestorSalida.ENVIAR_GIE_AGREGAR_BUFF_PELEA(this, 7, gie);
		}
	}
	
	private static String getStrParaGIE(final int efectoID, final String objetivos, final int turnos, final int hechizoID,
	final String args) {
		// para varios
		String[] s = args.replaceAll("-1", "null").split(",");
		final String mParam1 = s[0];
		final String valMax = s[1];
		final String mParam3 = s[2];
		final String suerte = s[4];
		final String packet = efectoID + ";" + objetivos + ";" + mParam1 + ";" + valMax + ";" + mParam3 + ";" + suerte + ";"
		+ turnos + ";" + hechizoID;
		return packet;
	}
	
	public static String getStrParaGA998(final int efectoID, final int objetivo, final int turnos, final int hechizoID,
	final String args) {
		// para uno solo
		String[] s = args.replaceAll("-1", "null").split(",");
		final String mParam1 = s[0];
		final String valMax = s[1];
		final String mParam3 = s[2];
		final String suerte = s[4];
		final String packet = objetivo + "," + efectoID + "," + mParam1 + "," + valMax + "," + mParam3 + "," + suerte + ","
		+ turnos + "," + hechizoID;
		return packet;
	}
	
	public boolean getSalvarMobHeroico() {
		return _salvarMobHeroico;
	}
	
	public void setSalvarMobHeroico(boolean _salvarMobHeroico) {
		this._salvarMobHeroico = _salvarMobHeroico;
	}
	
	public GrupoMob getMobGrupo() {
		return _mobGrupo;
	}
	
	public void setMobGrupo(GrupoMob _mobGrupo) {
		this._mobGrupo = _mobGrupo;
	}
}
