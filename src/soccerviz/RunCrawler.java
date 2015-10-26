package soccerviz;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class RunCrawler {
	public static void main(String[] args) throws IOException{
		SoccerBaseCrawler crawler = new SoccerBaseCrawler();
//		crawler.getIDs();
//		crawler.getTournaments();
//		crawler.loadTeamIds();
//		crawler.getSquad();
		crawler.loadPlayerIds();
		crawler.loadTournaments();
		crawler.getPlayerInfo();
	}
}
