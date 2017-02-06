package cn.a6_79.bicycle;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AccountActivity extends AppCompatActivity {
    static String transType = "AccountActivityType";
    static int transTypeAdd = 0;
    static int transTypeModify = 1;
    static String transIndex = "AccountIndex";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        final int iTransType = (int) getIntent().getExtras().get(transType);

        final EditText mUsernameView = (EditText) findViewById(R.id.username);
        final EditText mPasswordView = (EditText) findViewById(R.id.password);
        final int iTransIndex = (iTransType == transTypeModify) ?
                (int) getIntent().getExtras().get(transIndex) : -1;
        if (iTransType == transTypeModify) {
            setTitle("修改账号");
            Bicycle bicycle = CommonData.bicycles.get(iTransIndex);
            mUsernameView.setText(bicycle.getAccount().getUsername());
            mPasswordView.setText(bicycle.getAccount().getPassword());
        } else
            setTitle("添加账号");

        Button confirmButton = (Button) findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsernameView.getText().toString();
                String password = mPasswordView.getText().toString();
                username = username.replace(" ", "");
                password = password.replace(" ", "");
                if (username.equals("") || password.equals("")) {
                    Snackbar.make(view, "账号或密码不能为空", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }
                if (iTransType == transTypeAdd) {
                    CommonData.bicycles.add(new Bicycle(new Account(username, password)));
                    setResult(1);
                }
                else {
                    Bicycle bicycle = CommonData.bicycles.get(iTransIndex);
                    bicycle.getAccount().setAccount(username, password);
                    ThreadTask threadTask = bicycle.getThreadTask();
                    if (threadTask != null) {
                        bicycle.getThreadTask().cancel(true);
                        bicycle.setThreadTaskNull();
                    }
                    bicycle.setRefresh(true);
                    setResult(2);
                }
                finish();
            }
        });
    }

}

