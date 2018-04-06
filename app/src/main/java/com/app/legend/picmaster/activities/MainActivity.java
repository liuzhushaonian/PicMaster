package com.app.legend.picmaster.activities;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.app.legend.picmaster.R;
import com.app.legend.picmaster.interfaces.CompleteListener;
import com.app.legend.picmaster.interfaces.IMainActivity;
import com.app.legend.picmaster.models.ImageInfo;
import com.app.legend.picmaster.presenters.MainPresenter;
import com.app.legend.picmaster.utils.ImageHandler;
import com.dingmouren.colorpicker.ColorPickerDialog;
import com.dingmouren.colorpicker.OnColorPickerListener;

import java.util.List;

public class MainActivity extends BaseActivity implements IMainActivity,CompleteListener {


    private Toolbar toolbar;
    private Button add;
    private MainPresenter presenter;
    private EditText width, height, bai, rotation, water;
    private SeekBar qualitySeekbar, transSeekbar,textSizeSeekBar;
    private CheckBox rotationBox, retroBox, addWater;
    private Button selImagePath;
    private Spinner scaleType, formatType, retroType, waterPosition;
    private LinearLayout scaleLayout, water_linearLayout,textSizeGroup;
    private TextView quality_text, trans_text,textSizeView;
    private RadioGroup radioGroup;
    private boolean isText = false;
    private TextView about;




    private static final String[] permissionStrings =
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        getPermission();

        getComponent();
        initToolbar();

        presenter = new MainPresenter(this);

        presenter.setOutputPath(IMAGE_PATH);

        initScaleType();
        initQuality();
        initFormat();
        initRotation();
        initRetro();
        initWater();
        initTextSize();

        click();

        initAbout();

