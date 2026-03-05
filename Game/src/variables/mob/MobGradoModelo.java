package variables.mob;

import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import variables.hechizo.Hechizo;
import variables.hechizo.StatHechizo;
import variables.pelea.Luchador;
import variables.stats.Stats;
import estaticos.Constantes;
import estaticos.Mundo;

public class MobGradoModelo {
	private final byte _grado;
	private short _nivel;
	private int _PDVMAX, _baseXP, _minKamas, _maxKamas;
	private String _resistencias, _spells;
	private final MobModelo _mobModelo;
	private final Stats _stats = new Stats();
	private Map<Integer, StatHechizo> _hechizos = new TreeMap<Integer, StatHechizo>();
	private static int[] ORDEN_RESISTENCIAS = {Constantes.STAT_MAS_RES_PORC_NEUTRAL, Constantes.STAT_MAS_RES_PORC_TIERRA,
	Constantes.STAT_MAS_RES_PORC_FUEGO, Constantes.STAT_MAS_RES_PORC_AGUA, Constantes.STAT_MAS_RES_PORC_AIRE,
	Constantes.STAT_MAS_ESQUIVA_PERD_PA, Constantes.STAT_MAS_ESQUIVA_PERD_PM};
	
	public MobGradoModelo(final MobModelo modelo, final byte grado, final int PA, final int PM, final String resist,
	final String stats, final String hechizos, final int pdvMax, final int iniciativa, final int exp, final int minKamas,
	final int maxKamas) {
		_mobModelo = modelo;
		_grado = grado;
		_PDVMAX = pdvMax;
		_baseXP = exp;
		_resistencias = resist;
		if (!hechizos.equals("-1")) {
			_spells = hechizos;
		}
		_minKamas = minKamas;
		_maxKamas = maxKamas;
		Map<Integer, Integer> mapStats = new TreeMap<Integer, Integer>();
		mapStats.put(Constantes.STAT_MAS_PA, PA);
		mapStats.put(Constantes.STAT_MAS_PM, PM);
		int i = -1;
		for (String sValor : resist.split(",")) {
			try {
				if (sValor.isEmpty()) {
					continue;
				}
				switch (i) {
					case -1 :
						_nivel = Short.parseShort(sValor);
						break;
					default :
						mapStats.put(ORDEN_RESISTENCIAS[i], Integer.parseInt(sValor));
						break;
				}
				i++;
			} catch (Exception e) {}
		}
		// STATS
		i = 0;
		for (String s : stats.split(",")) {
			try {
				if (s.isEmpty()) {
					continue;
				}
				if (s.contains(":")) {
					String[] s2 = s.split(":");
					mapStats.put(Integer.parseInt(s2[0]), Integer.parseInt(s2[1]));
				} else {
					i++;
					int idStat = 0;
					switch (i) {
						case 1 :
							idStat = Constantes.STAT_MAS_FUERZA;
							break;
						case 2 :
							idStat = Constantes.STAT_MAS_INTELIGENCIA;
							break;
						case 3 :
							idStat = Constantes.STAT_MAS_SUERTE;
							break;
						case 4 :
							idStat = Constantes.STAT_MAS_AGILIDAD;
							break;
						default :
							continue;
					}
					mapStats.put(idStat, Integer.parseInt(s));
				}
			} catch (Exception e) {}
		}
		if (mapStats.get(Constantes.STAT_MAS_CRIATURAS_INVO) == null) {
			mapStats.put(Constantes.STAT_MAS_CRIATURAS_INVO, 1);
		}
		if (mapStats.get(Constantes.STAT_MAS_INICIATIVA) == null) {
			mapStats.put(Constantes.STAT_MAS_INICIATIVA, iniciativa);
		}
		_stats.nuevosStats(mapStats);
		setHechizos(hechizos);
	}
	
	public void setHechizos(String hechizos) {
		final String[] aHechizo = hechizos.split(";");
		for (final String str : aHechizo) {
			if (str.isEmpty()) {
				continue;
			}
			final String[] hechizoInfo = str.split("@");
			int hechizoID = 0, hechizoNivel = 0;
			try {
				hechizoID = Integer.parseInt(hechizoInfo[0]);
				hechizoNivel = Integer.parseInt(hechizoInfo[1]);
			} catch (final Exception e) {
				continue;
			}
			if (hechizoID <= 0 || hechizoNivel <= 0) {
				continue;
			}
			final Hechizo hechizo = Mundo.getHechizo(hechizoID);
			if (hechizo == null) {
				continue;
			}
			final StatHechizo hechizoStats = hechizo.getStatsPorNivel(hechizoNivel);
			if (hechizoStats == null) {
				continue;
			}
			_hechizos.put(hechizoID, hechizoStats);
		}
	}
	
