package variables.zotros;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Pattern;
import variables.casa.Casa;
import variables.gremio.Recaudador;
import variables.mapa.Area;
import variables.mapa.Celda;
import variables.mapa.Cercado;
import variables.mapa.Mapa;
import variables.mapa.SubArea;
import variables.mision.Mision;
import variables.mision.MisionModelo;
import variables.mision.MisionObjetivoModelo;
import variables.mob.GrupoMob;
import variables.mob.MobModelo.TipoGrupo;
import variables.npc.PreguntaNPC;
import variables.objeto.Objeto;
import variables.objeto.ObjetoModelo;
import variables.objeto.ObjetoSet;
import variables.objeto.ObjetoModelo.CAPACIDAD_STATS;
import variables.oficio.StatOficio;
import variables.personaje.MisionPVP;
import variables.personaje.Personaje;
import estaticos.Camino;
import estaticos.Constantes;
import estaticos.Formulas;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.MainServidor;
import estaticos.Mundo;

public class Accion {
	private static final int ACCION_CREAR_GREMIO = -2;
	private static final int ACCION_ABRIR_BANCO = -1;
	private static final int ACCION_TELEPORT_MAPA = 0;
	private static final int ACCION_DIALOGO = 1;
	private static final int ACCION_AGREGAR_OBJETO_AZAR = 2;
	private static final int ACCION_NADA = 3;
	private static final int ACCION_DAR_QUITAR_KAMAS = 4;
	private static final int ACCION_DAR_QUITAR_OBJETOS = 5;
	private static final int ACCION_APRENDER_OFICIO = 6;
	private static final int ACCION_RETORNAR_PUNTO_SALVADA = 7;
	private static final int ACCION_BOOST_STATS = 8;
	private static final int ACCION_APRENDER_HECHIZO = 9;
	private static final int ACCION_CURAR = 10;
	private static final int ACCION_CAMBIAR_ALINEACION = 11;
	private static final int ACCION_CREAR_GRUPO_MOB_CON_PIEDRA = 12;
	private static final int ACCION_RESETEAR_STATS = 13;
	private static final int ACCION_OLVIDAR_HECHIZO_PANEL = 14;
	private static final int ACCION_USAR_LLAVE = 15;
	private static final int ACCION_DAR_QUITAR_HONOR = 16;
	private static final int ACCION_EXP_OFICIO = 17;
	private static final int ACCION_TELEPORT_CASA = 18;
	private static final int ACCION_PANEL_CASA_GREMIO = 19;
	private static final int ACCION_DAR_QUITAR_PUNTOS_HECHIZO = 20;
	private static final int ACCION_DAR_QUITAR_ENERGIA = 21;
	private static final int ACCION_DAR_EXPERIENCIA = 22;
	private static final int ACCION_OLVIDAR_OFICIO = 23;
	private static final int ACCION_CAMBIAR_GFX = 24;
	private static final int ACCION_DEFORMAR = 25;
	private static final int ACCION_PANEL_CERCADO_GREMIO = 26;
	private static final int ACCION_INICIAR_PELEA_VS_MOBS = 27;
	private static final int ACCION_SUBIR_BAJAR_MONTURA = 28;
	private static final int ACCION_INCIAR_PELEA_VS_MOBS_NO_ESPADA = 29;
	private static final int ACCION_REFRESCAR_MOBS = 30;
	private static final int ACCION_CAMBIAR_CLASE = 31;
	private static final int ACCION_AUMENTAR_RESETS = 32;
	private static final int ACCION_OBJETO_BOOST = 33;
	private static final int ACCION_CAMBIAR_SEXO = 34;
	private static final int ACCION_PAGAR_PESCAR_KUAKUA = 35;
	private static final int ACCION_RULETA_JALATO = 36;
	private static final int ACCION_DAR_ORNAMENTO = 37;
	private static final int ACCION_TELEPORT_CELDA_MISMO_MAPA = 38;
	private static final int ACCION_GANAR_RULETA_JALATO = 39;
	private static final int ACCION_KAMAS_RULETA_JALATO = 40;
	private static final int ACCION_INICIAR_PELEA_DOPEUL = 41;
	private static final int ACCION_DAR_SET_OBJETOS = 42;
	private static final int ACCION_CONFIRMAR_CUMPLIO_OBJETIVO_MISION = 43;
	private static final int ACCION_DAR_MISION = 44;
	private static final int ACCION_AGREGAR_MOB_ALBUM = 45;
	private static final int ACCION_DAR_TITULO = 46;
	private static final int ACCION_RECOMPENSA_DOPEUL = 47;
	private static final int ACCION_VERIFICA_MISION_ALMANAX = 48;
	private static final int ACCION_DAR_MISION_PVP_CON_PERGAMINOS = 49;
	private static final int ACCION_DAR_MISION_PVP = 50;
	private static final int ACCION_GEOPOSICION_MISION_PVP = 51;
	private static final int ACCION_TELEPORT_MISION_PVP = 52;
	private static final int ACCION_CONFIRMA_CUMPLIO_MISION = 53;
	private static final int ACCION_BOOST_FULL_STATS = 54;
	private static final int ACCION_PAGAR_PARA_REALIZAR_ACCION = 55;
	private static final int ACCION_SOLICITAR_OBJETOS_PARA_DAR_OTROS = 56;
	private static final int ACCION_REVIVIR = 57;
	private static final int ACCION_ABRIR_DOCUMENTO = 58;
	private static final int ACCION_DAR_SET_OBJETOS_POR_FICHAS = 59;
	private static final int ACCION_REALIZAR_ACCION_PJS_EN_MAPA_POR_ALINEACION_Y_DISTANCIA = 60;
	private static final int ACCION_LIBERAR_TUMBA = 61;
	private static final int ACCION_REVIVIR2 = 62;
	private static final int ACCION_AGREGAR_PJ_LIBRO_ARTESANOS = 63;
	private static final int ACCION_ACTIVAR_CELDAS_INTERACTIVAS = 64;
	private static final int ACCION_DAR_QUITAR_EMOTE = 65;
	private static final int ACCION_SOLICITAR_OBJETOS_PARA_REALIZAR_ACCION = 66;
	private static final int ACCION_CAMBIAR_ROSTRO = 67;
	private static final int ACCION_MENSAJE_INFORMACION = 68;
	private static final int ACCION_MENSAJE_PANEL = 69;
	private static final int ACCION_DAR_OBJETOS_DE_LOS_STATS = 70;
	private static final int ACCION_DAR_ABONO_DIAS = 71;
	private static final int ACCION_DAR_ABONO_HORAS = 72;
	private static final int ACCION_DAR_ABONO_MINUTOS = 73;
	private static final int ACCION_DAR_NIVEL_DE_ORDEN = 74;
	private static final int ACCION_DAR_ORDEN = 75;
	private static final int ACCION_BORRAR_OBJETO_MODELO = 76;
	private static final int ACCION_VERIFICA_STAT_OBJETO_Y_LO_BORRA = 77;
	private static final int ACCION_BORRAR_OBJETO_AL_AZAR_PARA_DAR_OTROS = 78;
	private static final int ACCION_GDF_PERSONA = 79;
	private static final int ACCION_GDF_MAPA = 80;
	private static final int ACCION_RULETA_PREMIOS = 81;
	private static final int ACCION_TIEMPO_PROTECCION_RECAUDADOR = 82;
	private static final int ACCION_TIEMPO_PROTECCION_PRISMA = 83;
	private static final int ACCION_OLVIDAR_HECHIZO_RECAUDADOR = 84;
	private static final int ACCION_ENVIAR_PACKET = 99;
	private static final int ACCION_DAR_HABILIDAD_MONTURA = 100;
	private static final int ACCION_CASAR_DOS_PJS = 101;
	private static final int ACCION_DISCURSO_SACEDORTE = 102;
	private static final int ACCION_DIVORCIARSE = 103;
	private static final int ACCION_DAR_OGRINAS = 104;
	private static final int ACCION_DAR_CREDITOS = 105;
	private static final int ACCION_AGREGAR_OBJETO_A_CERCADO = 200;
	private static final int ACCION_AGREGAR_PRISMA_A_MAPA = 201;
	private static final int ACCION_LANZAR_ANIMACION = 227;
	private static final int ACCION_LANZAR_ANIMACION2 = 228;
	private final int _id;
	private final String _args;
	private String _condicion = "";
	
	public Accion(final int id, final String args, String condicion) {
		_id = id;
		_args = args;
		_condicion = condicion;
	}
	
	public int getID() {
		return _id;
	}
	
	public String getArgs() {
		return _args;
	}
	
	public String getCondicion() {
		return _condicion;
	}
	
	public void setCondicion(String condicion) {
		_condicion = condicion;
	}
	
	public boolean realizarAccion(final Personaje perso, Personaje objetivo, final int idObjUsar, final short celda) {
		return realizar_Accion_Estatico(_id, _args, perso, objetivo, idObjUsar, celda);
	}
	
