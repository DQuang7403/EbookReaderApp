package com.example.ebookreader.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ebookreader.MyApplication;
import com.example.ebookreader.PdfDetailActivity;
import com.example.ebookreader.databinding.RowBooksFavoriteBinding;
import com.example.ebookreader.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterPdfFav extends RecyclerView.Adapter<AdapterPdfFav.HolderPdfFavorite> {
    private Context context;
    private ArrayList<ModelPdf> pdfArrayList;
    private RowBooksFavoriteBinding binding;
    private static final String TAG = "FAV_BOOK_TAG";

    public AdapterPdfFav(Context context, ArrayList<ModelPdf> pdfArrayList) {
        this.context = context;
        this.pdfArrayList = pdfArrayList;
    }

    @NonNull
    @Override
    public HolderPdfFavorite onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //binding row_pdf_fav.xml layout
        binding = RowBooksFavoriteBinding.inflate(LayoutInflater.from(context), parent, false);

        return new HolderPdfFavorite(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfFavorite holder, int position) {
        ModelPdf model = pdfArrayList.get(position);
        loadBookDetail(model, holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PdfDetailActivity.class);
                intent.putExtra("bookId", model.getId());
                context.startActivity(intent);

            }
        });
        holder.removeFavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.removeFromFavorite(context, model.getId());
            }
        });
    }

    private void loadBookDetail(ModelPdf model, HolderPdfFavorite holder) {
        String bookId = model.getId();
        Log.d(TAG, "Book Details :" + bookId);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String bookTitle = ""+snapshot.child("title").getValue();
                        String bookDescription = ""+snapshot.child("description").getValue();
                        String categoryID = ""+snapshot.child("categoryID").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String bookUrl = ""+snapshot.child("url").getValue();
                        String uid = ""+snapshot.child("uid").getValue();
                        String downloadCount = ""+snapshot.child("downloadCount").getValue();

                        model.setFavorite(true);
                        model.setTitle(bookTitle);
                        model.setDescription(bookDescription);
                        model.setCategoryID(categoryID);
                        model.setTimestamp(Long.parseLong(timestamp));
                        model.setUid(uid);
                        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

                        MyApplication.loadCategory(categoryID, holder.categoryTv);
                        MyApplication.loadPdfFromUrlSingLePage(""+bookUrl, ""+bookTitle, holder.pdfView, holder.progressBar, null);
                        MyApplication.loadPdfSize(""+bookUrl, ""+bookTitle, holder.sizeTv);

                        holder.titleTv.setText(bookTitle);
                        holder.descriptionTv.setText(bookDescription);
                        holder.dateTv.setText(date);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    class HolderPdfFavorite extends RecyclerView.ViewHolder{

        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv, descriptionTv, categoryTv, dateTv, sizeTv;
        ImageButton removeFavBtn;
        public HolderPdfFavorite(@NonNull View itemView) {
            super(itemView);
            //init uid view from xml
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            titleTv = binding.titleTv;
            removeFavBtn = binding.removeFavBtn;
            descriptionTv = binding.descriptionTv;
            categoryTv = binding.categoryTv;
            dateTv = binding.dateTv;
            sizeTv= binding.sizeTv;
        }
    }
}
