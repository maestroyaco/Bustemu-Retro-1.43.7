package variables.objeto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import variables.encarnacion.Encarnacion;
import variables.encarnacion.EncarnacionModelo;
import variables.hechizo.EfectoHechizo;
import variables.objeto.MascotaModelo.Comida;
import variables.objeto.ObjetoModelo.CAPACIDAD_STATS;
import variables.oficio.Trabajo;
import variables.personaje.Personaje;
import variables.stats.Stats;
import estaticos.Constantes;
import estaticos.MainServidor;
import estaticos.Formulas;
import estaticos.GestorSalida;
import estaticos.Mundo;
import estaticos.Mundo.Duo;

public class Objeto {
	private ObjetoModelo _objModelo;
	private byte _posicion = Constantes.OBJETO_POS_NO_EQUIPADO;
	private int _id, _cantidad, _idObjevivo, _idObjModelo, _durabilidad = -1, _durabilidadMax = -1, _dueñoTemp;
	private int _precio;
	private Stats _statsGeneral = new Stats();
	private ArrayList<EfectoHechizo> _efectosNormales;
	private Encarnacion _encarnacion;
	
	// public void destruir() {
	// try {
	// this.finalize();
	// } catch (Throwable e) {}
	// }
	public Objeto() {
		_cantidad = 1;
		_posicion = Constantes.OBJETO_POS_NO_EQUIPADO;
		_statsGeneral = new Stats();
	}
	
	public Objeto(final int id, final int idObjModelo, final int cant, final byte pos, String strStats,
	final int idObjevi, final int precio) {
		_id = id;
		_objModelo = Mundo.getObjetoModelo(idObjModelo);
		if (_objModelo == null) {
			MainServidor.redactarLogServidorln("La id del objeto " + id + " esta bug porque no tiene objModelo "
			+ idObjModelo);
			return;
		}
		_cantidad = cant;
		_posicion = pos;
		_statsGeneral = new Stats();
		_idObjevivo = idObjevi;
		_idObjModelo = idObjModelo;
		_precio = precio;
		if (MainServidor.IDS_OBJETOS_STATS_MAXIMOS.contains(idObjModelo)) {
			convertirStringAStats(_objModelo.generarStatsModelo(CAPACIDAD_STATS.MAXIMO));
		} else if (MainServidor.IDS_OBJETOS_STATS_MINIMOS.contains(idObjModelo)) {
			convertirStringAStats(_objModelo.generarStatsModelo(CAPACIDAD_STATS.MINIMO));
		} else if (MainServidor.IDS_OBJETOS_STATS_RANDOM.contains(idObjModelo)) {
			convertirStringAStats(_objModelo.generarStatsModelo(CAPACIDAD_STATS.RANDOM));
		} else {
			convertirStringAStats(strStats);
		}
		crearEncarnacion();
	}
	
	public Encarnacion getEncarnacion() {
		return _encarnacion;
	}
	
	public int getDurabilidad() {
		return _durabilidad;
	}
	
	public int getDurabilidadMax() {
		return _durabilidadMax;
	}
	
	public boolean addDurabilidad(final int valor) {
		_durabilidad += valor;
		if (_durabilidad > _durabilidadMax) {
			_durabilidad = _durabilidadMax;
		}
		if (_durabilidad < 1) {
			return true;
		}
		return false;
	}
	
	public boolean puedeTenerStatsIguales() {
		// parece q es para q no se junten los items en 1 solo
		if (_objModelo.getTipo() == Constantes.OBJETO_TIPO_PIEDRA_DE_ALMA_LLENA || _objModelo
		.getTipo() == Constantes.OBJETO_TIPO_MASCOTA || _objModelo.getTipo() == Constantes.OBJETO_TIPO_FANTASMA_MASCOTA
		|| Mundo.getCreaTuItem(_objModelo.getID()) != null || _encarnacion != null) {
			return false;
		}
		return true;
	}
	
	public boolean sonStatsIguales(final Objeto otro) {
		if (_durabilidad != otro.getDurabilidad() || _durabilidadMax != otro.getDurabilidadMax()) {
			return false;
		}
		if (_idObjevivo > 0 || otro.getObjevivoID() > 0) {
			return false;
		}
		if (!_statsGeneral.sonStatsIguales(otro.getStats())) {
			return false;
		}
		if (_efectosNormales == null && otro._efectosNormales == null) {
			// nada
		} else if ((_efectosNormales == null && otro._efectosNormales != null) || (_efectosNormales != null
		&& otro._efectosNormales == null) || (_efectosNormales.isEmpty() && !otro._efectosNormales.isEmpty())
		|| (otro._efectosNormales.isEmpty() && !_efectosNormales.isEmpty())) {
			return false;
		} else if (!_efectosNormales.isEmpty() && !otro._efectosNormales.isEmpty()) {
			ArrayList<String> ePropios = new ArrayList<>();
			ArrayList<String> eOtros = new ArrayList<>();
			for (final EfectoHechizo eh : _efectosNormales) {
				ePropios.add(eh.getEfectoID() + "," + eh.getArgs());
			}
			for (final EfectoHechizo eh : otro._efectosNormales) {
				eOtros.add(eh.getEfectoID() + "," + eh.getArgs());
			}
			for (final EfectoHechizo eh : _efectosNormales) {
				String entry = eh.getEfectoID() + "," + eh.getArgs();
				if (!eOtros.contains(entry)) {
					return false;
				} else {
					eOtros.remove(entry);
				}
			}
			for (final EfectoHechizo eh : otro._efectosNormales) {
				String entry = eh.getEfectoID() + "," + eh.getArgs();
				if (!ePropios.contains(entry)) {
					return false;
				} else {
					ePropios.remove(entry);
				}
			}
		}
		return true;
	}
	
