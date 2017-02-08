package cn.a6_79.bicycle;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private Context context;
    SaveListener saveListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        getDataFromLocal();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.fabMenu);
        CommonData.menu = menu;

        FloatingActionButton fabRefresh = (FloatingActionButton) findViewById(R.id.fabRefresh);
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.collapse();
                if (CommonData.currentFragment == -1) {
                    Snackbar.make(view, "没有数据需要刷新", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    CommonData.bicycles.get(CommonData.currentFragment).setRefresh(true);
                    Bicycle.refresh(CommonData.currentFragment, CommonData.currentOnAsyncTaskListener);
                }
            }
        });

        FloatingActionButton fabModify = (FloatingActionButton) findViewById(R.id.fabModify);
        fabModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.collapse();
                if (CommonData.currentFragment == -1) {
                    Snackbar.make(view, "没有数据需要修改", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Intent intent = new Intent(context, AccountActivity.class);
                    intent.putExtra(AccountActivity.transType, AccountActivity.transTypeModify);
                    intent.putExtra(AccountActivity.transIndex, CommonData.currentFragment);
                    startActivityForResult(intent, 1);
                }
            }
        });

        FloatingActionButton fabDelete = (FloatingActionButton) findViewById(R.id.fabDelete);
        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.collapse();
                if (CommonData.currentFragment == -1 || CommonData.currentFragment >= CommonData.bicycles.size()) {
                    Snackbar.make(view, "没有数据需要删除", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Dialog.showSimpleDialog(context, "提示", "删除后将无法恢复，是否确认删除？", "确定", "取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                CommonData.bicycles.remove(CommonData.currentFragment);
                                CommonData.currentFragment = 0;
                                saveListener.save();
                                mViewPager.setAdapter(mSectionsPagerAdapter);
                                if (CommonData.bicycles.size() == 0) {
                                    Intent intent = new Intent(context, AccountActivity.class);
                                    intent.putExtra(AccountActivity.transType, AccountActivity.transTypeAdd);
                                    startActivityForResult(intent, 1);

                                }
                            }
                        }, null, true);
                }
            }
        });

        if (CommonData.bicycles.size() == 0) {
            Intent intent = new Intent(this, AccountActivity.class);
            intent.putExtra(AccountActivity.transType, AccountActivity.transTypeAdd);
            startActivityForResult(intent, 1);
        }

        saveListener = new SaveListener() {
            @Override
            public void save() {
                saveDataToLocal();
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (CommonData.bicycles.size() == 0) {
            Log.d("Finish", "true");
            finish();
        }
        Log.d("Finish", "false");
        if (resultCode == 1) {
            mSectionsPagerAdapter.getItem(CommonData.bicycles.size() - 1);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            saveListener.save();
        }
        else if (requestCode == 2) {
            if (CommonData.currentFragment >= 0) {
                CommonData.bicycles.get(CommonData.currentFragment).setRefresh(true);
                Bicycle.refresh(CommonData.currentFragment, CommonData.currentOnAsyncTaskListener);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            Intent intent = new Intent(this, AccountActivity.class);
            intent.putExtra(AccountActivity.transType, AccountActivity.transTypeAdd);
            startActivityForResult(intent, 1);
            return true;
        }
        else if (id == R.id.action_refresh_all) {
            for (int i = 0; i < CommonData.bicycles.size(); i++)
                CommonData.bicycles.get(i).setRefresh(true);
            Bicycle.refresh(CommonData.currentFragment, CommonData.currentOnAsyncTaskListener);
            return true;
        }
        else if (id == R.id.action_about) {
            Dialog.showSimpleDialog(
                    context, "关于", "浙江省江山市公共自行车租借应用", "确定", "源代码", null,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Uri uri = Uri.parse("https://www.github.com/lqj679ssn/Bicycle/");
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    }, true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getDataFromLocal() {
        SharedPreferences sharedPreferences = getSharedPreferences("bicycle", MODE_PRIVATE);
        CommonData.bicycles.clear();
        int size = sharedPreferences.getInt("size", 0);
        for (int i = 0; i < size; i++) {
            String username = sharedPreferences.getString("username"+i, null);
            String password = sharedPreferences.getString("password"+i, null);
            if (username == null || password == null)
                continue;
            CommonData.bicycles.add(new Bicycle(new Account(username, password)));
        }
    }

    public void saveDataToLocal() {
        SharedPreferences sharedPreferences = getSharedPreferences("bicycle", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int size = CommonData.bicycles.size();
        editor.putInt("size", size);
        for (int i = 0; i < size; i++) {
            Account account = CommonData.bicycles.get(i).getAccount();
            editor.putString("username"+i, account.getUsername());
            editor.putString("password"+i, account.getPassword());
        }
        editor.apply();
    }

    /**
     * A placeholder fragment containing a simple view.
     */

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return CommonData.bicycles.size();
        }
    }
}

interface SaveListener {
    void save();
}
