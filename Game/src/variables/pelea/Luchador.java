package variables.pelea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import sprites.PreLuchador;
import variables.gremio.Recaudador;
import variables.hechizo.Buff;
import variables.hechizo.EfectoHechizo.TipoDaño;
import variables.hechizo.HechizoLanzado;
import variables.hechizo.StatHechizo;
import variables.mapa.Celda;
import variables.mob.MobGrado;
import variables.objeto.Objeto;
import variables.pelea.Reto.EstReto;
import variables.personaje.Personaje;
import variables.stats.Stats;
import variables.stats.TotalStats;
import variables.zotros.Prisma;
import estaticos.Camino;
import estaticos.Constantes;
import estaticos.Formulas;
import estaticos.GestorSalida;
import estaticos.Inteligencia;
import estaticos.MainServidor;
import estaticos.Mundo;
import estaticos.Mundo.Duo;

public class Luchador {
	private boolean _estaMuerto;
	private boolean _estaRetirado;
	private boolean _puedeJugar;
	private boolean _contaminado;
	private boolean _desconectado;
	private boolean _estatico;
	private boolean _sirveParaBuff = true;
	private boolean _esBomba;
	private boolean _esDoble;
	private boolean _idReal = true;
	private boolean _listo;
	private boolean _espectadorAdmin;
	private boolean _esAbonado;
	private boolean _saqueado;
	private boolean _msjMuerto;
	private boolean _updateGTM;
	private char _direccion = 'b';
	private byte _tipoLuch, _equipoBin = -2;
	private byte _turnosParaMorir;
	private byte _turnosRestantes = 20;
	private byte _alineacion = Constantes.ALINEACION_NULL;
	private int _ultimoElementoDaño = Constantes.ELEMENTO_NULO;
	private int _idLuch, _PDVMax, _PDV, _gfxID;
	private int _nroInvocaciones;
	private int _idHechizoLanzado = -1;
	private int _idCeldaInicioTurno;
	private int _luchQueAtacoUltimo;
	private int _prospeccion = 100;
	private int _escudo;
	private int _colorNombre = -1;
	private int _distMinAtq = -1;
	private int _PArestantes, _PMrestantes, _PAusados, _PMusados;
	private final int _viejoNivel;
	private long _kamasGanadas;
	private long _xpGanada;
	private float _bonusAlinExp;
	private float _bonusAlinDrop;
	private final Pelea _pelea;
	private Celda _celda;
	private Luchador _transportandoA, _transportadoPor, _invocador, _muertoPor;
	private PreLuchador _preLuchador;
	private final TotalStats _totalStats;
	private ArrayList<Luchador> _bombas;
	private final ArrayList<HechizoLanzado> _hechizosLanzados = new ArrayList<HechizoLanzado>();
	private final ArrayList<Integer> _hechiLanzadosReto = new ArrayList<Integer>();
	private final ArrayList<Integer> _retoMobsAsesinados = new ArrayList<Integer>();
	private final ArrayList<Integer> _visibles = new ArrayList<Integer>();
	private final CopyOnWriteArrayList<Buff> _buffsPelea = new CopyOnWriteArrayList<Buff>();
	// private final ArrayList<Buff> _buffsCond = new ArrayList<Buff>();
	private final Map<Integer, Integer> _estados = new TreeMap<Integer, Integer>();
	private final Map<Integer, Integer> _bonusCastigo = new TreeMap<Integer, Integer>();
	private StringBuilder _stringBuilderGTM = new StringBuilder();
	private Inteligencia _IA = null;
	private String _nombre;// , _strGMLuchador;
	private Map<Objeto, Boolean> _objDropeados;
	
	public Luchador(final Pelea pelea, final PreLuchador pre, boolean espectador) {
		setPreLuchador(pre);
		_pelea = pelea;
		_idLuch = pre.getID();
		if (pre.getClass() == Personaje.class) {
			_tipoLuch = 1;
			_nombre = ((Personaje) pre).getNombre();
			_colorNombre = ((Personaje) pre).getColorNombre();
			_esAbonado = ((Personaje) pre).esAbonado();
		} else if (pre.getClass() == MobGrado.class) {
			_tipoLuch = 4;
			// final int IA = ((MobGrado) pre).getMobModelo().getTipoIA();
			// if (IA == 0 || IA == 9 || IA == 6) {
			// _sirveParaBuff = true;
			// }
			setInteligenciaArtificial(new Inteligencia(this, _pelea));
			_nombre = ((MobGrado) pre).getIDModelo() + "";
		} else if (pre.getClass() == Recaudador.class) {
			_tipoLuch = 5;
			setInteligenciaArtificial(new Inteligencia(this, _pelea));
			_nombre = ((Recaudador) pre).getN1() + "," + ((Recaudador) pre).getN2();
		} else if (pre.getClass() == Prisma.class) {
			_tipoLuch = 2;
			setInteligenciaArtificial(new Inteligencia(this, _pelea));
			_nombre = (pre.getAlineacion() == Constantes.ALINEACION_BONTARIANO ? 1111 : 1112) + "";
		}
		// _strGMLuchador = pre.stringGMLuchador();
		_viejoNivel = pre.getNivel();
		_totalStats = getPreLuchador().getTotalStatsPelea();
		limpiarStatsBuffs();
		resetPuntos();
		if (espectador) {
			return;
		}
		_alineacion = pre.getAlineacion();
		_PDVMax = pre.getPDVMax();
		_PDV = pre.getPDV();
		_gfxID = pre.getGfxID(false);
	}
	
	public boolean getUpdateGTM() {
		return _updateGTM;
	}
	
	public int getPARestantes() {
		return _PArestantes;
	}
	
	public int getPMRestantes() {
		return _PMrestantes;
	}
	
	public int addPARestantes(int p) {
		int r = p;
		if (r > 0) {
			if (_PArestantes < 0) {
				r += _PArestantes;
			}
		}
		setPARestantes(_PArestantes + p);
		return r;
	}
	
	public int addPMRestantes(int p) {
		int r = p;
		if (r > 0) {
			if (_PMrestantes < 0) {
				r += _PMrestantes;
			}
		}
		setPMRestantes(_PMrestantes + p);
		return r;
	}
	
