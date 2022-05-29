package com.example.sdzooseeker_team_64;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class PlanExhibitListAdapter extends RecyclerView.Adapter<PlanExhibitListAdapter.ViewHolder> {
    private List<ZooGraph.Exhibit> exhibitList = Collections.emptyList();

    public void setExhibitList(List<ZooGraph.Exhibit> newExhibitList) {
        this.exhibitList.clear();
        this.exhibitList = newExhibitList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.plan_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setExhibit(exhibitList.get(position));
    }

    @Override
    public int getItemCount() {
        return exhibitList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private ZooGraph.Exhibit exhibit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textView = itemView.findViewById(R.id.plan_item_text);
        }

        public ZooGraph.Exhibit getExhibit() { return exhibit; }

        public void setExhibit(ZooGraph.Exhibit exhibit) {
            this.exhibit = exhibit;
            this.textView.setText(exhibit.name);
        }
    }

}
