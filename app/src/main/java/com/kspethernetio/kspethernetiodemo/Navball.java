package com.kspethernetio.kspethernetiodemo;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.TypedValue;

/**
 * Navball class.
 * Set roll, pitch and yaw angles and get a navball bitmap.
 * The bitmap contains Vector nodes and optional prograde, maneuver and target node.
 */
public class Navball
{
    private Paint lineColor, lineColorNorth, topSideColor, bottomSideColor, orange;

    private double roll=0, pitch=0, yaw=0;
    private double progradepitch=0, progradeyaw=0;
    private double maneuverpitch=0, maneuveryaw=0;
    private double targetpitch=0, targetyaw=0;

    private boolean drawManeuver = false;
    private boolean drawTarget = false;
    private boolean hideRadialNormal = false;

    /**
     * Create new navball.
     * Initialize coloring.
     */
    public Navball()
    {
        //Initialize colors

        lineColor =  new Paint();
        lineColor.setStyle(Paint.Style.FILL);
        lineColor.setAntiAlias(true);
        lineColor.setARGB(255,240,240,240);

        lineColorNorth =  new Paint();
        lineColorNorth.setStyle(Paint.Style.FILL);
        lineColorNorth.setAntiAlias(true);
        lineColorNorth.setARGB(255,255,100,100);

        topSideColor =  new Paint();
        topSideColor.setStyle(Paint.Style.FILL);
        topSideColor.setAntiAlias(false);
        topSideColor.setARGB(255,75,175,200);

        bottomSideColor =  new Paint();
        bottomSideColor.setStyle(Paint.Style.FILL);
        bottomSideColor.setAntiAlias(false);
        bottomSideColor.setARGB(255,150,100,50);

        orange =  new Paint();
        orange.setStyle(Paint.Style.FILL);
        orange.setAntiAlias(true);
        orange.setARGB(255,255,200,0);
    }

    /**
     * Set roll, pitch and yaw angles
     * @param r Roll
     * @param p Pitch
     * @param y Yaw
     */
    public void set(double r, double p, double y)
    {
        roll=r;
        pitch=p;
        yaw=y;
    }

    /**
     * Set prograde vector
     * @param p Pitch
     * @param y Yaw
     */
    public void setPrograde(double p, double y)
    {
        progradepitch=p;
        progradeyaw=y;
    }

    /**
     * Set maneuver vector
     * @param p Pitch
     * @param y Yaw
     */
    public void setManeuver(double p, double y)
    {
        maneuverpitch=p;
        maneuveryaw=y;
    }

    /**
     * Set target vector
     * @param p Pitch
     * @param y Yaw
     */
    public void setTarget(double p, double y)
    {
        targetpitch=p;
        targetyaw=y;
    }

    /**
     * Hide normal and radial vector
     * @param b True = hide
     */
    public void hideRadialNormal(boolean b)
    {
        hideRadialNormal = b;
    }
    /**
     * Hide maneuver vector
     * @param b True = hide
     */
    public void setManeuverDisable(boolean b)
    {
        drawManeuver = !b;
    }
    /**
     * Hide target vector
     * @param b True = hide
     */
    public void setTargetDisable(boolean b)
    {
        drawTarget = !b;
    }

    /**
     * Render Navball bitmap.
     * Currently very CPU intense. To be optimized.
     * @param w Wide in px
     * @param h Height in px
     * @param activeActivity Context to get ressource images
     * @return Navball bitmap
     */
    public Bitmap render(int w, int h, Activity activeActivity)
    {
        initScale(w);

        //Rotation Matrix for the view angle
        Matrix3x3 view = Matrix3x3.rpy(-Math.PI/2.0, 0, -Math.PI/2.0);

        //Navball rotation Matrix
        Matrix3x3 rot = Matrix3x3.rpy(-roll, pitch, -yaw);
        rot = view.mult(rot);


        //Create new bitmap to render
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);



        //Draw topside
        for(int i=95;i<=180;i+=5)
        {
            Vector prevRot1 = null;
            Vector prevRot2 = null;
            for(int j=0;j<=360;j+=5)
            {
                Vector cur1 = Vector.point(Math.PI - Math.PI * (i-5)/180.0, Math.PI * j/180.0);
                Vector cur2 = Vector.point(Math.PI - Math.PI * i/180.0, Math.PI * j/180.0);
                Vector curRot1  = rot.mult(cur1);
                Vector curRot2 = rot.mult(cur2);

                if(prevRot1!=null && prevRot2!=null)
                {
                    if(curRot1.z > 0 && prevRot1.z > 0 && curRot2.z > 0 && prevRot2.z > 0)
                    {
                        drawRect(canvas, prevRot1, prevRot2, curRot2, curRot1, topSideColor);
                    }
                }

                prevRot1 = curRot1;
                prevRot2 = curRot2;
            }
        }

