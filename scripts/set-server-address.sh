#!/bin/bash
if [ $# == 1 ]
then
    curl -s -X POST -d "address=$1" http://localhost:4321/admin/serverAddress
else
    echo "1 parameter required"
fi
