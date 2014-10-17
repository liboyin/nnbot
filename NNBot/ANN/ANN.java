package NNBot.ANN;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ANN implements Externalizable
{
	private static final long serialVersionUID=2461517332277837465L;
	private Layer[] arr;
	
	public ANN(){} // for serialization
	
	public ANN( int[] conf )
	{
		arr=new Layer[conf.length];
		arr[0]=new Layer( conf[0], null );
		for( int i=1 ; i<conf.length ; i++ )
			arr[i]=new Layer( conf[i], arr[i-1] );
	}
	
	public void step( double[][] dat ) // train the ANN
	{
		feedForward( dat[0] );
		backPropagate( dat[1] );
	}
	
	public double[] feedForward( double[] input )
	{
		arr[0].result=input;
		for( int i=1 ; i<arr.length ; i++ )
			arr[i].feedForward();
		return arr[arr.length-1].result;
	}
	
	private void backPropagate( double[] target )
	{
		Layer outlLayer=arr[arr.length-1];
		double[] outGrad=outlLayer.grad, outResult=outlLayer.result;
		for( int i=0 ; i<outlLayer.neuNum ; i++ )
			outGrad[i]=(target[i]-outResult[i])*outResult[i]*(1-outResult[i]); // sig'(x)=sig(x)*(1-sig(x))
		outlLayer.updWeight();
		for( int i=arr.length-2 ; i>=1 ; i-- )
		{
			arr[i].updGradient();
			arr[i].updWeight();
		}
	}
	
	@Override
	public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException
	{
		arr=(Layer[])in.readObject();
		for( int i=1 ; i<arr.length ; i++ ) // set @prev pointers
			arr[i].prev=arr[i-1];
		for( int i=0 ; i<arr.length-1 ; i++ ) // set @next pointers
			arr[i].next=arr[i+1];
	}
	
	@Override
	public void writeExternal( ObjectOutput out ) throws IOException
	{
		out.writeObject( arr );
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb=new StringBuilder();
		for( int i=1 ; i<arr.length-1 ; i++ )
			sb.append( "Layer " ).append( i ).append( ":\n" ).append( arr[i] ).append( '\n' );
		sb.append( "Layer " ).append( arr.length-1 ).append( ":\n" ).append( arr[arr.length-1] );
		return sb.toString();
	}
}
