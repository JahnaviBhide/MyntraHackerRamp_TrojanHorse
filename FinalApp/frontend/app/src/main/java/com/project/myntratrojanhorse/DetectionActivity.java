package com.project.myntratrojanhorse;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
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


import com.project.myntratrojanhorse.Utils.Common;
import com.project.myntratrojanhorse.Utils.IUploadCallbacks;
import com.project.myntratrojanhorse.Utils.ProgressRequestBody;
import com.project.myntratrojanhorse.retrofit.IUploadAPI;
import com.project.myntratrojanhorse.retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Multipart;

public class DetectionActivity extends AppCompatActivity implements IUploadCallbacks {
    private static final int PICK_FILE_REQUEST = 1000;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    private static final int PERMISSION_REQ_ID = 22;
    public static final int GALLERY_REQUEST_CODE = 105;
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET};
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    IUploadAPI mService;
    Button btnCamera;
    Button btnGallery;
    Button btnSubmit;
    Button btnRecommend;
    TextView acneText;
    TextView toneText;
    ImageView toneImg;
    TextView genderText;
    TextView wrinkleText;
    ImageView imageView;
    Uri selectedFileUri;
    ProgressDialog dialog;

    String currentPhotoPath;
    String wrinklesDected;
    String acneDetected;
    String genderDetected;

    private IUploadAPI getAPIUpload(){
        return RetrofitClient.getClient().create(IUploadAPI.class);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);
        getSupportActionBar().hide();
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)){}

        imageView=findViewById(R.id.displayImageView);
        //imageView.setVisibility(View.INVISIBLE);

        btnRecommend=findViewById(R.id.recommendBtn);
        btnRecommend.setVisibility(View.INVISIBLE);

        btnSubmit=findViewById(R.id.submitBtn);
        btnSubmit.setVisibility(View.INVISIBLE);

        acneText=findViewById(R.id.acnetext);
        acneText.setVisibility(View.INVISIBLE);

        toneText=findViewById(R.id.tonetext);
        toneText.setVisibility(View.INVISIBLE);

        genderText=findViewById(R.id.gendertext);
        genderText.setVisibility(View.INVISIBLE);

        wrinkleText=findViewById(R.id.wrinklestext);
        wrinkleText.setVisibility(View.INVISIBLE);

        toneImg=findViewById(R.id.tonemageView);
        toneImg.setVisibility(View.INVISIBLE);

        mService = getAPIUpload();
        btnGallery= (Button)findViewById(R.id.galleryBtn);
        btnGallery.setOnClickListener(view ->{
            chooseFile();
            //uploadFile();
        });

        btnCamera=findViewById(R.id.cameraBtn);
        btnCamera.setOnClickListener(view->{
            dispatchTakePictureIntent();

        });

        btnSubmit.setOnClickListener(v->{
            btnCamera.setVisibility(View.INVISIBLE);
            btnGallery.setVisibility(View.INVISIBLE);
            uploadFile();
        });

        btnRecommend.setOnClickListener(v->{

            String urlString="http;//www.google.com";
            if(wrinklesDected.equalsIgnoreCase("\"Wrinkle Found\"")){
                urlString="https://www.myntra.com/anti-ageing-";
            }
            else{
                if(acneDetected.equalsIgnoreCase("\"Mild\"")||acneDetected.equalsIgnoreCase("\"Severe\"")
                        ||acneDetected.equalsIgnoreCase("\"Moderate\""))
                {
                    urlString="https://www.myntra.com/acne";
                }
                else {

                    if(genderDetected.equalsIgnoreCase("\"Male\""))
                        urlString="https://www.myntra.com/shop/men";
                    else
                        urlString="https://www.myntra.com/shop/women";
                }
            }

            Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse(urlString));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage("com.android.myntra");
            try{
                startActivity(intent);
            }catch(ActivityNotFoundException ex)
            {
                intent.setPackage(null);
                startActivity(intent);
            }
        });




    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.project.myntratrojanhorse.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        btnSubmit.setVisibility(View.VISIBLE);
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == CAMERA_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                File f = new File(currentPhotoPath);
                imageView.setImageURI(Uri.fromFile(f));
                imageView.setVisibility(View.VISIBLE);
                Log.d("tag", "ABsolute Url of Image is " + Uri.fromFile(f));

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
                selectedFileUri = Uri.fromFile(f);

            }

        }

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data != null) {
                    selectedFileUri = data.getData();
                    Log.d("tag", "ABsolute Url of Image is " + selectedFileUri);
                    if (selectedFileUri != null && !selectedFileUri.getPath().isEmpty()) {
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageURI(selectedFileUri);
                    } else
                        Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void uploadFile() {
        if (selectedFileUri != null) {
            btnSubmit.setVisibility(View.INVISIBLE);
            btnRecommend.setVisibility(View.VISIBLE);
            dialog = new ProgressDialog(DetectionActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage("Uploading...");
            dialog.setIndeterminate(false);
            dialog.setMax(100);
            dialog.setCancelable(false);
            dialog.show();
            File file = null;
            try {

                file = new File(Common.getFilePath(this, selectedFileUri));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            if (file!=null){
                final ProgressRequestBody requestBody = new ProgressRequestBody(file, this);
                MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestBody.create(MediaType.parse("image/*"),file));

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        mService.uploadTone(body)
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        dialog.dismiss();
                                        String image_processed_link = new StringBuilder("http://10.0.2.2:5000/" +
                                                response.body().replace("\"", "")).toString();
                                        Picasso.get().load(image_processed_link)
                                                .into(toneImg);
                                        toneImg.setVisibility(View.VISIBLE);
                                        toneText.setText("The Dominant Colors:");
                                        toneText.setVisibility(View.VISIBLE);
                                        Toast.makeText(DetectionActivity.this, "Tone Detected !!", Toast.LENGTH_SHORT);

                                        mService.uploadAcne(body)
                                                .enqueue(new Callback<String>() {
                                                    @Override
                                                    public void onResponse(Call<String> call, Response<String> response) {
                                                        dialog.dismiss();

                                                        acneDetected = new StringBuilder(response.body()).toString();
                                                        acneText.setVisibility(View.VISIBLE);
                                                        acneText.setText("Acne :"+acneDetected);
                                                        Toast.makeText(DetectionActivity.this, "Acne Detected !!", Toast.LENGTH_SHORT);
                                                    }

                                                    @Override
                                                    public void onFailure(Call<String> call, Throwable t) {
                                                        dialog.dismiss();
                                                        Toast.makeText(DetectionActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT);
                                                    }
                                                });

                                        mService.uploadGender(body)
                                                .enqueue(new Callback<String>() {
                                                    @Override
                                                    public void onResponse(Call<String> call, Response<String> response) {
                                                        dialog.dismiss();

                                                        String[] temp=new StringBuilder(response.body()).toString().split(";");
                                                        genderDetected = temp[0]+"\"";
                                                        String path="\""+temp[1];
                                                        genderText.setVisibility(View.VISIBLE);
                                                        genderText.setText("Gender :"+genderDetected);
                                                        String image_processed_link = new StringBuilder("http://10.0.2.2:5000/" +
                                                                path.replace("\"", "")).toString();
                                                        Picasso.get().load(image_processed_link)
                                                                .into(imageView);
                                                        imageView.setVisibility(View.VISIBLE);
                                                        Toast.makeText(DetectionActivity.this, "Gender Detected !!", Toast.LENGTH_SHORT);

                                                    }

                                                    @Override
                                                    public void onFailure(Call<String> call, Throwable t) {
                                                        dialog.dismiss();
                                                        Toast.makeText(DetectionActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT);
                                                    }
                                                });

                                        mService.uploadWrinkle(body)
                                                .enqueue(new Callback<String>() {
                                                    @Override
                                                    public void onResponse(Call<String> call, Response<String> response) {
                                                        dialog.dismiss();

                                                        wrinklesDected = new StringBuilder(response.body()).toString();
                                                        wrinkleText.setVisibility(View.VISIBLE);
                                                        wrinkleText.setText("Wrinkle :"+wrinklesDected);
                                                        Toast.makeText(DetectionActivity.this, "Wrinkle Detected !!", Toast.LENGTH_SHORT);
                                                    }

                                                    @Override
                                                    public void onFailure(Call<String> call, Throwable t) {
                                                        dialog.dismiss();
                                                        Toast.makeText(DetectionActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT);
                                                    }
                                                });

                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        dialog.dismiss();
                                        Toast.makeText(DetectionActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT);
                                    }
                                });
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();


            }
        } else {
            Toast.makeText(this, "Cannot upload this file !", Toast.LENGTH_SHORT).show();

        }
    }

    private void chooseFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,PICK_FILE_REQUEST);
    }




    @Override
    public void onProgressUpdate(int percent) {

    }
    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    REQUESTED_PERMISSIONS,
                    requestCode);
            return false;
        }
        return true;
    }
}