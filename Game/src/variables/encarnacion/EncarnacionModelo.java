package variables.encarnacion;

import java.util.HashMap;
import java.util.Map;
import estaticos.Mundo;

public class EncarnacionModelo {
	private final int _gfxID;
	private final Map<Integer, Character> _posicionHechizos = new HashMap<>();
	private final Map<Integer, Integer> _statsBase = new HashMap<>();
	private final Map<Integer, Float> _statsPorNivel = new HashMap<>();
	
	public EncarnacionModelo(final int gfx, String statsBase, String statsPorNivel, String strHechizos) {
		_gfxID = gfx;
		analizarPosHechizos(strHechizos);
		analizarStatsBase(statsBase);
		analizarStatsPorNivel(statsPorNivel);
	}
	
	private void analizarStatsBase(String statsBase) {
		for (final String s : statsBase.split("\\|")) {
			try {
				if (s.isEmpty()) {
					continue;
				}
				final int statID = Integer.parseInt(s.split(",")[0]);
				final int valor = Integer.parseInt(s.split(",")[1]);
				_statsBase.put(statID, valor);
			} catch (final Exception e1) {}
		}
	}
	
	private void analizarStatsPorNivel(String statsPorNivel) {
		for (final String s : statsPorNivel.split("\\|")) {
			try {
				if (s.isEmpty()) {
					continue;
				}
				final int statID = Integer.parseInt(s.split(",")[0]);
				final float valor = Float.parseFloat(s.split(",")[1]);
				_statsPorNivel.put(statID, valor);
			} catch (final Exception e1) {}
		}
	}
	
	private void analizarPosHechizos(final String str) {
		final String[] hechizos = str.split(";");
		for (final String s : hechizos) {
			try {
				if (s.isEmpty()) {
					continue;
				}
				final int id = Integer.parseInt(s.split(",")[0]);
				if (Mundo.getHechizo(id) == null) {
					continue;
				}
				final char pos = s.split(",")[1].charAt(0);
				_posicionHechizos.put(id, pos);
			} catch (final Exception e1) {}
		}
	}
	
	public Map<Integer, Character> getPosicionsHechizos() {
		return _posicionHechizos;
	}
	
	public int getGfxID() {
		return _gfxID;
	}
	
	public Map<Integer, Integer> getStatsBase() {
		return _statsBase;
	}
	
	public Map<Integer, Float> getStatsPorNivel() {
		return _statsPorNivel;
	}
}
