package variables.objeto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import variables.hechizo.EfectoHechizo;
import variables.hechizo.StatHechizo;
import variables.personaje.Personaje;
import variables.stats.Stats;
import variables.zotros.Accion;
import estaticos.Constantes;
import estaticos.Formulas;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.MainServidor;
import estaticos.Mundo.Duo;

public class ObjetoModelo {
	public enum CAPACIDAD_STATS {
		RANDOM, MINIMO, MAXIMO
	};
	private boolean _esDosManos, _forjaMagueable, _nivelModifi, _etereo;
	private byte _bonusGC, _costePA;
	private short _tipo, _peso, _nivel, _probGC;
	private int _idModelo, _kamas, _ogrinas, _vendidos, _diasIntercambio, _precioPanelOgrinas, _precioPanelKamas, _setID,
	_gfx;
	private long _precioMedio;
	private StatHechizo _statHechizo;
	private String _condiciones, _statsModelo, _nombre;
	private final Map<Integer, Accion> _accionesDeUso = new TreeMap<Integer, Accion>();
	private final ArrayList<EfectoHechizo> _efectosModelo = new ArrayList<EfectoHechizo>();
	private final Map<Integer, Duo<Integer, Integer>> _statsIniciales = new TreeMap<Integer, Duo<Integer, Integer>>();
	private Duo<Integer, Integer> _itemPago;
	private ArrayList<Integer> _mobsQueDropean = new ArrayList<>();
	
	public ObjetoModelo(final int id, final String statsModelo, final String nombre, final short tipo, final short nivel,
	final short peso, final int kamas, final String condiciones, final String infoArma, final int vendidos,
	final long precioMedio, final int ogrinas, final boolean fm, final int gfx, final boolean nivelModifi,
	final boolean etereo, final int diasIntercambio, final int panelOgrinas, final int panelKamas,
	final String itemPago) {
		_nivelModifi = nivelModifi;
		_idModelo = id;
		_gfx = gfx;
		_nombre = nombre;
		_tipo = tipo;
		_nivel = nivel;
		_peso = peso;
		_condiciones = condiciones;
		_vendidos = vendidos;
		_precioMedio = precioMedio;
		_kamas = kamas;
		_ogrinas = ogrinas;
		_forjaMagueable = fm;
		_etereo = etereo;
		_diasIntercambio = diasIntercambio;
		_precioPanelOgrinas = panelOgrinas;
		_precioPanelKamas = panelKamas;
		setStatsModelo(statsModelo);
		if (!_statsModelo.isEmpty()) {
			try {
				if (!infoArma.isEmpty()) {
					final String[] infos = infoArma.split(",");
					_bonusGC = Byte.parseByte(infos[0]);
					_costePA = Byte.parseByte(infos[1]);
					byte minAlc = Byte.parseByte(infos[2]);
					byte maxAlc = Byte.parseByte(infos[3]);
					_probGC = Short.parseShort(infos[4]);
					short porcFC = Short.parseShort(infos[5]);
					boolean lanzarLinea = infos[6].equalsIgnoreCase("true");
					boolean lineaVista = infos[7].equalsIgnoreCase("true");
					_esDosManos = infos[8].equalsIgnoreCase("true");
					_statHechizo = new StatHechizo(0, 1, _costePA, minAlc, maxAlc, _probGC, porcFC, lanzarLinea, lineaVista,
					false, false, (byte) 0, (byte) 0, (byte) 0, (byte) 0, true, "[18, 19, 1, 3, 41, 42]", "", (byte) -1, false);
				}
			} catch (final Exception e) {
				MainServidor.redactarLogServidorln("Objeto Modelo " + _idModelo + " tiene bug en infosArma");
				e.printStackTrace();
				System.exit(1);
			}
		}
		if (!itemPago.isEmpty()) {
			try {
				int idItemPago = Integer.parseInt(itemPago.split(",")[0]);
				int cantItemPago = Integer.parseInt(itemPago.split(",")[1]);
				_itemPago = new Duo<Integer, Integer>(idItemPago, cantItemPago);
			} catch (Exception e) {}
		}
	}
	
