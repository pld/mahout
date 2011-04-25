#!/bin/bash

bin/mahout seqdirectory --input             ../data/terse/enron \
                        --output            out-trec_legal-seqfiles \
                        --charset           utf-8

wait

bin/mahout seq2sparse   --input             out-trec_legal-seqfiles    \
                        --output            out-trec_legal-vectors     \
                        --maxNGramSize      2                        \
                        --namedVector                                \
                        --minDF             4                        \
                        --maxDFPercent      75                       \
                        --weight            TFIDF                    \
                        --norm              2                        \
                        --analyzerName      org.apache.mahout.analysis.TRECLegalAnalyzer

