package variables.npc;

import java.util.ArrayList;
import variables.personaje.Personaje;
import variables.zotros.Accion;

public class RespuestaNPC {
	private final int _id;
	private final ArrayList<Accion> _acciones = new ArrayList<Accion>();
	private String _condicion = "";
	
	public RespuestaNPC(final int id) {
		_id = id;
	}
	
	public int getID() {
		return _id;
	}
	
	public String getCondicion() {
		return _condicion;
	}
	//
	// public void setCondicion(String cond) {
	// _condicion = cond;
	// }
	
	public void borrarAcciones() {
		_acciones.clear();
		_condicion = "";
	}
	
	public ArrayList<Accion> getAcciones() {
		return _acciones;
	}
	
	public void addAccion(final Accion accion) {
		final ArrayList<Accion> c = new ArrayList<Accion>();
		c.addAll(_acciones);
		String condicion = accion.getCondicion();
		if (condicion.isEmpty()) {
			condicion = null;
		} else if (condicion.equals("BN")) {
			condicion = "";
		}
		for (final Accion a : c) {
			if (a.getID() == accion.getID()) {
				_acciones.remove(a);
			} else if (condicion != null) {
				a.setCondicion(condicion);
			}
		}
		if (condicion != null) {
			accion.setCondicion(condicion);
			_condicion = condicion;
		}
		_acciones.add(accion);
	}
	
	public void aplicar(final Personaje perso) {
		perso.setPreguntaID(0);
		for (final Accion accion : _acciones) {
			accion.realizarAccion(perso, null, -1, (short) -1);
		}
	}
}