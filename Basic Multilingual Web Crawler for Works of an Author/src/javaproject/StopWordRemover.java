/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaproject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author jgonzale5
 */
public class StopWordRemover {
    
    public Hashtable stopWords = new Hashtable();
    String stopWordFolder = "src\\stopwords\\";
    
    public List<String> RemoveStopWords(List<String> words, String forLanguage)
    {
        if (stopWords.containsKey(forLanguage))
        {
            List<String> list = new LinkedList<String>();
            LinkedList<String> tempL = (LinkedList<String>) stopWords.get(forLanguage);
        
            for (String s : words)
            {
                if (!tempL.contains(s))
                {
                    list.add(s);
                }
            }
        
            return list;
        }
        
        return null;
    }
    
    private void ReadStopwords(String filename)
    {
        List<String> words = new LinkedList<>();
      try
      {
        BufferedReader reader = new BufferedReader(new FileReader(stopWordFolder + filename + ".txt"));
        String line;
        while ((line = reader.readLine()) != null)
        {
            words.add(line);
        }
            stopWords.put(filename, words);
        reader.close();
      }
      catch (Exception e)
      {
        System.err.format("Exception occurred trying to read '%s'.", filename);
        e.printStackTrace();
      }
    }

    public StopWordRemover() 
    {
        ReadStopwords("es");
        ReadStopwords("en");
    }
}
