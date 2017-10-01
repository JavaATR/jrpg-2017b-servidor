package comandos;

import mensajeria.PaquetePersonaje;
import servidor.Servidor;

/**
 * Clase que muestra el mapa seleccionado por el cliente. <br>
 */
public class MostrarMapas extends ComandosServer {
	/**
	 * Indica el mapa que ha elegido el cliente. <br>
	 */
	@Override
	public void ejecutar() {
		escuchaCliente.setPaquetePersonaje((PaquetePersonaje) gson.fromJson(cadenaLeida, PaquetePersonaje.class));
		Servidor.log.append(escuchaCliente.getSocket().getInetAddress().getHostAddress() + " ha elegido el mapa "
				+ escuchaCliente.getPaquetePersonaje().getMapa() + System.lineSeparator());
	}
}