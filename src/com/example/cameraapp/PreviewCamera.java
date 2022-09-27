package com.example.cameraapp;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.*;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

//Omitted CameraPreview Activity  
//...      

class Preview extends ViewGroup implements PreviewCallback,
		SurfaceHolder.Callback {
	private final String TAG = "Preview";

	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	Size mPreviewSize;
	List<Size> mSupportedPreviewSizes;
	Camera mCamera;
	TextView mText;
	Button mbMinus;
	Button mbPlus;
	Button mbConf;
	Button mbSound;
	MediaPlayer mPlayer;
	Context mContext;

	// This variable is responsible for getting and setting the camera settings
	private Parameters parameters;
	// this variable stores the camera preview size
	private Size previewSize;
	// this array stores the pixels as hexadecimal pairs
	private int[] pixels;

	private int[] mPreviousFramePixels;
	private int[] mCurrentFramePixels;

	private int[] mOutputFramePixels;

	int wwidth;
	int hheight;
	int mcount = 0;
	int mButtonValue = 0;
	int mState = 0;
	boolean misTextDisabled = false;

	private boolean finishedProcess = true;

	Preview(Context context, TextView textView, Button bMinus, Button bPlus,
			Button bConf, Button bSound) {
		super(context);

		mbMinus = bMinus;
		mbPlus = bPlus;
		mbConf = bConf;
		mbSound = bSound;
		mContext = context;

		mText = textView;
		mText.setBackgroundColor(Color.BLACK);
		mText.setTextColor(Color.WHITE);

		try {
			File folder = new File(Environment.getExternalStorageDirectory()
					+ "/CameraApp");			
			if (!folder.exists()) {
				folder.mkdir();
				printOnText("Folder Created\n");
			}
		} catch (Exception e) {
			printOnText(e + " Folder Creation Failed \n");
		}

		mSurfaceView = new SurfaceView(context);
		addView(mSurfaceView);

		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);

		mbMinus.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					--mButtonValue;
					printOnText(mButtonValue + " \n");
				}
				return false;
			}
		});

		mbPlus.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					++mButtonValue;
					printOnText(mButtonValue + " \n");
				}
				return false;
			}
		});

		mbConf.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (mState == 0) {
						++mState;
						// Now State=1
						mText.setBackgroundColor(Color.TRANSPARENT);
						mbConf.setText("T1");
					} else if (mState == 1) {
						++mState;
						// Now State=2
						mText.setTextColor(Color.BLACK);
						mbConf.setText("T2");
					} else if (mState == 2) {
						++mState;
						// Now State=3
						misTextDisabled = true;
						mText.setText("");
						mbConf.setText("T3");
					} else {
						mState = 0;
						// Now State=0
						misTextDisabled = false;
						mText.setBackgroundColor(Color.BLACK);
						mText.setTextColor(Color.WHITE);
						mbConf.setText("T0");
					}
				}
				return false;
			}

		});

		mbSound.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					try {
						Uri u = Uri.fromFile(new File(Environment
								.getExternalStorageDirectory()
								+ "/CameraApp/Sound.mp3"));
						mPlayer = MediaPlayer.create(mContext, u);
						mPlayer.setOnCompletionListener(new OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {
								mp.release();
							}

						});
						mPlayer.start();

					} catch (Exception e) {
						printOnText(e + " mbSound onTouch() \n");
						printOnText(Environment.getExternalStorageDirectory()+ "/CameraApp/Sound.mp3 not Found!!! \n");
					}
				}
				return false;
			}
		});

		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void setCamera(Camera camera) {
		// mText.append("Inside setCamera()\n");
		try {
			mCamera = camera;
			if (mCamera != null) {
				printOnText("setCamera() Camera is not null\n");
				mSupportedPreviewSizes = mCamera.getParameters()
						.getSupportedPreviewSizes();
				printOnText("inside setCamera() mSupportedPreviewSizes="
						+ mSupportedPreviewSizes.size() + "\n");

				mCamera.setPreviewCallback(this);
				/*
				 * new PreviewCallback() { private boolean finishedProcess =
				 * true;
				 * 
				 * 
				 * });
				 */

				requestLayout();
			} else {
				printOnText("setCamera() Camera is null\n");
			}
		} catch (Exception e) {
			printOnText(e + " setCamera()\n");
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		printOnText("inside onMeasure()\n");
		try {
			final int width = resolveSize(getSuggestedMinimumWidth(),
					widthMeasureSpec);
			final int height = resolveSize(getSuggestedMinimumHeight(),
					heightMeasureSpec);
			setMeasuredDimension(width, height);
			// mText.append("width and height "+ width+""+height+"\n");
			if (mSupportedPreviewSizes != null) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
						width, height);
			}
		} catch (Exception e) {
			printOnText(e + " onMeasure()\n");
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		printOnText("Inside onLayout()\n");

		try {
			if (changed && getChildCount() > 0) {
				final View child = getChildAt(0);

				final int width = r - l;
				final int height = b - t;

				wwidth = width;
				hheight = height;

				// mText.append("width " + width + "\n");
				// mText.append("height " + height + "\n");

				// int previewWidth = width;
				// int previewHeight = height;
				// if (mPreviewSize != null) {
				// previewWidth = mPreviewSize.width;
				// previewHeight = mPreviewSize.height;
				// }

				// mText.append("previewWidth =" + previewWidth + "\n");
				// mText.append("previewHeight =" + previewHeight + "\n");

				child.layout(0, 0, width, height);

				/*
				 * if (width * previewHeight > height * previewWidth) { final
				 * int scaledChildWidth = previewWidth * height / previewHeight;
				 * child.layout((width - scaledChildWidth) / 2, 0, (width +
				 * scaledChildWidth) / 2, height);
				 * mText.append("Inside if scalechildwidth " + scaledChildWidth
				 * + " \n"); } else {
				 * 
				 * final int scaledChildHeight = previewHeight * width /
				 * previewWidth; child.layout(0, (height - scaledChildHeight) /
				 * 2, width, (height + scaledChildHeight) / 2);
				 * mText.append("Inside else scalechildheight " +
				 * scaledChildHeight + " \n"); }
				 */
			}
		} catch (Exception e) {
			printOnText(e + " onLayout()\n");
		}
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {

		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	private void processFrames() {
		printOnText("Inside processFrames()\n");
		try {
			if (mCurrentFramePixels.length == mPreviousFramePixels.length
					&& mPreviousFramePixels.length > 0) {

				for (int i = 0; i < mCurrentFramePixels.length; i++) {

					// mOutputFramePixels[i] = mCurrentFramePixels[i];
					int newPixel = mCurrentFramePixels[i];
					int oldPixel = mPreviousFramePixels[i];
					int oldBlueMax = Color.blue(oldPixel + 10);
					int oldBlueMin = Color.blue(oldPixel - 10);
					int newBlue = Color.blue(newPixel);

					boolean blueChanged = (newBlue <= oldBlueMax && newBlue >= oldBlueMin);

					if (blueChanged) {
						mOutputFramePixels[i] = 0;
					} else {
						mOutputFramePixels[i] = newPixel;
					}
				}
			}
		} catch (Exception e) {
			printOnText(e + " processFrame()\n");
		}

	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		printOnText("inside onPreviewFrame() count=" + mcount++ + "\n");

		if (finishedProcess) {
			finishedProcess = false;
			// transforms NV21 pixel data into RGB
			// pixels
			decodeYUV420SP(pixels, data, previewSize.width, previewSize.height);

			// Current Frame is shifted to Previous
			// Frame
			try {
				for (int i = 0; i < mCurrentFramePixels.length; i++) {
					mPreviousFramePixels[i] = mCurrentFramePixels[i];
				}
			} catch (Exception e) {
				printOnText(e
						+ " onPreviewFrame() Current Frame copied to Previous Frame\n");
			}

			// Pixels are copied to Current Frame
			// Frame
			try {
				for (int i = 0; i < pixels.length; i++) {
					mCurrentFramePixels[i] = pixels[i];
				}
			} catch (Exception e) {
				printOnText(e
						+ " onPreviewFrame() Pixel Data is copied to Curret Frame\n");
			}

			processFrames();

			try {

				Bitmap bm = Bitmap
						.createBitmap(mOutputFramePixels, previewSize.width,
								previewSize.height, Config.ARGB_8888);

				synchronized (mHolder) {
					Canvas canvas = mHolder.lockCanvas();
					if (canvas == null) {
						printOnText("Canvas is Null\n");
					} else {
						canvas.drawBitmap(bm, null, new Rect(0, 0, wwidth,
								hheight), null);
						mHolder.unlockCanvasAndPost(canvas);
					}
				}

			} catch (Exception e) {
				printOnText(e
						+ " onPreviewFrame() Creating and assigning Bitmap on Canvas\n");
			}
			finishedProcess = true;
		}

	}

	// Converts to RGB
	void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {

		final int frameSize = width * height;

		for (int j = 0, yp = 0; j < height; j++) {
			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
			for (int i = 0; i < width; i++, yp++) {
				int y = (0xff & ((int) yuv420sp[yp])) - 16;
				if (y < 0)
					y = 0;
				if ((i & 1) == 0) {
					v = (0xff & yuv420sp[uvp++]) - 128;
					u = (0xff & yuv420sp[uvp++]) - 128;
				}

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000)
						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
	}

	private void printOnText(String values) {
		if (mcount % 10 == 0) {
			mText.setText("");
		}
		if (!misTextDisabled) {
			mText.append(values);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			printOnText("Inside surfaceCreated()\n");
			if (mCamera != null) {

				// /initialize the variables
				parameters = mCamera.getParameters();
				previewSize = parameters.getPreviewSize();

				pixels = new int[previewSize.width * previewSize.height];
				mCurrentFramePixels = new int[previewSize.width
						* previewSize.height];
				mPreviousFramePixels = new int[previewSize.width
						* previewSize.height];
				mOutputFramePixels = new int[previewSize.width
						* previewSize.height];

				mCamera.setPreviewCallback(this);
				SurfaceTexture st = new SurfaceTexture(0);
				mCamera.setPreviewTexture(st);

			} else {
				printOnText("surfaceCreated() Camera is null\n");
			}
		} catch (Exception e) {
			printOnText(e + " surfaceCreated()\n");
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		try {
			printOnText("Inside surfaceChanged()\n");
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			requestLayout();

			mCamera.setParameters(parameters);
			mCamera.setPreviewCallback(this);
			mCamera.startPreview();

		} catch (Exception e) {
			printOnText(e + " surfaceChanged()\n");
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		printOnText("Inside surfaceDestroyed()\n");
		try {
			if (mCamera != null) {
				mCamera.stopPreview();
			}
		} catch (Exception e) {
			printOnText(e + " surfaceDestroyed()\n");
		}
	}

}
