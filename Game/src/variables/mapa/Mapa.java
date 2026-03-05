package variables.mapa;

import estaticos.MainServidor;
import estaticos.Camino;
import estaticos.Condiciones;
import estaticos.Constantes;
import estaticos.Encriptador;
import estaticos.Formulas;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.Mundo;
import estaticos.Mundo.Duo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import sprites.PreLuchador;
import variables.gremio.Recaudador;
import variables.mapa.interactivo.ObjetoInteractivo;
import variables.mob.AparecerMobs;
import variables.mob.GrupoMob;
import variables.mob.MobGrado;
import variables.mob.MobGradoModelo;
import variables.mob.MobModelo;
import variables.mob.MobPosible;
import variables.mob.AparecerMobs.Aparecer;
import variables.mob.MobModelo.TipoGrupo;
import variables.montura.Montura;
import variables.npc.NPC;
import variables.npc.NPCModelo;
import variables.objeto.Objeto;
import variables.pelea.Luchador;
import variables.pelea.Pelea;
import variables.personaje.GrupoKoliseo;
import variables.personaje.Personaje;
import variables.zotros.Accion;
import variables.zotros.Prisma;

public class Mapa {
	private byte _sigIDNpc = -51;
	private boolean _muteado;
	private short _id, _X, _Y, _capabilities, _bgID, _musicID, _ambienteID;
	private byte _ancho, _alto, _maxGrupoMobs = 5, _maxMobsPorGrupo = 8, _maxMercantes = 5, _maxPeleas = 99, _outDoor;
	private String _fecha, _mapData = "", _celdasPelea = "";
	private Map<Short, Pelea> _peleas;
	private Map<Integer, NPC> _npcs;
	private Map<Integer, GrupoMob> _grupoMobsTotales;
	private CopyOnWriteArrayList<GrupoMob> _grupoMobsEnCola;
	private CopyOnWriteArrayList<Personaje> _personajes;
	private Map<Integer, ArrayList<Accion>> _accionFinPelea;
	private Map<Integer, Personaje> _mercantes;
	private ArrayList<MobPosible> _mobPosibles;
	private ArrayList<Integer> _trabajosRealizar;
	private ArrayList<ObjetoInteractivo> _objInteractivos;
	private final Map<Short, Celda> _celdas = new TreeMap<Short, Celda>();
	private final ArrayList<Short> _posPeleaRojo1 = new ArrayList<Short>(), _posPeleaAzul2 = new ArrayList<Short>();
	private String _colorCeldaAtacante = "";
	private SubArea _subArea;
	private Cercado _cercado;
	private Prisma _prisma;
	private Recaudador _recaudador;
	private int _minNivelGrupoMob = 0;
	private int _maxNivelGrupoMob = 0;
	private Boolean _prePelea = new Boolean(true);
	
	public Boolean getPrePelea() {
		return _prePelea;
	}
	
	public Mapa(final short id, final String fecha, final byte ancho, final byte alto, final String posDePelea,
	String mapData, final String key, final String mobs, final short X, final short Y, final short subArea,
	final byte maxGrupoDeMobs, final byte maxMobsPorGrupo, final byte maxMercantes, final short parametros,
	final byte maxPeleas, short bgID, short musicID, short ambienteID, byte outDoor, int minNivelGrupoMob,
	int maxNivelGrupoMob) {
		mapaNormal();
		_id = id;
		_fecha = fecha;
		_ancho = ancho;
		_alto = alto;
		_X = X;
		_Y = Y;
		_key = key;  // AGREGAR ESTA LÍNEA  CLIENTE 1.43.7
		if (MainServidor.MODO_DEBUG) {
			System.out.println("  --> Descifrando MapData ID " + _id + " con key " + key);
		}		
		if (!key.trim().isEmpty()) {
			mapData = Encriptador.decifrarMapData(key, mapData);
		}
		_mapData = mapData;
		_bgID = bgID;
		_musicID = musicID;
		_ambienteID = ambienteID;
		_outDoor = outDoor;
		_capabilities = parametros;
		_maxGrupoMobs = maxGrupoDeMobs;
		_maxMobsPorGrupo = maxMobsPorGrupo;
		_maxMercantes = maxMercantes;
		_maxPeleas = maxPeleas;
		_minNivelGrupoMob = minNivelGrupoMob;
		_maxNivelGrupoMob = maxNivelGrupoMob;
		try {
			_subArea = Mundo.getSubArea(subArea);
			_subArea.addMapa(this);
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("Error al cargar el mapa ID " + id + " subAreaID " + subArea + " no existe");
			System.exit(1);
			return;
		}
		if (MainServidor.MODO_DEBUG) {
			System.out.println("  --> Decompilando MapData ID " + _id);
		}
		Encriptador.decompilarMapaData(this);
		_trabajosRealizar.trimToSize();
		if (MainServidor.MODO_DEBUG) {
			System.out.println("  --> MobPosibles en mapaID " + _id + " mobs " + mobs);
		}
		mobPosibles(mobs);
		if (MainServidor.MODO_DEBUG) {
			System.out.println("  --> Agregando Mobs mapaID " + _id);
		}
		if (MainServidor.PARAM_PERMITIR_MOBS) {
			agregarMobsInicioServer();
		}
		decodificarPosPelea(posDePelea);
		// corregirPosPelea();
		if (_colorCeldaAtacante.isEmpty() && (esMazmorra() || esArena())) {
			_colorCeldaAtacante = "red";
		}
		if (_colorCeldaAtacante.isEmpty()) {
			_colorCeldaAtacante = MainServidor.COLOR_CELDAS_PELEA_AGRESOR;
		}
		getConvertCeldasPelea();
	}
	
	private void agregarMobsInicioServer() {
		ArrayList<Short> s1 = Mundo.getMapaEstrellas(_id);
		ArrayList<String> s2 = Mundo.getMapaHeroico(_id);
		if (!_mobPosibles.isEmpty()) {
			for (int i = 0; i < _maxGrupoMobs; i++) {
				try {
					short estrellas = -1;
					String heroico = "";
					if (s1 != null && !s1.isEmpty()) {
						estrellas = s1.get(0);
					}
					if (s2 != null && !s2.isEmpty()) {
						heroico = s2.get(0);
					}
					if (MainServidor.MODO_DEBUG) {
						System.out.println("  --> Agregando grupoMob mapaID: " + _id + ", estrellas: " + estrellas + ", heroico: "
						+ heroico);
					}
					GrupoMob grupoMob = getGrupoMobInicioServer((short) -1, heroico, estrellas);// neutral
					if (grupoMob == null) {
						break;
					}
					if (s1 != null && !s1.isEmpty()) {
						s1.remove(0);
					}
					if (s2 != null && !s2.isEmpty()) {
						s2.remove(0);
					}
				} catch (Exception e) {}
			}
		}
		if (s2 != null) {
			for (String s : s2) {
				try {
					if (s.isEmpty()) {
						continue;
					}
					String strMob = s.split(Pattern.quote("|"))[0];
					GrupoMob grupoMob = new GrupoMob(this, (short) -1, strMob, TipoGrupo.HASTA_QUE_MUERA, "");
					if (grupoMob.getMobs().isEmpty()) {
						continue;
					}
					grupoMob.addObjetosKamasInicioServer(s);
					addUltimoGrupoMob(grupoMob);
				} catch (Exception e) {}
			}
		}
	}
	
