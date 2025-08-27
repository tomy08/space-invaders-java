import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class VentanaJuego extends JFrame {

	private static final long serialVersionUID = 1L;

	public VentanaJuego() {
		setTitle("Space Invaders");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		PanelJuego panel = new PanelJuego();
		setContentPane(panel);
		pack();
		setLocationRelativeTo(null); // centrar ventana
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new VentanaJuego().setVisible(true);
		});
	}
}