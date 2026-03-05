package sprites;

import variables.objeto.Objeto;
import variables.personaje.Personaje;

public interface Exchanger {
	public void addKamas(long kamas, Personaje perso);
	
	public long getKamas();
	
	public void addObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio);
	
	public void remObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio);
	
	public void cerrar(Personaje perso, String exito);
	
	public void botonOK(Personaje perso);
	
	public String getListaExchanger(Personaje perso);
}
