/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package progress;

import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 *
 * @author S401-28
 */
public class testmain {
    public static void main(String[] args) {
    	
        ProgressPanel pp = new ProgressPanel();
        pp.setVisible(false);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // 진행사항 최대치 설정
                pp.setProgressMax(1000);
                
                // 제목 설정
                pp.setFileName("메롱.txt");
                
                // 메시지 설정
                //pp.setMessage(msg);

                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(pp);
                frame.pack();
                frame.setVisible(true);
                              
            }
        });
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0; i <= 1000; i++) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(testmain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // 진행 정도 업데이트
                    pp.setCurrentProgress(i);
                    
                    // 진행 사항 업데이트
                    pp.notifyProgressChanged();
                }
            }
        }).start();
        
    }
}
