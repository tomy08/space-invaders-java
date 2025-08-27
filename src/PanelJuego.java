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
	private int nivel = 1; // aumentará indefinidamente
	private boolean juegoTerminado = false;
	private Random rnd = new Random();

	public PanelJuego() {
		setPreferredSize(new Dimension(ANCHO, ALTO));
		setBackground(Color.BLACK);
		setFocusable(true);
		addKeyListener(this);
		jugador = new Jugador(ANCHO / 2 - 20, ALTO - 70);
		generarNivel(nivel);
		reloj = new Timer(16, this);
		reloj.start();
	}
// Genera enemigos según el número de nivel.
// La dificultad aumenta con el nivel: más enemigos, más velocidad y más hp.

	private void generarNivel(int n) {
		enemigos.clear();
		balasEnemigas.clear();
		balas.clear();
		int tipo = n % 3; // 1: formación, 2: kamikaze, 0: shooters
		int filas = 3 + Math.min(3, n / 5); // cada 5 niveles agregamos una fila, hasta +3
		int cols = 6 + Math.min(6, n / 4); // más columnas con el nivel
		if (tipo == 1) { // formación que baja toda junta
			for (int f = 0; f < filas; f++) {
				for (int c = 0; c < cols; c++) {
					int x = 60 + c * 60;
					int y = 40 + f * 45;
					Enemigo en = new Enemigo(x, y, Enemigo.TIPO_NORMAL);
					en.velocidad += n / 6; // aumenta la velocidad con el nivel
					en.hp += n / 10; // un poco más de vida cada 10 niveles
					enemigos.add(en);
				}
			}
		} else if (tipo == 2) { // kamikazes
			int cantidad = 6 + n; // cada nivel más kamikazes
			for (int i = 0; i < cantidad; i++) {
				int x = 20 + rnd.nextInt(ANCHO - 40);
				int y = -rnd.nextInt(600);
				Enemigo en = new Enemigo(x, y, Enemigo.TIPO_KAMIKAZE);
				en.velocidad += n / 8;
				enemigos.add(en);
			}
		} else { // shooters (disparan)
			for (int f = 0; f < filas; f++) {
				for (int c = 0; c < cols; c++) {
					int x = 50 + c * 55;
					int y = 40 + f * 50;
					Enemigo en = new Enemigo(x, y, Enemigo.TIPO_SHOOTER);
					en.velocidad += n / 7;
					en.hp += n / 12;
					enemigos.add(en);
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (juegoTerminado)
			return;
		// movimiento del jugador
		if (izq)
			jugador.moverIzquierda();
		if (der)
			jugador.moverDerecha();
		// disparo con límite y cooldown simple
		if (espacio) {
			if (balas.size() < 4) {
				balas.add(new Bala(jugador.getCentroX() - 2, jugador.y - 10));
			}
			espacio = false; // más simple para principiantes
		}
		// mover balas del jugador
		Iterator<Bala> itB = balas.iterator();
		while (itB.hasNext()) {
			Bala b = itB.next();
			b.mover();
			if (b.y + b.alto < 0)
				itB.remove();
		}
		// mover enemigos
		boolean bordeTocado = false;
		for (Enemigo en : enemigos) {
			if (en.tipo == Enemigo.TIPO_KAMIKAZE) {
				en.seguirJugador(jugador);
			} else {
				en.mover();
			}
			if (en.x <= 0 || en.x + en.ancho >= ANCHO)
				bordeTocado = true;
			// si llegan abajo demasiado
			if (en.y + en.alto >= jugador.y) {
				// castigo: pierdes una vida y se reinicia el nivel
				jugador.vidas -= 1;
				if (jugador.vidas <= 0)
					juegoTerminado = true;
			}
		}
		// si cualquier enemigo toca borde, todos bajan y cambian dirección (formación)
		if (bordeTocado) {
			for (Enemigo en : enemigos) {
				en.direccion *= -1;
				en.y += 20 + nivel / 5; // más caída con niveles altos
			}
		}
		// disparos de enemigos (solo shooters)
		for (Enemigo en : enemigos) {
			if (en.tipo == Enemigo.TIPO_SHOOTER) {
				double prob = 0.003 + nivel * 0.0007; // sube con el nivel
				if (rnd.nextDouble() < prob) {
					balasEnemigas.add(new BalaEnemiga(en.getCentroX() - 2, en.y + en.alto + 2));

				}
			}
		}
		// mover balas enemigas
		Iterator<BalaEnemiga> itBE = balasEnemigas.iterator();
		while (itBE.hasNext()) {
			BalaEnemiga be = itBE.next();
			be.mover();
			if (be.y > ALTO)
				itBE.remove();
			else if (be.getRect().intersects(jugador.getRect())) {
				itBE.remove();
				jugador.vidas -= 1;
				if (jugador.vidas <= 0)
					juegoTerminado = true;
			}
		}
		// colisiones: balas del jugador vs enemigos
		itB = balas.iterator();
		while (itB.hasNext()) {
			Bala b = itB.next();
			Iterator<Enemigo> itE = enemigos.iterator();
			boolean impacto = false;
			while (itE.hasNext()) {
				Enemigo en = itE.next();
				if (b.getRect().intersects(en.getRect())) {
					en.hp -= 1;
					if (en.hp <= 0) {
						itE.remove();
						puntos += 10 + nivel * 2; // más puntos a niveles altos
					}
					impacto = true;
					break;
				}
			}
			if (impacto)
				itB.remove();
		}
		// si ya no quedan enemigos, sube de nivel
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
		g.drawString("Nivel: " + nivel, ANCHO - 120, 20);
		g.drawString("Vidas: " + jugador.vidas, ANCHO / 2 - 30, 20);
		// jugador
		jugador.dibujar(g);
		// balas jugador
		g.setColor(Color.YELLOW);
		for (Bala b : balas)
			b.dibujar(g);
		// enemigos
		for (Enemigo en : enemigos)
			en.dibujar(g);
		// balas enemigas
		g.setColor(Color.PINK);
		for (BalaEnemiga be : balasEnemigas)
			be.dibujar(g);
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
