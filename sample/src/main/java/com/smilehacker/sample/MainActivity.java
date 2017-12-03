package com.smilehacker.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.smilehacker.lego.annotation.LegoField;
import com.smilehacker.lego.annotation.LegoIndex;
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

        testModelHash();
        //loadData();

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                models.get(0).title = "aaa" + System.currentTimeMillis();
                List<Object> r = new ArrayList<>();
                r.addAll(models);
                mAdapter.commitData(r);

                //loadData();
            }
        });

    }
    List<Item0Component.Model> models = new ArrayList<>();

    private void testModelHash() {
        for (int i = 0; i < 30; i++) {
            Item0Component.Model model = new Item0Component.Model();
            model.id = i + "";
            model.title = String.format("item %d", i);
            model.content = new Random().nextInt();
            models.add(model);
        }
        List<Object> r = new ArrayList<>();
        r.addAll(models);
        mAdapter.commitData(r);
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

    public class Test0 {
        @LegoIndex
        public int a = 1;
        @LegoField
        public String b = "";
    }
    public class Test1 {
        @LegoIndex
        public int a = 1;
        @LegoField
        public Test0 c;
    }

}
