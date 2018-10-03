package explorer;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.tree.DefaultMutableTreeNode;

public class UploadListCellRenderer extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		ExplorerNode obj = (ExplorerNode) value;
		this.setText(obj.getFile().getName());
		this.setIcon(obj.getIcon());
		
		return this; 
	}
}
