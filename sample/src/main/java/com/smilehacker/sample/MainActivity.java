package com.smilehacker.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.smilehacker.lego.Lego;
import com.smilehacker.lego.util.NoAlphaDefaultItemAnimator;
import com.smilehacker.lego.util.StickyHeaderRecyclerViewContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRv;
    private TestAdapter mAdapter;
    private Button mBtn;
    private StickyHeaderRecyclerViewContainer mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Lego.addFactory(LegoFactory_sample.class);

        mRv = (RecyclerView) findViewById(R.id.rv);
        mBtn = (Button) findViewById(R.id.btn_refresh);
        mContainer = (StickyHeaderRecyclerViewContainer) findViewById(R.id.container);
        mAdapter = new TestAdapter(this);

        mRv.setAdapter(mAdapter);
        mRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRv.setItemAnimator(new NoAlphaDefaultItemAnimator());
        mContainer.addHeaderViewType(new Item1Component(this).getViewType());

        loadData();

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });

        Item0Component.Model model = new Item0Component.Model();
        model.id = "aaa";
        Log.d(TAG, "model index = " + Lego.legoFactoryProxy.getModelIndex(model, null));

        Boolean b = true;
        Log.d(TAG, "b hash = " + b.hashCode());
        b = false;
        Log.d(TAG, "b hash = " + b.hashCode());
        List<String> a = new ArrayList<>();
        Log.d(TAG, "a hash = " + a.hashCode());
        a.add("aaa");
        Log.d(TAG, "a hash = " + a.hashCode());
        a.add("ccc");
        Log.d(TAG, "a hash = " + a.hashCode());

        model.id = "aaa";
        Log.d(TAG, "model hash = " + model.hashCode());
        model.id = "aaaaa";
        Log.d(TAG, "model hash = " + model.hashCode());

    }


    private void loadData() {
        List<Object> models = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Item0Component.Model model = new Item0Component.Model();
            model.id = i + "";
            model.title = String.format("item %d", i);
            model.content = new Random().nextInt();
            models.add(model);
        }

        Item1Component.Model model1 = new Item1Component.Model();
        model1.title = "a";
        Item1Component.Model model2 = new Item1Component.Model();
        model2.title = "b";

        models.add(0, model1);
        models.add(10, model2);

        mAdapter.commitData(models);
    }


}
