package com.nodeers.scanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class DocumentScanner extends AppCompatActivity {

    private InputImage image;
    private ImageView img;
    private TextRecognizer recognizer;
    Task<Text> result;
    private TextView tvResult;
    private Button btnSaveImg,btnSavePdf,btnExtract;
    Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_scanner);

        recognizer = TextRecognition.getClient();

        img = findViewById(R.id.imageView);
        tvResult = findViewById(R.id.tvExtractedText);
        btnExtract = findViewById(R.id.textExtract);
        btnSaveImg = findViewById(R.id.saveImg);
        btnSavePdf = findViewById(R.id.savePdf);

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(DocumentScanner.this);

        btnExtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processImage();
            }
        });

        btnSaveImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    reqPermission();

            }
        });


    }

    private void reqPermission(){
        if (ContextCompat.checkSelfPermission(DocumentScanner.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(DocumentScanner.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(DocumentScanner.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            saveImage();
        }
    }


    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED;
    }

    private void saveImage() {
        img.buildDrawingCache();
        Bitmap bm=img.getDrawingCache();
        //MediaStore.Images.Media.insertImage(getContentResolver(), bm, "hjjhj" , "jgjgh");

        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";

        if (isExternalStorageWritable()){
            //String root = Environment.getExternalStorageDirectory().toString();
            //File myDir = new File(root + "/Scanner Images");
            //myDir.mkdir();

            //File file = new File (myDir, fname);
            File appSpecificExternalDir = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fname);
            if (appSpecificExternalDir.exists ()) appSpecificExternalDir.delete ();
            try {
                FileOutputStream out = new FileOutputStream(appSpecificExternalDir);
                bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                Log.e("get the image", "saved");


            } catch (Exception e) {
                e.printStackTrace();
                Log.e("failed saving", e.getMessage());

            }
        } else
            Log.e("failed saving", String.valueOf(isExternalStorageWritable()));
            MediaStore.Images.Media.insertImage(getContentResolver(), bm, "hjjhj" , "jgjgh");


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                assert result != null;
                resultUri = result.getUri();

                try {
                    image = InputImage.fromFilePath(this, resultUri);
                    Picasso.get().load(resultUri).into(img);
                    img.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //processImage();

                Log.e("get the image", String.valueOf(resultUri));
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void processImage() {

        result = recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text text) {
                        String resultText = text.getText();
                        for (Text.TextBlock block : text.getTextBlocks()) {
                            String blockText = block.getText();
                            Point[] blockCornerPoints = block.getCornerPoints();
                            Rect blockFrame = block.getBoundingBox();
                            for (Text.Line line : block.getLines()) {
                                String lineText = line.getText();
                                Point[] lineCornerPoints = line.getCornerPoints();
                                Rect lineFrame = line.getBoundingBox();
                                for (Text.Element element : line.getElements()) {
                                    String elementText = element.getText();
                                    Point[] elementCornerPoints = element.getCornerPoints();
                                    Rect elementFrame = element.getBoundingBox();
                                }
                            }
                        }
                        if (resultText.equals("")){
                            tvResult.setText(R.string.notFound);
                        } else {

                            tvResult.setText(resultText);
                            tvResult.setVisibility(View.VISIBLE);
                        }

                        Toast.makeText(DocumentScanner.this,
                                " processing image successfull",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DocumentScanner.this,
                                "Failed to process the image",Toast.LENGTH_LONG).show();
                    }
                });
    }
}