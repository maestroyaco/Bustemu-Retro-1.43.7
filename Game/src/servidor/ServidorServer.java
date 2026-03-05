package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import sincronizador.SincronizadorSocket;
import variables.personaje.Cuenta;
import variables.personaje.Personaje;
import estaticos.Constantes;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.MainServidor;
import estaticos.Mundo;

public class ServidorServer extends Thread {
	private static ServerSocket _serverSocket;
	private static Thread _thread;
	private static Map<String, ArrayList<ServidorSocket>> _IpsClientes = new ConcurrentHashMap<>();
	private static CopyOnWriteArrayList<ServidorSocket> _clientes = new CopyOnWriteArrayList<ServidorSocket>();
	private static CopyOnWriteArrayList<Cuenta> _cuentasEspera = new CopyOnWriteArrayList<Cuenta>();
	private static CopyOnWriteArrayList<String> _IpsEspera = new CopyOnWriteArrayList<String>();
	private static boolean _ban = true;
	private static byte _nroPub = 0, _j = 0;
	private static int _recordJugadores = 0, _alterna = 0, _segundosON = 0, _minAtaque = 5;
	private static long _tiempoBan1 = 0, _tiempoBan2 = 0;
	private static String _primeraIp = "", _segundaIp = "", _terceraIp = "";
	private static int[] _ataques = new int[5];
	