	public void setUpdateGTM(boolean b) {
		_updateGTM = b;
	}
	
	private void setPARestantes(int p) {
		_PArestantes = p;
		_updateGTM = true;
	}
	
	private void setPMRestantes(int p) {
		int oldPM = _PMrestantes;
		_PMrestantes = p;
		_updateGTM = true;
		if (oldPM != _PMrestantes) {
			if (_IA != null) {
				_IA.forzarRefrescarMov();
			}
		}
	}
	
	public int getPAUsados() {
		return _PAusados;
	}
	
	public int getPMUsados() {
		return _PMusados;
	}
	
	public void addPAUsados(int p) {
		_PAusados += p;
		if (_PAusados < 0) {
			_PAusados = 0;
		}
	}
	
	public void addPMUsados(int p) {
		_PMusados += p;
		if (_PMusados < 0) {
			_PMusados = 0;
		}
	}
	
	public void resetPuntos() {
		final TotalStats statsLuch = getTotalStats();
		setPARestantes(statsLuch.getTotalStatParaMostrar(Constantes.STAT_MAS_PA));
		setPMRestantes(statsLuch.getTotalStatParaMostrar(Constantes.STAT_MAS_PM));
		_PAusados = 0;
		_PMusados = 0;
	}
	
	public boolean getComandoPasarTurno() {
		if (getPersonaje() != null) {
			return getPersonaje().getComandoPasarTurno();
		}
		return false;
	}
	
	public void setSaqueado(boolean b) {
		_saqueado = b;
	}
	
	public boolean fueSaqueado() {
		return _saqueado;
	}
	
	public void setMsjMuerto(boolean b) {
		_msjMuerto = b;
	}
	
	public boolean getMsjMuerto() {
		return _msjMuerto;
	}
	
	private void limpiarStatsBuffs() {
		_totalStats.getStatsBuff().clear();
	}
	
	public int getUltimoElementoDaño() {
		return _ultimoElementoDaño;
	}
	
	public void setUltimoElementoDaño(int elemento) {
		_ultimoElementoDaño = elemento;
	}
	
	public Pelea getPelea() {
		return _pelea;
	}
	
	public void addKamasGanadas(long kamas) {
		setKamasGanadas(getKamasGanadas() + kamas);
		getPreLuchador().addKamasGanada(kamas);
	}
	
	public void addXPGanada(long xp) {
		setExpGanada(getExpGanada() + xp);
		getPreLuchador().addXPGanada(xp);
	}
	
	public boolean esNoIA() {
		return getIA() == null;
	}
	
	public boolean estaListo() {
		return _listo;
	}
	
	public void setListo(final boolean listo) {
		_listo = listo;
	}
	
	public boolean esIDReal() {
		return _idReal;
	}
	
	public boolean esMultiman() {
		if (getPersonaje() == null) {
			return false;
		}
		return getPersonaje().esMultiman();
	}
	
	public void setIDReal(boolean b) {
		_idReal = b;
	}
	
	public boolean esIAChafer() {
		if (getIA() == null) {
			return false;
		}
		if (getIA().getTipoIA() == 11) {
			return true;
		}
		return false;
	}
	
	public PreLuchador getPreLuchador() {
		return _preLuchador;
	}
	
	public String getNombre() {
		return _nombre;
	}
	
	public byte getFlag() {
		switch (_tipoLuch) {
			case 0 :
				return 2;// bandera desafio
			case 5 :
				return 3;// bandera de recaudador
			case 4 :
				return 1;// bandera mobs
			default :
				return 0;// bandera pj
		}
	}
	
	public byte getAlineacion() {
		return _alineacion;
	}
	
	public StringBuilder getStringBuilderGTM() {
		return _stringBuilderGTM;
	}
	
	public void resetStringBuilderGTM() {
		_stringBuilderGTM = new StringBuilder();
	}
	
	public void borrarBomba(final Luchador bomba) {
		_bombas.remove(bomba);
	}
	
	public void addBomba(final Luchador bomba) {
		if (_bombas.size() < 3) {
			_bombas.add(bomba);
		}
	}
	
	public char getDireccion() {
		return _direccion;
	}
	
	public boolean esBomba() {
		return _esBomba;
	}
	
	public void setBomba(final boolean bomba) {
		_esBomba = bomba;
	}
	
	public void addEscudo(final int escudo) {
		_escudo += escudo;
		if (_escudo < 0) {
			_escudo = 0;
		}
	}
	
	public boolean esInvisible(final int idMirador) {
		if (idMirador != 0) {
			if (idMirador == _idLuch || _visibles.contains(idMirador)) {
				return false;
			}
		}
		if (tieneBuff(150)) {
			return true;
		}
		return false;
	}
	
	public void hacerseVisible() {
		for (final Buff buff : _buffsPelea) {
			if (!buff.getCondicionBuff().isEmpty()) {
				continue;
			}
			if (buff.getEfectoID() == 150) {
				removeBuff(buff);
			}
		}
		GestorSalida.ENVIAR_GA_ACCION_PELEA(_pelea, 7, 150, _idLuch + "", _idLuch + ",0");
		GestorSalida.ENVIAR_GIC_APARECER_LUCHADORES_INVISIBLES(_pelea, 7, this);
	}
	
	public void aparecer(final Luchador mostrar) {
		_visibles.add(mostrar.getID());
		GestorSalida.ENVIAR_GA_ACCION_PELEA_LUCHADOR(mostrar, 150, _idLuch + "", _idLuch + ",0");
		GestorSalida.ENVIAR_GIC_APARECER_LUCHADORES_INVISIBLES(mostrar, _idLuch + ";" + _celda.getID());
	}
	
	public void vaciarVisibles() {
		_visibles.clear();
	}
	
	public void aplicarBuffInicioTurno(final Pelea pelea) {
		for (final Buff buff : _buffsPelea) {
			if (!buff.getCondicionBuff().isEmpty()) {
				continue;
			}
			buff.aplicarBuffDeInicioTurno(pelea, this);
		}
	}
	
	public Buff getBuff(final int id) {
		for (final Buff buff : _buffsPelea) {
			if (!buff.getCondicionBuff().isEmpty()) {
				continue;
			}
			if (buff.getEfectoID() == id) {
				return buff;
			}
		}
		return null;
	}
	
