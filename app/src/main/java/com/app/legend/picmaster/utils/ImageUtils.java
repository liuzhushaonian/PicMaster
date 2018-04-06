package com.app.legend.picmaster.utils;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;

import com.app.legend.picmaster.models.ImageInfo;

import java.util.ArrayList;
import java.util.List;

public class ImageUtils {

    public static List<ImageInfo> getList(Context context){


        ContentResolver resolver = context.getContentResolver();
        final String SORT_ORDER = MediaStore.Images.Media.DATE_MODIFIED + " DESC";

        List<ImageInfo> infos=new ArrayList<>();

        Cursor cursor = null;
        try {
            //查询数据库，参数分别为（路径，要查询的列名，条件语句，条件参数，排序）
            cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null ,null, SORT_ORDER);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    ImageInfo image = new ImageInfo();
                    image.setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID))); //获取唯一id
                    image.setPath(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))); //文件路径
                    image.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))); //文件名


                    //...   还有很多属性可以设置
                    //可以通过下一行查看属性名，然后在Images.Media.里寻找对应常量名


                    //获取缩略图（如果数据量大的话，会很耗时——需要考虑如何开辟子线程加载）
                    /*
                     * 可以访问android.provider.MediaStore.Images.Thumbnails查询图片缩略图
                     * Thumbnails下的getThumbnail方法可以获得图片缩略图，其中第三个参数类型还可以选择MINI_KIND
                     */

                    infos.add(image);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return infos;

    }

}
