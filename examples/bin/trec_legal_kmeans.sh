#!/bin/bash

bin/mahout kmeans       --input            out-trec_legal-vectors/tfidf-vectors \
                        --output           out-trec_legal-kmeans/clusters \
                        --clusters         out-trec_legal-kmeans/initialclusters \
                        --maxIter          10 \
                        --numClusters      100 \
                        --clustering       \
                        --overwrite
wait

bin/mahout clusterdump  --seqFileDir        out-trec_legal-kmeans/clusters/clusters-1 \
                        --pointsDir         out-trec_legal-kmeans/clusters/clusteredPoints \
                        --numWords          5 \
                        --dictionary        out-trec_legal-vectors/dictionary.file-0 \
                        --dictionaryType    sequencefile
