package variables.mob;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import variables.hechizo.Hechizo;
import variables.hechizo.StatHechizo;
import variables.pelea.DropMob;
import estaticos.Constantes;
import estaticos.Formulas;
import estaticos.GestorSQL;
import estaticos.Mundo;

public class MobModelo {
	public static enum TipoGrupo {
		FIJO, NORMAL, SOLO_UNA_PELEA, HASTA_QUE_MUERA
	}
	private final byte _alineacion;
	private byte _tipoIA, _distAgresion;
	private final byte _tipoMob;
	private final int _id;
	private MobModelo _archiMob;
	private int _probabilidadAparecer = -1;
	private ArrayList<Integer> _subAreasAparecer = new ArrayList<>();
	private short _talla, _gfxID;
	private String _colores;
	private final String _nombre;
	private final Map<Byte, MobGradoModelo> _grados = new TreeMap<Byte, MobGradoModelo>();
	private final ArrayList<DropMob> _drops = new ArrayList<DropMob>();
	private final boolean _esCapturable;
	private final boolean _esKickeable;
	
	public MobModelo(final int id, final String nombre, final short gfx, final byte alineacion, final String colores,
	final String grados, final String hechizos, final String stats, final String pdvs, final String puntos,
	final String strIniciativa, final String mK, final String MK, final String exps, final byte tipoIA,
	final boolean capturable, final short talla, final byte distAgresion, final byte tipoCriatura,
	final boolean esKickeable) {
		_id = id;
		_gfxID = gfx;
		_alineacion = alineacion;
		_colores = colores;
		_tipoIA = tipoIA;
		_esCapturable = capturable;
		_talla = talla;
		_nombre = nombre;
		_distAgresion = distAgresion;
		_tipoMob = tipoCriatura;
		_esKickeable = esKickeable;
		final String[] aGrados = grados.split(Pattern.quote("|"));
		final String[] aStats = stats.split(Pattern.quote("|"));
		final String[] aHechizos = hechizos.split(Pattern.quote("|"));
		final String[] aPuntos = puntos.split(Pattern.quote("|"));
		final String[] aExp = exps.split(Pattern.quote("|"));
		final String[] aPDV = pdvs.split(Pattern.quote("|"));
		final String[] aIniciativa = strIniciativa.split(Pattern.quote("|"));
		final String[] aMinKamas = mK.split(Pattern.quote("|"));
		final String[] aMaxKamas = MK.split(Pattern.quote("|"));
		byte grado = 1, PA = 6, PM = 3;
		int tempPDV = 1, tempIniciativa = 0, tempExp = 0, tempMinKamas = 0, tempMaxKamas = 0;
		String tempHechizo = "", tempResistNivel = "", tempStats = "";
		for (int n = 0; n < aGrados.length; n++) {
			try {
				tempResistNivel = aGrados[n].split("@")[1];
			} catch (final Exception e) {
				continue;
			}
			if (tempResistNivel.isEmpty()) {
				continue;
			}
			try {
				tempExp = Integer.parseInt(aExp[n]);
			} catch (final Exception e) {}
			try {
				tempStats = aStats[n];
			} catch (final Exception e) {}
			try {
				tempHechizo = aHechizos[n];
			} catch (final Exception e) {}
			try {
				tempPDV = Integer.parseInt(aPDV[n]);
			} catch (final Exception e) {}
			try {
				tempIniciativa = Integer.parseInt(aIniciativa[n]);
			} catch (final Exception e) {}
			try {
				PA = Byte.parseByte(aPuntos[n].split(";")[0]);
			} catch (final Exception e) {}
			try {
				PM = Byte.parseByte(aPuntos[n].split(";")[1]);
			} catch (final Exception e) {}
			try {
				tempMinKamas = Integer.parseInt(aMinKamas[n]);
			} catch (final Exception e) {}
			try {
				tempMaxKamas = Integer.parseInt(aMaxKamas[n]);
			} catch (final Exception e) {}
			_grados.put(grado, new MobGradoModelo(this, grado, PA, PM, tempResistNivel, tempStats, tempHechizo, tempPDV,
			tempIniciativa, tempExp, tempMinKamas, tempMaxKamas));
			grado++;
		}
		if (!stats.isEmpty() && !stats.contains(":")) {
			StringBuilder strStats = new StringBuilder();
			boolean e = false;
			for (byte i = 1; i <= 11; i++) {
				MobGradoModelo mob = _grados.get(i);
				if (mob == null) {
					break;
				}
				if (e) {
					strStats.append("|");
				}
				strStats.append(mob.stringStatsActualizado());
				e = true;
			}
			GestorSQL.UPDATE_STATS_MOB(_id, strStats.toString());
		}
	}
	
