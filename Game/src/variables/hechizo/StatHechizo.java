package variables.hechizo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;
import variables.mapa.Mapa;
import variables.pelea.Luchador;
import estaticos.Constantes;
import estaticos.Inteligencia.Accion;
import estaticos.MainServidor;
import estaticos.Formulas;
import estaticos.Mundo;

// private final ArrayList<Integer> _afectados = new ArrayList<Integer>();
public class StatHechizo {
	private final int _grado;// nivel
	private final byte _costePA;// coste de PA
	private final byte _minAlc;// minimo alcance
	private final byte _maxAlc;// maximo alcance
	private final short _probGC;// probabilidad de golpe critico
	private final short _probFC;// probabilidad de fallo critico
	private final boolean _lanzarLinea;// lanzar en linea
	private final boolean _lineaDeVista;// linea de vuelo
	private final boolean _necesarioCeldaLibre;// celda vacia
	private final boolean _alcModificable;// alcance modificalble
	private final boolean _necesitaObjetivo;// alcance modificalble
	private final byte _maxLanzPorTurno;// cantidad de veces por turno
	private final byte _maxLanzPorObjetivo;// cantidad de veces por objetivo
	private final byte _sigLanzamiento;// cantidad de turnos para volver a lanzar el hechizo
	private final int _nivelRequerido;// nivel requerido
	private final boolean _esFinTurnoSiFC;// si falla, es final del turno
	private final ArrayList<EfectoHechizo> _efectosNormales = new ArrayList<EfectoHechizo>();
	private final ArrayList<EfectoHechizo> _efectosCriticos = new ArrayList<EfectoHechizo>();
	private final ArrayList<EfectoHechizo> _ordenadoNormales = new ArrayList<EfectoHechizo>();
	private final ArrayList<EfectoHechizo> _ordenadoCriticos = new ArrayList<EfectoHechizo>();
	// private final String _areaEfecto;// genera un estado, tipo portador
	private final ArrayList<Integer> _estadosProhibidos = new ArrayList<Integer>();
	private final ArrayList<Integer> _estadosNecesarios = new ArrayList<Integer>();
	private final Hechizo _hechizo;
	private final byte _tipoHechizo;// 0 normal, 1 pergamino, 2 invocacion, 3 dominios, 4 de clase, 5
																	// de recaudador
	private boolean _trampa, _intercambioPos, _soloMover, _automaticoAlFinalTurno;
	
	public StatHechizo(final int hechizoID, final int grado, final byte costePA, final byte minAlc, final byte maxAlc,
	final short probGC, final short probFC, final boolean lanzarLinea, final boolean lineaDeVista, boolean esCeldaVacia,
	final boolean esModifAlc, final byte maxLanzPorTurno, final byte maxLanzPorObjetivo, final byte sigLanzamiento,
	final int reqLevel, final boolean esFinTurnoSiFC, final String estadosProhibidos, final String estadosNecesarios,
	final byte tipoHechizo, final boolean necesitaObjetivo) {
		_grado = grado;// nivel
		_costePA = costePA;// coste de PA
		_minAlc = minAlc;// minimo alcance
		_maxAlc = maxAlc;// maximo alcance
		_probGC = probGC;// tasa/probabilidad de golpe critico
		_probFC = probFC;// tasa/probabilidad de fallo critico
		_lanzarLinea = lanzarLinea;// lanzado en linea
		_lineaDeVista = lineaDeVista;// linea de vuelo
		if (_necesitaObjetivo = necesitaObjetivo) {
			esCeldaVacia = false;
		}
		_necesarioCeldaLibre = esCeldaVacia;// celda libre
		_alcModificable = esModifAlc;// alcance modificable
		_maxLanzPorTurno = maxLanzPorTurno;// cantidad de veces por turno
		_maxLanzPorObjetivo = maxLanzPorObjetivo;// cantidad de veces por objetivo
		_sigLanzamiento = sigLanzamiento;// cantidad de turnos para volver a lanzar el hechizo
		_nivelRequerido = reqLevel;// nivel requerido
		_esFinTurnoSiFC = esFinTurnoSiFC;// si es fallo critico , final de turno
		_tipoHechizo = tipoHechizo;
		_hechizo = Mundo.getHechizo(hechizoID);
		if (!estadosProhibidos.isEmpty()) {
			final ArrayList<String> estados = Constantes.convertirStringArray(estadosProhibidos);
			for (final String esta : estados) {
				if (esta.isEmpty()) {
					continue;
				}
				_estadosProhibidos.add(Integer.parseInt(esta));
			}
		}
		if (!estadosNecesarios.isEmpty()) {
			final ArrayList<String> estados = Constantes.convertirStringArray(estadosNecesarios);
			for (final String esta : estados) {
				if (esta.isEmpty()) {
					continue;
				}
				_estadosNecesarios.add(Integer.parseInt(esta));
			}
		}
	}
	
