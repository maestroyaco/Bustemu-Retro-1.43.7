package estaticos;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import variables.hechizo.StatHechizo;
import variables.mapa.Celda;
import variables.mapa.Mapa;
import variables.mob.GrupoMob;
import variables.pelea.Luchador;
import variables.pelea.Pelea;
import variables.pelea.Trampa;
import variables.personaje.Personaje;
import estaticos.Mundo.Duo;

public class Camino {
	private static char[] DIRECCIONES = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
	private static byte[][] COORD_ALREDEDOR = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
	private static class CeldaCamino {
		private short id, valorX, cantPM, direccion, movimiento, level;
		private int distEstimadaX, distEstimada;
		private CeldaCamino anterior;
		
		private CeldaCamino() {}
	}
	
	public static Duo<Integer, ArrayList<Celda>> getPathPelea(final Mapa mapa, final short celdaInicio,
	short celdaDestino, int PM, Luchador tacleado, boolean ignoraLuchadores) {
		int intentos = 0;
		while (intentos < 5) {
			try {
				if (celdaInicio == celdaDestino || mapa.getCelda(celdaInicio) == null || mapa.getCelda(celdaDestino) == null) {
					return null;
				}
				if (PM < 0) {
					PM = 500;
				}
				final byte ancho = mapa.getAncho();
				// final int nroLados = 4;
				final byte[] diagonales = {ancho, (byte) (ancho - 1), (byte) -ancho, (byte) -(ancho - 1)};
				final byte[] unos = {1, 1, 1, 1};
				final Map<Short, Celda> celdas = new TreeMap<Short, Celda>();
				celdas.putAll(mapa.getCeldas());
				final Map<Short, CeldaCamino> celdasCamino1 = new TreeMap<Short, CeldaCamino>();
				final Map<Short, CeldaCamino> celdasCamino2 = new TreeMap<Short, CeldaCamino>();
				boolean ok = true;
				final CeldaCamino newCeldaCamino = new CeldaCamino();
				newCeldaCamino.id = celdaInicio;
				newCeldaCamino.cantPM = 0;
				newCeldaCamino.valorX = 0;
				newCeldaCamino.distEstimada = distanciaEstimada(mapa, celdaInicio, celdaDestino);
				newCeldaCamino.distEstimadaX = newCeldaCamino.distEstimada;
				newCeldaCamino.level = celdas.get(celdaInicio).getLevel();
				newCeldaCamino.movimiento = celdas.get(celdaInicio).getMovimiento();
				newCeldaCamino.anterior = null;
				celdasCamino1.put(newCeldaCamino.id, newCeldaCamino);
				// pone la primera celda de inicio
				short celdaAnterior = -1;
				while (ok) {
					short sigCelda = -1;
					int distEntreCeldas = 500000;
					for (final CeldaCamino c : celdasCamino1.values()) {
						if (c.distEstimadaX < distEntreCeldas) {
							distEntreCeldas = c.distEstimadaX;
							sigCelda = c.id;
						}
					}
					CeldaCamino celdaCamino = celdasCamino1.get(sigCelda);
					celdasCamino1.remove(sigCelda);
					if (celdaCamino.anterior != null) {
						celdaAnterior = celdaCamino.anterior.id;
					}
					if (celdaCamino.id == celdaDestino) {
						// se llego al objetivo
						final ArrayList<Celda> tempCeldas = new ArrayList<Celda>();
						while (celdaCamino.id != celdaInicio) {
							if (celdaCamino.movimiento == 0) {
								tempCeldas.clear();
							} else {
								tempCeldas.add(0, celdas.get(celdaCamino.id));
							}
							celdaCamino = celdaCamino.anterior;
						}
						return new Duo<Integer, ArrayList<Celda>>(intentos, tempCeldas);
					}
					boolean enemigoAlr = false;
					if (tacleado != null) {
						if (hayAlrededorAmigoOEnemigo(mapa, tacleado, false, true)) {
							enemigoAlr = true;
						}
					}
					byte[] direcciones = listaDirEntreDosCeldas2(mapa, celdaCamino.id, celdaDestino, celdaAnterior);
					boolean puedeLlegarDestino = false;
					if (!enemigoAlr) {
						for (int i = 0; i < 4; i++) {
							byte direccion = direcciones[i];
							final short tempCeldaID = (short) (celdaCamino.id + diagonales[direccion]);
							if (celdas.get(tempCeldaID) == null) {
								continue;
							}
							if (Math.abs(celdas.get(tempCeldaID).getCoordX() - celdas.get(celdaCamino.id).getCoordX()) <= 53) {
								final Celda tempCelda = celdas.get(tempCeldaID);
								final byte tempLevelCelda = tempCelda.getLevel();
								final boolean sinLuchador = (tempCeldaID == celdaDestino || ignoraLuchadores)
								? true
								: (tempCelda.getPrimerLuchador() == null ? true : false);
								puedeLlegarDestino = tempCeldaID == celdaDestino && tempCelda.getMovimiento() == 1 ? true : false;
								final boolean caminable = celdaCamino.level == -1 || Math.abs(tempLevelCelda - celdaCamino.level) < 2;
								if (caminable && tempCelda.getActivo() && sinLuchador) {
									final short posibleSigCelda = tempCeldaID;
									int aaaa = (tempCelda.getMovimiento() == 0 || tempCelda.getMovimiento() == 1 ? (1000) : 0);
									final short valorX = (short) (celdaCamino.valorX + unos[direccion] + aaaa + ((tempCelda
									.getMovimiento() == 1 && puedeLlegarDestino)
									? -1000
									: (direccion != celdaCamino.direccion ? 0.5 : 0) + (5 - tempCelda.getMovimiento()) / 3));
									final short cantMov = (short) (celdaCamino.cantPM + unos[direccion]);
									short tempValorX = -1;
									if (celdasCamino1.get(posibleSigCelda) != null) {
										tempValorX = celdasCamino1.get(posibleSigCelda).valorX;
									} else if (celdasCamino2.get(posibleSigCelda) != null) {
										tempValorX = celdasCamino2.get(posibleSigCelda).valorX;
									}
									if ((tempValorX == -1 || tempValorX > valorX) && cantMov <= PM) {
										if (celdasCamino2.get(posibleSigCelda) != null) {
											celdasCamino2.remove(posibleSigCelda);
										}
										final CeldaCamino tempCeldaCamino = new CeldaCamino();
										tempCeldaCamino.id = tempCeldaID;
										tempCeldaCamino.cantPM = cantMov;
										tempCeldaCamino.valorX = valorX;
										tempCeldaCamino.distEstimada = distanciaEstimada(mapa, tempCeldaID, celdaDestino);
										tempCeldaCamino.distEstimadaX = (tempCeldaCamino.valorX + tempCeldaCamino.distEstimada + (i * 3));
										tempCeldaCamino.direccion = direccion;
										tempCeldaCamino.level = tempLevelCelda;
										tempCeldaCamino.movimiento = tempCelda.getMovimiento();
										tempCeldaCamino.anterior = celdaCamino;
										celdasCamino1.put(posibleSigCelda, tempCeldaCamino);
									}
								}
							}
						}
					}
					celdasCamino2.put(celdaCamino.id, new CeldaCamino());
					celdasCamino2.get(celdaCamino.id).valorX = celdaCamino.valorX;
					ok = false;
					for (final CeldaCamino c : celdasCamino1.values()) {
						if (c == null) {
							continue;
						}
						ok = true;
						break;
					}
				}
				return null;
			} catch (final Exception e) {
				celdaDestino = celdaMasCercanaACeldaObjetivo(mapa, celdaDestino, celdaInicio, null, false);
				intentos++;
			}
		}
		return null;
	}
	
