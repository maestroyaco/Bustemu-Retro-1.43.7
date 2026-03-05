package variables.npc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import estaticos.Constantes;
import estaticos.GestorSalida;
import estaticos.Mundo;
import estaticos.Mundo.Duo;
import sprites.Exchanger;
import variables.objeto.Objeto;
import variables.objeto.ObjetoModelo;
import variables.objeto.ObjetoModelo.CAPACIDAD_STATS;
import variables.personaje.Personaje;

public class Trueque implements Exchanger {
	private final Personaje _perso;
	private final ArrayList<Duo<Integer, Integer>> _entregar = new ArrayList<Duo<Integer, Integer>>();
	private Map<Integer, Integer> _dar = new HashMap<Integer, Integer>();
	private final Map<Integer, Integer> _objetosModelo = new HashMap<Integer, Integer>();
	private boolean _ok, _resucitar, _polvo;
	private int _idMascota = 0, _npcID = 0;
	
	public Trueque(final Personaje perso, final boolean resucitar, int npcID) {
		_perso = perso;
		_resucitar = resucitar;
		_npcID = npcID;
	}
	
	public synchronized void botonOK(Personaje perso) {
		_ok = !_ok;
		GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_perso, _ok, _perso.getID());
		if (_ok) {
			aplicar();
		}
	}
	
	public synchronized void cerrar(Personaje perso, String exito) {
		_perso.cerrarVentanaExchange(exito);
	}
	
	public synchronized void aplicar() {
		Objeto mascota = null;
		for (final Duo<Integer, Integer> duo : _entregar) {
			final int cant = duo._segundo;
			if (cant == 0) {
				continue;
			}
			final Objeto obj = _perso.getObjeto(duo._primero);
			if (obj != null) {
				final int nuevaCant = obj.getCantidad() - cant;
				if (_resucitar && _polvo && obj.getObjModelo().getTipo() == Constantes.OBJETO_TIPO_FANTASMA_MASCOTA) {
					GestorSalida.ENVIAR_OR_ELIMINAR_OBJETO(_perso, duo._primero);
					mascota = obj;
				} else {
					if (nuevaCant <= 0) {
						_perso.borrarOEliminarConOR(duo._primero, true);
					} else {
						obj.setCantidad(nuevaCant);
						GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_perso, obj);
					}
				}
			}
		}
		if (mascota != null) {
			mascota.setPDV(1);
			mascota.setCantidad(1);
			mascota.setIDOjbModelo(Mundo.getMascotaPorFantasma(mascota.getObjModeloID()));
			_perso.addObjetoConOAKO(mascota, true);
		} else if (!_dar.isEmpty()) {
			for (Entry<Integer, Integer> entry : _dar.entrySet()) {
				try {
					int idObjModelo = entry.getKey();
					int cantidad = entry.getValue();
					_perso.addObjIdentAInventario(Mundo.getObjetoModelo(idObjModelo).crearObjeto(cantidad,
					Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM), false);
				} catch (Exception e) {}
			}
		}
		cerrar(_perso, "a");
	}
	
	@Override
	public synchronized void addObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {
		if (!perso.tieneObjetoID(objeto.getID()) || objeto.getPosicion() != Constantes.OBJETO_POS_NO_EQUIPADO) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "1OBJECT_DONT_EXIST");
			return;
		}
		int idModelo = objeto.getObjModeloID();
		final int cantInter = getCantObjeto(objeto.getID());
		if (cantidad > objeto.getCantidad() - cantInter) {
			cantidad = objeto.getCantidad() - cantInter;
		}
		if (cantidad <= 0) {
			return;
		}
		Duo<Integer, Integer> duo = Mundo.getDuoPorIDPrimero(_entregar, objeto.getID());
		if (_objetosModelo.get(idModelo) != null) {
			_objetosModelo.put(idModelo, _objetosModelo.get(idModelo) + cantidad);
		} else {
			_objetosModelo.put(idModelo, cantidad);
		}
		if (duo != null) {
			duo._segundo += cantidad;
		} else {
			duo = new Duo<Integer, Integer>(objeto.getID(), cantidad);
			_entregar.add(duo);
		}
		GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_perso, 'O', "+", objeto.getID() + "|" + duo._segundo);
		refrescar();
	}
	
	@Override
	public synchronized void remObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {
		int idModelo = objeto.getObjModeloID();
		final int cantInter = getCantObjeto(objeto.getID());
		if (cantidad > objeto.getCantidad() - cantInter) {
			cantidad = objeto.getCantidad() - cantInter;
		}
		if (cantidad <= 0) {
			return;
		}
		final Duo<Integer, Integer> duo = Mundo.getDuoPorIDPrimero(_entregar, objeto.getID());
		if (duo == null) {
			return;
		}
		try {
			_objetosModelo.put(idModelo, _objetosModelo.get(idModelo) - cantidad);
			if (_objetosModelo.get(idModelo) <= 0) {
				_objetosModelo.remove(idModelo);
			}
		} catch (Exception e) {}
		duo._segundo -= cantidad;
		if (duo._segundo <= 0) {
			_entregar.remove(duo);
			GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_perso, 'O', "-", objeto.getID() + "");
		} else {
			GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_perso, 'O', "+", objeto.getID() + "|" + duo._segundo);
		}
		refrescar();
	}
	
	private void refrescar() {
		if (!_resucitar) {
			int i = 1000000;
			for (int xx = 0; xx < _dar.size(); xx++) {
				GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_perso, 'O', "-", "" + i++);
			}
		} else {
			GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_perso, 'O', "-", "" + _idMascota);
		}
		Objeto mascota = null;
		_polvo = false;
		_idMascota = 0;
		for (final Duo<Integer, Integer> duo : _entregar) {
			final ObjetoModelo objModelo = Mundo.getObjeto(duo._primero).getObjModelo();
			if (_resucitar) {
				if (objModelo.getTipo() == Constantes.OBJETO_TIPO_FANTASMA_MASCOTA) {// fantasma
					mascota = Mundo.getObjeto(duo._primero);
					_idMascota = duo._primero;
				}
				if (objModelo.getID() == 8012) {// polvo de resurreccion
					_polvo = true;
				}
			}
		}
		if (_resucitar) {
			if (mascota != null && _polvo) {
				GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_perso, 'O', "+", mascota.getID() + "|1|" + Mundo
				.getMascotaPorFantasma(mascota.getObjModeloID()) + "|" + mascota.convertirStatsAString(false));
			}
		} else {
			int i = 1000000;
			_dar = Mundo.listaObjetosTruequePor(_objetosModelo, _npcID);
			for (Entry<Integer, Integer> entry : _dar.entrySet()) {
				GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_perso, 'O', "+", i++ + "|" + entry.getValue() + "|" + entry
				.getKey() + "|" + Mundo.getObjetoModelo(entry.getKey()).stringStatsModelo());
			}
		}
	}
	
	public synchronized int getCantObjeto(final int objetoID) {
		for (final Duo<Integer, Integer> duo : _entregar) {
			if (duo._primero == objetoID) {
				return duo._segundo;
			}
		}
		return 0;
	}
	
	@Override
	public void addKamas(long k, Personaje perso) {}
	
	@Override
	public long getKamas() {
		return 0;
	}
	
	@Override
	public String getListaExchanger(Personaje perso) {
		// TODO Auto-generated method stub
		return null;
	}
}