	public void addMobQueDropea(int id) {
		if (!_mobsQueDropean.contains(id)) {
			_mobsQueDropean.add(id);
		}
	}
	
	public ArrayList<Integer> getMobsQueDropean() {
		return _mobsQueDropean;
	}
	
	public void delMobQueDropea(int id) {
		_mobsQueDropean.remove((Object) id);
	}
	
	public int getPrecioPanelOgrinas() {
		return _precioPanelOgrinas;
	}
	
	public int getPrecioPanelKamas() {
		return _precioPanelKamas;
	}
	
	public boolean esEtereo() {
		return _etereo;
	}
	
	public byte getCostePA() {
		return _costePA;
	}
	
	public byte getBonusGC() {
		return _bonusGC;
	}
	
	public short getProbabilidadGC() {
		return _probGC;
	}
	
	public StatHechizo getStatHechizo() {
		return _statHechizo;
	}
	
	public boolean getNivelModifi() {
		return _nivelModifi;
	}
	
	public void setNivel(final short nivel) {
		_nivel = nivel;
		_nivelModifi = true;
		GestorSQL.UPDATE_NIVEL_OBJMODELO(_idModelo, nivel);
	}
	
	public int getGFX() {
		return _gfx;
	}
	
	public void setGFX(final int gfx) {
		_gfx = gfx;
		GestorSQL.UPDATE_GFX_OBJMODELO(_idModelo, _gfx);
	}
	
	public static int statSimiliar(int statID) {
		switch (statID) {
			case Constantes.STAT_MAS_PA_2 :
				return Constantes.STAT_MAS_PA;
			case Constantes.STAT_MAS_DAÑOS_2 :
				return Constantes.STAT_MAS_DAÑOS;
			case Constantes.STAT_MAS_PM_2 :
				return Constantes.STAT_MAS_PM;
			case Constantes.STAT_DAÑOS_DEVUELTOS :
				return Constantes.STAT_REENVIA_DAÑOS;
		}
		return statID;
	}
	
	public void setStatsModelo(final String nuevosStats) {
		_statsModelo = nuevosStats;
		_statsIniciales.clear();
		if (_statsModelo.isEmpty()) {
			return;
		}
		for (final String stat : _statsModelo.split(",")) {
			if (stat.isEmpty()) {
				continue;
			}
			final String[] stats = stat.split("#");
			int statID = Integer.parseInt(stats[0], 16);
			if (statID != statSimiliar(statID)) {
				// aqui convierte los stats raros a stats normales, para q trabaje bien
				statID = statSimiliar(statID);
				_statsModelo = _statsModelo.replaceFirst(stat, stat.replaceFirst(stats[0], Integer.toHexString(statID)));
			}
			boolean esEfecto = false;
			for (final int a : Constantes.BUFF_ARMAS) {
				if (a == statID) {
					// HECHIZO ID = 0 (3 param)
					EfectoHechizo eh = new EfectoHechizo(statID, stats[1] + "," + stats[2] + ",-1,0,0," + stats[4], 0, -1,
					Constantes.getZonaEfectoArma(_tipo));
					eh.setAfectados(2);
					_efectosModelo.add(eh);
					esEfecto = true;
					break;
				}
			}
			if (esEfecto) {
				continue;
			}
			int statPositivo = Constantes.getStatPositivoDeNegativo(statID);
			if (Constantes.esStatDePelea(statPositivo)) {
				// filtra si es statDePelea
				int min = Integer.parseInt(stats[1], 16);
				int max = Integer.parseInt(stats[2], 16);
				if (max <= 0) {
					max = min;
				}
				if (statPositivo != statID) {
					// es negativo
					min = -min;
					max = -max;
				}
				Duo<Integer, Integer> duo = new Duo<Integer, Integer>(Math.min(min, max), Math.max(min, max));
				_statsIniciales.put(statPositivo, duo);
			}
		}
	}
	
	public Map<Integer, Duo<Integer, Integer>> getStatsIniciales() {
		return _statsIniciales;
	}
	
	public Duo<Integer, Integer> getDuoInicial(int statID) {
		return _statsIniciales.get(statID);
	}
	
