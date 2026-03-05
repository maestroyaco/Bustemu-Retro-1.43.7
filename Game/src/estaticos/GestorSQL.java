package estaticos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.regex.Pattern;
import variables.casa.Casa;
import variables.casa.Cofre;
import variables.encarnacion.EncarnacionModelo;
import variables.gremio.Gremio;
import variables.gremio.MiembroGremio;
import variables.gremio.Recaudador;
import variables.hechizo.Hechizo;
import variables.hechizo.StatHechizo;
import variables.mapa.Area;
import variables.mapa.Cercado;
import variables.mapa.Mapa;
import variables.mapa.SubArea;
import variables.mapa.interactivo.ObjetoInteractivoModelo;
import variables.mapa.interactivo.OtroInteractivo;
import variables.mercadillo.Mercadillo;
import variables.mercadillo.ObjetoMercadillo;
import variables.mision.MisionModelo;
import variables.mob.GrupoMob;
import variables.mob.MobModelo;
import variables.mob.MobModelo.TipoGrupo;
import variables.montura.Montura;
import variables.montura.MonturaModelo;
import variables.npc.NPC;
import variables.npc.NPCModelo;
import variables.npc.PreguntaNPC;
import variables.npc.RespuestaNPC;
import variables.objeto.CreaTuItem;
import variables.objeto.MascotaModelo;
import variables.objeto.Objeto;
import variables.objeto.ObjetoModelo;
import variables.objeto.ObjetoSet;
import variables.objeto.ObjetoModelo.CAPACIDAD_STATS;
import variables.oficio.Oficio;
import variables.pelea.DropMob;
import variables.personaje.Clase;
import variables.personaje.Cuenta;
import variables.personaje.Especialidad;
import variables.personaje.Personaje;
import variables.ranking.RankingKoliseo;
import variables.ranking.RankingPVP;
import variables.zotros.Accion;
import variables.zotros.Almanax;
import variables.zotros.Animacion;
import variables.zotros.Ornamento;
import variables.zotros.Prisma;
import variables.zotros.Servicio;
import variables.zotros.TiendaCategoria;
import variables.zotros.TiendaObjetos;
import variables.zotros.Titulo;
import variables.zotros.Tutorial;
import java.sql.PreparedStatement;
import estaticos.Mundo.Duo;
import estaticos.Mundo.Experiencia;
//import java.util.Timer;
//import java.util.TimerTask;

public class GestorSQL {
	private static Connection _bdDinamica;
	private static Connection _bdEstatica;
	private static Connection _bdCuentas;
	private static Connection _bdAlterna;
	private static Timer _timerComienzo;
	private static boolean _necesitaCommit;
	public static StringBuilder LOG_SQL = new StringBuilder();
	
