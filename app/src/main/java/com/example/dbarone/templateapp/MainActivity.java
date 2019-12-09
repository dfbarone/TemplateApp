package com.example.dbarone.templateapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);


    Glide.with(this)
            .load("")
            .apply(new RequestOptions()
              .centerCrop()
              .error(R.drawable.ic_launcher_background)
                    .apply(RequestOptions.circleCropTransform())
            )
            .into((ImageView)findViewById(R.id.image));

  }

}