	public boolean esNivelGrupoMobPermitido(int nivel) {
		int min = 0, max = 0;
		if (_subArea.getMinNivelGrupoMob() > 0) {
			min = _subArea.getMinNivelGrupoMob();
		}
		if (_subArea.getMaxNivelGrupoMob() > 0) {
			max = _subArea.getMaxNivelGrupoMob();
		}
		if (_minNivelGrupoMob > 0) {
			min = _minNivelGrupoMob;
		}
		if (_maxNivelGrupoMob > 0) {
			max = _maxNivelGrupoMob;
		}
		if (min == 0 && max == 0) {
			return true;
		}
		return nivel >= min && nivel <= max;
	}
	
	public int getMinNivelGrupoMob() {
		return _minNivelGrupoMob;
	}
	
	public int getMaxNivelGrupoMob() {
		return _maxNivelGrupoMob;
	}
	
	public Mapa(final Mapa mapa, final String posDePelea) {
		_id = mapa._id;
		_fecha = mapa._fecha;
		_ancho = mapa._ancho;
		_alto = mapa._alto;
		_X = mapa._X;
		_Y = mapa._Y;
		_celdas.clear();
		for (Celda newCelda : mapa._celdas.values()) {
			Celda celda = new Celda(this, newCelda.getID(), newCelda.getActivo(), newCelda.getMovimiento(), newCelda
			.getLevel(), newCelda.getSlope(), newCelda.lineaDeVista(), -1);
			_celdas.put(newCelda.getID(), celda);
			celda.celdaPelea();
		}
		decodificarPosPelea(posDePelea);
		getConvertCeldasPelea();
	}
	
	public void mapaNormal() {
		_peleas = new HashMap<Short, Pelea>();
		_npcs = new HashMap<Integer, NPC>();
		_grupoMobsTotales = new HashMap<Integer, GrupoMob>();
		// _grupoMobsFix = new HashMap<Integer, GrupoMob>();
		_grupoMobsEnCola = new CopyOnWriteArrayList<GrupoMob>();
		_personajes = new CopyOnWriteArrayList<Personaje>();
		_accionFinPelea = new HashMap<Integer, ArrayList<Accion>>();
		_mercantes = new TreeMap<Integer, Personaje>();
		_mobPosibles = new ArrayList<MobPosible>();
		_trabajosRealizar = new ArrayList<Integer>();
		_objInteractivos = new ArrayList<ObjetoInteractivo>();
	}
	
	public Mapa copiarMapa(String posPelea) {
		return new Mapa(this, posPelea);
	}
	
	public ArrayList<ObjetoInteractivo> getObjetosInteractivos() {
		return _objInteractivos;
	}
	
	public String getColorCeldasAtacante() {
		return _colorCeldaAtacante;
	}
	
	public void setStrCeldasPelea(String posDePelea) {
		_celdasPelea = posDePelea;
		_colorCeldaAtacante = "";
		decodificarPosPelea(_celdasPelea);
	}
	
	public void setColorCeldasAtacante(String pos) {
		_colorCeldaAtacante = pos;
	}
	
	public void decodificarPosPelea(String posDePelea) {
		try {
			_posPeleaRojo1.clear();
			_posPeleaAzul2.clear();
			// _colorCeldaAtacante = "";
			final String[] str = posDePelea.split(Pattern.quote("|"));
			if (str.length > 0 && !str[0].isEmpty()) {
				Encriptador.analizarCeldasDeInicio(str[0], _posPeleaRojo1);
			}
			if (str.length > 1 && !str[1].isEmpty()) {
				Encriptador.analizarCeldasDeInicio(str[1], _posPeleaAzul2);
			}
			if (str.length > 2 && !str[2].isEmpty()) {
				_colorCeldaAtacante = str[2];
			}
		} catch (final Exception e) {}
	}
	
	// private void corregirPosPelea() {
	// short temp = -1;
	// for (int i = 0; i < _posPeleaRojo1.size(); i++) {
	// short celdaRoja = _posPeleaRojo1.get(i);
	// if (temp == -1) {
	// temp = celdaRoja;
	// continue;
	// }
	// Duo<Integer, ArrayList<Celda>> path = Camino.getPathPelea(this, temp, celdaRoja, -1, null,
	// true);
	// if (path == null || path._segundo.isEmpty()) {
	// _posPeleaRojo1.remove(i);
	// i--;
	// continue;
	// }
	// if (path._segundo.get(path._segundo.size() - 1).getID() != celdaRoja) {
	// _posPeleaRojo1.remove(i);
	// i--;
	// continue;
	// }
	// }
	// for (int i = 0; i < _posPeleaAzul2.size(); i++) {
	// short celdaAzul = _posPeleaAzul2.get(i);
	// if (temp == -1) {
	// temp = celdaAzul;
	// continue;
	// }
	// Duo<Integer, ArrayList<Celda>> path = Camino.getPathPelea(this, temp, celdaAzul, -1, null,
	// true);
	// if (path == null || path._segundo.isEmpty()) {
	// _posPeleaAzul2.remove(i);
	// i--;
	// continue;
	// }
	// if (path._segundo.get(path._segundo.size() - 1).getID() != celdaAzul) {
	// _posPeleaAzul2.remove(i);
	// i--;
	// continue;
	// }
	// }
	// }
	public void addCeldaPelea(int equipo, short celdaID) {
		_posPeleaRojo1.remove((Object) celdaID);
		_posPeleaAzul2.remove((Object) celdaID);
		if (equipo == 1) {
			_posPeleaRojo1.add(celdaID);
		} else if (equipo == 2) {
			_posPeleaAzul2.add(celdaID);
		}
	}
	
	public void borrarCeldasPelea(short celdaID) {
		_posPeleaRojo1.remove((Object) celdaID);
		_posPeleaAzul2.remove((Object) celdaID);
	}
	
	public String getConvertCeldasPelea() {
		boolean vacio = true;
		StringBuilder str = new StringBuilder();
		for (short s : _posPeleaRojo1) {
			vacio = false;
			str.append(Encriptador.celdaIDAHash(s));
		}
		str.append("|");
		for (short s : _posPeleaAzul2) {
			vacio = false;
			str.append(Encriptador.celdaIDAHash(s));
		}
		_celdasPelea = str.toString();
		if (!vacio && !_colorCeldaAtacante.isEmpty()) {
			vacio = false;
			str.append("|");
			str.append(_colorCeldaAtacante);
		}
		return str.toString();
	}
	
	private boolean prepararCeldasPelea(int cant1, int cant2) {
		cant1 = _posPeleaAzul2.isEmpty() && _posPeleaRojo1.isEmpty() ? 8 : cant1;
		int pos = aptoParaPelea(cant1, cant2);
		switch (pos) {
			case 0 :
				return true;
			case 1 :
				getPosicionesAleatorias(cant1, 0);
				break;
			case 2 :
				getPosicionesAleatorias(0, cant2);
				break;
			case -1 :
				getPosicionesAleatorias(8, 8);
				break;
		}
		return aptoParaPelea(cant1, cant2) == 0;
	}
	
