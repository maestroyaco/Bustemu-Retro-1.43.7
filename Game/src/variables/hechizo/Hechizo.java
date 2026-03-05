package variables.hechizo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import estaticos.Camino;
import estaticos.Constantes;
import estaticos.Formulas;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import variables.hechizo.EfectoHechizo.TipoDaño;
import variables.mapa.Celda;
import variables.mapa.Mapa;
import variables.pelea.Luchador;
import variables.pelea.Pelea;

public class Hechizo {
	private final int _id;
	private int _spriteID;
	private final String _nombre;
	private String _spriteInfos;// tipo lanz, anim pj, 1 o 0 (frente al sprite)
	private int _valorIA = 0;
	private final Map<Integer, StatHechizo> _statsHechizos = new HashMap<Integer, StatHechizo>();
	
	public Hechizo(final int aHechizoID, final String aNombre, final int aSpriteID, final String aSpriteInfos,
	final int valorIA) {
		_id = aHechizoID;
		_nombre = aNombre;
		_spriteID = aSpriteID;
		_spriteInfos = aSpriteInfos;
		_valorIA = valorIA;
	}
	
	public void setAfectados(String afectados) {
		if (afectados.contains(":") || afectados.contains(";")) {
			afectados = afectados.replace(":", "|").replace(";", ",");
			GestorSQL.UPDATE_HECHIZO_AFECTADOS(_id, afectados);
		}
		String normales = "";
		try {
			normales = afectados.split(Pattern.quote("|"))[0];
		} catch (Exception e) {}
		String criticos = "";
		try {
			criticos = afectados.split(Pattern.quote("|"))[1];
		} catch (Exception e) {}
		String[] aNormales = normales.split(",");
		String[] aCriticos = criticos.split(",");
		for (StatHechizo sh : _statsHechizos.values()) {
			if (sh == null) {
				continue;
			}
			sh.setAfectados(aNormales, aCriticos);
		}
	}
	
	public void setCondiciones(String condicion) {
		if (condicion.contains(":") || condicion.contains(";")) {
			condicion = condicion.replace(":", "|").replace(";", ",");
		}
		String normales = "";
		try {
			normales = condicion.split(Pattern.quote("|"))[0];
		} catch (Exception e) {}
		String criticos = "";
		try {
			criticos = condicion.split(Pattern.quote("|"))[1];
		} catch (Exception e) {}
		String[] aNormales = normales.split(",");
		String[] aCriticos = criticos.split(",");
		for (StatHechizo sh : _statsHechizos.values()) {
			if (sh == null) {
				continue;
			}
			sh.setCondiciones(aNormales, aCriticos);
		}
	}
	
	// public ArrayList<Integer> getArrayAfectados() {
	// return _afectados;
	// }
	public int getValorIA() {
		return _valorIA;
	}
	
	public void setValorIA(int valorIA) {
		_valorIA = valorIA;
	}
	
	public int getSpriteID() {
		return _spriteID;
	}
	
	public String getSpriteInfos() {
		return _spriteInfos;
	}
	
	public void setSpriteInfos(final String str) {
		_spriteInfos = str;
	}
	
	public void setSpriteID(final int id) {
		_spriteID = id;
	}
	
	public String getNombre() {
		return _nombre;
	}
	
	public int getID() {
		return _id;
	}
	
	public StatHechizo getStatsPorNivel(final int nivel) {
		return _statsHechizos.get(nivel);
	}
	
	public void addStatsHechizos(final int nivel, final StatHechizo stats) {
		_statsHechizos.put(nivel, stats);
	}
	
