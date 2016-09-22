package com.doulala.annotation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.annotaions.bindview.BindView;
import com.doulala.annotation.components.bindview.InjectView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.txv)
    TextView txv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InjectView.bind(this);
        txv.setText("123");
    }
}