	public int getDueñoTemp() {
		return _dueñoTemp;
	}
	
	public void setDueñoTemp(final int id) {
		_dueñoTemp = id;
	}
	
	public int getPrecio() {
		return _precio;
	}
	
	public void setPrecio(final int precio) {
		_precio = precio;
	}
	
	public int getObjevivoID() {
		return _idObjevivo;
	}
	
	public void setIDObjevivo(final int id) {
		_idObjevivo = id;
	}
	
	public int getObjModeloID() {
		return _idObjModelo;
	}
	
	public void setIDOjbModelo(final int idObjModelo) {
		if (Mundo.getObjetoModelo(idObjModelo) == null) {
			return;
		}
		_idObjModelo = idObjModelo;
		_objModelo = Mundo.getObjetoModelo(idObjModelo);
	}
	
	public ObjetoModelo getObjModelo() {
		return _objModelo;
	}
	
	public Stats getStats() {
		return _statsGeneral;
	}
	
	public int getCantidad() {
		return _cantidad;
	}
	
	public void setCantidad(final int cantidad) {
		_cantidad = cantidad;
	}
	
	public byte getPosicion() {
		return _posicion;
	}
	
	public void setPosicion(final byte newPos) {
		_posicion = newPos;
	}
	
	public void setPosicion(final byte newPos, final Personaje perso, boolean refrescarStuff) {
		if (_posicion == newPos) {
			return;
		}
		final byte oldPos = _posicion;
		_posicion = newPos;
		if (perso != null) {
			perso.cambiarPosObjeto(this, oldPos, newPos, refrescarStuff);
			if (!refrescarStuff && perso.enLinea()) {
				GestorSalida.ENVIAR_OM_MOVER_OBJETO(perso, this);
			}
		}
	}
	
	public int getID() {
		return _id;
	}
	
	public void setID(final int id) {
		_id = id;
	}
	
	public ArrayList<EfectoHechizo> getEfectosNormales() {
		return _efectosNormales;
	}
	
	public ArrayList<EfectoHechizo> getEfectosCriticos() {
		if (_efectosNormales != null) {
			final ArrayList<EfectoHechizo> efectos = new ArrayList<EfectoHechizo>();
			for (final EfectoHechizo EH : _efectosNormales) {
				try {
					if (EH.getEfectoID() == Constantes.STAT_MENOS_PA) {
						efectos.add(EH);
					} else {
						final String[] infos = EH.getArgs().split(",");
						String dados = "";
						int primerValor = Integer.parseInt(infos[0]);
						int segundoValor = Integer.parseInt(infos[1]);
						if (segundoValor <= 0) {
							segundoValor = -1;
							primerValor += _objModelo.getBonusGC();
							dados = "0d0+" + (primerValor);
						} else {
							primerValor += _objModelo.getBonusGC();
							segundoValor += _objModelo.getBonusGC();
							dados = "1d" + (segundoValor - primerValor) + "+" + (primerValor - 1);
						}
						EfectoHechizo eh = new EfectoHechizo(EH.getEfectoID(), primerValor + "," + segundoValor + ",-1,0,0,"
						+ dados, 0, -1, Constantes.getZonaEfectoArma(_objModelo.getTipo()));
						eh.setAfectados(2);
						efectos.add(eh);
					}
				} catch (final Exception e) {}
			}
			return efectos;
		}
		return null;
	}
	
	public int getStatValor(final int statID) {
		return _statsGeneral.getStatParaMostrar(statID);
	}
	
	public void fijarStatValor(final int statID, final int valor) {
		_statsGeneral.fijarStatID(statID, valor);
	}
	
	public void addStatTexto(final int statID, final String texto) {
		_statsGeneral.addStatTexto(statID, texto, false);
	}
	
	public boolean tieneStatTexto(final int statID) {
		return _statsGeneral.tieneStatTexto(statID);
	}
	
