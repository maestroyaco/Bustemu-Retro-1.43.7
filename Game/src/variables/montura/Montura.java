package variables.montura;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import sprites.Exchanger;
import variables.mapa.Celda;
import variables.mapa.Cercado;
import variables.mapa.Mapa;
import variables.objeto.Objeto;
import variables.objeto.ObjetoModelo;
import variables.personaje.Personaje;
import variables.stats.Stats;
import estaticos.Camino;
import estaticos.Constantes;
import estaticos.Encriptador;
import estaticos.Formulas;
import estaticos.GestorSalida;
import estaticos.MainServidor;
import estaticos.Mundo;

public class Montura implements Exchanger {
	public static enum Ubicacion {
		ESTABLO, CERCADO, PERGAMINO, EQUIPADA, NULL
	}
	public static byte[] SEXO_POSIBLES = {0, 0, 1};// 0 = macho , 1 = hembra
	private boolean _salvaje;
	private final byte _sexo;
	private byte _orientacion = 1, _talla = 100, _reproducciones;
	private Mapa _mapa;
	private Celda _celda;
	private final int _id, _colorID;
	private int _dueñoID, _nivel = 1, _parejaID = -1, _certificadoID = -1;
	private int _fatiga, _energia, _madurez, _serenidad, _amor, _resistencia;
	private int _semiPod, _maxPod, _maxMadurez, _maxEnergia;
	private long _experiencia, _tiempoInicioDescanso, _tiempoGestacion;
	private Stats _stats = new Stats();
	private String _ancestros = "?,?,?,?,?,?,?,?,?,?,?,?,?,?", _nombre = "Sin Nombre";
	private final Map<Integer, Objeto> _objetos = new TreeMap<Integer, Objeto>();
	private final ArrayList<Byte> _habilidades = new ArrayList<Byte>(2);
	private Ubicacion _ubicacion = Ubicacion.PERGAMINO;// por defecto
	private MonturaModelo _monturaModelo;
	
	public Montura(final int color, final int dueño, boolean castrado, boolean salvaje) {
		_id = Mundo.sigIDMontura();
		_sexo = SEXO_POSIBLES[Formulas.getRandomInt(0, SEXO_POSIBLES.length - 1)];
		_colorID = color;
		_monturaModelo = Mundo.getMonturaModelo(_colorID);
		addExperiencia(Mundo.getExpMontura(MainServidor.INICIO_NIVEL_MONTURA));
		_energia = getMaxEnergia();
		_madurez = getMaxMadurez();
		_dueñoID = dueño;
		if (castrado) {
			castrarPavo();
		}
		_salvaje = salvaje;
		_tiempoGestacion = 0;
		getStatsMontura();
		maximos();
		Mundo.addMontura(this, true);
	}
	
	public Montura(final Montura madre, Montura padre) {
		if (padre == null) {
			padre = madre;
		}
		_id = Mundo.sigIDMontura();
		_sexo = SEXO_POSIBLES[Formulas.getRandomInt(0, SEXO_POSIBLES.length - 1)];
		_colorID = Constantes.getColorCria(madre.getColor(), padre.getColor(), madre._habilidades.contains(
		Constantes.HABILIDAD_PREDISPUESTA), padre._habilidades.contains(Constantes.HABILIDAD_PREDISPUESTA));
		_monturaModelo = Mundo.getMonturaModelo(_colorID);
		addExperiencia(Mundo.getExpMontura(MainServidor.INICIO_NIVEL_MONTURA));
		final String[] papa = padre.getAncestros().split(",");
		final String[] mama = madre.getAncestros().split(",");
		final String primero_papa = papa[0] + "," + papa[1];
		final String primera_mama = mama[0] + "," + mama[1];
		final String segundo_papa = papa[2] + "," + papa[3] + "," + papa[4] + "," + papa[5];
		final String segunda_mama = mama[2] + "," + mama[3] + "," + mama[4] + "," + mama[5];
		_ancestros = padre.getColor() + "," + madre.getColor() + "," + primero_papa + "," + primera_mama + ","
		+ segundo_papa + "," + segunda_mama;
		for (int i = 1; i <= 2; i++) {
			byte habilidad = (byte) Formulas.getRandomInt(1, 20);
			if (habilidad >= 9) {
				continue;
			}
			addHabilidad(habilidad);
		}
		_dueñoID = madre.getDueñoID();
		_talla = 50;
		_salvaje = false;
		_tiempoGestacion = 0;
		getStatsMontura();
		maximos();
		Mundo.addMontura(this, true);
	}
	
