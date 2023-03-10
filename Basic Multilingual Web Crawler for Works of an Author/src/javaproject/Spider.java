/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaproject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jgonzale5
 */
public class Spider {
    private static int DEPTH;
    private Set<String> VISITED = new HashSet<String>();
    private Set<String> RESULTS = new HashSet<String>();
    private HashMap<String, Integer> RESULTS_VALUES = new HashMap<String, Integer>();
    private HashMap<String, Integer> DEPTH_VALUES = new HashMap<String, Integer>();
    private HashMap<String, Integer> REFERENCE_SCORE = new HashMap<String, Integer>();
    private List<String> PENDING = new LinkedList<String>();
    private List<String> SEEDS = new LinkedList<String>();
    private int BASE_REFERENCE_SCORE = 0;
    private List<String> REQUIRED_WORDS = new LinkedList<>();
    private List<String> SPECIFIC_TERMS = new LinkedList<>();
    
    public Spider(int depth)
    {
        DEPTH = depth;
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
    
    private String nextURL()
    {
        String next;
        boolean tempB = false;
        
        next = PENDING.remove(0);
        
        while (VISITED.contains(next) )
        {   
            REFERENCE_SCORE.put(next, REFERENCE_SCORE.get(next) + (BASE_REFERENCE_SCORE  ));   

            if(PENDING.isEmpty())
            {
                return null;
            }
            
            next = PENDING.remove(0);
            
        }
        
        REFERENCE_SCORE.put(next, 0);
        
        VISITED.add(next);
        
        return next;
    }
    
    public void search(String url, String word)
    {
        while(VISITED.size() < DEPTH)
        {
            String currentURL;
            Seeker seeker = new Seeker();
            
            seeker.SetRequiredWords(REQUIRED_WORDS);
            seeker.SetSpecificTerms(SPECIFIC_TERMS);
            
            if (PENDING.isEmpty())
            {
                currentURL = url;
                
                VISITED.add(url);
            }
            else
            {
                currentURL = nextURL();
            }
            
            seeker.crawl(url);
            
            if (seeker.searchForWord(word))
            {
                //System.out.println(String.format("%s found at %s", word, url));
                RESULTS.add(currentURL);
                break;
            }
            PENDING.addAll(seeker.getLinks());
        }
        
        System.out.println(String.format("Found %s web page(s) with the terms", RESULTS.size()));
        //System.out.println(String.format("Visited %s web page(s)", VISITED.size()));
    }
    
    public void searchInSeeds(List<String> words)
    {
        int tempI;
        int currentDepth = 0;
        String currentURL = null;
        
        for(String url : SEEDS)
        {
            //currentDepth = 0;
            PENDING.add(url);
            
            while(!PENDING.isEmpty())
            {
                //currentDepth++;
                Seeker seeker = new Seeker();        
                                 
                currentURL = nextURL();
                  

                if (currentURL != null && !IsPictureLink(currentURL) && seeker.crawl(currentURL))
                {

                    if ((tempI = seeker.countScore(words)) > 1)
                    {
                        System.out.println(String.format("%s found at %s with temporary score of %d", words, currentURL, tempI));
                        RESULTS_VALUES.put(currentURL, tempI);
                        RESULTS.add(currentURL);
                    }


                    if (DEPTH_VALUES.get(currentURL) < DEPTH)
                    {
                        List<String> tempL = seeker.getLinks();

                        for (String s : tempL)
                        {
                            if (!s.contains("#"))
                            {
                            DEPTH_VALUES.put(s, DEPTH_VALUES.get(currentURL) + 1);
                            PENDING.add(s);
                            }
                        }
                    }
                } 
            }
        }
        
        //System.out.println(String.format("Found %s web page(s) with the terms", RESULTS.size()));
        System.out.println(String.format("Visited %s web page(s)", VISITED.size()));
    }
    
    public void setSeeds(List<String> seeds)
    {
        for (String s : seeds)
        {
            SEEDS.add(s);
            DEPTH_VALUES.put(s, 0);
        }
    }
    
    public String[] FilterResults (int maxSize)
    {
        String tempList[] = RESULTS.toArray(new String[0]);
        String temp;
        int totalScore;
        Boolean ordered = true;
        int urlScore = 0;
        
        Boolean excludeSeeds = false;
        Boolean containsSeed = false;
        //System.out.println(tempList.length);
        
        for (String s : tempList)
        {
            if (excludeSeeds)
            {
                for (String url : SEEDS)
                {
                    if (s.contains(url))
                    {
                        containsSeed = true;
                        break;
                    }
                }
                
                if (containsSeed)
                    urlScore = 0;
                else
                    urlScore = 1;
            }
            else
            {
                urlScore = 1;
            }
            //System.out.println("Scoring " + s + " " + RESULTS_VALUES.containsKey(s) + " " + REFERENCE_SCORE.containsKey(s) + " " + DEPTH_VALUES.containsKey(s) + " " + RESULTS.size());
            totalScore = (RESULTS_VALUES.get(s) + REFERENCE_SCORE.get(s)) * urlScore;
            
            System.out.println(s + " has a score of " + totalScore + " (" + RESULTS_VALUES.get(s) + " + " + REFERENCE_SCORE.get(s) + ")");
            
            RESULTS_VALUES.put(s, totalScore); 
        }
        
        
        
        do
        {
            ordered = true;
            
            for (int n = 0; n < tempList.length - 1; n++)
            {
                if (RESULTS_VALUES.get(tempList[n]) < RESULTS_VALUES.get(tempList[n + 1]))
                {
                    temp = tempList[n];
                    tempList[n] = tempList[n+1];
                    tempList[n+1] = temp;
                    ordered = false;
                }
            }
        } while (!ordered);
        
        for (int n = 0; n < RESULTS.size(); n++)
        {
            System.out.println(tempList[n] + " has a score of " + RESULTS_VALUES.get(tempList[n]));
        }
        String[] results = new String[Integer.min(maxSize, tempList.length)];
        
        for (int i = 0; i < results.length; i++)
        {
            results[i] = tempList[i];
        }
        
        return results;
    }
    
    public boolean IsPictureLink(String url)
    {
        return (url.endsWith(".jpg") || url.endsWith(".png") || url.endsWith(".jpeg") || url.endsWith(".bmp") || url.endsWith(".svg") || url.endsWith(".gif"));
    }
    
//    public boolean HasUnsafeCharacters(String url)
//    {
//        System.out.println(url);
//        Pattern pat = Pattern.compile("[@\\$\\,#\\[\\]]");
//        Matcher m = pat.matcher(url);
//        
//        return m.find();
//        //return url.matches("[\\(\\);:@&=\\+\\$,/\\?#\\[\\%\\-\\]\\+]*");
//    }
}
