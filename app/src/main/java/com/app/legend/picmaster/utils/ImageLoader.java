package com.app.legend.picmaster.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.app.legend.picmaster.models.ImageInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ImageLoader {


    private volatile static ImageLoader loader;
    private LruCache<String,Bitmap> lruCache;
    private static String CACHE_PATH= "";//文件缓存



    private ImageLoader() {

        int maxMemory= (int) (Runtime.getRuntime().maxMemory()/1024);
        int cacheSize=maxMemory/8;

        lruCache=new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                return bitmap.getRowBytes()*bitmap.getHeight()/1024;
            }
        };
    }

    public static ImageLoader getLoader(Context context){

        if (loader==null){

            synchronized (ImageLoader.class){
                loader=new ImageLoader();
                CACHE_PATH=context.getFilesDir().getAbsolutePath();


            }
        }
        return loader;
    }



    /**
     * 本地缓存
     * @param bitmap
     * @param url MD5加密命名
     */
    private void cacheImageInDisk(Bitmap bitmap, String url){
        String name=getMd5(url);

        try {


            File file=new File(CACHE_PATH,name);



            File parentFile=file.getParentFile();

            if (!parentFile.exists()){
                parentFile.mkdirs();
            }

            bitmap.compress(Bitmap.CompressFormat.WEBP,100,new FileOutputStream(file));

        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

    }

    /**
     * 从本地读取缓存
     * @param url
     * @return
     */
    private Bitmap getBitmapFromDisk(String url,int reqWidth,int reqHeight) {
        Bitmap bitmap = null;
        String name = getMd5(url);

        File file = new File(CACHE_PATH).getAbsoluteFile();

        String file_path = file + "/" + name;

        try {

            BitmapFactory.Options options = new BitmapFactory.Options();


//        options.inSampleSize=2;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            options.inJustDecodeBounds = true;

            bitmap = BitmapFactory.decodeFile(file_path, options);

            options.inSampleSize = reSize(options, reqWidth, reqHeight);

            options.inJustDecodeBounds = false;

            bitmap = BitmapFactory.decodeFile(file_path, options);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != bitmap) {
                //还需要进行内存缓存
                cacheInMemory(bitmap, url);
            }

            return bitmap;
        }
    }

    private Bitmap getLocalBitmap(ImageInfo info,int w,int h){

        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;

        Bitmap bitmap=BitmapFactory.decodeFile(info.getPath(),options);
        options.inSampleSize=reSize(options,w,h);

        options.inPreferredConfig= Bitmap.Config.RGB_565;

        options.inJustDecodeBounds=false;

        bitmap=BitmapFactory.decodeFile(info.getPath(),options);

        if (bitmap!=null){
            cacheInMemory(bitmap,info.getName());
            cacheImageInDisk(bitmap,info.getName());

        }

        return bitmap;



    }


    /**
     * 写入内存缓存
     * @param bitmap
     * @param url
     */
    private void cacheInMemory(Bitmap bitmap,String url){
        String name=getMd5(url);

        lruCache.put(name,bitmap);
    }

    /**
     * 读取内存缓存
     * @param url
     * @return
     */
    private Bitmap getBitmapFromMemory(String url){
        String name=getMd5(url);

        Bitmap bitmap=lruCache.get(name);

        return bitmap;
    }

    //md5加密改名
    private String getMd5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            //32位加密
            return buf.toString();
            // 16位的加密
            //return buf.toString().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

    }

    private int reSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        int size=1;

        int width=options.outWidth;

        int height=options.outHeight;

        Log.d("width--->>",width+"height--->>"+height);
        Log.d("rw--->>",reqWidth+"rh---->>"+reqHeight);


        if (height>reqHeight||width>reqWidth){
//            int halfHeight=height/2;
//
//            int halfWidth=width/2;
            final int heightRatio = Math.round((float) height / (float) reqHeight);
                  final int widthRatio = Math.round((float) width / (float) reqWidth);
                    // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
                   // 一定都会大于等于目标的宽和高。
            size = heightRatio < widthRatio ? heightRatio : widthRatio;

        }

        Log.d("size------>>>>>>>",size+"");

        return size;
    }

    public void bindImage(ImageInfo info,ImageView imageView,int w,int h){
        bindImageToView(info,imageView,w,h);
    }


    private void bindImageToView(ImageInfo info,ImageView imageView,int w,int h){

        Observable
                .create((ObservableOnSubscribe<Result>) e -> {
                    Bitmap bitmap=getBitmapFromMemory(info.getName());

                    if (bitmap==null){

                        bitmap=getBitmapFromDisk(info.getName(),w,h);
                    }

                    if (bitmap==null){

                        bitmap=getLocalBitmap(info,w,h);

                    }

                    Result result=new Result();
                    result.setBitmap(bitmap);
                    result.setImageView(imageView);

                    e.onNext(result);



                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Result result) {

                        if (result.getBitmap()!=null){
                            result.imageView.setImageBitmap(result.bitmap);
                        }


                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }



    static class Result{

        private Bitmap bitmap;
        private int w;
        private int h;
        private ImageView imageView;

        public Result(Bitmap bitmap,int w, int h, ImageView imageView) {
            this.bitmap = bitmap;

            this.w = w;
            this.h = h;
            this.imageView = imageView;
        }

        public Result() {
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }


        public void setW(int w) {
            this.w = w;
        }

        public void setH(int h) {
            this.h = h;
        }

        public void setImageView(ImageView imageView) {
            this.imageView = imageView;
        }

        public Bitmap getBitmap() {

            return bitmap;
        }


        public int getW() {
            return w;
        }

        public int getH() {
            return h;
        }

        public ImageView getImageView() {
            return imageView;
        }
    }


}
