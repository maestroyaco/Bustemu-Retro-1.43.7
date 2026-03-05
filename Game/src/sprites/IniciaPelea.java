package sprites;

import variables.mapa.Celda;
import variables.mapa.Mapa;
import variables.pelea.Pelea;

public interface IniciaPelea {
	public Celda getCelda();
	
	public Mapa getMapa();
	
	public int getID();
	
	public void setPelea(Pelea pelea);
}
