package estaticos;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import com.mysql.jdbc.PreparedStatement;
import servidor.ServidorServer;
import servidor.ServidorSocket;
import sincronizador.SincronizadorSocket;
import variables.casa.Casa;
import variables.casa.Cofre;
import variables.encarnacion.EncarnacionModelo;
import variables.gremio.Gremio;
import variables.gremio.Recaudador;
import variables.hechizo.Hechizo;
import variables.mapa.Area;
import variables.mapa.Cercado;
import variables.mapa.Mapa;
import variables.mapa.SubArea;
import variables.mapa.SuperArea;
import variables.mapa.interactivo.ObjetoInteractivo;
import variables.mapa.interactivo.ObjetoInteractivoModelo;
import variables.mapa.interactivo.OtroInteractivo;
import variables.mercadillo.Mercadillo;
import variables.mision.MisionEtapaModelo;
import variables.mision.MisionModelo;
import variables.mision.MisionObjetivoModelo;
import variables.mob.GrupoMob;
import variables.mob.MobModelo;
import variables.montura.Montura;
import variables.montura.Montura.Ubicacion;
import variables.montura.MonturaModelo;
import variables.npc.NPCModelo;
import variables.npc.ObjetoTrueque;
import variables.npc.PreguntaNPC;
import variables.npc.RespuestaNPC;
import variables.objeto.CreaTuItem;
import variables.objeto.MascotaModelo;
import variables.objeto.Objeto;
import variables.objeto.ObjetoModelo;
import variables.objeto.ObjetoModelo.CAPACIDAD_STATS;
import variables.objeto.ObjetoSet;
import variables.oficio.Oficio;
import variables.pelea.DropMob;
import variables.pelea.Pelea;
import variables.personaje.Clase;
import variables.personaje.Cuenta;
import variables.personaje.Especialidad;
import variables.personaje.GrupoKoliseo;
import variables.personaje.Personaje;
import variables.ranking.RankingKoliseo;
import variables.ranking.RankingPVP;
import variables.zotros.Almanax;
import variables.zotros.Animacion;
import variables.zotros.MasterReset;
import variables.zotros.Ornamento;
import variables.zotros.Prisma;
import variables.zotros.Servicio;
import variables.zotros.TiendaCategoria;
import variables.zotros.TiendaObjetos;
import variables.zotros.Titulo;
import variables.zotros.Tutorial;

public class Mundo {
	// Fijos
	public static Map<Short, Mapa> MAPAS = new TreeMap<Short, Mapa>();
	public static Map<Integer, Area> AREAS = new TreeMap<Integer, Area>();
	public static Map<Integer, SuperArea> SUPER_AREAS = new TreeMap<Integer, SuperArea>();
	public static Map<Integer, SubArea> SUB_AREAS = new TreeMap<Integer, SubArea>();
	public static Map<Short, Cercado> CERCADOS = new HashMap<Short, Cercado>();
	public static Map<Integer, Experiencia> EXPERIENCIA = new TreeMap<Integer, Experiencia>();
	public static Map<Integer, Hechizo> HECHIZOS = new HashMap<Integer, Hechizo>();
	public static Map<Integer, ObjetoModelo> OBJETOS_MODELOS = new HashMap<Integer, ObjetoModelo>();
	public static Map<Short, String> SISTEMA_ITEMS = new HashMap<Short, String>();
	public static Map<Integer, MobModelo> MOBS_MODELOS = new HashMap<Integer, MobModelo>();
	public static Map<Integer, MonturaModelo> MONTURAS_MODELOS = new HashMap<Integer, MonturaModelo>();
	public static Map<Integer, NPCModelo> NPC_MODELOS = new HashMap<Integer, NPCModelo>();
	public static Map<Integer, PreguntaNPC> NPC_PREGUNTAS = new HashMap<Integer, PreguntaNPC>();
	public static Map<Integer, RespuestaNPC> NPC_RESPUESTAS = new HashMap<Integer, RespuestaNPC>();
	public static Map<Integer, Oficio> OFICIOS = new HashMap<Integer, Oficio>();
	public static Map<Integer, ArrayList<Duo<Integer, Integer>>> RECETAS = new HashMap<Integer, ArrayList<Duo<Integer, Integer>>>();
	public static Map<Integer, ObjetoSet> OBJETOS_SETS = new HashMap<Integer, ObjetoSet>();
	public static Map<Integer, Casa> CASAS = new HashMap<Integer, Casa>();
	public static Map<Integer, Mercadillo> MERCADILLOS = new HashMap<Integer, Mercadillo>();
	public static Map<Integer, Animacion> ANIMACIONES = new HashMap<Integer, Animacion>();
	public static Map<Integer, Cofre> COFRES = new HashMap<Integer, Cofre>();
	public static Map<Integer, Tutorial> TUTORIALES = new HashMap<Integer, Tutorial>();
	public static Map<Integer, MisionObjetivoModelo> OBJETIVOS_MODELOS = new HashMap<Integer, MisionObjetivoModelo>();
	public static Map<Integer, EncarnacionModelo> ENCARNACIONES_MODELOS = new HashMap<Integer, EncarnacionModelo>();
	public static Map<Integer, MisionEtapaModelo> ETAPAS = new HashMap<Integer, MisionEtapaModelo>();
	public static Map<Integer, MisionModelo> MISIONES_MODELOS = new HashMap<Integer, MisionModelo>();
	public static Map<Integer, MascotaModelo> MASCOTAS_MODELOS = new TreeMap<Integer, MascotaModelo>();
	public static Map<Integer, Especialidad> ESPECIALIDADES = new TreeMap<Integer, Especialidad>();
	public static Map<Integer, Integer> DONES_MODELOS = new TreeMap<Integer, Integer>();
	public static ArrayList<ObjetoInteractivoModelo> OBJETOS_INTERACTIVOS_MODELOS = new ArrayList<ObjetoInteractivoModelo>();
	public static Map<Integer, Servicio> SERVICIOS = new HashMap<Integer, Servicio>();
	public static Map<String, Integer> COMANDOS = new HashMap<String, Integer>();
	public static Map<Short, ArrayList<Short>> MAPAS_ESTRELLAS = new HashMap<Short, ArrayList<Short>>();
	public static Map<Short, ArrayList<String>> MAPAS_HEROICOS = new HashMap<Short, ArrayList<String>>();
	public static Map<Byte, ArrayList<Duo<Integer, Integer>>> MOBS_EVENTOS = new HashMap<Byte, ArrayList<Duo<Integer, Integer>>>();
	public static Map<Integer, Almanax> ALMANAX = new HashMap<Integer, Almanax>();
	public static CopyOnWriteArrayList<OtroInteractivo> OTROS_INTERACTIVOS = new CopyOnWriteArrayList<OtroInteractivo>();
	public static ArrayList<ObjetoInteractivo> OBJETOS_INTERACTIVOS = new ArrayList<ObjetoInteractivo>();
	public static ArrayList<DropMob> DROPS_FIJOS = new ArrayList<DropMob>();
	public static ArrayList<ObjetoTrueque> OBJETOS_TRUEQUE = new ArrayList<>();
	public static Map<Short, Short> ZAAPS = new HashMap<Short, Short>();
	public static ArrayList<Short> ZAAPIS_BONTA = new ArrayList<Short>();
	public static ArrayList<Short> ZAAPIS_BRAKMAR = new ArrayList<Short>();
	public static Map<Integer, Clase> CLASES = new TreeMap<Integer, Clase>();
	public static Map<Integer, CreaTuItem> CREA_TU_ITEM = new TreeMap<Integer, CreaTuItem>();
	public static Map<Integer, String> RULETA = new TreeMap<Integer, String>();
	public static Map<Integer, Ornamento> ORNAMENTOS = new TreeMap<Integer, Ornamento>();
	public static Map<Integer, Titulo> TITULOS = new TreeMap<Integer, Titulo>();
	public static Map<Integer, MasterReset> MASTER_RESET = new TreeMap<>();
	
	
	private Map<Integer, TiendaCategoria> shopCategoria = new HashMap<>();
	public static Map<Integer, TiendaCategoria> TiendaCategoria = Collections
			.synchronizedMap(new HashMap<Integer, TiendaCategoria>());

	private Map<Integer, TiendaObjetos> shopObjetos = new HashMap<>();
	public static Map<Integer, TiendaObjetos> tiendaObjetos = Collections
			.synchronizedMap(new HashMap<Integer, TiendaObjetos>());
	//
	// concurrentes
	//
	private static ConcurrentHashMap<Integer, Cuenta> _CUENTAS = new ConcurrentHashMap<Integer, Cuenta>();
	private static ConcurrentHashMap<String, Integer> _CUENTAS_POR_NOMBRE = new ConcurrentHashMap<String, Integer>();
	private static ConcurrentHashMap<Integer, Personaje> _PERSONAJES = new ConcurrentHashMap<Integer, Personaje>();
	private static ConcurrentHashMap<Integer, Objeto> _OBJETOS = new ConcurrentHashMap<Integer, Objeto>();
	private static ConcurrentHashMap<Integer, Gremio> _GREMIOS = new ConcurrentHashMap<Integer, Gremio>();
	private static ConcurrentHashMap<Integer, Montura> _MONTURAS = new ConcurrentHashMap<Integer, Montura>();
	private static ConcurrentHashMap<Integer, Prisma> _PRISMAS = new ConcurrentHashMap<Integer, Prisma>();
	private static ConcurrentHashMap<Integer, Recaudador> _RECAUDADORES = new ConcurrentHashMap<Integer, Recaudador>();
	private static ConcurrentHashMap<Integer, RankingKoliseo> _RANKINGS_KOLISEO = new ConcurrentHashMap<Integer, RankingKoliseo>();
	private static ConcurrentHashMap<Integer, RankingPVP> _RANKINGS_PVP = new ConcurrentHashMap<Integer, RankingPVP>();
	// private static ConcurrentHashMap<Integer, Encarnacion> _ENCARNACIONES = new
	// ConcurrentHashMap<Integer, Encarnacion>();
	private static ConcurrentHashMap<Integer, Personaje> _INSCRITOS_KOLISEO = new ConcurrentHashMap<Integer, Personaje>();
	//
	// publicas
	//
	public static Map<Short, Short> ZONAS = new HashMap<Short, Short>();
	// public static ArrayList<Short> MAPAS_OBJETIVOS = new ArrayList<>();
	public static ArrayList<String> CAPTCHAS = new ArrayList<String>();
	public static ArrayList<Cuenta> CUENTAS_A_BORRAR = new ArrayList<Cuenta>();
	private static CopyOnWriteArrayList<Personaje> ONLINES = new CopyOnWriteArrayList<>();
	//
	// variables primitivas
	//
	public static boolean BLOQUEANDO, VENDER_BOLETOS;
	public static byte SERVIDOR_ESTADO = Constantes.SERVIDOR_OFFLINE;
	public static int[] LOTERIA_BOLETOS = new int[10000];
	public static int SIG_ID_LINEA_MERCADILLO, SIG_ID_OBJETO, SIG_ID_PERSONAJE, SIG_ID_MONTURA = -101,
	SIG_ID_RECAUDADOR = -100, SIG_ID_PRISMA = -102, CANT_SALVANDO, TOTAL_SALVADO, SEGUNDOS_INICIO_KOLISEO, DIA_DEL_AÑO;
	public static byte MOB_EVENTO;
	public static long SEG_CUENTA_REGRESIVA, MINUTOS_VIDA_REAL;
	public static String LIDER_RANKING = "Ninguno", MSJ_CUENTA_REGRESIVA = "", LISTA_GFX = "", LISTA_NIVEL = "",
	LISTA_ZONAS = "", KAMAS_OBJ_CACERIA = "", NOMBRE_CACERIA = "", LISTA_MASCOTAS = "", CLASES_PERMITIDAS = "",
	CREA_TU_ITEM_OBJETOS = "", CREA_TU_ITEM_DATA = "", CREAT_TU_ITEM_PRECIOS = "";
	private static CopyOnWriteArrayList<RankingKoliseo> _LADDER_KOLISEO = new CopyOnWriteArrayList<>();
	private static CopyOnWriteArrayList<RankingPVP> _LADDER_PVP = new CopyOnWriteArrayList<>();
	private static CopyOnWriteArrayList<Personaje> _LADDER_NIVEL = new CopyOnWriteArrayList<>();
	private static CopyOnWriteArrayList<Personaje> _LADDER_EXP_DIA = new CopyOnWriteArrayList<>();
	private static CopyOnWriteArrayList<Gremio> _LADDER_GREMIO = new CopyOnWriteArrayList<>();
	
	
	public void addTiendaCategoria(TiendaCategoria shopCategoria) {
		this.shopCategoria.put(shopCategoria.getId(), shopCategoria);
	}

	public TiendaCategoria getTiendaCategoria(int shopCategoria) {
		return this.shopCategoria.get(shopCategoria);
	}

	public void addTiendaObjetos(TiendaObjetos shopCategoria) {
		this.shopObjetos.put(shopCategoria.getId(), shopCategoria);
	}

	public TiendaObjetos getTiendaObjetos(int shopCategoria) {
		return this.shopObjetos.get(shopCategoria);
	}

	public static TiendaCategoria getTiendaCategoria2(final int id) {
		return Mundo.TiendaCategoria.get(id);
	}

	public static TiendaObjetos getTiendaObjetos2(final int id) {
		return Mundo.tiendaObjetos.get(id);
	}
	
	
	