	private static void contador() {
		Timer segundero = new Timer();
		segundero.schedule(new TimerTask() {
			public void run() {
				if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
					this.cancel();
					return;
				}
				_segundosON += 1;
				SincronizadorSocket.sendPacket("C" + nroJugadoresLinea(), false);
				if (MainServidor.PARAM_STOP_SEGUNDERO) {
					return;
				}
				if (MainServidor.SEGUNDOS_REBOOT_SERVER > 0) {
					MainServidor.SEGUNDOS_REBOOT_SERVER--;
					if (MainServidor.SEGUNDOS_REBOOT_SERVER == 0) {
						new Reiniciar(1);
						this.cancel();
						return;
					} else {
						final int segundosFaltan = MainServidor.SEGUNDOS_REBOOT_SERVER;
						if (segundosFaltan % 60 == 0) {
							int minutosFaltan = segundosFaltan / 60;
							if (minutosFaltan <= 60 && (minutosFaltan % 10 == 0 || minutosFaltan <= 5)) {
								new MensajeReset(minutosFaltan + " minutes");
							}
						}
					}
				}
				if (MainServidor.PARAM_ANTI_DDOS) {
					new AntiDDOS();
				}
				if (MainServidor.PARAM_LADDER_NIVEL) {
					if (_segundosON % 60 == 0) {
						new ActualizarLadder();
					}
				}
				if (MainServidor.SEGUNDOS_INACTIVIDAD > 0) {
					if (_segundosON % 3000 == 0) { // es 3000 para q se refresque rapido
						new ExpulsarInactivos();
					}
				}
				if (MainServidor.SEGUNDOS_SALVAR > 0) {
					if (_segundosON % MainServidor.SEGUNDOS_SALVAR == 0) {
						new SalvarServidor(false);
					}
				}
				if (MainServidor.SEGUNDOS_RESET_RATES > 0) {
					if (_segundosON % MainServidor.SEGUNDOS_RESET_RATES == 0) {
						new ResetRates();
					}
				}
				if (MainServidor.SEGUNDOS_LIVE_ACTION > 0) {
					if (_segundosON % MainServidor.SEGUNDOS_LIVE_ACTION == 0) {
						new LiveAction();
					}
				}
				if (MainServidor.SEGUNDOS_LIMPIAR_MEMORIA > 0) {
					if (_segundosON % MainServidor.SEGUNDOS_LIMPIAR_MEMORIA == 0) {
						new GarbageCollector();
					}
				}
				if (MainServidor.SEGUNDOS_ESTRELLAS_GRUPO_MOBS > 0) {
					if (_segundosON % MainServidor.SEGUNDOS_ESTRELLAS_GRUPO_MOBS == 0) {
						new SubirEstrellas();
					}
				}
				if (MainServidor.SEGUNDOS_DETECTAR_DDOS > 0) {
					if (_segundosON % MainServidor.SEGUNDOS_DETECTAR_DDOS == 0) {
						new DetectarDDOS();
					}
				}
				if (MainServidor.SEGUNDOS_MOVER_MONTURAS > 0) {
					if (_segundosON % MainServidor.SEGUNDOS_MOVER_MONTURAS == 0) {
						new MoverPavos();
					}
				}
				if (MainServidor.SEGUNDOS_MOVER_GRUPO_MOBS > 0) {
					if (_segundosON % MainServidor.SEGUNDOS_MOVER_GRUPO_MOBS == 0) {
						new MoverMobs();
					}
				}
				if (MainServidor.SEGUNDOS_PUBLICIDAD > 0) {
					if (_segundosON % MainServidor.SEGUNDOS_PUBLICIDAD == 0) {
						new Publicidad();
					}
				}
				if (MainServidor.PARAM_LOTERIA) {
					if (Mundo.SEG_CUENTA_REGRESIVA > 0) {
						if (--Mundo.SEG_CUENTA_REGRESIVA == 0) {
							if (Mundo.MSJ_CUENTA_REGRESIVA.equalsIgnoreCase("LOTERIA")) {
								new SortearLoteria();
							} else {
								GestorSalida.ENVIAR_ÑL_BOTON_LOTERIA_TODOS(false);
								new BorrarCuentaRegresiva();
							}
						}
					} else if (_segundosON % 3600 == 0) {
						new IniciarLoteria();
					}
				} else {
					if (Mundo.SEG_CUENTA_REGRESIVA > 0) {
						if (--Mundo.SEG_CUENTA_REGRESIVA == 0) {
							new BorrarCuentaRegresiva();
						}
					}
				}
				if (MainServidor.PARAM_KOLISEO) {
					Mundo.SEGUNDOS_INICIO_KOLISEO--;
					if (Mundo.SEGUNDOS_INICIO_KOLISEO == 60) {
						GestorSalida.ENVIAR_Im_INFORMACION_KOLISEO("1KOLISEO_UN_MINUTO_INICIA");
					} else if (Mundo.SEGUNDOS_INICIO_KOLISEO == 5) {
						// 5 segundos
					} else if (Mundo.SEGUNDOS_INICIO_KOLISEO == 0) {
						new Koliseo();
					}
				}
				// new EmbarazoMonturas();
				// cada segundo
				new DisminuirFatiga();
				new CheckearObjInteractivos();
				new MoverRecaudadores();
				int dia = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
				if (Mundo.DIA_DEL_AÑO != dia) {
					Mundo.DIA_DEL_AÑO = dia;
					new ResetExpDia();
				}
			}
		}, 1000, 1000);
		Timer autoSelect = new Timer();
		autoSelect.schedule(new TimerTask() {
			public void run() {
				if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
					this.cancel();
					return;
				}
				GestorSQL.ES_IP_BANEADA("111.222.333.444");// para usar el sql y q no se crashee
			}
		}, 300000, 300000);
	}
	
	public ServidorServer() {
		try {
			// _tiempoInicio = System.currentTimeMillis();
			contador();
			// muestra en el banner el tiempo para reboot
			if (!MainServidor.MENSAJE_TIMER_REBOOT.isEmpty() && MainServidor.SEGUNDOS_REBOOT_SERVER > 0) {
				Mundo.SEG_CUENTA_REGRESIVA = MainServidor.SEGUNDOS_REBOOT_SERVER;
				Mundo.MSJ_CUENTA_REGRESIVA = MainServidor.MENSAJE_TIMER_REBOOT;
			}
			if (MainServidor.PARAM_LOTERIA) {
				new IniciarLoteria();
			}
			_serverSocket = new ServerSocket(MainServidor.PUERTO_SERVIDOR);
			_thread = new Thread(this);
			_thread.setDaemon(true);
			_thread.setPriority(MAX_PRIORITY);
			_thread.start();
			System.out.println("Aperturado el Servidor, PUERTO SERVIDOR " + MainServidor.PUERTO_SERVIDOR);
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("ERROR AL CREAR EL SERVIDOR PERSONAJE" + e.toString());
			e.printStackTrace();
			// System.exit(1);
		}
	}
	
	public void run() {
		try {
			while (true) {
				final Socket socket = _serverSocket.accept();
				_ataques[_j]++;
				final String ip = socket.getInetAddress().getHostAddress();
				if (MainServidor.PARAM_MOSTRAR_IP_CONECTANDOSE || MainServidor.MODO_DEBUG) {
					System.out.println("SE ESTA CONECTANDO LA IP " + ip);
				}
				if (Mundo.BLOQUEANDO) {
					if (!socket.isClosed()) {
						socket.close();
					}
					continue;
				}
				if (GestorSQL.ES_IP_BANEADA(ip)) {
					if (!socket.isClosed()) {
						socket.close();
					}
					continue;
				}
				if (MainServidor.PARAM_ANTI_DDOS) {
					_alterna += 1;
					if (_alterna == 1) {
						_primeraIp = ip;
						if (_ban) {
							_tiempoBan1 = System.currentTimeMillis();
						} else {
							_tiempoBan2 = System.currentTimeMillis();
						}
						_ban = !_ban;
					} else if (_alterna == 2) {
						_segundaIp = ip;
						if (_ban) {
							_tiempoBan1 = System.currentTimeMillis();
						} else {
							_tiempoBan2 = System.currentTimeMillis();
						}
						_ban = !_ban;
					} else {
						_terceraIp = ip;
						_alterna = 0;
						if (_ban) {
							_tiempoBan1 = System.currentTimeMillis();
						} else {
							_tiempoBan2 = System.currentTimeMillis();
						}
						_ban = !_ban;
					}
					if (_primeraIp.equals(ip) && _segundaIp.equals(ip) && _terceraIp.equals(ip) && Math.abs(_tiempoBan1
					- _tiempoBan2) < 100) {
						GestorSQL.INSERT_BAN_IP(ip);
						if (!socket.isClosed()) {
							socket.close();
						}
						continue;
					}
				}
				new ServidorSocket(socket);
			}
		} catch (final IOException e) {
			MainServidor.redactarLogServidorln("EXCEPTION IO RUN SERVIDOR SERVER (FOR COMMAND EXIT)");
			// e.printStackTrace();
			// System.exit(1);
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION GENERAL RUN SERVIDOR SERVER : " + e.toString());
			e.printStackTrace();
			// System.exit(1);
		} finally {
			try {
				if (!_serverSocket.isClosed()) {
					_serverSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			MainServidor.redactarLogServidorln("FINALLY SERVIDOR SERVER - CERRANDO SERVIDOR");
		}
	}
	
	public static int getSegundosON() {
		return _segundosON;
	}
	
	// private static long getTiempoInicio() {
	// return _tiempoInicio;
	// }
	public static int getRecordJugadores() {
		return _recordJugadores;
	}
	
	public static void cerrarSocketServidor() {
		try {
			for (final ServidorSocket ep : _clientes) {
				ep.cerrarSocket(false, "cerrarServidor()");
			}
			MainServidor.redactarLogServidor(" SE EXPULSARON LOS CLIENTES ");
			if (!_serverSocket.isClosed()) {
				_serverSocket.close();
			}
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION CERRAR SERVIDOR : " + e.toString());
			e.printStackTrace();
		}
	}
	
	public static CopyOnWriteArrayList<ServidorSocket> getClientes() {
		return _clientes;
	}
	
	public static ServidorSocket getCliente(final int b) {
		return _clientes.get(b);
	}
	
	public static void addIPsClientes(ServidorSocket s) {
		String ip = s.getActualIP();
		if (_IpsClientes.get(ip) == null) {
			_IpsClientes.put(ip, new ArrayList<ServidorSocket>());
		}
		if (!_IpsClientes.get(ip).contains(s)) {
			_IpsClientes.get(ip).add(s);
		}
	}
	
	public static void borrarIPsClientes(ServidorSocket s) {
		String ip = s.getActualIP();
		if (_IpsClientes.get(ip) == null) {
			return;
		}
		_IpsClientes.get(ip).remove(s);
	}
	
	public static int getIPsClientes(String ip) {
		if (_IpsClientes.get(ip) == null) {
			return 0;
		}
		return _IpsClientes.get(ip).size();
	}
	
	public static void addCliente(ServidorSocket socket) {
		if (socket == null) {
			return;
		}
		addIPsClientes(socket);
		_clientes.add(socket);
		if (_clientes.size() > _recordJugadores) {
			_recordJugadores = _clientes.size();
		}
	}
	
	public static void borrarCliente(final ServidorSocket socket) {
		if (socket == null) {
			return;
		}
		borrarIPsClientes(socket);
		_clientes.remove(socket);
	}
	
	public static int nroJugadoresLinea() {
		return _clientes.size();
	}
	
	public static void delEsperandoCuenta(final Cuenta cuenta) {
		if (cuenta == null) {
			return;
		}
		_cuentasEspera.remove(cuenta);
	}
	
	public static void addEsperandoCuenta(final Cuenta cuenta) {
		if (cuenta != null) {
			_cuentasEspera.add(cuenta);
		}
	}
	
	public static Cuenta getEsperandoCuenta(final int id) {
		for (final Cuenta cuenta : _cuentasEspera) {
			if (cuenta.getID() == id) {
				return cuenta;
			}
		}
		return null;
	}
	
	public static boolean borrarIPEspera(final String ip) {
		return _IpsEspera.remove(ip);
	}
	
	public static void addIPEspera(final String ip) {
		_IpsEspera.add(ip);
	}
	
	public static String clientesDisponibles() {
		final ArrayList<String> IPs = new ArrayList<>();
		for (final ServidorSocket ep : _clientes) {
			try {
				if (!IPs.contains(ep.getActualIP())) {
					IPs.add(ep.getActualIP());
				}
			} catch (final Exception e) {}
		}
		return "IP Availables for attack: " + IPs.size();
	}
	
	public static String listaClientesBug(int segundos) {
		StringBuilder str = new StringBuilder();
		for (final ServidorSocket ep : _clientes) {
			try {
				if (ep.getPersonaje() != null) {
					if (!ep.getPersonaje().enLinea()) {
						ep.cerrarSocket(true, "listaClientesBug(1)");
						str.append("\n" + ep.getActualIP());
					}
				} else {
					if (System.currentTimeMillis() - ep.getTiempoUltPacket() > segundos * 1000) {
						ep.cerrarSocket(true, "listaClientesBug(2)");
						str.append("\n" + ep.getActualIP());
					}
				}
			} catch (final Exception e) {}
		}
		return str.toString();
	}
	
	public static int borrarClientesBug(int segundos) {
		int i = 0;
		for (final ServidorSocket ep : _clientes) {
			try {
				if (ep.getPersonaje() != null) {
					if (!ep.getPersonaje().enLinea()) {
						ep.cerrarSocket(true, "borrarClientesBug(1)");
						i++;
					}
				} else {
					if (System.currentTimeMillis() - ep.getTiempoUltPacket() > (segundos * 1000)) {
						ep.cerrarSocket(true, "borrarClientesBug(2)");
						i++;
					}
				}
			} catch (final Exception e) {}
		}
		return i;
	}
	
	public static String getHoraHoy(Personaje perso) {
		final Calendar hoy = Calendar.getInstance();
		if (perso.esDeDia()) {
			hoy.set(Calendar.HOUR_OF_DAY, MainServidor.HORA_DIA);
			hoy.set(Calendar.MINUTE, MainServidor.MINUTOS_DIA);
		} else if (perso.esDeNoche()) {
			hoy.set(Calendar.HOUR_OF_DAY, MainServidor.HORA_NOCHE);
			hoy.set(Calendar.MINUTE, MainServidor.MINUTOS_NOCHE);
		}
		return "BT" + (hoy.getTimeInMillis());// + hoy.getTimeZone().getRawOffset()
	}
	
	public static String getFechaHoy() {
		final Calendar hoy = Calendar.getInstance();
		String dia = hoy.get(Calendar.DAY_OF_MONTH) + "";
		while (dia.length() < 2) {
			dia = "0" + dia;
		}
		String mes = (hoy.get(Calendar.MONTH)) + "";
		while (mes.length() < 2) {
			mes = "0" + mes;
		}
		int año = hoy.get(Calendar.YEAR);
		return "BD" + año + "|" + mes + "|" + dia;
	}
	public static class Reiniciar extends Thread {
		private static boolean ACTIVO = false;
		private int _i = 0;
		
		public Reiniciar(int i) {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			_i = i;
			this.setDaemon(true);
			this.setPriority(10);
			this.start();
		}
		
		public void run() {
			System.exit(_i);
			ACTIVO = false;
		}
	}
	public static class SalvarServidor extends Thread {
		private static boolean ACTIVO = false;
		private boolean _inclusoOffline = false;
		
		public SalvarServidor(boolean inclusoOffline) {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			_inclusoOffline = inclusoOffline;
			this.setDaemon(true);
			this.setPriority(9);
			this.start();
		}
		
		public void run() {
			if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_SALVANDO) {
				MainServidor.redactarLogServidorln(
				"Se esta intentando salvar el servidor, cuando este ya se esta salvando (MUNDO DOFUS)");
			} else {
				GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1164");
				Mundo.salvarServidor(_inclusoOffline);
				GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1165");
			}
			ACTIVO = false;
		}
	}
	public static class RefrescarTodosMobs extends Thread {
		private static boolean ACTIVO = false;
		
		public RefrescarTodosMobs() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			Mundo.refrescarTodosMobs();
			ACTIVO = false;
		}
	}
	private static class ResetRates extends Thread {
		private static boolean ACTIVO = false;
		
		public ResetRates() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			MainServidor.resetRates();
			GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1FINISH_SUPER_RATES");
			MainServidor.SEGUNDOS_RESET_RATES = 0;
			ACTIVO = false;
		}
	}
	private static class DisminuirFatiga extends Thread {
		private static boolean ACTIVO = false;
		
		public DisminuirFatiga() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			Mundo.disminuirFatigaMonturas();
			ACTIVO = false;
		}
	}
	private static class CheckearObjInteractivos extends Thread {
		private static boolean ACTIVO = false;
		
		public CheckearObjInteractivos() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			Mundo.checkearObjInteractivos();
			ACTIVO = false;
		}
	}
	private static class MoverPavos extends Thread {
		private static boolean ACTIVO = false;
		
		public MoverPavos() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			Mundo.moverMonturas();
			ACTIVO = false;
		}
	}
	private static class MoverRecaudadores extends Thread {
		private static boolean ACTIVO = false;
		
		public MoverRecaudadores() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			Mundo.moverRecaudadores();
			ACTIVO = false;
		}
	}
	private static class MoverMobs extends Thread {
		private static boolean ACTIVO = false;
		
		public MoverMobs() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			Mundo.moverMobs();
			ACTIVO = false;
		}
	}
	private static class ResetExpDia extends Thread {
		private static boolean ACTIVO = false;
		
		public ResetExpDia() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			Mundo.resetExpDia();
			ACTIVO = false;
		}
	}
	private static class SubirEstrellas extends Thread {
		private static boolean ACTIVO = false;
		
		public SubirEstrellas() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			Mundo.subirEstrellasMobs(1);
			ACTIVO = false;
		}
	}
	private static class IniciarLoteria extends Thread {
		private static boolean ACTIVO = false;
		
		public IniciarLoteria() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			Mundo.iniciarLoteria();
			ACTIVO = false;
		}
	}
	private static class SortearLoteria extends Thread {
		private static boolean ACTIVO = false;
		
		public SortearLoteria() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			Mundo.sortearBoletos();
			ACTIVO = false;
		}
	}
	private static class BorrarCuentaRegresiva extends Thread {
		private static boolean ACTIVO = false;
		
		public BorrarCuentaRegresiva() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			if (MainServidor.SEGUNDOS_REBOOT_SERVER > 0) {
				Mundo.SEG_CUENTA_REGRESIVA = MainServidor.SEGUNDOS_REBOOT_SERVER;
			}
			Mundo.MSJ_CUENTA_REGRESIVA = MainServidor.MENSAJE_TIMER_REBOOT;
			GestorSalida.ENVIAR_bRS_PARAR_CUENTA_REGRESIVA_TODOS();
			ACTIVO = false;
		}
	}
	private static class ActualizarLadder extends Thread {
		private static boolean ACTIVO = false;
		
		public ActualizarLadder() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			Mundo.actualizarRankings();
			ACTIVO = false;
		}
	}
	private static class Koliseo extends Thread {
		private static boolean ACTIVO = false;
		
		public Koliseo() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			Mundo.iniciarKoliseo();
			ACTIVO = false;
		}
	}
	private static class LiveAction extends Thread {
		private static boolean ACTIVO = false;
		
		public LiveAction() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			GestorSQL.CARGAR_LIVE_ACTION();
			GestorSQL.VACIAR_LIVE_ACTION();
			ACTIVO = false;
		}
	}
	private static class ExpulsarInactivos extends Thread {
		private static boolean ACTIVO = false;
		
		public ExpulsarInactivos() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(6);
			this.start();
		}
		
		public void run() {
			Mundo.expulsarInactivos();
			ACTIVO = false;
		}
	}
	private static class GarbageCollector extends Thread {
		private static boolean ACTIVO = false;
		
		public GarbageCollector() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(8);
			this.start();
		}
		
		public void run() {
			System.gc();
			ACTIVO = false;
		}
	}
	private static class MensajeReset extends Thread {
		private static boolean ACTIVO = false;
		private String _str;
		
		public MensajeReset(final String str) {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			_str = str;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("115;" + _str);
			ACTIVO = false;
		}
	}
	private static class Publicidad extends Thread {
		private static boolean ACTIVO = false;
		private String _str;
		
		public Publicidad() {
			if (MainServidor.PUBLICIDAD.isEmpty()) {
				return;
			}
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			_str = MainServidor.PUBLICIDAD.get(_nroPub);
			_nroPub += 1;
			if (_nroPub >= MainServidor.PUBLICIDAD.size()) {
				_nroPub = 0;
			}
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE_TODOS(_str);
			ACTIVO = false;
		}
	}
	private static class AntiDDOS extends Thread {
		private static boolean ACTIVO = false;
		
		public AntiDDOS() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			if (!Mundo.BLOQUEANDO && _ataques[0] > _minAtaque && _ataques[1] > _minAtaque && _ataques[2] > _minAtaque
			&& _ataques[3] > _minAtaque && _ataques[4] > _minAtaque) {
				Mundo.BLOQUEANDO = true;
				MainServidor.redactarLogServidorln("SE ACTIVO EL BLOQUEO AUTOMATICO CONTRA ATAQUES");
			} else if (Mundo.BLOQUEANDO && _ataques[0] < _minAtaque && _ataques[1] < _minAtaque && _ataques[2] < _minAtaque
			&& _ataques[3] < _minAtaque && _ataques[4] < _minAtaque) {
				try {
					for (final ServidorSocket ss : _clientes) {
						if (ss.getPersonaje() == null) {
							// expulsa a los q no tienen personajes
							GestorSalida.ENVIAR_M0_MENSAJE_BASICOS_SVR_MUESTRA_DISCONNECT(ss, "16", "", "");
							ss.cerrarSocket(true, "ServidorServer.run()");
						}
					}
				} catch (final Exception e) {}
				Mundo.BLOQUEANDO = false;
				MainServidor.redactarLogServidorln("SE DESACTIVO EL BLOQUEO AUTOMATICO CONTRA ATAQUES");
			}
			_j = (byte) (_segundosON % 5);
			_ataques[_j] = 0;
			ACTIVO = false;
		}
	}
	private static class DetectarDDOS extends Thread {
		private static boolean ACTIVO = false;
		
		public DetectarDDOS() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			try {
				if (MainServidor.URL_DETECTAR_DDOS.isEmpty()) {
					return;
				}
				URL obj = new URL(MainServidor.URL_DETECTAR_DDOS);
				URLConnection con = obj.openConnection();
				con.setRequestProperty("Content-type", "charset=Unicode");
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				while ((in.readLine()) != null) {
					Thread.sleep(1);
				}
				in.close();
				if (!MainServidor.PARAM_JUGADORES_HEROICO_MORIR) {
					MainServidor.redactarLogServidorln("============= SE HA FINALIZADO ATAQUE DDOS (" + new Date()
					+ ") =============");
					MainServidor.PARAM_JUGADORES_HEROICO_MORIR = true;
					GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1SERVER_RESTORING_ATTACK");
				}
			} catch (MalformedURLException | UnknownHostException e1) {
				if (MainServidor.PARAM_JUGADORES_HEROICO_MORIR) {
					MainServidor.redactarLogServidorln("============= SE DETECTO ATAQUE DDOS (" + new Date() + ") =============");
					MainServidor.PARAM_JUGADORES_HEROICO_MORIR = false;
					GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1SERVER_IS_BEING_ATTACKED");
					Mundo.salvarServidor(false);
				}
			} catch (Exception e) {
				MainServidor.redactarLogServidorln("EXCEPTION DE DETECTAR DDOS: " + e.toString());
				e.printStackTrace();
			}
			ACTIVO = false;
		}
	}
}
