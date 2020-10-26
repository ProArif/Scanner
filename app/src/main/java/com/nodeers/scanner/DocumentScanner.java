package com.nodeers.scanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
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
import java.util.Locale;
import java.util.Random;

public class DocumentScanner extends AppCompatActivity {

    private InputImage image;
    private ImageView img;
    private TextRecognizer recognizer;
    Task<Text> result;
    private TextView tvResult;
    private Button btnSaveImg,btnSavePdf,btnExtract;
    Uri resultUri;
    boolean boolean_save;
    Bitmap bm;
    private boolean permission;
    private final int MY_PERMISSIONS_REQUEST = 10;

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

                    saveImage();

            }
        });
        btnSavePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePdf();
            }
        });


    }


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);

        } else {
            permission = true;
        }

    }
    //Function: Permission Request Results
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permission = true;
                } else {
                    permission = false;
                }
                return;
            }
        }
    }

    private void getBitmap(){
        img.buildDrawingCache();
        bm =img.getDrawingCache();
    }



    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED;
    }

    private void saveImage() {
        //MediaStore.Images.Media.insertImage(getContentResolver(), bm, "hjjhj" , "jgjgh");
        checkPermission();
        getBitmap();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";

        if (isExternalStorageWritable()){
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/Scanner Images");
            myDir.mkdir();
            File file = new File (myDir, fname);
            File appSpecificExternalDir = new File(this.getExternalFilesDir("/NodeerScanner"), fname);
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
        } else {
            Log.e("failed saving", String.valueOf(isExternalStorageWritable()));
            //MediaStore.Images.Media.insertImage(getContentResolver() , bm, fname, "Scanned Image");

            File newFile = new File(this.getFilesDir() + "NodeerScanner", fname);

            if (newFile.exists()) {
                newFile.delete();
            }
            try {
                FileOutputStream out = new FileOutputStream(newFile);
                bm.compress(Bitmap.CompressFormat.JPEG, 20, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, n);
            values.put(MediaStore.Images.Media.DESCRIPTION, "description");
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//            values.put("_data", newFile.getAbsolutePath());
            ContentResolver cr = getContentResolver();
            cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }

    }

    private void savePdf(){

//        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        DisplayMetrics displaymetrics = new DisplayMetrics();
//        this.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//        float hight = displaymetrics.heightPixels ;
//        float width = displaymetrics.widthPixels ;
//
//        int convertHighet = (int) hight, convertWidth = (int) width;

//        Resources mResources = getResources();
//        Bitmap bitmap = BitmapFactory.decodeResource(mResources, R.drawable.screenshot);

//        PdfDocument document = new PdfDocument();
//        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bm.getWidth(), bm.getHeight(), 1).create();
//        PdfDocument.Page page = document.startPage(pageInfo);
//
//        Canvas canvas = page.getCanvas();
//
//
//        Paint paint = new Paint();
//        paint.setColor(Color.parseColor("#ffffff"));
//        canvas.drawPaint(paint);
//
//
//
//        bm = Bitmap.createScaledBitmap(bm, bm.getWidth(), bm.getHeight(), true);
//
//        paint.setColor(Color.BLUE);
//        canvas.drawBitmap(bm, 0, 0 , null);
//        document.finishPage(page);
//
//
//        // write the document content
//        String targetPdf = getFilesDir() + "/test.pdf";
//        File filePath = new File(targetPdf);
//        try {
//            document.writeTo(new FileOutputStream(filePath));
//            btnSavePdf.setText("Check PDF");
//            boolean_save=true;
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
//        }
//
//        // close the document
//        document.close();

        bm = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c1 = new Canvas(bm);
        img.draw(c1);

        PdfDocument pd = new PdfDocument();

        PdfDocument.PageInfo pi = new PdfDocument.PageInfo.Builder(bm.getWidth(), bm.getHeight(), 1).create();
        PdfDocument.Page p = pd.startPage(pi);
        Canvas c = p.getCanvas();
        c.drawBitmap(bm, 0, 0, new Paint());
        pd.finishPage(p);

        try {
            //make sure you have asked for storage permission before this
            File f = new File(this.getFilesDir()
                    + File.separator + "a-computer-engineer-pdf-test.pdf");
            pd.writeTo(new FileOutputStream(f));
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        pd.close();
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
                    getBitmap();
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