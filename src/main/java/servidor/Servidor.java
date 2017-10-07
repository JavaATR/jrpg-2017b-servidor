package servidor;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import mensajeria.PaqueteEnemigo;
import mensajeria.PaqueteMensaje;
import mensajeria.PaqueteMovimiento;
import mensajeria.PaquetePersonaje;

/**
 * Clase que administra el servidor. <br>
 */
public class Servidor extends Thread {
	/**
	 * Clientes conectados en el servidor. <br>
	 */
	private static ArrayList<EscuchaCliente> clientesConectados = new ArrayList<>();
	/**
	 * Ubicación de los personajes en el juego. <br>
	 */
	private static Map<Integer, PaqueteMovimiento> ubicacionPersonajes = new HashMap<>();
	/**
	 * Personajes conectados en el juego. <br>
	 */
	private static Map<Integer, PaquetePersonaje> personajesConectados = new HashMap<>();
	/**
	 * Enemigos conectados. <br>
	 */
	private static Map<Integer, PaqueteEnemigo> enemigosConectados = new HashMap<>();
	/**
	 * Ubicación de los enemigos. <br>
	 */
	private static Map<Integer, PaqueteMovimiento> ubicacionEnemigos = new HashMap<>();
	/**
	 * Hilo principal del servidor. <br>
	 */
	private static Thread server;
	/**
	 * Socket del server. <br>
	 */
	private static ServerSocket serverSocket;
	/**
	 * Conector con la base de datos del juego. <br>
	 */
	private static Conector conexionDB;
	/**
	 * Puerto del juego. <br>
	 */
	private final int PUERTO = 55050;
	/**
	 * Ancho de la pantalla de log. <br>
	 */
	private final static int ANCHO = 700;
	/**
	 * Alto de la pantalla de log. <br>
	 */
	private final static int ALTO = 640;
	/**
	 * Ancho de la pantalla de dialogo del log. <br>
	 */
	private final static int ALTO_LOG = 520;
	/**
	 * Ancho de la pantalla de dialogo del log. <br>
	 */
	private final static int ANCHO_LOG = ANCHO - 25;
	/**
	 * Cuadro del log. <br>
	 */
	public static JTextArea log;
	/**
	 * Escucha de conexiones general. <br>
	 */
	public static AtencionConexiones atencionConexiones;
	/**
	 * Escucha de conexiones general. <br>
	 */
	public static AtencionEnemigos atencionEnemigos;
	/**
	 * Escucha de movimientos general. <br>
	 */
	public static AtencionMovimientos atencionMovimientos;

	/**
	 * Carga la interfaz visual del servidor. <br>
	 * 
	 * @param args
	 *            argumentos. <br>
	 */
	public static void main(String[] args) {
		cargarInterfaz();
	}

