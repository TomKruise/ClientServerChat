package com.tomkou.chat.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import javax.swing.JOptionPane;

import com.tomkou.chat.util.CharacterUtil;
import com.tomkou.chat.util.XMLUtil;

public class ClientConnection extends Thread {
	private Client client;
	private String username;
	private int port;
	private String hostAddress;
	

	private Socket socket;
	
	private InputStream is;
	private OutputStream os;
	
	private ChatClient chatClient;
	
	
	public Socket getSocket() {
		return socket;
	}

	public ClientConnection(Client client, String username, int port, String hostAddress) {
		this.client = client;
		this.username = username;
		this.port = port;
		this.hostAddress = hostAddress;
		
		//连接服务器
		this.connect2Server();
		
	}
	
	//连接服务器，由构造方法调用
	private void connect2Server() {
		try {
			this.socket = new Socket(this.hostAddress, this.port);
			
			this.is = this.socket.getInputStream();
			this.os = this.socket.getOutputStream();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	//用户登录，向服务器端传送用户名
	//返回true表示成功登录
	//返回false表示登录失败
	public boolean login() {
		try {
			String xml = XMLUtil.constructLoginXML(username);
			os.write(xml.getBytes());//向服务器端发送用户的登录信息（包含了用户名）
			
			byte[] buf = new byte[1024];
			int length = is.read(buf);//读取服务器端的响应结果，判断用户是否登录成功
			
			String loginResultXMl = new String(buf, 0, length);
			
			String loginResult = XMLUtil.extractLoginResult(loginResultXMl);
			
			//登录成功
			if("success".equals(loginResult)) {
				//聊天室主窗口
				this.chatClient = new ChatClient(this);
				
				this.client.setVisible(false);
				
				return true;
			}
			//登录失败
			else {
				return false;
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return false;
	}
	
	public void sendMessage(String message, String type) {
		try {
			int t = Integer.parseInt(type);
			
			String xml = null;
			
			//客户端向服务器端发送聊天数据
			if(CharacterUtil.CLIENT_MESSAGE == t) {
				xml = XMLUtil.constructMessageXML(this.username, message);
			}
			//客户端向服务器端发送关闭窗口的数据
			else if(CharacterUtil.CLOSE_CLIENT_WINDOW == t) {
				xml = XMLUtil.constructCloseClientWindowXML(this.username);
			}
			
			//向服务器端发送数据
			this.os.write(xml.getBytes());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				byte[] buf = new byte[2048];
				int length = is.read(buf);
				
				String xml = new String(buf, 0, length);
				
				int type = Integer.parseInt(XMLUtil.extractType(xml));
				
				//在线用户列表
				if(type == CharacterUtil.USER_LIST) {
					List<String> list = XMLUtil.extractUserList(xml);
					
					String users = "";
					
					for(String user : list) {
						users += user + "\n";
					}
					
					this.chatClient.getJTextArea2().setText(users);
				}
				//服务器端发来的聊天数据
				else if(type == CharacterUtil.SERVER_MESSAGE) {
					String content = XMLUtil.extractContent(xml);
					
					this.chatClient.getJTextArea1().append(content + "\n");
				}
				//关闭服务器端窗口
				else if(type == CharacterUtil.CLOSE_SERVER_WINDOW) {
					JOptionPane.showMessageDialog(this.chatClient, "服务器已关闭，程序将退出", "信息", JOptionPane.INFORMATION_MESSAGE);
					
					System.exit(0);//客户端退出
				}
				//服务器端确认关闭客户端窗口
				else if (type == CharacterUtil.CLOSE_CLIENT_WINDOW_CONFIRMATION) {
					try {
						this.getSocket().getInputStream().close();
						this.getSocket().getOutputStream().close();
						this.getSocket().close();
					} catch (Exception e) {
						
					}
					finally {
						System.exit(0);//退出客户端程序
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
