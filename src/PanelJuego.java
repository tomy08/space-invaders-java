import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class PanelJuego extends JPanel implements ActionListener, KeyListener {
	public static final int ANCHO = 800;
	public static final int ALTO = 600;
	private Timer reloj;
	private Jugador jugador;
	private ArrayList<Bala> balas = new ArrayList<>();
	private ArrayList<Enemigo> enemigos = new ArrayList<>();
	private boolean teclaIzq = false;
	private boolean teclaDer = false;
	private boolean teclaEspacio = false;
	private int puntos = 0;
	private boolean finJuego = false;

	public PanelJuego() {
		setPreferredSize(new Dimension(ANCHO, ALTO));
		setBackground(Color.BLACK);
		setFocusable(true);
		addKeyListener(this);
		jugador = new Jugador(ANCHO / 2 - 20, ALTO - 60);
// Crear enemigos simples
		for (int fila = 0; fila < 3; fila++) {
			for (int col = 0; col < 8; col++) {
				enemigos.add(new Enemigo(80 + col * 70, 60 + fila * 50));
			}
		}
		reloj = new Timer(16, this);
		reloj.start();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (finJuego)
			return;
		if (teclaIzq)
			jugador.moverIzquierda();
		if (teclaDer)
			jugador.moverDerecha();
		if (teclaEspacio) {
			if (balas.size() < 5) { // máximo 5 balas
				balas.add(new Bala(jugador.getCentroX(), jugador.y - 10));
			}
			teclaEspacio = false; // para que no dispare infinito
		}
// mover balas
		for (int i = 0; i < balas.size(); i++) {
			Bala b = balas.get(i);
			b.mover();
			if (b.y < 0)
				balas.remove(i);
		}
// mover enemigos
		for (Enemigo en : enemigos) {
			en.mover();
			if (en.y + en.alto >= jugador.y) {
				finJuego = true;
			}
		}
// colisiones
		for (int i = 0; i < balas.size(); i++) {
			Bala b = balas.get(i);
			for (int j = 0; j < enemigos.size(); j++) {
				Enemigo en = enemigos.get(j);
				if (b.getRect().intersects(en.getRect())) {
					enemigos.remove(j);
					balas.remove(i);
					puntos += 10;
					break;
				}
			}
		}
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.WHITE);
		g.drawString("Puntos: " + puntos, 20, 20);
		jugador.dibujar(g);
		for (Bala b : balas)
			b.dibujar(g);
		for (Enemigo en : enemigos)
			en.dibujar(g);
		if (finJuego) {
			g.setFont(new Font("Arial", Font.BOLD, 30));
			g.drawString("GAME OVER", ANCHO / 2 - 100, ALTO / 2);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			teclaIzq = true;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			teclaDer = true;
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
			teclaEspacio = true;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			teclaIzq = false;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			teclaDer = false;
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
			teclaEspacio = false;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}
