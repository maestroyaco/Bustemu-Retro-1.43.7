package estaticos;

import java.util.ArrayList;
import java.util.Collection;
import servidor.ServidorServer;
import servidor.ServidorSocket;
import sprites.Exchanger;
import variables.casa.Cofre;
import variables.gremio.Gremio;
import variables.gremio.MiembroGremio;
import variables.hechizo.EfectoHechizo;
import variables.mapa.Celda;
import variables.mapa.Cercado;
import variables.mapa.Mapa;
import variables.montura.Montura;
import variables.objeto.Objeto;
import variables.objeto.ObjetoModelo;
import variables.objeto.ObjetoSet;
import variables.oficio.StatOficio;
import variables.pelea.Luchador;
import variables.pelea.Pelea;
import variables.personaje.Cuenta;
import variables.personaje.Grupo;
import variables.personaje.GrupoKoliseo;
import variables.personaje.Personaje;
import variables.stats.TotalStats;

public class GestorSalida {
	public static void enviarEnCola(Personaje perso, final String packet, boolean redactar) {
		try {
			if (perso == null) {
				return;
			}
			if (perso.esMultiman()) {
				perso = perso.getCompañero();
				if (perso == null) {
					return;
				}
			}
			if (perso.enLinea()) {
				if (perso.getCargandoMapa()) {
					perso.addPacketCola(packet);
				} else {
					perso.getServidorSocket().enviarPW(packet, redactar, true);
				}
			}
		} catch (final Exception e) {}
	}
	
	public static void enviar(Personaje perso, final String packet) {
		try {
			if (perso == null) {
				return;
			}
			if (perso.esMultiman()) {
				perso = perso.getCompañero();
			}
			if (perso.enLinea()) {
				perso.getServidorSocket().enviarPW(packet, true, true);
			}
		} catch (final Exception e) {}
	}
	
	public static void enviarTodos(int tiempo, final String packet) {
		tiempo = Math.max(tiempo, 1);
		for (final ServidorSocket ep : ServidorServer.getClientes()) {
			try {
				Thread.sleep(tiempo);
				if (ep != null) {
					ep.enviarPW(packet);
				}
			} catch (Exception e) {}
		}
	}
	
	private static void imprimir(String pre, String packet) {
		if (MainServidor.MOSTRAR_ENVIOS) {
			System.out.println(pre + ">> " + packet);
		}
	}
	
	public static void ENVIAR_pong(final Personaje perso) {
		final String packet = "pong";
		enviarEnCola(perso, packet, true);
		imprimir("DOFUS PONG: PERSO", packet);
	}
	
	public static void ENVIAR_qpong(final Personaje ss) {
		final String packet = "qpong";
		enviarEnCola(ss, packet, true);
		imprimir("DOFUS QPONG: PERSO", packet);
	}
	
	public static void ENVIAR_HG_SALUDO_JUEGO_GENERAL(final ServidorSocket ss) {
		final String packet = "HG";
		ss.enviarPW(packet);
		imprimir("SALUDO JUEGO: OUT", packet);
	}
	
	public static void ENVIAR_XML_POLICY_FILE(final ServidorSocket ss) {
		final String packet = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"//
		+ "<cross-domain-policy>" + "<site-control permitted-cross-domain-policies=\"all\" />"
		+ "<allow-access-from domain=\"*\" to-ports=\"*\" secure=\"false\" />"
		+ "<allow-http-request-headers-from domain=\"*\" headers=\"*\" secure=\"false\"/>" // s
		+ "</cross-domain-policy>";
		ss.enviarPW(packet);
		imprimir("POLICY FILE: OUT", packet);
	}
	
	public static void ENVIAR_APK_NOMBRE_PJ_ALEATORIO(final ServidorSocket ss, String nombre) {
		final String packet = "APK" + nombre;
		ss.enviarPW(packet);
		imprimir("NOMBRE PJ ALEATORIO: OUT", packet);
	}
	
	public static void ENVIAR_AlEk_CUENTA_BANEADA_TIEMPO(final ServidorSocket ss, long tiempo) {
		String packet = "AlEk";
		tiempo -= System.currentTimeMillis();
		final int dia = (int) (tiempo / (1000 * 3600 * 24));
		tiempo %= 1000 * 3600 * 24;
		final int horas = (int) (tiempo / (1000 * 3600));
		tiempo %= 1000 * 3600;
		final int min = (int) (tiempo / (1000 * 60));
		packet += dia + "|" + horas + "|" + min;
		ss.enviarPW(packet);
		imprimir("CUENTA BANEADA TIEMPO: CONEXION", packet);
	}
	
	public static void ENVIAR_AlEb_CUENTA_BANEADA_DEFINITIVO(final ServidorSocket ss) {
		final String packet = "AlEb";
		ss.enviarPW(packet);
		imprimir("CUENTA BANEADA DEFINITIVA: CONEXION", packet);
	}
	
	public static void ENVIAR_AlEw_MUCHOS_JUG_ONLINE(final ServidorSocket ss) {
		final String packet = "AlEw";
		ss.enviarPW(packet);
		imprimir("MAX JUG ONLINE: CONEXION", packet);
	}
	
	public static void ENVIAR_AlEx_NOMBRE_O_PASS_INCORRECTA(final ServidorSocket ss) {
		final String packet = "AlEx";
		ss.enviarPW(packet);
		imprimir("LOGIN ERROR: CONEXION", packet);
	}
	
	public static void ENVIAR_AlEd_DESCONECTAR_CUENTA_CONECTADA(final ServidorSocket ss) {
		final String packet = "AlEd";
		ss.enviarPW(packet);
		imprimir("MISMA CUENTA CONECTADA: CONEXION", packet);
	}
	
	public static void ENVIAR_AlEr_CAMBIAR_NOMBRE(final ServidorSocket ss) {
		final String packet = "AlEr";
		ss.enviarPW(packet);
		imprimir("CAMBIAR NOMBRE: PERSO", packet);
	}
	
	public static void ENVIAR_AlEr_CAMBIAR_NOMBRE(final Personaje ss) {
		final String packet = "AlEr";
		enviarEnCola(ss, packet, true);
		imprimir("CAMBIAR NOMBRE: PERSO", packet);
	}
	
	public static void ENVIAR_AlEm_SERVER_MANTENIMIENTO(final ServidorSocket ss) {
		final String packet = "AlEm";
		ss.enviarPW(packet);
		imprimir("SERVER MANTENIMIENTO: CONEXION", packet);
	}
	
	public static void ENVIAR_Af_ABONADOS_POSCOLA(final ServidorSocket ss, final int posicion, final int totalAbo,
	final int totalNonAbo, final String subscribe, final int colaID) {
		final String packet = "Af" + posicion + "|" + totalAbo + "|" + totalNonAbo + "|" + subscribe + "|" + colaID;
		ss.enviarPW(packet);
		imprimir("MULTIPAQUETES: CONEXION", packet);
	}
	
	public static void ENVIAR_AN_MENSAJE_NUEVO_NIVEL(final Personaje perso, final int nivel) {
		final String packet = "AN" + nivel;
		enviarEnCola(perso, packet, true);
		imprimir("SUBIO NIVEL: PERSO", packet);
	}
	
	public static void ENVIAR_ATK_TICKET_A_CUENTA(final ServidorSocket ss, String key) {
		final String packet = "ATK" + key;
		ss.enviarPWSinEncriptar(packet);
		imprimir("TICKET A CUENTA: OUT", packet);
	}
	
	public static void ENVIAR_AK_KEY_ENCRIPTACION_PACKETS(final ServidorSocket ss, String key) {
		final String packet = "AK" + key;
		ss.enviarPWSinEncriptar(packet);
		imprimir("KEY ENCRIPTACION: OUT", packet);
	}
	
	public static void ENVIAR_ATE_TICKET_FALLIDA(final ServidorSocket ss) {
		final String packet = "ATE";
		ss.enviarPW(packet);
		imprimir("TICKET FALLIDA: OUT", packet);
	}
	
	public static void ENVIAR_AV_VERSION_REGIONAL(final ServidorSocket ss) {
		final String packet = "AV0";
		ss.enviarPW(packet);
		imprimir("VERSION DE REGION: OUT", packet);
	}
	
	public static void ENVIAR_APE2_GENERAR_NOMBRE_RANDOM(final ServidorSocket ss) {
		final String packet = "APE2";
		ss.enviarPW(packet);
		imprimir("GENERAR NOMBRE: PERSO", packet);
	}
	
	public static void ENVIAR_ALK_LISTA_DE_PERSONAJES(final Personaje ss, final Cuenta cuenta) {
		String packet = ("ALK" + cuenta.getTiempoAbono() + "|" + cuenta.getPersonajes().size());
		for (final Personaje perso : cuenta.getPersonajes()) {
			packet += (perso.stringParaListaPJsServer());
		}
		enviarEnCola(ss, packet, true);
		imprimir("LISTA DE PJS: OUT", packet);
	}
	
	public static void ENVIAR_ALK_LISTA_DE_PERSONAJES(final ServidorSocket ss, final Cuenta cuenta) {
		String packet = ("ALK" + cuenta.getTiempoAbono() + "|" + cuenta.getPersonajes().size());
		for (final Personaje perso : cuenta.getPersonajes()) {
			packet += (perso.stringParaListaPJsServer());
		}
		ss.enviarPW(packet);
		imprimir("LISTA DE PJS: OUT", packet);
	}
	
	public static void ENVIAR_Ag_LISTA_REGALOS(final ServidorSocket ss, final int idObjeto, final String codObjeto) {
		final String packet = "Ag1|" + idObjeto + "|Regalo " + MainServidor.NOMBRE_SERVER + "|SIN RELLENO|SIN FOTO|"
		+ codObjeto;
		// packet = "Ag"+idObjeto+"|" + idObjeto + "|Regalo " + Bustemu.NOMBRE_SERVER +
		// "|SIN RELLENO|SIN
		// FOTO|"+"1~2411~1~~3cc#0#0#1,3cb#0#0#1,3cd#0#0#11,3ca#0#0#0,3ce#0#0#0;111~2412~2~~76#2#0#0#0d0+2;22~100~20~~7d#4#0#0#0d0+4,77#4#0#0#0d0+4";
		ss.enviarPW(packet);
		imprimir("LISTA REGALOS: PERSO", packet);
	}
	
	public static void ENVIAR_AG_SIGUIENTE_REGALO(final ServidorSocket ss) {
		final String packet = "AGK";
		ss.enviarPW(packet);
		imprimir("SIGUIENTE REGALO: PERSO", packet);
	}
	
	public static void ENVIAR_AAE_ERROR_CREAR_PJ(final ServidorSocket ss, final String letra) {
		final String packet = "AAE" + letra;
		ss.enviarPW(packet);
		imprimir("ERROR CREAR PJ: OUT", packet);
	}
	
	public static void ENVIAR_AAE_ERROR_CREAR_PJ(final Personaje perso, final String letra) {
		final String packet = "AAE" + letra;
		enviarEnCola(perso, packet, true);
		imprimir("ERROR CREAR PJ: OUT", packet);
	}
	
	public static void ENVIAR_AAK_CREACION_PJ_OK(final ServidorSocket ss) {
		final String packet = "AAK";
		ss.enviarPW(packet);
		imprimir("CREAR PJ OK: PERSO", packet);
	}
	
	public static void ENVIAR_ADE_ERROR_BORRAR_PJ(final ServidorSocket ss) {
		final String packet = "ADE";
		ss.enviarPW(packet);
		imprimir("ERROR BORRAR PJ: OUT", packet);
	}
	
	public static void ENVIAR_ASE_SELECCION_PERSONAJE_FALLIDA(final ServidorSocket ss) {
		final String packet = "ASE";
		ss.enviarPW(packet);
		imprimir("ERROR SELECCION PJ: OUT", packet);
	}
	
	public static void ENVIAR_ASK_PERSONAJE_SELECCIONADO(final Personaje perso) {
		final String packet = "ASK|" + perso.getID() + "|" + perso.getNombre() + "|" + perso.getNivel() + "|" + perso
		.getClaseID(false) + "|" + perso.getSexo() + "|" + perso.getGfxID(false) + "|" + (perso.getColor1() == -1
		? "-1"
		: Integer.toHexString(perso.getColor1())) + "|" + (perso.getColor2() == -1
		? "-1"
		: Integer.toHexString(perso.getColor2())) + "|" + (perso.getColor3() == -1
		? "-1"
		: Integer.toHexString(perso.getColor3())) + "|" + perso.strListaObjetos();
		enviarEnCola(perso, packet, true);
		imprimir("PERSONAJE SELECCIONADO: OUT", packet);
	}
	
	public static void ENVIAR_ASK_PERSONAJE_A_ESPIAR(final Personaje perso, Personaje espiador) {
		final String packet = "ASK|" + perso.getID() + "|" + perso.getNombre() + "|" + perso.getNivel() + "|" + perso
		.getClaseID(false) + "|" + perso.getSexo() + "|" + perso.getGfxID(false) + "|" + (perso.getColor1() == -1
		? "-1"
		: Integer.toHexString(perso.getColor1())) + "|" + (perso.getColor2() == -1
		? "-1"
		: Integer.toHexString(perso.getColor2())) + "|" + (perso.getColor3() == -1
		? "-1"
		: Integer.toHexString(perso.getColor3())) + "|" + perso.strListaObjetos();
		enviarEnCola(espiador, packet, false);
		imprimir("PERSONAJE SELECCIONADO: OUT", packet);
	}
	
	public static void ENVIAR_AR_RESTRICCIONES_PERSONAJE(final Personaje perso) {
		String packet = "AR" + perso.getRestriccionesA();
		enviarEnCola(perso, packet, true);
		imprimir("RESTRICCIONES: PERSO", packet);
	}
	
	public static void ENVIAR_al_ESTADO_ZONA_ALINEACION(final Personaje ss) {
		final String packet = "al|" + Mundo.getAlineacionTodasSubareas();
		enviarEnCola(ss, packet, false);
		imprimir("SUBAREAS ALINEACION: PERSO", packet);
	}
	
	public static void ENVIAR_am_CAMBIAR_ALINEACION_SUBAREA(final Personaje perso, final int subArea, byte nuevaAlin,
	boolean mensaje) {
		String packet = "am" + subArea + "|" + nuevaAlin + "|" + (mensaje ? 0 : 1);
		enviarEnCola(perso, packet, false);
		imprimir("MSJ ALIN SUBAREA: PERSO", packet);
	}
	
	public static void ENVIAR_aM_CAMBIAR_ALINEACION_AREA(final Personaje perso, final int area, byte alineacion) {
		final String packet = "aM" + area + "|" + alineacion;
		enviarEnCola(perso, packet, true);
		imprimir("MSJ ALIN AREA: PERSO", packet);
	}
	
	public static void ENVIAR_BD_FECHA_SERVER(final Personaje perso) {
		final String packet = ServidorServer.getFechaHoy();
		enviarEnCola(perso, packet, true);
		imprimir("FECHA SERVER: PERSO", packet);
	}
	
	public static void ENVIAR_BT_TIEMPO_SERVER(final Personaje perso) {
		final String packet = ServidorServer.getHoraHoy(perso);
		enviarEnCola(perso, packet, true);
		imprimir("TIEMPO SERVER: PERSO", packet);
	}
	
	public static void ENVIAR_BN_NADA(final Personaje perso) {
		final String packet = "BN";
		enviarEnCola(perso, packet, true);
		imprimir("NADA: PERSO", packet);
	}
	
	public static void ENVIAR_BN_NADA(final ServidorSocket ss) {
		final String packet = "BN ";
		ss.enviarPW(packet);
		imprimir("NADA: PERSO", packet);
	}
	
	public static void ENVIAR_BN_NADA(final ServidorSocket ss, String s) {
		final String packet = "BN " + s;
		ss.enviarPW(packet);
		imprimir("NADA: PERSO", packet);
	}
	
	public static void ENVIAR_BN_NADA(final Personaje perso, String s) {
		final String packet = "BN " + s;
		enviarEnCola(perso, packet, true);
		imprimir("NADA: PERSO", packet);
	}
	
	public static void ENVIAR_EHS_BUSCAR_OBJETO_MERCADILLO(final Personaje ss, final String str) {
		final String packet = "EHS+" + str;
		enviarEnCola(ss, packet, true);
		imprimir("BUSCAR OBJ MERCADILLO: PERSO", packet);
	}
	
	public static void ENVIAR_dV_CERRAR_DOCUMENTO(final Personaje ss) {
		final String packet = "dV";
		enviarEnCola(ss, packet, true);
		imprimir("CERRAR DOCUMENTO: PERSO", packet);
	}
	
	public static void ENVIAR_dC_ABRIR_DOCUMENTO(final Personaje ss, final String str) {
		final String packet = "dCK" + str;
		enviarEnCola(ss, packet, true);
		imprimir("ABRIR DOCUMENTO: PERSO", packet);
	}
	
	public static void ENVIAR_cMK_A_TODOS(final String es, String fr) {
		String packet = "cMK+|0|ELBUSTEMU|" + es;
		String packetFr = "cMK+|0|ELBUSTEMU|" + fr;
		for (final Personaje perso : Mundo.getPersonajesEnLinea()) {
			if (perso.getCuenta().getIdioma().equals("fr") && !fr.isEmpty()) {
				enviarEnCola(perso, packetFr, false);
			} else if (!es.isEmpty()) {
				enviarEnCola(perso, packet, false);
			}
		}
		imprimir("MENSAJE ROJO: TODOS", packet);
	}
	
	public static void ENVIAR_SLo_MOSTRAR_TODO_HECHIZOS(final Personaje ss, final boolean mostrar) {
		final String packet = "SLo" + (mostrar ? "+" : "-");
		enviarEnCola(ss, packet, true);
		imprimir("MOSTRAR MAS HECHIZOS: PERSO", packet);
	}
	
	public static void ENVIAR_FO_MOSTRAR_CONEXION_AMIGOS(final Personaje ss, final boolean mostrar) {
		final String packet = "FO" + (mostrar ? "+" : "-");
		enviarEnCola(ss, packet, true);
		imprimir("MOSTRAR AMIGOS CONEX: PERSO", packet);
	}
	
	public static void ENVIAR_GCK_CREAR_PANTALLA_PJ(final Personaje ss) {
		final String packet = "GCK|1";
		enviar(ss, packet);
		imprimir("CREAR PANTALLA: PERSO", packet);
	}
	
	public static void ENVIAR_GA2_CARGANDO_MAPA(final Personaje perso) {
		final String packet = "GA;2;";
		enviar(perso, packet);
		imprimir("CARGANDO MAPA: PERSO", packet);
	}
	