	public static void crearServidor() {
		try {
			System.out.println("TotalMemory: " + Runtime.getRuntime().totalMemory() / 1048576f + " MB\t" + "MaxMemory: "
			+ Runtime.getRuntime().maxMemory() / 1048576f + " MB");
		} catch (Exception e) {}
		for (String s : Constantes.ZAAPI_BONTA.split(",")) {
			if (s.isEmpty()) {
				continue;
			}
			try {
				ZAAPIS_BONTA.add(Short.parseShort(s));
			} catch (Exception e) {}
		}
		for (String s : Constantes.ZAAPI_BRAKMAR.split(",")) {
			if (s.isEmpty()) {
				continue;
			}
			try {
				ZAAPIS_BRAKMAR.add(Short.parseShort(s));
			} catch (Exception e) {}
		}
		DIA_DEL_AÑO = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		MINUTOS_VIDA_REAL = Constantes.getTiempoActualEscala(1000 * 60);
		SEGUNDOS_INICIO_KOLISEO = MainServidor.SEGUNDOS_INICIAR_KOLISEO;
		System.out.println("===========> Database Static <===========");
		GestorSQL.CARGAR_CREA_OBJETOS_PRECIOS();
		GestorSQL.CARGAR_CREA_OBJETOS_MODELOS();
		for (CreaTuItem c : CREA_TU_ITEM.values()) {
			if (!CREA_TU_ITEM_OBJETOS.isEmpty()) {
				CREA_TU_ITEM_OBJETOS += ",";
				CREA_TU_ITEM_DATA += "|";
			}
			CREA_TU_ITEM_OBJETOS += c.getID();
			CREA_TU_ITEM_DATA += c.getID() + ";" + c.getMaximosStats() + ";" + c.getMaxOgrinas() + ";" + c.getPrecioBase();
		}
		System.out.print("Cargando las clases: ");
		GestorSQL.CARGAR_CLASES();
		System.out.println(CLASES.size() + " clases cargadas");
		for (Clase c : CLASES.values()) {
			if (!CLASES_PERMITIDAS.isEmpty()) {
				CLASES_PERMITIDAS += ",";
			}
			CLASES_PERMITIDAS += c.getID();
		}
		System.out.print("Cargando los servicios: ");
		GestorSQL.CARGAR_SERVICIOS();
		System.out.println(SERVICIOS.size() + " servicios cargados");
		System.out.print("Cargando los ornamentos: ");
		GestorSQL.CARGAR_ORNAMENTOS();
		System.out.println(ORNAMENTOS.size() + " ornamentos cargados");
		System.out.print("Cargando los titulos: ");
		GestorSQL.CARGAR_TITULOS();
		System.out.println(TITULOS.size() + " titulos cargados");
		System.out.print("Cargando los comandos modelo: ");
		GestorSQL.CARGAR_COMANDOS_MODELO();
		System.out.println(COMANDOS.size() + " comandos modelo cargados");
		System.out.print("Cargando los dones: ");
		GestorSQL.CARGAR_DONES_MODELOS();
		System.out.println(DONES_MODELOS.size() + " dones cargados");
		System.out.print("Cargando las especialidades: ");
		GestorSQL.CARGAR_ESPECIALIDADES();
		System.out.println(ESPECIALIDADES.size() + " especialidades cargadas");
		System.out.print("Cargando los misiones almanax: ");
		GestorSQL.CARGAR_ALMANAX();
		System.out.println(ALMANAX.size() + " almanax cargados");
		System.out.print("Cargando los niveles de experiencia: ");
		GestorSQL.CARGAR_EXPERIENCIA();
		EXPERIENCIA.put(MainServidor.NIVEL_MAX_PERSONAJE + 1, new Experiencia(8223372036854775808L, -1, -1, -1, -1, -1));
		System.out.println(EXPERIENCIA.size() + " niveles cargados");
		System.out.print("Cargando los hechizos: ");
		GestorSQL.CARGAR_HECHIZOS();
		System.out.println(HECHIZOS.size() + " hechizos cargados");
		System.out.print("Cargando las encarnaciones modelos: ");
		GestorSQL.CARGAR_ENCARNACIONES_MODELOS();
		System.out.println(ENCARNACIONES_MODELOS.size() + " encarnaciones modelo cargados");
		System.out.print("Cargando los mounstros: ");
		GestorSQL.CARGAR_MOBS_MODELOS();
		GestorSQL.CARGAR_MOBS_RAROS();
		GestorSQL.CARGAR_MOBS_EVENTO();
		System.out.println(MOBS_MODELOS.size() + " mounstros cargados");
		System.out.print("Cargando los objetos modelos: ");
		GestorSQL.CARGAR_OBJETOS_MODELOS();
		System.out.println(OBJETOS_MODELOS.size() + " objetos modelos cargados");
		System.out.print("Cargando los sets de objetos: ");
		GestorSQL.CARGAR_OBJETOS_SETS();
		System.out.println(OBJETOS_SETS.size() + " set de objetos cargados");
		System.out.print("Cargando las monturas modelos: ");
		GestorSQL.CARGAR_MONTURAS_MODELOS();
		System.out.println(MONTURAS_MODELOS.size() + " monturas modelos cargados");
		System.out.print("Cargando los objetos trueque: ");
		GestorSQL.CARGAR_OBJETOS_TRUEQUE();
		System.out.println(OBJETOS_TRUEQUE.size() + " objetos trueque cargados");
		System.out.print("Cargando los drops: ");
		System.out.println(GestorSQL.CARGAR_DROPS() + " drops cargados");
		System.out.print("Cargando los drops fijos: ");
		System.out.println(GestorSQL.CARGAR_DROPS_FIJOS() + " drops fijos cargados");
		System.out.print("Cargando los NPC: ");
		GestorSQL.CARGAR_NPC_MODELOS();
		System.out.println(NPC_MODELOS.size() + " NPC cargados");
		System.out.print("Cargando las preguntas de NPC: ");
		GestorSQL.CARGAR_PREGUNTAS();
		System.out.println(NPC_PREGUNTAS.size() + " preguntas de NPC cargadas");
		System.out.print("Cargando las respuestas de NPC: ");
		GestorSQL.CARGAR_RESPUESTAS();
		System.out.println(NPC_RESPUESTAS.size() + " respuestas de NPC cargadas");
		System.out.print("Cargando las areas: ");
		GestorSQL.CARGAR_AREA();
		System.out.println(AREAS.size() + " areas cargadas");
		System.out.print("Cargando las sub-areas: ");
		GestorSQL.CARGAR_SUBAREA();
		System.out.println(SUB_AREAS.size() + " sub-areas cargadas");
		System.out.print("Cargando los objetos interactivos: ");
		GestorSQL.CARGAR_INTERACTIVOS();
		System.out.println(OBJETOS_INTERACTIVOS_MODELOS.size() + " objetos interactivos cargados");
		System.out.print("Cargando las recetas: ");
		GestorSQL.CARGAR_RECETAS();
		System.out.println(RECETAS.size() + " recetas cargadas");
		System.out.print("Cargando los oficios: ");
		GestorSQL.CARGAR_OFICIOS();
		System.out.println(OFICIOS.size() + " oficios cargados");
		System.out.print("Cargando los objetivos: ");
		GestorSQL.CARGAR_MISION_OBJETIVOS();
		System.out.println(OBJETIVOS_MODELOS.size() + " objetivos cargados");
		System.out.print("Cargando las etapas: ");
		GestorSQL.CARGAR_ETAPAS();
		System.out.println(ETAPAS.size() + " etapas cargadas");
		System.out.print("Cargando los misiones: ");
		GestorSQL.CARGAR_MISIONES();
		System.out.println(MISIONES_MODELOS.size() + " misiones cargados");
		GestorSQL.CARGAR_MAPAS_ESTRELLAS();
		if (MainServidor.MODO_HEROICO || !MainServidor.MAPAS_MODO_HEROICO.isEmpty()) {
			System.out.print("Cargando los mapas heroicos: ");
			GestorSQL.CARGAR_MAPAS_HEROICO();
			System.out.println(MAPAS_HEROICOS.size() + " mapas heroicos cargados");
		}
		System.out.print("Cargando los mapas: ");
		long xxxx = System.currentTimeMillis();
		GestorSQL.CARGAR_MAPAS();
		System.out.println(MAPAS.size() + " mapas cargados ----> (en " + (System.currentTimeMillis() - xxxx)
		+ ") milisegundos");
		System.out.print("Cargando los grupo mobs fijos: ");
		System.out.println(GestorSQL.CARGAR_MOBS_FIJOS() + " grupo mobs fijos cargados");
		System.out.print("Cargando los zaaps: ");
		GestorSQL.CARGAR_ZAAPS();
		System.out.println(ZAAPS.size() + " zaaps cargados");
		System.out.print("Cargando los triggers: ");
		System.out.println(GestorSQL.CARGAR_TRIGGERS() + " trigger cargados");
		System.out.print("Cargando las acciones de pelea: ");
		System.out.println(GestorSQL.CARGAR_ACCION_FINAL_DE_PELEA() + " acciones de pelea cargadas");
		System.out.print("Cargando los NPCs: ");
		System.out.println(GestorSQL.CARGAR_NPCS() + " NPCs cargados");
		System.out.print("Cargando las acciones de objetos: ");
		System.out.println(GestorSQL.CARGAR_ACCIONES_USO_OBJETOS() + " acciones de objetos cargados");
		System.out.print("Cargando las animaciones: ");
		GestorSQL.SELECT_ANIMACIONES();
		System.out.println(ANIMACIONES.size() + " animaciones cargadas");
		System.out.print("Cargando los otros interactivos: ");
		GestorSQL.CARGAR_OTROS_INTERACTIVOS();
		System.out.println(OTROS_INTERACTIVOS.size() + " otros interactivos cargados");
		System.out.print("Cargando las comidas de mascotas: ");
		System.out.println(GestorSQL.CARGAR_COMIDAS_MASCOTAS() + " comidas de mascotas cargadas");
		System.out.print("Cargando los tutoriales: ");
		GestorSQL.CARGAR_TUTORIALES();
		System.out.println(TUTORIALES.size() + " tutoriales cargados");
		System.out.print("Cargando las zonas: ");
		GestorSQL.SELECT_ZONAS();
		System.out.println(ZONAS.size() + " zonas cargados");
		System.out.println("===========> Database Dynamic <===========");
		System.out.print("Cargando los objetos: ");
		GestorSQL.CARGAR_OBJETOS();
		System.out.println(_OBJETOS.size() + " objetos cargados");
		System.out.print("Cargando los dragopavos: ");
		GestorSQL.CARGAR_MONTURAS();
		System.out.println(_MONTURAS.size() + " dragopavos cargados");
		System.out.print("Cargando los puesto mercadillos: ");
		GestorSQL.SELECT_PUESTOS_MERCADILLOS();
		System.out.println(MERCADILLOS.size() + " puestos mercadillos cargados");
		System.out.print("Cargando las cuentas: ");
		GestorSQL.CARGAR_DB_CUENTAS();
		GestorSQL.CARGAR_CUENTAS_SERVER_PERSONAJE();
		System.out.println(_CUENTAS.size() + " cuentas cargadas");
		System.out.print("Cargando los personajes: ");
		GestorSQL.CARGAR_PERSONAJES();
		System.out.println(_PERSONAJES.size() + " personajes cargados");
		System.out.print("Cargando los objetos mercadillos: ");
		System.out.println(GestorSQL.SELECT_OBJETOS_MERCADILLO() + " objetos mercadillos cargados");
		System.out.print("Cargando los rankings PVP: ");
		GestorSQL.SELECT_RANKING_PVP();
		System.out.println(_RANKINGS_PVP.size() + " rankings PVP cargados cargados");
		System.out.print("Cargando los prismas: ");
		GestorSQL.CARGAR_PRISMAS();
		System.out.println(_PRISMAS.size() + " prismas cargados");
		System.out.print("Cargando los rankings Koliseo: ");
		GestorSQL.SELECT_RANKING_KOLISEO();
		System.out.println(_RANKINGS_KOLISEO.size() + " rankings Koliseo cargados cargados");
		System.out.print("Cargando los gremios: ");
		GestorSQL.CARGAR_GREMIOS();
		System.out.println(_GREMIOS.size() + " gremios cargados");
		System.out.print("Cargando los miembros de gremio: ");
		System.out.println(GestorSQL.CARGAR_MIEMBROS_GREMIO() + " miembros de gremio cargados");
		System.out.print("Cargando los recaudadores: ");
		GestorSQL.CARGAR_RECAUDADORES();
		System.out.println(_RECAUDADORES.size() + " recaudadores cargados");
		System.out.print("Cargando los cercados: ");
		GestorSQL.CARGAR_CERCADOS();
		GestorSQL.RECARGAR_CERCADOS();
		System.out.println(CERCADOS.size() + " cercados cargados");
		GestorSQL.CARGAR_TIENDA_CATEGORIA();
		System.out.print("Loading tienda categoria.");
		GestorSQL.CARGAR_TIENDA_OBJETO();
		System.out.print("Loading tienda objetos.");
		GestorSQL.CARGAR_TIENDA_CATEGORIA();
		System.out.print("Loading tienda categoria.");
		GestorSQL.CARGAR_TIENDA_OBJETO();
		System.out.print("Loading tienda objetos.");
		System.out.print("Cargando las casas: ");
		GestorSQL.CARGAR_CASAS();
		GestorSQL.RECARGAR_CASAS();
		System.out.println(CASAS.size() + " casas cargadas");
		System.out.print("Cargando los cofres: ");
		GestorSQL.CARGAR_COFRES();
		GestorSQL.RECARGAR_COFRES();
		System.out.println(COFRES.size() + " cofres cargados");
		SIG_ID_OBJETO = GestorSQL.GET_SIG_ID_OBJETO();
		try {
			if (!CUENTAS_A_BORRAR.isEmpty()) {
				int eliminados = 0;
				Thread.sleep(100);
				for (final Cuenta cuenta : CUENTAS_A_BORRAR) {
					for (final Personaje perso : cuenta.getPersonajes()) {
						if (perso == null) {
							continue;
						}
						cuenta.eliminarPersonaje(perso.getID());
						eliminados++;
					}
				}
				if (eliminados > 0) {
					MainServidor.redactarLogServidorln("\nSe eliminaron " + eliminados
					+ " personajes con sus objetos, dragopavos, casas\n");
				}
				Thread.sleep(100);
			}
		} catch (final Exception e) {}
		actualizarRankings();
		prepararListaGFX();
		prepararListaNivel();
		prepararPanelItems();
		listaMascotas();
		// lanzamiento del server
		setServidorEstado(Constantes.SERVIDOR_ONLINE);
	}
	
	public static boolean esZaapi(short mapaID, byte alineacion) {
		if (alineacion == Constantes.ALINEACION_BONTARIANO) {
			return ZAAPIS_BONTA.contains(mapaID);
		} else if (alineacion == Constantes.ALINEACION_BRAKMARIANO) {
			return ZAAPIS_BRAKMAR.contains(mapaID);
		}
		return ZAAPIS_BONTA.contains(mapaID) || ZAAPIS_BRAKMAR.contains(mapaID);
	}
	
	public static void setServidorEstado(byte estado) {
		SERVIDOR_ESTADO = estado;
		SincronizadorSocket.sendPacket("S" + SERVIDOR_ESTADO, true);
	}
	
	private static void listaMascotas() {
		StringBuilder s = new StringBuilder();
		for (ObjetoModelo o : Mundo.OBJETOS_MODELOS.values()) {
			if (o.getTipo() == Constantes.OBJETO_TIPO_MASCOTA) {
				if (s.length() > 0) {
					s.append(",");
				}
				s.append(o.getID());
			}
		}
		LISTA_MASCOTAS = s.toString();
	}
	
	public static Servicio getServicio(int id) {
		return SERVICIOS.get(id);
	}
	
	public static void addServicio(Servicio servicio) {
		SERVICIOS.put(servicio.getID(), servicio);
	}
	
	public static void addEncarnacionModelo(EncarnacionModelo encarnacion) {
		ENCARNACIONES_MODELOS.put(encarnacion.getGfxID(), encarnacion);
	}
	
	public static String mensajeCaceria() {
		final StringBuilder s = new StringBuilder();
		final StringBuilder s2 = new StringBuilder();
		final String[] param = KAMAS_OBJ_CACERIA.split(Pattern.quote("|"));
		if (param.length > 1) {
			byte i = 0;
			for (final String a : param[1].split(";")) {
				try {
					final String[] b = a.split(",");
					final String stats = getObjetoModelo(Integer.parseInt(b[0])).stringStatsModelo();
					if (s.length() > 0) {
						s.append(", ");
					}
					s.append("°" + i + "x" + b[1]);
					if (s2.length() > 0) {
						s2.append("!");
					}
					s2.append(b[0] + "!" + stats);
					i++;
				} catch (final Exception e) {}
			}
		}
		s.append(", " + param[0] + " Kamas|" + s2.toString());
		return s.toString();
	}
	
	public static void addMision(final MisionModelo mision) {
		MISIONES_MODELOS.put(mision.getID(), mision);
	}
	
	public static MisionModelo getMision(final int id) {
		return MISIONES_MODELOS.get(id);
	}
	
	public static void addAlmanax(Almanax almanax) {
		ALMANAX.put(almanax.getID(), almanax);
	}
	
	public static Almanax getAlmanax(int id) {
		return ALMANAX.get(id);
	}
	
	public static Almanax getAlmanaxDelDia() {
		return ALMANAX.get(DIA_DEL_AÑO);
	}
	
	public static Clase getClase(int clase) {
		return CLASES.get(clase);
	}
	
	public static void addOrnamento(final Ornamento ornamento) {
		ORNAMENTOS.put(ornamento.getID(), ornamento);
	}
	
	public static Ornamento getOrnamento(int id) {
		return ORNAMENTOS.get(id);
	}
	public static MasterReset getMasterReset(int id) {
		return MASTER_RESET.get(id);
	}

	public static boolean getMasterResetExist(int id) {
		return MASTER_RESET.containsKey(id);
	}
	public static void addTitulo(final Titulo ornamento) {
		TITULOS.put(ornamento.getID(), ornamento);
	}
	
	public static Titulo getTitulo(int id) {
		return TITULOS.get(id);
	}
	
	public static String listarOrnamentos(Personaje perso) {
		StringBuilder str = new StringBuilder();
		for (Ornamento o : ORNAMENTOS.values()) {
			if (!o.esValido()) {
				continue;
			}
			if (perso.tieneOrnamento(o.getID())) {
				if (str.length() > 0) {
					str.append(";");
				}
				str.append(o.getID());
			} else if (o.esParaVender()) {
				if (str.length() > 0) {
					str.append(";");
				}
				str.append(o.getID() + "," + o.getPrecioStr());
			}
		}
		return str.toString();
	}
	
	public static String listarTitulos(Personaje perso) {
		StringBuilder str = new StringBuilder();
		for (Titulo o : TITULOS.values()) {
			if (!o.esValido()) {
				continue;
			}
			if (perso.tieneTitulo(o.getID())) {
				if (str.length() > 0) {
					str.append(";");
				}
				str.append(o.getID());
			} else if (o.esParaVender()) {
				if (str.length() > 0) {
					str.append(";");
				}
				str.append(o.getID() + "," + o.getPrecioStr());
			}
		}
		return str.toString();
	}
	