	public static boolean realizar_Accion_Estatico(final int _id, final String _args, final Personaje perso,
	Personaje objetivo, final int idObjUsar, short celdaID) {
		try {
			if (objetivo == null) {
				objetivo = perso;
			}
			if (celdaID == -1) {
				celdaID = perso.getCelda().getID();
			}
			// if (!Condicion.validaCondiciones(perso, condicion)) {
			// return false;
			// }
			final Objeto objUsar = Mundo.getObjeto(idObjUsar);
			switch (_id) {
				case ACCION_CREAR_GREMIO :// crear gremio
					try {
						if (!perso.estaDisponible(false, false)) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						if (perso.getGremio() != null || perso.getMiembroGremio() != null) {
							GestorSalida.ENVIAR_gC_CREAR_PANEL_GREMIO(perso, "Ea");
							return false;
						}
						// perso.addObjIdentAInventario(Mundo.getObjetoModelo(1575).crearObjDesdeModelo(1,
						// Constantes.OBJETO_POS_NO_EQUIPADO, 0), false);
						perso.setOcupado(true);
						GestorSalida.ENVIAR_gn_CREAR_GREMIO(perso);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_ABRIR_BANCO :// abrir banco
					try {
						if (perso.getDeshonor() >= 1) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "183");
							return false;
						}
						if (!perso.estaDisponible(false, false)) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						final int costo = perso.getCostoAbrirBanco();
						if (perso.getKamas() - costo < 0) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1128;" + costo);
							GestorSalida.ENVIAR_M1_MENSAJE_SERVER_SVR_MUESTRA_INSTANTANEO(perso, 10, costo + "", "");
						} else {
							perso.addKamas(-costo, false, true);
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "020;" + costo);
							perso.getBanco().abrirCofre(perso);
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_TELEPORT_MAPA :// teleportar a otro mapa
					try {
						final String[] args = _args.split(",");
						Mapa nuevoMapa = Mundo.getMapa(Short.parseShort(args[0]));
						if (nuevoMapa == null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						if (objUsar != null) {
							if (perso.getMapa().esMazmorra()) {
								GestorSalida.ENVIAR_Im_INFORMACION(perso, "113");
								return false;
							}
						}
						if (perso.getPelea() != null) {
							Celda nuevaCelda = nuevoMapa.getCelda(Short.parseShort(args[1]));
							if (nuevaCelda != null) {
								perso.setMapa(nuevoMapa);
								perso.setCelda(nuevaCelda);
							}
						} else {
							if (args.length > 2) {
								GestorSalida.ENVIAR_GA2_CINEMATIC(perso, args[2]);
							}
							perso.teleport(Short.parseShort(args[0]), Short.parseShort(args[1]));
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DIALOGO :// dialogo
					try {
						if (_args.equals("DV")) {
							perso.dialogoFin();
						} else {
							int preguntaID = 0;
							try {
								preguntaID = Integer.parseInt(_args);
							} catch (final Exception e) {}
							if (preguntaID <= 0) {
								perso.dialogoFin();
							}
							PreguntaNPC pregunta = Mundo.getPreguntaNPC(preguntaID);
							if (pregunta == null) {
								pregunta = new PreguntaNPC(preguntaID, "", "", "");
								Mundo.addPreguntaNPC(pregunta);
							}
							GestorSalida.ENVIAR_DQ_DIALOGO_PREGUNTA(perso, pregunta.stringArgParaDialogo(perso, perso));
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_AGREGAR_OBJETO_AZAR :// agregar objeto al azar, por otro objeto
					// no borra el objeto q se usa
					try {
						final String quitar = _args.split(Pattern.quote("|"))[0];
						final String[] azar = _args.split(Pattern.quote("|"))[1].split(";");
						final int idQuitar = Integer.parseInt(quitar.split(",")[0]);
						final int cantQuitar = Math.abs(Integer.parseInt(quitar.split(",")[1]));
						if (perso.tenerYEliminarObjPorModYCant(idQuitar, cantQuitar)) {
							final String objetoAzar = azar[Formulas.getRandomInt(0, azar.length - 1)];
							final int idDar = Integer.parseInt(objetoAzar.split(",")[0]);
							final int cantDar = Math.abs(Integer.parseInt(objetoAzar.split(",")[1]));
							perso.addObjIdentAInventario(Mundo.getObjetoModelo(idDar).crearObjeto(cantDar,
							Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM), false);
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "022;" + cantQuitar + "~" + idQuitar);
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "021;" + cantDar + "~" + idDar);
							GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(perso);
						} else {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "14|43");
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
					}
					return false;// no borra
				case ACCION_NADA : //
					//
					//
					//
					//
					//
					break;
				case ACCION_DAR_QUITAR_KAMAS :// agregar o quitar kamas
					try {
						int kamas = 0;
						String[] s = _args.split(",");
						if (s.length == 1) {
							kamas = Integer.parseInt(s[0]);
						} else {
							kamas = Formulas.getRandomInt(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
						}
						perso.addKamas(kamas, true, true);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_QUITAR_OBJETOS :// 5 quitar o dar un objeto
					try {
						boolean b = false;
						for (String s : _args.split(";")) {
							String[] ss = s.split(",");
							final int id = Integer.parseInt(ss[0]);
							int cant = 1;// corregir los otros
							if (ss.length > 1) {
								cant = Integer.parseInt(ss[1]);
							}
							ObjetoModelo tempObjMod = Mundo.getObjetoModelo(id);
							if (tempObjMod == null) {
								GestorSalida.ENVIAR_BN_NADA(objetivo, "BUG ACCION " + _id + " idObjMod " + id);
							} else {
								if (cant > 0) {
									b = true;
									perso.addObjIdentAInventario(tempObjMod.crearObjeto(cant, Constantes.OBJETO_POS_NO_EQUIPADO,
									CAPACIDAD_STATS.RANDOM), false);
									GestorSalida.ENVIAR_Im_INFORMACION(perso, "021;" + cant + "~" + id);
								} else if (cant < 0) {
									int borrados = perso.restarObjPorModYCant(id, Math.abs(cant));
									if (borrados > 0) {
										b = true;
										GestorSalida.ENVIAR_Im_INFORMACION(perso, "022;" + borrados + "~" + id);
									}
								}
							}
						}
						if (b) {
							GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(perso);
						} else {
							return false;
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_APRENDER_OFICIO :// aprender oficio
					try {
						boolean b = false;
						for (String s : _args.split(";")) {
							final int idOficio = Integer.parseInt(s.split(",")[0]);
							boolean siOSi = false;
							try {
								siOSi = s.split(",")[1].equals("1");
							} catch (final Exception e) {}
							if (Mundo.getOficio(idOficio) == null) {
								GestorSalida.ENVIAR_BN_NADA(objetivo);
							} else {
								if (siOSi || perso.puedeAprenderOficio(idOficio)) {
									b = true;
									perso.aprenderOficio(Mundo.getOficio(idOficio), 0);
								} else {
									GestorSalida.ENVIAR_Im_INFORMACION(perso, "16");
								}
							}
						}
						if (!b) {
							return false;
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_RETORNAR_PUNTO_SALVADA :// returno al punto de salvada
					try {
						if (perso.getPelea() != null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						perso.teleportPtoSalvada();
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_BOOST_STATS :// stast boost scroll
					try {
						boolean as = false;
						for (String s : _args.split(";")) {
							final int statID = Integer.parseInt(s.split(",")[0]);
							final int cantidad = Integer.parseInt(s.split(",")[1]);
							int mensajeID = 0;
							switch (statID) {
								case Constantes.STAT_MAS_SABIDURIA :
									perso.addScrollStat(Constantes.STAT_MAS_SABIDURIA, cantidad);
									mensajeID = 9;
									break;
								case Constantes.STAT_MAS_FUERZA :
									perso.addScrollStat(Constantes.STAT_MAS_FUERZA, cantidad);
									mensajeID = 10;
									break;
								case Constantes.STAT_MAS_SUERTE :
									perso.addScrollStat(Constantes.STAT_MAS_SUERTE, cantidad);
									mensajeID = 11;
									break;
								case Constantes.STAT_MAS_AGILIDAD :
									perso.addScrollStat(Constantes.STAT_MAS_AGILIDAD, cantidad);
									mensajeID = 12;
									break;
								case Constantes.STAT_MAS_VITALIDAD :
									perso.addScrollStat(Constantes.STAT_MAS_VITALIDAD, cantidad);
									mensajeID = 13;
									break;
								case Constantes.STAT_MAS_INTELIGENCIA :
									perso.addScrollStat(Constantes.STAT_MAS_INTELIGENCIA, cantidad);
									mensajeID = 14;
									break;
							}
							if (mensajeID > 0) {
								as = true;
								GestorSalida.ENVIAR_Im_INFORMACION(perso, "0" + mensajeID + ";" + cantidad);
							}
						}
						if (as) {
							GestorSalida.ENVIAR_As_STATS_DEL_PJ(perso);
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_APRENDER_HECHIZO :// aprender hechizo
					try {
						for (String s : _args.split(";")) {
							int hechizoID = Integer.parseInt(s);
							if (Mundo.getHechizo(hechizoID) == null) {
								GestorSalida.ENVIAR_BN_NADA(objetivo);
								return false;
							}
							if (!objetivo.tieneHechizoID(hechizoID)) {
								objetivo.fijarNivelHechizoOAprender(hechizoID, 1, true);
							} else {
								GestorSalida.ENVIAR_Im_INFORMACION(objetivo, "17;" + hechizoID);
								return false;
							}
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_CURAR :// curar la vida sin/con objeto
					try {
						if (objetivo.getPorcPDV() >= 100) {
							GestorSalida.ENVIAR_BN_NADA(perso);
							return false;
						}
						try {
							if (objUsar != null) {
								short tipo = objUsar.getObjModelo().getTipo();
								switch (tipo) {
									case Constantes.OBJETO_TIPO_BEBIDA :
									case Constantes.OBJETO_TIPO_POCION :
										GestorSalida.ENVIAR_eUK_EMOTE_MAPA(objetivo.getMapa(), objetivo.getID(), 18, 0);
										break;
									case Constantes.OBJETO_TIPO_PAN :
									case Constantes.OBJETO_TIPO_CARNE_COMESTIBLE :
									case Constantes.OBJETO_TIPO_PESCADO_COMESTIBLE :
										GestorSalida.ENVIAR_eUK_EMOTE_MAPA(objetivo.getMapa(), objetivo.getID(), 17, 0);
										break;
								}
							}
						} catch (final Exception e) {}
						int valor = 0;
						String[] s = _args.split(",");
						if (s.length == 1) {
							valor = Integer.parseInt(s[0]);
						} else {
							valor = Formulas.getRandomInt(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
						}
						objetivo.addPDV(valor);
						GestorSalida.ENVIAR_Im_INFORMACION(perso, "01;" + valor);
						GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(objetivo);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_CAMBIAR_ALINEACION :// cambiar alineacion
					try {
						byte alineacion = Byte.parseByte(_args.split(",")[0]);
						if (alineacion == perso.getAlineacion()) {
							return false;
						}
						return perso.cambiarAlineacion(alineacion, false);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
				case ACCION_CREAR_GRUPO_MOB_CON_PIEDRA :// liberar piedra de alma
					try {
						// si o si es con el objeto a usar
						// final boolean delObj = _args.split(",")[0].equals("true");
						if (objUsar == null) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "14|43");
							return false;
						}
						final boolean enArena = _args.split(",")[1].equalsIgnoreCase("true");
						if (enArena && !perso.getMapa().esArena()) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "113");
							return false;
						}
						if (perso.getMapa().esMazmorra()) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "113");
							return false;
						}
						String condicion = "Mi=" + perso.getID();
						GrupoMob grupoMob = perso.getMapa().addGrupoMobPorTipo(perso.getCelda().getID(), objUsar.strGrupoMob(),
						TipoGrupo.SOLO_UNA_PELEA, condicion, null);
						grupoMob.setCondUnirsePelea("");
						grupoMob.startTiempoCondicion();
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_INICIAR_PELEA_DOPEUL :// iniciar pelea vs mob sin espada
					try {
						final int mobDopeul = Integer.parseInt(_args);
						final int nivel = Constantes.getNivelDopeul(perso.getNivel());
						GrupoMob grupoMob = new GrupoMob(perso.getMapa(), perso.getCelda().getID(), mobDopeul + "," + nivel + ","
						+ nivel, TipoGrupo.SOLO_UNA_PELEA, "");
						perso.getMapa().iniciarPelea(perso, null, perso.getCelda().getID(), (short) -1,
						Constantes.PELEA_TIPO_PVM_NO_ESPADA, grupoMob);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_INICIAR_PELEA_VS_MOBS :
					// iniciar pelea vs 1 mob mobID,mobnivel|mobID,mobNivel|.....
				case ACCION_INCIAR_PELEA_VS_MOBS_NO_ESPADA :
					// iniciar pelea vs 1 mob no espada mobID,mobnivel|mobID,mobNivel|.....
					try {
						if (objUsar != null) {
							if (perso.getMapa().esMazmorra()) {
								GestorSalida.ENVIAR_Im_INFORMACION(perso, "113");
								return false;
							}
						}
						final StringBuilder mobGrupo = new StringBuilder();
						for (final String mobYNivel : _args.split(Pattern.quote(";"))) {
							final String[] mobONivel = mobYNivel.split(",");
							int mobID = Integer.parseInt(mobONivel[0]);
							mobGrupo.append(mobID);
							if (mobONivel.length > 1) {
								mobGrupo.append("," + Integer.parseInt(mobONivel[1]));
							}
							if (mobONivel.length > 2) {
								mobGrupo.append("," + Integer.parseInt(mobONivel[2]));
							}
							mobGrupo.append(";");
						}
						GrupoMob grupoMob = new GrupoMob(perso.getMapa(), (short) (perso.getCelda().getID() + 1), mobGrupo
						.toString(), TipoGrupo.SOLO_UNA_PELEA, "");
						if (grupoMob.getCantMobs() <= 0) {
							return false;
						}
						byte tipoPelea = ACCION_INICIAR_PELEA_VS_MOBS == _id
						? Constantes.PELEA_TIPO_PVM
						: Constantes.PELEA_TIPO_PVM_NO_ESPADA;
						perso.getMapa().iniciarPelea(perso, null, perso.getCelda().getID(), (short) -1, tipoPelea, grupoMob);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_RESETEAR_STATS :// resetear stats
					try {
						boolean todo = false;
						try {
							todo = _args.equalsIgnoreCase("true");
						} catch (final Exception e) {}
						perso.resetearStats(todo);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_OLVIDAR_HECHIZO_PANEL :// olvidar hechizo
					try {
						try {
							return perso.olvidarHechizo(Integer.parseInt(_args), false, true);
						} catch (Exception e) {
							perso.setOlvidandoHechizo(true);
							GestorSalida.ENVIAR_SF_OLVIDAR_HECHIZO('+', perso);
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_USAR_LLAVE :// usar objeto o llave para entrar a una mazmorra
					try {
						// tpmap,tpcelda,objnecesario,mapanecesario
						final short nuevoMapaID = Short.parseShort(_args.split(",")[0]);
						final short nuevaCeldaID = Short.parseShort(_args.split(",")[1]);
						int objNecesario = 0;
						try {
							objNecesario = Integer.parseInt(_args.split(",")[2]);
						} catch (final Exception e) {}
						int mapaNecesario = 0;
						try {
							mapaNecesario = Integer.parseInt(_args.split(",")[3]);
						} catch (final Exception e) {}
						if (objNecesario == 0) {
							perso.teleport(nuevoMapaID, nuevaCeldaID);
						} else if (objNecesario > 0) {
							if (mapaNecesario == 0) {
								if (perso.tenerYEliminarObjPorModYCant(objNecesario, 1)) {
									GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(perso);
									perso.teleport(nuevoMapaID, nuevaCeldaID);
								} else {
									GestorSalida.ENVIAR_Im_INFORMACION(perso, "14|45");
								}
							} else if (mapaNecesario > 0) {
								if (perso.getMapa().getID() == mapaNecesario) {
									if (perso.tenerYEliminarObjPorModYCant(objNecesario, 1)) {
										GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(perso);
										perso.teleport(nuevoMapaID, nuevaCeldaID);
									} else {
										GestorSalida.ENVIAR_Im_INFORMACION(perso, "14|45");
									}
								} else if (perso.getMapa().getID() != mapaNecesario) {
									GestorSalida.ENVIAR_Im_INFORMACION(perso, "113");
								}
							}
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_QUITAR_HONOR :// aumentar o disminuir honor
					try {
						if (perso.getAlineacion() == Constantes.ALINEACION_NEUTRAL) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						perso.addHonor(Integer.parseInt(_args));
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_EXP_OFICIO :// agregar xp a oficio
					try {
						final int oficioID = Integer.parseInt(_args.split(",")[0]);
						final int xp = Integer.parseInt(_args.split(",")[1]);
						if (perso.getStatOficioPorID(oficioID) == null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						GestorSalida.ENVIAR_Im_INFORMACION(perso, "017;" + xp + "~" + oficioID);
						perso.getStatOficioPorID(oficioID).addExperiencia(perso, xp, 0);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_TELEPORT_CASA :// teleportar a casa
					try {
						final Casa casa = Mundo.getCasaDePj(perso.getID());
						if (objUsar == null || casa == null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						if (casa.getActParametros()) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "126");
							return false;
						}
						perso.teleport(casa.getMapaIDDentro(), casa.getCeldaIDDentro());
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_PANEL_CASA_GREMIO :// panel casa de gremio
					GestorSalida.ENVIAR_gUT_PANEL_CASA_GREMIO(perso);
					break;
				case ACCION_DAR_QUITAR_PUNTOS_HECHIZO :// agregar puntos hechizo
					try {
						perso.addPuntosHechizos(Integer.parseInt(_args));
						GestorSalida.ENVIAR_Im_INFORMACION(perso, "016;" + Integer.parseInt(_args));
						GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(perso);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_QUITAR_ENERGIA :// ganar energia
					try {
						int valor = 0;
						String[] s = _args.split(",");
						if (s.length == 1) {
							valor = Integer.parseInt(s[0]);
						} else {
							valor = Formulas.getRandomInt(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
						}
						perso.addEnergiaConIm(valor, true);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_EXPERIENCIA :// ganar experiencia
					try {
						int valor = 0;
						String[] s = _args.split(",");
						if (s.length == 1) {
							valor = Integer.parseInt(s[0]);
						} else {
							valor = Formulas.getRandomInt(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
						}
						perso.addExperiencia(valor, true);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_OLVIDAR_OFICIO :// olvidar oficio
					try {
						final int oficio = Integer.parseInt(_args);
						if (oficio < 1) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						return perso.olvidarOficio(oficio);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
				case ACCION_CAMBIAR_GFX :// cambiar de gfx
					try {
						final short gfxID = Short.parseShort(_args);
						if (gfxID <= 0) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						perso.setGfxID(gfxID);
						perso.refrescarEnMapa();
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DEFORMAR :// deformar
					try {
						perso.setGfxID((short) (perso.getClaseID(true) * 10 + perso.getSexo()));
						perso.refrescarEnMapa();
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_PANEL_CERCADO_GREMIO :// panel cercados de gremio
					GestorSalida.ENVIAR_gUF_PANEL_CERCADOS_GREMIO(perso);
					break;
				case ACCION_SUBIR_BAJAR_MONTURA :// bajar subir montura
					perso.subirBajarMontura(false);
					return false;
				case ACCION_REFRESCAR_MOBS :// refrescar mobs
					perso.getMapa().refrescarGrupoMobs();
					break;
				case ACCION_CAMBIAR_CLASE :// cambiar clase
					try {
						if (perso.getEncarnacionN() != null) {
							GestorSalida.ENVIAR_BN_NADA(perso);
							return false;
						}
						return perso.cambiarClase(Byte.parseByte(_args));
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
				case ACCION_AUMENTAR_RESETS :// aumentar resets
					try {
						if (perso.getEncarnacionN() != null) {
							GestorSalida.ENVIAR_BN_NADA(perso);
							return false;
						}
						if (!perso.estaDisponible(false, false)) {
							GestorSalida.ENVIAR_BN_NADA(perso);
							return false;
						}
						return perso.aumentarReset();
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
				case ACCION_OBJETO_BOOST :// objeto boost (candy, etc) 20
					try {
						String[] args = _args.split(",");
						final Objeto nuevo = Mundo.getObjetoModelo(Integer.parseInt(args[0])).crearObjeto(1,
						Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM);
						if (nuevo.getPosicion() < 20 || nuevo.getPosicion() > 25) {
							return false;
						}
						if (args.length > 1) {
							String stats = nuevo.convertirStatsAString(true);
							nuevo.convertirStringAStats((stats.isEmpty() ? "" : (stats + ",")) + args[1]);
						}
						if (perso.getObjPosicion(nuevo.getPosicion()) != null) {
							perso.borrarOEliminarConOR(perso.getObjPosicion(nuevo.getPosicion()).getID(), true);
						}
						perso.addObjetoConOAKO(nuevo, true);
						GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(perso);
						GestorSalida.ENVIAR_As_STATS_DEL_PJ(perso);
						if (!nuevo.getParamStatTexto(Constantes.STAT_PERSONAJE_SEGUIDOR, 3).isEmpty()) {
							perso.refrescarEnMapa();
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_CAMBIAR_SEXO :// ex-cambiar color
					try {
						perso.cambiarSexo();
						perso.deformar();
						perso.refrescarEnMapa();
						GestorSQL.CAMBIAR_SEXO_CLASE(perso);
					} catch (Exception e) {}
					break;
				case ACCION_PAGAR_PESCAR_KUAKUA :// pagar para pescar kuakua
					try {
						final int kamasApostar = Integer.parseInt(_args);
						final long tempKamas = perso.getKamas();
						if (tempKamas < kamasApostar) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1128;" + kamasApostar);
							return false;
						}
						perso.addKamas(kamasApostar, true, true);
						perso.setPescarKuakua(true);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_RULETA_JALATO :// precio tutorial (feria trool)
					try {
						final long precio = Integer.parseInt(_args.split(",")[0]);
						int tutorial = Integer.parseInt(_args.split(",")[1]);
						if (tutorial == 30) {
							final int aleatorio = Formulas.getRandomInt(1, 200);
							if (aleatorio == 100) {
								tutorial = 31;
							}
						}
						final Tutorial tuto = Mundo.getTutorial(tutorial);
						if (tuto == null || precio < 0) {
							return false;
						}
						if (perso.getKamas() < precio) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "182");
							return false;
						}
						perso.addKamas(-precio, true, true);
						if (tuto.getInicio() != null) {
							tuto.getInicio().realizarAccion(perso, null, -1, (short) -1);
						}
						Thread.sleep(1500);
						GestorSalida.ENVIAR_TC_CARGAR_TUTORIAL(perso, tutorial);
						perso.setTutorial(tuto);
						perso.setOcupado(true);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_ORNAMENTO :// ornamentos
					try {
						perso.addOrnamento(Integer.parseInt(_args));
						perso.setOrnamento(Integer.parseInt(_args));
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_TELEPORT_CELDA_MISMO_MAPA :// mover a una celda en el mismo mapa
					try {
						perso.setCelda(perso.getMapa().getCelda(Short.parseShort(_args)));
						GestorSalida.ENVIAR_GM_REFRESCAR_PJ_EN_MAPA_SIN_HUMO(perso.getMapa(), perso);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_GANAR_RULETA_JALATO :// ganar juego de la ruleta
					try {
						GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE_TODOS("El anutrofado ganador de la RULETA DEL JALATO es: "
						+ perso.getNombre() + ", demosle un fuerte aplauso!!!",
						"Vous avez gagnez en jouant a la roulette du bouftou :  " + perso.getNombre() + ", félicitations !");
						perso.addKamas(MainServidor.KAMAS_RULETA_JALATO, true, true);
						MainServidor.KAMAS_RULETA_JALATO = 10000;
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_KAMAS_RULETA_JALATO :// aumentar el pozo de la ruleta
					MainServidor.KAMAS_RULETA_JALATO += 1000;
					break;
				case ACCION_DAR_SET_OBJETOS :// dar un objeto set
					try {
						for (String s : _args.split(";")) {
							final ObjetoSet OS = Mundo.getObjetoSet(Integer.parseInt(s));
							if (OS == null) {
								GestorSalida.ENVIAR_BN_NADA(objetivo);
								return false;
							}
							for (final ObjetoModelo objMod : OS.getObjetosModelos()) {
								objetivo.addObjIdentAInventario(objMod.crearObjeto(1, Constantes.OBJETO_POS_NO_EQUIPADO,
								CAPACIDAD_STATS.RANDOM), false);
							}
						}
						GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(objetivo);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_CONFIRMAR_CUMPLIO_OBJETIVO_MISION :
					// confirma si se cumplio el objetivo de la mision
					try {
						for (String s : _args.split(";")) {
							for (final Mision mision : perso.getMisiones()) {
								if (mision.estaCompletada()) {
									continue;
								}
								for (final Entry<Integer, Integer> entry : mision.getObjetivos().entrySet()) {
									if (entry.getValue() == Mision.ESTADO_COMPLETADO) {
										continue;
									}
									MisionObjetivoModelo objMod = Mundo.getMisionObjetivoModelo(entry.getKey());
									if (objMod.getID() != Integer.parseInt(s)) {
										continue;
									}
									perso.confirmarObjetivo(mision, objMod, perso, null, false, 0);
								}
							}
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_MISION :// dar mision NPC
					try {
						if (perso.tieneMision(Integer.parseInt(_args))) {
							GestorSalida.ENVIAR_BN_NADA(objetivo, "TIENE MISION");
							return false;
						}
						final MisionModelo misionMod = Mundo.getMision(Integer.parseInt(_args));
						if (misionMod.getEtapas().isEmpty()) {
							GestorSalida.ENVIAR_BN_NADA(objetivo, "ETAPAS VACIAS");
							return false;
						}
						if (Mundo.getEtapa(misionMod.getEtapas().get(0)).getObjetivosPorNivel(0).isEmpty()) {
							GestorSalida.ENVIAR_BN_NADA(objetivo, "OBJETIVOS VACIOS");
							return false;
						}
						perso.addNuevaMision(misionMod);
						GestorSalida.ENVIAR_Im_INFORMACION(perso, "054;" + Integer.parseInt(_args));
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_AGREGAR_MOB_ALBUM :// añadir carta coleccion mob
					try {
						for (String s : _args.split(";")) {
							perso.addCardMob(Integer.parseInt(s));
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(perso, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_TITULO :// dar titulo
					try {
						final byte titulo = Byte.parseByte(_args);
						if (titulo < 1) {
							GestorSalida.ENVIAR_BN_NADA(perso);
							return false;
						}
						perso.addTitulo(titulo, -1);
						perso.refrescarEnMapa();
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_RECOMPENSA_DOPEUL :
					try {
						int exp = 0;
						if (perso.getNivel() < 20) {
							exp = 3000;
						} else if (perso.getNivel() <= 40) {
							exp = 8000;
						} else if (perso.getNivel() <= 60) {
							exp = 19000;
						} else if (perso.getNivel() <= 80) {
							exp = 34000;
						} else if (perso.getNivel() <= 100) {
							exp = 57000;
						} else if (perso.getNivel() <= 120) {
							exp = 90000;
						} else if (perso.getNivel() <= 140) {
							exp = 130000;
						} else if (perso.getNivel() <= 160) {
							exp = 180000;
						} else if (perso.getNivel() <= 180) {
							exp = 245000;
						} else if (perso.getNivel() <= 200) {
							exp = 320000;
						}
						perso.addExperiencia(exp, true);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					return false;
				case ACCION_VERIFICA_MISION_ALMANAX :// verifica si cumple la mision almanax
					return perso.cumplirMisionAlmanax();
				case ACCION_DAR_MISION_PVP_CON_PERGAMINOS : // asignar con pergamino virgenes una mision PVP
					try {
						int cantidad = 1;
						try {
							cantidad = Integer.parseInt(_args);
						} catch (Exception e) {}
						if (objetivo == perso) {
							return false;
						}
						Personaje victima = objetivo;
						final String nombreVict = victima.getNombre();
						Objeto pergamino = Mundo.getObjetoModelo(10085).crearObjeto(cantidad, Constantes.OBJETO_POS_NO_EQUIPADO,
						CAPACIDAD_STATS.RANDOM);
						pergamino.addStatTexto(Constantes.STAT_MISION, "0#0#0#" + nombreVict);
						pergamino.addStatTexto(Constantes.STAT_RANGO, "0#0#" + Integer.toHexString(victima.getGradoAlineacion()));
						pergamino.addStatTexto(Constantes.STAT_NIVEL, "0#0#" + Integer.toHexString(victima.getNivel()));
						pergamino.addStatTexto(Constantes.STAT_ALINEACION, "0#0#" + Integer.toHexString(victima.getAlineacion()));
						perso.addObjetoConOAKO(pergamino, true);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(perso, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_MISION_PVP :// buscar un pj para mision
					try {
						if (perso.getNivel() < MainServidor.NIVEL_MINIMO_PARA_PVP) {
							return false;
						}
						if (perso.getAlineacion() == Constantes.ALINEACION_NEUTRAL) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "134");
							return false;
						}
						MisionPVP mision = perso.getMisionPVP();
						if (mision != null) {
							if (System.currentTimeMillis() - mision.getTiempoInicio() < MainServidor.MINUTOS_MISION_PVP * 60 * 1000) {
								if (perso.getCuenta().getIdioma().equalsIgnoreCase("fr")) {
									GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso,
									"<b>[Thomas Sacre]</b> Tu viens de terminer un contrat, tu dois attendre 10 minutes avant de te relancer dans ta quête de meurtre.",
									"000000");
								} else {
									GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso,
									"<b>[Thomas Sacre]</b> Usted acaba de terminar un contrato, por ahora debes descansar 10 minutos.",
									"000000");
								}
								return false;
							}
						}
						if (!perso.alasActivadas()) {
							perso.botonActDesacAlas('+');
						}
						Personaje victima = null;
						final ArrayList<Personaje> victimas = new ArrayList<Personaje>();
						for (final Personaje temp : Mundo.getPersonajesEnLinea()) {
							if (temp.getNivel() < MainServidor.NIVEL_MINIMO_PARA_PVP) {
								continue;
							}
							if (temp == perso || temp.getAlineacion() == perso.getAlineacion() || (temp
							.getAlineacion() == Constantes.ALINEACION_NEUTRAL) || !temp.alasActivadas()) {
								continue;
							}
							if (!MainServidor.ES_LOCALHOST) {
								if (temp.getNombre().equalsIgnoreCase(perso.getUltMisionPVP()) || temp.getCuenta().getAdmin() > 0) {
									continue;
								}
							}
							if (!MainServidor.PARAM_PERMITIR_MULTICUENTA_PELEA_PVP) {
								if (temp.getCuenta().getActualIP().equals(perso.getCuenta().getActualIP())) {
									continue;
								}
							}
							if (perso.getNivel() + MainServidor.RANGO_NIVEL_PVP >= temp.getNivel() && perso.getNivel()
							- MainServidor.RANGO_NIVEL_PVP <= temp.getNivel()) {
								victimas.add(temp);
							}
						}
						if (victimas.isEmpty()) {
							if (perso.getCuenta().getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso,
								"<b>[Thomas Sacre]</b> Je ne trouve pas de victime à ta hauteur, reviens plus tard.", "000000");
							} else {
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso,
								"<b>[Thomas Sacre]</b> No se encontró ningún personaje a tu altura, porfavor regresa más tarde.",
								"000000");
							}
							return false;
						}
						victima = victimas.get(Formulas.getRandomInt(0, victimas.size() - 1));
						final String nombreVict = victima.getNombre();
						if (perso.getCuenta().getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>[Thomas Sacre]</b> Ta victime est : " + nombreVict + ".",
							"000000");
						} else {
							GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>[Thomas Sacre]</b> Usted esta ahora a la caza de "
							+ nombreVict + ".", "000000");
						}
						long recompensaExp = Formulas.getXPMision(victima.getNivel());
						MisionPVP misionPVP = new MisionPVP(System.currentTimeMillis(), nombreVict, MainServidor.MISION_PVP_KAMAS,
						recompensaExp, Constantes.getCraneoPorClase(victima.getClaseID(true)));
						Objeto pergamino = Mundo.getObjetoModelo(10085).crearObjeto(20, Constantes.OBJETO_POS_NO_EQUIPADO,
						CAPACIDAD_STATS.RANDOM);
						pergamino.addStatTexto(Constantes.STAT_INTERCAMBIABLE_DESDE, ObjetoModelo.stringFechaIntercambiable(365));
						pergamino.addStatTexto(Constantes.STAT_MISION, "0#0#0#" + nombreVict);
						pergamino.addStatTexto(Constantes.STAT_RANGO, "0#0#" + Integer.toHexString(victima.getGradoAlineacion()));
						pergamino.addStatTexto(Constantes.STAT_NIVEL, "0#0#" + Integer.toHexString(victima.getNivel()));
						pergamino.addStatTexto(Constantes.STAT_ALINEACION, "0#0#" + Integer.toHexString(victima.getAlineacion()));
						perso.addObjetoConOAKO(pergamino, true);
						perso.setUltMisionPVP(nombreVict);
						perso.setMisionPVP(misionPVP);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_GEOPOSICION_MISION_PVP :// geoposicion de la victima
					try {
						if (perso.getPelea() != null || perso.esFantasma() || objUsar == null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						final Personaje victima = Mundo.getPersonajePorNombre(objUsar.getParamStatTexto(Constantes.STAT_MISION, 4));
						if (victima == null || !victima.enLinea()) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1211");
							return false;
						}
						if (victima.esFantasma()) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1OBJETIVE_GHOST");
							return false;
						}
						if (perso.getMisionPVP() == null || !perso.getMisionPVP().getNombreVictima().equalsIgnoreCase(victima
						.getNombre())) {
							long recompensaExp = 0;
							if (objUsar.tieneStatTexto(Constantes.STAT_INTERCAMBIABLE_DESDE)) {
								recompensaExp = Formulas.getXPMision(victima.getNivel());
							}
							perso.setMisionPVP(new MisionPVP(0, victima.getNombre(), objUsar.getStatValor(
							Constantes.STAT_GANAR_KAMAS), recompensaExp, victima.getClaseID(true)));
						}
						GestorSalida.ENVIAR_IC_PERSONAJE_BANDERA_COMPAS(perso, victima.getMapa().getX() + "|" + victima.getMapa()
						.getY());
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_TELEPORT_MISION_PVP :// teleporta a los 2 en un mapa para la caceria
					try {
						if (perso.getPelea() != null || perso.esFantasma() || objUsar == null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						final Personaje victima = Mundo.getPersonajePorNombre(objUsar.getParamStatTexto(989, 4));
						if (victima == null || !victima.enLinea()) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1211");
							return false;
						}
						if (victima.esFantasma()) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1OBJETIVE_GHOST");
							return false;
						}
						if (!victima.alasActivadas()) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1195");
							return false;
						}
						if (victima.getPelea() != null) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1OBJETIVE_IN_FIGHT");
							return false;
						}
						if (!victima.getHuir()) {
							if (System.currentTimeMillis() - victima.getTiempoAgre() > 10000) {
								victima.setHuir(true);
							} else {
								GestorSalida.ENVIAR_Im_INFORMACION(perso, "1OBJETIVE_IN_FIGHT");
								return false;
							}
						}
						final Short[] mapas = {4422, 7810, 952, 1887, 833};
						final short mapa = mapas[Formulas.getRandomInt(0, 4)];
						perso.teleport(mapa, (short) 399);
						victima.teleport(mapa, (short) 194);
						perso.setHuir(false);
						victima.setHuir(false);
						perso.setTiempoAgre(System.currentTimeMillis());
						victima.setTiempoAgre(System.currentTimeMillis());
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_CONFIRMA_CUMPLIO_MISION : // confirma si se cumplio la mision
					try {
						for (String s : _args.split(";")) {
							for (final Mision mision : perso.getMisiones()) {
								if (mision.estaCompletada()) {
									continue;
								}
								if (mision.getIDModelo() != Integer.parseInt(s)) {
									continue;
								}
								for (final Entry<Integer, Integer> entry : mision.getObjetivos().entrySet()) {
									if (entry.getValue() == Mision.ESTADO_COMPLETADO) {
										continue;
									}
									MisionObjetivoModelo objMod = Mundo.getMisionObjetivoModelo(entry.getKey());
									perso.confirmarObjetivo(mision, objMod, perso, null, false, 0);
								}
							}
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_BOOST_FULL_STATS :// boostear todos los stats
					try {
						final int args = Integer.parseInt(_args);
						if (args < 10 || args > 15) {
							return false;
						}
						perso.boostStat2(args, perso.getCapital());
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_PAGAR_PARA_REALIZAR_ACCION :// pagar para realizar una accion
					try {
						final String[] sep = _args.split(Pattern.quote("|"));
						int precio = Integer.parseInt(sep[0]);
						if (perso.getKamas() >= precio) {
							perso.addKamas(-precio, true, true);
							String args = "";
							try {
								args = sep[1].split(";", 2)[1];
							} catch (final Exception e1) {}
							realizar_Accion_Estatico(Integer.parseInt(sep[1].split(";")[0]), args, perso, null, -1, (short) -1);
						} else {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "182");
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_SOLICITAR_OBJETOS_PARA_DAR_OTROS :
					// quita unos objetos para dar otros , pedir, solicitar
					try {
						String[] t = _args.split(Pattern.quote("|"));
						String args = t[0];
						if (t.length < 2) {
							if (objUsar == null) {
								GestorSalida.ENVIAR_Im_INFORMACION(perso, "14");
								return false;
							}
						} else {
							args = t[1];
							for (String s : t[0].split(";")) {
								final int id = Integer.parseInt(s.split(",")[0]);
								final int cant = Integer.parseInt(s.split(",")[1]);
								if (!perso.tieneObjPorModYCant(id, cant)) {
									GestorSalida.ENVIAR_Im_INFORMACION(perso, "14");
									return false;
								}
							}
							for (String s : t[0].split(";")) {
								final int id = Integer.parseInt(s.split(",")[0]);
								final int cant = Integer.parseInt(s.split(",")[1]);
								if (perso.tenerYEliminarObjPorModYCant(id, cant)) {
									GestorSalida.ENVIAR_Im_INFORMACION(perso, "022;" + cant + "~" + id);
								}
							}
						}
						for (String s : args.split(";")) {
							final int id = Integer.parseInt(s.split(",")[0]);
							final int cant = Integer.parseInt(s.split(",")[1]);
							boolean max = false;
							try {
								if (s.split(",").length > 2)
									max = s.split(",")[2].equals("1");
							} catch (Exception e) {}
							ObjetoModelo tempObjMod = Mundo.getObjetoModelo(id);
							if (tempObjMod == null) {
								GestorSalida.ENVIAR_BN_NADA(objetivo, "BUG ACCION " + _id + " idObjMod " + id);
								continue;
							}
							if (cant > 0) {
								perso.addObjIdentAInventario(tempObjMod.crearObjeto(cant, Constantes.OBJETO_POS_NO_EQUIPADO,
								CAPACIDAD_STATS.RANDOM), max);
								GestorSalida.ENVIAR_Im_INFORMACION(perso, "021;" + cant + "~" + id);
							}
						}
						GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(perso);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_REVIVIR :// revivir siendo fantasma, resucitar
					if (objetivo.getPelea() != null) {
						return false;
					}
					objetivo.revivir(true);
					break;
				case ACCION_ABRIR_DOCUMENTO :// abrir documento
					GestorSalida.ENVIAR_dC_ABRIR_DOCUMENTO(perso, _args);
					break;
				case ACCION_DAR_SET_OBJETOS_POR_FICHAS :// set por fichas
					try {
						final int idSet = Integer.parseInt(_args.split(",")[0]);
						final int fichas = Integer.parseInt(_args.split(",")[1]);
						final ObjetoSet OS = Mundo.getObjetoSet(idSet);
						if (OS == null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						if (perso.tenerYEliminarObjPorModYCant(1749, fichas)) {
							for (final ObjetoModelo objM : OS.getObjetosModelos()) {
								objetivo.addObjIdentAInventario(objM.crearObjeto(1, Constantes.OBJETO_POS_NO_EQUIPADO,
								CAPACIDAD_STATS.RANDOM), false);
								GestorSalida.ENVIAR_Im_INFORMACION(perso, "021;1~" + objM.getID());
							}
						} else {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "14|43");
						}
						GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(objetivo);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_REALIZAR_ACCION_PJS_EN_MAPA_POR_ALINEACION_Y_DISTANCIA :// realizar accion a
																																						// personajes en el mapa
					try {
						String[] t = _args.split(Pattern.quote("|"));
						int tipoAlin = 0;
						try {
							tipoAlin = Integer.parseInt(t[0]);
						} catch (Exception e) {}
						int dist = 1000;
						try {
							dist = Integer.parseInt(t[1]);
						} catch (Exception e) {}
						int idAccion = Integer.parseInt(t[2].split(";")[0]);
						String args2 = "";
						try {
							args2 = t[2].split(";", 2)[1];
						} catch (final Exception e1) {}
						Mapa mapa = perso.getMapa();
						short celdaPerso = perso.getCelda().getID();
						ArrayList<Personaje> aplicar = new ArrayList<>();
						for (final Personaje o : mapa.getArrayPersonajes()) {
							if (tipoAlin == 0) {
								// pasan normal
							} else {
								if (o.getAlineacion() == Constantes.ALINEACION_NEUTRAL) {
									continue;
								}
								if (tipoAlin == 1 && o.getAlineacion() != perso.getAlineacion()) {
									continue;
								} else if (tipoAlin == 2 && o.getAlineacion() == perso.getAlineacion()) {
									continue;
								}
							}
							if (o.getMapa().getID() != mapa.getID()) {
								continue;
							}
							if (Camino.distanciaDosCeldas(mapa, o.getCelda().getID(), celdaPerso) > dist) {
								continue;
							}
							aplicar.add(o);
						}
						for (Personaje o : aplicar) {
							realizar_Accion_Estatico(idAccion, args2, o, null, -1, (short) -1);
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_LIBERAR_TUMBA :// liberar un tumba
					if (objetivo.getPelea() != null) {
						return false;
					}
					if (objetivo.esTumba()) {
						objetivo.convertirseFantasma();
					}
					break;
				case ACCION_REVIVIR2 :// lo mismo q el 57
					if (objetivo.getPelea() != null) {
						return false;
					}
					objetivo.revivir(true);
					break;
				case ACCION_AGREGAR_PJ_LIBRO_ARTESANOS :// agregar en el libro de artesanos
					try {
						int idOficio = Integer.parseInt(_args);
						for (final StatOficio SO : perso.getStatsOficios().values()) {
							if (SO.getOficio().getID() == idOficio) {
								SO.setLibroArtesano(true);
								GestorSalida.ENVIAR_Ej_AGREGAR_LIBRO_ARTESANO(perso, "+" + idOficio);
								return false;
							}
						}
					} catch (final Exception e) {}
					GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
					return false;
				case ACCION_ACTIVAR_CELDAS_INTERACTIVAS :// 64 activar celda interactivas
					try {
						for (String s : _args.split(";")) {
							short m = perso.getMapa().getID();
							short c = -1;
							String[] split = s.split(",");
							try {
								String cm = split[0];
								c = Short.parseShort(cm.split("m")[0]);
								m = Short.parseShort(cm.split("m")[1]);
							} catch (Exception e) {}
							boolean bAnimacionMovimiento = false;// conGDF
							long milisegundos = 30000;
							try {
								bAnimacionMovimiento = split[1].equals("1");
							} catch (Exception e) {}
							try {
								milisegundos = Long.parseLong(split[2]);
							} catch (Exception e) {}
							Mapa mapa = Mundo.getMapa(m);
							if (mapa == null) {
								GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id + " MAPA " + m + " ES NULO");
								return false;
							}
							Celda celda = mapa.getCelda(c);
							if (celda == null) {
								GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id + " MAPA " + m + " CELDA " + c
								+ " ES NULO");
								return false;
							}
							celda.activarCelda(bAnimacionMovimiento, milisegundos);
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_QUITAR_EMOTE :// agregar/borrar un emote
					try {
						for (String s : _args.split(",")) {
							byte emote = Byte.parseByte(s);
							if (emote < 0) {
								emote = (byte) Math.abs(emote);
								if (perso.borrarEmote(emote)) {
									GestorSalida.ENVIAR_eR_BORRAR_EMOTE(perso, emote, true);
								}
							} else if (emote > 0) {
								if (perso.addEmote(emote)) {
									GestorSalida.ENVIAR_eA_AGREGAR_EMOTE(perso, emote, true);
								}
							}
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_SOLICITAR_OBJETOS_PARA_REALIZAR_ACCION :
					// quitar o restar el objeto para realizar una accion
					try {
						String[] t = _args.split(Pattern.quote("|"));
						String nuevaAccion = t[0];
						if (t.length < 2) {
							if (objUsar == null) {
								GestorSalida.ENVIAR_Im_INFORMACION(perso, "14");
								return false;
							}
						} else {
							nuevaAccion = t[1];
							String solicita = t[0];
							for (String s : solicita.split(";")) {
								final int id = Integer.parseInt(s.split(",")[0]);
								final int cant = Math.abs(Integer.parseInt(s.split(",")[1]));
								if (!perso.tieneObjPorModYCant(id, cant)) {
									GestorSalida.ENVIAR_Im_INFORMACION(perso, "14");
									return false;
								}
							}
							for (String s : solicita.split(";")) {
								final int id = Integer.parseInt(s.split(",")[0]);
								final int cant = Math.abs(Integer.parseInt(s.split(",")[1]));
								if (perso.tenerYEliminarObjPorModYCant(id, cant)) {
									GestorSalida.ENVIAR_Im_INFORMACION(perso, "022;" + cant + "~" + id);
								}
							}
						}
						String args2 = "";
						int accionID = Integer.parseInt(nuevaAccion.split(";")[0]);
						try {
							args2 = nuevaAccion.split(";", 2)[1];
						} catch (final Exception e1) {}
						realizar_Accion_Estatico(accionID, args2, perso, null, -1, (short) -1);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_CAMBIAR_ROSTRO : // cambiar rostro
					try {
						perso.cambiarRostro(Byte.parseByte(_args));
						GestorSalida.ENVIAR_Oa_CAMBIAR_ROPA_MAPA(perso.getMapa(), perso);
					} catch (final Exception e) {}
					break;
				case ACCION_MENSAJE_INFORMACION :// mensaje informacion
					GestorSalida.ENVIAR_Im_INFORMACION(perso, _args);
					break;
				case ACCION_MENSAJE_PANEL :// mensaje panel
					GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(perso, _args);
					break;
				case ACCION_DAR_OBJETOS_DE_LOS_STATS :// dar objeto de los stats
					if (objUsar == null) {
						return false;
					}
					for (String s : objUsar.strDarObjetos().split(";")) {
						try {
							if (s.isEmpty()) {
								continue;
							}
							final int id = Integer.parseInt(s.split(",")[0]);
							final int cant = Integer.parseInt(s.split(",")[1]);
							ObjetoModelo tempObjMod = Mundo.getObjetoModelo(id);
							if (tempObjMod == null) {
								GestorSalida.ENVIAR_BN_NADA(objetivo, "BUG ACCION " + _id + " idObjMod " + id);
								continue;
							}
							if (cant > 0) {
								perso.addObjIdentAInventario(tempObjMod.crearObjeto(cant, Constantes.OBJETO_POS_NO_EQUIPADO,
								CAPACIDAD_STATS.RANDOM), false);
								GestorSalida.ENVIAR_Im_INFORMACION(perso, "021;" + cant + "~" + id);
							}
						} catch (Exception e) {
							GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						}
					}
					break;
				case ACCION_DAR_ABONO_DIAS :// abono por dias
					try {
						int idInt = Integer.parseInt(_args);
						long abono = Math.max(GestorSQL.GET_ABONO(objetivo.getCuenta().getNombre()), System.currentTimeMillis());
						abono += (idInt * 24 * 3600 * 1000);
						abono = Math.max(abono, System.currentTimeMillis() - 1000);
						GestorSQL.SET_ABONO(abono, objetivo.getCuentaID());
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_ABONO_HORAS :// abono por horas
					try {
						int idInt = Integer.parseInt(_args);
						long abono = Math.max(GestorSQL.GET_ABONO(objetivo.getCuenta().getNombre()), System.currentTimeMillis());
						abono += (idInt * 3600 * 1000);
						abono = Math.max(abono, System.currentTimeMillis() - 1000);
						GestorSQL.SET_ABONO(abono, objetivo.getCuentaID());
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_ABONO_MINUTOS :// abono por minutos
					try {
						int idInt = Integer.parseInt(_args);
						long abono = Math.max(GestorSQL.GET_ABONO(objetivo.getCuenta().getNombre()), System.currentTimeMillis());
						abono += (idInt * 60 * 1000);
						abono = Math.max(abono, System.currentTimeMillis() - 1000);
						GestorSQL.SET_ABONO(abono, objetivo.getCuentaID());
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_NIVEL_DE_ORDEN :// add nivel orden / nivel dones
					try {
						objetivo.addOrdenNivel(Integer.parseInt(_args));
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_ORDEN :// setear orden id / set orden o don
					try {
						objetivo.setOrden(Integer.parseInt(_args));
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_BORRAR_OBJETO_MODELO :// 76 eliminar objeto modelo y por recibido desde
					try {
						boolean b = false;
						for (String s : _args.split(";")) {
							String[] args = s.split(",");
							final int id = Integer.parseInt(args[0]);
							int minutos = 0;
							try {
								minutos = Integer.parseInt(args[1]);
							} catch (Exception e) {}
							int borrados = perso.eliminarPorObjModeloRecibidoDesdeMinutos(id, minutos);
							if (borrados > 0) {
								b = true;
								GestorSalida.ENVIAR_Im_INFORMACION(perso, "022;" + borrados + "~" + id);
							}
						}
						if (b) {
							GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(perso);
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_VERIFICA_STAT_OBJETO_Y_LO_BORRA :// verifica si tiene un stat y si lo tiene lo
																											// borra
					try {
						boolean b = true;
						for (String s : _args.split(";")) {
							boolean bb = false;
							String[] args = s.split(",");
							final int idObjModelo = Integer.parseInt(args[0]);
							final int llaveID = Integer.parseInt(args[1]);
							for (Objeto obj : perso.getObjetosTodos()) {
								if (obj.getObjModeloID() != idObjModelo) {
									continue;
								}
								String[] stats = obj.convertirStatsAString(true).split(",");
								for (String st : stats) {
									int statID = Integer.parseInt(st.split("#")[0], 16);
									int tempObjetoID = Integer.parseInt(st.split("#")[3], 16);
									if (statID != Constantes.STAT_LLAVE_MAZMORRA) {
										continue;
									}
									if (tempObjetoID == llaveID) {
										bb = true;
									}
								}
								if (bb) {
									break;
								}
							}
							b &= bb;
						}
						if (b) {
							for (String s : _args.split(";")) {
								String[] args = s.split(",");
								final int id = Integer.parseInt(args[0]);
								final int objetoID = Integer.parseInt(args[1]);
								for (Objeto obj : perso.getObjetosTodos()) {
									if (obj.getObjModeloID() != id) {
										continue;
									}
									String[] stats = obj.convertirStatsAString(true).split(",");
									StringBuilder nuevo = new StringBuilder();
									b = false;
									for (String st : stats) {
										if (nuevo.length() > 0) {
											nuevo.append(",");
										}
										int statID = Integer.parseInt(st.split("#")[0], 16);
										int tempObjetoID = Integer.parseInt(st.split("#")[3], 16);
										if (statID == Constantes.STAT_LLAVE_MAZMORRA && tempObjetoID == objetoID) {
											b = true;
										} else {
											nuevo.append(st);
										}
									}
									if (b) {
										obj.convertirStringAStats(nuevo.toString());
										GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(perso, obj);
										break;
									}
								}
							}
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_BORRAR_OBJETO_AL_AZAR_PARA_DAR_OTROS :// quita 1 objeto al azar, y da objetos
					try {
						final String[] quitar = _args.split(Pattern.quote("|"))[0].split(";");
						final String[] dar = _args.split(Pattern.quote("|"))[1].split(";");
						boolean quito = false;
						ArrayList<String> array = new ArrayList<>();
						for (String s : quitar) {
							array.add(s);
						}
						while (!array.isEmpty()) {
							int random = new Random().nextInt(array.size());
							String s = array.get(random);
							final int id = Integer.parseInt(s.split(",")[0]);
							final int cant = Math.abs(Integer.parseInt(s.split(",")[1]));
							if (perso.tenerYEliminarObjPorModYCant(id, cant)) {
								quito = true;
								GestorSalida.ENVIAR_Im_INFORMACION(perso, "022;" + cant + "~" + id);
								break;
							}
							array.remove(random);
						}
						if (quito) {
							for (String s : dar) {
								final int id = Integer.parseInt(s.split(",")[0]);
								final int cant = Integer.parseInt(s.split(",")[1]);
								ObjetoModelo tempObjMod = Mundo.getObjetoModelo(id);
								if (tempObjMod == null) {
									GestorSalida.ENVIAR_BN_NADA(objetivo, "BUG ACCION " + _id + " idObjMod " + id);
									continue;
								} else {
									perso.addObjIdentAInventario(tempObjMod.crearObjeto(cant, Constantes.OBJETO_POS_NO_EQUIPADO,
									CAPACIDAD_STATS.RANDOM), false);
									GestorSalida.ENVIAR_Im_INFORMACION(perso, "021;" + cant + "~" + id);
								}
							}
							GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(perso);
						} else {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "14|43");
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_GDF_PERSONA :// GDF persona
					try {
						for (String s : _args.split(";")) {
							short c = Short.parseShort(s.split(",")[0]);
							int estado = 3;
							int interactivo = 0;
							try {
								estado = Integer.parseInt(s.split(",")[1]);
							} catch (Exception e) {}
							try {
								interactivo = Integer.parseInt(s.split(",")[2]);
							} catch (Exception e) {}
							GestorSalida.ENVIAR_GDF_FORZADO_PERSONAJE(perso, perso.getMapa().getCelda(c).getID() + ";" + estado + ";"
							+ interactivo);
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_GDF_MAPA :// GDF mapa
					try {
						for (String s : _args.split(";")) {
							short c = Short.parseShort(s.split(",")[0]);
							int estado = 3;
							int interactivo = 0;
							try {
								estado = Integer.parseInt(s.split(",")[1]);
							} catch (Exception e) {}
							try {
								interactivo = Integer.parseInt(s.split(",")[2]);
							} catch (Exception e) {}
							GestorSalida.ENVIAR_GDF_FORZADO_MAPA(perso.getMapa(), perso.getMapa().getCelda(c).getID() + ";" + estado
							+ ";" + interactivo);
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_RULETA_PREMIOS :// ruleta premios
					String premios = Mundo.RULETA.get(objUsar.getObjModeloID());
					GestorSalida.ENVIAR_brP_RULETA_PREMIOS(perso, premios + ";" + objUsar.getObjModeloID());
					return false;
				case ACCION_ENVIAR_PACKET :// envia un packet
					GestorSalida.enviar(perso, _args);
					break;
				case ACCION_DAR_HABILIDAD_MONTURA :// 100 asignar una habilidad a monturas
					try {
						if (perso.getMontura() == null) {
							return false;
						}
						for (String s : _args.split(",")) {
							if (s.isEmpty()) {
								continue;
							}
							byte habilidad = Byte.parseByte(s);
							perso.getMontura().addHabilidad(habilidad);
						}
						GestorSalida.ENVIAR_Re_DETALLES_MONTURA(perso, "+", perso.getMontura());
						GestorSQL.REPLACE_MONTURA(perso.getMontura(), false);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_CASAR_DOS_PJS :// casar 2 pjs
					try {
						if (perso.getMapa().getID() != 2019) {
							return false;
						}
						if ((perso.getSexo() == Constantes.SEXO_MASCULINO && perso.getCelda().getID() == 282) || (perso
						.getSexo() == Constantes.SEXO_FEMENINO && perso.getCelda().getID() == 297)) {
							// no pasa nada
						} else {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1102");
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DISCURSO_SACEDORTE :// discurso del sacerdote para casarse
					perso.preguntaCasarse();
					break;
				case ACCION_DIVORCIARSE :// divorciarse
					try {
						if (perso.getMapa().getID() != 2019) {
							return false;
						}
						final int precio = 50000;
						if (perso.getKamas() < precio) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1128;" + precio);
							return false;
						}
						perso.addKamas(-precio, true, true);
						final Personaje esposo = Mundo.getPersonaje(perso.getEsposoID());
						if (esposo != null) {
							esposo.divorciar();
						}
						perso.divorciar();
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_OGRINAS :// 104 dar ogrinas
					try {
						int idInt = 0;
						if (objUsar == null) {
							idInt = Integer.parseInt(_args);
						} else {
							try {
								idInt = Integer.parseInt(_args);
							} catch (final Exception e) {
								idInt = objUsar.getStatValor(Constantes.STAT_DAR_OGRINAS);
							}
						}
						if (idInt != 0) {
							GestorSQL.SET_OGRINAS_CUENTA(GestorSQL.GET_OGRINAS_CUENTA(objetivo.getCuentaID()) + idInt, objetivo
							.getCuentaID());
						} else {
							return false;
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_DAR_CREDITOS :// 105 dar creditos
					try {
						int idInt = 0;
						if (objUsar == null) {
							idInt = Integer.parseInt(_args);
						} else {
							try {
								idInt = Integer.parseInt(_args);
							} catch (final Exception e) {
								idInt = objUsar.getStatValor(Constantes.STAT_DAR_CREDITOS);
							}
						}
						if (idInt != 0) {
							GestorSQL.SET_CREDITOS_CUENTA(GestorSQL.GET_CREDITOS_CUENTA(objetivo.getCuentaID()) + idInt, objetivo
							.getCuentaID());
						} else {
							return false;
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_AGREGAR_OBJETO_A_CERCADO :// agregar objeto en un cercado
					try {
						final Cercado cercado = perso.getMapa().getCercado();
						if (cercado == null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						if (!perso.getNombre().equalsIgnoreCase("Elbusta")) {
							if (perso.getGremio() == null || cercado.getGremio().getID() != perso.getGremio().getID()) {
								GestorSalida.ENVIAR_Im_INFORMACION(perso, "1100");
								return false;
							}
							if (!perso.getMiembroGremio().puede(Constantes.G_MEJORAR_CERCADOS)) {
								GestorSalida.ENVIAR_Im_INFORMACION(perso, "193");
								return false;
							}
							if (!cercado.getCeldasObj().contains(celdaID)) {
								GestorSalida.ENVIAR_BN_NADA(objetivo);
								return false;
							}
						}
						if (cercado.getCantObjColocados() < cercado.getCantObjMax()) {
							cercado.addObjetoCria(celdaID, objUsar, perso.getID());
							int nuevaCantidad = objUsar.getCantidad() - 1;
							if (nuevaCantidad >= 1) {
								Objeto nuevoObj = objUsar.clonarObjeto(nuevaCantidad, objUsar.getPosicion());
								perso.addObjIdentAInventario(nuevoObj, false);
							}
							objUsar.setCantidad(1);
							perso.borrarOEliminarConOR(idObjUsar, false);
							GestorSalida.ENVIAR_GDO_OBJETO_TIRAR_SUELO(perso.getMapa(), '+', celdaID, objUsar.getObjModeloID(), true,
							objUsar.getDurabilidad() + ";" + objUsar.getDurabilidadMax());
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "022;" + 1 + "~" + objUsar.getObjModeloID());
						} else {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1107");
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
					}
					return false;
				case ACCION_AGREGAR_PRISMA_A_MAPA :// agregar prisma
					try {
						final Mapa mapa = perso.getMapa();
						final byte alineacion = perso.getAlineacion();
						if (perso.getDeshonor() > 0) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "183");
							return false;
						}
						if (perso.getGradoAlineacion() < 3) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1155");
							return false;
						}
						if (alineacion != Constantes.ALINEACION_BONTARIANO && alineacion != Constantes.ALINEACION_BRAKMARIANO) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "134|43");
							return false;
						}
						if (!perso.alasActivadas()) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1148");
							return false;
						}
						if (mapa.mapaNoPrisma()) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1146");
							return false;
						}
						final SubArea subarea = mapa.getSubArea();
						if (subarea.getAlineacion() != Constantes.ALINEACION_NEUTRAL || !subarea.esConquistable()) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1149");
							return false;
						}
						if (objUsar == null) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "14");
							return false;
						}
						final Area area = subarea.getArea();
						boolean cambio = area.getAlineacion() == Constantes.ALINEACION_NEUTRAL;
						final Prisma prisma = new Prisma(Mundo.sigIDPrisma(), alineacion, (byte) 1, mapa.getID(), perso.getCelda()
						.getID(), 0, area.getAlineacion() == Constantes.ALINEACION_NEUTRAL ? area.getID() : -1, subarea.getID(), 0);
						Mundo.addPrisma(prisma);
						for (final Personaje pj : Mundo.getPersonajesEnLinea()) {
							GestorSalida.ENVIAR_am_CAMBIAR_ALINEACION_SUBAREA(pj, subarea.getID(), alineacion, pj
							.getAlineacion() != Constantes.ALINEACION_NEUTRAL);
							if (cambio) {
								GestorSalida.ENVIAR_aM_CAMBIAR_ALINEACION_AREA(pj, area.getID(), alineacion);
							}
						}
						GestorSalida.ENVIAR_GM_PRISMA_A_MAPA(mapa, "+" + prisma.stringGM());
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_LANZAR_ANIMACION ://
					try {
						String[] args = _args.split(",");
						Animacion animacion = Mundo.getAnimacion(Integer.parseInt(args[0]));
						if (perso.getPelea() != null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						GestorSalida.ENVIAR_GA_ACCION_JUEGO_AL_MAPA(perso.getMapa(), 0, 227, perso.getID() + ";" + celdaID + ","
						+ animacion.getAnimacionID() + "," + animacion.getTipoDisplay() + "," + animacion.getSpriteAnimacion() + ","
						+ args[1] + "," + animacion.getDuracion() + "," + animacion.getTalla(), "");
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_LANZAR_ANIMACION2 :// hadas artificiales
					try {
						String[] args = _args.split(",");
						Animacion animacion = Mundo.getAnimacion(Integer.parseInt(args[0]));
						if (perso.getPelea() != null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						GestorSalida.ENVIAR_GA_ACCION_JUEGO_AL_MAPA(perso.getMapa(), 0, 228, perso.getID() + ";" + celdaID + ","
						+ animacion.getAnimacionID() + "," + animacion.getTipoDisplay() + "," + animacion.getSpriteAnimacion() + ","
						+ animacion.getLevel() + "," + animacion.getDuracion(), "");
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_TIEMPO_PROTECCION_PRISMA :
					try {
						final Mapa mapa = perso.getMapa();
						Prisma prisma = mapa.getPrisma();
						if (prisma == null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						if (prisma.getPelea() != null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						prisma.addTiempProtecion(Integer.parseInt(_args));
						long t = prisma.getTiempoRestProteccion();
						int[] f = Formulas.formatoTiempo(t);
						GestorSalida.ENVIAR_Im_INFORMACION(objetivo, "1TIENE_PROTECCION;" + f[4] + "~" + f[3] + "~" + f[2] + "~"
						+ f[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				case ACCION_OLVIDAR_HECHIZO_RECAUDADOR :
					try {
						if (perso.getGremio() == null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo, "NO TIENE GREMIO");
							return false;
						}
						return perso.getGremio().olvidarHechizo(Integer.parseInt(_args), true);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
				case ACCION_TIEMPO_PROTECCION_RECAUDADOR :
					try {
						final Mapa mapa = perso.getMapa();
						Recaudador recaudador = mapa.getRecaudador();
						if (recaudador == null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						if (recaudador.getPelea() != null) {
							GestorSalida.ENVIAR_BN_NADA(objetivo);
							return false;
						}
						recaudador.addTiempProtecion(Integer.parseInt(_args));
						long t = recaudador.getTiempoRestProteccion();
						int[] f = Formulas.formatoTiempo(t);
						GestorSalida.ENVIAR_Im_INFORMACION(objetivo, "1TIENE_PROTECCION;" + f[4] + "~" + f[3] + "~" + f[2] + "~"
						+ f[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(objetivo, "EXCEPTION ACCION " + _id);
						return false;
					}
					break;
				default :
					MainServidor.redactarLogServidorln("Accion ID = " + _id + " no implantada");
					return false;
			}
			return true;
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION id: " + _id + " args: " + _args + ", realizar_Accion_Estatico " + e
			.toString());
			e.printStackTrace();
			return false;
		}
	}
}
