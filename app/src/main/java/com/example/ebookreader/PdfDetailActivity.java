package com.example.ebookreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.ebookreader.databinding.ActivityPdfDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class PdfDetailActivity extends AppCompatActivity {

    //view binding
    private ActivityPdfDetailBinding binding;

    //pdf id, get from intent
    String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get data from intent e.e.bookId
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        loadBookDetails();
        //increment book view count, whenever this page starts
        MyApplication.incrementBookViewCount(bookId);

        //handle click, goback
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        //handle click, open to view pdf
        binding.readBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(PdfDetailActivity.this, PdfDetailActivity.class);
                intent1.putExtra( "bookId", bookId);
                startActivity(intent);
            }
        });
    }

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference ( "Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String title=""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        String url = ""+snapshot.child("url").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();

                        //format date
                        String date = MyApplication.formatTimestamp (Long.parseLong(timestamp));

                        MyApplication.loadCategory(
                                ""+categoryId,
                                binding.categoryTv
                        );

                        MyApplication.loadPdfFromUrlSingLePage(
                                ""+url,
                                ""+title,
                                binding.pdfView,
                                binding.progressBar
                        );

                        MyApplication.loadPdfSize(
                                ""+url,
                                ""+title,
                                binding.sizeTv
                        );

                        //set data
                        binding.titleTv.setText(title);
                        binding.descriptionTv.setText(description);
                        binding.viewsTv.setText(viewsCount.replace( "null", "N/A"));
                        binding.downloadsTv.setText(downloadsCount.replace("null", "N/A"));
                        binding.dateTv.setText(date);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void incrementBookViewCount(){
        //1) Get book views count
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference ( "Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get views count
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();

                        //in case of null replace with @
                        if (viewsCount.equals("") || viewsCount.equals("null")){
                            viewsCount = "0";
                        }

                        //2) Increment views count
                        long newViewsCount = Long.parseLong (viewsCount) + 1;
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("viewsCount", newViewsCount);

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference ( "Books");
                        reference.child(bookId)
                                .updateChildren(hashMap);
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}