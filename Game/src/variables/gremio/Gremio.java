package variables.gremio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import variables.casa.Casa;
import variables.hechizo.Hechizo;
import variables.hechizo.StatHechizo;
import variables.mapa.Cercado;
import variables.montura.Montura;
import variables.personaje.Personaje;
import variables.stats.Stats;
import estaticos.Constantes;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.MainServidor;
import estaticos.Mundo;

public class Gremio {
	private int _nroMaxRecaudadores;
	private short _nivel = 1, _capital;
	private final int _id;
	private long _experiencia;
	private String _nombre = "", _emblema = "";
	private final Map<Integer, StatHechizo> _hechizos = hechizosPrimarios(
	"462;0|461;0|460;0|459;0|458;0|457;0|456;0|455;0|454;0|453;0|452;0|451;0");;
	private final Map<Integer, Integer> _statsRecolecta = new HashMap<Integer, Integer>();
	private final Map<Short, Long> _tiempoMapaRecolecta = new HashMap<Short, Long>();
	private final Map<Integer, MiembroGremio> _miembros = new HashMap<Integer, MiembroGremio>();
	private final CopyOnWriteArrayList<Recaudador> _recaudadores = new CopyOnWriteArrayList<>();
	private final Stats _statsPelea = new Stats();
	
	public Gremio(final Personaje dueño, final String nombre, final String emblema) {
		_id = Mundo.sigIDGremio();
		_nombre = nombre;
		_emblema = emblema;
		_experiencia = 0;
		decompilarStats("176;100|158;1000|124;0");
	}
	
	public Gremio(final int id, final String nombre, final String emblema, final short nivel, final long xp,
	final short capital, final byte nroMaxRecau, final String hechizos, final String stats) {
		_id = id;
		addExperiencia(xp, true);
		_nombre = nombre;
		_emblema = emblema;
		_capital = capital;
		_nroMaxRecaudadores = nroMaxRecau;
		decompilarHechizos(hechizos);
		decompilarStats(stats);
	}
	
	public MiembroGremio addMiembro(final int id, final int rango, final long expDonada, final byte porcXPDonar,
	final int derechos) {
		final MiembroGremio miembro = new MiembroGremio(id, this, rango, expDonada, porcXPDonar, derechos);
		_miembros.put(id, miembro);
		return miembro;
	}
	
	public int getID() {
		return _id;
	}
	
	public int getCantRecaudadores() {
		return _recaudadores.size();
	}
	
	public int getNroMaxRecau() {
		return _nroMaxRecaudadores;
	}
	
	public void setNroMaxRecau(final int nro) {
		_nroMaxRecaudadores = nro;
	}
	
	public void addRecaudador(Recaudador r) {
		_recaudadores.add(r);
	}
	
	public void delRecaudador(Recaudador r) {
		_recaudadores.remove(r);
	}
	
	public void eliminarTodosRecaudadores() {
		for (Recaudador r : _recaudadores) {
			r.borrarRecaudador();
		}
	}
	
	public String getInfoGremio() {
		return _nombre + "," + getStatRecolecta(Constantes.STAT_MAS_PODS) + "," + getStatRecolecta(
		Constantes.STAT_MAS_PROSPECCION) + "," + getStatRecolecta(Constantes.STAT_MAS_SABIDURIA) + "," + _recaudadores
		.size();
	}
	
	public int getCapital() {
		return _capital;
	}
	
	public void addCapital(final int nro) {
		_capital += nro;
	}
	
	public Map<Integer, StatHechizo> getHechizos() {
		return _hechizos;
	}
	
	public boolean olvidarHechizo(final int hechizoID, boolean porCompleto) {
		final StatHechizo h = _hechizos.get(hechizoID);
		if (h == null) {
			return false;
		}
		for (int i = 1; i < h.getGrado(); i++) {
			_capital += i;
		}
		return fijarNivelHechizoOAprender(hechizoID, porCompleto ? 0 : 1, false);
	}
	