	public static void ENVIAR_GDM_CAMBIO_DE_MAPA(final Personaje perso, Mapa mapa) {  
	    perso.setCargandoMapa(true, null);  
	    perso.setMapaGDM(mapa);  
	    final String packet = "GDM|" + mapa.getID() + "|" + mapa.getFecha() + "|" + mapa.getKey();  
	    enviar(perso, packet);  
	    imprimir("CAMBIO MAPA: PERSO", packet);  
	}
	
	public static void ENVIAR_GDM_MAPDATA_COMPLETO(final Personaje perso) {
		perso.setCargandoMapa(true, null);
		Mapa mapa = perso.getMapaGDM();
		final String packet = "GDM|" + mapa.getID() + "|" + mapa.getFecha() + "||" + mapa.getAncho() + "|" + mapa.getAlto()
		+ "|" + mapa.getBgID() + "|" + mapa.getMusicID() + "|" + mapa.getAmbienteID() + "|" + mapa.getOutDoor() + "|" + mapa
		.getCapabilities() + "|" + mapa.getMapData() + "|1";
		enviar(perso, packet);
		imprimir("CAMBIO MAPA: PERSO", packet);
	}
	
	public static void ENVIAR_GDE_FRAME_OBJECT_EXTERNAL(final Personaje perso, final String str) {
		final String packet = "GDE|" + str;
		enviarEnCola(perso, packet, true);
		imprimir("FRAME OBJ EXT: PERSO", packet);
	}
	
	public static void ENVIAR_GDE_FRAME_OBJECT_EXTERNAL(final Mapa mapa, final String str) {
		final String packet = "GDE|" + str;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("FRAME OBJ EXT: MAPA", packet);
	}
	
	public static void ENVIAR_GDK_CARGAR_MAPA(final Personaje ss) {
		final String packet = "GDK";
		enviarEnCola(ss, packet, true);
		imprimir("CARGAR MAPA: PERSO", packet);
	}
	
