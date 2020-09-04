package com.e.deteccionapi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.Landmark;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Vision vision;
    List<Landmark> puntos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Vision.Builder visionBuilder = new Vision.Builder(new NetHttpTransport(),
                new AndroidJsonFactory(), null);
        visionBuilder.setVisionRequestInitializer(new
                VisionRequestInitializer("AIzaSyB5MkIB5lNnQH1kC1tZ3ATeEsv7z66moKs"));
        vision = visionBuilder.build();


    }

    public Image getImageToProcess() {
        ImageView imagen = (ImageView) findViewById(R.id.imgImgToProcess);
        BitmapDrawable drawable = (BitmapDrawable) imagen.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        bitmap = scaleBitmapDown(bitmap, 1200);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);

        byte[] imageInByte = stream.toByteArray();

        Image inputImage = new Image();
        inputImage.encodeContent(imageInByte);
        return inputImage;
    }

    public BatchAnnotateImagesRequest setBatchRequest(String TipoSolic, Image inputImage) {
        Feature desiredFeature = new Feature();
        desiredFeature.setType(TipoSolic);

        AnnotateImageRequest request = new AnnotateImageRequest();
        request.setImage(inputImage);
        request.setFeatures(Arrays.asList(desiredFeature));


        BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
        batchRequest.setRequests(Arrays.asList(request));
        return batchRequest;
    }


    public void ProcesarTexto(View View) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                BatchAnnotateImagesRequest batchRequest = setBatchRequest("FACE_DETECTION",
                        getImageToProcess());
                try {
                    Vision.Images.Annotate annotateRequest = vision.images().annotate(batchRequest);
                    annotateRequest.setDisableGZipContent(true);
                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    List<FaceAnnotation> faces = response.getResponses().get(0).getFaceAnnotations();

                    int numberOfFaces = faces.size();
                    String likelihoods = "";
                    for (int i = 0; i < numberOfFaces; i++) {
                        likelihoods += "\n Rostro " + i + "  " + faces.get(i).getJoyLikelihood();
                        puntos = faces.get(i).getLandmarks();
                    }
                    final String message = "Esta imagen tiene " + numberOfFaces + " rostros " + likelihoods;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView imageDetail = (TextView) findViewById(R.id.txtResult);
                            //imageDetail.setText(text.getText());
                            imageDetail.setText(message.toString());
                            DibujarCuadros();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void DibujarCuadros(){
        ImageView imv = findViewById(R.id.imgImgToProcess);
        Bitmap img_original = ((BitmapDrawable) imv.getDrawable()).getBitmap();
        Bitmap tempBitmap = Bitmap.createBitmap(img_original.getWidth(), img_original.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(tempBitmap);
        canvas.drawBitmap(img_original, 0, 0, null);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);

        FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false).build();

        Frame frame = new Frame.Builder().setBitmap(img_original).build();
        SparseArray<Face> caras = faceDetector.detect(frame);

        for (int i = 0; i < caras.size(); i++) {
            Face cara_actual = caras.valueAt(i);
            float xinicio = cara_actual.getPosition().x;
            float yinicio = cara_actual.getPosition().y;
            float xfin = xinicio + cara_actual.getWidth();
            float yfin = yinicio + cara_actual.getHeight();
            canvas.drawRoundRect(new RectF(xinicio, yinicio, xfin, yfin), 2, 2, paint);
        }

        imv.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;
        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }




}