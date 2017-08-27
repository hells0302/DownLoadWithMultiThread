package com.study.mutlithreaddownloadtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button btn1;
    private ImageView imageView;
    private int count=0;
    public static final String IMAGE_URL="https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1503860348291&di=e8727944c0f3cda43feef06dd04bdac0&imgtype=0&src=http%3A%2F%2Fimg.hb.aicdn.com%2F5a59537f7eed6e555710f18d301ab6a1d7d42935d524-KDHrXl_fw580";
    private Handler handler=new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case 1:
                    int result=msg.what;
                    count+=result;
                    if(count==3)
                    {
                        count=0;
                        Toast.makeText(MainActivity.this,"Down Success",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    Bitmap bitmap= BitmapFactory.decodeFile(msg.obj.toString());
                    imageView.setImageBitmap(bitmap);
                    break;

            }


        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btn1= (Button) findViewById(R.id.btn1);
        imageView= (ImageView) findViewById(R.id.imageView);
    }
    public void onClick(View view)
    {
        new Thread()
        {
            @Override
            public void run() {
                DownLoad downLoad=new DownLoad(handler);
                downLoad.downLoadImage(IMAGE_URL);
                String fileName="";
                if(!(fileName=downLoad.getFileRoad()).equals(""))
                {
                    Message msg=new Message();
                    msg.what=2;
                    msg.obj=fileName;
                    handler.sendMessage(msg);
                }
            }
        }.start();

    }

}