	private int aptoParaPelea(int cant1, int cant2) {
		switch (_colorCeldaAtacante) {
			case "red" :
				if (_posPeleaRojo1.size() < cant1) {
					if (_posPeleaAzul2.size() < cant2) {
						return -1;
					} else {
						return 1;
					}
				}
				if (_posPeleaAzul2.size() < cant2) {
					return 2;
				}
				break;
			case "blue" :
				if (_posPeleaAzul2.size() < cant1) {
					if (_posPeleaRojo1.size() < cant2) {
						return -1;
					} else {
						return 2;
					}
				}
				if (_posPeleaRojo1.size() < cant2) {
					return 1;
				}
				break;
			default :
				if (_posPeleaAzul2.size() < cant1 || _posPeleaRojo1.size() < cant1) {
					return -1;
				}
				if (_posPeleaRojo1.size() < cant2 || _posPeleaAzul2.size() < cant2) {
					return -1;
				}
				break;
		}
		return 0;
	}
	
	private void getPosicionesAleatorias(int cant1, int cant2) {
		final ArrayList<Short> celdaLibres = new ArrayList<Short>();
		for (final Entry<Short, Celda> entry : _celdas.entrySet()) {
			if (!entry.getValue().esCaminable(true)) {
				continue;
			}
			celdaLibres.add(entry.getKey());
		}
		if (celdaLibres.isEmpty() || celdaLibres.size() < (cant1 + cant2)) {
			return;
		}
		short temp = -1;
		if (cant1 >= 1) {
			_posPeleaRojo1.clear();
			while (_posPeleaRojo1.size() < cant1 && !celdaLibres.isEmpty()) {
				final int rand = Formulas.getRandomInt(0, celdaLibres.size() - 1);
				short t = celdaLibres.get(rand);
				if (temp == -1) {
					_posPeleaRojo1.add(t);
					temp = t;
				} else {
					Duo<Integer, ArrayList<Celda>> path = Camino.getPathPelea(this, temp, t, -1, null, true);
					if (path != null && !path._segundo.isEmpty()) {
						if (path._segundo.get(path._segundo.size() - 1).getID() == t) {
							_posPeleaRojo1.add(t);
						}
					}
				}
				celdaLibres.remove(rand);
			}
		}
		if (cant2 >= 1) {
			_posPeleaAzul2.clear();
			while (_posPeleaAzul2.size() < cant2 && !celdaLibres.isEmpty()) {
				final int rand = Formulas.getRandomInt(0, celdaLibres.size() - 1);
				short t = celdaLibres.get(rand);
				if (temp == -1) {
					_posPeleaAzul2.add(t);
					temp = t;
				} else {
					Duo<Integer, ArrayList<Celda>> path = Camino.getPathPelea(this, temp, t, -1, null, true);
					if (path != null && !path._segundo.isEmpty()) {
						if (path._segundo.get(path._segundo.size() - 1).getID() == t) {
							_posPeleaAzul2.add(t);
						}
					}
				}
				celdaLibres.remove(rand);
			}
		}
		getConvertCeldasPelea();
	}
	
	public void panelPosiciones(Personaje perso, boolean mostrar) {
		StringBuilder str = new StringBuilder();
		String signo = mostrar ? "+" : "-";
		for (short s : _posPeleaRojo1) {
			str.append("|" + signo + s + ";0;4");
		}
		for (short s : _posPeleaAzul2) {
			str.append("|" + signo + s + ";0;11");
		}
		if (_cercado != null) {
			for (short s : _cercado.getCeldasObj()) {
				str.append("|" + signo + s + ";0;5");
			}
		}
		if (str.length() == 0) {
			return;
		}
		GestorSalida.enviarEnCola(perso, "GDZ" + str.toString(), false);
	}
	
	public int getBgID() {
		return _bgID;
	}
	
	public int getMusicID() {
		return _musicID;
	}
	
	public int getAmbienteID() {
		return _ambienteID;
	}
	
	public int getOutDoor() {
		return _outDoor;
	}
	
	public int getCapabilities() {
		return _capabilities;
	}
	
	public String strCeldasPelea() {
		return _celdasPelea;
	}
	
	public ArrayList<Short> getPosTeamRojo1() {
		return _posPeleaRojo1;
	}
	
	public ArrayList<Short> getPosTeamAzul2() {
		return _posPeleaAzul2;
	}
	
	public void setMaxPeleas(byte max) {
		_maxPeleas = max;
	}
	
	public int getMaxNumeroPeleas() {
		return _maxPeleas;
	}
	
	private void mobPosibles(String mobs) {
		if (_mobPosibles == null) {
			return;
		}
		_mobPosibles.clear();
		final ArrayList<Integer> ids = new ArrayList<Integer>();
		for (final String str : mobs.split(Pattern.quote("|"))) {
			try {
				int mobID = 0;
				String[] split = str.split(",");
				try {
					mobID = Integer.parseInt(split[0]);
				} catch (final Exception e) {}
				mobID = Constantes.getMobSinHalloween(mobID);
				if (ids.contains(mobID)) {
					continue;
				}
				MobModelo mobModelo = Mundo.getMobModelo(mobID);
				if (mobModelo == null || mobModelo.getTipoMob() == Constantes.MOB_TIPO_LOS_ARCHIMONSTRUOS) {
					continue;
				}
				ids.add(mobID);
				if (!mobModelo.puedeSubArea(getSubArea().getID())) {
					continue;
				}
				int minLvl = 0, maxLvl = 0, cantidad = 0, probabilidad = mobModelo.getProbabilidadAparecer();
				try {
					minLvl = Integer.parseInt(split[1]);
					maxLvl = Integer.parseInt(split[2]);
					cantidad = Integer.parseInt(split[3]);
					probabilidad = Integer.parseInt(split[4]);
				} catch (final Exception e) {}
				MobPosible mobP = new MobPosible(cantidad, probabilidad);
				for (final MobGradoModelo mob : Mundo.getMobModelo(mobID).getGrados().values()) {
					if (minLvl > 0 && mob.getNivel() < minLvl) {
						continue;
					}
					if (maxLvl > 0 && mob.getNivel() > maxLvl) {
						continue;
					}
					mobP.addMobPosible(mob);
					addMobPosibles(mobP);
				}
			} catch (final Exception e) {}
		}
		_mobPosibles.trimToSize();
	}
	
	public void setKeyMapData(final String fecha, final String key, String mapData) {
		_fecha = fecha;
		if (!key.trim().isEmpty()) {
			mapData = Encriptador.decifrarMapData(key, mapData);
		}
		_mapData = mapData;
		actualizarCasillas();
		GestorSQL.CARGAR_TRIGGERS_POR_MAPA(_id);
	}
	
	// public void setKeyMap(final String key) {
	// _key = key;
	// }
	
	private String _key = "";
	
	public String getKey() {  
	    return _key;  
	}
	

	
	public void setFecha(String fecha) {
		_fecha = fecha;
	}
	
	public ArrayList<Integer> getTrabajos() {
		return _trabajosRealizar;
	}
	
	public void setMuteado(final boolean mute) {
		_muteado = mute;
	}
	
	public boolean getMuteado() {
		return _muteado;
	}
	
	public void setMaxMobsPorGrupo(final byte id) {
		_maxMobsPorGrupo = id;
	}
	
	private void addMobPosibles(final MobPosible mob) {
		if (_mobPosibles == null) {
			return;
		}
		if (_mobPosibles.contains(mob)) {
			return;
		}
		_mobPosibles.add(mob);
	}
	
