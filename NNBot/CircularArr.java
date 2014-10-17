package NNBot;

public class CircularArr<T>
{
	private T[] arr;
	private int size, cursor;
	
	@SuppressWarnings( "unchecked" )
	public CircularArr( int size )
	{
		arr=(T[])(new Object[size]);
		this.size=size;
	}
	
	public void add( T e )
	{
		if( arr[0]==null ) // if the array is empty, fill it
		{
			for( int i=0 ; i<size ; i++ )
				arr[i]=e;
		}
		if( ++cursor==size )
			cursor=0;
		arr[cursor]=e;
	}
	
	public T get( int offset )
	{
		return arr[(cursor-offset+size)%size];
	}
}