	public static void addComando(String comando, int rango) {
		COMANDOS.put(comando.toUpperCase(), rango);
	}
	
	public static int getRangoComando(String comando) {
		if (COMANDOS.get(comando) == null) {
			return 0;
		}
		return COMANDOS.get(comando);
	}
	
	public static void addEtapa(final int id, final String recompensas, final String steps, final String nombre) {
		ETAPAS.put(id, new MisionEtapaModelo(id, recompensas, steps, nombre));
	}
	
	public static MisionEtapaModelo getEtapa(final int id) {
		return ETAPAS.get(id);
	}
	
	public static void addDropFijo(DropMob drop) {
		DROPS_FIJOS.add(drop);
	}
	
	public static ArrayList<DropMob> listaDropsFijos() {
		return DROPS_FIJOS;
	}
	
	public static void addMisionObjetivoModelo(final int id, final byte tipo, final String args) {
		OBJETIVOS_MODELOS.put(id, new MisionObjetivoModelo(id, tipo, args));
	}
	
	public static MisionObjetivoModelo getMisionObjetivoModelo(final int id) {
		return OBJETIVOS_MODELOS.get(id);
	}
	
	public static void prepararListaGFX() {
		final StringBuilder str = new StringBuilder();
		for (final ObjetoModelo obj : OBJETOS_MODELOS.values()) {
			if (obj.getGFX() <= 0) {
				continue;
			}
			if (str.length() > 0) {
				str.append(";");
			}
			str.append(obj.getID() + "," + obj.getGFX());
		}
		LISTA_GFX = str.toString();
	}
	
	public static void prepararListaNivel() {
		final StringBuilder str = new StringBuilder();
		for (final ObjetoModelo obj : OBJETOS_MODELOS.values()) {
			if (!obj.getNivelModifi()) {
				continue;
			}
			if (str.length() > 0) {
				str.append(";");
			}
			str.append(obj.getID() + "," + obj.getNivel());
		}
		LISTA_NIVEL = str.toString();
	}
	
	public static void addMobEvento(final byte evento, final int mobOriginal, final int mobEvento) {
		if (MOBS_EVENTOS.get(evento) == null) {
			MOBS_EVENTOS.put(evento, new ArrayList<Duo<Integer, Integer>>());
		}
		MOBS_EVENTOS.get(evento).add(new Duo<Integer, Integer>(mobOriginal, mobEvento));
	}
	
	public static ArrayList<Duo<Integer, Integer>> getMobsEventoDelDia() {
		return MOBS_EVENTOS.get(Mundo.MOB_EVENTO);
	}
	
	public static void refrescarTodosMobs() {
		for (final Mapa mapa : MAPAS.values()) {
			mapa.refrescarGrupoMobs();
		}
	}
	
	public static void resetearStatsObjetos(ArrayList<Integer> idsModelo) {
		ArrayList<Objeto> objetos = new ArrayList<>();
		for (Objeto obj : _OBJETOS.values()) {
			if (idsModelo.contains(obj.getObjModeloID())) {
				objetos.add(obj);
				obj.convertirStringAStats(obj.getObjModelo().generarStatsModelo(CAPACIDAD_STATS.MAXIMO));
			}
		}
		GestorSQL.SALVAR_OBJETOS(objetos);
	}
	
	public static void moverMobs() {
		for (final Mapa mapa : MAPAS.values()) {
			mapa.moverGrupoMobs(MainServidor.CANTIDAD_GRUPO_MOBS_MOVER_POR_MAPA);
		}
	}
	private static class CompNivelMasMenos implements Comparator<Personaje> {
		@Override
		public int compare(Personaje p1, Personaje p2) {
			return new Long(p2.getExperiencia()).compareTo(new Long(p1.getExperiencia()));
		}
	}
	private static class CompDiaMasMenos implements Comparator<Personaje> {
		@Override
		public int compare(Personaje p1, Personaje p2) {
			return new Long(p2.getExperienciaDia()).compareTo(new Long(p1.getExperienciaDia()));
		}
	}
	private static class CompGremioMasMenos implements Comparator<Gremio> {
		@Override
		public int compare(Gremio p1, Gremio p2) {
			return new Long(p2.getExperiencia()).compareTo(new Long(p1.getExperiencia()));
		}
	}
	private static class CompPVPMasMenos implements Comparator<RankingPVP> {
		@Override
		public int compare(RankingPVP p1, RankingPVP p2) {
			int v = new Long(p2.getVictorias()).compareTo(new Long(p1.getVictorias()));
			if (v == 0) {
				return new Long(p1.getDerrotas()).compareTo(new Long(p2.getDerrotas()));
			}
			return v;
		}
	}
	private static class CompKoliseoMasMenos implements Comparator<RankingKoliseo> {
		@Override
		public int compare(RankingKoliseo p1, RankingKoliseo p2) {
			int v = new Long(p2.getVictorias()).compareTo(new Long(p1.getVictorias()));
			if (v == 0) {
				return new Long(p1.getDerrotas()).compareTo(new Long(p2.getDerrotas()));
			}
			return v;
		}
	}
	
	private static void rankingNivel() {
		if (!MainServidor.PARAM_LADDER_NIVEL)
			return;
		ArrayList<Personaje> persos = new ArrayList<>();
		persos.addAll(_PERSONAJES.values());
		Collections.sort(persos, new CompNivelMasMenos());
		_LADDER_NIVEL.clear();
		_LADDER_NIVEL.addAll(persos);
	}
	
	private static void rankingDia() {
		if (!MainServidor.PARAM_LADDER_EXP_DIA)
			return;
		ArrayList<Personaje> persos = new ArrayList<>();
		persos.addAll(_PERSONAJES.values());
		Collections.sort(persos, new CompDiaMasMenos());
		_LADDER_EXP_DIA.clear();
		_LADDER_EXP_DIA.addAll(persos);
	}
	
	private static void rankingGremio() {
		if (!MainServidor.PARAM_LADDER_GREMIO)
			return;
		ArrayList<Gremio> persos = new ArrayList<>();
		persos.addAll(_GREMIOS.values());
		Collections.sort(persos, new CompGremioMasMenos());
		_LADDER_GREMIO.clear();
		_LADDER_GREMIO.addAll(persos);
	}
	
	private static void rankingPVP() {
		if (!MainServidor.PARAM_LADDER_PVP) {
			return;
		}
		ArrayList<RankingPVP> persos = new ArrayList<>();
		persos.addAll(_RANKINGS_PVP.values());
		Collections.sort(persos, new CompPVPMasMenos());
		_LADDER_PVP.clear();
		_LADDER_PVP.addAll(persos);
	}
	
	private static void rankingKoliseo() {
		if (!MainServidor.PARAM_LADDER_KOLISEO) {
			return;
		}
		ArrayList<RankingKoliseo> persos = new ArrayList<>();
		persos.addAll(_RANKINGS_KOLISEO.values());
		Collections.sort(persos, new CompKoliseoMasMenos());
		_LADDER_KOLISEO.clear();
		_LADDER_KOLISEO.addAll(persos);
	}
	
	private static void addPaginas(StringBuilder temp, int inicio, int add) {
		temp.append("|" + (inicio == -1 ? 0 : 1) + "|" + (add == MainServidor.LIMITE_LADDER + 1 ? 1 : 0));
	}
	
	private static void addStringParaLadder(StringBuilder temp, Personaje perso, int pos) {
		if (temp.length() > 0) {
			temp.append("#");
		}
		temp.append(getStringParaLadder(perso, pos));
	}
	
	private static String getStringParaLadder(Personaje perso, int pos) {
		return pos + ";" + perso.getGfxID(false) + ";" + perso.getNombre() + ";" + perso.getTitulo(false) + ";" + perso
		.getNivel() + ";" + perso.getExperiencia() + ";" + (perso.enLinea() ? (perso.getPelea() != null ? 2 : 1) : 0) + ";"
		+ perso.getAlineacion();
	}
	
	private static void strStaffOnline(final Personaje out, String buscar, int iniciarEn) {
		if (!MainServidor.PARAM_LADDER_STAFF) {
			return;
		}
		int pos = 0, add = 0;
		int inicio = 0;
		final StringBuilder temp = new StringBuilder();
		for (final Personaje perso : ONLINES) {
			try {
				if (add > MainServidor.LIMITE_LADDER) {
					break;
				}
				if (perso.esIndetectable()) {
					continue;
				}
				if (perso.getCuenta().getAdmin() <= 0) {
					continue;
				}
				pos++;
				if (!buscar.isEmpty()) {
					if (!perso.getNombre().toUpperCase().contains(buscar)) {
						continue;
					}
				}
				if (inicio == 0) {
					inicio = pos;
				}
				if (pos < iniciarEn) {
					continue;
				}
				if (pos == inicio) {
					inicio = -1;
				}
				if (add < MainServidor.LIMITE_LADDER) {
					addStringParaLadder(temp, perso, pos);
				}
				add++;
			} catch (final Exception e) {}
		}
		addPaginas(temp, inicio, add);
		GestorSalida.ENVIAR_bl_RANKING_DATA(out, "STAFF", temp.toString());
	}
	
	private static void strRankingNivel(final Personaje out, String buscar, int iniciarEn) {
		if (!MainServidor.PARAM_LADDER_NIVEL) {
			return;
		}
		int pos = 0, add = 0;
		int inicio = 0;
		final StringBuilder temp = new StringBuilder();
		for (final Personaje perso : _LADDER_NIVEL) {
			try {
				if (add > MainServidor.LIMITE_LADDER) {
					break;
				}
				if (perso.esIndetectable()) {
					continue;
				}
				if (!MainServidor.PARAM_PERMITIR_ADMIN_EN_LADDER) {
					if (perso.getCuenta().getAdmin() > 0) {
						continue;
					}
				}
				pos++;
				if (!buscar.isEmpty()) {
					if (!perso.getNombre().toUpperCase().contains(buscar)) {
						continue;
					}
				}
				if (inicio == 0) {
					inicio = pos;
				}
				if (pos < iniciarEn) {
					continue;
				}
				if (pos == inicio) {
					inicio = -1;
				}
				if (add < MainServidor.LIMITE_LADDER) {
					addStringParaLadder(temp, perso, pos);
				}
				add++;
			} catch (final Exception e) {}
		}
		addPaginas(temp, inicio, add);
		GestorSalida.ENVIAR_bl_RANKING_DATA(out, "NIVEL", temp.toString());
	}
	
	private static void strRankingDia(final Personaje out, String buscar, int iniciarEn) {
		if (!MainServidor.PARAM_LADDER_EXP_DIA) {
			return;
		}
		int pos = 0, add = 0;
		int inicio = 0;
		final StringBuilder temp = new StringBuilder();
		for (final Personaje perso : _LADDER_EXP_DIA) {
			try {
				if (add > MainServidor.LIMITE_LADDER) {
					break;
				}
				if (perso.esIndetectable()) {
					continue;
				}
				if (!MainServidor.PARAM_PERMITIR_ADMIN_EN_LADDER) {
					if (perso.getCuenta().getAdmin() > 0) {
						continue;
					}
				}
				pos++;
				if (!buscar.isEmpty()) {
					if (!perso.getNombre().toUpperCase().contains(buscar)) {
						continue;
					}
				}
				if (inicio == 0) {
					inicio = pos;
				}
				if (pos < iniciarEn) {
					continue;
				}
				if (pos == inicio) {
					inicio = -1;
				}
				if (add < MainServidor.LIMITE_LADDER) {
					addStringParaLadder(temp, perso, pos);
				}
				add++;
			} catch (final Exception e) {}
		}
		addPaginas(temp, inicio, add);
		GestorSalida.ENVIAR_bl_RANKING_DATA(out, "DIA", temp.toString());
	}
	
	private static void strRankingPVP(final Personaje out, String buscar, int iniciarEn) {
		if (!MainServidor.PARAM_LADDER_PVP) {
			return;
		}
		int pos = 0, add = 0;
		int inicio = 0;
		final StringBuilder temp = new StringBuilder();
		for (final RankingPVP rank : _LADDER_PVP) {
			try {
				if (add > MainServidor.LIMITE_LADDER) {
					break;
				}
				Personaje perso = getPersonaje(rank.getID());
				if (perso == null) {
					continue;
				}
				if (perso.esIndetectable()) {
					continue;
				}
				if (!MainServidor.PARAM_PERMITIR_ADMIN_EN_LADDER) {
					if (perso.getCuenta().getAdmin() > 0) {
						continue;
					}
				}
				pos++;
				if (!buscar.isEmpty()) {
					if (!perso.getNombre().toUpperCase().contains(buscar)) {
						continue;
					}
				}
				if (inicio == 0) {
					inicio = pos;
				}
				if (pos < iniciarEn) {
					continue;
				}
				if (pos == inicio) {
					inicio = -1;
				}
				if (add < MainServidor.LIMITE_LADDER) {
					addStringParaLadder(temp, perso, pos);
				}
				add++;
			} catch (final Exception e) {}
		}
		addPaginas(temp, inicio, add);
		GestorSalida.ENVIAR_bl_RANKING_DATA(out, "PVP", temp.toString());
	}
	
	private static void strRankingKoliseo(final Personaje out, String buscar, int iniciarEn) {
		if (!MainServidor.PARAM_LADDER_KOLISEO) {
			return;
		}
		int pos = 0, add = 0;
		int inicio = 0;
		final StringBuilder temp = new StringBuilder();
		for (final RankingKoliseo rank : _LADDER_KOLISEO) {
			try {
				if (add > MainServidor.LIMITE_LADDER) {
					break;
				}
				Personaje perso = getPersonaje(rank.getID());
				if (perso == null) {
					continue;
				}
				if (perso.esIndetectable()) {
					continue;
				}
				if (!MainServidor.PARAM_PERMITIR_ADMIN_EN_LADDER) {
					if (perso.getCuenta().getAdmin() > 0) {
						continue;
					}
				}
				pos++;
				if (!buscar.isEmpty()) {
					if (!perso.getNombre().toUpperCase().contains(buscar)) {
						continue;
					}
				}
				if (inicio == 0) {
					inicio = pos;
				}
				if (pos < iniciarEn) {
					continue;
				}
				if (pos == inicio) {
					inicio = -1;
				}
				if (add < MainServidor.LIMITE_LADDER) {
					addStringParaLadder(temp, perso, pos);
				}
				add++;
			} catch (final Exception e) {}
		}
		addPaginas(temp, inicio, add);
		GestorSalida.ENVIAR_bl_RANKING_DATA(out, "KOLISEO", temp.toString());
	}
	
	private static void strRankingGremio(final Personaje out, String buscar, int iniciarEn) {
		if (!MainServidor.PARAM_LADDER_GREMIO) {
			return;
		}
		int pos = 0, add = 0;
		int inicio = 0;
		final StringBuilder temp = new StringBuilder();
		for (final Gremio gremio : _LADDER_GREMIO) {
			try {
				if (add > MainServidor.LIMITE_LADDER) {
					break;
				}
				pos++;
				if (!buscar.isEmpty()) {
					if (!gremio.getNombre().toUpperCase().contains(buscar)) {
						continue;
					}
				}
				if (inicio == 0) {
					inicio = pos;
				}
				if (pos < iniciarEn) {
					continue;
				}
				if (pos == inicio) {
					inicio = -1;
				}
				if (add < MainServidor.LIMITE_LADDER) {
					if (temp.length() > 0) {
						temp.append("#");
					}
					temp.append(pos + ";" + gremio.getEmblema() + ";" + gremio.getNombre() + ";" + gremio.getCantidadMiembros()
					+ ";" + gremio.getNivel() + ";" + gremio.getExperiencia() + ";;;");
				}
				add++;
			} catch (final Exception e) {}
		}
		addPaginas(temp, inicio, add);
		GestorSalida.ENVIAR_bl_RANKING_DATA(out, "GREMIO", temp.toString());
	}
	
	public static String nombreLiderRankingPVP() {
		String nombre = "";
		if (_RANKINGS_PVP.size() <= 0) {
			return nombre;
		}
		int vict = 0, derr = 0;
		for (final RankingPVP rank : _RANKINGS_PVP.values()) {
			if (rank.getVictorias() > vict) {
				nombre = rank.getNombre();
				vict = rank.getVictorias();
				derr = rank.getDerrotas();
			} else {
				if (rank.getVictorias() != vict || rank.getDerrotas() > derr) {
					continue;
				}
				nombre = rank.getNombre();
				vict = rank.getVictorias();
				derr = rank.getDerrotas();
			}
		}
		return nombre;
	}
	