	public boolean tieneStatInicial(int statID) {
		return _statsIniciales.get(statID) != null;
	}
	
	public void addAccion(final Accion accion) {
		_accionesDeUso.put(accion.getID(), accion);
	}
	
	public int cantAcciones() {
		return _accionesDeUso.size();
	}
	
	public void borrarAcciones() {
		_accionesDeUso.clear();
	}
	
	public boolean esForjaMagueable() {
		return _forjaMagueable;
	}
	
	public boolean esDosManos() {
		return _esDosManos;
	}
	
	public int getOgrinas() {
		return _ogrinas;
	}
	
	public int getID() {
		return _idModelo;
	}
	
	public Duo<Integer, Integer> getItemPago() {
		return _itemPago;
	}
	
	public String stringStatsModelo() {
		return _statsModelo;
	}
	
	public String getNombre() {
		return _nombre;
	}
	
	public short getTipo() {
		return _tipo;
	}
	
	public short getNivel() {
		return _nivel;
	}
	
	public short getPeso() {
		return _peso;
	}
	
	public int getKamas() {
		return _kamas;
	}
	
	public void setOgrinas(final int ogrinas) {
		_ogrinas = ogrinas;
		GestorSQL.UPDATE_PRECIO_OBJETO_MODELO(_idModelo, ogrinas, true);
	}
	
	public void setKamas(final int kamas) {
		_kamas = kamas;
		GestorSQL.UPDATE_PRECIO_OBJETO_MODELO(_idModelo, kamas, false);
	}
	
	public int getSetID() {
		return _setID;
	}
	
	void setSetID(int id) {
		_setID = id;
	}
	
	public String getCondiciones() {
		return _condiciones;
	}
	
	public String getStatsModelo() {
		return _statsModelo;
	}
	
	public String stringDeStatsParaTienda() {
		StringBuilder str = new StringBuilder();
		str.append(_idModelo + ";" + _statsModelo);
		if (_itemPago != null) {
			// no pasa nada
		} else if (_ogrinas > 0) {
			if (_statsModelo.length() > 0) {
				str.append(",");
			}
			str.append(Integer.toHexString(Constantes.STAT_COLOR_NOMBRE_OBJETO) + "#1");
		}
		str.append(";");
		if (_itemPago != null) {
			str.append(_itemPago._segundo + ";" + _itemPago._primero);
		} else if (_ogrinas > 0) {
			str.append(_ogrinas);
		} else {
			str.append(_kamas);
		}
		return str.toString();
	}
	
