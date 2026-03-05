package variables.pelea;

import estaticos.Mundo;

public class DropMob {
	private final int _objModeloID, _prospeccion;
	private int _maximo, _nivelMin, _nivelMax;
	private float _porcentaje;
	private String _condicion = "";
	private final boolean _esDropFijo;
	
	public DropMob(final int objeto, final int prospeccion, final float porcentaje, final int max,
	final String condicion) {
		_objModeloID = objeto;
		_prospeccion = prospeccion;
		_porcentaje = (float) Math.max(0.001, Math.min(100, porcentaje));
		_maximo = Math.max(max, 1);
		_condicion = condicion;
		_esDropFijo = false;
	}
	
	public DropMob(final int objeto, final float porcentaje, final int nivelMin, final int nivelMax) {
		_objModeloID = objeto;
		_prospeccion = 1;
		_porcentaje = (float) Math.max(0.001, Math.min(100, porcentaje));
		_nivelMin = nivelMin;
		_nivelMax = nivelMax;
		_maximo = 1;
		_esDropFijo = true;
	}
	
	public int getMaximo() {
		return _maximo;
	}
	
	public int getIDObjModelo() {
		return _objModeloID;
	}
	
	public int getProspeccion() {
		return _prospeccion;
	}
	
	public float getPorcentaje() {
		return _porcentaje;
	}
	
	public String getCondicion() {
		return _condicion;
	}
	
	public boolean esDropFijo() {
		return _esDropFijo;
	}
	
	public int getNivelMax() {
		return _nivelMax;
	}
	
	public int getNivelMin() {
		return _nivelMin;
	}
	
	public boolean esIdentico(DropMob d) {
		if (d._esDropFijo != _esDropFijo) {
			return false;
		}
		if (!d._condicion.equals(_condicion)) {
			return false;
		}
		if (d._porcentaje != _porcentaje) {
			return false;
		}
		if (d._prospeccion != _prospeccion) {
			return false;
		}
		if (d._objModeloID != _objModeloID) {
			return false;
		}
		if (d._nivelMax != _nivelMax) {
			return false;
		}
		if (d._nivelMin != _nivelMin) {
			return false;
		}
		return true;
	}
	
	public String getString() {
		return this + " [_objModeloID=" + _objModeloID + " " + Mundo.getObjetoModelo(_objModeloID).getNombre()
		+ ", _prospeccion=" + _prospeccion + ", _maximo=" + _maximo + ", _nivelMin=" + _nivelMin + ", _nivelMax="
		+ _nivelMax + ", _porcentaje=" + _porcentaje + ", _condicion=" + _condicion + ", _esEtereo=" + _esDropFijo + "]";
	}
}
