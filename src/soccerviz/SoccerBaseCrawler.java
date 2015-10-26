package soccerviz;
import java.io.*;
import java.net.URL;

import org.jsoup.*;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.util.*;

public class SoccerBaseCrawler {
	TreeSet<Integer> teamIds;
	List<Integer> p2t;
	TreeSet<Integer> tour_ids;
	String url = "http://www.soccerbase.com/";
	String[] tids;
	int[] cids = {116, 171, 25, 133, 127, 117, 112, 23,
			1, 2,3,4,12,13,14,15,
			208, 22,20,114,134,132,19,76,
			306, 48, 206,124,24,123,111,264,
			126,21,194,205,122,113,118,47};
	
	SoccerBaseCrawler(){
		teamIds = new TreeSet<Integer>();
		p2t = new ArrayList<Integer>();
		tour_ids = new TreeSet<Integer>();
	}
	
	void getIDs(){
		StringBuffer sb = new StringBuffer();
		Arrays.sort(cids);
		ArrayList<String> idList = new ArrayList<String>();
		for(int id : cids){
			String cur = "http://www.soccerbase.com/tournaments/tournament.sd?comp_id="+id;
			Connection con = Jsoup.connect(cur).userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36").followRedirects(false).timeout(3000).header("Host", "soccerbase.com").header("Upgrade-Insecure-Requests", "1").header("Connection", "keep-alive");
			Document doc = null;
			while(true){
				try {
					doc = con.get();
					break;
				}
				catch (IOException e) {
					try {
						hangup();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					System.out.println(e.getMessage());
				}
			}
			Elements ids = doc.select("#seasonSelect option");
			for(int i = 1; i < ids.size() && i <= 20; i++){
				idList.add(ids.get(i).attr("value"));
			}
//			appendToBuffer(sb, idList.toArray(new String[idList.size()]));

		}
//		writeFile("ids.csv", sb.toString(), false);
		tids = new String[idList.size()];
		idList.toArray(tids);
		System.out.printf("done looking up league ids. found %d ids.\n", tids.length);
	}
	
	String getTournaments(){
		StringBuffer sb = new StringBuffer();
//		String[] headers = {"Tournament ID", "Team ID", "Team Name", "Points", "Rank", "League Name", "Season"};
//		sb = appendToBuffer(sb, headers);
//		writeFile("tournaments.csv", sb.toString(), false);
//		sb = new StringBuffer();
//		int[] tids = {1435, 1386, 1304, 1159, 939, 1, 2, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117};
		for(int i = 295; i < tids.length; i++){
			String[] curdata = new String[7];
			curdata[0] = ""+tids[i]; //tournament id
			String cur = url+"tournaments/tournament.sd?tourn_id="+tids[i];
//				String cur = "http://www.soccerbase.com/tournaments/tournament.sd?tourn_id=107";
			Connection con = Jsoup.connect(cur).userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36").followRedirects(false).timeout(3000)
						.header("Host", "soccerbase.com").header("Upgrade-Insecure-Requests", "1").header("Connection", "keep-alive").referrer("www.racingpost.com");
			Document doc = null;
			while(true){
				try {
						doc = con.get();
						break;
				} catch (IOException e) {
					System.out.printf("error at: %d\n", i);
					System.out.println(e.getMessage());
				try {
						hangup();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
			Elements content = doc.getElementsByClass("siteContent");
			Element table = content.get(0).getElementsByTag("tbody").get(0);
			Elements trs = table.getElementsByTag("tr");
			for(int j = 0; j < trs.size(); j++){
				Element tr = trs.get(j);
				curdata[1] = tr.select("td a").get(0).attr("href").split("=")[1]; //team id
				curdata[2] = tr.select("td a").get(0).html(); //team name
				curdata[3] = tr.select("td").last().text(); //points
				if(curdata[3].length() > 2) curdata[3] = curdata[3].substring(0, 2);
				curdata[4] = tr.child(0).html(); //rank
				curdata[5] = content.select("h1").get(0).html(); //league name
				curdata[6] = content.select("h3").get(0).html(); //season
				sb = appendToBuffer(sb, curdata);
				writeFile("tournaments.csv", sb.toString(), true);
				//re-initialize
				sb = new StringBuffer();
				curdata = new String[7];
				curdata[0] = ""+tids[i];
			}
			
		}
		System.out.println("done writing tournaments.csv.");
		return sb.toString();
		
	}
	
	String getTeamInfo(){
		StringBuffer sb = new StringBuffer();
		String[] infoHeaders = {"Team ID", "Team Name", "Logo URL", "Nickname"};
		sb = appendToBuffer(sb, infoHeaders);
		writeFile("TeamInfo.csv", sb.toString(), false);
		for(Integer id : teamIds){
			String cur = "http://www.soccerbase.com/teams/team.sd?team_id="+id;
			String[] curData = new String[infoHeaders.length];
			sb = new StringBuffer();
			Connection con = Jsoup.connect(cur).userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36").followRedirects(false).timeout(8000).header("Host", "soccerbase.com").header("Upgrade-Insecure-Requests", "1").header("Connection", "keep-alive");
			Document doc = null;
			while(true){
				try {
					doc = con.get();
					break;
				}
				catch (IOException e) {
					System.out.printf("error at: %d\n", id);
					System.out.println(e.getMessage());
				}
			}
			Element head = doc.select(".pageHeader").get(0);
			curData[0] = ""+id; //team id
			curData[1] = head.select("h1").get(0).html().split(" <")[0]; //team name
			String imgUrl = null;
			try{
				imgUrl = head.select(".imageHead img").get(0).absUrl("src");
				String newName = id+"-"+curData[1]+".png";
				if(imgUrl != null)
					curData[2] = DownloadImg(imgUrl, newName); //local image path
			}
			catch(IndexOutOfBoundsException e){
				System.out.printf("error at: %d\n", id);
				System.out.println(e.getMessage());
				curData[2] = imgUrl;
			} catch (IOException e) {
				System.out.printf("error at: %d\n", id);
				System.out.println(e.getMessage());
			}
			curData[3] = doc.select(".clubInfo td strong").get(0).text();
			if(curData[3].equals("")) curData[3] = null;
			appendToBuffer(sb, curData);
			writeFile("TeamInfo.csv", sb.toString(), true);
//			System.out.println(sb);
			
		}
		System.out.println("done writing team info.csv.");
		return sb.toString();
	}
	
	String getSquad(){
		StringBuffer sb = new StringBuffer();
		String[] headers = {
				"Team ID",
				"Season ID",
				"Player ID",
				"Player Name",
				"Appearances",
				"Goals"
		};
		sb = appendToBuffer(sb, headers);
		writeFile("TeamSquad.csv", sb.toString(), false);
		for(Integer id : teamIds){
			for(int j = 2015; j >= 1996; j--){
				
				int year = j-1870;
				String cur = "http://www.soccerbase.com/teams/team.sd?season_id="+year+"&team_id="+id+"&teamTabs=stats";
				Connection con = Jsoup.connect(cur).userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36").followRedirects(false).timeout(8000).header("Host", "soccerbase.com").header("Upgrade-Insecure-Requests", "1").header("Connection", "keep-alive");
				try {
					Thread.sleep(50);
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				Document doc = null;
				while(true){
					try {
						doc = con.get();
						break;
					}
					catch (IOException e) {
						try {
							hangup();
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						System.out.printf("error at: %d\n", id);
						System.out.println(e.getMessage());
					}
				}
				Elements trs = doc.select(".table.center tbody tr");
				if(trs != null && !trs.isEmpty()){
					for(Element row : trs){
						sb = new StringBuffer();
						String[] curData = new String[headers.length];
						curData[0] = ""+id; //team id
						curData[1] = ""+year; //season id
						Element a = row.select("td a").get(0);
						curData[2] = a.attr("href").split("=")[1]; //player id
						curData[3] = a.text(); // player name
						Elements tds = row.select("td");
						curData[4] = tds.get(1).text(); // Appearances
						curData[5] = tds.get(2).text(); // Goals
						appendToBuffer(sb, curData);
						writeFile("TeamSquad.csv", sb.toString(), true);
					}
				}
			}
		}
		System.out.println("done writing team squad.csv.");
		return sb.toString();
	}
	
	String getPlayerInfo(){
		StringBuffer sb = new StringBuffer();
		String[] headers = {
				"Player ID",
				"Player Name",
				"Position",
				"Number",
				"Place of Birth",
				"Nationality",
		};
		String[] headers2 = {
			"Player ID",
			"Season ID",
			"Team ID",
			"Opponent ID",
			"Goal Scored",
			"Goal Against",
			"Win/Lose",
			"Individual Goal",
			"Yellow",
			"Red",
			"League",
		};
		sb = appendToBuffer(sb, headers);
		writeFile("PlayerInfo.csv", sb.toString(), false);
		sb = new StringBuffer();
		sb = appendToBuffer(sb, headers2);
		writeFile("matches.csv", sb.toString(), false);
		int startIndex = 0;
		for(int j = startIndex; j < p2t.size(); j++){
			int id = p2t.get(j);
			//get basic info
			String cur = "http://www.soccerbase.com/players/player.sd?player_id="+id;
			String[] curData = new String[headers.length];
			sb = new StringBuffer();
			Connection con = Jsoup.connect(cur).userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36").followRedirects(false).timeout(8000).header("Host", "soccerbase.com").header("Upgrade-Insecure-Requests", "1").header("Connection", "keep-alive");
			try {
				Thread.sleep(50);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			Document doc = null;
			while(true){
				try {
					doc = con.get();
					break;
				} catch (IOException e) {
					try {
						hangup();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					System.out.printf("error at: %d\n", id);
					System.out.println(e.getMessage());
				}
			}
			curData[0] = ""+id; //player id
			String[] NameNumber = doc.select(".imageHead h1").get(0).text().split("\\.");
			if(NameNumber.length > 1){ // has number
				curData[1] = NameNumber[1].trim(); // Name
				curData[3] = NameNumber[0].replace(".", ""); // Number
			}
			else{
				curData[1] = NameNumber[0].trim();
			}
			curData[2] = doc.select(".midfielder.bull").get(0).text().trim().split(" ")[0];
			Elements trs = doc.select(".soccerContent .soccerColumnLast .clubInfo tbody tr");
			curData[4] = "\""+trs.get(0).select("strong").get(0).text()+"\""; // PoB
			curData[5] = trs.get(1).select("strong").get(0).text(); // Nationality
			appendToBuffer(sb, curData);
			writeFile("PlayerInfo.csv", sb.toString(), true);
			
			
			//get match info
			Elements seasons = doc.select("#seasonSelect option");
			
			if(!seasons.isEmpty()){
				for(int i = 1; i < seasons.size(); i++){
					String curSeason = seasons.get(i).attr("value");
					cur = "http://www.soccerbase.com/players/player.sd?player_id="+id+"&season_id="+curSeason;
					con = Jsoup.connect(cur).userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36").followRedirects(false).timeout(8000).header("Host", "soccerbase.com").header("Upgrade-Insecure-Requests", "1").header("Connection", "keep-alive");
					try {
						Thread.sleep(50);
					} catch (InterruptedException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					doc = null;
					while(true){
						try {
							doc = con.get();
							break;
						} catch (IOException e) {
							try {
								hangup();
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							System.out.printf("error at: %d\n", id);
							System.out.println(e.getMessage());
						}
					}
					trs = doc.select(".soccerGrid tbody tr.match");
					for(Element tr : trs){
						int compid = Integer.parseInt(trs.select(".tournament a").get(0).attr("href").split("=")[1]);
						if(tour_ids.contains(compid)){ //if the match is in the league we want
							//let's start!
							try{
								curData = new String[headers2.length];
								sb = new StringBuffer();
								curData[0] = ""+id; //player id
								curData[1] = curSeason; // season id
//								Element team = tr.select(".team:not(.inactive)").get(0);
//								Element opponent = tr.select(".team.inactive").get(0);
								Element team = null, opponent = null;
								Elements teams = tr.select(".team");
								for(Element t : teams){
									if(t.hasClass("inactive")) opponent = t;
									else team = t;
								}
								Elements score = tr.select(".score a em");
								curData[2] = team.select("a").get(0).attr("href").split("=")[1]; //own team
								curData[3] = opponent.select("a").get(0).attr("href").split("=")[1]; //opponent team
								if(tr.children().indexOf(team) < tr.children().indexOf(opponent)){
									//home team
									curData[4] = score.get(0).text(); // own goals
									curData[5] = score.get(1).text(); // opponent goals
								}
								else{
									//away team
									curData[4] = score.get(1).text();
									curData[5] = score.get(0).text();
								}
								curData[6] = Integer.parseInt(curData[4]) > Integer.parseInt(curData[5]) ? "true" : "false";
								Elements blankCards = tr.select(".blankCard");
								if(!blankCards.get(0).html().equals("")){
									curData[7] = blankCards.get(0).select("span").get(0).text(); // personal goals
								}
								curData[8] = blankCards.get(1).html().equals("") ? "false" : "true"; // yellow card?
								curData[9] = blankCards.get(2).hasClass("redCard") ? "true" : "false"; // red card?
								curData[10] = trs.select(".tournament a").get(0).text().trim(); // league name
								appendToBuffer(sb, curData);
								writeFile("matches.csv", sb.toString(), true);
							}catch(Exception e){
								System.out.println("error url: "+cur);
								e.printStackTrace();
								System.exit(0);
							}
						}
					}
				}
			}
		}
		System.out.println("done.");
		return sb.toString();
	}
	
	void loadTeamIds(){
		try {
			Scanner scan = new Scanner(new File("tournaments.csv"));
			while(scan.hasNext()){
				String s = scan.nextLine().split(",")[1];
				if(!s.equals("Team ID") && !teamIds.contains(Integer.parseInt(s)))
					teamIds.add(Integer.parseInt(s));
			}
			scan.close();
			System.out.println(teamIds.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void loadPlayerIds(){
		try {
			Scanner scan = new Scanner(new File("TeamSquad.csv"));
			while(scan.hasNext()){
				String s = scan.nextLine().split(",")[2];
				if(!s.equals("Player ID") && !p2t.contains(Integer.parseInt(s)))
					p2t.add(Integer.parseInt(s));
			}
			scan.close();
			Collections.sort(p2t);
			System.out.println(p2t);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void loadTournaments(){
		for(int i : cids){
			tour_ids.add(i);
		}
	}
	
	void writeFile(String filename, String content, boolean append){
		try {
			FileWriter fw = null;
			if(!append){
				fw = new FileWriter(new File(filename));
			}
			else
				fw = new FileWriter(new File(filename), true);
			fw.write(content);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	String DownloadImg(String url, String name) throws IOException{
		String folder = "badges/";
//		InputStream in = u.openStream();
		String newPath = folder + name;
		
		Response resultImageResponse = Jsoup.connect(url)
                .ignoreContentType(true).execute();

		//output here
		FileOutputStream out = (new FileOutputStream(new java.io.File(newPath)));
		out.write(resultImageResponse.bodyAsBytes());  // resultImageResponse.body() is where the image's contents are.
		out.close();
		
//		OutputStream out = new BufferedOutputStream(new FileOutputStream(newPath));
//		for(int i = 0; (i = in.read())!=1;){
//			out.write(i);
//		}
//		out.close();
//		in.close();
		return newPath;
	}

	
	StringBuffer appendToBuffer(StringBuffer buff, String[] data){
		for(int i = 0; i < data.length; i++){
			buff.append(data[i]);
			if(i != data.length - 1){
				buff.append(',');
			}
			else
				buff.append('\n');
		}
		return buff;
	}
	
	void logError(String url){
		
	}
	
	void hangup() throws InterruptedException{
		Thread.sleep((long) Math.floor(Math.random()*1000));
	}
}
