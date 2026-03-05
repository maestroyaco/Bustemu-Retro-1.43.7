package variables.personaje;

public class MisionPVP {
	// private Personaje _victimaPVP;
	private final String _nombreVictima;
	private final long _kamas, _exp, _tiempo;
	private final boolean _cazaCabezas;
	private final int _craneo;
	
	public MisionPVP(final long tiempo, final String victima, final long kamas, final long exp, int craneo) {
		_tiempo = tiempo;
		_nombreVictima = victima;
		_kamas = kamas;
		_exp = exp;
		_craneo = _exp <= 0 ? 0 : craneo;
		_cazaCabezas = _craneo != 0;
	}
	
	public int getCraneo() {
		return _craneo;
	}

	public String getNombreVictima() {
		return _nombreVictima;
	}
	
	public long getKamasRecompensa() {
		return _kamas;
	}
	
	public long getExpMision() {
		return _exp;
	}
	
	public boolean esCazaCabezas() {
		return _cazaCabezas;
	}
	
	// public Personaje getPjMision() {
	// return _victimaPVP;
	// }
	public long getTiempoInicio() {
		return _tiempo;
	}
}