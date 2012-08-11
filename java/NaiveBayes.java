// NLP Programming Assignment #3
// NaiveBayes
// 2012

//
// Things for you to implement are marked with TODO!
// Generally, you should not need to touch things *not* marked TODO
//
// Remember that when you submit your code, it is not run from the command line 
// and your main() will *not* be run. To be safest, restrict your changes to
// addExample() and classify() and anything you further invoke from there.
//

import java.util.*;
import java.util.regex.*;
import java.io.*;

public class NaiveBayes {

  public static boolean FILTER_STOP_WORDS = false; // this gets set in main()
  private static List<String> stopList = readFile(new File("../data/english.stop"));

  private List<String> klassList = new ArrayList<String>();
  private Set<String> vocabulary = new HashSet<String>();
  private HashMap<String,Integer> countDocs = new HashMap<String,Integer>();
  private HashMap<String,HashMap<String,Integer>> klassDocsMap = new HashMap<String,HashMap<String,Integer>>();
  
 
  //TODO
  /**
   * Put your code for adding information to your NB classifier here
   **/
  public void addExample(String klass, List<String> words) {
    //pos, list of words  or neg, list of words
      
      for (String word:words) {    
          if(!vocabulary.contains(word))
              vocabulary.add(word);
      }
      
      if (!klassList.contains(klass)) {
          klassList.add(klass);
          //System.out.println(klassList);
          HashMap<String,Integer> bw = new HashMap<String,Integer>();
          klassDocsMap.put(klass,bw);
          countDocs.put(klass,new Integer(0));
      }
      
      for (String word:words) {
         HashMap<String,Integer> bw = klassDocsMap.get(klass);
         if (bw.containsKey(word)) {
             int value = bw.get(word).intValue();
             bw.put(word,new Integer(value+1));
         } else {
             bw.put(word,new Integer(1));
         }
         
        int count =  countDocs.get(klass).intValue();
        countDocs.put(klass,new Integer(count+1));
         
      }//for
      
  }
  
  //TODO
  /**
   *  Put your code here for deciding the class of the input file.
   *  Currently, it just randomly chooses "pos" or "negative"
   */  
  public String classify(List<String> words) {
      
 //System.out.println("VocabSize "+vocabulary.size());
     
      
 //System.out.println("NegdocBWSize "+countDocs.get("neg"));
  //System.out.println("PosdocBWSize "+countDocs.get("pos"));
    
    
    String result=null;
    double min=9999999;
    
    for (String klass:klassList) {
        double wordContrib=0.0;
    double totalContrib=0;
        //System.out.println("Current Class "+klass);
        for(String word:words) {
            HashMap<String,Integer> bw = klassDocsMap.get(klass);
            if(bw.containsKey(word)){
                
                wordContrib = Math.log(1+bw.get(word).intValue());
                wordContrib=wordContrib-Math.log(countDocs.get(klass).intValue()+vocabulary.size());
                wordContrib*=-1;
            } else {
                wordContrib = Math.log(1);
                wordContrib=wordContrib-Math.log(countDocs.get(klass).intValue()+vocabulary.size());
                wordContrib*=-1;
            }
            //System.out.println("WordContrib " +wordContrib);
            totalContrib+=wordContrib;
        }   
        //System.out.println("Total Contrib" + totalContrib);
        if (totalContrib<min) {
            min=totalContrib;
            result = klass;
        }
    }
    
    
      
    return result;
    
    
  }
  
  


  public void train(String trainPath) {
    File trainDir = new File(trainPath);
    if (!trainDir.isDirectory()) {
      System.err.println("[ERROR]\tinvalid training directory specified.  ");
    }

    TrainSplit split = new TrainSplit();
    for(File dir: trainDir.listFiles()) {
	if(!dir.getName().startsWith(".")) {
	    List<File> dirList = Arrays.asList(dir.listFiles());
	    for(File f: dirList) {
	      split.train.add(f);
	    }
	}
    }
    for(File file: split.train) {
      String klass = file.getParentFile().getName();
      List<String> words = readFile(file);
	    if (FILTER_STOP_WORDS) {words = filterStopWords(words);}
        addExample(klass,words);
    }
    return;
  }

