package variables.stats;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import variables.objeto.Objeto;
import variables.personaje.Personaje;
import estaticos.Constantes;

public class Stats {
	private Map<Integer, Integer> _statsIDs = new TreeMap<Integer, Integer>();
	private ArrayList<String> _statsHechizos;
	private ArrayList<String> _statsRepetidos;
	private Map<Integer, String> _statsTextos;
	
	public Stats() {}
	
	public Stats(final Stats stats) {
		_statsIDs.putAll(stats._statsIDs);
	}
	
	public Stats(final Map<Integer, Integer> stats) {
		_statsIDs.putAll(stats);
	}
	
	public void nuevosStatsBase(final Map<Integer, Integer> stats, final Personaje perso) {
		_statsIDs.clear();
		if (stats != null) {
			_statsIDs.putAll(stats);
		}
		// _statsIDs.put(Constantes.STAT_MAS_PA, perso.getNivel() < 100 ? Elbustemu.INICIO_PA :
		// Elbustemu.INICIO_PA + 1);// PA
		// _statsIDs.put(Constantes.STAT_MAS_PM, Elbustemu.INICIO_PM);// PM
		// _statsIDs.put(Constantes.STAT_MAS_PROSPECCION, perso.getClase(false) ==
		// Constantes.CLASE_ANUTROF
		// ? Elbustemu.INICIO_PROSPECCION + 20
		// : Elbustemu.INICIO_PROSPECCION);// prospeccion
		// _statsIDs.put(Constantes.STAT_MAS_PODS, Elbustemu.INICIO_PODS);// pods
		// _statsIDs.put(Constantes.STAT_MAS_CRIATURAS_INVO, 1);// invocaciones
		if (perso != null) {
			if (perso.getClase() != null) {
				for (Entry<Integer, Integer> entry : perso.getClase().getStats().entrySet()) {
					addStatID(entry.getKey(), entry.getValue());
				}
			}
			if (perso.getNivel() >= 100) {
				addStatID(Constantes.STAT_MAS_PA, 1);
			}
		}
	}
	
	// private Map<Integer, Integer> getStatsComoMap() {
	// return _statsIDs;
	// }
	public Set<Entry<Integer, Integer>> getEntrySet() {
		return _statsIDs.entrySet();
	}
	
	public ArrayList<String> getStatRepetidos() {
		return _statsRepetidos;
	}
	
	public ArrayList<String> getStatHechizos() {
		return _statsHechizos;
	}
	
	public void nuevosStats(final Map<Integer, Integer> stats) {
		_statsIDs.clear();
		if (stats != null) {
			_statsIDs.putAll(stats);
		}
	}
	
	public void nuevosStats(final Stats stats) {
		_statsIDs.clear();
		if (stats != null) {
			_statsIDs.putAll(stats._statsIDs);
		}
	}
	
	public void clear() {
		_statsIDs.clear();
		_statsHechizos = null;
		_statsRepetidos = null;
		_statsTextos = null;
	}
	
	public void acumularStats(final Stats stats) {
		if (stats == null) {
			return;
		}
		for (final Entry<Integer, Integer> entry : stats._statsIDs.entrySet()) {
			addStatID(entry.getKey(), entry.getValue());
		}
		if (stats._statsHechizos != null) {
			for (String s : stats._statsHechizos) {
				addStatHechizo(s);
			}
		}
		if (stats._statsRepetidos != null) {
			for (String s : stats._statsRepetidos) {
				addStatRepetido(s);
			}
		}
		if (stats._statsTextos != null) {
			for (Entry<Integer, String> entry : stats._statsTextos.entrySet()) {
				addStatTexto(entry.getKey(), entry.getValue(), false);
			}
		}
	}
	
	public void acumularStats(final Map<Integer, Integer> stats) {
		if (stats == null || stats.isEmpty()) {
			return;
		}
		for (final Entry<Integer, Integer> entry : stats.entrySet()) {
			addStatID(entry.getKey(), entry.getValue());
		}
	}
	
	public void addStatID(int statID, int valor) {
		if (_statsIDs.get(statID) != null) {
			valor += _statsIDs.get(statID);
		}
		fijarStatID(statID, valor);
	}
	
	public void fijarStatID(int statID, int valor) {
		int statOpuesto = Constantes.getStatOpuesto(statID);
		if (statOpuesto != statID && valor < 0) {
			fijarStatID(statOpuesto, -valor);
			// le manda al statOpuesto
			return;
		}
		int restar = 0;
		_statsIDs.remove(statID);
		if (statOpuesto != statID && _statsIDs.get(statOpuesto) != null) {
			restar = _statsIDs.get(statOpuesto);
			valor -= restar;
			_statsIDs.remove(statOpuesto);
			if (valor < 0) {
				statID = statOpuesto;
				valor = -valor;
			}
		}
		if (valor > 0) {
			_statsIDs.put(statID, valor);
		}
	}
	
