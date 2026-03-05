package variables.mob;

import java.util.ArrayList;
import estaticos.Formulas;

public class MobPosible {
	private ArrayList<MobGradoModelo> _mobs;
	private int _cantMax, _probabilidad;
	
	public MobPosible(int cant, int porcentaje) {
		_cantMax = cant;
		_probabilidad = porcentaje;
	}
	
	public void addMobPosible(MobGradoModelo mob) {
		if (_mobs == null) {
			_mobs = new ArrayList<>();
		}
		_mobs.add(mob);
	}
	
	public boolean tieneMob(MobGradoModelo mob) {
		if (_mobs == null) {
			return false;
		}
		return _mobs.contains(mob);
	}
	
	public MobGradoModelo getRandomMob() {
		if (_mobs == null) {
			return null;
		}
		return _mobs.get(Formulas.getRandomInt(0, _mobs.size() - 1));
	}
	
	public int getCantMax() {
		return _cantMax;
	}
	
	public boolean pasoProbabilidad() {
		if (_probabilidad < 0 || _probabilidad >= 100) {
			return true;
		}
		return Formulas.getRandomInt(1, 100) <= _probabilidad;
	}
}
