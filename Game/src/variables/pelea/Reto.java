package variables.pelea;

import estaticos.Constantes;
import estaticos.GestorSalida;

public class Reto {
	public enum EstReto {
		EN_ESPERA, REALIZADO, FALLADO
	};
	private final byte _id;
	private boolean _esLupa = false;
	private Luchador _mob = null;
	private EstReto _estado;
	private final int _bonusXPFijo, _bonusPPFijo, _bonusXPGrupo, _bonusPPGrupo;
	private final Pelea _pelea;
	
	public Reto(final byte ID, final int bonusXPFijo, final int bonusXPGrupo, final int bonusPPFijo,
	final int bonusPPGrupo, Pelea pelea) {
		_id = ID;
		_bonusXPFijo = bonusXPFijo;
		_bonusPPFijo = bonusPPFijo;
		_bonusXPGrupo = bonusXPGrupo;
		_bonusPPGrupo = bonusPPGrupo;
		_pelea = pelea;
		_estado = EstReto.EN_ESPERA;// 0
		switch (_id) {
			case Constantes.RETO_ZOMBI :
			case Constantes.RETO_ESTATUA :
			case Constantes.RETO_AHORRADOR :
			case Constantes.RETO_VERSATIL :
			case Constantes.RETO_JARDINERO :
			case Constantes.RETO_NOMADA :
			case Constantes.RETO_BARBARO :
			case Constantes.RETO_CRUEL :
			case Constantes.RETO_MISTICO :
			case Constantes.RETO_SEPULTURERO :
			case Constantes.RETO_CASINO_REAL :
			case Constantes.RETO_ARACNOFILO :
			case Constantes.RETO_ENTOMOLOGO :
			case Constantes.RETO_INTOCABLE :
			case Constantes.RETO_INCURABLE :
			case Constantes.RETO_MANOS_LIMPIAS :
			case Constantes.RETO_ELEMENTAL :
			case Constantes.RETO_CIRCULEN :
			case Constantes.RETO_EL_TIEMPO_PASA :
			case Constantes.RETO_PERDIDO_DE_VISTA :
			case Constantes.RETO_LIMITADO :
			case Constantes.RETO_ORDENADO :
			case Constantes.RETO_NI_PIAS_NI_SUMISAS :
			case Constantes.RETO_NI_PIOS_NI_SUMISOS :
			case Constantes.RETO_LOS_PEQUEÑOS_ANTES :
			case Constantes.RETO_SUPERVIVIENTE :
			case Constantes.RETO_AUDAZ :
			case Constantes.RETO_PEGAJOSO :
			case Constantes.RETO_BLITZKRIEG :
			case Constantes.RETO_ANACORETA :
			case Constantes.RETO_PUSILANIME :
			case Constantes.RETO_IMPETUOSO :
			case Constantes.RETO_EL_DOS_POR_UNO :
			case Constantes.RETO_ABNEGACION :
			case Constantes.RETO_REPARTO :
			case Constantes.RETO_DUELO :
			case Constantes.RETO_CADA_UNO_CON_SU_MONSTRUO :
			case Constantes.RETO_CONTAMINACION :
			case Constantes.RETO_LOS_PERSONAJES_SECUNDARIOS_PRIMERO :
			case Constantes.RETO_PROTEJAN_A_SUS_PERSONAJES_SECUNDARIOS :
			case Constantes.RETO_LA_TRAMPA_DE_LOS_DESARROLLADORES :
				break;
			case Constantes.RETO_ELEGIDO_VOLUNTARIO :
			case Constantes.RETO_APLAZAMIENTO :
			case Constantes.RETO_ELITISTA :
				// try {
				// final int azar = Formulas.getRandomValor(0, _pelea.cantLuchDeEquipo(2) - 1);
				// _mob = _pelea.luchadoresDeEquipo(2).get(azar);
				// } catch (Exception e) {}
				// continua para la lupa
			case Constantes.RETO_ASESINO_A_SUELDO :
			case Constantes.RETO_FOCALIZACION :
			case Constantes.RETO_IMPREVISIBLE :
				_esLupa = true;
				break;
		// (id, showTarget, targetId, basicXpBonus, teamXpBonus, basicDropBonus, teamDropBonus, state)
		}
	}
	
	public byte getID() {
		return _id;
	}
	
	public EstReto getEstado() {
		return _estado;
	}
	
	public void setEstado(EstReto e) {
		if (_estado == EstReto.EN_ESPERA) {
			if (e == EstReto.REALIZADO) {
				GestorSalida.ENVIAR_GdaK_RETO_REALIZADO(_pelea, _id);
			} else if (e == EstReto.FALLADO) {
				GestorSalida.ENVIAR_GdaO_RETO_FALLADO(_pelea, _id);
			} else
				return;
			_estado = e;
		}
	}
	
	private int numeroEstado() {
		if (_estado == EstReto.EN_ESPERA) {
			return 0;
		} else if (_estado == EstReto.REALIZADO) {
			return 1;
		} else {
			return 2;
		}
	}
	
	public String infoReto() {
		return _id + ";" + (_esLupa ? 1 : 0) + ";" + (_mob != null ? _mob.getID() : 0) + ";" + _bonusXPFijo + ";"
		+ _bonusXPGrupo + ";" + _bonusPPFijo + ";" + _bonusPPGrupo + ";" + numeroEstado();
	}
	
	public Luchador getLuchMob() {
		return _mob;
	}
	
	public void setMob(Luchador mob) {
		_mob = mob;
	}
	
	public int bonusXP() {
		return _bonusXPFijo + _bonusXPGrupo;
	}
	
	public int bonusDrop() {
		return _bonusPPFijo + _bonusPPGrupo;
	}
}
