/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package holoreader.serialcommunication;

import gnu.io.*;
import holoreader.gui.MainPage;
import holoreader.model.Chart;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.TooManyListenersException;

/**
 *
 * @author Rizaldi Habibie
 */
public class Input implements Runnable, SerialPortEventListener{
    
    MainPage window = null;

    //for saving ports that will be foud
    private Enumeration ports = null;
    //map the port names to CommPortIndentifiers
    private HashMap portMap = new HashMap();
    //this is the object that contains the opened port
    private CommPortIdentifier selectedPortIdentifier = null;
    private SerialPort serialPort;
    
    //input and output streams for sending and receiving data
    private InputStream input = null;
    private OutputStream output = null;
    private Chart result = null;
    //flag if the program is connected to port serial or not
    private boolean connected = false, showInput = false,stopChart = false;
    
    //timeout for connecting port
    private final static int TIMEOUT = 2000;
    
    //some ascii value for certain things
    private final static int SPACE_ASCII = 32;
    private final static int DASH_ASCII = 45;
    private final static int NEW_LINE_ASCII = 10;
    private List<Long> data = null, logData = null;
    
    private int loop = 0;
    private boolean getReady = false;
    private String activeRegister, temper, status;
    private String selectedPort ;
    //as string for recording what goes on in the program
    //this string is written to the gui
    private String logText = "";
    private Thread readThread;
    private byte[] readBuffer;
    private int endIndex = 0;

    /**
     *
     * @param window
     */
    public Input(MainPage window) {
        this.window = window;
    }
    public void searchAvailablePorts(){
        ports = CommPortIdentifier.getPortIdentifiers();
        while(ports.hasMoreElements()){
            CommPortIdentifier currentPort = (CommPortIdentifier)ports.nextElement();
            //get only serial port
            if(currentPort.getPortType() == CommPortIdentifier.PORT_SERIAL){
                window.getPortCbx().addItem(currentPort.getName());
                portMap.put(currentPort.getName(), currentPort);
            }
        }
    }
    
    public void connect(){
        data = new ArrayList<>();
        logData = new ArrayList<>();
        selectedPort = (String)window.getPortCbx().getSelectedItem();
        selectedPortIdentifier = (CommPortIdentifier)portMap.get(selectedPort);
        CommPort commPort=null;
        try{
            commPort = selectedPortIdentifier.open("Control Panel", TIMEOUT);
            serialPort = (SerialPort)commPort;
            setConnected(true);
            logText = selectedPort+" Opened Successfully";
            System.out.println(logText);
            window.getStatus().setText("SCANNING...");
            window.getStatus().setForeground(Color.BLUE);
        }catch(PortInUseException e){
            logText = selectedPort + " is in use. (" + e.toString() + ")";
            window.getStatus().setText("ERROR ! PLEASE VIEW LOG ---- >");
            window.getStatus().setForeground(Color.BLUE);
        }catch(Exception e){
            logText = "Failed to open " + selectedPort + "(" + e + ")";
            window.getStatus().setText("ERROR ! PLEASE VIEW LOG ---- >");
            window.getStatus().setForeground(Color.BLUE);
            e.printStackTrace();
        }
    }
    
    public boolean initIOStream(){
        boolean successful = false;
        try{
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            successful = true;
        }catch(IOException e){
            logText = "I/O Streams failed to open. (" + e.toString() + ")";
            window.getStatus().setText("ERROR ! PLEASE VIEW LOG ---- >");
            window.getStatus().setForeground(Color.BLUE);
            return successful;
        }
        return successful;
    }
    public void initListener(){
        try{
           serialPort.addEventListener(this);
           serialPort.notifyOnDataAvailable(true);  
           serialPort.setSerialPortParams(38400, SerialPort.DATABITS_8,SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
           readThread = new Thread(this);
           readThread.start();
        }catch(TooManyListenersException e){
            logText = "Too many listeners. (" + e.toString() + ")";
        } catch (UnsupportedCommOperationException e) {
            logText = "error (" + e.toString() + ")";
        }
    }
    public boolean showInput(){
        return showInput = true;
    }
    public boolean dontShowInput(){
        return showInput = false;
    }
    public void disconnect(){
        try{
            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
            setConnected(false);
            window.getStatus().setEnabled(true);
            window.getStatus().setText("STOPPED !");
            window.getStatus().setForeground(Color.RED);
            logText = "Disconnected.";
        }catch(Exception e){
            logText = "Failed to close " + serialPort.getName()
                              + "(" + e.toString() + ")";
        }
    }
    @Override
    public void serialEvent(SerialPortEvent event) {
        if(event.getEventType() == SerialPortEvent.DATA_AVAILABLE){
            window.getStatus().setText("READ INPUT...");
            window.getStatus().setForeground(Color.RED);
                Long singleData = null;
                try{
                  singleData = Long.parseLong(""+input.read());
                  if(singleData == 255){
                    endIndex++;
                  }else{
                    if(endIndex>2){
                        
                    }else if(endIndex<2){
                        data.add(singleData);  
                    }
                  }
                  
                if(endIndex==2){
                    if(!stopChart){
                    result = new Chart(this.window);
                    result.setData(data);
                    result.showChart();
                    logData = data;
                    data = new ArrayList<>();
                    window.getStatus().setText("SCANNING...");
                    window.getStatus().setForeground(Color.BLUE);
                    stopChart = true;
                    }else{
                        window.getStatus().setText("SCANNING...");
                        window.getStatus().setForeground(Color.BLUE);
                    }
                }else if(endIndex==4){
                    logData = data;
                    endIndex=0;
                    stopChart = false;
                    window.getStatus().setText("SCANNING...");
                    window.getStatus().setForeground(Color.BLUE);
                    data = new ArrayList<>();
                }
                    
            }catch(IOException | NumberFormatException e){
                logText = "Failed to read data. (" + e.toString() + ")";
            }
        }

    }

    public boolean isConnected() {
        return connected;
    }
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public List<Long> getLogData() {
        return logData;
    }

    public void setLogData(List<Long> logData) {
        this.logData = logData;
    }

    public String getLogText() {
        return logText;
    }

    public void setLogText(String logText) {
        this.logText = logText;
    }
    
    @Override
    public void run() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {System.out.println(e);}
    }

}
