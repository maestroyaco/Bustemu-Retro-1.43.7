package variables.mercadillo;

import java.util.ArrayList;
import java.util.Collections;
import variables.objeto.Objeto;

public class LineaMercadillo {
	private final int _lineaID, _modeloID;
	private final ArrayList<ArrayList<ObjetoMercadillo>> _categoriasDeUnaLinea = new ArrayList<ArrayList<ObjetoMercadillo>>(
	3);
	private final String _strStats;
	
	public LineaMercadillo(final int lineaID, final ObjetoMercadillo objMerca) {
		_lineaID = lineaID;
		final Objeto objeto = objMerca.getObjeto();
		_strStats = objeto.convertirStatsAString(false);
		_modeloID = objeto.getObjModeloID();
		for (int i = 0; i < 3; i++) {
			_categoriasDeUnaLinea.add(new ArrayList<ObjetoMercadillo>());
		}
		final int categoria = objMerca.getTipoCantidad(false) - 1;
		_categoriasDeUnaLinea.get(categoria).add(objMerca);
		ordenar(categoria);
		objMerca.setLineaID(_lineaID);
	}
	
	public boolean tieneIgual(final ObjetoMercadillo objMerca) {
		if (!lineaVacia() && !tieneMismoStats(objMerca.getObjeto())) {
			return false;
		}
		objMerca.setLineaID(_lineaID);
		final int categoria = objMerca.getTipoCantidad(false) - 1;
		_categoriasDeUnaLinea.get(categoria).add(objMerca);
		ordenar(categoria);
		return true;
	}
	
	private boolean tieneMismoStats(final Objeto objeto) {
		return _strStats.equalsIgnoreCase(objeto.convertirStatsAString(false));
	}
	
	public ObjetoMercadillo tuTienes(final int categoria, final long precio) {
		final int index = categoria - 1;
		for (int i = 0; i < _categoriasDeUnaLinea.get(index).size(); i++) {
			if (_categoriasDeUnaLinea.get(index).get(i).getPrecio() == precio) {
				return _categoriasDeUnaLinea.get(index).get(i);
			}
		}
		return null;
	}
	
	public long[] getLos3PreciosPorLinea() {
		final long[] str = new long[3];
		for (int i = 0; i < _categoriasDeUnaLinea.size(); i++) {
			try {
				str[i] = _categoriasDeUnaLinea.get(i).get(0).getPrecio();
			} catch (final IndexOutOfBoundsException e) {
				str[i] = 0;
			}
		}
		return str;
	}
	
//	public ArrayList<ObjetoMercadillo> todosObjMercaDeUnaLinea() {
//		final int totalEntradas = _categoriasDeUnaLinea.get(0).size() + _categoriasDeUnaLinea.get(1).size()
//		+ _categoriasDeUnaLinea.get(2).size();
//		final ArrayList<ObjetoMercadillo> todosObjMerca = new ArrayList<ObjetoMercadillo>(totalEntradas);
//		for (int cat = 0; cat < _categoriasDeUnaLinea.size(); cat++) {
//			todosObjMerca.addAll(_categoriasDeUnaLinea.get(cat));
//		}
//		return todosObjMerca;
//	}
	
	public boolean borrarObjMercaDeLinea(final ObjetoMercadillo objMerca) {
		final int categoria = objMerca.getTipoCantidad(false) - 1;// 1, 10 ,100
		final boolean borrable = _categoriasDeUnaLinea.get(categoria).remove(objMerca);
		ordenar(categoria);
		return borrable;
	}
	
	public String strListaDeLineasDeModelo() {
		final long[] precio = getLos3PreciosPorLinea();
		final String str = _lineaID + ";" + _strStats + ";" + (precio[0] == 0 ? "" : precio[0]) + ";"
		+ (precio[1] == 0 ? "" : precio[1]) + ";" + (precio[2] == 0 ? "" : precio[2]);
		return str;
	}
	
	public String str3PrecioPorLinea() {
		final long[] precio = getLos3PreciosPorLinea();
		final String str = _lineaID + "|" + _modeloID + "|" + _strStats + "|" + (precio[0] == 0 ? "" : precio[0]) + "|"
		+ (precio[1] == 0 ? "" : precio[1]) + "|" + (precio[2] == 0 ? "" : precio[2]);
		return str;
	}
	
	public void ordenar(final int categoria) {
		Collections.sort(_categoriasDeUnaLinea.get(categoria));
	}
	
	public boolean lineaVacia() {
		for (int i = 0; i < 3;) {// 3 categorias
			try {
				if (_categoriasDeUnaLinea.get(i).get(0) != null) {
					return false;
				}
			} catch (final Exception e) {}
			i++;
		}
		return true;
	}
}