package com.app.legend.picmaster.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.app.legend.picmaster.R;
import com.app.legend.picmaster.interfaces.CompleteListener;
import com.app.legend.picmaster.interfaces.IMainActivity;
import com.app.legend.picmaster.models.Conf;
import com.app.legend.picmaster.models.ImageInfo;
import com.app.legend.picmaster.utils.App;
import com.app.legend.picmaster.utils.ImageHandler;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class MainPresenter {

    private IMainActivity activity;
    private Conf conf;

    public MainPresenter(IMainActivity activity) {
        this.activity = activity;

        this.conf=new Conf();


    }


    public Conf getConf() {
        return conf;
    }


    /**
     * 设置输出路径
     * @param path 路径
     */
    public void setOutputPath(String path){

        this.conf.setOutputPath(path);

    }

    private boolean checkNumber(String value){
        String regex = "^(-?[1-9]\\d*\\.?\\d*)|(-?0\\.\\d*[1-9])|(-?[0])|(-?[0]\\.\\d*)$";
        return value.matches(regex);
    }


    /**
     * 设置缩放类型
     * @param type 类型
     */
    public void setScaleType(String type){

        switch (type){
            case "保留原大小":

                this.conf.setSCALE_TYPE(10);


                break;
            case "百分比模式":
                this.conf.setSCALE_TYPE(20);


                break;
            case "自定义大小":

                this.conf.setSCALE_TYPE(30);
                break;
        }

    }

    public void setSizeW(String w){
        if (checkNumber(w)){
            this.conf.setSizeW(Float.parseFloat(w));
        }

    }

    public void setSizeH(String h){


        if (checkNumber(h)){
            this.conf.setSizeH(Float.parseFloat(h));
        }

    }

    public void setScaleB(String b){

        if (checkNumber(b)){

            this.conf.setScaleB(Float.parseFloat(b));
        }
    }

    public void setQuality(int quality){

        this.conf.setImageQuality(quality);
    }

    public void setFormat(String format){
        switch (format){
            case "保留原格式":

                this.conf.setFormat(ImageHandler.Builder.OTHER);

                break;
            case "jpg":

                this.conf.setFormat(ImageHandler.Builder.JPG);
                break;
            case "png":
                this.conf.setFormat(ImageHandler.Builder.PNG);
                break;
            case "webp":
                this.conf.setFormat(ImageHandler.Builder.WEBP);
                break;
        }

    }

    public void setRotation(boolean rotation){
        this.conf.setRotation(rotation);
    }

    public void setRotationCount(String d){

        if (d.isEmpty()){
            return;
        }

        double s= Double.parseDouble(d);



        this.conf.setRotationCount(s);
    }

    public void setRetro(boolean retro){
        this.conf.setRetro(retro);
    }

    public void setRetroType(String s){
        switch (s){
            case "上下翻转":
                this.conf.setRETRO_TYPE(ImageHandler.Builder.MIRROR_TOP_TO_BOTTOM);
                break;
            case "左右翻转":
                this.conf.setRETRO_TYPE(ImageHandler.Builder.MIRROR_LEFT_TO_RIGHT);
                break;
        }

    }

    public void setAddwater(boolean addwater){

        this.conf.setAddWater(addwater);

    }

    public void setWaterType(String type){

        switch (type){
            case "图片水印":
//                this.conf.setWaterType(200);
                this.conf.setAddTextWater(false);

                break;
            case "文字水印":

//                this.conf.setWaterType(300);

                this.conf.setAddTextWater(true);
                break;
        }
    }

    public void setWaterText(String text){

        if (text.isEmpty()){
            return;
        }

        this.conf.setTextWater(text);
    }

    public void setTextSize(float size){
        this.conf.setTextSize(size);
    }

    public void setWaterTrans(int trans){

        if (trans<0){
            trans=0;
        }

        if (trans>100){
            trans=100;
        }

        float s=  trans*2.55f;

        if (s>255){
            s=255;
        }

        this.conf.setWaterTrans(s);



    }

    public void setWaterPosition(String position){

        switch (position){
            case "中间":
                this.conf.setWATER_POSITION(ImageHandler.Builder.CENTER);
                break;
            case "上边":
                this.conf.setWATER_POSITION(ImageHandler.Builder.TOP);
                break;
            case "下边":
                this.conf.setWATER_POSITION(ImageHandler.Builder.BOTTOM);
                break;
            case "左边":
                this.conf.setWATER_POSITION(ImageHandler.Builder.CENTER_LEFT);
                break;
            case "右边":
                this.conf.setWATER_POSITION(ImageHandler.Builder.CENTER_RIGHT);
                break;
            case "左上角":
                this.conf.setWATER_POSITION(ImageHandler.Builder.TOP_LEFT);
                break;
            case "右上角":
                this.conf.setWATER_POSITION(ImageHandler.Builder.TOP_RIGHT);
                break;
            case "左下角":
                this.conf.setWATER_POSITION(ImageHandler.Builder.BOTTOM_LEFT);
                break;
            case "右下角":
                this.conf.setWATER_POSITION(ImageHandler.Builder.BOTTOM_RIGHT);
                break;

        }

    }

    public void setWaterPath(Uri uri, Context context){

        Bitmap bitmap= null;
        try {
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
            InputStream inputStream=context.getContentResolver().openInputStream(uri);

            bitmap=BitmapFactory.decodeStream(inputStream);

            int b=bitmap.getByteCount();

            Log.d("b--->>",b+"");

            if (b>1024*1024*10){

                bitmap.recycle();

                bitmap=null;

                Log.d("bitmap---->>","null");
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.conf.setBitmap(bitmap);


    }

    public void setColor(int color){

        this.conf.setColor(color);
    }

    public void toChangeImage(List<ImageInfo> infos){

        String[] strings=new String[infos.size()];

        for (int i=0;i<infos.size();i++){
            strings[i]=infos.get(i).getPath();
        }


        ImageHandler.Builder builder=ImageHandler.ofPath(strings);

        if (this.conf.isRetro()){
            builder.mirror(conf.getRETRO_TYPE());
        }

        if (this.conf.isRotation()){

            builder.rotation(conf.getRotationCount());

        }

        if (conf.isAddWater()){

            if (conf.isAddTextWater()){

                builder.watermark(conf.getTextWater(),conf.getTextSize(),
                        conf.getColor(),
                        conf.getWATER_POSITION(),conf.getWaterTrans());

            }else {

                builder.watermark(conf.getBitmap(),conf.getWATER_POSITION(),conf.getWaterTrans());

            }

        }

        switch (conf.getSCALE_TYPE()){
            case 10:

                break;
            case 20:

                float s=conf.getScaleB()/100;

                builder.scale(s);
                break;
            case 30:

                builder.size((int) conf.getSizeW(),(int) conf.getSizeH());
                break;
        }

        builder.outputQuality((int) this.conf.getImageQuality()).toOutput(conf.getFormat(),conf.getOutputPath());


    }


}
