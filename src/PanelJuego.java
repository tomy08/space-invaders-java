
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

public class PanelJuego extends JPanel implements ActionListener, KeyListener {
	public static final int ANCHO = 800;
	public static final int ALTO = 600;

	private Timer reloj;
	private Jugador jugador;
	private ArrayList<Bala> balas = new ArrayList<>();
	private ArrayList<Enemigo> enemigos = new ArrayList<>();
	private ArrayList<BalaEnemiga> balasEnemigas = new ArrayList<>();

	// controles
	private boolean izq = false, der = false, espacio = false;
	// estado
	private int puntos = 0;
	private int nivel = 1;
	private boolean juegoTerminado = false;

	private Random rnd = new Random();

	// Control de disparo jugador
	private long ultimoDisparoJugador = 0;
	private final long COOLDOWN_JUGADOR = 500; // 300 ms entre disparos

	// Control de kamikaze: solo 1 ataca a la vez
	private Enemigo kamikazeAtacante = null;
	private long proximoKamikazeTime = 0;

	public PanelJuego() {
		setPreferredSize(new Dimension(ANCHO, ALTO));
		setBackground(Color.BLACK);
		setFocusable(true);
		addKeyListener(this);

		jugador = new Jugador(ANCHO / 2 - 20, ALTO - 70);
		jugador.vidas = 3; // asegúrate que Jugador tenga el campo 'vidas'

		generarNivel(nivel);

		reloj = new Timer(16, this);
		reloj.start();
	}

