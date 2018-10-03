package explorer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import javax.swing.JFrame;

import java.nio.ByteBuffer;

import explorer.ExplorerPanel.OnUploadButtonClickListener;

public class DemoFrame {

	public static void main(String[] args) {

		// 드라이브 전부 불러오기
		ExplorerPanel ep = new ExplorerPanel();
		
		// 루트 폴더 선택하기
//		ExplorerPanel ep = new ExplorerPanel("C:/");
		
		ep.setOnUploadButtonClickListener(new OnUploadButtonClickListener() {
			@Override
			public void onUpload(List<ExplorerNode> list) {
				for (ExplorerNode en : list) {
					FileInputStream fis;
					FileOutputStream fos;
					
					try {
						fis = new FileInputStream(en.getFile());
						fos = new FileOutputStream(en.getFile().getName());
						
						FileChannel fcin = fis.getChannel();
						FileChannel fcout = fos.getChannel();
						
						ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
						
						
						int read;
						while((read = fcin.read(byteBuffer)) != -1) {
							byteBuffer.flip();
							String base64 = Base64.getEncoder().encodeToString(byteBuffer.array());
							
							ByteBuffer bb = ByteBuffer.wrap(Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8)));
							fcout.write(bb);
							
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		JFrame frame = new JFrame();
		frame.setContentPane(ep);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 700);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
	}

}