	// public static String strDañosStats2(StatHechizo sh, int valoresStat[]) {
	// StringBuilder str = new StringBuilder(sh.getHechizoID() + "");
	// for (EfectoHechizo eh : sh.getEfectosNormales()) {
	// int valorStat = 0;
	// switch (eh.getEfectoID()) {
	// case Constantes.STAT_CURAR :
	// case Constantes.STAT_CURAR_2 :
	// valorStat = valoresStat[2];// inteligencia
	// break;
	// default :
	// byte elemento = Constantes.getElementoPorEfectoID(eh.getEfectoID());
	// if (elemento < 0) {
	// continue;
	// }
	// valorStat = valoresStat[elemento];
	// break;
	// }
	// str.append(";" + eh.getEfectoID() + "," + EfectoHechizo.strMinMax(eh, valorStat));
	// }
	// return str.toString();
	// }
	//
	public static String strDañosStats(StatHechizo sh, int valoresStat[]) {
		StringBuilder str = new StringBuilder(sh.getHechizoID() + "");
		boolean paso = false;
		for (EfectoHechizo eh : sh.getEfectosNormales()) {
			String nombre = Constantes.getNombreEfecto(eh.getEfectoID());
			int valorStat = -1;
			switch (eh.getEfectoID()) {
				case Constantes.STAT_CURAR :
				case Constantes.STAT_CURAR_2 :
					valorStat = valoresStat[2];// inteligencia
					break;
				default :
					byte elemento = Constantes.getElementoPorEfectoID(eh.getEfectoID());
					if (elemento != Constantes.ELEMENTO_NULO) {
						valorStat = valoresStat[elemento];
					}
					break;
			}
			if (paso) {
				str.append("\n");
			} else {
				str.append(";");
			}
			str.append("-> " + nombre);
			str.append(" " + stringDataEfecto(eh, valorStat));
			paso = true;
		}
		return str.toString();
	}
	
	private static String stringDataEfecto(EfectoHechizo EH, int valorStat) {
		String s = "";
		if (valorStat != -1) {
			if (EH._segundoValor != -1) {
				s = ("of " + (EH._primerValor * (100 + valorStat) / 100) + " a " + (EH._segundoValor * (100 + valorStat)
				/ 100));
			} else if (EH._primerValor != -1) {
				s = "fix " + (EH._primerValor * (100 + valorStat) / 100);
			} else {
				s = "[" + EH._primerValor + ", " + EH._segundoValor + "]";
			}
		} else {
			s = "[" + EH._primerValor + ", " + EH._segundoValor + "]";
		}
		s += " (Turns: " + (EH._duracion <= -1 ? "Inf." : EH._duracion) + ")";
		return "<i>" + s + "</i>";
	}
	
	public static void aplicaHechizoAPelea(final Pelea pelea, final Luchador lanzador, final Celda celdaObj,
	final ArrayList<EfectoHechizo> efectosH, final TipoDaño tipo, final boolean esGC) {
		int cantObjetivos = aplicaHechizoAPeleaSinGTM(pelea, lanzador, celdaObj, efectosH, tipo, esGC);
		if (cantObjetivos > 0) {
			GestorSalida.ENVIAR_GTM_INFO_STATS_TODO_LUCHADORES_A_TODOS(pelea, 7,  false);
		}
	}
	
	public static int aplicaHechizoAPeleaSinGTM(final Pelea pelea, final Luchador lanzador, final Celda celdaObj,
	final ArrayList<EfectoHechizo> efectosH, final TipoDaño tipo, final boolean esGC) {
		if (efectosH == null) {
			return 0;
		}
		int suerte = 0, suerteMax = 0, cantObjetivos = 0, azar = 0;
		for (final EfectoHechizo EH : efectosH) {
			suerteMax += EH.getSuerte();
		}
		if (suerteMax > 0) {
			azar = Formulas.getRandomInt(1, suerteMax);
		}
		for (final EfectoHechizo EH : efectosH) {
			if (pelea.getFase() == Constantes.PELEA_FASE_FINALIZADO) {
				return 0;
			}
			if (suerteMax > 0) {
				if (EH.getSuerte() > 0 && EH.getSuerte() < 100) {
					if (azar < suerte || azar >= EH.getSuerte() + suerte) {
						suerte += EH.getSuerte();
						continue;
					}
					suerte += EH.getSuerte();
				}
			}
			ArrayList<Luchador> objetivos = getObjetivosEfecto(pelea.getMapaCopia(), lanzador, EH, celdaObj.getID());
			if (cantObjetivos < objetivos.size()) {
				cantObjetivos = objetivos.size();
			}
			EH.aplicarAPelea(pelea, lanzador, objetivos, celdaObj, tipo, esGC);
			try {
				Thread.sleep(EfectoHechizo.TIEMPO_ENTRE_EFECTOS);
			} catch (Exception e) {}
		}
		return cantObjetivos;
	}
	
