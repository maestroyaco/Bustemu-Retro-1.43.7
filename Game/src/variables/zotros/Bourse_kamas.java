package variables.zotros;

public class Bourse_kamas {

	
	public int id;
	public int id_perso;
	public long kamas;
	public int points;
	public int taux;
	public int statu ;

	public Bourse_kamas(int id, int idperso, long kamas, int points , int taux , int statu) {
	    this.id = id;
	    this.id_perso  = idperso;
	    this.kamas = kamas;
	    this.points = points;
	    this.taux = taux;
	    this.statu = statu;
	    
	       }
	
	
public int getId() {
	return id;
}

public void setId(int id) {
	this.id = id;
}

public int getId_perso() {
	return id_perso;
}

public void setId_perso(int id_perso) {
	this.id_perso = id_perso;
}

public long getKamas() {
	return kamas;
}

public void setKamas(long kamas) {
	this.kamas = kamas;
}

public int getPoints() {
	return points;
}

public void setPoints(int points) {
	this.points = points;
}

public int getTaux() {
	return taux;
}

public void setTaux(int taux) {
	this.taux = taux;
}

public int getStatu() {
	return statu;
}

public void setStatu(int statu) {
	this.statu = statu;
}


}