	public boolean tieneAlgunStatExo() {
		for (final Entry<Integer, Integer> entry : _statsGeneral.getEntrySet()) {
			if (esStatExo(entry.getKey())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean tieneStatExo(final int statID) {
		if (!esStatExo(statID)) {
			return false;
		}
		return getStatValor(statID) != 0;
	}
	
	public boolean esStatExo(final int statID) {
		if (!Constantes.esStatDePelea(statID)) {
			return false;
		}
		return !_objModelo.tieneStatInicial(statID);// siempre sera positivo
	}
	
	public boolean esStatOver(final int statID, int valor) {
		if (!Constantes.esStatDePelea(statID)) {
			return false;
		}
		Duo<Integer, Integer> duo = _objModelo.getDuoInicial(statID);
		if (duo == null) {
			return false;
		}
		return valor > duo._segundo;
	}
	
	public void convertirStringAStats(final String strStats) {
		_statsGeneral.clear();
		_efectosNormales = null;
		_durabilidad = _durabilidadMax = -1;
		for (String str : strStats.split(",")) {
			if (str.isEmpty()) {
				continue;
			}
			try {
				String[] stats = str.split("#");
				int statID = ObjetoModelo.statSimiliar(Integer.parseInt(stats[0], 16));
				// si no es objevivo y tiene stats entre 970 y 974
				// if (_objModelo.getTipo() != Constantes.OBJETO_TIPO_OBJEVIVO && statID >= 970 && statID
				// <= 974) {
				// continue;
				// }
				// if (_idObjevivo > 0 && (statID == Constantes.STAT_RECIBIDO_EL || statID ==
				// Constantes.STAT_SE_HA_COMIDO_EL)) {
				// continue;
				// }
				if (Constantes.STAT_RECIBIDO_EL == statID) {
					if (stats.length > 1 && stats[1].equals("0")) {
						Calendar actual = Calendar.getInstance();
						str = (stats[0] + "#" + (Integer.toHexString(actual.get(Calendar.YEAR)) + "#" + Integer.toHexString(actual
						.get(Calendar.MONTH) * 100 + actual.get(Calendar.DAY_OF_MONTH)) + "#" + Integer.toHexString(actual.get(
						Calendar.HOUR_OF_DAY) * 100 + actual.get(Calendar.MINUTE))));
					}
				}
				if (Constantes.STAT_RESISTENCIA == statID) {
					_durabilidad = Integer.parseInt(stats[2], 16);
					_durabilidadMax = Integer.parseInt(stats[3], 16);
				} else if (Constantes.esStatHechizo(statID)) {
					_statsGeneral.addStatHechizo(str);
				} else if (Constantes.esStatRepetible(statID)) {
					_statsGeneral.addStatRepetido(str);
				} else if (Constantes.esStatTexto(statID)) {
					_statsGeneral.addStatTexto(statID, str, true);
				} else if (Constantes.esEfectoHechizo(statID)) {
					String dados = "";
					int primerValor = Integer.parseInt(stats[1], 16);
					int segundoValor = Integer.parseInt(stats[2], 16);
					if (segundoValor <= 0) {
						segundoValor = 0;
						dados = "0d0+" + (primerValor);
					} else {
						dados = "1d" + (segundoValor - primerValor) + "+" + (primerValor - 1);
					}
					EfectoHechizo eh = new EfectoHechizo(statID, primerValor + "," + segundoValor + ",-1,0,0," + dados, 0, -1,
					Constantes.getZonaEfectoArma(_objModelo.getTipo()));
					eh.setAfectados(2);
					if (_efectosNormales == null) {
						_efectosNormales = new ArrayList<>();
					}
					_efectosNormales.add(eh);
				} else {
					// int statPositivo = Constantes.getStatPositivoDeNegativo(statID);// +agi
					int valor = Integer.parseInt(stats[1], 16);// 100
					// if (_objModelo.tieneStatInicial(statPositivo)) {//+agi100
					// int cantStatInicial = _objModelo.getStatsIniciales().get(statPositivo)._primero;
					// if (cantStatInicial < 0) {// si es stat inicial negativo
					// // -agi -20
					// int tempValor = valor;
					// if (statPositivo != statID) {
					// tempValor = -tempValor;
					// }
					// if (tempValor > 0) {
					// continue;
					// }
					// }
					// }
					_statsGeneral.addStatID(statID, valor);
				}
			} catch (final Exception e) {
				MainServidor.redactarLogServidorln("BUG OBJETO ID: " + _id + ", OBJMOD: " + _idObjModelo + ", STAT BUG: " + str
				+ ", STATS: " + strStats + ", STATS MODELO: " + _objModelo.getStatsModelo());
				e.printStackTrace();
			}
		}
		if (_efectosNormales != null) {
			_efectosNormales.trimToSize();
		}
		if (_encarnacion != null) {
			_encarnacion.refrescarStatsItem();
		}
	}
	
	private void crearEncarnacion() {
		if (tieneStatTexto(Constantes.STAT_ENCARNACION_NIVEL)) {
			if (_encarnacion == null) {
				int encarID = Integer.parseInt(getParamStatTexto(Constantes.STAT_ENCARNACION_NIVEL, 1), 16);
				int encarExp = Integer.parseInt(getParamStatTexto(Constantes.STAT_ENCARNACION_NIVEL, 2), 16);
				EncarnacionModelo encarModelo = Mundo.getEncarnacionModelo(encarID);
				if (encarModelo != null) {
					_encarnacion = new Encarnacion(this, encarExp, encarModelo);
				}
			}
		}
	}
	
	public String convertirStatsAString(final boolean sinAdicionales) {
		final StringBuilder stats = new StringBuilder();
		if (ObjetoModelo.getTipoConStatsModelo(_objModelo.getTipo())) {
			stats.append(_objModelo.getStatsModelo());
		} else {
			if (_encarnacion != null) {
				addStatTexto(Constantes.STAT_ENCARNACION_NIVEL, Integer.toHexString(_encarnacion.getGfxID()) + "#" + Integer
				.toHexString(_encarnacion.getExp()) + "#" + Integer.toHexString(_encarnacion.getNivel()));
			}
			if (_efectosNormales != null) {
				for (final EfectoHechizo EH : _efectosNormales) {
					if (stats.length() > 0) {
						stats.append(",");
					}
					final String[] infos = EH.getArgs().split(",");
					try {
						stats.append(Integer.toHexString(EH.getEfectoID()) + "#" + Integer.toHexString(Integer.parseInt(infos[0]))
						+ "#" + Integer.toHexString(Integer.parseInt(infos[1])) + "#0#" + infos[5]);
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}
			if (_durabilidadMax > 0 && _durabilidad > 0) {
				if (stats.length() > 0) {
					stats.append(",");
				}
				stats.append(Integer.toHexString(Constantes.STAT_RESISTENCIA) + "#0#" + Integer.toHexString(_durabilidad) + "#"
				+ Integer.toHexString(_durabilidadMax) + "#0d0+" + _durabilidad);
			}
			String oStats = _statsGeneral.getStringStats(this);
			if (!oStats.isEmpty()) {
				if (stats.length() > 0) {
					stats.append(",");
				}
				stats.append(oStats);
			}
			if (!sinAdicionales) {
				if (_idObjevivo > 0 && _idObjevivo != _id) {
					Objeto objevivo = Mundo.getObjeto(_idObjevivo);
					if (objevivo != null) {
						if (stats.length() > 0) {
							stats.append(",");
						}
						stats.append(objevivo.convertirStatsAString(false));
					}
				} else {
					_idObjevivo = 0;
				}
			}
		}
		// fuera de for
		if (_objModelo.getOgrinas() > 0 && Mundo.getCreaTuItem(_objModelo.getID()) == null) {
			if (stats.length() > 0) {
				stats.append(",");
			}
			stats.append(Integer.toHexString(Constantes.STAT_COLOR_NOMBRE_OBJETO) + "#1");
		}
		return stats.toString();
	}
	
	public String strGrupoMob() {
		if (_statsGeneral.getStatRepetidos() == null) {
			return "";
		}
		final StringBuilder stats = new StringBuilder();
		for (String str : _statsGeneral.getStatRepetidos()) {
			try {
				String[] s = str.split("#");
				if (Integer.parseInt(s[0], 16) != Constantes.STAT_INVOCA_MOB && Integer.parseInt(s[0],
				16) != Constantes.STAT_INVOCA_MOB_2) {
					continue;
				}
				if (stats.length() > 0) {
					stats.append(";");
				}
				stats.append(Integer.parseInt(s[3], 16) + "," + Integer.parseInt(s[1], 16) + "," + Integer.parseInt(s[1], 16));
			} catch (Exception e) {}
		}
		return stats.toString();
	}
	
	public String strDarObjetos() {
		if (_statsGeneral.getStatRepetidos() == null) {
			return "";
		}
		final StringBuilder stats = new StringBuilder();
		for (String str : _statsGeneral.getStatRepetidos()) {
			try {
				String[] s = str.split("#");
				if (Integer.parseInt(s[0], 16) != Constantes.STAT_DAR_OBJETO)
					continue;
				if (stats.length() > 0) {
					stats.append(";");
				}
				stats.append(Integer.parseInt(s[1], 16) + "," + Integer.parseInt(s[2], 16));
			} catch (Exception e) {}
		}
		return stats.toString();
	}
	
	public boolean pasoIntercambiableDesde() {
		if (tieneStatTexto(Constantes.STAT_INTERCAMBIABLE_DESDE)) {
			if (getDiferenciaTiempo(Constantes.STAT_INTERCAMBIABLE_DESDE, 60 * 1000) >= 0) {
				addStatTexto(Constantes.STAT_INTERCAMBIABLE_DESDE, "");
			} else {
				return false;
			}
		}
		return true;
	}
	
	public String getParamStatTexto(final int stat, final int parametro) {
		return _statsGeneral.getParamStatTexto(stat, parametro);
	}
	
	public void comerComida(final int idModComida) {
		int nroComidas = 0;
		final MascotaModelo mascModelo = Mundo.getMascotaModelo(_idObjModelo);
		if (mascModelo == null) {
			return;
		}
		try {
			if (tieneStatTexto(Constantes.STAT_NUMERO_COMIDAS)) {
				nroComidas = Integer.parseInt(getParamStatTexto(Constantes.STAT_NUMERO_COMIDAS, 1), 16);
			}
		} catch (final Exception e) {}
		nroComidas++;
		addStatTexto(Constantes.STAT_ULTIMA_COMIDA, "0#0#" + Integer.toHexString(idModComida));
		if (nroComidas == 3) {
			nroComidas = 0;
			final Comida comida = mascModelo.getComida(idModComida);
			if (comida == null) {
				return;
			}
			final int efecto = comida.getIDStat();
			int maximo = 0, maxPorStat = 0;
			for (final Entry<Integer, Integer> entry : _statsGeneral.getEntrySet()) {
				final int statID = entry.getKey();
				byte factor = 1;
				switch (statID) {
					case Constantes.STAT_MAS_RES_PORC_AGUA :
					case Constantes.STAT_MAS_RES_PORC_TIERRA :
					case Constantes.STAT_MAS_RES_PORC_AIRE :
					case Constantes.STAT_MAS_RES_PORC_NEUTRAL :
					case Constantes.STAT_MAS_RES_PORC_FUEGO :
						factor = 6;
						break;
				}
				maximo += entry.getValue() * factor;
				if (statID == efecto) {
					maxPorStat = entry.getValue() * factor;
				}
			}
			if (maximo >= mascModelo.getMaxStats() || maxPorStat >= mascModelo.getStatsPorEfecto(efecto)) {
				// no hace ni mierda
			} else {
				if (efecto == Constantes.STAT_MAS_INICIATIVA || efecto == Constantes.STAT_MAS_PODS) {
					_statsGeneral.addStatID(efecto, 10);
				} else {
					_statsGeneral.addStatID(efecto, 1);
				}
			}
		}
		addStatTexto(Constantes.STAT_NUMERO_COMIDAS, nroComidas + "");
	}
	
	public void comerAlma(final int idMobModelo, final int cantAlmasDevor) {
		final MascotaModelo mascModelo = Mundo.getMascotaModelo(_idObjModelo);
		if (mascModelo == null) {
			return;
		}
		final Comida comida = mascModelo.getComida(idMobModelo);
		if (comida == null) {
			return;
		}
		int valorTemp = 0, index = -1, efecto = comida.getIDStat(), maximo = 0, maxPorStat = 0;
		if (_statsGeneral.getStatRepetidos() != null) {
			for (final String stati : _statsGeneral.getStatRepetidos()) {
				try {
					String[] x = stati.split("#");
					if (Integer.parseInt(x[0], 16) != Constantes.STAT_NOMBRE_MOB) {
						continue;
					}
					int i = Integer.parseInt(x[1], 16);
					int c = Integer.parseInt(x[3], 16);
					if (i == idMobModelo) {
						valorTemp = c;
						index = _statsGeneral.getStatRepetidos().indexOf(stati);
					}
				} catch (Exception e) {}
			}
			if (index > -1) {
				_statsGeneral.getStatRepetidos().remove(index);
			}
		}
		_statsGeneral.addStatRepetido(Integer.toHexString(Constantes.STAT_NOMBRE_MOB) + "#" + Integer.toHexString(
		idMobModelo) + "#0#" + Integer.toHexString(valorTemp + cantAlmasDevor) + "#0");
		for (final Entry<Integer, Integer> entry : _statsGeneral.getEntrySet()) {
			final int statID = entry.getKey();
			byte por = 1;
			switch (statID) {
				case Constantes.STAT_MAS_RES_PORC_TIERRA :
				case Constantes.STAT_MAS_RES_PORC_AGUA :
				case Constantes.STAT_MAS_RES_PORC_AIRE :
				case Constantes.STAT_MAS_RES_PORC_FUEGO :
				case Constantes.STAT_MAS_RES_PORC_NEUTRAL :
					por = 6;
					break;
			}
			maximo += entry.getValue() * por;
			if (statID == efecto) {
				maxPorStat = entry.getValue() * por;
			}
		}
		if (maximo >= mascModelo.getMaxStats() || maxPorStat >= mascModelo.getStatsPorEfecto(efecto)) {
			return;
		}
		if (((valorTemp + cantAlmasDevor) / comida.getCantidad()) > (valorTemp / comida.getCantidad())) {
			if (efecto == Constantes.STAT_MAS_INICIATIVA || efecto == Constantes.STAT_MAS_PODS) {
				_statsGeneral.addStatID(efecto, 10);
			} else {
				_statsGeneral.addStatID(efecto, 1);
			}
		}
	}
	
	public long getDiferenciaTiempo(int stat, int escala) {
		long tiempoActual = Constantes.getTiempoActualEscala(escala);
		long tiempoDif = Constantes.getTiempoDeUnStat(_statsGeneral.getStatTexto(stat), escala);
		return tiempoActual - tiempoDif;
	}
	
	public boolean horaComer(final boolean forzado, int corpulencia) {
		if (forzado || getDiferenciaTiempo(Constantes.STAT_SE_HA_COMIDO_EL, 60
		* 1000) >= MainServidor.MINUTOS_ALIMENTACION_MASCOTA) {
			addStatTexto(Constantes.STAT_SE_HA_COMIDO_EL, ObjetoModelo.getStatSegunFecha(Calendar.getInstance()));
			setCorpulencia(corpulencia);
			return true;
		}
		return false;
	}
	
	public void setCorpulencia(final int numero) {
		switch (numero) {
			case Constantes.CORPULENCIA_OBESO :// obeso
				addStatTexto(Constantes.STAT_CORPULENCIA, "0#7#0");
				break;
			case Constantes.CORPULENCIA_DELGADO :// delgaducho
				addStatTexto(Constantes.STAT_CORPULENCIA, "0#0#7");
				break;
			case Constantes.CORPULENCIA_NORMAL :// normal
				addStatTexto(Constantes.STAT_CORPULENCIA, "0#0#0");
				break;
		}
	}
	
	public int getPDV() {
		if (!tieneStatTexto(Constantes.STAT_PUNTOS_VIDA)) {
			return -1;
		}
		return Integer.parseInt(getParamStatTexto(Constantes.STAT_PUNTOS_VIDA, 3), 16);
	}
	
	public void setPDV(int pdv) {
		addStatTexto(Constantes.STAT_PUNTOS_VIDA, "0#0#" + Integer.toHexString(pdv));
	}
	
	public int getCorpulencia() {
		if (!tieneStatTexto(Constantes.STAT_CORPULENCIA)) {
			return -1;
		}
		if (getParamStatTexto(Constantes.STAT_CORPULENCIA, 3).equals("7")) {
			return Constantes.CORPULENCIA_DELGADO;
		}
		if (getParamStatTexto(Constantes.STAT_CORPULENCIA, 2).equals("7")) {
			return Constantes.CORPULENCIA_OBESO;
		}
		return Constantes.CORPULENCIA_NORMAL;
	}
	
	public boolean esDevoradorAlmas() {
		try {
			return Mundo.getMascotaModelo(_idObjModelo).esDevoradorAlmas();
		} catch (final Exception e) {
			return false;
		}
	}
	
	public int getDañoPromedioNeutral() {
		if (_efectosNormales != null) {
			for (final EfectoHechizo EH : _efectosNormales) {
				try {
					if (EH.getEfectoID() != Constantes.STAT_DAÑOS_NEUTRAL) {
						continue;
					}
					final String[] infos = EH.getArgs().split(",");
					return (Integer.parseInt(infos[1], 16) + Integer.parseInt(infos[0], 16)) / 2;
				} catch (final Exception e) {}
			}
		}
		return 1;
	}
	
	public void forjaMagiaGanar(final int statID, final int potencia) {
		switch (statID) {
			case 96 :
			case 97 :
			case 98 :
			case 99 :
				if (_efectosNormales != null) {
					for (final EfectoHechizo EH : _efectosNormales) {
						if (EH.getEfectoID() != Constantes.STAT_DAÑOS_NEUTRAL) {
							continue;
						}
						final String[] infos = EH.getArgs().split(",");
						try {
							final int min = Integer.parseInt(infos[0]);
							final int max = Integer.parseInt(infos[1]);
							int nuevoMin = (int) (Math.floor((min - 1) * (potencia / 100f)) + 1);// 50 y 78
							final int nuevoMax = (int) (Math.floor((min - 1) * (potencia / 100f)) + Math.floor((max - min + 1)
							* (potencia / 100f)));
							if (MainServidor.MODO_DEBUG) {
								System.out.println("min " + min);
								System.out.println("max " + max);
								System.out.println("nuevoMin " + nuevoMin);
								System.out.println("nuevoMax " + nuevoMax);
							}
							if (nuevoMin == 0) {
								nuevoMin = 1;
							}
							final String nuevosArgs = nuevoMin + "," + nuevoMax + ",-1,0,0," + "1d" + (nuevoMax - nuevoMin + 1) + "+"
							+ (nuevoMin - 1);
							if (MainServidor.MODO_DEBUG) {
								System.out.println("Nuevo Args FM elemental " + nuevosArgs);
							}
							EH.setArgs(nuevosArgs);
							EH.setEfectoID(statID);
							return;
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
				}
				break;
			default :
				_statsGeneral.addStatID(statID, potencia);
				// el metodo se encarga de converti lo negativo en positivo
				break;
		}
	}
	
	public void runasRomperObjeto(Map<Integer, Integer> runas, int cantObjeto) {
		for (int i = 1; i <= cantObjeto; i++) {
			for (final String s : convertirStatsAString(true).split(",")) {
				try {
					if (s.isEmpty()) {
						continue;
					}
					final String[] stats = s.split("#");
					final int statID = Integer.parseInt(stats[0], 16);
					final int statPositivo = Constantes.getStatPositivoDeNegativo(statID);
					int valor = Integer.parseInt(stats[1], 16);
					if (statID != statPositivo) {
						valor = -valor;
					}
					if (valor < 1) {
						continue;
					}
					if (Constantes.getRunaPorStat(statID, 1) == 0) {
						continue;
					}
					float pesoIndividual = Constantes.getPesoStat(statID);
					float pesoStat = pesoIndividual * valor;
					while (pesoStat > 0) {
						int tipoRuna = Constantes.getTipoRuna(statID, pesoStat);
						if (tipoRuna == 0) {
							break;
						}
						int[] v = Constantes.getPotenciaRunaPorStat(statID);
						float jet = Formulas.getRandomDecimal(3);
						int prob = (int) (Constantes.getPorcCrearRuna(statID, pesoStat, tipoRuna, _objModelo.getNivel()));
						tipoRuna--;
						float red = 0;
						if (prob >= jet) {
							int exTipoRuna = tipoRuna;
							red += v[tipoRuna] * pesoIndividual * 3;
							// tipoRuna = Formulas.getRandomInt(0, tipoRuna);//aqui cambia la runa por mas o menos
							int runa = Constantes.getRunaPorStat(statID, tipoRuna + 1);
							// if (!MainServidor.RUNAS_NO_PERMITIDAS.contains(runa)) {
							int cant = (int) Math.pow(2, exTipoRuna - tipoRuna);
							if (runas.get(runa) != null) {
								cant += runas.get(runa);
							}
							runas.put(runa, cant);
							// }
						}
						red += v[tipoRuna] * pesoIndividual;
						red = (float) Math.ceil(red / MainServidor.FACTOR_OBTENER_RUNAS);
						pesoStat -= red;
					}
				} catch (Exception e) {}
			}
		}
	}
	
	public void forjaMagiaPerder(final int statMaguear, final int potencia, boolean afectarStatMagueo) {
		int pozoResidual = getStatValor(Constantes.STAT_POZO_RESIDUAL);
		int pesoRunaRestar = (int) Math.ceil(Constantes.getPesoStat(statMaguear) * potencia);
		if (MainServidor.MODO_DEBUG) {
			System.out.println("------------- PERDIENDO STATS FM -------------------");
			System.out.println("pozoResidual: " + pozoResidual);
			System.out.println("pesoRuna: " + pesoRunaRestar);
		}
		final int pesoOrigRuna = pesoRunaRestar;
		if (pesoRunaRestar > 0) {
			// si sobro peso a restar, se tiene q disminuir
			ArrayList<Integer> statsCheckeados = new ArrayList<>();
			if (!afectarStatMagueo) {
				statsCheckeados.add(statMaguear);
			}
			while (pesoRunaRestar > 0) {
				int statPerder = getStatElegidoAPerder(pesoOrigRuna, statMaguear, statsCheckeados);
				if (MainServidor.MODO_DEBUG) {
					System.out.println("SE ESCOGIO A PERDER STATID " + statPerder);
				}
				if (statPerder == 0) {
					break;
				}
				int overExo = Trabajo.MAGUEO_NORMAL;
				if (statPerder > 2000) {
					statPerder -= 2000;
					overExo = Trabajo.MAGUEO_EXO;
				} else if (statPerder > 1000) {
					statPerder -= 1000;
					overExo = Trabajo.MAGUEO_OVER;
				}
				if (overExo == Trabajo.MAGUEO_NORMAL) {
					if (pozoResidual > 0) {
						pesoRunaRestar -= pozoResidual;// 100 .... 35 - 52 = -17
						pozoResidual = 0;
						continue;
					}
				}
				statsCheckeados.add(statPerder);
				final float pesoRunaPerder = Constantes.getPesoStat(statPerder);
				if (pesoRunaPerder == 0) {
					continue;
				}
				int cantStatPerder = getStatValor(statPerder);
				// lo mas cercano a positivo, minimo es 1
				int cantDebePerder = (int) Math.ceil(pesoRunaRestar / pesoRunaPerder);
				int maxPerder = Math.min(cantStatPerder, cantDebePerder);
				if (MainServidor.MODO_DEBUG) {
					System.out.println("statPerder " + statPerder + " cantStatPerder " + cantStatPerder + " pesoRunaRestar "
					+ pesoRunaRestar + " cantDebePerder " + cantDebePerder + " maxPerder " + maxPerder);
				}
				if (maxPerder <= 0) {
					continue;
				}
				int random = maxPerder;
				switch (overExo) {
					case Trabajo.MAGUEO_OVER :
					case Trabajo.MAGUEO_EXO :
						random = Formulas.getRandomInt(1, cantStatPerder);
						break;
					default :
						if (pesoRunaRestar == 1) {
							random = 1;
						} else {
							random = Formulas.getRandomInt(1, maxPerder);
						}
						break;
				}
				_statsGeneral.addStatID(statPerder, -random);
				int pesoPerder = (int) (random * pesoRunaPerder);
				pesoRunaRestar -= pesoPerder;
			}
		}
		if (pesoRunaRestar > 0) {
			pesoRunaRestar = 0;
		}
		_statsGeneral.fijarStatID(Constantes.STAT_POZO_RESIDUAL, Math.abs(pesoRunaRestar));
	}
	
	private int getStatElegidoAPerder(final int pesoOrigRuna, final int statRuna,
	final ArrayList<Integer> statsCheckeados) {
		final ArrayList<Integer> listaStats = new ArrayList<Integer>();
		for (final Entry<Integer, Integer> entry : _statsGeneral.getEntrySet()) {
			final int statID = entry.getKey();
			int valor = entry.getValue();
			if (MainServidor.MODO_DEBUG) {
				System.out.println("Se intenta perder el stat " + statID + " valor " + valor);
			}
			if (Constantes.getStatPositivoDeNegativo(statID) != statID) {
				// si es negativo no se le borrara
				if (MainServidor.MODO_DEBUG) {
					System.out.println("-- Cancel 1");
				}
				continue;
			}
			if (!Constantes.esStatDePelea(statID)) {
				if (MainServidor.MODO_DEBUG) {
					System.out.println("-- Cancel 2");
				}
				continue;
			}
			if (statsCheckeados.contains(statID)) {
				if (MainServidor.MODO_DEBUG) {
					System.out.println("-- Cancel 3");
				}
				continue;
			}
			if (esStatOver(statID, valor)) {
				// si el stat es OVER
				if (statID != statRuna) {
					return statID + 1000;
				}
			} else if (esStatExo(statID)) {
				// si el stat es EXO retorna primero
				if (statID != statRuna) {
					return statID + 2000;
				}
			}
			listaStats.add(statID);
		}
		while (!listaStats.isEmpty()) {
			// if (listaStats.size() == statsCheckeados.size()) {
			// statsCheckeados.clear();
			// }
			final int statID = listaStats.get(Formulas.getRandomInt(0, listaStats.size() - 1));
			listaStats.remove((Object) statID);
			final float pesoRunaPerder = Constantes.getPesoStat(statID);
			// si es 3 = sabiduria, pero tiene 10 de sab y el otro es 10 agilidad
			if (MainServidor.MODO_DEBUG) {
				System.out.println("-> Escoger statID " + statID + " pesoRuna " + pesoRunaPerder + " pesoOrig " + pesoOrigRuna);
			}
			if (pesoRunaPerder > pesoOrigRuna) {
				final int suerte = Formulas.getRandomInt(1, 101);
				if (suerte <= (pesoOrigRuna * 100 / pesoRunaPerder)) {
					return statID;
				}
			} else {
				return statID;
			}
		}
		return 0;
	}
	
	public String stringObjetoConGuiño() {
		final StringBuilder str = new StringBuilder();
		try {
			str.append(Integer.toHexString(_id) + "~" + Integer.toHexString(_idObjModelo) + "~" + Integer.toHexString(
			_cantidad) + "~" + (_posicion == Constantes.OBJETO_POS_NO_EQUIPADO ? "" : Integer.toHexString(_posicion)) + "~"
			+ convertirStatsAString(false) + "~" + _objModelo.getKamas() / 10);
			str.append(";");
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("OBJETO BUG stringObjetoConGuiño " + _id + " Exception: " + e.toString());
		}
		return str.toString();
	}
	
	public String stringObjetoConPalo(final int cantidad) {
		final StringBuilder str = new StringBuilder();
		try {
			str.append(_id + "|" + cantidad + "|" + _idObjModelo + "|" + convertirStatsAString(false));
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("OBJETO BUG stringObjetoConPalo " + _id + " Exception: " + e.toString());
		}
		return str.toString();
	}
	
	public boolean convertirPerfecto(final int cantMod) {
		return _objModelo.convertirStatsPerfecto(cantMod, _statsGeneral);
	}
	
	public String getStatsModelo() {
		return _objModelo.getStatsModelo();
	}
	
	public synchronized Objeto clonarObjeto(int cantidad, final byte pos) {
		if (cantidad < 1) {
			cantidad = 1;
		}
		return new Objeto(0, _idObjModelo, cantidad, pos, convertirStatsAString(true), 0, 0);
	}
}