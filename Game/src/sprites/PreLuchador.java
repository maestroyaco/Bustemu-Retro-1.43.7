package sprites;

import java.util.Map;
import variables.hechizo.StatHechizo;
import variables.pelea.Pelea;
import variables.stats.TotalStats;

public interface PreLuchador {
	public byte getAlineacion();
	
	public int getID();
	
	public int getGfxID(boolean rolePlayBuff);
	
	public int getPDVMax();
	
	public int getPDV();
	
	public TotalStats getTotalStatsPelea();
	
	public int getNivel();
	
	public int getGradoAlineacion();
	
	// public int getIniciativa();
	public void setPelea(Pelea pelea);
	
	public void actualizarAtacantesDefensores();
	
	public String stringGMLuchador();
	
	public Map<Integer, StatHechizo> getHechizos();
	
	public int getDeshonor();
	
	public int getHonor();
	
	public void addHonor(int honor);
	
	public boolean addDeshonor(int honor);
	
	public void addKamasGanada(long kamas);
	
	public void addXPGanada(long exp);
	
	public void murio();
	
	public void sobrevivio();
}