	public void agregarEspadaPelea(Personaje perso) {
		for (Pelea pelea : _peleas.values()) {
			pelea.infoEspadaPelea(perso);
		}
	}
	
	public void insertarMobs(final String mobs) {
		if (_mobPosibles == null) {
			return;
		}
		mobPosibles(mobs);
		if (_mobPosibles.isEmpty()) {
			return;
		}
		for (int i = 0; i < _maxGrupoMobs; i++) {
			getGrupoMobInicioServer((short) -1, "", MainServidor.INICIO_BONUS_ESTRELLAS_MOBS);
		}
	}
	
	// public void addCeldaObjInteractivo(final Celda celda) {
	// _celdasObjInterac.add(celda);
	// _celdasObjInterac.trimToSize();
	// }
	// public ArrayList<Celda> getCeldasObjInter() {
	// return _celdasObjInterac;
	// }
	public void setParametros(final short d) {
		_capabilities = d;
		_capabilities = getCapabilitiesCompilado();
		if (_colorCeldaAtacante.isEmpty() && (esMazmorra() || esArena())) {
			_colorCeldaAtacante = "red";
		}
	}
	
	public boolean mapaNoAgresion() {
		return (_capabilities & 1) == 1;
	}
	
	public boolean esArena() {
		return (_capabilities & 2) == 2;
	}
	
	public boolean esMazmorra() {
		return (_capabilities & 4) == 4;
	}
	
	public boolean mapaNoDesafio() {
		return (_capabilities & 8) == 8;
	}
	
	public boolean mapaNoRecaudador() {
		return (_capabilities & 16) == 16;
	}
	
	public boolean mapaNoMercante() {
		return (_capabilities & 32) == 32;
	}
	
	public boolean mapaAbonado() {
		return (_capabilities & 64) == 64;
	}
	
	public boolean mapaNoPrisma() {
		if (esMazmorra() || esArena() || Mundo.getCasaDentroPorMapa(_id) != null || !getTrabajos().isEmpty()) {
			return true;
		}
		return (_capabilities & 128) == 128;
	}
	
	public boolean mapaNoPuedeSalvarTeleport() {
		return (_capabilities & 256) == 256;
	}
	
	public boolean mapaNoPuedeTeleportarse() {
		return (_capabilities & 512) == 512;
	}
	
	public short getCapabilitiesCompilado() {
		short parametros = 0;
		if (mapaNoAgresion()) {
			parametros += 1;
		}
		if (esArena()) {
			parametros += 2;
		}
		if (esMazmorra()) {
			parametros += 4;
		}
		if (mapaNoDesafio()) {
			parametros += 8;
		}
		if (mapaNoRecaudador()) {
			parametros += 16;
		}
		if (mapaNoMercante()) {
			parametros += 32;
		}
		if (mapaAbonado()) {
			parametros += 64;
		}
		if (mapaNoPrisma()) {
			parametros += 128;
		}
		return parametros;
	}
	
	public byte getMaxMobsPorGrupo() {
		return _maxMobsPorGrupo;
	}
	
	public byte getMaxGrupoDeMobs() {
		return _maxGrupoMobs;
	}
	
	public void setMaxGrupoDeMobs(final byte id) {
		_maxGrupoMobs = id;
	}
	
	public void addAccionFinPelea(final int tipoPelea, final Accion accion) {
		delAccionFinPelea(tipoPelea, accion.getID(), accion.getCondicion());
		if (_accionFinPelea.get(tipoPelea) == null) {
			_accionFinPelea.put(tipoPelea, new ArrayList<Accion>());
		}
		_accionFinPelea.get(tipoPelea).add(accion);
	}
	
	private void delAccionFinPelea(final int tipoPelea, final int tipoAccion, String condicion) {
		if (_accionFinPelea.get(tipoPelea) == null) {
			return;
		}
		final ArrayList<Accion> copy = new ArrayList<Accion>();
		copy.addAll(_accionFinPelea.get(tipoPelea));
		for (final Accion acc : copy) {
			if (acc.getID() == tipoAccion && acc.getCondicion().equals(condicion)) {
				_accionFinPelea.get(tipoPelea).remove(acc);
			}
		}
	}
	
	public void borrarAccionesPelea() {
		_accionFinPelea.clear();
	}
	
	public void aplicarAccionFinPelea(final int tipo, final Collection<Luchador> ganadores,
	final ArrayList<Accion> acciones) {
		ArrayList<Accion> acc = new ArrayList<>();
		if (acciones != null) {
			acc.addAll(acciones);
		}
		if (_accionFinPelea.get(tipo) != null) {
			acc.addAll(_accionFinPelea.get(tipo));
		}
		for (final Accion accion : acc) {
			for (final Luchador ganador : ganadores) {
				if (ganador.estaRetirado()) {
					continue;
				}
				final Personaje perso = ganador.getPersonaje();
				if (perso != null) {
					if (!Condiciones.validaCondiciones(perso, accion.getCondicion())) {
						GestorSalida.ENVIAR_Im_INFORMACION(perso, "119|45");
						continue;
					}
					accion.realizarAccion(perso, null, -1, (short) -1);
				}
			}
		}
	}
	
	public void setCercado(final Cercado cercado) {
		_cercado = cercado;
	}
	
	public Cercado getCercado() {
		return _cercado;
	}
	
	public SubArea getSubArea() {
		return _subArea;
	}
	
	public void setMaxMercantes(byte mercantes) {
		_maxMercantes = mercantes;
	}
	
	public byte getMaxMercantes() {
		return _maxMercantes;
	}
	
	public Recaudador getRecaudador() {
		return _recaudador;
	}
	
	public Prisma getPrisma() {
		return _prisma;
	}
	
	public void setRecaudador(Recaudador recaudador) {
		_recaudador = recaudador;
	}
	
	public void setPrisma(Prisma prisma) {
		_prisma = prisma;
	}
	
	public short getX() {
		return _X;
	}
	
	public short getY() {
		return _Y;
	}
	
	public void actualizarCasillas() {
		if (!_mapData.isEmpty()) {
			Encriptador.decompilarMapaData(this);
		}
	}
	
	public String getMapData() {
		return _mapData;
	}
	
	// public void setMapData(final String mapdata) {
	// _mapData = mapdata;
	// }
	public void panelTriggers(Personaje perso, boolean mostrar) {
		StringBuilder str = new StringBuilder();
		for (Celda c : _celdas.values()) {
			if (c.getAcciones() == null) {
				return;
			}
			if (c.getAcciones().containsKey(0)) {
				str.append("|" + (mostrar ? "+" : "-") + c.getID() + ";0;11");
			}
		}
		if (str.length() == 0) {
			return;
		}
		GestorSalida.enviarEnCola(perso, "GDZ" + str.toString(), false);
	}
	
	public short getID() {
		return _id;
	}
	
	public String getFecha() {
		return _fecha;
	}
	
	public byte getAncho() {
		return _ancho;
	}
	
	public byte getAlto() {
		return _alto;
	}
	
	public Map<Short, Celda> getCeldas() {
		return _celdas;
	}
	
	public Celda getCelda(final short id) {
		return _celdas.get(id);
	}
	
	public Map<Integer, NPC> getNPCs() {
		return _npcs;
	}
	
