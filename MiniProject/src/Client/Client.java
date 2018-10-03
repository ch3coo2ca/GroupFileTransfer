package Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;

import explorer.ExplorerNode;
import explorer.ExplorerPanel;
import explorer.ExplorerPanel.OnUploadButtonClickListener;
import login.FrameDragListener;
import progress.ProgressPanel;

public class Client extends JFrame implements ActionListener {

	private InetAddress ia;
	private Socket soc;
	private Socket fileSoc;

	private DataOutputStream dos;
	private DataInputStream dis;
	private DataOutputStream fileDos;
	private DataInputStream fileDis;

	private String id, receiver;
	private StringTokenizer strToken;

	private JPanel panel, panel2;
	private JTextArea txtArea;
	private JTextField txtContent;
	private JButton btnSend, btnExitRoom, btnLogout, btnExit;

	private String currentRoom = null;
	private ProgressPanel pp = new ProgressPanel();

	UserList userListAll, userListRoom;
	RoomList roomList;

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void init() {
		Container con = this.getContentPane();
		// con.repaint();
		con.setBackground(new Color(160, 197, 207));
		con.setLayout(new GridBagLayout());

		this.setUndecorated(true);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		userListAll = new UserList("전체 접속자");
		userListRoom = new UserList("현재 방 접속자");
		roomList = new RoomList();

		txtArea = new JTextArea();
		txtArea.setEditable(false);
		txtArea.setEnabled(false);

		// txtArea 자동 스크롤
		DefaultCaret caret = (DefaultCaret) txtArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		txtContent = new JTextField();
		txtContent.setEnabled(false);
		btnSend = new JButton("전송");
		btnSend.setEnabled(false);
		btnExitRoom = new JButton("방 나가기");
		btnExitRoom.setEnabled(false);
		btnLogout = new JButton("로그아웃");
		btnExit = new JButton("X");
		// btnExit.setOpaque(false);
		btnExit.setFont(new Font("맑은 고딕", Font.BOLD, 20));
		btnExit.setForeground(Color.WHITE);
		btnExit.setContentAreaFilled(false);
		btnExit.setBackground(new Color(160, 197, 207));

		btnLogout.setBackground(new Color(254, 115, 96));
		// btnExit.setBackground(new Color(106, 97, 133));
		// btnSend.setBackground(new Color(106, 97, 133));
		btnExitRoom.setBackground(new Color(14, 162, 199));
		btnSend.setBackground(new Color(14, 162, 199));

		btnLogout.setForeground(Color.WHITE);
		btnExitRoom.setForeground(Color.WHITE);
		btnSend.setForeground(Color.WHITE);

		btnSend.addActionListener(this);
		btnExitRoom.addActionListener(this);
		btnLogout.addActionListener(this);
		btnExit.addActionListener(e -> System.exit(0));
		btnExit.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				btnExit.setContentAreaFilled(true);
				btnExit.setBackground(new Color(134, 181, 194));
			};

