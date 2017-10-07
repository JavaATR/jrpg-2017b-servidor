package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.google.gson.Gson;

import comandos.ComandosServer;
import mensajeria.Comando;
import mensajeria.Paquete;
import mensajeria.PaqueteAtacar;
import mensajeria.PaqueteBatalla;
import mensajeria.PaqueteDeEnemigos;
import mensajeria.PaqueteDeMovimientos;
import mensajeria.PaqueteDePersonajes;
import mensajeria.PaqueteEnemigo;
import mensajeria.PaqueteFinalizarBatalla;
import mensajeria.PaqueteMovimiento;
import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;

/**
 * Clase que administra la escucha del cliente. <br>
 */
public class EscuchaCliente extends Thread {
	/**
	 * Socket
	 */
	private final Socket socket;
	/**
	 * Entrada. <br>
	 */
	private final ObjectInputStream entrada;
	/**
	 * Salida. <br>
	 */
	private final ObjectOutputStream salida;
	/**
	 * ID del personaje. <br>
	 */
	private int idPersonaje;
	/**
	 * ID del enemigo. <br>
	 */
	private int idEnemigo;
	/**
	 * Gson. <br>
	 */
	private final Gson gson = new Gson();
	/**
	 * Paquete de personaje del cliente. <br>
	 */
	private PaquetePersonaje paquetePersonaje;
	/**
	 * Paquete de enemigo del cliente. <br>
	 */
	private PaqueteEnemigo paqueteEnemigo;
	/**
	 * Paquete de movimiento del cliente. <br>
	 */
	private PaqueteMovimiento paqueteMovimiento;
	/**
	 * Paquete de batalla del cliente. <br>
	 */
	private PaqueteBatalla paqueteBatalla;
	/**
	 * Paquete de atacar del cliente. <br>
	 */
	private PaqueteAtacar paqueteAtacar;
	/**
	 * Paquete de finalizar batalla del cliente. <br>
	 */
	private PaqueteFinalizarBatalla paqueteFinalizarBatalla;
	/**
	 * Paquete de usuario del cliente. <br>
	 */
	private PaqueteUsuario paqueteUsuario;
	/**
	 * Paquete de movimiento del cliente. <br>
	 */
	private PaqueteDeMovimientos paqueteDeMovimiento;
	/**
	 * Paquete de personajes del cliente. <br>
	 */
	private PaqueteDePersonajes paqueteDePersonajes;
	/**
	 * Paquete de enemigos del cliente. <br>
	 */
	private PaqueteDeEnemigos paqueteDeEnemigos;

	/**
	 * Crea un escucha del cliente. <br>
	 * 
	 * @param ip
	 *            IP del cliente. <br>
	 * @param socket
	 *            Socket del cliente. <br>
	 * @param entrada
	 *            Entrada. <br>
	 * @param salida
	 *            Salida. <br>
	 * @throws IOException
	 *             En caso de error, sale. <br>
	 */
	public EscuchaCliente(final String ip, final Socket socket, final ObjectInputStream entrada,
			final ObjectOutputStream salida) throws IOException {
		this.socket = socket;
		this.entrada = entrada;
		this.salida = salida;
		paquetePersonaje = new PaquetePersonaje();
	}

