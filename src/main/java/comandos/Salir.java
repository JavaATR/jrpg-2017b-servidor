package comandos;

import java.io.IOException;

import mensajeria.Paquete;
import servidor.Servidor;

/**
 * Clase que administra la salida de un cliente del juego. <br>
 */
public class Salir extends ComandosServer {
	/**
	 * Ejecuta la salida del cliente del servidor. <br>
	 */
	@Override
	public void ejecutar() {
		// Cierro todo
		try {
			escuchaCliente.getEntrada().close();
			escuchaCliente.getSalida().close();
			escuchaCliente.getSocket().close();
		} catch (IOException e) {
			Servidor.log.append("Falló al intentar salir \n");
		}
		// Lo elimino de los clientes conectados
		Servidor.getClientesConectados().remove(this);
		Paquete paquete = (Paquete) gson.fromJson(cadenaLeida, Paquete.class);
		// Indico que se desconecto
		Servidor.log.append(paquete.getIp() + " se ha desconectado." + System.lineSeparator());
	}
}