	// public boolean esHechizoParaAliados() {
	// return _hechizo.getValorIA() != 2;
	// }
	//
	// public boolean esHechizoParaEnemigos() {
	// return _hechizo.getValorIA() != 1;
	// }
	public int filtroValorIA(Accion tipo, char c) {
		int valorIA = _hechizo.getValorIA();
		if (valorIA == 0) {
			return 0;
		}
		int v = 0;
		switch (tipo) {
			case ATACAR :
				v = 1;
				break;
			case BOOSTEAR :
				v = 2;
				break;
			case CURAR :
				v = 3;
				break;
			case TRAMPEAR :
				v = 4;
				break;
			case INVOCAR :
				v = 5;
				break;
			case TELEPORTAR :
				v = 6;
				break;
			case NADA :
				v = 0;
				break;
		}
		if (Math.abs(valorIA) != v) {
			return -1;
		}
		if (c != ' ') {
			if (c == '+' && valorIA < 0) {
				return -1;
			} else if (c == '-' && valorIA > 0) {
				return -1;
			}
		}
		return 1;
	}
	
	public boolean esTrampa() {
		return _trampa;
	}
	
	public boolean esSoloMover() {
		return _soloMover;
	}
	
	public boolean esIntercambioPos() {
		return _intercambioPos;
	}
	
	public boolean esAutomaticoAlFinalTurno() {
		return _automaticoAlFinalTurno;
	}
	
	private void fijarEfectos(int efectoID) {
		if (efectoID == 8) {
			_intercambioPos = true;
		}
		if (efectoID == 5 || efectoID == 6) {
			_soloMover = true;
		} else {
			_soloMover = false;
		}
		if (efectoID == 400) {
			_trampa = true;
		}
		if (efectoID == 300) {
			_automaticoAlFinalTurno = true;
		}
	}
	
	void analizarEfectos(final String efectosN, String efectosC, String zonaEfecto, int hechizoID) {
		int num = 0;
		ArrayList<String> splt = Constantes.convertirStringArray(efectosN);
		for (String a : splt) {
			try {
				if (a.equals("null") || a.isEmpty()) {
					continue;
				}
				a = a.replace('[', ' ').replace(']', ' ').replace(" ", "");
				int efectoID = Integer.parseInt(a.split(",")[0]);
				String args = a.split(",", 2)[1];
				EfectoHechizo eh = new EfectoHechizo(efectoID, args, hechizoID, _grado, zonaEfecto.substring(num * 2, num * 2
				+ 2));
				_efectosNormales.add(eh);
				fijarEfectos(efectoID);
				num++;
			} catch (final Exception e) {
				MainServidor.redactarLogServidorln("[BUG HECHIZO ID] " + hechizoID + " : " + efectosN);
				e.printStackTrace();
				System.exit(1);
				return;
			}
		}
		_efectosNormales.trimToSize();
		splt = Constantes.convertirStringArray(efectosC);
		for (String a : splt) {
			try {
				if (a.equals("null") || a.isEmpty()) {
					continue;
				}
				a = a.replace('[', ' ').replace(']', ' ').replace(" ", "");
				int efectoID = Integer.parseInt(a.split(",")[0]);
				String args = a.split(",", 2)[1];
				EfectoHechizo eh = new EfectoHechizo(efectoID, args, hechizoID, _grado, zonaEfecto.substring(num * 2, num * 2
				+ 2));
				_efectosCriticos.add(eh);
				fijarEfectos(efectoID);
				num++;
			} catch (final Exception e) {
				MainServidor.redactarLogServidorln("[BUG HECHIZO ID] " + hechizoID + " : " + efectosC);
				e.printStackTrace();
				System.exit(1);
				return;
			}
		}
		_efectosCriticos.trimToSize();
		ordenar();
	}
	