	public boolean tieneStatID(final int statID) {
		return _statsIDs.get(statID) != null;
	}
	
	public void addStatHechizo(String str) {
		if (_statsHechizos == null) {
			_statsHechizos = new ArrayList<>();
		}
		_statsHechizos.add(str);
	}
	
	public void addStatRepetido(String str) {
		if (_statsRepetidos == null) {
			_statsRepetidos = new ArrayList<>();
		}
		_statsRepetidos.add(str);
	}
	
	public void addStatTexto(int statID, String str, boolean completo) {
		if (_statsTextos == null) {
			_statsTextos = new TreeMap<>();
		}
		if (str.isEmpty()) {
			_statsTextos.remove(statID);
		} else {
			_statsTextos.put(statID, completo ? str.split("#", 2)[1] : str);
		}
	}
	
	public boolean tieneStatTexto(final int statID) {
		if (_statsTextos == null) {
			return false;
		}
		return _statsTextos.get(statID) != null;
	}
	
	public String getStatTexto(final int stat) {
		if (_statsTextos == null || _statsTextos.get(stat) == null) {
			return "";
		}
		return _statsTextos.get(stat);
	}
	
	public String getParamStatTexto(final int stat, final int parametro) {
		try {
			String s = getStatTexto(stat);
			if (!s.isEmpty()) {
				if (s.split("#").length > parametro - 1) {
					return s.split("#")[parametro - 1];
				}
			}
		} catch (final Exception e) {}
		return "";
	}
	
	public String getStringStats(Objeto objeto) {
		StringBuilder stats = new StringBuilder();
		if (_statsHechizos != null) {
			for (final String hechizo : _statsHechizos) {
				if (stats.length() > 0) {
					stats.append(",");
				}
				stats.append(hechizo);
			}
		}
		if (_statsRepetidos != null) {
			for (String str : _statsRepetidos) {
				if (stats.length() > 0) {
					stats.append(",");
				}
				stats.append(str);
			}
		}
		if (_statsTextos != null) {
			for (final Entry<Integer, String> entry : _statsTextos.entrySet()) {
				if (stats.length() > 0) {
					stats.append(",");
				}
				stats.append(Integer.toHexString(entry.getKey()) + "#" + entry.getValue());
			}
		}
		for (final Entry<Integer, Integer> entry : _statsIDs.entrySet()) {
			if (stats.length() > 0) {
				stats.append(",");
			}
			boolean esExo = objeto.esStatExo(entry.getKey());
			stats.append(Integer.toHexString(entry.getKey()) + "#" + Integer.toHexString(entry.getValue()) + "#0#" + (esExo
			? "18B5B"
			: "0") + "#0d0+" + entry.getValue());
		}
		return stats.toString();
	}
	
