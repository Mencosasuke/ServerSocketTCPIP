/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package serversockettcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;

/**
 *
 * @author David Mencos
 */
public class NewClientAccept extends Thread {
    
    private Socket clientSocket = null;
    private DataInputStream input = null;
    private DataOutputStream output = null;
    
    @Override
    public void run(){
        //JOptionPane.showMessageDialog(ServerWindow.gui, "Cliente inicia proceso de comunicación");
        try{
            open();
        }catch(IOException ioe){
            //System.out.println(ioe);
            ServerWindow.gui.ActualizarNotificaciones(ioe.toString());
        }
    }
    
    public NewClientAccept(Socket cliente){
        this.clientSocket = cliente;
    }
    
    private void open() throws IOException{
        input = new DataInputStream(clientSocket.getInputStream());
        output = new DataOutputStream(clientSocket.getOutputStream());
//        try{
//            sendMessage("prueba de envio de mensaje servidor - cliente");
//            Thread.sleep(3000);
//        }catch(InterruptedException e){}
        listen();
    }
    
    private void listen(){
        try{
            boolean done = false;
            String line = "";
            while (!done){
                try{
                    byte tipoMensaje = input.readByte();
                    line = input.readUTF();
                    switch(tipoMensaje){
                        case 0:
                            String uname = line.substring(0, line.length()-5);
                            int port = Integer.parseInt(line.substring(line.length()-5, line.length()));
                            ServerWindow.server.usernames.add(uname);
                            ServerWindow.server.usersTable.put(uname, port);
                            //ServerWindow.gui.ActualizarNotificaciones("El usuario conectado es: " + line);
                            //System.out.println("El usuario conectado es: " + uname);
                            
                            // Envia los usuarios conectados actualmente
                            sendMessage("***********USUARIOS CONECTADOS***********", 2);
                            for(int i = 0; i < ServerWindow.server.usernames.size(); i++){
                                sendUser("- " + String.valueOf(ServerWindow.server.usernames.get(i)));
                            //System.out.println(String.valueOf(usuariosConectados.get(i).getPort()));
                            }
                            ServerWindow.server.sendBroadcastMessage("El usuario " + uname + " ha iniciado sesión." + port, 0);
                            
                            break;
                        case 1:
                            //System.out.println(line);
                            //int puerto = Integer.parseInt(line.substring(line.length()-5, line.length()));
                            ServerWindow.gui.ActualizarNotificaciones(line.substring(0, line.length()-5));
                            ServerWindow.server.sendBroadcastMessage(line, 1);
                            //done = line.equals("exit");
                            break;
                        case 3:
                            //System.out.println(line);
                            //System.out.println(user + "-" + mensaje);
                            String user = line.substring(line.indexOf('@') + 1, line.length());
                            String mensaje = line.substring(0, line.indexOf('@'));
                            int puertoReceptor = ServerWindow.server.usersTable.get(user);
                            for(int i = 0; i < ServerWindow.server.usuariosConectados.size(); i++){
                                if(ServerWindow.server.usuariosConectados.get(i).getClientPort() == puertoReceptor){
                                    ServerWindow.server.usuariosConectados.get(i).sendMessage(mensaje, 3);
                                    break;
                                }
                            }
                            break;
                    }
                }
                catch(IOException e){
                    ServerWindow.gui.ActualizarNotificaciones("Error en la escucha del cliente. " + clientSocket + ". Cliente desconectado.");
                    for (Map.Entry<String, Integer> entrada : ServerWindow.server.usersTable.entrySet()){
                        if(entrada.getValue() == clientSocket.getPort()){
                            ServerWindow.gui.ActualizarNotificaciones("Usuario encontrado. " + entrada.getKey() + " se ha desconectado.");
                            ServerWindow.server.usersTable.remove(entrada.getKey());
                            break;
                        }
                    }
                    //String logOffUser = String.valueOf(clientSocket.getPort());
                    done = true;
                }
            }
            close();
        }catch(IOException e){
            ServerWindow.gui.ActualizarNotificaciones("Error al escuchar al cliente: " + e.getMessage());
        }
    }
    
    private void close() throws IOException{
        if (clientSocket != null){
            clientSocket.close();
        }
        if (input != null){
            input.close();
        }
        if (output != null){
            output.close();
        }
    }
    
    public void sendMessage(String mensaje, int valorByte){
        try{
            ServerWindow.gui.ActualizarNotificaciones("Mensaje del servidor: " + mensaje);
            //System.out.println("Mensaje del servidor: " + mensaje);
            output.writeByte(valorByte);
            switch(valorByte){
                case 1:
                    output.writeUTF(mensaje);
                    break;
                case 2:
                    output.writeUTF("Servidor: " + mensaje);
                    break;
                case 3:
                    output.writeUTF(mensaje);
                    break;
            }
            output.flush();
        }
        catch(IOException e){
            //System.out.println("Error en envío: " + e.getMessage());
            ServerWindow.gui.ActualizarNotificaciones("Error en envío: " + e.getMessage());
        }
    }
    
    public void sendUser(String mensaje){
        try{
            ServerWindow.gui.ActualizarNotificaciones("Mensaje del servidor: " + mensaje);
            //System.out.println("Mensaje del servidor: " + mensaje);
            output.writeByte(0);
            output.writeUTF(mensaje);
            output.flush();
        }
        catch(IOException e){
            //System.out.println("Error en envío: " + e.getMessage());
            ServerWindow.gui.ActualizarNotificaciones("Error en envío: " + e.getMessage());
        }
    }
    
    public int getClientPort(){
        return clientSocket.getPort();
    }
    
}
