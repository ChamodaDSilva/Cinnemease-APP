package com.example.cinnamease;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private static final int REQUEST_IMAGE_FROM_STORAGE = 2;


    Button btnCameraUpload;

    Button BtnDeviceUpload;
    ImageView imgUploaded;
    TextView textViewResult;

    public ScanFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ScanFragment.
     */
    // TODO: Rename and change types and number of parameters
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
            // Permission is not granted, request it
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            // Permission has already been granted, proceed with camera operation
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        // Find the ImageButton by its ID
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
                // Create an intent to pick an image from storage
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_IMAGE_FROM_STORAGE);
            }
        });

        return view;
    }

    private void makeApiCall(final byte[] imageByteArray) {
        OkHttpClient client = new OkHttpClient();

        // Create MultipartBody to send the image file
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg", RequestBody.create(MediaType.parse("image/jpeg"), imageByteArray))
                .build();

        // Create the request
        Request request = new Request.Builder()
                .url("https://chamodadesilva-test-docker-cin.hf.space/predict")
                .post(requestBody)
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                // Handle API call response
                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    try {
                        final JSONObject jsonResponse = new JSONObject(responseBody);
                        final double maturityScore = jsonResponse.getDouble("maturity_score");
                        final String maturityStatus = jsonResponse.getString("maturity_status");

                        // Run UI updates on the main thread
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Now you can use these values to update UI elements
                                Log.d("API_RESPONSE", "Maturity Score: " + maturityScore);
                                Log.d("API_RESPONSE", "Maturity Status: " + maturityStatus);

                                String textResult;
                                if (maturityStatus.equals("Matured")){
                                    textResult="අස්වැන්න නෙළීමට හොඳම කාලයයි.";
                                    // Update TextView with maturity status
                                    textViewResult.setText(textResult);
                                    textViewResult.setTextColor(Color.BLUE);
                                }else{
                                    textResult="අස්වැන්න නෙළීමට හොඳම කාලය නොවේ.";
                                    // Update TextView with maturity status
                                    textViewResult.setText(textResult);
                                    textViewResult.setTextColor(Color.RED);
                                }


                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Handle unsuccessful response
                    String errorMessage = response.message();
                    // Log the error message
                    Log.e("API_ERROR", "Error: " + errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
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