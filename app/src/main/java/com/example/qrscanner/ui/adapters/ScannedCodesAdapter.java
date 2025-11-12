package com.example.qrscanner.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qrscanner.R;
import com.example.qrscanner.models.QRCode;

import java.util.ArrayList;
import java.util.List;

public class ScannedCodesAdapter extends RecyclerView.Adapter<ScannedCodesAdapter.ViewHolder> {
    private List<QRCode> codes = new ArrayList<>();
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDelete(int position);
    }

    public ScannedCodesAdapter(android.content.Context context, OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scanned_code, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QRCode qrCode = codes.get(position);
        holder.tvQRCode.setText("QR-код: " + qrCode.getValue());
        holder.btnDelete.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return codes.size();
    }

    public void setCodes(List<QRCode> codes) {
        this.codes = codes != null ? codes : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQRCode;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQRCode = itemView.findViewById(R.id.tvQRCode);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}



