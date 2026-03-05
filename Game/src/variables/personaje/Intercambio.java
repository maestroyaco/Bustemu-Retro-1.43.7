package variables.personaje;

import java.util.ArrayList;
import estaticos.Constantes;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.MainServidor;
import estaticos.Mundo;
import estaticos.Mundo.Duo;
import sprites.Exchanger;
import variables.objeto.Objeto;

public class Intercambio implements Exchanger {
	private final Personaje _perso1, _perso2;
	private long _kamas1, _kamas2;
	private final ArrayList<Duo<Integer, Integer>> _objetos1 = new ArrayList<Duo<Integer, Integer>>(),
	_objetos2 = new ArrayList<Duo<Integer, Integer>>();
	private boolean _ok1, _ok2;
	
	public Intercambio(final Personaje p1, final Personaje p2) {
		_perso1 = p1;
		_perso2 = p2;
	}
	
	public synchronized void botonOK(final Personaje perso) {
		if (_perso1.getID() == perso.getID()) {
			_ok1 = !_ok1;
			GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_perso1, _ok1, perso.getID());
			GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_perso2, _ok1, perso.getID());
		} else if (_perso2.getID() == perso.getID()) {
			_ok2 = !_ok2;
			GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_perso1, _ok2, perso.getID());
			GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_perso2, _ok2, perso.getID());
		}
		if (_ok1 && _ok2) {
			aplicar();
		}
	}
	
	public void desCheck() {
		_ok1 = false;
		_ok2 = false;
		GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_perso1, _ok1, _perso1.getID());
		GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_perso2, _ok1, _perso1.getID());
		GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_perso1, _ok2, _perso2.getID());
		GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_perso2, _ok2, _perso2.getID());
	}
	
	public synchronized long getKamas(final int id) {
		if (_perso1.getID() == id) {
			return _kamas1;
		}
		if (_perso2.getID() == id) {
			return _kamas2;
		}
		return 0;
	}
	
	public void cerrar(Personaje perso, String exito) {
		_perso1.cerrarVentanaExchange(exito);
		GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_perso1);
		_perso2.cerrarVentanaExchange(exito);
		GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_perso2);
	}
	
	public synchronized void aplicar() {
		_kamas1 = Math.min(_kamas1, _perso1.getKamas());
		_kamas2 = Math.min(_kamas2, _perso2.getKamas());
		_perso1.addKamas(-_kamas1 + _kamas2, true, true);
		_perso2.addKamas(-_kamas2 + _kamas1, true, true);
		final StringBuilder str = new StringBuilder();
		str.append(_perso1.getNombre() + " (" + _perso1.getID() + ") >> ");
		str.append("[" + _kamas1 + " KAMAS]");
		for (final Duo<Integer, Integer> duo : _objetos1) {
			try {
				final Objeto obj1 = _perso1.getObjeto(duo._primero);
				final int cantidad = duo._segundo;
				str.append(", ");
				if (obj1 == null) {
					str.append("[NO TIENE - ID " + duo._primero + " CANT " + duo._segundo);
					continue;
				}
				str.append("[" + obj1.getObjModelo().getNombre() + " ID:" + obj1.getID() + " Mod:" + obj1.getObjModeloID()
				+ " Cant:" + cantidad + "]");
				int nuevaCantidad = obj1.getCantidad() - cantidad;
				if (nuevaCantidad >= 1) {
					Objeto nuevoObj = obj1.clonarObjeto(cantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
					_perso2.addObjIdentAInventario(nuevoObj, false);
					obj1.setCantidad(obj1.getCantidad() - cantidad);
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_perso1, obj1);
				} else {
					_perso1.borrarOEliminarConOR(obj1.getID(), false);
					_perso2.addObjIdentAInventario(obj1, true);
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		str.append(" ### ");
		str.append(_perso2.getNombre() + " (" + _perso2.getID() + ") >> ");
		str.append("[" + _kamas2 + " KAMAS]");
		for (final Duo<Integer, Integer> duo : _objetos2) {
			try {
				final Objeto obj2 = _perso2.getObjeto(duo._primero);
				final int cantidad = duo._segundo;
				str.append(", ");
				if (obj2 == null) {
					str.append("[NO TIENE - ID " + duo._primero + " CANT " + duo._segundo);
					continue;
				}
				str.append("[" + obj2.getObjModelo().getNombre() + " ID:" + obj2.getID() + " Mod:" + obj2.getObjModeloID()
				+ " Cant:" + cantidad + "]");
				int nuevaCantidad = obj2.getCantidad() - cantidad;
				if (nuevaCantidad >= 1) {
					Objeto nuevoObj = obj2.clonarObjeto(cantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
					_perso1.addObjIdentAInventario(nuevoObj, false);
					obj2.setCantidad(nuevaCantidad);
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_perso2, obj2);
				} else {
					_perso2.borrarOEliminarConOR(obj2.getID(), false);
					_perso1.addObjIdentAInventario(obj2, true);
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		if (MainServidor.PARAM_GUARDAR_LOGS_INTERCAMBIOS) {
			GestorSQL.INSERT_INTERCAMBIO(str.toString());
		}
		cerrar(null, "a");
	}
	
	@Override
	public synchronized void addObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {
		if (!perso.tieneObjetoID(objeto.getID()) || objeto.getPosicion() != Constantes.OBJETO_POS_NO_EQUIPADO) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "1OBJECT_DONT_EXIST");
			return;
		}
		final int cantInter = getCantObjeto(objeto.getID(), perso.getID());
		if (cantidad > objeto.getCantidad() - cantInter) {
			cantidad = objeto.getCantidad() - cantInter;
		}
		if (cantidad < 1) {
			return;
		}
		desCheck();
		final int objetoID = objeto.getID();
		final String str = objetoID + "|" + cantidad;
		final String add = "|" + objeto.getObjModeloID() + "|" + objeto.convertirStatsAString(false);
		if (_perso1.getID() == perso.getID()) {
			final Duo<Integer, Integer> duo = Mundo.getDuoPorIDPrimero(_objetos1, objetoID);
			if (duo != null) {
				duo._segundo += cantidad;
				GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_perso1, 'O', "+", objetoID + "|" + duo._segundo);
				GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_perso2, 'O', "+", objetoID + "|" + duo._segundo + add);
			} else {
				GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_perso1, 'O', "+", str);
				GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_perso2, 'O', "+", str + add);
				_objetos1.add(new Duo<Integer, Integer>(objetoID, cantidad));
			}
		} else if (_perso2.getID() == perso.getID()) {
			final Duo<Integer, Integer> duo = Mundo.getDuoPorIDPrimero(_objetos2, objetoID);
			if (duo != null) {
				duo._segundo += cantidad;
				GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_perso2, 'O', "+", objetoID + "|" + duo._segundo);
				GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_perso1, 'O', "+", objetoID + "|" + duo._segundo + add);
			} else {
				GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_perso2, 'O', "+", str);
				GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_perso1, 'O', "+", str + add);
				_objetos2.add(new Duo<Integer, Integer>(objetoID, cantidad));
			}
		}
	}
	
	@Override
	public synchronized void remObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {
		final int cantInter = getCantObjeto(objeto.getID(), perso.getID());
		if (cantidad > cantInter) {
			cantidad = cantInter;
		}
		if (cantidad < 1) {
			return;
		}
		desCheck();
		if (_perso1.getID() == perso.getID()) {
			final Duo<Integer, Integer> duo = Mundo.getDuoPorIDPrimero(_objetos1, objeto.getID());
			if (duo != null) {
				final int nuevaCantidad = duo._segundo - cantidad;
				if (nuevaCantidad <= 0) {
					_objetos1.remove(duo);
					GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_perso1, 'O', "-", objeto.getID() + "");
					GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_perso2, 'O', "-", objeto.getID() + "");
				} else {
					duo._segundo = nuevaCantidad;
					GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_perso1, 'O', "+", objeto.getID() + "|" + nuevaCantidad);
					GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_perso2, 'O', "+", objeto.getID() + "|" + nuevaCantidad + "|"
					+ objeto.getObjModeloID() + "|" + objeto.convertirStatsAString(false));
				}
			}
		} else if (_perso2.getID() == perso.getID()) {
			final Duo<Integer, Integer> duo = Mundo.getDuoPorIDPrimero(_objetos2, objeto.getID());
			if (duo != null) {
				final int nuevaCantidad = duo._segundo - cantidad;
				if (nuevaCantidad <= 0) {
					_objetos2.remove(duo);
					GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_perso1, 'O', "-", objeto.getID() + "");
					GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_perso2, 'O', "-", objeto.getID() + "");
				} else {
					duo._segundo = nuevaCantidad;
					GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_perso1, 'O', "+", objeto.getID() + "|" + nuevaCantidad + "|"
					+ objeto.getObjModeloID() + "|" + objeto.convertirStatsAString(false));
					GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_perso2, 'O', "+", objeto.getID() + "|" + nuevaCantidad);
				}
			}
		}
	}
	
	public synchronized int getCantObjeto(final int objetoID, final int persoID) {
		ArrayList<Duo<Integer, Integer>> objetos;
		if (_perso1.getID() == persoID) {
			objetos = _objetos1;
		} else {
			objetos = _objetos2;
		}
		for (final Duo<Integer, Integer> duo : objetos) {
			if (duo._primero == objetoID) {
				return duo._segundo;
			}
		}
		return 0;
	}
	
	@Override
	public void addKamas(long kamas, Personaje perso) {
		desCheck();
		if (kamas < 0) {
			return;
		}
		if (_perso1.getID() == perso.getID()) {
			_kamas1 = kamas;
			GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_perso1, 'G', "", kamas + "");
			GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_perso2, 'G', "", kamas + "");
		} else if (_perso2.getID() == perso.getID()) {
			_kamas2 = kamas;
			GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_perso2, 'G', "", kamas + "");
			GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_perso1, 'G', "", kamas + "");
		}
	}
	
	@Override
	public long getKamas() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String getListaExchanger(Personaje perso) {
		// TODO Auto-generated method stub
		return null;
	}
}