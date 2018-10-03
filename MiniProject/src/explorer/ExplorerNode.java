package explorer;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class ExplorerNode extends DefaultMutableTreeNode implements TreeNode {
	private File file;
	private Icon icon;

	public ExplorerNode(File file) {
		super();
		if (file == null) {
			rootLevel++;
		} else {
			this.file = file;
			this.icon = FileSystemView.getFileSystemView().getSystemIcon(this.file);			
		}
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	@Override
	public boolean equals(Object obj) {
		ExplorerNode en = (ExplorerNode) obj;
		
		if(this.getFile() == null && en.getFile() == null) {
			return true;
		}
		
		if (this.getFile() == null || en.getFile() == null)
			return false;

		return this.getFile().getAbsolutePath().equals(en.getFile().getAbsolutePath());
	}

	@Override
	public ExplorerNode getLastChild() {
		return (ExplorerNode) super.getLastChild();
	}

	@Override
	public ExplorerNode getFirstChild() {
		return (ExplorerNode) super.getFirstChild();
	}

	@Override
	public boolean isLeaf() {
		return file == null ? false : file.isFile();
	}

	public String getFilePath() {
		String result;
		if (file == null) {
			result = this.getFileName();
		} else {
			result = file.toString();
		}
		return result;
	}
	
	public String getFileName() {
		return file == null ? "내 컴퓨터" : file.getName();
	}

	public static <TreeModel extends DefaultTreeModel> ExplorerNode findNodeByFile(TreeModel dtm, ExplorerNode rootNode, File file) {
		return findNodeByPath(dtm, rootNode, file.getPath());
	}

	private static int rootLevel = 0;

	public static <TreeModel extends DefaultTreeModel> ExplorerNode findNodeByPath(TreeModel dtm,ExplorerNode rootNode, String filePath) {
		String[] path = filePath.split("[/\\\\]");

		if (path.length <= rootLevel)
			return rootNode;

		addChildIfNoChild(rootNode);
		dtm.nodeChanged(rootNode);

		Enumeration<ExplorerNode> e = rootNode.children();
		while(e.hasMoreElements()) {
			ExplorerNode child = e.nextElement();
			
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j <= child.getLevel() - rootLevel; j++) {
				sb.append(path[j] + File.separatorChar);
			}
			String findPath = Arrays.toString(sb.toString().split("[/\\\\]"));
			String targetPath = Arrays.toString(child.getFilePath().split("[/\\\\]"));

			if (findPath.equals(targetPath)) {

				if (path.length - 1 == child.getLevel() - rootLevel) {
					return child;
				} else {
					return findNodeByPath(dtm, child, filePath);
				}
			}
		}

		return null;
	}

	public static void addChildIfNoChild(ExplorerNode rootNode) {
		if (rootNode != null && !rootNode.isLeaf() && rootNode.getChildCount() == 0 && rootNode.getFile() != null
				&& rootNode.getFile().listFiles() != null) {
			List<File> list = new ArrayList<>();
			for(int i = 0 ; i< rootNode.getChildCount(); i++) {
				ExplorerNode en = (ExplorerNode) rootNode.getChildAt(i);
				list.add(en.getFile());
			}
			
			for (File childFile : rootNode.getFile().listFiles()) {
				if(list.contains(childFile)) {
					continue;
				} else {
					ExplorerNode newChild = new ExplorerNode(childFile);
					rootNode.add(newChild);
				}
			}

		}

	}
	
	public static void openNodeFile(Component c ,ExplorerNode node) {
		try {
			Desktop desktop = Desktop.getDesktop();
			desktop.open(node.getFile());
		} catch (IOException | NullPointerException e) {
			JOptionPane.showMessageDialog(c, e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
		}
	}

}
