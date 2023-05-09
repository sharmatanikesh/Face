package com.example.facy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button button;
    TextView textView;
    ImageView imageView;
    private static final int REQUEST_IMAGE_CAPTURE=124;
    InputImage fireBaseVersion;
    FaceDetection visionFaceDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView) ;

        FirebaseApp.initializeApp(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenFile();
            }
        });
        Toast.makeText(this, "App started", Toast.LENGTH_SHORT).show();
    }

    private void OpenFile() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);

        }else{
            Toast.makeText(this, "App failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle bundle = data.getExtras();
        Bitmap bitmap = (Bitmap)bundle.get("data");

        FaceDetectionProcess(bitmap);
        Toast.makeText(this, "successful", Toast.LENGTH_SHORT).show();
    }

    public void  FaceDetectionProcess(Bitmap bitmap){
        textView.setText("Processing Image");
        final StringBuilder builder = new StringBuilder();
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        InputImage image = InputImage.fromBitmap(bitmap,0);

        FaceDetectorOptions highAccuracyOpt = new FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL).setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).enableTracking().build();

        FaceDetector detector = FaceDetection.getClient(highAccuracyOpt);

        Task<List<Face>> result = detector.process(image);
        
        result.addOnSuccessListener(new OnSuccessListener<List<Face>>() {
            @Override
            public void onSuccess(List<Face> faces) {
                if (faces.size() !=0){
                    if(faces.size()==1){
                        builder.append(faces.size() + "Face Detected\n\n");
                        
                    } else if ((faces.size()>1)) {
                        builder.append(faces.size() + "Faces Detected\n\n");
                    }
                }
                for(Face face : faces){
                    int id = face.getTrackingId();
                    float rotY = face.getHeadEulerAngleY();
                    float rotZ= face.getHeadEulerAngleZ();
                    builder.append("1. Face tracking ID ["+id+"]\n ");
                    builder.append("2. Head rotation to right [" + String.format("%.2f",rotY)+"deg. ]\n");
                    builder.append("2. Head tilted to sideways [" + String.format("%.2f",rotZ)+"deg. ]\n");

                    if (face.getSmilingProbability() >0){
                        float SmilingProbablity = face.getSmilingProbability();
                        builder.append("4. Smiling probab;ity " +String.format( "%.2f",SmilingProbablity));
                    }

                    if (face.getLeftEyeOpenProbability() >0){
                        float leftEye = face.getLeftEyeOpenProbability();
                        builder.append("5. Left Eye " +String.format( "%.2f",leftEye));
                    }
                    if (face.getRightEyeOpenProbability() >0){
                        float rigthEye = face.getRightEyeOpenProbability();
                        builder.append("6. Right eye " +String.format( "%.2f",rigthEye));
                    }
                    builder.append("\n");

                }

                ShowDetection("Face Detection ", builder, true);
            }
        });

        result.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                StringBuilder builder1 = new StringBuilder();
                builder1.append("Sorry error occured");
                ShowDetection("Face Detection ", builder, false);

            }
        });
    }

    public void ShowDetection(final String title, final StringBuilder builder ,boolean success) {
        if (success = true) {
            textView.setText(null);
            textView.setMovementMethod(new ScrollingMovementMethod());


            if (builder.length() != 0) {
                textView.append(builder);
                if (title.substring(0, title.indexOf(' ')).equalsIgnoreCase("OCR")) {
                    textView.append("\n(Hold the text to copy it)");
                } else {
                    textView.append("\n Hold the text to copy it");
                }

                textView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(title, builder);
                        clipboardManager.setPrimaryClip(clip);
                        return true;
                    }
                });
            } else {
                textView.append(title.substring(0, title.indexOf(' ')) + "Failed to open");
            }

        } else if (success == false) {
            textView.setText(null);
            textView.setMovementMethod(new ScrollingMovementMethod());
            textView.append(builder);
        }
    }
}