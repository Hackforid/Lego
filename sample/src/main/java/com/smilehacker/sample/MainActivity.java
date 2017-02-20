package com.smilehacker.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.smilehacker.lego.ILegoFactory;
import com.smilehacker.lego.LegoFactory;
import com.smilehacker.lego.LegoModel;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
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
        test(new Item0Component.Model1());

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //loadData();
                try {
                    test1();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
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
        mAdapter.commitData(models);
    }

    private void test(LegoModel model) {
        if (model.getClass().equals(Item0Component.Model.class)) {
            Log.i("aa", "equal");
        }


    }

    private void test1() throws InvocationTargetException, IllegalAccessException {
        Item0Component.Model model = new Item0Component.Model();
        model.title = String.format("item %d", 1);
        model.content = 1;

        long now = System.currentTimeMillis();
        ILegoFactory legoFactory = new LegoFactory();
        for (int i = 0; i < 10000000; i++) {
            legoFactory.getModelIndex(model);
        }

        Log.i(TAG, "native cost=" +(System.currentTimeMillis() - now));



        now = System.currentTimeMillis();
        legoFactory = TestAdapter.legoFactory;
        for (int i = 0; i < 10000000; i++) {
            legoFactory.getModelIndex(model);
        }
        Log.i(TAG, "reflect cost=" +(System.currentTimeMillis() - now));

    }
}