	public boolean tieneBuff(final int id) {
		return getBuff(id) != null;
	}
	
	public ArrayList<Buff> getBuffsPorEfectoID(final int efectotID) {
		final ArrayList<Buff> buffs = new ArrayList<>();
		for (final Buff buff : _buffsPelea) {
			if (!buff.getCondicionBuff().isEmpty()) {
				continue;
			}
			if (buff.getEfectoID() == efectotID) {
				buffs.add(buff);
			}
		}
		return buffs;
	}
	
	public Buff getBuffPorHechizoYEfecto(final int hechizoID, final int efectoID) {
		for (final Buff buff : _buffsPelea) {
			if (!buff.getCondicionBuff().isEmpty()) {
				continue;
			}
			if (buff.getHechizoID() == hechizoID && (efectoID == 0 || efectoID == buff.getEfectoID())) {
				return buff;
			}
		}
		return null;
	}
	
	public boolean tieneBuffPorHechizoYEfecto(final int hechizoID, final int efectoID) {
		return getBuffPorHechizoYEfecto(hechizoID, efectoID) != null;
	}
	
	public int getValorPorBuffsID(final int efectoID) {
		int valor = 0;
		for (final Buff buff : _buffsPelea) {
			if (!buff.getCondicionBuff().isEmpty()) {
				continue;
			}
			if (buff.getEfectoID() == efectoID) {
				if (efectoID == 106 || efectoID == 750) {
					// reenvio de hechizo y efecto de captura de almas
					if (buff.getPrimerValor() > valor) {
						valor = buff.getPrimerValor();
					}
				} else {
					valor += buff.getPrimerValor();
				}
			}
		}
		return valor;
	}
	
	public int getValorPorPrimerYEfectoID(final int efectoID, int primerValor) {
		int valor = 0;
		for (final Buff buff : _buffsPelea) {
			if (!buff.getCondicionBuff().isEmpty()) {
				continue;
			}
			if (buff.getEfectoID() == efectoID && buff.getPrimerValor() == primerValor) {
				valor += buff.getSegundoValor();
			}
		}
		return valor;
	}
	
	// la conmbinacion de GTF , GTM y GTS realiza la disminucion de turnos de los buffs
	public void disminuirBuffsPelea() {
		disminuirEstados();
		if (!_buffsPelea.isEmpty()) {
			for (final Buff buff : _buffsPelea) {
				int turnosRestantes = buff.disminuirTurnosRestantes();
				if (turnosRestantes <= -1) {
					continue;
				}
				if (turnosRestantes == 0) {
					switch (buff.getEfectoID()) {
						case Constantes.STAT_MAS_VITALIDAD :
							if (buff.getHechizoID() != 441) {
								continue;
							}
							break;
						case 422 :
							addEscudo(-buff.getPrimerValor());
							break;
						case 150 :// invisibilidad
							hacerseVisible();
							break;
					}
					removeBuff(buff);
				}
			}
			actualizarBuffStats();
			if (getPDVConBuff() <= 0) {
				_pelea.addMuertosReturnFinalizo(this, null);
			}
		}
	}
	
	public void resetearBuffs(final ArrayList<Buff> buffs) {
		addNuevosBuffs(buffs);
		for (final Buff buff : _buffsPelea) {
			if (!buff.getCondicionBuff().isEmpty()) {
				continue;
			}
			GestorSalida.ENVIAR_GA998_AGREGAR_BUFF_PELEA(_pelea, 7, Pelea.getStrParaGA998(buff.getEfectoID(), _idLuch, buff
			.getTurnosRestantes(false), buff.getHechizoID(), buff.getArgs()));
		}
	}
	
	private void addNuevosBuffs(final ArrayList<Buff> buffs) {
		_updateGTM = true;
		_buffsPelea.clear();
		_buffsPelea.addAll(buffs);
		actualizarBuffStats();
	}
	
	private void addBuff(final Buff buff) {
		_updateGTM = true;
		_buffsPelea.add(buff);
		actualizarBuffStats();
	}
	
	private void removeBuff(final Buff buff) {// solo lo usa para quitar invisbilidad
		_updateGTM = true;
		_buffsPelea.remove(buff);
	}
	
	private void actualizarBuffStats() {// refresh buffs, refrescar buffs
		limpiarStatsBuffs();
		for (final Buff buff : _buffsPelea) {
			if (!buff.getCondicionBuff().isEmpty()) {
				continue;
			}
			_totalStats.getStatsBuff().addStatID(buff.getEfectoID(), buff.getPrimerValor());
		}
	}
	
	public boolean paraDeshechizar(int equipoBin) {
		int i = 0;
		for (Buff buff : _buffsPelea) {
			if (!buff.getCondicionBuff().isEmpty()) {
				continue;
			}
			i += Constantes.estimaDaño(buff.getEfectoID());
		}
		if (equipoBin != _equipoBin) {
			i = -i;
		}
		return i > 0;
	}
	
	public Luchador clonarLuchador(final int id) {
		Luchador ret = null;
		if (getPreLuchador().getClass() == Personaje.class) {
			ret = new Luchador(_pelea, Personaje.crearClon(getPersonaje(), id), false);
		} else if (getPreLuchador().getClass() == MobGrado.class) {
			ret = new Luchador(_pelea, (getMob()).getMobGradoModelo().invocarMob(id, true, this), false);
		} else {
			return null;
		}
		ret._esDoble = true;
		ret.setInteligenciaArtificial(new Inteligencia(ret, _pelea));
		return ret;
	}
	
	public Map<Integer, StatHechizo> getHechizos() {
		return getPreLuchador().getHechizos();
	}
	
	public void setLuchQueAtacoUltimo(final int id) {
		_luchQueAtacoUltimo = id;
	}
	
	public int getLuchQueAtacoUltimo() {
		return _luchQueAtacoUltimo;
	}
	
	public void setBonusCastigo(final int bonus, final int stat) {
		getBonusCastigo().put(stat, bonus);
	}
	
	public int getBonusCastigo(final int stat) {
		int bonus = 0;
		if (_bonusCastigo.containsKey(stat)) {
			bonus = _bonusCastigo.get(stat);
		}
		return bonus;
	}
	
