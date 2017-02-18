package com.smilehacker.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.smilehacker.lego.LegoModel;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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
        test(new Item0Component.Model1());

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
    }

    private void loadData() {
        List<LegoModel> models = new LinkedList<>();
        for (int i = 0; i < 30; i++) {
            Item0Component.Model model = new Item0Component.Model();
            model.title = String.format("item %d", i);
            model.content = i;
            models.add(model);
        }
        mAdapter.setData(models);
    }

    private void test(LegoModel model) {
        if (model.getClass().equals(Item0Component.Model.class)) {
            Log.i("aa", "equal");
        }
    }
}
