package variables.objeto;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import variables.stats.Stats;
import estaticos.Constantes;
import estaticos.MainServidor;
import estaticos.Mundo;

public class ObjetoSet {
	private final int _id;
	private final ArrayList<ObjetoModelo> _objetosModelos = new ArrayList<ObjetoModelo>();
	private final Map<Integer, Stats> _bonus = new TreeMap<>();
	private final String _nombre;
	
	public ObjetoSet(final int id, final String nombre, String objetos) {
		_id = id;
		_nombre = nombre;
		_bonus.clear();
		_bonus.put(1, new Stats());
		for (String s : objetos.split(",")) {
			try {
				int idMod = Integer.parseInt(s.trim());
				ObjetoModelo objMod = Mundo.getObjetoModelo(idMod);
				if (objMod != null) {
					objMod.setSetID(_id);
					_objetosModelos.add(objMod);
				}
			} catch (Exception e) {
				MainServidor.redactarLogServidor("El objeto modelo " + s
				+ " no existe y no se le puede asignar a un objeto set");
			}
		}
	}
	
	public void setStats(String str, int cantObjetos) {
		if (str.isEmpty()) {
			return;
		}
		final Stats stats = new Stats();
		convertirStringAStatsSet(stats, str);
		_bonus.put(cantObjetos, stats);
	}
	
	private static void convertirStringAStatsSet(Stats stats, String strStats) {
		for (final String str : strStats.split(",")) {
			if (str.isEmpty()) {
				continue;
			}
			try {
				final String[] splitStats = str.split("#");
				int statID = ObjetoModelo.statSimiliar(Integer.parseInt(splitStats[0], 16));
				if (Constantes.esStatHechizo(statID)) {
					stats.addStatHechizo(str);
				} else if (Constantes.esStatRepetible(statID)) {
					stats.addStatRepetido(str);
				} else if (Constantes.esStatTexto(statID)) {
					stats.addStatTexto(statID, str, true);
				} else if (Constantes.esEfectoHechizo(statID)) {
					// no da efectos de daño
				} else {
					int valor = Integer.parseInt(splitStats[1], 16);
					stats.addStatID(statID, valor);
				}
			} catch (final Exception e) {}
		}
	}
	
	public ArrayList<ObjetoModelo> getObjetosModelos() {
		return _objetosModelos;
	}
	
	public String getNombre() {
		return _nombre;
	}
	
	public int getID() {
		return _id;
	}
	
	public Stats getBonusStatPorNroObj(final int numero) {
		try {
			return _bonus.get(numero);
		} catch (final Exception e) {
			return _bonus.get(1);
		}
	}
}