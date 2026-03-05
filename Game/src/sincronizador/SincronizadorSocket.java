package sincronizador;

import java.net.*;
import java.io.*;
import servidor.ServidorServer;
import variables.personaje.Cuenta;
import estaticos.MainServidor;
import estaticos.Encriptador;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.Mundo;

public class SincronizadorSocket implements Runnable {
	public static SincronizadorSocket CONECTOR;
	public static int INDEX_IP = 0;
	private Socket _socket;
	private BufferedInputStream _in;
	private PrintWriter _out;
	private Thread _thread;
	private int _puerto;
	private String _IP;
	
	public SincronizadorSocket() {
		_IP = MainServidor.IP_MULTISERVIDOR.get(MainServidor.INDEX_IP);
		MainServidor.ES_LOCALHOST = _IP.equals("127.0.0.1");
		MainServidor.INDEX_IP = (MainServidor.INDEX_IP + 1) % MainServidor.IP_MULTISERVIDOR.size();
		_puerto = MainServidor.PUERTO_SINCRONIZADOR;
		if (MainServidor.MOSTRAR_SINCRONIZACION) {
			System.out.println("INTENTO SINCRONIZAR IP: " + _IP + " - PUERTO: " + _puerto);
		}
		CONECTOR = this;
		try {
			_socket = new Socket(_IP, _puerto);
			_in = new BufferedInputStream((_socket.getInputStream()));
			_out = new PrintWriter(_socket.getOutputStream());
			_thread = new Thread(this);
			_thread.setDaemon(true);
			_thread.start();
		} catch (final Exception e) {
			if (MainServidor.MOSTRAR_SINCRONIZACION) {
				System.out.println("INTENTO FALLIDO -> " + e.toString());
			}
			desconectar();
		}
	}
	
	public void run() {
		try {
			System.out.println("<<<--- INICIANDO SINCRONIZADOR, IP: " + _IP + " PUERTO: " + _puerto + " --->>>");
			sendPacket("D" + MainServidor.SERVIDOR_ID + ";" + MainServidor.PUERTO_SERVIDOR + ";"
			+ MainServidor.SERVIDOR_PRIORIDAD + ";" + Mundo.SERVIDOR_ESTADO
			+ (MainServidor.IP_PUBLICA_SERVIDOR.isEmpty() ? "" : ";" + MainServidor.IP_PUBLICA_SERVIDOR), true);
			int c = -1;
			int lenght = -1;
			int index = 0;
			byte[] bytes = new byte[1];
			while ((c = _in.read()) != -1) {
				if (lenght == -1) {
					lenght = _in.available();
					bytes = new byte[lenght + 1];
					index = 0;
				}
				bytes[index++] = (byte) c;
				if (bytes.length == index) {
					String tempPacket = new String(bytes, "UTF-8");
					for (String packet : tempPacket.split("[\u0000\n\r]")) {
						if (packet.isEmpty()) {
							continue;
						}
						analizarPackets(packet);
					}
					lenght = -1;
				}
			}
		} catch (final Exception e) {
			// System.out.println("EXCEPTION RUN CONECTOR, IP: " + _IP + " PUERTO: " + _puerto);
		} finally {
			System.out.println("<<<--- CERRANDO SINCRONIZADOR, IP: " + _IP + " PUERTO: " + _puerto + " --->>>");
			try {
				// ServidorGeneral.packetClientesEscogerServer("AH" + Mundo.strParaAH());
			} catch (final Exception e) {}
			desconectar();
		}
	}
	
	public void analizarPackets(final String packet) {
		try {
			switch (packet.charAt(0)) {
				case 'I' :
					try {
						final String[] infos = packet.substring(1).split(";");
						final String ip = infos[0];
						int cantidad = ServidorServer.getIPsClientes(ip);
						sendPacket("I" + ip + ";" + cantidad, false);
					} catch (final Exception e) {}
					break;
				case 'A' :// cuenta
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								final String[] infos = packet.substring(1).split(";");
								final int id = Integer.parseInt(infos[0]);
								Cuenta cuenta = Mundo.getCuenta(id);
								if (cuenta == null) {
									GestorSQL.CARGAR_CUENTA_POR_ID(id);// cuenta nueva
									cuenta = Mundo.getCuenta(id);
								}
								if (cuenta == null) {
									MainServidor.redactarLogServidorln("SE QUIERE REGISTRAR CUENTA FALSA: " + packet);
									return;
								}
								try {
									if (cuenta.getSocket() != null) {
										GestorSalida.ENVIAR_AlEd_DESCONECTAR_CUENTA_CONECTADA(cuenta.getSocket());
										GestorSalida.ENVIAR_M0_MENSAJE_BASICOS_SVR_MUESTRA_DISCONNECT(cuenta.getSocket(), "45",
										"OTHER PLAYER CONNECTED WITH YOUR ACCOUNT", "");
										cuenta.getSocket().cerrarSocket(true, "analizarPackets()");
									}
								} catch (final Exception e) {
									e.printStackTrace();
								}
								if (MainServidor.PARAM_SISTEMA_IP_ESPERA) {
									ServidorServer.addIPEspera(infos[1]);
								}
								ServidorServer.addEsperandoCuenta(cuenta);
								sendPacket("A" + id + ";" + cuenta.getPersonajes().size(), true);
							} catch (final Exception e) {
								MainServidor.redactarLogServidorln(" EXPCETION AL CARGAR CUENTA: " + e.toString());
							}
						}
					});
					t.setDaemon(true);
					t.start();
					break;
			}
		} catch (final Exception e) {}
	}
	
	private void desconectar() {
		try {
			if (_socket != null && !_socket.isClosed()) {
				_socket.close();
			}
			if (_in != null) {
				_in.close();
			}
			if (_out != null) {
				_out.close();
			}
		} catch (final Exception e) {
			// nada
		} finally {
			CONECTOR = null;
			new SincronizadorSocket();
		}
	}
	
	public static void sendPacket(String packet, boolean imprimir) {
		if (CONECTOR == null) {
			return;
		}
		if (CONECTOR._out != null && !packet.isEmpty() && !packet.equals("" + (char) 0x00)) {
			packet = Encriptador.aUTF(packet);
			if (imprimir) {
				System.out.println("ENVIAR PACKET MULTISERVIDOR >> " + packet);
			}
			CONECTOR._out.print(packet + (char) 0x00);
			CONECTOR._out.flush();
		}
	}
}
