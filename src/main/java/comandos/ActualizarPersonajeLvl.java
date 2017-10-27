package comandos;

import java.io.IOException;

import mensajeria.PaquetePersonaje;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Clase que administra el nivel del cliente con relación a como lo ven los
 * demás clientes. <br>
 */
public class ActualizarPersonajeLvl extends ComandosServer {
	/**
	 * Ejecuta el actualizado de personajes en relación a su nivel.
	 * <p>
	 * <i>En caso de que no se pueda tener actualizado su nivel, se avisa.</i>
	 * <br>
	 */
	@Override
	public void ejecutar() {
		escuchaCliente.setPaquetePersonaje((PaquetePersonaje) gson.fromJson(
				cadenaLeida, PaquetePersonaje.class));
		Servidor.getConector().actualizarPersonajeSubioNivel(
				escuchaCliente.getPaquetePersonaje());
		Servidor.getPersonajesConectados().remove(
				escuchaCliente.getPaquetePersonaje().getId());
		Servidor.getPersonajesConectados().put(
				escuchaCliente.getPaquetePersonaje().getId(),
				escuchaCliente.getPaquetePersonaje());
		escuchaCliente.getPaquetePersonaje().ponerBonus();
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
