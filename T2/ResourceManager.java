import java.util.*;


class ResourceManager{
	static private int state;
	static private final int working =3;	//utilizando o recurso
	static private final int waiting = 4;	//querendo utilizar o recurso
	static private final int standing = 5;	//não precisa do recuros

	private int clock;
	private int quantAck;
	private int quantNack;
	private int quant;
  	private LinkedList<Integer> sndList;

	public ResourceManager(int clock, int quant) {
		state = standing;
		this.clock = clock;
		quantAck = quant;
		quantNack = 0;
		this.quant = quant;
    sndList = new LinkedList<Integer>();
	}

	public int getState(){
		return state;
	}

	public void setState(int state){
		this.state = state;
	}

	public int RcvAns(boolean isAck){
			if(isAck)
				quantAck--;
			else
				quantNack++;

			if(((quant-quantAck) + quantNack) > quant)
				quantNack --;

			return quantAck;
    }

  public int Nack(){
  	return quantNack;
  }

  public void add(int pid){
    sndList.add(pid);
  }

  public int pop(){
    int pid = sndList.remove();
    return pid;
  }

  public int size(){
    return sndList.size();
  }
}
