package com.app.legend.picmaster.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.app.legend.picmaster.interfaces.CompleteListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ImageHandler {

    private ThreadPoolExecutor poolExecutor;
    private int CORE_COUNT = Runtime.getRuntime().availableProcessors();
    private int CORE_POOL_SIZE = 2;
    private int CORE_POOL_MAX_SIZE = 3;
    private int KEEP_ALIVE = 1;
    public static final int FILE = 0x0011;
    public static final int URI = 0x0012;
    public static final int STRING = 0x0013;
    private LruCache<String, Bitmap> lruCache;
    private int taskSize=0;

    private int count=0;
    private CompleteListener listener;

    private static volatile ImageHandler handler;

    public void setListener(CompleteListener listener) {
        this.listener = listener;
    }

    public static ImageHandler getHandler() {

        if (handler == null) {
            synchronized (ImageHandler.class) {
                handler = new ImageHandler();
            }
        }

        return handler;

    }

    public void shutdown(){

        if (this.poolExecutor!=null){
            poolExecutor.shutdown();
        }
    }

    public int getTaskSize() {
        return taskSize;
    }

    public ImageHandler setTaskSize(int taskSize) {
        this.taskSize = taskSize;
        return handler;
    }

    public ImageHandler() {

        BlockingQueue<Runnable> runnableBlockingQueue = new LinkedBlockingQueue<>();
        poolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, CORE_POOL_MAX_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, runnableBlockingQueue);


        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;

        lruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };

    }

    private String getImagePath(Uri uri, String selection, Context context) {
        String path = null;
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }

            cursor.close();
        }
        return path;
    }

    public String handleImageOnKitKat(Uri uri, Context context) {
        String imagePath = null;


        if (DocumentsContract.isDocumentUri(context, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                //Log.d(TAG, uri.toString());
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, context);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                //Log.d(TAG, uri.toString());
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null, context);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //Log.d(TAG, "content: " + uri.toString());
            imagePath = getImagePath(uri, null, context);
        }

        return imagePath;
    }


    /**
     * 线程池内执行
     *
     * @param builder
     */
    public void runWithThreadPool(Builder builder, Uri uri) {

        Runnable runnable = () -> {

            Bitmap bitmap = null;
            try {

                bitmap = BitmapFactory.decodeStream(builder.context.getContentResolver().openInputStream(uri));

                String path = handleImageOnKitKat(uri, builder.context);

                Log.d("path---->>", path);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            String path = handleImageOnKitKat(uri, builder.context);

            String name = path.substring(path.lastIndexOf("/") + 1, path.length());


            handlerPic(builder, bitmap, name);

        };

        poolExecutor.execute(runnable);

    }

    public void runWithThreadPool(Builder builder, File file) {

        Runnable runnable = () -> {

            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            String name = file.getName().substring(0, file.getName().length());

            handlerPic(builder, bitmap, name);
        };

        poolExecutor.execute(runnable);


//        handlerPic(builder,bitmap);

    }

    public void runWithThreadPool(Builder builder, String s) {

        Runnable runnable = () -> {
            Bitmap bitmap = BitmapFactory.decodeFile(s);

            String name = s.substring(s.lastIndexOf("/") + 1, s.length());

            handlerPic(builder, bitmap, name);

        };

        poolExecutor.execute(runnable);

//        handlerPic(builder,bitmap);

    }


    /**
     * 解析builder并处理图片
     * <p>
     * 1、先处理翻转
     * 2、处理缩放
     * 3、处理水印
     * 4、保存并设置质量
     *
     * @param builder
     */
    private void handlerPic(Builder builder, Bitmap bitmap, String name) {

        if (bitmap != null) {

            if (builder.mirrorType > 0) {//判断是否需要翻转，大于0则需要
                bitmap = getTurnOverBitmap(bitmap, builder.mirrorType);
            }

            if (builder.scale > 0) {

                bitmap = getScaleBitmap(bitmap, builder.scale);
            }

            if (builder.size_w > 0 && builder.size_h > 0) {

                bitmap = getSizeBitmap(bitmap, builder.size_w, builder.size_h);
            }

            if (builder.watermark != null) {//图片水印，不用缓存

                bitmap = getWatermarkBitmap(bitmap, builder.watermark, builder.waterPosition, (int) builder.trans);

            } else if (builder.waterText != null) {//文字水印,需要缓存


                String cache_name=builder.waterText+builder.textSize+builder.color+"";

                Bitmap water = getBitmapFromMemory(cache_name);
                if (water == null) {//从缓存处获取不到图片

                    water = makeTextWater(builder.waterText, builder.textSize, builder.color);

                    if (water != null) {//缓存
                        cacheInMemory(water, cache_name);
                    }

                }

                bitmap = getWatermarkBitmap(bitmap, water, builder.waterPosition, (int) builder.trans);


            }

            if (builder.rotation_count > 0) {//旋转


                bitmap = getRotationBitmap(bitmap, builder.rotation_count);
            }

            saveBitmap(bitmap, builder.format, builder.quality, builder.outputPath, name);


        } else {
            Log.w("#ImageHandler-->:", "the bitmap is null!");
        }


    }


    /**
     * 翻转图片
     *
     * @param bitmap 翻转source
     * @param type   翻转类型
     * @return 返回Bitmap
     * 出处：http://www.loongwind.com/archives/72.html（十分感谢）
     */
    private Bitmap getTurnOverBitmap(Bitmap bitmap, int type) {

        if (bitmap == null) {
            return null;
        }

        Canvas canvas = new Canvas();
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(output);
        Matrix matrix = new Matrix();
        // 缩放 当sy为-1时向上翻转 当sx为-1时向左翻转 sx、sy都为-1时相当于旋转180°

        switch (type) {
            case Builder.MIRROR_LEFT_TO_RIGHT://左右翻转
                matrix.postScale(-1, 1, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
                // 因为向上翻转了所以y要向下平移一个bitmap的高度
//                matrix.postTranslate(0, bitmap.getHeight());


                break;
            case Builder.MIRROR_TOP_TO_BOTTOM://上下翻转

                matrix.postScale(1, -1, bitmap.getWidth() / 2, bitmap.getHeight() / 2);

//                matrix.postTranslate(bitmap.getWidth(),0);


                break;
        }

        canvas.drawBitmap(bitmap, matrix, null);


        return output;
    }


    private Bitmap getSizeBitmap(Bitmap bitmap, float w, float h) {

        if (bitmap == null) {
            return null;
        }

        float yw = bitmap.getWidth();
        float yh = bitmap.getHeight();

        float dy=w/yw;
        float dh=h/yh;
        Bitmap bitmap1=null;

        if (w < yw || h < yh) {


            bitmap = Bitmap.createScaledBitmap(bitmap,(int) w,(int)h,true);

            ByteArrayOutputStream b=getOutStreamFromBitmap(bitmap,100,"jpg");

            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inJustDecodeBounds=true;

            bitmap1=BitmapFactory.decodeByteArray(b.toByteArray(),0,b.size(),options);

            options.inSampleSize=reSize(options,(int) w,(int) h);

            b=getOutStreamFromBitmap(bitmap,100,"jpg");

            options.inJustDecodeBounds=false;

            bitmap1=BitmapFactory.decodeByteArray(b.toByteArray(),0,b.size(),options);

            bitmap.recycle();

        }

        return bitmap1;


    }

    private int reSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        int size=1;

        int width=options.outWidth;

        int height=options.outHeight;

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

//        Log.d("size------>>>>>>>",size+"");

        return size;
    }

    private Bitmap getScaleBitmap(Bitmap bitmap, float scale) {
        if (bitmap == null) {
            return null;
        }

//        Log.d("scale--->>",scale+"");

        float yw = bitmap.getWidth();
        float yh = bitmap.getHeight();

//        Matrix matrix = new Matrix();

//        matrix.postScale(scale, scale, yw / 2, yh / 2);
//
//        bitmap = Bitmap.createBitmap(bitmap, 0, 0, (int) (yw * scale), (int) (yh * scale), matrix, true);

        bitmap=Bitmap.createScaledBitmap(bitmap,(int) (yw * scale), (int) (yh * scale),false);

        return bitmap;
    }

    private Bitmap getRotationBitmap(Bitmap bitmap, double r) {

        if (bitmap == null) {
            return null;
        }

        Matrix matrix = new Matrix();

        float px = bitmap.getWidth() / 2;
        float py = bitmap.getHeight() / 2;

        matrix.postRotate((float) r, px, py);//旋转

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

    }


    /**
     * 设置水印
     *
     * @param bitmap   原图
     * @param water    水印
     * @param position 位置
     * @return 返回添加好水印的图
     */
    private Bitmap getWatermarkBitmap(Bitmap bitmap, Bitmap water, int position,int alpha) {

        if (bitmap == null) {

            Log.w("#ImageHandler-->:", "the bitmap is null!!");
            return null;
        }

        float w = bitmap.getWidth();
        float h = bitmap.getHeight();


        Bitmap output = Bitmap.createBitmap((int) w, (int) h, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        canvas.drawBitmap(bitmap, 0, 0, null);

        Log.d("alpha---->>",alpha+"");

        Paint paint=new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(alpha);

        if (water != null) {//在水印不为null的前提下执行，否则只返回原图

            float ww = water.getWidth();
            float wh = water.getHeight();

            switch (position) {
                case Builder.BOTTOM://底部
                    canvas.drawBitmap(water, w / 2 - ww / 2, h - wh, paint);

                    break;
                case Builder.BOTTOM_LEFT://左下

                    canvas.drawBitmap(water, 0, h - wh, paint);

                    break;
                case Builder.BOTTOM_RIGHT://右下

                    canvas.drawBitmap(water, w - ww, h - wh, paint);
                    break;
                case Builder.TOP://顶部

                    canvas.drawBitmap(water, w / 2 - ww / 2, 0, paint);
                    break;
                case Builder.TOP_LEFT://左上

                    canvas.drawBitmap(water, 0, 0, paint);
                    break;
                case Builder.TOP_RIGHT://右上

                    canvas.drawBitmap(water, w - ww, 0, paint);
                    break;
                case Builder.CENTER://中间

                    canvas.drawBitmap(water, w / 2 - ww / 2, h / 2 - wh / 2, paint);
                    break;
                case Builder.CENTER_LEFT://左边

                    canvas.drawBitmap(water, 0, h / 2 - wh / 2, paint);
                    break;
                case Builder.CENTER_RIGHT://右边

                    canvas.drawBitmap(water, w - ww, h / 2 - wh / 2, paint);
                    break;

            }

        }

        canvas.save(Canvas.ALL_SAVE_FLAG);

        canvas.restore();

        return output;


    }

    /**
     * 文字水印
     *
     * @param text     文字
     * @param textSize 文字大小
     * @param color    文字颜色
     * @return 返回Bitmap
     */
    private Bitmap makeTextWater(String text, float textSize, int color) {

        Paint paint = new Paint();

        paint.setTextSize(textSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float height = fontMetrics.descent - fontMetrics.ascent;
        float width = paint.measureText(text);

        int baseline = (int) ((height - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top);

        paint.setColor(color);
//        paint.setAlpha((int) alpha);

        Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);


        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(Color.TRANSPARENT);

        canvas.drawText(text, 0, baseline, paint);//绘制文字

        canvas.drawBitmap(bitmap, 0, 0, paint);//做成Bitmap
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap;

    }

    //保存图片
    private void saveBitmap(Bitmap bitmap, String format, float quality, String path, String name) {

        if (bitmap == null) {
            return;
        }
        if (quality<=40){

            bitmap=bitmap.copy(Bitmap.Config.ALPHA_8,true);

        }else if (quality<=60){

            bitmap=bitmap.copy(Bitmap.Config.RGB_565,true);
        }else {
            bitmap=bitmap.copy(Bitmap.Config.ARGB_4444,true);
        }


        String other=name.substring(name.lastIndexOf(".")+1,name.length());

        name=name.substring(0,name.lastIndexOf("."));



//        File picFile = null;

        try {

            switch (format) {
                case Builder.JPG:
//                    picFile = new File(path + "/" + name + "." + format);
//
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, (int) quality, new FileOutputStream(picFile));



//                    break;
                case Builder.PNG:

//                    picFile = new File(path + "/" + name + "." + format);
//                    bitmap.compress(Bitmap.CompressFormat.PNG, (int) quality, new FileOutputStream(picFile));
//                    break;
                case Builder.WEBP:

                    ByteArrayOutputStream bos=getOutStreamFromBitmap(bitmap, (int) quality,format);

                    writeImage2File(bos,path,name,format);
//                    picFile = new File(path + "/" + name + "." + format);
//                    bitmap.compress(Bitmap.CompressFormat.WEBP, (int) quality, new FileOutputStream(picFile))
                    break;
                default://其他格式
//                    String other = name.substring(name.lastIndexOf(".") + 1, name.length());

//                    picFile = new File(path + "/" + name + "." + other);

                    switch (other) {
                        case "jpg":

//                            bitmap.compress(Bitmap.CompressFormat.JPEG, (int) quality, new FileOutputStream(picFile));
//                            break;
                        case "png":
//                            bitmap.compress(Bitmap.CompressFormat.PNG, (int) quality, new FileOutputStream(picFile));
//                            break;
                        case "webp":

                            ByteArrayOutputStream bos1=getOutStreamFromBitmap(bitmap, (int) quality,other);

                            boolean b=writeImage2File(bos1,path,name,other);

                            Log.d("b---->>",b+"");

//                            bitmap.compress(Bitmap.CompressFormat.WEBP, (int) quality, new FileOutputStream(picFile));
                            break;

                        default:

                            Log.d("ImageHandler--->>","无法找到适合的格式，不作处理");

                            break;
                    }

//                    bitmap.compress(Bitmap.CompressFormat.valueOf(other),(int) quality,new FileOutputStream(picFile));

                    break;
            }


        } catch (Exception e) {
            e.printStackTrace();


        }
    }

    private ByteArrayOutputStream getOutStreamFromBitmap(Bitmap bitmap, int quality,String format){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        switch (format){
            case Builder.JPG:
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                break;
            case Builder.PNG:
                bitmap.compress(Bitmap.CompressFormat.PNG, quality, bos);
                break;
            case Builder.WEBP:
                bitmap.compress(Bitmap.CompressFormat.WEBP, quality, bos);
                break;
        }


        if(bos != null){
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bos;
    }

    public boolean writeImage2File(ByteArrayOutputStream bos,String savePath,String imageName,String format){




        File file =new File(savePath);
        if(!file.exists()){
            file.mkdirs();
        }
        FileOutputStream fos;
        try {
            if(TextUtils.isEmpty(imageName)){
                imageName = System.currentTimeMillis()+"";
            }
            File f = new File(file,imageName+"."+format);
            fos = new FileOutputStream(f);
            fos.write(bos.toByteArray());
            fos.flush();

        } catch (Exception e) {
            e.printStackTrace();


            count++;

            if (count>=taskSize){

                if (this.listener!=null){
                    listener.complete();
                }

                count=0;

            }

            return false;
        }

        count++;


        if (count>=taskSize){

            if (this.listener!=null){
                listener.complete();
            }

            count=0;

        }

        return true;
    }


    /**
     * 写入内存缓存
     *
     * @param bitmap 保存bitmap，避免大量创建相同的Bitmap
     * @param url    Bitmap的路径或是文字，因为水印分为文字水印与图片水印
     */
    private void cacheInMemory(Bitmap bitmap, String url) {
        String name = getMd5(url);

        lruCache.put(name, bitmap);
    }

    /**
     * 读取内存缓存
     *
     * @param url Bitmap的路径或是文字
     * @return 返回Bitmap
     */
    private Bitmap getBitmapFromMemory(String url) {
        String name = getMd5(url);

        Bitmap bitmap = lruCache.get(name);

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


    public static Builder ofFile(File... files) {
        Builder builder = new Builder();

        builder.setFiles(files);
        return builder;
    }


    public static Builder ofUri(Context context, Uri... uris) {
        Builder builder = new Builder();
        builder.setUris(uris, context);
        return builder;
    }


    public static Builder ofPath(String... strings) {
        Builder builder = new Builder();
        builder.setStrings(strings);
        return builder;
    }

    public static Builder ofFileList(List<File> fileList) {
        Builder builder = new Builder();

        return builder;
    }


    public static class Builder {

        private Uri[] uris = null;
        private String[] strings = null;
        private File[] files = null;


        public static final int TOP = 0x00100;
        public static final int TOP_LEFT = 0x00200;
        public static final int TOP_RIGHT = 0x00300;
        public static final int CENTER = 0x00400;
        public static final int CENTER_LEFT = 0x00500;
        public static final int CENTER_RIGHT = 0x00600;
        public static final int BOTTOM = 0x00700;
        public static final int BOTTOM_LEFT = 0x00800;
        public static final int BOTTOM_RIGHT = 0x00900;
        public static final int MIRROR_TOP_TO_BOTTOM = 0x001000;
        public static final int MIRROR_LEFT_TO_RIGHT = 0x002000;
        public static final String JPG = "jpg";
        public static final String PNG = "png";
        public static final String WEBP = "webp";
        public static final String OTHER = "other";

        private Context context;

        private float size_w = -1;
        private float size_h = -1;

        private int scale_w = -1;
        private int scale_h = -1;
        private float scale = -1;

        private Bitmap watermark;
        private double rotation_count;
        private int waterPosition = -1;
        private float trans = -1;
        private String waterText;
        private float textSize = 20;
        private int color = -1;

        private String format;
        private String outputPath;

        private int mirrorType = -1;

        private int type = -1;
        private int quality = 100;

        public Uri[] getUris() {
            return uris;
        }

        public void setUris(Uri[] uris, Context context) {
            type = URI;
            this.uris = uris;
            this.context = context;
        }

        public String[] getStrings() {
            return strings;
        }

        public void setStrings(String[] strings) {
            type = STRING;
            this.strings = strings;
        }

        public File[] getFiles() {

            return files;
        }

        public void setFiles(File[] files) {
            type = FILE;
            this.files = files;
        }

        public Builder size(float w, float h) {
            this.size_w = w;
            this.size_h = h;
            this.scale=-1;

            return this;
        }

        public Builder scale(float scale) {

            this.scale=scale;
            this.size_w = -1;
            this.size_h = -1;


            return this;

        }

        public Builder rotation(double r) {
            this.rotation_count = r;
            return this;
        }

        public Builder watermark(Bitmap bitmap, int position, float trans) {
            this.watermark = bitmap;
            this.waterPosition = position;
            this.trans = trans;

            this.waterText = null;

            return this;
        }

        public Builder watermark(String text, float textSize, int color, int position, float trans) {

            this.waterText = text;
            this.waterPosition = position;
            this.textSize = textSize;
            this.color = color;
            this.trans = trans;

            this.watermark = null;

            return this;

        }

        public Builder mirror(int type) {
            this.mirrorType = type;

            return this;
        }

        public Builder outputQuality(int quality){

            this.quality=quality;

            return this;
        }


        public void toOutput(String format, String path) {

            this.format = format;
            this.outputPath = path;

            if (type == FILE) {

                for (File file : files) {
                    ImageHandler.getHandler().setTaskSize(files.length).runWithThreadPool(this, file);
                }

            } else if (type == URI) {

                for (Uri uri : uris) {
                    ImageHandler.getHandler().setTaskSize(uris.length).runWithThreadPool(this, uri);
                }

            } else if (type == STRING) {

                for (String s : strings) {
                    ImageHandler.getHandler().setTaskSize(strings.length).runWithThreadPool(this, s);
                }
            }

//            else if (this.strings!=null){
//                for (String s:strings){
//                    ImageHandler.getHandler().runWithThreadPool(this,s);
//                }
//
//            }

        }

    }


}
