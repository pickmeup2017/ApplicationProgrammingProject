package com.example.apple.kre;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.media.MediaRecorder;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;


public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2{
    MediaRecorder mRecorder = null;
    String pa;
    private MenuItem fr;
    private MenuItem ba;
    private MenuItem ma;
    private MenuItem po;
    private MenuItem restr;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;

    private boolean img = false;
    private boolean rec = false;

    private Mat getImage;
    private Mat replaceImage;
    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private CameraBridgeViewBase mOpenCvCameraView;
    private int mAbsoluteFaceSize = 0;
    private float mRelativeFaceSize = 0.2f;
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.i("opencv", "Failure!!!!");
        } else {
            Log.i("opencv", "Success!!!!");
        }
    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("open", "OpenCV loaded successfully");

                    try {
                        if (img == true) {
                            getImage = Utils.loadResource(MainActivity.this, R.drawable.pokerface, Highgui.CV_LOAD_IMAGE_COLOR);
                        } else {
                            getImage = Utils.loadResource(MainActivity.this, R.drawable.imgmask, Highgui.CV_LOAD_IMAGE_COLOR);
                        }
                        Imgproc.cvtColor(getImage, getImage, Imgproc.COLOR_RGB2RGBA);

                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());

                        if (mJavaDetector.empty()) {
                            Log.e("Error", "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i("Normal", "Loaded cascade classsifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("Error", "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.setCameraIndex(1);
                    mOpenCvCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Normal", "called onCreate");
//        mPreview = (SurfaceView) findViewById(R.id.MainActivityCameraView);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.MainActivityCameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        fr = menu.add("Front Camera");
        ba = menu.add("BackSide Camera");
        po = menu.add("PokerFace Image");
        ma = menu.add("Mask Image");
        restr = menu.add("Record Start");
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item == fr) {
            mOpenCvCameraView.disableView();
            mOpenCvCameraView.setCameraIndex(1);
            mOpenCvCameraView.enableView();
        } else if (item == ba) {
            mOpenCvCameraView.disableView();
            mOpenCvCameraView.setCameraIndex(0);
            mOpenCvCameraView.enableView();
        } else if (item == ma) {
            img = false;
        } else if (item == po) {
            img = true;
        } else if (item == restr) {
//            if (rec == true) {
//                String sd = Environment.getExternalStorageDirectory().getAbsolutePath();
//                pa = sd + "/recvideo.mp4";
//                if (mRecorder == null) {
//                    mRecorder = new MediaRecorder();
//                } else {
//                    mRecorder.reset();
//                }
//                rec = false;
//                mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//                mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
//                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//                mRecorder.setOutputFile(pa);
//                mRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
//                try {
//                    mRecorder.prepare();
//                    mRecorder.start();
//
//                } catch (IllegalStateException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            item.setTitle("Record Stop");

        } else {
//            mRecorder.stop();
//            mRecorder.release();
//            mRecorder = null;
//            rec = true;
            item.setTitle("Record Start");

        }

        return true;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();

    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        try {
            if (img == true) {
                getImage = Utils.loadResource(MainActivity.this, R.drawable.pokerface, Highgui.CV_LOAD_IMAGE_COLOR);
            } else {
                getImage = Utils.loadResource(MainActivity.this, R.drawable.imgmask, Highgui.CV_LOAD_IMAGE_COLOR);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Imgproc.cvtColor(getImage, getImage, Imgproc.COLOR_RGB2RGBA);


        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();
        if (mJavaDetector != null)
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 5, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        Rect[] facesArray = faces.toArray();
        replaceImage = new Mat();

        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.resize(getImage, replaceImage, new Size(facesArray[i].width, facesArray[i].height));
            Rect roi = new Rect((int) facesArray[i].tl().x, (int) facesArray[i].tl().y + 30, replaceImage.cols(), replaceImage.rows());
            Core.addWeighted(mRgba.submat(roi), 1, replaceImage, 1, 1, mRgba.submat(roi));
        }

        return mRgba;

    }


}

