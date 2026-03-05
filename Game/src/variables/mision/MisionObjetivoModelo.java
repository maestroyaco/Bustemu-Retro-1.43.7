package variables.mision;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import variables.npc.NPC;
import variables.objeto.Objeto;
import variables.personaje.Personaje;
import estaticos.Constantes;
import estaticos.GestorSalida;

public class MisionObjetivoModelo {
	// public final static int SIN_CUMPLIR = 0, CUMPLIDO = 1;
	public final static byte NULL = 0;
	public final static byte HABLAR_CON_NPC = 1;
	public final static byte ENSEÑAR_OBJETO_NPC = 2;
	public final static byte ENTREGAR_OBJETO_NPC = 3;
	public final static byte DESCUBRIR_MAPA = 4;
	public final static byte DESCUBRIR_ZONA = 5;
	public final static byte VENCER_MOBS_UN_COMBATE = 6;
	public final static byte VENCER_AL_MOB = 7;
	public final static byte UTILIZAR_OBJETO = 8;
	public final static byte VOLVER_VER_NPC = 9;
	public final static byte ENTREGAR_ALMAS_NPC = 12;
	private final int _id;
	private final byte _tipo;
	private String _args;
	
	public MisionObjetivoModelo(final int id, final byte tipo, final String args) {
		_id = id;
		_tipo = tipo;
		_args = args;
	}
	
	public int getID() {
		return _id;
	}
	
	public byte getTipo() {
		return _tipo;
	}
	
	public String getArgs() {
		return _args;
	}
	
	public void setArgs(String args) {
		_args = args;
	}
	