	private void setValorAfectado(EfectoHechizo eh, String[] normales, int i) {
		int afectado = 0;
		int afectadoCond = 0;
		if (i < normales.length && !normales[i].isEmpty()) {
			String[] s = normales[i].toUpperCase().split(Pattern.quote("*"));
			String a = "";
			if (s.length > 1) {
				a = s[1];
			}
			try {
				afectado = Integer.parseInt(s[0]);
			} catch (Exception e) {
				if (a.isEmpty()) {
					a = s[0];
				}
			}
			try {
				if (a.contains("D_")) {
					String[] ele = a.replace("D_", "").split("");
					for (String e : ele) {
						switch (e) {
							case "A" :
								afectadoCond |= 1 << Constantes.ELEMENTO_AIRE;
								break;
							case "W" :
								afectadoCond |= 1 << Constantes.ELEMENTO_AGUA;
								break;
							case "F" :
								afectadoCond |= 1 << Constantes.ELEMENTO_FUEGO;
								break;
							case "E" :
								afectadoCond |= 1 << Constantes.ELEMENTO_TIERRA;
								break;
							case "N" :
								afectadoCond |= 1 << Constantes.ELEMENTO_NEUTRAL;
								break;
						}
					}
				}
			} catch (Exception e) {}
		}
		eh.setAfectados(afectado);
		eh.setAfectadosCond(afectadoCond);
	}
	
	public void setAfectados(String[] normales, String[] criticos) {
		for (int i = 0; i < _efectosNormales.size(); i++) {
			EfectoHechizo eh = _efectosNormales.get(i);
			setValorAfectado(eh, normales, i);
		}
		for (int i = 0; i < _efectosCriticos.size(); i++) {
			EfectoHechizo eh = _efectosCriticos.get(i);
			setValorAfectado(eh, criticos, i);
		}
	}
	
	public void setCondiciones(String[] normales, String[] criticos) {
		for (int i = 0; i < _efectosNormales.size(); i++) {
			EfectoHechizo eh = _efectosNormales.get(i);
			if (i < normales.length && !normales[i].isEmpty()) {
				eh.setCondicion(normales[i]);
			}
		}
		for (int i = 0; i < _efectosCriticos.size(); i++) {
			EfectoHechizo eh = _efectosCriticos.get(i);
			if (i < criticos.length && !criticos[i].isEmpty()) {
				eh.setCondicion(criticos[i]);
			}
		}
	}
	
	private void ordenar() {
		_ordenadoNormales.clear();
		_ordenadoNormales.addAll(_efectosNormales);
		Collections.sort(_ordenadoNormales, new CompPrioridad());
		_ordenadoNormales.trimToSize();
		_ordenadoCriticos.clear();
		_ordenadoCriticos.addAll(_efectosCriticos);
		Collections.sort(_ordenadoCriticos, new CompPrioridad());
		_ordenadoCriticos.trimToSize();
	}
	
