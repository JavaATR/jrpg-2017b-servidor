package comandos;

import java.io.IOException;

import estados.Estado;
import mensajeria.PaqueteBatalla;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Clase que administra la batalla entre clientes. <br>
 */
public class Batalla extends ComandosServer {
	/**
	 * Ejecuta la batalla entre personajes.
	 * <p>
	 * <i>En caso de que no se pueda realizar la batalla, se avisa.</i> <br>
	 */
	@Override
	public void ejecutar() {
		// Le reenvio el id del personaje batallado que quieren pelear
		escuchaCliente.setPaqueteBatalla((PaqueteBatalla) gson.fromJson(cadenaLeida, PaqueteBatalla.class));
		Servidor.log.append(escuchaCliente.getPaqueteBatalla().getId() + " quiere batallar con "
				+ escuchaCliente.getPaqueteBatalla().getIdEnemigo() + System.lineSeparator());
		
		// Me fijo si el usuario va a batallar contra un NPC
		if (escuchaCliente.getPaqueteBatalla().getIdEnemigo() < 0) {
			try {
				// Seteo estado de batalla
				Servidor.getPersonajesConectados().get(escuchaCliente.getPaqueteBatalla().getId())
						.setEstado(Estado.estadoBatalla);
				escuchaCliente.getPaqueteBatalla().setMiTurno(true);
				escuchaCliente.getSalida().writeObject(gson.toJson(escuchaCliente.getPaqueteBatalla()));
			} catch (IOException e) {
				Servidor.log.append("Falló al intentar enviar Batalla.\n");
			}
			synchronized (Servidor.atencionConexiones) {
				Servidor.atencionConexiones.notify();
			}
		}
		
		// Si no es NPC, es otro usuario
		else {
			try {
		
			// Seteo estado de batalla
			Servidor.getPersonajesConectados().get(escuchaCliente.getPaqueteBatalla().getId())
					.setEstado(Estado.estadoBatalla);
			Servidor.getPersonajesConectados().get(escuchaCliente.getPaqueteBatalla().getIdEnemigo())
					.setEstado(Estado.estadoBatalla);
			escuchaCliente.getPaqueteBatalla().setMiTurno(true);
			escuchaCliente.getSalida().writeObject(gson.toJson(escuchaCliente.getPaqueteBatalla()));
			for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
				if (conectado.getIdPersonaje() == escuchaCliente.getPaqueteBatalla().getIdEnemigo()) {
					int aux = escuchaCliente.getPaqueteBatalla().getId();
					escuchaCliente.getPaqueteBatalla().setId(escuchaCliente.getPaqueteBatalla().getIdEnemigo());
					escuchaCliente.getPaqueteBatalla().setIdEnemigo(aux);
					escuchaCliente.getPaqueteBatalla().setMiTurno(false);
					conectado.getSalida().writeObject(gson.toJson(escuchaCliente.getPaqueteBatalla()));
					
					break;
				}
			}
			} catch (IOException e) {
				Servidor.log.append("Falló al intentar enviar Batalla.\n");
			}
			synchronized (Servidor.atencionConexiones) {
				Servidor.atencionConexiones.notify();
			}
		}
	}
}