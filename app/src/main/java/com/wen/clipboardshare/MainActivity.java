package com.wen.clipboardshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

/**
 * Author: wenxl
 * Date: 20-12-15 下午3:25
 * Description:
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, ClientService.class));
    }
}
