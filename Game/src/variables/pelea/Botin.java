package variables.pelea;
public class Botin {
	private DropMob _drop;
	private int _maximo;
	
	public Botin(DropMob drop) {
		_drop = drop;
		_maximo = drop.getMaximo();
	}
	
	public DropMob getDrop() {
		return _drop;
	}
	
	public void addBotinMaximo(int cant) {
		_maximo += cant;
	}
	
	public int getBotinMaximo() {
		return _maximo;
	}
	
	public int getIDObjModelo() {
		return _drop.getIDObjModelo();
	}
	
	public int getProspeccionBotin() {
		return _drop.getProspeccion();
	}
	
	public float getPorcentajeBotin() {
		return _drop.getPorcentaje();
	}
	
	public String getCondicionBotin() {
		return _drop.getCondicion();
	}
	
	public boolean esDropFijo() {
		return _drop.esDropFijo();
	}
}
