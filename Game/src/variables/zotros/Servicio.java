package variables.zotros;

import java.util.regex.Pattern;
import variables.gremio.Gremio;
import variables.objeto.Objeto;
import variables.objeto.ObjetoModelo.CAPACIDAD_STATS;
import variables.personaje.Personaje;
import estaticos.MainServidor;
import estaticos.Constantes;
import estaticos.Encriptador;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.Mundo;

public class Servicio {
	private int _id, _creditosSinAbono, _ogrinasSinAbono, _creditosAbonado, _ogrinasAbonado;
	private boolean _activado, _biPagoSinAbono, _biPagoAbonado;
	
	public Servicio(int id, int creditos, int ogrinas, boolean activado, int creditosVIP, int ogrinasVIP) {
		_id = id;
		_activado = activado;
		_creditosSinAbono = creditos;
		_ogrinasSinAbono = ogrinas;
		_creditosAbonado = creditosVIP;
		_ogrinasAbonado = ogrinasVIP;
		_biPagoSinAbono = _creditosSinAbono > 0 && _ogrinasSinAbono > 0;
		_biPagoAbonado = _creditosAbonado > 0 && _ogrinasAbonado > 0;
	}
	
	public int getID() {
		return _id;
	}
	
	public boolean estaActivo() {
		return _activado;
	}
	
	public String string(boolean abonado) {
		if (!_activado) {
			return "";
		}
		if (abonado) {
			return _id + ";" + _creditosAbonado + ";" + _ogrinasAbonado;
		} else {
			return _id + ";" + _creditosSinAbono + ";" + _ogrinasSinAbono;
		}
	}
	