	public Montura(final int id, final int color, final byte sexo, final int amor, final int resistencia, final int nivel,
	final long exp, final String nombre, final int fatiga, final int energia, final byte reprod, final int madurez,
	final int serenidad, final String objetos, final String anc, final String habilidad, final byte talla,
	final short celda, final short mapa, final int dueño, final byte orientacion, final long fecundada, final int pareja,
	final byte salvaje) {
		_id = id;
		_colorID = color;
		_monturaModelo = Mundo.getMonturaModelo(color);
		_sexo = sexo;
		addExperiencia(exp);
		_amor = amor;
		_resistencia = resistencia;
		_nombre = nombre;
		_fatiga = fatiga;
		_energia = energia;
		_reproducciones = reprod;
		_madurez = madurez;
		_serenidad = serenidad;
		_ancestros = anc;
		_talla = talla;
		_mapa = Mundo.getMapa(mapa);
		if (_mapa != null) {
			_celda = _mapa.getCelda(celda);
			if (_celda != null) {
				setUbicacion(Ubicacion.CERCADO);
			}
		}
		_dueñoID = dueño;
		_orientacion = orientacion;
		_tiempoGestacion = fecundada;
		_parejaID = pareja;
		_salvaje = salvaje == 1;
		for (final String s : habilidad.split(",")) {
			try {
				if (s.isEmpty()) {
					continue;
				}
				_habilidades.add(Byte.parseByte(s));
			} catch (final Exception e) {}
		}
		objetos.replaceAll(";", ",");
		for (final String str : objetos.split(",")) {
			try {
				if (str.isEmpty()) {
					continue;
				}
				final Objeto obj = Mundo.getObjeto(Integer.parseInt(str));
				if (obj != null) {
					_objetos.put(Integer.parseInt(str), obj);
				}
			} catch (final Exception e) {}
		}
		getStatsMontura();
		maximos();
	}
	
	public final void getStatsMontura() {
		if (_monturaModelo == null) {
			return;
		}
		_stats.clear();
		for (Entry<Integer, Integer> stat : _monturaModelo.getStats().entrySet()) {
			int valor = stat.getValue() * _nivel / MainServidor.NIVEL_MAX_MONTURA;
			if (valor > 0)
				_stats.addStatID(stat.getKey(), valor);
		}
		return;
	}
	
	public final ObjetoModelo getObjModCertificado() {
		if (_monturaModelo == null) {
			return null;
		}
		return Mundo.getObjetoModelo(_monturaModelo.getCertificadoModeloID());
	}
	
	public void setUbicacion(Ubicacion ubicacion) {
		_ubicacion = ubicacion;
		if (_ubicacion == Ubicacion.ESTABLO) {
			_tiempoInicioDescanso = System.currentTimeMillis();
		} else {
			_tiempoInicioDescanso = 0;
		}
	}
	
	public void disminuirFatiga() {
		if (_tiempoInicioDescanso == 0 || _fatiga == 0) {
			return;
		}
		if (System.currentTimeMillis() - _tiempoInicioDescanso >= 60 * 60 * 1000) {
			_tiempoInicioDescanso = System.currentTimeMillis();
			restarFatiga();
		}
	}
	
	public Ubicacion getUbicacion() {
		return _ubicacion;
	}
	
	private void maximos() {
		if (_monturaModelo == null) {
			return;
		}
		int _generacion = _monturaModelo.getGeneracionID();
		_semiPod = ((_generacion + 1) / 2) * 5;
		_maxPod = 50 + (50 * _generacion);
		_maxMadurez = 1000 * _generacion;
		_maxEnergia = 1000 + ((_generacion - 1) * 100);
	}
	
	public void setPergamino(int pergamino) {
		_certificadoID = pergamino;
		if (_certificadoID > 0) {
			setUbicacion(Ubicacion.PERGAMINO);
		}
	}
	
	public int getPergamino() {
		return _certificadoID;
	}
	
	public int getID() {
		return _id;
	}
	
	public int getColor() {
		return _colorID;
	}
	
