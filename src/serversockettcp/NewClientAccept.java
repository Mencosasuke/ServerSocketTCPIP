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
                            ServerWindow.server.usernames.add(line);
                            //ServerWindow.gui.ActualizarNotificaciones("El usuario conectado es: " + line);
                            System.out.println("El usuario conectado es: " + line);
                            
                            // Envia los usuarios conectados actualmente
                            for(int i = 0; i < ServerWindow.server.usernames.size(); i++){
                                sendUser(String.valueOf(ServerWindow.server.usernames.get(i)));
                            //System.out.println(String.valueOf(usuariosConectados.get(i).getPort()));
                            }
                            
                            break;
                        case 1:
                            line = input.readUTF();
                            //System.out.println(line);
                            ServerWindow.gui.ActualizarNotificaciones(line);
                            done = line.equals("exit");
                            break;
                    }
                }
                catch(IOException e){
                    System.out.println("Server salio del while de escucha de cliente.");
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
    
    public void sendMessage(String mensaje){
        try{
            ServerWindow.gui.ActualizarNotificaciones("Mensaje del servidor: " + mensaje);
            System.out.println("Mensaje del servidor: " + mensaje);
            output.write(1);
            output.writeUTF("Servidor: " + mensaje);
            output.flush();
        }
        catch(IOException e){
            //System.out.println("Error en envío: " + e.getMessage());
            ServerWindow.gui.ActualizarNotificaciones("Error en envío: " + e.getMessage());
        }
    }
    
    public void sendUser(String mensaje){
        try{
            //ServerWindow.gui.ActualizarNotificaciones("Mensaje del servidor: " + mensaje);
            //System.out.println("Mensaje del servidor: " + mensaje);
            output.writeByte(0);
            output.writeUTF("Servidor: " + mensaje);
            output.flush();
        }
        catch(IOException e){
            //System.out.println("Error en envío: " + e.getMessage());
            ServerWindow.gui.ActualizarNotificaciones("Error en envío: " + e.getMessage());
        }
    }
}
