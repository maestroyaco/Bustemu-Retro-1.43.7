package variables.mapa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import servidor.ServidorSocket.AccionDeJuego;
import variables.casa.Casa;
import variables.casa.Cofre;
import variables.mapa.interactivo.ObjetoInteractivo;
import variables.objeto.Objeto;
import variables.objeto.ObjetoModelo.CAPACIDAD_STATS;
import variables.pelea.Glifo;
import variables.pelea.Luchador;
import variables.pelea.Trampa;
import variables.personaje.Personaje;
import variables.zotros.Accion;
import estaticos.Condiciones;
import estaticos.Constantes;
import estaticos.MainServidor;
import estaticos.Encriptador;
import estaticos.Formulas;
import estaticos.GestorSalida;
import estaticos.Mundo;

// public void destruir() {
// try {
// // for (Celda celda : _celdas.values()) {
// // celda.destruir();
// // }
// // this.finalize();
// } catch (Throwable e) {
// Bustemu.escribirLog("Throwable destruir mapa " + e.toString());
// e.printStackTrace();
// }
// }
public class Celda {
	private short _celdaID;
	private short _mapaID;
	private Mapa _mapa;
	private long _ultimoUsoTrigger;
	private CopyOnWriteArrayList<Personaje> _personajes;
	private CopyOnWriteArrayList<Trampa> _trampas;
	private CopyOnWriteArrayList<Glifo> _glifos;
	private ArrayList<Luchador> _luchadores;
	private Map<Integer, Accion> _acciones;
	private boolean _activo, _esCaminableLevel;
	private boolean _lineaDeVista = true;
	private boolean _conGDF = false;
	private byte _level;
	private byte _movimiento;
	private byte _coordX;
	private byte _coordY;
	private byte _estadoCelda;
	private byte _movimientoInicial;
	private byte _slope;
	private final ObjetoInteractivo _objetoInterac;
	private Objeto _objetoTirado;
	
	public Celda(final Mapa mapa, final short id, final boolean activo, final byte movimiento, final byte level,
	final byte slope, final boolean lineaDeVista, final int objID) {
		_mapa = mapa;
		_mapaID = mapa.getID();
		_celdaID = id;
		_activo = activo;
		_level = level;
		_movimiento = movimiento;
		_lineaDeVista = lineaDeVista;
		_estadoCelda = Constantes.CI_ESTADO_LLENO;
		_movimientoInicial = _movimiento;
		_slope = slope;
		final byte ancho = mapa.getAncho();
		final int _loc5 = (int) Math.floor(_celdaID / (ancho * 2 - 1));
		final int _loc6 = _celdaID - (_loc5 * (ancho * 2 - 1));
		final int _loc7 = _loc6 % ancho;
		_coordY = (byte) (_loc5 - _loc7);
		// es en plano inclinado, solo Y es negativo partiendo del 0 arriba negativo, abajo positivo
		_coordX = (byte) ((_celdaID - (ancho - 1) * _coordY) / ancho);
		if (objID == -1) {
			_objetoInterac = null;
		} else {
			_objetoInterac = new ObjetoInteractivo(mapa, this, objID);
			Mundo.addObjInteractivo(_objetoInterac);
		}
		int tempD = (int) (((_coordX + _coordY) - 1) * 13.5f);
		int tempL = (_level - 7) * 20;
		_esCaminableLevel = (tempD - tempL) >= 0;
	}
	
	public long getUltimoUsoTrigger() {
		return _ultimoUsoTrigger;
	}
	
	public void celdaNornmal() {
		_personajes = new CopyOnWriteArrayList<Personaje>();
		_acciones = new TreeMap<Integer, Accion>();
	}
	
	public Mapa getMapa() {
		return _mapa;
	}
	
	public void celdaPelea() {
		_luchadores = new ArrayList<>();
	}
	
	public byte getEstado() {
		return _estadoCelda;
	}
	
	public byte getCoordX() {
		return _coordX;
	}
	
	public byte getCoordY() {
		return _coordY;
	}
	
	public boolean getActivo() {
		return _activo;
	}
	
	public byte getMovimiento() {
		return _movimiento;
	}
	
	public byte getLevel() {
		return _level;
	}
	
	public byte getSlope() {
		return _slope;
	}
	
