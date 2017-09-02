package de.wikiclicks.utils;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by shahbaz on 7/12/16.
 */
public class EntityExtractor {
    private AbstractSequenceClassifier<CoreLabel> classifier;

    public static Set<String> stopwords = new HashSet<>(
            Arrays.asList("a", "as", "able", "about", "above", "according", "accordingly", "across",
                    "actually", "after", "afterwards", "again", "against", "aint", "ain't","all", "allow", "allows",
                    "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an",
                    "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere",
                    "apart", "appear", "appreciate", "appropriate", "are", "arent", "aren't", "around", "as", "aside",
                    "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become",
                    "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside",
                    "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "cmon", "c'mon", "cs",
                    "came", "can", "cant", "can't", "cannot", "cause", "causes", "certain", "certainly", "changes",
                    "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain",
                    "containing", "contains", "corresponding", "could", "couldnt", "couldn't", "course", "currently",
                    "definitely", "described", "despite", "did", "didnt", "didn't", "different", "do", "does", "doesnt",
                    "doesn't", "doing", "dont", "don't", "done", "down", "downwards", "during", "each", "edu", "eg",
                    "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc", "even",
                    "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example",
                    "except", "far", "few", "ff", "fifth", "first", "five", "followed", "following", "follows", "for",
                    "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting",
                    "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadnt", "hadn't",
                    "happens", "hardly", "has", "hasnt", "hasn't", "have", "havent", "haven't", "having", "he", "hes",
                    "hello", "help", "hence", "her", "here", "heres", "here's", "hereafter", "hereby", "herein", "hereupon",
                    "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however",
                    "i", "id", "ill", "i'll", "im", "i'm", "ive", "i've", "ie", "if", "ignored", "immediate", "in",
                    "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead",
                    "into", "inward", "is", "isnt", "isn't", "it", "itd", "itll", "it'll", "its", "it's", "itself",
                    "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter",
                    "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look",
                    "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely",
                    "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely",
                    "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new",
                    "next", "nine", "no", "nobody", "non", "none", "noone", "nor", "normally", "not", "nothing",
                    "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on",
                    "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours",
                    "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per",
                    "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que",
                    "quite", "qv", "rather", "rd", "re", "really", "reasonably", "regarding", "regardless", "regards",
                    "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second",
                    "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible",
                    "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldnt", "shouldn't",
                    "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes",
                    "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such",
                    "sup", "sure", "ts", "take", "taken", "tell", "tends", "th", "than", "thank", "thanks", "thanx",
                    "that", "thats", "that's", "the", "their", "theirs", "them", "themselves", "then", "thence", "there",
                    "theres", "there's", "thereafter", "thereby", "therefore", "therein", "thereupon", "these", "they",
                    "theyd", "they'd", "theyll", "they'll", "theyre", "they're", "theyve", "they've", "think", "third",
                    "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus",
                    "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying",
                    "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon",
                    "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz",
                    "vs", "want", "wants", "was", "wasnt", "wasn't", "way", "we", "wed", "we'd", "well", "were", "weve",
                    "we've", "welcome", "we'll", "went", "were", "werent", "weren't", "what", "whats", "what's", "whatever",
                    "when", "whence", "whenever", "where", "wheres", "where's", "whereafter", "whereas", "whereby",
                    "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "whos", "who's",
                    "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without",
                    "wont", "won't", "wonder", "would", "would", "wouldnt", "wouldn't", "yes", "yet", "you", "youd", "you'd",
                    "youll", "you'll", "youre", "you're", "youve", "you've", "your", "yours", "yourself", "yourselves", "zero"));

    public EntityExtractor(){
        String serializedClassifier = getClass().getResource("/classifiers/english.conll.4class.distsim.crf.ser.gz").getFile();
        try {
            classifier = CRFClassifier.getClassifier(serializedClassifier);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public Set<String> getEntities(String text){
        Set<String> entities = new HashSet<>();
        text = text.replaceAll("\n", "").replaceAll("[^A-Za-z0-9]"," ");

        List<Triple<String, Integer, Integer>> classified = classifier.classifyToCharacterOffsets(text);

        for(Triple<String, Integer, Integer> annotation: classified){
            entities.add(text.substring(annotation.second, annotation.third)
                    .toLowerCase().trim().replaceAll("[ ]+", " "));

        }

        return entities;
    }

    public String getNERTag(String entity){
        List<Triple<String, Integer, Integer>> classified = classifier.classifyToCharacterOffsets(entity);

        if(classified.size() > 0)
            return classified.get(0).first;

        return "0";
    }


    public static void main(String[] args) {
        EntityExtractor entityExtractor = new EntityExtractor();
        String paragraph="Barack Hussein Obama is an American politician serving as the 44th President of the United States. " +
                "He is the first African American to hold the office, as well as the first president born outside of the continental United States. " +
                "Born in Honolulu, Hawaii, Obama is a graduate of Columbia University and Harvard Law School, where he served as president of the Harvard Law Review." +
                " He was a community organizer in Chicago before earning his law degree. " +
                "He worked as a civil rights attorney and taught constitutional law at the University of Chicago Law School between 1992 and 2004. " +
                "He served three terms representing the 13th District in the Illinois Senate from 1997 to 2004, " +
                "and ran unsuccessfully in the Democratic primary for the United States House of Representatives in 2000 against incumbent Bobby Rush.";
        System.out.println(entityExtractor.getEntities(paragraph));
    }

}
