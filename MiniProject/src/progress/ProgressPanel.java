/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package progress;

/**
 *
 * @author S401-28
 */
public class ProgressPanel extends javax.swing.JPanel {

    /**
     * Creates new form ProgressPanel
     */
    public ProgressPanel() {
        initComponents();
    }
    
    public void setFileName(String fileName) {
        this.jLabel_FileName.setText("["+fileName+"]");
    }
    
    public void setMessage(String msg) {
        this.jLabel_Message.setText(msg);
    }
    
    public void setProgressMax(int max) {
        ProgressBarPanel pbp = (ProgressBarPanel) this.jPanel_ProgressBar;
        pbp.setMax(max);
        this.jLabel_Max.setText(Integer.toString(max));
    }
    
    public void setCurrentProgress(int x) {
        ProgressBarPanel pbp = (ProgressBarPanel) this.jPanel_ProgressBar;
        pbp.setProgress(x);
        this.jLabel_CurrentProgress.setText(Integer.toString(x));
    }
    
    public void notifyProgressChanged() {
        ProgressBarPanel pbp = (ProgressBarPanel) this.jPanel_ProgressBar;
        pbp.notifyProgressChanged();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabel_FileName = new javax.swing.JLabel();
        jLabel_Message = new javax.swing.JLabel();
        jPanel_ProgressBar = new ProgressBarPanel();
        jLabel_Max = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel_CurrentProgress = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(400, 150));

        jLabel_FileName.setFont(new java.awt.Font("맑은 고딕", 0, 12)); // NOI18N
        jLabel_FileName.setText("[file name]");

        jLabel_Message.setFont(new java.awt.Font("맑은 고딕", 0, 12)); // NOI18N
        jLabel_Message.setText("파일 다운로드를 진행 중 입니다.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_FileName)
                    .addComponent(jLabel_Message))
                .addContainerGap(189, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel_FileName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel_Message))
        );

        jPanel_ProgressBar.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel_ProgressBarLayout = new javax.swing.GroupLayout(jPanel_ProgressBar);
        jPanel_ProgressBar.setLayout(jPanel_ProgressBarLayout);
        jPanel_ProgressBarLayout.setHorizontalGroup(
            jPanel_ProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel_ProgressBarLayout.setVerticalGroup(
            jPanel_ProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        jLabel_Max.setText("jLabel1");

        jLabel1.setText("/");

        jLabel_CurrentProgress.setText("jLabel2");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel_ProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel_CurrentProgress)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel_Max)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel_ProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_Max)
                    .addComponent(jLabel1)
                    .addComponent(jLabel_CurrentProgress))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel_CurrentProgress;
    private javax.swing.JLabel jLabel_FileName;
    private javax.swing.JLabel jLabel_Max;
    private javax.swing.JLabel jLabel_Message;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel_ProgressBar;
    // End of variables declaration//GEN-END:variables
}