	/**
	 * Carga la interfaz visual del servidor.
	 * <p>
	 * En ella se muestra el log del servidor. <br>
	 */
	private static void cargarInterfaz() {
		JFrame ventana = new JFrame("Servidor WOME");
		ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ventana.setSize(ANCHO, ALTO);
		ventana.setResizable(false);
		ventana.setLocationRelativeTo(null);
		ventana.setLayout(null);
		ventana.setIconImage(Toolkit.getDefaultToolkit().getImage("src/main/java/servidor/server.png"));
		JLabel titulo = new JLabel("Log del servidor...");
		titulo.setFont(new Font("Courier New", Font.BOLD, 16));
		titulo.setBounds(10, 0, 200, 30);
		ventana.add(titulo);

		log = new JTextArea();
		log.setEditable(false);
		log.setFont(new Font("Times New Roman", Font.PLAIN, 13));
		JScrollPane scroll = new JScrollPane(log, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBounds(10, 40, ANCHO_LOG, ALTO_LOG);
		ventana.add(scroll);

		final JButton botonIniciar = new JButton();
		final JButton botonDetener = new JButton();
		botonIniciar.setText("Iniciar");
		botonIniciar.setBounds(220, ALTO - 70, 100, 30);
		botonIniciar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				server = new Thread(new Servidor());
				server.start();
				botonIniciar.setEnabled(false);
				botonDetener.setEnabled(true);
			}
		});

		ventana.add(botonIniciar);

		botonDetener.setText("Detener");
		botonDetener.setBounds(360, ALTO - 70, 100, 30);
		botonDetener.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					server.stop();
					atencionConexiones.stop();
					atencionEnemigos.stop();
					atencionMovimientos.stop();
					for (EscuchaCliente cliente : clientesConectados) {
						cliente.getSalida().close();
						cliente.getEntrada().close();
						cliente.getSocket().close();
					}
					serverSocket.close();
					log.append("El servidor se ha detenido." + System.lineSeparator());
				} catch (IOException e1) {
					log.append("Fallo al intentar detener el servidor." + System.lineSeparator());
				}
				if (conexionDB != null)
					conexionDB.close();
				botonDetener.setEnabled(false);
				botonIniciar.setEnabled(true);
			}
		});
		botonDetener.setEnabled(false);
		ventana.add(botonDetener);

		ventana.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		ventana.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				if (serverSocket != null) {
					try {
						server.stop();
						atencionConexiones.stop();
						atencionEnemigos.stop();
						atencionMovimientos.stop();
						for (EscuchaCliente cliente : clientesConectados) {
							cliente.getSalida().close();
							cliente.getEntrada().close();
							cliente.getSocket().close();
						}
						serverSocket.close();
						log.append("El servidor se ha detenido." + System.lineSeparator());
					} catch (IOException e) {
						log.append("Fallo al intentar detener el servidor." + System.lineSeparator());
						System.exit(1);
					}
				}
				if (conexionDB != null)
					conexionDB.close();
				System.exit(0);
			}
		});

		ventana.setVisible(true);
	}

	/**
	 * Ejecuta el servidor. <br>
	 */
	public void run() {
		try {
			conexionDB = new Conector();
			conexionDB.connect();

			log.append("Iniciando el servidor..." + System.lineSeparator());
			serverSocket = new ServerSocket(PUERTO);
			log.append("Servidor esperando conexiones..." + System.lineSeparator());
			String ipRemota;

			atencionConexiones = new AtencionConexiones();
			atencionEnemigos = new AtencionEnemigos();
			atencionMovimientos = new AtencionMovimientos();

			atencionConexiones.start();
			atencionEnemigos.start();
			atencionMovimientos.start();
			
			generarEnemigos();

			while (true) {
				Socket cliente = serverSocket.accept();
				ipRemota = cliente.getInetAddress().getHostAddress();
				log.append(ipRemota + " se ha conectado" + System.lineSeparator());

				ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
				ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());

				EscuchaCliente atencion = new EscuchaCliente(ipRemota, cliente, entrada, salida);
				atencion.start();
				clientesConectados.add(atencion);
			}
		} catch (Exception e) {
			log.append("Fallo la conexión." + System.lineSeparator());
		}
	}

	/**
	 * Envía un mensaje a un cliente. <br>
	 * 
	 * @param pqm
	 *            Paquete de mensaje del cliente al que se le envía el mensaje.
	 *            <br>
	 * @return <b>true</b> si se lo logró envíar.<br>
	 *         <b>false</b> si se encuentra desconectado. <br>
	 */
	public static boolean mensajeAUsuario(PaqueteMensaje pqm) {
		boolean result = true;
		boolean noEncontro = true;
		for (Map.Entry<Integer, PaquetePersonaje> personaje : personajesConectados.entrySet()) {
			if (noEncontro && (!personaje.getValue().getNombre().equals(pqm.getUserReceptor()))) {
				result = false;
			} else {
				result = true;
				noEncontro = false;
			}
		}
		// Si existe inicio sesion
		if (result) {
			Servidor.log
					.append(pqm.getUserEmisor() + " envió mensaje a " + pqm.getUserReceptor() + System.lineSeparator());
			return true;
		} else {
			// Si no existe informo y devuelvo false
			Servidor.log.append("El mensaje para " + pqm.getUserReceptor()
					+ " no se envió, ya que se encuentra desconectado." + System.lineSeparator());
			return false;
		}
	}

	/**
	 * Envía un mensaje a todos los usuarios. <br>
	 * 
	 * @param contador
	 *            Contador de personajes conectados. <br>
	 * @return <b>true</b> si se le envío el mensaje a todos los usuarios.<br>
	 *         <b>false</b> si se lo envío a casi todos o a los que se pudo.
	 *         <br>
	 */
	public static boolean mensajeAAll(int contador) {
		boolean result = true;
		if (personajesConectados.size() != contador + 1) {
			result = false;
		}
		// Si existe inicio sesion
		if (result) {
			Servidor.log.append("Se ha enviado un mensaje a todos los usuarios" + System.lineSeparator());
			return true;
		} else {
			// Si no existe informo y devuelvo false
			Servidor.log
					.append("Uno o más de todos los usuarios se ha desconectado, se ha mandado el mensaje a los demas."
							+ System.lineSeparator());
			return false;
		}
	}

	/**
	 * Devuelve una lista con los clientes conectados. <br>
	 * 
	 * @return Clientes conectados. <br>
	 */
	public static ArrayList<EscuchaCliente> getClientesConectados() {
		return clientesConectados;
	}

	/**
	 * Devuelve una lista con la ubicación de los personajes. <br>
	 * 
	 * @return Ubicación de los personajes. <br>
	 */
	public static Map<Integer, PaqueteMovimiento> getUbicacionPersonajes() {
		return ubicacionPersonajes;
	}

	/**
	 * Devuelve una lista con los personajes conectados. <br>
	 * 
	 * @return Personajes conectados. <br>
	 */
	public static Map<Integer, PaquetePersonaje> getPersonajesConectados() {
		return personajesConectados;
	}

	/**
	 * Devuelve una lista con la ubicación de los enemigos. <br>
	 * 
	 * @return Ubicación de los enemigos. <br>
	 */
	public static Map<Integer, PaqueteMovimiento> getUbicacionEnemigos() {
		return ubicacionEnemigos;
	}

	/**
	 * Devuelve una lista de los enemigos conectados. <br>
	 * 
	 * @return Enemigos conectados. <br>
	 */
	public static Map<Integer, PaqueteEnemigo> getEnemigosConectados() {
		return enemigosConectados;
	}

	/**
	 * Devuelve el conector a la base de datos. <br>
	 * 
	 * @return Conector a la base de datos. <br>
	 */
	public static Conector getConector() {
		return conexionDB;
	}
	

	
	public void generarEnemigos() {
		generateBryans();
	}
	
	private void generateBryans() {
		Random randomGenerator = new Random(); // Random generator para las posiciones
		Integer i = 0;
		PaqueteEnemigo bryans[] = new PaqueteEnemigo[10]; // Creo array de 10 Bryans y otro para sus posiciones
		PaqueteMovimiento posicionesBryans[] = new PaqueteMovimiento[10];
		enemigosConectados = new HashMap<Integer, PaqueteEnemigo>();
		ubicacionEnemigos = new HashMap<Integer, PaqueteMovimiento>();
		
		for(i=0; i<bryans.length; i++) {
			bryans[i] = new PaqueteEnemigo();
			posicionesBryans[i] = new PaqueteMovimiento(0, 100 + (i * 40), 150 + (i * 20)); // TODO: Generacion de posiciones
			enemigosConectados.put(i, bryans[i]); // Paso los arrays a hashmaps
			ubicacionEnemigos.put(i, posicionesBryans[i]);
		}
		
//		setEnemigosConectados(enemigosConectados); // Inserto los Bryans al juego usando los hashmaps
//		setUbicacionEnemigos(ubicacionEnemigos);
	}
}