package Behaviours;

import java.awt.Point;
import javax.swing.JPanel;
import Entities.AbstractEntity;

public class BezierCurveBehaviour implements Behaviour {

   	private static double START = 0;
   	private static double END = 1.0;
   	private static double INCR = 0.02;

   	private static int CENTRE_X = 150;
   	private static int CENTRE_Y = 100;

   	private JPanel panel;

	private AbstractEntity gameEntity;

	private boolean active;		// to activate or deactivate behaviour

	private Point p0;		// first point for drawing curve;
	private Point p1;		// second point for drawing curve;
	private Point p2;		// third point for drawing curve;

	private int b;			// semi-minor axis of ellipse
	private double t;		// loop curve for t =[0.0, 1.0]
 	private double incr;		// increment for looping t in [0.0, 1.0]
 
	private int saveX;		// x-coordinate of game entity before behaviour starts
	private int saveY;		// y-coordinate of game entity before behaviour starts

	public BezierCurveBehaviour (JPanel panel, AbstractEntity gameEntity, 
				     Point p0, Point p1, Point p2) {

		this.panel = panel;
		this.gameEntity = gameEntity;
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;

		active = false;		// behaviour is inactive by default

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

		//System.out.println("t = " + t);

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