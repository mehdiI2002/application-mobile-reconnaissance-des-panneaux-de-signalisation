package com.example.appytb;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.appytb.ml.ConvertedModel2;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {
    Button selectBtn,predictBtn,captureBtn;
    TextView result;
    Bitmap bitmap;//Une image bitmap est un format d'image qui stocke les données de chaque pixel de l'image.
    ImageView imageview;
    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPermission();
         String[] labels=new String[43] ;
         int cnt=0;
        try {
            BufferedReader bufferedReader=new BufferedReader( new InputStreamReader(getAssets().open("labels.txt")));
            String line=bufferedReader.readLine();//lire fichier label
            while(line!=null && cnt < labels.length){
                labels[cnt]=line;
                 cnt++;
                 line=bufferedReader.readLine();

            }
        } catch (IOException e){
            throw new RuntimeException(e);
        }


        setContentView(R.layout.activity_main);
        selectBtn =findViewById(R.id. selectBtn);
        predictBtn =findViewById(R.id. predictBtn);
        captureBtn =findViewById(R.id. captureBtn);
        result =findViewById(R.id.result);

        imageview =findViewById(R.id.imageView);

        //creer une autre inerface lorsque je clique sur le boutton.
        selectBtn.setOnClickListener(view -> {

            Intent intent =new Intent();//utilisée pour représenter une action qui doit être effectuée par le système ou un autre composant obtenir le conenu de l'appareil
            intent.setAction(Intent.ACTION_GET_CONTENT);//creer une intention pour obtenir le contenu du contenu
            //"intent.setAction(Intent.ACTION_GET_CONTENT)", ui spécifie l'action à effectuer pour obtenir du contenu (ACTION_GET_CONTENT).
            // Cette action demande à l'utilisateur de sélectionner un ou plusieurs fichiers à
            //partir de l'application, tels que des images, des vidéos ou des documents.
            intent.setType("image/*");
            startActivityForResult(intent,10);//le code permet de savoir quelle activité à été appller

        });
        captureBtn.setOnClickListener(view-> {

            Intent intent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent,12);
        });
        predictBtn.setOnClickListener(view-> {
            if (isImageViewEmpty(  imageview)) {
                result.setText("image vide");
                result.setVisibility(TextView.VISIBLE);

                // ImageView est vide
            }
            else {
                // ImageView n'est pas vide

                try {
                    int batchSize = 1;
                    int inputHeight = 32;
                    int inputWidth = 32;
                    int inputChannels = 1;
                    int inputSize = batchSize * inputHeight * inputWidth * inputChannels;
                    ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputSize * 4); // 4 bytes per float

// Converts input image to a TensorBuffer.Un tenseur est un objet mathématique généralisant les concepts de vecteurs et de matrices à des dimensions supérieures.


                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{batchSize, inputHeight, inputWidth, inputChannels}, DataType.FLOAT32);
                    inputFeature0.loadBuffer(inputBuffer);

// Load input data into the ByteBuffer.
                    bitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true);
                    // Resize input image
                    inputBuffer.rewind();
                    inputBuffer.order(ByteOrder.nativeOrder());
                    int[] pixels = new int[inputHeight * inputWidth];
                    bitmap.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight);
                    int pixel = 0;
                    for (int i = 0; i < inputHeight; ++i) {
                        for (int j = 0; j < inputWidth; ++j) {
                            final int val = pixels[pixel++];
                            // Normalize pixel value to [0,1]
                            float normalizedPixelValue = ((val >> 16) & 0xFF) / 255.0f;
                            inputBuffer.putFloat(normalizedPixelValue);
                        }
                    }


// Run model inference and get result
                    ConvertedModel2 model = ConvertedModel2.newInstance(MainActivity.this);
                    //cette classe est généré par le modèle
                    ConvertedModel2.Outputs outputs = model.process((inputFeature0));
                    //la methode procces retourn un objet output
                    //inputfeature0 sont les données d'entrée préparé par le bytebuffer
                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                    //.getOutputFeature0AsTensorBuffer() retourne  le resultats sous forme d'un renseur

                    result.setText(labels[getMax(outputFeature0.getFloatArray())]);
                    result.setVisibility(TextView.VISIBLE);

// Release model resources if no longer used
                    model.close();

                } catch (IOException e) {
                    // TODO Handle the exception
                }
            }});
    }
    int getMax(float[] arr){
        int max=0;
        for(int i=0;i<arr.length;i++){
            if(arr[i]>arr[max])
                max=i;

        }
        return max;
    }
    public boolean isImageViewEmpty(   ImageView imageview) {
        Drawable drawable = imageview.getDrawable();//getdrawable represente l'image affiché dans image view

        if (drawable == null) {
            return true;
        } else if (drawable instanceof BitmapDrawable) {
            //BitmapDrawable sous type de drawable qui contient l'image bitmap

            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            return bitmap == null;
        } else {
            return false;
        }
    }

    void getPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // La méthode checkSelfPermission() est utilisée pour vérifier si l'application a déjà été accordée la permission d'utiliser la caméra
            // Manifest.permission.CAMERA. Si l'application a déjà la permission d'utiliser la caméra, cette méthode renvoie PackageManager.PERMISSION_GRANTED, sinon elle renvoi
            //  e PackageManager.PERMISSION_DENIED.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 11 );
//ActivityCompat demande la permission
            //requestPermissions : Il s'agit d'une méthode qui permet de demander des permissions à l'utilisateur pour une application.
            //MainActivity.this : Cela fait référence à l'activité en cours qui demande la permission.
            //new String[]{Manifest.permission.CAMERA} : C'est un tableau de chaînes de caractères qui contient la permission à demander,
            // dans ce cas-ci la permission d'accéder à l'appareil photo.
        }
    }

    //ata dans la méthode onActivityResult() est un objet de type Intent
    // qui contient les données de retour renvoyées par l'activité appelée.
    protected void onActivityResult(int requestcode,int resultcode ,Intent data) {//elle peut récupérer les données renvoyées par l'activité lancée.
        if(requestcode==10){
            if(data!=null){
                Uri uri=data.getData();
                try {
                    bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);//MediaStore.Images.Media pour récupérer un objet Bitmap à partir de l'UR
                    //I uri en utilisant la méthode getBitmap().
                    //ContentResolver est utilisé pour accéder aux données de l'application, telle
                    imageview.setImageBitmap(bitmap);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
        else if(requestcode==12){
            bitmap=(Bitmap) data.getExtras().get("data");
            imageview.setImageBitmap(bitmap);


        }
        super.onActivityResult(requestcode, resultcode, data);
    }
}