package comandos;

import java.io.IOException;

import estados.Estado;
import mensajeria.PaqueteFinalizarBatalla;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Clase que admnistra el finalizado de la batalla. <br>
 */
public class FinalizarBatalla extends ComandosServer {
	/**
	 * Ejecuta el finaliza de la batalla.
	 * <p>
	 * <i>En caso de que no se pueda finalizar la batalla, se avisa.</i> <br>
	 */
	@Override
	public void ejecutar() {
		PaqueteFinalizarBatalla paqueteFinalizarBatalla = 
				(PaqueteFinalizarBatalla) gson.fromJson(cadenaLeida,
				PaqueteFinalizarBatalla.class);
		escuchaCliente.setPaqueteFinalizarBatalla(paqueteFinalizarBatalla);
		Servidor.getConector().actualizarInventario(paqueteFinalizarBatalla
				.getGanadorBatalla());
		Servidor.getPersonajesConectados().get(escuchaCliente
				.getPaqueteFinalizarBatalla().getId())
				.setEstado(Estado.estadoJuego);
		if (escuchaCliente.getPaqueteFinalizarBatalla().getIdEnemigo() < 0)
			Servidor.getEnemigosConectados().get(escuchaCliente.getPaqueteFinalizarBatalla().getIdEnemigo() * -1 + 1)
			.setEstado(Estado.estadoJuego);
		else
		{
			Servidor.getPersonajesConectados().get(escuchaCliente.getPaqueteFinalizarBatalla().getIdEnemigo())
				.setEstado(Estado.estadoJuego);
			for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
				if (conectado.getIdPersonaje() == escuchaCliente.getPaqueteFinalizarBatalla().getIdEnemigo()) {
					try {
						conectado.getSalida().writeObject(gson.toJson(escuchaCliente.getPaqueteFinalizarBatalla()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Servidor.log.append("Falló al intentar enviar finalizarBatalla a:"
								+ conectado.getPaquetePersonaje().getId() + "\n");
					}
				}
			}
		}
		synchronized (Servidor.atencionConexiones) {
			Servidor.atencionConexiones.notify();
		}
	}
}