	// public int getTipo() {
	// return _tipo;
	// }
	public void setEstatico(final boolean estatico) {
		_estatico = estatico;
	}
	
	public boolean esEstatico() {
		return _estatico;
	}
	
	public void setSirveParaBuff(final boolean apto) {
		_sirveParaBuff = apto;
	}
	
	public boolean getSirveParaBuff() {// FIXME aun no se para q sirve esto
		return _sirveParaBuff;
	}
	
	public ArrayList<HechizoLanzado> getHechizosLanzados() {
		return _hechizosLanzados;
	}
	
	public void actualizaHechizoLanzado() {
		final ArrayList<HechizoLanzado> copia = new ArrayList<HechizoLanzado>();
		copia.addAll(_hechizosLanzados);
		for (final HechizoLanzado HL : copia) {
			HL.actuSigLanzamiento();
			if (HL.getSigLanzamiento() <= 0) {
				_hechizosLanzados.remove((Object) HL);
			}
		}
		copia.clear();
	}
	
	public void addHechizoLanzado(final Luchador lanzador, final StatHechizo hechizo, final Luchador objetivo) {
		_hechizosLanzados.add(new HechizoLanzado(lanzador, hechizo, objetivo == null ? 0 : objetivo.getID()));
	}
	
	public int getID() {
		return _idLuch;
	}
	
	public int getTipoIA() {
		if (getIA() == null)
			return -1;
		return getIA().getTipoIA();
	}
	
	public Luchador getTransportando() {
		return _transportandoA;
	}
	
	public void setTransportando(final Luchador transportado) {
		_transportandoA = transportado;
	}
	
	public Luchador getPortador() {
		return _transportadoPor;
	}
	
	public void setTransportadoPor(final Luchador transportadoPor) {
		_transportadoPor = transportadoPor;
	}
	
	public int getGfxID() {
		return _gfxID;
	}
	
	public void setGfxID(final int gfxID) {
		_gfxID = gfxID;
	}
	
	public Celda getCeldaPelea() {
		return _celda;
	}
	
	public void setCeldaPelea(final Celda celda) {
		_celda = celda;
		if (_celda != null) {
			_celda.addLuchador(this);
		}
		for (Luchador l : _pelea.luchadoresDeEquipo(3)) {
			if (l.getIA() != null) {
				l.getIA().forzarRefrescarMov();
			}
		}
	}
	
	public void setEquipoBin(final byte i) {
		_equipoBin = i;
	}
	
	public boolean estaMuerto() {
		return _estaMuerto;
	}
	
	public void setEstaMuerto(boolean m) {
		_estaMuerto = m;
	}
	
	public void setMuertoPor(Luchador luch) {
		_muertoPor = luch;
	}
	
	public Luchador getMuertoPor() {
		return _muertoPor;
	}
	
	public boolean estaRetirado() {
		return _estaRetirado;
	}
	
	public boolean puedeGolpeCritico(final StatHechizo SH) {// formula de golpes criticos
		int probGC = SH.getProbabilidadGC();
		if (probGC < 2) {
			return false;
		}
		if (tieneBuff(781)) {// mala sombra
			return false;
		}
		final TotalStats statsConBuff = getTotalStats();
		int agilidad = statsConBuff.getTotalStatParaMostrar(Constantes.STAT_MAS_AGILIDAD);
		if (agilidad < 0) {
			agilidad = 0;
		}
		if (SH != null && getPersonaje() != null) {
			if (getPersonaje().tieneModfiSetClase(SH.getHechizoID())) {
				final int modi = getPersonaje().getModifSetClase(SH.getHechizoID(), 287);
				probGC -= modi;
			}
		}
		probGC = (int) ((probGC - statsConBuff.getTotalStatParaMostrar(Constantes.STAT_MAS_GOLPES_CRITICOS)) * (1.1 * Math.E
		/ Math.log(agilidad + 12)));
		if (probGC < 2) {
			probGC = 2;
		}
		final int jet = Formulas.getRandomInt(1, probGC);
		return jet == probGC;
	}
	
	public boolean puedeFalloCritico(final StatHechizo SH) {
		int probFC = SH.getProbabilidadFC();
		if (probFC < 2) {
			return false;
		}
		final TotalStats statsConBuff = getTotalStats();
		probFC = probFC - statsConBuff.getTotalStatParaMostrar(Constantes.STAT_MAS_FALLOS_CRITICOS);
		if (probFC < 2) {
			probFC = 2;
		}
		final int jet = Formulas.getRandomInt(1, probFC);
		return jet == probFC;
	}
	
	public TotalStats getTotalStats() {
		return _totalStats;
	}
	
	public Stats getBaseStats() {
		return _totalStats.getStatsBase();
	}
	
	public Stats getObjetosStats() {
		return _totalStats.getStatsObjetos();
	}
	
	public Stats getBuffsStats() {
		return _totalStats.getStatsBuff();
	}
	
	public CopyOnWriteArrayList<Buff> getBuffsPelea() {
		return _buffsPelea;
	}
	
