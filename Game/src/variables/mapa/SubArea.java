package variables.mapa;

import java.util.ArrayList;
import variables.zotros.Prisma;
import estaticos.Constantes;
import estaticos.Mundo;

public class SubArea {
	public static int BONTAS, BRAKMARS;
	private final int _id;
	private final String _nombre;
	private final ArrayList<Mapa> _mapas = new ArrayList<Mapa>();
	private final boolean _conquistable;
	private byte _alineacion = Constantes.ALINEACION_NEUTRAL;
	private final Area _area;
	private Prisma _prisma;
	private int _minNivelGrupoMob = 0;
	private int _maxNivelGrupoMob = 0;
	private String _cementerio = "";
	
	public SubArea(final int id, final short areaID, final String nombre, final boolean conquistable,
	int minNivelGrupoMob, int maxNivelGrupoMob, String cementerio) {
		_id = id;
		_nombre = nombre;
		_area = Mundo.getArea(areaID);
		_conquistable = conquistable;
		_minNivelGrupoMob = minNivelGrupoMob;
		_maxNivelGrupoMob = maxNivelGrupoMob;
		_cementerio = cementerio;
	}
	
	public String getCementerio() {
		return _cementerio;
	}
	
	public int getMinNivelGrupoMob() {
		return _minNivelGrupoMob;
	}
	
	public int getMaxNivelGrupoMob() {
		return _maxNivelGrupoMob;
	}
	
	public void setPrisma(Prisma prisma) {
		_prisma = prisma;
		setAlineacion(prisma == null ? Constantes.ALINEACION_NEUTRAL : prisma.getAlineacion());
	}
	
	public Prisma getPrisma() {
		return _prisma;
	}
	
	public String getNombre() {
		return _nombre;
	}
	
	public boolean esConquistable() {
		return _conquistable;
	}
	
	public int getID() {
		return _id;
	}
	
	public Area getArea() {
		return _area;
	}
	
	public byte getAlineacion() {
		if (_area.getID() == 7) {
			return Constantes.ALINEACION_BONTARIANO;
		}
		if (_area.getID() == 11) {
			return Constantes.ALINEACION_BRAKMARIANO;
		}
		return _alineacion;
	}
	
	private void setAlineacion(final byte alin) {
		if (_alineacion == alin) {
			return;
		}
		if (_alineacion == Constantes.ALINEACION_BONTARIANO) {
			BONTAS--;
		}
		if (_alineacion == Constantes.ALINEACION_BRAKMARIANO) {
			BRAKMARS--;
		}
		if (alin == Constantes.ALINEACION_BONTARIANO) {
			BONTAS++;
		}
		if (alin == Constantes.ALINEACION_BRAKMARIANO) {
			BRAKMARS++;
		}
		_alineacion = alin;
	}
	
	public ArrayList<Mapa> getMapas() {
		return _mapas;
	}
	
	public void addMapa(final Mapa mapa) {
		_mapas.add(mapa);
	}
	
	public static int subareasBontas() {
		return BONTAS;
	}
	
	public static int subareasBrakmars() {
		return BRAKMARS;
	}
}