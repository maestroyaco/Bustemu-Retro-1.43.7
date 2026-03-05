package estaticos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;
import servidor.ServidorServer;
import servidor.ServidorServer.RefrescarTodosMobs;
import servidor.ServidorServer.SalvarServidor;
import servidor.ServidorServer.Reiniciar;
import servidor.ServidorSocket;
import variables.casa.Casa;
import variables.gremio.Recaudador;
import variables.hechizo.EfectoHechizo;
import variables.hechizo.Hechizo;
import variables.hechizo.StatHechizo;
import variables.mapa.Celda;
import variables.mapa.Mapa;
import variables.mapa.interactivo.OtroInteractivo;
import variables.mision.MisionEtapaModelo;
import variables.mision.Mision;
import variables.mision.MisionModelo;
import variables.mision.MisionObjetivoModelo;
import variables.mob.GrupoMob;
import variables.mob.MobGradoModelo;
import variables.mob.MobModelo;
import variables.mob.MobModelo.TipoGrupo;
import variables.montura.Montura;
import variables.npc.NPC;
import variables.npc.NPCModelo;
import variables.npc.PreguntaNPC;
import variables.npc.RespuestaNPC;
import variables.objeto.MascotaModelo;
import variables.objeto.Objeto;
import variables.objeto.ObjetoModelo;
import variables.objeto.ObjetoSet;
import variables.objeto.ObjetoModelo.CAPACIDAD_STATS;
import variables.oficio.StatOficio;
import variables.pelea.DropMob;
import variables.pelea.Pelea;
import variables.personaje.Cuenta;
import variables.personaje.Personaje;
import variables.zotros.Accion;
import variables.zotros.Almanax;
import variables.zotros.Prisma;
import estaticos.Mundo.Duo;