        ImageHandler.getHandler().setListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ImageHandler.getHandler().shutdown();
    }

    private void getPermission() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, permissionStrings[0]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permissionStrings[0]}, 1000);

        }else {

            openAlbums();
        }

    }

    private void openAlbums(){

        Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
        startActivityForResult(intent, 3000);
    }

    private void getComponent() {

//        sel_image.findViewById(R.id.sel_image_and_handler);
        textSizeGroup=findViewById(R.id.textSizeGroup);
        textSizeView=findViewById(R.id.textSizeView);
        add = findViewById(R.id.sel_image_and_handler);
        toolbar = findViewById(R.id.main_toolbar);
        width = findViewById(R.id.size_width);
        height = findViewById(R.id.size_height);
        bai = findViewById(R.id.scale_bai);
        rotation = findViewById(R.id.rotation_count);
        water = findViewById(R.id.water_edit_text);
        qualitySeekbar = findViewById(R.id.quality_seekBar);
        transSeekbar = findViewById(R.id.trans_seekBar);
        rotationBox = findViewById(R.id.rotation_checkBox);
        retroBox = findViewById(R.id.retro_checkBox);
        addWater = findViewById(R.id.add_water_checkBox);
        selImagePath = findViewById(R.id.sel_water_button);
        scaleType = findViewById(R.id.scale_type_spinner);
        formatType = findViewById(R.id.format_spinner);
        retroType = findViewById(R.id.retro_type_spinner);
        waterPosition = findViewById(R.id.water_position_spinner);
        scaleLayout = findViewById(R.id.linearLayout);
        quality_text = findViewById(R.id.quality_textView);
        water_linearLayout = findViewById(R.id.water_linearLayout);
        radioGroup = findViewById(R.id.radios);
        textSizeSeekBar=findViewById(R.id.textSizeSeekBar);

        trans_text = findViewById(R.id.trans_textView);
        about=findViewById(R.id.about_text);

    }

    private void initToolbar() {
        toolbar.setTitle("PicMaster");

        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        setSupportActionBar(toolbar);
    }

    private OnColorPickerListener mOnColorPickerListener = new OnColorPickerListener() {
        @Override
        public void onColorCancel(ColorPickerDialog dialog) {//取消选择的颜色

        }

        @Override
        public void onColorChange(ColorPickerDialog dialog, int color) {//实时监听颜色变化

        }

        @Override
        public void onColorConfirm(ColorPickerDialog dialog, int color) {//确定的颜色

            presenter.setColor(color);

            Log.d("color---->>", color + "");

        }
    };

    private void click() {

        add.setOnClickListener(v -> {


            getPermission();

        });

        about.setOnClickListener(v -> {

            initPopup();
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 1000:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    openAlbums();

                } else {
                    Toast.makeText(MainActivity.this, "无法获取权限，请赋予相关权限", Toast.LENGTH_SHORT).show();
                }


                break;
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 3000:

                if (data == null) {
                    return;
                }

                List<ImageInfo> infos = (List<ImageInfo>) data.getSerializableExtra("images");

//                presenter.toChangeImage(infos);

                startHandler(infos);


                break;
            case 400://略缩图
                if (data == null) {
                    return;
                }

                Uri uri = data.getData();

                String path = ImageHandler.getHandler().handleImageOnKitKat(uri, this);

                presenter.setWaterPath(uri, this);

                water.setText(path);

                break;
        }

    }


    /**
     * 初始化缩放方式
     */
    private void initScaleType() {

        presenter.setScaleType(scaleType.getAdapter().getItem(scaleType.getSelectedItemPosition()).toString());

        scaleType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String s = (String) scaleType.getAdapter().getItem(position);

//                Log.d("s---->>", s);

                presenter.setScaleType(s);

                switch (s) {
                    case "百分比模式":
                        scaleLayout.setVisibility(View.VISIBLE);
                        width.setVisibility(View.GONE);
                        height.setVisibility(View.GONE);
                        bai.setVisibility(View.VISIBLE);
                        break;
                    case "自定义大小":

                        scaleLayout.setVisibility(View.VISIBLE);
                        width.setVisibility(View.VISIBLE);
                        height.setVisibility(View.VISIBLE);
                        bai.setVisibility(View.GONE);


                        break;
                    default:
                        scaleLayout.setVisibility(View.GONE);
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        width.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                presenter.setSizeW(s.toString());

            }
        });

        height.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                presenter.setSizeH(s.toString());
            }
        });

        bai.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                presenter.setScaleB(s.toString());
            }
        });

    }

    private void initQuality() {

        presenter.setQuality(100);//默认100

        qualitySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                String s = "图片质量 " + progress + "%";

                quality_text.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();

                Log.d("progress---->>",progress+"");

                presenter.setQuality(progress);
            }
        });
    }

    private void initFormat() {

        presenter.setFormat((String) formatType.getAdapter().getItem(formatType.getSelectedItemPosition()));

        formatType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String format = (String) formatType.getAdapter().getItem(position);

                presenter.setFormat(format);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void initRotation() {

        rotationBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            presenter.setRotation(isChecked);
            if (isChecked) {

                rotation.setVisibility(View.VISIBLE);
            } else {
                rotation.setVisibility(View.GONE);
            }

        });

        rotation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                presenter.setRotationCount(s.toString());
            }
        });

    }

    private void initRetro() {

        retroBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            presenter.setRetro(isChecked);

            if (isChecked) {
                retroType.setVisibility(View.VISIBLE);
            } else {
                retroType.setVisibility(View.GONE);
            }
        });

        presenter.setRetroType("上下翻转");

        retroType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = (String) retroType.getAdapter().getItem(position);

                presenter.setRetroType(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initWater() {

        addWater.setOnCheckedChangeListener((buttonView, isChecked) -> {
            presenter.setAddwater(isChecked);

            if (isChecked) {

                water_linearLayout.setVisibility(View.VISIBLE);
                waterPosition.setVisibility(View.VISIBLE);


            } else {

                water_linearLayout.setVisibility(View.GONE);
                waterPosition.setVisibility(View.GONE);

            }

        });

        waterPosition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String p = (String) waterPosition.getAdapter().getItem(position);
                presenter.setWaterPosition(p);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton button = findViewById(checkedId);
            String s = button.getText().toString();

            switch (s) {
                case "图片水印":
//                    selImagePath.setVisibility(View.VISIBLE);
                    selImagePath.setText("选择图片");
                    selImagePath.setBackgroundColor(ContextCompat.getColor(this, R.color.colorTeal));
                    textSizeGroup.setVisibility(View.GONE);

                    water.setText("");
                    water.setHint("请选择图片\n");
                    isText = false;
                    break;
                case "文字水印":
//                    selImagePath.setVisibility(View.GONE);
                    selImagePath.setText("选择颜色");
                    selImagePath.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPink));
                    textSizeGroup.setVisibility(View.VISIBLE);
                    water.setText("");
                    water.setHint("请输入文字，建议不超过10个字");
                    isText = true;
                    break;
            }

            presenter.setWaterType(s);

        });

        water.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                presenter.setWaterText(s.toString());
            }
        });

        //打开相册
        selImagePath.setOnClickListener(v -> {

            if (isText){

                new ColorPickerDialog(
                        MainActivity.this,
                        getResources().getColor(R.color.colorPrimary),
                        false,
                        mOnColorPickerListener
                ).show();

            }else {

                openAlbum(400);
            }
        });

        presenter.setWaterTrans(100);

        transSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String s = "水印不透明度 " + progress + "%";
                trans_text.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                int progress = seekBar.getProgress();

                presenter.setWaterTrans(progress);
            }
        });


    }

    private void initTextSize(){

        float textSize=getResources().getDimension(R.dimen.text_size);

        presenter.setTextSize(px2sp(textSize));

        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                float s=textSize;
                s+=progress;

                textSizeView.setTextSize(px2sp(s));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                float s=seekBar.getProgress()+textSize;

                presenter.setTextSize(px2sp(s));
            }
        });


    }

    public int px2sp(float pxValue) {
        final float fontScale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    @Override
    public void complete() {

       handler.sendEmptyMessage(10);
    }

    Handler handler=new Handler(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case 10:
                    initNotification();
                    endHandler();

                    Toast.makeText(MainActivity.this,"处理完成",Toast.LENGTH_LONG).show();
                    break;
                case 20:
                    resume();

                    break;
            }
        }
    };


    private void initNotification(){

        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_check_circle_black_24dp)
                        .setContentTitle("处理完成")
                        .setContentText("处理好的图片在sdcard/PicMaster文件夹下");

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(100, mBuilder.build());

    }

    private void startHandler(List<ImageInfo> infos){

        presenter.toChangeImage(infos);

        add.setClickable(false);
        add.setBackgroundColor(ContextCompat.getColor(this,R.color.colorGrey));
        add.setText(getResources().getText(R.string.string_handle));
    }

    private void endHandler(){

        add.setClickable(false);
        add.setBackgroundColor(ContextCompat.getColor(this,R.color.colorSuccess));
        add.setText(getResources().getText(R.string.string_success));

        new Thread(){
            @Override
            public void run() {
                super.run();


                try {
                    sleep(2000);
                    handler.sendEmptyMessage(20);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void resume(){

        add.setClickable(true);
        add.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPink));
        add.setText(getResources().getText(R.string.string_add));
    }

    private void initAbout(){

        SharedPreferences sharedPreferences=getSharedPreferences(ABOUT,MODE_PRIVATE);

        int tag=sharedPreferences.getInt("tag",-1);

        if (tag<0){//第一次打开
            initPopup();

            tag=1;

            sharedPreferences.edit().putInt("tag",tag).apply();
        }

    }

    private void initPopup(){

        final AlertDialog.Builder builder=new AlertDialog.Builder(this);

        LayoutInflater inflater=LayoutInflater.from(this);

        View view=inflater.inflate(R.layout.main_item,null,false);

        TextView textView=view.findViewById(R.id.about);

        textView.setText(getResources().getText(R.string.string_about));

        builder.setView(view).setTitle("给用户的说明书");
        builder.show();

    }
}