	private int sigIDNPC() {
		if (_sigIDNpc <= -100) {
			return -51;
		}
		return _sigIDNpc--;
	}
	
	public NPC addNPC(final NPCModelo npcModelo, final short celdaID, final byte dir) {
		final NPC npc = new NPC(npcModelo, sigIDNPC(), celdaID, dir);
		_npcs.put(npc.getID(), npc);
		return npc;
	}
	
	public void borrarNPC(final int id) {
		_npcs.remove(id);
	}
	
	public NPC getNPC(final int id) {
		return _npcs.get(id);
	}
	
	public CopyOnWriteArrayList<Personaje> getArrayPersonajes() {
		return _personajes;
	}
	
	void addPersonaje(Personaje perso) {
		if (_personajes == null) {
			return;
		}
		if (!_personajes.contains(perso)) {
			_personajes.add(perso);
		}
	}
	
	void removerPersonaje(Personaje perso) {
		if (_personajes == null) {
			return;
		}
		_personajes.remove(perso);
	}
	
	public void expulsarMercanterPorCelda(final short celda) {
		for (final Personaje perso : _mercantes.values()) {
			if (perso.getCelda().getID() == celda) {
				removerMercante(perso.getID());
				perso.setMercante(false);
				GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(perso.getMapa(), perso.getID());
				return;
			}
		}
	}
	
	public boolean removerMercante(final int id) {
		return _mercantes.remove(id) != null;
	}
	
	public int mercantesEnCelda(short celda) {
		int i = 0;
		for (Personaje perso : _mercantes.values()) {
			if (perso.getCelda().getID() == celda) {
				i++;
			}
		}
		return i;
	}
	
	public boolean addMercante(final Personaje perso) {
		if (_mercantes.size() >= _maxMercantes) {
			return false;
		}
		_mercantes.put(perso.getID(), perso);
		return true;
	}
	
	public int cantMercantes() {
		return _mercantes.size();
	}
	
	public int cantPersonajes() {
		return _personajes == null ? 0 : _personajes.size();
	}
	
	public int cantNpcs() {
		return _npcs.size();
	}
	
	public int cantMobs() {
		return _grupoMobsTotales.size();
	}
	
	public boolean puedeAgregarOtraPelea() {
		return _peleas.size() < _maxPeleas;
	}
	
	public String getGMPrisma() {
		if (_prisma == null) {
			return "";
		}
		return "GM|+" + _prisma.stringGM();
	}
	
	public String getGMRecaudador() {
		if (_recaudador == null) {
			return "";
		}
		return "GM|+" + _recaudador.stringGM();
	}
	
	public String getGMsPersonajes(Personaje perso) {
		final StringBuilder str = new StringBuilder("GM");
		try {
			boolean i = !perso.esIndetectable();
			for (final Personaje p : getArrayPersonajes()) {
				if (i && p.esIndetectable()) {
					continue;
				}
				if (removePersonajeBug(p)) {
					continue;
				}
				if (p.getPelea() == null) {
					str.append("|+" + p.stringGM());
				}
			}
		} catch (final Exception e) {
			return getGMsPersonajes(perso);
		}
		if (str.length() < 3) {
			return "";
		}
		return str.toString();
	}
	
	private boolean removePersonajeBug(Personaje perso) {
		if (!perso.enLinea()) {
			try {
				perso.setCelda(null);
			} catch (Exception e) {}
			removerPersonaje(perso);
			return true;
		}
		return false;
	}
	
	public String getGMsLuchadores(int idMirador) {
		final StringBuilder str = new StringBuilder("GM");
		for (final Celda celda : _celdas.values()) {
			try {
				if (celda.getLuchadores() == null)
					return "";
				for (final Luchador luchador : celda.getLuchadores()) {
					str.append("|+" + luchador.stringGM(idMirador));
				}
			} catch (final Exception Exception) {}
		}
		if (str.length() < 3) {
			return "";
		}
		return str.toString();
	}
	
	public String getGMsGrupoMobs() {
		if (_grupoMobsTotales.isEmpty()) {
			return "";
		}
		final StringBuilder str = new StringBuilder("GM");
		String GM;
		for (final GrupoMob grupoMob : _grupoMobsTotales.values()) {
			try {
				GM = grupoMob.stringGM();
				if (GM.isEmpty()) {
					continue;
				}
				str.append("|+" + GM);
			} catch (final Exception Exception) {}
		}
		return str.toString();
	}
	
	public String getGMsNPCs(final Personaje perso) {
		if (_npcs.isEmpty()) {
			return "";
		}
		final StringBuilder str = new StringBuilder("GM");
		for (final NPC npc : _npcs.values()) {
			try {
				str.append("|+" + npc.strinGM(perso));
			} catch (final Exception Exception) {}
		}
		return str.toString();
	}
	
	public String getGMsMercantes() {
		if (_mercantes.isEmpty()) {
			return "";
		}
		final StringBuilder str = new StringBuilder("GM");
		for (final Personaje perso : _mercantes.values()) {
			try {
				str.append("|+" + perso.stringGMmercante());
			} catch (final Exception Exception) {}
		}
		return str.toString();
	}
	
	public String getGMsMonturas(Personaje perso) {
		if (_cercado == null || _cercado.getCriando().isEmpty()) {
			return "";
		}
		final StringBuilder str = new StringBuilder("GM");
		boolean esPublico = _cercado.esPublico();
		for (final Montura montura : _cercado.getCriando().values()) {
			if (esPublico && montura.getDueñoID() != perso.getID()) {
				continue;
			}
			str.append("|+" + montura.stringGM());
		}
		return str.toString();
	}
	
	public String getObjetosCria() {
		if (_cercado == null || _cercado.getObjetosCrianza().isEmpty()) {
			return "";
		}
		final StringBuilder str = new StringBuilder();
		for (final Entry<Short, Objeto> entry : _cercado.getObjetosCrianza().entrySet()) {
			if (str.length() > 0) {
				str.append("|");
			}
			if (_cercado.getDueñoID() == -1) {
				str.append(entry.getKey() + ";" + Constantes.getObjCriaPorMapa(_id) + ";1;1000;1000");
			} else {
				str.append(entry.getKey() + ";" + entry.getValue().getObjModeloID() + ";1;" + entry.getValue().getDurabilidad()
				+ ";" + entry.getValue().getDurabilidadMax());
			}
		}
		return "GDO+" + str.toString();
	}
	
	public String getObjetosInteracGDF() {
		final StringBuilder str = new StringBuilder("GDC");
		final StringBuilder str2 = new StringBuilder("GDF");
		for (final Celda celda : _celdas.values()) {
			if (celda.getObjetoInteractivo() != null) {
				str2.append("|" + celda.getID() + ";" + celda.getObjetoInteractivo().getInfoPacket());
			} else if (celda.getEstado() != 1) {
				str2.append("|" + celda.getID() + ";" + celda.getEstado());
				str.append(celda.getID() + ";aaVaaaaaaa800|");
			}
		}
		if (str.length() == 3 && str2.length() == 3) {
			return "";
		}
		if (str.length() == 3) {
			return str2.toString();
		}
		if (str2.length() == 3) {
			return str.toString();
		}
		return str.toString() + (char) 0x00 + str2.toString();
	}
	
