/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package serversockettcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Map;
import java.util.UUID;

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
                    switch(tipoMensaje){
                        case 0:
                            line = input.readUTF();
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
                            line = input.readUTF();
                            System.out.println("El servidor recibió el mensaje con codigo 1.");
                            //System.out.println(line);
                            //int puerto = Integer.parseInt(line.substring(line.length()-5, line.length()));
                            ServerWindow.gui.ActualizarNotificaciones(line.substring(0, line.length()-5));
                            ServerWindow.server.sendBroadcastMessage(line, 1);
                            //done = line.equals("exit");
                            break;
                        case 3:
                            line = input.readUTF();
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
                        case 4:
                            String nombreArchivo = input.readUTF().toString();
                            int tam = input.readInt();
                            System.out.println("Recibiendo archivo " + nombreArchivo);
                            
                            String path = ServerWindow.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                            String decodedPath = URLDecoder.decode(path, "UTF-8");
                            String fullPath = decodedPath + UUID.randomUUID().toString().substring(0, 13) + nombreArchivo;
                            
                            FileOutputStream fos = new FileOutputStream(fullPath); 
                            BufferedOutputStream out = new BufferedOutputStream(fos); 
                            BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream());
                            
                            byte[] buffer = new byte[tam];
                            
                            for(int i = 0; i < buffer.length; i++){
                                buffer[i] = (byte)in.read();
                            }
                            
                            out.write(buffer);
                            out.flush();
                            in.close();
                            out.close();
                            
                            //System.out.println("Archivo " + fullPath + " descargado (" + tam + " bytes leidos.)");
                            ServerWindow.gui.ActualizarNotificaciones("Archivo " + fullPath + " descargado (" + tam + " bytes leidos.)");
                            
                            ServerWindow.server.sendBroadcastFile(fullPath, nombreArchivo, tam, 4);
                            
                            
                            //DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

                            //ServerWindow.server.sendBroadcastMessage("se envió el archivo :3", 2);

                            
                            
                            
                            
                            
                            break;
                        case 5:
//                            String usr = line.substring(line.indexOf('@') + 1, line.length());
//                            int portRecept = ServerWindow.server.usersTable.get(usr);
//                            for(int i = 0; i < ServerWindow.server.usuariosConectados.size(); i++){
//                                if(ServerWindow.server.usuariosConectados.get(i).getClientPort() == portRecept){
//                                    ServerWindow.server.usuariosConectados.get(i).sendMessage("@" + usr + " esta enviando un archivo...", 3);
//                                    break;
//                                }
//                            }
                            break;
                    }
                }
                catch(IOException e){
                    ServerWindow.gui.ActualizarNotificaciones("Error en la escucha del cliente. " + clientSocket + ". Cliente desconectado.");
                    for (Map.Entry<String, Integer> entrada : ServerWindow.server.usersTable.entrySet()){
                        if(entrada.getValue() == clientSocket.getPort()){
                            ServerWindow.gui.ActualizarNotificaciones("Usuario encontrado. " + entrada.getKey() + " se ha desconectado.");
                            ServerWindow.server.sendBroadcastMessage("El usuario " + entrada.getKey() + " se ha desconectado.", 2);
                            ServerWindow.server.usersTable.remove(entrada.getKey());
                            ServerWindow.server.usernames.remove(entrada.getKey());
                            for(NewClientAccept sockets : ServerWindow.server.usuariosConectados){
                                if(sockets.getClientPort() == clientSocket.getPort()){
                                    ServerWindow.gui.ActualizarNotificaciones("Socket encontrado: " +  sockets.clientSocket);
                                    ServerWindow.server.usuariosConectados.remove(sockets);
                                }
                            }
                            // Envia los usuarios conectados actualmente
                            sendMessage("***********USUARIOS CONECTADOS***********", 2);
                            for(int i = 0; i < ServerWindow.server.usernames.size(); i++){
                                sendUser("- " + String.valueOf(ServerWindow.server.usernames.get(i)));
                            //System.out.println(String.valueOf(usuariosConectados.get(i).getPort()));
                            }
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
    
    public void sendFile(String fullPath, String nombreArchivo, int tamaño, int valorByte){
        try{
            ServerWindow.gui.ActualizarNotificaciones("Enviando archivo: " + nombreArchivo);
            output.writeByte(4);
            output.writeUTF(nombreArchivo);
            output.writeInt(tamaño);

            FileInputStream fis = new FileInputStream(fullPath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            BufferedOutputStream bos = new BufferedOutputStream(clientSocket.getOutputStream());
            byte[] buffer = new byte[tamaño];
            bis.read(buffer);

            for(int i = 0; i < buffer.length; i++){
                bos.write(buffer[i]);
            }

            bos.flush();
            bis.close();
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