	public String stringGM(int idMirador) {
	    final StringBuilder str = new StringBuilder();

	    // ID de celda, considerando si el luchador es invisible para el mirador.
	    str.append(idMirador != 0 && esInvisible(idMirador) ? 0 : _celda.getID()).append(";");
	    
	    // Dirección del luchador.
	    str.append(Camino.getIndexPorDireccion(getDireccion())).append(";");
	    
	    // Estrellas de bonus y estado de abonado.
	    str.append("0" + "^" + _esAbonado).append(";");
	    
	    // ID y nombre del luchador con su color.
	    str.append(_idLuch).append(";");
	    str.append(_nombre).append("^").append(_colorNombre).append(";");
	    
	    // Información gráfica del luchador (skin, tamaño, etc.).
	    str.append(getPreLuchador().stringGMLuchador());
	    
	    // Puntos de vida (PDV), Puntos de Acción (PA) y Puntos de Movimiento (PM) actuales.
	    str.append(getPDVConBuff()).append(";");       // Vida actual (LP)
	    str.append(getPARestantes()).append(";");      // PA actuales (AP)
	    str.append(getPMRestantes()).append(";");      // PM actuales (MP)
	    
	    // Cálculo y formato de las resistencias (PVP o PVM).
	    str.append(getResistenciasFormateadas()).append(";");

	    // Equipo del luchador (formato binario).
	    str.append(_equipoBin).append(";");
	    
	    // Apariencia de la montura si el luchador es un personaje y está montando.
	    appendInfoMontura(str);
	    
	    // Estadísticas de Huida y Placaje.
	    str.append(_totalStats.getTotalStatConComplemento(Constantes.STAT_MAS_HUIDA)).append(";");
	    str.append(_totalStats.getTotalStatConComplemento(Constantes.STAT_MAS_PLACAJE)).append(";");
	    str.append(_totalStats.getTotalStatParaMostrar(Constantes.STAT_MAS_PA) + ";");  // APinit  
	    str.append(_totalStats.getTotalStatParaMostrar(Constantes.STAT_MAS_PM));        // MPinit  


	    // Vida máxima (LPmax) al final de la cadena.
	    str.append(getPDVMaxConBuff());

	    return str.toString();
	}

	/**
	 * Determina y formatea la cadena de resistencias según el tipo de pelea (PvP o PvM).
	 * @return Una cadena con las 7 resistencias separadas por comas.
	 */
	private String getResistenciasFormateadas() {
	    boolean esPVP = esPeleaPVP();
	    int[] statsResistencias = {
	        esPVP ? Constantes.STAT_MAS_RES_PORC_PVP_NEUTRAL : Constantes.STAT_MAS_RES_PORC_NEUTRAL,
	        esPVP ? Constantes.STAT_MAS_RES_PORC_PVP_TIERRA : Constantes.STAT_MAS_RES_PORC_TIERRA,
	        esPVP ? Constantes.STAT_MAS_RES_PORC_PVP_FUEGO : Constantes.STAT_MAS_RES_PORC_FUEGO,
	        esPVP ? Constantes.STAT_MAS_RES_PORC_PVP_AGUA : Constantes.STAT_MAS_RES_PORC_AGUA,
	        esPVP ? Constantes.STAT_MAS_RES_PORC_PVP_AIRE : Constantes.STAT_MAS_RES_PORC_AIRE,
	        Constantes.STAT_MAS_ESQUIVA_PERD_PA,
	        Constantes.STAT_MAS_ESQUIVA_PERD_PM
	    };

	    StringBuilder resistStr = new StringBuilder();
	    for (int i = 0; i < statsResistencias.length; i++) {
	        resistStr.append(_totalStats.getTotalStatConComplemento(statsResistencias[i]));
	        if (i < statsResistencias.length - 1) {
	            resistStr.append(",");
	        }
	    }
	    return resistStr.toString();
	}

	/**
	 * Verifica si la pelea actual es de tipo PvP.
	 * @return true si es una pelea PvP, de lo contrario false.
	 */
	private boolean esPeleaPVP() {
	    switch (_pelea.getTipoPelea()) {
	        case Constantes.PELEA_TIPO_DESAFIO:
	        case Constantes.PELEA_TIPO_KOLISEO:
	        case Constantes.PELEA_TIPO_PVP:
	        case Constantes.PELEA_TIPO_RECAUDADOR:
	            return true;
	        default:
	            return false;
	    }
	}

	/**
	 * Añade la información de la montura a la cadena de StringBuilder si es aplicable.
	 * @param str El StringBuilder al que se añadirá la información.
	 */
	private void appendInfoMontura(StringBuilder str) {
	    if (getPreLuchador() instanceof Personaje) {
	        Personaje perso = (Personaje) getPreLuchador();
	        if (perso.estaMontando() && perso.getMontura() != null) {
	            str.append(perso.getMontura().getStringColor(perso.stringColor()));
	        }
	    }
	    str.append(";");
	}
	
	public int getPDVMaxConBuff() {
		return _PDVMax + getBuffsStats().getStatParaMostrar(Constantes.STAT_MAS_VITALIDAD);
	}
	
	public int getPDVConBuff() {
		return _PDV + getBuffsStats().getStatParaMostrar(Constantes.STAT_MAS_VITALIDAD);
	}
	
	public int getPDVMaxSinBuff() {
		return _PDVMax;
	}
	
	public int getPDVSinBuff() {
		return _PDV;
	}
	
	public void restarPDV(int pdv) {
		// positivo = restar vida, negativo = curar}
		if (pdv > 0) {
			if (_escudo > 0) {
				final int escudo = _escudo;
				addEscudo(-pdv);
				pdv -= escudo;
				if (pdv < 0) {
					return;
				}
			}
		}
		setPDV(_PDV - pdv);
		if (pdv > 0) {
			int pdvMax = getPDVMaxSinBuff();
			pdvMax -= Math.floor(pdv * MainServidor.PORCENTAJE_DAÑO_NO_CURABLE / 100);
			if (pdvMax < 1) {
				pdvMax = 1;
			}
			setPDVMAX(pdvMax, false);
			if (_pelea.getRetos() != null && !_esDoble && esNoIA()) {
				for (final Entry<Byte, Reto> entry : _pelea.getRetos().entrySet()) {
					Reto reto = entry.getValue();
					final byte retoID = entry.getKey();
					EstReto exitoReto = reto.getEstado();
					if (exitoReto != Reto.EstReto.EN_ESPERA) {
						continue;
					}
					switch (retoID) {
						case Constantes.RETO_INTOCABLE :
							exitoReto = Reto.EstReto.FALLADO;
							break;
						case Constantes.RETO_CONTAMINACION :
							setContaminado(true);
							break;
					}
					reto.setEstado(exitoReto);
				}
			}
		}
	}
	
	public void setPDV(final int pdv) {
		_PDV = pdv;
		if (_PDV > _PDVMax) {
			_PDV = _PDVMax;
		}
	}
	