	public boolean sonStatsIguales(Stats stats) {
		if (_statsHechizos == null && stats._statsHechizos == null) {
			// nada
		} else if ((_statsHechizos == null && stats._statsHechizos != null) || (_statsHechizos != null
		&& stats._statsHechizos == null) || (_statsHechizos.isEmpty() && !stats._statsHechizos.isEmpty())
		|| (!_statsHechizos.isEmpty() && stats._statsHechizos.isEmpty())) {
			return false;
		} else if (!_statsHechizos.isEmpty() && !stats._statsHechizos.isEmpty()) {
			for (final String entry : _statsHechizos) {
				if (!stats._statsHechizos.contains(entry)) {
					return false;
				}
			}
			for (final String entry : stats._statsHechizos) {
				if (!_statsHechizos.contains(entry)) {
					return false;
				}
			}
		}
		if (_statsTextos == null && stats._statsTextos == null) {
			// nada
		} else if ((_statsTextos == null && stats._statsTextos != null) || (_statsTextos != null
		&& stats._statsTextos == null) || (_statsTextos.isEmpty() && !stats._statsTextos.isEmpty()) || (!_statsTextos
		.isEmpty() && stats._statsTextos.isEmpty())) {
			return false;
		} else if (!_statsTextos.isEmpty() && !stats._statsTextos.isEmpty()) {
			for (final Entry<Integer, String> entry : _statsTextos.entrySet()) {
				if (stats._statsTextos.get(entry.getKey()) == null || !stats._statsTextos.get(entry.getKey()).equalsIgnoreCase(
				entry.getValue())) {
					return false;
				}
			}
			for (final Entry<Integer, String> entry : stats._statsTextos.entrySet()) {
				if (_statsTextos.get(entry.getKey()) == null || !_statsTextos.get(entry.getKey()).equalsIgnoreCase(entry
				.getValue())) {
					return false;
				}
			}
		}
		if (_statsRepetidos == null && stats._statsRepetidos == null) {
			// nada
		} else if ((_statsRepetidos == null && stats._statsRepetidos != null) || (_statsRepetidos != null
		&& stats._statsRepetidos == null) || (_statsRepetidos.isEmpty() && !stats._statsRepetidos.isEmpty())
		|| (!_statsRepetidos.isEmpty() && stats._statsRepetidos.isEmpty())) {
			return false;
		} else if (!_statsRepetidos.isEmpty() && !stats._statsRepetidos.isEmpty()) {
			ArrayList<String> repetidos = new ArrayList<>();
			repetidos.addAll(stats._statsRepetidos);
			for (final String entry : _statsRepetidos) {
				if (!repetidos.contains(entry)) {
					return false;
				} else {
					repetidos.remove(entry);
				}
			}
			repetidos.clear();
			repetidos.addAll(_statsRepetidos);
			for (final String entry : stats._statsRepetidos) {
				if (!repetidos.contains(entry)) {
					return false;
				} else {
					repetidos.remove(entry);
				}
			}
		}
		if ((_statsIDs == null && stats._statsIDs != null) || (_statsIDs != null && stats._statsIDs == null) || (_statsIDs
		.isEmpty() && !stats._statsIDs.isEmpty()) || (stats._statsIDs.isEmpty() && !_statsIDs.isEmpty())) {
			return false;
		} else if (!_statsIDs.isEmpty() && !stats._statsIDs.isEmpty()) {
			for (final Entry<Integer, Integer> entry : _statsIDs.entrySet()) {
				if (stats._statsIDs.get(entry.getKey()) == null || !stats._statsIDs.get(entry.getKey()).equals(entry
				.getValue())) {
					return false;
				}
			}
			for (final Entry<Integer, Integer> entry : stats._statsIDs.entrySet()) {
				if (_statsIDs.get(entry.getKey()) == null || !_statsIDs.get(entry.getKey()).equals(entry.getValue())) {
					return false;
				}
			}
		}
		return true;
	}
	
	public String convertirStatsAString() {
		final StringBuilder str = new StringBuilder();
		for (final Entry<Integer, Integer> entry : _statsIDs.entrySet()) {
			if (str.length() > 0) {
				str.append(",");
			}
			str.append(Integer.toHexString(entry.getKey()) + "#" + Integer.toHexString(entry.getValue()) + "#0#0");
		}
		return str.toString();
	}
	
