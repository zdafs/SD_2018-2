
class ResourceManager{
	static private int state;
	static private final int working = 0;	//utilizando o recurso
	static private final int waiting = 1;	//querendo utilizar o recurso
	static private final int standing = 2;	//não precisa do recuros

	private int clock;
	private int quantAck;
	private int quantNack;
	private int quant;

	public ResourceManager(int clock, int quant) {
		state = standing;
		this.clock = clock;
		quantAck = quant;
		quantNack = 0;
		this.quant = quant;
	}
	
	public int state(){
		return state;
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
}
	
