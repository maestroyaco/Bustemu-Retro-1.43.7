package variables.mercadillo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import sprites.Exchanger;
import variables.objeto.Objeto;
import variables.personaje.Cuenta;
import variables.personaje.Personaje;
import estaticos.Constantes;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.MainServidor;
import estaticos.Mundo;
import estaticos.Mundo.Duo;

public class Mercadillo implements Exchanger {
	private final int _id, _porcMercadillo;
	private short _tiempoVenta;
	private final short _maxObjCuenta, _nivelMax;
	private final String _tipoObjPermitidos;
	private final ArrayList<Short> _mapas = new ArrayList<Short>();
	private final CopyOnWriteArrayList<ObjetoMercadillo> _objMercadillos = new CopyOnWriteArrayList<>();
	private final Map<Integer, TipoObjetos> _tipoObjetos = new HashMap<Integer, TipoObjetos>();
	private final Map<Integer, Duo<Integer, Integer>> _lineas = new HashMap<Integer, Duo<Integer, Integer>>();
	
	public Mercadillo(final int id, final String mapaID, final int tasa, final short tiempoVenta,
	final short maxObjCuenta, final short nivelMax, final String tipoObj) {
		_id = id;
		final String[] mapas = mapaID.split(",");
		for (final String str : mapas) {
			_mapas.add(Short.parseShort(str));
		}
		_porcMercadillo = tasa;
		_maxObjCuenta = maxObjCuenta;
		_tipoObjPermitidos = tipoObj;
		_nivelMax = nivelMax;
		for (final String tipo : tipoObj.split(",")) {
			final int tipoID = Integer.parseInt(tipo);
			_tipoObjetos.put(tipoID, new TipoObjetos(tipoID));
		}
	}
	
	public int getID() {
		return _id;
	}
	
	public ArrayList<Short> getMapas() {
		return _mapas;
	}
	
	public int getPorcentajeImpuesto() {
		return _porcMercadillo;
	}
	
	//
	// public float getImpuesto() {
	// return _porcMercadillo / 100f;
	// }
	public short getTiempoVenta() {
		return _tiempoVenta;
	}
	
	public short getMaxObjCuenta() {
		return _maxObjCuenta;
	}
	
	public String getTipoObjPermitidos() {
		return _tipoObjPermitidos;
	}
	
	public short getNivelMax() {
		return _nivelMax;
	}
	
	public String getListaExchanger(Personaje perso) {
		StringBuilder packet = new StringBuilder();
		for (final ObjetoMercadillo objMerca : getObjetosMercadillos()) {
			if (objMerca == null) {
				continue;
			}
			if (objMerca.getCuentaID() != perso.getCuentaID()) {
				continue;
			}
			if (packet.length() > 0) {
				packet.append("|");
			}
			packet.append(objMerca.analizarParaEL());
		}
		return packet.toString();
	}
	
	public String strListaLineasPorModelo(final int modeloID) {
		try {
			final int tipo = Mundo.getObjetoModelo(modeloID).getTipo();
			return _tipoObjetos.get(tipo).getModelo(modeloID).strLineasPorObjMod();
		} catch (final Exception e) {
			return "";
		}
	}
	
	public boolean hayModeloEnEsteMercadillo(final int tipo, final int modeloID) {
		return _tipoObjetos.get(tipo).getModelo(modeloID) != null;
	}
	
	public String stringModelo(final int tipoObj) {
		return _tipoObjetos.get(tipoObj).stringModelo();
	}
	
	public boolean esTipoDeEsteMercadillo(final int tipoObj) {
		return _tipoObjetos.get(tipoObj) != null;
	}
	
	public LineaMercadillo getLinea(final int lineaID) {
		try {
			final int tipoObj = _lineas.get(lineaID)._primero;
			final int modeloID = _lineas.get(lineaID)._segundo;
			return _tipoObjetos.get(tipoObj).getModelo(modeloID).getLinea(lineaID);
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION getLinea linea: " + lineaID);
			e.printStackTrace();
			return null;
		}
	}
	
	public CopyOnWriteArrayList<ObjetoMercadillo> getObjetosMercadillos() {
		return _objMercadillos;
	}
	
	public void addObjMercaAlPuesto(final ObjetoMercadillo objMerca) {
		if (objMerca.getObjeto() == null) {
			MainServidor.redactarLogServidorln("Objeto del mercadillo no tiene objeto, linea: " + objMerca.getLineaID());
			return;
		}
		final int tipoObj = objMerca.getObjeto().getObjModelo().getTipo();
		final int modeloID = objMerca.getObjeto().getObjModeloID();
		if (_tipoObjetos.get(tipoObj) == null) {
			MainServidor.redactarLogServidorln("Bug Objeto del mercadillo " + _id + " , objetoID: " + objMerca.getObjeto()
			.getID() + ", objetoTipo: " + tipoObj);
			return;
		}
		// objMerca.setMercadilloID(_id);
		_tipoObjetos.get(tipoObj).addModeloVerificacion(objMerca);
		_lineas.put(objMerca.getLineaID(), new Duo<Integer, Integer>(tipoObj, modeloID));
		_objMercadillos.add(objMerca);
	}
	
	public void borrarPath(final int linea) {
		_lineas.remove(linea);
	}
	
