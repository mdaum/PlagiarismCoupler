#!/bin/sh
java -jar jplag-2.11.8-SNAPSHOT-jar-with-dependencies.jar -l java17 -s -r JPlagResults -m $1 $2 $3
