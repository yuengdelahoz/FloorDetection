package delahoz.floordetection.structured;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import delahoz.floordetection.structured.R;

public class CameraActivity extends Activity implements CvCameraViewListener2 {
	private static final String TAG = "OCVSample::Activity";
	private CameraBridgeViewBase mOpenCvCameraView;
	Mat mrgb;
	ImageTools Tool;


	int counter = 0;
	final static Object lock = new Object();

	private FrameProcessing FP;
	int k = 0;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FP = new FrameProcessing();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.helloopencvlayout);
		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
		mOpenCvCameraView.setMaxFrameSize(320, 240);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);
		Tool = new ImageTools();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this,
				mLoaderCallback);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub	
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
		super.onDestroy();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {

	}

	@Override
	public void onCameraViewStopped() {

	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// return inputFrame.gray();

		final Mat img = inputFrame.rgba().clone();
		final long timestamp = System.currentTimeMillis();

		Thread FloorDetector = new Thread(new Runnable() {

			@Override
			public void run() {
				Mat svImg;

				Mat edges = FP.FindEdges(img);

				Mat lines = FP.FindLines(edges);

				FP.FindWallFloorBoundary(lines, img);

				svImg = FP.FindFloor(img);
				
				Tool.SaveImage(svImg, timestamp+"_P_");


			}
		});

		FloorDetector.start();

		return (img);

	}

}