	public void setPDVMAX(final int pdvMax, boolean conPorc) {
		int porc = 0;
		if (_PDVMax != 0) {
			porc = _PDV * 100 / _PDVMax;
		}
		boolean max = pdvMax >= _PDVMax;
		_PDVMax = pdvMax;
		if (_PDV > _PDVMax) {
			_PDV = _PDVMax;
		}
		if (!conPorc) {
			return;
		}
		int newPDV = _PDVMax * porc / 100;
		if (max) {
			newPDV = Math.max(_PDV, newPDV);
		} else {
			newPDV = Math.min(_PDV, newPDV);
		}
		setPDV(newPDV);
	}
	
	public float getPorcPDV() {
		int vitalidad = getBuffsStats().getStatParaMostrar(Constantes.STAT_MAS_VITALIDAD);
		if (_PDVMax + vitalidad <= 0) {
			return 0;
		}
		float porc = (_PDV + vitalidad) * 100f / (_PDVMax + vitalidad);
		porc = Math.max(0, porc);
		porc = Math.min(100, porc);
		return porc;
	}
	
	public void setEstado(final int estado, final int turnos) {
		if (!getSirveParaBuff()) {
			return;
		}
		if (turnos != 0) {
			if (_estados.get(estado) != null) {
				if (_estados.get(estado) == -1 || _estados.get(estado) > turnos) {
					// no hace nada, porq es infinito o mayor al actual
					return;
				} else {
					_estados.put(estado, turnos);
				}
			} else {
				_estados.put(estado, turnos);
			}
		} else {
			if (_estados.get(estado) == null) {
				return;
			}
			_estados.remove(estado);
		}
		GestorSalida.ENVIAR_GA950_ACCION_PELEA_ESTADOS(_pelea, 7, _idLuch, estado, turnos != 0);
	}
	
	public boolean tieneEstado(final int id) {
		// return _estados.get(id) != null;
		if (_estados.get(id) == null) {
			return false;
		}
		return _estados.get(id) != 0;
	}
	
	private void disminuirEstados() {
		final Map<Integer, Integer> copia = new TreeMap<Integer, Integer>();
		for (final Entry<Integer, Integer> est : _estados.entrySet()) {
			if (est.getValue() <= 0) {
				copia.put(est.getKey(), est.getValue());
				continue;
			}
			final int nVal = est.getValue() - 1;
			if (nVal == 0) {
				GestorSalida.ENVIAR_GA950_ACCION_PELEA_ESTADOS(_pelea, 7, _idLuch, est.getKey(), false);
				continue;
			}
			copia.put(est.getKey(), nVal);
		}
		_estados.clear();
		_estados.putAll(copia);
	}
	
	public synchronized void deshechizar(Luchador luchador, boolean desbuffTodo) {// desbuffear
		// if idLanzador es 0, deshechiza normal
		if (!_buffsPelea.isEmpty()) {
			boolean tiene = false;
			final ArrayList<Buff> nuevosBuffs = new ArrayList<>();
			for (final Buff buff : _buffsPelea) {
				if (!buff.esDesbufeable()) {
					nuevosBuffs.add(buff);
					continue;
				}
				if (!buff.getCondicionBuff().isEmpty()) {
					continue;
				}
				if (!desbuffTodo) {
					if (luchador != null && buff.getLanzador().getID() != luchador.getID()) {
						nuevosBuffs.add(buff);
						continue;
					}
				}
				tiene = true;
				int valor = buff.getPrimerValor();
				switch (buff.getEfectoID()) {
					case 111 :// + PA
					case 120 :
						valor = addPARestantes(-valor);
						if (valor < 0) {
							GestorSalida.ENVIAR_GA_ACCION_PELEA(_pelea, 7, Constantes.STAT_MAS_PA, _idLuch + "", _idLuch + ","
							+ valor);
						}
						break;
					case 101 :// - PA
					case 168 :
					case 84 :
						valor = addPARestantes(valor);
						if (valor > 0) {
							GestorSalida.ENVIAR_GA_ACCION_PELEA(_pelea, 7, Constantes.STAT_MAS_PA, _idLuch + "", _idLuch + ","
							+ valor);
						}
						break;
					case 78 :// + PM
					case 128 :
						valor = addPMRestantes(-valor);
						if (valor < 0) {
							GestorSalida.ENVIAR_GA_ACCION_PELEA(_pelea, 7, Constantes.STAT_MAS_PM, _idLuch + "", _idLuch + ","
							+ valor);
						}
						break;
					case 127 :// - PM
					case 169 :
					case 77 :
						valor = addPMRestantes(valor);
						if (valor > 0) {
							GestorSalida.ENVIAR_GA_ACCION_PELEA(_pelea, 7, Constantes.STAT_MAS_PM, _idLuch + "", _idLuch + ","
							+ valor);
						}
						break;
					case 422 :
						addEscudo(-buff.getPrimerValor());
						break;
					case 150 :// invisibilidad
						hacerseVisible();
						break;
				}
			}
			// acaba el for
			if (!desbuffTodo && luchador != null) {
				if (!tiene) {
					return;
				} else {
					GestorSalida.ENVIAR_GIe_QUITAR_BUFF(_pelea, 7, _idLuch);
				}
			}
			resetearBuffs(nuevosBuffs);
			if (getPDVConBuff() <= 0) {
				_pelea.addMuertosReturnFinalizo(this, luchador);
			} else if (puedeJugar() && !estaRetirado() && getPersonaje() != null) {
				GestorSalida.ENVIAR_As_STATS_DEL_PJ(getPersonaje());
			}
		}
	}
	
	public Duo<Boolean, Buff> addBuffConGIE(int efectoID, final int valor, final int turnosRestantes, final int hechizoID,
	String args, final Luchador lanzador, final boolean conGIE, TipoDaño tipo, String condicion) {
		// se usa para todos menos los de la clase buff porq tienen condicional
		return addBuffConGIE(efectoID, valor, turnosRestantes, hechizoID, args, lanzador, conGIE, tipo, condicion, true);
	}
	
