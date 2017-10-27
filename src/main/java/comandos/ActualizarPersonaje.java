package comandos;

import java.io.IOException;

import mensajeria.PaquetePersonaje;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Clase que administra el actualizado del personaje. <br>
 */
public class ActualizarPersonaje extends ComandosServer {
	/**
	 * Ejecuta el actualizado del personaje.
	 * <p>
	 * <i>En caso de que no se pueda enviar el actualizado del personaje, se
	 * avisa.</i> <br>
	 */
	@Override
	public void ejecutar() {
		escuchaCliente.setPaquetePersonaje((PaquetePersonaje) gson
				.fromJson(cadenaLeida, PaquetePersonaje.class));
		Servidor.getConector().actualizarPersonaje(escuchaCliente
				.getPaquetePersonaje());
		Servidor.getPersonajesConectados().remove(escuchaCliente
				.getPaquetePersonaje().getId());
		Servidor.getPersonajesConectados().put(escuchaCliente
				.getPaquetePersonaje().getId(),
				escuchaCliente.getPaquetePersonaje());
		for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
			try {
				conectado.getSalida().writeObject(
						gson.toJson(escuchaCliente.getPaquetePersonaje()));
			} catch (IOException e) {
				Servidor.log.append(
						"Falló al intentar enviar paquetePersonaje a:"
						+ conectado.getPaquetePersonaje().getId() + ".\n");
			}
		}
	}
}