	public short getRandomCeldaIDLibre() {
		final ArrayList<Short> celdaLibre = new ArrayList<Short>();
		for (final Celda celda : _celdas.values()) {
			if (!celda.esCaminable(true) || celda.getPrimerPersonaje() != null) {
				continue;
			}
			if (_cercado != null && _cercado.getCeldasObj().contains(celda.getID()))
				continue;
			celdaLibre.add(celda.getID());
		}
		for (final GrupoMob grupoMob : _grupoMobsTotales.values()) {
			celdaLibre.remove((Object) grupoMob.getCeldaID());
		}
		for (final NPC npc : _npcs.values()) {
			celdaLibre.remove((Object) npc.getCeldaID());
		}
		if (celdaLibre.isEmpty()) {
			return -1;
		}
		return celdaLibre.get(Formulas.getRandomInt(0, celdaLibre.size() - 1));
	}
	
	public void refrescarGrupoMobs() {
		if (MainServidor.MODO_HEROICO || MainServidor.MAPAS_MODO_HEROICO.contains(_id)) {
			return;
		}
		ArrayList<Integer> idsBorrar = new ArrayList<>();
		for (final GrupoMob gm : _grupoMobsTotales.values()) {
			if (gm.getTipo() != TipoGrupo.NORMAL) {
				continue;
			}
			final int id = gm.getID();
			idsBorrar.add(id);
			GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(this, id);
		}
		for (int id : idsBorrar) {
			_grupoMobsTotales.remove(id);
		}
		if (_mobPosibles == null) {
			return;
		}
		if (_mobPosibles.isEmpty()) {
			return;
		}
		for (int i = 1; i <= _maxGrupoMobs; i++) {
			getGrupoMobInicioServer((short) -1, "", MainServidor.INICIO_BONUS_ESTRELLAS_MOBS);
		}
	}
	
	public void moverGrupoMobs(int mover) {
		// String str = "";
		try {
			int cantGruposAMover = 0;
			while (cantGruposAMover < mover) {
				boolean noHay = true;
				for (final GrupoMob grupoMob : _grupoMobsTotales.values()) {
					if (grupoMob.enPelea()) {
						continue;
					}
					if (!MainServidor.PARAM_MOVER_MOBS_FIJOS && grupoMob.getTipo() == TipoGrupo.FIJO) {
						continue;
					}
					noHay = false;
					if (Formulas.getRandomBoolean()) {
						grupoMob.moverGrupoMob(this);
						cantGruposAMover++;
					}
				}
				if (noHay) {
					break;
				}
			}
		} catch (final Exception e) {}
		// return str;
	}
	
	public void subirEstrellasOI(final int cant) {
		for (final ObjetoInteractivo oi : _objInteractivos) {
			oi.subirBonusEstrellas(cant * 20);
		}
	}
	
	public void subirEstrellasMobs(final int cant) {
		for (final GrupoMob grupoMob : _grupoMobsTotales.values()) {
			grupoMob.subirBonusEstrellas(cant * 20);// * (mapaMazmorra() ? 45 ):
		}
	}
	
	public synchronized boolean jugadorLLegaACelda(final Personaje perso, short celdaIDDestino, short celdaIDPacket,
	boolean ok) {
		byte bug = 0;
		try {
			Celda celdaDestino = getCelda(celdaIDDestino);
			if (celdaDestino == null) {
				GestorSalida.ENVIAR_BN_NADA(perso, " FINALIZAR DESPLAZAMIENTO CELDA NULA");
				return false;
			}
			bug = 1;
			if (perso.getMapa().getID() != _id || !perso.estaDisponible(false, true)) {
				return false;
			}
			bug = 2;
			perso.setCelda(celdaDestino);
			Objeto objTirado = celdaDestino.getObjetoTirado();
			bug = 3;
			if (objTirado != null) {
				celdaDestino.setObjetoTirado(null);
				perso.addObjIdentAInventario(objTirado, true);
				GestorSalida.ENVIAR_GDO_OBJETO_TIRAR_SUELO(this, '-', celdaDestino.getID(), 0, false, "");
				GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(perso);
			}
			bug = 4;
			boolean activoInt = false;
			if (celdaIDPacket != celdaIDDestino) {
				// cuando no se puede llegar a cierta celda por un stop:
				final Celda celdaObjetivo = getCelda(celdaIDPacket);
				if (celdaObjetivo != null) {
					ObjetoInteractivo objInteractivo = celdaObjetivo.getObjetoInteractivo();
					if (objInteractivo != null && objInteractivo.getObjIntModelo() == null) {
						activoInt = true;
						perso.realizarOtroInteractivo(celdaObjetivo, objInteractivo);
					}
				}
			}
			bug = 5;
			if (!activoInt) {
				if (ok) {
					if (!perso.estaDisponible(true, true)) {
						bug = 6;
					} else {
						bug = 7;
						for (Personaje p : getArrayPersonajes()) {
							if (removePersonajeBug(p)) {
								continue;
							}
							if (!Constantes.puedeIniciarPelea(perso, p, this, celdaDestino)) {
								continue;
							}
							int agroP = p.getStatsObjEquipados().getStatParaMostrar(Constantes.STAT_AGREDIR_AUTOMATICAMENTE);
							int agroPerso = perso.getStatsObjEquipados().getStatParaMostrar(Constantes.STAT_AGREDIR_AUTOMATICAMENTE);
							Personaje agresor = agroPerso >= agroP ? perso : p;
							Personaje agredido = agroPerso >= agroP ? p : perso;
							iniciarPeleaPVP(agresor, agredido, false);
							return true;
						}
						bug = 8;
						for (final GrupoMob grupoMob : _grupoMobsTotales.values()) {
							if (!Constantes.puedeIniciarPelea(perso, grupoMob, this, celdaDestino)) {
								// System.out.println("no cumple las condiciones para iniciar pelea");
								continue;
							}
							Pelea pelea = iniciarPelea(perso, null, perso.getCelda().getID(), (short) -1, Constantes.PELEA_TIPO_PVM,
							grupoMob);
							if (perso.esMaestro() && pelea != null) {
								perso.getGrupoParty().unirAPelea(pelea);
							}
							return true;
						}
					}
				}
				bug = 9;
				celdaDestino.aplicarAccion(perso);
			}
		} catch (Exception e) {
			String error = "EXCEPTION jugadorLLegaACelda bug: " + bug + " e:" + e.toString();
			GestorSalida.ENVIAR_BN_NADA(perso, error);
			MainServidor.redactarLogServidorln(error);
		}
		return ok;
	}
	
	private boolean addSigGrupoMobEnCola(GrupoMob grupoMob) {
		GrupoMob gm = null;
		if (_grupoMobsEnCola != null) {
			for (GrupoMob g : _grupoMobsEnCola) {
				if (g.getTipo() != grupoMob.getTipo()) {
					continue;
				}
				if (g.getStrGrupoMob().equalsIgnoreCase(grupoMob.getStrGrupoMob())) {
					gm = g;
					break;
				}
			}
		}
		if (gm != null) {
			_grupoMobsEnCola.remove(gm);
			addGrupoMobSioSi(gm);
			return true;
		}
		return false;
	}
	
