package servidor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.swing.Timer;
import variables.casa.Casa;
import variables.gremio.Gremio;
import variables.gremio.MiembroGremio;
import variables.gremio.Recaudador;
import variables.hechizo.StatHechizo;
import variables.mapa.Celda;
import variables.mapa.Cercado;
import variables.mapa.Mapa;
import variables.mercadillo.Mercadillo;
import variables.mision.MisionModelo;
import variables.mision.MisionObjetivoModelo;
import variables.montura.Montura;
import variables.montura.Montura.Ubicacion;
import variables.npc.NPC;
import variables.npc.NPCModelo;
import variables.npc.PreguntaNPC;
import variables.npc.RespuestaNPC;
import variables.npc.Trueque;
import variables.objeto.CreaTuItem;
import variables.objeto.MascotaModelo;
import variables.objeto.Objeto;
import variables.objeto.ObjetoModelo;
import variables.objeto.ObjetoModelo.CAPACIDAD_STATS;
import variables.oficio.StatOficio;
import variables.oficio.Trabajo;
import variables.pelea.Luchador;
import variables.pelea.Pelea;
import variables.personaje.Cuenta;
import variables.personaje.Grupo;
import variables.personaje.GrupoKoliseo;
import variables.personaje.Intercambio;
import variables.personaje.Personaje;
import variables.personaje.SetRapido;
import variables.zotros.Accion;
import variables.zotros.Almanax;
import variables.zotros.Ornamento;
import variables.zotros.Prisma;
import variables.zotros.Servicio;
import variables.zotros.TiendaCategoria;
import variables.zotros.TiendaObjetos;
import variables.zotros.Titulo;
import variables.zotros.Tutorial;
import estaticos.Camino;
import estaticos.Comandos;
import estaticos.Condiciones;
import estaticos.Constantes;
import estaticos.Encriptador;
import estaticos.Formulas;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.MainServidor;
import estaticos.Mundo;
import servidor.ServidorSocket.AccionDeJuego;
import sprites.Preguntador;

public class ServidorSocket implements Runnable {
	private static HashMap<String, Integer> POSIBLES_ATAQUES = new HashMap<>();
	public static Map<String, StringBuilder> REGISTROS = new ConcurrentHashMap<>();
	public static ArrayList<String> JUGADORES_REGISTRAR = new ArrayList<String>();
	public static ArrayList<Integer> RASTREAR_CUENTAS = new ArrayList<Integer>();
	public static ArrayList<String> RASTREAR_IPS = new ArrayList<String>();
	//
	// dinamicos
	//
	private BufferedInputStream _in;
	private PrintWriter _out;
	private Socket _socket;
	private Thread _thread;
	private Cuenta _cuenta;
	private Personaje _perso;
	private String _IP, _ultimoPacket;
	private boolean _votarDespuesPelea = false;
	private Map<Integer, AccionDeJuego> _accionesDeJuego = new TreeMap<Integer, AccionDeJuego>();
	private long _tiempoUltComercio, _tiempoUltReclutamiento, _tiempoUltAlineacion, _tiempoUltIncarnam, _tiempoUltVIP,
	_ultSalvada, _tiempoUltPacket, _tiempoLLegoMapa, _tiempoUltAll;
	private long _ping, _lastMillis, _ultMillis;
	private byte _excesoPackets = 0, _sigPacket = 0;
	private final String[] _ultPackets = new String[7];
	private final long[] _timePackets = new long[7];
	private Timer _timerAcceso;
	private boolean _realizaciandoAccion;
	private String[] _aKeys = new String[16];
	private int _currentKey = -1;
	
	public ServidorSocket(final Socket socket) {
		try {
			_socket = socket;
			_IP = _socket.getInetAddress().getHostAddress();
			_in = new BufferedInputStream((_socket.getInputStream()));
			_out = new PrintWriter(_socket.getOutputStream());
			ServidorServer.addCliente(this);
			if (Mundo.BLOQUEANDO) {
				GestorSalida.ENVIAR_M0_MENSAJE_BASICOS_SVR_MUESTRA_DISCONNECT(this, "16", "", "");
				cerrarSocket(false, "ServidorSocket(1)");
			} else if (MainServidor.PARAM_SISTEMA_IP_ESPERA && !ServidorServer.borrarIPEspera(_IP)) {
				// defecto en la seguridad de tu conexion
				GestorSalida.ENVIAR_M0_MENSAJE_BASICOS_SVR_MUESTRA_DISCONNECT(this, "29", "", "");
				MainServidor.redactarLogServidorln("IP SIN ESPERA (posible ataque): " + _IP);
				posibleAtaque();
				cerrarSocket(false, "ServidorSocket(2)");
			} else {
				GestorSalida.ENVIAR_XML_POLICY_FILE(this);
				_thread = new Thread(this);
				_thread.setDaemon(true);
				_thread.setPriority(4);
				_thread.start();
			}
		} catch (final IOException e) {
			cerrarSocket(false, "ServidorSocket(3)");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	private void posibleAtaque() {
		if (POSIBLES_ATAQUES.get(_IP) != null) {
			int veces = POSIBLES_ATAQUES.get(_IP);
			if (veces > MainServidor.VECES_PARA_BAN_IP_SIN_ESPERA) {
				GestorSQL.INSERT_BAN_IP(_IP);
			} else {
				POSIBLES_ATAQUES.put(_IP, veces++);
			}
		} else {
			POSIBLES_ATAQUES.put(_IP, 0);
		}
	}
	
	private void crearTimerAcceso() {
		_timerAcceso = new Timer(10 * 1000, new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				MainServidor.redactarLogServidorln("TIMER ACCEDER SERVER AGOTADO (posible ataque): " + _IP);
				posibleAtaque();
				cerrarSocket(true, "crearTimerAcceso()");
			}
		});
	}
	
	private String crearPacketKey() {
		if (MainServidor.PARAM_ENCRIPTAR_PACKETS) {
			_currentKey = Formulas.getRandomInt(1, 15);
			String key = Encriptador.crearKey(16);
			_aKeys[_currentKey] = Encriptador.prepareKey(key);
			return Integer.toHexString(_currentKey).toUpperCase() + key;
		}
		return "0";
	}
	