	public void aplicarAccion(final Personaje perso, final Personaje objetivo, final int objID, final short celda) {
		boolean b = false;
		for (final Accion accion : _accionesDeUso.values()) {
			if (accion.realizarAccion(perso, objetivo, objID, celda))
				b = true;
		}
		if (b) {
			perso.restarCantObjOEliminar(objID, 1, true);
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "022;" + 1 + "~" + _idModelo);
		}
	}
	
	public void nuevoPrecio(final int cantidad, final long precio) {
		final int viejaVenta = _vendidos;
		_vendidos += cantidad;
		_precioMedio = (_precioMedio * viejaVenta + precio) / _vendidos;
		GestorSQL.UPDATE_PRECIO_MEDIO_OBJETO_MODELO(this);
	}
	
	public long getPrecioPromedio() {
		return _precioMedio;
	}
	
	public int getVendidos() {
		return _vendidos;
	}
	
	public Objeto crearObjeto(int cantidad, byte pos, CAPACIDAD_STATS capStats) {
		// capStats => 0 = random, 1 = maximo, 2 = minimio
		if (cantidad < 1) {
			cantidad = 1;
		}
		final StringBuilder stats = new StringBuilder();
		// Calendar actual = Calendar.getInstance();
		// if (_ogrinas > 0) {
		// stats.append("325#" + Integer.toHexString(actual.get(1)) + "#"
		// + Integer.toHexString(actual.get(2) * 100 + actual.get(5)) + "#"
		// + Integer.toHexString(actual.get(11) * 100 + actual.get(12)) + ",");
		// }
		// stats.append("3d7#" + Integer.toHexString((actual.get(2) + 3) / 12 + actual.get(1)) + "#"
		// + Integer.toHexString(((actual.get(2) + 3) % 12) * 100 + actual.get(5)) + "#"
		// + Integer.toHexString(actual.get(11) * 100 + (actual.get(12)));
		if (_diasIntercambio > 0) {
			stats.append(Integer.toHexString(Constantes.STAT_INTERCAMBIABLE_DESDE) + "#" + stringFechaIntercambiable(
			_diasIntercambio));
		}
		if (_tipo == Constantes.OBJETO_TIPO_OBJETO_MUTACION) {// objeto de mutacion
			pos = Constantes.OBJETO_POS_OBJ_MUTACION;
		} else if (_tipo == Constantes.OBJETO_TIPO_ALIMENTO_BOOST) {// alimento boost
			pos = Constantes.OBJETO_POS_BOOST;
		} else if (_tipo == Constantes.OBJETO_TIPO_BENDICION) {// maldicion
			pos = Constantes.OBJETO_POS_MALDICION;
		} else if (_tipo == Constantes.OBJETO_TIPO_MALDICION) {// bendicion
			pos = Constantes.OBJETO_POS_BENDICION;
		} else if (_tipo == Constantes.OBJETO_TIPO_ROLEPLAY_BUFF) {// role play
			pos = Constantes.OBJETO_POS_ROLEPLAY;
		} else if (_tipo == Constantes.OBJETO_TIPO_PJ_SEGUIDOR) {// personaje seguidor
			pos = Constantes.OBJETO_POS_PJ_SEGUIDOR;
		}
		if (_tipo == Constantes.OBJETO_TIPO_MASCOTA && MainServidor.PARAM_ALIMENTAR_MASCOTAS) {// mascotas
			if (stats.length() > 0) {
				stats.append(",");
			}
			stats.append("320#0#0#a");
			if (capStats == CAPACIDAD_STATS.MAXIMO) {// maximo stats
				if (stats.length() > 0) {
					stats.append(",");
				}
				stats.append(generarStatsModelo(capStats));
			}
		} else if (_tipo == Constantes.OBJETO_TIPO_CERTIFICADO_DE_LA_PETRERA
		|| _tipo == Constantes.OBJETO_TIPO_CERTIFICADO_DE_MONTURA) {// certificados
			// nada
		} else if (getTipoConStatsModelo(_tipo)) {
			// pocima, perga exp, pan, golosina, pescado, carne
			if (stats.length() > 0) {
				stats.append(",");
			}
			stats.append(_statsModelo);
		} else {
			if (stats.length() > 0) {
				stats.append(",");
			}
			stats.append(generarStatsModelo(capStats));
		}
		return new Objeto(0, _idModelo, cantidad, pos, stats.toString(), 0, 0);
	}
	
	public boolean convertirStatsPerfecto(int cantMod, final Stats stats) {
		try {
			final Map<Integer, Integer> tempStats = new TreeMap<Integer, Integer>();
			for (Entry<Integer, Duo<Integer, Integer>> entry : _statsIniciales.entrySet()) {
				int statID = entry.getKey();
				int valor = entry.getValue()._segundo;
				if (stats.getStatParaMostrar(statID) < valor) {
					tempStats.put(statID, valor);
				}
			}
			if (tempStats.isEmpty()) {
				return false;
			}
			for (int x = 1; x <= cantMod; x++) {
				if (tempStats.isEmpty()) {
					break;
				}
				final int i = (int) tempStats.keySet().toArray()[Formulas.getRandomInt(0, tempStats.size() - 1)];
				stats.fijarStatID(i, tempStats.get(i));
				tempStats.remove(i);
			}
			return true;
		} catch (final Exception e) {}
		return false;
	}
	
	public String generarStatsModelo(final CAPACIDAD_STATS capStats) {
		final StringBuilder statsObjeto = new StringBuilder();
		for (final String s : _statsModelo.split(",")) {
			try {
				if (s.isEmpty()) {
					continue;
				}
				final String[] stats = s.split("#");
				final int statID = Integer.parseInt(stats[0], 16);
				if (statsObjeto.length() > 0) {
					statsObjeto.append(",");
				}
				if (Constantes.STAT_RECIBIDO_EL == statID) {
					Calendar actual = Calendar.getInstance();
					statsObjeto.append(stats[0] + "#" + (Integer.toHexString(actual.get(Calendar.YEAR)) + "#" + Integer
					.toHexString(actual.get(Calendar.MONTH) * 100 + actual.get(Calendar.DAY_OF_MONTH)) + "#" + Integer
					.toHexString(actual.get(Calendar.HOUR_OF_DAY) * 100 + actual.get(Calendar.MINUTE))));
					continue;
				}
				if (Constantes.esStatRepetible(statID) || Constantes.esStatTexto(statID) || Constantes.esStatHechizo(statID)
				|| statID == Constantes.STAT_RESISTENCIA) {
					statsObjeto.append(s);
					continue;
				}
				if (statID == Constantes.STAT_TURNOS || statID == Constantes.STAT_PUNTOS_VIDA) {
					statsObjeto.append(stats[0] + "#0#0#" + stats[3]);
					continue;
				}
				boolean esEfecto = false;
				for (final int a : Constantes.BUFF_ARMAS) {
					if (a == statID) {
						statsObjeto.append(stats[0] + "#" + stats[1] + "#" + stats[2] + "#0#" + stats[4]);
						esEfecto = true;
						break;
					}
				}
				if (esEfecto) {
					continue;
				}
				boolean esNegativo = Constantes.getStatPositivoDeNegativo(statID) != statID;
				int valor = 1, min = -1, max = -1;
				try {
					try {
						min = Integer.parseInt(stats[1], 16);
					} catch (final Exception e) {}
					try {
						max = Integer.parseInt(stats[2], 16);
					} catch (final Exception e) {}
					if (max <= 0) {
						max = min;
					}
					if (capStats == CAPACIDAD_STATS.MAXIMO) {
						// stas maximos
						if (esNegativo) {
							valor = Math.min(min, max);
						} else {
							valor = Math.max(min, max);
						}
					} else if (capStats == CAPACIDAD_STATS.MINIMO) {
						// stats minimos
						if (esNegativo) {
							valor = Math.max(min, max);
						} else {
							valor = Math.min(min, max);
						}
					} else {
						// random
						valor = Formulas.getRandomInt(min, max);
					}
					if (valor < 0) {
						valor = 0;
					}
				} catch (final Exception e) {}
				statsObjeto.append(stats[0] + "#" + Integer.toHexString(valor) + "#0#" + stats[3] + "#0d0+" + valor);
			} catch (final Exception e) {}
		}
		return statsObjeto.toString();
	}
	
	public static String stringFechaIntercambiable(int dias) {
		Calendar actual = Calendar.getInstance();
		actual.add(Calendar.DAY_OF_YEAR, dias);
		return getStatSegunFecha(actual);
	}
	
	public static String getStatSegunFecha(Calendar actual) {
		int año = actual.get(Calendar.YEAR);
		int mes = actual.get(Calendar.MONTH);
		int dia_del_mes = actual.get(Calendar.DAY_OF_MONTH);
		int hora_del_dia = actual.get(Calendar.HOUR_OF_DAY);
		int minuto_de_hora = actual.get(Calendar.MINUTE);
		return (Integer.toHexString(año) + "#" + Integer.toHexString(mes * 100 + dia_del_mes) + "#" + Integer.toHexString(
		hora_del_dia * 100 + minuto_de_hora));
	}
	
	public static boolean getTipoConStatsModelo(int tipo) {
		switch (tipo) {
			case Constantes.OBJETO_TIPO_POCION :
			case Constantes.OBJETO_TIPO_PERGAMINO_EXP :
			case Constantes.OBJETO_TIPO_PAN :
			case Constantes.OBJETO_TIPO_GOLOSINA :
			case Constantes.OBJETO_TIPO_PESCADO_COMESTIBLE :
			case Constantes.OBJETO_TIPO_PIEDRA_DEL_ALMA :
			case Constantes.OBJETO_TIPO_CARNE_COMESTIBLE :
			case Constantes.OBJETO_TIPO_OBJETO_CRIA :
				return true;
		}
		return false;
	}
}