	private Duo<Boolean, Buff> addBuffConGIE(int efectoID, final int valor, final int turnosRestantes,
	final int hechizoID, String args, final Luchador lanzador, final boolean conGIE, TipoDaño tipo,
	String condicionHechizo, boolean inicioBuff) {
		Buff buff = null;
		boolean variosGIE = false;
		if (getSirveParaBuff()) {
			variosGIE = true;
			boolean desbufeable = true;
			int tempTurnos = turnosRestantes;
			if (inicioBuff && (tipo != TipoDaño.TRAMPA && puedeJugar())) {
				variosGIE = false;
				tempTurnos++;
			}
			if (tempTurnos == 0) {
				switch (efectoID) {
					case 81 :// Cura, PDV devueltos
					case 108 :// Cura, PDV devueltos
					case 82 :// Robar Vida(fijo)
					case 90 :// Dona % de su vida
					case 275 :// Daï¿½os Agua %vida del atacante
					case 276 :// Daï¿½os Tierra %vida del atacante
					case 277 :// Daï¿½os Aire %vida del atacante
					case 278 :// Daï¿½os Fuego %vida del atacante
					case 279 :// Daï¿½os Neutral %vida del atacante
					case 85 :// Daï¿½os Agua %vida del atacante
					case 86 :// Daï¿½os Tierra %vida del atacante
					case 87 :// Daï¿½os Aire %vida del atacante
					case 88 :// Daï¿½os Fuego %vida del atacante
					case 89 :// Daï¿½os Neutral %vida del atacante
					case 91 :// Robar Vida(agua)
					case 92 :// Robar Vida(tierra)
					case 93 :// Robar Vida(aire)
					case 94 :// Robar Vida(fuego)
					case 95 :// Robar Vida(neutral)
					case 96 :// Daï¿½os Agua
					case 97 :// Daï¿½os Tierra
					case 98 :// Daï¿½os Aire
					case 99 :// Daï¿½os Fuego
					case 100 :// Daï¿½os Neutral
						break;
					default :
						variosGIE = false;
						tempTurnos = 1;
						break;
				}
			}
			// tempTurnos no es para GIE
			switch (efectoID) {
				case 293 :// aumenta los daï¿½os del hechizo X
				case 294 :
				case 788 :// estado de los castigos de sacrogito
				case Constantes.STAT_MENOS_PORC_PDV_TEMPORAL :
				case Constantes.STAT_DAR_ESTADO :
					desbufeable = false;
					break;
			}
			switch (hechizoID) {
				case 413 : // captura de almas
				case 414 : // domesticacion de montura
				case 421 : // dolor compartido
					// case 431 : // castigo osado
					// case 433 : // castigo forzado
					// case 437 : // castigo agil
					// case 443 : // castigo espiritual
				case 108 : // espiritu felino
					desbufeable = false;
					break;
			}
			boolean estado = false;
			if (condicionHechizo.isEmpty()) {
				switch (efectoID) {
					case Constantes.STAT_MAS_PM :
					case Constantes.STAT_MAS_PM_2 :
						addPMRestantes(valor);
						break;
					case Constantes.STAT_MAS_PA :
					case Constantes.STAT_MAS_PA_2 :
						addPARestantes(valor);
						break;
					case Constantes.STAT_MENOS_PM_FIJO :
					case Constantes.STAT_MENOS_PM :
						addPMRestantes(-valor);
						break;
					case Constantes.STAT_MENOS_PA_FIJO :
					case Constantes.STAT_MENOS_PA :
						addPARestantes(-valor);
						break;
					case Constantes.STAT_QUITAR_ESTADO :
						tempTurnos = 0;
					case Constantes.STAT_DAR_ESTADO :
						setEstado(valor, tempTurnos);
						estado = true;
						break;
				}
			}
			if (!estado) {
				buff = new Buff(efectoID == 424 ? 153 : efectoID, hechizoID, desbufeable, tempTurnos, lanzador, args, tipo);
				addBuff(buff);
			}
			if (!condicionHechizo.isEmpty()) {
				if (buff != null) {
					buff.setCondBuff(condicionHechizo);
				}
			} else if (conGIE || !variosGIE) {
				GestorSalida.ENVIAR_GA998_AGREGAR_BUFF_PELEA(_pelea, 7, Pelea.getStrParaGA998(efectoID, _idLuch, tempTurnos,
				hechizoID, args));
			}
		}
		return new Duo<Boolean, Buff>(variosGIE, buff);
	}
	
	public boolean esDoble() {
		return _esDoble;
	}
	
	public int getNivel() {
		return getPreLuchador().getNivel();
	}
	
	public int getNivelViejo() {
		return _viejoNivel;
	}
	
	public int getNivelAlineacion() {
		return getPreLuchador().getGradoAlineacion();
	}
	
	public String xpStringLuch(final String str) {
		if (getPreLuchador().getClass() == Personaje.class) {
			return ((Personaje) getPreLuchador()).stringExperiencia(str);
		}
		return "0" + str + "0" + str + "0";
	}
	
	void addKamasLuchador() {
		if (esInvocacion()) {
			try {
				getInvocador().getPersonaje().addKamas(getKamasGanadas(), false, false);
			} catch (Exception e) {}
		} else if (getPersonaje() != null) {
			getPersonaje().addKamas(getKamasGanadas(), false, false);
		} else if (getRecaudador() != null) {
			// nada
		} else if (getMob() != null) {
			if (getEquipoBin() == 1) {
				if (_pelea.getMobGrupo() != null) {
					_pelea.getMobGrupo().addKamasHeroico(getKamasGanadas());
					_pelea.setSalvarMobHeroico(true);
				}
			}
		}
	}
	
	void addObjetoAInventario(Objeto obj) {
		if (esInvocacion()) {
			try {
				getInvocador().getPersonaje().addObjDropPelea(obj, true);
			} catch (Exception e) {}
		} else if (getPersonaje() != null) {
			getPersonaje().addObjDropPelea(obj, true);
		} else if (getRecaudador() != null) {
			getRecaudador().addObjAInventario(obj);
		} else if (getMob() != null) {
			if (getEquipoBin() == 1) {
				if (_pelea.getMobGrupo() != null) {
					_pelea.getMobGrupo().addObjAInventario(obj);
					_pelea.setSalvarMobHeroico(true);
				}
			}
		}
	}
	