	// public static int aplicaHechizoAPelea(final Pelea pelea, final Luchador lanzador, final Celda
	// celdaObj,
	// final ArrayList<EfectoHechizo> efectosH, final TipoDaño tipo, final boolean esGC,
	// ArrayList<ArrayList<Luchador>> aObjetivos) {
	// if (efectosH == null) {
	// return 0;
	// }
	// int suerte = 0, suerteMax = 0, cantObjetivos = 0, azar = 0;
	// for (final EfectoHechizo EH : efectosH) {
	// suerteMax += EH.getSuerte();
	// }
	// if (suerteMax > 0) {
	// azar = Formulas.getRandomValor(1, suerteMax);
	// }
	// int index = 0;
	// for (final EfectoHechizo EH : efectosH) {
	// index++;
	// if (pelea.getFase() == Constantes.PELEA_FASE_FINALIZADO) {
	// return 0;
	// }
	// if (suerteMax > 0) {
	// if (EH.getSuerte() > 0 && EH.getSuerte() < 100) {
	// if (azar < suerte || azar >= EH.getSuerte() + suerte) {
	// suerte += EH.getSuerte();
	// continue;
	// }
	// suerte += EH.getSuerte();
	// }
	// }
	// ArrayList<Luchador> objetivos = aObjetivos.get(index);
	// if (cantObjetivos < objetivos.size()) {
	// cantObjetivos = objetivos.size();
	// }
	// EH.aplicarAPelea(pelea, lanzador, objetivos, celdaObj, tipo, esGC);
	// try {
	// Thread.sleep(EfectoHechizo.TIEMPO_ENTRE_EFECTOS);
	// } catch (Exception e) {}
	// }
	// return cantObjetivos;
	// }
	public static ArrayList<Luchador> getObjetivosEfecto(Mapa mapa, Luchador lanzador, EfectoHechizo EH,
	short celdaObjetivo) {
		ArrayList<Luchador> objetivos = new ArrayList<>();
		int elemento = EH.getAfectadosCond();
		if (elemento > 0) {
			// son bytes
			int ultDaño = lanzador.getUltimoElementoDaño();
			if (ultDaño < Constantes.ELEMENTO_NULO) {
				return objetivos;
			}
			if (((1 << ultDaño) & elemento) == 0) {
				return objetivos;
			}
		}
		final ArrayList<Celda> celdasObj = Camino.celdasAfectadasEnElArea(mapa, celdaObjetivo, lanzador.getCeldaPelea()
		.getID(), EH.getZonaEfecto());
		objetivos = getAfectadosZona(lanzador, celdasObj, EH.getAfectados(), celdaObjetivo);
		return objetivos;
	}
	
