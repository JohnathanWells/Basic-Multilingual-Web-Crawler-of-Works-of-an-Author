/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaproject;
import com.google.api.GoogleAPI;
import com.google.api.translate.*;
import com.google.api.translate.Language;
import com.inet.jortho.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.text.JTextComponent;

/**
 *
 * @author jgonzale5
 */
public class TranslatorClass {
    
    String codeURL = "https://script.google.com/macros/s/AKfycbxhtFHkJp7wdXHXeK8YN-o663PSXi-Kb5IlwWDYH3xSNVB5IbX4/exec";
    
//    public HashSet<String> AddSpellcheckedWords(HashSet<String> words)
//    {
//        HashSet<String> list = new HashSet<String>();
//        
//        for (String s : words)
//        {
//            list.add(s)
//        }
//        
//        return list;
//    }
    
    public List<String> AddTranslation(List<String> words) throws Exception
    {
        HashSet<String> list = new HashSet<String>();
        
        for (String s : words)
        {
            list.add(s.toLowerCase());
            //list.add(Translate.execute(s, Language.ENGLISH, Language.SPANISH).toLowerCase());
            //list.add(Translate.execute(s, Language.SPANISH, Language.ENGLISH).toLowerCase());
            list.add(translate("en", "es", s).toLowerCase());
            list.add(translate("es", "en", s).toLowerCase());
        }
        
        LinkedList<String> listL = new LinkedList<String>();
        
        for (String s : list)
        {
            listL.add(s);
        }
        
        return listL;
    }
    
    private static String translate(String langFrom, String langTo, String text) throws IOException {
        // INSERT YOU URL HERE
        String urlStr = "https://script.google.com/macros/s/AKfycbxhtFHkJp7wdXHXeK8YN-o663PSXi-Kb5IlwWDYH3xSNVB5IbX4/exec" +
                "?q=" + URLEncoder.encode(text, "UTF-8") +
                "&target=" + langTo +
                "&source=" + langFrom;
        URL url = new URL(urlStr);
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public TranslatorClass(JTextComponent jtc) 
    {
        SpellChecker.setUserDictionaryProvider(new FileUserDictionary());      
        SpellChecker.registerDictionaries(this.getClass().getResource("/dictionary"), "en,es");
        SpellChecker.register(jtc);
    }
}
