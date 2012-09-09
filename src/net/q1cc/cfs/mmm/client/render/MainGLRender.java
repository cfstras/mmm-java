package net.q1cc.cfs.mmm.client.render;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.swing.JOptionPane;
import net.q1cc.cfs.mmm.common.Player;
import net.q1cc.cfs.mmm.common.world.World;
import net.q1cc.cfs.mmm.common.world.WorldOctree;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * @author claus
 */
public class MainGLRender extends Thread {
        
    WorkerTaskPool taskPool = new WorkerTaskPool();
    
    ConcurrentLinkedDeque<GLChunklet> chunksToBuffer = new ConcurrentLinkedDeque<GLChunklet>();
    
    private boolean fullscreen = false;
    private final String windowTitle = "mmm-java";
    private boolean f11 = false;
    private boolean done = false;
    
    Player player;

    //Vec3d cameraPos;
    
    World world;
    float mouseSpeed=1.0f;
    
    int basicShader;
    int attPos;
    int attTex;
    //int attOrient;
    int attColor;
    //int attBlock;
    
    int uniBlockTex;
    int uniProjMat;
    int uniPosChunkMat;
    
    Texture blockTexture;
   
    Matrix4f projMat;
    Matrix4f posChunkMat;
    FloatBuffer projMatB;
    FloatBuffer posChunkMatB;
    
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
        init();
        