	private static ArrayList<Luchador> getAfectadosZona(Luchador lanzador, ArrayList<Celda> celdasObj, int afectados,
	short celdaObjetivo) {
		final ArrayList<Luchador> objetivos = new ArrayList<Luchador>();
		for (final Celda C : celdasObj) {
			if (C == null) {
				continue;
			}
			final Luchador luchTemp = C.getPrimerLuchador();
			if (luchTemp == null) {
				continue;
			}
			// no afecta a los aliados
			if (afectados >= 0) {
				if ((afectados & 1) != 0 && luchTemp.getEquipoBin() == lanzador.getEquipoBin()) {
					continue;
				}
				// no afecta al lanzador
				if ((afectados & 2) != 0 && luchTemp.getID() == lanzador.getID()) {
					continue;
				}
				// no afecta a los enemigos
				if ((afectados & 4) != 0 && luchTemp.getEquipoBin() != lanzador.getEquipoBin()) {
					continue;
				}
				// no afecta a los combatientes (solamente invocaciones)
				if ((afectados & 8) != 0 && !luchTemp.esInvocacion()) {
					continue;
				}
				// No afecta a las invocaciones
				if ((afectados & 16) != 0 && luchTemp.esInvocacion()) {
					continue;
				}
				// 32 y 64 son de agregar si o si, respectivamente lanzador e invocador
				if ((afectados == 32) && luchTemp.getID() != lanzador.getID()) {
					continue;
				}
				if ((afectados == 64) && lanzador.getInvocador() != null && lanzador.getInvocador().getID() != luchTemp
				.getID()) {
					continue;
				}
				// no afecta a la casilla donde se lanza
				if ((afectados & 128) != 0 && celdaObjetivo == luchTemp.getCeldaPelea().getID()) {
					continue;
				}
				if ((afectados & 256) != 0 && luchTemp.esInvocacion()) {
					continue;
				}
				// de aqui pasa el siguiente filtro
			}
			if (!objetivos.contains(luchTemp)) {
				objetivos.add(luchTemp);
			}
		}
		// agrega si o si al lanzador
		if (afectados >= 0) {
			if ((afectados & 32) != 0) {
				if (!objetivos.contains(lanzador)) {
					objetivos.add(lanzador);
				}
			}
			// agrega si o si al invocador
			if ((afectados & 64) != 0) {
				final Luchador invocador = lanzador.getInvocador();
				if (invocador != null && !objetivos.contains(invocador)) {
					objetivos.add(invocador);
				}
			}
		}
		return objetivos;
	}
	
	//
	public static StatHechizo analizarHechizoStats(final int hechizoID, final int grado, final String str)
	throws Exception {
		final ArrayList<String> stat = Constantes.convertirStringArray(str);
		final String efectosNormales = stat.get(0).replace("\"", "");
		final String efectosCriticos = stat.get(1).replace("\"", "");
		final byte costePA = Byte.parseByte(stat.get(2));
		final byte alcMin = Byte.parseByte(stat.get(3));
		final byte alcMax = Byte.parseByte(stat.get(4));
		final short probGC = Short.parseShort(stat.get(5));
		final short probFC = Short.parseShort(stat.get(6));
		final boolean lanzarLinea = stat.get(7).equalsIgnoreCase("true");
		final boolean lineaVista = stat.get(8).equalsIgnoreCase("true");
		final boolean celdaVacia = stat.get(9).equalsIgnoreCase("true");
		final boolean alcanceModificable = stat.get(10).equalsIgnoreCase("true");
		final byte tipoHechizo = Byte.parseByte(stat.get(11));
		final byte maxPorTurno = Byte.parseByte(stat.get(12));
		final byte maxPorObjetivo = Byte.parseByte(stat.get(13));
		final byte sigLanzamiento = Byte.parseByte(stat.get(14));
		final String areaAfectados = stat.get(15).replace("\"", "");
		final String estadosNecesarios = stat.get(16);
		final String estadosProhibidos = stat.get(17);
		final int nivelReq = Integer.parseInt(stat.get(18));
		final boolean finTurnoSiFC = stat.get(19).equalsIgnoreCase("true");
		final boolean necesitaObjetivo = stat.size() >= 21 ? stat.get(20).equalsIgnoreCase("true") : false;
		final StatHechizo stats = new StatHechizo(hechizoID, grado, costePA, alcMin, alcMax, probGC, probFC, lanzarLinea,
		lineaVista, celdaVacia, alcanceModificable, maxPorTurno, maxPorObjetivo, sigLanzamiento, nivelReq, finTurnoSiFC,
		estadosProhibidos, estadosNecesarios, tipoHechizo, necesitaObjetivo);
		stats.analizarEfectos(efectosNormales, efectosCriticos, areaAfectados, hechizoID);
		return stats;
	}
}