  public List<List<String>> readTest(String ch_aux) {
    List<List<String>> data = new ArrayList<List<String>>();
    String [] docs = ch_aux.split("###");
    TrainSplit split = new TrainSplit();
    for(String doc : docs) {
      List<String> words = segmentWords(doc);
      if (FILTER_STOP_WORDS) {words = filterStopWords(words);}
      data.add(words);
    }
    return data;
  }

     
  /**
   * This class holds the list of train and test files for a given CV fold
   * constructed in getFolds()
   **/
  public static class TrainSplit {
    // training files for this split
    List<File> train = new ArrayList<File>(); 
    // test files for this split;
    List<File> test = new ArrayList<File>();
  }

  public static int numFolds = 10;

  /**
   * This creates train/test splits for each of the numFold folds.
   **/
  static public List<TrainSplit> getFolds(List<File> files) {
    List<TrainSplit> splits = new ArrayList<TrainSplit>();
    
    for( Integer fold=0; fold<numFolds; fold++ ) {
      TrainSplit split = new TrainSplit();
      for(File file: files) {
        if( file.getName().subSequence(2,3).equals(fold.toString()) ) {
          split.test.add(file);
        } else {
          split.train.add(file);
        }
      }

      splits.add(split);
    }
    return splits;
  }

  // returns accuracy 
  public double evaluate(TrainSplit split) {
    int numCorrect = 0;
    for (File file : split.test) {
      String klass = file.getParentFile().getName();
	    List<String> words = readFile(file);
	    if (FILTER_STOP_WORDS) {words = filterStopWords(words);}
      String guess = classify(words);
      if(klass.equals(guess)) {
	      numCorrect++;
      }
    }
    return ((double)numCorrect)/split.test.size();
  }


  /**
   * Remove any stop words or punctuation from a list of words.
   **/
  public static List<String> filterStopWords(List<String> words) {
    List<String> filtered = new ArrayList<String>();
    for (String word :words) {
      if (!stopList.contains(word) && !word.matches(".*\\W+.*")) {
	filtered.add(word);
      }
    }
    return filtered;
  }

