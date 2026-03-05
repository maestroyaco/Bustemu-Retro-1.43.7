package variables.mapa.interactivo;

import variables.zotros.Accion;

public class OtroInteractivo {
	private final int _gfxID, _accionID;
	private final short _mapaID, _celdaID;
	private final String _args, _condicion;
	private final Accion _accion;
	private final int _tiempoRecarga;
	
	public OtroInteractivo(final int id, final short mapaID, final short celdaID, final int accion, final String args,
	final String condiciones, int tiempoRecarga) {
		_gfxID = id;
		_mapaID = mapaID;
		_celdaID = celdaID;
		_accionID = accion;
		_args = args;
		_condicion = condiciones;
		_tiempoRecarga = tiempoRecarga;
		_accion = new Accion(_accionID, _args, "");
	}
	
	public int getGfxID() {
		return _gfxID;
	}
	
	public int getTiempoRecarga() {
		return _tiempoRecarga;
	}
	
	public short getMapaID() {
		return _mapaID;
	}
	
	public short getCeldaID() {
		return _celdaID;
	}
	
	public int getAccionID() {
		return _accionID;
	}
	
	public String getArgs() {
		return _args;
	}
	
	public Accion getAccion() {
		return _accion;
	}
	
	public String getCondicion() {
		return _condicion;
	}
}