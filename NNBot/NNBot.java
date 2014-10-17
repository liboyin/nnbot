package NNBot;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

public class NNBot extends AdvancedRobot // NN for nothing new
{
	private static RoundRectangle2D field; // must be initialized in run(), otherwise bot banned
	private static EnemyModel mod;
	private Random rnd=new Random();
	private double dLat=0.3; // delta lateral speed in radian
	private int hitCount, fireCount;
	
	public void run()
	{
		if( field==null )
			field=new RoundRectangle2D.Double( 25.0, 25.0, getBattleFieldWidth()-50.0, getBattleFieldHeight()-50.0, 50.0, 50.0 );
		setAdjustRadarForGunTurn( true );
		setAdjustGunForRobotTurn( true );
		while( true )
			turnRadarRightRadians( Double.POSITIVE_INFINITY );
	}
	
	public void onScannedRobot( ScannedRobotEvent e )
	{
		if( mod==null )
			initModel( e );
		setRandomMove( e );
		Point2D[] preXY=mod.predict( this, e ); // predicted location of the enemy in the next 50 ticks
		paintTargets( Color.red, preXY );
		int tte=(int)(e.getDistance()/19.7); // time to reach the enemy. no more than 49.5 due to round corners
		double mid=((double)tte)/mod.gap;
		int left=(int)Math.floor( mid ), right=left+1; // must not use ceil function for right index, as @mid may me integer
		double x=preXY[left].getX()*(mid-left)+preXY[right].getX()*(right-mid); // linear combination
		double y=preXY[left].getY()*(mid-left)+preXY[right].getY()*(right-mid);
		paintTargets( Color.white, new Point2D.Double( x, y ));
		double arg=toArg( new Point2D.Double( getX(), getY() ), new Point2D.Double( x, y ) );
		setTurnGunRightRadians( Utils.normalRelativeAngle( Utils.normalAbsoluteAngle( arg )-getGunHeadingRadians() ) );
		if( getGunHeat()==0 && Math.abs( getGunTurnRemaining() )<10 )
		{
			fire( 0.1 ); // speed 19.7
			fireCount++;
		}
	}
	
	private void initModel( ScannedRobotEvent e )
	{
		File archive=getDataFile( e.getName()+".em" );
		if( archive==null )
			mod=new EnemyModel( e.getName() );
		else
		{
			try
			{
				ObjectInputStream in=new ObjectInputStream( new FileInputStream( archive ) );
				mod=(EnemyModel)in.readObject();
				in.close();
			}
			catch( Exception e1 )
			{
				mod=new EnemyModel( e.getName() );
			}
		}
	}
	
	private void setRandomMove( ScannedRobotEvent e )
	{
		Point2D myPos=new Point2D.Double( getX(), getY() );
		double absBearing=getHeadingRadians()+e.getBearingRadians();
		Point2D enemyPos=toPoint( myPos, absBearing, e.getDistance() );
		if( rnd.nextDouble()<0.2 ) // flattener factor
			dLat*=-1;
		Point2D dest=toPoint( enemyPos, toArg( enemyPos, myPos )+dLat, e.getDistance()*1.25 ); // avoidance factor
		for( int i=1 ; i<100 && !field.contains( dest ) ; i++ )
			dest=toPoint( enemyPos, toArg( enemyPos, myPos )+dLat, e.getDistance()*(1.25-i/100.0) ); // avoidance factor
		double arg=Utils.normalRelativeAngle( toArg( myPos, dest )-getHeadingRadians() ); // [-Pi, Pi]
		double turn=Math.atan( Math.tan( arg ) ); // [-Pi/2, Pi/2]
		setTurnRightRadians( turn );
		setAhead( myPos.distance( dest )*(arg==turn ? 1 : -1) );
		setMaxVelocity( Math.abs( getTurnRemaining() )>33 ? 0 : 8 ); // break to turn sharply
		setTurnRadarRightRadians( Utils.normalRelativeAngle( absBearing-getRadarHeadingRadians() )*2 ); // radar lock
	}
	
	private void paintTargets( Color c, Point2D... preXY )
	{
		Graphics2D g=getGraphics();
		g.setColor( c );
		for( int i=0 ; i<preXY.length ; i++ )
		{
			int x=(int)(preXY[i].getX());
			int y=(int)(preXY[i].getY());
			g.drawLine( x-5, y, x+5, y );
			g.drawLine( x, y-5, x, y+5 );
		}
	}
	
	@Override
	public void onBulletHit( BulletHitEvent e ) 
	{
		hitCount++;
	}
	
	@Override
	public void onRoundEnded( RoundEndedEvent e )
	{
		System.out.printf( "Success Target Rate: %5.3f\n", ((double)hitCount)/fireCount );
	}
	
	@Override
	public void onBattleEnded( BattleEndedEvent e )
	{
		try
		{
			ObjectOutputStream out=new ObjectOutputStream( new RobocodeFileOutputStream( getDataFile( mod.name+".em" ) ) );
			out.writeObject( mod );
			out.close();
		}
		catch( IOException e1 )
		{
			e1.printStackTrace();
		}
	}
	
	private static Point2D toPoint( Point2D from, double arg, double modulus )
	{
		return new Point2D.Double( from.getX()+Math.sin(arg)*modulus, from.getY()+Math.cos(arg)*modulus );
	}
	
	private static double toArg( Point2D from, Point2D to )
	{
		return Math.atan2( to.getX()-from.getX(), to.getY()-from.getY() );
	}
}