	public void setArchiMob(MobModelo archiMob) {
		_archiMob = archiMob;
	}
	
	public void setDataExtra(int probabilidad, String subareas) {
		_probabilidadAparecer = probabilidad;
		for (String s : subareas.split(",")) {
			if (s.isEmpty()) {
				continue;
			}
			try {
				_subAreasAparecer.add(Integer.parseInt(s));
			} catch (Exception e) {}
		}
	}
	
	public MobModelo getArchiMob() {
		return _archiMob;
	}
	
	public int getProbabilidadAparecer() {
		return _probabilidadAparecer;
	}
	
	public boolean puedeSubArea(int subarea) {
		return _subAreasAparecer.isEmpty() || _subAreasAparecer.contains(subarea);
	}
	
	public boolean modificarStats(byte grado, String packet) {
		try {
			MobGradoModelo mob = _grados.get(grado);
			if (mob == null) {
				return false;
			}
			String[] split = packet.split(Pattern.quote("|"));
			String[] stats = split[0].split(",");
			for (int i = 0; i < stats.length; i++) {
				try {
					String[] a = stats[i].split(":");
					mob.getStats().fijarStatID(Integer.parseInt(a[0]), Integer.parseInt(a[1]));
				} catch (Exception e) {}
			}
			mob.setPDVMAX(Integer.parseInt(split[1]));
			mob.setBaseXp(Integer.parseInt(split[2]));
			mob.setMinKamas(Integer.parseInt(split[3]));
			mob.setMaxKamas(Integer.parseInt(split[4]));
			String[] s = strStatsTodosMobs().split("~");
			GestorSQL.UPDATE_STATS_PUNTOS_PDV_XP_MOB(_id, s[0], s[1], s[2], split[3], split[4]);
			return true;
		} catch (Exception e) {}
		return false;
	}
	
	public String strStatsTodosMobs() {
		// se usa mas q todo para el panel mobs, pero minkamas y maxkamas son int para sql
		StringBuilder strStats = new StringBuilder();
		StringBuilder strPDV = new StringBuilder();
		StringBuilder strExp = new StringBuilder();
		StringBuilder strMinKamas = new StringBuilder();
		StringBuilder strMaxKamas = new StringBuilder();
		boolean e = false;
		for (byte i = 1; i <= 11; i++) {
			MobGradoModelo mob = _grados.get(i);
			if (mob == null) {
				break;
			}
			if (e) {
				strStats.append("|");
				strPDV.append("|");
				strExp.append("|");
				strMinKamas.append("|");
				strMaxKamas.append("|");
			}
			strStats.append(mob.stringStatsActualizado());
			strPDV.append(mob.getPDVMAX());
			strExp.append(mob.getBaseXp());
			strMinKamas.append(mob.getMinKamas());
			strMaxKamas.append(mob.getMaxKamas());
			e = true;
		}
		return strStats.toString() + "~" + strPDV.toString() + "~" + strExp.toString() + "~" + strMinKamas.toString() + "~"
		+ strMaxKamas.toString();
	}
	
	// public String testDaño(byte grado, String s) {
	// MobGradoModelo mg = getGradoPorGrado(grado);
	// int[] stats = getStatsParaCalculo(grado, s);
	// if (stats == null) {
	// return "MOB NO EXISTE";
	// }
	// StringBuilder str = new StringBuilder("");
	// for (StatHechizo sh : mg.getHechizos().values()) {
	// if (str.length() > 0) {
	// str.append("|");
	// }
	// str.append(Hechizo.strDañosStats2(sh, stats));
	// }
	// return str.toString();
	// }
	public String calculoDaño(byte grado, String s) {
		MobGradoModelo mg = getGradoPorGrado(grado);
		int[] stats = getStatsParaCalculo(grado, s);
		if (stats == null) {
			return "";
		}
		StringBuilder str = new StringBuilder();
		// str.append("\nCalculo de daño del mob " + _nombre + ":");
		boolean paso = false;
		for (StatHechizo sh : mg.getHechizos().values()) {
			if (paso) {
				str.append("|");
			}
			paso = true;
			str.append(Hechizo.strDañosStats(sh, stats));
		}
		return str.toString();
	}
	
