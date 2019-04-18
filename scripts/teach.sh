#!/bin/bash
if [ $# -ge 2 ]
then
    data="learnRate=$1"; shift
    data="$data&samplesAmount=$1"; shift
    while [ $# -gt 0 ]
    do
	data="$data&bots=$1"; shift
    done
    curl -s -X POST -d "$data" http://localhost:4321/admin/teach
else
    echo "3 or more parameters required"
fi
