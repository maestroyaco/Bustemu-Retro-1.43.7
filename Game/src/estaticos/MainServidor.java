package estaticos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import servidor.ServidorServer;
import servidor.ServidorSocket;
import sincronizador.SincronizadorSocket;
import variables.hechizo.EfectoHechizo;
import variables.montura.Montura;
import variables.npc.NPC;
import variables.pelea.Pelea;
import variables.personaje.Personaje;

public class MainServidor {
	private static final String ARCHIVO_CONFIG = "config_Servidor.txt";
	// private static StringBuilder Log_Servidor = new StringBuilder();
	private static PrintStream LOG_SERVIDOR;
	public static boolean ES_LOCALHOST = false;
	//
	//
	public static boolean MODO_ALL_OGRINAS = false;
	//
	public static boolean SON_DE_LUCIANO = true;
	public static boolean MODO_DESCONECTAR_PACKET_MALO = false;
	public static int MAX_PACKETS_PARA_RASTREAR = 10;
	public static int MAX_CARACTERES_SONIDO = 50;
	public static int MAX_PACKETS_DESCONOCIDOS = 20;
	public static String URL_LINK_COMPRA = "";
	public static String URL_LINK_VOTO = "";
	public static String URL_LINK_BUG = "";
	public static String URL_IMAGEN_VOTO = "";
	public static String URL_BACKUP_PHP = "";
	public static String URL_DETECTAR_DDOS = "";
	public static String URL_LINK_MP3 = "http://localhost/mp3/";
	public static String DIRECTORIO_LOCAL_MP3 = "C://wamp/www/mp3/";
	public static ArrayList<String> COMANDOS_PERMITIDOS = new ArrayList<>();
	public static ArrayList<String> COMANDOS_VIP = new ArrayList<>();
	public static int ID_MIMOBIONTE = -1;
	public static int ID_ORBE = 0;
	public static int INDEX_IP = 0;
	public static int TIME_SLEEP_PACKETS_CARGAR_MAPA = 50;
	public static int CANTIDAD_GRUPO_MOBS_MOVER_POR_MAPA = 5;
	public static int VECES_PARA_BAN_IP_SIN_ESPERA = 3;
	public static int NIVEL_INTELIGENCIA_ARTIFICIAL = 8;
	// horas
	public static int HORA_NOCHE = 2;
	public static int MINUTOS_NOCHE = 0;
	public static int HORA_DIA = 14;
	public static int MINUTOS_DIA = 0;
	// public static short PODER_PRISMA = 100;
	// public static short PDV_PRISMA = 1000;
	// public static short DIVISOR_PP = 2000;
	public static boolean ACTIVAR_CONSOLA = true;
	public static boolean MOSTRAR_RECIBIDOS;
	public static boolean MOSTRAR_ENVIOS;
	public static boolean MOSTRAR_SINCRONIZACION;
	// public static boolean REGISTER_SENDING = false;
	// public static boolean REGISTER_RECIVED = true;
	// MODOS
	public static boolean MODO_DEBUG;
	public static boolean MODO_MAPAS_LIMITE;
	public static boolean MODO_PVP;
	public static boolean MODO_HEROICO;
	public static boolean MODO_ANKALIKE;
	public static boolean MODO_BETA = false;
	public static NPC NPC_BOUTIQUE;
	public static short ID_NPC_BOUTIQUE = 0;
	public static int ID_BOLSA_OGRINAS = 0;
	public static int ID_BOLSA_CREDITOS = 0;
	public static int IMPUESTO_BOLSA_OGRINAS = 1;
	public static int IMPUESTO_BOLSA_CREDITOS = 1;
	public static int DURABILIDAD_REDUCIR_OBJETO_CRIA = 10;
	public static short DIAS_PARA_BORRAR = 60;
	public static String PALABRA_CLAVE_CONSOLA = "";
	public static String PERMITIR_MULTIMAN = "0,4";
	public static float SISTEMA_ITEMS_PERFECTO_MULTIPLICA_POR = 2;
	public static short SISTEMA_ITEMS_EXO_PA_PRECIO = 100;
	public static short SISTEMA_ITEMS_EXO_PM_PRECIO = 100;
	public static String SISTEMA_ITEMS_TIPO_DE_PAGO = "OGRINAS";
	public static ArrayList<Short> SISTEMA_ITEMS_EXO_TIPOS_NO_PERMITIDOS = new ArrayList<>();
	public static ArrayList<Short> MAPAS_MODO_HEROICO = new ArrayList<>();
	public static ArrayList<Integer> RUNAS_NO_PERMITIDAS = new ArrayList<>();
	public static ArrayList<Integer> MOBS_DOBLE_ORBES = new ArrayList<>();
	public static ArrayList<Integer> MOBS_NO_ORBES = new ArrayList<>();
	public static ArrayList<Integer> IDS_NPCS_VENDE_OBJETOS_STATS_MAXIMOS = new ArrayList<>();
	public static ArrayList<Integer> IDS_OBJETOS_STATS_MAXIMOS = new ArrayList<>();
	public static ArrayList<Integer> IDS_OBJETOS_STATS_RANDOM = new ArrayList<>();
	public static ArrayList<Integer> IDS_OBJETOS_STATS_MINIMOS = new ArrayList<>();
	public static ArrayList<Byte> SALVAR_LOGS_TIPO_COMBATE = new ArrayList<>();
	public static ArrayList<Byte> PERMITIR_MULTIMAN_TIPO_COMBATE = new ArrayList<>();
	public static Map<Byte, Integer> OGRINAS_CREAR_CLASE = new TreeMap<>();
	public static Map<String, String> PRECIOS_SERVICIOS = new TreeMap<>();
	public static String COLOR_CELDAS_PELEA_AGRESOR = "";
	public static Map<Integer, Integer> MAX_GOLPES_CAC = new TreeMap<Integer, Integer>();
	// CREAR TU ITEM
	public static String GFX_CREA_TU_ITEM_CAPAS = "1,2,3,4,5,7,8,9,10,11,12,15,16,17,18,19,21,22,23,33,34,35,36,37,38,39,40,41,42,43,44,46,47,48,49,50,51,52,53,54,55,56,58,59,60,61,62,63,64,65,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,89,90,91,92,93,94,95,96,97,98,99,100,101,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224,225,226,227,230,231,232,233,234,236,237,238,239,240,241,242,243,244,245,246,247,248,249,250,251,252,253,254,255,256,257,258,259";
	public static String GFX_CREA_TU_ITEM_AMULETOS = "1,2,3,4,5,6,7,8,9,10,11,12,13,15,16,17,18,19,20,22,23,24,25,26,27,28,29,30,31,32,33,34,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224,225";
	public static String GFX_CREA_TU_ITEM_ANILLOS = "1,2,3,4,5,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224,225,226,227,228,229,230,231,232,233,234,235,236,237,238,239,240,241,242,243,244,245,246,247,248,249,250,251,252,253,254,255,256";
	public static String GFX_CREA_TU_ITEM_CINTURONES = "3,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224,225,226,227,228,229,230,231,232,233,234,235,236,237";
	public static String GFX_CREA_TU_ITEM_BOTAS = "1,2,3,4,5,6,7,8,9,10,11,12,14,15,16,17,18,19,20,21,22,23,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224,225,226,227,228,229,230";
	public static String GFX_CREA_TU_ITEM_SOMBREROS = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,20,21,22,23,24,25,26,27,28,29,30,31,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,61,64,65,66,67,68,69,70,71,72,73,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,102,103,108,109,110,111,112,114,115,116,117,118,119,120,121,122,123,124,125,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,166,167,168,169,170,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,197,199,200,201,202,203,204,205,206,207,208,209,210,212,213,214,215,216,217,218,219,220,221,222,223,224,225,226,227,228,229,230,231,232,233,234,235,236,237,238,239,240,241,242,243,244,245,246,247,248,249,250,251,252,253,254,255,256,257,259,260,261,262,263,264,265,266,268,269,270,271,272,273,274,275,276,277,278,279,280,281,282,283,284,285,286,287,288,289,290,291,292,293,294,295,296,297,300,301,302,304,305,306,307,308,309,310,313,314,315,316,317,318,319,320,321,322,323,324,325,326,327,328,329,330,331,332,333,334,335,336,337,339,340,341,342,343";
	public static String GFX_CREA_TU_ITEM_ESCUDOS = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,71,72,73,74";
	public static String GFX_CREA_TU_ITEM_DOFUS = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18";
	// BONUS RESETS
	public static String SUFIJO_RESET = "R";
	public static short BONUS_RESET_PUNTOS_HECHIZOS = 3;
	public static short BONUS_RESET_PUNTOS_STATS = 200;
	// KAMAS
	public static int KAMAS_RULETA_JALATO = 1000;
	public static int KAMAS_BANCO = 0;
	public static int KAMAS_MOSTRAR_PROBABILIDAD_FORJA = 0;
	// OGRINAS
	public static short OGRINAS_POR_VOTO = -1;
	public static int VALOR_KAMAS_POR_OGRINA = 0;
	public static int DIAS_INTERCAMBIO_COMPRAR_SISTEMA_ITEMS = 0;
	// LOTERIA
	public static int PRECIO_LOTERIA = 5;
	public static int PREMIO_LOTERIA = 50;
	public static int GANADORES_POR_BOLETOS = 20;
	// SISTEMA OGRINAS
	//
	public static String STR_MAPAS_LIMITE = "7411,8534,951";
	public static String STR_SUBAREAS_LIMITE = "1,2";
	public static String MAPAS_KOLISEO = "951,7449";
	public static String MENSAJE_BIENVENIDA = "";
	public static String PANEL_BIENVENIDA = "";
	public static String PANEL_DESPUES_CREAR_PERSONAJE = "";
	public static String TUTORIAL_FR = "";
	public static String TUTORIAL_ES = "";
	public static String MENSAJE_SERVICIOS = "Mensaje de lista de servicios del servidor";
	public static String MENSAJE_COMANDOS = "Mensaje de lista de comandos del servidor";
	public static String MENSAJE_VIP = "Beneficios de ser vip";
	public static String MENSAJE_ERROR_OGRINAS_CREAR_CLASE = "PRICE OGRINES";
	public static String CANALES_COLOR_CHAT = "";
	// public static String COLOR_CHAT_ALL = "#777777";
	// MAPA CELDA
	public static String CERCADO_MAPA_CELDA = "";
	public static String START_MAPA_CELDA = "7411,340";
	public static String SHOP_MAPA_CELDA = "";
	public static String PVP_MAPA_CELDA = "";
	// MUTE
	public static boolean MUTE_CANAL_INCARNAM;
	public static boolean MUTE_CANAL_COMERCIO;
	public static boolean MUTE_CANAL_ALINEACION;
	public static boolean MUTE_CANAL_RECLUTAMIENTO;
	// PARAMETROS
	public static boolean PARAM_VIP_CRAFT_SPEED;
	public static boolean PARAM_CORREGIR_NOMBRE_JUGADOR;
	public static boolean PARAM_ANTIFLOOD = true;
	public static boolean PARAM_LIMITAR_RECAUDADOR_GREMIO_POR_ZONA;
	public static boolean PARAM_RESETEAR_LUPEAR_OBJETOS_MAGUEADOS;
	public static boolean PARAM_DESHABILITAR_SQL;
	public static boolean PARAM_FM_CON_POZO_RESIDUAL;
	public static boolean PARAM_MOSTRAR_STATS_INVOCACION;
	public static boolean PARAM_ENCRIPTAR_PACKETS;
	public static boolean PARAM_PERMITIR_ORNAMENTOS;
	public static boolean PARAM_RESTRINGIR_COLOR_DIA;
	public static boolean PARAM_REINICIAR_ESTRELLAS_SI_LLEGA_MAX;
	public static boolean PARAM_CLASIFICAR_POR_STUFF_EN_KOLISEO;
	public static boolean PARAM_CLASIFICAR_POR_RANKING_EN_KOLISEO;
	public static boolean PARAM_MOSTRAR_APODO_LISTA_AMIGOS = true;
	public static boolean PARAM_MOSTRAR_EXP_MOBS = true;
	public static boolean PARAM_PERMITIR_MISMAS_CLASES_EN_KOLISEO = true;
	public static boolean PARAM_PERMITIR_DESACTIVAR_ALAS = true;
	public static boolean PARAM_AGREDIR_ALAS_DESACTIVADAS = true;
	public static boolean PARAM_PERMITIR_MULTICUENTA_PELEA_KOLISEO;
	public static boolean PARAM_PERMITIR_MULTICUENTA_PELEA_RECAUDADOR = true;
	public static boolean PARAM_PERMITIR_MULTICUENTA_PELEA_PVP;
	public static boolean PARAM_SISTEMA_IP_ESPERA;
	public static boolean PARAM_BORRAR_CUENTAS_VIEJAS;
	public static boolean PARAM_AUTO_COMMIT;
	public static boolean PARAM_AGREDIR_NEUTRAL = true;
	public static boolean PARAM_MOVER_MOBS_FIJOS = true;
	public static boolean PARAM_MOBS_RANDOM_REAPARECER_OTRA_CELDA = true;
	public static boolean PARAM_CRIAR_MONTURA = true;
	public static boolean PARAM_TIMER_ACCESO = true;
	public static boolean PARAM_START_EMOTES_COMPLETOS;
	public static boolean PARAM_SOLO_PRIMERA_VEZ;
	public static boolean PARAM_PVP = true;
	public static boolean PARAM_PERMITIR_MOBS = true;
	public static boolean PARAM_ACTIVAR_AURA = true;
	public static boolean PARAM_AURA_VIP = true;
	public static boolean PARAM_PERDER_ENERGIA = true;
	public static boolean PARAM_COMANDOS_JUGADOR = true;
	public static boolean PARAM_ALMANAX;
	public static boolean PARAM_ESTRELLAS_RECURSOS;
	public static boolean PARAM_HEROICO_PIERDE_ITEMS_VIP;
	public static boolean PARAM_LOTERIA;
	public static boolean PARAM_LOTERIA_OGRINAS = true;
	public static boolean PARAM_PERDER_PDV_ARMAS_ETEREAS = true;
	public static boolean PARAM_HEROICO_GAME_OVER = true;
	public static boolean PARAM_DEVOLVER_OGRINAS;
	public static boolean PARAM_KOLISEO;
	public static boolean PARAM_LADDER_NIVEL;
	public static boolean PARAM_LADDER_KOLISEO;
	public static boolean PARAM_LADDER_PVP;
	public static boolean PARAM_LADDER_GREMIO;
	public static boolean PARAM_LADDER_EXP_DIA;
	public static boolean PARAM_LADDER_STAFF;
	public static boolean PARAM_ANTI_SPEEDHACK;
	public static boolean PARAM_MOSTRAR_CHAT_VIP_TODOS;
	// public static boolean PARAM_CREAR_ITEM;
	public static boolean PARAM_VER_JUGADORES_KOLISEO;
	// public static boolean PARAM_SISTEMA_OBJETOS_POR_OGRINAS;
	public static boolean PARAM_PRECIO_RECURSOS_EN_OGRINAS;
	public static boolean PARAM_BESTIARIO;
	public static boolean PARAM_TODOS_MOBS_EN_BESTIARIO;
	public static boolean PARAM_AUTO_RECUPERAR_TODA_VIDA;
	public static boolean PARAM_CRAFT_SIEMPRE_EXITOSA;
	public static boolean PARAM_CRAFT_PERFECTO_STATS;
	public static boolean PARAM_MONTURA_SIEMPRE_MONTABLES;
	public static boolean PARAM_JUGAR_RAPIDO;
	public static boolean PARAM_ANTI_DDOS;
	public static boolean PARAM_MOSTRAR_NRO_TURNOS;
	public static boolean PARAM_RESET_STATS_OBJETO;
	public static boolean PARAM_OBJETOS_PEFECTOS_COMPRADOS_NPC;
	public static boolean PARAM_DAR_ALINEACION_AUTOMATICA;
	public static boolean PARAM_CINEMATIC_CREAR_PERSONAJE = true;
	public static boolean PARAM_REGISTRO_LOGS_JUGADORES;
	public static boolean PARAM_REGISTRO_LOGS_SQL;
	public static boolean PARAM_NOMBRE_COMPRADOR;
	public static boolean PARAM_OBJETOS_OGRINAS_LIGADO;
	public static boolean PARAM_VARIOS_RECAUDADORES;
	public static boolean PARAM_ELIMINAR_PERSONAJES_BUG;
	public static boolean PARAM_AGREDIR_JUGADORES_ASESINOS = true;
	public static boolean PARAM_MOSTRAR_IP_CONECTANDOSE;
	public static boolean PARAM_MENSAJE_ASESINOS_HEROICO = true;
	public static boolean PARAM_MENSAJE_ASESINOS_PVP;
	public static boolean PARAM_MENSAJE_ASESINOS_KOLISEO;
	public static boolean PARAM_GUARDAR_LOGS_INTERCAMBIOS;
	public static boolean PARAM_FORMULA_TIPO_OFICIAL;
	public static boolean PARAM_STOP_SEGUNDERO;
	public static boolean PARAM_BOTON_BOUTIQUE;
	public static boolean PARAM_SISTEMA_ITEMS_SOLO_PERFECTO;
	public static boolean PARAM_SISTEMA_ITEMS_EXO_PA_PM;
	public static boolean PARAM_GANAR_HONOR_RANDOM;
	public static boolean PARAM_RESET_STATS_PLAYERS;
	public static boolean PARAM_AGRESION_ADMIN = true;
	public static boolean PARAM_AUTO_SALTAR_TURNO;
	public static boolean PARAM_TITULO_MAESTRO_OFICIO = true;
	public static boolean PARAM_GANAR_KAMAS_PVP = true;
	public static boolean PARAM_GANAR_EXP_PVP = true;
	public static boolean PARAM_ALIMENTAR_MASCOTAS = true;
	public static boolean PARAM_MASCOTAS_PERDER_VIDA = true;
	public static boolean PARAM_LIMITE_MIEMBROS_GREMIO = true;
	public static boolean PARAM_MOSTRAR_PROBABILIDAD_TACLEO;
	public static boolean PARAM_SISTEMA_ORBES;
	public static boolean PARAM_MATRIMONIO_GAY;
	public static boolean PARAM_PERMITIR_OFICIOS = true;
	public static boolean PARAM_SALVAR_LOGS_AGRESION_SQL;
	public static boolean PARAM_MOB_TENER_NIVEL_INVOCADOR_PARA_EMPUJAR;
	public static boolean PARAM_NO_USAR_OGRINAS;
	public static boolean PARAM_NO_USAR_CREDITOS;
	public static boolean PARAM_PERMITIR_DESHONOR = true;
	public static boolean PARAM_PERMITIR_AGRESION_MILICIANOS = true;
	public static boolean PARAM_PERMITIR_MILICIANOS_EN_PELEA = true;
	public static boolean PARAM_CAPTURAR_MONTURA_COMO_PERGAMINO;
	public static boolean PARAM_EXPULSAR_PREFASE_PVP = true;
	public static boolean PARAM_JUGADORES_HEROICO_MORIR = true;
	public static boolean PARAM_INFO_DAÑO_BATALLA;
	public static boolean PARAM_BOOST_SACRO_DESBUFEABLE;
	public static boolean PARAM_REINICIAR_CANALES;
	public static boolean PARAM_PERMITIR_BONUS_PELEA_AFECTEN_PROSPECCION;
	public static boolean PARAM_PERMITIR_BONUS_ESTRELLAS = true;
	public static boolean PARAM_PERMITIR_BONUS_DROP_RETOS = true;
	public static boolean PARAM_PERMITIR_BONUS_EXP_RETOS = true;
	public static boolean PARAM_PERMITIR_ADMIN_EN_LADDER = true;
	public static boolean PARAM_MOVER_MULTIPLE_OBJETOS_SOLO_ABONADOS;
	public static boolean PARAM_EXP_PVP_MISION_POR_TABLA;
	//
	public static String MENSAJE_TIMER_REBOOT = "";
	//
	public static ArrayList<Integer> SUBAREAS_NO_PVP = new ArrayList<Integer>();
	public static ArrayList<Short> TIPO_RECURSOS = new ArrayList<Short>();
	public static ArrayList<Integer> OBJ_NO_PERMITIDOS = new ArrayList<Integer>();
	public static ArrayList<Short> TIPO_ALIMENTO_MONTURA = new ArrayList<Short>();
	public static ArrayList<String> PUBLICIDAD = new ArrayList<String>();
	public static String ARMAS_ENCARNACIONES = "9544,9545,9546,9547,9548,10125,10126,10127,10133";
	public static int SABIDURIA_PARA_REENVIO = 100;
	// TIEMPOS MILISEGUNDOS
	public static int MILISEGUNDOS_ANTI_FLOOD = 5 * 1000;// 5 segundos
	public static int MILISEGUNDOS_CERRAR_SERVIDOR = 3 * 1000;// segundos
	// TIEMPO SEGUNDOS
	public static int SEGUNDOS_ENTRE_DESAFIOS_PJ = 5;
	public static int SEGUNDOS_ARENA = 10 * 60;// segundos
	public static int SEGUNDOS_INICIO_PELEA = 45;// segundos
	public static int SEGUNDOS_TURNO_PELEA = 30;// segundos
	public static int SEGUNDOS_CANAL_COMERCIO = 45;// segundos
	public static int SEGUNDOS_CANAL_RECLUTAMIENTO = 20;// segundos
	public static int SEGUNDOS_CANAL_ALINEACION = 20;// segundos
	public static int SEGUNDOS_CANAL_VIP = 10;// segundos
	public static int SEGUNDOS_CANAL_INCARNAM = 5;// segundos
	public static int SEGUNDOS_CANAL_ALL = 5;// segundos
	public static int SEGUNDOS_INACTIVIDAD = 30 * 60;// segundos
	public static int SEGUNDOS_TRANSACCION_BD = 30;// segundos
	public static int SEGUNDOS_MOVER_MONTURAS = 10 * 60;// segundos (10 miuntos)
	public static int SEGUNDOS_MOVER_RECAUDADOR = 30 * 60;// segundos (10 miuntos)
	public static int SEGUNDOS_MOVER_GRUPO_MOBS = 1 * 60;// segundos (1 minuto)
	public static int SEGUNDOS_ESTRELLAS_GRUPO_MOBS = 20 * 60;// segundos (20 minutos)
	public static int SEGUNDOS_ESTRELLAS_RECURSOS = 15 * 60;// segundos (15 minutos)
	public static int SEGUNDOS_PUBLICIDAD = 55 * 60;// segundos (55 minutos)
	public static int SEGUNDOS_SALVAR = 60 * 60;// segundos (60 minutos = 1 hora)
	public static int SEGUNDOS_INICIAR_KOLISEO = 10 * 60;// segundos (10 minutos)
	public static int SEGUNDOS_LIMPIAR_MEMORIA = 0;// segundos
	public static int SEGUNDOS_RESET_RATES = 0;// segundos
	public static int SEGUNDOS_LIVE_ACTION = 0;// segundos
	public static int SEGUNDOS_REBOOT_SERVER = 24 * 60 * 60; // minutos (24 horas = 1 dia)
	public static int SEGUNDOS_DETECTAR_DDOS = 1 * 60;// 5 minutos
	public static int SEGUNDOS_REAPARECER_MOBS = 0;
	public static int SEGUNDOS_AGREDIR_RECIEN_LLEGADO_MAPA = 2;
	// TIEMPO MINUTOS
	public static int MINUTOS_MISION_PVP = 10;// minutos (10 minutos)
	public static int MINUTOS_ALIMENTACION_MASCOTA = 10;// minutos (10 minutos)
	public static int MINUTOS_GESTACION_MONTURA = 60;// minutos (1 hora)
	public static int MINUTOS_SPAMEAR_BOTON_VOTO = 30;
	public static int MINUTOS_VALIDAR_VOTO = 180;
	public static int MINUTOS_PENALIZACION_KOLISEO = 0;
	// TIEMPO HORAS
	public static int HORAS_VOLVER_A_PONER_RECAUDADOR_MAPA = 6;
	public static int HORAS_PERDER_CRIAS_MONTURA = 24;
	// INFO SERVER
	public static int SERVIDOR_PRIORIDAD = 10;
	// public static int PUERTO_MULTISERVIDOR = 444;
	public static int PUERTO_SERVIDOR = 5555;
	public static int PUERTO_SINCRONIZADOR = 19999;
	public static CopyOnWriteArrayList<String> IP_MULTISERVIDOR = new CopyOnWriteArrayList<>();// 25.91.217.194
	public static CopyOnWriteArrayList<String> IP_PERMTIDAS = new CopyOnWriteArrayList<>();// 25.91.217.194
	public static String IP_PUBLICA_SERVIDOR = "149.130.162.183";
	public static String BD_HOST;
	public static String BD_USUARIO;
	public static String BD_PASS;
	public static String BD_ESTATICA;
	public static String BD_DINAMICA;
	public static String BD_CUENTAS;
	public static String NOMBRE_SERVER = "BUSTOFUS";
	public static int SERVIDOR_ID = 1;
	public static int ACCESO_ADMIN_MINIMO = 0;
	// RATES
	public static int RATE_XP_PVP = 1;
	public static int RATE_XP_PVM = 1;
	public static int RATE_XP_MONTURA = 1;
	public static int RATE_XP_RECAUDADOR = 1;
	public static int RATE_DROP_NORMAL = 1;
	public static int RATE_KAMAS = 1;
	public static int RATE_XP_OFICIO = 1;
	public static int RATE_XP_PVM_ABONADOS = 1;
	public static int RATE_DROP_ABONADOS = 1;
	public static int RATE_KAMAS_ABONADOS = 1;
	public static int RATE_XP_OFICIO_ABONADOS = 1;
	public static int RATE_HONOR = 1;
	public static int RATE_CRIANZA_MONTURA = 1;
	public static int RATE_CAPTURA_MONTURA = 1;
	public static int RATE_DROP_ARMAS_ETEREAS = 1;
	public static int RATE_PODS = 1;
	public static int RATE_FM = 1;
	public static int RATE_CONQUISTA_EXPERIENCIA = 1;
	public static int RATE_CONQUISTA_RECOLECTA = 1;
	public static int RATE_CONQUISTA_DROP = 1;
	// INICIO PERSONAJE
	public static int INICIO_NIVEL = 1;
	public static int INICIO_KAMAS = 0;
	public static int INICIO_EMOTES = 1;// 7667711
	public static int INICIO_PUNTOS_STATS = 0;
	public static String INICIO_OBJETOS = "";
	public static String INICIO_SET_ID = "";
	public static String INICIO_ZAAPS = "164,528,844,935,951,1158,1242,1841,2191,3022,3250,4263,4739,5295,6137,6855,6954,7411,8037,8088,8125,8163,8437,8785,9454,10297,10304,10317,10349,10643,11170,11210";
	public static int INICIO_BONUS_ESTRELLAS_RECURSOS = -20;
	public static int INICIO_BONUS_ESTRELLAS_MOBS = -20;
	public static int INICIO_NIVEL_MONTURA = 1;
	public static int PUNTOS_STATS_POR_NIVEL = 5;
	public static int PUNTOS_HECHIZO_POR_NIVEL = 1;
	// OTROS SERVER
	public static float PRECIO_SISTEMA_RECURSO = 0;
	public static float FACTOR_DEVOLVER_OGRINAS = 0.75f;
	public static float FACTOR_OBTENER_RUNAS = 1;
	public static float FACTOR_PLUS_PP_PARA_DROP = 1;
	public static int FACTOR_ZERO_DROP = 3;
	public static int MIN_CANTIDAD_MOBS_EN_GRUPO = 1;
	public static int MAX_ID_OBJETO_MODELO = 99999;
	public static int MAX_CUENTAS_POR_IP = 50;
	public static int MAX_MISIONES_ALMANAX = 180;
	public static int MAX_RECAUDADORES_POR_ZONA = 1;
	public static int MAX_BONUS_ESTRELLAS_RECURSOS = 10 * 20;
	public static int MAX_BONUS_ESTRELLAS_MOBS = 50 * 20;
	public static int MAX_PESO_POR_STAT = 101;
	public static int MAX_RESETS = 3;
	public static int MAX_CAC_POR_TURNO = 0;
	public static int MAX_PJS_POR_CUENTA = 5;
	public static int MAX_PORCENTAJE_DE_STAT_PARA_FM = 80;
	public static int PORCENTAJE_DAÑO_NO_CURABLE = 10;
	public static int PROBABILIDAD_ARCHI_MOBS = 10;
	public static int PROBABILIDAD_PROTECTOR_RECURSOS = 1;
	public static int PROBABILIDAD_RECURSO_ESPECIAL = 1;
	public static int PROBABLIDAD_PERDER_STATS_FM = 100;
	public static int PROBABILIDAD_ESCAPAR_MONTURA_DESPUES_FECUNDAR = 30;
	public static int HONOR_FIJO_PARA_TODOS = -1;
	public static int NIVEL_MINIMO_PARA_PVP = 25;
	public static int RANGO_NIVEL_PVP = 20;
	public static int RANGO_NIVEL_KOLISEO = 0;
	public static int CANTIDAD_MIEMBROS_EQUIPO_KOLISEO = 3;
	// KOLISEO
	public static int MIN_NIVEL_KOLISEO = 1;
	public static int KOLISEO_PREMIO_KAMAS = 0;
	public static int KOLISEO_DIVISOR_XP = 3;
	public static String KOLISEO_PREMIO_OBJETOS = "";
	public static int MISION_PVP_KAMAS = 2000;
	public static String MISION_PVP_OBJETOS = "10275,2";
	// LIMITES
	public static int LIMITE_MAPAS = 9000;
	public static int LIMITE_LADDER = 20;
	public static int LIMITE_REPORTES = 50;
	public static int LIMITE_SCROLL = 101;
	public static int LIMITE_MIEMBROS_GREMIO = 0;
	public static int LIMITE_OBJETOS_COFRE = 80;
	public static long LIMITE_DETECTAR_FALLA_KAMAS = 10000000;
	public static Map<Integer, Integer> LIMITE_STATS_SIN_BUFF = new TreeMap<Integer, Integer>();
	public static Map<Integer, Integer> LIMITE_STATS_CON_BUFF = new TreeMap<Integer, Integer>();
	public static Map<Integer, Integer> LIMITE_STATS_EXO_FORJAMAGIA = new TreeMap<Integer, Integer>();
	public static Map<Integer, Integer> LIMITE_STATS_OVER_FORJAMAGIA = new TreeMap<Integer, Integer>();
	// public static short LIMITE_PA = 15;
	// public static short LIMITE_PM = 7;
	// public static short LIMITE_ALCANCE = 30;
	// public static short LIMITE_PORC_RESISTENCIA_OBJETOS = 75;
	// public static short LIMITE_PORC_RESISTENCIA_BUFFS = 75;
	// NIVELES MAXIMOS
	public static int NIVEL_MAX_OFICIO;
	public static int NIVEL_MAX_PERSONAJE;
	public static int NIVEL_MAX_MONTURA;
	public static int NIVEL_MAX_GREMIO;
	public static int NIVEL_MAX_ENCARNACION;
	public static int NIVEL_MAX_ALINEACION;
	public static int NIVEL_MAX_ESCOGER_NIVEL;
	//
	public static ArrayList<String> PALABRAS_PROHIBIDAS = new ArrayList<String>();
	// PRIVATES
	private static int DEFECTO_XP_PVM;
	private static int DEFECTO_XP_PVP;
	private static int DEFECTO_XP_OFICIO;
	private static int DEFECTO_XP_HONOR;
	private static int DEFECTO_DROP;
	private static int DEFECTO_KAMAS;
	private static int DEFECTO_CRIANZA_MONTURA;
	
