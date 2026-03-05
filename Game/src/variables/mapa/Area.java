package variables.mapa;

import java.util.ArrayList;
import variables.zotros.Prisma;
import estaticos.Constantes;
import estaticos.Mundo;

public class Area {
	private final int _id;
	private final String _nombre;
	private final ArrayList<SubArea> _subAreas = new ArrayList<SubArea>();
	private byte _alineacion = Constantes.ALINEACION_NEUTRAL;
	private SuperArea _superArea;
	private Prisma _prisma;
	public static int BONTAS = 0, BRAKMARS = 0;
	
	public Area(final int id, final short superArea, final String nombre) {
		_id = id;
		_nombre = nombre;
		_superArea = Mundo.getSuperArea(superArea);
		if (_superArea == null) {
			_superArea = new SuperArea(superArea);
			Mundo.addSuperArea(_superArea);
		}
	}
	
	public void setPrisma(Prisma prisma) {
		_prisma = prisma;
		setAlineacion(prisma == null ? Constantes.ALINEACION_NEUTRAL : prisma.getAlineacion());
	}
	
	public Prisma getPrisma() {
		return _prisma;
	}
	
	public static int subareasBontas() {
		return BONTAS;
	}
	
	public static int subareasBrakmars() {
		return BRAKMARS;
	}
	
	public byte getAlineacion() {
		if (_id == 7) {
			return Constantes.ALINEACION_BONTARIANO;
		}
		if (_id == 11) {
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
	
	public String getNombre() {
		return _nombre;
	}
	
	public int getID() {
		return _id;
	}
	
	public SuperArea getSuperArea() {
		return _superArea;
	}
	
	public void addSubArea(final SubArea sa) {
		_subAreas.add(sa);
	}
	
	public ArrayList<SubArea> getSubAreas() {
		return _subAreas;
	}
	
	public ArrayList<Mapa> getMapas() {
		final ArrayList<Mapa> mapas = new ArrayList<Mapa>();
		for (final SubArea SA : _subAreas) {
			mapas.addAll(SA.getMapas());
		}
		return mapas;
	}
}