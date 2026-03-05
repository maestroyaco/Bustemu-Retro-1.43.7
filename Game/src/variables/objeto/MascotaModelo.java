package variables.objeto;

import java.util.ArrayList;
import java.util.regex.Pattern;
import estaticos.Mundo;
import estaticos.Mundo.Duo;

public class MascotaModelo {
	public static class Comida {
		private final int _idComida, _cant, _idStat;
		
		public Comida(final int idModelo, final int cant, final int idStat) {
			_idComida = idModelo;
			_cant = cant;
			_idStat = idStat;
		}
		
		public int getIDComida() {
			return _idComida;
		}
		
		public int getCantidad() {
			return _cant;
		}
		
		public int getIDStat() {
			return _idStat;
		}
	}
	private final int _id, _maxStats, _fantasma;
	private final ArrayList<Duo<Integer, Integer>> _statsPorEfecto = new ArrayList<Duo<Integer, Integer>>();
	private final ArrayList<Comida> _comidas = new ArrayList<Comida>();
	private final boolean _esDevorador;
	private String _strComidas = "";
	
	public MascotaModelo(final int id, final int maxStas, final String statsPorEfecto, final String comidas,
	final int devorador, final int fantasma) {
		_id = id;
		_fantasma = fantasma;
		_maxStats = maxStas;
		if (!comidas.isEmpty()) {
			for (final String comida : comidas.split(Pattern.quote("|"))) {
				try {
					final String[] str = comida.split(";");
					_comidas.add(new Comida(Integer.parseInt(str[0]), Integer.parseInt(str[1]), Integer.parseInt(str[2])));
				} catch (final Exception e) {}
			}
		}
		_strComidas = "comidas: " + comidas + " statsPorEfecto: " + statsPorEfecto + " maxStats: " + maxStas;
		final String[] stats = statsPorEfecto.split(Pattern.quote("|"));
		for (final String s : stats) {
			try {
				_statsPorEfecto.add(new Duo<Integer, Integer>(Integer.parseInt(s.split(";")[0]), Integer
				.parseInt(s.split(";")[1])));
			} catch (final Exception e) {}
		}
		_esDevorador = devorador == 1;
	}
	
	public int getID() {
		return _id;
	}
	
	public Comida getComida(final int idModComida) {
		for (final Comida comi : _comidas) {
			if (comi.getIDComida() < 0) {
				if (Math.abs(comi.getIDComida()) == Mundo.getObjetoModelo(idModComida).getTipo()) {
					return comi;
				}
			} else {
				if (comi.getIDComida() == idModComida) {
					return comi;
				}
			}
		}
		return null;
	}
	
	public boolean esDevoradorAlmas() {
		return _esDevorador;
	}
	
	public int getMaxStats() {
		return _maxStats;
	}
	
	public String getStrComidas() {
		return _strComidas;
	}
	
	public int getFantasma() {
		return _fantasma;
	}
	
	public int getStatsPorEfecto(final int stat) {
		for (final Duo<Integer, Integer> duo : _statsPorEfecto) {
			if (duo._primero == stat) {
				return duo._segundo;
			}
		}
		return 0;
	}
}
