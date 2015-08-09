package cn.woodox.test.cameraclient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class MainActivity extends Activity {
	private EditText etIP,etPort;
	private TextView tvResolu;
	private Button btnConnect;
	private ImageView ivVideo;
	private Bitmap bmp = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViews();
	}

	void findViews(){
		etIP = (EditText)findViewById(R.id.etIP);
		etPort = (EditText)findViewById(R.id.etPort);
		btnConnect = (Button)findViewById(R.id.btnConnect);
		btnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread() {
					@Override
					public void run() {
						super.run();
						connectServer();
					}
				}.start();
			}
		});
		tvResolu = (TextView)findViewById(R.id.tvResolu);
		ivVideo = (ImageView)findViewById(R.id.ivVideo);
	}

	void connectServer(){
		try{
			System.out.println(etIP.getText().toString());
			Socket socket = new Socket(etIP.getText().toString(),Integer.parseInt(etPort.getText().toString()));
			DataInputStream dataInput = new DataInputStream(socket.getInputStream());
			while (true) {
				int size = dataInput.readInt();
				byte[] data = new byte[size];
				System.out.println("size:"+size);
				int len = 0;
				while (len < size) {
					len += dataInput.read(data, len, size - len);
				}
				bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

				Message msg = new Message();
				msg.what = 0x456;
				uiHandler.sendMessage(msg);
			}
//			dataInput.close();
//			socket.close();
		}catch (IOException e){
			e.printStackTrace();
		}
	}

	Handler uiHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == 0x123){

			}
			if(msg.what == 0x456){
				ivVideo.setImageBitmap(bmp);
				tvResolu.setText("Resolution: "+bmp.getWidth()+" * "+bmp.getHeight());
			}
		}
	};
}
