package variables.casa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import variables.gremio.Gremio;
import variables.mapa.Mapa;
import variables.personaje.Personaje;
import estaticos.Constantes;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.MainServidor;
import estaticos.Mundo;

public class Casa {
	private boolean _actParametros;
	private final short _mapaIDFuera, _celdaIDFuera;
	private short _mapaIDDentro, _celdaIDDentro;
	private final int _id;
	private Personaje _dueñoID;
	private Gremio _gremio;
	private int _derechosGremio;
	private long _kamasVenta = 1000000;
	private String _clave = "-";
	private final Map<Integer, Boolean> _tieneDerecho = new HashMap<Integer, Boolean>();
	private final ArrayList<Short> _mapasContenidos = new ArrayList<Short>();
	
	public Casa(final int id, final short mapaIDFuera, final short celdaIDFuera, final short mapaIDDentro,
	final short celdaIDDentro, final long precio, final String mapasContenidos) {
		_id = id;
		_mapaIDFuera = mapaIDFuera;
		_celdaIDFuera = celdaIDFuera;
		_mapaIDDentro = mapaIDDentro;
		_celdaIDDentro = celdaIDDentro;
		_kamasVenta = precio;
		for (final String str : mapasContenidos.split(";")) {
			try {
				_mapasContenidos.add(Short.parseShort(str));
			} catch (final Exception e) {}
		}
		_mapasContenidos.trimToSize();
	}
	
	public void actualizarCasa(final int dueño, final long precio, final byte bloqueado, final String clave,
	final int derechos) {
		_dueñoID = Mundo.getPersonaje(dueño);
		_kamasVenta = precio;
		_clave = clave;
		_actParametros = bloqueado == 1;
		if (_actParametros && _dueñoID != null) {
			_gremio = _dueñoID.getGremio();
			if (_gremio == null) {
				_actParametros = false;
			}
		}
		_derechosGremio = derechos;
		analizarDerechos(_derechosGremio);
	}
	
	public int getID() {
		return _id;
	}
	
	public short getMapaIDFuera() {
		return _mapaIDFuera;
	}
	
	public short getCeldaIDFuera() {
		return _celdaIDFuera;
	}
	
	public ArrayList<Short> getMapasContenidos() {
		return _mapasContenidos;
	}
	
	public Personaje getDueño() {
		return _dueñoID;
	}
	
	public long getKamasVenta() {
		return _kamasVenta;
	}
	
	public int getDerechosGremio() {
		return _derechosGremio;
	}
	
	public String getClave() {
		return _clave;
	}
	
	public short getMapaIDDentro() {
		return _mapaIDDentro;
	}
	
	public void setMapaIDDentro(final short mapa) {
		_mapaIDDentro = mapa;
	}
	
	public short getCeldaIDDentro() {
		return _celdaIDDentro;
	}
	
	public void setCeldaIDDentro(final short celda) {
		_celdaIDDentro = celda;
	}
	
	public boolean getActParametros() {
		return _actParametros;
	}
	
	public boolean esSuCasa(final int id) {
		if (_dueñoID != null && _dueñoID.getID() == id) {
			return true;
		}
		return false;
	}
	
	public void resetear() {
		_dueñoID = null;
		_gremio = null;
		_kamasVenta = 1000000;
		_clave = "-";
		_actParametros = false;
		actualizarDerechos(0);
	}
	
	public int getGremioID() {
		return _gremio == null ? 0 : _gremio.getID();
	}
	
	public void nullearGremio() {
		_gremio = null;
	}
	
	public boolean tieneDerecho(final int derecho) {
		return _tieneDerecho.get(derecho);
	}
	
