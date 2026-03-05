package variables.gremio;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;
import sprites.Preguntador;
import sprites.Exchanger;
import sprites.PreLuchador;
import variables.hechizo.StatHechizo;
import variables.mapa.Celda;
import variables.mapa.Mapa;
import variables.objeto.Objeto;
import variables.pelea.Luchador;
import variables.pelea.Pelea;
import variables.personaje.Personaje;
import variables.stats.Stats;
import variables.stats.TotalStats;
import estaticos.Camino;
import estaticos.Constantes;
import estaticos.MainServidor;
import estaticos.Encriptador;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.Mundo;
import estaticos.Mundo.Duo;

public class Recaudador implements PreLuchador, Exchanger, Preguntador {
	private final int _id;
	private int _dueño;
	private byte _direccion;
	private long _kamas, _exp, _proxMovimiento = -1, _tiempoProteccion, _tiempoCreacion;
	private boolean _enRecolecta = false;
	private String _nombre1 = "", _nombre2 = "";
	private final Map<Integer, Objeto> _objetos = new TreeMap<Integer, Objeto>();
	private final Map<Integer, Integer> _objModeloID = new TreeMap<Integer, Integer>();
	private Pelea _pelea;
	private Gremio _gremio;
	private final Mapa _mapa;
	private Celda _celda;
	private TotalStats _totalStats;
	
	public Recaudador(final int id, final short mapa, final short celdaID, final byte orientacion, final int gremioID,
	final String N1, final String N2, final String objetos, final long kamas, final long xp, final long tiempoProteccion,
	long tiempoCreacion, int dueño) {
		_id = id;
		_mapa = Mundo.getMapa(mapa);
		_celda = _mapa.getCelda(celdaID);
		_direccion = orientacion;
		_nombre1 = N1;
		_nombre2 = N2;
		for (final String str : objetos.split(Pattern.quote("|"))) {
			try {
				final Objeto obj = Mundo.getObjeto(Integer.parseInt(str));
				if (obj == null) {
					continue;
				}
				_objetos.put(obj.getID(), obj);
			} catch (final Exception e) {}
		}
		_exp = xp;
		_tiempoProteccion = tiempoProteccion;
		_tiempoCreacion = tiempoCreacion;
		_dueño = dueño;
		addKamas(kamas, null);
		restarMovimiento();
		_gremio = Mundo.getGremio(gremioID);
		try {
			_gremio.addRecaudador(this);
			_totalStats = new TotalStats(_gremio.getStatsPelea(), null, new Stats(), null, 3);
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("Recaudador con gremio inexistente " + gremioID);
			GestorSQL.DELETE_RECAUDADOR(_id);
		}
	}
	
	public int getDueño() {
		return _dueño;
	}
	
	public long getTiempoCreacion() {
		return _tiempoCreacion;
	}
	
	public void addTiempProtecion(int segundos) {
		long l = Math.max(System.currentTimeMillis(), _tiempoProteccion);
		l += (segundos * 1000l);
		if (l < System.currentTimeMillis()) {
			l = 0;
		}
		_tiempoProteccion = l;
	}
	
	public long getTiempoRestProteccion() {
		long l = _tiempoProteccion - System.currentTimeMillis();
		if (l < 0) {
			l = 0;
		}
		return l;
	}
	
	public long getTiempoProteccion() {
		return _tiempoProteccion;
	}
	
	private void restarMovimiento() {
		_proxMovimiento = System.currentTimeMillis() + (MainServidor.SEGUNDOS_MOVER_RECAUDADOR * 1000);
	}
	
	public int getID() {
		return _id;
	}
	
	public void setPelea(final Pelea pelea) {
		_pelea = pelea;
	}
	
	public Pelea getPelea() {
		return _pelea;
	}
	
	public short getPeleaID() {
		if (_pelea == null) {
			return -1;
		}
		return _pelea.getID();
	}
	
	public String getN1() {
		return _nombre1;
	}
	
	public String getN2() {
		return _nombre2;
	}
	
	public int getPodsActuales() {
		int pods = 0;
		for (final Entry<Integer, Objeto> entry : _objetos.entrySet()) {
			pods += entry.getValue().getObjModelo().getPeso() * entry.getValue().getCantidad();
		}
		return pods;
	}
	
	public long getKamas() {
		return _kamas;
	}
	
	public void addKamas(final long kamas, Personaje perso) {
		if (kamas == 0) {
			return;
		}
		_kamas += kamas;
		if (_kamas < 0) {
			_kamas = 0;
		}
	}
	
	public long getExp() {
		return _exp;
	}
	
