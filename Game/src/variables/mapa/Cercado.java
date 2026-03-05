package variables.mapa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import sprites.Exchanger;
import variables.gremio.Gremio;
import variables.montura.Montura;
import variables.montura.Montura.Ubicacion;
import variables.objeto.Objeto;
import variables.personaje.Personaje;
import estaticos.Formulas;
import estaticos.Mundo;

public class Cercado implements Exchanger {
	private byte _capacidadMax, _objetosMax;
	private short _celda = -1, _celdaMontura, _celdaPuerta;
	private int _dueñoID, _precioPJ, _precioOriginal;
	private Gremio _gremio;
	private final Mapa _mapa;
	// private final Map<Short, Map<Integer, Objeto>> _objCrianzaConDueño = new HashMap<Short,
	// Map<Integer, Objeto>>();
	private final Map<Short, Objeto> _objCrianza = new HashMap<Short, Objeto>();
	private final ConcurrentHashMap<Integer, Montura> _criando = new ConcurrentHashMap<Integer, Montura>();
	private final ArrayList<Short> _celdasObjeto = new ArrayList<Short>();
	
	// private static String CERCADO_8848 =
	// "305;0|171;0|308;0|311;0|413;0|470;0|228;0|527;0|194;0|254;0|117;0|251;0|365;0";
	// private static String CERCADO_8744 =
	// "550;0|304;0|474;0|337;0|545;0|400;0|394;0|213;0|453;0|270;0|451;0|420;0|361;0";
	// private static String CERCADO_8743 =
	// "305;0|272;0|413;0|470;0|522;0|319;0|359;0|601;0|416;0|215;0|421;0|362;0|211;0";
	// private static String CERCADO_8747 =
	// "476;0|415;0|234;0|432;0|438;0|358;0|325;0|291;0|486;0|301;0|637;0|268;0|211;0";
	// private static String CERCADO_8746 =
	// "513;0|559;0|380;0|527;0|377;0|193;0|288;0|488;0|323;0|355;0|635;0|454;0|603;0";
	// private static String CERCADO_8745 =
	// "307;0|341;0|512;0|581;0|505;0|231;0|471;0|383;0|587;0|395;0|429;0|417;0|301;0";
	// private static String CERCADO_8752 =
	// "304;0|476;0|474;0|65;0|544;0|472;0|232;0|381;0|468;0|228;0|253;0|156;0|396;0";
	// private static String CERCADO_8750 =
	// "100;0|472;0|172;0|197;0|400;0|324;0|252;0|320;0|248;0|396;0|213;0|244;0|121;0";
	// private static String CERCADO_8851 =
	// "544;0|472;0|400;0|286;0|379;0|358;0|190;0|217;0|430;0|82;0|247;0|214;0|328;0";
	// private static String CERCADO_8749 =
	// "342;0|580;0|504;0|475;0|432;0|471;0|465;0|392;0|253;0|250;0|528;0|418;0|177;0";
	// private static String CERCADO_8748 =
	// "343;0|137;0|308;0|402;0|436;0|393;0|564;0|80;0|397;0|560;0|321;0|267;0|451;0";
	// private static String CERCADO_8751 =
	// "342;0|504;0|472;0|567;0|493;0|495;0|290;0|251;0|396;0|176;0|419;0|385;0|542;0";
	public Cercado(final Mapa mapa, final byte capacidad, final byte objetos, final short celdaID,
	final short celdaPuerta, final short celdaMontura, final String celdasObjetos, int precioOriginal) {
		_capacidadMax = capacidad;
		_objetosMax = objetos;
		_mapa = mapa;
		_celda = celdaID;
		_celdaMontura = celdaMontura;
		_celdaPuerta = celdaPuerta;
		_precioOriginal = precioOriginal;
		for (final String celda : celdasObjetos.split(";")) {
			try {
				_celdasObjeto.add(Short.parseShort(celda));
			} catch (Exception e) {}
		}
		_celdasObjeto.trimToSize();
		if (_mapa != null) {
			_mapa.setCercado(this);
		}
		if (_mapa != null) {
			boolean publico = true;
			String objCrianza = "";
			switch (_mapa.getID()) {
				case 8848 :
					objCrianza = "305;0|171;0|308;0|311;0|413;0|470;0|228;0|527;0|194;0|254;0|117;0|251;0|365;0";
					break;
				case 8744 :
					objCrianza = "550;0|304;0|474;0|337;0|545;0|400;0|394;0|213;0|453;0|270;0|451;0|420;0|361;0";
					break;
				case 8743 :
					objCrianza = "305;0|272;0|413;0|470;0|522;0|319;0|359;0|601;0|416;0|215;0|421;0|362;0|211;0";
					break;
				case 8747 :
					objCrianza = "476;0|415;0|234;0|432;0|438;0|358;0|325;0|291;0|486;0|301;0|637;0|268;0|211;0";
					break;
				case 8746 :
					objCrianza = "513;0|559;0|380;0|527;0|377;0|193;0|288;0|488;0|323;0|355;0|635;0|454;0|603;0";
					break;
				case 8745 :
					objCrianza = "307;0|341;0|512;0|581;0|505;0|231;0|471;0|383;0|587;0|395;0|429;0|417;0|301;0";
					break;
				case 8752 :
					objCrianza = "304;0|476;0|474;0|65;0|544;0|472;0|232;0|381;0|468;0|228;0|253;0|156;0|396;0";
					break;
				case 8750 :
					objCrianza = "100;0|472;0|172;0|197;0|400;0|324;0|252;0|320;0|248;0|396;0|213;0|244;0|121;0";
					break;
				case 8851 :
					objCrianza = "544;0|472;0|400;0|286;0|379;0|358;0|190;0|217;0|430;0|82;0|247;0|214;0|328;0";
					break;
				case 8749 :
					objCrianza = "342;0|580;0|504;0|475;0|432;0|471;0|465;0|392;0|253;0|250;0|528;0|418;0|177;0";
					break;
				case 8748 :
					objCrianza = "343;0|137;0|308;0|402;0|436;0|393;0|564;0|80;0|397;0|560;0|321;0|267;0|451;0";
					break;
				case 8751 :
					objCrianza = "342;0|504;0|472;0|567;0|493;0|495;0|290;0|251;0|396;0|176;0|419;0|385;0|542;0";
					break;
				default :
					publico = false;
					break;
			}
			if (publico) {
				_dueñoID = -1;
				_precioOriginal = _precioPJ = 0;
				for (final String str : objCrianza.split(Pattern.quote("|"))) {
					try {
						final String[] infos = str.split(";");
						if (Integer.parseInt(infos[1]) == 0) {
							_objCrianza.put(Short.parseShort(infos[0]), null);
							continue;
						}
						final Objeto objeto = Mundo.getObjeto(Integer.parseInt(infos[1]));
						if (objeto == null || objeto.getDurabilidad() <= 0) {
							continue;
						}
						_objCrianza.put(Short.parseShort(infos[0]), objeto);
					} catch (Exception e) {}
				}
			}
		}
	}
	
