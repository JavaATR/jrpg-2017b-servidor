package comandos;

import mensajeria.PaqueteMovimiento;
import servidor.Servidor;

/**
 * Clase que administra el movimiento y la ubicación de los clientes. <br>
 */
public class Movimiento extends ComandosServer {
	/**
	 * Ejecuta la obtención de la ubicación de los personajes de los otros
	 * usuarios y su movimiento.<br>
	 */
	@Override
	public void ejecutar() {
		escuchaCliente.setPaqueteMovimiento(
				(PaqueteMovimiento) (gson.fromJson((String) cadenaLeida, 
						PaqueteMovimiento.class)));
		Servidor.getUbicacionPersonajes().get(escuchaCliente
				.getPaqueteMovimiento().getIdPersonaje())
				.setPosX(escuchaCliente.getPaqueteMovimiento().getPosX());
		Servidor.getUbicacionPersonajes().get(escuchaCliente
				.getPaqueteMovimiento().getIdPersonaje())
				.setPosY(escuchaCliente.getPaqueteMovimiento().getPosY());
		Servidor.getUbicacionPersonajes().get(escuchaCliente
				.getPaqueteMovimiento().getIdPersonaje())
				.setDireccion(escuchaCliente.getPaqueteMovimiento()
						.getDireccion());
		Servidor.getUbicacionPersonajes().get(escuchaCliente.
				getPaqueteMovimiento().getIdPersonaje())
				.setFrame(escuchaCliente.getPaqueteMovimiento().getFrame());
		synchronized (Servidor.atencionMovimientos) {
			Servidor.atencionMovimientos.notify();
		}
	}
}