	public MobGrado invocarMob(int id, boolean clon, Luchador invocador) {
		final MobGrado copia = new MobGrado(this, _mobModelo, _grado, _nivel, _PDVMAX, _stats);
		if (clon) {
			copia.getStats().fijarStatID(Constantes.STAT_MAS_CRIATURAS_INVO, 0);
			if (invocador != null) {
				copia.setPDVMAX(invocador.getPDVMaxSinBuff());
				copia.setPDV(invocador.getPDVSinBuff());
			}
		} else if (invocador != null) {
			float coefStats = 1;
			float coefVita = 1;
			Stats stats = copia.getStats();
			while (invocador.esInvocacion()) {
				invocador = invocador.getInvocador();
				// coefStats -= 0.3f;
				stats.fijarStatID(Constantes.STAT_MAS_CRIATURAS_INVO, 0);
			}
			if (invocador.getPersonaje() != null) {
				coefVita += invocador.getNivel() / 100f;
				coefStats += invocador.getNivel() / 100f;
			}
			int[] s = {Constantes.STAT_MAS_FUERZA, Constantes.STAT_MAS_INTELIGENCIA, Constantes.STAT_MAS_SUERTE,
			Constantes.STAT_MAS_AGILIDAD};
			for (int i : s) {
				stats.fijarStatID(i, (int) (stats.getStatParaMostrar(i) * coefStats));
			}
			copia.setPDVMAX((int) (_PDVMAX * coefVita));// + Math.sqrt(invocador.getPDVMaxSinBuff())
			copia.setPDV(copia.getPDVMax());
		}
		copia.setIDPersonal(id);
		return copia;
	}
	
	public String stringStatsActualizado() {
		StringBuilder strStats = new StringBuilder();
		for (Entry<Integer, Integer> entry : _stats.getEntrySet()) {
			switch (entry.getKey()) {
				case Constantes.STAT_MAS_PA :
				case Constantes.STAT_MAS_PM :
				case Constantes.STAT_MAS_CRIATURAS_INVO :
				case Constantes.STAT_MAS_SUERTE :
				case Constantes.STAT_MAS_AGILIDAD :
				case Constantes.STAT_MAS_FUERZA :
				case Constantes.STAT_MAS_INTELIGENCIA :
				case Constantes.STAT_MAS_INICIATIVA :
					if (strStats.length() > 0) {
						strStats.append(",");
					}
					strStats.append(entry.getKey() + ":" + entry.getValue());
					break;
			}
		}
		return strStats.toString();
	}
	
	public int getMinKamas() {
		return _minKamas;
	}
	
	public int getMaxKamas() {
		return _maxKamas;
	}
	
	public void setMinKamas(int kamas) {
		_minKamas = kamas;
	}
	
	public void setMaxKamas(int kamas) {
		_maxKamas = kamas;
	}
	
	public String getSpells() {
		return _spells;
	}
	
	public String getResistencias() {
		return _resistencias;
	}
	
	public int getBaseXp() {
		return _baseXP;
	}
	
	public void setBaseXp(int xp) {
		_baseXP = xp;
	}
	
	public void setPDVMAX(int pdv) {
		_PDVMAX = pdv;
	}
	
	public Stats getStats() {
		return _stats;
	}
	
	int getPA() {
		return _stats.getStatParaMostrar(Constantes.STAT_MAS_PA);
	}
	
	int getPM() {
		return _stats.getStatParaMostrar(Constantes.STAT_MAS_PM);
	}
	
	public short getNivel() {
		return _nivel;
	}
	
	public Map<Integer, StatHechizo> getHechizos() {
		return _hechizos;
	}
	
	public MobModelo getMobModelo() {
		return _mobModelo;
	}
	
	public int getIDModelo() {
		return _mobModelo.getID();
	}
	
	public int getPDVMAX() {
		return _PDVMAX;
	}
	
	public byte getGrado() {
		return _grado;
	}
	
	public short getGfxID() {
		return _mobModelo.getGfxID();
	}
}