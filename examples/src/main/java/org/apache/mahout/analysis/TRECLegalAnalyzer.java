package org.apache.mahout.analysis;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.lang.Override;import java.lang.String;import java.lang.SuppressWarnings;import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Peter Lubell-Doughtie 
 */
public class TRECLegalAnalyzer extends Analyzer {

  //final List<String> stopWords = Arrays.asList(
  //);

  public TRECLegalAnalyzer() {
  }

  @SuppressWarnings("unchecked")
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    TokenStream tokenStream = new LetterTokenizer(reader);
    tokenStream = new StandardFilter(tokenStream);
    tokenStream = new LengthFilter(tokenStream, 4, 20);

    tokenStream = new LowerCaseFilter(tokenStream);

    return tokenStream;
  }
}

