package servidor;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
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
	 * Ubicación posibles para la regeneración de los enemigos. <br>
	 */
	private static int[][] ubicacionesPosiblesEnemigos = {{160, 353, 225, -577, 97, 609, 1057, 959, 607, 192, -225, -415, -1055, -255, 511, 223, 287, 284, 287, 283, 282},
															{273, 975, 1360, 1567, 1872, 1935, 1711, 1535, 1520, 1216, 1072, 752, 559, 160, 319, 367, 1135, 1137, 1487, 1491, 1484}};
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
	 * Defino de donde se saca el puerto del juego. <br>
	 */
	Properties prop = new Properties();
	{
		try {
			prop.load(new FileInputStream("puerto.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Puerto del juego. <br>
	 */

	private final int puerto = Integer.valueOf(prop.getProperty("puerto"));
	/**
	 * Ancho de la pantalla de log. <br>
	 */
	private static final int ANCHO = 700;
	/**
	 * Alto de la pantalla de log. <br>
	 */
	private static final int ALTO = 640;
	/**
	 * Ancho de la pantalla de dialogo del log. <br>
	 */
	private static final int ALTO_LOG = 520;
	/**
	 * Ancho de la pantalla de dialogo del log. <br>
	 */
	private static final int ANCHO_LOG = ANCHO - 25;
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
	 * Tamaño del font del título. <br>
	 */
	private static final int TAMANOFONTTITULO = 16;
	/**
	 * Tamaño del font de los mensajes del log. <br>
	 */
	private static final int TAMANO_FONT_LOG = 13;
	/**
	 * Bound X de la pantalla. <br>
	 */
	private static final int BOUND_X_TITULO = 10;
	/**
	 * Bound Y de la pantalla. <br>
	 */
	private static final int BOUND_Y_TITULO = 0;
	/**
	 * Ancho de la pantalla. <br>
	 */
	private static final int WIDTH_TITULO = 200;
	/**
	 * Alto de la pantalla. <br>
	 */
	private static final int HEIGHT_TITULO = 30;
	/**
	 * Bound X de la pantalla. <br>
	 */
	private static final int BOUND_X_LOG = 10;
	/**
	 * Bound Y de la pantalla. <br>
	 */
	private static final int BOUND_Y_LOG = 40;
	/**
	 * Bound X del botón iniciar. <br>
	 */
	private static final int BOUND_X_BOTON_INICIAR = 220;
	/**
	 * Bound X del botón detener. <br>
	 */
	private static final int BOUND_X_BOTON_DETENER = 360;
	/**
	 * Bound Y del botón. <br>
	 */
	private static final int BOUND_Y_BOTON = ALTO - 70;
	/**
	 * Ancho deL botón. <br>
	 */
	private static final int WIDTH_BOTON = 100;
	/**
	 * Alto del botón. <br>
	 */
	private static final int HEIGHT_BOTON = 30;
	/**
	 * Posición X inicial del enemigo. <br>
	 */
	private static final int POSXBRYAN = 100;
	/**
	 * Variante adicional para la posición X del enemigo. <br>
	 */
	private static final int POSXIBRYAN = 40;
	/**
	 * Posición Y inicial del enemigo. <br>
	 */
	private static final int POSYBRYAN = 150;
	/**
	 * Variante adicional para la posición X del enemigo. <br>
	 */
	private static final int POSYIBRYAN = 20;

	/**
	 * Carga la interfaz visual del servidor. <br>
	 *
	 * @param args
	 *            argumentos. <br>
	 */
	public static void main(final String[] args) {
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
		titulo.setFont(new Font("Courier New", Font.BOLD, TAMANOFONTTITULO));
		titulo.setBounds(BOUND_X_TITULO, BOUND_Y_TITULO, WIDTH_TITULO, HEIGHT_TITULO);
		ventana.add(titulo);
		log = new JTextArea();
		log.setEditable(false);
		log.setFont(new Font("Times New Roman", Font.PLAIN, TAMANO_FONT_LOG));
		JScrollPane scroll = new JScrollPane(log, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBounds(BOUND_X_LOG, BOUND_Y_LOG, ANCHO_LOG, ALTO_LOG);
		ventana.add(scroll);
		final JButton botonIniciar = new JButton();
		final JButton botonDetener = new JButton();
		botonIniciar.setText("Iniciar");
		botonIniciar.setBounds(BOUND_X_BOTON_INICIAR, BOUND_Y_BOTON, WIDTH_BOTON, HEIGHT_BOTON);
		botonIniciar.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				server = new Thread(new Servidor());
				server.start();
				botonIniciar.setEnabled(false);
				botonDetener.setEnabled(true);
			}
		});
		ventana.add(botonIniciar);
		botonDetener.setText("Detener");
		botonDetener.setBounds(BOUND_X_BOTON_DETENER, BOUND_Y_BOTON, WIDTH_BOTON, HEIGHT_BOTON);
		botonDetener.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
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
				if (conexionDB != null) {
					conexionDB.close();
				}
				botonDetener.setEnabled(false);
				botonIniciar.setEnabled(true);
			}
		});
		botonDetener.setEnabled(false);
		ventana.add(botonDetener);
		ventana.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		ventana.addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent evt) {
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
				if (conexionDB != null) {
					conexionDB.close();
				}
				System.exit(0);
			}
		});
		ventana.setVisible(true);
	}

	/**
	 * Ejecuta el servidor. <br>
	 */
	public final void run() {
		try {
			conexionDB = new Conector();
			conexionDB.connect();
			log.append("Iniciando el servidor..." + System.lineSeparator());
			serverSocket = new ServerSocket(puerto);
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
	 *            Paquete de mensaje del cliente al que se le envía el mensaje. <br>
	 * @return <b>true</b> si se lo logró envíar.<br>
	 *         <b>false</b> si se encuentra desconectado. <br>
	 */
	public static boolean mensajeAUsuario(final PaqueteMensaje pqm) {
		Iterator<Map.Entry<Integer, PaquetePersonaje>> iterator = personajesConectados.entrySet().iterator();
		Map.Entry<Integer, PaquetePersonaje> entry = iterator.next();
		while (iterator.hasNext() && !entry.getValue().getNombre().equals(pqm.getUserReceptor())) {
			entry = iterator.next();
		}
		// Si existe inicio sesion
		if (entry.getValue().getNombre().equals(pqm.getUserReceptor())) {
			Servidor.log
					.append(pqm.getUserEmisor() + " envió mensaje a " + pqm.getUserReceptor() + System.lineSeparator());
			return true;
		}
		// Si no existe informo y devuelvo false
		Servidor.log.append("El mensaje para " + pqm.getUserReceptor()
				+ " no se envió, ya que se encuentra desconectado." + System.lineSeparator());
		return false;
	}

	/**
	 * Envía un mensaje a todos los usuarios. <br>
	 *
	 * @param contador
	 *            Contador de personajes conectados. <br>
	 * @return <b>true</b> si se le envío el mensaje a todos los usuarios.<br>
	 *         <b>false</b> si se lo envío a casi todos o a los que se pudo. <br>
	 */
	public static boolean mensajeAAll(final int contador) {
		// Compruebo que estén todos conectados.
		if (personajesConectados.size() == contador + 1) {
			Servidor.log.append("Se ha enviado un mensaje a todos los usuarios" + System.lineSeparator());
			return true;
		}
		// Si no existe informo y devuelvo false
		Servidor.log.append("Uno o más de todos los usuarios se ha desconectado, se ha mandado el mensaje a los demas."
				+ System.lineSeparator());
		return false;
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

	/**
	 * Genera los npc. <br>
	 */
	public final void generarEnemigos() {
		Random randomGenerator = new Random(); // Random generator para las	posiciones
		Integer i = 0, id = -1;
		PaqueteEnemigo bryans[] = new PaqueteEnemigo[10]; // Creo array de 10 Bryans y otro para sus posiciones
		PaqueteMovimiento posicionesBryans[] = new PaqueteMovimiento[10];
		enemigosConectados = new HashMap<Integer, PaqueteEnemigo>();
		ubicacionEnemigos = new HashMap<Integer, PaqueteMovimiento>();
		for (i = 0; i < bryans.length; i++) {
			bryans[i] = new PaqueteEnemigo(id);
			posicionesBryans[i] = generarPosicion(id);
			enemigosConectados.put(i, bryans[i]); // Paso los arrays a hashmaps
			ubicacionEnemigos.put(i, posicionesBryans[i]);
			id--;
		}
	}
	
	public static PaqueteMovimiento generarPosicion(int id) {
		PaqueteMovimiento posicionBryan = new PaqueteMovimiento();		
		Random randomCoordinates = new Random();
		int indiceUbicacion = 0;
		Boolean posicionOcupada;
		
		do {
			posicionOcupada = false;
			
			indiceUbicacion = randomCoordinates.nextInt(ubicacionesPosiblesEnemigos[0].length);
			
			posicionBryan.setPosX(ubicacionesPosiblesEnemigos[0][indiceUbicacion]);
			posicionBryan.setPosY(ubicacionesPosiblesEnemigos[1][indiceUbicacion]);
			
			for(Map.Entry<Integer, PaqueteMovimiento> entry : ubicacionEnemigos.entrySet()) {
			    PaqueteMovimiento value = entry.getValue();

			    if (value.getPosX() == posicionBryan.getPosX())
			    	posicionOcupada = true;
			}
		} while (posicionOcupada);
		
		posicionBryan.setIdPersonaje(id);
		
		return posicionBryan;
	}
	
	public static void regenerarBryan(int id) {
		PaqueteMovimiento posicionNuevoBryan;
		
		do {
			posicionNuevoBryan = generarPosicion(id);
			
			ubicacionEnemigos.put(id * -1 -1, posicionNuevoBryan);
		} while (isInPlayersVision(posicionNuevoBryan.getPosX(), posicionNuevoBryan.getPosY()));
	}
	
	private static Boolean isInPlayersVision(float x, float y)
	{
		Boolean isInPlayersVision = false;
	    float diferenciaX, diferenciaY;
		
		for(Map.Entry<Integer, PaqueteMovimiento> entry : ubicacionPersonajes.entrySet()) {
		    PaqueteMovimiento value = entry.getValue();
		    
		    diferenciaX = value.getPosX() - x;
		    diferenciaY = value.getPosY() - y;

		    // Los Bryan se encuentran en nuestro rango de visión si se cumple lo siguiente, calculado usando trigonometría
		    if (Math.sqrt((double)(Math.abs(diferenciaX * diferenciaX) + Math.abs(diferenciaY * diferenciaX))) < 464)
		    	isInPlayersVision = true;
		}
		
		return isInPlayersVision;
	}
}