	public void addExp(final long xp) {
		_exp += xp;
	}
	
	public Gremio getGremio() {
		return _gremio;
	}
	
	public void setEnRecolecta(final boolean recolecta) {
		_enRecolecta = recolecta;
	}
	//
	// public byte getEstadoPelea() {
	// try {
	// return _pelea.getFase();
	// } catch (Exception e) {
	// return 0;
	// }
	// }
	
	public boolean getEnRecolecta() {
		return _enRecolecta;
	}
	
	public int getOrientacion() {
		return _direccion;
	}
	
	public Mapa getMapa() {
		return _mapa;
	}
	
	public void puedeMoverRecaudador() {
		if (_proxMovimiento <= 0) {
			return;
		}
		if (System.currentTimeMillis() - _proxMovimiento >= 0) {
			moverRecaudador();
			restarMovimiento();
		}
	}
	
	public void moverRecaudador() {
		if (_enRecolecta || _pelea != null) {
			return;
		}
		final short celdaDestino = Camino.celdaMoverSprite(_mapa, _celda.getID());
		if (celdaDestino == -1) {
			return;
		}
		final Duo<Integer, ArrayList<Celda>> pathCeldas = Camino.getPathPelea(_mapa, _celda.getID(), celdaDestino, -1, null,
		false);
		if (pathCeldas == null) {
			return;
		}
		final ArrayList<Celda> celdas = pathCeldas._segundo;
		String pathStr = Camino.getPathComoString(_mapa, celdas, _celda.getID(), false);
		if (pathStr.isEmpty()) {
			MainServidor.redactarLogServidorln("Fallo de desplazamiento de mob grupo: camino vacio - MapaID: " + _mapa.getID()
			+ " - CeldaID: " + _celda.getID());
			return;
		}
		try {
			Thread.sleep(100);
		} catch (final Exception e) {}
		GestorSalida.ENVIAR_GA_MOVER_SPRITE_MAPA(_mapa, 0, 1, _id + "", Encriptador.getValorHashPorNumero(_direccion)
		+ Encriptador.celdaIDAHash(_celda.getID()) + pathStr);
		_direccion = Camino.getIndexPorDireccion(pathStr.charAt(pathStr.length() - 3));
		_celda = _mapa.getCelda(celdaDestino);
	}
	
	public void borrarRecaudador() {
		GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(_mapa, _id);
		for (final Objeto obj : _objetos.values()) {
			Mundo.eliminarObjeto(obj.getID());
		}
		Mundo.eliminarRecaudador(this);
	}
	
	public Collection<Objeto> getObjetos() {
		return _objetos.values();
	}
	
	public void clearObjetos() {
		_objetos.clear();
	}
	
	public boolean addObjAInventario(final Objeto objeto) {
		if (_objetos.containsKey(objeto.getID())) {
			return false;
		}
		// tipo piedra de alma y mascota
		if (objeto.puedeTenerStatsIguales()) {
			for (final Objeto obj : _objetos.values()) {
				if (Constantes.esPosicionEquipamiento(obj.getPosicion())) {
					continue;
				}
				if (objeto.getID() != obj.getID() && obj.getObjModeloID() == objeto.getObjModeloID() && obj.sonStatsIguales(
				objeto)) {
					obj.setCantidad(obj.getCantidad() + objeto.getCantidad());
					if (objeto.getID() > 0) {
						Mundo.eliminarObjeto(objeto.getID());
					}
					return true;
				}
			}
		}
		addObjeto(objeto);
		return false;
	}
	
	private void addObjeto(final Objeto objeto) {
		try {
			if (objeto.getID() == 0) {
				Mundo.addObjeto(objeto, false);
			} else {
				GestorSQL.SALVAR_OBJETO(objeto);
			}
			_objetos.put(objeto.getID(), objeto);
		} catch (Exception e) {}
	}
	
	public String stringListaObjetosBD() {
		final StringBuilder str = new StringBuilder();
		for (final Objeto obj : _objetos.values()) {
			str.append(obj.getID() + "|");
		}
		return str.toString();
	}
	
	private String stringRecolecta() {
		final StringBuilder str = new StringBuilder("|" + _exp);
		for (final Entry<Integer, Integer> entry : _objModeloID.entrySet()) {
			str.append(";" + entry.getKey() + "," + entry.getValue());
		}
		// for (final Objeto obj : _objetos.values()) {
		// str.append(";" + obj.getIDObjModelo() + "," + obj.getCantidad());
		// }
		return str.toString();
	}
	
