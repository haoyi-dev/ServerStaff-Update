package camchua.serverstaff;

import java.util.HashMap;

public class StaffData {
	
	public static HashMap<String, StaffData> data = new HashMap<String, StaffData>();
	
	private String name;
	private int onlinetime;
	private int vote;
	private int report;
	
	public StaffData(String name, int onlinetime, int vote, int report) {
		this.name = name;
		this.onlinetime = onlinetime;
		this.vote = vote;
		this.report = report;
		data.put(name, this);
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getOnlineTime() {
		return this.onlinetime;
	}
	
	public int getVote() {
		return this.vote;
	}
	
	public int getReport() {
		return this.report;
	}
	
	public void setOnlineTime(int value) {
		onlinetime = value;
	}
	
	public void setVote(int value) {
		vote = value;
	}
	
	public void setReport(int value) {
		report = value;
	}

}
