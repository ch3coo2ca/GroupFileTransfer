package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Server {

	private ServerSocket ss, ss2;
	private Socket soc, fileSoc;

	private DataInputStream dis;
	private DataOutputStream dos;

	private ArrayList<RoomInfo> arr_room;
	private ArrayList<UserInfo> arr_user;

	public Server() throws Exception {
		ss = new ServerSocket(20000);
		ss2 = new ServerSocket(20001);
		arr_room = new ArrayList<>();
		arr_user = new ArrayList<>();

		new MessageServer().start();
		new FileServer().start();

	}

	class MessageServer extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					soc = ss.accept();
					System.out.println(soc.toString() + "연결");

					UserInfo userInfo = new UserInfo(soc);
					Thread th = new Thread(userInfo);
					th.start();
				} catch (IOException e) {
					try {
						soc.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
					break;
				}
			}
		}
	}

	class FileServer extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					fileSoc = ss2.accept();
					System.out.println(fileSoc.toString() + "file socket 연결");

					FileSocket fileSocket = new FileSocket(fileSoc);
					Thread th = new Thread(fileSocket);
					th.start();
				} catch (IOException e) {
					try {
						fileSoc.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
					break;
				}
			}
		}
	}

	// 클라이언트에 메시지 보냄
	private void sendClientMsg(Socket soc, String str) {
		try {
			dos = new DataOutputStream(soc.getOutputStream());
			dos.writeUTF(str);
			dos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void unicast(String str, ArrayList<String> users) {
		for (int j = 0; j < users.size(); j++) {
			for (int i = 0; i < arr_user.size(); i++) {
				if (!users.get(j).equals(arr_user.get(i).getId()))
					continue;
				Socket soc = arr_user.get(i).getSoc();
				sendClientMsg(soc, str);
			}
		}
	}

	public void broadcast(String str) {
		System.out.println("str = " + str);

		for (int i = 0; i < arr_user.size(); i++) {
			Socket soc = arr_user.get(i).getSoc();
			sendClientMsg(soc, str);
		}

	}

	class UserInfo implements Runnable {

		private String id;
		private Boolean roomState = true;
		private String currentRoom = "";
		private Socket soc;
		private FileSocket fileSocket;
		private StringTokenizer strToken;

		public Socket getSoc() {
			return soc;
		}

		public String getId() {
			return id;
		}

		public FileSocket getFileSocket() {
			return fileSocket;
		}

		public void setFileSocket(FileSocket fileSocket) {
			this.fileSocket = fileSocket;
		}

		public void setId(String id) {
			this.id = id;
		}

		public UserInfo(Socket soc) {
			this.soc = soc;

			setUser();
			setRoom();
			arr_user.add(this);

		}

		// 기존에 있던 사용자
		public void setUser() {
			for (int i = 0; i < arr_user.size(); i++) {
				String id = arr_user.get(i).getId();
				sendClientMsg(soc, "OTHER_USER@" + id); // 나머지 사용자
			}
		}

		// 기존에 있던방 세팅
		public void setRoom() {
			for (int i = 0; i < arr_room.size(); i++) {
				String roomName = arr_room.get(i).getRoomName();
				sendClientMsg(soc, "OTHER_ROOM@" + roomName);
			}

			// broadcast("UPDATE_ROOMLIST@ ");
		}

		public void getClientMsg(String str) {
			// client에서 'command@msg' 형식의 메시지를 전달받음

			strToken = new StringTokenizer(str, "@");
			String command = strToken.nextToken();
			String msg = strToken.nextToken();

			try {

				if (command.equals("CREATE_ROOM")) {
					// 중복되는 방제 있는지 체크
					for (int i = 0; i < arr_room.size(); i++) {
						RoomInfo roomInfo = arr_room.get(i);
						if (roomInfo.getRoomName().equals(msg)) {
							sendClientMsg(soc, "ROOM_CREATE_FAIL@ ");
							roomState = false;
							break;
						}
					}

					// 중복되는 방 없을때
					if (roomState) {
						RoomInfo roomInfo = new RoomInfo(msg);
						arr_room.add(roomInfo);
						broadcast("NEW_ROOM@" + msg);
						broadcast("UPDATE_ROOMLIST@ ");
					} else {
						roomState = true;
					}

				} else if (command.equals("LOGIN")) {
					id = msg;
					broadcast("NEW_USER@" + id);
					broadcast("UPDATE_USERLIST@ ");
					broadcast("UPDATE_ROOMLIST@ ");
				} else if (command.equals("LOGOUT")) {

					id = msg; // 로그아웃할 사용자 ID
					broadcast("REMOVE_USER@" + id);
					broadcast("UPDATE_USERLIST@ ");

					// 소켓 닫음
					for (int i = 0; i < arr_user.size(); i++) {
						UserInfo user = arr_user.get(i);
						if (user.getId().equals(id)) {
							user.getSoc().close();
							user.getFileSocket().fileSoc.close();
							break;
						}
					}

					arr_user.remove(this); // 유저 삭제

					for (int i = 0; i < arr_room.size(); i++) {
						RoomInfo room = arr_room.get(i);
						if (room.getRoomName().equals(currentRoom)) {
							currentRoom = null;
							room.removeUser(this);
							room.broadcastRoom("REMOVE_ROOM_USER@" + id);
							room.broadcastRoom("UPDATE_ROOM_USERLIST@ ");
							break;
						}
					}

				} else if (command.equals("JOIN_ROOM")) {

					for (int i = 0; i < arr_room.size(); i++) {
						RoomInfo room = arr_room.get(i);
						if (room.getRoomName().equals(msg)) {
							currentRoom = msg;
							room.setRoomUser(soc); // 기존에 방에 있던 사용자 세팅
							room.addUser(this); // 방에 사용자 추가
							sendClientMsg(soc, "JOIN_ROOM@" + msg);
							room.broadcastRoom("NEW_ROOM_USER@" + id);
							room.broadcastRoom("UPDATE_ROOM_USERLIST@ ");
							break;
						}
					}

				} else if (command.equals("EXIT_ROOM")) {

					for (int i = 0; i < arr_room.size(); i++) {
						RoomInfo room = arr_room.get(i);
						if (room.getRoomName().equals(msg)) {
							room.removeUser(this);
							currentRoom = null;
							sendClientMsg(soc, "EXIT_ROOM@" + msg);
							room.broadcastRoom("REMOVE_ROOM_USER@" + id);
							room.broadcastRoom("UPDATE_ROOM_USERLIST@ ");
							break;
						}
					}

				} else if (command.equals("SEND_MESSAGE")) { // 현재 방에 채팅 전송
					String txtContent = strToken.nextToken(); // 전송할 메시지

					for (int i = 0; i < arr_room.size(); i++) {
						RoomInfo room = arr_room.get(i);
						if (room.getRoomName().equals(msg)) {
							room.broadcastRoom("CHAT@" + id + "@" + txtContent);
						}
					}

				} else if (command.equals("NOTE")) {
					String receiver = strToken.nextToken(); // 받는 사람
					String content = strToken.nextToken(); // 쪽지 내용

					for (int i = 0; i < arr_user.size(); i++) {
						UserInfo user = arr_user.get(i);
						if (user.getId().equals(receiver)) {
							sendClientMsg(user.getSoc(), "NOTE@" + id + "@" + content); // 보내는사람@쪽지내용
							break;
						}
					}
				} else if (command.equals("FILE")) {
					String spt[] = msg.split("#");
					new FileSendGroup(fileSocket.fileDos, spt[0], spt[1], spt[2]).start();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void run() {

			while (true) {
				try {
					dis = new DataInputStream(soc.getInputStream());
					getClientMsg(dis.readUTF());
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("강제 종료");

					for (int i = 0; i < arr_room.size(); i++) {
						RoomInfo room = arr_room.get(i);

						if (room.getRoomName().equals(currentRoom)) {
							room.removeUser(this);
							room.broadcastRoom("REMOVE_ROOM_USER@" + id);
							room.broadcastRoom("UPDATE_ROOM_USERLIST@" + id);
							break;
						}
					}

					arr_user.remove(this);
					broadcast("REMOVE_USER@" + id);
					broadcast("UPDATE_USERLIST@" + id);

					try {
						soc.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					break;
				}
			}

		}

	}

	/**
	 * 채팅방 정보 클래스
	 * 
	 * @author wjddp
	 *
	 */
	class RoomInfo {
		private String roomName; // 방 이름
		private ArrayList<UserInfo> arr_userList = new ArrayList<>(); // 방에 있는 사용자 목록

		public RoomInfo(String roomName) {
			this.roomName = roomName;
		}

		//방에 사용자 추가 
		public void addUser(UserInfo userInfo) {
			arr_userList.add(userInfo);
		}

		//방에 사용자 제거 
		public void removeUser(UserInfo userInfo) {
			arr_userList.remove(userInfo);

			//마지
			if (arr_userList.size() == 0) { 
				broadcast("REMOVE_ROOM@" + roomName);
				broadcast("UPDATE_ROOMLIST@ ");
				arr_room.remove(this);
			}

			if (arr_room.size() == 0) {
				broadcast("REMOVE_ROOM_ALL@ ");
			}
		}

		public String getRoomName() {
			return roomName;
		}

		public void setRoomName(String roomName) {
			this.roomName = roomName;
		}

		public ArrayList<UserInfo> getArr_userList() {
			return arr_userList;
		}

		// 방에 메시지 브로드캐스트
		public void broadcastRoom(String msg) {
			System.out.println("broadcast room :" + msg);
			for (UserInfo i : arr_userList) {
				sendClientMsg(i.getSoc(), msg);
			}

		}

		public void setRoomUser(Socket soc) {

			for (UserInfo i : arr_userList) {
				String id = i.getId();
				sendClientMsg(soc, "OTHER_ROOM_USER@" + id);
			}

		}
	}

	class FileSocket implements Runnable {

		Socket fileSoc;
		private DataInputStream fileDis;
		private DataOutputStream fileDos;

		public FileSocket(Socket fileSoc) {
			this.fileSoc = fileSoc;
			try {
				fileDis = new DataInputStream(fileSoc.getInputStream());
				fileDos = new DataOutputStream(fileSoc.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {

			while (true) {
				try {
					fileDis = new DataInputStream(fileSoc.getInputStream());
					getClientFileMsg(fileDis.readUTF());

				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("강제 종료");

					try {
						fileSoc.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					break;
				}
			}
		}

		public void getClientFileMsg(String str) {

			String id;
			String[] splita = new String[2];
			splita = str.split("@");
			String command = splita[0];
			String msg = splita[1];

			if (command.equals("FILE")) {
				try {
					fileWrite();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (command.equals("FILELOGIN")) {
				id = msg;
				for (int i = 0; i < arr_user.size(); i++) {
					if (arr_user.get(i).id.equals(id)) {
						arr_user.get(i).setFileSocket(this);
						break;
					}
				}
			}
		}

		public void fileWrite() throws IOException {

			String result;

			String filePath = "C:/testdata";
			ArrayList<String> users = new ArrayList<>();

			System.out.println("서버 : 파일 수신 작업을 시작");
			String fileNm = fileDis.readUTF();
			String me = fileDis.readUTF(); // 자기자신 소켓
			long fileLength = fileDis.readLong();
			System.out.println("서버 : 파일명 " + fileNm + "을 전송받았습니다. " + me);

			users.clear();
			String roomName = fileDis.readUTF();
			for (RoomInfo a : arr_room) {
				if (a.getRoomName().equals(roomName)) {
					for (UserInfo b : a.arr_userList) {
						users.add(b.id);
					}
				}
			}

			System.out.println("서버 : 그룹 멤버 수신 완료");

			// 파일을 생성하고 파일에 대한 출력 스트림 생성
			File file = new File(filePath + "/" + fileNm);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] readBuffer = new byte[1024];

			int sum = 0;
			int read;
			long remain = fileLength;

			while ((read = fileDis.read(readBuffer, 0, remain >= 1024 ? 1024 : (int) remain)) > 0) {
				sum += read;
				remain -= read;
				ByteBuffer bb = ByteBuffer.wrap(readBuffer, 0, read);
				FileChannel fcout = fos.getChannel();
				fcout.write(bb);
				if (sum == fileLength)
					break;
			}

			System.out.println("서버 : 파일 수신 작업을 완료");
			System.out.println("서버 : 받은 파일의 사이즈 : " + file.length());

			fos.close();

			// 인원마다 전송
			for (String user : users) {
				for (UserInfo userInfo : arr_user) {
					if (user.equals(userInfo.getId())) {
						if (userInfo.getId().equals(me))
							continue;
						dos = new DataOutputStream(userInfo.getSoc().getOutputStream());
						dos.writeUTF("FILE@" + fileNm + "#" + filePath + "#" + userInfo.getId());

					}
				}
			}

		}
	}

}

class FileSendGroup extends Thread {

	DataOutputStream fileDos;
	String fileNm, filePath, userName;

	public DataOutputStream getFileDos() {
		return fileDos;
	}

	public void setFileDos(DataOutputStream fileDos) {
		this.fileDos = fileDos;
	}

	public String getFileNm() {
		return fileNm;
	}

	public void setFileNm(String fileNm) {
		this.fileNm = fileNm;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public FileSendGroup(DataOutputStream fileDos, String fileNm, String filePath, String userName) {
		this.fileDos = fileDos;
		this.fileNm = fileNm;
		this.filePath = filePath;
		this.userName = userName;
	}

	@Override
	public void run() {
		try {
			// 파일을 읽어서 서버에 전송
			fileDos.writeUTF("FILESEND@ ");
			File file = new File(filePath + "\\" + fileNm);
			// fileDos = new
			// DataOutputStream(user.getFileSocket().fileSoc.getOutputStream());
			fileDos.writeUTF(fileNm);
			fileDos.flush();
			fileDos.writeLong(file.length());
			System.out.println("파일 이름(" + fileNm + ")을 전송하였습니다.");
			fileDos.flush();

			FileInputStream fis = new FileInputStream(file);
			FileChannel fcin = fis.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate((int) file.length());

			int read;
			while ((read = fcin.read(buffer)) > 0) {
				buffer.flip(); // String
				fileDos.write(buffer.array());
			}

			fileDos.flush();
			fis.close();
			System.out.println(userName + "(사용자에게) : 전송 성공");
			System.out.println("file size : " + file.length());

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("fileRead : 전송 실패");
		}
	}

}
