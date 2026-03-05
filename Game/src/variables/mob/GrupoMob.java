package variables.mob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import variables.mapa.Celda;
import variables.mapa.Mapa;
import variables.mob.AparecerMobs.Aparecer;
import variables.mob.MobModelo.TipoGrupo;
import variables.objeto.Objeto;
import variables.pelea.Pelea;
import estaticos.Camino;
import estaticos.Constantes;
import estaticos.MainServidor;
import estaticos.Encriptador;
import estaticos.Formulas;
import estaticos.GestorSQL;
import estaticos.GestorSalida;
import estaticos.Mundo;
import estaticos.Mundo.Duo;

public class GrupoMob {
	private final TipoGrupo _tipoGrupo;
	private boolean _muerto, _fijo;
	private byte _orientacion = 3, _alineacion = Constantes.ALINEACION_NULL, _distanciaAgresion;
	private short _celdaID;
	private int _bonusEstrellas = MainServidor.INICIO_BONUS_ESTRELLAS_MOBS;
	private int _grupoID, _segundosRespawn;
	private final ArrayList<MobGradoModelo> _mobsGradoModelo = new ArrayList<MobGradoModelo>();
	private final HashMap<Integer, Integer> _almas = new HashMap<Integer, Integer>();
	private String _condInicioPelea = "", _strGrupoMob = "", _condUnirsePelea = "";
	private Timer _timer;
	private Pelea _pelea;
	private final ArrayList<Integer> _objetosHeroico = new ArrayList<Integer>();
	private long _kamasHeroico;
	private ArrayList<Mapa> _mapasRandom;
	
