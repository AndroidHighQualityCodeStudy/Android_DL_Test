package com.xiaxl.dynamicloadhost;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends Activity {

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // 请求sdcard权限
        ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 10010);

        //
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                startProxyActivity();
            }
        });
    }


    private void startProxyActivity() {
        Intent intent = new Intent(this, ProxyActivity.class);
        intent.putExtra(ProxyActivity.EXTRA_DEX_PATH, "/mnt/sdcard/m_client.apk");
        startActivity(intent);
    }

}
