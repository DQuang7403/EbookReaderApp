package com.example.ebookreader.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.ebookreader.Constants;
import com.example.ebookreader.R;
import com.example.ebookreader.databinding.ActivityPdfViewBinding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PdfViewActivity extends AppCompatActivity {

    //view binding
    private ActivityPdfViewBinding binding;

    private String bookId;

    private static final String TAG = "PDF_VIEW_TAG";

    public static int currentPage = 0;

    public static String pdfUrl = "";
    private static final String PREFS_NAME = "pdf_bookmarks";
    private static final String PREF_BOOKMARK_KEY = "bookmark_page_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //get bookId from intent that we passed in intent
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        Log.d(TAG, "onCreate: BookId: " + bookId);
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int bookmarkedPage = sharedPreferences.getInt(PREF_BOOKMARK_KEY + bookId, -1);
        if(bookmarkedPage != -1){
            binding.bookMark.setImageResource(R.drawable.ic_bookmark_white);
            Toast.makeText(PdfViewActivity.this, "Press bookmark icon to go to bookmarked page", Toast.LENGTH_SHORT).show();
        }
        loadBookDetails();


        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
        binding.bookMarkRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //remove bookmark
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.remove(PREF_BOOKMARK_KEY + bookId);
                editor.apply();
                binding.bookMark.setImageResource(R.drawable.ic_bookmark_border_white);
                Toast.makeText(PdfViewActivity.this, "Removed Bookmark", Toast.LENGTH_SHORT).show();
            }
        });
        binding.bookMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                int bookmarkedPage = sharedPreferences.getInt(PREF_BOOKMARK_KEY + bookId, -1);
                if (bookmarkedPage != -1) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(PREF_BOOKMARK_KEY + bookId);
                    editor.apply();
                    binding.pdfView.jumpTo(bookmarkedPage, true);
                    Toast.makeText(PdfViewActivity.this, "Jumped to bookmarked page", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(PREF_BOOKMARK_KEY + bookId, currentPage);
                    binding.bookMark.setImageResource(R.drawable.ic_bookmark_white);
                    Toast.makeText(PdfViewActivity.this, "Marked", Toast.LENGTH_SHORT).show();
                    editor.apply();
                }
            }
        });
    }

    private void loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Get Pdf URL...");
        //Database Reference to get book details e.g. get book url using book id
        //Step (1) Get Book Url using Book Id
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get book url
                        pdfUrl = "" + snapshot.child("url").getValue();
                        Log.d(TAG, "onDataChange: PDF URL: " + pdfUrl);

                        //Step (2) Load Pdf using that url from firebase storage
                        LoadBookFromUrl(pdfUrl);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void LoadBookFromUrl(String pdfUrl) {
        Log.d(TAG, "loadBookFromUrl: Get PDF from storage");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        //load pdf using bytes
                        binding.pdfView.fromBytes(bytes)
                                .swipeHorizontal(false)
                                .onPageChange(new OnPageChangeListener() {
                                    @Override
                                    public void onPageChanged(int page, int pageCount) {
                                        //set current and total pages in toolbar subtitle
                                        currentPage = (page + 1); //do + 1 because page starts from @
                                        binding.toolbarSubtitleTv.setText(currentPage + "/" + pageCount); //e.g. 3/290
                                        Log.d(TAG, "onPageChange: PDF URL: " + currentPage + "/" + pageCount);
                                    }
                                })
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Log.d(TAG, "onError: " + t.getMessage());
                                        Toast.makeText(PdfViewActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    public void onPageError(int page, Throwable t) {
                                        Log.d(TAG, "onPageError: " + t.getMessage());
                                        Toast.makeText(PdfViewActivity.this, "Error on page " + page + " " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .load();
                        binding.progressBar.setVisibility(View.GONE);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                        //failed to load book
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}