	private boolean puede(Personaje _perso) {
		if (!_activado) {
			return false;
		}
		if (_perso.getCuenta().esAbonado()) {
			if (_biPagoAbonado) {
				switch (_perso.getMedioPagoServicio()) {
					case 1 :
						if (!GestorSQL.RESTAR_CREDITOS(_perso.getCuenta(), _creditosAbonado, _perso)) {
							return false;
						}
						return true;
					case 2 :
						if (!GestorSQL.RESTAR_OGRINAS(_perso.getCuenta(), _ogrinasAbonado, _perso)) {
							return false;
						}
						return true;
				}
			}
			if (_creditosAbonado > 0) {
				if (!GestorSQL.RESTAR_CREDITOS(_perso.getCuenta(), _creditosAbonado, _perso)) {
					return false;
				}
			} else if (_ogrinasAbonado > 0) {
				if (!GestorSQL.RESTAR_OGRINAS(_perso.getCuenta(), _ogrinasAbonado, _perso)) {
					return false;
				}
			}
		} else {
			if (_biPagoSinAbono) {
				switch (_perso.getMedioPagoServicio()) {
					case 1 :
						if (!GestorSQL.RESTAR_CREDITOS(_perso.getCuenta(), _creditosSinAbono, _perso)) {
							return false;
						}
						return true;
					case 2 :
						if (!GestorSQL.RESTAR_OGRINAS(_perso.getCuenta(), _ogrinasSinAbono, _perso)) {
							return false;
						}
						return true;
				}
			}
			if (_creditosSinAbono > 0) {
				if (!GestorSQL.RESTAR_CREDITOS(_perso.getCuenta(), _creditosSinAbono, _perso)) {
					return false;
				}
			} else if (_ogrinasSinAbono > 0) {
				if (!GestorSQL.RESTAR_OGRINAS(_perso.getCuenta(), _ogrinasSinAbono, _perso)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public void usarServicio(Personaje _perso, String packet) {
		if (!_activado) {
			return;
		}
		try {
			switch (_id) {
				case Constantes.SERVICIO_CAMBIO_NOMBRE :
					if (!_perso.estaDisponible(false, true)) {
						return;
					}
					if (!packet.isEmpty()) {
						String[] params = packet.substring(3).split(";");
						String nombre = params[0];
						int colorN = 0;
						try {
							colorN = Integer.parseInt(params[1]);
							if (colorN > 16777215) {
								colorN = 0;
							}
						} catch (final Exception e) {
							return;
						}
						if (nombre.equalsIgnoreCase(_perso.getNombre())) {
							// si tiene el mismo nombre y diferente color
							if (colorN == _perso.getColorNombre()) {
								return;
							}
						}
						nombre = Personaje.nombreValido(nombre, false);
						if (nombre == null) {
							GestorSalida.ENVIAR_AAE_ERROR_CREAR_PJ(_perso, "a");
							return;
						}
						if (nombre.isEmpty()) {
							GestorSalida.ENVIAR_AAE_ERROR_CREAR_PJ(_perso, "n");
							return;
						}
						if (!puede(_perso)) {
							return;
						}
						_perso.setColorNombre(colorN);
						_perso.cambiarNombre(nombre);
					} else {
						GestorSalida.enviar(_perso, "bN" + _perso.getColorNombre());
					}
					break;
				case Constantes.SERVICIO_CAMBIO_COLOR :
					if (!_perso.estaDisponible(false, true)) {
						return;
					}
					if (!packet.isEmpty()) {
						if (!puede(_perso)) {
							return;
						}
						final String[] colores = packet.substring(3).split(";");
						_perso.setColores(Integer.parseInt(colores[0]), Integer.parseInt(colores[1]), Integer.parseInt(colores[2]));
						_perso.refrescarEnMapa();
						GestorSalida.ENVIAR_bV_CERRAR_PANEL(_perso);
					} else {
						GestorSalida.enviar(_perso, "bC");
					}
					break;
				case Constantes.SERVICIO_CAMBIO_SEXO :
					if (!_perso.estaDisponible(false, true)) {
						return;
					}
					if (!puede(_perso)) {
						return;
					}
					_perso.cambiarSexo();
					_perso.deformar();
					_perso.refrescarEnMapa();
					GestorSQL.CAMBIAR_SEXO_CLASE(_perso);
					GestorSalida.ENVIAR_bV_CERRAR_PANEL(_perso);
					break;
				case Constantes.SERVICIO_REVIVIR :
					if (_perso.getPelea() != null) {
						return;
					}
					if (!puede(_perso)) {
						return;
					}
					_perso.revivir(true);
					GestorSalida.ENVIAR_bV_CERRAR_PANEL(_perso);
					break;
				case Constantes.SERVICIO_TITULO_PERSONALIZADO :
					if (!packet.isEmpty()) {
						if (packet.substring(3).isEmpty()) {
							_perso.setTituloVIP("");
						} else {
							final String[] str = packet.substring(3).split(";");
							final String titulo = str[0];
							int colorT = 0;
							try {
								colorT = Integer.parseInt(str[1]);
								if (colorT > 16777215) {
									colorT = 0;
								}
							} catch (final Exception e) {
								return;
							}
							if (titulo.isEmpty() || titulo.length() > 25) {
								return;
							}
							final String plantilla = Encriptador.NUMEROS + Encriptador.ABC_MIN + Encriptador.ABC_MAY
							+ Encriptador.ESPACIO + Encriptador.GUIONES;
							for (final char letra : titulo.toCharArray()) {
								if (!plantilla.contains(letra + "")) {
									return;
								}
							}
							if (!puede(_perso)) {
								return;
							}
							_perso.setTituloVIP(titulo + "*" + colorT);
						}
						_perso.refrescarEnMapa();
						GestorSalida.ENVIAR_bV_CERRAR_PANEL(_perso);
					} else {
						GestorSalida.enviar(_perso, "b—");
					}
					break;
				case Constantes.SERVICIO_MIMOBIONTE :
					if (!packet.isEmpty()) {
						String[] split = packet.substring(3).split(Pattern.quote("|"));
						Objeto huesped = _perso.getObjeto(Integer.parseInt(split[0]));
						Objeto mascara = _perso.getObjeto(Integer.parseInt(split[1]));
						if (huesped == null || mascara == null) {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1OBJECT_DONT_EXIST");
							return;
						}
						if (huesped.getObjevivoID() != 0 || mascara.getObjevivoID() != 0) {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1MIMOBIONTE_ERROR_TYPES");
							return;
						}
						if (huesped.getID() == mascara.getID()) {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1MIMOBIONTE_ERROR_IDS");
							return;
						}
						int[] tipos = {Constantes.OBJETO_TIPO_AMULETO, Constantes.OBJETO_TIPO_ARCO, Constantes.OBJETO_TIPO_VARITA,
						Constantes.OBJETO_TIPO_BASTON, Constantes.OBJETO_TIPO_DAGAS, Constantes.OBJETO_TIPO_ESPADA,
						Constantes.OBJETO_TIPO_MARTILLO, Constantes.OBJETO_TIPO_PALA, Constantes.OBJETO_TIPO_ANILLO,
						Constantes.OBJETO_TIPO_CINTURON, Constantes.OBJETO_TIPO_BOTAS, Constantes.OBJETO_TIPO_SOMBRERO,
						Constantes.OBJETO_TIPO_CAPA, Constantes.OBJETO_TIPO_MASCOTA, Constantes.OBJETO_TIPO_HACHA,
						Constantes.OBJETO_TIPO_PICO, Constantes.OBJETO_TIPO_GUADA—A, Constantes.OBJETO_TIPO_MOCHILA,
						Constantes.OBJETO_TIPO_ESCUDO};
						boolean esTipo = false;
						for (int t : tipos) {
							if (t == huesped.getObjModelo().getTipo()) {
								esTipo = true;
								break;
							}
						}
						if (!esTipo || huesped.getObjModelo().getTipo() != mascara.getObjModelo().getTipo()) {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1MIMOBIONTE_ERROR_TYPES");
							return;
						}
						if (huesped.getObjModelo().getNivel() < mascara.getObjModelo().getNivel()) {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1MIMOBIONTE_ERROR_LEVELS");
							return;
						}
						if (MainServidor.ID_MIMOBIONTE != -1) {
							if (!_perso.tenerYEliminarObjPorModYCant(MainServidor.ID_MIMOBIONTE, 1)) {
								GestorSalida.ENVIAR_Im_INFORMACION(_perso, "14|43");
								return;
							}
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "022;" + 1 + "~" + MainServidor.ID_MIMOBIONTE);
						} else if (!puede(_perso)) {
							GestorSalida.ENVIAR_BN_NADA(_perso, "MIMOBIONTE DESHABILITADO");
							return;
						}
						if (!_perso.restarCantObjOEliminar(mascara.getID(), 1, true)) {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "14|43");
							return;
						}
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "022;" + 1 + "~" + mascara.getObjModeloID());
						int nuevaCantidad = huesped.getCantidad() - 1;
						if (nuevaCantidad >= 1) {
							Objeto nuevo = huesped.clonarObjeto(nuevaCantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
							_perso.addObjetoConOAKO(nuevo, true);
							huesped.setCantidad(1);
							GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_perso, huesped);
						}
						huesped.addStatTexto(Constantes.STAT_APARIENCIA_OBJETO, "0#0#" + Integer.toHexString(mascara
						.getObjModeloID()));
						GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(_perso, huesped);
					} else {
						GestorSalida.enviar(_perso, "bM");
					}
					break;
				case Constantes.SERVICIO_CREA_TU_ITEM :
					GestorSalida.ENVIAR_bB_PANEL_CREAR_ITEM(_perso);
					break;
				case Constantes.SERVICIO_SISTEMA_ITEMS :
					GestorSalida.ENVIAR_bSP_PANEL_ITEMS(_perso);
					break;
				case Constantes.SERVICIO_CAMBIO_EMBLEMA :
					if (!_perso.estaDisponible(false, true)) {
						return;
					}
					if (_perso.getMiembroGremio() == null || _perso.getMiembroGremio().getRango() != 1) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1YOU_DONT_HAVE_GUILD");
						return;
					}
					if (!packet.isEmpty()) {
						final String[] infos = packet.substring(3).split(Pattern.quote("|"));
						final String escudoID = Integer.toString(Integer.parseInt(infos[0]), 36);
						final String colorEscudo = Integer.toString(Integer.parseInt(infos[1]), 36);
						final String emblemaID = Integer.toString(Integer.parseInt(infos[2]), 36);
						final String colorEmblema = Integer.toString(Integer.parseInt(infos[3]), 36);
						String nombreGremio = infos[4].substring(0, 1).toUpperCase() + infos[4].substring(1).toLowerCase();
						if (nombreGremio.length() < 2 || nombreGremio.length() > 20 || (!_perso.getGremio().getNombre()
						.equalsIgnoreCase(nombreGremio) && Mundo.nombreGremioUsado(nombreGremio))) {
							GestorSalida.ENVIAR_gC_CREAR_PANEL_GREMIO(_perso, "Ean");
							return;
						}
						boolean esValido = true;
						final String abcMin = "abcdefghijklmnopqrstuvwxyz- '";
						byte cantSimbol = 0, cantLetras = 0;
						char letra_A = ' ', letra_B = ' ';
						for (final char letra : nombreGremio.toLowerCase().toCharArray()) {
							if (!abcMin.contains(letra + "")) {
								esValido = false;
								break;
							}
							if (letra == letra_A && letra == letra_B) {
								esValido = false;
								break;
							}
							if (letra != '-') {
								letra_A = letra_B;
								letra_B = letra;
								cantLetras++;
							} else {
								if (cantLetras == 0 || cantSimbol > 0) {
									esValido = false;
									break;
								}
								cantSimbol++;
							}
						}
						if (!esValido) {
							GestorSalida.ENVIAR_gC_CREAR_PANEL_GREMIO(_perso, "Ean");
							return;
						}
						final String emblema = escudoID + "," + colorEscudo + "," + emblemaID + "," + colorEmblema;
						if (Mundo.emblemaGremioUsado(emblema)) {
							GestorSalida.ENVIAR_gC_CREAR_PANEL_GREMIO(_perso, "Eae");
							return;
						}
						if (!puede(_perso)) {
							return;
						}
						Gremio gremio = _perso.getGremio();
						gremio.setNombre(nombreGremio);
						gremio.setEmblema(emblema);
						if (_perso.getPelea() == null) {
							GestorSalida.ENVIAR_Oa_CAMBIAR_ROPA_MAPA(_perso.getMapa(), _perso);
						}
						_perso.refrescarEnMapa();
						GestorSalida.ENVIAR_bV_CERRAR_PANEL(_perso);
					} else {
						GestorSalida.enviar(_perso, "bG");
					}
					break;
				case Constantes.SERVICIO_ESCOGER_NIVEL :
					if (!_perso.estaDisponible(false, true)) {
						return;
					}
					if (!packet.isEmpty()) {
						if (!puede(_perso)) {
							return;
						}
						String[] split = packet.substring(2).split(Pattern.quote("|"));
						int nivel = Integer.parseInt(split[0]);
						byte alineacion = Byte.parseByte(split[1]);
						_perso.cambiarNivelYAlineacion(nivel, alineacion);
					} else {
						GestorSalida.ENVIAR_bA_ESCOGER_NIVEL(_perso);
					}
					break;
				case Constantes.SERVICIO_TRANSFORMAR_MONTURA :
					if (!_perso.estaDisponible(false, true)) {
						return;
					}
					if (_perso.getMontura() == null) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1104");
						return;
					}
					if (!packet.isEmpty()) {
						if (!puede(_perso)) {
							return;
						}
						String statsMontura = _perso.getMontura().getStats().convertirStatsAString();
						Objeto mascota = Mundo.getObjetoModelo(Integer.parseInt(packet.substring(3))).crearObjeto(1,
						Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.MAXIMO);
						mascota.convertirStringAStats(statsMontura);
						_perso.addObjetoConOAKO(mascota, true);
						if (_perso.estaMontando()) {
							_perso.subirBajarMontura(false);
						}
						Mundo.eliminarMontura(_perso.getMontura());
						_perso.setMontura(null);
					} else {
						GestorSalida.ENVIAR_bm_TRANSFORMAR_MONTURA(_perso);
					}
					break;
				case Constantes.SERVICIO_ALINEACION_MERCENARIO :
					if (!_perso.estaDisponible(false, true)) {
						return;
					}
					if (_perso.getAlineacion() == Constantes.ALINEACION_MERCENARIO) {
						return;
					}
					if (_perso.getDeshonor() >= 2) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "183");
						return;
					}
					if (!puede(_perso)) {
						return;
					}
					_perso.cambiarAlineacion(Constantes.ALINEACION_MERCENARIO, false);
					break;
				case Constantes.SERVICIO_MONTURA_CAMALEON :
					if (!_perso.estaDisponible(false, true)) {
						return;
					}
					if (_perso.getMontura() == null) {
						return;
					}
					if (!puede(_perso)) {
						return;
					}
					_perso.getMontura().addHabilidad(Constantes.HABILIDAD_CAMALEON);
					GestorSalida.ENVIAR_Re_DETALLES_MONTURA(_perso, "+", _perso.getMontura());
					GestorSQL.REPLACE_MONTURA(_perso.getMontura(), false);
					break;
				case Constantes.SERVICIO_ABONO_DIA :
				case Constantes.SERVICIO_ABONO_SEMANA :
				case Constantes.SERVICIO_ABONO_MES :
				case Constantes.SERVICIO_ABONO_TRES_MESES :
					if (!puede(_perso)) {
						return;
					}
					int dias = 1;
					switch (_id) {
						case Constantes.SERVICIO_ABONO_DIA :
							dias = 1;
							break;
						case Constantes.SERVICIO_ABONO_SEMANA :
							dias = 7;
							break;
						case Constantes.SERVICIO_ABONO_MES :
							dias = 30;
							break;
						case Constantes.SERVICIO_ABONO_TRES_MESES :
							dias = 90;
							break;
						default :
							return;
					}
					long abonoD = Math.max(GestorSQL.GET_ABONO(_perso.getCuenta().getNombre()), System.currentTimeMillis());
					abonoD += (dias * 24 * 3600 * 1000l);
					abonoD = Math.max(abonoD, System.currentTimeMillis() - 1000);
					GestorSQL.SET_ABONO(abonoD, _perso.getCuentaID());
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1NUEVO_ABONO;" + dias);
					break;
				case 101 :
				case 102 :
				case 103 :
				case 104 :
				case 105 :
				case 106 :
				case 107 :
				case 108 :
				case 109 :
				case 110 :
				case 111 :
				case 112 :
					if (!_perso.estaDisponible(false, true)) {
						return;
					}
					if (_perso.getEncarnacionN() != null) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1NO_PUEDES_CAMBIAR_CLASE");
						return;
					}
					if (!puede(_perso)) {
						return;
					}
					final byte clase = (byte) (_id - 100);
					_perso.cambiarClase(clase);
					GestorSalida.ENVIAR_bV_CERRAR_PANEL(_perso);
					break;
			}
		} catch (Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "EXCEPTION USAR SERVICIO");
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", usarServicios " + e.toString());
			e.printStackTrace();
		}
	}
}
