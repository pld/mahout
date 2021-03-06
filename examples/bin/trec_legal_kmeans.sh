#!/bin/bash

for topic in 200 201 202 203 204 205 206 207
do
  # clear old data
  rm -r out-trec_legal_$topic-kmeans/*

  bin/mahout kmeans       --input            out-trec_legal_$topic-vectors/tfidf-vectors \
                          --output           out-trec_legal_$topic-kmeans/clusters \
                          --clusters         out-trec_legal_$topic-kmeans/initialclusters \
                          --maxIter          10 \
                          --numClusters      5 \
                          --clustering       \
                          --overwrite
  wait

  bin/mahout clusterdump  --seqFileDir        out-trec_legal_$topic-kmeans/clusters/clusters-1 \
                          --pointsDir         out-trec_legal_$topic-kmeans/clusters/clusteredPoints \
                          --numWords          50 \
                          --dictionary        out-trec_legal_$topic-vectors/dictionary.file-0 \
                          --dictionaryType    sequencefile \
                          --output            clusteranalyze_$topic.log
done