	public void iniciarDerechos() {
		_tieneDerecho.clear();
		_tieneDerecho.put(Constantes.C_VISIBLE_PARA_GREMIO, false);
		_tieneDerecho.put(Constantes.C_ESCUDO_VISIBLE_MIEMBROS, false);
		_tieneDerecho.put(Constantes.C_ESCUDO_VISIBLE_PARA_TODOS, false);
		_tieneDerecho.put(Constantes.C_ACCESOS_MIEMBROS_SIN_CODIGO, false);
		_tieneDerecho.put(Constantes.C_ACCESO_PROHIBIDO_NO_MIEMBROS, false);
		_tieneDerecho.put(Constantes.C_ACCESOS_COFRES_MIEMBROS_SIN_CODIGO, false);
		_tieneDerecho.put(Constantes.C_ACCESO_PROHIBIDO_COFRES_NO_MIEMBROS, false);
		_tieneDerecho.put(Constantes.C_TELEPORT_GREMIO, false);
		_tieneDerecho.put(Constantes.C_DESCANSO_GREMIO, false);
	}
	
	public void analizarDerechos(final int derechos) {
		iniciarDerechos();
		for (int i = 0; i < 8; i++) {
			final int exp = (int) Math.pow(2, i);
			if ((exp & derechos) == exp) {
				_tieneDerecho.put(exp, true);
			}
		}
	}
	