	// public static ResultSet consultaSQL(final PreparedStatement declaracion) throws Exception {
	// if (!Bustemu._INICIADO) {
	// return null;
	// }
	// final ResultSet resultado = declaracion.executeQuery();
	// declaracion.setQueryTimeout(300);
	// return resultado;
	// }
	// private static PreparedStatement preparedQuery(final String consultaSQL, final Connection
	// conexion) throws Exception {
	// if (!Bustemu._INICIADO) {
	// return null;
	// }
	// return (PreparedStatement) conexion.prepareStatement(consultaSQL);
	// }
	//
	private static void cerrarResultado(final ResultSet resultado) {
		try {
			resultado.getStatement().close();
			resultado.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void exceptionExit(Exception e, String metodo) {
		MainServidor.redactarLogServidorln("EXCEP EXIT SQL " + metodo + ": " + e.toString());
		e.printStackTrace();
		System.exit(1);
	}

	private static void exceptionNormal(Exception e, String metodo) {
		MainServidor.redactarLogServidorln("EXCEP NORMAL SQL " + metodo + ": " + e.toString());
		e.printStackTrace();
	}

	private static void exceptionModify(Exception e, String consultaSQL, String metodo) {
		MainServidor.redactarLogServidorln("EXCEP MODIFY SQL " + metodo + ": " + e.toString());
		MainServidor.redactarLogServidorln("LINEA MODIFY SQL " + metodo + ": " + consultaSQL);
		e.printStackTrace();
	}

	public static ResultSet consultaSQL(final String consultaSQL, final Connection conexion) throws Exception {
		final PreparedStatement declaracion = conexion.prepareStatement(consultaSQL);  // ✅ sin cast
		declaracion.setQueryTimeout(300); 
		final ResultSet resultado = declaracion.executeQuery();
		return resultado;
	}

	public static PreparedStatement transaccionSQL(final String consultaSQL, final Connection conexion) throws Exception {
		final PreparedStatement declaracion = conexion.prepareStatement(consultaSQL);  // ✅ sin cast
		_necesitaCommit = true;
		return declaracion;
	}

	private static int ejecutarTransaccion(PreparedStatement declaracion) {
		int ejecutar = 0;
		try {
			ejecutar = declaracion.executeUpdate();
		} catch (SQLException e) {
			MainServidor.redactarLogServidorln("EXECUTE UPDATE " + declaracion.toString());
			e.printStackTrace();
		}
		String str = declaracion.toString();
		LOG_SQL.append(System.currentTimeMillis() + " " + str.substring(str.indexOf(":")) + "\n");
		return ejecutar;
	}

	
	public static void ejecutarBatch(PreparedStatement declaracion) {
		try {
			declaracion.executeBatch();
			cerrarDeclaracion(declaracion);
		} catch (SQLException e) {
			MainServidor.redactarLogServidorln("EXECUTE UPDATE " + declaracion.toString());
			e.printStackTrace();
		}
	}
	
	public static void cerrarDeclaracion(final PreparedStatement declaracion) {
		try {
			declaracion.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void timerCommit(final boolean iniciar) {
		if (MainServidor.PARAM_AUTO_COMMIT) {
			return;
		}
		if (iniciar) {
			_timerComienzo = new Timer();
			_timerComienzo.schedule(new TimerTask() {
				public void run() {
					if (!_necesitaCommit || MainServidor.PARAM_DESHABILITAR_SQL) {
						return;
					}
					iniciarCommit(true);
				}
			}, MainServidor.SEGUNDOS_TRANSACCION_BD * 1000, MainServidor.SEGUNDOS_TRANSACCION_BD * 1000);
		} else if (_timerComienzo != null) {
			_timerComienzo.cancel();
		}
	}
	// Edit by Maestro-yaco para usar MYSQL en Linux VPS
	public static final boolean iniciarConexion() {
	    try {
	        // Registrar el driver actualizado
	        Class.forName("com.mysql.cj.jdbc.Driver");

	        _bdDinamica = DriverManager.getConnection(
	            "jdbc:mysql://" + MainServidor.BD_HOST + "/" + MainServidor.BD_DINAMICA
	            + "?autoReconnect=true&useSSL=false&serverTimezone=UTC",
	            MainServidor.BD_USUARIO,
	            MainServidor.BD_PASS
	        );
	        _bdDinamica.setAutoCommit(MainServidor.PARAM_AUTO_COMMIT);

	        _bdEstatica = DriverManager.getConnection(
	            "jdbc:mysql://" + MainServidor.BD_HOST + "/" + MainServidor.BD_ESTATICA
	            + "?autoReconnect=true&useSSL=false&serverTimezone=UTC",
	            MainServidor.BD_USUARIO,
	            MainServidor.BD_PASS
	        );
	        _bdEstatica.setAutoCommit(MainServidor.PARAM_AUTO_COMMIT);

	        _bdCuentas = DriverManager.getConnection(
	            "jdbc:mysql://" + MainServidor.BD_HOST + "/" + MainServidor.BD_CUENTAS
	            + "?autoReconnect=true&useSSL=false&serverTimezone=UTC",
	            MainServidor.BD_USUARIO,
	            MainServidor.BD_PASS
	        );
	        _bdCuentas.setAutoCommit(MainServidor.PARAM_AUTO_COMMIT);

	        if (!_bdEstatica.isValid(2) || !_bdDinamica.isValid(2) || !_bdCuentas.isValid(2)) {
	            MainServidor.redactarLogServidorln("SQLError : Conexion a la BDD invalida");
	            return false;
	        }
	        timerCommit(true);
	        return true;
	    } catch (final Exception e) {
	        MainServidor.redactarLogServidorln("ERROR SQL INICIAR CONEXION: " + e.toString());
	        e.printStackTrace();
	    }
	    return false;
	}

	
	public static final String conexionAlterna(String host, String database, String user, String pass) {
		try {
			if (_bdAlterna != null) {
				try {
					_bdAlterna.close();
				} catch (Exception e1) {}
			}
			_bdAlterna = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database, user, pass);
			if (!_bdAlterna.isValid(1000)) {
				return "BAD :(";
			}
			return "GOOD!!";
		} catch (Exception e) {
			return e.toString();
		}
	}
	
	public static void iniciarCommit(boolean reiniciando) {
		if (MainServidor.PARAM_AUTO_COMMIT) {
			return;
		}
		try {
			if (reiniciando) {
				if (_bdDinamica.isClosed() || _bdEstatica.isClosed() || _bdCuentas.isClosed()) {
					cerrarConexion();
					iniciarConexion();
				}
			}
			_necesitaCommit = false;
			try {
				_bdCuentas.commit();
			} catch (final Exception e) {
				MainServidor.redactarLogServidorln("EXCEPTION COMMIT ACCOUNTS: " + e.toString());
				e.printStackTrace();
			}
			try {
				_bdEstatica.commit();
			} catch (final Exception e) {
				MainServidor.redactarLogServidorln("EXCEPTION COMMIT STATIC: " + e.toString());
				e.printStackTrace();
			}
			try {
				_bdDinamica.commit();
			} catch (final Exception e) {
				MainServidor.redactarLogServidorln("EXCEPTION COMMIT DYNAMIC: " + e.toString());
				e.printStackTrace();
			}
			Thread.sleep(1000);
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("SQL ERROR COMENZAR TRANSACCIONES: " + e.toString());
			e.printStackTrace();
		}
	}
	
	public static void cerrarConexion() {
		iniciarCommit(false);
		try {
			_bdCuentas.close();
			_bdDinamica.close();
			_bdEstatica.close();
			MainServidor.redactarLogServidorln("########## CONEXIONES SQL CERRADAS ##########");
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("SQL ERROR CERRAR CONEXION: " + e.toString());
			e.printStackTrace();
		}
	}
	
	public static boolean ES_IP_BANEADA(final String ip) {
		boolean b = false;
		String consultaSQL = "SELECT `ip` FROM `banip` WHERE `ip` = '" + ip + "';";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			if (resultado.next()) {
				b = true;
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return b;
	}
	
	public static String LISTA_BAN_IP() {
		final StringBuilder str = new StringBuilder();
		try {
			final ResultSet resultado = consultaSQL("SELECT `ip` FROM `banip`;", _bdCuentas);
			while (resultado.next()) {
				if (str.length() > 0) {
					str.append(", ");
				}
				str.append(resultado.getString("ip"));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return str.toString();
	}
	
	public static boolean INSERT_BAN_IP(final String ip) {
		// return true;
		MainServidor.redactarLogServidorln("IP BANEADA : " + ip);
		GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1IP_BAN;" + ip);
		final String consultaSQL = "INSERT INTO `banip` (`ip`) VALUES (?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
			declaracion.setString(1, ip);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean DELETE_BAN_IP(final String ip) {
		final String consultaSQL = "DELETE FROM `banip` WHERE `ip` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
			declaracion.setString(1, ip);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static byte GET_VIP(final String cuenta) {
		byte b = 0;
		String consultaSQL = "SELECT * FROM `cuentas` WHERE `cuenta` = '" + cuenta + "' ;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			while (resultado.next()) {
				try {
					b = resultado.getByte("vip");
				} catch (final Exception e) {
					b = 1;
				}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			// exceptionNormal(e, "");
		}
		return b;
	}
	
	public static byte GET_RANGO(final String cuenta) {
		byte b = 0;
		String consultaSQL = "SELECT `rango` FROM `cuentas` WHERE `cuenta` = '" + cuenta + "' ;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			while (resultado.next()) {
				try {
					b = resultado.getByte("rango");
				} catch (final Exception e) {}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return b;
	}
	
	// public static String GET_ULTIMA_IP(final String cuenta) {
	// String str = "";
	// String consultaSQL = "SELECT `ultimaIP` FROM `cuentas` WHERE `cuenta` = '" + cuenta + "' ;";
	// try {
	// final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
	// while (resultado.next()) {
	// str = resultado.getString("ultimaIP");
	// }
	// cerrarResultado(resultado);
	// } catch (final Exception e) {
	// Bustemu.redactarLogServidorln("ERROR SQL GET ULTIMA IP: " + e.toString());
	// Bustemu.redactarLogServidorln("LINEA SQL: " + consultaSQL);
	// e.printStackTrace();
	// }
	// return str;
	// }
	public static int GET_ID_WEB(final String cuenta) {
		int str = -1;
		String consultaSQL = "SELECT `idWeb` FROM `cuentas` WHERE `cuenta` = '" + cuenta + "' ;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			if (resultado.next()) {
				str = resultado.getInt("idWeb");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return str;
	}
	
	public static String GET_APODO(final String cuenta) {
		String str = "";
		String consultaSQL = "SELECT `apodo` FROM `cuentas` WHERE `cuenta` = '" + cuenta + "' ;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			if (resultado.next()) {
				str = resultado.getString("apodo");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return str;
	}
	
	public static long GET_ABONO(final String cuenta) {
		long l = 0;
		String consultaSQL = "SELECT `abono` FROM `cuentas` WHERE `cuenta` = '" + cuenta + "' ;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			if (resultado.next()) {
				l = resultado.getLong("abono");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return l;
	}
	
	public static void SET_ABONO(final long abono, final int cuentaID) {
		String consultaSQL = "UPDATE `cuentas` SET `abono`='" + abono + "' WHERE `id`= '" + cuentaID + "'";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static int GET_OGRINAS_CUENTA(final int cuentaID) {
		if (MainServidor.PARAM_NO_USAR_OGRINAS) {
			return 9999999;
		}
		int i = 0;
		String consultaSQL = "SELECT `ogrinas` FROM `cuentas` WHERE `id` = '" + cuentaID + "' ;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			if (resultado.next()) {
				i = resultado.getInt("ogrinas");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return i;
	}
	
	private static boolean SET_OGRINAS_CUENTA(int monto, final int cuentaID, Personaje perso) {
		if (MainServidor.PARAM_NO_USAR_OGRINAS) {
			return false;
		}
		if (monto < 0) {
			monto = 0;
		}
		String consultaSQL = "UPDATE `cuentas` SET `ogrinas` = '" + monto + "' WHERE `id` = '" + cuentaID + "'";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
			return false;
		} finally {
			if (MainServidor.MODO_ALL_OGRINAS && perso != null) {
				GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(perso);
			}
		}
		return true;
	}
	
	
	public static void RESTAR_OGRINAS(final Cuenta cuenta, int restar) {
		try {
			int ogrinas = GET_OGRINAS_CUENTA(cuenta.getID());
			if (ogrinas < restar) {
				restar = ogrinas;
			}
			if (!SET_OGRINAS_CUENTA(ogrinas - restar, cuenta.getID(), null)) {
				return;
			}
		} catch (final Exception ignored) {
		}
	}
	
	public static int GET_CREDITOS_CUENTA(final int cuentaID) {
		if (MainServidor.PARAM_NO_USAR_CREDITOS) {
			return 9999999;
		}
		int i = 0;
		String consultaSQL = "SELECT `creditos` FROM `cuentas` WHERE `id` = '" + cuentaID + "' ;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			if (resultado.next()) {
				i = resultado.getInt("creditos");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return i;
	}
	
	public static void SET_OGRINAS_CUENTA(final int ogrinas, final int cuentaID) {
		if (MainServidor.PARAM_NO_USAR_OGRINAS) {
			return;
		}
		String consultaSQL = "UPDATE `cuentas` SET `ogrinas` = '" + ogrinas + "' WHERE `id` = '" + cuentaID + "'";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void SET_CREDITOS_CUENTA(final int creditos, final int cuentaID) {
		if (MainServidor.PARAM_NO_USAR_CREDITOS) {
			return;
		}
		String consultaSQL = "UPDATE `cuentas` SET `creditos` = '" + creditos + "' WHERE `id` = '" + cuentaID + "'";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void ADD_OGRINAS_CUENTA(final long ogrinas, final int cuentaID) {
		if (MainServidor.PARAM_NO_USAR_OGRINAS) {
			return;
		}
		int exOgrinas = GET_OGRINAS_CUENTA(cuentaID);
		String consultaSQL = "UPDATE `cuentas` SET `ogrinas` = '" + (ogrinas + exOgrinas) + "' WHERE `id` = '" + cuentaID
		+ "'";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void ADD_CREDITOS_CUENTA(final long creditos, final int cuentaID) {
		if (MainServidor.PARAM_NO_USAR_CREDITOS) {
			return;
		}
		int exOgrinas = GET_CREDITOS_CUENTA(cuentaID);
		String consultaSQL = "UPDATE `cuentas` SET `creditos` = '" + (creditos + exOgrinas) + "' WHERE `id` = '" + cuentaID
		+ "'";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static boolean RESTAR_OGRINAS(final Cuenta cuenta, final long restar, Personaje perso) {
		if (MainServidor.PARAM_NO_USAR_OGRINAS) {
			return true;
		}
		boolean resto = false;
		try {
			String consultaSQL = "SELECT `ogrinas` FROM `cuentas` WHERE `id` = '" + cuenta.getID() + "' ;";
			int ogrinas = GET_OGRINAS_CUENTA(cuenta.getID());
			if (restar <= 0 || ogrinas < restar) {
				if (perso != null) {
					GestorSalida.ENVIAR_Im_INFORMACION(perso, "1ERROR_BUY_WITH_OGRINES;" + (restar - ogrinas));
				}
				return false;
			}
			consultaSQL = "UPDATE `cuentas` SET `ogrinas` = ? WHERE `id` = '" + cuenta.getID() + "';";
			try {
				final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
				declaracion.setLong(1, ogrinas - restar);
				ejecutarTransaccion(declaracion);
				cerrarDeclaracion(declaracion);
				resto = true;
			} catch (final Exception e) {
				exceptionModify(e, consultaSQL, "");
				return false;
			}
			if (perso != null) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1EXITO_BUY_VIP;" + (ogrinas - restar) + "~"
				+ MainServidor.NOMBRE_SERVER);
			}
			// GestorSalida.ENVIAR_bOA_ACTUALIZAR_PANEL_OGRINAS(out, ogrinas - restar);
			return true;
		} catch (final Exception e) {
			if (resto) {
				ADD_OGRINAS_CUENTA(restar, cuenta.getID());
			}
			exceptionNormal(e, "RESTAR OGRINAS A " + cuenta.getNombre() + ", OGRINAS " + restar + ", LE RESTO? " + resto);
		}
		return false;
	}
	
	public static boolean RESTAR_CREDITOS(final Cuenta cuenta, final long restar, Personaje perso) {
		if (MainServidor.PARAM_NO_USAR_CREDITOS) {
			return true;
		}
		boolean resto = false;
		try {
			String consultaSQL = "SELECT `creditos` FROM `cuentas` WHERE `id` = '" + cuenta.getID() + "' ;";
			int creditos = GET_CREDITOS_CUENTA(cuenta.getID());
			if (restar <= 0 || creditos < restar) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1ERROR_BUY_VIP;" + (restar - creditos));
				return false;
			}
			consultaSQL = "UPDATE `cuentas` SET `creditos` = ? WHERE `id` = '" + cuenta.getID() + "';";
			try {
				final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
				declaracion.setLong(1, creditos - restar);
				ejecutarTransaccion(declaracion);
				cerrarDeclaracion(declaracion);
				resto = true;
			} catch (final Exception e) {
				exceptionModify(e, consultaSQL, "");
				return false;
			}
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "1EXITO_BUY_WITH_CREDITS;" + (creditos - restar) + "~"
			+ MainServidor.NOMBRE_SERVER);
			// GestorSalida.ENVIAR_bOA_ACTUALIZAR_PANEL_OGRINAS(out, ogrinas - restar);
			return true;
		} catch (final Exception e) {
			if (resto) {
				ADD_CREDITOS_CUENTA(restar, cuenta.getID());
			}
			exceptionNormal(e, "RESTAR CREDITOS A " + cuenta.getNombre() + ", CREDITOS " + restar + ", LE RESTO? " + resto);
		}
		return false;
	}
	
	public static String GET_CONTRASEÑA_CUENTA(final String cuenta) {
		String str = "";
		String consultaSQL = "SELECT `contraseña` FROM `cuentas` WHERE `cuenta` = '" + cuenta + "' ;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			if (resultado.next()) {
				try {
					str = resultado.getString("contraseña");
				} catch (final Exception e) {}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return str;
	}
	
	public static void CAMBIAR_CONTRASEÑA_CUENTA(final String contraseña, final int cuentaID) {
		String consultaSQL = "UPDATE `cuentas` SET `contraseña`= ? WHERE `id`= ?";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
			declaracion.setString(1, contraseña);
			declaracion.setInt(2, cuentaID);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static String GET_PREGUNTA_SECRETA(final String cuenta) {
		String str = "";
		String consultaSQL = "SELECT `pregunta` FROM `cuentas` WHERE `cuenta` = '" + cuenta + "' ;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			if (resultado.next()) {
				try {
					str = resultado.getString("pregunta");
				} catch (final Exception e) {}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return str;
	}
	
	public static long GET_BANEADO(final String cuenta) {
		long i = 0;
		try {
			String consultaSQL = "SELECT `baneado` FROM `cuentas` WHERE `cuenta` = '" + cuenta + "' ;";
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			if (resultado.next()) {
				i = resultado.getLong("baneado");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return i;
	}
	
	public static String GET_RESPUESTA_SECRETA(final String cuenta) {
		String str = "";
		String consultaSQL = "SELECT `respuesta` FROM `cuentas` WHERE `cuenta` = '" + cuenta + "' ;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			if (resultado.next()) {
				try {
					str = resultado.getString("respuesta");
				} catch (final Exception e) {}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return str;
	}
	
	public static void SET_RANGO(final String cuenta, final int rango) {
		String consultaSQL = "UPDATE `cuentas` SET `rango` = ? WHERE `cuenta` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
			declaracion.setInt(1, rango);
			declaracion.setString(2, cuenta);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	// public static void SET_ULTIMA_IP(final String cuenta, final String ip) {
	// String consultaSQL = "UPDATE `cuentas` SET `ultimaIP` = ? WHERE `cuenta` = ?;";
	// try {
	// final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
	// declaracion.setString(1, ip);
	// declaracion.setString(2, cuenta);
	// ejecutarTransaccion(declaracion);
	// cerrarDeclaracion(declaracion);
	// } catch (final Exception e) {
	// Bustemu.redactarLogServidorln("ERROR SQL SET ULTIMA IP: " + e.toString());
	// Bustemu.redactarLogServidorln("LINEA SQL: " + consultaSQL);
	// e.printStackTrace();
	// }
	// }
	public static void SET_BANEADO(final String cuenta, final long baneado) {
		String consultaSQL = "UPDATE `cuentas` SET `baneado` = '" + baneado + "' WHERE `cuenta` = '" + cuenta + "';";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static int GET_CUENTAS_CONECTADAS_IP(final String ip) {
		int i = 0;
		String consultaSQL = "SELECT * FROM `cuentas` WHERE `ultimaIP` = '" + ip + "' AND `logeado` = 1 ;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			while (resultado.next()) {
				i++;
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return i;
	}
	
	// public static void UPDATE_CUENTA_LOGUEADO(final int cuentaID, final byte log) {
	// String consultaSQL = "UPDATE `cuentas` SET `logeado`= " + log + " WHERE `id`=" + cuentaID +
	// ";";
	// try {
	// final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
	// ejecutarTransaccion(declaracion);
	// cerrarDeclaracion(declaracion);
	// } catch (final Exception e) {
	// exceptionModify(e, consultaSQL, "");
	// }
	// }
	//
	// public static void UPDATE_TODAS_CUENTAS_CERO() {
	// final String cuentas = Mundo.strCuentasOnline();
	// if (cuentas.isEmpty()) {
	// return;
	// }
	// String consultaSQL = "UPDATE `cuentas` SET `logeado`= 0 WHERE `id` IN (" + cuentas + ");";
	// try {
	// final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
	// ejecutarTransaccion(declaracion);
	// cerrarDeclaracion(declaracion);
	// } catch (final Exception e) {
	// exceptionModify(e, consultaSQL, "");
	// }
	// }
	public static void CARGAR_CUENTA_POR_ID(final int id) {
		String consultaSQL = "SELECT * FROM `cuentas` WHERE `id` = " + id + ";";
		try {
			if (Mundo.getCuenta(id) != null) {
				return;
			}
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			if (resultado.next()) {
				Cuenta cuenta = new Cuenta(resultado.getInt("id"), resultado.getString("cuenta"));
				Mundo.addCuenta(cuenta);
				REPLACE_CUENTA_SERVIDOR(cuenta, (byte) 1);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
	}
	
	public static void CARGAR_DB_CUENTAS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `cuentas`;", _bdCuentas);
			while (resultado.next()) {
				Cuenta cuenta = new Cuenta(resultado.getInt("id"), resultado.getString("cuenta"));
				Mundo.addCuenta(cuenta);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static String GET_REGALO(final String cuenta) {
		String str = "";
		String consultaSQL = "SELECT `regalo` FROM `cuentas_servidor` WHERE `cuenta` = '" + cuenta + "' ;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdDinamica);
			if (resultado.next()) {
				try {
					str = resultado.getString("regalo");
				} catch (final Exception e) {}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return str;
	}
	
	public static void CARGAR_CAPTCHAS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `captchas`;", _bdCuentas);
			while (resultado.next()) {
				Mundo.CAPTCHAS.add(resultado.getString("captcha") + "|" + resultado.getString("respuesta"));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {}
	}
	
	public static long GET_ULTIMO_SEGUNDOS_VOTO(final String ip, final int cuentaID) {
		long time = 0;
		String consultaSQL = "SELECT `ultimoVoto` FROM `cuentas` WHERE `ultimaIP` = '" + ip
		+ "' ORDER BY `ultimoVoto` DESC ;";
		try {
			ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			while (resultado.next()) {
				try {
					if (resultado.getString("ultimoVoto").isEmpty()) {
						continue;
					}
					time = resultado.getLong("ultimoVoto");
					break;
				} catch (final Exception e) {}
			}
			cerrarResultado(resultado);
			consultaSQL = "SELECT `ultimoVoto` FROM `cuentas` WHERE `id` = '" + cuentaID + "' ;";
			resultado = consultaSQL(consultaSQL, _bdCuentas);
			while (resultado.next()) {
				if (resultado.getString("ultimoVoto").isEmpty()) {
					continue;
				}
				time = Math.max(time, resultado.getLong("ultimoVoto"));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return time;
	}
	
	public static int GET_VOTOS(final String cuenta) {
		int i = 0;
		String consultaSQL = "SELECT `votos` FROM `cuentas` WHERE `cuenta` = '" + cuenta + "' ;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdCuentas);
			if (resultado.next()) {
				try {
					i = resultado.getInt("votos");
				} catch (final Exception e) {}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return i;
	}
	
	public static void SET_ULTIMO_SEGUNDOS_VOTO(final int cuentaID, final String ip, final long time) {
		final String consultaSQL = "UPDATE `cuentas` SET `ultimoVoto` = ? WHERE `id` = ? OR `ultimaIP` = ? ;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
			declaracion.setLong(1, time);
			declaracion.setInt(2, cuentaID);
			declaracion.setString(3, ip);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void SET_VOTOS(final int cuentaID, final int votos) {
		final String consultaSQL = "UPDATE `cuentas` SET `votos` = ? WHERE `id` = ? ;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdCuentas);
			declaracion.setInt(1, votos);
			declaracion.setInt(2, cuentaID);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void CARGAR_CUENTAS_SERVER_PERSONAJE() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `cuentas_servidor`;", _bdDinamica);
			while (resultado.next()) {
				try {
					Mundo.getCuenta(resultado.getInt("id")).cargarInfoServerPersonaje(resultado.getString("objetos"), resultado
					.getLong("kamas"), resultado.getString("amigos"), resultado.getString("enemigos"), resultado.getString(
					"establo"), resultado.getString("reportes"), resultado.getString("ultimaConexion"), resultado.getString(
					"mensajes"), resultado.getString("ultimaIP"));
				} catch (final Exception e) {}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
	}
	
	public static byte GET_PRIMERA_VEZ(final String cuenta) {
		byte b = 0;
		String consultaSQL = "SELECT `primeraVez` FROM `cuentas_servidor` WHERE `cuenta` = '" + cuenta + "' ;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdDinamica);
			if (resultado.next()) {
				try {
					b = resultado.getByte("primeraVez");
				} catch (final Exception e) {}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return b;
	}
	
	public static void SET_PRIMERA_VEZ_CERO(final String cuenta) {
		final String consultaSQL = "UPDATE `cuentas_servidor` SET `primeraVez` = 0 WHERE `cuenta` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setString(1, cuenta);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void SET_REGALO(final String cuenta, final String regalo) {
		final String consultaSQL = "UPDATE `cuentas_servidor` SET `regalo` = ? WHERE `cuenta` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setString(1, regalo);
			declaracion.setString(2, cuenta);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_MENSAJES_CUENTA(final String cuenta, String mensajes) {
		final String consultaSQL = "UPDATE `cuentas_servidor` SET `mensajes` = ? WHERE `cuenta` = ? ;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setString(1, mensajes);
			declaracion.setString(2, cuenta);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void REPLACE_CUENTA_SERVIDOR(final Cuenta cuenta, final byte primeraVez) {
		final String consultaSQL = "REPLACE INTO `cuentas_servidor` VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, cuenta.getID());
			declaracion.setString(2, cuenta.getNombre());
			declaracion.setLong(3, cuenta.getKamasBanco());
			declaracion.setString(4, cuenta.stringBancoObjetosBD());
			declaracion.setString(5, cuenta.stringIDsEstablo());
			declaracion.setString(6, cuenta.analizarListaAmigosABD());
			declaracion.setString(7, cuenta.stringListaEnemigosABD());
			declaracion.setString(8, cuenta.listaReportes());
			declaracion.setByte(9, primeraVez);
			declaracion.setString(10, cuenta.getRegalo());
			declaracion.setString(11, cuenta.getUltimaConexion());
			declaracion.setString(12, cuenta.getActualIP());
			declaracion.setString(13, cuenta.stringMensajes());
			// cuenta.setUltimaIP(cuenta.getActualIP());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			SALVAR_OBJETOS(cuenta.getObjetosBanco());
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	//
	// public static ArrayList<Personaje> GET_RANKING_NIVEL() {
	// final ArrayList<Personaje> persos = new ArrayList<>();
	// String consultaSQL = "SELECT `id` FROM `personajes` ORDER BY `xp` DESC LIMIT " +
	// Bustemu.LIMITE_LADDER + ";";
	// try {
	// final ResultSet resultado = consultaSQL(consultaSQL, _bdDinamica);
	// while (resultado.next()) {
	// try {
	// persos.add(Mundo.getPersonaje(Integer.parseInt(resultado.getString("id"))));
	// } catch (final Exception e) {}
	// }
	// cerrarResultado(resultado);
	// } catch (final Exception e) {
	// Bustemu.redactarLogServidorln("ERROR SQL: " + e.toString());
	// e.printStackTrace();
	// }
	// return persos;
	// }
	//
	// public static ArrayList<Gremio> GET_RANKING_GREMIOS() {
	// final ArrayList<Gremio> gremios = new ArrayList<>();
	// String consultaSQL = "SELECT `id` FROM `gremios` ORDER BY `xp` DESC LIMIT " +
	// Bustemu.LIMITE_LADDER + ";";
	// try {
	// final ResultSet resultado = consultaSQL(consultaSQL, _bdDinamica);
	// while (resultado.next()) {
	// try {
	// gremios.add(Mundo.getGremio(Integer.parseInt(resultado.getString("id"))));
	// } catch (final Exception e) {}
	// }
	// cerrarResultado(resultado);
	// } catch (final Exception e) {
	// Bustemu.redactarLogServidorln("ERROR SQL: " + e.toString());
	// e.printStackTrace();
	// }
	// return gremios;
	// }
	
	
	
	
	
	
	public static ArrayList<TiendaObjetos> loadObjetosTienda() {
		final ArrayList<TiendaObjetos> gremios = new ArrayList<>();
		String consultaSQL = "SELECT `id` FROM `tiendaobjetos` ORDER BY `id` ASC LIMIT 200;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdDinamica);
			while (resultado.next()) {
				try {
					gremios.add(Mundo.getTiendaObjetos2(Integer.parseInt(resultado.getString("id"))));
				} catch (final Exception e) {
				}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return gremios;
	}
	
	
	public static ArrayList<TiendaCategoria> loadCategorias() {
		final ArrayList<TiendaCategoria> gremios = new ArrayList<>();
		String consultaSQL = "SELECT `id` FROM `tiendacategoria` ORDER BY `id` ASC LIMIT 200;";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdDinamica);
			while (resultado.next()) {
				try {
					gremios.add(Mundo.getTiendaCategoria2(Integer.parseInt(resultado.getString("id"))));
				} catch (final Exception e) {
				}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("ERROR SQL: " + e.toString());
			e.printStackTrace();
		}
		return gremios;
	}

	
	
	
	
	
	
	
	
	
	
	
	//
	// public static ArrayList<Personaje> GET_RANKING_PVP() {
	// final ArrayList<Personaje> persos = new ArrayList<>();
	// String consultaSQL =
	// "SELECT `id` FROM `ranking_pvp` ORDER BY `victorias` DESC, `derrotas`ASC LIMIT "
	// + Bustemu.LIMITE_LADDER + ";";
	// try {
	// final ResultSet resultado = consultaSQL(consultaSQL, _bdDinamica);
	// while (resultado.next()) {
	// try {
	// persos.add(Mundo.getPersonaje(resultado.getInt("id")));
	// } catch (final Exception e) {}
	// }
	// cerrarResultado(resultado);
	// } catch (final Exception e) {
	// Bustemu.redactarLogServidorln("ERROR SQL: " + e.toString());
	// e.printStackTrace();
	// }
	// return persos;
	// }
	//
	// public static ArrayList<Personaje> GET_RANKING_KOLISEO() {
	// final ArrayList<Personaje> persos = new ArrayList<>();
	// String consultaSQL =
	// "SELECT `id` FROM `ranking_koliseo` ORDER BY `victorias` DESC, `derrotas`ASC LIMIT "
	// + Bustemu.LIMITE_LADDER + ";";
	// try {
	// final ResultSet resultado = consultaSQL(consultaSQL, _bdDinamica);
	// while (resultado.next()) {
	// try {
	// persos.add(Mundo.getPersonaje(resultado.getInt("id")));
	// } catch (final Exception e) {}
	// }
	// cerrarResultado(resultado);
	// } catch (final Exception e) {
	// Bustemu.redactarLogServidorln("ERROR SQL: " + e.toString());
	// e.printStackTrace();
	// }
	// return persos;
	// }
	//
	public static int GET_SIG_ID_OBJETO() {
		int id = 1;
		try {
			final ResultSet resultado = consultaSQL("SELECT MAX(id) AS max FROM `objetos`;", _bdDinamica);
			if (resultado.next()) {
				id = resultado.getInt("max");
			}
			cerrarResultado(resultado);
			return id;
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return id;
	}
	
	public static void CARGAR_RECETAS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `recetas`;", _bdEstatica);
			while (resultado.next()) {
				final ArrayList<Duo<Integer, Integer>> arrayDuos = new ArrayList<Duo<Integer, Integer>>();
				boolean continua = false;
				int idReceta = resultado.getInt("id");
				String receta = resultado.getString("receta");
				for (String str : receta.split(";")) {
					try {
						final String[] s = str.split(Pattern.quote(","));
						final int idModeloObj = Integer.parseInt(s[0]);
						final int cantidad = Integer.parseInt(s[1]);
						arrayDuos.add(new Duo<Integer, Integer>(idModeloObj, cantidad));
						continua = true;
					} catch (final Exception e) {
						continua = false;
					}
				}
				if (continua) {
					Mundo.addReceta(idReceta, arrayDuos);
				}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static int CARGAR_DROPS() {
		int numero = 0;
		try {
			if (!MainServidor.PARAM_SISTEMA_ORBES) {
				final ResultSet resultado = consultaSQL("SELECT * FROM `drops`;", _bdEstatica);
				while (resultado.next()) {
					if (Mundo.getObjetoModelo(resultado.getInt("objeto")) == null) {
						continue;
					}
					DropMob drop = new DropMob(resultado.getInt("objeto"), resultado.getInt("prospeccion"), resultado.getFloat(
					"porcentaje"), resultado.getInt("max"), resultado.getString("condicion"));
					Mundo.getMobModelo(resultado.getInt("mob")).addDrop(drop);
					numero++;
				}
				cerrarResultado(resultado);
			}
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
		return numero;
	}
	
	public static int CARGAR_DROPS_FIJOS() {
		int numero = 0;
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `drops_fijos`;", _bdEstatica);
			while (resultado.next()) {
				if (Mundo.getObjetoModelo(resultado.getInt("objeto")) == null) {
					continue;
				}
				Mundo.addDropFijo(new DropMob(resultado.getInt("objeto"), resultado.getFloat("porcentaje"), resultado.getInt(
				"nivelMin"), resultado.getInt("nivelMax")));
				numero++;
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
		return numero;
	}
	
	public static void SELECT_ZONAS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM zonas;", _bdEstatica);
			while (resultado.next()) {
				Mundo.ZONAS.put(resultado.getShort("mapa"), resultado.getShort("celda"));
				Mundo.LISTA_ZONAS += "|" + resultado.getString("nombre") + ";" + resultado.getShort("mapa");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_OBJETOS_SETS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM objetos_set;", _bdEstatica);
			while (resultado.next()) {
				ObjetoSet set = new ObjetoSet(resultado.getInt("id"), resultado.getString("nombre"), resultado.getString(
				"objetos"));
				for (int i = 2; i <= 8; i++) {
					set.setStats(resultado.getString(i + "_objetos"), i);
				}
				Mundo.addObjetoSet(set);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_CERCADOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `cercados_modelo`;", _bdEstatica);
			while (resultado.next()) {
				final Mapa mapa = Mundo.getMapa(resultado.getShort("mapa"));
				if (mapa == null) {
					continue;
				}
				final Cercado cercado = new Cercado(mapa, resultado.getByte("capacidad"), resultado.getByte("objetos"),
				resultado.getShort("celdaPJ"), resultado.getShort("celdaPuerta"), resultado.getShort("celdaMontura"), resultado
				.getString("celdasObjetos"), resultado.getInt("precioOriginal"));
				Mundo.addCercado(cercado);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_OFICIOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `oficios`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addOficio(new Oficio(resultado.getInt("id"), resultado.getString("herramientas"), resultado.getString(
				"recetas")));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_SERVICIOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `servicios`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addServicio(new Servicio(resultado.getInt("id"), resultado.getInt("creditos"), resultado.getInt(
				"ogrinas"), resultado.getBoolean("activado"), resultado.getInt("creditosVIP"), resultado.getInt("ogrinasVIP")));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_ENCARNACIONES_MODELOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `encarnaciones_modelo`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addEncarnacionModelo(new EncarnacionModelo(resultado.getInt("gfx"), resultado.getString("statsFijos"),
				resultado.getString("statsPorNivel"), resultado.getString("hechizos")));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_CLASES() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `clases`;", _bdEstatica);
			while (resultado.next()) {
				Clase clase = new Clase(resultado.getInt("id"), resultado.getString("gfxs"), resultado.getString("tallas"),
				resultado.getShort("mapaInicio"), resultado.getShort("celdaInicio"), resultado.getInt("PDV"), resultado
				.getString("boostVitalidad"), resultado.getString("boostSabiduria"), resultado.getString("boostFuerza"),
				resultado.getString("boostInteligencia"), resultado.getString("boostAgilidad"), resultado.getString(
				"boostSuerte"), resultado.getString("statsInicio"), resultado.getString("hechizos"));
				Mundo.CLASES.put(clase.getID(), clase);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_CREA_OBJETOS_MODELOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `crear_objetos_modelos`;", _bdEstatica);
			while (resultado.next()) {
				CreaTuItem creaTuItem = new CreaTuItem(resultado.getInt("id"), resultado.getString("statsMaximos"), resultado
				.getInt("limiteOgrinas"), resultado.getInt("precioBase"));
				Mundo.CREA_TU_ITEM.put(creaTuItem.getID(), creaTuItem);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_CREA_OBJETOS_PRECIOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `crear_objetos_stats`;", _bdEstatica);
			while (resultado.next()) {
				if (!Mundo.CREAT_TU_ITEM_PRECIOS.isEmpty()) {
					Mundo.CREAT_TU_ITEM_PRECIOS += ";";
				}
				int stat = resultado.getInt("id");
				float ogrinas = resultado.getFloat("ogrinas");
				Mundo.CREAT_TU_ITEM_PRECIOS += stat + "," + ogrinas;
				CreaTuItem.PRECIOS.put(stat, ogrinas);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_AREA() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `areas`;", _bdEstatica);
			while (resultado.next()) {
				final Area area = new Area(resultado.getShort("id"), resultado.getShort("superarea"), resultado.getString(
				"nombre"));
				Mundo.addArea(area);
				area.getSuperArea().addArea(area);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_SUBAREA() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `subareas`;", _bdEstatica);
			while (resultado.next()) {
				final SubArea subarea = new SubArea(resultado.getShort("id"), resultado.getShort("area"), resultado.getString(
				"nombre"), resultado.getInt("conquistable") == 1, resultado.getInt("minNivelGrupoMob"), resultado.getInt(
				"maxNivelGrupoMob"), resultado.getString("cementerio"));
				Mundo.addSubArea(subarea);
				if (subarea.getArea() != null) {
					subarea.getArea().addSubArea(subarea);
				}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static int CARGAR_NPCS() {
		int numero = 0;
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `npcs_ubicacion`;", _bdEstatica);
			while (resultado.next()) {
				try {
					final NPCModelo npcModelo = Mundo.getNPCModelo(resultado.getInt("npc"));
					if (npcModelo == null) {
						DELETE_NPC_UBICACION(resultado.getInt("npc"));
						continue;
					}
					Mundo.getMapa(resultado.getShort("mapa")).addNPC(npcModelo, resultado.getShort("celda"), resultado.getByte(
					"orientacion"));
					numero++;
				} catch (final Exception e) {}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
		return numero;
	}
	
	public static void CARGAR_CASAS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `casas_modelo`;", _bdEstatica);
			while (resultado.next()) {
				if (Mundo.getMapa(resultado.getShort("mapaFuera")) == null) {
					continue;
				}
				Mundo.addCasa(new Casa(resultado.getInt("id"), resultado.getShort("mapaFuera"), resultado.getShort(
				"celdaFuera"), resultado.getShort("mapaDentro"), resultado.getShort("celdaDentro"), resultado.getLong("precio"),
				resultado.getString("mapasContenidos")));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void RECARGAR_CASAS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `casas`;", _bdDinamica);
			while (resultado.next()) {
				try {
					Mundo.getCasa(resultado.getInt("id")).actualizarCasa(resultado.getInt("dueño"), resultado.getInt("precio"),
					resultado.getByte("bloqueado"), resultado.getString("clave"), resultado.getInt("derechosGremio"));
				} catch (Exception e) {}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
	}
	
	public static void CARGAR_COFRES() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `cofres_modelo`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addCofre(new Cofre(resultado.getInt("id"), resultado.getInt("casa"), resultado.getShort("mapa"), resultado
				.getShort("celda"), MainServidor.LIMITE_OBJETOS_COFRE));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_EXPERIENCIA() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `experiencia`;", _bdEstatica);
			int maxAlineacion = 0;
			int maxPersonaje = 0;
			int maxGremio = 0;
			int maxOficio = 0;
			int maxMontura = 0;
			int maxEncarnacion = 0;
			while (resultado.next()) {
				int nivel = resultado.getInt("nivel");
				Experiencia exp = new Experiencia(resultado.getLong("personaje"), resultado.getInt("oficio"), resultado.getInt(
				"dragopavo"), resultado.getLong("gremio"), resultado.getInt("pvp"), resultado.getInt("encarnacion"));
				if (exp._alineacion > 0) {
					maxAlineacion = Math.max(maxAlineacion, nivel);
				}
				if (exp._personaje > 0) {
					maxPersonaje = Math.max(maxPersonaje, nivel);
				}
				if (exp._gremio > 0) {
					maxGremio = Math.max(maxGremio, nivel);
				}
				if (exp._oficio > 0) {
					maxOficio = Math.max(maxOficio, nivel);
				}
				if (exp._montura > 0) {
					maxMontura = Math.max(maxMontura, nivel);
				}
				if (exp._encarnacion > 0) {
					maxEncarnacion = Math.max(maxEncarnacion, nivel);
				}
				Mundo.addExpNivel(nivel, exp);
			}
			if (MainServidor.NIVEL_MAX_PERSONAJE <= 0) {
				MainServidor.NIVEL_MAX_PERSONAJE = maxPersonaje;
			}
			if (MainServidor.NIVEL_MAX_ALINEACION <= 0) {
				MainServidor.NIVEL_MAX_ALINEACION = maxAlineacion;
			}
			if (MainServidor.NIVEL_MAX_GREMIO <= 0) {
				MainServidor.NIVEL_MAX_GREMIO = maxGremio;
			}
			if (MainServidor.NIVEL_MAX_OFICIO <= 0) {
				MainServidor.NIVEL_MAX_OFICIO = maxOficio;
			}
			if (MainServidor.NIVEL_MAX_MONTURA <= 0) {
				MainServidor.NIVEL_MAX_MONTURA = maxMontura;
			}
			if (MainServidor.NIVEL_MAX_ENCARNACION <= 0) {
				MainServidor.NIVEL_MAX_ENCARNACION = maxEncarnacion;
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static int CARGAR_TRIGGERS() {
		int numero = 0;
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `celdas_accion`;", _bdEstatica);
			while (resultado.next()) {
				final Mapa mapa = Mundo.getMapa(resultado.getShort("mapa"));
				if (mapa == null || mapa.getCelda(resultado.getShort("celda")) == null) {
					continue;
				}
				mapa.getCelda(resultado.getShort("celda")).addAccion(resultado.getInt("accion"), resultado.getString("args"),
				resultado.getString("condicion"));
				numero++;
			}
			cerrarResultado(resultado);
			return numero;
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
		return numero;
	}
	
	public static void CARGAR_MOBS_EVENTO() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `mobs_evento`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addMobEvento(resultado.getByte("evento"), resultado.getInt("mobOriginal"), resultado.getInt("mobEvento"));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_PERSONAJES() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `personajes`;", _bdDinamica);
			while (resultado.next()) {
				final TreeMap<Integer, Integer> statsBase = new TreeMap<Integer, Integer>();
				statsBase.put(Constantes.STAT_MAS_VITALIDAD, resultado.getInt("vitalidad"));
				statsBase.put(Constantes.STAT_MAS_FUERZA, resultado.getInt("fuerza"));
				statsBase.put(Constantes.STAT_MAS_SABIDURIA, resultado.getInt("sabiduria"));
				statsBase.put(Constantes.STAT_MAS_INTELIGENCIA, resultado.getInt("inteligencia"));
				statsBase.put(Constantes.STAT_MAS_SUERTE, resultado.getInt("suerte"));
				statsBase.put(Constantes.STAT_MAS_AGILIDAD, resultado.getInt("agilidad"));
				final TreeMap<Integer, Integer> statsScroll = new TreeMap<Integer, Integer>();
				statsScroll.put(Constantes.STAT_MAS_VITALIDAD, resultado.getInt("sVitalidad"));
				statsScroll.put(Constantes.STAT_MAS_FUERZA, resultado.getInt("sFuerza"));
				statsScroll.put(Constantes.STAT_MAS_SABIDURIA, resultado.getInt("sSabiduria"));
				statsScroll.put(Constantes.STAT_MAS_INTELIGENCIA, resultado.getInt("sInteligencia"));
				statsScroll.put(Constantes.STAT_MAS_SUERTE, resultado.getInt("sSuerte"));
				statsScroll.put(Constantes.STAT_MAS_AGILIDAD, resultado.getInt("sAgilidad"));
				final Personaje perso = new Personaje(resultado.getInt("id"), resultado.getString("nombre"), resultado.getByte(
				"sexo"), resultado.getByte("clase"), resultado.getInt("color1"), resultado.getInt("color2"), resultado.getInt(
				"color3"), resultado.getLong("kamas"), resultado.getInt("puntosHechizo"), resultado.getInt("capital"), resultado
				.getInt("energia"), resultado.getShort("nivel"), resultado.getLong("xp"), resultado.getInt("talla"), resultado
				.getInt("gfx"), resultado.getByte("alineacion"), resultado.getInt("cuenta"), statsBase, statsScroll, resultado
				.getInt("mostrarAmigos") == 1, resultado.getByte("mostrarAlineacion") == 1, resultado.getString("canal"),
				resultado.getShort("mapa"), resultado.getShort("celda"), resultado.getString("objetos"), resultado.getByte(
				"porcVida"), resultado.getString("hechizos"), resultado.getString("posSalvada"), resultado.getString("oficios"),
				resultado.getByte("xpMontura"), resultado.getInt("montura"), resultado.getInt("honor"), resultado.getInt(
				"deshonor"), resultado.getByte("nivelAlin"), resultado.getString("zaaps"), resultado.getInt("esposo"), resultado
				.getString("tienda"), resultado.getInt("mercante") == 1, resultado.getInt("restriccionesA"), resultado.getInt(
				"restriccionesB"), resultado.getInt("encarnacion"), resultado.getInt("emotes"), resultado.getString("titulos"),
				resultado.getString("tituloVIP"), resultado.getString("ornamentos"), resultado.getString("misiones"), resultado
				.getString("coleccion"), resultado.getByte("resets"), resultado.getString("almanax"), resultado.getInt(
				"ultimoNivel"), resultado.getString("setsRapidos"), resultado.getInt("colorNombre"), resultado.getString(
				"orden"));
				if (perso.getCuenta() != null) {
					Mundo.addPersonaje(perso);
				}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_PRISMAS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `prismas`;", _bdDinamica);
			while (resultado.next()) {
				if (Mundo.getMapa(resultado.getShort("mapa")) == null) {
					continue;
				}
				Prisma prisma = new Prisma(resultado.getInt("id"), resultado.getByte("alineacion"), resultado.getByte("nivel"),
				resultado.getShort("mapa"), resultado.getShort("celda"), resultado.getInt("honor"), resultado.getShort("area"),
				resultado.getShort("subArea"), resultado.getLong("tiempoProteccion"));
				Mundo.addPrisma(prisma);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static int SELECT_OBJETOS_MERCADILLO() {
		int num = 0;
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `mercadillo_objetos`;", _bdDinamica);
			while (resultado.next()) {
				final Mercadillo puesto = Mundo.getPuestoMercadillo(resultado.getInt("mercadillo"));
				Objeto objeto = Mundo.getObjeto(resultado.getInt("objeto"));
				if (puesto == null || objeto == null || objeto.getDueñoTemp() != 0) {
					MainServidor.redactarLogServidorln("Se borro el objeto mercadillo id:" + resultado.getInt("objeto")
					+ ", dueño: " + resultado.getInt("dueño"));
					DELETE_OBJ_MERCADILLO(resultado.getInt("objeto"));
					continue;
				}
				puesto.addObjMercaAlPuesto(new ObjetoMercadillo(resultado.getInt("precio"), resultado.getByte("cantidad"),
				resultado.getInt("dueño"), objeto, puesto.getID()));
				num++;
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
		return num;
	}
	
	public static int CARGAR_RECAUDADORES() {
		int numero = 0;
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `recaudadores`;", _bdDinamica);
			while (resultado.next()) {
				final Mapa mapa = Mundo.getMapa(resultado.getShort("mapa"));
				if (mapa == null) {
					continue;
				}
				final Recaudador recaudador = new Recaudador(resultado.getInt("id"), resultado.getShort("mapa"), resultado
				.getShort("celda"), resultado.getByte("orientacion"), resultado.getInt("gremio"), resultado.getString(
				"nombre1"), resultado.getString("nombre2"), resultado.getString("objetos"), resultado.getLong("kamas"),
				resultado.getLong("xp"), resultado.getLong("tiempoProteccion"), resultado.getLong("tiempoCreacion"), resultado
				.getInt("dueño"));
				Mundo.addRecaudador(recaudador);
				numero++;
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
		return numero;
	}
	
	public static void CARGAR_GREMIOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM gremios;", _bdDinamica);
			while (resultado.next()) {
				Mundo.addGremio(new Gremio(resultado.getInt("id"), resultado.getString("nombre"), resultado.getString(
				"emblema"), resultado.getShort("nivel"), resultado.getLong("xp"), resultado.getShort("capital"), resultado
				.getByte("recaudadores"), resultado.getString("hechizos"), resultado.getString("stats")));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	private static Mapa RESULSET_MAP(ResultSet resultado) throws SQLException {
		short mapaID = resultado.getShort("id");
		if (MainServidor.MODO_DEBUG) {
			System.out.println("Cargando mapa ID " + mapaID);
		}
		return new Mapa(mapaID, resultado.getString("fecha"), resultado.getByte("ancho"), resultado.getByte("alto"),
		resultado.getString("posPelea"), resultado.getString("mapData"), resultado.getString("key"), resultado.getString(
		"mobs"), resultado.getShort("X"), resultado.getShort("Y"), resultado.getShort("subArea"), resultado.getByte(
		"maxGrupoMobs"), resultado.getByte("maxMobsPorGrupo"), resultado.getByte("maxMercantes"), resultado.getShort(
		"capabilities"), resultado.getByte("maxPeleas"), resultado.getShort("bgID"), resultado.getShort("musicID"),
		resultado.getShort("ambienteID"), resultado.getByte("outDoor"), resultado.getInt("minNivelGrupoMob"), resultado
		.getInt("maxNivelGrupoMob"));
	}
	
	public static boolean CLONAR_MAPA(Mapa mapaClonar, int nuevaID, String nuevaFecha, int nuevaX, int nuevaY,
	int nuevaSubArea) {
		String consultaSQL = "REPLACE INTO `mapas` VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		try {
			int i = 1;
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(i++, nuevaID);
			declaracion.setString(i++, nuevaFecha);
			declaracion.setInt(i++, mapaClonar.getAncho());
			declaracion.setInt(i++, mapaClonar.getAlto());
			declaracion.setInt(i++, mapaClonar.getBgID());
			declaracion.setInt(i++, mapaClonar.getAmbienteID());
			declaracion.setInt(i++, mapaClonar.getMusicID());
			declaracion.setInt(i++, mapaClonar.getOutDoor());
			declaracion.setInt(i++, mapaClonar.getCapabilities());
			declaracion.setString(i++, mapaClonar.strCeldasPelea());
			declaracion.setString(i++, mapaClonar.getKey());
			declaracion.setString(i++, mapaClonar.getMapData());
			declaracion.setString(i++, "");
			declaracion.setInt(i++, nuevaX);
			declaracion.setInt(i++, nuevaY);
			declaracion.setInt(i++, nuevaSubArea);
			declaracion.setInt(i++, mapaClonar.getMaxGrupoDeMobs());
			declaracion.setInt(i++, mapaClonar.getMaxMobsPorGrupo());
			declaracion.setInt(i++, mapaClonar.getMinNivelGrupoMob());
			declaracion.setInt(i++, mapaClonar.getMaxNivelGrupoMob());
			declaracion.setInt(i++, mapaClonar.getMaxMercantes());
			declaracion.setInt(i++, mapaClonar.getMaxNumeroPeleas());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			consultaSQL = "SELECT * FROM `mapas` WHERE `id` = " + nuevaID + ";";
			ResultSet resultado = consultaSQL(consultaSQL, _bdEstatica);
			while (resultado.next()) {
				if (Mundo.mapaExiste(resultado.getShort("id"))) {
					continue;
				}
				Mapa mapa = RESULSET_MAP(resultado);
				Mundo.addMapa(mapa);
				return true;
			}
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static void CARGAR_MAPAS() {
		String consultaSQL = "SELECT * FROM `mapas` LIMIT " + MainServidor.LIMITE_MAPAS + ";";
		try {
			if (MainServidor.MODO_MAPAS_LIMITE) {
				consultaSQL = "SELECT * FROM `mapas` WHERE `subArea` IN (" + MainServidor.STR_SUBAREAS_LIMITE + ") OR `id` IN ("
				+ MainServidor.STR_MAPAS_LIMITE + ");";
			}
			ResultSet resultado = consultaSQL(consultaSQL, _bdEstatica);
			Mapa mapa;
			// 256 MB = 1500 MAPAS
			// 1 GB = 6000 MAPAS
			while (resultado.next()) {
				if (Mundo.mapaExiste(resultado.getShort("id"))) {
					continue;
				}
				mapa = RESULSET_MAP(resultado);
				Mundo.addMapa(mapa);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_MAPAS_IDS(final String ids) {
		if (ids.isEmpty()) {
			return;
		}
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `mapas` WHERE `id` IN (" + ids + ") ;", _bdEstatica);
			Mapa mapa;
			while (resultado.next()) {
				if (Mundo.mapaExiste(resultado.getShort("id"))) {
					continue;
				}
				mapa = RESULSET_MAP(resultado);
				Mundo.addMapa(mapa);
				CARGAR_TRIGGERS_POR_MAPA(mapa.getID());
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_MAPAS_SUBAREAS(final String subAreas) {
		if (subAreas.isEmpty()) {
			return;
		}
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `mapas` WHERE `subArea` IN (" + subAreas + ") ;",
			_bdEstatica);
			Mapa mapa;
			while (resultado.next()) {
				if (Mundo.mapaExiste(resultado.getShort("id"))) {
					continue;
				}
				mapa = RESULSET_MAP(resultado);
				Mundo.addMapa(mapa);
				CARGAR_TRIGGERS_POR_MAPA(mapa.getID());
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_TRIGGERS_POR_MAPA(final short id) {
		String consultaSQL = "SELECT * FROM `celdas_accion` WHERE `mapa` = '" + id + "';";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdEstatica);
			while (resultado.next()) {
				final Mapa mapa = Mundo.getMapa(resultado.getShort("mapa"));
				if (mapa == null || mapa.getCelda(resultado.getShort("celda")) == null) {
					continue;
				}
				mapa.getCelda(resultado.getShort("celda")).addAccion(resultado.getInt("accion"), resultado.getString("args"),
				resultado.getString("condicion"));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
	}
	
	public static int CARGAR_MOBS_FIJOS() {
		int numero = 0;
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `mobs_fix`;", _bdEstatica);
			while (resultado.next()) {
				ArrayList<Mapa> mapas = new ArrayList<>();
				for (String m : resultado.getString("mapa").split(",")) {
					if (m.isEmpty()) {
						continue;
					}
					try {
						Mapa mapa = Mundo.getMapa(Short.parseShort(m));
						if (m != null) {
							mapas.add(mapa);
						}
					} catch (Exception e) {}
				}
				if (mapas.isEmpty()) {
					continue;
				}
				final Mapa mapa = mapas.get(0);
				if (mapa == null) {
					MainServidor.redactarLogServidorln("EL MAPA " + resultado.getShort("mapa") + " NO EXISTE");
					continue;
				}
				if (mapa.getCelda(resultado.getShort("celda")) == null) {
					MainServidor.redactarLogServidorln("LA CELDA " + resultado.getShort("celda") + " DEL MAPA " + resultado
					.getShort("mapa") + " NO EXISTE");
					continue;
				}
				TipoGrupo tipoGrupo = Constantes.getTipoGrupoMob(resultado.getInt("tipo"));
				if (tipoGrupo == TipoGrupo.NORMAL) {
					tipoGrupo = TipoGrupo.FIJO;
				}
				GrupoMob grupoMob = mapa.addGrupoMobPorTipo(resultado.getShort("celda"), resultado.getString("mobs"), tipoGrupo,
				resultado.getString("condicion"), mapas);
				if (grupoMob != null) {
					ArrayList<Short> s1 = Mundo.getMapaEstrellas(mapa.getID());
					ArrayList<String> s2 = Mundo.getMapaHeroico(mapa.getID());
					short estrellas = s1 == null ? -1 : (s1.isEmpty() ? -1 : s1.get(0));
					String heroico = s2 == null ? "" : (s2.isEmpty() ? "" : s2.get(0));
					if (estrellas > -1) {
						grupoMob.setBonusEstrellas(estrellas);
					}
					if (!heroico.isEmpty()) {
						grupoMob.addObjetosKamasInicioServer(heroico);
					}
					if (s1 != null && !s1.isEmpty()) {
						s1.remove(0);
					}
					if (s2 != null && !s2.isEmpty()) {
						s2.remove(0);
					}
					grupoMob.setSegundosRespawn(resultado.getInt("segundosRespawn"));
					numero++;
				} else {
					MainServidor.redactarLogServidorln("NO SE PUDO AGREGAR EL GRUPOMOB FIJO " + resultado.getString("mobs")
					+ " EN EL MAPA " + resultado.getShort("mapa") + ", CELDA " + resultado.getShort("celda"));
				}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
		return numero;
	}
	
	public static void SELECT_ANIMACIONES() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `animaciones`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addAnimacion(new Animacion(resultado.getInt("id"), resultado.getInt("hechizoAnimacion"), resultado.getInt(
				"tipoDisplay"), resultado.getInt("spriteAnimacion"), resultado.getInt("level"), resultado.getInt("duracion"),
				resultado.getInt("talla")));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_COMANDOS_MODELO() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `comandos_modelo`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addComando(resultado.getString("comando"), resultado.getInt("rango"));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_OTROS_INTERACTIVOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `otros_interactivos`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addOtroInteractivo(new OtroInteractivo(resultado.getInt("gfx"), resultado.getShort("mapaID"), resultado
				.getShort("celdaID"), resultado.getInt("accion"), resultado.getString("args"), resultado.getString("condicion"),
				resultado.getInt("tiempoRecarga")));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static int CARGAR_COMIDAS_MASCOTAS() {
		int numero = 0;
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `mascotas_modelo`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addMascotaModelo(new MascotaModelo(resultado.getInt("mascota"), resultado.getInt("maximoComidas"),
				resultado.getString("statsPorEfecto"), resultado.getString("comidas"), resultado.getInt("devorador"), resultado
				.getInt("fantasma")));
				numero++;
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
		return numero;
	}
	
	public static void CARGAR_HECHIZOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `hechizos`;", _bdEstatica);
			while (resultado.next()) {
				final int id = resultado.getInt("id");
				final Hechizo hechizo = new Hechizo(id, resultado.getString("nombre"), resultado.getInt("sprite"), resultado
				.getString("spriteInfos"), resultado.getInt("valorIA"));
				Mundo.addHechizo(hechizo);
				for (int i = 1; i <= 6; i++) {
					StatHechizo sh = null;
					String txt = resultado.getString("nivel" + i);
					if (!txt.isEmpty()) {
						try {
							sh = Hechizo.analizarHechizoStats(id, i, txt);
						} catch (Exception e) {
							MainServidor.redactarLogServidorln("BUG HECHIZO: " + id + " NIVEL " + i);
							exceptionExit(e, "");
						}
					}
					hechizo.addStatsHechizos(i, sh);
				}
				hechizo.setAfectados(resultado.getString("afectados"));
				hechizo.setCondiciones(resultado.getString("condiciones"));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_ESPECIALIDADES() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `especialidades`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addEspecialidad(new Especialidad(resultado.getInt("id"), resultado.getInt("orden"), resultado.getInt(
				"nivel"), resultado.getString("dones")));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_DONES_MODELOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `dones`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addDonModelo(resultado.getInt("id"), resultado.getInt("stat"));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_OBJETOS_MODELOS() {
		try {
			int maxID = 0;
			final ResultSet resultado = consultaSQL("SELECT * FROM `objetos_modelo`;", _bdEstatica);
			while (resultado.next()) {
				if (resultado.getInt("id") > MainServidor.MAX_ID_OBJETO_MODELO) {
					continue;
				}
				ObjetoModelo obj = new ObjetoModelo(resultado.getInt("id"), resultado.getString("statsModelo"), resultado
				.getString("nombre"), resultado.getShort("tipo"), resultado.getShort("nivel"), resultado.getShort("pods"),
				resultado.getInt("kamas"), resultado.getString("condicion"), resultado.getString("infosArma"), resultado.getInt(
				"vendidos"), resultado.getInt("precioMedio"), resultado.getInt("ogrinas"), resultado.getBoolean("magueable"),
				resultado.getShort("gfx"), resultado.getBoolean("nivelCore"), resultado.getBoolean("etereo"), resultado.getInt(
				"diasIntercambio"), resultado.getInt("panelOgrinas"), resultado.getInt("panelKamas"), resultado.getString(
				"itemPago"));
				Mundo.addObjModelo(obj);
				maxID = Math.max(maxID, obj.getID());
			}
			MainServidor.MAX_ID_OBJETO_MODELO = Math.min(maxID, MainServidor.MAX_ID_OBJETO_MODELO);
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_MONTURAS_MODELOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `monturas_modelo`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addMonturaModelo(new MonturaModelo(resultado.getInt("id"), resultado.getString("stats"), resultado
				.getString("color"), resultado.getInt("certificado"), resultado.getByte("generacion")));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_MOBS_MODELOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `mobs_modelo`;", _bdEstatica);
			while (resultado.next()) {
				final boolean capturable = resultado.getInt("capturable") == 1;
				final boolean esKickeable = resultado.getString("kickeable").equals("true");
				final byte alineacion = resultado.getByte("alineacion");
				final byte tipoIA = resultado.getByte("tipoIA");
				final byte tipo = resultado.getByte("tipo");
				final short talla = resultado.getShort("talla");
				final byte distAgresion = resultado.getByte("agresion");
				final int id = resultado.getInt("id");
				final short gfxID = resultado.getShort("gfxID");
				final String mK = resultado.getString("minKamas");
				final String MK = resultado.getString("maxKamas");
				final String nombre = resultado.getString("nombre");
				final String colores = resultado.getString("colores");
				final String grados = resultado.getString("grados").replaceAll(" ", "").replaceAll(",g", "g").replaceAll(
				":\\{l:", "@").replaceAll(",r:\\[", ",").replaceAll("\\]", "|").replaceAll("\\]\\}", "|");
				// g1: {l: 1, r: [25, 0, -12, 6, -50, 15, 15], lp: 30, ap: 5, mp: 2}, g2: {l: 2
				final String hechizos = resultado.getString("hechizos");
				final String stats = resultado.getString("stats");
				final String pdvs = resultado.getString("pdvs");
				final String puntos = resultado.getString("puntos");
				final String iniciativa = resultado.getString("iniciativa");
				final String xp = resultado.getString("exps");
				Mundo.addMobModelo(new MobModelo(id, nombre, gfxID, alineacion, colores, grados, hechizos, stats, pdvs, puntos,
				iniciativa, mK, MK, xp, tipoIA, capturable, talla, distAgresion, tipo, esKickeable));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_MOBS_RAROS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `mobs_raros`;", _bdEstatica);
			while (resultado.next()) {
				final int idMobRaro = resultado.getInt("idMobRaro");
				final int idMobNormal = resultado.getInt("idMobNormal");
				final String subAreas = resultado.getString("subAreas");
				final int probabilidad = resultado.getInt("probabilidad");
				MobModelo mobM = Mundo.getMobModelo(idMobRaro);
				if (mobM == null) {
					continue;
				}
				MobModelo mobN = Mundo.getMobModelo(idMobNormal);
				if (mobN != null) {
					mobN.setArchiMob(mobM);
				}
				mobM.setDataExtra(probabilidad, subAreas);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static int CARGAR_MIEMBROS_GREMIO() {
		int numero = 0;
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM miembros_gremio;", _bdDinamica);
			while (resultado.next()) {
				if (Mundo.getPersonaje(resultado.getInt("id")) == null) {
					DELETE_MIEMBRO_GREMIO(resultado.getInt("id"));
					continue;
				}
				Gremio gremio = Mundo.getGremio(resultado.getInt("gremio"));
				if (gremio == null) {
					continue;
				}
				gremio.addMiembro(resultado.getInt("id"), resultado.getInt("rango"), resultado.getLong("xpDonada"), resultado
				.getByte("porcXp"), resultado.getInt("derechos"));
				numero++;
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
		return numero;
	}
	
	public static void CARGAR_MONTURAS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `monturas`;", _bdDinamica);
			while (resultado.next()) {
				Mundo.addMontura(new Montura(resultado.getInt("id"), resultado.getInt("color"), resultado.getByte("sexo"),
				resultado.getInt("amor"), resultado.getInt("resistencia"), resultado.getInt("nivel"), resultado.getLong("xp"),
				resultado.getString("nombre"), resultado.getInt("fatiga"), resultado.getInt("energia"), resultado.getByte(
				"reproducciones"), resultado.getInt("madurez"), resultado.getInt("serenidad"), resultado.getString("objetos"),
				resultado.getString("ancestros"), resultado.getString("habilidad"), resultado.getByte("talla"), resultado
				.getShort("celda"), resultado.getShort("mapa"), resultado.getInt("dueño"), resultado.getByte("orientacion"),
				resultado.getLong("fecundable"), resultado.getInt("pareja"), resultado.getByte("salvaje")), false);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_NPC_MODELOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `npcs_modelo`;", _bdEstatica);
			while (resultado.next()) {
				final int id = resultado.getInt("id");
				final int gfxID = resultado.getInt("gfxID");
				final short escalaX = resultado.getShort("scaleX");
				final short escalaY = resultado.getShort("scaleY");
				final byte sexo = resultado.getByte("sexo");
				final int color1 = resultado.getInt("color1");
				final int color2 = resultado.getInt("color2");
				final int color3 = resultado.getInt("color3");
				final int foto = resultado.getInt("foto");
				final int preguntaID = resultado.getInt("pregunta");
				final String ventas = resultado.getString("ventas");
				final String nombre = resultado.getString("nombre");
				NPCModelo npcModelo = new NPCModelo(id, gfxID, escalaX, escalaY, sexo, color1, color2, color3, foto, preguntaID,
				ventas, nombre, resultado.getInt("arma"), resultado.getInt("sombrero"), resultado.getInt("capa"), resultado
				.getInt("mascota"), resultado.getInt("escudo"));
				Mundo.addNPCModelo(npcModelo);
				if (MainServidor.ID_NPC_BOUTIQUE == npcModelo.getID()) {
					MainServidor.NPC_BOUTIQUE = new NPC(npcModelo, 0, (short) 0, (byte) 0);
				}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_MISION_OBJETIVOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `mision_objetivos`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addMisionObjetivoModelo(resultado.getInt("id"), resultado.getByte("tipo"), resultado.getString("args"));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_ORNAMENTOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `ornamentos`;", _bdEstatica);
			while (resultado.next()) {
				Ornamento o = new Ornamento(resultado.getInt("id"), resultado.getString("nombre"), resultado.getInt("creditos"),
				resultado.getInt("ogrinas"), resultado.getInt("kamas"), resultado.getString("vender").equalsIgnoreCase("true"),
				resultado.getString("valido").equalsIgnoreCase("true"));
				Mundo.addOrnamento(o);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_TITULOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `titulos`;", _bdEstatica);
			while (resultado.next()) {
				Titulo o = new Titulo(resultado.getInt("id"), resultado.getString("nombre"), resultado.getInt("creditos"),
				resultado.getInt("ogrinas"), resultado.getInt("kamas"), resultado.getString("vender").equalsIgnoreCase("true"),
				resultado.getString("valido").equalsIgnoreCase("true"));
				Mundo.addTitulo(o);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_ZAAPS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `zaaps`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addZaap(resultado.getShort("mapa"), resultado.getShort("celda"));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_PREGUNTAS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `npc_preguntas`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addPreguntaNPC(new PreguntaNPC(resultado.getInt("id"), resultado.getString("respuestas"), resultado
				.getString("params"), resultado.getString("alternos")));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_RESPUESTAS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `npc_respuestas`;", _bdEstatica);
			while (resultado.next()) {
				final int id = resultado.getInt("id");
				final int tipo = resultado.getInt("accion");
				final String args = resultado.getString("args");
				final String condicion = resultado.getString("condicion");
				RespuestaNPC respuesta = Mundo.getRespuestaNPC(id);
				if (respuesta == null) {
					respuesta = new RespuestaNPC(id);
					Mundo.addRespuestaNPC(respuesta);
				}
				respuesta.addAccion(new Accion(tipo, args, condicion));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static int CARGAR_ACCION_FINAL_DE_PELEA() {
		int numero = 0;
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `accion_pelea`;", _bdEstatica);
			while (resultado.next()) {
				final Mapa mapa = Mundo.getMapa(resultado.getShort("mapa"));
				if (mapa == null) {
					continue;
				}
				Accion accion = new Accion(resultado.getInt("accion"), resultado.getString("args"), resultado.getString(
				"condicion"));
				mapa.addAccionFinPelea(resultado.getInt("tipoPelea"), accion);
				numero++;
			}
			cerrarResultado(resultado);
			return numero;
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
		return numero;
	}
	
	public static int CARGAR_ACCIONES_USO_OBJETOS() {
		int numero = 0;
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `objetos_accion`;", _bdEstatica);
			while (resultado.next()) {
				final ObjetoModelo objMod = Mundo.getObjetoModelo(resultado.getInt("objetoModelo"));
				if (objMod == null) {
					continue;
				}
				objMod.addAccion(new Accion(resultado.getInt("accion"), resultado.getString("args"), ""));
				numero++;
			}
			cerrarResultado(resultado);
			return numero;
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
		return numero;
	}
	
	public static void CARGAR_TUTORIALES() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `tutoriales`;", _bdEstatica);
			while (resultado.next()) {
				final int id = resultado.getInt("id");
				final String inicio = resultado.getString("inicio");
				final String recompensa = resultado.getString("recompensa1") + "$" + resultado.getString("recompensa2") + "$"
				+ resultado.getString("recompensa3") + "$" + resultado.getString("recompensa4");
				final String fin = resultado.getString("final");
				Mundo.addTutorial(new Tutorial(id, recompensa, inicio, fin));
			}
			cerrarResultado(resultado);
			return;
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_INTERACTIVOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `objetos_interactivos`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addObjInteractivoModelo(new ObjetoInteractivoModelo(resultado.getInt("id"), resultado.getInt("recarga"),
				resultado.getInt("duracion"), resultado.getByte("accionPJ"), resultado.getByte("caminable"), resultado.getByte(
				"tipo"), resultado.getString("gfx"), resultado.getString("skill")));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void RECARGAR_CERCADOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `cercados`;", _bdDinamica);
			while (resultado.next()) {
				try {
					Mundo.getCercadoPorMapa(resultado.getShort("mapa")).actualizarCercado(resultado.getInt("propietario"),
					resultado.getInt("gremio"), resultado.getInt("precio"), resultado.getString("objetosColocados"), resultado
					.getString("criando"));
				} catch (Exception e) {}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void RECARGAR_COFRES() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `cofres`;", _bdDinamica);
			while (resultado.next()) {
				try {
					Mundo.getCofre(resultado.getInt("id")).actualizarCofre(resultado.getString("objetos"), resultado.getLong(
					"kamas"), resultado.getString("clave"), resultado.getInt("dueño"));
				} catch (Exception e) {}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_OBJETOS_TRUEQUE() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `objetos_trueque` ORDER BY `prioridad` DESC;",
			_bdEstatica);
			while (resultado.next()) {
				try {
					Mundo.addObjetoTrueque(resultado.getInt("idObjeto"), resultado.getString("necesita"), resultado.getInt(
					"prioridad"), resultado.getString("npc_ids"));
				} catch (Exception e) {}
			}
			cerrarResultado(resultado);
			return;
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_ALMANAX() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `almanax`;", _bdEstatica);
			while (resultado.next()) {
				try {
					if (resultado.getString("ofrenda").isEmpty()) {
						continue;
					}
					Mundo.addAlmanax(new Almanax(resultado.getInt("id"), resultado.getInt("tipo"), resultado.getInt("bonus"),
					resultado.getString("ofrenda")));
				} catch (Exception e) {}
			}
			cerrarResultado(resultado);
			return;
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_MISIONES() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `misiones`;", _bdEstatica);
			while (resultado.next()) {
				MisionModelo mision = new MisionModelo(resultado.getInt("id"), resultado.getString("etapas"), resultado
				.getString("nombre"), resultado.getString("pregDarMision"), resultado.getString("pregMisCompletada"), resultado
				.getString("pregMisIncompleta"), resultado.getString("puedeRepetirse").equalsIgnoreCase("true"));
				Mundo.addMision(mision);
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void SELECT_PUESTOS_MERCADILLOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `mercadillos`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addPuestoMercadillo(new Mercadillo(resultado.getInt("id"), resultado.getString("mapa"), resultado.getInt(
				"porcVenta"), resultado.getShort("tiempoVenta"), resultado.getShort("cantidad"), resultado.getShort("nivelMax"),
				resultado.getString("categorias")));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_ETAPAS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `mision_etapas`;", _bdEstatica);
			while (resultado.next()) {
				Mundo.addEtapa(resultado.getInt("id"), resultado.getString("recompensas"), resultado.getString("objetivos"),
				resultado.getString("nombre"));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_MAPAS_ESTRELLAS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `mapas_estrellas`;", _bdDinamica);
			while (resultado.next()) {
				try {
					Mundo.addMapaEstrellas(resultado.getShort("mapa"), resultado.getString("estrellas"));
				} catch (Exception e) {}
			}
			cerrarResultado(resultado);
			return;
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_OBJETOS() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `objetos`;", _bdDinamica);
			while (resultado.next()) {
				Mundo.objetoIniciarServer(resultado.getInt("id"), resultado.getInt("modelo"), resultado.getInt("cantidad"),
				resultado.getByte("posicion"), resultado.getString("stats"), resultado.getInt("objevivo"), resultado.getInt(
				"precio"));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void SELECT_RANKING_KOLISEO() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `ranking_koliseo`;", _bdDinamica);
			while (resultado.next()) {
				Mundo.addRankingKoliseo(new RankingKoliseo(resultado.getInt("id"), resultado.getString("nombre"), resultado
				.getInt("victorias"), resultado.getInt("derrotas")));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void SELECT_RANKING_PVP() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `ranking_pvp`;", _bdDinamica);
			while (resultado.next()) {
				Mundo.addRankingPVP(new RankingPVP(resultado.getInt("id"), resultado.getString("nombre"), resultado.getInt(
				"victorias"), resultado.getInt("derrotas"), resultado.getInt("nivelAlineacion")));
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void CARGAR_MAPAS_HEROICO() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `mapas_heroico`;", _bdDinamica);
			while (resultado.next()) {
				try {
					Mundo.addMapaHeroico(resultado.getShort("mapa"), resultado.getString("mobs"), resultado.getString("objetos"),
					resultado.getString("kamas"));
				} catch (Exception e) {}
			}
			cerrarResultado(resultado);
			return;
		} catch (final Exception e) {
			exceptionExit(e, "");
		}
	}
	
	public static void INSERT_CEMENTERIO(String nombre, int nivel, byte sexo, byte clase, String asesino, int subArea) {
		final String consultaSQL = "INSERT INTO `cementerio` (`nombre`,`nivel`,`sexo`,`clase`,`asesino`,`subArea`,`fecha`) VALUES (?,?,?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setString(1, nombre);
			declaracion.setInt(2, nivel);
			declaracion.setByte(3, sexo);
			declaracion.setByte(4, clase);
			declaracion.setString(5, asesino);
			declaracion.setInt(6, subArea);
			declaracion.setLong(7, System.currentTimeMillis());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void INSERT_OBJETO_TRUEQUE(int objeto, String solicita, int prioridad, String npcs, String nombre) {
		final String consultaSQL = "INSERT INTO `objetos_trueque` (`idObjeto`,`necesita`,`prioridad`,`npc_ids`,`nombre_objeto`) VALUES (?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, objeto);
			declaracion.setString(2, solicita);
			declaracion.setInt(3, prioridad);
			declaracion.setString(4, npcs);
			declaracion.setString(5, nombre);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_STATS_OBJETO_SET(final int id, final String bonus) {
		String consultaSQL = "UPDATE `objetos_set` SET `bonus` = ? WHERE `id` = ? ;";
		try {
			PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, bonus);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_PRECIO_OBJETO_MODELO(final int id, final int ogrinas, final boolean vip) {
		String consultaSQL = "UPDATE `objetos_modelo` SET `kamas` = ? WHERE `id` = ? ;";
		if (vip) {
			consultaSQL = "UPDATE `objetos_modelo` SET `ogrinas` = ? WHERE `id` = ? ;";
		}
		try {
			PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, ogrinas);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_STATS_OBJETO_MODELO(final int id, final String stats) {
		final String consultaSQL = "UPDATE `objetos_modelo` SET `statsModelo` = ? WHERE `id` = ? ;";
		try {
			PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, stats);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void INSERT_COFRE_MODELO(int casaID, short mapaID, short celdaID) {
		final String consultaSQL = "INSERT INTO `cofres_modelo` (`casa`,`mapa`,`celda`) VALUES (?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, casaID);
			declaracion.setInt(2, mapaID);
			declaracion.setInt(3, celdaID);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static int GET_COFRE_POR_MAPA_CELDA(short mapa, short celda) {
		int id = -1;
		String consultaSQL = "SELECT * FROM `cofres_modelo` WHERE `mapa` = '" + mapa + "' AND `celda` = '" + celda + "';";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdEstatica);
			if (resultado.next()) {
				id = resultado.getInt("id");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return id;
	}
	
	public static void REPLACE_COFRE(final Cofre cofre, boolean salvarObjetos) {
		if (cofre == null) {
			return;
		}
		final String consultaSQL = "REPLACE INTO `cofres` VALUES (?,?,?,?,?)";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, cofre.getID());
			declaracion.setString(2, cofre.analizarObjetoCofreABD());
			declaracion.setLong(3, cofre.getKamas());
			declaracion.setString(4, cofre.getClave());
			declaracion.setInt(5, cofre.getDueñoID());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			if (salvarObjetos) {
				SALVAR_OBJETOS(cofre.getObjetos());
			}
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	// private static void CARGAR_PERSONAJES_POR_CUENTA(final Cuenta cuenta) {
	// try {
	// final ResultSet resultado = consultaSQL("SELECT * FROM `personajes` WHERE `cuenta` = " +
	// cuenta.getID() + ";",
	// _bdDinamica);
	// while (resultado.next()) {
	// try {
	// cuenta.addPersonaje(Mundo.getPersonaje(resultado.getInt("id")));
	// } catch (final Exception e) {
	// Bustemu.redactarLogServidorln("El personaje " + resultado.getString("nombre")
	// + " no se pudo agregar a la cuenta (REFRESCAR CUENTA)");
	// }
	// }
	// cerrarResultado(resultado);
	// } catch (final Exception e) {
	// Bustemu.redactarLogServidorln("ERROR SQL: " + e.toString());
	// e.printStackTrace();
	// }
	// }
	public static boolean DELETE_PERSONAJE(final Personaje perso) {
		final String consultaSQL = "DELETE FROM `personajes` WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, perso.getID());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static void INSERT_CAPTCHA(final String captcha, final String respuesta) {
		final String consultaSQL = "INSERT INTO `captchas` (`captcha`,`respuesta`) VALUES (?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, captcha);
			declaracion.setString(2, respuesta);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {}
	}
	
	public static void INSERT_MAPA(final short id, final String fecha, final byte ancho, final byte alto,
	final String mapData, final short X, final short Y, final short subArea) {
		final String consultaSQL = "INSERT INTO `mapas` (`id`,`fecha`,`ancho`,`alto`,`mapData`,`X`,`Y`, `subArea`,`key`, `mobs`) VALUES (?,?,?,?,?,?,?,?,'','');";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setShort(1, id);
			declaracion.setString(2, fecha);
			declaracion.setByte(3, ancho);
			declaracion.setByte(4, alto);
			declaracion.setString(5, mapData);
			declaracion.setShort(6, X);
			declaracion.setShort(7, Y);
			declaracion.setShort(8, subArea);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_GFX_OBJMODELO(final int id, final int gfx) {
		final String consultaSQL = "UPDATE `objetos_modelo` SET `gfx` = ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, gfx);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_NIVEL_OBJMODELO(final int id, final short nivel) {
		final String consultaSQL = "UPDATE `objetos_modelo` SET `nivel` = ?, `nivelCore` = 'true' WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setShort(1, nivel);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void ACTUALIZAR_NPC_VENTAS(final NPCModelo npc) {
		final String consultaSQL = "UPDATE `npcs_modelo` SET `ventas` = ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, npc.actualizarStringBD());
			declaracion.setInt(2, npc.getID());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static boolean REPLACE_GRUPOMOB_FIJO(final int mapaID, final int celdaID, final String grupoData, int tipo,
	String condicion, int segundos) {
		final String consultaSQL = "REPLACE INTO `mobs_fix` (`mapa`,`celda`,`mobs`,`tipo`,`condicion`,`segundosRespawn`,`descripcion`) VALUES (?,?,?,?,?,?,'')";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, mapaID);
			declaracion.setInt(2, celdaID);
			declaracion.setString(3, grupoData);
			declaracion.setInt(4, tipo);
			declaracion.setString(5, condicion);
			declaracion.setInt(6, segundos);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static void UPDATE_MISION(final int id, final String etapas, final String pregDarMision,
	final String pregMisCompletada, final String pregMisIncompleta) {
		final String consultaSQL = "UPDATE `misiones` SET `etapas`= ?, `pregDarMision`= ?, `pregMisCompletada`= ?, `pregMisIncompleta`= ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, etapas);
			declaracion.setString(2, pregDarMision);
			declaracion.setString(3, pregMisCompletada);
			declaracion.setString(4, pregMisIncompleta);
			declaracion.setInt(5, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_OBJETIVO_MISION(final int id, final String args) {
		final String consultaSQL = "UPDATE `mision_objetivos` SET `args`= ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, args);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_RECOMPENSA_ETAPA(final int id, final String recompensas) {
		final String consultaSQL = "UPDATE `mision_etapas` SET `recompensas`= ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, recompensas);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_ETAPA(final int id, final String objetivos) {
		final String consultaSQL = "UPDATE `mision_etapas` SET `objetivos`= ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, objetivos);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_NPC_MODELO(final NPCModelo npcMod, final int arma, final int sombrero, final int capa,
	final int mascota, final int escudo) {
		final String consultaSQL = "UPDATE `npcs_modelo` SET `sexo`= ?, `scaleX`= ?, `scaleY`= ?, `gfxID`= ?, `color1`= ?, `color2`= ?, `color3`= ?, `arma`= ?, `sombrero`= ?, `capa`= ?, `mascota`= ?, `escudo`= ?  WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setByte(1, npcMod.getSexo());
			declaracion.setShort(2, npcMod.getTallaX());
			declaracion.setShort(3, npcMod.getTallaY());
			declaracion.setInt(4, npcMod.getGfxID());
			declaracion.setInt(5, npcMod.getColor1());
			declaracion.setInt(6, npcMod.getColor2());
			declaracion.setInt(7, npcMod.getColor3());
			declaracion.setInt(8, arma);
			declaracion.setInt(9, sombrero);
			declaracion.setInt(10, capa);
			declaracion.setInt(11, mascota);
			declaracion.setInt(12, escudo);
			declaracion.setInt(13, npcMod.getID());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_ALMANAX(int id, String ofrenda, int tipo, int bonus) {
		final String consultaSQL = "UPDATE `almanax` SET `ofrenda`=?, `tipo`=?, `bonus`= ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, ofrenda);
			declaracion.setInt(2, tipo);
			declaracion.setInt(3, bonus);
			declaracion.setInt(4, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void CAMBIAR_SEXO_CLASE(final Personaje perso) {
		final String consultaSQL = "UPDATE `personajes` SET `sexo`=?, `clase`= ?, `hechizos`= ? WHERE `id`= ?";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, perso.getSexo());
			declaracion.setInt(2, perso.getClaseID(true));
			declaracion.setString(3, perso.stringHechizosParaSQL());
			declaracion.setInt(4, perso.getID());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_NOMBRE_PJ(final Personaje perso) {
		final String consultaSQL = "UPDATE `personajes` SET `nombre` = ? WHERE `id` = ? ;";
		try {
			PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setString(1, perso.getNombre());
			declaracion.setInt(2, perso.getID());
			ejecutarTransaccion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_COLORES_PJ(final Personaje perso) {
		final String consultaSQL = "UPDATE `personajes` SET `color1` = ?, `color2`= ?, `color3` = ? WHERE `id` = ? ;";
		try {
			PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, perso.getColor1());
			declaracion.setInt(2, perso.getColor2());
			declaracion.setInt(3, perso.getColor3());
			declaracion.setInt(4, perso.getID());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void SALVAR_PERSONAJE(final Personaje perso, final boolean salvarObjetos) {
		String consultaSQL = "REPLACE INTO `personajes` VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
		try {
			int parametro = 1;
			PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(parametro++, perso.getID());
			declaracion.setString(parametro++, perso.getNombre());
			declaracion.setByte(parametro++, perso.getSexo());
			declaracion.setByte(parametro++, perso.getClaseID(true));
			declaracion.setInt(parametro++, perso.getColor1());
			declaracion.setInt(parametro++, perso.getColor2());
			declaracion.setInt(parametro++, perso.getColor3());
			declaracion.setLong(parametro++, perso.getKamas());
			declaracion.setInt(parametro++, perso.getPuntosHechizos());
			declaracion.setInt(parametro++, perso.getCapital());
			declaracion.setInt(parametro++, perso.getEnergia());
			declaracion.setInt(parametro++, perso.getNivel());
			declaracion.setLong(parametro++, perso.getExperiencia());
			declaracion.setInt(parametro++, perso.getTalla());
			declaracion.setInt(parametro++, perso.getGfxIDReal());
			declaracion.setInt(parametro++, perso.getAlineacion());
			declaracion.setInt(parametro++, perso.getHonor());
			declaracion.setInt(parametro++, perso.getDeshonor());
			declaracion.setInt(parametro++, perso.getGradoAlineacion());
			declaracion.setInt(parametro++, perso.getCuentaID());
			declaracion.setInt(parametro++, perso.getSubStatsBase().get(Constantes.STAT_MAS_VITALIDAD));
			declaracion.setInt(parametro++, perso.getSubStatsBase().get(Constantes.STAT_MAS_FUERZA));
			declaracion.setInt(parametro++, perso.getSubStatsBase().get(Constantes.STAT_MAS_SABIDURIA));
			declaracion.setInt(parametro++, perso.getSubStatsBase().get(Constantes.STAT_MAS_INTELIGENCIA));
			declaracion.setInt(parametro++, perso.getSubStatsBase().get(Constantes.STAT_MAS_SUERTE));
			declaracion.setInt(parametro++, perso.getSubStatsBase().get(Constantes.STAT_MAS_AGILIDAD));
			declaracion.setInt(parametro++, perso.mostrarAmigos() ? 1 : 0);
			declaracion.setInt(parametro++, perso.alasActivadas() ? 1 : 0);
			declaracion.setString(parametro++, perso.getCanales());
			declaracion.setInt(parametro++, perso.getMapa().getID());
			declaracion.setInt(parametro++, perso.getCelda().getID());
			declaracion.setInt(parametro++, (int) perso.getPorcPDV());
			declaracion.setString(parametro++, perso.stringHechizosParaSQL());
			declaracion.setString(parametro++, perso.stringObjetosABD());
			declaracion.setString(parametro++, perso.getPtoSalvada());
			declaracion.setString(parametro++, perso.stringZaapsParaBD());
			declaracion.setString(parametro++, perso.stringOficios());
			declaracion.setInt(parametro++, perso.getPorcXPMontura());
			declaracion.setInt(parametro++, perso.getMontura() != null ? perso.getMontura().getID() : -1);
			declaracion.setInt(parametro++, perso.getEsposoID());
			declaracion.setString(parametro++, perso.getStringTienda());
			declaracion.setInt(parametro++, perso.esMercante() ? 1 : 0);
			declaracion.setInt(parametro++, perso.getSubStatsScroll().get(Constantes.STAT_MAS_FUERZA));
			declaracion.setInt(parametro++, perso.getSubStatsScroll().get(Constantes.STAT_MAS_INTELIGENCIA));
			declaracion.setInt(parametro++, perso.getSubStatsScroll().get(Constantes.STAT_MAS_AGILIDAD));
			declaracion.setInt(parametro++, perso.getSubStatsScroll().get(Constantes.STAT_MAS_SUERTE));
			declaracion.setInt(parametro++, perso.getSubStatsScroll().get(Constantes.STAT_MAS_VITALIDAD));
			declaracion.setInt(parametro++, perso.getSubStatsScroll().get(Constantes.STAT_MAS_SABIDURIA));
			declaracion.setLong(parametro++, perso.getRestriccionesA());
			declaracion.setLong(parametro++, perso.getRestriccionesB());
			declaracion.setInt(parametro++, 0);
			declaracion.setInt(parametro++, perso.getEmotes());
			declaracion.setString(parametro++, perso.listaTitulosParaBD());
			declaracion.setString(parametro++, perso.getTituloVIP());
			declaracion.setString(parametro++, perso.listaOrnamentosParaBD());
			declaracion.setString(parametro++, perso.stringMisiones());
			declaracion.setString(parametro++, perso.listaCardMobs());
			declaracion.setByte(parametro++, perso.getResets());
			declaracion.setString(parametro++, perso.listaAlmanax());
			declaracion.setInt(parametro++, perso.getUltimoNivel());
			declaracion.setString(parametro++, perso.getSetsRapidos());
			declaracion.setInt(parametro++, perso.getColorNombre());
			declaracion.setString(parametro++, perso.getOrden() + "," + perso.getOrdenNivel());
			ejecutarTransaccion(declaracion);
			String str = declaracion.toString();
			perso.registrar("<=SQL=> " + str.substring(str.indexOf(":")));
			if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
				MainServidor.redactarLogServidorSinPrint("SAVE SQL [" + perso.getNombre() + "] ==>" + str.substring(str.indexOf(
				":")));
			}
			cerrarDeclaracion(declaracion);
			if (perso.getMiembroGremio() != null) {
				REPLACE_MIEMBRO_GREMIO(perso.getMiembroGremio());
			}
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "PERSONAJE NO SALVADO");
		}
		if (salvarObjetos) {
			SALVAR_OBJETOS(perso.getObjetosTienda());
			SALVAR_OBJETOS(perso.getObjetosTodos());
			// SALVAR_OBJETOS(perso.getObjetosBanco());
			if (perso.getMontura() != null) {
				REPLACE_MONTURA(perso.getMontura(), true);
			}
		}
	}
	
	public static void SALVAR_OBJETOS(Collection<Objeto> objetos) {
		if (objetos == null || objetos.isEmpty()) {
			return;
		}
		List<Objeto> tempObjetos = new ArrayList<>();
		tempObjetos.addAll(objetos);
		String consultaSQL = "REPLACE INTO `objetos` VALUES(?,?,?,?,?,?,?);";
		try {
			PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			for (final Objeto obj : tempObjetos) {
				if (obj == null) {
					continue;
				}
				declaracion.setInt(1, obj.getID());
				declaracion.setInt(2, obj.getObjModeloID());
				declaracion.setInt(3, obj.getCantidad());
				declaracion.setInt(4, obj.getPosicion());
				declaracion.setString(5, obj.convertirStatsAString(true));
				declaracion.setInt(6, obj.getObjevivoID());
				declaracion.setLong(7, obj.getPrecio());
				ejecutarTransaccion(declaracion);
			}
			cerrarDeclaracion(declaracion);
			tempObjetos = null;
		} catch (Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void SALVAR_OBJETO(final Objeto objeto) {
		final String consultaSQL = "REPLACE INTO `objetos` VALUES (?,?,?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, objeto.getID());
			declaracion.setInt(2, objeto.getObjModeloID());
			declaracion.setInt(3, objeto.getCantidad());
			declaracion.setInt(4, objeto.getPosicion());
			declaracion.setString(5, objeto.convertirStatsAString(true));
			declaracion.setInt(6, objeto.getObjevivoID());
			declaracion.setLong(7, objeto.getPrecio());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void VACIAR_MAPAS_ESTRELLAS() {
		try {
			final PreparedStatement declaracion = transaccionSQL("TRUNCATE `mapas_estrellas`;", _bdDinamica);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (Exception e) {
			exceptionNormal(e, "");
		}
	}
	
	public static void VACIAR_MAPAS_HEROICO() {
		try {
			final PreparedStatement declaracion = transaccionSQL("TRUNCATE `mapas_heroico`;", _bdDinamica);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (Exception e) {
			exceptionNormal(e, "");
		}
	}
	
	public static PreparedStatement GET_STATEMENT_SQL_DINAMICA(String consultaSQL) {
		try {
			return transaccionSQL(consultaSQL, _bdDinamica);
		} catch (Exception e) {}
		return null;
	}
	
	public static void REPLACE_MAPAS_ESTRELLAS_BATCH(PreparedStatement declaracion, final int mapaID, String estrellas) {
		try {
			declaracion.setInt(1, mapaID);
			declaracion.setString(2, estrellas);
			declaracion.addBatch();
		} catch (final Exception e) {}
		return;
	}
	
	public static void REPLACE_MAPAS_HEROICO(final int mapaID, String mobs, String objetos, String kamas) {
		final String consultaSQL = "REPLACE INTO `mapas_heroico` VALUES (?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, mapaID);
			declaracion.setString(2, mobs);
			declaracion.setString(3, objetos);
			declaracion.setString(4, kamas);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void DELETE_MAPA_HEROICO(final int mapaID) {
		final String consultaSQL = "DELETE FROM `mapas_heroico` WHERE `mapa` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, mapaID);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void DELETE_MONTURA(final Montura drago) {
		final String consultaSQL = "DELETE FROM `monturas` WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, drago.getID());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void DELETE_DRAGOPAVO_LISTA(final String lista) {
		final String consultaSQL = "DELETE FROM `monturas` WHERE `id` IN (" + lista + ");";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void DELETE_OBJETOS_LISTA(final String lista) {
		final String consultaSQL = "DELETE FROM `objetos` WHERE `id` IN (" + lista + ");";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void DELETE_OBJETO(final int id) {
		final String consultaSQL = "DELETE FROM `objetos` WHERE `id` = ?";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void ACTUALIZAR_TITULO_POR_NOMBRE(final String nombre) {
		final String consultaSQL = "UPDATE `personajes` SET `titulo` = 0 WHERE `nombre` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setString(1, nombre);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void REPLACE_MONTURA(final Montura montura, final boolean salvarObjetos) {
		String consultaSQL = "REPLACE INTO `monturas` VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
		try {
			PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, montura.getID());
			declaracion.setInt(2, montura.getColor());
			declaracion.setInt(3, montura.getSexo());
			declaracion.setString(4, montura.getNombre());
			declaracion.setLong(5, montura.getExp());
			declaracion.setInt(6, montura.getNivel());
			declaracion.setInt(7, montura.getTalla());
			declaracion.setInt(8, montura.getResistencia());
			declaracion.setInt(9, montura.getAmor());
			declaracion.setInt(10, montura.getMadurez());
			declaracion.setInt(11, montura.getSerenidad());
			declaracion.setInt(12, montura.getReprod());
			declaracion.setInt(13, montura.getFatiga());
			declaracion.setInt(14, montura.getEnergia());
			declaracion.setString(15, montura.stringObjetosBD());
			declaracion.setString(16, montura.getAncestros());
			declaracion.setString(17, montura.strCapacidades());
			declaracion.setInt(18, montura.getOrientacion());
			declaracion.setInt(19, montura.getCelda() == null ? -1 : montura.getCelda().getID());
			declaracion.setInt(20, montura.getMapa() == null ? -1 : montura.getMapa().getID());
			declaracion.setInt(21, montura.getDueñoID());
			declaracion.setLong(22, montura.getTiempoFecundacion());
			declaracion.setInt(23, montura.getParejaID());
			declaracion.setString(24, montura.esSalvaje() ? "1" : "0");
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			if (salvarObjetos) {
				SALVAR_OBJETOS(montura.getObjetos());
			}
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static boolean DELETE_CERCADO(final int id) {
		final String consultaSQL = "DELETE FROM `cercados` WHERE `mapa` = ? ;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static void REPLACE_CERCADO(final Cercado cercado) {
		String consultaSQL = "REPLACE INTO `cercados` VALUES (?,?,?,?,?,?);";
		try {
			PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, cercado.getMapa().getID());
			declaracion.setInt(2, cercado.getDueñoID());
			declaracion.setInt(3, cercado.getGremio() == null ? -1 : cercado.getGremio().getID());
			declaracion.setInt(4, cercado.getPrecioPJ());
			declaracion.setString(5, cercado.strPavosCriando());
			declaracion.setString(6, cercado.strObjCriaParaBD());
			ejecutarTransaccion(declaracion);
			consultaSQL = "REPLACE INTO `objetos` VALUES (?,?,?,?,?,?,?);";
			try {
				declaracion = transaccionSQL(consultaSQL, _bdDinamica);
				for (final Objeto obj : cercado.getObjetosParaBD()) {
					if (obj == null) {
						continue;
					}
					declaracion.setInt(1, obj.getID());
					declaracion.setInt(2, obj.getObjModeloID());
					declaracion.setInt(3, obj.getCantidad());
					declaracion.setInt(4, obj.getPosicion());
					declaracion.setString(5, obj.convertirStatsAString(true));
					declaracion.setInt(6, obj.getObjevivoID());
					declaracion.setLong(7, obj.getPrecio());
					ejecutarTransaccion(declaracion);
				}
			} catch (final Exception e1) {}
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void REPLACE_RANKING_KOLISEO(final RankingKoliseo rank) {
		final String consultaSQL = "REPLACE INTO `ranking_koliseo` VALUES (?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, rank.getID());
			declaracion.setString(2, rank.getNombre());
			declaracion.setInt(3, rank.getVictorias());
			declaracion.setInt(4, rank.getDerrotas());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static boolean DELETE_RANKING_KOLISEO(final int id) {
		final String consultaSQL = "DELETE FROM `ranking_koliseo` WHERE `id` = ? ;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static void REPLACE_RANKING_PVP(final RankingPVP rank) {
		final String consultaSQL = "REPLACE INTO `ranking_pvp` VALUES (?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, rank.getID());
			declaracion.setString(2, rank.getNombre());
			declaracion.setInt(3, rank.getVictorias());
			declaracion.setInt(4, rank.getDerrotas());
			declaracion.setInt(5, rank.getGradoAlineacion());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static boolean DELETE_RANKING_PVP(final int id) {
		final String consultaSQL = "DELETE FROM `ranking_pvp` WHERE `id` = ? ;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static void REPLACE_ACCION_OBJETO(final int idModelo, final int accion, final String args,
	final String nombre) {
		final String consultaSQL = "REPLACE INTO `objetos_accion` VALUES(?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, idModelo);
			declaracion.setInt(2, accion);
			declaracion.setString(3, args);
			declaracion.setString(4, nombre);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void DELETE_ACCION_OBJETO(final int id) {
		final String consultaSQL = "DELETE FROM `objetos_accion` WHERE `objetoModelo` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void INSERT_DROP(final int mob, final int objeto, final int prosp, final float porcentaje, int max,
	final String nMob, final String nObjeto, final String condicion) {
		final String consultaSQL = "INSERT INTO `drops` (`mob`,`objeto`,`prospeccion`, `porcentaje`,`max`, `nombre_mob`, `nombre_objeto`,`condicion`) VALUES (?,?,?,?,?,?,?,?);";
		try {
			DELETE_DROP(objeto, mob);
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, mob);
			declaracion.setInt(2, objeto);
			declaracion.setInt(3, prosp);
			declaracion.setFloat(4, porcentaje);
			declaracion.setInt(5, max);
			declaracion.setString(6, nMob);
			declaracion.setString(7, nObjeto);
			declaracion.setString(8, condicion);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_DROPS(final int idMob, final int idObjeto, final String nombreMob,
	final String nombreObjeto) {
		final String consultaSQL = "UPDATE `drops` SET `nombre_mob`=?, `nombre_objeto` =? WHERE `mob`=? AND `objeto`= ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, nombreMob);
			declaracion.setString(2, nombreObjeto);
			declaracion.setInt(3, idMob);
			declaracion.setInt(4, idObjeto);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void DELETE_DROP(final int objeto, final int mob) {
		final String consultaSQL = "DELETE FROM `drops` WHERE `objeto` ='" + objeto + "' AND `mob`= '" + mob + "' ;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void ACTUALIZAR_SERVER(final boolean estatica, final boolean dinamica, final boolean cuentas) {
		try {
			if (MainServidor.ES_LOCALHOST) {
				return;
			}
			if (estatica) {
				final PreparedStatement declaracion = transaccionSQL("DROP DATABASE " + MainServidor.BD_ESTATICA + " ;",
				_bdEstatica);
				ejecutarTransaccion(declaracion);
				cerrarDeclaracion(declaracion);
			}
			if (dinamica) {
				final PreparedStatement declaracion = transaccionSQL("DROP DATABASE " + MainServidor.BD_DINAMICA + " ;",
				_bdDinamica);
				ejecutarTransaccion(declaracion);
				cerrarDeclaracion(declaracion);
			}
			if (cuentas) {
				final PreparedStatement declaracion = transaccionSQL("DROP DATABASE " + MainServidor.BD_CUENTAS + " ;",
				_bdCuentas);
				ejecutarTransaccion(declaracion);
				cerrarDeclaracion(declaracion);
			}
		} catch (final Exception e) {}
	}
	
	public static String QUERY_ESTATICA(final String query) {
		try {
			final PreparedStatement declaracion = transaccionSQL(query, _bdEstatica);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return "GOOD!!";
		} catch (final Exception e) {
			return e.toString();
		}
	}
	
	public static String QUERY_DINAMICA(final String query) {
		try {
			final PreparedStatement declaracion = transaccionSQL(query, _bdDinamica);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return "GOOD!!";
		} catch (final Exception e) {
			return e.toString();
		}
	}
	
	public static String QUERY_CUENTAS(final String query) {
		try {
			final PreparedStatement declaracion = transaccionSQL(query, _bdCuentas);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return "GOOD!!";
		} catch (final Exception e) {
			return e.toString();
		}
	}
	
	public static String QUERY_ALTERNA(final String query) {
		try {
			final PreparedStatement declaracion = transaccionSQL(query, _bdAlterna);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return "GOOD!!";
		} catch (final Exception e) {
			return e.toString();
		}
	}
	
	public static boolean REPLACE_CELDAS_ACCION(final int mapa1, final int celda1, final int accion, final String args,
	final String condicion) {
		final String consultaSQL = "REPLACE INTO `celdas_accion` VALUES (?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, mapa1);
			declaracion.setInt(2, celda1);
			declaracion.setInt(3, accion);
			declaracion.setString(4, args);
			declaracion.setString(5, condicion);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean REPLACE_OBJETO_MODELO(final int id, final short tipo, final String nombre, final short gfx,
	final boolean nivelCore, final short nivel, final String stats, final short peso, final short set, final int kamas,
	final int ogrinas, final boolean magueable, final String infoArma, final String condicion) {
		final String consultaSQL = "REPLACE INTO `objetos_modelo` VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,'0','0');";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, id);
			declaracion.setShort(2, tipo);
			declaracion.setString(3, nombre);
			declaracion.setShort(4, gfx);
			declaracion.setString(5, nivelCore ? "true" : "false");
			declaracion.setShort(6, nivel);
			declaracion.setString(7, stats);
			declaracion.setShort(8, peso);
			declaracion.setShort(9, set);
			declaracion.setInt(10, kamas);
			declaracion.setInt(11, ogrinas);
			declaracion.setString(12, magueable ? "true" : "false");
			declaracion.setString(13, infoArma);
			declaracion.setString(14, condicion);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean DELETE_TRIGGER(final int mapaID, final int celdaID) {
		final String consultaSQL = "DELETE FROM `celdas_accion` WHERE `mapa` = ? AND `celda` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, mapaID);
			declaracion.setInt(2, celdaID);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean UPDATE_MAPA_POS_PELEA(int mapaID, String pos) {
		final String consultaSQL = "UPDATE `mapas` SET `posPelea` = ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, pos);
			declaracion.setInt(2, mapaID);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean UPDATE_MAPA_MAX_PELEAS(short mapaID, byte max) {
		final String consultaSQL = "UPDATE `mapas` SET `maxPeleas` = ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setByte(1, max);
			declaracion.setShort(2, mapaID);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean UPDATE_MAPA_MAX_MERCANTES(short mapaID, byte max) {
		final String consultaSQL = "UPDATE `mapas` SET `maxMercantes` = ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setByte(1, max);
			declaracion.setShort(2, mapaID);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean UPDATE_MAPA_MAX_GRUPO_MOBS(int mapaID, byte max) {
		final String consultaSQL = "UPDATE `mapas` SET `maxGrupoMobs` = ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setByte(1, max);
			declaracion.setInt(2, mapaID);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean UPDATE_MAPA_MAX_MOB_GRUPO(int mapaID, byte max) {
		final String consultaSQL = "UPDATE `mapas` SET `maxMobsPorGrupo` = ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setByte(1, max);
			declaracion.setInt(2, mapaID);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean UPDATE_MAPA_PARAMETROS(final int id, final int param) {
		final String consultaSQL = "UPDATE `mapas` SET `capabilities` = ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, param);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean DELETE_NPC_DEL_MAPA(final int mapa, final int id) {
		final String consultaSQL = "DELETE FROM `npcs_ubicacion` WHERE `mapa` = ? AND `npc` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, mapa);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean DELETE_NPC_UBICACION(final int id) {
		final String consultaSQL = "DELETE FROM `npcs_ubicacion` WHERE `npc` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static void DELETE_RECAUDADOR(final int id) {
		final String consultaSQL = "DELETE FROM `recaudadores` WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static boolean REPLACE_NPC_AL_MAPA(final short mapa, final short celda, final int id, final byte direccion,
	final String nombre) {
		final String consultaSQL = "REPLACE INTO `npcs_ubicacion` VALUES (?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setShort(1, mapa);
			declaracion.setShort(2, celda);
			declaracion.setInt(3, id);
			declaracion.setByte(4, direccion);
			declaracion.setString(5, nombre);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static void REPLACE_RECAUDADOR(final Recaudador recaudador, boolean salvarObjetos) {
		final String consultaSQL = "REPLACE INTO `recaudadores` VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, recaudador.getID());
			declaracion.setInt(2, recaudador.getMapa().getID());
			declaracion.setInt(3, recaudador.getCelda().getID());
			declaracion.setInt(4, recaudador.getOrientacion());
			declaracion.setInt(5, recaudador.getGremio().getID());
			declaracion.setString(6, recaudador.getN1());
			declaracion.setString(7, recaudador.getN2());
			declaracion.setString(8, recaudador.stringListaObjetosBD());
			declaracion.setLong(9, recaudador.getKamas());
			declaracion.setLong(10, recaudador.getExp());
			declaracion.setLong(11, recaudador.getTiempoProteccion());
			declaracion.setInt(12, recaudador.getDueño());
			declaracion.setLong(13, recaudador.getTiempoCreacion());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			try {
				if (salvarObjetos) {
					SALVAR_OBJETOS(recaudador.getObjetos());
				}
			} catch (Exception e) {}
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static boolean INSERT_LOG_PELEA(final String cuentas_g, String personajes_g, String ips_g, String puntos_g,
	String cuentas_p, String personajes_p, String ips_p, String puntos_p, long duracion, int agresor, int agredido,
	short mapa) {
		final String consultaSQL = "INSERT INTO `logs_aggro` (`gagnant_account`,`gagnant_perso`,`gagnant_ip`,`gagnant_ph`,`perdant_account`,`perdant_perso`,`perdant_ip`,`perdant_ph`,`duree`,`aggroBy`,`aggroTo`,`idMap`,`timestamp`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setString(1, cuentas_g);
			declaracion.setString(2, personajes_g);
			declaracion.setString(3, ips_g);
			declaracion.setString(4, puntos_g);
			declaracion.setString(5, cuentas_p);
			declaracion.setString(6, personajes_p);
			declaracion.setString(7, ips_p);
			declaracion.setString(8, puntos_p);
			declaracion.setLong(9, duracion);
			declaracion.setInt(10, agresor);
			declaracion.setInt(11, agredido);
			declaracion.setInt(12, mapa);
			declaracion.setLong(13, System.currentTimeMillis());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean INSERT_ACCION_FIN_PELEA(final int mapaID, final int tipoPelea, final int accionID,
	final String args, String condicion, String descripcion) {
		DELETE_FIN_ACCION_PELEA(mapaID, tipoPelea, accionID);
		final String consultaSQL = "INSERT INTO `accion_pelea` VALUES (?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, mapaID);
			declaracion.setInt(2, tipoPelea);
			declaracion.setInt(3, accionID);
			declaracion.setString(4, args);
			declaracion.setString(5, args);
			declaracion.setString(6, descripcion);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean DELETE_FIN_ACCION_PELEA(final int mapaID, final int tipoPelea, final int accionID) {
		final String consultaSQL = "DELETE FROM `accion_pelea` WHERE mapa = ? AND tipoPelea = ? AND accion = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, mapaID);
			declaracion.setInt(2, tipoPelea);
			declaracion.setInt(3, accionID);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static void INSERT_GREMIO(final Gremio gremio) {
		final String consultaSQL = "INSERT INTO `gremios` VALUES (?,?,?,1,0,0,0,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, gremio.getID());
			declaracion.setString(2, gremio.getNombre());
			declaracion.setString(3, gremio.getEmblema());
			declaracion.setString(4, "462;0|461;0|460;0|459;0|458;0|457;0|456;0|455;0|454;0|453;0|452;0|451;0|");
			declaracion.setString(5, "176;100|158;1000|124;100|");
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void REPLACE_GREMIO(final Gremio gremio) {
		final String consultaSQL = "REPLACE INTO `gremios` VALUES(?,?,?,?,?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, gremio.getID());
			declaracion.setString(2, gremio.getNombre());
			declaracion.setString(3, gremio.getEmblema());
			declaracion.setInt(4, gremio.getNivel());
			declaracion.setLong(5, gremio.getExperiencia());
			declaracion.setInt(6, gremio.getCapital());
			declaracion.setInt(7, gremio.getNroMaxRecau());
			declaracion.setString(8, gremio.compilarHechizo());
			declaracion.setString(9, gremio.compilarStats());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void DELETE_GREMIO(final int id) {
		final String consultaSQL = "DELETE FROM `gremios` WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void REPLACE_MIEMBRO_GREMIO(final MiembroGremio miembro) {
		final String consultaSQL = "REPLACE INTO `miembros_gremio` VALUES(?,?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, miembro.getID());
			declaracion.setInt(2, miembro.getGremio().getID());
			declaracion.setInt(3, miembro.getRango());
			declaracion.setLong(4, miembro.getXpDonada());
			declaracion.setInt(5, miembro.getPorcXpDonada());
			declaracion.setInt(6, miembro.getDerechos());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void DELETE_MIEMBRO_GREMIO(final int id) {
		final String consultaSQL = "DELETE FROM `miembros_gremio` WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void DELETE_OTRO_INTERACTIVO(final int gfxID, short mapaID, short celdaID, int accion) {
		final String consultaSQL = "DELETE FROM `otros_interactivos` WHERE `gfx` = ? AND `mapaID` = ? AND `celdaID` = ? AND `accion` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, gfxID);
			declaracion.setInt(2, mapaID);
			declaracion.setInt(3, celdaID);
			declaracion.setInt(4, accion);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void INSERT_OTRO_INTERACTIVO(final int gfxID, final short mapaID, final short celdaID,
	final int accionID, final String args, final String condiciones, int tiempoRecarga, String descripcion) {
		String consultaSQL = "REPLACE INTO `otros_interactivos` VALUES (?,?,?,?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, gfxID);
			declaracion.setInt(2, mapaID);
			declaracion.setInt(3, celdaID);
			declaracion.setInt(4, accionID);
			declaracion.setString(5, args);
			declaracion.setString(6, condiciones);
			declaracion.setInt(7, tiempoRecarga);
			declaracion.setString(8, descripcion);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return;
	}
	
	public static boolean REPLACE_ACCIONES_RESPUESTA(final int respuestaID, final int accion, final String args,
	String condicion) {
		String consultaSQL = "REPLACE INTO `npc_respuestas` VALUES (?,?,?,?);";
		try {
			PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, respuestaID);
			declaracion.setInt(2, accion);
			declaracion.setString(3, args);
			declaracion.setString(4, condicion);
			ejecutarTransaccion(declaracion);
			consultaSQL = "UPDATE `npc_respuestas` SET `condicion` = ? WHERE `id` = ?;";
			declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, condicion);
			declaracion.setInt(2, respuestaID);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean DELETE_ACCIONES_RESPUESTA(final int respuestaID) {
		final String consultaSQL = "DELETE FROM `npc_respuestas` WHERE `id` = ? ;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, respuestaID);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean UPDATE_NPC_PREGUNTA(final int id, final int pregunta) {
		final String consultaSQL = "UPDATE `npcs_modelo` SET `pregunta` = ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, pregunta);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static boolean REPLACE_PREGUNTA_NPC(final PreguntaNPC pregunta) {
		final String consultaSQL = "REPLACE INTO `npc_preguntas` VALUES (?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, pregunta.getID());
			declaracion.setString(2, pregunta.getStrRespuestas());
			declaracion.setString(3, pregunta.getParams());
			declaracion.setString(4, pregunta.getStrAlternos());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static void REPLACE_CASA(final Casa casa) {
		final String consultaSQL = "REPLACE INTO `casas` VALUES (?,?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, casa.getID());
			declaracion.setInt(2, casa.getDueño() != null ? casa.getDueño().getID() : 0);
			declaracion.setLong(3, casa.getKamasVenta());
			declaracion.setByte(4, (byte) (casa.getActParametros() ? 1 : 0));
			declaracion.setString(5, casa.getClave());
			declaracion.setInt(6, casa.getDerechosGremio());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_CELDA_MAPA_DENTRO_CASA(final Casa casa) {
		final String consultaSQL = "UPDATE `casas_modelo` SET `mapaDentro` = ?, `celdaDentro` = ? WHERE id = ?;";
		try {
			PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setShort(1, casa.getMapaIDDentro());
			declaracion.setShort(2, casa.getCeldaIDDentro());
			declaracion.setInt(3, casa.getID());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void REPLACE_OBJETOS_MERCADILLOS(final ArrayList<ObjetoMercadillo> lista) {
		try {
			for (final ObjetoMercadillo objMerca : lista) {
				REPLACE_OBJETO_MERCADILLO(objMerca);
			}
		} catch (final Exception e) {}
	}
	
	public static boolean REPLACE_OBJETO_MERCADILLO(final ObjetoMercadillo objMerca) {
		String consultaSQL = "REPLACE INTO `mercadillo_objetos` (`objeto`,`mercadillo`,`cantidad`,`dueño`,`precio`) VALUES (?,?,?,?,?);";
		try {
			PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			if (objMerca.getCuentaID() == 0) {
				return false;
			}
			declaracion.setInt(1, objMerca.getObjeto().getID());
			declaracion.setInt(2, objMerca.getMercadilloID());
			declaracion.setInt(3, objMerca.getTipoCantidad(false));
			declaracion.setInt(4, objMerca.getCuentaID());
			declaracion.setLong(5, objMerca.getPrecio());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			SALVAR_OBJETO(objMerca.getObjeto());
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
			return false;
		}
		return true;
	}
	
	public static void DELETE_OBJ_MERCADILLO(final int idObjeto) {
		final String consultaSQL = "DELETE FROM `mercadillo_objetos` WHERE `objeto` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, idObjeto);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_PRECIO_MEDIO_OBJETO_MODELO(ObjetoModelo objMod) {
		final String consultaSQL = "UPDATE `objetos_modelo` SET vendidos = ?, precioMedio = ? WHERE id = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, objMod.getVendidos());
			declaracion.setLong(2, objMod.getPrecioPromedio());
			declaracion.setInt(3, objMod.getID());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_MOB_IA_TALLA(final MobModelo mob) {
		final String consultaSQL = "UPDATE `mobs_modelo` SET `tipoIA` = ?, `talla` = ? WHERE id = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, mob.getTipoIA());
			declaracion.setInt(2, mob.getTalla());
			declaracion.setInt(3, mob.getID());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_STATS_MOB(int id, final String stats) {
		final String consultaSQL = "UPDATE `mobs_modelo` SET `stats` = ? WHERE id = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, stats);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_STATS_PUNTOS_PDV_XP_MOB(int id, final String stats, String pdv, String exp, String minKamas,
	String maxKamas) {
		final String consultaSQL = "UPDATE `mobs_modelo` SET `stats` = ?, `pdvs` = ?,`exps` = ? ,`minKamas` = ?,`maxKamas` = ? WHERE id = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, stats);
			declaracion.setString(2, pdv);
			declaracion.setString(3, exp);
			declaracion.setString(4, minKamas);
			declaracion.setString(5, maxKamas);
			declaracion.setInt(6, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_MOB_COLORES(final MobModelo mob) {
		final String consultaSQL = "UPDATE `mobs_modelo` SET `colores` = ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, mob.getColores());
			declaracion.setInt(2, mob.getID());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_MOB_AGRESION(final MobModelo mob) {
		final String consultaSQL = "UPDATE `mobs_modelo` SET `agresion` = ? WHERE id = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, mob.getDistAgresion());
			declaracion.setInt(2, mob.getID());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_HECHIZO_AFECTADOS(final int id, final String afectados) {
		final String consultaSQL = "UPDATE `hechizos` SET `afectados` = ? WHERE id = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, afectados);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_HECHIZOS_VALOR_IA(final int id, final int valorIA) {
		final String consultaSQL = "UPDATE `hechizos` SET `valorIA` = ? WHERE id = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, valorIA);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void ACTUALIZAR_CONDICIONES_HECHIZO(final int id, final String condiciones) {
		final String consultaSQL = "UPDATE `hechizos` SET `condiciones` = ? WHERE id = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, condiciones);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_STAT_HECHIZO(final int id, final String stat, final int grado) {
		final String consultaSQL = "UPDATE `hechizos` SET `nivel" + grado + "` = ? WHERE id = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, stat);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void ACTUALIZAR_SPRITE_INFO_HECHIZO(final int id, final String str) {
		final String consultaSQL = "UPDATE `hechizos` SET `spriteInfos` = ? WHERE id = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, str);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void ACTUALIZAR_SPRITE_ID_HECHIZO(final int id, final int sprite) {
		final String consultaSQL = "UPDATE `hechizos` SET `sprite` = ? WHERE id = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, sprite);
			declaracion.setInt(2, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void DELETE_MASCOTA(final int id) {
		final String consultaSQL = "DELETE FROM `mascotas` WHERE `objeto` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static String GET_NUEVA_FECHA_KEY(final short mapa) {
		String str = "";
		final String consultaSQL = "SELECT * FROM `maps` WHERE `id` = '" + mapa + "';";
		try {
			final ResultSet resultado = consultaSQL(consultaSQL, _bdEstatica);
			while (resultado.next()) {
				try {
					str = resultado.getString("fecha") + "|" + resultado.getString("key") + "|" + resultado.getString("mapData");
				} catch (final Exception e) {}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return str;
	}
	
	public static void UPDATE_FECHA_KEY_MAPDATA(final short mapa, final String fecha, final String key,
	final String mapData) {
		final String consultaSQL = "UPDATE `mapas` SET `fecha` = ?, `key`= ?, `mapData`= ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, fecha);
			declaracion.setString(2, key);
			declaracion.setString(3, mapData);
			declaracion.setShort(4, mapa);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_KEY_MAPA(final short mapa, final String key) {
		final String consultaSQL = "UPDATE `mapas` SET `key` = ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, key);
			declaracion.setShort(2, mapa);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_FECHA_MAPA(final short mapa, final String fecha) {
		final String consultaSQL = "UPDATE `mapas` SET `fecha` = ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, fecha);
			declaracion.setShort(2, mapa);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void UPDATE_SET_MOBS_MAPA(final int mapa, final String mob) {
		final String consultaSQL = "UPDATE `mapas` SET `mobs` = ? WHERE `id` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setString(1, mob);
			declaracion.setInt(2, mapa);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void DELETE_MOBS_FIX_MAPA(final int mapa) {
		final String consultaSQL = "DELETE FROM `mobs_fix` WHERE `mapa` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, mapa);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void DELETE_ACCION_PELEA(final int mapa) {
		final String consultaSQL = "DELETE FROM `accion_pelea` WHERE `mapa` = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdEstatica);
			declaracion.setInt(1, mapa);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void CARGAR_LIVE_ACTION() {
		try {
			final ResultSet resultado = consultaSQL("SELECT * FROM `live_action`;", _bdDinamica);
			while (resultado.next()) {
				ObjetoModelo objMod = Mundo.getObjetoModelo(resultado.getInt("idModelo"));
				if (objMod == null) {
					continue;
				}
				Objeto objNew = objMod.crearObjeto(resultado.getInt("cantidad"), Constantes.OBJETO_POS_NO_EQUIPADO,
				CAPACIDAD_STATS.RANDOM);
				objNew.convertirStringAStats(resultado.getString("stats"));
				Personaje perso = Mundo.getPersonaje(resultado.getInt("idPersonaje"));
				if (perso != null) {
					perso.addObjetoConOAKO(objNew, true);
					GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(perso, "Vous avez reçu " + resultado.getInt("cantidad") + " "
					+ resultado.getString("nombreObjeto"));
				}
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
	}
	
	public static void VACIAR_LIVE_ACTION() {
		final String consultaSQL = "TRUNCATE `live_action`;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void DELETE_PRISMA(final int id) {
		final String consultaSQL = "DELETE FROM `prismas` WHERE id = ?;";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, id);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static void REPLACE_PRISMA(final Prisma prisma) {
		final String consultaSQL = "REPLACE INTO `prismas` VALUES(?,?,?,?,?,?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setInt(1, prisma.getID());
			declaracion.setInt(2, prisma.getAlineacion());
			declaracion.setInt(3, prisma.getNivel());
			declaracion.setInt(4, prisma.getMapa().getID());
			declaracion.setInt(5, prisma.getCelda().getID());
			declaracion.setInt(6, prisma.getHonor());
			declaracion.setInt(7, prisma.getSubArea().getID());
			declaracion.setInt(8, prisma.getArea() == null ? -1 : prisma.getArea().getID());
			declaracion.setLong(9, prisma.getTiempoProteccion());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void INSERT_COMANDO_GM(final String rango, final String comando) {
		final String consultaSQL = "INSERT INTO `comandos` (`nombre gm`,`comando`,`date`) VALUES (?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setString(1, rango);
			declaracion.setString(2, comando);
			declaracion.setString(3, new Date().toLocaleString());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void INSERT_INTERCAMBIO(final String inte) {
		final String consultaSQL = "INSERT INTO `intercambios` (`intercambio`,`fecha`) VALUES (?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setString(1, inte);
			declaracion.setString(2, new Date().toLocaleString());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void INSERT_REPORTE_BUG(final String nombre, final String tema, final String detalle) {
		final String consultaSQL = "INSERT INTO `reporte_bugs` (`perso`,`asunto`,`detalle`,`fecha`) VALUES (?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setString(1, nombre);
			declaracion.setString(2, tema);
			declaracion.setString(3, detalle);
			declaracion.setString(4, new Date().toLocaleString());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void INSERT_PROBLEMA_OGRINAS(final String nombre, final String tema, final String detalle) {
		final String consultaSQL = "INSERT INTO `problema_ogrinas` (`perso`,`asunto`,`detalle`,`fecha`) VALUES (?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setString(1, nombre);
			declaracion.setString(2, tema);
			declaracion.setString(3, detalle);
			declaracion.setString(4, new Date().toLocaleString());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void INSERT_DENUNCIAS(final String nombre, final String tema, final String detalle) {
		final String consultaSQL = "INSERT INTO `denuncias` (`perso`,`asunto`,`detalle`,`fecha`) VALUES (?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setString(1, nombre);
			declaracion.setString(2, tema);
			declaracion.setString(3, detalle);
			declaracion.setString(4, new Date().toLocaleString());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void INSERT_SUGERENCIAS(final String nombre, final String tema, final String detalle) {
		final String consultaSQL = "INSERT INTO `sugerencias` (`perso`,`asunto`,`detalle`,`fecha`) VALUES (?,?,?,?);";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			declaracion.setString(1, nombre);
			declaracion.setString(2, tema);
			declaracion.setString(3, detalle);
			declaracion.setString(4, new Date().toLocaleString());
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
	}
	
	public static boolean DELETE_REPORTE(final byte tipo, final int id) {
		final String[] tipos = {"reporte_bugs", "sugerencias", "denuncias", "problema_ogrinas"};
		final String consultaSQL = "DELETE FROM `" + tipos[tipo] + "` WHERE `id` = '" + id + "';";
		try {
			final PreparedStatement declaracion = transaccionSQL(consultaSQL, _bdDinamica);
			ejecutarTransaccion(declaracion);
			cerrarDeclaracion(declaracion);
			return true;
		} catch (final Exception e) {
			exceptionModify(e, consultaSQL, "");
		}
		return false;
	}
	
	public static String GET_DESCRIPTION_REPORTE(final byte tipo, final int id) {
		String str = "";
		try {
			final String[] tipos = {"reporte_bugs", "sugerencias", "denuncias", "problema_ogrinas"};
			final ResultSet resultado = consultaSQL("SELECT * FROM `" + tipos[tipo] + "` WHERE `id` = '" + id + "';",
			_bdDinamica);
			while (resultado.next()) {
				str = "<b>" + resultado.getString("perso") + "</b> - <i><u>" + resultado.getString("asunto") + "</i></u>: "
				+ resultado.getString("detalle");
			}
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return str;
	}
	
	public static String GET_LISTA_REPORTES(final Cuenta cuenta) {
		final StringBuilder str = new StringBuilder();
		try {
			ResultSet resultado;
			resultado = consultaSQL("SELECT * FROM `reporte_bugs` LIMIT " + MainServidor.LIMITE_REPORTES + ";", _bdDinamica);
			StringBuilder str2 = new StringBuilder();
			while (resultado.next()) {
				if (str2.length() > 0) {
					str2.append("#");
				}
				str2.append(resultado.getInt("id") + ";" + resultado.getString("perso") + ";" + resultado.getString("asunto")
				+ ";" + resultado.getString("fecha") + ";" + (cuenta.tieneReporte(Constantes.REPORTE_BUGS, resultado.getInt(
				"id")) ? 1 : 0));
			}
			str.append(str2.toString() + "|");
			cerrarResultado(resultado);
			resultado = consultaSQL("SELECT * FROM `sugerencias` LIMIT " + MainServidor.LIMITE_REPORTES + ";", _bdDinamica);
			str2 = new StringBuilder();
			while (resultado.next()) {
				if (str2.length() > 0) {
					str2.append("#");
				}
				str2.append(resultado.getInt("id") + ";" + resultado.getString("perso") + ";" + resultado.getString("asunto")
				+ ";" + resultado.getString("fecha") + ";" + (cuenta.tieneReporte(Constantes.REPORTE_SUGERENCIAS, resultado
				.getInt("id")) ? 1 : 0));
			}
			str.append(str2.toString() + "|");
			cerrarResultado(resultado);
			resultado = consultaSQL("SELECT * FROM `denuncias` LIMIT " + MainServidor.LIMITE_REPORTES + ";", _bdDinamica);
			str2 = new StringBuilder();
			while (resultado.next()) {
				if (str2.length() > 0) {
					str2.append("#");
				}
				str2.append(resultado.getInt("id") + ";" + resultado.getString("perso") + ";" + resultado.getString("asunto")
				+ ";" + resultado.getString("fecha") + ";" + (cuenta.tieneReporte(Constantes.REPORTE_DENUNCIAS, resultado
				.getInt("id")) ? 1 : 0));
			}
			str.append(str2.toString() + "|");
			cerrarResultado(resultado);
			resultado = consultaSQL("SELECT * FROM `problema_ogrinas` LIMIT " + MainServidor.LIMITE_REPORTES + ";",
			_bdDinamica);
			str2 = new StringBuilder();
			while (resultado.next()) {
				if (str2.length() > 0) {
					str2.append("#");
				}
				str2.append(resultado.getInt("id") + ";" + resultado.getString("perso") + ";" + resultado.getString("asunto")
				+ ";" + resultado.getString("fecha") + ";" + (cuenta.tieneReporte(Constantes.REPORTE_OGRINAS, resultado.getInt(
				"id")) ? 1 : 0));
			}
			str.append(str2.toString());
			cerrarResultado(resultado);
		} catch (final Exception e) {
			exceptionNormal(e, "");
		}
		return str.toString();
	}


public static void CARGAR_TIENDA_CATEGORIA() {
	try {
		final ResultSet resultado = consultaSQL("SELECT * FROM `tiendacategoria`;", _bdDinamica);
		while (resultado.next()) {
			Mundo.TiendaCategoria.put(resultado.getInt("id"), new TiendaCategoria(resultado.getInt("id"), resultado.getInt("icono"), resultado.getString("nombre")));
		}
		cerrarResultado(resultado);
	} catch (final Exception e) {
		exceptionExit(e, "");
	}
}

public static void CARGAR_TIENDA_OBJETO() {
	try {
		final ResultSet resultado = consultaSQL("SELECT * FROM `tiendaobjetos`;", _bdDinamica);
		while (resultado.next()) {
			Mundo.tiendaObjetos.put(resultado.getInt("id"), new TiendaObjetos(resultado.getInt("id"), resultado.getInt("idObjeto"), resultado.getInt("tipo"), resultado.getInt("ogrinas"), resultado.getString("contenidoCaja")));
			}
		cerrarResultado(resultado);
	} catch (final Exception e) {
		exceptionExit(e, "");
	}
}

}
