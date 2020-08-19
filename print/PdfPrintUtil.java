package com.nmpa.nmpaapp.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.print.PrintHelper;
import com.nmpa.nmpaapp.R;
import com.nmpa.nmpaapp.modules.punish.adapter.MyPrintPdfAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfPrintUtils {
    private static final String TAG = "PdfPrintUtils";
    /**
     * 把View转为PDF，必须要在View 渲染完毕之后
     * 1.使用LayoutInflater反射出来的View不行；
     * 2. 将要转换成pdf的xml view文件include到一个界面中，将其设置成android:visibility=”invisible”就可以实现，不显示，但是能转换成PDF；
     * 3. 设置成gone不行；
     * @param view
     * @param pdfName
     */
    public static void createPdfFromView(@NonNull View view, @NonNull final String pdfName ){
        //1, 建立PdfDocument
        final PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo
                .Builder(view.getMeasuredWidth(), view.getMeasuredHeight(), 1)
                //设置绘制的内容区域，此处我预留了10的内边距
                .setContentRect(new Rect(10,10,view.getMeasuredWidth()-10,view.getMeasuredHeight()-10))
//                .setContentRect(new Rect(0,0,view.getMeasuredWidth(),view.getMeasuredHeight()))
                .create();
        PdfDocument.Page page = document.startPage(pageInfo);
        view.draw(page.getCanvas());
        //必须在close 之前调用，通常是在最后一页调用
        document.finishPage(page);
        //保存至SD卡
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = Environment.getExternalStorageDirectory() + File.separator + pdfName;
                    File e = new File(path);
                    if (e.exists()) {
                        e.delete();
                    }
                    document.writeTo(new FileOutputStream(e));
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /*
    *  创建多页PDF文档
    * */
    public static void createMorePdfFromView(Context mContext,@NonNull View view, @NonNull final String pdfName ){
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), //2, 测量大小
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight()+240); //3, 测量位置
        int viewHeight = view.getMeasuredHeight();

        int screenWidth = ((Activity)mContext).getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = ((Activity)mContext).getWindowManager().getDefaultDisplay().getHeight();

        int count = 1;
        if (viewHeight/screenHeight > 0) {
            if (viewHeight % screenHeight == 0) {
                count = viewHeight/screenHeight;
            } else {
                count = viewHeight/screenHeight + 1;
            }
        }
        Log.e(TAG,"count="+count+",viewHeight="+viewHeight+",screenHeight="+screenHeight);
        //1, 建立PdfDocument
        final PdfDocument document = new PdfDocument();
        for (int i=0; i<count; i++) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo
                    .Builder(screenWidth, screenHeight-240, i)
                    //设置绘制的内容区域，此处我预留了10的内边距
//                    .setContentRect(new Rect(10,10,screenWidth-10,screenHeight-100))
                .setContentRect(new Rect(0,0,screenWidth,screenHeight-240))
                    .create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            canvas.translate(0,-(screenHeight-240)*i);
            view.draw(canvas);

            //必须在close 之前调用，通常是在最后一页调用
            document.finishPage(page);
        }

        //保存至SD卡
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String path = Environment.getExternalStorageDirectory() + File.separator + pdfName;
                    File e = new File(path);
                    if (e.exists()) {
                        e.delete();
                    }
                    document.writeTo(new FileOutputStream(e));
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    
    public static void createMorePdfFromView1(Context mContext,@NonNull View view, @NonNull final String pdfName, int count ){
        int screenWidth = ((Activity)mContext).getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = ((Activity)mContext).getWindowManager().getDefaultDisplay().getHeight();
        //1, 建立PdfDocument
        final PdfDocument document = new PdfDocument();
        for (int i=0; i<count; i++) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo
                    .Builder(screenWidth, screenHeight-240, i)
                    //设置绘制的内容区域，此处我预留了10的内边距
                    .setContentRect(new Rect(10,60,screenWidth-10,screenHeight-300))
                    .create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            canvas.translate(0,-(screenHeight-360)*i);
            view.draw(canvas);

            //必须在close 之前调用，通常是在最后一页调用
            document.finishPage(page);
        }

        //保存至SD卡
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fs = null;
                try {
                    String path = Environment.getExternalStorageDirectory() + File.separator + pdfName;
                    File e = new File(path);
                    if (e.exists()) {
                        e.delete();
                    }
                    fs = new FileOutputStream(e);
                    document.writeTo(fs);
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fs != null) {
                        try {
                            fs.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                doPrint(mContext,pdfName);
            }
        }).start();
    }
    
    /*
    *  打印PDF
    * */
    public static void doPrint(Context mContext,String fileName) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) mContext.getSystemService(Context.PRINT_SERVICE);

        // Set job name, which will be displayed in the print queue
        String jobName = mContext.getResources().getString(R.string.app_name) + " Document";
        // Start a print job, passing in a PrintDocumentAdapter implementation
        // to handle the generation of a print document
        printManager.print(jobName, new MyPrintPdfAdapter(Environment.getExternalStorageDirectory()+"/"+fileName), null); //
//        printManager.print(jobName, new MyPrintAdapter(mContext,Environment.getExternalStorageDirectory()+"/"+fileName), null); //

    }
    
    //绘制 PDF 页面内容
    private void drawPage(PdfDocument.Page page) {
        Canvas canvas = page.getCanvas();

        // units are in points (1/72 of an inch)
        int titleBaseLine = 72;
        int leftMargin = 54;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(36);
        canvas.drawText("Test Title", leftMargin, titleBaseLine, paint);

        paint.setTextSize(11);
        canvas.drawText("Test paragraph", leftMargin, titleBaseLine + 25, paint);

        paint.setColor(Color.BLUE);
        canvas.drawRect(100, 100, 172, 172, paint);
    }
    
    public static Bitmap createBitmap(View view) {
        //当使用View.MeasureSpec.EXACTLY时报java.lang.IllegalArgumentException: width and height must be > 0
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), //2, 测量大小
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight()); //3, 测量位置
        int width = view.getWidth();
        int height = view.getHeight();
        Log.e(TAG,"height="+height);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
    
    public static void photoPrint(Context mContext,String fileName) {
        Log.e("cmo","是否支持："+ PrintHelper.systemSupportsPrint());
        //初始化创建PrintHelper对象
        PrintHelper photoPrinter = new PrintHelper(mContext);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        
        //        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.ic_launcher_round);
        Bitmap bitmap = BitmapFactory.decodeFile(fileName);
        //第一个参数为jobName 任意字符串，建议可以使用随机字符串，下同
        photoPrinter.printBitmap("cmo:photoPrint", bitmap);
    }
    
    public static void createWebPrintJob(Context mContext,WebView webView) {

        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) mContext.getSystemService(Context.PRINT_SERVICE);

        String jobName = mContext.getResources().getString(R.string.app_name) + " Document";

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
        // Create a print job with name and adapter instance
        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

        // Save the job object for later status checking
        printJobs.add(printJob);
    }
    static List<PrintJob> printJobs = new ArrayList<>();
}
