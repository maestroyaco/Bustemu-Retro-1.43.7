package estaticos;

import java.security.SecureRandom;
import java.util.ArrayList;
import variables.gremio.Recaudador;
import variables.mapa.Mapa;
import variables.pelea.Botin;
import variables.pelea.Luchador;
import variables.personaje.Personaje;
import variables.stats.TotalStats;

public class Formulas {
	public static SecureRandom RANDOM = new SecureRandom();
	
	public static void lanzarError() {
		try {
			Integer.parseInt("3RR0R");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean valorValido(int cantidad, int precio) {
		// if (cantidad > 1000) {
		// return false;
		// }
		if (precio == 0 || cantidad == 0) {
			return true;
		}
		boolean signo = precio >= 0;
		for (int i = 0; i < cantidad; i++) {
			boolean signo2 = precio * (i + 1) >= 0;
			if (signo2 != signo) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean getRandomBoolean() {
		return RANDOM.nextBoolean();
	}
	
	public static int getRandomInt(final int i1, final int i2) {
		try {
			if (i1 < 0) {
				return i2;
			}
			if (i2 < 0) {
				return i1;
			}
			if (i1 > i2) {
				return RANDOM.nextInt(i1 - i2 + 1) + i2;
			} else {
				return RANDOM.nextInt(i2 - i1 + 1) + i1;
			}
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static long getRandomLong(final long i1, final long i2) {
		try {
			if (i1 < 0) {
				return i2;
			}
			if (i2 < 0) {
				return i1;
			}
			if (i1 > i2) {
				return (RANDOM.nextLong() % (i1 - i2)) + i2;
			} else {
				return (RANDOM.nextLong() % (i2 - i1)) + i1;
			}
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static int[] formatoTiempo(long milis) {
		int[] f = {1, 1000, 60000, 3600000, 86400000};
		int[] formato = new int[f.length];
		for (int i = f.length - 1; i >= 0; i--) {
			formato[i] = (int) (milis / f[i]);
			milis %= f[i];
		}
		return formato;
	}
	
	public static int calcularCosteZaap(final Mapa mapa1, final Mapa mapa2) {
		if (mapa1.getID() == mapa2.getID()) {
			return 0;
		}
		return 10 * Camino.distanciaEntreMapas(mapa1, mapa2);
	}
	
	// @SuppressWarnings("unused")
	// private static long getXPGanadaPVM(final ArrayList<Luchador> luchadores, final Luchador
	// luchRec, int nivelGrupoPJ,
	// int nivelGrupoMob, long totalExp, float coefBonus) {
	// if (luchRec.getPersonaje() == null) {
	// return 0;
	// }
	// final TotalStats totalStats = luchRec.getTotalStats();
	// // 910 multiplicar la xp
	// int numJugadores = 0;
	// final float coefSab = (totalStats.getStatParaMostrar(Constantes.STAT_MAS_SABIDURIA)
	// + totalStats.getStatParaMostrar(Constantes.STAT_MAS_PORC_EXP) + 100) / 100f;
	// float coefEntreNiv = nivelGrupoMob / (float) nivelGrupoPJ;
	// if (coefEntreNiv <= 1.1f && coefEntreNiv >= 0.9) {
	// coefEntreNiv = 1;
	// } else if (coefEntreNiv > 1) {
	// coefEntreNiv = 1 / coefEntreNiv;
	// } else if (coefEntreNiv < 0.01) {
	// coefEntreNiv = 0.01f;
	// }
	// for (final Luchador luch : luchadores) {
	// if (luch.esInvocacion() || luch.estaRetirado()) {
	// continue;
	// }
	// numJugadores++;
	// }
	// float coefMul = 1;
	// switch (numJugadores) {
	// case 0 :
	// coefMul = 0.5f;
	// break;
	// case 1 :
	// coefMul = 1;
	// break;
	// case 2 :
	// coefMul = 1.1f;
	// break;
	// case 3 :
	// coefMul = 1.5f;
	// break;
	// case 4 :
	// coefMul = 2.3f;
	// break;
	// case 5 :
	// coefMul = 3.1f;
	// break;
	// case 6 :
	// coefMul = 3.6f;
	// break;
	// case 7 :
	// coefMul = 4.2f;
	// break;
	// case 8 :
	// coefMul = 4.7f;
	// break;
	// default :
	// coefMul = 4.7f;
	// break;
	// }
	// long expFinal = (long) ((1 + coefSab + coefBonus) * (coefMul + coefEntreNiv) * (totalExp /
	// numJugadores))
	// * MainServidor.RATE_XP_PVM;
	// if (expFinal < 0) {
	// expFinal = 0;
	// }
	// return expFinal;
	// }
	public static long getXPGanadaRecau(final Recaudador recaudador, final long totalXP) {
		final float coef = (recaudador.getGremio().getStatRecolecta(Constantes.STAT_MAS_SABIDURIA) + 100) / 100f;
		return (long) (coef * totalXP);
	}
	
	public static long getXPOficial(final ArrayList<Luchador> luchadores, final ArrayList<Luchador> mobs,
	final Luchador luchador, float coefEstrellas, float coefReto, boolean gano) {
		int sumaNivelesLuch = 0;
		int maxNivelLuch = 0;
		int sumaNivelesMobs = 0;
		int maxNivelMob = 0;
		int sumaExpMobs = 0;
		int cantLuch = 0;
		boolean esRecaudador = true;
		if (luchadores != null) {
			for (final Luchador luch : luchadores) {
				if (luch.getID() == luchador.getID()) {
					esRecaudador = false;
				}
				if (luch.esInvocacion() || luch.estaRetirado()) {
					continue;
				}
				cantLuch++;
				sumaNivelesLuch += luch.getNivelViejo();
				if (maxNivelLuch < luch.getNivelViejo()) {
					maxNivelLuch = luch.getNivelViejo();
				}
			}
		} else {
			sumaNivelesLuch = luchador.getNivelViejo();
			maxNivelLuch = luchador.getNivelViejo();
		}
		for (final Luchador luch : mobs) {
			if (luch.esInvocacion() || luch.estaRetirado()) {
				continue;
			}
			if (luch.getMob() == null) {
				continue;
			}
			if (gano || (luch.getMuertoPor() != null && luch.getMuertoPor().getEquipoBin() == luchador.getEquipoBin())) {
				sumaExpMobs += luch.getMob().getBaseXp();
				sumaNivelesMobs += luch.getNivelViejo();
				if (maxNivelMob < luch.getNivelViejo()) {
					maxNivelMob = luch.getNivelViejo();
				}
			}
		}
		if (sumaExpMobs <= 0) {
			return 0;
		}
		final float coefSab = (luchador.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_SABIDURIA) + luchador
		.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_PORC_EXP) + 100) / 100f;
		// ahora se calcula la media
		float coefMobLuch = (Math.min(luchador.getNivelViejo(), Math.round(2.5f * maxNivelMob)) / (float) sumaNivelesLuch);
		if (coefMobLuch > 1) {
			coefMobLuch = 1;
		} else if (coefMobLuch < 0.2f) {
			coefMobLuch = 0.2f;
		}
		int ratioLuch = 0;
		if (!esRecaudador && gano) {
			for (final Luchador luch : luchadores) {
				if (luch.esInvocacion() || luch.estaRetirado()) {
					continue;
				}
				if (luch.getNivelViejo() >= maxNivelLuch / 3) {
					ratioLuch++;
				}
			}
		}
		float coefNivel = 1;
		if (cantLuch > 1) {
			if (sumaNivelesLuch - 5 > sumaNivelesMobs) {
				coefNivel = (float) sumaNivelesMobs / sumaNivelesLuch;
			} else if (sumaNivelesLuch + 10 < sumaNivelesMobs) {
				coefNivel = (sumaNivelesLuch + 10) / (float) sumaNivelesMobs;
			}
		}
		if (coefNivel > 1.2f) {
			coefNivel = 1.2f;
		} else if (coefNivel < 0.8f) {
			coefNivel = 0.8f;
		}
		float coefMult = 0;
		switch (ratioLuch) {
			case 0 :
				coefMult = 0.5f;
				break;
			case 1 :
				coefMult = 1;
				break;
			case 2 :
				coefMult = 1.1f;
				break;
			case 3 :
				coefMult = 1.5f;
				break;
			case 4 :
				coefMult = 2.3f;
				break;
			case 5 :
				coefMult = 3.1f;
				break;
			case 6 :
				coefMult = 3.6f;
				break;
			case 7 :
				coefMult = 4.2f;
				break;
			case 8 :
			default :
				coefMult = 4.7f;
				break;
		}
		long baseXp = sumaExpMobs;
		if (!esRecaudador) {
			baseXp = (long) (baseXp * coefMult * coefMobLuch * coefNivel);
		}
		long xp = (long) (Math.round(baseXp * coefSab));
		if (!esRecaudador) {
			xp = (long) (xp * (coefReto + coefEstrellas + MainServidor.RATE_XP_PVM));
		} else {
			xp = xp * MainServidor.RATE_XP_RECAUDADOR;
		}
		if (luchador.getPersonaje() != null) {
			if (MainServidor.RATE_XP_PVM_ABONADOS > 1) {
				if (luchador.getPersonaje().esAbonado()) {
					xp *= MainServidor.RATE_XP_PVM_ABONADOS;
				}
			}
		}
		if (MainServidor.MODO_DEBUG) {
			System.out.println("suma exp " + sumaExpMobs);
			System.out.println("ratioLuch " + ratioLuch);
			System.out.println("coefMob " + coefMobLuch);
			System.out.println("coefMult " + coefMult);
			System.out.println("coefSab " + coefSab);
			System.out.println("coefReto " + coefReto);
			System.out.println("coefEstrellas " + coefEstrellas);
			System.out.println("MainServidor.RATE_XP_PVM " + MainServidor.RATE_XP_PVM);
			System.out.println("sumaExpMobs " + sumaExpMobs);
			System.out.println("baseXp " + baseXp);
			System.out.println("xp es " + xp);
		}
		if (xp < 1) {
			xp = 0;
		}
		return xp;
	}
	
	public static float getRandomDecimal(int decimales) {
		int entero = RANDOM.nextInt(100);
		float decimal = 0;
		if (decimales > 0) {
			int b = (int) Math.pow(10, decimales);
			decimal = (RANDOM.nextInt(b) + 1) / (float) b;
		}
		return entero + decimal;
	}
	
	public static float getPorcDropLuchador(float porcDrop, Luchador luch) {
		porcDrop += (luch.getProspeccionLuchador() - 100) / 1000f;
		if (porcDrop < 0.01) {
			porcDrop = 0.01f;
		}
		return porcDrop;
	}
	
	public static float getPorcParaDropAlEquipo(int prospecEquipo, float coefEstrellas, float coefReto, Botin drop,
	int cantDropeadores) {
		// int pp = prospec * (coefReto + coefEstrellas +rate);
		float porcDrop = drop.getPorcentajeBotin() * 1000;
		int cantCeros = 0;
		if (porcDrop >= 1) {
			cantCeros = (int) Math.log10(porcDrop) + 1;
		}
		int rate = 0;
		float porcEquipo = (prospecEquipo - drop.getProspeccionBotin()) * MainServidor.FACTOR_PLUS_PP_PARA_DROP
		* cantDropeadores;
		int factor = MainServidor.FACTOR_ZERO_DROP;
		if (drop.esDropFijo()) {
			rate = MainServidor.RATE_DROP_ARMAS_ETEREAS;
			factor += 3;
		} else {
			rate = MainServidor.RATE_DROP_NORMAL;
		}
		if (cantCeros < factor) {
			// si factor zero es mas alto, mayor sera la dificultad para dropear
			porcEquipo = (int) (porcEquipo / Math.pow(10, (factor - cantCeros)));
		}
		porcDrop += porcEquipo;
		float coef = rate;
		if (!MainServidor.PARAM_PERMITIR_BONUS_PELEA_AFECTEN_PROSPECCION) {
			coef += coefReto + coefEstrellas;
		}
		int entero = (int) (porcDrop / 1000);
		int decimal = (int) (porcDrop % 1000);
		entero += Math.sqrt(entero) * coef;
		decimal += Math.sqrt(decimal) * coef;
		float finalDrop = entero + (decimal / 1000f);
		return finalDrop;// decimal
	}
	
	public static int getIniciativa(TotalStats totalStats, float coefPDV) {
		int iniciativa = 0;
		iniciativa += totalStats.getTotalStatParaMostrar(Constantes.STAT_MAS_INICIATIVA);// iniciativa
		iniciativa += totalStats.getTotalStatParaMostrar(Constantes.STAT_MAS_AGILIDAD);
		iniciativa += totalStats.getTotalStatParaMostrar(Constantes.STAT_MAS_INTELIGENCIA);
		iniciativa += totalStats.getTotalStatParaMostrar(Constantes.STAT_MAS_SUERTE);
		iniciativa += totalStats.getTotalStatParaMostrar(Constantes.STAT_MAS_FUERZA);
		// iniciativa += getPDVMax() / fact;
		iniciativa *= coefPDV / 100;
		if (iniciativa < 0) {
			iniciativa = 0;
		}
		return iniciativa;
	}
	
	public static long getXPDonada(final int nivelPerso, final int nivelOtro, final long xpGanada) {
		final int dif = nivelPerso - nivelOtro;
		float coef = 0.1f;
		if (dif < 10) {
			coef = 0.1f;
		} else if (dif < 20) {
			coef = 0.08f;
		} else if (dif < 30) {
			coef = 0.06f;
		} else if (dif < 40) {
			coef = 0.04f;
		} else if (dif < 50) {
			coef = 0.03f;
		} else if (dif < 60) {
			coef = 0.02f;
		} else if (dif < 70) {
			coef = 0.015f;
		} else if (dif > 70) {
			coef = 0.01f;
		}
		return (long) (xpGanada * coef);
	}
	
	public static long getXPMision(int nivelGanador) {
		if (nivelGanador >= MainServidor.NIVEL_MAX_PERSONAJE) {
			nivelGanador = MainServidor.NIVEL_MAX_PERSONAJE - 1;
		}
		long experiencia = Mundo.getExpCazaCabezas(nivelGanador);
		return experiencia * MainServidor.RATE_XP_PVP;
		// float coef = 0.125f;
		// if (nivelGanador > nivelPerdedor) {
		// coef = 1 / ((float) Math.sqrt(nivelGanador - nivelPerdedor) * 8);
		// } else if (nivelGanador < nivelPerdedor) {
		// coef = (2 / (float) Math.sqrt(nivelGanador - nivelPerdedor));
		// }
		// return (long) (exp * Bustemu.RATE_XP_PVP * coef * 8);
	}
	
	public static int getHonorGanado(final ArrayList<Luchador> ganadores, final ArrayList<Luchador> perdedores,
	Luchador recibidor, boolean peleaMobs) {
		if (peleaMobs) {
			return 0;
		}
		int totalNivLuchGanador = 0, totalNivLuchPerdedor = 0;
		byte cantGanadores = 0, cantPerdedores = 0;
		ArrayList<String> ips = new ArrayList<>(16);
		ArrayList<Luchador> oGanadores = new ArrayList<Luchador>();
		ArrayList<Luchador> oPerdedores = new ArrayList<Luchador>();
		while (oGanadores.size() < ganadores.size()) {
			int mayor = -1;
			Luchador lTemp = null;
			for (final Luchador luch : ganadores) {
				if (oGanadores.contains(luch)) {
					continue;
				}
				if (luch.getNivelViejo() > mayor) {
					mayor = luch.getNivelViejo();
					lTemp = luch;
				}
			}
			if (lTemp != null) {
				oGanadores.add(lTemp);
			}
		}
		while (oPerdedores.size() < perdedores.size()) {
			int mayor = -1;
			Luchador lTemp = null;
			for (final Luchador luch : perdedores) {
				if (oPerdedores.contains(luch)) {
					continue;
				}
				if (luch.getNivelViejo() > mayor) {
					mayor = luch.getNivelViejo();
					lTemp = luch;
				}
			}
			if (lTemp != null) {
				oPerdedores.add(lTemp);
			}
		}
		int i = 1;
		for (final Luchador luch : oGanadores) {
			if (luch.esInvocacion()) {
				continue;
			}
			if (luch.getAlineacion() == Constantes.ALINEACION_NEUTRAL) {
				return 0;
			}
			if (!MainServidor.ES_LOCALHOST) {
				if (luch.getPersonaje() != null) {
					ips.add(luch.getPersonaje().getCuenta().getActualIP());
				}
			}
			totalNivLuchGanador += luch.getNivelViejo() / i;
			cantGanadores++;
		}
		i = 1;
		for (final Luchador luch : oPerdedores) {
			if (luch.esInvocacion()) {
				continue;
			}
			if (luch.getAlineacion() == Constantes.ALINEACION_NEUTRAL) {
				return 0;
			}
			if (!MainServidor.ES_LOCALHOST) {
				if (luch.getPersonaje() != null) {
					if (ips.contains(luch.getPersonaje().getCuenta().getActualIP())) {
						return 0;
					}
				}
			}
			totalNivLuchPerdedor += luch.getNivelViejo() / i;
			// totalNivAlinPerdedor += luch.getNivelAlineacion();
			cantPerdedores++;
		}
		// System.out.println("totalNivLuchGanador " + totalNivLuchGanador);
		// System.out.println("cantPerdedores " + cantPerdedores);
		// System.out.println("totalNivLuchPerdedor " + totalNivLuchPerdedor);
		// System.out.println("cantGanadores " + cantGanadores);
		if (cantGanadores == 0 || cantPerdedores == 0) {
			return 0;
		}
		boolean paso = false;
		int honor = 0;
		int porcPerd = totalNivLuchPerdedor * 20 / 100;
		if (totalNivLuchGanador <= totalNivLuchPerdedor || (Math.abs(totalNivLuchPerdedor
		- totalNivLuchGanador) < MainServidor.RANGO_NIVEL_PVP) || (Math.abs(totalNivLuchPerdedor
		- totalNivLuchGanador) < porcPerd)) {
			paso = true;
		}
		// System.out.println("porcPerd " + porcPerd);
		// System.out.println("paso " + paso);
		if (!paso) {
			return 0;
		}
		int nivelAlin = recibidor.getNivelAlineacion();
		if (nivelAlin < 1) {
			nivelAlin = 1;
		} else if (nivelAlin > 10) {
			nivelAlin = 10;
		}
		if (MainServidor.PARAM_GANAR_HONOR_RANDOM) {
			if (!ganadores.contains(recibidor)) {
				// para el perdedor negativo
				honor = -(Mundo.getExpAlineacion(nivelAlin) * 5 / 100);
			} else {
				honor = getRandomInt(80, peleaMobs ? 60 : 120);
			}
		} else {
			float ratio = Math.min(2.0F, (float) totalNivLuchPerdedor / totalNivLuchGanador);
			int xp = Mundo.getExpParaNivelAlineacion(nivelAlin + 1);
			// System.out.println("ratio " + ratio);
			// System.out.println("xp " + xp);
			honor = (int) (xp * ratio * 10.0F / 100.0F);
			honor = Math.min(peleaMobs ? 200 : 400, honor);
			honor = Math.max(honor, 0);
			if (!ganadores.contains(recibidor)) {
				// para el perdedor negativo
				honor = -honor;
			}
		}
		return MainServidor.RATE_HONOR * honor;
	}
	
	public static long getKamasGanadas(long maxKamas, final long minKamas, Personaje perso) {
		long posiblesKamas = 0;
		if (minKamas > maxKamas) {
			posiblesKamas = getRandomLong(maxKamas, minKamas);
		} else {
			posiblesKamas = getRandomLong(maxKamas, minKamas);
		}
		float coef = (float) Math.sqrt(MainServidor.RATE_KAMAS);
		if (perso != null) {
			if (MainServidor.RATE_KAMAS_ABONADOS > 1) {
				if (perso.esAbonado()) {
					coef += Math.sqrt(MainServidor.RATE_KAMAS_ABONADOS) - 1;
				}
			}
		}
		posiblesKamas = (long) (posiblesKamas * coef);
		return posiblesKamas;
	}
	
	public static int getKamasKoliseo(final int nivel) {
		return (int) (Math.sqrt(nivel) * MainServidor.KOLISEO_PREMIO_KAMAS);
	}
}
