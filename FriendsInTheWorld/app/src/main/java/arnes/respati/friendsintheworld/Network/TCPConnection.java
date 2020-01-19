package arnes.respati.friendsintheworld.Network;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import arnes.respati.friendsintheworld.Communications;
import arnes.respati.friendsintheworld.Controller;

//service runs throughout the application lifecycle, foreground background

public class TCPConnection extends Service {
    public static final String IP="IP",PORT="PORT";

    private RunOnThread thread;
    private Receive receive;
    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private InetAddress address;
    private int connectionPort;
    private String ip;
    private Exception exception;
    private Buffer <String> receiveBuffer;
    private boolean connected = false;

    @Override
    public void onCreate(){
        super.onCreate();
        //If the service is already running, this method is not called.
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.ip = intent.getStringExtra(IP);
        this.connectionPort = Integer.parseInt(intent.getStringExtra(PORT));
        thread = new RunOnThread();
        receiveBuffer = new Buffer<String>();
        return Service.START_STICKY;

        //START_STICKY tells the OS to recreate the service after it has enough
        // memory and call onStartCommand() again with a null intent.
        // START_NOT_STICKY tells the OS to not bother recreating the service again.
    }

    //provide an interface that clients use to communicate with the service by returning an IBinder.
    //otherwise will return null

    @Override
    public IBinder onBind(Intent arg0) {
        return new LocalService();
    }

    public void connect() {
        thread.start();
        thread.execute(new Connect());
    }

    public void disconnect() {
        thread.execute(new Disconnect());
        thread.stop();
    }

    public void send(String communications) {
        if (!connected) {
            thread.execute(new Connect());
        }
        thread.execute(new Send(communications));    }

    public String receive() throws InterruptedException {
        return receiveBuffer.get();
    }

    //A bound service runs only as long as another application component is bound to it.
    // Multiple components can bind to the service at once, but when all of them unbind, the service is destroyed

    public class LocalService extends Binder {
        public TCPConnection getService() {
            return TCPConnection.this;
        }
    }

    public Exception getException() {
        Exception result = exception;
        exception = null;
        return result;
    }

    private class Receive extends Thread {
        public void run() {
            String result;
            try {
                while (receive != null) {
                    result = (String) input.readUTF();
                    ((Controller) getApplication()).receiving(result);
                    receiveBuffer.put(result);
                    Log.d("Receive message " , result);
                }
            } catch (Exception e) { // IOException, ClassNotFoundException
                receive = null;
                Handler h = new Handler(Looper.getMainLooper());
                h.post(() -> {
                    Toast.makeText(TCPConnection.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private class Connect implements Runnable {
        public void run() {
            try {
                Log.d("TCPConnection","Connect-run");
                address = InetAddress.getByName(ip);
                Log.d("TCPConnection-Connect","Skapar socket");
                socket = new Socket(address, connectionPort);
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
                output.flush();
                Log.d("TCPConnection-Connect","Strömmar klara");
                receiveBuffer.put("CONNECTED");
                receive = new Receive();
                receive.start();
                connected = true;
            } catch (Exception e) { // SocketException, UnknownHostException
                Log.d("TCPConnection-Connect",e.toString());
                exception = e;
                receiveBuffer.put("EXCEPTION");
            }
        }
    }

    public class Disconnect implements Runnable {
        public void run() {
            try {
                if (socket != null)
                    socket.close();
                if (input != null)
                    input.close();
                if (output != null)
                    output.close();
                thread.stop();
                receiveBuffer.put("CLOSED");
            } catch(IOException e) {
                exception = e;
                receiveBuffer.put("EXCEPTION");
            }
        }
    }

    public class Send implements Runnable {
        private String communications;

        public Send(String communications) {
            this.communications = communications;
        }

        public void run() {
            try {
                Log.d("Send message",communications);
                output.writeUTF(communications);
                output.flush();
            } catch (IOException e) {
                exception = e;
                receiveBuffer.put("EXCEPTION");
            }
        }
    }

}