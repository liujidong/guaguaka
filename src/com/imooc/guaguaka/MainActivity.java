package com.imooc.guaguaka;

import com.imooc.guaguaka.view.GuaGuaKa;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
	GuaGuaKa mGuaGuaKa;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mGuaGuaKa = (GuaGuaKa)findViewById(R.id.id_guaguaka);
        mGuaGuaKa.setOnGuaGuaKaCompleteListener(new GuaGuaKa.OnGuaGuaKaCompleteListener() {
			
			@Override
			public void complete() {
				Toast.makeText(getApplicationContext(), "用户已经挂的差不多了", Toast.LENGTH_SHORT).show();
				
			}
		});
        //mGuaGuaKa.setText("Android新技能");
    }
}