	public boolean intentarAcceder(final Personaje perso, final String clave) {
		if (!perso.estaDisponible(false, true)) {
			GestorSalida.ENVIAR_BN_NADA(perso);
			return false;
		}
		boolean esDelGremio = perso.getGremio() != null && perso.getGremio().getID() == getGremioID();
		if (_actParametros && !esDelGremio && tieneDerecho(Constantes.C_ACCESO_PROHIBIDO_NO_MIEMBROS)) {//
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "1101");
			return false;
		} else if (clave.isEmpty()) {
			if (esSuCasa(perso.getID()) || _clave.equals("-") || (_actParametros && esDelGremio && tieneDerecho(
			Constantes.C_ACCESOS_MIEMBROS_SIN_CODIGO))) {
				perso.teleport(_mapaIDDentro, _celdaIDDentro);
				return false;
			} else {
				ponerClave(perso, false);
			}
		} else {
			if (clave.equals(_clave)) {
				cerrarVentanaClave(perso);
				perso.teleport(_mapaIDDentro, _celdaIDDentro);
			} else {
				GestorSalida.ENVIAR_KKE_ERROR_CLAVE(perso);
			}
		}
		return true;
	}
	
	public void expulsar(final Personaje perso, final String packet) {
		if (!esSuCasa(perso.getID())) {
			GestorSalida.ENVIAR_BN_NADA(perso);
		} else {
			try {
				final Personaje objetivo = Mundo.getPersonaje(Integer.parseInt(packet));
				if (objetivo == null || !objetivo.enLinea() || objetivo.getPelea() != null || objetivo.getMapa()
				.getID() != perso.getMapa().getID()) {
					return;
				}
				objetivo.teleport(_mapaIDFuera, _celdaIDFuera);
				GestorSalida.ENVIAR_Im_INFORMACION(objetivo, "018;" + perso.getNombre());
			} catch (final Exception e) {}
		}
	}
	
	public void quitarCerrojo(Personaje perso) {
		if (esSuCasa(perso.getID())) {
			_clave = "-";
			// _actParametros = false;
			GestorSalida.ENVIAR_hL_INFO_CASA(perso, informacionCasa(perso.getID()));
		} else {
			GestorSalida.ENVIAR_BN_NADA(perso);
		}
	}
	
	public void ponerClave(Personaje perso, boolean modificarClave) {
		perso.setConsultarCasa(this);
		GestorSalida.ENVIAR_KCK_VENTANA_CLAVE(perso, modificarClave, (byte) 8);// para bloquear clave
	}
	
	public void cerrarVentanaClave(Personaje perso) {
		perso.setConsultarCasa(null);
		GestorSalida.ENVIAR_KV_CERRAR_VENTANA_CLAVE(perso);
	}
	
	public void modificarClave(final Personaje perso, final String packet) {
		if (packet.isEmpty()) {
			return;
		}
		if (esSuCasa(perso.getID())) {
			_clave = packet;
			// _actParametros = false;
			if (packet.length() > 8) {
				_clave = packet.substring(0, 8);
			}
			GestorSalida.ENVIAR_hL_INFO_CASA(perso, informacionCasa(perso.getID()));
		}
		cerrarVentanaClave(perso);
	}
	
	public void comprarCasa(final Personaje perso) {
		if (esSuCasa(perso.getID()) || Mundo.getCasaDePj(perso.getID()) != null) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "132;1");
			return;
		}
		if (_kamasVenta <= 0 || perso.getKamas() < _kamasVenta) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "1CANT_BUY_HOUSE;" + _kamasVenta);
			return;
		}
		perso.addKamas(-_kamasVenta, true, true);
		long kamasCofre = 0;
		for (final Cofre cofre : Mundo.getCofresPorCasa(this)) {
			try {
				cofre.moverCofreABanco(_dueñoID.getCuenta());
			} catch (final Exception e) {}
			kamasCofre += cofre.getKamas();
			cofre.setKamasCero();
			cofre.setClave("-");
			cofre.setDueñoID(perso.getID());
			GestorSQL.REPLACE_COFRE(cofre, false);
		}
		try {
			_dueñoID.addKamasBanco(_kamasVenta + kamasCofre);
			Personaje tempPerso = _dueñoID.getCuenta().getTempPersonaje();
			if (tempPerso != null) {
				GestorSalida.ENVIAR_M1_MENSAJE_SERVER_SVR_MUESTRA_INSTANTANEO(tempPerso, 5, _kamasVenta + ";" + perso
				.getNombre(), "");
			} else {
				_dueñoID.getCuenta().addMensaje("M15|" + _kamasVenta + ";" + perso.getNombre() + "|", true);
			}
		} catch (final Exception e) {}
		_dueñoID = perso;
		_kamasVenta = 0;
		_clave = "-";
		_actParametros = false;
		_gremio = null;
		actualizarDerechos(0);
		cerrarVentanaCompra(perso);
		GestorSalida.ENVIAR_hL_INFO_CASA(perso, informacionCasa(perso.getID()));
		for (final Personaje p : Mundo.getMapa(_mapaIDFuera).getArrayPersonajes()) {
			GestorSalida.ENVIAR_hP_PROPIEDADES_CASA(p, propiedadesPuertaCasa(p));
		}
	}
	
	public void modificarPrecioVenta(final Personaje perso, final String packet) {
		if (esSuCasa(perso.getID())) {
			final int precio = Integer.parseInt(packet);
			if (precio < 0) {
				GestorSalida.ENVIAR_BN_NADA(perso);
				return;
			}
			_kamasVenta = precio;
			GestorSalida.ENVIAR_hV_CERRAR_VENTANA_COMPRA_CASA(perso);
			GestorSalida.ENVIAR_hSK_FIJAR_PRECIO_CASA(perso, _id + "|" + _kamasVenta);
			for (final Personaje p : Mundo.getMapa(_mapaIDFuera).getArrayPersonajes()) {
				GestorSalida.ENVIAR_hP_PROPIEDADES_CASA(p, propiedadesPuertaCasa(p));
			}
			GestorSalida.ENVIAR_hL_INFO_CASA(perso, informacionCasa(perso.getID()));
		}
	}
	
	public void abrirVentanaCompraVentaCasa(final Personaje perso) {
		perso.setConsultarCasa(this);
		GestorSalida.ENVIAR_hCK_VENTANA_COMPRA_VENTA_CASA(perso, _id + "|" + _kamasVenta);
	}
	
	public void cerrarVentanaCompra(final Personaje perso) {
		GestorSalida.ENVIAR_hV_CERRAR_VENTANA_COMPRA_CASA(perso);
		perso.setConsultarCasa(null);
	}
	
	public void analizarCasaGremio(final Personaje perso, String packet) {
		if (!esSuCasa(perso.getID())) {
			GestorSalida.ENVIAR_BN_NADA(perso);
			return;
		}
		try {
			switch (packet) {
				case "+" :
					Gremio gremio = _dueñoID.getGremio();
					if (gremio == null) {
						return;
					}
					if (Mundo.cantCasasGremio(gremio.getID()) >= (byte) Math.ceil(gremio.getNivel() / 10f)) {
						GestorSalida.ENVIAR_Im_INFORMACION(perso, "1151");
						return;
					} else if (gremio.getCantidadMiembros() < 10) {
						GestorSalida.ENVIAR_Im_INFORMACION(perso, "1NOT_ENOUGHT_MEMBERS_IN_GUILD");
						return;
					}
					_gremio = gremio;
					_actParametros = true;
					break;
				case "-" :
				case "0" :
					_gremio = null;
					_actParametros = false;
					break;
				case "" :
					break;
				default :
					try {
						actualizarDerechos(Integer.parseInt(packet));
					} catch (Exception e) {}
					break;
			}
			GestorSalida.ENVIAR_hG_DERECHOS_GREMIO_CASA(perso, _id + (_actParametros && _gremio != null
			? (";" + _gremio.getNombre() + ";" + _gremio.getEmblema() + ";" + _derechosGremio)
			: ""));
		} catch (Exception e) {
			GestorSalida.ENVIAR_BN_NADA(perso, "EXCEPTION ANALIZAR CASA GREMIO");
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", analizarCasaGremio " + e.toString());
			e.printStackTrace();
		}
	}
	
	public void actualizarDerechos(int derechos) {
		_derechosGremio = derechos;
		analizarDerechos(_derechosGremio);
	}
	
	// poner el mensaje de condicin cuando el mob tiene condicino
	// probar los canales en reconeccion
	// poner finalize al map q se crea para la pelea
	//
	public String propiedadesPuertaCasa(Personaje perso) {
		final StringBuilder packet = new StringBuilder(_id + "|");
		try {
			packet.append(_dueñoID.getNombre());
		} catch (final Exception e) {}
		packet.append(";" + (_kamasVenta > 0 ? 1 : 0));
		boolean esDelGremio = perso.getGremio() != null && perso.getGremio().getID() == getGremioID();
		if (_gremio != null) {
			if (_gremio.getCantidadMiembros() < 10) {
				_gremio = null;
			} else if (tieneDerecho(Constantes.C_ESCUDO_VISIBLE_PARA_TODOS) || (tieneDerecho(
			Constantes.C_ESCUDO_VISIBLE_MIEMBROS) && esDelGremio)) {
				packet.append(";" + _gremio.getNombre() + ";" + _gremio.getEmblema());
			}
		}
		return packet.toString();
	}
	
	public String informacionCasa(int id) {
		return (esSuCasa(id) ? "+" : "-") + "|" + _id + ";" + (_clave.equals("-") ? 0 : 1) + ";" + (_kamasVenta > 0 ? 1 : 0)
		+ ";" + (_derechosGremio > 0 ? 1 : 0);
	}
	
	public static String stringCasaGremio(final int gremioID) {
		final StringBuilder packet = new StringBuilder();
		for (final Casa casa : Mundo.getCasas().values()) {
			if (casa.getGremioID() == gremioID && casa.getDerechosGremio() > 0) {
				if (packet.length() > 0) {
					packet.append("|");
				}
				packet.append(casa._id + ";");
				try {
					packet.append(casa.getDueño().getNombre() + ";");
				} catch (final Exception e) {
					packet.append("?;");
				}
				final Mapa mapa = Mundo.getMapa(casa.getMapaIDDentro());
				packet.append(mapa.getX() + "," + mapa.getY() + ";");
				packet.append("0;");
				packet.append(casa.getDerechosGremio());
			}
		}
		if (packet.length() == 0) {
			return "";
		}
		return "+" + packet.toString();
	}
}