        lastFPS = getTime();
        try {
            while (!done) {
                keyboard();
                if(Display.wasResized()){
                    setupProjection();
                }
                render();
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
    
    private void loadShaders() {
        String[] fragSource = null;
        String[] vertSource = null;
        try {
            // load the basic shader
            InputStream fragFile = MainGLRender.class.getResourceAsStream("/net/q1cc/cfs/mmm/client/render/shaders/basic.fsh");
            if(fragFile==null){
                System.out.println("Error: could not find fragment shader");
            } else {
                fragSource = readString(fragFile);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            // load the basic shader
            InputStream vertFile = MainGLRender.class.getResourceAsStream("/net/q1cc/cfs/mmm/client/render/shaders/basic.vsh");
            if(vertFile==null){
                System.out.println("Error: could not find vertex shader");
            } else {
                vertSource = readString(vertFile);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        basicShader = glCreateProgram();
        int vertShader = glCreateShader(GL_VERTEX_SHADER);
        int fragShader = glCreateShader(GL_FRAGMENT_SHADER);
        
        glShaderSource(vertShader, vertSource);
        glCompileShader(vertShader);
        System.out.println("Vertex Shader: "+glGetShaderInfoLog(vertShader, 512));
        glShaderSource(fragShader, fragSource);
        glCompileShader(fragShader);
        System.out.println("Fragment Shader: "+glGetShaderInfoLog(fragShader, 512));
        
        glAttachShader(basicShader, vertShader);
        glAttachShader(basicShader, fragShader);
        glLinkProgram(basicShader);
        System.out.println("Shader Link : "+glGetProgramInfoLog(basicShader, 512));
        
        attPos = glGetAttribLocation(basicShader, "inPos");
        attTex = glGetAttribLocation(basicShader, "inTex");
        //attOrient = glGetAttribLocation(basicShader, "inOrient");
        attColor = glGetAttribLocation(basicShader, "inColor");
        //attBlock = glGetAttribLocation(basicShader, "inBlock");
        
        uniPosChunkMat = glGetUniformLocation(basicShader, "posChunkMat");
        uniProjMat = glGetUniformLocation(basicShader, "projMat");
        uniBlockTex = glGetUniformLocation(basicShader, "blockTex");
    }
    private static String[] readString(InputStream in) throws IOException {
        BufferedReader i = new BufferedReader(new InputStreamReader(in));
        LinkedList<String> l = new LinkedList<String>();
        String s;
        while((s = i.readLine()) != null){
            l.add(s+"\n");
        }
        i.close();
        return l.toArray(new String[l.size()]);
    }
    boolean f5=false;
    private void keyboard() {
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {       // Exit if Escape is pressed
            Mouse.setGrabbed(false);
        }
        if(Display.isCloseRequested()) {                     // Exit if window is closed
            done = true;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_F11) && !f11) {    // Is F11 Being Pressed?
            f11 = true;                                      // Tell Program F1 Is Being Held
            switchMode();                                   // Toggle Fullscreen / Windowed Mode
        }
        if(!Keyboard.isKeyDown(Keyboard.KEY_F11)) {
            f11 = false;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_F5)&&!f5) {
            loadShaders();
            taskPool.add(new WorkerTask(){
            @Override
            public synchronized boolean doWork() {
                prepareAllChunklets(true);
                return true;
            }

            @Override
            public int getPriority() {
                return WorkerTask.PRIORITY_MAX;
            }
        });
            f5=true;
        } else {
            f5=false;
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
        if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
            player.position.y+=5*deltaTime;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_C)){
            player.position.y-=5*deltaTime;
        }
        if(left||right||forward||back){
            player.move(forward, left, right, back, false);
        }
        
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
            
        } else if(Mouse.isButtonDown(0) && !Mouse.isGrabbed()) {
            Mouse.setGrabbed(true);
        }
        
    }

    private void switchMode() {
        fullscreen = !fullscreen;
        try {
            if(fullscreen){
                displayMode=Display.getDesktopDisplayMode();
                
                Display.setDisplayModeAndFullscreen(displayMode);
            } else {
                Display.setFullscreen(fullscreen);
            }
            setupProjection();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        setCamera();
        
        // wireframe mode
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        glUseProgram(basicShader);
        glUniformMatrix4(uniProjMat,false,projMatB);
        glUniformMatrix4(uniPosChunkMat,false,posChunkMatB);
        
        //glActiveTexture(GL_TEXTURE0);
        blockTexture.bind();
        glUniform1i(uniBlockTex,0);
        
        //TODO all the render methods here
        chunkletsRendered=0;
        renderOctree(world.generateOctree);
        //System.out.println("chunklets: "+chunkletsRendered);
        //TODO HUD here
        
        //TODO check if there is time
        //now to buffer some chunks
        if(!chunksToBuffer.isEmpty()){
            for(int i=0;i<10;i++){ //TODO do as many as time allows us to
                if(!chunksToBuffer.isEmpty())
                    bufferChunk(chunksToBuffer.pop());
                else
                    break;
            }
        }
        
        
        return true;
    }
    
    void setCamera() {
        posChunkMat.setIdentity();
        posChunkMat.rotate((float)Math.toRadians(player.rotation.y),
                new Vector3f(1.0f,0.0f,0.0f));
        posChunkMat.rotate((float)Math.toRadians(player.rotation.x),
                new Vector3f(0.0f,1.0f,0.0f));
        posChunkMat.translate(new Vector3f(-player.position.x,-player.position.y,-player.position.z));
        posChunkMat.store(posChunkMatB);
        posChunkMatB.flip();
    }
    
    private void createWindow() {
        try {
            Display.setFullscreen(fullscreen);
            DisplayMode d[] = Display.getAvailableDisplayModes();
            displayMode=d[0];
            for (int i = 1; i < d.length; i++) {
                if (d[i].getWidth() == 800
                    && d[i].getBitsPerPixel() == 32) {
                    displayMode = d[i];
                    break;
                }
            }
            Display.setDisplayMode(displayMode);
            Display.setTitle(windowTitle);
            Display.setVSyncEnabled(true);
            Display.setResizable(true);
            //ContextAttribs attrib = new ContextAttribs(3,0);//.withProfileCore(true);
            //attrib = attrib.withForwardCompatible(true);
            //attrib = attrib.withProfileCompatibility(true);
            //attrib = attrib.withDebug(true);
            PixelFormat pf = new PixelFormat(8,16,0,1);
            Display.create(pf);
            
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
            public synchronized boolean doWork() {
                prepareAllChunklets(false);
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
        glClearColor(0.25f, 0.35f, 0.9f, 0.0f); // Blue Background
        glClearDepth(1); // Depth Buffer Setup
        glEnable(GL_DEPTH_TEST); // Enables Depth Testing
        glDepthFunc(GL_LEQUAL); // The Type Of Depth Testing To Do
        
        
        //glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        
        loadShaders();
        loadTextures();
        posChunkMat = new Matrix4f();
        posChunkMatB = BufferUtils.createFloatBuffer(16);
        setupProjection();
        
    }
    
    private void cleanup() {
        System.out.println("cleaning up...");
        //TODO delete all chunklets from memory
        
        Display.destroy();
        taskPool.finishWorkers();
        System.out.println("finis.");
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
            Display.setTitle(windowTitle+ " FPS: " + fps +" deltaT: "+deltaTime+
                    " rot: "+player.rotation+ " c: "+chunkletsRendered);
            fps = 0; //reset the FPS counter
            lastFPS += 1000; //add one second
        }
        fps++;
    }
    
    void prepareAllChunklets(boolean reload) {
        recursePrepare(world.generateOctree,reload);
    }
    
    void recursePrepare(WorldOctree oc, boolean reload){
        if(oc==null) return;
        if(oc.block!=null){
            if(!reload){
                if(!(oc.block instanceof GLChunklet)){
                    GLChunklet n = new GLChunklet(oc.block);
                    oc.block = n;
                    taskPool.add(n);
                }
            } else if(oc.block instanceof GLChunklet){
                ((GLChunklet)oc.block).built=false;
                taskPool.add((GLChunklet)oc.block);
            }
            
            
        }
        if(!oc.hasSubtrees) return;
        for(WorldOctree c: oc.subtrees){
            recursePrepare(c,reload);
        }
    }
    
    private void bufferChunk(GLChunklet cl) {
        int vboID = glGenBuffers();
        int iboID = glGenBuffers();
        int vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, cl.vertexB, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,iboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER,cl.indexB,GL_STATIC_DRAW);
        glEnableVertexAttribArray(attPos);
        glEnableVertexAttribArray(attColor);
        glEnableVertexAttribArray(attTex);
        glVertexAttribPointer(attPos, 3, GL_FLOAT, false, 4*8, 4*0);
        glVertexAttribPointer(attTex, 2, GL_FLOAT, false, 4*8, 4*3);
        glVertexAttribPointer(attColor, 4, GL_UNSIGNED_BYTE, false, 4*8, 4*5);
        glBindVertexArray(0);
        cl.vboID = vboID;
        cl.vaoID = vaoID;
        cl.iboID = iboID;
        //glBindBuffer(GL_ARRAY_BUFFER,0);
        //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,0);
    }

    private void renderOctree(WorldOctree oc) {
        if(oc==null) return;
        if(oc.block!=null && oc.block instanceof GLChunklet){
            GLChunklet g = (GLChunklet)oc.block;
            if(g.vaoID!=-1 && g.blocksInside>0){
                Matrix4f posChunkMatn = new Matrix4f(posChunkMat);
                posChunkMatn.translate(new Vector3f(g.posX,g.posY,g.posZ));
                posChunkMatn.store(posChunkMatB);
                posChunkMatB.flip();
                glUniformMatrix4(uniPosChunkMat, false, posChunkMatB);
                
                //render this
                glBindVertexArray(g.vaoID);
                glDrawElements(GL_TRIANGLES, g.indCount, GL_UNSIGNED_INT, 0);
                //TODO if rendering is fine, remove this call (leave it to debug)
                glBindVertexArray(0);
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
                System.out.println("GL error: "+Util.translateGLErrorString(err));
                lastError=err;
                lastErrorCount=1;
            } else {
                lastErrorCount++;
                if(lastErrorCount%100==0){
                    System.out.println("repeated "+lastErrorCount+" times.");
                }
            }
        }
    }

    private void setupProjection() {
        displayMode = Display.getDisplayMode();
        glViewport(0, 0, displayMode.getWidth(), displayMode.getHeight());
        System.out.println(displayMode);
        projMat = getProjection(80,
                (float) displayMode.getWidth() / (float) displayMode.getHeight(),
                0.1f, 1000.0f);
        projMatB = BufferUtils.createFloatBuffer(16);
        projMat.store(projMatB);
        projMatB.flip();
    }
    
    public static Matrix4f getProjection(float fov, float aspect, float zNear, float zFar) {
        float sine, cotangent, deltaZ;
        float radians = fov / 2 * (float) Math.PI / 180;

        deltaZ = zFar - zNear;
        sine = (float) Math.sin(radians);

        if ((deltaZ == 0) || (sine == 0) || (aspect == 0)) {
            System.out.println("Error: incorrect parameters for projection");
            return new Matrix4f();
        }
        
        cotangent = (float) Math.cos(radians) / sine;
        FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
        Matrix4f id = new Matrix4f();
        id.store(matrix);
        matrix.put(0 * 4 + 0, cotangent / aspect);
        matrix.put(1 * 4 + 1, cotangent);
        matrix.put(2 * 4 + 2, -(zFar + zNear) / deltaZ);
        matrix.put(2 * 4 + 3, -1);
        matrix.put(3 * 4 + 2, -2 * zNear * zFar / deltaZ);
        matrix.put(3 * 4 + 3, 0);
        matrix.flip();
        id.load(matrix);
        return id;
    }

    private void loadTextures() {
        texL= new TextureLoader();
        try {
            glActiveTexture(GL_TEXTURE0);
            Texture b = texL.getTexture("/png/blocks.png", GL_TEXTURE_2D,GL_RGB,
                    GL_LINEAR,GL_NEAREST);
            //JOptionPane.showMessageDialog(Display.getParent(),"Texture loaded:"+b );
            if(b==null){
                System.out.println("error: texture could not be loaded.");
                JOptionPane.showMessageDialog(Display.getParent(),"texture not found");
            } else {
                blockTexture=b;
                b.bind();
                glGenerateMipmap(GL_TEXTURE_2D);
                glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP);
                glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP);
                glBindTexture(GL_TEXTURE_2D,0);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(Display.getParent(),ex);
        }
    }
    
}