	public boolean boostHechizo(final int hechizoID) {
		if (!_hechizos.containsKey(hechizoID)) {
			return false;
		}
		final StatHechizo SH = _hechizos.get(hechizoID);
		if (SH != null && SH.getGrado() >= 5) {
			return false;
		}
		Hechizo hechizo = Mundo.getHechizo(hechizoID);
		if (hechizo == null) {
			return false;
		}
		int nivel = SH == null ? 1 : SH.getGrado() + 1;
		return fijarNivelHechizoOAprender(hechizoID, nivel, false);
	}
	
	public boolean fijarNivelHechizoOAprender(final int hechizoID, final int nivel, final boolean mensaje) {
		if (nivel > 0) {
			final Hechizo hechizo = Mundo.getHechizo(hechizoID);
			if (hechizo == null) {
				return false;
			}
			final StatHechizo statHechizo = hechizo.getStatsPorNivel(nivel);
			if (statHechizo == null || statHechizo.getNivelRequerido() > _nivel) {
				return false;
			}
			_hechizos.put(hechizoID, statHechizo);
		} else {
			_hechizos.put(hechizoID, null);
		}
		return true;
	}
	
	public Stats getStatsPelea() {
		return _statsPelea;
	}
	
	public String getNombre() {
		return _nombre;
	}
	
	public String getEmblema() {
		return _emblema;
	}
	
	public long getExperiencia() {
		return _experiencia;
	}
	
	public short getNivel() {
		return _nivel;
	}
	
	public int getCantidadMiembros() {
		return _miembros.size();
	}
	
	public void setNombre(String nombre) {
		_nombre = nombre;
	}
	
	public void setEmblema(String emblema) {
		_emblema = emblema;
	}
	
	public String infoPanelGremio() {
		final long xpMin = Mundo.getExpGremio(_nivel);
		final long xpMax = Mundo.getExpGremio(_nivel + 1);
		final String packet = "gIG" + (getCantidadMiembros() >= 10 ? 1 : 0) + "|" + _nivel + "|" + xpMin + "|"
		+ _experiencia + "|" + xpMax;
		return packet;
	}
	
	public String analizarMiembrosGM() {
		final StringBuilder str = new StringBuilder();
		for (final MiembroGremio miembro : _miembros.values()) {
			if (miembro.getPersonaje() == null) {
				continue;
			}
			if (str.length() > 0) {
				str.append("|");
			}
			str.append(miembro.getID() + ";");
			str.append(miembro.getNombre() + ";");
			str.append(miembro.getNivel() + ";");
			str.append(miembro.getGfx() + ";");
			str.append(miembro.getRango() + ";");
			str.append(miembro.getXpDonada() + ";");
			str.append(miembro.getPorcXpDonada() + ";");
			str.append(miembro.getDerechos() + ";");
			str.append((miembro.getPersonaje().enLinea() ? 1 : 0) + ";");
			str.append(miembro.getPersonaje().getAlineacion() + ";");
			str.append(miembro.getHorasDeUltimaConeccion());
		}
		return str.toString();
	}
	
	public ArrayList<Personaje> getMiembros() {
		final ArrayList<Personaje> a = new ArrayList<Personaje>();
		for (final MiembroGremio miembro : _miembros.values()) {
			a.add(miembro.getPersonaje());
		}
		return a;
	}
	
	// public Collection<MiembroGremio> getMiembros() {
	// return _miembros.values();
	// }
	public MiembroGremio getMiembro(final int idMiembro) {
		return _miembros.get(idMiembro);
	}
	
	public void expulsarTodosMiembros() {
		final ArrayList<MiembroGremio> a = new ArrayList<MiembroGremio>();
		a.addAll(_miembros.values());
		for (final MiembroGremio miembro : a) {
			expulsarMiembro(miembro.getID());
		}
	}
	
