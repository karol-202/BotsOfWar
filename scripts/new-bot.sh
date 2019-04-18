#!/bin/bash
if [ $# == 2 ]
then
    curl -s -X POST -d "name=$1&randomRange=$2" http://localhost:4321/admin/newBot
else
    echo "2 parameters required"
fi