	public boolean esSalvaje() {
		return _salvaje;
	}
	
	public void setSalvaje(boolean s) {
		_salvaje = s;
	}
	
	public int getSexo() {
		return _sexo;
	}
	
	public int getAmor() {
		return _amor;
	}
	
	public String getAncestros() {
		return _ancestros;
	}
	
	public int getResistencia() {
		return _resistencia;
	}
	
	public int getPods() {
		int pods = 0;
		for (final Objeto obj : _objetos.values()) {
			pods += obj.getObjModelo().getPeso() * obj.getCantidad();
		}
		return pods;
	}
	
	public String getListaExchanger(Personaje perso) {
		final StringBuilder objetos = new StringBuilder();
		for (final Objeto obj : _objetos.values()) {
			objetos.append("O" + obj.stringObjetoConGuiño());
		}
		return objetos.toString();
	}
	
	private Objeto getSimilarObjeto(final Objeto objeto) {
		if (objeto.puedeTenerStatsIguales()) {
			for (final Objeto obj : _objetos.values()) {
				if (Constantes.esPosicionEquipamiento(obj.getPosicion())) {
					continue;
				}
				if (objeto.getID() != obj.getID() && obj.getObjModeloID() == objeto.getObjModeloID() && obj.sonStatsIguales(
				objeto)) {
					return obj;
				}
			}
		}
		return null;
	}
	
