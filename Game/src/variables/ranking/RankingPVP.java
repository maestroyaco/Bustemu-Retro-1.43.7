package variables.ranking;
public class RankingPVP {
	private final int _id;
	private String _nombre;
	private int _victorias = 0;
	private int _derrotas = 0;
	private int _gradoAlineacion = 1;
	
	public RankingPVP(final int id, final String nombre, final int victorias, final int derrotas,
	final int gradoAlineacion) {
		_id = id;
		_nombre = nombre;
		_victorias = victorias;
		_derrotas = derrotas;
		_gradoAlineacion = gradoAlineacion;
	}
	
	public int getVictorias() {
		return _victorias;
	}
	
	public int getDerrotas() {
		return _derrotas;
	}
	
	public int getID() {
		return _id;
	}
	
	public void aumentarVictoria() {
		_victorias += 1;
	}
	
	public void aumentarDerrota() {
		_derrotas += 1;
	}
	
	public void setGradoAlineacion(final int nivel) {
		_gradoAlineacion = nivel;
	}
	
	public String getNombre() {
		return _nombre;
	}
	
	public int getGradoAlineacion() {
		return _gradoAlineacion;
	}
	
	public void setNombre(final String nombre) {
		_nombre = nombre;
	}
}