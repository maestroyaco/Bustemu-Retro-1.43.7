package variables.zotros;

public class MasterReset {
	
	private int id;
	private int nivel;
	private String sufijo;
	private int capital;
	private int hechizo;
	private boolean reinicia;
	
	public MasterReset() {
		
	}
		
	public MasterReset(int id, int nivel, String sufijo, int capital, int hechizo, boolean reinicia) {
		super();
		this.id = id;
		this.nivel = nivel;
		this.sufijo = sufijo;
		this.capital = capital;
		this.hechizo = hechizo;
		this.reinicia = reinicia;
	}
	
	


	public boolean isReinicia() {
		return reinicia;
	}

	public void setReinicia(boolean reinicia) {
		this.reinicia = reinicia;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getNivel() {
		return nivel;
	}
	public void setNivel(int nivel) {
		this.nivel = nivel;
	}
	public String getSufijo() {
		return sufijo;
	}
	public void setSufijo(String sufijo) {
		this.sufijo = sufijo;
	}
	public int getCapital() {
		return capital;
	}
	public void setCapital(int capital) {
		this.capital = capital;
	}
	public int getHechizo() {
		return hechizo;
	}
	public void setHechizo(int hechizo) {
		this.hechizo = hechizo;
	}
}
