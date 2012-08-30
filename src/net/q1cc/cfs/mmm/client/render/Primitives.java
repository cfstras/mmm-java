
package net.q1cc.cfs.mmm.client.render;


import java.nio.FloatBuffer;
import net.q1cc.cfs.mmm.common.world.Block;
import net.q1cc.cfs.mmm.common.math.Quaternionf;
import net.q1cc.cfs.mmm.common.math.Vec3d;
import net.q1cc.cfs.mmm.common.math.Vec3f;
import net.q1cc.cfs.mmm.common.world.WorldOctree;
import org.lwjgl.LWJGLException;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;
import org.lwjgl.util.glu.GLU;
/**
 *
 * @author claus
 */
public class Primitives {
    
    static int cubeList;
 
    
    static void renderCubeAlternative(Vec3d position,Vec3f size,ReadableColor color,Vec3f rotationAxis,float angle) {
        //glPushMatrix();                          // Reset The Current Modelview Matrix
        glTranslated(position.x,position.y, position.z);              // Move
        glRotatef(angle, rotationAxis.x,rotationAxis.y, rotationAxis.z);               // Rotate The Quad On The X axis
        glColor4f(color.getRed()/255f,color.getGreen()/255f,color.getBlue()/255f, color.getAlpha()/255f);
        
        glBegin(GL_QUADS);                        // Draw A Quad
            //use vertex buffer object here // dump whole chunk in VBO
        glEnd();
        
        glTranslated(-position.x,-position.y, -position.z);
        //glPopMatrix();
    }
    
    static void renderCubeList(Vec3d position,Vec3f size,ReadableColor color,Vec3f rotationAxis,float angle) {
        glPushMatrix();                          // Reset The Current Modelview Matrix
        glTranslated(position.x,position.y, position.z);              // Move
        glRotatef(angle, rotationAxis.x,rotationAxis.y, rotationAxis.z);               // Rotate The Quad On The X axis
        glColor4f(color.getRed()/255f,color.getGreen()/255f,color.getBlue()/255f, color.getAlpha()/255f);
        glScalef(size.x, size.y, size.z);
        glCallList(cubeList);
        glPopMatrix();
    }
    
}