public class Comandos {
	public static void consolaComando(final String mensaje, final Cuenta _cuenta, final Personaje _perso) {
		try {
			final String[] infos = mensaje.split(" ");
			final String comando = infos[0].toUpperCase();
			int rangoJugador = _cuenta.getAdmin();
			int rangoComando = Mundo.getRangoComando(comando);
			if (rangoComando == -1) {
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Commande non reconnue: " + comando);
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Comando no reconocido: " + comando);
				}
				return;
			}
			switch (rangoJugador) {
				case 0 :
					// nada
					break;
				default :
					if (rangoJugador >= rangoComando) {
						rangoJugador = 5;
					} else {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1DONT_RANGE_GM");
						return;
					}
					break;
			}
			switch (rangoJugador) {
				
				case 1 :
					GM_lvl_1(comando, infos, mensaje, _cuenta, _perso);
					break;
				case 2 :
					GM_lvl_2(comando, infos, mensaje, _cuenta, _perso);
					break;
				case 3 :
					GM_lvl_3(comando, infos, mensaje, _cuenta, _perso);
					break;
				case 4 :
					GM_lvl_4(comando, infos, mensaje, _cuenta, _perso);
					break;
				case 5 :
					GM_lvl_5(comando, infos, mensaje, _cuenta, _perso);
					break;
				default :
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1DONT_RANGE_GM");
					return;
			}
			if (!_cuenta.getSinco() && !MainServidor.PARAM_DESHABILITAR_SQL) {
				GestorSQL.INSERT_COMANDO_GM(_perso.getNombre(), mensaje);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void GM_lvl_1(final String comando, String[] infos, final String mensaje, final Cuenta _cuenta,
	final Personaje _perso) {
		Personaje objetivo = null;
		int numInt = -1;
		short celdaID = -1, mapaID = -1, numShort = 1;
		StringBuilder strB = new StringBuilder();
		Mapa mapa = _perso.getMapa();
		switch (comando.toUpperCase()) {
			case "CELDA_A_HASH" :
				try {
					celdaID = Short.parseShort(infos[1]);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "CeldaID: " + celdaID + "  HASH: " + Encriptador.celdaIDAHash(
					celdaID));
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incompletos");
				}
				break;
			case "HASH_A_CELDA" :
				try {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "HASH: " + infos[1] + "  CeldaID: " + Encriptador.hashACeldaID(
					infos[1]));
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incompletos");
				}
				break;
			case "INFO_NPC" :
				try {
					NPCModelo npcMod = Mundo.getNPCModelo(Integer.parseInt(infos[1]));
					if (npcMod == null) {
						GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(_perso, "NPC NO EXISTE");
					} else {
						GestorSalida.enviar(_perso, "bp" + npcMod.getSexo() + "," + npcMod.getTallaX() + "," + npcMod.getTallaY()
						+ "," + npcMod.getGfxID() + "," + npcMod.getColor1() + "," + npcMod.getColor2() + "," + npcMod.getColor3()
						+ "," + npcMod.getAccesoriosInt());
					}
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incompletos");
				}
				break;
			case "RATES" :
				_perso.mostrarRates();
				break;
			case "CONGELAR" :
			case "FREEZE" :
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects");
					return;
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				objetivo.setInmovil(true);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " a été freeze.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ha sido inmovilizado el personaje " + objetivo.getNombre());
				}
				break;
			case "DESCONGELAR" :
			case "UN_FREEZE" :
			case "UNFREEZE" :
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects");
					return;
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				objetivo.setInmovil(false);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " peut désormais bouger.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ha sido movilizado el personaje " + objetivo.getNombre());
				}
				break;
			case "CONGELAR_MAPA" :
			case "FREEZE_MAP" :
				if (infos.length > 1) {
					mapa = Mundo.getMapa(Short.parseShort(infos[1]));
				}
				for (final Personaje objetivos : mapa.getArrayPersonajes()) {
					if (objetivos.getCuenta().getAdmin() > 0) {
						continue;
					}
					objetivos.setInmovil(true);
				}
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Tous les joueurs présents sur la MAP " + mapa.getID()
					+ " ont été freeze.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Han sido inmovilizados todos los personajes del mapa " + mapa
					.getID());
				}
				break;
			case "DESCONGELAR_MAPA" :
			case "UN_FREEZE_MAP" :
			case "UNFREEZE_MAP" :
				if (infos.length > 1)
					mapa = Mundo.getMapa(Short.parseShort(infos[1]));
				for (final Personaje objetivos : mapa.getArrayPersonajes()) {
					objetivos.setInmovil(false);
				}
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Les joueurs de cette map ont été défreeze.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Han sido movilizados todos los personajes del mapa " + mapa
					.getID());
				}
				break;
			case "MUTEAR_MAPA" :
			case "MUTE_MAPA" :
			case "MUTE_MAP" :
				if (infos.length > 1)
					mapa = Mundo.getMapa(Short.parseShort(infos[1]));
				mapa.setMuteado(true);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Map mutée.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ha sido muteado el mapa " + mapa.getID());
				}
				break;
			case "DES_MUTEAR_MAPA" :
			case "DESMUTEAR_MAPA" :
			case "DES_MUTE_MAP" :
			case "UN_MUTE_MAP" :
			case "DESMUTE_MAP" :
				try {
					mapa = Mundo.getMapa(Short.parseShort(infos[1]));
				} catch (final Exception e) {}
				mapa.setMuteado(false);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Map unmute.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ha sido desmuteado el mapa " + mapa.getID());
				}
				break;
			case "MUTE_SEGUNDOS" :
			case "MUTE_SECONDS" :
			case "MUTEAR" :
			case "SILENCIAR" :
			case "MUTE" :
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects");
					return;
				}
				String motivo = "";
				try {
					if (infos.length > 2) {
						numInt = Integer.parseInt(infos[2]);
					}
				} catch (Exception e) {}
				try {
					if (infos.length > 3) {
						infos = mensaje.split(" ", 4);
						motivo = infos[3];
					}
				} catch (Exception e) {}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				if (numInt < 0) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La durée de mute est invalide.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La duracion es invalida.");
					}
					return;
				}
				objetivo.getCuenta().mutear(true, numInt);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " a été mute pour " + numInt
					+ " secondes.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ha sido mute " + objetivo.getNombre() + " por " + numInt
					+ " segundos.");
				}
				GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1JUGADOR_MUTEAR;" + objetivo.getNombre() + "~" + numInt / 60 + "~"
				+ motivo);
				break;
			case "UN_MUTE" :
			case "DESMUTE" :
			case "UNMUTE" :
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects");
					return;
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				objetivo.getCuenta().mutear(false, 0);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur peut désormais parler.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El jugador " + objetivo.getNombre() + " ha sido desmuteado");
				}
				break;
			case "CARCEL" :
			case "JAIL" :
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects");
					return;
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				if (objetivo.getPelea() != null || objetivo.getTutorial() != null || objetivo.estaExchange() || objetivo
				.estaInmovil()) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso,
					"Le joueur est en combat ou en craft, impossible de le TP en prison.");
					return;
				}
				objetivo.modificarA(Personaje.RA_PUEDE_USAR_OBJETOS, 0 ^ Personaje.RA_PUEDE_USAR_OBJETOS);
				GestorSalida.ENVIAR_AR_RESTRICCIONES_PERSONAJE(objetivo);
				final short[] celdas = {127, 119, 359, 351};
				objetivo.teleport((short) 666, celdas[Formulas.getRandomInt(0, 3)]);
				objetivo.setCalabozo(true);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " a été envoyé en prison");
				break;
			case "UNJAIL" :
			case "LIBERAR" :
			case "UN_JAIL" :
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects");
					return;
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				objetivo.modificarA(Personaje.RA_PUEDE_USAR_OBJETOS, Personaje.RA_PUEDE_USAR_OBJETOS
				^ Personaje.RA_PUEDE_USAR_OBJETOS);
				GestorSalida.ENVIAR_AR_RESTRICCIONES_PERSONAJE(objetivo);
				objetivo.setCalabozo(false);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " a été libéré.");
				break;
			case "TAMAÑO" :
			case "TALLA" :
			case "SIZE" :
				try {
					if (infos.length > 1)
						numShort = Short.parseShort(infos[1]);
				} catch (final Exception e) {}
				if (numShort < 0) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Taille invalide");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Talla invalida");
					}
					return;
				}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
				}
				objetivo.setTalla(numShort);
				objetivo.refrescarEnMapa();
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La taille du joueur " + objetivo.getNombre() + " a été modifiée");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La talla del personaje " + objetivo.getNombre()
					+ " ha sido modificada");
				}
				break;
			case "INVISIBLE" :
			case "INDETECTABLE" :
				_perso.setIndetectable(true);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Has entrado al estado INDETECTABLE");
				break;
			case "VISIBLE" :
			case "DETECTABLE" :
				_perso.setIndetectable(false);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Has salido al estado INDETECTABLE");
				break;
			case "GFXID" :
			case "FORMA" :
			case "MORPH" :
				try {
					if (infos.length > 1)
						numShort = Short.parseShort(infos[1]);
				} catch (Exception e) {}
				if (numShort < 0) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Gfx ID invalide");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Gfx ID invalida");
					}
					return;
				}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
				}
				objetivo.setGfxID(numShort);
				objetivo.refrescarEnMapa();
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " a changé d'apparence.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre()
					+ " a cambiado de apariencia");
				}
				break;
			case "INFO" :
			case "INFOS" :
				try {
					long enLinea = ServidorServer.getSegundosON() * 1000;
					final int dia = (int) (enLinea / 86400000L);
					enLinea %= 86400000L;
					final int hora = (int) (enLinea / 3600000L);
					enLinea %= 3600000L;
					final int minuto = (int) (enLinea / 60000L);
					enLinea %= 60000L;
					final int segundo = (int) (enLinea / 1000L);
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "====================\n" + MainServidor.NOMBRE_SERVER
						+ " (ELBUSTEMU " + Constantes.VERSION_EMULADOR + ")\n\nUptime: " + dia + "j " + hora + "h " + minuto + "m "
						+ segundo + "s\n" + "Joueurs en ligne: " + ServidorServer.nroJugadoresLinea() + "\n"
						+ "Record de connexions: " + ServidorServer.getRecordJugadores() + "\n" + "====================");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "====================\n" + MainServidor.NOMBRE_SERVER
						+ " (ELBUSTEMU " + Constantes.VERSION_EMULADOR + ")\n\nEnLínea: " + dia + "d " + hora + "h " + minuto + "m "
						+ segundo + "s\n" + "Jugadores en línea: " + ServidorServer.nroJugadoresLinea() + "\n"
						+ "Record de conexión: " + ServidorServer.getRecordJugadores() + "\n" + "====================");
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio un error");
					e.printStackTrace();
					return;
				}
				break;
			case "REFRESCAR_MOBS" :
			case "REFRESH_MOBS" :
				_perso.getMapa().refrescarGrupoMobs();
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mobs respawns.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mobs Refrescados");
				}
				break;
			case "INFO_MAP" :
			case "INFO_MAPA" :
			case "MAPA_INFO" :
			case "MAP_INFOS" :
			case "MAPA_INFOS" :
			case "INFOS_MAPA" :
			case "INFOS_MAP" :
			case "MAP_INFO" :
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "====================");
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "MAP ID: " + mapa.getID() + " [" + mapa.getX() + ", " + mapa.getY()
				+ "]");
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Liste des PNJS sur la map:");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Lista de NPC del mapa:");
				}
				mapa = _perso.getMapa();
				for (final NPC npc : mapa.getNPCs().values()) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "ID: " + npc.getID() + " - Template: " + npc.getModelo().getID()
						+ " - Nom: " + npc.getModelo().getNombre() + " - Case: " + npc.getCeldaID() + " - Question: " + npc
						.getModelo().getPreguntaID(null));
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "ID: " + npc.getID() + " - Modelo: " + npc.getModelo().getID()
						+ " - Nombre: " + npc.getModelo().getNombre() + " - Celda: " + npc.getCeldaID() + " - Pregunta: " + npc
						.getModelo().getPreguntaID(null));
					}
				}
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Liste des groupes de monstres sur la map:");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Lista de los grupos de mounstros:");
				}
				for (final GrupoMob gm : mapa.getGrupoMobsTotales().values()) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "ID: " + gm.getID() + " - Case ID: " + gm.getCeldaID()
						+ " - Monstres: " + gm.getStrGrupoMob() + " - Quantité: " + gm.getCantMobs() + " - Type: " + gm.getTipo()
						+ " - Kamas: " + gm.getKamasHeroico() + " - ItemsID: " + gm.getIDsObjeto());
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "ID: " + gm.getID() + " - CeldaID: " + gm.getCeldaID()
						+ " - StringMob: " + gm.getStrGrupoMob() + " - Cantidad: " + gm.getCantMobs() + " - Tipo: " + gm.getTipo()
						+ " - Kamas: " + gm.getKamasHeroico() + " - ObjetosID: " + gm.getIDsObjeto());
					}
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "====================");
				break;
			case "CANT_SALVANDO" :
			case "SAVE_TIMES" :
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le serveur a été sauvegardé " + Mundo.CANT_SALVANDO + " fois.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El salvado del servidor esta en el " + Mundo.CANT_SALVANDO);
				}
				break;
			case "EN_LINEA" :
			case "ONLINE" :
			case "JUGADORES" :
			case "PLAYERS" :
			case "JOUERS" :
			case "QUIENES" :
			case "WHOIS" :
				int maximo = 50;
				try {
					maximo = Integer.parseInt(infos[1]);
				} catch (Exception e) {}
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "====================\nListe de joueur en ligne:");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "====================\nLista de los jugadores en línea:");
				}
				int players = 0;
				for (ServidorSocket ep : ServidorServer.getClientes()) {
					try {
						objetivo = ep.getPersonaje();
					} catch (final Exception e) {
						continue;
					}
					players++;
					if (players >= maximo) {
						continue;
					}
					if (strB.length() > 0) {
						strB.append("\n");
					}
					if (ep.getCuenta() == null) {
						strB.append("Socket sin loguear cuenta - IP: " + ep.getActualIP());
						continue;
					}
					if (objetivo == null) {
						strB.append("Cuenta sin loguear personaje - Cuenta: " + ep.getCuenta().getNombre() + " IP: " + ep
						.getActualIP());
						continue;
					}
					if (!objetivo.enLinea()) {
						strB.append("Personaje Offline: " + objetivo.getNombre() + "Cuenta: " + ep.getCuenta().getNombre() + " IP: "
						+ ep.getActualIP());
						continue;
					}
					strB.append(objetivo.getNombre() + "\t");
					strB.append("(" + objetivo.getID() + ") " + "\t");
					strB.append("[" + objetivo.getCuenta().getNombre() + "]" + "\t");
					switch (objetivo.getClaseID(true)) {
						case 1 :
							strB.append("Feca" + "\t");
							break;
						case 2 :
							strB.append("Osamoda" + "\t");
							break;
						case 3 :
							strB.append("Anutrof" + "\t");
							break;
						case 4 :
							strB.append("Sram" + "\t");
							break;
						case 5 :
							strB.append("Xelor" + "\t");
							break;
						case 6 :
							strB.append("Zurcarak" + "\t");
							break;
						case 7 :
							strB.append("Aniripsa" + "\t");
							break;
						case 8 :
							strB.append("Yopuka" + "\t");
							break;
						case 9 :
							strB.append("Ocra" + "\t");
							break;
						case 10 :
							strB.append("Sadida" + "\t");
							break;
						case 11 :
							strB.append("Sacrogito" + "\t");
							break;
						case 12 :
							strB.append("Pandawa" + "\t");
							break;
						case 13 :
							strB.append("Tymador" + "\t");
							break;
						case 14 :
							strB.append("Zobal" + "\t");
							break;
						case 15 :
							strB.append("Steamer" + "\t");
							break;
						default :
							strB.append("Desconocido" + "\t");
					}
					strB.append(" " + (objetivo.getSexo() == 0 ? "M" : "F") + "\t");
					strB.append(objetivo.getNivel() + "\t");
					strB.append(objetivo.getMapa().getID() + "\t");
					strB.append("(" + objetivo.getMapa().getX() + "," + objetivo.getMapa().getY() + ")" + "\t");
					strB.append(objetivo.getPelea() == null ? "" : "En combat ");
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, strB.toString());
				if (players > 0) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Et " + players + " joueurs en plus");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Realmente " + players + " personajes");
					}
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "====================");
				break;
			case "CREAR_GREMIO" :
			case "CREATE_GUILD" :
				objetivo = _perso;
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				if (!objetivo.enLinea()) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'est pas connecté.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no esta conectado");
					}
					return;
				}
				if (objetivo.getGremio() != null || objetivo.getMiembroGremio() != null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre()
						+ " appartient déjà à une guilde.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre() + " ya tiene gremio");
					}
					return;
				}
				Accion.realizar_Accion_Estatico(-2, "", objetivo, objetivo, -1, (short) -1);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Panel de guilde ouvert pour : " + objetivo.getNombre());
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se abrió la ventana de gremio al personaje " + objetivo
					.getNombre());
				}
				break;
			case "DEFORMAR" :
			case "DEMORPH" :
				objetivo = _perso;
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
				}
				if (objetivo.getPelea() != null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur est en combat.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje esta en un combate");
					}
					return;
				}
				objetivo.deformar();
				objetivo.refrescarEnMapa();
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre()
					+ " a retrouvé son apparence initiale.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El jugador " + objetivo.getNombre() + " ha sido deformado");
				}
				break;
			case "IR_DONDE" :
			case "JOIN" :
			case "GO_TO_PLAYER" :
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				} else {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Veuillez indiquer le nom du joueur à rejoindre.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Hace falta colocar un nombre de jugador");
					}
					return;
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				if (!objetivo.enLinea()) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'est pas connecté.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no esta conectado");
					}
					return;
				}
				mapaID = objetivo.getMapa().getID();
				celdaID = objetivo.getCelda().getID();
				Personaje teleportado = _perso;
				if (infos.length > 2) {
					teleportado = Mundo.getPersonajePorNombre(infos[2]);
					if (teleportado == null || !teleportado.enLinea()) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur à téléporter n'existe pas ou n'est pas connecté");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje a teleportar no existe o no esta conectado");
						}
						return;
					}
				}
				if (teleportado.getPelea() != null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + teleportado.getNombre() + " est en combat.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + teleportado.getNombre() + " esta en combate");
					}
					return;
				}
				if (teleportado.estaExchange()) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + teleportado.getNombre()
						+ " est entrain de exchange.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + teleportado.getNombre()
						+ " esta haciendo un exchange");
					}
					return;
				}
				if (teleportado.getTutorial() != null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + teleportado.getNombre() + " est en tutoriel.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + teleportado.getNombre()
						+ " esta en un tutorial");
					}
					return;
				}
				if (!teleportado.getHuir()) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + teleportado.getNombre()
						+ " ne peut fuir d'un combat PVP");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + teleportado.getNombre()
						+ " no puede huir de una pelea PVP");
					}
					return;
				}
				teleportado.teleport(mapaID, celdaID);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur a été téléporté");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El jugador " + teleportado.getNombre()
					+ " fue teletransportado donde jugador " + objetivo.getNombre() + " (Map: " + objetivo.getMapa().getID()
					+ ")");
				}
				break;
			case "TRAER" :
			case "JOIN_ME" :
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				} else {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Veuillez indiquer le nom du joueur à rejoindre.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Hace falta colocar un nombre de jugador");
					}
					return;
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				if (!objetivo.enLinea()) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'est pas connecté.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no esta conectado");
					}
					return;
				}
				if (objetivo.getPelea() != null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " est en combat.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre() + " esta en combate");
					}
					return;
				}
				if (objetivo.estaExchange()) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " est en exchange.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre() + " esta en exchange");
					}
					return;
				}
				if (objetivo.getTutorial() != null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " est en tutoriel.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre() + " esta en un tutorial");
					}
					return;
				}
				if (!objetivo.getHuir()) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre()
						+ " ne peut fuir d'un combat PVP");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre()
						+ " no puede huir de una pelea PVP");
					}
					return;
				}
				Personaje traedor = _perso;
				if (infos.length > 2) {
					traedor = Mundo.getPersonajePorNombre(infos[2]);
					if (traedor == null || !traedor.enLinea()) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'est pas connecté.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personajeno no esta conectado");
						}
						return;
					}
				}
				mapaID = traedor.getMapa().getID();
				celdaID = traedor.getCelda().getID();
				objetivo.teleport(mapaID, celdaID);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre()
					+ " ha sido teletransportado hacia el personaje " + traedor.getNombre());
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El jugador " + objetivo.getNombre() + " (Map: " + objetivo
					.getNombre() + ")" + " fue teletransportado donde jugador " + traedor.getNombre());
				}
				break;
			case "AN" :
			case "ALL" :
			case "ANNOUNCE" :
				try {
					infos = mensaje.split(" ", 2);
					if (infos.length < 2) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Veuillez indiquer le message à envoyer!");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Falta argumentos");
						}
						return;
					}
					GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE_TODOS("<b>[" + _perso.getNombre() + "] : </b> " + infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "TELEPORT" :
				try {
					if (infos.length > 1) {
						mapaID = Short.parseShort(infos[1]);
					}
					if (infos.length > 2) {
						celdaID = Short.parseShort(infos[2]);
					}
				} catch (final Exception e) {}
				mapa = Mundo.getMapa(mapaID);
				if (mapa == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "MAPID INVALIDE!");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mapa a teleportar no existe");
					}
					return;
				}
				if (celdaID <= -1) {
					celdaID = mapa.getRandomCeldaIDLibre();
				} else if (mapa.getCelda(celdaID) == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "CELLID INVALIDE!");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "CeldaID inválida");
					}
					return;
				}
				objetivo = _perso;
				if (infos.length > 3) {
					objetivo = Mundo.getPersonajePorNombre(infos[3]);
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					if (!objetivo.enLinea()) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'est pas connecté.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no esta conectado");
						}
						return;
					}
				}
				if (objetivo.getPelea() != null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur à téléporter est en combat.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje a teleportar esta en combate");
					}
					return;
				}
				if (objetivo.estaExchange()) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur à téléporter est entrain de crafter.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje a teleportar esta haciendo un trabajo");
					}
					return;
				}
				if (objetivo.getTutorial() != null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur à téléporter est en tutoriel.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje a teleportar esta en un tutorial");
					}
					return;
				}
				if (!objetivo.getHuir()) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur à téléporter ne peut fuir d'un combat PVP");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje a teleportar no puede huir de una pelea PVP");
					}
					return;
				}
				objetivo.teleport(mapaID, celdaID);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre()
					+ " ha sido teletransportado a mapaID: " + mapaID + ", celdaID: " + celdaID);
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El jugador " + objetivo.getNombre()
					+ " ha sido teletransportado a mapaID: " + mapaID + ", celdaID: " + celdaID);
				}
				break;
			case "TELEPORT_SIN_TODOS" :
				try {
					if (infos.length > 1) {
						mapaID = Short.parseShort(infos[1]);
					}
					if (infos.length > 2) {
						celdaID = Short.parseShort(infos[2]);
					}
				} catch (final Exception e) {}
				mapa = Mundo.getMapa(mapaID);
				if (mapa == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "MAPID INVALIDE!");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mapa a teleportar no existe");
					}
					return;
				}
				if (celdaID <= -1) {
					celdaID = mapa.getRandomCeldaIDLibre();
				} else if (mapa.getCelda(celdaID) == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "CELLID INVALIDE!");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "CeldaID inválida");
					}
					return;
				}
				objetivo = _perso;
				if (infos.length > 3) {
					objetivo = Mundo.getPersonajePorNombre(infos[3]);
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					if (!objetivo.enLinea()) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'est pas connecté.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no esta conectado");
						}
						return;
					}
				}
				objetivo.teleportSinTodos(mapaID, celdaID);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre()
					+ " ha sido teletransportado a mapaID: " + mapaID + ", celdaID: " + celdaID);
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El jugador " + objetivo.getNombre()
					+ " ha sido teletransportado a mapaID: " + mapaID + ", celdaID: " + celdaID);
				}
				break;
			case "IR_MAPA" :
			case "GO_MAP" :
				int mapaX = 0;
				int mapaY = 0;
				celdaID = 0;
				int contID = 0;
				try {
					mapaX = Integer.parseInt(infos[1]);
					mapaY = Integer.parseInt(infos[2]);
					celdaID = Short.parseShort(infos[3]);
					contID = Integer.parseInt(infos[4]);
				} catch (final Exception e10) {}
				mapa = Mundo.mapaPorCoordXYContinente(mapaX, mapaY, contID);
				if (mapa == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Position ou continent invalide!");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Posicion o continente inválido");
					}
					return;
				}
				if (mapa.getCelda(celdaID) == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "CellID invalide!");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "CeldaID inválido");
					}
					return;
				}
				objetivo = _perso;
				if (infos.length > 5) {
					objetivo = Mundo.getPersonajePorNombre(infos[5]);
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					if (!objetivo.enLinea()) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'est pas connecté.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no esta conectado");
						}
						return;
					}
					if (objetivo.getPelea() != null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur est en combat!");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje esta en combate");
						}
						return;
					}
				}
				objetivo.teleport(mapa.getID(), celdaID);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " a été téléporté!");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El jugador " + objetivo.getNombre() + " ha sido teletransportado");
				}
				break;
			default :
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Commande non reconnue: " + comando);
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Comando no reconocido: " + comando);
				}
				return;
		}
		// if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
		// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Commande GM 1!");
		// } else {
		// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Comando de nivel 1");
		// }
	}
	
	public static void GM_lvl_2(final String comando, String[] infos, final String mensaje, final Cuenta _cuenta,
	final Personaje _perso) {
		int numInt = -1;
		short celdaID, x, y;
		Personaje objetivo = null;
		StringBuilder strB = new StringBuilder();
		Mapa mapa = _perso.getMapa();
		String motivo = "";
		switch (comando.toUpperCase()) {
			case "EQUIPO_GANADOR" :
			case "WINNER" :
			case "GANADOR_EQUIPO" :
			case "GANAR_PELEA" :
			case "GANADOR_PELEA" :
			case "WINNER_FIGHT" :
			case "TEAM_WINNER" :
				try {
					try {
						numInt = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
						return;
					}
					if (numInt != 2 && numInt != 1) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ganador invalido");
						return;
					}
					objetivo = _perso;
					if (infos.length > 2) {
						objetivo = Mundo.getPersonajePorNombre(infos[2]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					Pelea pelea = objetivo.getPelea();
					if (pelea == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, objetivo.getNombre() + " no estas en pelea");
						return;
					}
					pelea.acaboPelea((byte) (numInt == 1 ? 2 : 1));
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El equipo " + infos[1] + " ha salido victorioso, en la pelea ID "
					+ pelea.getID() + " del mapa " + pelea.getMapaCopia().getID());
				} catch (Exception e) {}
				break;
			case "CANCEL_FIGHT" :
			case "CANCELAR_PELEA" :
			case "ANULAR_PELEA" :
			case "ANULATE_FIGHT" :
			case "FIGHT_CANCEL" :
				try {
					objetivo = _perso;
					if (infos.length > 1) {
						objetivo = Mundo.getPersonajePorNombre(infos[1]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					Pelea pelea = objetivo.getPelea();
					if (pelea == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, objetivo.getNombre() + " no estas en pelea");
						return;
					}
					pelea.cancelarPelea();
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La pelea ID " + pelea.getID() + " del mapa " + pelea.getMapaCopia()
					.getID() + " ha sido cancelada");
				} catch (Exception e) {}
				break;
			case "SHOW_BAN_IPS" :
			case "MOSTRAR_BAN_IPS" :
			case "SHOW_LIST_BAN_IPS" :
			case "MOSTRAR_BAN_IP" :
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Las IPs Baneadas son las siguientes:\n" + GestorSQL.LISTA_BAN_IP());
				break;
			case "BANEAR" :
			case "BAN" :
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				} else {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				try {
					numInt = Integer.parseInt(infos[2]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Debes ingresar un tiempo (minutos)");
					return;
				}
				try {
					if (infos.length > 3) {
						infos = mensaje.split(" ", 4);
						motivo = infos[3];
					}
				} catch (final Exception e) {}
				if (numInt == 0) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Durée du ban incorrecte!");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Tiempo de baneo incorrecto");
					}
					return;
				}
				objetivo.getCuenta().setBaneado(true, numInt);
				if (objetivo.getServidorSocket() != null) {
					objetivo.getServidorSocket().cerrarSocket(true, " command BANEAR");
				}
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " a été banni par " + numInt
					+ " minutes.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ha sido baneado " + objetivo.getNombre() + " por " + numInt
					+ " minutos.");
				}
				GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1JUGADOR_BANEAR;" + objetivo.getNombre() + "~" + motivo);
				break;
			case "DES_BAN" :
			case "UN_BAN" :
			case "DESBANEAR" :
			case "UNBAN" :
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				} else {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				objetivo.getCuenta().setBaneado(false, 0);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " a été débanni.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ha sido desbaneado " + objetivo.getNombre());
				}
				break;
			case "BANEAR_IP_PJ" :
			case "BAN_IP_PLAYER" :
			case "BAN_IP_PERSO" :
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				} else {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				final String ipBaneada = objetivo.getCuenta().getActualIP();
				if (!GestorSQL.ES_IP_BANEADA(ipBaneada)) {
					if (GestorSQL.INSERT_BAN_IP(ipBaneada)) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "L'IP " + ipBaneada + " est bannie.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La IP " + ipBaneada + " esta baneada.");
						}
					}
					if (objetivo.enLinea()) {
						objetivo.getServidorSocket().cerrarSocket(true, "command Banear IP");
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur a été kick.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El jugador fue retirado.");
						}
					}
				} else {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "L'IP n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La IP no existe");
					}
				}
				break;
			case "BANEAR_IP" :
			case "BAN_IP" :
			case "BANEAR_IP_NUMERO" :
			case "BAN_IP_NUMBER" :
				if (infos.length <= 1) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argument incorrect.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				if (!GestorSQL.ES_IP_BANEADA(infos[1])) {
					if (GestorSQL.INSERT_BAN_IP(infos[1])) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La IP " + infos[1] + " esta baneada.");
					}
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La IP no existe");
				}
				break;
			case "DESBANEAR_IP_NUMERO" :
			case "UNBAN_IP_NUMERO" :
				if (infos.length <= 1) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argument incorrect.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				GestorSQL.DELETE_BAN_IP(infos[1]);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "L'ip " + infos[1] + " a été débannie.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se borro la ip " + infos[1] + " de la lista de ip baneadas");
				}
				break;
			case "EXPULSAR" :
			case "KICK" :
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				} else {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				try {
					if (infos.length > 2) {
						infos = mensaje.split(" ", 3);
						motivo = infos[2];
					}
				} catch (final Exception e) {}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				if (!objetivo.enLinea()) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'est pas connecté.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no esta conectado");
					}
					return;
				} else {
					try {
						objetivo.getServidorSocket().cerrarSocket(true, "command EXPULSAR");
					} catch (final Exception e) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Impossible de kicker " + objetivo.getNombre());
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No se ha podido expulsar a " + objetivo.getNombre());
						}
						return;
					}
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " a été kick.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ha sido expulsado " + objetivo.getNombre());
					}
					GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1JUGADOR_EXPULSAR;" + objetivo.getNombre() + "~" + motivo);
				}
				break;
			case "BOLETOS_COMPRADOS" :
			case "TICKETS_ACHETES" :
			case "GET_BOUGHT_TICKETS" :
				numInt = 0;
				for (int z = 1; z <= Mundo.LOTERIA_BOLETOS.length; z++) {
					if (Mundo.LOTERIA_BOLETOS[z - 1] != 0) {
						numInt++;
					}
				}
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Actuellement, le nombre de tickets achetés est de " + numInt + ".");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Actualmente hay " + numInt + " boletos comprados.");
				}
				break;
			case "LISTA_BOLETOS_COMPRADOS" :
				for (int z = 1; z <= Mundo.LOTERIA_BOLETOS.length; z++) {
					if (Mundo.LOTERIA_BOLETOS[z - 1] != 0) {
						if (strB.length() > 0) {
							strB.append(",");
						}
						strB.append(Mundo.LOTERIA_BOLETOS[z - 1]);
					}
				}
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Liste de tickets achetes " + strB.toString() + ".");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Lista de boletos comprados: " + strB.toString());
				}
				break;
			case "SET_CELDAS_PELEA" :
				mapa.setStrCeldasPelea(infos[1]);
				if (!GestorSQL.UPDATE_MAPA_POS_PELEA(mapa.getID(), mapa.getConvertCeldasPelea())) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Une erreur est survenue lors de la sauvegarde en BDD!");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrió un error al guardar la actualización en la BD.");
					}
					return;
				} else {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le string cells fight change to " + infos[1]);
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El str de celdas pelea cambio a " + infos[1]);
					}
				}
				break;
			case "SET_COLOUR_AGGRESSOR" :
			case "SET_COLOR_ATK" :
			case "SET_COLOR_AGRESOR" :
			case "SET_COLOR_ATACANTE" :
				mapa.setColorCeldasAtacante(infos[1]);
				if (!GestorSQL.UPDATE_MAPA_POS_PELEA(mapa.getID(), mapa.getConvertCeldasPelea())) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Une erreur est survenue lors de la sauvegarde en BDD!");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrió un error al guardar la actualización en la BD.");
					}
					return;
				} else {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le colour de cells aggressor c'est " + infos[1]);
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El color de celdas del agresor es " + infos[1]);
					}
				}
				break;
			case "BORRAR_POSICIONES" :
			case "ELIMINAR_POSICIONES" :
			case "DEL_POSICIONES_PELEA" :
			case "BORRAR_TODAS_POS_PELEA" :
			case "DEL_ALL_POS" :
				mapa.decodificarPosPelea("");
				if (!GestorSQL.UPDATE_MAPA_POS_PELEA(mapa.getID(), mapa.getConvertCeldasPelea())) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Une erreur est survenue lors de la sauvegarde en BDD!");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrió un error al guardar la actualización en la BD.");
					}
				} else {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Les positions de combat ont été supprimées.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Las posiciones de pelea han sido borradas.");
					}
				}
				break;
			case "DEL_FIGHT_POS" :
			case "DEL_POS_FIGHT" :
			case "BORRAR_POS_PELEA" :
			case "DEL_FIGHT_POS_BY_CELL" :
				celdaID = -1;
				try {
					celdaID = Short.parseShort(infos[1]);
				} catch (final Exception e) {}
				if (mapa.getCelda(celdaID) == null) {
					celdaID = _perso.getCelda().getID();
				}
				// if (mapa.getCercado() != null) {
				// mapa.getCercado().getCeldasObj().remove((Object) celdaID);
				// }
				GestorSalida.enviarEnCola(_perso, "GDZ|-" + celdaID + ";0;4|-" + celdaID + ";0;11|-" + celdaID + ";0;5", false);
				mapa.borrarCeldasPelea(celdaID);
				if (!GestorSQL.UPDATE_MAPA_POS_PELEA(mapa.getID(), mapa.getConvertCeldasPelea())) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Une erreur est survenue lors de la sauvegarde en BDD!");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrió un error al guardar la actualización en la BD.");
					}
				}
				break;
			case "ADD_CELL_FIGHT" :
			case "AGREGAR_CELDA_PELEA" :
			case "ADD_CELDA_PELEA" :
			case "ADD_POS_FIGHT" :
			case "AGREGAR_POS_PELEA" :
			case "ADD_FIGHT_POS" :
				int equipo = -1;
				celdaID = -1;
				try {
					equipo = Integer.parseInt(infos[1]);
					celdaID = Short.parseShort(infos[2]);
				} catch (final Exception e) {}
				if (equipo != 2 && equipo != 1) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Equipe incorrecte, use colour 2(blue) o 1(rouge)");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Equipo incorrecto, usa 2(azul) o 1(rojo)");
					}
					return;
				}
				if (mapa.getCelda(celdaID) == null || !mapa.getCelda(celdaID).esCaminable(true)) {
					celdaID = _perso.getCelda().getID();
				}
				GestorSalida.enviarEnCola(_perso, "GDZ|-" + celdaID + ";0;4|-" + celdaID + ";0;11", false);
				GestorSalida.enviarEnCola(_perso, "GDZ|+" + celdaID + ";0;" + (equipo == 1 ? 4 : 11), false);
				mapa.addCeldaPelea(equipo, celdaID);
				if (!GestorSQL.UPDATE_MAPA_POS_PELEA(mapa.getID(), mapa.getConvertCeldasPelea())) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Une erreur est survenue lors de la sauvegarde en BDD.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrió un error al guardar la actualización en la BD.");
					}
					return;
				}
				break;
			case "OCULTAR_POSICIONES" :
			case "HIDE_POSITIONS" :
			case "ESCONDER_POSICIONES" :
				mapa.panelPosiciones(_perso, false);
				break;
			case "LISTA_POS_PELEA" :
			case "MOSTRAR_POSICIONES" :
			case "MOSTRAR_POSICIONES_PELEA" :
			case "MOSTRAR_POS_PELEA" :
			case "SHOW_POSITIONS" :
			case "SHOW_FIGHT_POS" :
				mapa.panelPosiciones(_perso, true);
				break;
			case "MAPAS" :
			case "MAPS" :
			case "MAPAS_COORDENADAS" :
			case "GET_MAPS_BY_COORDS" :
				x = -1;
				y = -1;
				try {
					x = Short.parseShort(infos[1]);
					y = Short.parseShort(infos[2]);
				} catch (final Exception e) {
					x = mapa.getX();
					y = mapa.getY();
				}
				strB = new StringBuilder(Mundo.mapaPorCoordenadas(x, y, mapa.getSubArea().getArea().getSuperArea().getID()));
				if (strB.toString().isEmpty()) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No hay ID mapa para esas coordenadas");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Los ID mapas para las coordenas X: " + x + " Y: " + y + " son "
					+ strB.toString());
				}
				break;
			case "MAP_UP" :
			case "MAPA_ARRIBA" :
				x = mapa.getX();
				y = (short) (mapa.getY() - 1);
				strB = new StringBuilder(Mundo.mapaPorCoordenadas(x, y, mapa.getSubArea().getArea().getSuperArea().getID()));
				if (strB.toString().isEmpty()) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No hay ID mapa para esas coordenadas");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Los ID mapas para las coordenas X: " + x + " Y: " + y + " son "
					+ strB.toString());
				}
				break;
			case "MAP_DOWN" :
			case "MAPA_ABAJO" :
				x = mapa.getX();
				y = (short) (mapa.getY() + 1);
				strB = new StringBuilder(Mundo.mapaPorCoordenadas(x, y, mapa.getSubArea().getArea().getSuperArea().getID()));
				if (strB.toString().isEmpty()) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No hay ID mapa para esas coordenadas");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Los ID mapas para las coordenas X: " + x + " Y: " + y + " son "
					+ strB.toString());
				}
				break;
			case "MAP_LEFT" :
			case "MAPA_IZQUIERDA" :
				x = (short) (mapa.getX() - 1);
				y = mapa.getY();
				strB = new StringBuilder(Mundo.mapaPorCoordenadas(x, y, mapa.getSubArea().getArea().getSuperArea().getID()));
				if (strB.toString().isEmpty()) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No hay ID mapa para esas coordenadas");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Los ID mapas para las coordenas X: " + x + " Y: " + y + " son "
					+ strB.toString());
				}
				break;
			case "MAP_RIGHT" :
			case "MAPA_DERECHA" :
				x = (short) (mapa.getX() + 1);
				y = mapa.getY();
				strB = new StringBuilder(Mundo.mapaPorCoordenadas(x, y, mapa.getSubArea().getArea().getSuperArea().getID()));
				if (strB.toString().isEmpty()) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No hay ID mapa para esas coordenadas");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Los ID mapas para las coordenas X: " + x + " Y: " + y + " son "
					+ strB.toString());
				}
				break;
			case "CAMBIAR_ALINEACION" :
			case "ALINEACION" :
			case "ALIGN" :
			case "SET_ALIGN" :
				byte alineacion = -1;
				try {
					alineacion = Byte.parseByte(infos[1]);
				} catch (final Exception e5) {}
				if (alineacion < -1 || alineacion > 3) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Alignement incorrect.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
				}
				objetivo.cambiarAlineacion(alineacion, true);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "L'alignement du joueur " + objetivo.getNombre()
					+ " a été modifié.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La alineacion del personaje " + objetivo.getNombre()
					+ " ha sido modificada");
				}
				break;
			case "APRENDER_OFICIO" :
			case "LEARN_JOB" :
				try {
					numInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Veuillez indiquer l'id du métier.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos Incorrectos");
					}
					return;
				}
				if (Mundo.getOficio(numInt) == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "L'ID du métier est incorrect.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "ID Oficio no existe");
					}
					return;
				}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
				}
				if (objetivo.aprenderOficio(Mundo.getOficio(numInt), 0) != -1) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur a appris ce métier " + numInt);
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre() + " ha aprendido el oficio "
						+ numInt);
					}
				} else {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur ne peut pas apprendre ce métier.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre()
						+ " no puede aprender ese oficio");
					}
				}
				break;
			case "PDV" :
			case "PDVPER" :
				int porcPDV = 0;
				try {
					porcPDV = Integer.parseInt(infos[1]);
					if (porcPDV < 0) {
						porcPDV = 0;
					}
					if (porcPDV > 100) {
						porcPDV = 100;
					}
					objetivo = _perso;
					if (infos.length > 2) {
						final String nombre = infos[2];
						objetivo = Mundo.getPersonajePorNombre(nombre);
						if (objetivo == null || !objetivo.enLinea()) {
							objetivo = _perso;
						}
					}
					objetivo.actualizarPDV(porcPDV);
					if (objetivo.enLinea()) {
						GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(objetivo);
					}
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le pourcentage de vie du joueur " + objetivo.getNombre()
						+ " a été modifié en " + porcPDV);
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ha sido modificado el porcentaje de vida " + objetivo.getNombre()
						+ " a " + porcPDV);
					}
					GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(objetivo);
				} catch (final Exception e) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
				}
				break;
			default :
				GM_lvl_1(comando, infos, mensaje, _cuenta, _perso);
				return;
		}
		// if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
		// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Commande de GM 2!");
		// } else {
		// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Comando de nivel 2");
		// }
	}
	
	public static void GM_lvl_3(final String comando, String[] infos, final String mensaje, final Cuenta _cuenta,
	final Personaje _perso) {
		boolean sql = false;
		// byte idByte = 0, numByte = 0;
		// short numShort = 0;
		// int numInt = 0, tipo = 0, accionID = -1, id2 = -1, restriccion = -1;
		// int idMob = -1, idObjMod = -1, prospecc = 100, max = 1;
		// float porcentaje = 0, numFloat = 0;
		// final StringBuilder strB = new StringBuilder();
		// String args = "", condicion = "", str = "";
		Mapa mapa = _perso.getMapa();
		// short celdaID = -1, mapaID = mapa.getID();
		// MobModelo mobModelo;
		Personaje objetivo = null;
		// ObjetoModelo objModelo;
		// Objeto obj;
		// NPC npc;
		// PreguntaNPC pregunta;
		// RespuestaNPC respuesta;
		switch (comando.toUpperCase()) {
			case "KICK_MERCHANT" :
			case "KICK_MERCHAND" :
			case "VOTAR_MERCANTE" :
			case "EXPULSAR_MERCANTE" :
				try {
					if (infos.length > 1) {
						objetivo = Mundo.getPersonajePorNombre(infos[1]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					if (!objetivo.esMercante()) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre()
						+ " no esta en modo mercante");
						return;
					}
					objetivo.getMapa().removerMercante(objetivo.getID());
					objetivo.setMercante(false);
					GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(objetivo.getMapa(), objetivo.getID());
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre()
					+ " ha sido expulsado del modo mercante");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "EXCEPTION COMANDO");
				}
				break;
			case "IPS_AFKS" :
			case "IPS_CLIENTES_AFKS" :
			case "IPS_BUGS" :
			case "IPS_ATACANTES" :
			case "IPS_ATTACK" :
				try {
					int segundos = 0;
					try {
						segundos = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Valor incorrecto (segundos)");
						return;
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Las ips de las connexiones BUGS son: " + ServidorServer
					.listaClientesBug(segundos));
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "EXCEPTION COMANDO");
				}
				break;
			case "EXPULSAR_AFKS" :
			case "EXPULSAR_CLIENTES_BUG" :
			case "KICK_CLIENTS_BUG" :
			case "VOTAR_CLIENTES_BUG" :
			case "EXPULSAR_INACTIVOS" :
			case "CLEAN_SERVER" :
			case "LIMPIAR_SERVIDOR" :
			case "LIMPIAR_SOCKETS" :
				try {
					int segundos = 0;
					try {
						segundos = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Valor incorrecto (segundos)");
						return;
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se expulsó " + ServidorServer.borrarClientesBug(segundos)
					+ " clientes bugeados");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "EXCEPTION COMANDO");
				}
				break;
			// case "ACTUALIZAR_NPC" :
			// case "ACCESSORIES_NPC" :
			// case "STUFF_NPC" :
			// try {
			// numInt = Integer.parseInt(infos[1]);
			// } catch (final Exception e) {}
			// if (Mundo.getNPCModelo(numInt) == null) {
			// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "NPC modelo invalido");
			// return;
			// }
			// objetivo = _perso;
			// if (infos.length > 2) {
			// objetivo = Mundo.getPersonajePorNombre(infos[2]);
			// }
			// if (objetivo == null) {
			// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
			// return;
			// }
			// NPCModelo npcMod = Mundo.getNPCModelo(numInt);
			// try {
			// npcMod.modificarNPC(objetivo.getGfxID(false), objetivo.getSexo(), objetivo.getColor1(),
			// objetivo.getColor2(),
			// objetivo.getColor3(), objetivo.getStringAccesorios());
			// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se actualizo el NPC modelo " + numInt +
			// ", Nombre: "
			// + npcMod.getNombre() + ", Gfx: " + npcMod.getGfxID() + ", Accesorios: " +
			// npcMod.getAccesoriosInt());
			// } catch (Exception e) {}
			// break;
			case "PANEL_ADMIN" :
				try {
					GestorSalida.enviarEnCola(_perso, "ÑP" + mapa.getCapabilitiesCompilado() + "|" + mapa.getMaxGrupoDeMobs()
					+ "|" + mapa.getMaxMobsPorGrupo() + "|" + mapa.getMaxMercantes(), false);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "A" :
				infos = mensaje.split(" ", 2);
				try {
					GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE_TODOS(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Falta argumentos");
				}
				break;
			case "GET_LISTA_PACKETS_COLA" :
				try {
					String packetsCola = _perso.getPacketsCola().replaceAll(((char) 0x00) + "", "\n");
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Lista packets en cola:\n" + packetsCola);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "HACER_ACCION" :
			case "DO_ACTION" :
			case "REALIZAR_ACCION" :
				try {
					objetivo = _perso;
					if (infos.length > 1) {
						objetivo = Mundo.getPersonajePorNombre(infos[1]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					if (!objetivo.enLinea()) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'est pas connecté.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no esta conectado");
						}
						return;
					}
					int tipoAccion = 0;
					String args = "";
					try {
						tipoAccion = Integer.parseInt(infos[2]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "ID Accion incorrecta");
						return;
					}
					if (infos.length > 3) {
						args = infos[3];
					}
					Accion.realizar_Accion_Estatico(tipoAccion, args, objetivo, null, -1, (short) -1);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre() + " realizó la acción "
					+ tipoAccion + " con los argumentos " + args);
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
				}
				break;
			case "FIJAR_STATS_MOB" :
			case "FIJAR_STATS" :
			case "SET_STATS_MOB" :
			case "FIJAR_DAÑOS" :
			case "FIJAR_DAÑO" :
			case "MODIFICAR_STATS_MOB" :
				try {
					int id = 0;
					byte grado = 0;
					String stats = "";
					try {
						id = Integer.parseInt(infos[1]);
						grado = Byte.parseByte(infos[2]);
						stats = infos[3];
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MobModelo mobModelo = Mundo.getMobModelo(id);
					if (mobModelo == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "MobModelo " + infos[1] + " no existe");
						return;
					}
					MobGradoModelo mGrado = mobModelo.getGradoPorGrado(grado);
					if (mGrado == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "MobGradorModelo " + infos[1] + "-" + infos[2] + " no existe");
						return;
					}
					if (mobModelo.modificarStats(grado, stats)) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mob " + mobModelo.getNombre() + " (" + mobModelo.getID()
						+ ") g: " + grado + " lvl: " + mobModelo.getGradoPorGrado(grado).getNivel() + " ha sido modificado stats a "
						+ stats);
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "STATS_DEFECTO_MOB" :
				try {
					int id = 0;
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MobModelo mobModelo = Mundo.getMobModelo(id);
					if (mobModelo == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					GestorSalida.ENVIAR_ÑJ_STATS_DEFECTO_MOB(_perso, mobModelo.strStatsTodosMobs());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "TEST_DAÑO" :
				try {
					int id = 0;
					byte grado = 1;
					String stats = "";
					try {
						id = Integer.parseInt(infos[1]);
						grado = Byte.parseByte(infos[2]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					try {
						stats = infos[3];
					} catch (final Exception e) {}
					MobModelo mobModelo = Mundo.getMobModelo(id);
					if (mobModelo == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					GestorSalida.ENVIAR_ÑK_TEST_DAÑO_MOB(_perso, mobModelo.calculoDaño(grado, stats));
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MAX_PELEAS_MAPA" :
			case "MAP_MAX_FIGHTS" :
			case "MAX_FIGHTS_MAP" :
				try {
					byte maxPeleas = 0;
					short mapaID = 0;
					try {
						maxPeleas = Byte.parseByte(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					try {
						mapaID = Short.parseShort(infos[2]);
						if (Mundo.getMapa(mapaID) != null) {
							mapa = Mundo.getMapa(mapaID);
						}
					} catch (final Exception e) {}
					mapa.setMaxPeleas(maxPeleas);
					GestorSQL.UPDATE_MAPA_MAX_PELEAS(mapa.getID(), maxPeleas);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mapa " + mapa.getID() + " cambio el valor de maximo de pleas a "
					+ maxPeleas);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MODIFICAR_NPC" :
				try {
					int id = 0;
					byte sexo = 1;
					short escalaX = 100;
					short escalaY = 100;
					short gfxID = 9999;
					int color1 = -1;
					int color2 = -1;
					int color3 = -1;
					int arma = 0;
					int sombrero = 0;
					int capa = 0;
					int mascota = 0;
					int escudo = 0;
					try {
						id = Short.parseShort(infos[1]);
					} catch (final Exception e) {}
					try {
						sexo = Byte.parseByte(infos[2]);
					} catch (final Exception e) {}
					try {
						escalaX = Short.parseShort(infos[3]);
					} catch (final Exception e) {}
					try {
						escalaY = Short.parseShort(infos[4]);
					} catch (final Exception e) {}
					try {
						gfxID = Short.parseShort(infos[5]);
					} catch (final Exception e) {}
					try {
						color1 = Integer.parseInt(infos[6]);
					} catch (final Exception e) {}
					try {
						color2 = Integer.parseInt(infos[7]);
					} catch (final Exception e) {}
					try {
						color3 = Integer.parseInt(infos[8]);
					} catch (final Exception e) {}
					try {
						arma = Integer.parseInt(infos[9]);
					} catch (final Exception e) {}
					try {
						sombrero = Integer.parseInt(infos[10]);
					} catch (final Exception e) {}
					try {
						capa = Integer.parseInt(infos[11]);
					} catch (final Exception e) {}
					try {
						mascota = Integer.parseInt(infos[12]);
					} catch (final Exception e) {}
					try {
						escudo = Integer.parseInt(infos[13]);
					} catch (final Exception e) {}
					NPCModelo npcMod = Mundo.getNPCModelo(id);
					if (npcMod == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "NPC " + id + " no existe");
					}
					npcMod.modificarNPC(sexo, escalaX, escalaY, gfxID, color1, color2, color3);
					npcMod.setAccesorios(arma, sombrero, capa, mascota, escudo);
					GestorSQL.UPDATE_NPC_MODELO(npcMod, arma, sombrero, capa, mascota, escudo);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modifico el NPC " + id + " con las sig. caracteristicas, GFX: "
					+ gfxID + " SEX: " + sexo + " ESCALA X: " + escalaX + " ESCALA Y: " + escalaY + " COLOR1: " + color1
					+ " COLOR2: " + color2 + " COLOR3: " + color3 + " ACCES: " + npcMod.getAccesoriosInt());
					GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(_perso, "NPC MODIFICADO!! :D");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "CELDA_COORD" :
			case "CELL_COORD" :
			case "EJES_CELDA" :
			case "POS_CELDA" :
			case "COORD_CELDA" :
			case "CELDA_POS" :
				try {
					short celdaID = 0;
					try {
						celdaID = Short.parseShort(infos[1]);
					} catch (final Exception e) {
						celdaID = _perso.getCelda().getID();
					}
					try {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Las coordenadas de la celda " + celdaID + " es, X: " + mapa
						.getCelda(celdaID).getCoordX() + ", Y: " + mapa.getCelda(celdaID).getCoordY());
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Celda No existe");
						return;
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "TEST_CELDAS" :
				try {
					short celdaID = 0;
					try {
						celdaID = Short.parseShort(infos[1]);
					} catch (final Exception e) {
						return;
					}
					try {
						String s = "";
						for (short c : Camino.celdasPorDistancia(_perso.getCelda(), _perso.getMapa(), celdaID)) {
							s += c + ",";
							GestorSalida.ENVIAR_GDO_OBJETO_TIRAR_SUELO(_perso, '+', c, 311, false, "");
						}
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Las celdas a mostrar son " + s);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Celda No existe");
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			// case "ADD_CAPTCHA" :
			// try {
			// infos = mensaje.split(" ", 2);
			// Mundo.Captchas.add(infos[1]);
			// GestorSQL.INSERT_CAPTCHA(infos[1].split("\\|")[0], infos[1].split(Pattern.quote("|"))[1]);
			// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se agrego la captcha: " +
			// infos[1].split(Pattern.quote("|"))[0]
			// + " y respuesta: "
			// + infos[1].split(Pattern.quote("|"))[1]);
			// } catch (final Exception e) {
			// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
			// }
			// break;
			case "CONSULTAR_OGRINAS" :
			case "GET_OGRINAS" :
			case "CONSULTA_OGRINAS" :
			case "CONSULTA_PUNTOS" :
			case "GET_POINTS" :
				try {
					objetivo = _perso;
					if (infos.length > 1) {
						objetivo = Mundo.getPersonajePorNombre(infos[1]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				try {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " possède " + GestorSQL
						.GET_OGRINAS_CUENTA(objetivo.getCuentaID()) + " ogrines/points");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre() + " posee " + GestorSQL
						.GET_OGRINAS_CUENTA(objetivo.getCuentaID()));
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "SEGUNDOS_TURNO_PELEA" :
			case "TIEMPO_TURNO_PELEA" :
			case "RATE_TIEMPO_PELEA" :
				try {
					int segundos = 0;
					try {
						segundos = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.SEGUNDOS_TURNO_PELEA = segundos;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el SEGUNDOS_TURNO_PELEA a "
					+ MainServidor.SEGUNDOS_TURNO_PELEA + " segundos");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MINUTOS_ALIMENTACION_MASCOTA" :
			case "TIEMPO_ALIMENTACION" :
			case "RATE_TIEMPO_ALIMENTACION" :
				try {
					int minutos = 0;
					try {
						minutos = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.MINUTOS_ALIMENTACION_MASCOTA = minutos;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el MINUTOS_ALIMENTACION_MASCOTA a "
					+ MainServidor.MINUTOS_ALIMENTACION_MASCOTA + " minutos");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "SEGUNDOS_MOVERSE_MONTURAS" :
			case "TIEMPO_MOVERSE_PAVOS" :
			case "RATE_TIEMPO_MOV_PAVO" :
				try {
					int segundos = 0;
					try {
						segundos = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.SEGUNDOS_MOVER_MONTURAS = segundos;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso,
					"El Tiempo para que los dragopavos se muevan automáticamente ha sido modificado a "
					+ MainServidor.SEGUNDOS_MOVER_MONTURAS + " segundos");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MINUTOS_PARIR_MONTURA" :
			case "TIEMPO_PARIR" :
			case "RATE_TIEMPO_PARIR" :
				try {
					int minutos = 0;
					try {
						minutos = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.MINUTOS_GESTACION_MONTURA = minutos;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el MINUTOS_PARIR_MONTURA a "
					+ MainServidor.MINUTOS_GESTACION_MONTURA + " minutos");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "RATE_FM" :
			case "DIFICULTAD_FM" :
				try {
					byte rate = 0;
					try {
						rate = Byte.parseByte(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.RATE_FM = rate;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el RATE_FM a " + MainServidor.RATE_FM);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "RATE_PODS" :
				try {
					byte rate = 0;
					try {
						rate = Byte.parseByte(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.RATE_PODS = rate;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el RATE_PODS a " + MainServidor.RATE_PODS);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "RATE_CAPTURA_PAVOS" :
				try {
					byte rate = 0;
					try {
						rate = Byte.parseByte(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.RATE_CAPTURA_MONTURA = rate;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el RATE_CAPTURA_MONTURA a "
					+ MainServidor.RATE_CAPTURA_MONTURA);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "RATE_KAMAS" :
				try {
					byte rate = 0;
					try {
						rate = Byte.parseByte(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.RATE_KAMAS = rate;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el RATE_KAMAS a " + MainServidor.RATE_KAMAS);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "RATE_DROP" :
				try {
					byte rate = 0;
					try {
						rate = Byte.parseByte(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.RATE_DROP_NORMAL = rate;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el RATE_DROP a " + MainServidor.RATE_DROP_NORMAL);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "RATE_PVM" :
			case "RATE_XP_PVM" :
			case "RATE_EXP_PVM" :
				try {
					byte rate = 0;
					try {
						rate = Byte.parseByte(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.RATE_XP_PVM = rate;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el RATE_XP_PVM a " + MainServidor.RATE_XP_PVM);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "RATE_PVP" :
			case "RATE_XP_PVP" :
			case "RATE_EXP_PVP" :
				try {
					byte rate = 0;
					try {
						rate = Byte.parseByte(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.RATE_XP_PVP = rate;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el RATE_XP_PVP a " + MainServidor.RATE_XP_PVP);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "RATE_OFICIO" :
			case "RATE_XP_OFICIO" :
			case "RATE_EXP_OFICIO" :
			case "RATE_METIER" :
				try {
					byte rate = 0;
					try {
						rate = Byte.parseByte(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.RATE_XP_OFICIO = rate;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el RATE_XP_OFICIO a " + MainServidor.RATE_XP_OFICIO);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "RATE_CRIANZA_PAVOS" :
			case "RATE_CRIANZA_MONTURA" :
			case "RATE_CRIANZA" :
			case "RATE_ELEVAGE" :
				try {
					byte rate = 0;
					try {
						rate = Byte.parseByte(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.RATE_CRIANZA_MONTURA = rate;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el RATE_CRIANZA_MONTURA a "
					+ MainServidor.RATE_CRIANZA_MONTURA);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "RATE_HONOUR" :
			case "RATE_HONOR" :
				try {
					byte rate = 0;
					try {
						rate = Byte.parseByte(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.RATE_HONOR = rate;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el RATE_HONOR a " + MainServidor.RATE_HONOR);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "RATE_DROP_ARMAS_ETEREAS" :
				try {
					byte rate = 0;
					try {
						rate = Byte.parseByte(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.RATE_DROP_ARMAS_ETEREAS = rate;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el RATE_DROP_ARMAS_ETEREAS a "
					+ MainServidor.RATE_DROP_ARMAS_ETEREAS);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MAX_REPORTES" :
			case "LIMITE_MOSTRAR_REPORTES" :
			case "MAX_MOSTRAR_REPORTES" :
			case "LIMITE_REPORTES" :
				try {
					short limite = 0;
					try {
						limite = Short.parseShort(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.LIMITE_REPORTES = limite;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se configuro a " + limite + " el limite de reportes");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "LIMIT_LADDER" :
			case "LIMITE_LADDER" :
				try {
					short limite = 0;
					try {
						limite = Short.parseShort(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.LIMITE_LADDER = limite;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se configuro a " + limite + " el limite del ladder");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "TIEMPO_MOSTRAR_BOTON_VOTO" :
			case "MINUTOS_SPAMEAR_BOTON_VOTO" :
				try {
					int minutos = 0;
					try {
						minutos = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.MINUTOS_SPAMEAR_BOTON_VOTO = minutos;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se configuro a " + MainServidor.MINUTOS_SPAMEAR_BOTON_VOTO
					+ " minutos para mostrar boton voto");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "TIEMPO_ESTRELLAS_MOBS" :
			case "TIEMPO_MOB_ESTRELLAS" :
			case "SEGUNDOS_ESTRELLAS_MOBS" :
				try {
					int segundos = 0;
					try {
						segundos = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.SEGUNDOS_ESTRELLAS_GRUPO_MOBS = segundos;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se configuro a " + MainServidor.SEGUNDOS_ESTRELLAS_GRUPO_MOBS
					+ " segundos la recarga de estrellas de mobs");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "PROBABILIDAD_ARCHI_MOBS" :
				try {
					int probabilidad = 0;
					try {
						probabilidad = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.PROBABILIDAD_ARCHI_MOBS = probabilidad;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se configuro a " + MainServidor.PROBABILIDAD_ARCHI_MOBS
					+ " probabilidad de archi mobs");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "TIEMPO_ESTRELLAS_RECURSOS" :
			case "TIEMPO_RECURSOS_ESTRELLAS" :
			case "SEGUNDOS_ESTRELLAS_RECURSOS" :
				try {
					int segundos = 0;
					try {
						segundos = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					MainServidor.SEGUNDOS_ESTRELLAS_RECURSOS = segundos;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se configuro a " + MainServidor.SEGUNDOS_ESTRELLAS_RECURSOS
					+ " segundos la recarga de estrellas de mobs");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "RESSOURCES_MAP_STARS" :
			case "STARS_RESSOURCES_MAP" :
			case "ESTRELLAS_RECURSOS_MAPA" :
			case "SUBIR_ESTRELLAS_RECURSOS_MAPA" :
				try {
					int estrellas = 0;
					try {
						estrellas = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						estrellas = 1;
					}
					_perso.getMapa().subirEstrellasOI(estrellas);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se subio " + estrellas + " estrellas recursos a este mapa");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "STARS_RESSOURCES" :
			case "RESSOURCES_STARS" :
			case "ESTRELLAS_RECURSOS" :
			case "SUBIR_ESTRELLAS_RECURSOS" :
				try {
					int estrellas = 0;
					try {
						estrellas = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						estrellas = 1;
					}
					Mundo.subirEstrellasOI(estrellas);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se subio " + estrellas + " estrellas a todos los recursos");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MAPA_STARS_MOBS" :
			case "MAPA_MOBS_STARS" :
			case "MAP_MOBS_STARS" :
			case "MAP_STARS_MOBS" :
			case "MOB_MAP_STARS" :
			case "STARS_MOBS_MAP" :
			case "UP_STARS_MOBS_MAP" :
			case "ESTRELLAS_MAPA_MOBS" :
			case "MOBS_ESTRELLAS_MAPA" :
			case "MOB_ESTRELLAS_MAPA" :
			case "ESTRELLAS_MOBS_MAPA" :
			case "MAPA_ESTRELLAS_MOBS" :
			case "MAPA_ESTRELLAS_MOB" :
			case "MAPA_MOBS_ESTRELLAS" :
			case "MAPA_MOB_ESTRELLAS" :
			case "ESTRELLAS_MOB_MAPA" :
			case "SUBIR_ESTRELLAS_MOBS_MAPA" :
				try {
					int estrellas = 0;
					try {
						estrellas = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						estrellas = 1;
					}
					mapa.subirEstrellasMobs(estrellas);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se subio " + estrellas + " estrellas mob al mapa " + mapa.getID());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MOBS_ESTRELLAS_TODOS" :
			case "MOB_ESTRELLAS_TODOS" :
			case "MOBS_STARS_TODOS" :
			case "TODOS_MOBS_ESTRELLAS" :
			case "TODOS_MOBS_STARS" :
			case "TODOS_STARS_MOBS" :
			case "TODOS_ESTRELLAS_MOBS" :
			case "ALL_MOBS_STARS" :
			case "ALL_MOBS_ESTRELLAS" :
			case "ALL_STARS_MOBS" :
			case "UP_ALL_STARS_MOBS" :
			case "ESTRELLAS_MOBS_TODOS" :
			case "SUBIR_ESTRELLAS_MOB_TODOS" :
				try {
					int estrellas = 0;
					try {
						estrellas = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						estrellas = 1;
					}
					Mundo.subirEstrellasMobs(estrellas);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se subio " + estrellas + " estrellas a todos los mobs");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "SHOW_A" :
			case "MOSTRAR_A" :
			case "SHOW_RESTRICTIONS_A" :
			case "MOSTRAR_RESTRICCIONES_A" :
				try {
					objetivo = _perso;
					if (infos.length > 1) {
						objetivo = Mundo.getPersonajePorNombre(infos[1]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, objetivo.mostrarmeA());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "SHOW_B" :
			case "MOSTRAR_B" :
			case "SHOW_RESTRICTIONS_B" :
			case "MOSTRAR_RESTRICCIONES_B" :
				try {
					objetivo = _perso;
					if (infos.length > 1) {
						objetivo = Mundo.getPersonajePorNombre(infos[1]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, objetivo.mostrarmeB());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "SET_RESTRICCIONES_A" :
			case "RESTRICCIONES_A" :
			case "MODIFICAR_A" :
			case "RESTRICCION_A" :
				try {
					int restriccion = 0;
					int modificador = 0;
					try {
						restriccion = Integer.parseInt(infos[1]);
						modificador = Integer.parseInt(infos[2]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					objetivo = _perso;
					if (infos.length > 3) {
						objetivo = Mundo.getPersonajePorNombre(infos[3]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					objetivo.modificarA(restriccion, restriccion ^ modificador);
					GestorSalida.ENVIAR_AR_RESTRICCIONES_PERSONAJE(objetivo);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se coloco la restriccionA " + objetivo.getRestriccionesA()
					+ " al pj " + objetivo.getNombre());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "SET_RESTRICCIONES_B" :
			case "RESTRICCIONES_B" :
			case "MODIFICAR_B" :
			case "RESTRICCION_B" :
				try {
					int restriccion = 0;
					int modificador = 0;
					try {
						restriccion = Integer.parseInt(infos[1]);
						modificador = Integer.parseInt(infos[2]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					objetivo = _perso;
					if (infos.length > 3) {
						objetivo = Mundo.getPersonajePorNombre(infos[3]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					objetivo.modificarB(restriccion, restriccion ^ modificador);
					objetivo.refrescarEnMapa();
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se coloco la restriccionB " + objetivo.getRestriccionesB()
					+ " al pj " + objetivo.getNombre());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "PANEL_ALL" :
			case "PANEL_ONLINE" :
			case "PANEL_TODOS" :
				try {
					infos = mensaje.split(" ", 2);
					GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION_TODOS(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
				}
				break;
			case "PANEL_ALONE" :
			case "PANEL" :
			case "PANEL_SOLO" :
				try {
					infos = mensaje.split(" ", 2);
					GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(_perso, infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
				}
				break;
			case "PREMIO_CACERIA" :
				try {
					Mundo.KAMAS_OBJ_CACERIA = infos[1]; // kamas | id,cant;id,cant
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se fijo el premio de la caceria a: " + infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
				}
				break;
			case "NOMBRE_CACERIA" :
				try {
					Mundo.NOMBRE_CACERIA = infos[1];
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se fijo el nombre de la caceria a: " + infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
				}
				break;
			case "LIMPIAR_CACERIA" :
				try {
					Mundo.KAMAS_OBJ_CACERIA = "";
					Mundo.NOMBRE_CACERIA = "";
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se limpio el nombre caceria y premio caceria");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
				}
				break;
			case "MAPAS_KOLISEO" :
				try {
					MainServidor.MAPAS_KOLISEO = infos[1];
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se fijo la lista de mapas koliseo a: " + infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
				}
				break;
			case "LISTA_RASTREOS" :
				try {
					StringBuilder strB = new StringBuilder();
					for (final int i : ServidorSocket.RASTREAR_CUENTAS) {
						if (strB.length() > 0) {
							strB.append(", ");
						}
						strB.append(i);
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Las ids de las cuentas que estan siendo rastreadas son " + strB
					.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "BORRAR_RASTREOS" :
				try {
					ServidorSocket.RASTREAR_CUENTAS.clear();
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se limpio la lista de rastreados");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "SPY_PERSO" :
			case "SPY_PJ" :
			case "ESPIAR_PJ" :
			case "ESPIAR_JUGADOR" :
			case "SPY_PLAYER" :
			case "RASTREAR_PJ" :
				try {
					try {
						objetivo = Mundo.getPersonajePorNombre(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Personaje no existe");
						return;
					}
					if (!ServidorSocket.RASTREAR_CUENTAS.contains(objetivo.getCuentaID())) {
						ServidorSocket.RASTREAR_CUENTAS.add(objetivo.getCuentaID());
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se agregó a la lista de rastreos: " + infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "SPY_ACCOUNT" :
			case "SPY_COMPTE" :
			case "RASTREAR_CUENTA" :
				try {
					Cuenta cuenta;
					try {
						cuenta = Mundo.getCuentaPorNombre(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Cuenta no existe");
						return;
					}
					if (cuenta == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Cuenta no existe");
						return;
					}
					if (!ServidorSocket.RASTREAR_CUENTAS.contains(cuenta.getID())) {
						ServidorSocket.RASTREAR_CUENTAS.add(cuenta.getID());
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se agregó a la lista de rastreos: " + infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "MODIFY_OBJETIVE_MISION" :
			case "MODIFICA_OBJETIVO_MISION" :
			case "MODIFICAR_MISION_OBJETIVO" :
			case "MODIFICA_MISION_OBJETIVO" :
			case "MODIFICAR_OBJETIVO_MISION" :
				try {
					int id = 0;
					String args = "";
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					try {
						args = infos[2];
					} catch (final Exception e) {}
					final MisionObjetivoModelo objMision = Mundo.getMisionObjetivoModelo(id);
					if (objMision == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El objetivo mision no existe");
						return;
					}
					objMision.setArgs(args);
					GestorSQL.UPDATE_OBJETIVO_MISION(id, args);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El objetivo mision (" + id + ") tipo: " + objMision.getTipo()
					+ " ha modificado sus args a " + args);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "MODIFY_RECOMPENSE_STEP" :
			case "MODIFICA_RECOMPENSA_ETAPA" :
			case "MODIFICA_PREMIO_ETAPA" :
			case "MODIFICAR_RECOMPENSA_ETAPA" :
				try {
					int id = 0;
					String args = "";
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					try {
						args = infos[2];
					} catch (final Exception e) {}
					final MisionEtapaModelo etapa = Mundo.getEtapa(id);
					if (etapa == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La etapa de misión no existe");
						return;
					}
					etapa.setRecompensa(args);
					GestorSQL.UPDATE_RECOMPENSA_ETAPA(id, args);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La etapa de misión (" + id + ") " + etapa.getNombre()
					+ " ha modificado sus recompensas a " + args);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "MODIFICA_ETAPA" :
			case "MODIFY_STEP" :
			case "MODIFICAR_ETAPA" :
				try {
					int id = 0;
					String args = "";
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					try {
						args = infos[2];
					} catch (final Exception e) {}
					final MisionEtapaModelo etapa = Mundo.getEtapa(id);
					if (etapa == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La etapa de misión no existe");
						return;
					}
					etapa.setObjetivos(args);
					GestorSQL.UPDATE_ETAPA(id, args);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La etapa de misión (" + id + ") " + etapa.getNombre()
					+ " ha modificado sus objetivos a " + args);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "INFO_STEP" :
			case "INFO_STEPS" :
			case "INFO_ETAPAS" :
			case "INFO_ETAPA" :
				try {
					int id = 0;
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					final MisionEtapaModelo etapa = Mundo.getEtapa(id);
					if (etapa == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La etapa de misión no existe");
						return;
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La etapa de misión (" + id + ") " + etapa.getNombre()
					+ " tiene como objetivos: " + etapa.strObjetivos());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "MODIFY_QUEST" :
			case "MODIFY_MISSION" :
			case "MODIFICAR_QUEST" :
			case "MODIFICAR_MISION" :
			case "MODIFICA_MISION" :
			case "MODIFICA_QUEST" :
				try {
					int id = 0;
					String pregDarMision = "", pregMisCumplida = "", pregMisIncompleta = "", etapas = "";
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					try {
						etapas = infos[2];
					} catch (final Exception e) {}
					try {
						pregDarMision = infos[3];
					} catch (final Exception e) {}
					try {
						pregMisCumplida = infos[4];
					} catch (final Exception e) {}
					try {
						pregMisIncompleta = infos[5];
					} catch (final Exception e) {}
					final MisionModelo mision = Mundo.getMision(id);
					if (mision == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La misión no existe");
						return;
					}
					mision.setEtapas(etapas);
					mision.setPreguntas(pregDarMision, Mision.ESTADO_NO_TIENE);
					mision.setPreguntas(pregMisCumplida, Mision.ESTADO_COMPLETADO);
					mision.setPreguntas(pregMisIncompleta, Mision.ESTADO_INCOMPLETO);
					GestorSQL.UPDATE_MISION(id, etapas, pregDarMision, pregMisCumplida, pregMisIncompleta);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La misión (" + id + ") " + mision.getNombre()
					+ " ha modificado sus etapas: " + etapas + ", pregDarMision: " + pregDarMision + ", pregMisCumplida: "
					+ pregMisCumplida + ", pregMisIncompleta: " + pregMisIncompleta);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "INFO_QUEST" :
			case "INFO_MISION" :
				try {
					int id = 0;
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					final MisionModelo mision = Mundo.getMision(id);
					if (mision == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La misión no existe");
						return;
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La misión (" + id + ") " + mision.getNombre()
					+ " tiene como info etapas: " + mision.strEtapas() + ", pregDarMision: " + mision.strMisionPregunta(
					Mision.ESTADO_NO_TIENE) + ", pregMisCumplida: " + mision.strMisionPregunta(Mision.ESTADO_COMPLETADO)
					+ ", pregMisIncompleta: " + mision.strMisionPregunta(Mision.ESTADO_INCOMPLETO));
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "NIVEL_OBJETO_MODELO" :
				try {
					int id = Integer.parseInt(infos[1]);
					short nivel = Short.parseShort(infos[2]);
					ObjetoModelo objModelo = Mundo.getObjetoModelo(id);
					if (objModelo == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El objeto no existe");
						return;
					}
					objModelo.setNivel(nivel);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el objeto (" + id + ") " + objModelo.getNombre()
					+ " con nivel " + objModelo.getNivel());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
				}
				break;
			case "GFX_OBJETO_MODELO" :
				try {
					int id = Integer.parseInt(infos[1]);
					ObjetoModelo objModelo = Mundo.getObjetoModelo(id);
					if (objModelo == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El objeto no existe");
						return;
					}
					objModelo.setGFX(Integer.parseInt(infos[2]));
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modificó el objeto (" + id + ") " + objModelo.getNombre()
					+ " con gfx " + infos[2]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
				}
				break;
			case "PREPARAR_LISTA_NIVEL" :
				try {
					Mundo.prepararListaNivel();
					GestorSalida.ENVIAR_ÑB_LISTA_NIVEL_TODOS();
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se actualizó la lista de niveles modificados");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "PREPARAR_LISTA_GFX" :
				try {
					Mundo.prepararListaGFX();
					GestorSalida.ENVIAR_ÑA_LISTA_GFX_TODOS();
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se actualizó la lista de GFXs modificados");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "AGREGAR_MOBS_MAPA" :
			case "ADD_MOBS_MAPA" :
			case "ADD_MOBS" :
			case "AGREGAR_MOBS" :
			case "INSERTAR_MOBS" :
				try {
					String mobs = "";
					if (infos.length > 1) {
						mobs = infos[1];
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects");
						return;
					}
					try {
						GestorSQL.UPDATE_SET_MOBS_MAPA(mapa.getID(), mobs);
						mapa.insertarMobs(mobs);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Has cometido algun error");
						return;
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se inserto la lista de mobs " + mobs + " al mapa " + mapa.getID());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "SET_DATE_MAP" :
			case "SET_FECHA_MAPA" :
				try {
					short mapaID = 0;
					String date = "";
					try {
						mapaID = Short.parseShort(infos[1]);
						date = infos[2];
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					mapa = Mundo.getMapa(mapaID);
					if (mapa == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mapa no existe");
						return;
					}
					mapa.setFecha(date);
					GestorSQL.UPDATE_FECHA_MAPA(mapa.getID(), date);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se cambio la fecha del mapa " + mapa.getID() + " a " + date);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "REFRESH_MAP" :
			case "REFRESCAR_MAPA" :
			case "RELOAD_MAP" :
			case "RECARGAR_MAPA" :
			case "RELOAD_DATE_KEY_MAPDATA_MAP" :
			case "MAP_OLD" :
			case "MAPA_VIEJO" :
			case "CAMBIAR_MAPA_VIEJO" :
			case "CHANGE_MAP_OLD" :
				try {
					short mapaID = 0;
					try {
						mapaID = Short.parseShort(infos[1]);
					} catch (final Exception e) {}
					final String[] key = GestorSQL.GET_NUEVA_FECHA_KEY(mapaID).split(Pattern.quote("|"));
					// if (key.length < 20) {
					// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "tiene una key muy corta");
					// }
					mapa = Mundo.getMapa(mapaID);
					if (mapa == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mapa no existe");
						return;
					}
					mapa.setKeyMapData(key[0], key[1], key[2]);
					GestorSQL.UPDATE_FECHA_KEY_MAPDATA(mapa.getID(), key[0], key[1], key[2]);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se cambió el mapa " + mapa.getID() + " por un mapa con key");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "LIST_MOBS" :
			case "LISTA_MOBS" :
				try {
					int id = 0;
					StringBuilder strB = new StringBuilder();
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
						return;
					}
					for (final MobModelo mMod : Mundo.MOBS_MODELOS.values()) {
						if (mMod.getTipoMob() == id) {
							strB.append("ID: " + mMod.getID() + " - Nombre: " + mMod.getNombre() + " - Niveles: " + mMod
							.listaNiveles() + " - Colores: " + mMod.getColores() + "\n");
						}
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Los Mobs Tipo Criatura - " + Constantes.getNombreTipoMob(id)
					+ " son:\n" + strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "LIST_TYPE_MOBS" :
			case "LISTA_TIPO_MOBS" :
			case "LIST_CREATURES_TYPE" :
			case "LIST_TYPE_CREATURES" :
			case "LIST_TYPE_CRIATURES" :
			case "LISTA_TIPO_CRIATURAS" :
				try {
					StringBuilder strB = new StringBuilder();
					for (int i = -1; i < 100; i++) {
						if (!Constantes.getNombreTipoMob(i).isEmpty()) {
							strB.append("Tipo ID: " + i + " - Criaturas: " + Constantes.getNombreTipoMob(i) + "\n");
						}
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Lista Tipo Criatura son:\n" + strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "BORRAR_OTRO_INTERACTIVO" :
			case "DELETE_OTRO_INTERACTIVO" :
			case "ELIMINAR_OTRO_INTERACTIVO" :
			case "DEL_OTRO_INTERACTIVO" :
			case "BORRAR_OTRO_OI" :
			case "ELIMINAR_OTRO_OI" :
			case "DEL_OTRO_OI" :
				try {
					int id = 0;
					short mapaID = 0, celdaID = 0;
					try {
						id = Integer.parseInt(infos[1]);
						mapaID = Short.parseShort(infos[2]);
						celdaID = Short.parseShort(infos[3]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos inválidos");
						return;
					}
					Mundo.borrarOtroInteractivo(id, mapaID, celdaID, 0, false);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se borraron todos los otros interactivos con gfxID: " + id
					+ " mapaID: " + mapaID + " celdaID: " + celdaID);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "ADD_OTRO_INTERACTIVO" :
			case "ADD_OTRO_OI" :
			case "AGREGAR_OTRO_INTERACTIVO" :
				try {
					int id = 0, accionID = 0;
					int tiempoRecarga = 0;
					short mapaID = 0, celdaID = 0;
					String args = "", condicion = "";
					String descripcion = "";
					StringBuilder strB = new StringBuilder();
					try {
						id = Integer.parseInt(infos[1]);
						mapaID = Short.parseShort(infos[2]);
						celdaID = Short.parseShort(infos[3]);
						accionID = Integer.parseInt(infos[4]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos inválidos");
						return;
					}
					try {
						args = infos[5];
					} catch (final Exception e) {}
					try {
						condicion = infos[6];
					} catch (final Exception e) {}
					try {
						tiempoRecarga = Integer.parseInt(infos[7]);
					} catch (final Exception e) {}
					try {
						descripcion = infos[8];
					} catch (final Exception e) {}
					strB.append("Se creo acción para Otro Interactivo GfxID: " + id + ", mapaID: " + mapaID + ", celdaID: "
					+ celdaID + ", accionID: " + accionID + ", args: " + args + ", condicion: " + condicion + ", tiempoRecarga: "
					+ tiempoRecarga);
					Mundo.borrarOtroInteractivo(id, mapaID, celdaID, accionID, true);
					OtroInteractivo otro = new OtroInteractivo(id, mapaID, celdaID, accionID, args, condicion, tiempoRecarga);
					Mundo.addOtroInteractivo(otro);
					GestorSQL.INSERT_OTRO_INTERACTIVO(id, mapaID, celdaID, accionID, args, condicion, tiempoRecarga, descripcion);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MOSTRAR_OTROS_INTERACTIVOS" :
			case "MOSTRAR_OTROS_OIS" :
			case "LISTAR_OTROS_INTERACTIVOS" :
			case "LIST_OTROS_INTERACTIVOS" :
			case "SHOW_OTHER_INTERACTIVES" :
			case "LISTA_OTROS_OIS" :
			case "LISTAR_OTROS_OIS" :
				try {
					short mapaID = mapa.getID();
					StringBuilder strB = new StringBuilder();
					try {
						mapaID = Short.parseShort(infos[1]);
					} catch (final Exception e) {}
					for (final OtroInteractivo oi : Mundo.OTROS_INTERACTIVOS) {
						if (oi.getMapaID() != mapaID) {
							continue;
						}
						strB.append("\n");
						strB.append("Mapa: " + oi.getMapaID() + " Celda: " + oi.getCeldaID() + " GfxID: " + oi.getGfxID()
						+ " Accion: " + oi.getAccionID() + " Args: " + oi.getArgs() + " Condicion: " + oi.getCondicion());
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Los otros interactivos son:" + strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "ACCESO_ADMIN_MINIMO" :
			case "BLOQUEAR" :
			case "BLOCK_GM" :
				try {
					byte accesoGM = 0;
					byte botarRango = 0;
					try {
						accesoGM = Byte.parseByte(infos[1]);
						botarRango = Byte.parseByte(infos[2]);
					} catch (final Exception e) {}
					MainServidor.ACCESO_ADMIN_MINIMO = accesoGM;
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso,
						"Le serveur est désormais accessible au joueur dont le GM est supérieur à : " + accesoGM);
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Server bloqueado a Nivel GM : " + accesoGM);
					}
					if (botarRango > 0) {
						for (final Personaje pj : Mundo.getPersonajesEnLinea()) {
							if (pj.getCuenta().getAdmin() < botarRango) {
								try {
									GestorSalida.ENVIAR_M0_MENSAJE_BASICOS_SVR_MUESTRA_DISCONNECT(pj.getServidorSocket(), "19", "", "");
									pj.getServidorSocket().cerrarSocket(true, "command BLOCK GM");
								} catch (Exception e) {}
							}
						}
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso,
							"Le joueurs dont le GM est inférieur à celui spécifié ont été expulsés.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Los jugadores nivel GM inferior a " + botarRango
							+ " han sido expulsados.");
						}
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "ADD_ACTION_REPONSE" :
			case "ADD_ACTION_ANSWER" :
			case "ADD_ACCION_RESPUESTA" :
			case "ADD_ACCIONES_RESPUESTA" :
			case "ADD_ACCIONES_RESPUESTAS" :
			case "FIJAR_ACCION_RESPUESTA" :
			case "AGREGAR_ACCION_RESPUESTA" :
				try {
					infos = mensaje.split(" ", 5);
					int id = 0, accionID = 0;
					String args = "", condicion = "";
					StringBuilder strB = new StringBuilder();
					try {
						id = Integer.parseInt(infos[1]);
						accionID = Integer.parseInt(infos[2]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos inválidos");
						return;
					}
					try {
						args = infos[3];
					} catch (final Exception e) {}
					try {
						condicion = infos[4];
					} catch (final Exception e) {}
					RespuestaNPC respuesta = Mundo.getRespuestaNPC(id);
					if (respuesta == null) {
						respuesta = new RespuestaNPC(id);
						Mundo.addRespuestaNPC(respuesta);
					}
					Accion accion = new Accion(accionID, args, condicion);
					respuesta.addAccion(accion);
					strB.append("La acción respuesta " + respuesta.getID() + ", accionID: " + accion.getID() + ", args: " + accion
					.getArgs() + ", condición: " + accion.getCondicion() + " agregada");
					if (GestorSQL.REPLACE_ACCIONES_RESPUESTA(respuesta.getID(), accion.getID(), accion.getArgs(), accion
					.getCondicion())) {
						strB.append(" a la BDD");
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "DELETE_ACTIONS_ANSWER" :
			case "DELETE_ACTIONS_REPONSE" :
			case "DEL_ACTIONS_ANSWER" :
			case "DEL_ACTIONS_REPONSE" :
			case "REMOVE_ACTONS_REPONSE" :
			case "BORRAR_ACCIONES_RESPUESTA" :
			case "BORRAR_ACCION_RESPUESTA" :
				try {
					int id = 0;
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos inválidos");
						return;
					}
					RespuestaNPC respuesta = Mundo.getRespuestaNPC(id);
					if (respuesta == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Respuesta inválida");
						return;
					}
					respuesta.borrarAcciones();
					GestorSQL.DELETE_ACCIONES_RESPUESTA(id);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se borraron todas las acciones de la respuesta " + id);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "FIX_QUESTION_NPC" :
			case "FIX_QUESTION" :
			case "FIJAR_PREGUNTA" :
			case "FIJAR_NPC_PREGUNTA" :
			case "FIJAR_PREGUNTA_NPC" :
				try {
					int npcID = 0;
					int preguntaID = 0;
					StringBuilder strB = new StringBuilder();
					try {
						npcID = Integer.parseInt(infos[1]);
						preguntaID = Integer.parseInt(infos[2]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos inválidos");
						return;
					}
					final NPCModelo npcModelo = Mundo.getNPCModelo(npcID);
					if (npcModelo == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "NPC no existe");
						return;
					}
					strB.append("Fija al NPC Modelo " + npcID + " - Nombre: " + npcModelo.getNombre() + ", Pregunta: "
					+ preguntaID);
					npcModelo.setPreguntaID(preguntaID);
					if (GestorSQL.UPDATE_NPC_PREGUNTA(npcID, preguntaID)) {
						strB.append(" a la BDD");
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "ADD_ANSWERS" :
			case "FIX_ANSWERS" :
			case "FIX_REPONSES" :
			case "WRITE_ANSWERS" :
			case "FIJAR_RESPUESTAS" :
			case "FIJAR_RESPUESTAS_PREGUNTA" :
			case "SET_ANSWERS" :
			case "SET_RESPUESTAS" :
				try {
					StringBuilder strB = new StringBuilder();
					int id = 0;
					String args = "";
					String respuestas = "";
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos inválidos");
						return;
					}
					try {
						respuestas = infos[2];
					} catch (final Exception e) {}
					try {
						args = infos[3];
					} catch (final Exception e) {}
					PreguntaNPC pregunta = Mundo.getPreguntaNPC(id);
					if (pregunta == null) {
						pregunta = new PreguntaNPC(id, respuestas, args, "");
						Mundo.addPreguntaNPC(pregunta);
					} else {
						pregunta.setRespuestas(respuestas);
						pregunta.setParams(args);
					}
					strB.append("Parámetros de la pregunta " + id + " => respuestas: " + pregunta.getStrRespuestas() + ", args: "
					+ pregunta.getParams() + ", alternos: " + pregunta.getStrAlternos());
					if (GestorSQL.REPLACE_PREGUNTA_NPC(pregunta)) {
						strB.append(" a la BDD");
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "SET_ALTERNOS_PREGUNTA" :
			case "SET_ALTERNOS_QUESTION" :
			case "SET_PREGUNTA_ALTERNAS" :
			case "SET_PREGUNTA_ALTERNOS" :
			case "FIJAR_PREGUNTA_ALTERNOS" :
			case "FIJAR_ALTERNOS" :
			case "FIJAR_ALTENOS_PREGUNTA" :
			case "FIJAR_PREGUNTA_ALTERNAS" :
			case "SET_ALTERNOS" :
			case "SET_QUESTIONS_CONDITIONS" :
				try {
					int id = 0;
					String alternos = "";
					StringBuilder strB = new StringBuilder();
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos inválidos");
						return;
					}
					try {
						alternos = infos[2];
					} catch (final Exception e) {}
					PreguntaNPC pregunta = Mundo.getPreguntaNPC(id);
					if (pregunta == null) {
						pregunta = new PreguntaNPC(id, "", "", alternos);
						Mundo.addPreguntaNPC(pregunta);
					} else {
						pregunta.setPreguntasCondicionales(alternos);
					}
					strB.append("Parámetros de la pregunta " + id + " => respuestas: " + pregunta.getStrRespuestas() + ", args: "
					+ pregunta.getParams() + ", alternos: " + pregunta.getStrAlternos());
					if (GestorSQL.REPLACE_PREGUNTA_NPC(pregunta)) {
						strB.append(" a la BDD");
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "BUSCAR_PREGUNTA" :
			case "BUSCAR_PREGUNTAS" :
			case "SEARCH_QUESTIONS" :
			case "SEARCH_QUESTION" :
				try {
					infos = mensaje.split(" ", 2);
					String buscar = infos[1];
					GestorSalida.enviar(_perso, "DBQ" + buscar.toUpperCase());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "BUSCAR_RESPUESTA" :
			case "BUSCAR_RESPUESTAS" :
			case "SEARCH_ANSWERS" :
			case "SEARCH_ANSWER" :
				try {
					infos = mensaje.split(" ", 2);
					String buscar = infos[1];
					GestorSalida.enviar(_perso, "DBA" + buscar.toUpperCase());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "SHOW_QUESTIONS" :
			case "SHOW_QUESTION" :
			case "LISTAR_PREGUNTAS" :
			case "LISTA_PREGUNTAS" :
			case "MOSTRAR_PREGUNTAS" :
				try {
					for (String ss : infos[1].split(",")) {
						if (ss.isEmpty()) {
							continue;
						}
						int respuestaID = 0;
						try {
							respuestaID = Integer.parseInt(ss);
						} catch (final Exception e) {
							continue;
						}
						PreguntaNPC pregunta = Mundo.getPreguntaNPC(respuestaID);
						if (pregunta == null) {
							continue;
						}
						StringBuilder sB = new StringBuilder();
						sB.append("\n\t--> Answers: " + pregunta.getStrRespuestas());
						sB.append("\n\t--> Alternates: " + pregunta.getStrAlternos());
						GestorSalida.enviar(_perso, "DLQ" + respuestaID + "|" + sB.toString());
					}
					GestorSalida.enviar(_perso, "DX");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
				}
				break;
			case "SHOW_ANSWER" :
			case "SHOW_ANSWERS" :
			case "SHOW_REPONSES" :
			case "LISTAR_RESPUESTAS" :
			case "LISTA_RESPUESTAS" :
			case "MOSTRAR_RESPUESTAS" :
				try {
					for (String ss : infos[1].split(",")) {
						if (ss.isEmpty()) {
							continue;
						}
						int respuestaID = 0;
						try {
							respuestaID = Integer.parseInt(ss);
						} catch (final Exception e) {
							continue;
						}
						RespuestaNPC respuesta = Mundo.getRespuestaNPC(respuestaID);
						if (respuesta == null) {
							continue;
						}
						StringBuilder sB = new StringBuilder();
						sB.append("\n\t--> Condition: " + respuesta.getCondicion());
						for (Accion a : respuesta.getAcciones()) {
							sB.append("\n\t--> Action ID: " + a.getID() + ", Args: " + a.getArgs());
						}
						GestorSalida.enviar(_perso, "DLA" + respuestaID + "|" + sB.toString());
					}
					GestorSalida.enviar(_perso, "DX");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
				}
				break;
			case "LISTAR_RESPUESTAS_POR_TIPO_Y_ARGS" :
			case "LISTAR_RESPUESTAS_POR_TIPO_ARGS" :
			case "LISTAR_RESPUESTAS_POR_TIPO_O_ARGS" :
			case "LISTAR_RESPUESTAS_TIPO_ARGS" :
			case "LISTAR_ACCIONES_RESPUESTAS_POR_TIPO_Y_ARGS" :
			case "LISTAR_ACCIONES_RESPUESTAS_POR_TIPO_ARGS" :
			case "LISTAR_ACCIONES_RESPUESTAS_POR_TIPO_O_ARGS" :
			case "LISTAR_ACCIONES_RESPUESTAS_TIPO_ARGS" :
			case "LISTA_RESPUESTAS_POR_TIPO_Y_ARGS" :
			case "LISTA_RESPUESTAS_POR_TIPO_ARGS" :
			case "LISTA_RESPUESTAS_POR_TIPO_O_ARGS" :
			case "LISTA_RESPUESTAS_TIPO_ARGS" :
			case "LISTA_ACCIONES_RESPUESTAS_POR_TIPO_Y_ARGS" :
			case "LISTA_ACCIONES_RESPUESTAS_POR_TIPO_ARGS" :
			case "LISTA_ACCIONES_RESPUESTAS_POR_TIPO_O_ARGS" :
			case "LISTA_ACCIONES_RESPUESTAS_TIPO_ARGS" :
				try {
					int id = -100;
					String args = "";
					StringBuilder strB = new StringBuilder();
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {}
					try {
						args = infos[2];
					} catch (Exception e) {}
					for (RespuestaNPC respuesta2 : Mundo.NPC_RESPUESTAS.values()) {
						boolean b = false;
						for (Accion a : respuesta2.getAcciones()) {
							if (id != -100 && a.getID() != id) {
								continue;
							}
							if (!args.isEmpty() && !a.getArgs().toUpperCase().contains(args.toUpperCase())) {
								continue;
							}
							b = true;
							break;
						}
						if (!b) {
							continue;
						}
						if (strB.length() > 0) {
							strB.append("\n----------------------------------\n");
						}
						strB.append("Acciones de la respuesta ID " + respuesta2.getID() + ", condición: " + respuesta2
						.getCondicion());
						for (Accion a : respuesta2.getAcciones()) {
							strB.append("\n\tAccion ID: " + a.getID() + ", Args: " + a.getArgs());
						}
					}
					if (strB.length() == 0) {
						strB.append("No se encontraron respuestas con esos datos");
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "LISTA_PREGUNTAS_CON_RESPUESTAS" :
			case "LISTA_PREGUNTAS_CON_RESPUESTA" :
			case "LISTAR_PREGUNTAS_CON_RESPUESTAS" :
			case "LISTAR_PREGUNTAS_CON_RESPUESTA" :
				try {
					int id = 0;
					StringBuilder strB = new StringBuilder();
					try {
						id = Integer.parseInt(infos[1]);
					} catch (Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
						return;
					}
					for (PreguntaNPC pregunta2 : Mundo.NPC_PREGUNTAS.values()) {
						if (pregunta2.getRespuestas().contains(id)) {
							if (strB.length() > 0) {
								strB.append("\n");
							}
							strB.append("Respuestas de la pregunta " + pregunta2.getID() + ", respuestas: " + pregunta2
							.getStrRespuestas());
						}
					}
					if (strB.length() == 0) {
						strB.append("No se encontraron preguntas con esos datos");
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "UPDATE_HOUSE" :
			case "MODIFY_HOUSE" :
			case "MODIFICAR_CASA" :
			case "UPDATE_CASA" :
			case "MAPA_DENTRO_CASA" :
			case "ACTUALIZAR_CASA" :
				try {
					short mapaID = 0, celdaID = 0;
					try {
						mapaID = Short.parseShort(infos[1]);
						celdaID = Short.parseShort(infos[2]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
						return;
					}
					Casa casa = null;
					final short ancho = mapa.getAncho();
					final short[] dir = {(short) -ancho, (short) -(ancho - 1), (short) (ancho - 1), ancho, 0};
					for (int i = 0; i < 5; i++) {
						casa = Mundo.getCasaPorUbicacion(mapa.getID(), _perso.getCelda().getID() + dir[i]);
						if (casa != null) {
							break;
						}
					}
					if (casa == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No existe la casa");
						return;
					}
					casa.setCeldaIDDentro(celdaID);
					casa.setMapaIDDentro(mapaID);
					GestorSQL.UPDATE_CELDA_MAPA_DENTRO_CASA(casa);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se actualizo la casa " + casa.getID() + " , con mapaID dentro: "
					+ mapaID + " y como celdaID dentro: " + celdaID);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "SHOW_STATS_OBJETO" :
			case "GET_STATS_OBJETO" :
			case "INFO_ITEM" :
			case "INFO_OBJETO" :
				try {
					int id = -1;
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {}
					Objeto obj = Mundo.getObjeto(id);
					if (obj == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Objeto nulo");
						return;
					}
					ObjetoModelo objModelo = obj.getObjModelo();
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Info del objeto " + id + ": \nNombre Objeto - " + objModelo
					.getNombre() + "\nNivel - " + objModelo.getNivel() + "\nTipo - " + objModelo.getTipo() + "\nPosicion - " + obj
					.getPosicion() + "\nString Stats - " + obj.convertirStatsAString(true) + "\n---------------------------");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "GUARDAR" :
			case "SALVAR" :
			case "SAVE" :
				if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_SALVANDO) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Une sauvegarde est déjà en cours.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso,
						"No se puede ejecutar el comando, porque el server ya se esta salvando");
					}
					return;
				}
				MainServidor.redactarLogServidorln("Se uso el comando SALVAR (COMANDOS) por " + _perso.getNombre());
				new SalvarServidor(false);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Lancement de la sauvegarde...");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Salvando servidor...");
				}
				break;
			case "GUARDAR_TODOS" :
			case "SALVAR_TODOS" :
			case "SAVE_ALL" :
				if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_SALVANDO) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Une sauvegarde est déjà en cours.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso,
						"No se puede ejecutar el comando, porque el server ya se esta salvando");
					}
					return;
				}
				MainServidor.redactarLogServidorln("Se uso el comando SALVAR (COMANDOS) por " + _perso.getNombre());
				new SalvarServidor(true);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Lancement de la sauvegarde ONLINE Y OFFLINE ...");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Salvando servidor ONLINE Y OFFLINE ...");
				}
				break;
			case "SECONDS_ON" :
			case "SECONDS_ONLINE" :
			case "SEGUNDOS_ON" :
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El servidor tiene " + ServidorServer.getSegundosON()
				+ " segundos ONLINE");
				break;
			case "PASS_TURN" :
			case "PASAR_TURNO" :
			case "FIN_TURNO" :
			case "END_TURN" :
			case "CHECK_TURNO" :
			case "DEBUG_TURN" :
				if (_perso.getPelea() == null) {
					return;
				}
				String finTurno = _perso.getPelea().pasarTurno(null);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Debug du tour.." + finTurno);
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se verifico el pase de turno " + finTurno);
				}
				break;
			case "SET_TEMP" :
				try {
					StringBuilder strB = new StringBuilder();
					if (_perso.getPelea() == null) {
						return;
					}
					try {
						strB.append(infos[1]);
					} catch (final Exception e) {}
					_perso.getPelea().setTempAccion(strB.toString());
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se asigno la tempAccion: " + strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "COUNT_OIS" :
			case "CONTAR_OBJETOS_INTERACTIVOS" :
			case "CONTAR_OIS" :
				try {
					int cantidad = 0;
					for (final Celda celda : mapa.getCeldas().values()) {
						if (celda.getObjetoInteractivo() != null) {
							cantidad++;
						}
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Este mapa tiene " + cantidad + " interactivos");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "SHOW_OIS" :
				// case "MOSTRAR_OTROS_INTERACTIVOS" :
			case "MOSTRAR_OBJETOS_INTERACTIVOS" :
			case "MOSTRAR_OIS" :
				try {
					StringBuilder strB = new StringBuilder();
					for (final Celda celda : mapa.getCeldas().values()) {
						if (celda.getObjetoInteractivo() != null) {
							strB.append("\n");
							strB.append("Mapa: " + mapa.getID() + " Celda: " + celda.getID() + " Movimiento: " + celda.getMovimiento()
							+ " Gfx: " + celda.getObjetoInteractivo().getGfxID());
							try {
								strB.append(" ID ObjInt: " + celda.getObjetoInteractivo().getObjIntModelo().getID());
							} catch (Exception e) {}
						}
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Este mapa tiene:" + strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "RESETEAR_OBJETOS_INTERACTIVOS" :
			case "RESET_OBJETOS_INTERACTIVOS" :
			case "RESETEAR_OIS" :
			case "REINICIAR_OIS" :
			case "REFRESCAR_OIS" :
				try {
					int cantidad = 0;
					StringBuilder strB = new StringBuilder();
					for (final Celda celda : mapa.getCeldas().values()) {
						if (celda.getObjetoInteractivo() != null) {
							if (strB.length() > 0) {
								strB.append("|");
							}
							celda.getObjetoInteractivo().recargando(true);
							strB.append(celda.getID() + ";" + celda.getObjetoInteractivo().getInfoPacket());
							cantidad++;
						}
					}
					GestorSalida.ENVIAR_GDF_FORZADO_MAPA(mapa, strB.toString());
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se ha refrescado " + cantidad + " interactivos");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MAX_MERCANTES" :
			case "MAX_MERCHANTS" :
				try {
					byte limite = 0;
					try {
						limite = Byte.parseByte(infos[1]);
					} catch (final Exception e) {}
					if (limite < 0) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					mapa.setMaxMercantes(limite);
					if (!GestorSQL.UPDATE_MAPA_MAX_MERCANTES(mapa.getID(), limite)) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrió un error al guardar la actualización en la BD.");
						return;
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "En el mapa " + mapa.getID()
					+ " el max de mercantes ha sido modificado a " + limite);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MOBS_FOR_GROUP" :
			case "MAX_MOBS_POR_GRUPO" :
			case "MOBS_POR_GRUPO" :
				try {
					byte limite = 0;
					try {
						limite = Byte.parseByte(infos[1]);
					} catch (final Exception e) {}
					if (limite <= 0) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					mapa.setMaxMobsPorGrupo(limite);
					if (!GestorSQL.UPDATE_MAPA_MAX_MOB_GRUPO(mapa.getID(), limite)) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrió un error al guardar la actualización en la BD.");
						return;
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "En el mapa " + mapa.getID()
					+ " el maximo de mobs por grupo ha sido modificado a " + limite);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "GRUPO_MOBS_POR_MAPA" :
			case "MAX_GROUP_MOBS" :
			case "MAX_GRUPO_MOBS" :
				try {
					byte limite = 0;
					try {
						limite = Byte.parseByte(infos[1]);
					} catch (final Exception e) {}
					if (limite < 0) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					mapa.setMaxGrupoDeMobs(limite);
					if (!GestorSQL.UPDATE_MAPA_MAX_GRUPO_MOBS(mapa.getID(), limite)) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrió un error al guardar la actualización en la BD.");
						return;
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "En el mapa " + mapa.getID()
					+ " el maximo de grupo mobs ha sido modificado a " + limite);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "REBOOT" :
			case "RESET" :
			case "SALIR" :
			case "RESETEAR" :
			case "EXIT" :
				try {
					if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_SALVANDO) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le serveur est en cours de sauvegarde, impossible de reboot.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso,
							"No se puede cerrar, porque el server se esta guardando, intentar en 5 minutos");
						}
						return;
					}
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Reboot maintenant.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se esta cerrando el server");
					}
					new Reiniciar(0);
				} catch (final Exception e) {
					e.printStackTrace();
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "INFO_DROP_MOB_OBJETO" :
			case "DROP_POR_OBJETO_MOB" :
				try {
					int mobID = 0, objModID = 0;
					try {
						mobID = Integer.parseInt(infos[1]);
						objModID = Integer.parseInt(infos[2]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Error con los argumentos");
						return;
					}
					MobModelo mobModelo = Mundo.getMobModelo(mobID);
					ObjetoModelo objModelo = Mundo.getObjetoModelo(objModID);
					if (mobModelo == null || objModelo == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Objeto o mob nulos");
						return;
					}
					for (DropMob drop : mobModelo.getDrops()) {
						if (drop.getIDObjModelo() == objModID) {
							GestorSalida.enviarEnCola(_perso, "Ñd" + drop.getProspeccion() + ";" + (drop.getPorcentaje() * 1000) + ";"
							+ drop.getMaximo(), false);
							break;
						}
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "ADD_DROP" :
			case "AGREGAR_DROP" :
				try {
					int mobID = 0, objModID = 0, prospecc = 0, max = 0;
					float porcentaje = 0;
					String condicion = "";
					try {
						mobID = Integer.parseInt(infos[1]);
						objModID = Integer.parseInt(infos[2]);
						prospecc = Integer.parseInt(infos[3]);
						porcentaje = Float.parseFloat(infos[4]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Error con los argumentos");
						return;
					}
					try {
						max = Integer.parseInt(infos[5]);
					} catch (final Exception e) {}
					try {
						condicion = infos[6];
					} catch (final Exception e) {}
					MobModelo mobModelo = Mundo.getMobModelo(mobID);
					ObjetoModelo objModelo = Mundo.getObjetoModelo(objModID);
					if (mobModelo == null || objModelo == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Objeto o mob nulos");
						return;
					}
					mobModelo.addDrop(new DropMob(objModID, prospecc, porcentaje, max, condicion));
					GestorSQL.INSERT_DROP(mobID, objModID, prospecc, porcentaje, max, mobModelo.getNombre(), objModelo
					.getNombre(), condicion);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se agregó al mob " + mobModelo.getNombre() + " (" + mobModelo
					.getID() + ") el objeto " + objModelo.getNombre() + " (" + objModelo.getID() + ") con PP " + prospecc + ", "
					+ porcentaje + "%, máximo " + max + " y condición " + condicion);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "LIST_DROPS" :
			case "LISTA_DROPS" :
			case "LISTA_DROP" :
				try {
					StringBuilder strB = new StringBuilder();
					int mobID = 0;
					try {
						mobID = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Error con los argumentos");
						return;
					}
					MobModelo mobModelo = Mundo.getMobModelo(mobID);
					if (mobModelo == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Hay valores nulos");
						return;
					}
					for (final DropMob drop : mobModelo.getDrops()) {
						ObjetoModelo objModelo = Mundo.getObjetoModelo(drop.getIDObjModelo());
						if (objModelo == null) {
							continue;
						}
						strB.append(" - " + drop.getIDObjModelo() + " - " + objModelo.getNombre() + "\tProsp: " + drop
						.getProspeccion() + "\tPorcentaje: " + drop.getPorcentaje() + "%\tMax: " + drop.getMaximo() + "\n");
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La listas de drop del mob " + mobModelo.getNombre() + " es: \n"
					+ strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "DEL_DROP" :
			case "ELIMINATE_DROP" :
			case "ERASER_DROP" :
			case "BORRAR_DROP" :
			case "DELETE_DROP" :
				try {
					int mobID = 0, objModID = 0;
					try {
						mobID = Integer.parseInt(infos[1]);
						objModID = Integer.parseInt(infos[2]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Error con los argumentos");
						return;
					}
					ObjetoModelo objModelo = Mundo.getObjetoModelo(objModID);
					MobModelo mobModelo = Mundo.getMobModelo(mobID);
					if (objModelo == null || mobModelo == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Hay valores nulos");
						return;
					}
					mobModelo.borrarDrop(objModID);
					GestorSQL.DELETE_DROP(objModID, mobID);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se borro el objeto " + objModelo.getNombre() + " del drop del mob "
					+ mobModelo.getNombre());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "ADD_END_FIGHT" :
			case "ADD_END_FIGHT_ACTION" :
			case "ADD_ACTION_END_FIGHT" :
			case "ADD_ACCION_FIN_PELEA" :
			case "AGREGAR_ACCION_FIN_PELEA" :
				try {
					infos = mensaje.split(" ", 6);
					int tipo = 0, accionID = 0;
					String args = "";
					String condicion = "";
					String descripcion = "";
					StringBuilder strB = new StringBuilder();
					try {
						tipo = Integer.parseInt(infos[1]);
						accionID = Integer.parseInt(infos[2]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento inválido");
						return;
					}
					if (infos.length > 3) {
						args = infos[3];
					}
					if (infos.length > 4) {
						condicion = infos[4];
					}
					try {
						if (infos.length > 5) {
							descripcion = infos[5];
						}
					} catch (final Exception e) {}
					try {
						if (infos.length > 6) {
							mapa = Mundo.getMapa(Short.parseShort(infos[6]));
						}
					} catch (final Exception e) {}
					mapa.addAccionFinPelea(tipo, new Accion(accionID, args, ""));
					strB.append("Se agregó la accion fin pelea, mapaID: " + mapa.getID() + ", tipoPelea: " + tipo + ", accionID: "
					+ accionID + ", args: " + args + " condicion: " + condicion);
					if (GestorSQL.INSERT_ACCION_FIN_PELEA(mapa.getID(), tipo, accionID, args, condicion, descripcion)) {
						strB.append(" a la BDD");
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "DELETED_ACTION_END_FIGHT" :
			case "ELIMNAR_ACCION_FIN_PELEA" :
			case "DEL_ACTION_END_FIGHT" :
			case "BORRAR_ACCION_FIN_PELEA" :
			case "BORRAR_ACCIONES_FIN_PELEA" :
				mapa.borrarAccionesPelea();
				GestorSQL.DELETE_ACCION_PELEA(mapa.getID());
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se borraron las acciones de pelea");
				break;
			case "ESPECTATOR_FIGHT" :
			case "ESPECTAR_PELEA" :
			case "ESPECTATE_FIGHT" :
			case "ESPECTAR_A" :
			case "JOIN_FIGHT" :
			case "UNIRSE_PELEA" :
				try {
					if (infos.length > 1) {
						objetivo = Mundo.getPersonajePorNombre(infos[1]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					if (objetivo.getPelea() == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no esta en pelea");
						return;
					}
					if (objetivo.getPelea().getFase() < Constantes.PELEA_FASE_COMBATE) {
						objetivo.getPelea().unirsePelea(_perso, objetivo.getID());
					} else {
						objetivo.getPelea().unirseEspectador(_perso, true);
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Te uniste a la pelea de " + infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "SHOW_FIGHTS" :
			case "MOSTRAR_PELEAS" :
				try {
					final StringBuilder packet = new StringBuilder();
					boolean primero = true;
					for (final Pelea pelea : mapa.getPeleas().values()) {
						if (!primero) {
							packet.append("|");
						}
						try {
							final String info = pelea.strParaListaPelea();
							if (!info.isEmpty()) {
								packet.append(info);
								primero = false;
							}
						} catch (final Exception e) {}
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Lista peleas de:\n" + packet.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "ADD_GRUPOMOB_FIJO" :
			case "AGREGAR_GRUPOMOB_FIJO" :
			case "ADD_MOB_FIJO" :
			case "AGREGAR_MOB_FIJO" :
			case "SPAWN_FIX" :
			case "SPAWN_SQL" :
			case "AGREGAR_MOB_GRUPO_SQL" :
			case "AGREGAR_GRUPO_MOB_SQL" :
			case "ADD_GRUPO_MOB_SQL" :
				sql = true;
			case "AGREGAR_MOB_GRUPO" :
			case "AGREGAR_GRUPO_MOB" :
			case "ADD_GRUPO_MOB" :
			case "ADD_GROUP_MOB" :
			case "SPAWN_MOBS" :
			case "SPAWN_GROUP_MOB" :
			case "SPAWN_MOB" :
			case "SPAWN" :
				try {
					String condUnirse = "", condInicio = "", grupoData = "";
					int segundosRespawn = 0, tipoGrupoMob = 0;
					try {
						grupoData = infos[1];
						tipoGrupoMob = Integer.parseInt(infos[2]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects");
						return;
					}
					if (grupoData.isEmpty()) {
						return;
					}
					try {
						condInicio = infos[3].replaceAll("menor", "<");
						condInicio = condInicio.replaceAll("mayor", ">");
						condInicio = condInicio.replaceAll("igual", "=");
						condInicio = condInicio.replaceAll("diferente", "!");
					} catch (final Exception e) {}
					try {
						condUnirse = infos[4].replaceAll("menor", "<");
						condUnirse = condUnirse.replaceAll("mayor", ">");
						condUnirse = condUnirse.replaceAll("igual", "=");
						condUnirse = condUnirse.replaceAll("diferente", "!");
					} catch (final Exception e) {}
					try {
						segundosRespawn = Integer.parseInt(infos[5]);
					} catch (Exception e) {}
					TipoGrupo tipoGrupo = Constantes.getTipoGrupoMob(tipoGrupoMob);
					if (tipoGrupo == TipoGrupo.NORMAL) {
						tipoGrupo = TipoGrupo.FIJO;
					}
					GrupoMob grupoMob = mapa.addGrupoMobPorTipo(_perso.getCelda().getID(), grupoData, tipoGrupo, condInicio,
					null);
					grupoMob.setCondUnirsePelea(condUnirse);
					grupoMob.setSegundosRespawn(segundosRespawn);
					if (sql) {
						GestorSQL.REPLACE_GRUPOMOB_FIJO(mapa.getID(), _perso.getCelda().getID(), grupoData, tipoGrupoMob,
						condInicio, segundosRespawn);
					}
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le groupe monstre a été spawn.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se agrego el grupomob: " + grupoData + " de tipo: " + tipoGrupo
						+ ", condInicio: " + condInicio + ", condUnirse: " + condUnirse + ", tiempoReaparecer: " + segundosRespawn
						+ (sql ? " y guardado en la BD" : ""));
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "REMOVE_MOBS" :
			case "DELETE_MOBS" :
			case "DEL_MOBS" :
			case "ELIMINAR_MOBS" :
			case "BORRAR_MOBS" :
				mapa.borrarTodosMobsNoFijos();
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se borraron todos los mobs normales de este mapa");
				break;
			case "ELIMINAR_MOBS_FIJOS" :
			case "BORRAR_MOBS_FIX" :
			case "BORRAR_MOBS_FIJOS" :
			case "REMOVE_MOBS_FIX" :
			case "DEL_MOBS_FIX" :
			case "DELETE_MOBS_FIX" :
				mapa.borrarTodosMobsFijos();
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se borraron todos los mobs fix de este mapa");
				break;
			case "AGREGAR_NPC" :
			case "ADD_NPC" :
				try {
					int id = 0;
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e5) {}
					if (Mundo.getNPCModelo(id) == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "L'ID du PNJ est invalide.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "NPC ID inválido");
						}
						return;
					}
					NPC npc = mapa.addNPC(Mundo.getNPCModelo(id), _perso.getCelda().getID(), _perso.getOrientacion());
					GestorSalida.ENVIAR_GM_NPC_A_MAPA(mapa, '+', npc.strinGM(null));
					if (GestorSQL.REPLACE_NPC_AL_MAPA(mapa.getID(), _perso.getCelda().getID(), id, _perso.getOrientacion(), npc
					.getModelo().getNombre())) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le PNJ a été ajouté.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El NPC " + npc.getModelo().getNombre() + " ha sido agregado");
						}
					} else {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Une erreur est survenue.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Error al agregar el NPC");
						}
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MOVER_NPC" :
			case "MOVE_NPC" :
				try {
					int id = 0;
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argument incorrect.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						}
						return;
					}
					NPC npc = mapa.getNPC(id);
					if (npc == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "L'ID du PNJ est invalide.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "NPC ID inválido");
						}
						return;
					}
					npc.setOrientacion(_perso.getOrientacion());
					if (GestorSQL.REPLACE_NPC_AL_MAPA(mapa.getID(), _perso.getCelda().getID(), npc.getModelo().getID(), _perso
					.getOrientacion(), npc.getModelo().getNombre())) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El NPC " + npc.getModelo().getNombre() + " ha sido desplazado");
						npc.setCeldaID(_perso.getCelda().getID());
						GestorSalida.ENVIAR_GM_NPC_A_MAPA(mapa, '~', npc.strinGM(null));
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Error al mover el NPC");
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "BORRAR_NPC" :
			case "DEL_NPC" :
				try {
					int id = 0;
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {}
					NPC npc = mapa.getNPC(id);
					if (npc == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "L'ID du PNJ est invalide.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "NPC ID inválido");
						}
						return;
					}
					GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(mapa, id);
					mapa.borrarNPC(id);
					if (GestorSQL.DELETE_NPC_DEL_MAPA(mapa.getID(), npc.getModeloID())) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El NPC fue eliminado correctamente");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No se pudo eliminar el NPC de la BD");
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "BORRAR_TRIGER" :
			case "BORRAR_CELDA_TELEPORT" :
			case "DEL_TRIGGER" :
			case "BORRAR_TRIGGER" :
			case "BORRAR_CELDA_ACCION" :
			case "DEL_CELDA_ACCION" :
				try {
					Short celdaID = -1;
					try {
						celdaID = Short.parseShort(infos[1]);
					} catch (final Exception e) {}
					Celda celda = mapa.getCelda(celdaID);
					if (celda == null) {
						celda = _perso.getCelda();
					}
					GestorSalida.enviarEnCola(_perso, "GDZ|-" + celdaID + ";0;11", false);
					celda.eliminarAcciones();
					final boolean exito = GestorSQL.DELETE_TRIGGER(mapa.getID(), celdaID);
					if (exito) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El trigger de la celda " + celdaID + " ha sido borrado");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El trigger no se puede borrar");
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "ADD_TRIGGER" :
			case "AGREGAR_TRIGER" :
			case "ADD_CELDA_ACCION" :
			case "AGREGAR_CELDA_ACCION" :
			case "AGREGAR_CELDA_TELEPORT" :
			case "AGREGAR_TRIGGER" :
				try {
					int accionID = 0;
					String args = "", condicion = "";
					try {
						accionID = Integer.parseInt(infos[1]);
						args = infos[2];
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					if (accionID <= -3) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "AccionID incorrecta");
						return;
					}
					Celda celda = _perso.getCelda();
					try {
						if (infos.length > 3) {
							mapa = Mundo.getMapa(Short.parseShort(infos[3].split(",")[0]));
							celda = mapa.getCelda(Short.parseShort(infos[3].split(",")[1]));
						}
					} catch (Exception e) {
						mapa = _perso.getMapa();
						celda = _perso.getCelda();
					}
					try {
						if (infos.length > 4) {
							condicion = infos[4];
						}
					} catch (Exception e) {}
					if (GestorSQL.REPLACE_CELDAS_ACCION(mapa.getID(), celda.getID(), accionID, args, condicion)) {
						if (mapa.getID() == _perso.getMapa().getID()) {
							GestorSalida.enviarEnCola(_perso, "GDZ|+" + celda.getID() + ";0;11", false);// color
																																													// azul
						}
						celda.addAccion(accionID, args, condicion);
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mapa: " + mapa.getID() + ", celda: " + celda.getID()
						+ ", le ha sido agregado la acción: " + accionID + ", args: " + args + ", y condición (4to arg): "
						+ condicion);
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El trigger no se puede agregar");
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "LIST_TRIGGERS" :
			case "LISTA_CELDAS_ACCION" :
			case "LISTA_TRIGGERS" :
				try {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Triggers del mapa " + mapa.getID());
					for (Celda celda : mapa.getCeldas().values()) {
						if (celda.accionesIsEmpty()) {
							continue;
						}
						for (Accion a : celda.getAcciones().values()) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "\tCeldaID: " + celda.getID() + " AccionID: " + a.getID()
							+ " Args: " + a.getArgs() + " Condicion: " + a.getCondicion());
						}
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "MOSTRAR_TRIGGERS" :
			case "SHOW_TRIGGERS" :
			case "SHOW_CELLS_ACTION" :
				mapa.panelTriggers(_perso, true);
				break;
			case "OCULTAR_TRIGGERS" :
			case "HIDE_TRIGGERS" :
			case "ESCONDER_TRIGGERS" :
				mapa.panelTriggers(_perso, false);
				break;
			case "ADD_ACCION_OBJETO" :
			case "ADD_ACTION_ITEM" :
			case "AGREGAR_ACCION_OBJETO" :
			case "ADD_ITEM_ACTION" :
			case "ADD_OBJETO_ACCION" :
			case "AGREGAR_OBJETO_ACCION" :
				try {
					int id = 0, accionID = 0;
					String args = "";
					try {
						id = Integer.parseInt(infos[1]);
						accionID = Integer.parseInt(infos[2]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Error con los argumentos");
						return;
					}
					try {
						args = infos[3];
					} catch (final Exception e) {}
					ObjetoModelo objModelo = Mundo.getObjetoModelo(id);
					if (objModelo == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Algun valor inválido");
						return;
					}
					objModelo.addAccion(new Accion(accionID, args, ""));
					GestorSQL.REPLACE_ACCION_OBJETO(id, accionID, args, objModelo.getNombre());
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El objeto " + objModelo.getNombre()
					+ " se le ha agregado la accionID " + accionID + " con args " + args);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "DEL_ACTION_ITEM" :
			case "BORRAR_ACCION_OBJETO" :
			case "BORRAR_ACCIONES_OBJETO" :
			case "BORRAR_OBJETO_ACCIONES" :
			case "BORRAR_OBJETO_ACCION" :
			case "DELETE_ITEM_ACTIONS" :
			case "DELETE_ACTION_ITEM" :
				try {
					int id = 0;
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Error con los argumentos");
						return;
					}
					ObjetoModelo objModelo = Mundo.getObjetoModelo(id);
					if (objModelo == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Objeto modelo inválido");
						return;
					}
					objModelo.borrarAcciones();
					GestorSQL.DELETE_ACCION_OBJETO(id);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El objeto " + objModelo.getNombre() + " borro todas sus acciones");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "MESSAGE_WELCOME" :
			case "MENSAJE_BIENVENIDA" :
				try {
					String str = "";
					try {
						str = mensaje.split(" ", 2)[1];
					} catch (final Exception e) {}
					MainServidor.MENSAJE_BIENVENIDA = str;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mensaje de bienvenida es :\n" + str);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "PANEL_BIENVENIDA" :
				try {
					String str = "";
					try {
						str = mensaje.split(" ", 2)[1];
					} catch (final Exception e) {}
					MainServidor.PANEL_BIENVENIDA = str;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El panel bievenida dice :\n" + str);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "PANEL_DESPUES_CREAR_PJ" :
			case "PANEL_CREAR_PJ" :
				try {
					String str = "";
					try {
						str = mensaje.split(" ", 2)[1];
					} catch (final Exception e) {}
					MainServidor.PANEL_DESPUES_CREAR_PERSONAJE = str;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El panel crear pj dice :\n" + str);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MENSAJE_COMANDOS" :
				try {
					String str = "";
					try {
						str = mensaje.split(" ", 2)[1];
					} catch (final Exception e) {}
					MainServidor.MENSAJE_COMANDOS = str;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El nuevo mensaje de comandos es :\n" + str);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "TUTORIAL_ES" :
				try {
					String str = "";
					try {
						str = mensaje.split(" ", 2)[1];
					} catch (final Exception e) {}
					MainServidor.TUTORIAL_ES = str;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mensaje de tutorial_es es :\n" + str);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "TUTORIAL_FR" :
				try {
					String str = "";
					try {
						str = mensaje.split(" ", 2)[1];
					} catch (final Exception e) {}
					MainServidor.TUTORIAL_FR = str;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mensaje de tutorial_fr es :\n" + str);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "ADD_CELDA_CERCADO" :
			case "ADD_CELL_MOUNTPARK" :
			case "CELDA_OBJETO" :
				try {
					short celdaID = 0;
					if (mapa.getCercado() == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Este mapa no tiene cercado");
						return;
					}
					try {
						celdaID = Short.parseShort(infos[1]);
					} catch (final Exception e) {
						celdaID = _perso.getCelda().getID();
					}
					mapa.getCercado().addCeldaObj(celdaID);
					GestorSalida.enviarEnCola(_perso, "GDZ|+" + celdaID + ";0;5", false);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "CELDAS_CERCADO" :
				try {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Tiene las celdas: " + mapa.getCercado().getStringCeldasObj());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Este mapa no tiene cercado");
				}
				break;
			case "MUTE_CANAL_ALINEACION" :
				try {
					boolean a = false;
					try {
						a = infos[1].equalsIgnoreCase("true");
					} catch (final Exception e) {}
					MainServidor.MUTE_CANAL_ALINEACION = a;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Canal alineacion: " + !MainServidor.MUTE_CANAL_COMERCIO);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MUTE_CANAL_INCARNAM" :
			case "MUTE_CANAL_ALL" :
				try {
					boolean a = false;
					try {
						a = infos[1].equalsIgnoreCase("true");
					} catch (final Exception e) {}
					MainServidor.MUTE_CANAL_INCARNAM = a;
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Canal incarnam: " + !MainServidor.MUTE_CANAL_COMERCIO);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MUTE_CANAL_COMERCIO" :
			case "MUTE_CANAL_COMMERCE" :
				try {
					boolean a = false;
					try {
						a = infos[1].equalsIgnoreCase("true");
					} catch (final Exception e) {}
					MainServidor.MUTE_CANAL_COMERCIO = a;
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Canal commerce: " + !MainServidor.MUTE_CANAL_COMERCIO);
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Canal comercio: " + !MainServidor.MUTE_CANAL_COMERCIO);
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "MUTE_CANAL_RECRUTEMENT" :
			case "MUTE_CANAL_RECLUTAMIENTO" :
				try {
					boolean b = false;
					try {
						b = infos[1].equalsIgnoreCase("true");
					} catch (final Exception e) {}
					MainServidor.MUTE_CANAL_RECLUTAMIENTO = b;
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Canal recrutement: " + !MainServidor.MUTE_CANAL_RECLUTAMIENTO);
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Canal reclutamiento: " + !MainServidor.MUTE_CANAL_RECLUTAMIENTO);
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			default :
				GM_lvl_2(comando, infos, mensaje, _cuenta, _perso);
				return;
		}
		// if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
		// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Commande GM 3!.");
		// } else {
		// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Comando de nivel 3");
		// }
	}
	
	public static void GM_lvl_4(final String comando, String[] infos, final String mensaje, final Cuenta _cuenta,
	final Personaje _perso) {
		int npcID = 0, id = -1, cantInt = 0, tipo = 0;
		byte cantByte = 0;
		short cantShort = 0;
		long cantLong = 0;
		float cantFloat = 0;
		ObjetoModelo objModelo;
		MobModelo mobModelo;
		NPCModelo npcModelo;
		String str = "", intercambiable = "";
		final StringBuilder strB = new StringBuilder();
		Personaje objetivo = null;
		Hechizo hechizo;// *0287014
		switch (comando.toUpperCase()) {
			case "SET_SPELLS_MOB" :
			case "SET_HECHIZOS_MOB" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (Exception e) {}
				mobModelo = Mundo.getMobModelo(id);
				if (mobModelo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mob ID " + id + " no existe");
					return;
				}
				try {
					cantByte = Byte.parseByte(infos[2]);
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ingresa un valor correcto para GRADO del mob");
					return;
				}
				MobGradoModelo mobGradoModelo = mobModelo.getGradoPorGrado(cantByte);
				if (mobGradoModelo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mob ID " + id + " con Grado " + cantByte + " no existe");
					return;
				}
				try {
					str = infos[3];
				} catch (Exception e) {}
				mobGradoModelo.setHechizos(str);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mob " + mobModelo.getNombre() + " (" + id + ") con Grado "
				+ cantByte + " a modificado sus hechizos a " + str);
				break;
			case "ADD_ALMANAX" :
			case "AGREGAR_MISION_DIARIA" :
			case "MISION_ALMANAX" :
			case "UPDATE_ALMANAX" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (Exception e) {}
				if (id == -1) {
					id = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
				}
				if (id < 1 || id > 366) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Dia Incorrecto");
					return;
				}
				try {
					str = infos[2];
					tipo = Integer.parseInt(infos[3]);
					cantInt = Integer.parseInt(infos[4]);
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
					return;
				}
				GestorSQL.UPDATE_ALMANAX(id, str, tipo, cantInt);
				Mundo.addAlmanax(new Almanax(id, tipo, cantInt, str));
				String bonus = "EXP PJ";
				switch (tipo) {
					case 1 :
						bonus = "EXP PJ";
						break;
					case 2 :
						bonus = "KAMAS";
						break;
					case 3 :
						bonus = "DROP";
						break;
					case 4 :
						bonus = "EXP CRAFT";
						break;
					case 5 :
						bonus = "EXP RECOLECCION";
						break;
					case 6 :
						bonus = "DROP RECOLECCION";
						break;
					case 7 :
						bonus = "BONUS HONOR";
						break;
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se actualizó el día almanax " + id + ", con ofrenda " + str
				+ ", tipoBonus " + bonus + " y bonus %" + cantInt);
				break;
			case "ADD_MOB_CARD" :
			case "ADD_CARD_MOB" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				if (!objetivo.enLinea()) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'est pas connecté.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no esta conectado");
					}
					return;
				}
				if (Mundo.getMobModelo(id) == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le monstre n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mob no existe");
					}
					return;
				}
				objetivo.addCardMob(id);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre()
					+ " agrego asu lista de cardMobs la tarjeta N°" + id);
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre()
					+ " agrego asu lista de cardMobs la tarjeta N°" + id + " (" + Mundo.getMobModelo(id).getNombre() + ")");
				}
				break;
			case "PRECIO_SISTEMA_RECURSO" :
			case "PRECIO_RECURSO" :
				try {
					cantFloat = Float.parseFloat(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
					return;
				}
				MainServidor.PRECIO_SISTEMA_RECURSO = cantFloat;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El precio de recurso se cambio a " + cantFloat);
				break;
			case "CARGAR_MAPAS_IDS" :
			case "LOAD_MAPS_IDS" :
			case "CARGAR_MAPAS" :
			case "MAPPEAR_IDS" :
				try {
					GestorSQL.CARGAR_MAPAS_IDS(infos[1]);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se integro los mapas " + infos[1]);
				} catch (final Exception e1) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				break;
			case "CARGAR_SUBAREAS" :
			case "LOAD_SUBAREAS" :
			case "MAPPEAR_SUBAREAS" :
				try {
					GestorSQL.CARGAR_MAPAS_SUBAREAS(infos[1]);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se integro las subareas " + infos[1]);
				} catch (final Exception e1) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				break;
			case "ADD_ENERGIA" :
			case "AGREGAR_ENERGIA" :
			case "ENERGIA" :
			case "ENERGY" :
			case "SET_ENERGY" :
				if (infos.length <= 1) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argument incorrect.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				cantInt = Integer.parseInt(infos[1]);
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
				}
				objetivo.addEnergiaConIm(cantInt, true);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "L'énergie de " + objetivo.getNombre() + " a été modifiée en "
					+ objetivo.getEnergia());
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ha sido modificado la energía de " + objetivo.getNombre() + " a "
					+ objetivo.getEnergia());
				}
				break;
			case "ADD_TITULO" :
			case "SET_TITLE" :
			case "ADD_TITLE" :
			case "TITULO" :
			case "TITRE" :
			case "TITLE" :
				int titulo = 0;
				int color = -1;
				try {
					titulo = Byte.parseByte(infos[1]);
					color = Integer.parseInt(infos[2]);
				} catch (final Exception e) {}
				objetivo = _perso;
				if (infos.length > 3) {
					objetivo = Mundo.getPersonajePorNombre(infos[3]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				objetivo.addTitulo(titulo, color);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " possède désormais le titre "
					+ titulo);
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre() + " adquirió el título "
					+ titulo);
				}
				if (objetivo.getPelea() == null) {
					objetivo.refrescarEnMapa();
				}
				break;
			case "ORNAMENTO" :
			case "ORNEMENT" :
				try {
					cantByte = Byte.parseByte(infos[1]);
				} catch (final Exception e) {}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				objetivo.addOrnamento(cantByte);
				objetivo.setOrnamento(cantByte);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre()
					+ " possède désormais l'ornement " + cantByte);
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre() + " adquirió el ornamento "
					+ cantByte);
				}
				if (objetivo.getPelea() == null) {
					objetivo.refrescarEnMapa();
				}
				break;
			case "TITULO_VIP" :
			case "TITRE_VIP" :
				try {
					infos = mensaje.split(" ", 2);
					objetivo = _perso;
					if (infos.length > 1) {
						objetivo = Mundo.getPersonajePorNombre(infos[1]);
					}
					str = infos[2];
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					objetivo.setTituloVIP(str);
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre()
						+ " possède désormais le titre VIP " + str);
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre()
						+ " a adquirido el titulo VIP de " + str);
					}
					if (objetivo.getPelea() == null) {
						objetivo.refrescarEnMapa();
					}
				} catch (Exception e) {}
				break;
			case "SET_STATS_OBJETO_SET" :
			case "SET_STATS_SET_OBJETO" :
			case "SET_STATS_SET_ITEM" :
			case "SET_STATS_ITEM_SET" :
			case "SET_BONUS_OBJETO_SET" :
			case "SET_BONUS_SET_OBJETO" :
			case "SET_BONUS_SET_ITEM" :
			case "SET_BONUS_ITEM_SET" :
				try {
					id = Integer.parseInt(infos[1]);
					str = infos[2];
					cantInt = Integer.parseInt(infos[3]);
				} catch (final Exception e) {}
				final ObjetoSet set = Mundo.getObjetoSet(id);
				if (set == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El objeto set " + id + " no existe");
					return;
				}
				set.setStats(str, cantInt);
				GestorSQL.UPDATE_STATS_OBJETO_SET(id, str);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El objeto set " + id + " (" + set.getNombre()
				+ ") cambio su bonus de " + cantInt + " objetos a: " + str);
				break;
			case "SET_OBJETO" :
			case "SET_ITEM" :
			case "OBJETO_SET" :
			case "ITEM_SET" :
				try {
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {}
					final ObjetoSet OS = Mundo.getObjetoSet(id);
					if (OS == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El objeto set " + id + " no existe");
						return;
					}
					objetivo = _perso;
					if (infos.length > 2) {
						objetivo = Mundo.getPersonajePorNombre(infos[2]);
					}
					if (MainServidor.PARAM_NOMBRE_COMPRADOR) {
						if (_cuenta.getAdmin() < 5) {
							objetivo = _perso;
						}
						intercambiable = (ObjetoModelo.stringFechaIntercambiable(3650));
					}
					CAPACIDAD_STATS useMax = CAPACIDAD_STATS.RANDOM;
					if (infos.length > 3) {
						useMax = infos[3].equalsIgnoreCase("MAX")
						? CAPACIDAD_STATS.MAXIMO
						: (infos[3].equalsIgnoreCase("MIN") ? CAPACIDAD_STATS.MINIMO : CAPACIDAD_STATS.RANDOM);
					}
					for (final ObjetoModelo OM : OS.getObjetosModelos()) {
						Objeto obj = OM.crearObjeto(1, Constantes.OBJETO_POS_NO_EQUIPADO, useMax);
						if (MainServidor.PARAM_NOMBRE_COMPRADOR) {
							obj.addStatTexto(Constantes.STAT_INTERCAMBIABLE_DESDE, intercambiable);
							obj.addStatTexto(Constantes.STAT_PERTENECE_Y_NO_VENDER, "0#0#0#" + _perso.getNombre());
						}
						objetivo.addObjIdentAInventario(obj, false);
					}
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						strB.append("Création de la panoplie " + OS.getNombre() + " pour " + objetivo.getNombre());
					} else {
						strB.append("Creación del objeto set " + OS.getNombre() + " a " + objetivo.getNombre());
					}
					switch (useMax) {
						case MAXIMO :
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								strB.append(" avec des jets parfaits.");
							} else {
								strB.append(" con stats máximos");
							}
							break;
						case MINIMO :
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								strB.append(" avec des jets minimuns.");
							} else {
								strB.append(" con stats mínimos");
							}
							break;
						default :
							break;
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, strB.toString());
					GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(objetivo);
				} catch (Exception e) {}
				break;
			case "DEL_NPC_ITEM" :
			case "DEL_ITEM_NPC" :
			case "BORRAR_NPC_ITEM" :
			case "BORRAR_ITEM_NPC" :
			case "BORRAR_OBJETO_NPC" :
			case "BORRAR_NPC_OBJETO" :
				try {
					try {
						npcID = Integer.parseInt(infos[1]);
						npcModelo = Mundo.getNPCModelo(npcID);
						npcModelo.getID();
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "NPC inválido");
						return;
					}
					final ArrayList<ObjetoModelo> objetos = new ArrayList<ObjetoModelo>();
					for (final String a : infos[2].split(",")) {
						try {
							objModelo = Mundo.getObjetoModelo(Integer.parseInt(a));
							if (objModelo != null) {
								objetos.add(objModelo);
								strB.append("\n" + objModelo.getNombre());
							}
						} catch (final Exception e) {}
					}
					npcModelo.borrarObjetoAVender(objetos);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Al NPC " + npcModelo.getNombre()
					+ " se le borró los siguientes objetos:" + strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos inválidos");
					return;
				}
				break;
			case "ADD_ITEM_NPC" :
			case "ADD_OBJETO_NPC" :
			case "ADD_NPC_ITEM" :
			case "ADD_NPC_OBJETO" :
			case "AGREGAR_NPC_OBJETO" :
			case "AGREGAR_OBJETO_NPC" :
			case "AGREGAR_NPC_ITEM" :
			case "AGREGAR_ITEM_NPC" :
				try {
					try {
						npcID = Integer.parseInt(infos[1]);
						npcModelo = Mundo.getNPCModelo(npcID);
						npcModelo.getID();
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "NPC inválido");
						return;
					}
					final ArrayList<ObjetoModelo> objetos = new ArrayList<ObjetoModelo>();
					for (final String a : infos[2].split(",")) {
						try {
							objModelo = Mundo.getObjetoModelo(Integer.parseInt(a));
							if (objModelo != null) {
								objetos.add(objModelo);
								strB.append("\n" + objModelo.getNombre());
							}
						} catch (final Exception e) {}
					}
					npcModelo.addObjetoAVender(objetos);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Al NPC " + npcModelo.getNombre()
					+ " se le agregó los siguientes objetos:" + strB.toString());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos inválidos");
					return;
				}
				break;
			case "BORRAR_OBJETOS_NPC" :
			case "BORRAR_NPC_TODOS_OBJETOS" :
				try {
					npcID = Integer.parseInt(infos[1]);
					npcModelo = Mundo.getNPCModelo(npcID);
					npcModelo.borrarTodosObjVender();
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se borraron todos los objetos del NPC " + npcModelo.getNombre());
				} catch (final Exception ex) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos inválido");
					return;
				}
				break;
			case "HONOUR" :
			case "HONOR" :
				int honor = 0;
				try {
					honor = Integer.parseInt(infos[1]);
				} catch (final Exception e6) {}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
				}
				if (objetivo.getAlineacion() == Constantes.ALINEACION_NEUTRAL || objetivo
				.getAlineacion() == Constantes.ALINEACION_NULL) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur est neutre.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje es neutral");
					}
					return;
				}
				objetivo.addHonor(honor);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, honor + " points d'honneur ont été ajoutés à " + objetivo
					.getNombre());
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ha sido agregado " + honor + " honor a " + objetivo.getNombre());
				}
				break;
			case "MAPA_PARAMETROS" :
			case "PARAMETERS_MAPA" :
			case "MAPA_DESCRIPCION" :
			case "DESCRIPCION_MAPA" :
				try {
					cantShort = Byte.parseByte(infos[1]);
				} catch (final Exception e) {}
				_perso.getMapa().setParametros(cantShort);
				GestorSQL.UPDATE_MAPA_PARAMETROS(_perso.getMapa().getID(), cantShort);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Los parametros del mapa cambió a " + cantShort);
				break;
			case "REGALO" :
			case "GIFT" :
				try {
					str = infos[1];
				} catch (final Exception e) {}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
				}
				objetivo.getCuenta().addRegalo(str);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le cadeau été envoyé.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se entregó el regalo " + str + " a " + objetivo.getNombre());
				}
				break;
			case "REGALO_PARA_ONLINE" :
			case "REGALO_ONLINE" :
			case "GIFT_ONLINE" :
				try {
					str = infos[1];
				} catch (final Exception e) {}
				for (final Personaje pj : Mundo.getPersonajesEnLinea()) {
					try {
						pj.getCuenta().addRegalo(str);
					} catch (final Exception e) {}
				}
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le cadeau a été envoyé à tous les joueurs en ligne.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se entregó el regalo " + str + " a todos los jugadores en línea");
				}
				break;
			case "REGALO_PARA_TODOS" :
			case "GIFT_FOR_ALL" :
				try {
					str = infos[1];
				} catch (final Exception e) {}
				for (Cuenta cuenta : Mundo.getCuentas().values()) {
					cuenta.addRegalo(str);
				}
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le cadeau a été envoyé à tous les joueurs database.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se entregó el regalo " + str
					+ " a todos los jugadores de la database");
				}
				break;
			case "OBJETO_PARA_PLAYERS_ONLINE" :
			case "OBJETO_PARA_JUGADORES_ONLINE" :
			case "ITEM_PLAYERS_ONLINE" :
			case "ITEM_FOR_ONLINE" :
			case "OBJETO_PARA_ONLINE" :
			case "GIVE_ITEM_TO_ONLINE" :
				if (_cuenta.getAdmin() < 5) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No puedes usar este comando");
					return;
				}
				try {
					id = Integer.parseInt(infos[1]);
					cantInt = Integer.parseInt(infos[2]);
				} catch (final Exception e13) {}
				objModelo = Mundo.getObjetoModelo(id);
				if (objModelo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "L'objet n'existe pas.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Objeto modelo nulo");
					}
					return;
				}
				if (cantInt < 1) {
					cantInt = 1;
				}
				if (MainServidor.PARAM_NOMBRE_COMPRADOR) {
					intercambiable = (ObjetoModelo.stringFechaIntercambiable(3650));
				}
				for (final Personaje pj : Mundo.getPersonajesEnLinea()) {
					Objeto obj = objModelo.crearObjeto(cantInt, Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM);
					if (MainServidor.PARAM_NOMBRE_COMPRADOR) {
						obj.addStatTexto(Constantes.STAT_INTERCAMBIABLE_DESDE, intercambiable);
						obj.addStatTexto(Constantes.STAT_PERTENECE_Y_NO_VENDER, "0#0#0#" + _perso.getNombre());
					}
					pj.addObjIdentAInventario(obj, false);
				}
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "L'objet " + objModelo.getNombre() + " avec quant " + cantInt
					+ " a été envoyé à tous les joueurs en ligne");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se entregó el objeto " + objModelo.getNombre() + " con cantidad "
					+ cantInt + " a todos los jugadores en línea");
				}
				break;
			case "ADD_EXP_OFICIO" :
			case "ADD_XP_OFICIO" :
			case "EXP_OFICIO" :
			case "ADD_JOB_XP" :
				if (infos.length <= 2) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argument incorrect.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				int oficio = -1, exp = -1;
				try {
					oficio = Integer.parseInt(infos[1]);
					exp = Integer.parseInt(infos[2]);
				} catch (final Exception e) {}
				if (oficio < 0 || exp < 0) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argument incorrect.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				objetivo = _perso;
				if (infos.length > 3) {
					objetivo = Mundo.getPersonajePorNombre(infos[3]);
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
				}
				final StatOficio statsOficio = objetivo.getStatOficioPorID(oficio);
				if (statsOficio == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'exerce pas ce métier.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no conoce el oficio");
					}
					return;
				}
				statsOficio.addExperiencia(objetivo, exp, 0);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le métier du joueur a gagné de l'expérience.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El oficio ha subido de experiencia");
				}
				break;
			case "PUNTOS_HECHIZO" :
			case "SPELL_POINTS" :
				int pts = -1;
				try {
					if (infos.length > 1) {
						pts = Integer.parseInt(infos[1]);
					}
				} catch (final Exception e) {}
				if (pts < 0) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argument incorrect.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				objetivo.addPuntosHechizos(pts);
				GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(objetivo);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " a reçu " + pts
					+ " points de sort");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre() + " se le ha aumentado " + pts
					+ " puntos de hechizo");
				}
				break;
			case "FORGET_SPELL" :
			case "OLVIDAR_HECHIZO" :
				try {
					if (infos.length > 1)
						id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {}
				if (id < 0) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argument incorrect.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				hechizo = Mundo.getHechizo(id);
				if (hechizo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le sort n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El hechizo no existe");
					}
					return;
				}
				objetivo.olvidarHechizo(id, true, true);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " a oublier le sort " + hechizo
					.getNombre());
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre() + " ha olvidado el hechizo "
					+ hechizo.getNombre());
				}
				break;
			case "APRENDER_HECHIZO" :
			case "LEARN_SPELL" :
				try {
					if (infos.length > 1)
						id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {}
				if (id < 0) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argument incorrect.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				hechizo = Mundo.getHechizo(id);
				if (hechizo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le sort n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El hechizo no existe");
					}
					return;
				}
				objetivo.fijarNivelHechizoOAprender(id, 1, true);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " a appris le sort " + id
					+ " (" + hechizo.getNombre() + ")");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre() + " aprendio el hechizo " + id
					+ " (" + hechizo.getNombre() + ")");
				}
				break;
			case "ADD_XP_MONTURA" :
			case "ADD_EXP_MONTURA" :
			case "AGREGAR_EXP_MONTURA" :
				try {
					Montura montura = _perso.getMontura();
					try {
						if (infos.length > 1) {
							id = Integer.parseInt(infos[1]);
							if (id > 0) {
								id = -id;
							}
							if (id != 0)
								montura = Mundo.getMontura(id);
						}
					} catch (final Exception e) {}
					if (montura == null) {
						return;
					}
					montura.addExperiencia(Integer.parseInt(infos[2]));
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La montura ID " + montura.getID() + " a recibido " + Integer
					.parseInt(infos[2]) + " puntos de exp");
				} catch (Exception e) {}
				break;
			case "FECUNDADA_HACE" :
				try {
					Montura montura = _perso.getMontura();
					try {
						if (infos.length > 1) {
							id = Integer.parseInt(infos[1]);
							if (id > 0) {
								id = -id;
							}
							if (id != 0)
								montura = Mundo.getMontura(id);
						}
					} catch (final Exception e) {}
					if (montura == null) {
						return;
					}
					montura.setFecundada(100);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La montura ID " + montura.getID() + " esta lista para parir");
				} catch (Exception e) {}
				break;
			case "MONTABLE" :
			case "MONTAR" :
				try {
					Montura montura = _perso.getMontura();
					try {
						if (infos.length > 1) {
							id = Integer.parseInt(infos[1]);
							if (id > 0) {
								id = -id;
							}
							if (id != 0)
								montura = Mundo.getMontura(id);
						}
					} catch (final Exception e) {}
					if (montura == null) {
						return;
					}
					montura.setSalvaje(false);
					montura.setMaxEnergia();
					montura.setMaxMadurez();
					montura.setFatiga(0);
					long restante = Mundo.getExpMontura(5) - montura.getExp();
					if (restante > 0) {
						montura.addExperiencia(restante);
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La montura ID " + montura.getID() + " ahora es montable");
				} catch (Exception e) {}
				break;
			case "FECUNDABLE" :
			case "FECUNDAR" :
				try {
					Montura montura = _perso.getMontura();
					try {
						if (infos.length > 1) {
							id = Integer.parseInt(infos[1]);
							if (id > 0) {
								id = -id;
							}
							if (id != 0)
								montura = Mundo.getMontura(id);
						}
					} catch (final Exception e) {}
					if (montura == null) {
						return;
					}
					// montura.setSalvaje(false);
					montura.setAmor(7500);
					montura.setResistencia(7500);
					montura.setMaxEnergia();
					montura.setMaxMadurez();
					long restante = Mundo.getExpMontura(5) - montura.getExp();
					if (restante > 0) {
						montura.addExperiencia(restante);
					}
					if (montura.esCastrado()) {
						montura.quitarCastrado();
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La montura ID " + montura.getID() + " ahora esta fecundo");
				} catch (Exception e) {}
				break;
			case "STATS_POINTS" :
			case "POINTS_CAPITAL" :
			case "PUNTOS_STATS" :
			case "PUNTOS_CAPITAL" :
			case "CAPITAL" :
				int puntos = -1;
				try {
					if (infos.length > 1)
						puntos = Integer.parseInt(infos[1]);
				} catch (final Exception e) {}
				if (puntos < 0) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argument incorrect.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				objetivo.addPuntosStats(puntos);
				GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(objetivo);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " a reçu " + puntos
					+ " points de capital");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre() + " se le ha aumentado "
					+ puntos + " puntos de capital");
				}
				break;
			case "KAMAS" :
				try {
					if (infos.length > 1) {
						cantLong = Long.parseLong(infos[1]);
					}
				} catch (final Exception e) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argument incorrect.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					}
					return;
				}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				objetivo.addKamas(cantLong, true, true);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Tu as " + (cantLong < 0 ? "retiré" : "ajouté") + " " + Math.abs(
					cantLong) + " kamas a " + objetivo.getNombre());
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ha sido " + (cantLong < 0 ? "retirado" : "agregado") + " " + Math
					.abs(cantLong) + " kamas a " + objetivo.getNombre());
				}
				break;
			case "REINICIAR_EN" :
			case "RESET_IN" :
			case "RESET_EN" :
			case "REBOOT_IN" :
			case "REBOOT_EN" :
			case "REBOOTEN" :
			case "RESETEN" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto (minutos)");
					return;
				}
				if (id < 0) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto valor positivo");
					return;
				}
				String msj = MainServidor.MENSAJE_TIMER_REBOOT.isEmpty() ? "REBOOT" : MainServidor.MENSAJE_TIMER_REBOOT;
				try {
					infos = mensaje.split(" ", 3);
					msj = infos[2];
				} catch (final Exception e) {}
				int segundos = (id * 60);
				Mundo.SEG_CUENTA_REGRESIVA = segundos;
				if (id == 0) {
					MainServidor.SEGUNDOS_REBOOT_SERVER = 0;
					Mundo.MSJ_CUENTA_REGRESIVA = "";
					GestorSalida.ENVIAR_bRS_PARAR_CUENTA_REGRESIVA_TODOS();
				} else {
					MainServidor.SEGUNDOS_REBOOT_SERVER = segundos;
					Mundo.MSJ_CUENTA_REGRESIVA = msj;
					GestorSalida.ENVIAR_bRI_INICIAR_CUENTA_REGRESIVA_TODOS();
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se lanzo el temporizador rebooot por " + id + " minutos");
				break;
			case "RESET_RATES" :
				if (!Mundo.MSJ_CUENTA_REGRESIVA.isEmpty()) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Espera que se termine el otro evento.");
					return;
				}
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto (minutos)");
					return;
				}
				if (id < 0) {
					id = 0;
				}
				int segundosRates = (id * 60);
				MainServidor.SEGUNDOS_RESET_RATES = segundosRates + ServidorServer.getSegundosON();
				Mundo.SEG_CUENTA_REGRESIVA = segundosRates;
				Mundo.MSJ_CUENTA_REGRESIVA = "RESET RATES";
				GestorSalida.ENVIAR_bRI_INICIAR_CUENTA_REGRESIVA_TODOS();
				GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1RESET_RATES;" + id);
				break;
			case "CUENTA_REGRESIVA" :
				if (Mundo.MSJ_CUENTA_REGRESIVA.equalsIgnoreCase("RESET RATES")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Espera que se termine el evento de super rates.");
					return;
				}
				infos = mensaje.split(" ", 3);
				try {
					id = Integer.parseInt(infos[1]);
					str = infos[2];
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				Mundo.SEG_CUENTA_REGRESIVA = id;
				Mundo.MSJ_CUENTA_REGRESIVA = str;
				if (str.equalsIgnoreCase("LOTERIA")) {
					Mundo.VENDER_BOLETOS = true;
				} else if (str.equalsIgnoreCase("CACERIA")) {
					final Personaje victima = Mundo.getPersonajePorNombre(Mundo.NOMBRE_CACERIA);
					if (victima != null && !victima.enLinea()) {
						final String geo = victima.getMapa().getX() + "|" + victima.getMapa().getY();
						final String rec = Mundo.mensajeCaceria();
						try {
							for (final Personaje perso : Mundo.getPersonajesEnLinea()) {
								GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_PERSONAJE(perso, "", 0, Mundo.NOMBRE_CACERIA, "INICIA CACERIA - "
								+ rec + " - USA COMANDO .caceria");
								GestorSalida.ENVIAR_IC_PERSONAJE_BANDERA_COMPAS(perso, geo);
							}
						} catch (final Exception e) {}
					}
				}
				GestorSalida.ENVIAR_ÑL_BOTON_LOTERIA_TODOS(Mundo.VENDER_BOLETOS);
				GestorSalida.ENVIAR_bRI_INICIAR_CUENTA_REGRESIVA_TODOS();
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se inicio la cuenta regresiva con mensaje " + str + " y tiempo " + id
				+ " segundos");
				break;
			case "BOLETO_DE" :
				try {
					try {
						id = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					objetivo = Mundo.getPersonaje(Mundo.LOTERIA_BOLETOS[id]);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El dueño del boleto Nº " + id + " es el jugador " + objetivo
					.getNombre());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio un error");
					return;
				}
				break;
			case "ADIC_PJ" :
			case "MULTIPLICADOR_DAÑO_PJ" :
				try {
					cantFloat = Float.parseFloat(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				EfectoHechizo.MULTIPLICADOR_DAÑO_PJ = cantFloat;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El multiplicador daño personaje ha sido cambiado a " + cantFloat);
				break;
			case "ADIC_MOB" :
			case "MULTIPLICADOR_DAÑO_MOB" :
				try {
					cantFloat = Float.parseFloat(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				EfectoHechizo.MULTIPLICADOR_DAÑO_MOB = cantFloat;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El multiplicador de daño mob ha sido cambiado a " + cantFloat);
				break;
			case "ADIC_CAC" :
			case "MULTIPLICADOR_DAÑO_CAC" :
				try {
					cantFloat = Float.parseFloat(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				EfectoHechizo.MULTIPLICADOR_DAÑO_CAC = cantFloat;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El multiplicador de daño CaC ha sido cambiado a " + cantFloat);
				break;
			// case "TOLERANCIA_VIP" :
			// try {
			// cantFloat = Float.parseFloat(infos[1]);
			// } catch (final Exception e) {
			// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
			// return;
			// }
			// Trabajo.TOLERANCIA_VIP = cantFloat;
			// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La tolerancia VIP de magueo ha sido cambiado a "
			// + cantFloat);
			// break;
			// case "TOLERANCIA_NORMAL" :
			// try {
			// cantFloat = Float.parseFloat(infos[1]);
			// } catch (final Exception e) {
			// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
			// return;
			// }
			// Trabajo.TOLERANCIA_NORMAL = cantFloat;
			// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso,
			// "La tolerancia Normal de magueo ha sido cambiado a " + cantFloat);
			// break;
			case "SET_IA" :
			case "SET_IA_MOB" :
			case "SET_MOB_IA" :
			case "CAMBIAR_IA_MOB" :
			case "SET_TIPO_IA" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				mobModelo = Mundo.getMobModelo(id);
				if (mobModelo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mob no existe");
					return;
				}
				byte tipoIA = 0;
				try {
					tipoIA = Byte.parseByte(infos[2]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				mobModelo.setTipoIA(tipoIA);
				GestorSQL.UPDATE_MOB_IA_TALLA(mobModelo);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mob " + mobModelo.getNombre() + " a cambiado su IA a: " + tipoIA);
				break;
			case "GET_IA" :
			case "GET_IA_MOB" :
			case "GET_MOB_IA" :
			case "INFO_IA_MOB" :
			case "GET_TIPO_IA" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				mobModelo = Mundo.getMobModelo(id);
				if (mobModelo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mob no existe");
					return;
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mob " + mobModelo.getNombre() + " tiene la IA: " + mobModelo
				.getTipoIA());
				break;
			case "MOB_COLORES" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				mobModelo = Mundo.getMobModelo(id);
				if (mobModelo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mob no existe");
					return;
				}
				try {
					str = infos[2];
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				mobModelo.setColores(str);
				GestorSQL.UPDATE_MOB_COLORES(mobModelo);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mob " + mobModelo.getNombre() + " a cambiado su color a: " + str);
				break;
			case "DISTANCIA_AGRESION_MOB" :
			case "DISTANCIA_AGRESION" :
			case "MOB_DISTANCIA_AGRESION" :
			case "MOB_AGRESION" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				mobModelo = Mundo.getMobModelo(id);
				if (mobModelo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mob no existe");
					return;
				}
				byte agresion = 0;
				try {
					agresion = Byte.parseByte(infos[2]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				mobModelo.setDistAgresion(agresion);
				GestorSQL.UPDATE_MOB_AGRESION(mobModelo);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mob " + mobModelo.getNombre() + " a cambiado su agresion a: "
				+ agresion);
				break;
			case "MOB_SIZE" :
			case "MOB_TALLA" :
			case "SIZE_MOB" :
			case "TALLA_MOB" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				mobModelo = Mundo.getMobModelo(id);
				if (mobModelo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mob no existe");
					return;
				}
				try {
					cantShort = Short.parseShort(infos[2]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				mobModelo.setTalla(cantShort);
				GestorSQL.UPDATE_MOB_IA_TALLA(mobModelo);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mob " + mobModelo.getNombre() + " a cambiado su talla a: "
				+ cantShort);
				break;
			case "MODIFICAR_STATS_HECHIZO" :
			case "MODIFICAR_HECHIZO" :
			case "STAT_HECHIZO" :
			case "STATS_HECHIZO" :
			case "MODIFICAR_STAT_HECHIZO" :
				String stat = "";
				try {
					infos = mensaje.split(" ", 4);
					id = Integer.parseInt(infos[1]);
					cantInt = Integer.parseInt(infos[2]);
					stat = infos[3];
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				hechizo = Mundo.getHechizo(id);
				if (hechizo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Hechizo no existe");
					return;
				}
				StatHechizo sh = null;
				try {
					sh = Hechizo.analizarHechizoStats(id, cantInt, stat);
					hechizo.addStatsHechizos(cantInt, sh);
					GestorSQL.UPDATE_STAT_HECHIZO(id, stat, cantInt);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modifico el hechizo " + hechizo.getNombre() + " (" + hechizo
					.getID() + ") nivel " + cantInt + " a " + stat);
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Stat hechizo incorrecto o no valido");
					return;
				}
				break;
			case "SET_CONDICIONES_HECHIZO" :
			case "SET_CONDICION_HECHIZO" :
			case "SET_HECHIZO_CONDICION" :
			case "SET_HECHIZO_CONDICIONES" :
			case "CONDICIONES_HECHIZO" :
			case "SET_CONDICIONES_HECHIZOS" :
			case "HECHIZO_CONDICIONES" :
			case "CONDICIONES_HECHIZOS" :
			case "CONDITION_SPELLS" :
			case "CONDITION_SPELL" :
			case "CONDITIONS_SPELLS" :
			case "CONDITIONS_SPELL" :
			case "SPELLS_CONDITIONS" :
			case "SPELL_CONDITIONS" :
			case "SET_SPELLS_CONDITIONS" :
			case "SET_SPELL_CONDITIONS" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				hechizo = Mundo.getHechizo(id);
				if (hechizo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Hechizo no existe");
					return;
				}
				String condiciones = "";
				try {
					condiciones = infos[2];
				} catch (final Exception e) {}
				hechizo.setCondiciones(condiciones);
				GestorSQL.ACTUALIZAR_CONDICIONES_HECHIZO(id, condiciones);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El hechizo " + hechizo.getNombre() + " a cambiado sus condiciones : "
				+ condiciones);
				break;
			case "SET_AFECTADOS" :
			case "HECHIZO_AFECTADOS" :
			case "SPELL_TARGETS" :
			case "TARGETS_SPELL" :
			case "TARGETS" :
			case "AFECTADOS" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				hechizo = Mundo.getHechizo(id);
				if (hechizo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Hechizo no existe");
					return;
				}
				String afectados = "";
				try {
					afectados = infos[2];
				} catch (final Exception e) {}
				hechizo.setAfectados(afectados);
				GestorSQL.UPDATE_HECHIZO_AFECTADOS(id, afectados);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El hechizo " + hechizo.getNombre() + " a cambiado sus afectados : "
				+ afectados);
				break;
			case "SET_HECHIZO_VALOR_IA" :
			case "SPELL_VALUE_IA" :
			case "HECHIZO_VALOR_IA" :
			case "VALOR_IA_HECHIZO" :
			case "SET_IA_HECHIZO" :
			case "SET_VALOR_IA_HECHIZO" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				hechizo = Mundo.getHechizo(id);
				if (hechizo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Hechizo no existe");
					return;
				}
				int valorIA = 0;
				try {
					valorIA = Integer.parseInt(infos[2]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				hechizo.setValorIA(valorIA);
				GestorSQL.UPDATE_HECHIZOS_VALOR_IA(id, valorIA);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El hechizo " + hechizo.getNombre() + " a cambiado su valorIA : "
				+ valorIA);
				break;
			case "SPRITE_ID_HECHIZO" :
			case "HECHIZO_SPRITE_ID" :
			case "SPELL_SPRITE_ID" :
			case "SPRITE_ID_SPELL" :
			case "SPRITE_ID" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				hechizo = Mundo.getHechizo(id);
				if (hechizo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Hechizo no existe");
					return;
				}
				int spriteID = 0;
				try {
					spriteID = Integer.parseInt(infos[2]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				hechizo.setSpriteID(spriteID);
				GestorSQL.ACTUALIZAR_SPRITE_ID_HECHIZO(id, spriteID);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El hechizo " + hechizo.getNombre() + " a cambiado su sprite ID : "
				+ spriteID);
				break;
			case "SPELL_SPRITE_INFOS" :
			case "HECHIZO_SPRITE_INFOS" :
			case "SPRITE_INFOS_HECHIZO" :
			case "SPRITE_INFOS_SPELL" :
			case "SPRITE_INFOS" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				hechizo = Mundo.getHechizo(id);
				if (hechizo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Hechizo no existe");
					return;
				}
				String spriteInfos = "";
				try {
					spriteInfos = infos[2];
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				hechizo.setSpriteInfos(spriteInfos);
				GestorSQL.ACTUALIZAR_SPRITE_INFO_HECHIZO(id, spriteInfos);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El hechizo " + hechizo.getNombre() + " a cambiado su spriteInfos : "
				+ spriteInfos);
				break;
			case "GET_SPRITE_INFOS" :
			case "DAR_SPRITE_INFOS" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				hechizo = Mundo.getHechizo(id);
				if (hechizo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Hechizo no existe");
					return;
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El hechizo " + hechizo.getNombre() + " tiene como spriteInfos : "
				+ hechizo.getSpriteInfos());
				break;
			case "GET_SPRITE_ID" :
			case "DAR_SPRITE_ID" :
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				hechizo = Mundo.getHechizo(id);
				if (hechizo == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Hechizo no existe");
					return;
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El hechizo " + hechizo.getNombre() + " tiene como sprite ID : "
				+ hechizo.getSpriteID());
				break;
			case "ADD_XP" :
			case "ADD_EXP" :
			case "DAR_EXP" :
			case "DAR_XP" :
			case "GANAR_XP" :
			case "GANAR_EXP" :
				try {
					cantInt = Integer.parseInt(infos[1]);
					if (cantInt < 1) {
						cantInt = 1;
					}
					objetivo = _perso;
					if (infos.length > 2) {
						objetivo = Mundo.getPersonajePorNombre(infos[2]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					objetivo.addExperiencia(cantInt, true);
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur " + objetivo.getNombre() + " a gagne " + cantInt
						+ " points experience");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "EL jugador " + objetivo.getNombre() + " a ganado " + cantInt
						+ " puntos de experiencia");
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects");
				}
				break;
			case "UP_LEVEL" :
			case "NIVEL" :
			case "LEVEL" :
				try {
					cantInt = Integer.parseInt(infos[1]);
					if (cantInt < 1) {
						cantInt = 1;
					}
					if (cantInt > MainServidor.NIVEL_MAX_PERSONAJE) {
						cantInt = MainServidor.NIVEL_MAX_PERSONAJE;
					}
					objetivo = _perso;
					if (infos.length > 2) {
						objetivo = Mundo.getPersonajePorNombre(infos[2]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					if (objetivo.getEncarnacionN() != null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso,
							"Le joueur est en mode incarnation, impossible de lui augmenter son niveau.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso,
							"No se le puede subir el nivel, porque el personaje es una encarnación.");
						}
						return;
					}
					objetivo.subirHastaNivel(cantInt);
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le niveau du joueur a été modifié.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ha sido modificado el nivel de " + objetivo.getNombre() + " a "
						+ cantInt);
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrects");
				}
				break;
			case "IP_PLAYER" :
			case "IP_PERSONAJE" :
			case "DAR_IP_PLAYER" :
			case "DAR_IP_PERSONAJE" :
			case "DAR_IP" :
			case "GET_IP" :
			case "GET_IP_PLAYER" :
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				if (!objetivo.enLinea()) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'est pas connecté.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no esta conectado");
					}
					return;
				}
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "L'IP du joueur " + objetivo.getNombre() + " est : " + objetivo
					.getCuenta().getActualIP());
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El IP del personaje " + objetivo.getNombre() + " es: " + objetivo
					.getCuenta().getActualIP());
				}
				break;
			case "MOVER_RECAU" :
			case "MOVER_PERCO" :
			case "MOVE_PERCO" :
			case "MOVER_RECAUDADOR" :
				if (_perso.getPelea() != null) {
					return;
				}
				Recaudador recaudador = _perso.getMapa().getRecaudador();
				if (recaudador == null || recaudador.getPelea() != null) {
					return;
				}
				recaudador.moverRecaudador();
				recaudador.setEnRecolecta(false);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se movio el recaudador del mapa");
				break;
			case "MOVER_MOBS" :
			case "MOVE_MOB" :
			case "MOVE_MOBS" :
			case "MOVER_MOB" :
				if (_perso.getPelea() != null) {
					return;
				}
				id = 1;
				try {
					id = Integer.parseInt(infos[1]);
				} catch (final Exception e) {}
				_perso.getMapa().moverGrupoMobs(id);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se movieron " + id + "  grupos de mobs del mapa");
				break;
			case "OBJETO" :
			case "ITEM" :
			case "!GETITEM" :
			case "GETITEM" :
				// final boolean esPorConsole = comando.equalsIgnoreCase("!GETITEM");
				try {
					id = Integer.parseInt(infos[1]);
					final ObjetoModelo OM = Mundo.getObjetoModelo(id);
					if (OM == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "L'objet " + id + " n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El objeto modelo " + id + " no existe ");
						}
						return;
					}
					if (OM.getOgrinas() > 0 && _cuenta.getAdmin() < 5) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Tu ne possèdes pas le GM nécessaire pour spawn cet objet.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No posees el nivel de GM requerido");
						}
						return;
					}
					try {
						if (infos.length > 2) {
							cantInt = Integer.parseInt(infos[2]);
						}
					} catch (final Exception e) {}
					if (cantInt < 1) {
						cantInt = 1;
					}
					objetivo = _perso;
					if (infos.length > 3) {
						objetivo = Mundo.getPersonajePorNombre(infos[3]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					if (MainServidor.PARAM_NOMBRE_COMPRADOR) {
						if (_cuenta.getAdmin() < 5) {
							objetivo = _perso;
						}
						intercambiable = (ObjetoModelo.stringFechaIntercambiable(3650));
					}
					CAPACIDAD_STATS useMax = CAPACIDAD_STATS.RANDOM;
					if (infos.length > 4) {
						useMax = infos[4].equalsIgnoreCase("MAX")
						? CAPACIDAD_STATS.MAXIMO
						: (infos[4].equalsIgnoreCase("MIN") ? CAPACIDAD_STATS.MINIMO : CAPACIDAD_STATS.RANDOM);
					}
					Objeto obj = OM.crearObjeto(cantInt, Constantes.OBJETO_POS_NO_EQUIPADO, useMax);
					if (MainServidor.PARAM_NOMBRE_COMPRADOR) {
						obj.addStatTexto(Constantes.STAT_INTERCAMBIABLE_DESDE, intercambiable);
						obj.addStatTexto(Constantes.STAT_PERTENECE_Y_NO_VENDER, "0#0#0#" + _perso.getNombre());
					}
					objetivo.addObjIdentAInventario(obj, false);
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						strB.append("Creatio de l'objet " + OM.getNombre() + " (" + id + ") en " + cantInt + " exemplaires pour "
						+ objetivo.getNombre());
					} else {
						strB.append("Se creó " + cantInt + " objeto(s) " + OM.getNombre() + " (" + id + ") para el personaje "
						+ objetivo.getNombre());
					}
					switch (useMax) {
						case MAXIMO :
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								strB.append(" avec des jets parfaits.");
							} else {
								strB.append(" con stats máximos");
							}
							break;
						case MINIMO :
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								strB.append(" avec des jets minimuns.");
							} else {
								strB.append(" con stats mínimos");
							}
							break;
						default :
							break;
					}
					if (objetivo.enLinea()) {
						GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(objetivo);
					} else {
						strB.append(", mais le joueur n'est pas en ligne");
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, strB.toString());
				} catch (Exception e) {}
				break;
			default :
				GM_lvl_3(comando, infos, mensaje, _cuenta, _perso);
				return;
		}
		// if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
		// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Commande GM 4!");
		// } else {
		// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Comando de nivel 4");
		// }spamear elbusta 3 HOwww.dofus.com
	}
	
	public static void GM_lvl_5(final String comando, String[] infos, final String mensaje, final Cuenta _cuenta,
	final Personaje _perso) {
		boolean boleano = false;
		byte idByte = 0;
		int idInt = 0, ogrinas = 0, accionID = 0;
		short idShort = 0, celda1 = 0, celda2 = 0;
		ObjetoModelo objMod;
		Personaje objetivo = null;
		Cuenta cuenta;
		String str = "", args = "";
		switch (comando.toUpperCase()) {
			case "RELOAD_CONFIG" :
			case "CARGAR_CONFIGURACION" :
			case "LOAD_CONFIG" :
			case "REFRESH_CONFIG" :
				MainServidor.cargarConfiguracion(_perso);
				break;
			case "CLONAR_MAPA" :
				try {
					int idClonar = Integer.parseInt(infos[1]);
					int nuevaID = Integer.parseInt(infos[2]);
					String fecha = infos[3];
					int x = Integer.parseInt(infos[4]);
					int y = Integer.parseInt(infos[5]);
					int subArea = Integer.parseInt(infos[6]);
					Mapa mapa = Mundo.getMapa((short) idClonar);
					if (mapa == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mapa a clonar no existe");
						return;
					}
					if (Mundo.getMapa((short) nuevaID) != null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mapa a crear ya existe");
						return;
					}
					if (GestorSQL.CLONAR_MAPA(mapa, nuevaID, fecha, x, y, subArea)) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mapa clonado con ID " + nuevaID + " y fecha " + fecha);
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No se pudo clonar el mapa");
						return;
					}
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
					return;
				}
				break;
			case "CREAR_AUDIO" :
			case "CREAR_MP3" :
			case "CREATE_SOUND" :
			case "CREAR_SONIDO" :
				try {
					infos = mensaje.split(" ", 2);
					str = infos[1];
					String mp3 = TextoAVoz.crearMP3(str, "");
					if (mp3 == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No se pudo crear el sonido");
					} else if (mp3.isEmpty()) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Excediste en los caracteres");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Sonando el mp3 " + mp3);
						GestorSalida.ENVIAR_Bv_SONAR_MP3(_perso, mp3);
					}
				} catch (Exception e) {}
				break;
			case "CREAR_AUDIO_IDIOMA" :
			case "CREAR_MP3_IDIOMA" :
			case "CREAR_MP3_LANG" :
			case "CREAR_SOUND_LANG" :
			case "CREATE_MP3_LANG" :
			case "CREATE_SOUND_LANG" :
			case "CREAR_SONIDO_IDIOMA" :
				try {
					infos = mensaje.split(" ", 3);
					str = infos[2];
					String mp3 = TextoAVoz.crearMP3(str, infos[1]);
					if (mp3 == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No se pudo crear el sonido");
					} else if (mp3.isEmpty()) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Excediste en los caracteres");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Sonando el mp3 " + mp3);
						GestorSalida.ENVIAR_Bv_SONAR_MP3(_perso, mp3);
					}
				} catch (Exception e) {}
				break;
			case "TELEPORT_TODOS" :
				try {
					short mapaID = 0, celdaID = 0;
					try {
						if (infos.length > 1) {
							mapaID = Short.parseShort(infos[1]);
						}
						if (infos.length > 2) {
							celdaID = Short.parseShort(infos[2]);
						}
					} catch (final Exception e) {}
					Mapa mapa = Mundo.getMapa(mapaID);
					if (mapa == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "MAPID INVALIDE!");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mapa a teleportar no existe");
						}
						return;
					}
					if (celdaID <= -1) {
						celdaID = mapa.getRandomCeldaIDLibre();
					} else if (mapa.getCelda(celdaID) == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "CELLID INVALIDE!");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "CeldaID inválida");
						}
						return;
					}
					for (Personaje p : Mundo.getPersonajesEnLinea()) {
						p.teleport(mapaID, celdaID);
					}
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
					return;
				}
				break;
			case "LIMITE_STATS_CON_BUFF" :
				try {
					for (final String s : infos[1].split(";")) {
						if (s.isEmpty()) {
							continue;
						}
						try {
							final String[] stat = s.split(",");
							MainServidor.LIMITE_STATS_CON_BUFF.put(Integer.parseInt(stat[0]), Integer.parseInt(stat[1]));
						} catch (final Exception e) {}
					}
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
					return;
				}
				break;
			case "LIMITE_STATS_SIN_BUFF" :
				try {
					for (final String s : infos[1].split(";")) {
						if (s.isEmpty()) {
							continue;
						}
						try {
							final String[] stat = s.split(",");
							MainServidor.LIMITE_STATS_SIN_BUFF.put(Integer.parseInt(stat[0]), Integer.parseInt(stat[1]));
						} catch (final Exception e) {}
					}
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
					return;
				}
				break;
			case "GET_CONFIGURACION" :
			case "GET_CONFIG" :
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, MainServidor.getConfiguracion());
				break;
			case "ADD_OBJETO_TRUEQUE" :
			case "AGREGAR_OBJETO_TRUEQUE" :
			case "ADD_TRUEQUE" :
			case "AGREGAR_TRUEQUE" :
				try {
					int prioridad = 0;
					String npcs = "";
					try {
						idInt = Integer.parseInt(infos[1]);
						str = infos[2];
						prioridad = Byte.parseByte(infos[3]);
					} catch (Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
						return;
					}
					objMod = Mundo.getObjetoModelo(idInt);
					if (objMod == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Objeto modelo no existe");
						return;
					}
					if (str.isEmpty()) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Objetos necesarios invalidos");
						return;
					}
					Mundo.addObjetoTrueque(objMod.getID(), str, prioridad, npcs);
					GestorSQL.INSERT_OBJETO_TRUEQUE(objMod.getID(), str, prioridad, npcs, objMod.getNombre());
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se agregó el objeto trueque " + objMod.getNombre() + " (" + objMod
					.getID() + "), objetos necesarios: " + str + ", prioridad: " + prioridad + ", npcs: " + npcs);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una excepcion");
				}
				break;
			case "INICIAR_ATAQUE" :
			case "START_ATTACK" :
			case "STARTATTACK" :
				str = infos[1];
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Start the Attack: " + str);
				GestorSalida.enviarTodos(1, "AjI" + str);
				break;
			case "PARAR_ATAQUE" :
			case "STOP_ATTACK" :
			case "STOPATTACK" :
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Stop the Attack");
				GestorSalida.enviarTodos(1, "AjP");
				break;
			case "PAQUETE_ATAQUE" :
			case "PACKET_ATTACK" :
			case "PACKETATTACK" :
				str = infos[1];
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Send Packet of Attack: " + str);
				GestorSalida.enviarTodos(1, "AjE" + str);
				break;
			case "BAN_PERM_FDP" :
			case "BAN_PERMANENTE" :
			case "BAN_CLIENTE" :
			case "BAN_CLIENT" :
			case "BAN_DOFUS" :
				try {
					if (infos.length > 1) {
						objetivo = Mundo.getPersonajePorNombre(infos[1]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					if (!objetivo.enLinea()) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'est pas connecté.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no esta conectado");
						}
						return;
					}
					int tiempo = 10000;
					try {
						if (infos.length > 2) {
							tiempo = Integer.parseInt(infos[2]);
						}
					} catch (Exception e) {}
					GestorSalida.enviar(objetivo, "$" + tiempo);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre()
					+ " ha sido crasheado su cliente por " + tiempo + " minutos");
					objetivo.getCuenta().getSocket().cerrarSocket(true, "command BAN CLIENTE");
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
				}
				break;
			case "CRASH" :
			case "CRASH_FDP" :
				try {
					infos = mensaje.split(" ", 3);
					if (infos.length > 1) {
						objetivo = Mundo.getPersonajePorNombre(infos[1]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					if (!objetivo.enLinea()) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'est pas connecté.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no esta conectado");
						}
						return;
					}
					int veces = 10000;
					GestorSalida.enviar(objetivo, "@" + veces + ";HOhttp://" + infos[2]);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre()
					+ " ha sido crasheado con la url " + infos[2]);
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
				}
				break;
			case "SPAMMEAR" :
			case "SPAM" :
			case "SPAMEAR" :
				try {
					infos = mensaje.split(" ", 4);
					if (infos.length > 1) {
						objetivo = Mundo.getPersonajePorNombre(infos[1]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					if (!objetivo.enLinea()) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'est pas connecté.");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no esta conectado");
						}
						return;
					}
					int veces = 10000;
					try {
						if (infos.length > 2) {
							veces = Integer.parseInt(infos[2]);
						}
					} catch (Exception e) {}
					GestorSalida.enviar(objetivo, "@" + veces + ";" + infos[3]);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre()
					+ " ha sido spameado su cliente por " + veces + " veces, con el packet " + infos[3]);
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
				}
				break;
			case "CAMBIAR_NOMBRE" :
			case "CHANGE_NAME" :
				try {
					if (infos.length > 1) {
						objetivo = Mundo.getPersonajePorNombre(infos[1]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					if (infos.length > 2) {
						String viejoNombre = objetivo.getNombre();
						String nombre = Personaje.nombreValido(infos[2], true);
						if (nombre != null && !nombre.isEmpty()) {
							objetivo.cambiarNombre(nombre);
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se cambio el nombre del jugador " + viejoNombre + " cambio a "
							+ objetivo.getNombre());
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Nuevo nombre incorrecto o ya esta en uso");
						}
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ingrese el nuevo nombre");
					}
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
				}
				break;
			case "GET_PATH" :
				try {
					celda1 = Short.parseShort(infos[1]);
					celda2 = Short.parseShort(infos[2]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				idInt = -1;
				try {
					idInt = Integer.parseInt(infos[3]);
				} catch (final Exception e) {}
				Duo<Integer, ArrayList<Celda>> path = Camino.getPathPelea(_perso.getPelea() != null
				? _perso.getPelea().getMapaCopia()
				: _perso.getMapa(), celda1, celda2, idInt, null, true);
				if (path != null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Path es " + path._primero + " y " + path._segundo.size());
					String s = "";
					for (Celda c : path._segundo) {
						s += c.getID() + " ";
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "celdas " + s);
				} else
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Path es nulo");
				break;
			case "FINISH_ALL_FIGHTS" :
			case "FINALIZAR_PELEAS" :
			case "FINISH_COMBATS" :
			case "FINISH_FIGHTS" :
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "FINALIZANDO TODAS LAS PELEAS ... ");
				Mundo.finalizarPeleas();
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "100%");
				break;
			case "REGISTER" :
			case "REGISTRO" :
			case "REGISTE" :
			case "REGISTRAR" :
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "INICIANDO EL REGISTRO DE JUGADORES Y SQL ... ");
				MainServidor.imprimirLogPlayers();
				MainServidor.imprimirLogSQL();
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "100%");
				break;
			case "REGISTER_SQL" :
			case "REGISTRO_SQL" :
			case "REGISTE_SQL" :
			case "REGISTRAR_SQL" :
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "INICIANDO EL REGISTRO DE SQL ... ");
				MainServidor.imprimirLogSQL();
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "100%");
				break;
			case "REGISTER_PLAYERS" :
			case "REGISTRO_PLAYERS" :
			case "REGISTE_PLAYERS" :
			case "REGISTRAR_PLAYERS" :
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "INICIANDO EL REGISTRO DE JUGADORES ... ");
				MainServidor.imprimirLogPlayers();
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "100%");
				break;
			case "REGISTRAR_PJ" :
			case "REGISTER_PLAYER" :
			case "REGISTRAR_PLAYER" :
			case "REGISTRAR_JUGADOR" :
				objetivo = _perso;
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				if (!ServidorSocket.JUGADORES_REGISTRAR.contains(objetivo.getCuenta().getNombre())) {
					ServidorSocket.JUGADORES_REGISTRAR.add(objetivo.getCuenta().getNombre());
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La cuenta del personaje " + objetivo.getNombre()
					+ " fue registrada para archivar los logs");
				}
				break;
			case "INFO_PJ" :
			case "INFO_PLAYER" :
			case "INFO_PERSONAJE" :
			case "STATS_PERSO" :
			case "STATS_PJ" :
			case "STATS_PERSONAJE" :
			case "STATS_PLAYER" :
				objetivo = _perso;
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Abre tu panel de caracteristicas para ver la informacion del jugador "
				+ objetivo.getNombre());
				GestorSalida.enviarEnCola(_perso, objetivo.stringStats(), false);
				break;
			case "INVENTORY_PLAYER" :
			case "INVENTARIO_PLAYER" :
			case "INVENTARIO_JUGADOR" :
			case "INVENTARIO_PERSO" :
			case "INVENTARIO_PJ" :
			case "INVENTORY_PJ" :
			case "INVENTORY_PERSO" :
				objetivo = _perso;
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Abre tu panel de inventario para ver los items del jugador "
				+ objetivo.getNombre());
				// _perso.setEspiarPj(true);
				GestorSalida.ENVIAR_ASK_PERSONAJE_A_ESPIAR(objetivo, _perso);
				break;
			case "COMANDOS_PERMITIDOS" :
			case "COMANDO_PERMITIDO" :
			case "ADD_COMANDO_PERMITIDO" :
			case "AGREGAR_COMANDO_PERMITIDO" :
				try {
					MainServidor.COMANDOS_PERMITIDOS.add(infos[1]);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se agrego a comandos permitidos: " + infos[1]);
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Debes poner algun comando para agregar");
				}
				break;
			case "COMANDOS_VIP" :
			case "COMANDO_VIP" :
			case "ADD_COMANDO_VIP" :
			case "AGREGAR_COMANDO_VIP" :
				try {
					MainServidor.COMANDOS_VIP.add(infos[1]);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se agrego a comandos vips: " + infos[1]);
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Debes poner algun comando para agregar");
				}
				break;
			case "PALABRAS_PROHIBIDAS" :
				try {
					MainServidor.PALABRAS_PROHIBIDAS.add(infos[1]);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se agrego a palabras prohibidas: " + infos[1]);
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Debes poner alguna palabra para agregar");
				}
				break;
			case "INFO_STUFF_PJ" :
			case "INFO_STUFF_PERSO" :
			case "INFO_ROPA_PJ" :
			case "INFO_ROPA_PERSO" :
				objetivo = _perso;
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				for (byte i : Constantes.POSICIONES_EQUIPAMIENTO) {
					if (!str.isEmpty()) {
						str += ", ";
					}
					if (objetivo.getObjPosicion(i) == null) {
						str += "null";
					} else {
						str += objetivo.getObjPosicion(i).getID();
					}
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Stuff de " + objetivo.getNombre() + " es " + str);
				break;
			case "TIEMPO_POR_LANZAR_HECHIZO" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				EfectoHechizo.TIEMPO_POR_LANZAR_HECHIZO = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El TIEMPO_POR_LANZAR_HECHIZO cambio a " + idInt);
				break;
			case "TIEMPO_GAME_ACTION" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				EfectoHechizo.TIEMPO_GAME_ACTION = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El TIEMPO_GAME_ACTION cambio a " + idInt);
				break;
			case "TIEMPO_ENTRE_EFECTOS" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				EfectoHechizo.TIEMPO_ENTRE_EFECTOS = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El TIEMPO_ENTRE_EFECTOS cambio a " + idInt);
				break;
			case "TIME_SLEEP_PACKETS_CARGAR_MAPA" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El TIME_SLEEP_PACKETS_CARGAR_MAPA cambio a " + idInt);
				break;
			case "PROBABILIDAD_PROTECTOR_RECURSOS" :
				try {
					idByte = Byte.parseByte(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.PROBABILIDAD_PROTECTOR_RECURSOS = idByte;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PROBABILIDAD_PROTECTOR_RECURSOS cambio a " + idByte);
				break;
			case "SEGUNDOS_REAPARECER_MOBS" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.SEGUNDOS_REAPARECER_MOBS = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El SEGUNDOS_REAPARECER_MOBS cambio a " + idInt);
				break;
			case "FACTOR_ZERO_DROP" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.FACTOR_ZERO_DROP = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El FACTOR_ZERO_DROP cambio a " + idInt);
				break;
			case "ID_MIMOBIONTE" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.ID_MIMOBIONTE = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La ID del mimobionte cambio a " + idInt);
				break;
			case "MODIFICAR_PARAM" :
				try {
					String resto = mensaje.split(" ", 2)[1];
					consolaComando(resto, _cuenta, _perso);
					infos = resto.split(" ", 2);
					MainServidor.modificarParam(infos[0], infos[1]);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se cambio el parametro: " + infos[0] + " a " + infos[1]);
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
				}
				break;
			case "DESHABILITAR_SQL" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.PARAM_DESHABILITAR_SQL = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Deshabilitar sql ahora esta " + boleano);
				break;
			case "OGRINAS_POR_VOTO" :
				try {
					idByte = Byte.parseByte(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.OGRINAS_POR_VOTO = idByte;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Las ogrinas por voto cambio a " + idByte);
				break;
			case "MINUTOS_VALIDAR_VOTO" :
			case "MINUTOS_SIGUIENTE_VOTO" :
			case "MINUTOS_SIG_VOTO" :
				try {
					idShort = Short.parseShort(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.MINUTOS_VALIDAR_VOTO = idShort;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Los minutos para el siguiente voto cambio a " + idShort);
				break;
			case "MAX_MISIONES_ALMANAX" :
				try {
					idShort = Short.parseShort(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.MAX_MISIONES_ALMANAX = idShort;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El max de misiones almanax cambio a " + idShort);
				break;
			case "MAX_CARACTERES_SONIDO" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.MAX_CARACTERES_SONIDO = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El MAX_CARACTERES_SONIDO cambio a " + idInt);
				break;
			case "MAX_PACKETS_DESCONOCIDOS" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.MAX_PACKETS_DESCONOCIDOS = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El MAX_PACKETS_PARA_RASTREAR cambio a " + idInt);
				break;
			case "MAX_PACKETS_PARA_RASTREAR" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.MAX_PACKETS_PARA_RASTREAR = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El MAX_PACKETS_PARA_RASTREAR cambio a " + idInt);
				break;
			case "PROBABILIDAD_RECURSO_ESPECIAL" :
			case "PROBABILIDAD_OBJ_ESPECIAL" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.PROBABILIDAD_RECURSO_ESPECIAL = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La probabilidad de recurso recolecta especial cambio a "
				+ MainServidor.PROBABILIDAD_RECURSO_ESPECIAL);
				break;
			case "PROBABLIDAD_PERDER_STATS_FM" :
			case "PROBABLIDAD_LOST_STATS_FM" :
			case "PROBABILIDAD_FALLO_FM" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.PROBABLIDAD_PERDER_STATS_FM = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La probabilidad de fallo critico FM cambio a "
				+ MainServidor.PROBABLIDAD_PERDER_STATS_FM);
				break;
			case "PERMITIR_BONUS_ESTRELLAS" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.PARAM_PERMITIR_BONUS_ESTRELLAS = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El parametro permitir bonus estrellas cambio a "
				+ MainServidor.PARAM_PERMITIR_BONUS_ESTRELLAS);
				break;
			case "PERMITIR_BONUS_RETOS" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.PARAM_PERMITIR_BONUS_DROP_RETOS = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El parametro permitir bonus retos cambio a " + boleano);
				break;
			case "MAX_STARS_MOBS" :
			case "MAX_ESTRELLAS_MOBS" :
				try {
					idShort = Short.parseShort(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.MAX_BONUS_ESTRELLAS_MOBS = (short) (idShort * 20);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El maximo de estrellas mobs cambio a " + idShort);
				break;
			case "MAX_BONUS_ESTRELLAS_RECURSOS" :
			case "MAX_STARS_RESSOURCES" :
			case "MAX_STARS_RECURSOS" :
			case "MAX_ESTRELLAS_RECURSOS" :
				try {
					idShort = Short.parseShort(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.MAX_BONUS_ESTRELLAS_RECURSOS = (short) (idShort * 20);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El maximo de estrellas mobs cambio a " + idShort);
				break;
			case "PROBABILIDAD_ESCAPAR_MONTURA_DESPUES_PARIR" :
				try {
					idByte = Byte.parseByte(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.PROBABILIDAD_ESCAPAR_MONTURA_DESPUES_FECUNDAR = idByte;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La probabilidad de escapar la montura despues de parir cambio a "
				+ MainServidor.PROBABILIDAD_ESCAPAR_MONTURA_DESPUES_FECUNDAR);
				break;
			case "CHANGE_FACE" :
			case "CAMBIAR_ROSTRO" :
				try {
					idByte = Byte.parseByte(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				objetivo.cambiarRostro(idByte);
				GestorSalida.ENVIAR_Oa_CAMBIAR_ROPA_MAPA(objetivo.getMapa(), objetivo);
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se cambio el rostro al personaje " + objetivo.getNombre() + " a "
				+ idByte);
				break;
			case "PERMITIR_BONUS_DROP_RETOS" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_PERMITIR_BONUS_DROP_RETOS = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PERMITIR_BONUS_DROP_RETOS cambio a " + boleano);
				break;
			case "PERMITIR_BONUS_EXP_RETOS" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_PERMITIR_BONUS_EXP_RETOS = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PERMITIR_BONUS_EXP_RETOS cambio a " + boleano);
				break;
			case "PARAM_RANKING_STAFF" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_LADDER_STAFF = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_RANKING_STAFF cambio a " + boleano);
				break;
			case "PARAM_INFO_DAÑO_BATALLA" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_INFO_DAÑO_BATALLA = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_INFO_DAÑO_BATALLA cambio a " + boleano);
				break;
			case "PARAM_MOSTRAR_EXP_MOBS" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_MOSTRAR_EXP_MOBS = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_MOSTRAR_EXP_MOBS cambio a " + boleano);
				break;
			case "PARAM_AUTO_SANAR" :
			case "PARAM_AUTO_CURAR" :
			case "PARAM_AUTO_RECUPERAR_VIDA" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_AUTO_RECUPERAR_TODA_VIDA = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_AUTO_RECUPERAR_TODA_VIDA cambio a " + boleano);
				break;
			case "PARAM_MOSTRAR_PROBABILIDAD_TACLEO" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_MOSTRAR_PROBABILIDAD_TACLEO = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_MOSTRAR_PROBABILIDAD_TACLEO cambio a " + boleano);
				break;
			case "PARAM_AUTO_SALTAR_TURNO" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_AUTO_SALTAR_TURNO = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_AUTO_SALTAR_TURNO cambio a " + boleano);
				break;
			case "PARAM_TODOS_MOBS_EN_BESTIARIO" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_TODOS_MOBS_EN_BESTIARIO = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_TODOS_MOBS_EN_BESTIARIO cambio a " + boleano);
				break;
			case "PARAM_CAPTURAR_MONTURA_COMO_PERGAMINO" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_CAPTURAR_MONTURA_COMO_PERGAMINO = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_CAPTURAR_MONTURA_COMO_PERGAMINO cambio a " + boleano);
				break;
			case "PARAM_AGRESION_ADMIN" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_AGRESION_ADMIN = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_AGRESION_ADMIN cambio a " + boleano);
				break;
			case "JUGAR_RAPIDO" :
			case "PARAM_JUGAR_RAPIDO" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_JUGAR_RAPIDO = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_JUGAR_RAPIDO cambio a " + boleano);
				break;
			case "PARAM_PERDER_ENERGIA" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_PERDER_ENERGIA = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_PERDER_ENERGIA cambio a " + boleano);
				break;
			case "PARAM_ALBUM" :
			case "ALBUM_MOBS" :
			case "ACTIVAR_ALBUM" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_BESTIARIO = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_ALBUM_MOBS cambio a " + boleano);
				break;
			case "PARAM_AGRESION_MULTICUENTA" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_PERMITIR_MULTICUENTA_PELEA_PVP = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_AGRESION_MULTICUENTA cambio a " + boleano);
				break;
			case "PARAM_LOTERIA" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_LOTERIA = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_LOTERIA cambio a " + boleano);
				break;
			case "COMANDOS_JUGADOR" :
			case "COMMANDES_JOUERS" :
			case "COMMANDS_PLAYERS" :
			case "PARAM_COMANDOS_JUGADOR" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_COMANDOS_JUGADOR = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_COMANDOS_JUGADOR cambio a " + boleano);
				break;
			case "PARAM_AURA" :
			case "ACTIVAR_AURA" :
			case "AURA" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_ACTIVAR_AURA = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_ACTIVAR_AURA cambio a " + boleano);
				break;
			case "PARAM_ANTI_SPEEDHACK" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_ANTI_SPEEDHACK = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_ANTI_SPEEDHACK cambio a " + boleano);
				break;
			case "PARAM_ANTI_DDOS" :
			case "CONTRA_DDOS" :
			case "ANTI_DDOS" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_ANTI_DDOS = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El PARAM_ANTI_DDOS cambio a " + boleano);
				break;
			case "RECOLECTOR_BASURA" :
			case "GC" :
			case "GARBAGE_COLLECTOR" :
				try {
					System.gc();
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se inicio el garbage collector");
				} catch (final Exception e) {
					MainServidor.redactarLogServidorln("COMANDO GARBAGE COLLECTOR " + e.toString());
					e.printStackTrace();
				}
				break;
			case "MEMORY" :
			case "MEMORY_USE" :
			case "MEMORIA" :
			case "MEMORIA_USADA" :
			case "ESTADO_JVM" :
				try {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "----- ESTADO JVM -----\nFreeMemory: " + Runtime.getRuntime()
					.freeMemory() / 1048576f + " MB\nTotalMemory: " + Runtime.getRuntime().totalMemory() / 1048576f
					+ " MB\nMaxMemory: " + Runtime.getRuntime().maxMemory() / 1048576f + " MB\nProcesos: " + Runtime.getRuntime()
					.availableProcessors());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "SABIDURIA_PARA_REENVIO" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.SABIDURIA_PARA_REENVIO = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La sabiduria para aumentar el daño por reenvio cambio a " + idInt);
				break;
			case "MILISEGUNDOS_CERRAR_SERVIDOR" :
			case "TIEMPO_CERRAR_SERVIDOR" :
			case "TIME_CLOSE_SERVER" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.MILISEGUNDOS_CERRAR_SERVIDOR = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modifico el tiempo para cerra el servidor a " + idInt
				+ " milisegundos");
				break;
			case "SEGUNDOS_PUBLICIDAD" :
			case "TIEMPO_PUBLICIDAD" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.SEGUNDOS_PUBLICIDAD = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modifico el tiempo de publicidad a " + idInt + " segundos");
				break;
			case "MILISEGUNDOS_ANTI_FLOOD" :
			case "TIEMPO_ANTI_FLOOD" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.MILISEGUNDOS_ANTI_FLOOD = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modifico el tiempo de Anti-Flood a " + idInt + " milisegundos");
				break;
			case "MIN_NIVEL_KOLISEO" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.MIN_NIVEL_KOLISEO = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modifico el nivel minimo para koliseo a " + idInt);
				break;
			case "SEGUNDOS_INICIAR_KOLISEO" :
			case "TIEMPO_KOLISEO" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				Mundo.SEGUNDOS_INICIO_KOLISEO = idInt;
				MainServidor.SEGUNDOS_INICIAR_KOLISEO = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modifico el tiempo de Koliseo a " + idInt);
				break;
			case "PARAM_DEVOLVER_OGRINAS" :
			case "DEVOLVER_OGRINAS" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_DEVOLVER_OGRINAS = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se cambió devolver ogrinas a " + MainServidor.PARAM_DEVOLVER_OGRINAS);
				break;
			case "PARAM_LADDER" :
			case "LADDER" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_LADDER_NIVEL = boleano;
				if (boleano)
					Mundo.actualizarRankings();
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se cambió ladder a " + MainServidor.PARAM_LADDER_NIVEL);
				break;
			case "MOBS_EVENTO" :
				try {
					idByte = Byte.parseByte(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				Mundo.MOB_EVENTO = idByte;
				new RefrescarTodosMobs().start();
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se cambió el mobEvento a " + idByte
				+ " y se esta refrescando todos los mapas");
				break;
			case "SET_STATS_OBJ_MODELO" :
			case "SET_STATS_OBJETO_MODELO" :
			case "SET_STATS_ITEM_TEMPLATE" :
			case "SET_STATS_MODELO" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				try {
					str = infos[2];
				} catch (final Exception e) {}
				objMod = Mundo.getObjetoModelo(idInt);
				if (objMod == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Objeto set nulo");
					return;
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modifico los statsModelo del objeto " + objMod.getNombre()
				+ ": \nAntiguo Stats - " + objMod.getStatsModelo() + "\nNuevos Stats - " + str);
				objMod.setStatsModelo(str);
				GestorSQL.UPDATE_STATS_OBJETO_MODELO(idInt, str);
				try {
					for (final NPCModelo npcMod : Mundo.NPC_MODELOS.values()) {
						if (npcMod.getObjAVender().contains(objMod)) {
							npcMod.actualizarObjetosAVender();
						}
					}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "PRECIO_LOTERIA" :
				try {
					idInt = Integer.parseInt(infos[1]);
					boleano = infos[2].equalsIgnoreCase("true");
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.PRECIO_LOTERIA = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modifico el precio de loteria  a " + idInt + (boleano
				? " ogrinas"
				: " kamas"));
				break;
			case "PREMIO_LOTERIA" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.PREMIO_LOTERIA = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modifico el premio de loteria  a " + idInt
				+ (MainServidor.PARAM_LOTERIA_OGRINAS ? " ogrinas" : " kamas"));
				break;
			case "LOTERIA_OGRINAS" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_LOTERIA_OGRINAS = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Loteria ogrinas es " + MainServidor.PARAM_DEVOLVER_OGRINAS);
				break;
			case "GANADORES_POR_BOLETOS" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				MainServidor.GANADORES_POR_BOLETOS = idInt;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modifico la cantidad de premios por cada " + idInt
				+ " boletos comprados");
				break;
			case "OGRINAS_OBJETO_MODELO" :
			case "SET_ITEM_POINTS" :
				try {
					try {
						idInt = Integer.parseInt(infos[1]);
						ogrinas = Integer.parseInt(infos[2]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					objMod = Mundo.getObjetoModelo(idInt);
					if (objMod == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Objeto set nulo");
						return;
					}
					objMod.setOgrinas(ogrinas);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modifico el precio del objeto modelo " + objMod.getNombre()
					+ " a " + ogrinas + " ogrinas");
					try {
						for (final NPCModelo npcMod : Mundo.NPC_MODELOS.values()) {
							if (npcMod.getObjAVender().contains(objMod)) {
								npcMod.actualizarObjetosAVender();
							}
						}
					} catch (final Exception e) {}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "KAMAS_OBJETO_MODELO" :
				try {
					try {
						idInt = Integer.parseInt(infos[1]);
						ogrinas = Integer.parseInt(infos[2]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					objMod = Mundo.getObjetoModelo(idInt);
					if (objMod == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Objeto set nulo");
						return;
					}
					objMod.setKamas(ogrinas);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se modifico el precio del objeto modelo " + objMod.getNombre()
					+ " a " + ogrinas + " kamas");
					try {
						for (final NPCModelo npcMod : Mundo.NPC_MODELOS.values()) {
							if (npcMod.getObjAVender().contains(objMod)) {
								npcMod.actualizarObjetosAVender();
							}
						}
					} catch (final Exception e) {}
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Ocurrio una exception");
				}
				break;
			case "SET_STATS_ITEM" :
			case "SETSTATSOBJETO" :
			case "SET_STATS_OBJETO" :
				try {
					infos = mensaje.split(" ", 3);
					try {
						idInt = Integer.parseInt(infos[1]);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
						return;
					}
					final Objeto obj = Mundo.getObjeto(idInt);
					if (obj == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Objeto nulo");
						return;
					}
					try {
						str = infos[2];
					} catch (final Exception e) {}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Cambio stats del objeto " + idInt + ": \nAntiguo Stats - " + obj
					.convertirStatsAString(true) + "\nNuevos Stats - " + str);
					obj.convertirStringAStats(str);
					if (_perso.getObjeto(idInt) != null) {
						if (_perso.enLinea()) {
							GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(_perso, obj);
							if (Constantes.esPosicionEquipamiento(obj.getPosicion())) {
								_perso.refrescarStuff(true, true, false);
							}
						}
					}
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
				}
				break;
			case "BORRAR_OBJETO" :
			case "BORRAR_ITEM" :
			case "DEL_ITEM" :
			case "DELETE_ITEM" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				final Objeto obj = Mundo.getObjeto(idInt);
				if (obj == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Objeto nulo");
					return;
				}
				if (_perso.tieneObjetoID(obj.getID())) {
					_perso.borrarOEliminarConOR(idInt, true);
				} else {
					Mundo.eliminarObjeto(obj.getID());
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se elimino el objeto " + obj.getID() + " (" + obj.getObjModelo()
				.getNombre() + ")");
				break;
			case "CAMBIAR_CONTRASEÑA" :
			case "CAMBIAR_CLAVE" :
			case "CHANGE_PASSWORD" :
				Cuenta consultado = null;
				if (infos.length > 1) {
					consultado = Mundo.getCuentaPorNombre(infos[1]);
				}
				if (consultado == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La cuenta no existe");
					return;
				}
				if (infos.length > 2) {
					str = infos[2];
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La contraseña no puede estar vacia");
					return;
				}
				GestorSQL.CAMBIAR_CONTRASEÑA_CUENTA(str, consultado.getID());
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La cuenta " + consultado.getNombre() + " ha cambiado su contraseña a "
				+ str);
				break;
			case "ADMIN" :
				idInt = -1;
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Arguments incorrecto");
					return;
				}
				if (idInt <= -1) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Merci d'indiquer un GM valide!");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El arguemento tiene que ser un número positivo");
					}
					return;
				}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				objetivo.getCuenta().setRango(idInt);
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le GM du joueur a été modifié!");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje " + objetivo.getNombre() + " ahora tiene GM nivel "
					+ idInt);
				}
				break;
			case "PARAM_PRECIO_RECURSOS_EN_OGRINAS" :
			case "RECURSOS_EN_OGRINAS" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_PRECIO_RECURSOS_EN_OGRINAS = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Sistema recurso de ogrinas se cambio a " + boleano);
				break;
			case "PARAM_RECETA_SIEMPRE_EXITOSA" :
			case "PARAM_CRAFT_SEGURO" :
			case "CRAFT_SEGURO" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_CRAFT_SIEMPRE_EXITOSA = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Craft Seguro se cambio a "
				+ MainServidor.PARAM_CRAFT_SIEMPRE_EXITOSA);
				break;
			case "DATOS_SQL" :
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Datos de la database: \nusuario-" + MainServidor.BD_USUARIO
				+ "\npass-" + MainServidor.BD_PASS + "\nhost-" + MainServidor.BD_HOST + "\ndb_dinamica-"
				+ MainServidor.BD_DINAMICA + "\nbd_estatica-" + MainServidor.BD_ESTATICA + "\nbd_cuentas-"
				+ MainServidor.BD_CUENTAS);
				break;
			case "COMIDA_MASCOTA" :
				int mascota = -1;
				try {
					mascota = Integer.parseInt(infos[1]);
				} catch (final Exception e1) {}
				if (mascota == -1) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos incorrectos");
					return;
				}
				final MascotaModelo masc = Mundo.getMascotaModelo(mascota);
				if (masc == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mascota nula");
					return;
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Las estadisticas de la mascota son " + masc.getStrComidas());
				break;
			case "BLOQUEAR_ATAQUE" :
			case "BLOCK_ATTACK" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e2) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
					return;
				}
				Mundo.BLOQUEANDO = boleano;
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso,
					"L'accès au serveur est bloqué le temps que les attaques se calment.");
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso,
					"Se activo medidas de bloqueo acceso al server, hasta que pare el ataque");
				}
				if (Mundo.BLOQUEANDO) {
					GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE_TODOS(
					"El Servidor esta siendo atacado, se ha activado la el ANTI-ATTACK de Elbusta, por el momento no se podran conectar al servidor, "
					+ "pero si continuar jugando, porfavor eviten salir, que en unos minutos reestablecemos la conexion al servidor, GRACIAS!!",
					"L'accès au serveur est bloqué car nous sommes attaqués, merci de ne pas vous déconnecter!");
				} else {
					GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE_TODOS(
					"El ataque ha parado, ahora el servidor desbloqueara el acceso a las cuentas, YA PUEDEN LOGUEARSE, SIN TEMOR!! GRACIAS ELBUSTA!!",
					"L'accès au serveur est rétabli!");
				}
				break;
			case "PARAM_REGISTRO_JUGADORES" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.PARAM_REGISTRO_LOGS_JUGADORES = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "PARAM_REGISTRO_JUGADORES se cambio a " + boleano);
				break;
			case "MOSTRAR_RECIBIDOS" :
			case "RECIBIDOS" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.MOSTRAR_RECIBIDOS = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mostrar recibidos se cambio a " + boleano);
				break;
			case "MOSTRAR_ENVIOS" :
			case "ENVIADOS" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.MOSTRAR_ENVIOS = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mostrar enviados se cambio a " + boleano);
				break;
			case "DEBUG" :
			case "MODO_DEBUG" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.MODO_DEBUG = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Mostrar mensajes debug se cambio a " + boleano);
				break;
			case "MODO_HEROICO" :
				try {
					boleano = infos[1].equalsIgnoreCase("true");
				} catch (final Exception e) {}
				MainServidor.MODO_HEROICO = boleano;
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El MODO_HEROICO cambio a " + boleano);
				break;
			case "ACCOUNT_PASSWORD" :
			case "CUENTA_CONTRASEÑA" :
			case "GET_PASS" :
				if (infos.length > 1) {
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				cuenta = objetivo.getCuenta();
				if (cuenta == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La cuenta es nula");
					return;
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La cuenta es " + cuenta.getNombre() + " y la contraseña es " + cuenta
				.getContraseña());
				break;
			case "BORRAR_PRISMA" :
				try {
					Mapa mapa = _perso.getMapa();
					final Prisma prisma = mapa.getSubArea().getPrisma();
					if (prisma == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Esta subArea no posee prisma");
						return;
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se borró el prisma de la subArea " + prisma.getSubArea().getID());
					prisma.murio();
				} catch (Exception e) {}
				break;
			case "SEND" :
			case "ENVIAR" :
				try {
					infos = mensaje.split(" ", 2);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El emulador ha recibido el packet " + infos[1]);
					_cuenta.getSocket().analizar_Packets(infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
				}
				break;
			case "SEND_FLOOD" :
			case "ENVIAR_FLOOD" :
				try {
					infos = mensaje.split(" ", 4);
					int veces = Integer.parseInt(infos[1]);
					int time = Integer.parseInt(infos[2]);
					if (time <= 0) {
						time = 1;
					}
					for (int i = 0; i < veces; i++) {
						_cuenta.getSocket().analizar_Packets(infos[3]);
						Thread.sleep(time);
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El emulador ha recibido el packet " + infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
				}
				break;
			case "SEND_PLAYER" :
			case "ENVIAR_PJ" :
				try {
					infos = mensaje.split(" ", 3);
					if (infos.length > 1) {
						objetivo = Mundo.getPersonajePorNombre(infos[1]);
					}
					if (objetivo == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
						}
						return;
					}
					cuenta = objetivo.getCuenta();
					if (cuenta == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La cuenta es nula");
						return;
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El emulador ha recibido del jugador " + objetivo.getNombre()
					+ " el packet " + infos[2]);
					cuenta.getSocket().analizar_Packets(infos[2]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
				}
				break;
			case "RECIVED" :
			case "RECIBIR" :
				try {
					infos = mensaje.split(" ", 2);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La core ha recibido el packet " + infos[1]);
					GestorSalida.enviarEnCola(_perso, infos[1], false);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
				}
				break;
			case "RECIBIR_TODOS" :
				try {
					infos = mensaje.split(" ", 3);
					int tiempo = Integer.parseInt(infos[1]);
					GestorSalida.enviarTodos(tiempo, infos[2]);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La core de todos han recibido el packet " + infos[2]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
				}
				break;
			case "RECIBIR_MAPA" :
				try {
					infos = mensaje.split(" ", 2);
					for (final Personaje perso : _perso.getMapa().getArrayPersonajes()) {
						GestorSalida.enviarEnCola(perso, infos[1], false);
					}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La core de los del mapa han recibido el packet " + infos[1]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
				}
				break;
			case "RECIBIRNOS" :
				try {
					infos = mensaje.split(" ", 3);
					objetivo = Mundo.getPersonajePorNombre(infos[1]);
					GestorSalida.enviarEnCola(objetivo, infos[2], false);
					GestorSalida.enviarEnCola(_perso, infos[2], false);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La core de " + infos[1] + " y tu, han recibido el packet "
					+ infos[2]);
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento incorrecto");
				}
				break;
			// case "LIDER_PVP" :
			// case "LEADER_PVP" :
			// Mundo.actualizarLiderPVP();
			// GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le meilleur joueur PVP a été mis a jour.");
			// break;
			case "ADD_ACCION_PELEA" :
			case "ADD_ACTION_FIGHT" :
			case "AGREGAR_ACCION_PELEA" :
				try {
					Pelea pelea = _perso.getPelea();
					if (pelea == null) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No te encuentras en una pelea");
						return;
					}
					infos = mensaje.split(" ", 3);
					try {
						accionID = Integer.parseInt(infos[1]);
						args = infos[2];
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumento inválido");
						return;
					}
					pelea.addAccion(new Accion(accionID, args, ""));
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La pelea agrego la accion: ID " + accionID + ", Args " + args);
				} catch (Exception e) {}
				break;
			case "STR_ACCIONES_PELEA" :
				if (_perso.getPelea() == null) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "No te encuentras en una pelea");
					return;
				}
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La acciones de esta pelea son: " + _perso.getPelea()
				.getStrAcciones());
				break;
			case "GET_PERSONAJES" :
				GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La cantidad de personajes en MundoDofus es de " + Mundo
				.getCantidadPersonajes());
				break;
			case "REGALAR_CREDITOS" :
			case "AGREGAR_CREDITOS" :
			case "DAR_CREDITOS" :
			case "ADD_CREDITS" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				GestorSQL.SET_CREDITOS_CUENTA(GestorSQL.GET_CREDITOS_CUENTA(objetivo.getCuentaID()) + idInt, objetivo
				.getCuentaID());
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, idInt + " creditos ont été ajoutés à " + objetivo.getNombre());
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se le ha agregado " + idInt + " creditos a " + objetivo
					.getNombre());
				}
				break;
			case "REGALAR_OGRINAS" :
			case "AGREGAR_OGRINAS" :
			case "DAR_OGRINAS" :
			case "ADD_POINTS" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				GestorSQL.SET_OGRINAS_CUENTA(GestorSQL.GET_OGRINAS_CUENTA(objetivo.getCuentaID()) + idInt, objetivo
				.getCuentaID());
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, idInt + " ogrines ont été ajoutés à " + objetivo.getNombre());
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se le ha agregado " + idInt + " ogrinas a " + objetivo.getNombre());
				}
				break;
			case "DAR_OGRINAS_CUENTA" :
			case "ADD_POINTS_ACCOUNT" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {}
				cuenta = _cuenta;
				if (infos.length > 2) {
					cuenta = Mundo.getCuentaPorNombre(infos[2]);
				}
				if (cuenta == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le compte pas exist.");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "La cuenta no existe");
					}
					return;
				}
				GestorSQL.SET_OGRINAS_CUENTA(GestorSQL.GET_OGRINAS_CUENTA(cuenta.getID()) + idInt, cuenta.getID());
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, idInt + " ogrines ont été ajoutés à " + cuenta.getNombre());
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se le ha agregado " + idInt + " ogrinas a " + cuenta.getNombre());
				}
				break;
			case "ABONO_MINUTES" :
			case "ABONO_MINUTOS" :
			case "DAR_ABONO_MINUTOS" :
			case "ADD_ABONO_MINUTOS" :
			case "ADD_ABONO_MINUTES" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				long abonoM = Math.max(GestorSQL.GET_ABONO(objetivo.getCuenta().getNombre()), System.currentTimeMillis());
				abonoM += (idInt * 60 * 1000l);
				abonoM = Math.max(abonoM, System.currentTimeMillis() - 1000);
				GestorSQL.SET_ABONO(abonoM, objetivo.getCuentaID());
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, idInt + " minutes abonne ont été ajoutés à " + objetivo.getNombre());
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se le ha agregado " + idInt + " minutos de abono a " + objetivo
					.getNombre());
				}
				break;
			case "ABONO_HOURS" :
			case "ABONO_HORAS" :
			case "DAR_ABONO_HORAS" :
			case "ADD_ABONO_HORAS" :
			case "ADD_ABONO_HOURS" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				long abonoH = Math.max(GestorSQL.GET_ABONO(objetivo.getCuenta().getNombre()), System.currentTimeMillis());
				abonoH += (idInt * 3600 * 1000l);
				abonoH = Math.max(abonoH, System.currentTimeMillis() - 1000);
				GestorSQL.SET_ABONO(abonoH, objetivo.getCuentaID());
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, idInt + " heures abonne ont été ajoutés à " + objetivo.getNombre());
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se le ha agregado " + idInt + " horas de abono a " + objetivo
					.getNombre());
				}
				break;
			case "ABONO_DAYS" :
			case "ABONO_DIAS" :
			case "DAR_ABONO_DIAS" :
			case "ADD_ABONO_DIAS" :
			case "ADD_ABONO_DAYS" :
				try {
					idInt = Integer.parseInt(infos[1]);
				} catch (final Exception e) {}
				objetivo = _perso;
				if (infos.length > 2) {
					objetivo = Mundo.getPersonajePorNombre(infos[2]);
				}
				if (objetivo == null) {
					if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Le joueur n'existe pas");
					} else {
						GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El personaje no existe");
					}
					return;
				}
				long abonoD = Math.max(GestorSQL.GET_ABONO(objetivo.getCuenta().getNombre()), System.currentTimeMillis());
				abonoD += (idInt * 24 * 3600 * 1000l);
				abonoD = Math.max(abonoD, System.currentTimeMillis() - 1000);
				GestorSQL.SET_ABONO(abonoD, objetivo.getCuentaID());
				if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, idInt + " jouers abonne ont été ajoutés à " + objetivo.getNombre());
				} else {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se le ha agregado " + idInt + " dias de abono a " + objetivo
					.getNombre());
				}
				break;
			case "RESETEAR_STATS_OBJETOS_MODELO" :
				try {
					ArrayList<Integer> idsObjetos = new ArrayList<>();
					for (final String s : infos[1].split(";")) {
						if (s.isEmpty()) {
							continue;
						}
						idsObjetos.add(Integer.parseInt(s));
					}
					Mundo.resetearStatsObjetos(idsObjetos);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se reseteo los objetos modelo IDs: " + infos[1]);
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
					return;
				}
				break;
			case "LISTA_DIR_CELDAS" :
				try {
					celda1 = Short.parseShort(infos[1]);
					celda2 = Short.parseShort(infos[2]);
					byte[] b = Camino.listaDirEntreDosCeldas2(_perso.getMapa(), celda1, celda2, (short) -1);
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "listaDirEntreDosCeldas2 " + b[0] + "," + b[1] + "," + b[2] + ","
					+ b[3]);
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
					return;
				}
				break;
			case "HORARIO_DIA" :
				try {
					String[] dia = infos[1].split(":");
					try {
						int h = Integer.parseInt(dia[0]);
						if (h >= 0 && h <= 23) {
							MainServidor.HORA_DIA = h;
						}
					} catch (Exception e) {}
					try {
						int h = Integer.parseInt(dia[1]);
						if (h >= 0 && h <= 59) {
							MainServidor.MINUTOS_DIA = h;
						}
					} catch (Exception e) {}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se cambio el HORARIO_DIA a " + MainServidor.HORA_DIA + ":"
					+ (MainServidor.MINUTOS_DIA < 10 ? "0" : "") + MainServidor.MINUTOS_DIA);
					for (Personaje p : Mundo.getPersonajesEnLinea()) {
						if (p.esDeDia()) {
							GestorSalida.ENVIAR_BT_TIEMPO_SERVER(p);
						}
					}
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
					return;
				}
				break;
			case "HORARIO_NOCHE" :
				try {
					String[] dia = infos[1].split(":");
					try {
						int h = Integer.parseInt(dia[0]);
						if (h >= 0 && h <= 23) {
							MainServidor.HORA_NOCHE = h;
						}
					} catch (Exception e) {}
					try {
						int h = Integer.parseInt(dia[1]);
						if (h >= 0 && h <= 59) {
							MainServidor.MINUTOS_NOCHE = h;
						}
					} catch (Exception e) {}
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Se cambio el HORARIO_NOCHE a " + MainServidor.HORA_NOCHE + ":"
					+ (MainServidor.MINUTOS_NOCHE < 10 ? "0" : "") + MainServidor.MINUTOS_NOCHE);
					for (Personaje p : Mundo.getPersonajesEnLinea()) {
						if (p.esDeNoche()) {
							GestorSalida.ENVIAR_BT_TIEMPO_SERVER(p);
						}
					}
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
					return;
				}
				break;
			case "SIMULACION_GE" :
			case "SIMULATION_GE" :
				try {
					short mapaID = 0, celdaID = 0;
					try {
						if (infos.length > 1) {
							mapaID = Short.parseShort(infos[1]);
						}
						if (infos.length > 2) {
							celdaID = Short.parseShort(infos[2]);
						}
					} catch (final Exception e) {}
					Mapa mapa = Mundo.getMapa(mapaID);
					if (mapa == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "MAPID INVALIDE!");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "El mapa a teleportar no existe");
						}
						return;
					}
					String ge = infos[3];
					if (celdaID <= -1) {
						celdaID = mapa.getRandomCeldaIDLibre();
					} else if (mapa.getCelda(celdaID) == null) {
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "CELLID INVALIDE!");
						} else {
							GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "CeldaID inválida");
						}
						return;
					}
					for (Personaje p : Mundo.getPersonajesEnLinea()) {
						p.setMapa(mapa);
						p.setCelda(mapa.getCelda(celdaID));
						GestorSalida.enviar(p, ge);
					}
				} catch (Exception e) {
					GestorSalida.ENVIAR_BAT2_CONSOLA(_perso, "Argumentos invalidos");
					return;
				}
				break;
			default :
				GM_lvl_4(comando, infos, mensaje, _cuenta, _perso);
				return;
		}
	}
	
	public static void GM_lvl_0(final String comando, String[] infos, final String mensaje, final Cuenta _cuenta,
	final Personaje _perso) {
		
	}
}
