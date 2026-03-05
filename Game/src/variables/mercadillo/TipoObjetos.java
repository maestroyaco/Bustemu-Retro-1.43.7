package variables.mercadillo;

import java.util.HashMap;
import java.util.Map;
import variables.personaje.Personaje;
import estaticos.GestorSalida;

public class TipoObjetos {
	private Map<Integer, ModeloMercadillo> _modelosDeUnTipo = new HashMap<Integer, ModeloMercadillo>();
	private int _tipoObjeto;
	
	public TipoObjetos(final int tipoObjeto) {
		_tipoObjeto = tipoObjeto;
	}
	
	public int getTipoObjetoID() {
		return _tipoObjeto;
	}
	
	public void addModeloVerificacion(final ObjetoMercadillo objMerca) {
		final int modeloID = objMerca.getObjeto().getObjModeloID();
		final ModeloMercadillo modelo = _modelosDeUnTipo.get(modeloID);
		if (modelo == null) {
			_modelosDeUnTipo.put(modeloID, new ModeloMercadillo(modeloID, objMerca));
		} else {
			modelo.addObjMercaConLinea(objMerca);
		}
	}
	
	public boolean borrarObjMercaDeModelo(final ObjetoMercadillo objMerca, final Personaje perso, final Mercadillo puesto) {
		final int idModelo = objMerca.getObjeto().getObjModeloID();
		final boolean borrable = _modelosDeUnTipo.get(idModelo).borrarObjMercaDeUnaLinea(objMerca, perso, puesto);
		if (_modelosDeUnTipo.get(idModelo).estaVacio()) {
			_modelosDeUnTipo.remove(idModelo);
			GestorSalida.ENVIAR_EHM_MOVER_OBJMERCA_POR_MODELO(perso, "-", idModelo + "");
		}
		return borrable;
	}
	
	public ModeloMercadillo getModelo(final int modeloID) {
		return _modelosDeUnTipo.get(modeloID);
	}
	
	// public ArrayList<ObjetoMercadillo> todoListaObjMercaDeUnTipo() {
	// final ArrayList<ObjetoMercadillo> listaObjMerca = new ArrayList<ObjetoMercadillo>();
	// for (final ModeloMercadillo modelo : _modelosDeUnTipo.values()) {
	// listaObjMerca.addAll(modelo.todosObjMercaDeUnModelo());
	// }
	// return listaObjMerca;
	// }
	public String stringModelo() {
		final StringBuilder str = new StringBuilder();
		for (final int idModelo : _modelosDeUnTipo.keySet()) {
			if (str.length() > 0) {
				str.append(";");
			}
			str.append(idModelo);
		}
		return str.toString();
	}
}
