import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.androidybp.basics.ApplicationContext;
import com.androidybp.basics.R;
import com.androidybp.basics.utils.date.ProjectDateUtils;
import com.androidybp.basics.utils.permission.CuttingModel;
import com.androidybp.basics.utils.permission.PermissionUtil;
import com.androidybp.basics.utils.resources.Fileprovider;
//import com.gamerole.orcameralib.CameraActivity;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import androidx.core.os.EnvironmentCompat;
import androidx.fragment.app.Fragment;

/**
 * 选择图片的工具类
 * <p>
 * 注意 相册选择不多说 拍照时候图片 保存路径 为 沙盒私有路径
 * <p>
 * 当前工具类只返回 数据uri地址 后续调用类自行进行处理
 */
public class SelectPhotoUtils {
  private final static int GALLERY_REQUEST_CODE = 12;//相册
    // 拍照的requestCode
    private static final int CAMERA_REQUEST_CODE = 0x00000010;

    private Fragment fragment;
    private Activity activity;
    private PopupWindow popImage;
    public PermissionUtil permissionUtil;

    private int type;//类型
    private View view;//打开Pop使用的坐标view对象

    private Uri mCameraUri;
    private SelectPhotoCallback callback;
    boolean isShow = true;
    LinearLayout linearLayout;
    /*
     * 是否是Android 10以上手机
     */
    private boolean isAndroidQ = Build.VERSION.SDK_INT > Build.VERSION_CODES.P;


    public SelectPhotoUtils(Activity act, Fragment fragment) {
        this.activity = act;
        this.fragment = fragment;
        this.permissionUtil = new PermissionUtil();
        initImagePop();
    }

    public void canSelectPic(boolean b) {
        this.isShow = b;
    }

