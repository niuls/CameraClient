package cn.woodox.test.cameraclient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;

public class MainActivity extends Activity {
	private EditText etIP, etPort;
	private TextView tvRate, tvGPS;
	private Button btnConnect;
	private ImageView ivVideo;
	private Bitmap bmp = null;
	private int fps = 0;
	private String localAddr = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//禁止屏幕休眠
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		findViews();
		new Thread(){
			@Override
			public void run() {
				super.run();
				startServer();
			}
		}.start();
	}

	void findViews() {
		etIP = (EditText) findViewById(R.id.etIP);
		etPort = (EditText) findViewById(R.id.etPort);
		btnConnect = (Button) findViewById(R.id.btnConnect);
		btnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread() {
					@Override
					public void run() {
						super.run();
						if (localAddr == null) {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						connectServer();
					}
				}.start();
			}
		});
		tvRate = (TextView) findViewById(R.id.tvRate);
		tvGPS = (TextView) findViewById(R.id.tvGPS);
		ivVideo = (ImageView) findViewById(R.id.ivVideo);
	}

	void startServer() {
		ServerSocket ss;
		try {
			ss = new ServerSocket(20000);
			localAddr = getLocalIpAddress() + ":" + ss.getLocalPort();
			while (true) {
				Socket s = ss.accept();
				DataInputStream dataInput = new DataInputStream(s.getInputStream());
				Bundle bundle = new Bundle();
				bundle.putString("position", dataInput.readUTF());
				Message msg = new Message();
				msg.what = 0x123;
				msg.setData(bundle);
				uiHandler.sendMessage(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					//过滤掉回环地址和IPv6
					if (!inetAddress.isLoopbackAddress() && !(inetAddress instanceof Inet6Address)) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	void connectServer() {
		long time = new Date().getTime();
		int framesCount = 0;
		try {
			Socket socket = new Socket(etIP.getText().toString(), Integer.parseInt(etPort.getText().toString()));
			DataOutputStream dataOutput = new DataOutputStream(socket.getOutputStream());
			if (localAddr != null) {
				dataOutput.writeUTF(localAddr);
				dataOutput.flush();
			}else {
				Log.e("what","localAddr is null!!!!");
			}
//			dataOutput.close();
			DataInputStream dataInput = new DataInputStream(socket.getInputStream());
			while (true) {
				int size = dataInput.readInt();
				byte[] data = new byte[size];
				System.out.println("size:" + size);
				int len = 0;
				while (len < size) {
					len += dataInput.read(data, len, size - len);
				}
				bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

				framesCount++;
				if (new Date().getTime() - time >= 1000) {
					time = new Date().getTime();
					fps = framesCount;
					framesCount = 0;
				}
				Message msg = new Message();
				msg.what = 0x456;
				uiHandler.sendMessage(msg);
			}
//			dataInput.close();
//			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0x123) {
				try {
					tvGPS.setText(msg.getData().getString("position"));
				} catch (Exception e) {
					e.printStackTrace();
					tvGPS.setText("Data null.");
				}
			}
			if (msg.what == 0x456) {
				ivVideo.setImageBitmap(bmp);
				tvRate.setText("Resolution: " + bmp.getWidth() + " * " + bmp.getHeight() + "    FPS:" + fps);
			}
		}
	};
}