	void addDropLuchador(Objeto objeto, boolean addInventario) {
		if (_objDropeados == null) {
			_objDropeados = (new HashMap<Objeto, Boolean>());
		}
		// tipo piedra de alma y mascota
		if (objeto.puedeTenerStatsIguales()) {
			for (Objeto obj : _objDropeados.keySet()) {
				if (obj == null) {
					continue;
				}
				if (Constantes.esPosicionEquipamiento(obj.getPosicion())) {
					continue;
				}
				if (obj.getObjModeloID() == objeto.getObjModeloID() && obj.sonStatsIguales(objeto)) {
					obj.setCantidad(obj.getCantidad() + objeto.getCantidad());
					if (objeto.getID() > 0) {
						Mundo.eliminarObjeto(objeto.getID());
					}
					return;
				}
			}
		}
		_objDropeados.put(objeto, addInventario);
	}
	
	public Personaje getPersonaje() {
		if (_esDoble) {
			return null;
		}
		if (getPreLuchador().getClass() == Personaje.class) {
			return (Personaje) getPreLuchador();
		}
		return null;
	}
	
	public MobGrado getMob() {
		if (getPreLuchador().getClass() == MobGrado.class) {
			return (MobGrado) getPreLuchador();
		}
		return null;
	}
	
	public Recaudador getRecaudador() {
		if (getPreLuchador().getClass() == Recaudador.class) {
			return (Recaudador) getPreLuchador();
		}
		return null;
	}
	
	public Prisma getPrisma() {
		if (getPreLuchador().getClass() == Prisma.class) {
			return (Prisma) getPreLuchador();
		}
		return null;
	}
	
	public byte getEquipoBin() {
		// 0 = agresor
		// 1 = agredido
		return _equipoBin;
	}
	
	public byte getParamEquipoAliado() {
		return _pelea.getParamMiEquipo(_idLuch);
	}
	
	public byte getParamEquipoEnemigo() {
		return _pelea.getParamEquipoEnemigo(_idLuch);
	}
	
	public boolean puedeJugar() {
		return _puedeJugar;
	}
	
	public void setInvocador(final Luchador invocador) {
		_invocador = invocador;
	}
	
	public Luchador getInvocador() {
		return _invocador;
	}
	
	public boolean esInvocacion() {
		return _invocador != null && !_pelea.esLuchInicioPelea(this);
	}
	
	public void addNroInvocaciones(int add) {
		_nroInvocaciones += add;
	}
	
	public int getNroInvocaciones() {
		return _nroInvocaciones;
	}
	
	public void fullPDV() {
		_PDV = _PDVMax;
	}
	
	public void setDireccion(char _direccion) {
		this._direccion = _direccion;
	}
	
	public byte getTurnosRestantes() {
		return _turnosRestantes;
	}
	
	public void setTurnosRestantes(int _turnosRestantes) {
		this._turnosRestantes = (byte) _turnosRestantes;
	}
	
	public boolean estaDesconectado() {
		return _desconectado;
	}
	
	public void setDesconectado(boolean _desconectado) {
		this._desconectado = _desconectado;
	}
	
	public boolean esEspectadorAdmin() {
		return _espectadorAdmin;
	}
	
	public void setEspectadorAdmin(boolean _espectadorAdmin) {
		this._espectadorAdmin = _espectadorAdmin;
	}
	
	public void setPreLuchador(PreLuchador _preLuchador) {
		this._preLuchador = _preLuchador;
	}
	
	public void setPuedeJugar(boolean _puedeJugar) {
		this._puedeJugar = _puedeJugar;
	}
	
	public int getDistMinAtq() {
		return _distMinAtq;
	}
	
	public void setDistMinAtq(int _distMinAtq) {
		this._distMinAtq = _distMinAtq;
	}
	
	public Inteligencia getIA() {
		return _IA;
	}
	
	public void setInteligenciaArtificial(Inteligencia _IA) {
		this._IA = _IA;
	}
	
	public byte getTurnosParaMorir() {
		return _turnosParaMorir;
	}
	
	public void addTurnosParaMorir() {
		this._turnosParaMorir++;
	}
	
	public boolean estaContaminado() {
		return _contaminado;
	}
	
	public void setContaminado(boolean _contaminado) {
		this._contaminado = _contaminado;
	}
	
	public Map<Integer, Integer> getBonusCastigo() {
		return _bonusCastigo;
	}
	
	public ArrayList<Integer> getHechizosLanzadosReto() {
		return _hechiLanzadosReto;
	}
	
	public int getIDCeldaInicioTurno() {
		return _idCeldaInicioTurno;
	}
	
	public void setIDCeldaInicioTurno(int _idCeldaIniTurnoReto) {
		this._idCeldaInicioTurno = _idCeldaIniTurnoReto;
	}
	
	public ArrayList<Integer> getMobsAsesinadosReto() {
		return _retoMobsAsesinados;
	}
	
	public int getIDHechizoLanzado() {
		return _idHechizoLanzado;
	}
	
	public void setIDHechizoLanzado(int _idHechiLanzReto) {
		this._idHechizoLanzado = _idHechiLanzReto;
	}
	
	public long getExpGanada() {
		return _xpGanada;
	}
	
	public void setExpGanada(long _expGanada) {
		if (_expGanada <= 0) {
			_expGanada = 0;
		}
		this._xpGanada = _expGanada;
	}
	
	public long getKamasGanadas() {
		return _kamasGanadas;
	}
	
	public void setKamasGanadas(long _kamasGanadas) {
		if (_kamasGanadas <= 0) {
			_kamasGanadas = 0;
		}
		this._kamasGanadas = _kamasGanadas;
	}
	
	public void setEstaRetirado(boolean b) {
		_estaRetirado = b;
	}
	
	public Map<Objeto, Boolean> getObjDropeados() {
		return _objDropeados;
	}
	
	public float getBonusAlinDrop() {
		return _bonusAlinDrop;
	}
	
	public void setBonusAlinDrop(float _bonusAlineacion) {
		this._bonusAlinDrop = _bonusAlineacion;
	}
	
	public float getBonusAlinExp() {
		return _bonusAlinExp;
	}
	
	public void setBonusAlinExp(float _bonusAlineacion) {
		this._bonusAlinExp = _bonusAlineacion;
	}
	
	public int getProspeccionLuchador() {
		return _prospeccion;
	}
	
	public void setProspeccion(int _prospeccion) {
		this._prospeccion = _prospeccion;
	}
}