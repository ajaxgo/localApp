package base;


public class josefulCircle {
	public void finalWin(int total,int count){
		int finalOne = 0;
		for(int i =2;i<=total;i++){
			finalOne = (finalOne+count)%i;
		}
		System.out.println(finalOne+1);
	}
	
	public static void main(String[] args) {
		josefulCircle obj = new josefulCircle();
		obj.finalWin(20, 4);
		obj.finalWinSimu(20, 4);
	}
	
	
	public void finalWinSimu(int total, int count){
		int leftNum = total;
		int[] allPeople = new int[total];
		int nextGoIndex = -1;
		for(int i=0;i<allPeople.length;i++){
			allPeople[i]=i;
		}
		while(leftNum>1){
			for(int i=1;i<=count;i++){
				nextGoIndex = getNextIndex(nextGoIndex,total);
				while(allPeople[nextGoIndex]<0){
					nextGoIndex= getNextIndex(nextGoIndex,total);
				}
			}
			allPeople[nextGoIndex]=-1;
			leftNum--;
		}
		for(int i=0;i<total;i++){
			if(allPeople[i]>=0){
				System.out.println(allPeople[i]+1);
			}
		}
	}
	
	public int getNextIndex(int lastGoIndex,int total){
		lastGoIndex++;
		if(lastGoIndex>=total){
			lastGoIndex=0;
		}
		return lastGoIndex;
	}
	
}

