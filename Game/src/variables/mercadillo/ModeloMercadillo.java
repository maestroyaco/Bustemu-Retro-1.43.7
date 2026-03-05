package variables.mercadillo;

import java.util.HashMap;
import java.util.Map;
import variables.personaje.Personaje;
import estaticos.GestorSalida;
import estaticos.Mundo;

public class ModeloMercadillo {
	private final int _modeloID;
	private final Map<Integer, LineaMercadillo> _lineasDeUnModelo = new HashMap<Integer, LineaMercadillo>();
	
	public ModeloMercadillo(final int modeloID, final ObjetoMercadillo objMerca) {
		_modeloID = modeloID;
		addObjMercaConLinea(objMerca);
	}
	
	public void addObjMercaConLinea(final ObjetoMercadillo objMerca) {
		for (final LineaMercadillo linea : _lineasDeUnModelo.values()) {
			if (linea.tieneIgual(objMerca)) {
				return;
			}
		}
		final int lineaID = Mundo.sigIDLineaMercadillo();
		_lineasDeUnModelo.put(lineaID, new LineaMercadillo(lineaID, objMerca));
	}
	
	public LineaMercadillo getLinea(final int lineaID) {
		return _lineasDeUnModelo.get(lineaID);
	}
	
	public boolean borrarObjMercaDeUnaLinea(final ObjetoMercadillo objMerca, final Personaje perso,
	final Mercadillo puesto) {
		final int lineaID = objMerca.getLineaID();
		final boolean borrable = _lineasDeUnModelo.get(lineaID).borrarObjMercaDeLinea(objMerca);
		if (_lineasDeUnModelo.get(lineaID).lineaVacia()) {
			_lineasDeUnModelo.remove(lineaID);
			puesto.borrarPath(lineaID);
			GestorSalida.ENVIAR_EHm_DETALLE_LINEA_CON_PRECIOS(perso, "-", lineaID + "");
		} else {
			GestorSalida
			.ENVIAR_EHm_DETALLE_LINEA_CON_PRECIOS(perso, "+", _lineasDeUnModelo.get(lineaID).str3PrecioPorLinea());
		}
		return borrable;
	}
	
	// public ArrayList<ObjetoMercadillo> todosObjMercaDeUnModelo() {
	// final ArrayList<ObjetoMercadillo> listaObj = new ArrayList<ObjetoMercadillo>();
	// for (final LineaMercadillo linea : _lineasDeUnModelo.values()) {
	// listaObj.addAll(linea.todosObjMercaDeUnaLinea());
	// }
	// return listaObj;
	// }
	public String strLineasPorObjMod() {
		final StringBuilder str = new StringBuilder();
		for (final LineaMercadillo linea : _lineasDeUnModelo.values()) {
			if (str.length() > 0) {
				str.append("|");
			}
			str.append(linea.strListaDeLineasDeModelo());
		}
		return _modeloID + "|" + str.toString();
	}
	
	public boolean estaVacio() {
		return _lineasDeUnModelo.isEmpty();
	}
}
