package com.example.appytb;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.appytb.ml.ConvertedModel2;

import org.opencv.android.OpenCVLoader;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ThirdActivity3 extends AppCompatActivity {
    static{
        OpenCVLoader.initDebug();

    }
    private static final int REQUEST_CODE_PICK_VIDEO = 1;

    private VideoView videoView;
    String pathvd;

    @SuppressLint({"IntentReset", "MissingInflatedId"})
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"IntentReset", "MissingInflatedId"})

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);

        String[] labels = new String[43];
        int cnt = 0;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("labels.txt")));
            String line = bufferedReader.readLine();
            while (line != null && cnt < labels.length) {
                labels[cnt] = line;
                cnt++;
                line = bufferedReader.readLine();

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setContentView(R.layout.activity_third3);

        Button predictBtn = findViewById(R.id.predictBtn);
        videoView = findViewById(R.id.video_view);

        TextView result = findViewById(R.id.result);
        Button selectBtn = findViewById(R.id.selectBtn);
        selectBtn.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/mp4");
            startActivityForResult(intent, REQUEST_CODE_PICK_VIDEO);

        });
        predictBtn.setOnClickListener(view -> {
            VideoToFramesExtractor extractor = new VideoToFramesExtractor();
            extractor.open(pathvd);
            // Load model and input size
            ConvertedModel2 model;
            try {
                model = ConvertedModel2.newInstance(ThirdActivity3.this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            int batchSize = 1;
            int inputHeight = 32;
            int inputWidth =32;
            int inputChannels = 1;
            int inputSize = batchSize * inputHeight * inputWidth * inputChannels;

            // Allocate input buffer
            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputSize * 4);
            inputBuffer.order(ByteOrder.nativeOrder());
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{batchSize, inputHeight, inputWidth, inputChannels}, DataType.FLOAT32);
            inputFeature0.loadBuffer(inputBuffer);

            // Process frames
            StringBuilder resultString = new StringBuilder();

            Bitmap frame = extractor.getNextFrame();
            String previousPrediction = "";

            while (frame != null ) {

                // Resize input image
                Bitmap resizedFrame = Bitmap.createScaledBitmap(frame, inputWidth, inputHeight, true);

                // Convert input image to TensorBuffer
                inputBuffer.rewind();
                int[] pixels = new int[inputHeight * inputWidth];
                resizedFrame.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight);
                int pixel = 0;
                for (int i = 0; i < inputHeight; ++i) {
                    for (int j = 0; j < inputWidth; ++j) {
                        final int val = pixels[pixel++];
                        // Normalize pixel value to [0,1]
                        float normalizedPixelValue = ((val >> 16) & 0xFF) / 255.0f;
                        inputBuffer.putFloat(normalizedPixelValue);
                    }
                }
                inputFeature0.loadBuffer(inputBuffer);

                // Run model inference and get result
                ConvertedModel2.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                String currentPrediction = labels[getMax(outputFeature0.getFloatArray())];
                if (!currentPrediction.equals(previousPrediction)) {
                    resultString.append(currentPrediction).append("\n");
                }// Concatenate result with newline character
                previousPrediction = currentPrediction;

                // Release resources
                frame.recycle();
                frame = extractor.getNextFrame();

            }

            model.close();

            // Update TextView with the final predictions
            result.setText(resultString.toString());
            result.setVisibility(TextView.VISIBLE);
        });
    }



    int getMax(float[] arr){
        int max=0;
        for(int i=0;i<arr.length;i++){
            if(arr[i]>arr[max])
                max=i;

        }
        return max;
    }

    public String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) {
            return null;
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst();
        String filePath = cursor.getString(column_index);
        cursor.close();
        return filePath;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_VIDEO && resultCode == RESULT_OK) {
            if (data != null) {
                Uri videoUri = data.getData();
                pathvd=getRealPathFromUri(videoUri);

                videoView.setVideoURI(videoUri);
                videoView.start();



            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // L'autorisation a été accordée, nous pouvons maintenant sélectionner la vidéo
        // L'autorisation a été refusée, nous ne pouvons pas accéder aux fichiers de la galerie
    }
    }