  /** 
   * Code for reading a file.  you probably don't want to modify anything here, 
   * unless you don't like the way we segment files.
   **/
  private static List<String> readFile(File f) {
    try {
      StringBuilder contents = new StringBuilder();

      BufferedReader input = new BufferedReader(new FileReader(f));
      for(String line = input.readLine(); line != null; line = input.readLine()) {
        contents.append(line);
        contents.append("\n");
      }
      input.close();

      return segmentWords(contents.toString());
      
    } catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
      return null;
    } 
  }

  /**
   * Splits lines on whitespace for file reading
   **/
  private static List<String> segmentWords(String s) {
    List<String> ret = new ArrayList<String>();
    
    for(String word:  s.split("\\s")) {
      if(word.length() > 0) {
        ret.add(word);
      }
    }
    return ret;
  }

  public List<TrainSplit> getTrainSplits(String trainPath) {
    File trainDir = new File(trainPath);
    if (!trainDir.isDirectory()) {
      System.err.println("[ERROR]\tinvalid training directory specified.  ");
    }
    List<TrainSplit> splits = new ArrayList<TrainSplit>();
    List<File> files = new ArrayList<File>();
    for(File dir: trainDir.listFiles()) {
	if(!dir.getName().startsWith(".")) {
	    List<File> dirList = Arrays.asList(dir.listFiles());
	    for(File f: dirList) {
	      files.add(f);
	    }
	}
    }
    splits = getFolds(files);
    return splits;
  }

  
  /**
   * build splits according to command line args.  If args.length==1
   * do 10-fold cross validation, if args.length==2 create one TrainSplit
   * with all files from the train_dir and all files from the test_dir
   */
  private static List<TrainSplit> buildSplits(List<String> args) {
    File trainDir = new File(args.get(0));
    if (!trainDir.isDirectory()) {
      System.err.println("[ERROR]\tinvalid training directory specified.  ");
    }

    List<TrainSplit> splits = new ArrayList<TrainSplit>();
    if (args.size() == 1) {
      System.out.println("[INFO]\tPerforming 10-fold cross-validation on data set:\t"+args.get(0));
      List<File> files = new ArrayList<File>();
      for(File dir: trainDir.listFiles()) {
	if(!dir.getName().startsWith(".")) {
		List<File> dirList = Arrays.asList(dir.listFiles());
		for(File f: dirList) {
		  files.add(f);
		}
	}
      }
      //System.out.println(files);
      splits = getFolds(files);
    } else if (args.size() == 2) {
      // testing/training on two different data sets is treated like a single fold
      System.out.println("[INFO]\tTraining on data set:\t"+args.get(0)+" testing on data set:\t"+args.get(1));
      TrainSplit split = new TrainSplit();
      for(File dir: trainDir.listFiles()) {
	if(!dir.getName().startsWith(".")) {
		List<File> dirList = Arrays.asList(dir.listFiles());
		for(File f: dirList) {
		  split.train.add(f);
		}
	}
      }
      File testDir = new File(args.get(1));
      if (!testDir.isDirectory()) {
	System.err.println("[ERROR]\tinvalid testing directory specified.  ");
      }
      for(File dir: testDir.listFiles()) {
	if(!dir.getName().startsWith(".")) {
		List<File> dirList = Arrays.asList(dir.listFiles());
		for(File f: dirList) {
		  split.test.add(f);
		}
	}
      }
      splits.add(split);
    }
    return splits;
  }

  public void train(TrainSplit split) {
      for(File file: split.train) {
        String klass = file.getParentFile().getName();
        List<String> words = readFile(file);
	if (FILTER_STOP_WORDS) {words = filterStopWords(words);}
        addExample(klass,words);
      }
  }


  public static void main(String[] args) {
    List<String> otherArgs = Arrays.asList(args);
    if ( args.length > 0 && args[0].equals("-f") ) {
      FILTER_STOP_WORDS = true;
      otherArgs = otherArgs.subList(1,otherArgs.size());
    }
    if (otherArgs.size() < 1 || otherArgs.size() > 2) {
      System.out.println("[ERROR]\tInvalid number of arguments");
      System.out.println("\tUsage: java -cp [-f] trainDir [testDir]");
      System.out.println("\tWith -f flag implements stop word removal.");
      System.out.println("\tIf testDir is omitted, 10-fold cross validation is used for evaluation");
      return;
    }
    System.out.println("[INFO]\tFILTER_STOP_WORDS="+FILTER_STOP_WORDS);
    
   
    List<TrainSplit> splits = buildSplits(otherArgs);
    //System.out.println(splits);
    
    double avgAccuracy = 0.0;
    int fold = 0;
    for(TrainSplit split: splits) {
      NaiveBayes classifier = new NaiveBayes();
      double accuracy = 0.0;

      for(File file: split.train) {
        String klass = file.getParentFile().getName();
        List<String> words = readFile(file);
	if (FILTER_STOP_WORDS) {words = filterStopWords(words);}
        classifier.addExample(klass,words);
      }

      for (File file : split.test) {
        String klass = file.getParentFile().getName();
	List<String> words = readFile(file);
        if (FILTER_STOP_WORDS) {words = filterStopWords(words);}
        String guess = classifier.classify(words);
        if(klass.equals(guess)) {
	  accuracy++;
        }
      }
      accuracy = accuracy/split.test.size();
      avgAccuracy += accuracy;
      System.out.println("[INFO]\tFold " + fold + " Accuracy: " + accuracy);
      fold += 1;
    }
    avgAccuracy = avgAccuracy / numFolds;
    System.out.println("[INFO]\tAccuracy: " + avgAccuracy);
    

  } //main
  
}
