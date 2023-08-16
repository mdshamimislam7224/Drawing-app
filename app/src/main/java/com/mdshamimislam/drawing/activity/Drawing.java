package com.mdshamimislam.drawing.activity;

import static android.view.MotionEvent.INVALID_POINTER_ID;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.MotionEventCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.app.drawing.Util.CanvasView;
import com.google.android.material.appbar.MaterialToolbar;
import com.mdshamimislam.drawing.R;
import com.mdshamimislam.drawing.adapter.ColorAdapter;
import com.mdshamimislam.drawing.database.DatabaseHandler;
import com.mdshamimislam.drawing.eventbus.Events;
import com.mdshamimislam.drawing.eventbus.GlobalBus;
import com.mdshamimislam.drawing.item.ColorList;
import com.mdshamimislam.drawing.util.Constant;
import com.mdshamimislam.drawing.util.Method;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Drawing extends AppCompatActivity {

    private Method method;
    private Dialog dialog;
    private DatabaseHandler db;
    private SeekBar seekBar,seekbar_imageZoom;
    private boolean isShare = false;
    private CanvasView canvasView;
    private RecyclerView recyclerView;
    private ColorAdapter colorAdapter;
    private List<ColorList> colorLists;
    private ProgressDialog progressDialog;
    private ConstraintLayout constraintLayout,main;
    private int xDelta;
    private int yDelta;
    ImageView imageView;
   TextView textView;

    private int mActivePointerId = INVALID_POINTER_ID;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);



        method = new Method(Drawing.this);
        method.forceRTLIfSupported();

        GlobalBus.getBus().register(this);

        db = new DatabaseHandler(Drawing.this);
        colorLists = new ArrayList<>();

        progressDialog = new ProgressDialog(Drawing.this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_drawing);
        toolbar.setTitle(getResources().getString(R.string.drawing));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dialog = new Dialog(Drawing.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (method.isRtl()) {
            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        dialog.setContentView(R.layout.dialog);
        dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        seekBar = dialog.findViewById(R.id.seekBarVolume_dialog);
        seekBar.setProgress(20);

        canvasView = findViewById(R.id.canvas_drawing);
        canvasView.paintStork(20);
        canvasView.paintColor(Constant.colorChose);
        if (db.isColorCode(Constant.colorChose)) {
            db.addColor(Constant.colorChose);
        }
        colorLists = db.getColorDetail();

        int position = getIntent().getIntExtra("position", 0);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), Constant.image[position]);

        constraintLayout = findViewById(R.id.constraintLayout_imageView_drawing);
       // main=findViewById(R.id.Main);
         imageView = findViewById(R.id.imageView_drawing);
        ImageView imageViewEraser = findViewById(R.id.imageView_eraser_drawing);
        ImageView imageViewPaintPalette = findViewById(R.id.imageView_paintPalette_drawing);
        ImageView imageViewPaintBrush = findViewById(R.id.imageView_paintBrush_drawing);
        seekbar_imageZoom = findViewById(R.id.seeksBar);
        imageView.setImageBitmap(bitmap);



        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
               float xDown,yDown,moveDx,moveDy;
                switch (event.getActionMasked())
               {
                   case MotionEvent.ACTION_DOWN:
                       xDown=event.getX();
                       yDown=event.getY();

                       break;

                   case MotionEvent.ACTION_MOVE:
                       moveDx=event.getX();
                       moveDy=event.getY();
                       break;
               }

                return false;
            }
        });
       // constraintLayout.setOnTouchListener(onTouchListener());
        recyclerView = findViewById(R.id.recyclerView_drawing);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Drawing.this, RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        colorAdapter = new ColorAdapter(Drawing.this, colorLists, canvasView);
        recyclerView.setAdapter(colorAdapter);

        imageViewEraser.setOnClickListener(v -> canvasView.paintColor(Color.parseColor("#FFFFFF")));

        imageViewPaintPalette.setOnClickListener(v -> startActivity(new Intent(Drawing.this, ColorChose.class)));

        imageViewPaintBrush.setOnClickListener(v -> {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    canvasView.paintStork(progress / 2);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            dialog.show();
        });




        seekbar_imageZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               float scale =((progress/100.F)+1);

               constraintLayout.setScaleX(scale);
                constraintLayout.setScaleY(scale);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Subscribe
    public void getData(Events.ColorNotify colorNotify) {
        colorLists.clear();
        colorLists = db.getColorDetail();
        colorAdapter = new ColorAdapter(Drawing.this, colorLists, canvasView);
        recyclerView.setAdapter(colorAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drawing_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ic_share_main:
                isShare = true;
                new SaveShare().execute();
                Toast.makeText(Drawing.this, getResources().getString(R.string.share), Toast.LENGTH_SHORT).show();
                break;

            case R.id.ic_restart:
                canvasView.clear(getResources().getColor(R.color.canvasBg_drawing));
                break;

            case R.id.screen_short:
                new SaveShare().execute();
                break;

            case android.R.id.home:
                onBackPressed();
                break;

            default:
                break;
        }

        return true;
    }


    @SuppressLint("StaticFieldLeak")
    public class SaveShare extends AsyncTask<String, String, String> {

        String filePath;
        Bitmap bitmap;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.show();

            // create bitmap screen capture
            constraintLayout.setDrawingCacheEnabled(true);
            bitmap = Bitmap.createBitmap(constraintLayout.getDrawingCache());
            constraintLayout.setDrawingCacheEnabled(false);

        }

        @Override
        protected String doInBackground(String... strings) {

            String path;
            if (isShare) {
                path = getExternalCacheDir().getAbsolutePath();
            } else {
                path = getExternalFilesDir(getResources().getString(R.string.saveDataPath)).toString();
            }
            File imageFile = new File(path);

            if (!imageFile.exists()) {
                imageFile.mkdirs();
            }

            //Using Date class
            Date date = new Date();
            //Pattern for showing milliseconds in the time "SSS"
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
            String stringDate = sdf.format(date);

            //Using Calendar class
            Calendar cal = Calendar.getInstance();
            String s = sdf.format(cal.getTime());

            Random generator = new Random();
            filePath = imageFile + "/" + "Image-" + s + ".jpg";

            try {

                FileOutputStream outputStream = new FileOutputStream(filePath);
                int quality = 100;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                outputStream.flush();
                outputStream.close();

            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if (isShare) {
                isShare = false;
                method.share(filePath);
            } else {
                method.alertBox(getResources().getString(R.string.save));
            }

            super.onPostExecute(s);
        }

    }

    @Override
    protected void onResume() {
        if (canvasView != null) {
            canvasView.paintColor(Constant.colorChose);
        }
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        GlobalBus.getBus().unregister(this);
        super.onDestroy();
    }

    private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                final int x = (int) event.getRawX();
                final int y = (int) event.getRawY();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:
                        ConstraintLayout.LayoutParams lParams = (ConstraintLayout.LayoutParams)
                                view.getLayoutParams();

                        xDelta = x - lParams.leftMargin;
                        yDelta = y - lParams.topMargin;
                        break;

                    case MotionEvent.ACTION_UP:
                        Toast.makeText(Drawing.this,
                                        "I'm here!", Toast.LENGTH_SHORT)
                                .show();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view
                                .getLayoutParams();
                        layoutParams.leftMargin = x - xDelta;
                        layoutParams.topMargin = y - yDelta;
                        layoutParams.rightMargin = 0;
                        layoutParams.bottomMargin = 0;
                        view.setLayoutParams(layoutParams);
                        break;
                }

                main.invalidate();
                return true;
            }
        };
    }


}


