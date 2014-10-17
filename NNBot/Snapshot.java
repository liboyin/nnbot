package NNBot;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class Snapshot
{
	public double dist, bearing, mVel, eVel, mHeading, eHeading;
	
	public Snapshot( AdvancedRobot me, ScannedRobotEvent e )
	{
		dist=e.getDistance()/1000; // normalized against the diagonal length of a standard 800*600 battlefield
		bearing=Utils.normalAbsoluteAngle( me.getHeadingRadians()+e.getBearingRadians() )/(Math.PI*2); // normalized against 2*Pi
		mVel=(me.getVelocity()+8)/16; // normalized against 16 pixels/tick
		eVel=(e.getVelocity()+8)/16;
		mHeading=me.getHeadingRadians()/(Math.PI*2); // normalized against 2*Pi
		eHeading=e.getHeadingRadians()/(Math.PI*2);
	}
	
	public double[] toInput()
	{
		return new double[]{ dist, bearing, mVel, eVel, mHeading, eHeading };
	}
	
	public double[] toOutput()
	{
		return new double[]{ eVel, eHeading };
	}
}
