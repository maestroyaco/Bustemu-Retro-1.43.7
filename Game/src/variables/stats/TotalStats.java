package variables.stats;

import estaticos.Constantes;
import estaticos.MainServidor;

public class TotalStats {
	private final Stats _statsBase;
	private final Stats _statsObjetos;
	private final Stats _statsBuff;
	private final Stats _statsBendMald;
	private final int _tipo;
	
	public TotalStats(Stats statsBase, Stats statsObjEquipados, Stats statsBuff, Stats statsBendMald, int tipo) {
		_statsBase = statsBase;
		_statsObjetos = statsObjEquipados;
		_statsBuff = statsBuff;
		_statsBendMald = statsBendMald;
		_tipo = tipo;
	}
	
	public Stats getStatsBase() {
		return _statsBase;
	}
	
	public Stats getStatsObjetos() {
		return _statsObjetos;
	}
	
	public Stats getStatsBuff() {
		return _statsBuff;
	}
	
	public Stats getStatsBendMald() {
		return _statsBendMald;
	}
	
	public void clear() {
		if (_statsBase != null) {
			_statsBase.clear();
		}
		if (_statsBendMald != null) {
			_statsBendMald.clear();
		}
		if (_statsObjetos != null) {
			_statsObjetos.clear();
		}
		if (_statsBuff != null) {
			_statsBuff.clear();
		}
	}
	
//	private boolean tieneStatID(int statID) {
//		boolean b = false;
//		if (_statsBase != null) {
//			b |= _statsBase.tieneStatID(statID);
//		}
//		if (_statsBendMald != null) {
//			b |= _statsBendMald.tieneStatID(statID);
//		}
//		if (_statsObjetos != null) {
//			b |= _statsObjetos.tieneStatID(statID);
//		}
//		if (_statsBuff != null) {
//			b |= _statsBuff.tieneStatID(statID);
//		}
//		return b;
//	}
	
	// aqui se aplican los limites
	public int getTotalStatConComplemento(int statID) {
		int[] valores2;
		int divisor = 1;
		switch (statID) {
			case Constantes.STAT_MAS_RES_FIJA_PVP_TIERRA :
				valores2 = getArrayStats(Constantes.STAT_MAS_RES_FIJA_TIERRA);
				break;
			case Constantes.STAT_MAS_RES_FIJA_PVP_AGUA :
				valores2 = getArrayStats(Constantes.STAT_MAS_RES_FIJA_AGUA);
				break;
			case Constantes.STAT_MAS_RES_FIJA_PVP_AIRE :
				valores2 = getArrayStats(Constantes.STAT_MAS_RES_FIJA_AIRE);
				break;
			case Constantes.STAT_MAS_RES_FIJA_PVP_FUEGO :
				valores2 = getArrayStats(Constantes.STAT_MAS_RES_FIJA_FUEGO);
				break;
			case Constantes.STAT_MAS_RES_FIJA_PVP_NEUTRAL :
				valores2 = getArrayStats(Constantes.STAT_MAS_RES_FIJA_NEUTRAL);
				break;
			case Constantes.STAT_MAS_RES_PORC_PVP_TIERRA :
				valores2 = getArrayStats(Constantes.STAT_MAS_RES_PORC_TIERRA);
				break;
			case Constantes.STAT_MAS_RES_PORC_PVP_AGUA :
				valores2 = getArrayStats(Constantes.STAT_MAS_RES_PORC_AGUA);
				break;
			case Constantes.STAT_MAS_RES_PORC_PVP_AIRE :
				valores2 = getArrayStats(Constantes.STAT_MAS_RES_PORC_AIRE);
				break;
			case Constantes.STAT_MAS_RES_PORC_PVP_FUEGO :
				valores2 = getArrayStats(Constantes.STAT_MAS_RES_PORC_FUEGO);
				break;
			case Constantes.STAT_MAS_RES_PORC_PVP_NEUTRAL :
				valores2 = getArrayStats(Constantes.STAT_MAS_RES_PORC_NEUTRAL);
				break;
			case Constantes.STAT_MAS_ESQUIVA_PERD_PA :// prob perdidas PA
				valores2 = getArrayStats(Constantes.STAT_MAS_SABIDURIA);
				divisor = 4;
				break;
			case Constantes.STAT_MAS_ESQUIVA_PERD_PM :// prob perdidas PM
				valores2 = getArrayStats(Constantes.STAT_MAS_SABIDURIA);
				divisor = 4;
				break;
			case Constantes.STAT_MAS_PROSPECCION :// prospeccion
				valores2 = getArrayStats(Constantes.STAT_MAS_SUERTE);
				divisor = 10;
				break;
			case Constantes.STAT_MAS_HUIDA :// huida
				valores2 = getArrayStats(Constantes.STAT_MAS_AGILIDAD);
				divisor = 10;
				break;
			case Constantes.STAT_MAS_PLACAJE :// placaje
				valores2 = getArrayStats(Constantes.STAT_MAS_AGILIDAD);
				divisor = 10;
				break;
			default :
				return getTotalStatParaMostrar(statID);
		}
		int valor = 0;
		if (_statsBase != null) {
			valor += _statsBase.getStatParaMostrar(statID);
			valor += valores2[0] / divisor;
		}
		if (_statsBendMald != null) {
			valor += _statsBendMald.getStatParaMostrar(statID);
			valor += valores2[1] / divisor;
		}
		if (_statsObjetos != null) {
			valor += _statsObjetos.getStatParaMostrar(statID);
			valor += valores2[2] / divisor;
		}
		int limitSin = 0;
		if (_tipo == 1) {
			if (MainServidor.LIMITE_STATS_SIN_BUFF.get(statID) != null) {
				limitSin = MainServidor.LIMITE_STATS_SIN_BUFF.get(statID);
				valor = Math.min(valor, limitSin);
			}
		}
		if (_statsBuff != null) {
			int v = _statsBuff.getStatParaMostrar(statID);
			valor += v;
			valor += valores2[3] / divisor;
		}
		if (_tipo == 1) {
			if (MainServidor.LIMITE_STATS_CON_BUFF.get(statID) != null) {
				int limitCon = Math.max(limitSin, MainServidor.LIMITE_STATS_CON_BUFF.get(statID));
				valor = Math.min(valor, limitCon);
			}
		}
		return valor;
	}
	