	// public synchronized void addSigGrupoMobRespawn(GrupoMob grupoMob) {
	// GrupoMob gm = null;
	// if (grupoMob.getTipo() == TipoGrupo.NORMAL) {
	// gm = addGrupoMobPosible(grupoMob.getCeldaID());
	// } else if (grupoMob.getTipo() == TipoGrupo.FIJO) {
	// gm = addGrupoMobPorTipo(grupoMob.getCeldaID(), grupoMob.getStrGrupoMob(), grupoMob.getTipo(),
	// grupoMob
	// .getCondInicioPelea());
	// } else if (grupoMob.getTipo() == TipoGrupo.HASTA_QUE_MUERA) {
	// addGrupoMobSioSi(grupoMob);
	// }
	// }
	public synchronized void addSiguienteGrupoMob(GrupoMob grupoMob, boolean filtro) {
		if (grupoMob == null) {
			return;
		}
		switch (grupoMob.getTipo()) {
			case HASTA_QUE_MUERA :
			case SOLO_UNA_PELEA :
			case NORMAL :
				return;
			case FIJO :
				if (filtro) {
					if (!addSigGrupoMobEnCola(grupoMob)) {
						new AparecerMobs(this, grupoMob, Aparecer.INICIO_PELEA);
					}
					return;
				}
				GrupoMob gm = addGrupoMobPorTipo(grupoMob.getCeldaID(), grupoMob.getStrGrupoMob(), TipoGrupo.FIJO, grupoMob
				.getCondInicioPelea(), grupoMob.getMapasRandom());
				if (gm != null) {
					gm.setSegundosRespawn(grupoMob.getSegundosRespawn());
				}
				break;
		}
	}
	
	public synchronized void addUltimoGrupoMob(GrupoMob grupoMob, boolean filtro) {
		if (grupoMob == null) {
			return;
		}
		switch (grupoMob.getTipo()) {
			case SOLO_UNA_PELEA :
				return;
			case HASTA_QUE_MUERA :
				if (!grupoMob.estaMuerto()) {
					addGrupoMobSioSi(grupoMob);
				}
				return;
			case NORMAL :
				// System.out.println("estaMuerto: " + grupoMob.estaMuerto());
				// System.out.println("esHeroico: " + grupoMob.esHeroico());
				if (filtro) {
					// if (addSigGrupoMobEnCola()) {
					// return;
					// }
					if (!grupoMob.estaMuerto() && grupoMob.esHeroico()) {
						addGrupoMobSioSi(grupoMob);
					} else {
						new AparecerMobs(this, grupoMob, Aparecer.FINAL_PELEA);
					}
					return;
				}
				short celdaID = grupoMob.getCeldaID();
				if (MainServidor.PARAM_MOBS_RANDOM_REAPARECER_OTRA_CELDA) {
					celdaID = -1;
				}
				GrupoMob gm = null;
				if (grupoMob.getFijo()) {
					// esta en duda, porq en gestorsql, al normal se le cambia a fijo
					gm = addGrupoMobPorTipo(celdaID, grupoMob.getStrGrupoMob(), TipoGrupo.NORMAL, grupoMob.getCondInicioPelea(),
					grupoMob.getMapasRandom());
				} else {
					gm = addGrupoMobPosible(celdaID);
				}
				if (gm != null) {
					gm.setSegundosRespawn(grupoMob.getSegundosRespawn());
				}
				break;
			case FIJO :
				if (!grupoMob.estaMuerto() && grupoMob.esHeroico()) {
					addUltimoGrupoMob(grupoMob);
				}
				break;
		}
	}
	
	private void addUltimoGrupoMob(final GrupoMob grupoMob) {
		if (grupoMob.estaMuerto()) {
			return;
		}
		if (MainServidor.MODO_HEROICO || MainServidor.MAPAS_MODO_HEROICO.contains(_id)) {
			_grupoMobsEnCola.add(grupoMob);
		}
	}
	
	// --------------------------------
	public synchronized int sigIDGrupoMob() {
		for (int id = -1; id >= -50; id--) {
			if (_grupoMobsTotales.get(id) == null) {
				boolean usado = false;
				for (Pelea pelea : _peleas.values()) {
					if (pelea.getIDLuchInit2() == id) {
						usado = true;
						break;
					}
				}
				if (usado) {
					continue;
				}
				return id;
			}
		}
		return -1;
	}
	
	private synchronized GrupoMob getGrupoMobInicioServer(short celdaID, final String heroico, int estrellas) {
		if ((_mobPosibles == null || _mobPosibles.isEmpty()) || _grupoMobsTotales.size() >= _maxGrupoMobs) {
			return null;
		}
		GrupoMob grupoMob = null;
		if (!heroico.isEmpty()) {
			String strMob = heroico.split(Pattern.quote("|"))[0];
			grupoMob = addGrupoMobPorTipo(celdaID, strMob, TipoGrupo.NORMAL, "", null);
			grupoMob.addObjetosKamasInicioServer(heroico);
		} else {
			grupoMob = addGrupoMobPosible(celdaID);
		}
		if (grupoMob == null) {
			return null;
		}
		grupoMob.setBonusEstrellas(estrellas);
		return grupoMob;
	}
	
	// --------------------------
	// AQUI ES PARA MOSTRAR LOS GRUPOS DE MOBS
	public synchronized GrupoMob addGrupoMobSioSi(final GrupoMob grupoMob) {
		grupoMob.setID(sigIDGrupoMob());
		_grupoMobsTotales.put(grupoMob.getID(), grupoMob);
		if (cantPersonajes() > 0) {
			GestorSalida.ENVIAR_GM_GRUPOMOB_A_MAPA(this, "+" + grupoMob.stringGM());
		}
		return grupoMob;
	}
	
	private synchronized GrupoMob addGrupoMobPosible(final short celdaID) {
		if (_mobPosibles == null || _mobPosibles.isEmpty()) {
			return null;
		}
		final GrupoMob grupoMob = new GrupoMob(_mobPosibles, this, celdaID, _maxMobsPorGrupo);
		if (grupoMob.getMobs().isEmpty()) {
			return null;
		}
		_grupoMobsTotales.put(grupoMob.getID(), grupoMob);
		if (cantPersonajes() > 0) {
			GestorSalida.ENVIAR_GM_GRUPOMOB_A_MAPA(this, "+" + grupoMob.stringGM());
		}
		return grupoMob;
	}
	
	public synchronized GrupoMob addGrupoMobPorTipo(final short celdaID, final String strGrupoMob, final TipoGrupo tipo,
	String condicion, ArrayList<Mapa> mapas) {
		Mapa mapa = this;
		if (mapas != null && mapas.size() > 1) {
			mapa = mapas.get(Formulas.getRandomInt(0, mapas.size() - 1));
		} else {
			mapas = null;
		}
		GrupoMob grupoMob = new GrupoMob(mapa, celdaID, strGrupoMob, tipo, condicion);
		if (grupoMob.getMobs().isEmpty()) {
			return null;
		}
		grupoMob.setMapasRandom(mapas);
		mapa._grupoMobsTotales.put(grupoMob.getID(), grupoMob);
		if (mapa.cantPersonajes() > 0) {
			GestorSalida.ENVIAR_GM_GRUPOMOB_A_MAPA(mapa, "+" + grupoMob.stringGM());
		}
		return grupoMob;
	}
	
