
package net.q1cc.cfs.mmm.client.render;


import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedDeque;
import net.q1cc.cfs.mmm.client.Client;
import net.q1cc.cfs.mmm.common.math.Quaternionf;
import net.q1cc.cfs.mmm.common.math.Vec3d;
import net.q1cc.cfs.mmm.common.math.Vec3f;
import org.lwjgl.LWJGLException;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL20.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.util.glu.GLU.*;
import static net.q1cc.cfs.mmm.client.render.Primitives.*;

import net.q1cc.cfs.mmm.common.Player;
import net.q1cc.cfs.mmm.common.world.World;
import net.q1cc.cfs.mmm.common.world.WorldOctree;
import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GLContext;

/**
 * @author claus
 */
public class MainGLRender extends Thread {
    
    WorkerTaskPool taskPool = new WorkerTaskPool();
    
    ConcurrentLinkedDeque<GLChunklet> chunksToBuffer = new ConcurrentLinkedDeque<GLChunklet>();
    
    private boolean fullscreen = false;
    private final String windowTitle = "mmm-java";
    private boolean f11 = false;
    private boolean done=false;
    
    Player player;

    //Vec3d cameraPos;
    
    World world;
    float mouseSpeed=1.0f;
    
    private DisplayMode displayMode;
    private TextureLoader texL;
    
    /* stats */
    
    /**
     * the number of chunklets successfully rendered in the current frame.
     */
    private int chunkletsRendered;
    
    public MainGLRender(World world){
        this.world=world;
        player=world.player;
    }
    
    @Override
    public void run() {
        texL= new TextureLoader();
        init();
        
        lastFPS = getTime();
        try {
            while (!done) {
                keyboard();
                
                render();
                checkGLError();
                Display.update();
                updateFPSandDelta();
                Display.sync(60);
            }
            cleanup();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    private void keyboard() {
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {       // Exit if Escape is pressed
            Mouse.setGrabbed(false);
        }
        if(Display.isCloseRequested()) {                     // Exit if window is closed
            done = true;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_F11) && !f11) {    // Is F1 Being Pressed?
            f11 = true;                                      // Tell Program F1 Is Being Held
            switchMode();                                   // Toggle Fullscreen / Windowed Mode
        }
        if(!Keyboard.isKeyDown(Keyboard.KEY_F11)) {          // Is F1 Being Pressed?
            f11 = false;
        }
        
        boolean left=false,right=false,forward=false,back=false;
        if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
            left=true;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
            right=true;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
            forward=true;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
            back=true;
        }
        if(left||right||forward||back){
            player.move(forward, left, right, back, false);
        }
        
        
        //cameraRotY=Mouse.getDX()*0.05f;
        //cameraRotX=Mouse.getDY()*0.05f;
        if(Mouse.isGrabbed()){
            player.rotation.x+=Mouse.getDX()*0.1f*mouseSpeed;
            player.rotation.y+=-Mouse.getDY()*0.1f*mouseSpeed;
            if(player.rotation.y>90f)
                player.rotation.y=90f;
            else if(player.rotation.y<-90f)
                player.rotation.y=-90f;
            if(player.rotation.x<0f)
                player.rotation.x+=360f;
            else if(player.rotation.x>359f)
                player.rotation.x-=360f;
            
            //TODO calculate camera normal vector
            player.rotationNormal=Vec3f.BACK;
            player.rotationNormal=Quaternionf.rotate(player.rotationNormal, -player.rotation.y, true, false, false);
            player.rotationNormal=Quaternionf.rotate(player.rotationNormal, -player.rotation.x, false, true, false);
            
            
        } else if(Mouse.isButtonDown(0)) {
            Mouse.setGrabbed(true);
        }
        
    }

