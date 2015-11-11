/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package serversockettcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author David Mencos
 */
public class ServerSocketTCP {

    /**
     * @param args the command line arguments
     */
    
    public ArrayList<NewClientAccept> usuariosConectados = new ArrayList<NewClientAccept>();
    public ArrayList<String> usernames = new ArrayList<String>();
    private ServerSocket server = null;
    private Socket clientSocket = null;
    private DataInputStream input = null;
    //private DataOutputStream output = null;
    
    public void ChatServer(int port){
        try{
            //System.out.println("Conexión en puerto " + port + "...");
            ServerWindow.gui.ActualizarNotificaciones("Conexión en puerto " + port + "...");
            server = new ServerSocket(port);
            //System.out.println("Servidor iniciado: " + server);
            ServerWindow.gui.ActualizarNotificaciones("Servidor iniciado: " + server);
            //System.out.println("Esperando por clientes...");
            ServerWindow.gui.ActualizarNotificaciones("Esperando por clientes...");
//            while(true){
//                clientSocket = server.accept();
//                NewClientAccept nuevoCliente = new NewClientAccept(clientSocket);
//                //System.out.println("Cliente conectado: " + clientSocket);
//                ServerWindow.gui.ActualizarNotificaciones("Cliente conectado: " + clientSocket);
//            }
            //open();
        }
        catch(IOException ioe){
            //System.out.println(ioe);
            ServerWindow.gui.ActualizarNotificaciones(ioe.toString());
        }
    }
    
    public void listen(){
        while(true){
            try{
                clientSocket = server.accept();
                //System.out.println("Cliente conectado: " + clientSocket);
                ServerWindow.gui.ActualizarNotificaciones("Cliente conectado: " + clientSocket);
                //System.out.println("Puerto al que está conectado: " + clientSocket.getPort());
                
                // Agrega al nuevo cliente a la lista de clientes activos y lo inicializa
                NewClientAccept nuevoCliente = new NewClientAccept(clientSocket);
                nuevoCliente.start();
                usuariosConectados.add(nuevoCliente);
            }
            catch(IOException ioe){
                //System.out.println(ioe);
                ServerWindow.gui.ActualizarNotificaciones("Error al conectar con cliente: " + ioe.toString());
            }
        }
    }
    
    public void sendBroadcastMessage(String mensaje, int valorByte){
        int puerto = 0;
        if(valorByte == 1){
            puerto = Integer.parseInt(mensaje.substring(mensaje.length()-5, mensaje.length()));
            mensaje = mensaje.substring(0, mensaje.length()-5);
            //ServerWindow.gui.ActualizarNotificaciones("Puerto del cliente: " + puerto);
        }
        for(int i = 0; i < usuariosConectados.size(); i++){
            //ServerWindow.gui.ActualizarNotificaciones("Puerto del cliente en server: " + usuariosConectados.get(i).getClientPort());
            if(usuariosConectados.get(i).getClientPort() != puerto){
                usuariosConectados.get(i).sendMessage(mensaje, valorByte);
//                System.out.println(String.valueOf(ServerWindow.server.usuariosConectados.get(i).getPort()));
            }
        }
    }
    
//    public void sendBroadcastServerMessage(String mensaje, int valorByte){
//        for(int i = 0; i < usuariosConectados.size(); i++){
//            usuariosConectados.get(i).sendMessage(mensaje, valorByte);
////            System.out.println(String.valueOf(ServerWindow.server.usuariosConectados.get(i).getPort()));
//        }
//    }
    
//    private void open() throws IOException{
//        input = new DataInputStream(clientSocket.getInputStream());
//    }
//    
//    public void listen(){
//        try{
//            boolean done = false;
//            String line = "";
//            while (!done){
//                try{
//                    line = input.readUTF();
//                    //System.out.println(line);
//                    ServerWindow.gui.ActualizarNotificaciones(line);
//                    done = line.equals("exit");
//                }
//                catch(IOException e){
//                    System.out.println("Server salio del while de escucha de cliente.");
//                    done = true;
//                }
//            }
//            close();
//        }catch(IOException e){
//            ServerWindow.gui.ActualizarNotificaciones("Error al escuchar al cliente: " + e.getMessage());
//        }
//    }
//    
//    private void close() throws IOException{
//        if (clientSocket != null){
//            clientSocket.close();
//        }
//        if (input != null){
//            input.close();
//        }
//    }
    
//    public static void main(String[] args) {
//    }
}
