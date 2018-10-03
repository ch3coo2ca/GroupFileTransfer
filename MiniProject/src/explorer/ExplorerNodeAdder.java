package explorer;

import java.io.File;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.swing.tree.DefaultTreeModel;

class ExplorerNodeAdder implements Runnable {
	private static ExplorerNodeAdderFactory explorerNodeAdderFactory = new ExplorerNodeAdderFactory();
	private static ExecutorService executor;
	private static ExplorerNodeAdder instance;
	private DefaultTreeModel dtm;
	private ExplorerNode rootNode;

	private ExplorerNodeAdder(DefaultTreeModel dtm, ExplorerNode root) {
		this.dtm = dtm;
		this.rootNode = root;
	}

	public static ExplorerNodeAdder initExcutorInstance(DefaultTreeModel dtm, ExplorerNode rootNode) {
		if (instance != null) {
			instance.setRootNode(rootNode);
			return instance;
		}
		return instance = new ExplorerNodeAdder(dtm, rootNode);
	}

	@Override
	public void run() {
		recursiveNodeAdd(rootNode);
	}

	private void recursiveNodeAdd(ExplorerNode parentNode) {
		File currentFile = parentNode.getFile();
		// System.out.println(currentFile.getPath());
		if (currentFile.isDirectory()) {
			File[] list = currentFile.listFiles();
			if (list == null)
				return;
			synchronized (parentNode) {
				for (File childFile : list) {
					ExplorerNode newChild = new ExplorerNode(childFile);
					parentNode.add(newChild);
					explorerNodeAdderFactory.setPriority(newChild.getLevel());
					executor.submit(new ExplorerNodeAdder(dtm,newChild));
				}	
			}

		}
		dtm.nodeStructureChanged(parentNode);
	}
	
	public void startExplorerFileTree() {
		if (executor == null)
			executor = Executors.newFixedThreadPool(100, explorerNodeAdderFactory);
		executor.submit(instance);
	}

	public void stopExplorFileTree() {
		if (executor != null)
			executor.shutdownNow();
	}
	
	public void startExplorerFileTreeNonThead() {
		ExplorerNode.addChildIfNoChild(rootNode);
	}
	
	private void setRootNode(ExplorerNode rootNode) {
		this.rootNode = rootNode;
	}

	private static class ExplorerNodeAdderFactory implements ThreadFactory {
		int priority = Thread.NORM_PRIORITY;

		@Override
		public Thread newThread(Runnable r) {
			Thread task = new Thread(r);
			task.setPriority(priority);
			return task;
		}

		public void setPriority(int priority) {
			this.priority = priority % 10 + 1;
		}

	}
}