	public byte getTipo() {
		return _tipoHechizo;
	}
	
	public Hechizo getHechizo() {
		return _hechizo;
	}
	
	public int getHechizoID() {
		return _hechizo.getID();
	}
	
	public int getSpriteID() {
		return _hechizo.getSpriteID();
	}
	
	public String getSpriteInfos() {
		return _hechizo.getSpriteInfos();
	}
	
	public int getGrado() {
		return _grado;
	}
	
	public ArrayList<Integer> getEstadosProhibido() {
		return _estadosProhibidos;
	}
	
	public ArrayList<Integer> getEstadosNecesario() {
		return _estadosNecesarios;
	}
	
	public byte getCostePA() {
		return _costePA;
	}
	
	public byte getMinAlc() {
		return _minAlc;
	}
	
	public byte getMaxAlc() {
		return _maxAlc;
	}
	
	public short getProbabilidadGC() {
		return _probGC;
	}
	
	public short getProbabilidadFC() {
		return _probFC;
	}
	
	public boolean esLanzarLinea() {
		return _lanzarLinea;
	}
	
	public boolean esLineaVista() {
		return _lineaDeVista;
	}
	
	public boolean esNecesarioCeldaLibre() {
		return _necesarioCeldaLibre;
	}
	
	public boolean esAlcanceModificable() {
		return _alcModificable;
	}
	
	public boolean esNecesarioObjetivo() {
		return _necesitaObjetivo;
	}
	
	public byte getMaxLanzPorTurno() {
		return _maxLanzPorTurno;
	}
	
	public byte getMaxLanzPorObjetivo() {
		return _maxLanzPorObjetivo;
	}
	
	public byte getSigLanzamiento() {
		return _sigLanzamiento;
	}
	
	public int getNivelRequerido() {
		return _nivelRequerido;
	}
	
	public boolean esFinTurnoSiFC() {
		return _esFinTurnoSiFC;
	}
	
	public ArrayList<EfectoHechizo> getEfectosNormales() {
		return _ordenadoNormales;
	}
	
	public ArrayList<EfectoHechizo> getEfectosCriticos() {
		return _ordenadoCriticos;
	}
	
	public int beneficio(final Luchador lanzador, final Mapa mapa, final short idCeldaObjetivo, Luchador objetivo) {
		ArrayList<EfectoHechizo> efectos = _efectosNormales;
		if (_efectosCriticos != null && !_efectosCriticos.isEmpty()) {
			efectos = _efectosCriticos;
		}
		int suerte = 0, suerteMax = 0, azar = 0, cantidad = 0;
		boolean tiene666 = false;
		boolean filtrarSuerte = false;
		for (final EfectoHechizo EH : efectos) {
			if (EH.getEfectoID() == 666 && EH.getSuerte() > 0) {
				tiene666 = true;
			}
			if (EH.getSuerte() == 0) {
				filtrarSuerte = true;
			}
			suerteMax += EH.getSuerte();
		}
		if (suerteMax > 0) {
			azar = Formulas.getRandomInt(1, suerteMax);
		}
		for (final EfectoHechizo EH : efectos) {
			if (EH.getSuerte() > 0) {
				if (filtrarSuerte || tiene666) {
					continue;
				}
				if (azar < suerte || azar >= EH.getSuerte() + suerte) {
					suerte += EH.getSuerte();
					continue;
				}
				suerte += EH.getSuerte();
			}
			final ArrayList<Luchador> listaLuchadores = Hechizo.getObjetivosEfecto(mapa, lanzador, EH, idCeldaObjetivo);
			int estima = Constantes.getInflDañoPorEfecto(EH.getEfectoID(), lanzador, objetivo, EH.getValorParaPromediar(),
			idCeldaObjetivo, this);
			for (Luchador L : listaLuchadores) {
				if (estima > 0) {
					if (L.getEquipoBin() != lanzador.getEquipoBin()) {
						cantidad++;
					} else {
						cantidad--;
					}
				} else if (estima < 0) {
					if (L.getEquipoBin() == lanzador.getEquipoBin()) {
						cantidad++;
					} else {
						cantidad--;
					}
				}
			}
		}
		return cantidad;
	}
	