	public static short nroCeldasAMover(final Mapa mapa, final Pelea pelea, final AtomicReference<String> pathRef,
	final short celdaInicio, final short celdaFinal, Personaje perso) {
		short nuevaCelda = celdaInicio;
		short movimientos = 0;
		final String path = pathRef.get();
		final StringBuilder nuevoPath = new StringBuilder();
		for (int i = 0; i < path.length(); i += 3) {
			if (path.length() < i + 3) {
				return movimientos;
			}
			final String miniPath = path.substring(i, i + 3);
			final char cDir = miniPath.charAt(0);
			final short celdaTemp = Encriptador.hashACeldaID(miniPath.substring(1));
			// if (pelea != null && i > 0) {
			// if (getEnemigoAlrededor(nuevaCelda, mapa, null, pelea.getLuchadorTurno().getEquipoBin())
			// != null) {
			// pathRef.set(nuevoPath.toString());
			// return (short) (movimientos + 10000);
			// }
			// for (final Trampa trampa : pelea.getTrampas()) {
			// final int dist = distanciaDosCeldas(mapa, trampa.getCelda().getID(), nuevaCelda);
			// if (dist <= trampa.getTamaño()) {
			// pathRef.set(nuevoPath.toString());
			// return (short) (movimientos + 10000);
			// }
			// }
			// if (pelea.getMapaCopia().getCelda(nuevaCelda).getPrimerLuchador() != null) {
			// pathRef.set(nuevoPath.toString());
			// return (short) (movimientos + 20000);
			// }
			// }
			final String[] aPathInfos = pathSimpleValido(nuevaCelda, celdaTemp, getIndexPorDireccion(cDir), mapa, pelea,
			celdaFinal, perso).split(Pattern.quote(";"));
			String resultado = aPathInfos[0];
			int nroCeldas = Integer.parseInt(aPathInfos[1]);
			if (aPathInfos.length > 2) {
				nuevaCelda = Short.parseShort(aPathInfos[2]);
			}
			switch (resultado) {
				case "invisible" :
					movimientos += nroCeldas;
					nuevoPath.append(cDir + Encriptador.celdaIDAHash(nuevaCelda));
					pathRef.set(nuevoPath.toString());
					return (short) (movimientos + 20000);
				case "stop" :
				case "trampa" :
					movimientos += nroCeldas;
					nuevoPath.append(cDir + Encriptador.celdaIDAHash(nuevaCelda));
					pathRef.set(nuevoPath.toString());
					return (short) (movimientos + 10000);
				case "no" :
					pathRef.set(nuevoPath.toString());
					return -1000;
				case "ok" :
					nuevaCelda = celdaTemp;
					movimientos += nroCeldas;
					break;
			}
			nuevoPath.append(cDir + Encriptador.celdaIDAHash(nuevaCelda));
		}
		pathRef.set(nuevoPath.toString());
		return movimientos;
	}
	