	public boolean confirmar(final Personaje perso, final Map<Integer, Integer> mobs, final boolean preConfirma,
	int idObjeto) {
		// preconfirma no borra nada solo confirma
		boolean b = false;
		NPC npc = null;
		if (perso.getConversandoCon() < 0 && perso.getConversandoCon() > -100) {
			npc = perso.getMapa().getNPC(perso.getConversandoCon());
		}
		switch (_tipo) {
			case NULL :// XXXXXX
				b = true;
				break;
			case VOLVER_VER_NPC :// vuelve a ver a X NPC
			case HABLAR_CON_NPC :// hablar con X NPC
				try {
					if (npc == null) {
						break;
					}
					final String[] args = argsPreparados();
					final int idNPC = Integer.parseInt(args[0]);
					b = idNPC == npc.getModeloID();
					if (args.length >= 3) {
						int x = Integer.parseInt(args[1].split(":")[1]);
						int y = Integer.parseInt(args[2].split(":")[1]);
						b &= (perso.getMapa().getX() == x) & (perso.getMapa().getY() == y);
					}
				} catch (final Exception e) {}
				break;
			case ENSEÑAR_OBJETO_NPC :// enseñar X cant de X objeto a X NPC
				try {
					if (npc == null) {
						break;
					}
					final String[] args = argsPreparados();
					String[] req = args[0].split(",");
					final int idNPC = Integer.parseInt(req[0]);
					final int idObjModelo = Integer.parseInt(req[1]);
					final int cantObj = Integer.parseInt(req[2]);
					b = idNPC == npc.getModeloID();
					if (args.length >= 3) {
						int x = Integer.parseInt(args[1].split(":")[1]);
						int y = Integer.parseInt(args[2].split(":")[1]);
						b &= (perso.getMapa().getX() == x) & (perso.getMapa().getY() == y);
					}
					if (b) {
						b &= perso.tieneObjPorModYCant(idObjModelo, cantObj);
					}
				} catch (final Exception e) {}
				break;
			case ENTREGAR_OBJETO_NPC :// entregar X cant de X objeto X NPC
				try {
					if (npc == null) {
						break;
					}
					final String[] args = argsPreparados();
					String[] req = args[0].split(",");
					final int idNPC = Integer.parseInt(req[0]);
					final int idObjModelo = Integer.parseInt(req[1]);
					final int cantObj = Integer.parseInt(req[2]);
					b = idNPC == npc.getModeloID();
					if (args.length >= 3) {
						int x = Integer.parseInt(args[1].split(":")[1]);
						int y = Integer.parseInt(args[2].split(":")[1]);
						b &= (perso.getMapa().getX() == x) & (perso.getMapa().getY() == y);
					}
					if (b) {
						if (preConfirma) {
							b = perso.tieneObjPorModYCant(idObjModelo, cantObj);
						} else if (b = perso.tenerYEliminarObjPorModYCant(idObjModelo, cantObj)) {
							GestorSalida.ENVIAR_Im_INFORMACION(perso, "022;" + cantObj + "~" + idObjModelo);
						}
					}
				} catch (final Exception e) {}
				break;
			case DESCUBRIR_MAPA :// descubre X mapa
				try {
					b = Short.parseShort(_args) == perso.getMapa().getID();
				} catch (final Exception e) {}
				break;
			case DESCUBRIR_ZONA :
				try {
					b = Short.parseShort(_args) == perso.getMapa().getSubArea().getArea().getID();
				} catch (final Exception e) {}
				break;
			case VENCER_AL_MOB :// vence al monstruo
			case VENCER_MOBS_UN_COMBATE :// matar cierto mob
				try {
					b = true;
					final String[] args = argsPreparados();
					String[] req = args[0].split(",");
					for (int i = 0; i < req.length; i += 2) {
						final int idMob = Integer.parseInt(req[i]);
						final int cant = Integer.parseInt(req[i + 1]);
						boolean t = false;
						for (final Entry<Integer, Integer> entry : mobs.entrySet()) {
							if (entry.getKey() == idMob && entry.getValue() >= cant) {
								t = true;
								break;
							}
						}
						b &= t;
					}
					if (args.length >= 3) {
						int x = Integer.parseInt(args[1].split(":")[1]);
						int y = Integer.parseInt(args[2].split(":")[1]);
						b &= (perso.getMapa().getX() == x) & (perso.getMapa().getY() == y);
					}
				} catch (final Exception e) {}
				break;
			case UTILIZAR_OBJETO :// utiliza X objeto
				try {
					final String[] args = _args.replace("[", "").replace("]", "").replace(" ", "").split(",");
					final int idObj = Integer.parseInt(args[0]);
					b = idObj == idObjeto;
				} catch (final Exception e) {}
				break;
			case 10 :// escolta X
			case 11 :// vence jugador desafio
			case 13 :// elimina X
				b = true;
				break;
			case ENTREGAR_ALMAS_NPC :// lleva X alma a X NPC
				final String[] args = _args.replace("[", "").replace("]", "").replace(" ", "").split(",");
				String alma = Integer.toHexString(Integer.parseInt(args[1]));
				int cantidad = Integer.parseInt(args[2]);
				int van = 0;
				ArrayList<Objeto> o = new ArrayList<>();
				for (Objeto obj : perso.getObjetosTodos()) {
					if (van >= cantidad) {
						break;
					}
					switch (obj.getObjModeloID()) {
						case 7010 :
						case 9720 :
						case 10417 :
						case 10418 :
							break;
						default :
							continue;
					}
					String[] stats = obj.convertirStatsAString(true).split(",");
					boolean c = false;
					for (String st : stats) {
						try {
							int statID = Integer.parseInt(st.split("#")[0], 16);
							if (statID != Constantes.STAT_INVOCA_MOB) {
								continue;
							}
							if (van >= cantidad) {
								continue;
							}
							if (st.split("#")[3].equalsIgnoreCase(alma)) {
								van++;
								c = true;
							}
						} catch (Exception e) {}
					}
					if (c) {
						o.add(obj);
					}
				}
				if (van >= cantidad) {
					b = true;
					if (!preConfirma) {
						van = 0;
						for (Objeto obj : o) {
							String[] stats = obj.convertirStatsAString(true).split(",");
							String nuevo = "";
							for (String st : stats) {
								if (!nuevo.isEmpty()) {
									nuevo += ",";
								}
								int statID = Integer.parseInt(st.split("#")[0], 16);
								if (statID == Constantes.STAT_INVOCA_MOB && st.split("#")[3].equalsIgnoreCase(alma) && van < cantidad) {
									van++;
								} else {
									nuevo += st;
								}
							}
							if (nuevo.isEmpty()) {
								perso.borrarOEliminarConOR(obj.getID(), true);
							} else {
								obj.convertirStringAStats(nuevo);
								GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(perso, obj);
							}
						}
					}
				}
				break;
		}
		return b;
	}
	
	private String[] argsPreparados() {
		return prepararArgs(_args, ',', '|').replace(" ", "").split(Pattern.quote("|"));
	}
	
	private static String prepararArgs(String args, char buscar, char reemplazar) {
		StringBuilder s = new StringBuilder();
		int corchetes = 0;
		for (char a : args.toCharArray()) {
			switch (a) {
				case '[' :
					corchetes++;
					if (corchetes > 1) {
						s.append(a);
					}
					break;
				case ']' :
					if (corchetes > 1) {
						s.append(a);
					}
					corchetes--;
					break;
				default :
					if (corchetes == 0 && a == buscar) {
						s.append(reemplazar);
					} else {
						s.append(a);
					}
					break;
			}
		}
		return s.toString();
	}
}