    private void switchMode() {
        fullscreen = !fullscreen;
        try {
            Display.setFullscreen(fullscreen);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glLoadIdentity();
        setCamera();
        
        // wireframe mode
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        
        //TODO all the render methods here
        chunkletsRendered=0;
        renderOctree(world.generateOctree);
        //System.out.println("chunklets: "+chunkletsRendered);
        //TODO HUD here
        
        //TODO check if there is time
        //now to buffer some chunks
        if(!chunksToBuffer.isEmpty()){
            for(int i=0;i<50;i++){ //TODO do as many as time allows us to
                if(!chunksToBuffer.isEmpty())
                    bufferChunk(chunksToBuffer.pop());
            }
            //chunksToBuffer.clear(); //debug
        }
        
        
        return true;
    }
    
    
    void setCamera() {
        glRotatef(player.rotation.y,1.0f,0.0f,0.0f);  //rotate our camera on teh x-axis (up down)
        glRotatef(player.rotation.x,0.0f,1.0f,0.0f);  //rotate our camera on the y-axis (left right)
        glTranslated(-player.position.x,-player.position.y,-player.position.z); //translate the screento the position of our camera
    }
    private void createWindow() {
        try {
            Display.setFullscreen(fullscreen);
            DisplayMode d[] = Display.getAvailableDisplayModes();
            displayMode=d[0];
            for (int i = 1; i < d.length; i++) {
                if (d[i].getWidth() == 800
                    && d[i].getHeight() == 600
                    && d[i].getBitsPerPixel() == 32) {
                    displayMode = d[i];
                    break;
                }
            }
            Display.setDisplayMode(displayMode);
            Display.setTitle(windowTitle);
            Display.setVSyncEnabled(true);
            Display.create();
            
        } catch (LWJGLException ex) {
            ex.printStackTrace();
        }
    }
    public void init() {
        // start worker Threads
        taskPool.initWorkers();
        // start loading the world and converting chunklets to glchunklets
        
        //since we have the full world already (debug mode), just convert all chunklets
        taskPool.add(new WorkerTask(){
            @Override
            public boolean doWork() {
                convertAllBlocks();
                return true;
            }

            @Override
            public int getPriority() {
                return WorkerTask.PRIORITY_MAX;
            }
        });
        
        createWindow();
        Mouse.setGrabbed(false);
        initGL();
    }

    private void initGL() {
        glEnable(GL_TEXTURE_2D); // Enable Texture Mapping
        glShadeModel(GL_SMOOTH); // Enable Smooth Shading
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // Black Background
        glClearDepth(1.0); // Depth Buffer Setup
        glEnable(GL_DEPTH_TEST); // Enables Depth Testing
        glDepthFunc(GL_LEQUAL); // The Type Of Depth Testing To Do
        
        glMatrixMode(GL_PROJECTION); // Select The Projection Matrix
        glLoadIdentity(); // Reset The Projection Matrix
        
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        // Calculate The Aspect Ratio Of The Window
        gluPerspective(
          50.0f,
          (float) displayMode.getWidth() / (float) displayMode.getHeight(),
          0.1f,
          1024.0f);
        glMatrixMode(GL_MODELVIEW); // Select The Modelview Matrix

        // Really Nice Perspective Calculations
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        
        //Primitives.cubeList=Primitives.generateCubeList();
    }
    
    private static void cleanup() {
        Display.destroy();
        
        //TODO kill worker threads
    }
    
    
    /**
     * Get the time in milliseconds
     * 
     * @return The system time in milliseconds
     */
    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }
    
    /** time at last frame */
    long lastFrame;
    /** frames per second */
    int fps;
    /** last fps time */
    long lastFPS;

    public float deltaTime;
    
    /**
     * Calculate the FPS and set it in the title bar
     */
    public void updateFPSandDelta() {
        long time = getTime();
        deltaTime = (time - lastFrame)/1000f;
        lastFrame = time;
        
        if (time - lastFPS > 1000) {
            Display.setTitle("FPS: " + fps +" deltaT: "+deltaTime+" normal: "+player.rotation);
            fps = 0; //reset the FPS counter
            lastFPS += 1000; //add one second
        }
        fps++;
    }
    
