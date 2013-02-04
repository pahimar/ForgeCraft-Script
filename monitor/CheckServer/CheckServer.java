import java.io.*;
import java.net.*;

public class CheckServer {
	public static void main(String[] args) {
	 if (args.length < 2)
	 {
	   System.out.println("Usage: CheckServer <host> <port>");
	   System.exit(1);
	 }
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		
		int failCount = 0;
	
		for (;;) {
			Socket socket = null;
			DataInputStream datainputstream = null;
			DataOutputStream dataoutputstream = null;

			try {
				socket = new Socket();
		
				socket.setSoTimeout(3000);
				socket.setTcpNoDelay(true);
				socket.setTrafficClass(18);
		
				socket.connect(new InetSocketAddress(host, port), 3000);
		
				datainputstream = new DataInputStream(socket.getInputStream());
				dataoutputstream = new DataOutputStream(socket.getOutputStream());
		
				dataoutputstream.write(254);
		
				if (datainputstream.read() != 255) throw new IOException("Bad message");
				
				short strLength = datainputstream.readShort();
				if (strLength < 0 || strLength > 64) throw new IOException("invalid string length");
				
				StringBuilder stringBuilder = new StringBuilder();
				for(int i=0; i<strLength; i++) stringBuilder.append(datainputstream.readChar());

				String data = stringBuilder.toString();
				String dataParts[] = data.split("\u00a7");
		
				String motd = dataParts[0];
		
				int playerCount = -1;
				int maxPlayerCount = -1;
		
				try {
					playerCount = Integer.parseInt(dataParts[1]);
					maxPlayerCount = Integer.parseInt(dataParts[2]);
				} catch(Exception e) { }

				//System.out.println("Motd: "+motd);
				//System.out.println("Players: "+playerCount+"/"+maxPlayerCount);
				
				failCount = 0;
			} catch (Exception e) {
				//e.printStackTrace();
				
				failCount++;
				
				if (failCount >= 10) {
					try {
						//System.out.println("Restart");
						String command[] = { "/bin/bash", "service", "minecraft", "restart" };
						
						Process	restartProcess = Runtime.getRuntime().exec(command);
						restartProcess.waitFor();
					
						Thread.sleep(60000);
					} catch (Exception e2) {
						return;
					}
				}
			} finally {
				try {
					if(datainputstream != null) datainputstream.close();
				} catch(Throwable throwable) { }
				try {
					if(dataoutputstream != null) dataoutputstream.close();
				} catch(Throwable throwable1) { }
				try {
					if(socket != null) socket.close();
				} catch(Throwable throwable2) { }
			}
			
			try {
				Thread.sleep(30000);
			} catch (Exception e) { }
		}
	}
}