	public void expulsarMiembro(final int persoID) {
		final Casa casa = Mundo.getCasaDePj(persoID);
		if (casa != null) {
			casa.nullearGremio();
			casa.actualizarDerechos(0);
		}
		_miembros.remove(persoID);
		GestorSQL.DELETE_MIEMBRO_GREMIO(persoID);
		final Personaje perso = Mundo.getPersonaje(persoID);
		if (perso != null) {
			perso.setMiembroGremio(null);
			if (perso.enLinea() && perso.getPelea() == null) {
				GestorSalida.ENVIAR_Oa_CAMBIAR_ROPA_MAPA(perso.getMapa(), perso);
			}
		}
	}
	
	public void addExperiencia(final long xp, boolean sinPuntos) {
		_experiencia += xp;
		final int nivelAnt = _nivel;
		while (_experiencia >= Mundo.getExpGremio(_nivel + 1) && _nivel < MainServidor.NIVEL_MAX_GREMIO) {
			subirNivel(sinPuntos);
		}
		if (!sinPuntos) {
			if (_nivel != nivelAnt) {
				refrescarStatsPelea();
			}
		}
	}
	
	public void subirNivel(boolean sinPuntos) {
		_nivel += 1;
		if (!sinPuntos) {
			_capital += 5;
		}
	}
	
	public void refrescarStatsPelea() {
		Map<Integer, Integer> stats = new TreeMap<Integer, Integer>();
		stats.put(Constantes.STAT_MAS_PA, 6);
		stats.put(Constantes.STAT_MAS_PM, 5);
		stats.put(Constantes.STAT_MAS_SABIDURIA, getStatRecolecta(Constantes.STAT_MAS_SABIDURIA));
		stats.put(Constantes.STAT_MAS_DAÑOS, (int) _nivel);
		int[] statsIDs = {Constantes.STAT_MAS_RES_PORC_NEUTRAL, Constantes.STAT_MAS_RES_PORC_TIERRA,
		Constantes.STAT_MAS_RES_PORC_FUEGO, Constantes.STAT_MAS_RES_PORC_AIRE, Constantes.STAT_MAS_RES_PORC_AGUA,
		Constantes.STAT_MAS_ESQUIVA_PERD_PA, Constantes.STAT_MAS_ESQUIVA_PERD_PM,};
		int resistencia = Math.min(50, _nivel);
		for (int s : statsIDs) {
			stats.put(s, resistencia);
		}
		_statsPelea.nuevosStats(stats);
	}
	
	public void addUltRecolectaMapa(short mapaID) {
		_tiempoMapaRecolecta.put(mapaID, System.currentTimeMillis());
	}
	
	public boolean puedePonerRecaudadorMapa(short mapaID) {
		if (MainServidor.HORAS_VOLVER_A_PONER_RECAUDADOR_MAPA < 1) {
			return true;
		}
		if (_tiempoMapaRecolecta.containsKey(mapaID)) {
			int tiempoM = ((60 * 60 * 1000) * _nivel) / MainServidor.HORAS_VOLVER_A_PONER_RECAUDADOR_MAPA;
			return _tiempoMapaRecolecta.get(mapaID) + tiempoM <= System.currentTimeMillis();
		}
		return true;
	}
	
	public String analizarInfoCercados() {
		final byte maxCercados = (byte) (int) Math.floor(_nivel / 10);
		final StringBuilder str = new StringBuilder(maxCercados);
		for (final Cercado cercados : Mundo.CERCADOS.values()) {
			if (cercados.getGremio() == this) {
				str.append("|" + cercados.getMapa().getID() + ";" + cercados.getCapacidadMax() + ";" + cercados
				.getCantObjMax());
				if (cercados.getCriando().size() > 0) {
					str.append(";");
					boolean primero = false;
					for (final Montura DP : cercados.getCriando().values()) {
						if (DP == null) {
							continue;
						}
						if (primero) {
							str.append(",");
						}
						str.append(DP.getColor() + "," + DP.getNombre() + ",");
						if (Mundo.getPersonaje(DP.getDueñoID()) == null) {
							str.append("SIN DUEÑO");
						} else {
							str.append(Mundo.getPersonaje(DP.getDueñoID()).getNombre());
						}
						primero = true;
					}
				}
			}
		}
		return str.toString();
	}
	
