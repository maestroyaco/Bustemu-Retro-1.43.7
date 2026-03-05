package variables.oficio;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import sprites.Exchanger;
import variables.mapa.Celda;
import variables.objeto.Objeto;
import variables.objeto.ObjetoModelo;
import variables.objeto.ObjetoModelo.CAPACIDAD_STATS;
import variables.personaje.Personaje;
import estaticos.Constantes;
import estaticos.Formulas;
import estaticos.GestorSalida;
import estaticos.MainServidor;
import estaticos.Mundo;
import estaticos.Mundo.Duo;

public class Trabajo implements Runnable, Exchanger {
	public static final byte MENSAJE_SIN_RESULTADO = 0;
	public static final byte MENSAJE_OBJETO_FABRICADO = 1;
	public static final byte MENSAJE_INTERRUMPIDA = 2;
	public static final byte MENSAJE_FALTA_RECURSOS = 3;
	public static final byte MENSAJE_RECETA_NO_FUNCIONA = 4;
	public static final byte MAGUEO_EXO = 2;
	public static final byte MAGUEO_OVER = 1;
	public static final byte MAGUEO_NORMAL = 0;
	public static final byte RESULTADO_EXITO_CRITICO = 1;
	public static final byte RESULTADO_EXITO_NORMAL = 2;
	public static final byte RESULTADO_FALLO_NORMAL = 3;
	public static final byte RESULTADO_FALLO_CRITICO = 4;
	// private static float TOLERANCIA_NORMAL = 1.0f, TOLERANCIA_VIP = 1.8f;
	private final int _skillID;
	private int _casillasMin = 1, _casillasMax = 1;
	private int _suerteCraft = 100, _tiempoRecoleccion = 0, _xpGanadaRecoleccion = 0, _cuantasRepeticiones = 0;
	private final boolean _esCraft, _esForjaMagia;
	private boolean _varios;
	private byte _interrumpir;
	private Map<Integer, Integer> _ingredientes, _ultimosIngredientes;
	private Personaje _artesano, _cliente;
	private Celda _celda;
	private final StatOficio _statOficio;
	private boolean _finThread = true;
	// taller
	private long _kamasPago, _kamasSiSeConsigue = 0;
	private boolean _ok1, _ok2;
	private ArrayList<Duo<Integer, Integer>> _objArtesano, _objCliente, _objetosPago, _objetosSiSeConsegui;
	
	public Trabajo(final int idSKill, final int min, final int max, final boolean esCraft, final int nSuerteTiempo,
	final int xpGanada, final StatOficio oficio) {
		_skillID = idSKill;
		_casillasMax = max;
		_casillasMin = min;
		_statOficio = oficio;
		if (_esCraft = esCraft) {
			_suerteCraft = nSuerteTiempo;
			_ingredientes = new TreeMap<>();
			_ultimosIngredientes = new TreeMap<>();
		} else {
			_tiempoRecoleccion = nSuerteTiempo;
			_xpGanadaRecoleccion = xpGanada;
		}
		_esForjaMagia = Constantes.esOficioMago(_statOficio.getOficio().getID());
	}
	
	synchronized boolean iniciarTrabajo(final Personaje perso, final int idUnica, final Celda celda) {
		_artesano = perso;
		_celda = celda;
		if (_esCraft) {
			GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(_artesano, 3, _casillasMax + ";" + _skillID);
			GestorSalida.ENVIAR_GDF_FORZADO_PERSONAJE(_artesano, _celda.getID() + ";" + 2 + ";" + 1);
			_artesano.setTipoExchange(Constantes.INTERCAMBIO_TIPO_TALLER);
			_artesano.setExchanger(this);
			return false;
		} else {
			// Recolecta
			if (_celda.getObjetoInteractivo() != null && _celda.getObjetoInteractivo().puedeIniciarRecolecta()) {
				_celda.getObjetoInteractivo().iniciarRecolecta(_tiempoRecoleccion);
				GestorSalida.ENVIAR_GA_ACCION_JUEGO_AL_MAPA(_artesano.getMapa(), idUnica, 501, _artesano.getID() + "", _celda
				.getID() + "," + _tiempoRecoleccion);
				iniciarThread();
			}
			return true;
		}
	}
	
	public boolean puedeFinalizarRecolecta() {
		if (_celda.getObjetoInteractivo() == null) {
			return false;
		}
		return _celda.getObjetoInteractivo().puedeFinalizarRecolecta();
	}
	
	public synchronized int getExpFinalizarRecoleccion() {
		if (_celda.getObjetoInteractivo() == null) {
			return 0;
		}
		float coefEstrellas = _celda.getObjetoInteractivo().getBonusEstrellas() / 100f;
		_celda.getObjetoInteractivo().activandoRecarga(Constantes.OI_ESTADO_VACIANDO, Constantes.OI_ESTADO_VACIO);
		return (int) (preExp(_xpGanadaRecoleccion) + (_xpGanadaRecoleccion * coefEstrellas));
	}
	
	private int preExp(int exp) {
		int finalExp = exp *= MainServidor.RATE_XP_OFICIO;
		if (_artesano != null) {
			if (MainServidor.RATE_XP_OFICIO_ABONADOS > 1) {
				if (_artesano.esAbonado()) {
					finalExp *= MainServidor.RATE_XP_OFICIO_ABONADOS;
				}
			}
		}
		return finalExp;
	}
	
