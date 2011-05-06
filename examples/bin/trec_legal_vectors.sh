#!/bin/bash

for topic in 200 201 202 203 204 205 206 207
do
  # clear old data
  rm -r out-trec_legal_$topic-seqfiles
  rm -r out-trec_legal_$topic-vectors

  bin/mahout seqdirectory --input             ../data/rel/$topic/1 \
                          --output            out-trec_legal_$topic-seqfiles \
                          --charset           utf-8

  wait

  bin/mahout seq2sparse   --input             out-trec_legal_$topic-seqfiles    \
                          --output            out-trec_legal_$topic-vectors     \
                          --maxNGramSize      2                        \
                          --namedVector                                \
                          --minDF             4                        \
                          --maxDFPercent      75                       \
                          --weight            TFIDF                    \
                          --norm              2                        \
                          --analyzerName      org.apache.mahout.analysis.TRECLegalAnalyzer
done

