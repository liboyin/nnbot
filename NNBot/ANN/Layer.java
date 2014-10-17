package NNBot.ANN;

import java.io.Serializable;
import java.util.Random;

public class Layer implements Serializable
{
	private static final long serialVersionUID=-2256794071746196317L;
	private final double eta=1.0, alpha=0.05; // eta=learning rate, alpha=momentum
	public int neuNum;
	public transient Layer prev, next;
	public double[] bias, result, grad;
	public double[][] weight, dWeight; // from*to
	
	public Layer(){} // for serialization
	
	public Layer( int neuNum, Layer prev )
	{
		Random rnd=new Random();
		this.neuNum=neuNum;
		if( prev==null ) // the input layer does not need anything
			return;
		this.prev=prev;
		prev.next=this;
		bias=new double[neuNum];
		for( int i=0 ; i<neuNum ; i++ )
			bias[i]=rnd.nextDouble()*2-1; // random initial weight between 0.0 and 1.0
		result=new double[neuNum];
		grad=new double[neuNum];
		weight=new double[prev.neuNum][neuNum];
		for( int i=0 ; i<weight.length ; i++ ) // traverse through the weight matrix
		{
			for( int j=0 ; j<neuNum ; j++ )
				weight[i][j]=rnd.nextDouble()*2-1; // random initial weight between 0.0 and 1.0
		}
		dWeight=new double[prev.neuNum][neuNum];
	}
	
	public void feedForward()
	{
		for( int i=0 ; i<neuNum ; i++ )
			// reset result vector to 0
			result[i]=0;
		for( int i=0 ; i<weight.length ; i++ ) // matrix multiplication: result=prev.result*weight
		{
			for( int j=0 ; j<neuNum ; j++ )
				result[j]+=prev.result[i]*weight[i][j];
		}
		for( int i=0 ; i<neuNum ; i++ )
			// apply bias and sigmoid transformation
			result[i]=(double)(1/(1+Math.exp( -result[i]-bias[i] ))); // sigmoid(x)=1/(1+e^(-x))
	}
	
	public void updGradient()
	{
		for( int i=0 ; i<neuNum ; i++ )
		{
			grad[i]=0;
			for( int j=0 ; j<next.neuNum ; j++ )
				// matrix multiplication: grad=next.grad*transpose(next.weight)
				grad[i]+=next.grad[j]*next.weight[i][j];
			grad[i]*=result[i]*(1-result[i]);
		}
	}
	
	public void updWeight()
	{
		double[] dBias=new double[neuNum];
		for( int i=0 ; i<neuNum ; i++ ) // update bias vector
		{
			dBias[i]=eta*grad[i];
			bias[i]+=dBias[i];
		}
		for( int i=0 ; i<weight.length ; i++ ) // update weight matrix
		{
			for( int j=0 ; j<neuNum ; j++ )
			{
				weight[i][j]+=alpha*dWeight[i][j]; // add a small proportion of last update
				dWeight[i][j]=dBias[j]*prev.result[i];
				weight[i][j]+=dWeight[i][j];
			}
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb=new StringBuilder();
		sb.append( "weight matrix:\n" );
		for( int i=0 ; i<weight.length ; i++ )
		{
			for( int j=0 ; j<neuNum ; j++ )
				sb.append( String.format( "%5.3f ", weight[i][j] ) );
			sb.append( '\n' );
		}
		sb.append( "bias vector:\n" );
		for( int i=0 ; i<neuNum ; i++ )
			sb.append( String.format( "%5.3f ", bias[i] ) );
		return sb.toString();
	}
}
