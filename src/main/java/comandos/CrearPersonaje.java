package comandos;

import java.io.IOException;

import mensajeria.PaquetePersonaje;
import servidor.Servidor;

/**
 * Clase que administra la creación de un personaje. <br>
 */
public class CrearPersonaje extends ComandosServer {
	/**
	 * Ejecuta la creación de un personaje.
	 * <p>
	 * <i>En caso de que no se pueda crear al personaje, se avisa.</i> <br>
	 */
	@Override
	public void ejecutar() {
		// Casteo el paquete personaje
		escuchaCliente.setPaquetePersonaje((PaquetePersonaje) (
				gson.fromJson(cadenaLeida, PaquetePersonaje.class)));
		// Guardo el personaje en ese usuario
		Servidor.getConector().registrarPersonaje(
				escuchaCliente.getPaquetePersonaje(),
				escuchaCliente.getPaqueteUsuario());
		try {
			PaquetePersonaje paquetePersonaje;
			paquetePersonaje = new PaquetePersonaje();
			paquetePersonaje = Servidor.getConector().getPersonaje(
					escuchaCliente.getPaqueteUsuario());
			escuchaCliente.setIdPersonaje(paquetePersonaje.getId());
			escuchaCliente.getSalida().writeObject(
					gson.toJson(escuchaCliente.getPaquetePersonaje(), 
							escuchaCliente.getPaquetePersonaje().getClass()));
		} catch (IOException e1) {
			Servidor.log.append(
					"Falló al intentar enviar personaje creado.\n");
		}
	}
}