	public float getAlto() {
		float a = _slope == 1 ? 0 : 0.5f;
		int b = _level - 7;
		return a + b;
	}
	
	public ObjetoInteractivo getObjetoInteractivo() {
		return _objetoInterac;
	}
	
	// private int getTipoObjInterac() {
	// if (_objetoInterac == null) {
	// return -1;
	// }
	// return _objetoInterac.getTipoObjInteractivo();
	// }
	public Objeto getObjetoTirado() {
		return _objetoTirado;
	}
	
	public void setObjetoTirado(final Objeto obj) {
		_objetoTirado = obj;
	}
	
	public short getID() {
		return _celdaID;
	}
	
	public void aplicarAccion(final Personaje perso) {
		if (_acciones == null || _acciones.isEmpty()) {
			return;
		}
		for (final Accion accion : _acciones.values()) {
			if (!Condiciones.validaCondiciones(perso, accion.getCondicion())) {
				GestorSalida.ENVIAR_Im_INFORMACION(perso, "119|45");
				return;
			}
		}
		boolean tieneCondicion = false;
		for (final Accion accion : _acciones.values()) {
			if (!accion.getCondicion().isEmpty()) {
				tieneCondicion = true;
			}
			accion.realizarAccion(perso, null, -1, (short) -1);
		}
		if (tieneCondicion) {
			_ultimoUsoTrigger = System.currentTimeMillis();
		}
	}
	
	public void addAccion(final int idAccion, final String args, String condicion) {
		if (_acciones == null) {
			return;
		}
		Accion accion = new Accion(idAccion, args, condicion);
		_acciones.put(idAccion, accion);
	}
	
	public void eliminarAcciones() {
		if (_acciones == null) {
			return;
		}
		_acciones.clear();
	}
	
	public boolean accionesIsEmpty() {
		if (_acciones == null) {
			return true;
		}
		return _acciones.isEmpty();
	}
	
	public Map<Integer, Accion> getAcciones() {
		return _acciones;
	}
	
	public boolean librerParaMercante() {
		if (_personajes == null) {
			return false;
		}
		if (_mapa.mercantesEnCelda(_celdaID) > 0) {
			return false;
		}
		return !(_personajes.size() > 1);
	}
	
	public Personaje getPrimerPersonaje() {
		if (_personajes == null) {
			return null;
		}
		if (_personajes.isEmpty()) {
			return null;
		}
		try {
			return _personajes.get(0);
		} catch (Exception e) {
			return getPrimerPersonaje();
		}
	}
	
	public void addPersonaje(final Personaje perso, boolean aMapa) {
		if (_personajes == null) {
			return;
		}
		if (!_personajes.contains(perso)) {
			_personajes.add(perso);
		}
		if (aMapa) {
			_mapa.addPersonaje(perso);
		}
	}
	
	public void removerPersonaje(final Personaje perso, boolean aMapa) {
		if (_personajes == null) {
			return;
		}
		_personajes.remove(perso);
		if (aMapa) {
			_mapa.removerPersonaje(perso);
		}
	}
	
	public boolean esCaminable(final boolean pelea) {
		if (!_activo || _movimiento == 0 || _movimiento == 1) {
			return false;
		}
		return _esCaminableLevel;
	}
	
	ArrayList<Luchador> getLuchadores() {
		return _luchadores;
	}
	
	public boolean lineaDeVista() {
		return _lineaDeVista;
	}
	
	// public boolean lineaDeVistaLibre(int idLuch) {
	// if (!_activo) {
	// return false;
	// }
	// if (_luchadores == null || _luchadores.isEmpty() || !_lineaDeVista) {
	// return _lineaDeVista;
	// }
	// for (final Luchador luch : _luchadores) {
	// if (luch.getID() == idLuch) {
	// continue;
	// }
	// if (!luch.esInvisible(0)) {
	// return false;
	// }
	// }
	// return _lineaDeVista;
	// }
	public boolean tieneSprite(int idLuch, boolean suponiendo) {
		if (_luchadores == null || _luchadores.isEmpty()) {
			return false;
		}
		for (final Luchador luch : _luchadores) {
			if (luch.getID() == idLuch && suponiendo) {
				continue;
			}
			if (!luch.esInvisible(idLuch)) {
				return true;
			}
		}
		return false;
	}
	
