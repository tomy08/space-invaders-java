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

	public Enemigo(int x, int y, int tipo) {
		this.x = x;
		this.y = y;
		this.tipo = tipo;
		if (tipo == TIPO_SHOOTER)
			hp = 2; // shooters más resistentes
		if (tipo == TIPO_KAMIKAZE) {
			hp = 1; // kamikaze explota rápido
			velocidad = 3;
		}
	}

	public void mover() {
		x += velocidad * direccion;
	}

	public void seguirJugador(Jugador j) {
		if (j.getCentroX() < getCentroX())
			x -= velocidad + 1;
		else
			x += velocidad + 1;
		y += velocidad; // siempre bajan
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
			g.setColor(new Color(180, 80, 255));
		g.fillRect(x, y, ancho, alto);
// dibujar HP encima
		g.setColor(Color.WHITE);
		g.fillRect(x + 4, y - 6, Math.max(0, hp) * 6, 4);
	}
}
