package delahoz.floordetection.structured;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.app.Activity;
import android.os.Bundle;

public class PicActivity extends Activity {
	FrameProcessing FP;
	ImageTools Tool;
	private Mat img;
	double startTimeMod, startTimeFD, TimeFD, TimeMod1, TimeMod2, TimeMod3, TimeMod4, TimeMod5;

	double avgTimeMod1 = 0, avgTimeMod2 = 0, avgTimeMod3 = 0, avgTimeMod4 = 0, avgTimeMod5 = 0, avgTimeFD = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);

	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			if (status == LoaderCallbackInterface.SUCCESS) {
				Tool = new ImageTools();
				Mat svImg;

				File folder = new File("/storage/sdcard0/Floor/Input");
				for (final File fileEntry : folder.listFiles()) {
					FP = new FrameProcessing();

					String name = fileEntry.getName();
					img = Tool.ReadImage(folder, name);
					// If I cannot read the image ignore
					Mat edges = FP.FindEdges(img);
					Mat lines = FP.FindLines(edges);
					FP.FindWallFloorBoundary(lines, img);
					svImg = FP.FindFloor(img);
					Tool.SaveImage(svImg, "P_" + name);

				}
				System.exit(1);
			} else {
				super.onManagerConnected(status);
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		// you may be tempted to do something here, but it's *async*, and may
		// take some time,
		// so any opencv call here will lead to unresolved native errors.

	}

}
