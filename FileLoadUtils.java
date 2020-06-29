public class FileLoadUtils {
    private static final String TAG = "FileLoadUtils";
 
    public FileLoadUtils() {
 
    }
    
    /**
     * 创建一个文件
     * @param FileName 文件名
     * @return
     */
    public static File createFile(String FileName) {
      String path = Environment.getExternalStorageDirectory().toString() + "/1nmpaapp";
        File file = new File(path);
        /**
         *如果文件夹不存在就创建
         */
        if (!file.exists()) {
            file.mkdirs();
        }
        return new File(path, FileName);
    }
    
    /**
     * 创建一个文件
     * @param FileName 文件名
     * @return
     */
    public static String BASE_PATH = "/storage/emulated/0/Android/data/com.***.****/******/";
    public static File createFileEm(String type,String FileName,String toUser) {
        String path = null;
        if (toUser != null && !toUser.equals("") ) {

            if (type.equals("3")) {
                path = BASE_PATH + toUser + "/voice/" ;
            } else if (type.equals("4")) {
                path =  BASE_PATH + toUser  + "/video/";
            }else if (type.equals("6")) {
                path = BASE_PATH + toUser  + "/file/";
            } else {
                path = BASE_PATH + toUser  + "/image/";
            }
        } else {
            if (type.equals("3")) {
                path = PathUtil.getInstance().getVoicePath() + "/" ;
            } else if (type.equals("4")) {
              path = PathUtil.getInstance().getVideoPath() + "/";
            }else if (type.equals("6")) {
                path = PathUtil.getInstance().getFilePath() + "/";
            } else {
                path = PathUtil.getInstance().getImagePath() + "/";
            }
        }

        File file = new File(path);
        /**
         *如果文件夹不存在就创建
         */
        if (!file.exists()) {
            file.mkdirs();
        }
        return new File(path, FileName);
    }
    
    /**
     * 获取拍照相片存储文件
     * @param context
     * @return
     */
    public static File createFile(Context context){
        File file;
        String savePath = Environment.getExternalStorageDirectory().toString() + "/nmpaapp/";
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            String timeStamp = String.valueOf(new Date().getTime());
            file = new File(savePath + timeStamp+".jpg");
        }else{
            File cacheDir = context.getCacheDir();
            String timeStamp = String.valueOf(new Date().getTime());
            file = new File(cacheDir, timeStamp+".jpg");
        }
        return file;
    }
 
    public static String handleImageOnKitKat(Context context, Intent data) {
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                String type = docId.split(":")[0];
                Uri contentUri = null;
                if (type.equalsIgnoreCase("image")) {
                    contentUri =  MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if (type.equalsIgnoreCase("audio")) {
                    contentUri =  MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                } else if (type.equalsIgnoreCase("video")) {
                    contentUri =  MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                return getImagePath(context, contentUri, selection);
            } else if ("com.android.providers.media.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                return getImagePath(context, contentUri, null);
            } else if ("content".equals(uri.getAuthority())) {
                return getImagePath(context, uri, null);
            } else if ("file".equals(uri.getAuthority())) {
                return uri.getPath();
            }
        }
        return "";
    }
 
    private static String getImagePath(Context context, Uri uri, String selection) {
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
    
    /**
     * 5.采样率压缩（设置图片的采样率，降低图片像素）
     *
     * @param filePath
     * @param file
     */
    public static void samplingRateCompress(String filePath, File file) {
        // 数值越高，图片像素越低
        int inSampleSize = 8;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
//          options.inJustDecodeBounds = true;//为true的时候不会真正加载图片，而是得到图片的宽高信息。
        //采样率
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 把压缩后的数据存放到baos中
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        try {
            if (file.exists()) {
                file.delete();
            } else {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public static Bitmap getimage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空
 
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        Log.e(TAG,"inSampleSize="+be);
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }
 
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 90;
        int length = baos.toByteArray().length / 1024;
        Log.e(TAG,"length="+length);
        if (length>5000){
            //重置baos即清空baos
            baos.reset();
            //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, 10, baos);
        }else if (length>4000){
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        }else if (length>3000){
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        }else if (length>2000){
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        }
 
        Log.e(TAG,"baos.toByteArray().length="+baos.toByteArray().length);
        //循环判断如果压缩后图片是否大于1M,大于继续压缩
        while (baos.toByteArray().length / 1024>1024) {
            //重置baos即清空baos
            baos.reset();
            //这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            //每次都减少10
            options -= 10;
        }
 
        //把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        //把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }
 
    /**
     * 保存bitmap到本地
     * @param context the context
     * @param mBitmap the m bitmap
     * @return string
     */
    public static String saveBitmap(Context context, Bitmap mBitmap) {
        String savePath = Environment.getExternalStorageDirectory().toString() + "/nmpaapp/";
        File filePic;
        try {
 
            filePic = new File(savePath + System.currentTimeMillis() + ".jpg");
            Log.d("LUO", "图片地址====" + filePic);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            //不压缩，保存本地
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return filePic.getAbsolutePath();
    }
 
    /*
    * 弹出框，选择是从本机选择图片还是拍照
    * */
    public static void showPhotoDialog (Activity mContext,int requestCode) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_photo,null,false);
        final AlertDialog dialog = new AlertDialog.Builder(mContext).setView(view).create();
 
        TextView btn_cancel = view.findViewById(R.id.tv_confirm);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //... To-do
                dialog.dismiss();
            }
        });
        TextView tv_selPhoto = view.findViewById(R.id.tv_selPhoto);
        tv_selPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                mContext.startActivityForResult(intent, requestCode);
                dialog.dismiss();
            }
        });
        TextView tv_takePhoto = view.findViewById(R.id.tv_takePhoto);
        tv_takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto(mContext,requestCode+1);
                dialog.dismiss();
            }
        });
        dialog.show();
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.setBackgroundDrawable(null);
    }
 
 
    public static void takePhoto(Activity mContext, int requestCode) {
        // 启动相机程序
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mContext.startActivityForResult(intent, requestCode);
    }
 
 
    /*
    * 从拍照返回的数据中保存图片并返回图片路径
    * */
    public static String getCameraData(Intent data) {
        String path = null;
        Bitmap photo = null;
        if (data.getData() != null || data.getExtras() != null) { // 防止没有返回结果
            Uri uri = data.getData();
            if (uri != null) {
                photo = BitmapFactory.decodeFile(uri.getPath()); // 拿到图片
            }
 
            if (photo == null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    photo = (Bitmap) bundle.get("data");
                    String saveDir = Environment.getExternalStorageDirectory().toString() + "/nmpaapp/";
                    String filename = System.currentTimeMillis() + ".jpg";
                    File file = new File(saveDir, filename);
                    FileOutputStream fileOutputStream = null;
                    // 打开文件输出流
                    try {
                        fileOutputStream = new FileOutputStream(file);
                        // 生成图片文件
                        photo.compress(Bitmap.CompressFormat.JPEG,
                                100, fileOutputStream);
                        path = file.getPath();
                        Log.e("FileLoadUtils", "str=" + path);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return path;
    }
    
    public static void postFile(final String url, final Map<String, String> map,String imgStr, File file,String param1,File file1, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        // form 表单形式上传
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (file != null) {
            Log.e("FileLoadUtils","file="+file.getPath()+","+file.getName());
            // MediaType.parse() 里面是上传的文件类型。
            RequestBody body = RequestBody.create(MediaType.parse("image/*"), file);
            String filename = file.getName();
            // 参数分别为， 请求key ，文件名称 ， RequestBody
            requestBody.addFormDataPart(imgStr, filename, body);
        }

        if (file1 != null) {
            Log.e("FileLoadUtils","file="+file1.getPath()+","+file1.getName());
            // MediaType.parse() 里面是上传的文件类型。
            RequestBody body = RequestBody.create(MediaType.parse("image/*"), file1);
            String filename = file1.getName();
            // 参数分别为， 请求key ，文件名称 ， RequestBody
            requestBody.addFormDataPart(param1, filename, body);
        }

        if (map != null) {
            // map 里面是请求中所需要的 key 和 value
            Set<Map.Entry<String, String>> entries = map.entrySet();
            for (Map.Entry entry : entries) {
                String key = String.valueOf(entry.getKey());
                String value = String.valueOf(entry.getValue());
                Log.d("HttpUtils", "key=="+key+",value=="+value);
                requestBody.addFormDataPart(key,value);
            }
        }
        String token = SavePreferences.getString(Const.TOKEN_KEY);
        Request request = new Request.Builder().url(url).header("token", token).post(requestBody.build()).build();
        // readTimeout("请求超时时间" , 时间单位);
        client.newBuilder().readTimeout(5000, TimeUnit.MILLISECONDS).build()
                .newCall(request).enqueue(callback);

    }