	private static String pathSimpleValido(final short celdaID, final short celdaSemiFinal, final int dir,
	final Mapa mapa, final Pelea pelea, final short celdaFinalDest, Personaje perso) {
		short ultimaCelda = celdaID;
		for (int _nroMovimientos = 1; _nroMovimientos <= 64; _nroMovimientos++) {
			final short celdaTempID = getSigIDCeldaMismaDir(ultimaCelda, dir, mapa, pelea != null);
			Celda celdaTemp = mapa.getCelda(celdaTempID);
			if (celdaTemp == null || !celdaTemp.esCaminable(true)) {
				_nroMovimientos--;
				return "stop" + ";" + _nroMovimientos + ";" + ultimaCelda;
			}
			if (pelea != null) {
				Luchador ocupado = mapa.getCelda(celdaTempID).getPrimerLuchador();
				Luchador luchTurno = pelea.getLuchadorTurno();
				if (ocupado != null) {
					_nroMovimientos--;
					if (ocupado.esInvisible(luchTurno.getID())) {
						return ("invisible" + ";" + _nroMovimientos + ";" + ultimaCelda);
					} else {
						return ("stop" + ";" + _nroMovimientos + ";" + ultimaCelda);
					}
				}
				if (celdaTempID != celdaFinalDest) {
					// si algun luchador esta alrededor por donde va a pasar
					Luchador alrededor = getEnemigoAlrededor(celdaTempID, mapa, null, luchTurno.esIAChafer()
					? 3
					: luchTurno.getEquipoBin());
					if (alrededor != null && alrededor.getID() != luchTurno.getID()) {
						if (alrededor.esInvisible(luchTurno.getID())) {
							return "invisible" + ";" + _nroMovimientos + ";" + celdaTempID;
						} else {
							return "stop" + ";" + _nroMovimientos + ";" + celdaTempID;
						}
					}
					// si se topa con una trampa
					if (pelea.getTrampas() != null) {
						for (final Trampa trampa : pelea.getTrampas()) {
							final int dist = distanciaDosCeldas(mapa, trampa.getCelda().getID(), celdaTempID);
							if (dist <= trampa.getTamaño()) {
								return "trampa" + ";" + _nroMovimientos + ";" + celdaTempID;
							}
						}
					}
				}
			} else {
				try {
					for (Personaje p : mapa.getArrayPersonajes()) {
						if (!Constantes.puedeAgredir(perso, p)) {
							continue;
						}
						if ((p.getAlineacion() == Constantes.ALINEACION_BONTARIANO && perso
						.getAlineacion() == Constantes.ALINEACION_NEUTRAL) || (p
						.getAlineacion() == Constantes.ALINEACION_BRAKMARIANO && perso
						.getAlineacion() == Constantes.ALINEACION_NEUTRAL) || (p.getAlineacion() == Constantes.ALINEACION_MERCENARIO
						&& perso.getAlineacion() == Constantes.ALINEACION_NEUTRAL)) {
							continue;
						}
						int agroP = p.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_AGREDIR_AUTOMATICAMENTE);
						int agroPerso = perso.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_AGREDIR_AUTOMATICAMENTE);
						if (agroP <= 0 && agroPerso <= 0) {
							continue;
						}
						int distAgro = agroPerso >= agroP ? agroPerso : agroP;
						if (distanciaDosCeldas(mapa, p.getCelda().getID(), celdaTempID) <= (distAgro)) {
							return "stop" + ";" + _nroMovimientos + ";" + celdaTempID;
						}
					}
				} catch (Exception e) {}
				try {
					for (GrupoMob gm : mapa.getGrupoMobsTotales().values()) {
						if (!perso.estaDisponible(true, true)) {
							continue;
						}
						if (gm.getDistAgresion() <= 0) {
							continue;
						}
						if (perso.getAlineacion() == gm.getAlineacion()) {
							continue;
						}
						if ((gm.getAlineacion() == Constantes.ALINEACION_BONTARIANO && perso
						.getAlineacion() == Constantes.ALINEACION_NEUTRAL) || (gm
						.getAlineacion() == Constantes.ALINEACION_BRAKMARIANO && perso
						.getAlineacion() == Constantes.ALINEACION_NEUTRAL) || (gm
						.getAlineacion() == Constantes.ALINEACION_MERCENARIO && perso
						.getAlineacion() == Constantes.ALINEACION_NEUTRAL)) {
							continue;
						}
						if (distanciaDosCeldas(mapa, gm.getCeldaID(), celdaTempID) <= gm.getDistAgresion()) {
							return "stop" + ";" + _nroMovimientos + ";" + celdaTempID;
						}
					}
				} catch (Exception e) {}
				if (celdaTempID == celdaFinalDest) {
					if (celdaTemp.getObjetoInteractivo() != null) {
						// para hacer q los trigos, cereales e interactivos caminables
						// los demas como nidos bwaks y otros se muevan hasta ahi
						return "stop" + ";" + _nroMovimientos + ";" + ultimaCelda;
					}
				}
				if (!celdaTemp.accionesIsEmpty()) {
					return "stop" + ";" + _nroMovimientos + ";" + celdaTempID;
				}
			}
			if (celdaTempID == celdaSemiFinal) {
				return "ok" + ";" + _nroMovimientos;
			}
			ultimaCelda = celdaTempID;
		}
		return "no" + ";" + 0;
	}
	
	public static String getPathComoString(Mapa mapa, ArrayList<Celda> celdas, short celdaInicio, boolean esPelea) {
		StringBuilder pathStr = new StringBuilder();
		short tempCeldaID = celdaInicio;
		for (final Celda celda : celdas) {
			final int dir = Camino.direccionEntreDosCeldas(mapa, tempCeldaID, celda.getID(), esPelea);
			if (dir == -1) {
				return "";
			}
			pathStr.append(getDireccionPorIndex(dir));
			pathStr.append(Encriptador.celdaIDAHash(celda.getID()));
			tempCeldaID = celda.getID();
		}
		return pathStr.toString();
	}
	
	public static Luchador getEnemigoAlrededor(final short celdaID, final Mapa mapa, final ArrayList<Integer> noRepetir,
	final int equipoBinLanz) {
		Celda celda = mapa.getCelda(celdaID);
		for (byte[] c : COORD_ALREDEDOR) {
			Celda cell = mapa.getCeldaPorPos((byte) (celda.getCoordX() + c[0]), (byte) (celda.getCoordY() + c[1]));
			if (cell == null) {
				continue;
			}
			final Luchador luchador = cell.getPrimerLuchador();
			if (luchador == null) {
				continue;
			}
			if (noRepetir != null) {
				if (noRepetir.contains(luchador.getID())) {
					continue;
				}
			}
			if (luchador.getEquipoBin() != equipoBinLanz) {
				return luchador;
			}
		}
		return null;
	}
	
	public static boolean hayAlrededorAmigoOEnemigo(final Mapa mapa, final Luchador lanzador, final boolean amigo,
	boolean invisible) {
		Celda celda = lanzador.getCeldaPelea();
		for (byte[] c : COORD_ALREDEDOR) {
			Celda cell = mapa.getCeldaPorPos((byte) (celda.getCoordX() + c[0]), (byte) (celda.getCoordY() + c[1]));
			if (cell == null) {
				continue;
			}
			final Luchador luchador = cell.getPrimerLuchador();
			if (luchador != null && !luchador.estaMuerto()) {
				if (amigo) {
					if (luchador.getEquipoBin() == lanzador.getEquipoBin()) {
						return true;
					}
				} else {// enemigo
					if (luchador.getEquipoBin() != lanzador.getEquipoBin()) {
						if (invisible) {
							if (luchador.esInvisible(lanzador.getID())) {
								continue;
							}
						}
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static ArrayList<Luchador> luchadoresAlrededor(final Mapa mapa, final Pelea pelea, final Celda celda) {
		ArrayList<Luchador> luchadores = new ArrayList<>();
		for (byte[] c : COORD_ALREDEDOR) {
			Celda cell = mapa.getCeldaPorPos((byte) (celda.getCoordX() + c[0]), (byte) (celda.getCoordY() + c[1]));
			if (cell == null) {
				continue;
			}
			final Luchador luchador = cell.getPrimerLuchador();
			if (luchador != null) {
				luchadores.add(luchador);
			}
		}
		return luchadores;
	}
	
	public static boolean esSiguienteA(final Celda celda1, final Celda celda2) {
		byte x = (byte) Math.abs(celda1.getCoordX() - celda2.getCoordX());
		byte y = (byte) Math.abs(celda1.getCoordY() - celda2.getCoordY());
		return (x == 1 && y == 0) || (x == 0 && y == 1);
	}
	
	public static int distanciaEntreMapas(Mapa mapa1, Mapa mapa2) {
		if (mapa1.getSubArea().getArea().getSuperArea() != mapa2.getSubArea().getArea().getSuperArea())
			return 10000;
		return (Math.abs(mapa2.getX() - mapa1.getX()) + Math.abs(mapa2.getY() - mapa1.getY()));
	}
	
	public static short getSigIDCeldaMismaDir(final short celdaID, final int direccion, final Mapa mapa,
	final boolean combate) {
		switch (direccion) {
			case 0 :
				return (short) (combate ? -1 : celdaID + 1);// derecha
			case 1 :
				return (short) (celdaID + mapa.getAncho()); // diagonal derecha abajo
			case 2 :
				return (short) (combate ? -1 : celdaID + (mapa.getAncho() * 2 - 1));// abajo
			case 3 :
				return (short) (celdaID + (mapa.getAncho() - 1)); // diagonal izquierda abajo
			case 4 :
				return (short) (combate ? -1 : celdaID - 1);// izquierda
			case 5 :
				return (short) (celdaID - mapa.getAncho()); // diagonal izquierda arriba
			case 6 :
				return (short) (combate ? -1 : celdaID - (mapa.getAncho() * 2 - 1));// arriba
			case 7 :
				return (short) (celdaID - mapa.getAncho() + 1);// diagonal derecha arriba
		}
		return -1;
	}
	
	public static short distanciaDosCeldas(final Mapa mapa, final short celdaInicio, final short celdaDestino) {
		if (celdaInicio == celdaDestino) {
			return 0;
		}
		Celda cInicio = mapa.getCelda(celdaInicio);
		Celda cDestino = mapa.getCelda(celdaDestino);
		if (cInicio == null || cDestino == null) {
			return 0;
		}
		final int difX = Math.abs(cInicio.getCoordX() - cDestino.getCoordX());
		final int difY = Math.abs(cInicio.getCoordY() - cDestino.getCoordY());
		return (short) (difX + difY);
	}
	
	private static short distanciaEstimada(final Mapa mapa, final short celdaInicio, final short celdaDestino) {
		if (celdaInicio == celdaDestino) {
			return 0;
		}
		Celda cInicio = mapa.getCelda(celdaInicio);
		Celda cDestino = mapa.getCelda(celdaDestino);
		if (cInicio == null || cDestino == null) {
			return 0;
		}
		final int difX = Math.abs(cInicio.getCoordX() - cDestino.getCoordX());
		final int difY = Math.abs(cInicio.getCoordY() - cDestino.getCoordY());
		// return (short) Math.sqrt(Math.pow(difX, 2) + Math.pow(difY, 2));
		// era antes pero lo modifique
		return (short) (difX + difY);
	}
	
	public static Duo<Integer, Short> getCeldaDespuesDeEmpujon(final Pelea pelea, final Celda celdaInicio,
	final Celda celdaObjetivo, int movimientos) {
		if (celdaInicio.getID() == celdaObjetivo.getID()) {
			return new Duo<Integer, Short>(-1, (short) -1);
		}
		Mapa mapa = pelea.getMapaCopia();
		int dir = direccionEntreDosCeldas(mapa, celdaInicio.getID(), celdaObjetivo.getID(), true);
		short celdaID = celdaObjetivo.getID();
		if (movimientos < 0) {
			dir = getDireccionOpuesta(dir);
			movimientos = -movimientos;
		}
		for (int i = 0; i < movimientos; i++) {
			final short sigCeldaID = getSigIDCeldaMismaDir(celdaID, dir, mapa, true);
			Celda sigCelda = mapa.getCelda(sigCeldaID);
			if (sigCelda == null || !sigCelda.esCaminable(true) || sigCelda.getPrimerLuchador() != null) {
				return new Duo<Integer, Short>((movimientos - i), celdaID);
			}
			if (pelea.getTrampas() != null) {
				for (final Trampa trampa : pelea.getTrampas()) {
					final int dist = distanciaDosCeldas(mapa, trampa.getCelda().getID(), sigCeldaID);
					if (dist <= trampa.getTamaño()) {
						return new Duo<Integer, Short>(0, sigCeldaID);
					}
				}
			}
			celdaID = sigCeldaID;
		}
		if (celdaID == celdaObjetivo.getID()) {
			return new Duo<Integer, Short>(-1, (short) -1);
		}
		return new Duo<Integer, Short>(0, celdaID);
	}
	
	public static int getDireccionOpuesta(int dir) {
		return correctaDireccion(dir - 4);
	}
	
	public static boolean siCeldasEstanEnMismaLinea(final Mapa mapa, final Celda c1, final Celda c2) {
		if (c1.getID() == c2.getID()) {
			return true;
		}
		return c1.getCoordX() == c2.getCoordX() || c1.getCoordY() == c2.getCoordY();
	}
	
	public static byte getIndexPorDireccion(char c) {
		byte b = 0;
		for (char a : DIRECCIONES) {
			if (a == c) {
				return b;
			}
			b++;
		}
		return 0;
	}
	
	public static char getDireccionAleatorio(final boolean combate) {
		return DIRECCIONES[Formulas.getRandomInt(0, combate ? 7 : 3)];
	}
	
	public static char getDireccionPorIndex(final int index) {
		return DIRECCIONES[index];
	}
	
	public static int direccionEntreDosCeldas(final Mapa mapa, final short celdaInicio, final short celdaDestino,
	final boolean esPelea) {
		if (celdaInicio == celdaDestino || mapa == null) {
			return -1;
		}
		if (!esPelea) {
			final byte ancho = mapa.getAncho();
			final byte[] alrededores = {1, ancho, (byte) (ancho * 2 - 1), (byte) (ancho - 1), -1, (byte) -ancho,
			(byte) (-ancho * 2 + 1), (byte) -(ancho - 1)};
			final int _loc7 = celdaDestino - celdaInicio;
			for (int _loc8 = 7; _loc8 >= 0; _loc8--) {
				if (alrededores[_loc8] == _loc7) {
					return _loc8;
				}
			}
		}
		Celda cInicio = mapa.getCelda(celdaInicio);
		Celda cDestino = mapa.getCelda(celdaDestino);
		final int difX = cDestino.getCoordX() - cInicio.getCoordX();
		final int difY = cDestino.getCoordY() - cInicio.getCoordY();
		if (difX == 0) {
			if (difY > 0) {
				return 3;
			} else {
				return 7;
			}
		} else if (difX > 0) {
			return 1;
		} else {
			return 5;
		}
	}
	
	private static char[] listaDirEntreDosCeldas(final Mapa mapa, final short celdaInicio, final short celdaDestino) {
		if (celdaInicio == celdaDestino || mapa == null) {
			return new char[]{};
		}
		char[] abc = new char[4];
		byte[] b = listaDirEntreDosCeldas2(mapa, celdaInicio, celdaDestino, (short) -1);
		for (int i = 0; i < 4; i++) {
			switch (b[i]) {
				case 0 :
					abc[i] = 'b';
					break;
				case 1 :
					abc[i] = 'd';
					break;
				case 2 :
					abc[i] = 'f';
					break;
				case 3 :
					abc[i] = 'h';
					break;
			}
		}
		return abc;
	}
	
	public static byte[] listaDirEntreDosCeldas2(final Mapa mapa, final short celdaInicio, final short celdaDestino,
	final short celdaAnterior) {
		if (celdaInicio == celdaDestino || mapa == null) {
			return new byte[]{};
		}
		Celda cInicio = mapa.getCelda(celdaInicio);
		Celda cDestino = mapa.getCelda(celdaDestino);
		final int difX = cDestino.getCoordX() - cInicio.getCoordX();
		final int difY = cDestino.getCoordY() - cInicio.getCoordY();
		if (Math.abs(difY) == Math.abs(difX) && celdaAnterior > 0) {
			return listaDirEntreDosCeldas2(mapa, celdaAnterior, celdaDestino, (short) -1);
		} else if (Math.abs(difY) > Math.abs(difX)) {
			int[][] c = {{difX, 0, 2}, {difY, 1, 3}};
			return formulaDireccion(c);
		} else {
			int[][] c = {{difY, 1, 3}, {difX, 0, 2}};
			return formulaDireccion(c);
		}
	}
	
	private static byte[] formulaDireccion(int[][] c) {
		byte[] abc = new byte[4];
		for (int i = 0; i < 2; i++) {
			int dif = c[i][0];
			int p = i;
			if (dif < 0) {
				p = Math.abs(3 - i);
			}
			abc[p] = (byte) c[i][1];
			abc[Math.abs(3 - p)] = (byte) c[i][2];
		}
		return abc;
	}
	
	private static int correctaDireccion(int dir) {
		while (dir < 0) {
			dir += 8;
		}
		while (dir >= 8) {
			dir -= 8;
		}
		return dir;
	}
	
	public static byte[] getCoordPorDireccion(final int dir) {
		byte[][] f = {{1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}};
		return f[dir];
	}
	
	public static ArrayList<Celda> celdasAfectadasEnElArea(final Mapa mapa, short celdaIDObjetivo,
	final short celdaIDLanzador, final String areaEfecto) {
		final ArrayList<Celda> celdas = new ArrayList<Celda>();
		if (mapa.getCelda(celdaIDObjetivo) == null) {
			return celdas;
		}
		final int tamaño = Encriptador.getNumeroPorValorHash(areaEfecto.charAt(1));
		switch (areaEfecto.charAt(0)) {
			case 'A' :// cruz en celda lanzador
				for (int a = tamaño; a >= 0; a--) {
					for (final short celda2 : celdasPorCruz(mapa.getCelda(celdaIDLanzador), mapa, a)) {
						Celda celda = mapa.getCelda(celda2);
						if (!celdas.contains(celda)) {
							celdas.add(celda);
						}
					}
				}
				break;
			case 'D' :// diagonales
				int i = tamaño % 2 == 0 ? 1 : 0;
				for (; i < tamaño; i += 2) {
					for (final short celda2 : celdasPorDistancia(mapa.getCelda(celdaIDObjetivo), mapa, i + 1)) {
						Celda celda = mapa.getCelda(celda2);
						if (!celdas.contains(celda)) {
							celdas.add(celda);
						}
					}
				}
				break;
			case 'C' :// Circulo
				if (tamaño >= 64) {
					celdas.addAll(mapa.getCeldas().values());
					break;
				}
				for (int a = tamaño; a >= 0; a--) {
					for (final short celda2 : celdasPorDistancia(mapa.getCelda(celdaIDObjetivo), mapa, a)) {
						Celda celda = mapa.getCelda(celda2);
						if (!celdas.contains(celda)) {
							celdas.add(celda);
						}
					}
				}
				break;
			case 'O' :// anillo
				for (final short celda2 : celdasPorDistancia(mapa.getCelda(celdaIDObjetivo), mapa, tamaño)) {
					Celda celda = mapa.getCelda(celda2);
					if (!celdas.contains(celda)) {
						celdas.add(celda);
					}
				}
				break;
			case 'X' :// Cruz
				for (int a = tamaño; a >= 0; a--) {
					for (final short celda2 : celdasPorCruz(mapa.getCelda(celdaIDObjetivo), mapa, a)) {
						Celda celda = mapa.getCelda(celda2);
						if (!celdas.contains(celda)) {
							celdas.add(celda);
						}
					}
				}
				break;
			case 'T' :// Estilo Baston
				final int dir2 = direccionEntreDosCeldas(mapa, celdaIDLanzador, celdaIDObjetivo, true);
				for (final short celda2 : celdasPorLinea(mapa.getCelda(celdaIDObjetivo), mapa, tamaño, correctaDireccion(dir2
				- 2))) {
					Celda celda = mapa.getCelda(celda2);
					if (!celdas.contains(celda)) {
						celdas.add(celda);
					}
				}
				for (final short celda2 : celdasPorLinea(mapa.getCelda(celdaIDObjetivo), mapa, tamaño, correctaDireccion(dir2
				+ 2))) {
					Celda celda = mapa.getCelda(celda2);
					if (!celdas.contains(celda)) {
						celdas.add(celda);
					}
				}
				if (!celdas.contains(mapa.getCelda(celdaIDObjetivo))) {
					celdas.add(mapa.getCelda(celdaIDObjetivo));
				}
				break;
			case 'L' :// Linea
				final int dir = direccionEntreDosCeldas(mapa, celdaIDLanzador, celdaIDObjetivo, true);
				for (final short celda2 : celdasPorLinea(mapa.getCelda(celdaIDObjetivo), mapa, tamaño, correctaDireccion(
				dir))) {
					Celda celda = mapa.getCelda(celda2);
					if (!celdas.contains(celda)) {
						celdas.add(celda);
					}
				}
				break;
			case 'P' :// Jugador?
				if (!celdas.contains(mapa.getCelda(celdaIDObjetivo))) {
					celdas.add(mapa.getCelda(celdaIDObjetivo));
				}
				break;
			default :
				MainServidor.redactarLogServidorln("[FIXME]Tipo de alcance no reconocido: " + areaEfecto.charAt(0));
				break;
		}
		return celdas;
	}
	
	public static ArrayList<Short> celdasPorDistancia(final Celda celda, final Mapa mapa, final int distancia) {
		ArrayList<Short> celdas = new ArrayList<Short>();
		byte x = celda.getCoordX(), y = celda.getCoordY();
		byte[][] f = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
		for (int x2 = 0; x2 <= distancia; x2++) {
			int y2 = distancia - x2;
			for (byte[] b : f) {
				Celda cell = mapa.getCeldaPorPos((byte) (x + (b[0] * x2)), (byte) (y + (b[1] * y2)));
				if (cell != null) {
					if (!celdas.contains(cell.getID()))
						celdas.add(cell.getID());
				}
			}
		}
		return celdas;
	}
	
	private static ArrayList<Short> celdasPorCruz(final Celda celda, final Mapa mapa, final int distancia) {
		ArrayList<Short> celdas = new ArrayList<Short>();
		byte x = celda.getCoordX(), y = celda.getCoordY();
		for (byte[] b : COORD_ALREDEDOR) {
			Celda cell = mapa.getCeldaPorPos((byte) (x + (b[0] * distancia)), (byte) (y + (b[1] * distancia)));
			if (cell != null) {
				if (!celdas.contains(cell.getID()))
					celdas.add(cell.getID());
			}
		}
		return celdas;
	}
	
	private static ArrayList<Short> celdasPorLinea(final Celda celda, final Mapa mapa, final int distancia, int dir) {
		ArrayList<Short> celdas = new ArrayList<Short>();
		if (dir == -1) {
			return celdas;
		}
		byte x = celda.getCoordX(), y = celda.getCoordY();
		byte[] b = getCoordPorDireccion(dir);
		for (int x2 = distancia; x2 >= 0; x2--) {
			Celda cell = mapa.getCeldaPorPos((byte) (x + (b[0] * x2)), (byte) (y + (b[1] * x2)));
			if (cell != null) {
				if (!celdas.contains(cell.getID()))
					celdas.add(cell.getID());
			}
		}
		return celdas;
	}
	
	public static ArrayList<Celda> celdasPosibleLanzamiento(final StatHechizo SH, final Luchador lanzador,
	final Mapa mapa, short tempCeldaIDLanzador, short celdaObjetivo) {
		final ArrayList<Celda> celdasF = new ArrayList<Celda>();
		Personaje perso = lanzador.getPersonaje();
		int maxAlc = SH.getMaxAlc();
		final int minAlc = SH.getMinAlc();
		boolean alcModificable = SH.esAlcanceModificable();
		boolean lineaVista = SH.esLineaVista();
		boolean lanzarLinea = SH.esLanzarLinea();
		boolean necesitaCeldaLibre = SH.esNecesarioCeldaLibre();
		boolean necesitaObjetivo = SH.esNecesarioObjetivo();
		final int hechizoID = SH.getHechizoID();
		if (perso != null && perso.tieneModfiSetClase(hechizoID)) {
			maxAlc += perso.getModifSetClase(hechizoID, 281);
			alcModificable |= perso.getModifSetClase(hechizoID, 282) == 1;
			lanzarLinea &= !(perso.getModifSetClase(hechizoID, 288) == 1);
			lineaVista &= !(perso.getModifSetClase(hechizoID, 289) == 1);
		}
		if (alcModificable) {
			maxAlc += lanzador.getTotalStats().getTotalStatParaMostrar(Constantes.STAT_MAS_ALCANCE);
		}
		if (maxAlc < minAlc) {
			maxAlc = minAlc;
		}
		Celda celdaI = mapa.getCelda(tempCeldaIDLanzador);
		if (celdaI == null) {
			celdaI = lanzador.getCeldaPelea();
		}
		boolean suponiendo = lanzador.getCeldaPelea().getID() != celdaI.getID();
		for (Celda celdaC : mapa.getCeldas().values()) {
			if (celdaC == null) {
				continue;
			}
			int dist = Camino.distanciaDosCeldas(mapa, celdaI.getID(), celdaC.getID());
			if (dist < minAlc || dist > maxAlc) {
				if (celdaObjetivo == celdaC.getID()) {
					if (perso != null) {
						GestorSalida.ENVIAR_Im_INFORMACION(perso, "1171;" + minAlc + "~" + maxAlc + "~" + dist);
					}
					if (MainServidor.MODO_DEBUG) {
						System.out.println("El hechizo " + SH.getHechizo().getNombre() + " esta fuera del rango");
					}
				}
				continue;
			}
			if (lanzarLinea) {
				if (celdaI.getCoordX() != celdaC.getCoordX() && celdaI.getCoordY() != celdaC.getCoordY()) {
					if (celdaObjetivo == celdaC.getID()) {
						if (perso != null) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1173");
						}
						if (MainServidor.MODO_DEBUG) {
							System.out.println("El hechizo " + SH.getHechizo().getNombre() + " necesita lanzarse en linea recta");
						}
					}
					continue;
				}
			}
			if (necesitaCeldaLibre) {
				if (celdaC.getMovimiento() > 1 && celdaC.getPrimerLuchador() != null) {
					if (celdaObjetivo == celdaC.getID()) {
						if (perso != null) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1172");
						}
						if (MainServidor.MODO_DEBUG) {
							System.out.println("El hechizo " + SH.getHechizo().getNombre() + " necesita celda libre");
						}
					}
					continue;
				}
				if (celdaC.getMovimiento() <= 1) {
					continue;
				}
			}
			if (necesitaObjetivo) {
				if (celdaC.getPrimerLuchador() == null) {
					if (celdaObjetivo == celdaC.getID()) {
						if (perso != null) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1172");
						}
						if (MainServidor.MODO_DEBUG) {
							System.out.println("El hechizo " + SH.getHechizo().getNombre() + " necesita un objetivo");
						}
					}
					continue;
				}
			}
			if (lineaVista) {
				if (!lineaDeVista1(mapa, celdaI, celdaC, lanzador, suponiendo, celdaObjetivo)) {
					if (celdaObjetivo == celdaC.getID()) {
						if (perso != null) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1174");
						}
						if (MainServidor.MODO_DEBUG) {
							System.out.println("El hechizo " + SH.getHechizo().getNombre() + " tiene linea de vista");
						}
					}
					continue;
				}
			}
			celdasF.add(celdaC);
		}
		// for (Celda c : celdasF) {
		// GestorSalida.enviar(perso, "GDZ|+" + c.getID() + ";0;3");
		// }
		return celdasF;
	}
	
	private static Celda celdaPorCoordenadas(Mapa mapa, int x, int y) {
		return mapa.getCelda((short) (x * mapa.getAncho() + y * (mapa.getAncho() - 1)));
	}
	
	private static boolean lineaDeVista1(Mapa mapa, Celda celdaI, Celda celdaC, Luchador lanzador, boolean suponiendo,
	short celdaObjetivo) {
		float _loc9 = celdaI.tieneSprite(lanzador.getID(), suponiendo) ? (1.5f) : (0);
		float _loc10 = celdaC.tieneSprite(lanzador.getID(), suponiendo) ? (1.5f) : (0);
		float zI = celdaI.getAlto() + _loc9;
		float zC = celdaC.getAlto() + _loc10;
		float _loc11 = zC - zI;
		float _loc12 = Math.max(Math.abs(celdaI.getCoordY() - celdaC.getCoordY()), Math.abs(celdaI.getCoordX() - celdaC
		.getCoordX()));
		float _loc13 = ((float) (celdaI.getCoordY() - celdaC.getCoordY()) / (float) (celdaI.getCoordX() - celdaC
		.getCoordX()));
		boolean isNaN = Float.isInfinite(_loc13) || Float.isNaN(_loc13);
		float _loc14 = celdaI.getCoordY() - _loc13 * celdaI.getCoordX();
		float _loc15 = celdaC.getCoordX() - celdaI.getCoordX() < 0 ? (-1) : (1);
		float _loc16 = celdaC.getCoordY() - celdaI.getCoordY() < 0 ? (-1) : (1);
		int _loc17 = celdaI.getCoordY();
		// int _loc18 = celdaI.getX();
		float _loc19 = celdaC.getCoordX() * _loc15;
		// int _loc20 = celdaC.getY() * _loc16;
		float _loc26 = 0;
		float _loc27 = celdaI.getCoordX() + 0.5f * _loc15;
		// if (celdaC.getID() == celdaObjetivo) {
		// System.out.println("_loc5.x " + celdaI.getX() + " _loc5.y " + celdaI.getY() + " alto5 " +
		// celdaI.getAlto()
		// + " _loc5.z " + zI + " _loc6.x " + celdaC.getX() + " _loc6.y " + celdaC.getY() + " alto6 " +
		// celdaC.getAlto()
		// + " _loc6.z " + zC + " _loc9 " + _loc9 + " _loc10 " + _loc10 + " _loc11 " + _loc11 +
		// " _loc12 " + _loc12
		// + " _loc13 " + _loc13 + " _loc14 " + _loc14 + " _loc15 " + _loc15 + " _loc16 " + _loc16 +
		// " _loc17 " + _loc17
		// + " _loc19 " + _loc19 + " _loc27 " + _loc27);
		// }
		if (!isNaN) {
			for (; _loc27 * _loc15 <= _loc19; _loc27 = _loc27 + _loc15) {
				float _loc25 = _loc13 * _loc27 + _loc14;
				int _loc21 = 0;
				int _loc22 = 0;
				if (_loc16 > 0) {
					_loc21 = Math.round(_loc25);
					_loc22 = (int) Math.ceil(_loc25 - 0.5f);
				} else {
					_loc21 = (int) Math.ceil(_loc25 - 0.5f);
					_loc22 = Math.round(_loc25);
				}
				_loc26 = _loc17;
				for (; _loc26 * _loc16 <= _loc22 * _loc16; _loc26 = _loc26 + _loc16) {
					if (!lineaDeVista2(mapa, (int) (_loc27 - _loc15 / 2), (int) _loc26, false, celdaI, celdaC, zI, zC, _loc11,
					_loc12, lanzador.getID(), suponiendo, celdaObjetivo)) {
						return (false);
					}
				}
				_loc17 = _loc21;
			}
		}
		_loc26 = _loc17;// celdaI.getY();
		for (; _loc26 * _loc16 <= celdaC.getCoordY() * _loc16; _loc26 = _loc26 + _loc16) {
			if (!lineaDeVista2(mapa, (int) (_loc27 - 0.5f * _loc15), (int) _loc26, false, celdaI, celdaC, zI, zC, _loc11,
			_loc12, lanzador.getID(), suponiendo, celdaObjetivo)) {
				// if (celdaC.getID() == celdaObjetivo) {
				// System.out.println("FUE EN LINEA 2 ");
				// }
				return (false);
			}
		}
		if (!lineaDeVista2(mapa, (int) (_loc27 - 0.5f * _loc15), (int) (_loc26 - _loc16), true, celdaI, celdaC, zI, zC,
		_loc11, _loc12, lanzador.getID(), suponiendo, celdaObjetivo)) {
			// if (celdaC.getID() == celdaObjetivo) {
			// System.out.println("FUE EN LINEA 3 ");
			// }
			return (false);
		}
		return (true);
	}
	
	private static boolean lineaDeVista2(Mapa mapa, int x, int y, boolean bool, Celda celdaI, Celda celdaC, float zI,
	float zC, float zDiff, float d, int idLanzador, boolean suponiendo, short celdaObjetivo) {
		Celda _loc11 = celdaPorCoordenadas(mapa, x, y);
		float _loc12 = Math.max(Math.abs(celdaI.getCoordY() - y), Math.abs(celdaI.getCoordX() - x));
		float _loc13 = _loc12 / d * zDiff + zI;
		float _loc14 = _loc11.getAlto();
		boolean _loc15 = !_loc11.tieneSprite(idLanzador, suponiendo) || (_loc12 == 0 || (bool || celdaC.getCoordX() == x
		&& celdaC.getCoordY() == y)) ? (false) : (true);
		// if (celdaObjetivo == _loc11.getID()) {
		// System.out.println(" _loc11.lineaDeVista " + _loc11.lineaDeVista() + " _loc12 " + _loc12 +
		// " _loc13 " + _loc13
		// + " _loc14 " + _loc14 + " _loc15 " + _loc15 + " _loc14 <= _loc13 " + (_loc14 <= _loc13)
		// + " (_loc14 <= _loc13 && !_loc15) " + (_loc14 <= _loc13 && !_loc15));
		// }
		// NaN en java con condicional siempre es FALSE, en AS2 es true con >= <= y FALSE con ==
		if (_loc11.lineaDeVista() && ((Float.isNaN(_loc13) || _loc14 <= _loc13) && !_loc15)) {
			return (true);
		} else {
			if (bool) {
				return (true);
			}
			return (false);
		}
	}
	
	private static short celdaMasCercanaACeldaObjetivo(final Mapa mapa, final short celdaInicio, final short celdaDestino,
	ArrayList<Celda> celdasProhibidas, final boolean ocupada) {
		if (mapa.getCelda(celdaInicio) == null || mapa.getCelda(celdaDestino) == null) {
			return -1;
		}
		int distancia = 1000;
		short celdaID = celdaInicio;
		if (celdasProhibidas == null) {
			celdasProhibidas = new ArrayList<Celda>();
		}
		final char[] dirs = listaDirEntreDosCeldas(mapa, celdaInicio, celdaDestino);
		for (final char d : dirs) {
			final short sigCelda = getSigIDCeldaMismaDir(celdaInicio, d, mapa, true);
			final Celda celda = mapa.getCelda(sigCelda);
			if (celda == null) {
				continue;
			}
			final int tempDistancia = distanciaDosCeldas(mapa, celdaDestino, sigCelda);
			if (tempDistancia < distancia && celda.esCaminable(true) && (!ocupada || celda.getPrimerLuchador() == null)
			&& !celdasProhibidas.contains(celda)) {
				distancia = tempDistancia;
				celdaID = sigCelda;
			}
		}
		return celdaID == celdaInicio ? -1 : celdaID;
	}
	
	public static ArrayList<Short> celdasDeMovimiento(final Pelea pelea, final Celda celdaInicio, boolean filtro,
	boolean ocupadas, Luchador tacleado) {
		final ArrayList<Short> celdas = new ArrayList<Short>();
		if (pelea.getPMLuchadorTurno() <= 0) {
			return celdas;
		}
		Mapa mapa = pelea.getMapaCopia();
		for (int a = 0; a <= pelea.getPMLuchadorTurno(); a++) {
			for (final short tempCeldaID : celdasPorDistancia(celdaInicio, mapa, a)) {
				Celda tempCelda = mapa.getCelda(tempCeldaID);
				if (!tempCelda.esCaminable(true)) {
					continue;
				}
				if (ocupadas && tempCelda.getPrimerLuchador() != null) {
					continue;
				}
				if (!celdas.contains(tempCeldaID)) {
					if (filtro) {
						Duo<Integer, ArrayList<Celda>> pathTemp = getPathPelea(mapa, celdaInicio.getID(), tempCeldaID, pelea
						.getPMLuchadorTurno(), tacleado, false);
						if (pathTemp == null) {
							continue;
						}
						if (pathTemp._segundo.isEmpty()) {
							continue;
						}
						if (pathTemp._segundo.get(pathTemp._segundo.size() - 1).getID() != tempCeldaID) {
							continue;
						}
					}
					celdas.add(tempCeldaID);
				}
			}
		}
		return celdas;
	}
	
	public static short celdaMoverSprite(final Mapa mapa, final short celda) {
		final ArrayList<Short> celdasPosibles = new ArrayList<Short>();
		final short ancho = mapa.getAncho();
		final short[] dir = {(short) -ancho, (short) -(ancho - 1), (short) (ancho - 1), ancho};
		for (final short element : dir) {
			try {
				if (celda + element > 14 || celda + element < 464) {
					if (mapa.getCelda((short) (celda + element)).esCaminable(false)) {
						celdasPosibles.add((short) (celda + element));
					}
				}
			} catch (Exception e) {}
		}
		if (celdasPosibles.size() <= 0) {
			return -1;
		}
		return celdasPosibles.get(Formulas.getRandomInt(0, celdasPosibles.size() - 1));
	}
	
	public static short getCeldaIDCercanaLibre(final Celda celda, final Mapa mapa) {
		for (byte[] c : COORD_ALREDEDOR) {
			Celda cell = mapa.getCeldaPorPos((byte) (celda.getCoordX() + c[0]), (byte) (celda.getCoordY() + c[1]));
			if (cell != null && cell.getObjetoTirado() == null && cell.getPrimerPersonaje() == null && cell.esCaminable(
			false)) {
				return cell.getID();
			}
		}
		return 0;
	}
	
	public static short ultimaCeldaID(Mapa mapa) {
		return (short) (mapa.getAncho() * mapa.getAlto() * 2 - (mapa.getAlto() + mapa.getAncho()));
	}
	
	public static boolean esCeldaLadoIzq(byte ancho, byte alto, final short celda) {
		short ladoIzq = ancho;
		for (int i = 0; i < alto; i++) {
			if (celda == ladoIzq || celda == ladoIzq - ancho) {
				return true;
			}
			ladoIzq += ancho * 2 - 1;
		}
		return false;
	}
	
	public static boolean esCeldaLadoDer(byte ancho, byte alto, final short celda) {
		short ladoDer = (short) (2 * (ancho - 1));
		for (int i = 0; i < alto; i++) {
			if (celda == ladoDer || celda == ladoDer - ancho + 1) {
				return true;
			}
			ladoDer += ancho * 2 - 1;
		}
		return false;
	}
	
	public static boolean celdaSalienteLateral(byte ancho, byte alto, final short celda1, final short celda2) {
		if (esCeldaLadoIzq(ancho, alto, celda1) && (celda2 == celda1 + ancho - 1 || celda2 == celda1 - ancho)) {
			return true;
		}
		if (esCeldaLadoDer(ancho, alto, celda1) && (celda2 == celda1 - ancho + 1 || celda2 == celda1 + ancho)) {
			return true;
		}
		return false;
	}
}
