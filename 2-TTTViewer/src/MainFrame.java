import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;


public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	JLabel[][] labels = new JLabel[3][3];
	
	NXTComm nxtComm;
	
	InputStream istream;
	
	Timer timer;
	
	public MainFrame() {
		setLayout(new GridLayout(3,3));
		
		for(int r = 0; r < 3; r++) {
			for(int c = 0; c < 3; c++) {
				labels[r][c] = new JLabel("-", SwingConstants.CENTER);
				labels[r][c].setBorder(BorderFactory.createLineBorder(Color.black, 1));
				add(labels[r][c]);
			}
		}
		pack();
		setSize(400, 400);
		
		try {
			nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.USB);
			NXTInfo[] devices = nxtComm.search(null);
			if(devices.length == 0)
			{
				JOptionPane.showMessageDialog(this, "No NXTs found.");
				return;
			}
			if(!nxtComm.open(devices[0])) {
				JOptionPane.showMessageDialog(this, "Could not connect to NXT.");
				return;
			}
			istream = nxtComm.getInputStream();
		} catch (NXTCommException e) {
			e.printStackTrace();
		}
		
		timer = new Timer(10, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					byte[] data = new byte[100];
					int numbytes = istream.read(data);
					System.out.println(numbytes + " bytes received.");
					if(numbytes > 0) {
						boolean started = false;
						int elemCount = 0;
						for(int i = 0; i < numbytes; i++) {
							if(!started) {
								if(data[i] == '$')
									started = true;
							} else {
								labels[elemCount/3][elemCount%3].setText(new String(new byte[] {data[i]}));
								elemCount++;
								if(elemCount > 8)
									started = false;
							}
						}
					} else if(numbytes == -1) {
						System.err.println("NXT Disconnected?");
						timer.stop();
					}
				} catch(Exception exc) {
					exc.printStackTrace();
					timer.stop();
				}
			}
		});
		
		timer.start();
	}
	
	public static void main(String[] args) {
		MainFrame frame = new MainFrame();
		frame.setVisible(true);
		while(frame.isVisible() && frame.timer.isRunning()) { }
		if(frame.timer.isRunning())
			frame.timer.stop();
		if(frame.isVisible())
			frame.setVisible(false);
		try {
			frame.nxtComm.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
