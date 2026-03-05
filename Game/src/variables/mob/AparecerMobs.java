package variables.mob;

import variables.mapa.Mapa;
import estaticos.MainServidor;

public class AparecerMobs extends Thread {
	public static enum Aparecer {
		INICIO_PELEA, FINAL_PELEA
	};
	private final Mapa _mapa;
	private final GrupoMob _grupoMob;
	private final Aparecer _tipoAparecer;
	
	public AparecerMobs(Mapa mapa, GrupoMob grupoMob, Aparecer aparecer) {
		_mapa = mapa;
		_grupoMob = grupoMob;
		_tipoAparecer = aparecer;
		setDaemon(true);
		setPriority(6);
		start();
	}
	
	public void run() {
		int tiempo = MainServidor.SEGUNDOS_REAPARECER_MOBS;
		if (_grupoMob.getSegundosRespawn() > 0) {
			tiempo = _grupoMob.getSegundosRespawn();
		}
		if (tiempo > 0) {
			try {
				Thread.sleep(tiempo * 1000);
			} catch (Exception e) {}
		}
		switch (_tipoAparecer) {
			case INICIO_PELEA :
				_mapa.addSiguienteGrupoMob(_grupoMob, false);
				break;
			case FINAL_PELEA :
				_mapa.addUltimoGrupoMob(_grupoMob, false);
				break;
		}
	}
}