        //Draw bottomside
        for(int i=5;i<=90;i+=5)
        {
            Vector prevRot1 = null;
            Vector prevRot2 = null;
            for(int j=0;j<=360;j+=5)
            {
                Vector cur1 = Vector.point(Math.PI - Math.PI * (i-5)/180.0, Math.PI * j/180.0);
                Vector cur2 = Vector.point(Math.PI - Math.PI * i/180.0, Math.PI * j/180.0);
                Vector curRot1 = rot.mult(cur1);
                Vector curRot2 = rot.mult(cur2);

                if(prevRot1!=null && prevRot2!=null)
                {
                    if(curRot1.z > 0 && prevRot1.z > 0 && curRot2.z > 0 && prevRot2.z > 0)
                    {
                        drawRect(canvas, prevRot1, prevRot2, curRot2, curRot1, bottomSideColor);
                    }
                }

                prevRot1 = curRot1;
                prevRot2 = curRot2;
            }
        }



        //Draw lines

        Vector prevRot = null;

        //Horizontal 10deg lines
        for(int i=0;i<=180;i+=10)
        {
            prevRot = null;
            for(int j=0;j<=360;j+=5)
            {
                Vector cur = Vector.point(Math.PI * i/180.0, Math.PI * j/180.0);
                Vector curRot  = rot.mult(cur);

                if(prevRot!=null)
                {
                    if(curRot.z > 0 && prevRot.z > 0) drawLine(canvas, curRot, prevRot, lineColor);
                }

                prevRot = curRot;
            }
        }

        //Vertical EastWest line
        prevRot=null;
        for(int i=0;i<=360;i+=5)
        {
            Vector cur = Vector.point(Math.PI * i/180.0, 0);
            Vector curRot = rot.mult(cur);

            if(prevRot!=null)
            {
                if(curRot.z > 0 && prevRot.z > 0) drawLine(canvas, curRot, prevRot, lineColor);
            }

            prevRot = curRot;
        }

        //Vertical North line
        prevRot = null;
        for(int i=0;i<=180;i+=5)
        {
            Vector cur = Vector.point(Math.PI * i/180.0, Math.PI/2.0);
            Vector curRot = rot.mult(cur);

            if(prevRot!=null)
            {
                if(curRot.z > 0 && prevRot.z > 0) drawLine(canvas, curRot, prevRot, 4, lineColorNorth);
            }

            prevRot = curRot;
        }

        //Vertical South line
        prevRot = null;
        for(int i=180;i<=360;i+=5)
        {
            Vector cur = Vector.point(Math.PI * i/180.0, Math.PI/2.0);
            Vector curRot = rot.mult(cur);

            if(prevRot!=null)
            {
                if(curRot.z > 0 && prevRot.z > 0) drawLine(canvas, curRot, prevRot, lineColor);
            }

            prevRot = curRot;
        }












        //Draw orbital vectors
        Vector progradeVect = Vector.point(Math.PI/2-progradepitch,Math.PI/2+progradeyaw);
        Vector progradeVectRot = rot.mult(progradeVect);
        Vector retroradeVectRot = new Vector(-progradeVectRot.x,-progradeVectRot.y,-progradeVectRot.z);

        Vector radialinVect = Vector.point(-progradepitch,Math.PI/2+progradeyaw);
        Vector radialinVectRot = rot.mult(radialinVect);
        Vector radialoutVectRot = new Vector(-radialinVectRot.x,-radialinVectRot.y,-radialinVectRot.z);

        Vector antinormalVect = progradeVect.crossproduct(radialinVect);
        Vector antinormalVectRot = rot.mult(antinormalVect);
        Vector normalVectRot = new Vector(-antinormalVectRot.x,-antinormalVectRot.y,-antinormalVectRot.z);

        drawVectorBitmap(canvas,progradeVectRot,R.drawable.vect_prograde,activeActivity);
        drawVectorBitmap(canvas,retroradeVectRot,R.drawable.vect_retrograde,activeActivity);
        if(!hideRadialNormal)
        {
            drawVectorBitmap(canvas, normalVectRot, R.drawable.vect_normal, activeActivity);
            drawVectorBitmap(canvas, antinormalVectRot, R.drawable.vect_antinormal, activeActivity);
            drawVectorBitmap(canvas, radialinVectRot, R.drawable.vect_radialin, activeActivity);
            drawVectorBitmap(canvas, radialoutVectRot, R.drawable.vect_radialout, activeActivity);
        }

        //Draw Maneuver
        if(drawManeuver)
        {
            Vector maneuverVect = Vector.point(Math.PI/2-maneuverpitch,Math.PI/2+maneuveryaw);
            Vector maneuverVectRot = rot.mult(maneuverVect);
            drawVectorBitmap(canvas,maneuverVectRot,R.drawable.vect_maneuver,activeActivity);
        }

