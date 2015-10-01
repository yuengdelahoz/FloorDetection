package delahoz.floordetection.structured;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;

/*For some reason OpenCV 3.0 does not work properly with the floor detection
*/
public class FrameProcessing extends Activity {

	private ArrayList<Line> vertical_lines_left;
	private ArrayList<Line> vertical_lines_right;
	private Line LeftLine = null, RightLine = null;

	private ArrayList<Line> obliques_lines_left;
	private ArrayList<Line> obliques_lines_right;
	private ArrayList<Line> horizontal_lines;

	private Point left;
	private Point right;
	private Point CornerL;
	private Point CornerR;

	public FrameProcessing() {
	}

	public Mat Smooth(Mat src) {
		Mat gray = new Mat();
		Mat dst = new Mat();
		Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.GaussianBlur(gray, dst, new Size(5, 5), 0);

		return dst;
	}

	public Mat FindEdges(Mat src) {
		Mat gray = new Mat();
		Mat edges = new Mat();

		Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.Canny(gray, edges, 30, 90);
		// Imgproc.dilate(edges, edges, new Mat());
		// Imgproc.erode(edges,edges, new Mat());
		return edges;
	}

	public Mat FindLines(Mat edges) {
		Mat lines = new Mat();
		Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 50, 50, 10);
		// Imgproc.HoughLines(edges, lines, 1, Math.PI / 180, 50);
		return lines;
	}

	public void FindWallFloorBoundary(Mat lines, Mat img) {

		int width = img.cols();
		int height = img.rows();

		vertical_lines_left = new ArrayList<Line>();
		vertical_lines_right = new ArrayList<Line>();

		obliques_lines_left = new ArrayList<Line>();
		obliques_lines_right = new ArrayList<Line>();
		horizontal_lines = new ArrayList<Line>();

		for (int x = 0; x < lines.cols(); x++) {

			double[] vec = lines.get(0, x);
			double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
			double angle;

			Point endpoint1 = new Point(x1, y1);
			Point endpoint2 = new Point(x2, y2);

			Line line = new Line(endpoint1, endpoint2);
			double theta = line.getAngle();
			// Log.i("Angle", theta + "");

			angle = Math.abs(theta);

			// If angle is 90 +- 5 then line is a vertical line
			if (angle - 90 > -10 && angle - 90 < 10) {

				// if line endpoint is below height/2 then it is a possible line
				// that collide with floor
				if (line.getEnd().y > height / 3)

					// split lines into left and right lines
					if (line.getStart().x < width / 2) {
						vertical_lines_left.add(line);
					} else if (line.getStart().x > width / 2) {
						vertical_lines_right.add(line);
					}

			}

			// If angle is 45 +- 10 then line is an oblique line

			if (angle - 45 > -20 && angle - 45 < 20) {

				// if the line endpoint is below height/2 then it is a
				// possible wall-floor line
				if (line.getEnd().y > height / 2)

					// split lines into left and right lines
					if (line.getStart().x < width / 2) {
						if (theta < 0)
							obliques_lines_left.add(line);
					} else if (line.getStart().x > width / 2) {
						if (theta > 0)
							obliques_lines_right.add(line);
					}
			}

			if ((angle - 0 > -5 && angle - 0 < 5) || (angle - 180 > -5 && angle - 180 < 5)) {
				if (line.getEnd().y > height / 3 && line.getEnd().y < 2 * height / 3)
					horizontal_lines.add(line);
			}

		}
	}

	public Mat FindFloor(Mat img) {

		Mat imgColor = img.clone();
		int height = img.rows();
		int width = img.cols();
		Point center = new Point(width / 2, height / 2);

		if (!horizontal_lines.isEmpty()) {
			horizontal_lines = getCandidates(horizontal_lines);
			Line theone = horizontal_lines.get(0);

			if (theone.getStart().x < theone.getEnd().x) {
				left = theone.getStart();
				right = theone.getEnd();
			} else {
				right = theone.getStart();
				left = theone.getEnd();
			}
			if (left.x < width / 2 && right.x > width / 2) {
				CornerL = new Point(0, height - 1);
				CornerR = new Point(width - 1, height - 1);

				LeftLine = new Line(left, CornerL);
				RightLine = new Line(right, CornerR);
			}

		}

		if (!vertical_lines_left.isEmpty()) {
			ArrayList<Line> candidates = getCandidates(vertical_lines_left);
			Line theone = candidates.get(0);
			double m = -1.4;
			double b = theone.getEnd().y - (m) * theone.getEnd().x;
			int y = height / 2;

			int x = (int) Math.ceil(((y - b) / m));

			Point midPoint = new Point(x, y);
			int y1 = height - 1;
			int x1 = (int) ((y1 - b) / m);
			if (x1 < 0) {
				x1 = 0;
				y1 = (int) b;
			}
			Point lastPoint = new Point(x1, y1);
			LeftLine = new Line(midPoint, lastPoint);

		}

		if (!vertical_lines_right.isEmpty()) {
			ArrayList<Line> candidates = getCandidates(vertical_lines_right);
			Line theone = candidates.get(0);
			double m = 1.4;
			double b = theone.getEnd().y - (m) * theone.getEnd().x;
			int y = height / 2;

			int x = (int) (((y - b) / m));

			Point midPoint = new Point(x, y);
			int y1 = height - 1;
			int x1 = (int) (((y1 - b) / m));
			if (x1 > width) {
				x1 = width - 1;
				y1 = (int) (m * x1 + b);
			}
			Point lastPoint = new Point(x1, y1);
			RightLine = new Line(midPoint, lastPoint);

		}

		if (!obliques_lines_left.isEmpty()) {
			obliques_lines_left = getCandidates(obliques_lines_left);
			Line theone = null;
			theone = obliques_lines_left.get(0);
			int c = 0;
			for (int i = 0; i < obliques_lines_left.size(); i++) {

				if (obliques_lines_left.get(i).distanceTocenter(center) < theone.distanceTocenter(center))
					theone = obliques_lines_left.get(i);
				c++;
				if (c == 3)
					break;
			}
			double b = theone.yIntercept();
			double m = theone.getSlope();

			Point midPoint = null;
			if (theone.getStart().y < height / 2) {

				int y = height / 2;
				int x = (int) (((y - b) / m));

				midPoint = new Point(x, y);
			} else {
				midPoint = theone.getStart();
			}

			int y1 = height - 1;
			int x1 = (int) (((y1 - b) / m));

			if (x1 < 0) {
				x1 = 0;
				y1 = (int) b;
			}

			Point lastPoint = new Point(x1, y1);
			LeftLine = new Line(midPoint, lastPoint);

		}

		if (!obliques_lines_right.isEmpty()) {
			obliques_lines_right = getCandidates(obliques_lines_right);
			Line theone = obliques_lines_right.get(0);
			int c = 0;
			for (int i = 0; i < obliques_lines_right.size(); i++) {
				double curD = obliques_lines_right.get(i).distanceTocenter(center);
				double oD = theone.distanceTocenter(center);

				if (curD < oD)
					theone = obliques_lines_right.get(i);
				c++;
				if (c == 3)
					break;
			}
			double b = theone.yIntercept();
			double m = theone.getSlope();

			Point midPoint = null;
			if (theone.getStart().y < height / 2) {

				int y = height / 2;
				int x = (int) (((y - b) / m));

				midPoint = new Point(x, y);
			} else {
				midPoint = theone.getStart();
			}

			int y1 = height - 1;
			int x1 = (int) (((y1 - b) / m));
			if (x1 > width) {
				x1 = width - 1;
				y1 = (int) (m * x1 + b);
			}
			Point lastPoint = new Point(x1, y1);
			RightLine = new Line(midPoint, lastPoint);

		}
		if (LeftLine != null && RightLine != null) {
/*			Imgproc.line(imgColor, LeftLine.getStart(), LeftLine.getEnd(), new Scalar(255, 255, 255), 5);
			Imgproc.line(imgColor, RightLine.getStart(), RightLine.getEnd(), new Scalar(255, 255, 255), 5);
			Imgproc.line(imgColor, LeftLine.getStart(), RightLine.getStart(), new Scalar(255, 255, 255), 5);*/
			
		/*	Core.line(imgColor, LeftLine.getStart(), LeftLine.getEnd(), new Scalar(255, 255, 255), 1);
			Core.line(imgColor, RightLine.getStart(), RightLine.getEnd(), new Scalar(255, 255, 255), 1);
			Core.line(imgColor, LeftLine.getStart(), RightLine.getStart(), new Scalar(255, 255, 255), 1);
			*/
		}

		return imgColor;
	}

	private ArrayList<Line> getCandidates(ArrayList<Line> lines) {
		Sorting S = new Sorting();
		// Sort Lines based on their lenghts
		ArrayList<Line> Sortedlines = S.HeapSort(lines);
		int ArraySize = Sortedlines.size();
		// Discard lines that are not as long as the longest line
		ArrayList<Line> temp = new ArrayList<Line>();
		for (int i = ArraySize - 1; i >= 0; i--) {
			temp.add(Sortedlines.get(i));
		}
		return temp;
	}

}
