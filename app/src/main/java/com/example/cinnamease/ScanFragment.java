package com.example.cinnamease;

import android.app.ProgressDialog;
import android.content.Intent;
import android.Manifest;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScanFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private static final int REQUEST_IMAGE_FROM_STORAGE = 2;


    Button btnCameraUpload;

    Button BtnDeviceUpload;
    ImageView imgUploaded;
    TextView textViewResult;

    ProgressDialog progressDialog;

    public ScanFragment() {
    }

    public static ScanFragment newInstance(String param1, String param2) {
        ScanFragment fragment = new ScanFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
        }

        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        btnCameraUpload = view.findViewById(R.id.btn_camera);

        BtnDeviceUpload =view.findViewById(R.id.btn_upload);

        imgUploaded = view.findViewById(R.id.imageViewUpload);

        textViewResult = view.findViewById(R.id.textResult);

        // Set click listener on the button
        btnCameraUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        BtnDeviceUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_FROM_STORAGE);
            }
        });

        return view;
    }

    private void makeApiCall(final byte[] imageByteArray) {
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("විශ්ලේෂණය සිදු වෙමින් පවතී...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg", RequestBody.create(MediaType.parse("image/jpeg"), imageByteArray))
                .build();


        Request request = new Request.Builder()
                .url("https://chamodadesilva-cinnamease-api.hf.space/predict")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    try {
                        final JSONObject jsonResponse = new JSONObject(responseBody);
                        final String maturityStatus = jsonResponse.getString("maturity_status");

                        // Run UI updates on the main thread
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Log.d("API_RESPONSE", "Maturity Status: " + maturityStatus);

                                String textResult;
                                if (maturityStatus.equals("Matured")){
                                    textResult="අස්වැන්න නෙළීමට හොඳම කාලයයි.";
                                    // Update TextView with maturity status
                                    textViewResult.setText(textResult);
                                    textViewResult.setTextColor(Color.BLUE);
                                }else if (maturityStatus.equals("Unmatured")){
                                    textResult="අස්වැන්න නෙළීමට හොඳම කාලය නොවේ.";
                                    // Update TextView with maturity status
                                    textViewResult.setText(textResult);
                                    textViewResult.setTextColor(Color.GREEN);
                                }else{
                                    textResult="සමාවෙන්න මට විශ්වාසයක් නැහැ.";
                                    // Update TextView with maturity status
                                    textViewResult.setText(textResult);
                                    textViewResult.setTextColor(Color.GREEN);
                                }


                            }
                        });
                    } catch (JSONException e) {
                        String textResult="පද්ධති ගැටළුවක් විය හැක.";
                        textViewResult.setText(textResult);
                        textViewResult.setTextColor(Color.RED);
                        e.printStackTrace();
                    }
                } else {
                    // Handle unsuccessful response
                    String errorMessage = response.message();
                    // Log the error message
                    Log.e("API_ERROR", "Error: " + errorMessage);
                    String textResult="පද්ධති ගැටළුවක් විය හැක.";
                    textViewResult.setText(textResult);
                    textViewResult.setTextColor(Color.RED);
                    textViewResult.setBackgroundColor(Color.WHITE);
                }
            }

            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                String textResult="පද්ධති ගැටළුවක් විය හැක.";
                textViewResult.setText(textResult);
                textViewResult.setTextColor(Color.RED);
                // Handle API call failure
                e.printStackTrace();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        textViewResult.setTextColor(Color.BLACK);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            // Capture image from camera
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imgUploaded.setImageBitmap(imageBitmap);

            // Convert bitmap to byte array
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Update TextView with maturity status
            textViewResult.setText("කරුණාකර රැඳී සිටින්න!");
            // Make API call with the byte array
            makeApiCall(byteArray);

        } else if (requestCode == REQUEST_IMAGE_FROM_STORAGE && resultCode == getActivity().RESULT_OK) {
            // Pick image from storage
            try {
                Uri selectedImageUri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                imgUploaded.setImageBitmap(bitmap);
                // Make API call with the byte array
                // Convert bitmap to byte array
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                // Update TextView with maturity status
                textViewResult.setText("කරුණාකර රැඳී සිටින්න!");
                // Make API call with the byte array
                makeApiCall(byteArray);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


        @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with camera operation
            } else {
                // Permission denied, show a message or handle it gracefully
                Toast.makeText(requireContext(), "කැමරා අවසරයක් නැත", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}