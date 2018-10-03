package login;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class SignInForm extends JFrame implements ActionListener{
	
	UserDAO userDAO;
	UserDTO userDTO;
	
	private JFrame superFrame;
	
	private JLabel lblTitle, lblId, lblPw, lblCheckPw, lblName;
	private JTextField txtId, txtName;
	private JPasswordField txtPw, txtCheckPw;
	
	private JButton btnSignIn, btnExit, btnCheck, btnCancel;
	
	private JPanel mJpId, jpId, jpPw, jpBtn, jpNorth, jpBorder, jpCenter, jpWindow, jpCheck, jpName;
	private JPanel jpGridBag;
	private Image img[] = new Image[3];

	private boolean overlabId = true;
	
	public void init() {
		initImage();
		userDAO = new UserDAO();
		
		jpBorder = new JPanel(new BorderLayout());
//		jpBorder.setBackground(Color.CYAN);
		
		/*
		 * 최상단 윈도우 패널
		 */
		jpWindow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		jpWindow.setOpaque(false);
		jpWindow.add(btnExit = new JButton());
		btnExit.setIcon(new ImageIcon(img[0]));
		btnExit.setOpaque(false);	btnExit.setBorderPainted(false);	btnExit.setContentAreaFilled(false);
		btnExit.setFocusPainted(false);	   btnExit.addActionListener(this);
		btnExit.addActionListener(this);
		/*
		 * 상탄 타이틀 바
		 */
		jpNorth = new JPanel();
		jpNorth.add(lblTitle = new JLabel("Member Sign In", JLabel.CENTER));
		lblTitle.setFont(new Font("", Font.ITALIC, 32));
		lblTitle.setForeground(Color.WHITE);
		jpNorth.setOpaque(false);
		/*
		 * 로그인 뷰
		 */
		jpGridBag = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;		// 가로로 정렬
		
		// id
		JPanel jp1 = new JPanel(new GridBagLayout());
		jp1.setOpaque(false);
		gbAdd(c, 0, 0, 0.1);
		c.insets = new Insets(0, 70, 0, 0);
		jp1.add(lblId = new JLabel("userID"), c);		
		gbAdd(c, 0, 1, 0.7);
		c.insets = new Insets(0, 5, 0, 3);
		jp1.add(txtId = new JTextField(), c);
		lblId.setFont(new Font("", Font.BOLD, 16));
		txtId.setFont(new Font("", Font.BOLD, 16));
		gbAdd(c, 0, 2, 0.2);
		c.insets = new Insets(0, 10, 0, 30);
		jp1.add(btnCheck = new JButton("중복확인"), c);
		btnCheck.setBackground(new Color(204, 255, 204));	btnCheck.setForeground(Color.black);
		btnCheck.addActionListener(this);
		// pw
		gbAdd(c, 1, 0, 0.1);
		c.insets = new Insets(10, 70, 0, 0);
		jp1.add(lblPw = new JLabel("userPW"), c);		
		gbAdd(c, 1, 1, 0.9);
		c.insets = new Insets(10, 5, 0, 2);
		jp1.add(txtPw = new JPasswordField(), c);
		lblPw.setFont(new Font("", Font.BOLD, 16));
		txtPw.setFont(new Font("", Font.BOLD, 16));
		//check Pw
		gbAdd(c, 2, 0, 0.1);
		c.insets = new Insets(10, 70, 0, 0);
		jp1.add(lblCheckPw = new JLabel("CheckPW"), c);		
		gbAdd(c, 2, 1, 0.9);
		c.insets = new Insets(10, 5, 0, 2);
		jp1.add(txtCheckPw = new JPasswordField(), c);
		lblCheckPw.setFont(new Font("", Font.BOLD, 16));
		txtCheckPw.setFont(new Font("", Font.BOLD, 16));
		
		// Name
		gbAdd(c, 3, 0, 0.1);
		c.insets = new Insets(10, 70, 0, 0);
		jp1.add(lblName = new JLabel("userName"), c);		
		gbAdd(c, 3, 1, 0.9);
		c.insets = new Insets(10, 5, 0, 3);
		jp1.add(txtName = new JTextField(), c);
		lblName.setFont(new Font("", Font.BOLD, 16));
		txtName.setFont(new Font("", Font.BOLD, 16));
		
		
		// Button
		jpBtn = new JPanel(new GridBagLayout());
		jpBtn.setOpaque(false);
		gbAdd(c, 0, 0, 0.5);
		c.insets = new Insets(0, 75, 20, 25);
		jpBtn.add(btnSignIn = new JButton("Sign In"), c);
		btnSignIn.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
		btnSignIn.setBackground(new Color(179, 236, 255));   btnSignIn.setForeground(Color.black);
		btnSignIn.addActionListener(this);
		
		gbAdd(c, 0, 1, 0.5);
		c.insets = new Insets(0, 0, 20, 50);
		jpBtn.add(btnCancel = new JButton("Cancel"), c);
		btnCancel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
		btnCancel.setBackground(new Color(255, 204, 203));	btnCancel.setForeground(Color.black);
		btnCancel.addActionListener(this);
		
		
		jpCenter = new JPanel(new BorderLayout());
		jpCenter.add("North", jpNorth);
		jpCenter.add("Center", jp1);
		jpCenter.add("South", jpBtn);
		jpCenter.setBackground(new Color(173,216,230));

		jpBorder.add("North", jpWindow);	// 창 닫기 .. 패널
		jpBorder.add("Center", jpCenter);
		add(jpBorder);
	}
	
	public SignInForm(JFrame frame) {
		superFrame = frame;
		init();
		setUndecorated(true);
		setTitle("Sign In!");
		setVisible(true);
		setResizable(false);
		setSize(500, 350);

		
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public boolean idFormCheck() {
		if(txtId.getText().toString().equals("")) {
			JOptionPane.showMessageDialog(this, "아이디를 입력해주세요.");
			return false;
		}
		return true;
	}
	
	public boolean isOverlabId() {
		return overlabId;
	}
	
	public boolean formCheck() {
		if(isOverlabId()) {
			// 중복된 아이디 체크 ?
			JOptionPane.showMessageDialog(this, "아이디 중복을 체크해주세요.");
			return false;
		}
		
		if(txtName.getText().toString().equals("")||txtId.getText().toString().equals("")
				||txtPw.getText().toString().equals("")) {
			JOptionPane.showMessageDialog(this, "모두 입력해주세요.");
			return false;
		}
		return true;
	}
	
	// 비밀번호 확인, JYK
	public boolean pwFormCheck() {		
		if(! (txtPw.getText().toString().equals(txtCheckPw.getText().toString())) ) {
			// 비번1, 비번2가 일치하지 않으면
			JOptionPane.showMessageDialog(this, "비밀번호가 일치하지 않습니다.");
			return false;
		}else
			return true;
	}
	
	public void initImage() {
		ImageIcon icon[] = new ImageIcon[3];
		// index : 0, 닫기 
		icon[0] = new ImageIcon("src/cancel.png");
		img[0] = icon[0].getImage();
		img[0] = img[0].getScaledInstance(30, 30, Image.SCALE_SMOOTH);
		// index : 1, member Image
		icon[1] = new ImageIcon("src/member.png");
		img[1] = icon[1].getImage();
		img[1] = img[1].getScaledInstance(25, 25, Image.SCALE_SMOOTH);
		// index : 2, password img
		icon[2] = new ImageIcon("src/password.png");
		img[2] = icon[2].getImage();
		img[2] = img[2].getScaledInstance(25, 25, Image.SCALE_SMOOTH);
	}

	public void initAllTxT() {
		txtCheckPw.setText("");
		txtId.setText("");
		txtName.setText("");
		txtPw.setText("");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// frame hide
		if(e.getSource() == btnExit || e.getSource() == btnCancel) {
			this.setVisible(false);
			initAllTxT();
		}
		
		String name = txtName.getText();
		String id = txtId.getText();
		String pw = txtPw.getText();
		//id Check
		if (e.getSource() == btnCheck) {
			if(idFormCheck() == true) {
				if (userDAO.idCheck(id)) {	// overlabId ( boolean, 중복이면 true, 사용가능이면 false )
					overlabId = true;
					JOptionPane.showMessageDialog(this, "중복되는 아이디입니다. 다시 입력해주세요.");
				} else {
					overlabId = false;
					JOptionPane.showMessageDialog(this, "사용 가능한 아이디입니다.");
				}
			}
		}
		
		if(e.getSource() == btnSignIn) {
			if(formCheck() && pwFormCheck()) {
				// 회원가입 시도			
				userDTO = new UserDTO(name, id, pw);
				if (userDAO.join(userDTO) == true) {
					JOptionPane.showMessageDialog(this, "회원가입에 성공하였습니다.");
					
					initAllTxT();
					setVisible(false);
					
					superFrame.setVisible(true);
				} else {
					JOptionPane.showMessageDialog(this, "err : 회원가입에 실패하였습니다.");
					superFrame.setVisible(true);
				}
			}
		}
	}

	public void gbAdd(GridBagConstraints c, int y, int x, double weight) {
		c.gridx = x;
		c.gridy = y;
		c.weightx = weight;
	}
}
