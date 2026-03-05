package variables.encarnacion;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import variables.hechizo.StatHechizo;
import variables.objeto.Objeto;
import variables.personaje.Personaje;
import variables.stats.Stats;
import variables.stats.TotalStats;
import estaticos.GestorSalida;
import estaticos.MainServidor;
import estaticos.Mundo;

public class Encarnacion {
	private EncarnacionModelo _encarnacionModelo;
	private int _experiencia;
	private int _nivel = 0;
	private boolean _noLevel = false;
	private TotalStats _totalStats;
	private Objeto _objeto;
	private long _ultEquipada;
	private Map<Integer, StatHechizo> _hechizos = new HashMap<>();
	private final Map<Integer, Character> _posicionHechizos = new HashMap<>();
	
	public Encarnacion(Objeto objeto, int exp, EncarnacionModelo encarnacion) {
		_objeto = objeto;
		_encarnacionModelo = encarnacion;
		if (!_encarnacionModelo.getStatsBase().isEmpty()) {
			Stats statsBase = new Stats(_encarnacionModelo.getStatsBase());
			_totalStats = new TotalStats(statsBase, new Stats(), new Stats(), new Stats(), 1);
			_noLevel = true;
		}
		_posicionHechizos.putAll(encarnacion.getPosicionsHechizos());
		addExperiencia(exp, null);
	}
	
	public void addExperiencia(long exp, Personaje perso) {
		if (_noLevel) {
			return;
		}
		if (_experiencia >= Mundo.getExpEncarnacion(MainServidor.NIVEL_MAX_ENCARNACION)) {
			return;
		}
		_experiencia += exp;
		int nivel = _nivel;
		while (_experiencia >= Mundo.getExpEncarnacion(_nivel + 1) && _nivel < MainServidor.NIVEL_MAX_ENCARNACION) {
			subirNivel();
		}
		if (nivel != _nivel && perso != null) {
			if (perso.enLinea()) {
				perso.refrescarStuff(true, false, false);
				GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(perso, _objeto);
			}
		}
	}
	
	private void subirNivel() {
		_nivel++;
		refrescarStatsItem();
		refrescarHechizos();
	}
	
	public void refrescarHechizos() {
		int nivel = _nivel / 10;
		for (int hechizoID : _posicionHechizos.keySet()) {
			_hechizos.put(hechizoID, Mundo.getHechizo(hechizoID).getStatsPorNivel(nivel + 1));
		}
	}
	
	public void refrescarStatsItem() {
		if (!_encarnacionModelo.getStatsPorNivel().isEmpty()) {
			for (Entry<Integer, Float> entry : _encarnacionModelo.getStatsPorNivel().entrySet()) {
				int valor = (int) (entry.getValue() * _nivel);
				if (valor > 0) {
					_objeto.getStats().fijarStatID(entry.getKey(), valor);
				}
			}
		}
	}
	
	public String stringListaHechizos() {
		int nivel = _nivel / 10;
		final StringBuilder str = new StringBuilder();
		for (final Entry<Integer, Character> SH : _posicionHechizos.entrySet()) {
			str.append(SH.getKey() + "~" + (nivel + 1) + "~" + SH.getValue() + ";");
		}
		return str.toString();
	}
	
	public boolean tieneHechizoID(final int hechizoID) {
		return _posicionHechizos.get(hechizoID) != null;
	}
	
	public Map<Integer, StatHechizo> getStatHechizos() {
		return _hechizos;
	}
	
	public StatHechizo getStatsHechizo(final int hechizoID) {
		return _hechizos.get(hechizoID);
	}
	
	public void setEquipado() {
		_ultEquipada = System.currentTimeMillis();
	}
	
	public long getUltEquipada() {
		return _ultEquipada;
	}
	
	public int getID() {
		return _objeto.getID();
	}
	
	public TotalStats getTotalStats() {
		return _totalStats;
	}
	
	public int getNivel() {
		return _nivel;
	}
	
	public int getExp() {
		return _experiencia;
	}
	
	public int getGfxID() {
		return _encarnacionModelo.getGfxID();
	}
	
	public void setPosHechizo(final int hechizoID, final char pos, Personaje perso) {
		if (pos == 'a') {
			GestorSalida.ENVIAR_BN_NADA(perso, "SET POS HECHIZO - POS INVALIDA");
			return;
		}
		if (!tieneHechizoID(hechizoID)) {
			GestorSalida.ENVIAR_BN_NADA(perso, "SET POS HECHIZO - NO TIENE HECHIZO");
			return;
		}
		int exID = -1;
		if (pos != '_') {
			for (final Entry<Integer, Character> SH : _posicionHechizos.entrySet()) {
				if (SH.getValue() == pos) {
					exID = SH.getKey();
					break;
				}
			}
		}
		if (exID != -1) {
			_posicionHechizos.put(exID, '_');
		}
		_posicionHechizos.put(hechizoID, pos);
		GestorSalida.ENVIAR_BN_NADA(perso);
	}
}
