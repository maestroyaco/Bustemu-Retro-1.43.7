package variables.hechizo;

import java.util.ArrayList;
import java.util.Map.Entry;
import variables.mapa.Celda;
import variables.mapa.Mapa;
import variables.mob.MobGrado;
import variables.pelea.Glifo;
import variables.pelea.Luchador;
import variables.pelea.Pelea;
import variables.pelea.Reto;
import variables.pelea.Trampa;
import variables.pelea.Reto.EstReto;
import variables.personaje.Personaje;
import variables.stats.TotalStats;
import estaticos.Camino;
import estaticos.Constantes;
import estaticos.MainServidor;
import estaticos.Encriptador;
import estaticos.Formulas;
import estaticos.GestorSalida;
import estaticos.Mundo;
import estaticos.Mundo.Duo;

public class EfectoHechizo {
	// staticos
	public static int TIEMPO_ENTRE_EFECTOS = 50;
	public static int TIEMPO_GAME_ACTION = 5;
	public static int TIEMPO_POR_LANZAR_HECHIZO = 1000;
	public static int TIEMPO_POR_LUCHADOR_MUERTO = 500;
	public static float MULTIPLICADOR_DAÑO_PJ = 1;
	public static float MULTIPLICADOR_DAÑO_MOB = 1;
	public static float MULTIPLICADOR_DAÑO_CAC = 1;
	public static enum TipoDaño {
		NORMAL, POST_TURNOS, GLIFO, TRAMPA, CAC, NULL
	}
	//
	protected byte _suerte = 0;
	protected int _efectoID, _nivelHechizoID = 1, _duracion = 0, _afectados = 0, _afectadosCond;
	protected int _primerValor = -1, _segundoValor = -1, _tercerValor = -1;
	protected int _hechizoID;
	protected String _args = "", _condicionHechizo = "";
	// condicion es para especificar si el buff hara efecto segun la condicion DAÑO AGUA, DAÑO TIERRA,
	// CURA , MENOS_PA, NADA ... etc
	protected String _zonaEfecto;
	
	public EfectoHechizo(int hechizoID) {
		_hechizoID = hechizoID;
	}
	
	public EfectoHechizo(final int efectoID, final String args, final int hechizo, final int grado, String zonaEfecto) {
		_efectoID = efectoID;
		_hechizoID = hechizo;
		_nivelHechizoID = grado;
		_zonaEfecto = zonaEfecto;
		setArgs(args);
	}
	
	public void setArgs(final String args) {
		_args = args;
		final String[] split = _args.split(",");
		try {
			_primerValor = Integer.parseInt(split[0]);
		} catch (final Exception e) {}
		try {
			_segundoValor = Integer.parseInt(split[1]);
		} catch (final Exception e) {}
		try {
			_tercerValor = Integer.parseInt(split[2]);
		} catch (final Exception e) {}
		try {
			_duracion = Integer.parseInt(split[3]);
		} catch (final Exception e) {}
		try {
			_suerte = Byte.parseByte(split[4]);
		} catch (final Exception e) {}
		if (_duracion <= -1) {
			_duracion = -3;
		}
	}
	
	public void setCondicion(String condicion) {
		if (_duracion <= 0) {
			_condicionHechizo = "";
			return;
		}
		if (!condicion.contains("BN")) {
			_condicionHechizo = condicion.toUpperCase().trim();
		}
	}
	
	public int getAfectados() {
		return _afectados;
	}
	
	public void setAfectados(int afectados) {
		_afectados = afectados;
	}
	
	public int getAfectadosCond() {
		return _afectadosCond;
	}
	
	public void setAfectadosCond(int afectados) {
		_afectadosCond = afectados;
	}
	
	public String getZonaEfecto() {
		return _zonaEfecto;
	}
	
	public boolean esMismoHechizo(final int id) {
		return _hechizoID == id;
	}
	
	public int getHechizoID() {
		return _hechizoID;
	}
	
	public int getEfectoID() {
		return _efectoID;
	}
	
	public int getDuracion() {
		return _duracion;
	}
	
	public int getSuerte() {
		return _suerte;
	}
	
	public String getArgs() {
		return _args;
	}
	
	public int getPrimerValor() {
		return _primerValor;
	}
	
	public int getSegundoValor() {
		return _segundoValor;
	}
	
	public int getTercerValor() {
		return _tercerValor;
	}
	
	public int getRandomValor(Luchador objetivo) {
		if (_segundoValor <= 0) {
			return _primerValor;
		}
		if (objetivo.tieneBuff(781)) {// mala sombra
			return Math.min(_primerValor, _segundoValor);
		}
		return Formulas.getRandomInt(_primerValor, _segundoValor);
	}
	
	public int getValorParaPromediar() {
		switch (_efectoID) {
			case 5 :// empuja de X casillas
			case 6 :
			case 8 :// intercambia posiciones
			case 132 :// deshechiza
			case 141 :// mata al objetivo
			case 405 :// mata y reemplaza por una invocacion
			case 765 :// sacrificio
				return 1;
			default :
				String[] split = _args.split(",");
				int max = 1;
				try {
					if (!split[0].equals("null")) {
						max = Math.max(max, Integer.parseInt(split[0]));
					}
				} catch (final Exception e) {}
				try {
					if (!split[1].equals("null")) {
						max = Math.max(max, Integer.parseInt(split[1]));
					}
				} catch (final Exception e) {}
				return max;
		}
	}
	
	protected int getMaxMinHechizo(final Luchador objetivo, int valor) {
		// System.out.println("old valor " + valor);
		// if (objetivo.tieneBuff(781)) {// mala sombra
		// valor = _primerValor;
		// } else
		if (objetivo.tieneBuff(782)) {// brokle
			valor = Math.max(_primerValor, _segundoValor);
		}
		// System.out.println("new valor " + valor);
		return valor;
	}
	
	public void setEfectoID(final int id) {
		_efectoID = id;
	}
	
	public static int getStatPorEfecto(final int efecto) {
		switch (efecto) {
			case Constantes.STAT_ROBA_PM :
			case 169 :
				return Constantes.STAT_MENOS_PM;
			case Constantes.STAT_ROBA_PA :
			case 168 :
				return Constantes.STAT_MENOS_PA;
			case 266 :
				return Constantes.STAT_MENOS_SUERTE;
			case 267 :
				return Constantes.STAT_MENOS_VITALIDAD;
			case 268 :
				return Constantes.STAT_MENOS_AGILIDAD;
			case 269 :
				return Constantes.STAT_MENOS_INTELIGENCIA;
			case 270 :
				return Constantes.STAT_MENOS_SABIDURIA;
			case 271 :
				return Constantes.STAT_MENOS_FUERZA;
			case Constantes.STAT_ROBA_ALCANCE :
				return Constantes.STAT_MENOS_ALCANCE;
			case 606 :
				return Constantes.STAT_MAS_SABIDURIA;
			case 607 :
				return Constantes.STAT_MAS_FUERZA;
			case 608 :
				return Constantes.STAT_MAS_SUERTE;
			case 609 :
				return Constantes.STAT_MAS_AGILIDAD;
			case 610 :
				return Constantes.STAT_MAS_VITALIDAD;
			case 611 :
				return Constantes.STAT_MAS_INTELIGENCIA;
		}
		return efecto;
	}
	
	private static int getStatContrario(int efecto) {
		switch (efecto) {
			case Constantes.STAT_MAS_PM :
				return Constantes.STAT_MENOS_PM;
			case Constantes.STAT_MAS_PA :
				return Constantes.STAT_MENOS_PA;
			case Constantes.STAT_MAS_SUERTE :
				return Constantes.STAT_MENOS_SUERTE;
			case Constantes.STAT_MAS_VITALIDAD :
				return Constantes.STAT_MENOS_VITALIDAD;
			case Constantes.STAT_MAS_AGILIDAD :
				return Constantes.STAT_MENOS_AGILIDAD;
			case Constantes.STAT_MAS_INTELIGENCIA :
				return Constantes.STAT_MENOS_INTELIGENCIA;
			case Constantes.STAT_MAS_SABIDURIA :
				return Constantes.STAT_MENOS_SABIDURIA;
			case Constantes.STAT_MAS_FUERZA :
				return Constantes.STAT_MENOS_FUERZA;
			case Constantes.STAT_MAS_ALCANCE :
				return Constantes.STAT_MENOS_ALCANCE;
			case Constantes.STAT_MENOS_PM :
				return Constantes.STAT_MAS_PM;
			case Constantes.STAT_MENOS_PA :
				return Constantes.STAT_MAS_PA;
			case Constantes.STAT_MENOS_SUERTE :
				return Constantes.STAT_MAS_SUERTE;
			case Constantes.STAT_MENOS_VITALIDAD :
				return Constantes.STAT_MAS_VITALIDAD;
			case Constantes.STAT_MENOS_AGILIDAD :
				return Constantes.STAT_MAS_AGILIDAD;
			case Constantes.STAT_MENOS_INTELIGENCIA :
				return Constantes.STAT_MAS_INTELIGENCIA;
			case Constantes.STAT_MENOS_SABIDURIA :
				return Constantes.STAT_MAS_SABIDURIA;
			case Constantes.STAT_MENOS_FUERZA :
				return Constantes.STAT_MAS_FUERZA;
			case Constantes.STAT_MENOS_ALCANCE :
				return Constantes.STAT_MAS_ALCANCE;
		}
		return efecto;
	}
	
	static String convertirArgs(int valor, int efectoID, String args) {
		if (efectoID == 788) { // castigo
			return args;
		}
		String[] splits = args.split(",");
		String valMax = "-1";
		switch (efectoID) {
			case 81 :// Cura, PDV devueltos
			case 85 :// Daños Agua %vida del atacante
			case 86 :// Daños Tierra %vida del atacante
			case 87 :// Daños Aire %vida del atacante
			case 88 :// Daños Fuego %vida del atacante
			case 89 :// Daños Neutral %vida del atacante
			case 91 :// Robar Vida(agua)
			case 92 :// Robar Vida(tierra)
			case 93 :// Robar Vida(aire)
			case 94 :// Robar Vida(fuego)
			case 95 :// Robar Vida(neutral)
			case 96 :// Daños Agua
			case 97 :// Daños Tierra
			case 98 :// Daños Aire
			case 99 :// Daños Fuego
			case 100 :// Daños Neutral
			case 107 :// reenvia daños
			case 108 :// Cura, PDV devueltos
			case 220 :// reenvia daños
			case 265 :// armaduras
				valor = Integer.parseInt(splits[0]);
				if (!splits[1].equals(valor + "")) {
					valMax = splits[1];
				}
				break;
			case 9 :// Esquiva un X% del ataque haciendolo retroceder Y casillas
			case 79 :// + X % de posibilidades de que sufras daños x X, o de que te cure x Y
			case 106 :// reenvio de hechizo
			case 131 :// Veneno : X Pdv por PA
			case 165 :// Aumenta los daños % del arma
			case 181 :// Invoca una criatura
			case 293 :// Aumenta los daños de base del hechizo X de Y
			case 301 :// Efecto de hechizo
			case 302 :
			case 303 :
			case 304 :
			case 305 :
			case 787 :// Activa un hechizo despues de turnos
			case 788 :// Castigo X durante Y turnos
				valMax = splits[1];
				break;
		}
		String argsF = valor + "," + valMax + "," + splits[2] + "," + splits[3] + "," + splits[4];
		return argsF;
	}
	
	// aumentados por efecto 293
	public static int getDañoAumentadoPorHechizo(final Luchador lanzador, int hechizo, int daño) {
		if (hechizo != 0) {
			for (final Buff buff : lanzador.getBuffsPorEfectoID(293)) {
				if (buff.getPrimerValor() == hechizo) {
					int add = buff.getTercerValor();
					if (add <= 0) {
						continue;
					}
					daño += add;
				}
			}
		}
		return daño;
	}
	
	// param official
	public static int getPorcHuida2(final int agiMov, final int agiTac) {
		// int porcTac = (int) ((100 * (Math.pow(agiTac + 25, 2))) / ((Math.pow(agiTac + 25, 2)) +
		// (Math.pow(agiMov + 25, 2))));
		int porcTac = (300 * (agiMov + 25) / (agiMov + agiTac + 50)) - 100;
		porcTac = Math.min(100, porcTac);
		porcTac = Math.max(0, porcTac);
		int porcHuida = porcTac;
		return porcHuida;
	}
	
	public static int getPorcHuida(final Luchador movedor, final Luchador tacleador) {
		final TotalStats statsTac = tacleador.getTotalStats();
		final int placajeTac = statsTac.getTotalStatConComplemento(Constantes.STAT_MAS_PLACAJE);
		if (2 * (placajeTac + 2) <= 0) {
			return 100;
		}
		final TotalStats statsMov = movedor.getTotalStats();
		int huidaMov = statsMov.getTotalStatConComplemento(Constantes.STAT_MAS_HUIDA);
		if (huidaMov < -2) {
			huidaMov = -2;
		}
		int porc = ((huidaMov + 2) * 100) / (2 * (placajeTac + 2));
		if (porc < 0) {
			porc = 0;
		} else if (porc > 100) {
			porc = 100;
		}
		return porc;
	}
	
	private static int reenvioDaño(Luchador objetivo) {
		final TotalStats totalStats = objetivo.getTotalStats();
		int sabiduria = totalStats.getTotalStatParaMostrar(Constantes.STAT_MAS_SABIDURIA);
		int rEquipo = totalStats.getTotalStatParaMostrar(Constantes.STAT_REENVIA_DAÑOS);
		int rHechizos = 0;
		for (final Buff buff : objetivo.getBuffsPorEfectoID(Constantes.STAT_DAÑOS_DEVUELTOS)) {
			rHechizos += buff.getRandomValor(buff.getLanzador());
		}
		for (final Buff buff : objetivo.getBuffsPorEfectoID(Constantes.STAT_REENVIA_DAÑOS)) {
			rHechizos += buff.getRandomValor(buff.getLanzador());
		}
		return (int) ((rHechizos * (1 + (sabiduria / (float) MainServidor.SABIDURIA_PARA_REENVIO))) + rEquipo);
	}
	