	// public static void actualizarLiderPVP() {
	// final String antiguoLider = _liderRanking;
	// final Personaje liderViejo = getPersonajePorNombre(antiguoLider);
	// if (liderViejo != null) {
	// liderViejo.setTitulo((byte) 0);
	// }
	// GestorSQL.ACTUALIZAR_TITULO_POR_NOMBRE(antiguoLider);
	// final Personaje perso = getPersonajePorNombre(nombreLiderRankingPVP());
	// if (perso != null) {
	// perso.setTitulo((byte) 8);
	// getNPCModelo(1350).modificarNPC( perso.getSexo(), perso.getGfxID(false), perso.getColor1(),
	// perso.getColor2(),
	// perso.getColor3());
	// }
	// _liderRanking = nombreLiderRankingPVP();
	// }
	public static String rankingsPermitidos() {
		final StringBuilder temp = new StringBuilder();
		if (MainServidor.PARAM_LADDER_NIVEL) {
			if (temp.length() > 0) {
				temp.append("|");
			}
			temp.append("Nivel");
		}
		if (MainServidor.PARAM_LADDER_PVP) {
			if (temp.length() > 0) {
				temp.append("|");
			}
			temp.append("PVP");
		}
		if (MainServidor.PARAM_LADDER_GREMIO) {
			if (temp.length() > 0) {
				temp.append("|");
			}
			temp.append("Gremio");
		}
		if (MainServidor.PARAM_LADDER_KOLISEO) {
			if (temp.length() > 0) {
				temp.append("|");
			}
			temp.append("Koliseo");
		}
		if (MainServidor.PARAM_LADDER_EXP_DIA) {
			if (temp.length() > 0) {
				temp.append("|");
			}
			temp.append("DiaXP");
		}
		if (MainServidor.PARAM_LADDER_STAFF) {
			if (temp.length() > 0) {
				temp.append("|");
			}
			temp.append("Staff");
		}
		return temp.toString();
	}
	
	public static void enviarRanking(Personaje perso, String param, String buscar, int iniciarEn) {
		switch (param) {
			case "NIVEL" :
				strRankingNivel(perso, buscar, iniciarEn);
				break;
			case "PVP" :
				strRankingPVP(perso, buscar, iniciarEn);
				break;
			case "DIA" :
				strRankingDia(perso, buscar, iniciarEn);
				break;
			case "STAFF" :
				strStaffOnline(perso, buscar, iniciarEn);
				break;
			case "KOLISEO" :
				strRankingKoliseo(perso, buscar, iniciarEn);
				break;
			case "GREMIO" :
				strRankingGremio(perso, buscar, iniciarEn);
				break;
			default :
				GestorSalida.ENVIAR_BN_NADA(perso);
				return;
		}
	}
	
	public static void actualizarRankings() {
		rankingNivel();
		rankingPVP();
		rankingGremio();
		rankingKoliseo();
		rankingDia();
	}
	
	public static String misBoletos(final int persoID) {
		if (!VENDER_BOLETOS) {
			return "";
		}
		final StringBuilder str = new StringBuilder();
		for (int a = 1; a <= LOTERIA_BOLETOS.length; a++) {
			if (LOTERIA_BOLETOS[a - 1] != persoID) {
				continue;
			}
			if (str.length() > 0) {
				str.append(", ");
			}
			str.append(a);
		}
		return str.toString();
	}
	
	public static void devolverBoletos() {
		for (int a = 0; a < LOTERIA_BOLETOS.length; a++) {
			try {
				if (LOTERIA_BOLETOS[a] == 0) {
					continue;
				}
				Personaje perso = getPersonaje(LOTERIA_BOLETOS[a]);
				if (MainServidor.PARAM_LOTERIA_OGRINAS) {
					int idCuenta = perso.getCuentaID();
					GestorSQL.SET_OGRINAS_CUENTA(MainServidor.PRECIO_LOTERIA + GestorSQL.GET_OGRINAS_CUENTA(idCuenta), idCuenta);
				} else {
					perso.addKamas(MainServidor.PRECIO_LOTERIA, false, true);
				}
			} catch (Exception e) {}
		}
	}
	