	public int getStatParaMostrar(final int statID) {
		int valor = 0;
		if (_statsIDs.get(statID) != null) {
			valor = _statsIDs.get(statID);
		}
		switch (statID) {
			// case Informacion.STAT_REENVIA_DAÑOS :// reenvio daños
			// if (_statsIDs.get(Informacion.STAT_DAÑOS_DEVUELTOS) != null) {
			// val += _statsIDs.get(Informacion.STAT_DAÑOS_DEVUELTOS);
			// }
			// break;
			case Constantes.STAT_MAS_DAÑOS_DE_AGUA :
				if (_statsIDs.get(Constantes.STAT_MENOS_DAÑOS_DE_AGUA) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_DAÑOS_DE_AGUA);
				}
				break;
			case Constantes.STAT_MAS_DAÑOS_DE_AIRE :
				if (_statsIDs.get(Constantes.STAT_MENOS_DAÑOS_DE_AIRE) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_DAÑOS_DE_AIRE);
				}
				break;
			case Constantes.STAT_MAS_DAÑOS_DE_FUEGO :
				if (_statsIDs.get(Constantes.STAT_MENOS_DAÑOS_DE_FUEGO) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_DAÑOS_DE_FUEGO);
				}
				break;
			case Constantes.STAT_MAS_DAÑOS_DE_TIERRA :
				if (_statsIDs.get(Constantes.STAT_MENOS_DAÑOS_DE_TIERRA) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_DAÑOS_DE_TIERRA);
				}
				break;
			case Constantes.STAT_MAS_DAÑOS_DE_NEUTRAL :
				if (_statsIDs.get(Constantes.STAT_MENOS_DAÑOS_DE_NEUTRAL) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_DAÑOS_DE_NEUTRAL);
				}
				break;
			case Constantes.STAT_MAS_DAÑOS_EMPUJE :
				if (_statsIDs.get(Constantes.STAT_MENOS_DAÑOS_EMPUJE) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_DAÑOS_EMPUJE);
				}
				break;
			case Constantes.STAT_MAS_REDUCCION_CRITICOS :
				if (_statsIDs.get(Constantes.STAT_MENOS_REDUCCION_CRITICOS) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_REDUCCION_CRITICOS);
				}
				break;
			case Constantes.STAT_MAS_DAÑOS_CRITICOS :
				if (_statsIDs.get(Constantes.STAT_MENOS_DAÑOS_CRITICOS) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_DAÑOS_CRITICOS);
				}
				break;
			case Constantes.STAT_MAS_GOLPES_CRITICOS :
				if (_statsIDs.get(Constantes.STAT_MENOS_GOLPES_CRITICOS) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_GOLPES_CRITICOS);
				}
				break;
			case Constantes.STAT_MAS_REDUCCION_EMPUJE :
				if (_statsIDs.get(Constantes.STAT_MENOS_REDUCCION_EMPUJE) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_REDUCCION_EMPUJE);
				}
				break;
			case Constantes.STAT_MAS_RETIRO_PA :
				if (_statsIDs.get(Constantes.STAT_MENOS_RETIRO_PA) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RETIRO_PA);
				}
				break;
			case Constantes.STAT_MAS_RETIRO_PM :
				if (_statsIDs.get(Constantes.STAT_MENOS_RETIRO_PM) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RETIRO_PM);
				}
				break;
			case Constantes.STAT_MAS_HUIDA :// huida
				if (_statsIDs.get(Constantes.STAT_MENOS_HUIDA) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_HUIDA);
				}
				break;
			case Constantes.STAT_MAS_PLACAJE :// placaje
				if (_statsIDs.get(Constantes.STAT_MENOS_PLACAJE) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_PLACAJE);
				}
				break;
			case Constantes.STAT_MAS_ESQUIVA_PERD_PA :// prob perdidas PA
				if (_statsIDs.get(Constantes.STAT_MENOS_ESQUIVA_PERD_PA) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_ESQUIVA_PERD_PA);
				}
				break;
			case Constantes.STAT_MAS_ESQUIVA_PERD_PM :// prob perdidas PM
				if (_statsIDs.get(Constantes.STAT_MENOS_ESQUIVA_PERD_PM) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_ESQUIVA_PERD_PM);
				}
				break;
			case Constantes.STAT_MAS_INICIATIVA :// iniciativa
				if (_statsIDs.get(Constantes.STAT_MENOS_INICIATIVA) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_INICIATIVA);
				}
				break;
			case Constantes.STAT_MAS_AGILIDAD :// agilidad
				if (_statsIDs.get(154) != null) {
					valor -= _statsIDs.get(154);
				}
				if (_statsIDs.get(609) != null) {
					valor += _statsIDs.get(609);
				}
				break;
			case Constantes.STAT_MAS_FUERZA :// fuerza
				if (_statsIDs.get(157) != null) {
					valor -= _statsIDs.get(157);
				}
				if (_statsIDs.get(607) != null) {
					valor += _statsIDs.get(607);
				}
				break;
			case Constantes.STAT_MAS_SUERTE :// suerte
				if (_statsIDs.get(152) != null) {
					valor -= _statsIDs.get(152);
				}
				if (_statsIDs.get(608) != null) {
					valor += _statsIDs.get(608);
				}
				break;
			case Constantes.STAT_MAS_SABIDURIA :// sabiduria
				if (_statsIDs.get(606) != null) {
					valor += _statsIDs.get(606);
				}
				if (_statsIDs.get(156) != null) {
					valor -= _statsIDs.get(156);
				}
				break;
			case Constantes.STAT_MAS_VITALIDAD :// vitalidad
				if (_statsIDs.get(110) != null) {
					valor += _statsIDs.get(110);
				}
				if (_statsIDs.get(153) != null) {
					valor -= _statsIDs.get(153);
				}
				if (_statsIDs.get(424) != null) {
					valor -= _statsIDs.get(424);
				}
				if (_statsIDs.get(610) != null) {
					valor += _statsIDs.get(610);
				}
				break;
			case Constantes.STAT_MAS_INTELIGENCIA :// inteligencia
				if (_statsIDs.get(155) != null) {
					valor -= _statsIDs.get(155);
				}
				if (_statsIDs.get(611) != null) {
					valor += _statsIDs.get(611);
				}
				break;
			case Constantes.STAT_MAS_PA :// PA
				if (_statsIDs.get(120) != null) {
					valor += _statsIDs.get(120);
				}
				if (_statsIDs.get(101) != null) {
					valor -= _statsIDs.get(101);
				}
				if (_statsIDs.get(168) != null) {
					valor -= _statsIDs.get(168);
				}
				break;
			case Constantes.STAT_MAS_PM :// PM
				if (_statsIDs.get(78) != null) {
					valor += _statsIDs.get(78);
				}
				if (_statsIDs.get(127) != null) {
					valor -= _statsIDs.get(127);
				}
				if (_statsIDs.get(169) != null) {
					valor -= _statsIDs.get(169);
				}
				break;
			case Constantes.STAT_MAS_ALCANCE :// alcance
				if (_statsIDs.get(116) != null) {
					valor -= _statsIDs.get(116);
				}
				break;
			case Constantes.STAT_MAS_DAÑOS :// + daños
				if (_statsIDs.get(121) != null) {
					valor += _statsIDs.get(121);
				}
				if (_statsIDs.get(145) != null) {
					valor -= _statsIDs.get(145);
				}
				if (_statsIDs.get(144) != null) {
					valor -= _statsIDs.get(144);
				}
				break;
			case Constantes.STAT_MAS_PORC_DAÑOS :// % daños
				if (_statsIDs.get(186) != null) {
					valor -= _statsIDs.get(186);
				}
				break;
			case Constantes.STAT_MAS_PODS :// pods
				if (_statsIDs.get(159) != null) {
					valor -= _statsIDs.get(159);
				}
				break;
			case Constantes.STAT_MAS_PROSPECCION :// prospeccion
				if (_statsIDs.get(177) != null) {
					valor -= _statsIDs.get(177);
				}
				break;
			case Constantes.STAT_MAS_CURAS :// curas
				if (_statsIDs.get(179) != null) {
					valor -= _statsIDs.get(179);
				}
				break;
			case Constantes.STAT_MAS_RES_PORC_TIERRA :// % resistencia
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_PORC_TIERRA) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_PORC_TIERRA);
				}
				break;
			case Constantes.STAT_MAS_RES_PORC_AGUA :// % resistencia
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_PORC_AGUA) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_PORC_AGUA);
				}
				break;
			case Constantes.STAT_MAS_RES_PORC_AIRE :// % resistencia
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_PORC_AIRE) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_PORC_AIRE);
				}
				break;
			case Constantes.STAT_MAS_RES_PORC_FUEGO :// % resistencia
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_PORC_FUEGO) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_PORC_FUEGO);
				}
				break;
			case Constantes.STAT_MAS_RES_PORC_NEUTRAL :// % resistencia
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_PORC_NEUTRAL) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_PORC_NEUTRAL);
				}
				break;
			case Constantes.STAT_MAS_RES_FIJA_TIERRA :
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_FIJA_TIERRA) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_FIJA_TIERRA);
				}
				break;
			case Constantes.STAT_MAS_RES_FIJA_AGUA :
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_FIJA_AGUA) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_FIJA_AGUA);
				}
				break;
			case Constantes.STAT_MAS_RES_FIJA_AIRE :
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_FIJA_AIRE) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_FIJA_AIRE);
				}
				break;
			case Constantes.STAT_MAS_RES_FIJA_FUEGO :
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_FIJA_FUEGO) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_FIJA_FUEGO);
				}
				break;
			case Constantes.STAT_MAS_RES_FIJA_NEUTRAL :
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_FIJA_NEUTRAL) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_FIJA_NEUTRAL);
				}
				break;
			case Constantes.STAT_MAS_RES_PORC_PVP_TIERRA :
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_PORC_PVP_TIERRA) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_PORC_PVP_TIERRA);
				}
				break;
			case Constantes.STAT_MAS_RES_PORC_PVP_AGUA :
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_PORC_PVP_AGUA) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_PORC_PVP_AGUA);
				}
				break;
			case Constantes.STAT_MAS_RES_PORC_PVP_AIRE :
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_PORC_PVP_AIRE) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_PORC_PVP_AIRE);
				}
				break;
			case Constantes.STAT_MAS_RES_PORC_PVP_FUEGO :
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_PORC_PVP_FUEGO) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_PORC_PVP_FUEGO);
				}
				break;
			case Constantes.STAT_MAS_RES_PORC_PVP_NEUTRAL :
				if (_statsIDs.get(Constantes.STAT_MENOS_RES_PORC_PVP_NEUTRAL) != null) {
					valor -= _statsIDs.get(Constantes.STAT_MENOS_RES_PORC_PVP_NEUTRAL);
				}
				break;
		}
		return valor;
	}
}