	private int[] getArrayStats(int statID) {
		int[] valores = new int[5];
		int valor = 0;
		if (_statsBase != null) {
			valores[0] = _statsBase.getStatParaMostrar(statID);
			valor += valores[0];
		}
		if (_statsBendMald != null) {
			valores[1] += _statsBendMald.getStatParaMostrar(statID);
			valor += valores[1];
		}
		if (_statsObjetos != null) {
			valores[2] += _statsObjetos.getStatParaMostrar(statID);
			valor += valores[2];
		}
		int limitSin = 0;
		if (_tipo == 1) {
			if (MainServidor.LIMITE_STATS_SIN_BUFF.get(statID) != null) {
				limitSin = MainServidor.LIMITE_STATS_SIN_BUFF.get(statID);
				valor = Math.min(valor, limitSin);
			}
		}
		if (_statsBuff != null) {
			valores[3] += _statsBuff.getStatParaMostrar(statID);
			valor += valores[3];
		}
		if (_tipo == 1) {
			if (MainServidor.LIMITE_STATS_CON_BUFF.get(statID) != null) {
				int limitCon = Math.max(limitSin, MainServidor.LIMITE_STATS_CON_BUFF.get(statID));
				valor = Math.min(valor, limitCon);
			}
		}
		valores[4] = valor;
		return valores;
	}
	
	public int getTotalStatParaMostrar(int statID) {
		int valor = 0;
		if (_statsBase != null) {
			valor += _statsBase.getStatParaMostrar(statID);
		}
		if (_statsBendMald != null) {
			valor += _statsBendMald.getStatParaMostrar(statID);
		}
		if (_statsObjetos != null) {
			valor += _statsObjetos.getStatParaMostrar(statID);
		}
		int limitSin = 0;
		if (_tipo == 1) {
			if (MainServidor.LIMITE_STATS_SIN_BUFF.get(statID) != null) {
				limitSin = MainServidor.LIMITE_STATS_SIN_BUFF.get(statID);
				valor = Math.min(valor, limitSin);
			}
		}
		if (_statsBuff != null) {
			valor += _statsBuff.getStatParaMostrar(statID);
		}
		if (_tipo == 1) {
			if (MainServidor.LIMITE_STATS_CON_BUFF.get(statID) != null) {
				int limitCon = Math.max(limitSin, MainServidor.LIMITE_STATS_CON_BUFF.get(statID));
				valor = Math.min(valor, limitCon);
			}
		}
		return valor;
	}
	
	public void clearBuffStats() {
		if (_statsBuff != null) {
			_statsBuff.clear();
		}
	}
}