	public static void ENVIAR_fL_LISTA_PELEAS_AL_MAPA(final Mapa mapa) {
		StringBuilder packet = new StringBuilder("fL");
		int peleas = 0;
		for (final Pelea pelea : mapa.getPeleas().values()) {
			if (peleas > 0) {
				packet.append("|");
			}
			try {
				final String info = pelea.strParaListaPelea();
				if (!info.isEmpty()) {
					packet.append(info);
					peleas++;
				}
			} catch (final Exception e) {}
		}
		for (Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet.toString(), true);
				enviarEnCola(pj, "fC" + peleas, true);
			}
		}
		imprimir("LISTA PELEAS: MAPA", packet.toString());
	}
	
	public static void ENVIAR_fL_LISTA_PELEAS(final Personaje ss, final Mapa mapa) {
		StringBuilder packet = new StringBuilder("fL");
		int peleas = 0;
		for (final Pelea pelea : mapa.getPeleas().values()) {
			if (peleas > 0) {
				packet.append("|");
			}
			try {
				final String info = pelea.strParaListaPelea();
				if (!info.isEmpty()) {
					packet.append(info);
					peleas++;
				}
			} catch (final Exception e) {}
		}
		enviarEnCola(ss, packet.toString(), true);
		enviarEnCola(ss, "fC" + peleas, true);
		imprimir("LISTA PELEAS: PERSO", packet.toString());
	}
	
	public static void ENVIAR_fC_CANTIDAD_DE_PELEAS(final Personaje ss, final Mapa mapa) {
		final String packet = "fC" + mapa.getNumeroPeleas();
		enviarEnCola(ss, packet, true);
		imprimir("CANTIDAD PELEAS: PERSO", packet);
	}
	
	public static void ENVIAR_fC_CANTIDAD_DE_PELEAS(final Mapa mapa) {
		final String packet = "fC" + mapa.getNumeroPeleas();
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("CANTIDAD PELEAS: MAPA", packet);
	}
	
	public static void ENVIAR_GJK_UNIRSE_PELEA(final Personaje perso, final int estado, final boolean botonCancelar,
	final boolean mostrarBotones, final boolean espectador, final long tiempo, final int tipoPelea) {
		final String packet = "GJK" + estado + "|" + (botonCancelar ? 1 : 0) + "|" + (mostrarBotones ? 1 : 0) + "|"
		+ (espectador ? 1 : 0) + "|" + tiempo + "|" + tipoPelea;
		enviarEnCola(perso, packet, true);
		imprimir("UNIRSE PELEA: PERSO", packet);
	}
	
	public static void ENVIAR_GJK_UNIRSE_PELEA(final Pelea pelea, final int equipos, final int estado,
	final boolean botonCancelar, final boolean mostrarBotones, final boolean espectador, final long tiempo,
	final int tipoPelea) {
		final String packet = "GJK" + estado + "|" + (botonCancelar ? 1 : 0) + "|" + (mostrarBotones ? 1 : 0) + "|"
		+ (espectador ? 1 : 0) + "|" + tiempo + "|" + tipoPelea;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("UNIRSE PELEA: PELEA", packet);
	}
	
	public static void ENVIAR_GP_POSICIONES_PELEA(final Personaje perso, final String posiciones, final int equipo) {
		final String packet = "GP" + posiciones + "|" + equipo;
		enviarEnCola(perso, packet, true);
		imprimir("POSICIONES PELEA: PERSO", packet);
	}
	
	public static void ENVIAR_GP_POSICIONES_PELEA(final Pelea pelea, final int equipos, final String posiciones,
	final int colorEquipo) {
		final String packet = "GP" + posiciones + "|" + colorEquipo;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("POSICIONES PELEA: PELEA", packet);
	}
	
	public static void ENVIAR_Gc_MOSTRAR_ESPADA_EN_MAPA(final Mapa mapa, final String packet) {
		for (final Personaje perso : mapa.getArrayPersonajes()) {
			if (perso.getPelea() == null) {
				enviarEnCola(perso, packet, true);
			}
		}
		imprimir("MOSTRAR ESPADA: MAPA", packet);
	}
	
	public static void ENVIAR_Gc_MOSTRAR_ESPADA_A_JUGADOR(final Personaje perso, final String packet) {
		enviarEnCola(perso, packet, true);
		imprimir("MOSTRAR ESPADA: PERSO", packet);
	}
	
	public static void ENVIAR_Gc_BORRAR_ESPADA_EN_MAPA(final Mapa mapa, final int idPelea) {
		final String packet = "Gc-" + idPelea;
		for (final Personaje perso : mapa.getArrayPersonajes()) {
			if (perso.getPelea() == null) {
				enviarEnCola(perso, packet, true);
			}
		}
		imprimir("BORRAR ESPADA: MAPA", packet);
	}
	
	public static void ENVIAR_Gt_AGREGAR_NOMBRE_ESPADA(final Mapa mapa, final int idInit1, final Luchador luchador) {
		final String packet = "Gt" + idInit1 + "|+" + luchador.getID() + ";" + luchador.getNombre() + ";" + luchador
		.getNivel();
		for (final Personaje perso : mapa.getArrayPersonajes()) {
			if (perso.getPelea() == null) {
				enviarEnCola(perso, packet, true);
			}
		}
		imprimir("AGREGAR NOMBRE ESPADA: MAPA", packet);
	}
	
	public static void ENVIAR_Gt_AGREGAR_NOMBRE_ESPADA(final Personaje perso, final int idInit1, final String str) {
		final String packet = "Gt" + idInit1 + "|+" + str;
		enviarEnCola(perso, packet, true);
		imprimir("AGREGAR NOMBRE ESPADA: PERSO", packet);
	}
	
	public static void ENVIAR_Gt_BORRAR_NOMBRE_ESPADA(final Mapa mapa, final int idInit1, final Luchador luchador) {
		final String packet = "Gt" + idInit1 + "|-" + luchador.getID();
		for (final Personaje perso : mapa.getArrayPersonajes()) {
			if (perso.getPelea() == null) {
				enviarEnCola(perso, packet, true);
			}
		}
		imprimir("BORRAR NOMBRE ESPADA: MAPA", packet);
	}
	
	public static void ENVIAR_Os_SETS_RAPIDOS(final Personaje perso) {
		final String packet = "Os" + perso.getSetsRapidos();
		enviarEnCola(perso, packet, true);
		imprimir("SETS RAPIDOS: PERSO", packet);
	}
	
	public static void ENVIAR_Oa_CAMBIAR_ROPA_MAPA(final Mapa mapa, final Personaje perso) {
		final String packet = perso.strRopaDelPJ();
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("CAMBIAR ROPA: MAPA", packet);
	}
	
	public static void ENVIAR_Oa_CAMBIAR_ROPA_PELEA(final Pelea pelea, final Personaje perso) {
		final String packet = perso.strRopaDelPJ();
		for (final Luchador luchador : pelea.luchadoresDeEquipo(3)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("CAMBIAR ROPA: PELEA", packet);
	}
	
	public static void ENVIAR_GIC_CAMBIAR_POS_PELEA(final Pelea pelea, final int equipos, final Mapa mapa, final int id,
	final short celda) {
		final String packet = "GIC|" + id + ";" + celda;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("CAMBIAR POS PELEA: PELEA", packet);
	}
	
	public static void ENVIAR_Go_BOTON_ESPEC_AYUDA(final Mapa mapa, final char s, final char opcion, final int id) {
		final String packet = "Go" + s + opcion + id;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("BOT. ESPEC. AYUDA: MAPA", packet);
	}
	
	public static void ENVIAR_Go_BOTON_ESPEC_AYUDA(final Personaje perso, final char s, final char opcion, final int id) {
		final String packet = "Go" + s + opcion + id;
		enviarEnCola(perso, packet, true);
		imprimir("BOT. ESPEC. AYUDA: PERSO", packet);
	}
	
	public static void ENVIAR_GR_TODOS_LUCHADORES_LISTOS(final Pelea pelea, final int equipos, final int id,
	final boolean b) {
		final String packet = "GR" + (b ? "1" : "0") + id;
		if (pelea.getFase() != 2) {
			return;
		}
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("LUCHADORES LISTO: PELEA", packet);
	}
	
	public static void ENVIAR_Im_INFORMACION(final Personaje perso, final String str) {
		final String packet = "Im" + str;
		enviarEnCola(perso, packet, true);
		imprimir("INFORMACION: PERSO", packet);
	}
	
	public static void ENVIAR_Im_INFORMACION_A_TODOS(final String str) {
		final String packet = "Im" + str;
		for (final Personaje perso : Mundo.getPersonajesEnLinea()) {
			enviarEnCola(perso, packet, false);
		}
		imprimir("INFORMACION: TODOS", packet);
	}
	
	public static void ENVIAR_Im_INFORMACION_KOLISEO(final String str) {
		String packet = "Im" + str;
		for (final Personaje perso : Mundo.getInscritosKoliseo()) {
			enviarEnCola(perso, packet, false);
		}
		imprimir("INFORMACION: KOLISEO", packet);
	}
	
	public static void ENVIAR_Im1223_MENSAJE_IMBORRABLE_PELEA(final Pelea pelea, final int equipos, final String str) {
		final String packet = "Im1223;" + str;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("Im1223: PELEA", packet);
	}
	
	public static void ENVIAR_Im1223_MENSAJE_IMBORRABLE_TODOS(final String es, final String fr) {
		String packet = "Im1223;" + es;
		String packetFr = "Im1223;" + fr;
		for (final Personaje perso : Mundo.getPersonajesEnLinea()) {
			if (perso.getCuenta().getIdioma().equals("fr") && !fr.isEmpty()) {
				enviarEnCola(perso, packetFr, false);
			} else if (!es.isEmpty()) {
				enviarEnCola(perso, packet, false);
			}
		}
		imprimir("Im1223: TODOS", packet);
	}
	
	public static void ENVIAR_Im1223_MENSAJE_IMBORRABLE_TODOS(final String es) {
		String packet = "Im1223;" + es;
		for (final Personaje perso : Mundo.getPersonajesEnLinea()) {
			enviarEnCola(perso, packet, false);
		}
		imprimir("Im1223: TODOS", packet);
	}
	
	public static void ENVIAR_Im1223_MENSAJE_IMBORRABLE_KOLISEO(final String es, final String fr) {
		String packet = "Im1223;" + es;
		String packetFr = "Im1223;" + fr;
		for (final Personaje perso : Mundo.getInscritosKoliseo()) {
			if (perso.getCuenta().getIdioma().equals("fr") && !fr.isEmpty()) {
				enviarEnCola(perso, packetFr, false);
			} else if (!es.isEmpty()) {
				enviarEnCola(perso, packet, false);
			}
		}
		imprimir("Im1223: KOLISEO", packet);
	}
	
	public static void ENVIAR_Im1223_MENSAJE_IMBORRABLE(final Personaje perso, final String str) {
		final String packet = "Im1223;" + str;
		enviarEnCola(perso, packet, true);
		imprimir("Im1223: PERSO", packet);
	}
	
	public static void ENVIAR_ILS_TIEMPO_REGENERAR_VIDA(final Personaje perso, final int tiempoRegen) {
		final String packet = "ILS" + tiempoRegen;
		enviarEnCola(perso, packet, true);
		imprimir("TIEMPO REGEN VIDA: PERSO", packet);
	}
	
	public static void ENVIAR_ILF_CANTIDAD_DE_VIDA(final Personaje perso, final int cantidad) {
		final String packet = "ILF" + cantidad;
		enviarEnCola(perso, packet, true);
		imprimir("CANT VIDA REGENERADA: PERSO", packet);
	}
	
	public static void ENVIAR_Im_INFORMACION_A_MAPA(final Mapa mapa, final String id) {
		final String packet = "Im" + id;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("INFORMACION: MAPA", packet);
	}
	
	public static void ENVIAR_eUK_EMOTE_MAPA(final Mapa mapa, final int id, final int emote, final int tiempo) {
		final String packet = "eUK" + id + "|" + emote + "|" + tiempo;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("EMOTE: MAPA", packet);
	}
	
	public static void ENVIAR_Im_INFORMACION_A_PELEA(final Pelea pelea, final int equipos, final String msj) {
		final String packet = "Im" + msj;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("INFORMACION: PELEA", packet);
	}
	
	public static void ENVIAR_cs_CHAT_MENSAJE(final Personaje perso, final String msj, final String color) {
		final String packet = "cs<font color='#" + color + "'>" + msj + "</font>";
		enviarEnCola(perso, packet, true);
		imprimir("CHAT: PERSO", packet);
	}
	
	public static void ENVIAR_cs_CHAT_MENSAJE_A_TODOS(final String msj, final String color) {
		final String packet = "cs<font color='#" + color + "'>" + msj + "</font>";
		for (final Personaje pj : Mundo.getPersonajesEnLinea()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("CHAT: TODOS", packet);
	}
	
	public static void ENVIAR_cs_CHAT_MENSAJE_A_MAPA(final Mapa mapa, final String msj, final String color) {
		final String packet = "cs<font color='#" + color + "'>" + msj + "</font>";
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("CHAT: MAPA", packet);
	}
	
	public static void ENVIAR_cs_CHAT_MENSAJE_A_PELEA(final Pelea pelea, final String msj, final String color) {
		final String packet = "cs<font color='#" + color + "'>" + msj + "</font>";
		for (final Luchador luchador : pelea.luchadoresDeEquipo(7)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("CHAT: MAPA", packet);
	}
	
	public static void ENVIAR_GA900_DESAFIAR(final Mapa mapa, final int id, final int id2) {
		final String packet = "GA;900;" + id + ";" + id2;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("DESAFIAR: MAPA ID " + mapa.getID() + ": MAPA", packet);
	}
	
	public static void ENVIAR_GA901_ACEPTAR_DESAFIO(final Personaje pj, final int id, final int id2) {
		final String packet = "GA;901;" + id + ";" + id2;
		enviarEnCola(pj, packet, true);
		imprimir("ACEPTAR DESAFIO: MAPA", packet);
	}
	
	public static void ENVIAR_GA902_RECHAZAR_DESAFIO(final Personaje pj, final int id, final int id2) {
		final String packet = "GA;902;" + id + ";" + id2;
		enviarEnCola(pj, packet, true);
		imprimir("RECHAZAR DESAFIO: PERSO", packet);
	}
	
	public static void ENVIAR_GA903_ERROR_PELEA(final Personaje perso, final char c) {
		if (perso == null) {
			return;
		}
		final String packet = "GA;903;;" + c;
		enviarEnCola(perso, packet, true);
		imprimir("ERROR JUEGO: PERSO", packet);
	}
	
	public static void ENVIAR_GIC_UBICACION_LUCHADORES_INICIAR(final Pelea pelea, final int equipos) {
		String packet = ("GIC|");
		for (final Luchador luchador : pelea.luchadoresDeEquipo(3)) {
			if (luchador.estaRetirado() || luchador.getCeldaPelea() == null || luchador.esMultiman()) {
				continue;
			}
			packet += (luchador.getID() + ";" + luchador.getCeldaPelea().getID() + "|");
		}
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("UBIC LUCH INICIAR: PELEA", packet);
	}
	
	public static void ENVIAR_GIC_APARECER_LUCHADORES_INVISIBLES(final Pelea pelea, final int equipos,
	final Luchador luch) {
		final String packet = "GIC|" + luch.getID() + ";" + luch.getCeldaPelea().getID();
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("APARECER LUCH INVI: PELEA", packet);
	}
	
	public static void ENVIAR_GIC_APARECER_LUCHADORES_INVISIBLES(final Luchador luchador, final String str) {
		final String packet = "GIC|" + str;
		if (luchador.estaRetirado() || luchador.esMultiman()) {
			return;
		}
		enviarEnCola(luchador.getPersonaje(), packet, true);
		imprimir("APARECER LUCH INVI: PERSO", packet);
	}
	
	public static void ENVIAR_GS_EMPEZAR_COMBATE_EQUIPOS(final Pelea pelea, final int equipos) {
		final String packet = "GS";
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("INICIAR PELEA: PELEA", packet);
	}
	
	public static void ENVIAR_GS_EMPEZAR_COMBATE(final Personaje perso) {
		final String packet = "GS";
		enviarEnCola(perso, packet, true);
		imprimir("INICIO PELEA: PERSO", packet);
	}
	
	public static void ENVIAR_GTL_ORDEN_JUGADORES(final Pelea pelea, final int equipos) {
		final String packet = pelea.stringOrdenJugadores();
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("ORDEN LUCH: PELEA", packet);
	}
	
	public static void ENVIAR_GTL_ORDEN_JUGADORES(final Personaje perso, final Pelea pelea) {
		final String packet = pelea.stringOrdenJugadores();
		enviarEnCola(perso, packet, true);
		imprimir("ORDEN LUCH: PERSO", packet);
	}
	
	public static void ENVIAR_Gñ_IDS_PARA_MODO_CRIATURA(final Pelea pelea, final Personaje perso) {
		String packet = ("Gñ");
		for (final Luchador luchador : pelea.luchadoresDeEquipo(3)) {
			if (luchador.estaMuerto() || luchador.esInvisible(perso.getID())) {
				continue;
			}
			if (packet.length() > 2) {
				packet += (",");
			}
			packet += (luchador.getID());
		}
		enviarEnCola(perso, packet, true);
		imprimir("IDS MODO CRIATURA: PERSO", packet);
	}
	
	public static void ENVIAR_GTM_INFO_STATS_TODO_LUCHADORES_A_TODOS(final Pelea pelea, final int equipos, boolean como999) {
	    ArrayList<Luchador> aEnviar = pelea.luchadoresDeEquipo(equipos);
	    for (final Luchador luchador : pelea.luchadoresDeEquipo(3)) {
	        final TotalStats totalStats = luchador.getTotalStats();
	        final StringBuilder packet1 = new StringBuilder();
	        final StringBuilder packet2 = new StringBuilder();
	        packet1.append("|" + luchador.getID() + ";");

	        if (luchador.estaMuerto()) {
	            packet1.append(1 + ";");
	        } else {
	            if (como999 && !luchador.getUpdateGTM()) {
	                continue;
	            }

	            packet1.append(0 + ";");
	            packet1.append(luchador.getPDVConBuff() + ";");
	            packet1.append(Math.max(0, luchador.getPARestantes()) + ";");
	            packet1.append(Math.max(0, luchador.getPMRestantes()) + ";"); // PM actuales

	            //packet2.append(";"); // comentado compatibilidad 1.43.7   
	            packet2.append(luchador.getPDVMaxConBuff() + ";");

	            // === NUEVO ORDEN: primero APinit / MPinit ===
	            packet2.append(totalStats.getTotalStatParaMostrar(Constantes.STAT_MAS_PA) + ";"); // APinit
	            packet2.append(totalStats.getTotalStatParaMostrar(Constantes.STAT_MAS_PM) + ";"); // MPinit

	            // === Luego las resistencias ===
	            int[] resist = new int[7];
	            switch (pelea.getTipoPelea()) {
	                case Constantes.PELEA_TIPO_DESAFIO:
	                case Constantes.PELEA_TIPO_KOLISEO:
	                case Constantes.PELEA_TIPO_PVP:
	                case Constantes.PELEA_TIPO_RECAUDADOR:
	                    resist[0] = Constantes.STAT_MAS_RES_PORC_PVP_NEUTRAL;
	                    resist[1] = Constantes.STAT_MAS_RES_PORC_PVP_TIERRA;
	                    resist[2] = Constantes.STAT_MAS_RES_PORC_PVP_FUEGO;
	                    resist[3] = Constantes.STAT_MAS_RES_PORC_PVP_AGUA;
	                    resist[4] = Constantes.STAT_MAS_RES_PORC_PVP_AIRE;
	                    break;
	                default:
	                    resist[0] = Constantes.STAT_MAS_RES_PORC_NEUTRAL;
	                    resist[1] = Constantes.STAT_MAS_RES_PORC_TIERRA;
	                    resist[2] = Constantes.STAT_MAS_RES_PORC_FUEGO;
	                    resist[3] = Constantes.STAT_MAS_RES_PORC_AGUA;
	                    resist[4] = Constantes.STAT_MAS_RES_PORC_AIRE;
	                    break;
	            }
	            resist[5] = Constantes.STAT_MAS_ESQUIVA_PERD_PA;
	            resist[6] = Constantes.STAT_MAS_ESQUIVA_PERD_PM;

	            for (int statID : resist) {
	                int total = totalStats.getTotalStatConComplemento(statID);
	                packet2.append(total + ",");
	            }

	            luchador.setUpdateGTM(false);
	        }

	        for (final Luchador enviar : aEnviar) {
	            if (enviar.estaRetirado() || enviar.getPersonaje() == null || enviar.esMultiman()) {
	                continue;
	            }
	            enviar.getStringBuilderGTM().append(packet1.toString());
	            if (!luchador.estaMuerto()) {
	                enviar.getStringBuilderGTM().append(
	                    (luchador.getCeldaPelea() == null || luchador.esInvisible(enviar.getID())
	                        ? "-1"
	                        : luchador.getCeldaPelea().getID())
	                        + ";" + packet2.toString()
	                );
	            }
	        }
	    }

	    for (final Luchador enviar : aEnviar) {
	        if (enviar.estaRetirado() || enviar.getPersonaje() == null || enviar.esMultiman()) {
	            continue;
	        }
	        if (enviar.getStringBuilderGTM().toString().isEmpty()) {
	            continue;
	        }

	        String packet = "";
	        if (como999) {
	            packet += "GA;999;;GTU";
	        } else {
	            packet += "GTM"; // Cliente 1.43.7 espera GTM, no GA;999;;GTU
	        }

	        packet += enviar.getStringBuilderGTM().toString();
	        enviar.resetStringBuilderGTM();
	        enviarEnCola(enviar.getPersonaje(), packet, true);
	        imprimir("INFO STATS LUCH: PERSO " + enviar.getID() + "", packet);
	    }
	}

	
	public static void ENVIAR_GTM_INFO_STATS_TODO_LUCHADORES_A_PERSO(final Personaje perso, final Pelea pelea) {
	    if (perso == null) {
	        return;
	    }

	    final StringBuilder packet = new StringBuilder("GTM");

	    for (final Luchador luchador : pelea.luchadoresDeEquipo(3)) {
	        final TotalStats totalStats = luchador.getTotalStats();
	        packet.append("|" + luchador.getID() + ";");

	        if (luchador.estaMuerto()) {
	            packet.append(1 + ";");
	        } else {
	            packet.append(0 + ";");
	            packet.append(luchador.getPDVConBuff() + ";");
	            packet.append(Math.max(0, luchador.getPARestantes()) + ";");
	            packet.append(Math.max(0, luchador.getPMRestantes()) + ";"); // PM actuales
	            packet.append(
	                (luchador.getCeldaPelea() == null || luchador.esInvisible(perso.getID())
	                    ? "-1"
	                    : luchador.getCeldaPelea().getID()) + ";"
	            );

	            //packet.append(";"); // comentado compatibilidad 1.43.7 
	            packet.append(luchador.getPDVMaxConBuff() + ";");

	            // === NUEVO ORDEN: APinit y MPinit antes de resistencias ===
	            packet.append(totalStats.getTotalStatParaMostrar(Constantes.STAT_MAS_PA) + ";"); // APinit
	            packet.append(totalStats.getTotalStatParaMostrar(Constantes.STAT_MAS_PM) + ";"); // MPinit

	            // === Luego las resistencias ===
	            int[] resist = new int[7];
	            switch (pelea.getTipoPelea()) {
	                case Constantes.PELEA_TIPO_DESAFIO:
	                case Constantes.PELEA_TIPO_KOLISEO:
	                case Constantes.PELEA_TIPO_PVP:
	                case Constantes.PELEA_TIPO_RECAUDADOR:
	                    resist[0] = Constantes.STAT_MAS_RES_PORC_PVP_NEUTRAL;
	                    resist[1] = Constantes.STAT_MAS_RES_PORC_PVP_TIERRA;
	                    resist[2] = Constantes.STAT_MAS_RES_PORC_PVP_FUEGO;
	                    resist[3] = Constantes.STAT_MAS_RES_PORC_PVP_AGUA;
	                    resist[4] = Constantes.STAT_MAS_RES_PORC_PVP_AIRE;
	                    break;
	                default:
	                    resist[0] = Constantes.STAT_MAS_RES_PORC_NEUTRAL;
	                    resist[1] = Constantes.STAT_MAS_RES_PORC_TIERRA;
	                    resist[2] = Constantes.STAT_MAS_RES_PORC_FUEGO;
	                    resist[3] = Constantes.STAT_MAS_RES_PORC_AGUA;
	                    resist[4] = Constantes.STAT_MAS_RES_PORC_AIRE;
	                    break;
	            }
	            resist[5] = Constantes.STAT_MAS_ESQUIVA_PERD_PA;
	            resist[6] = Constantes.STAT_MAS_ESQUIVA_PERD_PM;

	            for (int statID : resist) {
	                int total = totalStats.getTotalStatConComplemento(statID);
	                packet.append(total + ",");
	            }
	        }
	    }

	    enviarEnCola(perso, packet.toString(), true);
	    imprimir("INFO STATS LUCH: PERSO", packet.toString());
	}

	
	public static void ENVIAR_GTS_INICIO_TURNO_PELEA(final Pelea pelea, final int equipos, final int id,
	final int tiempo) {
		final String packet = "GTS" + id + "|" + tiempo;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("INICIO TURNO: PELEA", packet);
	}
	
	public static void ENVIAR_GTS_INICIO_TURNO_PELEA(final Personaje perso, final int id, final int tiempo) {
		final String packet = "GTS" + id + "|" + tiempo;
		enviarEnCola(perso, packet, true);
		imprimir("INICIO TURNO: PERSO", packet);
	}
	
	public static void ENVIAR_GV_RESETEAR_PANTALLA_JUEGO(final Personaje perso) {
		String packet = "GV";
		enviarEnCola(perso, packet, true);
		imprimir("RESETEAR PANTALLA JUEGO: PERSO", packet);
	}
	
	public static void ENVIAR_GAS_INICIO_DE_ACCION(final Personaje perso, final int id) {
		final String packet = "GAS" + id;
		enviarEnCola(perso, packet, true);
		imprimir("INICIO ACCION: PELEA", packet);
	}
	
	public static void ENVIAR_GA_ACCION_DE_JUEGO(final Personaje perso, final int respuestaID, final int accionID,
			final String s2, final String s3) { // this variable is not used search in your sources 
		if (!Constantes.esAccionParaMostrar(accionID)) {
			return;
		}
		String packet = ("GA" + respuestaID + ";" + accionID);
		if (!s2.isEmpty()) {
			packet += (";" + s2);
		}
		if (!s3.isEmpty()) {
			packet += (";" + s3);
		}
		enviarEnCola(perso, packet, true);
		if (MainServidor.MOSTRAR_ENVIOS) {
			imprimir("ACCION DE JUEGO: PERSO", packet);
		}
	}
	// this is use
	public static void ENVIAR_GA_ACCION_PELEA_LUCHADOR(final Luchador luchador, final int accionID, final String s2,
			final String s3) {
		if (!Constantes.esAccionParaMostrar(accionID)) {
			return;
		}
		if (luchador.estaRetirado() || luchador.esMultiman()) {
			return;
		}
		String packet = ("GA;" + accionID + ";" + s2);
		if (!s3.isEmpty()) {
			packet += (";" + s3);
		}
		enviarEnCola(luchador.getPersonaje(), packet, true);
		if (MainServidor.MOSTRAR_ENVIOS) {
			imprimir("ACCION PELEA: PERSO", packet);
		}
		try {
			Thread.sleep(EfectoHechizo.TIEMPO_GAME_ACTION);
		} catch (Exception e) {
		}
	}
	
	// public static void ENVIAR_GA_ACCION_PELEA_CON_DURACION(final Pelea pelea, final int equipos,
	// final int accionID, final Luchador lanzador,
	// final int idObjetivo , final int valor, final int duracion) {
	// final StringBuilder packet = new StringBuilder("GA;" + accionID + ";" + lanzador.getID() + ";"
	// + idObjetivo +"," + valor +","+ (lanzador.puedeJugar() ? duracion + 1 ) );
	// for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
	// if (luchador.estaRetirado() || luchador.esMultiman()) {
	// continue;
	// }
	// enviar(luchador.getPersonaje(), packet.toString());
	// }
	// if (Bustemu.MOSTRAR_ENVIOS) {
	// imprimir("ACCION PELEA DURACION: PELEA" , packet.toString());
	// }
	// }
	public static void ENVIAR_GA_ACCION_PELEA(final Pelea pelea, final int equipos, final int accionID, final String s2,
			final String s3) {
		if (!Constantes.esAccionParaMostrar(accionID)) {
			return;
		} 
    	String packet = ("GA;" + accionID + ";" + s2);
		ArrayList<String> packetlist = new ArrayList<String>();
    	if (!s3.isEmpty()) {
                if(s3.contains("¬")) {
                    String[] s3list = s3.split("¬");
                    for (String s : s3list) {
                        packetlist.add(packet+";"+s);
                    }
                } else {
                    packet = packet+ ";" + s3;
                }
            }
		
    	for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
            if (luchador.getPersonaje()!= null) {
                if (packetlist.isEmpty()) {
                		enviar(luchador.getPersonaje(), packet);
                } else {
                    for (String s : packetlist) {
                    	enviar(luchador.getPersonaje(), s); //open retro folder pls
                    	 try {
                             Thread.sleep(100);
                         } catch (Exception e) {
                         }
                    }
                }

            } 
        }
        imprimir("ACCION PELEA: PELEA", packet);
	}
	
	public static void ENVIAR_GA950_ACCION_PELEA_ESTADOS(final Pelea pelea, final int equipos, int afectado, int estado,
	boolean activo) {
		int accionID = 950;
		if (!Constantes.esAccionParaMostrar(accionID)) {
			return;
		}
		String packet = ("GA;" + accionID + ";" + afectado + ";" + afectado + "," + estado + "," + (activo ? 1 : 0));
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("ACCION 950 ESTADOS: PELEA", packet);
		try {
			Thread.sleep(EfectoHechizo.TIEMPO_GAME_ACTION);
		} catch (Exception e) {}
	}
	
	public static void ENVIAR_GA_ACCION_PELEA_CON_RESPUESTA(final Pelea pelea, final int equipos, final int respuestaID,
	final int accionID, final String s2, final String s3) {
		if (!Constantes.esAccionParaMostrar(accionID)) {
			return;
		}
		final String packet = "GA" + respuestaID + ";" + accionID + ";" + s2 + ";" + s3;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("ACCION PELEA CON RESP.: PELEA", packet);
	}
	
	public static void ENVIAR_GA_ACCION_PELEA_MOVERSE(final Pelea pelea, final Luchador movedor, final int equipos,
	final int respuestaID, int accionID, final String s2, final String s3) {
		final String packet = "GA" + respuestaID + ";" + accionID + ";" + s2 + ";" + s3;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || movedor.esInvisible(luchador.getID()) || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("ACCION PELEA MOVERSE: PELEA", packet);
	}
	
	// public static void ENVIAR_GA_PERDER_PM_PELEA(final Pelea pelea, final int equipos, final String
	// packet) {
	// if (packet.isEmpty()) {
	// return;
	// }
	// for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
	// if (luchador.estaRetirado() || luchador.esMultiman()) {
	// continue;
	// }
	// enviar(luchador.getPersonaje(), packet, true);
	// }
	// if (Bustemu.MOSTRAR_ENVIOS) {
	// imprimir("PM USADOS PARA MOVERSE: PELEA" , packet);
	// }
	// }
	public static void ENVIAR_GAF_FINALIZAR_ACCION(final Personaje perso, final int luchID, final int unicaID) {
		String packet = "GAF" + luchID;
		if (unicaID >= 0) {
			packet += "|" + unicaID;// si se pone accion envia un GKK(ID_UNICA)
		}
		enviarEnCola(perso, packet, true);
		imprimir("FINALIZAR ACCION: PERSO", packet);
	}
	
	public static void ENVIAR_GAs_PARAR_MOVIMIENTO_SPRITE(final Personaje perso, final int luchID) {
		String packet = "GAF" + luchID;
		enviarEnCola(perso, packet, true);
		imprimir("PARAR MOVIMIENTO SPRITE: PERSO", packet);
	}
	
	public static void ENVIAR_GAC_LIMPIAR_ACCION(final Personaje perso) {
		String packet = "GAC" + (char) 0x00 + "GA;940";
		enviarEnCola(perso, packet, true);
		imprimir("LIMPIAR ACCION: PERSO", packet);
	}
	
	public static void ENVIAR_GA_DEBUG_ACCIONES(final Personaje perso) {
		String packet = "GA;940";
		enviarEnCola(perso, packet, true);
		imprimir("DEBUG ACCIONES: PERSO", packet);
	}
	
	public static void ENVIAR_GTF_FIN_DE_TURNO(final Pelea pelea, final int equipos, final int id) {
		final String packet = "GTF" + id;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("FIN TURNO: PELEA", packet);
	}
	
	public static void ENVIAR_GTR_TURNO_LISTO(final Pelea pelea, final int equipos, final int id) {
		final String packet = "GTR" + id;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("TURNO LISTO: PELEA", packet);
	}
	
	public static void ENVIAR_cS_EMOTICON_MAPA(final Mapa mapa, final int id, final int pid) {
		final String packet = "cS" + id + "|" + pid;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("EMOTE: MAPA", packet);
	}
	
	public static void ENVIAR_SUE_NIVEL_HECHIZO_ERROR(final Personaje ss) {
		final String packet = "SUE";
		enviarEnCola(ss, packet, true);
		imprimir("NIVEL HECHIZO ERROR: OUT", packet);
	}
	
	public static void ENVIAR_SUK_NIVEL_HECHIZO(final Personaje perso, final int hechizoID, final int nivel) {
		final String packet = "SUK" + hechizoID + "~" + nivel;
		enviarEnCola(perso, packet, true);
		imprimir("NIVEL HECHIZOS: PERSO", packet);
	}
	
	public static void ENVIAR_SL_LISTA_HECHIZOS(final Personaje perso) {
		final String packet = "SL" + perso.stringListaHechizos();
		enviarEnCola(perso, packet, true);
		imprimir("LISTA HECHIZOS: PERSO", packet);
	}
	
	public static void ENVIAR_GA103_JUGADOR_MUERTO(final Pelea pelea, final int equipos, final int id) {
		final String packet = "GA;103;" + id + ";" + id;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("LUCH. MUERTO: PELEA", packet);
	}
	
	public static void ENVIAR_GE_PANEL_RESULTADOS_PELEA(final Pelea pelea, final int equipos, final String packet) {
		ArrayList<String> ips = new ArrayList<>();
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			if (luchador.getPersonaje() != null) {
				String ip = luchador.getPersonaje().getCuenta().getActualIP();
				if (ips.contains(ip)) {
					try {
						Thread.sleep(100);
					} catch (final Exception e) {}
				} else {
					ips.add(ip);
				}
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("PANEL RESULTADOS: PELEA", packet);
	}
	
	public static void ENVIAR_GA998_AGREGAR_BUFF_PELEA(final Pelea pelea, final int equipos, String packet) {
		if (packet.isEmpty()) {
			return;
		}
		packet = "GA;998;" + packet;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("AGREGAR BUFF: PELEA", packet);
	}
	
	public static void ENVIAR_GA998_AGREGAR_BUFF(final Personaje perso, String packet) {
		if (packet.isEmpty()) {
			return;
		}
		packet = "GA;998;" + packet;
		enviarEnCola(perso, packet, true);
		imprimir("AGREGAR BUFF: PERSO", packet);
	}
	
	public static void ENVIAR_GIE_AGREGAR_BUFF_PELEA(final Pelea pelea, final int equipos, String packet) {
		if (packet.isEmpty()) {
			return;
		}
		packet = "GIE" + packet;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("AGREGAR BUFF: PELEA", packet);
	}
	
	public static void ENVIAR_GIE_AGREGAR_BUFF(final Personaje perso, String packet) {
		if (packet.isEmpty()) {
			return;
		}
		packet = "GIE" + packet;
		enviarEnCola(perso, packet, true);
		imprimir("AGREGAR BUFF: PERSO", packet);
	}
	
	public static void ENVIAR_GIe_QUITAR_BUFF(final Pelea pelea, final int equipos, final int id) {
		final String packet = "GIe" + id;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("QUITAR BUFFS: PELEA", packet);
	}
	
	public static void ENVIAR_cMK_CHAT_MENSAJE_PERSONAJE(final Personaje perso, final String sufijo, final int id,
	final String nombre, final String msj) {
		if (!perso.tieneCanal(sufijo)) {
			return;
		}
		final String packet = "cMK" + sufijo + "|" + id + "|" + nombre + "|" + msj;
		enviarEnCola(perso, packet, true);
		imprimir("CHAT: PERSO", packet);
	}
	
	public static void ENVIAR_cMK_CHAT_MENSAJE_PELEA(final Pelea pelea, final int equipos, final String sufijo,
	final int id, final String nombre, final String msj) {
		final String packet = "cMK" + sufijo + "|" + id + "|" + nombre + "|" + msj;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador == null) {
				continue;
			}
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			final Personaje p = luchador.getPersonaje();
			if (p == null || !p.tieneCanal(sufijo)) {
				continue;
			}
			enviarEnCola(p, packet, true);
		}
		imprimir("CHAT: PELEA", packet);
	}
	
	public static void ENVIAR_cMK_CHAT_MENSAJE_MAPA(final Personaje perso, String sufijo, final String msj) {
		final String packet = "cMK" + sufijo + "|" + perso.getID() + "|" + perso.getNombre() + "|" + msj;
		for (final Personaje p : perso.getMapa().getArrayPersonajes()) {
			if (!p.tieneCanal(sufijo)) {
				continue;
			}
			enviarEnCola(p, packet, true);
		}
		imprimir("CHAT: MAPA", packet);
	}
	
	public static void ENVIAR_cMK_MENSAJE_CHAT_GRUPO(final Personaje perso, final String msj) {
		String sufijo = "$";
		final String packet = "cMK" + sufijo + "|" + perso.getID() + "|" + perso.getNombre() + "|" + msj;
		for (final Personaje p : perso.getGrupoParty().getMiembros()) {
			if (!p.tieneCanal(sufijo)) {
				continue;
			}
			enviarEnCola(p, packet, true);
		}
		imprimir("MSJ CHAT GRUPO: GRUPO", packet);
	}
	
	public static void ENVIAR_cMK_CHAT_MENSAJE_GREMIO(final Personaje perso, final String msj) {
		String sufijo = "%";
		final String packet = "cMK" + sufijo + "|" + perso.getID() + "|" + perso.getNombre() + "|" + msj;
		for (final Personaje p : perso.getGremio().getMiembros()) {
			if (!p.tieneCanal(sufijo)) {
				continue;
			}
			enviarEnCola(p, packet, true);
		}
		imprimir("CHAT: GREMIO", packet);
	}
	
	public static void ENVIAR_cMK_CHAT_MENSAJE_KOLISEO(final Personaje perso, final String msj) {
		String sufijo = "¿";
		final String packet = "cMK" + sufijo + "|" + perso.getID() + "|" + perso.getNombre() + "|" + msj;
		for (final Personaje p : perso.getGrupoKoliseo().getMiembros()) {
			if (!p.tieneCanal(sufijo)) {
				continue;
			}
			enviarEnCola(p, packet, true);
		}
		imprimir("CHAT: KOLISEO", packet);
	}
	
	public static void ENVIAR_cMK_CHAT_MENSAJE_TODOS(final String sufijo, final Personaje perso, final String msj) {
		final String packet = "cMK" + sufijo + "|" + perso.getID() + "|" + perso.getNombre() + "|" + msj;
		for (final Personaje p : Mundo.getPersonajesEnLinea()) {
			if (!p.tieneCanal(sufijo)) {
				continue;
			}
			switch (sufijo) {
				case "^" :// mensaje incarnam
					if (p.getMapa().getSubArea().getArea().getSuperArea().getID() != 3) {
						continue;
					}
					break;
				case "!" :// alineacion
					if (p.getAlineacion() != perso.getAlineacion()) {
						continue;
					}
					break;
				case "¡" :// vip
					if (!MainServidor.PARAM_MOSTRAR_CHAT_VIP_TODOS && !p.getCuenta().esAbonado()) {
						continue;
					}
					break;
				case "@" :// admin
					if (p.getCuenta().getAdmin() <= 0) {
						continue;
					}
					break;
				case ":" :// mensaje comercio
					break;
			}
			enviarEnCola(p, packet, false);
		}
		imprimir("CHAT " + sufijo + " : TODOS", packet);
	}
	
	public static void ENVIAR_GDZ_COLOREAR_ZONA_EN_PELEA(final Pelea pelea, final int equipos, final String add,
	final short celda, final int tamaño, final int color, char forma) {// , int radioInt
		final String packet = "GDZ" + add + celda + ";" + tamaño + ";" + color + ";" + forma;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador == null) {
				continue;
			}
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("COLOREAR ZONA: PELEA", packet);
	}
	
	public static void ENVIAR_GDZ_COLOREAR_ZONA_A_LUCHADORES(final ArrayList<Luchador> luchadores, final String add,
	final short celda, final int tamaño, final int color, char forma) {
		final String packet = "GDZ" + add + celda + ";" + tamaño + ";" + color + ";" + forma;
		for (final Luchador luchador : luchadores) {
			if (luchador == null) {
				continue;
			}
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("COLOREAR ZONA: LUCHADORES", packet);
	}
	
	public static void ENVIAR_GDZ_COLOREAR_ZONA_A_LUCHADOR(final Luchador luchador, final String add, final short celda,
	final int tamaño, final int color, char forma) {
		final String packet = "GDZ" + add + celda + ";" + tamaño + ";" + color + ";" + forma;
		enviarEnCola(luchador.getPersonaje(), packet, true);
		imprimir("COLOREAR ZONA: LUCHADOR", packet);
	}
	
	public static void ENVIAR_GDC_ACTUALIZAR_CELDA_EN_PELEA(final Pelea pelea, final int equipos, final short celda,
	final String s1, final boolean permanente) {
		String packet = ("GDC" + celda + ";" + s1 + ";" + (permanente ? "0" : "1"));
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador == null) {
				continue;
			}
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("ACTUALIZAR CELDA: PELEA", packet);
	}
	
	public static void ENVIAR_GDC_ACTUALIZAR_CELDA_A_LUCHADORES(final ArrayList<Luchador> luchadores, final short celda,
	final String s1, final boolean permanente) {
		String packet = ("GDC" + celda + ";" + s1 + ";" + (permanente ? "0" : "1"));
		for (final Luchador luchador : luchadores) {
			if (luchador == null) {
				continue;
			}
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("ACTUALIZAR CELDA: LUCHADORES", packet);
	}
	
	public static void ENVIAR_GDC_ACTUALIZAR_CELDA_A_LUCHADOR(final Luchador luchador, final short celda, final String s1,
	final boolean permanente) {
		String packet = ("GDC" + celda + ";" + s1 + ";" + (permanente ? "0" : "1"));
		enviarEnCola(luchador.getPersonaje(), packet, true);
		imprimir("ACTUALIZAR CELDA: LUCHADOR", packet);
	}
	
	public static void ENVIAR_GDC_ACTUALIZAR_CELDA_MAPA(final Mapa mapa, short celda, final String s1,
	final boolean permanente) {// FIXME
		final String packet = "GDC" + celda + ";" + s1 + ";" + (permanente ? "0" : "1");
		for (final Personaje perso : mapa.getArrayPersonajes()) {
			enviarEnCola(perso, packet, true);
		}
		imprimir("AUTORIZAR CELDA: PELEA", packet);
	}
	
	public static void ENVIAR_cMEf_CHAT_ERROR(final Personaje ss, final String nombre) {
		final String packet = "cMEf" + nombre;
		enviarEnCola(ss, packet, true);
		imprimir("CHAT ERROR: PERSO", packet);
	}
	
	public static void ENVIAR_eD_CAMBIAR_ORIENTACION(final Mapa mapa, final int id, final byte dir) {
		final String packet = "eD" + id + "|" + dir;
		for (final Personaje perso : mapa.getArrayPersonajes()) {
			enviarEnCola(perso, packet, true);
		}
		imprimir("CAMBIAR ORIENTACION: MAPA", packet);
	}
	
	// public static void ENVIAR_eF_CAMBIAR_ORIENTACION_SI_O_SI(final Pelea pelea, final int id, final
	// byte dir) {
	// final String packet = "eF" + id + "|" + dir;
	// for (final Luchador luchador : pelea.luchadoresDeEquipo(7)) {
	// if (luchador.estaRetirado()) {
	// continue;
	// }
	// enviar(luchador.getPersonaje(), packet, true);
	// }
	// if (Bustemu.MOSTRAR_ENVIOS) {
	// imprimir("CAMBIAR ORIENTACION: PELEA" , packet);
	// }
	// }
	public static void ENVIAR_TB_CINEMA_INICIO_JUEGO(final Personaje ss) {
		final String packet = "TB";
		enviarEnCola(ss, packet, true);
		imprimir("CINEMA INICIO JUEGO: PERSO", packet);
	}
	
	public static void ENVIAR_TB_CINEMA_INICIO_JUEGO(final ServidorSocket ss) {
		final String packet = "TB";
		ss.enviarPW(packet);
		imprimir("CINEMA INICIO JUEGO: PERSO", packet);
	}
	
	public static void ENVIAR_GA2_CINEMATIC(final Personaje perso, final String cinema) {
		final String packet = "GA;2;;" + cinema;
		enviarEnCola(perso, packet, true);
		imprimir("CINEMATIC: PERSO", packet);
	}
	
	public static void ENVIAR_TC_CARGAR_TUTORIAL(final Personaje ss, final int tutorial) {
		final String packet = "TC" + tutorial + "|7001010000";
		enviarEnCola(ss, packet, true);
		imprimir("CARGAR TUTORIAL: PERSO", packet);
	}
	
	public static void ENVIAR_TT_MOSTRAR_TIP(final Personaje ss, final int tutorial) {
		final String packet = "TT" + tutorial;
		enviarEnCola(ss, packet, true);
		imprimir("MOSTRAR TIP: PERSO", packet);
	}
	
	public static void ENVIAR_DCK_CREAR_DIALOGO(final Personaje ss, final int id) {
		final String packet = "DCK" + id;
		enviarEnCola(ss, packet, true);
		imprimir("CREAR DIALOGO: PERSO", packet);
	}
	
	public static void ENVIAR_DQ_DIALOGO_PREGUNTA(final Personaje ss, final String str) {
		final String packet = "DQ" + str;
		enviarEnCola(ss, packet, true);
		imprimir("DIALOGO PREGUNTA: PERSO", packet);
	}
	
	public static void ENVIAR_DV_FINALIZAR_DIALOGO(final Personaje perso) {
		final String packet = "DV";
		enviarEnCola(perso, packet, true);
		imprimir("CERRAR DIALOGO: PERSO", packet);
	}
	
	public static void ENVIAR_BAT2_CONSOLA(final Personaje ss, final String str) {
		final String packet = "BAT2" + str;
		enviarEnCola(ss, packet, true);
		imprimir("CONSOLA COMANDOS: PERSO", packet);
	}
	
	public static void ENVIAR_EBE_ERROR_DE_COMPRA(final Personaje ss) {
		final String packet = "EBE";
		enviarEnCola(ss, packet, true);
		imprimir("ERROR COMPRA: PERSO", packet);
	}
	
	public static void ENVIAR_ESE_ERROR_VENTA(final Personaje perso) {
		String packet = "ESE";
		enviarEnCola(perso, packet, true);
		imprimir("ERROR VENTA: PERSO", packet);
	}
	
	public static void ENVIAR_EBK_COMPRADO(final Personaje ss) {
		final String packet = "EBK";
		enviarEnCola(ss, packet, true);
		imprimir("COMPRADO: PERSO", packet);
	}
	
	public static void ENVIAR_ESK_VENDIDO(final Personaje perso) {
		final String packet = "ESK";
		enviarEnCola(perso, packet, true);
		imprimir("VENDIDO: PERSO", packet);
	}
	
	public static void ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(final Personaje perso, final Objeto obj) {
		final String packet = "OQ" + obj.getID() + "|" + obj.getCantidad();
		enviarEnCola(perso, packet, true);
		imprimir("CAMBIA CANT OBJETO: PERSO", packet);
	}
	
	public static void ENVIAR_OR_ELIMINAR_OBJETO(final Personaje perso, final int id) {
		final String packet = "OR" + id;
		enviarEnCola(perso, packet, true);
		imprimir("ELIMINAR OBJETO: PERSO", packet);
	}
	
	public static void ENVIAR_ODE_ERROR_ELIMINAR_OBJETO(final Personaje ss) {
		final String packet = "ODE";
		enviarEnCola(ss, packet, true);
		imprimir("ERROR ELIMINAR OBJETO: PERSO", packet);
	}
	
	public static void ENVIAR_OM_MOVER_OBJETO(final Personaje perso, final Objeto obj) {
		String packet = "OM" + obj.getID() + "|";
		if (obj.getPosicion() != Constantes.OBJETO_POS_NO_EQUIPADO) {
			packet += obj.getPosicion();
		}
		enviarEnCola(perso, packet, true);
		imprimir("MOVER OBJETO: PERSO", packet);
	}
	
	public static void ENVIAR_cS_EMOTE_EN_PELEA(final Pelea pelea, final int equipos, final int id, final int id2) {
		final String packet = "cS" + id + "|" + id2;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("EMOTE PELEA: PELEA", packet);
	}
	
	public static void ENVIAR_OAEL_ERROR_AGREGAR_OBJETO(final Personaje ss) {
		final String packet = "OAEL";
		enviarEnCola(ss, packet, true);
		imprimir("ERROR AGREGAR OBJETO: PERSO", packet);
	}
	
	public static void ENVIAR_Ow_PODS_DEL_PJ(final Personaje perso) {
		final String packet = "Ow" + perso.getPodsUsados() + "|" + perso.getPodsMaximos();
		enviarEnCola(perso, packet, true);
		imprimir("PODS: PERSO", packet);
	}
	
	public static void ENVIAR_OAKO_APARECER_OBJETO(final Personaje perso, final Objeto objeto) {
		final String packet = "OAKO" + objeto.stringObjetoConGuiño();
		enviarEnCola(perso, packet, true);
		imprimir("APARECER OBJETO: PERSO", packet);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {}
	}
	
	public static void ENVIAR_OAKO_APARECER_MUCHOS_OBJETOS(final Personaje perso, final String str) {
		final String packet = "OAKO" + str;
		enviarEnCola(perso, packet, true);
		imprimir("APARECER MUCHOS OBJETO: PERSO", packet);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {}
	}
	
	public static void ENVIAR_OCK_ACTUALIZA_OBJETO(final Personaje perso, final Objeto objeto) {
		if (objeto == null)
			return;
		final String packet = "OCK" + objeto.stringObjetoConGuiño();
		enviarEnCola(perso, packet, true);
		imprimir("ACTUALIZA OBJETO: PERSO", packet);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {}
	}
	
	public static void ENVIAR_ERK_CONSULTA_INTERCAMBIO(final Personaje ss, final int id, final int idT, final int tipo) {
		final String packet = "ERK" + id + "|" + idT + "|" + tipo;
		enviarEnCola(ss, packet, true);
		imprimir("CONSULTA INTERCAMBIO: PERSO", packet);
	}
	
	public static void ENVIAR_ERE_ERROR_CONSULTA(final Personaje ss, final char c) {
		final String packet = "ERE" + c;
		enviarEnCola(ss, packet, true);
		imprimir("CONSULTA ERROR: PERSO", packet);
	}
	
	public static void ENVIAR_EMK_MOVER_OBJETO_LOCAL(final Personaje ss, final char tipoOG, final String signo,
	final String s1) {
		String packet = "EMK" + tipoOG + signo;
		if (!s1.isEmpty()) {
			packet += s1;
		}
		enviarEnCola(ss, packet, true);
		imprimir("MOVER OBJ LOCAL: PERSO", packet);
	}
	
	public static void ENVIAR_EmK_MOVER_OBJETO_DISTANTE(final Personaje perso, final char tipoOG, final String signo,
	final String s1) {
		String packet = "EmK" + tipoOG + signo;
		if (!s1.isEmpty()) {
			packet += s1;
		}
		enviarEnCola(perso, packet, true);
		imprimir("MOVER OBJ DISTANTE: PERSO", packet);
	}
	
	public static void ENVIAR_EmE_ERROR_MOVER_OBJETO_DISTANTE(final Personaje ss, final char tipoOG, final String signo,
	final String s1) {
		String packet = "EmE" + tipoOG + signo;
		if (!s1.isEmpty()) {
			packet += s1;
		}
		enviarEnCola(ss, packet, true);
		imprimir("MOVER OBJ DISTANTE: PERSO", packet);
	}
	
	public static void ENVIAR_EiK_MOVER_OBJETO_TIENDA(final Personaje ss, final char tipoOG, final String signo,
	final String s1) {
		String packet = "EiK" + tipoOG + signo;
		if (!s1.isEmpty()) {
			packet += s1;
		}
		enviarEnCola(ss, packet, true);
		imprimir("MOVER OBJ TIENDA: PERSO", packet);
	}
	
	public static void ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(final Personaje ss, final int tipo, final String objKama,
	final String signo, final String s1) {
		final String packet = "Ep" + tipo + "K" + objKama + signo + s1;
		enviarEnCola(ss, packet, true);
		imprimir("PAGO POR TRABAJO: PERSO", packet);
	}
	
	public static void ENVIAR_ErK_RESULTADO_TRABAJO(final Personaje perso, final String objKama, final String signo,
	final String s1) {
		final String packet = "ErK" + objKama + signo + s1;
		enviarEnCola(perso, packet, true);
		imprimir("RESULTADO TRABAJO: PERSO", packet);
	}
	
	public static void ENVIAR_EK_CHECK_OK_INTERCAMBIO(final Personaje ss, final boolean ok, final int id) {
		final String packet = "EK" + (ok ? "1" : "0") + id;
		enviarEnCola(ss, packet, true);
		imprimir("ACEPTAR INTER: PERSO", packet);
	}
	
	public static void ENVIAR_EV_CERRAR_VENTANAS(final Personaje perso, String exito) {
		final String packet = "EV" + exito;
		enviarEnCola(perso, packet, true);
		imprimir("CERRAR VENTANA: PERSO", packet);
	}
	
	public static void ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(final Personaje perso, final int tipo, final String str) {
		String packet = ("ECK" + tipo);
		if (!str.isEmpty()) {
			packet += ("|" + str);
		}
		enviarEnCola(perso, packet, true);
		imprimir("PANEL INTERCAMBIOS: PERSO", packet);
	}
	
	public static void ENVIAR_El_LISTA_OBJETOS_COFRE_PRECARGADO(final Personaje perso, final Cofre cofre) {
		String lista = cofre.getListaExchanger(perso);
		if (lista.isEmpty()) {
			return;
		}
		final String packet = "El" + lista;
		enviarEnCola(perso, packet, true);
		imprimir("PRECARGA OBJ COFRE: PERSO", packet);
	}
	
	// public static void ENVIAR_EL_LISTA_DE_OBJETO_MERCADILLO_POR_CUENTA(final Personaje perso,
	// Mercadillo mercadillo) {
	// StringBuilder packet = new StringBuilder();
	// for (final ObjetoMercadillo objMerca : mercadillo.getObjetosMercadillos()) {
	// if (objMerca == null) {
	// continue;
	// }
	// if (objMerca.getIDCuenta() != perso.getIDCuenta()) {
	// continue;
	// }
	// if (packet.length() > 0) {
	// packet.append("|");
	// }
	// packet.append(objMerca.analizarParaEL());
	// }
	// packet = new StringBuilder("EL" + packet.toString());
	// enviarEnCola(perso, packet.toString(), true);
	// if (MainServidor.MOSTRAR_ENVIOS) {
	// imprimir("LISTA OBJ MERCADILLO: PERSO", packet.toString());
	// }
	// }
	// public static void ENVIAR_EL_LISTA_OBJETOS_COFRE(final Personaje perso, final Cofre cofre) {
	// String lista = cofre.listaObjCofre();
	// // if (lista.isEmpty()) {
	// // return;
	// // }
	// final String packet = "EL" + lista;
	// enviarEnCola(perso, packet, true);
	// if (MainServidor.MOSTRAR_ENVIOS) {
	// imprimir("LISTA OBJ COFRE: PERSO", packet);
	// }
	// }
	//
	//
	// public static void ENVIAR_EL_LISTA_OBJETOS_NPC(final Personaje ss, final String str) {
	// final String packet = "EL" + str;
	// enviarEnCola(ss, packet, true);
	// if (MainServidor.MOSTRAR_ENVIOS) {
	// imprimir("LISTA OBJ NPC: PERSO", packet);
	// }
	// }
	//
	// public static void ENVIAR_EL_LISTA_OBJETOS_RECAUDADOR(final Personaje ss, final String str) {
	// final String packet = "EL" + str;
	// enviarEnCola(ss, packet, true);
	// if (MainServidor.MOSTRAR_ENVIOS) {
	// imprimir("LISTA OBJ RECAUDADOR: PERSO", packet);
	// }
	// }
	//
	// public static void ENVIAR_EL_LISTA_OBJETOS_DRAGOPAVO(final Personaje ss, final String str) {
	// final String packet = "EL" + str;
	// enviarEnCola(ss, packet, true);
	// if (MainServidor.MOSTRAR_ENVIOS) {
	// imprimir("LISTA MOCHILA DP: PERSO", packet);
	// }
	// }
	//
	public static void ENVIAR_EL_LISTA_EXCHANGER(final Personaje perso, final Exchanger exchanger) {
		final String packet = "EL" + exchanger.getListaExchanger(perso);
		enviarEnCola(perso, packet, true);
		imprimir("LISTA TIENDA PJ: PERSO", packet);
	}
	
	public static void ENVIAR_PIE_ERROR_INVITACION_GRUPO(final Personaje ss, final String s) {
		final String packet = "PIE" + s;
		enviarEnCola(ss, packet, true);
		imprimir("ERROR INVIT GRUPO: PERSO", packet);
	}
	
	public static void ENVIAR_PIK_INVITAR_GRUPO(final Personaje perso, final String n1, final String n2) {
		final String packet = "PIK" + n1 + "|" + n2;
		enviarEnCola(perso, packet, true);
		imprimir("INVITAR AL GRUPO: PERSO", packet);
	}
	
	public static void ENVIAR_PCK_CREAR_GRUPO(final Personaje perso, final String s) {
		final String packet = "PCK" + s;
		enviarEnCola(perso, packet, true);
		imprimir("CREAR GRUPO: PERSO", packet);
	}
	
	public static void ENVIAR_PL_LIDER_GRUPO(final Personaje perso, final int id) {
		final String packet = "PL" + id;
		enviarEnCola(perso, packet, true);
		imprimir("LIDER GRUPO: PERSO", packet);
	}
	
	public static void ENVIAR_PR_RECHAZAR_INVITACION_GRUPO(final Personaje perso) {
		String packet = "PR";
		enviarEnCola(perso, packet, true);
		imprimir("RECHAZ INVIT GRUPO: PERSO", packet);
	}
	
	public static void ENVIAR_PA_ACEPTAR_INVITACION_GRUPO(final Personaje perso) {
		final String packet = "PA";
		enviarEnCola(perso, packet, true);
		imprimir("ACEPTAR INVIT GRUPO: PERSO", packet);
	}
	
	public static void ENVIAR_PV_DEJAR_GRUPO(final Personaje perso, final String s) {
		final String packet = "PV" + s;
		enviarEnCola(perso, packet, true);
		imprimir("DEJAR GRUPO: PERSO", packet);
	}
	
	public static void ENVIAR_PM_TODOS_MIEMBROS_GRUPO_A_GRUPO(final Grupo grupo) {
		String packet = "";
		for (final Personaje pj : grupo.getMiembros()) {
			if (!packet.isEmpty()) {
				packet += "|";
			}
			packet += pj.stringInfoGrupo();
		}
		packet = "PM+" + packet;
		for (final Personaje pj : grupo.getMiembros()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("MIEMBROS GRUPO: GRUPO", packet);
	}
	
	public static void ENVIAR_PM_TODOS_MIEMBROS_GRUPO_A_PERSO(final Personaje perso, final Grupo grupo) {
		String packet = "";
		for (final Personaje pj : grupo.getMiembros()) {
			if (!packet.isEmpty()) {
				packet += "|";
			}
			packet += pj.stringInfoGrupo();
		}
		packet = "PM+" + packet;
		enviarEnCola(perso, packet, true);
		imprimir("MIEMBROS GRUPO: PERSO", packet);
	}
	
	public static void ENVIAR_PM_AGREGAR_PJ_GRUPO_A_GRUPO(final Grupo grupo, final String s) {
		final String packet = "PM+" + s;
		for (final Personaje pj : grupo.getMiembros()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("AGREGAR PJ GRUPO: GRUPO", packet);
	}
	
	public static void ENVIAR_PM_ACTUALIZAR_INFO_PJ_GRUPO(final Grupo grupo, final String s) {
		final String packet = "PM~" + s;
		for (final Personaje pj : grupo.getMiembros()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("ACTUALIZAR INFO GRUPO: GRUPO", packet);
	}
	
	public static void ENVIAR_PM_EXPULSAR_PJ_GRUPO(final Grupo grupo, final int id) {
		final String packet = "PM-" + id;
		for (final Personaje pj : grupo.getMiembros()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("EXPULSAR PJ GRUPO: GRUPO", packet);
	}
	
	public static void ENVIAR_kIE_ERROR_INVITACION_KOLISEO(final Personaje ss, final String s) {
		final String packet = "kIE" + s;
		enviarEnCola(ss, packet, true);
		imprimir("ERROR INVIT GRUPO: PERSO", packet);
	}
	
	public static void ENVIAR_kIK_INVITAR_KOLISEO(final Personaje perso, final String n1, final String n2) {
		final String packet = "kIK" + n1 + "|" + n2;
		enviarEnCola(perso, packet, true);
		imprimir("INVITAR AL KOLISEO: PERSO", packet);
	}
	
	public static void ENVIAR_kR_RECHAZAR_INVITACION_KOLISEO(final Personaje ss) {
		final String packet = "kR";
		enviarEnCola(ss, packet, true);
		imprimir("RECHAZ INVIT KOLISEO: PERSO", packet);
	}
	
	public static void ENVIAR_kA_ACEPTAR_INVITACION_KOLISEO(final Personaje ss) {
		final String packet = "kA";
		enviarEnCola(ss, packet, true);
		imprimir("ACEPTAR INVIT KOLISEO: PERSO", packet);
	}
	
	public static void ENVIAR_kP_PANEL_KOLISEO(final Personaje ss) {
		final String packet = "kP" + Mundo.cantKoliseo() + ";" + Mundo.SEGUNDOS_INICIO_KOLISEO;
		enviarEnCola(ss, packet, true);
		imprimir("PANEL KOLISEO: PERSO", packet);
	}
	
	public static void ENVIAR_kV_DEJAR_KOLISEO(final Personaje perso) {
		final String packet = "kV";
		enviarEnCola(perso, packet, true);
		imprimir("DEJAR KOLISEO: PERSO", packet);
	}
	
	public static void ENVIAR_kCK_CREAR_KOLISEO(final Personaje perso) {
		final String packet = "kCK";
		enviarEnCola(perso, packet, true);
		imprimir("CREAR KOLISEO: PERSO", packet);
	}
	
	public static void ENVIAR_kM_TODOS_MIEMBROS_KOLISEO(final Personaje perso, final GrupoKoliseo grupo) {
		String packet = ("kM+");
		boolean primero = true;
		for (final Personaje pj : grupo.getMiembros()) {
			if (!primero) {
				packet += "|";
			}
			packet += (pj.stringInfoGrupo());
			primero = false;
		}
		enviarEnCola(perso, packet, true);
		imprimir("MIEMBROS GRUPO: PERSO", packet);
	}
	
	public static void ENVIAR_kM_AGREGAR_PJ_KOLISEO(final GrupoKoliseo grupo, final String s) {
		final String packet = "kM+" + s;
		for (final Personaje pj : grupo.getMiembros()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("AGREGAR PJ GRUPO: GRUPO", packet);
	}
	
	public static void ENVIAR_kM_ACTUALIZAR_INFO_PJ_KOLISEO(final GrupoKoliseo grupo, final String s) {
		final String packet = "kM~" + s;
		for (final Personaje pj : grupo.getMiembros()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("ACTUALIZAR INFO GRUPO: GRUPO", packet);
	}
	
	public static void ENVIAR_kM_EXPULSAR_PJ_KOLISEO(final GrupoKoliseo grupo, final int id) {
		final String packet = "kM-" + id;
		for (final Personaje pj : grupo.getMiembros()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("EXPULSAR PJ GRUPO: GRUPO", packet);
	}
	
	public static void ENVIAR_fD_DETALLES_PELEA(final Personaje ss, final Pelea pelea) {
		if (pelea == null) {
			return;
		}
		String packet = ("fD" + pelea.getID() + "|");
		for (final Luchador luchador : pelea.luchadoresDeEquipo(1)) {
			if (luchador.esInvocacion()) {
				continue;
			}
			packet += (luchador.getNombre() + "~" + luchador.getNivel() + ";");
		}
		packet += ("|");
		for (final Luchador luchador : pelea.luchadoresDeEquipo(2)) {
			if (luchador.esInvocacion()) {
				continue;
			}
			packet += (luchador.getNombre() + "~" + luchador.getNivel() + ";");
		}
		enviarEnCola(ss, packet, true);
		imprimir("DETALLES PELEA: PERSO", packet);
	}
	
	public static void ENVIAR_IQ_NUMERO_ARRIBA_PJ(final Personaje perso, final int idPerso, final int numero) {
		final String packet = "IQ" + idPerso + "|" + numero;
		enviarEnCola(perso, packet, true);
		imprimir("NUMERO ARRIBA PJ: PERSO", packet);
	}
	
	public static void ENVIAR_JN_OFICIO_NIVEL(final Personaje perso, final int oficioID, final int nivel) {
		final String packet = "JN" + oficioID + "|" + nivel;
		enviarEnCola(perso, packet, true);
		imprimir("OFICIO NIVEL: PERSO", packet);
	}
	
	// private static void ENVIAR_GDF_OBJETOS_INTERACTIVOS(final Personaje ss, final Mapa mapa) {
	// final String packet = mapa.getObjetosInteracGDF();
	// if (packet.isEmpty()) {
	// return;
	// }
	// enviar(ss, packet, true);
	// if (Bustemu.MOSTRAR_ENVIOS) {
	// imprimir("OBJ INTERACTIVOS: PERSO" , packet);
	// }
	// }
	public static void ENVIAR_GDF_ESTADO_OBJETO_INTERACTIVO(final Mapa mapa, final Celda celda) {
		String packet = "GDF|" + celda.getID() + ";";
		if (celda.getObjetoInteractivo() == null) {
			packet += celda.getEstado();
		} else {
			packet += celda.getObjetoInteractivo().getInfoPacket();
		}
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("ESTADO OBJ INTERACTIVO: MAPA", packet);
	}
	
	public static void ENVIAR_GDF_FORZADO_MAPA(final Mapa mapa, final String str) {
		final String packet = "GDF|" + str;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("EST OBJ INTER FORZADO: MAPA", packet);
	}
	
	public static void ENVIAR_GDF_FORZADO_PERSONAJE(final Personaje perso, final String str) {
		final String packet = "GDF|" + str;
		enviarEnCola(perso, packet, true);
		imprimir("EST OBJ INTER FORZADO: MAPA", packet);
	}
	
	public static void ENVIAR_GA_ACCION_JUEGO_AL_MAPA(final Mapa mapa, final int idUnica, final int idAccionModelo,
	final String s1, final String s2) {
		String packet = "GA" + (idUnica <= -1 ? "" : idUnica) + ";" + idAccionModelo + ";" + s1;
		if (!s2.isEmpty()) {
			packet += ";" + s2;
		}
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("ACCION JUEGO: MAPA", packet);
	}
	
	public static void ENVIAR_GA_MOVER_SPRITE_MAPA(final Mapa mapa, final int idUnica, final int idAccionModelo,
	final String s1, final String s2) {
		String packet = "GA" + (idUnica <= -1 ? "" : idUnica) + ";" + idAccionModelo + ";" + s1;
		if (!s2.isEmpty()) {
			packet += ";" + s2;
		}
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			enviarEnCola(pj, packet, true);
		}
	}
	
	// public static void ENVIAR_EL_LISTA_OBJETOS_BANCO(final Personaje perso) {
	// final String packet = "EL" + perso.listaObjBanco();
	// enviarEnCola(perso, packet, true);
	// if (MainServidor.MOSTRAR_ENVIOS) {
	// imprimir("LISTA OBJ BANCO: PERSO", packet);
	// }
	// }
	//
	// public static void ENVIAR_El_LISTA_OBJETOS_BANCO_PRECARGADO(final Personaje perso) {
	// final String packet = "El" + perso.listaObjBanco();
	// enviarEnCola(perso, packet, true);
	// }
	public static void ENVIAR_JX_EXPERINENCIA_OFICIO(final Personaje perso, final Collection<StatOficio> oficios) {
		String packet = ("JX");
		for (final StatOficio statOficio : oficios) {
			if (statOficio.getPosicion() != 7) {
				packet += ("|" + statOficio.getOficio().getID() + ";" + statOficio.getNivel() + ";" + statOficio.getExpString(
				";") + ";");
			}
		}
		enviarEnCola(perso, packet, true);
		imprimir("EXPERIENCIA OFICIO: PERSO", packet);
	}
	
	public static void ENVIAR_JX_EXPERINENCIA_OFICIO(final Personaje perso, StatOficio statOficio) {
		final String packet = "JX" + "|" + statOficio.getOficio().getID() + ";" + statOficio.getNivel() + ";" + statOficio
		.getExpString(";") + ";";
		enviarEnCola(perso, packet, true);
		imprimir("EXPERIENCIA OFICIO: PERSO", packet);
	}
	
	public static void ENVIAR_JO_OFICIO_OPCIONES(final Personaje perso, final Collection<StatOficio> oficios) {
		for (final StatOficio statOficio : oficios) {
			if (statOficio.getPosicion() == 7) {
				continue;
			}
			final String packet = "JO" + statOficio.getPosicion() + "|" + statOficio.getOpcionBin() + "|" + statOficio
			.getSlotsPublico();
			enviarEnCola(perso, packet, true);
			imprimir("OFICIO OPCIONES: PERSO", packet);
		}
	}
	
	public static void ENVIAR_JO_OFICIO_OPCIONES(final Personaje perso, final StatOficio statOficio) {
		final String packet = "JO" + statOficio.getPosicion() + "|" + statOficio.getOpcionBin() + "|" + statOficio
		.getSlotsPublico();
		enviarEnCola(perso, packet, true);
		imprimir("OFICIO OPCIONES: PERSO", packet);
	}
	
	public static void ENVIAR_EJ_DESCRIPCION_LIBRO_ARTESANO(final Personaje perso, final String str) {
		final String packet = "EJ" + str;
		enviarEnCola(perso, packet, true);
		imprimir("DESCRIP LIBRO ARTESANO: PERSO", packet);
	}
	
	public static void ENVIAR_Ej_AGREGAR_LIBRO_ARTESANO(final Personaje perso, final String str) {
		final String packet = "Ej" + str;
		enviarEnCola(perso, packet, true);
		imprimir("AGREG LIBRO ARTESANO: PERSO", packet);
	}
	
	public static void ENVIAR_JS_SKILLS_DE_OFICIO(final Personaje perso, final Collection<StatOficio> oficios) {
		String packet = ("JS");
		for (final StatOficio statOficio : oficios) {
			if (statOficio.getPosicion() != 7) {
				packet += (statOficio.stringSKillsOficio());
			}
		}
		enviarEnCola(perso, packet, true);
		imprimir("TRABAJO POR OFICIO: PERSO", packet);
	}
	
	public static void ENVIAR_JS_SKILL_DE_OFICIO(final Personaje perso, StatOficio statsOficios) {
		final String packet = "JS" + statsOficios.stringSKillsOficio();
		enviarEnCola(perso, packet, true);
		imprimir("TRABAJO POR OFICIO: PERSO", packet);
	}
	
	public static void ENVIAR_JR_OLVIDAR_OFICIO(final Personaje perso, final int id) {
		final String packet = "JR" + id;
		enviarEnCola(perso, packet, true);
		imprimir("OLVIDAR OFICIO: PERSO", packet);
	}
	
	public static void ENVIAR_EsK_MOVER_A_TIENDA_COFRE_BANCO(final Personaje perso, final String str) {
		final String packet = "EsK" + str;
		enviarEnCola(perso, packet, true);
		imprimir("MOVER OBJ: PERSO", packet);
	}
	
	public static void ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(final Pelea pelea, final int equipos, final int id,
	final short celdaID) {
		final String packet = "Gf" + id + "|" + celdaID;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("MOSTRAR CELDA: PELEA", packet);
	}
	
	public static void ENVIAR_Gf_MOSTRAR_CELDA(final Personaje perso, final int id, final short celdaID) {
		final String packet = "Gf" + id + "|" + celdaID;
		enviarEnCola(perso, packet, true);
		imprimir("MOSTRAR CELDA: PERSO", packet);
	}
	
	public static void ENVIAR_Ea_MENSAJE_RECETAS(final Personaje perso, final byte cant) {
		final String packet = "Ea" + cant;
		enviarEnCola(perso, packet, true);
		imprimir("TERMINOS RECETAS: PERSO", packet);
	}
	
	public static void ENVIAR_EA_TURNO_RECETA(final Personaje perso, final int cant) {
		final String packet = "EA" + cant;
		enviarEnCola(perso, packet, true);
		imprimir("TURNO RECETA: PERSO", packet);
	}
	
	public static void ENVIAR_Ec_RESULTADO_RECETA(final Personaje perso, final String str) {
		final String packet = "Ec" + str;
		enviarEnCola(perso, packet, true);
		imprimir("INICIAR RECETA: PERSO", packet);
	}
	
	public static void ENVIAR_IO_ICONO_OBJ_INTERACTIVO(final Mapa mapa, final int id, final String str) {
		final String packet = "IO" + id + "|" + str;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("ICONO OBJ INTERACTIVO: MAPA", packet);
	}
	
	public static void ENVIAR_FL_LISTA_DE_AMIGOS(final Personaje perso) {
		String packet = "FL" + perso.getCuenta().stringListaAmigos();
		enviarEnCola(perso, packet, true);
		imprimir("AMIGOS LINEA: PERSO", packet);
		if (perso.getEsposoID() != 0) {
			packet = "FS" + perso.getEsposoListaAmigos();
			enviarEnCola(perso, packet, true);
			imprimir("ESPOSO: PERSO", packet);
		}
	}
	
	public static void ENVIAR_Im0143_AMIGO_CONECTADO(final Personaje perso, String str) {
		final String packet = "Im0143;" + str;
		enviarEnCola(perso, packet, true);
		imprimir("MENSAJE AMIGO CONECTADO: PERSO", packet);
	}
	
	public static void ENVIAR_FA_AGREGAR_AMIGO(final Personaje perso, final String str) {
		final String packet = "FA" + str;
		enviarEnCola(perso, packet, true);
		imprimir("AGREGAR AMIGO: PERSO", packet);
	}
	
	public static void ENVIAR_FD_BORRAR_AMIGO(final Personaje perso, final String str) {
		final String packet = "FD" + str;
		enviarEnCola(perso, packet, true);
		imprimir("BORRAR AMIGO: PERSO", packet);
	}
	
	public static void ENVIAR_iA_AGREGAR_ENEMIGO(final Personaje perso, final String str) {
		final String packet = "iA" + str;
		enviarEnCola(perso, packet, true);
		imprimir("AGREGAR ENEMIGO: PERSO", packet);
	}
	
	public static void ENVIAR_iD_BORRAR_ENEMIGO(final Personaje perso, final String str) {
		final String packet = "iD" + str;
		enviarEnCola(perso, packet, true);
		imprimir("BORRAR ENEMIGO: PERSO", packet);
	}
	
	public static void ENVIAR_iL_LISTA_ENEMIGOS(final Personaje perso) {
		final String packet = "iL" + perso.getCuenta().stringListaEnemigos();
		enviarEnCola(perso, packet, true);
		imprimir("LISTA ENEMIGOS: PERSO", packet);
	}
	
	public static void ENVIAR_Rp_INFORMACION_CERCADO(final Personaje perso, final Cercado cercado) {
		String packet = "";
		if (cercado == null) {
			return;
		}
		packet = "Rp" + cercado.getDueñoID() + ";" + cercado.getPrecio() + ";" + cercado.getCapacidadMax() + ";" + cercado
		.getCantObjMax() + ";";
		final Gremio gremio = cercado.getGremio();
		if (gremio != null) {
			packet += gremio.getNombre() + ";" + gremio.getEmblema();
		} else {
			packet += ";";
		}
		enviarEnCola(perso, packet, true);
		imprimir("INFO CERCADO: PERSO", packet);
	}
	
	public static void ENVIAR_OS_BONUS_SET(final Personaje perso, final int setID, final int numero) {
		String packet = ("OS");
		int num = 0;
		if (numero != -1) {
			num = numero;
		} else {
			num = perso.getNroObjEquipadosDeSet(setID);
		}
		final ObjetoSet OS = Mundo.getObjetoSet(setID);
		if (num == 0 || OS == null) {
			packet += ("-" + setID);
		} else {
			packet += ("+" + setID + "|");
			String objetos = "";
			for (final ObjetoModelo OM : OS.getObjetosModelos()) {
				if (perso.tieneObjModeloEquipado(OM.getID())) {
					if (objetos.length() > 0) {
						objetos += (";");
					}
					objetos += (OM.getID());
				}
			}
			packet += (objetos + "|" + OS.getBonusStatPorNroObj(num).convertirStatsAString());
		}
		enviarEnCola(perso, packet, true);
		imprimir("BONUS SET: PERSO", packet);
	}
	
	public static void ENVIAR_Re_DETALLES_MONTURA(final Personaje perso, final String simbolo, final Montura dragopavo) {
		String packet = "Re" + simbolo;
		if (simbolo.equals("+") && dragopavo != null) {
			packet += dragopavo.detallesMontura();
		}
		enviarEnCola(perso, packet, true);
		imprimir("DETALLE MONTURA: PERSO", packet);
	}
	
	public static void ENVIAR_Rd_DESCRIPCION_MONTURA(final Personaje perso, final Montura dragopavo) {
		final String packet = "Rd" + dragopavo.detallesMontura();
		enviarEnCola(perso, packet, true);
		imprimir("DESCRIPCION MONTURA: PERSO", packet);
	}
	
	public static void ENVIAR_Rr_ESTADO_MONTADO(final Personaje perso, final String montado) {
		final String packet = "Rr" + montado;
		enviarEnCola(perso, packet, true);
		imprimir("ESTADO MONTADO: PERSO", packet);
	}
	
	public static void ENVIAR_AC_CAMBIAR_CLASE(final Personaje perso, final int clase) {
		final String packet = "AC" + clase;
		enviarEnCola(perso, packet, true);
		imprimir("CAMBIAR CLASE: PERSO", packet);
	}
	
	public static void ENVIAR_AI_CAMBIAR_ID(final Personaje perso, final int id) {
		final String packet = "AI" + id;
		enviarEnCola(perso, packet, true);
		imprimir("CAMBIAR ID: PERSO", packet);
	}
	
	public static void ENVIAR_Rz_STATS_VIP(final Personaje perso, final String stats) {
		final String packet = "Rz" + stats;
		enviarEnCola(perso, packet, true);
		imprimir("STATS VIP: PERSO", packet);
	}
	
	public static void ENVIAR_GM_BORRAR_GM_A_MAPA(final Mapa mapa, final int id) {
		final String packet = "GM|-" + id;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("BORRAR PJ: MAPA ID " + mapa.getID() + ": MAPA", packet);
	}
	
	public static void ENVIAR_GM_BORRAR_LUCHADOR(final Pelea pelea, final int id, final int equipos) {
		final String packet = "GM|-" + id;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			if (luchador.getPersonaje() == null || luchador.getID() == id) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("BORRRA LUCH: PELEA ID " + pelea.getID() + ": PELEA", packet);
	}
	
	public static void ENVIAR_GM_REFRESCAR_PJ_EN_MAPA(final Mapa mapa, final Personaje perso) {
		final String packet = "GM|~" + perso.stringGM();
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("REFRESCAR PJ: MAPA", packet);
	}
	
	public static void ENVIAR_GM_REFRESCAR_PJ_EN_MAPA_SIN_HUMO(final Mapa mapa, final Personaje perso) {
		final String packet = "GM|-" + perso.getID() + (char) 0x00 + "GM|+" + perso.stringGM();
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("REFRESCAR PJ: MAPA", packet);
	}
	
	public static void ENVIAR_GM_REFRESCAR_LUCHADOR_EN_PELEA(final Pelea pelea, final Luchador luch) {
		final String packet = "GM|~" + luch.stringGM(0);
		for (final Luchador luchador : pelea.luchadoresDeEquipo(3)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("REFRESCAR PJ: PELEA", packet);
	}
	
	public static void ENVIAR_GM_LUCHADORES_A_PERSO(final Pelea pelea, final Mapa mapa, final Personaje perso) {
		final String packet = mapa.getGMsLuchadores(perso.getID());
		if (packet.isEmpty()) {
			return;
		}
		enviarEnCola(perso, packet, true);
		imprimir("GM LUCHADORES: PERSO", packet);
	}
	
	public static void ENVIAR_GM_LUCHADORES_A_PELEA(final Pelea pelea, final int equipos, final Mapa mapa) {
		final String packet = mapa.getGMsLuchadores(0);
		if (packet.isEmpty()) {
			return;
		}
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("GM LUCHADORES: PELEA", packet);
	}
	
	public static void ENVIAR_GM_PERSONAJES_MAPA_A_PERSO(final Mapa mapa, final Personaje perso) {
		final String packet = mapa.getGMsPersonajes(perso);
		if (packet.isEmpty()) {
			return;
		}
		enviarEnCola(perso, packet, true);
		imprimir("GM PERSONAJE: MAPA", packet);
	}
	
	public static void ENVIAR_GM_JUGADOR_UNIRSE_PELEA(final Pelea pelea, final int equipos, final Luchador luch) {
		final String packet = "GM|+" + luch.stringGM(0);
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador == luch || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("LUCH UNIR PELEA: PELEA", packet);
	}
	
	public static void ENVIAR_GM_MERCANTE_A_MAPA(final Mapa mapa, final String str) {
		final String packet = "GM|" + str;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("GM MERCANTE: MAPA", packet);
	}
	
	public static void ENVIAR_GM_PJ_A_MAPA(final Mapa mapa, final Personaje perso) {
		final String packet = "GM|+" + perso.stringGM();
		boolean i = perso.esIndetectable();
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() != null) {
				continue;
			}
			if (i && !pj.esIndetectable()) {
				continue;
			}
			enviarEnCola(pj, packet, true);
		}
		imprimir("AGREGAR PJ: MAPA ID " + mapa.getID() + ": MAPA", packet);
	}
	
	public static void ENVIAR_GM_GRUPOMOB_A_MAPA(final Mapa mapa, final String str) {
		final String packet = "GM|" + str;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("GM GRUPOMOB: MAPA", packet);
	}
	
	public static void ENVIAR_GM_DRAGOPAVO_A_MAPA(final Mapa mapa, String signo, Montura montura) {
		final String packet = "GM|" + signo + montura.stringGM();
		boolean esPublico = mapa.getCercado().esPublico();
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (esPublico && montura.getDueñoID() != pj.getID()) {
				continue;
			}
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("GM DRAGOPAVO: MAPA", packet);
	}
	
	public static void ENVIAR_GM_PRISMA_A_MAPA(final Mapa mapa, final String str) {
		final String packet = "GM|" + str;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("GM PRISMA: MAPA", packet);
	}
	
	public static void ENVIAR_GM_NPC_A_MAPA(final Mapa mapa, char signo, final String str) {
		final String packet = "GM|" + signo + str;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("GM AGREGAR NPC: MAPA", packet);
	}
	
	public static void ENVIAR_GM_RECAUDADOR_A_MAPA(final Mapa mapa, final String str) {
		final String packet = "GM|" + str;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("GM AGREGAR RECAUDADOR: MAPA", packet);
	}
	
	public static void ENVIAR_As_STATS_DEL_PJ(final Personaje perso) {
		final String packet = perso.stringStats();
		enviar(perso, packet);
		ENVIAR_Ab_CIRCULO_XP_BANNER(perso);
		imprimir("STATS COMPLETO PJ: PERSO", packet);
	}
	
	public static void ENVIAR_Ak_KAMAS_PDV_EXP_PJ(final Personaje perso) {
		final String packet = perso.stringStats2();
		enviar(perso, packet);
		ENVIAR_Ab_CIRCULO_XP_BANNER(perso);
		imprimir("STATS KAMAS PDV EXP PJ: PERSO", packet);
	}
	
	public static void ENVIAR_Ab_CIRCULO_XP_BANNER(final Personaje perso) {
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {}
		enviarEnCola(perso, "Ab", true);
	}
	
	public static void ENVIAR_Rx_EXP_DONADA_MONTURA(final Personaje perso) {
		final String packet = "Rx" + perso.getPorcXPMontura();
		enviarEnCola(perso, packet, true);
		imprimir("XP DONADA MONTURA: PERSO", packet);
	}
	
	public static void ENVIAR_Rn_CAMBIO_NOMBRE_MONTURA(final Personaje perso, final String nombre) {
		final String packet = "Rn" + nombre;
		enviarEnCola(perso, packet, true);
		imprimir("CAMBIO NOMBRE MONTURA: PERSO", packet);
	}
	
	public static void ENVIAR_Ee_MONTURA_A_ESTABLO(final Personaje perso, final char c, final String s) {
		String packet = "Ee" + c + s;
		enviarEnCola(perso, packet, true);
		imprimir("PANEL MONTURA A ESTABLO: PERSO", packet);
	}
	
	public static void ENVIAR_Ef_MONTURA_A_CRIAR(final Personaje perso, final char c, final String s) {
		String packet = "Ef" + c + s;
		enviarEnCola(perso, packet, true);
		imprimir("PANEL MONTURA A CRIAR: PERSO", packet);
	}
	
	public static void ENVIAR_cC_SUSCRIBIR_CANAL(final Personaje perso, final char c, final String s) {
		String packet = "cC" + c + s;
		enviarEnCola(perso, packet, true);
		imprimir("SUSCRIBIR CANAL: PERSO", packet);
	}
	
	public static void ENVIAR_GDO_OBJETO_TIRAR_SUELO(final Mapa mapa, final char agre_borr, final short celda,
	final int idObjetoMod, final boolean gigante, String durabilidad) {
		String packet = "GDO" + agre_borr + celda + (agre_borr == '-'
		? ""
		: (";" + idObjetoMod + ";" + (gigante ? (1 + (durabilidad.isEmpty() ? "" : ";" + durabilidad)) : 0)));
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			if (pj.getPelea() == null) {
				enviarEnCola(pj, packet, true);
			}
		}
		imprimir("OBJ TIRADO SUELO: MAPA", packet);
	}
	
	public static void ENVIAR_GDO_OBJETO_TIRAR_SUELO(final Personaje perso, final char agre_borr, final short celda,
	final int idObjetoMod, final boolean gigante, String durabilidad) {
		String packet = "GDO" + agre_borr + celda + (agre_borr == '-'
		? ""
		: (";" + idObjetoMod + ";" + (gigante ? (1 + (durabilidad.isEmpty() ? "" : ";" + durabilidad)) : 0)));
		enviarEnCola(perso, packet, true);
		imprimir("OBJ TIRADO SUELO: PERSO", packet);
	}
	
	public static void ENVIAR_ZC_CAMBIAR_ESPECIALIDAD_ALINEACION(final Personaje perso, final int especialidad) {
		String packet = "ZC" + especialidad;
		enviarEnCola(perso, packet, true);
		imprimir("CAMBIAR ESPEC. ALIN.: PERSO", packet);
	}
	
	public static void ENVIAR_ZS_SET_ESPECIALIDAD_ALINEACION(final Personaje ss, final int especialidad) {
		final String packet = "ZS" + especialidad;
		enviarEnCola(ss, packet, true);
		imprimir("SET ESPEC. ALIN.: PERSO", packet);
	}
	
	public static void ENVIAR_GIP_ACT_DES_ALAS_PERDER_HONOR(final Personaje perso, final int a) {
		String packet = "GIP" + a;
		enviarEnCola(perso, packet, true);
		imprimir("ACT. ALAS HONOR: PERSO", packet);
	}
	
	public static void ENVIAR_gn_CREAR_GREMIO(final Personaje perso) {
		String packet = "gn";
		enviarEnCola(perso, packet, true);
		imprimir("CREAR GREMIO: PERSO", packet);
	}
	
	public static void ENVIAR_gC_CREAR_PANEL_GREMIO(final Personaje perso, final String s) {
		String packet = "gC" + s;
		enviarEnCola(perso, packet, true);
		imprimir("CREAR PANEL GREMIO: PERSO", packet);
	}
	
	public static void ENVIAR_gV_CERRAR_PANEL_GREMIO(final Personaje perso) {
		String packet = "gV";
		enviarEnCola(perso, packet, true);
		imprimir("CERRAR PANEL GREMIO: PERSO", packet);
	}
	
	public static void ENVIAR_gIM_GREMIO_INFO_MIEMBROS(final Personaje perso, final Gremio g, final char c) {
		String packet = "gIM" + c;
		switch (c) {
			case '+' :
				try {
					packet += g.analizarMiembrosGM();
				} catch (final NullPointerException localNullPointerException) {}
				break;
			case '-' :
				try {
					packet += g.analizarMiembrosGM();
				} catch (final NullPointerException localNullPointerException1) {}
				break;
		}
		enviarEnCola(perso, packet, true);
		imprimir("INFO MIEMBROS GREMIO: PERSO", packet);
	}
	
	public static void ENVIAR_gIB_GREMIO_INFO_BOOST(final Personaje perso, final String infos) {
		String packet = "gIB" + infos;
		enviarEnCola(perso, packet, true);
		imprimir("INFO BOOST GREMIO: PERSO", packet);
	}
	
	public static void ENVIAR_gIH_GREMIO_INFO_CASAS(final Personaje perso, final String infos) {
		String packet = "gIH" + infos;
		enviarEnCola(perso, packet, true);
		imprimir("INFO CASAS GREMIO: PERSO", packet);
	}
	
	public static void ENVIAR_gS_STATS_GREMIO(final Personaje perso, final MiembroGremio miembro) {
		final Gremio gremio = miembro.getGremio();
		final String packet = "gS" + gremio.getNombre() + "|" + gremio.getEmblema().replace(',', '|') + "|" + miembro
		.analizarDerechos();
		enviarEnCola(perso, packet, true);
		imprimir("GREMIO STATS: PERSO", packet);
	}
	
	public static void ENVIAR_gJ_GREMIO_UNIR(final Personaje perso, final String str) {
		String packet = "gJ" + str;
		enviarEnCola(perso, packet, true);
		imprimir("UNIR GREMIO: PERSO", packet);
	}
	
	public static void ENVIAR_gK_GREMIO_BAN(final Personaje perso, final String str) {
		String packet = "gK" + str;
		enviarEnCola(perso, packet, true);
		imprimir("BAN GREMIO: PERSO", packet);
	}
	
	public static void ENVIAR_gIG_GREMIO_INFO_GENERAL(final Personaje perso, final Gremio gremio) {
		if (gremio == null) {
			return;
		}
		String packet = gremio.infoPanelGremio();
		enviarEnCola(perso, packet, true);
		imprimir("INFO GENERAL GREMIO: PERSO", packet);
	}
	
	public static void ENVIAR_WC_MENU_ZAAP(final Personaje perso) {
		final String packet = "WC" + perso.listaZaap();
		enviarEnCola(perso, packet, true);
		imprimir("MENU ZAAP: PERSO", packet);
	}
	
	public static void ENVIAR_Wp_MENU_PRISMA(final Personaje perso) {
		final String packet = "Wp" + perso.listaPrismas();
		enviarEnCola(perso, packet, true);
		imprimir("MENU PRISMA: PERSO", packet);
	}
	
	public static void ENVIAR_WV_CERRAR_ZAAP(final Personaje perso) {
		String packet = "WV";
		enviarEnCola(perso, packet, true);
		imprimir("CERRAR ZAAP: PERSO", packet);
	}
	
	public static void ENVIAR_Ww_CERRAR_PRISMA(final Personaje perso) {
		String packet = "Ww";
		enviarEnCola(perso, packet, true);
		imprimir("CERRAR PRISMA: PERSO", packet);
	}
	
	public static void ENVIAR_Wv_CERRAR_ZAPPI(final Personaje perso) {
		String packet = "Wv";
		enviarEnCola(perso, packet, true);
		imprimir("CERRAR ZAAPIS: PERSO", packet);
	}
	
	public static void ENVIAR_zV_CERRAR_ZONAS(final Personaje perso) {
		String packet = "zV";
		enviarEnCola(perso, packet, true);
		imprimir("CERRAR ZONAS: PERSO", packet);
	}
	
	public static void ENVIAR_zC_LISTA_ZONAS(final Personaje perso) {
		String packet = "zC" + Mundo.LISTA_ZONAS;
		enviarEnCola(perso, packet, true);
		imprimir("LISTA ZONAS: PERSO", packet);
	}
	
	public static void ENVIAR_Wc_LISTA_ZAPPIS(final Personaje perso, final String lista) {
		String packet = "Wc" + lista;
		enviarEnCola(perso, packet, true);
		imprimir("MENU ZAAPIS: PERSO", packet);
	}
	
	public static void ENVIAR_WUE_ZAPPI_ERROR(final Personaje perso) {
		String packet = "WUE";
		enviarEnCola(perso, packet, true);
		imprimir("ERROR ZAPPI: ENVIAR", packet);
	}
	
	public static void ENVIAR_eL_LISTA_EMOTES(final Personaje perso, final int s) {
		String packet = "eL" + s;
		enviarEnCola(perso, packet, true);
		imprimir("LISTA EMOTES: PERSO", packet);
	}
	
	public static void ENVIAR_eA_AGREGAR_EMOTE(final Personaje perso, final int s, final boolean mostrar) {
		ENVIAR_eL_LISTA_EMOTES(perso, perso.getEmotes());
		String packet = "eA" + s + "|" + (mostrar ? 1 : 0);
		enviarEnCola(perso, packet, true);
		imprimir("AGREGAR EMOTE: PERSO", packet);
	}
	
	public static void ENVIAR_eR_BORRAR_EMOTE(final Personaje perso, final int s, final boolean mostrar) {
		ENVIAR_eL_LISTA_EMOTES(perso, perso.getEmotes());
		String packet = "eR" + s + "|" + (mostrar ? 1 : 0);
		enviarEnCola(perso, packet, true);
		imprimir("BORRAR EMOTE: PERSO", packet);
	}
	
	public static void ENVIAR_eUE_EMOTE_ERROR(final Personaje perso) {
		String packet = "eUE";
		enviarEnCola(perso, packet, true);
		imprimir("EMOTE ERROR: PERSO", packet);
	}
	
	public static void ENVIAR_BWK_QUIEN_ES(final Personaje perso, final String str) {
		String packet = "BWK" + str;
		enviarEnCola(perso, packet, true);
		imprimir("QUIEN ES: PERSO", packet);
	}
	
	public static void ENVIAR_KCK_VENTANA_CLAVE(final Personaje perso, boolean modificar, byte cant) {
		String packet = "KCK" + (modificar ? 1 : 0) + "|" + cant;
		enviarEnCola(perso, packet, true);
		imprimir("VENTANA CLAVE: PERSO", packet);
	}
	
	public static void ENVIAR_KKE_ERROR_CLAVE(final Personaje perso) {
		String packet = "KKE";
		enviarEnCola(perso, packet, true);
		imprimir("CLAVE ERROR: PERSO", packet);
	}
	
	public static void ENVIAR_KV_CERRAR_VENTANA_CLAVE(final Personaje perso) {
		String packet = "KV";
		enviarEnCola(perso, packet, true);
		imprimir("CERRAR CLAVE: PERSO", packet);
	}
	
	public static void ENVIAR_hL_INFO_CASA(final Personaje perso, final String str) {
		String packet = "hL" + str;
		enviarEnCola(perso, packet, true);
		imprimir("CASA: PERSO", packet);
	}
	
	public static void ENVIAR_hP_PROPIEDADES_CASA(final Personaje perso, final String str) {
		String packet = "hP" + str;
		enviarEnCola(perso, packet, true);
		imprimir("CASA: PERSO", packet);
	}
	
	public static void ENVIAR_hV_CERRAR_VENTANA_COMPRA_CASA(final Personaje perso) {
		String packet = "hV";
		enviarEnCola(perso, packet, true);
		imprimir("CASA: PERSO", packet);
	}
	
	public static void ENVIAR_hCK_VENTANA_COMPRA_VENTA_CASA(final Personaje perso, final String str) {
		String packet = "hCK" + str;
		enviarEnCola(perso, packet, true);
		imprimir("CASA: PERSO", packet);
	}
	
	public static void ENVIAR_hSK_FIJAR_PRECIO_CASA(final Personaje perso, final String str) {
		String packet = "hSK" + str;
		enviarEnCola(perso, packet, true);
		imprimir("CASA: PERSO", packet);
	}
	
	public static void ENVIAR_hG_DERECHOS_GREMIO_CASA(final Personaje perso, final String str) {
		String packet = "hG" + str;
		enviarEnCola(perso, packet, true);
		imprimir("CASA: PERSO", packet);
	}
	
	public static void ENVIAR_hX_CERROJO_CASA(final Personaje perso, final int casaID, final boolean activar) {
		String packet = "hX" + casaID + "|" + (activar ? 1 : 0);
		enviarEnCola(perso, packet, true);
		imprimir("CASA: PERSO", packet);
	}
	
	public static void ENVIAR_SF_OLVIDAR_HECHIZO(final char signo, final Personaje perso) {
		String packet = "SF" + signo;
		enviarEnCola(perso, packet, true);
		imprimir("OLVIDAR HECHIZO: PERSO", packet);
	}
	
	public static void ENVIAR_Rv_MONTURA_CERRAR(final Personaje ss) {
		String packet = "Rv";
		enviarEnCola(ss, packet, true);
		imprimir("MONTURA CERRAR: PERSO", packet);
	}
	
	public static void ENVIAR_RD_COMPRAR_CERCADO(final Personaje perso, final String str) {
		String packet = "RD" + str;
		enviarEnCola(perso, packet, true);
		imprimir("COMPRAR CERCADO: PERSO", packet);
	}
	
	public static void ENVIAR_gIF_GREMIO_INFO_CERCADOS(final Personaje perso, final String str) {
		String packet = "gIF" + str;
		enviarEnCola(perso, packet, true);
		imprimir("INFO CERCADOS: PERSO", packet);
	}
	
	public static void ENVIAR_gITM_GREMIO_INFO_RECAUDADOR(final Personaje perso, final String str) {
		if (str.isEmpty()) {
			return;
		}
		String packet = "gITM" + str;
		enviarEnCola(perso, packet, true);
		imprimir("INFO RECAUDOR: PERSO", packet);
	}
	
	public static void ENVIAR_gITp_INFO_ATACANTES_RECAUDADOR(final Personaje perso, final String str) {
		String packet = "gITp" + str;
		enviarEnCola(perso, packet, true);
		imprimir("INFO ATACANTES RECAU: PERSO", packet);
	}
	
	public static void ENVIAR_gITP_INFO_DEFENSORES_RECAUDADOR(final Personaje perso, final String str) {
		String packet = "gITP" + str;
		enviarEnCola(perso, packet, true);
		imprimir("INFO DEFENSORES RECAU: PERSO", packet);
	}
	
	public static void ENVIAR_CP_INFO_DEFENSORES_PRISMA(final Personaje perso, final String str) {
		String packet = "CP" + str;
		enviarEnCola(perso, packet, true);
		imprimir("INFO DEFENSORES PRISMA: PERSO", packet);
	}
	
	public static void ENVIAR_Cp_INFO_ATACANTES_PRISMA(final Personaje perso, final String str) {
		String packet = "Cp" + str;
		enviarEnCola(perso, packet, true);
		imprimir("INFO ATACANTES PRISMA: PERSO", packet);
	}
	
	public static void ENVIAR_gT_PANEL_RECAUDADORES_GREMIO(final Personaje perso, final char c, final String str) {
		String packet = "gT" + c + str;
		enviarEnCola(perso, packet, true);
		imprimir("PANEL RECAU GREMIO: PERSO", packet);
	}
	
	public static void ENVIAR_gUT_PANEL_CASA_GREMIO(final Personaje perso) {
		String packet = "gUT";
		enviarEnCola(perso, packet, true);
		imprimir("PANEL CASA GREMIO: PERSO", packet);
	}
	
	public static void ENVIAR_gUF_PANEL_CERCADOS_GREMIO(final Personaje perso) {
		String packet = "gUF";
		enviarEnCola(perso, packet, true);
		imprimir("PANEL CERCADO GREMIO: PERSO", packet);
	}
	
	public static void ENVIAR_EHm_DETALLE_LINEA_CON_PRECIOS(final Personaje perso, final String signo, final String str) {
		String packet = "EHm" + signo + str;
		enviarEnCola(perso, packet, true);
		imprimir("MOVER OBJMERCA X PRECIO: PERSO", packet);
	}
	
	public static void ENVIAR_EHM_MOVER_OBJMERCA_POR_MODELO(final Personaje perso, final String signo, final String str) {
		String packet = "EHM" + signo + str;
		enviarEnCola(perso, packet, true);
		imprimir("MOVER OBJMERCA X MODELO: PERSO", packet);
	}
	
	public static void ENVIAR_EHP_PRECIO_PROMEDIO_OBJ(final Personaje perso, final int modeloID, final long precio) {
		String packet = "EHP" + modeloID + "|" + precio;
		enviarEnCola(perso, packet, true);
		imprimir("PRECIO PROMEDIO OBJ: PERSO", packet);
	}
	
	public static void ENVIAR_EHl_LISTA_LINEAS_OBJMERCA_POR_MODELO(final Personaje perso, final String str) {
		String packet = "EHl" + str;
		enviarEnCola(perso, packet, true);
		imprimir("LISTA OBJ MERCA MODELO: PERSO", packet);
	}
	
	public static void ENVIAR_EHL_LISTA_OBJMERCA_POR_TIPO(final Personaje perso, final int categ, final String modelos) {
		String packet = "EHL" + categ + "|" + modelos;
		enviarEnCola(perso, packet, true);
		imprimir("LISTA OBJ CATEG MERCADILLO: PERSO", packet);
	}
	
	public static void GAME_SEND_EHL_PACKET(final Personaje perso, final String str) {
		String packet = "EHL" + str;
		enviarEnCola(perso, packet, true);
		imprimir("LISTA OBJ CATEG MERCADILLO: PERSO", packet);
	}
	
	public static void ENVIAR_GA_ACCIONES_MATRIMONIO(final Mapa mapa, int accionID, int propone, int propuesto,
	int sacerdote) {
		String packet = "GA;" + accionID + ";" + propone + ";" + propuesto + "," + propone + "," + sacerdote;
		for (final Personaje pj : mapa.getArrayPersonajes()) {
			enviarEnCola(pj, packet, true);
		}
		imprimir("ACCIONES MATRIMONIO: PERSO", packet);
	}
	
	public static void ENVIAR_Gd_RETO_A_LOS_LUCHADORES(final Pelea pelea, final String reto) {
		String packet = "Gd" + reto;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(1)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("MOSTRAR RETOS: PELEA", packet);
	}
	
	public static void ENVIAR_Gd_RETO_A_PERSONAJE(final Personaje perso, final String reto) {
		String packet = "Gd" + reto;
		enviarEnCola(perso, packet, true);
		imprimir("MOSTRAR RETOS: PERSO", packet);
	}
	
	public static void ENVIAR_GdaK_RETO_REALIZADO(final Personaje perso, final int reto) {
		String packet = "GdaK" + reto;
		enviarEnCola(perso, packet, true);
		imprimir("RETO GANADO: PERSO", packet);
	}
	
	public static void ENVIAR_GdaO_RETO_FALLADO(final Personaje perso, final int reto) {
		String packet = "GdaO" + reto;
		enviarEnCola(perso, packet, true);
		imprimir("RETO PERDIDO: PERSO", packet);
	}
	
	public static void ENVIAR_GdaK_RETO_REALIZADO(final Pelea pelea, final int reto) {
		String packet = "GdaK" + reto;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(5)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("RETO GANADO: PELEA", packet);
	}
	
	public static void ENVIAR_GdaO_RETO_FALLADO(final Pelea pelea, final int reto) {
		String packet = "GdaO" + reto;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(5)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("RETO PERDIDO: PELEA", packet);
	}
	
	public static void ENVIAR_Eq_PREGUNTAR_MERCANTE(final Personaje perso, final int todoItems, final int tasa,
	final long precioPagar) {
		String packet = "Eq" + todoItems + "|" + tasa + "|" + precioPagar;
		enviarEnCola(perso, packet, true);
		imprimir("PREG. MERCANTE: PERSO", packet);
	}
	
	public static void ENVIAR_SB_HECHIZO_BOOST_SET_CLASE(final Personaje perso, final String modificacion) {
		String packet = "SB" + modificacion;
		enviarEnCola(perso, packet, true);
		imprimir("HECHIZO BOOST: PERSO", packet);
	}
	
	public static void ENVIAR_M1_MENSAJE_SERVER_SVR_MUESTRA_INSTANTANEO(final Personaje perso, final int id,
	final String msj, final String nombre) {
		String packet = "M1" + id + "|" + msj + "|" + nombre;
		enviarEnCola(perso, packet, true);
		imprimir("MSJ SERVER: PERSO", packet);
	}
	
	// public static void ENVIAR_M0_MENSAJE_BASICOS_SVR_MUESTRA_DISCONNECT(final Personaje perso,
	// final String id,
	// final String msj, final String nombre) {
	// String packet = "M0" + id + "|" + msj + "|" + nombre;
	// enviarEnCola(perso, packet, true);
	// // if (Bustemu.MOSTRAR_ENVIOS) {
	// // imprimir("MSJ SERVER: PERSO" , packet);
	// // }
	// }
	public static void ENVIAR_M0_MENSAJE_BASICOS_SVR_MUESTRA_DISCONNECT(final ServidorSocket ss, final String id,
	final String msj, final String nombre) {
		String packet = "M0" + id + "|" + msj + "|" + nombre;
		ss.enviarPW(packet);
		imprimir("MSJ SERVER: PERSO", packet);
	}
	
	public static void ENVIAR_IH_COORDENADAS_UBICACION(final Personaje perso, final String str) {
		String packet = "IH" + str;
		enviarEnCola(perso, packet, true);
		imprimir("COORD UBIC: PERSO", packet);
	}
	
	public static void ENVIAR_IC_PERSONAJE_BANDERA_COMPAS(final Personaje perso, final String str) {
		String packet = "IC" + str;
		enviarEnCola(perso, packet, true);
		imprimir("PJ BAND COMPAS: PERSO", packet);
	}
	
	public static void ENVIAR_IC_BORRAR_BANDERA_COMPAS(final Personaje perso) {
		String packet = "IC|";
		enviarEnCola(perso, packet, true);
		imprimir("BORRAR BAND COMPAS: PERSO", packet);
	}
	
	public static void ENVIAR_gA_MENSAJE_SOBRE_RECAUDADOR(final Personaje perso, final String str) {
		String packet = "gA" + str;
		enviarEnCola(perso, packet, true);
		imprimir("MSJ SOBRE RECAU: PERSO", packet);
	}
	
	public static void ENVIAR_CA_MENSAJE_ATAQUE_PRISMA(final Personaje perso, final String str) {
		String packet = "CA" + str;
		enviarEnCola(perso, packet, true);
		imprimir("MSJ ATAQ PRISMA: PERSO", packet);
	}
	
	public static void ENVIAR_CS_MENSAJE_SOBREVIVIO_PRISMA(final Personaje perso, final String str) {
		String packet = "CS" + str;
		enviarEnCola(perso, packet, true);
		// if (Bustemu.MOSTRAR_ENVIOS) {
		// imprimir("MSJ SOBREVIVIO PRISMA: PERSO>> " + "CS" + str);
		// }
	}
	
	public static void ENVIAR_CD_MENSAJE_MURIO_PRISMA(final Personaje perso, final String str) {
		String packet = "CD" + str;
		enviarEnCola(perso, packet, true);
		// if (Bustemu.MOSTRAR_ENVIOS) {
		// imprimir("MSJ MURIO PRISMA: PERSO>> " + "CD" + str);
		// }
	}
	
	public static void ENVIAR_PF_SEGUIR_PERSONAJE(final Personaje perso, final String str) {
		String packet = "PF" + str;
		enviarEnCola(perso, packet, true);
		imprimir("SEGUIR PERSO: PERSO", packet);
	}
	
	public static void ENVIAR_OT_OBJETO_HERRAMIENTA(final Personaje ss, final int id) {
		String packet = "OT" + (id > 0 ? id : "");
		enviarEnCola(ss, packet, true);
		imprimir("OBJ HERRAMIENTA: PERSO", packet);
	}
	
	public static void ENVIAR_EW_OFICIO_MODO_PUBLICO(final Personaje ss, final String signo) {
		String packet = "EW" + signo;
		enviarEnCola(ss, packet, true);
		imprimir("MODO PUBLICO: PERSO", packet);
	}
	
	public static void ENVIAR_EW_OFICIO_MODO_INVITACION(final Personaje perso, final String signo, final int idPerso,
	final String idOficios) {
		String packet = "EW" + signo + idPerso + "|" + idOficios;
		enviarEnCola(perso, packet, true);
		imprimir("INVITAR TALLER: PERSO", packet);
	}
	
	public static void ENVIAR_Cb_BALANCE_CONQUISTA(final Personaje perso, final String str) {
		String packet = "Cb" + str;
		enviarEnCola(perso, packet, true);
		imprimir("BALANCE CONQUISTA: PERSO", packet);
	}
	
	public static void ENVIAR_CB_BONUS_CONQUISTA(final Personaje perso, final String str) {
		String packet = "CB" + str;
		enviarEnCola(perso, packet, true);
		imprimir("BONUS CONQUISTA: PERSO", packet);
	}
	
	public static void ENVIAR_CW_INFO_MUNDO_CONQUISTA(final Personaje perso, final String str) {
		String packet = "CW" + str;
		enviarEnCola(perso, packet, true);
		imprimir("MUNDO CONQUISTA: PERSO", packet);
	}
	
	public static void ENVIAR_CIJ_INFO_UNIRSE_PRISMA(final Personaje perso, final String str) {
		String packet = "CIJ" + str;
		enviarEnCola(perso, packet, true);
		imprimir("UNIRSE PRISMA: PERSO", packet);
	}
	
	public static void ENVIAR_CIV_CERRAR_INFO_CONQUISTA(final Personaje perso) {
		String packet = "CIV";
		enviarEnCola(perso, packet, true);
		imprimir("CERRAR INFO CONQUISTA: PERSO", packet);
	}
	
	public static void ENVIAR_M145_MENSAJE_PANEL_INFORMACION(final Personaje perso, final String str) {
		String packet = "M145|" + str.replace("\\n", "\n").replace("\\t", "\t").replace("\\r", "\r");
		enviarEnCola(perso, packet, true);
	}
	
	public static void ENVIAR_M145_MENSAJE_PANEL_INFORMACION(final ServidorSocket ss, final String str) {
		String packet = "M145|" + str.replace("\\n", "\n").replace("\\t", "\t").replace("\\r", "\r");
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_M145_MENSAJE_PANEL_INFORMACION_TODOS(final String str) {
		String packet = "M145|" + str;
		for (final Personaje perso : Mundo.getPersonajesEnLinea()) {
			enviarEnCola(perso, packet, true);
		}
		imprimir("PANEL INFORMACION: PERSO", packet);
	}
	
	public static void ENVIAR_BAIO_HABILITAR_ADMIN(final Personaje perso, final String str) {
		String packet = "BAIO" + str;
		enviarEnCola(perso, packet, true);
		imprimir("HABILITAR ADMIN: PERSO", packet);
	}
	
	public static void ENVIAR_Ew_PODS_MONTURA(final Personaje perso) {
		String packet = "Ew" + perso.getMontura().getPods() + ";" + perso.getMontura().getTotalPods();
		enviarEnCola(perso, packet, true);
		imprimir("PODS MONTURA: PERSO", packet);
	}
	
	public static void ENVIAR_QL_LISTA_MISIONES(final Personaje perso, final String str) {
		String packet = "QL" + str;
		enviarEnCola(perso, packet, true);
		imprimir("LISTA MISIONES: PERSO", packet);
	}
	
	public static void ENVIAR_QS_PASOS_RECOMPENSA_MISION(final Personaje perso, final String str) {
		String packet = "QS" + str;
		enviarEnCola(perso, packet, true);
		imprimir("RECOMPENSA MISION: PERSO", packet);
	}
	
	public static void ENVIAR_GO_GAME_OVER(final Personaje perso) {
		String packet = "GO";
		enviarEnCola(perso, packet, true);
		imprimir("GAME OVER: PERSO", packet);
	}
	
	public static void ENVIAR_bRI_INICIAR_CUENTA_REGRESIVA(final Personaje perso) {
		if (Mundo.SEG_CUENTA_REGRESIVA == 0)
			return;
		final String packet = "bRI" + Mundo.MSJ_CUENTA_REGRESIVA + ";" + Mundo.SEG_CUENTA_REGRESIVA;
		enviarEnCola(perso, packet, true);
	}
	
	public static void ENVIAR_GX_EXTRA_CLIP(final Personaje perso, final String str) {
		String packet = "GX" + str;
		enviarEnCola(perso, packet, true);
		imprimir("EXTRA CLIP: PERSO", packet);
	}
	
	public static void ENVIAR_GX_EXTRA_CLIP_PELEA(final Pelea pelea, final int equipos, final String str) {
		if (!MainServidor.PARAM_MOSTRAR_NRO_TURNOS) {
			return;
		}
		String packet = "GX" + str;
		for (final Luchador luchador : pelea.luchadoresDeEquipo(equipos)) {
			if (luchador.estaRetirado() || luchador.esMultiman()) {
				continue;
			}
			enviarEnCola(luchador.getPersonaje(), packet, true);
		}
		imprimir("EXTRA CLIP: PELEA", packet);
	}
	
	public static void ENVIAR_bOC_ABRIR_PANEL_SERVICIOS(final Personaje ss, final int creditos, final int ogrinas) {
		String packet = "bOC" + creditos + "^" + ogrinas + "^" + Mundo.stringServicios(ss);
		enviarEnCola(ss, packet, true);
		imprimir("ABRIR PANEL OGRINAS: PERSO", packet);
	}
	
	public static void ENVIAR_bB_PANEL_CREAR_ITEM(final Personaje ss) {
		String packet = "bB";
		enviarEnCola(ss, packet, true);
		imprimir("PANEL CREAR ITEM: PERSO", packet);
	}
	
	public static void ENVIAR_bb_DATA_CREAR_ITEM(final Personaje ss) {
		String packet = "bb" + Mundo.CREA_TU_ITEM_DATA;
		enviarEnCola(ss, packet, true);
		imprimir("PANEL CREAR ITEM: PERSO", packet);
	}
	
	public static void ENVIAR_bSP_PANEL_ITEMS(final Personaje ss) {
		String packet = "bSP" + (MainServidor.SISTEMA_ITEMS_TIPO_DE_PAGO.equals("KAMAS") ? "K" : "O") + Mundo
		.getTiposPanelItems();
		enviarEnCola(ss, packet, true);
		imprimir("PANEL SISTEMA ITEMS: PERSO", packet);
	}
	
	public static void ENVIAR_bSO_PANEL_ITEMS_OBJETOS_POR_TIPO(final Personaje ss, String str) {
		String packet = "bSO" + str;
		enviarEnCola(ss, packet, true);
	}
	
	public static void ENVIAR_bP_VOTO_RPG_PARADIZE(final Personaje perso, final int tiempo, boolean url) {
		String packet = "bP" + tiempo + "," + (url ? "1" : "0");
		enviarEnCola(perso, packet, true);
		imprimir("VOTO RGP PARADIZE: PERSO", packet);
	}
	
	public static void ENVIAR_bC_CAMBIAR_COLOR(final Personaje ss) {
		String packet = "bC";
		enviarEnCola(ss, packet, true);
		imprimir("CAMBIAR COLOR: PERSO", packet);
	}
	
	public static void ENVIAR_bRS_PARAR_CUENTA_REGRESIVA(final Personaje perso) {
		String packet = "bRS";
		enviarEnCola(perso, packet, true);
		imprimir("PARAR CUENTA REGRESIVA: PERSO", packet);
	}
	
	public static void ENVIAR_bOA_ACTUALIZAR_PANEL_OGRINAS(final Personaje ss, final long puntos) {
		String packet = "bOA" + puntos;
		enviarEnCola(ss, packet, true);
		imprimir("ACTUALIZAR PANEL OGRINAS: PERSO", packet);
	}
	
	public static void ENVIAR_bI_SISTEMA_RECURSO(final Personaje ss, final String data) {
		String packet = "bI" + data;
		enviarEnCola(ss, packet, true);
		imprimir("SISTEMA RECURSO: PERSO", packet);
	}
	
	public static void ENVIAR_bT_PANEL_LOTERIA(final Personaje ss, final String data) {
		String packet = "bT" + data;
		enviarEnCola(ss, packet, true);
		imprimir("PANEL LOTERIA: PERSO", packet);
	}
	
	public static void ENVIAR_bL_RANKING_PERMITIDOS(final Personaje ss) {
		final String packet = "bL" + Mundo.rankingsPermitidos();
		enviarEnCola(ss, packet, true);
		imprimir("RANKING PERMITIDOS: PERSO", packet);
	}
	
	public static void ENVIAR_bl_RANKING_DATA(final Personaje ss, String param, String data) {
		final String packet = "bl" + param + "|" + data;
		enviarEnCola(ss, packet, true);
		imprimir("RANKING DATA: PERSO", packet);
	}
	
	public static void ENVIAR_bA_ESCOGER_NIVEL(final Personaje perso) {
		final String packet = "bA" + perso.getNivel();
		enviarEnCola(perso, packet, true);
		imprimir("ESCOGER NIVEL: PERSO", packet);
	}
	
	public static void ENVIAR_bm_TRANSFORMAR_MONTURA(final Personaje perso) {
		final String packet = "bm" + Mundo.LISTA_MASCOTAS;
		enviarEnCola(perso, packet, true);
		imprimir("TRANSFORMAR MONTURA: PERSO", packet);
	}
	
	public static void ENVIAR_bRS_PARAR_CUENTA_REGRESIVA_TODOS() {
		String packet = "bRS";
		for (final Personaje perso : Mundo.getPersonajesEnLinea()) {
			enviarEnCola(perso, packet, true);
		}
		imprimir("PARAR CUENTA REGRESIVA: PERSO", packet);
	}
	
	public static void ENVIAR_bRI_INICIAR_CUENTA_REGRESIVA_TODOS() {
		if (Mundo.SEG_CUENTA_REGRESIVA == 0)
			return;
		final String packet = "bRI" + Mundo.MSJ_CUENTA_REGRESIVA + ";" + Mundo.SEG_CUENTA_REGRESIVA;
		for (final Personaje perso : Mundo.getPersonajesEnLinea()) {
			enviarEnCola(perso, packet, true);
		}
		imprimir("INICIAR CUENTA REGRESIVA: TODOS", packet);
	}
	
	public static void ENVIAR_brP_RULETA_PREMIOS(final Personaje ss, String str) {
		final String packet = "brP" + str;
		enviar(ss, packet);
		imprimir("RULETA PREMIOS: PERSO", packet);
	}
	
	public static void ENVIAR_brG_RULETA_GANADOR(final ServidorSocket ss, int index) {
		final String packet = "brG" + index;
		ss.enviarPW(packet);
		imprimir("RULETA GANADOR: PERSO", packet);
	}
	
	public static void ENVIAR_bV_CERRAR_PANEL(final Personaje _perso) {
		final String packet = "bV";
		enviarEnCola(_perso, packet, false);
	}
	
	public static void ENVIAR_bn_CAMBIAR_NOMBRE_CONFIRMADO(final Personaje _perso, String nombre) {
		final String packet = "bn" + nombre;
		enviarEnCola(_perso, packet, false);
	}
	
	public static void ENVIAR_bñ_PANEL_ORNAMENTOS(final Personaje perso) {
		final String packet = "bñ" + Mundo.listarOrnamentos(perso);
		enviarEnCola(perso, packet, true);
		imprimir("PANEL ORNAMENTOS: PERSO", packet);
	}
	
	public static void ENVIAR_bt_PANEL_TITULOS(final Personaje perso) {
		final String packet = "bt" + Mundo.listarTitulos(perso);
		enviarEnCola(perso, packet, true);
		imprimir("PANEL TITULOS: PERSO", packet);
	}
	
	public static void ENVIAR_Ñs_BOTON_BOUTIQUE(final ServidorSocket ss) {
		final String packet = "Ñs";
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_ÑL_BOTON_LOTERIA(final ServidorSocket ss, boolean bMostrar) {
		final String packet = "ÑL" + (bMostrar ? "1" : "0");
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_ÑU_URL_IMAGEN_VOTO(final ServidorSocket ss) {
		final String packet = "ÑU" + MainServidor.URL_IMAGEN_VOTO;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_Ñu_URL_LINK_VOTO(final ServidorSocket ss) {
		final String packet = "Ñu" + MainServidor.URL_LINK_VOTO;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_Ñx_URL_LINK_BUG(final ServidorSocket ss) {
		final String packet = "Ñx" + MainServidor.URL_LINK_BUG;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_Ñz_URL_LINK_COMPRA(final ServidorSocket ss) {
		final String packet = "Ñz" + MainServidor.URL_LINK_COMPRA;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_Ñe_EXO_PANEL_ITEMS(final ServidorSocket ss) {
		final String packet = "Ñe" + MainServidor.SISTEMA_ITEMS_PERFECTO_MULTIPLICA_POR + ","
		+ MainServidor.SISTEMA_ITEMS_EXO_PA_PRECIO + "," + MainServidor.SISTEMA_ITEMS_EXO_PM_PRECIO;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_Ña_AUTO_PASAR_TURNO(final ServidorSocket ss) {
		final String packet = "Ña" + (MainServidor.PARAM_AUTO_SALTAR_TURNO ? "1" : "0");
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_Ñr_SUFJIO_RESET(final ServidorSocket ss) {
		final String packet = "Ñr" + MainServidor.SUFIJO_RESET;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_ÑL_BOTON_LOTERIA_TODOS(boolean bMostrar) {
		final String packet = "ÑL" + (bMostrar ? "1" : "0");
		for (final Personaje perso : Mundo.getPersonajesEnLinea()) {
			enviarEnCola(perso, packet, false);
		}
	}
	
	public static void ENVIAR_bD_LISTA_REPORTES(final Personaje _perso, String str) {
		String packet = "bD" + str;
		enviarEnCola(_perso, packet, true);
	}
	
	public static void ENVIAR_ÑA_LISTA_GFX(final ServidorSocket ss) {
		String packet = "ÑA" + Mundo.LISTA_GFX;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_Bv_SONAR_MP3(final Personaje _perso, String str) {
		String packet = "Bv" + str;
		enviarEnCola(_perso, packet, true);
	}
	
	public static void ENVIAR_ÑV_ACTUALIZAR_URL_LINK_MP3(final ServidorSocket ss) {
		String packet = "ÑV" + MainServidor.URL_LINK_MP3;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_ÑA_LISTA_GFX_TODOS() {
		String packet = "ÑA" + Mundo.LISTA_GFX;
		for (final Personaje perso : Mundo.getPersonajesEnLinea()) {
			enviarEnCola(perso, packet, false);
		}
	}
	
	public static void ENVIAR_ÑB_LISTA_NIVEL(final ServidorSocket ss) {
		String packet = "ÑB" + Mundo.LISTA_NIVEL;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_ÑB_LISTA_NIVEL_TODOS() {
		String packet = "ÑB" + Mundo.LISTA_NIVEL;
		for (final Personaje perso : Mundo.getPersonajesEnLinea()) {
			enviarEnCola(perso, packet, false);
		}
	}
	
	public static void ENVIAR_ÑE_DETALLE_MOB(final ServidorSocket ss, final String str) {
		String packet = "ÑE" + str;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_ÑF_BESTIARIO_MOBS(final ServidorSocket ss, final String str) {
		String packet = "ÑF" + str;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_Ñf_BESTIARIO_DROPS(final ServidorSocket ss, final String str) {
		String packet = "Ñf" + str;
		ss.enviarPW(packet);
		// if (MainServidor.MOSTRAR_ENVIOS) {
		// imprimir("BESTIARIO DROPS: PERSO", packet);
		// }
	}
	
	public static void ENVIAR_ÑV_VOTO_RPG(final ServidorSocket ss, final String str) {
		ss.enviarPW("ÑV" + str);
	}
	
	public static void ENVIAR_ÑR_ACTIVAR_BOTON_RECURSOS(final ServidorSocket ss) {
		ss.enviarPW("ÑR");
	}
	
	public static void ENVIAR_ÑX_PANEL_ALMANAX(final ServidorSocket ss, String str) {
		ss.enviarPW("ÑX" + str);
	}
	
	public static void ENVIAR_ÑK_TEST_DAÑO_MOB(final Personaje _perso, String str) {
		final String packet = "ÑK" + str;
		enviarEnCola(_perso, packet, false);
	}
	
	public static void ENVIAR_ÑS_SERVER_HEROICO(final ServidorSocket ss) {
		ss.enviarPW("ÑS");
	}
	
	public static void ENVIAR_ÑR_BOTON_RECURSOS(final ServidorSocket ss) {
		ss.enviarPW("ÑR");
	}
	
	public static void ENVIAR_Ñm_MENSAJE_NOMBRE_SERVER(final ServidorSocket ss) {
		ss.enviarPW("Ñm" + MainServidor.NOMBRE_SERVER);
	}
	
	public static void ENVIAR_ÑG_CLASES_PERMITIDAS(final ServidorSocket ss) {
		String packet = "ÑG" + Mundo.CLASES_PERMITIDAS;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_ÑO_ID_OBJETO_MODELO_MAX(ServidorSocket ss) {
		ss.enviarPW("ÑO" + MainServidor.MAX_ID_OBJETO_MODELO);
	}
	
	public static void ENVIAR_ÑD_DAÑO_PERMANENTE(final ServidorSocket ss) {
		final String packet = "ÑD" + MainServidor.PORCENTAJE_DAÑO_NO_CURABLE;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_ÑM_PANEL_MIMOBIONTE(final Personaje _perso) {
		String packet = "ÑM";
		enviarEnCola(_perso, packet, true);
	}
	
	public static void ENVIAR_ÑJ_STATS_DEFECTO_MOB(final Personaje _perso, final String str) {
		final String packet = "ÑJ" + str;
		enviarEnCola(_perso, packet, false);
	}
	
	public static void ENVIAR_ÑI_CREA_TU_ITEM_OBJETOS(final ServidorSocket ss) {
		final String packet = "ÑI" + Mundo.CREA_TU_ITEM_OBJETOS;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_Ñi_CREA_TU_ITEM_PRECIOS(final ServidorSocket ss) {
		final String packet = "Ñi" + Mundo.CREAT_TU_ITEM_PRECIOS;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_Ñp_RANGO_NIVEL_PVP(final ServidorSocket ss) {
		final String packet = "Ñp" + MainServidor.RANGO_NIVEL_PVP;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_ÑZ_COLOR_CHAT(final ServidorSocket ss) {
		if (MainServidor.CANALES_COLOR_CHAT.isEmpty()) {
			return;
		}
		final String packet = "ÑZ" + MainServidor.CANALES_COLOR_CHAT;
		ss.enviarPW(packet);
	}
	
	public static void ENVIAR_bo_RESTRINGIR_COLOR_DIA(final ServidorSocket ss) {
		final String packet = "bo" + (MainServidor.PARAM_RESTRINGIR_COLOR_DIA ? "1" : "0");
		ss.enviarPW(packet);
	}
}