	public synchronized void iniciarPeleaPVP(Personaje agresor, Personaje agredido, boolean deshonor) {
		agresor.botonActDesacAlas('+');
		agredido.setAgresion(true);
		agresor.setAgresion(true);
		GestorSalida.ENVIAR_GA_ACCION_JUEGO_AL_MAPA(this, -1, 906, agresor.getID() + "", agredido.getID() + "");
		iniciarPelea(agresor, agredido, agresor.getCelda().getID(), agredido.getCelda().getID(), Constantes.PELEA_TIPO_PVP,
		null);
		agresor.getPelea().setDeshonor(deshonor);
		agredido.setAgresion(false);
		agresor.setAgresion(false);
	}
	
	public synchronized Pelea iniciarPelea(final PreLuchador pre1, PreLuchador pre2, short celda1, short celda2,
	final byte tipo, final GrupoMob grupoMob) {
		try {
			if (!puedeAgregarOtraPelea()) {
				return null;
			}
			int cant = grupoMob != null ? grupoMob.getCantMobs() : 1;
			if (!prepararCeldasPelea(1, cant)) {
				return null;
			}
			if (grupoMob != null) {
				if (grupoMob.enPelea()) {
					return null;
				}
				pre2 = grupoMob.getMobs().get(0).invocarMob(grupoMob.getID(), false, null);
				celda2 = Camino.getCeldaIDCercanaLibre(_celdas.get(grupoMob.getCeldaID()), this);
			}
			short id = sigIDPelea();
			Pelea pelea = new Pelea(id, this, pre1, pre2, celda1, celda2, tipo, grupoMob, strCeldasPeleaPosAtacante());
			_peleas.put(id, pelea);
			if (grupoMob != null) {
				_grupoMobsTotales.remove(grupoMob.getID());
				// _grupoMobsFix.remove(grupoMob.getID());
				for (int i = 1; i < grupoMob.getMobs().size(); i++) {
					MobGrado mob = grupoMob.getMobs().get(i).invocarMob(pelea.sigIDLuchadores(), false, null);
					if (mob.getID() == pre2.getID())
						continue;
					pelea.unirsePelea(mob, pre2.getID());
				}
			}
			GestorSalida.ENVIAR_fC_CANTIDAD_DE_PELEAS(this);
			return pelea;
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION iniciarPelea " + e.toString());
			e.printStackTrace();
		}
		return null;
	}
	
	public synchronized boolean iniciarPeleaKoliseo(final GrupoKoliseo init1, final GrupoKoliseo init2) {
		if (!prepararCeldasPelea(MainServidor.CANTIDAD_MIEMBROS_EQUIPO_KOLISEO,
		MainServidor.CANTIDAD_MIEMBROS_EQUIPO_KOLISEO)) {
			return false;
		}
		short id = sigIDPelea();
		final Pelea pelea = new Pelea(id, this, init1, init2, strCeldasPeleaPosAtacante());
		_peleas.put(id, pelea);
		GestorSalida.ENVIAR_fC_CANTIDAD_DE_PELEAS(this);
		return true;
	}
	
	private String strCeldasPeleaPosAtacante() {
		return _celdasPelea + "|" + _colorCeldaAtacante;
	}
	
	public synchronized short sigIDPelea() {
		for (short id = 1; true; id++) {
			if (_peleas.get(id) == null) {
				return id;
			}
		}
	}
	
	public int getNumeroPeleas() {
		return _peleas.size();
	}
	
	public Map<Short, Pelea> getPeleas() {
		return _peleas;
	}
	
	public void borrarPelea(final short id) {
		_peleas.remove(id);
	}
	
	public Pelea getPelea(final short id) {
		return _peleas.get(id);
	}
	
	public ObjetoInteractivo getPuertaCercado() {
		for (final Celda c : _celdas.values()) {
			try {
				if (c.getObjetoInteractivo().getObjIntModelo().getID() == 120) {
					return c.getObjetoInteractivo();
				}
			} catch (final Exception e) {}
		}
		return null;
	}
	
	public Map<Integer, GrupoMob> getGrupoMobsTotales() {
		return _grupoMobsTotales;
	}
	
	// public CopyOnWriteArrayList<GrupoMob> getGrupoMobsHeroicos() {
	// return _grupoMobsHeroicos;
	// }
	public void borrarGrupoMob(final int id) {
		_grupoMobsTotales.remove(id);
	}
	
	public void borrarTodosMobsNoFijos() {
		_mobPosibles.clear();
		ArrayList<Integer> idsBorrar = new ArrayList<>();
		for (final GrupoMob gm : _grupoMobsTotales.values()) {
			if (gm.getTipo() == TipoGrupo.FIJO) {
				continue;
			}
			final int id = gm.getID();
			idsBorrar.add(id);
			GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(this, id);
		}
		for (int id : idsBorrar) {
			_grupoMobsTotales.remove(id);
		}
		GestorSQL.UPDATE_SET_MOBS_MAPA(_id, "");
	}
	
	public void borrarTodosMobsFijos() {
		ArrayList<Integer> idsBorrar = new ArrayList<>();
		for (final GrupoMob gm : _grupoMobsTotales.values()) {
			if (gm.getTipo() != TipoGrupo.FIJO) {
				continue;
			}
			final int id = gm.getID();
			idsBorrar.add(id);
			GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(this, id);
		}
		for (int id : idsBorrar) {
			_grupoMobsTotales.remove(id);
		}
		GestorSQL.DELETE_MOBS_FIX_MAPA(_id);
	}
	
	public synchronized void salvarMapaHeroico() {
		if (_grupoMobsTotales.isEmpty() && _grupoMobsEnCola.isEmpty()) {
			GestorSQL.DELETE_MAPA_HEROICO(_id);
			return;
		}
		StringBuilder mobs = new StringBuilder();
		StringBuilder objetos = new StringBuilder();
		StringBuilder kamas = new StringBuilder();
		ArrayList<GrupoMob> grupoMobs = new ArrayList<>();
		grupoMobs.addAll(_grupoMobsTotales.values());
		grupoMobs.addAll(_grupoMobsEnCola);
		boolean paso = false;
		for (GrupoMob g : grupoMobs) {
			if (g.getKamasHeroico() <= 0 && g.cantObjHeroico() == 0) {
				continue;
			}
			if (paso) {
				mobs.append("|");
				objetos.append("|");
				kamas.append("|");
			}
			mobs.append(g.getStrGrupoMob());
			objetos.append(g.getIDsObjeto());
			kamas.append(g.getKamasHeroico());
			paso = true;
		}
		if (!paso) {
			GestorSQL.DELETE_MAPA_HEROICO(_id);
			return;
		}
		GestorSQL.REPLACE_MAPAS_HEROICO(_id, mobs.toString(), objetos.toString(), kamas.toString());
	}
	
	public void objetosTirados(final Personaje perso) {
		for (final Celda c : _celdas.values()) {
			if (c.getObjetoTirado() != null) {
				GestorSalida.ENVIAR_GDO_OBJETO_TIRAR_SUELO(perso, '+', c.getID(), c.getObjetoTirado().getObjModelo().getID(),
				false, "");
			}
		}
	}
	
	public Celda getCeldaPorPos(byte x, byte y) {
		for (final Celda c : _celdas.values()) {
			if (c.getCoordX() == x && c.getCoordY() == y) {
				return c;
			}
		}
		return null;
	}
}