	public void actualizarCercado(int dueñoID, int gremio, int precio, String objCrianza, String criando) {
		_dueñoID = dueñoID;
		_gremio = null;
		if (_dueñoID > 0) {
			Personaje dueño = Mundo.getPersonaje(_dueñoID);
			if (dueño == null) {
				_dueñoID = 0;
			} else {
				_gremio = dueño.getGremio();
			}
			_precioPJ = precio;
			for (final String str : objCrianza.split(Pattern.quote("|"))) {
				try {
					final String[] infos = str.split(";");
					if (Integer.parseInt(infos[1]) == 0) {
						_objCrianza.put(Short.parseShort(infos[0]), null);
						continue;
					}
					final Objeto objeto = Mundo.getObjeto(Integer.parseInt(infos[1]));
					if (objeto == null || objeto.getDurabilidad() <= 0) {
						continue;
					}
					_objCrianza.put(Short.parseShort(infos[0]), objeto);
				} catch (Exception e) {}
			}
		}
		for (final String montura : criando.split(";")) {
			try {
				final Montura DP = Mundo.getMontura(Integer.parseInt(montura));
				if (DP.getCelda() == null) {
					continue;
				}
				_criando.put(DP.getID(), DP);
			} catch (Exception e) {}
		}
	}
	
	public void resetear() {
		_dueñoID = 0;
		_precioPJ = 3_000_000;
		_gremio = null;
		_objCrianza.clear();
		_criando.clear();
	}
	
	public synchronized void startMoverMontura() {
		for (final Montura montura : _criando.values()) {
			final int dir = (Formulas.getRandomInt(0, 3) * 2) + 1;
			montura.moverMontura(null, dir, 3, false);
			try {
				Thread.sleep(300);
			} catch (final Exception e) {}
		}
	}
	
	public String strObjCriaParaBD() {
		if (_objCrianza.isEmpty()) {
			return "";
		}
		final StringBuilder str = new StringBuilder();
		for (final Entry<Short, Objeto> entry : _objCrianza.entrySet()) {
			if (str.length() > 0) {
				str.append("|");
			}
			str.append(entry.getKey() + ";" + (esPublico() ? 0 : entry.getValue().getID()));
		}
		return str.toString();
	}
	
	public ArrayList<Objeto> getObjetosParaBD() {
		final ArrayList<Objeto> objetos = new ArrayList<Objeto>();
		for (final Objeto obj : _objCrianza.values()) {
			if (obj == null) {
				continue;
			}
			objetos.add(obj);
		}
		return objetos;
	}
	