	@Override
	public synchronized void addObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {
		if (getPods() >= getTotalPods()) {
			return;
		}
		if (!perso.tieneObjetoID(objeto.getID()) || objeto.getPosicion() != Constantes.OBJETO_POS_NO_EQUIPADO) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "1OBJECT_DONT_EXIST");
			return;
		}
		if (cantidad > objeto.getCantidad()) {
			cantidad = objeto.getCantidad();
		}
		String str = "";
		Objeto objMontura = getSimilarObjeto(objeto);
		final int nuevaCant = objeto.getCantidad() - cantidad;
		if (objMontura == null) {
			if (nuevaCant <= 0) {
				perso.borrarOEliminarConOR(objeto.getID(), false);
				_objetos.put(objeto.getID(), objeto);
				str = "O+" + objeto.getID() + "|" + objeto.getCantidad() + "|" + objeto.getObjModeloID() + "|" + objeto
				.convertirStatsAString(false);
			} else {
				objMontura = objeto.clonarObjeto(cantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
				Mundo.addObjeto(objMontura, false);
				_objetos.put(objMontura.getID(), objMontura);
				objeto.setCantidad(nuevaCant);
				str = "O+" + objMontura.getID() + "|" + objMontura.getCantidad() + "|" + objMontura.getObjModeloID() + "|"
				+ objMontura.convertirStatsAString(false);
				GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(perso, objeto);
			}
		} else {
			if (nuevaCant <= 0) {
				perso.borrarOEliminarConOR(objeto.getID(), true);
				objMontura.setCantidad(objMontura.getCantidad() + objeto.getCantidad());
				str = "O+" + objMontura.getID() + "|" + objMontura.getCantidad() + "|" + objMontura.getObjModeloID() + "|"
				+ objMontura.convertirStatsAString(false);
			} else {
				objeto.setCantidad(nuevaCant);
				objMontura.setCantidad(objMontura.getCantidad() + cantidad);
				str = "O+" + objMontura.getID() + "|" + objMontura.getCantidad() + "|" + objMontura.getObjModeloID() + "|"
				+ objMontura.convertirStatsAString(false);
				GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(perso, objeto);
			}
		}
		GestorSalida.ENVIAR_EsK_MOVER_A_TIENDA_COFRE_BANCO(perso, str);
	}
	
	@Override
	public synchronized void remObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {
		if (!_objetos.containsKey(objeto.getID())) {
			return;
		}
		if (cantidad > objeto.getCantidad()) {
			cantidad = objeto.getCantidad();
		}
		final int nuevaCant = objeto.getCantidad() - cantidad;
		String str;
		if (nuevaCant < 1) {
			_objetos.remove(objeto.getID());
			perso.addObjIdentAInventario(objeto, true);
			str = "O-" + objeto.getID();
		} else {
			Objeto nuevoObj = objeto.clonarObjeto(cantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
			perso.addObjIdentAInventario(nuevoObj, true);
			objeto.setCantidad(nuevaCant);
			str = "O+" + objeto.getID() + "|" + objeto.getCantidad() + "|" + objeto.getObjModeloID() + "|" + objeto
			.convertirStatsAString(false);
		}
		GestorSalida.ENVIAR_EsK_MOVER_A_TIENDA_COFRE_BANCO(perso, str);
	}
	
	public void setDueñoID(final int dueño) {
		_dueñoID = dueño;
	}
	
	public int getDueñoID() {
		return _dueñoID;
	}
	
	public int getNivel() {
		return _nivel;
	}
	
	public long getExp() {
		return _experiencia;
	}
	
	public String getNombre() {
		return _nombre;
	}
	
	public boolean estaCriando() {
		return _celda != null;
	}
	
	public int getFecundadaHaceMinutos() {
		if (esCastrado() || _reproducciones >= 20 || _tiempoGestacion <= 0) {
			_tiempoGestacion = 0;
			return -1;
		}
		int minutos = (int) ((System.currentTimeMillis() - _tiempoGestacion) / (60 * 1000));
		return minutos + 1;
	}
	
	private boolean disponibleParaFecundar() {
		if (esCastrado() || _reproducciones >= 20 || _tiempoGestacion > 0) {
			return false;
		}
		if (_amor >= 7500 && _resistencia >= 7500 && (_salvaje || _nivel >= 5)) {
			return true;
		}
		return false;
	}
	
	public void setMapaCelda(final Mapa mapa, final Celda celda) {
		_mapa = mapa;
		_celda = celda;
	}
	
	public int getFatiga() {
		return _fatiga;
	}
	
	public Mapa getMapa() {
		return _mapa;
	}
	
	public Celda getCelda() {
		return _celda;
	}
	
	public int getTalla() {
		return _talla;
	}
	
	public int getEnergia() {
		return _energia;
	}
	
	public int getReprod() {
		return _reproducciones;
	}
	
	public int getMadurez() {
		return _madurez;
	}
	
	public int getSerenidad() {
		return _serenidad;
	}
	
	public Stats getStats() {
		return _stats;
	}
	
	public Collection<Objeto> getObjetos() {
		return _objetos.values();
	}
	
	public ArrayList<Byte> getCapacidades() {
		return _habilidades;
	}
	
	public void castrarPavo() {
		_reproducciones = -1;
	}
	
	public void quitarCastrado() {
		_reproducciones = 0;
	}
	
	public String strCapacidades() {
		StringBuilder s = new StringBuilder();
		for (byte b : _habilidades) {
			if (s.length() > 0) {
				s.append(",");
			}
			s.append(b);
		}
		return s.toString();
	}
	
	public String detallesMontura() {
		final StringBuilder str = new StringBuilder(_id + ":");
		str.append(_colorID + ":");
		str.append(_ancestros + ":");
		str.append(",," + strCapacidades() + ":");
		str.append(_nombre + ":");
		str.append(_sexo + ":");
		str.append(stringExp() + ":");
		str.append(_nivel + ":");
		str.append((esMontable() ? "1" : "0") + ":");
		str.append(getTotalPods() + ":");
		str.append((_salvaje ? 1 : 0) + ":");// salvaje
		str.append(_resistencia + ",10000:");
		str.append(_madurez + "," + getMaxMadurez() + ":");
		str.append(_energia + "," + getMaxEnergia() + ":");
		str.append(_serenidad + ",-10000,10000:");
		str.append(_amor + ",10000:");
		str.append(getFecundadaHaceMinutos() + ":");
		str.append((disponibleParaFecundar() ? 10 : 0) + ":");
		str.append(convertirStringAStats() + ":");
		str.append(_fatiga + ",240:");
		str.append(_reproducciones + ",20:");
		return str.toString();
	}
	
	private String convertirStringAStats() {
		final StringBuilder stats = new StringBuilder();
		for (final Entry<Integer, Integer> entry : _stats.getEntrySet()) {
			if (stats.length() > 0) {
				stats.append(",");
			}
			stats.append(Integer.toHexString(entry.getKey()) + "#" + Integer.toHexString(entry.getValue()) + "#0#0");
		}
		return stats.toString();
	}
	
	private int getMaxEnergia() {
		return _maxEnergia + (_maxPod / 10) * _nivel;
	}
	
	private int getMaxMadurez() {
		return _maxMadurez;
	}
	
	public int getTotalPods() {// portadora
		return (_habilidades.contains(Constantes.HABILIDAD_PORTADORA) ? 2 : 1) * (_maxPod + (_semiPod * _nivel));
	}
	
	private String stringExp() {
		return _experiencia + "," + Mundo.getExpMontura(_nivel) + "," + Mundo.getExpMontura(_nivel + 1);
	}
	
	public boolean esMontable() {
		if (MainServidor.PARAM_MONTURA_SIEMPRE_MONTABLES) {
			return true;
		}
		if (!MainServidor.PARAM_CRIAR_MONTURA || (_monturaModelo != null && _monturaModelo.getColorID() == 88)) {
			return true;
		}
		if (_salvaje || _energia < 10 || _madurez < getMaxMadurez() || _fatiga >= 240 || (MainServidor.MODO_ANKALIKE
		&& _nivel < 5)) {
			return false;
		}
		return true;
	}
	
	public void setAmor(final int amor) {
		_amor = amor;
	}
	
	public void setResistencia(final int resistencia) {
		_resistencia = resistencia;
	}
	
	public void setMaxMadurez() {
		_madurez = getMaxMadurez();
	}
	
	public void setFatiga(int fatiga) {
		_fatiga = fatiga;
	}
	
	public void setMaxEnergia() {
		_energia = getMaxEnergia();
	}
	
	private void restarFatiga() {
		if (!MainServidor.PARAM_CRIAR_MONTURA) {
			return;
		}
		_fatiga -= 10;
		if (_fatiga < 0) {
			_fatiga = 0;
		}
	}
	
	public void restarAmor(final int amor) {
		if (!MainServidor.PARAM_CRIAR_MONTURA) {
			return;
		}
		_amor -= amor;
		if (_amor < 0) {
			_amor = 0;
		}
	}
	
	public void restarResistencia(final int resistencia) {
		if (!MainServidor.PARAM_CRIAR_MONTURA) {
			return;
		}
		_resistencia -= resistencia;
		if (_resistencia < 0) {
			_resistencia = 0;
		}
	}
	
	private void restarSerenidad() {
		if (!MainServidor.PARAM_CRIAR_MONTURA) {
			return;
		}
		_serenidad -= 100 * MainServidor.RATE_CRIANZA_MONTURA;
		if (_serenidad < -10000) {
			_serenidad = -10000;
		}
	}
	
	private void aumentarMadurez() {
		final int maxMadurez = getMaxMadurez();
		if (_madurez < maxMadurez) {
			_madurez += 100 * MainServidor.RATE_CRIANZA_MONTURA;
			if (_habilidades.contains(Constantes.HABILIDAD_PRECOZ)) {
				_madurez += 100 * MainServidor.RATE_CRIANZA_MONTURA;
			}
			if (_talla < 100) {
				byte talla = _talla;
				if (maxMadurez / _madurez <= 1) {
					_talla = 100;
				} else if (_talla < 75 && maxMadurez / _madurez == 2) {
					_talla = 75;
				} else if (_talla < 50 && maxMadurez / _madurez >= 3) {
					_talla = 50;
				}
				if (talla != _talla)
					GestorSalida.ENVIAR_GM_DRAGOPAVO_A_MAPA(_mapa, "~", this);
			}
		}
		if (_madurez > maxMadurez) {
			_madurez = maxMadurez;
		}
	}
	
	private void aumentarAmor() {// enamorada
		if (!MainServidor.PARAM_CRIAR_MONTURA) {
			return;
		}
		_amor += 100 * MainServidor.RATE_CRIANZA_MONTURA;
		if (_amor > 10000) {
			_amor = 10000;
		}
	}
	
	private void aumentarResistencia() {// resistente
		if (!MainServidor.PARAM_CRIAR_MONTURA) {
			return;
		}
		_resistencia += (_habilidades.contains(Constantes.HABILIDAD_RESISTENTE) ? 2 : 1) * 100
		* MainServidor.RATE_CRIANZA_MONTURA;
		if (_resistencia > 10000) {
			_resistencia = 10000;
		}
	}
	
	private void aumentarFatiga() {// infatigable
		if (!MainServidor.PARAM_CRIAR_MONTURA) {
			return;
		}
		_fatiga += _habilidades.contains(Constantes.HABILIDAD_INFATIGABLE) ? 1 : 2;
		if (_fatiga > 240) {
			_fatiga = 240;
		}
	}
	
	private void aumentarSerenidad() {
		if (!MainServidor.PARAM_CRIAR_MONTURA) {
			return;
		}
		_serenidad += 100 * MainServidor.RATE_CRIANZA_MONTURA;
		if (_serenidad > 10000) {
			_serenidad = 10000;
		}
	}
	
	private void aumentarEnergia() {
		if (!MainServidor.PARAM_CRIAR_MONTURA) {
			return;
		}
		_energia += 10 * MainServidor.RATE_CRIANZA_MONTURA;
		final int maxEnergia = getMaxEnergia();
		if (_energia > maxEnergia) {
			_energia = maxEnergia;
		}
	}
	
	public void aumentarEnergia(final int valor, final int veces) {
		_energia += valor * veces;
		final int maxEnergia = getMaxEnergia();
		if (_energia > maxEnergia) {
			_energia = maxEnergia;
		}
	}
	
	public void energiaPerdida(final int energia) {
		_energia -= energia;
		if (_energia < 0) {
			_energia = 0;
		}
	}
	
	public void aumentarReproduccion() {
		if (esCastrado()) {
			return;
		}
		_reproducciones += 1;
	}
	
	public String stringObjetosBD() {
		final StringBuilder str = new StringBuilder();
		for (final int id : _objetos.keySet()) {
			str.append((str.length() > 0 ? "," : "") + id);
		}
		return str.toString();
	}
	
	public void setNombre(final String nombre) {
		_nombre = nombre;
	}
	
	public void addExperiencia(long exp) {
		if (_habilidades.contains(Constantes.HABILIDAD_SABIA)) {
			exp *= 2;
		}
		int nivel = _nivel;
		_experiencia += exp;
		while (_experiencia >= Mundo.getExpMontura(_nivel + 1) && _nivel < MainServidor.NIVEL_MAX_MONTURA) {
			_nivel++;
		}
		if (nivel != _nivel) {
			getStatsMontura();
		}
	}
	
	public String getStringColor(final String colorDueñoPavo) {
		return _colorID + (_habilidades.contains(Constantes.HABILIDAD_CAMALEON) ? ("," + colorDueñoPavo) : "");
	}
	
	public void addHabilidad(final byte habilidad) {
		if (habilidad >= 1 && habilidad <= 9) {
			_habilidades.add(habilidad);
		}
	}
	
	public int getParejaID() {
		return _parejaID;
	}
	
	private void setParejaID(final int pareja) {
		_parejaID = pareja;
	}
	
	public int getOrientacion() {
		return _orientacion;
	}
	
	public long getTiempoFecundacion() {
		return _tiempoGestacion;
	}
	
	public void setFecundada(boolean b) {
		if (esCastrado() || _sexo == Constantes.SEXO_MASCULINO) {
			_tiempoGestacion = 0;
			return;
		}
		_tiempoGestacion = b ? System.currentTimeMillis() : 0;
	}
	
	public void setFecundada(int minutos) {
		if (_sexo == Constantes.SEXO_MASCULINO) {
			return;
		}
		_tiempoGestacion = System.currentTimeMillis() - (minutos * 60 * 1000);
	}
	
	public boolean esCastrado() {
		return _reproducciones == -1;
	}
	
	public String stringGM() {
		final StringBuilder str = new StringBuilder("");
		if (_celda == null) {
			str.append(_mapa.getCercado().getCeldaMontura());
		} else {
			str.append(_celda.getID());
		}
		str.append(";");
		str.append(_orientacion + ";0;" + _id + ";" + _nombre + ";-9;");
		if (_colorID == 88) {
			str.append(7005);
		} else {
			str.append(7002);
		}
		str.append("^" + _talla + ";");
		try {
			str.append(Mundo.getPersonaje(_dueñoID).getNombre());
		} catch (final Exception e) {
			str.append("Sin Dueño");
		}
		str.append(";" + _nivel + ";" + _colorID);
		return str.toString();
	}
	
	public void moverMontura(final Personaje dueño, final int dir, final int celdasAMover, final boolean alejar) {
		final Cercado cercado = _mapa.getCercado();
		if (_mapa == null || _celda == null || cercado == null) {
			return;
		}
		int direccion;
		final short celdaInicio = _celda.getID();
		if (dir == -1) {
			if (dueño == null || dueño.getCelda().getID() == celdaInicio) {
				return;
			}
			direccion = Camino.direccionEntreDosCeldas(_mapa, celdaInicio, dueño.getCelda().getID(), true);
		} else {
			direccion = dir;
		}
		if (alejar) {
			direccion = Camino.getDireccionOpuesta(direccion);
		}
		char cDir = Camino.getDireccionPorIndex(direccion);
		int accion = 0, celdasMovidas = 0;
		StringBuilder path = new StringBuilder();
		short tempCeldaID = celdaInicio, celdaPrueba = celdaInicio;
		boolean golpeoObjetoCrianza = false;
		for (int i = 0; i < celdasAMover; i++) {
			celdaPrueba = Camino.getSigIDCeldaMismaDir(celdaPrueba, direccion, _mapa, false);
			if (_mapa.getCelda(celdaPrueba) == null) {
				return;
			}
			if (cercado.getObjetosCrianza().containsKey(celdaPrueba)) {
				Objeto objeto = cercado.getObjetosCrianza().get(celdaPrueba);
				if (objeto == null && !cercado.esPublico()) {
					break;
				}
				golpeoObjetoCrianza = true;
				int caract = Constantes.getCaracObjCria(objeto == null
				? Constantes.getObjCriaPorMapa(_mapa.getID())
				: objeto.getObjModeloID());
				switch (caract) {
					case 1 :// abrevadero
						if (_serenidad <= 2000 && _serenidad >= -2000) {
							aumentarMadurez();
						}
						break;
					case 2 :// fulminadora
						if (_serenidad < 0) {
							aumentarResistencia();
						}
						break;
					case 3 :// dragonalga
						if (_serenidad > 0) {
							aumentarAmor();
						}
						break;
					case 4 :// aporreadora
						restarSerenidad();
						break;
					case 5 :// acariciador
						aumentarSerenidad();
						break;
					case 6 :// pesebre
						restarFatiga();
						aumentarEnergia();
						break;
				}
				aumentarFatiga();
				if (!cercado.esPublico()) {
					if (objeto.addDurabilidad(-MainServidor.DURABILIDAD_REDUCIR_OBJETO_CRIA)) {
						if (cercado.retirarObjCria(celdaPrueba, null)) {
							GestorSalida.ENVIAR_GDO_OBJETO_TIRAR_SUELO(_mapa, '-', celdaPrueba, 0, false, "");
						}
					} else {
						GestorSalida.ENVIAR_GDO_OBJETO_TIRAR_SUELO(_mapa, '+', celdaPrueba, objeto.getObjModeloID(), true, objeto
						.getDurabilidad() + ";" + objeto.getDurabilidadMax());
					}
				}
				break;
			}
			if (!_mapa.getCelda(celdaPrueba).esCaminable(false) || cercado.getCeldaPuerta() == celdaPrueba || Camino
			.celdaSalienteLateral(_mapa.getAncho(), _mapa.getAlto(), tempCeldaID, celdaPrueba)) {
				break;
			}
			tempCeldaID = celdaPrueba;
			path.append(cDir + Encriptador.celdaIDAHash(tempCeldaID));
			celdasMovidas++;
		}
		if (tempCeldaID != celdaInicio) {
			GestorSalida.ENVIAR_GA_MOVER_SPRITE_MAPA(_mapa, 0, 1, _id + "", Encriptador.getValorHashPorNumero(_orientacion)
			+ Encriptador.celdaIDAHash(celdaInicio) + path);
			_celda = _mapa.getCelda(tempCeldaID);
			final int azar = Formulas.getRandomInt(1, 10);
			if (azar == 5) {
				accion = 8;
			}
			if (cercado.getCriando().size() > 1) {
				for (final Montura montura : cercado.getCriando().values()) {
					if (puedeFecundar(montura)) {
						accion = 4; // accion de aparearse
						break;
					}
				}
			}
		} else {
			GestorSalida.ENVIAR_eD_CAMBIAR_ORIENTACION(_mapa, _id, Encriptador.getNumeroPorValorHash(cDir));
		}
		if (_ubicacion == Ubicacion.NULL) {
			return;
		}
		_orientacion = Encriptador.getNumeroPorValorHash(cDir);
		try {
			Thread.sleep((celdasMovidas * 250) + 1);
		} catch (final Exception e) {}
		switch (accion) {
			case 4 :// aparearse
				GestorSalida.ENVIAR_eUK_EMOTE_MAPA(_mapa, _id, accion, 0);
				break;
			default :
				GestorSalida.ENVIAR_eUK_EMOTE_MAPA(_mapa, _id, accion, 0);
			case 0 :// no hace nada
				if (golpeoObjetoCrianza) {
					GestorSalida.ENVIAR_GDE_FRAME_OBJECT_EXTERNAL(_mapa, celdaPrueba + ";4");
				}
				break;
		}
	}
	
	private boolean puedeFecundar(Montura montura) {
		if (montura.getID() == _id) {
			return false;
		}
		if (montura.getCelda() != _celda) {// diferente celdas
			return false;
		}
		if (montura.getSexo() == _sexo || !montura.disponibleParaFecundar() || !disponibleParaFecundar() || montura
		.esCastrado() || esCastrado()) {
			return false;
		}
		if (_mapa.getCercado().esPublico() && montura.getDueñoID() != _dueñoID) {
			return false;
		}
		if (montura.getCapacidades().contains(Constantes.HABILIDAD_ENAMORADA) || _habilidades.contains(
		Constantes.HABILIDAD_ENAMORADA) || Formulas.getRandomInt(0, 5) == 2) {
			Montura madre = null;
			Montura padre = null;
			if (_sexo == Constantes.SEXO_FEMENINO) {
				madre = this;
				padre = montura;
			} else if (montura.getSexo() == Constantes.SEXO_FEMENINO) {
				padre = this;
				madre = montura;
			}
			// madre
			madre.setFecundada(true);
			madre.setParejaID(padre.getID());
			// padre
			padre.aumentarReproduccion();
			padre.restarAmor(7500);
			padre.restarResistencia(7500);
			if (padre.pudoEscapar()) {
				GestorSalida.ENVIAR_GM_BORRAR_GM_A_MAPA(_mapa, _id);
			}
			return true;
		}
		return false;
	}
	
	public boolean pudoEscapar() {
		if (esSalvaje()) {
			int prob = Formulas.getRandomInt(1, 100);
			if (prob <= MainServidor.PROBABILIDAD_ESCAPAR_MONTURA_DESPUES_FECUNDAR) {
				Personaje dueñoOtro = Mundo.getPersonaje(getDueñoID());
				if (dueñoOtro != null) {
					if (dueñoOtro.enLinea()) {
						GestorSalida.ENVIAR_Im_INFORMACION(dueñoOtro, "0111; <b>" + getNombre() + "</b>~" + getMapa().getID());
					} else {
						dueñoOtro.getCuenta().addMensaje("0111; <b>" + getNombre() + "</b>~" + getMapa().getID(), true);
					}
				}
				Mundo.eliminarMontura(this);
				return true;
			}
		}
		return false;
	}
	
	public byte velocidadAprendizaje() {
		if (_colorID == 18) {// dragopavo dorado
			return 20;
		}
		if (_monturaModelo == null) {
			return 0;
		}
		switch (_monturaModelo.getGeneracionID()) {
			case 1 :
				return 100;
			case 2 :
			case 3 :
			case 4 :
				return 80;
			case 5 :
			case 6 :
			case 7 :
				return 60;
			case 8 :
			case 9 :
				return 40;
			case 10 :
				return 20;
			default :
				return 100;
		}
	}
	
	public int minutosParir() {
		return MainServidor.MINUTOS_GESTACION_MONTURA + ((_monturaModelo.getGeneracionID() - 1)
		* (MainServidor.MINUTOS_GESTACION_MONTURA / 4)) + ((_reproducciones - 1) * MainServidor.MINUTOS_GESTACION_MONTURA
		/ 8);
	}
	
	@Override
	public void addKamas(long k, Personaje perso) {}
	
	@Override
	public long getKamas() {
		return 0;
	}
	
	@Override
	public void cerrar(Personaje perso, String exito) {
		perso.cerrarVentanaExchange(exito);
	}
	
	public void botonOK(Personaje perso) {}
}
