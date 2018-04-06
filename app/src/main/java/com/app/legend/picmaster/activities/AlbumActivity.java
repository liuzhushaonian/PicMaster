package com.app.legend.picmaster.activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.app.legend.picmaster.R;
import com.app.legend.picmaster.adapter.AlbumAdapter;
import com.app.legend.picmaster.interfaces.IAlbumActivity;
import com.app.legend.picmaster.interfaces.SelectListener;
import com.app.legend.picmaster.models.ImageInfo;
import com.app.legend.picmaster.presenters.AlbumPresenter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends BaseActivity implements IAlbumActivity{

    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;
    private AlbumAdapter adapter;
    private Toolbar toolbar;
    private AlbumPresenter presenter;
    private List<ImageInfo> selList;
    private FloatingActionButton done;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        getComponent();

        initList();

        initToolbar();

        presenter=new AlbumPresenter(this);

        presenter.getData(this);
        selList=new ArrayList<>();

        click();
    }

    private void getComponent(){

        toolbar=findViewById(R.id.album_toolbar);
        recyclerView=findViewById(R.id.album_list);
        done=findViewById(R.id.sel_done);
    }

    private void initList(){

        gridLayoutManager=new GridLayoutManager(AlbumActivity.this,3);

        adapter=new AlbumAdapter();
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setListener((view, info) -> {

            if (!this.selList.contains(info)){
                this.selList.add(info);
                if (done.getVisibility()==View.GONE){
                    done.setVisibility(View.VISIBLE);
                }
            }else {
                this.selList.remove(info);

                if (selList.isEmpty()){
                    done.setVisibility(View.GONE);
                }
            }
        });

    }



    private void initToolbar(){

        toolbar.setTitle("选择图片");

        setSupportActionBar(toolbar);

        toolbar.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        toolbar.setNavigationOnClickListener(v -> {

            finish();
        });



    }

    private void click(){

        done.setOnClickListener(v -> {

            Intent intent=new Intent();
            intent.putExtra("images",(Serializable) this.selList);

            setResult(3000,intent);

            finish();

        });
    }




    @Override
    public void setAlbumData(List<ImageInfo> infos) {

        adapter.setInfoList(infos);

    }
}
