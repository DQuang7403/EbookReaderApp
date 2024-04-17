package com.example.ebookreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.ebookreader.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {
    //view binding
    private ActivityPdfEditBinding binding;

    //book id get from intent started from AdapterPdfAdmin
    private  String bookId;

    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;

    private static final String TAG = "BOOK_EDIT_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //book id get from intent started from AdapterPdfAdmin
        bookId = getIntent().getStringExtra("bookId");

        loadCategaries();
        LoadBookInfo();

        //handle click, pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDiaLog();
            }
        });

        //handle click, go to previous screen
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        //handle click begin upload
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();

            }
        });

    }

    private void LoadBookInfo() {
        Log.d(TAG, "LoadBookInfo: Loading book info");

        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        selectedCategoryId = ""+snapshot.child("categoryID").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String title = ""+snapshot.child("title").getValue();
                        //set to views
                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);

                        Log.d(TAG, "onDataChange: Loading Book Category Info");
                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance().getReference( "Categories");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        //get category
                                        String category = ""+snapshot.child("category").getValue();
                                        //set to category text view
                                        binding.categoryTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String title="", description ="";
    private void validateData(){
        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Enter Title...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(description)){
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(this, "Pick Category", Toast.LENGTH_SHORT).show();
        }
        else {
            updatePdf();
        }

    }

    private void updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating pdf info to db... ");

        //setup data to update to db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("categoryID", ""+selectedCategoryId);

        //start updating
        DatabaseReference ref =  FirebaseDatabase.getInstance().getReference ("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Book updated...");
                        Toast.makeText(PdfEditActivity.this,"Book info updated...", Toast.LENGTH_SHORT).show();
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: failed to update due to "+e.getMessage());
                        Toast.makeText(PdfEditActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String selectedCategoryId="", selectedCategoryTitle="";

    private void categoryDiaLog(){
        //make string array from arraylist of string
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i=0; i<categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCategoryId = categoryIdArrayList.get(which);
                        selectedCategoryTitle = categoryTitleArrayList.get(which);

                        //set to textview
                        binding.categoryTv.setText(selectedCategoryTitle);
                    }
                })
                .show();
    }

    private void loadCategaries() {
        Log.d(TAG, "loadCategaries: Loading categories...");

        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArrayList.clear();
                categoryTitleArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    String id = ""+ds.child("id").getValue();
                    String category = ""+ds.child("category").getValue();
                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);

                    Log.d(TAG, "onDataChange: ID: "+id);
                    Log.d(TAG, "onDataChange: Category: "+category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}