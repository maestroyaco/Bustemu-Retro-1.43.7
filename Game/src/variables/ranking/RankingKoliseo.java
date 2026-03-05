package variables.ranking;
public class RankingKoliseo {
	private final int _id;
	private String _nombre;
	private int _victorias = 0;
	private int _derrotas = 0;
	
	public RankingKoliseo(final int id, final String nombre, final int victorias, final int derrotas) {
		_id = id;
		_nombre = nombre;
		_victorias = victorias;
		_derrotas = derrotas;
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
	
	public String getNombre() {
		return _nombre;
	}
	
	public void setNombre(final String nombre) {
		_nombre = nombre;
	}
}