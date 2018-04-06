package com.app.legend.picmaster.adapter;


import android.view.View;
import android.view.ViewGroup;

public class MainAdapter extends BaseAdapter<MainAdapter.ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    static class ViewHolder extends BaseAdapter.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
