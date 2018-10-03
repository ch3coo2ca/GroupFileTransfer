package explorer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

public class ExplorerTreeCellRenderer extends DefaultTreeCellRenderer {

	@Override
	public Color getBackgroundNonSelectionColor() {
		return (null);
	}

	@Override
	public Color getBackgroundSelectionColor() {
		// 3399ff
		return new Color(0x33, 0x99, 0xff);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		ExplorerNode obj = (ExplorerNode) value;

		setText(obj.getFileName().equals("") ? obj.getFilePath().replaceFirst("[/\\\\]", "") : obj.getFileName());
		setIcon(obj.getIcon());
		if (selected) {
			setForeground(Color.white);
		}

		return this;
	}

}
