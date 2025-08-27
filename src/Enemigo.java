import java.awt.*;

public class Enemigo {
	public static final int TIPO_NORMAL = 0;
	public static final int TIPO_KAMIKAZE = 1;
	public static final int TIPO_SHOOTER = 2;

	public int x, y;
	public int ancho = 30, alto = 20;
	public int velocidad = 2;
	public int direccion = 1; // 1 derecha, -1 izquierda
	public int tipo = TIPO_NORMAL;
	public int hp = 1;

	// Nuevos campos
	public boolean atacando = false; // para kamikaze (cuando ataca)
	public long ultimoDisparo = 0; // para shooters
	public long siguienteDisparo = 0;

	// Constructor: ahora recibe nivel para ajustar velocidad/HP
	public Enemigo(int x, int y, int tipo, int nivel) {
		this.x = x;
		this.y = y;
		this.tipo = tipo;

		// Aumentar velocidad/vida ligeramente según nivel
		int bonusVel = nivel / 6;
		int bonusHp = nivel / 12;

		if (tipo == TIPO_SHOOTER) {
			hp = 2 + bonusHp;
			velocidad = 2 + bonusVel;
			ultimoDisparo = System.currentTimeMillis();
			siguienteDisparo = 1000 + (long) (Math.random() * 3000); // 1-4s
		} else if (tipo == TIPO_KAMIKAZE) {
			hp = 1 + bonusHp;
			velocidad = 2 + bonusVel;
		} else { // normal
			hp = 1 + bonusHp;
			velocidad = 1 + bonusVel;
		}
	}

	// Movimiento normal: avanza y rebota en bordes, bajando un poco cuando rebota.
	public void mover() {
		x += velocidad * direccion;

		if (x <= 0) {
			x = 0;
			direccion = 1;
			y += 20;
		} else if (x + ancho >= PanelJuego.ANCHO) {
			x = PanelJuego.ANCHO - ancho;
			direccion = -1;
			y += 20;
		}
	}

	// Movimiento de kamikaze cuando está atacando: sigue al jugador bajando.
	public void seguirJugador(Jugador j) {
		if (j.getCentroX() < getCentroX())
			x -= (velocidad + 1);
		else
			x += (velocidad + 1);
		y += velocidad * 2; // baja más rápido al atacar
	}

	// Comprueba si puede disparar (solo shooters)
	public boolean puedeDisparar() {
		if (tipo != TIPO_SHOOTER)
			return false;
		return (System.currentTimeMillis() - ultimoDisparo) >= siguienteDisparo;
	}

	// Resetea el temporizador de disparo (para el próximo disparo aleatorio)
	public void resetDisparo() {
		ultimoDisparo = System.currentTimeMillis();
		siguienteDisparo = 1000 + (long) (Math.random() * 3000); // 1-4s
	}

	public int getCentroX() {
		return x + ancho / 2;
	}

	public Rectangle getRect() {
		return new Rectangle(x, y, ancho, alto);
	}

	public void dibujar(Graphics g) {
		if (tipo == TIPO_NORMAL)
			g.setColor(Color.GREEN);
		else if (tipo == TIPO_KAMIKAZE)
			g.setColor(Color.ORANGE);
		else
			g.setColor(new Color(180, 80, 255)); // shooter violeta

		g.fillRect(x, y, ancho, alto);

		// HP encima (sencillo)
		g.setColor(Color.WHITE);
		g.fillRect(x + 4, y - 6, Math.max(0, hp) * 6, 4);
	}
}