	public void moverLuchadoresACelda(final Celda celdaNew) {
		if (_luchadores == null || celdaNew.getID() == _celdaID) {
			return;
		}
		for (final Luchador luch : _luchadores) {
			celdaNew.addLuchador(luch);
			luch.setCeldaPelea(celdaNew);
		}
		// limpia al final a los luchadores
		_luchadores.clear();
	}
	
	public void addLuchador(final Luchador luchador) {
		if (_luchadores == null) {
			return;
		}
		if (!_luchadores.contains(luchador)) {
			_luchadores.add(luchador);
			_luchadores.trimToSize();
		}
	}
	
	public void removerLuchador(final Luchador luchador) {
		if (_luchadores == null) {
			return;
		}
		_luchadores.remove(luchador);
	}
	
	public void limpiarLuchadores() {
		if (_luchadores == null) {
			return;
		}
		_luchadores.clear();
	}
	
	public Luchador getPrimerLuchador() {
		if (_luchadores == null) {
			return null;
		}
		if (_luchadores.isEmpty()) {
			return null;
		}
		return _luchadores.get(0);
	}
	
	public void addGlifo(Glifo glifo) {
		if (_glifos == null) {
			_glifos = new CopyOnWriteArrayList<>();
		}
		if (!_glifos.contains(glifo)) {
			_glifos.add(glifo);
		}
	}
	
	public boolean borrarGlifo(Glifo glifo) {
		if (_glifos == null) {
			return false;
		}
		return _glifos.remove(glifo);
	}
	
	public boolean tieneGlifo() {
		if (_glifos == null) {
			return false;
		}
		return !_glifos.isEmpty();
	}
	
	public CopyOnWriteArrayList<Glifo> getGlifos() {
		return _glifos;
	}
	
