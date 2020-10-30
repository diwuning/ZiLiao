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
}
