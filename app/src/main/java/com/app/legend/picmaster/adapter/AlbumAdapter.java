package com.app.legend.picmaster.adapter;

import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.app.legend.picmaster.R;
import com.app.legend.picmaster.interfaces.SelectListener;
import com.app.legend.picmaster.models.ImageInfo;
import com.app.legend.picmaster.utils.App;
import com.bumptech.glide.Glide;

import java.util.Collections;
import java.util.List;

public class AlbumAdapter extends BaseAdapter<AlbumAdapter.ViewHolder>{


    private boolean isScroll=false;
    private boolean lastState=isScroll;
    private SelectListener listener;


    private List<ImageInfo> infoList;

    int w= App.getContext().getResources().getDisplayMetrics().widthPixels/3;

    public void setInfoList(List<ImageInfo> infoList) {
        this.infoList = infoList;
        notifyDataSetChanged();
    }

    private void listByTime(List<ImageInfo> infos){
        Collections.sort(infos);

    }

    public void addList(List<ImageInfo> info){

        this.infoList.addAll(info);
        notifyDataSetChanged();
    }

    public void setListener(SelectListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater=LayoutInflater.from(parent.getContext());

        View view=inflater.inflate(R.layout.album_item,parent,false);

        ViewHolder viewHolder=new ViewHolder(view);

        viewHolder.view.setOnClickListener(v -> {
            if (listener!=null){
                int position=viewHolder.getAdapterPosition();

                ImageInfo info=infoList.get(position);

                listener.selected(v,info);
                if (info.getSel()<0) {
                    info.setSel(1);
                }else {
                    info.setSel(-1);
                }

                notifyItemChanged(position);
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (this.infoList!=null){
            ImageInfo info=infoList.get(position);
            Glide.with(App.getContext())
                    .load(info.getPath())
                    .into(holder.imageView);

            if (info.getSel()>0){
                holder.sel.setVisibility(View.VISIBLE);
            }else {
                holder.sel.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {

        if (this.infoList!=null){
            return infoList.size();
        }
        return super.getItemCount();
    }


    static class ViewHolder extends BaseAdapter.ViewHolder {

        View view;
        ImageView imageView;
        ImageView sel;

        ViewHolder(View itemView) {
            super(itemView);

            this.view=itemView;
            this.imageView=itemView.findViewById(R.id.album_image);
            this.sel=itemView.findViewById(R.id.sel_icon);

            reDraw();
        }

        private void reDraw(){

            GridLayoutManager.LayoutParams params= (GridLayoutManager.LayoutParams) view.getLayoutParams();

            int w= App.getContext().getResources().getDisplayMetrics().widthPixels/3;

            params.width=w;
            params.height=w;

            view.setLayoutParams(params);

        }
    }
}
