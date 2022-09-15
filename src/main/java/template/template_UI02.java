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
public class template_UI02 extends JFrame {
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
		Properties settings = new Properties();
		settings.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
		settings.setProperty(Context.PROVIDER_URL, "tcp://localhost:61616");
		Context ctx = new InitialContext(settings);
		Object obj = ctx.lookup("TopicConnectionFactory");
		ConnectionFactory factory = (ConnectionFactory) obj;
		final Connection con = factory.createConnection("admin", "admin");
		con.start();
		final Session session = con.createSession(/* transaction */false, /* ACK */Session.CLIENT_ACKNOWLEDGE);
		final Destination destination = (Destination) ctx.lookup("dynamicTopics/nmthuan1");
		final Destination destination2 = (Destination) ctx.lookup("dynamicTopics/nmthuan");
		final MessageConsumer receiver = session.createConsumer(destination);
		
		//RECEIVER
		ta.setEditable(false);
		receiver.setMessageListener(new MessageListener() {			
			public void onMessage(Message msg) {
				try {
					if (msg instanceof TextMessage) {
						TextMessage tm = (TextMessage) msg;
						String txt = tm.getText();						
						ta.append("Nhận được "+txt+"\n");
						msg.acknowledge();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		
		//SENDER
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				Object object = e.getSource();
				if(object.equals(send)) {
					try {
						MessageProducer producer = session.createProducer(destination2);
						String txt = tf.getText();
						ta.append(txt+"\n");
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