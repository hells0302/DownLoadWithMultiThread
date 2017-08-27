package com.study.mutlithreaddownloadtest;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by dnw on 2017/8/27.
 */

public class DownLoad {
    private static final String TAG = "DownLoad";
    private Handler handler;
    private File file;
    //线程池，固定数量4个
    private Executor executor= Executors.newFixedThreadPool(4);

    public DownLoad(Handler handler)
    {
        this.handler=handler;
    }
    //下载数据路径获取函数，用于ImageView的显示
    public String getFileRoad()
    {
        if(file!=null)
            return file.getAbsolutePath();
        return null;
    }
    static class DownLoadRunnable implements Runnable
    {
        private long start;
        private long end;
        private String url;
        private String fileName;
        private Handler handler;
        InputStream in;
        RandomAccessFile accessFile;

        public DownLoadRunnable(String url,String fileName,long start,long end,Handler handler)
        {
            this.url=url;
            this.fileName=fileName;
            this.start=start;
            this.end=end;
            this.handler=handler;
        }
        @Override
        public void run() {
            try {
                Log.d(TAG, "run: in downLoadRun");
                //根据分好的数据段下载数据
                URL url1=new URL(url);
                HttpURLConnection connection= (HttpURLConnection) url1.openConnection();
                connection.setReadTimeout(5000);
                //多线程文件下载的关键
                connection.setRequestProperty("Range","bytes="+start+"-"+end);
                connection.setRequestMethod("GET");
                //注意必须使用这个文件打开
                accessFile=new RandomAccessFile(new File(fileName),"rwd");
                accessFile.seek(start);
                in=connection.getInputStream();
                byte[] b=new byte[1024*4];
                int len;
                while((len=in.read(b))!=-1)
                {
                    Log.d(TAG, "b: "+b);
                    accessFile.write(b,0,len);
                }
                //给主线程发送消息
                Message msg=new Message();
                msg.what=1;
                handler.sendMessage(msg);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(accessFile!=null)
                {
                    try {
                        accessFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(in!=null)
                {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    //图片下载函数
    public void downLoadImage(String url)
    {
        try {
            //获取需要下载的文件长度
            URL url1=new URL(url);
            HttpURLConnection connection= (HttpURLConnection) url1.openConnection();
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            //获取数据长度
            int count=connection.getContentLength();
            Log.d(TAG, "downLoadImage: "+count);
            int block=count/4;
            //创建文件
            File parent= Environment.getExternalStorageDirectory();
            file=new File(parent,"multiThread.jpg");
            //分4个线程下载
            for(int i=0;i<4;i++)
            {
                long start=i*block;
                long end=(i+1)*block-1;
                //当最后一段时，包含剩下的所有数据
                if(i==3)
                {
                    end=count;
                }
                Log.d(TAG, "downLoadImage: "+"start= "+start+" end= "+end);
                //开启线程池，下载数据
                DownLoadRunnable runnable=new DownLoadRunnable(url,file.getAbsolutePath(),start,end,handler);
                executor.execute(runnable);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
