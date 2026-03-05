package variables.casa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import sprites.Exchanger;
import variables.objeto.Objeto;
import variables.personaje.Cuenta;
import variables.personaje.Personaje;
import estaticos.MainServidor;
import estaticos.Constantes;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.Mundo;

public class Cofre implements Exchanger {
	private final short _mapaID, _celdaID;
	private final int _id, _casaID;
	private int _persoID, _limite;
	private long _kamas;
	private String _clave = "-";
	private final Map<Integer, Objeto> _objetos = new HashMap<Integer, Objeto>();
	private ArrayList<Personaje> _consultores = new ArrayList<>();
	
	public Cofre(final int id, final int casaID, final short mapaID, final short celdaID, int limite) {
		_id = id;
		_casaID = casaID;
		_mapaID = mapaID;
		_celdaID = celdaID;
		if (_id <= 0) {
			_persoID = -1;
		}
		_limite = limite;
	}
	
	public void actualizarCofre(final String objetos, final long kamas, final String clave, final int dueñoID) {
		for (final String str : objetos.split(Pattern.quote("|"))) {
			try {
				if (str.isEmpty()) {
					continue;
				}
				final String[] infos = str.split(":");
				final int objetoID = Integer.parseInt(infos[0]);
				final Objeto objeto = Mundo.getObjeto(objetoID);
				if (objeto == null) {
					continue;
				}
				_objetos.put(objetoID, objeto);
			} catch (Exception e) {}
		}
		addKamas(kamas, null);
		_clave = clave;
		_persoID = dueñoID;
	}
	
	public int getID() {
		return _id;
	}
	
	public int getCasaID() {
		return _casaID;
	}
	
	public short getMapaID() {
		return _mapaID;
	}
	
	public short getCeldaID() {
		return _celdaID;
	}
	
	public long getKamas() {
		return _kamas;
	}
	
	public void setKamasCero() {
		_kamas = 0;
	}
	
	public void addKamas(final long kamas, Personaje perso) {
		if (kamas == 0) {
			return;
		}
		_kamas += kamas;
		if (_kamas < 0) {
			_kamas = 0;
		}
	}
	
	public String getClave() {
		return _clave;
	}
	
	public void setClave(final String clave) {
		_clave = clave;
	}
	
	public int getDueñoID() {
		return _persoID;
	}
	
	public void setDueñoID(final int dueñoID) {
		_persoID = dueñoID;
	}
	
