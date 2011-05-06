#!/bin/bash

for topic in 200 201 202 203 204 205 206 207
do
  bin/mahout clusterdump  --seqFileDir        out-trec_legal_$topic-kmeans/clusters/clusters-1 \
                          --pointsDir         out-trec_legal_$topic-kmeans/clusters/clusteredPoints \
                          --numWords          50 \
                          --dictionary        out-trec_legal_$topic-vectors/dictionary.file-0 \
                          --dictionaryType    sequencefile \
                          --output            clusteranalyze_$topic.log
done

