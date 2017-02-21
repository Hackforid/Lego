package com.smilehacker.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.smilehacker.lego.LegoModel;
import com.smilehacker.lego.util.StickyHeaderRecyclerViewContainer;

import java.util.ArrayList;
import java.util.List;

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
        mRv = (RecyclerView) findViewById(R.id.rv);
        mBtn = (Button) findViewById(R.id.btn_refresh);
        mContainer = (StickyHeaderRecyclerViewContainer) findViewById(R.id.container);
        mAdapter = new TestAdapter(this);

        mRv.setAdapter(mAdapter);
        mRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
//        StickyHeaderDecoration decoration = new StickyHeaderDecoration();
//        decoration.addHeaderViewType(new Item1Component(this).getViewType());

//        mRv.addItemDecoration(decoration);
        mContainer.addHeaderViewType(new Item1Component(this).getViewType());

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
