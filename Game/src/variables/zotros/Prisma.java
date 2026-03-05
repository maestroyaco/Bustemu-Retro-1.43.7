package variables.zotros;

import java.util.Map;
import java.util.TreeMap;
import sprites.PreLuchador;
import variables.hechizo.StatHechizo;
import variables.mapa.Area;
import variables.mapa.Celda;
import variables.mapa.Mapa;
import variables.mapa.SubArea;
import variables.mob.MobGradoModelo;
import variables.pelea.Luchador;
import variables.pelea.Pelea;
import variables.personaje.Personaje;
import variables.stats.Stats;
import variables.stats.TotalStats;
import estaticos.Constantes;
import estaticos.MainServidor;
import estaticos.GestorSalida;
import estaticos.Mundo;

public class Prisma implements PreLuchador {
	private final int _id;
	private int _honor = 0, _PDVMAX = 0;
	private final byte _alineacion, _dir;
	private int _nivel;
	private long _tiempoProteccion;
	private final Mapa _mapa;
	private final Celda _celda;
	private short _idMob, _gfx;
	private Area _area;
	private SubArea _subArea;
	private Pelea _pelea;
	private final Stats _stats = new Stats();
	private final TotalStats _totalStats = new TotalStats(_stats, null, new Stats(), null, 4);
	private final Map<Integer, StatHechizo> _hechizos = new TreeMap<Integer, StatHechizo>();
	
	// public void destruir() {
	// try {
	// this.finalize();
	// } catch (Throwable e) {}
	// }
	//
	public Prisma(final int id, final byte alineacion, final byte nivel, final short mapaID, final short celdaID,
	final int honor, final int area, final int subArea, final long tiempoProteccion) {
		_id = id;
		_alineacion = alineacion;
		_nivel = nivel;
		_mapa = Mundo.getMapa(mapaID);
		_celda = _mapa.getCelda(celdaID);
		_dir = 1;
		if (_alineacion == Constantes.ALINEACION_BONTARIANO) {
			_idMob = 1111;
		} else {
			_idMob = 1112;
		}
		MobGradoModelo mob = Mundo.getMobModelo(_idMob).getGradoPorNivel((int) Math.ceil(_nivel / 2f));
		actualizarStats(mob);
		_gfx = mob.getGfxID();
		_honor = honor;
		_subArea = Mundo.getSubArea(subArea);
		_area = Mundo.getArea(area);
		_tiempoProteccion = tiempoProteccion;
	}
	
	public void addTiempProtecion(int segundos) {
		long l = Math.max(System.currentTimeMillis(), _tiempoProteccion);
		l += (segundos * 1000l);
		if (l < System.currentTimeMillis()) {
			l = 0;
		}
		_tiempoProteccion = l;
	}
	
	public long getTiempoRestProteccion() {
		long l = _tiempoProteccion - System.currentTimeMillis();
		if (l < 0) {
			l = 0;
		}
		return l;
	}
	
	public long getTiempoProteccion() {
		return _tiempoProteccion;
	}
	
	public int getID() {
		return _id;
	}
	
	public Area getArea() {
		return _area;
	}
	
	public SubArea getSubArea() {
		return _subArea;
	}
	
	public byte getAlineacion() {
		return _alineacion;
	}
	
	public Map<Integer, StatHechizo> getHechizos() {
		return _hechizos;
	}
	
	private void actualizarStats(MobGradoModelo mob) {
		_stats.nuevosStats(mob.getStats());
		_stats.fijarStatID(Constantes.STAT_MAS_INICIATIVA, _nivel * 1000);
		_hechizos.putAll(mob.getHechizos());
		_PDVMAX = mob.getPDVMAX();
	}
	
	public int getEstadoPelea() {
		if (_pelea == null) {
			return -1;
		}
		if (_pelea.getFase() == Constantes.PELEA_FASE_POSICION) {
			return 0;
		}
		if (_pelea.getFase() == Constantes.PELEA_FASE_COMBATE) {
			return -2;
		}
		return 4;
	}
	
	public void setPelea(final Pelea pelea) {
		_pelea = pelea;
	}
	
	public Pelea getPelea() {
		return _pelea;
	}
	
	public int getHonor() {
		return _honor;
	}
	
	public void addHonor(final int honor) {
		_honor += honor;
		int nivel = _nivel;
		if (_honor < 0) {
			_honor = 0;
		} else if (_honor >= Mundo.getExpAlineacion(MainServidor.NIVEL_MAX_ALINEACION)) {
			_nivel = MainServidor.NIVEL_MAX_ALINEACION;
			_honor = Mundo.getExpAlineacion(MainServidor.NIVEL_MAX_ALINEACION);
		}
		for (byte n = 1; n <= MainServidor.NIVEL_MAX_ALINEACION; n++) {
			if (_honor < Mundo.getExpAlineacion(n)) {
				_nivel = (byte) (n - 1);
				break;
			}
		}
		if (nivel != _nivel) {
			MobGradoModelo mob = Mundo.getMobModelo(_idMob).getGradoPorNivel((int) Math.ceil(_nivel / 2f));
			actualizarStats(mob);
		}
	}
	