	// INFO_CHAT_COLOR = "009900";
	// MSG_CHAT_COLOR = "111111";
	// EMOTE_CHAT_COLOR = "222222";
	// THINK_CHAT_COLOR = "232323";
	// MSGCHUCHOTE_CHAT_COLOR = "0066FF";
	// GROUP_CHAT_COLOR = "006699";
	// ERROR_CHAT_COLOR = "C10000";
	// GUILD_CHAT_COLOR = "663399";
	// PVP_CHAT_COLOR = "DD7700";
	// RECRUITMENT_CHAT_COLOR = "737373";
	// TRADE_CHAT_COLOR = "663300";
	// MEETIC_CHAT_COLOR = "0000CC";
	// ADMIN_CHAT_COLOR = "FF00FF";
	// VIP_CHAT_COLOR = "FF00FF";
	public static void main(final String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				cerrarServer();
			}
		});
		System.out.println("ELBUSTEMU " + Constantes.VERSION_EMULADOR);
		System.out.println("Creado por Elbusta solo para Dofus");
		System.out.println("Modificado por Maestro-Yaco Para Onesv.fun");
		System.out.println("Gracias Elbusta, % trabajo 256 MB = 1000 Mapas\n");
		// cargando la config
		System.out.println("Cargando la configuración");
		leyendoIpsPermitidas();
		cargarConfiguracion(null);
		while (!IP_MULTISERVIDOR.get(0).equalsIgnoreCase("127.0.0.1")) {
			try {
				final String fecha = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" + (Calendar.getInstance().get(
				Calendar.MONTH) + 1) + "-" + Calendar.getInstance().get(Calendar.YEAR);
				LOG_SERVIDOR = new PrintStream(new FileOutputStream("Logs_Servidor_" + NOMBRE_SERVER + "/Log_Servidor_" + fecha
				+ ".txt", true));
				LOG_SERVIDOR.println("---------- INICIO DEL SERVER ----------");
				LOG_SERVIDOR.flush();
				System.setErr(LOG_SERVIDOR);
				break;
			} catch (final IOException e) {
				new File("Logs_Servidor_" + NOMBRE_SERVER).mkdir();
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		// conectado a la base de datos sql
		System.out.print("Conexión a la base de datos:  ");
		if (GestorSQL.iniciarConexion()) {
			System.out.println("CONEXION OK!!");
		} else {
			redactarLogServidorln("CONEXION SQL INVALIDA!!");
			// System.exit(1);
			return;
		}
		System.out.println("Creando el Servidor ...");
		Mundo.crearServidor();
		new IniciarSincronizacion();
		new ServidorServer();
		if (ACTIVAR_CONSOLA) {
			new Consola();
		}
		System.out.println("Esperando que los jugadores se conecten");
	}
	
	private static void leyendoIpsPermitidas() {
		final String url = "http://bustofus-fenix.com/clientes/ips.txt";
		URL obj;
		try {
			obj = new URL(url);
			final URLConnection con = obj.openConnection();
			con.setRequestProperty("Content-type", "charset=Unicode");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			Charset utf8charset = Charset.forName("UTF-8");
			while ((inputLine = in.readLine()) != null) {
				String linea = new String(inputLine.getBytes(), utf8charset);
				IP_PERMTIDAS.add(linea);
			}
			in.close();
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}
	
	public static void modificarParam(String p, String v) {
		try {
			final BufferedReader config = new BufferedReader(new FileReader(ARCHIVO_CONFIG));
			String linea = "";
			StringBuilder str = new StringBuilder();
			boolean tiene = false;
			while ((linea = config.readLine()) != null) {
				if (linea.split("=").length == 1) {
					str.append(linea);
				} else {
					final String param = linea.split("=")[0].trim();
					if (param.equalsIgnoreCase(p)) {
						str.append(param + " = " + v);
						tiene = true;
					} else {
						str.append(linea);
					}
				}
				str.append("\n");
			}
			if (!tiene) {
				str.append(p + " = " + v);
			}
			config.close();
			BufferedWriter mod = new BufferedWriter(new FileWriter(ARCHIVO_CONFIG));
			mod.write(str.toString());
			mod.flush();
			mod.close();
		} catch (Exception e) {}
	}
	
	public static String getConfiguracion() {
		StringBuilder str = new StringBuilder();
		try {
			final BufferedReader config = new BufferedReader(new FileReader(ARCHIVO_CONFIG));
			String linea = "";
			while ((linea = config.readLine()) != null) {
				str.append(linea + "\n");
			}
			config.close();
		} catch (Exception e) {}
		return str.toString();
	}
	
	public static void cargarConfiguracion(Personaje perso) {
		try {
			final BufferedReader config = new BufferedReader(new FileReader(ARCHIVO_CONFIG));
			String linea = "";
			ArrayList<String> parametros = new ArrayList<>();
			Map<String, String> repetidos = new TreeMap<>();
			while ((linea = config.readLine()) != null) {
				try {
					final String parametro = linea.split("=", 2)[0].trim();
					String valor = linea.split("=", 2)[1].trim();
					if (parametros.contains(parametro)) {
						System.out.println("EN EL ARCHIVO " + ARCHIVO_CONFIG + " SE REPITE EL PARAMETRO " + parametro);
						System.exit(1);
						return;
					} else {
						parametros.add(parametro);
					}
					String variable = "";
					valor = valor.replace("\\n", "\n").replace("\\t", "\t").replace("\\r", "\r").replace("\\b", "\b");
					switch (parametro.toUpperCase()) {
						// case "REGISTER_SENDING" :
						// REGISTER_SENDING = valor.equalsIgnoreCase("true");
						// break;
						// case "REGISTER_RECIVED" :
						// REGISTER_RECIVED = valor.equalsIgnoreCase("true");
						// break;
						case "ES_LOCALHOST" :
							ES_LOCALHOST = valor.equalsIgnoreCase("true");
							variable = "ES_LOCALHOST";
							break;
						case "MOSTRAR_ENVIADOS" :
						case "ENVIADOS" :
							MOSTRAR_ENVIOS = valor.equalsIgnoreCase("true");
							variable = "MOSTRAR_ENVIOS";
							break;
						case "MOSTRAR_SINCRONIZADOR" :
						case "SINCRONIZADOS" :
						case "MOSTRAR_SINCRONIZACION" :
							MOSTRAR_SINCRONIZACION = valor.equalsIgnoreCase("true");
							variable = "MOSTRAR_SINCRONIZACION";
							break;
						case "ACTIVAR_CONSOLA" :
							ACTIVAR_CONSOLA = valor.equalsIgnoreCase("true");
							variable = "ACTIVAR_CONSOLA";
							break;
						case "MOSTRAR_RECIBIDOS" :
						case "RECIBIDOS" :
							MOSTRAR_RECIBIDOS = valor.equalsIgnoreCase("true");
							variable = "MOSTRAR_RECIBIDOS";
							break;
						case "MODO_DEBUG" :
							MODO_DEBUG = valor.equalsIgnoreCase("true");
							variable = "MODO_DEBUG";
							break;
						case "INICIO_NIVEL_MONTURA" :
							INICIO_NIVEL_MONTURA = Integer.parseInt(valor);
							variable = "INICIO_NIVEL_MONTURA";
							break;
						case "INICIO_KAMAS" :
							INICIO_KAMAS = Integer.parseInt(valor);
							variable = "INICIO_KAMAS";
							if (INICIO_KAMAS < 0) {
								INICIO_KAMAS = 0;
							}
							break;
						case "INICIO_NIVEL" :
							INICIO_NIVEL = Short.parseShort(valor);
							variable = "INICIO_NIVEL";
							if (INICIO_NIVEL < 1) {
								INICIO_NIVEL = 1;
							}
							break;
						case "INICIO_SET_ID" :
							INICIO_SET_ID = valor;
							variable = "INICIO_SET_ID";
							break;
						case "INICIO_ZAAPS" :
							INICIO_ZAAPS = valor;
							variable = "INICIO_ZAAPS";
							break;
						case "INICIO_OBJETOS" :
							INICIO_OBJETOS = valor;
							variable = "INICIO_OBJETOS";
							break;
						case "INICIO_EMOTES" :
							INICIO_EMOTES = Integer.parseInt(valor);
							variable = "INICIO_EMOTES";
							break;
						case "INICIO_PUNTOS_STATS" :
							INICIO_PUNTOS_STATS = Integer.parseInt(valor);
							variable = "INICIO_PUNTOS_STATS";
							break;
						case "PUNTOS_STAT_POR_NIVEL" :
						case "PUNTOS_STATS_POR_NIVEL" :
							PUNTOS_STATS_POR_NIVEL = Integer.parseInt(valor);
							variable = "PUNTOS_STATS_POR_NIVEL";
							break;
						case "PUNTOS_HECHIZO_POR_NIVEL" :
							PUNTOS_HECHIZO_POR_NIVEL = Integer.parseInt(valor);
							variable = "PUNTOS_HECHIZO_POR_NIVEL";
							break;
						case "KOLISEO_PREMIO_KAMAS" :
						case "KOLISEO_KAMAS" :
							KOLISEO_PREMIO_KAMAS = Integer.parseInt(valor);
							variable = "KOLISEO_PREMIO_KAMAS";
							break;
						case "MISION_PVP_KAMAS" :
							MISION_PVP_KAMAS = Integer.parseInt(valor);
							variable = "MISION_PVP_KAMAS";
							break;
						case "KOLISEO_DIVISOR_XP" :
							KOLISEO_DIVISOR_XP = Integer.parseInt(valor);
							variable = "KOLISEO_DIVISOR_XP";
							break;
						case "KOLISEO_PREMIO_OBJETOS" :
						case "KOLISEO_OBJETOS" :
							KOLISEO_PREMIO_OBJETOS = valor;
							variable = "KOLISEO_PREMIO_OBJETOS";
							break;
						case "MISION_PVP_OBJETOS" :
							MISION_PVP_OBJETOS = valor;
							variable = "MISION_PVP_OBJETOS";
							break;
						case "PARAM_PERMITIR_BONUS_PELEA_AFECTEN_PROSPECCION" :
							PARAM_PERMITIR_BONUS_PELEA_AFECTEN_PROSPECCION = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERMITIR_BONUS_PELEA_AFECTEN_PROSPECCION";
							break;
						case "PERMITIR_BONUS_ESTRELLAS" :
							PARAM_PERMITIR_BONUS_ESTRELLAS = valor.equalsIgnoreCase("true");
							variable = "PERMITIR_BONUS_ESTRELLAS";
							break;
						case "PERMITIR_BONUS_DROP_RETOS" :
							PARAM_PERMITIR_BONUS_DROP_RETOS = valor.equalsIgnoreCase("true");
							variable = "PERMITIR_BONUS_DROP_RETOS";
							break;
						case "PERMITIR_BONUS_EXP_RETOS" :
							PARAM_PERMITIR_BONUS_EXP_RETOS = valor.equalsIgnoreCase("true");
							variable = "PERMITIR_BONUS_EXP_RETOS";
							break;
						case "PARAM_PERMITIR_ADMIN_EN_LADDER" :
							PARAM_PERMITIR_ADMIN_EN_LADDER = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERMITIR_ADMIN_EN_LADDER";
							break;
						case "PARAM_MOVER_MULTIPLE_OBJETOS_SOLO_ABONADOS" :
							PARAM_MOVER_MULTIPLE_OBJETOS_SOLO_ABONADOS = valor.equalsIgnoreCase("true");
							variable = "PARAM_MOVER_MULTIPLE_OBJETOS_SOLO_ABONADOS";
							break;
						case "PARAM_EXP_PVP_MISION_POR_TABLA" :
							PARAM_EXP_PVP_MISION_POR_TABLA = valor.equalsIgnoreCase("true");
							variable = "PARAM_EXP_PVP_MISION_POR_TABLA";
							break;
						case "FACTOR_ZERO_DROP" :
							FACTOR_ZERO_DROP = Integer.parseInt(valor);
							variable = "FACTOR_ZERO_DROP";
							break;
						case "RATE_FM" :
							// case "DIFICULTAD_FM" :
							RATE_FM = Integer.parseInt(valor);
							variable = "RATE_FM";
							break;
						case "RATE_KAMAS" :
							RATE_KAMAS = Integer.parseInt(valor);
							variable = "RATE_KAMAS";
							DEFECTO_KAMAS = RATE_KAMAS;
							break;
						case "RATE_HONOR" :
							RATE_HONOR = Integer.parseInt(valor);
							variable = "RATE_HONOR";
							DEFECTO_XP_HONOR = RATE_HONOR;
							break;
						case "RATE_XP_OFICIO" :
							RATE_XP_OFICIO = Integer.parseInt(valor);
							variable = "RATE_XP_OFICIO";
							DEFECTO_XP_OFICIO = RATE_XP_OFICIO;
							break;
						case "RATE_PVM" :
						case "RATE_XP_PVM" :
							RATE_XP_PVM = Integer.parseInt(valor);
							variable = "RATE_XP_PVM";
							DEFECTO_XP_PVM = RATE_XP_PVM;
							break;
						case "RATE_XP_MONTURA" :
							RATE_XP_MONTURA = Integer.parseInt(valor);
							variable = "RATE_XP_MONTURA";
							break;
						case "RATE_XP_PERCO" :
						case "RATE_XP_RECAUDADOR" :
							RATE_XP_RECAUDADOR = Integer.parseInt(valor);
							variable = "RATE_XP_RECAUDADOR";
							break;
						case "RATE_PVP" :
						case "RATE_XP_PVP" :
							RATE_XP_PVP = Integer.parseInt(valor);
							variable = "RATE_XP_PVP";
							DEFECTO_XP_PVP = RATE_XP_PVP;
							break;
						case "RATE_DROP_PORC" :
						case "RATE_DROP" :
							RATE_DROP_NORMAL = Integer.parseInt(valor);
							variable = "RATE_DROP_PORC";
							DEFECTO_DROP = RATE_DROP_NORMAL;
							break;
						case "RATE_XP_PVM_ABONADOS" :
							RATE_XP_PVM_ABONADOS = Integer.parseInt(valor);
							variable = "RATE_XP_PVM_ABONADOS";
							break;
						case "RATE_DROP_ABONADOS" :
							RATE_DROP_ABONADOS = Integer.parseInt(valor);
							variable = "RATE_DROP_ABONADOS";
							break;
						case "RATE_KAMAS_ABONADOS" :
							RATE_KAMAS_ABONADOS = Integer.parseInt(valor);
							variable = "RATE_KAMAS_ABONADOS";
							break;
						case "RATE_XP_OFICIO_ABONADOS" :
							RATE_XP_OFICIO_ABONADOS = Integer.parseInt(valor);
							variable = "RATE_XP_OFICIO_ABONADOS";
							break;
						case "RATE_PODS" :
							RATE_PODS = Integer.parseInt(valor);
							variable = "RATE_PODS";
							break;
						case "RATE_CONQUISTA_EXPERIENCIA" :
							RATE_CONQUISTA_EXPERIENCIA = Integer.parseInt(valor);
							variable = "RATE_CONQUISTA_EXPERIENCIA";
							break;
						case "RATE_CONQUISTA_RECOLECTA" :
							RATE_CONQUISTA_RECOLECTA = Integer.parseInt(valor);
							variable = "RATE_CONQUISTA_RECOLECTA";
							break;
						case "RATE_CONQUISTA_DROP" :
							RATE_CONQUISTA_DROP = Integer.parseInt(valor);
							variable = "RATE_CONQUISTA_DROP";
							break;
						case "RATE_DROP_ARMAS_ETEREAS" :
							RATE_DROP_ARMAS_ETEREAS = Integer.parseInt(valor);
							variable = "RATE_DROP_ARMAS_ETEREAS";
							break;
						case "RATE_CRIANZA_MONTURAS" :
						case "RATE_CRIANZA_MONTURA" :
						case "RATE_CRIANZA_PAVOS" :
							RATE_CRIANZA_MONTURA = Integer.parseInt(valor);
							variable = "RATE_CRIANZA_MONTURA";
							DEFECTO_CRIANZA_MONTURA = RATE_CRIANZA_MONTURA;
							break;
						case "RATE_CAPTURA_MONTURAS" :
						case "RATE_CAPTURA_MONTURA" :
						case "RATE_CAPTURA_PAVOS" :
							RATE_CAPTURA_MONTURA = Integer.parseInt(valor);
							variable = "RATE_CAPTURA_MONTURA";
							break;
						case "MENSAJE_BIENVENIDA_1" :
						case "MENSAJE_BIENVENIDA" :
							MENSAJE_BIENVENIDA = valor;
							variable = "MENSAJE_BIENVENIDA";
							break;
						case "PANEL_BIENVENIDA_1" :
						case "PANEL_BIENVENIDA" :
							PANEL_BIENVENIDA = valor;
							variable = "PANEL_BIENVENIDA";
							break;
						case "PANEL_CREAR_PJ" :
							PANEL_DESPUES_CREAR_PERSONAJE = valor;
							variable = "PANEL_DESPUES_CREAR_PJ";
							break;
						case "MESSAGE_COMMANDS" :
						case "MENSAJE_COMANDOS" :
							MENSAJE_COMANDOS = valor;
							variable = "MENSAJE_COMANDOS";
							break;
						case "MESSAGE_SERVICIES" :
						case "MENSAJE_SERVICIOS" :
							MENSAJE_SERVICIOS = valor;
							variable = "MENSAJE_SERVICIOS";
							break;
						case "MENSAJE_ERROR_OGRINAS_CREAR_CLASE" :
							MENSAJE_ERROR_OGRINAS_CREAR_CLASE = valor;
							variable = "MENSAJE_ERROR_OGRINAS_CREAR_CLASE";
							break;
						case "MESSAGE_VIP" :
						case "MENSAJE_VIP" :
							MENSAJE_VIP = valor;
							variable = "MENSAJE_VIP";
							break;
						case "TUTORIAL_FR" :
							TUTORIAL_FR = valor;
							variable = "TUTORIAL_FR";
							break;
						case "TUTORIAL_ES" :
							TUTORIAL_ES = valor;
							variable = "TUTORIAL_ES";
							break;
						case "PUBLICIDAD_1" :
						case "PUBLICIDAD_2" :
						case "PUBLICIDAD_3" :
						case "PUBLICIDAD_4" :
						case "PUBLICIDAD_5" :
							PUBLICIDAD.add(valor);
							break;
						case "MAPAS_KOLISEO" :
							MAPAS_KOLISEO = valor;
							variable = "MAPAS_KOLISEO";
							break;
						case "PUERTO_SERVER" :
						case "PUERTO_SERVIDOR" :
							PUERTO_SERVIDOR = Integer.parseInt(valor);
							variable = "PUERTO_SERVIDOR";
							break;
						case "PUERTO_SINCRONIZACION" :
						case "PUERTO_SINCRONIZADOR" :
							PUERTO_SINCRONIZADOR = Integer.parseInt(valor);
							variable = "PUERTO_SINCRONIZADOR";
							break;
						case "SERVIDOR_PRIORIDAD" :
							SERVIDOR_PRIORIDAD = Integer.parseInt(valor);
							variable = "SERVIDOR_PRIORIDAD";
							break;
						case "IP_PUBLIC_SERVER" :
						case "IP_SERVIDOR_PUBLICA" :
						case "IP_SERVIDOR_FIJA" :
						case "IP_FIJA_SERVIDOR" :
						case "IP_FIX_SERVER" :
						case "IP_PUBLICA_SERVIDOR" :
							variable = "IP_PUBLICA_SERVIDOR";
							if (IP_PERMTIDAS.contains(valor) || IP_PERMTIDAS.contains("*")) {
								IP_PUBLICA_SERVIDOR = valor;
							}
							break;
						case "IP_MULTISERVIDOR" :
						case "IP_MULTISERVER" :
							variable = "IP_MULTISERVIDOR";
							for (String s : valor.split(";")) {
								if (s.isEmpty()) {
									continue;
								}
								if (IP_PERMTIDAS.contains(s) || IP_PERMTIDAS.contains("*")) {
									try {
										InetAddress in = InetAddress.getByName(s);
										IP_MULTISERVIDOR.add(in.getHostAddress());
									} catch (Exception e) {
										IP_MULTISERVIDOR.add(s);
									}
								}
							}
							break;
						case "DB_HOST" :
						case "BD_HOST" :
							BD_HOST = valor;
							variable = "BD_HOST";
							break;
						case "DB_USER" :
						case "BD_USER" :
						case "BD_USUARIO" :
							BD_USUARIO = valor;
							variable = "BD_USUARIO";
							break;
						case "DB_PASSWORD" :
						case "DB_PASS" :
						case "BD_PASSWORD" :
						case "BD_CONTRASEÑA" :
						case "BD_PASS" :
							BD_PASS = valor;
							variable = "BD_PASS";
							break;
						case "DB_STATIC" :
						case "BD_STATIC" :
						case "BD_STATIQUE" :
						case "BD_FIJA" :
						case "BD_LUIS" :
							BD_ESTATICA = valor;
							variable = "BD_ESTATICA";
							break;
						case "DB_DYNAMIC" :
						case "BD_TANIA" :
						case "BD_DYNAMIC" :
						case "BD_DINAMICA" :
						case "BD_OTHERS" :
							BD_DINAMICA = valor;
							variable = "BD_DINAMICA";
							break;
						case "DB_ACCOUNTS" :
						case "BD_ACCOUNTS" :
						case "BD_COMPTES" :
						case "BD_CUENTAS" :
						case "BD_LOGIN" :
						case "BD_REALM" :
							BD_CUENTAS = valor;
							variable = "BD_CUENTAS";
							break;
						case "NAME_SERVER" :
						case "NOMBRE_SERVIDOR" :
						case "NOMBRE_SERVER" :
							NOMBRE_SERVER = valor;
							variable = "NOMBRE_SERVER";
							break;
						case "URL_IMAGEN_VOTO" :
							URL_IMAGEN_VOTO = valor;
							variable = "URL_IMAGEN_VOTO";
							break;
						case "URL_LINK_VOTE" :
						case "URL_LINK_VOTO" :
							URL_LINK_VOTO = valor;
							variable = "URL_LINK_VOTO";
							break;
						case "URL_LINK_COMPRA" :
							URL_LINK_COMPRA = valor;
							variable = "URL_LINK_COMPRA";
							break;
						case "URL_BACKUP_PHP" :
							URL_BACKUP_PHP = valor;
							variable = "URL_BACKUP_PHP";
							break;
						case "URL_DETECTAR_DDOS" :
							URL_DETECTAR_DDOS = valor;
							variable = "URL_DETECTAR_DDOS";
							break;
						case "URL_REPORT_BUG" :
						case "URL_LINK_BUG" :
							URL_LINK_BUG = valor;
							variable = "URL_LINK_BUG";
							break;
						case "URL_LINK_MP3" :
							URL_LINK_MP3 = valor;
							variable = "URL_LINK_MP3";
							break;
						case "DIRECTORIO_MP3" :
						case "DIRECTORIO_LOCAL_MP3" :
							DIRECTORIO_LOCAL_MP3 = valor;
							variable = "DIRECTORIO_LOCAL_MP3";
							break;
						case "COMMANDS_AUTHORIZATE" :
						case "COMMANDS_PLAYER" :
						case "COMANDOS_AUTORIZADOS" :
						case "COMANDOS_JUGADOR" :
						case "COMANDOS_PERMITIDOS" :
							variable = "COMANDOS_PERMITIDOS";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								COMANDOS_PERMITIDOS.add(s);
							}
							break;
						case "COMMANDS_VIP" :
						case "COMMANDS_BOUTIQUE" :
						case "COMANDOS_ABONADO" :
						case "COMANDOS_VIP" :
							variable = "COMANDOS_VIP";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								COMANDOS_VIP.add(s);
							}
							break;
						case "SALVAR_LOGS_TIPO_PELEA" :
						case "SALVAR_LOGS_TIPO_COMBATE" :
							variable = "SALVAR_LOGS_TIPO_COMBATE";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									SALVAR_LOGS_TIPO_COMBATE.add(Byte.parseByte(s));
								} catch (Exception e) {}
							}
							break;
						case "CANALES_COLOR_CHAT" :
						case "COLORES_CANALES_CHAT" :
						case "COLOR_CHAT" :
							CANALES_COLOR_CHAT = valor;
							variable = "CANALES_COLOR_CHAT";
							break;
						// case "COLOR_CHAT_ALL" :
						// COLOR_CHAT_ALL = valor;
						// variable = "COLOR_CHAT_ALL";
						// break;
						case "PERMITIR_MULTIMAN_TIPO_PELEA" :
						case "PERMITIR_MULTIMAN_TIPO_COMBATE" :
							PERMITIR_MULTIMAN = valor;
							variable = "PERMITIR_MULTIMAN";
							break;
						case "PALABRA_CLAVE_CONSOLA" :
							PALABRA_CLAVE_CONSOLA = valor;
							variable = "PALABRA_CLAVE_CONSOLA";
							break;
						case "SISTEMA_ITEMS_TIPO_DE_PAGO" :
						case "PANEL_ITEMS_TIPO_DE_PAGO" :
							SISTEMA_ITEMS_TIPO_DE_PAGO = valor.toUpperCase();
							variable = "SISTEMA_ITEMS_TIPO_DE_PAGO";
							break;
						case "INICIO_MAPA_CELDA" :
						case "START_MAPA_CELDA" :
						case "RETURN_MAPA_CELDA" :
							START_MAPA_CELDA = valor;
							variable = "START_MAPA_CELDA";
							break;
						case "ENCLO_MAPA_CELDA" :
						case "ENCLOS_MAPA_CELDA" :
						case "CERCADO_MAPA_CELDA" :
						case "CERCADOS_MAPA_CELDA" :
							CERCADO_MAPA_CELDA = valor;
							variable = "CERCADO_MAPA_CELDA";
							break;
						case "TIENDA_MAPA_CELDA" :
						case "SHOP_MAPA_CELDA" :
							SHOP_MAPA_CELDA = valor;
							variable = "SHOP_MAPA_CELDA";
							break;
						case "PVP_MAPA_CELDA" :
							PVP_MAPA_CELDA = valor;
							variable = "PVP_MAPA_CELDA";
							break;
						case "NIVEL_IA" :
						case "LEVEL_PROCCESS_IA" :
						case "NIVEL_PROCESAMIENTO_IA" :
						case "NIVEL_EJECUCION_IA" :
						case "NIVEL_INTELIGENCIA_ARTIFICIAL" :
							NIVEL_INTELIGENCIA_ARTIFICIAL = Integer.parseInt(valor);
							variable = "NIVEL_INTELIGENCIA_ARTIFICIAL";
							break;
						case "VECES_PARA_BAN_IP_SIN_ESPERA" :
							VECES_PARA_BAN_IP_SIN_ESPERA = Integer.parseInt(valor);
							variable = "VECES_PARA_BAN_IP_SIN_ESPERA";
							break;
						case "SEGUNDOS_CANAL_COMERCIO" :
							SEGUNDOS_CANAL_COMERCIO = Integer.parseInt(valor);
							variable = "SEGUNDOS_CANAL_COMERCIO";
							break;
						case "SEGUNDOS_CANAL_RECLUTAMIENTO" :
							SEGUNDOS_CANAL_RECLUTAMIENTO = Integer.parseInt(valor);
							variable = "SEGUNDOS_CANAL_RECLUTAMIENTO";
							break;
						case "SEGUNDOS_CANAL_ALINEACION" :
							SEGUNDOS_CANAL_ALINEACION = Integer.parseInt(valor);
							variable = "SEGUNDOS_CANAL_ALINEACION";
							break;
						case "SEGUNDOS_CANAL_INCARNAM" :
							SEGUNDOS_CANAL_INCARNAM = Integer.parseInt(valor);
							variable = "SEGUNDOS_CANAL_INCARNAM";
							break;
						case "SEGUNDOS_CANAL_ALL" :
							SEGUNDOS_CANAL_ALL = Integer.parseInt(valor);
							variable = "SEGUNDOS_CANAL_ALL";
							break;
						case "SEGUNDOS_CANAL_VIP" :
							SEGUNDOS_CANAL_VIP = Integer.parseInt(valor);
							variable = "SEGUNDOS_CANAL_VIP";
							break;
						case "SEGUNDOS_TURNO_PELEA" :
							SEGUNDOS_TURNO_PELEA = Integer.parseInt(valor);
							variable = "SEGUNDOS_TURNO_PELEA";
							break;
						case "SEGUNDOS_ARENA" :
							SEGUNDOS_ARENA = Integer.parseInt(valor);
							variable = "SEGUNDOS_ARENA";
							break;
						case "SEGUNDOS_ENTRE_DESAFIOS_PJ" :
							SEGUNDOS_ENTRE_DESAFIOS_PJ = Integer.parseInt(valor);
							variable = "SEGUNDOS_ENTRE_DESAFIOS_PJ";
							break;
						case "SEGUNDOS_INACTIVIDAD" :
							SEGUNDOS_INACTIVIDAD = Integer.parseInt(valor);
							variable = "SEGUNDOS_INACTIVIDAD";
							break;
						case "SEGUNDOS_TRANSACCION_BD" :
							SEGUNDOS_TRANSACCION_BD = Integer.parseInt(valor);
							variable = "SEGUNDOS_TRANSACCION_BD";
							break;
						case "SEGUNDOS_SALVAR" :
							SEGUNDOS_SALVAR = Integer.parseInt(valor);
							variable = "SEGUNDOS_SALVAR";
							break;
						case "SEGUNDOS_REBOOT_SERVER" :
						case "SEGUNDOS_RESET_SERVER" :
						case "SEGUNDOS_REBOOT" :
						case "SEGUNDOS_RESET" :
							SEGUNDOS_REBOOT_SERVER = Integer.parseInt(valor);
							variable = "SEGUNDOS_REBOOT_SERVER";
							break;
						case "SEGUNDOS_DETECTAR_DDOS" :
							SEGUNDOS_DETECTAR_DDOS = Integer.parseInt(valor);
							variable = "SEGUNDOS_DETECTAR_DDOS";
							break;
						case "SEGUNDOS_VOLVER_APARECER_MOBS" :
						case "SEGUNDOS_REAPARECER_GRUPO_MOBS" :
						case "SEGUNDOS_REAPARECER_MOBS" :
							SEGUNDOS_REAPARECER_MOBS = Integer.parseInt(valor);
							variable = "SEGUNDOS_REAPARECER_MOBS";
							break;
						case "SEGUNDOS_AGREDIR_RECIEN_LLEGADO_MAPA" :
							SEGUNDOS_AGREDIR_RECIEN_LLEGADO_MAPA = Integer.parseInt(valor);
							variable = "SEGUNDOS_AGREDIR_RECIEN_LLEGADO_MAPA";
							break;
						case "SEGUNDOS_LIMPIAR_MEMORIA" :
							SEGUNDOS_LIMPIAR_MEMORIA = Integer.parseInt(valor);
							variable = "SEGUNDOS_LIMPIAR_MEMORIA";
							break;
						case "SEGUNDOS_RESET_RATES" :
							SEGUNDOS_RESET_RATES = Integer.parseInt(valor);
							variable = "SEGUNDOS_RESET_RATES";
							break;
						case "MINUTOS_VALIDAR_VOTO" :
						case "MINUTOS_SIGUIENTE_VOTO" :
						case "MINUTOS_SIG_VOTO" :
							MINUTOS_VALIDAR_VOTO = Integer.parseInt(valor);
							variable = "MINUTOS_VALIDAR_VOTO";
							break;
						case "MINUTOS_PENALIZACION_KOLISEO" :
							MINUTOS_PENALIZACION_KOLISEO = Integer.parseInt(valor);
							variable = "MINUTOS_PENALIZACION_KOLISEO";
							break;
						case "MINUTOS_SPAMEAR_BOTON_VOTO" :
							MINUTOS_SPAMEAR_BOTON_VOTO = Integer.parseInt(valor);
							variable = "MINUTOS_SPAMEAR_BOTON_VOTO";
							break;
						case "MILISEGUNDOS_CERRAR_SERVIDOR" :
							MILISEGUNDOS_CERRAR_SERVIDOR = Integer.parseInt(valor);
							variable = "MILISEGUNDOS_CERRAR_SERVIDOR";
							break;
						case "SEGUNDOS_LIVE_ACTION" :
							SEGUNDOS_LIVE_ACTION = Integer.parseInt(valor);
							variable = "SEGUNDOS_LIVE_ACTION";
							break;
						case "SEGUNDOS_PUBLICIDAD" :
							SEGUNDOS_PUBLICIDAD = Integer.parseInt(valor);
							variable = "SEGUNDOS_PUBLICIDAD";
							break;
						case "SEGUNDOS_ESTRELLAS_GRUPO_MOBS" :
						case "SEGUNDOS_ESTRELLAS_MOBS" :
							SEGUNDOS_ESTRELLAS_GRUPO_MOBS = Integer.parseInt(valor);
							variable = "SEGUNDOS_ESTRELLAS_GRUPO_MOBS";
							break;
						case "SEGUNDOS_ESTRELLAS_RECURSOS" :
							SEGUNDOS_ESTRELLAS_RECURSOS = Integer.parseInt(valor);
							variable = "SEGUNDOS_ESTRELLAS_RECURSOS";
							break;
						case "SEGUNDOS_INICIAR_KOLISEO" :
							SEGUNDOS_INICIAR_KOLISEO = Integer.parseInt(valor);
							variable = "SEGUNDOS_INICIAR_KOLISEO";
							break;
						case "MINUTOS_ALIMENTACION_MASCOTA" :
						case "MINUTOS_ALIMENTACION" :
							MINUTOS_ALIMENTACION_MASCOTA = Integer.parseInt(valor);
							variable = "MINUTOS_ALIMENTACION_MASCOTA";
							break;
						case "MINUTOS_MISION_PVP" :
							MINUTOS_MISION_PVP = Integer.parseInt(valor);
							variable = "MINUTOS_MISION_PVP";
							break;
						case "MINUTOS_GESTACION_MONTURA" :
						case "MINUTOS_PARIR" :
						case "MINUTOS_PARIR_MONTURA" :
							MINUTOS_GESTACION_MONTURA = Integer.parseInt(valor);
							variable = "MINUTOS_GESTACION_MONTURA";
							break;
						case "SEGUNDOS_MOVER_RECAUDADOR" :
							SEGUNDOS_MOVER_RECAUDADOR = Integer.parseInt(valor);
							variable = "SEGUNDOS_MOVER_RECAUDADOR";
							break;
						case "SEGUNDOS_MOVER_MONTURAS" :
						case "SEGUNDOS_MOVERSE_MONTURAS" :
						case "SEGUNDOS_MOVERSE_PAVOS" :
							SEGUNDOS_MOVER_MONTURAS = Integer.parseInt(valor);
							variable = "SEGUNDOS_MOVER_MONTURAS";
							break;
						case "SEGUNDOS_MOVER_GRUPO_MOBS" :
						case "SEGUNDOS_MOVER_MOBS" :
						case "SEGUNDOS_MOVERSE_MOBS" :
							SEGUNDOS_MOVER_GRUPO_MOBS = Integer.parseInt(valor);
							variable = "SEGUNDOS_MOVER_GRUPO_MOBS";
							break;
						case "HORAS_VOLVER_A_PONER_RECAUDADOR_MAPA" :
							HORAS_VOLVER_A_PONER_RECAUDADOR_MAPA = Integer.parseInt(valor);
							variable = "HORAS_VOLVER_A_PONER_RECAUDADOR_MAPA";
							break;
						case "HORAS_PERDER_CRIAS_MONTURA" :
							HORAS_PERDER_CRIAS_MONTURA = Integer.parseInt(valor);
							variable = "HORAS_PERDER_CRIAS_MONTURA";
							break;
						case "MIN_NIVEL_KOLISEO" :
							MIN_NIVEL_KOLISEO = Short.parseShort(valor);
							variable = "MIN_NIVEL_KOLISEO";
							break;
						case "LIMITE_SCROLL" :
						case "MAX_SCROLL" :
							LIMITE_SCROLL = Short.parseShort(valor);
							variable = "LIMITE_SCROLL";
							break;
						case "MAX_MIEMBROS_GREMIO" :
						case "LIMITE_MIEMBROS_GREMIO" :
							LIMITE_MIEMBROS_GREMIO = Integer.parseInt(valor);
							variable = "LIMITE_MIEMBROS_GREMIO";
							break;
						case "LIMITE_OBJETOS_COFRE" :
						case "MAX_OBJETOS_COFRE" :
						case "LIMITE_MAX_OBJETOS_COFRE" :
							LIMITE_OBJETOS_COFRE = Integer.parseInt(valor);
							variable = "LIMITE_OBJETOS_COFRE";
							break;
						case "ID_BOLSA_CREDITOS" :
							ID_BOLSA_CREDITOS = Integer.parseInt(valor);
							variable = "ID_BOLSA_CREDITOS";
							break;
						case "ID_BOLSA_OGRINAS" :
							ID_BOLSA_OGRINAS = Integer.parseInt(valor);
							variable = "ID_BOLSA_OGRINAS";
							break;
						case "IMPUESTO_BOLSA_OGRINAS" :
							IMPUESTO_BOLSA_OGRINAS = Integer.parseInt(valor);
							variable = "IMPUESTO_BOLSA_OGRINAS";
							break;
						case "DURABILIDAD_REDUCIR_OBJETO_CRIA" :
							DURABILIDAD_REDUCIR_OBJETO_CRIA = Integer.parseInt(valor);
							variable = "DURABILIDAD_REDUCIR_OBJETO_CRIA";
							break;
						case "IMPUESTO_BOLSA_CREDITOS" :
							IMPUESTO_BOLSA_CREDITOS = Integer.parseInt(valor);
							variable = "IMPUESTO_BOLSA_CREDITOS";
							break;
						case "LIMITE_LADDER" :
							LIMITE_LADDER = Short.parseShort(valor);
							variable = "LIMITE_LADDER";
							break;
						case "LIMITE_MOSTRAR_REPORTES" :
						case "MAX_MOSTRAR_REPORTES" :
						case "MAX_REPORTES" :
						case "LIMIT_REPORTES" :
							LIMITE_REPORTES = Short.parseShort(valor);
							variable = "LIMITE_REPORTES";
							break;
						case "MAX_MISIONES_ALMANAX" :
							MAX_MISIONES_ALMANAX = Short.parseShort(valor);
							variable = "MAX_MISIONES_ALMANAX";
							break;
						case "COLOR_CASES_PLAYER" :
						case "COLOR_CASES_FIGHT_AGRESSOR" :
						case "COLOR_CELDAS_PELEA_AGRESOR" :
							COLOR_CELDAS_PELEA_AGRESOR = valor.toLowerCase();
							variable = "COLOR_CELDAS_PELEA_AGRESOR";
							break;
						case "MAX_RECAUDADORES_POR_ZONA" :
							MAX_RECAUDADORES_POR_ZONA = Integer.parseInt(valor);
							variable = "MAX_RECAUDADORES_POR_ZONA";
							break;
						case "MAX_BONUS_STARS_MOB" :
						case "MAX_BONUS_STARS_MOBS" :
						case "MAX_BONUS_ESTRELLAS_MOBS" :
							MAX_BONUS_ESTRELLAS_MOBS = Short.parseShort(valor);
							variable = "MAX_BONUS_ESTRELLAS_MOBS";
							break;
						case "MAX_BONUS_STARS_RESSOURCES" :
						case "MAX_BONUS_ESTRELLAS_RECURSO" :
						case "MAX_BONUS_ESTRELLAS_RECURSOS" :
							MAX_BONUS_ESTRELLAS_RECURSOS = Short.parseShort(valor);
							variable = "MAX_BONUS_ESTRELLAS_RECURSOS";
							break;
						// la diferencia q uno es x20
						case "MAX_STARS_MOBS" :
						case "MAX_ESTRELLAS_MOB" :
						case "MAX_ESTRELLAS_MOBS" :
							MAX_BONUS_ESTRELLAS_MOBS = (short) (Short.parseShort(valor) * 20);
							variable = "MAX_BONUS_ESTRELLAS_MOBS";
							break;
						case "MAX_STARS_RESSOURCES" :
						case "MAX_STARS_RECURSOS" :
						case "MAX_ESTRELLAS_RECURSO" :
						case "MAX_ESTRELLAS_RECURSOS" :
							MAX_BONUS_ESTRELLAS_RECURSOS = (short) (Short.parseShort(valor) * 20);
							variable = "MAX_BONUS_ESTRELLAS_RECURSOS";
							break;
						case "INICIO_BONUS_ESTRELLAS_MOBS" :
							INICIO_BONUS_ESTRELLAS_MOBS = Short.parseShort(valor);
							variable = "INICIO_BONUS_ESTRELLAS_MOBS";
							break;
						case "INICIO_ESTRELLAS_MOBS" :
							INICIO_BONUS_ESTRELLAS_MOBS = (short) (Short.parseShort(valor) * 20);
							variable = "INICIO_BONUS_ESTRELLAS_MOBS";
							break;
						case "INICIO_BONUS_ESTRELLAS_RECURSOS" :
							INICIO_BONUS_ESTRELLAS_RECURSOS = Short.parseShort(valor);
							variable = "INICIO_BONUS_ESTRELLAS_RECURSOS";
							break;
						case "INICIO_ESTRELLAS_RECURSOS" :
							INICIO_BONUS_ESTRELLAS_RECURSOS = (short) (Short.parseShort(valor) * 20);
							variable = "INICIO_BONUS_ESTRELLAS_RECURSOS";
							break;
						case "NIVEL_MAX_PERSONAJE" :
							NIVEL_MAX_PERSONAJE = Short.parseShort(valor);
							variable = "NIVEL_MAX_PERSONAJE";
							break;
						case "NIVEL_MAX_ESCOGER_NIVEL" :
							NIVEL_MAX_ESCOGER_NIVEL = Short.parseShort(valor);
							variable = "NIVEL_MAX_ESCOGER_NIVEL";
							break;
						case "NIVEL_MAX_MONTURA" :
						case "NIVEL_MAX_DRAGOPAVO" :
							NIVEL_MAX_MONTURA = Short.parseShort(valor);
							variable = "NIVEL_MAX_MONTURA";
							break;
						case "NIVEL_MAX_GREMIO" :
							NIVEL_MAX_GREMIO = Short.parseShort(valor);
							variable = "NIVEL_MAX_GREMIO";
							break;
						case "NIVEL_MAX_ENCARNACION" :
							NIVEL_MAX_ENCARNACION = Short.parseShort(valor);
							variable = "NIVEL_MAX_ENCARNACION";
							break;
						case "NIVEL_MAX_ALINEACION" :
							NIVEL_MAX_ALINEACION = Byte.parseByte(valor);
							variable = "NIVEL_MAX_ALINEACION";
							break;
						case "MAX_PESO_POR_STAT" :
							MAX_PESO_POR_STAT = Integer.parseInt(valor);
							variable = "MAX_PESO_POR_STAT";
							break;
						case "MAX_RESET" :
						case "MAX_RESETS" :
							MAX_RESETS = Integer.parseInt(valor);
							variable = "MAX_RESETS";
							break;
						case "MAX_CAC_POR_TURNO" :
							MAX_CAC_POR_TURNO = Integer.parseInt(valor);
							variable = "MAX_CAC_POR_TURNO";
							break;
						case "PRECIO_LOTERIA" :
							PRECIO_LOTERIA = Short.parseShort(valor);
							variable = "PRECIO_LOTERIA";
							break;
						case "PREMIO_LOTERIA" :
							PREMIO_LOTERIA = Short.parseShort(valor);
							variable = "PREMIO_LOTERIA";
							break;
						case "DIAS_PARA_BORRAR" :
							DIAS_PARA_BORRAR = Short.parseShort(valor);
							variable = "DIAS_PARA_BORRAR";
							break;
						case "PARAM_VIP_CRAFT_SPEED" :
							PARAM_VIP_CRAFT_SPEED = valor.equalsIgnoreCase("true");
							variable = "PARAM_VIP_CRAFT_SPEED";
							break;
						case "PARAM_CORREGIR_NOMBRE_JUGADOR" :
							PARAM_CORREGIR_NOMBRE_JUGADOR = valor.equalsIgnoreCase("true");
							variable = "PARAM_CORREGIR_NOMBRE_JUGADOR";
							break;
						case "PARAM_REINICIAR_ESTRELLAS_SI_LLEGA_MAX" :
						case "RESETEAR_ESTRELLAS" :
							PARAM_REINICIAR_ESTRELLAS_SI_LLEGA_MAX = valor.equalsIgnoreCase("true");
							variable = "PARAM_REINICIAR_ESTRELLAS_SI_LLEGA_MAX";
							break;
						case "PARAM_ANTI_FLOOD" :
						case "PARAM_ANTIFLOOD" :
							PARAM_ANTIFLOOD = valor.equalsIgnoreCase("true");
							variable = "PARAM_ANTIFLOOD";
							break;
						case "PARAM_LIMITAR_RECAUDADOR_GREMIO_POR_ZONA" :
							PARAM_LIMITAR_RECAUDADOR_GREMIO_POR_ZONA = valor.equalsIgnoreCase("true");
							variable = "PARAM_LIMITAR_RECAUDADOR_GREMIO_POR_ZONA";
							break;
						case "RESETEAR_LUPEAR_OBJETOS_MAGUEADOS" :
							PARAM_RESETEAR_LUPEAR_OBJETOS_MAGUEADOS = valor.equalsIgnoreCase("true");
							variable = "RESETEAR_LUPEAR_OBJETOS_MAGUEADOS";
							break;
						case "NPC_BOUTIQUE" :
							ID_NPC_BOUTIQUE = Short.parseShort(valor);
							variable = "ID_NPC_BOUTIQUE";
							break;
						case "PRECIO_SISTEMA_RECURSO" :
						case "PRECIO_RECURSO" :
							PRECIO_SISTEMA_RECURSO = Float.parseFloat(valor);
							variable = "PRECIO_SISTEMA_RECURSO";
							break;
						case "FACTOR_OBTENER_RUNAS" :
							FACTOR_OBTENER_RUNAS = Float.parseFloat(valor);
							variable = "FACTOR_OBTENER_RUNAS";
							break;
						case "FACTOR_PLUS_PP_PARA_DROP" :
							FACTOR_PLUS_PP_PARA_DROP = Float.parseFloat(valor);
							variable = "FACTOR_PLUS_PP_PARA_DROP";
							break;
						case "FACTOR_DEVOLVER_OGRINAS" :
							FACTOR_DEVOLVER_OGRINAS = Float.parseFloat(valor);
							variable = "FACTOR_DEVOLVER_OGRINAS";
							break;
						case "OGRINAS_CREAR_CLASE" :
							variable = "OGRINAS_CREAR_CLASE";
							for (final String s : valor.split(";")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									final String[] stat = s.split(",");
									OGRINAS_CREAR_CLASE.put(Byte.parseByte(stat[0]), Integer.parseInt(stat[1]));
								} catch (final Exception e) {}
							}
							break;
						case "GFX_CREA_TU_ITEM_CAPAS" :
							GFX_CREA_TU_ITEM_CAPAS = valor;
							variable = "GFX_CREA_TU_ITEM_CAPAS";
							break;
						case "GFX_CREA_TU_ITEM_AMULETOS" :
							GFX_CREA_TU_ITEM_AMULETOS = valor;
							variable = "GFX_CREA_TU_ITEM_AMULETOS";
							break;
						case "GFX_CREA_TU_ITEM_ANILLOS" :
							GFX_CREA_TU_ITEM_ANILLOS = valor;
							variable = "GFX_CREA_TU_ITEM_ANILLOS";
							break;
						case "GFX_CREA_TU_ITEM_CINTURONES" :
							GFX_CREA_TU_ITEM_CINTURONES = valor;
							variable = "GFX_CREA_TU_ITEM_CINTURONES";
							break;
						case "GFX_CREA_TU_ITEM_BOTAS" :
							GFX_CREA_TU_ITEM_BOTAS = valor;
							variable = "GFX_CREA_TU_ITEM_BOTAS";
							break;
						case "GFX_CREA_TU_ITEM_SOMBREROS" :
							GFX_CREA_TU_ITEM_SOMBREROS = valor;
							variable = "GFX_CREA_TU_ITEM_SOMBREROS";
							break;
						case "GFX_CREA_TU_ITEM_ESCUDOS" :
							GFX_CREA_TU_ITEM_SOMBREROS = valor;
							variable = "GFX_CREA_TU_ITEM_SOMBREROS";
							break;
						case "GFX_CREA_TU_ITEM_DOFUS" :
							GFX_CREA_TU_ITEM_DOFUS = valor;
							variable = "GFX_CREA_TU_ITEM_DOFUS";
							break;
						case "PRECIOS_SERVICIOS" :
						case "PRECIO_SERVICIOS" :
							variable = "PRECIOS_SERVICIOS";
							for (final String s : valor.split(";")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									final String[] stat = s.split(",");
									PRECIOS_SERVICIOS.put(stat[0].toLowerCase(), stat[1].toLowerCase());
								} catch (final Exception e) {}
							}
							break;
						case "MAX_GOLPES_CAC" :
							variable = "MAX_GOLPES_CAC";
							for (final String s : valor.split(";")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									final String[] stat = s.split(",");
									MAX_GOLPES_CAC.put(Integer.parseInt(stat[0]), Integer.parseInt(stat[1]));
								} catch (final Exception e) {}
							}
							break;
						case "LIMITE_STATS_CON_BUFF" :
							variable = "LIMITE_STATS_CON_BUFF";
							for (final String s : valor.split(";")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									final String[] stat = s.split(",");
									LIMITE_STATS_CON_BUFF.put(Integer.parseInt(stat[0]), Integer.parseInt(stat[1]));
								} catch (final Exception e) {}
							}
							break;
						case "LIMITE_STATS_EXO_FM" :
						case "LIMITE_STATS_EXOMAGIA" :
						case "LIMITE_STATS_EXO_FORJAMAGIA" :
							variable = "LIMITE_STATS_EXO_FORJAMAGIA";
							for (final String s : valor.split(";")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									final String[] stat = s.split(",");
									LIMITE_STATS_EXO_FORJAMAGIA.put(Integer.parseInt(stat[0]), Integer.parseInt(stat[1]));
								} catch (final Exception e) {}
							}
							break;
						case "LIMITE_STATS_OVER_FM" :
						case "LIMITE_STATS_OVERMAGIA" :
						case "LIMITE_STATS_OVER_FORJAMAGIA" :
							variable = "LIMITE_STATS_OVER_FORJAMAGIA";
							for (final String s : valor.split(";")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									final String[] stat = s.split(",");
									LIMITE_STATS_OVER_FORJAMAGIA.put(Integer.parseInt(stat[0]), Integer.parseInt(stat[1]));
								} catch (final Exception e) {}
							}
							break;
						case "LIMITE_STATS_SIN_BUFF" :
							variable = "LIMITE_STATS_SIN_BUFF";
							for (final String s : valor.split(";")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									final String[] stat = s.split(",");
									LIMITE_STATS_SIN_BUFF.put(Integer.parseInt(stat[0]), Integer.parseInt(stat[1]));
								} catch (final Exception e) {}
							}
							break;
						case "SERVER_ID" :
							SERVIDOR_ID = Short.parseShort(valor);
							variable = "SERVIDOR_ID";
							break;
						case "ADMIN_ACCESS" :
						case "ACCESO_ADMIN" :
						case "ACCESS_ADMIN" :
						case "ACCESO_ADMIN_MIN" :
							ACCESO_ADMIN_MINIMO = Integer.parseInt(valor);
							variable = "ACCESO_ADMIN_MINIMO";
							break;
						case "PORCENTAJE_DAÑO_NO_CURABLE" :
						case "DAÑO_PERMANENTE" :
							PORCENTAJE_DAÑO_NO_CURABLE = Integer.parseInt(valor);
							variable = "PORCENTAJE_DAÑO_NO_CURABLE";
							if (PORCENTAJE_DAÑO_NO_CURABLE < 0) {
								PORCENTAJE_DAÑO_NO_CURABLE = 0;
							}
							if (PORCENTAJE_DAÑO_NO_CURABLE > 100) {
								PORCENTAJE_DAÑO_NO_CURABLE = 100;
							}
							break;
						case "LIMITE_MAPAS" :
							LIMITE_MAPAS = Short.parseShort(valor);
							variable = "LIMITE_MAPAS";
							break;
						case "LIMITE_DETECTAR_FALLA_KAMAS" :
							LIMITE_DETECTAR_FALLA_KAMAS = Long.parseLong(valor);
							variable = "LIMITE_DETECTAR_FALLA_KAMAS";
							break;
						case "MAX_PJS_POR_CUENTA" :
							MAX_PJS_POR_CUENTA = Integer.parseInt(valor);
							variable = "MAX_PJS_POR_CUENTA";
							break;
						case "BONUS_RESET_PUNTOS_STATS" :
							BONUS_RESET_PUNTOS_STATS = Short.parseShort(valor);
							variable = "BONUS_RESET_PUNTOS_STATS";
							break;
						case "BONUS_RESET_PUNTOS_HECHIZOS" :
							BONUS_RESET_PUNTOS_HECHIZOS = Short.parseShort(valor);
							variable = "BONUS_RESET_PUNTOS_HECHIZOS";
							break;
						case "SISTEMA_ITEMS_EXO_PM_PRECIO" :
						case "PANEL_ITEMS_EXO_PM_PRECIO" :
						case "PANEL_ITEMS_EXO_PM" :
							SISTEMA_ITEMS_EXO_PM_PRECIO = Short.parseShort(valor);
							variable = "SISTEMA_ITEMS_EXO_PM_PRECIO";
							break;
						case "SISTEMA_ITEMS_EXO_PA_PRECIO" :
						case "PANEL_ITEMS_EXO_PA_PRECIO" :
						case "PANEL_ITEMS_EXO_PA" :
							SISTEMA_ITEMS_EXO_PA_PRECIO = Short.parseShort(valor);
							variable = "SISTEMA_ITEMS_EXO_PA_PRECIO";
							break;
						case "SISTEMA_ITEMS_PERFECTO_MULTIPLICA_POR" :
						case "PANEL_ITEMS_PERFECTO_MULTIPLICA_POR" :
							SISTEMA_ITEMS_PERFECTO_MULTIPLICA_POR = Float.parseFloat(valor);
							variable = "SISTEMA_ITEMS_PERFECTO_MULTIPLICA_POR";
							break;
						case "SISTEMA_ITEMS_EXO_TIPOS_NO_PERMITIDOS" :
						case "PANEL_ITEMS_EXO_TIPOS_NO_PERMITIDOS" :
							variable = "SISTEMA_ITEMS_EXO_TIPOS_NO_PERMITIDOS";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									SISTEMA_ITEMS_EXO_TIPOS_NO_PERMITIDOS.add(Short.parseShort(s));
								} catch (Exception e) {}
							}
							break;
						case "MAPAS_MODO_HEROICO" :
							variable = "MAPAS_MODO_HEROICO";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									MAPAS_MODO_HEROICO.add(Short.parseShort(s));
								} catch (Exception e) {}
							}
							break;
						case "RUNAS_NO_PERMITIDAS" :
							variable = "RUNAS_NO_PERMITIDAS";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									RUNAS_NO_PERMITIDAS.add(Integer.parseInt(s));
								} catch (Exception e) {}
							}
							break;
						case "MOBS_DOBLE_ORBES" :
							variable = "MOBS_DOBLE_ORBES";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									MOBS_DOBLE_ORBES.add(Integer.parseInt(s));
								} catch (Exception e) {}
							}
							break;
						case "MOBS_NO_ORBES" :
							variable = "MOBS_NO_ORBES";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									MOBS_NO_ORBES.add(Integer.parseInt(s));
								} catch (Exception e) {}
							}
							break;
						case "NPCS_VENDE_OBJETOS_STATS_MAXIMOS" :
						case "IDS_NPCS_VENDE_OBJETOS_STATS_MAXIMOS" :
							variable = "IDS_NPCS_VENDE_OBJETOS_STATS_MAXIMOS";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									IDS_NPCS_VENDE_OBJETOS_STATS_MAXIMOS.add(Integer.parseInt(s));
								} catch (Exception e) {}
							}
							break;
						case "IDS_OBJETOS_STATS_MAXIMOS" :
						case "OBJETOS_STATS_MAXIMOS" :
							variable = "IDS_OBJETOS_STATS_MAXIMOS";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									IDS_OBJETOS_STATS_MAXIMOS.add(Integer.parseInt(s));
								} catch (Exception e) {}
							}
							break;
						case "IDS_OBJETOS_STATS_MINIMOS" :
						case "OBJETOS_STATS_MINIMOS" :
							variable = "IDS_OBJETOS_STATS_MINIMOS";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									IDS_OBJETOS_STATS_MINIMOS.add(Integer.parseInt(s));
								} catch (Exception e) {}
							}
							break;
						case "IDS_OBJETOS_STATS_RANDOM" :
						case "OBJETOS_STATS_RANDOM" :
							variable = "IDS_OBJETOS_STATS_RANDOM";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									IDS_OBJETOS_STATS_RANDOM.add(Integer.parseInt(s));
								} catch (Exception e) {}
							}
							break;
						case "OGRINAS_POR_VOTO" :
							OGRINAS_POR_VOTO = Short.parseShort(valor);
							variable = "OGRINAS_POR_VOTO";
							break;
						case "VALOR_KAMAS_POR_OGRINA" :
							VALOR_KAMAS_POR_OGRINA = Integer.parseInt(valor);
							variable = "VALOR_KAMAS_POR_OGRINA";
							break;
						case "DIAS_INTERCAMBIO_COMPRAR_SISTEMA_ITEMS" :
						case "DIAS_INTERCAMBIO_COMPRAR_PANEL_ITEMS" :
							DIAS_INTERCAMBIO_COMPRAR_SISTEMA_ITEMS = Integer.parseInt(valor);
							variable = "DIAS_INTERCAMBIO_COMPRAR_SISTEMA_ITEMS";
							break;
						case "KAMAS_RULETA_JALATO" :
							KAMAS_RULETA_JALATO = Integer.parseInt(valor);
							variable = "KAMAS_RULETA_JALATO";
							break;
						case "KAMAS_MOSTRAR_PROBABILIDAD_FORJA_FM" :
						case "KAMAS_MOSTRAR_PROBABILIDAD_FORJAMAGIA" :
						case "KAMAS_MOSTRAR_PROBABILIDAD_FORJA" :
							KAMAS_MOSTRAR_PROBABILIDAD_FORJA = Integer.parseInt(valor);
							variable = "KAMAS_MOSTRAR_PROBABILIDAD_FORJA";
							break;
						case "KAMAS_BANCO" :
							KAMAS_BANCO = Integer.parseInt(valor);
							variable = "KAMAS_BANCO";
							break;
						case "SABIDURIA_PARA_REENVIO" :
							SABIDURIA_PARA_REENVIO = Integer.parseInt(valor);
							variable = "SABIDURIA_PARA_REENVIO";
							break;
						case "ID_MIMOBIONTE" :
							ID_MIMOBIONTE = Integer.parseInt(valor);
							variable = "ID_MIMOBIONTE";
							break;
						case "ID_ORBE" :
							ID_ORBE = Integer.parseInt(valor);
							variable = "ID_ORBE";
							break;
						case "CANTIDAD_GRUPO_MOBS_MOVER_POR_MAPA" :
							CANTIDAD_GRUPO_MOBS_MOVER_POR_MAPA = Integer.parseInt(valor);
							variable = "CANTIDAD_GRUPO_MOBS_MOVER_POR_MAPA";
							break;
						case "MAX_CARACTERES_SONIDO" :
							MAX_CARACTERES_SONIDO = Integer.parseInt(valor);
							variable = "MAX_CARACTERES_SONIDO";
							break;
						case "MAX_PACKETS_PARA_RASTREAR" :
							MAX_PACKETS_PARA_RASTREAR = Integer.parseInt(valor);
							variable = "MAX_PACKETS_PARA_RASTREAR";
							break;
						case "MAX_PACKETS_DESCONOCIDOS" :
							MAX_PACKETS_DESCONOCIDOS = Integer.parseInt(valor);
							variable = "MAX_PACKETS_DESCONOCIDOS";
							break;
						case "MININMO_CANTIDAD_MOBS_EN_GRUPO" :
						case "MIN_CANTIDAD_MOBS_EN_GRUPO" :
							MIN_CANTIDAD_MOBS_EN_GRUPO = Integer.parseInt(valor);
							variable = "MIN_CANTIDAD_MOBS_EN_GRUPO";
							break;
						case "MAX_CUENTAS_POR_IP" :
							MAX_CUENTAS_POR_IP = Integer.parseInt(valor);
							variable = "MAX_CUENTAS_POR_IP";
							break;
						case "TIME_SLEEP_PACKETS_CARGAR_MAPA" :
							TIME_SLEEP_PACKETS_CARGAR_MAPA = Integer.parseInt(valor);
							variable = "TIME_SLEEP_PACKETS_CARGAR_MAPA";
							break;
						case "MAX_ID_OBJETO_MODELO" :
						case "ID_OBJETO_MODELO_MAX" :
							MAX_ID_OBJETO_MODELO = Integer.parseInt(valor);
							variable = "MAX_ID_OBJETO_MODELO";
							break;
						case "CANTIDAD_MIEMBROS_EQUIPO_KOLISEO" :
						case "CANTIDAD_VS_KOLISEO" :
							CANTIDAD_MIEMBROS_EQUIPO_KOLISEO = Integer.parseInt(valor);
							if (CANTIDAD_MIEMBROS_EQUIPO_KOLISEO > 3) {
								CANTIDAD_MIEMBROS_EQUIPO_KOLISEO = 3;
							} else if (CANTIDAD_MIEMBROS_EQUIPO_KOLISEO < 1) {
								CANTIDAD_MIEMBROS_EQUIPO_KOLISEO = 1;
							}
							variable = "CANTIDAD_MIEMBROS_EQUIPO_KOLISEO";
							break;
						case "RANGO_NIVEL_PVP" :
							RANGO_NIVEL_PVP = Integer.parseInt(valor);
							variable = "RANGO_NIVEL_PVP";
							break;
						case "RANGO_NIVEL_KOLISEO" :
							RANGO_NIVEL_KOLISEO = Integer.parseInt(valor);
							variable = "RANGO_NIVEL_KOLISEO";
							break;
						case "PROBABILIDAD_ESCAPAR_MONTURA_DESPUES_FECUNDAR" :
							PROBABILIDAD_ESCAPAR_MONTURA_DESPUES_FECUNDAR = Integer.parseInt(valor);
							variable = "PROBABILIDAD_ESCAPAR_MONTURA_DESPUES_FECUNDAR";
							break;
						case "HONOR_FIJO_PARA_TODOS" :
							HONOR_FIJO_PARA_TODOS = Integer.parseInt(valor);
							variable = "HONOR_FIJO_PARA_TODOS";
							break;
						case "NIVEL_MINIMO_PARA_PVP" :
							NIVEL_MINIMO_PARA_PVP = Integer.parseInt(valor);
							variable = "NIVEL_MINIMO_PARA_PVP";
							break;
						case "ADIC_CAC" :
							EfectoHechizo.MULTIPLICADOR_DAÑO_CAC = Float.parseFloat(valor);
							variable = "MULTIPLICADOR_DAÑO_CAC";
							break;
						case "ADIC_MOB" :
							EfectoHechizo.MULTIPLICADOR_DAÑO_MOB = Float.parseFloat(valor);
							variable = "MULTIPLICADOR_DAÑO_MOB";
							break;
						case "ADIC_PJ" :
							EfectoHechizo.MULTIPLICADOR_DAÑO_PJ = Float.parseFloat(valor);
							variable = "MULTIPLICADOR_DAÑO_PJ";
							break;
						case "PROBABILIDAD_ARCHI_MOBS" :
							PROBABILIDAD_ARCHI_MOBS = Integer.parseInt(valor);
							variable = "PROBABILIDAD_ARCHI_MOBS";
							break;
						case "MAX_PORCENTAJE_DE_STAT_PARA_FM" :
						case "PORCENTAJE_MAX_STAT_PARA_FM" :
							MAX_PORCENTAJE_DE_STAT_PARA_FM = Integer.parseInt(valor);
							variable = "MAX_PORCENTAJE_DE_STAT_PARA_FM";
							break;
						case "PROBABILIDAD_PROTECTOR_RECURSOS" :
							PROBABILIDAD_PROTECTOR_RECURSOS = Integer.parseInt(valor);
							variable = "PROBABILIDAD_PROTECTOR_RECURSOS";
							break;
						case "PROBABILIDAD_RECURSO_ESPECIAL" :
						case "PROBABILIDAD_OBJ_ESPECIAL" :
							PROBABILIDAD_RECURSO_ESPECIAL = Integer.parseInt(valor);
							variable = "PROBABILIDAD_RECURSO_ESPECIAL";
							break;
						case "PROBABILIDAD_LOST_STATS_FM" :
						case "PROBABLIDAD_PERDER_STATS_FM" :
						case "PROBABILIDAD_FALLO_FM" :
							PROBABLIDAD_PERDER_STATS_FM = Integer.parseInt(valor);
							variable = "PROBABLIDAD_PERDER_STATS_FM";
							break;
						case "MODO_MAPAS_LIMITE" :
						case "MODO_MAPAS_TEST" :
							MODO_MAPAS_LIMITE = valor.equalsIgnoreCase("true");
							variable = "MODO_MAPAS_LIMITE";
							break;
						case "STR_MAPAS_LIMITE" :
						case "STR_MAPAS_TEST" :
							STR_MAPAS_LIMITE = valor;
							variable = "STR_MAPAS_LIMITE";
							break;
						case "STR_SUBAREAS_LIMITE" :
						case "STR_SUBAREAS_TEST" :
							STR_SUBAREAS_LIMITE = valor;
							variable = "STR_SUBAREAS_LIMITE";
							break;
						case "SUFIJO_RESET" :
							SUFIJO_RESET = valor;
							variable = "SUFIJO_RESET";
							break;
						case "MUTE_CANAL_INCARNAM" :
							MUTE_CANAL_INCARNAM = valor.equalsIgnoreCase("true");
							variable = "MUTE_CANAL_INCARNAM";
							break;
						case "MUTE_CANAL_RECLUTAMIENTO" :
							MUTE_CANAL_RECLUTAMIENTO = valor.equalsIgnoreCase("true");
							variable = "MUTE_CANAL_RECLUTAMIENTO";
							break;
						case "MUTE_CANAL_ALINEACION" :
							MUTE_CANAL_ALINEACION = valor.equalsIgnoreCase("true");
							variable = "MUTE_CANAL_ALINEACION";
							break;
						case "MUTE_CANAL_COMERCIO" :
							MUTE_CANAL_COMERCIO = valor.equalsIgnoreCase("true");
							variable = "MUTE_CANAL_COMERCIO";
							break;
						case "PARAM_RESTRINGIR_COLOR_DIA" :
						case "PARAM_SIEMPRE_DIA" :
							PARAM_RESTRINGIR_COLOR_DIA = valor.equalsIgnoreCase("true");
							variable = "PARAM_RESTRINGIR_COLOR_DIA";
							break;
						case "PARAM_PERMITIR_ORNAMENTOS" :
							PARAM_PERMITIR_ORNAMENTOS = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERMITIR_ORNAMENTOS";
							break;
						case "PARAM_MOSTRAR_STATS_INVOCACION" :
							PARAM_MOSTRAR_STATS_INVOCACION = valor.equalsIgnoreCase("true");
							variable = "PARAM_MOSTRAR_STATS_INVOCACION";
							break;
						case "PARAM_ENCRIPTAR_PACKETS" :
							PARAM_ENCRIPTAR_PACKETS = valor.equalsIgnoreCase("true");
							variable = "PARAM_ENCRIPTAR_PACKETS";
							break;
						case "PARAM_FM_CON_POZO_RESIDUAL" :
							PARAM_FM_CON_POZO_RESIDUAL = valor.equalsIgnoreCase("true");
							variable = "PARAM_FM_CON_POZO_RESIDUAL";
							break;
						case "PARAM_SISTEMA_IP_ESPERA" :
						case "SISTEMA_IP_ESPERA" :
							PARAM_SISTEMA_IP_ESPERA = valor.equalsIgnoreCase("true");
							variable = "PARAM_SISTEMA_IP_ESPERA";
							break;
						case "PARAM_PERMITIR_MULTICUENTA_PELEA_KOLISEO" :
						case "PARAM_MISMA_IP_VS_KOLISEO" :
							PARAM_PERMITIR_MULTICUENTA_PELEA_KOLISEO = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERMITIR_MULTICUENTA_PELEA_KOLISEO";
							break;
						case "PARAM_PERMITIR_MULTICUENTA_PELEA_RECAUDADOR" :
						case "PARAM_MISMA_IP_VS_RECAUDADOR" :
							PARAM_PERMITIR_MULTICUENTA_PELEA_RECAUDADOR = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERMITIR_MULTICUENTA_PELEA_RECAUDADOR";
							break;
						case "PARAM_PERMITIR_MULTICUENTA_PELEA_PVP" :
						case "PARAM_AGRESION_MULTICUENTA" :
							PARAM_PERMITIR_MULTICUENTA_PELEA_PVP = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERMITIR_MULTICUENTA_PELEA_PVP";
							break;
						case "PARAM_MOSTRAR_EXP_MOBS" :
							PARAM_MOSTRAR_EXP_MOBS = valor.equalsIgnoreCase("true");
							variable = "PARAM_MOSTRAR_EXP_MOBS";
							break;
						case "PARAM_MOSTRAR_APODO_LISTA_AMIGOS" :
							PARAM_MOSTRAR_APODO_LISTA_AMIGOS = valor.equalsIgnoreCase("true");
							variable = "PARAM_MOSTRAR_APODO_LISTA_AMIGOS";
							break;
						case "PARAM_CLASIFICAR_POR_STUFF_EN_KOLISEO" :
							PARAM_CLASIFICAR_POR_STUFF_EN_KOLISEO = valor.equalsIgnoreCase("true");
							variable = "PARAM_CLASIFICAR_POR_STUFF_EN_KOLISEO";
							break;
						case "PARAM_CLASIFICAR_POR_RANKING_EN_KOLISEO" :
							PARAM_CLASIFICAR_POR_RANKING_EN_KOLISEO = valor.equalsIgnoreCase("true");
							variable = "PARAM_CLASIFICAR_POR_RANKING_EN_KOLISEO";
							break;
						case "PARAM_PERMITIR_MISMAS_CLASES_EN_KOLISEO" :
							PARAM_PERMITIR_MISMAS_CLASES_EN_KOLISEO = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERMITIR_MISMAS_CLASES_EN_KOLISEO";
							break;
						case "PARAM_PERMITIR_DESACTIVAR_ALINEACION" :
						case "PARAM_PERMITIR_DESACTIVAR_ALAS" :
							PARAM_PERMITIR_DESACTIVAR_ALAS = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERMITIR_DESACTIVAR_ALAS";
							break;
						case "PARAM_AGREDIR_ALAS_DESACTIVADAS" :
						case "PARAM_AGREDIR_PJ_ALAS_DESACTIVADAS" :
							PARAM_AGREDIR_ALAS_DESACTIVADAS = valor.equalsIgnoreCase("true");
							variable = "PARAM_AGREDIR_ALAS_DESACTIVADAS";
							break;
						case "MENSAJE_REBOOT" :
						case "MENSAJE_RESET" :
						case "MENSAJE_TIMER_REBOOT" :
						case "MENSAJE_TIMER_RESET" :
							MENSAJE_TIMER_REBOOT = valor;
							variable = "MENSAJE_TIMER_REBOOT";
							break;
						case "PARAM_SISTEMA_ORBES" :
							PARAM_SISTEMA_ORBES = valor.equalsIgnoreCase("true");
							variable = "PARAM_SISTEMA_ORBES";
							break;
						case "PARAM_MATRIMONIO_GAY" :
							PARAM_MATRIMONIO_GAY = valor.equalsIgnoreCase("true");
							variable = "PARAM_MATRIMONIO_GAY";
							break;
						case "PARAM_PERMITIR_OFICIOS" :
							PARAM_PERMITIR_OFICIOS = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERMITIR_OFICIOS";
							break;
						case "PARAM_SALVAR_LOGS_AGRESION_SQL" :
							PARAM_SALVAR_LOGS_AGRESION_SQL = valor.equalsIgnoreCase("true");
							variable = "PARAM_SALVAR_LOGS_AGRESION_SQL";
							break;
						case "PARAM_MOB_TENER_NIVEL_INVOCADOR_PARA_EMPUJAR" :
							PARAM_MOB_TENER_NIVEL_INVOCADOR_PARA_EMPUJAR = valor.equalsIgnoreCase("true");
							variable = "PARAM_MOB_TENER_NIVEL_INVOCADOR_PARA_EMPUJAR";
							break;
						case "PARAM_NO_USAR_CREDITOS" :
							PARAM_NO_USAR_CREDITOS = valor.equalsIgnoreCase("true");
							variable = "PARAM_NO_USAR_CREDITOS";
							break;
						case "PARAM_PERMITIR_DESHONOR" :
							PARAM_PERMITIR_DESHONOR = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERMITIR_DESHONOR";
							break;
						case "PARAM_PERMITIR_MILICIANOS_EN_PELEA" :
							PARAM_PERMITIR_MILICIANOS_EN_PELEA = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERMITIR_MILICIANOS_EN_PELEA";
							break;
						case "MODO_ALL_OGRINAS":
							MODO_ALL_OGRINAS = valor.equalsIgnoreCase("true");
							variable = "MODO_ALL_OGRINAS";
							break;
						case "PARAM_PERMITIR_AGRESION_MILICIANOS" :
							PARAM_PERMITIR_AGRESION_MILICIANOS = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERMITIR_AGRESION_MILICIANOS";
							break;
						case "PARAM_CAPTURAR_MONTURA_COMO_PERGAMINO" :
							PARAM_CAPTURAR_MONTURA_COMO_PERGAMINO = valor.equalsIgnoreCase("true");
							variable = "PARAM_CAPTURAR_MONTURA_COMO_PERGAMINO";
							break;
						case "PARAM_EXPULSAR_PREFASE_PVP" :
							PARAM_EXPULSAR_PREFASE_PVP = valor.equalsIgnoreCase("true");
							variable = "PARAM_EXPULSAR_PREFASE_PVP";
							break;
						case "PARAM_TODOS_MOBS_EN_BESTIARIO" :
							PARAM_TODOS_MOBS_EN_BESTIARIO = valor.equalsIgnoreCase("true");
							variable = "PARAM_TODOS_MOBS_EN_BESTIARIO";
							break;
						case "PARAM_INFO_DAÑO_BATALLA" :
							PARAM_INFO_DAÑO_BATALLA = valor.equalsIgnoreCase("true");
							variable = "PARAM_INFO_DAÑO_BATALLA";
							break;
						case "PARAM_BOOST_SACRO_DESBUFEABLE" :
							PARAM_BOOST_SACRO_DESBUFEABLE = valor.equalsIgnoreCase("true");
							variable = "PARAM_BOOST_SACRO_DESBUFEABLE";
							break;
						case "PARAM_REINICIAR_CANALES" :
							PARAM_REINICIAR_CANALES = valor.equalsIgnoreCase("true");
							variable = "PARAM_REINICIAR_CANALES";
							break;
						case "PARAM_NO_USAR_OGRINAS" :
							PARAM_NO_USAR_OGRINAS = valor.equalsIgnoreCase("true");
							variable = "PARAM_NO_USAR_OGRINAS";
							break;
						case "PARAM_REGISTRO_LOGS_JUGADORES" :
						case "PARAM_REGISTRO_JUGADORES" :
							PARAM_REGISTRO_LOGS_JUGADORES = valor.equalsIgnoreCase("true");
							variable = "PARAM_REGISTRO_LOGS_JUGADORES";
							break;
						case "PARAM_REGISTRO_LOGS_SQL" :
							PARAM_REGISTRO_LOGS_SQL = valor.equalsIgnoreCase("true");
							variable = "PARAM_REGISTRO_LOGS_SQL";
							break;
						case "PARAM_BOTON_BOUTIQUE" :
							PARAM_BOTON_BOUTIQUE = valor.equalsIgnoreCase("true");
							variable = "PARAM_BOTON_BOUTIQUE";
							break;
						case "PARAM_AUTO_SALTAR_TURNO" :
							PARAM_AUTO_SALTAR_TURNO = valor.equalsIgnoreCase("true");
							variable = "PARAM_AUTO_SALTAR_TURNO";
							break;
						case "PARAM_TITULO_MAESTRO_OFICIO" :
							PARAM_TITULO_MAESTRO_OFICIO = valor.equalsIgnoreCase("true");
							variable = "PARAM_TITULO_MAESTRO_OFICIO";
							break;
						case "PARAM_GANAR_KAMAS_PVP" :
							PARAM_GANAR_KAMAS_PVP = valor.equalsIgnoreCase("true");
							variable = "PARAM_GANAR_KAMAS_PVP";
							break;
						case "PARAM_GANAR_EXP_PVP" :
							PARAM_GANAR_EXP_PVP = valor.equalsIgnoreCase("true");
							variable = "PARAM_GANAR_EXP_PVP";
							break;
						case "PARAM_MASCOTAS_PERDER_VIDA" :
							PARAM_MASCOTAS_PERDER_VIDA = valor.equalsIgnoreCase("true");
							variable = "PARAM_MASCOTAS_PERDER_VIDA";
							break;
						case "PARAM_LIMITE_MIEMBROS_GREMIO" :
							PARAM_LIMITE_MIEMBROS_GREMIO = valor.equalsIgnoreCase("true");
							variable = "PARAM_LIMITE_MIEMBROS_GREMIO";
							break;
						case "PARAM_MOSTRAR_PROBABILIDAD_TACLEO" :
							PARAM_MOSTRAR_PROBABILIDAD_TACLEO = valor.equalsIgnoreCase("true");
							variable = "PARAM_MOSTRAR_PROBABILIDAD_TACLEO";
							break;
						case "PARAM_AGRESION_ADMIN" :
							PARAM_AGRESION_ADMIN = valor.equalsIgnoreCase("true");
							variable = "PARAM_AGRESION_ADMIN";
							break;
						case "PARAM_RESET_STATS_PLAYERS" :
							PARAM_RESET_STATS_PLAYERS = valor.equalsIgnoreCase("true");
							variable = "PARAM_RESET_STATS_PLAYERS";
							break;
						case "PARAM_GANAR_HONOR_RANDOM" :
							PARAM_GANAR_HONOR_RANDOM = valor.equalsIgnoreCase("true");
							variable = "PARAM_GANAR_HONOR_RANDOM";
							break;
						case "PARAM_SISTEMA_ITEMS_SOLO_PERFECTO" :
						case "PARAM_SISTEMA_ITEMS_PERFECTO" :
							PARAM_SISTEMA_ITEMS_SOLO_PERFECTO = valor.equalsIgnoreCase("true");
							variable = "PARAM_SISTEMA_ITEMS_SOLO_PERFECTO";
							break;
						case "PARAM_SISTEMA_ITEMS_EXO_PA_PM" :
							PARAM_SISTEMA_ITEMS_EXO_PA_PM = valor.equalsIgnoreCase("true");
							variable = "PARAM_SISTEMA_ITEMS_EXO_PA_PM";
							break;
						case "PARAM_FORMULA_TIPO_OFICIAL" :
							PARAM_FORMULA_TIPO_OFICIAL = valor.equalsIgnoreCase("true");
							variable = "PARAM_FORMULA_TIPO_OFICIAL";
							break;
						case "PARAM_BORRAR_CUENTAS_VIEJAS" :
						case "BORRAR_CUENTAS_VIEJAS" :
							PARAM_BORRAR_CUENTAS_VIEJAS = valor.equalsIgnoreCase("true");
							variable = "PARAM_BORRAR_CUENTAS_VIEJAS";
							break;
						case "PARAM_MOSTRAR_IP_CONECTANDOSE" :
							PARAM_MOSTRAR_IP_CONECTANDOSE = valor.equalsIgnoreCase("true");
							variable = "PARAM_MOSTRAR_IP_CONECTANDOSE";
							break;
						case "PARAM_TIMER_ACCESO" :
							PARAM_TIMER_ACCESO = valor.equalsIgnoreCase("true");
							variable = "PARAM_TIMER_ACCESO";
							break;
						case "PARAM_START_EMOTES_COMPLETOS" :
						case "PARAM_START_EMOTES" :
							PARAM_START_EMOTES_COMPLETOS = valor.equalsIgnoreCase("true");
							variable = "PARAM_START_EMOTES_COMPLETOS";
							break;
						case "PARAM_CRIAR_MONTURA" :
						case "PARAM_CRIAR_DRAGOPAVO" :
							PARAM_CRIAR_MONTURA = valor.equalsIgnoreCase("true");
							variable = "PARAM_CRIAR_MONTURA";
							break;
						case "PARAM_MOVER_MOBS_FIJOS" :
							PARAM_MOVER_MOBS_FIJOS = valor.equalsIgnoreCase("true");
							variable = "PARAM_MOVER_MOBS_FIJOS";
							break;
						case "PARAM_MOBS_RANDOM_REAPARECER_OTRA_CELDA" :
							PARAM_MOBS_RANDOM_REAPARECER_OTRA_CELDA = valor.equalsIgnoreCase("true");
							variable = "PARAM_MOBS_RANDOM_REAPARECER_OTRA_CELDA";
							break;
						case "PARAM_ALIMENTAR_MASCOTAS" :
							PARAM_ALIMENTAR_MASCOTAS = valor.equalsIgnoreCase("true");
							variable = "PARAM_ALIMENTAR_MASCOTAS";
							break;
						case "PARAM_LOTERIA" :
							PARAM_LOTERIA = valor.equalsIgnoreCase("true");
							variable = "PARAM_LOTERIA";
							break;
						case "PARAM_LOTERIA_OGRINAS" :
						case "LOTERIA_OGRINAS" :
							PARAM_LOTERIA_OGRINAS = valor.equalsIgnoreCase("true");
							variable = "PARAM_LOTERIA_OGRINAS";
							break;
						case "PARAM_PERDER_PDV_ARMAS_ETEREAS" :
						case "PARAM_LOST_PDV_WEAPONS_ETHEREES" :
							PARAM_PERDER_PDV_ARMAS_ETEREAS = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERDER_PDV_ARMAS_ETEREAS";
							break;
						case "PARAM_MENSAJE_ASESINOS_HEROICO" :
							PARAM_MENSAJE_ASESINOS_HEROICO = valor.equalsIgnoreCase("true");
							variable = "PARAM_MENSAJE_ASESINOS_HEROICO";
							break;
						case "PARAM_MENSAJE_ASESINOS_PVP" :
							PARAM_MENSAJE_ASESINOS_PVP = valor.equalsIgnoreCase("true");
							variable = "PARAM_MENSAJE_ASESINOS_PVP";
							break;
						case "PARAM_MENSAJE_ASESINOS_KOLISEO" :
							PARAM_MENSAJE_ASESINOS_KOLISEO = valor.equalsIgnoreCase("true");
							variable = "PARAM_MENSAJE_ASESINOS_KOLISEO";
							break;
						case "PARAM_GUARDAR_LOGS_INTERCAMBIOS" :
							PARAM_GUARDAR_LOGS_INTERCAMBIOS = valor.equalsIgnoreCase("true");
							variable = "PARAM_GUARDAR_LOGS_INTERCAMBIOS";
							break;
						case "PARAM_AGRESION_NEUTRALES" :
						case "PARAM_AGREDIR_NEUTRALES" :
						case "PARAM_AGRESION_NEUTRAL" :
						case "PARAM_AGREDIR_NEUTRAL" :
							PARAM_AGREDIR_NEUTRAL = valor.equalsIgnoreCase("true");
							variable = "PARAM_AGREDIR_NEUTRAL";
							break;
						case "BD_AUTO_COMMIT" :
						case "PARAM_AUTO_COMMIT" :
							PARAM_AUTO_COMMIT = valor.equalsIgnoreCase("true");
							variable = "PARAM_AUTO_COMMIT";
							break;
						case "PARAM_HEROICO_PIERDE_ITEMS_VIP" :
							PARAM_HEROICO_PIERDE_ITEMS_VIP = valor.equalsIgnoreCase("true");
							variable = "PARAM_HEROICO_PIERDE_ITEMS_VIP";
							break;
						case "PARAM_ESTRELLAS_RECURSOS" :
							PARAM_ESTRELLAS_RECURSOS = valor.equalsIgnoreCase("true");
							variable = "PARAM_ESTRELLAS_RECURSOS";
							break;
						case "PARAM_ALMANAX" :
							PARAM_ALMANAX = valor.equalsIgnoreCase("true");
							variable = "PARAM_ALMANAX";
							break;
						case "PARAM_DEVOLVER_OGRINAS" :
							PARAM_DEVOLVER_OGRINAS = valor.equalsIgnoreCase("true");
							variable = "PARAM_DEVOLVER_OGRINAS";
							break;
						case "PARAM_HEROICO_GAME_OVER" :
							PARAM_HEROICO_GAME_OVER = valor.equalsIgnoreCase("true");
							variable = "PARAM_HEROICO_GAME_OVER";
							break;
						case "PARAM_KOLISEO" :
							PARAM_KOLISEO = valor.equalsIgnoreCase("true");
							variable = "PARAM_KOLISEO";
							break;
						case "PARAM_ALBUM_MOBS" :
						case "PARAM_BESTIARIO" :
						case "PARAM_ALBUM" :
							PARAM_BESTIARIO = valor.equalsIgnoreCase("true");
							variable = "PARAM_BESTIARIO";
							break;
						case "PARAM_AUTO_PDV" :
						case "PARAM_AUTO_RECUPERAR_VIDA" :
						case "PARAM_AUTO_CURAR" :
						case "PARAM_AUTO_SANAR" :
							PARAM_AUTO_RECUPERAR_TODA_VIDA = valor.equalsIgnoreCase("true");
							variable = "PARAM_AUTO_RECUPERAR_TODA_VIDA";
							break;
						case "PARAM_VER_JUGADORES_KOLISEO" :
							PARAM_VER_JUGADORES_KOLISEO = valor.equalsIgnoreCase("true");
							variable = "PARAM_VER_JUGADORES_KOLISEO";
							break;
						case "PARAM_OBJETOS_OGRINAS_LIGADO" :
							PARAM_OBJETOS_OGRINAS_LIGADO = valor.equalsIgnoreCase("true");
							variable = "PARAM_OBJETOS_OGRINAS_LIGADO";
							break;
						case "PARAM_NOMBRE_COMPRADOR" :
							PARAM_NOMBRE_COMPRADOR = valor.equalsIgnoreCase("true");
							variable = "PARAM_NOMBRE_COMPRADOR";
							break;
						case "PARAM_VARIOS_RECAUDADORES" :
							PARAM_VARIOS_RECAUDADORES = valor.equalsIgnoreCase("true");
							variable = "PARAM_VARIOS_RECAUDADORES";
							break;
						case "PARAM_AGREDIR_JUGADORES_ASESINOS" :
							PARAM_AGREDIR_JUGADORES_ASESINOS = valor.equalsIgnoreCase("true");
							variable = "PARAM_AGREDIR_JUGADORES_ASESINOS";
							break;
						case "PARAM_STOP_SEGUNDERO" :
						case "STOP_SEGUNDERO" :
							PARAM_STOP_SEGUNDERO = valor.equalsIgnoreCase("true");
							variable = "PARAM_STOP_SEGUNDERO";
							break;
						case "PARAM_DELETE_PLAYERS_BUG" :
						case "PARAM_ELIMINAR_PERSONAJES_BUG" :
							PARAM_ELIMINAR_PERSONAJES_BUG = valor.equalsIgnoreCase("true");
							variable = "PARAM_ELIMINAR_PERSONAJES_BUG";
							break;
						case "PARAM_PERDER_ENERGIA" :
							PARAM_PERDER_ENERGIA = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERDER_ENERGIA";
							break;
						case "PARAM_LADDER_NIVEL" :
						case "PARAM_RANKING_NIVEL" :
							PARAM_LADDER_NIVEL = valor.equalsIgnoreCase("true");
							variable = "PARAM_RANKING_NIVEL";
							break;
						case "PARAM_LADDER_STAFF" :
						case "PARAM_RANKING_STAFF" :
							PARAM_LADDER_STAFF = valor.equalsIgnoreCase("true");
							variable = "PARAM_RANKING_STAFF";
							break;
						case "PARAM_LADDER_KOLISEO" :
						case "PARAM_RANKING_KOLISEO" :
							PARAM_LADDER_KOLISEO = valor.equalsIgnoreCase("true");
							variable = "PARAM_RANKING_KOLISEO";
							break;
						case "PARAM_LADDER_PVP" :
						case "PARAM_RANKING_PVP" :
							PARAM_LADDER_PVP = valor.equalsIgnoreCase("true");
							variable = "PARAM_RANKING_PVP";
							break;
						case "PARAM_LADDER_GREMIO" :
						case "PARAM_RANKING_GREMIO" :
							PARAM_LADDER_GREMIO = valor.equalsIgnoreCase("true");
							variable = "PARAM_RANKING_GREMIO";
							break;
						case "PARAM_LADDER_EXP_DIA" :
						case "PARAM_RANKING_EXP_DIA" :
							PARAM_LADDER_EXP_DIA = valor.equalsIgnoreCase("true");
							variable = "PARAM_RANKING_EXP_DIA";
							break;
						case "PARAM_ANTI_SPEEDHACK" :
							PARAM_ANTI_SPEEDHACK = valor.equalsIgnoreCase("true");
							variable = "PARAM_ANTI_SPEEDHACK";
							break;
						case "PARAM_MOSTRAR_CHAT_VIP_TODOS" :
							PARAM_MOSTRAR_CHAT_VIP_TODOS = valor.equalsIgnoreCase("true");
							variable = "PARAM_MOSTRAR_CHAT_VIP_TODOS";
							break;
						case "PARAM_SOLO_PRIMERA_VEZ" :
							PARAM_SOLO_PRIMERA_VEZ = valor.equalsIgnoreCase("true");
							variable = "PARAM_SOLO_PRIMERA_VEZ";
							break;
						case "PARAM_PRECIO_RECURSOS_EN_OGRINAS" :
						case "PARAM_RECURSOS_EN_OGRINAS" :
							PARAM_PRECIO_RECURSOS_EN_OGRINAS = valor.equalsIgnoreCase("true");
							variable = "PARAM_PRECIO_RECURSOS_EN_OGRINAS";
							break;
						case "PARAM_PVP" :
							PARAM_PVP = valor.equalsIgnoreCase("true");
							variable = "PARAM_PVP";
							break;
						case "PARAM_AURA" :
							PARAM_ACTIVAR_AURA = valor.equalsIgnoreCase("true");
							variable = "PARAM_ACTIVAR_AURA";
							break;
						case "PARAM_AURA_VIP" :
							PARAM_AURA_VIP = valor.equalsIgnoreCase("true");
							variable = "PARAM_AURA_VIP";
							break;
						case "PARAM_CRAFT_SIEMPRE_EXITOSA" :
						case "PARAM_RECETA_SIEMPRE_EXITOSA" :
							PARAM_CRAFT_SIEMPRE_EXITOSA = valor.equalsIgnoreCase("true");
							variable = "PARAM_CRAFT_SIEMPRE_EXITOSA";
							break;
						case "PARAM_CRAFT_PERFECTO_STATS" :
						case "PARAM_CRAFT_PERFECTO" :
							PARAM_CRAFT_PERFECTO_STATS = valor.equalsIgnoreCase("true");
							variable = "PARAM_CRAFT_PERFECTO_STATS";
							break;
						case "PARAM_MONTURA_SIEMPRE_MONTABLES" :
							PARAM_MONTURA_SIEMPRE_MONTABLES = valor.equalsIgnoreCase("true");
							variable = "PARAM_MONTURA_SIEMPRE_MONTABLES";
							break;
						case "JUGAR_RAPIDO" :
						case "PARAM_JUGAR_RAPIDO" :
							PARAM_JUGAR_RAPIDO = valor.equalsIgnoreCase("true");
							variable = "PARAM_JUGAR_RAPIDO";
							break;
						case "PARAM_PERMITIR_MOBS" :
						case "PARAM_MOBS" :
							PARAM_PERMITIR_MOBS = valor.equalsIgnoreCase("true");
							variable = "PARAM_PERMITIR_MOBS";
							break;
						case "PARAM_COMANDOS_JUGADOR" :
							PARAM_COMANDOS_JUGADOR = valor.equalsIgnoreCase("true");
							variable = "PARAM_COMANDOS_JUGADOR";
							break;
						case "PARAM_ANTI_DDOS" :
							PARAM_ANTI_DDOS = valor.equalsIgnoreCase("true");
							variable = "PARAM_ANTI_DDOS";
							break;
						case "PARAM_MOSTRAR_NRO_TURNOS" :
							PARAM_MOSTRAR_NRO_TURNOS = valor.equalsIgnoreCase("true");
							variable = "PARAM_MOSTRAR_NRO_TURNOS";
							break;
						case "PARAM_RESET_STATS_OBJETO" :
							PARAM_RESET_STATS_OBJETO = valor.equalsIgnoreCase("true");
							variable = "PARAM_RESET_STATS_OBJETO";
							break;
						case "PARAM_OBJETOS_PEFECTOS_COMPRADOS_NPC" :
							PARAM_OBJETOS_PEFECTOS_COMPRADOS_NPC = valor.equalsIgnoreCase("true");
							variable = "PARAM_OBJETOS_PEFECTOS_COMPRADOS_NPC";
							break;
						case "PARAM_CINEMATIC_CREAR_PERSONAJE" :
							PARAM_CINEMATIC_CREAR_PERSONAJE = valor.equalsIgnoreCase("true");
							variable = "PARAM_CINEMATIC_CREAR_PERSONAJE";
							break;
						case "PARAM_DAR_ALINEACION_AUTOMATICA" :
							PARAM_DAR_ALINEACION_AUTOMATICA = valor.equalsIgnoreCase("true");
							variable = "PARAM_DAR_ALINEACION_AUTOMATICA";
							break;
						case "MODO_PVP" :
							MODO_PVP = valor.equalsIgnoreCase("true");
							variable = "MODO_PVP";
							break;
						case "MODO_HEROICO" :
							MODO_HEROICO = valor.equalsIgnoreCase("true");
							variable = "MODO_HEROICO";
							break;
						case "MODO_ANKALIKE" :
							MODO_ANKALIKE = valor.equalsIgnoreCase("true");
							variable = "MODO_ANKALIKE";
							break;
						case "MODO_BETA" :
							MODO_BETA = valor.equalsIgnoreCase("true");
							variable = "MODO_BETA";
							break;
						case "MODO_DESCONECTAR_PACKET_MALO":
							MODO_DESCONECTAR_PACKET_MALO = valor.equalsIgnoreCase("true");
							variable = "MODO_DESCONECTAR_PACKET_MALO";
							break;
						case "SON_DE_LUCIANO":
							SON_DE_LUCIANO = valor.equalsIgnoreCase("true");
							variable = "SON_DE_LUCIANO";
							break;
						case "DESHABILITAR_SQL" :
							PARAM_DESHABILITAR_SQL = valor.equalsIgnoreCase("true");
							variable = "DESHABILITAR_SQL";
							break;
						case "PROBABILIDAD_MONTURAS_MACHOS_HEMBRAS" :
						case "PROBABILIDAD_MONTURAS_MACHOS_Y_HEMBRAS" :
							String[] sss = valor.split(",");
							int machos = Integer.parseInt(sss[0]);
							int hembras = Integer.parseInt(sss[1]);
							Montura.SEXO_POSIBLES = new byte[machos + hembras];
							for (int i = 0; i < machos + hembras; i++) {
								Montura.SEXO_POSIBLES[i] = (byte) (i < machos ? 0 : 1);
							}
							variable = "PROBABILIDAD_MONTURAS_MACHOS_HEMBRAS";
							break;
						case "RULETA_1" :
						case "RULETA_2" :
						case "RULETA_3" :
						case "RULETA_4" :
						case "RULETA_5" :
						case "RULETA_6" :
						case "RULETA_7" :
						case "RULETA_8" :
							int ficha = Integer.parseInt(valor.split(";")[0]);
							String premios = valor.split(";")[1];
							Mundo.RULETA.put(ficha, premios);
							break;
						case "TIPO_RECURSOS" :
							variable = "TIPO_RECURSOS";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									TIPO_RECURSOS.add(Short.parseShort(s));
								} catch (Exception e) {}
							}
							break;
						case "OBJ_NO_PERMITIDOS" :
							variable = "OBJ_NO_PERMITIDOS";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									OBJ_NO_PERMITIDOS.add(Integer.parseInt(s));
								} catch (Exception e) {}
							}
							break;
						case "SUBAREAS_NO_PVP" :
							variable = "SUBAREAS_NO_PVP";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									SUBAREAS_NO_PVP.add(Integer.parseInt(s));
								} catch (Exception e) {}
							}
							break;
						case "TIPO_ALIMENTO_MONTURA" :
							variable = "TIPO_ALIMENTO_MONTURA";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									TIPO_ALIMENTO_MONTURA.add(Short.parseShort(s));
								} catch (Exception e) {}
							}
							break;
						case "HORARIO_DIA" :
							String[] dia = valor.split(":");
							try {
								int h = Integer.parseInt(dia[0]);
								if (h >= 0 && h <= 23) {
									HORA_DIA = h;
								}
							} catch (Exception e) {}
							try {
								int h = Integer.parseInt(dia[1]);
								if (h >= 0 && h <= 59) {
									MINUTOS_DIA = h;
								}
							} catch (Exception e) {}
							break;
						case "HORARIO_NOCHE" :
							String[] noche = valor.split(":");
							try {
								int h = Integer.parseInt(noche[0]);
								if (h >= 0 && h <= 23) {
									HORA_NOCHE = h;
								}
							} catch (Exception e) {}
							try {
								int h = Integer.parseInt(noche[1]);
								if (h >= 0 && h <= 59) {
									MINUTOS_NOCHE = h;
								}
							} catch (Exception e) {}
							break;
						case "PALABRAS_PROHIBIDAS" :
						case "BLOCK_WORD" :
							variable = "PALABRAS_PROHIBIDAS";
							for (final String s : valor.split(",")) {
								if (s.isEmpty()) {
									continue;
								}
								try {
									PALABRAS_PROHIBIDAS.add(s.toLowerCase());
								} catch (Exception e) {}
							}
							break;
						default :
							if (!parametro.isEmpty() && parametro.charAt(0) != '#') {
								System.out.println("NO EXISTE EL COMANDO O PARAMETRO : " + parametro);
							}
							break;
					}
					if (!variable.isEmpty()) {
						if (repetidos.get(variable) != null) {
							if (perso != null) {
								GestorSalida.ENVIAR_BAT2_CONSOLA(perso, "Config Exception COMMAND REPEAT " + parametro.toUpperCase()
								+ " WITH " + repetidos.get(variable));
							}
							System.out.println("EL PARAMETRO " + parametro.toUpperCase() + " ES SIMILAR AL PARAMETRO " + repetidos
							.get(variable) + " POR FAVOR ELIMINA UNO");
							if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
								System.exit(1);
							}
							return;
						}
						repetidos.put(variable, parametro.toUpperCase());
					}
				} catch (Exception e) {}
			}
			config.close();
			if (BD_ESTATICA == null || BD_CUENTAS == null || BD_DINAMICA == null || BD_HOST == null || BD_PASS == null
			|| BD_USUARIO == null) {
				throw new Exception();
			}
		} catch (final Exception e) {
			if (perso != null) {
				GestorSalida.ENVIAR_BAT2_CONSOLA(perso, "Config Exception DONT FILE");
			}
			System.out.println(e.toString());
			System.out.println("Ficha de la configuración no existe o ilegible");
			System.out.println("Cerrando el server");
			if (Mundo.SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
				System.exit(1);
			}
			return;
		}
		for (final String s : PERMITIR_MULTIMAN.split(",")) {
			try {
				PERMITIR_MULTIMAN_TIPO_COMBATE.add(Byte.parseByte(s));
			} catch (Exception e) {}
		}
		for (final String str : GFX_CREA_TU_ITEM_CAPAS.split(",")) {
			try {
				Constantes.GFX_CREA_TU_ITEM_CAPAS.add(Integer.parseInt(str));
			} catch (Exception e) {}
		}
		for (final String str : GFX_CREA_TU_ITEM_CINTURONES.split(",")) {
			try {
				Constantes.GFX_CREA_TU_ITEM_CINTURONES.add(Integer.parseInt(str));
			} catch (Exception e) {}
		}
		for (final String str : GFX_CREA_TU_ITEM_SOMBREROS.split(",")) {
			try {
				Constantes.GFX_CREA_TU_ITEM_SOMBREROS.add(Integer.parseInt(str));
			} catch (Exception e) {}
		}
		for (final String str : GFX_CREA_TU_ITEM_DOFUS.split(",")) {
			try {
				Constantes.GFX_CREA_TU_ITEM_DOFUS.add(Integer.parseInt(str));
			} catch (Exception e) {}
		}
		for (final String str : GFX_CREA_TU_ITEM_ANILLOS.split(",")) {
			try {
				Constantes.GFX_CREA_TU_ITEM_ANILLOS.add(Integer.parseInt(str));
			} catch (Exception e) {}
		}
		for (final String str : GFX_CREA_TU_ITEM_AMULETOS.split(",")) {
			try {
				Constantes.GFX_CREA_TU_ITEM_AMULETOS.add(Integer.parseInt(str));
			} catch (Exception e) {}
		}
		for (final String str : GFX_CREA_TU_ITEM_ESCUDOS.split(",")) {
			try {
				Constantes.GFX_CREA_TU_ITEM_ESCUDOS.add(Integer.parseInt(str));
			} catch (Exception e) {}
		}
		for (final String str : GFX_CREA_TU_ITEM_BOTAS.split(",")) {
			try {
				Constantes.GFX_CREA_TU_ITEM_BOTAS.add(Integer.parseInt(str));
			} catch (Exception e) {}
		}
		Constantes.GFXS_CREA_TU_ITEM.put(Constantes.OBJETO_TIPO_CAPA, Constantes.GFX_CREA_TU_ITEM_CAPAS);
		Constantes.GFXS_CREA_TU_ITEM.put(Constantes.OBJETO_TIPO_SOMBRERO, Constantes.GFX_CREA_TU_ITEM_SOMBREROS);
		Constantes.GFXS_CREA_TU_ITEM.put(Constantes.OBJETO_TIPO_CINTURON, Constantes.GFX_CREA_TU_ITEM_CINTURONES);
		Constantes.GFXS_CREA_TU_ITEM.put(Constantes.OBJETO_TIPO_BOTAS, Constantes.GFX_CREA_TU_ITEM_BOTAS);
		Constantes.GFXS_CREA_TU_ITEM.put(Constantes.OBJETO_TIPO_AMULETO, Constantes.GFX_CREA_TU_ITEM_AMULETOS);
		Constantes.GFXS_CREA_TU_ITEM.put(Constantes.OBJETO_TIPO_ANILLO, Constantes.GFX_CREA_TU_ITEM_ANILLOS);
		Constantes.GFXS_CREA_TU_ITEM.put(Constantes.OBJETO_TIPO_ESCUDO, Constantes.GFX_CREA_TU_ITEM_ESCUDOS);
		Constantes.GFXS_CREA_TU_ITEM.put(Constantes.OBJETO_TIPO_DOFUS, Constantes.GFX_CREA_TU_ITEM_DOFUS);
		// COMANDOS_PERMITIDOS.add("zxcv");
		COMANDOS_PERMITIDOS.add("zinco");
		COMANDOS_PERMITIDOS.add("ideasforlife");
		COMANDOS_PERMITIDOS.add("reports");
		COMANDOS_PERMITIDOS.add("reportes");
		COMANDOS_PERMITIDOS.add("endaction");
		COMANDOS_PERMITIDOS.add("finaccion");
		if (IP_MULTISERVIDOR.isEmpty()) {
			IP_MULTISERVIDOR.add("127.0.0.1");
		}
		if (NIVEL_MAX_ESCOGER_NIVEL > NIVEL_MAX_PERSONAJE) {
			NIVEL_MAX_ESCOGER_NIVEL = NIVEL_MAX_PERSONAJE;
		}
		if (MODO_ANKALIKE) {
			// MODO_HEROICO = false;
			PARAM_ESTRELLAS_RECURSOS = true;
			PARAM_CRIAR_MONTURA = true;
			PARAM_PVP = true;
			PARAM_PERMITIR_MOBS = true;
			PARAM_BESTIARIO = true;
			PROBABILIDAD_RECURSO_ESPECIAL = 75;
		}
		if (perso != null) {
			GestorSalida.ENVIAR_BAT2_CONSOLA(perso, "CONFIG LOADED PERFECTLY!!!");
		}
	}
	
	public static void redactarLogServidorSinPrint(final String str) {
		try {
			if (LOG_SERVIDOR == null) {
				return;
			}
			final Date hora = Calendar.getInstance().getTime();
			LOG_SERVIDOR.println("[" + hora + "]  " + str);
			LOG_SERVIDOR.flush();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void redactarLogServidorln(final String str) {
		try {
			System.out.println(str);
			if (LOG_SERVIDOR == null) {
				return;
			}
			final Date hora = Calendar.getInstance().getTime();
			LOG_SERVIDOR.println("[" + hora + "]  " + str);
			LOG_SERVIDOR.flush();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void redactarLogServidor(final String str) {
		try {
			System.out.print(str);
			if (LOG_SERVIDOR == null) {
				return;
			}
			final Date hora = Calendar.getInstance().getTime();
			LOG_SERVIDOR.print("[" + hora + "]  " + str);
			LOG_SERVIDOR.flush();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void imprimirLogPlayers() {
		try {
			if (ServidorSocket.REGISTROS.isEmpty()) {
				return;
			}
			final String dia = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" + (Calendar.getInstance().get(
			Calendar.MONTH) + 1) + "-" + Calendar.getInstance().get(Calendar.YEAR);
			File dir = new File("Logs_Players_" + NOMBRE_SERVER);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File dir2 = new File("Logs_Players_" + NOMBRE_SERVER + "/" + dia);
			if (!dir2.exists()) {
				dir2.mkdir();
			}
			for (Entry<String, StringBuilder> entry : ServidorSocket.REGISTROS.entrySet()) {
				if (!PARAM_REGISTRO_LOGS_JUGADORES && !ServidorSocket.JUGADORES_REGISTRAR.contains(entry.getKey())) {
					continue;
				}
				PrintStream log = new PrintStream(new FileOutputStream("Logs_Players_" + NOMBRE_SERVER + "/" + dia + "/Log_"
				+ entry.getKey() + "_" + dia + ".txt", true));
				log.println(entry.getValue().toString());
				log.flush();
				log.close();
			}
			ServidorSocket.REGISTROS.clear();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("resource")
	public static void imprimirLogCombates() {
		try {
			if (Pelea.LOG_COMBATES.length() == 0) {
				return;
			}
			final String fecha = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" + (Calendar.getInstance().get(
			Calendar.MONTH) + 1) + "-" + Calendar.getInstance().get(Calendar.YEAR);
			try {
				FileOutputStream f = new FileOutputStream("Logs_Combates_" + NOMBRE_SERVER + "/Log_Combates_" + fecha + ".txt",
				true);
				PrintStream log = new PrintStream(f);
				log.flush();
			} catch (final IOException e) {
				new File("Logs_Combates_" + NOMBRE_SERVER).mkdir();
				PrintStream log = new PrintStream(new FileOutputStream("Log_Combates_" + NOMBRE_SERVER + "/Log_Combates_"
				+ fecha + ".txt", true));
				log.println("----- FECHA -----\t- TIPO -\t-- MAPA --\t-------- PANEL RESULTADOS --------");
				log.flush();
				log.close();
			}
			PrintStream log = new PrintStream(new FileOutputStream("Log_Combates_" + NOMBRE_SERVER + "/Log_Combates_" + fecha
			+ ".txt", true));
			log.println(Pelea.LOG_COMBATES.toString());
			log.flush();
			log.close();
			Pelea.LOG_COMBATES = new StringBuilder();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("resource")
	public static void imprimirLogSQL() {
		try {
			if (GestorSQL.LOG_SQL.length() == 0) {
				return;
			}
			final String fecha = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" + (Calendar.getInstance().get(
			Calendar.MONTH) + 1) + "-" + Calendar.getInstance().get(Calendar.YEAR);
			try {
				FileOutputStream f = new FileOutputStream("Logs_SQL_" + NOMBRE_SERVER + "/Log_SQL_" + fecha + ".txt", true);
				PrintStream log = new PrintStream(f);
				log.flush();
			} catch (final IOException e) {
				new File("Logs_SQL_" + NOMBRE_SERVER).mkdir();
			}
			PrintStream log = new PrintStream(new FileOutputStream("Logs_SQL_" + NOMBRE_SERVER + "/Log_SQL_" + fecha + ".txt",
			true));
			log.println(GestorSQL.LOG_SQL.toString());
			log.flush();
			log.close();
			GestorSQL.LOG_SQL = new StringBuilder();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void cerrarServer() {
		// GestorSalida.ENVIAR_M145_MENSAJE_PANEL_INFORMACION_TODOS("CERRANDO SERVIDOR / CLOSING SERVER
		// / FERMER SERVEUR");
		GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("115;1 seconde");
		redactarLogServidorln(" ######## INICIANDO CIERRE DEL SERVIDOR  ########");
		try {
			boolean estabaCorriendo = Mundo.SERVIDOR_ESTADO != Constantes.SERVIDOR_OFFLINE;
			Mundo.setServidorEstado(Constantes.SERVIDOR_OFFLINE);
			if (estabaCorriendo) {
				// GestorSQL.UPDATE_TODAS_CUENTAS_CERO();
				Mundo.devolverBoletos();
				while (Mundo.SALVANDO) {
					try {
						Thread.sleep(5000);
					} catch (final Exception e) {}
				}
				Mundo.salvarServidor(false);
				Mundo.salvarMapasEstrellas();
				try {
					Thread.sleep(1000);
				} catch (final Exception e) {}
				redactarLogServidor(" ########  CERRANDO SERVERSOCKET...  ");
				ServidorServer.cerrarSocketServidor();
				redactarLogServidorln("... IS OK  ########");
				if (!PARAM_AUTO_COMMIT) {
					GestorSQL.iniciarCommit(false);
					redactarLogServidorln("######## ESPERANDO COMMIT SQL  ########");
					try {
						Thread.sleep(MILISEGUNDOS_CERRAR_SERVIDOR);
					} catch (final Exception e) {}
				}
			}
			try {
				Thread.sleep(1000);
			} catch (final Exception e) {}
		} catch (Exception e) {
			redactarLogServidorln("EXCEPTION MIENTRAS SE CERRABA EL SERVIDOR : " + e.toString());
			e.printStackTrace();
		}
		redactarLogServidorln(" ########  IMPRIMIENDO LOGS PLAYERS  ########");
		imprimirLogPlayers();
		if (PARAM_REGISTRO_LOGS_SQL) {
			redactarLogServidorln(" ########  IMPRIMIENDO LOGS SQL  ########");
			imprimirLogPlayers();
		}
		redactarLogServidorln(" ########  SERVIDOR CERRO CON EXITO  ########");
	}
	
	public static void resetRates() {
		RATE_XP_PVM = DEFECTO_XP_PVM;
		RATE_XP_PVP = DEFECTO_XP_PVP;
		RATE_XP_OFICIO = DEFECTO_XP_OFICIO;
		RATE_HONOR = DEFECTO_XP_HONOR;
		RATE_DROP_NORMAL = DEFECTO_DROP;
		RATE_KAMAS = DEFECTO_KAMAS;
		RATE_CRIANZA_MONTURA = DEFECTO_CRIANZA_MONTURA;
	}
	private static class IniciarSincronizacion extends Thread {
		private static boolean ACTIVO = false;
		
		public IniciarSincronizacion() {
			if (ACTIVO) {
				return;
			}
			ACTIVO = true;
			this.setDaemon(true);
			this.setPriority(5);
			this.start();
		}
		
		public void run() {
			new SincronizadorSocket();
			ACTIVO = false;
		}
	}
}
