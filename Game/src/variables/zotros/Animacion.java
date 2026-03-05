package variables.zotros;
public class Animacion {
	private final int _id, _animacionID, _tipoDisplay, _spriteAnimacion, _level, _duracion, _talla;
	
	public Animacion(final int id, final int animacionID, final int tipoDisplay, final int spriteAnimacion,
	final int level, final int duracion, final int talla) {
		_id = id;
		_animacionID = animacionID;
		_tipoDisplay = tipoDisplay;
		_spriteAnimacion = spriteAnimacion;
		_level = level;
		_duracion = duracion;
		_talla = talla;
	}
	
	public int getID() {
		return _id;
	}
	
	public int getTipoDisplay() {
		return _tipoDisplay;
	}
	
	public int getAnimacionID() {
		return _animacionID;
	}
	
	public int getLevel() {
		return _level;
	}
	
	public int getDuracion() {
		return _duracion;
	}
	public int getTalla()	{
		return _talla;
	}
	public int getSpriteAnimacion() {
		return _spriteAnimacion;
	}
}