	private Map<Integer, StatHechizo> hechizosPrimarios(String strHechizo) {
		TreeMap<Integer, StatHechizo> hechizos = new TreeMap<Integer, StatHechizo>();
		for (final String split : strHechizo.split(Pattern.quote("|"))) {
			try {
				int id = Integer.parseInt(split.split(";")[0]);
				int nivel = Integer.parseInt(split.split(";")[1]);
				if (Mundo.getHechizo(id) == null) {
					continue;
				}
				hechizos.put(id, Mundo.getHechizo(id).getStatsPorNivel(nivel));
			} catch (Exception e) {}
		}
		return hechizos;
	}
	
	public void decompilarHechizos(final String strHechizo) {
		for (final String split : strHechizo.split(Pattern.quote("|"))) {
			try {
				int id = Integer.parseInt(split.split(";")[0]);
				int nivel = Integer.parseInt(split.split(";")[1]);
				_hechizos.put(id, Mundo.getHechizo(id).getStatsPorNivel(nivel));
			} catch (Exception e) {}
		}
	}
	
	public void decompilarStats(final String statsStr) {
		for (final String split : statsStr.split(Pattern.quote("|"))) {
			try {
				int stat = Integer.parseInt(split.split(";")[0]);
				int cant = Integer.parseInt(split.split(";")[1]);
				_statsRecolecta.put(stat, cant);
			} catch (Exception e) {}
		}
		refrescarStatsPelea();
	}
	
	public String compilarHechizo() {
		final StringBuilder str = new StringBuilder();
		for (final Entry<Integer, StatHechizo> statHechizo : _hechizos.entrySet()) {
			if (str.length() > 0) {
				str.append("|");
			}
			str.append(statHechizo.getKey() + ";" + (statHechizo.getValue() == null ? 0 : statHechizo.getValue().getGrado()));
		}
		return str.toString();
	}
	
	public String compilarStats() {
		final StringBuilder str = new StringBuilder();
		for (final Entry<Integer, Integer> stats : _statsRecolecta.entrySet()) {
			if (str.length() > 0) {
				str.append("|");
			}
			str.append(stats.getKey() + ";" + stats.getValue());
		}
		return str.toString();
	}
	
	public int getStatRecolecta(final int id) {
		return _statsRecolecta.get(id) != null ? _statsRecolecta.get(id) : 0;
	}
	
	public void addStat(final int id, final int add) {
		try {
			_statsRecolecta.put(id, _statsRecolecta.get(id) + add);
		} catch (Exception e) {
			_statsRecolecta.put(id, add);
		}
	}
	
	public String analizarRecauAGremio() {
		return _nroMaxRecaudadores + "|" + _recaudadores.size() + "|" + (100 * _nivel) + "|" + _nivel + "|"
		+ getStatRecolecta(Constantes.STAT_MAS_PODS) + "|" + getStatRecolecta(Constantes.STAT_MAS_PROSPECCION) + "|"
		+ getStatRecolecta(Constantes.STAT_MAS_SABIDURIA) + "|" + _nroMaxRecaudadores + "|" + _capital + "|" + (1000 + (10
		* _nivel)) + "|" + compilarHechizo();
	}
	
	public String analizarRecaudadores() {
		final StringBuilder str = new StringBuilder();
		for (final Recaudador r : _recaudadores) {
			if (str.length() > 0) {
				str.append("|");
			}
			str.append(r.getInfoPanel());
		}
		if (str.length() == 0) {
			return "";
		}
		return "+" + str.toString();
	}
	
	public void actualizarAtacantesDefensores() {
		for (Recaudador re : _recaudadores) {
			re.actualizarAtacantesDefensores();
		}
	}
	
	public int getMaxMiembros() {
		int maxMiembros = 40 + (_nivel * 4);
		if (MainServidor.LIMITE_MIEMBROS_GREMIO > 0) {
			maxMiembros = MainServidor.LIMITE_MIEMBROS_GREMIO;
		}
		return maxMiembros;
	}
}