	public boolean borrarObjMercaDelPuesto(final ObjetoMercadillo objMerca, final Personaje perso) {
		try {
			final int tipo = objMerca.getObjeto().getObjModelo().getTipo();
			final boolean borrable = _tipoObjetos.get(tipo).borrarObjMercaDeModelo(objMerca, perso, this);
			if (borrable) {
				_objMercadillos.remove(objMerca);
				GestorSQL.DELETE_OBJ_MERCADILLO(objMerca.getObjeto().getID());
			}
			return borrable;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public synchronized boolean comprarObjeto(final int lineaID, final int cant, final long precio,
	final Personaje nuevoDueño) {
		try {
			if (nuevoDueño.getKamas() < precio) {
				GestorSalida.ENVIAR_Im_INFORMACION(nuevoDueño, "1128;" + precio);
				return false;
			}
			final LineaMercadillo linea = getLinea(lineaID);
			final ObjetoMercadillo objAComprar = linea.tuTienes(cant, precio);
			final Objeto objeto = objAComprar.getObjeto();
			if (objeto == null || !borrarObjMercaDelPuesto(objAComprar, nuevoDueño)) {
				MainServidor.redactarLogServidorln("Bug objeto mercadillo " + objeto.getID());
				return false;
			}
			nuevoDueño.addObjIdentAInventario(objeto, true);
			objeto.getObjModelo().nuevoPrecio(objAComprar.getTipoCantidad(true), precio);
			nuevoDueño.addKamas(-precio, true, true);
			Cuenta viejoProp = Mundo.getCuenta(objAComprar.getCuentaID());
			if (viejoProp != null) {
				viejoProp.addKamasBanco(precio);
				if (viejoProp.getTempPersonaje() != null) {
					GestorSalida.ENVIAR_Im_INFORMACION(viejoProp.getTempPersonaje(), "065;" + precio + "~" + objeto
					.getObjModeloID() + "~" + objeto.getObjModeloID() + "~" + objeto.getCantidad());
				} else {
					viejoProp.addMensaje("Im073;" + precio + "~" + objeto.getObjModeloID() + "~" + objeto.getObjModeloID() + "~"
					+ objeto.getCantidad(), false);
				}
				GestorSQL.REPLACE_CUENTA_SERVIDOR(viejoProp, (byte) 0);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	@Override
	public void addKamas(long kamas, Personaje perso) {}
	
	@Override
	public long getKamas() {
		return 0;
	}
	
	public int cantObjMercaEnPuesto(int cuentaID) {
		int i = 0;
		for (ObjetoMercadillo objMerca : _objMercadillos) {
			if (objMerca.getCuentaID() == cuentaID) {
				i++;
			}
		}
		return i;
	}
	
	@Override
	public synchronized void addObjetoExchanger(Objeto objeto, int cantPow, Personaje perso, int precio) {
		if (precio <= 1) {
			return;
		}
		if (!perso.tieneObjetoID(objeto.getID()) || objeto.getPosicion() != Constantes.OBJETO_POS_NO_EQUIPADO) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "1OBJECT_DONT_EXIST");
			return;
		}
		if (cantObjMercaEnPuesto(perso.getCuentaID()) >= getMaxObjCuenta()) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "166");
			return;
		}
		final long restPrecio = (precio * getPorcentajeImpuesto() / 100);
		if (perso.getKamas() < restPrecio) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "176");
			return;
		}
		if (cantPow > 3) {
			cantPow = 3;
		} else if (cantPow < 1) {
			cantPow = 1;
		}
		final int cantReal = (int) (Math.pow(10, cantPow - 1));
		int nuevaCantidad = objeto.getCantidad() - cantReal;
		if (nuevaCantidad >= 1) {
			final Objeto nuevoObj = objeto.clonarObjeto(nuevaCantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
			perso.addObjetoConOAKO(nuevoObj, true);
			objeto.setCantidad(cantReal);
			// GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(perso, objeto);
		}
		perso.borrarOEliminarConOR(objeto.getID(), false);
		final ObjetoMercadillo objMerca = new ObjetoMercadillo(precio, cantPow, perso.getCuentaID(), objeto, _id);
		if (!GestorSQL.REPLACE_OBJETO_MERCADILLO(objMerca)) {
			return;
		}
		perso.addKamas(-restPrecio, true, true);
		addObjMercaAlPuesto(objMerca);
		GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(perso, '+', "", objMerca.analizarParaEmK());
	}
	
	@Override
	public synchronized void remObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {
		ObjetoMercadillo objMerca = null;
		try {
			for (final ObjetoMercadillo temp : _objMercadillos) {
				if (temp.getObjeto().getID() == objeto.getID()) {
					objMerca = temp;
					break;
				}
			}
		} catch (final Exception e) {
			return;
		}
		if (objMerca == null) {
			return;
		}
		perso.addObjIdentAInventario(objMerca.getObjeto(), true);
		borrarObjMercaDelPuesto(objMerca, null);
		GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(perso, '-', "", objeto.getID() + "");
	}
	
	@Override
	public void cerrar(Personaje perso, String exito) {
		perso.cerrarVentanaExchange(exito);
	}
	
	public void botonOK(Personaje perso) {}
}