	public String stringPanelInfo(Personaje perso) {
		return _nombre1 + "," + _nombre2 + "|" + _mapa.getID() + "|" + _mapa.getX() + "|" + _mapa.getY() + "|" + perso
		.getNombre();
	}
	
	public String stringGM() {
		if (_pelea != null || _gremio == null) {
			return "";
		}
		final StringBuilder str = new StringBuilder();
		str.append(_celda.getID() + ";");
		str.append(_direccion + ";");
		str.append("0;");
		str.append(_id + ";");
		str.append(_nombre1 + "," + _nombre2 + ";");
		str.append("-6;");// tipo
		str.append("6000^100;");// gfxID ^ talla
		str.append(_gremio.getNivel() + ";");
		str.append(_gremio.getNombre() + ";" + _gremio.getEmblema());
		return str.toString();
	}
	
	public String mensajeDeAtaque() {
		return "A" + _nombre1 + "," + _nombre2 + "|.|" + _mapa.getID() + "|" + _celda.getID();
	}
	
	public String atacantesAlGremio() {
		final StringBuilder str = new StringBuilder("+" + Integer.toString(_id, 36));
		try {
			for (final Luchador luchador : _pelea.luchadoresDeEquipo(1)) {
				final Personaje perso = luchador.getPersonaje();
				if (perso == null) {
					continue;
				}
				str.append("|" + Integer.toString(perso.getID(), 36) + ";");
				str.append(perso.getNombre() + ";");
				str.append(perso.getNivel() + ";");
			}
		} catch (Exception e) {}
		return str.toString();
	}
	
	public String defensoresDelGremio() {
		final StringBuilder str = new StringBuilder("+" + Integer.toString(_id, 36));
		try {
			final StringBuilder stra = new StringBuilder("-");
			for (final Luchador luchador : _pelea.luchadoresDeEquipo(2)) {
				final Personaje perso = luchador.getPersonaje();
				if (perso == null) {
					continue;
				}
				str.append("|" + Integer.toString(perso.getID(), 36) + ";");
				str.append(perso.getNombre() + ";");
				str.append(perso.getGfxID(false) + ";");
				str.append(perso.getNivel() + ";");
			}
			stra.append(str.substring(1));
			_pelea.setListaDefensores(stra.toString());
		} catch (Exception e) {}
		return str.toString();
	}
	
	public String getListaExchanger(Personaje perso) {
		final StringBuilder str = new StringBuilder();
		for (final Objeto obj : _objetos.values()) {
			str.append("O" + obj.stringObjetoConGuiño());
		}
		if (_kamas > 0) {
			str.append("G" + _kamas);
		}
		return str.toString();
	}
	
	public void actualizarAtacantesDefensores() {
		if (_pelea == null)
			return;
		final String str = defensoresDelGremio();
		final String str2 = atacantesAlGremio();
		for (final Personaje p : _gremio.getMiembros()) {
			if (p.enLinea()) {
				GestorSalida.ENVIAR_gITP_INFO_DEFENSORES_RECAUDADOR(p, str);
				GestorSalida.ENVIAR_gITp_INFO_ATACANTES_RECAUDADOR(p, str2);
			}
		}
	}
	
	public void sobrevivio() {
		final String str = "S" + _nombre1 + "," + _nombre2 + "|.|" + _mapa.getID() + "|" + _celda.getID();
		String str2 = _gremio.analizarRecaudadores();
		for (final Personaje pj : _gremio.getMiembros()) {
			if (pj.enLinea()) {
				GestorSalida.ENVIAR_gITM_GREMIO_INFO_RECAUDADOR(pj, str2);
				GestorSalida.ENVIAR_gA_MENSAJE_SOBRE_RECAUDADOR(pj, str);
			}
		}
		setPelea(null);
		GestorSalida.ENVIAR_GM_RECAUDADOR_A_MAPA(_mapa, "+" + stringGM());
	}
	
	public void murio() {
		final String str = "D" + _nombre1 + "," + _nombre2 + "|.|" + _mapa.getID() + "|" + _celda.getID();
		String str2 = _gremio.analizarRecaudadores();
		for (final Personaje pj : _gremio.getMiembros()) {
			if (pj.enLinea()) {
				GestorSalida.ENVIAR_gITM_GREMIO_INFO_RECAUDADOR(pj, str2);
				GestorSalida.ENVIAR_gA_MENSAJE_SOBRE_RECAUDADOR(pj, str);
			}
		}
		borrarRecaudador();
	}
	
	public Celda getCelda() {
		return _celda;
	}
	
	public int getGfxID(boolean buff) {
		return 6000;// recaudador
	}
	
	public int getPDVMax() {
		return (_gremio.getNivel() * 100);
	}
	
