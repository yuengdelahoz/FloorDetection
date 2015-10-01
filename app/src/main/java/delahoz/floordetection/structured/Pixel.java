package delahoz.floordetection.structured;

import java.util.ArrayList;

import org.opencv.core.Mat;

public class Pixel {
	int x, y;
	boolean visited = false, isInStack = false;
	ArrayList<Pixel> Neighbors;
	Mat img;

	Pixel(int HeightPix, int WidthPix, Mat img) {
		this.img = img;
		this.y = HeightPix;
		this.x = WidthPix;
	}

	public ArrayList<Pixel> getNeighbors(Pixel[][] imagePixel) {
		Neighbors = new ArrayList<Pixel>();
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (i == 0 && j == 0)
					continue;
				if (y + i <0 || x+j<0)
					continue;
				if (y + i >imagePixel.length-1 || x+j > imagePixel[1].length-1)
					continue;
				Pixel N = imagePixel[y + i][x + j];
				if (N != null)
					Neighbors.add(N);
			}
		}

		return Neighbors;
	}

}
