package ru.fumycat.cp;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Context context;

    private Building mBuilding;
    private GLCircleCarriage mCarriageBack, mCarriageFront;
    private GLCylinder mCylinder;
    private Cuboid mCuboid;
    private CuboidTexturesWIP mCuboidBuilding;
    private CuboidTexturesWIP mCuboidFloor;
    private CuboidTexturesWIP mCuboidDoor;

    private float[] mvMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private float[] mvpMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float[16];

    private final float[] rotationMatrix = new float[16];

    public final float cubeAngel = 0f;

    public volatile float fi = 30f;
    public volatile float tetta = 120f;
    public volatile float radius = -9;

    private int mTextureDataHandleBrick;
    private int mTextureDataHandleMetal;
    private int mTextureDataHandleConcrete;
    private int mTextureDataHandleDoor;
    private int mTextureDataHandlerClock;

    public MyGLRenderer(Context context) {
        this.context = context;
    }

    public float getFi() {
        return fi;
    }

    public void setFi(float fi) {
        this.fi = fi;
    }

    public float getTetta() {
        return tetta;
    }

    public void setTetta(float tetta) {
        this.tetta = tetta;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_FRONT);

        mTextureDataHandleBrick = Utils.loadTexture(context, R.drawable.stone_wall_ext);
        mTextureDataHandleMetal = Utils.loadTexture(context, R.drawable.rasty_metal);
        mTextureDataHandleConcrete = Utils.loadTexture(context, R.drawable.concrete_floor);
        mTextureDataHandleDoor = Utils.loadTexture(context, R.drawable.door);
        mTextureDataHandlerClock = Utils.loadTexture(context, R.drawable.clock);

        GLCircleCarriage.DrawCtrl ctrl = new GLCircleCarriage.DrawCtrl(){
            @Override
            public void shaders_free() {

            }

            @Override
            public void shaders_init(int mProgramHandle) {
                float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
                GLES20.glUniform4fv(GLES20.glGetUniformLocation(mProgramHandle, "u_Color"), 1, color, 0);
            }
        };


        float[] cubeColorData = new float[36 * 4];
        for (int i = 0; i < 36 * 4; i++) {
            if (i % 4 == 2) {
                cubeColorData[i] = 1.0f;
            } else {
                cubeColorData[i] = 1.0f;
            }
        }

        mBuilding = new Building();
        mCarriageBack = new GLCircleCarriage(context, 0, 0,-3.9f, 1);
        mCarriageFront = new GLCircleCarriage(context,0, 0,3.9f, 1, ctrl, null, Utils.readStringFromResource(context, R.raw.basic_fragment));
        mCylinder = new GLCylinder(context, 0, 0,0, 0.5f,1f, mTextureDataHandleMetal);
        mCuboid = new Cuboid(context, 1.5f, 0, 0, 2, 1,2);
        mCuboidBuilding = new CuboidTexturesWIP(context,
                0f, 5f, 16f,
                24f, 8f, 12f,
                mTextureDataHandleBrick, cubeColorData);
        mCuboidFloor = new CuboidTexturesWIP(context,
                0f, 0.5f, 14f,
                32f, 1f, 24f,
                mTextureDataHandleConcrete, cubeColorData);
        mCuboidDoor = new CuboidTexturesWIP(context,
                0f, 3f, 9.9f,
                3f, 4f, 0.1f,
                mTextureDataHandleDoor, cubeColorData);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 80);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // GLES20.glCullFace(GLES20.GL_FRONT);
        // Camera
        double tettaRad = Math.toRadians(tetta);
        double fiRad = Math.toRadians(fi);
        float decZ = (float) (radius * Math.sin(tettaRad) * Math.cos(fiRad));
        float decX = (float) (radius * Math.sin(tettaRad) * Math.sin(fiRad));
        float decY = (float) (radius * Math.cos(tettaRad));

        Matrix.setLookAtM(viewMatrix, 0,
                decX, decY, decZ,
                0, 0, 0f,
                0f, 1.0f, 0.0f);

        // Calculate
        // Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, mvMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // draw
        // mBuilding.draw(finalMatrixCube);
        //mCarriageBack.draw(mvpMatrix);
        //mCarriageFront.draw(mvpMatrix);
        mCylinder.draw(mvpMatrix);

        mCuboid.draw(mvpMatrix);

        GLES20.glCullFace(GLES20.GL_BACK);
        mCuboidBuilding.draw(projectionMatrix, decX, decY, decZ);
        mCuboidFloor.draw(projectionMatrix, decX, decY, decZ);
        mCuboidDoor.draw(projectionMatrix, decX, decY, decZ);
        GLES20.glCullFace(GLES20.GL_FRONT);
    }
}