    void convertAllBlocks() {
        doConvert(world.generateOctree);
    }
    void doConvert(WorldOctree oc){
        if(oc==null) return;
        if(oc.block!=null){
            GLChunklet n = new GLChunklet(oc.block);
            oc.block = n;
            taskPool.add(n);
        }
        if(!oc.hasSubtrees) return;
        for(WorldOctree c: oc.subtrees){
            doConvert(c);
        }
    }

    private void bufferChunk(GLChunklet cl) {
        cl.vboID = glGenBuffers();
        cl.iboID = glGenBuffers();
        cl.vaoID = glGenVertexArrays();
        glBindVertexArray(cl.vaoID);
        glBindBuffer(GL_ARRAY_BUFFER, cl.vboID);
        glBufferData(GL_ARRAY_BUFFER, cl.vertexB, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,cl.iboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER,cl.indexB,GL_STATIC_DRAW);
        checkGLError();
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);
        glEnableClientState(GL_INDEX_ARRAY);
        checkGLError();
        //glEnableVertexAttribArray(0);
        //glEnableVertexAttribArray(1);
        //glEnableVertexAttribArray(2);
        //glVertexAttribPointer(0, 3, GL_FLOAT, false,4*8,4*0);
        //glVertexAttribPointer(1, 1, GL_FLOAT, false,4*8,4*3);
        //glVertexAttribPointer(2, 3, GL_FLOAT, false,4*8,4*4);
        glVertexPointer(3, GL_FLOAT,4*8, 4*0);
        glColorPointer(3,GL_FLOAT,4*8,4*4);
        checkGLError();
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER,0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,0);
    }

    private void renderOctree(WorldOctree oc) {
        //TODO use renderLists here
        if(oc==null) return;
        if(oc.block!=null){
            if(oc.block instanceof GLChunklet && oc.block.blocksInside>0){
                //render this
                checkGLError();
                GLChunklet g = (GLChunklet)oc.block;
                //glBindVertexArray(g.vaoID);
                checkGLError();
                glBindBuffer(GL_ARRAY_BUFFER, g.vboID);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, g.iboID);
                checkGLError();
                //glEnableVertexAttribArray(0);
                //glEnableVertexAttribArray(1);
                //glEnableVertexAttribArray(2);
                glEnableClientState(GL_VERTEX_ARRAY);
                glEnableClientState(GL_COLOR_ARRAY);
                glEnableClientState(GL_INDEX_ARRAY);
                //glVertexAttribPointer(0, 3, GL_FLOAT, false,4*8,4*0);
                //glVertexAttribPointer(1, 1, GL_FLOAT, false,4*8,4*3);
                //glVertexAttribPointer(2, 3, GL_FLOAT, false,4*8,4*4);
                glVertexPointer(3, GL_FLOAT, 4 * 8, 4 * 0);
                glColorPointer(3, GL_FLOAT, 4 * 8, 4 * 4);
                checkGLError();
                glDrawElements(GL_TRIANGLES, g.indCount, GL_UNSIGNED_INT, 0);
                checkGLError();
                glBindBuffer(GL_ARRAY_BUFFER, 0);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,0);
                checkGLError();
                chunkletsRendered++;
            }
        }
        if(!oc.hasSubtrees) return;
        for(WorldOctree s:oc.subtrees){
            renderOctree(s);
        }
    }

    private static int lastError=0;
    private static int lastErrorCount=0;
    public static void checkGLError() {
        int err = glGetError();
        if(err!=GL_NO_ERROR){
            if(err!=lastError){
                System.out.println(gluErrorString(err));
                lastError=err;
                lastErrorCount=1;
            } else {
                lastErrorCount++;
                if(lastErrorCount%1000==0){
                    System.out.println("repeated "+lastErrorCount+" times.");
                }
            }
        }
    }
    
}