			public void mouseExited(MouseEvent e) {
				btnExit.setContentAreaFilled(false);
				btnExit.setBackground(new Color(160, 197, 207));
			};
		});

		// Enter 눌렀을때 메시지 전송
		txtContent.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 10) {
					sendServerMsg("SEND_MESSAGE@" + currentRoom + "@" + txtContent.getText());
					txtContent.setText("");
					txtContent.requestFocus();
				}
			}
		});

		JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		// panelTop.setBackground(Color.red);
		panelTop.setOpaque(false);
		panelTop.add(btnExit);

		panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridBagLayout());

		panel2 = new JPanel(); // 채팅방 패널
		panel2.setLayout(new BorderLayout());
		panel2.setBorder(new EtchedBorder(3));

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.7;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0, 0, 20, 0);
		panel.add(setFileExplorer(), c);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.5;
		c.weighty = 0.3;
		c.insets = new Insets(0, 0, 0, 0);
		c.fill = GridBagConstraints.BOTH;
		panel.add(panel2, c);

		JPanel panel3 = new JPanel(); // 보낼 내용, 전송버튼
		JScrollPane scroll = new JScrollPane(txtArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel3.setLayout(new BorderLayout());
		panel2.add("Center", scroll);
		panel2.add("South", panel3);

		panel3.add("Center", txtContent);
		panel3.add("East", btnSend);
		panel3.add("South", btnExitRoom);

		JPanel panelRight = new JPanel(new GridBagLayout());
		panelRight.setOpaque(false);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.1;
		c.insets = new Insets(0, 0, 10, 0);
		JLabel lbl = new JLabel("ID  : " + getId(), JLabel.CENTER);
		lbl.setFont(new Font("맑은 고딕", Font.BOLD, 17));
		// lbl.setForeground(new Color(154, 171, 181));
		lbl.setForeground(Color.WHITE);
		lbl.setBorder(new TitledBorder(new LineBorder(new Color(154, 171, 181), 2)));
		panelRight.add(lbl, c);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.5;
		c.weighty = 0.4;
		c.insets = new Insets(10, 0, 10, 0);
		panelRight.add(userListAll, c);

		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0.5;
		c.weighty = 0.4;
		c.insets = new Insets(10, 0, 0, 0);
		panelRight.add(userListRoom, c);

		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0.5;
		c.weighty = 0.0;
		c.insets = new Insets(10, 0, 0, 0);
		panelRight.add(btnLogout, c);

		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 0;

		c.insets = new Insets(10, 10, 10, 10);
		con.add(panelTop, c);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.1;
		c.weighty = 0.5;
		c.insets = new Insets(10, 10, 10, 10);
		con.add(roomList, c);

		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.3;
		c.weighty = 0.5;
		c.insets = new Insets(10, 10, 10, 10);
		con.add(panel, c);

		c.gridx = 2;
		c.gridy = 1;
		c.weightx = 0.1;
		c.weighty = 0.5;
		c.insets = new Insets(10, 10, 10, 10);
		con.add(panelRight, c);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == btnSend) {
			sendServerMsg("SEND_MESSAGE@" + currentRoom + "@" + txtContent.getText()); // 현재 방에 txtContent 내용 보낸다.
			txtContent.setText("");

		} else if (e.getSource() == btnExitRoom) {
			sendServerMsg("EXIT_ROOM@" + currentRoom);
			currentRoom = null;
			userListRoom.arr_userList.clear();
			DefaultListModel list = (DefaultListModel) userListRoom.userList.getModel();
			list.removeAllElements();

		} else if (e.getSource() == btnLogout) {
			sendServerMsg("LOGOUT@" + id);
			try {
				soc.close();
				fileSoc.close();
				System.exit(0);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	public ExplorerPanel setFileExplorer() {
		ExplorerPanel ep = new ExplorerPanel();

		ep.setOnUploadButtonClickListener(new OnUploadButtonClickListener() {
			@Override
			public void onUpload(List<ExplorerNode> list) {
				// TODO : 여기에 업로드 버튼 이벤트 추가
				for (ExplorerNode en : list) {
					sendServerFileMsg("FILE@ ");
					fileRead(en.getFile().getName(), en.getFile().getParent());
				}
			}
		});
		return ep;
	}

	public Client(String id) {
		this.setId(id);
		this.setTitle(id + "님");
		this.init();

		super.setSize(900, 600);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) (screen.getWidth() / 2 - super.getWidth() / 2);
		int y = (int) (screen.getHeight() / 2 - super.getHeight() / 2);
		super.setLocation(x, y);
		super.setResizable(false);
		super.setVisible(true);
		
		// FrameDrag 적용
		FrameDragListener frameDragListener = new FrameDragListener(this);
		this.addMouseListener(frameDragListener);
		this.addMouseMotionListener(frameDragListener);
		this.setVisible(true);
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// 닫기 버튼
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				sendServerMsg("LOGOUT@" + id);
			}
		});

		try {
			ia = InetAddress.getByName("14.49.39.77");
			//ia = InetAddress.getByName("127.0.0.1");
			// ia = InetAddress.getByName("172.16.103.117");
			soc = new Socket(ia, 20000);
			fileSoc = new Socket(ia, 20001);

			dis = new DataInputStream(soc.getInputStream());
			dos = new DataOutputStream(soc.getOutputStream());
			fileDis = new DataInputStream(fileSoc.getInputStream());
			fileDos = new DataOutputStream(fileSoc.getOutputStream());

			new MessageClient().start();
			new FileClient().start();

			sendServerMsg("LOGIN@" + id); // 서버에 로그인 알림

			sendServerFileMsg("FILELOGIN@" + id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class MessageClient extends Thread {
		@Override
		public void run() {
			String msg;
			while (true) {
				try {
					msg = dis.readUTF();
					getServerMsg(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

	class FileClient extends Thread {
		@Override
		public void run() {
			String fileMsg;
			while (true) {
				try {
					fileMsg = fileDis.readUTF();
					getServerFile(fileMsg);
				} catch (IOException e) {
					// socket close & reConnectoin
					try {
						fileSoc.close();
						new FileClient().start();
						sendServerFileMsg("FILELOGIN@" + id);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}

		}
	}

	public void getServerMsg(String str) {
		strToken = new StringTokenizer(str, "@");
		String command = strToken.nextToken();
		String msg = strToken.nextToken();

		System.out.println("client >> " + str);

		switch (command) {
		case "UPDATE_USERLIST":
			userListAll.setUserList();
			break;

		case "NEW_USER":
			userListAll.arr_userList.add(msg);
			break;

		case "OTHER_USER":
			userListAll.arr_userList.add(msg);
			break;

		case "REMOVE_USER":
			userListAll.arr_userList.remove(msg);
			break;

		case "NEW_ROOM_USER":
			userListRoom.arr_userList.add(msg);
			txtArea.append("[" + msg + "] 님이 참가하셨습니다. \n");
			break;

		case "OTHER_ROOM_USER":
			userListRoom.arr_userList.add(msg);
			break;

		case "REMOVE_ROOM_USER":
			userListRoom.arr_userList.remove(msg);
			txtArea.append("[" + msg + "] 님이 나가셨습니다. \n");
			break;

		case "UPDATE_ROOM_USERLIST":
			userListRoom.setUserList();
			break;

		case "UPDATE_ROOMLIST":
			roomList.setRoomList();
			break;

		case "NEW_ROOM":
			roomList.arr_roomList.add(msg);
			break;

		case "REMOVE_ROOM":
			roomList.arr_roomList.remove(msg);
			break;

		case "REMOVE_ROOM_ALL":
			DefaultListModel<String> list = (DefaultListModel<String>) roomList.roomList.getModel();
			list.removeAllElements();
			break;

		case "ROOM_CREATE_FAIL":
			JOptionPane.showMessageDialog(null, "이미 존재하는 방입니다!", "", JOptionPane.WARNING_MESSAGE);
			break;

		case "OTHER_ROOM":
			roomList.arr_roomList.add(msg);
			break;

		case "JOIN_ROOM":
			currentRoom = msg; // 현재 방
			txtArea.setEnabled(true);
			txtContent.setEnabled(true);
			btnSend.setEnabled(true);
			btnExitRoom.setEnabled(true);
			txtArea.setText("");
			txtArea.append("========" + msg + "========\n");
			break;

		case "EXIT_ROOM":
			currentRoom = null;
			txtArea.setEnabled(false);
			txtContent.setEnabled(false);
			btnSend.setEnabled(false);
			btnExitRoom.setEnabled(false);

			txtArea.setText("");
			txtContent.setText("");
			break;
		case "CHAT":
			String txtContent = strToken.nextToken(); // 전송할 메시지
			txtArea.append("[" + msg + "] : " + txtContent + "\n");
			break;

		case "NOTE":
			String content = strToken.nextToken();
			showRcvDialog(msg, content);
			break;
		case "FILE":
			String spt[] = msg.split("#");

			int dlgbtn = JOptionPane.YES_NO_OPTION;
			int check = JOptionPane.showConfirmDialog(this, spt[0] + "을 수신하시겠습니까?", "", dlgbtn);
			if (check == 0) {
				sendServerMsg("FILE@" + spt[0] + "#" + spt[1] + "#" + spt[2]);
			}
			break;

		default:
			break;
		}

	}

	public void showSendDialog() {
		JDialog dialog = new JDialog();
		dialog.setLayout(new BorderLayout());
		dialog.setSize(400, 300);
		dialog.setLocation(this.getX() + 100, this.getY() + 100);
		dialog.setTitle("받는 사람 : " + receiver);

		JTextArea ta = new JTextArea();
		JButton btnSend = new JButton("쪽지 전송");

		dialog.add("Center", ta);
		dialog.add("South", btnSend);

		dialog.setVisible(true);
		btnSend.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				// 보내는 사람 : id , 받는 사람 : receiver
				// 전송할 메시지 : ta
				sendServerMsg("NOTE@" + id + "@" + receiver + "@" + ta.getText());
				dialog.dispose();
			}
		});

	}

	// 쪽지 뷰
	public void showRcvDialog(String sender, String content) {
		JDialog dialog = new JDialog();
		dialog.setLayout(new BorderLayout());
		dialog.setSize(400, 300);
		dialog.setLocation(this.getX() + 200, this.getY() + 200);
		dialog.setTitle(sender + "님으로 부터 메시지 도착");
		receiver = sender;

		JTextArea ta = new JTextArea();
		JButton btnSend = new JButton("답장하기");

		ta.setText(content);
		ta.setEditable(false);
		ta.setBackground(new Color(209, 229, 235));
		ta.setFont(new Font("맑은고딕", Font.PLAIN, 17));

		dialog.add("Center", ta);
		dialog.add("South", btnSend);

		dialog.setVisible(true);
		btnSend.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				showSendDialog();
				dialog.dispose();
			}
		});

		dialog.setAlwaysOnTop(true);
		this.requestFocus();
	}

	public void getServerFile(String str) {
		switch (str) {
		case "FILE":

			break;
		case "FILESEND@ ":
			fileWrite();
			break;
		}

	}

	public void sendServerMsg(String str) {
		System.out.println("str = " + str);
		try {
			dos.writeUTF(str);
			dos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendServerFileMsg(String str) {
		try {
			System.out.println("file = " + str);
			fileDos.writeUTF(str);
			fileDos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// File 서버에 전송
	private void fileRead(String fileNm, String filePath) {
		try {
			File file = new File(filePath + "\\" + fileNm);

			fileDos.writeUTF(fileNm);
			fileDos.flush();
			fileDos.writeUTF(getId()); // 자기 자신의 ID 발송
			fileDos.flush();
			fileDos.writeLong(file.length());
			fileDos.flush();
			fileDos.writeUTF(currentRoom);
			fileDos.flush();

			fileDos.flush();
			System.out.println("파일 이름(" + fileNm + ")을 전송하였습니다.");
			// 파일을 읽어서 서버에 전송

			FileInputStream fis = new FileInputStream(file);
			FileChannel fcin = fis.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate((int) file.length());

			/*// progress bar
			pp.setProgressMax(((int) file.length()) / 1000000);
			// 제목 설정
			pp.setFileName(fileNm);
			JFrame frame = new JFrame();
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (int) (screen.getWidth() / 2 - frame.getWidth() / 2);
			int y = (int) (screen.getHeight() / 2 - frame.getHeight() / 2);

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(pp);
			frame.pack();
			frame.setLocation(x, y);
			frame.setVisible(true);
			 */
			long sum = 0;
			int read;
			while ((read = fcin.read(buffer)) > 0) {
				buffer.flip();
				fileDos.write(buffer.array());
				
				/*// 진행 정도 업데이트
				pp.setCurrentProgress((int) sum / 1000000);
				// 진행 사항 업데이트
				pp.notifyProgressChanged();*/
			}
			fileDos.flush();
			//frame.setVisible(false);

			System.out.println("전송 파일 크기 : " + file.length());
			fis.close();

			System.out.println("fileRead : 전송 성공");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("fileRead : 전송 실패");
		}
	}

	// File 서버에서 받고 파일에 쓰기
	private String fileWrite() {

		String result;
		String filePath = "";

		try {
			System.out.println("파일 수신 작업을 시작");
			fileDis = new DataInputStream(fileSoc.getInputStream());
			String fileNm = fileDis.readUTF();
			long fileLength = fileDis.readLong();

			String[] splitString = new String[2];
			splitString = fileNm.split("\\.");

			
			FileDialog fileDialog = new FileDialog(this, fileNm, FileDialog.SAVE);
			fileDialog.setModal(true);
			fileDialog.setVisible(true);
			filePath = fileDialog.getDirectory();
			fileNm = fileDialog.getFile() + "." + splitString[1];
			if (filePath == null) {
				return "ERROR";
			}

			// 파일을 생성하고 파일에 대한 출력 스트림 생성
			File file = new File(filePath + "/" + fileNm);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] readBuffer = new byte[1024];

			pp.setProgressMax((int) fileLength / 1000000);
			// 제목 설정
			pp.setFileName(fileNm);

			JFrame frame = new JFrame();
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (int) (screen.getWidth() / 2 - frame.getWidth() / 2);
			int y = (int) (screen.getHeight() / 2 - frame.getHeight() / 2);

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(pp);
			frame.pack();
			frame.setLocation(x, y);
			frame.setVisible(true);
			int read;
			int sum = 0;
			long remain = fileLength;

			while ((read = fileDis.read(readBuffer, 0, remain >= 1024 ? 1024 : (int) remain)) > 0) {
				sum += read;
				remain -= read;

				ByteBuffer bb = ByteBuffer.wrap(readBuffer, 0, read);
				FileChannel fcout = fos.getChannel();
				fcout.write(bb);

				// 진행 정도 업데이트
				pp.setCurrentProgress((int) sum / 1000000);

				// 진행 사항 업데이트
				pp.notifyProgressChanged();
				if (sum == fileLength)
					break;
			}

			frame.setVisible(false);

			result = "SUCCESS";
			fos.close();

			System.out.println(file.getName() + "파일 수신 작업을 완료");
			System.out.println("받은 파일의 사이즈 : " + file.length());

		} catch (Exception e) {
			e.printStackTrace();
			result = "ERROR";
		}

		return result;
	}

	// 사용자 리스트
	class UserList extends JPanel {

		JList userList;
		String title;
		ArrayList<String> arr_userList;

		public void init() {
			this.setLayout(new BorderLayout());
			this.setBackground(new Color(247, 245, 245));
			userList = new JList<>();
			userList.setBackground(new Color(247, 245, 245));
			arr_userList = new ArrayList<>();

			JPopupMenu menu = new JPopupMenu();
			JMenuItem item = new JMenuItem("쪽지 보내기");
			menu.add(item);

			userList.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (!userList.isSelectionEmpty()) {
						if (e.getButton() == MouseEvent.BUTTON3) {
							if (!userList.getSelectedValue().equals(id)) {
								receiver = userList.getSelectedValue().toString();
								menu.show(userList, e.getX(), e.getY());
							}

						}
					}
				}
			});

			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					showSendDialog();
				}
			});

			setBorder(BorderFactory.createTitledBorder(null, title, TitledBorder.CENTER, TitledBorder.TOP,
					new Font("맑은고딕", Font.BOLD, 12), new Color(41, 59, 71)));

			JScrollPane scroll = new JScrollPane(userList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

			scroll.setBorder(BorderFactory.createEmptyBorder());
			add("Center", scroll);

		}

		public UserList(String title) {
			this.title = title;
			this.init();
			super.setSize(400, 300);
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (int) (screen.getWidth() / 2 - super.getWidth() / 2);
			int y = (int) (screen.getHeight() / 2 - super.getHeight() / 2);
			super.setLocation(x, y);
			super.setVisible(true);

		}

		public void setUserList() {
			DefaultListModel<String> listModel = new DefaultListModel<>();
			System.out.println(arr_userList.toString());

			for (String user : arr_userList) {
				listModel.addElement(user);
				userList.setModel(listModel);
			}
		}

	}

	class RoomList extends JPanel implements ActionListener {

		private JList roomList;
		private JButton btnMakeRoom;

		private ArrayList<String> arr_roomList;

		public void init() {
			this.setLayout(new BorderLayout(10, 10));
			this.setOpaque(false);
			roomList = new JList<>();
			roomList.setBackground(new Color(243, 243, 243));
			arr_roomList = new ArrayList<>();
			btnMakeRoom = new JButton("방 만들기");
			btnMakeRoom.setBackground(new Color(14, 162, 199));
			btnMakeRoom.setForeground(Color.WHITE);
			// 방 더블 클릭
			roomList.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					JList list = (JList) e.getSource();
					int index = list.locationToIndex(e.getPoint());
					String room = list.getModel().getElementAt(index).toString(); // 클릭한 방 제목

					if (e.getClickCount() == 2) {

						if (currentRoom != null) { // 현재 참가중인 방이 있을때
							JOptionPane.showMessageDialog(null, "방은 하나만 참가할 수 있습니다!", "",
									JOptionPane.INFORMATION_MESSAGE);
						} else {
							sendServerMsg("JOIN_ROOM@" + room);
						}
					}

				};
			});

			JScrollPane scroll = new JScrollPane(roomList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			add("Center", scroll);
			add("South", btnMakeRoom);
			btnMakeRoom.addActionListener(this);

		}

		public RoomList() {
			this.init();
			super.setSize(400, 300);
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (int) (screen.getWidth() / 2 - super.getWidth() / 2);
			int y = (int) (screen.getHeight() / 2 - super.getHeight() / 2);
			super.setLocation(x, y);
			super.setVisible(true);

		}

		// 방 목록 설정
		public void setRoomList() {
			DefaultListModel<String> listModel = new DefaultListModel<>();

			for (String roomName : arr_roomList) {
				listModel.addElement(roomName);
				roomList.setModel(listModel);
			}

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == btnMakeRoom) { // 방 만들기
				String roomName = JOptionPane.showInputDialog(this, "방이름 :", "방이름 입력", JOptionPane.INFORMATION_MESSAGE);

				if (roomName.length() > 0)
					sendServerMsg("CREATE_ROOM@" + roomName); // 서버에 보냄
				else
					JOptionPane.showMessageDialog(null, "방 이름을 입력해주세요!", "", JOptionPane.WARNING_MESSAGE);
			}
		}

	}

}
