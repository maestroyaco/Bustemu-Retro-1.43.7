package variables.mapa.interactivo;

import java.util.ArrayList;

public class ObjetoInteractivoModelo {
	private final int _id, _tiempoRecarga, _duracion;
	private final ArrayList<Integer> _gfx = new ArrayList<Integer>(), _skills = new ArrayList<Integer>();
	private final byte _tipo, _animacionnPJ;
	@SuppressWarnings("unused")
	private final byte _caminable;
	
	public ObjetoInteractivoModelo(final int id, final int tiempoRespuesta, final int duracion, final byte spritePJ,
	final byte caminable, final byte tipo, final String gfx, final String skill) {
		_id = id;
		_tiempoRecarga = tiempoRespuesta;
		_duracion = duracion;
		_animacionnPJ = spritePJ;
		_caminable = caminable;
		_tipo = tipo;
		for (final String str : gfx.split(",")) {
			if (str.isEmpty()) {
				continue;
			}
			try {
				_gfx.add(Integer.parseInt(str));
			} catch (final Exception e) {}
		}
		for (final String str : skill.split(",")) {
			if (str.isEmpty()) {
				continue;
			}
			try {
				_skills.add(Integer.parseInt(str));
			} catch (final Exception e) {}
		}
	}
	
	public ArrayList<Integer> getSkills() {
		return _skills;
	}
	
	public ArrayList<Integer> getGfxs() {
		return _gfx;
	}
	
	public byte getTipo() {
		// 1 recursos para recoger
		return _tipo;
	}
	
	public int getID() {
		return _id;
	}
	
	public int getTiempoRecarga() {
		return _tiempoRecarga;
	}
	
	public int getDuracion() {
		return _duracion;
	}
	
	public int getAnimacionPJ() {
		return _animacionnPJ;
	}
	// @SuppressWarnings("unused")
	// private boolean acercarse() {
	// return (_caminable & 1) == 1;
	// }
	//
	// @SuppressWarnings("unused")
	// private boolean esCaminable() {
	// return (_caminable & 2) == 2;
	// }
	
	public boolean tieneSkill(int skillID) {
		return _skills.contains(skillID);
	}
}