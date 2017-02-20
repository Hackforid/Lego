package com.smilehacker.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.smilehacker.lego.LegoFactory;
import com.smilehacker.lego.LegoModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRv;
    private TestAdapter mAdapter;
    private Button mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRv = (RecyclerView) findViewById(R.id.rv);
        mBtn = (Button) findViewById(R.id.btn_refresh);
        mAdapter = new TestAdapter(this);

        mRv.setAdapter(mAdapter);
        mRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        loadData();

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
    }


    private void loadData() {
        List<LegoModel> models = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Item0Component.Model model = new Item0Component.Model();
            model.title = String.format("item %d", i);
            model.content = i;
            model.b = new int[] {1,2};
            model.a = new ArrayList<>();
            model.a.add("aa");
            model.a.add("bb");
            models.add(model);
        }
        LegoFactory factory = new LegoFactory();
        factory.isModelEquals(models, mAdapter.getData());
        mAdapter.commitData(models);

        Item0Component.Model1 a = new Item0Component.Model1();
        a.model2 = new Item0Component.Model2();
        a.model2.a = "a";

        Item0Component.Model1 b = new Item0Component.Model1();
        b.model2 = new Item0Component.Model2();
        b.model2.a = "aa";

        Log.d(TAG, "is equals " + factory.isModelEquals(a, b));
    }


}
