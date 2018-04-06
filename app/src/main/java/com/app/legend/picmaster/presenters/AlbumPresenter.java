package com.app.legend.picmaster.presenters;

import android.content.Context;

import com.app.legend.picmaster.interfaces.IAlbumActivity;
import com.app.legend.picmaster.models.ImageInfo;
import com.app.legend.picmaster.utils.ImageUtils;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AlbumPresenter {

    private IAlbumActivity activity;

    public AlbumPresenter(IAlbumActivity activity) {
        this.activity = activity;
    }

    public void getData(Context context){

        Observable
                .create((ObservableOnSubscribe<List<ImageInfo>>) e -> {
                    List<ImageInfo> infos= ImageUtils.getList(context);
                    e.onNext(infos);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<ImageInfo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<ImageInfo> imageInfos) {

                        activity.setAlbumData(imageInfos);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }
}
