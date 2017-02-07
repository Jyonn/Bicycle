package cn.a6_79.bicycle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

public class Dialog {
    private static AlertDialog showDialog(Context context, String title, String message, View contentView,
                                          String positiveBtnText, String negativeBtnText,
                                          DialogInterface.OnClickListener positiveCallback,
                                          DialogInterface.OnClickListener negativeCallback,
                                          boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(title == null ? "提示" : title);
        if (message != null) {
            builder.setMessage(message);
        }
        if (contentView != null) {
            builder.setView(contentView);
        }
        if (positiveBtnText != null) {
            builder.setPositiveButton(positiveBtnText, positiveCallback);
        }
        if (negativeBtnText != null) {
            builder.setNegativeButton(negativeBtnText, negativeCallback);
        }
        builder.setCancelable(cancelable);
        return builder.show();
    }
    //普通对话框
    public static AlertDialog showSimpleDialog(Context context, String title, String message,
                                               String positiveBtnText, String negativeBtnText,
                                               DialogInterface.OnClickListener positiveCallback,
                                               DialogInterface.OnClickListener negativeCallback,
                                               boolean cancelable) {
        return showDialog(context, title, message, null, positiveBtnText, negativeBtnText, positiveCallback, negativeCallback, cancelable);
    }
}