	public String stringGM() {
		if (_pelea != null) {
			return "";
		}
		return _celda.getID() + ";" + _dir + ";0;" + _id + ";" + _idMob + ";-10;" + _gfx + "^100;" + _nivel + ";" + _nivel
		+ ";" + _alineacion;
	}
	
	public String atacantesDePrisma() {
		final StringBuilder str = new StringBuilder("+" + Integer.toString(_id, 36));
		for (final Luchador luchador : _pelea.luchadoresDeEquipo(1)) {
			final Personaje perso = luchador.getPersonaje();
			if (perso == null) {
				continue;
			}
			str.append("|" + Integer.toString(perso.getID(), 36) + ";");
			str.append(perso.getNombre() + ";");
			str.append(perso.getNivel() + ";");
		}
		return str.toString();
	}
	
	public String defensoresDePrisma() {
		final StringBuilder str = new StringBuilder("+" + Integer.toString(_id, 36));
		final StringBuilder stra = new StringBuilder("-");
		for (final Luchador luchador : _pelea.luchadoresDeEquipo(2)) {
			final Personaje perso = luchador.getPersonaje();
			if (perso == null) {
				continue;
			}
			str.append("|" + Integer.toString(perso.getID(), 36) + ";");
			str.append(perso.getNombre() + ";");
			str.append(perso.getGfxID(false) + ";");
			str.append(perso.getNivel() + ";");
			if (_pelea.cantLuchDeEquipo(2) >= 8) {
				str.append("1;");
			} else {
				str.append("0;");
			}
		}
		stra.append(str.substring(1));
		_pelea.setListaDefensores(stra.toString());
		return str.toString();
	}
	
	public void actualizarAtacantesDefensores() {
		final String str = atacantesDePrisma();
		final String str2 = defensoresDePrisma();
		for (final Personaje perso : Mundo.getPersonajesEnLinea()) {
			if (perso.getAlineacion() == _alineacion) {
				GestorSalida.ENVIAR_CP_INFO_DEFENSORES_PRISMA(perso, str2);
				GestorSalida.ENVIAR_Cp_INFO_ATACANTES_PRISMA(perso, str);
			}
		}
	}
	
	public String analizarPrismas(byte alineacion) {
		if (alineacion != _alineacion) {
			return "-3";
		} else if (getEstadoPelea() == 0) {
			return "0;" + getPelea().getTiempoFaltInicioPelea() + ";" + (MainServidor.SEGUNDOS_INICIO_PELEA * 1000) + ";7";
		} else {
			return getEstadoPelea() + "";
		}
	}
	
	public Mapa getMapa() {
		return _mapa;
	}
	
	public Celda getCelda() {
		return _celda;
	}
	
	public int getGfxID(boolean buff) {
		return _gfx;
	}
	
	public int getPDVMax() {
		return _PDVMAX;
	}
	
	public int getPDV() {
		return _PDVMAX;
	}
	
	public TotalStats getTotalStatsPelea() {
		return _totalStats;
	}
	
	public int getNivel() {
		return _nivel;
	}
	
	public String stringGMLuchador() {
		StringBuilder str = new StringBuilder();
		str.append("-2;");
		str.append((_alineacion == 1 ? 8101 : 8100) + "^100;");
		str.append(_nivel + ";");
		str.append("-1;-1;-1;");
		str.append("0,0,0,0;");
		return str.toString();
	}
	
	public void sobrevivio() {
		final String str = _mapa.getID() + "|" + _mapa.getX() + "|" + _mapa.getY();
		for (final Personaje pj : Mundo.getPersonajesEnLinea()) {
			if (pj.getAlineacion() == _alineacion) {
				GestorSalida.ENVIAR_CS_MENSAJE_SOBREVIVIO_PRISMA(pj, str);
			}
		}
		setPelea(null);
		GestorSalida.ENVIAR_GM_PRISMA_A_MAPA(_mapa, "+" + stringGM());
	}
	
	public void murio() {
		String str = _mapa.getID() + "|" + _mapa.getX() + "|" + _mapa.getY();
		for (final Personaje pj : Mundo.getPersonajesEnLinea()) {
			if (pj.getAlineacion() == _alineacion) {
				GestorSalida.ENVIAR_CD_MENSAJE_MURIO_PRISMA(pj, str);
			}
			if (_area != null) {
				GestorSalida.ENVIAR_aM_CAMBIAR_ALINEACION_AREA(pj, _area.getID(), (byte) -1);
			}
			GestorSalida.ENVIAR_am_CAMBIAR_ALINEACION_SUBAREA(pj, _subArea.getID(), Constantes.ALINEACION_NULL, true);
			GestorSalida.ENVIAR_am_CAMBIAR_ALINEACION_SUBAREA(pj, _subArea.getID(), Constantes.ALINEACION_NEUTRAL, false);
		}
		GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(_mapa, _id);
		Mundo.eliminarPrisma(this);
	}
	
	public int getGradoAlineacion() {
		return _nivel;
	}
	
	public int getDeshonor() {
		return 0;
	}
	
	public boolean addDeshonor(int honor) {
		return false;
	}
	
	@Override
	public void addKamasGanada(long kamas) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void addXPGanada(long exp) {
		// TODO Auto-generated method stub
	}
}
