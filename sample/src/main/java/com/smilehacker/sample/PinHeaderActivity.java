package com.smilehacker.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.smilehacker.lego.util.NoAlphaDefaultItemAnimator;
import com.smilehacker.lego.util.StickyHeaderRecyclerViewContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PinHeaderActivity extends AppCompatActivity {

    private static final String TAG = PinHeaderActivity.class.getSimpleName();
    private RecyclerView mRv;
    private TestAdapter mAdapter;
    private StickyHeaderRecyclerViewContainer mContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_header);

        mRv = findViewById(R.id.rv);
        mContainer = findViewById(R.id.container);
        mAdapter = new TestAdapter(this);

        mRv.setAdapter(mAdapter);
        mRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRv.setItemAnimator(new NoAlphaDefaultItemAnimator());
        mContainer.addHeaderViewType(new Item1Component(this).getViewType());

        testPinHeader();
    }


    private void testPinHeader() {
        List<Object> models = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Item0Component.Model model = new Item0Component.Model();
            model.id = i;
            model.desc = String.format("item %d", i);
            model.content = new Random().nextInt();
            models.add(model);
        }

        models.add(0, new Item1Component.Model("First"));
        models.add(10, new Item1Component.Model("Second"));
        models.add(20, new Item1Component.Model("Third"));

        mAdapter.commitData(models);
    }

}
