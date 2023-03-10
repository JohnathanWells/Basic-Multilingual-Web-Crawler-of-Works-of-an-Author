/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package javaproject;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author jgonzale5
 */
public class Seeker {
    
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    private List<String> links = new LinkedList<String>();
    private Document htmlDocument;
    private List<String> REQUIRED_WORDS = new LinkedList<>();
    private List<String> SPECIFIC_TERMS = new LinkedList<>();
    
    public boolean crawl(String URL)
    {
        try
        {
            Connection connection = Jsoup.connect(URL).userAgent(USER_AGENT);
            Document htmlDoc = connection.get();
            htmlDocument = htmlDoc;
            
            if(connection.response().statusCode() == 200) // 200 is the HTTP OK status code
                                                          // indicating that everything is great.
            {
                System.out.println("\n**Visiting** Received web page at " + URL);
            }
            if(!connection.response().contentType().contains("text/html"))
            {
                System.out.println("**Failure** Retrieved something other than HTML");
                return false;
            }
            
            //System.out.println("Received web page at " + URL);
            
            Elements linksOnPage = htmlDoc.select("a[href]");
            //System.out.println("Found (" + linksOnPage.size() + ") links");
            for(Element link : linksOnPage)
            {
                links.add(link.absUrl("href"));
            }
            return true;
        } catch(Exception ioe) {
            System.out.println("Error in out HTTP request " + ioe);
            return false;
        }
    }
    
    public boolean searchForWord(String word)
    {
        if(this.htmlDocument == null)
        {
            System.out.println("ERROR! Call crawl() before performing analysis on the document");
            return false;
        }
        System.out.println("Searching for the word " + word + "...");
        String bodyText = this.htmlDocument.body().text();
        
        return bodyText.toLowerCase().contains(word.toLowerCase());
    }
    
    public int countScore(List<String> words)
    {
        int counter = 0;
        int totalCount = 0;
        int bonusForURLContent = 2;
        int bonusForSpecificTerm = 10;
        
        if(this.htmlDocument == null || this.htmlDocument.body() == null || this.htmlDocument.body().text() == null)
        {
            System.out.println("ERROR! Call crawl() before performing analysis on the document");
            return -1;
        }
        //System.out.println("Searching for the word " + word + "...");
        
        String bodyText = this.htmlDocument.body().text().toLowerCase();
        
        boolean hasRequired = true;
        
        for (String r : REQUIRED_WORDS)
        {
            if (!bodyText.contains(r))
            {
                System.out.println("The required words" + REQUIRED_WORDS + " were not found in this website");
                return 0;
            }
        }
        
        for (String s : SPECIFIC_TERMS)
        {
            String str = s.toLowerCase();
            counter = bodyText.split(Pattern.quote(str)).length - 1;
            
            totalCount += counter * bonusForSpecificTerm;
        }
        
        for (String s : words)
        {
            String str = s.toLowerCase();
            counter = bodyText.split(Pattern.quote(str)).length - 1;
            
            if (this.htmlDocument.title().toLowerCase().contains(str))
                counter += bonusForURLContent;
            
            //totalCount += (counter / words.size());
            totalCount += counter;
            
            System.out.println(counter + " instances of [" + s + "] in '" + this.htmlDocument.title()+"'");
        }
        //System.out.println("Total count: " + totalCount);
        return totalCount;
    }
    
    public List<String> getLinks()
    {
        return links;
    }
    
    public void SetRequiredWords(List<String> to)
    {
        REQUIRED_WORDS.clear();
        
        for (String s : to)
        {
            REQUIRED_WORDS.add(s);
        }
    }
    
    public void SetSpecificTerms(List<String> to)
    {
        SPECIFIC_TERMS.clear();
        
        for (String s : to)
        {
            SPECIFIC_TERMS.add(s);
        }
    }
    
    public String RemoveSpanishCharacters(String from)
    {
        String result = from;
        result.replaceAll("á", "a");
        result.replaceAll("é", "e");
        result.replaceAll("í", "i");
        result.replaceAll("ó", "o");
        result.replaceAll("ú", "u");
        
        return result;
    }
}
