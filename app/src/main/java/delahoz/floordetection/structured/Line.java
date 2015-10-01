package delahoz.floordetection.structured;

import org.opencv.core.Point;

public class Line {

	private double length;
	private double angle;
	private Point start;
	private Point end;
	private double dc;
	private double m;

	public Line(Point endp1, Point endp2) {

		if (endp1.y < endp2.y) {
			start = endp1;
			end = endp2;
		} else {
			start = endp2;
			end = endp1;
		}
		calculateSlope();
		calculateAngle();
		calculateLength();

	}

	public double getAngle() {
		return angle;
	}

	public double getLength() {
		return length;
	}

	public double distanceTocenter(Point center) {
		// If the line passes through two points P1=(x1,y1) and P2=(x2,y2) then
		// the distance of (x0,y0) from the line is:
		// distance(P1,P2,(x0,y0))= |(y2-y1)x0 -(x2-x1)y0 +x2y1
		// -y2x1|/distance(P1,P2)

		dc = Math.abs((end.y - start.y) * center.x - (end.x - start.x)
				* center.y + end.x * start.y - end.y * start.x)
				/ length;
		return dc;
	}

	private void calculateAngle() {
		double dy = (end.y - start.y);
		double dx = (end.x - start.x);

		if (dx == 0) {
			angle = 90;
		} else {
			double m = dy / dx;
			angle = Math.toDegrees(Math.atan(m));

		}
	}

	private void calculateLength() {
		double fterm = Math.pow(end.x - start.x, 2);
		double sterm = Math.pow(end.y - start.y, 2);
		length = Math.sqrt(fterm + sterm);
	}
	
	private void calculateSlope(){
		double dy = (end.y - start.y);
		double dx = (end.x - start.x);
		if (dx==0) m = 9999999999999999.9;
		else
		m = dy / dx;

	}

	public Point getStart() {
		return start;
	}

	public Point getEnd() {
		return end;
	}

	public double getSlope() {
		return m;
	}

	public double yIntercept() {
		double b;
		b = start.y - m * start.x;
		return b;

	}

	@Override
	public String toString() {

		return length + "";
	}
}
