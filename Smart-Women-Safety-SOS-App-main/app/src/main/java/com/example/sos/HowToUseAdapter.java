package com.example.sos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class HowToUseAdapter extends RecyclerView.Adapter<HowToUseAdapter.ViewHolder> {

    private List<HowToUseActivity.GuideSection> sections;

    public HowToUseAdapter(List<HowToUseActivity.GuideSection> sections) {
        this.sections = sections;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_guide_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HowToUseActivity.GuideSection section = sections.get(position);
        holder.bind(section);
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvTitle, tvDescription, tvContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvContent = itemView.findViewById(R.id.tvContent);
        }

        public void bind(HowToUseActivity.GuideSection section) {
            tvTitle.setText(section.title);
            tvDescription.setText(section.description);

            StringBuilder contentBuilder = new StringBuilder();
            for (int i = 0; i < section.content.length; i++) {
                contentBuilder.append(section.content[i]);
                if (i < section.content.length - 1) {
                    contentBuilder.append("\n\n");
                }
            }
            tvContent.setText(contentBuilder.toString());
        }
    }
}
