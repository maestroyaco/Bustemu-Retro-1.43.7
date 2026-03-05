package variables.personaje;

import java.util.ArrayList;
import java.util.regex.Pattern;
import variables.stats.Stats;
import estaticos.Mundo;

public class Especialidad {
	private final int _id;
	private final int _orden;
	private final int _nivel;
	private ArrayList<Don> _dones = new ArrayList<>();
	
	public Especialidad(final int id, final int orden, final int nivel, final String dones) {
		_id = id;
		_orden = orden;
		_nivel = nivel;
		for (String s : dones.split(Pattern.quote("|"))) {
			if (s.isEmpty()) {
				continue;
			}
			try {
				String[] args = s.split(",");
				int donID = Integer.parseInt(args[0]);
				int donNivel = Integer.parseInt(args[1]);
				int donStat = Mundo.getDonStat(donID);
				int valor = 0;
				if (args.length > 2) {
					valor = Integer.parseInt(args[2]);
				}
				_dones.add(new Don(donID, donNivel, donStat, valor));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public int getID() {
		return _id;
	}
	
	public int getOrden() {
		return _orden;
	}
	
	public int getNivel() {
		return _nivel;
	}
	
	public ArrayList<Don> getDones() {
		return _dones;
	}
	public class Don {
		private final int _id;
		private final int _nivel;
		private final Stats _stats = new Stats();
		
		public Don(int id, int nivel, int statID, int valor) {
			_id = id;
			_nivel = nivel;
			if (statID > 0 && valor > 0) {
				_stats.addStatID(statID, valor);
			}
		}
		
		public int getID() {
			return _id;
		}
		
		public int getNivel() {
			return _nivel;
		}
		
		public Stats getStat() {
			return _stats;
		}
	}
}
