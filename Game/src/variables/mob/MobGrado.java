package variables.mob;

import java.util.Map;
import sprites.PreLuchador;
import variables.hechizo.StatHechizo;
import variables.mapa.Celda;
import variables.pelea.Pelea;
import variables.stats.Stats;
import variables.stats.TotalStats;

public class MobGrado implements PreLuchador {
	private GrupoMob _grupoMob;
	private final MobGradoModelo _mobGradoModelo;
	private final MobModelo _mobModelo;
	private final byte _grado;
	private short _nivel;
	private int _id;
	private int _PDV;
	private int _PDVMAX;
	private Celda _celdaPelea;
	private final Stats _stats;
	private final TotalStats _totalStats;
	
	// public void destruir() {
	// try {
	// this.finalize();
	// } catch (Throwable e) {}
	// }
	public MobGrado(final MobGradoModelo mobGrado, final MobModelo mobModelo, final byte grado, final short nivel,
	final int pdvMax, Stats stats) {
		_mobGradoModelo = mobGrado;
		_mobModelo = mobModelo;
		_stats = new Stats(stats);
		_totalStats = new TotalStats(_stats, null, new Stats(), null, 2);
		_grado = grado;
		_nivel = nivel;
		_PDVMAX = pdvMax;
		_PDV = _PDVMAX;
	}
	
	void setIDPersonal(int id) {
		_id = id;
	}
	
	public void setGrupoMob(GrupoMob gm) {
		_grupoMob = gm;
	}
	
	public long getBaseXp() {
		return _mobGradoModelo.getBaseXp();
	}
	
	public Map<Integer, StatHechizo> getHechizos() {
		return _mobGradoModelo.getHechizos();
	}
	
	public Stats getStats() {
		return _stats;
	}
	
	public int getIDModelo() {
		return _mobModelo.getID();
	}
	
	public MobGradoModelo getMobGradoModelo() {
		return _mobGradoModelo;
	}
	
	public MobModelo getMobModelo() {
		return _mobModelo;
	}
	
	public Celda getCeldaPelea() {
		return _celdaPelea;
	}
	
	public void setCeldaPelea(final Celda celda) {
		_celdaPelea = celda;
	}
	
	public int getGrado() {
		return _grado;
	}
	
	public void setPDV(final int pdv) {
		_PDV = pdv;
	}
	
	public int getPDV() {
		return _PDV;
	}
	
	public void setPDVMAX(final int pdv) {
		_PDVMAX = pdv;
	}
	
	public int getPDVMax() {
		return _PDVMAX;
	}
	
	public int getID() {
		return _id;
	}
	
	public int getNivel() {
		return _nivel;
	}
	
	public int getGfxID(boolean buff) {
		return _mobGradoModelo.getGfxID();
	}
	
	public TotalStats getTotalStatsPelea() {
		return _totalStats;
	}
	
	public byte getAlineacion() {
		return _mobModelo.getAlineacion();
	}
	
	public String stringGMLuchador() {
		StringBuilder str = new StringBuilder();
		str.append("-2;");
		str.append(_mobGradoModelo.getGfxID() + "^" + _mobModelo.getTalla() + ";");
		str.append(_grado + ";");
		str.append(_mobModelo.getColores().replace(",", ";") + ";");
		str.append("0,0,0,0;");
		return str.toString();
	}
	
	public int getDeshonor() {
		return 0;
	}
	
	public int getHonor() {
		return 0;
	}
	
	public int getGradoAlineacion() {
		return 1;
	}
	
	public void addHonor(int honor) {}
	
	public boolean addDeshonor(int honor) {
		return false;
	}
	
	public void addKamasGanada(long kamas) {
		if (_grupoMob != null)
			_grupoMob.addKamasHeroico(kamas);
	}
	
	public void addXPGanada(long exp) {}
	
	public void setPelea(Pelea pelea) {}
	
	public void actualizarAtacantesDefensores() {}
	
	public void murio() {}
	
	public void sobrevivio() {}
}