	public ArrayList<Luchador> listaObjetivosAfectados(final Luchador lanzador, final Mapa mapa,
	final short celdaObjetivoID) {
		ArrayList<Luchador> objetivos = new ArrayList<>();
		ArrayList<EfectoHechizo> efectos = _efectosNormales;
		if (_efectosCriticos != null && !_efectosCriticos.isEmpty()) {
			efectos = _efectosCriticos;
		}
		int suerte = 0, suerteMax = 0, azar = 0;
		boolean tiene666 = false;
		boolean filtrarSuerte = false;
		for (final EfectoHechizo EH : efectos) {
			if (EH.getEfectoID() == 666 && EH.getSuerte() > 0) {
				tiene666 = true;
			}
			if (EH.getSuerte() == 0) {
				filtrarSuerte = true;
			}
			suerteMax += EH.getSuerte();
		}
		if (suerteMax > 0) {
			azar = Formulas.getRandomInt(1, suerteMax);
		}
		for (final EfectoHechizo EH : efectos) {
			if (EH.getSuerte() > 0) {
				if (filtrarSuerte || tiene666) {
					continue;
				}
				if (azar < suerte || azar >= EH.getSuerte() + suerte) {
					suerte += EH.getSuerte();
					continue;
				}
				suerte += EH.getSuerte();
			}
			ArrayList<Luchador> objs = Hechizo.getObjetivosEfecto(mapa, lanzador, EH, celdaObjetivoID);
			for (Luchador o : objs) {
				if (!objetivos.contains(o)) {
					objetivos.add(o);
				}
			}
		}
		return objetivos;
	}
	
	public boolean estaDentroAfectados(final Luchador lanzador, final Luchador objetivo, final Mapa mapa,
	final short celdaObjetivoID) {
		ArrayList<EfectoHechizo> efectos = _efectosNormales;
		if (_efectosCriticos != null && !_efectosCriticos.isEmpty()) {
			efectos = _efectosCriticos;
		}
		int suerte = 0, suerteMax = 0, azar = 0;
		boolean tiene666 = false;
		boolean filtrarSuerte = false;
		for (final EfectoHechizo EH : efectos) {
			if (EH.getEfectoID() == 666 && EH.getSuerte() > 0) {
				tiene666 = true;
			}
			if (EH.getSuerte() == 0) {
				filtrarSuerte = true;
			}
			suerteMax += EH.getSuerte();
		}
		if (suerteMax > 0) {
			azar = Formulas.getRandomInt(1, suerteMax);
		}
		for (final EfectoHechizo EH : efectos) {
			if (EH.getSuerte() > 0) {
				if (filtrarSuerte || tiene666) {
					continue;
				}
				if (azar < suerte || azar >= EH.getSuerte() + suerte) {
					suerte += EH.getSuerte();
					continue;
				}
				suerte += EH.getSuerte();
			}
			ArrayList<Luchador> objetivos = Hechizo.getObjetivosEfecto(mapa, lanzador, EH, celdaObjetivoID);
			if (objetivos.contains(objetivo)) {
				return true;
			}
		}
		return false;
	}
	private static class CompPrioridad implements Comparator<EfectoHechizo> {
		@Override
		public int compare(EfectoHechizo p1, EfectoHechizo p2) {
			if (Constantes.prioridadEfecto(p1.getEfectoID()) < Constantes.prioridadEfecto(p2.getEfectoID()))
				return -1;
			if (Constantes.prioridadEfecto(p1.getEfectoID()) > Constantes.prioridadEfecto(p2.getEfectoID()))
				return 1;
			return 0;
		}
	}
}