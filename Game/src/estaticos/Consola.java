package estaticos;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import servidor.ServidorServer;
import servidor.ServidorServer.SalvarServidor;
import variables.personaje.Personaje;

public class Consola extends Thread {
	private static boolean CONSOLA_ACTIVADA = true;
	
	public Consola() {
		this.setDaemon(true);
		this.setPriority(7);
		this.start();
	}
	
	public void run() {
		while (CONSOLA_ACTIVADA) {
			try {
				final BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
				String linea = b.readLine();
				String str = "";
				try {
					String[] args = linea.split(" ", 2);
					str = args[1];
					linea = args[0];
				} catch (final Exception e2) {}
				leerComandos(linea, str);
			} catch (final Exception e) {
				System.out.println("Error al ingresar texto a la consola");
			}
		}
	}
	
	public static void leerComandos(final String linea, final String valor) {
		try {
			if (linea == null) {
				return;
			}
			switch (linea.toUpperCase()) {
				case "PARAM_STOP_SEGUNDERO" :
				case "STOP_SEGUNDERO" :
					MainServidor.PARAM_STOP_SEGUNDERO = valor.equalsIgnoreCase("true");
					break;
				case "ENVIADOS" :
					MainServidor.MOSTRAR_ENVIOS = valor.equalsIgnoreCase("true");
					break;
				case "RECIBIDOS" :
					MainServidor.MOSTRAR_RECIBIDOS = valor.equalsIgnoreCase("true");
					break;
				case "MODO_DEBUG" :
				case "DEBUG" :
					MainServidor.MODO_DEBUG = valor.equalsIgnoreCase("true");
					break;
				case "RECOLECTAR_BASURA" :
				case "GC" :
				case "GARBAGE_COLLECTOR" :
					System.out.print("INICIANDO GARGBAGE COLLECTOR ... ");
					System.gc();
					System.out.println("100%");
					break;
				case "FINISH_ALL_FIGHTS" :
				case "FINALIZAR_PELEAS" :
				case "FINISH_COMBATS" :
				case "FINISH_FIGHTS" :
					System.out.print("FINALIZANDO TODAS LAS PELEAS ... ");
					Mundo.finalizarPeleas();
					System.out.println("100%");
					break;
				case "REGISTER" :
				case "REGISTRO" :
				case "REGISTE" :
				case "REGISTRAR" :
					System.out.print("INICIANDO EL REGISTRO DE JUGADORES Y SQL ... ");
					MainServidor.imprimirLogPlayers();
					MainServidor.imprimirLogSQL();
					System.out.println("100%");
					break;
				case "REGISTER_SQL" :
				case "REGISTRO_SQL" :
				case "REGISTE_SQL" :
				case "REGISTRAR_SQL" :
					System.out.print("INICIANDO EL REGISTRO DE SQL ... ");
					MainServidor.imprimirLogSQL();
					System.out.println("100%");
					break;
				case "REGISTER_PLAYERS" :
				case "REGISTRO_PLAYERS" :
				case "REGISTE_PLAYERS" :
				case "REGISTRAR_PLAYERS" :
					System.out.print("INICIANDO EL REGISTRO DE JUGADORES ... ");
					MainServidor.imprimirLogPlayers();
					System.out.println("100%");
					break;
				case "MEMORY" :
				case "MEMORY_USE" :
				case "MEMORIA" :
				case "MEMORIA_USADA" :
				case "ESTADO_JVM" :
					System.out.println("----- ESTADO JVM -----\nFreeMemory: " + Runtime.getRuntime().freeMemory() / 1048576f
					+ " MB\nTotalMemory: " + Runtime.getRuntime().totalMemory() / 1048576f + " MB\nMaxMemory: "
					+ Runtime.getRuntime().maxMemory() / 1048576f + " MB\nProcesos: "
					+ Runtime.getRuntime().availableProcessors());
					break;
				case "DESACTIVAR" :
				case "DESACTIVE" :
				case "DESACTIVER" :
					CONSOLA_ACTIVADA = false;
					System.out.println("=============== CONSOLA DESACTIVADA ===============");
					break;
				case "INFOS" :
					long enLinea = ServidorServer.getSegundosON() * 1000;
					final int dia = (int) (enLinea / 86400000L);
					enLinea %= 86400000L;
					final int hora = (int) (enLinea / 3600000L);
					enLinea %= 3600000L;
					final int minuto = (int) (enLinea / 60000L);
					enLinea %= 60000L;
					final int segundo = (int) (enLinea / 1000L);
					System.out.println("===========\n" + MainServidor.NOMBRE_SERVER + " (ELBUSTEMU "
					+ Constantes.VERSION_EMULADOR + ")\n\nEnLínea: " + dia + "d " + hora + "h " + minuto + "m " + segundo + "s\n"
					+ "Jugadores en línea: " + ServidorServer.nroJugadoresLinea() + "\n" + "Record de conexión: "
					+ ServidorServer.getRecordJugadores() + "\n" + "===========");
					break;
				case "SAVE" :
				case "GUARDAR" :
				case "GUARDA" :
				case "SALVAR" :
					System.out.println("Salvando Servidor");
					new SalvarServidor(false);
					break;
				case "SAVE_ALL" :
				case "GUARDAR_TODOS" :
				case "SALVAR_TODOS" :
					System.out.println("Salvando Servidor ONLINE y OFFLINE");
					new SalvarServidor(true);
					break;
				case "ANNUANCE" :
				case "ANUNCIO" :
					if (!valor.isEmpty()) {
						GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE_TODOS(valor);
					}
					System.out.println("Anuncio para todos los jugadores: " + valor);
					break;
				case "SALIR" :
				case "EXIT" :
				case "RESET" :
					System.exit(0);
					break;
				case "AVALAIBLE" :
				case "THREADS" :
					System.out.println(ServidorServer.clientesDisponibles());
					break;
				case "ACCESS_ADMIN" :
				case "ACCESO_ADMIN" :
					try {
						MainServidor.ACCESO_ADMIN_MINIMO = Byte.parseByte(valor);
						System.out.println("Se limito el acceso al server a rango " + MainServidor.ACCESO_ADMIN_MINIMO);
					} catch (Exception e) {}
					break;
				case "ADMIN" :
					try {
						String[] infos = valor.split(" ");
						int id = -1;
						try {
							id = Integer.parseInt(infos[0]);
						} catch (final Exception e1) {}
						if (id <= -1) {
							System.out.println("Rango invalido");
							return;
						}
						Personaje objetivo = Mundo.getPersonajePorNombre(infos[1]);
						if (objetivo == null) {
							System.out.println("El Personaje no existe");
							return;
						}
						objetivo.getCuenta().setRango(id);
						System.out.println("El personaje " + objetivo.getNombre() + " tiene rango " + id);
					} catch (Exception e) {
						System.out.println("A ocurrido un error");
					}
					break;
				case "INICIAR_ATAQUE" :
				case "START_ATTACK" :
				case "STARTATTACK" :
					System.out.println("Start the Attack: " + valor);
					GestorSalida.enviarTodos(1, "AjI" + valor);
					break;
				case "PARAR_ATAQUE" :
				case "STOP_ATTACK" :
				case "STOPATTACK" :
					System.out.println("Stop the Attack");
					GestorSalida.enviarTodos(1, "AjP");
					break;
				case "PAQUETE_ATAQUE" :
				case "PACKET_ATTACK" :
				case "PACKETATTACK" :
					System.out.println("Send Packet of Attack: " + valor);
					GestorSalida.enviarTodos(1, "AjE" + valor);
					break;
				case "PLAYERS_DONT_DIE" :
				case "JUGADORES_NO_MORIR" :
				case "PARAM_JUGADORES_NO_MORIR" :
					MainServidor.PARAM_JUGADORES_HEROICO_MORIR = !valor.equalsIgnoreCase("true");
					System.out.println("El parametro jugadores morir esta : "
					+ (MainServidor.PARAM_JUGADORES_HEROICO_MORIR ? "activado" : "desactivado"));
					break;
				default :
					System.out.println("Comando no existe");
					return;
			}
			System.out.println("Comando realizado: " + linea + " -> " + valor);
		} catch (final Exception e) {
			System.out.println("Ocurrio un error con el comando " + linea);
		}
	}
}