	public Map<Short, Objeto> getObjetosCrianza() {
		return _objCrianza;
	}
	
	public void setTamañoyObjetos(final byte tamaño, final byte objetos) {
		_capacidadMax = tamaño;
		_objetosMax = objetos;
	}
	
	public void addObjetoCria(final short celda, final Objeto objeto, final int dueño) {
		// final Map<Integer, Objeto> otro = new TreeMap<Integer, Objeto>();
		// otro.put(dueño, objeto);
		_objCrianza.put(celda, objeto);
		// _objCrianzaConDueño.put(celda, otro);
	}
	
	public boolean retirarObjCria(final short celda, final Personaje perso) {
		if (!_objCrianza.containsKey(celda)) {
			return false;
		}
		if (perso != null) {// si el jugador lo retira intencionalmente
			perso.addObjIdentAInventario(_objCrianza.get(celda), true);
		} else {// si se elimnia por desgaste
			Mundo.eliminarObjeto(_objCrianza.get(celda).getID());
		}
		// _objCrianzaConDueño.remove(celda);
		_objCrianza.remove(celda);
		return true;
	}
	
	public int getCantObjColocados() {
		return _objCrianza.size();
	}
	
	public String getStringCeldasObj() {
		if (_celdasObjeto.isEmpty()) {
			return "";
		}
		final StringBuilder str = new StringBuilder();
		for (final short celda : _celdasObjeto) {
			if (str.length() > 0) {
				str.append(";");
			}
			str.append(celda);
		}
		return str.toString();
	}
	
	public ArrayList<Short> getCeldasObj() {
		return _celdasObjeto;
	}
	
	public void addCeldaObj(final short celda) {
		if (_celdasObjeto.contains(celda) || celda <= 0) {
			return;
		}
		_celdasObjeto.add(celda);
		_celdasObjeto.trimToSize();
	}
	
	public void addCeldaMontura(final short celda) {
		_celdaMontura = celda;
	}
	
	public byte getCantObjMax() {
		return _objetosMax;
	}
	
	public short getCeldaMontura() {
		return _celdaMontura;
	}
	
	public short getCeldaPuerta() {
		return _celdaPuerta;
	}
	
	public String strPavosCriando() {
		if (_criando.isEmpty()) {
			return "";
		}
		final StringBuilder str = new StringBuilder();
		for (Entry<Integer, Montura> entry : _criando.entrySet()) {
			if (str.length() > 0) {
				str.append(";");
			}
			str.append(entry.getKey());
		}
		return str.toString();
	}
	
	public void addCriando(final Montura montura) {
		_criando.put(montura.getID(), montura);
		montura.setUbicacion(Ubicacion.CERCADO);
	}
	
	public boolean puedeAgregar() {
		return _criando.size() < _capacidadMax;
	}
	
	public ConcurrentHashMap<Integer, Montura> getCriando() {
		return _criando;
	}
	
	public boolean borrarMonturaCercado(int id) {
		return _criando.remove(id) != null;
	}
	
	public byte getCapacidadMax() {
		return _capacidadMax;
	}
	
	public boolean esPublico() {
		return _dueñoID == -1;
	}
	
	public int getDueñoID() {
		return _dueñoID;
	}
	
	public void setDueñoID(final int dueño) {
		_dueñoID = dueño;
	}
	
	public Gremio getGremio() {
		return _gremio;
	}
	
	public void setGremio(final Gremio gremio) {
		_gremio = gremio;
	}
	
	public Mapa getMapa() {
		return _mapa;
	}
	
	public short getCeldaID() {
		return _celda;
	}
	
	public int getPrecioPJ() {
		return _precioPJ;
	}
	
	public int getPrecio() {
		return _dueñoID > 0 ? _precioPJ : _precioOriginal;
	}
	
	public void setPrecioPJ(final int precio) {
		_precioPJ = precio;
	}
	
	public String informacionCercado() {
		return "Rp" + _dueñoID + ";" + getPrecio() + ";" + _capacidadMax + ";" + _objetosMax + ";" + (_gremio == null
		? ";"
		: (_gremio.getNombre() + ";" + _gremio.getEmblema()));
	}
	
	@Override
	public void addKamas(long kamas, Personaje perso) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public long getKamas() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public synchronized void addObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {}
	
	@Override
	public synchronized void remObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {}
	
	@Override
	public void cerrar(Personaje perso, String exito) {
		perso.cerrarVentanaExchange(exito);
	}
	
	public void botonOK(Personaje perso) {}
	
	@Override
	public String getListaExchanger(Personaje perso) {
		// TODO Auto-generated method stub
		return null;
	}
}