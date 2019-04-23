#!/bin/bash
if [[ $# == 4 ]]
then
    data="bot1Name=$1"; shift
    data="$data&bot1WeightRange=$1"; shift
    data="$data&bot2Name=$1"; shift
    data="$data&bot2WeightRange=$1"; shift
    curl -s -X POST -d "$data" http://localhost:4321/robot/initTeaching
else
    echo "4 parameters required; usage:"
    echo "./teach-init.sh <bot1-name> <bot1-weight-range> <bot2-name> <bot2-weight-range>"
fi
