package Behaviours;

import java.awt.Point;
import javax.swing.JPanel;

import Entities.Enemies.AbstractEnemy;

public class BezierCurveBehaviour implements Behaviour {

	private static double START = 0;
	private static double END = 1.0;
	private static double INCR = 0.02;

	private static int CENTRE_X = 150;
	private static int CENTRE_Y = 100;

	private JPanel panel;

	private AbstractEnemy gameEntity;

	private boolean active;

	private Point p0;
	private Point p1;
	private Point p2;

	private int b;
	private double t;
	private double incr;

	private int saveX;
	private int saveY;

	public BezierCurveBehaviour (JPanel panel, AbstractEnemy gameEntity,
				     Point p0, Point p1, Point p2) {

		this.panel = panel;
		this.gameEntity = gameEntity;
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;

		active = false;

		t = START;
		incr = INCR;
	}

	public boolean isActive() {
		return active;
	}

	public void activate() {
		active = true;
		saveX = gameEntity.getX();
		saveY = gameEntity.getY();
	}

	public void deActivate() {
		active = false;
		gameEntity.setX(saveX);
		gameEntity.setY(saveY);
	}

	public void update () {

		if (!active || !panel.isVisible ())
			return;

		t = t + incr;

		if (t > END) {
			t = END;
		incr = INCR * -1.0;
		}
		else
		if (t < START) {
			t = START;
		incr = INCR;
		}

		int x = (int) 	((1 - t) * (1 - t) * p0.x +
				 2 * (1 - t) * t * p1.x +
				 t * t * p2.x);

		int y = (int)	((1 - t) * (1 - t) * p0.y +
				 2 * (1 - t) * t * p1.y +
				 t * t * p2.y);

		gameEntity.setX(x);
		gameEntity.setY(y);
   }

}