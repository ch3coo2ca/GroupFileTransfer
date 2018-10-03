package explorer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

public class ExplorerTreeTransferHandler extends TransferHandler {
	public static DataFlavor FLAVOR = new ActivationDataFlavor(ArrayList.class,
			DataFlavor.javaJVMLocalObjectMimeType, "List of ExplorerNode");

	@Override
	protected Transferable createTransferable(JComponent c) {
		System.out.println("createTransferable");
		JTree source = (JTree) c;
		TreePath[] paths = source.getSelectionPaths();
		ArrayList<ExplorerNode> explorerNodes = new ArrayList<>(); 
		
		for (int i = 0; i < paths.length; i++) {
			explorerNodes.add((ExplorerNode) paths[i].getLastPathComponent());
		}
		
		return new DataHandler(explorerNodes, FLAVOR.getMimeType());
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}

}