	public synchronized void recogerRecolecta() {
		if (_celda.getObjetoInteractivo() == null) {
			return;
		}
		int estrellas = _celda.getObjetoInteractivo().getBonusEstrellas();
		final boolean especial = Formulas.getRandomInt(0, 100 - MainServidor.PROBABILIDAD_RECURSO_ESPECIAL) == 0;
		int cantidad = _casillasMax > _casillasMin ? Formulas.getRandomInt(_casillasMin, _casillasMax) : _casillasMin;
		if (especial) {
			cantidad = 1;
		}
		int cantidadTotal = cantidad;
		if (_artesano.alasActivadas() && _artesano.getMapa().getSubArea().getAlineacion() == _artesano.getAlineacion()) {
			final float balance = Mundo.getBalanceMundo(_artesano);
			final float bonusExp = Mundo.getBonusAlinExp(_artesano);
			cantidadTotal += (cantidad * balance * bonusExp / 100);
		}
		cantidadTotal += (cantidad * estrellas / 100);
		if (cantidadTotal > 0) {
			final ObjetoModelo OM = Mundo.getObjetoModelo(Constantes.getObjetoPorRecurso(_skillID, especial));
			if (OM != null) {
				_artesano.addObjIdentAInventario(OM.crearObjeto(cantidadTotal, Constantes.OBJETO_POS_NO_EQUIPADO,
				CAPACIDAD_STATS.RANDOM), false);
				GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_artesano);
			} else {
				MainServidor.redactarLogServidorln("El idTrabajoMod " + _skillID + " no tiene objeto para recolectar");
			}
			GestorSalida.ENVIAR_IQ_NUMERO_ARRIBA_PJ(_artesano, _artesano.getID(), cantidadTotal);
		}
	}
	
	public int getTrabajoID() {
		return _skillID;
	}
	
	public int getCasillasMin() {
		return _casillasMin;
	}
	
	public int getCasillasMax() {
		return _casillasMax;
	}
	
	public int getSuerte() {
		return _suerteCraft;
	}
	
	public Celda getCelda() {
		return _celda;
	}
	
	public int getTiempo() {
		return _tiempoRecoleccion;
	}
	
	public boolean esCraft() {
		return _esCraft;
	}
	
	public boolean esTaller() {
		return _cliente != null;
	}
	
	public boolean esFM() {
		return _esForjaMagia;
	}
	
	public void mostrarProbabilidades(final Personaje perso) {
		final int precio = MainServidor.KAMAS_MOSTRAR_PROBABILIDAD_FORJA;
		if (precio <= 0) {
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>NO SE PUEDE USAR ESTA ACCION</b>", Constantes.COLOR_ROJO);
			return;
		}
		if (perso.getKamas() < precio) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "1128;" + precio);
			return;
		}
		perso.addKamas(-precio, true, true);
		Objeto objAMaguear = null, objRunaOPocima = null;
		int statMagueo = -1, valorRuna = 0, pesoPlusRuna = 0;
		for (final int idIngrediente : _ingredientes.keySet()) {
			final Objeto ing = _artesano.getObjeto(idIngrediente);
			if (ing == null) {
				GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>HAY UN INGREDIENTE NULO</b>", Constantes.COLOR_ROJO);
				return;
			}
			final int idModelo = ing.getObjModeloID();
			final int statRuna = Constantes.getStatPorRunaPocima(ing);
			if (statRuna > 0) {
				statMagueo = statRuna;
				valorRuna = Constantes.getValorPorRunaPocima(ing);
				pesoPlusRuna = Constantes.getPotenciaPlusRuna(ing);
				objRunaOPocima = ing;
			} else if (idModelo == 7508) {
				// runa de firma
			} else {
				final int tipo = ing.getObjModelo().getTipo();
				if (tipo >= 1 && tipo <= 11 || tipo >= 16 && tipo <= 22 || tipo == 81 || tipo == 102 || tipo == 114 || ing
				.getObjModelo().getCostePA() > 0) {
					objAMaguear = ing;
				}
			}
		}
		if (_statOficio == null) {
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>EL STATOFICIO ES NULO</b>", Constantes.COLOR_ROJO);
			return;
		}
		if (objAMaguear == null) {
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>EL OBJETO A MAGUEAR ES NULO</b>", Constantes.COLOR_ROJO);
			return;
		}
		if (objRunaOPocima == null) {
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>LA RUNA O POCIMA ES NULO</b>", Constantes.COLOR_ROJO);
			return;
		}
		float pesoRuna = Constantes.getPesoStat(statMagueo);
		switch (statMagueo) {
			case 96 :
			case 97 :
			case 98 :
			case 99 :
				pesoRuna = 1;
				break;
		}
		if (pesoRuna <= 0) {
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>RUNA FORJAMAGIA INCORRECTA</b>", Constantes.COLOR_ROJO);
			return;
		}
		int ExitoCritico = 0, ExitoNormal = 0, FallaNormal = 0, FalloCritico = 0;
		int[] resultados = new int[4];
		resultados = getProbabilidadesMagueo(objAMaguear, objRunaOPocima, statMagueo, valorRuna, pesoPlusRuna);
		ExitoCritico = resultados[0];
		ExitoNormal = resultados[1];
		FallaNormal = resultados[2];
		FalloCritico = resultados[3];
		if (perso.getCuenta().getIdioma().equalsIgnoreCase("fr")) {
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "La probabilité de FM ton item <b>" + objAMaguear.getObjModelo()
			.getNombre() + "</b>:", Constantes.COLOR_NEGRO);
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>[Succès critique] = " + ExitoCritico + "%</b>",
			Constantes.COLOR_AZUL);
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>[Succès] = " + ExitoNormal + "%</b>",
			Constantes.COLOR_VERDE_OSCURO);
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>[Echec] = " + FallaNormal + "%</b>", Constantes.COLOR_NARANJA);
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>[Echec Critique] = " + FalloCritico + "%</b>",
			Constantes.COLOR_ROJO);
		} else {
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "La probabilidad de magueo del objeto <b>" + objAMaguear.getObjModelo()
			.getNombre() + "</b> con la <b>" + objRunaOPocima.getObjModelo().getNombre() + "</b>", Constantes.COLOR_NEGRO);
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>[Exito Crítico] = " + ExitoCritico + "%</b>",
			Constantes.COLOR_AZUL);
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>[Exito Normal] = " + ExitoNormal + "%</b>",
			Constantes.COLOR_VERDE_OSCURO);
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>[Fallo Normal] = " + FallaNormal + "%</b>",
			Constantes.COLOR_NARANJA);
			GestorSalida.ENVIAR_cs_CHAT_MENSAJE(perso, "<b>[Fallo Crítico] = " + FalloCritico + "%</b>",
			Constantes.COLOR_ROJO);
		}
	}
	
	private int[] getProbabilidadesMagueo(Objeto objMaguear, Objeto objRuna, int statMagueo, int cantAumRuna,
	int pesoPlus) {
		int[] probabilidades = new int[4];
		ObjetoModelo objModelo = objMaguear.getObjModelo();
		float razonMax = MainServidor.NIVEL_MAX_PERSONAJE / MainServidor.NIVEL_MAX_OFICIO;
		int nivelOficio = (int) (razonMax * _statOficio.getNivel() / objModelo.getNivel());
		if (nivelOficio > 25) {
			nivelOficio = 25;
		} else if (nivelOficio < 0) {
			nivelOficio = 0;
		}
		switch (statMagueo) {
			case 96 :
			case 97 :
			case 98 :
			case 99 :
				int suerte = objModelo.getProbabilidadGC() * objModelo.getCostePA() / (objModelo.getBonusGC() + objMaguear
				.getDañoPromedioNeutral());
				suerte += cantAumRuna + nivelOficio;
				if (suerte > 100) {
					suerte = 100;
				} else if (suerte < 5) {
					suerte = 5;
				}
				probabilidades[0] = suerte;
				probabilidades[1] = 0;
				probabilidades[2] = (100 - suerte);
				probabilidades[3] = 0;
				break;
			default :
				int pozoResidual = 0;
				if (MainServidor.PARAM_FM_CON_POZO_RESIDUAL) {
					pozoResidual += objMaguear.getStats().getStatParaMostrar(Constantes.STAT_POZO_RESIDUAL);
				}
				float pesoGlActual = 0;
				float pesoGlMin = 0;
				float pesoGlMax = 0;
				int cantStMin = 0;
				int cantStMax = 0;
				int cantStActual = 0;
				float pesoStActual = 0;
				float pesoStMax = 0;
				float pesoStMin = 0;
				pesoGlActual -= pozoResidual;
				if (MainServidor.MODO_DEBUG) {
					System.out.println("-------------- FORMULA FM --------------");
					System.out.println("statMagueo: " + statMagueo);
					System.out.println("pozoResidual: " + pozoResidual);
				}
				float pesoExo = 0;
				float pesoExcesoOver = 0;
				for (final Entry<Integer, Integer> entry : objMaguear.getStats().getEntrySet()) {
					int statID = entry.getKey();
					int cant = entry.getValue();
					final int statPositivo = Constantes.getStatPositivoDeNegativo(statID);
					float coef = 1;
					if (statPositivo != statID) {
						cant *= -1;
					}
					if (statPositivo == statMagueo) {
						cantStActual = cant;
						pesoStActual = Constantes.getPesoStat(statPositivo) * cant;
					}
					if (objModelo.tieneStatInicial(statPositivo)) {
						int max = objModelo.getDuoInicial(statPositivo)._segundo;
						if (max < cant) {
							// over
							if (statPositivo != statMagueo) {
								pesoExcesoOver += Constantes.getPesoStat(statPositivo) * (cant - max);
							}
							coef = 1.2f;
						}
					} else {
						// exo
						if (statPositivo != statMagueo) {
							pesoExo += Constantes.getPesoStat(statPositivo) * cant;
						}
						coef = 1.4f;
					}
					pesoGlActual += Constantes.getPesoStat(statPositivo) * cant * coef;
				}
				for (final Entry<Integer, Duo<Integer, Integer>> entry : objModelo.getStatsIniciales().entrySet()) {
					int statID = entry.getKey();
					int statMin = entry.getValue()._primero;
					int statMax = entry.getValue()._segundo;
					if (statID == statMagueo) {
						cantStMin = statMin;
						cantStMax = statMax;
						pesoStMin = Constantes.getPesoStat(statID) * statMin;
						pesoStMax = Constantes.getPesoStat(statID) * statMax;
					}
					pesoGlMin += Constantes.getPesoStat(statID) * statMin;
					pesoGlMax += Constantes.getPesoStat(statID) * statMax;
				}
				// if (Bustemu.RATE_FM != 1) {///
				// cantStMax *= Bustemu.RATE_FM;
				// pesoStMax *= Bustemu.RATE_FM;
				// pesoGlMax *= Bustemu.RATE_FM;
				// }
				byte tipoMagueo = MAGUEO_EXO;
				if (pesoStMax != 0) {
					tipoMagueo = MAGUEO_NORMAL;// 0
				}
				if (tipoMagueo == MAGUEO_NORMAL && cantStActual + cantAumRuna > cantStMax) {// es stat over
					tipoMagueo = MAGUEO_OVER;// 1
				}
				final int pesoRuna = (int) Math.ceil(Constantes.getPesoStat(statMagueo) * cantAumRuna);
				if (MainServidor.MODO_DEBUG) {
					System.out.println("tipoMagueo: " + tipoMagueo);
					System.out.println("cantAumRuna: " + cantAumRuna + " , pesoRuna: " + pesoRuna);
					System.out.println("cantStMax: " + cantStMax + " , cantStActual: " + cantStActual);
					System.out.println("pesoGlMax: " + pesoGlMax + " , pesoGlMin: " + pesoGlMin + " , pesoGlActual: "
					+ pesoGlActual);
					System.out.println("pesoStMax: " + pesoStMax + " , pesoStMin: " + pesoStMin + " , pesoStActual: "
					+ pesoStActual);
				}
				boolean puede = true;
				if (pesoGlMax == 0) {
					puede = false;
				}
				pesoGlMax += pesoPlus;// el aumento de la runa plus
				if (pesoStMax <= pesoStActual && Constantes.excedioLimitePeso(objRuna, cantStActual + cantAumRuna)) {
					if (MainServidor.MODO_DEBUG) {
						System.out.println("fallo en 6");
					}
					puede = false;
				}
				switch (tipoMagueo) {
					case MAGUEO_EXO :
						if (pesoGlActual < 0 && pesoGlMax < 0) {
							if (MainServidor.MODO_DEBUG) {
								System.out.println("fallo en 1");
							}
							puede = false;
						}
						if (((pesoStActual * 3) + pesoRuna + pesoGlActual) >= pesoGlMax * 2) {
							if (MainServidor.MODO_DEBUG) {
								System.out.println("fallo en 2");
							}
							puede = false;
						}
						if (Constantes.excedioLimiteMagueoDeRuna(objRuna.getObjModeloID(), cantStActual + cantAumRuna)) {
							if (MainServidor.MODO_DEBUG) {
								System.out.println("fallo en 3");
							}
							puede = false;
						}
						if (Constantes.excedioLimiteExomagia(statMagueo, cantStActual + cantAumRuna)) {
							puede = false;
						}
						break;
					case MAGUEO_OVER :
						if (pesoStActual + pesoRuna > pesoStMax * 2) {
							if (MainServidor.MODO_DEBUG) {
								System.out.println("fallo en 5");
							}
							puede = false;
						}
						if (Constantes.excedioLimiteOvermagia(statMagueo, cantStActual + cantAumRuna)) {
							puede = false;
						}
					case MAGUEO_NORMAL :
						if (cantStMin < 0 && (cantStActual >= 0 || cantStActual >= cantStMax)) {
							if (MainServidor.MODO_DEBUG) {
								System.out.println("fallo en 7");
							}
							puede = false;
						}
						if (cantStMax < 0 && (cantStMax < cantStActual + cantAumRuna)) {
							if (MainServidor.MODO_DEBUG) {
								System.out.println("fallo en 8");
							}
							puede = false;
						}
						break;
				}
				if (!puede) {
					if (MainServidor.MODO_DEBUG) {
						System.out.println("No se puede maguear, esta fuera de las estadisticas");
					}
					int FC = 40 + (pesoRuna / 2);
					probabilidades[0] = 0;
					probabilidades[1] = 0;
					probabilidades[2] = 100 - FC;
					probabilidades[3] = FC;
				} else {
					// pGlActual = Math.max(0, pGlActual);
					// pGlMax = Math.max(0, pGlMax);
					// pGlMin = Math.max(0, pGlMin);
					float porcGlobal = 0, porcStat = 0;
					if (pesoGlMin < 0 || pesoGlMax < 0) {
						if (pesoGlActual > 0) {
							pesoGlActual += Math.abs(pesoGlMin);
						}
						porcGlobal = Math.abs(pesoGlActual * 100 / pesoGlMin);
					} else {
						if (pesoGlMax == 0) {
							// no tiene stats
							porcGlobal = 100;
						} else if (pesoGlMax == pesoGlMin) {
							// son stats fijos
							porcGlobal = (pesoGlActual) * 100f / pesoGlMax;
						} else {
							porcGlobal = (pesoGlActual - pesoGlMin) * 100f / (pesoGlMax - pesoGlMin);
						}
					}
					if (pesoStMin < 0 || pesoStMax < 0) {
						if (pesoStActual > 0) {
							pesoStActual += Math.abs(pesoStMin);
						}
						porcStat = Math.abs(pesoStActual * 100 / pesoStMin);
					} else {
						if (pesoStMax == 0) {// exo
							porcStat = 100;
						} else if (pesoStMax == pesoStMin) {
							porcStat = pesoStActual * 100f / pesoStMax;
						} else {
							if (tipoMagueo == MAGUEO_NORMAL) {
								pesoStMin -= (pesoExo / 2 + pesoExcesoOver / 3);
								pesoStMin = Math.max(0, pesoStMin);
							}
							porcStat = (pesoStActual - pesoStMin) * 100f / (pesoStMax - pesoStMin);
						}
					}
					porcGlobal = Math.max(0, porcGlobal);
					porcStat = Math.max(0, porcStat);
					// el porcStat esta basado desde el Min Valor al Max Valor
					if (MainServidor.MODO_DEBUG) {
						System.out.println("Antes-> porcGlobal: " + porcGlobal + " , porcStat: " + porcStat);
					}
					int pSG = (int) ((pesoStMax + pesoPlus) * 100 / pesoGlMax);
					pSG = Math.max(0, pSG);
					int pG = (int) (100 - porcGlobal);
					int pS = (int) (100 - porcStat);
					int porcMaxExito = 0;
					int EC = 0, EN = 0, FN = 0, FC = 0;
					switch (tipoMagueo) {
						case MAGUEO_EXO :
							porcMaxExito = 11 - (pesoRuna / 10);
							break;
						case MAGUEO_OVER :
							porcMaxExito = 50 - (pesoRuna / 2);
							break;
						case MAGUEO_NORMAL :
							porcMaxExito = 100;
							break;
					}
					switch (tipoMagueo) {
						case MAGUEO_EXO :
							if (pesoRuna + (pesoExo * 3) + (pesoExcesoOver * 2) >= pesoGlMax) {
								pG = 0;
							}
							pS = 0;
							EN = pG / 2;// puede ser maximo 50
							break;
						case MAGUEO_OVER :
							if (pesoRuna + (pesoExo * 3) + (pesoExcesoOver * 2) >= pesoGlMax) {
								pG = 0;
							} else if (pesoExo > 0) {
								if (pesoExo > pesoRuna) {
									pG -= (pesoExo / pesoRuna);
								}
							}
							EN = pSG + pS;// pS aqui es negativo
							break;
						case MAGUEO_NORMAL :
							if (pesoExo > 0) {
								if (pesoExo > pesoRuna) {
									pG -= (pesoExo / pesoRuna);
								}
							}
							if (pesoExcesoOver > 0) {
								if (pesoExcesoOver > pesoRuna) {
									pG -= (pesoExcesoOver / pesoRuna);
								}
							}
							if (porcStat > MainServidor.MAX_PORCENTAJE_DE_STAT_PARA_FM) {
								pS = (int) Math.sqrt(pS);
							}
							EN = pS;
							break;
					}
					if (MainServidor.MODO_DEBUG) {
						System.out.println("Despues-> porcGlobal: " + porcGlobal + " , porcStat: " + porcStat);
						System.out.println("Despues-> pG: " + pG + ", pS: " + pS + ", pSG: " + pSG);
						System.out.println("Anterior-> EN: " + EN);
					}
					EN = Math.max(0, EN);
					EN = Math.min(porcMaxExito, EN);
					int factorRate = (100 - EN) * MainServidor.RATE_FM / 100;
					EN += factorRate;// aqui se adiciona el rate de la FM
					if (MainServidor.MODO_DEBUG) {
						System.out.println("Despues-> EN: " + EN);
					}
					switch (tipoMagueo) {
						case MAGUEO_EXO :
							EC = (int) Math.ceil(EN / 2f);
							EN = 0;
							break;
						case MAGUEO_OVER :
							break;
						case MAGUEO_NORMAL :
							int critico = 0;
							if (pG <= 0) {
								critico = (int) ((pS + pSG) / 2);
							} else {
								critico = (int) ((pS + pSG + pG) / 2);
							}
							critico = Math.max(1, critico);
							critico = Math.min(99, critico);
							if (critico < EN) {
								EC = critico;
								EN -= critico;
							} else {
								EC = EN;
								EN = 0;
							}
							break;
					}
					// if (pesoStMax == pesoGlMax && pesoStActual == 0) {
					// // cuando tiene un solo stat
					// EC = 99;
					// EN = 1;
					// } else
					FC = 100 - (EN + EC);// no pasa nada
					// FC = pesoRuna * FN / 100;
					// if (pesoGlActual == 0) {
					// FC = 0;
					// }
					// FN = FN - FC;
					probabilidades[0] = EC;
					probabilidades[1] = EN;
					probabilidades[2] = FN;
					probabilidades[3] = FC;
				}
		}
		return probabilidades;
	}
	
	private void addIngrediente(int idModelo, int cantidad) {
		if (_ingredientes.get(idModelo) == null) {
			_ingredientes.put(idModelo, cantidad);
		} else {
			final int nueva = _ingredientes.get(idModelo) + cantidad;
			_ingredientes.remove(idModelo);
			_ingredientes.put(idModelo, nueva);
		}
	}
	
	public void setArtesanoCliente(Personaje artesano, Personaje _perso) {
		_artesano = artesano;
		_cliente = _perso;
		if (_objArtesano == null) {
			_objArtesano = new ArrayList<>();
		}
		if (_objCliente == null) {
			_objCliente = new ArrayList<>();
		}
		if (_objetosPago == null) {
			_objetosPago = new ArrayList<>();
		}
		if (_objetosSiSeConsegui == null) {
			_objetosSiSeConsegui = new ArrayList<>();
		}
	}
	
	// machacar recursos
	public boolean iniciarTaller(final ArrayList<Duo<Integer, Integer>> objArtesano,
	final ArrayList<Duo<Integer, Integer>> objCliente) {
		if (!_esCraft) {
			return false;
		}
		_ingredientes.clear();
		for (final Duo<Integer, Integer> duo : objArtesano) {
			addIngrediente(duo._primero, duo._segundo);
		}
		for (final Duo<Integer, Integer> duo : objCliente) {
			addIngrediente(duo._primero, duo._segundo);
		}
		if (Constantes.esSkillMago(_skillID)) {
			return trabajoPagoFM();
		} else {
			return trabajoPagoCraft();
		}
	}
	
	private void iniciarThread() {
		if (!_finThread) {
			return;
		}
		Thread _thread = new Thread(this);
		_thread.setDaemon(true);
		_thread.setPriority(10);
		_thread.start();
	}
	
	public void run() {
		try {
			_finThread = false;
			if (_esCraft) {
				boolean esVIP = _artesano.esAbonado();
				boolean speedCraft = MainServidor.PARAM_VIP_CRAFT_SPEED && esVIP;
				_ultimosIngredientes.clear();
				_ultimosIngredientes.putAll(_ingredientes);
				try {
					for (int a = _cuantasRepeticiones; a >= 1; a--) {
						if (_interrumpir != MENSAJE_SIN_RESULTADO && _interrumpir != MENSAJE_OBJETO_FABRICADO) {
							break;
						}
						if (a == 1) {
							speedCraft = false;
						}
						if (_cuantasRepeticiones > 1 && a != _cuantasRepeticiones) {
							GestorSalida.ENVIAR_EA_TURNO_RECETA(_artesano, a);
						}
						iniciarCraft(speedCraft);
						if (a == 1 && _interrumpir == MENSAJE_SIN_RESULTADO) {
							_interrumpir = MENSAJE_OBJETO_FABRICADO;
						}
						if (_interrumpir == MENSAJE_SIN_RESULTADO) {
							Thread.sleep(speedCraft ? 25 : 1000);
						}
					}
				} catch (final Exception e) {}
				switch (_interrumpir) {
					case MENSAJE_RECETA_NO_FUNCIONA :
					case MENSAJE_FALTA_RECURSOS :
						if (!_esForjaMagia) {
							GestorSalida.ENVIAR_Ec_RESULTADO_RECETA(_artesano, "EI");
						}
						GestorSalida.ENVIAR_IO_ICONO_OBJ_INTERACTIVO(_artesano.getMapa(), _artesano.getID(), "-");
						break;
				}
				if (_cuantasRepeticiones > 1 || _interrumpir > 1) {
					GestorSalida.ENVIAR_Ea_MENSAJE_RECETAS(_artesano, _interrumpir);
				}
			} else {
				// recolecta
				try {
					Thread.sleep(_tiempoRecoleccion);
				} catch (Exception e) {}
				_statOficio.finalizarTrabajo(_artesano);
			}
		} catch (Exception e) {
			MainServidor.redactarLogServidorln("EXCEPTION DE RUN TRABAJO EN OFICIO esCraft: " + _esCraft);
			e.printStackTrace();
		} finally {
			if (_ingredientes != null) {
				if (!_esForjaMagia || esTaller()) {
					_ingredientes.clear();
				}
			}
			_cuantasRepeticiones = 0;
			_interrumpir = MENSAJE_SIN_RESULTADO;
			_finThread = true;
		}
	}
	
	private Objeto iniciarCraft(boolean esSpeedCraft) {
		if (Constantes.esSkillMago(_skillID)) {
			return trabajoMaguear(esSpeedCraft);
		} else {
			trabajoCraftear(esSpeedCraft);
			return null;
		}
	}
	
	private void trabajoCraftear(boolean esSpeedCraft) {
		try {
			final Map<Integer, Integer> ingredientesModelo = new TreeMap<Integer, Integer>();
			final Map<Integer, Integer> runasModelo = new TreeMap<Integer, Integer>();
			for (final Entry<Integer, Integer> ingrediente : _ingredientes.entrySet()) {
				final int objetoID = ingrediente.getKey();
				final int cantObjeto = ingrediente.getValue();
				final Objeto objeto = _artesano.getObjeto(objetoID);
				if (objeto == null || objeto.getCantidad() < cantObjeto) {
					if (objeto == null) {
						continue;
					} else {
						GestorSalida.ENVIAR_Im_INFORMACION(_artesano, "1CRAFT_NOT_ENOUGHT;" + objeto.getObjModeloID() + " ("
						+ objeto.getCantidad() + ")");
					}
					_interrumpir = MENSAJE_FALTA_RECURSOS;
					return;
				}
				if (_varios) {
					GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_artesano, 'O', "+", objetoID + "|" + cantObjeto);
				}
				if (_skillID == Constantes.SKILL_ROMPER_OBJETO) {
					objeto.runasRomperObjeto(runasModelo, cantObjeto);
				} else {
					ingredientesModelo.put(objeto.getObjModeloID(), cantObjeto);
				}
				final int nuevaCant = objeto.getCantidad() - cantObjeto;
				if (nuevaCant == 0) {
					_artesano.borrarOEliminarConOR(objetoID, true);
				} else {
					objeto.setCantidad(nuevaCant);
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_artesano, objeto);
				}
			}
			_varios = true;
			boolean firmado = false;
			if (ingredientesModelo.containsKey(7508)) {
				ingredientesModelo.remove(7508);
				firmado = true;
			}
			int resultadoReceta = -1;
			if (_skillID == Constantes.SKILL_ROMPER_OBJETO) {
				resultadoReceta = runasModelo.isEmpty() ? -1 : 8378;
			} else {
				resultadoReceta = Mundo.getIDRecetaPorIngredientes(_statOficio.getOficio().listaRecetaPorTrabajo(_skillID),
				ingredientesModelo);
			}
			if (resultadoReceta == -1 || Mundo.getObjetoModelo(resultadoReceta) == null
			|| (_skillID != Constantes.SKILL_ROMPER_OBJETO && !_statOficio.getOficio().puedeReceta(_skillID,
			resultadoReceta))) {
				_interrumpir = MENSAJE_RECETA_NO_FUNCIONA;
				return;
			}
			int suerte = 100;
			switch (_skillID) {
				case Constantes.SKILL_PELAR_PATATAS :
				case Constantes.SKILL_UTILIZAR_BANCO :
				case Constantes.SKILL_MACHACAR_RECURSOS :
					suerte = 100;
					break;
				case Constantes.SKILL_ROMPER_OBJETO :
					suerte = 99;
					break;
				default :
					suerte = Constantes.getSuerteNivelYSlots(_statOficio.getNivel(), ingredientesModelo.size());
					break;
			}
			final boolean exito = MainServidor.PARAM_CRAFT_SIEMPRE_EXITOSA || suerte == 100 || (suerte >= Formulas
			.getRandomInt(1, 100));
			if (exito) {
				Objeto objCreado = Mundo.getObjetoModelo(resultadoReceta).crearObjeto(1, Constantes.OBJETO_POS_NO_EQUIPADO,
				MainServidor.PARAM_CRAFT_PERFECTO_STATS ? CAPACIDAD_STATS.MAXIMO : CAPACIDAD_STATS.RANDOM);
				if (_skillID == Constantes.SKILL_ROMPER_OBJETO) {
					StringBuilder st = new StringBuilder();
					for (Entry<Integer, Integer> entry : runasModelo.entrySet()) {
						if (entry.getValue() > 0) {
							if (st.length() > 0) {
								st.append(",");
							}
							st.append("1f4#" + Integer.toHexString(entry.getKey()) + "#" + Integer.toHexString(entry.getValue()));
						}
					}
					objCreado.convertirStringAStats(st.toString());
				} else if (firmado) {
					objCreado.addStatTexto(Constantes.STAT_FACBRICADO_POR, "0#0#0#" + _artesano.getNombre());
				}
				Objeto igual = _artesano.getObjIdentInventario(objCreado, null);
				if (igual == null) {
					_artesano.addObjetoConOAKO(objCreado, true);
				} else {
					igual.setCantidad(igual.getCantidad() + 1);
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_artesano, igual);
					objCreado = igual;
				}
				if (!esSpeedCraft) {
					GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_artesano, 'O', "+", objCreado.stringObjetoConPalo(objCreado
					.getCantidad()));
					GestorSalida.ENVIAR_Ec_RESULTADO_RECETA(_artesano, "K;" + resultadoReceta);
				}
			} else {
				if (!esSpeedCraft) {
					GestorSalida.ENVIAR_Ec_RESULTADO_RECETA(_artesano, "EF");
					GestorSalida.ENVIAR_Im_INFORMACION(_artesano, "0118");
				}
			}
			if (!esSpeedCraft) {
				GestorSalida.ENVIAR_IO_ICONO_OBJ_INTERACTIVO(_artesano.getMapa(), _artesano.getID(), (exito ? "+" : "-")
				+ resultadoReceta);
				GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_artesano);
			}
			switch (_skillID) {
				case Constantes.SKILL_PELAR_PATATAS :
				case Constantes.SKILL_UTILIZAR_BANCO :
				case Constantes.SKILL_ROMPER_OBJETO :
				case Constantes.SKILL_MACHACAR_RECURSOS :
					break;
				default :
					int exp = Constantes.calculXpGanadaEnOficio(_statOficio.getNivel(), ingredientesModelo.size());
					_statOficio.addExperiencia(_artesano, preExp(exp), Constantes.OFICIO_EXP_TIPO_CRAFT);
					break;
			}
		} catch (final Exception e) {
			_interrumpir = MENSAJE_INTERRUMPIDA;
		}
	}
	
	private Objeto trabajoMaguear(boolean esSpeedCraft) {
		try {
			Objeto objAMaguear = null, objRunaFirma = null, objRunaOPocima = null;
			int statMagueo = -1, valorRuna = 0, pesoPlusRuna = 0;
			boolean firmado = false;
			if (_statOficio == null) {
				_interrumpir = MENSAJE_RECETA_NO_FUNCIONA;
				return null;
			}
			for (final int idIngrediente : _ingredientes.keySet()) {
				final Objeto ing = _artesano.getObjeto(idIngrediente);
				if (ing == null) {
					// GestorSalida.ENVIAR_Im_INFORMACION(_artesano, "1OBJECT_DONT_EXIST;" + idIngrediente);
					// _interrumpir = MENSAJE_FALTA_RECURSOS;
					// return null;
					continue;
				}
				final int statRuna = Constantes.getStatPorRunaPocima(ing);
				final int idModelo = ing.getObjModeloID();
				if (idModelo == 7508) {
					firmado = true;
					objRunaFirma = ing;
				} else if (statRuna > 0) {
					statMagueo = statRuna;
					valorRuna = Constantes.getValorPorRunaPocima(ing);
					pesoPlusRuna = Constantes.getPotenciaPlusRuna(ing);
					objRunaOPocima = ing;
				} else {
					final int tipo = ing.getObjModelo().getTipo();
					switch (tipo) {
						case Constantes.OBJETO_TIPO_AMULETO :
						case Constantes.OBJETO_TIPO_ARCO :
						case Constantes.OBJETO_TIPO_VARITA :
						case Constantes.OBJETO_TIPO_BASTON :
						case Constantes.OBJETO_TIPO_DAGAS :
						case Constantes.OBJETO_TIPO_ESPADA :
						case Constantes.OBJETO_TIPO_MARTILLO :
						case Constantes.OBJETO_TIPO_PALA :
						case Constantes.OBJETO_TIPO_ANILLO :
						case Constantes.OBJETO_TIPO_CINTURON :
						case Constantes.OBJETO_TIPO_BOTAS :
						case Constantes.OBJETO_TIPO_SOMBRERO :
						case Constantes.OBJETO_TIPO_CAPA :
						case Constantes.OBJETO_TIPO_HACHA :
						case Constantes.OBJETO_TIPO_HERRAMIENTA :
						case Constantes.OBJETO_TIPO_PICO :
						case Constantes.OBJETO_TIPO_GUADAÑA :
						case Constantes.OBJETO_TIPO_MOCHILA :
						case Constantes.OBJETO_TIPO_BALLESTA :
						case Constantes.OBJETO_TIPO_ARMA_MAGICA :
							objAMaguear = ing;
							break;
					}
				}
			}
			if (objAMaguear == null || objRunaOPocima == null) {
				_interrumpir = MENSAJE_FALTA_RECURSOS;
				return null;
			}
			float pesoRuna = Constantes.getPesoStat(statMagueo);
			switch (statMagueo) {
				case 96 :
				case 97 :
				case 98 :
				case 99 :
					pesoRuna = 1;
					break;
			}
			if (pesoRuna <= 0) {
				GestorSalida.ENVIAR_cs_CHAT_MENSAJE(_artesano, "<b>RUNA FORJAMAGIA INCORRECTA</b>", Constantes.COLOR_ROJO);
				return null;
			}
			if (objRunaOPocima != null) {
				final int nuevaCant = objRunaOPocima.getCantidad() - 1;
				if (nuevaCant <= 0) {
					_artesano.borrarOEliminarConOR(objRunaOPocima.getID(), true);
				} else {
					objRunaOPocima.setCantidad(nuevaCant);
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_artesano, objRunaOPocima);
				}
				Objeto o = objRunaOPocima;
				int n = _ingredientes.get(o.getID()) - 1;
				if (n <= 0) {
					_ultimosIngredientes.remove(o.getID());
				} else {
					_ultimosIngredientes.put(o.getID(), n);
				}
			}
			if (objRunaFirma != null) {
				final int nuevaCant = objRunaFirma.getCantidad() - 1;
				if (nuevaCant <= 0) {
					_artesano.borrarOEliminarConOR(objRunaFirma.getID(), true);
				} else {
					objRunaFirma.setCantidad(nuevaCant);
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_artesano, objRunaFirma);
				}
				Objeto o = objRunaFirma;
				int n = _ingredientes.get(o.getID()) - 1;
				if (n <= 0) {
					_ultimosIngredientes.remove(o.getID());
				} else {
					_ultimosIngredientes.put(o.getID(), n);
				}
			}
			if (objAMaguear != null) {
				int nuevaCantidad = objAMaguear.getCantidad() - 1;
				if (nuevaCantidad >= 1) {
					Objeto nuevoObj = objAMaguear.clonarObjeto(nuevaCantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
					_artesano.addObjetoConOAKO(nuevoObj, true);
					objAMaguear.setCantidad(1);
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_artesano, objAMaguear);
					_ultimosIngredientes.put(objAMaguear.getID(), objAMaguear.getCantidad());
				}
			}
			_ingredientes.clear();
			_ingredientes.putAll(_ultimosIngredientes);
			int ExitoCritico = 0, ExitoNormal = 0, FallaNormal = 0, FalloCritico = 0;
			final int objModeloID = objAMaguear.getObjModeloID();
			final int jet = Formulas.getRandomInt(1, 100);
			int[] resultados = new int[4];
			resultados = getProbabilidadesMagueo(objAMaguear, objRunaOPocima, statMagueo, valorRuna, pesoPlusRuna);
			ExitoCritico = resultados[0];
			ExitoNormal = resultados[1];
			FallaNormal = resultados[2];
			FalloCritico = resultados[3];
			if (MainServidor.MODO_DEBUG) {
				System.out.println("ExitoCritico: " + ExitoCritico);
				System.out.println("ExitoNormal: " + ExitoNormal);
				System.out.println("FallaNormal: " + FallaNormal);
				System.out.println("FalloCritico: " + FalloCritico);
				System.out.println("Jet: " + jet);
			}
			int r = 0, t = 0;
			for (int i : resultados) {
				r++;
				t += i;
				if (jet <= t) {
					break;
				}
			}
			if (MainServidor.MODO_DEBUG) {
				String res = "NADA";
				switch (r) {
					case RESULTADO_EXITO_CRITICO :
						res = "RESULTADO_EXITO_CRITICO";
						break;
					case RESULTADO_EXITO_NORMAL :
						res = "RESULTADO_EXITO_NORMAL";
						break;
					case RESULTADO_FALLO_NORMAL :
						res = "RESULTADO_FALLO_NORMAL";
						break;
					case RESULTADO_FALLO_CRITICO :
						res = "RESULTADO_FALLO_CRITICO";
						break;
				}
				System.out.println("Resultado: " + res);
			}
			boolean exito = false;
			switch (r) {
				case RESULTADO_EXITO_NORMAL :
					// va antes porq agrega el mensaje de la magia no ha funcionado bien
					objAMaguear.forjaMagiaPerder(statMagueo, valorRuna, false);
					if (!esSpeedCraft) {
						GestorSalida.ENVIAR_Im_INFORMACION(_artesano, "0194");
					}
				case RESULTADO_EXITO_CRITICO :
					if (firmado) {
						objAMaguear.addStatTexto(Constantes.STAT_MODIFICADO_POR, "0#0#0#" + _artesano.getNombre());
					}
					objAMaguear.forjaMagiaGanar(statMagueo, valorRuna);
					exito = true;
					break;
				case RESULTADO_FALLO_NORMAL :
					if (!esSpeedCraft) {
						GestorSalida.ENVIAR_Im_INFORMACION(_artesano, "0183");
					}
					break;
				case RESULTADO_FALLO_CRITICO :
					objAMaguear.forjaMagiaPerder(statMagueo, valorRuna, true);
					if (!esSpeedCraft) {
						GestorSalida.ENVIAR_Im_INFORMACION(_artesano, "0117");
					}
					break;
			}
			if (!esSpeedCraft) {
				GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(_artesano, objAMaguear);
				if (exito) {
					GestorSalida.ENVIAR_Ec_RESULTADO_RECETA(_artesano, "K;" + objAMaguear.getObjModeloID());
				} else {
					GestorSalida.ENVIAR_Ec_RESULTADO_RECETA(_artesano, "EF");
				}
				GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(_artesano, 'O', "+", objAMaguear.stringObjetoConPalo(objAMaguear
				.getCantidad()));
				for (Entry<Integer, Integer> e : _ultimosIngredientes.entrySet()) {
					GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_artesano, 'O', "+", e.getKey() + "|" + e.getValue());
				}
				GestorSalida.ENVIAR_IO_ICONO_OBJ_INTERACTIVO(_artesano.getMapa(), _artesano.getID(), (exito ? "+" : "-")
				+ objModeloID);
			}
			int exp = Constantes.getExpForjamaguear(Constantes.getPesoStat(statMagueo) * valorRuna, objAMaguear.getObjModelo()
			.getNivel());
			_statOficio.addExperiencia(_artesano, preExp(exp), Constantes.OFICIO_EXP_TIPO_CRAFT);
			return objAMaguear;
		} catch (Exception e) {
			_interrumpir = MENSAJE_INTERRUMPIDA;
		}
		return null;
	}
	
	private boolean trabajoPagoCraft() {
		try {
			Objeto nuevoObj = null;
			int r = RESULTADO_FALLO_NORMAL;
			final Map<Integer, Integer> ingredientesPorModelo = new TreeMap<Integer, Integer>();
			for (final Entry<Integer, Integer> ingrediente : _ingredientes.entrySet()) {
				final int objetoID = ingrediente.getKey();
				final int cantObjeto = ingrediente.getValue();
				final Objeto objeto = Mundo.getObjeto(objetoID);
				Personaje dueño = null;
				if (_artesano.tieneObjetoID(objetoID)) {
					dueño = _artesano;
				} else if (_cliente.tieneObjetoID(objetoID)) {
					dueño = _cliente;
				}
				if (dueño == null || objeto == null || objeto.getCantidad() < cantObjeto) {
					_artesano.setForjaEc("EI");
					_cliente.setForjaEc("EI");
					if (objeto == null) {
						GestorSalida.ENVIAR_Im_INFORMACION(_artesano, "1OBJECT_DONT_EXIST;" + objetoID);
					} else {
						GestorSalida.ENVIAR_Im_INFORMACION(_artesano, "1CRAFT_NOT_ENOUGHT;" + objeto.getObjModeloID() + " ("
						+ objeto.getCantidad() + ")");
					}
					return false;
				}
				final int nuevaCant = objeto.getCantidad() - cantObjeto;
				if (nuevaCant <= 0) {
					// agregar si lo tiene el artesano o el cliente
					dueño.borrarOEliminarConOR(objetoID, true);
				} else {
					objeto.setCantidad(nuevaCant);
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(dueño, objeto);
				}
				final int idModelo = objeto.getObjModeloID();
				if (ingredientesPorModelo.get(idModelo) == null) {
					ingredientesPorModelo.put(idModelo, cantObjeto);
				} else {
					final int nueva = ingredientesPorModelo.get(idModelo) + cantObjeto;
					ingredientesPorModelo.remove(idModelo);
					ingredientesPorModelo.put(idModelo, nueva);
				}
			}
			boolean firmado = false;
			if (ingredientesPorModelo.containsKey(7508)) {
				ingredientesPorModelo.remove(7508);
				firmado = true;
			}
			final int recetaID = Mundo.getIDRecetaPorIngredientes(_statOficio.getOficio().listaRecetaPorTrabajo(_skillID),
			ingredientesPorModelo);
			if (recetaID == -1 || !_statOficio.getOficio().puedeReceta(_skillID, recetaID)) {
				r = RESULTADO_FALLO_CRITICO;
			}
			boolean exito = false;
			if (r != RESULTADO_FALLO_CRITICO) {
				final int suerte = Constantes.getSuerteNivelYSlots(_statOficio.getNivel(), ingredientesPorModelo.size());
				exito = MainServidor.PARAM_CRAFT_SIEMPRE_EXITOSA || (suerte >= Formulas.getRandomInt(1, 100));
				if (exito) {
					r = RESULTADO_EXITO_NORMAL;
					nuevoObj = Mundo.getObjetoModelo(recetaID).crearObjeto(1, Constantes.OBJETO_POS_NO_EQUIPADO,
					MainServidor.PARAM_CRAFT_PERFECTO_STATS ? CAPACIDAD_STATS.MAXIMO : CAPACIDAD_STATS.RANDOM);
					if (firmado) {
						nuevoObj.addStatTexto(Constantes.STAT_FACBRICADO_POR, "0#0#0#" + _artesano.getNombre());
					}
					Objeto igual = _cliente.getObjIdentInventario(nuevoObj, null);
					if (igual == null) {
						_cliente.addObjetoConOAKO(nuevoObj, true);
					} else {
						igual.setCantidad(igual.getCantidad() + 1);
						GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_cliente, igual);
						nuevoObj = igual;
					}
				}
			}
			GestorSalida.ENVIAR_IO_ICONO_OBJ_INTERACTIVO(_artesano.getMapa(), _artesano.getID(), (nuevoObj == null
			? "-"
			: "+") + recetaID);
			GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_artesano);
			GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_cliente);
			switch (r) {
				case RESULTADO_EXITO_NORMAL :
				case RESULTADO_EXITO_CRITICO :
					final String statsNuevoObj = nuevoObj.convertirStatsAString(false);
					final String todaInfo = nuevoObj.stringObjetoConPalo(nuevoObj.getCantidad());
					GestorSalida.ENVIAR_ErK_RESULTADO_TRABAJO(_artesano, "O", "+", todaInfo);
					GestorSalida.ENVIAR_ErK_RESULTADO_TRABAJO(_cliente, "O", "+", todaInfo);
					_artesano.setForjaEc("K;" + recetaID + ";T" + _cliente.getNombre() + ";" + statsNuevoObj);
					_cliente.setForjaEc("K;" + recetaID + ";B" + _artesano.getNombre() + ";" + statsNuevoObj);
					break;
				case RESULTADO_FALLO_NORMAL :
				case RESULTADO_FALLO_CRITICO :
					GestorSalida.ENVIAR_Im_INFORMACION(_artesano, "0118");
					_artesano.setForjaEc("EF");
					_cliente.setForjaEc("EF");
					break;
			}
			int exp = Constantes.calculXpGanadaEnOficio(_statOficio.getNivel(), ingredientesPorModelo.size());
			_statOficio.addExperiencia(_artesano, preExp(exp), Constantes.OFICIO_EXP_TIPO_CRAFT);
			return exito;
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean trabajoPagoFM() {
		try {
			Objeto objAMaguear = null, objRunaFirma = null, objRunaOPocima = null;
			int statMagueo = -1, valorRuna = 0, pesoPlusRuna = 0;
			boolean firmado = false;
			for (final int idIngrediente : _ingredientes.keySet()) {
				final Objeto ing = Mundo.getObjeto(idIngrediente);
				if (ing == null) {
					_artesano.setForjaEc("EI");
					_cliente.setForjaEc("EI");
					return false;
				}
				final int statRuna = Constantes.getStatPorRunaPocima(ing);
				final int idModelo = ing.getObjModeloID();
				if (idModelo == 7508) {
					firmado = true;
					objRunaFirma = ing;
				} else if (statRuna > 0) {
					statMagueo = statRuna;
					valorRuna = Constantes.getValorPorRunaPocima(ing);
					pesoPlusRuna = Constantes.getPotenciaPlusRuna(ing);
					objRunaOPocima = ing;
				} else {
					final int tipo = ing.getObjModelo().getTipo();
					switch (tipo) {
						case Constantes.OBJETO_TIPO_AMULETO :
						case Constantes.OBJETO_TIPO_ARCO :
						case Constantes.OBJETO_TIPO_VARITA :
						case Constantes.OBJETO_TIPO_BASTON :
						case Constantes.OBJETO_TIPO_DAGAS :
						case Constantes.OBJETO_TIPO_ESPADA :
						case Constantes.OBJETO_TIPO_MARTILLO :
						case Constantes.OBJETO_TIPO_PALA :
						case Constantes.OBJETO_TIPO_ANILLO :
						case Constantes.OBJETO_TIPO_CINTURON :
						case Constantes.OBJETO_TIPO_BOTAS :
						case Constantes.OBJETO_TIPO_SOMBRERO :
						case Constantes.OBJETO_TIPO_CAPA :
						case Constantes.OBJETO_TIPO_HACHA :
						case Constantes.OBJETO_TIPO_HERRAMIENTA :
						case Constantes.OBJETO_TIPO_PICO :
						case Constantes.OBJETO_TIPO_GUADAÑA :
						case Constantes.OBJETO_TIPO_MOCHILA :
						case Constantes.OBJETO_TIPO_BALLESTA :
						case Constantes.OBJETO_TIPO_ARMA_MAGICA :
							objAMaguear = ing;
							int nuevaCantidad = objAMaguear.getCantidad() - 1;
							if (nuevaCantidad >= 1) {
								final Personaje modificado = _artesano.tieneObjetoID(idIngrediente) ? _artesano : _cliente;
								Objeto nuevoObj = objAMaguear.clonarObjeto(nuevaCantidad, Constantes.OBJETO_POS_NO_EQUIPADO);
								modificado.addObjetoConOAKO(nuevoObj, true);
								objAMaguear.setCantidad(1);
								GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(modificado, objAMaguear);
							}
							break;
					}
				}
			}
			if (_statOficio == null || objAMaguear == null || objRunaOPocima == null) {
				_artesano.setForjaEc("EI");
				_cliente.setForjaEc("EI");
				return false;
			}
			float pesoRuna = Constantes.getPesoStat(statMagueo);
			if (pesoRuna <= 0) {
				_artesano.setForjaEc("EI");
				_cliente.setForjaEc("EI");
				return false;
			}
			if (objRunaFirma != null) {
				final Personaje modificado = _artesano.tieneObjetoID(objRunaFirma.getID()) ? _artesano : _cliente;
				final int nuevaCant = objRunaFirma.getCantidad() - 1;
				if (nuevaCant <= 0) {
					modificado.borrarOEliminarConOR(objRunaFirma.getID(), true);
				} else {
					objRunaFirma.setCantidad(nuevaCant);
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(modificado, objRunaFirma);
				}
			}
			if (objRunaOPocima != null) {
				final Personaje modificado = _artesano.tieneObjetoID(objRunaOPocima.getID()) ? _artesano : _cliente;
				final int nuevaCant = objRunaOPocima.getCantidad() - 1;
				if (nuevaCant <= 0) {
					modificado.borrarOEliminarConOR(objRunaOPocima.getID(), true);
				} else {
					objRunaOPocima.setCantidad(nuevaCant);
					GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(modificado, objRunaOPocima);
				}
			}
			final int objModeloID = objAMaguear.getObjModeloID();
			final int jet = Formulas.getRandomInt(1, 100);
			int[] resultados = getProbabilidadesMagueo(objAMaguear, objRunaOPocima, statMagueo, valorRuna, pesoPlusRuna);
			int r = 0, t = 0;
			for (int i : resultados) {
				r++;
				t += i;
				if (jet <= t) {
					break;
				}
			}
			switch (r) {
				case RESULTADO_EXITO_NORMAL :
					objAMaguear.forjaMagiaPerder(statMagueo, valorRuna, false);
					GestorSalida.ENVIAR_Im_INFORMACION(_cliente, "0194");
					GestorSalida.ENVIAR_Im_INFORMACION(_artesano, "0194");
				case RESULTADO_EXITO_CRITICO :
					if (firmado) {
						objAMaguear.addStatTexto(Constantes.STAT_MODIFICADO_POR, "0#0#0#" + _artesano.getNombre());
					}
					objAMaguear.forjaMagiaGanar(statMagueo, valorRuna);
					break;
				case RESULTADO_FALLO_NORMAL :
					GestorSalida.ENVIAR_Im_INFORMACION(_cliente, "0183");
					GestorSalida.ENVIAR_Im_INFORMACION(_artesano, "0183");
					break;
				case RESULTADO_FALLO_CRITICO :
					objAMaguear.forjaMagiaPerder(statMagueo, valorRuna, true);
					GestorSalida.ENVIAR_Im_INFORMACION(_cliente, "0117");
					GestorSalida.ENVIAR_Im_INFORMACION(_artesano, "0117");
					break;
			}
			final Personaje modificado = _artesano.tieneObjetoID(objAMaguear.getID()) ? _artesano : _cliente;
			GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_cliente);
			GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(_artesano);
			GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(modificado, objAMaguear);
			final String todaInfo = objAMaguear.stringObjetoConPalo(objAMaguear.getCantidad());
			GestorSalida.ENVIAR_ErK_RESULTADO_TRABAJO(_artesano, "O", "+", todaInfo);
			GestorSalida.ENVIAR_ErK_RESULTADO_TRABAJO(_cliente, "O", "+", todaInfo);
			switch (r) {
				case RESULTADO_EXITO_NORMAL :
				case RESULTADO_EXITO_CRITICO :
					final String statsNuevoObj = objAMaguear.convertirStatsAString(false);
					_artesano.setForjaEc("K;" + objModeloID + ";T" + _cliente.getNombre() + ";" + statsNuevoObj);
					_cliente.setForjaEc("K;" + objModeloID + ";B" + _artesano.getNombre() + ";" + statsNuevoObj);
					break;
				case RESULTADO_FALLO_NORMAL :
				case RESULTADO_FALLO_CRITICO :
					_artesano.setForjaEc("EF");
					_cliente.setForjaEc("EF");
					break;
			}
			int exp = Constantes.getExpForjamaguear(Constantes.getPesoStat(statMagueo) * valorRuna, objAMaguear.getObjModelo()
			.getNivel());
			_statOficio.addExperiencia(_artesano, preExp(exp), Constantes.OFICIO_EXP_TIPO_CRAFT);
			return r == RESULTADO_EXITO_CRITICO || r == RESULTADO_EXITO_NORMAL;
		} catch (Exception e) {
			return false;
		}
	}
	
	public void limpiarReceta() {
		if (_ingredientes != null) {
			_ingredientes.clear();
		}
		if (_ultimosIngredientes != null) {
			_ultimosIngredientes.clear();
		}
		_kamasPago = _kamasSiSeConsigue = 0;
		_ok1 = _ok2 = false;
		_cliente = null;
		_artesano = null;
		if (_objArtesano != null) {
			_objArtesano.clear();
		}
		if (_objCliente != null) {
			_objCliente.clear();
		}
		if (_objetosPago != null) {
			_objetosPago.clear();
		}
		if (_objetosSiSeConsegui != null) {
			_objetosSiSeConsegui.clear();
		}
	}
	
	public void craftearXVeces(int cantidad) {
		if (_esCraft) {
			if (_cuantasRepeticiones > 0) {
				return;
			}
			try {
				_cuantasRepeticiones = Math.abs(cantidad);
				_interrumpir = MENSAJE_SIN_RESULTADO;
				_varios = false;
				iniciarThread();
			} catch (final Exception e) {}
		}
	}
	
	public void interrumpirReceta() {
		if (_esCraft) {
			_interrumpir = MENSAJE_INTERRUMPIDA;
		}
	}
	
	public void ponerIngredUltRecet() {
		if (_ultimosIngredientes == null || _ultimosIngredientes.isEmpty() || _ingredientes == null || !_ingredientes
		.isEmpty()) {
			return;
		}
		_ingredientes.putAll(_ultimosIngredientes);
		for (final Entry<Integer, Integer> e : _ingredientes.entrySet()) {
			final Objeto objeto = _artesano.getObjeto(e.getKey());
			if (objeto == null) {
				continue;
			}
			if (objeto.getCantidad() < e.getValue()) {
				continue;
			}
			GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(_artesano, 'O', "+", objeto.getID() + "|" + e.getValue());
		}
	}
	
	@Override
	public void cerrar(Personaje perso, String exito) {
		if (_artesano != null) {
			_artesano.cerrarVentanaExchange(exito);
			_artesano.setInvitandoA(null, "");
			_artesano.setInvitador(null, "");
		}
		if (_cliente != null) {
			_cliente.cerrarVentanaExchange(exito);
			_cliente.setInvitandoA(null, "");
			_cliente.setInvitador(null, "");
		}
		interrumpirReceta();
		limpiarReceta();
	}
	
	public synchronized void botonOK(final Personaje perso) {
		if (_artesano.getID() == perso.getID()) {
			_ok1 = !_ok1;
			GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_artesano, _ok1, perso.getID());
			GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_cliente, _ok1, perso.getID());
		} else if (_cliente.getID() == perso.getID()) {
			_ok2 = !_ok2;
			GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_artesano, _ok2, perso.getID());
			GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_cliente, _ok2, perso.getID());
		} else {
			return;
		}
		if (_ok1 && _ok2) {
			aplicar();
		}
	}
	
	public void desCheck() {
		_ok1 = false;
		_ok2 = false;
		GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_artesano, _ok1, _artesano.getID());
		GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_cliente, _ok1, _artesano.getID());
		GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_artesano, _ok2, _cliente.getID());
		GestorSalida.ENVIAR_EK_CHECK_OK_INTERCAMBIO(_cliente, _ok2, _cliente.getID());
	}
	
	public long getKamasSiSeConsigue() {
		return _kamasSiSeConsigue;
	}
	
	public long getKamasPaga() {
		return _kamasPago;
	}
	
	public void setKamas(final int tipoPago, long kamas, final long kamasT) {
		desCheck();
		if (kamas < 0) {
			return;
		}
		if (tipoPago == 1) {
			if (_kamasSiSeConsigue + kamas > kamasT) {
				kamas = kamasT - _kamasSiSeConsigue;
			}
			_kamasPago = kamas;
		} else {
			if (_kamasPago + kamas > kamasT) {
				kamas = kamasT - _kamasPago;
			}
			_kamasSiSeConsigue = kamas;
		}
		GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_artesano, tipoPago, "G", "+", kamas + "");
		GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_cliente, tipoPago, "G", "+", kamas + "");
	}
	
	public synchronized void aplicar() {
		try {
			final boolean resultado = iniciarTaller(_objArtesano, _objCliente);
			final StatOficio oficio = _artesano.getStatOficioPorTrabajo(getTrabajoID());
			if (_cliente.getKamas() < _kamasSiSeConsigue + _kamasPago) {
				_kamasPago = _cliente.getKamas();
				_kamasSiSeConsigue = 0;
			}
			if (oficio != null) {
				if (resultado) {
					_cliente.addKamas(-_kamasSiSeConsigue, true, true);
					_artesano.addKamas(_kamasSiSeConsigue, true, true);
					for (final Duo<Integer, Integer> duo : _objetosSiSeConsegui) {
						try {
							final int cant = duo._segundo;
							if (cant == 0) {
								continue;
							}
							final Objeto obj = _cliente.getObjeto(duo._primero);
							if (obj.getCantidad() - cant < 1) {
								_cliente.borrarOEliminarConOR(duo._primero, false);
								_artesano.addObjIdentAInventario(obj, true);
							} else {
								Objeto nuevoOjb = obj.clonarObjeto(cant, Constantes.OBJETO_POS_NO_EQUIPADO);
								_artesano.addObjIdentAInventario(nuevoOjb, false);
								obj.setCantidad(obj.getCantidad() - cant);
								GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_cliente, obj);
							}
						} catch (final Exception e) {}
					}
				}
				if (!oficio.esGratisSiFalla() || resultado) {
					_cliente.addKamas(-_kamasPago, true, true);
					_artesano.addKamas(_kamasPago, true, true);
					for (final Duo<Integer, Integer> duo : _objetosPago) {
						try {
							final int cant = duo._segundo;
							if (cant == 0) {
								continue;
							}
							final Objeto obj = _cliente.getObjeto(duo._primero);
							if (obj.getCantidad() - cant < 1) {
								_cliente.borrarOEliminarConOR(duo._primero, false);
								_artesano.addObjIdentAInventario(obj, true);
							} else {
								obj.setCantidad(obj.getCantidad() - cant);
								GestorSalida.ENVIAR_OQ_CAMBIA_CANTIDAD_DEL_OBJETO(_cliente, obj);
								Objeto nuevoOjb = obj.clonarObjeto(cant, Constantes.OBJETO_POS_NO_EQUIPADO);
								_artesano.addObjIdentAInventario(nuevoOjb, false);
							}
						} catch (final Exception e) {}
					}
				}
			}
			_objetosSiSeConsegui.clear();
			_objetosPago.clear();
			_objArtesano.clear();
			_objCliente.clear();
			_kamasPago = 0;
			_kamasSiSeConsigue = 0;
			GestorSalida.ENVIAR_Ec_RESULTADO_RECETA(_artesano, _artesano.getForjaEc());
			GestorSalida.ENVIAR_Ec_RESULTADO_RECETA(_cliente, _cliente.getForjaEc());
			_artesano.setForjaEc("");
			_cliente.setForjaEc("");
		} catch (Exception e) {
			cerrar(null, "");
		}
	}
	
	public int cantSlotsctual() {
		return _objArtesano.size() + _objCliente.size();
	}
	
	@Override
	public synchronized void addObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {
		if (!perso.tieneObjetoID(objeto.getID()) || objeto.getPosicion() != Constantes.OBJETO_POS_NO_EQUIPADO) {
			GestorSalida.ENVIAR_Im_INFORMACION(perso, "1OBJECT_DONT_EXIST");
			return;
		}
		if (esTaller()) {
			addObjetoExchangerTaller(objeto, cantidad, perso, precio);
		} else {
			addObjetoExchangerCraft(objeto, cantidad, perso, precio);
		}
	}
	
	@Override
	public synchronized void remObjetoExchanger(Objeto objeto, int cantidad, Personaje perso, int precio) {
		if (esTaller()) {
			addObjetoExchangerTaller(objeto, -cantidad, perso, precio);
		} else {
			addObjetoExchangerCraft(objeto, -cantidad, perso, precio);
		}
	}
	
	public synchronized void addObjetoExchangerCraft(Objeto objeto, int cantidad, Personaje perso, int precio) {
		if (cantidad > 0) {
			//
			if (objeto.getObjModeloID() == 7508) {
				// runa de firma
			} else if ((!objeto.getObjModelo().esForjaMagueable() && _esForjaMagia) || !Constantes
			.getTipoObjPermitidoEnTrabajo(_skillID, objeto.getObjModelo().getTipo())) {
				return;
			}
			if (Constantes.getStatPorRunaPocima(objeto) <= 0 && Constantes.esSkillMago(_skillID)) {
				float coef = MainServidor.NIVEL_MAX_PERSONAJE / MainServidor.NIVEL_MAX_OFICIO;
				if (objeto.getObjModelo().getNivel() > _statOficio.getNivel() * coef) {
					GestorSalida.ENVIAR_M1_MENSAJE_SERVER_SVR_MUESTRA_INSTANTANEO(perso, 22, objeto.getObjModelo().getNivel()
					+ ";" + ((int) (objeto.getObjModelo().getNivel() / coef)), "");
					return;
				}
			}
		}
		int cantInter = _ingredientes.get(objeto.getID()) == null ? 0 : _ingredientes.get(objeto.getID());
		if (cantidad + cantInter > objeto.getCantidad()) {
			cantidad = objeto.getCantidad() - cantInter;
		} else if (cantidad + cantInter < 0) {
			cantidad = -cantInter;
		}
		if (cantidad == 0) {
			return;
		}
		_ingredientes.remove(objeto.getID());
		int nuevaCant = cantidad + cantInter;
		if (nuevaCant > 0) {
			_ingredientes.put(objeto.getID(), nuevaCant);
			GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(perso, 'O', "+", objeto.getID() + "|" + nuevaCant);
		} else {
			GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(perso, 'O', "-", objeto.getID() + "");
		}
	}
	
	public synchronized void addObjetoExchangerTaller(Objeto objeto, int cantidad, Personaje perso, int precio) {
		final int objetoID = objeto.getID();
		Personaje artesano = _artesano.getID() == perso.getID() ? _artesano : _cliente;
		Personaje cliente = _artesano.getID() == perso.getID() ? _cliente : _artesano;
		ArrayList<Duo<Integer, Integer>> objMovedor = _artesano.getID() == perso.getID() ? _objArtesano : _objCliente;
		ArrayList<Duo<Integer, Integer>> objDelOtro = _artesano.getID() == perso.getID() ? _objCliente : _objArtesano;
		final Duo<Integer, Integer> duoMovedor = Mundo.getDuoPorIDPrimero(objMovedor, objetoID);
		final Duo<Integer, Integer> duoOtro = Mundo.getDuoPorIDPrimero(objDelOtro, objetoID);
		final int cantInter = duoMovedor == null ? 0 : duoMovedor._segundo;
		if (cantidad + cantInter > objeto.getCantidad()) {
			cantidad = objeto.getCantidad() - cantInter;
		} else if (cantidad + cantInter < 0) {
			cantidad = -cantInter;
		}
		if (cantidad == 0) {
			return;
		}
		if (cantidad > 0) {
			if (objeto.getObjModeloID() == 7508) {
				// runa de firma
			} else if ((!objeto.getObjModelo().esForjaMagueable() && _esForjaMagia) || !Constantes
			.getTipoObjPermitidoEnTrabajo(_skillID, objeto.getObjModelo().getTipo())) {
				return;
			}
			if (_esForjaMagia) {
				if (Constantes.getStatPorRunaPocima(objeto) <= 0 && Constantes.esSkillMago(_skillID)) {
					float coef = MainServidor.NIVEL_MAX_PERSONAJE / MainServidor.NIVEL_MAX_OFICIO;
					if (objeto.getObjModelo().getNivel() > _statOficio.getNivel() * coef) {
						GestorSalida.ENVIAR_M1_MENSAJE_SERVER_SVR_MUESTRA_INSTANTANEO(artesano, 22, objeto.getObjModelo().getNivel()
						+ ";" + ((int) (objeto.getObjModelo().getNivel() / coef)), "");
						GestorSalida.ENVIAR_M1_MENSAJE_SERVER_SVR_MUESTRA_INSTANTANEO(cliente, 22, objeto.getObjModelo().getNivel()
						+ ";" + ((int) (objeto.getObjModelo().getNivel() / coef)), "");
						return;
					}
				}
			}
			if (duoMovedor == null && duoOtro == null && cantSlotsctual() >= getCasillasMax()) {
				// si el item es nuevo y ya llego al limite de slots
				return;
			}
		}
		desCheck();
		final String str = objetoID + "|" + cantidad;
		final String add = "|" + objeto.getObjModeloID() + "|" + objeto.convertirStatsAString(false);
		if (duoMovedor != null) {
			duoMovedor._segundo += cantidad;
			if (duoMovedor._segundo > 0) {
				GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(artesano, 'O', "+", "" + objetoID + "|" + duoMovedor._segundo);
				GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(cliente, 'O', "+", "" + objetoID + "|" + duoMovedor._segundo
				+ add);
			} else {
				objMovedor.remove(duoMovedor);
				GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(artesano, 'O', "-", objeto.getID() + "");
				GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(cliente, 'O', "-", objeto.getID() + "");
			}
		} else {
			GestorSalida.ENVIAR_EMK_MOVER_OBJETO_LOCAL(artesano, 'O', "+", str);
			GestorSalida.ENVIAR_EmK_MOVER_OBJETO_DISTANTE(cliente, 'O', "+", str + add);
			objMovedor.add(new Duo<Integer, Integer>(objetoID, cantidad));
		}
	}
	
	public synchronized void addObjetoPaga(final Objeto obj, int cant, final int pagoID) {
		desCheck();
		if (cant == 1) {
			cant = 1;
		}
		final int idObj = obj.getID();
		final String str = idObj + "|" + cant;
		final String add = "|" + obj.getObjModeloID() + "|" + obj.convertirStatsAString(false);
		if (pagoID == 1) {
			final Duo<Integer, Integer> duo = Mundo.getDuoPorIDPrimero(_objetosPago, idObj);
			if (duo != null) {
				duo._segundo += cant;
				GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_artesano, pagoID, "O", "+", idObj + "|" + duo._segundo
				+ add);
				GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_cliente, pagoID, "O", "+", idObj + "|" + duo._segundo);
			} else {
				GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_artesano, pagoID, "O", "+", str + add);
				GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_cliente, pagoID, "O", "+", str);
				_objetosPago.add(new Duo<Integer, Integer>(idObj, cant));
			}
		} else {
			final Duo<Integer, Integer> duo = Mundo.getDuoPorIDPrimero(_objetosSiSeConsegui, idObj);
			if (duo != null) {
				duo._segundo += cant;
				GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_artesano, pagoID, "O", "+", idObj + "|" + duo._segundo
				+ add);
				GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_cliente, pagoID, "O", "+", idObj + "|" + duo._segundo);
			} else {
				GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_artesano, pagoID, "O", "+", str + add);
				GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_cliente, pagoID, "O", "+", str);
				_objetosSiSeConsegui.add(new Duo<Integer, Integer>(idObj, cant));
			}
		}
	}
	
	public synchronized void quitarObjetoPaga(final Objeto obj, final int cant, final int idPago) {
		desCheck();
		final int idObj = obj.getID();
		if (idPago == 1) {
			final Duo<Integer, Integer> duo = Mundo.getDuoPorIDPrimero(_objetosPago, idObj);
			if (duo == null) {
				return;
			}
			GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_artesano, idPago, "O", "-", idObj + "");
			GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_cliente, idPago, "O", "-", idObj + "");
			final int nuevaCantidad = duo._segundo - cant;
			if (nuevaCantidad <= 0) {
				_objetosPago.remove(duo);
			} else {
				duo._segundo = nuevaCantidad;
				GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_artesano, idPago, "O", "+", idObj + "|" + nuevaCantidad + "|"
				+ obj.getObjModeloID() + "|" + obj.convertirStatsAString(false));
				GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_cliente, idPago, "O", "+", idObj + "|" + nuevaCantidad);
			}
		} else {
			final Duo<Integer, Integer> duo = Mundo.getDuoPorIDPrimero(_objetosSiSeConsegui, idObj);
			if (duo == null) {
				return;
			}
			GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_artesano, idPago, "O", "-", idObj + "");
			GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_cliente, idPago, "O", "-", idObj + "");
			final int nuevaCantidad = duo._segundo - cant;
			if (nuevaCantidad <= 0) {
				_objetosSiSeConsegui.remove(duo);
			} else {
				duo._segundo = nuevaCantidad;
				GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_artesano, idPago, "O", "+", idObj + "|" + nuevaCantidad + "|"
				+ obj.getObjModeloID() + "|" + obj.convertirStatsAString(false));
				GestorSalida.ENVIAR_Ep_PAGO_TRABAJO_KAMAS_OBJETOS(_cliente, idPago, "O", "+", idObj + "|" + nuevaCantidad);
			}
		}
	}
	
	public synchronized int getCantObjetoPago(final int idObj, final int tipoPago) {
		ArrayList<Duo<Integer, Integer>> objetos;
		if (tipoPago == 1) {
			objetos = _objetosPago;
		} else {
			objetos = _objetosSiSeConsegui;
		}
		for (final Duo<Integer, Integer> duo : objetos) {
			if (duo._primero == idObj) {
				return duo._segundo;
			}
		}
		return 0;
	}
	
	@Override
	public void addKamas(long kamas, Personaje perso) {}
	
	@Override
	public long getKamas() {
		return 0;
	}
	
	@Override
	public String getListaExchanger(Personaje perso) {
		// TODO Auto-generated method stub
		return null;
	}
}
