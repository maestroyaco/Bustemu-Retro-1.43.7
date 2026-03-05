package variables.mercadillo;

import variables.objeto.Objeto;

public class ObjetoMercadillo implements Comparable<ObjetoMercadillo> {
	private int _lineaID;
	private final int _mercadilloID;
	private final int _tipoCantidad;
	private final int _cuentaID;
	private final long _precio;
	private final Objeto _objeto;
	
	public ObjetoMercadillo(final long precio, final int cant, final int dueño, final Objeto objeto, int mercadilloID) {
		_precio = precio;
		_tipoCantidad = cant;
		_cuentaID = dueño;
		_objeto = objeto;
		_mercadilloID = mercadilloID;
	}
	
	public int getMercadilloID() {
		return _mercadilloID;
	}
	
	public long getPrecio() {
		return _precio;
	}
	
	public int getTipoCantidad(final boolean cantidadReal) {
		if (cantidadReal) {
			return (int) (Math.pow(10.0D, _tipoCantidad) / 10.0D);
		}
		return _tipoCantidad;
	}
	
	public Objeto getObjeto() {
		return _objeto;
	}
	
	public int getObjetoID() {
		return _objeto.getID();
	}
	
	public int getLineaID() {
		return _lineaID;
	}
	
	public void setLineaID(final int ID) {
		_lineaID = ID;
	}
	
	public int getCuentaID() {
		return _cuentaID;
	}
	
	public String analizarParaEL() {
		return _objeto.getID() + ";" + getTipoCantidad(true) + ";" + _objeto.getObjModeloID() + ";" + _objeto
		.convertirStatsAString(false) + ";" + _precio + ";350";
	}
	
	public String analizarParaEmK() {
		return _objeto.getID() + "|" + getTipoCantidad(true) + "|" + _objeto.getObjModeloID() + "|" + _objeto
		.convertirStatsAString(false) + "|" + _precio + "|350";
	}
	
	public String analizarObjeto(final char separador) {
		return _lineaID + separador + getTipoCantidad(true) + separador + _objeto.getObjModeloID() + separador + _objeto
		.convertirStatsAString(false) + separador + _precio + separador + "350";
	}
	
	public int compareTo(final ObjetoMercadillo objMercadillo) {
		final long otroPrecio = objMercadillo.getPrecio();
		if (otroPrecio > _precio) {
			return -1;
		}
		if (otroPrecio == _precio) {
			return 0;
		}
		if (otroPrecio < _precio) {
			return 1;
		}
		return 0;
	}
}