package soccerviz;
import java.io.*;
import java.net.URL;

import org.jsoup.*;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.util.*;

public class SoccerBaseCrawler {
	HashMap<String, String> ids;
	String url = "http://www.soccerbase.com/";
	String[] tids;
	
	SoccerBaseCrawler(){
		ids = new HashMap<String, String>();
		ids.put("team", "team_id");
		ids.put("tournament", "tourn_id");
		ids.put("season", "season_id");
		ids.put("player", "player_id");
	}
	
	void getIDs(){
		StringBuffer sb = new StringBuffer();
		int[] cids = {116, 171, 25, 133, 127, 117, 112, 23,
				1, 2,3,4,12,13,14,15,
				208, 22,20,114,134,132,19,76,
				306, 48, 206,124,24,123,111,264,
				126,21,194,205,122,113,118,47};
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
		String[] headers = {"Team ID", "Team Name", "Logo URL", "Nickname"};
		sb = appendToBuffer(sb, headers);
		String cur = "http://www.soccerbase.com/teams/team.sd?team_id=142";
		String[] curData = new String[headers.length];
		Connection con = Jsoup.connect(cur).userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36").followRedirects(false).timeout(8000).header("Host", "soccerbase.com").header("Upgrade-Insecure-Requests", "1").header("Connection", "keep-alive");
		try {
			Document doc = con.get();
			Element head = doc.select(".pageHeader").get(0);
			curData[0] = "142"; //team id
			curData[1] = head.select("h1").get(0).html().split(" <")[0]; //team name
			String imgUrl = head.select(".imageHead img").get(0).absUrl("src");
			String newName = 142+"-"+curData[1]+".png";
			curData[2] = DownloadImg(imgUrl, newName); //local image path
			curData[3] = doc.select(".clubInfo td strong").get(0).text();
			appendToBuffer(sb, curData);
			System.out.println(sb);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		String cur = "http://www.soccerbase.com/teams/team.sd?team_id=142&teamTabs=stats";
		String[] curData = new String[headers.length];
		Connection con = Jsoup.connect(cur).userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36").followRedirects(false).timeout(8000).header("Host", "soccerbase.com").header("Upgrade-Insecure-Requests", "1").header("Connection", "keep-alive");
		try {
			Document doc = con.get();
			Elements trs = doc.select(".table.center tbody tr");
			for(Element row : trs){
				curData[0] = "142"; //team id
				curData[1] = "145"; //season id
				Element a = row.select("td a").get(0);
				curData[2] = a.attr("href").split("=")[1]; //player id
				curData[3] = a.text(); // player name
				Elements tds = row.select("td");
				curData[4] = tds.get(1).text(); // Appearances
				curData[5] = tds.get(2).text(); // Goals
				appendToBuffer(sb, curData);
			}
			System.out.println(sb);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		sb = appendToBuffer(sb, headers);
		String cur = "http://www.soccerbase.com/players/player.sd?player_id=30921";
		String[] curData = new String[headers.length];
		Connection con = Jsoup.connect(cur).userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36").followRedirects(false).timeout(8000).header("Host", "soccerbase.com").header("Upgrade-Insecure-Requests", "1").header("Connection", "keep-alive");
		try {
			Document doc = con.get();
			curData[0] = "41643"; //team id
			String[] NameNumber = doc.select(".imageHead h1").get(0).text().split("\\.");
			curData[1] = NameNumber[1].trim(); // Name
			curData[2] = doc.select(".midfielder.bull").get(0).text().trim().split(" ")[0];
			curData[3] = NameNumber[0].replace(".", ""); // Number
			Elements trs = doc.select(".soccerContent .soccerColumnLast .clubInfo tbody tr");
			curData[4] = "\""+trs.get(0).select("strong").get(0).text()+"\""; // PoB
			curData[5] = trs.get(1).select("strong").get(0).text(); // Nationality
			appendToBuffer(sb, curData);
			System.out.println(sb);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
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