    /**
     * 设置 选择图片回调接口
     *
     * @param callback
     */
    public SelectPhotoUtils setCallback(SelectPhotoCallback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * 打开 图片选项器
     *
     * @param type
     */
    public void selectPhoto(int type, View view) {
        this.type = type;
        this.view = view;
        //先判断权限是否充足
        String[] umCutting = new CuttingModel().getCameraAndMemoryCutting();
        if (permissionUtil.hasPermission(activity, umCutting)) {
            //打开对应popWind
            showSelectPop(view);
        } else {
            List<String> strings = Arrays.asList(umCutting);
            if (fragment != null) {
                permissionUtil.requestPermissions(fragment, strings, 700);
            } else {
                permissionUtil.requestPermissions(activity, strings, 700);
            }

        }

    }

    private void showSelectPop(View view) {
        // 这里设置显示PopuWindow之后在外面点击是否有效。如果为false的话，那么点击PopuWindow外面并不会关闭PopuWindow。
        if (isShow) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.GONE);
        }
        backgroundAlpha(0.5f, activity);
        popImage.setBackgroundDrawable(new BitmapDrawable());
        popImage.setOutsideTouchable(true);// 不能在没有焦点的时候使用
        popImage.showAtLocation(view, Gravity.BOTTOM, 0, 0);
    }

    /**
     * 权限申请 回调方法
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 700) {
            String str = "需要获取“相机/摄像头/储存”权限,否则应用无法正常使用,请前往 应用信息-->权限 页面设置该应用权限为允许";
            //读写权限
            boolean perm = permissionUtil.verifyPermissions(activity, permissions, grantResults, null, str, null, null);
            if (perm) {
                //打开对应popWind
                showSelectPop(view);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //拍摄图片回调
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == activity.RESULT_OK) {
                //回调 方法
                if (callback != null) {
                    callback.selectPhotoCallback(1, type, mCameraUri);
                }
            }
        } else if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == activity.RESULT_OK) {
                Uri imageUri = data.getData();
                //回调 方法
                if (callback != null) {
                    callback.selectPhotoCallback(1, type, imageUri);
                }
            }
        } else if (requestCode == 1001) {
            if (resultCode == activity.RESULT_OK) {
                //回调 方法
                if (callback != null) {
                    File file = new File(data.getStringExtra("image"));
                    callback.takePictureCallBack(1, type, Uri.parse(file.toString()));
                }
            }
        }

    }
  
  /**
     * 初始化 popwindown
     */
    private void initImagePop() {


        View view = activity.getLayoutInflater().inflate(R.layout.popu_portrait, null);

        linearLayout = view.findViewById(R.id.xiangce);

        view.findViewById(R.id.paizhao).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popImage.dismiss();
                //拍照
                if (isShow) {
                    openCamera();
                } else { //身份证拍照

//                   Intent intent  = new Intent(activity, CameraActivity.class);
////                   intent.putExtra("contentType","IDCardFront");
////                    intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,new File(CameraActivity.KEY_OUTPUT_FILE_PATH+"/"+"2.jpg"));
////                    if(fragment != null){
////                        fragment.startActivityForResult(intent, 1001);
////                    } else {
////                        activity.startActivityForResult(intent, 1001);
////                    }
                }
            }
        });


        view.findViewById(R.id.xiangce).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popImage.dismiss();
                Intent intentToPickPic = new Intent(Intent.ACTION_PICK, null);
                // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型" 所有类型则写 "image/*"
                intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                if (fragment != null) {
                    fragment.startActivityForResult(intentToPickPic, GALLERY_REQUEST_CODE);
                } else {
                    activity.startActivityForResult(intentToPickPic, GALLERY_REQUEST_CODE);
                }


            }
        });
        view.findViewById(R.id.cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popImage.dismiss();
            }
        });
        popImage = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popImage.setAnimationStyle(R.style.mypopwindow_anim_style);
        popImage.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f, activity);
            }
        });
    }

    /**
     * 设置添加屏幕的背景透明度v
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha, Activity activity) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }

    //     ----------------      打开相机

    /**
     * 生成图片文件名
     *
     * @return
     */
    private String createPhotoName() {
        StringBuilder sb = new StringBuilder();
        sb.append(ProjectDateUtils.getTimeDay("yyyyMMdd", System.currentTimeMillis()));
        String guid = UUID.randomUUID().toString();
        sb.append(guid.replace("-", ""));
        sb.append(".jpg");
        return sb.toString();
    }

    /**
     * 调起相机拍照
     */
    private void openCamera() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 判断是否有相机
        if (captureIntent.resolveActivity(activity.getPackageManager()) != null) {
            File photoFile = null;
            Uri photoUri = null;
            if (isAndroidQ) {
                // 适配android 10
                photoUri = createImageUri();
            } else {
                try {
                    photoFile = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (photoFile != null) {
                    Fileprovider fileprovider = new Fileprovider();
                    photoUri = fileprovider.getUri(activity, photoFile, captureIntent);
                }
            }
            mCameraUri = photoUri;
            if (photoUri != null) {
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (fragment != null) {
                    fragment.startActivityForResult(captureIntent, CAMERA_REQUEST_CODE);
                } else {
                    activity.startActivityForResult(captureIntent, CAMERA_REQUEST_CODE);
                }

            }
        }
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     *
     * @return 图片的uri
     */
    private Uri createImageUri() {
        String status = Environment.getExternalStorageState();
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        } else {
            return activity.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
        }
    }

    /**
     * 创建保存图片的文件
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        File storageDir = ApplicationContext.getInstance().application.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, createPhotoName());
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }


    //     ----------------      打开相机  结束


    //     ----------------      打开相册  开始


    /**
     * 清空页面数据
     */
    public void clear() {

        fragment = null;
        activity = null;
        if (popImage != null && popImage.isShowing()) {
            popImage.dismiss();
        }
        popImage = null;
        permissionUtil = null;
        view = null;//打开Pop使用的坐标view对象
        mCameraUri = null;
        callback = null;
    }

    public interface SelectPhotoCallback {
        /**
         * @param selectType 1:相机拍摄  2：相册选择
         * @param openType
         * @param uri
         */
        void selectPhotoCallback(int selectType, int openType, Uri uri);

        void takePictureCallBack(int selectType, int openType, Uri path);
    }
}
