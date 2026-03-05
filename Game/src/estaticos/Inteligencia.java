package estaticos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import variables.hechizo.EfectoHechizo;
import variables.hechizo.Hechizo;
import variables.hechizo.HechizoLanzado;
import variables.hechizo.StatHechizo;
import variables.mapa.Celda;
import variables.mapa.Mapa;
import variables.pelea.Luchador;
import variables.pelea.Pelea;
import variables.stats.TotalStats;
import estaticos.Mundo.Duo;

public class Inteligencia extends Thread {
	public enum EstadoLanzHechizo {
		PODER, NO_TIENE_PA, NO_TIENE_ALCANCE, OBJETIVO_NO_PERMITIDO, NO_PODER, FALLAR, COOLDOWN, NO_OBJETIVO
	};
	public enum EstadoDistancia {
		TACLEADO, ACERCARSE, NO_PUEDE, INVISIBLE
	};
	public enum EstadoMovAtq {
		SE_MOVIO, NO_HIZO_NADA, LANZO_HECHIZO, TACLEADO, NO_PUEDE_MOVERSE, NO_TIENE_HECHIZOS, TIENE_HECHIZOS_SIN_LANZAR, NULO
	};
	public enum Orden {
		PDV_MENOS_A_MAS, PDV_MAS_A_MENOS, NADA, NIVEL_MENOS_A_MAS, NIVEL_MAS_A_MENOS, INVOS_PRIMEROS, INVOS_ULTIMOS
	}
	public enum Accion {
		ATACAR, BOOSTEAR, CURAR, TRAMPEAR, INVOCAR, TELEPORTAR, NADA
	}
	private static final int INTELIGENCIA_COMPARAR_INF_OBJETIVOS = 9;
	private static final int INTELIGENCIA_COMPARAR_INF_DIST_OBJETIVOS = 11;
	private static final int INTELIGENCIA_ORDENAR_PRIORIDAD_OBJETIVOS = 5;
	private static final int INTELIGENCIA_SOLO_CELDAS_CON_LUCHADOR = 7;
	// sorts ordena siempre en orden ascendente
	private static class CompPDVMenosMas implements Comparator<Luchador> {
		@Override
		public int compare(Luchador p1, Luchador p2) {
			return new Integer(p1.getPDVSinBuff()).compareTo(new Integer(p2.getPDVSinBuff()));
		}
	}
	private static class CompPDVMasMenos implements Comparator<Luchador> {
		@Override
		public int compare(Luchador p1, Luchador p2) {
			return new Integer(p2.getPDVSinBuff()).compareTo(new Integer(p1.getPDVSinBuff()));
		}
	}
	private static class CompNivelMenosMas implements Comparator<Luchador> {
		@Override
		public int compare(Luchador p1, Luchador p2) {
			return new Integer(p1.getNivel()).compareTo(new Integer(p2.getNivel()));
		}
	}
	private static class CompNivelMasMenos implements Comparator<Luchador> {
		@Override
		public int compare(Luchador p1, Luchador p2) {
			return new Integer(p2.getNivel()).compareTo(new Integer(p1.getNivel()));
		}
	}
	private static class CompInvosUltimos implements Comparator<Luchador> {
		@Override
		public int compare(Luchador p1, Luchador p2) {
			if (!p1.esInvocacion()) {
				return -1;
			}
			return 1;
		}
	}
	private static class CompInvosPrimeros implements Comparator<Luchador> {
		@Override
		public int compare(Luchador p1, Luchador p2) {
			if (p1.esInvocacion()) {
				return -1;
			}
			return 1;
		}
	}
	private static int PDV_MINIMO_CURAR = 99;
	private boolean _fin = false, _resetearCeldasHechizos, _resetearInfluencias;
	private boolean _refrescarMov = false;
	private final Pelea _pelea;
	private final Luchador _lanzador;
	private Map<Short, Map<StatHechizo, Map<Celda, ArrayList<Luchador>>>> _celdasHechizos = new LinkedHashMap<>();
	private Map<Luchador, Map<EfectoHechizo, Integer>> _influencias = new HashMap<>();
	private ArrayList<Short> _celdasMovimiento = new ArrayList<>();
	
	public Inteligencia(final Luchador atacante, final Pelea p) {
		_lanzador = atacante;
		_pelea = p;
		_pelea.addIA(this);
		this.setDaemon(true);
		this.setPriority(1);
	}
	
	public int getTipoIA() {
		if (_lanzador == null) {
			return -1;
		}
		try {
			if (_lanzador.esDoble()) {
				return 5;
			} else if (_lanzador.getRecaudador() != null) {
				return 21;
			} else if (_lanzador.getPrisma() != null) {
				return 20;
			} else if (_lanzador.getMob() != null) {
				return _lanzador.getMob().getMobModelo().getTipoIA();
			}
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION getTipoIA " + e.toString());
			e.printStackTrace();
		}
		return -1;
	}
	
	public void forzarRefrescarMov() {
		_refrescarMov = true;
	}
	
	public void nullear() {
		_resetearCeldasHechizos = true;
		_resetearInfluencias = true;
	}
	
	// public void destruir() {
	// try {
	// finalize();
	// } catch (final Throwable e) {}
	// }
	@SuppressWarnings("deprecation")
	public synchronized void arrancar() {
		try {
			if (this.getState() == State.NEW) {
				this.start();
			} else if (this.getState() != State.TERMINATED) {
				this.resume();
			}
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("Exception ARRANCAR IA tipo: " + getTipoIA() + ", atacante: "
			+ (_lanzador == null ? "Null" : _lanzador.getNombre()) + ", " + (_pelea == null
			? " pelea Null"
			: ("pMapa: " + _pelea.getMapaCopia().getID() + " pID: " + _pelea.getID() + " pEstado: " + _pelea.getFase()))
			+ ", Exception " + e.toString());
		}
	}
	
	// solo funciona cuando se cancela una pelea
	@SuppressWarnings("deprecation")
	public synchronized void parar() {
		try {
			_fin = true;
			// arrancar();
			this.stop();
		} catch (Exception e) {
			e.printStackTrace();
			MainServidor.redactarLogServidorln("EXCEPTION parar IA tipo: " + getTipoIA() + ", atacante: " + (_lanzador == null
			? "Null"
			: _lanzador.getNombre()) + ", " + (_pelea == null
			? " pelea Null"
			: ("pMapa: " + _pelea.getMapaCopia().getID() + " pID: " + _pelea.getID() + " pEstado: " + _pelea.getFase()))
			+ ", Exception " + e.toString());
		}
	}
	
