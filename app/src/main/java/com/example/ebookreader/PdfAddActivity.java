package com.example.ebookreader;

import static android.app.ProgressDialog.show;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.ebookreader.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    //setup viewbinding
    private ActivityPdfAddBinding binding;

    //firebaseauth
    private FirebaseAuth firebaseAuth;

    //progress dialog


    //arraylist to hold pdf category
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    //uri of picked pdf
    private Uri pdfUri = null;

    private static final int PDF_PICK_CODE = 1000;

    //tag for debugging
    private  static final String TAG = "ADD_PDF_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        loadPdfCategories();

        //set up progress dialog
        //handle click, go to previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        //handle click, attach pdf
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {pdfPickIntent();
            }
        });
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryPickDialog();
            }
        });

        //handle click, upload pdf
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //validate data
                validateData();
            }
        });
    }

    private String title="", description="";
    private void validateData() {
        //Step 1: Validate Data
        Log.d(TAG, "validateData: validating data...");

        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
//        category = binding.categoryTv.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedcategoryTitle)) {
            Toast.makeText(this, "Pick Category...", Toast.LENGTH_SHORT).show();
        }
        else if (pdfUri==null){
            Toast.makeText(this, "Pick Pdf", Toast.LENGTH_SHORT).show();
        }
        else {
            //all data is valid, can upload now
            uploadPdfToStorage();
        }
    }

    private void uploadPdfToStorage() {
        //Step 2: Upload Pdf to firebase storage
        Log.d(TAG, "uploadPdfToStorage: uploading to storage...");

        //timestamp
        long timestamp = System.currentTimeMillis();

        //path of pdf in firebase storage
        String filePathAndName = "Books/" + timestamp;
        // storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: PDF uploaded to storage...");
                        Log.d(TAG, "onSuccess: getting pdf url");

                        //get pdf url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult();

                        //upload to firebase st db
                        uploadPdfInfoToDb(uploadedPdfUrl, timestamp );
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: PDF upload failed due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "PDF upload failed due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void uploadPdfInfoToDb(String uploadedPdfUrl, long timestamp) {
        //Step 3: Up load Pdf info to firebase db
        Log.d(TAG, "uploadPdfInfoToDb: uploading Pdf info to firebase db...");
        Toast.makeText(this, "Uploading Pdf info to firebase db...", Toast.LENGTH_SHORT).show();
        String uid = firebaseAuth.getUid();

        //setup data to upload,
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("categoryID",""+selectedcategoryId);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("timestamp", timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadCount", 0);

        //db refrence: DB > Books
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Successfully uploaded...");
                        Toast.makeText(PdfAddActivity.this, "Successfully uploaded...", Toast.LENGTH_SHORT).show();
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to up load to db due to"+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Failed to up load to db due to"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading pdf categories...");
        Toast.makeText(PdfAddActivity.this, "loadPdfCategories: Loading pdf categories...", Toast.LENGTH_SHORT).show();
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear(); //clear before adding data
                categoryIdArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();

                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String selectedcategoryId, selectedcategoryTitle;
    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category pick dialog");
        //get string array of categories from arraylist
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i=0; i<categoryTitleArrayList.size();i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //handle item click
                        //get clicked item form list
                        selectedcategoryTitle = categoryTitleArrayList.get(which);
                        selectedcategoryId = categoryIdArrayList.get(which);
                        //set to category textview
                        binding.categoryTv.setText(selectedcategoryTitle);

                        Log.d(TAG, "onClick: Select Category: "+selectedcategoryId+""+selectedcategoryTitle);
                    }
                }) .show();
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == PDF_PICK_CODE){
                Log.d(TAG, "onActivityResult: PDF Picked");
                pdfUri = data.getData();
                Log.d(TAG, "onActivityResult: "+pdfUri);
                Toast.makeText(this, "PDF Picked", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Log.d(TAG, "onActivityResult: cancelled picking pdf");
            Toast.makeText(this, "cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
    }
}