	public GrupoMob(final ArrayList<MobPosible> posiblesMobs, final Mapa mapa, final short celdaID,
	final int maxMobsPorGrupo) {
		_tipoGrupo = TipoGrupo.NORMAL;
		if (posiblesMobs == null || posiblesMobs.isEmpty()) {
			return;
		}
		if (maxMobsPorGrupo < MainServidor.MIN_CANTIDAD_MOBS_EN_GRUPO) {
			return;
		}
		int nroMobs = Formulas.getRandomInt(MainServidor.MIN_CANTIDAD_MOBS_EN_GRUPO, maxMobsPorGrupo);
		// if (nroMobs > 8) {
		// nroMobs = 8;
		// }
		_celdaID = celdaID == -1 ? mapa.getRandomCeldaIDLibre() : celdaID;
		if (_celdaID == 0) {
			return;
		}
		int maxNivel = 0;
		boolean archi = false;
		StringBuilder str = new StringBuilder();
		ArrayList<MobGradoModelo> mobsEscogidos = new ArrayList<>();
		ArrayList<MobPosible> tempPosibles = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			tempPosibles.clear();
			tempPosibles.addAll(posiblesMobs);
			int nivelTotal = 0;
			while (mobsEscogidos.size() < nroMobs && !tempPosibles.isEmpty()) {
				MobPosible mp = tempPosibles.get(Formulas.getRandomInt(0, tempPosibles.size() - 1));
				if (mp.getCantMax() > 0) {
					int cantidad = 0;
					for (MobGradoModelo m : mobsEscogidos) {
						if (mp.tieneMob(m)) {
							cantidad++;
						}
					}
					if (cantidad >= mp.getCantMax()) {
						tempPosibles.remove(mp);
						continue;
					}
				}
				if (!mp.pasoProbabilidad()) {
					continue;
				}
				MobGradoModelo mob = mp.getRandomMob();
				nivelTotal += mob.getNivel();
				mobsEscogidos.add(mob);
			}
			if (mapa.esNivelGrupoMobPermitido(nivelTotal)) {
				break;
			}
		}
		for (MobGradoModelo mg : mobsEscogidos) {
			MobGradoModelo mobGrado = mg;
			int idMobModelo = mobGrado.getIDModelo();
			if (!archi && !mapa.esMazmorra()) {
				MobModelo archiMob = mobGrado.getMobModelo().getArchiMob();
				if (archiMob != null) {
					if (archiMob.puedeSubArea(mapa.getSubArea().getID())) {
						int prob = archiMob.getProbabilidadAparecer();
						if (prob == -1 || prob >= 100 || prob >= Formulas.getRandomInt(1, 100)) {
							mobGrado = archiMob.getGradoPorGrado(mg.getGrado());
							archi = true;
						}
					}
				}
			}
			if (Mundo.MOB_EVENTO > 0) {
				final ArrayList<Duo<Integer, Integer>> array = Mundo.getMobsEventoDelDia();
				if (array != null && !array.isEmpty()) {
					for (final Duo<Integer, Integer> duo : array) {
						if (duo._primero == idMobModelo) {
							try {
								mobGrado = Mundo.getMobModelo(duo._segundo).getRandomGrado();
							} catch (final Exception e) {}
						}
					}
				}
			}
			if (mobGrado.getNivel() > maxNivel) {
				maxNivel = mobGrado.getNivel();
			}
			if (_almas.containsKey(idMobModelo)) {
				final int valor = _almas.get(idMobModelo);
				_almas.remove(idMobModelo);
				_almas.put(idMobModelo, valor + 1);
			} else {
				_almas.put(idMobModelo, 1);
			}
			if (_alineacion == Constantes.ALINEACION_NULL) {
				_alineacion = mobGrado.getMobModelo().getAlineacion();
			}
			if (_distanciaAgresion < mobGrado.getMobModelo().getDistAgresion()) {
				_distanciaAgresion = mobGrado.getMobModelo().getDistAgresion();
			}
			if (str.length() > 0) {
				str.append(";");
			}
			str.append(mobGrado.getIDModelo() + "," + mobGrado.getNivel() + "," + mobGrado.getNivel());
			_mobsGradoModelo.add(mobGrado);
		}
		if (_mobsGradoModelo.isEmpty()) {
			return;
		}
		_grupoID = mapa.sigIDGrupoMob();
		if (_distanciaAgresion == 0) {
			_distanciaAgresion = Constantes.distAgresionPorNivel(maxNivel);
		}
		if (_alineacion == Constantes.ALINEACION_BONTARIANO || _alineacion == Constantes.ALINEACION_BRAKMARIANO) {
			_distanciaAgresion = 10;
		} else if (_alineacion == Constantes.ALINEACION_NEUTRAL) {
			_distanciaAgresion = 30;
		}
		_orientacion = (byte) (Formulas.getRandomInt(0, 3) * 2 + 1);
		_strGrupoMob = str.toString();
	}
	
	public GrupoMob(Mapa mapa, final short celdaID, final String strGrupoMob, TipoGrupo tipo, String condiciones) {
		_celdaID = celdaID == -1 ? mapa.getRandomCeldaIDLibre() : celdaID;
		_tipoGrupo = tipo;
		if (_celdaID == 0) {
			return;
		}
		_fijo = true;
		_strGrupoMob = strGrupoMob;
		int maxNivel = 0;
		final List<Byte> grados = new ArrayList<Byte>();
		for (final String data : _strGrupoMob.split(";")) {
			try {
				final String[] infos = data.split(",");
				int idMobModelo = Integer.parseInt(infos[0]);
				final MobModelo mobModelo = Mundo.getMobModelo(idMobModelo);
				int min = 0;
				int max = 0;
				try {
					min = Integer.parseInt(infos[1]);
				} catch (Exception e) {}
				try {
					max = Integer.parseInt(infos[2]);
				} catch (Exception e) {}
				grados.clear();
				for (final MobGradoModelo mob : mobModelo.getGrados().values()) {
					if (mob.getNivel() >= min && mob.getNivel() <= max) {
						grados.add(mob.getGrado());
					}
				}
				MobGradoModelo mob;
				if (grados.isEmpty()) {
					mob = mobModelo.getRandomGrado();
				} else {
					byte grado = grados.get(Formulas.getRandomInt(0, grados.size() - 1));
					mob = mobModelo.getGradoPorGrado(grado);
				}
				if (mob.getNivel() > maxNivel) {
					maxNivel = mob.getNivel();
				}
				if (_almas.containsKey(idMobModelo)) {
					final int valor = _almas.get(idMobModelo);
					_almas.remove(idMobModelo);
					_almas.put(idMobModelo, valor + 1);
				} else {
					_almas.put(idMobModelo, 1);
				}
				if (_alineacion == Constantes.ALINEACION_NULL) {
					_alineacion = mobModelo.getAlineacion();
				}
				if (_distanciaAgresion < mobModelo.getDistAgresion()) {
					_distanciaAgresion = mobModelo.getDistAgresion();
				}
				_mobsGradoModelo.add(mob);
			} catch (final Exception e) {}
		}
		if (_mobsGradoModelo.isEmpty()) {
			return;
		}
		_grupoID = mapa.sigIDGrupoMob();
		_condInicioPelea = condiciones;
		_condUnirsePelea = condiciones;
		if (_distanciaAgresion == 0) {
			_distanciaAgresion = Constantes.distAgresionPorNivel(maxNivel);
		}
		if (_alineacion == Constantes.ALINEACION_BONTARIANO || _alineacion == Constantes.ALINEACION_BRAKMARIANO) {
			_distanciaAgresion = 10;
		} else if (_alineacion == Constantes.ALINEACION_NEUTRAL) {
			_distanciaAgresion = 30;
		}
		_orientacion = (byte) (Formulas.getRandomInt(0, 3) * 2 + 1);
	}
	
	public boolean getFijo() {
		return _fijo;
	}
	
	public void setMapasRandom(ArrayList<Mapa> mapas) {
		_mapasRandom = mapas;
	}
	
	public ArrayList<Mapa> getMapasRandom() {
		return _mapasRandom;
	}
	
	public void moverGrupoMob(final Mapa mapa) {
		if (_pelea != null) {
			return;
		}
		final short celdaDestino = Camino.celdaMoverSprite(mapa, _celdaID);
		if (celdaDestino == -1) {
			return;
		}
		final Duo<Integer, ArrayList<Celda>> pathCeldas = Camino.getPathPelea(mapa, _celdaID, celdaDestino, -1, null,
		false);
		if (pathCeldas == null) {
			return;
		}
		final ArrayList<Celda> celdas = pathCeldas._segundo;
		String pathStr = Camino.getPathComoString(mapa, celdas, _celdaID, false);
		if (pathStr.isEmpty()) {
			MainServidor.redactarLogServidorln("Fallo de desplazamiento de mob grupo: camino vacio - MapaID: " + mapa.getID()
			+ " - CeldaID: " + _celdaID);
			return;
		}
		try {
			Thread.sleep(100);
		} catch (final Exception e) {}
		GestorSalida.ENVIAR_GA_MOVER_SPRITE_MAPA(mapa, 0, 1, _grupoID + "", Encriptador.getValorHashPorNumero(_orientacion)
		+ Encriptador.celdaIDAHash(_celdaID) + pathStr);
		_orientacion = Camino.getIndexPorDireccion(pathStr.charAt(pathStr.length() - 3));
		_celdaID = celdaDestino;
	}
	
	public boolean tieneMobModeloID(int id, int lvlMin, int lvlMax) {
		for (MobGradoModelo m : _mobsGradoModelo) {
			if (m.getIDModelo() == id) {
				if (m.getNivel() >= lvlMin && m.getNivel() <= lvlMax) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void addObjetosKamasInicioServer(String heroico) {
		String[] infos = heroico.split(Pattern.quote("|"));
		if (infos.length > 1 && !infos[1].isEmpty()) {
			for (String s : infos[1].split(",")) {
				try {
					if (s.isEmpty())
						continue;
					addIDObjeto(Integer.parseInt(s));
				} catch (Exception e) {}
			}
		}
		if (infos.length > 2 && !infos[2].isEmpty()) {
			long kamas = 0;
			try {
				kamas = Long.parseLong(infos[1]);
			} catch (Exception e) {}
			addKamasHeroico(kamas);
		}
	}
	
	public void addKamasHeroico(final long kamas) {
		if (kamas < 1) {
			return;
		}
		_kamasHeroico += kamas;
		Math.max(0, _kamasHeroico);
	}
	
	public void setKamasHeroico(final long kamas) {
		_kamasHeroico = kamas;
	}
	
	public long getKamasHeroico() {
		return _kamasHeroico;
	}
	
	public boolean esHeroico() {
		return _kamasHeroico > 0 || !_objetosHeroico.isEmpty();
	}
	
	private void addIDObjeto(int id) {
		if (!_objetosHeroico.contains(id))
			_objetosHeroico.add(id);
	}
	
	public String getIDsObjeto() {
		StringBuilder str = new StringBuilder();
		for (int i : _objetosHeroico) {
			if (str.length() > 0) {
				str.append(",");
			}
			str.append(i);
		}
		return str.toString();
	}
	
	public int cantObjHeroico() {
		return _objetosHeroico.size();
	}
	
	public ArrayList<Integer> getObjetosHeroico() {
		return _objetosHeroico;
	}
	
	public void borrarObjetosHeroico() {
		_objetosHeroico.clear();
	}
	
	public boolean addObjAInventario(final Objeto objeto) {
		if (_objetosHeroico.contains(objeto.getID())) {
			return false;
		}
		// tipo piedra de alma y mascota
		if (objeto.puedeTenerStatsIguales()) {
			for (int id : _objetosHeroico) {
				final Objeto obj = Mundo.getObjeto(id);
				if (obj == null)
					continue;
				if (Constantes.esPosicionEquipamiento(obj.getPosicion())) {
					continue;
				}
				if (objeto.getID() != obj.getID() && obj.getObjModeloID() == objeto.getObjModeloID() && obj.sonStatsIguales(
				objeto)) {
					obj.setCantidad(obj.getCantidad() + objeto.getCantidad());
					if (objeto.getID() > 0) {
						Mundo.eliminarObjeto(objeto.getID());
					}
					return true;
				}
			}
		}
		if (objeto.getID() == 0) {
			Mundo.addObjeto(objeto, false);
		} else {
			GestorSQL.SALVAR_OBJETO(objeto);
		}
		addIDObjeto(objeto.getID());
		return false;
	}
	
	public boolean puedeTimerReaparecer(Mapa mapa, GrupoMob grupoMob, Aparecer i) {
		if (_tipoGrupo == TipoGrupo.SOLO_UNA_PELEA) {
			return false;
		}
		switch (i) {
			case INICIO_PELEA :
				mapa.addSiguienteGrupoMob(grupoMob, true);
				break;
			case FINAL_PELEA :
				mapa.addUltimoGrupoMob(grupoMob, true);
				break;
		}
		return true;
	}
	
	public int getID() {
		return _grupoID;
	}
	
	public String getStrGrupoMob() {
		return _strGrupoMob;
	}
	
	public boolean enPelea() {
		return _pelea != null;
	}
	
	public TipoGrupo getTipo() {
		return _tipoGrupo;
	}
	
	public boolean estaMuerto() {
		return _muerto;
	}
	
	public void setMuerto(final boolean muerto) {
		_muerto = muerto;
	}
	
	public int getBonusEstrellas() {
		setBonusEstrellas(_bonusEstrellas);
		return Math.max(0, _bonusEstrellas);
	}
	
	public int realBonusEstrellas() {
		return _bonusEstrellas;
	}
	
	public void subirBonusEstrellas(int cant) {
		setBonusEstrellas(_bonusEstrellas + cant);
	}
	
	public void setBonusEstrellas(final int estrellas) {
		_bonusEstrellas = estrellas;
		if (_bonusEstrellas < MainServidor.INICIO_BONUS_ESTRELLAS_MOBS) {
			_bonusEstrellas = MainServidor.INICIO_BONUS_ESTRELLAS_MOBS;
		}
		if (_bonusEstrellas > MainServidor.MAX_BONUS_ESTRELLAS_MOBS) {
			if (MainServidor.PARAM_REINICIAR_ESTRELLAS_SI_LLEGA_MAX) {
				_bonusEstrellas = MainServidor.INICIO_BONUS_ESTRELLAS_MOBS;
			} else {
				_bonusEstrellas = MainServidor.MAX_BONUS_ESTRELLAS_MOBS;
			}
		}
	}
	
	public short getCeldaID() {
		return _celdaID;
	}
	
	public void setCeldaID(final short id) {
		_celdaID = id;
	}
	
	public byte getOrientacion() {
		return _orientacion;
	}
	
	public byte getDistAgresion() {
		return _distanciaAgresion;
	}
	
	public void setOrientacion(final byte o) {
		_orientacion = 0;
	}
	
	public byte getAlineacion() {
		return _alineacion;
	}
	
	// public MobGrado getMobGradoPorID(final int id) {
	// return _mobsGradoMod.get(id);
	// }
	public int getCantMobs() {
		return _mobsGradoModelo.size();
	}
	
	public String stringGM() {
		if (_mobsGradoModelo.isEmpty()) {
			return "";
		}
		final StringBuilder mobIDs = new StringBuilder();
		final StringBuilder mobGFX = new StringBuilder();
		final StringBuilder mobNiveles = new StringBuilder();
		final StringBuilder colorAccesorios = new StringBuilder();
		final String forma = Formulas.getRandomBoolean() ? "," : ":";
		long totalExp = 0;
		for (final MobGradoModelo mob : _mobsGradoModelo) {
			if (mobIDs.length() > 0) {
				mobIDs.append(",");
				mobGFX.append(forma);
				mobNiveles.append(",");
			}
			mobIDs.append(mob.getMobModelo().getID());
			mobGFX.append(mob.getMobModelo().getGfxID() + "^" + mob.getMobModelo().getTalla());
			mobNiveles.append(mob.getNivel());
			totalExp += mob.getBaseXp();
		}
		totalExp = (long) (totalExp * ((getBonusEstrellas() / 100f) + MainServidor.RATE_XP_PVM));
		for (final MobGradoModelo mob : _mobsGradoModelo) {
			if (colorAccesorios.length() > 0) {
				colorAccesorios.append(";");
			}
			colorAccesorios.append(mob.getMobModelo().getColores());
			colorAccesorios.append(";");
			// colorAccesorios.append("accesorios");
		}
		StringBuilder s = new StringBuilder();
		s.append(_celdaID + ";" + _orientacion + ";" + getBonusEstrellas() + ";" + _grupoID + ";" + mobIDs.toString()
		+ ";-3;" + mobGFX.toString() + ";" + mobNiveles.toString() + ";");
		if (MainServidor.PARAM_MOSTRAR_EXP_MOBS) {
			s.append(totalExp);
		}
		s.append(";" + colorAccesorios.toString());
		return s.toString();
	}
	
	public void setID(int id) {
		_grupoID = id;
	}
	
	public ArrayList<MobGradoModelo> getMobs() {
		return _mobsGradoModelo;
	}
	
	public Map<Integer, Integer> getAlmasMobs() {
		return _almas;
	}
	
	// public void setCondIniciaPelea(final String cond) {
	// _condInicioPelea = cond;
	// }
	public String getCondInicioPelea() {
		return _condInicioPelea;
	}
	
	public void setCondUnirsePelea(final String cond) {
		_condUnirsePelea = cond;
	}
	
	public String getCondUnirsePelea() {
		return _condUnirsePelea;
	}
	
	public void startTiempoCondicion() {
		_timer = new Timer();
		_timer.schedule(new TimerTask() {
			public void run() {
				_timer.cancel();
				_condInicioPelea = "";
				_condUnirsePelea = "";
			}
		}, MainServidor.SEGUNDOS_ARENA * 1000);
	}
	
	public void setSegundosRespawn(int segundos) {
		_segundosRespawn = segundos;
	}
	
	public int getSegundosRespawn() {
		return _segundosRespawn;
	}
	
	// public void startTimerRespawn(final Mapa mapa) {
	// final GrupoMob g = this;
	// _timer = new Timer();
	// _timer.schedule(new TimerTask() {
	// public void run() {
	// _timer.cancel();
	// mapa.addSigGrupoMobRespawn(g);
	// }
	// }, _segundosRespawn * 1000);
	// }
	public void setPelea(Pelea pelea) {
		_pelea = pelea;
	}
}