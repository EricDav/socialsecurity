package com.example.davidalienyi.socialsecurity.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.davidalienyi.socialsecurity.Models.Social;
import com.example.davidalienyi.socialsecurity.R;

import static com.example.davidalienyi.socialsecurity.Helper.getFormatedDate;

import java.util.List;

public class SocialAdapter extends RecyclerView.Adapter <SocialAdapter.MyViewHolder> {
    List<Social> socials;

    public SocialAdapter(List<Social> socials) {
        this.socials = socials;
    }

    public  class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView subject, contentSummary, date;

        public MyViewHolder(View itemView) {
            super(itemView);
            subject = (TextView) itemView.findViewById(R.id.socialHandleNameId);
            contentSummary = (TextView) itemView.findViewById(R.id.contentSummaryId);
            date = (TextView) itemView.findViewById(R.id.dateId);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.social, parent, false);

        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Social social = socials.get(position);

        holder.subject.setText(social.getTitle());
        holder.contentSummary.setText(social.getSocialHandle());
        holder.date.setText(getFormatedDate(social.getDateCreated()));
    }

    @Override
    public int getItemCount() {
        return socials.size();
    }
}