	public boolean esGlifo() {
		if (_glifos == null) {
			return false;
		}
		for (Glifo glifo : _glifos) {
			if (glifo.getCelda().getID() == _celdaID) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public void addTrampa(Trampa trampa) {
		if (_trampas == null) {
			_trampas = new CopyOnWriteArrayList<>();
		}
		if (!_trampas.contains(trampa)) {
			_trampas.add(trampa);
			@SuppressWarnings("rawtypes")
			List arrayList = Arrays.asList(_trampas.toArray());
			Collections.sort(arrayList);
			_trampas.clear();
			_trampas.addAll(arrayList);
		}
	}
	
	public boolean borrarTrampa(Trampa trampa) {
		if (_trampas == null) {
			return false;
		}
		return _trampas.remove(trampa);
	}
	
	public boolean tieneTrampa() {
		if (_trampas == null) {
			return false;
		}
		return !_trampas.isEmpty();
	}
	
	public CopyOnWriteArrayList<Trampa> getTrampas() {
		return _trampas;
	}
	
	public boolean esTrampa() {
		if (_trampas == null) {
			return false;
		}
		for (Trampa trampa : _trampas) {
			if (trampa.getCelda().getID() == _celdaID) {
				return true;
			}
		}
		return false;
	}
	
	public synchronized void activarCelda(final boolean conGDF, final long milisegundos) {
		if (_estadoCelda != Constantes.CI_ESTADO_LLENO) {
			return;
		}
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				_movimiento = 4;// caminable
				_conGDF = conGDF;
				_estadoCelda = Constantes.CI_ESTADO_VACIANDO;
				boolean[] permisos = new boolean[16];
				int[] valores = new int[16];
				permisos[11] = true;
				valores[11] = _movimiento;
				GestorSalida.ENVIAR_GDC_ACTUALIZAR_CELDA_MAPA(_mapa, _celdaID, Encriptador.stringParaGDC(permisos, valores),
				false);
				if (_conGDF) {
					GestorSalida.ENVIAR_GDF_ESTADO_OBJETO_INTERACTIVO(_mapa, _mapa.getCelda(_celdaID));
					try {
						Thread.sleep(2000);
					} catch (final Exception e) {}
				}
				if (milisegundos > 0) {
					_estadoCelda = Constantes.CI_ESTADO_VACIO;
					try {
						Thread.sleep(milisegundos);// hace de timer;
					} catch (final Exception e) {}
					_movimiento = _movimientoInicial;
					_estadoCelda = Constantes.CI_ESTADO_LLENANDO;
					if (_conGDF) {
						GestorSalida.ENVIAR_GDF_ESTADO_OBJETO_INTERACTIVO(_mapa, _mapa.getCelda(_celdaID));
						try {
							Thread.sleep(2000);
						} catch (final Exception e) {}
					}
					_estadoCelda = Constantes.CI_ESTADO_LLENO;
					valores[11] = _movimiento;
					GestorSalida.ENVIAR_GDC_ACTUALIZAR_CELDA_MAPA(_mapa, _celdaID, Encriptador.stringParaGDC(permisos, valores),
					false);
				} else {
					_movimiento = _movimientoInicial;
					_estadoCelda = Constantes.CI_ESTADO_LLENO;
					if (_conGDF) {
						GestorSalida.ENVIAR_GDF_ESTADO_OBJETO_INTERACTIVO(_mapa, _mapa.getCelda(_celdaID));
					}
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}
	
	public boolean puedeHacerAccion(final int skillID, final boolean pescarKuakua) {
		if (_objetoInterac == null) {
			return false;
		}
		if (_objetoInterac.getObjIntModelo().tieneSkill(skillID)) {
			if (skillID == Constantes.SKILL_PESCAR_KUAKUA) {
				return pescarKuakua;
			} else if (_objetoInterac.getObjIntModelo().getTipo() == 1) {
				// trigo, cereal, flores
				return _objetoInterac.getEstado() == Constantes.OI_ESTADO_LLENO;
			} else {
				return true;
			}
		}
		MainServidor.redactarLogServidorln("Bug al verificar si se puede realizar el skill ID = " + skillID);
		return false;
	}
	
	public boolean puedeIniciarAccion(final Personaje perso, final AccionDeJuego AJ) {
		try {
			if (perso.getPelea() != null) {
				return false;
			}
			if (AJ == null) {
				return false;
			}
			short celdaID = -1;
			int skillID = -1;
			try {
				celdaID = Short.parseShort(AJ.getPathPacket().split(";")[0]);
				skillID = Integer.parseInt(AJ.getPathPacket().split(";")[1]);
			} catch (final Exception e) {
				return false;
			}
			if (Constantes.esTrabajo(skillID)) {
				boolean resultado = perso.puedeIniciarTrabajo(skillID, _objetoInterac, AJ.getIDUnica(), this);
				return resultado;
			} else {
				final Casa casa1;
				switch (skillID) {
					case Constantes.SKILL_PELAR_PATATAS :
						break;
					case Constantes.SKILL_GUARDAR_POSICION :// zaap , punto salvada
						perso.setPuntoSalvada(_mapaID + "," + _celdaID);
						GestorSalida.ENVIAR_Im_INFORMACION(perso, "06");
						break;
					case Constantes.SKILL_REGENERARSE :// fuente de rejuvenecimiento
						perso.fullPDV();
						GestorSalida.ENVIAR_Ak_KAMAS_PDV_EXP_PJ(perso);
						break;
					case Constantes.SKILL_UTILIZAR_ZAAP :// zaap
						perso.abrirMenuZaap();
						break;
					case 152 :// kua kua
						perso.setPescarKuakua(false);
					case Constantes.SKILL_SACAR_AGUA :// pozo de agua
					case Constantes.SKILL_JUGAR_MAQUINA_FUERZA :// jugar maquina de fuerza (feria trool)
						if (!_objetoInterac.puedeIniciarRecolecta()) {
							break;
						}
						_objetoInterac.iniciarRecolecta(_objetoInterac.getDuracion());
						GestorSalida.ENVIAR_GA_ACCION_JUEGO_AL_MAPA(perso.getMapa(), AJ.getIDUnica(), 501, perso.getID() + "",
						_celdaID + "," + _objetoInterac.getDuracion() + "," + _objetoInterac.getAnimacionPJ());
						return true;
					case 157 :// zaapi
						perso.abrirMenuZaapi();
						break;
					case 175 :// cercado
						perso.abrirCercado();
						break;
					case 176 :// comprar cercado
						final Cercado cercado = perso.getMapa().getCercado();
						if (cercado.esPublico()) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "196");
							break;
						}
						if (cercado.getPrecio() <= 0) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "197");
							break;
						}
						if (perso.getGremio() == null) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1135");
							break;
						}
						// if (perso.getMiembroGremio().getRango() != 1) {
						// GestorSalida.ENVIAR_Im_INFORMACION(perso, "198");
						// break;
						// }
						GestorSalida.ENVIAR_RD_COMPRAR_CERCADO(perso, cercado.getPrecio() + "|" + cercado.getPrecio());
						break;
					case 177 :// comprar cercado
					case 178 :
						final Cercado cercado1 = perso.getMapa().getCercado();
						if (cercado1.esPublico()) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "194");
							break;
						}
						if (cercado1.getDueñoID() != perso.getID()) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "195");
							break;
						}
						GestorSalida.ENVIAR_RD_COMPRAR_CERCADO(perso, cercado1.getPrecio() + "|" + cercado1.getPrecio());
						break;
					case Constantes.SKILL_ACCIONAR_PALANCA : // palanca 179
						perso.realizarOtroInteractivo(this, _objetoInterac);
						// System.out.println("ENVIO LA ACCION 2");
						break;
					case 183 :// estatua ir a incarnam
						if (perso.getNivel() > 15) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "1127");
						} else {
							GestorSalida.ENVIAR_GA2_CINEMATIC(perso, "5");
							final String[] mapa = Constantes.getMapaInicioIncarnam(perso.getClaseID(true)).split(",");
							perso.teleport(Short.parseShort(mapa[0]), Short.parseShort(mapa[1]));
						}
						break;
					case 81 :// poner el cerrojo
						casa1 = Mundo.getCasaPorUbicacion(_mapaID, celdaID);
						if (casa1 == null) {
							break;
						}
						casa1.ponerClave(perso, true);
						break;
					case 100 :// quitar el cerrojo
						casa1 = Mundo.getCasaPorUbicacion(_mapaID, celdaID);
						if (casa1 == null) {
							break;
						}
						// perso.setConsultarCasa(casa1);
						casa1.quitarCerrojo(perso);
						break;
					case 84 :// entrar
						casa1 = Mundo.getCasaPorUbicacion(_mapaID, celdaID);
						if (casa1 == null) {
							break;
						}
						casa1.intentarAcceder(perso, "");
						break;
					case 97 :// comprar
					case 98 :// vender
					case 108 :// modificar el precio de venta
						casa1 = Mundo.getCasaPorUbicacion(_mapaID, celdaID);
						if (casa1 == null) {
							break;
						}
						casa1.abrirVentanaCompraVentaCasa(perso);
						break;
					case 104 :// abrir cofre
						if (_mapaID == 7442) {
							GestorSalida.ENVIAR_BN_NADA(perso);
							break;
						}
						Cofre cofre2 = Mundo.getCofrePorUbicacion(_mapaID, celdaID);
						if (cofre2 == null) {
							cofre2 = Cofre.insertarCofre(_mapaID, _celdaID);
						}
						if (cofre2 == null) {
							break;
						}
						cofre2.intentarAcceder(perso, "");
						break;
					case 105 :// poner el cerrojo cofre
						if (_mapaID == 7442) {
							GestorSalida.ENVIAR_BN_NADA(perso);
							break;
						}
						Cofre cofre = Mundo.getCofrePorUbicacion(_mapaID, celdaID);
						if (cofre == null) {
							cofre = Cofre.insertarCofre(_mapaID, _celdaID);
						}
						if (cofre == null) {
							break;
						}
						cofre.ponerClave(perso, true);
						break;
					case 153 :// basura
						final Cofre basura = Mundo.getCofrePorUbicacion((short) 0, (short) 0);
						if (basura == null) {
							break;
						}
						basura.intentarAcceder(perso, "");
						break;
					case 170 :// libro artesanos
						GestorSalida.ENVIAR_ECK_PANEL_DE_INTERCAMBIOS(perso, Constantes.INTERCAMBIO_TIPO_LIBRO_ARTESANOS,
						Constantes.SKILLS_LIBRO_ARTESANOS);
						perso.setTipoExchange(Constantes.INTERCAMBIO_TIPO_LIBRO_ARTESANOS);
						break;
					case 181 :// romper objetos
					case 121 :// machacar recursos
						if (!perso.estaDisponible(false, true)) {
							GestorSalida.ENVIAR_BN_NADA(perso);
							break;
						}
						break;
					default :
						MainServidor.redactarLogServidorln("Bug al iniciar la skill ID = " + skillID);
						break;
				}
			}
		} catch (final Exception e) {
			String error = "EXCEPTION iniciarAccion AJ.getPacket(): " + AJ.getPathPacket() + " e: " + e.toString();
			GestorSalida.ENVIAR_BN_NADA(perso, error);
			MainServidor.redactarLogServidorln(error);
		}
		return false;
	}
	
	public boolean finalizarAccion(final Personaje perso, final AccionDeJuego AJ) {
		try {
			if (AJ == null) {
				return false;
			}
			int accionID = -1;
			try {
				accionID = Integer.parseInt(AJ.getPathPacket().split(";")[1]);
			} catch (final Exception e) {
				return false;
			}
			if (Constantes.esTrabajo(accionID)) {// es de oficio
				return perso.finalizarTrabajo(accionID);
			} else {
				switch (accionID) {
					case Constantes.SKILL_PELAR_PATATAS :
					case Constantes.SKILL_GUARDAR_POSICION :
					case 81 :
					case 84 :
					case 97 :
					case 98 :
					case 104 :// chekear cofre
					case 105 :// poner cerrojo cofre
					case 108 :
					case 114 :
					case Constantes.SKILL_MACHACAR_RECURSOS :// machacar recursos
					case 157 :
					case 170 :
					case 175 :
					case 176 :
					case 177 :
					case 178 :
					case 181 :// romper objetos
					case 183 :// ir a incarnam
					case 153 :// basura
					case Constantes.SKILL_ACCIONAR_PALANCA : // palanca
						return true;
					case Constantes.SKILL_JUGAR_MAQUINA_FUERZA :// maquina de fuerza
					case Constantes.SKILL_SACAR_AGUA :// pozo de agua
					case Constantes.SKILL_PESCAR_KUAKUA :// kua kua
						if (!_objetoInterac.puedeFinalizarRecolecta()) {
							return false;
						}
						_objetoInterac.activandoRecarga(Constantes.OI_ESTADO_VACIANDO, Constantes.OI_ESTADO_VACIO);
						switch (accionID) {
							case Constantes.SKILL_SACAR_AGUA :
								int cantidad = Formulas.getRandomInt(1, 10);
								perso.addObjIdentAInventario(Mundo.getObjetoModelo(311).crearObjeto(cantidad,
								Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM), false);
								GestorSalida.ENVIAR_IQ_NUMERO_ARRIBA_PJ(perso, perso.getID(), cantidad);
								GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(perso);
								break;
							case Constantes.SKILL_PESCAR_KUAKUA :
								final int x = Formulas.getRandomInt(0, 5);
								if (x == 5) {
									GestorSalida.ENVIAR_cS_EMOTICON_MAPA(perso.getMapa(), perso.getID(), 11);
									perso.addObjIdentAInventario(Mundo.getObjetoModelo(6659).crearObjeto(1,
									Constantes.OBJETO_POS_NO_EQUIPADO, CAPACIDAD_STATS.RANDOM), false);
									GestorSalida.ENVIAR_IQ_NUMERO_ARRIBA_PJ(perso, perso.getID(), 1);
								} else {
									GestorSalida.ENVIAR_Im_INFORMACION(perso, "1TRY_OTHER");
									GestorSalida.ENVIAR_cS_EMOTICON_MAPA(perso.getMapa(), perso.getID(), 12);
								}
								GestorSalida.ENVIAR_Ow_PODS_DEL_PJ(perso);
								break;
							case Constantes.SKILL_JUGAR_MAQUINA_FUERZA :
								break;
						}
						break;
					default :
						MainServidor.redactarLogServidorln("Bug al finalizar la accion ID = " + accionID);
						break;
				}
			}
		} catch (final Exception e) {}
		return true;
	}
	// public void destruir() {
	// try {
	// this.finalize();
	// } catch (Throwable e) {
	// Bustemu.escribirLog("Throwable destruir celda " + e.toString());
	// e.printStackTrace();
	// }
	// }
}