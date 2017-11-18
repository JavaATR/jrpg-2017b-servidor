package comandos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import mensajeria.Comando;
import mensajeria.PaqueteMensaje;
import mensajeria.PaquetePersonaje;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Clase que admnistra el chat entre clientes. <br>
 */
public class Talk extends ComandosServer {
	/**
	 * Ejecuta el envío de mensajes entre usuarios.
	 * <p>
	 * <i>En caso de que no se pueda envíar el mensaje, se avisa.</i> <br>
	 */
	
	List<String> trucos = new ArrayList<>(Arrays.asList("iddqd", "noclip", "bigdaddy", "tinydaddy", "war aint what it used to be"));
	
	@Override
	public final void ejecutar() {
		int idUser = 0;
		int contador = 0;
		int truco = 0;
		
		PaqueteMensaje paqueteMensaje = (PaqueteMensaje) (gson.fromJson(cadenaLeida, PaqueteMensaje.class));
		if (!(paqueteMensaje.getUserReceptor() == null)) {
			if (Servidor.mensajeAUsuario(paqueteMensaje)) {
				paqueteMensaje.setComando(Comando.TALK);
				for (Map.Entry<Integer, PaquetePersonaje> personaje : Servidor.getPersonajesConectados().entrySet()) {
					if (personaje.getValue().getNombre().equals(paqueteMensaje.getUserReceptor())) {
						idUser = personaje.getValue().getId();
					}
				}
				for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
					if (conectado.getIdPersonaje() == idUser) {
						try {
							conectado.getSalida().writeObject(gson.toJson(paqueteMensaje));
						} catch (IOException e) {
							Servidor.log.append("Falló al intentar enviar mensaje a:"
									+ conectado.getPaquetePersonaje().getId() + ".\n");
						}
					}
				}
			} else {
				Servidor.log.append("No se envió el mensaje.\n");
			}
		} else { // Esto es cuando no se está hablando a nadie por privado. Acá se aplican los cheats
			for (Map.Entry<Integer, PaquetePersonaje> personaje : Servidor.getPersonajesConectados().entrySet()) {
				if (personaje.getValue().getNombre().equals(paqueteMensaje.getUserEmisor())) {
					idUser = personaje.getValue().getId();
					contador++;
				}
			}
			
			truco = mensajeEsTruco(paqueteMensaje.getMensaje());
			
			if (truco != -1) {
				switch (truco) {
					case 0:
						ponerPersonajeEnModoDios(idUser);
						break;
					case 1:
						permitirPersonajeAtravesarParedes(idUser);
						break;
					case 2:
						ponerPersonajeEnModoExtraFuerza(idUser);
						break;
					case 3:
						ponerPersonajeEnModoMitadFuerza(idUser);
						break;
					case 4:
						ponerPersonajeEnModoInvisible(idUser);
						break;
				}
			} else {			
				for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
					if (conectado.getIdPersonaje() != idUser) {
						try {
							conectado.getSalida().writeObject(gson.toJson(paqueteMensaje));
						} catch (IOException e) {
							Servidor.log.append("Falló al intentar enviar mensaje a:"
									+ conectado.getPaquetePersonaje().getId() + ".\n");
						}
					}
				}
				Servidor.mensajeAAll(contador);
			}
		}
	}

	private void ponerPersonajeEnModoInvisible(int idUser) {
		// TODO Auto-generated method stub
		
	}

	private void ponerPersonajeEnModoExtraFuerza(int idUser) {
		// TODO Auto-generated method stub
		
	}
	
	private void ponerPersonajeEnModoMitadFuerza(int idUser) {
		// TODO Auto-generated method stub
		
	}

	private void permitirPersonajeAtravesarParedes(int idUser) {
		// TODO Auto-generated method stub
		
	}

	private void ponerPersonajeEnModoDios(int idUser) {
		// TODO Auto-generated method stub
		
	}

	private int mensajeEsTruco(String mensaje) {
		return trucos.indexOf(mensaje);
	}
}
