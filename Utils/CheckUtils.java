import android.text.TextUtils;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckUtils {
  // 是否数字
    public static boolean isNumber(String paramString) {
        if (StringUtils.isEmpty(paramString))
            return false;
        return paramString.matches("[0-9]+");
    }

    //
    public static boolean isEmail(String paramString) {
        if (StringUtils.isEmpty(paramString))
            return false;
        return Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$").matcher(paramString).find();
    }

    public static boolean isEquals(EditText paramEditText1, EditText paramEditText2) {
        if ((paramEditText1 == null) || (paramEditText2 == null))
            ;
        while (!paramEditText1.getText().toString().equals(paramEditText2.getText().toString()))
            return false;
        return true;
    }

    public static boolean isPwdEquals(String paramString1, String paramString2) {
        if (!isValidPassword(paramString1).equals("YES"))
            return false;
        if (!isValidPassword(paramString2).equals("YES"))
            return false;
        if (!paramString1.equals(paramString2))
            return false;
        return true;
    }

    public static boolean isIDCard(String paramString) {
        if (StringUtils.isEmpty(paramString))
            return false;
        if (!Pattern.compile("(^\\d{15}$)|(^\\d{17}([0-9]|X)$)").matcher(paramString).find())
            return false;
        return true;
    }

    /**
     * 验证手机号码（支持国际格式，+86135xxxx...（中国内地），+00852137xxxx...（中国香港））
     *
     * @param mobile 移动、联通、电信运营商的号码段
     *               <p>移动的号段：134(0-8)、135、136、137、138、139、147（预计用于TD上网卡）
     *               、150、151、152、157（TD专用）、158、159、187（未启用）、188（TD专用）</p>
     *               <p>联通的号段：130、131、132、155、156（世界风专用）、185（未启用）、186（3g）</p>
     *               <p>电信的号段：133、153、180（未启用）、189</p>
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean isMobile(String mobile) {
        if (StringUtils.isEmpty(mobile))
            return false;
        String regex = "(\\+\\d+)?1[3456789]\\d{9}$";
        return Pattern.matches(regex, mobile);
    }

    public static boolean isUserName(String paramString) {
        if (StringUtils.isEmpty(paramString))
            return false;
        if (paramString.trim().length() < 5) return false;
        if (!Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$").matcher(paramString).find())
            return false;
        return true;
    }
  
  /**
     * 验证Email
     *
     * @param email email地址，格式：zhangsan@zuidaima.com，zhangsan@xxx.com.cn，xxx代表邮件服务商
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkEmail(String email) {
        String regex = "\\w+@\\w+\\.[a-z]+(\\.[a-z]+)?";
        return Pattern.matches(regex, email);
    }

    /**
     * 验证身份证号码
     *
     * @param idCard 居民身份证号码15位或18位，最后一位可能是数字或字母
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkIdCard(String idCard) {
        //String regex = "[1-9]\\d{13,16}[a-zA-Z0-9]{1}";
        String regex = "(^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{2}[0-9Xx]$)";
        return Pattern.matches(regex, idCard);
    }

    /**
     * 验证固定电话号码
     *
     * @param phone 电话号码，格式：国家（地区）电话代码 + 区号（城市代码） + 电话号码，如：+8602085588447
     *              <p><b>国家（地区） 代码 ：</b>标识电话号码的国家（地区）的标准国家（地区）代码。它包含从 0 到 9 的一位或多位数字，
     *              数字之后是空格分隔的国家（地区）代码。</p>
     *              <p><b>区号（城市代码）：</b>这可能包含一个或多个从 0 到 9 的数字，地区或城市代码放在圆括号——
     *              对不使用地区或城市代码的国家（地区），则省略该组件。</p>
     *              <p><b>电话号码：</b>这包含从 0 到 9 的一个或多个数字 </p>
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkPhone(String phone) {
        String regex = "(\\+\\d+)?(\\d{3,4}\\-?)?\\d{7,8}$";
        return Pattern.matches(regex, phone);
    }

    /**
     * 验证整数（正整数和负整数）
     *
     * @param digit 一位或多位0-9之间的整数
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkDigit(String digit) {
        String regex = "\\-?[1-9]\\d+";
        return Pattern.matches(regex, digit);
    }

    /**
     * 验证整数和浮点数（正负整数和正负浮点数）
     *
     * @param decimals 一位或多位0-9之间的浮点数，如：1.23，233.30
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkDecimals(String decimals) {
        String regex = "\\-?[1-9]\\d+(\\.\\d+)?";
        return Pattern.matches(regex, decimals);
    }

    /**
     * 验证空白字符
     *
     * @param blankSpace 空白字符，包括：空格、\t、\n、\r、\f、\x0B
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkBlankSpace(String blankSpace) {
        String regex = "\\s+";
        return Pattern.matches(regex, blankSpace);
    }

    /**
     * 验证中文
     *
     * @param chinese 中文字符
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkChinese(String chinese) {
        String regex = "^[\u4E00-\u9FA5]+$";
        return Pattern.matches(regex, chinese);
    }
  
  /**
     * 验证日期（年月日）
     *
     * @param birthday 日期，格式：1992-09-03，或1992.09.03
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkBirthday(String birthday) {
        String regex = "[1-9]{4}([-./])\\d{1,2}\\1\\d{1,2}";
        return Pattern.matches(regex, birthday);
    }

    /**
     * 验证URL地址
     *
     * @param url 格式：http://blog.csdn.net:80/xyang81/article/details/7705960? 或 http://www.csdn.net:80
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkURL(String url) {
        String regex = "(https?://(w{3}\\.)?)?\\w+\\.\\w+(\\.[a-zA-Z]+)*(:\\d{1,5})?(/\\w*)*(\\??(.+=.*)?(&.+=.*)?)?";
        return Pattern.matches(regex, url);
    }

    /**
     * <pre>
     * 获取网址 URL 的一级域名
     * http://www.zuidaima.com/share/1550463379442688.htm ->> zuidaima.com
     * </pre>
     *
     */
    public static String getDomain(String url) {
        Pattern p = Pattern.compile("(?<=http://|\\.)[^.]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE);
        // 获取完整的域名  
        // Pattern p=Pattern.compile("[^//]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE);  
        Matcher matcher = p.matcher(url);
        matcher.find();
        return matcher.group();
    }
  
  /**
     * 匹配中国邮政编码
     *
     * @param postcode 邮政编码
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkPostcode(String postcode) {
        String regex = "[1-9]\\d{5}";
        return Pattern.matches(regex, postcode);
    }

    /**
     * 匹配IP地址(简单匹配，格式，如：192.168.1.1，127.0.0.1，没有匹配IP段的大小)
     *
     * @param ipAddress IPv4标准地址
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkIpAddress(String ipAddress) {
        String regex = "[1-9](\\d{1,2})?\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))";
        return Pattern.matches(regex, ipAddress);
    }

    public static String isValidPassword(String paramString) {
        String str = "YES";
        if ((paramString.length() < 6) || (paramString.length() > 16))
            str = "密码长度应在6-16位，当前" + paramString.length() + "位";
    /*	if (paramString.matches("[0-9]+"))
			str = "密码不能全部为数字";
		if (paramString.matches("[a-zA-Z]+"))
			str = "密码不能全部为字母";*/
        if (Pattern.compile("[\\u4e00-\\u9fa5]").matcher(paramString).find())
            str = "请不要使用中文作为密码";
      /*  if(Pattern.compile("[0-9a-zA-Z\\u4E00-\\u9FA5\\\\s]+").matcher(paramString).find())
            str = "密码不能含有空格";*/
//		if (!Pattern.compile("^[A-Za-z\\d]+(\\.[A-Za-z\\d]+)*@([\\dA-Za-z](-[\\dA-Za-z])?)+(\\.{1,2}[A-Za-z]+)+$").matcher(paramString).find())
//			str = "请不要使用中文作为密码"; android:digits="0123456789abcdefghigklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return str;
    }

    /**
     * 判断是否含有特殊字符
     *
     * @return true为包含，false为不包含
     */
    public static boolean isSpecialChar(String str) {
        String regEx = "[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }
  
  /*
     *只允许字母，数字，下划线，横线
     * */
    public static boolean isFormatStr(String str) {
        String qr = str.toLowerCase().replaceAll("_", "");
        int length = qr.length();
        if (length < 10 || length > 50) {
            return false;
        }
        Matcher matcher = Pattern.compile("^[0-9a-zA-Z _-]+$").matcher(str);
        return matcher.find();
    }

    /*
     *只允许字母，数字，下划线，横线
     * */
    public static boolean isPasswordStr(String str) {
        String qr = str.toLowerCase().replaceAll("_", "");
        int length = qr.length();
        if (length <= 16 && length >= 6) {
            return true;
        }
        Matcher matcher = Pattern.compile("^[0-9a-zA-Z _-]+$").matcher(str);
        return matcher.find();
    }

    /*
     *只允许6-16位字符
     * */
    public static boolean validatePhonePass(String pass) {
        String passRegex = "^[a-zA-Z0-9_]{6,16}$";
        return !TextUtils.isEmpty(pass) && pass.matches(passRegex);
    }

    static String pwdRes="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[ _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？])[A-Za-z\\d _`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]{8,}";


    /**
     * 密码不可以
     */
    public static boolean checkRePwdIsNo(String pwd) {
        if (!pwd.matches(pwdRes)) {
            return true;
        }
        return false;
    }
    /**
     * 密码不可以
     */
    public static boolean checkPwdIsNo(String pwd){
        if (pwd.matches("[0-9]+")){
            return true;
        }
        if (pwd.matches("[a-zA-Z]+")){
            return true;
        }
        if (pwd.length() < 6){
            return true;
        }
        if (pwd.length() > 10){
            return true;
        }
        return false;
    }
}
