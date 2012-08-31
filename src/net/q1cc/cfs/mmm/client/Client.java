/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.mmm.client;

import net.q1cc.cfs.mmm.common.world.WorldLoader;
import net.q1cc.cfs.mmm.common.world.World;
import java.io.File;
import javax.swing.JOptionPane;
import net.q1cc.cfs.mmm.client.render.MainGLRender;
import net.q1cc.cfs.mmm.common.Info;
import net.q1cc.cfs.mmm.common.Player;

/**
 *
 * @author claus
 */
public class Client {
    
    public static Client instance;

    private static void setLibPath() {
        String osname = System.getProperty("os.name");
        String os="";
        if(osname.contains("Windows")){
            os="windows";
        } else if(osname.contains("Linux")){
            os="linux";
        } else if(osname.contains("Mac OS X") || osname.contains("OS X")){
            os="macosx";
        } else if(osname.contains("Solaris")){
            os="solaris";
        } else {
            JOptionPane.showMessageDialog(null,"Your System, "+osname
                    +", seems not to be supported by mmm. Sorry.",
                    "OS not supported",JOptionPane.ERROR_MESSAGE,null);
        }
        
        System.setProperty("org.lwjgl.librarypath",new File("").getAbsolutePath()+"/lib/native/"+os);
    }
    
    public MainGLRender renderer;
    
    public World world;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        setLibPath();
        //some switches
        System.setProperty("org.lwjgl.util.Debug", "true");
        
        //process commandline
        //init something else
        Info.info = new ClientInfo();
        instance=new Client();
        instance.init();
    }
    
    public void init(){
        //display menu
        //connect to server or load world
        System.out.println(Runtime.getRuntime().maxMemory());
        loadWorld(new File("testworld/"));
        //System.out.println(world.generateOctree.toString(0)); //this takes some time.
        renderer=new MainGLRender(world);
        renderer.start();
        try {
            renderer.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.out.println("exiting");
    }
    
    private void loadWorld(File folder){
        world=WorldLoader.load(folder);
    }
    
}