	// Genera enemigos según el número de nivel y regula la proporción de tipos.
	private void generarNivel(int n) {
		enemigos.clear();
		balas.clear();
		balasEnemigas.clear();
		kamikazeAtacante = null;

		int filas = 3 + n / 2; // aumenta de a poco
		int cols = 8;

		for (int f = 0; f < filas; f++) {
			for (int c = 0; c < cols; c++) {
				int x = 60 + c * 70;
				int y = 40 + f * 50;
				int tipo = Enemigo.TIPO_NORMAL;

				if (n == 1) {
					tipo = Enemigo.TIPO_NORMAL;
				} else if (n == 2) {
					// pocos shooters
					if (rnd.nextDouble() < 0.18)
						tipo = Enemigo.TIPO_SHOOTER;
				} else if (n == 3) {
					// pocos kamikazes
					if (rnd.nextDouble() < 0.18)
						tipo = Enemigo.TIPO_KAMIKAZE;
				} else {
					// nivel 4+: mezcla con menos normales según sube el nivel
					double r = rnd.nextDouble();
					double probShooter = Math.min(0.3, 0.08 + n * 0.006);
					double probKamikaze = Math.min(0.3, 0.06 + n * 0.006);
					if (r < probShooter)
						tipo = Enemigo.TIPO_SHOOTER;
					else if (r < probShooter + probKamikaze)
						tipo = Enemigo.TIPO_KAMIKAZE;
					else
						tipo = Enemigo.TIPO_NORMAL;
				}

				enemigos.add(new Enemigo(x, y, tipo, n)); // importante: constructor usa nivel
			}
		}

		// programar primer kamikaze (si hay) entre 1s y 4s (se reduce un poco con el nivel)
		long ahora = System.currentTimeMillis();
		int base = Math.max(600, 2000 - n * 50); // reduce tiempo con niveles
		proximoKamikazeTime = ahora + base + rnd.nextInt(2000);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (juegoTerminado)
			return;

		// controles
		if (izq)
			jugador.moverIzquierda();
		if (der)
			jugador.moverDerecha();

		// disparo jugador con cooldown 0.3s
		if (espacio) {
			long ahora = System.currentTimeMillis();
			if (ahora - ultimoDisparoJugador >= COOLDOWN_JUGADOR) {
				balas.add(new Bala(jugador.getCentroX() - 2, jugador.y - 10));
				ultimoDisparoJugador = ahora;
			}
		}

		// mover balas jugador
		for (Iterator<Bala> itB = balas.iterator(); itB.hasNext();) {
			Bala b = itB.next();
			b.mover();
			if (b.y + b.alto < 0)
				itB.remove();
		}

		// mover enemigos
		for (Iterator<Enemigo> itE = enemigos.iterator(); itE.hasNext();) {
			Enemigo en = itE.next();

			// si es el kamikaze que está atacando -> sigue al jugador
			if (en.atacando) {
				// si el atacante fue eliminado antes (por bala), se limpia más abajo
				// usar seguirJugador para movimiento de ataque
				en.seguirJugador(jugador);
			} else {
				en.mover();
			}

			// si el enemigo llega muy abajo y choca con jugador
			if (en.getRect().intersects(jugador.getRect())) {
				jugador.vidas--;
				itE.remove();
				if (en == kamikazeAtacante)
					kamikazeAtacante = null;
				if (jugador.vidas <= 0) {
					juegoTerminado = true;
					return;
				}
				continue;
			}

			// si el enemigo sale del panel por abajo, quitarlo (y liberar atacante)
			if (en.y > ALTO + 50) {
				if (en == kamikazeAtacante)
					kamikazeAtacante = null;
				itE.remove();
				continue;
			}

			// shooters: disparan según su temporizador interno
			if (en.tipo == Enemigo.TIPO_SHOOTER) {
				if (en.puedeDisparar()) {
					balasEnemigas.add(new BalaEnemiga(en.getCentroX() - 2, en.y + en.alto + 2));
					en.resetDisparo();
				}
			}
		}

		// elegir un kamikaze para atacar (solo 1 a la vez)
		long ahora = System.currentTimeMillis();
		if (kamikazeAtacante == null && ahora >= proximoKamikazeTime) {
			// buscar kamikazes vivos
			ArrayList<Enemigo> listaK = new ArrayList<>();
			for (Enemigo e2 : enemigos) {
				if (e2.tipo == Enemigo.TIPO_KAMIKAZE && !e2.atacando)
					listaK.add(e2);
			}
			if (!listaK.isEmpty()) {
				Enemigo elegido = listaK.get(rnd.nextInt(listaK.size()));
				elegido.atacando = true;
				kamikazeAtacante = elegido;
			}
			// programar siguiente intento (reduce con nivel)
			int base = Math.max(500, 2000 - nivel * 80);
			proximoKamikazeTime = ahora + base + rnd.nextInt(2000);
		}

		// mover balas enemigas y colisiones contra jugador
		for (Iterator<BalaEnemiga> it = balasEnemigas.iterator(); it.hasNext();) {
			BalaEnemiga be = it.next();
			be.mover();
			if (be.y > ALTO)
				it.remove();
			else if (be.getRect().intersects(jugador.getRect())) {
				it.remove();
				jugador.vidas--;
				if (jugador.vidas <= 0) {
					juegoTerminado = true;
					return;
				}
			}
		}

		// colisiones: balas del jugador vs enemigos
		for (Iterator<Bala> itB = balas.iterator(); itB.hasNext();) {
			Bala b = itB.next();
			boolean impacto = false;
			for (Iterator<Enemigo> itE = enemigos.iterator(); itE.hasNext();) {
				Enemigo en = itE.next();
				if (b.getRect().intersects(en.getRect())) {
					en.hp--;
					if (en.hp <= 0) {
						// si era el kamikaze atacante, liberar
						if (en == kamikazeAtacante)
							kamikazeAtacante = null;
						itE.remove();
						puntos += 10 + nivel * 2;
					}
					impacto = true;
					break;
				}
			}
			if (impacto)
				itB.remove();
		}

		// si ya no quedan enemigos, subir nivel (infinitos)
		if (enemigos.isEmpty()) {
			nivel++;
			generarNivel(nivel);
		}

		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// HUD
		g.setColor(Color.WHITE);
		g.setFont(new Font("Consolas", Font.PLAIN, 16));
		g.drawString("Puntos: " + puntos, 10, 20);
		g.drawString("Nivel: " + nivel, 120, 20);
		g.drawString("Vidas: " + jugador.vidas, 220, 20);

		// dibujar
		jugador.dibujar(g);
		for (Bala b : balas)
			b.dibujar(g);
		for (BalaEnemiga be : balasEnemigas)
			be.dibujar(g);
		for (Enemigo en : enemigos)
			en.dibujar(g);

		if (juegoTerminado) {
			g.setColor(new Color(0, 0, 0, 170));
			g.fillRect(0, 0, ANCHO, ALTO);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Consolas", Font.BOLD, 36));
			g.drawString("GAME OVER", ANCHO / 2 - 120, ALTO / 2 - 10);
			g.setFont(new Font("Consolas", Font.PLAIN, 18));
			g.drawString("Puntos finales: " + puntos, ANCHO / 2 - 90, ALTO / 2 + 30);
			g.drawString("Presiona ENTER para reiniciar", ANCHO / 2 - 120, ALTO / 2 + 60);
		}
	}

	// Controles
	@Override
	public void keyPressed(KeyEvent e) {
		int k = e.getKeyCode();
		if (k == KeyEvent.VK_LEFT)
			izq = true;
		if (k == KeyEvent.VK_RIGHT)
			der = true;
		if (k == KeyEvent.VK_SPACE)
			espacio = true;
		if (k == KeyEvent.VK_ENTER && juegoTerminado) {
			// reiniciar todo
			puntos = 0;
			nivel = 1;
			jugador = new Jugador(ANCHO / 2 - 20, ALTO - 70);
			jugador.vidas = 3;
			juegoTerminado = false;
			generarNivel(nivel);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int k = e.getKeyCode();
		if (k == KeyEvent.VK_LEFT)
			izq = false;
		if (k == KeyEvent.VK_RIGHT)
			der = false;
		if (k == KeyEvent.VK_SPACE)
			espacio = false;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}
