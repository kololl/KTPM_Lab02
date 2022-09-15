package template;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.apache.log4j.BasicConfigurator;
public class template_UI01 extends JFrame {
	public static void main(String[] args) throws NamingException, JMSException {
		JFrame frame = new JFrame("Chat Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 400);
		JMenuBar mb = new JMenuBar();
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Enter Text");
		final JTextField tf = new JTextField(10);
		final JButton send = new JButton("Send");
		panel.add(label);
		panel.add(tf);
		panel.add(send);
		final JTextArea ta = new JTextArea();
		frame.getContentPane().add(BorderLayout.SOUTH, panel);
		frame.getContentPane().add(BorderLayout.NORTH, mb);
		frame.getContentPane().add(BorderLayout.CENTER, ta);
		frame.setVisible(true);

		BasicConfigurator.configure();
		// thiáº¿t láº­p mÃ´i trÆ°á»�ng cho JJNDI
		Properties settings = new Properties();
		settings.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		settings.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
		// táº¡o context
		Context ctx = new InitialContext(settings);
		// lookup JMS connection factory
		Object obj = ctx.lookup("TopicConnectionFactory");
		ConnectionFactory factory = (ConnectionFactory) obj;
		// táº¡o connection
		final Connection con = factory.createConnection("admin", "admin");
		// ná»‘i Ä‘áº¿n MOM
		con.start();
		// táº¡o session
		final Session session = con.createSession(/* transaction */false, /* ACK */Session.CLIENT_ACKNOWLEDGE);
		// táº¡o consumer
		final Destination destination = (Destination) ctx.lookup("dynamicTopics/nmthuan1");
		final MessageConsumer receiver = session.createConsumer(destination);
		// receiver.receive();//blocked method
		// Cho receiver láº¯ng nghe trÃªn queue, chá»«ng cÃ³ message thÃ¬ notify
		ta.setEditable(false);
		receiver.setMessageListener(new MessageListener() {
			// cÃ³ message Ä‘áº¿n queue, phÆ°Æ¡ng thá»©c nÃ y Ä‘Æ°á»£c thá»±c thi
			public void onMessage(Message msg) {// msg lÃ  message nháº­n Ä‘Æ°á»£c
				try {
					if (msg instanceof TextMessage) {
						TextMessage tm = (TextMessage) msg;

						String txt = tm.getText();
						ta.append(txt + "\n");
//						("XML= " + txt);
						msg.acknowledge();// gá»­i tÃ­n hiá»‡u ack
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				Object object = e.getSource();
				if (object.equals(send)) {
					try {
						MessageProducer producer = session.createProducer(destination);
						String txt = tf.getText();
						Message msg = session.createTextMessage(txt);
						producer.send(msg);
					} catch (Exception e2) {
						// TODO: handle exception
						e2.printStackTrace();
					}
				}

			}
		});
//		session.close();
//		con.close();
	}
}