	/**
	 * Conecta al cliente al juego. Lo desconecta una vez finalizada su
	 * conexión. <br>
	 */
	public void run() {
		try {
			ComandosServer comand;
			Paquete paquete;
			Paquete paqueteSv = new Paquete(null, 0);
			paqueteUsuario = new PaqueteUsuario();

			String cadenaLeida = (String) entrada.readObject();

			while (!((paquete = gson.fromJson(cadenaLeida, Paquete.class)).getComando() == Comando.DESCONECTAR)) {
				System.out.println(cadenaLeida);
				
				comand = (ComandosServer) paquete.getObjeto(Comando.NOMBREPAQUETE);
				comand.setCadena(cadenaLeida);
				comand.setEscuchaCliente(this);
				comand.ejecutar();
				cadenaLeida = (String) entrada.readObject();
			}
			
			entrada.close();
			salida.close();
			socket.close();

			Servidor.getPersonajesConectados().remove(paquetePersonaje.getId());
			Servidor.getUbicacionPersonajes().remove(paquetePersonaje.getId());
			Servidor.getClientesConectados().remove(this);

			for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
				paqueteDePersonajes = new PaqueteDePersonajes(Servidor.getPersonajesConectados());
				paqueteDePersonajes.setComando(Comando.CONEXION);
				conectado.salida.writeObject(gson.toJson(paqueteDePersonajes, PaqueteDePersonajes.class));
			}

			Servidor.log.append(paquete.getIp() + " se ha desconectado." + System.lineSeparator());

		} catch (IOException | ClassNotFoundException e) {
			Servidor.log.append("Error de conexion: " + e.getMessage() + System.lineSeparator());
		}
	}

	/**
	 * Devuelve el socket del cliente. <br>
	 * 
	 * @return Socket del cliente. <br>
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * Devuelve la entrada. <br>
	 * 
	 * @return Entrada. <br>
	 */
	public ObjectInputStream getEntrada() {
		return entrada;
	}

	/**
	 * Devuelve la salida. <br>
	 * 
	 * @return Salida. <br>
	 */
	public ObjectOutputStream getSalida() {
		return salida;
	}

	/**
	 * Obtiene el personaje del cliente. <br>
	 * 
	 * @return Personaje del cliente. <br>
	 */
	public PaquetePersonaje getPaquetePersonaje() {
		return paquetePersonaje;
	}
	
	/**
	 * Obtiene el enemigo del cliente. <br>
	 * 
	 * @return Enemigo del cliente. <br>
	 */
	public PaqueteEnemigo getPaqueteEnemigo() {
		return paqueteEnemigo;
	}

	/**
	 * Devuelve el ID del personaje del cliente. <br>
	 * 
	 * @return ID del personaje. <br>
	 */
	public int getIdPersonaje() {
		return idPersonaje;
	}
	
	/**
	 * Devuelve el ID del enemigo del cliente. <br>
	 * 
	 * @return ID del enemigo. <br>
	 */
	public int getIdEnemigo() {
		return idEnemigo;
	}

	/**
	 * Devuelve los movimientos del personaje. <br>
	 * 
	 * @return Movimientos del personaje. <br>
	 */
	public PaqueteMovimiento getPaqueteMovimiento() {
		return paqueteMovimiento;
	}

	/**
	 * Establece los movimiento del personaje. <br>
	 * 
	 * @param paqueteMovimiento
	 *            Movimientos del personaje. <br>
	 */
	public void setPaqueteMovimiento(final PaqueteMovimiento paqueteMovimiento) {
		this.paqueteMovimiento = paqueteMovimiento;
	}

	/**
	 * Devuelve la batalla del cliente. <br>
	 * 
	 * @return Batalla del cliente. <br>
	 */
	public PaqueteBatalla getPaqueteBatalla() {
		return paqueteBatalla;
	}

	/**
	 * Establece la batalla del cliente. <br>
	 * 
	 * @param paqueteBatalla
	 *            Batalla del cliente. <br>
	 */
	public void setPaqueteBatalla(final PaqueteBatalla paqueteBatalla) {
		this.paqueteBatalla = paqueteBatalla;
	}

	/**
	 * Devuelve el ataque del personaje. <br>
	 * 
	 * @return Ataque del personaje. <br>
	 */
	public PaqueteAtacar getPaqueteAtacar() {
		return paqueteAtacar;
	}

	/**
	 * Establece el ataque del personaje. <br>
	 * 
	 * @param paqueteAtacar
	 *            Ataque del personaje. <br>
	 */
	public void setPaqueteAtacar(final PaqueteAtacar paqueteAtacar) {
		this.paqueteAtacar = paqueteAtacar;
	}

	/**
	 * Devuelve el finaliado de batalla del personaje. <br>
	 * 
	 * @return Finalizado de batalla del personaje. <br>
	 */
	public PaqueteFinalizarBatalla getPaqueteFinalizarBatalla() {
		return paqueteFinalizarBatalla;
	}

	/**
	 * Establece el finalizado de batalla del personaje. <br>
	 * 
	 * @param paqueteFinalizarBatalla
	 *            Finalizado de batalla del personaje. <br>
	 */
	public void setPaqueteFinalizarBatalla(final PaqueteFinalizarBatalla paqueteFinalizarBatalla) {
		this.paqueteFinalizarBatalla = paqueteFinalizarBatalla;
	}

	/**
	 * Devuelve los movimiento y ubicación de los demás personajes con respecto
	 * al del cliente. <br>
	 * 
	 * @return Movimiento y ubicación de los demás personajes. <br>
	 */
	public PaqueteDeMovimientos getPaqueteDeMovimiento() {
		return paqueteDeMovimiento;
	}

	/**
	 * Establece el movimiento y ubicación de los demás personajes con respecto
	 * al del cliente. <br>
	 * 
	 * @param paqueteDeMovimiento
	 *            Movimiento y ubicación de los demás personajes. <br>
	 */
	public void setPaqueteDeMovimiento(final PaqueteDeMovimientos paqueteDeMovimiento) {
		this.paqueteDeMovimiento = paqueteDeMovimiento;
	}

	/**
	 * Devuelve información con respecto a los otros personajes. <br>
	 * 
	 * @return Otros personajes. <br>
	 */
	public PaqueteDePersonajes getPaqueteDePersonajes() {
		return paqueteDePersonajes;
	}

	/**
	 * Establece información sobre los otros personajes. <br>
	 * 
	 * @param paqueteDePersonajes
	 *            Otros personajes. <br>
	 */
	public void setPaqueteDePersonajes(final PaqueteDePersonajes paqueteDePersonajes) {
		this.paqueteDePersonajes = paqueteDePersonajes;
	}

	/**
	 * Establece el ID del personaje. <br>
	 * 
	 * @param idPersonaje
	 *            ID del personaje. <br>
	 */
	public void setIdPersonaje(final int idPersonaje) {
		this.idPersonaje = idPersonaje;
	}

	/**
	 * Establece el personaje del cliente. <br>
	 * 
	 * @param paquetePersonaje
	 *            Personaje. <br>
	 */
	public void setPaquetePersonaje(final PaquetePersonaje paquetePersonaje) {
		this.paquetePersonaje = paquetePersonaje;
	}
	
	/**
	 * Establece el enemigo del cliente. <br>
	 * 
	 * @param paqueteEnemigo
	 *            Enemigo. <br>
	 */
	public void setPaqueteEnemigo(final PaqueteEnemigo paqueteEnemigo) {
		this.paqueteEnemigo = paqueteEnemigo;
	}
	
	/**
	 * Devuelve información con respecto a los enemigos. <br>
	 */
	public PaqueteDeEnemigos getPaqueteDeEnemigos() {
		return paqueteDeEnemigos;
	}

	/**
	 * Establece información sobre los enemigos. <br>
	 */
	public void setPaqueteDeEnemigos(final PaqueteDeEnemigos paqueteDeEnemigos) {
		this.paqueteDeEnemigos = paqueteDeEnemigos;
	}

	/**
	 * Devuelve el usuario del cliente. <br>
	 * 
	 * @return Usuario del cliente. <br>
	 */
	public PaqueteUsuario getPaqueteUsuario() {
		return paqueteUsuario;
	}

	/**
	 * Establece el usuario del cliente. <br>
	 * 
	 * @param paqueteUsuario
	 *            Usuario. <br>
	 */
	public void setPaqueteUsuario(final PaqueteUsuario paqueteUsuario) {
		this.paqueteUsuario = paqueteUsuario;
	}
}