	@SuppressWarnings("deprecation")
	public void run() {
		String sTipo = "ninguna";
		try {
			while (!_fin && Mundo.SERVIDOR_ESTADO != Constantes.SERVIDOR_OFFLINE && this.isAlive()) {
				int tipo = getTipoIA();
				sTipo = tipo + "";
				nullear();
				switch (tipo) {
					case -1 : // no hace nada
						break;
					case 0 :// general -lag
						tipo_0();
						break;
					case 1 :// general
						tipo_1();
						break;
					case 2 :// esfera xelor
						tipo_2();
						break;
					case 3 ://
						tipo_3();
						break;
					case 4 :// tofu,prespic
						tipo_4();
						break;
					case 5 :// bloqueadora
						tipo_5();
						break;
					case 6 :// hinchable, conejo
						tipo_6();
						break;
					case 7 :// gatake, ataca y solo ataca
						tipo_7();
						break;
					case 8 :// mochila animada
						tipo_8();
						break;
					case 9 :// cofre animado, arbol de la vida
						tipo_9();
						break;
					case 10 :// cascara explosiva
						tipo_10();
						break;
					case 11 :// chaferloko, y lancero
						tipo_11();
						break;
					case 12 :// kralamar gigante
						tipo_12();
						break;
					case 13 :// vasija
						tipo_13();
						break;
					case 14 :// aguja buscadora
						tipo_14();
						break;
					case 15 :// IA @fox discord
						tipo_15();
						break;
					case 16 :// tentaculo
						tipo_16();
						break;
					case 20 :// prisma
						tipo_20();
						break;
					case 21 :// recaudador
						tipo_21();
						break;
				}
				//
				if (_lanzador != null) {
					if (!_lanzador.estaMuerto()) {
						// esta vivo y va a pasar turno
						Thread.sleep(100);
						if (_lanzador.puedeJugar()) {
							if (_pelea != null) {
								_pelea.pasarTurno(_lanzador);
							}
						}
					}
					if (!_lanzador.puedeJugar()) {
						this.suspend();
					}
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			MainServidor.redactarLogServidorln("EXCEPTION run IA tipo: " + sTipo + ", atacante: " + (_lanzador == null
			? "Null"
			: _lanzador.getNombre()) + ", " + (_pelea == null
			? " pelea Null"
			: ("pMapa: " + _pelea.getMapaCopia().getID() + " pID: " + _pelea.getID() + " pEstado: " + _pelea.getFase()))
			+ ", Exception " + e.toString());
			try {
				if (_pelea != null) {
					MainServidor.redactarLogServidorln("ELMINANDO A LA IA DEL MOB");
					_pelea.addMuertosReturnFinalizo(_lanzador, null);
				}
			} catch (Exception e1) {}
		}
	}
	
	private void clearInfluencias() {
		if (_resetearInfluencias) {
			_influencias.clear();
			_resetearInfluencias = false;
		}
	}
	
	private void clearCeldasHechizos() {
		if (_resetearCeldasHechizos) {
			_celdasHechizos.clear();
			_resetearCeldasHechizos = false;
		}
	}
	
	private void setCeldasHechizoCeldaLanz() {
		clearCeldasHechizos();
		short celdaLanzID = _lanzador.getCeldaPelea().getID();
		if (!_celdasHechizos.containsKey(celdaLanzID)) {
			Map<StatHechizo, Map<Celda, ArrayList<Luchador>>> a = getObjHechDesdeCeldaLanz(celdaLanzID);
			if (a == null || a.isEmpty()) {
				return;
			}
			_celdasHechizos.put(celdaLanzID, a);
		}
	}
	
	private ArrayList<Luchador> getObjetivosGuardado(Celda celdaPosibleLanzamiento, StatHechizo SH) {
		for (Map<StatHechizo, Map<Celda, ArrayList<Luchador>>> a : _celdasHechizos.values()) {
			if (a.get(SH) != null) {
				if (a.get(SH).get(celdaPosibleLanzamiento) != null) {
					return a.get(SH).get(celdaPosibleLanzamiento);
				}
			}
		}
		return null;
	}
	
	private Map<StatHechizo, Map<Celda, ArrayList<Luchador>>> getObjHechDesdeCeldaLanz(short celdaLanzador) {
		Map<StatHechizo, Map<Celda, ArrayList<Luchador>>> map = new HashMap<>();
		for (final StatHechizo SH2 : hechizosLanzables()) {
			ArrayList<Celda> celdas = Camino.celdasPosibleLanzamiento(SH2, _lanzador, _pelea.getMapaCopia(), celdaLanzador,
			(short) -1);
			for (Celda celdaPosibleLanzamiento : celdas) {
				if (_pelea.puedeLanzarHechizo(_lanzador, SH2, celdaPosibleLanzamiento,
				celdaLanzador) != EstadoLanzHechizo.PODER) {
					continue;
				}
				ArrayList<Luchador> o = getObjetivosGuardado(celdaPosibleLanzamiento, SH2);
				if (o == null) {
					o = SH2.listaObjetivosAfectados(_lanzador, _pelea.getMapaCopia(), celdaPosibleLanzamiento.getID());
				}
				// if (!o.isEmpty()) {
				if (map.get(SH2) == null) {
					map.put(SH2, new HashMap<Celda, ArrayList<Luchador>>());
				}
				map.get(SH2).put(celdaPosibleLanzamiento, o);
				// }
			}
		}
		return map;
	}
	
	private void tipo_0() {
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> amigos = ordenLuchadores(_lanzador.getParamEquipoAliado(), Orden.NIVEL_MAS_A_MENOS);
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		final float porcPDV = _lanzador.getPorcPDV();
		if (porcPDV > 50) {
			EstadoMovAtq movAtq;
			while (buffeaSiEsPosible(amigos)) {}
			while (trampearSiEsPosible()) {}
			invocarSiEsPosible(enemigos);
			fullAtaqueSioSi(enemigos);
			do {
				movAtq = moverYLanzarAlgo(enemigos, Accion.ATACAR, false);
			} while (movAtq == EstadoMovAtq.TACLEADO || movAtq == EstadoMovAtq.SE_MOVIO
			|| movAtq == EstadoMovAtq.LANZO_HECHIZO);
			teleportSiEsPosible(enemigos);
			while (curaSiEsPosible(amigos)) {}
			EstadoDistancia acercarse = EstadoDistancia.NO_PUEDE;
			do {
				if (movAtq != EstadoMovAtq.NO_TIENE_HECHIZOS) {
					fullAtaqueSioSi(enemigos);
				}
			} while (movAtq != EstadoMovAtq.NO_PUEDE_MOVERSE && (acercarse = acercarseA(enemigos, true,
			true)) == EstadoDistancia.ACERCARSE);
			while (trampearSiEsPosible()) {}
			siEsInvisible(acercarse);
		} else {
			while (curaSiEsPosible(amigos)) {}
			while (buffeaSiEsPosible(amigos)) {}
			while (trampearSiEsPosible()) {}
			EstadoMovAtq movAtq;
			invocarSiEsPosible(enemigos);
			fullAtaqueSioSi(enemigos);
			do {
				movAtq = moverYLanzarAlgo(enemigos, Accion.ATACAR, false);
			} while (movAtq == EstadoMovAtq.TACLEADO || movAtq == EstadoMovAtq.SE_MOVIO
			|| movAtq == EstadoMovAtq.LANZO_HECHIZO);
			EstadoDistancia acercarse = EstadoDistancia.NO_PUEDE;
			do {
				if (movAtq != EstadoMovAtq.NO_TIENE_HECHIZOS) {
					fullAtaqueSioSi(enemigos);
				}
			} while (movAtq != EstadoMovAtq.NO_PUEDE_MOVERSE && (acercarse = acercarseA(enemigos, true,
			true)) == EstadoDistancia.ACERCARSE);
			siEsInvisible(acercarse);
		}
	}
	
	private void tipo_1() {
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> amigos = ordenLuchadores(_lanzador.getParamEquipoAliado(), Orden.NIVEL_MAS_A_MENOS);
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		final float porcPDV = _lanzador.getPorcPDV();
		final boolean azar = Formulas.getRandomBoolean();
		if (porcPDV > 50 || azar) {
			EstadoMovAtq movAtq;
			while (buffeaSiEsPosible(amigos)) {}
			if (azar) {
				fullAtaqueSioSi(enemigos);
				invocarSiEsPosible(enemigos);
			} else {
				invocarSiEsPosible(enemigos);
				fullAtaqueSioSi(enemigos);
			}
			do {
				movAtq = moverYLanzarAlgo(enemigos, Accion.ATACAR, false);
			} while (movAtq == EstadoMovAtq.TACLEADO || movAtq == EstadoMovAtq.SE_MOVIO
			|| movAtq == EstadoMovAtq.LANZO_HECHIZO);
			teleportSiEsPosible(enemigos);
			while (curaSiEsPosible(amigos)) {}
			EstadoDistancia acercarse = EstadoDistancia.NO_PUEDE;
			do {
				if (movAtq != EstadoMovAtq.NO_TIENE_HECHIZOS) {
					fullAtaqueSioSi(enemigos);
				}
			} while (movAtq != EstadoMovAtq.NO_PUEDE_MOVERSE && (acercarse = acercarseA(enemigos, true,
			true)) == EstadoDistancia.ACERCARSE);
			while (trampearSiEsPosible()) {}
			siEsInvisible(acercarse);
		} else {
			while (curaSiEsPosible(amigos)) {}
			while (buffeaSiEsPosible(amigos)) {}
			while (trampearSiEsPosible()) {}
			EstadoMovAtq movAtq;
			if (azar) {
				fullAtaqueSioSi(enemigos);
				invocarSiEsPosible(enemigos);
			} else {
				invocarSiEsPosible(enemigos);
				fullAtaqueSioSi(enemigos);
			}
			do {
				movAtq = moverYLanzarAlgo(enemigos, Accion.ATACAR, false);
			} while (movAtq == EstadoMovAtq.TACLEADO || movAtq == EstadoMovAtq.SE_MOVIO
			|| movAtq == EstadoMovAtq.LANZO_HECHIZO);
			EstadoDistancia acercarse = EstadoDistancia.NO_PUEDE;
			do {
				if (movAtq != EstadoMovAtq.NO_TIENE_HECHIZOS) {
					fullAtaqueSioSi(enemigos);
				}
			} while (movAtq != EstadoMovAtq.NO_PUEDE_MOVERSE && (acercarse = acercarseA(enemigos, true,
			true)) == EstadoDistancia.ACERCARSE);
			siEsInvisible(acercarse);
		}
	}
	
	private void tipo_2() { // esfera xelor
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		fullAtaqueSioSi(enemigos);
	}
	
	private void tipo_3() {
		// mobs salas de entrenamiento
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		EstadoDistancia acercarse = EstadoDistancia.NO_PUEDE;
		do {
			fullAtaqueSioSi(enemigos);
		} while ((acercarse = acercarseA(enemigos, true, true)) == EstadoDistancia.ACERCARSE);
		siEsInvisible(acercarse);
	}
	
	private void tipo_4() { // tofu, prespic
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> amigos = ordenLuchadores(_lanzador.getParamEquipoAliado(), Orden.NIVEL_MAS_A_MENOS);
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		EstadoMovAtq tempMovAtq = EstadoMovAtq.NULO;
		// aqui comienza todo
		EstadoMovAtq movAtq;
		do {
			movAtq = moverYLanzarAlgo(amigos, Accion.BOOSTEAR, false);
			EstadoMovAtq eEnemigos = moverYLanzarAlgo(enemigos, Accion.BOOSTEAR, false);
			if (eEnemigos == EstadoMovAtq.LANZO_HECHIZO) {
				tempMovAtq = EstadoMovAtq.LANZO_HECHIZO;
			}
			if (eEnemigos == EstadoMovAtq.TACLEADO || eEnemigos == EstadoMovAtq.SE_MOVIO
			|| eEnemigos == EstadoMovAtq.LANZO_HECHIZO) {
				movAtq = eEnemigos;
			}
		} while (movAtq == EstadoMovAtq.TACLEADO || movAtq == EstadoMovAtq.SE_MOVIO
		|| movAtq == EstadoMovAtq.LANZO_HECHIZO);
		do {
			movAtq = moverYLanzarAlgo(enemigos, Accion.ATACAR, false);
			if (movAtq == EstadoMovAtq.LANZO_HECHIZO) {
				tempMovAtq = EstadoMovAtq.LANZO_HECHIZO;
			}
		} while (movAtq == EstadoMovAtq.TACLEADO || movAtq == EstadoMovAtq.SE_MOVIO
		|| movAtq == EstadoMovAtq.LANZO_HECHIZO);
		if (tempMovAtq == EstadoMovAtq.NULO) {
			if (acercarseA(enemigos, true, true) == EstadoDistancia.ACERCARSE) {
				if (movAtq != EstadoMovAtq.NO_TIENE_HECHIZOS) {
					if (fullAtaqueSioSi(enemigos)) {
						tempMovAtq = EstadoMovAtq.LANZO_HECHIZO;
					}
				}
			}
		}
		while (buffeaSiEsPosible(amigos)) {}
		if (tempMovAtq == EstadoMovAtq.LANZO_HECHIZO) {
			while (alejarseDeEnemigo()) {}
		}
	}
	
	private void tipo_5() { // la bloqueadora
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		EstadoDistancia acercarse = EstadoDistancia.NO_PUEDE;
		while ((acercarse = acercarseA(enemigos, false, false)) == EstadoDistancia.ACERCARSE) {}
		siEsInvisible(acercarse);
	}
	
	private void tipo_6() {// la hinchable, conejo
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> amigos = ordenLuchadores(_lanzador.getParamEquipoAliado(), Orden.NIVEL_MAS_A_MENOS);
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		invocarSiEsPosible(enemigos);
		do {
			EstadoMovAtq movAtq;
			do {
				movAtq = moverYLanzarAlgo(amigos, Accion.BOOSTEAR, false);
			} while (movAtq == EstadoMovAtq.TACLEADO || movAtq == EstadoMovAtq.SE_MOVIO
			|| movAtq == EstadoMovAtq.LANZO_HECHIZO);
			do {
				movAtq = moverYLanzarAlgo(amigos, Accion.CURAR, false);
			} while (movAtq == EstadoMovAtq.TACLEADO || movAtq == EstadoMovAtq.SE_MOVIO
			|| movAtq == EstadoMovAtq.LANZO_HECHIZO);
		} while (acercarseA(amigos, false, false) == EstadoDistancia.ACERCARSE);
	}
	
	private void tipo_7() {// gatake, pala animada, jabali, crujidor
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		EstadoDistancia acercarse = EstadoDistancia.NO_PUEDE;
		do {
			while (buffeaSiEsPosible(null)) {}
			EstadoMovAtq movAtq;
			do {
				movAtq = moverYLanzarAlgo(enemigos, Accion.ATACAR, false);
			} while (movAtq == EstadoMovAtq.TACLEADO || movAtq == EstadoMovAtq.SE_MOVIO
			|| movAtq == EstadoMovAtq.LANZO_HECHIZO);
			while (buffeaSiEsPosible(enemigos)) {}
		} while ((acercarse = acercarseA(enemigos, false, false)) == EstadoDistancia.ACERCARSE);
		siEsInvisible(acercarse);
	}
	
	private void tipo_8() { // mochila animada
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> amigos = ordenLuchadores(_lanzador.getParamEquipoAliado(), Orden.NIVEL_MAS_A_MENOS);
		EstadoMovAtq movAtq;
		do {
			movAtq = moverYLanzarAlgo(amigos, Accion.BOOSTEAR, false);
		} while (movAtq == EstadoMovAtq.TACLEADO || movAtq == EstadoMovAtq.SE_MOVIO
		|| movAtq == EstadoMovAtq.LANZO_HECHIZO);
		if (movAtq == EstadoMovAtq.TIENE_HECHIZOS_SIN_LANZAR) {
			while (acercarseA(amigos, false, false) == EstadoDistancia.ACERCARSE) {}
		} else {
			while (alejarseDeEnemigo()) {}
		}
	}
	
	private void tipo_9() {// cofre animado, arbol de vida
		if (!_lanzador.puedeJugar()) {
			return;
		}
		while (lanzaHechizoAlAzar(null, Accion.BOOSTEAR)) {}
	}
	
	private void tipo_10() {// cascara explosiva
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		EstadoMovAtq movAtq;
		do {
			movAtq = moverYLanzarAlgo(enemigos, Accion.ATACAR, false);
		} while (movAtq == EstadoMovAtq.TACLEADO || movAtq == EstadoMovAtq.SE_MOVIO
		|| movAtq == EstadoMovAtq.LANZO_HECHIZO);
		while (buffeaSiEsPosible(null)) {}
	}
	
	private void tipo_11() { // chafer y chaferloko
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> todos = ordenLuchadores(3, Orden.PDV_MENOS_A_MAS, Orden.NADA);
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		while (buffeaSiEsPosible(null)) {} // auto-buff
		EstadoDistancia acercarse = EstadoDistancia.NO_PUEDE;
		do {
			fullAtaqueSioSi(todos);
		} while ((acercarse = acercarseA(enemigos, false, true)) == EstadoDistancia.ACERCARSE);
		siEsInvisible(acercarse);
	}
	
	private void tipo_12() {// kralamar
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		invocarSiEsPosible(enemigos);
		buffeaSiEsPosible(null);
		fullAtaqueSioSi(enemigos);
	}
	
	private void tipo_13() {// vasija
		if (!_lanzador.puedeJugar()) {
			return;
		}
		while (lanzaHechizoAlAzar(null, Accion.BOOSTEAR)) {} // auto boost
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		fullAtaqueSioSi(enemigos);
	}
	
	private void tipo_14() {// aguja buscadora
		if (!_lanzador.puedeJugar()) {
			return;
		}
		EstadoDistancia acercarse = EstadoDistancia.NO_PUEDE;
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		do {
			while (lanzaHechizoAlAzar(enemigos, Accion.ATACAR)) {} // ataca
		} while ((acercarse = acercarseA(enemigos, true, true)) == EstadoDistancia.ACERCARSE);
		siEsInvisible(acercarse);
	}
	
	private void tipo_15() {// IA para @fox discord
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> amigos = ordenLuchadores(_lanzador.getParamEquipoAliado(), Orden.NIVEL_MAS_A_MENOS);
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		while (buffeaSiEsPosible(amigos)) {}
		invocarSiEsPosible(enemigos);
		while (buffeaSiEsPosible(enemigos)) {}
		boolean ataco = false;
		if (fullAtaqueSioSi(enemigos)) {
			ataco = true;
		} else {
			if (acercarseA(enemigos, true, true) == EstadoDistancia.ACERCARSE) {
				if (fullAtaqueSioSi(enemigos)) {
					ataco = true;
				}
			}
		}
		while (buffeaSiEsPosible(amigos)) {}
		if (ataco) {
			while (alejarseDeEnemigo()) {}
		}
	}
	
	private void tipo_16() {// tentaculos
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> amigos = ordenLuchadores(_lanzador.getParamEquipoAliado(), Orden.NIVEL_MAS_A_MENOS);
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		EstadoMovAtq movAtq;
		invocarSiEsPosible(enemigos);
		fullAtaqueSioSi(enemigos);
		while (buffeaSiEsPosible(amigos)) {}
		do {
			movAtq = moverYLanzarAlgo(enemigos, Accion.ATACAR, false);
		} while (movAtq == EstadoMovAtq.TACLEADO || movAtq == EstadoMovAtq.SE_MOVIO
		|| movAtq == EstadoMovAtq.LANZO_HECHIZO);
		while (curaSiEsPosible(amigos)) {}
		EstadoDistancia acercarse = EstadoDistancia.NO_PUEDE;
		do {
			if (movAtq != EstadoMovAtq.NO_TIENE_HECHIZOS) {
				fullAtaqueSioSi(enemigos);
			}
		} while (movAtq != EstadoMovAtq.NO_PUEDE_MOVERSE && (acercarse = acercarseA(enemigos, true,
		true)) == EstadoDistancia.ACERCARSE);
		siEsInvisible(acercarse);
	}
	
	private void tipo_20() { // Prisma
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> amigos = ordenLuchadores(_lanzador.getParamEquipoAliado(), Orden.NIVEL_MAS_A_MENOS);
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		curaSiEsPosible(amigos);
		buffeaSiEsPosible(amigos);
		fullAtaqueSioSi(enemigos);
	}
	
	private void tipo_21() {// recaudador
		if (!_lanzador.puedeJugar()) {
			return;
		}
		final ArrayList<Luchador> amigos = ordenLuchadores(_lanzador.getParamEquipoAliado(), Orden.NIVEL_MAS_A_MENOS);
		final ArrayList<Luchador> enemigos = ordenLuchadores(_lanzador.getParamEquipoEnemigo(), Orden.PDV_MENOS_A_MAS,
		Orden.INVOS_ULTIMOS);
		final float porcPDV = _lanzador.getPorcPDV();
		if (porcPDV > 50) {
			EstadoMovAtq movAtq;
			buffeaSiEsPosible(amigos);
			fullAtaqueSioSi(enemigos);
			do {
				movAtq = moverYLanzarAlgo(enemigos, Accion.ATACAR, false);
			} while (movAtq == EstadoMovAtq.TACLEADO || movAtq == EstadoMovAtq.SE_MOVIO
			|| movAtq == EstadoMovAtq.LANZO_HECHIZO);
			while (curaSiEsPosible(amigos)) {}
			EstadoDistancia acercarse = EstadoDistancia.NO_PUEDE;
			do {
				if (movAtq != EstadoMovAtq.NO_TIENE_HECHIZOS) {
					fullAtaqueSioSi(enemigos);
				}
			} while (movAtq != EstadoMovAtq.NO_PUEDE_MOVERSE && (acercarse = acercarseA(enemigos, true,
			true)) == EstadoDistancia.ACERCARSE);
			siEsInvisible(acercarse);
		} else {
			EstadoMovAtq movAtq;
			while (curaSiEsPosible(null)) {}
			buffeaSiEsPosible(amigos);
			fullAtaqueSioSi(enemigos);
			do {
				movAtq = moverYLanzarAlgo(enemigos, Accion.ATACAR, false);
			} while (movAtq == EstadoMovAtq.TACLEADO || movAtq == EstadoMovAtq.SE_MOVIO
			|| movAtq == EstadoMovAtq.LANZO_HECHIZO);
			EstadoDistancia acercarse = EstadoDistancia.NO_PUEDE;
			do {
				if (movAtq != EstadoMovAtq.NO_TIENE_HECHIZOS) {
					fullAtaqueSioSi(enemigos);
				}
			} while (movAtq != EstadoMovAtq.NO_PUEDE_MOVERSE && (acercarse = acercarseA(enemigos, true,
			true)) == EstadoDistancia.ACERCARSE);
			siEsInvisible(acercarse);
		}
	}
	
	private void siEsInvisible(EstadoDistancia acercarse) {
		if (acercarse == EstadoDistancia.INVISIBLE) {
			if (Formulas.getRandomInt(1, 3) == 2) {
				while (acercarseAInvis()) {}
			} else {
				while (alejarseDeEnemigo()) {}
			}
		}
	}
	
	private boolean objetivoApto(Luchador objetivo, boolean pInvi) {
		if (objetivo == null) {
			return true;
		}
		if (objetivo.estaMuerto() || (pInvi && objetivo.esInvisible(_lanzador.getID()))) {
			return false;
		}
		return true;
	}
	
	private ArrayList<StatHechizo> hechizosLanzables() {
		ArrayList<StatHechizo> disponibles = new ArrayList<StatHechizo>();
		if (_lanzador.getHechizos() == null) {
			return disponibles;
		}
		for (final StatHechizo SH : _lanzador.getHechizos().values()) {
			if (SH == null) {
				continue;
			}
			try {
				if (MainServidor.MODO_DEBUG) {
					System.out.println("hechizosLanzables() -> Hechizo " + SH.getHechizo().getNombre());
				}
				// filtra los hechizos q esten con tiempo o faltos de PA o algo por el estilo
				if (_pelea.filtraHechizoDisponible(_lanzador, SH, 0) != EstadoLanzHechizo.PODER) {
					continue;
				}
				disponibles.add(SH);
				for (EfectoHechizo EH : SH.getEfectosNormales()) {
					if (Constantes.estimaDaño(EH.getEfectoID()) == 1) {
						if (_lanzador.getDistMinAtq() == -1) {
							_lanzador.setDistMinAtq(SH.getMinAlc());
						}
						_lanzador.setDistMinAtq(Math.min(SH.getMinAlc(), _lanzador.getDistMinAtq()));
						break;
					}
				}
			} catch (Exception e) {}
		}
		return disponibles;
	}
	
	private ArrayList<Luchador> ordenLuchadores(final int equipo, final Orden... orden) {
		final ArrayList<Luchador> temporales = new ArrayList<Luchador>();
		for (final Luchador luch : _pelea.luchadoresDeEquipo(equipo)) {
			if (!objetivoApto(luch, true)) {
				continue;
			}
			temporales.add(luch);
		}
		ordena(temporales, orden);
		return temporales;
	}
	
	private void ordenarLuchMasCercano(final ArrayList<Luchador> preLista, ArrayList<Short> celdas, Orden... orden) {
		ArrayList<Luchador> alejados = new ArrayList<Luchador>();
		ArrayList<Luchador> cercanos = new ArrayList<Luchador>();
		for (final Luchador objetivo : preLista) {
			if (!objetivoApto(objetivo, true)) {
				continue;
			}
			if (celdas.contains(objetivo.getCeldaPelea().getID())) {
				cercanos.add(objetivo);
			} else {
				alejados.add(objetivo);
			}
		}
		ordena(cercanos, orden);
		ordena(alejados, orden);
		preLista.clear();
		preLista.addAll(cercanos);
		preLista.addAll(alejados);
		alejados = null;
		cercanos = null;
	}
	
	private void ordenarLuchVulnerables(final ArrayList<Luchador> preLista) {
		ArrayList<Luchador> vulnerables = new ArrayList<Luchador>();
		ArrayList<Luchador> invulnerables = new ArrayList<Luchador>();
		for (final Luchador objetivo : preLista) {
			if (!objetivoApto(objetivo, true)) {
				continue;
			}
			if (esInvulnerable(objetivo)) {
				invulnerables.add(objetivo);
			} else {
				vulnerables.add(objetivo);
			}
		}
		preLista.clear();
		preLista.addAll(vulnerables);
		preLista.addAll(invulnerables);
		vulnerables = null;
		invulnerables = null;
	}
	
	private Luchador enemigoMasCercano(final ArrayList<Luchador> objetivos) {
		int dist = 1000;
		Luchador tempObjetivo = null;
		for (final Luchador objetivo : objetivos) {
			if (!objetivoApto(objetivo, true)) {
				continue;
			}
			try {
				final int d = Camino.distanciaDosCeldas(_pelea.getMapaCopia(), _lanzador.getCeldaPelea().getID(), objetivo
				.getCeldaPelea().getID());
				if (d < dist) {
					dist = d;
					tempObjetivo = objetivo;
				}
			} catch (final Exception e) {}
		}
		return tempObjetivo;
	}
	
	private boolean alejarseDeEnemigo() {
		if (!_lanzador.puedeJugar()) {
			return false;
		}
		if (_lanzador.getPMRestantes() <= 0) {
			return false;
		}
		final short celdaIDLanzador = _lanzador.getCeldaPelea().getID();
		final ArrayList<Short> celdasMovimiento = Camino.celdasDeMovimiento(_pelea, _lanzador.getCeldaPelea(), true, true,
		null);
		celdasMovimiento.add(celdaIDLanzador);
		final ArrayList<Luchador> enemigos = new ArrayList<>();
		for (final Luchador blanco : _pelea.luchadoresDeEquipo(_lanzador.getParamEquipoEnemigo())) {
			if (!objetivoApto(blanco, false)) {
				continue;
			}
			enemigos.add(blanco);
		}
		final Mapa mapa = _pelea.getMapaCopia();
		int distEntreTodos = -1;
		short celdaIdeal = -1;
		for (short celdaTemp : celdasMovimiento) {
			int distTemp = 0;
			for (final Luchador blanco : enemigos) {
				distTemp += Camino.distanciaDosCeldas(mapa, celdaTemp, blanco.getCeldaPelea().getID());
			}
			if (distTemp >= distEntreTodos) {
				distEntreTodos = distTemp;
				celdaIdeal = celdaTemp;
			}
		}
		if (celdaIdeal == -1 || celdaIdeal == celdaIDLanzador) {
			return false;
		}
		final Duo<Integer, ArrayList<Celda>> pathCeldas = Camino.getPathPelea(mapa, celdaIDLanzador, celdaIdeal, -1, null,
		false);
		if (pathCeldas == null) {
			return false;
		}
		final ArrayList<Celda> path = pathCeldas._segundo;
		final ArrayList<Celda> finalPath = new ArrayList<Celda>();
		for (int a = 0; a < _lanzador.getPMRestantes(); a++) {
			if (path.size() == a || path.get(a).getPrimerLuchador() != null) {
				break;
			}
			finalPath.add(path.get(a));
		}
		String pathStr = Camino.getPathComoString(mapa, finalPath, celdaIDLanzador, true);
		if (pathStr.isEmpty()) {
			return false;
		}
		String resultado = _pelea.intentarMoverse(_lanzador, pathStr.toString(), 0, null);
		if (resultado.equals("stop")) {
			return false;
		}
		return resultado.equals("ok");
	}
	
	// private static boolean mueveLoMasLejosPosible(final Pelea pelea, final Luchador lanzador) {
	// if (!lanzador.puedeJugar()) {
	// return false;
	// }
	// final int PM = pelea.getTempPM();
	// if (PM <= 0) {
	// return false;
	// }
	//
	// final short celdaIDLanzador = lanzador.getCeldaPelea().getID();
	// final Mapa mapa = pelea.getMapaCopia();
	// final short dist[] = {1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}, celda[] =
	// {0, 0, 0, 0, 0, 0, 0,
	// 0, 0, 0};
	// for (int i = 0; i < 10; i++) {
	// for (final Luchador blanco : pelea.luchadoresDeEquipo(lanzador.getParamEquipoEnemigo())) {
	// if (blanco.estaMuerto()) {
	// continue;
	// }
	// final short celdaEnemigo = blanco.getCeldaPelea().getID();
	// if (celdaEnemigo == celda[0] || celdaEnemigo == celda[1] || celdaEnemigo == celda[2]
	// || celdaEnemigo == celda[3] || celdaEnemigo == celda[4] || celdaEnemigo == celda[5] ||
	// celdaEnemigo == celda[6]
	// || celdaEnemigo == celda[7] || celdaEnemigo == celda[8] || celdaEnemigo == celda[9]) {
	// continue;
	// }
	// short d = 0;
	// d = Camino.distanciaDosCeldas(mapa, celdaIDLanzador, celdaEnemigo);
	// if (d == 0) {
	// continue;
	// }
	// if (d < dist[i]) {
	// dist[i] = d;
	// celda[i] = celdaEnemigo;
	// }
	// if (dist[i] == 1000) {
	// dist[i] = 0;
	// celda[i] = celdaIDLanzador;
	// }
	// }
	// }
	// if (dist[0] == 0) {
	// return false;
	// }
	// final int dist2[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	// final byte ancho = mapa.getAncho(), alto = mapa.getAlto();
	// short celdaInicio = celdaIDLanzador;
	// short celdaDestino = celdaIDLanzador;
	// final short ultCelda = Camino.ultimaCeldaID(mapa);
	// final int valor = Formulas.getRandomValor(0, 3);
	// int[] movidas;
	// if (valor == 0) {
	// movidas = new int[]{0, 1, 2, 3};
	// } else if (valor == 1) {
	// movidas = new int[]{1, 2, 3, 0};
	// } else if (valor == 2) {
	// movidas = new int[]{2, 3, 0, 1};
	// } else {
	// movidas = new int[]{3, 0, 1, 2};
	// }
	// for (int i = 0; i <= PM; i++) {
	// if (celdaDestino > 0) {
	// celdaInicio = celdaDestino;
	// }
	// short celdaTemporal = celdaInicio;
	// int infl = 0, inflF = 0;
	// for (final int x : movidas) {
	// switch (x) {
	// case 0 :
	// celdaTemporal = (short) (celdaTemporal + ancho);
	// break;
	// case 1 :
	// celdaTemporal = (short) (celdaInicio + (ancho - 1));
	// break;
	// case 2 :
	// celdaTemporal = (short) (celdaInicio - ancho);
	// break;
	// case 3 :
	// celdaTemporal = (short) (celdaInicio - (ancho - 1));
	// break;
	// }
	// infl = 0;
	// for (int a = 0; a < 10 && dist[a] != 0; a++) {
	// dist2[a] = Camino.distanciaDosCeldas(mapa, celdaTemporal, celda[a]);
	// if (dist2[a] > dist[a]) {
	// infl++;
	// }
	// }
	// if (infl > inflF && celdaTemporal > 0 && celdaTemporal < ultCelda
	// && !Camino.celdaSalienteLateral(ancho, alto, celdaDestino, celdaTemporal)
	// && mapa.getCelda(celdaTemporal).esCaminable(true)) {
	// inflF = infl;
	// celdaDestino = celdaTemporal;
	// }
	// }
	// }
	// if (celdaDestino < 0 || celdaDestino > ultCelda || celdaDestino == celdaIDLanzador
	// || !mapa.getCelda(celdaDestino).esCaminable(true)) {
	// return false;
	// }
	// final ArrayList<Celda> path = Camino.pathMasCortoEntreDosCeldas(mapa, celdaIDLanzador,
	// celdaDestino, 0);
	// if (path == null) {
	// return false;
	// }
	// final ArrayList<Celda> finalPath = new ArrayList<Celda>();
	// for (int a = 0; a < pelea.getTempPM(); a++) {
	// if (path.size() == a) {
	// break;
	// }
	// finalPath.add(path.get(a));
	// }
	// final StringBuilder pathStr = new StringBuilder();
	// try {
	// short tempCeldaID = celdaIDLanzador;
	// for (final Celda c : finalPath) {
	// final char d = Camino.dirEntreDosCeldas(mapa, tempCeldaID, c.getID(), true);
	// if (d == 0) {
	// return false;
	// }
	// if (finalPath.indexOf(c) != 0) {
	// pathStr.append(Encriptador.celdaIDACodigo(tempCeldaID));
	// }
	// pathStr.append(d);
	// tempCeldaID = c.getID();
	// }
	// if (tempCeldaID != celdaIDLanzador) {
	// pathStr.append(Encriptador.celdaIDACodigo(tempCeldaID));
	// }
	// } catch (final Exception e) {
	// e.printStackTrace();
	// }
	// String resultado = pelea.intentaMoverseLuchador(lanzador, pathStr.toString(), 0);
	// if (resultado.equals("stop")) {
	// return mueveLoMasLejosPosible(pelea, lanzador);
	// }
	// return resultado.equals("ok");
	// }
	//
	private boolean acercarseAInvis() {
		if (!_lanzador.puedeJugar()) {
			return false;
		}
		if (_lanzador.getPMRestantes() <= 0) {
			return false;
		}
		final ArrayList<Luchador> enemigos = new ArrayList<>();
		for (final Luchador blanco : _pelea.luchadoresDeEquipo(_lanzador.getParamEquipoEnemigo())) {
			if (!objetivoApto(blanco, false)) {
				continue;
			}
			enemigos.add(blanco);
		}
		final Mapa mapa = _pelea.getMapaCopia();
		final short celdaIDLanzador = _lanzador.getCeldaPelea().getID();
		final ArrayList<Short> celdasMovimiento = Camino.celdasDeMovimiento(_pelea, _lanzador.getCeldaPelea(), false, false,
		null);
		if (celdasMovimiento.isEmpty()) {
			return false;
		}
		celdasMovimiento.add(_lanzador.getCeldaPelea().getID());
		//
		ArrayList<Luchador> tempObjetivos = new ArrayList<>();
		tempObjetivos.addAll(enemigos);
		short tempCeldaID = -1;
		int dist = 1000;
		int repeticiones = 100;
		final ArrayList<Celda> path = new ArrayList<>();
		for (final Luchador objetivo : tempObjetivos) {
			if (!objetivoApto(objetivo, false)) {
				continue;
			}
			if (objetivo == _lanzador) {
				continue;
			}
			if (objetivo.esEstatico() && objetivo.getEquipoBin() == _lanzador.getEquipoBin()) {
				continue;
			}
			short celdaTempObj = objetivo.getCeldaPelea().getID();
			final Duo<Integer, ArrayList<Celda>> pathTemp = Camino.getPathPelea(mapa, celdaIDLanzador, celdaTempObj, -1, null,
			false);
			if (pathTemp == null || pathTemp._segundo.isEmpty()) {
				tempCeldaID = -2;
				continue;
			} else if (pathTemp._primero < repeticiones) {
				celdaTempObj = pathTemp._segundo.get(pathTemp._segundo.size() - 1).getID();
				if (celdasMovimiento.contains(objetivo.getCeldaPelea().getID()) && pathTemp._segundo.size() <= _lanzador
				.getPMRestantes() && pathTemp._primero == 0) {
					path.clear();
					path.addAll(pathTemp._segundo);
					tempCeldaID = celdaTempObj;
					// break;
				} else {
					final int d = Camino.distanciaDosCeldas(mapa, celdaIDLanzador, celdaTempObj);
					if (d < dist || pathTemp._primero < repeticiones) {
						path.clear();
						path.addAll(pathTemp._segundo);
						tempCeldaID = celdaTempObj;
						dist = d;
					}
				}
				repeticiones = pathTemp._primero;
				break;
			}
		}
		if (tempCeldaID == -1) {
			return false;
		} else if (tempCeldaID == -2) {// (-2) el path es nulo porq no hay camino
			return false;
		}
		final ArrayList<Celda> finalPath = new ArrayList<Celda>();
		for (int a = 0; a < _lanzador.getPMRestantes(); a++) {
			if (path.size() == a || path.get(a).getPrimerLuchador() != null) {
				break;
			}
			finalPath.add(path.get(a));
		}
		String pathStr = Camino.getPathComoString(mapa, finalPath, celdaIDLanzador, true);
		if (pathStr.isEmpty()) {
			return false;
		}
		String resultado = _pelea.intentarMoverse(_lanzador, pathStr.toString(), 0, null);
		switch (resultado) {
			case "stop" :
			case "ok" :
				return true;
			case "tacleado" :
				return true;
		}
		return false;
	}
	
	private EstadoDistancia acercarseA(ArrayList<Luchador> objetivos, final boolean masCercano,
	final boolean paraAtacar) {
		if (!_lanzador.puedeJugar()) {
			return EstadoDistancia.NO_PUEDE;
		}
		if (_lanzador.getPMRestantes() <= 0) {
			return EstadoDistancia.NO_PUEDE;
		}
		if (objetivos.isEmpty()) {
			return EstadoDistancia.INVISIBLE;
		}
		final Mapa mapa = _pelea.getMapaCopia();
		final short celdaIDLanzador = _lanzador.getCeldaPelea().getID();
		final ArrayList<Short> celdasMovimiento = Camino.celdasDeMovimiento(_pelea, _lanzador.getCeldaPelea(), false, false,
		null);
		if (celdasMovimiento.isEmpty()) {
			return EstadoDistancia.NO_PUEDE;
		}
		celdasMovimiento.add(_lanzador.getCeldaPelea().getID());
		//
		ArrayList<Luchador> tempObjetivos = new ArrayList<>();
		tempObjetivos.addAll(objetivos);
		if (masCercano) {
			ordenarLuchMasCercano(tempObjetivos, celdasMovimiento, Orden.PDV_MENOS_A_MAS);
		}
		if (paraAtacar) {
			ordenarLuchVulnerables(tempObjetivos);
		}
		short tempCeldaID = -1;
		short celdaLuchObjetivo = -1;
		int dist = 1000;
		int repeticiones = 100;
		final ArrayList<Celda> path = new ArrayList<>();
		for (final Luchador objetivo : tempObjetivos) {
			if (!objetivoApto(objetivo, true)) {
				continue;
			}
			if (objetivo == _lanzador) {
				continue;
			}
			if (objetivo.esEstatico() && objetivo.getEquipoBin() == _lanzador.getEquipoBin()) {
				continue;
			}
			short celdaTempObj = objetivo.getCeldaPelea().getID();
			final Duo<Integer, ArrayList<Celda>> pathTemp = Camino.getPathPelea(mapa, celdaIDLanzador, celdaTempObj, -1, null,
			false);
			if (pathTemp == null || pathTemp._segundo.isEmpty()) {
				tempCeldaID = -2;
				continue;
			} else if (pathTemp._primero < repeticiones) {
				celdaTempObj = pathTemp._segundo.get(pathTemp._segundo.size() - 1).getID();
				if (celdasMovimiento.contains(objetivo.getCeldaPelea().getID()) && pathTemp._segundo.size() <= _lanzador
				.getPMRestantes() && pathTemp._primero == 0) {
					path.clear();
					path.addAll(pathTemp._segundo);
					tempCeldaID = celdaTempObj;
					celdaLuchObjetivo = objetivo.getCeldaPelea().getID();
					// break;
				} else {
					final int d = Camino.distanciaDosCeldas(mapa, celdaIDLanzador, celdaTempObj);
					if (d < dist || pathTemp._primero < repeticiones) {
						path.clear();
						path.addAll(pathTemp._segundo);
						tempCeldaID = celdaTempObj;
						celdaLuchObjetivo = objetivo.getCeldaPelea().getID();
						dist = d;
					}
				}
				repeticiones = pathTemp._primero;
				break;
			}
		}
		if (tempCeldaID == -1) {
			return EstadoDistancia.NO_PUEDE;
		} else if (tempCeldaID == -2) {// (-2) el path es nulo porq no hay camino
			return EstadoDistancia.NO_PUEDE;
		}
		final ArrayList<Celda> finalPath = new ArrayList<Celda>();
		for (int a = 0; a < _lanzador.getPMRestantes(); a++) {
			if (path.size() == a || path.get(a).getPrimerLuchador() != null) {
				break;
			}
			if (paraAtacar) {
				int d = Camino.distanciaDosCeldas(mapa, path.get(a).getID(), celdaLuchObjetivo);
				if (_lanzador.getDistMinAtq() != -1 && d < _lanzador.getDistMinAtq()) {
					break;
				}
			}
			finalPath.add(path.get(a));
		}
		String pathStr = Camino.getPathComoString(mapa, finalPath, celdaIDLanzador, true);
		if (pathStr.isEmpty()) {
			return EstadoDistancia.NO_PUEDE;
		}
		String resultado = _pelea.intentarMoverse(_lanzador, pathStr.toString(), 0, null);
		switch (resultado) {
			case "stop" :
			case "ok" :
				return EstadoDistancia.ACERCARSE;
			case "tacleado" :
				return EstadoDistancia.TACLEADO;
		}
		return EstadoDistancia.NO_PUEDE;
	}
	
	private boolean lanzaHechizoAlAzar(ArrayList<Luchador> objetivos, Accion accion) {
		if (!_lanzador.puedeJugar()) {
			return false;
		}
		if (objetivos == null || objetivos.isEmpty()) {
			objetivos = new ArrayList<Luchador>();
			objetivos.add(_lanzador);
		}
		for (final Luchador objetivo : objetivos) {
			if (!objetivoApto(objetivo, true)) {
				continue;
			}
			final StatHechizo SH = hechizoAlAzar(objetivo);
			if (SH == null) {
				continue;
			}
			if (accion == Accion.ATACAR && tieneReenvio(_lanzador, objetivo, SH)) {
				continue;
			}
			if (_pelea.intentarLanzarHechizo(_lanzador, SH, objetivo.getCeldaPelea(), true) != EstadoLanzHechizo.PODER) {
				continue;
			}
			return true;
		}
		return false;
	}
	
	private void setCeldasMovimiento() {
		if (_refrescarMov) {
			_celdasMovimiento = Camino.celdasDeMovimiento(_pelea, _lanzador.getCeldaPelea(), true, true, _lanzador);
			_refrescarMov = false;
		}
	}
	
	private EstadoMovAtq moverYLanzarAlgo(ArrayList<Luchador> objetivos, final Accion buffInvoAtaq, boolean obviarTacle) {
		if (objetivos == null || !_lanzador.puedeJugar()) {
			return EstadoMovAtq.NO_HIZO_NADA;
		}
		StatHechizo filtroPorHechizo = null;
		for (final StatHechizo SH2 : hechizosLanzables()) {
			for (Luchador objetivo : objetivos) {
				int filtroIA = getFiltroIA(SH2, objetivo, buffInvoAtaq);
				if (filtroIA < 0) {
					continue;
				}
				filtroPorHechizo = SH2;
				break;
			}
			if (filtroPorHechizo != null) {
				break;
			}
		}
		if (filtroPorHechizo == null) {
			return EstadoMovAtq.NO_TIENE_HECHIZOS;
		}
		ArrayList<Luchador> tempObjetivos = new ArrayList<>();
		tempObjetivos.addAll(objetivos);
		if (buffInvoAtaq == Accion.ATACAR) {
			Collections.sort(tempObjetivos, new CompPDVMenosMas());
		}
		Collections.sort(tempObjetivos, new CompInvosUltimos());
		// -----
		Mapa mapa = _pelea.getMapaCopia();
		StatHechizo SH = null;
		Celda celdaObjetivoLanz = null;
		short celdaIDLanzador = _lanzador.getCeldaPelea().getID();
		short celdaDestinoMov = 0;
		int influenciaMax = -1000000000;
		int distancia = 10000;
		final ArrayList<Short> tempCeldasMovPrioridad = new ArrayList<>();
		tempCeldasMovPrioridad.add(_lanzador.getCeldaPelea().getID());
		setCeldasHechizoCeldaLanz();
		setCeldasMovimiento();
		// ordena por prioridad las celdas segun los objetivos
		if (MainServidor.NIVEL_INTELIGENCIA_ARTIFICIAL > INTELIGENCIA_ORDENAR_PRIORIDAD_OBJETIVOS) {
			for (final Luchador objetivo : tempObjetivos) {
				if (!objetivoApto(objetivo, true)) {
					continue;
				}
				if (buffInvoAtaq == Accion.CURAR && objetivo.getPorcPDV() > PDV_MINIMO_CURAR) {
					continue;
				}
				Duo<Integer, ArrayList<Celda>> pathTemp = Camino.getPathPelea(mapa, _lanzador.getCeldaPelea().getID(), objetivo
				.getCeldaPelea().getID(), -1, _lanzador, true);
				if (pathTemp == null) {
					continue;
				}
				for (Celda c : pathTemp._segundo) {
					if (!_celdasMovimiento.contains(c.getID()) || tempCeldasMovPrioridad.contains(c.getID())) {
						continue;
					}
					tempCeldasMovPrioridad.add(c.getID());
				}
			}
		}
		for (short c : _celdasMovimiento) {
			if (tempCeldasMovPrioridad.contains(c)) {
				continue;
			}
			tempCeldasMovPrioridad.add(c);
		}
		for (short tempCeldaLanz : tempCeldasMovPrioridad) {
			Map<StatHechizo, Map<Celda, ArrayList<Luchador>>> map = _celdasHechizos.get(tempCeldaLanz);
			if (map == null) {
				map = getObjHechDesdeCeldaLanz(tempCeldaLanz);
				if (map == null || map.isEmpty()) {
					continue;
				}
				_celdasHechizos.put(tempCeldaLanz, map);
			}
			// }
			// if (_celdasHechizos.isEmpty()) {
			// return EstadoMovAtq.NO_TIENE_HECHIZOS;
			// }
			// for (short tempCeldaLanz : tempCeldasMovPrioridad) {
			int prioridadObj = tempObjetivos.size();
			for (final Luchador objetivo : tempObjetivos) {
				prioridadObj--;
				if (!objetivoApto(objetivo, true)) {
					continue;
				}
				Duo<Integer, Duo<StatHechizo, Celda>> duo = null;
				if (buffInvoAtaq == Accion.ATACAR) {
					duo = mejorAtaque(tempCeldaLanz, objetivo, map);
				} else if (buffInvoAtaq == Accion.BOOSTEAR) {
					duo = mejorBuff(tempCeldaLanz, objetivo, map);
				} else if (buffInvoAtaq == Accion.CURAR) {
					duo = mejorCura(tempCeldaLanz, objetivo, map);
				}
				if (duo == null || duo._primero <= 0) {
					continue;
				}
				int tempInf = duo._primero + prioridadObj;
				// esto era un antiguo codigo
				if (SH == null || tempInf > influenciaMax) {
					influenciaMax = tempInf;
					SH = duo._segundo._primero;
					celdaObjetivoLanz = duo._segundo._segundo;
					celdaDestinoMov = tempCeldaLanz;
					if (MainServidor.NIVEL_INTELIGENCIA_ARTIFICIAL <= INTELIGENCIA_COMPARAR_INF_OBJETIVOS) {
						break;
					}
					if (MainServidor.NIVEL_INTELIGENCIA_ARTIFICIAL > INTELIGENCIA_COMPARAR_INF_DIST_OBJETIVOS) {
						distancia = Camino.distanciaDosCeldas(mapa, celdaIDLanzador, tempCeldaLanz);
					}
					//
					// objetivo = tempObjetivo;
				}
				if (MainServidor.NIVEL_INTELIGENCIA_ARTIFICIAL > INTELIGENCIA_COMPARAR_INF_DIST_OBJETIVOS) {
					if (tempInf == influenciaMax && Camino.distanciaDosCeldas(mapa, celdaIDLanzador, tempCeldaLanz) < distancia) {
						influenciaMax = tempInf;
						SH = duo._segundo._primero;
						celdaObjetivoLanz = duo._segundo._segundo;
						celdaDestinoMov = tempCeldaLanz;
						distancia = Camino.distanciaDosCeldas(mapa, celdaIDLanzador, tempCeldaLanz);
						// objetivo = tempObjetivo;
					}
				}
				// else
			}
			if (MainServidor.NIVEL_INTELIGENCIA_ARTIFICIAL <= INTELIGENCIA_COMPARAR_INF_DIST_OBJETIVOS) {
				if (SH != null) {
					break;
				}
			}
		}
		if (SH == null) {
			return EstadoMovAtq.TIENE_HECHIZOS_SIN_LANZAR;
		}
		if (celdaDestinoMov == 0 || celdaDestinoMov == celdaIDLanzador) {
			// si no hay necesidad de moverse y solo se lanza el hechizo sobre la ubicacion
			EstadoLanzHechizo i = _pelea.intentarLanzarHechizo(_lanzador, SH, celdaObjetivoLanz, false);
			if (i == EstadoLanzHechizo.PODER) {
				return EstadoMovAtq.LANZO_HECHIZO;
			}
			switch (i) {
				case PODER :
					return EstadoMovAtq.LANZO_HECHIZO;
				case NO_TIENE_ALCANCE :
					return EstadoMovAtq.TIENE_HECHIZOS_SIN_LANZAR;
				default :
					return EstadoMovAtq.NO_HIZO_NADA;
			}
		}
		final Duo<Integer, ArrayList<Celda>> pathCeldas = Camino.getPathPelea(mapa, celdaIDLanzador, celdaDestinoMov, -1,
		null, false);
		if (pathCeldas == null) {
			return EstadoMovAtq.TIENE_HECHIZOS_SIN_LANZAR;
		}
		final ArrayList<Celda> path = pathCeldas._segundo;
		final ArrayList<Celda> finalPath = new ArrayList<Celda>();
		for (int a = 0; a < _lanzador.getPMRestantes() && a < path.size(); a++) {
			if (path.get(a).getPrimerLuchador() != null) {
				break;
			}
			// int d = Camino.distanciaDosCeldas(mapa, path.get(a).getID(), celdaDestinoMov);
			// if (lanzador._distMinAtq != -1 && d < lanzador._distMinAtq) {
			// break;
			// }
			finalPath.add(path.get(a));
		}
		String pathStr = Camino.getPathComoString(mapa, finalPath, celdaIDLanzador, true);
		if (pathStr.isEmpty()) {
			return EstadoMovAtq.TIENE_HECHIZOS_SIN_LANZAR;
		}
		final String resultado = _pelea.intentarMoverse(_lanzador, pathStr.toString(), 0, null);
		switch (resultado) {
			case "ok" :
				EstadoLanzHechizo i = _pelea.intentarLanzarHechizo(_lanzador, SH, celdaObjetivoLanz, false);
				if (i == EstadoLanzHechizo.PODER) {
					return EstadoMovAtq.LANZO_HECHIZO;
				}
			case "stop" :
				return EstadoMovAtq.SE_MOVIO;
		}
		return EstadoMovAtq.TIENE_HECHIZOS_SIN_LANZAR;
	}
	
	private boolean fullAtaqueSioSi(final ArrayList<Luchador> enemigos) {
		if (enemigos == null || !_lanzador.puedeJugar()) {
			return false;
		}
		boolean ataco = false;
		ArrayList<Luchador> objetivos = new ArrayList<>();
		objetivos.addAll(enemigos);
		Collections.sort(objetivos, new CompPDVMenosMas());
		Collections.sort(objetivos, new CompInvosUltimos());
		while (atacaSiEsPosible(objetivos) == EstadoLanzHechizo.PODER) {
			ataco = true;
		}
		objetivos = null;
		return ataco;
	}
	
	private EstadoLanzHechizo atacaSiEsPosible(ArrayList<Luchador> objetivos) {
		if (objetivos == null || !_lanzador.puedeJugar()) {
			return EstadoLanzHechizo.NO_PODER;
		}
		setCeldasHechizoCeldaLanz();
		// objetivos = listaLuchadoresMasCercano(pelea, lanzador, objetivos, Orden.PDV_MENOS_A_MAS);
		for (final Luchador objetivo : objetivos) {
			if (!objetivoApto(objetivo, true)) {
				continue;
			}
			short celdaLanzID = _lanzador.getCeldaPelea().getID();
			final Duo<Integer, Duo<StatHechizo, Celda>> duo = mejorAtaque(celdaLanzID, objetivo, _celdasHechizos.get(
			celdaLanzID));
			if (duo != null) {
				return _pelea.intentarLanzarHechizo(_lanzador, duo._segundo._primero, duo._segundo._segundo, true);
			}
		}
		return EstadoLanzHechizo.NO_PODER;// no pudo lanzar
	}
	
	private boolean buffeaSiEsPosible(ArrayList<Luchador> objetivos) {
		if (!_lanzador.puedeJugar()) {
			return false;
		}
		setCeldasHechizoCeldaLanz();
		if (objetivos == null || objetivos.isEmpty()) {
			objetivos = new ArrayList<Luchador>();
			objetivos.add(_lanzador);
		}
		// Collections.sort(objetivos, new CompNivelMasMenos());
		for (final Luchador objetivo : objetivos) {
			if (!objetivoApto(objetivo, true)) {
				continue;
			}
			short celdaLanzID = _lanzador.getCeldaPelea().getID();
			final Duo<Integer, Duo<StatHechizo, Celda>> duo = mejorBuff(celdaLanzID, objetivo, _celdasHechizos.get(
			celdaLanzID));
			if (duo != null) {
				return _pelea.intentarLanzarHechizo(_lanzador, duo._segundo._primero, duo._segundo._segundo,
				true) == EstadoLanzHechizo.PODER;
			}
		}
		return false;
	}
	
	private boolean curaSiEsPosible(ArrayList<Luchador> objetivos) {
		if (!_lanzador.puedeJugar()) {
			return false;
		}
		setCeldasHechizoCeldaLanz();
		ArrayList<Luchador> paraCurar = new ArrayList<Luchador>();
		if (objetivos == null || objetivos.isEmpty()) {
			paraCurar.add(_lanzador);
		} else {
			paraCurar.addAll(objetivos);
		}
		Collections.sort(paraCurar, new CompPDVMenosMas());
		for (final Luchador objetivo : paraCurar) {
			if (!objetivoApto(objetivo, true)) {
				continue;
			}
			if (objetivo.getPorcPDV() > PDV_MINIMO_CURAR) {
				continue;
			}
			short celdaLanzID = _lanzador.getCeldaPelea().getID();
			final Duo<Integer, Duo<StatHechizo, Celda>> duo = mejorCura(celdaLanzID, objetivo, _celdasHechizos.get(
			celdaLanzID));
			if (duo != null) {
				return _pelea.intentarLanzarHechizo(_lanzador, duo._segundo._primero, duo._segundo._segundo,
				true) == EstadoLanzHechizo.PODER;
			}
		}
		return false;
	}
	
	private boolean invocarSiEsPosible(final ArrayList<Luchador> objetivos) {
		if (objetivos == null || !_lanzador.puedeJugar()) {
			return false;
		}
		if (_lanzador.getNroInvocaciones() >= _lanzador.getTotalStats().getTotalStatParaMostrar(
		Constantes.STAT_MAS_CRIATURAS_INVO)) {
			return false;
		}
		final Luchador enemigoCercano = enemigoMasCercano(objetivos);
		if (enemigoCercano == null) {
			return false;
		}
		final Duo<Celda, StatHechizo> hechizo = mejorInvocacion(enemigoCercano);
		if (hechizo == null) {
			return false;
		}
		return _pelea.intentarLanzarHechizo(_lanzador, hechizo._segundo, hechizo._primero, true) == EstadoLanzHechizo.PODER;
	}
	
	private boolean teleportSiEsPosible(final ArrayList<Luchador> objetivos) {
		if (objetivos == null || !_lanzador.puedeJugar()) {
			return false;
		}
		if (_lanzador.tieneEstado(Constantes.ESTADO_PESADO) || _lanzador.tieneEstado(Constantes.ESTADO_ARRAIGADO)
		|| _lanzador.tieneEstado(Constantes.ESTADO_PORTADOR) || _lanzador.tieneEstado(Constantes.ESTADO_TRANSPORTADO)
		|| objetivos.isEmpty()) {
			return false;
		}
		if (Camino.getEnemigoAlrededor(_lanzador.getCeldaPelea().getID(), _pelea.getMapaCopia(), null, _lanzador
		.getEquipoBin()) != null) {
			return false;
		}
		final Luchador enemigoCercano = objetivos.get(0);
		if (enemigoCercano == null) {
			return false;
		}
		final Duo<Celda, StatHechizo> hechizo = mejorTeleport(enemigoCercano);
		if (hechizo == null) {
			return false;
		}
		return _pelea.intentarLanzarHechizo(_lanzador, hechizo._segundo, hechizo._primero, true) == EstadoLanzHechizo.PODER;
	}
	
	private boolean trampearSiEsPosible() {
		if (!_lanzador.puedeJugar()) {
			return false;
		}
		final Duo<Celda, StatHechizo> hechizo = mejorGlifoTrampa();
		if (hechizo == null) {
			return false;
		}
		return _pelea.intentarLanzarHechizo(_lanzador, hechizo._segundo, hechizo._primero, true) == EstadoLanzHechizo.PODER;
	}
	
	private StatHechizo hechizoAlAzar(final Luchador objetivo) {
		if (!_lanzador.puedeJugar() || objetivo == null) {
			return null;
		}
		final ArrayList<StatHechizo> hechizos = new ArrayList<StatHechizo>();
		for (final StatHechizo SH : hechizosLanzables()) {
			if (!_pelea.dentroDelRango(_lanzador, SH, _lanzador.getCeldaPelea().getID(), objetivo.getCeldaPelea().getID())) {
				continue;
			}
			hechizos.add(SH);
		}
		if (hechizos.isEmpty()) {
			return null;
		}
		return hechizos.get(Formulas.getRandomInt(0, hechizos.size() - 1));
	}
	
	private int getFiltroIA(StatHechizo SH2, Luchador objetivo, Accion accion) {
		char c = ' ';
		if (objetivo != null) {
			if (_lanzador.getEquipoBin() == objetivo.getEquipoBin()) {
				c = '+';
			} else {
				c = '-';
			}
		}
		return SH2.filtroValorIA(accion, c);
	}
	
	private Duo<Integer, Duo<StatHechizo, Celda>> mejorAtaque(final short celdaLanzador, Luchador objetivo,
	Map<StatHechizo, Map<Celda, ArrayList<Luchador>>> map) {
		if (!_lanzador.puedeJugar()) {
			return null;
		}
		if (map == null) {
			return null;
		}
		int menorCostePA = 1000, influenciaMax = 0;
		StatHechizo SH = null;
		Celda celdaObjetivo = null;
		if (!objetivoApto(objetivo, true)) {
			return null;
		}
		clearInfluencias();
		for (Entry<StatHechizo, Map<Celda, ArrayList<Luchador>>> e : map.entrySet()) {
			StatHechizo SH2 = e.getKey();
			int filtroIA = getFiltroIA(SH2, objetivo, Accion.ATACAR);
			if (filtroIA < 0) {
				continue;
			}
			for (Entry<Celda, ArrayList<Luchador>> f : e.getValue().entrySet()) {
				Celda celdaPosibleObj = f.getKey();
				if (!estaDentroObjetivos(objetivo, f.getValue(), celdaPosibleObj)) {
					continue;
				}
				if (_pelea.puedeLanzarHechizo(_lanzador, SH2, celdaPosibleObj, celdaLanzador) != EstadoLanzHechizo.PODER) {
					continue;
				}
				int influencia = calculaInfluenciaDaño(_pelea.getMapaCopia(), SH2, celdaPosibleObj.getID(), celdaLanzador,
				filtroIA);
				if (influencia <= 0) {
					continue;
				}
				if (influencia > influenciaMax || (influencia == influenciaMax && SH2.getCostePA() < menorCostePA)) {
					SH = SH2;
					celdaObjetivo = celdaPosibleObj;
					menorCostePA = SH2.getCostePA();
					influenciaMax = influencia;
				}
			}
		}
		if (celdaObjetivo == null || SH == null) {
			return null;
		}
		Duo<StatHechizo, Celda> a = new Duo<StatHechizo, Celda>(SH, celdaObjetivo);
		if (MainServidor.MODO_DEBUG) {
			System.out.println("mejorAtaque() Hechizo: " + SH.getHechizo().getNombre() + " (" + SH.getHechizoID()
			+ ") Celda: " + celdaObjetivo.getID() + " Inf: " + influenciaMax);
		}
		return new Duo<Integer, Duo<StatHechizo, Celda>>(influenciaMax, a);
	}
	
	private Duo<Integer, Duo<StatHechizo, Celda>> mejorBuff(final short celdaLanzador, Luchador objetivo,
	Map<StatHechizo, Map<Celda, ArrayList<Luchador>>> map) {
		if (!_lanzador.puedeJugar()) {
			return null;
		}
		if (map == null) {
			return null;
		}
		int menorCostePA = 1000, influenciaMax = 0;
		StatHechizo SH = null;
		Celda celdaObjetivo = null;
		if (!objetivoApto(objetivo, true)) {
			return null;
		}
		clearInfluencias();
		for (Entry<StatHechizo, Map<Celda, ArrayList<Luchador>>> e : map.entrySet()) {
			StatHechizo SH2 = e.getKey();
			int filtroIA = getFiltroIA(SH2, objetivo, Accion.BOOSTEAR);
			if (filtroIA < 0) {
				continue;
			}
			for (Entry<Celda, ArrayList<Luchador>> f : e.getValue().entrySet()) {
				Celda celdaPosibleObj = f.getKey();
				if (!estaDentroObjetivos(objetivo, f.getValue(), celdaPosibleObj)) {
					continue;
				}
				if (_pelea.puedeLanzarHechizo(_lanzador, SH2, celdaPosibleObj, celdaLanzador) != EstadoLanzHechizo.PODER) {
					continue;
				}
				int influencia = calculaInfluenciaBuff(_pelea.getMapaCopia(), SH2, celdaPosibleObj.getID(), celdaLanzador,
				filtroIA);
				if (influencia <= 0) {
					continue;
				}
				if (influencia > influenciaMax || (influencia == influenciaMax && SH2.getCostePA() < menorCostePA)) {
					SH = SH2;
					celdaObjetivo = celdaPosibleObj;
					menorCostePA = SH2.getCostePA();
					influenciaMax = influencia;
				}
			}
		}
		if (celdaObjetivo == null || SH == null) {
			return null;
		}
		Duo<StatHechizo, Celda> a = new Duo<StatHechizo, Celda>(SH, celdaObjetivo);
		if (MainServidor.MODO_DEBUG) {
			System.out.println("mejorBuff() Hechizo: " + SH.getHechizo().getNombre() + " (" + SH.getHechizoID() + ") Celda: "
			+ celdaObjetivo.getID() + " Inf: " + influenciaMax);
		}
		return new Duo<Integer, Duo<StatHechizo, Celda>>(influenciaMax, a);
	}
	
	private Duo<Integer, Duo<StatHechizo, Celda>> mejorCura(final short celdaLanzador, Luchador objetivo,
	Map<StatHechizo, Map<Celda, ArrayList<Luchador>>> map) {
		if (!_lanzador.puedeJugar()) {
			return null;
		}
		if (map == null) {
			return null;
		}
		int menorCostePA = 1000, influenciaMax = 0;
		StatHechizo SH = null;
		Celda celdaObjetivo = null;
		if (!objetivoApto(objetivo, true)) {
			return null;
		}
		clearInfluencias();
		for (Entry<StatHechizo, Map<Celda, ArrayList<Luchador>>> e : map.entrySet()) {
			StatHechizo SH2 = e.getKey();
			int filtroIA = getFiltroIA(SH2, objetivo, Accion.CURAR);
			if (filtroIA < 0) {
				continue;
			}
			for (Entry<Celda, ArrayList<Luchador>> f : e.getValue().entrySet()) {
				Celda celdaPosibleObj = f.getKey();
				if (!estaDentroObjetivos(objetivo, f.getValue(), celdaPosibleObj)) {
					continue;
				}
				if (_pelea.puedeLanzarHechizo(_lanzador, SH2, celdaPosibleObj, celdaLanzador) != EstadoLanzHechizo.PODER) {
					continue;
				}
				int influencia = calculaInfluenciaCura(_pelea.getMapaCopia(), SH2, celdaPosibleObj.getID(), celdaLanzador,
				filtroIA);
				if (influencia <= 0) {
					continue;
				}
				if (influencia > influenciaMax || (influencia == influenciaMax && SH2.getCostePA() < menorCostePA)) {
					SH = SH2;
					celdaObjetivo = celdaPosibleObj;
					menorCostePA = SH2.getCostePA();
					influenciaMax = influencia;
				}
			}
		}
		if (celdaObjetivo == null || SH == null) {
			return null;
		}
		Duo<StatHechizo, Celda> a = new Duo<StatHechizo, Celda>(SH, celdaObjetivo);
		if (MainServidor.MODO_DEBUG) {
			System.out.println("mejorCura() Hechizo: " + SH.getHechizo().getNombre() + " (" + SH.getHechizoID() + ") Celda: "
			+ celdaObjetivo.getID() + " Inf: " + influenciaMax);
		}
		return new Duo<Integer, Duo<StatHechizo, Celda>>(influenciaMax, a);
	}
	
	private Duo<Celda, StatHechizo> mejorInvocacion(Luchador objetivo) {
		if (!_lanzador.puedeJugar()) {
			return null;
		}
		if (objetivo == null) {
			return null;
		}
		for (final StatHechizo SH : hechizosLanzables()) {
			int filtroIA = getFiltroIA(SH, objetivo, Accion.INVOCAR);
			if (filtroIA < 0) {
				continue;
			}
			if (filtroIA == 0) {
				boolean esInvocacion = false;
				for (final EfectoHechizo EH : SH.getEfectosNormales()) {
					switch (EH.getEfectoID()) {
						case 180 :
						case 181 :
						case 185 :
						case 780 :
							esInvocacion = true;
							break;
					}
				}
				if (!esInvocacion) {
					continue;
				}
			}
			int distancia = 1000;
			Celda celdaObjetivo = null;
			ArrayList<Celda> celdas = Camino.celdasPosibleLanzamiento(SH, _lanzador, _pelea.getMapaCopia(), _lanzador
			.getCeldaPelea().getID(), (short) -1);
			for (final Celda celda : celdas) {
				final int dist = Camino.distanciaDosCeldas(_pelea.getMapaCopia(), celda.getID(), objetivo.getCeldaPelea()
				.getID());
				if (dist < distancia) {
					celdaObjetivo = celda;
					distancia = dist;
				}
			}
			if (celdaObjetivo == null) {
				continue;
			}
			if (MainServidor.MODO_DEBUG) {
				System.out.println("mejorInvocacion() Hechizo: " + SH.getHechizo().getNombre() + " (" + SH.getHechizoID()
				+ ") Celda: " + celdaObjetivo.getID());
			}
			return new Duo<Celda, StatHechizo>(celdaObjetivo, SH);
		}
		return null;
	}
	
	private Duo<Celda, StatHechizo> mejorGlifoTrampa() {
		if (!_lanzador.puedeJugar()) {
			return null;
		}
		for (final StatHechizo SH : hechizosLanzables()) {
			int tamaño = 0;
			int trampa = 0;
			boolean daño = false;
			int filtroIA = getFiltroIA(SH, null, Accion.TRAMPEAR);
			if (filtroIA < 0) {
				continue;
			}
			if (filtroIA == 0) {
				for (final EfectoHechizo EH : SH.getEfectosNormales()) {
					if (trampa == 3) {
						break;
					}
					switch (EH.getEfectoID()) {
						case 82 :
						case 85 :// Daños Agua %vida del atacante
						case 86 :// Daños Tierra %vida del atacante
						case 87 :// Daños Aire %vida del atacante
						case 88 :// Daños Fuego %vida del atacante
						case 89 :// Daños Neutral %vida del atacante
						case 91 :// robo de vida Agua
						case 92 :// robo de vida Tierra
						case 93 :// robo de vida Aire
						case 94 :// robo de vida fuego
						case 95 :// robo de vida neutral
						case 96 :// Daños Agua
						case 97 :// Daños Tierra
						case 98 :// Daños Aire
						case 99 :// Daños fuego
						case 100 :// Daños neutral
						case 275 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (agua)
						case 276 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (tierra)
						case 277 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (aire)
						case 278 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (fuego)
						case 279 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (neutro)
							trampa = 3;
							break;
						case 400 :// Crea una trampa
						case 401 :// Crea un glifo de nivel
						case 402 :// Glifo de blops Crea un glifo de nivel
							if (EH.getEfectoID() == 400) {
								trampa = 1;
							} else {
								trampa = 2;
							}
							tamaño = Encriptador.getNumeroPorValorHash(EH.getZonaEfecto().charAt(1));
							final StatHechizo sh = Mundo.getHechizo(EH.getPrimerValor()).getStatsPorNivel(EH.getSegundoValor());
							for (EfectoHechizo eh : sh.getEfectosNormales()) {
								daño = Constantes.estimaDaño(eh.getEfectoID()) == 1;
							}
							break;
					}
				}
				if (trampa == 3 || trampa == 0) {
					continue;
				}
			}
			int distancia = 10000;
			ArrayList<Luchador> objetivos = new ArrayList<>();
			objetivos.addAll(ordenLuchadores(daño ? _lanzador.getParamEquipoEnemigo() : _lanzador.getParamEquipoAliado(),
			Orden.NADA));
			Celda celdaObjetivo = null;
			for (final Celda celda : Camino.celdasPosibleLanzamiento(SH, _lanzador, _pelea.getMapaCopia(), _lanzador
			.getCeldaPelea().getID(), (short) -1)) {
				if (trampa == 1) {
					if (celda.esTrampa()) {
						continue;
					}
				} else if (trampa == 2) {
					if (celda.esGlifo()) {
						continue;
					}
				}
				for (Luchador objetivo : objetivos) {
					final int dist = Camino.distanciaDosCeldas(_pelea.getMapaCopia(), celda.getID(), objetivo.getCeldaPelea()
					.getID());
					if (dist - tamaño > 3) {
						continue;
					}
					if (dist - tamaño < distancia) {
						celdaObjetivo = celda;
						distancia = dist;
						if (dist == 0) {
							break;
						}
					}
				}
			}
			if (celdaObjetivo == null) {
				continue;
			}
			if (MainServidor.MODO_DEBUG) {
				System.out.println("mejorGlifoTrampa() Hechizo: " + SH.getHechizo().getNombre() + " (" + SH.getHechizoID()
				+ ") Celda: " + celdaObjetivo.getID());
			}
			return new Duo<Celda, StatHechizo>(celdaObjetivo, SH);
		}
		return null;
	}
	
	private Duo<Celda, StatHechizo> mejorTeleport(Luchador objetivo) {
		if (!_lanzador.puedeJugar()) {
			return null;
		}
		if (objetivo == null) {
			return null;
		}
		for (final StatHechizo SH : hechizosLanzables()) {
			int filtroIA = getFiltroIA(SH, objetivo, Accion.TELEPORTAR);
			if (filtroIA < 0) {
				continue;
			}
			if (filtroIA == 0) {
				boolean esTeleport = false;
				boolean esDaño = false;
				for (final EfectoHechizo EH : SH.getEfectosNormales()) {
					switch (EH.getEfectoID()) {
						case 4 :
							esTeleport = true;
							break;
						case 82 :
						case 85 :// Daños Agua %vida del atacante
						case 86 :// Daños Tierra %vida del atacante
						case 87 :// Daños Aire %vida del atacante
						case 88 :// Daños Fuego %vida del atacante
						case 89 :// Daños Neutral %vida del atacante
						case 91 :// robo de vida Agua
						case 92 :// robo de vida Tierra
						case 93 :// robo de vida Aire
						case 94 :// robo de vida fuego
						case 95 :// robo de vida neutral
						case 96 :// Daños Agua
						case 97 :// Daños Tierra
						case 98 :// Daños Aire
						case 99 :// Daños fuego
						case 100 :// Daños neutral
						case 275 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (agua)
						case 276 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (tierra)
						case 277 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (aire)
						case 278 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (fuego)
						case 279 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (neutro)
							esDaño = true;
							break;
					}
				}
				if (!esTeleport || esDaño) {
					continue;
				}
			}
			int distancia = 1000;
			Celda celdaObjetivo = null;
			ArrayList<Celda> celdas = Camino.celdasPosibleLanzamiento(SH, _lanzador, _pelea.getMapaCopia(), _lanzador
			.getCeldaPelea().getID(), (short) -1);
			for (final Celda celda : celdas) {
				final int dist = Camino.distanciaDosCeldas(_pelea.getMapaCopia(), celda.getID(), objetivo.getCeldaPelea()
				.getID());
				if (dist < distancia) {
					celdaObjetivo = celda;
					distancia = dist;
				}
			}
			if (celdaObjetivo == null) {
				continue;
			}
			if (MainServidor.MODO_DEBUG) {
				System.out.println("mejorTeleport() Hechizo: " + SH.getHechizo().getNombre() + " (" + SH.getHechizoID()
				+ ") Celda: " + celdaObjetivo.getID());
			}
			return new Duo<Celda, StatHechizo>(celdaObjetivo, SH);
		}
		return null;
	}
	
	private int calculaInfluenciaBuff(final Mapa mapa, final StatHechizo SH, final short celdaLanzamientoID,
	final short celdaLanzadorID, int filtroIA) {
		if (SH == null) {
			return -1;
		}
		boolean obligarUsar = false;
		int influenciaTotal = 0;
		// int suerte = 0, suerteMax = 0, azar = 0;
		// boolean tiene666 = false;
		ArrayList<EfectoHechizo> efectos = SH.getEfectosNormales();
		if (SH.getEfectosCriticos() != null && !SH.getEfectosCriticos().isEmpty()) {
			efectos = SH.getEfectosCriticos();
		}
		boolean matanza = false;
		if (filtroIA == 0) {
			byte retorna = 0;
			for (final EfectoHechizo EH : efectos) {
				// if (EH.getEfectoID() == 666)
				// tiene666 = true;
				// suerteMax += EH.getSuerte();
				switch (EH.getEfectoID()) {
					case 108 :// cura
					case 81 :// cura
						if (retorna == 0) {
							retorna = 1;
						}
						break;
					case 4 : // teletransporta
					case 5 :// empuja
					case 6 :// atrae
					case 82 :
					case 85 :// Daños Agua %vida del atacante
					case 86 :// Daños Tierra %vida del atacante
					case 87 :// Daños Aire %vida del atacante
					case 88 :// Daños Fuego %vida del atacante
					case 89 :// Daños Neutral %vida del atacante
					case 91 :// robo de vida Agua
					case 92 :// robo de vida Tierra
					case 93 :// robo de vida Aire
					case 94 :// robo de vida fuego
					case 95 :// robo de vida neutral
					case 96 :// Daños Agua
					case 97 :// Daños Tierra
					case 98 :// Daños Aire
					case 99 :// Daños fuego
					case 100 :// Daños neutral
					case 180 :// invoca mob
					case 181 :// invoca doble
					case 185 :// invoca estatico
					case 275 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (agua)
					case 276 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (tierra)
					case 277 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (aire)
					case 278 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (fuego)
					case 279 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (neutro)
					case 780 :// resucita
						return -1;
					case 141 :// mata al objetivo
					case 405 :// mata y reemplaza por una invocacion
						matanza = true;
						retorna = 3;
						break;
					// case 666 :
					// tiene666 = true;
					default :
						retorna = 3;
						break;
				}
			}
			if (retorna < 2) {
				return -1;
			}
		}
		// if (suerteMax > 0) {
		// azar = Formulas.getRandomValor(1, suerteMax);
		// }
		for (final EfectoHechizo EH : efectos) {
			ArrayList<Luchador> listaLuchadores = Hechizo.getObjetivosEfecto(mapa, _lanzador, EH, celdaLanzamientoID);
			switch (EH.getEfectoID()) {
				case 141 :// mata al objetivo
				case 405 :// mata y reemplaza por una invocacion
					matanza = true;
					if (listaLuchadores.isEmpty()) {
						return -1;
					}
					break;
			}
			int max = EH.getValorParaPromediar();
			for (Luchador objetivo : listaLuchadores) {
				if (!HechizoLanzado.puedeLanzPorObjetivo(_lanzador, objetivo.getID(), SH)) {
					continue;
				}
				if (objetivo.estaMuerto()) {
					continue;
				}
				int influencia = 0;
				if (_influencias.containsKey(objetivo) && _influencias.get(objetivo).containsKey(EH)) {
					influencia = _influencias.get(objetivo).get(EH);
				} else {
					influencia = Constantes.getInflBuffPorEfecto(EH.getEfectoID(), _lanzador, objetivo, max, celdaLanzamientoID,
					SH);
					if (!_influencias.containsKey(objetivo)) {
						_influencias.put(objetivo, new HashMap<EfectoHechizo, Integer>());
					}
					_influencias.get(objetivo).put(EH, influencia);
				}
				if (influencia == 0) {
					continue;
				}
				if (EH.getSuerte() == 0) {
					if (!objetivo.esInvocacion()) {
						if (influencia > 0) {
							influencia += 1000;
						} else if (influencia < 0) {
							influencia -= 1000;
						}
					}
				}
				switch (EH.getEfectoID()) {
					case 77 :
					case 84 :
					case 266 :
					case 267 :
					case 268 :
					case 269 :
					case 270 :
						if (matanza) {
							influenciaTotal += influencia * max;
							break;
						}
					default :
						int preTotal = influencia * max;
						if (_lanzador.getEquipoBin() == objetivo.getEquipoBin()) {
							preTotal = -preTotal;
							if (influencia < 0) {
								preTotal += objetivo.getNivel();
							}
						}
						if (preTotal > 0) {
							obligarUsar = true;
						}
						influenciaTotal += preTotal;
						break;
				}
			}
		}
		if (filtroIA == 1 && obligarUsar && influenciaTotal <= 0) {
			influenciaTotal = 1;
		}
		return influenciaTotal;
	}
	
	private int calculaInfluenciaCura(final Mapa mapa, final StatHechizo SH, final short celdaLanzamientoID,
	final short celdaLanzadorID, int filtroIA) {
		if (SH == null) {
			return -1;
		}
		boolean obligarUsar = false;
		int influenciaTotal = 0;
		// int suerte = 0, suerteMax = 0, azar = 0;
		// boolean tiene666 = false;
		ArrayList<EfectoHechizo> efectos = SH.getEfectosNormales();
		if (SH.getEfectosCriticos() != null && !SH.getEfectosCriticos().isEmpty()) {
			efectos = SH.getEfectosCriticos();
		}
		if (filtroIA == 0) {
			byte retorna = 0;
			for (final EfectoHechizo EH : efectos) {
				// if (EH.getEfectoID() == 666)
				// tiene666 = true;
				// suerteMax += EH.getSuerte();
				switch (EH.getEfectoID()) {
					case 108 :// cura
					case 81 :// cura
						retorna = 3;
						break;
					case 4 : // teletransporta
					case 5 :// empuja
					case 6 :// atrae
					case 82 :
					case 85 :// Daños Agua %vida del atacante
					case 86 :// Daños Tierra %vida del atacante
					case 87 :// Daños Aire %vida del atacante
					case 88 :// Daños Fuego %vida del atacante
					case 89 :// Daños Neutral %vida del atacante
					case 91 :// robo de vida Agua
					case 92 :// robo de vida Tierra
					case 93 :// robo de vida Aire
					case 94 :// robo de vida fuego
					case 95 :// robo de vida neutral
					case 96 :// Daños Agua
					case 97 :// Daños Tierra
					case 98 :// Daños Aire
					case 99 :// Daños fuego
					case 100 :// Daños neutral
					case 180 :// invoca mob
					case 181 :// invoca doble
					case 185 :// invoca estatico
					case 275 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (agua)
					case 276 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (tierra)
					case 277 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (aire)
					case 278 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (fuego)
					case 279 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (neutro)
					case 780 :// resucita
						return -1;
					case 141 :// mata al objetivo
					case 405 :// mata y reemplaza por una invocacion
						break;
					// case 666 :
					// tiene666 = true;
					default :
						break;
				}
			}
			if (retorna < 2) {
				return -1;
			}
		}
		// if (suerteMax > 0) {
		// azar = Formulas.getRandomValor(1, suerteMax);
		// }
		for (final EfectoHechizo EH : efectos) {
			ArrayList<Luchador> listaLuchadores = Hechizo.getObjetivosEfecto(mapa, _lanzador, EH, celdaLanzamientoID);
			switch (EH.getEfectoID()) {
				case 141 :// mata al objetivo
				case 405 :// mata y reemplaza por una invocacion
					if (listaLuchadores.isEmpty()) {
						return -1;
					}
					break;
			}
			int max = EH.getValorParaPromediar();
			for (Luchador objetivo : listaLuchadores) {
				if (!HechizoLanzado.puedeLanzPorObjetivo(_lanzador, objetivo.getID(), SH)) {
					continue;
				}
				if (objetivo.estaMuerto()) {
					continue;
				}
				int influencia = 0;
				if (_influencias.containsKey(objetivo) && _influencias.get(objetivo).containsKey(EH)) {
					influencia = _influencias.get(objetivo).get(EH);
				} else {
					influencia = Constantes.getInflDañoPorEfecto(EH.getEfectoID(), _lanzador, objetivo, max, celdaLanzamientoID,
					SH);
					if (!_influencias.containsKey(objetivo)) {
						_influencias.put(objetivo, new HashMap<EfectoHechizo, Integer>());
					}
					_influencias.get(objetivo).put(EH, influencia);
				}
				if (influencia == 0) {
					continue;
				}
				if (EH.getSuerte() == 0) {
					if (!objetivo.esInvocacion()) {
						if (influencia > 0) {
							influencia += 1000;
						} else if (influencia < 0) {
							influencia -= 1000;
						}
					}
				}
				int preTotal = influencia * max;
				if (_lanzador.getEquipoBin() == objetivo.getEquipoBin()) {
					preTotal = -preTotal;
				}
				if (preTotal > 0) {
					obligarUsar = true;
				}
				influenciaTotal += preTotal;
			}
		}
		if (filtroIA == 1 && obligarUsar && influenciaTotal <= 0) {
			influenciaTotal = 1;
		}
		return influenciaTotal;
	}
	
	private int calculaInfluenciaDaño(final Mapa mapa, final StatHechizo SH, final short celdaLanzamientoID,
	final short celdaLanzadorID, int filtroIA) {
		if (SH == null) {
			return -1;
		}
		boolean obligarUsar = false;
		int influenciaTotal = 0;
		// int suerte = 0, suerteMax = 0, azar = 0;
		// boolean tiene666 = false;
		ArrayList<EfectoHechizo> efectos = SH.getEfectosNormales();
		if (SH.getEfectosCriticos() != null && !SH.getEfectosCriticos().isEmpty()) {
			efectos = SH.getEfectosCriticos();
		}
		boolean matanza = false;
		byte retorna = 0;
		if (filtroIA == 0) {
			for (final EfectoHechizo EH : efectos) {
				// if (EH.getEfectoID() == 666)
				// tiene666 = true;
				// suerteMax += EH.getSuerte();
				switch (EH.getEfectoID()) {
					case 4 : // teletransporta
						continue;
					case 180 :// invoca
					case 181 :// invoca
					case 185 :// invoca
					case 780 :// invoca
						// System.out.println("calculaInf efectos -1");
						return -1;
					// case 666 :
					// tiene666 = true;
					case 5 :// empuja de X casillas
					case 6 :// atrae X casillas
					case 8 :// intercambia posiciones
					case 77 :// robo de PM
					case 82 :// robar Pdv fijo
					case 84 :// robo de PA
					case 85 :// Daños Agua %vida del atacante
					case 86 :// Daños Tierra %vida del atacante
					case 87 :// Daños Aire %vida del atacante
					case 88 :// Daños Fuego %vida del atacante
					case 89 :// Daños Neutral %vida del atacante
					case 91 :// robo de vida Agua
					case 92 :// robo de vida Tierra
					case 93 :// robo de vida Aire
					case 94 :// robo de vida fuego
					case 95 :// robo de vida neutral
					case 96 :// Daños Agua
					case 97 :// Daños Tierra
					case 98 :// Daños Aire
					case 99 :// Daños fuego
					case 100 :// Daños neutral
					case 101 :// - PA
					case 116 :// - Alcance
					case 127 :// - PM
					case 131 :// veneno X pdv por PA
					case 132 :// deshechiza
					case 140 :// pasar turno
					case 145 :// - a los daños
					case 152 :// -#1{~1~2 a -}#2 a la suerte
					case 153 :// -#1{~1~2 a -}#2 a la vitalidad
					case 154 :// -#1{~1~2 a -}#2 a la agilidad
					case 155 :// -#1{~1~2 a -}#2 a la inteligencia
					case 156 :// -#1{~1~2 a -}#2 a la sabiduría
					case 157 :// -#1{~1~2 a -}#2 a la fuerza
					case 162 :// - % probabilidad de perder PA
					case 163 :// - % probabilidad de perder PM
					case 168 :// - PA no esquivable
					case 169 :// - PM no esquivable
					case 215 :// debilidad
					case 216 :// debilidad
					case 217 :// debilidad
					case 218 :// debilidad
					case 219 :// debilidad
					case 266 :// robo de suerte
					case 267 :// robo de vitalidad
					case 268 :// robo de agilidad
					case 269 :// robo de inteligencia
					case 270 :// robo de sabiduría
					case 271 :// robo de fuerza
					case 275 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (agua)
					case 276 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (tierra)
					case 277 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (aire)
					case 278 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (fuego)
					case 279 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (neutro)
						retorna = 3;
						break;
					case 141 :// mata al objetivo
					case 405 :// mata y reemplaza por una invocacion
						matanza = true;
						retorna = 3;
						break;
				}
			}
			if (retorna < 2) {
				return -1;
			}
			retorna = 0;
		}
		// if (suerteMax > 0) {
		// azar = Formulas.getRandomValor(1, suerteMax);
		// }
		for (final EfectoHechizo EH : efectos) {
			ArrayList<Luchador> listaLuchadores = Hechizo.getObjetivosEfecto(mapa, _lanzador, EH, celdaLanzamientoID);
			boolean esDaño = false;
			switch (EH.getEfectoID()) {
				case 4 :
					continue;
				case 141 :// mata al objetivo
				case 405 :// mata y reemplaza por una invocacion
					if (listaLuchadores.isEmpty()) {
						return -1;
					}
				case 5 :// empuja de X casillas
				case 6 :// atrae X casillas
				case 8 :// intercambia posiciones
				case 77 :// robo de PM
				case 82 :// robar PDV fijo
				case 84 :// robo de PA
				case 85 :// Daños Agua %vida del atacante
				case 86 :// Daños Tierra %vida del atacante
				case 87 :// Daños Aire %vida del atacante
				case 88 :// Daños Fuego %vida del atacante
				case 89 :// Daños Neutral %vida del atacante
				case 91 :// robo de vida Agua
				case 92 :// robo de vida Tierra
				case 93 :// robo de vida Aire
				case 94 :// robo de vida fuego
				case 95 :// robo de vida neutral
				case 96 :// Daños Agua
				case 97 :// Daños Tierra
				case 98 :// Daños Aire
				case 99 :// Daños fuego
				case 100 :// Daños neutral
				case 101 :// - PA
				case 116 :// - Alcance
				case 127 :// - PM
				case 131 :// veneno X pdv por PA
				case 132 :// deshechiza
				case 140 :// pasar turno
				case 145 :// - a los daños
				case 152 :// -#1{~1~2 a -}#2 a la suerte
				case 153 :// -#1{~1~2 a -}#2 a la vitalidad
				case 154 :// -#1{~1~2 a -}#2 a la agilidad
				case 155 :// -#1{~1~2 a -}#2 a la inteligencia
				case 156 :// -#1{~1~2 a -}#2 a la sabiduría
				case 157 :// -#1{~1~2 a -}#2 a la fuerza
				case 162 :// - % probabilidad de perder PA
				case 163 :// - % probabilidad de perder PM
				case 168 :// - PA no esquivable
				case 169 :// - PM no esquivable
				case 215 :// debilidad
				case 216 :// debilidad
				case 217 :// debilidad
				case 218 :// debilidad
				case 219 :// debilidad
				case 266 :// robo de suerte
				case 267 :// robo de vitalidad
				case 268 :// robo de agilidad
				case 269 :// robo de inteligencia
				case 270 :// robo de sabiduría
				case 271 :// robo de fuerza
				case 275 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (agua)
				case 276 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (tierra)
				case 277 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (aire)
				case 278 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (fuego)
				case 279 :// Daños: #1{~1~2 a }#2% de la vida que le queda al atacante (neutro)
					esDaño = true;
					break;
			}
			int max = EH.getValorParaPromediar();
			for (Luchador objetivo : listaLuchadores) {
				if (!HechizoLanzado.puedeLanzPorObjetivo(_lanzador, objetivo.getID(), SH)) {
					continue;
				}
				if (objetivo.estaMuerto()) {
					continue;
				}
				int influencia = 0;
				if (_influencias.containsKey(objetivo) && _influencias.get(objetivo).containsKey(EH)) {
					influencia = _influencias.get(objetivo).get(EH);
				} else {
					influencia = Constantes.getInflDañoPorEfecto(EH.getEfectoID(), _lanzador, objetivo, max, celdaLanzamientoID,
					SH);
					if (!_influencias.containsKey(objetivo)) {
						_influencias.put(objetivo, new HashMap<EfectoHechizo, Integer>());
					}
					_influencias.get(objetivo).put(EH, influencia);
				}
				if (influencia == 0) {
					continue;
				}
				if (EH.getSuerte() == 0) {
					if (!objetivo.esInvocacion()) {
						if (influencia > 0) {
							influencia += 1000;
						} else if (influencia < 0) {
							influencia -= 1000;
						}
					}
				}
				switch (EH.getEfectoID()) {
					case 77 :
					case 84 :
					case 266 :
					case 267 :
					case 268 :
					case 269 :
					case 270 :
						if (matanza) {
							influenciaTotal += influencia * max;
							break;
						}
					default :
						int preTotal = influencia * max;
						if (_lanzador.esIAChafer() || _lanzador.getEquipoBin() != objetivo.getEquipoBin()) {
							if (esDaño && influencia > 0) {
								retorna = 3;
							}
						} else {
							preTotal = -preTotal;
						}
						if (preTotal > 0) {
							obligarUsar = true;
						}
						influenciaTotal += preTotal;
						break;
				}
				break;
			}
		}
		if (filtroIA == 1 && obligarUsar && influenciaTotal <= 0) {
			influenciaTotal = 1;
		} else if (retorna < 2) {
			return -1;
		}
		return influenciaTotal;
	}
	
	private static boolean estaDentroObjetivos(Luchador objetivo, ArrayList<Luchador> objetivos, Celda celda) {
		if (objetivo != null) {
			if (!objetivos.contains(objetivo)) {
				return false;
			}
			if (MainServidor.NIVEL_INTELIGENCIA_ARTIFICIAL <= INTELIGENCIA_SOLO_CELDAS_CON_LUCHADOR) {
				if (objetivo.getCeldaPelea() != celda) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean tieneReenvio(Luchador lanzador, Luchador objetivo, StatHechizo SH) {
		if (objetivo == null) {
			return false;
		}
		if (objetivo == lanzador) {
			return false;
		}
		if (objetivo.getValorPorBuffsID(106) >= SH.getGrado()) {
			return true;
		}
		return false;
	}
	
	private static void ordena(ArrayList<Luchador> lista, Orden... orden) {
		for (Orden o : orden) {
			if (o == Orden.PDV_MENOS_A_MAS) {
				Collections.sort(lista, new CompPDVMenosMas());
			}
			if (o == Orden.PDV_MAS_A_MENOS) {
				Collections.sort(lista, new CompPDVMasMenos());
			}
			if (o == Orden.NIVEL_MENOS_A_MAS) {
				Collections.sort(lista, new CompNivelMenosMas());
			}
			if (o == Orden.NIVEL_MAS_A_MENOS) {
				Collections.sort(lista, new CompNivelMasMenos());
			}
			if (o == Orden.INVOS_PRIMEROS) {
				Collections.sort(lista, new CompInvosPrimeros());
			}
			if (o == Orden.INVOS_ULTIMOS) {
				Collections.sort(lista, new CompInvosUltimos());
			}
		}
	}
	
	private static boolean esInvulnerable(Luchador objetivo) {
		if (objetivo == null) {
			return false;
		}
		TotalStats stats = objetivo.getTotalStats();
		if (stats.getTotalStatParaMostrar(Constantes.STAT_REDUCCION_FISICA) > 100 || stats.getTotalStatParaMostrar(
		Constantes.STAT_REDUCCION_MAGICA) > 100 || stats.getTotalStatParaMostrar(
		Constantes.STAT_MAS_DAÑOS_REDUCIDOS_NO_FECA) > 100)
			return true;
		return false;
	}
}