	public int getPDV() {
		return getPDVMax();
	}
	
	public TotalStats getTotalStatsPelea() {
		return _totalStats;
	}
	
	public int getNivel() {
		return _gremio.getNivel();
	}
	
	public byte getAlineacion() {
		return Constantes.ALINEACION_NULL;
	}
	
	public String stringGMLuchador() {
		return "-6;6000^100;" + _gremio.getNivel() + ";";
	}
	
	public Map<Integer, StatHechizo> getHechizos() {
		return _gremio.getHechizos();
	}
	
	public int getGradoAlineacion() {
		return 0;
	}
	
	public int getDeshonor() {
		return 0;
	}
	
	public int getHonor() {
		return 0;
	}
	
	public void addHonor(int honor) {}
	
	public boolean addDeshonor(int honor) {
		return false;
	}
	
	@Override
	public void addKamasGanada(long kamas) {}
	
	@Override
	public void addXPGanada(long exp) {}
	
	@Override
	public synchronized void addObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {}
	
	@Override
	public synchronized void remObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {
		if (!_objetos.containsKey(objeto.getID())) {
			return;
		}
		if (cantidad > objeto.getCantidad()) {
			cantidad = objeto.getCantidad();
		}
		final int nuevaCant = objeto.getCantidad() - cantidad;
		if (nuevaCant < 1) {
			perso.addObjIdentAInventario(objeto, true);
			_objetos.remove(objeto.getID());
			GestorSalida.ENVIAR_EsK_MOVER_A_TIENDA_COFRE_BANCO(perso, "O-" + objeto.getID());
		} else {
			Objeto persoObj = objeto.clonarObjeto(cantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
			perso.addObjIdentAInventario(persoObj, true);
			objeto.setCantidad(nuevaCant);
			GestorSalida.ENVIAR_EsK_MOVER_A_TIENDA_COFRE_BANCO(perso, "O+" + objeto.getID() + "|" + objeto.getCantidad() + "|"
			+ objeto.getObjModeloID() + "|" + objeto.convertirStatsAString(false));
		}
		if (_objModeloID.get(objeto.getObjModeloID()) == null) {
			_objModeloID.put(objeto.getObjModeloID(), 0);
		}
		_objModeloID.put(objeto.getObjModeloID(), _objModeloID.get(objeto.getObjModeloID()) + cantidad);
	}
	
	@Override
	public void cerrar(Personaje perso, String exito) {
		String s = _gremio.analizarRecaudadores();
		final StringBuilder str = new StringBuilder();
		str.append(stringPanelInfo(perso));
		str.append(stringRecolecta());
		for (final Personaje miembro : _gremio.getMiembros()) {
			if (miembro.enLinea()) {
				GestorSalida.ENVIAR_gITM_GREMIO_INFO_RECAUDADOR(miembro, s);
				GestorSalida.ENVIAR_gT_PANEL_RECAUDADORES_GREMIO(miembro, 'G', str.toString());
			}
		}
		_gremio.addExperiencia(_exp, false);
		borrarRecaudador();
		perso.cerrarVentanaExchange(exito);
	}
	
	public void botonOK(Personaje perso) {}
	
	public String getArgsDialogo(String args) {
		return ";" + _gremio.getInfoGremio();
	}
	
	public String getInfoPanel() {
		StringBuilder str = new StringBuilder();
		str.append(Integer.toString(_id, 36) + ";");
		str.append(_nombre1 + "," + _nombre2 + ",");
		Personaje dueño = Mundo.getPersonaje(_dueño);
		if (dueño != null) {
			str.append(dueño.getNombre());
		}
		str.append("," + _tiempoCreacion + ",,100000000,100000000");
		str.append(";" + Integer.toString(_mapa.getID(), 36) + "," + _mapa.getX() + "," + _mapa.getY() + ";");
		int estadoR = 0;
		if (_pelea != null) {
			switch (_pelea.getFase()) {
				case Constantes.PELEA_FASE_INICIO :
				case Constantes.PELEA_FASE_POSICION :
					estadoR = 1;
					break;
				case Constantes.PELEA_FASE_COMBATE :
					estadoR = 2;
					break;
			}
		}
		str.append(estadoR + ";");
		if (estadoR == 1) {
			str.append(_pelea.getTiempoFaltInicioPelea() + ";");
		} else {
			str.append("0;");
		}
		str.append((MainServidor.SEGUNDOS_INICIO_PELEA * 1000) + ";");
		str.append((_pelea == null ? 0 : (_pelea.getPosPelea(2) - 1)) + ";");
		return str.toString();
	}
}