	public static int aplicarBuffContraGolpe(int efectoID, int daño, final Luchador objetivo, final Luchador lanzador,
	final Pelea pelea, final int hechizoID, final TipoDaño tipo) {
		if ((tipo == TipoDaño.CAC || tipo == TipoDaño.NORMAL || tipo == TipoDaño.GLIFO || tipo == TipoDaño.TRAMPA)
		&& lanzador.getID() != objetivo.getID()) {
			int reenvio = reenvioDaño(objetivo);
			if (reenvio > daño) {
				reenvio = daño;
			}
			// el reenvio no disminuye los daños
			// dañoFinal -= reenvio;
			if (reenvio > 0) {
				pelea.setUltimoTipoDaño(TipoDaño.NORMAL);
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 107, "-1", objetivo.getID() + "," + reenvio);
				restarPDVLuchador(pelea, lanzador, objetivo, null, reenvio);
			}
		}
		if (objetivo.estaMuerto()) {
			return 0;
		}
		// for (final int id : Constantes.BUFF_ACCION_RESPUESTA) {
		for (final Buff buff : objetivo.getBuffsPelea()) {
			if (objetivo.estaMuerto()) {
				return 0;
			}
			if (!buff.getCondicionBuff().isEmpty()) {
				continue;
			}
			switch (buff.getEfectoID()) {
				case 9 :// retrocede al ser golpeado
					if (tipo == TipoDaño.TRAMPA || tipo == TipoDaño.GLIFO) {
						continue;
					}
					if (Camino.distanciaDosCeldas(pelea.getMapaCopia(), objetivo.getCeldaPelea().getID(), lanzador.getCeldaPelea()
					.getID()) > 1) {
						continue;
					}
					final int elusion = buff.getPrimerValor(), azar = Formulas.getRandomInt(1, 100);
					if (azar > elusion) {
						continue;
					}
					int nroCasillas = 0;
					try {
						nroCasillas = Integer.parseInt(buff.getArgs().split(",")[1]);
					} catch (final Exception e) {}
					efectoEmpujones(pelea, lanzador, objetivo, lanzador.getCeldaPelea(), objetivo.getCeldaPelea(), nroCasillas,
					true);
					daño = 0;
					break;
				case 79 :// Suerte de zurcarak
					if (tipo == TipoDaño.TRAMPA || tipo == TipoDaño.GLIFO) {
						continue;
					}
					try {
						final String[] infos = buff.getArgs().split(",");
						final int coefDaño = Integer.parseInt(infos[0]);
						final int coefCura = Integer.parseInt(infos[1]);
						final int suerte = Integer.parseInt(infos[2]);
						if (Formulas.getRandomInt(1, 100) <= suerte) {
							// Cura
							daño *= coefCura;
							daño = Math.min(daño, objetivo.getPDVMaxConBuff() - objetivo.getPDVConBuff());
							daño = -daño;
						} else {
							daño *= coefDaño;
						}
					} catch (final Exception e) {}
					break;
				case 304 ://
					for (Luchador o : pelea.luchadoresDeEquipo(3)) {
						if (o.estaMuerto()) {
							continue;
						}
						if (o.tieneEstado(Constantes.ESTADO_TRANSPORTADO)) {
							continue;
						}
						Buff b = o.getBuff(766);
						if (b != null && b.getLanzador().getID() == objetivo.getID()) {
							b.aplicarHechizoDeBuff(pelea, o, o.getCeldaPelea());
						}
					}
					break;
				case 305 :
					if (tipo == TipoDaño.CAC) {
						buff.aplicarHechizoDeBuff(pelea, objetivo, objetivo.getCeldaPelea());
					}
					break;
				case 776 :// daños incurables
					if (tipo == TipoDaño.TRAMPA || tipo == TipoDaño.GLIFO) {
						continue;
					}
					if (objetivo.tieneBuff(776)) {// si posee daños incurables
						int pdvMax = objetivo.getPDVMaxSinBuff();
						final float pdaño = objetivo.getValorPorBuffsID(776) / 100f;
						pdvMax -= (int) (daño * pdaño);
						if (pdvMax < 0) {
							pdvMax = 0;
						}
						objetivo.setPDVMAX(pdvMax, false);
					}
					break;
				case 788 :// Castigos
					switch (efectoID) {
						case 85 :
						case 86 :
						case 87 :
						case 88 :
						case 89 :
							return daño;
					}
					final int porc = lanzador.getPersonaje() == null ? 1 : 2;
					int bonusGanado = daño / porc, stat = buff.getPrimerValor();
					if (stat == Constantes.STAT_CURAR) {
						stat = Constantes.STAT_MAS_VITALIDAD;
					}
					int max = 0;
					try {
						max = Integer.parseInt(buff.getArgs().split(",")[1]);
					} catch (final Exception e) {}
					max -= objetivo.getBonusCastigo(stat);
					if (max <= 0 || bonusGanado <= 0) {
						continue;
					}
					if (bonusGanado > max) {
						bonusGanado = max;
					}
					objetivo.setBonusCastigo(objetivo.getBonusCastigo(stat) + bonusGanado, stat);
					String[] splits = buff.getArgs().split(",", 2);
					Duo<Boolean, Buff> duo = objetivo.addBuffConGIE(stat, bonusGanado, 5, buff.getHechizoID(), convertirArgs(
					bonusGanado, stat, bonusGanado + "," + splits[1]), lanzador, true, TipoDaño.POST_TURNOS, "");
					if (duo._segundo != null) {
						duo._segundo.setDesbufeable(MainServidor.PARAM_BOOST_SACRO_DESBUFEABLE);
					}
					GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, stat, lanzador.getID() + "", objetivo.getID() + ","
					+ bonusGanado + "," + 5);
					break;
				default :
					// Bustemu.redactarLogServidorln("Efecto ID " + buff.getEfectoID() +
					// " no definido como EFECTO DE CONTRAGOLPE.");
					break;
			}
			// }
		}
		return daño;
	}
	
	private static boolean efectoEmpujones(Pelea pelea, Luchador lanzador, Luchador objetivo, Celda celdaInicio,
	Celda celdaDestino, int nCeldasAMover, boolean golpe) {
		if (nCeldasAMover == 0 || objetivo.estaMuerto() || objetivo.esEstatico() || objetivo.tieneEstado(
		Constantes.ESTADO_ARRAIGADO)) {
			return false;
		}
		Mapa mapaCopia = pelea.getMapaCopia();
		Duo<Integer, Short> duo = Camino.getCeldaDespuesDeEmpujon(pelea, celdaInicio, celdaDestino, nCeldasAMover);
		int celdasFaltantes = duo._primero;
		if (celdasFaltantes == -1) {
			return false;
		}
		nCeldasAMover = Math.abs(nCeldasAMover);
		int dañoEmpuje = 0;
		short nuevaCeldaID = duo._segundo;
		if (celdasFaltantes > 0) {// si falto celdas para seguir empujando
			nCeldasAMover -= celdasFaltantes;
			if (golpe) {
				Luchador empujador = lanzador;
				if (MainServidor.PARAM_MOB_TENER_NIVEL_INVOCADOR_PARA_EMPUJAR) {
					while (empujador.esInvocacion()) {
						empujador = empujador.getInvocador();
					}
				}
				int nivelEmpujador = empujador.getNivel();
				int statsDañoEmpuje = (lanzador.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_DAÑOS_EMPUJE));
				// modifique esta formula por la q me dio un frances, y suena mejor
				int max = Math.max(1, 8 * nivelEmpujador / 50);
				int rand = Formulas.getRandomInt(1, max);
				dañoEmpuje = (8 + rand) * celdasFaltantes;
				if (dañoEmpuje < 8) {
					dañoEmpuje = 8;
				}
				dañoEmpuje += statsDañoEmpuje;
				if (dañoEmpuje < 0) {
					dañoEmpuje = 0;
				}
			}
		}
		if (nuevaCeldaID > 0 && objetivo.getCeldaPelea().getID() != nuevaCeldaID) {
			final Celda nuevaCelda = mapaCopia.getCelda(nuevaCeldaID);
			if (nuevaCelda != null) {
				final Luchador transportado = objetivo.getTransportando();
				if (transportado != null) {
					objetivo.getCeldaPelea().removerLuchador(objetivo);
					objetivo.setCeldaPelea(nuevaCelda);
					pelea.quitarTransportados(objetivo);
				} else {
					objetivo.getCeldaPelea().moverLuchadoresACelda(nuevaCelda);
				}
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 5, lanzador.getID() + "", objetivo.getID() + "," + nuevaCeldaID);
				try {
					Thread.sleep((int) (300 + (200 * Math.sqrt(nCeldasAMover))));
				} catch (final Exception e) {}
			}
		}
		if (dañoEmpuje > 0) {
			final int dir = Camino.direccionEntreDosCeldas(mapaCopia, celdaInicio.getID(), celdaDestino.getID(), true);
			Luchador afectado = null;
			Celda celdaQueGolpea = null;
			while (dañoEmpuje > 0) {
				if (celdaQueGolpea == null) {
					afectado = objetivo;
					celdaQueGolpea = objetivo.getCeldaPelea();
				} else {
					short sigCeldaID = Camino.getSigIDCeldaMismaDir(celdaQueGolpea.getID(), dir, mapaCopia, true);
					final Celda sigCelda = mapaCopia.getCelda(sigCeldaID);
					if (sigCelda == null) {
						break;
					}
					celdaQueGolpea = sigCelda;
					afectado = sigCelda.getPrimerLuchador();
					if (afectado == null) {
						break;
					}
				}
				int redDañoEmpuje = afectado.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_REDUCCION_EMPUJE);
				int dañoEmpuje2 = dañoEmpuje - redDañoEmpuje;
				dañoEmpuje /= 2;
				if (redDañoEmpuje > 0) {
					GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, Constantes.STAT_MAS_DAÑOS_REDUCIDOS_NO_FECA, lanzador.getID()
					+ "", afectado.getID() + "," + redDañoEmpuje);
				}
				if (dañoEmpuje2 > 0) {
					restarPDVLuchador(pelea, afectado, lanzador, null, dañoEmpuje2);
					if (afectado.estaMuerto()) {
						break;
					} else {
						Buff buff = afectado.getBuff(303);
						if (buff != null) {
							buff.aplicarHechizoDeBuff(pelea, afectado, afectado.getCeldaPelea());
						}
					}
				}
			}
		}
		verificaTrampas(objetivo);
		return true;
	}
	
	public static void verificaTrampas(Luchador objetivo) {
		if (objetivo.getCeldaPelea().getTrampas() == null) {
			return;
		}
		for (final Trampa trampa : objetivo.getCeldaPelea().getTrampas()) {
			trampa.activarTrampa(objetivo);
		}
	}
	
	private static Luchador reenvioHechizo(Pelea pelea, int nivelHechizo, int hechizoID, Luchador objetivo,
	Luchador lanzador, TipoDaño tipo) {
		if (tipo != TipoDaño.NORMAL) {
			return objetivo;
		}
		Luchador retorno = objetivo;
		if (hechizoID != 0 && objetivo.getValorPorBuffsID(106) >= nivelHechizo) {
			int azar = Formulas.getRandomInt(1, 100);
			boolean reenvia = azar <= objetivo.getBuff(106).getTercerValor();
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 106, objetivo.getID() + "", objetivo.getID() + "," + (reenvia
			? 1
			: 0));
			if (reenvia) {
				retorno = lanzador;
			}
		}
		return retorno;
	}
	
	public static Luchador sacrificio(Pelea pelea, Luchador objetivo) {
		Luchador retorno = objetivo;
		if (retorno.tieneBuff(765)) {
			if (retorno.estaMuerto() || retorno.esEstatico() || retorno.tieneEstado(Constantes.ESTADO_ARRAIGADO) || retorno
			.tieneEstado(Constantes.ESTADO_TRANSPORTADO) || retorno.tieneEstado(Constantes.ESTADO_PORTADOR)) {
				return retorno;
			}
			final Luchador sacrificado = retorno.getBuff(765).getLanzador();
			if (sacrificado.estaMuerto() || sacrificado.esEstatico() || sacrificado.tieneEstado(Constantes.ESTADO_ARRAIGADO)
			|| sacrificado.tieneEstado(Constantes.ESTADO_TRANSPORTADO) || sacrificado.tieneEstado(
			Constantes.ESTADO_PORTADOR)) {
				return retorno;
			}
			final Celda cSacrificado = sacrificado.getCeldaPelea();
			final Celda cObjetivo = objetivo.getCeldaPelea();
			cSacrificado.limpiarLuchadores();
			cObjetivo.limpiarLuchadores();
			sacrificado.setCeldaPelea(cObjetivo);
			objetivo.setCeldaPelea(cSacrificado);
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 4, objetivo.getID() + "", objetivo.getID() + "," + cSacrificado
			.getID());
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 4, sacrificado.getID() + "", sacrificado.getID() + "," + cObjetivo
			.getID());
			retorno = sacrificado;
			try {
				Thread.sleep(250);
			} catch (final Exception e) {}
		}
		if (retorno.tieneBuff(766)) {// intercepcion de daños (no cambia de posicion)
			if (retorno.esEstatico() || retorno.estaMuerto()) {
				return retorno;
			}
			final Luchador sacrificado = retorno.getBuff(766).getLanzador();
			if (sacrificado.estaMuerto()) {
				return retorno;
			}
			retorno = sacrificado;
		}
		return retorno;
	}
	
	// public static int maxResistencia(Luchador objetivo, int resPorcT) {
	// if (objetivo.getMob() != null) {
	// return resPorcT;
	// }
	// // if (objetivo.getPersonaje() != null && resPorcT >
	// MainServidor.LIMITE_PORC_RESISTENCIA_BUFFS)
	// // {
	// // return MainServidor.LIMITE_PORC_RESISTENCIA_BUFFS;
	// // }
	// if (resPorcT > MainServidor.LIMITE_PORC_RESISTENCIA_BUFFS) {// recaurdador, prisma
	// return MainServidor.LIMITE_PORC_RESISTENCIA_BUFFS;
	// }
	// return resPorcT;
	// }
	public static int calcularCuraFinal(final Luchador curador, final int base) {
		TotalStats stats = curador.getTotalStats();
		int inteligencia = Math.max(0, stats.getTotalStatParaMostrar(Constantes.STAT_MAS_INTELIGENCIA));
		int curas = Math.max(0, stats.getTotalStatParaMostrar(Constantes.STAT_MAS_CURAS));
		return Math.max(0, (base * (100 + inteligencia) / 100) + curas);
	}
	
	public static int getDañosReducidos(final Luchador afectado, int elementoID) {
		int defensa = 0;
		int[] stats = {Constantes.STAT_MAS_DAÑOS_REDUCIDOS_ARMADURAS_FECA, Constantes.STAT_MAS_DAÑOS_REDUCIDOS_NO_FECA};
		for (int efectoID : stats) {
			for (final Buff buff : afectado.getBuffsPorEfectoID(efectoID)) {// daños reducidos
				int statComplementario = Constantes.STAT_MAS_INTELIGENCIA;
				switch (buff.getHechizoID()) {
					case 1 :// incandescente
					case 452 :
						if (elementoID != Constantes.ELEMENTO_FUEGO) {
							continue;
						}
						statComplementario = Constantes.STAT_MAS_INTELIGENCIA;
						break;
					case 6 :// terrestre
					case 453 :
						if (elementoID != Constantes.ELEMENTO_NEUTRAL && elementoID != Constantes.ELEMENTO_TIERRA) {
							continue;
						}
						statComplementario = Constantes.STAT_MAS_FUERZA;
						break;
					case 14 :// ventisca
					case 454 :
						if (elementoID != Constantes.ELEMENTO_AIRE) {
							continue;
						}
						statComplementario = Constantes.STAT_MAS_AGILIDAD;
						break;
					case 18 :// acuosa
					case 451 :
						if (elementoID != Constantes.ELEMENTO_AGUA) {
							continue;
						}
						statComplementario = Constantes.STAT_MAS_SUERTE;
						break;
				}
				Luchador lTemp = buff.getLanzador();
				if (efectoID == Constantes.STAT_MAS_DAÑOS_REDUCIDOS_NO_FECA) {
					lTemp = afectado;
				}
				int inteligencia = afectado.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_INTELIGENCIA);
				inteligencia = Math.max(0, inteligencia);
				int complemento = lTemp.getTotalStats().getTotalStatParaMostrar(statComplementario);
				complemento = Math.max(0, complemento);
				int value = buff.getRandomValor(buff.getLanzador());
				float sinComp = 1 + (complemento / 100f);
				float conComp = 1 + (inteligencia / 200f) + (complemento / 200f);
				defensa += Math.max(conComp, sinComp) * value;
			}
		}
		return defensa;
	}
	
	public static int getPuntosPerdidos(final int efecto, final int puntosARestar, final Luchador lanzador,
	final Luchador objetivo) {
		int esquivaLanzador, esquivaObjetivo, puntosIniciales, puntosActuales = 0;
		esquivaLanzador = lanzador.getTotalStats().getTotalStatConComplemento(Constantes.STAT_MAS_SABIDURIA) / 4;
		if (efecto == Constantes.STAT_MENOS_PM) {// movimiento
			esquivaObjetivo = objetivo.getTotalStats().getTotalStatConComplemento(Constantes.STAT_MAS_ESQUIVA_PERD_PM);
			puntosActuales = objetivo.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_PM);
			puntosIniciales = objetivo.getBaseStats().getStatParaMostrar(Constantes.STAT_MAS_PM);
			if (objetivo.getObjetosStats() != null) {
				puntosIniciales += objetivo.getObjetosStats().getStatParaMostrar(Constantes.STAT_MAS_PM);
			}
		} else {
			esquivaObjetivo = objetivo.getTotalStats().getTotalStatConComplemento(Constantes.STAT_MAS_ESQUIVA_PERD_PA);
			puntosActuales = objetivo.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_PA);
			puntosIniciales = objetivo.getBaseStats().getStatParaMostrar(Constantes.STAT_MAS_PA);
			if (objetivo.getObjetosStats() != null) {
				puntosIniciales += objetivo.getObjetosStats().getStatParaMostrar(Constantes.STAT_MAS_PA);
			}
		}
		int plus = 0;
		if (esquivaObjetivo < 0) {
			plus = Math.abs(esquivaObjetivo);
		}
		esquivaObjetivo = Math.max(1, esquivaObjetivo);
		puntosIniciales = Math.max(1, puntosIniciales);
		// System.out.println("--------------");
		// System.out.println("esquivaLanzador " + esquivaLanzador);
		// System.out.println("esquivaObjetivo " + esquivaObjetivo);
		// System.out.println("puntosActuales " + puntosActuales);
		// System.out.println("puntosiniciales " + puntosIniciales);
		int restar = 0;
		for (int i = 0; i < puntosARestar; i++) {
			int acierto = (int) ((puntosActuales / (float) puntosIniciales) * (esquivaLanzador / (float) esquivaObjetivo)
			* 50);
			// System.out.println("prob " + acierto);
			acierto += plus;
			if (acierto < 10) {
				acierto = 10;
			} else if (acierto > 90) {
				acierto = 90;
			}
			if (acierto >= Formulas.getRandomInt(1, 100)) {
				puntosActuales--;
				restar++;
			}
		}
		if (restar > puntosActuales) {
			restar = puntosActuales;
		}
		return restar;
	}
	
	public static int dañoPorEspalda(Pelea pelea, Luchador lanzador, Luchador objetivo, int daño) {
		// if (MainServidor.BONUS_ATAQUE_ESPALDA > 0 && lanzador.getDireccion() ==
		// objetivo.getDireccion()) {
		// if (Camino.esSiguienteA(lanzador.getCeldaPelea(), objetivo.getCeldaPelea())) {
		// daño += (daño * MainServidor.BONUS_ATAQUE_ESPALDA / 100);
		// // GestorSalida.ENVIAR_cS_EMOTE_EN_PELEA(pelea, 7, objetivo.getID(), 61);
		// // GestorSalida.ENVIAR_cS_EMOTE_EN_PELEA(pelea, 7, lanzador.getID(), 68);
		// }
		// }
		return daño;
	}
	
	public static int calcularDañoFinal(final Pelea pelea, final Luchador lanzador, final Luchador objetivo, int elemento,
	final float dañoInicial, final int hechizoID, final TipoDaño tipoDaño, boolean esGC) {
		final boolean esCaC = tipoDaño == TipoDaño.CAC;
		final TotalStats tStatsLanzador = lanzador.getTotalStats();
		final TotalStats tStatsObjetivo = objetivo.getTotalStats();
		float multiDañoPJ = MULTIPLICADOR_DAÑO_PJ;
		int statC = 0, resMasO = 0, resPorcO = 0, redMag = 0, redFis = 0, redArmadO = 0, masDaños = 0, porcDaños = 0,
		multiplicaDaños = 0;
		final Personaje lanzaPerso = lanzador.getPersonaje();
		StringBuilder info = null;
		if (MainServidor.PARAM_INFO_DAÑO_BATALLA) {
			info = new StringBuilder();
			info.append("SpellID: " + hechizoID + " TargetID: " + objetivo.getID() + " CaC: " + esCaC + " GC: " + esGC
			+ " DmgStart: " + dañoInicial);
		}
		switch (elemento) {
			case Constantes.ELEMENTO_NEUTRAL + 10 :
			case Constantes.ELEMENTO_NEUTRAL :
				statC = tStatsLanzador.getTotalStatConComplemento(Constantes.STAT_MAS_FUERZA);
				masDaños = tStatsLanzador.getTotalStatConComplemento(Constantes.STAT_MAS_DAÑO_FISICO);
				masDaños = tStatsLanzador.getTotalStatConComplemento(Constantes.STAT_MAS_DAÑOS_DE_NEUTRAL);
				resMasO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_FIJA_NEUTRAL);
				resPorcO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_PORC_NEUTRAL);
				redFis = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_REDUCCION_FISICA);
				switch (pelea.getTipoPelea()) {
					case Constantes.PELEA_TIPO_DESAFIO :
					case Constantes.PELEA_TIPO_KOLISEO :
					case Constantes.PELEA_TIPO_PVP :
					case Constantes.PELEA_TIPO_RECAUDADOR :
						resPorcO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_PORC_PVP_NEUTRAL);
						resMasO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_FIJA_PVP_NEUTRAL);
						break;
				}
				break;
			case Constantes.ELEMENTO_TIERRA + 10 :
			case Constantes.ELEMENTO_TIERRA :
				statC = tStatsLanzador.getTotalStatConComplemento(Constantes.STAT_MAS_FUERZA);
				masDaños = tStatsLanzador.getTotalStatConComplemento(Constantes.STAT_MAS_DAÑO_FISICO);
				masDaños = tStatsLanzador.getTotalStatConComplemento(Constantes.STAT_MAS_DAÑOS_DE_TIERRA);
				resMasO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_FIJA_TIERRA);
				resPorcO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_PORC_TIERRA);
				redFis = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_REDUCCION_FISICA);
				switch (pelea.getTipoPelea()) {
					case Constantes.PELEA_TIPO_DESAFIO :
					case Constantes.PELEA_TIPO_KOLISEO :
					case Constantes.PELEA_TIPO_PVP :
					case Constantes.PELEA_TIPO_RECAUDADOR :
						resPorcO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_PORC_PVP_TIERRA);
						resMasO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_FIJA_PVP_TIERRA);
						break;
				}
				break;
			case Constantes.ELEMENTO_FUEGO + 10 :
			case Constantes.ELEMENTO_FUEGO :
				statC = tStatsLanzador.getTotalStatConComplemento(Constantes.STAT_MAS_INTELIGENCIA);
				masDaños = tStatsLanzador.getTotalStatConComplemento(Constantes.STAT_MAS_DAÑOS_DE_FUEGO);
				resMasO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_FIJA_FUEGO);
				resPorcO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_PORC_FUEGO);
				redMag = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_REDUCCION_MAGICA);
				switch (pelea.getTipoPelea()) {
					case Constantes.PELEA_TIPO_DESAFIO :
					case Constantes.PELEA_TIPO_KOLISEO :
					case Constantes.PELEA_TIPO_PVP :
					case Constantes.PELEA_TIPO_RECAUDADOR :
						resPorcO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_PORC_PVP_FUEGO);
						resMasO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_FIJA_PVP_FUEGO);
						break;
				}
				break;
			case Constantes.ELEMENTO_AGUA + 10 :
			case Constantes.ELEMENTO_AGUA :
				statC = tStatsLanzador.getTotalStatConComplemento(Constantes.STAT_MAS_SUERTE);
				masDaños = tStatsLanzador.getTotalStatConComplemento(Constantes.STAT_MAS_DAÑOS_DE_AGUA);
				resMasO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_FIJA_AGUA);
				resPorcO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_PORC_AGUA);
				redMag = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_REDUCCION_MAGICA);
				switch (pelea.getTipoPelea()) {
					case Constantes.PELEA_TIPO_DESAFIO :
					case Constantes.PELEA_TIPO_KOLISEO :
					case Constantes.PELEA_TIPO_PVP :
					case Constantes.PELEA_TIPO_RECAUDADOR :
						resPorcO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_PORC_PVP_AGUA);
						resMasO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_FIJA_PVP_AGUA);
						break;
				}
				break;
			case Constantes.ELEMENTO_AIRE + 10 :
			case Constantes.ELEMENTO_AIRE :
				statC = tStatsLanzador.getTotalStatConComplemento(Constantes.STAT_MAS_AGILIDAD);
				masDaños = tStatsLanzador.getTotalStatConComplemento(Constantes.STAT_MAS_DAÑOS_DE_AIRE);
				resMasO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_FIJA_AIRE);
				resPorcO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_PORC_AIRE);
				redMag = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_REDUCCION_MAGICA);
				switch (pelea.getTipoPelea()) {
					case Constantes.PELEA_TIPO_DESAFIO :
					case Constantes.PELEA_TIPO_KOLISEO :
					case Constantes.PELEA_TIPO_PVP :
					case Constantes.PELEA_TIPO_RECAUDADOR :
						resPorcO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_PORC_PVP_AIRE);
						resMasO = tStatsObjetivo.getTotalStatConComplemento(Constantes.STAT_MAS_RES_FIJA_PVP_AIRE);
						break;
				}
				break;
		}
		if (elemento >= 10) {
			elemento -= 10;
			statC = 0;
			masDaños = 0;
			porcDaños = 0;
		} else {
			masDaños += tStatsLanzador.getTotalStatParaMostrar(Constantes.STAT_MAS_DAÑOS);
			porcDaños += tStatsLanzador.getTotalStatParaMostrar(Constantes.STAT_MAS_PORC_DAÑOS);
		}
		multiplicaDaños = tStatsLanzador.getTotalStatParaMostrar(Constantes.STAT_MULTIPLICA_DAÑOS);
		if (MainServidor.PARAM_INFO_DAÑO_BATALLA) {
			info.append("\n");
			info.append("PtsStats: " + statC + " +Dmg: " + masDaños + " %Dmg: " + porcDaños + " +ResTarget: " + resMasO
			+ " %ResTarget: " + resPorcO + " RedMagTarget: " + redMag + " RedPhyTarget: " + redFis + " xDmg: "
			+ multiplicaDaños);
		}
		// resPorcO = maxResistencia(objetivo, resPorcO);
		int armaClase = 90;
		int dominioArma = 0;
		if (lanzaPerso != null && esCaC) {
			multiDañoPJ = MULTIPLICADOR_DAÑO_CAC;
			int armaTipo = 0;
			try {
				armaTipo = lanzaPerso.getObjPosicion(Constantes.OBJETO_POS_ARMA).getObjModelo().getTipo();
				final int clase = lanzaPerso.getClaseID(true);
				dominioArma = lanzador.getValorPorPrimerYEfectoID(165, armaTipo);
				switch (armaTipo) {
					case Constantes.OBJETO_TIPO_ARCO :
						if (clase == Constantes.CLASE_SRAM) {
							armaClase = 95;
						} else {
							if (clase != Constantes.CLASE_OCRA) {
								break;
							}
							armaClase = 100;
						}
						break;
					case Constantes.OBJETO_TIPO_VARITA :
						if (clase == Constantes.CLASE_FECA || clase == Constantes.CLASE_XELOR) {
							armaClase = 95;
						} else {
							if (clase != Constantes.CLASE_ANIRIPSA) {
								break;
							}
							armaClase = 100;
						}
						break;
					case Constantes.OBJETO_TIPO_BASTON :
						if (clase == Constantes.CLASE_ANIRIPSA || clase == Constantes.CLASE_OSAMODAS
						|| clase == Constantes.CLASE_PANDAWA) {
							armaClase = 95;
						} else {
							if (clase != Constantes.CLASE_FECA && clase != Constantes.CLASE_SADIDA) {
								break;
							}
							armaClase = 100;
						}
						break;
					case Constantes.OBJETO_TIPO_DAGAS :
						if (clase == Constantes.CLASE_OCRA || clase == Constantes.CLASE_ZURCARAK) {
							armaClase = 95;
						} else {
							if (clase != Constantes.CLASE_SRAM) {
								break;
							}
							armaClase = 100;
						}
						break;
					case Constantes.OBJETO_TIPO_ESPADA :
						if (clase != Constantes.CLASE_YOPUKA && clase != Constantes.CLASE_ZURCARAK) {
							break;
						}
						armaClase = 100;
						break;
					case Constantes.OBJETO_TIPO_MARTILLO :
						if (clase == Constantes.CLASE_ANUTROF || clase == Constantes.CLASE_YOPUKA
						|| clase == Constantes.CLASE_SADIDA) {
							armaClase = 95;
						} else {
							if (clase != Constantes.CLASE_OSAMODAS && clase != Constantes.CLASE_XELOR) {
								break;
							}
							armaClase = 100;
						}
						break;
					case Constantes.OBJETO_TIPO_PALA :
						if (clase != Constantes.CLASE_ANUTROF) {
							break;
						}
						armaClase = 100;
						break;
					case Constantes.OBJETO_TIPO_HACHA :
						if (clase != Constantes.CLASE_PANDAWA) {
							break;
						}
						armaClase = 100;
						break;
				}
				if (MainServidor.PARAM_INFO_DAÑO_BATALLA) {
					info.append(" %DmgWeapon: " + dominioArma + " ClasseWeapon: " + armaClase);
				}
			} catch (final Exception e) {}
		}
		if (statC < 0) {
			statC = 0;
		}
		if (tipoDaño == TipoDaño.TRAMPA) {
			int porcTrampa = tStatsLanzador.getTotalStatParaMostrar(Constantes.STAT_MAS_PORC_DAÑOS_TRAMPA);
			int masTrampa = tStatsLanzador.getTotalStatParaMostrar(Constantes.STAT_MAS_DAÑOS_TRAMPA);
			porcDaños += porcTrampa;
			masDaños += masTrampa;
			if (MainServidor.PARAM_INFO_DAÑO_BATALLA) {
				info.append(" %DmgTrap: " + porcTrampa + " +DmgTrap: " + masTrampa);
			}
		}
		if (multiplicaDaños < 1) {
			multiplicaDaños = 1;
		}
		if (MainServidor.PARAM_INFO_DAÑO_BATALLA) {
			info.append("\n");
			info.append("Formule: ");
			info.append("DmgFinal = DmgStart X xDmg {" + dañoInicial + " * " + multiplicaDaños + "}");
		}
		float dañoFinal = dañoInicial * multiplicaDaños;
		if (esCaC) {
			if (MainServidor.PARAM_INFO_DAÑO_BATALLA) {
				info.append("\n");
				info.append("DmgFinal = DmgFinal X ((100 + %DmgWeapon) / 100) X (ClasseWeapon / 100)   {" + dañoFinal + " * "
				+ " ((100 + " + dominioArma + ") / 100f) * (" + armaClase + "/ 100f)}");
			}
			dañoFinal = (dañoFinal * ((100 + dominioArma) / 100f) * (armaClase / 100f));
		}
		if (MainServidor.PARAM_INFO_DAÑO_BATALLA) {
			info.append("\n");
			info.append("DmgFinal = (DmgFinal X (100 + PtsStats + %Dmg) / 100) + +Dmg   {(" + dañoFinal + " * " + " (100 + "
			+ statC + " + " + porcDaños + ") / 100f) + " + masDaños + " }");
		}
		dañoFinal = (dañoFinal * (100 + statC + porcDaños) / 100f);
		dañoFinal += masDaños;
		if (esGC) {
			int dañoCritico = tStatsLanzador.getTotalStatParaMostrar(Constantes.STAT_MAS_DAÑOS_CRITICOS);
			int redDañoCritico = tStatsObjetivo.getTotalStatParaMostrar(Constantes.STAT_MAS_REDUCCION_CRITICOS);
			if (MainServidor.PARAM_INFO_DAÑO_BATALLA) {
				info.append("\n");
				info.append("DmgFinal = DmgFinal + +DmgCritique - +RedCritTarget  {" + dañoFinal + " + " + dañoCritico + " + "
				+ redDañoCritico + "}");
			}
			dañoFinal += dañoCritico;
			dañoFinal -= redDañoCritico;
		}
		if (lanzador.getMob() != null) {
			if (lanzador.esInvocacion()) {
				dañoFinal *= (MULTIPLICADOR_DAÑO_MOB - 0.3f);
			} else {
				dañoFinal *= MULTIPLICADOR_DAÑO_MOB;
			}
		} else {
			dañoFinal *= multiDañoPJ;
		}
		// if (hechizoID == 2006 && elemento != -1) {//hechizo la sacrificada
		// daño = lanzador.getPDVMaxSinBuff();
		// }
		if (dañoFinal < 0) {
			dañoFinal = 0;
		}
		if (tipoDaño != TipoDaño.POST_TURNOS) {
			redArmadO = getDañosReducidos(objetivo, elemento);
			if (redArmadO > 0) {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, Constantes.STAT_MAS_DAÑOS_REDUCIDOS_NO_FECA, lanzador.getID()
				+ "", objetivo.getID() + "," + redArmadO);
			}
		} else {
			resMasO = 0;
			redArmadO = 0;
		}
		int dañoReducido = (int) (dañoFinal * resPorcO / 100f);
		if (MainServidor.PARAM_INFO_DAÑO_BATALLA) {
			info.append("\n");
			info.append("DmgFinal = DmgFinal - (DmgFinal X  %ResTarget / 100)  {" + dañoFinal + " - (" + dañoFinal + " * "
			+ resPorcO + " / 100f)}");
		}
		dañoFinal -= dañoReducido;
		int resistencias = resMasO + redMag + redFis + redArmadO;
		if (MainServidor.PARAM_INFO_DAÑO_BATALLA) {
			info.append("\n");
			info.append("DmgFinal = DmgFinal - (ResistElem + RedMagic + RedPhysic + ArmSpell)  {" + dañoFinal + " - ("
			+ resMasO + " + " + redMag + " + " + redFis + " + " + redArmadO + ")}");
		}
		dañoFinal -= resistencias;
		if (dañoFinal < 0) {
			dañoFinal = 0;
		}
		for (Buff b : objetivo.getBuffsPelea()) {
			if (b.getCondicionBuff().isEmpty()) {
				continue;
			}
			String condicion = "";
			switch (elemento) {
				case Constantes.ELEMENTO_NEUTRAL :
					condicion = "N";
					break;
				case Constantes.ELEMENTO_TIERRA :
					condicion = "E";
					break;
				case Constantes.ELEMENTO_FUEGO :
					condicion = "F";
					break;
				case Constantes.ELEMENTO_AGUA :
					condicion = "W";
					break;
				case Constantes.ELEMENTO_AIRE :
					condicion = "A";
					break;
			}
			if (b.getCondicionBuff().contains("DMG_ALL") || b.getCondicionBuff().contains("DMG_" + condicion) || (b
			.getCondicionBuff().contains("D_") && b.getCondicionBuff().contains(condicion))) {
				b.aplicarBuffCondicional(objetivo);
			}
		}
		if ((MainServidor.PARAM_INFO_DAÑO_BATALLA) && (lanzaPerso != null)) {
			GestorSalida.ENVIAR_Im1223_MENSAJE_IMBORRABLE(lanzaPerso, info.toString());
		}
		if ((pelea.getTipoPelea() == Constantes.PELEA_TIPO_PVM || pelea
		.getTipoPelea() == Constantes.PELEA_TIPO_PVM_NO_ESPADA) && pelea.getRetos() != null && lanzador.esNoIA()) {
			for (final Entry<Byte, Reto> entry : pelea.getRetos().entrySet()) {
				final Reto reto = entry.getValue();
				final byte retoID = entry.getKey();
				EstReto exitoReto = reto.getEstado();
				if (exitoReto != Reto.EstReto.EN_ESPERA) {
					continue;
				}
				switch (retoID) {
					case Constantes.RETO_BLITZKRIEG :
						if (objetivo.getEquipoBin() == 1) {
							if (reto.getLuchMob() == null) {
								reto.setMob(objetivo);
							}
						}
						break;
				}
			}
		}
		pelea.setUltimoTipoDaño(tipoDaño);
		objetivo.setUltimoElementoDaño(elemento);
		return (int) dañoFinal;
	}
	
	public static void buffFinTurno(Luchador luchTurno) {
		int cadaCuantosPA, nroPAusados;
		// efecto daños por PA usados
		for (final Buff buff : luchTurno.getBuffsPorEfectoID(131)) {
			if (luchTurno.estaMuerto()) {
				continue;
			}
			int dañoPorPA = buff.getSegundoValor();
			if (dañoPorPA <= 0) {
				continue;
			}
			cadaCuantosPA = buff.getPrimerValor();
			nroPAusados = (int) Math.floor(luchTurno.getPAUsados() / cadaCuantosPA);
			final TotalStats statsTotal = buff.getLanzador().getTotalStats();
			final int inteligencia = statsTotal.getTotalStatParaMostrar(Constantes.STAT_MAS_INTELIGENCIA);
			final int pDaños = statsTotal.getTotalStatParaMostrar(Constantes.STAT_MAS_PORC_DAÑOS);
			final int masDaños = statsTotal.getTotalStatParaMostrar(Constantes.STAT_MAS_DAÑOS);
			final int reduccion = statsTotal.getTotalStatParaMostrar(Constantes.STAT_MAS_DAÑOS_REDUCIDOS_NO_FECA);
			final float factor = (100 + inteligencia + pDaños) / 100f;
			int daño = (int) (dañoPorPA * factor * nroPAusados) + masDaños;
			if (reduccion > 0) {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(luchTurno.getPelea(), 7, Constantes.STAT_MAS_DAÑOS_REDUCIDOS_NO_FECA,
				luchTurno.getID() + "", luchTurno.getID() + "," + reduccion);
				daño -= reduccion;
			}
			if (daño <= 0) {
				continue;
			}
			luchTurno.getPelea().setUltimoTipoDaño(TipoDaño.POST_TURNOS);
			EfectoHechizo.restarPDVLuchador(luchTurno.getPelea(), luchTurno, buff.getLanzador(), null, daño);
			if (luchTurno.estaMuerto()) {
				break;
			}
		}
	}
	
	private static int restarPDVLuchador(Pelea pelea, Luchador objetivo, Luchador lanzador, StringBuilder afectados,
	int valor) {
		if (valor < 0) {
			int cura = -valor;
			if (cura + objetivo.getPDVSinBuff() > objetivo.getPDVMaxSinBuff()) {
				cura = objetivo.getPDVMaxSinBuff() - objetivo.getPDVSinBuff();
			}
			valor = -cura;
		}
		int vitalidad = objetivo.getBuffsStats().getStatParaMostrar(Constantes.STAT_MAS_VITALIDAD);
		objetivo.restarPDV(valor);
		int pdv = objetivo.getPDVSinBuff() + vitalidad;
		if (pdv <= 0) {
			valor += pdv;// si pdv es menor a 0 le resta al daño
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 100, lanzador.getID() + "", objetivo.getID() + "," + (-valor));
			if (pelea != null) {
				pelea.addMuertosReturnFinalizo(objetivo, lanzador);
			}
		} else if (afectados == null) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 100, lanzador.getID() + "", objetivo.getID() + "," + (-valor));
		} else {
			if (afectados.length() > 0) {
				afectados.append("¬");
			}
			afectados.append(objetivo.getID() + "," + (-valor));
		}
		return valor;
	}
	
	private void curaSiLoGolpeas(Luchador objetivo, Luchador lanzador, StringBuilder afectados, int daño) {
		if (this.getClass() == Buff.class) {
			return;
		}
		if (objetivo.tieneBuff(786)) {
			restarPDVLuchador(null, objetivo, lanzador, afectados, -daño);
		}
	}
	
	private int duracionFinal(Luchador luch) {
		return luch.puedeJugar() ? _duracion + 1 : _duracion;
	}
	
	private void quitarInvisibilidad(Luchador lanzador, TipoDaño tipo) {
		if (lanzador.esInvisible(0) && (tipo == TipoDaño.CAC || tipo == TipoDaño.NORMAL)) {
			lanzador.hacerseVisible();
		}
	}
	
	private void aplicarHechizoDeBuff(Pelea pelea, Luchador objetivo, Celda celdaObjetivo) {
		final Hechizo hechizo = Mundo.getHechizo(_primerValor);
		if (hechizo == null) {
			return;
		}
		StatHechizo sh = hechizo.getStatsPorNivel(_segundoValor);
		if (sh == null) {
			return;
		}
		Hechizo.aplicaHechizoAPelea(pelea, objetivo, celdaObjetivo, sh.getEfectosNormales(), TipoDaño.NORMAL, false);
	}
	
	public void aplicarAPelea(final Pelea pelea, final Luchador lanzador, final ArrayList<Luchador> objetivos,
	final Celda celdaObjetivo, final TipoDaño tipo, final boolean esGC) {
		try {
			if (objetivos != null) {
				pelea.setUltAfec((byte) objetivos.size());
				// pelea.addTiempoHechizo(objetivos.size() * 200);
			}
			if ((pelea.getTipoPelea() == Constantes.PELEA_TIPO_PVM || pelea
			.getTipoPelea() == Constantes.PELEA_TIPO_PVM_NO_ESPADA) && pelea.getRetos() != null && lanzador.esNoIA()) {
				for (final Entry<Byte, Reto> entry : pelea.getRetos().entrySet()) {
					final Reto reto = entry.getValue();
					final byte retoID = entry.getKey();
					EstReto exitoReto = reto.getEstado();
					if (exitoReto != Reto.EstReto.EN_ESPERA) {
						continue;
					}
					int elementoDaño = Constantes.getElementoPorEfectoID(_efectoID);
					switch (retoID) {
						case Constantes.RETO_BARBARO :
							if (tipo != TipoDaño.CAC) {
								exitoReto = Reto.EstReto.FALLADO;
							}
							break;
						case Constantes.RETO_INCURABLE :
							if (_efectoID == 108) {// cura
								exitoReto = Reto.EstReto.FALLADO;
							}
							break;
						case Constantes.RETO_ELEMENTAL :
							if (elementoDaño != Constantes.ELEMENTO_NULO) {
								for (final Luchador luch : pelea.getInicioLuchEquipo2()) {
									if (objetivos.contains(luch)) {
										if (pelea.getUltimoElementoReto() == Constantes.ELEMENTO_NULO) {
											pelea.setUltimoElementoReto(elementoDaño);
											// fija para siempre el elemento
										} else if (pelea.getUltimoElementoReto() != elementoDaño) {
											exitoReto = Reto.EstReto.FALLADO;
										}
									}
									break;
								}
							}
							break;
						case Constantes.RETO_CIRCULEN : // circulen
							if (_efectoID == Constantes.STAT_ROBA_PM || _efectoID == Constantes.STAT_MENOS_PM
							|| _efectoID == Constantes.STAT_MENOS_PM_FIJO) {
								for (final Luchador luch : pelea.getInicioLuchEquipo2()) {
									if (objetivos.contains(luch)) {
										exitoReto = Reto.EstReto.FALLADO;
										break;
									}
								}
							}
							break;
						case Constantes.RETO_EL_TIEMPO_PASA : // el tiempo pasa
							if (_efectoID == Constantes.STAT_ROBA_PA || _efectoID == Constantes.STAT_MENOS_PA
							|| _efectoID == Constantes.STAT_MENOS_PA_FIJO) {
								for (final Luchador luch : pelea.getInicioLuchEquipo2()) {
									if (objetivos.contains(luch)) {
										exitoReto = Reto.EstReto.FALLADO;
										break;
									}
								}
							}
							break;
						case Constantes.RETO_PERDIDO_DE_VISTA : // perdido de vista
							if (_efectoID == Constantes.STAT_MENOS_ALCANCE || _efectoID == Constantes.STAT_ROBA_ALCANCE) {
								for (final Luchador luch : pelea.getInicioLuchEquipo2()) {
									if (objetivos.contains(luch)) {
										exitoReto = Reto.EstReto.FALLADO;
										break;
									}
								}
							}
							break;
						case Constantes.RETO_FOCALIZACION : // focalizacion
							if (elementoDaño != Constantes.ELEMENTO_NULO) {
								// es efecto de daño
								if (reto.getLuchMob() == null) {
									for (final Luchador luch : objetivos) {
										if (luch.getEquipoBin() == 1) {
											reto.setMob(luch);
											break;
										}
									}
								}
								for (final Luchador luch : objetivos) {
									if (luch.getEquipoBin() == 1) {
										if (reto.getLuchMob() != null && luch.getID() != reto.getLuchMob().getID()) {
											exitoReto = Reto.EstReto.FALLADO;
											break;
										}
									}
								}
							}
							break;
						case Constantes.RETO_ELITISTA : // elitisa
						case Constantes.RETO_IMPREVISIBLE : // imprevisible
							// son lo mismo, execpto q el mob cambia cada turno en elitista
							if (elementoDaño != Constantes.ELEMENTO_NULO) {
								if (reto.getLuchMob() != null) {
									for (final Luchador luch : objetivos) {
										if (!pelea.getInicioLuchEquipo2().contains(luch)) {
											continue;
										}
										if (luch.getID() != reto.getLuchMob().getID()) {
											exitoReto = Reto.EstReto.FALLADO;
											break;
										}
									}
								}
							}
							break;
						case Constantes.RETO_ABNEGACION : // abnegacion
							if (_efectoID == Constantes.STAT_CURAR || _efectoID == Constantes.STAT_CURAR_2) {// cura
								for (final Luchador luch : objetivos) {
									if (luch.getID() == lanzador.getID()) {
										exitoReto = Reto.EstReto.FALLADO;
										break;
									}
								}
							}
							break;
						case Constantes.RETO_DUELO : // duelo
						case Constantes.RETO_CADA_UNO_CON_SU_MONSTRUO : // cada uno con su monstruo
							if (elementoDaño != Constantes.ELEMENTO_NULO) {
								for (final Luchador luch : objetivos) {
									if (!pelea.getInicioLuchEquipo2().contains(luch)) {
										continue;
									}
									if (luch.getLuchQueAtacoUltimo() == 0) {
										luch.setLuchQueAtacoUltimo(lanzador.getID());
									} else {
										if (luch.getLuchQueAtacoUltimo() != lanzador.getID()) {
											exitoReto = Reto.EstReto.FALLADO;
											break;
										}
									}
								}
							}
							break;
					}
					reto.setEstado(exitoReto);
				}
			}
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("Exception en aplicarAPelea " + e.toString());
			e.printStackTrace();
			return;
		}
		aplicarEfecto(pelea, lanzador, objetivos, celdaObjetivo, tipo, esGC);
	}
	
	public void aplicarEfecto(final Pelea pelea, final Luchador lanzador, final ArrayList<Luchador> objetivos,
	final Celda celdaObjetivo, final TipoDaño tipo, final boolean esGC) {
		switch (_efectoID) {
			case 4 :// Teletransporta a una casilla Huida/Salto felino/ Salto / Teleport
				efecto_Telenstransporta(pelea, lanzador, celdaObjetivo);
				break;
			case 5 :// Hace retroceder X casillas
				efecto_Empujar(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 6 :// Hace avanzar X casillas, o atrae
				efecto_Atraer(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 8 :// Intercambia la posicion de 2 jugadores
				efecto_Intercambiar_Posiciones(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 9 :// Esquiva un X% del ataque haciendolo retroceder Y casillas
				efecto_Buff_Valor_Fijo(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 50 :// Permite levantar un jugador
				efecto_Levantar_Jugador(pelea, lanzador, celdaObjetivo);
				break;
			case 51 :// Lanzar el jugador levantado
				efecto_Lanzar_Jugador(pelea, lanzador, celdaObjetivo);
				break;
			case 84 :// Robar PA
			case 77 :// Robar PM, roba pm
				efecto_Robo_PA_PM(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 79 :// + X % de posibilidades de que sufras daños x X, o de que te cure x Y
				efecto_Buff_Valor_Fijo(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 81 :// Cura, PDV devueltos
			case 108 :// Cura, PDV devueltos
				efecto_Cura(objetivos, pelea, lanzador, tipo);
				break;
			case 82 :// Robar Vida(fijo)
				efecto_Robo_PDV_Fijo(objetivos, pelea, lanzador, celdaObjetivo, tipo, esGC);
				break;
			case 90 :// Dona % de su vida
				efecto_Dona_Porc_Vida(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 275 :// Daños Agua %vida del atacante
			case 276 :// Daños Tierra %vida del atacante
			case 277 :// Daños Aire %vida del atacante
			case 278 :// Daños Fuego %vida del atacante
			case 279 :// Daños Neutral %vida del atacante
			case 85 :// Daños Agua %vida del atacante
			case 86 :// Daños Tierra %vida del atacante
			case 87 :// Daños Aire %vida del atacante
			case 88 :// Daños Fuego %vida del atacante
			case 89 :// Daños Neutral %vida del atacante
				efecto_Daños_Porc_Elemental(objetivos, pelea, lanzador, tipo, esGC);
				break;
			case 91 :// Robar Vida(agua)
			case 92 :// Robar Vida(tierra)
			case 93 :// Robar Vida(aire)
			case 94 :// Robar Vida(fuego)
			case 95 :// Robar Vida(neutral)
				efecto_Roba_PDV_Elemental(objetivos, pelea, lanzador, tipo, esGC);
				break;
			case 96 :// Daños Agua
			case 97 :// Daños Tierra
			case 98 :// Daños Aire
			case 99 :// Daños Fuego
			case 100 :// Daños Neutral
				efecto_Daños_Elemental(objetivos, pelea, lanzador, tipo, esGC);
				break;
			case 101 :// - PA
			case 127 :// - PM
			case 168 :// - PA , no esquivable
			case 169 :// - PM , no esquivable
				efecto_Menos_PA_PM(objetivos, pelea, lanzador, celdaObjetivo, tipo);
				break;
			case 106 :// Reenvia un hechizo de nivel
				efecto_Reenvio_Hechizo(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 109 :// Daños para el lanzador
				efecto_Daños_Para_Lanzador(pelea, lanzador, celdaObjetivo, tipo, esGC);
				break;
			case 78 :// + PM
			case 105 :// Daños reducidos a X (sapo, inmunidad, tregua)
			case 107 :// Reenvia daños, Daños devueltos
			case 110 :// + X vida
			case 111 :// + X PA
			case 112 :// + Daños
			case 114 :// Multiplica los daños por X
			case 115 :// + Golpes Criticos
			case 116 :// - Alcance
			case 117 :// + Alcance
			case 118 :// + Fuerza
			case 119 :// + Agilidad
			case 120 :// + PA segun el 3arg
			case 121 :// + Daños
			case 122 :// + Fallos Criticos
			case 123 :// + Suerte
			case 124 :// + Sabiduria
			case 125 :// + Vitalidad
			case 126 :// + Inteligencia
			case 128 :// + PM
			case 138 :// + % daños
			case 142 :// + Daños Fisicos
			case 144 :// - Daños (no boosteados)
			case 145 :// - Daños
			case 152 :// - Suerte
			case 153 :// - Vitalidad
			case 154 :// - Agilidad
			case 155 :// - Inteligencia
			case 156 :// - Sabiduria
			case 157 :// - Fuerza
			case 160 :// + Esquiva PA
			case 161 :// + Esquiva PM
			case 162 :// - Esquiva PA
			case 163 :// - Esquiva PM
			case 164 :// Daños reducidos en x%
			case 171 :// - Golpes Criticos
			case 176 :// + a las prospecciones
			case 177 :// - a las prospecciones
			case 178 :// + a las curaciones
			case 179 :// - a las curaciones
			case 210 :// Resist % tierra
			case 211 :// Resist % agua
			case 212 :// Resist % aire
			case 213 :// Resist % fuego
			case 214 :// Resist % neutral
			case 215 :// Debilidad % tierra
			case 216 :// Debilidad % agua
			case 217 :// Debilidad % aire
			case 218 :// Debilidad % fuego
			case 219 :// Debilidad % neutral
			case 220 :// Reenvia daños, Daños devueltos
			case 240 :// Resistencia tierra
			case 241 :// Resistencia agua
			case 242 :// Resistencia aire
			case 243 :// Resistencia fuego
			case 244 :// Resistencia neutral
			case 245 :// Debilidad tierra
			case 246 :// Debilidad agua
			case 247 :// Debilidad aire
			case 248 :// Debilidad fuego
			case 249 :// Debilidad neutral
			case 265 :// Daños reducidos a X
			case 182 :// + Invocaciones
			case 183 :// + Reduccion Magica
			case 184 :// + Reduccion Fisica
			case 186 :// Disminuye los daños %
			case 410 :// + huida
			case 411 :// - huida
			case 413 :// + placaje
			case 414 :// - placaje
			case 415 :// + daño agua
			case 416 :// + daño tierra
			case 417 :// + daño aire
			case 418 :// + daño fuego
			case 419 :// + daño neutral
			case 425 :// + daños empuje
			case 430 :// + daños criticos
			case 429 :// + reduccion criticos
			case 431 :// STAT_MAS_RETIRO_PA
			case 432 :// STAT_MAS_RETIRO_PM
			case 433 :// STAT_MENOS_RETIRO_PA
			case 434 :// STAT_MENOS_RETIRO_PM
			case 435 :// STAT_MAS_RESISTENCIA_EMPUJE
			case 436 :
			case 437 :
			case 438 :
			case 439 :
			case 440 :
			case 441 :
			case 442 :
			case 443 :
			case 444 :
			case 606 :// + sabiduria
			case 607 :// + fuerza
			case 608 :// + suerte
			case 609 :// + agilidad
			case 610 :// + vitalidad
			case 611 :// + inteligencia
			case 776 :// +% de los daños incurables sufridos
				efecto_Bonus_Malus(objetivos, pelea, lanzador, celdaObjetivo, tipo);
				break;
			case 130 :// robo kamas
				efecto_Robar_Kamas(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 131 :// Veneno : X Pdv por PA
			case 140 :// Pasar el turno
				efecto_Buff_Valor_Fijo(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 132 :// Deshechiza Desbuffea
				efecto_Deshechizar(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 141 :// Mata al blanco
				efecto_Matar_Objetivo(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 143 :// PDV devueltos sin stats (inteligencia) cura
				efecto_Curar_Sin_Stats(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 149 :// Cambia la apariencia
				efecto_Cambiar_Apariencia(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 150 :// Invisible
				efecto_Invisibilidad(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 165 :// Aumenta los daños % del arma
				efecto_Dominio_Arma(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 180 :// Doble de sram
				efecto_Invoca_Doble(pelea, lanzador, celdaObjetivo);
				break;
			case 181 :// Invoca una criatura
			case 405 :// mata y reemplaza por invocacion
				efecto_Invoca_Mob(pelea, lanzador, celdaObjetivo);
				break;
			case 185 :// Invoca una criatura estatica
				efecto_Invoca_Estatico(pelea, lanzador, celdaObjetivo);
				break;
			case 202 :// Revela todos los objetos invisibles
				efecto_Releva_Invisibles(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 266 :// Robar Suerte
			case 267 :// Robar Vitalidad
			case 268 :// Robar agilidad
			case 269 :// Robar inteligencia
			case 270 :// Robar sabiduria
			case 271 :// Robar fuerza
				efecto_Robo_Bonus(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 293 :// Aumenta los daños de base del hechizo X de Y
				efecto_Aumenta_Daños_Hechizo(pelea, lanzador, celdaObjetivo);
				break;
			case 300 :// se lanza automaticamente al final del turno
				break;
			case 302 :// efecto de hechizo inmediato
				aplicarHechizoDeBuff(pelea, lanzador, celdaObjetivo);
				break;
			case 301 :// da efecto para inico del turno (multiman) lumino
			case 303 :// cura cuando recibe daño por empuje
			case 304 :// efecto al recibir daño
			case 305 :// cuando recibe golpe CAC
				efecto_Efectos_De_Hechizos(objetivos, pelea, lanzador);
				break;
			// 310 ya esta ocupado
			case 311 :
				efecto_Cura_Porc_Vida_Objetivo(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 320 :// Robar Alcance
				efecto_Robo_Alcance(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 400 :// Crea una trampa
				efecto_Poner_Trampa(pelea, lanzador, celdaObjetivo);
				break;
			case 401 :// Crea un glifo de nivel
				efecto_Glifo_Libre(pelea, lanzador, celdaObjetivo);
				break;
			case 402 :// Glifo de blops Crea un glifo de nivel
				efecto_Glifo_Fin_Turno(pelea, lanzador, celdaObjetivo);
				break;
			case 420 :// quitar efectos de hechizo
				efecto_Quita_Efectos_Hechizo(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 421 :// retroceder
				efecto_Retroceder(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 422 :// % de PDV en el escudo
				efecto_Porc_PDV_Escudo(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 423 :// avanzar casilla
				efecto_Avanzar(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 424 :// menos % PDV temporal
				efecto_Menos_Porc_PDV_Temporal(objetivos, lanzador, celdaObjetivo);
				break;
			case 666 :// Paso de efecto complementario
				break;
			case 670 :// Daños : X% de la vida del atacante (neutral)
			case 671 :// Daños : X% de la vida del atacante (neutral) uno mismo
			case 672 :// Daños : X% de la vida del atacante (neutral) enemigos
				efecto_Daños_Porc_Vida_Neutral(objetivos, pelea, lanzador, celdaObjetivo, tipo, esGC);
				break;
			case 750 :// bonus de captura de alma
			case 751 :// bonus de domesticacion de montura
			case 765 :// Interacambia la posicion de 2 jugadores aliados (sacrificio)
				efecto_Buff_Valor_Fijo(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 766 :// recibe daños por el lanzador sin cambiar posicion
				// FIXME TODO
				efecto_Buff_Valor_Fijo(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 780 :// lazo espiritual (invoca a un aliado muerto en combate)
				efecto_Resucitar(pelea, lanzador, celdaObjetivo);
				break;
			case 782 :// Maximiza los efectos aleatorios
			case 781 :// Minimiza los efectos aleatorios
				efecto_Buff_Valor_Fijo(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 783 :// Hace retroceder hasta la casilla objetivo
				efecto_Retrocede_Hasta_Cierta_Casilla(pelea, lanzador, celdaObjetivo);
				break;
			case 784 :// teleporta al punto de inicio
				efecto_Teleport_Inicio(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 786 :// Cura durante el ataque
			case 787 :// Activa un hechizo despues de turnos
			case 788 :// Castigo X durante Y turnos
				efecto_Buff_Valor_Fijo(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			case 950 :// Entra al Estado X
			case 951 :// Sale del Estado X
				efecto_Estados(objetivos, pelea, lanzador, celdaObjetivo);
				break;
			default :
				MainServidor.redactarLogServidorln("Efecto no implantado ID: " + _efectoID + " args: " + _args);
				break;
		}
	}
	
	// private void efectosDeHechizoAfeitado(Luchador lanzador, Pelea pelea, Luchador objetivo) {
	// final Buff buff = objetivo.getBuffPorHechizoYEfecto(1038, Constantes.STAT_MAS_PA);
	// objetivo.addBuffConGIE(Constantes.STAT_MAS_PA, buff.getPrimerValor(),
	// buff.getTurnosRestantes(), _hechizoID,
	// convertirArgs(buff.getPrimerValor(), Constantes.STAT_MAS_PA, buff.getArgs()), lanzador, true,
	// TipoDaño.POST_TURNOS);
	// GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, Constantes.STAT_MAS_PA, lanzador.getID() + "",
	// objetivo.getID() + ","
	// + buff.getPrimerValor() + "," + buff.getTurnosRestantes());
	// }
	private void efecto_Quita_Efectos_Hechizo(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		for (final Luchador objetivo : objetivos) {
			final ArrayList<Buff> buffs = new ArrayList<>();
			boolean tiene = false;
			switch (_tercerValor) {
				case 2201 :// eskerdikat
					if (objetivo.tieneEstado(63)) {
						objetivo.setEstado(63, 0);
					}
					if (objetivo.tieneEstado(62)) {
						objetivo.setEstado(62, 0);
					}
					break;
				case 2207 :// zobal
					if (objetivo.tieneEstado(64)) {
						objetivo.setEstado(64, 0);
					}
					if (objetivo.tieneEstado(62)) {
						objetivo.setEstado(62, 0);
					}
					break;
				case 2209 :// saikopat
					if (objetivo.tieneEstado(64)) {
						objetivo.setEstado(64, 0);
					}
					if (objetivo.tieneEstado(63)) {
						objetivo.setEstado(63, 0);
					}
					break;
			}
			for (final Buff buff : objetivo.getBuffsPelea()) {
				if (buff.getHechizoID() != _tercerValor) {
					buffs.add(buff);
				} else {
					tiene = true;
				}
			}
			if (!tiene) {
				continue;
			}
			GestorSalida.ENVIAR_GIe_QUITAR_BUFF(pelea, 7, objetivo.getID());
			objetivo.resetearBuffs(buffs);
		}
	}
	
	private void efecto_Telenstransporta(final Pelea pelea, Luchador lanzador, Celda celdaObjetivo) {// teletransporta
		if (lanzador.estaMuerto() || lanzador.esEstatico() || lanzador.tieneEstado(Constantes.ESTADO_PESADO) || lanzador
		.tieneEstado(Constantes.ESTADO_ARRAIGADO) || lanzador.tieneEstado(Constantes.ESTADO_TRANSPORTADO) || lanzador
		.tieneEstado(Constantes.ESTADO_PORTADOR)) {
			return;
		}
		if (!celdaObjetivo.esCaminable(true)) {
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(pelea, 7, "1CELDA_NO_CAMINABLE");
			return;
		}
		if (celdaObjetivo.getPrimerLuchador() != null) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 151, lanzador.getID() + "", _hechizoID + "");
			return;
		}
		lanzador.getCeldaPelea().moverLuchadoresACelda(celdaObjetivo);
		GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 4, lanzador.getID() + "", lanzador.getID() + "," + celdaObjetivo
		.getID());
		try {
			Thread.sleep(500);
		} catch (final Exception e) {}
		verificaTrampas(lanzador);
	}
	
	private void efecto_Empujar(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		if (_duracion == 0) {
			for (final Luchador objetivo : objetivos) {
				Celda celdaInicio = celdaObjetivo;
				if (objetivo.getCeldaPelea().getID() == celdaObjetivo.getID()) {
					celdaInicio = lanzador.getCeldaPelea();
				}
				int nroCasillas = _primerValor;
				if (objetivo.tieneEstado(Constantes.ESTADO_ESCARIFICADO)) {
					if (_hechizoID == 3298) {
						nroCasillas += 1;
					}
					if (_hechizoID == 3299) {
						nroCasillas += 2;
					}
				}
				efectoEmpujones(pelea, lanzador, objetivo, celdaInicio, objetivo.getCeldaPelea(), nroCasillas, true);
			}
		}
	}
	
	// auto retroceder - zobal
	private void efecto_Retroceder(final ArrayList<Luchador> objetivos, final Pelea pelea, final Luchador lanzador,
	final Celda celdaObjetivo) {
		int nroCasillas = _primerValor;
		// for (final Luchador objetivo : objetivos) {
		efectoEmpujones(pelea, lanzador, lanzador, celdaObjetivo, lanzador.getCeldaPelea(), nroCasillas, true);
		// }
	}
	
	// auto avanzar - zobal
	private void efecto_Avanzar(final ArrayList<Luchador> objetivos, final Pelea pelea, final Luchador lanzador,
	final Celda celdaObjetivo) {
		int nroCasillas = -_primerValor;
		// for (final Luchador objetivo : objetivos) {
		efectoEmpujones(pelea, lanzador, lanzador, celdaObjetivo, lanzador.getCeldaPelea(), nroCasillas, false);
		// }
	}
	
	private void efecto_Atraer(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		for (final Luchador objetivo : objetivos) {
			Celda celdaInicio = celdaObjetivo;
			if (objetivo.getCeldaPelea().getID() == celdaObjetivo.getID()) {
				celdaInicio = lanzador.getCeldaPelea();
			}
			int nroCasillas = -_primerValor;
			efectoEmpujones(pelea, lanzador, objetivo, celdaInicio, objetivo.getCeldaPelea(), nroCasillas, false);
		}
	}
	
	private void efecto_Porc_PDV_Escudo(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		final int escudo = lanzador.getPDVConBuff() * _primerValor / 100;
		int efectoID = getStatPorEfecto(_efectoID);
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			objetivo.addEscudo(escudo);
			objetivo.addBuffConGIE(efectoID, escudo, _duracion, _hechizoID, convertirArgs(escudo, efectoID, _args), lanzador,
			false, TipoDaño.POST_TURNOS, _condicionHechizo);
		}
	}
	
	private void efecto_Menos_Porc_PDV_Temporal(final ArrayList<Luchador> objetivos, Luchador lanzador,
	Celda celdaObjetivo) {
		int efectoID = getStatPorEfecto(_efectoID);
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			int valor = objetivo.getPDVConBuff() * _primerValor / 100;
			objetivo.addBuffConGIE(efectoID, valor, _duracion, _hechizoID, convertirArgs(valor, efectoID, _args), lanzador,
			true, TipoDaño.POST_TURNOS, _condicionHechizo);
		}
	}
	
	private void efecto_Intercambiar_Posiciones(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		if (lanzador.estaMuerto() || lanzador.esEstatico()) {
			return;
		}
		if (lanzador.tieneEstado(Constantes.ESTADO_PESADO) || lanzador.tieneEstado(Constantes.ESTADO_ARRAIGADO) || lanzador
		.tieneEstado(Constantes.ESTADO_TRANSPORTADO) || lanzador.tieneEstado(Constantes.ESTADO_PORTADOR)) {
			return;
		}
		if (objetivos == null || objetivos.isEmpty()) {
			return;
		}
		final Luchador objetivo = objetivos.get(0);
		if (objetivo == null || objetivo.estaMuerto()) {
			return;
		}
		switch (_hechizoID) {
			default :
				if (objetivo.esEstatico()) {
					break;
				}
			case 438 :// Transposicion
			case 449 :// Cambio
			case 445 :// Cooperacion
				if (objetivo.tieneEstado(Constantes.ESTADO_ARRAIGADO) || objetivo.tieneEstado(Constantes.ESTADO_TRANSPORTADO)
				|| objetivo.tieneEstado(Constantes.ESTADO_PORTADOR)) {
					return;
				}
				break;
		}
		final Celda exCeldaObjetivo = objetivo.getCeldaPelea();
		final Celda exCeldaLanzador = lanzador.getCeldaPelea();
		exCeldaObjetivo.limpiarLuchadores();
		exCeldaLanzador.limpiarLuchadores();
		objetivo.setCeldaPelea(exCeldaLanzador);
		lanzador.setCeldaPelea(exCeldaObjetivo);
		GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 4, lanzador.getID() + "", objetivo.getID() + "," + exCeldaLanzador
		.getID());
		GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 4, lanzador.getID() + "", lanzador.getID() + "," + exCeldaObjetivo
		.getID());
		try {
			Thread.sleep(500);
		} catch (final Exception e) {}
		verificaTrampas(objetivo);
		verificaTrampas(lanzador);
	}
	
	// esquiva golpes retrocediendo casillas
	private void efecto_Buff_Valor_Fijo(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		int efectoID = getStatPorEfecto(_efectoID);
		final ArrayList<Luchador> temp = new ArrayList<Luchador>();
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			temp.add(objetivo);
		}
		if (!temp.isEmpty()) {
			pelea.addBuffLuchadores(temp, efectoID, _primerValor, _duracion, _hechizoID, convertirArgs(_primerValor, efectoID,
			_args), lanzador, TipoDaño.POST_TURNOS, _condicionHechizo);
		}
	}
	
	// permite levantar a un jugador
	private void efecto_Levantar_Jugador(final Pelea pelea, Luchador lanzador, Celda celdaObjetivo) {
		final Luchador objetivo = celdaObjetivo.getPrimerLuchador();
		if (lanzador.estaMuerto() || lanzador.esEstatico() || lanzador.tieneEstado(Constantes.ESTADO_TRANSPORTADO)
		|| lanzador.tieneEstado(Constantes.ESTADO_PORTADOR)) {
			return;
		}
		if (objetivo == null || objetivo.estaMuerto() || objetivo.esEstatico() || objetivo.tieneEstado(
		Constantes.ESTADO_ARRAIGADO) || objetivo.tieneEstado(Constantes.ESTADO_TRANSPORTADO) || objetivo.tieneEstado(
		Constantes.ESTADO_PORTADOR)) {
			return;
		}
		objetivo.getCeldaPelea().removerLuchador(objetivo);
		objetivo.setCeldaPelea(lanzador.getCeldaPelea());
		objetivo.setEstado(Constantes.ESTADO_TRANSPORTADO, -1);// infinito
		lanzador.setEstado(Constantes.ESTADO_PORTADOR, -1);
		objetivo.setTransportadoPor(lanzador);
		lanzador.setTransportando(objetivo);
		GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 50, lanzador.getID() + "", "" + objetivo.getID());
		try {
			Thread.sleep(500);
		} catch (final Exception e) {}
	}
	
	// lanza a un jugador
	private void efecto_Lanzar_Jugador(final Pelea pelea, Luchador lanzador, Celda celdaObjetivo) {
		if (!celdaObjetivo.esCaminable(true) || celdaObjetivo.getPrimerLuchador() != null) {
			return;
		}
		final Luchador objetivo = lanzador.getTransportando();
		objetivo.getCeldaPelea().removerLuchador(objetivo);
		objetivo.setCeldaPelea(celdaObjetivo);
		GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 51, lanzador.getID() + "", celdaObjetivo.getID() + "");
		try {
			Thread.sleep(1000);
		} catch (final Exception e) {}
		pelea.quitarTransportados(lanzador);
	}
	
	private void efecto_Robo_PA_PM(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		final int valor = getRandomValor(lanzador);
		StringBuilder afectados = new StringBuilder();
		int ganados = 0, efectoID = getStatPorEfecto(_efectoID);
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			boolean paso = false;
			for (Buff b : objetivo.getBuffsPelea()) {
				if (b.getCondicionBuff().isEmpty()) {
					continue;
				}
				if (efectoID == Constantes.STAT_MENOS_PA && b.getCondicionBuff().contains("-PA")) {
					paso = true;
					b.aplicarBuffCondicional(objetivo);
					continue;
				}
				if (efectoID == Constantes.STAT_MENOS_PM && b.getCondicionBuff().contains("-PM")) {
					paso = true;
					b.aplicarBuffCondicional(objetivo);
					continue;
				}
			}
			if (paso) {
				continue;
			}
			int perdidos = getPuntosPerdidos(efectoID, valor, lanzador, objetivo);
			perdidos = getMaxMinHechizo(objetivo, perdidos);
			if (perdidos < valor) {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, efectoID == Constantes.STAT_MENOS_PM ? 309 : 308, lanzador.getID()
				+ "", objetivo.getID() + "," + (valor - perdidos));
			}
			if (perdidos >= 1) {
				objetivo.addBuffConGIE(efectoID, perdidos, _duracion, _hechizoID, convertirArgs(perdidos, efectoID, _args),
				lanzador, true, TipoDaño.POST_TURNOS, _condicionHechizo);
				if (afectados.length() > 0) {
					afectados.append("¬");
				}
				afectados.append(objetivo.getID() + "," + (-perdidos) + "," + duracionFinal(objetivo));
				ganados += perdidos;
			}
		}
		if (afectados.length() > 0 && _condicionHechizo.isEmpty()) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, efectoID, lanzador.getID() + "", afectados.toString());
		}
		if (ganados > 0) {
			efectoID = _efectoID == Constantes.STAT_ROBA_PM ? Constantes.STAT_MAS_PM : Constantes.STAT_MAS_PA;
			lanzador.addBuffConGIE(efectoID, ganados, _duracion, _hechizoID, convertirArgs(ganados, efectoID, _args),
			lanzador, true, TipoDaño.POST_TURNOS, _condicionHechizo);
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, efectoID, lanzador.getID() + "", lanzador.getID() + "," + ganados
			+ "," + duracionFinal(lanzador));
		}
	}
	
	// FIXME ANALIZAR PORQ DURACION + 1
	private void efecto_Robo_Alcance(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		final int valor = getRandomValor(lanzador);
		StringBuilder afectados = new StringBuilder();
		int ganados = 0, efectoID = getStatPorEfecto(_efectoID);
		final ArrayList<Luchador> temp = new ArrayList<Luchador>();
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			if (afectados.length() > 0)
				afectados.append("¬");
			afectados.append(objetivo.getID() + "," + (valor) + "," + duracionFinal(objetivo));
			temp.add(objetivo);
			ganados += valor;
		}
		if (afectados.length() > 0 && _condicionHechizo.isEmpty()) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, efectoID, lanzador.getID() + "", afectados.toString());
		}
		if (!temp.isEmpty()) {
			pelea.addBuffLuchadores(temp, efectoID, valor, _duracion, _hechizoID, convertirArgs(valor, efectoID, _args),
			lanzador, TipoDaño.POST_TURNOS, _condicionHechizo);
		}
		if (ganados > 0) {
			efectoID = Constantes.STAT_MAS_ALCANCE;
			lanzador.addBuffConGIE(efectoID, ganados, (_duracion), _hechizoID, convertirArgs(ganados, efectoID, _args),
			lanzador, true, TipoDaño.POST_TURNOS, _condicionHechizo);
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, efectoID, lanzador.getID() + "", lanzador.getID() + "," + ganados
			+ "," + duracionFinal(lanzador));
		}
	}
	
	// FIXME OBSERVAR
	private void efecto_Robo_Bonus(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		int valor = getRandomValor(lanzador);
		StringBuilder afectados = new StringBuilder();
		int robo = 0, efectoID = getStatPorEfecto(_efectoID);
		final int valor2 = valor;
		final ArrayList<Luchador> temp = new ArrayList<Luchador>();
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			valor = getMaxMinHechizo(objetivo, valor);
			if (valor != valor2) {
				objetivo.addBuffConGIE(efectoID, valor, _duracion, _hechizoID, convertirArgs(valor, efectoID, _args), lanzador,
				true, TipoDaño.POST_TURNOS, _condicionHechizo);
			} else {
				temp.add(objetivo);
			}
			if (afectados.length() > 0)
				afectados.append("¬");
			afectados.append(objetivo.getID() + "," + (valor) + "," + duracionFinal(objetivo));
			robo += valor;
			valor = valor2;
		}
		if (afectados.length() > 0 && _condicionHechizo.isEmpty()) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, efectoID, lanzador.getID() + "", afectados.toString());
		}
		if (!temp.isEmpty()) {
			pelea.addBuffLuchadores(temp, efectoID, valor, _duracion, _hechizoID, convertirArgs(valor, efectoID, _args),
			lanzador, TipoDaño.POST_TURNOS, _condicionHechizo);
		}
		if (robo > 0) {
			int stat = getStatContrario(efectoID);
			lanzador.addBuffConGIE(stat, robo, _duracion, _hechizoID, convertirArgs(robo, stat, _args), lanzador, true,
			TipoDaño.POST_TURNOS, _condicionHechizo);
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, stat, lanzador.getID() + "", lanzador.getID() + "," + robo + ","
			+ duracionFinal(lanzador));
		}
	}
	
	private void efecto_Robo_PDV_Fijo(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo, final TipoDaño tipo, boolean esGC) {
		int efectoID = getStatPorEfecto(_efectoID);
		if (_duracion == 0) {
			quitarInvisibilidad(lanzador, tipo);
			StringBuilder afectados = new StringBuilder();
			for (Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				objetivo = reenvioHechizo(pelea, _nivelHechizoID, _hechizoID, objetivo, lanzador, tipo);
				objetivo = sacrificio(pelea, objetivo);
				int daño = _primerValor;
				daño = calcularDañoFinal(pelea, lanzador, objetivo, (10 + Constantes.getElementoPorEfectoID(efectoID)), daño,
				_hechizoID, tipo, esGC);
				daño = aplicarBuffContraGolpe(efectoID, daño, objetivo, lanzador, pelea, _hechizoID, tipo);
				daño = restarPDVLuchador(pelea, objetivo, lanzador, afectados, daño);
				restarPDVLuchador(pelea, lanzador, lanzador, afectados, -daño / 2);
				curaSiLoGolpeas(objetivo, lanzador, afectados, daño);
			}
			if (afectados.length() > 0) {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 100, lanzador.getID() + "", afectados.toString());
			}
		} else {
			final ArrayList<Luchador> temp = new ArrayList<Luchador>();
			for (final Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				temp.add(objetivo);
			}
			if (!temp.isEmpty()) {
				pelea.addBuffLuchadores(temp, efectoID, _primerValor, _duracion, _hechizoID, convertirArgs(_primerValor,
				efectoID, _args), lanzador, tipo, _condicionHechizo);
			}
		}
	}
	
	private void efecto_Robar_Kamas(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {// robar
		// kamas
		if (pelea.getTipoPelea() == 0) {
			return;
		}
		int valor = getRandomValor(lanzador);
		StringBuilder afectados = new StringBuilder();
		for (final Luchador objetivo : objetivos) {
			final Personaje perso = objetivo.getPersonaje();
			if (objetivo.estaMuerto() || perso == null) {
				continue;
			}
			if (valor > perso.getKamas()) {
				valor = (int) perso.getKamas();
			}
			if (valor == 0) {
				continue;
			}
			perso.addKamas(-valor, false, false);
			final Personaje perso2 = lanzador.getPersonaje();
			if (perso2 != null) {
				perso2.addKamas(valor, false, false);
			}
			if (afectados.length() > 0) {
				afectados.append("¬");
			}
			afectados.append(objetivo.getID() + "," + (valor));
		}
		if (afectados.length() > 0 && _condicionHechizo.isEmpty()) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 130, lanzador.getID() + "", afectados.toString());
		}
	}
	
	private void efecto_Efectos_De_Hechizos(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador) {
		int efectoID = getStatPorEfecto(_efectoID);
		final ArrayList<Luchador> temp = new ArrayList<Luchador>();
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			temp.add(objetivo);
		}
		if (!temp.isEmpty()) {
			pelea.addBuffLuchadores(temp, efectoID, _primerValor, _duracion, _hechizoID, convertirArgs(_primerValor, efectoID,
			_args), lanzador, TipoDaño.POST_TURNOS, _condicionHechizo);
		}
	}
	
	private void efecto_Menos_PA_PM(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo, TipoDaño tipo) {
		final int valor = getRandomValor(lanzador);
		int efectoID = getStatPorEfecto(_efectoID);
		final ArrayList<Luchador> temp = new ArrayList<Luchador>();
		StringBuilder afectados = new StringBuilder();
		for (Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			boolean paso = false;
			for (Buff b : objetivo.getBuffsPelea()) {
				if (b.getCondicionBuff().isEmpty()) {
					continue;
				}
				if (efectoID == Constantes.STAT_MENOS_PA && b.getCondicionBuff().contains("-PA")) {
					paso = true;
					b.aplicarBuffCondicional(objetivo);
					continue;
				}
				if (efectoID == Constantes.STAT_MENOS_PM && b.getCondicionBuff().contains("-PM")) {
					paso = true;
					b.aplicarBuffCondicional(objetivo);
					continue;
				}
			}
			if (paso) {
				continue;
			}
			int perdidos = valor;
			switch (_efectoID) {
				case Constantes.STAT_MENOS_PA :// esquivables
					objetivo = reenvioHechizo(pelea, _nivelHechizoID, _hechizoID, objetivo, lanzador, tipo);
					if (objetivo.estaMuerto()) {
						continue;
					}
				case Constantes.STAT_MENOS_PM :// esquivables
					perdidos = getPuntosPerdidos(_efectoID, valor, lanzador, objetivo);
					perdidos = getMaxMinHechizo(objetivo, perdidos);
					if (perdidos < valor) {// esquivados
						GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, efectoID == Constantes.STAT_MENOS_PM ? 309 : 308, lanzador
						.getID() + "", objetivo.getID() + "," + (valor - perdidos));
					}
					if (perdidos >= 1) {
						objetivo.addBuffConGIE(efectoID, perdidos, _duracion, _hechizoID, convertirArgs(perdidos, _efectoID, _args),
						lanzador, true, tipo, _condicionHechizo);
					}
					break;
				case Constantes.STAT_MENOS_PA_FIJO :
				case Constantes.STAT_MENOS_PM_FIJO :
					temp.add(objetivo);
					break;
			}
			if (perdidos <= 0) {
				continue;
			}
			if (afectados.length() > 0) {
				afectados.append("¬");
			}
			afectados.append(objetivo.getID() + "," + (-perdidos) + "," + duracionFinal(objetivo));
		}
		if (afectados.length() > 0 && _condicionHechizo.isEmpty()) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, efectoID, lanzador.getID() + "", afectados.toString());
		}
		if (!temp.isEmpty()) {
			pelea.addBuffLuchadores(temp, efectoID, valor, _duracion, _hechizoID, convertirArgs(valor, efectoID, _args),
			lanzador, tipo, _condicionHechizo);
		}
	}
	
	protected void efecto_Cura(final ArrayList<Luchador> objetivos, final Pelea pelea, final Luchador lanzador,
	TipoDaño tipo) {// curacion
		// if (lanzador.tieneEstado(Constantes.ESTADO_ALTRUISTA)) {
		// return;
		// }
		int efectoID = getStatPorEfecto(_efectoID);
		int modi = 0;
		Personaje perso = lanzador.getPersonaje();
		if (perso != null) {
			if (perso.tieneModfiSetClase(_hechizoID)) {
				modi = perso.getModifSetClase(_hechizoID, 284);
			}
		}
		final int cura2 = getRandomValor(lanzador);
		if (_duracion == 0) {
			StringBuilder afectados = new StringBuilder();
			for (final Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				// if (objetivo.tieneEstado(Constantes.ESTADO_ALTRUISTA)) {
				// continue;
				// }
				boolean paso = false;
				for (Buff b : objetivo.getBuffsPelea()) {
					if (b.getCondicionBuff().isEmpty()) {
						continue;
					}
					if (b.getCondicionBuff().contains("SOIN")) {
						paso = true;
						b.aplicarBuffCondicional(objetivo);
						continue;
					}
				}
				if (paso) {
					continue;
				}
				int cura = getMaxMinHechizo(objetivo, cura2);
				cura = calcularCuraFinal(lanzador, cura) + modi;
				restarPDVLuchador(pelea, objetivo, lanzador, afectados, -cura);
			}
			if (afectados.length() > 0) {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 100, lanzador.getID() + "", afectados.toString());
			}
		} else {
			final ArrayList<Luchador> temp = new ArrayList<Luchador>();
			for (final Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				temp.add(objetivo);
			}
			if (!temp.isEmpty()) {
				pelea.addBuffLuchadores(temp, efectoID, _primerValor, _duracion, _hechizoID, convertirArgs(_primerValor,
				efectoID, _args), lanzador, tipo, _condicionHechizo);
			}
		}
	}
	
	private void efecto_Cura_Porc_Vida_Objetivo(final ArrayList<Luchador> objetivos, final Pelea pelea,
	final Luchador lanzador, final Celda celdaObjetivo) {
		int efectoID = 108;
		if (_duracion == 0) {
			final int porc = getRandomValor(lanzador);
			StringBuilder afectados = new StringBuilder();
			for (final Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				int pdvMaxBuff = objetivo.getPDVMaxConBuff();
				int cura = porc * pdvMaxBuff / 100;
				restarPDVLuchador(pelea, objetivo, lanzador, afectados, -cura);
			}
			if (afectados.length() > 0) {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 100, lanzador.getID() + "", afectados.toString());
			}
		} else {
			final ArrayList<Luchador> temp = new ArrayList<Luchador>();
			for (final Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				temp.add(objetivo);
			}
			if (!temp.isEmpty()) {
				pelea.addBuffLuchadores(temp, efectoID, _primerValor, _duracion, _hechizoID, convertirArgs(_primerValor,
				efectoID, _args), lanzador, TipoDaño.POST_TURNOS, _condicionHechizo);
			}
		}
	}
	
	private void efecto_Curar_Sin_Stats(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		if (_duracion == 0) {
			int cura = getRandomValor(lanzador);
			StringBuilder afectados = new StringBuilder();
			for (final Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				int curaTemp = getMaxMinHechizo(objetivo, cura);
				restarPDVLuchador(pelea, objetivo, lanzador, afectados, -curaTemp);
			}
			if (afectados.length() > 0) {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 100, lanzador.getID() + "", afectados.toString());
			}
		} else {
			int efectoID = getStatPorEfecto(_efectoID);
			final ArrayList<Luchador> temp = new ArrayList<Luchador>();
			for (final Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				temp.add(objetivo);
			}
			if (!temp.isEmpty())
				pelea.addBuffLuchadores(temp, efectoID, _primerValor, _duracion, _hechizoID, convertirArgs(_primerValor,
				efectoID, _args), lanzador, TipoDaño.POST_TURNOS, _condicionHechizo);
		}
	}
	
	private void efecto_Dona_Porc_Vida(final ArrayList<Luchador> objetivos, final Pelea pelea, final Luchador lanzador,
	final Celda celdaObjetivo) {
		int efectoID = getStatPorEfecto(_efectoID);
		if (_duracion == 0) {
			StringBuilder afectados = new StringBuilder();
			final int porc = getRandomValor(lanzador);
			int daño = porc * lanzador.getPDVConBuff() / 100;
			if (daño > lanzador.getPDVConBuff()) {
				daño = lanzador.getPDVConBuff() - 1;
			}
			daño = restarPDVLuchador(pelea, lanzador, lanzador, afectados, daño);
			for (final Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				int cura = daño;
				restarPDVLuchador(pelea, objetivo, lanzador, afectados, -cura);
			}
			if (afectados.length() > 0) {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 100, lanzador.getID() + "", afectados.toString());
			}
		} else {
			final ArrayList<Luchador> temp = new ArrayList<Luchador>();
			for (final Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				temp.add(objetivo);
			}
			if (!temp.isEmpty())
				pelea.addBuffLuchadores(temp, efectoID, _primerValor, _duracion, _hechizoID, convertirArgs(_primerValor,
				efectoID, _args), lanzador, TipoDaño.POST_TURNOS, _condicionHechizo);
		}
	}
	
	// daños % de vida elemental --> tenia el ex CaC como true
	protected void efecto_Daños_Porc_Elemental(final ArrayList<Luchador> objetivos, final Pelea pelea,
	final Luchador lanzador, final TipoDaño tipo, final boolean esGC) {
		int efectoID = getStatPorEfecto(_efectoID);
		if (_duracion == 0) {
			quitarInvisibilidad(lanzador, tipo);
			StringBuilder afectados = new StringBuilder();
			for (Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				objetivo = reenvioHechizo(pelea, _nivelHechizoID, _hechizoID, objetivo, lanzador, tipo);
				objetivo = sacrificio(pelea, objetivo);
				int porc = getRandomValor(lanzador);
				porc = getMaxMinHechizo(objetivo, porc);
				int daño = porc * lanzador.getPDVConBuff() / 100;
				daño = getDañoAumentadoPorHechizo(lanzador, _hechizoID, daño);
				daño = calcularDañoFinal(pelea, lanzador, objetivo, (10 + Constantes.getElementoPorEfectoID(_efectoID)), daño,
				_hechizoID, tipo, esGC);
				daño = dañoPorEspalda(pelea, lanzador, objetivo, daño);
				daño = aplicarBuffContraGolpe(efectoID, daño, objetivo, lanzador, pelea, _hechizoID, tipo);
				daño = restarPDVLuchador(pelea, objetivo, lanzador, afectados, daño);
				curaSiLoGolpeas(objetivo, lanzador, afectados, daño);
			}
			if (afectados.length() > 0) {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 100, lanzador.getID() + "", afectados.toString());
			}
		} else {
			final ArrayList<Luchador> temp = new ArrayList<Luchador>();
			for (final Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				temp.add(objetivo);
			}
			if (!temp.isEmpty())
				pelea.addBuffLuchadores(temp, efectoID, _primerValor, _duracion, _hechizoID, convertirArgs(_primerValor,
				efectoID, _args), lanzador, tipo, _condicionHechizo);
		}
	}
	
	// roba PDV elementales
	protected void efecto_Roba_PDV_Elemental(final ArrayList<Luchador> objetivos, final Pelea pelea,
	final Luchador lanzador, final TipoDaño tipo, final boolean esGC) {
		int modi = 0;
		Personaje perso = lanzador.getPersonaje();
		if (perso != null) {
			if (perso.tieneModfiSetClase(_hechizoID)) {
				modi = perso.getModifSetClase(_hechizoID, 283);
			}
		}
		if (_duracion == 0) {
			quitarInvisibilidad(lanzador, tipo);
			StringBuilder afectados = new StringBuilder();
			for (Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				objetivo = reenvioHechizo(pelea, _nivelHechizoID, _hechizoID, objetivo, lanzador, tipo);
				objetivo = sacrificio(pelea, objetivo);
				int daño = getRandomValor(lanzador);
				daño = getMaxMinHechizo(objetivo, daño);
				daño = getDañoAumentadoPorHechizo(lanzador, _hechizoID, daño);
				daño = calcularDañoFinal(pelea, lanzador, objetivo, Constantes.getElementoPorEfectoID(_efectoID), daño,
				_hechizoID, tipo, esGC);
				daño += modi;
				dañoPorEspalda(pelea, lanzador, objetivo, daño);
				daño = aplicarBuffContraGolpe(_efectoID, daño, objetivo, lanzador, pelea, _hechizoID, tipo);
				daño = restarPDVLuchador(pelea, objetivo, lanzador, afectados, daño);
				restarPDVLuchador(pelea, lanzador, lanzador, afectados, -daño / 2);
				curaSiLoGolpeas(objetivo, lanzador, afectados, daño);
			}
			if (afectados.length() > 0) {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 100, lanzador.getID() + "", afectados.toString());
			}
		} else {
			final ArrayList<Luchador> temp = new ArrayList<Luchador>();
			for (final Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				temp.add(objetivo);
			}
			if (!temp.isEmpty())
				pelea.addBuffLuchadores(temp, _efectoID, _primerValor, _duracion, _hechizoID, convertirArgs(_primerValor,
				_efectoID, _args), lanzador, tipo, _condicionHechizo);
		}
	}
	
	// daños elementales por parte de los hechizos
	protected void efecto_Daños_Elemental(final ArrayList<Luchador> objetivos, final Pelea pelea, final Luchador lanzador,
	final TipoDaño tipo, final boolean esGC) {
		int efectoID = getStatPorEfecto(_efectoID);
		int modi = 0;
		Personaje perso = lanzador.getPersonaje();
		if (perso != null) {
			if (perso.tieneModfiSetClase(_hechizoID)) {
				modi = perso.getModifSetClase(_hechizoID, 283);
			}
		}
		if (_duracion == 0) {
			quitarInvisibilidad(lanzador, tipo);
			StringBuilder afectados = new StringBuilder();
			for (Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				objetivo = reenvioHechizo(pelea, _nivelHechizoID, _hechizoID, objetivo, lanzador, tipo);
				objetivo = sacrificio(pelea, objetivo);
				int daño = getRandomValor(lanzador);
				daño = getMaxMinHechizo(objetivo, daño);
				daño = getDañoAumentadoPorHechizo(lanzador, _hechizoID, daño);
				daño = calcularDañoFinal(pelea, lanzador, objetivo, Constantes.getElementoPorEfectoID(_efectoID), daño,
				_hechizoID, tipo, esGC);
				daño += modi;
				dañoPorEspalda(pelea, lanzador, objetivo, daño);
				daño = aplicarBuffContraGolpe(efectoID, daño, objetivo, lanzador, pelea, _hechizoID, tipo);
				daño = restarPDVLuchador(pelea, objetivo, lanzador, afectados, daño);
				curaSiLoGolpeas(objetivo, lanzador, afectados, daño);
			}
			if (afectados.length() > 0) {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 100, lanzador.getID() + "", afectados.toString());
			}
		} else {
			final ArrayList<Luchador> temp = new ArrayList<Luchador>();
			for (final Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				temp.add(objetivo);
			}
			if (!temp.isEmpty())
				pelea.addBuffLuchadores(temp, efectoID, _primerValor, _duracion, _hechizoID, convertirArgs(_primerValor,
				efectoID, _args), lanzador, tipo, _condicionHechizo);
		}
	}
	
	// daños para el lanzador (fixe) (FIJOS)--> no sacrificio, no reenvio
	private void efecto_Daños_Para_Lanzador(final Pelea pelea, Luchador lanzador, Celda celdaObjetivo,
	final TipoDaño tipo, final boolean esGC) {
		int efectoID = getStatPorEfecto(_efectoID);
		if (_duracion == 0) {
			quitarInvisibilidad(lanzador, tipo);
			int daño = getRandomValor(lanzador);
			daño = calcularDañoFinal(pelea, lanzador, lanzador, Constantes.getElementoPorEfectoID(_efectoID), daño,
			_hechizoID, tipo, esGC);
			daño = aplicarBuffContraGolpe(efectoID, daño, lanzador, lanzador, pelea, _hechizoID, tipo);
			daño = restarPDVLuchador(pelea, lanzador, lanzador, null, daño);
		} else {
			lanzador.addBuffConGIE(efectoID, _primerValor, _duracion, _hechizoID, convertirArgs(_primerValor, efectoID,
			_args), lanzador, true, tipo, _condicionHechizo);
		}
	}
	
	private void efecto_Daños_Porc_Vida_Neutral(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo, final TipoDaño tipo, final boolean esGC) {
		final float val = getRandomValor(lanzador) / 100f;
		final int pdvMax = lanzador.getPDVMaxConBuff();
		final int pdvMedio = pdvMax / 2;
		float porc = 1;
		if (_efectoID == 672) {// enemigos
			porc = 1 - Math.abs(lanzador.getPDVConBuff() - pdvMedio) / (float) pdvMedio;
		}
		final int daño = (int) (val * pdvMax * porc);
		quitarInvisibilidad(lanzador, tipo);
		StringBuilder afectados = new StringBuilder();
		for (Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			objetivo = reenvioHechizo(pelea, _nivelHechizoID, _hechizoID, objetivo, lanzador, tipo);
			objetivo = sacrificio(pelea, objetivo);
			int daño2 = calcularDañoFinal(pelea, lanzador, objetivo, (byte) (10 + Constantes.ELEMENTO_NEUTRAL), daño,
			_hechizoID, tipo, esGC);
			daño2 = aplicarBuffContraGolpe(_efectoID, daño2, objetivo, lanzador, pelea, _hechizoID, tipo);
			daño2 = restarPDVLuchador(pelea, objetivo, lanzador, afectados, daño2);
		}
		if (afectados.length() > 0) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 100, lanzador.getID() + "", afectados.toString());
		}
	}
	
	private void efecto_Matar_Objetivo(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		quitarInvisibilidad(lanzador, TipoDaño.NORMAL);
		for (Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			if (_duracion == 0) {
				objetivo = sacrificio(pelea, objetivo);
			}
			pelea.addMuertosReturnFinalizo(objetivo, lanzador);
			try {
				Thread.sleep(500);
			} catch (final Exception e) {}
		}
	}
	
	private void efecto_Dominio_Arma(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		int efectoID = getStatPorEfecto(_efectoID);
		final ArrayList<Luchador> temp = new ArrayList<Luchador>();
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			temp.add(objetivo);
		}
		if (!temp.isEmpty()) {
			pelea.addBuffLuchadores(temp, efectoID, _primerValor, _duracion, _hechizoID, convertirArgs(_primerValor, efectoID,
			_args), lanzador, TipoDaño.POST_TURNOS, _condicionHechizo);
		}
	}
	
	private void efecto_Bonus_Malus(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo, TipoDaño tipo) {
		// solo tiene los mas pa y mas pm, no esta los menos
		switch (_hechizoID) {
			case 2210 :// furia de zobal
				if (celdaObjetivo.getPrimerLuchador() == null) {
					return;
				}
				break;
		}
		int valor = getRandomValor(lanzador);
		int efectoID = getStatPorEfecto(_efectoID);
		final int valor2 = valor;
		final ArrayList<Luchador> temp = new ArrayList<Luchador>();
		StringBuilder afectados = new StringBuilder();
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			valor = getMaxMinHechizo(objetivo, valor2);
			if (valor != valor2) {
				objetivo.addBuffConGIE(efectoID, valor, _duracion, _hechizoID, convertirArgs(valor, efectoID, _args), lanzador,
				true, tipo, _condicionHechizo);
			} else {
				temp.add(objetivo);
			}
			switch (_efectoID) {
				case 78 :// + PM
				case 111 :// + X PA
				case 128 :// + PM
				case 120 :// + PA segun el 3arg
				case 110 :// + X vida
				case 112 :// + Daños
				case 114 :// Multiplica los daños por X
				case 115 :// + Golpes Criticos
				case 116 :// - Alcance
				case 117 :// + Alcance
				case 118 :// + Fuerza
				case 119 :// + Agilidad
				case 121 :// + Daños
				case 122 :// + Fallos Criticos
				case 123 :// + Suerte
				case 124 :// + Sabiduria
				case 125 :// + Vitalidad
				case 126 :// + Inteligencia
				case 138 :// + % daños
				case 142 :// + Daños Fisicos
				case 144 :// - Daños (no boosteados)
				case 145 :// - Daños
				case 152 :// - Suerte
				case 153 :// - Vitalidad
				case 154 :// - Agilidad
				case 155 :// - Inteligencia
				case 156 :// - Sabiduria
				case 157 :// - Fuerza
				case 160 :// + Esquiva PA
				case 161 :// + Esquiva PM
				case 162 :// - Esquiva PA
				case 163 :// - Esquiva PM
				case 171 :// - Golpes Criticos
				case 176 :// + a las prospecciones
				case 177 :// - a las prospecciones
				case 178 :// + a las curaciones
				case 179 :// - a las curaciones
				case 182 :// + Invocaciones
				case 183 :// + Reduccion Magica
				case 184 :// + Reduccion Fisica
				case 186 :// Disminuye los daños %
					// case 210 :// Resist % tierra
					// case 211 :// Resist % agua
					// case 212 :// Resist % aire
					// case 213 :// Resist % fuego
					// case 214 :// Resist % neutral
					// case 215 :// Debilidad % tierra
					// case 216 :// Debilidad % agua
					// case 217 :// Debilidad % aire
					// case 218 :// Debilidad % fuego
					// case 219 :// Debilidad % neutral
				case 425 :// + daños empuje
				case 606 :// + sabiduria
				case 607 :// + fuerza
				case 608 :// + suerte
				case 609 :// + agilidad
				case 610 :// + vitalidad
				case 611 :// + inteligencia
				case 776 :// +% de los daños incurables sufridos
					if (afectados.length() > 0) {
						afectados.append("¬");
					}
					afectados.append(objetivo.getID() + "," + valor + "," + duracionFinal(objetivo));
					break;
			}
		}
		if (afectados.length() > 0 && _condicionHechizo.isEmpty()) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, _efectoID, lanzador.getID() + "", afectados.toString());
		}
		if (!temp.isEmpty()) {
			pelea.addBuffLuchadores(temp, efectoID, valor, _duracion, _hechizoID, convertirArgs(valor, efectoID, _args),
			lanzador, tipo, _condicionHechizo);
		}
	}
	
	private void efecto_Reenvio_Hechizo(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		int nivelMax = _segundoValor, efectoID = getStatPorEfecto(_efectoID);
		if (nivelMax == -1) {
			return;
		}
		final ArrayList<Luchador> temp = new ArrayList<Luchador>();
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			temp.add(objetivo);
		}
		if (!temp.isEmpty())
			pelea.addBuffLuchadores(temp, efectoID, nivelMax, _duracion, _hechizoID, convertirArgs(nivelMax, efectoID, _args),
			lanzador, TipoDaño.POST_TURNOS, _condicionHechizo);
		try {
			Thread.sleep(200);
		} catch (final Exception e) {}
	}
	
	private void efecto_Deshechizar(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {// deshechiza
		StringBuilder afectados = new StringBuilder();
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			if (afectados.length() > 0) {
				afectados.append("¬");
			}
			afectados.append(objetivo.getID() + "");
		}
		if (afectados.length() > 0 && _condicionHechizo.isEmpty()) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, _efectoID, lanzador.getID() + "", afectados.toString());
			for (final Luchador objetivo : objetivos) {
				if (objetivo.estaMuerto()) {
					continue;
				}
				objetivo.deshechizar(lanzador, true);
			}
		}
	}
	
	// cambia la apariencia
	private void efecto_Cambiar_Apariencia(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		int gfxID = _tercerValor;
		int efectoID = getStatPorEfecto(_efectoID);
		final ArrayList<Luchador> temp = new ArrayList<Luchador>();
		quitarInvisibilidad(lanzador, TipoDaño.NORMAL);
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			temp.add(objetivo);
			if (gfxID == 8010 && objetivo.getPersonaje() != null && objetivo.getPersonaje()
			.getSexo() == Constantes.SEXO_FEMENINO) {
				gfxID = 8011;
			}
			if (gfxID == -1 || _duracion == 0) {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, _efectoID, lanzador.getID() + "", objetivo.getID() + ","
				+ objetivo.getGfxID() + "," + objetivo.getGfxID());
			} else {
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, _efectoID, lanzador.getID() + "", objetivo.getID() + ","
				+ objetivo.getGfxID() + "," + gfxID + "," + duracionFinal(objetivo));
			}
		}
		if (gfxID > -1 && _duracion != 0) {
			if (!temp.isEmpty())
				pelea.addBuffLuchadores(temp, efectoID, gfxID, _duracion, _hechizoID, convertirArgs(gfxID, efectoID, _args),
				lanzador, TipoDaño.POST_TURNOS, _condicionHechizo);
		}
	}
	
	// vuelve invisible a un pj
	private void efecto_Invisibilidad(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		int efectoID = getStatPorEfecto(_efectoID);
		final ArrayList<Luchador> temp = new ArrayList<Luchador>();
		StringBuilder afectados = new StringBuilder();
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			temp.add(objetivo);
			objetivo.vaciarVisibles();
			if (afectados.length() > 0)
				afectados.append("¬");
			afectados.append(objetivo.getID() + "," + duracionFinal(objetivo));
		}
		if (afectados.length() > 0 && _condicionHechizo.isEmpty()) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, _efectoID, lanzador.getID() + "", afectados.toString());
		}
		if (!temp.isEmpty())
			pelea.addBuffLuchadores(temp, efectoID, _primerValor, _duracion, _hechizoID, convertirArgs(_primerValor, efectoID,
			_args), lanzador, TipoDaño.POST_TURNOS, _condicionHechizo);
	}
	
	// invocar doble
	private void efecto_Invoca_Doble(final Pelea pelea, Luchador lanzador, Celda celdaObjetivo) {
		if (lanzador.getNroInvocaciones() >= lanzador.getTotalStats().getTotalStatParaMostrar(182)) {
			GestorSalida.ENVIAR_Im_INFORMACION(lanzador.getPersonaje(), "0CANT_SUMMON_MORE_CREATURE;" + lanzador
			.getNroInvocaciones());
			return;
		}
		if (!celdaObjetivo.esCaminable(true)) {
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(pelea, 7, "1CELDA_NO_CAMINABLE");
			GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(pelea, 7, lanzador.getID(), celdaObjetivo.getID());
			return;
		}
		if (celdaObjetivo.getPrimerLuchador() != null) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 151, lanzador.getID() + "", _hechizoID + "");
			GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(pelea, 7, lanzador.getID(), celdaObjetivo.getID());
			return;
		}
		final Luchador doble = lanzador.clonarLuchador(pelea.getSigIDLuchador());
		doble.setEquipoBin(lanzador.getEquipoBin());
		doble.setInvocador(lanzador);
		doble.setPDVMAX(lanzador.getPDVMaxSinBuff(), false);
		doble.setPDV(lanzador.getPDVMaxSinBuff());
		doble.setCeldaPelea(celdaObjetivo);
		pelea.getOrdenLuchadores().add(pelea.getOrdenLuchadores().indexOf(lanzador) + 1, doble);
		pelea.addLuchadorEnEquipo(doble, lanzador.getEquipoBin());
		GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 180, lanzador.getID() + "", "+" + doble.stringGM(0));
		GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 999, lanzador.getID() + "", pelea.stringOrdenJugadores());
		lanzador.addNroInvocaciones(1);
		try {
			Thread.sleep(1000);
		} catch (final Exception e) {}
		verificaTrampas(doble);
		// pelea.actualizarNumTurnos(null);
	}
	
	// invocar una criatura
	private void efecto_Invoca_Mob(final Pelea pelea, Luchador lanzador, Celda celdaObjetivo) {
		if (lanzador.getNroInvocaciones() >= lanzador.getTotalStats().getTotalStatParaMostrar(
		Constantes.STAT_MAS_CRIATURAS_INVO)) {
			GestorSalida.ENVIAR_Im_INFORMACION(lanzador.getPersonaje(), "0CANT_SUMMON_MORE_CREATURE;" + lanzador
			.getNroInvocaciones());
			return;
		}
		if (_efectoID == 405) {// mata para invocar
			if (celdaObjetivo.getPrimerLuchador() != null) {
				pelea.addMuertosReturnFinalizo(celdaObjetivo.getPrimerLuchador(), lanzador);
				try {
					Thread.sleep(1000);
				} catch (final Exception e) {}
			}
		}
		if (!celdaObjetivo.esCaminable(true)) {
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(pelea, 7, "1CELDA_NO_CAMINABLE");
			GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(pelea, 7, lanzador.getID(), celdaObjetivo.getID());
			return;
		}
		if (celdaObjetivo.getPrimerLuchador() != null) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 151, lanzador.getID() + "", _hechizoID + "");
			GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(pelea, 7, lanzador.getID(), celdaObjetivo.getID());
			return;
		}
		int mobID = 0;
		byte mobNivel = 0;
		try {
			mobID = Integer.parseInt(_args.split(",")[0]);
			mobNivel = Byte.parseByte(_args.split(",")[1]);
		} catch (final Exception e) {}
		MobGrado mob = null;
		final int idInvocacion = pelea.getSigIDLuchador();
		try {
			mob = Mundo.getMobModelo(mobID).getGradoPorGrado(mobNivel).invocarMob(idInvocacion, false, lanzador);
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("El Mob ID esta reparandose: " + mobID);
			return;
		}
		final Luchador invocacion = new Luchador(pelea, mob, false);
		invocacion.setEquipoBin(lanzador.getEquipoBin());
		invocacion.setInvocador(lanzador);
		invocacion.setCeldaPelea(celdaObjetivo);
		pelea.getOrdenLuchadores().add(pelea.getOrdenLuchadores().indexOf(lanzador) + 1, invocacion);
		pelea.addLuchadorEnEquipo(invocacion, lanzador.getEquipoBin());
		GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 181, lanzador.getID() + "", "+" + invocacion.stringGM(0));
		GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 999, lanzador.getID() + "", pelea.stringOrdenJugadores());
		lanzador.addNroInvocaciones(1);
		if (MainServidor.PARAM_MOSTRAR_STATS_INVOCACION) {
			StringBuilder str = new StringBuilder();
			str.append("<b>STATS INVOCATION [</b>");
			str.append("<b>STR:</b> " + invocacion.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_FUERZA)
			+ ", ");
			str.append("<b>INT:</b> " + invocacion.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_INTELIGENCIA)
			+ ", ");
			str.append("<b>CHA:</b> " + invocacion.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_SUERTE)
			+ ", ");
			str.append("<b>AGI:</b> " + invocacion.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_AGILIDAD)
			+ "<b>]</b>");
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE_A_PELEA(pelea, str.toString(), Constantes.COLOR_NARANJA);
		}
		try {
			Thread.sleep(1000);
		} catch (final Exception e) {}
		switch (mobID) {
			case 556 :// zanahowia
			case 282 :// arbol
			case 2750 :// arbol de la vida
				invocacion.setEstatico(true);
				invocacion.setSirveParaBuff(false);
				break;
		}
		verificaTrampas(invocacion);
		// pelea.actualizarNumTurnos(null);
	}
	
	// invoca una criatura estatica
	private void efecto_Invoca_Estatico(final Pelea pelea, Luchador lanzador, Celda celdaObjetivo) {
		// if (lanzador.getNroInvocaciones() >= lanzador.getTotalStats().getStatParaMostrar(
		// CentroInfo.STAT_MAS_CRIATURAS_INVO)) {
		// GestorSalida.ENVIAR_Im_INFORMACION(lanzador.getPersonaje(), "0CANT_SUMMON_MORE_CREATURE;"
		// + lanzador.getNroInvocaciones());
		// return;
		// }
		if (!celdaObjetivo.esCaminable(true)) {
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(pelea, 7, "1CELDA_NO_CAMINABLE");
			GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(pelea, 7, lanzador.getID(), celdaObjetivo.getID());
			return;
		}
		if (celdaObjetivo.getPrimerLuchador() != null) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 151, lanzador.getID() + "", _hechizoID + "");
			GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(pelea, 7, lanzador.getID(), celdaObjetivo.getID());
			return;
		}
		int mobID = 0;
		byte mobnivel = 0;
		try {
			mobID = Integer.parseInt(_args.split(",")[0]);
			mobnivel = Byte.parseByte(_args.split(",")[1]);
		} catch (final Exception e) {}
		MobGrado mob = null;
		final int idInvocacion = pelea.getSigIDLuchador();
		try {
			mob = Mundo.getMobModelo(mobID).getGradoPorGrado(mobnivel).invocarMob(idInvocacion, false, lanzador);
		} catch (final Exception e) {
			MainServidor.redactarLogServidorln("El Mob ID esta mal configurado: " + mobID);
			return;
		}
		final Luchador invocacion = new Luchador(pelea, mob, false);
		final byte equipoLanz = lanzador.getEquipoBin();
		invocacion.setEquipoBin(equipoLanz);
		invocacion.setInvocador(lanzador);
		invocacion.setEstatico(true);
		invocacion.setSirveParaBuff(false);
		invocacion.setCeldaPelea(celdaObjetivo);
		pelea.addLuchadorEnEquipo(invocacion, equipoLanz);
		GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 185, lanzador.getID() + "", "+" + invocacion.stringGM(0));
		// lanzador.aumentarInvocaciones(); supuestamente no cuenta para estas
		try {
			Thread.sleep(1000);
		} catch (final Exception e) {}
		// invocacion.setEstado(Constantes.ESTADO_ARRAIGADO, -1);
		// invocacion.setEstado(Constantes.ESTADO_PESADO, -1);
		verificaTrampas(invocacion);
	}
	
	// invoca a un aliado muerto en combate, revivir
	private void efecto_Resucitar(final Pelea pelea, Luchador lanzador, Celda celdaObjetivo) {
		Luchador objetivo = null;
		for (int i = pelea.getListaMuertos().size() - 1; i >= 0; i--) {
			Luchador muerto = pelea.getListaMuertos().get(i);
			if (muerto.estaRetirado()) {
				continue;
			}
			if (muerto.getEquipoBin() == lanzador.getEquipoBin()) {
				if (muerto.esInvocacion()) {
					if (muerto.getInvocador().estaMuerto()) {
						continue;
					}
				}
				objetivo = muerto;
				break;
			}
		}
		if (objetivo == null) {
			return;
		}
		objetivo.setEstaMuerto(false);
		objetivo.setCeldaPelea(celdaObjetivo);
		objetivo.getBuffsPelea().clear();
		pelea.getListaMuertos().remove(objetivo);
		final int vida = _primerValor * objetivo.getPDVMaxConBuff() / 100;
		boolean iniciador = pelea.esLuchInicioPelea(objetivo);
		if (!iniciador) {
			pelea.getOrdenLuchadores().add(pelea.getOrdenLuchadores().indexOf(lanzador) + 1, objetivo);
		} else if (objetivo.getPersonaje() != null) {
			GestorSalida.ENVIAR_ILF_CANTIDAD_DE_VIDA(objetivo.getPersonaje(), vida);
		}
		objetivo.setPDV(vida);
		objetivo.setInvocador(lanzador);
		pelea.addLuchadorEnEquipo(objetivo, lanzador.getEquipoBin());
		GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, iniciador ? 147 : 780, lanzador.getID() + "", "+" + objetivo.stringGM(
		0));
		GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 999, lanzador.getID() + "", pelea.stringOrdenJugadores());
		if (objetivo.getPersonaje() != null) {
			GestorSalida.ENVIAR_As_STATS_DEL_PJ(objetivo.getPersonaje());
		}
		if (!iniciador) {
			lanzador.addNroInvocaciones(1);
		}
		try {
			Thread.sleep(1000);
		} catch (final Exception e) {}
		verificaTrampas(objetivo);
		// pelea.actualizarNumTurnos(null);
	}
	
	private void efecto_Releva_Invisibles(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {
		ArrayList<Luchador> aliados = pelea.luchadoresDeEquipo(lanzador.getParamEquipoAliado());
		final ArrayList<Celda> celdasObj = Camino.celdasAfectadasEnElArea(pelea.getMapaCopia(), celdaObjetivo.getID(),
		lanzador.getCeldaPelea().getID(), getZonaEfecto());
		for (final Luchador mostrar : pelea.luchadoresDeEquipo(3)) {
			if (mostrar.estaMuerto() || !mostrar.esInvisible(0)) {
				continue;
			}
			if (!celdasObj.contains(mostrar.getCeldaPelea())) {
				continue;
			}
			for (final Luchador aliado : aliados) {
				if (mostrar.esInvisible(aliado.getID())) {
					mostrar.aparecer(aliado);
				}
			}
		}
		if (pelea.getTrampas() != null) {
			for (final Trampa trampa : pelea.getTrampas()) {
				if (!celdasObj.contains(trampa.getCelda())) {
					continue;
				}
				trampa.aparecer(objetivos);
			}
		}
		//
	}
	
	// aumenta los daños del hechizo X
	private void efecto_Aumenta_Daños_Hechizo(final Pelea pelea, Luchador lanzador, Celda celdaObjetivo) {
		int efectoID = getStatPorEfecto(_efectoID);
		lanzador.addBuffConGIE(efectoID, _primerValor, _duracion, _hechizoID, convertirArgs(_primerValor, efectoID, _args),
		lanzador, true, TipoDaño.POST_TURNOS, _condicionHechizo);
	}
	
	// pone una trampa de nivel X
	private void efecto_Poner_Trampa(final Pelea pelea, Luchador lanzador, Celda celdaObjetivo) {
		if (!celdaObjetivo.esCaminable(true)) {
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(pelea, 7, "1CELDA_NO_CAMINABLE");
			GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(pelea, 7, lanzador.getID(), celdaObjetivo.getID());
			return;
		}
		if (celdaObjetivo.getPrimerLuchador() != null) {
			GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 151, lanzador.getID() + "", _hechizoID + "");
			GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(pelea, 7, lanzador.getID(), celdaObjetivo.getID());
			return;
		}
		if (celdaObjetivo.esTrampa()) {
			GestorSalida.ENVIAR_Im_INFORMACION(lanzador.getPersonaje(), "1229");
			GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(pelea, 7, lanzador.getID(), celdaObjetivo.getID());
			return;
		}
		final StatHechizo SH = Mundo.getHechizo(_primerValor).getStatsPorNivel(_segundoValor);
		if (SH == null)
			return;
		final byte tamaño = Encriptador.getNumeroPorValorHash(_zonaEfecto.charAt(1));
		final ArrayList<Celda> celdas = Camino.celdasAfectadasEnElArea(pelea.getMapaCopia(), celdaObjetivo.getID(), lanzador
		.getCeldaPelea().getID(), _zonaEfecto);
		new Trampa(pelea, lanzador, celdaObjetivo, tamaño, SH, _hechizoID, pelea.luchadoresDeEquipo(lanzador
		.getParamEquipoAliado()), celdas, _tercerValor);
	}
	
	// pone un glifo nivel X
	private void efecto_Glifo_Libre(final Pelea pelea, Luchador lanzador, Celda celdaObjetivo) {
		if (!celdaObjetivo.esCaminable(true)) {
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(pelea, 7, "1CELDA_NO_CAMINABLE");
			GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(pelea, 7, lanzador.getID(), celdaObjetivo.getID());
			return;
		}
		final StatHechizo SH = Mundo.getHechizo(_primerValor).getStatsPorNivel(_segundoValor);
		if (SH == null) {
			return;
		}
		final byte tamaño = Encriptador.getNumeroPorValorHash(_zonaEfecto.charAt(1));
		final char tipo = _zonaEfecto.charAt(0);
		final ArrayList<Celda> celdas = Camino.celdasAfectadasEnElArea(pelea.getMapaCopia(), celdaObjetivo.getID(), lanzador
		.getCeldaPelea().getID(), _zonaEfecto);
		new Glifo(pelea, lanzador, celdaObjetivo, tamaño, SH, _duracion, _hechizoID, true, celdas, _tercerValor, tipo);
	}
	
	// pone un glifo nivel X asi halla un jugador, efecto al final de turno
	private void efecto_Glifo_Fin_Turno(final Pelea pelea, Luchador lanzador, Celda celdaObjetivo) {
		if (!celdaObjetivo.esCaminable(true)) {
			GestorSalida.ENVIAR_Im_INFORMACION_A_PELEA(pelea, 7, "1CELDA_NO_CAMINABLE");
			GestorSalida.ENVIAR_Gf_MOSTRAR_CELDA_EN_PELEA(pelea, 7, lanzador.getID(), celdaObjetivo.getID());
			return;
		}
		final StatHechizo SH = Mundo.getHechizo(_primerValor).getStatsPorNivel(_segundoValor);
		if (SH == null) {
			return;
		}
		final byte tamaño = Encriptador.getNumeroPorValorHash(_zonaEfecto.charAt(1));
		final ArrayList<Celda> celdas = Camino.celdasAfectadasEnElArea(pelea.getMapaCopia(), celdaObjetivo.getID(), lanzador
		.getCeldaPelea().getID(), _zonaEfecto);
		new Glifo(pelea, lanzador, celdaObjetivo, tamaño, SH, _duracion, _hechizoID, false, celdas, _tercerValor,
		_zonaEfecto.charAt(0));
	}
	
	// hechizo miedo
	private void efecto_Retrocede_Hasta_Cierta_Casilla(final Pelea pelea, Luchador lanzador, Celda celdaObjetivo) {
		final Celda celdaLanzamiento = lanzador.getCeldaPelea();
		final Mapa mapaCopia = pelea.getMapaCopia();
		final int dir = Camino.direccionEntreDosCeldas(mapaCopia, celdaLanzamiento.getID(), celdaObjetivo.getID(), true);
		final short sigCeldaID = Camino.getSigIDCeldaMismaDir(celdaLanzamiento.getID(), dir, mapaCopia, true);
		final Celda sigCelda = mapaCopia.getCelda(sigCeldaID);
		if (sigCelda == null || sigCelda.getPrimerLuchador() == null) {
			return;
		}
		final Luchador objetivo = sigCelda.getPrimerLuchador();
		if (objetivo.estaMuerto() || objetivo.tieneEstado(Constantes.ESTADO_ARRAIGADO)) {
			return;
		}
		int distancia = Camino.distanciaDosCeldas(mapaCopia, sigCeldaID, celdaObjetivo.getID());
		efectoEmpujones(pelea, lanzador, objetivo, celdaLanzamiento, sigCelda, distancia, false);
	}
	
	private void efecto_Teleport_Inicio(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {// teletransporta
		if (lanzador.tieneEstado(Constantes.ESTADO_PESADO)) {
			return;
		}
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto() || objetivo.esInvocacion()) {
				continue;
			}
			Celda celda1 = null;
			for (final Entry<Integer, Celda> entry : pelea.getPosInicial().entrySet()) {
				if (entry.getKey() == objetivo.getID()) {
					celda1 = entry.getValue();
					break;
				}
			}
			if (celda1.esCaminable(true) && celda1.getPrimerLuchador() == null) {
				objetivo.getCeldaPelea().moverLuchadoresACelda(celda1);
				GestorSalida.ENVIAR_GA_ACCION_PELEA(pelea, 7, 4, lanzador.getID() + "", objetivo.getID() + "," + celda1
				.getID());
				verificaTrampas(objetivo);
			}
		}
	}
	
	private void efecto_Estados(final ArrayList<Luchador> objetivos, final Pelea pelea, Luchador lanzador,
	Celda celdaObjetivo) {// estatdo X
		int estadoID = _tercerValor;
		if (estadoID == -1) {
			return;
		}
		final ArrayList<Luchador> temp = new ArrayList<Luchador>();
		// StringBuilder afectados = new StringBuilder();
		for (final Luchador objetivo : objetivos) {
			if (objetivo.estaMuerto()) {
				continue;
			}
			temp.add(objetivo);
			if (_efectoID == Constantes.STAT_QUITAR_ESTADO && !objetivo.tieneEstado(estadoID)) {
				continue;
			}
			// if (afectados.length() > 0) {
			// afectados.append("¬");
			// }
			// afectados.append(objetivo.getID() + "," + idEstado + "," + (_efectoID ==
			// Constantes.STAT_DAR_ESTADO ? 1 : 0));
		}
		if (!temp.isEmpty()) {
			pelea.addBuffLuchadores(temp, _efectoID, estadoID, _duracion, _hechizoID, convertirArgs(estadoID, _efectoID,
			_args), lanzador, TipoDaño.POST_TURNOS, _condicionHechizo);
		}
	}
}