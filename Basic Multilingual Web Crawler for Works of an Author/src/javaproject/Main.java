/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaproject;

import it.uniroma1.lcl.babelnet.data.BabelSenseSource;
import it.uniroma1.lcl.jlt.util.Language;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.*;
import javax.swing.text.JTextComponent;

/**
 *
 * @author jgonzale5
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    //static List<String> Seeds = Arrays.asList("http://www.eprodoffice.com/Shhh/abcdefghijklmnopqrstuvwxyz.htm", "https://www.wikipedia.org/", "https://www.webdirectory.com/", "https://botw.org/", "http://www.jayde.com/");
    //static List<String> Seeds = Arrays.asList("https://en.wikipedia.org/wiki/List_of_dog_breeds", "https://www.akc.org/dog-breeds/", "https://dogtime.com/dog-breeds/profiles", "https://www.petfinder.com/dog-breeds/");
    //static List<String> Seeds = Arrays.asList("https://en.wikipedia.org/wiki/List_of_dog_breeds","https://dogtime.com/dog-breeds/profiles");
    static List<String> Seeds = Arrays.asList("https://en.wikipedia.org/wiki/Stephen_King_bibliography", "https://es.wikipedia.org/wiki/Anexo:Bibliograf%C3%ADa_de_Stephen_King");
    static int Depth = 1;
    static int maxSize = 10;
    static TranslatorClass translator;
    static StopWordRemover stopRemover;
    static SemanticClass semanticer = new SemanticClass();
    public static final Object monitor = new Object();
    public static boolean startSearch = false;
    public static String query;

    public static void main(String[] args) throws Exception {
        try {
            stopRemover = new StopWordRemover();
            createInputWindow();

            startSearch = false;
            System.out.println("Waiting for query...");
            synchronized (monitor) {
                while (!startSearch) {
                    try {
                        monitor.wait(); // wait until notified
                    } catch (Exception e) {
                    }
                }
            }

            System.out.println("Starting search");

            StartSearch(query);

            System.exit(0);
        } catch (NullPointerException ex) {
            System.exit(0);
        }
        //return;
    }

    public static void StartSearch(String withText) throws Exception {
        System.out.println("Searching...");
        Spider englishSpider = new Spider(Depth);
        Spider spanishSpider = new Spider(Depth);

        //englishSpider.setSeeds(Seeds);
        //spanishSpider.setSeeds(Seeds);
        englishSpider.setSeeds(new ArrayList<>(Arrays.asList(Seeds.get(0))));
        spanishSpider.setSeeds(new ArrayList<>(Arrays.asList(Seeds.get(1))));

        List<String> quotedTerms = new LinkedList<String>();
        String query = removeQuoteWords(quotedTerms, withText);

        System.out.println("Spiders created...");
        List<String> generalTerms = cleanPrompt(query);
        //generalTerms = translator.AddTranslation(generalTerms);
        System.out.println("Removing stop words...");

        List<String> specificTerms = new LinkedList<>();
        for (String s : query.split(",")) {
            specificTerms.add(s.trim());
        }

        englishSpider.SetSpecificTerms(specificTerms);
        spanishSpider.SetSpecificTerms(specificTerms);
        System.out.println("Re-adding original query sentences just in case we want something literal");

        List<String> englishTerms = stopRemover.RemoveStopWords(stopRemover.RemoveStopWords(generalTerms, "en"), "es");
        englishTerms = translator.AddTranslation(englishTerms);
        //System.out.println("Adding semantically similar words");
        englishTerms = semanticer.AddSemanticallySimilarWords(englishTerms, Language.EN, Arrays.asList(BabelSenseSource.WN));

        List<String> spanishTerms = stopRemover.RemoveStopWords(stopRemover.RemoveStopWords(generalTerms, "en"), "es");
        spanishTerms = translator.AddTranslation(spanishTerms);
        //System.out.println("Adding semantically similar words");
        spanishTerms = semanticer.AddSemanticallySimilarWords(spanishTerms, Language.ES, Arrays.asList(BabelSenseSource.MCR_ES));

        englishSpider.SetRequiredWords(Arrays.asList("Stephen", "King"));
        spanishSpider.SetRequiredWords(Arrays.asList("Stephen", "King"));

        englishTerms.addAll(quotedTerms);
        spanishTerms.addAll(quotedTerms);

        System.out.println("English Terms: " + englishTerms.toString());
        System.out.println("Spanish Terms: " + spanishTerms.toString());

        System.out.println("Starting english search...");
        englishSpider.searchInSeeds(englishTerms);
        System.out.println("Starting spanish search...");
        spanishSpider.searchInSeeds(spanishTerms);

        System.out.println("Search and scoring complete!");

        String[] englishResults = englishSpider.FilterResults(maxSize);
        System.out.println("English results filtered");
        String[] spanishResults = spanishSpider.FilterResults(maxSize);
        System.out.println("Spanish results filtered");

        showBilingualResults(spanishResults, englishResults);
    }

    public static void showResults(String urls[]) {
        JFrame frame = new JFrame("Results");
        String output = "The results to your search were:\n";

        for (String s : urls) {
            output += (s + "\n");
        }

        JOptionPane.showMessageDialog(frame, output);
        System.out.println(output);
    }

    public static void showBilingualResults(String spanishUrls[], String englishUrls[]) {
        JFrame frame = new JFrame("Results");
        String output = "Top " + maxSize + " English Results:\n";

        for (String s : englishUrls) {
            //output += "<a href=\"" + s + "\">" + s + "</a>\n";
            output += s + "\n";
        }

        output += "Top " + maxSize + " Spanish Results:\n";

        for (String s : spanishUrls) {
            //output += "<a href=\"" + s + "\">" + s + "</a>\n";
            output += s + "\n";
        }

        JOptionPane.showMessageDialog(frame, output);
        System.out.println(output);
    }

    public static List<String> cleanPrompt(String str) {
        System.out.println("Cleaning Query...");
        String arr[] = str.trim().split("\\s|,");
        List<String> buildingList = new LinkedList<String>();

        for (String s : arr) {
            if (!buildingList.contains(s)) {
                if (s.length() > 0 && !s.isEmpty()) {
                    buildingList.add(s.trim());
                }
            }
        }
        //System.out.println("Clean query: " + buildingList.toString());
        return buildingList;
    }

    private static JTextField textField;
    private static JFrame frame;

    public static void createInputWindow() {
        frame = new JFrame();
        // Release the window and quit the application when it has been closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JLabel prompt = new JLabel("   With this, you can find any Stephen King novel:   ");
        frame.getContentPane().add(prompt, BorderLayout.NORTH);

        textField = new JTextField(20);
        frame.getContentPane().add(textField, SwingConstants.CENTER);

        final JButton button = new JButton("Search");

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                //StartSearch(textField.getText());
                query = textField.getText();

                synchronized (monitor) {
                    startSearch = true;
                    monitor.notify();
                }
                System.out.println(startSearch);
                frame.dispose();
            }
        });

        frame.getContentPane().add(button, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);

        //System.out.println("Window created");
        translator = new TranslatorClass(textField);
    }

    static String removeQuoteWords(List<String> quotedWords, String fromQuery) {
        if (fromQuery.contains("\"")) {
            //List<String> quotedWords = new LinkedList<String>();
            String filteredQuery = "";
            int a = -1, b = 0;
            for (int n = 0; n < fromQuery.length(); n++) {
                if (fromQuery.charAt(n) == '\"') {
                    if (a == -1) {
                        a = n;

                        if (a > b) {
                            filteredQuery += fromQuery.substring(b, a);
                        }
                    } else {
                        b = n + 1;
                        quotedWords.add(fromQuery.substring(a + 1, b - 1).trim());
                        a = -1;
                    }
                }
            }
            //System.out.println("Query without quotes: " + filteredQuery);
            //System.out.println("Quoted Query: " + quotedWords.toString());

            return filteredQuery;
        } else {
            return fromQuery;
        }
    }
//    static String AddSemanticsToBaseQuery(String query, Language lang, List<BabelSenseSource> source)
//    {
//        List<String> tempL = new LinkedList<String>();
//        
//        for (String s : query.split(","))
//        {
//            tempL.add(s.trim());
//        }
//        
//        semanticer.AddSemanticallySimilarWords(tempL, lang, source);
//        
//        
//    }

}
