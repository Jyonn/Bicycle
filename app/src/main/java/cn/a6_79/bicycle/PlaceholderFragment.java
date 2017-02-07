package cn.a6_79.bicycle;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    ImageView imageView;
    TextView startTextView, endTextView, nameTextView;
    int sessionNumber;
    LinearLayout startLinearLayout, endLinearLayout;
    TextView historyTextView;

    static Context context;
    static MainActivity.SectionsPagerAdapter sectionsPagerAdapter;
    static ViewPager viewPager;
    static SaveListener saveListener;

    public PlaceholderFragment() {
    }


    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            CommonData.currentFragment = getArguments().getInt(ARG_SECTION_NUMBER);
            CommonData.currentOnAsyncTaskListener = onAsyncTaskListener;
        }
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(
            int sectionNumber, Context context, SaveListener saveListener,
            MainActivity.SectionsPagerAdapter sectionsPagerAdapter, ViewPager viewPager) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        PlaceholderFragment.context = context;
        PlaceholderFragment.sectionsPagerAdapter = sectionsPagerAdapter;
        PlaceholderFragment.viewPager = viewPager;
        PlaceholderFragment.saveListener = saveListener;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        imageView = (ImageView) rootView.findViewById(R.id.bicycle_image);
        startTextView = (TextView) rootView.findViewById(R.id.start_place);
        endTextView = (TextView) rootView.findViewById(R.id.end_place);
        nameTextView = (TextView) rootView.findViewById(R.id.name);

        startLinearLayout = (LinearLayout) rootView.findViewById(R.id.start_block);
        endLinearLayout = (LinearLayout) rootView.findViewById(R.id.end_block);
        historyTextView = (TextView) rootView.findViewById(R.id.no_history);

        sessionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        Bicycle.refresh(sessionNumber, onAsyncTaskListener);

        Button modifyButton = (Button) rootView.findViewById(R.id.action_modify);
        modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AccountActivity.class);
                intent.putExtra(AccountActivity.transType, AccountActivity.transTypeModify);
                intent.putExtra(AccountActivity.transIndex, sessionNumber);
                startActivityForResult(intent, 1);
            }
        });

        Button deleteButton = (Button) rootView.findViewById(R.id.action_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog.showSimpleDialog(context, "提示", "删除后将无法恢复，是否确认删除？", "确定", "取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                CommonData.bicycles.remove(sessionNumber);
                                CommonData.currentFragment = -1;
                                saveListener.save();
                                viewPager.setAdapter(sectionsPagerAdapter);
                            }
                        }, null, true);
            }
        });
        return rootView;
    }

    OnAsyncTaskListener onAsyncTaskListener = new OnAsyncTaskListener() {
        @Override
        public void callback(Bicycle bicycle) {
            if (bicycle == null) {
                nameTextView.setText("无法获取");
                return;
            }
            imageView.setImageBitmap(bicycle.getBitmap());

            BicycleRecord bicycleRecord = bicycle.getBicycleRecord();
            if (bicycleRecord != null) {
                historyTextView.setVisibility(View.INVISIBLE);
                startLinearLayout.setVisibility(View.VISIBLE);
                endLinearLayout.setVisibility(View.VISIBLE);
                startTextView.setText(bicycleRecord.getStartPoint() + " " + bicycleRecord.getStartTime());
                endTextView.setText(bicycleRecord.getEndPoint() + " " + bicycleRecord.getEndTime());
            } else {
                historyTextView.setVisibility(View.VISIBLE);
                startLinearLayout.setVisibility(View.INVISIBLE);
                endLinearLayout.setVisibility(View.INVISIBLE);
            }

            UserInfo userInfo = bicycle.getUserInfo();
            if (userInfo != null) {
                nameTextView.setText(userInfo.getRealname());
            }
        }
    };
}
