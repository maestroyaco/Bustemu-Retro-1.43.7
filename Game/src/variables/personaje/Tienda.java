package variables.personaje;

import java.util.ArrayList;
import estaticos.Constantes;
import estaticos.GestorSalida;
import estaticos.Mundo;
import sprites.Exchanger;
import variables.objeto.Objeto;

public class Tienda implements Exchanger {
	private final ArrayList<Objeto> _tienda = new ArrayList<Objeto>();
	
	
	public void addObjeto(Objeto objeto) {
		if (objeto.getID() == 0) {
			Mundo.addObjeto(objeto, false);
		}
		if (_tienda.contains(objeto)) {
			return;
		}
		_tienda.add(objeto);
	}
	
	public void borrarObjeto(Objeto obj) {
		_tienda.remove(obj);
	}
	
	public boolean contains(Objeto obj) {
		return _tienda.contains(obj);
	}
	
	public ArrayList<Objeto> getObjetos() {
		return _tienda;
	}
	
	@Override
	public void addKamas(long kamas, Personaje perso) {}
	
	@Override
	public long getKamas() {
		return 0;
	}
	
	public void clear() {
		_tienda.clear();
	}
	
	public boolean estaVacia() {
		return _tienda.isEmpty();
	}
	
	@Override
	public synchronized void addObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {
		if (!_tienda.contains(objeto)) {// si no lo tiene en la tienda
			if (cantidad == 0) {
				GestorSalida.ENVIAR_BN_NADA(perso);
				return;
			}
			if (!perso.tieneObjetoID(objeto.getID()) || objeto.getPosicion() != Constantes.OBJETO_POS_NO_EQUIPADO) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1OBJECT_DONT_EXIST");
				return;
			}
			if (_tienda.size() >= perso.getNivel()) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "166");
				return;
			}
			if (cantidad > objeto.getCantidad()) {
				cantidad = objeto.getCantidad();
			}
			int nuevaCantidad = objeto.getCantidad() - cantidad;
			if (nuevaCantidad >= 1) {
				final Objeto nuevoObj = objeto.clonarObjeto(nuevaCantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
				perso.addObjetoConOAKO(nuevoObj, true);
				objeto.setCantidad(cantidad);
				GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(perso, objeto);
			}
			perso.borrarOEliminarConOR(objeto.getID(), false);
			objeto.setPrecio(precio);
			addObjeto(objeto);
		} else {// si lo tiene en la tienda
			cantidad = objeto.getCantidad();
			objeto.setPrecio(precio);
		}
		GestorSalida.ENVIAR_EiK_MOVER_OBJETO_TIENDA(perso, '+', "", objeto.getID() + "|" + cantidad + "|" + objeto
		.getObjModeloID() + "|" + objeto.convertirStatsAString(false) + "|" + precio);
	}
	
	@Override
	public synchronized void remObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {
		System.out.println("cantidad " + cantidad);
		if (_tienda.remove(objeto)) {
			if (!perso.addObjIdentAInventario(objeto, true)) {
				objeto.setPrecio(0);
			}
			GestorSalida.ENVIAR_EiK_MOVER_OBJETO_TIENDA(perso, '-', "", objeto.getID() + "");
		}
	}
	
	@Override
	public void cerrar(Personaje perso, String exito) {
		perso.cerrarVentanaExchange(exito);
	}
	
	public void botonOK(Personaje perso) {}
	
	@Override
	public String getListaExchanger(Personaje perso) {
		// TODO Auto-generated method stub
		return null;
	}
}
