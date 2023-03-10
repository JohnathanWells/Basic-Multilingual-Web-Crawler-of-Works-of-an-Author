/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaproject;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.babelnet.data.BabelSenseSource;
import it.uniroma1.lcl.jlt.util.Language;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author jgonzale5
 */
public class SemanticClass 
{
    BabelNet bn;
    
//    public boolean CheckRelation(String queryWord, String otherWord, Language lang)
//    {
//        BabelNetQuery query = new BabelNetQuery.Builder(otherWord).from(lang).build();
//        List<BabelSynset> byl = bn.getSynsets(query);
//        
//        for (BabelSynset synset : byl)
//        {
//            if (synset.)
//        }
//    }
    
    SemanticClass()
    {
        System.out.println("Creating semantic object");
        bn = BabelNet.getInstance();
        //System.out.println("Finished creating semantic object");
    }
    
    public List<String> ReturnSimilarWords(String queryWord, Language lang, Collection<BabelSenseSource> sources)
    {
        BabelNetQuery query = new BabelNetQuery.Builder(queryWord).from(lang).sources(sources).build();
        List<BabelSynset> byl = bn.getSynsets(query);
        List<String> result = new LinkedList<String>();
        
        for (BabelSynset synset : byl)
        {
            result.add(synset.getMainSense().get().getSimpleLemma());
        }
        
        return result;
    }
    
    public List<String> AddSemanticallySimilarWords(List<String> to, Language lang, Collection<BabelSenseSource> source)
    {
        HashSet<String> tempList = new HashSet<String>();
        List<String> result = new LinkedList<String>();
        String lastWord = "";
        
        for (String s : to)
        {
            tempList.add(s);
        //System.out.println("Adding other words for " + to);
        
            if (lastWord != "")
            {
                for (String t : ReturnSimilarWords((lastWord + " " + s), lang, source))
                    tempList.add(t.toLowerCase().replace("_", " "));
            }
            
            for (String l : ReturnSimilarWords(s, lang, source))
            {
                //System.out.println("Adding semantically similar word " + l);
                tempList.add(l.toLowerCase().replace("_", " "));
            }
        }
        
        for (String s : tempList)
        {
            result.add(s);
        }
        
        return result;
    }
}
