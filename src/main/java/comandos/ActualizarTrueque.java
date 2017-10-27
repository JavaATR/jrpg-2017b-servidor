package comandos;

import java.io.IOException;

import mensajeria.PaquetePersonaje;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Clase que actualiza el trueque entre los clientes. <br>
 */
public class ActualizarTrueque extends ComandosServer {
	/**
	 * Ejecuta el escucha de trueque entre personajes.
	 * <p>
	 * <i>En caso de que no se pueda tener actualizado el trueque, se avisa.</i>
	 * <br>
	 */
	@Override
	public void ejecutar() {
		escuchaCliente.setPaquetePersonaje((PaquetePersonaje) gson
				.fromJson(cadenaLeida, PaquetePersonaje.class));
		Servidor.getConector().actualizarInventario(escuchaCliente
				.getPaquetePersonaje());
		Servidor.getConector().actualizarPersonaje(escuchaCliente
				.getPaquetePersonaje());
		Servidor.getPersonajesConectados().remove(escuchaCliente
				.getPaquetePersonaje().getId());
		Servidor.getPersonajesConectados().put(escuchaCliente
				.getPaquetePersonaje().getId(),
				escuchaCliente.getPaquetePersonaje());
		for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
			try {
				conectado.getSalida().writeObject(gson
						.toJson(escuchaCliente.getPaquetePersonaje()));
			} catch (IOException e) {
				Servidor.log.append(
						"Falló al intentar enviar actualizacion de trueque a:"
						+ conectado.getPaquetePersonaje().getId() + ".\n");
			}
		}
	}
}
