package com.example.appytb;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;

public class VideoToFramesExtractor {
    private VideoCapture mVideoCapture;
    private Mat mFrame;

    public VideoToFramesExtractor() {
        mVideoCapture = new VideoCapture();
        mFrame = new Mat();
    }

    public boolean open(String videoFilePath) {
        return mVideoCapture.open(videoFilePath);
    }

    public void release() {
        mVideoCapture.release();
    }//release libère les ressources


    public Bitmap getNextFrame() {
        if (mVideoCapture.read(mFrame)) {
            Size size = mFrame.size();
            int width = (int) size.width;
            int height = (int) size.height;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mFrame, bitmap);
            return bitmap;
        } else {
            return null;
        }
    }
}
