package soccerviz;
import java.io.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.util.*;

public class SoccerBaseCrawler {
	HashMap<String, String> ids;
	String url = "http://www.soccerbase.com/";
	
	SoccerBaseCrawler(){
		ids = new HashMap<String, String>();
		ids.put("team", "team_id");
		ids.put("tournament", "tourn_id");
		ids.put("season", "season_id");
		ids.put("player", "player_id");
	}
	
	String getTournaments(){
		StringBuffer sb = new StringBuffer();
		int[] tournaments = {1435, 1386, 1304, 1159, 939, 1, 2, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117};
		int count = 0;
		for(int i = 0; i < tournaments.length; i++){
			try {
				String cur = url+"tournaments/tournament.sd?tourn_id="+tournaments[i];
				Connection con = Jsoup.connect(cur).header("Cache-Control", "no-cache").header("Cache-Store", "no-store");
				Document doc = con.get();
				Elements table = doc.getElementsByTag("table");
				//System.out.println(table.get(0).childNodeSize());
				if(i == 5){
					System.out.println(doc);
				}
				hangup();
			} catch (Exception e) {
				System.out.println("error at: "+i);
				System.out.println(e.getMessage());
				System.exit(0);
				e.printStackTrace();
			}
		}
		System.out.println(count);
		return null;
		
	}
	
	void writeFile(String filename, char sep, boolean app){
		
	}
	
	void hangup() throws InterruptedException{
		Thread.sleep(1000);
	}
}