	public static synchronized void comprarLoteria(final String packet, final Personaje perso) {
		if (!VENDER_BOLETOS) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "1DONT_TIME_BUY_LOTERIE");
			return;
		}
		int boleto = 1;
		try {
			boleto = Integer.parseInt(packet.substring(3));
		} catch (Exception e) {}
		if (boleto < 1) {
			boleto = 1;
		} else if (boleto > LOTERIA_BOLETOS.length) {
			boleto = LOTERIA_BOLETOS.length;
		}
		if (boleto > 9999) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "1NUMBER_LOTERIE_INCORRECT");
			return;
		}
		if (LOTERIA_BOLETOS[boleto - 1] != 0) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "1NUMBER_LOTERIE_OCCUPED");
			return;
		}
		if (MainServidor.PARAM_LOTERIA_OGRINAS) {
			if (GestorSQL.RESTAR_OGRINAS(perso.getCuenta(), MainServidor.PRECIO_LOTERIA, perso)) {
				LOTERIA_BOLETOS[boleto - 1] = perso.getID();
			}
		} else {
			if (perso.getKamas() >= MainServidor.PRECIO_LOTERIA) {
				perso.addKamas(-MainServidor.PRECIO_LOTERIA, true, true);
				LOTERIA_BOLETOS[boleto - 1] = perso.getID();
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "1TU_BOLETO;" + boleto);
			} else {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "182");
			}
		}
	}
	
	public static void iniciarLoteria() {
		if (!MainServidor.PARAM_LOTERIA) {
			return;
		}
		if (MSJ_CUENTA_REGRESIVA.equalsIgnoreCase("RESET RATES") || MSJ_CUENTA_REGRESIVA.equalsIgnoreCase("LOTERIA")) {
			return;
		}
		MSJ_CUENTA_REGRESIVA = "LOTERIA";
		SEG_CUENTA_REGRESIVA = 1800;
		VENDER_BOLETOS = true;
		GestorSalida.ENVIAR_bRI_INICIAR_CUENTA_REGRESIVA_TODOS();
	}
	
	public static void sortearBoletos() {
		if (!VENDER_BOLETOS) {
			return;
		}
		VENDER_BOLETOS = false;
		final ArrayList<Integer> lista = new ArrayList<Integer>();
		for (int x = 1; x <= LOTERIA_BOLETOS.length; x++) {
			if (LOTERIA_BOLETOS[x - 1] != 0) {
				lista.add(x);
			}
		}
		if (lista.size() < 10) {
			SEG_CUENTA_REGRESIVA = 600;
			MSJ_CUENTA_REGRESIVA = "LOTERIA";
			VENDER_BOLETOS = true;
			GestorSalida.ENVIAR_ÑL_BOTON_LOTERIA_TODOS(true);
			GestorSalida.ENVIAR_bRI_INICIAR_CUENTA_REGRESIVA_TODOS();
			GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1PLUS_TIME_SORTEO");
			return;
		}
		GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1SORTEO_LOTERIE");
		try {
			Thread.sleep(10000);
		} catch (final Exception e) {}
		int premios = 1;
		premios += lista.size() / MainServidor.GANADORES_POR_BOLETOS;
		final Map<Integer, Integer> ganadores = new TreeMap<Integer, Integer>();
		for (int a = 0; a < premios; a++) {
			final int boleto = lista.get(Formulas.getRandomInt(0, lista.size() - 1));
			ganadores.put(boleto, LOTERIA_BOLETOS[boleto - 1]);
			lista.remove(lista.indexOf(boleto));
		}
		int b = 1;
		for (final Entry<Integer, Integer> entry : ganadores.entrySet()) {
			final Personaje perso = getPersonaje(entry.getValue());
			if (perso == null) {
				continue;
			}
			int idCuenta = perso.getCuentaID();
			GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1NUMBER_WIN_LOTERIE;" + b + "~(" + entry.getKey() + ") - " + perso
			.getNombre());
			if (MainServidor.PARAM_LOTERIA_OGRINAS) {
				GestorSQL.SET_OGRINAS_CUENTA(GestorSQL.GET_OGRINAS_CUENTA(idCuenta) + MainServidor.PREMIO_LOTERIA, idCuenta);
			} else {
				perso.addKamas(MainServidor.PREMIO_LOTERIA, true, true);
			}
			try {
				Thread.sleep(2000);
			} catch (final InterruptedException e) {}
			b++;
		}
		GestorSalida.ENVIAR_Im_INFORMACION_A_TODOS("1FINISH_LOTERIE");
		LOTERIA_BOLETOS = new int[10000];
	}
	
	public static void resetExpDia() {
		for (final Personaje perso : _PERSONAJES.values()) {
			perso.resetExpDia();
		}
	}
	
	public static void moverMonturas() {
		for (final Cercado cercado : CERCADOS.values()) {
			cercado.startMoverMontura();
		}
	}
	
	public static void moverRecaudadores() {
		for (final Recaudador recauador : _RECAUDADORES.values()) {
			recauador.puedeMoverRecaudador();
		}
	}
	
	// public static void embarazoMonturas() {
	// for (final Montura montura : Monturas.values()) {
	// montura.aumentarTiempoFecundacion();
	// if (montura.getUbicacion() != Ubicacion.ESTABLO) {
	// continue;
	// }
	// montura.disminuirFatiga();
	// }
	// }
	public static void disminuirFatigaMonturas() {
		for (final Montura montura : _MONTURAS.values()) {
			if (montura.getUbicacion() != Ubicacion.ESTABLO) {
				continue;
			}
			montura.disminuirFatiga();
		}
	}
	
	public static void checkearObjInteractivos() {
		for (final ObjetoInteractivo oi : OBJETOS_INTERACTIVOS) {
			oi.recargando(false);
			oi.subirEstrella();
		}
	}
	
	public static void expulsarInactivos() {
		for (final ServidorSocket ss : ServidorServer.getClientes()) {
			try {
				if (ss.getTiempoUltPacket() + (MainServidor.SEGUNDOS_INACTIVIDAD * 1000) < System.currentTimeMillis()) {
					ss.registrar("<===> EXPULSAR POR INACTIVIDAD!!!");
					GestorSalida.ENVIAR_M0_MENSAJE_BASICOS_SVR_MUESTRA_DISCONNECT(ss, "1", "", "");
					ss.cerrarSocket(true, "expulsarInactivos()");
				}
			} catch (final Exception e) {}
		}
	}
	
	public static void lanzarPublicidad(final String str) {
		GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE_TODOS(str);
	}
	
	public static void finalizarPeleas() {
		for (Mapa mapa : MAPAS.values()) {
			try {
				for (Pelea pelea : mapa.getPeleas().values()) {
					pelea.cancelarPelea();
				}
			} catch (Exception e) {
				MainServidor.redactarLogServidorln("EXCEPTION finalizarPeleas " + e.toString());
				e.printStackTrace();
			}
		}
	}
	public static boolean SALVANDO;
	
	public static void salvarServidor(boolean inclusoOffline) {
		SALVANDO = true;
		MainServidor.redactarLogServidorln("Se invoco el metodo salvar Servidor (MUNDO DOFUS) ");
		if (SERVIDOR_ESTADO != Constantes.SERVIDOR_OFFLINE) {
			setServidorEstado(Constantes.SERVIDOR_SALVANDO);
		}
		if (!MainServidor.PARAM_AUTO_COMMIT) {
			GestorSQL.timerCommit(false);
			GestorSQL.iniciarCommit(true);
		}
		MainServidor.redactarLogServidor("Iniciando salvado de registros JUGADORES Y SQL ... ");
		MainServidor.imprimirLogPlayers();
		MainServidor.redactarLogServidorln("100%");
		TOTAL_SALVADO = 0;
		try {
			MainServidor.redactarLogServidor("Salvando Kamas de la Ruleta de Jalato");
			MainServidor.modificarParam("KAMAS_RULETA_JALATO", MainServidor.KAMAS_RULETA_JALATO + "");
			// PERSONAJES
			CANT_SALVANDO = 0;
			MainServidor.redactarLogServidorln("Salvando los personajes: ");
			for (final Personaje perso : _PERSONAJES.values()) {
				try {
					if (perso == null || perso.getCuenta() == null) {
						continue;
					}
					if (perso.enLinea() || inclusoOffline) {
						MainServidor.redactarLogServidor(" -> Salvando a " + perso.getNombre() + " ... ");// Ecatome
						if (SERVIDOR_ESTADO == Constantes.SERVIDOR_OFFLINE) {
							perso.previosDesconectar();
						}
						GestorSQL.SALVAR_PERSONAJE(perso, true);
						MainServidor.redactarLogServidorln(" [ONLINE] " + " 100%");
						CANT_SALVANDO++;
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			MainServidor.redactarLogServidorln("Finalizó con " + CANT_SALVANDO);
			TOTAL_SALVADO += CANT_SALVANDO;
			CANT_SALVANDO = 0;
			MainServidor.redactarLogServidorln("Salvando los mercantes: ");
			for (final Personaje perso : _PERSONAJES.values()) {
				try {
					if (perso == null || perso.getCuenta() == null || inclusoOffline) {
						continue;
					}
					if (perso.esMercante()) {
						MainServidor.redactarLogServidor(" -> Salvando a " + perso.getNombre() + " ... ");// Ecatome
						GestorSQL.SALVAR_PERSONAJE(perso, true);
						MainServidor.redactarLogServidorln(" [MERCANTE] " + " 100%");
						CANT_SALVANDO++;
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			MainServidor.redactarLogServidorln("Finalizó con " + CANT_SALVANDO);
			TOTAL_SALVADO += CANT_SALVANDO;
			CANT_SALVANDO = 0;
			MainServidor.redactarLogServidor("Salvando los prismas: ");
			for (final Prisma prisma : _PRISMAS.values()) {
				try {
					GestorSQL.REPLACE_PRISMA(prisma);
					CANT_SALVANDO++;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			MainServidor.redactarLogServidorln("Finalizó con " + CANT_SALVANDO);
			TOTAL_SALVADO += CANT_SALVANDO;
			CANT_SALVANDO = 0;
			MainServidor.redactarLogServidor("Salvando los gremios: ");
			for (final Gremio gremio : _GREMIOS.values()) {
				try {
					GestorSQL.REPLACE_GREMIO(gremio);
					CANT_SALVANDO++;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			MainServidor.redactarLogServidorln("Finalizó con " + CANT_SALVANDO);
			TOTAL_SALVADO += CANT_SALVANDO;
			CANT_SALVANDO = 0;
			MainServidor.redactarLogServidor("Salvando los recaudadores: ");
			for (final Recaudador recau : _RECAUDADORES.values()) {
				try {
					if (recau.getGremio() == null) {
						continue;
					}
					GestorSQL.REPLACE_RECAUDADOR(recau, true);
					CANT_SALVANDO++;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			MainServidor.redactarLogServidorln("Finalizó con " + CANT_SALVANDO);
			TOTAL_SALVADO += CANT_SALVANDO;
			CANT_SALVANDO = 0;
			MainServidor.redactarLogServidor("Salvando los cercados: ");
			for (final Cercado cercado : CERCADOS.values()) {
				try {
					GestorSQL.REPLACE_CERCADO(cercado);
					CANT_SALVANDO++;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			MainServidor.redactarLogServidorln("Finalizó con " + CANT_SALVANDO);
			TOTAL_SALVADO += CANT_SALVANDO;
			CANT_SALVANDO = 0;
			MainServidor.redactarLogServidor("Salvando las monturas: ");
			for (final Montura montura : _MONTURAS.values()) {
				try {
					if (montura.estaCriando()) {
						GestorSQL.REPLACE_MONTURA(montura, false);
						CANT_SALVANDO++;
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			MainServidor.redactarLogServidorln("Finalizó con " + CANT_SALVANDO);
			TOTAL_SALVADO += CANT_SALVANDO;
			CANT_SALVANDO = 0;
			MainServidor.redactarLogServidor("Salvando las casas: ");
			for (final Casa casa : CASAS.values()) {
				try {
					GestorSQL.REPLACE_CASA(casa);
					CANT_SALVANDO++;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			MainServidor.redactarLogServidorln("Finalizó con " + CANT_SALVANDO);
			TOTAL_SALVADO += CANT_SALVANDO;
			CANT_SALVANDO = 0;
			MainServidor.redactarLogServidor("Salvando los cofres: ");
			for (final Cofre cofre : COFRES.values()) {
				try {
					GestorSQL.REPLACE_COFRE(cofre, true);
					CANT_SALVANDO++;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			MainServidor.redactarLogServidorln("Finalizó con " + CANT_SALVANDO);
			TOTAL_SALVADO += CANT_SALVANDO;
			CANT_SALVANDO = 0;
			MainServidor.redactarLogServidor("Salvando los rankings PVP: ");
			for (final RankingPVP rank : _RANKINGS_PVP.values()) {
				try {
					GestorSQL.REPLACE_RANKING_PVP(rank);
					CANT_SALVANDO++;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			MainServidor.redactarLogServidorln("Finalizó con " + CANT_SALVANDO);
			TOTAL_SALVADO += CANT_SALVANDO;
			CANT_SALVANDO = 0;
			MainServidor.redactarLogServidor("Salvando los rankings Koliseo: ");
			for (final RankingKoliseo rank : _RANKINGS_KOLISEO.values()) {
				try {
					GestorSQL.REPLACE_RANKING_KOLISEO(rank);
					CANT_SALVANDO++;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			MainServidor.redactarLogServidorln("Finalizó con " + CANT_SALVANDO);
			TOTAL_SALVADO += CANT_SALVANDO;
			CANT_SALVANDO = 0;
			MainServidor.redactarLogServidor("Salvando las cuentas: ");
			for (final Cuenta cuenta : _CUENTAS.values()) {
				try {
					if (cuenta.enLinea() || inclusoOffline) {
						GestorSQL.REPLACE_CUENTA_SERVIDOR(cuenta, GestorSQL.GET_PRIMERA_VEZ(cuenta.getNombre()));
						CANT_SALVANDO++;
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			MainServidor.redactarLogServidorln("Finalizó con " + CANT_SALVANDO);
			TOTAL_SALVADO += CANT_SALVANDO;
			MainServidor.redactarLogServidorln("------------ Se salvó exitosamente el servidor 100% ------------");
		} catch (final ConcurrentModificationException e) {
			MainServidor.redactarLogServidorln("------------ Ocurrio un error de concurrent " + e.toString());
			e.printStackTrace();
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("------------ Error al salvar : " + e.toString());
			e.printStackTrace();
		} finally {
			if (!MainServidor.PARAM_AUTO_COMMIT) {
				GestorSQL.iniciarCommit(true);
				GestorSQL.timerCommit(true);
			}
			if (SERVIDOR_ESTADO != Constantes.SERVIDOR_OFFLINE) {
				setServidorEstado(Constantes.SERVIDOR_ONLINE);
			}
			if (!MainServidor.URL_BACKUP_PHP.isEmpty()) {
				try {
					if (!MainServidor.PARAM_AUTO_COMMIT) {
						Thread.sleep(20000);
					}
					MainServidor.redactarLogServidorln("REALIZANDO BACKUP SQL DEL SERVIDOR");
					Encriptador.consultaWeb(MainServidor.URL_BACKUP_PHP);
					MainServidor.redactarLogServidorln("BACKUP SQL REALIZADO CON EXITO");
				} catch (Exception e) {
					MainServidor.redactarLogServidorln("ERROR AL REALIZAR BACKUP SQL");
					e.printStackTrace();
				}
			}
			MainServidor.imprimirLogCombates();
			SALVANDO = false;
		}
	}
	
	// private static void salvarMapasHeroico() {
	// _cantSalvado = 0;
	// Bustemu.redactarLogServidor("Salvando los objetos heroicos: ");
	// GestorSQL.VACIAR_MAPAS_HEROICO();
	// for (final Mapa mapa : Mapas.values()) {
	// if (mapa.getGrupoMobsTotales().isEmpty() && mapa.getGrupoMobsHeroicos().isEmpty()) {
	// continue;
	// }
	// StringBuilder mobs = new StringBuilder();
	// StringBuilder objetos = new StringBuilder();
	// StringBuilder kamas = new StringBuilder();
	// ArrayList<GrupoMob> grupos = new ArrayList<>();
	// grupos.addAll(mapa.getGrupoMobsTotales().values());
	// grupos.addAll(mapa.getGrupoMobsHeroicos());
	// boolean paso = false;
	// for (GrupoMob g : grupos) {
	// if (g.getKamasHeroico() <= 0 && g.cantObjHeroico() == 0) {
	// continue;
	// }
	// if (paso) {
	// mobs.append("|");
	// objetos.append("|");
	// kamas.append("|");
	// }
	// mobs.append(g.getStrGrupoMob());
	// objetos.append(g.getIDsObjeto());
	// kamas.append(g.getKamasHeroico());
	// paso = true;
	// }
	// if (!paso) {
	// continue;
	// }
	// _cantSalvado++;
	// GestorSQL.REPLACE_MAPAS_HEROICO(mapa.getID(), mobs.toString(), objetos.toString(),
	// kamas.toString());
	// }
	// Bustemu.redactarLogServidorln("Finalizo con " + _cantSalvado);
	// _totalSalvado += _cantSalvado;
	// }
	public static void salvarMapasEstrellas() {
		if (MainServidor.MODO_DEBUG) {
			return;
		}
		CANT_SALVANDO = 0;
		MainServidor.redactarLogServidor("Salvando las estrellas de los mobs: ");
		GestorSQL.VACIAR_MAPAS_ESTRELLAS();
		PreparedStatement declaracion = (PreparedStatement) GestorSQL.GET_STATEMENT_SQL_DINAMICA(
		"REPLACE INTO `mapas_estrellas` VALUES (?,?);");
		for (final Mapa mapa : MAPAS.values()) {
			if (mapa.getGrupoMobsTotales().isEmpty()) {
				continue;
			}
			StringBuilder s = new StringBuilder();
			for (GrupoMob gm : mapa.getGrupoMobsTotales().values()) {
				if (gm.realBonusEstrellas() <= 0) {
					continue;
				}
				if (s.length() > 0) {
					s.append(",");
				}
				s.append(gm.realBonusEstrellas());
			}
			if (s.length() == 0) {
				continue;
			}
			CANT_SALVANDO++;
			GestorSQL.REPLACE_MAPAS_ESTRELLAS_BATCH(declaracion, mapa.getID(), s.toString());
		}
		if (CANT_SALVANDO > 0) {
			GestorSQL.ejecutarBatch(declaracion);
		}
		MainServidor.redactarLogServidorln("Finalizo con " + CANT_SALVANDO);
		TOTAL_SALVADO += CANT_SALVANDO;
	}
	
	public static ArrayList<Cofre> getCofresPorCasa(final Casa casa) {
		final ArrayList<Cofre> cofres = new ArrayList<Cofre>();
		for (final Cofre cofre : COFRES.values()) {
			if (cofre.getCasaID() == casa.getID()) {
				cofres.add(cofre);
			}
		}
		return cofres;
	}
	
	public static Cofre getCofrePorUbicacion(final short mapaID, final short celdaID) {
		for (final Cofre cofre : COFRES.values()) {
			if (cofre.getMapaID() == mapaID && cofre.getCeldaID() == celdaID) {
				return cofre;
			}
		}
		return null;
	}
	
	public static boolean borrarLasCuentas(final long minutos) {
		if (!MainServidor.PARAM_BORRAR_CUENTAS_VIEJAS) {
			return false;
		}
		if (MINUTOS_VIDA_REAL - minutos > MainServidor.DIAS_PARA_BORRAR * 24 * 60) {
			// se convierte a minutos para comparar 2 meses
			return true;
		}
		return false;
	}
	
	public static void usoMemoria() {
		System.out.println("======== FreeMemory: " + Runtime.getRuntime().freeMemory() / 1048576f + " MB ========");
	}
	
	public static byte getCantCercadosGremio(int id) {
		byte i = 0;
		for (Cercado cercado : CERCADOS.values()) {
			if (cercado.getGremio() != null && cercado.getGremio().getID() == id) {
				i++;
			}
		}
		return i;
	}
	
	public static void addMapaEstrellas(short id, String estrellas) {
		try {
			ArrayList<Short> array = new ArrayList<>();
			for (String s : estrellas.split(",")) {
				array.add(Short.parseShort(s));
			}
			MAPAS_ESTRELLAS.put(id, array);
		} catch (Exception e) {}
	}
	
	public static void addMapaHeroico(short id, String mobs, String objetos, String kamas) {
		try {
			ArrayList<String> array = new ArrayList<>();
			String[] m = mobs.split(Pattern.quote("|"));
			String[] o = objetos.split(Pattern.quote("|"));
			String[] k = kamas.split(Pattern.quote("|"));
			for (int i = 0; i < m.length; i++) {
				array.add(m[i] + "|" + o[i] + "|" + k[i]);
			}
			MAPAS_HEROICOS.put(id, array);
		} catch (Exception e) {}
	}
	
	public static ArrayList<String> getMapaHeroico(short id) {
		return MAPAS_HEROICOS.get(id);
	}
	
	public static ArrayList<Short> getMapaEstrellas(short id) {
		return MAPAS_ESTRELLAS.get(id);
	}
	
	public static Area getArea(final int area) {
		return AREAS.get(area);
	}
	
	public static SubArea getSubArea(final int subArea) {
		return SUB_AREAS.get(subArea);
	}
	
	public static SuperArea getSuperArea(final int superArea) {
		return SUPER_AREAS.get(superArea);
	}
	
	public static void addArea(final Area area) {
		AREAS.put(area.getID(), area);
	}
	
	public static void addSubArea(final SubArea subArea) {
		SUB_AREAS.put(subArea.getID(), subArea);
	}
	
	public static void addSuperArea(final SuperArea superArea) {
		SUPER_AREAS.put(superArea.getID(), superArea);
	}
	
	public static void addExpNivel(final int nivel, final Experiencia exp) {
		// if (nivel > Bustemu.NIVEL_MAX_PERSONAJE) {
		// return;
		// }
		EXPERIENCIA.put(nivel, exp);
	}
	
	public static CreaTuItem getCreaTuItem(int id) {
		return CREA_TU_ITEM.get(id);
	}
	
	public static ConcurrentHashMap<Integer, Cuenta> getCuentas() {
		return _CUENTAS;
	}
	
	public static Cuenta getCuenta(final int id) {
		return _CUENTAS.get(id);
	}
	
	public static void addRespuestaNPC(final RespuestaNPC respuesta) {
		NPC_RESPUESTAS.put(respuesta.getID(), respuesta);
	}
	
	public static RespuestaNPC getRespuestaNPC(final int id) {
		return NPC_RESPUESTAS.get(id);
	}
	
	public static void addPreguntaNPC(final PreguntaNPC pregunta) {
		NPC_PREGUNTAS.put(pregunta.getID(), pregunta);
	}
	
	public static PreguntaNPC getPreguntaNPC(final int id) {
		return NPC_PREGUNTAS.get(id);
	}
	
	public static NPCModelo getNPCModelo(final int id) {
		return NPC_MODELOS.get(id);
	}
	
	public static void addNPCModelo(final NPCModelo npcModelo) {
		NPC_MODELOS.put(npcModelo.getID(), npcModelo);
	}
	
	public static Mapa getMapa(final short id) {
		return MAPAS.get(id);
	}
	
	public static void addMapa(final Mapa mapa) {
		if (!MAPAS.containsKey(mapa.getID())) {
			MAPAS.put(mapa.getID(), mapa);
		}
	}
	
	public static boolean mapaExiste(final short mapa) {
		return MAPAS.containsKey(mapa);
	}
	
	public static Mapa mapaPorCoordXYContinente(final int mapaX, final int mapaY, final int idContinente) {
		for (final Mapa mapa : MAPAS.values()) {
			if (mapa.getX() == mapaX && mapa.getY() == mapaY && mapa.getSubArea().getArea().getSuperArea()
			.getID() == idContinente) {
				return mapa;
			}
		}
		return null;
	}
	
	public static String mapaPorCoordenadas(final int mapaX, final int mapaY, final int idContinente) {
		final StringBuilder str = new StringBuilder();
		for (final Mapa mapa : MAPAS.values()) {
			if (mapa.getX() == mapaX && mapa.getY() == mapaY && mapa.getSubArea().getArea().getSuperArea()
			.getID() == idContinente) {
				str.append(mapa.getID() + ", ");
			}
		}
		return str.toString();
	}
	
	public static void subirEstrellasMobs(final int cant) {
		for (final Mapa mapa : MAPAS.values()) {
			mapa.subirEstrellasMobs(cant);
		}
	}
	
	public static void subirEstrellasOI(final int cant) {
		for (final Mapa mapa : MAPAS.values()) {
			mapa.subirEstrellasOI(cant);
		}
	}
	
	public static Cuenta getCuentaPorApodo(final String apodo) {
		for (final Cuenta cuenta : _CUENTAS.values()) {
			if (cuenta.getApodo().equals(apodo)) {
				return cuenta;
			}
		}
		return null;
	}
	
	public static String strCuentasOnline() {
		final StringBuilder str = new StringBuilder();
		for (final Personaje perso : ONLINES) {
			if (str.length() > 0) {
				str.append(",");
			}
			str.append(perso.getCuentaID());
		}
		return str.toString();
	}
	
	public static Cuenta getCuentaPorNombre(final String nombre) {
		return _CUENTAS_POR_NOMBRE.get(nombre.toLowerCase()) != null
		? _CUENTAS.get(_CUENTAS_POR_NOMBRE.get(nombre.toLowerCase()))
		: null;
	}
	
	public static void addCuenta(final Cuenta cuenta) {
		_CUENTAS.put(cuenta.getID(), cuenta);
		_CUENTAS_POR_NOMBRE.put(cuenta.getNombre().toLowerCase(), cuenta.getID());
	}
	
	public static void addPersonaje(final Personaje perso) {
		if (perso.getID() > SIG_ID_PERSONAJE) {
			SIG_ID_PERSONAJE = perso.getID();
		}
		_PERSONAJES.put(perso.getID(), perso);
	}
	
	public static Personaje getPersonaje(final int id) {
		return _PERSONAJES.get(id);
	}
	
	public static int getCantidadPersonajes() {
		return _PERSONAJES.size();
	}
	
	public static Personaje getPersonajePorNombre(final String nombre) {
		final ArrayList<Personaje> Ps = new ArrayList<Personaje>();
		Ps.addAll(_PERSONAJES.values());
		for (final Personaje perso : Ps) {
			if (perso.getNombre().equalsIgnoreCase(nombre)) {
				return perso;
			}
		}
		return null;
	}
	
	public static Casa getCasaPorUbicacion(final short mapaID, final int celdaID) {
		for (final Casa casa : CASAS.values()) {
			if (casa.getMapaIDFuera() == mapaID && casa.getCeldaIDFuera() == celdaID) {
				return casa;
			}
		}
		return null;
	}
	
	public static void cargarPropiedadesCasa(final Personaje perso) {
		for (final Casa casa : CASAS.values()) {
			try {
				if (casa.getMapaIDFuera() == perso.getMapa().getID()) {
					GestorSalida.ENVIAR_hP_PROPIEDADES_CASA(perso, casa.propiedadesPuertaCasa(perso));
					GestorSalida.ENVIAR_hL_INFO_CASA(perso, casa.informacionCasa(perso.getID()));
					Thread.sleep(5);
				}
			} catch (InterruptedException e) {}
		}
	}
	
	public static byte cantCasasGremio(final int gremioID) {
		byte i = 0;
		for (final Casa casa : CASAS.values()) {
			if (casa.getGremioID() == gremioID) {
				i++;
			}
		}
		return i;
	}
	
	public static Casa getCasaDePj(final int persoID) {
		for (final Casa casa : CASAS.values()) {
			if (casa.esSuCasa(persoID)) {
				return casa;
			}
		}
		return null;
	}
	
	public static void borrarCasaGremio(final int gremioID) {
		for (final Casa casa : CASAS.values()) {
			if (casa.getGremioID() == gremioID) {
				casa.nullearGremio();
				casa.actualizarDerechos(0);
			}
		}
	}
	
	public static Casa getCasaDentroPorMapa(final short mapaID) {
		for (final Casa casa : CASAS.values()) {
			if (casa.getMapasContenidos().contains(mapaID)) {
				return casa;
			}
		}
		return null;
	}
	
	public static String getAlineacionTodasSubareas() {
		final StringBuilder str = new StringBuilder();
		for (final SubArea subarea : SUB_AREAS.values()) {
			// if (!subarea.getConquistable()) {
			// continue;
			// }
			if (str.length() > 0) {
				str.append("|");
			}
			str.append(subarea.getID() + ";" + subarea.getAlineacion());
		}
		return str.toString();
	}
	
	// public static long getExpMinPersonaje(int nivel) {
	// if (nivel > Bustemu.NIVEL_MAX_PERSONAJE) {
	// nivel = Bustemu.NIVEL_MAX_PERSONAJE;
	// } else if (nivel < 1) {
	// nivel = 1;
	// }
	// return Experiencia.get(nivel)._personaje;
	// }
	//
	// public static long getExpMaxPersonaje(int nivel) {
	// if (nivel >= Bustemu.NIVEL_MAX_PERSONAJE) {
	// nivel = Bustemu.NIVEL_MAX_PERSONAJE - 1;
	// } else if (nivel <= 1) {
	// nivel = 1;
	// }
	// return Experiencia.get(nivel + 1)._personaje;
	// }
	//
	// public static long getExpMaxEncarnacion(int nivel) {
	// if (nivel >= Bustemu.NIVEL_MAX_ENCARNACION) {
	// nivel = Bustemu.NIVEL_MAX_ENCARNACION - 1;
	// } else if (nivel <= 1) {
	// nivel = 1;
	// }
	// return Experiencia.get(nivel + 1)._encarnacion;
	// }
	//
	// public static long getExpMaxGremio(int nivel) {
	// if (nivel >= Bustemu.NIVEL_MAX_GREMIO) {
	// nivel = Bustemu.NIVEL_MAX_GREMIO - 1;
	// } else if (nivel <= 1) {
	// nivel = 1;
	// }
	// return Experiencia.get(nivel + 1)._gremio;
	// }
	public static long getExpCazaCabezas(int nivel) {
		if (nivel >= MainServidor.NIVEL_MAX_PERSONAJE) {
			nivel = MainServidor.NIVEL_MAX_PERSONAJE - 1;
		} else if (nivel < 1) {
			nivel = 1;
		}
		if (MainServidor.PARAM_EXP_PVP_MISION_POR_TABLA) {
			int exp = 0;
			if (nivel < 60) {
				exp = 65000;
			} else if (nivel < 70) {
				exp = 90000;
			} else if (nivel < 80) {
				exp = 120000;
			} else if (nivel < 90) {
				exp = 160000;
			} else if (nivel < 100) {
				exp = 210000;
			} else if (nivel < 110) {
				exp = 270000;
			} else if (nivel < 120) {
				exp = 350000;
			} else if (nivel < 130) {
				exp = 440000;
			} else if (nivel < 140) {
				exp = 540000;
			} else if (nivel < 150) {
				exp = 650000;
			} else if (nivel < 155) {
				exp = 760000;
			} else if (nivel < 160) {
				exp = 880000;
			} else if (nivel < 165) {
				exp = 1000000;
			} else if (nivel < 170) {
				exp = 1130000;
			} else if (nivel < 175) {
				exp = 1300000;
			} else if (nivel < 180) {
				exp = 1000000;
			} else if (nivel < 185) {
				exp = 1700000;
			} else if (nivel < 190) {
				exp = 2000000;
			} else if (nivel < 195) {
				exp = 2000000;
			} else if (nivel <= 200) {
				exp = 3000000;
			}
			return exp;
		}
		return EXPERIENCIA.get(nivel)._personaje / 20;
	}
	
	public static long getExpPersonaje(int nivel) {
		if (nivel > MainServidor.NIVEL_MAX_PERSONAJE) {
			return Long.MAX_VALUE;
		} else if (nivel < 1) {
			nivel = 1;
		}
		return EXPERIENCIA.get(nivel)._personaje;
	}
	
	public static long getExpGremio(int nivel) {
		if (nivel > MainServidor.NIVEL_MAX_GREMIO) {
			nivel = MainServidor.NIVEL_MAX_GREMIO;
		} else if (nivel < 1) {
			nivel = 1;
		}
		return EXPERIENCIA.get(nivel)._gremio;
	}
	
	public static long getExpMontura(int nivel) {
		if (nivel > MainServidor.NIVEL_MAX_MONTURA) {
			nivel = MainServidor.NIVEL_MAX_MONTURA;
		} else if (nivel < 1) {
			nivel = 1;
		}
		return EXPERIENCIA.get(nivel)._montura;
	}
	
	public static long getExpEncarnacion(int nivel) {
		if (nivel > MainServidor.NIVEL_MAX_ENCARNACION) {
			nivel = MainServidor.NIVEL_MAX_ENCARNACION;
		} else if (nivel < 1) {
			nivel = 1;
		}
		return EXPERIENCIA.get(nivel)._encarnacion;
	}
	
	public static int getExpOficio(int nivel) {
		if (nivel > MainServidor.NIVEL_MAX_OFICIO) {
			nivel = MainServidor.NIVEL_MAX_OFICIO;
		} else if (nivel < 1) {
			nivel = 1;
		}
		return EXPERIENCIA.get(nivel)._oficio;
	}
	
	public static int getExpAlineacion(int nivel) {
		if (nivel > MainServidor.NIVEL_MAX_ALINEACION) {
			nivel = MainServidor.NIVEL_MAX_ALINEACION;
		} else if (nivel < 1) {
			nivel = 1;
		}
		return EXPERIENCIA.get(nivel)._alineacion;
	}
	
	public static int getExpParaNivelAlineacion(int nivel) {
		if (nivel > MainServidor.NIVEL_MAX_ALINEACION) {
			nivel = MainServidor.NIVEL_MAX_ALINEACION;
		} else if (nivel < 2) {
			nivel = 2;
		}
		return EXPERIENCIA.get(nivel)._alineacion - EXPERIENCIA.get(nivel - 1)._alineacion;
	}
	
	// public static Experiencia getExpNivel(final int nivel) {
	// return Experiencia.get(nivel);
	// }
	public static ObjetoInteractivoModelo getObjInteractivoModelo(final int id) {
		return OBJETOS_INTERACTIVOS_MODELOS.get(id);
	}
	
	public static ObjetoInteractivoModelo getObjIntModeloPorGfx(final int gfx) {
		for (final ObjetoInteractivoModelo oi : OBJETOS_INTERACTIVOS_MODELOS) {
			if (oi.getGfxs().contains(gfx)) {
				return oi;
			}
		}
		return null;
	}
	
	public static void addObjInteractivo(final ObjetoInteractivo oi) {
		OBJETOS_INTERACTIVOS.add(oi);
	}
	
	public static void addObjInteractivoModelo(final ObjetoInteractivoModelo OIM) {
		OBJETOS_INTERACTIVOS_MODELOS.add(OIM);
	}
	
	public static Oficio getOficio(final int id) {
		return OFICIOS.get(id);
	}
	
	public static void addOficio(final Oficio oficio) {
		OFICIOS.put(oficio.getID(), oficio);
	}
	
	public static void addReceta(final int id, final ArrayList<Duo<Integer, Integer>> arrayDuos) {
		RECETAS.put(id, arrayDuos);
	}
	
	public static ArrayList<Duo<Integer, Integer>> getReceta(final int id) {
		return RECETAS.get(id);
	}
	
	public static boolean esIngredienteDeReceta(int id) {
		for (ArrayList<Duo<Integer, Integer>> a : RECETAS.values()) {
			for (Duo<Integer, Integer> d : a) {
				if (d._primero == id)
					return true;
			}
		}
		return false;
	}
	
	public static int getIDRecetaPorIngredientes(final ArrayList<Integer> listaIDRecetas,
	final Map<Integer, Integer> ingredientes) {
		if (listaIDRecetas == null) {
			return -1;
		}
		for (final int id : listaIDRecetas) {
			final ArrayList<Duo<Integer, Integer>> receta = RECETAS.get(id);
			if (receta == null || receta.size() != ingredientes.size()) {
				continue;
			}
			boolean ok = true;
			for (final Duo<Integer, Integer> ing : receta) {
				if (ingredientes.get(ing._primero) == null) {
					ok = false;
					break;
				}
				final int primera = ingredientes.get(ing._primero);
				final int segunda = ing._segundo;
				if (primera != segunda) {
					ok = false;
					break;
				}
			}
			if (ok) {
				return id;
			}
		}
		return -1;
	}
	
	public static void addObjetoSet(final ObjetoSet objetoSet) {
		OBJETOS_SETS.put(objetoSet.getID(), objetoSet);
	}
	
	public static ObjetoSet getObjetoSet(final int id) {
		return OBJETOS_SETS.get(id);
	}
	
	public static int getNumeroObjetoSet() {
		return OBJETOS_SETS.size();
	}
	
	public static int sigIDPersonaje() {
		return ++SIG_ID_PERSONAJE;
	}
	
	// public static int sigIDCofre() {
	// return ++sigIDCofre;
	// }
	public static synchronized int sigIDObjeto() {
		return ++SIG_ID_OBJETO;
	}
	
	public static synchronized int sigIDLineaMercadillo() {
		return ++SIG_ID_LINEA_MERCADILLO;
	}
	
	public static int sigIDRecaudador() {
		SIG_ID_RECAUDADOR -= 3;
		return SIG_ID_RECAUDADOR;
	}
	
	public synchronized static int sigIDMontura() {
		SIG_ID_MONTURA -= 3;
		return SIG_ID_MONTURA;
	}
	
	public synchronized static int sigIDPrisma() {
		SIG_ID_PRISMA -= 3;
		return SIG_ID_PRISMA;
	}
	
	public synchronized static int sigIDGremio() {
		if (_GREMIOS.isEmpty()) {
			return 1;
		}
		int n = 0;
		for (Entry<Integer, Gremio> entry : _GREMIOS.entrySet()) {
			int x = entry.getKey();
			if (n < x) {
				n = x;
			}
		}
		return n + 1;
	}
	
	public static void addGremio(final Gremio gremio) {
		_GREMIOS.put(gremio.getID(), gremio);
	}
	
	public static synchronized boolean nombreGremioUsado(final String nombre) {
		try {
			for (final Gremio gremio : _GREMIOS.values()) {
				if (gremio.getNombre().equalsIgnoreCase(nombre)) {
					return true;
				}
			}
		} catch (final Exception e) {
			return true;
		}
		return false;
	}
	
	public static synchronized boolean emblemaGremioUsado(final String emblema) {
		for (final Gremio gremio : _GREMIOS.values()) {
			if (gremio.getEmblema().equals(emblema)) {
				return true;
			}
		}
		return false;
	}
	
	public static Gremio getGremio(final int i) {
		return _GREMIOS.get(i);
	}
	
	public static void addZaap(short mapa, short celda) {
		ZAAPS.put(mapa, celda);
	}
	
	public static short getCeldaZaapPorMapaID(final short mapaID) {
		try {
			if (ZAAPS.get(mapaID) != null) {
				return ZAAPS.get(mapaID);
			}
		} catch (final Exception e) {}
		return -1;
	}
	
	public static short getCeldaCercadoPorMapaID(final short mapaID) {
		final Cercado cercado = getMapa(mapaID).getCercado();
		if (cercado != null && cercado.getCeldaID() > 0) {
			return cercado.getCeldaID();
		}
		return -1;
	}
	
	public static void eliminarMontura(final Montura montura) {
		montura.setUbicacion(Ubicacion.NULL);
		_MONTURAS.remove(montura.getID());
		GestorSQL.DELETE_MONTURA(montura);
		final ArrayList<Objeto> objetos = new ArrayList<Objeto>();
		objetos.addAll(montura.getObjetos());
		eliminarObjetosPorArray(objetos);
	}
	
	public static synchronized void eliminarPersonaje(Personaje perso, final boolean totalmente) {
		// perso.getObjetos().clear();
		if (perso.esMercante()) {
			perso.getMapa().removerMercante(perso.getID());
			GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(perso.getMapa(), perso.getID());
		}
		if (totalmente) {
			if (perso.getMontura() != null) {
				eliminarMontura(perso.getMontura());
			}
			Casa casa = getCasaDePj(perso.getID());
			if (casa != null)
				casa.resetear();
			for (final Cercado cercado : CERCADOS.values()) {
				if (cercado.getDueñoID() == perso.getID()) {
					final String[] criando = cercado.strPavosCriando().split(";");
					for (final String pavo : criando) {
						try {
							eliminarMontura(getMontura(Integer.parseInt(pavo)));
						} catch (final Exception e) {}
					}
					if (cercado.strPavosCriando().length() > 0) {
						GestorSQL.DELETE_DRAGOPAVO_LISTA(cercado.strPavosCriando().replaceAll(";", ","));
					}
					cercado.resetear();
					GestorSQL.DELETE_CERCADO(cercado.getMapa().getID());
				}
			}
			if (perso.getMiembroGremio() != null) {
				final Gremio gremio = perso.getGremio();
				if (gremio.getCantidadMiembros() <= 1 || perso.getMiembroGremio().getRango() == 1) {
					eliminarGremio(gremio);
				}
				gremio.expulsarMiembro(perso.getID());
			}
			delRankingPVP(perso.getID());
			delRankingKoliseo(perso.getID());
			final Personaje esposo = getPersonaje(perso.getEsposoID());
			if (esposo != null) {
				esposo.divorciar();
				perso.divorciar();
			}
			final ArrayList<Objeto> objetos = new ArrayList<Objeto>();
			objetos.addAll(perso.getObjetosTodos());
			eliminarObjetosPorArray(objetos);
			objetos.clear();
			objetos.addAll(perso.getObjetosTienda());
			eliminarObjetosPorArray(objetos);
			GestorSQL.DELETE_PERSONAJE(perso);
			_PERSONAJES.remove(perso.getID());
		}
		MainServidor.redactarLogServidorln("SE ELIMINO EL PERSONAJE " + perso.getNombre() + " (" + perso.getID()
		+ ") PERTENICIENTE A LA CUENTA " + perso.getCuentaID());
	}
	
	public static void eliminarGremio(Gremio gremio) {
		gremio.eliminarTodosRecaudadores();
		gremio.expulsarTodosMiembros();
		borrarCasaGremio(gremio.getID());
		GestorSQL.DELETE_GREMIO(gremio.getID());
		_GREMIOS.remove(gremio.getID());
		// gremio.destruir();
	}
	
	public static int cuentasIP(final String ip) {
		int veces = 0;
		for (final Cuenta c : _CUENTAS.values()) {
			if (!c.enLinea()) {
				continue;
			}
			if (c.getActualIP().equals(ip)) {
				veces++;
			}
		}
		return veces;
	}
	
	public static void addOnline(Personaje perso) {
		if (!ONLINES.contains(perso))
			ONLINES.add(perso);
	}
	
	public static void removeOnline(Personaje perso) {
		ONLINES.remove(perso);
	}
	
	public static void addHechizo(final Hechizo hechizo) {
		HECHIZOS.put(hechizo.getID(), hechizo);
	}
	
	public static void addObjModelo(final ObjetoModelo objMod) {
		OBJETOS_MODELOS.put(objMod.getID(), objMod);
	}
	
	private static void prepararPanelItems() {
		for (short tipo = 1; tipo < 200; tipo++) {
			StringBuilder add = new StringBuilder();
			for (ObjetoModelo objMod : OBJETOS_MODELOS.values()) {
				if (objMod.getTipo() != tipo) {
					continue;
				}
				if (MainServidor.SISTEMA_ITEMS_TIPO_DE_PAGO.equalsIgnoreCase("KAMAS")) {
					if (objMod.getPrecioPanelKamas() <= 0) {
						continue;
					}
				} else {
					if (objMod.getPrecioPanelOgrinas() <= 0) {
						continue;
					}
				}
				if (add.length() > 0) {
					add.append("|");
				}
				add.append(objMod.getID() + ";");
				add.append(objMod.stringStatsModelo() + ";");
				if (MainServidor.SISTEMA_ITEMS_TIPO_DE_PAGO.equalsIgnoreCase("KAMAS")) {
					add.append(objMod.getPrecioPanelKamas());
				} else {
					add.append(objMod.getPrecioPanelOgrinas());
				}
				add.append(";");
				if (MainServidor.PARAM_SISTEMA_ITEMS_SOLO_PERFECTO) {
					add.append("1");// solo perfecto
				} else {
					add.append("0");
					if (MainServidor.PARAM_SISTEMA_ITEMS_EXO_PA_PM) {
						if (!MainServidor.SISTEMA_ITEMS_EXO_TIPOS_NO_PERMITIDOS.contains(objMod.getTipo())) {
							add.append(";" + (objMod.tieneStatInicial(Constantes.STAT_MAS_PA) ? "0" : "1") + ";" + (objMod
							.tieneStatInicial(Constantes.STAT_MAS_PM) ? "0" : "1"));
						}
					}
				}
			}
			if (add.length() > 0) {
				SISTEMA_ITEMS.put(tipo, add.toString());
			}
		}
	}
	
	public static String getTiposPanelItems() {
		StringBuilder str = new StringBuilder();
		for (short s : SISTEMA_ITEMS.keySet()) {
			if (str.length() > 0) {
				str.append(";");
			}
			str.append(s);
		}
		return str.toString();
	}
	
	public static void getObjetosPorTipo(Personaje out, short tipo) {
		if (SISTEMA_ITEMS.get(tipo) == null) {
			for (short s : SISTEMA_ITEMS.keySet()) {
				GestorSalida.ENVIAR_bSO_PANEL_ITEMS_OBJETOS_POR_TIPO(out, s + "@" + SISTEMA_ITEMS.get(s));
			}
			GestorSalida.ENVIAR_bSO_PANEL_ITEMS_OBJETOS_POR_TIPO(out, "-1@");
		} else {
			GestorSalida.ENVIAR_bSO_PANEL_ITEMS_OBJETOS_POR_TIPO(out, tipo + "@" + SISTEMA_ITEMS.get(tipo));
		}
	}
	
	public static Hechizo getHechizo(final int id) {
		return HECHIZOS.get(id);
	}
	
	public static ObjetoModelo getObjetoModelo(final int id) {
		return OBJETOS_MODELOS.get(id);
	}
	
	public static void addMobModelo(final MobModelo mob) {
		MOBS_MODELOS.put(mob.getID(), mob);
	}
	
	public static MobModelo getMobModelo(final int id) {
		return MOBS_MODELOS.get(id);
	}
	
	public static MonturaModelo getMonturaModelo(final int id) {
		return MONTURAS_MODELOS.get(id);
	}
	
	public static void addMonturaModelo(final MonturaModelo montura) {
		MONTURAS_MODELOS.put(montura.getColorID(), montura);
	}
	
	public static CopyOnWriteArrayList<Personaje> getPersonajesEnLinea() {
		return ONLINES;
	}
	
	public static void objetoIniciarServer(final int id, final int idObjModelo, final int cant, final byte pos,
	final String strStats, final int idObvi, final int precio) {
		if (getObjetoModelo(idObjModelo) == null) {
			MainServidor.redactarLogServidorln("La id del objeto " + id + " esta bug porque no tiene objModelo "
			+ idObjModelo);
			if (!MainServidor.PARAM_DESHABILITAR_SQL) {
				GestorSQL.DELETE_OBJETO(id);
			}
			return;
		}
		Objeto obj = new Objeto(id, idObjModelo, cant, pos, strStats, idObvi, precio);
		if (MainServidor.PARAM_RESETEAR_LUPEAR_OBJETOS_MAGUEADOS) {
			switch (obj.getObjModelo().getTipo()) {
				case Constantes.OBJETO_TIPO_AMULETO :
				case Constantes.OBJETO_TIPO_ANILLO :
				case Constantes.OBJETO_TIPO_CINTURON :
				case Constantes.OBJETO_TIPO_BOTAS :
				case Constantes.OBJETO_TIPO_SOMBRERO :
				case Constantes.OBJETO_TIPO_CAPA :
				case Constantes.OBJETO_TIPO_BASTON :
				case Constantes.OBJETO_TIPO_HACHA :
				case Constantes.OBJETO_TIPO_PALA :
				case Constantes.OBJETO_TIPO_ESPADA :
				case Constantes.OBJETO_TIPO_ARCO :
				case Constantes.OBJETO_TIPO_MARTILLO :
				case Constantes.OBJETO_TIPO_GUADAÑA :
				case Constantes.OBJETO_TIPO_DAGAS :
					obj.convertirStringAStats(obj.getObjModelo().generarStatsModelo(CAPACIDAD_STATS.MAXIMO));
					// obj._reseteado = true;
					GestorSQL.SALVAR_OBJETO(obj);
			}
		}
		_OBJETOS.put(id, obj);
	}
	
	public synchronized static void addObjeto(final Objeto obj, final boolean salvarSQL) {
		try {
			if (obj.getID() == 0) {
				obj.setID(sigIDObjeto());
			}
			_OBJETOS.put(obj.getID(), obj);
			if (SERVIDOR_ESTADO != Constantes.SERVIDOR_OFFLINE) {
				if (salvarSQL || obj.getObjModelo().getTipo() == Constantes.OBJETO_TIPO_OBJEVIVO) {
					GestorSQL.SALVAR_OBJETO(obj);
				}
			} else if (MainServidor.PARAM_RESET_STATS_OBJETO) {
				obj.convertirStringAStats(obj.getObjModelo().generarStatsModelo(CAPACIDAD_STATS.MAXIMO));
				GestorSQL.SALVAR_OBJETO(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Objeto getObjeto(final int id) {
		return _OBJETOS.get(id);
	}
	
	public static void eliminarObjeto(final int id) {
		if (id == 0) {
			return;
		}
		GestorSQL.DELETE_OBJETO(id);
		_OBJETOS.remove(id);
	}
	
	private static void eliminarObjetosPorArray(final ArrayList<Objeto> objetos) {
		if (objetos.isEmpty()) {
			return;
		}
		final StringBuilder str = new StringBuilder();
		for (Objeto obj : objetos) {
			str.append((str.length() > 0 ? "," : "") + obj.getID());
			_OBJETOS.remove(obj.getID());
			obj = null;
		}
		GestorSQL.DELETE_OBJETOS_LISTA(str.toString());
	}
	
	public static Montura getMontura(final int id) {
		return _MONTURAS.get(id);
	}
	
	public static synchronized void addMontura(final Montura montura, final boolean agregar) {
		if (montura.getID() < SIG_ID_MONTURA) {
			SIG_ID_MONTURA = montura.getID();
		}
		_MONTURAS.put(montura.getID(), montura);
		if (agregar) {
			GestorSQL.REPLACE_MONTURA(montura, false);
		}
	}
	
	public static synchronized void addPuestoMercadillo(final Mercadillo mercadillo) {
		MERCADILLOS.put(mercadillo.getID(), mercadillo);
	}
	
	public static Mercadillo getPuestoMercadillo(final int id) {
		return MERCADILLOS.get(id);
	}
	
	public static Mercadillo getPuestoPorMapa(final short mapa) {
		for (final Mercadillo merca : MERCADILLOS.values()) {
			if (merca.getMapas().contains(mapa)) {
				return merca;
			}
		}
		return null;
	}
	
	public static int cantPuestosMercadillos() {
		return MERCADILLOS.size();
	}
	
	// public synchronized static void addObjMercadillo(final int cuentaID, final int mercadilloID,
	// final ObjetoMercadillo objMercadillo) {
	// if (_OBJETOS_MERCADILLOS.get(cuentaID) == null) {
	// _OBJETOS_MERCADILLOS.put(cuentaID, new HashMap<Integer, ArrayList<ObjetoMercadillo>>());
	// }
	// if (_OBJETOS_MERCADILLOS.get(cuentaID).get(mercadilloID) == null) {
	// _OBJETOS_MERCADILLOS.get(cuentaID).put(mercadilloID, new ArrayList<ObjetoMercadillo>());
	// }
	// _OBJETOS_MERCADILLOS.get(cuentaID).get(mercadilloID).add(objMercadillo);
	// }
	//
	// public synchronized static void borrarObjMercadillo(final int cuentaID, final int mercadilloID,
	// final ObjetoMercadillo objMerca) {
	// try {
	// _OBJETOS_MERCADILLOS.get(cuentaID).get(mercadilloID).remove(objMerca);
	// } catch (Exception e) {}
	// }
	//
	// public static Map<Integer, ArrayList<ObjetoMercadillo>> getMisObjetosMercadillos(final int
	// cuentaID) {
	// return _OBJETOS_MERCADILLOS.get(cuentaID);
	// }
	public static Animacion getAnimacion(final int animacionId) {
		return ANIMACIONES.get(animacionId);
	}
	
	public static void addAnimacion(final Animacion animacion) {
		ANIMACIONES.put(animacion.getID(), animacion);
	}
	
	public static void addObjetoTrueque(int objetoID, String necesita, int prioridad, String npcs) {
		ObjetoTrueque objT = new ObjetoTrueque(objetoID, necesita, prioridad, npcs);
		if (!objT.getNecesita().isEmpty()) {
			OBJETOS_TRUEQUE.add(objT);
		}
	}
	
	public static ArrayList<ObjetoInteractivoModelo> getObjInteractivos() {
		return OBJETOS_INTERACTIVOS_MODELOS;
	}
	
	public static void borrarOtroInteractivo(int gfxID, short mapaID, short celdaID, int accion, boolean conAccion) {
		for (final OtroInteractivo oi : OTROS_INTERACTIVOS) {
			if (gfxID == oi.getGfxID() && mapaID == oi.getMapaID() && celdaID == oi.getCeldaID() && (!conAccion
			|| accion == oi.getAccionID())) {
				OTROS_INTERACTIVOS.remove(oi);
				GestorSQL.DELETE_OTRO_INTERACTIVO(gfxID, mapaID, celdaID, oi.getAccionID());
			}
		}
	}
	
	public static void addOtroInteractivo(final OtroInteractivo otro) {
		OTROS_INTERACTIVOS.add(otro);
	}
	
	public static void addMascotaModelo(final MascotaModelo mascota) {
		MASCOTAS_MODELOS.put(mascota.getID(), mascota);
	}
	
	public static MascotaModelo getMascotaModelo(final int id) {
		return MASCOTAS_MODELOS.get(id);
	}
	
	public static void addEspecialidad(final Especialidad especialidad) {
		ESPECIALIDADES.put(especialidad.getID(), especialidad);
	}
	
	public static Especialidad getEspecialidad(final int orden, final int nivel) {
		Especialidad esp = null;
		for (Especialidad e : ESPECIALIDADES.values()) {
			if (e.getOrden() != orden) {
				continue;
			}
			if (esp == null || e.getNivel() <= nivel && e.getNivel() > esp.getNivel()) {
				esp = e;
			}
		}
		return esp;
	}
	
	public static void addDonModelo(int id, int stat) {
		DONES_MODELOS.put(id, stat);
	}
	
	public static int getDonStat(int id) {
		return DONES_MODELOS.get(id);
	}
	
	public static void addCasa(final Casa casa) {
		CASAS.put(casa.getID(), casa);
	}
	
	public static Map<Integer, Casa> getCasas() {
		return CASAS;
	}
	
	public static Casa getCasa(final int id) {
		return CASAS.get(id);
	}
	
	public static void addCercado(final Cercado cercado) {
		CERCADOS.put(cercado.getMapa().getID(), cercado);
	}
	
	public static Cercado getCercadoPorMapa(final short mapa) {
		return CERCADOS.get(mapa);
	}
	
	public static Prisma getPrisma(final int id) {
		return _PRISMAS.get(id);
	}
	
	public static void addPrisma(final Prisma prisma) {
		if (prisma.getMapa().getPrisma() != null) {
			_PRISMAS.remove(prisma.getID());
			GestorSQL.DELETE_PRISMA(prisma.getID());
			return;
		}
		prisma.getMapa().setPrisma(prisma);
		if (prisma.getArea() != null) {
			prisma.getArea().setPrisma(prisma);
		}
		if (prisma.getSubArea() != null) {
			prisma.getSubArea().setPrisma(prisma);
		}
		if (prisma.getID() < SIG_ID_PRISMA) {
			SIG_ID_PRISMA = prisma.getID();
		}
		_PRISMAS.put(prisma.getID(), prisma);
	}
	
	public static void eliminarPrisma(final Prisma prisma) {
		prisma.getMapa().setPrisma(null);
		if (prisma.getArea() != null) {
			prisma.getArea().setPrisma(null);
		}
		if (prisma.getSubArea() != null) {
			prisma.getSubArea().setPrisma(null);
		}
		_PRISMAS.remove(prisma.getID());
		GestorSQL.DELETE_PRISMA(prisma.getID());
	}
	
	public static Collection<Prisma> getPrismas() {
		return _PRISMAS.values();
	}
	
	public static Recaudador getRecaudador(final int id) {
		return _RECAUDADORES.get(id);
	}
	
	public static void addRecaudador(final Recaudador recaudador) {
		if (recaudador.getMapa().getRecaudador() != null) {
			recaudador.getGremio().delRecaudador(recaudador);
			GestorSQL.DELETE_RECAUDADOR(recaudador.getID());
			return;
		}
		recaudador.getMapa().setRecaudador(recaudador);
		if (recaudador.getID() < SIG_ID_RECAUDADOR) {
			SIG_ID_RECAUDADOR = recaudador.getID();
		}
		_RECAUDADORES.put(recaudador.getID(), recaudador);
	}
	
	public static void eliminarRecaudador(final Recaudador recaudador) {
		recaudador.getMapa().setRecaudador(null);
		recaudador.getGremio().delRecaudador(recaudador);
		_RECAUDADORES.remove(recaudador.getID());
		GestorSQL.DELETE_RECAUDADOR(recaudador.getID());
	}
	
	public static boolean puedePonerRecauEnZona(final int subAreaID, final int gremioID) {
		int i = 0;
		for (final Recaudador recau : _RECAUDADORES.values()) {
			if (recau.getGremio().getID() == gremioID && recau.getMapa().getSubArea().getID() == subAreaID) {
				i++;
			}
		}
		return i < MainServidor.MAX_RECAUDADORES_POR_ZONA;
	}
	
	public static void addCofre(final Cofre cofre) {
		COFRES.put(cofre.getID(), cofre);
	}
	
	public static Cofre getCofre(final int id) {
		return COFRES.get(id);
	}
	
	public static void addRankingKoliseo(final RankingKoliseo rank) {
		_RANKINGS_KOLISEO.put(rank.getID(), rank);
	}
	
	public static void delRankingKoliseo(final int id) {
		GestorSQL.DELETE_RANKING_PVP(id);
		_RANKINGS_KOLISEO.remove(id);
	}
	
	public static RankingKoliseo getRankingKoliseo(final int id) {
		return _RANKINGS_KOLISEO.get(id);
	}
	
	public static void addRankingPVP(final RankingPVP rank) {
		_RANKINGS_PVP.put(rank.getID(), rank);
	}
	
	public static void delRankingPVP(final int id) {
		GestorSQL.DELETE_RANKING_PVP(id);
		_RANKINGS_PVP.remove(id);
	}
	
	public static RankingPVP getRankingPVP(final int id) {
		return _RANKINGS_PVP.get(id);
	}
	
	public static float getBalanceMundo(final Personaje perso) {
		int cant = 0;
		for (final SubArea subarea : SUB_AREAS.values()) {
			if (subarea.getAlineacion() == perso.getAlineacion()) {
				cant++;
			}
		}
		if (cant == 0 || SUB_AREAS.isEmpty()) {
			return 0;
		}
		return (float) Math.rint(1000f * cant / SUB_AREAS.size()) / 10f;
	}
	
	public static float getBalanceArea(final Personaje perso) {
		int cant = 0;
		Area area = null;
		try {
			area = perso.getMapa().getSubArea().getArea();
		} catch (Exception e) {}
		if (area == null) {
			return 0;
		}
		for (final SubArea subarea : SUB_AREAS.values()) {
			if (subarea.getArea() == area && subarea.getAlineacion() == perso.getAlineacion()) {
				cant++;
			}
		}
		if (cant == 0 || area.getSubAreas().isEmpty()) {
			return 0;
		}
		return (float) Math.rint(1000f * cant / area.getSubAreas().size()) / 10f;
	}
	
	public static float getBonusAlinExp(Personaje perso) {
		float bonus = (float) (Math.rint(Math.sqrt(MainServidor.RATE_CONQUISTA_EXPERIENCIA) * 100) / 100f);
		return (perso.getGradoAlineacion() / 2.5f) + bonus;
	}
	
	public static float getBonusAlinRecolecta(Personaje perso) {
		float bonus = (float) (Math.rint(Math.sqrt(MainServidor.RATE_CONQUISTA_RECOLECTA) * 100) / 100f);
		return (perso.getGradoAlineacion() / 2.5f) + bonus;
	}
	
	public static float getBonusAlinDrop(Personaje perso) {
		float bonus = (float) (Math.rint(Math.sqrt(MainServidor.RATE_CONQUISTA_DROP) * 100) / 100f);
		return (perso.getGradoAlineacion() / 2.5f) + bonus;
	}
	
	public static String prismasGeoposicion(final int alineacion) {
		final StringBuilder str = new StringBuilder();
		if (alineacion == Constantes.ALINEACION_BONTARIANO) {
			str.append(SubArea.BONTAS);
		} else if (alineacion == Constantes.ALINEACION_BRAKMARIANO) {
			str.append(SubArea.BRAKMARS);
		}
		str.append("|" + SUB_AREAS.size() + "|" + (SUB_AREAS.size() - (SubArea.BONTAS + SubArea.BRAKMARS)) + "|");
		boolean primero = false;
		for (final SubArea subArea : SUB_AREAS.values()) {
			if (!subArea.esConquistable()) {
				continue;
			}
			if (primero) {
				str.append(";");
			}
			str.append(subArea.getID() + ",");
			str.append(subArea.getAlineacion() + ",");
			str.append((subArea.getPrisma() == null ? 0 : (subArea.getPrisma().getPelea() == null ? 0 : 1)) + ",");// pelea
			str.append((subArea.getPrisma() == null ? 0 : (subArea.getPrisma().getMapa().getID())) + ",");
			str.append("1");// atacable
			primero = true;
		}
		str.append("|");
		if (alineacion == Constantes.ALINEACION_BONTARIANO) {
			str.append(Area.BONTAS);
		} else if (alineacion == Constantes.ALINEACION_BRAKMARIANO) {
			str.append(Area.BRAKMARS);
		}
		str.append("|" + AREAS.size() + "|");
		primero = false;
		for (final Area area : AREAS.values()) {
			if (primero) {
				str.append(";");
			}
			str.append(area.getID() + ",");
			str.append(area.getAlineacion() + ",");// alineacion
			str.append("1,");// door
			str.append(area.getPrisma() == null ? 0 : 1);// tiene prisma
			primero = true;
		}
		return str.toString();
	}
	
	public static EncarnacionModelo getEncarnacionModelo(final int id) {
		return ENCARNACIONES_MODELOS.get(id);
	}
	
	public static void addTutorial(final Tutorial tutorial) {
		TUTORIALES.put(tutorial.getID(), tutorial);
	}
	
	public static Tutorial getTutorial(final int id) {
		return TUTORIALES.get(id);
	}
	
	public static Duo<Integer, Integer> getDuoPorIDPrimero(final ArrayList<Duo<Integer, Integer>> objetos, final int id) {
		for (final Duo<Integer, Integer> duo : objetos) {
			if (duo._primero == id) {
				return duo;
			}
		}
		return null;
	}
	
	public static String stringServicios(Personaje perso) {
		boolean abonado = perso.getCuenta().esAbonado();
		StringBuilder str = new StringBuilder();
		for (Servicio s : SERVICIOS.values()) {
			if (s.string(abonado).isEmpty()) {
				continue;
			}
			if (str.length() > 0) {
				str.append("|");
			}
			str.append(s.string(abonado));
		}
		return str.toString();
	}
	
	public static int getMascotaPorFantasma(final int fantasma) {
		for (final MascotaModelo masc : MASCOTAS_MODELOS.values()) {
			if (masc.getFantasma() == fantasma) {
				return masc.getID();
			}
		}
		return 0;
	}
	
	public static void delKoliseo(int id) {
		_INSCRITOS_KOLISEO.remove(id);
	}
	
	public static void addKoliseo(final Personaje perso) {
		_INSCRITOS_KOLISEO.put(perso.getID(), perso);
		if (_RANKINGS_KOLISEO.get(perso.getID()) == null) {
			final RankingKoliseo rank = new RankingKoliseo(perso.getID(), perso.getNombre(), 0, 0);
			addRankingKoliseo(rank);
			GestorSQL.REPLACE_RANKING_KOLISEO(rank);
		}
	}
	
	public static boolean estaEnKoliseo(final int id) {
		return _INSCRITOS_KOLISEO.get(id) != null;
	}
	
	public static int cantKoliseo() {
		return _INSCRITOS_KOLISEO.size();
	}
	
	public static Collection<Personaje> getInscritosKoliseo() {
		return _INSCRITOS_KOLISEO.values();
	}
	
	public static void iniciarKoliseo() {
		final ArrayList<Mapa> mapas = new ArrayList<Mapa>();
		for (final String s : MainServidor.MAPAS_KOLISEO.split(",")) {
			try {
				mapas.add(getMapa(Short.parseShort(s)));
			} catch (final Exception e) {}
		}
		if (mapas.isEmpty()) {
			SEGUNDOS_INICIO_KOLISEO = MainServidor.SEGUNDOS_INICIAR_KOLISEO;
			GestorSalida.ENVIAR_Im_INFORMACION_KOLISEO("1KOLISEO_FALTA_MAPAS");
			return;
		}
		final ArrayList<Personaje> listos = new ArrayList<Personaje>();
		for (final Personaje p : _INSCRITOS_KOLISEO.values()) {
			if (p.puedeIrKoliseo()) {
				listos.add(p);
			} else {
				if (p.getGrupoKoliseo() != null) {
					p.getGrupoKoliseo().dejarGrupo(p);
				}
			}
		}
		if (listos.size() < MainServidor.CANTIDAD_MIEMBROS_EQUIPO_KOLISEO * 2) {
			SEGUNDOS_INICIO_KOLISEO = MainServidor.SEGUNDOS_INICIAR_KOLISEO;
			GestorSalida.ENVIAR_Im_INFORMACION_KOLISEO("1KOLISEO_FALTA_INSCRITOS");
			return;
		}
		_INSCRITOS_KOLISEO.clear();
		for (final Personaje p : listos) {
			final int ptsNivel = p.getNivel() * 5;
			int ptsStats = 0;
			int ptsKoliseo = 0;
			if (MainServidor.PARAM_CLASIFICAR_POR_STUFF_EN_KOLISEO) {
				ptsStats = Constantes.convertirStatsEnPuntosKoliseo(p.getStatsObjEquipados());
			}
			if (MainServidor.PARAM_CLASIFICAR_POR_RANKING_EN_KOLISEO) {
				int ptsVictorias = getRankingKoliseo(p.getID()).getVictorias() * 10;
				int ptsDerrotas = getRankingKoliseo(p.getID()).getDerrotas() * 7;
				ptsKoliseo = Math.max(-40, Math.min(40, ptsVictorias - ptsDerrotas));
			}
			p.setPuntKoli(ptsNivel + ptsStats + ptsKoliseo);
		}
		final ArrayList<Personaje> ordenados = new ArrayList<Personaje>();
		while (true) {
			int maximo = 0;
			if (ordenados.size() == listos.size()) {
				break;
			}
			Personaje p = null;
			for (final Personaje d : listos) {
				if (ordenados.contains(d)) {
					continue;
				}
				if (d.getPuntoKoli() > maximo) {
					maximo = d.getPuntoKoli();
					p = d;
				}
			}
			if (p != null) {
				ordenados.add(p);
			} else {
				break;
			}
		}
		final ArrayList<GrupoKoliseo> grupos = new ArrayList<GrupoKoliseo>();
		int i = 0, j = 1;
		boolean b = true;
		for (final Personaje p : ordenados) {
			if (p.getGrupoKoliseo() != null) {
				if (!grupos.contains(p.getGrupoKoliseo())) {
					grupos.add(p.getGrupoKoliseo());
				}
			} else {
				while (true) {
					if (b) {
						try {
							if (grupos.get(i).addPersonaje(p)) {
								p.setGrupoKoliseo(grupos.get(i));
								break;
							} else {
								i = i + 2;
							}
						} catch (final Exception e) {
							final GrupoKoliseo g = new GrupoKoliseo(p);
							p.setGrupoKoliseo(g);
							grupos.add(g);
							break;
						}
					} else {
						try {
							if (grupos.get(j).addPersonaje(p)) {
								p.setGrupoKoliseo(grupos.get(j));
								break;
							} else {
								j = j + 2;
							}
						} catch (final Exception e) {
							final GrupoKoliseo g = new GrupoKoliseo(p);
							p.setGrupoKoliseo(g);
							grupos.add(g);
							break;
						}
					}
					b = !b;
				}
			}
		}
		final ArrayList<GrupoKoliseo> combate = new ArrayList<GrupoKoliseo>();
		while (true) {
			int maximo = 0;
			if (combate.size() == grupos.size()) {
				break;
			}
			GrupoKoliseo p = null;
			for (final GrupoKoliseo d : grupos) {
				if (combate.contains(d)) {
					continue;
				}
				if (d.getPuntuacion() > maximo) {
					maximo = d.getPuntuacion();
					p = d;
				}
			}
			if (p != null) {
				combate.add(p);
			} else {
				break;
			}
		}
		for (int x = 0; x + 1 < combate.size(); x += 2) {
			try {
				GrupoKoliseo grupoA = combate.get(x);
				GrupoKoliseo grupoB = combate.get(x + 1);
				if (grupoA.getCantPjs() != grupoB.getCantPjs() || grupoA.contieneIPOtroGrupo(grupoB)) {
					x -= 1;
					continue;
				}
				Mapa mapa = mapas.get(Formulas.getRandomInt(0, mapas.size() - 1));
				if (mapa.iniciarPeleaKoliseo(grupoA, grupoB)) {
					grupos.remove(grupoA);
					grupos.remove(grupoB);
				}
			} catch (final Exception e) {}
		}
		for (final GrupoKoliseo k : grupos) {
			for (final Personaje p : k.getMiembros()) {
				GestorSalida.ENVIAR_Im_INFORMACION(p, "1KOLISEO_NO_ELEGIDO");
				_INSCRITOS_KOLISEO.put(p.getID(), p);
			}
			k.limpiarGrupo();
		}
		SEGUNDOS_INICIO_KOLISEO = MainServidor.SEGUNDOS_INICIAR_KOLISEO;
	}
	
	public static Map<Integer, Integer> listaObjetosTruequePor(Map<Integer, Integer> aDar, int npcID) {
		Map<Integer, Integer> recibir = new TreeMap<Integer, Integer>();
		Map<Integer, Integer> aDar2 = new TreeMap<Integer, Integer>();
		aDar2.putAll(aDar);
		ArrayList<ObjetoTrueque> objetos = new ArrayList<>();
		for (ObjetoTrueque objT : OBJETOS_TRUEQUE) {
			if (objT.permiteNPC(npcID) && !objT.getNecesita().isEmpty()) {
				objetos.add(objT);
			}
		}
		Collections.sort(objetos);
		for (ObjetoTrueque objT : objetos) {
			int cantFinal = -1;
			boolean completo = true;
			for (Entry<Integer, Integer> a : objT.getNecesita().entrySet()) {
				int cant = 0;
				int idNecesita = a.getKey();
				int cantNecesita = a.getValue();
				try {
					int tiene = aDar2.get(idNecesita);
					cant = tiene / cantNecesita;
				} catch (Exception e) {
					completo = false;
					break;
				}
				if (cant <= 0) {
					completo = false;
					break;
				}
				if (cantFinal == -1 || cant < cantFinal) {
					cantFinal = cant;
				}
			}
			if (completo) {
				for (Entry<Integer, Integer> a : objT.getNecesita().entrySet()) {
					int idNecesita = a.getKey();
					int cantNecesita = a.getValue();
					int tiene = aDar2.get(idNecesita) - (cantFinal * cantNecesita);
					if (tiene <= 0) {
						aDar2.remove(idNecesita);
					} else {
						aDar2.put(idNecesita, tiene);
					}
				}
				int actual = 0;
				if (recibir.containsKey(objT.getID())) {
					actual = recibir.get(objT.getID());
				}
				recibir.put(objT.getID(), actual + cantFinal);
			}
		}
		return recibir;
	}
	// public static void crearGruposKoliseo1() {
	// CopyOnWriteArrayList<Personaje> kolis1 = new CopyOnWriteArrayList<Personaje>();
	// for (Personaje persos : Koliseo1) {
	// if ((persos == null) || (!persos.enLinea()))
	// continue;
	// kolis1.add(persos);
	// }
	// if (kolis1.size() < 6)
	// return;
	// int size = kolis1.size();
	// for (int i = 0; i < size; i += 3) {
	// Personaje koli1 = null;
	// Personaje koli2 = null;
	// Personaje koli3 = null;
	// Random rand = new Random();
	// int random = rand.nextInt(kolis1.size() - 1);
	// koli1 = kolis1.get(random);
	// kolis1.remove(random);
	// random = rand.nextInt(kolis1.size() - 1);
	// koli2 = kolis1.get(random);
	// kolis1.remove(random);
	// random = rand.nextInt(kolis1.size() - 1);
	// koli3 = kolis1.get(random);
	// kolis1.remove(random);
	// if ((koli1 != null) && (koli2 != null) && (koli3 != null)) {
	// GrupoKoliseo grupo = new GrupoKoliseo(koli1, koli2, koli3, 1);
	// GrupoKoliseo1.add(grupo);
	// }
	// }
	// }
	//
	// public static void crearGruposKoliseo2() {
	// CopyOnWriteArrayList<Personaje> kolis1 = new CopyOnWriteArrayList<Personaje>();
	// for (Personaje persos : Koliseo2) {
	// if ((persos == null) || (!persos.enLinea()))
	// continue;
	// kolis1.add(persos);
	// }
	// if (kolis1.size() < 6)
	// return;
	// int size = kolis1.size();
	// for (int i = 0; i < size; i += 3) {
	// Personaje koli1 = null;
	// Personaje koli2 = null;
	// Personaje koli3 = null;
	// Random rand = new Random();
	// int random = rand.nextInt(kolis1.size() - 1);
	// koli1 = kolis1.get(random);
	// kolis1.remove(random);
	// random = rand.nextInt(kolis1.size() - 1);
	// koli2 = kolis1.get(random);
	// kolis1.remove(random);
	// random = rand.nextInt(kolis1.size() - 1);
	// koli3 = kolis1.get(random);
	// kolis1.remove(random);
	// if ((koli1 != null) && (koli2 != null) && (koli3 != null)) {
	// GrupoKoliseo grupo = new GrupoKoliseo(koli1, koli2, koli3, 1);
	// GrupoKoliseo2.add(grupo);
	// }
	// }
	// }
	//
	// public static void crearGruposKoliseo3() {
	// CopyOnWriteArrayList<Personaje> kolis1 = new CopyOnWriteArrayList<Personaje>();
	// for (Personaje persos : Koliseo3) {
	// if ((persos == null) || (!persos.enLinea()))
	// continue;
	// kolis1.add(persos);
	// }
	// if (kolis1.size() < 6)
	// return;
	// int size = kolis1.size();
	// for (int i = 0; i < size; i += 3) {
	// Personaje koli1 = null;
	// Personaje koli2 = null;
	// Personaje koli3 = null;
	// Random rand = new Random();
	// int random = rand.nextInt(kolis1.size() - 1);
	// koli1 = kolis1.get(random);
	// kolis1.remove(random);
	// random = rand.nextInt(kolis1.size() - 1);
	// koli2 = kolis1.get(random);
	// kolis1.remove(random);
	// random = rand.nextInt(kolis1.size() - 1);
	// koli3 = kolis1.get(random);
	// kolis1.remove(random);
	// if ((koli1 != null) && (koli2 != null) && (koli3 != null)) {
	// GrupoKoliseo grupo = new GrupoKoliseo(koli1, koli2, koli3, 1);
	// GrupoKoliseo3.add(grupo);
	// }
	// }
	// }
	public static class Duo<L, R> {
		public L _primero;
		public R _segundo;
		
		public Duo(final L s, final R i) {
			_primero = s;
			_segundo = i;
		}
	}
	public static class Experiencia {
		public int _oficio, _montura, _alineacion;
		public long _personaje, _gremio, _encarnacion;
		
		public Experiencia(final long perso, final int oficio, final int montura, final long gremio, final int pvp,
		final int encarnacion) {
			_personaje = perso;
			_oficio = oficio;
			_montura = montura;
			_alineacion = pvp;
			_gremio = gremio;
			_encarnacion = encarnacion;
		}
	}
}
