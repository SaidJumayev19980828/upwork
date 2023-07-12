#!/bin/sh

# This awk script calculates a code coverage %
# usage: pass the the jacoco.csv file as first argument

awk -F "," '
    {
      instructions += $4 + $5; covered += $5
    }
    END {
      print covered, "/", instructions, "instructions covered";
      printf "Code coverage: %.9f%%\n", covered / instructions * 100
    }' "$1"