	public int[] getStatsParaCalculo(byte grado, String s) {
		MobGradoModelo mg = getGradoPorGrado(grado);
		if (mg == null) {
			return null;
		}
		int[] stats = new int[5];
		try {
			for (String s2 : s.split(";")) {
				int statID = Integer.parseInt(s2.split(",")[0]);
				int valor = Integer.parseInt(s2.split(",")[1]);
				switch (statID) {
					case Constantes.STAT_MAS_FUERZA :
						stats[0] = stats[1] = valor;
						break;
					case Constantes.STAT_MAS_INTELIGENCIA :
						stats[2] = valor;
						break;
					case Constantes.STAT_MAS_SUERTE :
						stats[3] = valor;
						break;
					case Constantes.STAT_MAS_AGILIDAD :
						stats[4] = valor;
						break;
				}
			}
		} catch (Exception e) {
			stats[0] = (mg.getStats().getStatParaMostrar(Constantes.STAT_MAS_FUERZA));
			stats[1] = (mg.getStats().getStatParaMostrar(Constantes.STAT_MAS_FUERZA));
			stats[2] = (mg.getStats().getStatParaMostrar(Constantes.STAT_MAS_INTELIGENCIA));
			stats[3] = (mg.getStats().getStatParaMostrar(Constantes.STAT_MAS_SUERTE));
			stats[4] = (mg.getStats().getStatParaMostrar(Constantes.STAT_MAS_AGILIDAD));
		}
		return stats;
	}
	
	public String detalleMob() {
		final StringBuilder str = new StringBuilder();
		str.append(_tipoMob + "|");
		StringBuilder str2 = new StringBuilder();
		for (final DropMob drop : _drops) {
			if (str2.length() > 0) {
				str2.append(";");
			}
			str2.append(drop.getIDObjModelo() + "," + drop.getProspeccion() + "#" + (drop.getPorcentaje() * 1000) + "#"
			+ drop.getMaximo());
		}
		str.append(str2.toString() + "|");
		str2 = new StringBuilder();
		for (final MobGradoModelo mob : _grados.values()) {
			if (str2.length() > 0) {
				str2.append("|");
			}
			str2.append(mob.getPDVMAX() + "~" + mob.getPA() + "~" + mob.getPM() + "~" + mob.getResistencias() + "~" + mob
			.getSpells().replace(";", ",") + "~" + mob.getBaseXp());
			str2.append("~" + mob.getMinKamas() + " - " + mob.getMaxKamas());
		}
		str.append(str2.toString());
		return str.toString();
	}
	
	public String listaNiveles() {
		final StringBuilder str = new StringBuilder();
		for (final MobGradoModelo mob : _grados.values()) {
			if (str.length() > 0) {
				str.append(", ");
			}
			str.append(mob.getNivel());
		}
		return str.toString();
	}
	
	public void setColores(final String colores) {
		_colores = colores;
	}
	
	public byte getTipoMob() {
		return _tipoMob;
	}
	
	public boolean esKickeable() {
		return _esKickeable;
	}
	
	public byte getDistAgresion() {
		return _distAgresion;
	}
	
	public void setDistAgresion(final byte dist) {
		_distAgresion = dist;
	}
	
	public int getID() {
		return _id;
	}
	
	public void addDrop(final DropMob drop) {
		borrarDrop(drop.getIDObjModelo());
		Mundo.getObjetoModelo(drop.getIDObjModelo()).addMobQueDropea(_id);
		_drops.add(drop);
		_drops.trimToSize();
	}
	
	public void borrarDrop(final int id) {
		DropMob remove = null;
		for (final DropMob d : _drops) {
			if (d.getIDObjModelo() == id) {
				remove = d;
				break;
			}
		}
		if (remove != null) {
			Mundo.getObjetoModelo(id).delMobQueDropea(_id);
			_drops.remove(remove);
		}
	}
	
	public ArrayList<DropMob> getDrops() {
		return _drops;
	}
	
	public short getTalla() {
		return _talla;
	}
	
	public short getGfxID() {
		return _gfxID;
	}
	
	public byte getAlineacion() {
		return _alineacion;
	}
	
	public String getColores() {
		return _colores;
	}
	
	public byte getTipoIA() {
		return _tipoIA;
	}
	
	public void setTipoIA(final byte IA) {
		_tipoIA = IA;
	}
	
	public void setTalla(final short talla) {
		_talla = talla;
	}
	
	public Map<Byte, MobGradoModelo> getGrados() {
		return _grados;
	}
	
	public MobGradoModelo getGradoPorNivel(final int nivel) {
		for (final MobGradoModelo grado : _grados.values()) {
			if (grado.getNivel() == nivel) {
				return grado;
			}
		}
		return null;
	}
	
	public MobGradoModelo getGradoPorGrado(final byte pos) {
		return _grados.get(pos);
	}
	
	public MobGradoModelo getRandomGrado() {
		return _grados.get((byte) Formulas.getRandomInt(1, _grados.size()));
	}
	
	public boolean esCapturable() {
		return _esCapturable;
	}
	
	public String getNombre() {
		return _nombre;
	}
}
