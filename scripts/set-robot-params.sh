#!/bin/bash
while [[ $# -gt 0 ]]
do
    case "$1" in
        --learn-rate)
            shift
            if [[ $# -gt 0 ]]; then
                data="$data&learnRate=$1"
            else
                echo "No learn rate specified"
            fi
            shift
            ;;
        --samples)
            shift
            if [[ $# -gt 0 ]]; then
                data="$data&samplesPerEpoch=$1"
            else
                echo "No samples per epoch specified"
            fi
            shift
            ;;
        *)
            break
            ;;
    esac
done

curl -s -X POST -d "$data" http://localhost:4321/robot/params
