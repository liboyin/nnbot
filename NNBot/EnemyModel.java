package NNBot;

import java.awt.geom.Point2D;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import NNBot.ANN.ANN;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class EnemyModel implements Externalizable
{
	private static final long serialVersionUID=2002899937133789645L;
	public final int gap=5;
	public String name; // name of enemy
	private ANN ann;
	private CircularArr<Snapshot> obs=new CircularArr<>( 101 ); //50 snapshots to each direction, one in the middle
	
	public EnemyModel(){} // for serialization
	
	public EnemyModel( String name )
	{
		this.name=name;
		ann=new ANN( new int[]{ 60,60,20 } ); // input/output 10 snapshots, 6/2 entries each
	}
	
	public Point2D[] predict( AdvancedRobot me, ScannedRobotEvent e ) // return the expected X/Y of the enemy
	{
		obs.add( new Snapshot( me, e ) );
		double[] farPast=new double[60]; // training input
		for( int i=0 ; i<10 ; i++ )
			System.arraycopy( obs.get( 100-i*5 ).toInput(), 0, farPast, i*6, 6 );
		double[] nearPast=new double[20]; // training output
		for( int i=0 ; i<10 ; i++ )
			System.arraycopy( obs.get( 45-i*5 ).toOutput(), 0, nearPast, i*2, 2 );
		ann.step( new double[][]{ farPast, nearPast } ); // train the neural network
		double[] now=new double[60]; // prediction input
		for( int i=0 ; i<10 ; i++ )
			System.arraycopy( obs.get( 45-i*5 ).toInput(), 0, now, i*6, 6 );
		double[] preMove=ann.feedForward( now ); // predicted movement
		Point2D[] preXY=new Point2D[11]; // an extra cell for the current location
		preXY[0]=toPoint( new Point2D.Double( me.getX(), me.getY() ), me.getHeadingRadians()+e.getBearingRadians(), e.getDistance() );
		for( int i=1 ; i<preXY.length ; i++ )
			preXY[i]=toPoint( preXY[i-1], preMove[(i-1)*2+1]*Math.PI*2, (preMove[(i-1)*2]*16-8)*gap ); // @eVel and @eHeading, respectively
		return preXY;
	}
	
	private static Point2D toPoint( Point2D from, double arg, double modulus )
	{
		return new Point2D.Double( from.getX()+Math.sin(arg)*modulus, from.getY()+Math.cos(arg)*modulus );
	}
	
	@Override
	public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException
	{
		name=(String)in.readObject();
		ann=(ANN)in.readObject();
	}

	@Override
	public void writeExternal( ObjectOutput out ) throws IOException
	{
		out.writeObject( name );
		out.writeObject( ann );
	}
}
