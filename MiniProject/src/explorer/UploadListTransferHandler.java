package explorer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class UploadListTransferHandler extends TransferHandler {
	private JList<ExplorerNode> list;
	private JTree tree;

	public UploadListTransferHandler(JList<ExplorerNode> list, JTree tree) {
		super();
		this.list = list;
		this.tree = tree;
	}

	@Override
	public boolean canImport(TransferSupport support) {
		if (!support.isDrop()) {
			return false;
		}

		return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
				|| support.isDataFlavorSupported(ExplorerTreeTransferHandler.FLAVOR);
	}

	@Override
	public boolean importData(TransferSupport support) {

		if (!canImport(support)) {
			return false;
		}

		Transferable transferable = support.getTransferable();

		try {
			if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				List<File> fileList = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
				return fileToList(support, fileList);

			} else if (transferable.isDataFlavorSupported(ExplorerTreeTransferHandler.FLAVOR)) {
				List<ExplorerNode> nodeList = (List<ExplorerNode>) transferable
						.getTransferData(ExplorerTreeTransferHandler.FLAVOR);
				return treeToList(support, nodeList);
			}

		} catch (UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
		}

		return super.importData(support);
	}

	private boolean treeToList(TransferSupport support, List<ExplorerNode> nodeList) {
		DefaultListModel<ExplorerNode> dlm = (DefaultListModel<ExplorerNode>) list.getModel();
		for (ExplorerNode en : nodeList) {
			if (ExplorerPanel.checkFileSize(list.getParent(), en.getFile())) {
				if (dlm.contains(en)) {
					continue;
				} else {
					dlm.addElement(en);
				}
			} else {
				return false;
			}

		}
		return true;
	}

	private boolean fileToList(TransferSupport support, List<File> fileList) {
		DefaultListModel<ExplorerNode> dlm = (DefaultListModel<ExplorerNode>) list.getModel();

		for (File file : fileList) {
			DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
			ExplorerNode en = ExplorerNode.findNodeByFile(dtm, (ExplorerNode) dtm.getRoot(), file);
			if (en == null)
				en = new ExplorerNode(file);

			if (ExplorerPanel.checkFileSize(list.getParent(), file)) {
				if (dlm.contains(en)) {
					continue;
				} else {
					dlm.addElement(en);
				}
			} else {
				return false;
			}

		}

		list.setModel(dlm);
		return true;
	}

}
