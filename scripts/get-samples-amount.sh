#!/bin/bash
curl -s -w "\n" -X GET http://localhost:4321/admin/samplesAmount | pjson