	public void run() {  
	    try {  
	        GestorSalida.ENVIAR_HG_SALUDO_JUEGO_GENERAL(this);  
	        // GestorSalida.ENVIAR_AK_KEY_ENCRIPTACION_PACKETS(this, crearPacketKey());  
	        if (MainServidor.PARAM_TIMER_ACCESO) {  
	            crearTimerAcceso();  
	            _timerAcceso.start();  
	        }  
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
	                      
	                    // Manejar packets con formato ù del cliente 1.43.7  
	                    boolean esPacket143 = false;  
	                    if (packet.contains("ù")) {  
	                        String[] parts = packet.split("ù");  
	                        if (parts.length >= 3) {  
	                            // parts[0] = Base64 (ignorar)  
	                            // parts[1] = checksum (ignorar)  
	                            // parts[2] = comando real (ya desencriptado)  
	                            packet = parts[2];  
	                            esPacket143 = true;  
	                        }  
	                    }  
	                      
	                    // Solo desencriptar si NO es un packet 1.43.7 Y si está habilitada la encriptación  
	                    if (!esPacket143 && MainServidor.PARAM_ENCRIPTAR_PACKETS) {  
	                        packet = Encriptador.unprepareData(packet, _currentKey, _aKeys);  
	                    }  
	                      
	                    if (MainServidor.MOSTRAR_RECIBIDOS) {  
	                        System.out.println("<<RECIBIR PERSONAJE:  " + packet);  
	                    }  
	                    rastrear(packet);  
	                    registrar("===>> " + packet);  
	                    analizar_Packets(packet);  
	                }  
	                lenght = -1;  
	            }  
	        }  
	    } catch (final IOException e) {  
	        // registrar("<===> " + " IOException " + e.toString());  
	    } catch (final Exception e) {  
	        registrar("<===> " + " Exception " + e.toString());  
	        e.printStackTrace();  
	    } finally {  
	        cerrarSocket(true, "ServidorSocket.run()");  
	    }  
	}
	
	public void enviarPWSinEncriptar(String packet) {
		enviarPW(packet, true, false);
	}
	
	public void enviarPW(String packet) {
		enviarPW(packet, false, true);
	}
	
	public void enviarPW(String p, boolean redactar, boolean encriptado) {
		if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
			return;
		}
		for (String packet : p.split("" + (char) 0x00)) {
			if (_out == null || packet.isEmpty()) {
				continue;
			}
			if (MainServidor.PARAM_ENCRIPTAR_PACKETS && encriptado) {
				// System.out.println("ANTES DE ENCRIPTAR " + packet);
				packet = Encriptador.prepareData(packet, _currentKey, _aKeys);
				// System.out.println("DESPUES DE ENCRIPTAR " + packet);
			}
			packet = Encriptador.aUTF(packet);
			_out.print(packet + (char) 0x00);
			_out.flush();
			if (redactar && _perso != null) {
				_perso.registrar("<<=== " + packet);
			}
		}
	}
	
	public void cerrarSocket(boolean cuenta, String n) {
		try {
			if (MainServidor.MODO_DEBUG) {
				System.out.println("CERRAR SOCKET " + n);
			}
			ServidorServer.borrarCliente(this);
			ServidorServer.delEsperandoCuenta(_cuenta);
			try {
				if (_timerAcceso != null && _timerAcceso.isRunning()) {
					_timerAcceso.stop();
				}
			} catch (final Exception e) {
				MainServidor.redactarLogServidorln("EXCEPTION al cerrar socket [" + _IP + "]: timer " + e.toString());
			}
			if (_in != null) {
				_in.close();
			}
			if (_out != null) {
				_out.close();
			}
			if (_socket != null) {
				if (cuenta && _cuenta != null) {
					registrar("<===> " + "DESCONECTANDO CON ULTIMO PACKET " + _ultimoPacket);
					_cuenta.desconexion();
				}
				if (_socket != null) {
					_socket.close();
				}
			}
			_socket = null;
			// _cuenta = null;
			// _perso = null;
			// _timerAcceso = null;
			// _accionesDeJuego = null;
			// _thread.interrupt();
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION al cerrar servidor socket " + e.toString());
			e.printStackTrace();
		}
	}
	
	private void rastrear(final String packet) {
		try {
			if (RASTREAR_IPS.contains(_IP) || (_cuenta != null && RASTREAR_CUENTAS.contains(_cuenta.getID()))) {
				if (_perso != null) {
					MainServidor.redactarLogServidorln("[" + _perso.getNombre() + "] " + packet.toString());
				} else if (_cuenta != null) {
					MainServidor.redactarLogServidorln("<<" + _cuenta.getNombre() + ">> " + packet.toString());
				} else {
					MainServidor.redactarLogServidorln("{" + _IP + "} " + packet.toString());
				}
			}
		} catch (Exception e) {}
	}
	
	public void registrar(final String packet) {
		try {
			if (_cuenta != null && !_cuenta.getSinco()) {
				if (REGISTROS.get(_cuenta.getNombre()) == null) {
					REGISTROS.put(_cuenta.getNombre(), new StringBuilder());
				}
				REGISTROS.get(_cuenta.getNombre()).append(System.currentTimeMillis() + " - " + new Date(System
				.currentTimeMillis()) + " : \t" + packet + "\n");
			}
		} catch (Exception e) {}
	}
	
	public long getPing() {
		return _ping;
	}
	
	private void registrarUltPing() {
		_ultMillis++;
		enviarPW("rpong" + _ultMillis);
		_lastMillis = System.currentTimeMillis();
	}
	
	public long getTiempoUltPacket() {
		return _tiempoUltPacket;
	}
	
	public String getActualIP() {
		return _IP;
	}
	
	public PrintWriter getOut() {
		return _out;
	}
	
	public Socket getSock() {
		return _socket;
	}
	
	public Thread getThread() {
		return _thread;
	}
	
	public Personaje getPersonaje() {
		return _perso;
	}
	
	public Cuenta getCuenta() {
		return _cuenta;
	}
	
	public void analizar_Packets(final String packet) {
		if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
			return;
		}
		if (packet.equals("<policy-file-request/>")) {
			GestorSalida.ENVIAR_XML_POLICY_FILE(this);
			return;
		}
		if (packet.charAt(0) != 'A' && _perso == null) {
			GestorSalida.ENVIAR_BN_NADA(this);
			return;
		}
		_tiempoUltPacket = System.currentTimeMillis();
		_ultimoPacket = packet;
		if (antiFlood(packet)) {
			return;
		}
		switch (packet.charAt(0)) {
			case 'A' :// cuenta
				analizar_Cuenta(packet);
				break;
			case 'B' :// basic
				analizar_Basicos(packet);
				break;
			case 'C' :// conquista
				analizar_Conquista(packet);
				break;
			case 'c' :// canal
				analizar_Canal(packet);
				break;
			case 'D' :// dialogo
				analizar_Dialogos(packet);
				break;
			case 'd' :// dialogo
				analizar_Documentos(packet);
				break;
			case 'E' :// intercambio
				analizar_Intercambios(packet);
				break;
			case 'e' :// entorno, ambiente
				analizar_Ambiente(packet);
				break;
			case 'F' :// amigo
				analizar_Amigos(packet);
				break;
			case 'f' :// pelea
				analizar_Peleas(packet);
				break;
			case 'G' :// juego
				analizar_Juego(packet);
				break;
			case 'g' :// gremio
				analizar_Gremio(packet);
				break;
			case 'h' :// casa
				analizar_Casas(packet);
				break;
			case 'i' :// enemigo
				analizar_Enemigos(packet);
				break;
			case 'I' :// descripcion de la ventana dofus
				break;
			case 'J' :// oficios
				analizar_Oficios(packet);
				break;
			case 'k' :// koliseo
				analizar_Koliseo(packet);
				break;
			case 'K' :// casa
				analizar_Claves(packet);
				break;
			case 'Ñ' :// captcha
				try {
					GestorSalida.ENVIAR_ÑV_VOTO_RPG(this, Mundo.CAPTCHAS.get(Formulas.getRandomInt(0, Mundo.CAPTCHAS.size()
					- 1)));
				} catch (final Exception e) {}
				break;
			case 'O' :// objetos
				analizar_Objetos(packet);
				break;
			case 'P' :// grupo
				analizar_Grupo(packet);
				break;
			case 'p' :// ping
				if (packet.equals("ping")) {
					GestorSalida.ENVIAR_pong(_perso);
				}
				break;
			case 'Q' :// misiones
				analizar_Misiones(packet);
				break;
			case 'q' : // qping
				analizar_Qping(packet);
				break;
			case 'R' :// montura
				analizar_Montura(packet);
				break;
			case 'r' :// rpong
				try {
					int i = Integer.parseInt(packet.substring(5));
					if (i == _ultMillis) {
						_ping = System.currentTimeMillis() - _lastMillis;
					}
				} catch (Exception e) {}
				break;
			case 'S' :// hechizo
				analizar_Hechizos(packet);
				break;
			case 'T' : // tutoriales
				analizar_Tutoriales(packet);
				break;
			case 'W' :// areas
				analizar_Areas(packet);
				break;
			case 'z' :// zonas
				analizar_Zonas(packet);
				break;
			case 'Z' :// bustofus
				analizar_Bustofus(packet);
				break;
			case '|' :
				// nada pero es el q ocsiona las aparecidas
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR PACKETS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Packets()");
				}
				break;
		}
	}
	
	
	
	
	
	
	private void analizar_Qping(final String packet) {
		if (packet.equals("qping")) {
			// if (_perso.getPelea() != null) {
			// _perso.getPelea().tiempoParaPasarTurno();
			// }
			GestorSalida.ENVIAR_BN_NADA(_perso, "QPING");
		}
	}
	
	private boolean antiFlood(final String packet) {
		try {
			if (!MainServidor.PARAM_ANTIFLOOD) {
				return false;
			}
			if (packet.equalsIgnoreCase("GT") || packet.equals("BD") || packet.equals("qping") || packet.equals("ping")
			|| packet.equals("EMR1")) {
				return false;
			} else if (packet.length() >= 2 && (packet.substring(0, 2).equalsIgnoreCase("OU") || packet.substring(0, 2)
			.equals("EB") || packet.substring(0, 2).equals("BA") || packet.substring(0, 2).equals("GP"))) {
				return false;
			} else if (packet.length() >= 3 && (packet.substring(0, 3).equalsIgnoreCase("EMO"))) {
				return false;
			} else if (packet.length() >= 5 && (packet.substring(0, 5).equals("GA300"))) {
				return false;
			} else {
				_ultPackets[_sigPacket] = packet;
				_timePackets[_sigPacket] = System.currentTimeMillis();
				_sigPacket += 1;
				if (_sigPacket >= 7) {
					_sigPacket = 0;
				}
				if (_ultPackets[0].equals(_ultPackets[1]) && System.currentTimeMillis()
				- _timePackets[_sigPacket] < MainServidor.MILISEGUNDOS_ANTI_FLOOD) {
					if (_ultPackets[1].equals(_ultPackets[2])) {
						if (_ultPackets[2].equals(_ultPackets[3])) {
							if (_ultPackets[3].equals(_ultPackets[4])) {
								if (_ultPackets[4].equals(_ultPackets[5])) {
									if (_ultPackets[5].equals(_ultPackets[6])) {
										if (_ultPackets[6].equals(_ultPackets[0])) {
											registrar("<===> " + "EXPULSADOR POR ANTI-FLOOD PACKET " + packet);
											GestorSalida.ENVIAR_M0_MENSAJE_BASICOS_SVR_MUESTRA_DISCONNECT(this, "45", "DISCONNECT FOR FLOOD",
											"");
											cerrarSocket(true, "antiFlood");
											return true;
										}
									}
								} else {
									GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1ADVERTENCIA_FLOOD");
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", antiFlood " + e.toString());
			e.printStackTrace();
		}
		return false;
	}
	
	private void analizar_Cuenta(final String packet) {
		try {
			switch (packet.charAt(1)) {
				case 'A' :
					if (_perso != null) {
						return;
					}
					cuenta_Crear_Personaje(packet);
					break;
				case 'B' :
					if (_perso == null) {
						return;
					}
					cuenta_Boostear_Stat(packet);
					break;
				case 'D' :
					if (_perso != null) {
						return;
					}
					cuenta_Eliminar_Personaje(packet);
					break;
				case 'f' :
					if (_excesoPackets > 10) {
						cerrarSocket(true, "analizarCuenta(1)");
					} else {
						_excesoPackets++;
					}
					break;
				case 'g' :
					cuenta_Idioma(packet);
					break;
				case 'G' :
					if (_perso != null) {
						return;
					}
					cuenta_Entregar_Regalo(packet.substring(2));
					break;
				case 'i' :
					if (_perso != null) {
						return;
					}
					// _cuenta.setIdentidad(packet.substring(2));
					break;
				case 'k' :// uso de key (entrar)
					break;
				case 'L' :// lista de pjs
					if (_perso != null) {
						return;
					}
					// LAS Ñs y sus variables globales
					// GestorSalida.ENVIAR_Ñm_MENSAJE_NOMBRE_SERVER(this);;
					GestorSalida.ENVIAR_ÑG_CLASES_PERMITIDAS(this);
					GestorSalida.ENVIAR_ÑO_ID_OBJETO_MODELO_MAX(this);
					GestorSalida.ENVIAR_Ña_AUTO_PASAR_TURNO(this);
					GestorSalida.ENVIAR_Ñe_EXO_PANEL_ITEMS(this);
					GestorSalida.ENVIAR_Ñr_SUFJIO_RESET(this);
					GestorSalida.ENVIAR_ÑD_DAÑO_PERMANENTE(this);
					// el % de daño incurable
					GestorSalida.ENVIAR_ÑI_CREA_TU_ITEM_OBJETOS(this);
					GestorSalida.ENVIAR_Ñp_RANGO_NIVEL_PVP(this);
					GestorSalida.ENVIAR_ÑZ_COLOR_CHAT(this);
					GestorSalida.ENVIAR_ÑV_ACTUALIZAR_URL_LINK_MP3(this);
					GestorSalida.ENVIAR_bo_RESTRINGIR_COLOR_DIA(this);
					if (!MainServidor.URL_IMAGEN_VOTO.isEmpty()) {
						GestorSalida.ENVIAR_ÑU_URL_IMAGEN_VOTO(this);
					}
					if (!MainServidor.URL_LINK_VOTO.isEmpty()) {
						GestorSalida.ENVIAR_Ñu_URL_LINK_VOTO(this);
					}
					if (!MainServidor.URL_LINK_BUG.isEmpty()) {
						GestorSalida.ENVIAR_Ñx_URL_LINK_BUG(this);
					}
					if (!MainServidor.URL_LINK_COMPRA.isEmpty()) {
						GestorSalida.ENVIAR_Ñz_URL_LINK_COMPRA(this);
					}
					GestorSalida.ENVIAR_ALK_LISTA_DE_PERSONAJES(this, _cuenta);
					break;
				case 'P' :// nombre pj aleatorio
					if (_perso != null) {
						return;
					}
					GestorSalida.ENVIAR_APK_NOMBRE_PJ_ALEATORIO(this, Encriptador.palabraAleatorio(5));
					break;
				case 'p':
					switch (packet.charAt(2)) {
					case 's':
						int hechizo = Integer.parseInt(packet.substring(3));
						_perso.unboostearHechizo(hechizo);
						break;
					case 'c':
						if (_perso.getCuenta().esAbonado()) {
							_perso.resetStatsNoparcho(true);
							// int captotal = _perso.getNivel() * 5 - 5;
							// _perso.addPuntosStats(captotal);//test
							GestorSalida.ENVIAR_As_STATS_DEL_PJ(_perso);
							GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Tus caracteristicas han sido reiniciadas.",
									"2997F1");
						} else {
							GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Funcion aplicable solo para abonados.", "2997F1");
							return;
						}
						break;
					}
					break;
				case 'R' :// reiniciar pj (modo heroico)
					if (_perso != null || (!MainServidor.MODO_HEROICO && MainServidor.MAPAS_MODO_HEROICO.isEmpty())) {
						return;
					}
					cuenta_Reiniciar_Personaje(packet);
					break;
				case 'S' :
					if (_perso != null) {
						return;
					}
					cuenta_Seleccion_Personaje(packet);
					break;
				case 'T' :
					if (_perso != null) {
						return;
					}
					cuenta_Acceder_Server(packet);
					break;
				case 'V' :
					if (_perso != null) {
						return;
					}
					GestorSalida.ENVIAR_AV_VERSION_REGIONAL(this);
					break;
				default :
					MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR CUENTA: " + packet);
					if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
						MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
						cerrarSocket(true, "analizarCuenta(2)");
					}
					break;
			}
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", analizar cuenta " + e.toString()
			+ ", packet " + packet);
			e.printStackTrace();
		}
	}
	
	private String getStringDesconocido() {
		_excesoPackets++;
		if (_excesoPackets >= MainServidor.MAX_PACKETS_PARA_RASTREAR) {
			if (!RASTREAR_IPS.contains(_IP)) {
				RASTREAR_IPS.add(_IP);
			}
			if (_cuenta != null) {
				if (!RASTREAR_CUENTAS.contains(_cuenta.getID())) {
					RASTREAR_CUENTAS.add(_cuenta.getID());
				}
			}
		}
		return "PACKET DESCONOCIDO Cuenta: " + (_cuenta == null
		? " null "
		: (_cuenta.getNombre() + "(" + _cuenta.getID() + ") Perso: " + (_perso == null
		? " null "
		: (_perso.getNombre() + "(" + _perso.getID() + ")"))));
	}
	
	private void cuenta_Acceder_Server(final String packet) {
		try {
			for (byte i = 0; i < 3; i++) {
				_cuenta = ServidorServer.getEsperandoCuenta(Integer.parseInt(packet.substring(2)));
				if (_cuenta != null) {
					try {
						if (_timerAcceso != null && _timerAcceso.isRunning()) {
							_timerAcceso.stop();
						}
						_timerAcceso = null;
					} catch (Exception e) {}
					ServidorServer.delEsperandoCuenta(_cuenta);
					int cuentasPorIP = ServidorServer.getIPsClientes(_IP);
					if (_cuenta.getAdmin() <= 0 && cuentasPorIP >= MainServidor.MAX_CUENTAS_POR_IP) {
						GestorSalida.ENVIAR_M0_MENSAJE_BASICOS_SVR_MUESTRA_DISCONNECT(this, "34", cuentasPorIP + ";"
						+ MainServidor.MAX_CUENTAS_POR_IP, "");
						cerrarSocket(false, "cuenta_Acceder_Server(0)");
						return;
					}
					if (_cuenta.getAdmin() < MainServidor.ACCESO_ADMIN_MINIMO) {
						GestorSalida.ENVIAR_M0_MENSAJE_BASICOS_SVR_MUESTRA_DISCONNECT(this, "19", "", "");
						cerrarSocket(true, "cuenta_Acceder_Server(1)");
						return;
					}
					if (GestorSQL.ES_IP_BANEADA(_IP)) {
						GestorSalida.ENVIAR_AlEb_CUENTA_BANEADA_DEFINITIVO(this);
						cerrarSocket(true, "cuenta_Acceder_Server(2)");
						return;
					}
					final long tiempoBaneo = GestorSQL.GET_BANEADO(_cuenta.getNombre());
					if (tiempoBaneo != 0) {
						if (tiempoBaneo <= -1) {
							GestorSalida.ENVIAR_AlEb_CUENTA_BANEADA_DEFINITIVO(this);
							cerrarSocket(true, "cuenta_Acceder_Server(3)");
							return;
						} else if (tiempoBaneo > System.currentTimeMillis()) {
							GestorSalida.ENVIAR_AlEk_CUENTA_BANEADA_TIEMPO(this, tiempoBaneo);
							cerrarSocket(true, "cuenta_Acceder_Server(4)");
							return;
						} else {
							GestorSQL.SET_BANEADO(_cuenta.getNombre(), 0);
						}
					}
					_cuenta.setEntradaPersonaje(this);
					_cuenta.setActualIP(_IP);
					GestorSalida.ENVIAR_ATK_TICKET_A_CUENTA(this, crearPacketKey());
					if (MainServidor.MODO_HEROICO) {
						GestorSalida.ENVIAR_ÑS_SERVER_HEROICO(this);
					}
					for (final Personaje perso : _cuenta.getPersonajes()) {
						if (perso.getPelea() == null) {
							continue;
						}
						_perso = perso;
						_perso.conectarse();
						return;
					}
					return;
				}
				Thread.sleep(1000);
			}
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION Packet: " + packet + " SE INTENTA ACCEDER CON UNA CUENTA RARA");
			e.printStackTrace();
		}
		GestorSalida.ENVIAR_ATE_TICKET_FALLIDA(this);
		cerrarSocket(true, "cuenta_Acceder_Server(5) _cuenta: " + _cuenta + " packet: " + packet);
	}
	
	private void cuenta_Idioma(final String packet) {
		GestorSalida.ENVIAR_ÑA_LISTA_GFX(this);
		GestorSalida.ENVIAR_ÑB_LISTA_NIVEL(this);
		_cuenta.setIdioma(packet.substring(2));
		if (_perso == null) {
			cuenta_Regalo();
		}
	}
	
	private boolean cuenta_Regalo() {
		final String regalo = _cuenta.getRegalo();
		if (regalo.isEmpty()) {
			return false;
		}
		final StringBuilder lista = new StringBuilder();
		for (final String str : regalo.split(",")) {
			try {
				final String efectos = Mundo.getObjetoModelo(Integer.parseInt(str)).stringStatsModelo();
				if (lista.length() > 0) {
					lista.append(";");
				}
				lista.append("0~" + Integer.toString(Integer.parseInt(str), 16) + "~1~~" + efectos);
			} catch (final Exception e) {}
		}
		if (lista.length() == 0) {
			return false;
		}
		GestorSalida.ENVIAR_Ag_LISTA_REGALOS(this, 1, lista.toString());
		return true;
	}
	
	private void cuenta_Entregar_Regalo(final String packet) {
		try {
			final String regalo = _cuenta.getRegalo();
			if (regalo.isEmpty()) {
				return;
			}
			final String[] info = packet.split(Pattern.quote("|"));
			int idPerso = Integer.parseInt(info[1]);
			int idObjMod = Integer.parseInt(info[0]);
			final StringBuilder nuevo = new StringBuilder();
			boolean listo = false;
			for (final String str : regalo.split(",")) {
				if (str.isEmpty()) {
					continue;
				}
				int idTemp = 0;
				try {
					idTemp = Integer.parseInt(str);
				} catch (Exception e) {
					continue;
				}
				if (Mundo.getObjetoModelo(idTemp) == null) {
					continue;
				}
				if (listo || idTemp != idObjMod) {
					if (nuevo.length() > 0) {
						nuevo.append(",");
					}
					nuevo.append(str);
				} else {
					listo = true;
				}
			}
			_cuenta.setRegalo(nuevo.toString());
			if (listo) {
				Mundo.getPersonaje(idPerso).addObjIdentAInventario(Mundo.getObjetoModelo(idObjMod).crearObjeto(1,
				Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.MAXIMO), false);
				cuenta_Regalo();
				GestorSalida.ENVIAR_AG_SIGUIENTE_REGALO(this);
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(this);
		}
	}
	
	private void cuenta_Boostear_Stat(final String packet) {
		int stat = 0, capital = 1;
		try {
			stat = Integer.parseInt(packet.substring(2).split(";")[0]);
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(this, "BOOSTEAR INVALIDO");
			return;
		}
		try {
			capital = Integer.parseInt(packet.split(";")[1]);
		} catch (final Exception e) {}
		_perso.boostStat2(stat, capital);
	}
	
	private void cuenta_Reiniciar_Personaje(final String packet) {
		try {
			Personaje p = _cuenta.getPersonaje(Integer.parseInt(packet.substring(2)));
			if (p.esFantasma()) {
				p.reiniciarCero();
			}
		} catch (Exception e) {}
	}
	
	private void cuenta_Seleccion_Personaje(final String packet) {
		final int persoID = Integer.parseInt(packet.substring(2));
		if (_cuenta.getPersonaje(persoID) != null) {
			_perso = _cuenta.getPersonaje(persoID);
			if (MainServidor.MODO_HEROICO && _perso.esFantasma()) {
				_perso = null;
				GestorSalida.ENVIAR_ASE_SELECCION_PERSONAJE_FALLIDA(this);
			} else {
				_perso.conectarse();
			}
		} else {
			MainServidor.redactarLogServidorln("El personaje de ID " + persoID + " es nulo, para la cuenta " + _cuenta
			.getID());
			GestorSalida.ENVIAR_ASE_SELECCION_PERSONAJE_FALLIDA(this);
		}
	}
	
	private void cuenta_Eliminar_Personaje(final String packet) {
		try {
			final String[] split = packet.substring(2).split(Pattern.quote("|"));
			Personaje perso = _cuenta.getPersonaje(Integer.parseInt(split[0]));
			String respuesta = "";
			try {
				respuesta = URLDecoder.decode(split[1], "UTF-8");
			} catch (Exception e) {}
			if (perso != null) {
				if (perso.getNivel() < 25 || (perso.getNivel() >= 25 && respuesta.equalsIgnoreCase(_cuenta.getRespuesta()))) {
					_cuenta.eliminarPersonaje(perso.getID());
					GestorSalida.ENVIAR_ALK_LISTA_DE_PERSONAJES(this, _cuenta);
				} else {
					GestorSalida.ENVIAR_ADE_ERROR_BORRAR_PJ(this);
				}
			} else {
				GestorSalida.ENVIAR_ADE_ERROR_BORRAR_PJ(this);
			}
		} catch (Exception e) {
			GestorSalida.ENVIAR_ADE_ERROR_BORRAR_PJ(this);
		}
	}
	
	private void cuenta_Crear_Personaje(final String packet) {
		try {
			final String[] infos = packet.substring(2).split(Pattern.quote("|"));
			if (Mundo.getPersonajePorNombre(infos[0]) != null) {
				GestorSalida.ENVIAR_AAE_ERROR_CREAR_PJ(this, "a");
				return;
			}
			if (_cuenta.getPersonajes().size() >= MainServidor.MAX_PJS_POR_CUENTA) {
				GestorSalida.ENVIAR_AAE_ERROR_CREAR_PJ(this, "f");
				return;
			}
			String nombre = Personaje.nombreValido(infos[0], false);
			if (nombre == null) {
				GestorSalida.ENVIAR_AAE_ERROR_CREAR_PJ(_perso, "a");
				return;
			}
			if (nombre.isEmpty()) {
				GestorSalida.ENVIAR_AAE_ERROR_CREAR_PJ(_perso, "n");
				return;
			}
			byte claseID = Byte.parseByte(infos[1]);
			byte sexo = Byte.parseByte(infos[2]);
			int color1 = Integer.parseInt(infos[3]);
			int color2 = Integer.parseInt(infos[4]);
			int color3 = Integer.parseInt(infos[5]);
			if (Mundo.getClase(claseID) == null) {
				GestorSalida.ENVIAR_AAE_ERROR_CREAR_PJ(this, "ZCLASE NO EXISTE");
				return;
			}
			if (MainServidor.OGRINAS_CREAR_CLASE.containsKey(claseID)) {
				int ogrinas = MainServidor.OGRINAS_CREAR_CLASE.get(claseID);
				if (!GestorSQL.RESTAR_OGRINAS(_cuenta, ogrinas, null)) {
					GestorSalida.ENVIAR_AAE_ERROR_CREAR_PJ(this, "Z");
					GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(this, MainServidor.MENSAJE_ERROR_OGRINAS_CREAR_CLASE + " "
					+ ogrinas);
					return;
				}
			}
			Personaje perso = _cuenta.crearPersonaje(nombre, claseID, sexo, color1, color2, color3);
			if (perso != null) {
				// cambia la alineacion aletario
				if (MainServidor.PARAM_DAR_ALINEACION_AUTOMATICA) {
					perso.cambiarAlineacion((new Random().nextBoolean()
					? Constantes.ALINEACION_BONTARIANO
					: Constantes.ALINEACION_BRAKMARIANO), true);
				}
				GestorSalida.ENVIAR_AAK_CREACION_PJ_OK(this);
				GestorSalida.ENVIAR_ALK_LISTA_DE_PERSONAJES(this, _cuenta);
				if (MainServidor.PARAM_CINEMATIC_CREAR_PERSONAJE) {
					GestorSalida.ENVIAR_TB_CINEMA_INICIO_JUEGO(this);
				}
				if (!MainServidor.PANEL_DESPUES_CREAR_PERSONAJE.isEmpty()) {
					GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(this, MainServidor.PANEL_DESPUES_CREAR_PERSONAJE);
				}
			} else {
				GestorSalida.ENVIAR_AAE_ERROR_CREAR_PJ(this, "Z");
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_AAE_ERROR_CREAR_PJ(this, "Z");
		}
	}
	
	private void analizar_Bustofus(final String packet) {
		try {
			switch (packet.charAt(1)) {
				case 'A' : // almanax;
					bustofus_Panel_Almanax();
					break;
				case 'b' :
					break;
				case 'B' :// buscar ontoral zo
					bustofus_Mision_Almanax();
					break;
				case 'C' :// album mob o bestiario
					GestorSalida.ENVIAR_ÑF_BESTIARIO_MOBS(this, _perso.listaCardMobs());
					break;
				case 'D' :
					GestorSalida.ENVIAR_Ñi_CREA_TU_ITEM_PRECIOS(this);
					GestorSalida.ENVIAR_bb_DATA_CREAR_ITEM(_perso);
					break;
				case 'E' :// detalle mob
					final int idMob = Integer.parseInt(packet.substring(2));
					if (!_perso.tieneCardMob(idMob)) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					GestorSalida.ENVIAR_ÑE_DETALLE_MOB(this, Mundo.getMobModelo(idMob).detalleMob());
					break;
				case 'e' :
					bustofus_Buscar_Mobs_Drop(packet);
					break;
				case 'F' : // crea tu item
					bustofus_Crea_Tu_Item(packet);
					break;
				case 'G' :// modificaciones pj
					break;
				case 'h' : // lista objetos por tipo sistem items
					Mundo.getObjetosPorTipo(_perso, Short.parseShort(packet.substring(2)));
					break;
				case 'I' :// compra sistema recurso
					bustofus_Comprar_Sistema_Recurso(packet);
					break;
				case 'i' :// compra sistema items / objetos
					bustofus_Comprar_Panel_Items(packet);
					break;
				case 'J' :// panel sistem recurso
					bustofus_Sistema_Recurso();
					break;
				case 'L' :// comprar boleto
					Mundo.comprarLoteria(packet, _perso);
					break;
				case 'l' :
					bustofus_Ruleta_Suerte(packet);
					break;
				case 'm' :
					bustofus_Mostrar_Loteria();
					break;
				case 'ñ' :
					if (_perso.getPelea() != null) {
						return;
					}
					bustofus_Panel_Ornamentos();
					break;
				case 'Ñ' :
					bustofus_Elegir_Ornamento(packet);
					break;
				case 'O' :// ogrinas
					if (_perso != null) {
						bustofus_Ogrinas();
					}
					break;
				case 'P' :// cambiar nivel (modo PVP)
					bustofus_Cambiar_Nivel_Alineacion(packet);
					break;
				case 'q' :// borrar reportePIAni
					bustofus_Borrar_Reporte(packet);
					break;
				case 'r' :// detalle reporte
					bustofus_Detalle_Reporte(packet);
					break;
				case 'R' :// reportar bug
					bustofus_Reportar(packet);
					break;
				case 's' :// servicios
					if (_perso != null) {
						bustofus_Servicios(packet);
					}
					break;
				case 'S' :
					bustofus_Sets_Rapidos(packet);
					break;
				case 't' :
					if (_perso.getPelea() != null) {
						return;
					}
					GestorSalida.ENVIAR_bt_PANEL_TITULOS(_perso);
					break;
				case 'T' :
					bustofus_Elegir_Titulo(packet);
					break;
				case 'V' : // voto rpg paradize
					bustofus_Votar();
					break;
				case 'z' :
					final String[] infos = packet.substring(2).split(Pattern.quote("|"));
					String buscar = infos.length > 1 ? infos[1] : "";
					int iniciarEn = infos.length > 2 ? Integer.parseInt(infos[2]) : 0;
					if (iniciarEn < 0) {
						iniciarEn = Math.abs(iniciarEn);
						iniciarEn -= MainServidor.LIMITE_LADDER;
					}
					Mundo.enviarRanking(_perso, infos[0], buscar.toUpperCase(), iniciarEn);
					break;
				case 'Z' :
					GestorSalida.ENVIAR_bL_RANKING_PERMITIDOS(_perso);
					break;
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
	}
	
	private void bustofus_Panel_Ornamentos() {
		if (!MainServidor.PARAM_PERMITIR_ORNAMENTOS) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "ORNAMENTOS NO DISPONIBLES");
			return;
		}
		GestorSalida.ENVIAR_bñ_PANEL_ORNAMENTOS(_perso);
	}
	
	private void bustofus_Elegir_Ornamento(final String packet) {
		try {
			if (_perso.getPelea() != null) {
				return;
			}
			if (!MainServidor.PARAM_PERMITIR_ORNAMENTOS) {
				GestorSalida.ENVIAR_BN_NADA(_perso, "ORNAMENTOS NO DISPONIBLES");
				return;
			}
			final int ornamentoID = Integer.parseInt(packet.substring(2));
			if (ornamentoID == 0) {
				_perso.setOrnamento(ornamentoID);
			} else {
				Ornamento ornamento = Mundo.getOrnamento(ornamentoID);
				if (ornamento.adquirirOrnamento(_perso)) {
					_perso.addOrnamento(ornamentoID);
					_perso.setOrnamento(ornamentoID);
				}
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
	}
	
	private void bustofus_Elegir_Titulo(final String packet) {
		try {
			if (_perso.getPelea() != null) {
				return;
			}
			String[] a = packet.substring(2).split(";");
			final int tituloID = Integer.parseInt(a[0]);
			final int color = Integer.parseInt(a[1]);
			if (tituloID == 0) {
				_perso.addTitulo(0, -1);
			} else {
				Titulo titulo = Mundo.getTitulo(tituloID);
				if (titulo.adquirirTitulo(_perso)) {
					_perso.addTitulo(tituloID, color);
				}
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
	}
	
	private void bustofus_Buscar_Mobs_Drop(String packet) {
		final StringBuilder str = new StringBuilder();
		ArrayList<Integer> mobs = new ArrayList<>();
		for (String s : packet.substring(2).split(",")) {
			if (s.isEmpty()) {
				continue;
			}
			ObjetoModelo objMod = Mundo.getObjetoModelo(Integer.parseInt(s));
			if (objMod == null) {
				continue;
			}
			for (int idMob : objMod.getMobsQueDropean()) {
				if (!_perso.tieneCardMob(idMob)) {
					continue;
				}
				if (!mobs.contains(idMob)) {
					mobs.add(idMob);
				}
			}
		}
		for (int idMob : mobs) {
			if (str.length() > 0) {
				str.append(",");
			}
			str.append(idMob);
		}
		GestorSalida.ENVIAR_Ñf_BESTIARIO_DROPS(this, str.toString());
	}
	
	public void bustofus_Mision_Almanax() {
		_perso.cumplirMisionAlmanax();
	}
	
	private void bustofus_Ruleta_Suerte(final String packet) {
		try {
			switch (packet.charAt(2)) {
				case 'P' :// lista de premios
					break;
				case 'G' :// item ganador
					int ficha = Integer.parseInt(packet.substring(3));
					int index = Formulas.getRandomInt(0, 7);
					String premios = Mundo.RULETA.get(ficha);
					if (premios == null || premios.isEmpty()) {
						GestorSalida.ENVIAR_BN_NADA(_perso, "RULETA NO TIENE PREMIOS");
						return;
					}
					int premio = Integer.parseInt(premios.split(",")[index]);
					ObjetoModelo objMod = Mundo.getObjetoModelo(premio);
					if (objMod == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso, "RULETA PREMIO NO EXISTE");
						return;
					}
					if (!_perso.restarCantObjOEliminar(ficha, 1, true)) {
						GestorSalida.ENVIAR_BN_NADA(_perso, "RULETA NO TIENE FICHA");
						return;
					}
					GestorSalida.ENVIAR_brG_RULETA_GANADOR(this, index);
					Thread.sleep(3000);
					_perso.addObjIdentAInventario(objMod.crearObjeto(1, Constantes.OBJETO_POS_NO_EQUIPADO,
					CAPACIDAD_STATS.RANDOM), false);
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "021;" + 1 + "~" + premio);
					break;
			}
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", bustofus_Ruleta_Suerte " + e.toString());
			e.printStackTrace();
		}
	}
	
	private void bustofus_Servicios(String packet) {
		try {
			Servicio servicio = null;
			switch (packet.charAt(2)) {
				case 'C' :// cambio de color
					servicio = Mundo.getServicio(Constantes.SERVICIO_CAMBIO_COLOR);
					break;
				case 'G' :// cambio de emblema
					servicio = Mundo.getServicio(Constantes.SERVICIO_CAMBIO_EMBLEMA);
					break;
				case 'M' :
					servicio = Mundo.getServicio(Constantes.SERVICIO_MIMOBIONTE);
					break;
				case 'm' :
					servicio = Mundo.getServicio(Constantes.SERVICIO_TRANSFORMAR_MONTURA);
					break;
				case 'N' :// cambio de nombre
					servicio = Mundo.getServicio(Constantes.SERVICIO_CAMBIO_NOMBRE);
					break;
				case 'T' :// titulo VIP
					servicio = Mundo.getServicio(Constantes.SERVICIO_TITULO_PERSONALIZADO);
					break;
				default :
					String[] arg = packet.split(";");
					try {
						if (arg.length > 1) {
							_perso.setMedioPagoServicio(Byte.parseByte(arg[1]));
						}
					} catch (Exception e) {}
					servicio = Mundo.getServicio(Integer.parseInt(arg[0].substring(2)));
					packet = "";
					break;
			}
			servicio.usarServicio(_perso, packet);
		} catch (Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "EXCEPTION BUSTOFUS SERVICIOS");
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", bustofus_Servicios " + e.toString());
			e.printStackTrace();
		}
	}
	
	private void bustofus_Sets_Rapidos(final String packet) {
		try {
			switch (packet.charAt(2)) {
				case 'B' :
					_perso.borrarSetRapido(Integer.parseInt(packet.substring(3)));
					GestorSalida.ENVIAR_BN_NADA(_perso, "SET RAPIDO BORRADO");
					break;
				case 'C' :
					String[] split = packet.substring(3).split(Pattern.quote("|"));
					int id = Integer.parseInt(split[0]);
					String nombre = split[1];
					if (nombre.length() > 20) {
						nombre = nombre.substring(0, 20);
					}
					final String plantilla = Encriptador.NUMEROS + Encriptador.ABC_MIN + Encriptador.ABC_MAY
					+ Encriptador.ESPACIO;
					for (final char letra : nombre.toCharArray()) {
						if (!plantilla.contains(letra + "")) {
							nombre = nombre.replace(letra + "", "");
						}
					}
					int icono = Integer.parseInt(split[2]);
					String data = objeto_String_Set_Equipado();
					_perso.addSetRapido(id, nombre, icono, data);
					GestorSalida.ENVIAR_BN_NADA(_perso, "SET RAPIDO CREADO");
					break;
				case 'U' :
					SetRapido set = _perso.getSetRapido(Integer.parseInt(packet.substring(3)));
					if (set == null) {
						return;
					}
					int cambio = objeto_Desequipar_Set();
					if (cambio >= 1) {
						_perso.actualizarObjEquipStats();
					}
					Thread.sleep(200);
					cambio = Math.max(cambio, objeto_Equipar_Set(set));
					if (cambio >= 1) {
						_perso.refrescarStuff(true, cambio >= 1, cambio >= 2);
					}
					break;
			}
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", bustofus_Sets_Rapidos " + e.toString());
			e.printStackTrace();
		}
	}
	
	private void bustofus_Cambiar_Nivel_Alineacion(final String packet) {
		if (!_perso.estaDisponible(true, true)) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "NO DISPONIBLE");
			return;
		}
		if (MainServidor.NIVEL_MAX_ESCOGER_NIVEL <= 1) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "MAX ESCOGER NIVEL ES 1");
			return;
		}
		try {
			String[] split = packet.substring(2).split(Pattern.quote("|"));
			int nivel = Integer.parseInt(split[0]);
			byte alineacion = Byte.parseByte(split[1]);
			if (MainServidor.MODO_PVP) {
				_perso.cambiarNivelYAlineacion(nivel, alineacion);
			} else {
				Mundo.getServicio(Constantes.SERVICIO_ESCOGER_NIVEL).usarServicio(_perso, packet);
			}
		} catch (Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
	}
	
	private void bustofus_Panel_Almanax() {
		if (!MainServidor.PARAM_ALMANAX) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "ALMANAX NO DISPONIBLE");
			return;
		}
		Almanax almanax = Mundo.getAlmanaxDelDia();
		if (almanax == null) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1ALMANAX_NO_HAY_MISION");
			return;
		}
		try {
			Calendar cal = Calendar.getInstance();
			GestorSalida.ENVIAR_ÑX_PANEL_ALMANAX(this, cal.get(Calendar.YEAR) + "|" + cal.get(Calendar.MONTH) + "|" + cal.get(
			Calendar.DAY_OF_MONTH) + "|" + almanax.getOfrenda()._primero + "," + almanax.getOfrenda()._segundo + "|" + almanax
			.getTipo() + "," + almanax.getBonus() + "|" + _perso.cantMisionseAlmanax() + ","
			+ MainServidor.MAX_MISIONES_ALMANAX + "|" + (_perso.realizoMisionDelDia() ? 1 : 0));
		} catch (Exception e) {}
	}
	
	private void bustofus_Borrar_Reporte(final String packet) {
		if (GestorSQL.DELETE_REPORTE(Byte.parseByte(packet.charAt(2) + ""), Integer.parseInt(packet.substring(3)))) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1REPORTE_BORRADO_OK");
		} else {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1REPORTE_BORRADO_ERROR");
		}
	}
	
	private void bustofus_Detalle_Reporte(final String packet) {
		switch (packet.charAt(2)) {
			case '0' :
			case '1' :
			case '2' :
			case '3' :
				final String[] arg = packet.substring(3).split(";");
				byte tipo = Byte.parseByte(packet.charAt(2) + "");
				int idReporte = Integer.parseInt(arg[0]);
				GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, GestorSQL.GET_DESCRIPTION_REPORTE(tipo, idReporte));
				_cuenta.addIDReporte(tipo, idReporte);
				break;
			default :
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
		}
	}
	
	private void bustofus_Reportar(final String packet) {
		if (System.currentTimeMillis() - _cuenta.getUltimoReporte() < 300000) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1REPORTE_ESPERAR_ENVIAR_OTRO");
			return;
		}
		String packet2 = Constantes.filtro(packet);
		String tema = packet2.substring(4).split(Pattern.quote("|"))[1];
		String detalle = packet2.substring(4).split(Pattern.quote("|"))[2];
		switch (packet.charAt(3)) {
			case '1' :
				GestorSQL.INSERT_REPORTE_BUG(_perso.getNombre(), tema, detalle);
				break;
			case '2' :
				GestorSQL.INSERT_SUGERENCIAS(_perso.getNombre(), tema, detalle);
				break;
			case '3' :
				GestorSQL.INSERT_DENUNCIAS(_perso.getNombre(), tema, detalle);
				break;
			case '4' :
				GestorSQL.INSERT_PROBLEMA_OGRINAS(_perso.getNombre(), tema, detalle);
				break;
			default :
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1REPORTE_ENVIADO_ERROR");
				return;
		}
		_cuenta.setUltimoReporte(System.currentTimeMillis());
		GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1REPORTE_ENVIADO_OK");
	}
	
	private void bustofus_Mostrar_Loteria() {
		if (!MainServidor.PARAM_LOTERIA_OGRINAS) {
			float precioL = MainServidor.PRECIO_LOTERIA / 1000000;
			boolean millonesPrecio = true;
			if (precioL <= 0) {
				precioL = MainServidor.PRECIO_LOTERIA / 1000;
				millonesPrecio = false;
			}
			float premioL = MainServidor.PREMIO_LOTERIA / 1000000;
			boolean millonesPremio = true;
			if (premioL <= 0) {
				premioL = MainServidor.PREMIO_LOTERIA / 1000;
				millonesPremio = false;
			}
			GestorSalida.ENVIAR_bT_PANEL_LOTERIA(_perso, (precioL % 1 > 0 ? precioL : (int) precioL + "") + (millonesPrecio
			? "M"
			: "K") + ";" + (premioL % 1 > 0 ? premioL : (int) premioL + "") + (millonesPremio ? "M" : "K"));
		} else {
			GestorSalida.ENVIAR_bT_PANEL_LOTERIA(_perso, MainServidor.PRECIO_LOTERIA + ";" + MainServidor.PREMIO_LOTERIA);
		}
	}
	
	private void bustofus_Votar() {
		if (MainServidor.OGRINAS_POR_VOTO < 0) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "VOTAR NO OGRINAS");
			return;
		}
		if (_perso.getPelea() != null) {
			_votarDespuesPelea = true;
			GestorSalida.ENVIAR_BN_NADA(_perso, "VOTAR PELEA");
			return;
		}
		int tiempoRestante = _cuenta.tiempoRestanteParaVotar();
		int tiempoAparecer = MainServidor.MINUTOS_SPAMEAR_BOTON_VOTO;
		if (tiempoRestante > 0) {
			tiempoAparecer = tiempoRestante;
		}
		if (MainServidor.OGRINAS_POR_VOTO > 0) {
			if (_cuenta.puedeVotar()) {
				_cuenta.darOgrinasPorVoto();
			}
		}
		for (ServidorSocket ep : ServidorServer.getClientes()) {
			if (ep == this) {
				continue;
			}
			if (ep._perso == null) {
				continue;
			}
			if (ep.getActualIP().equals(_IP)) {
				if (tiempoRestante <= 0 && ep._perso.getPelea() != null) {
					continue;
				}
				GestorSalida.ENVIAR_bP_VOTO_RPG_PARADIZE(ep._perso, tiempoAparecer, false);
			}
		}
		GestorSalida.ENVIAR_bP_VOTO_RPG_PARADIZE(_perso, tiempoAparecer, tiempoRestante <= 0);
	}
	
	// private void bustofus_Mostrar_Boton_Voto() {
	// if (Bustemu.OGRINAS_POR_VOTO < 0) {
	// GestorSalida.ENVIAR_BN_NADA(this);;
	// return;
	// }
	// if (Bustemu.OGRINAS_POR_VOTO > 0) {
	// if (_cuenta.puedeVotar()) {
	// _cuenta.darOgrinasPorVoto();
	// }
	// }
	// int tiempo = _cuenta.tiempoRestanteParaVotar();
	// for (EntradaPersonaje ep : ServidorPersonaje.getClientes()) {
	// if (ep == Bustemu.CONECTOR)
	// continue;
	// if (ep == this) {
	// continue;
	// }
	// if (ep.getActualIP().equals(_actualIP)) {
	// GestorSalida.ENVIAR_bP_VOTO_RPG_PARADIZE(ep._perso, tiempo, false);
	// }
	// }
	// GestorSalida.ENVIAR_bP_VOTO_RPG_PARADIZE(_perso, tiempo, false);
	// }
	private void bustofus_Comprar_Panel_Items(final String packet) {
		final String[] r = packet.substring(2).split(";");
		int idObjMod = -1, cantidad = -1;
		CAPACIDAD_STATS capStas = CAPACIDAD_STATS.RANDOM;
		try {
			idObjMod = Integer.parseInt(r[0]);
			cantidad = Integer.parseInt(r[1]);
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "COMPRAR SISTEMA ITEM " + packet);
			return;
		}
		boolean exoPA = false;
		boolean exoPM = false;
		try {
			capStas = r[2].equals("1") ? CAPACIDAD_STATS.MAXIMO : CAPACIDAD_STATS.RANDOM;
		} catch (final Exception e) {}
		try {
			exoPA = r[3].equals("1");
		} catch (final Exception e) {}
		try {
			exoPM = r[4].equals("1");
		} catch (final Exception e) {}
		final ObjetoModelo objMod = Mundo.getObjetoModelo(idObjMod);
		if (objMod == null) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1OBJECT_DONT_EXIST");
			return;
		}
		if (MainServidor.SISTEMA_ITEMS_TIPO_DE_PAGO.equals("KAMAS")) {
			if (objMod.getPrecioPanelKamas() <= 0) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1ERROR_BUY_ITEM");
				return;
			}
		} else {
			if (objMod.getPrecioPanelOgrinas() <= 0) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1ERROR_BUY_ITEM");
				return;
			}
		}
		if (cantidad < 1) {
			cantidad = 1;
		}
		int precio = (MainServidor.SISTEMA_ITEMS_TIPO_DE_PAGO.equals("KAMAS")
		? objMod.getPrecioPanelKamas()
		: objMod.getPrecioPanelOgrinas());
		if (!Formulas.valorValido(cantidad, precio)) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "INTENTO BUG MULTIPLICADOR");
			return;
		}
		precio = precio * cantidad;
		if (!MainServidor.PARAM_SISTEMA_ITEMS_SOLO_PERFECTO) {
			if (!MainServidor.SISTEMA_ITEMS_EXO_TIPOS_NO_PERMITIDOS.contains(objMod.getTipo())) {
				if (exoPA) {
					precio += MainServidor.SISTEMA_ITEMS_EXO_PA_PRECIO;
				} else if (exoPM) {
					precio += MainServidor.SISTEMA_ITEMS_EXO_PM_PRECIO;
				}
			} else {
				exoPA = false;
				exoPM = false;
			}
			capStas = CAPACIDAD_STATS.MAXIMO;
		} else {
			exoPA = false;
			exoPM = false;
			if (capStas == CAPACIDAD_STATS.MAXIMO) {
				precio *= MainServidor.SISTEMA_ITEMS_PERFECTO_MULTIPLICA_POR;
			}
		}
		if (precio <= 0) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		if (MainServidor.SISTEMA_ITEMS_TIPO_DE_PAGO.equals("KAMAS")) {
			if (_perso.getKamas() >= precio) {
				_perso.addKamas(-precio, true, true);
			} else {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1128;" + precio);
				return;
			}
		} else {
			if (!GestorSQL.RESTAR_OGRINAS(_cuenta, precio, _perso)) {
				return;
			}
		}
		Objeto nuevo = objMod.crearObjeto(cantidad, Constantes.OBJETO_POS_NO_EQUIPADO, capStas);
		if (exoPA) {
			nuevo.fijarStatValor(Constantes.STAT_MAS_PA, 1);
		}
		if (exoPM) {
			nuevo.fijarStatValor(Constantes.STAT_MAS_PM, 1);
		}
		if (MainServidor.DIAS_INTERCAMBIO_COMPRAR_SISTEMA_ITEMS > 0) {
			nuevo.addStatTexto(Constantes.STAT_INTERCAMBIABLE_DESDE, ObjetoModelo.stringFechaIntercambiable(
			MainServidor.DIAS_INTERCAMBIO_COMPRAR_SISTEMA_ITEMS));
		}
		if (MainServidor.PARAM_OBJETOS_OGRINAS_LIGADO) {
			nuevo.addStatTexto(Constantes.STAT_LIGADO_A_CUENTA, "0#0#0#" + _perso.getNombre());
		}
		_perso.addObjIdentAInventario(nuevo, false);
		GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_perso);
		GestorSalida.ENVIAR_Im_INFORMACION(_perso, "021;" + cantidad + "~" + idObjMod);
	}
	
	private void bustofus_Sistema_Recurso() {
		if (MainServidor.PRECIO_SISTEMA_RECURSO <= 0) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "SISTEMA RECURSO DESHABILITADO");
			return;
		}
		final StringBuilder str = new StringBuilder();
		for (final short tipo : MainServidor.TIPO_RECURSOS) {
			if (str.length() > 0) {
				str.append(";");
			}
			str.append(tipo);
		}
		final StringBuilder str2 = new StringBuilder();
		for (final int objNo : MainServidor.OBJ_NO_PERMITIDOS) {
			if (str2.length() > 0) {
				str2.append(";");
			}
			str2.append(objNo);
		}
		GestorSalida.ENVIAR_bI_SISTEMA_RECURSO(_perso, MainServidor.PRECIO_SISTEMA_RECURSO + "|" + str.toString() + "|"
		+ str2.toString() + "|" + (MainServidor.PARAM_PRECIO_RECURSOS_EN_OGRINAS ? 1 : 0));
	}
	
	private void bustofus_Comprar_Sistema_Recurso(final String packet) {
		if (MainServidor.PRECIO_SISTEMA_RECURSO <= 0) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "COMPRAR SISTEMA RECURSO DESHABILITADO");
			return;
		}
		final String[] r = packet.substring(2).split(";");
		int idObjMod = -1;
		int cantidad = -1;
		try {
			idObjMod = Integer.parseInt(r[0]);
			cantidad = Integer.parseInt(r[1]);
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "COMPRAR SISTEMA RECURSO " + packet);
			return;
		}
		final ObjetoModelo objMod = Mundo.getObjetoModelo(idObjMod);
		if (objMod == null) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1OBJECT_DONT_EXIST");
			return;
		}
		if (!MainServidor.TIPO_RECURSOS.contains(objMod.getTipo()) || MainServidor.OBJ_NO_PERMITIDOS.contains(idObjMod)
		|| objMod.getOgrinas() > 0) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1ERROR_BUY_RECURSE");
			return;
		}
		float precioInt = (float) (MainServidor.PRECIO_SISTEMA_RECURSO * objMod.getNivel() * (Math.pow(objMod.getNivel(),
		0.5)));
		if (cantidad < 1) {
			cantidad = 1;
		}
		if (!Formulas.valorValido(cantidad, (int) precioInt)) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "INTENTO BUG MULTIPLICADOR");
			return;
		}
		int precio = 0;
		try {
			precio = (int) Math.ceil(precioInt * cantidad);
			if (!MainServidor.PARAM_PRECIO_RECURSOS_EN_OGRINAS) {
				precio = Math.max(objMod.getKamas() * cantidad, precio);
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "COMPRAR SISTEM RECURSO PRECIO INVALIDO");
			return;
		}
		if (precio <= 0) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "COMPRAR SISTEMA RECURSO PRECIO <= 0");
			return;
		}
		if (MainServidor.PARAM_PRECIO_RECURSOS_EN_OGRINAS) {
			if (!GestorSQL.RESTAR_OGRINAS(_cuenta, precio, _perso)) {
				return;
			}
		} else {
			if (_perso.getKamas() >= precio) {
				_perso.addKamas(-precio, true, true);
			} else {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1128;" + precio);
				return;
			}
		}
		_perso.addObjIdentAInventario(objMod.crearObjeto(cantidad, Constantes.OBJETO_POS_NO_EQUIPADO,
		CAPACIDAD_STATS.RANDOM), false);
		GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_perso);
		GestorSalida.ENVIAR_Im_INFORMACION(_perso, "021;" + cantidad + "~" + idObjMod);
	}
	
	private void bustofus_Crea_Tu_Item(final String packet) {
		int error = 0;
		try {
			error = 1;
			if (!Mundo.getServicio(Constantes.SERVICIO_CREA_TU_ITEM).estaActivo()) {
				GestorSalida.ENVIAR_BN_NADA(_perso, "CREA TU ITEM DESHABILITADO");
				return;
			}
			error = 2;
			final String[] split = packet.substring(2).split(Pattern.quote("|"));
			final String nombre = split[0];
			final int idModelo = Integer.parseInt(split[1]);
			final int gfx = Integer.parseInt(split[2]);
			final String[] aStats = split[3].split(";");
			final boolean firma = split[4].equals("1");
			CreaTuItem crea = Mundo.getCreaTuItem(idModelo);
			ObjetoModelo objMod = Mundo.getObjetoModelo(idModelo);
			error = 3;
			if (objMod == null) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1CREA_TU_ITEM_ERROR_OBJ_MODELO");
				return;
			}
			error = 4;
			if (crea == null) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1CREA_TU_ITEM_ERROR_CREAR_MODELO");
				return;
			}
			int tipo = objMod.getTipo();
			error = 5;
			if (Constantes.GFXS_CREA_TU_ITEM.get(tipo) == null) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1CREA_TU_ITEM_ERROR_GFX_TIPO");
				return;
			}
			error = 6;
			if (!Constantes.GFXS_CREA_TU_ITEM.get(tipo).contains(gfx)) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1CREA_TU_ITEM_ERROR_GFX_CONTAIN");
				return;
			}
			int ogrinas = crea.getPrecioBase();
			if (firma) {
				ogrinas += 10;
			}
			error = 7;
			final String plantilla = Encriptador.NUMEROS + Encriptador.ABC_MIN + Encriptador.ABC_MAY + Encriptador.ESPACIO;
			boolean paso = true;
			for (final char letra : nombre.toCharArray()) {
				if (!plantilla.contains(letra + "")) {
					paso = false;
					break;
				}
			}
			if (nombre.length() < 4 || nombre.length() > 30) {
				paso = false;
			}
			error = 8;
			if (!paso) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1CREA_TU_ITEM_ERROR_NOMBRE");
				return;
			}
			error = 9;
			final StringBuilder stats = new StringBuilder(Integer.toHexString(Constantes.STAT_COLOR_NOMBRE_OBJETO) + "#3#0#0"
			+ "," + Integer.toHexString(Constantes.STAT_CAMBIAR_GFX_OBJETO) + "#0#0#" + Integer.toHexString(gfx) + ","
			+ Integer.toHexString(Constantes.STAT_CAMBIAR_NOMBRE_OBJETO) + "#0#0#0#" + nombre);
			final ArrayList<Integer> ids = new ArrayList<>();
			for (final String e : aStats) {
				try {
					final int statID = Integer.parseInt(e.split(",")[0]);
					if (statID <= 0 || ids.contains(statID)) {
						continue;
					}
					int cantidad = Math.max(1, Math.min(Integer.parseInt(e.split(",")[1]), crea.getMaximoStat(statID)));
					if (CreaTuItem.PRECIOS.get(statID) == null) {
						continue;
					}
					ogrinas += Math.ceil(CreaTuItem.PRECIOS.get(statID) * cantidad);
					if (stats.length() > 0) {
						stats.append(",");
					}
					stats.append(Integer.toHexString(statID) + "#" + Integer.toHexString(cantidad) + "#0#0#0d0+" + cantidad);
					ids.add(statID);
					if (ids.size() >= 9) {
						break;
					}
				} catch (final Exception e1) {}
			}
			error = 10;
			int maximo = crea.getMaxOgrinas();
			if (ogrinas > maximo) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1CREA_TU_ITEM_ERROR_MAXIMO_OGRINAS");
				return;
			}
			error = 11;
			if (firma) {
				if (stats.length() > 0) {
					stats.append(",");
				}
				stats.append(Integer.toHexString(Constantes.STAT_FACBRICADO_POR) + "#0#0#0#" + _perso.getNombre());
			}
			error = 12;
			if (GestorSQL.RESTAR_OGRINAS(_cuenta, ogrinas, _perso)) {
				error = 13;
				final Objeto nuevo = Mundo.getObjetoModelo(idModelo).crearObjeto(1, Constantes.OBJETO_POS_NO_EQUIPADO,
				CAPACIDAD_STATS.MAXIMO);
				error = 14;
				if (MainServidor.PARAM_OBJETOS_OGRINAS_LIGADO) {
					nuevo.addStatTexto(Constantes.STAT_LIGADO_A_CUENTA, "0#0#0#" + _perso.getNombre());
				}
				error = 15;
				nuevo.convertirStringAStats(stats.toString());
				error = 16;
				_perso.addObjIdentAInventario(nuevo, false);
			}
		} catch (Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "CREART TU ITEM EXCEPTION - " + error);
		}
	}
	
	private void bustofus_Ogrinas() {
		try {
			_perso.setMedioPagoServicio((byte) 0);
			GestorSalida.ENVIAR_bOC_ABRIR_PANEL_SERVICIOS(_perso, GestorSQL.GET_CREDITOS_CUENTA(_cuenta.getID()), GestorSQL
			.GET_OGRINAS_CUENTA(_cuenta.getID()));
		} catch (final Exception e) {}
	}
	
	private void analizar_Documentos(final String packet) {
		switch (packet.charAt(1)) {
			case 'V' :
				GestorSalida.ENVIAR_dV_CERRAR_DOCUMENTO(_perso);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR DOCUMENTOS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Documentos");
				}
				break;
		}
	}
	
	private void analizar_Tutoriales(final String packet) {
		final String[] param = packet.split(Pattern.quote("|"));
		final Tutorial tuto = _perso.getTutorial();
		if (tuto == null) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "TUTORIAL NULO");
			return;
		}
		_perso.setTutorial(null);
		switch (packet.charAt(1)) {
			case 'V' :
				if (packet.charAt(2) != '0' && packet.charAt(2) != '4') {
					try {
						if (System.currentTimeMillis() - _perso.getInicioTutorial() > 13000) {
							int recompensa = Integer.parseInt(packet.charAt(2) + "") - 1;
							tuto.getRecompensa().get(recompensa).realizarAccion(_perso, null, -1, (short) -1);
						}
					} catch (final Exception e) {
						MainServidor.redactarLogServidorln("Se quizo usar un tutorial con " + packet);
					}
				}
				if (tuto.getFin() != null) {
					tuto.getFin().realizarAccion(_perso, null, -1, (short) -1);
				}
				try {
					if (param.length > 2) {
						byte orientacion = Byte.parseByte(param[2]);
						short celdaID = Short.parseShort(param[1]);
						_perso.setOrientacion(orientacion);
						_perso.setCelda(_perso.getMapa().getCelda(celdaID));
					}
				} catch (final Exception e) {}
				_perso.setOcupado(false);
				GestorSalida.ENVIAR_BN_NADA(_perso);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR TUTORIALES: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Tutoriales()");
				}
				break;
		}
	}
	
	private void analizar_Misiones(final String packet) {
		switch (packet.charAt(1)) {
			case 'L' :
				GestorSalida.ENVIAR_QL_LISTA_MISIONES(_perso, _perso.listaMisiones());
				break;
			case 'S' :
				int misionID = -1;
				try {
					misionID = Integer.parseInt(packet.substring(2));
				} catch (final Exception e) {}
				if (misionID <= 0) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
				}
				GestorSalida.ENVIAR_QS_PASOS_RECOMPENSA_MISION(_perso, _perso.detalleMision(misionID));
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR MISIONES: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Misiones()");
				}
				break;
		}
	}
	
	private void analizar_Conquista(final String packet) {
		switch (packet.charAt(1)) {
			case 'b' :
				conquista_Balance();
				break;
			case 'B' :
				conquista_Bonus();
				break;
			case 'W' :
				conquista_Geoposicion(packet);
				break;
			case 'I' :
				conquista_Defensa(packet);
				break;
			case 'F' :
				conquista_Unirse_Defensa_Prisma(packet);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR CONQUISTA: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Conquista()");
				}
				break;
		}
	}
	
	private void conquista_Balance() {
		final float balanceMundo = Mundo.getBalanceMundo(_perso);
		final float balanceArea = Mundo.getBalanceArea(_perso);
		GestorSalida.ENVIAR_Cb_BALANCE_CONQUISTA(_perso, balanceMundo + ";" + balanceArea);
	}
	
	private void conquista_Bonus() {
		final float balanceMundo = Mundo.getBalanceMundo(_perso);
		final float bonusExp = Mundo.getBonusAlinExp(_perso);
		final float bonusRecolecta = Mundo.getBonusAlinRecolecta(_perso);
		final float bonusDrop = Mundo.getBonusAlinDrop(_perso);
		GestorSalida.ENVIAR_CB_BONUS_CONQUISTA(_perso, balanceMundo + "," + balanceMundo + "," + balanceMundo + ";"
		+ bonusExp + "," + bonusRecolecta + "," + bonusDrop);
	}
	
	private void conquista_Defensa(final String packet) {
		try {
			if (_perso.getAlineacion() != Constantes.ALINEACION_BONTARIANO && _perso
			.getAlineacion() != Constantes.ALINEACION_BRAKMARIANO) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			switch (packet.charAt(2)) {
				case 'J' :
					final Prisma prisma = _perso.getMapa().getSubArea().getPrisma();
					if (prisma != null && prisma.getPelea() != null) {
						prisma.actualizarAtacantesDefensores();
					}
					GestorSalida.ENVIAR_CIJ_INFO_UNIRSE_PRISMA(_perso, prisma == null
					? "-3"
					: prisma.analizarPrismas(_perso.getAlineacion()));
					break;
				case 'V' :
					GestorSalida.ENVIAR_CIV_CERRAR_INFO_CONQUISTA(_perso);
					break;
				default :
					break;
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
	}
	
	private void conquista_Geoposicion(final String packet) {
		switch (packet.charAt(2)) {
			case 'J' :
				GestorSalida.ENVIAR_CW_INFO_MUNDO_CONQUISTA(_perso, Mundo.prismasGeoposicion(_perso.getAlineacion()));
				break;
			case 'V' :
				GestorSalida.ENVIAR_CIV_CERRAR_INFO_CONQUISTA(_perso);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR CONQUISTA GEOPOSICION: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "conquista_Geoposicion()");
				}
				break;
		}
	}
	
	private void conquista_Unirse_Defensa_Prisma(final String packet) {
		switch (packet.charAt(2)) {
			case 'J' :
				final Prisma prisma = _perso.getMapa().getSubArea().getPrisma();
				if (prisma == null || prisma.getPelea() == null || _perso.getPelea() != null || prisma.getAlineacion() != _perso
				.getAlineacion()) {
					return;
				}
				prisma.getPelea().unirsePelea(_perso, prisma.getID());
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR CONQ UNIRSE DEFENSA: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "conquista_Unirse_Defensa_Prisma()");
				}
				break;
		}
	}
	
	private void analizar_Casas(final String packet) {
		Casa casa = _perso.getAlgunaCasa();
		if (casa == null) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		switch (packet.charAt(1)) {
			case 'B' :
				casa.comprarCasa(_perso);
				break;
			case 'G' :
				casa.analizarCasaGremio(_perso, packet.substring(2));
				break;
			case 'Q' :// expulsar a otro perso
				casa.expulsar(_perso, packet.substring(2));
				break;
			case 'S' :
				casa.modificarPrecioVenta(_perso, packet.substring(2));
				break;
			case 'V' :
				casa.cerrarVentanaCompra(_perso);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR CASAS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Casas()");
				}
				break;
		}
	}
	
	private void analizar_Koliseo(final String packet) {
		if (!MainServidor.PARAM_KOLISEO) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1KOLISEO_DESACTIVADO");
			return;
		}
		try {
			if (_perso.getPelea().getTipoPelea() == Constantes.PELEA_TIPO_KOLISEO) {
				return;
			}
		} catch (final Exception ignored) {
		}
		switch (packet.charAt(1)) {
		case 'A':
			koliseo_Aceptar_Invitacion(packet);
			break;
		case 'b':
			switch (packet.charAt(2)) {
			case 'b': {
				try {

					final ArrayList<TiendaObjetos> idObjetos = new ArrayList<TiendaObjetos>();
					idObjetos.addAll(GestorSQL.loadObjetosTienda());
					String[] infos = packet.substring(3).split("\\|");
					int idObjeto = Integer.parseInt(infos[0]);
					int cantidad = Integer.parseInt(infos[1]);
					ObjetoModelo objT = Mundo.getObjetoModelo(idObjeto);
					if (objT == null) {
						GestorSalida.enviar(_perso, "BN");
						return;
					}
					for (TiendaObjetos tienda : idObjetos) {
						int precio = tienda.getPrice() * cantidad;
						if (idObjeto == tienda.getIdObjeto()) {
							_perso.addObjIdentAInventario(objT.crearObjeto(cantidad, Constantes.OBJETO_POS_NO_EQUIPADO,
									CAPACIDAD_STATS.RANDOM), false);
							GestorSQL.RESTAR_OGRINAS(_perso.getCuenta(), precio);
							GestorSalida.enviar(_perso,
									"?r" + GestorSQL.GET_OGRINAS_CUENTA(_perso.getCuenta().getID()));
							break;
						}

					}
				} catch (Exception e) {
					System.out.println(e);
				}
			}
				break;
			case '+': {

//					?m9233;100;7c#1#32#0#1d50+0;111;2B6,2E1
//					?midObjeto;Precio;Stats,Stats;???;itemIdBox
				String idTipo = packet.substring(2);
				StringBuilder infos = new StringBuilder();
				StringBuilder items = new StringBuilder();
				boolean first = true;
				final ArrayList<TiendaObjetos> idObjetos = new ArrayList<TiendaObjetos>();
				idObjetos.addAll(GestorSQL.loadObjetosTienda());
				for (TiendaObjetos i : idObjetos) {
					ObjetoModelo objetoTemplate = Mundo.getObjetoModelo(i.getIdObjeto());
					if (!first)
						infos.append("|");
					first = false;
					if (i.getTipo() == Integer.parseInt(idTipo)) {
						String rHex = "";
						String[] contenido = i.getContenido().split(",");
						for (String numeroString : contenido) {
							int numero = Integer.parseInt(numeroString);
							String hex = Integer.toHexString(numero);
							rHex += hex + ",";
						}
						items.append(i.getIdObjeto() + ";" + i.getPrice() + ";" + objetoTemplate.getStatsModelo() + ";;"
								+ rHex + "|");
					}
				}
				GestorSalida.enviar(_perso, "?m" + items);
			}
				break;
			case 'o':
				StringBuilder infos = new StringBuilder();
				StringBuilder items = new StringBuilder();
				boolean first = true;
				final ArrayList<TiendaCategoria> idCategoria2 = new ArrayList<TiendaCategoria>();

				idCategoria2.addAll(GestorSQL.loadCategorias());
				for (TiendaCategoria i : idCategoria2) { // can you change coleur clips simple ? blance which coleur? simpla, I don't understand, show me xD
					if (!first)
						infos.append("|");
					first = false;
					items.append(i.getId() + "," + i.getIcon() + "," + i.getName() + "|");
				}
				GestorSalida.enviar(_perso,
						"?M" + items + "@" + GestorSQL.GET_OGRINAS_CUENTA(_perso.getCuenta().getID()));
			}
			break;
		case 'I':
			koliseo_Invitar(packet);
			break;
		case 'P':
			if (_perso.getPelea() != null) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			GestorSalida.ENVIAR_kP_PANEL_KOLISEO(_perso);
			break;
		case 'R':
			koliseo_Rechazar_Invitacion();
			break;
		case 'V':
			koliseo_Expulsar(packet);
			break;
	//	case 'y':
		//	koliseo_Inscribirse1();
		//	break;
		case 'Y':
			koliseo_Inscribirse();
			break;
		case 'Z':
			koliseo_Desinscribirse();
			break;
		default:
			MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR KOLISEO: " + packet);
			if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
				MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
				cerrarSocket(true, "analizar_Koliseo()");
			}
			break;
		}
	}
	
	private void koliseo_Inscribirse() {
		if (_perso.getNivel() < MainServidor.MIN_NIVEL_KOLISEO) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "13");
			return;
		}
		if (_perso.getTiempoPenalizacionKoliseo() > System.currentTimeMillis()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1PENALIZACION_KOLISEO;" + ((_perso.getTiempoPenalizacionKoliseo()
			- System.currentTimeMillis()) / 60000));
			return;
		}
		if (Mundo.SEGUNDOS_INICIO_KOLISEO <= 5) {// inscribirse
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1KOLISEO_INSCRIBIR_TARDE");
			return;
		}
		if (Mundo.estaEnKoliseo(_perso.getID())) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1KOLISEO_INSCRIBIR_REPETIDA");
			return;
		}
		Mundo.addKoliseo(_perso);
		GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1KOLISEO_INSCRIBIR_OK");
	}
	
	private void koliseo_Desinscribirse() {
		if (Mundo.SEGUNDOS_INICIO_KOLISEO <= 5) {// inscribirse
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1KOLISEO_DESINSCRIBIR_TARDE");
			return;
		}
		if (!Mundo.estaEnKoliseo(_perso.getID())) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1KOLISEO_DESINSCRIBIR_NO_EXISTE");
			return;
		}
		if (_perso.getGrupoKoliseo() != null) {
			_perso.getGrupoKoliseo().dejarGrupo(_perso);
		}
		Mundo.delKoliseo(_perso.getID());
		GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1KOLISEO_DESINSCRIBIR_OK");
	}
	
	private void koliseo_Invitar(final String packet) {
		if (!Mundo.estaEnKoliseo(_perso.getID())) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1KOLISEO_INVITAR_TU_NO_INSCRITO");
			return;
		}
		if (Mundo.SEGUNDOS_INICIO_KOLISEO <= 20) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1KOLISEO_INVITAR_TARDE");
			return;
		}
		final String nombre = packet.substring(2);
		final Personaje invitandoA = Mundo.getPersonajePorNombre(nombre);
		if (invitandoA == null || invitandoA == _perso || !invitandoA.enLinea()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1211");
			return;
		}
		if (!invitandoA.estaVisiblePara(_perso)) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1209");
			return;
		}
		if (!Mundo.estaEnKoliseo(invitandoA.getID())) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1KOLISEO_INVITAR_EL_NO_INSCRITO");
			return;
		}
		if (invitandoA.getGrupoKoliseo() != null) {
			GestorSalida.ENVIAR_kIE_ERROR_INVITACION_KOLISEO(_perso, "a" + nombre);
			return;
		}
		if (!_perso.puedeInvitar() || !invitandoA.puedeInvitar()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1PLAYERS_IS_BUSSY");
			return;
		}
		if (_perso.getGrupoKoliseo() != null && _perso.getGrupoKoliseo()
		.getCantPjs() >= MainServidor.CANTIDAD_MIEMBROS_EQUIPO_KOLISEO) {
			GestorSalida.ENVIAR_kIE_ERROR_INVITACION_KOLISEO(_perso, "f");
			return;
		}
		if (!MainServidor.PARAM_PERMITIR_MISMAS_CLASES_EN_KOLISEO && invitandoA.getClaseID(false) == _perso.getClaseID(
		false)) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1KOLISEO_MISMAS_CLASES");
			return;
		}
		invitandoA.setInvitador(_perso, "koliseo");
		_perso.setInvitandoA(invitandoA, "koliseo");
		GestorSalida.ENVIAR_kIK_INVITAR_KOLISEO(_perso, _perso.getNombre(), nombre);
		GestorSalida.ENVIAR_kIK_INVITAR_KOLISEO(invitandoA, _perso.getNombre(), nombre);
	}
	
	private void koliseo_Aceptar_Invitacion(final String packet) {
		if (!_perso.getTipoInvitacion().equals("koliseo")) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		if (Mundo.SEGUNDOS_INICIO_KOLISEO <= 20) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1KOLISEO_GRUPO_TARDE");
			return;
		}
		final Personaje invitador = _perso.getInvitador();
		if (invitador == null || !invitador.enLinea()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1211");
			return;
		}
		GrupoKoliseo grupo = invitador.getGrupoKoliseo();
		try {
			if (grupo == null) {
				grupo = new GrupoKoliseo(invitador);
				invitador.setGrupoKoliseo(grupo);
			} else if (grupo.getCantPjs() >= MainServidor.CANTIDAD_MIEMBROS_EQUIPO_KOLISEO) {
				GestorSalida.ENVIAR_kIE_ERROR_INVITACION_KOLISEO(_perso, "f");
				return;
			}
			grupo.addPersonaje(_perso);
			_perso.setGrupoKoliseo(grupo);
			GestorSalida.ENVIAR_kA_ACEPTAR_INVITACION_KOLISEO(_perso);
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
		invitador.setInvitandoA(null, "");
		_perso.setInvitador(null, "");
	}
	
	private void koliseo_Rechazar_Invitacion() {
		_perso.rechazarKoliseo();
	}
	
	private void koliseo_Expulsar(final String packet) {// usar este packet para atacar
		try {
			_perso.getGrupoKoliseo().dejarGrupo(_perso);
			GestorSalida.ENVIAR_kV_DEJAR_KOLISEO(_perso);
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
	}
	
	private void analizar_Claves(final String packet) {
		try {
			switch (packet.charAt(1)) {
				case 'V' :
					if (_perso.getConsultarCofre() != null) {
						_perso.getConsultarCofre().cerrarVentanaClave(_perso);
					} else if (_perso.getConsultarCasa() != null) {
						_perso.getConsultarCasa().cerrarVentanaClave(_perso);
					}
					break;
				case 'K' :
					panel_Claves(packet);
					break;
				default :
					MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR CLAVES: " + packet);
					if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
						MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
						cerrarSocket(true, "analizar_Claves()");
					}
					break;
			}
		} catch (Exception e) {}
	}
	
	private void panel_Claves(final String packet) {
		try {
			switch (packet.charAt(2)) {
				case '0' :
					if (_perso.getConsultarCofre() != null) {
						_perso.getConsultarCofre().intentarAcceder(_perso, packet.substring(4));
					} else if (_perso.getAlgunaCasa() != null) {
						_perso.getAlgunaCasa().intentarAcceder(_perso, packet.substring(4));
					}
					break;
				case '1' :
					if (_perso.getConsultarCofre() != null) {
						_perso.getConsultarCofre().modificarClave(_perso, packet.substring(4));
					} else if (_perso.getAlgunaCasa() != null) {
						_perso.getAlgunaCasa().modificarClave(_perso, packet.substring(4));
					}
					break;
				default :
					MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR CASA CODIGO: " + packet);
					if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
						MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
						cerrarSocket(true, "panel_Claves()");
					}
					break;
			}
		} catch (Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
	}
	
	private void analizar_Enemigos(final String packet) {
		switch (packet.charAt(1)) {
			case 'A' :
				enemigo_Agregar(packet);
				break;
			case 'D' :
				enemigo_Borrar(packet);
				break;
			case 'L' :
				GestorSalida.ENVIAR_iL_LISTA_ENEMIGOS(_perso);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR ENEMIGOS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Enemigos()");
				}
				break;
		}
	}
	
	private void enemigo_Agregar(final String packet) {
		int id = -1;
		String nombre = "";
		switch (packet.charAt(2)) {
			case '%' :
				nombre = packet.substring(3);
				final Personaje perso = Mundo.getPersonajePorNombre(nombre);
				if (perso == null) {
					GestorSalida.ENVIAR_FD_BORRAR_AMIGO(_perso, "Ef");
					return;
				}
				id = perso.getCuentaID();
				break;
			case '*' :
				nombre = packet.substring(3);
				final Cuenta cuenta = Mundo.getCuentaPorApodo(nombre);
				if (cuenta == null) {
					GestorSalida.ENVIAR_FD_BORRAR_AMIGO(_perso, "Ef");
					return;
				}
				id = cuenta.getID();
				break;
			default :
				nombre = packet.substring(2);
				final Personaje perso2 = Mundo.getPersonajePorNombre(nombre);
				if (perso2 == null || !perso2.enLinea()) {
					GestorSalida.ENVIAR_FD_BORRAR_AMIGO(_perso, "Ef");
					return;
				}
				id = perso2.getCuentaID();
				break;
		}
		_cuenta.addEnemigo(nombre, id);
	}
	
	private void enemigo_Borrar(final String packet) {
		int id = -1;
		String nombre = "";
		switch (packet.charAt(2)) {
			case '%' :
				nombre = packet.substring(3);
				final Personaje pj = Mundo.getPersonajePorNombre(nombre);
				if (pj == null) {
					GestorSalida.ENVIAR_FD_BORRAR_AMIGO(_perso, "Ef");
					return;
				}
				id = pj.getCuentaID();
				break;
			case '*' :
				nombre = packet.substring(3);
				final Cuenta cuenta = Mundo.getCuentaPorApodo(nombre);
				if (cuenta == null) {
					GestorSalida.ENVIAR_FD_BORRAR_AMIGO(_perso, "Ef");
					return;
				}
				id = cuenta.getID();
				break;
			default :
				nombre = packet.substring(2);
				final Personaje perso = Mundo.getPersonajePorNombre(nombre);
				if (perso == null || !perso.enLinea()) {
					GestorSalida.ENVIAR_FD_BORRAR_AMIGO(_perso, "Ef");
					return;
				}
				id = perso.getCuentaID();
				break;
		}
		_cuenta.borrarEnemigo(id);
	}
	
	private void analizar_Oficios(final String packet) {
		switch (packet.charAt(1)) {
			case 'O' :
				final String[] infos = packet.substring(2).split(Pattern.quote("|"));
				final byte posOficio = Byte.parseByte(infos[0]);
				final int opciones = Integer.parseInt(infos[1]);
				final byte slots = Byte.parseByte(infos[2]);
				final StatOficio statOficio = _perso.getStatsOficios().get(posOficio);
				if (statOficio == null) {
					return;
				}
				statOficio.setOpciones(opciones);
				statOficio.setSlotsPublico(slots);
				GestorSalida.ENVIAR_JO_OFICIO_OPCIONES(_perso, statOficio);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR OFICIOS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Oficios()");
				}
				break;
		}
	}
	
	private void analizar_Zonas(final String packet) {
		if (_perso.getPelea() != null) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		switch (packet.charAt(1)) {
			case 'U' :
				try {
					_perso.usarZonas(Short.parseShort(packet.substring(2)));
				} catch (final Exception e) {}
			case 'V' :
				GestorSalida.ENVIAR_zV_CERRAR_ZONAS(_perso);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR ZONAS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Zonas()");
				}
				break;
		}
	}
	
	private void analizar_Areas(final String packet) {
		if (_perso.getPelea() != null) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		switch (packet.charAt(1)) {
			case 'U' :
				zaap_Usar(packet);
				break;
			case 'u' :
				zaapi_Usar(packet);
				break;
			case 'v' :
				GestorSalida.ENVIAR_Wv_CERRAR_ZAPPI(_perso);
				break;
			case 'V' :
				GestorSalida.ENVIAR_WV_CERRAR_ZAAP(_perso);
				break;
			case 'w' :
				GestorSalida.ENVIAR_Ww_CERRAR_PRISMA(_perso);
				break;
			case 'p' :
				prisma_Usar(packet);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR AREAS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Areas()");
				}
				break;
		}
	}
	
	private void zaap_Usar(final String packet) {
		try {
			_perso.usarZaap(Short.parseShort(packet.substring(2)));
			if (_perso.esMaestro()) {
				_perso.getGrupoParty().packetSeguirLider(packet);
			}
		} catch (final Exception e) {}
	}
	
	private void zaapi_Usar(final String packet) {
		try {
			_perso.usarZaapi(Short.parseShort(packet.substring(2)));
			if (_perso.esMaestro()) {
				_perso.getGrupoParty().packetSeguirLider(packet);
			}
		} catch (final Exception e) {}
	}
	
	private void prisma_Usar(final String packet) {
		try {
			_perso.usarPrisma(Short.parseShort(packet.substring(2)));
			if (_perso.esMaestro()) {
				_perso.getGrupoParty().packetSeguirLider(packet);
			}
		} catch (final Exception e) {}
	}
	
	private void analizar_Gremio(final String packet) {
		final Gremio gremio = _perso.getGremio();
		if (packet.charAt(1) != 'C' && packet.charAt(1) != 'V' && packet.charAt(1) != 'J') {
			if (gremio == null) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
		}
		switch (packet.charAt(1)) {
			case 'B' :
				gremio_Stats(packet);
				break;
			case 'b' :
				gremio_Hechizos(packet);
				break;
			case 'C' :
				gremio_Crear(packet);
				break;
			case 'f' :
				if (!_perso.estaDisponible(true, true)) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
				}
				gremio_Cercado(packet.substring(2));
				break;
			case 'F' :
				gremio_Retirar_Recaudador(packet.substring(2));
				break;
			case 'h' :// casa de gremio
				if (!_perso.estaDisponible(true, true)) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
				}
				gremio_Casa(packet.substring(2));
				break;
			case 'H' :
				gremio_Poner_Recaudador();
				break;
			case 'I' :
				gremio_Informacion(packet);
				break;
			case 'J' :
				gremio_Invitar(packet);
				break;
			case 'K' :
				gremio_Expulsar(packet.substring(2));
				break;
			case 'P' :
				gremio_Promover_Rango(packet.substring(2));
				break;
			case 'T' :
				gremio_Pelea_Recaudador(packet.substring(2));
				break;
			case 'V' :
				gremio_Cancelar_Creacion();
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR GREMIO: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Gremio()");
				}
				break;
		}
	}
	
	private void gremio_Stats(final String packet) {
		final Gremio gremio = _perso.getGremio();
		if (!_perso.getMiembroGremio().puede(Constantes.G_MODIF_BOOST)) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1101");
			return;
		}
		switch (packet.charAt(2)) {
			case 'p' :// prospeccion
				if (gremio.getCapital() < 1 || gremio.getStatRecolecta(176) >= 500) {
					return;
				}
				gremio.addCapital(-1);
				gremio.addStat(176, 1);
				break;
			case 'x' :// sabiduria
				if (gremio.getCapital() < 1 || gremio.getStatRecolecta(124) >= 400) {
					return;
				}
				gremio.addCapital(-1);
				gremio.addStat(124, 1);
				break;
			case 'o' :// pods
				if (gremio.getCapital() < 1 || gremio.getStatRecolecta(158) >= 5000) {
					return;
				}
				gremio.addCapital(-1);
				gremio.addStat(158, 20);
				break;
			case 'k' :// mas recaudadores
				if (gremio.getCapital() < 10 || gremio.getNroMaxRecau() >= 50) {
					return;
				}
				gremio.addCapital(-10);
				gremio.setNroMaxRecau((byte) (gremio.getNroMaxRecau() + 1));
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR GREMIO STATS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "gremio_Stats()");
				}
				break;
		}
		GestorSalida.ENVIAR_gIB_GREMIO_INFO_BOOST(_perso, gremio.analizarRecauAGremio());
	}
	
	private void gremio_Hechizos(final String packet) {
		final Gremio gremio = _perso.getGremio();
		if (gremio.getCapital() < 5) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		if (!_perso.getMiembroGremio().puede(Constantes.G_MODIF_BOOST)) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1101");
			return;
		}
		final int hechizoID = Integer.parseInt(packet.substring(2));
		if (gremio.boostHechizo(hechizoID)) {
			gremio.addCapital(-5);
			GestorSalida.ENVIAR_gIB_GREMIO_INFO_BOOST(_perso, _perso.getGremio().analizarRecauAGremio());
		} // probar los hechizos de recaudador porq no ataca
	}
	
	private void gremio_Pelea_Recaudador(final String packet) {
		try {
			final int recaudadorID = Integer.parseInt(packet.substring(1));
			switch (packet.charAt(0)) {
				case 'J' :// defender recaudador
					if (!_perso.estaDisponible(true, true)) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					Mundo.getRecaudador(recaudadorID).getPelea().unirsePelea(_perso, recaudadorID);
					break;
				case 'V' :
					Pelea p = Mundo.getRecaudador(recaudadorID).getPelea();
					if (p.getFase() == Constantes.PELEA_FASE_POSICION) {
						p.retirarsePelea(_perso.getID(), 0, false);
					}
					break;
				default :
					MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR GREMIO UNIRSE PELEA: " + packet);
					if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
						MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
						cerrarSocket(true, "gremio_Pelea_Recaudador()");
					}
					break;
			}
		} catch (Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
	}
	
	private void gremio_Retirar_Recaudador(final String packet) {
		final int recaudadorID = Integer.parseInt(packet);
		final Recaudador recaudador = Mundo.getRecaudador(recaudadorID);
		if (recaudador == null || recaudador.getPelea() != null || _perso.getGremio() == null) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "NO SE PUEDE RETIRAR");
			return;
		}
		if (recaudador.getGremio().getID() != _perso.getGremio().getID()) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "NO ES DEL GREMIO");
			return;
		}
		if (!_perso.getMiembroGremio().puede(Constantes.G_RECOLECTAR_RECAUDADOR)) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1101");
			return;
		}
		String str = _perso.getGremio().analizarRecaudadores();
		String str2 = recaudador.stringPanelInfo(_perso);
		for (final Personaje p : _perso.getGremio().getMiembros()) {
			if (p.enLinea()) {
				GestorSalida.ENVIAR_gITM_GREMIO_INFO_RECAUDADOR(p, str);
				GestorSalida.ENVIAR_gT_PANEL_RECAUDADORES_GREMIO(p, 'R', str2);
			}
		}
		recaudador.borrarRecaudador();
	}
	
	private void gremio_Poner_Recaudador() {
		if (!_perso.estaDisponible(true, true)) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "PONER_RECAUDADOR NO DISPONIBLE");
			return;
		}
		final Gremio gremio = _perso.getGremio();
		if (gremio.getCantidadMiembros() < 10) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1NOT_ENOUGHT_MEMBERS_IN_GUILD");
			return;
		}
		if (!_perso.getMiembroGremio().puede(Constantes.G_PONER_RECAUDADOR)) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1101");
			return;
		}
		final Mapa mapa = _perso.getMapa();
		if (mapa.mapaNoRecaudador() || mapa.esArena() || !mapa.getTrabajos().isEmpty() || Mundo.getCasaDentroPorMapa(mapa
		.getID()) != null || mapa.getSubArea().getArea().getSuperArea().getID() == 3) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "113");
			return;
		}
		if (gremio.getCantRecaudadores() >= gremio.getNroMaxRecau()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1CANT_HIRE_MAX_TAXCOLLECTORS"); // WTFFF !!!!!!
			return;
		}
		if (mapa.getRecaudador() != null) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1ALREADY_TAXCOLLECTOR_ON_MAP");
			return;
		}
		if (!gremio.puedePonerRecaudadorMapa(mapa.getID())) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1CANT_PUT_TAXCOLLECTOR_FOR_TIME");
			return;
		}
		if (MainServidor.PARAM_LIMITAR_RECAUDADOR_GREMIO_POR_ZONA && !Mundo.puedePonerRecauEnZona(mapa.getSubArea().getID(),
		gremio.getID())) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1168;" + MainServidor.MAX_RECAUDADORES_POR_ZONA);
			return;
		}
		final int precio = (1000 + 10 * gremio.getNivel());
		if (precio <= 0 || _perso.getKamas() < precio) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "182");
			return;
		}
		_perso.addKamas(-precio, true, true);
		final String random1 = Integer.toString(Formulas.getRandomInt(1, 129), 36);
		final String random2 = Integer.toString(Formulas.getRandomInt(1, 227), 36);
		final Recaudador recau = new Recaudador(Mundo.sigIDRecaudador(), mapa.getID(), _perso.getCelda().getID(), (byte) 3,
		gremio.getID(), random1, random2, "", 0, 0, 0, System.currentTimeMillis(), _perso.getID());
		Mundo.addRecaudador(recau);
		gremio.addUltRecolectaMapa(mapa.getID());
		GestorSalida.ENVIAR_GM_RECAUDADOR_A_MAPA(mapa, "+" + recau.stringGM());
		String str = gremio.analizarRecaudadores();
		String str2 = recau.stringPanelInfo(_perso);
		for (final Personaje p : gremio.getMiembros()) {
			if (p.enLinea()) {
				GestorSalida.ENVIAR_gITM_GREMIO_INFO_RECAUDADOR(p, str);
				GestorSalida.ENVIAR_gT_PANEL_RECAUDADORES_GREMIO(p, 'S', str2);
			}
		}
	}
	
	private void gremio_Cercado(final String packet) {
		if (!_perso.estaDisponible(true, true)) {
			return;
		}
		final short mapaID = Short.parseShort(packet);
		final Cercado cercado = Mundo.getMapa(mapaID).getCercado();
		if (cercado.getGremio().getID() != _perso.getGremio().getID()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1135");
			return;
		}
		final short celdaID = Mundo.getCeldaCercadoPorMapaID(mapaID);
		if (_perso.tenerYEliminarObjPorModYCant(9035, 1)) {
			_perso.teleport(mapaID, celdaID);
		} else {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1159");
			return;
		}
	}
	
	private void gremio_Casa(final String packet) {
		final int casaID = Integer.parseInt(packet);
		final Casa casa = Mundo.getCasas().get(casaID);
		if (casa == null) {
			return;
		}
		if (_perso.getGremio().getID() != casa.getGremioID()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1135");
			return;
		}
		if (!casa.tieneDerecho(Constantes.C_TELEPORT_GREMIO)) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1136");
			return;
		}
		if (!_perso.estaDisponible(true, true)) {
			return;
		}
		if (_perso.tenerYEliminarObjPorModYCant(8883, 1)) {// pocima de la casa del gremio
			_perso.teleport(casa.getMapaIDDentro(), casa.getCeldaIDDentro());
		} else {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1137");
			return;
		}
	}
	
	private void gremio_Cancelar_Creacion() {
		_perso.setOcupado(false);
		GestorSalida.ENVIAR_gV_CERRAR_PANEL_GREMIO(_perso);
	}
	
	private void gremio_Promover_Rango(final String packet) {
		try {
			final String[] infos = packet.split(Pattern.quote("|"));
			final int id = Integer.parseInt(infos[0]);
			int rango = Integer.parseInt(infos[1]);
			int xpDonada = Integer.parseInt(infos[2]);
			int derecho = Integer.parseInt(infos[3]);
			final Personaje perso = Mundo.getPersonaje(id);
			final MiembroGremio cambiador = _perso.getMiembroGremio();
			if (perso == null || perso.getGremio() == null) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			final MiembroGremio aCambiar = perso.getMiembroGremio();
			if (aCambiar == null || _perso.getGremio().getID() != perso.getGremio().getID()) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1210");
				return;
			}
			if (cambiador.getRango() == 1) {// lider de gremio
				if (cambiador.getID() == aCambiar.getID()) {
					rango = -1;
					derecho = -1;
				} else if (rango == 1) {
					derecho = 1;
					xpDonada = -1;
					cambiador.setTodosDerechos(2, xpDonada, Constantes.G_TODOS_LOS_DERECHOS);
				}
			} else {
				if (aCambiar.getRango() == 1) {
					GestorSalida.ENVIAR_BN_NADA(_perso, "CAMBIAR RANGO A LIDER");
					return;
				} else {
					if (xpDonada >= 0 && xpDonada != aCambiar.getPorcXpDonada()) {
						if (cambiador.getID() == aCambiar.getID()) {
							if (!cambiador.puede(Constantes.G_SU_XP_DONADA)) {
								GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1101");
								return;
							}
						} else if (!cambiador.puede(Constantes.G_TODAS_XP_DONADAS)) {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1101");
							return;
						}
					}
					if (rango >= 2) {
						if (rango != aCambiar.getRango() && !cambiador.puede(Constantes.G_MODIF_RANGOS)) {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1101");
							return;
						}
					}
					if (derecho >= 2) {
						if (derecho != aCambiar.getDerechos() && !cambiador.puede(Constantes.G_MODIF_DERECHOS)) {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1101");
							return;
						}
					}
				}
			}
			aCambiar.setTodosDerechos(rango, xpDonada, derecho);
			GestorSalida.ENVIAR_gS_STATS_GREMIO(_perso, _perso.getMiembroGremio());
			if (perso != null && perso.getID() != _perso.getID()) {
				GestorSalida.ENVIAR_gS_STATS_GREMIO(perso, perso.getMiembroGremio());
			}
		} catch (final Exception e) {}
	}
	
	private void gremio_Expulsar(final String nombre) {
		final Personaje persoExpulsar = Mundo.getPersonajePorNombre(nombre);
		if (persoExpulsar == null) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		Gremio gremio = persoExpulsar.getGremio();
		if (gremio == null) {
			gremio = Mundo.getGremio(_perso.getGremio().getID());
		}
		final MiembroGremio aExpulsar = gremio.getMiembro(persoExpulsar.getID());
		if (aExpulsar == null || aExpulsar.getGremio().getID() != _perso.getGremio().getID()) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		if (gremio.getID() != _perso.getGremio().getID()) {
			GestorSalida.ENVIAR_gK_GREMIO_BAN(_perso, "Ea");
			return;
		}
		final MiembroGremio expulsador = _perso.getMiembroGremio();
		if (!expulsador.puede(Constantes.G_BANEAR) && expulsador.getID() != aExpulsar.getID()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1101");
			return;
		}
		if (expulsador.getID() != aExpulsar.getID()) {
			if (aExpulsar.getRango() == 1) {
				return;
			}
			gremio.expulsarMiembro(aExpulsar.getID());
			GestorSalida.ENVIAR_gK_GREMIO_BAN(_perso, "K" + _perso.getNombre() + "|" + nombre);
			if (persoExpulsar != null) {
				GestorSalida.ENVIAR_gK_GREMIO_BAN(persoExpulsar, "K" + _perso.getNombre());
			}
		} else {
			if (expulsador.getRango() == 1 && gremio.getMiembros().size() > 1) {
				for (final Personaje pj : gremio.getMiembros()) {
					gremio.expulsarMiembro(pj.getID());
				}
			} else {
				gremio.expulsarMiembro(_perso.getID());
			}
			if (gremio.getMiembros().isEmpty()) {
				Mundo.eliminarGremio(gremio);
			}
			GestorSalida.ENVIAR_gK_GREMIO_BAN(_perso, "K" + nombre + "|" + nombre);
		}
	}
	
	private void gremio_Invitar(final String packet) {
		switch (packet.charAt(2)) {
			case 'R' :// invitar
				gremio_Invitar_Unirse(packet);
				break;
			case 'E' :// rechazar
				gremio_Invitar_Rechazar();
				break;
			case 'K' :// aceptar
				gremio_Invitar_Aceptar();
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR GREMIO INVITAR: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "gremio_Invitar_Unirse()");
				}
				break;
		}
	}
	
	private void gremio_Invitar_Unirse(String packet) {
		Personaje invitandoA = Mundo.getPersonajePorNombre(packet.substring(3));
		if (invitandoA == null || invitandoA == _perso) {
			GestorSalida.ENVIAR_gJ_GREMIO_UNIR(_perso, "Eu");
			return;
		}
		if (!invitandoA.enLinea()) {
			GestorSalida.ENVIAR_gJ_GREMIO_UNIR(_perso, "Eu");
			return;
		}
		if (!invitandoA.estaVisiblePara(_perso)) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1209");
			return;
		}
		if (invitandoA.getGremio() != null) {
			GestorSalida.ENVIAR_gJ_GREMIO_UNIR(_perso, "Ea");
			return;
		}
		if (!_perso.puedeInvitar() || !invitandoA.puedeInvitar()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1PLAYERS_IS_BUSSY");
			return;
		}
		if (!_perso.getMiembroGremio().puede(Constantes.G_INVITAR)) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1101");
			return;
		}
		if (MainServidor.PARAM_LIMITE_MIEMBROS_GREMIO) {
			int maxMiembros = _perso.getGremio().getMaxMiembros();
			if (_perso.getGremio().getCantidadMiembros() >= maxMiembros) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "155;" + maxMiembros);
				return;
			}
		}
		_perso.setInvitandoA(invitandoA, "gremio");
		invitandoA.setInvitador(_perso, "gremio");
		GestorSalida.ENVIAR_gJ_GREMIO_UNIR(_perso, "R" + invitandoA.getNombre());
		GestorSalida.ENVIAR_gJ_GREMIO_UNIR(invitandoA, "r" + _perso.getID() + "|" + _perso.getNombre() + "|" + _perso
		.getGremio().getNombre());
	}
	
	private void gremio_Invitar_Aceptar() {
		if (!_perso.getTipoInvitacion().equals("gremio")) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		Personaje invitador = _perso.getInvitador();
		if (invitador == null) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		final Gremio gremio = invitador.getGremio();
		if (gremio == null) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		if (MainServidor.PARAM_LIMITE_MIEMBROS_GREMIO) {
			int maxMiembros = 40 + gremio.getNivel();
			if (MainServidor.LIMITE_MIEMBROS_GREMIO > 0) {
				maxMiembros = MainServidor.LIMITE_MIEMBROS_GREMIO;
			}
			if (gremio.getCantidadMiembros() >= maxMiembros) {
				GestorSalida.ENVIAR_Im_INFORMACION(invitador, "155;" + maxMiembros);
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "155;" + maxMiembros);
				return;
			}
		}
		final MiembroGremio miembro = gremio.addMiembro(_perso.getID(), 0, 0, (byte) 0, 0);
		_perso.setMiembroGremio(miembro);
		invitador.setInvitandoA(null, "");
		_perso.setInvitador(null, "");
		GestorSalida.ENVIAR_gJ_GREMIO_UNIR(invitador, "Ka" + _perso.getNombre());
		GestorSalida.ENVIAR_gS_STATS_GREMIO(_perso, miembro);
		GestorSalida.ENVIAR_gJ_GREMIO_UNIR(_perso, "Kj");
		_perso.cambiarRopaVisual();
	}
	
	private void gremio_Invitar_Rechazar() {
		_perso.rechazarGremio();
	}
	
	private void gremio_Informacion(final String packet) {
		final Gremio gremio = _perso.getGremio();
		if (gremio == null) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		switch (packet.charAt(2)) {
			case 'B' :
				GestorSalida.ENVIAR_gIB_GREMIO_INFO_BOOST(_perso, gremio.analizarRecauAGremio());
				break;
			case 'F' :
				GestorSalida.ENVIAR_gIF_GREMIO_INFO_CERCADOS(_perso, gremio.analizarInfoCercados());
				break;
			case 'G' :
				GestorSalida.ENVIAR_gIG_GREMIO_INFO_GENERAL(_perso, gremio);
				break;
			case 'H' :
				GestorSalida.ENVIAR_gIH_GREMIO_INFO_CASAS(_perso, Casa.stringCasaGremio(gremio.getID()));
				break;
			case 'M' :
				GestorSalida.ENVIAR_gIM_GREMIO_INFO_MIEMBROS(_perso, gremio, '+');
				break;
			case 'T' :
				char c = 'a';
				try {
					c = packet.charAt(3);
				} catch (Exception e) {}
				switch (c) {
					case 'V' :
						break;
					default :
						GestorSalida.ENVIAR_gITM_GREMIO_INFO_RECAUDADOR(_perso, gremio.analizarRecaudadores());
						gremio.actualizarAtacantesDefensores();
						break;
				}
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR GREMIO INFORMACION: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "gremio_Informacion()");
				}
				break;
		}
	}
	
	private void gremio_Crear(final String packet) {
		if (_perso.getMiembroGremio() != null) {
			GestorSalida.ENVIAR_gC_CREAR_PANEL_GREMIO(_perso, "Ea");
			return;
		}
		try {
			final String[] infos = packet.substring(2).split(Pattern.quote("|"));
			final String escudoID = Integer.toString(Integer.parseInt(infos[0]), 36);
			final String colorEscudo = Integer.toString(Integer.parseInt(infos[1]), 36);
			final String emblemaID = Integer.toString(Integer.parseInt(infos[2]), 36);
			final String colorEmblema = Integer.toString(Integer.parseInt(infos[3]), 36);
			String nombre = infos[4].substring(0, 1).toUpperCase() + infos[4].substring(1).toLowerCase();
			if (Mundo.nombreGremioUsado(nombre)) {
				GestorSalida.ENVIAR_gC_CREAR_PANEL_GREMIO(_perso, "Ean");
				return;
			}
			if (nombre.length() < 2 || nombre.length() > 30) {
				GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "1NAME_GUILD_MANY_LONG");
				return;
			}
			boolean esValido = true;
			final String abcMin = "abcdefghijklmnopqrstuvwxyz- '";
			byte cantSimbol = 0, cantLetras = 0;
			char letra_A = ' ', letra_B = ' ';
			for (final char letra : nombre.toLowerCase().toCharArray()) {
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
				GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "1NAME_GUILD_USE_CHARACTERS_INVALIDS");
				return;
			}
			final String emblema = escudoID + "," + colorEscudo + "," + emblemaID + "," + colorEmblema;
			if (Mundo.emblemaGremioUsado(emblema)) {
				GestorSalida.ENVIAR_gC_CREAR_PANEL_GREMIO(_perso, "Eae");
				return;
			}
			if (!_perso.tieneObjPorModYCant(1575, 1)) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "14");
				return;
			}
			_perso.restarObjPorModYCant(1575, 1);// quitar la gremiologema
			final Gremio gremio = new Gremio(_perso, nombre, emblema);
			Mundo.addGremio(gremio);
			GestorSQL.INSERT_GREMIO(gremio);
			final MiembroGremio miembro = gremio.addMiembro(_perso.getID(), 0, 0, (byte) 0, 0);
			miembro.setTodosDerechos(1, (byte) 0, 1);
			_perso.setMiembroGremio(miembro);
			GestorSalida.ENVIAR_gS_STATS_GREMIO(_perso, miembro);
			GestorSalida.ENVIAR_gC_CREAR_PANEL_GREMIO(_perso, "K");
			GestorSalida.ENVIAR_gV_CERRAR_PANEL_GREMIO(_perso);
			_perso.setOcupado(false);
			_perso.cambiarRopaVisual();
		} catch (final Exception e) {}
	}
	
	private void analizar_Canal(final String packet) {
		switch (packet.charAt(1)) {
			case 'C' :
				canal_Cambiar(packet);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR CANAL: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Canal()");
				}
				break;
		}
	}
	
	private void canal_Cambiar(final String packet) {
		try {
			final String[] canal = packet.substring(3).split("");
			switch (packet.charAt(2)) {
				case '+' :
					for (final String c : canal) {
						if (c.isEmpty()) {
							continue;
						}
						_perso.addCanal(c);
					}
					break;
				case '-' :
					for (final String c : canal) {
						if (c.isEmpty()) {
							continue;
						}
						_perso.removerCanal(c);
					}
					break;
				default :
					MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR CANAL CAMBIAR: " + packet);
					if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
						MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
						cerrarSocket(true, "canal_Cambiar()");
					}
					break;
			}
		} catch (final Exception e) {}
	}
	
	private void analizar_Montura(final String packet) {
		switch (packet.charAt(1)) {
			case 'b' :
				montura_Comprar_Cercado(packet);
				break;
			case 'c' :
				montura_Castrar();
				break;
			case 'd' :
				montura_Descripcion(packet);
				break;
			case 'f' :
				montura_Liberar();
				break;
			case 'n' :
				montura_Nombre(packet.substring(2));
				break;
			case 'o' :
				montura_Borrar_Objeto_Crianza(packet);
				break;
			case 'p' :
				montura_Descripcion(packet);
				break;
			case 'r' :
				montura_Montar();
				break;
			case 's' :
				montura_Vender_Cercado(packet);
				break;
			case 'v' :
				GestorSalida.ENVIAR_Rv_MONTURA_CERRAR(_perso);
				break;
			case 'x' :
				montura_CambiarXP_Donada(packet);
				break;
			// case 'z' :
			// montura_Stats_VIP(packet);
			// break;
			case 'Z' :
				// try {
				// if (_perso.getMontura().getColor() == 75) {
				// GestorSalida.ENVIAR_Rz_STATS_VIP(_perso, _perso.getMontura().getStatsVIP());
				// } else {
				// GestorSalida.ENVIAR_BN_NADA(_perso);
				// }
				// } catch (final Exception e) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				// }
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR MONTURA: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Montura()");
				}
				break;
		}
	}
	
	private void montura_Vender_Cercado(final String packet) {
		GestorSalida.ENVIAR_Rv_MONTURA_CERRAR(_perso);
		final Cercado cercado = _perso.getMapa().getCercado();
		if (cercado.esPublico()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "194");
			return;
		}
		if (cercado.getDueñoID() != _perso.getID()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "195");
			return;
		}
		int precio = 0;
		try {
			precio = Integer.parseInt(packet.substring(2));
		} catch (Exception e) {}
		if (precio < 0) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		cercado.setPrecioPJ(precio);
		for (final Personaje p : _perso.getMapa().getArrayPersonajes()) {
			GestorSalida.ENVIAR_Rp_INFORMACION_CERCADO(p, cercado);
		}
	}
	
	private void montura_Comprar_Cercado(final String packet) {
		GestorSalida.ENVIAR_Rv_MONTURA_CERRAR(_perso);
		final Cercado cercado = _perso.getMapa().getCercado();
		final Personaje vendedor = Mundo.getPersonaje(cercado.getDueñoID());
		if (cercado.esPublico()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "196");
			return;
		}
		if (cercado.getPrecio() <= 0) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "197");
			return;
		}
		if (_perso.getKamas() < cercado.getPrecio()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1128;" + cercado.getPrecio());
			return;
		}
		if (_perso.getGremio() == null) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1135");
			return;
		}
		// if (_perso.getMiembroGremio().getRango() != 1) {
		// GestorSalida.ENVIAR_Im_INFORMACION(_perso, "198");
		// return;
		// }
		if (Mundo.getCantCercadosGremio(_perso.getGremio().getID()) >= (byte) Math.ceil(_perso.getGremio().getNivel()
		/ 10f)) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1103");
			return;
		}
		_perso.addKamas(-cercado.getPrecio(), true, true);
		if (vendedor != null) {
			vendedor.addKamasBanco(cercado.getPrecio());
			Personaje tempPerso = vendedor.getCuenta().getTempPersonaje();
			if (tempPerso != null) {
				GestorSalida.ENVIAR_M1_MENSAJE_SERVER_SVR_MUESTRA_INSTANTANEO(tempPerso, 17, cercado.getPrecio() + ";" + _perso
				.getGremio().getNombre(), "");
			} else {
				vendedor.getCuenta().addMensaje("M117|" + cercado.getPrecio() + ";" + _perso.getGremio().getNombre() + "|",
				true);
			}
		}
		cercado.setPrecioPJ(0);
		cercado.setDueñoID(_perso.getID());
		cercado.setGremio(_perso.getGremio());
		for (final Personaje pj : _perso.getMapa().getArrayPersonajes()) {
			GestorSalida.ENVIAR_Rp_INFORMACION_CERCADO(pj, cercado);
		}
	}
	
	private void montura_CambiarXP_Donada(final String packet) {
		try {
			byte xp = Byte.parseByte(packet.substring(2));
			_perso.setPorcXPMontura(xp);
			GestorSalida.ENVIAR_Rx_EXP_DONADA_MONTURA(_perso);
		} catch (final Exception e) {}
	}
	
	private void montura_Borrar_Objeto_Crianza(final String packet) {
		try {
			final Cercado cercado = _perso.getMapa().getCercado();
			if (cercado == null) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			if (_perso.getNombre() != "Elbusta") {
				if (_perso.getGremio() == null) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
				}
				if (!_perso.getMiembroGremio().puede(Constantes.G_MEJORAR_CERCADOS)) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "193");
					return;
				}
			}
			final short celda = Short.parseShort(packet.substring(2));
			if (cercado.retirarObjCria(celda, _perso)) {
				GestorSalida.ENVIAR_GDO_OBJETO_TIRAR_SUELO(_perso.getMapa(), '-', celda, 0, false, "");
				return;
			}
		} catch (final Exception e) {}
	}
	
	private void montura_Nombre(final String nombre) {
		if (_perso.getMontura() == null) {
			GestorSalida.ENVIAR_Re_DETALLES_MONTURA(_perso, "Er", null);
		} else {
			_perso.getMontura().setNombre(nombre);
			GestorSalida.ENVIAR_Rn_CAMBIO_NOMBRE_MONTURA(_perso, nombre);
		}
	}
	
	private void montura_Montar() {
		_perso.subirBajarMontura(false);
	}
	
	private void montura_Castrar() {
		if (_perso.getMontura() == null) {
			GestorSalida.ENVIAR_Re_DETALLES_MONTURA(_perso, "Er", null);
		} else {
			_perso.getMontura().castrarPavo();
			GestorSalida.ENVIAR_Re_DETALLES_MONTURA(_perso, "+", _perso.getMontura());
		}
	}
	
	private void montura_Liberar() {
		if (_perso.getMontura() == null) {
			GestorSalida.ENVIAR_Re_DETALLES_MONTURA(_perso, "Er", null);
		} else {
			Mundo.eliminarMontura(_perso.getMontura());
			_perso.setMontura(null);
		}
	}
	
	private void montura_Descripcion(final String packet) {
		try {
			int id = Integer.parseInt(packet.substring(2).split(Pattern.quote("|"))[0]);
			if (id > 0) {
				id = -id;
			}
			GestorSalida.ENVIAR_Rd_DESCRIPCION_MONTURA(_perso, Mundo.getMontura(id));
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
	}
	
	private void analizar_Amigos(final String packet) {
		switch (packet.charAt(1)) {
			case 'A' :
				amigo_Agregar(packet);
				break;
			case 'D' :
				amigo_Borrar(packet);
				break;
			case 'L' :
				GestorSalida.ENVIAR_FL_LISTA_DE_AMIGOS(_perso);
				break;
			case 'O' :
				switch (packet.charAt(2)) {
					case '-' :
						_perso.mostrarAmigosEnLinea(false);
						GestorSalida.ENVIAR_BN_NADA(_perso);
						break;
					case '+' :
						_perso.mostrarAmigosEnLinea(true);
						GestorSalida.ENVIAR_BN_NADA(_perso);
				}
				break;
			case 'J' :
				amigo_Esposo(packet);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR AMIGOS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Amigos()");
				}
				break;
		}
	}
	
	private void amigo_Esposo(final String packet) {
		final Personaje esposo = Mundo.getPersonaje(_perso.getEsposoID());
		if (esposo == null) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "138");
			return;
		}
		if (!esposo.enLinea()) {
			if (esposo.getSexo() == Constantes.SEXO_FEMENINO) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "136");
			} else {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "137");
			}
			GestorSalida.ENVIAR_FL_LISTA_DE_AMIGOS(_perso);
			return;
		}
		switch (packet.charAt(2)) {
			case 'S' :
				_perso.teleportEsposo(esposo);
				break;
			case 'C' :
				_perso.seguirEsposo(esposo, packet);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR AMIGO ESPOSO: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "amigo_Esposo()");
				}
				break;
		}
	}
	
	private void amigo_Borrar(final String packet) {
		int id = -1;
		switch (packet.charAt(2)) {
			case '%' :
				final Personaje perso = Mundo.getPersonajePorNombre(packet.substring(3));
				if (perso == null) {
					GestorSalida.ENVIAR_FD_BORRAR_AMIGO(_perso, "Ef");
					return;
				}
				id = perso.getCuentaID();
				break;
			case '*' :
				final Cuenta cuenta = Mundo.getCuentaPorApodo(packet.substring(3));
				if (cuenta == null) {
					GestorSalida.ENVIAR_FD_BORRAR_AMIGO(_perso, "Ef");
					return;
				}
				id = cuenta.getID();
				break;
			default :
				final Personaje perso2 = Mundo.getPersonajePorNombre(packet.substring(2));
				if (perso2 == null || !perso2.enLinea()) {
					GestorSalida.ENVIAR_FD_BORRAR_AMIGO(_perso, "Ef");
					return;
				}
				id = perso2.getCuentaID();
				break;
		}
		if (id == -1 || !_cuenta.esAmigo(id)) {
			GestorSalida.ENVIAR_FD_BORRAR_AMIGO(_perso, "Ef");
		} else {
			_cuenta.borrarAmigo(id);
		}
	}
	
	private void amigo_Agregar(final String packet) {
		int id = -1;
		switch (packet.charAt(2)) {
			case '%' :
				final Personaje perso = Mundo.getPersonajePorNombre(packet.substring(3));
				if (perso == null || !perso.enLinea()) {
					GestorSalida.ENVIAR_FA_AGREGAR_AMIGO(_perso, "Ef");
					return;
				}
				id = perso.getCuentaID();
				break;
			case '*' :
				final Cuenta cuenta = Mundo.getCuentaPorApodo(packet.substring(3));
				if (cuenta == null || !cuenta.enLinea()) {
					GestorSalida.ENVIAR_FA_AGREGAR_AMIGO(_perso, "Ef");
					return;
				}
				id = cuenta.getID();
				break;
			default :
				final Personaje perso2 = Mundo.getPersonajePorNombre(packet.substring(2));
				if (perso2 == null || !perso2.enLinea()) {
					GestorSalida.ENVIAR_FA_AGREGAR_AMIGO(_perso, "Ef");
					return;
				}
				id = perso2.getCuentaID();
				break;
		}
		if (id == -1) {
			GestorSalida.ENVIAR_FA_AGREGAR_AMIGO(_perso, "Ef");
		} else {
			_cuenta.addAmigo(id);
		}
	}
	
	private void analizar_Grupo(final String packet) {
		final Grupo grupo = _perso.getGrupoParty();
		switch (packet.charAt(1)) {
			case 'A' :
				grupo_Aceptar();
				break;
			case 'F' :
				if (grupo == null) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
				}
				grupo_Seguir(packet);
				break;
			case 'G' :
				if (grupo == null) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
				}
				grupo_Seguirme_Todos(packet);
				break;
			case 'I' :
				grupo_Invitar(packet);
				break;
			case 'R' :
				grupo_Rechazar();
				break;
			case 'V' :
				if (grupo == null) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
				}
				grupo_Expulsar(packet);
				break;
			case 'W' :
				if (grupo == null) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
				}
				grupo_Localizar();
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR GRUPO: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Grupo()");
				}
				break;
		}
	}
	
	private void grupo_Seguirme_Todos(final String packet) {
		if (_perso.getGrupoParty() == null) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		int id = -1;
		try {
			id = Integer.parseInt(packet.substring(3));
		} catch (final Exception e) {}
		final Personaje persoSeguir = Mundo.getPersonaje(id);
		if (persoSeguir == null || !persoSeguir.enLinea()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1211");
			return;
		}
		switch (packet.charAt(2)) {
			case '+' :
				_perso.getGrupoParty().setRastrear(persoSeguir);
				for (final Personaje integrante : _perso.getGrupoParty().getMiembros()) {
					GestorSalida.ENVIAR_IC_PERSONAJE_BANDERA_COMPAS(integrante, persoSeguir.getMapa().getX() + "|" + persoSeguir
					.getMapa().getY());
					GestorSalida.ENVIAR_PF_SEGUIR_PERSONAJE(integrante, "+" + persoSeguir.getID());
				}
				break;
			case '-' :
				_perso.getGrupoParty().setRastrear(null);
				for (final Personaje integrante : _perso.getGrupoParty().getMiembros()) {
					GestorSalida.ENVIAR_IC_BORRAR_BANDERA_COMPAS(integrante);
					GestorSalida.ENVIAR_PF_SEGUIR_PERSONAJE(integrante, "-");
				}
				break;
		}
	}
	
	private void grupo_Seguir(final String packet) {
		if (_perso.getGrupoParty() == null) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		int id = -1;
		try {
			id = Integer.parseInt(packet.substring(3));
		} catch (final Exception e) {}
		final Personaje persoSeguir = Mundo.getPersonaje(id);
		if (persoSeguir == null || !persoSeguir.enLinea()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1211");
			return;
		}
		switch (packet.charAt(2)) {
			case '+' :
				GestorSalida.ENVIAR_IC_PERSONAJE_BANDERA_COMPAS(_perso, persoSeguir.getMapa().getX() + "|" + persoSeguir
				.getMapa().getY());
				GestorSalida.ENVIAR_PF_SEGUIR_PERSONAJE(_perso, "+" + persoSeguir.getID());
				_perso.getGrupoParty().setRastrear(persoSeguir);
				break;
			case '-' :
				GestorSalida.ENVIAR_IC_BORRAR_BANDERA_COMPAS(_perso);
				GestorSalida.ENVIAR_PF_SEGUIR_PERSONAJE(_perso, "-");
				_perso.getGrupoParty().setRastrear(null);
				break;
		}
	}
	
	private void grupo_Localizar() {
		final Grupo grupo = _perso.getGrupoParty();
		final StringBuilder str = new StringBuilder();
		for (final Personaje miembro : grupo.getMiembros()) {
			if (str.length() > 0) {
				str.append("|");
			}
			str.append(miembro.getMapa().getX() + ";" + miembro.getMapa().getY() + ";" + miembro.getMapa().getID() + ";2;"
			+ miembro.getID() + ";" + miembro.getNombre());
		}
		GestorSalida.ENVIAR_IH_COORDENADAS_UBICACION(_perso, str.toString());
	}
	
	private void grupo_Expulsar(final String packet) {// usar este packet para atacar
		final Grupo grupo = _perso.getGrupoParty();
		if (grupo == null) {
			return;
		}
		if (!grupo.esLiderGrupo(_perso) || packet.length() == 2) {
			grupo.dejarGrupo(_perso, false);
		} else {
			int id = -1;
			try {
				id = Integer.parseInt(packet.substring(2));
			} catch (final Exception e) {}
			final Personaje expulsado = Mundo.getPersonaje(id);
			if (expulsado == null) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1211");
				return;
			}
			grupo.dejarGrupo(expulsado, true);
		}
	}
	
	private void grupo_Invitar(final String packet) {
		final String nombre = packet.substring(2);
		final Personaje invitandoA = Mundo.getPersonajePorNombre(nombre);
		if (invitandoA == null || invitandoA == _perso) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1211");
			return;
		}
		if (!invitandoA.enLinea() || invitandoA.esIndetectable()) {
			GestorSalida.ENVIAR_PIE_ERROR_INVITACION_GRUPO(_perso, "n" + nombre);
			return;
		}
		if (!invitandoA.estaVisiblePara(_perso)) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1209");
			return;
		}
		if (!_perso.puedeInvitar() || !invitandoA.puedeInvitar()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1PLAYERS_IS_BUSSY");
			return;
		}
		if (invitandoA.getGrupoParty() != null) {
			GestorSalida.ENVIAR_PIE_ERROR_INVITACION_GRUPO(_perso, "a" + nombre);
			return;
		}
		if ((_perso.getGrupoParty() != null && _perso.getGrupoParty().getMiembros().size() >= 8)) {
			GestorSalida.ENVIAR_PIE_ERROR_INVITACION_GRUPO(_perso, "f");
			return;
		}
		invitandoA.setInvitador(_perso, "grupo");
		_perso.setInvitandoA(invitandoA, "grupo");
		GestorSalida.ENVIAR_PIK_INVITAR_GRUPO(_perso, _perso.getNombre(), nombre);
		GestorSalida.ENVIAR_PIK_INVITAR_GRUPO(invitandoA, _perso.getNombre(), nombre);
	}
	
	private void grupo_Rechazar() {
		_perso.rechazarGrupo();
	}
	
	private void grupo_Aceptar() {
		if (!_perso.getTipoInvitacion().equals("grupo")) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		final Personaje lider = _perso.getInvitador();
		if (lider == null) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1211");
			return;
		}
		Grupo grupo = lider.getGrupoParty();
		if (grupo == null) {
			grupo = new Grupo();
			grupo.addIntegrante(lider);
			lider.mostrarGrupo();
		}
		GestorSalida.ENVIAR_PM_AGREGAR_PJ_GRUPO_A_GRUPO(grupo, _perso.stringInfoGrupo());
		grupo.addIntegrante(_perso);
		_perso.mostrarGrupo();
		_perso.setInvitador(null, "");
		lider.setInvitandoA(null, "");
		GestorSalida.ENVIAR_PA_ACEPTAR_INVITACION_GRUPO(lider);
	}
	
	private void analizar_Objetos(final String packet) {
		if (_perso.estaExchange()) {
			return;
		}
		switch (packet.charAt(1)) {
			case 'd' :
				objeto_Eliminar(packet);
				break;
			case 'D' :
				objeto_Tirar(packet);
				break;
			case 'f' :
				objeto_Alimentar_Objevivo(packet);
				break;
			case 'M' :
				objeto_Mover(packet);
				break;
			case 'm' :// disasociar mimobionte
				objeto_Desasociar_Mimobionte(packet);
				break;
			 case 's': // Sets rápidos  
	                if (packet.length() > 2) {  
	                    char subCmd = packet.charAt(2);  
	                    if (Character.isDigit(subCmd)) {  
	                        // Cliente solicita info de un set específico  
	                        // Simplemente enviar BN (no hacer nada)  
	                        GestorSalida.ENVIAR_BN_NADA(_perso, "Os");  
	                        return;  
	                    }  
	                    // Procesar OsE, OsS, OsL, OsD normalmente  
	                }  
	                break;  
			case 'U' :
			case 'u' :
				objeto_Usar(packet);
				break;
			case 'x' :
				objeto_Desequipar_Objevivo(packet);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR OBJETOS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Objetos()");
				}
				break;
		}
	}
	
	private synchronized void objeto_Eliminar(final String packet) {
		try {
			final String[] infos = packet.substring(2).split(Pattern.quote("|"));
			final int id = Integer.parseInt(infos[0]);
			int cant = 0;
			try {
				if (infos.length > 1) {
					cant = Integer.parseInt(infos[1]);
				}
			} catch (final Exception e) {}
			final Objeto objeto = _perso.getObjeto(id);
			if (objeto == null || cant <= 0 || !_perso.estaDisponible(false, true)) {
				GestorSalida.ENVIAR_ODE_ERROR_ELIMINAR_OBJETO(_perso);
				return;
			}
			if (objeto.getObjModelo().getTipo() == Constantes.OBJETO_TIPO_OBJETO_DE_BUSQUEDA) {
				GestorSalida.ENVIAR_ODE_ERROR_ELIMINAR_OBJETO(_perso);
				return;
			}
			if (objeto.getCantidad() - cant < 1) {
				_perso.borrarOEliminarConOR(id, true);
				if (Constantes.esPosicionEquipamiento(objeto.getPosicion())) {
					GestorSalida.ENVIAR_As_STATS_DEL_PJ(_perso);
				}
				if (Constantes.esPosicionVisual(objeto.getPosicion())) {
					_perso.cambiarRopaVisual();
				}
			} else {
				objeto.setCantidad(objeto.getCantidad() - cant);
				GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_perso, objeto);
			}
			GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_perso);
		} catch (final Exception e) {
			GestorSalida.ENVIAR_ODE_ERROR_ELIMINAR_OBJETO(_perso);
		}
	}
	
	private synchronized void objeto_Tirar(final String packet) {
		int id = -1;
		int cantidad = -1;
		try {
			id = Integer.parseInt(packet.substring(2).split(Pattern.quote("|"))[0]);
			cantidad = Integer.parseInt(packet.split(Pattern.quote("|"))[1]);
		} catch (final Exception e) {}
		final Objeto objeto = _perso.getObjeto(id);
		if (objeto == null || cantidad < 1) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "OBJETO TIRAR NULO O -1");
			return;
		}
		if (!_perso.estaDisponible(false, true)) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "OBJETO TIRAR NO DISPONIBLE");
			return;
		}
		if (objeto.getObjModelo().getTipo() == Constantes.OBJETO_TIPO_OBJETO_DE_BUSQUEDA) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "OBJETO TIRAR TIPO BUSQUEDA");
			return;
		}
		if (objeto.tieneStatTexto(Constantes.STAT_LIGADO_A_CUENTA)) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1129");
			return;
		}
		if (!objeto.pasoIntercambiableDesde()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1129");
			return;
		}
		if (objeto.getObjModeloID() == 10085) {// pergaminos de busqueda
			_perso.borrarOEliminarConOR(id, true);
			return;
		}
		final short celdaDrop = Camino.getCeldaIDCercanaLibre(_perso.getCelda(), _perso.getMapa());
		if (celdaDrop == 0) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1145");
			return;
		}
		final Celda celdaTirar = _perso.getMapa().getCelda(celdaDrop);
		int nuevaCantidad = objeto.getCantidad() - cantidad;
		if (nuevaCantidad >= 1) {
			final Objeto nuevoObj = objeto.clonarObjeto(nuevaCantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
			_perso.addObjetoConOAKO(nuevoObj, true);
			objeto.setCantidad(cantidad);
			GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_perso, objeto);
		}
		_perso.borrarOEliminarConOR(id, false);
		celdaTirar.setObjetoTirado(objeto);
		GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_perso);
		GestorSalida.ENVIAR_GDO_OBJETO_TIRAR_SUELO(_perso.getMapa(), '+', celdaTirar.getID(), objeto.getObjModeloID(),
		false, "");
		// GestorSalida.ENVIAR_As_STATS_DEL_PJ(_perso);
	}
	
	private synchronized void objeto_Usar(final String packet) {
		try {
			if (!_perso.getRestriccionA(Personaje.RA_PUEDE_USAR_OBJETOS)) {
				GestorSalida.ENVIAR_BN_NADA(_perso, "OBJETO USAR NO PUEDES USAR OBJETOS");
				return;
			}
			if (_perso.estaFullOcupado()) {
				GestorSalida.ENVIAR_BN_NADA(_perso, "OBJETO FULL OCUPADO");
				return;
			}
			int idObjeto = -1, idObjetivo = -1;
			short idCelda = -1;
			Personaje pjObjetivo = null;
			final String[] infos = packet.substring(2).split(Pattern.quote("|"));
			try {
				idObjeto = Integer.parseInt(infos[0]);
			} catch (final Exception e) {}
			try {
				idObjetivo = Integer.parseInt(infos[1]);
			} catch (final Exception e) {}
			try {
				idCelda = Short.parseShort(infos[2]);
			} catch (final Exception e) {}
			Objeto objeto = _perso.getObjeto(idObjeto);
			if (objeto == null) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1OBJECT_DONT_EXIST");
				return;
			}
			pjObjetivo = Mundo.getPersonaje(idObjetivo);
			if (pjObjetivo == null) {
				pjObjetivo = _perso;
			}
			final ObjetoModelo objModelo = objeto.getObjModelo();
			if (objModelo.getID() == MainServidor.ID_MIMOBIONTE) {
				GestorSalida.ENVIAR_ÑM_PANEL_MIMOBIONTE(_perso);
				return;
			}
			boolean comestible = objModelo.getTipo() == Constantes.OBJETO_TIPO_BEBIDA || objModelo
			.getTipo() == Constantes.OBJETO_TIPO_POCION || objModelo.getTipo() == Constantes.OBJETO_TIPO_PAN || objModelo
			.getTipo() == Constantes.OBJETO_TIPO_CARNE_COMESTIBLE || objModelo
			.getTipo() == Constantes.OBJETO_TIPO_PESCADO_COMESTIBLE;
			if (!comestible && objModelo.getNivel() > pjObjetivo.getNivel()) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "13");
				return;
			}
			if (!(objModelo.getID() <= 680 && objModelo.getID() >= 678) && !Condiciones.validaCondiciones(_perso, objModelo
			.getCondiciones())) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "119|43");
				return;
			}
			if (pjObjetivo.getPelea() != null && (pjObjetivo.getPelea().getFase() != Constantes.PELEA_FASE_POSICION
			|| !comestible)) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "191");
			} else if (!Condiciones.validaCondiciones(pjObjetivo, objModelo.getCondiciones())) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "119|43");
			} else {
				objModelo.aplicarAccion(_perso, pjObjetivo, idObjeto, idCelda);
				int[] tt = {MisionObjetivoModelo.UTILIZAR_OBJETO};
				_perso.verificarMisionesTipo(tt, null, false, objModelo.getID());
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
	}
	
	private int objeto_Mover_2(int objetoID, byte posAMover, int cantObjMover) {
		int r = -1;
		Objeto objMover = _perso.getObjeto(objetoID);
		if (objMover == null || cantObjMover < 1) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "OM2");
			return r;
		}
		final byte posAnt = objMover.getPosicion();
		if (posAnt == posAMover) {// misma posicion a mover
			GestorSalida.ENVIAR_BN_NADA(_perso, "OM3");
			return r;
		}
		final ObjetoModelo objMoverMod = objMover.getObjModelo();
		if (posAMover != Constantes.OBJETO_POS_NO_EQUIPADO && objMoverMod
		.getTipo() == Constantes.OBJETO_TIPO_OBJETO_DE_BUSQUEDA) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "OM4");
			return r;
		}
		if (Constantes.esPosicionEquipamiento(posAMover)) {
			if (objMoverMod.getNivel() > _perso.getNivel()) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "13");
				return r;
			}
			if (!Condiciones.validaCondiciones(_perso, objMoverMod.getCondiciones())) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "119|43");
				return r;
			}
			if (!_perso.puedeEquiparRepetido(objMoverMod, 1)) {
				GestorSalida.ENVIAR_BN_NADA(_perso, "OM5");
				return r;
			}
			if (objMover.getEncarnacion() != null && !_perso.sePuedePonerEncarnacion()) {
				GestorSalida.ENVIAR_BN_NADA(_perso, "60 SEG PARA ENCARNAR");
				return r;
			}
			cantObjMover = 1;
		} else if (cantObjMover > objMover.getCantidad()) {
			cantObjMover = objMover.getCantidad();
		}
		if (posAMover == Constantes.OBJETO_POS_MASCOTA) {// posicion de mascota
			if (objMoverMod.getTipo() != Constantes.OBJETO_TIPO_MASCOTA) {// alimentar a mascota
				return objeto_Alimentar_Mascota(objMover, _perso.getObjPosicion(Constantes.OBJETO_POS_MASCOTA));
			}
			if (_perso.estaMontando()) {
				_perso.subirBajarMontura(false);
			}
		}
		if (posAMover == Constantes.OBJETO_POS_MONTURA && _perso.getMontura() != null) {
			return objeto_Alimentar_Montura(objMoverMod, cantObjMover, objetoID);
		}
		if (!Constantes.esUbicacionValidaObjeto(objMoverMod.getTipo(), posAMover)) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "OM6");
			return r;
		}
		if (objMoverMod.getTipo() == Constantes.OBJETO_TIPO_OBJEVIVO) {// si es objevivo
			return objeto_Equipar_Objevivo(objMover, posAMover);
		}
		r = 0;
		// se crea un nuevo objeto con la cantidad restante (para no repetirlo despues)
		int nuevaCantidad = objMover.getCantidad() - cantObjMover;
		if (nuevaCantidad >= 1) {
			final Objeto nuevoObj = objMover.clonarObjeto(nuevaCantidad, posAnt);
			_perso.addObjetoConOAKO(nuevoObj, true);
			objMover.setCantidad(cantObjMover);
			GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_perso, objMover);
			_perso.actualizarSetsRapidos(objMover.getID(), nuevoObj.getID(), objMover.getPosicion(), posAMover);
		}
		if (objMoverMod.getTipo() != Constantes.OBJETO_TIPO_ESPECIALES && objMoverMod
		.getTipo() != Constantes.OBJETO_TIPO_POCION_FORJAMAGIA) {
			if (posAMover == Constantes.OBJETO_POS_ESCUDO) {// pos a mover es escudo
				objeto_Quitar_Arma_Dos_Manos(objMover);
			} else if (posAMover == Constantes.OBJETO_POS_ARMA) {// pos a mover es arma
				if (objMoverMod.esDosManos()) {
					objeto_Quitar_Escudo_Para_Arma(objMover);
				}
			}
		}
		final Objeto exObj = _perso.getObjPosicion(posAMover);
		if (exObj != null) {// el objeto q habia en la posicion a mover
			if (objMoverMod.getTipo() == Constantes.OBJETO_TIPO_ESPECIALES || objMoverMod
			.getTipo() == Constantes.OBJETO_TIPO_POCION_FORJAMAGIA) {
				// convertir perfecto, si es lupa o pocima de FM
				objeto_Maguear_O_Lupear(exObj, posAMover, objMover);
				return r;
			} else if (Constantes.esPosicionEquipamiento(posAMover)) {
				// no es del tipo especial o pocima fm (cuando se mueve a una pos equipamiento)
				final Objeto identInvExObj = _perso.getObjIdentInventario(exObj, objMover);
				if (identInvExObj != null) {
					identInvExObj.setCantidad(identInvExObj.getCantidad() + exObj.getCantidad());
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_perso, identInvExObj);
					_perso.borrarOEliminarConOR(exObj.getID(), true);
					_perso.actualizarSetsRapidos(exObj.getID(), identInvExObj.getID(), exObj.getPosicion(), identInvExObj
					.getPosicion());
				} else {
					// mueve el exobjeto al inventario
					exObj.setPosicion(Constantes.OBJETO_POS_NO_EQUIPADO, _perso, false);
				}
				objMover.setPosicion(posAMover, _perso, false);
				if (exObj.getObjModelo().getSetID() > 0) {
					GestorSalida.ENVIAR_OS_BONUS_SET(_perso, exObj.getObjModelo().getSetID(), -1);
				}
			} else {
				// posiciones donde se pone caramelo, panes y otros
				if (objMover.getObjModeloID() == exObj.getObjModeloID() && objMover.sonStatsIguales(exObj)) {
					exObj.setCantidad(cantObjMover + exObj.getCantidad());
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_perso, exObj);
					_perso.borrarOEliminarConOR(objMover.getID(), true);
					_perso.actualizarSetsRapidos(objMover.getID(), exObj.getID(), objMover.getPosicion(), exObj.getPosicion());
				} else {
					GestorSalida.ENVIAR_BN_NADA(_perso, "OM PENDEJADA");
				}
				return r;
			}
		} else {// si no habia un objeto donde queremos mover
			if (objMoverMod.getTipo() == Constantes.OBJETO_TIPO_ESPECIALES || objMoverMod
			.getTipo() == Constantes.OBJETO_TIPO_POCION_FORJAMAGIA) {
				// no equipables
				GestorSalida.ENVIAR_BN_NADA(_perso, "OM8");
				return r;
			}
			Objeto identicoInv;
			if (posAMover == Constantes.OBJETO_POS_NO_EQUIPADO && (identicoInv = _perso.getObjIdentInventario(objMover,
			null)) != null) {
				// mover a NO EQUIPADO y hay otro objeto identico
				_perso.borrarOEliminarConOR(objMover.getID(), true);
				identicoInv.setCantidad(identicoInv.getCantidad() + cantObjMover);
				GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_perso, identicoInv);
				_perso.actualizarSetsRapidos(objMover.getID(), identicoInv.getID(), objMover.getPosicion(), identicoInv
				.getPosicion());
			} else {
				objMover.setPosicion(posAMover, _perso, false);
			}
		}
		// para los oficios
		if (posAMover == Constantes.OBJETO_POS_ARMA) {
			objeto_Refrescar_Oficio_Por_Herramienta(objMoverMod.getID());
		} else if (posAMover == Constantes.OBJETO_POS_NO_EQUIPADO && posAnt == Constantes.OBJETO_POS_ARMA) {
			_perso.packetModoInvitarTaller(null, true);
		}
		// rectifica la cantidad de objetos por set
		if (objMoverMod.getSetID() > 0) {
			GestorSalida.ENVIAR_OS_BONUS_SET(_perso, objMoverMod.getSetID(), -1);
		}
		if (Constantes.esPosicionEquipamiento(posAMover) || (posAMover == Constantes.OBJETO_POS_NO_EQUIPADO && Constantes
		.esPosicionEquipamiento(posAnt))) {
			r = 1;
		}
		// solo cambios visuales
		if (Constantes.esPosicionVisual(posAMover) || (posAMover == Constantes.OBJETO_POS_NO_EQUIPADO && Constantes
		.esPosicionVisual(posAnt))) {
			r = 2;
		}
		return r;
	}
	
	public synchronized void objeto_Mover(final String packet) {
		// al mover se actualizan los stats objetos
		try {
			if (_perso.getPelea() != null) {
				if (_perso.getPelea().getFase() != Constantes.PELEA_FASE_POSICION || _perso.getPelea()
				.getTipoPelea() == Constantes.PELEA_TIPO_KOLISEO) {
					GestorSalida.ENVIAR_BN_NADA(_perso, "OM1");
					return;
				}
			}
			int cambio = 0;
			String subPacket = packet.substring(2).split("\n")[0];
			for (String s : subPacket.split(Pattern.quote("*"))) {
				final String[] infos = s.split(Pattern.quote("|"));
				final int objetoID = Integer.parseInt(infos[0]);
				final byte posAMover = Byte.parseByte(infos[1]);
				int cantObjMover = 1;
				try {
					if (infos.length > 2) {
						cantObjMover = Integer.parseInt(infos[2]);
					}
				} catch (final Exception e) {}
				cambio = Math.max(objeto_Mover_2(objetoID, posAMover, cantObjMover), cambio);
				Thread.sleep(100);
			}
			if (cambio >= 1) {
				_perso.refrescarStuff(true, cambio >= 1, cambio >= 2);
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "OM9");
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", objeto_Mover " + e.toString());
			e.printStackTrace();
		}
	}
	
	private int objeto_Equipar_Set(SetRapido set) {
		int r = -1;
		try {
			if (_perso.getPelea() != null) {
				if (_perso.getPelea().getFase() != Constantes.PELEA_FASE_POSICION || _perso.getPelea()
				.getTipoPelea() == Constantes.PELEA_TIPO_KOLISEO) {
					GestorSalida.ENVIAR_BN_NADA(_perso, "EQUIPAR TODO EN PELEA");
					return r;
				}
			}
			byte[] orden = {Constantes.OBJETO_POS_DOFUS1, Constantes.OBJETO_POS_DOFUS2, Constantes.OBJETO_POS_DOFUS3,
			Constantes.OBJETO_POS_DOFUS4, Constantes.OBJETO_POS_DOFUS5, Constantes.OBJETO_POS_DOFUS6,
			Constantes.OBJETO_POS_COMPAÑERO, Constantes.OBJETO_POS_MASCOTA, Constantes.OBJETO_POS_ANILLO1,
			Constantes.OBJETO_POS_ANILLO_DERECHO, Constantes.OBJETO_POS_BOTAS, Constantes.OBJETO_POS_CINTURON,
			Constantes.OBJETO_POS_AMULETO, Constantes.OBJETO_POS_SOMBRERO, Constantes.OBJETO_POS_CAPA,
			Constantes.OBJETO_POS_ESCUDO, Constantes.OBJETO_POS_ARMA};
			for (byte i : orden) {
				int idObjeto = set.getObjetos()[i];
				if (idObjeto <= 0) {
					continue;
				}
				r = Math.max(r, objeto_Mover_2(idObjeto, i, 1));
				Thread.sleep(100);
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "EQUIPAR TODO EXCEPTION");
			e.printStackTrace();
		}
		return r;
	}
	
	private int objeto_Desequipar_Set() {
		int r = -1;
		try {
			if (_perso.getPelea() != null) {
				if (_perso.getPelea().getFase() != Constantes.PELEA_FASE_POSICION || _perso.getPelea()
				.getTipoPelea() == Constantes.PELEA_TIPO_KOLISEO) {
					GestorSalida.ENVIAR_BN_NADA(_perso, "DESEQUIPAR TODO EN PELEA");
					return r;
				}
			}
			byte[] orden = {Constantes.OBJETO_POS_DOFUS1, Constantes.OBJETO_POS_DOFUS2, Constantes.OBJETO_POS_DOFUS3,
			Constantes.OBJETO_POS_DOFUS4, Constantes.OBJETO_POS_DOFUS5, Constantes.OBJETO_POS_DOFUS6,
			Constantes.OBJETO_POS_COMPAÑERO, Constantes.OBJETO_POS_MASCOTA, Constantes.OBJETO_POS_ANILLO1,
			Constantes.OBJETO_POS_ANILLO_DERECHO, Constantes.OBJETO_POS_BOTAS, Constantes.OBJETO_POS_CINTURON,
			Constantes.OBJETO_POS_AMULETO, Constantes.OBJETO_POS_SOMBRERO, Constantes.OBJETO_POS_CAPA,
			Constantes.OBJETO_POS_ESCUDO, Constantes.OBJETO_POS_ARMA};
			for (byte i : orden) {
				Objeto objeto = _perso.getObjPosicion(i);
				if (objeto == null) {
					continue;
				}
				r = Math.max(r, objeto_Mover_2(objeto.getID(), Constantes.OBJETO_POS_NO_EQUIPADO, 1));
				Thread.sleep(100);
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "DESEQUIPAR TODO EXCEPTION");
			e.printStackTrace();
		}
		return r;
	}
	
	private String objeto_String_Set_Equipado() {
		StringBuilder str = new StringBuilder();
		StringBuilder cond = new StringBuilder();
		byte[] orden = {Constantes.OBJETO_POS_DOFUS1, Constantes.OBJETO_POS_DOFUS2, Constantes.OBJETO_POS_DOFUS3,
		Constantes.OBJETO_POS_DOFUS4, Constantes.OBJETO_POS_DOFUS5, Constantes.OBJETO_POS_DOFUS6,
		Constantes.OBJETO_POS_COMPAÑERO, Constantes.OBJETO_POS_MASCOTA, Constantes.OBJETO_POS_ANILLO1,
		Constantes.OBJETO_POS_ANILLO_DERECHO, Constantes.OBJETO_POS_BOTAS, Constantes.OBJETO_POS_CINTURON,
		Constantes.OBJETO_POS_AMULETO, Constantes.OBJETO_POS_SOMBRERO, Constantes.OBJETO_POS_CAPA,
		Constantes.OBJETO_POS_ESCUDO, Constantes.OBJETO_POS_ARMA};
		for (byte i : orden) {
			Objeto obj = _perso.getObjPosicion(i);
			if (obj == null) {
				continue;
			}
			if (obj.getObjModelo().getCondiciones().isEmpty()) {
				if (str.length() > 0) {
					str.append(";");
				}
				str.append(obj.getID() + "," + obj.getPosicion());
			} else {
				if (cond.length() > 0) {
					cond.append(";");
				}
				cond.append(obj.getID() + "," + obj.getPosicion());
			}
		}
		if (cond.length() > 0) {
			if (str.length() > 0) {
				str.append(";");
			}
			str.append(cond.toString());
		}
		return str.toString();
	}
	
	private void objeto_Refrescar_Oficio_Por_Herramienta(int idObjModelo) {
		_perso.verificarHerramientOficio();
	}
	
	private void objeto_Quitar_Escudo_Para_Arma(Objeto objMover) {
		final Objeto escudo = _perso.getObjPosicion(Constantes.OBJETO_POS_ESCUDO);// escudo
		if (escudo != null) {
			final Objeto identInvExObj = _perso.getObjIdentInventario(escudo, objMover);
			if (identInvExObj != null) {// el objeto
				identInvExObj.setCantidad(identInvExObj.getCantidad() + escudo.getCantidad());
				GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_perso, identInvExObj);
				_perso.borrarOEliminarConOR(escudo.getID(), true);
				_perso.actualizarSetsRapidos(escudo.getID(), identInvExObj.getID(), escudo.getPosicion(), identInvExObj
				.getPosicion());
			} else {
				escudo.setPosicion(Constantes.OBJETO_POS_NO_EQUIPADO, _perso, false);
			}
			if (escudo.getObjModelo().getSetID() > 0) {
				GestorSalida.ENVIAR_OS_BONUS_SET(_perso, escudo.getObjModelo().getSetID(), -1);
			}
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "079");
		}
	}
	
	private void objeto_Quitar_Arma_Dos_Manos(Objeto objMover) {
		final Objeto arma = _perso.getObjPosicion(Constantes.OBJETO_POS_ARMA);// arma
		if (arma != null && arma.getObjModelo().esDosManos()) {// arma 2 manos
			final Objeto identicoArma = _perso.getObjIdentInventario(arma, objMover);
			if (identicoArma != null) {// el objeto
				identicoArma.setCantidad(identicoArma.getCantidad() + arma.getCantidad());
				GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_perso, identicoArma);
				_perso.borrarOEliminarConOR(arma.getID(), true);
				_perso.actualizarSetsRapidos(arma.getID(), identicoArma.getID(), arma.getPosicion(), identicoArma
				.getPosicion());
			} else {
				arma.setPosicion(Constantes.OBJETO_POS_NO_EQUIPADO, _perso, false);
			}
			if (arma.getObjModelo().getSetID() > 0) {
				GestorSalida.ENVIAR_OS_BONUS_SET(_perso, arma.getObjModelo().getSetID(), -1);
			}
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "078");
		}
	}
	
	private int objeto_Alimentar_Montura(ObjetoModelo objMoverMod, int cantObjMover, int idObjMover) {
		if (Constantes.esAlimentoMontura(objMoverMod.getTipo())) {
			_perso.restarCantObjOEliminar(idObjMover, cantObjMover, true);
			_perso.getMontura().aumentarEnergia(objMoverMod.getNivel(), cantObjMover);
			GestorSalida.ENVIAR_Re_DETALLES_MONTURA(_perso, "+", _perso.getMontura());
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "0105");
			return 0;
		} else {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "190");
		}
		return -1;
	}
	
	private int objeto_Equipar_Objevivo(final Objeto objevivo, final byte posAMover) {
		int r = -1;
		try {
			if (!_perso.estaDisponible(true, true)) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return r;
			}
			Objeto objeto = _perso.getObjPosicion(posAMover);
			if (objeto == null) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1161");
				return r;
			}
			int tipoObj = objeto.getObjModelo().getTipo();
			int tipoVivo = Integer.parseInt(objevivo.getParamStatTexto(Constantes.STAT_REAL_TIPO, 3), 16);
			boolean paso = false;
			switch (tipoVivo) {
				case Constantes.OBJETO_TIPO_CAPA :
					paso = tipoObj == tipoVivo || tipoObj == Constantes.OBJETO_TIPO_MOCHILA;
					break;
				case Constantes.OBJETO_TIPO_SOMBRERO :
				case Constantes.OBJETO_TIPO_AMULETO :
				case Constantes.OBJETO_TIPO_BOTAS :
				case Constantes.OBJETO_TIPO_CINTURON :
				case Constantes.OBJETO_TIPO_ANILLO :
					paso = tipoObj == tipoVivo;
					break;
			}
			if (!paso) {
				return r;
			}
			if (objeto.tieneStatTexto(Constantes.STAT_INTERCAMBIABLE_DESDE)) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1162");
				return r;
			}
			if (objeto.getObjevivoID() > 0 || objeto.tieneStatTexto(Constantes.STAT_TURNOS)) {
				GestorSalida.ENVIAR_BN_NADA(_perso, "EQUIPAR OBJEVIVO TIENE OBJEVIVO");
				return r;
			}
			int nuevaCantidad = objevivo.getCantidad() - 1;
			if (nuevaCantidad >= 1) {
				final Objeto nuevoObj = objevivo.clonarObjeto(nuevaCantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
				_perso.addObjetoConOAKO(nuevoObj, true);
				objevivo.setCantidad(1);
			}
			objevivo.addStatTexto(Constantes.STAT_REAL_GFX, "0#0#" + Integer.toHexString(objevivo.getObjModeloID()));
			objeto.setIDObjevivo(objevivo.getID());
			_perso.borrarOEliminarConOR(objevivo.getID(), false);
			GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(_perso, objeto);
			// lo salva porq despues ya no lo tendra en la lista de items
			GestorSQL.SALVAR_OBJETO(objevivo);
			if (Constantes.esPosicionVisual(objeto.getPosicion())) {
				_perso.cambiarRopaVisual();
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
		return r;
	}
	
	private void objeto_Maguear_O_Lupear(Objeto exObj, byte posAMover, Objeto objMover) {
		if (exObj.getObjevivoID() != 0 || exObj.getCantidad() > 1) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "OM7");
			return;
		}
		ObjetoModelo objMoverMod = objMover.getObjModelo();
		if (objMoverMod.getTipo() == Constantes.OBJETO_TIPO_ESPECIALES) {
			if (!exObj.convertirPerfecto(objMoverMod.getNivel())) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1OBJETO_NO_NECESITA_MEJORAS");
				return;
			}
		} else if (!MainServidor.MODO_ANKALIKE && objMoverMod.getTipo() == Constantes.OBJETO_TIPO_POCION_FORJAMAGIA
		&& posAMover == Constantes.OBJETO_POS_ARMA) {
			// cambiar daño elemental
			int statFM = Constantes.getStatPorRunaPocima(objMover);
			int potenciaFM = Constantes.getValorPorRunaPocima(objMover);
			exObj.forjaMagiaGanar(statFM, potenciaFM);
			GestorSalida.ENVIAR_IO_ICONO_OBJ_INTERACTIVO(_perso.getMapa(), _perso.getID(), "+" + objMoverMod.getID());
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1OBJETO_CAMBIO_DAÑO_ELEMENTAL");
		}
		if (objMover.addDurabilidad(-1)) {
			_perso.borrarOEliminarConOR(objMover.getID(), true);
		} else {
			GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(_perso, objMover);
		}
		GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(_perso, exObj);
		GestorSalida.ENVIAR_As_STATS_DEL_PJ(_perso);
		GestorSQL.SALVAR_OBJETO(exObj);
	}
	
	private void objeto_Apariencia_Objevivo(final String packet) {
		try {
			String[] split = packet.substring(2).split(Pattern.quote("|"));
			final Objeto objeto = Mundo.getObjeto(Integer.parseInt(split[0]));
			Objeto objevivo = null;
			if (objeto.getObjModelo().getTipo() == Constantes.OBJETO_TIPO_OBJEVIVO) {
				objevivo = objeto;
			} else {
				objevivo = Mundo.getObjeto(objeto.getObjevivoID());
			}
			int exp = Integer.parseInt(objevivo.getParamStatTexto(Constantes.STAT_EXP_OBJEVIVO, 3), 16);
			int skin = Integer.parseInt(split[2]);
			int nivel = Constantes.getNivelObjevivo(exp);
			if (skin > nivel) {
				skin = nivel;
			}
			if (skin < 1) {
				skin = 1;
			}
			objevivo.addStatTexto(Constantes.STAT_SKIN_OBJEVIVO, "0#0#" + Integer.toHexString(skin));
			GestorSQL.SALVAR_OBJETO(objevivo);
			GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(_perso, objeto);
			if (Constantes.esPosicionVisual(objeto.getPosicion())) {
				_perso.cambiarRopaVisual();
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
	}
	
	private void objeto_Alimentar_Objevivo(final String packet) {
		try {
			if (!_perso.estaDisponible(true, true)) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			final Objeto objetoObj = _perso.getObjeto(Integer.parseInt(packet.substring(2).split(Pattern.quote("|"))[0]));
			final int idObjAlimento = Integer.parseInt(packet.split(Pattern.quote("|"))[2]);
			if (objetoObj.getPosicion() == Constantes.OBJETO_POS_NO_EQUIPADO || !_perso.tieneObjetoID(idObjAlimento)) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			final Objeto objevivo = Mundo.getObjeto(objetoObj.getObjevivoID());
			objevivo.addStatTexto(Constantes.STAT_SE_HA_COMIDO_EL, ObjetoModelo.getStatSegunFecha(Calendar.getInstance()));// comida
			int expActual = Integer.parseInt(objevivo.getParamStatTexto(Constantes.STAT_EXP_OBJEVIVO, 3), 16);
			int expAdicional = (int) Math.ceil(_perso.getObjeto(idObjAlimento).getObjModelo().getNivel() / 5);
			objevivo.addStatTexto(Constantes.STAT_EXP_OBJEVIVO, "0#0#" + Integer.toHexString(expActual + expAdicional));
			_perso.restarCantObjOEliminar(idObjAlimento, 1, true);
			GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(_perso, objetoObj);
			GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_perso);
			GestorSQL.SALVAR_OBJETO(objevivo);
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "ALIMENTAR OBJEVIVO");
		}
	}
	
	private void objeto_Desequipar_Objevivo(final String packet) {
		try {
			if (!_perso.estaDisponible(true, true)) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			final Objeto objeto = _perso.getObjeto(Integer.parseInt(packet.substring(2).split(Pattern.quote("|"))[0]));
			final Objeto objevivo = Mundo.getObjeto(objeto.getObjevivoID());
			if (objevivo != null) {
				objevivo.addStatTexto(Constantes.STAT_REAL_GFX, "0#0#0");
				_perso.addObjetoConOAKO(objevivo, true);
				GestorSQL.SALVAR_OBJETO(objevivo);
			}
			objeto.setIDObjevivo(0);
			GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(_perso, objeto);
			if (Constantes.esPosicionVisual(objeto.getPosicion())) {
				_perso.cambiarRopaVisual();
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
	}
	
	private void objeto_Desasociar_Mimobionte(final String packet) {
		try {
			if (!_perso.estaDisponible(true, true)) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			final Objeto objeto = _perso.getObjeto(Integer.parseInt(packet.substring(2)));
			if (objeto == null) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			objeto.addStatTexto(Constantes.STAT_APARIENCIA_OBJETO, "");
			GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(_perso, objeto);
			if (Constantes.esPosicionVisual(objeto.getPosicion())) {
				_perso.cambiarRopaVisual();
			}
		} catch (Exception e) {}
	}
	
	private int objeto_Alimentar_Mascota(final Objeto comida, final Objeto mascObj) {
		int r = -1;
		try {
			final MascotaModelo mascota = Mundo.getMascotaModelo(mascObj.getObjModeloID());
			if (comida.getObjModeloID() == 2239) {// polvo de aniripsa
				final int pdv = mascObj.getPDV();
				if (pdv >= 10) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return r;
				}
				mascObj.addStatTexto(Constantes.STAT_PUNTOS_VIDA, "0#0#" + Integer.toHexString(pdv + 1));
			} else if (MainServidor.PARAM_ALIMENTAR_MASCOTAS && !mascota.esDevoradorAlmas() && mascota.getComida(comida
			.getObjModeloID()) != null) {
				if (mascObj.horaComer(false, Constantes.CORPULENCIA_NORMAL)) {
					mascObj.comerComida(comida.getObjModeloID());
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "032");
					GestorSalida.ENVIAR_As_STATS_DEL_PJ(_perso);
				} else {
					int corpulencia = mascObj.getCorpulencia();
					mascObj.setCorpulencia(Constantes.CORPULENCIA_OBESO);
					if (corpulencia == Constantes.CORPULENCIA_OBESO) {
						_perso.restarVidaMascota(mascObj);
					}
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "026");
				}
			} else {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "153");
				return r;
			}
			_perso.restarCantObjOEliminar(comida.getID(), 1, true);
			GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(_perso, mascObj);
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
		return r;
	}
	
	private void analizar_Dialogos(final String packet) {
		switch (packet.charAt(1)) {
			case 'C' :
				dialogo_Iniciar(packet);
				break;
			case 'B' :// iniciando dialogo con NPC, igual al C
				break;
			case 'R' :
				dialogo_Respuesta(packet);
				break;
			case 'V' :
				_perso.dialogoFin();
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR DIALOGOS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Dialogos()");
				}
				break;
		}
	}
	
	private void dialogo_Iniciar(final String packet) {
		try {
			final int id = Integer.parseInt(packet.substring(2).split("\n")[0]);
			_perso.setConversandoCon(id);
			if (id > -100) {
				int[] tt = {MisionObjetivoModelo.HABLAR_CON_NPC, MisionObjetivoModelo.VOLVER_VER_NPC,
				MisionObjetivoModelo.ENSEÑAR_OBJETO_NPC, MisionObjetivoModelo.ENTREGAR_OBJETO_NPC};
				_perso.verificarMisionesTipo(tt, null, false, 0);
			}
			Preguntador preguntador = _perso;
			int preguntaID = 0;
			NPC npc = _perso.getMapa().getNPC(id);
			if (npc != null) {
				preguntaID = npc.getPreguntaID(_perso);
			} else {
				Recaudador recau = _perso.getMapa().getRecaudador();
				if (recau != null) {
					if (recau.getID() == id) {
						preguntaID = 1;
						preguntador = recau;
					}
				}
			}
			if (preguntaID == 0) {
				_perso.dialogoFin();
				return;
			}
			PreguntaNPC pregunta = Mundo.getPreguntaNPC(preguntaID);
			if (pregunta == null) {
				pregunta = new PreguntaNPC(preguntaID, "", "", "");
				Mundo.addPreguntaNPC(pregunta);
			}
			final String str = pregunta.stringArgParaDialogo(_perso, preguntador);
			GestorSalida.ENVIAR_DCK_CREAR_DIALOGO(_perso, id);
			GestorSalida.ENVIAR_DQ_DIALOGO_PREGUNTA(_perso, str);
			if (_perso.esMaestro()) {
				_perso.getGrupoParty().packetSeguirLider(packet);
			}
		} catch (final Exception e) {
			_perso.dialogoFin();
		}
	}
	
	private void dialogo_Respuesta(final String packet) {
		final String[] infos = packet.substring(2).split(Pattern.quote("|"));
		try {
			final int preguntaID = Integer.parseInt(infos[0]);
			if (_perso.getConversandoCon() >= 0 || _perso.getPreguntaID() == 0 || preguntaID != _perso.getPreguntaID()) {
				_perso.dialogoFin();
				return;
			}
			final int respuestaID = Integer.parseInt(infos[1]);
			final PreguntaNPC pregunta = Mundo.getPreguntaNPC(preguntaID);
			final RespuestaNPC respuesta = Mundo.getRespuestaNPC(respuestaID);
			if (pregunta.getRespuestas().contains(respuestaID)) {
				respuesta.aplicar(_perso);
				if (_perso.getPreguntaID() == 0) {
					_perso.dialogoFin();
				}
			} else {
				_perso.dialogoFin();
			}
			if (_perso.esMaestro()) {
				_perso.getGrupoParty().packetSeguirLider(packet);
			}
		} catch (final Exception e) {
			_perso.dialogoFin();
		}
	}
	
	private void analizar_Intercambios(final String packet) {
		switch (packet.charAt(1)) {
			case 'A' :
				intercambio_Aceptar();
				break;
			case 'B' :
				intercambio_Comprar(packet);
				break;
			case 'f' :
				intercambio_Cercado(packet);
				break;
			case 'F' :// mostrar probabilidades
				if (_perso.getTrabajo() != null) {
					_perso.getTrabajo().mostrarProbabilidades(_perso);
				}
				break;
			case 'H' :
				intercambio_Mercadillo(packet);
				break;
			case 'J' :
				intercambio_Oficios(packet);
				break;
			case 'K' :
				intercambio_Boton_OK();
				break;
            case 'L': // Replay craft  
                if (packet.length() <= 2 || packet.substring(2).contains("NaN")) {  
                    // Cliente envía EL sin datos - ignorar  
                    GestorSalida.ENVIAR_BN_NADA(_perso, "EL");  
                    return;  
                }   // Procesar replay craft si hay datos válidos  
                break;
			case 'M' :
				intercambio_Mover_Objeto(packet);
				break;
			case 'q' :
				intercambio_Preg_Mercante();
				break;
			case 'P' :
				intercambio_Pago_Por_Trabajo(packet);
				break;
			case 'Q' :
				intercambio_Ok_Mercante();
				break;
			case 'r' :
				intercambio_Establo(packet);
				break;
			case 'R' :
				intercambio_Iniciar(packet);
				break;
			case 'S' :
				intercambio_Vender(packet);
				break;
			case 'V' :
				intercambio_Cerrar();
				break;
			case 'W' :
				intercambio_Oficio_Publico(packet);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR INTERCAMBIOS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Intercambios()");
				}
				break;
		}
	}
	
	private synchronized void intercambio_Iniciar(final String packet) {
		try {
			final String[] split = packet.substring(2).split(Pattern.quote("|"));
			byte tipo = -1;
			try {
				tipo = Byte.parseByte(split[0]);
			} catch (final Exception e) {}
			if ((_perso.getTipoExchange() == Constantes.INTERCAMBIO_TIPO_MERCADILLO_COMPRAR
			&& tipo == Constantes.INTERCAMBIO_TIPO_MERCADILLO_VENDER) || (_perso
			.getTipoExchange() == Constantes.INTERCAMBIO_TIPO_MERCADILLO_VENDER
			&& tipo == Constantes.INTERCAMBIO_TIPO_MERCADILLO_COMPRAR)) {
				// nada
			} else if (!_perso.estaDisponible(true, true)) {
				GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO NO ESTA DISPONIBLE");
				return;
			}
			if (_perso.getConsultarCofre() != null) {
				GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO CONSULTAR COFRE");
				return;
			}
			if (_perso.getConsultarCasa() != null) {
				GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO CONSULTAR CASA");
				return;
			}
			switch (tipo) {
				case Constantes.INTERCAMBIO_TIPO_MERCADILLO_COMPRAR :// mercadillo comprar
				case Constantes.INTERCAMBIO_TIPO_MERCADILLO_VENDER :// mercadillo vender
					if (_perso.getDeshonor() >= 5) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "183");
						GestorSalida.ENVIAR_EV_CERRAR_VENTANAS(_perso, "");
						return;
					}
					if (_perso.getExchanger() != null) {
						GestorSalida.ENVIAR_EV_CERRAR_VENTANAS(_perso, "");
					}
					final Mercadillo mercadillo = Mundo.getPuestoPorMapa(_perso.getMapa().getID());
					if (mercadillo == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					_perso.setExchanger(mercadillo);
					GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(_perso, tipo, "1,10,100;" + mercadillo.getTipoObjPermitidos()
					+ ";" + mercadillo.getPorcentajeImpuesto() + ";" + mercadillo.getNivelMax() + ";" + mercadillo
					.getMaxObjCuenta() + ";-1;" + mercadillo.getTiempoVenta());
					if (tipo == Constantes.INTERCAMBIO_TIPO_MERCADILLO_VENDER) {// mercadillo vender
						GestorSalida.ENVIAR_EL_LISTA_EXCHANGER(_perso, mercadillo);
					}
					break;
				case Constantes.INTERCAMBIO_TIPO_TALLER_ARTESANO :// invitar oficio
					final int invitadoID = Integer.parseInt(split[1]);
					final int trabajoID = Integer.parseInt(split[2]);
					Trabajo trabajo = null;
					boolean paso = false;
					for (final StatOficio statOficio : _perso.getStatsOficios().values()) {
						if (statOficio.getPosicion() == 7) {
							continue;
						}
						for (final Trabajo t : statOficio.trabajosARealizar()) {
							if (t.getTrabajoID() != trabajoID) {
								continue;
							}
							trabajo = t;
							paso = true;
							break;
						}
						if (paso) {
							break;
						}
					}
					if (trabajo == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					final Personaje invitandoA = Mundo.getPersonaje(invitadoID);
					if (invitandoA == null || invitandoA == _perso) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					if (!invitandoA.estaVisiblePara(_perso)) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1209");
						return;
					}
					_perso.setInvitandoA(invitandoA, "taller");
					invitandoA.setInvitador(_perso, "taller");
					_perso.setExchanger(trabajo);
					GestorSalida.ENVIAR_ERK_CONSULTA_INTERCAMBIO(_perso, _perso.getID(), invitandoA.getID(),
					Constantes.INTERCAMBIO_TIPO_TALLER_ARTESANO);
					GestorSalida.ENVIAR_ERK_CONSULTA_INTERCAMBIO(invitandoA, _perso.getID(), invitandoA.getID(),
					Constantes.INTERCAMBIO_TIPO_TALLER_CLIENTE);
					break;
				case Constantes.INTERCAMBIO_TIPO_MONTURA :// dragopavo mochila
					if (_perso.getMontura() == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					_perso.setExchanger(_perso.getMontura());
					GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(_perso, tipo, _perso.getMontura().getID() + "");
					GestorSalida.ENVIAR_EL_LISTA_EXCHANGER(_perso, _perso.getMontura());
					GestorSalida.ENVIAR_Ew_PODS_MONTURA(_perso);
					break;
				case Constantes.INTERCAMBIO_TIPO_TIENDA_NPC :// tienda NPC
					final int npcID = Integer.parseInt(split[1]);
					final NPC npc = _perso.getMapa().getNPC(npcID);
					if (npc == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					_perso.setExchanger(npc);
					GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(_perso, tipo, npcID + "");
					GestorSalida.ENVIAR_EL_LISTA_EXCHANGER(_perso, npc);
					break;
				case Constantes.INTERCAMBIO_TIPO_PERSONAJE :// intercambio
					final int objetidoID = Integer.parseInt(split[1]);
					final Personaje invitandoA2 = Mundo.getPersonaje(objetidoID);
					if (invitandoA2 == null || invitandoA2 == _perso || invitandoA2.getMapa() != _perso.getMapa() || !invitandoA2
					.enLinea()) {
						GestorSalida.ENVIAR_ERE_ERROR_CONSULTA(_perso, 'E');
						return;
					}
					if (!invitandoA2.estaVisiblePara(_perso)) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1209");
						return;
					}
					if (!invitandoA2.estaDisponible(false, true)) {
						GestorSalida.ENVIAR_ERE_ERROR_CONSULTA(_perso, 'O');
						return;
					}
					_perso.setInvitandoA(invitandoA2, "intercambio");
					invitandoA2.setInvitador(_perso, "intercambio");
					GestorSalida.ENVIAR_ERK_CONSULTA_INTERCAMBIO(_perso, _perso.getID(), invitandoA2.getID(),
					Constantes.INTERCAMBIO_TIPO_PERSONAJE);
					GestorSalida.ENVIAR_ERK_CONSULTA_INTERCAMBIO(invitandoA2, _perso.getID(), invitandoA2.getID(),
					Constantes.INTERCAMBIO_TIPO_PERSONAJE);
					break;
				case Constantes.INTERCAMBIO_TIPO_TRUEQUE :// intercambio npc
				case Constantes.INTERCAMBIO_TIPO_RESUCITAR_MASCOTA :// resucitar mascota
					final int npcID2 = Integer.parseInt(split[1]);
					final NPC npc2 = _perso.getMapa().getNPC(npcID2);
					if (npc2 == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					Trueque trueque = new Trueque(_perso, tipo == Constantes.INTERCAMBIO_TIPO_RESUCITAR_MASCOTA, npc2
					.getModeloID());
					_perso.setExchanger(trueque);
					GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(_perso, Constantes.INTERCAMBIO_TIPO_TRUEQUE, "");
					break;
				case Constantes.INTERCAMBIO_TIPO_MERCANTE :// mercante
					final int mercanteID = Integer.parseInt(split[1]);
					Personaje mercante = Mundo.getPersonaje(mercanteID);
					if (mercante == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					_perso.setExchanger(mercante);
					GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(_perso, tipo, mercanteID + "");
					GestorSalida.ENVIAR_EL_LISTA_EXCHANGER(_perso, mercante);
					break;
				case Constantes.INTERCAMBIO_TIPO_MI_TIENDA :// misma tienda
					_perso.setExchanger(_perso.getTienda());
					GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(_perso, tipo, _perso.getID() + "");
					GestorSalida.ENVIAR_EL_LISTA_EXCHANGER(_perso, _perso);
					break;
				case Constantes.INTERCAMBIO_TIPO_RECAUDADOR :
					final int recaudaID = Integer.parseInt(split[1]);
					final Recaudador recaudador = Mundo.getRecaudador(recaudaID);
					if (recaudador == null || recaudador.getPelea() != null || recaudador.getEnRecolecta() || _perso
					.getGremio() == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					if (recaudador.getGremio().getID() != _perso.getGremio().getID()) {
						GestorSalida.ENVIAR_BN_NADA(_perso, "NO ES DEL GREMIO");
						return;
					}
					if (!_perso.getMiembroGremio().puede(Constantes.G_RECOLECTAR_RECAUDADOR)) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1101");
						return;
					}
					recaudador.setEnRecolecta(true);
					GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(_perso, tipo, recaudador.getID() + "");
					GestorSalida.ENVIAR_EL_LISTA_EXCHANGER(_perso, recaudador);
					_perso.setExchanger(recaudador);
					break;
				default :
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
			}
			_perso.setTipoExchange(tipo);
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", intercambio_Iniciar " + e.toString());
			e.printStackTrace();
		}
	}
	
	private synchronized void intercambio_Aceptar() {
		switch (_perso.getTipoInvitacion()) {
			case "taller" :
				final Personaje artesano = _perso.getInvitador();
				if (artesano == null) {
					GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO ACEPTAR ARTESANO NULO");
					return;
				}
				Trabajo trabajo = (Trabajo) artesano.getIntercambiandoCon(Trabajo.class);
				if (trabajo == null) {
					GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO ACEPTAR TRABAJO NULO");
					return;
				}
				// final InvitarTaller taller = new InvitarTaller(artesano, _perso, trabajo);
				artesano.setTipoExchange(Constantes.INTERCAMBIO_TIPO_TALLER_ARTESANO);
				_perso.setTipoExchange(Constantes.INTERCAMBIO_TIPO_TALLER_CLIENTE);
				_perso.setExchanger(trabajo);
				GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(artesano, Constantes.INTERCAMBIO_TIPO_TALLER_ARTESANO, trabajo
				.getCasillasMax() + ";" + trabajo.getTrabajoID());
				GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(_perso, Constantes.INTERCAMBIO_TIPO_TALLER_CLIENTE, trabajo
				.getCasillasMax() + ";" + trabajo.getTrabajoID());
				_perso.setInvitador(null, "");
				artesano.setInvitandoA(null, "");
				trabajo.setArtesanoCliente(artesano, _perso);
				break;
			case "intercambio" :
				final Personaje invitador = _perso.getInvitador();
				if (invitador == null) {
					GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO ACEPTAR INTERCAMBIO NULO");
					return;
				}
				final Intercambio intercambio = new Intercambio(invitador, _perso);
				invitador.setTipoExchange(Constantes.INTERCAMBIO_TIPO_PERSONAJE);
				_perso.setTipoExchange(Constantes.INTERCAMBIO_TIPO_PERSONAJE);
				invitador.setExchanger(intercambio);
				_perso.setExchanger(intercambio);
				GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(invitador, Constantes.INTERCAMBIO_TIPO_PERSONAJE, "");
				GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(_perso, Constantes.INTERCAMBIO_TIPO_PERSONAJE, "");
				_perso.setInvitador(null, "");
				invitador.setInvitandoA(null, "");
				break;
		}
	}
	
	private synchronized void intercambio_Cerrar() {
		_perso.cerrarExchange("");
	}
	
	private synchronized void intercambio_Boton_OK() {
		if (_perso.getExchanger() != null) {
			_perso.getExchanger().botonOK(_perso);
		}
	}
	
	private void intercambio_Oficios(final String packet) {
		switch (packet.charAt(2)) {
			case 'F' :
				final int idOficio = Integer.parseInt(packet.substring(3));
				for (final Personaje artesano : Mundo.getPersonajesEnLinea()) {
					final Mapa mapa = artesano.getMapa();
					for (final StatOficio oficio : artesano.getStatsOficios().values()) {
						if (oficio.getLibroArtesano() && oficio.getOficio().getID() == idOficio) {
							GestorSalida.ENVIAR_EJ_DESCRIPCION_LIBRO_ARTESANO(_perso, "+" + oficio.getOficio().getID() + ";"
							+ artesano.getID() + ";" + artesano.getNombre() + ";" + oficio.getNivel() + ";" + mapa.getID() + ";"
							+ (mapa.getTrabajos().isEmpty() ? 0 : 1) + ";" + artesano.getClaseID(true) + ";" + artesano.getSexo()
							+ ";" + artesano.getColor1() + "," + artesano.getColor2() + "," + artesano.getColor3() + ";" + artesano
							.getStringAccesorios() + ";" + oficio.getOpcionBin() + "," + oficio.getSlotsPublico());
						}
					}
				}
				GestorSalida.ENVIAR_BN_NADA(_perso);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR INTERCAMBIO OFICIO: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "intercambio_Oficios()");
				}
				break;
		}
	}
	
	private void intercambio_Oficio_Publico(final String packet) {
		switch (packet.charAt(2)) {
			case '+' :
				GestorSalida.ENVIAR_EW_OFICIO_MODO_PUBLICO(_perso, "+");
				_perso.packetModoInvitarTaller(null, false);
				break;
			case '-' :
				// for (StatsOficio SO : _perso.getStatsOficios().values()) {
				// if (SO.getPosicion() != 7)
				// GestorSalida.ENVIAR_Ej_AGREGAR_LIBRO_ARTESANO(_perso, "-" + SO.getOficio().getID());
				// }
				GestorSalida.ENVIAR_EW_OFICIO_MODO_PUBLICO(_perso, "-");
				GestorSalida.ENVIAR_EW_OFICIO_MODO_INVITACION(_perso, "-", _perso.getID(), "");
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR INTER OFICIO PUBLICO: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "intercambio_Oficio_Publico()");
				}
				break;
		}
	}
	
	private void intercambio_Mover_Objeto(final String packet) {
		try {
			switch (packet.charAt(2)) {
				case 'G' :
					try {
						long kamas = 0;
						try {
							kamas = Long.parseLong(packet.substring(3));
						} catch (final Exception e) {
							GestorSalida.ENVIAR_BN_NADA(_perso, "KAMAS EXCEPTION");
							return;
						}
						if (kamas < 0) {// retirar
							kamas = Math.abs(kamas);
							switch (_perso.getTipoExchange()) {
								case Constantes.INTERCAMBIO_TIPO_RECAUDADOR :
								case Constantes.INTERCAMBIO_TIPO_COFRE :
									if (_perso.getExchanger() == null) {
										return;
									}
									if (_perso.getExchanger().getKamas() < kamas) {
										kamas = _perso.getExchanger().getKamas();
									}
									_perso.getExchanger().addKamas(-kamas, _perso);
									_perso.addKamas(kamas, false, true);
									GestorSalida.ENVIAR_EsK_MOVER_A_TIENDA_COFRE_BANCO(_perso, "G" + _perso.getExchanger().getKamas());
									break;
							}
						} else {// si kamas > 0
							if (_perso.getKamas() < kamas) {
								kamas = _perso.getKamas();
							}
							switch (_perso.getTipoExchange()) {
								case Constantes.INTERCAMBIO_TIPO_RECAUDADOR :
								case Constantes.INTERCAMBIO_TIPO_COFRE :
									if (_perso.getExchanger() == null) {
										return;
									}
									_perso.getExchanger().addKamas(kamas, _perso);
									_perso.addKamas(-kamas, false, true);
									GestorSalida.ENVIAR_EsK_MOVER_A_TIENDA_COFRE_BANCO(_perso, "G" + _perso.getExchanger().getKamas());
									break;
								case Constantes.INTERCAMBIO_TIPO_PERSONAJE :// intercambio
									if (_perso.getExchanger() == null) {
										return;
									}
									_perso.getExchanger().addKamas(kamas, _perso);
									break;
							}
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO MOVER OBJETO KAMAS");
						MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", intercambio_Mover_Objeto(kamas) " + e
						.toString());
						e.printStackTrace();
					}
					break;
				case 'O' :
					try {
						String sp = packet.substring(3).replace("-", ";-").replace("+", ";+");
						boolean varios = false;
						String[] split = sp.split(Pattern.quote(";"));
						if (MainServidor.PARAM_MOVER_MULTIPLE_OBJETOS_SOLO_ABONADOS && _perso.getCuenta().getVip() == 0) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Réservé au V.I.P", "B9121B");
							} else {
								GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1ONLY_FOR_VIP");
							}
							return;
						}
						for (String sPacket : split) {
							if (sPacket.isEmpty()) {
								continue;
							}
							if (varios) {
								Thread.sleep(500);// es para evitar lag en packets
							}
							varios = true;
							final String[] infos = sPacket.substring(1).split(Pattern.quote("|"));
							int id = -1, cantidad = -1;
							int precio = 0;
							try {
								id = Integer.parseInt(infos[0]);
								cantidad = Integer.parseInt(infos[1]);
							} catch (final Exception e) {}
							Objeto objeto = Mundo.getObjeto(id);
							if (cantidad < 0 || objeto == null) {
								GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1OBJECT_DONT_EXIST~" + id);
								continue;
							}
							if (objeto.getObjModelo().getTipo() == Constantes.OBJETO_TIPO_OBJETO_MISION) {
								GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO MOVER TIPO MISION");
								continue;
							}
							if (objeto.getObjModelo().getTipo() == Constantes.OBJETO_TIPO_OBJETO_DE_BUSQUEDA) {
								GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO MOVER TIPO BUSQUEDA");
								continue;
							}
							if (_perso.getTipoExchange() != Constantes.INTERCAMBIO_TIPO_COFRE) {
								if (objeto.tieneStatTexto(Constantes.STAT_LIGADO_A_CUENTA)) {
									GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO MOVER LIGADO");
									continue;
								}
								if (!objeto.pasoIntercambiableDesde()) {
									GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO MOVER NO INTERCAMBIABLE");
									return;
								}
							}
							char c = sPacket.charAt(0);
							switch (c) {
								case '+' :
									if (cantidad > objeto.getCantidad()) {
										cantidad = objeto.getCantidad();
									}
									switch (_perso.getTipoExchange()) {
										case Constantes.INTERCAMBIO_TIPO_TALLER :
										case Constantes.INTERCAMBIO_TIPO_RECAUDADOR :
										case Constantes.INTERCAMBIO_TIPO_PERSONAJE :// intercambio
										case Constantes.INTERCAMBIO_TIPO_TALLER_ARTESANO :// invitar oficio
										case Constantes.INTERCAMBIO_TIPO_TALLER_CLIENTE :// invitar oficio
										case Constantes.INTERCAMBIO_TIPO_TRUEQUE :// intercambio npc
										case Constantes.INTERCAMBIO_TIPO_MI_TIENDA :// misma tienda
										case Constantes.INTERCAMBIO_TIPO_MERCADILLO_VENDER :// mercadillo vender
										case Constantes.INTERCAMBIO_TIPO_RESUCITAR_MASCOTA :// resucitar mascota
										case Constantes.INTERCAMBIO_TIPO_MONTURA :// dragopavo mochila
										case Constantes.INTERCAMBIO_TIPO_COFRE :
											if (_perso.getExchanger() == null) {
												continue;
											}
											// if (_perso.getObjeto(id) == null) {
											// GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1OBJECT_DONT_EXIST");
											// continue;
											// }
											try {
												precio = Integer.parseInt(infos[2]);
											} catch (final Exception e) {}
											if (precio < 0) {
												GestorSalida.ENVIAR_BN_NADA(_perso);
												continue;
											}
											_perso.getExchanger().addObjetoExchanger(objeto, cantidad, _perso, precio);
											break;
									}
									break;
								case '-' :
									switch (_perso.getTipoExchange()) {
										case Constantes.INTERCAMBIO_TIPO_PERSONAJE :// intercambio
										case Constantes.INTERCAMBIO_TIPO_TALLER_ARTESANO :// invitar oficio
										case Constantes.INTERCAMBIO_TIPO_TALLER_CLIENTE :// invitar oficio
										case Constantes.INTERCAMBIO_TIPO_TRUEQUE :// intercambio npc
											if (_perso.getObjeto(id) == null) {
												GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1OBJECT_DONT_EXIST");
												continue;
											}
										case Constantes.INTERCAMBIO_TIPO_TALLER :
										case Constantes.INTERCAMBIO_TIPO_RECAUDADOR :
										case Constantes.INTERCAMBIO_TIPO_MI_TIENDA :// misma tienda
										case Constantes.INTERCAMBIO_TIPO_MERCADILLO_VENDER :// mercadillo vender
										case Constantes.INTERCAMBIO_TIPO_RESUCITAR_MASCOTA :// resucitar mascota
										case Constantes.INTERCAMBIO_TIPO_MONTURA :// dragopavo mochila
										case Constantes.INTERCAMBIO_TIPO_COFRE :
											if (_perso.getExchanger() == null) {
												continue;
											}
											try {
												precio = Integer.parseInt(infos[2]);
											} catch (final Exception e) {}
											if (precio < 0) {
												GestorSalida.ENVIAR_BN_NADA(_perso);
												continue;
											}
											_perso.getExchanger().remObjetoExchanger(objeto, cantidad, _perso, precio);
											break;
									}
									break;
							}
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO MOVER OBJETO");
						MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", intercambio_Mover_Objeto " + e
						.toString());
						e.printStackTrace();
					}
					GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_perso);
					if (_perso.getTipoExchange() == Constantes.INTERCAMBIO_TIPO_MONTURA && _perso.getMontura() != null) {
						GestorSalida.ENVIAR_Ew_PODS_MONTURA(_perso);
					}
					break;
				case 'R' :// varios craft o un craft
					switch (_perso.getTipoExchange()) {
						case Constantes.INTERCAMBIO_TIPO_TALLER :
							Trabajo trabajo = (Trabajo) _perso.getIntercambiandoCon(Trabajo.class);
							if (trabajo == null) {
								GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO MOVER OBJETO TRABAJO NULL 'R'");
								return;
							}
							if (trabajo.esCraft()) {
								trabajo.craftearXVeces(Integer.parseInt(packet.substring(3)));
							}
							break;
					}
					break;
				case 'r' :
					switch (_perso.getTipoExchange()) {
						case Constantes.INTERCAMBIO_TIPO_TALLER :
							Trabajo trabajo = (Trabajo) _perso.getIntercambiandoCon(Trabajo.class);
							if (trabajo == null) {
								GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO MOVER OBJETO TRABAJO NULL 'r'");
								return;
							}
							if (trabajo.esCraft()) {
								trabajo.interrumpirReceta();
							}
							break;
					}
					break;
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "INTERCAMBIO MOVER OBJETO FINAL");
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", intercambio_Mover_Objeto(final) " + e
			.toString());
			e.printStackTrace();
		}
	}
	
	private void intercambio_Pago_Por_Trabajo(final String packet) {
		final Trabajo taller = (Trabajo) _perso.getIntercambiandoCon(Trabajo.class);
		if (taller == null || !taller.esTaller()) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		final int tipoPago = Integer.parseInt(packet.substring(2, 3));
		final char caracter = packet.charAt(3);
		final char signo = packet.charAt(4);
		switch (caracter) {
			case 'G' :
				long kamas = 0;
				try {
					kamas = Long.parseLong(packet.substring(4));
				} catch (final Exception e) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
				}
				if (kamas < 0) {
					kamas = 0;
				}
				taller.setKamas(tipoPago, kamas, _perso.getKamas());
				break;
			case 'O' :
			default :
				final String[] infos = packet.substring(5).split(Pattern.quote("|"));
				int id = -1, cantidad = 0;
				try {
					id = Integer.parseInt(infos[0]);
					cantidad = Integer.parseInt(infos[1]);
				} catch (final Exception e) {}
				final Objeto objeto = _perso.getObjeto(id);
				if (cantidad <= 0 || objeto == null) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1OBJECT_DONT_EXIST");
					return;
				}
				if (objeto.tieneStatTexto(Constantes.STAT_LIGADO_A_CUENTA)) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1129");
					return;
				}
				if (!objeto.pasoIntercambiableDesde()) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1129");
					return;
				}
				final int cantInter = taller.getCantObjetoPago(id, tipoPago);
				switch (signo) {
					case '+' :
						final int nuevaCant = objeto.getCantidad() - cantInter;
						if (cantidad > nuevaCant) {
							cantidad = nuevaCant;
						}
						taller.addObjetoPaga(objeto, cantidad, tipoPago);
						break;
					case '-' :
						if (cantidad > cantInter) {
							cantidad = cantInter;
						}
						taller.quitarObjetoPaga(objeto, cantidad, tipoPago);
						break;
				}
				break;
		}
	}
	
	private void intercambio_Preg_Mercante() {
		if (_perso.getDeshonor() >= 4) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "183");
			return;
		}
		if (!_perso.estaDisponible(false, false)) {
			return;
		}
		final int tasa = 1;// _perso.getNivel() / 2;
		long impuesto = _perso.precioTotalTienda() * tasa / 1000;
		short mapaID = _perso.getMapa().getID();
		if (Constantes.esMapaMercante(mapaID)) {
			impuesto = 0;
		}
		GestorSalida.ENVIAR_Eq_PREGUNTAR_MERCANTE(_perso, _perso.getObjetosTienda().size(), tasa, impuesto);
	}
	
	private void intercambio_Ok_Mercante() {
		if (!_perso.estaDisponible(false, false)) {
			return;
		}
		if (_perso.getObjetosTienda().isEmpty()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "123");
			return;
		}
		if (_perso.getMapa().cantMercantes() >= _perso.getMapa().getMaxMercantes()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "125;" + _perso.getMapa().getMaxMercantes());
			return;
		}
		if (!_perso.getCelda().librerParaMercante() || !_perso.getCelda().esCaminable(false)) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "124");
			return;
		}
		long impuesto = _perso.precioTotalTienda() / 1000;
		short mapaID = _perso.getMapa().getID();
		if (Constantes.esMapaMercante(mapaID)) {
			impuesto = 0;
		}
		if (impuesto < 0 || _perso.getKamas() < impuesto) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "176");
		} else {
			_perso.addKamas(-impuesto, false, true);
			_perso.setMercante(true);
			_perso.getMapa().addMercante(_perso);
			cerrarSocket(true, "intercambio_Ok_Mercante()");
		}
	}
	
	private void intercambio_Mercadillo(final String packet) {
		try {
			final Mercadillo mercadillo = (Mercadillo) _perso.getIntercambiandoCon(Mercadillo.class);
			if (mercadillo == null) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			switch (packet.charAt(2)) {
				case 'B' :// comprar objeto mercadillo
					final String[] info = packet.substring(3).split(Pattern.quote("|"));
					if (mercadillo.comprarObjeto(Integer.parseInt(info[0]), Integer.parseInt(info[1]), Long.parseLong(info[2]),
					_perso)) {
						GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_perso);
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "068");
					} else {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "172");
					}
					break;
				case 'l' :
					final String str = mercadillo.strListaLineasPorModelo(Integer.parseInt(packet.substring(3)));
					if (str.isEmpty()) {
						GestorSalida.ENVIAR_EHM_MOVER_OBJMERCA_POR_MODELO(_perso, "-", Integer.parseInt(packet.substring(3)) + "");
					} else {
						GestorSalida.ENVIAR_EHl_LISTA_LINEAS_OBJMERCA_POR_MODELO(_perso, str);
					}
					break;
				case 'P' :// precio promedio
					GestorSalida.ENVIAR_EHP_PRECIO_PROMEDIO_OBJ(_perso, Integer.parseInt(packet.substring(3)), Mundo
					.getObjetoModelo(Integer.parseInt(packet.substring(3))).getPrecioPromedio());
					break;
				case 'S' : // buscar
					final String[] splt = packet.substring(3).split(Pattern.quote("|"));
					if (mercadillo.esTipoDeEsteMercadillo(Integer.parseInt(splt[0]))) {
						if (mercadillo.hayModeloEnEsteMercadillo(Integer.parseInt(splt[0]), Integer.parseInt(splt[1]))) {
							GestorSalida.ENVIAR_EHS_BUSCAR_OBJETO_MERCADILLO(_perso, "K");
							GestorSalida.ENVIAR_EHl_LISTA_LINEAS_OBJMERCA_POR_MODELO(_perso, mercadillo.strListaLineasPorModelo(
							Integer.parseInt(splt[1])));
						} else {
							GestorSalida.ENVIAR_EHS_BUSCAR_OBJETO_MERCADILLO(_perso, "E");
						}
					} else {
						GestorSalida.ENVIAR_EHS_BUSCAR_OBJETO_MERCADILLO(_perso, "E");
					}
					break;
				case 'T' :// lista por tipo de objeto
					GestorSalida.ENVIAR_EHL_LISTA_OBJMERCA_POR_TIPO(_perso, Integer.parseInt(packet.substring(3)), mercadillo
					.stringModelo(Integer.parseInt(packet.substring(3))));
					break;
				default :
					MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR INTERCAMBIO MERCADILLO: " + packet);
					if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
						MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
						cerrarSocket(true, "intercambio_Mercadillo()");
					}
					return;
			}
		} catch (final Exception e) {}
	}
	
	private synchronized void intercambio_Cercado(final String packet) {
		try {
			Cercado cercado = (Cercado) _perso.getIntercambiandoCon(Cercado.class);
			if (cercado == null) {
				return;
			}
			if (_perso.getDeshonor() >= 5) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "183");
				GestorSalida.ENVIAR_EV_CERRAR_VENTANAS(_perso, "");
				return;
			}
			final char c = packet.charAt(2);
			String packet2 = packet.substring(3);
			int id = -1;
			try {
				id = Integer.parseInt(packet2);
			} catch (final Exception e) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			Montura montura = null;
			switch (c) {
				case 'g' :// cercado => a establo
					if (!cercado.borrarMonturaCercado(id)) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					montura = Mundo.getMontura(id);
					if (montura == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					GestorSalida.ENVIAR_Ef_MONTURA_A_CRIAR(_perso, '-', montura.getID() + "");
					GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(_perso.getMapa(), id);
					if (!escapaDespuesParir(montura)) {
						montura.setMapaCelda(null, null);
						_cuenta.addMonturaEstablo(montura);
						GestorSalida.ENVIAR_Ee_MONTURA_A_ESTABLO(_perso, '+', montura.detallesMontura());
					}
					break;
				case 'p' :// establo => cercado
					if (!cercado.puedeAgregar()) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1107");
						return;
					}
					if (!_cuenta.borrarMonturaEstablo(id)) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					montura = Mundo.getMontura(id);
					if (_perso.getMontura() != null && _perso.getMontura().getID() == id) {
						if (_perso.estaMontando()) {
							_perso.subirBajarMontura(false);
						}
						_perso.setMontura(null);
					}
					montura.setMapaCelda(_perso.getMapa(), _perso.getMapa().getCelda(cercado.getCeldaMontura()));
					cercado.addCriando(montura);
					GestorSalida.ENVIAR_Ef_MONTURA_A_CRIAR(_perso, '+', montura.detallesMontura());
					GestorSalida.ENVIAR_Ee_MONTURA_A_ESTABLO(_perso, '-', montura.getID() + "");
					GestorSalida.ENVIAR_GM_DRAGOPAVO_A_MAPA(_perso.getMapa(), "+", montura);
					break;
				default :
					MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR INTERCAMBIO CERCADO: " + packet);
					if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
						MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
						cerrarSocket(true, "intercambio_Cercado()");
					}
					break;
			}
		} catch (Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
	}
	
	private synchronized void intercambio_Establo(final String packet) {
		try {
			Cercado cercado = (Cercado) _perso.getIntercambiandoCon(Cercado.class);
			if (cercado == null) {
				return;
			}
			if (_perso.getDeshonor() >= 5) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "183");
				GestorSalida.ENVIAR_EV_CERRAR_VENTANAS(_perso, "");
				return;
			}
			final char c = packet.charAt(2);
			String packet2 = packet.substring(3);
			int id = -1;
			try {
				id = Integer.parseInt(packet2);
			} catch (final Exception e) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			Montura montura = null;
			switch (c) {
				case 'C' :// certificado / pergamino => establo
					final Objeto obj = _perso.getObjeto(id);
					if (obj == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					if (!Condiciones.validaCondiciones(_perso, obj.getObjModelo().getCondiciones())) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "119|43");
						return;
					}
					montura = Mundo.getMontura(-Math.abs(obj.getStatValor(Constantes.STAT_CONSULTAR_MONTURA)));
					if (montura == null) {
						final int color = Constantes.getColorMonturaPorCertificado(obj.getObjModeloID());
						if (color < 1) {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1MOUNT_COLOR_NOT_EXIST");
							return;
						}
						montura = new Montura(color, _perso.getID(), true, false);
					}
					if (obj.getCantidad() <= 1) {
						_perso.borrarOEliminarConOR(id, true);
					} else {
						obj.setCantidad(obj.getCantidad() - 1);
						GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_perso, obj);
					}
					if (montura.getPergamino() != -1 && montura.getPergamino() != obj.getID()) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1MOUNT_IS_NOT_CERTIFICATED");
						return;
					}
					if (!escapaDespuesParir(montura)) {
						_cuenta.addMonturaEstablo(montura);
						montura.setDueñoID(_perso.getID());
						montura.setPergamino(0);
						GestorSalida.ENVIAR_Ee_MONTURA_A_ESTABLO(_perso, '+', montura.detallesMontura());
					}
					break;
				case 'c' :// Establo => certificado / pergamino
					montura = Mundo.getMontura(id);
					if (montura == null || !_cuenta.borrarMonturaEstablo(id)) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1104");
						return;
					}
					if (montura.getPergamino() > 0) {
						GestorSalida.ENVIAR_Ee_MONTURA_A_ESTABLO(_perso, '-', montura.getID() + "");
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1104");
						return;
					}
					final Objeto obj1 = montura.getObjModCertificado().crearObjeto(1, Constantes.OBJETO_POS_NO_EQUIPADO,
					CAPACIDAD_STATS.RANDOM);
					obj1.fijarStatValor(Constantes.STAT_CONSULTAR_MONTURA, Math.abs(montura.getID()));
					obj1.addStatTexto(Constantes.STAT_PERTENECE_A, "0#0#0#" + _perso.getNombre());
					obj1.addStatTexto(Constantes.STAT_NOMBRE, "0#0#0#" + montura.getNombre());
					_perso.addObjetoConOAKO(obj1, true);
					montura.setMapaCelda(null, null);
					montura.setPergamino(obj1.getID());
					GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_perso);
					GestorSalida.ENVIAR_Ee_MONTURA_A_ESTABLO(_perso, '-', montura.getID() + "");
					GestorSQL.REPLACE_MONTURA(montura, false);
					break;
				case 'g' :// Establo = Equipar montura
					if (_perso.getMontura() != null) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1YOU_HAVE_MOUNT");
						return;
					}
					montura = Mundo.getMontura(id);
					if (montura == null || _cuenta.getEstablo().remove(id) == null) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1104");
						return;
					}
					if (montura.getPergamino() > 0) {
						GestorSalida.ENVIAR_Ee_MONTURA_A_ESTABLO(_perso, '-', montura.getID() + "");
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1104");
						return;
					}
					montura.setMapaCelda(null, null);
					_perso.setMontura(montura);
					GestorSalida.ENVIAR_Ee_MONTURA_A_ESTABLO(_perso, '-', montura.getID() + "");
					GestorSalida.ENVIAR_Rx_EXP_DONADA_MONTURA(_perso);
					break;
				case 'p' :// Equipar => Establo
					montura = _perso.getMontura();
					if (montura == null || montura.getID() != id) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1YOU_DONT_HAVE_MOUNT");
						return;
					}
					if (!montura.getObjetos().isEmpty()) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1106");
						return;
					}
					if (_perso.estaMontando()) {
						_perso.subirBajarMontura(false);
					}
					_perso.setMontura(null);
					if (!escapaDespuesParir(montura)) {
						montura.setMapaCelda(null, null);
						montura.setUbicacion(Ubicacion.ESTABLO);
						_cuenta.addMonturaEstablo(montura);
						GestorSalida.ENVIAR_Ee_MONTURA_A_ESTABLO(_perso, '+', montura.detallesMontura());
					}
					GestorSalida.ENVIAR_Rx_EXP_DONADA_MONTURA(_perso);
					break;
				default :
					MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR INTERCAMBIO ESTABLO: " + packet);
					if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
						MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
						cerrarSocket(true, "intercambio_Establo()");
					}
					break;
			}
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", intercambio_Establo " + e.toString());
			e.printStackTrace();
		}
	}
	
	private synchronized boolean escapaDespuesParir(Montura madre) {
		final Montura padre = Mundo.getMontura(madre.getParejaID());
		if (madre.getFecundadaHaceMinutos() >= (MainServidor.HORAS_PERDER_CRIAS_MONTURA * 60)) {
			// las crias mueren por tiempo
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1112");
			madre.aumentarReproduccion();
		} else if (madre.getFecundadaHaceMinutos() >= madre.minutosParir()) {
			// nacen las crias
			int crias = Formulas.getRandomInt(1, 2);
			if (madre.getCapacidades().contains(Constantes.HABILIDAD_REPRODUCTORA)) {
				crias *= 2;
			}
			if (madre.getReprod() + crias > 20) {
				crias = 20 - madre.getReprod();
			}
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, (crias == 1 ? 1110 : 1111) + ";" + crias);
			for (int i = 1; i <= crias; i++) {
				final Montura bebeMontura = new Montura(madre, padre);
				GestorSalida.ENVIAR_Ee_MONTURA_A_ESTABLO(_perso, '~', bebeMontura.detallesMontura());
				madre.aumentarReproduccion();
				bebeMontura.setMapaCelda(null, null);
				_cuenta.addMonturaEstablo(bebeMontura);
			}
		} else {
			return false;
		}
		if (padre != null) {
			GestorSQL.REPLACE_MONTURA(padre, false);
		}
		madre.restarAmor(7500);
		madre.restarResistencia(7500);
		madre.setFecundada(false);
		return madre.pudoEscapar();
	}
	
	private synchronized void intercambio_Repetir_Ult_Craft() {
		final Trabajo trabajo = (Trabajo) _perso.getIntercambiandoCon(Trabajo.class);
		if (trabajo == null) {
			return;
		}
		trabajo.ponerIngredUltRecet();
	}
	
	private synchronized void intercambio_Vender(final String packet) {
		try {
			switch (_perso.getTipoExchange()) {
				case Constantes.INTERCAMBIO_TIPO_TIENDA_NPC :// npc
					// case 20 ://boutique
					_perso.venderObjetos(packet.substring(2));
					return;
			}
		} catch (final Exception e) {}
		GestorSalida.ENVIAR_ESE_ERROR_VENTA(_perso);
	}
	
	private synchronized void intercambio_Comprar(final String packet) {
		try {
			final String[] infos = packet.substring(2).split(Pattern.quote("|"));
			switch (_perso.getTipoExchange()) {
				case Constantes.INTERCAMBIO_TIPO_TIENDA_NPC :// npc
				case Constantes.INTERCAMBIO_TIPO_BOUTIQUE :// boutique
					try {
						int objModeloID = 0, cantidad = 0;
						try {
							objModeloID = Integer.parseInt(infos[0]);
							cantidad = Integer.parseInt(infos[1]);
						} catch (final Exception e) {}
						if (cantidad <= 0 || objModeloID <= 0) {
							GestorSalida.ENVIAR_BN_NADA(_perso);
							return;
						}
						final ObjetoModelo objModelo = Mundo.getObjetoModelo(objModeloID);
						if (objModelo == null) {
							GestorSalida.ENVIAR_EBE_ERROR_DE_COMPRA(_perso);
							return;
						}
						CAPACIDAD_STATS capStats = CAPACIDAD_STATS.RANDOM;
						if (_perso.getTipoExchange() == Constantes.INTERCAMBIO_TIPO_TIENDA_NPC) {
							final NPC npc = (NPC) _perso.getIntercambiandoCon(NPC.class);
							if (npc == null) {
								GestorSalida.ENVIAR_EBE_ERROR_DE_COMPRA(_perso);
								return;
							}
							final NPCModelo npcMod = npc.getModelo();
							if (npcMod == null || !npcMod.tieneObjeto(objModeloID)) {
								GestorSalida.ENVIAR_EBE_ERROR_DE_COMPRA(_perso);
								return;
							}
							if (MainServidor.IDS_NPCS_VENDE_OBJETOS_STATS_MAXIMOS.contains(npcMod.getID())) {
								capStats = CAPACIDAD_STATS.MAXIMO;
							}
						} else {
							try {
								if (!Mundo.getNPCModelo(MainServidor.ID_NPC_BOUTIQUE).tieneObjeto(objModeloID)) {
									GestorSalida.ENVIAR_EBE_ERROR_DE_COMPRA(_perso);
									return;
								}
							} catch (final Exception e) {
								GestorSalida.ENVIAR_EBE_ERROR_DE_COMPRA(_perso);
								return;
							}
						}
						if (objModelo.getItemPago() != null) {
							int idItemPago = objModelo.getItemPago()._primero;
							int cantItemPago = objModelo.getItemPago()._segundo;
							if (!Formulas.valorValido(cantidad, cantItemPago)) {
								GestorSalida.ENVIAR_BN_NADA(_perso, "INTENTO BUG MULTIPLICADOR");
								return;
							}
							if (!_perso.tieneObjPorModYCant(idItemPago, cantItemPago * cantidad)) {
								GestorSalida.ENVIAR_Im_INFORMACION(_perso, "14");
								GestorSalida.ENVIAR_EBE_ERROR_DE_COMPRA(_perso);
								return;
							}
							_perso.restarObjPorModYCant(idItemPago, cantItemPago * cantidad);
						} else if (objModelo.getOgrinas() > 0) {
							if (!Formulas.valorValido(cantidad, objModelo.getOgrinas())) {
								GestorSalida.ENVIAR_BN_NADA(_perso, "INTENTO BUG MULTIPLICADOR");
								return;
							}
							final long ogrinas = objModelo.getOgrinas() * cantidad;
							if (ogrinas < 0) {
								GestorSalida.ENVIAR_EBE_ERROR_DE_COMPRA(_perso);
								return;
							}
							if (objModelo.getKamas() > 0) {
								objModelo.setKamas(0);
							}
							if (!GestorSQL.RESTAR_OGRINAS(_cuenta, ogrinas, _perso)) {
								GestorSalida.ENVIAR_EBE_ERROR_DE_COMPRA(_perso);
								return;
							}
						} else {
							if (!Formulas.valorValido(cantidad, objModelo.getKamas())) {
								GestorSalida.ENVIAR_BN_NADA(_perso, "INTENTO BUG MULTIPLICADOR");
								return;
							}
							final long kamas = objModelo.getKamas() * cantidad;
							if (kamas < 0) {
								GestorSalida.ENVIAR_EBE_ERROR_DE_COMPRA(_perso);
								return;
							}
							if (_perso.getKamas() < kamas) {
								GestorSalida.ENVIAR_Im_INFORMACION(_perso, "182");
								GestorSalida.ENVIAR_EBE_ERROR_DE_COMPRA(_perso);
								return;
							}
							_perso.addKamas(-kamas, true, true);
						}
						if (MainServidor.PARAM_OBJETOS_PEFECTOS_COMPRADOS_NPC || objModelo.getOgrinas() > 0) {
							capStats = CAPACIDAD_STATS.MAXIMO;
						}
						Objeto objeto = objModelo.crearObjeto(cantidad, Constantes.OBJETO_POS_NO_EQUIPADO, capStats);
						if (objModelo.getOgrinas() > 0) {
							if (MainServidor.PARAM_NOMBRE_COMPRADOR) {
								objeto.addStatTexto(Constantes.STAT_PERTENECE_Y_NO_VENDER, "0#0#0#" + _perso.getNombre());
							}
							if (MainServidor.PARAM_OBJETOS_OGRINAS_LIGADO) {
								objeto.addStatTexto(Constantes.STAT_LIGADO_A_CUENTA, "0#0#0#" + _perso.getNombre());
							}
						}
						_perso.addObjIdentAInventario(objeto, false);
						GestorSalida.ENVIAR_EBK_COMPRADO(_perso);
						GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_perso);
					} catch (final Exception e) {
						GestorSalida.ENVIAR_EBE_ERROR_DE_COMPRA(_perso);
					}
					break;
				case Constantes.INTERCAMBIO_TIPO_MERCANTE :// mercante
					final Personaje mercante = (Personaje) _perso.getIntercambiandoCon(Personaje.class);
					if (!mercante.esMercante()) {
						GestorSalida.ENVIAR_EBE_ERROR_DE_COMPRA(_perso);
						return;
					}
					try {
						final int cantidad = Integer.parseInt(infos[1]);
						int objetoID = Integer.parseInt(infos[0]);
						Objeto objeto = Mundo.getObjeto(objetoID);
						if (mercante.comprarTienda(_perso, cantidad, objeto)) {
							GestorSalida.ENVIAR_EBK_COMPRADO(_perso);
							GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(_perso);
							GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_perso);
							if (mercante.getTienda().estaVacia()) {
								_perso.getMapa().removerMercante(mercante.getID());
								mercante.setMercante(false);
								_perso.cerrarVentanaExchange("b");
								GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(_perso.getMapa(), mercante.getID());
							} else {
								GestorSalida.ENVIAR_EL_LISTA_EXCHANGER(_perso, mercante);
							}
						} else {
							GestorSalida.ENVIAR_EBE_ERROR_DE_COMPRA(_perso);
						}
					} catch (final Exception e) {
						GestorSalida.ENVIAR_EBE_ERROR_DE_COMPRA(_perso);
						GestorSalida.ENVIAR_EL_LISTA_EXCHANGER(_perso, mercante);
					}
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
		}
	}
	
	private void analizar_Ambiente(final String packet) {
		switch (packet.charAt(1)) {
			case 'D' :
				ambiente_Cambio_Direccion(packet);
				break;
			case 'U' :
				ambiente_Emote(packet);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR AMBIENTE: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Ambiente()");
				}
				break;
		}
	}
	
	private void ambiente_Emote(final String packet) {
		byte emote = -1;
		try {
			emote = Byte.parseByte(packet.substring(2));
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		if (_perso.getPelea() != null) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		if (emote < 0 || !_perso.tieneEmote(emote)) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1CANT_USE_EMOTE");
			return;
		}
		switch (emote) {
			case Constantes.EMOTE_ACOSTARSE :
			case Constantes.EMOTE_SENTARSE :
				if (_perso.estaSentado()) {
					emote = 0;
				}
				_perso.setSentado(!_perso.estaSentado());
				break;
		}
		_perso.setEmoteActivado(emote);
		int tiempo = 0;
		if (emote == Constantes.EMOTE_FLAUTA) {// flauta
			tiempo = 9000;
		} else if (emote == Constantes.EMOTE_CAMPEON) {// campeon
			tiempo = 5000;
		}
		GestorSalida.ENVIAR_eUK_EMOTE_MAPA(_perso.getMapa(), _perso.getID(), emote, tiempo);
		final Cercado cercado = _perso.getMapa().getCercado();
		if (cercado != null) {
			switch (emote) {
				case Constantes.EMOTE_SEÑAL_CON_MANO :
				case Constantes.EMOTE_ENFADARSE :
				case Constantes.EMOTE_APLAUDIR :
				case Constantes.EMOTE_PEDO :
				case Constantes.EMOTE_MOSTRAR_ARMA :
				case Constantes.EMOTE_BESO :
					ArrayList<Montura> monturas = new ArrayList<Montura>();
					for (final Montura montura : cercado.getCriando().values()) {
						if (montura.getDueñoID() == _perso.getID()) {
							monturas.add(montura);
						}
					}
					if (!monturas.isEmpty()) {
						int casillas = 0;
						switch (emote) {
							case Constantes.EMOTE_SEÑAL_CON_MANO :
							case Constantes.EMOTE_ENFADARSE :
								casillas = 1;
								break;
							case Constantes.EMOTE_APLAUDIR :
							case Constantes.EMOTE_PEDO :
								casillas = Formulas.getRandomInt(2, 3);
								break;
							case Constantes.EMOTE_MOSTRAR_ARMA :
							case Constantes.EMOTE_BESO :
								casillas = Formulas.getRandomInt(4, 7);
								break;
						}
						boolean alejar;
						if (emote == Constantes.EMOTE_SEÑAL_CON_MANO || emote == Constantes.EMOTE_APLAUDIR
						|| emote == Constantes.EMOTE_BESO) {
							alejar = false;
						} else {
							alejar = true;
						}
						monturas.get(Formulas.getRandomInt(0, monturas.size() - 1)).moverMontura(_perso, -1, casillas, alejar);
					}
					monturas = null;
					break;
			}
		}
	}
	
	private void ambiente_Cambio_Direccion(final String packet) {
		try {
			if (_perso.getPelea() != null) {
				return;
			}
			final byte dir = Byte.parseByte(packet.substring(2));
			_perso.setOrientacion(dir);
			GestorSalida.ENVIAR_eD_CAMBIAR_ORIENTACION(_perso.getMapa(), _perso.getID(), dir);
		} catch (final Exception e) {}
	}
	
	private void analizar_Hechizos(final String packet) {
		switch (packet.charAt(1)) {
			case 'B' :
				hechizos_Boost(packet);
				break;
			case 'F' :
				hechizos_Olvidar(packet);
				break;
			case 'M' :
				hechizos_Acceso_Rapido(packet);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR HECHIZOS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Hechizos()");
				}
				break;
		}
	}
	
	private void hechizos_Acceso_Rapido(final String packet) {
		try {
			String[] split = packet.substring(2).split(Pattern.quote("|"));
			final int hechizoID = Integer.parseInt(split[0]);
			final int posicion = Integer.parseInt(split[1]);
			_perso.setPosHechizo(hechizoID, Encriptador.getValorHashPorNumero(posicion));
		} catch (final Exception e) {}
	}
	
	private void hechizos_Boost(final String packet) {
		try {
			// if (_perso.getPelea() != null) {
			// GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1CANT_BOOST_IN_GAME");
			// return;
			// }
			if (!_perso.boostearHechizo(Integer.parseInt(packet.substring(2)))) {
				GestorSalida.ENVIAR_SUE_NIVEL_HECHIZO_ERROR(_perso);
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_SUE_NIVEL_HECHIZO_ERROR(_perso);
		}
	}
	
	private void hechizos_Olvidar(final String packet) {
		try {
			if (!_perso.estaOlvidandoHechizo()) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			if (_perso.olvidarHechizo(Integer.parseInt(packet.substring(2)), false, true)) {
				_perso.setOlvidandoHechizo(false);
			}
		} catch (Exception e) {}
	}
	
	private void analizar_Peleas(final String packet) {
		final Pelea pelea = _perso.getPelea();
		switch (packet.charAt(1)) {
			case 'D' :
				if (pelea != null) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
				}
				pelea_Detalles(packet);
				break;
			case 'H' :
				if (pelea == null) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
				}
				pelea.botonAyuda(_perso.getID());
				break;
			case 'L' :
				if (pelea != null) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
				}
				GestorSalida.ENVIAR_fL_LISTA_PELEAS(_perso, _perso.getMapa());
				break;
			case 'N' :
				if (pelea == null) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
				}
				pelea.botonBloquearMasJug(_perso.getID());
				break;
			case 'P' :
				if (pelea == null || _perso.getGrupoParty() == null) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
				}
				pelea.botonSoloGrupo(_perso.getID());
				break;
			case 'S' :
				if (pelea == null) {
					GestorSalida.ENVIAR_BN_NADA(_perso);
					return;
				}
				pelea.botonBloquearEspect(_perso.getID());
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR PELEAS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "analizar_Peleas()");
				}
				break;
		}
	}
	
	private void pelea_Detalles(final String packet) {
		short id = -1;
		try {
			id = Short.parseShort(packet.substring(2).replace("0", ""));
		} catch (final Exception e) {}
		if (id == -1) {
			GestorSalida.ENVIAR_BN_NADA(_perso);
			return;
		}
		GestorSalida.ENVIAR_fD_DETALLES_PELEA(_perso, _perso.getMapa().getPeleas().get(id));
	}
	
	private void analizar_Basicos(final String packet) {
		try {
			switch (packet.charAt(1)) {
				case 'a' :
					basicos_Comandos_Rapidos(packet);
					break;
				case 'A' :
					basicos_Comandos_Consola(packet);
					break;
				case 'D' :
					basicos_Enviar_Fecha();
					registrarUltPing();
					break;
				case 'K' :// sancion por mal vocabulario
					GestorSalida.ENVIAR_BN_NADA(_perso);
					break;
				case 'M' :
					basicos_Chat(packet);
					break;
				case 'R' :// reportar un problema o mensaje
					break;
				case 'S' :
					_perso.mostrarEmoteIcon(packet.substring(2));
					break;
				case 'Q' :// Expulsar mercante de casa
					short celdaMercante = Short.parseShort(packet.substring(2));
					_perso.getMapa().expulsarMercanterPorCelda(celdaMercante);
					break;
				case 'W' :// informacion pj
					basicos_Mensaje_Informacion(packet);
					break;
				case 'Y' :
					basicos_Estado(packet);
					break;
				default :
					MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR BASICOS: " + packet);
					if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
						MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
						cerrarSocket(true, "analizar_Basicos()");
					}
					break;
			}
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", analizar_Basicos " + e.toString());
			e.printStackTrace();
		}
	}
	
	private void basicos_Comandos_Rapidos(final String packet) {
		switch (packet.charAt(2)) {
			case 'M' :// moverse por geoposicion
				if (_cuenta.getAdmin() == 0) {
					GestorSalida.ENVIAR_BN_NADA(_perso, "NO TIENE RANGO");
					return;
				}
				if (!_perso.estaDisponible(false, true)) {
					GestorSalida.ENVIAR_BN_NADA(_perso, "NO ESTA DISPONIBLE");
					return;
				}
				try {
					final String[] infos = packet.substring(3).split(",");
					final int coordX = Integer.parseInt(infos[0]);
					final int coordY = Integer.parseInt(infos[1]);
					final Mapa mapa = Mundo.mapaPorCoordXYContinente(coordX, coordY, _perso.getMapa().getSubArea().getArea()
					.getSuperArea().getID());
					_perso.teleport(mapa.getID(), mapa.getRandomCeldaIDLibre());
				} catch (final Exception e) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1MAPA_NO_EXISTE");
				}
				break;
			case 'K' :// expulsar en un tiempo a pj
				GestorSalida.ENVIAR_BN_NADA(_perso, MainServidor.PALABRA_CLAVE_CONSOLA);
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR BASICO COMANDOS RAPIDOS: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "basicos_Comandos_Rapidos()");
				}
				break;
		}
	}
	
	private void basicos_Comandos_Consola(final String packet) {
		String mensaje = packet.substring(2);
		if (!MainServidor.PALABRA_CLAVE_CONSOLA.isEmpty()) {
			if (!mensaje.contains(MainServidor.PALABRA_CLAVE_CONSOLA)) {
				return;
			}
			mensaje = mensaje.replaceFirst(MainServidor.PALABRA_CLAVE_CONSOLA, "");
		}
		Comandos.consolaComando(mensaje, _cuenta, _perso);
	}
	
	private void basicos_Estado(final String packet) {
		switch (packet.charAt(2)) {
			case 'A' :// ausente
				if (_perso.estaAusente()) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "038");
					_perso.setAusente(false);
				} else {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "037");
					_perso.setAusente(true);
				}
				break;
			case 'I' :// invisible
				if (_perso.esInvisible()) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "051");
					_perso.setInvisible(false);
				} else {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "050");
					_perso.setInvisible(true);
				}
				break;
			case 'O' :
				switch (packet.charAt(3)) {
					case '+' :
					default :
						_perso.addOmitido(packet.substring(4));
						break;
					case '-' :
						_perso.borrarOmitido(packet.substring(4));
						break;
				}
				break;
			default :
				MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR BASICOS ESTADO: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "basicos_Estado()");
				}
				break;
		}
	}
	
	private void basicos_Enviar_Fecha() {
		// Activos para el cliente 1.43.7 by Maestro-yaco 08/11/2025
		GestorSalida.ENVIAR_BD_FECHA_SERVER(_perso);
		GestorSalida.ENVIAR_BT_TIEMPO_SERVER(_perso);
	}
	
	private void basicos_Mensaje_Informacion(final String packet) {
		try {
			final Personaje perso = Mundo.getPersonajePorNombre(packet.substring(2));
			if (perso == null) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			if (!perso.enLinea()) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1211");
				return;
			}
			GestorSalida.ENVIAR_BWK_QUIEN_ES(_perso, perso.getCuenta().getApodo() + "|" + (perso.getPelea() != null ? 2 : 1)
			+ "|" + perso.getNombre() + "|" + perso.getMapa().getID());
		} catch (final Exception e) {}
	}
	
	private void basicos_Chat(final String packet) {
		try {
			String msjChat = "";
			if (_perso.estaMuteado()) {
				final long tiempoTrans = System.currentTimeMillis() - _cuenta.getHoraMuteado();
				if (tiempoTrans > _cuenta.getTiempoMuteado()) {
					_cuenta.mutear(false, 0);
				} else {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1124;" + (_cuenta.getTiempoMuteado() - tiempoTrans) / 1000);
					return;
				}
			}
			String packet2 = packet.replace("<", "").replace(">", "");
			if (packet2.length() <= 3) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			if (packet2.length() > 1500) {
				packet2 = packet2.substring(0, 1499);
			}
			try {
				msjChat = packet2.split("\\|", 2)[1];
				if (msjChat.charAt(msjChat.length() - 1) == '|')
					msjChat = msjChat.substring(0, msjChat.length() - 1);
			} catch (Exception e) {
				msjChat = "";
			}
			if (msjChat.isEmpty()) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			if (!MainServidor.PALABRAS_PROHIBIDAS.isEmpty()) {
				String[] filtro = msjChat.replace(".", " ").split(" ");
				int veces = 0;
				for (String s : filtro) {
					if (MainServidor.PALABRAS_PROHIBIDAS.contains(s.toLowerCase())) {
						veces++;
					}
				}
				if (veces == 0) {
					String filtro2 = msjChat.replace(" ", "");
					for (String s : MainServidor.PALABRAS_PROHIBIDAS) {
						if (s.length() < 5) {
							continue;
						}
						if (filtro2.toLowerCase().contains(s)) {
							veces++;
						}
					}
				}
				if (veces > 0) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1DONT_USE_BLOCK_WORDS;" + veces);
					_cuenta.mutear(true, veces * 60);
					return;
				}
			}
			String sufijo = packet2.charAt(2) + "";
			switch (sufijo) {
				case "$" :// mensaje grupo
					if (!_perso.tieneCanal(sufijo) || _perso.getGrupoParty() == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					GestorSalida.ENVIAR_cMK_MENSAJE_CHAT_GRUPO(_perso, msjChat);
					break;
				case "¿" :// koliseo
					if (_perso.getGrupoKoliseo() == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_KOLISEO(_perso, msjChat);
					break;
				case "~" :// all
					GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_TODOS(sufijo, _perso, msjChat);
					break;
				case "¬" :// unknown
					GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_TODOS(sufijo, _perso, msjChat);
					break;
				case "%" :// mensaje gremio
					if (!_perso.tieneCanal(sufijo) || _perso.getGremio() == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_GREMIO(_perso, msjChat);
					break;
				case "#" :// mensaje pelea equipo
					if (!_perso.tieneCanal(sufijo) || _perso.getPelea() == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					final int equipo = _perso.getPelea().getParamMiEquipo(_perso.getID());
					if (equipo == 4) {
						GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_PELEA(_perso.getPelea(), 4, sufijo, _perso.getID(), _perso.getNombre(),
						msjChat);
					} else {
						GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_PELEA(_perso.getPelea(), equipo, sufijo, _perso.getID(), _perso
						.getNombre(), msjChat);
					}
					break;
				case "*" :
					if (!_perso.tieneCanal(sufijo)) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					if (comando_jugador(msjChat)) {
						return;
					}
					// mensaje mapa
					if (_perso.getPelea() == null) {
						if (!_perso.getMapa().getMuteado() || _cuenta.getAdmin() > 0) {
							GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_MAPA(_perso, sufijo, msjChat);
						} else {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1MAPA_MUTEADO");
						}
					} else {
						final int equipo2 = _perso.getPelea().getParamMiEquipo(_perso.getID());
						if (equipo2 == 1 || equipo2 == 2) {
							GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_PELEA(_perso.getPelea(), 7, "", _perso.getID(), _perso.getNombre(),
							msjChat);
						} else {
							GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_PELEA(_perso.getPelea(), 7, "p", _perso.getID(), _perso.getNombre(),
							msjChat);
						}
					}
					break;
				case "¡" :// mensaje vip
					if (!_cuenta.esAbonado()) {
						GestorSalida.ENVIAR_BN_NADA(_perso, "NO ABONADO");
						return;
					}
					long h;
					if ((h = ((System.currentTimeMillis() - _tiempoUltVIP) / 1000)) < MainServidor.SEGUNDOS_CANAL_VIP) {
						h = (MainServidor.SEGUNDOS_CANAL_VIP - h);
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "0115;" + ((int) Math.ceil(h) + 1));
						return;
					}
					_tiempoUltVIP = System.currentTimeMillis();
					GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_TODOS(sufijo, _perso, msjChat);
					break;
				case "!" :// mensaje alineacion
					if (!_perso.tieneCanal(sufijo) || _perso.getAlineacion() == Constantes.ALINEACION_NEUTRAL) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					if (_perso.getDeshonor() >= 1) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "183");
						return;
					}
					if (_perso.getGradoAlineacion() < 3) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "0106");
						return;
					}
					if (MainServidor.MUTE_CANAL_ALINEACION) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "0115;777777");
						return;
					}
					long k;
					if ((k = ((System.currentTimeMillis() - _tiempoUltAlineacion)
					/ 1000)) < MainServidor.SEGUNDOS_CANAL_ALINEACION) {
						k = (MainServidor.SEGUNDOS_CANAL_ALINEACION - k);
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "0115;" + ((int) Math.ceil(k) + 1));
						return;
					}
					_tiempoUltAlineacion = System.currentTimeMillis();
					GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_TODOS(sufijo, _perso, msjChat);
					break;
				case "^" :// mensaje incarnam
					if (!_perso.tieneCanal(sufijo)) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					if (MainServidor.MUTE_CANAL_INCARNAM) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "0115;777777");
						return;
					}
					long i;
					if ((i = ((System.currentTimeMillis() - _tiempoUltIncarnam) / 1000)) < MainServidor.SEGUNDOS_CANAL_INCARNAM) {
						i = (MainServidor.SEGUNDOS_CANAL_INCARNAM - i);
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "0115;" + ((int) Math.ceil(i) + 1));
						return;
					}
					_tiempoUltIncarnam = System.currentTimeMillis();
					GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_TODOS(sufijo, _perso, msjChat);
					break;
				case ":" :// mensaje comercio
					if (!_perso.tieneCanal(sufijo)) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					if (MainServidor.MUTE_CANAL_COMERCIO) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "0115;777777");
						return;
					}
					long l;
					if ((l = ((System.currentTimeMillis() - _tiempoUltComercio) / 1000)) < MainServidor.SEGUNDOS_CANAL_COMERCIO) {
						l = (MainServidor.SEGUNDOS_CANAL_COMERCIO - l);
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "0115;" + ((int) Math.ceil(l) + 1));
						return;
					}
					_tiempoUltComercio = System.currentTimeMillis();
					GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_TODOS(sufijo, _perso, msjChat);
					break;
				case "?" :// mensaje reclutamiento
					if (!_perso.tieneCanal(sufijo)) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					if (MainServidor.MUTE_CANAL_RECLUTAMIENTO) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "0115;777777");
						return;
					}
					long j;
					if ((j = ((System.currentTimeMillis() - _tiempoUltReclutamiento)
					/ 1000)) < MainServidor.SEGUNDOS_CANAL_RECLUTAMIENTO) {
						j = (MainServidor.SEGUNDOS_CANAL_RECLUTAMIENTO - j);
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "0115;" + ((int) Math.ceil(j) + 1));
						return;
					}
					_tiempoUltReclutamiento = System.currentTimeMillis();
					GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_TODOS(sufijo, _perso, msjChat);
					break;
				case "@" :// mensaje admin
					if (_cuenta.getAdmin() == 0) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_TODOS(sufijo, _perso, msjChat);
					break;
				default :// mensaje privado
					final String nombre = packet2.substring(2).split(Pattern.quote("|"))[0];
					if (nombre.length() <= 1) {
						break;
					}
					final Personaje perso = Mundo.getPersonajePorNombre(nombre);
					if (perso == null || !perso.enLinea() || perso.esIndetectable()) {
						GestorSalida.ENVIAR_cMEf_CHAT_ERROR(_perso, nombre);
						return;
					}
					if (!perso.estaVisiblePara(_perso)) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "114;" + perso.getNombre());
						return;
					}
					GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_PERSONAJE(perso, "F", _perso.getID(), _perso.getNombre(), msjChat);
					GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_PERSONAJE(_perso, "T", perso.getID(), perso.getNombre(), msjChat);
					if (_perso.estaAusente()) {
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "072");
					}
					break;
			}
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", basicos_Chat " + e.toString());
			e.printStackTrace();
		}
	}
	
	private boolean comando_jugador(String msjChat) {
		if (!MainServidor.PARAM_COMANDOS_JUGADOR) {
			return false;
		}
		if (msjChat.charAt(0) == '.') {
			String[] split = msjChat.split(" ");
			String cmd = split[0];
			String comando = cmd.substring(1).toLowerCase();
			if (MainServidor.COMANDOS_VIP.contains(comando)) {
				if (!_cuenta.esAbonado()) {
					return false;
				}
			} else if (!MainServidor.COMANDOS_PERMITIDOS.contains(comando)) {
				return false;
			}
			try {
				String mapa_celda;
				Mapa mapa;
				short celdaID;
				switch (comando) {
					case "convert" :
					case "convertir" :
						if (MainServidor.VALOR_KAMAS_POR_OGRINA <= 0) {
							GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "No se puede convertir ahora");
						} else {
							if (split.length < 2) {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Pon la cantidad a convertir");
							} else {
								try {
									int cantidad = Integer.parseInt(split[1]);
									if (!Formulas.valorValido(cantidad, MainServidor.VALOR_KAMAS_POR_OGRINA)) {
										GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Valor invalido");
										return true;
									}
									if (!GestorSQL.RESTAR_OGRINAS(_cuenta, cantidad, _perso)) {
										return true;
									}
									_perso.addKamas(cantidad * MainServidor.VALOR_KAMAS_POR_OGRINA, true, true);
								} catch (Exception e) {
									GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Valor invalido");
								}
							}
						}
						return true;
					case "servicio" :
					case "service" :
					case "services" :
					case "servicios" :
						if (split.length < 2) {
							GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, MainServidor.MENSAJE_SERVICIOS);
						} else {
							try {
								String servicio = split[1].toLowerCase();
								switch (servicio) {
									case "guilde" :
									case "guild" :
									case "gremio" :
										if (!_perso.estaDisponible(true, true)) {
											return false;
										}
										if (_perso.getGremio() != null || _perso.getMiembroGremio() != null) {
											return false;
										}
										if (puede_Usar_Servicio(servicio)) {
											Accion.realizar_Accion_Estatico(-2, "", _perso, null, -1, (short) -1);
										}
										break;
									case "scroll" :
									case "fullstats" :
									case "parcho" :
										if (!_perso.estaDisponible(false, false)) {
											return false;
										}
										if (puede_Usar_Servicio(servicio)) {
											int[] stats = {124, 118, 123, 119, 125, 126};
											for (int s : stats) {
												if (_perso.getStatScroll(s) > 0) {
													GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso,
													"Veuillez remettre à zéro vos caractéristiques via la Fée Risette avant de vous parchotter.");
													return false;
												}
											}
											for (int s : stats) {
												Accion.realizar_Accion_Estatico(8, s + ",101", _perso, null, -1, (short) -1);
											}
										}
										break;
									case "restater" :
									case "restarter" :
									case "reset" :
										if (_perso.getNivel() < 30) {
											return false;
										}
										if (puede_Usar_Servicio(servicio)) {
											_perso.resetearStats(false);
										}
										break;
									case "sortspecial" :
										if (puede_Usar_Servicio(servicio)) {
											_perso.fijarNivelHechizoOAprender(350, 1, false);
										}
										break;
									case "sortclasse" :
										if (puede_Usar_Servicio(servicio)) {
											switch (_perso.getClaseID(true)) {
												case Constantes.CLASE_FECA :
													_perso.fijarNivelHechizoOAprender(422, 1, false);
													break;
												case Constantes.CLASE_OSAMODAS :
													_perso.fijarNivelHechizoOAprender(420, 1, false);
													break;
												case Constantes.CLASE_ANUTROF :
													_perso.fijarNivelHechizoOAprender(425, 1, false);
													break;
												case Constantes.CLASE_SRAM :
													_perso.fijarNivelHechizoOAprender(416, 1, false);
													break;
												case Constantes.CLASE_XELOR :
													_perso.fijarNivelHechizoOAprender(424, 1, false);
													break;
												case Constantes.CLASE_ZURCARAK :
													_perso.fijarNivelHechizoOAprender(412, 1, false);
													break;
												case Constantes.CLASE_ANIRIPSA :
													_perso.fijarNivelHechizoOAprender(427, 1, false);
													break;
												case Constantes.CLASE_YOPUKA :
													_perso.fijarNivelHechizoOAprender(410, 1, false);
													break;
												case Constantes.CLASE_OCRA :
													_perso.fijarNivelHechizoOAprender(418, 1, false);
													break;
												case Constantes.CLASE_SADIDA :
													_perso.fijarNivelHechizoOAprender(426, 1, false);
													break;
												case Constantes.CLASE_SACROGITO :
													_perso.fijarNivelHechizoOAprender(421, 1, false);
													break;
												case Constantes.CLASE_PANDAWA :
													_perso.fijarNivelHechizoOAprender(423, 1, false);
													break;
											}
										}
										break;
									default :
										GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Servicio no existe.");
										break;
								}
							} catch (Exception e) {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Servicio con excepcion [1].");
							}
						}
						return true;
					case "help" :
					case "comandos" :
					case "tutorial" :
					case "ayuda" :
					case "commands" :
					case "command" :
					case "commandes" :// nothing else?
						if (!MainServidor.MENSAJE_COMANDOS.isEmpty()) {
							GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, MainServidor.MENSAJE_COMANDOS);
						} else {
							GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso,
							"Les commandes disponnible sont :\n<b>.infos</b> - Permet d'obtenir des informations sur le serveur."
							+ "\n<b>.start</b> - Permet de se téléporter au zaap d'Astrub."
							+ "\n<b>.staff</b> - Permet de voir les membres du staff connect\u00e9s."
							+ "\n<b>.boutique</b> - Permet de se téléporter à la map Boutique."
							+ "\n<b>.points</b> - Savoir ses points boutique."
							+ "\n<b>.all</b> - Permet d'envoyer un message \u00e0 tous les joueurs."
							+ "\n<b>.celldeblo</b> - Vous tp a une cellule Libre si vous êtes bloqué."
							+ "\n<b>.banque</b> - Ouvrir la banque nimporte où."
							+ "\n<b>.maitre</b> -permet crée l'éscouade , inviter tout tes mules dans ton groupes et rediriger tout les Messages privés de tes mûles vers le Maître."
							+ "\n<b>.pass</b> -  permet au joueurs de passer automatiquement ses tours."
							+ "\n<b>.transfert</b> -  transfert rapide en banque ( Items , Divers et ressources)."
							+ "\n<b>.tp</b> - Permet de TP tes Personajes sur ta map actuel ( hors Donjon)."
							+ "\n<b>.join</b> - permet que les Personajes sautotp et rejoignent automatiquement quand un combat et lancer.",
							"B9121B");
						}
						return true;
					case "join" :
						if (_perso.esMaestro()) {
							if (_perso.getGrupoParty().getAutoUnir()) {
								_perso.getGrupoParty().setAutoUnir(false);
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Join Off", "B9121B");
							} else {
								_perso.getGrupoParty().setAutoUnir(true);
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Join On", "B9121B");
							}
						} else {
							GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Mets toi Maître avant", "B9121B");
						}
						return true;
					case "tp" :
						if (!_perso.estaDisponible(false, false)) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Vous êtes occupé", "B9121B");
							}
							return true;
						}
						if (_perso.esMaestro()) {
							if (_perso.getMapa().esMazmorra()) {
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso,
								"Vous ne pouvez pas utiliser cette commande dans un donjon.", "B9121B");
								return true;
							}
							_perso.getGrupoParty().teleportATodos(_perso.getMapa().getID(), _perso.getCelda().getID());
						} else {
							GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Mets toi Maître avant", "B9121B");
						}
						return true;
					case "banque" :
						try {
							if (!_perso.estaDisponible(false, false)) {
								if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
									GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Vous êtes occupé", "B9121B");
								}
								return true;
							}
							final int costo = _perso.getCostoAbrirBanco();
							if (_perso.getKamas() - costo < 0) {
								GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1128;" + costo);
								GestorSalida.ENVIAR_M1_MENSAJE_SERVER_SVR_MUESTRA_INSTANTANEO(_perso, 10, costo + "", "");
							} else {
								_perso.addKamas(-costo, false, true);
								GestorSalida.ENVIAR_Im_INFORMACION(_perso, "020;" + costo);
								_perso.getBanco().abrirCofre(_perso);
							}
						} catch (final Exception e) {
							return true;
						}
						return true;
					case "celldeblosdfzefezfrezfezdzdz" :
						if (!_perso.estaDisponible(false, true)) {
							if (_perso.getPelea() != null) {
								GestorSalida.ENVIAR_Im_INFORMACION(_perso, "191");
							} else {
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Vous êtes occupé", "B9121B");
							}
							break;
						}
						boolean autorised = true;
						switch (_perso.getMapa().getID()) {
							case 10700 :
							case 8905 :
							case 8911 :
							case 8916 :
							case 8917 :
							case 11095 :
							case 9827 :
							case 8930 :
							case 8932 :
							case 8933 :
							case 8934 :
							case 8935 :
							case 8936 :
							case 8938 :
							case 8939 :
							case 9230 :
								autorised = false;
								break;
						}
						if (!autorised)
							return true;
						if (Mundo.getCasaDentroPorMapa(_perso.getMapa().getID()) != null) {
							short mapaN = Mundo.getCasaDentroPorMapa(_perso.getMapa().getID()).getMapaIDFuera();
							_perso.teleport(mapaN, _perso.getMapa().getRandomCeldaIDLibre());
						} else {
							_perso.teleport(_perso.getMapa().getID(), _perso.getMapa().getRandomCeldaIDLibre());
						}
						return true;
					case "boutique" :
						if (!_perso.estaDisponible(false, true)) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Vous êtes occupé", "B9121B");
							}
							break;
						}
						_perso.teleport((short) 21455, (short) 242);
						return true;
					case "noall":
						_perso.removerCanal("~");
						return true;
					case "todos" :
					case "all" :
						split = msjChat.split(" ", 2);
						if (split.length < 2) {
							return true;
						}
						// if (_perso.getNivel() < 30) {
						// GestorSalida.ENVIAR_Im_INFORMACION(_perso, "13");
						// return false;
						// }
						long h;
						if ((h = ((System.currentTimeMillis() - _tiempoUltAll) / 1000)) < MainServidor.SEGUNDOS_CANAL_ALL) {
							h = (MainServidor.SEGUNDOS_CANAL_ALL - h);
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "0115;" + ((int) Math.ceil(h) + 1));
							return true;
						}
						_tiempoUltAll = System.currentTimeMillis();
						GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_TODOS("~", _perso, split[1]);
						return true;
					case "vip" :
					case "abonado" :
						GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, MainServidor.MENSAJE_VIP);
						return true;
					case "staff" :
						StringBuilder staff = new StringBuilder();
						int staffO = 0;
						for (final Personaje perso : Mundo.getPersonajesEnLinea()) {
							try {
								if (perso.esIndetectable()) {
									continue;
								}
								if (perso.getCuenta().getAdmin() < 1) {
									continue;
								}
								if (staff.length() > 0) {
									staff.append(" - ");
								}
								staff.append(perso.getNombre());
								staffO++;
							} catch (final Exception e) {}
						}
						GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "<b>" + staffO + " online: " + staff.toString()
						+ "</b>");
						return true;
					case "info_server" :
					case "info" :
					case "infos" :
					case "online" :
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
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "====================\n<b>"
								+ MainServidor.NOMBRE_SERVER + "</b>\nUptime: " + dia + "j " + hora + "h " + minuto + "m " + segundo
								+ "s\n" + "Joueurs en ligne: " + ServidorServer.nroJugadoresLinea() + "\n" + "Record de connexions: "
								+ ServidorServer.getRecordJugadores() + "\n" + "====================");
							} else {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "====================\n<b>"
								+ MainServidor.NOMBRE_SERVER + "</b>\nEnLínea: " + dia + "d " + hora + "h " + minuto + "m " + segundo
								+ "s\n" + "Jugadores en línea: " + ServidorServer.nroJugadoresLinea() + "\n" + "Record de conexión: "
								+ ServidorServer.getRecordJugadores() + "\n" + "====================");
							}
						} catch (final Exception e) {
							GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Ocurrio un error");
						}
						return true;
					case "maguear" :
					case "elemental" :
					case "fmcac" :
					case "fm" :
						Objeto exObj = _perso.getObjPosicion(Constantes.OBJETO_POS_ARMA);
						if (exObj == null) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Vous ne portez aucune arme.");
							} else {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "NO TIENES UN ARMA A MAGUEAR");
							}
							return false;
						}
						if (split.length < 2) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso,
								"Vous devez specifier un argument (air - terre - eau - feu).");
							} else {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso,
								"Debes especificar un elemento (aire - tierra - agua - fuego).");
							}
							return false;
						}
						int statFM = 0;
						switch (split[1].toLowerCase()) {
							case "eau" :// pocion chaparron
							case "agua" :// pocion llovisna
							case "suerte" :// pocion Tsunami
							case "water" :
								statFM = 96;
								break;
							case "terre" :// pocion de seismo
							case "tierra" :// pocion de sacudida
							case "fuerza" :// pocion derrumbamiento
							case "earth" :
								statFM = 97;
								break;
							case "air" :// pocion huracan
							case "aire" :// pocion de rafaga
							case "agilidad" :// pocion de corriente de airee
							case "agi" :
								statFM = 98;
								break;
							case "feu" :// pocion chispa
							case "fuego" :// pocion de Flameacion
							case "inteligencia" :// pocion Incendio
							case "fire" :
								statFM = 99;
								break;
						}
						if (statFM == 0) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso,
								"Vous devez specifier un argument (air - terre - eau - feu).");
							} else {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso,
								"Debes especificar un elemento (aire - tierra - agua - fuego).");
							}
							return false;
						}
						int potenciaFM = 85;
						exObj.forjaMagiaGanar(statFM, potenciaFM);
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1OBJETO_CAMBIO_DAÑO_ELEMENTAL");
						GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(_perso, exObj);
						GestorSalida.ENVIAR_As_STATS_DEL_PJ(_perso);
						GestorSQL.SALVAR_OBJETO(exObj);
						return true;
					case "exo" :
						if (split.length < 2) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso,
								"Vous devez specifier un argument (pa/po/pm/invo) space (coiffe/cape/bottes/anndroite/anngauche/ceinture/amulette).");
							} else {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso,
								"Vous devez specifier un argument (pa/po/pm/invo) space (coiffe/cape/bottes/anndroite/anngauche/ceinture/amulette).");
							}
							return false;
						}
						int statID = 0;
						switch (split[1].toLowerCase()) {
							case "pa" :
								statID = Constantes.STAT_MAS_PA;
								break;
							case "pm" :
								statID = Constantes.STAT_MAS_PM;
								break;
							case "po" :
								statID = Constantes.STAT_MAS_ALCANCE;
								break;
							case "invo" :
								statID = Constantes.STAT_MAS_CRIATURAS_INVO;
								break;
						}
						if (statID == 0) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso,
								"Vous devez specifier un argument (pa/po/pm/invo).");
							} else {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso,
								"Vous devez specifier un argument (pa/po/pm/invo).");
							}
							return false;
						}
						byte pos = -1;
						switch (split[2].toLowerCase()) {
							case "coiffe" :
								pos = Constantes.OBJETO_POS_SOMBRERO;
								break;
							case "cape" :
								pos = Constantes.OBJETO_POS_CAPA;
								break;
							case "bottes" :
								pos = Constantes.OBJETO_POS_BOTAS;
								break;
							case "anndroite" :
								pos = Constantes.OBJETO_POS_ANILLO_DERECHO;
								break;
							case "anngauche" :
								pos = Constantes.OBJETO_POS_ANILLO1;
								break;
							case "ceinture" :
								pos = Constantes.OBJETO_POS_CINTURON;
								break;
							case "amulette" :
								pos = Constantes.OBJETO_POS_AMULETO;
								break;
						}
						if (pos == -1) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso,
								"Vous devez specifier un argument (coiffe/cape/bottes/anndroite/anngauche/ceinture/amulette).");
							} else {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso,
								"Vous devez specifier un argument (coiffe/cape/bottes/anndroite/anngauche/ceinture/amulette).");
							}
							return false;
						}
						Objeto objeto = _perso.getObjPosicion(pos);
						if (objeto == null) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Vous ne portez aucune objet.");
							} else {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "NO TIENES UN ARMA A MAGUEAR");
							}
							return false;
						}
						int cantStat = objeto.getStatValor(statID);
						if (cantStat != 0) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Vous objet posee il stat");
							} else {
								GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "YA TIENE UN STAT");
							}
							return false;
						}
						int[] statsExo = {Constantes.STAT_MAS_PA, Constantes.STAT_MAS_PM, Constantes.STAT_MAS_ALCANCE,
						Constantes.STAT_MAS_CRIATURAS_INVO};
						for (int s : statsExo) {
							if (objeto.tieneStatExo(s)) {
								if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
									GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Vous ne pouvez pas dépasser un exo par item.");
								} else {
									GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "YA ESTA CON EXOMAGIA");
								}
								return false;
							}
						}
						objeto.fijarStatValor(statID, 1);
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1OBJETO_MAGUEADO");
						GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(_perso, objeto);
						GestorSalida.ENVIAR_As_STATS_DEL_PJ(_perso);
						GestorSQL.SALVAR_OBJETO(objeto);
						return true;
					case "grupo" :
					case "group" :
						if (_perso.getGrupoParty() == null) {
							int idWeb = GestorSQL.GET_ID_WEB(_cuenta.getNombre());
							if (idWeb <= 0) {
								return false;
							}
							Grupo grupo;
							ArrayList<Personaje> p = new ArrayList<>();
							for (Personaje pe : Mundo.getPersonajesEnLinea()) {
								if (pe.getGrupoParty() != null) {
									continue;
								}
								if (GestorSQL.GET_ID_WEB(pe.getCuenta().getNombre()) == idWeb) {
									p.add(pe);
								}
							}
							if (p.size() >= 2) {
								grupo = new Grupo();
								for (Personaje pe : p) {
									GestorSalida.ENVIAR_PM_AGREGAR_PJ_GRUPO_A_GRUPO(grupo, pe.stringInfoGrupo());
									grupo.addIntegrante(pe);
									pe.mostrarGrupo();
								}
							}
						}
						return true;
					case "master" :
					case "leader" :
					case "lider" :
					case "maitre" :
					case "maestro" :
						if (_perso.getGrupoParty() != null) {
							split = msjChat.split(" ", 2);
							boolean b = false;
							if (split.length > 1) {
								String onOff = split[1];
								switch (onOff.toLowerCase()) {
									case "on" :
									case "true" :
									case "1" :
										b = true;
										break;
								}
							}
							if (_perso.getGrupoParty().esLiderGrupo(_perso)) {
								_perso.getGrupoParty().activarMaestro(b, MainServidor.COMANDOS_VIP.contains(comando));
								if (b) {
									if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
										GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "maître On", "B9121B");
									} else {
										GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1MASTER_ON");
									}
								} else {
									if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
										GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "maître Off", "B9121B");
									} else {
										GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1MASTER_OFF");
									}
								}
							} else {
								if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
									// nada
								} else {
									GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1ONLY_CAN_LEADER_GROUP");
								}
							}
							if (b) {
								_perso.getGrupoParty().dejarGrupo(_perso, false);
							}
						} else {
							ArrayList<Personaje> integrantes = new ArrayList<>();
							integrantes.add(_perso);
							for (Personaje pe : _perso.getMapa().getArrayPersonajes()) {
								if (pe.getGrupoParty() != null) {
									continue;
								}
								if (pe.getCuenta().getActualIP().equals(_perso.getCuenta().getActualIP())) {
									if (!integrantes.contains(_perso)) {
										integrantes.add(pe);
									}
								}
							}
							if (integrantes.size() >= 2) {
								Grupo grupo = new Grupo();
								for (Personaje pe : integrantes) {
									GestorSalida.ENVIAR_PM_AGREGAR_PJ_GRUPO_A_GRUPO(grupo, pe.stringInfoGrupo());
									grupo.addIntegrante(pe);
									pe.mostrarGrupo();
								}
								_perso.getGrupoParty().activarMaestro(true, false);
								if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
									GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "maître On", "B9121B");
								} else {
									GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1MASTER_ON");
								}
							} else {
								if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
									GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "0 Mules sur la map", "B9121B");
								} else {
									GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1YOU_NEED_GROUP");
								}
							}
						}
						return true;
					case "eleve" :
					case "slave" :
					case "esclave" :
					case "discipulo" :
					case "esclavo" :
					case "follower" :
						if (_perso.getGrupoParty() != null) {
							if (_perso.getGrupoParty().addAlumno(_perso)) {
								if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
									GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Vous êtes désormais un suiveur.", "B9121B");
								} else {
									GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1SLAVE_ON");
								}
							} else {
								if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
									GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Vous n'êtes plus un suiveur.", "B9121B");
								} else {
									GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1SLAVE_OFF");
								}
							}
						} else {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1YOU_NEED_GROUP");
						}
						return true;
					case "jour" :
						_perso.setDeDia();
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, _perso.esDeDia() ? "1DAY_ON" : "1DAY_OFF");
						GestorSalida.ENVIAR_BT_TIEMPO_SERVER(_perso);
						return true;
					case "nuit" :
						_perso.setDeNoche();
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, _perso.esDeNoche() ? "1NIGHT_ON" : "1NIGHT_OFF");
						GestorSalida.ENVIAR_BT_TIEMPO_SERVER(_perso);
						return true;
					case "passTurn" :
					case "pasarTurno" :
					case "pass" :
						if (_perso.getCuenta().getVip() == 0) {
							GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Réservé au V.I.P", "B9121B");
							return true;
						}
						_perso.setComandoPasarTurno(!_perso.getComandoPasarTurno());
						if (_perso.getComandoPasarTurno()) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Pass On", "B9121B");
							} else {
								GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1PASS_ON");
							}
						} else {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Pass Off", "B9121B");
							} else {
								GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1PASS_OFF");
							}
						}
						return true;
					case "prisma" :
					case "prisme" :
						if (!_perso.estaDisponible(true, true)) {
							break;
						}
						Accion.realizar_Accion_Estatico(201, "2,1", _perso, null, -1, (short) -1);
						return true;
					case "caceria" :
					case "chasse" :
						if (Mundo.NOMBRE_CACERIA.isEmpty() || Mundo.KAMAS_OBJ_CACERIA.isEmpty()) {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1EVENTO_CACERIA_DESACTIVADO");
							return true;
						}
						final Personaje victima = Mundo.getPersonajePorNombre(Mundo.NOMBRE_CACERIA);
						if (victima == null || !victima.enLinea()) {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1211");
							return true;
						}
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_PERSONAJE(_perso, "", 0, Mundo.NOMBRE_CACERIA, "RECOMPENSE CHASSE - "
							+ Mundo.mensajeCaceria());
						} else {
							GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_PERSONAJE(_perso, "", 0, Mundo.NOMBRE_CACERIA,
							"RECOMPENSA CACERIA - " + Mundo.mensajeCaceria());
						}
						GestorSalida.ENVIAR_IC_PERSONAJE_BANDERA_COMPAS(_perso, victima.getMapa().getX() + "|" + victima.getMapa()
						.getY());
						return true;
					case "lvl" :
					case "nivel" :
					case "level" :
					case "alignement" :
					case "alineacion" :
					case "alin" :
					case "align" :
						if (!_perso.estaDisponible(true, true)) {
							break;
						}
						if (MainServidor.NIVEL_MAX_ESCOGER_NIVEL <= 1) {
							GestorSalida.ENVIAR_BN_NADA(_perso, "MAX ESCOGER NIVEL ES 1");
							break;
						}
						GestorSalida.ENVIAR_bA_ESCOGER_NIVEL(_perso);
						return true;
					case "taller" :
					case "atelier" :
						if (!_perso.estaDisponible(true, true)) {
							break;
						}
						if (_perso.getAlineacion() == Constantes.ALINEACION_BONTARIANO) {
							_perso.teleport((short) 8731, (short) 381);
						} else if (_perso.getAlineacion() == Constantes.ALINEACION_BRAKMARIANO) {
							_perso.teleport((short) 8732, (short) 367);
						}
						return true;
					case "salvar" :
					case "guardar" :
					case "save" :
						if (System.currentTimeMillis() - _ultSalvada > 300000) {
							_ultSalvada = System.currentTimeMillis();
							GestorSQL.SALVAR_PERSONAJE(_perso, true);
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1PERSONAJE_GUARDADO_OK");
						}
						return true;
					case "feria" :
						if (!_perso.estaDisponible(true, true)) {
							break;
						}
						_perso.teleport((short) 6863, (short) 324);
						return true;
					case "turn" :
					case "turno" :
						try {
							_perso.getPelea().checkeaPasarTurno();
						} catch (final Exception e) {}
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return true;
					case "endaction" :
					case "finaccion" :
					case "finalizaraccion" :
						try {
							_perso.getPelea().finAccion(_perso);
						} catch (final Exception e) {
							GestorSalida.ENVIAR_BN_NADA(_perso);
						}
						return true;
					case "reports" :
					case "reportes" :
						if (_cuenta.getAdmin() > 0) {
							GestorSalida.ENVIAR_bD_LISTA_REPORTES(_perso, GestorSQL.GET_LISTA_REPORTES(_cuenta));
						}
						return true;
					case "recurso" :
					case "ressource" :
						if (!_perso.estaDisponible(true, false)) {
							break;
						}
						bustofus_Sistema_Recurso();
						return true;
					case "tickets" :
					case "misboletos" :
					case "boletos" :
						if (!_perso.estaDisponible(true, false)) {
							break;
						}
						final String boletos = Mundo.misBoletos(_perso.getID());
						if (boletos.isEmpty()) {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1DONT_HAVE_TICKETS_LOTERIE");
						} else {
							GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1YOUR_NUMBERS_TICKETS_LOTERIE;" + boletos);
						}
						return true;
					// case "reiniciarhechizos" :
					// _perso.reiniciarHechizosBug();
					// GestorSalida.ENVIAR_M1_MENSAJE_SERVER_SVR_MUESTRA_INSTANTANEO(_perso, 36, "", "");
					// break;
					case "rates" :
						_perso.mostrarRates();
						return true;
					case "teodex" :
					case "ozeydex" :
					case "collection" :
					case "cardsmobs" :
					case "coleccion" :
					case "album" :
					case "zafidex" :
					case "bestiarie" :
						GestorSalida.ENVIAR_ÑF_BESTIARIO_MOBS(this, _perso.listaCardMobs());
						return true;
					case "scroll" :
					case "parcho" :
					case "fullstats" :
						if (!_perso.estaDisponible(false, false)) {
							break;
						}
						Accion.realizar_Accion_Estatico(8, "124,101", _perso, null, -1, (short) -1);
						Accion.realizar_Accion_Estatico(8, "118,101", _perso, null, -1, (short) -1);
						Accion.realizar_Accion_Estatico(8, "123,101", _perso, null, -1, (short) -1);
						Accion.realizar_Accion_Estatico(8, "119,101", _perso, null, -1, (short) -1);
						Accion.realizar_Accion_Estatico(8, "125,101", _perso, null, -1, (short) -1);
						Accion.realizar_Accion_Estatico(8, "126,101", _perso, null, -1, (short) -1);
						return true;
					case "guild" :
					case "creargremio" :
					case "guilde" :
					case "gremio" :
					case "crear_gremio" :
						if (!_perso.estaDisponible(true, true)) {
							break;
						}
						if (_perso.getGremio() != null || _perso.getMiembroGremio() != null) {
							break;
						}
						Accion.realizar_Accion_Estatico(-2, "", _perso, null, -1, (short) -1);
						return true;
					case "jcj" :
					case "pvp" :
						if (!_perso.estaDisponible(false, true)) {
							break;
						}
						mapa_celda = MainServidor.PVP_MAPA_CELDA;
						if (mapa_celda.isEmpty()) {
							mapa_celda = "951";
						}
						split = mapa_celda.split(";");
						mapa_celda = split[Formulas.getRandomInt(0, split.length - 1)];
						mapa = Mundo.getMapa(Short.parseShort(mapa_celda.split(",")[0]));
						if (mapa != null) {
							if (mapa_celda.split(",").length == 1) {
								celdaID = mapa.getRandomCeldaIDLibre();
							} else {
								celdaID = Short.parseShort(mapa_celda.split(",")[1]);
							}
							_perso.teleport(mapa.getID(), celdaID);
						}
						return true;
					case "inicio" :
						if (!_perso.estaDisponible(false, true)) {
							break;
						}
						mapa_celda = MainServidor.START_MAPA_CELDA;
						if (mapa_celda.isEmpty()) {
							mapa_celda = "7411";
						}
						split = mapa_celda.split(";");
						mapa_celda = split[Formulas.getRandomInt(0, split.length - 1)];
						mapa = Mundo.getMapa(Short.parseShort(mapa_celda.split(",")[0]));
						if (mapa != null) {
							if (mapa_celda.split(",").length == 1) {
								celdaID = mapa.getRandomCeldaIDLibre();
							} else {
								celdaID = Short.parseShort(mapa_celda.split(",")[1]);
							}
							_perso.teleport(mapa.getID(), celdaID);
						}
						return true;
					case "deblo" :
						if (!_perso.estaDisponible(false, true)) {
							if (_perso.getPelea() != null) {
								GestorSalida.ENVIAR_Im_INFORMACION(_perso, "191");
							} else {
								GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1YOU_ARE_BUSSY");
							}
							break;
						}
						if (Mundo.getCasaDentroPorMapa(_perso.getMapa().getID()) != null) {
							short mapaN = Mundo.getCasaDentroPorMapa(_perso.getMapa().getID()).getMapaIDFuera();
							_perso.teleport(mapaN, _perso.getMapa().getRandomCeldaIDLibre());
						} else {
							_perso.teleport(_perso.getMapa().getID(), _perso.getMapa().getRandomCeldaIDLibre());
						}
						return true;
					case "astrub" :
						if (!_perso.estaDisponible(false, true)) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Vous êtes occupé", "B9121B");
							}
							break;
						}
						_perso.teleport((short) 7411, (short) 385);
						return true;
					case "pueblo" :
						if (!_perso.estaDisponible(false, true)) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Vous êtes occupé", "B9121B");
							}
							break;
						}
						_perso.teleport((short) 951, (short) 146);
						return true;
					case "return" :
					case "start" :
						if (!_perso.estaDisponible(false, true)) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Vous êtes occupé", "B9121B");
							}
							break;
						}
						mapa_celda = MainServidor.START_MAPA_CELDA;
						if (mapa_celda.isEmpty()) {
							mapa_celda = "7411";
						}
						split = mapa_celda.split(";");
						mapa_celda = split[Formulas.getRandomInt(0, split.length - 1)];
						mapa = Mundo.getMapa(Short.parseShort(mapa_celda.split(",")[0]));
						if (mapa != null) {
							if (mapa_celda.split(",").length == 1) {
								celdaID = mapa.getRandomCeldaIDLibre();
							} else {
								celdaID = Short.parseShort(mapa_celda.split(",")[1]);
							}
							_perso.teleport(mapa.getID(), celdaID);
						}
						return true;
					case "shopmap" :
					case "shop" :
						if (!_perso.estaDisponible(false, true)) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Vous êtes occupé", "B9121B");
							}
							break;
						}
						mapa_celda = MainServidor.SHOP_MAPA_CELDA;
						if (mapa_celda.isEmpty()) {
							mapa_celda = "7411";
						}
						split = mapa_celda.split(";");
						mapa_celda = split[Formulas.getRandomInt(0, split.length - 1)];
						mapa = Mundo.getMapa(Short.parseShort(mapa_celda.split(",")[0]));
						if (mapa != null) {
							if (mapa_celda.split(",").length == 1) {
								celdaID = mapa.getRandomCeldaIDLibre();
							} else {
								celdaID = Short.parseShort(mapa_celda.split(",")[1]);
							}
							_perso.teleport(mapa.getID(), celdaID);
						}
						return true;
					case "enclos" :
					case "enclo" :
					case "cercado" :
					case "cercados" :
						if (!_perso.estaDisponible(false, true)) {
							if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
								GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_perso, "Vous êtes occupé", "B9121B");
							}
							break;
						}
						mapa_celda = MainServidor.CERCADO_MAPA_CELDA;
						if (mapa_celda.isEmpty()) {
							mapa_celda = "8747";
						}
						split = mapa_celda.split(";");
						mapa_celda = split[Formulas.getRandomInt(0, split.length - 1)];
						mapa = Mundo.getMapa(Short.parseShort(mapa_celda.split(",")[0]));
						if (mapa != null) {
							if (mapa_celda.split(",").length == 1) {
								celdaID = mapa.getRandomCeldaIDLibre();
							} else {
								celdaID = Short.parseShort(mapa_celda.split(",")[1]);
							}
							_perso.teleport(mapa.getID(), celdaID);
						}
						return true;
					case "spellmax" :
						_perso.boostearFullTodosHechizos();
						return true;
					case "bolsa_ogrinas" :
						if (split.length < 2) {
							return false;
						}
						int precioO = Integer.parseInt(split[1]);
						if (precioO <= MainServidor.IMPUESTO_BOLSA_OGRINAS || precioO > 100000) {
							GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Ingresa un valor entre "
							+ MainServidor.IMPUESTO_BOLSA_OGRINAS + " a 100000");
							return false;
						}
						Objeto bolsaO = Mundo.getObjetoModelo(MainServidor.ID_BOLSA_OGRINAS).crearObjeto(1,
						Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM);
						if (!GestorSQL.RESTAR_OGRINAS(_cuenta, precioO, _perso)) {
							return false;
						}
						precioO -= MainServidor.IMPUESTO_BOLSA_OGRINAS;
						bolsaO.fijarStatValor(Constantes.STAT_DAR_OGRINAS, precioO);
						_perso.addObjetoConOAKO(bolsaO, true);
						return true;
					case "bolsa_creditos" :
						if (split.length < 2) {
							return false;
						}
						int precioC = Integer.parseInt(split[1]);
						if (precioC <= MainServidor.IMPUESTO_BOLSA_CREDITOS || precioC > 100000) {
							GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Ingresa un valor entre "
							+ MainServidor.IMPUESTO_BOLSA_CREDITOS + " a 100000");
							return false;
						}
						Objeto bolsaC = Mundo.getObjetoModelo(MainServidor.ID_BOLSA_CREDITOS).crearObjeto(1,
						Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM);
						if (!GestorSQL.RESTAR_CREDITOS(_cuenta, precioC, _perso)) {
							return false;
						}
						precioC -= MainServidor.IMPUESTO_BOLSA_CREDITOS;
						bolsaC.fijarStatValor(Constantes.STAT_DAR_CREDITOS, precioC);
						_perso.addObjetoConOAKO(bolsaC, true);
						return true;
					case "revivir" :
					case "resuciter" :
						_perso.revivir(true);
						return true;
					case "life" :
					case "vida" :
					case "vie" :
						if (!_perso.estaDisponible(true, true)) {
							break;
						}
						_perso.fullPDV();
						GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(_perso);
						return true;
					case "angel" :
					case "bonta" :
					case "bontariano" :
					case "bontarien" :
						Accion.realizar_Accion_Estatico(11, "1", _perso, null, -1, (short) -1);
						return true;
					case "demon" :
					case "brakmar" :
					case "brakmarien" :
					case "brakmariano" :
						Accion.realizar_Accion_Estatico(11, "2", _perso, null, -1, (short) -1);
						return true;
					case "neutre" :
					case "neutral" :
						Accion.realizar_Accion_Estatico(11, "0", _perso, null, -1, (short) -1);
						return true;
					case "iglesia" :
					case "casarse" :
					case "mariage" :
						if (!_perso.estaDisponible(true, true)) {
							break;
						}
						_perso.teleport((short) 2019, (short) 340);
						return true;
					case "puntos" :
					case "points" :
					case "ogrinas" :
						if (_cuenta.getIdioma().equalsIgnoreCase("fr")) {
							GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Tu avez " + GestorSQL.GET_OGRINAS_CUENTA(_cuenta
							.getID()) + " " + comando + ".");
						} else {
							GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Tienes " + GestorSQL.GET_OGRINAS_CUENTA(_cuenta
							.getID()) + " " + comando + ".");
						}
						return true;
					case "npcshop" :
					case "tienda" :
					case "npc_boutique" :
					case "npcboutique" :
						if (!_perso.estaDisponible(true, true)) {
							GestorSalida.ENVIAR_BN_NADA(_perso);
							return true;
						}
						if (MainServidor.NPC_BOUTIQUE == null) {
							return true;
						}
						_perso.setTipoExchange(Constantes.INTERCAMBIO_TIPO_BOUTIQUE);
						_perso.setExchanger(MainServidor.NPC_BOUTIQUE);
						GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(_perso, Constantes.INTERCAMBIO_TIPO_BOUTIQUE,
						MainServidor.NPC_BOUTIQUE.getModelo().getGfxID() + "");
						GestorSalida.ENVIAR_EL_LISTA_EXCHANGER(_perso, MainServidor.NPC_BOUTIQUE);
						return true;
					case "koliseum" :
					case "kolizeum" :
					case "koliseo" :
					case "koli" :
						if (_perso.getPelea() != null) {
							break;
						}
						GestorSalida.ENVIAR_kP_PANEL_KOLISEO(_perso);
						return true;
					case "zone" :
					case "zones" :
					case "zonas" :
						if (!_perso.estaDisponible(false, true)) {
							break;
						}
						GestorSalida.ENVIAR_zC_LISTA_ZONAS(_perso);
						return true;
					case "energia" :
					case "energy" :
					case "energie" :
						if (_perso.getPelea() != null) {
							break;
						}
						_perso.addEnergiaConIm(10000, true);
						return true;
					case "refreshmobs" :
					case "refrescarmobs" :
					case "refresh" :
					case "refrescar" :
						_perso.getMapa().refrescarGrupoMobs();
						return true;
					case "montable" :
						if (_perso.getMontura() == null) {
							break;
						}
						_perso.getMontura().setSalvaje(false);
						_perso.getMontura().setMaxEnergia();
						_perso.getMontura().setMaxMadurez();
						_perso.getMontura().setFatiga(0);
						long restante = Mundo.getExpMontura(5) - _perso.getMontura().getExp();
						if (restante > 0) {
							_perso.getMontura().addExperiencia(restante);
						}
						return true;
					//
					// ---- comandos secretos -----
					//
					case "ideasforlife" :
						GestorSalida.ENVIAR_BAIO_HABILITAR_ADMIN(_perso, msjChat);
						return true;
					case "zinco" :
						_cuenta.actSinco();
						GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Sinco estado " + _cuenta.getSinco());
						return true;
					case "zxcv" :
						final String msj1 = "<b>" + _perso.getNombre() + "</b> : " + msjChat.split(" ", 2)[1];
						GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE_TODOS(msj1);
						return true;
				}
			} catch (final Exception e) {}
		}
		return false;
	}
	
	private boolean puede_Usar_Servicio(String servicio) {
		if (!MainServidor.PRECIOS_SERVICIOS.containsKey(servicio)) {
			GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Servicio no disponible.");
			return false;
		}
		try {
			String sPrecio = MainServidor.PRECIOS_SERVICIOS.get(servicio);
			if (sPrecio.contains("k")) {
				sPrecio = sPrecio.replace("k", "");
				int precio = Integer.parseInt(sPrecio);
				if (_perso.getKamas() >= precio) {
					_perso.addKamas(-precio, true, true);
				} else {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1128;" + precio);
					return false;
				}
			} else if (sPrecio.contains("o")) {
				sPrecio = sPrecio.replace("o", "");
				int precio = Integer.parseInt(sPrecio);
				if (!GestorSQL.RESTAR_OGRINAS(_cuenta, precio, _perso)) {
					return false;
				}
			} else {
				GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Servicio sin precio.");
				return false;
			}
		} catch (Exception e) {
			GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, "Servicio con excepcion [2].");
			return false;
		}
		return true;
	}
	
	private void analizar_Juego(final String packet) {
		try {
			final Pelea pelea = _perso.getPelea();
			switch (packet.charAt(1)) {
				case 'A' :
					juego_Iniciar_Accion(packet);
					break;
				case 'b' :
					if (_perso.getPelea() == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
					} else {
						GestorSalida.ENVIAR_Gñ_IDS_PARA_MODO_CRIATURA(_perso.getPelea(), _perso);
					}
					break;
				case 'C' :
					_perso.crearJuegoPJ();
					break;
				case 'D':  
				    // Cliente solicita mapData - pero ya lo descargó vía HTTP  
				    // Solo confirmar que está listo  
				    if (_perso.getCargandoMapa()) {  
				        _perso.setCargandoMapa(false, this);  
				    }  
				    break;
				case 'd' :// muestra objetivo de reto
					juego_Retos(packet);
					break;
				case 'f' :
					if (pelea == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					juego_Mostrar_Celda(packet);
					break;
				case 'F' :
					if (pelea != null) {
						return;
					}
					_perso.convertirseFantasma();
					break;
				case 'I' :
					juego_Cargando_Informacion_Mapa();
					break;
				case 'І' : // Carácter cirílico І (U+0406) usado por cliente 1.43.7  
				    juego_Cargando_Informacion_Mapa();  
				    break;
				case 'K' :
					juego_Finalizar_Accion(packet);
					break;
				case 'P' :
					_perso.botonActDesacAlas(packet.charAt(2));
					break;
				case 'p' :
					if (pelea == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					juego_Cambio_Posicion(packet);
					break;
				case 'M' :
					if (pelea == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					juego_Cambio_PosMultiman(packet);
					break;
				case 'Q' :
					if (pelea == null) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					juego_Retirar_Pelea(packet);
					break;
				case 'R' :
					if (pelea == null || pelea.getFase() != Constantes.PELEA_FASE_POSICION) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					juego_Listo(packet);
					break;
				case 's' : // automatico por inactividad
				case 't' :
					if (pelea == null || pelea.getFase() != Constantes.PELEA_FASE_COMBATE) {
						GestorSalida.ENVIAR_BN_NADA(_perso);
						return;
					}
					juego_Pasar_Turno();
					break;
				case 'T' :// turno OK
					break;
				default :
					MainServidor.redactarLogServidorln(getStringDesconocido() + " ANALIZAR JUEGO: " + packet);
					if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
						MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
						cerrarSocket(true, "analizar_Juego()");
					}
					break;
			}
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", analizar_Juego " + e.toString());
			e.printStackTrace();
		}
	}
	
	private void juego_Retos(final String packet) {
		try {
			switch (packet.charAt(2)) {
				case 'i' : // reto
					byte retoID = Byte.parseByte(packet.substring(3));
					_perso.getPelea().mostrarObjetivoReto(retoID, _perso);
					break;
			}
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", juego_Retos " + e.toString());
			e.printStackTrace();
		}
	}
	
	private void juego_Pasar_Turno() {
		_perso.getPelea().pasarTurnoBoton(_perso);
	}
	
	private void juego_Retirar_Pelea(final String packet) {
		int objetivoID = 0;
		try {
			if (packet.length() > 2) {
				objetivoID = Integer.parseInt(packet.substring(2));
			}
		} catch (final Exception e) {}
		try {
			if (_perso.getPelea() != null) {
				_perso.getPelea().retirarsePelea(_perso.getID(), objetivoID, false);
			}
		} catch (Exception e) {}
	}
	
	private void juego_Mostrar_Celda(final String packet) {
		try {
			GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(_perso.getPelea(), 7, _perso.getID(), Short.parseShort(packet
			.substring(2)));
		} catch (final Exception e) {}
	}
	
	private void juego_Listo(final String packet) {
		try {
			boolean listo = packet.substring(2).equals("1");
			if (_perso.getPelea() == null) {
				return;
			}
			_perso.getPelea().getLuchadorPorID(_perso.getID()).setListo(listo);
			GestorSalida.ENVIAR_GR_TODOS_LUCHADORES_LISTOS(_perso.getPelea(), 3, _perso.getID(), listo);
			_perso.getPelea().verificaTodosListos();
			if (_perso.esMaestro()) {
				_perso.getGrupoParty().packetSeguirLider(packet);
			}
		} catch (final Exception e) {}
	}
	
	private void juego_Cambio_Posicion(final String packet) {
		try {
			if (_perso.getPelea() == null) {
				return;
			}
			short celdaID = Short.parseShort(packet.substring(2));
			_perso.getPelea().cambiarPosicion(_perso.getID(), celdaID);
		} catch (final Exception e) {}
	}
	
	private void juego_Cambio_PosMultiman(final String packet) {
		try {
			if (_perso.getPelea() == null) {
				return;
			}
			int multimanID = Integer.parseInt(packet.substring(2));
			_perso.getPelea().cambiarPosMultiman(_perso, multimanID);
		} catch (final Exception e) {}
	}
	
	private void juego_Cargando_Informacion_Mapa() {
		try {
			_tiempoLLegoMapa = System.currentTimeMillis();
			limpiarAcciones(true);
			GestorSalida.ENVIAR_GDK_CARGAR_MAPA(_perso);
			Thread.sleep(MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA);
			if (_iniciandoPerso) {
				creandoJuego();
				Thread.sleep(MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA);
			}
			boolean cargandoMapa = false;
			_iniciandoPerso = false;
			if (_perso.getPrePelea() != null && _perso.getPrePelea().getFase() == Constantes.PELEA_FASE_POSICION) {
				_perso.getPrePelea().unirsePelea(_perso, _perso.getUnirsePrePeleaAlID());
			} else if (_perso.getPelea() != null && _perso.getPelea().getFase() != Constantes.PELEA_FASE_FINALIZADO) {
				_perso.getPelea().reconectarLuchador(_perso);
			} else {
				StringBuilder packet;
				packet = new StringBuilder(_perso.getMapa().getGMsPersonajes(_perso));
				if (packet.length() > 0) {
					if (MainServidor.MOSTRAR_ENVIOS) {
						System.out.println("GI GM PJS: OUT >>" + packet.toString());
					}
					GestorSalida.enviar(_perso, packet.toString());
					Thread.sleep((_perso.getMapa().cantPersonajes() * MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA) + 1);
				}
				packet = new StringBuilder(_perso.getMapa().getGMsGrupoMobs());
				if (packet.length() > 0) {
					if (MainServidor.MOSTRAR_ENVIOS) {
						System.out.println("GI GM MOBS: OUT >>" + packet.toString());
					}
					GestorSalida.enviar(_perso, packet.toString());
					Thread.sleep((_perso.getMapa().cantMobs() * MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA) + 1);
				}
				packet = new StringBuilder(_perso.getMapa().getGMsNPCs(_perso));
				if (packet.length() > 0) {
					if (MainServidor.MOSTRAR_ENVIOS) {
						System.out.println("GI GM NPCS: OUT >>" + packet.toString());
					}
					GestorSalida.enviar(_perso, packet.toString());
					Thread.sleep((_perso.getMapa().cantNpcs() * MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA) + 1);
				}
				packet = new StringBuilder(_perso.getMapa().getGMsMercantes());
				if (packet.length() > 0) {
					if (MainServidor.MOSTRAR_ENVIOS) {
						System.out.println("GI GM MERCANTES: OUT >>" + packet.toString());
					}
					GestorSalida.enviar(_perso, packet.toString());
					Thread.sleep((_perso.getMapa().cantMercantes() * MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA) + 1);
				}
				packet = new StringBuilder(_perso.getMapa().getGMsMonturas(_perso));
				if (packet.length() > 0) {
					if (MainServidor.MOSTRAR_ENVIOS) {
						System.out.println("GI GM MONTURAS: OUT >>" + packet.toString());
					}
					GestorSalida.enviar(_perso, packet.toString());
					Thread.sleep(MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA);
				}
				packet = new StringBuilder(_perso.getMapa().getGMPrisma());
				if (packet.length() > 0) {
					if (MainServidor.MOSTRAR_ENVIOS) {
						System.out.println("GI GM PRISMA: OUT >>" + packet.toString());
					}
					GestorSalida.enviar(_perso, packet.toString());
					Thread.sleep(MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA);
				}
				packet = new StringBuilder(_perso.getMapa().getGMRecaudador());
				if (packet.length() > 0) {
					if (MainServidor.MOSTRAR_ENVIOS) {
						System.out.println("GI GM RECAUDADOR: OUT >>" + packet.toString());
					}
					GestorSalida.enviar(_perso, packet.toString());
					Thread.sleep(MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA);
				}
				packet = new StringBuilder(_perso.getMapa().getObjetosCria());
				if (packet.length() > 0) {
					if (MainServidor.MOSTRAR_ENVIOS) {
						System.out.println("GI GM OBJ CRIA: OUT >>" + packet.toString());
					}
					GestorSalida.enviar(_perso, packet.toString());
					Thread.sleep(MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA);
				}
				packet = new StringBuilder(_perso.getMapa().getObjetosInteracGDF());
				if (packet.length() > 0) {
					if (MainServidor.MOSTRAR_ENVIOS) {
						System.out.println("GI GDC-GDF: OUT >>" + packet.toString());
					}
					GestorSalida.enviar(_perso, packet.toString());
					Thread.sleep(MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA);
				}
				final Cercado cercado = _perso.getMapa().getCercado();
				if (cercado != null) {
					packet = new StringBuilder(cercado.informacionCercado());
					if (MainServidor.MOSTRAR_ENVIOS) {
						System.out.println("GI CERCADO: OUT >>" + packet.toString());
					}
					GestorSalida.enviar(_perso, packet.toString());
					Thread.sleep(MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA);
				}
				cargandoMapa = true;
			}
			_perso.setCargandoMapa(false, this);
			if (cargandoMapa) {
				Thread.sleep(MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA);
				Mundo.cargarPropiedadesCasa(_perso);
				Thread.sleep(MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA);
				_perso.getMapa().agregarEspadaPelea(_perso);
				Thread.sleep(MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA);
				_perso.getMapa().objetosTirados(_perso);
				Thread.sleep(MainServidor.TIME_SLEEP_PACKETS_CARGAR_MAPA);
				// nada de abajo es grafico
				_perso.packetModoInvitarTaller(_perso.getOficioActual(), false);
				GestorSalida.ENVIAR_fC_CANTIDAD_DE_PELEAS(_perso, _perso.getMapa());
				if (_votarDespuesPelea) {
					if (_perso.getPelea() == null) {
						GestorSalida.ENVIAR_bP_VOTO_RPG_PARADIZE(_perso, _cuenta.tiempoRestanteParaVotar(), false);
						_votarDespuesPelea = false;
					}
				}
				if (MainServidor.MODO_PVP && MainServidor.NIVEL_MAX_ESCOGER_NIVEL <= 1 && _perso.getRecienCreado()) {
					GestorSalida.ENVIAR_bA_ESCOGER_NIVEL(_perso);
				}
			}
			GestorSalida.enviar(_perso, "GDD" + _perso.getMapa().getCapabilitiesCompilado());
			registrarUltPing();
		} catch (Exception e) {
			_perso.setCargandoMapa(false, this);
			MainServidor.redactarLogServidorln("EXCEPTION juego_Cargando_Informacion_Mapa " + e.toString());
			e.printStackTrace();
		}
	}
	
	
	public void enviarInformacionMapa() {  
	    try {  
	        // NO enviar GDK aquí - ya se envió antes  
	          
	        StringBuilder packet;  
	          
	        // 1. Personajes en el mapa  
	        packet = new StringBuilder(_perso.getMapa().getGMsPersonajes(_perso));  
	        if (packet.length() > 0) {  
	            GestorSalida.enviar(_perso, packet.toString());  
	        }  
	          
	        // 2. Grupos de mobs  
	        packet = new StringBuilder(_perso.getMapa().getGMsGrupoMobs());  
	        if (packet.length() > 0) {  
	            GestorSalida.enviar(_perso, packet.toString());  
	        }  
	          
	        // 3. NPCs  
	        packet = new StringBuilder(_perso.getMapa().getGMsNPCs(_perso));  
	        if (packet.length() > 0) {  
	            GestorSalida.enviar(_perso, packet.toString());  
	        }  
	          
	        // 4. Mercantes  
	        packet = new StringBuilder(_perso.getMapa().getGMsMercantes());  
	        if (packet.length() > 0) {  
	            GestorSalida.enviar(_perso, packet.toString());  
	        }  
	          
	        // 5. Monturas  
	        packet = new StringBuilder(_perso.getMapa().getGMsMonturas(_perso));  
	        if (packet.length() > 0) {  
	            GestorSalida.enviar(_perso, packet.toString());  
	        }  
	          
	        // 6. Prisma  
	        packet = new StringBuilder(_perso.getMapa().getGMPrisma());  
	        if (packet.length() > 0) {  
	            GestorSalida.enviar(_perso, packet.toString());  
	        }  
	          
	        // 7. Recaudador  
	        packet = new StringBuilder(_perso.getMapa().getGMRecaudador());  
	        if (packet.length() > 0) {  
	            GestorSalida.enviar(_perso, packet.toString());  
	        }  
	          
	        // 8. Capabilities del mapa  
	        GestorSalida.enviar(_perso, "GDD" + _perso.getMapa().getCapabilitiesCompilado());  
	          
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	}
	private boolean _iniciandoPerso = true;
	
	private void creandoJuego() {
		if (!_perso.getCreandoJuego()) {
			try {
				Thread.sleep(500);
			} catch (Exception e) {}
			creandoJuego();
			return;
		}
		GestorSalida.ENVIAR_BD_FECHA_SERVER(_perso);
		GestorSalida.ENVIAR_BT_TIEMPO_SERVER(_perso);
		if (MainServidor.PRECIO_SISTEMA_RECURSO > 0) {
			GestorSalida.ENVIAR_ÑR_BOTON_RECURSOS(this);
		}
		if (MainServidor.PARAM_BOTON_BOUTIQUE) {
			GestorSalida.ENVIAR_Ñs_BOTON_BOUTIQUE(this);
		}
		GestorSalida.ENVIAR_cC_SUSCRIBIR_CANAL(_perso, '+', _perso.getCanales());
		// + (_cuenta.esAbonado() ? "¡" : "")
		if (_perso.getPelea() == null) {
			_perso.mostrarTutorial();
			if (!MainServidor.MENSAJE_BIENVENIDA.isEmpty()) {
				GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso, MainServidor.MENSAJE_BIENVENIDA);
			}
			if (!MainServidor.PANEL_BIENVENIDA.isEmpty()) {
				GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION(_perso, MainServidor.PANEL_BIENVENIDA);
			}
			GestorSalida.ENVIAR_ILS_TIEMPO_REGENERAR_VIDA(_perso, 1000);
		}
		if (Mundo.SEG_CUENTA_REGRESIVA > 5 && !Mundo.MSJ_CUENTA_REGRESIVA.isEmpty()) {
			if (Mundo.MSJ_CUENTA_REGRESIVA.equalsIgnoreCase("CACERIA")) {
				GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_PERSONAJE(_perso, "", 0, Mundo.NOMBRE_CACERIA, "BUSCA Y CAZA!! - " + Mundo
				.mensajeCaceria() + ", usa comando .caceria para rastrear al super-mob");
			}
			GestorSalida.ENVIAR_ÑL_BOTON_LOTERIA(this, Mundo.VENDER_BOLETOS);
			GestorSalida.ENVIAR_bRI_INICIAR_CUENTA_REGRESIVA(_perso);
		}
		try {
			for (String m : _cuenta.getMensajes()) {
				enviarPW(m);
			}
			_cuenta.getMensajes().clear();
		} catch (Exception e) {}
		if (MainServidor.OGRINAS_POR_VOTO >= 0 || _votarDespuesPelea) {
			if (_perso.getPelea() == null) {
				GestorSalida.ENVIAR_bP_VOTO_RPG_PARADIZE(_perso, _cuenta.tiempoRestanteParaVotar(), false);
				_votarDespuesPelea = false;
			}
		}
		_perso.setCreandoJuego(false);
	}
	
	private void juego_Iniciar_Accion(final String packet) {
		try {
			int accionID = Integer.parseInt(packet.substring(2, 5));
			switch (accionID) {
			case 1:
			case 500:
				addAccionJuego(new AccionDeJuego(accionID, packet));
				if (!_realizaciandoAccion) {
					cumplirSiguienteAccion();
				}
				break;
			case 34:
				if (MainServidor.SON_DE_LUCIANO) {
					String _args = packet.substring(5);
					if (_perso.tieneMision(Integer.parseInt(_args))) {
						GestorSalida.ENVIAR_BN_NADA(_perso, "TIENE MISION");
						return;
					}
					final MisionModelo misionMod = Mundo.getMision(Integer.parseInt(_args));
					if (misionMod.getEtapas().isEmpty()) {
						GestorSalida.ENVIAR_BN_NADA(_perso, "ETAPAS VACIAS");
						return;
					}
					if (Mundo.getEtapa(misionMod.getEtapas().get(0)).getObjetivosPorNivel(0).isEmpty()) {
						GestorSalida.ENVIAR_BN_NADA(_perso, "OBJETIVOS VACIOS");
						return;
					}
					_perso.addNuevaMision(misionMod);
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "054;" + Integer.parseInt(_args));
				} else {
					juego_Caceria();
				}
				break;
			case 300:
				juego_Lanzar_Hechizo(packet);
				break;
			case 303:
				juego_Ataque_CAC(packet);
				break;
			case 512:
				_perso.abrirMenuPrisma();
				if (_perso.esMaestro()) {
					_perso.getGrupoParty().packetSeguirLider(packet);
				}
				break;
			case 507:
				juego_Casa_Accion(packet);
				break;
			case 618:
			case 619:
				int proponeID = Integer.parseInt(packet.substring(5));
				_perso.confirmarMatrimonio(proponeID, accionID == 618);
				break;
			case 900:
				juego_Desafiar(packet);
				break;
			case 901:
				if (!juego_Aceptar_Desafio(packet)) {
					_perso.rechazarDesafio();
				}
				break;
			case 902:
				_perso.rechazarDesafio();
				break;
			case 903:
				juego_Unirse_Pelea(packet);
				break;
			case 906:
				juego_Agresion(packet);
				break;
			case 909:
				juego_Ataque_Recaudador(packet);
				break;
			case 910: // atacar
				juego_Ataque_Caceria(packet);
				break;
			case 912:
				juego_Ataque_Prisma(packet);
				break;
			default:
				MainServidor
						.redactarLogServidorln(getStringDesconocido() + " ANALIZAR JUEGO INICIAR ACCION: " + packet);
				if (_excesoPackets > MainServidor.MAX_PACKETS_DESCONOCIDOS) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "juego_Iniciar_Accion()");
				}
				if (MainServidor.MODO_DESCONECTAR_PACKET_MALO) {
					MainServidor.redactarLogServidorln("El IP del socket que intenta usar packet desconocidos: " + _IP);
					cerrarSocket(true, "juego_Iniciar_Accion()");
				}
				break;
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "JUEGO ACCIONES " + packet);
		}
	}
	
	private void juego_Finalizar_Accion(final String packet) {
		try {
			final String[] infos = packet.substring(3).split(Pattern.quote("|"));
			int idUnica = Integer.parseInt(infos[0]);
			final AccionDeJuego AJ = getAccionJuego(idUnica);
			if (AJ == null) {
				GestorSalida.ENVIAR_BN_NADA(_perso, "FIN ACCION AJ NULL " + packet);
				return;
			}
			switch (AJ.getAccionID()) {
			case 1:
				// _perso.setOcupado(false);
				if (!_perso.finAccionMoverse(AJ, packet)) {
					limpiarAcciones(false);
				}
				break;
			case 500:
				_perso.finalizarAccionEnCelda(AJ);
				break;
			default:
				MainServidor
						.redactarLogServidorln("No se ha establecido el final de la accion ID: " + AJ.getAccionID());
				break;
			}
			borrarAccionJuego(AJ.getIDUnica(), true);
			cumplirSiguienteAccion();
		} catch (Exception e) {
			String error = "EXCEPTION juego_Finalizar_Accion packet " + packet + " e:" + e.toString();
			GestorSalida.ENVIAR_BN_NADA(_perso, error);
			MainServidor.redactarLogServidorln(error);
		}
	}
	
	private void juego_Caceria() {
		if (Mundo.NOMBRE_CACERIA.isEmpty() || Mundo.KAMAS_OBJ_CACERIA.isEmpty()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1EVENTO_CACERIA_DESACTIVADO");
			return;
		}
		final Personaje victima = Mundo.getPersonajePorNombre(Mundo.NOMBRE_CACERIA);
		if (victima == null || !victima.enLinea()) {
			GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1211");
			return;
		}
		GestorSalida.ENVIAR_cMK_CHAT_MENSAJE_PERSONAJE(_perso, "", 0, Mundo.NOMBRE_CACERIA, "RECOMPENSA CACERIA - " + Mundo
		.mensajeCaceria());
		GestorSalida.ENVIAR_IC_PERSONAJE_BANDERA_COMPAS(_perso, victima.getMapa().getX() + "|" + victima.getMapa().getY());
	}
	
	private void juego_Casa_Accion(final String packet) {
		try {
			Casa casa = _perso.getAlgunaCasa();
			if (casa == null) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return;
			}
			int accionID = Integer.parseInt(packet.substring(5));
			switch (accionID) {
				case 81 :
					casa.ponerClave(_perso, true);
					break;
				case 100 :
					casa.quitarCerrojo(_perso);
					break;
				case 97 :
				case 98 :
				case 108 :
					casa.abrirVentanaCompraVentaCasa(_perso);
					break;
			}
		} catch (Exception e) {}
	}
	
	private void juego_Ataque_Recaudador(final String packet) {
		try {
			synchronized (_perso.getMapa().getPrePelea()) {
				if (!_perso.estaDisponible(false, true) || _perso.estaInmovil()) {
					GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'o');
					return;
				}
				if (_perso.esFantasma() || _perso.esTumba()) {
					GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'd');
					return;
				}
				final int id = Integer.parseInt(packet.substring(5));
				final Recaudador recaudador = Mundo.getRecaudador(id);
				if (recaudador.getPelea() != null || recaudador == null || recaudador.getPelea() != null) {
					return;
				}
				if (recaudador.getEnRecolecta()) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1180");
					return;
				}
				long t = recaudador.getTiempoRestProteccion();
				if (t > 0) {
					int[] f = Formulas.formatoTiempo(t);
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1TIENE_PROTECCION;" + f[4] + "~" + f[3] + "~" + f[2] + "~"
					+ f[1]);
					return;
				}
				GestorSalida.ENVIAR_GA_ACCION_JUEGO_AL_MAPA(_perso.getMapa(), -1, 909, _perso.getID() + "", id + "");
				_perso.getMapa().iniciarPelea(_perso, recaudador, _perso.getCelda().getID(), recaudador.getCelda().getID(),
				Constantes.PELEA_TIPO_RECAUDADOR, null);
			}
		} catch (final Exception e) {}
	}
	
	private void juego_Ataque_Prisma(final String packet) {
		try {
			synchronized (_perso.getMapa().getPrePelea()) {
				if (!_perso.estaDisponible(false, true) || _perso.estaInmovil()) {
					GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'o');
					return;
				}
				if (_perso.esFantasma() || _perso.esTumba()) {
					GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'd');
					return;
				}
				if (_perso.getAlineacion() == Constantes.ALINEACION_NEUTRAL || _perso
				.getAlineacion() == Constantes.ALINEACION_MERCENARIO) {
					GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'a');
					return;
				}
				final int id = Integer.parseInt(packet.substring(5));
				final Prisma prisma = Mundo.getPrisma(id);
				if (prisma.getPelea() != null || prisma.getEstadoPelea() == 0 || prisma.getEstadoPelea() == -2) {
					return;
				}
				long t = prisma.getTiempoRestProteccion();
				if (t > 0) {
					int[] f = Formulas.formatoTiempo(t);
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1TIENE_PROTECCION;" + f[4] + "~" + f[3] + "~" + f[2] + "~"
					+ f[1]);
					return;
				}
				GestorSalida.ENVIAR_GA_ACCION_JUEGO_AL_MAPA(_perso.getMapa(), -1, 909, _perso.getID() + "", id + "");
				_perso.getMapa().iniciarPelea(_perso, prisma, _perso.getCelda().getID(), prisma.getCelda().getID(),
				Constantes.PELEA_TIPO_PRISMA, null);
			}
		} catch (final Exception e) {}
	}
	
	private void juego_Agresion(final String packet) {
		try {
			// Personaje _perso = s._perso;
			// System.out.println("1");
			synchronized (_perso.getMapa().getPrePelea()) {
				// System.out.println("2");
				if (!_perso.estaDisponible(false, true) || _perso.estaInmovil()) {
					GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'o');
					return;
				}
				if (_perso.esFantasma() || _perso.esTumba()) {
					GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'd');
					return;
				}
				if (!_perso.getMapa().puedeAgregarOtraPelea()) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1MAP_LIMI_OF_FIGHTS");
					return;
				}
				final Personaje agredido = Mundo.getPersonaje(Integer.parseInt(packet.substring(5)));
				if (agredido == null) {
					GestorSalida.ENVIAR_BN_NADA(_perso, "NO EXISTE AGREDIDO");
					return;
				}
				if (System.currentTimeMillis() - _tiempoLLegoMapa < MainServidor.SEGUNDOS_AGREDIR_RECIEN_LLEGADO_MAPA * 1000
				|| System.currentTimeMillis() - agredido.getCuenta()
				.getSocket()._tiempoLLegoMapa < MainServidor.SEGUNDOS_AGREDIR_RECIEN_LLEGADO_MAPA * 1000) {
					GestorSalida.ENVIAR_BN_NADA(_perso, "NO PUEDES AGREDIR POR "
					+ MainServidor.SEGUNDOS_AGREDIR_RECIEN_LLEGADO_MAPA);
					return;
				}
				if (!Constantes.puedeAgredir(_perso, agredido)) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1DONT_ATTACK_PLAYER");
					return;
				}
				if (!MainServidor.PARAM_AGRESION_ADMIN && (agredido.getCuenta().getAdmin() > 0 || _cuenta.getAdmin() > 0)) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1DONT_ATTACK_PLAYER");
					return;
				}
				if (!_perso.getCuenta().getActualIP().equals("127.0.0.1") && _perso.getCuenta().getActualIP().equals(agredido
				.getCuenta().getActualIP())) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1DONT_ATTACK_PLAYER_SAME_IP");
					return;
				}
				// System.out.println("3");
				// Thread.sleep(20000);
				// System.out.println("4");
				boolean deshonor = false;
				if (MainServidor.PARAM_AGREDIR_JUGADORES_ASESINOS && agredido.getDeshonor() > 0 && agredido
				.getAlineacion() != _perso.getAlineacion()) {
					// salta para irse a atacar
				} else if (_perso.getMapa().mapaNoAgresion() || _perso.getMapa().getSubArea().getArea().getSuperArea()
				.getID() == 3 || MainServidor.SUBAREAS_NO_PVP.contains(_perso.getMapa().getSubArea().getID())) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "113");
					return;
				} else if (!MainServidor.PARAM_AGREDIR_NEUTRAL && agredido.getAlineacion() == Constantes.ALINEACION_NEUTRAL) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1DONT_ATTACK_PLAYER");
					return;
				} else if (!MainServidor.PARAM_AGREDIR_ALAS_DESACTIVADAS && !agredido.alasActivadas()) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1DONT_ATTACK_PLAYER");
					return;
				} else if (agredido.getAlineacion() == Constantes.ALINEACION_NEUTRAL || !agredido.alasActivadas()) {
					if (_perso.getAlineacion() != Constantes.ALINEACION_NEUTRAL) {
						_perso.addDeshonor(1);
						GestorSalida.ENVIAR_Im_INFORMACION(_perso, "084;1");
						deshonor = true;
					}
				}
				_perso.getMapa().iniciarPeleaPVP(_perso, agredido, deshonor);
			}
		} catch (final Exception e) {}
	}
	
	private void juego_Ataque_Caceria(final String packet) {
		try {
			synchronized (_perso.getMapa().getPrePelea()) {
				if (!_perso.estaDisponible(false, true) || _perso.estaInmovil()) {
					GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'o');
					return;
				}
				if (_perso.esFantasma() || _perso.esTumba()) {
					GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'd');
					return;
				}
				final int id = Integer.parseInt(packet.substring(5));
				final Personaje agredido = Mundo.getPersonaje(id);
				if (!Mundo.NOMBRE_CACERIA.equalsIgnoreCase(agredido.getNombre())) {
					return;
				}
				if (agredido == null || !agredido.enLinea() || !agredido.estaDisponible(true, true) || agredido
				.getMapa() != _perso.getMapa()) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1DONT_ATTACK_PLAYER");
					return;
				}
				GestorSalida.ENVIAR_GA_ACCION_JUEGO_AL_MAPA(_perso.getMapa(), -1, 906, _perso.getID() + "", id + "");
				_perso.getMapa().iniciarPelea(_perso, agredido, _perso.getCelda().getID(), agredido.getCelda().getID(),
				Constantes.PELEA_TIPO_CACERIA, null);
			}
			// _perso.getPelea().cargarMultiman(_perso);
		} catch (final Exception e) {}
	}
	
	private void juego_Desafiar(final String packet) {
		try {
			if (!_perso.estaDisponible(false, true)) {
				GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'o');
				return;
			}
			if (_perso.esFantasma() || _perso.esTumba()) {
				GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'd');
				return;
			}
			if (_perso.getMapa().mapaNoDesafio()) {// || _perso.getMapa().mapaMazmorra()
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "113");
				return;
			}
			if (!_perso.getMapa().puedeAgregarOtraPelea()) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1MAP_LIMI_OF_FIGHTS");
				return;
			}
			if (System.currentTimeMillis() - _perso.getTiempoUltDesafio() <= (MainServidor.SEGUNDOS_ENTRE_DESAFIOS_PJ
			* 1000)) {
				GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'o');
				return;
			}
			final int desafiadoID = Integer.parseInt(packet.substring(5));
			final Personaje invitandoA = Mundo.getPersonaje(desafiadoID);
			if (invitandoA == null || invitandoA == _perso || !invitandoA.enLinea() || !invitandoA.estaDisponible(true, true)
			|| invitandoA.getMapa() != _perso.getMapa()) {
				GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'z');
				return;
			}
			if (!_perso.puedeInvitar() || !invitandoA.puedeInvitar()) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1PLAYERS_IS_BUSSY");
				return;
			}
			if (!invitandoA.estaVisiblePara(_perso)) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1209");
				return;
			}
			_perso.setTiempoUltDesafio();
			_perso.setInvitandoA(invitandoA, "desafio");
			invitandoA.setInvitador(_perso, "desafio");
			GestorSalida.ENVIAR_GA900_DESAFIAR(_perso.getMapa(), _perso.getID(), invitandoA.getID());
		} catch (final Exception e) {}
	}
	
	private boolean juego_Aceptar_Desafio(final String packet) {
		synchronized (_perso.getMapa().getPrePelea()) {
			if (!_perso.getTipoInvitacion().equals("desafio")) {
				GestorSalida.ENVIAR_BN_NADA(_perso);
				return false;
			}
			if (_perso.estaInmovil()) {
				GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'o');
				return false;
			}
			if (_perso.esFantasma() || _perso.esTumba()) {
				GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'd');
				return false;
			}
			Personaje retador = _perso.getInvitador();
			if (retador == null || !retador.enLinea()) {
				GestorSalida.ENVIAR_GA903_ERROR_PELEA(retador, 'o');
				return false;
			}
			if (!_perso.getMapa().puedeAgregarOtraPelea()) {
				GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1MAP_LIMI_OF_FIGHTS");
				return false;
			}
			GestorSalida.ENVIAR_GA901_ACEPTAR_DESAFIO(_perso, retador.getID(), _perso.getID());
			GestorSalida.ENVIAR_GA901_ACEPTAR_DESAFIO(retador, retador.getID(), _perso.getID());
			retador.setInvitador(null, "");
			_perso.setInvitandoA(null, "");
			_perso.getMapa().iniciarPelea(retador, _perso, retador.getCelda().getID(), _perso.getCelda().getID(),
			Constantes.PELEA_TIPO_DESAFIO, null);
		}
		return true;
	}
	
	private void juego_Ataque_CAC(final String packet) {
		try {
			final Pelea pelea = _perso.getPelea();
			if (pelea == null || pelea.getFase() != Constantes.PELEA_FASE_COMBATE) {
				GestorSalida.ENVIAR_BN_NADA(_perso, "JUEGO ATAQUE CAC");
				return;
			}
			Personaje perso = _perso;
			Luchador luch = pelea.getLuchadorPorID(perso.getID());
			if (!luch.puedeJugar()) {
				return;
			}
			final short celdaID = Short.parseShort(packet.substring(5));
			pelea.intentarCAC(_perso, celdaID);
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "JUEGO ATAQUE CAC EXCEPTION");
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", juego_Ataque_CAC " + e.toString());
			e.printStackTrace();
		}
	}
	
	private void juego_Lanzar_Hechizo(final String packet) {
		try {
			final Pelea pelea = _perso.getPelea();
			if (pelea == null || pelea.getFase() != Constantes.PELEA_FASE_COMBATE) {
				GestorSalida.ENVIAR_BN_NADA(_perso, "JUEGO LANZAR HECHIZO");
				return;
			}
			Personaje perso = _perso;
			Luchador luch = pelea.getLuchadorPorID(perso.getID());
			if (!luch.puedeJugar()) {
				if (perso.getCompañero() != null) {
					luch = pelea.getLuchadorPorID(perso.getCompañero().getID());
					perso = perso.getCompañero();
					if (!luch.puedeJugar()) {
						luch = null;
						perso = null;
						for (Personaje invo : _perso.invocacionControlable) {
							Luchador luchTemp = pelea.getLuchadorPorID(invo.getID());
							if (luchTemp.puedeJugar()) {
								luch = luchTemp;
								perso = invo;
							}
						}

					}
				} else {
					luch = null;
					perso = null;
					for (Personaje invo : _perso.invocacionControlable) {
						Luchador luchTemp = pelea.getLuchadorPorID(invo.getID());
						if (luchTemp.puedeJugar()) {
							luch = luchTemp;
							perso = invo;
						}
					}
				}
			}
			if (luch == null) {
				return;
			}

			try {
				final String[] split = packet.split(";");
				final int hechizoID = Integer.parseInt(split[0].substring(5));
				if (!perso.tieneHechizoID(hechizoID)) {
					GestorSalida.ENVIAR_Im_INFORMACION(_perso, "1169");
					return;
				}
				final short celdaID = Short.parseShort(split[1]);
				final StatHechizo SH = perso.getStatsHechizo(hechizoID);
				if (SH != null) {
					pelea.intentarLanzarHechizo(luch, SH, pelea.getMapaCopia().getCelda(celdaID), false);
				}
			} catch (Exception e) {
				GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(_perso,
						"Ha ocurrido un error al lanzar el hechizo por favor vuelvalo a intentar");
				GestorSalida.ENVIAR_BN_NADA(_perso, "JUEGO LANZAR HECHIZO EXCEPTION");
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "JUEGO LANZAR HECHIZO EXCEPTION");
			MainServidor.redactarLogServidorln("EXCEPTION Packet " + packet + ", juego_Lanzar_Hechizo " + e.toString());
			e.printStackTrace();
		}
	}
	
	private void juego_Unirse_Pelea(final String packet) {
		try {
			if (!_perso.estaDisponible(false, true)) {
				GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'o');
				return;
			}
			if (_perso.esFantasma() || _perso.esTumba()) {
				GestorSalida.ENVIAR_GA903_ERROR_PELEA(_perso, 'd');
				return;
			}
			final String[] infos = packet.substring(5).split(";");
			Pelea pelea = _perso.getMapa().getPelea(Short.parseShort(infos[0]));
			if (infos.length == 1) {
				pelea.unirseEspectador(_perso, _cuenta.getAdmin() > 0);
			} else {
				if (pelea.unirsePelea(_perso, Integer.parseInt(infos[1]))) {
					if (_perso.esMaestro()) {
						_perso.getGrupoParty().packetSeguirLider(packet);
					}
				}
			}
		} catch (final Exception e) {
			GestorSalida.ENVIAR_BN_NADA(_perso, "JUEGO UNIRSE PELEA EXCEPTION");
		}
	}
	
	private AccionDeJuego getAccionJuego(int unicaID) {
		return _accionesDeJuego.get(unicaID);
	}
	
	public synchronized void limpiarAcciones(boolean forzar) {
		_accionesDeJuego.clear();
		if (forzar) {
			_realizaciandoAccion = false;
		}
	}
	
	public synchronized void borrarAccionJuego(int unicaID, boolean forzar) {
		_accionesDeJuego.remove(unicaID);
		if (forzar) {
			_realizaciandoAccion = false;
		}
	}
	
	private synchronized void addAccionJuego(AccionDeJuego AJ) {
		try {
			int idUnica = 1;
			if (!_accionesDeJuego.isEmpty()) {
				idUnica = ((Integer) _accionesDeJuego.keySet().toArray()[_accionesDeJuego.size() - 1] + 1);
			}
			AJ.setIDUnica(idUnica);
			_accionesDeJuego.put(idUnica, AJ);
		} catch (Exception e) {
			String error = "EXCEPTION addAccionJuego e: " + e.toString();
			GestorSalida.ENVIAR_BN_NADA(_perso, error);
			MainServidor.redactarLogServidorln(error);
		}
	}
	
	private synchronized void cumplirSiguienteAccion() {
		try {
			if (_accionesDeJuego.isEmpty()) {
				return;
			}
			AccionDeJuego AJ = (AccionDeJuego) _accionesDeJuego.values().toArray()[0];
			if (AJ != null) {
				_realizaciandoAccion = true;
				// Thread.sleep(500);
				realizarAccion(AJ);
			}
		} catch (Exception e) {
			String error = "EXCEPTION cumplirSiguienteAccion e: " + e.toString();
			GestorSalida.ENVIAR_BN_NADA(_perso, error);
			MainServidor.redactarLogServidorln(error);
		}
	}
	
	private void realizarAccion(final AccionDeJuego AJ) {
		try {
			if (AJ == null) {
				return;
			}
			switch (AJ.getAccionID()) {
				case 1 :
					if (!_perso.inicioAccionMoverse(AJ)) {
						limpiarAcciones(true);
					}
					break;
				case 500 :
					if (!_perso.puedeIniciarAccionEnCelda(AJ)) {
						borrarAccionJuego(AJ.getIDUnica(), true);
						cumplirSiguienteAccion();
					}
					break;
			}
		} catch (Exception e) {
			String error = "EXCEPTION realizarAccion AJ.getPacket(): " + AJ.getPathPacket() + " e: " + e.toString();
			GestorSalida.ENVIAR_BN_NADA(_perso, error);
			MainServidor.redactarLogServidorln(error);
		}
	}
	public static class AccionDeJuego {
		private int _idUnica, _accionID, _celdas;
		private long _tiempoInicio;
		private String _pathPacket, _pathMover, _packet;
		
		private AccionDeJuego(final int accionID, String packet) {
			_accionID = accionID;
			_pathPacket = packet.substring(5);
			_packet = packet;
			_tiempoInicio = System.currentTimeMillis();
		}
		
		public long getTiempoInicio() {
			return _tiempoInicio;
		}
		
		public void setCeldas(int celdas) {
			_celdas = celdas;
		}
		
		public int getCeldas() {
			return _celdas;
		}
		
		private void setIDUnica(int id) {
			_idUnica = id;
		}
		
		private int getAccionID() {
			return _accionID;
		}
		
		public int getIDUnica() {
			return _idUnica;
		}
		
		public String getPathReal() {
			return _pathMover;
		}
		
		public void setPathReal(String path) {
			_pathMover = path;
		}
		
		public String getPathPacket() {
			return _pathPacket;
		}
		
		public String getPacket() {
			return _packet;
		}
	}
}