        //Draw Target
        if(drawTarget)
        {
            Vector targetVect = Vector.point(Math.PI/2-targetpitch,Math.PI/2+targetyaw);
            Vector targetVectRot = rot.mult(targetVect);
            Vector antitargetVectRot = new Vector(-targetVectRot.x,-targetVectRot.y,-targetVectRot.z);
            drawVectorBitmap(canvas,targetVectRot,R.drawable.vect_target,activeActivity);
            drawVectorBitmap(canvas,antitargetVectRot,R.drawable.vect_targetr,activeActivity);
        }





        Vector v1 = new Vector(-0.5, 0, 0);
        Vector v2 = new Vector(-0.2, 0, 0);
        drawLine(canvas,v1,v2,3, orange);
        v1 = new Vector(0, 0.2, 0);
        drawLine(canvas,v1,v2,3,orange);
        v2 = new Vector(0.2, 0, 0);
        drawLine(canvas,v1,v2,3,orange);
        v1 = new Vector(0.5, 0, 0);
        drawLine(canvas,v1,v2,3,orange);
        v1 = new Vector(0, 0, 0);
        drawPoint(canvas, v1, 10, orange);

        return bmp;
    }

    /**
     * Draw vector bitmap on canvas.
     * @param g Canvas
     * @param v Position
     * @param drawable Drawable ID
     * @param activeActivity Context to load drawable ressource
     */
    private void drawVectorBitmap(Canvas g, Vector v, int drawable, Activity activeActivity)
    {
        if(v.z>0.05)
        {
            Bitmap progradeBmp = BitmapFactory.decodeResource(activeActivity.getResources(), drawable);
            Rect source = new Rect(0, 0, progradeBmp.getWidth(), progradeBmp.getHeight());
            int x = scale(v.x);
            int y = scale(v.y);
            float twentyfourDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, activeActivity.getResources().getDisplayMetrics());
            Rect target = new Rect((int) (x - twentyfourDp / 2), (int) (y - twentyfourDp / 2), (int) (x + twentyfourDp / 2), (int) (y + twentyfourDp / 2));
            g.drawBitmap(progradeBmp, source, target, new Paint());
        }
    }

    /**
     * Draw point on canvas
     * @param g Canvas
     * @param v Position
     * @param d Diameter in px
     * @param p Color
     */
    private void drawPoint(Canvas g, Vector v, int d, Paint p)
    {
        int r = (int)((double)d*(v.z+3.0f)/8.0f);
        int x = scale(v.x)-r;
        int y = scale(v.y)-r;
        g.drawCircle(x, y, r, p);
    }

    /**
     * Draw line on canvas
     * @param g Canvas
     * @param v1 Start point
     * @param v2 Target point
     * @param p Color
     */
    private void drawLine(Canvas g, Vector v1, Vector v2, Paint p)
    {
        int x1 = scale(v1.x);
        int y1 = scale(v1.y);
        int x2 = scale(v2.x);
        int y2 = scale(v2.y);
        g.drawLine(x1, y1, x2, y2, p);
    }

    /**
     * Draw line with given strength on canvas
     * @param g Canvas
     * @param v1 Start point
     * @param v2 Target point
     * @param d Line strength
     * @param p Color
     */
    private void drawLine(Canvas g, Vector v1, Vector v2, int d, Paint p)
    {
        int x1 = scale(v1.x);
        int y1 = scale(v1.y);
        int x2 = scale(v2.x);
        int y2 = scale(v2.y);
        for(int i=0;i<d;i++)
            for(int j=0;j<d;j++)
                g.drawLine(x1+i-d/2, y1+j-d/2, x2+i-d/2, y2+j-d/2, p);
    }

    /**
     * Draw rectangle on canvas
     * @param g Canvas
     * @param v1 Corner
     * @param v2 Corner
     * @param v3 Corner
     * @param v4 Corner
     * @param p Color
     */
    private void drawRect(Canvas g, Vector v1, Vector v2, Vector v3, Vector v4, Paint p)
    {
        int x[] = new int[4];
        int y[] = new int[4];
        x[0] = scale(v1.x);
        y[0] = scale(v1.y);
        x[1] = scale(v2.x);
        y[1] = scale(v2.y);
        x[2] = scale(v3.x);
        y[2] = scale(v3.y);
        x[3] = scale(v4.x);
        y[3] = scale(v4.y);
        Path path = new Path();
        path.moveTo(x[0], y[0]);
        path.lineTo(x[1], y[1]);
        path.lineTo(x[2], y[2]);
        path.lineTo(x[3], y[3]);
        path.lineTo(x[0], y[0]);
        g.drawPath(path, p);
    }

    //Scaling factor, is initializen on new renderBitmap
    private int scaleW=1;

    /**
     * Input -1.0f..1.0f -> 0 .. getWidth()
     * @param p Input
     * @return Scaled output
     */
    private int scale(double p)
    {
        return (int)(((scaleW-1)*       (((p+1.0f)/2.0f)*1.0-0.0)));
    }

    /**
     * Initialize scaling
     * @param r Bitmap width
     */
    private void initScale(int r)
    {
        scaleW = r;
    }


}