	public boolean intentarAcceder(final Personaje perso, String clave) {
		if (!perso.estaDisponible(false, true)) {
			GestorSalida.ENVIAR_BN_NADA(perso, "INTENTA COFRE 1");
			return false;
		}
		final Casa casa = Mundo.getCasa(_casaID);
		if (casa == null) {
			abrirCofre(perso);
		} else {
			boolean esDelGremio = perso.getGremio() != null && perso.getGremio().getID() == casa.getGremioID();
			if (casa.getActParametros() && !esDelGremio && casa.tieneDerecho(
			Constantes.C_ACCESO_PROHIBIDO_COFRES_NO_MIEMBROS)) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1101");
				return false;
			} else if (clave.isEmpty()) {
				if (esSuCofreOPublico(perso) || _clave.equals("-") || (casa.getActParametros() && esDelGremio && casa
				.tieneDerecho(Constantes.C_ACCESOS_COFRES_MIEMBROS_SIN_CODIGO))) {
					abrirCofre(perso);
				} else {
					ponerClave(perso, false);// para insertar clave
				}
			} else {
				if (clave.equals(_clave)) {
					cerrarVentanaClave(perso);
					abrirCofre(perso);
				} else {}
				GestorSalida.ENVIAR_KKE_ERROR_CLAVE(perso);
			}
		}
		return true;
	}
	
	public void modificarClave(final Personaje perso, final String packet) {
		if (packet.isEmpty()) {
			return;
		}
		if (_persoID == perso.getID()) {
			_clave = packet;
		}
		cerrarVentanaClave(perso);
	}
	
	public void ponerClave(final Personaje perso, boolean modificarClave) {
		perso.setConsultarCofre(this);
		GestorSalida.ENVIAR_KCK_VENTANA_CLAVE(perso, modificarClave, (byte) 8);// para bloquear
	}
	
	public void cerrarVentanaClave(Personaje perso) {
		perso.setConsultarCofre(null);
		GestorSalida.ENVIAR_KV_CERRAR_VENTANA_CLAVE(perso);
	}
	
	public void abrirCofre(Personaje perso) {
		if (!_consultores.contains(perso)) {
			_consultores.add(perso);
		}
		perso.setExchanger(this);
		perso.setTipoExchange(Constantes.INTERCAMBIO_TIPO_COFRE);
		GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(perso, Constantes.INTERCAMBIO_TIPO_COFRE, "");
		GestorSalida.ENVIAR_EL_LISTA_EXCHANGER(perso, this);
	}
	
	public boolean esSuCofreOPublico(final Personaje perso) {
		return (_persoID == perso.getID() || _persoID == -1);
	}
	
	public Collection<Objeto> getObjetos() {
		return _objetos.values();
	}
	
	public String getListaExchanger(Personaje perso) {
		final StringBuilder packet = new StringBuilder();
		for (final Objeto objeto : _objetos.values()) {
			if (objeto == null) {
				continue;
			}
			packet.append("O" + objeto.stringObjetoConGuiño());
		}
		if (_kamas > 0) {
			packet.append("G" + getKamas());
		}
		return packet.toString();
	}
	
	public String analizarObjetoCofreABD() {
		final StringBuilder str = new StringBuilder();
		for (final Objeto objeto : _objetos.values()) {
			if (objeto == null) {
				continue;
			}
			if (str.length() > 0) {
				str.append("|");
			}
			str.append(objeto.getID());
		}
		return str.toString();
	}
	
	public void addObjetoRapido(Objeto obj) {
		if (obj == null) {
			return;
		}
		_objetos.put(obj.getID(), obj);
	}
	
	@Override
	public synchronized void addObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {
		if (_casaID != -1) {
			if (objeto.tieneStatTexto(Constantes.STAT_LIGADO_A_CUENTA)) {
				GestorSalida.ENVIAR_BN_NADA(perso, "INTERCAMBIO MOVER LIGADO");
				return;
			}
			if (!objeto.pasoIntercambiableDesde()) {
				GestorSalida.ENVIAR_BN_NADA(perso, "INTERCAMBIO MOVER NO INTERCAMBIABLE");
				return;
			}
		}
		if (_objetos.size() >= _limite) {
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "Llegaste al máximo de objetos que puede soportar este cofre",
			Constantes.COLOR_ROJO);
			return;
		}
		if (!perso.tieneObjetoID(objeto.getID()) || objeto.getPosicion() != Constantes.OBJETO_POS_NO_EQUIPADO) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "1OBJECT_DONT_EXIST");
			return;
		}
		if (cantidad > objeto.getCantidad()) {
			cantidad = objeto.getCantidad();
		}
		String str = "";
		Objeto cofreObj = objetoSimilarEnElCofre(objeto);
		final int nuevaCant = objeto.getCantidad() - cantidad;
		if (cofreObj == null) {
			if (nuevaCant <= 0) {
				perso.borrarOEliminarConOR(objeto.getID(), false);
				_objetos.put(objeto.getID(), objeto);
				str = "O+" + objeto.getID() + "|" + objeto.getCantidad() + "|" + objeto.getObjModeloID() + "|" + objeto
				.convertirStatsAString(false);
			} else {
				cofreObj = objeto.clonarObjeto(cantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
				Mundo.addObjeto(cofreObj, false);
				_objetos.put(cofreObj.getID(), cofreObj);
				objeto.setCantidad(nuevaCant);
				str = "O+" + cofreObj.getID() + "|" + cofreObj.getCantidad() + "|" + cofreObj.getObjModeloID() + "|" + cofreObj
				.convertirStatsAString(false);
				GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(perso, objeto);
			}
		} else {
			if (nuevaCant <= 0) {
				perso.borrarOEliminarConOR(objeto.getID(), true);
				cofreObj.setCantidad(cofreObj.getCantidad() + objeto.getCantidad());
				str = "O+" + cofreObj.getID() + "|" + cofreObj.getCantidad() + "|" + cofreObj.getObjModeloID() + "|" + cofreObj
				.convertirStatsAString(false);
			} else {
				objeto.setCantidad(nuevaCant);
				cofreObj.setCantidad(cofreObj.getCantidad() + cantidad);
				str = "O+" + cofreObj.getID() + "|" + cofreObj.getCantidad() + "|" + cofreObj.getObjModeloID() + "|" + cofreObj
				.convertirStatsAString(false);
				GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(perso, objeto);
			}
		}
		for (final Personaje pj : _consultores) {
			GestorSalida.ENVIAR_EsK_MOVER_A_TIENDA_COFRE_BANCO(pj, str);
		}
	}
	
	@Override
	public synchronized void remObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {
		if (!_objetos.containsKey(objeto.getID())) {
			GestorSalida.ENVIAR_EsK_MOVER_A_TIENDA_COFRE_BANCO(perso, "O-" + objeto.getID());
			return;
		}
		if (cantidad > objeto.getCantidad()) {
			cantidad = objeto.getCantidad();
		}
		String str;
		final int nuevaCant = objeto.getCantidad() - cantidad;
		if (nuevaCant < 1) {
			_objetos.remove(objeto.getID());
			perso.addObjIdentAInventario(objeto, true);
			str = "O-" + objeto.getID();
		} else {
			Objeto nuevoObj = objeto.clonarObjeto(cantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
			perso.addObjIdentAInventario(nuevoObj, true);
			objeto.setCantidad(nuevaCant);
			str = "O+" + objeto.getID() + "|" + objeto.getCantidad() + "|" + objeto.getObjModeloID() + "|" + objeto
			.convertirStatsAString(false);
		}
		for (final Personaje pj : _consultores) {
			GestorSalida.ENVIAR_EsK_MOVER_A_TIENDA_COFRE_BANCO(pj, str);
		}
	}
	
	private synchronized Objeto objetoSimilarEnElCofre(final Objeto objeto) {
		if (objeto.puedeTenerStatsIguales()) {
			for (final Objeto obj : _objetos.values()) {
				if (Constantes.esPosicionEquipamiento(obj.getPosicion())) {
					continue;
				}
				if (objeto.getID() != obj.getID() && obj.getObjModeloID() == objeto.getObjModeloID() && obj.sonStatsIguales(
				objeto)) {
					return obj;
				}
			}
		}
		return null;
	}
	
	public synchronized void limpiarCofre() {
		for (final Entry<Integer, Objeto> obj : _objetos.entrySet()) {
			Mundo.eliminarObjeto(obj.getKey());
		}
		_objetos.clear();
	}
	
	public synchronized void moverCofreABanco(final Cuenta cuenta) {
		for (Objeto obj : _objetos.values()) {
			cuenta.addObjetoAlBanco(obj);
		}
		_objetos.clear();
	}
	
	public static Cofre insertarCofre(short mapaID, short celdaID) {
		try {
			final Casa casa = Mundo.getCasaDentroPorMapa(mapaID);
			if (casa == null) {
				return null;
			}
			final Cofre c = Mundo.getCofrePorUbicacion(mapaID, celdaID);
			if (c != null) {
				return null;
			}
			if (Mundo.getMapa(mapaID).getCelda(celdaID).getObjetoInteractivo() == null) {
				return null;
			}
			int id = GestorSQL.GET_COFRE_POR_MAPA_CELDA(mapaID, celdaID);
			if (id == -1) {
				GestorSQL.INSERT_COFRE_MODELO(casa.getID(), mapaID, celdaID);
				id = GestorSQL.GET_COFRE_POR_MAPA_CELDA(mapaID, celdaID);
				if (id == -1) {
					return null;
				}
			}
			final Cofre cofre = new Cofre(id, casa.getID(), mapaID, celdaID, MainServidor.LIMITE_OBJETOS_COFRE);
			cofre.actualizarCofre("", 0, "-", casa.getDueño() != null ? casa.getDueño().getID() : 0);
			Mundo.addCofre(cofre);
			GestorSQL.REPLACE_COFRE(cofre, false);
			return cofre;
		} catch (final Exception e) {
			return null;
		}
	}
	
	@Override
	public void cerrar(Personaje perso, String exito) {
		_consultores.remove(perso);
		perso.cerrarVentanaExchange(exito);
	}
	
	@Override
	public void botonOK(Personaje perso) {}
}