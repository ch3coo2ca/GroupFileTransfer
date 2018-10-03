package login;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
	Connection con = null;//오라클 연결
	PreparedStatement ps = null;//쿼리문 전송_ 동적쿼리
	ResultSet rs = null;
	
	public UserDAO() {
		//드라이버 찾기
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			//System.out.println("드라이버 찾기 성공");
		}catch(ClassNotFoundException e) {
			System.out.println("드라이버 찾기 실패");
		}
	}
	
	public Connection getConnection() {
		try {
			con = DriverManager.getConnection("jdbc:oracle:thin:@127.0.0.1:1521:xe", "javajava", "javajava");
			//System.out.println("오라클 연결 성공");
		}catch(SQLException e) {
			System.err.println("오라클 연결 실패");
			e.printStackTrace();
		}
		return con;
	}
		
	public boolean idCheck(String txtId) {//아이디 있는지도 확인, 아이디 중복 체크
		boolean state = false;
		try {
			getConnection();
			String sql = "select id from java_member";
			ps = con.prepareStatement(sql);
			
			rs = ps.executeQuery();
			
			while(rs.next()) {
				String id = rs.getString(1);
				
				if(txtId.equals(id)) {
					state = true;
					break;
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if( rs != null) {
					rs.close();
					rs = null;
				}
				if( ps != null) {
					ps.close();
					ps = null;
				}
				if(con != null) {
					con.close();
					con = null;
				}
			}catch(Exception e2) {
				e2.printStackTrace();
			}
		}
		return state;
	}
	
	
	public boolean login(String txtId, String txtPw) {//로그인
		boolean state = true;
		try {
			getConnection();
			String sql = "select id, pw from java_member";
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				String id = rs.getString(1);
				String pw = rs.getString(2);
				
				if(txtId.equals(id) && txtPw.equals(pw)) {
					return state = true;
				}
			}
			return state = false;
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(rs != null) {
					rs.close();
					rs = null;
				}
				if(ps != null) {
					ps.close();
					ps = null;
				}
				if(con != null) {
					con.close();
					con = null;
				}
			}catch(Exception e2) {
				e2.printStackTrace();
			}
		}
		return state;
	}
	
	
	public boolean join(UserDTO dto) {//회원가입
		try {
			getConnection();
			String sql = "insert into java_member values (?,?,?)";
			ps = con.prepareStatement(sql);
			
			ps.setString(1, dto.getName());
			ps.setString(2, dto.getID());
			ps.setString(3, dto.getPw());
			
			int result = ps.executeUpdate();
			if(result == 0) {
				return false;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			
		}finally {
			try {
				if(con != null) {
					con.close();
					con = null;
				}
				if(ps != null) {
					ps.close();
					ps = null;
				}
				
			}catch(Exception e2) {
				e2.printStackTrace();
			}
		}
		return true;
	}
}
