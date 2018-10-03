package explorer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class ExplorerPanel extends JSplitPane {
	public static double MAX_FILE_SIZE = 1e+6 * 1000;
	private String rootPath;

	private JTree mTreeExplorer;
	private JTextField mCurrentPath;
	private JList<ExplorerNode> mUploadList;
	private JButton mBtnUploadListAdd;
	private JButton mBtnUploadListDel;
	private JButton mBtnUpload;

	private JPopupMenu mPopupMenuTreeNode;

	private JPanel mTopPanel;
	private JPanel mBottomPanel;

	public ExplorerPanel(String rootPath) {
		super(JSplitPane.VERTICAL_SPLIT);

		this.mTreeExplorer = new JTree();
		this.mUploadList = new JList<>();
		this.mBtnUploadListAdd = new JButton("+");
		this.mBtnUploadListDel = new JButton("-");
		this.mBtnUpload = new JButton("Upload");

		this.mPopupMenuTreeNode = new JPopupMenu();

		this.mTopPanel = new JPanel(new BorderLayout());
		this.mBottomPanel = new JPanel(new BorderLayout());
		this.setTopComponent(mTopPanel);
		this.setBottomComponent(mBottomPanel);
		this.rootPath = rootPath;

		initTreeExplorer();
		initUploadList();
		initButtons();
		initPopupMenu();
	}
	
	public ExplorerPanel() {
		this(null);
	}

	private void initPopupMenu() {
		JMenuItem menuOpen = new JMenuItem("열기");
		JMenuItem menuOpenParent = new JMenuItem("상위폴더 열기");
		JMenuItem menuAddToList = new JMenuItem("업로드 리스트에 추가");

		menuOpen.addActionListener(event -> {
			ExplorerNode en = (ExplorerNode) mTreeExplorer.getSelectionPath().getLastPathComponent();
			ExplorerNode.openNodeFile(ExplorerPanel.this, en);
		});
		
		menuOpenParent.addActionListener(event -> {
			ExplorerNode en = (ExplorerNode) mTreeExplorer.getSelectionPath().getLastPathComponent();
			ExplorerNode enp = (ExplorerNode) en.getParent();
			ExplorerNode.openNodeFile(ExplorerPanel.this, enp);
			
		});

		menuAddToList.addActionListener(event -> {
			onAddTreeToList();
		});

		mPopupMenuTreeNode.add(menuOpen);
		mPopupMenuTreeNode.add(menuOpenParent);
		mPopupMenuTreeNode.addSeparator();
		mPopupMenuTreeNode.add(menuAddToList);

	}

	private void initButtons() {
		mBtnUploadListAdd.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				onAddTreeToList();
			}
		});

		mBtnUploadListDel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				List<ExplorerNode> selected = mUploadList.getSelectedValuesList();
				DefaultListModel<ExplorerNode> dlm = (DefaultListModel<ExplorerNode>) mUploadList.getModel();

				for (ExplorerNode node : selected) {
					dlm.removeElement(node);
				}
				mUploadList.setModel(dlm);
			}
		});

		mBtnUpload.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<ExplorerNode> dlm = (DefaultListModel<ExplorerNode>) mUploadList.getModel();
				List<ExplorerNode> list = Collections.list(dlm.elements());

				if (onUploadButtonClickListener != null)
					onUploadButtonClickListener.onUpload(list);
			}
		});

		JPanel panel = new JPanel(new FlowLayout());
		panel.add(mBtnUploadListAdd);
		panel.add(mBtnUploadListDel);
		panel.add(mBtnUpload);
		this.mTopPanel.add(panel, BorderLayout.SOUTH);

	}

	protected void onAddTreeToList() {
		TreePath[] selectedFromExplorer = mTreeExplorer.getSelectionPaths();
		DefaultListModel<ExplorerNode> dlm = (DefaultListModel<ExplorerNode>) mUploadList.getModel();
		List<ExplorerNode> listElements = Collections.list(dlm.elements());

		for (TreePath tp : selectedFromExplorer) {
			ExplorerNode en = (ExplorerNode) tp.getLastPathComponent();
			File fileSizeCheck = en.getFile();

			if (checkFileSize(this, fileSizeCheck)) {
				if (!listElements.contains(en))
					dlm.addElement(en);
			} else {
				return;
			}

		}
		mUploadList.setModel(dlm);

	}

	protected void onDoubleClickTreeNode() {
		TreePath tp = mTreeExplorer.getSelectionModel().getSelectionPath();
		ExplorerNode selectedNode = (ExplorerNode) tp.getLastPathComponent();

		if (selectedNode.isLeaf()) {
			onAddTreeToList();
		}

	}

	protected void onRightClickTreeNode(MouseEvent e) {
		int row = mTreeExplorer.getClosestRowForLocation(e.getX(), e.getY());
		mTreeExplorer.setSelectionRow(row);
		mPopupMenuTreeNode.show(mTreeExplorer, e.getX(), e.getY());

	}

	private void initUploadList() {
		DefaultListModel<ExplorerNode> dlm = new DefaultListModel<>();
		mUploadList.setModel(dlm);
		mUploadList.setDropMode(DropMode.INSERT);
		mUploadList.setCellRenderer(new UploadListCellRenderer());
		mUploadList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mUploadList.setTransferHandler(new UploadListTransferHandler(mUploadList, mTreeExplorer));

		JScrollPane scrollList = new JScrollPane(this.mUploadList);
		scrollList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.mTopPanel.add(scrollList, BorderLayout.CENTER);
	}

	private void initTreeExplorer() {
		if (mTreeExplorer != null)
			mTreeExplorer.removeAll();
		
		ExplorerNode rootNode;
		DefaultTreeModel dtm;
		
		if(this.rootPath != null) {
			String rootPath = this.rootPath;
			
			rootNode = new ExplorerNode(new File(rootPath));
			ExplorerNode.addChildIfNoChild(rootNode);
			dtm = new DefaultTreeModel(rootNode, false);
		} else {
			File[] roots = File.listRoots();
			rootNode = new ExplorerNode(null);
			
			for(File root : roots) {
				if(root.getFreeSpace() > 0) {
					ExplorerNode driveRootNode = new ExplorerNode(root);
					ExplorerNode.addChildIfNoChild(driveRootNode);
					rootNode.add(driveRootNode);
				}
			}
			
			dtm = new DefaultTreeModel(rootNode);
//			mTreeExplorer.setRootVisible(false);
		}
		
		mCurrentPath = new JTextField(rootNode.getFileName());
		
		mTreeExplorer.setDragEnabled(true);
		mTreeExplorer.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		mTreeExplorer.setModel(dtm);
		mTreeExplorer.setCellRenderer(new ExplorerTreeCellRenderer());
		mTreeExplorer.setTransferHandler(new ExplorerTreeTransferHandler());

		JScrollPane scrollExplorer = new JScrollPane(this.mTreeExplorer);
		scrollExplorer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollExplorer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.mBottomPanel.add(mCurrentPath, BorderLayout.NORTH);
		this.mBottomPanel.add(scrollExplorer, BorderLayout.CENTER);

		mTreeExplorer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);

				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && !e.isConsumed()) {
					e.consume();
					// handle double click event.
					onDoubleClickTreeNode();
					
				} else if (MouseEvent.BUTTON3 == e.getButton()) {
					e.consume();
					onRightClickTreeNode(e);
				}
			}

		});

		mTreeExplorer.addTreeWillExpandListener(new TreeWillExpandListener() {

			@Override
			public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
				ExplorerNode node = (ExplorerNode) event.getPath().getLastPathComponent();
				ExplorerNode.addChildIfNoChild(node);
				dtm.nodeStructureChanged(node);
				updatePath(event);
			}

			@Override
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
				updatePath(event);
			}

			private void updatePath(TreeExpansionEvent event) {
				ExplorerNode select = (ExplorerNode) event.getPath().getLastPathComponent();

				if (!select.isLeaf())
					mCurrentPath.setText(select.getFilePath());
			}
		});

		mCurrentPath.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_ENTER) {
					String path = mCurrentPath.getText();
					expandFilePath(rootNode, path);
				}
				super.keyTyped(e);
			}
		});
		
	}
	
	public void expandFilePath(ExplorerNode rootNode, String path) {
		DefaultTreeModel dtm = (DefaultTreeModel) mTreeExplorer.getModel();
		ExplorerNode en = ExplorerNode.findNodeByPath(dtm, rootNode, path);
		
		TreePath tp = new TreePath(en.getPath());
		
		try {
			mTreeExplorer.expandPath(tp);
			mTreeExplorer.scrollPathToVisible(new TreePath(en.getLastChild().getPath()));
		} catch(NoSuchElementException e1) {
			mTreeExplorer.scrollPathToVisible(tp);
		}
		
		mTreeExplorer.setSelectionPath(tp);
	}

	public interface OnExplorerPathNeed {
		String getRootPath();
	}

	public String getDefaultPath() {
		return "C:/";
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
		initTreeExplorer();
	}

	private OnUploadButtonClickListener onUploadButtonClickListener = null;

	public interface OnUploadButtonClickListener {
		void onUpload(List<ExplorerNode> list);
	}

	public void setOnUploadButtonClickListener(OnUploadButtonClickListener l) {
		this.onUploadButtonClickListener = l;
	}

	private boolean painted;

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (!painted) {
			painted = true;
			this.setDividerLocation(0.5);
		}
	}

	public static boolean checkFileSize(Component c, File file) {
		try {
			long size = 0;
			if (file.isDirectory()) {
				size = Files.walk(Paths.get(file.getPath())).mapToLong(p -> p.toFile().length()).sum();
			} else {
				size = file.length();
			}

			if (size > MAX_FILE_SIZE)
				throw new FileSystemException("size is too big over " + MAX_FILE_SIZE + " bytes");
			return true;

		} catch (FileSystemException e) {
			JOptionPane.showMessageDialog(c, e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
		} catch (UncheckedIOException e) {
			JOptionPane.showMessageDialog(c, e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
